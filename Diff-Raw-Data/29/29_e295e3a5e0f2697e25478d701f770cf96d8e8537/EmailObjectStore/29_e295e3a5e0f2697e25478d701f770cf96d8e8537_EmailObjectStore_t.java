 /*
  * CitadelSourceAdmin.java
  *
  * Created on May 9, 2008, 10:45 PM
  * Funambol Citadel Connector
  * (C) 2007-2008 Mathew McBride
  * http://bionicmessage.net
  * 
  * Portions of code may come from: 
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2003 - 2007 Funambol, Inc.
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
 package net.bionicmessage.funambol.citadel.store;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import org.citadel.lite.CitadelException;
 import org.citadel.lite.CitadelToolkit;
 import org.citadel.lite.CtdlMessage;
 
 /**
  * EmailObjectStore manages a local database of emails and provides
  * functionality for syncing the local database with the server
  * @author matt
  */
 public class EmailObjectStore {
 
     protected Properties storeProperties = null;
     protected CitadelToolkit server = null;
     protected db4oObjectStore store = null;
     protected List<CitadelMailObject> addedOnServer = null;
     protected List<CitadelMailObject> deletedOnServer = null;
     protected String srvHost = null;
     protected int srvPort = 0;
     protected String userName = null;
     protected String password = null;
 
     public EmailObjectStore(Properties props) {
         storeProperties = props;
         server = new CitadelToolkit();
         srvHost = storeProperties.getProperty(CtdlFnblConstants.SERVER_HOST);
         Integer portInt = Integer.parseInt(
                 storeProperties.getProperty(
                 CtdlFnblConstants.SERVER_PORT));
         srvPort = portInt.intValue();
         String storeLoc = storeProperties.getProperty(CtdlFnblConstants.STORE_LOC) +
                 File.separator + "emailobjects.db";
         if (storeProperties.getProperty(CtdlFnblConstants.PURGE_DB_OPTION) != null) {
             File storeDb = new File(storeLoc);
             storeDb.delete();
         }
         store = new db4oObjectStore(storeLoc);
         addedOnServer = new ArrayList();
         deletedOnServer = new ArrayList();
         if (storeProperties.getProperty(CtdlFnblConstants.USER_NAME) != null) {
             userName = storeProperties.getProperty(CtdlFnblConstants.USER_NAME);
             password = storeProperties.getProperty(CtdlFnblConstants.USER_PASS);
         }
     }
 
     public void setCredentials(String user, String pass) {
         userName = user;
         password = pass;
     }
 
     public void startSync() throws Exception {
         store.init();
         server.open(srvHost, srvPort);
         server.login(userName, password);
        server.setPreferredType("text/plain"); //change when HTML is implemented later.
         ArrayList<String> listOfRooms = new ArrayList();
         // Get a list of rooms
         //Iterator propList = storeProperties.keySet().iterator();
         // Use Enumeration instead so we get default keys.
         for (Enumeration pList = storeProperties.propertyNames(); pList.hasMoreElements();) {
             String key = (String) pList.nextElement();
             if (key.contains(CtdlFnblConstants.ROOM_BASE)) {
                 String name = key.replace(CtdlFnblConstants.ROOM_BASE, "");
                 String value = storeProperties.getProperty(key);
                 listOfRooms.add(value);
             }
         }
         Iterator<String> rooms = listOfRooms.iterator();
         while (rooms.hasNext()) {
             String room = rooms.next();
             server.gotoRoom(room);
             List msgsInRoom = server.getMessagesInRoom();
             Iterator<String> msgs = msgsInRoom.iterator();
             Hashtable mapWeHave = getMapForRoom(room);
             while (msgs.hasNext()) {
                 String msgId = msgs.next();
                 if (mapWeHave.get(msgId) == null) {
                    try {
                        CtdlMessage hint = server.getMessageHeaders(msgId);
                        CitadelMailObject cmo = new CitadelMailObject(hint);
                        Long msgPointer = Long.parseLong(msgId);
                        cmo.setCtdlMessagePointer(msgPointer);
                        cmo.setCtdlMessageRoom(room);
                        store.storeObject(cmo);
                        addedOnServer.add(cmo);
                        System.out.format("Stored %d in %s\n", msgPointer.intValue(), room).flush();
                    } catch (Exception e) {
                        System.err.println("Error in storing message: " + msgId);
                        e.printStackTrace();
                    }
                 } else {
                     mapWeHave.remove(msgId);
                 }
             }
             Set<String> keySet = mapWeHave.keySet();
             String[] keys = keySet.toArray(new String[keySet.size()]);
             for (int i = 0; i < keys.length; i++) {
                 CitadelMailObject obj = store.getMailByMessagePointer(keys[i]);
                 store.deleteObject(obj);
                 deletedOnServer.add(obj);
             }
         }
 
     }
 
     public List<String> listMessagesInRoom(String room) {
         return store.getMessagePointersInRoom(room);
     }
 
     public List<String> listAllStoredMessages() {
         return store.getAllMessagePointers();
     }
 
     public Hashtable getMapForRoom(
             String room) {
         List<String> keyList = store.getMessagePointersInRoom(room);
         Iterator<String> it = keyList.iterator();
         Hashtable map = new Hashtable(keyList.size());
         while (it.hasNext()) {
             map.put(it.next(), "");
         }
 
         return map;
     }
 
     /** Obtain a message object by its Citadel message pointer. Objects
      * returned by this message may be 'unfilled'
      * @param messagePointer Message pointer, in String form 
      * @return CitadelMailObject - most likely unfilled
      */
     public CitadelMailObject getMessageByPointer(String messagePointer) {
         //Long msgLong = Long.decode(messagePointer);
         return store.getMailByMessagePointer(messagePointer);
     }
 
     /**
      * Obtain a message object by its Citadel message pointer. Objects returned
      * by this message will be 'filled'
      * @param messagePointer Citadel message pointer, in String form
      * @return CitadelMailobject - filled
      * @throws java.lang.Exception - due to server failure etc.
      */
     public CitadelMailObject getFilledMessageByPointer(String messagePointer) throws Exception {
         CitadelMailObject obj = getMessageByPointer(messagePointer);
         if (obj != null && !obj.hasData()) {
             System.err.println("Fetching data for " + messagePointer);
             CtdlMessage filled = server.getMessage4(messagePointer);
             obj.setData(filled.getContent());
             //Also regenerate part data
             obj.generateParts(filled.getPartList());
         }
         return obj;
     }
 
     public void fillPartsForMessage(CitadelMailObject cmo) throws Exception {
         List<CitadelPart> partList = cmo.getAttachedParts();
         Iterator<CitadelPart> partIterator = partList.iterator();
         while (partIterator.hasNext()) {
             CitadelPart partToFill = partIterator.next();
             byte[] data = server.downloadPart("" + cmo.getCtdlMessagePointer(),
                     partToFill.getPartNum(),
                     partToFill.getPartSize());
             partToFill.setPartData(data);
         }
     }
 
     /**
      * Get objects that were added onto the server
      * @return A collection of objects added to the server
      */
     public List<CitadelMailObject> getAddedOnServerObjects() {
         return addedOnServer;
     }
 
     public List<CitadelMailObject> getDeletedOnServerObjects() {
         return deletedOnServer;
     }
 
     public void moveToTrash(String msgId) throws IOException, CitadelException {
         CitadelMailObject toDelete = getMessageByPointer(msgId);
         server.moveToTrash(toDelete.getCtdlMessageRoom(), msgId);
         store.deleteObject(toDelete);
     }
 
     public CitadelToolkit getToolkit() {
         return server;
     }
 
     public void printDebugReport() {
     }
 
     /** Commit all changes and close the database */
     public void close() throws Exception {
         store.close();
     }
 }
