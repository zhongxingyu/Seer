 /*
  * Copyright 2011 Miltiadis Allamanis
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
  */
  
 package uk.ac.cam.cl.passgori;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import com.google.nigori.client.DAG;
 import com.google.nigori.common.Revision;
 
 /**
  * 
  */
 
 /**
  * A memory unsafe password store.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class MemoryUnsafePasswordStore implements IPasswordStore {
 
 	/**
 	 * Variable indicating if the user has been authorized.
 	 */
 	private boolean mAuthorized = false;
 
 	/**
 	 * A hash map storing the passwords.
 	 */
 	protected final HashMap<String, Password> mPasswordStore;
 
 	public MemoryUnsafePasswordStore() {
 		mPasswordStore = new HashMap<String, Password>();
 	}
 
 	@Override
 	public boolean authorize(String username, String password) {
 		mAuthorized = true;
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IPasswordStore#getAllStoredPasswordIds()
 	 */
 	@Override
 	public List<String> getAllStoredPasswordIds() throws PasswordStoreException {
 		if (!mAuthorized)
 			throw new PasswordStoreException("Not Connected/Authorized");
 		final Set<String> idSet = mPasswordStore.keySet();
 		return new ArrayList<String>(idSet);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IPasswordStore#removePassword(java.lang.String)
 	 */
 	@Override
 	public boolean removePassword(String aId) throws PasswordStoreException {
 		if (!mAuthorized)
 			throw new PasswordStoreException("Not Connected/Authorized");
 		return mPasswordStore.remove(aId) != null;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IPasswordStore#retrivePassword(java.lang.String)
 	 */
 	@Override
 	public Password retrivePassword(String aId) throws PasswordStoreException {
 		if (!mAuthorized)
 			throw new PasswordStoreException("Not Connected/Authorized");
 		return mPasswordStore.get(aId);
 	}
 
 	@Override
   public Password retrivePassword(String string, Revision revision) throws PasswordStoreException {
     return retrivePassword(string);
   }
 
   /*
 	 * (non-Javadoc)
 	 * 
 	 * @see IPasswordStore#storePassword(Password)
 	 */
 	@Override
 	public boolean storePassword(Password aPassword)
 			throws PasswordStoreException {
 		if (!mAuthorized)
 			throw new PasswordStoreException("Not Connected/Authorized");
 		return mPasswordStore.put(aPassword.getId(), aPassword) != null;
 	}
 
   @Override
   public boolean destroyStore() throws PasswordStoreException {
     mPasswordStore.clear();
     mAuthorized = false;
     return true;
   }
 
   @Override
   public DAG<Revision> getHistory(String mPasswordTitle) {
     // TODO(drt24) implement getHistory
     return null;
   }
 
   @Override
   public void backup(OutputStream output, String password) throws IOException {
     ObjectOutputStream oos = new ObjectOutputStream(output);
     oos.writeObject(mPasswordStore);
     oos.flush();
     oos.close();
   }
 
  @SuppressWarnings("unchecked")// exception caught
   @Override
   public void restore(InputStream input, String password) throws IOException,
       ClassNotFoundException {
     ObjectInputStream ois = new ObjectInputStream(input);
 
     try {
       mPasswordStore.putAll((HashMap<String, Password>) ois.readObject());
     } catch (ClassCastException e) {
       throw new ClassNotFoundException(e.toString());
     }
     ois.close();
   }
 
 }
