 package eu.ydp.ldapgroups.resources;
 
 import com.sun.jersey.api.NotFoundException;
 import eu.ydp.ldapgroups.entity.Group;
 import eu.ydp.ldapgroups.dao.GroupDao;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.List;
 import java.util.Set;
 
 @Named
 @Path("/groups")
 @Produces(MediaType.APPLICATION_JSON)
 public class GroupResource {
     GroupDao groupDao;
 
     @Inject
     public GroupResource(GroupDao groupDao) {
         this.groupDao = groupDao;
     }
 
     @GET
     public List<Group> listGroups() {
         return groupDao.findAll();
     }
 
     @POST
     public Group createGroup(Group group) {
         group = new Group.Builder(group).dateModified().build();
         return groupDao.create(group);
     }
 
     @GET
     @Path("/{groupName}")
     public Group getGroup(@PathParam("groupName") String groupName) {
         return getByNameOrNotFound(groupName);
     }
 
     @PUT
     @Path("/{groupName}")
     public Group setGroup(@PathParam("groupName") String groupName, Group group) {
         Group up2dateGroup = groupDao.getByName(groupName);
         if (up2dateGroup==null) {
             return createGroup(group);
         } else {
             Group mergeGroup = new Group.Builder(up2dateGroup).members(group.getMembers()).dateModified().build();
             return groupDao.update(mergeGroup);
         }
     }
 
     @DELETE
     @Path("/{groupName}")
     public void deleteGroup(@PathParam("groupName") String groupName) {
        if (!groupDao.delete(groupName)) {
            throwNotFound(groupName);
        }
     }
 
     protected Group getByNameOrNotFound(String groupName) {
         Group group = groupDao.getByName(groupName);
         if (group==null)
            throwNotFound(groupName);
         return group;
     }
 
    protected void throwNotFound(String groupName) throws NotFoundException {
        throw new NotFoundException("Group '"+ groupName +"' not found");
    }
 }
