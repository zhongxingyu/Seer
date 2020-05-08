 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.box.server.persistence.file;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 import org.waveprotocol.box.server.account.AccountData;
 import org.waveprotocol.box.server.persistence.AccountStore;
 import org.waveprotocol.box.server.persistence.PersistenceException;
 import org.waveprotocol.box.server.persistence.protos.ProtoAccountDataSerializer;
 import org.waveprotocol.box.server.persistence.protos.ProtoAccountStoreData.ProtoAccountData;
 import org.waveprotocol.box.server.util.Log;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 
 /**
  * A flat file based implementation of {@link AccountStore}
  * 
  * @author tad.glines@gmail.com (Tad Glines)
  */
 public class FileAccountStore implements AccountStore {
   private static final String ACCOUNT_FILE_EXTENSION = ".account";
   private final String accountStoreBasePath;
   private final Map<ParticipantId, AccountData> accounts = Maps.newHashMap();
 
   private static final Log LOG = Log.get(FileAccountStore.class);
   
   @Inject
   public FileAccountStore(@Named("account_store_directory") String accountStoreBasePath) {
     Preconditions.checkNotNull(accountStoreBasePath, "Requested path is null");
     this.accountStoreBasePath = accountStoreBasePath;
   }
 
   @Override
   public void initializeAccountStore() throws PersistenceException {
     File baseDir = new File(accountStoreBasePath);
     
     // Make sure accountStoreBasePath exists.
     if (!baseDir.exists()) {
       // It doesn't so try and create it.
       if (!baseDir.mkdirs()) {
         throw new PersistenceException("Configured account store directory ("
             + accountStoreBasePath + ") doesn't exist and could not be created!");
       }
     }
     
     // Make sure accountStoreBasePath is a directory.
     if (!baseDir.isDirectory()) {
       throw new PersistenceException("Configured account store path ("
           + accountStoreBasePath + ") isn't a directory!");
     }
     
     // Make sure we can read accounts by trying to read one of the account files.
     File[] files = baseDir.listFiles(new FilenameFilter() {
       @Override
       public boolean accept(File dir, String name) {
         return name.endsWith(ACCOUNT_FILE_EXTENSION);
       }
     });
 
     if (files == null) {
       throw new PersistenceException("Configured account store directory ("
           + accountStoreBasePath + ") does not appear to be readable!");
     }
 
    /*
     * If file list isn't empty, try opening the first file in the list to make sure it
     * is readable. If the first file is readable, then it is likely that the rest will
     * be readable as well.
     */
    if (files.length > 0) {
      try {
        FileInputStream file = new FileInputStream(files[0]);
        file.read();
      } catch (IOException e) {
        throw new PersistenceException("Failed to read '" + files[0].getName()
            + "' in configured account store directory '" + accountStoreBasePath
            + "'. The directory's contents do not appear to be readable.", e);
      }
     }
     
     // Make sure accountStoreBasePath is a writable.
     try {
       File tmp = File.createTempFile("tempInitialization", ".bugus_account", baseDir);
      FileOutputStream stream = new FileOutputStream(tmp);
      stream.write(new byte[]{'H','e','l','l','o'});
      stream.close();
       tmp.delete();
     } catch (IOException e) {
       throw new PersistenceException("Configured account store directory ("
          + accountStoreBasePath + ") does not appear to be writable!", e);
     }
   }
   
   @Override
   public AccountData getAccount(ParticipantId id) throws PersistenceException {
     synchronized (accounts) {
       AccountData account = accounts.get(id);
       if (account == null) {
         account = readAccount(id);
         if (account != null) {
           accounts.put(id, account);
         }
       }
       return account;
     }
   }
 
   @Override
   public void putAccount(AccountData account) throws PersistenceException {
     synchronized (accounts) {
       Preconditions.checkNotNull(account);
       writeAccount(account);
       accounts.put(account.getId(), account);
     }
   }
 
   @Override
   public void removeAccount(ParticipantId id) throws PersistenceException {
     synchronized (accounts) {
       File file = new File(participantIdToFileName(id));
       if (file.exists()) {
         if (!file.delete()) {
           throw new PersistenceException("Failed to delete account data associated with "
               + id.getAddress());
         }
       }
       accounts.remove(id);
     }
   }
 
   private String participantIdToFileName(ParticipantId id) {
     return accountStoreBasePath + File.separator + id.getAddress().toLowerCase()
         + ACCOUNT_FILE_EXTENSION;
   }
   
   /*
    * This is here instead of in a utility class so that a more useful error message
    * can be generated.
    */
   private void closeAndIgnoreException(Closeable closeable) {
     if (closeable != null) {
       try {
         closeable.close();
       } catch (IOException e) {
         // This should never happen in practice. But just in case... log it.
         LOG.warning("Failed to close account data file!", e);
       }
     }
   }
   
   private AccountData readAccount(ParticipantId id) throws PersistenceException {
     FileInputStream file = null;
     try {
       File accountFile = new File(participantIdToFileName(id));
       if (!accountFile.exists()) {
         return null;
       }
       file = new FileInputStream(accountFile);
       ProtoAccountData data = ProtoAccountData.newBuilder().mergeFrom(file).build();
       return ProtoAccountDataSerializer.deserialize(data);
     } catch (IOException e) {
       LOG.severe("Failed to read account data from disk!", e);
       throw new PersistenceException(e);
     } finally {
       closeAndIgnoreException(file);
     }
   }
   
   private void writeAccount(AccountData account) throws PersistenceException {
     OutputStream file = null;
     try {
       File accountFile = new File(participantIdToFileName(account.getId()));
       file = new FileOutputStream(accountFile);
       ProtoAccountData data = ProtoAccountDataSerializer.serialize(account);
       file.write(data.toByteArray());
       file.flush();
     } catch (IOException e) {
       LOG.severe("Failed to write account data to disk!", e);
       throw new PersistenceException(e);
     } finally {
       closeAndIgnoreException(file);
     }
   }
 }
