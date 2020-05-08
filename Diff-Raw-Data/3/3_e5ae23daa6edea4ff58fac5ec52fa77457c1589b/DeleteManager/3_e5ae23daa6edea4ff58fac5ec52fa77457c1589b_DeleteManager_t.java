 package org.jboss.pressgang.ccms.restserver.webdav.managers;
 
 import org.jboss.pressgang.ccms.restserver.webdav.constants.WebDavConstants;
 
 import javax.enterprise.context.ApplicationScoped;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Database fields can not be deleted, but when they are exposed as files, some applications expect to be able to delete
  * them (e.g. Kate will delete the file it is editing, and then check to make sure it is deleted before saving any changes).
  * This manager simply keeps a track of delete requests, and shows a file as being deleted for a short period of time,
  * or until it is "created" again.
  */
 @ApplicationScoped
 public class DeleteManager {
 
     /**
      * TODO: make an object to represent a delete request
      * A resource type mapped to a remote address mapped to an id mapped to a deletion time.
      */
     final Map<ResourceTypes, HashMap<String, HashMap<Integer, Calendar>>> deletedResources = new HashMap<ResourceTypes, HashMap<String, HashMap<Integer, Calendar>>>();
 
     synchronized public boolean isDeleted(final ResourceTypes resourceType, final String remoteAddress, final Integer id) {
         if (deletedResources.containsKey(resourceType)) {
             final HashMap<String, HashMap<Integer, Calendar>> specificDeletedResources = deletedResources.get(resourceType);
 
             if (specificDeletedResources.containsKey(remoteAddress)) {
                 if (specificDeletedResources.get(remoteAddress).containsKey(id)) {
                     final Calendar deletionDate = specificDeletedResources.get(remoteAddress).get(id);
                     final Calendar window = Calendar.getInstance();
                     window.add(Calendar.SECOND, -WebDavConstants.DELETE_WINDOW);
 
                     if (deletionDate.before(window)) {
                         specificDeletedResources.remove(id);
                         return false;
                     }
 
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     synchronized public void delete(final ResourceTypes resourceType, final String remoteAddress, final Integer id) {
         if (!deletedResources.containsKey(resourceType)) {
             deletedResources.put(resourceType, new HashMap<String, HashMap<Integer, Calendar>>());
         }
 
         if (!deletedResources.get(resourceType).containsKey(remoteAddress))  {
             deletedResources.get(resourceType).put(remoteAddress, new HashMap<Integer, Calendar>());
         }
 
         deletedResources.get(resourceType).get(remoteAddress).put(id, Calendar.getInstance());
     }
 
     synchronized public void create(final ResourceTypes resourceType, final String remoteAddress, final Integer id) {
         if (deletedResources.containsKey(resourceType) &&
             deletedResources.get(resourceType).containsKey(remoteAddress) &&
             deletedResources.get(resourceType).get(remoteAddress).containsKey(id)) {
                 deletedResources.get(resourceType).get(remoteAddress).remove(id);
         }
     }

 }
