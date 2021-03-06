 package com.fsck.k9.preferences;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import javax.crypto.CipherOutputStream;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Environment;
 import android.util.Log;
 import android.util.Xml;
 
 import com.fsck.k9.Account;
 import com.fsck.k9.K9;
 import com.fsck.k9.Preferences;
 import com.fsck.k9.helper.Utility;
 import com.fsck.k9.mail.Store;
 import com.fsck.k9.mail.ServerSettings;
 import com.fsck.k9.mail.Transport;
 import com.fsck.k9.mail.store.LocalStore;
 
 
 public class StorageExporter {
     private static final String EXPORT_FILENAME = "settings.k9s";
 
     public static final String ROOT_ELEMENT = "k9settings";
     public static final String VERSION_ATTRIBUTE = "version";
     public static final String FILE_FORMAT_ATTRIBUTE = "format";
     public static final String GLOBAL_ELEMENT = "global";
     public static final String SETTINGS_ELEMENT = "settings";
     public static final String ACCOUNTS_ELEMENT = "accounts";
     public static final String ACCOUNT_ELEMENT = "account";
     public static final String UUID_ATTRIBUTE = "uuid";
     public static final String INCOMING_SERVER_ELEMENT = "incoming-server";
     public static final String OUTGOING_SERVER_ELEMENT = "outgoing-server";
     public static final String TYPE_ATTRIBUTE = "type";
     public static final String HOST_ELEMENT = "host";
     public static final String PORT_ELEMENT = "port";
     public static final String CONNECTION_SECURITY_ELEMENT = "connection-security";
     public static final String AUTHENTICATION_TYPE_ELEMENT = "authentication-type";
     public static final String USERNAME_ELEMENT = "username";
     public static final String PASSWORD_ELEMENT = "password";
     public static final String EXTRA_ELEMENT = "extra";
     public static final String IDENTITIES_ELEMENT = "identities";
     public static final String IDENTITY_ELEMENT = "identity";
     public static final String FOLDERS_ELEMENT = "folders";
     public static final String FOLDER_ELEMENT = "folder";
     public static final String NAME_ATTRIBUTE = "name";
     public static final String VALUE_ELEMENT = "value";
     public static final String KEY_ATTRIBUTE = "key";
     public static final String NAME_ELEMENT = "name";
     public static final String EMAIL_ELEMENT = "email";
     public static final String DESCRIPTION_ELEMENT = "description";
 
 
     public static String exportToFile(Context context, boolean includeGlobals,
             Set<String> accountUuids, String encryptionKey)
             throws StorageImportExportException {
 
         OutputStream os = null;
         try
         {
             File dir = new File(Environment.getExternalStorageDirectory() + File.separator
                                 + context.getPackageName());
             dir.mkdirs();
             File file = Utility.createUniqueFile(dir, EXPORT_FILENAME);
             String fileName = file.getAbsolutePath();
             os = new FileOutputStream(fileName);
 
             if (encryptionKey == null) {
                 exportPreferences(context, os, includeGlobals, accountUuids);
             } else {
                 exportPreferencesEncrypted(context, os, includeGlobals, accountUuids,
                         encryptionKey);
             }
 
             // If all went well, we return the name of the file just written.
             return fileName;
         } catch (Exception e) {
             throw new StorageImportExportException(e);
         } finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (IOException ioe) {}
             }
         }
     }
 
     public static void exportPreferencesEncrypted(Context context, OutputStream os, boolean includeGlobals,
             Set<String> accountUuids, String encryptionKey) throws StorageImportExportException  {
 
         try {
             K9Krypto k = new K9Krypto(encryptionKey, K9Krypto.MODE.ENCRYPT);
             CipherOutputStream cos = new CipherOutputStream(os, k.mCipher);
 
             exportPreferences(context, cos, includeGlobals, accountUuids);
         } catch (Exception e) {
             throw new StorageImportExportException();
         }
     }
 
     public static void exportPreferences(Context context, OutputStream os, boolean includeGlobals,
             Set<String> accountUuids) throws StorageImportExportException  {
 
         try {
             XmlSerializer serializer = Xml.newSerializer();
             serializer.setOutput(os, "UTF-8");
 
             serializer.startDocument(null, Boolean.valueOf(true));
 
             // Output with indentation
             serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
 
             serializer.startTag(null, ROOT_ELEMENT);
             //TODO: write content version number here
             serializer.attribute(null, VERSION_ATTRIBUTE, "x");
             //TODO: set file format version to "1" once the feature is stable and about to be merged into master
             serializer.attribute(null, FILE_FORMAT_ATTRIBUTE, "y");
 
             Log.i(K9.LOG_TAG, "Exporting preferences");
 
             Preferences preferences = Preferences.getPreferences(context);
             SharedPreferences storage = preferences.getPreferences();
 
            Set<String> exportAccounts;
             if (accountUuids == null) {
                 Account[] accounts = preferences.getAccounts();
                exportAccounts = new HashSet<String>();
                 for (Account account : accounts) {
                    exportAccounts.add(account.getUuid());
                 }
            } else {
                exportAccounts = accountUuids;
             }
 
             Map<String, Object> prefs = new TreeMap<String, Object>(storage.getAll());
 
             if (includeGlobals) {
                 serializer.startTag(null, GLOBAL_ELEMENT);
                 writeSettings(serializer, prefs);
                 serializer.endTag(null, GLOBAL_ELEMENT);
             }
 
             serializer.startTag(null, ACCOUNTS_ELEMENT);
            for (String accountUuid : exportAccounts) {
                 Account account = preferences.getAccount(accountUuid);
                 writeAccount(serializer, account, prefs);
             }
             serializer.endTag(null, ACCOUNTS_ELEMENT);
 
             serializer.endTag(null, ROOT_ELEMENT);
             serializer.endDocument();
             serializer.flush();
 
         } catch (Exception e) {
             throw new StorageImportExportException(e.getLocalizedMessage(), e);
         }
     }
 
     private static void writeSettings(XmlSerializer serializer,
             Map<String, Object> prefs) throws IOException {
 
         for (Map.Entry<String, Object> entry : prefs.entrySet()) {
             String key = entry.getKey();
             String value = entry.getValue().toString();
             if (key.indexOf('.') != -1) {
                 // Skip account entries
                 continue;
             }
             writeKeyValue(serializer, key, value);
         }
     }
 
     private static void writeAccount(XmlSerializer serializer, Account account,
             Map<String, Object> prefs) throws IOException {
 
         Set<Integer> identities = new HashSet<Integer>();
         Set<String> folders = new HashSet<String>();
         String accountUuid = account.getUuid();
 
         serializer.startTag(null, ACCOUNT_ELEMENT);
         serializer.attribute(null, UUID_ATTRIBUTE, accountUuid);
 
         String name = (String) prefs.get(accountUuid + "." + Account.ACCOUNT_DESCRIPTION_KEY);
         if (name != null) {
             serializer.startTag(null, NAME_ELEMENT);
             serializer.text(name);
             serializer.endTag(null, NAME_ELEMENT);
         }
 
 
         // Write incoming server settings
         ServerSettings incoming = Store.decodeStoreUri(account.getStoreUri());
         serializer.startTag(null, INCOMING_SERVER_ELEMENT);
         serializer.attribute(null, TYPE_ATTRIBUTE, incoming.type);
 
         writeElement(serializer, HOST_ELEMENT, incoming.host);
         if (incoming.port != -1) {
             writeElement(serializer, PORT_ELEMENT, Integer.toString(incoming.port));
         }
         writeElement(serializer, CONNECTION_SECURITY_ELEMENT, incoming.connectionSecurity.name());
         writeElement(serializer, AUTHENTICATION_TYPE_ELEMENT, incoming.authenticationType);
         writeElement(serializer, USERNAME_ELEMENT, incoming.username);
         //TODO: make saving the password optional
         writeElement(serializer, PASSWORD_ELEMENT, incoming.password);
 
         Map<String, String> extras = incoming.getExtra();
         if (extras != null && extras.size() > 0) {
             serializer.startTag(null, EXTRA_ELEMENT);
             for (Entry<String, String> extra : extras.entrySet()) {
                 writeKeyValue(serializer, extra.getKey(), extra.getValue());
             }
             serializer.endTag(null, EXTRA_ELEMENT);
         }
 
         serializer.endTag(null, INCOMING_SERVER_ELEMENT);
 
 
         // Write outgoing server settings
         ServerSettings outgoing = Transport.decodeTransportUri(account.getTransportUri());
         serializer.startTag(null, OUTGOING_SERVER_ELEMENT);
         serializer.attribute(null, TYPE_ATTRIBUTE, outgoing.type);
 
         writeElement(serializer, HOST_ELEMENT, outgoing.host);
         if (outgoing.port != -1) {
             writeElement(serializer, PORT_ELEMENT, Integer.toString(outgoing.port));
         }
         writeElement(serializer, CONNECTION_SECURITY_ELEMENT, outgoing.connectionSecurity.name());
         writeElement(serializer, AUTHENTICATION_TYPE_ELEMENT, outgoing.authenticationType);
         writeElement(serializer, USERNAME_ELEMENT, outgoing.username);
         //TODO: make saving the password optional
         writeElement(serializer, PASSWORD_ELEMENT, outgoing.password);
 
         extras = outgoing.getExtra();
         if (extras != null && extras.size() > 0) {
             serializer.startTag(null, EXTRA_ELEMENT);
             for (Entry<String, String> extra : extras.entrySet()) {
                 writeKeyValue(serializer, extra.getKey(), extra.getValue());
             }
             serializer.endTag(null, EXTRA_ELEMENT);
         }
 
         serializer.endTag(null, OUTGOING_SERVER_ELEMENT);
 
 
         // Write account settings
         serializer.startTag(null, SETTINGS_ELEMENT);
         for (Map.Entry<String, Object> entry : prefs.entrySet()) {
             String key = entry.getKey();
             String value = entry.getValue().toString();
             String[] comps = key.split("\\.");
             if (comps.length >= 2) {
                 String keyUuid = comps[0];
                 String secondPart = comps[1];
 
                 if (!keyUuid.equals(accountUuid)
                         || Account.ACCOUNT_DESCRIPTION_KEY.equals(secondPart)
                         || "storeUri".equals(secondPart)
                         || "transportUri".equals(secondPart)) {
                     continue;
                 }
                 if (comps.length == 3) {
                     String thirdPart = comps[2];
 
                     if (Account.IDENTITY_KEYS.contains(secondPart)) {
                         // This is an identity key. Save identity index for later...
                         try {
                             identities.add(Integer.parseInt(thirdPart));
                         } catch (NumberFormatException e) { /* ignore */ }
                         // ... but don't write it now.
                         continue;
                     }
 
                     if (LocalStore.FOLDER_SETTINGS_KEYS.contains(thirdPart)) {
                         // This is a folder key. Save folder name for later...
                         folders.add(secondPart);
                         // ... but don't write it now.
                         continue;
                     }
                 }
             } else {
                 // Skip global config entries and identity entries
                 continue;
             }
 
             // Strip account UUID from key
             String keyPart = key.substring(comps[0].length() + 1);
 
             writeKeyValue(serializer, keyPart, value);
         }
         serializer.endTag(null, SETTINGS_ELEMENT);
 
         if (identities.size() > 0) {
             serializer.startTag(null, IDENTITIES_ELEMENT);
 
             // Sort identity indices (that's why we store them as Integers)
             List<Integer> sortedIdentities = new ArrayList<Integer>(identities);
             Collections.sort(sortedIdentities);
 
             for (Integer identityIndex : sortedIdentities) {
                 writeIdentity(serializer, accountUuid, identityIndex.toString(), prefs);
             }
             serializer.endTag(null, IDENTITIES_ELEMENT);
         }
 
         if (folders.size() > 0) {
             serializer.startTag(null, FOLDERS_ELEMENT);
             for (String folder : folders) {
                 writeFolder(serializer, accountUuid, folder, prefs);
             }
             serializer.endTag(null, FOLDERS_ELEMENT);
         }
 
         serializer.endTag(null, ACCOUNT_ELEMENT);
     }
 
     private static void writeIdentity(XmlSerializer serializer, String accountUuid,
             String identity, Map<String, Object> prefs) throws IOException {
 
         serializer.startTag(null, IDENTITY_ELEMENT);
 
         String name = (String) prefs.get(accountUuid + "." + Account.IDENTITY_NAME_KEY +
                 "." + identity);
         serializer.startTag(null, NAME_ELEMENT);
         serializer.text(name);
         serializer.endTag(null, NAME_ELEMENT);
 
         String email = (String) prefs.get(accountUuid + "." + Account.IDENTITY_EMAIL_KEY +
                 "." + identity);
         serializer.startTag(null, EMAIL_ELEMENT);
         serializer.text(email);
         serializer.endTag(null, EMAIL_ELEMENT);
 
         String description = (String) prefs.get(accountUuid + "." +
                 Account.IDENTITY_DESCRIPTION_KEY + "." + identity);
         if (description != null) {
             serializer.startTag(null, DESCRIPTION_ELEMENT);
             serializer.text(description);
             serializer.endTag(null, DESCRIPTION_ELEMENT);
         }
 
         serializer.startTag(null, SETTINGS_ELEMENT);
         for (Map.Entry<String, Object> entry : prefs.entrySet()) {
             String key = entry.getKey();
             String value = entry.getValue().toString();
             String[] comps = key.split("\\.");
             if (comps.length >= 3) {
                 String keyUuid = comps[0];
                 String identityKey = comps[1];
                 String identityIndex = comps[2];
                 if (!keyUuid.equals(accountUuid) || !identityIndex.equals(identity)
                         || !Account.IDENTITY_KEYS.contains(identityKey)
                         || Account.IDENTITY_NAME_KEY.equals(identityKey)
                         || Account.IDENTITY_EMAIL_KEY.equals(identityKey)
                         || Account.IDENTITY_DESCRIPTION_KEY.equals(identityKey)) {
                     continue;
                 }
             } else {
                 // Skip non-identity config entries
                 continue;
             }
 
             writeKeyValue(serializer, comps[1], value);
         }
         serializer.endTag(null, SETTINGS_ELEMENT);
 
         serializer.endTag(null, IDENTITY_ELEMENT);
     }
 
     private static void writeFolder(XmlSerializer serializer, String accountUuid,
             String folder, Map<String, Object> prefs) throws IOException {
 
         serializer.startTag(null, FOLDER_ELEMENT);
         serializer.attribute(null, NAME_ATTRIBUTE, folder);
         for (Map.Entry<String, Object> entry : prefs.entrySet()) {
             String key = entry.getKey();
             String value = entry.getValue().toString();
             String[] comps = key.split("\\.");
             if (comps.length >= 3) {
                 String keyUuid = comps[0];
                 String folderName = comps[1];
                 String folderKey = comps[2];
                 if (!keyUuid.equals(accountUuid) || !folderName.equals(folder)
                         || !LocalStore.FOLDER_SETTINGS_KEYS.contains(folderKey)) {
                     continue;
                 }
             } else {
                 // Skip non-folder config entries
                 continue;
             }
 
             writeKeyValue(serializer, comps[2], value);
         }
         serializer.endTag(null, FOLDER_ELEMENT);
     }
 
     private static void writeElement(XmlSerializer serializer, String elementName, String value)
             throws IllegalArgumentException, IllegalStateException, IOException {
         if (value != null) {
             serializer.startTag(null, elementName);
             serializer.text(value);
             serializer.endTag(null, elementName);
         }
     }
 
     private static void writeKeyValue(XmlSerializer serializer, String key, String value)
             throws IllegalArgumentException, IllegalStateException, IOException {
         serializer.startTag(null, VALUE_ELEMENT);
         serializer.attribute(null, KEY_ATTRIBUTE, key);
         if (value != null) {
             serializer.text(value);
         }
         serializer.endTag(null, VALUE_ELEMENT);
     }
 }
