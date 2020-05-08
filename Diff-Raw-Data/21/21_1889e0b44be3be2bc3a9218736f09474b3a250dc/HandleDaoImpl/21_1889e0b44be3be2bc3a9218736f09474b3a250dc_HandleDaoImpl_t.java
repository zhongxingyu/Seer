 /*
  * The PID webservice offers SOAP methods to manage the Handle System(r) resolution technology.
  *
  * Copyright (C) 2010-2011, International Institute of Social History
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.socialhistoryservices.pid.database.dao;
 
 import com.mongodb.*;
 import net.handle.hdllib.HandleException;
 import net.handle.hdllib.HandleValue;
 import net.handle.hdllib.Util;
 import net.handle.server.MongoDBHandleStorage;
 import org.socialhistoryservices.pid.database.domain.Handle;
 import org.socialhistoryservices.pid.schema.LocAttType;
 import org.socialhistoryservices.pid.schema.LocationType;
 import org.socialhistoryservices.pid.schema.PidType;
 import org.socialhistoryservices.pid.util.PidGenerator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.oxm.jaxb.Jaxb2Marshaller;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 import javax.xml.transform.stream.StreamResult;
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Implemenation of the Dao, but with a dependency on the
  *
  * @author Lucien van Wouw <lwo@iisg.nl>
  */
 
 public class HandleDaoImpl implements HandleDao {
 
     @Value("#{pidProperties['handle.baseUrl']}")
     private String handleBaseUrl;
 
     private MongoDBHandleStorage handleStorage;
 
     @Autowired
     Jaxb2Marshaller marshaller;
 
     final public static QName qname = new QName("http://pid.socialhistoryservices.org/", "locations");
     final public static String LID = "LID";
     final public static String URL = "URL";
     final public static String LOC = "10320/loc";
     final private static String HS_ADMIN = "HS_ADMIN";
 
     @Override
     public List<Handle> upsertHandle(String na, PidType pidType) throws HandleException {
         return upsertHandle(na, pidType, 0);
     }
 
     @Override
     public List<Handle> createNewHandle(String na, PidType pidType) throws HandleException {
 
         return upsertHandle(na, pidType, 1);
     }
 
     @Override
     public List<Handle> updateHandle(String na, PidType pidType) throws HandleException {
 
         // get Pid
         final String pid = pidType.getPid();
         PidGenerator.validatePid(na, pid);
         return upsertHandle(na, pidType, 2);
     }
 
     @Override
     public List<Handle> fetchHandleByPID(String na, String pid) throws HandleException {
 
         final List<HandleValue> handleValues = handleStorage.getHandleValues(pid);
         final Iterator<HandleValue> iterator = handleValues.iterator();
         final List<Handle> handles = new ArrayList<Handle>(handleValues.size());
         while (iterator.hasNext()) {
             HandleValue value = iterator.next();
             Handle handle = Handle.cast(pid, value);
             handles.add(handle);
         }
         return handles;
     }
 
     @Override
     public List<Handle> fetchHandleByAttribute(String na, String attribute, String type) {
 
         final BasicDBObject query = new BasicDBObject();
         query.put("_lookup", attribute);
         if (type != null) {
             query.put("handles.type", type);
         }
         final DBCollection collection = handleStorage.getCollection(Util.encodeString(na + "/bla"));
         final DBCursor dbCursor = collection.find(query);
         final List<Handle> list = new ArrayList<Handle>();
         for (DBObject aDbCursor : dbCursor) {
             final BasicDBObject handle = (BasicDBObject) aDbCursor;
             final List<Handle> handles = getHandleValueByData(handle);
             list.addAll(handles);
         }
         return list;
     }
 
     /**
      * Retrieves a list of HandleValues from the resultset and casts each to the domain model Handle.
      *
      * @param handle The pid
      * @return The handle documents
      */
     private List<Handle> getHandleValueByData(BasicDBObject handle) {
 
         final BasicDBList results = (BasicDBList) handle.get("handles");
         final Iterator<Object> iterator = results.iterator();
         final List<Handle> handles = new ArrayList<Handle>();
         while (iterator.hasNext()) {
             HandleValue value = handleStorage.getHandleValue((BasicDBObject) iterator.next());
             handles.add(Handle.cast((String) handle.get("handle"), value));
         }
         return handles;
     }
 
     @Override
     public boolean deletePid(String pid) {
 
         boolean ok = false;
         try{
             ok = handleStorage.deleteHandle(Util.encodeString(pid));
         } catch (HandleException e){
         }
         return ok;
     }
 
     @Override
     public long deletePids(String na) {
 
         return handleStorage.deleteAllRecords(na);
     }
 
     /**
      * Adds the resolve url or location attributes to the handle and persists.
      * Should both resolveUrl and location attributes be declared,
      * then the resolveUrl is moved to the location attributes.
      *
      * @param na      Naming authority
      * @param pidType The pid and it's bound attributes
      * @param action  0=upsert, 1=create, 2=update
      * @return The handle document that has been created or updated
      * @throws net.handle.hdllib.HandleException
      *          A Handle System error description
      */
     private List<Handle> upsertHandle(String na, PidType pidType, int action) throws HandleException {
 
         final String pid = pidType.getPid();
         final String lid = pidType.getLocalIdentifier();
         final String resolveUrl = pidType.getResolveUrl();
         final LocAttType locationAttributes = pidType.getLocAtt();
 
         List<Handle> handles = new ArrayList<Handle>();
         if (locationAttributes == null) {
             Handle handle = new Handle(pid);
             handle.setIndex(1);
             handle.setType(URL);
             handle.setData(resolveUrl);
             handle.setLocation(resolveUrl);
             handles.add(handle);
         } else {
             // Set the resolveUrl as location attribute.
             if (resolveUrl != null && !resolveUrl.isEmpty()) {
                 LocationType l = new LocationType();
                 l.setHref(resolveUrl);
                 l.setWeight("1"); // This setting wil make more probable the URL is choosen when ambiqious
                 // resolve parameters are presented to the resolver.
                 locationAttributes.getLocation().add(l);
             }
 
             Handle handle = new Handle(pid);
             handle.setIndex(1000);
             handle.setType(LOC);
             setLocations(handle, locationAttributes);
             for (LocationType location : locationAttributes.getLocation()) {
                 handle.setLocation(location.getHref());
             }
             handles.add(handle);
         }
 
         if (lid != null) {
             Handle handle = new Handle(pid);
             handle.setIndex(2000);
             handle.setType(LID);
             handle.setData(lid);
             handle.setLocation(lid);
             handles.add(handle);
         }
 
         upsertHandle(na, pid, handles, action);
         return handles;
     }
 
     private void upsertHandle(String na, String pid, List<Handle> handles, int action) throws HandleException {
 
         // if the handle already exists, throw an exception
         final BasicDBObject query = new BasicDBObject("handle", pid);
         final List<HandleValue> currentValues = handleStorage.getHandleValues(pid);
         if (action == 2 && currentValues.size() == 0) {
             throw new HandleException(HandleException.HANDLE_DOES_NOT_EXIST, "The pid " + pid + " does not exist. Use the create method.");
         } else if (action == 1 && currentValues.size() != 0) {
             throw new HandleException(HandleException.HANDLE_ALREADY_EXISTS, "The pid " + pid + " already exists. Use the update method.");
         }
 
         // add non-PID webservice handles ( other than those managed here like URL, LID and the 10320/loc )
         preserveHandles(na, pid, currentValues, handles);
        final int timestamp = (int)(System.currentTimeMillis()/1000);
         final BasicDBList _lookup = new BasicDBList();
         final BasicDBList list = new BasicDBList();
         for (Handle handle : handles) {
            handle.setTimestamp(timestamp);
            handle.setTTLType(HandleValue.TTL_TYPE_RELATIVE);     
            handle.setTTL(86400);     
             final BasicDBObject hv = handleStorage.setHandleValue(handle);
             list.add(hv);
             _lookup.addAll(handle.getLocations());
         }
 
         final BasicDBObject handle = new BasicDBObject("handle", pid);
         handle.put("handles", list);
         handle.put("_lookup", _lookup);
         final DBCollection collection = handleStorage.getCollection(Util.encodeString(pid));
         final WriteResult result = collection.update(query, handle, true, false); // Upsert
         if (result.getN() == 0)
             throw new HandleException(900, "Cannot create or update the pid " + pid);
     }
 
     /**
      * Keeps alternative Handles of types like HS_ADMIN, etc, in the document.
      * Ensures a HS_ADMIN records is always available no matter what.
      *
      * @param na            Naming authority
      * @param pid           Persistant identifier
      * @param currentValues The handles that were in the handle document before the update takes place
      * @param newValues     the handles that are NOT in the handle document before the update takes place
      * @throws net.handle.hdllib.HandleException
      *          A Handle System error description
      */
     private void preserveHandles(String na, String pid, List<HandleValue> currentValues, List<Handle> newValues) throws HandleException {
 
         boolean hs_admin = false;
         for (HandleValue handleValue : currentValues) {
             final String type = Util.decodeString(handleValue.getType());
             if (
                     type.equals(URL) || type.equals(LID) || type.equals(LOC)) {
             } else {
                 if (type.equals(HS_ADMIN))
                     hs_admin = true;
                 Handle handle = Handle.cast(pid, handleValue);
                 newValues.add(handle);
             }
         }
         if (!hs_admin) {
             final HandleValue adminValue = handleStorage.createAdminValue("0.NA/" + na, 200, 100);
             Handle handle = Handle.cast(pid, adminValue);
             newValues.add(handle);
         }
     }
 
     /**
      * add locations without namespace as Handle System describes it.
      * The namespace will have to be removed to make sure future implementations of the Handle System
      * process the XML correctly:
      * <locations><location></location></locations>
      *
      * @param handle    The persistent identifier
      * @param locations The 10320/loc value
      */
     private void setLocations(Handle handle, LocAttType locations) {
 
         final JAXBElement element = new JAXBElement(qname, PidType.class, LocAttType.class, locations);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         StreamResult result = new StreamResult(baos);
         marshaller.marshal(element, result);
         final String s = Util.decodeString(baos.toByteArray());
         final int i = s.indexOf("<location ");
         if (i == -1)
             return;
         handle.setData("<locations>" + s.substring(i));
     }
 
     public void setHandleStorage(MongoDBHandleStorage handleStorage) {
         this.handleStorage = handleStorage;
     }
 }
