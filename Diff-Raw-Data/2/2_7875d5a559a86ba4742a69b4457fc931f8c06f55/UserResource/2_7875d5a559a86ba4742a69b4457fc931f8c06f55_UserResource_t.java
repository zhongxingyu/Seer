 package pl.psnc.dl.wf4ever.oauth;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.naming.NamingException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.db.UserProfile;
 import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
 import pl.psnc.dl.wf4ever.dl.ConflictException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 
 import com.sun.jersey.api.NotFoundException;
 
 /**
  * OAuth user REST API resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path(("users/{U_ID}"))
 public class UserResource {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(UserResource.class);
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
 
 
     /**
      * Get a user described with RDF/XML format.
      * 
      * @param urlSafeUserId
      *            user id URL-safe base 64 encoded
      * @return 200 OK with user metadata serialized in RDF
      * @throws DigitalLibraryException
      *             dLibra error
      */
     @GET
     public Response getUserRdfXml(@PathParam("U_ID") String urlSafeUserId)
             throws DigitalLibraryException {
         return getUser(urlSafeUserId, RDFFormat.RDFXML);
     }
 
 
     /**
      * Get a user described with Turtle format.
      * 
      * @param urlSafeUserId
      *            user id URL-safe base 64 encoded
      * @return 200 OK with user metadata serialized in RDF
      * @throws DigitalLibraryException
      *             dLibra error
      */
     @GET
     @Produces({ "application/x-turtle", "text/turtle" })
     public Response getUserTurtle(@PathParam("U_ID") String urlSafeUserId)
             throws DigitalLibraryException {
         return getUser(urlSafeUserId, RDFFormat.TURTLE);
     }
 
 
     /**
      * Get a user described with N3 format.
      * 
      * @param urlSafeUserId
      *            user id URL-safe base 64 encoded
      * @return 200 OK with user metadata serialized in RDF
      * @throws DigitalLibraryException
      *             dLibra error
      */
     @GET
     @Produces("text/rdf+n3")
     public Response getUserN3(@PathParam("U_ID") String urlSafeUserId)
             throws DigitalLibraryException {
         return getUser(urlSafeUserId, RDFFormat.N3);
     }
 
 
     /**
      * Get a user REST API resource.
      * 
      * @param urlSafeUserId
      *            user id URL-safe base 64 encoded
      * @param rdfFormat
      *            RDF format for the output
      * @return 200 OK with user metadata serialized in RDF
      * @throws DigitalLibraryException
      *             dLibra error
      */
     private Response getUser(@PathParam("U_ID") String urlSafeUserId, RDFFormat rdfFormat)
             throws DigitalLibraryException {
         String userId = new String(Base64.decodeBase64(urlSafeUserId));
         UserProfileDAO userProfileDAO = new UserProfileDAO();
         UserProfile user = userProfileDAO.findByLogin(userId);
         if (user == null) {
             throw new NotFoundException("User not found");
         }
         InputStream userDesc = user.getAsInputStream(rdfFormat);
         if (DigitalLibraryFactory.getDigitalLibrary().userExists(userId)) {
             return Response.ok(userDesc).type(rdfFormat.getDefaultMIMEType()).build();
         } else {
             return Response.status(Status.NOT_FOUND).type("text/plain").entity("User " + userId + " does not exist")
                     .build();
         }
     }
 
 
     /**
      * Creates new user with given USER_ID. input: USER_ID (the password is generated internally).
      * 
      * @param urlSafeUserId
      *            id, base64 url-safe encoded
      * @param username
      *            human friendly username
      * @return 201 (Created) when the user was successfully created, 200 OK if it was updated, 400 (Bad Request) if the
      *         content is malformed 409 (Conflict) if the USER_ID is already used
      * @throws DigitalLibraryException
      *             error storing the user in dLibra
      * @throws ConflictException
      *             error storing user profile in SMS
      * @throws SQLException
      *             error storing user profile in SMS
      * @throws NamingException
      *             error storing user profile in SMS
      * @throws IOException
      *             error storing user profile in SMS
      * @throws ClassNotFoundException
      *             error storing user profile in SMS
      * @throws pl.psnc.dl.wf4ever.dl.NotFoundException
      *             not found
      */
     @PUT
     @Consumes("text/plain")
     public Response createOrUpdateUser(@PathParam("U_ID") String urlSafeUserId, String username)
             throws DigitalLibraryException, ConflictException, ClassNotFoundException, IOException, NamingException,
             SQLException, pl.psnc.dl.wf4ever.dl.NotFoundException {
         String userId = new String(Base64.decodeBase64(urlSafeUserId));
         boolean updated = false;
         UserProfileDAO userProfileDAO = new UserProfileDAO();
         try {
             new URI(userId);
 
             UserProfile user = userProfileDAO.findByLogin(userId);
             if (user != null && username != null && !(user.getName().equals(username))) {
                 updated = true;
             }
         } catch (URISyntaxException e) {
             LOGGER.warn("URI " + userId + " is not valid", e);
         }
 
         String password = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
         boolean created = DigitalLibraryFactory.getDigitalLibrary().createOrUpdateUser(userId, password,
             username != null && !username.isEmpty() ? username : userId);
         if (created) {
             return Response.created(uriInfo.getAbsolutePath()).build();
         } else {
             if (updated) {
                 UserProfile user = userProfileDAO.findByLogin(userId);
                 UpdateUserIndexThread updateThread = new UpdateUserIndexThread(ResearchObject.getAll(builder, user));
                updateThread.start();
             }
             return Response.ok().build();
         }
     }
 
 
     /**
      * Deletes the workspace.
      * 
      * @param urlSafeUserId
      *            id, base64 url-safe encoded
      * @throws DigitalLibraryException
      *             error deleting the user from dLibra
      * @throws NotFoundException
      *             error deleting the user profile from SMS
      * @throws SQLException
      *             error deleting the user profile from SMS
      * @throws NamingException
      *             error deleting the user profile from SMS
      * @throws IOException
      *             error deleting the user profile from SMS
      * @throws ClassNotFoundException
      *             error deleting the user profile from SMS
      * @throws pl.psnc.dl.wf4ever.dl.NotFoundException
      *             error deleting the user profile from DL
      */
     @DELETE
     public void deleteUser(@PathParam("U_ID") String urlSafeUserId)
             throws DigitalLibraryException, NotFoundException, ClassNotFoundException, IOException, NamingException,
             SQLException, pl.psnc.dl.wf4ever.dl.NotFoundException {
         String userId = new String(Base64.decodeBase64(urlSafeUserId));
         UserProfileDAO userProfileDAO = new UserProfileDAO();
         UserProfile user = userProfileDAO.findByLogin(userId);
         if (user == null) {
             throw new NotFoundException("User not found");
         }
         Set<ResearchObject> ros = ResearchObject.getAll(builder, user);
         for (ResearchObject ro : ros) {
             ro.delete();
         }
         DigitalLibraryFactory.getDigitalLibrary().deleteUser(userId);
         userProfileDAO.delete(user);
     }
 
 
     /**
      * Simple Thread to update users information (update names in index) in the background when they are changed.
      * 
      * @author pejot
      * 
      */
     class UpdateUserIndexThread extends Thread {
 
         /** Set of ros to update. */
         Set<ResearchObject> roSet;
 
 
         /**
          * Constructor.
          * 
          * @param set
          *            set of ros
          */
         public UpdateUserIndexThread(Set<ResearchObject> set) {
             this.roSet = set;
         }
 
 
         @Override
         public void run() {
             super.run();
             for (ResearchObject ro : roSet) {
                 ro.updateIndexAttributes();
             }
         }
     }
 }
