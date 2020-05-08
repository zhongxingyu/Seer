 package org.hackystat.dailyprojectdata.resource.dailyprojectdata;
 
 import java.util.Map;
 
 import org.hackystat.dailyprojectdata.server.Server;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.restlet.Context;
 import org.restlet.data.CharacterSet;
 import org.restlet.data.Language;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Resource;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 import static 
 org.hackystat.dailyprojectdata.server.Authenticator.AUTHENTICATOR_SENSORBASECLIENTS_KEY;
 
 /**
  * An abstract superclass for all DailyProjectData resources that supplies common 
  * initialization processing. 
  * This includes:
  * <ul>
  * <li> Extracting the authenticated user identifier (when authentication available)
  * <li> Extracting the user email from the URI (when available)
  * <li> Declares that the TEXT/XML representational variant is supported.
  * </ul>
  * 
  * @author Philip Johnson
  *
  */
 public abstract class DailyProjectDataResource extends Resource {
   
   /** To be retrieved from the URL as the 'email' template parameter, or null. */
   protected String uriUser = null; 
 
   /** To be retrieved from the URL as the 'project' template parameter, or null. */
   protected String project = null; 
 
   /** To be retrieved from the URL as the 'timestamp' template parameter, or null. */
   protected String timestamp = null; 
 
   /** The authenticated user, retrieved from the ChallengeResponse, or null */
   protected String authUser = null;
   
   /** The server. */
   protected Server server;
   
   /** The standard error message returned from invalid authentication. */
   protected String badAuth = "User is not admin and authenticated user does not not match URI user";
   
   /**
    * Provides the following representational variants: TEXT_XML.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public DailyProjectDataResource(Context context, Request request, Response response) {
     super(context, request, response);
     if (request.getChallengeResponse() != null) {
       this.authUser = request.getChallengeResponse().getIdentifier();
     }
     this.server = (Server)getContext().getAttributes().get("DailyProjectDataServer");
     this.uriUser = (String) request.getAttributes().get("user");
     this.project = (String) request.getAttributes().get("project");
     this.timestamp = (String) request.getAttributes().get("timestamp");
     getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
     getVariants().add(new Variant(MediaType.TEXT_XML));
   }
 
   /**
    * The Restlet getRepresentation method which must be overridden by all concrete Resources.
    * @param variant The variant requested.
    * @return The Representation. 
    */
   @Override
   public abstract Representation getRepresentation(Variant variant);
   
   /**
    * Creates and returns a new Restlet StringRepresentation built from xmlData.
    * The xmlData will be prefixed with a processing instruction indicating UTF-8 and version 1.0.
    * @param xmlData The xml data as a string. 
    * @return A StringRepresentation of that xmldata. 
    */
   public StringRepresentation getStringRepresentation(String xmlData) {
     return new StringRepresentation(xmlData, MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8);
   }
   
   /**
    * Returns a SensorBaseClient instance associated with the User in this request. 
    * @return The SensorBaseClient instance. 
    */
   @SuppressWarnings("unchecked")
   public SensorBaseClient getSensorBaseClient() {
     Map<String, SensorBaseClient> userClientMap = 
       (Map<String, SensorBaseClient>)this.server.getContext()
       .getAttributes().get(AUTHENTICATOR_SENSORBASECLIENTS_KEY);
    return userClientMap.get(this.authUser);
   }
 
 }
