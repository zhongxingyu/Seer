 package org.exoplatform.extension.exchange.service;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.CacheControl;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import microsoft.exchange.webservices.data.Folder;
 import microsoft.exchange.webservices.data.FolderId;
 
 import org.exoplatform.common.http.HTTPStatus;
 import org.exoplatform.extension.exchange.listener.IntegrationListener;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.rest.resource.ResourceContainer;
 import org.exoplatform.services.security.ConversationState;
 
 /**
  * 
  * @author Boubaker Khanfir
  * 
  */
 @Path("/exchange")
 public class ExchangeRESTService implements ResourceContainer, Serializable {
   private static final long serialVersionUID = -8085801604143848875L;
 
   private static final Log LOG = ExoLogger.getLogger(ExchangeRESTService.class);
   static CacheControl cc = new CacheControl();
   static {
     cc.setNoCache(true);
     cc.setNoStore(true);
   }
 
   private IntegrationListener integrationListener;
   private OrganizationService organizationService;
 
   public ExchangeRESTService(IntegrationListener integrationListener, OrganizationService organizationService) {
     this.integrationListener = integrationListener;
     this.organizationService = organizationService;
   }
 
   @GET
   @RolesAllowed("users")
   @Path("/calendars")
   @Produces({ MediaType.APPLICATION_JSON })
   public Response getCalendars() throws Exception {
     // It must be a user present in the session because of RolesAllowed
     // annotation
     String username = ConversationState.getCurrent().getIdentity().getUserId();
     try {
       List<FolderBean> beans = new ArrayList<FolderBean>();
 
       IntegrationService service = IntegrationService.getInstance(username);
       if (service != null) {
         List<FolderId> folderIDs = service.getAllExchangeCalendars();
         for (FolderId folderId : folderIDs) {
           Folder folder = service.getExchangeCalendar(folderId);
           if (folder != null) {
             boolean synchronizedFolder = service.isCalendarSynchronizedWithExchange(folderId.getUniqueId());
             FolderBean bean = new FolderBean(folderId.getUniqueId(), folder.getDisplayName(), synchronizedFolder);
             beans.add(bean);
           }
         }
       }
       return Response.ok(beans).cacheControl(cc).build();
     } catch (Exception e) {
       if (LOG.isTraceEnabled()) {
         LOG.trace("Exchange Inegration Service: Unable to retrieve list of calendars for user: '" + username + "'");
       }
       return Response.ok().cacheControl(cc).build();
     }
   }
 
   @GET
   @RolesAllowed("users")
   @Path("/sync")
   public Response synchronizeFolderWithExo(@QueryParam("folderId") String folderIdString) throws Exception {
     if (folderIdString == null || folderIdString.isEmpty()) {
       LOG.warn("folderId parameter is null while synchronizing.");
       return Response.noContent().build();
     }
     // It must be a user present in the session because of RolesAllowed
     // annotation
     String username = ConversationState.getCurrent().getIdentity().getUserId();
     IntegrationService service = IntegrationService.getInstance(username);
     service.addFolderToSynchronization(folderIdString);
     return Response.ok().build();
   }
 
   @GET
   @RolesAllowed("users")
   @Path("/unsync")
   public Response unsynchronizeFolderWithExo(@QueryParam("folderId") String folderIdString) throws Exception {
     if (folderIdString == null || folderIdString.isEmpty()) {
       LOG.warn("folderId parameter is null while unsynchronizing");
       return Response.noContent().build();
     }
     // It must be a user present in the session because of RolesAllowed
     // annotation
     String username = ConversationState.getCurrent().getIdentity().getUserId();
     IntegrationService service = IntegrationService.getInstance(username);
     service.deleteFolderFromSynchronization(folderIdString);
     return Response.ok().build();
   }
 
   @GET
   @RolesAllowed("users")
   @Path("/settings")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getSettings() throws Exception {
     try {
       String username = ConversationState.getCurrent().getIdentity().getUserId();
 
       UserSettings settings = new UserSettings();
 
       String exchangeServerName = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_URL_ATTRIBUTE);
       String exchangeDomainName = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_DOMAIN_ATTRIBUTE);
       String exchangeUsername = IntegrationService.getUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_USERNAME_ATTRIBUTE);
 
       settings.setServerName(exchangeServerName == null ? integrationListener.exchangeServerURL : exchangeServerName);
       settings.setDomainName(exchangeDomainName == null ? integrationListener.exchangeDomain : exchangeDomainName);
       settings.setUsername(exchangeUsername == null ? username : exchangeUsername);
 
       return Response.ok(settings, MediaType.APPLICATION_JSON).cacheControl(cc).build();
     } catch (Exception e) {
       return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
     }
   }
 
   @POST
   @RolesAllowed("users")
   @Path("/settings")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response setSettings(UserSettings settings) throws Exception {
     try {
       String username = ConversationState.getCurrent().getIdentity().getUserId();
 
       IntegrationService.setUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_URL_ATTRIBUTE, settings.getServerName());
       IntegrationService.setUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_SERVER_DOMAIN_ATTRIBUTE, settings.getDomainName());
       IntegrationService.setUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_USERNAME_ATTRIBUTE, settings.getUsername());
       IntegrationService.setUserArrtibute(organizationService, username, IntegrationService.USER_EXCHANGE_PASSWORD_ATTRIBUTE, settings.getPassword());
 
       integrationListener.userLoggedOut(username);
      integrationListener.userLoggedIn(username, settings.getUsername(), settings.getPassword(), settings.getDomainName(), settings.getServerName());
 
       return Response.ok().cacheControl(cc).build();
     } catch (Exception e) {
       return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
     }
   }
 
   public static class FolderBean implements Serializable {
     private static final long serialVersionUID = 4517749353533921356L;
 
     String id;
     String name;
     boolean synchronizedFolder = false;
 
     public FolderBean(String id, String name, boolean synchronizedFolder) {
       this.id = id;
       this.name = name;
       this.synchronizedFolder = synchronizedFolder;
     }
 
     public String getId() {
       return id;
     }
 
     public void setId(String id) {
       this.id = id;
     }
 
     public String getName() {
       return name;
     }
 
     public void setName(String name) {
       this.name = name;
     }
 
     public boolean isSynchronizedFolder() {
       return synchronizedFolder;
     }
 
     public void setSynchronizedFolder(boolean synchronizedFolder) {
       this.synchronizedFolder = synchronizedFolder;
     }
   }
 
   public static class UserSettings implements Serializable {
     private static final long serialVersionUID = -3248503274980906631L;
 
     private String serverName;
     private String domainName;
     private String username;
     private String password;
 
     public String getServerName() {
       return serverName;
     }
 
     public void setServerName(String serverName) {
       this.serverName = serverName;
     }
 
     public String getDomainName() {
       return domainName;
     }
 
     public void setDomainName(String domainName) {
       this.domainName = domainName;
     }
 
     public String getUsername() {
       return username;
     }
 
     public void setUsername(String username) {
       this.username = username;
     }
 
     protected String getPassword() {
       return password;
     }
 
     public void setPassword(String password) {
       this.password = password;
     }
 
   }
 }
