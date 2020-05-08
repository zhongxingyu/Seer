 package com.jtbdevelopment.e_eye_o.jersey.rest;
 
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectEntitySettings;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import com.jtbdevelopment.e_eye_o.entities.security.AppUserUserDetails;
 import com.jtbdevelopment.e_eye_o.serialization.JSONIdObjectSerializer;
 import org.joda.time.DateTime;
 import org.springframework.security.access.annotation.Secured;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.util.Set;
 
 /**
  * Date: 2/10/13
  * Time: 12:33 PM
  */
 public class AppUserResource extends SecurityAwareResource {
     private final ReadWriteDAO readWriteDAO;
     private final JSONIdObjectSerializer jsonIdObjectSerializer;
     private final IdObjectReflectionHelper idObjectReflectionHelper;
     private final AppUser appUser;
     private Boolean archiveFlag;
     private Class<? extends AppUserOwnedObject> entityType;
    private final String roleUser = AppUserUserDetails.ROLE_USER;
 
     public AppUserResource(final ReadWriteDAO readWriteDAO,
                            final JSONIdObjectSerializer jsonIdObjectSerializer,
                            final IdObjectReflectionHelper idObjectReflectionHelper,
                            final String userId,
                            final Boolean archiveFlag,
                            final Class<? extends AppUserOwnedObject> entityType) {
         this.readWriteDAO = readWriteDAO;
         this.jsonIdObjectSerializer = jsonIdObjectSerializer;
         this.appUser = readWriteDAO.get(AppUser.class, userId);
         this.idObjectReflectionHelper = idObjectReflectionHelper;
         this.archiveFlag = archiveFlag;
         this.entityType = entityType == null ? AppUserOwnedObject.class : entityType;
     }
 
     private AppUserResource(final AppUserResource appUserResource) {
         this.readWriteDAO = appUserResource.readWriteDAO;
         this.jsonIdObjectSerializer = appUserResource.jsonIdObjectSerializer;
         this.idObjectReflectionHelper = appUserResource.idObjectReflectionHelper;
         this.appUser = appUserResource.appUser;
     }
 
     private AppUserResource(final AppUserResource appUserResource,
                             final Boolean archiveFlag) {
         this(appUserResource);
         this.archiveFlag = archiveFlag;
         this.entityType = appUserResource.entityType;
     }
 
     private AppUserResource(final AppUserResource appUserResource,
                             final Class<? extends AppUserOwnedObject> entityType) {
         this(appUserResource);
         this.archiveFlag = appUserResource.archiveFlag;
         this.entityType = entityType;
     }
 
     //  TODO - paging
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response getEntitiesForUser() {
         Set<? extends AppUserOwnedObject> set;
         if (archiveFlag == null)
             set = readWriteDAO.getEntitiesForUser(entityType, appUser);
         else {
             set = archiveFlag ? readWriteDAO.getArchivedEntitiesForUser(entityType, appUser) :
                     readWriteDAO.getActiveEntitiesForUser(entityType, appUser);
         }
         return Response.ok(jsonIdObjectSerializer.write(set)).build();
     }
 
     @GET
     @Path("ModifiedSince/{modifiedSince}")
     @Produces(MediaType.APPLICATION_JSON)
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response getModifiedSince(@PathParam("modifiedSince") final String dateTimeString) {
         DateTime dateTime = DateTime.parse(dateTimeString);
         return Response.ok(jsonIdObjectSerializer.write(readWriteDAO.getEntitiesModifiedSince(AppUserOwnedObject.class, appUser, dateTime))).build();
     }
 
     @Path("archived")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getArchived() {
         if (archiveFlag == null)
             return new AppUserResource(this, Boolean.TRUE);
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 
     @Path("active")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getActive() {
         if (archiveFlag == null)
             return new AppUserResource(this, Boolean.FALSE);
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 
     @Path("photos")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getPhotos() {
         return getEntityRefinedResource(Photo.class);
     }
 
     @Path("students")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getStudents() {
         return getEntityRefinedResource(Student.class);
     }
 
     @Path("classes")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getClassLists() {
         return getEntityRefinedResource(ClassList.class);
     }
 
     @Path("observations")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getObservations() {
         return getEntityRefinedResource(Observation.class);
     }
 
     @Path("categories")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getObservationCategories() {
         return getEntityRefinedResource(ObservationCategory.class);
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Response createEntity(@FormParam("appUserOwnedObject") final String appUserOwnedObjectString) {
         AppUser sessionAppUser = getSessionAppUser();
         AppUserOwnedObject newObject = jsonIdObjectSerializer.read(appUserOwnedObjectString);
         if (!sessionAppUser.isAdmin()) {
             if (!idObjectReflectionHelper.getIdObjectInterfaceForClass(newObject.getClass()).getAnnotation(IdObjectEntitySettings.class).editable()) {
                 return Response.status(Response.Status.FORBIDDEN).build();
             }
             newObject.setAppUser(sessionAppUser);
         }
         final AppUserOwnedObject entity = readWriteDAO.create(newObject);
 
         return Response.created(URI.create(entity.getId() + "/")).build();
     }
 
     @Path("{entityId}")
     @Secured({AppUserUserDetails.ROLE_USER, AppUserUserDetails.ROLE_ADMIN})
     public Object getAppUserEntityResource(@PathParam("entityId") final String entityId) {
         return new AppUserEntityResource(readWriteDAO, jsonIdObjectSerializer, idObjectReflectionHelper, entityId);
     }
 
     private Object getEntityRefinedResource(final Class<? extends AppUserOwnedObject> entityType) {
         if (this.entityType.equals(AppUserOwnedObject.class))
             return new AppUserResource(this, entityType);
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 }
