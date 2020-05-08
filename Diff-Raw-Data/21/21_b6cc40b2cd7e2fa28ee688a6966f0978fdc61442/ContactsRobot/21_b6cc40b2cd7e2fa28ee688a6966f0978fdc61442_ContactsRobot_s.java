 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2010 Funambol, Inc.
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  *
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.test.contact;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Enumeration;
 
 import org.json.me.JSONObject;
 import org.json.me.JSONArray;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.test.BasicScriptRunner;
 import com.funambol.client.test.ClientTestException;
 import com.funambol.client.test.Robot;
 import com.funambol.client.test.basic.BasicRobot;
 import com.funambol.client.test.util.CheckSyncClient;
 import com.funambol.client.test.util.CheckSyncSource;
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 public abstract class ContactsRobot extends Robot {
    
     private static final String TAG_LOG = "ContactsRobot";
 
     protected static final char FOLDING_INDENT_CHAR = ' ';
 
     protected long currentContactId = -1;
 
     protected long incrementalServerItemkey = 10000000;
 
     protected String contactAsVcard = null;
 
     protected BasicRobot basicRobot;
 
     protected AppSyncSourceManager appSourceManager;
 
     protected SapiHandler sapiHandler = null;
 
     public ContactsRobot(AppSyncSourceManager appSourceManager) {
         this.appSourceManager = appSourceManager;
     }
 
     private SapiHandler getSapiHandler() {
         if (sapiHandler == null) {
             Configuration configuration = getConfiguration();
             sapiHandler = new SapiHandler(StringUtil.extractAddressFromUrl(configuration.getSyncUrl()),
                                           configuration.getUsername(),
                                           configuration.getPassword());
 
         }
         return sapiHandler;
     }
 
     public void saveContactOnServer(String firstName, String lastName) throws Throwable {
 
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
 
         // Is this an add or an update?
         String id = findContactIdOnServer(firstName, lastName);
 
         StringBuffer jsonContact = new StringBuffer();
         jsonContact.append("{");
         if (id != null) {
             jsonContact.append("\"id\":\"").append(id).append("\",");
         }
         jsonContact.append("\"firstname\":\"").append(firstName)
                    .append("\",\"lastname\":\"").append(lastName).append("\"}");
        params.add("data=" + jsonContact.toString());
         Log.trace(TAG_LOG, "Save contact: " + jsonContact.toString());
         JSONObject resp = sapiHandler.query("contacts","contactsave",params,null,null);
 
         if (id == null) {
             id = resp.getString("id");
             Log.debug(TAG_LOG, "The new contact has id=" + id);
         }
     }
 
     public void saveContactOnServer(String jsonObj) throws Throwable {
 
         // Unescape the parameter
         jsonObj = StringUtil.replaceAll(jsonObj, "&quot;","\"");
         jsonObj = StringUtil.replaceAll(jsonObj, "&comma;",",");
 
         Log.trace(TAG_LOG, "JSON contact is: " + jsonObj); 
 
         JSONObject jsonContact = new JSONObject(jsonObj);
         // If the values are not available, an excpetion is thrown
         String firstName = jsonContact.getString("firstname");
         String lastName = jsonContact.getString("lastname");
 
         Log.trace(TAG_LOG, "firstName=" + firstName + ",lastName=" + lastName);
 
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
 
         // Is this an add or an update?
         String id = findContactIdOnServer(firstName, lastName);
 
         Log.trace(TAG_LOG, "Server id=" + id);
 
         if (id != null) {
             jsonContact.put("id",id);
         }
 
        params.add("data=" + jsonContact.toString());
         JSONObject resp = sapiHandler.query("contact","contactsave",params,null,null);
 
         if (id == null) {
             id = resp.getString("id");
             Log.debug(TAG_LOG, "The new contact has id=" + id);
         }
     }
 
     private String findContactIdOnServer(String firstName, String lastName) throws Throwable {
         SapiHandler sapiHandler = getSapiHandler();
         JSONObject resp = sapiHandler.query("contact","get",null,null,null);
         if (resp.has("contacts")) {
             JSONArray contacts = resp.getJSONArray("contacts");
             for(int i=0;i<contacts.length();++i) {
                 JSONObject c = contacts.getJSONObject(i);
                 String fName = c.getString("firstname");
                 String lName = c.getString("lastname");
 
                 if (firstName.equals(fName) && lastName.equals(lName)) {
                     String id = c.getString("id");
                     return id;
                 }
             }
         }
         return null;
     }
 
 
     public void deleteContactOnServer(String firstName, String lastName,
             CheckSyncClient client) throws Throwable {
 
         String id = findContactIdOnServer(firstName, lastName);
         if (id == null) {
             throw new ClientTestException("Contact not available on server");
         }
         Log.trace(TAG_LOG, "Deleting contact on server with key: " + id);
         SapiHandler sapiHandler = getSapiHandler();
         Vector params = new Vector();
         JSONObject param = new JSONObject();
         Vector ids = new Vector();
         ids.addElement(id);
         param.put("contacts", ids);
        params.add("data="+param.toString());
         sapiHandler.query("contacts","contactsdelete",params,null,null);
     }
 
     public void deleteAllContactsOnServer(CheckSyncClient client) throws Throwable {
 
         // Do this via SAPI to increase performance
         SapiHandler sapiHandler = getSapiHandler();
         sapiHandler.query("contacts","reset",null,null,null);
     }
 
     public void setContactAsVCard(String vCard) throws Throwable{
         String[] sep = new String[]{"\\r\\n"};
         String[] parts = StringUtil.split(vCard, sep);
 
         ByteArrayOutputStream ostream = new ByteArrayOutputStream();
         for (int i=0;i<parts.length;i++){
             ostream.write(parts[i].getBytes());
             ostream.write("\r\n".getBytes());
         }
         contactAsVcard = ostream.toString();
         ostream.close();
     }
 
     public void resetContacts(CheckSyncClient client) throws Throwable {
         CheckSyncSource source = client.getSyncSource(
                 CheckSyncClient.SOURCE_NAME_CONTACTS);
         // Remove locally
         deleteAllContacts();
         // Clean the check sync client and perform a refresh from client to
         // server
         client.clear(source);
     }
 
     public void checkItemsCountOnServer(int count) throws Throwable {
 
         SapiHandler sapiHandler = getSapiHandler();
         JSONObject resp = sapiHandler.query("contacts","count",null,null,null);
         int serverCount = resp.getInt("count");
         assertTrue(count, serverCount, "Server contacts count mismatch");
     }
     
     public void setContactFromServer(String vCard) throws Throwable {
 
         vCard = StringUtil.replaceAll(vCard, "\\r\\n", "\r\n");
 
         Enumeration sources = getAppSyncSourceManager().getWorkingSources();
         AppSyncSource appSource = null;
 
         while(sources.hasMoreElements()) {
             appSource = (AppSyncSource)sources.nextElement();
             if (appSource.getId() == AppSyncSourceManager.CONTACTS_ID) {
                 break;
             }
         }
 
         // We add an item via the SyncSource
         SyncSource source = appSource.getSyncSource();
         SyncItem item = new SyncItem("guid", "text/x-vcard", SyncItem.STATE_NEW, null);
         item.setContent(vCard.getBytes("UTF-8"));
 
         Vector items = new Vector();
         items.addElement(item);
         source.applyChanges(items);
     }
 
     /**
      * Order the vCard item fields alphabetically.
      * @param vcard
      * @return
      */
     protected String orderVCard(String vcard) {
         return orderVCard(vcard, null, null);
     }
 
     protected AppSyncSourceManager getAppSyncSourceManager() {
         return appSourceManager;
     }
 
     protected String orderVCard(String vcard, String supportedFields[], Hashtable supportedValues) {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Ordering vcard: " + vcard);
         }
         Vector fieldsAl = getFieldsVector(vcard);
 
         // order the fields array list
         String result = "";
         String[] fields = StringUtil.getStringArray(fieldsAl);
         for(int i=0; i<fields.length; i++) {
             for(int j=fields.length-1; j>i; j--) {
                 if(fields[j].compareTo(fields[j-1])<0) {
                     String temp = fields[j];
                     fields[j] = fields[j-1];
                     fields[j-1] = temp;
                 }
             }
 
             // Trim any leading/trailing white space
             fields[i] = fields[i].trim();
 
             // Exclude last occurrence of ";" from all fields
             while (fields[i].endsWith(";")) {
                 fields[i] = new String(fields[i].substring(0, fields[i].length()-1));
             }
             
             // Order ENCODING and CHARSET parameters
             int index = fields[i].indexOf("ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8");
             int length = "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8".length();
             if(index != -1) {
                 StringBuffer field = new StringBuffer();
                 field.append(fields[i].substring(0, index));
                 field.append("CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE");
                 field.append(fields[i].substring(index+length));
                 fields[i] = field.toString();
             }
             
             // Exclude empty fields and fields which are not supported by the
             // device
             if(!fields[i].endsWith(":")) {
                 if (supportedFields != null) {
                     int fieldNameIdx = fields[i].indexOf(":");
                     if (fieldNameIdx != -1) {
                         String fieldName = fields[i].substring(0, fieldNameIdx);
 
                         for(int j=0;j<supportedFields.length;++j) {
                         
                             String supportedFieldWithEncoding = supportedFields[j] + 
                                 ";CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE";
                             if (fieldName.equals(supportedFields[j]) || 
                                 fieldName.equals(supportedFieldWithEncoding)) {
 
                                 if (fieldNameIdx + 1 < fields[i].length()) {
                                     String value = fields[i].substring(fieldNameIdx + 1);
                                     value = cleanField(fieldName, value, supportedValues);
 
                                     // Exclude last occurrence of ";" from all fields
                                     while (value.endsWith(";")) {
                                         value = new String(value.substring(0, value.length()-1));
                                     }
 
                                     result += fieldName + ":" + value + "\r\n";
                                 } else {
                                     result += fields[i] + "\r\n";
                                 }
                                 break;
                             }
                         }
                     } else {
                         result += fields[i] + "\r\n";
                     }
                 } else {
                     result += fields[i] + "\r\n";
                 }
             }
         }
         
         // Replace all the encoded \r\n occurences with \n
         result = StringUtil.replaceAll(result, "=0D=0A", "=0A");
         
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Ordered vcard: " + result);
         }
         return result;
     }
 
     private String cleanField(String fieldName, String value, Hashtable supportedValues) {
         String filter = (String)supportedValues.get(fieldName); 
         if (filter != null) {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Found filter for field: " + fieldName + "," + filter);
             }
             String values[] = StringUtil.split(value, ";");
             String filters[] = StringUtil.split(filter, ";");
             String res = "";
 
             for(int i=0;i<values.length;++i) {
                 String v = values[i];
                 boolean include;
                 if (i<filters.length) {
                     String f = filters[i];
                     if (f.length() > 0) {
                         include = true;
                     } else {
                         include = false;
                     }
                 } else {
                     include = true;
                 }
 
                 if (include) {
                     res = res + v;
                 }
                 if (i != values.length - 1) {
                     res = res + ";";
                 }
             }
             return res;
 
         } else {
             return value;
         }
     }
 
     private Vector getFieldsVector(String vcard) {
 
         String sep[] = {"\r\n"};
         String lines[] = StringUtil.split(new String(vcard), sep);
 
         Vector fieldsAl = new Vector();
         String field = "";
         for(int i=0;i<lines.length;++i) {
             String line = lines[i];
             if(line.length() > 0 && line.charAt(0) == FOLDING_INDENT_CHAR) {
                 // this is a multi line field
                 field += line.substring(1); // cut the indent char
             } else {
                 if(!field.equals("")) {
                     fieldsAl.addElement(field);
                 }
                 field = line;
             }
         }
         // add the latest field
         fieldsAl.addElement(field);
 
         return fieldsAl;
     }
 
     protected void checkContactAsVCard(String vcard) throws Throwable {
         String currentVCard = getCurrentContactVCard();
         // The incoming vcard has \r\n as strings, we shall replace them
         vcard = StringUtil.replaceAll(vcard, "\\r\\n", "\r\n");
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "vcard.length=" + vcard.length());
         }
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "currentVCard.length=" + currentVCard.length());
         }
 
         assertTrue(currentVCard, vcard, "VCard mismatch");
     }
 
     protected abstract Configuration getConfiguration();
 
     public abstract void createEmptyContact() throws Throwable;
     public abstract void setContactField(String field, String value) throws Throwable;
     public abstract void saveContact() throws Throwable;
     public abstract void deleteContact(String firstname, String lastname) throws Throwable;
     public abstract void deleteAllContacts() throws Throwable;
     public abstract void loadContact(String firstName, String lastName) throws Throwable;
     protected abstract String getCurrentContactVCard() throws Throwable;
     public abstract void createEmptyRawContact() throws Throwable;
     public abstract void setRawContactData(String mimeType, Vector dataValues) throws Throwable;
     public abstract void saveRawContact() throws Throwable;
     public abstract void checkRawContactData(String mimeType, Vector dataValues) throws Throwable;
     public abstract void checkRawContactAsVCard(String vcal) throws Throwable;
 
 }
