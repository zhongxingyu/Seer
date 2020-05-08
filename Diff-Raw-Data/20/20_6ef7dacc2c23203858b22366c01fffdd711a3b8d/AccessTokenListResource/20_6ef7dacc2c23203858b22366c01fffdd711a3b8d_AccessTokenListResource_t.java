 /**
  * 
  */
 package pl.psnc.dl.wf4ever.oauth;
 
 import java.net.URI;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.db.AccessToken;
 import pl.psnc.dl.wf4ever.db.AccessTokenList;
 import pl.psnc.dl.wf4ever.db.OAuthClient;
 import pl.psnc.dl.wf4ever.db.UserProfile;
 import pl.psnc.dl.wf4ever.db.dao.AccessTokenDAO;
 import pl.psnc.dl.wf4ever.db.dao.OAuthClientDAO;
 import pl.psnc.dl.wf4ever.db.dao.UserProfileDAO;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.exceptions.ForbiddenException;
 import pl.psnc.dl.wf4ever.model.Builder;
 
 /**
  * REST API access tokens resource.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("accesstokens")
 public class AccessTokenListResource {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(AccessTokenListResource.class);
 
     /** URI info. */
     @Context
     private UriInfo uriInfo;
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
 
 
     /**
      * Returns list of access tokens as XML. The optional parameters are client_id and user_id.
      * 
      * @param clientId
      *            client application id
      * @param userId
      *            Base64, url-safe encoded.
      * @return an access token XML encoded
      */
     @GET
     @Produces("text/xml")
     public AccessTokenList getAccessTokenList(@QueryParam("client_id") String clientId,
             @QueryParam("user_id") String userId) {
         if (builder.getUser().getRole() != UserMetadata.Role.ADMIN) {
             throw new ForbiddenException("Only admin users can manage access tokens.");
         }
         OAuthClientDAO oAuthClientDAO = new OAuthClientDAO();
         OAuthClient client = clientId != null ? oAuthClientDAO.findById(clientId) : null;
         UserProfileDAO userProfileDAO = new UserProfileDAO();
         UserProfile userProfile = userId != null ? userProfileDAO.findByLogin(userId) : null;
         AccessTokenDAO accessTokenDAO = new AccessTokenDAO();
         List<AccessToken> list = accessTokenDAO.findByClientOrUser(client, userProfile);
         return new AccessTokenList(list);
     }
 
 
     /**
      * Creates new access token for a given client and user. input: client_id and user.
      * 
      * @param data
      *            text/plain with id in first line and password in second.
      * @return 201 (Created) when the access token was successfully created, 400 (Bad Request) if the user does not
      *         exist
      * @throws BadRequestException
      *             the body is incorrect
      */
     @POST
     @Consumes("text/plain")
     @Produces("text/plain")
     public Response createAccessToken(String data)
             throws BadRequestException {
         if (builder.getUser().getRole() != UserProfile.Role.ADMIN) {
             throw new ForbiddenException("Only admin users can manage access tokens.");
         }
         String[] lines = data.split("[\\r\\n]+");
         if (lines.length < 2) {
             throw new BadRequestException("Content is shorter than 2 lines");
         }
 
         try {
             OAuthClientDAO oAuthClientDAO = new OAuthClientDAO();
             OAuthClient client = oAuthClientDAO.findById(lines[0]);
             if (client == null) {
                 throw new BadRequestException("Client not found");
             }
             UserProfileDAO dao = new UserProfileDAO();
             UserProfile creds = dao.findByLogin(lines[1]);
             if (creds == null) {
                 throw new BadRequestException("User not found");
             }
             AccessTokenDAO accessTokenDAO = new AccessTokenDAO();
             AccessToken accessToken = new AccessToken(client, creds);
             accessTokenDAO.save(accessToken);
             URI resourceUri = uriInfo.getAbsolutePathBuilder().path("/").build().resolve(accessToken.getToken());
 
             return Response.created(resourceUri).build();
         } catch (IllegalArgumentException e) {
             return Response.status(Status.NOT_FOUND).type("text/plain").entity(e.getMessage()).build();
         }
     }
 }
