 package com.jtbdevelopment.e_eye_o.jersey.rest;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUser;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.entities.IdObject;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectEntitySettings;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import com.jtbdevelopment.e_eye_o.entities.security.AppUserUserDetails;
 import com.jtbdevelopment.e_eye_o.serialization.JSONIdObjectSerializer;
 import org.springframework.security.access.annotation.Secured;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 /**
  * Date: 2/10/13
  * Time: 7:07 PM
  */
 public class AppUserEntityResource extends SecurityAwareResource {
     private final ReadWriteDAO readWriteDAO;
     private final JSONIdObjectSerializer jsonIdObjectSerializer;
     private final IdObjectReflectionHelper idObjectReflectionHelper;
     private final String entityId;
 
     public AppUserEntityResource(final ReadWriteDAO readWriteDAO, final JSONIdObjectSerializer jsonIdObjectSerializer, final IdObjectReflectionHelper idObjectReflectionHelper, final String entityId) {
         this.readWriteDAO = readWriteDAO;
         this.jsonIdObjectSerializer = jsonIdObjectSerializer;
         this.entityId = entityId;
         this.idObjectReflectionHelper = idObjectReflectionHelper;
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response getEntity() {
         AppUserOwnedObject entity = readWriteDAO.get(AppUserOwnedObject.class, entityId);
         if (entity == null) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
         return Response.ok(jsonIdObjectSerializer.write(entity)).build();
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Produces(MediaType.APPLICATION_JSON)
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response updateEntity(@FormParam("appUserOwnedObject") final String appUserOwnedObjectString) {
         AppUser sessionAppUser = getSessionAppUser();
 
         AppUserOwnedObject updateObject = jsonIdObjectSerializer.read(appUserOwnedObjectString);
         Class<? extends IdObject> idObjectInterface = idObjectReflectionHelper.getIdObjectInterfaceForClass(updateObject.getClass());
         if (!idObjectInterface.getAnnotation(IdObjectEntitySettings.class).editable()) {
             return Response.status(Response.Status.FORBIDDEN).build();
         }
 
         AppUserOwnedObject dbEntity = readWriteDAO.get(updateObject.getClass(), entityId);
         if (dbEntity == null) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
         if (!dbEntity.equals(updateObject) || !dbEntity.getId().equals(entityId)) {
             return Response.status(Response.Status.FORBIDDEN).build();
         }
 
         boolean archiveRequested = updateObject.isArchived();
         if (sessionAppUser.isAdmin() || dbEntity.getAppUser().equals(sessionAppUser)) {
             updateObject = readWriteDAO.update(sessionAppUser, updateObject);
             if (updateObject.isArchived() != archiveRequested) {
                 readWriteDAO.changeArchiveStatus(updateObject);
                 updateObject = readWriteDAO.get(updateObject.getClass(), updateObject.getId());
             }
         } else {
             if (!dbEntity.getAppUser().equals(sessionAppUser)) {
                 return Response.status(Response.Status.FORBIDDEN).build();
             }
         }
 
         //  Ignoring any sort of chained updated from archive status change
         return Response.ok(jsonIdObjectSerializer.write(updateObject)).build();
     }
 
     @DELETE
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response deleteEntity() {
         AppUser sessionAppUser = getSessionAppUser();
 
         AppUserOwnedObject dbObject = readWriteDAO.get(AppUserOwnedObject.class, entityId);
         if (dbObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
         }
 
         Class<? extends IdObject> idObjectInterface = idObjectReflectionHelper.getIdObjectInterfaceForClass(dbObject.getClass());
         if (sessionAppUser.isAdmin() || dbObject.getAppUser().equals(sessionAppUser)) {
             if (!idObjectInterface.getAnnotation(IdObjectEntitySettings.class).editable()) {
                 return Response.status(Response.Status.FORBIDDEN).build();
             }
             readWriteDAO.delete(dbObject);
             return Response.ok().build();
         }
         return Response.status(Response.Status.FORBIDDEN).build();
     }
 }
