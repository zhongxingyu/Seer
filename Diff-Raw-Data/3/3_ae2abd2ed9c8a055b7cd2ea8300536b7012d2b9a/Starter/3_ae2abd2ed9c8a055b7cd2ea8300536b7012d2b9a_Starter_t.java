     /*******************************************************************************
  * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
  *
  * This file is part of SITools2.
  *
  * SITools2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SITools2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package fr.cnes.sitools.server;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.restlet.Application;
 import org.restlet.Client;
 import org.restlet.Component;
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.Server;
 import org.restlet.data.Method;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.ext.solr.SolrClientHelper;
 import org.restlet.resource.ResourceException;
 import org.restlet.routing.Filter;
 import org.restlet.routing.VirtualHost;
 import org.restlet.service.LogService;
 
 import fr.cnes.sitools.applications.AdministratorApplication;
 import fr.cnes.sitools.applications.ClientAdminApplication;
 import fr.cnes.sitools.applications.ClientUserApplication;
 import fr.cnes.sitools.applications.OrdersFilesApplication;
 import fr.cnes.sitools.applications.PublicApplication;
 import fr.cnes.sitools.applications.TemporaryFolderApplication;
 import fr.cnes.sitools.applications.UploadApplication;
 import fr.cnes.sitools.collections.CollectionsApplication;
 import fr.cnes.sitools.collections.model.Collection;
 import fr.cnes.sitools.common.SitoolsComponent;
 import fr.cnes.sitools.common.SitoolsSettings;
 import fr.cnes.sitools.common.application.ContextAttributes;
 import fr.cnes.sitools.common.application.StaticWebApplication;
 import fr.cnes.sitools.common.exception.SitoolsException;
 import fr.cnes.sitools.common.model.Category;
 import fr.cnes.sitools.common.store.SitoolsStore;
 import fr.cnes.sitools.dataset.DataSetAdministration;
 import fr.cnes.sitools.dataset.converter.ConverterApplication;
 import fr.cnes.sitools.dataset.converter.model.ConverterChainedModel;
 import fr.cnes.sitools.dataset.filter.FilterApplication;
 import fr.cnes.sitools.dataset.filter.model.FilterChainedModel;
 import fr.cnes.sitools.dataset.model.DataSet;
 import fr.cnes.sitools.dataset.opensearch.OpenSearchApplication;
 import fr.cnes.sitools.dataset.opensearch.model.Opensearch;
 import fr.cnes.sitools.dataset.plugins.converters.ConverterPluginsApplication;
 import fr.cnes.sitools.dataset.plugins.filters.FilterPluginsApplication;
 import fr.cnes.sitools.dataset.services.ServiceApplication;
 import fr.cnes.sitools.dataset.services.model.ServiceCollectionModel;
 import fr.cnes.sitools.dataset.view.DatasetViewApplication;
 import fr.cnes.sitools.dataset.view.model.DatasetView;
 import fr.cnes.sitools.datasource.jdbc.JDBCDataSourceAdministration;
 import fr.cnes.sitools.datasource.jdbc.model.JDBCDataSource;
 import fr.cnes.sitools.datasource.mongodb.MongoDBDataSourceAdministration;
 import fr.cnes.sitools.datasource.mongodb.model.MongoDBDataSource;
 import fr.cnes.sitools.dictionary.ConceptTemplateAdministration;
 import fr.cnes.sitools.dictionary.DictionaryAdministration;
 import fr.cnes.sitools.dictionary.model.ConceptTemplate;
 import fr.cnes.sitools.dictionary.model.Dictionary;
 import fr.cnes.sitools.feeds.FeedsApplication;
 import fr.cnes.sitools.feeds.model.FeedModel;
 import fr.cnes.sitools.form.components.FormComponentsApplication;
 import fr.cnes.sitools.form.components.model.FormComponent;
 import fr.cnes.sitools.form.dataset.FormApplication;
 import fr.cnes.sitools.form.dataset.model.Form;
 import fr.cnes.sitools.form.project.FormProjectApplication;
 import fr.cnes.sitools.form.project.model.FormProject;
 import fr.cnes.sitools.inscription.InscriptionApplication;
 import fr.cnes.sitools.inscription.UserInscriptionApplication;
 import fr.cnes.sitools.inscription.model.Inscription;
 import fr.cnes.sitools.logging.LogDataServerService;
 import fr.cnes.sitools.logging.SitoolsApplicationLogFilter;
 import fr.cnes.sitools.mail.MailAdministration;
 import fr.cnes.sitools.notification.NotificationApplication;
 import fr.cnes.sitools.notification.business.NotificationManager;
 import fr.cnes.sitools.notification.store.NotificationStore;
 import fr.cnes.sitools.order.OrderAdministration;
 import fr.cnes.sitools.order.UserOrderApplication;
 import fr.cnes.sitools.order.model.Order;
 import fr.cnes.sitools.plugins.applications.ApplicationPluginApplication;
 import fr.cnes.sitools.plugins.applications.ApplicationPluginStore;
 import fr.cnes.sitools.plugins.filters.FilterClassPluginApplication;
 import fr.cnes.sitools.plugins.filters.FilterPluginApplication;
 import fr.cnes.sitools.plugins.filters.model.FilterModel;
 import fr.cnes.sitools.plugins.guiservices.declare.GuiServiceApplication;
 import fr.cnes.sitools.plugins.guiservices.declare.model.GuiServiceModel;
 import fr.cnes.sitools.plugins.guiservices.implement.GuiServicePluginApplication;
 import fr.cnes.sitools.plugins.guiservices.implement.model.GuiServicePluginModel;
 import fr.cnes.sitools.plugins.resources.ResourceClassPluginApplication;
 import fr.cnes.sitools.plugins.resources.ResourcePluginApplication;
 import fr.cnes.sitools.plugins.resources.model.ResourceModel;
 import fr.cnes.sitools.portal.PortalApplication;
 import fr.cnes.sitools.portal.PortalStore;
 import fr.cnes.sitools.portal.multidatasets.opensearch.MultiDsOsApplication;
 import fr.cnes.sitools.project.ProjectAdministration;
 import fr.cnes.sitools.project.graph.model.Graph;
 import fr.cnes.sitools.project.model.Project;
 import fr.cnes.sitools.project.modules.ProjectModuleApplication;
 import fr.cnes.sitools.project.modules.model.ProjectModuleModel;
 import fr.cnes.sitools.proxy.ProxySettings;
 import fr.cnes.sitools.registry.AppRegistryApplication;
 import fr.cnes.sitools.registry.model.AppRegistry;
 import fr.cnes.sitools.role.RoleApplication;
 import fr.cnes.sitools.role.model.Role;
 import fr.cnes.sitools.security.UsersAndGroupsAdministration;
 import fr.cnes.sitools.security.UsersAndGroupsStore;
 import fr.cnes.sitools.security.authentication.SitoolsMemoryRealm;
 import fr.cnes.sitools.security.authentication.SitoolsRealm;
 import fr.cnes.sitools.security.authorization.AuthorizationApplication;
 import fr.cnes.sitools.security.authorization.AuthorizationStore;
 import fr.cnes.sitools.security.captcha.CaptchaContainer;
 import fr.cnes.sitools.security.ssl.SslFactory;
 import fr.cnes.sitools.service.storage.DataStorageStore;
 import fr.cnes.sitools.service.storage.StorageAdministration;
 import fr.cnes.sitools.service.storage.StorageApplication;
 import fr.cnes.sitools.solr.SolrApplication;
 import fr.cnes.sitools.status.SitoolsStatusService;
 import fr.cnes.sitools.tasks.exposition.TaskApplication;
 import fr.cnes.sitools.tasks.model.TaskModel;
 import fr.cnes.sitools.trigger.AuthorizationTrigger;
 import fr.cnes.sitools.trigger.DataStorageTrigger;
 import fr.cnes.sitools.trigger.DefaultGuiServicesTrigger;
 import fr.cnes.sitools.trigger.GroupTrigger;
 import fr.cnes.sitools.trigger.RoleTrigger;
 import fr.cnes.sitools.trigger.UserTrigger;
 import fr.cnes.sitools.units.UnitsApplication;
 import fr.cnes.sitools.units.dimension.DimensionAdministration;
 import fr.cnes.sitools.units.dimension.model.SitoolsDimension;
 import fr.cnes.sitools.userstorage.UserStorageApplication;
 import fr.cnes.sitools.userstorage.UserStorageManagement;
 import fr.cnes.sitools.userstorage.UserStorageStore;
 
 /**
  * Server Starting class.
  * 
  * @author AKKA Technologies
  */
 public final class Starter {
 
   /** Resource bundle name */
   public static final String BUNDLE = "sitools";
 
   /** Maximal connections */
   public static final int DEFAULT_CONNECTIONS = 50;
 
   /** ----------------------- */
   /** Global Server variables */
 
   /** Component server */
   private static Component server = null;
 
   /** appManager for monitoring applications */
   private static AppRegistryApplication appManager = null;
 
   /**
    * Private constructor for utility classes
    */
   private Starter() {
   }
 
   /**
    * For Test purpose
    * 
    * @param hostindex
    *          no d'ordre d'application attachée au composant.
    * @return Application
    */
   public static Application getApplication(int hostindex) {
     return server.getHosts().get(hostindex).getApplication();
   }
 
   /**
    * Run the server as a stand-alone component.
    * 
    * @param args
    *          The optional arguments.
    */
   public static void main(String[] args) {
 
     SitoolsSettings settings = SitoolsSettings.getInstance(BUNDLE, Starter.class.getClassLoader(), Locale.FRANCE, true);
 
     if (args.length > 0 && "-migration".equals(args[0])) {
       settings.setStartWithMigration(true);
     }
 
     startWithProxy(args, settings);
   }
 
   /**
    * setup proxy configuration Default none.
    * 
    * @param args
    *          The optional arguments.
    * @param settings
    *          SitoolsSettings
    */
   public static void startWithProxy(String[] args, SitoolsSettings settings) {
     try {
 
       // ===========================================================================
       // PROXY CONFIGURATION
 
       ProxySettings.init(args, settings);
 
       // ===========================================================================
       // Builds and starts the server
 
       // TODO args : String hostname, int port, String publicHostName
 
       start(null, 0, null);
 
     }
     catch (Exception e) {
       System.err.println("ERROR starting SITools2 server.");
       System.err.println(e.getMessage());
       e.printStackTrace();
     }
   }
 
   /**
    * Builds and starts the server component.
    * 
    * @param hostname
    *          host name default "localhost"
    * @param port
    *          port HTTP for server / virtual host . Default 8182
    * @param publicHostName
    *          public host reference of server
    * @throws Exception
    *           always possible
    */
   @SuppressWarnings("unchecked")
   public static void start(String hostname, int port, String publicHostName) throws Exception {
     if (server != null) {
       System.err.println("SERVER ALREADY STARTED");
       System.exit(-1);
     }
 
     // ============================
     // Sitools settings
     SitoolsSettings settings = SitoolsSettings.getInstance(BUNDLE, Starter.class.getClassLoader(), Locale.FRANCE, true);
 
     String hostPort = (port != 0) ? String.valueOf(port) : settings.getString("Starter.HOST_PORT");
     hostPort = ((hostPort != null) && !hostPort.equals("")) ? hostPort : SitoolsSettings.DEFAULT_HOST_PORT;
 
     // ============================
     // Create a component
     Component component = new SitoolsComponent(settings);
 
     // ============================
     // Logging configuration
 
     String logConfigFile = settings.getRootDirectory() + settings.getString("Starter.Logging.configFile");
     File loggingConfigFile = new File(logConfigFile);
     if (!loggingConfigFile.exists()) {
       System.err.println("Config file not found :" + logConfigFile);
     }
     else {
       System.setProperty("java.util.logging.config.file", logConfigFile);
     }
 
     // ============================
     // Logs access
 
     String logOutputFile = settings.getRootDirectory() + settings.getString("Starter.LogService.outputFile");
     String logLevelName = settings.getString("Starter.LogService.levelName");
     String logFormat = settings.getString("Starter.LogService.logFormat");
     String logName = settings.getString("Starter.LogService.logName");
     boolean logActive = Boolean.parseBoolean(settings.getString("Starter.LogService.active"));
 
     LogService logService = new LogDataServerService(logOutputFile, logLevelName, logFormat, logName, logActive);
 
     component.setLogService(logService);
 
     String appLogOutputFile = settings.getRootDirectory() + settings.getString("Starter.AppLogService.outputFile");
     String appLogLevelName = settings.getString("Starter.AppLogService.levelName");
     String appLogFormat = settings.getString("Starter.AppLogService.logFormat");
     String appLogName = settings.getString("Starter.AppLogService.logName");
     boolean appLogActive = Boolean.parseBoolean(settings.getString("Starter.AppLogService.active"));
 
     LogService logServiceApplication = new LogDataServerService(appLogOutputFile, appLogLevelName, appLogFormat,
         appLogName, appLogActive) {
       /*
        * (non-Javadoc)
        * 
        * @see org.restlet.service.LogService#createInboundFilter(org.restlet.Context)
        */
       @Override
       public Filter createInboundFilter(Context context) {
         return new SitoolsApplicationLogFilter(context, this);
       }
     };
     component.getServices().add(logServiceApplication);
 
     // ============================
     // Protocols
 
     // HTTP
     
     // if IP address is specified for the HTTP protocol (useful when multiple
     // address available)
     String ipAddress = settings.getString("Starter.component.protocol.HTTP.address");
    
     Server serverHTTP = null;
     if ((ipAddress != null) && !ipAddress.equals("")) {
       serverHTTP = component.getServers().add(Protocol.HTTP, ipAddress, Integer.parseInt(hostPort));
     }
     else {
       serverHTTP = component.getServers().add(Protocol.HTTP, Integer.parseInt(hostPort));
     }
     
     JettyProperties jettyProps = new JettyProperties();
     jettyProps.setValues(settings);
     jettyProps.addParamsToServerContext(serverHTTP);
     
     serverHTTP.getContext().getParameters().add("useForwardedForHeader", settings.getString(Consts.USE_FORWARDED_FOR_HEADER));
     
     serverHTTP.getContext().getAttributes().put("maxThreads", DEFAULT_CONNECTIONS);
     serverHTTP.getContext().getAttributes().put("maxTotalConnections", DEFAULT_CONNECTIONS);
     serverHTTP.getContext().getAttributes().put("maxConnectionsPerHost", DEFAULT_CONNECTIONS);
     
     component.getClients().add(Protocol.FILE);
     component.getClients().add(Protocol.HTTP);
     component.getClients().add(Protocol.CLAP);
     component.getClients().add(Protocol.ZIP);
 
     // component.getContext().getAttributes().put("ROOT_DIRECTORY",
     // ROOT_DIRECTORY );
     component.getContext().getAttributes().put("ROOT_DIRECTORY", settings.getRootDirectory());
 
     // HTTPS
     component = SslFactory.addSslSupport(component, settings);
 
     // ============================
     // Init Stores
     Map<String, Object> stores = null;
     try {
       stores = StoreHelper.initContext(component.getContext());
     }
     catch (SitoolsException e) {
       startServerFailed(settings, component, e);
       return;
     }
     settings.setStores(stores);
 
     // ============================
     // Authentication / Authorizations
 
     // Store Role
     SitoolsStore<Role> storeRole = (SitoolsStore<Role>) settings.getStores().get(Consts.APP_STORE_ROLE);
 
     // Store Users and Groups
     UsersAndGroupsStore storeUandG = (UsersAndGroupsStore) settings.getStores().get(Consts.APP_STORE_USERSANDGROUPS);
 
     String realm = settings.getString(Consts.REALM_CLASS, SitoolsMemoryRealm.class.getName());
 
     SitoolsRealm smr;
     try {
       Class<?> realmClass = Class.forName(realm);
       smr = (SitoolsRealm) realmClass.getConstructor(UsersAndGroupsStore.class, SitoolsStore.class,
           SitoolsSettings.class).newInstance(storeUandG, storeRole, settings);
     }
     catch (Exception e) {
       startServerFailed(settings, component, e);
       return;
     }
 
     // Realm
     // SitoolsRealm smr = new LdapMemoryRealm(storeUandG, storeRole, settings);
 
     // Realm
     // SitoolsRealm smr = new SitoolsMemoryRealm(storeUandG, storeRole, settings);
     component.getContext().setDefaultEnroler(smr.getEnroler());
     component.getContext().setDefaultVerifier(smr.getVerifier());
 
     // Set global AuthenticationRealm in SitoolsProperties.
     settings.setAuthenticationRealm(smr);
     component.getContext().getAttributes().put(ContextAttributes.APP_REALM, smr);
 
     // =============================================================
     // Create a virtual host
     Context vhostContext = component.getContext().createChildContext();
     VirtualHost host = Starter.initVirtualHost(vhostContext, settings);
 
     // host.getContext().getAttributes().put("maxThreads", 50);
     // host.getContext().getAttributes().put("maxTotalConnections", 50);
     // host.getContext().getAttributes().put("maxConnectionsPerHost", 50);
 
     // host.setHostPort(hostPort); // settings.getString("Starter.HOST_PORT")
 
     // host.setMaxAttempts(0);
 
     // =============================================================
     // Create applications
 
     // repertoire de reference pour toutes les applications
     String appPath = settings.getString(Consts.APP_PATH);
     component.getContext().getAttributes().put("APP_PATH", appPath);
 
     // racine d'attachement des applications
     String baseUrl = settings.getString(Consts.APP_URL);
     component.getContext().getAttributes().put("BASE_URL", baseUrl);
 
     // racine de l'url publique (listings de repertoires)
     if (publicHostName != null) {
       settings.setPublicHostDomain(publicHostName);
     }
     String baseRef = settings.getPublicHostDomain();
     component.getContext().getAttributes().put("PUBLIC_HOST_DOMAIN", baseRef);
 
     // for each application
     Context appContext = null;
     String appReference = null;
 
     // ============================
     // SERVER STATUS SERVICE
     Starter.initStatusService(settings, baseUrl, component);
 
     // ==============================
     // RESOURCES NOTIFICATION MANAGER
 
     // Store
     NotificationStore storeNotification = (NotificationStore) settings.getStores().get(Consts.APP_STORE_NOTIFICATION);
     // Notification manager
     NotificationManager notificationManager = new NotificationManager(storeNotification);
 
     // Instance
     settings.setNotificationManager(notificationManager);
 
     // =============================================================
     // Create applications
 
     // ===========================================================================
     // ApplicationManager for application registering
 
     // Store
     SitoolsStore<AppRegistry> storeApp = (SitoolsStore<AppRegistry>) settings.getStores()
         .get(Consts.APP_STORE_REGISTRY);
 
     // Context
     appContext = host.getContext().createChildContext();
     appReference = baseUrl + settings.getString(Consts.APP_APPLICATIONS_URL);
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeApp);
 
     // Application
     appManager = new AppRegistryApplication(appContext);
     appManager.setHost(host);
 
     // for applications whose attach / detach themselves other applications to
     // the virtualhost.
     settings.setAppRegistry(appManager);
 
     // Attachment => GOTO the end
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_APPLICATIONS_URL), appManager);
 
     // TODO SETS DEFAULT SECURITY ACCESS FOR AppManager if not exists ?
 
     // ===========================================================================
     // AuthorizationApplication for application security
 
     // Store
     AuthorizationStore storeAuthorization = (AuthorizationStore) settings.getStores().get(
         Consts.APP_STORE_AUTHORIZATION);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_AUTHORIZATIONS_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeAuthorization);
 
     // Application
     AuthorizationApplication appAuthorization = new AuthorizationApplication(appContext);
 
     // Attachement
     component.getInternalRouter().attach(settings.getString(Consts.APP_AUTHORIZATIONS_URL), appAuthorization);
 
     appManager.attachApplication(appAuthorization);
 
     // ===================================
     // RESOURCES NOTIFICATIONS APPLICATION
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_NOTIFICATIONS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeNotification);
 
     // Application
     NotificationApplication notificationApplication = new NotificationApplication(appContext);
 
     // Attachment
     appManager.attachApplication(notificationApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_NOTIFICATIONS_URL), notificationApplication);
 
     // ===========================================================================
     // StaticWebApplications
 
     // -------------------------
     // LOGS
 
     // Directory
     String logAppPath = appPath + settings.getString(Consts.APP_LOGS_PATH);
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_LOGS_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     StaticWebApplication logApp = new StaticWebApplication(appContext, logAppPath, baseRef + appReference) {
 
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.ADMIN);
         this.setName("LogDirectory");
         this.setDescription("This application give access to the server logs\n"
             + "It would be better to give authorizations only to the administrator");
       }
     };
 
     // Attachment
     appManager.attachApplication(logApp);
 
     // -------------------------
     // Client-public application (commons)
 
     // Directory
     String publicAppPath = appPath + settings.getString(Consts.APP_CLIENT_PUBLIC_PATH);
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_CLIENT_PUBLIC_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     PublicApplication publicApp = new PublicApplication(appContext, publicAppPath, baseRef + appReference);
 
     // Attachment
     appManager.attachApplication(publicApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_CLIENT_PUBLIC_PATH), publicApp);
 
     // ------------------------
     // Client-admin application
 
     // Directory
     String adminAppPath = appPath + settings.getString(Consts.APP_CLIENT_ADMIN_PATH);
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_CLIENT_ADMIN_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     ClientAdminApplication clientAdminApp = new ClientAdminApplication(appContext, adminAppPath, baseRef + appReference);
 
     // Attachment
     appManager.attachApplication(clientAdminApp);
 
     // -----------------------
     // Client-user application
 
     // Directory
     String userAppPath = appPath + settings.getString(Consts.APP_CLIENT_USER_PATH);
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_CLIENT_USER_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     ClientUserApplication clientUserApp = new ClientUserApplication(appContext, userAppPath, baseRef + appReference);
 
     // Attachment
     appManager.attachApplication(clientUserApp);
 
     // ===========================================================================
     // Gestion des utilisateurs / groupes
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appReference = baseUrl + settings.getString(Consts.APP_SECURITY_URL);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeUandG);
 
     // Application
     UsersAndGroupsAdministration anApplication = new UsersAndGroupsAdministration(appContext);
 
     // Attachment
     appManager.attachApplication(anApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_SECURITY_URL), anApplication);
 
     // ===========================================================================
     // Gestion des inscriptions user et admin
 
     // Store
     SitoolsStore<Inscription> storeIns = (SitoolsStore<Inscription>) settings.getStores().get(
         Consts.APP_STORE_INSCRIPTION);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_INSCRIPTIONS_ADMIN_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeIns);
 
     // Application à sécuriser pour l'administration des inscriptions.
     InscriptionApplication inscriptionApplication = new InscriptionApplication(appContext);
 
     // Attachment
     appManager.attachApplication(inscriptionApplication);
 
     // INSCRIPTION DES UTILISATEURS
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_INSCRIPTIONS_USER_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeIns);
 
     CaptchaContainer captchaContainer = new CaptchaContainer();
     appContext.getAttributes().put("Security.Captcha.CaptchaContainer", captchaContainer);
 
     // Application publique pour pouvoir s'enregistrer
     UserInscriptionApplication userInscriptions = new UserInscriptionApplication(appContext);
 
     // Attachment
     appManager.attachApplication(userInscriptions);
 
     // ===========================================================================
     // Gestion des roles
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_ROLES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeRole);
 
     // Application
     RoleApplication roleApplication = new RoleApplication(appContext);
 
     // Attachment
     appManager.attachApplication(roleApplication);
 
     // ===========================================================================
     // Gestion des datasouces jdbc
 
     // Store
     SitoolsStore<JDBCDataSource> storeDS = (SitoolsStore<JDBCDataSource>) settings.getStores().get(
         Consts.APP_STORE_DATASOURCE);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDS);
 
     // Application
     JDBCDataSourceAdministration appDS = new JDBCDataSourceAdministration(host, appContext);
 
     // Attachment
     appManager.attachApplication(appDS);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASOURCES_URL), appDS);
 
     // ===========================================================================
     // Gestion des datasouces mongodb
 
     // Store
     SitoolsStore<MongoDBDataSource> storeMongoDBDs = (SitoolsStore<MongoDBDataSource>) settings.getStores().get(
         Consts.APP_STORE_DATASOURCE_MONGODB);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASOURCES_MONGODB_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeMongoDBDs);
 
     // Application
     MongoDBDataSourceAdministration appMongoDBDS = new MongoDBDataSourceAdministration(host, appContext);
 
     // Attachment
     appManager.attachApplication(appMongoDBDS);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASOURCES_MONGODB_URL), appMongoDBDS);
 
     // ===========================================================================
     // SitoolsEngine pour decouvrir les classes de converters / filters / svas
 
     // SitoolsEngine engine = SitoolsEngine.getInstance();
 
     // ==========================================
     // Gestion des converters attaches au dataset
 
     // Store
     SitoolsStore<ConverterChainedModel> storeConv = (SitoolsStore<ConverterChainedModel>) settings.getStores().get(
         Consts.APP_STORE_DATASETS_CONVERTERS);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
         + settings.getString(Consts.APP_DATASETS_CONVERTERS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeConv);
 
     // Application
     ConverterApplication converterApp = new ConverterApplication(appContext);
 
     // Attachment
     appManager.attachApplication(converterApp);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
             + settings.getString(Consts.APP_DATASETS_CONVERTERS_URL), converterApp);
 
     // ==========================================
     // Gestion des filters attaches au dataset
 
     // Store
     SitoolsStore<FilterChainedModel> storeFilter = (SitoolsStore<FilterChainedModel>) settings.getStores().get(
         Consts.APP_STORE_DATASETS_FILTERS);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
         + settings.getString(Consts.APP_DATASETS_FILTERS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFilter);
 
     // Application
     FilterApplication filterApp = new FilterApplication(appContext);
 
     // Attachment
     appManager.attachApplication(filterApp);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
             + settings.getString(Consts.APP_DATASETS_FILTERS_URL), filterApp);
 
     // ===========================================================================
     // Gestion des plugins d'application
     // Et exposition des plugins d'ApplicationPlugin
 
     // Store
     ApplicationPluginStore appPluginStore = (ApplicationPluginStore) settings.getStores().get(
         Consts.APP_STORE_PLUGINS_APPLICATIONS);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PLUGINS_APPLICATIONS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, appPluginStore);
 
     // Application
     ApplicationPluginApplication appPlugApp = new ApplicationPluginApplication(host, appContext);
 
     // Attachment
     appManager.attachApplication(appPlugApp);
 
     // component.getInternalRouter().attach(settings.getString(Consts.APP_PLUGINS_APPLICATIONS_URL"),
     // appPlugApp);
 
     // =============================================================================
     // Gestion des filtres dynamiques attaches aux applications ou autres restlets internes (DataStorage Directory)
 
     // Store
     SitoolsStore<FilterModel> filterPluginStore = (SitoolsStore<FilterModel>) settings.getStores().get(
         Consts.APP_STORE_PLUGINS_FILTERS);
 
     // Reference - only one filter can be defined for an {objectId}
     appReference = baseUrl + settings.getString(Consts.APP_PLUGINS_FILTERS_INSTANCES_URL); // + "/{parentId}";
     // appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_URL) + "/{parentId}"
     // + settings.getString(Consts.APP_PLUGINS_RESOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, filterPluginStore);
 
     // Application
     FilterPluginApplication filterPluginsApp = new FilterPluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(filterPluginsApp);
 
     component.getInternalRouter()
         .attach(settings.getString(Consts.APP_PLUGINS_FILTERS_INSTANCES_URL), filterPluginsApp);
 
     // ==========================================
     // Gestion des resources dynamiques attachees au projet
 
     // Store
     SitoolsStore<ResourceModel> resPlugStore = (SitoolsStore<ResourceModel>) settings.getStores().get(
         Consts.APP_STORE_PLUGINS_RESOURCES);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_URL) + "/{parentId}"
         + settings.getString(Consts.APP_RESOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, resPlugStore);
 
     SitoolsStore<TaskModel> taskModelStore = (SitoolsStore<TaskModel>) settings.getStores().get(Consts.APP_STORE_TASK);
 
     appContext.getAttributes().put(Consts.APP_STORE_TASK, taskModelStore);
     appContext.getAttributes().put(Consts.APP_STORE_PLUGINS_RESOURCES, resPlugStore);
 
     // Application
     ResourcePluginApplication resourcePluginApp = new ResourcePluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(resourcePluginApp);
 
     // Toutes les resources pour tous les objets, attaché derrière APP_APPLICATION_URL car il s'agit du cas généralisé
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_APPLICATIONS_URL) + "/{parentId}" + settings.getString(Consts.APP_RESOURCES_URL),
         resourcePluginApp);
 
     // ==========================================
     // Gestion des resources dynamiques attachees aux datasets
     // ou resource riap sur la DatasetAdministration avec riap redirection sur la ResourceApp
     // Store
     // idem
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{parentId}"
         + settings.getString(Consts.APP_RESOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, resPlugStore);
 
     // Application
     ResourcePluginApplication resourcePluginAppOnDataset = new ResourcePluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(resourcePluginAppOnDataset);
 
     // ==========================================
     // Gestion des resources dynamiques attachees aux applications
 
     // Store
     // idem
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_APPLICATIONS_URL) + "/{parentId}"
         + settings.getString(Consts.APP_RESOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, resPlugStore);
 
     // Application
     ResourcePluginApplication resourcePluginAppOnApp = new ResourcePluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(resourcePluginAppOnApp);
 
     // ===========================================================================
     // Dictionary management
 
     // Store
     SitoolsStore<Dictionary> storeDictionary = (SitoolsStore<Dictionary>) settings.getStores().get(
         Consts.APP_STORE_DICTIONARY);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DICTIONARIES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDictionary);
 
     // Application
     DictionaryAdministration dictionaryApp = new DictionaryAdministration(appContext);
 
     // Attachment
     appManager.attachApplication(dictionaryApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DICTIONARIES_URL), dictionaryApp);
 
     // ===========================================================================
     // Gestion des datasets
 
     // Store
     SitoolsStore<DataSet> storeDataSet = (SitoolsStore<DataSet>) settings.getStores().get(Consts.APP_STORE_DATASET);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDataSet);
 
     // Application
     DataSetAdministration dataSetAdministration = new DataSetAdministration(host, appContext);
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASETS_URL), dataSetAdministration);
 
     // Attachment
     appManager.attachApplication(dataSetAdministration);
 
     // ===========================================================================
     // ConceptTemplate management
 
     // Store
     SitoolsStore<ConceptTemplate> storeConceptTemplate = (SitoolsStore<ConceptTemplate>) settings.getStores().get(
         Consts.APP_STORE_TEMPLATE);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DICTIONARIES_TEMPLATES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeConceptTemplate);
 
     // Application
     ConceptTemplateAdministration templateApp = new ConceptTemplateAdministration(appContext);
 
     // Attachment
     appManager.attachApplication(templateApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DICTIONARIES_TEMPLATES_URL), templateApp);
 
     // ===========================================================================
     // Informations du portail
 
     // Store
     PortalStore storePortal = (PortalStore) settings.getStores().get(Consts.APP_STORE_PORTAL);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PORTAL_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storePortal);
 
     // Application
     PortalApplication portalApplication = new PortalApplication(appContext);
 
     // Attachment
     appManager.attachApplication(portalApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_PORTAL_URL), portalApplication);
 
     // ===========================================================================
     // Gestion des opensearch multidatasets
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PORTAL_URL) + settings.getString(Consts.APP_OPENSEARCH_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     MultiDsOsApplication multiDsOsApplication = new MultiDsOsApplication(appContext);
 
     // Attachment
     appManager.attachApplication(multiDsOsApplication);
 
     String osPortalRIAPurl = settings.getString(Consts.APP_PORTAL_URL) + settings.getString(Consts.APP_OPENSEARCH_URL);
 
     component.getInternalRouter().attach(osPortalRIAPurl, multiDsOsApplication);
 
     // ===========================================================================
     // Gestion des formComponents
 
     // Store
     SitoolsStore<FormComponent> storefc = (SitoolsStore<FormComponent>) settings.getStores().get(
         Consts.APP_STORE_FORMCOMPONENT);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_FORMCOMPONENTS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storefc);
 
     // Application
     FormComponentsApplication formComponentsApplication = new FormComponentsApplication(appContext);
 
     // Attachment
     appManager.attachApplication(formComponentsApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_FORMCOMPONENTS_URL), formComponentsApplication);
 
     // ===========================================================================
     // Gestion des Collections
 
     // Store
     SitoolsStore<Collection> storeCollections = (SitoolsStore<Collection>) settings.getStores().get(
         Consts.APP_STORE_COLLECTIONS);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_COLLECTIONS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeCollections);
 
     // Application
     CollectionsApplication collectionsApplication = new CollectionsApplication(appContext);
 
     // Attachment
     appManager.attachApplication(collectionsApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_COLLECTIONS_URL), collectionsApplication);
 
     // ===========================================================================
     // Gestion des formulaires MultiDatasets
 
     // Store
     SitoolsStore<FormProject> storeFormProject = (SitoolsStore<FormProject>) settings.getStores().get(
         Consts.APP_STORE_FORMPROJECT);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_URL) + "/{projectId}"
         + settings.getString(Consts.APP_FORMPROJECT_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFormProject);
 
     // Application
     FormProjectApplication formProjectApplication = new FormProjectApplication(appContext);
 
     // Attachment
     appManager.attachApplication(formProjectApplication);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_PROJECTS_URL) + "/{projectId}" + settings.getString(Consts.APP_FORMPROJECT_URL),
         formProjectApplication);
     // ===========================================================================
     // Gestion des datasets views
 
     // Store
     SitoolsStore<DatasetView> storeDsView = (SitoolsStore<DatasetView>) settings.getStores().get(
         Consts.APP_STORE_DATASETS_VIEWS);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_VIEWS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDsView);
 
     // Application
     DatasetViewApplication datasetViewApplication = new DatasetViewApplication(appContext);
 
     // Attachment
     appManager.attachApplication(datasetViewApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASETS_VIEWS_URL), datasetViewApplication);
 
     // ===========================================================================
     // Gestion des modules de projets
 
     // Store
     SitoolsStore<ProjectModuleModel> storeProjectModules = (SitoolsStore<ProjectModuleModel>) settings.getStores().get(
         Consts.APP_STORE_PROJECTS_MODULES);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_MODULES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeProjectModules);
 
     // Application
     ProjectModuleApplication projectModulesApplication = new ProjectModuleApplication(appContext);
 
     // Attachment
     appManager.attachApplication(projectModulesApplication);
 
     component.getInternalRouter()
         .attach(settings.getString(Consts.APP_PROJECTS_MODULES_URL), projectModulesApplication);
 
     // ===========================================================================
     // Gestion des projets
 
     // Store
     SitoolsStore<Project> storePrj = (SitoolsStore<Project>) settings.getStores().get(Consts.APP_STORE_PROJECT);
 
     clientUserApp.setStore(storePrj);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storePrj);
 
     // gestion des graphs
     SitoolsStore<Graph> storeGraph = (SitoolsStore<Graph>) settings.getStores().get(Consts.APP_STORE_GRAPH);
     appContext.getAttributes().put(Consts.APP_STORE_GRAPH, storeGraph);
 
     // Dependence clientUserApp > storePrj
     clientUserApp.setStore(storePrj);
 
     // Application
     ProjectAdministration projectApplication = new ProjectAdministration(host, appContext);
 
     // Attachment
     appManager.attachApplication(projectApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_PROJECTS_URL), projectApplication);
 
     // ===========================================================================
     // Gestion des formulaires
 
     // Store
     SitoolsStore<Form> storeForm = (SitoolsStore<Form>) settings.getStores().get(Consts.APP_STORE_FORM);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
         + settings.getString(Consts.APP_FORMS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeForm);
 
     // Application
     FormApplication formApplication = new FormApplication(appContext);
 
     // Attachment - recouvrement url du dataset administration
     appManager.attachApplication(formApplication);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}" + settings.getString(Consts.APP_FORMS_URL),
         formApplication);
 
     // ===========================================================================
     // Gestion des flux
 
     // Store
     SitoolsStore<FeedModel> storeFeeds = (SitoolsStore<FeedModel>) settings.getStores().get(Consts.APP_STORE_FEED);
 
     // attachment for Projects
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PROJECTS_URL) + "/{dataId}"
         + settings.getString(Consts.APP_FEEDS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFeeds);
 
     // Application
     FeedsApplication feedsProjectsApp = new FeedsApplication(appContext, storeFeeds) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.ADMIN);
         setName("FeedsProjectApplication");
         setDescription("Feeds administration for projects");
       }
     };
 
     // Attachment
     appManager.attachApplication(feedsProjectsApp);
 
     // attachment for DataSets
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{dataId}"
         + settings.getString(Consts.APP_FEEDS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFeeds);
 
     // Application
     FeedsApplication feedsDataSetsApp = new FeedsApplication(appContext, storeFeeds) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.ADMIN);
         setName("FeedsDataSetsApplication");
         setDescription("Feeds administration for datasets");
       }
     };
 
     // Attachment
     appManager.attachApplication(feedsDataSetsApp);
 
     // attachment for Portal
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PORTAL_URL) + "/{dataId}"
         + settings.getString(Consts.APP_FEEDS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFeeds);
 
     // Application
     FeedsApplication feedsPortalApp = new FeedsApplication(appContext, storeFeeds) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.ADMIN);
         setName("FeedsPortalApplication");
         setDescription("Feeds administration for portal");
       }
     };
 
     // Attachment
     appManager.attachApplication(feedsPortalApp);
 
     // Internal attachment for Feeds notifications
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, false);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeFeeds);
 
     // Application
     FeedsApplication feedsInternalApp = new FeedsApplication(appContext, storeFeeds) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.SYSTEM);
         setName("FeedsInternalApplication");
         setDescription("Feeds internal application to handle notifications on feeds");
       }
     };
 
     // feedsInternalApp.start();
 
     // RIAP
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_FEEDS_OBJECT_URL) + "/{dataId}" + settings.getString(Consts.APP_FEEDS_URL),
         feedsInternalApp);
 
     // Also attach it with the Portal Url RIAP
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_PORTAL_URL) + "/{dataId}" + settings.getString(Consts.APP_FEEDS_URL),
         feedsInternalApp);
 
     // ===========================================================================
     // Gestion des recherches opensearch
 
     // Store
     SitoolsStore<Opensearch> storeOS = (SitoolsStore<Opensearch>) settings.getStores().get(Consts.APP_STORE_OPENSEARCH);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}"
         + settings.getString(Consts.APP_OPENSEARCH_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeOS);
     appContext.getAttributes().put("APP_STORE_FEEDS", storeFeeds);
 
     // Application
     OpenSearchApplication opensearchApp = new OpenSearchApplication(appContext);
 
     // Attachment
     appManager.attachApplication(opensearchApp);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{datasetId}" + settings.getString(Consts.APP_OPENSEARCH_URL),
         opensearchApp);
 
     // ===========================================================================
     // Documentation projet
 
     // Directory
     String documentationAppPath = appPath + settings.getString(Consts.APP_DOCUMENTATION_PATH);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DOCUMENTATION_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     StaticWebApplication documentationApp = new StaticWebApplication(appContext, documentationAppPath, baseRef
         + appReference) {
       @Override
       public void sitoolsDescribe() {
         setName("Documentation");
         setDescription("Diffusion de la documentation associée au projet");
       }
     };
 
     // Attachment
     appManager.attachApplication(documentationApp);
 
     // ============================
     // SOLR Integration
 
     // -----------------------------------------------------------
     // Client Solr initialisation
 
     String directory = settings.getStoreDIR(Consts.APP_SOLR_STORE_DIR) + "/config";
     String configFile = "solr.xml";
 
     String directoryTemplate = settings.getStoreDIR(Consts.APP_SOLR_STORE_DIR) + "/template";
 
     Client solrClient = component.getClients().add(SolrClientHelper.SOLR_PROTOCOL);
 
     solrClient.getContext().getParameters().add("directory", directory);
     solrClient.getContext().getParameters().add("configFile", new File(directory, configFile).getAbsolutePath());
 
     // -----------------------------------------------------------
     // Solr HTTP via SolrApplication
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_SOLR_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put("SOLR_DIRECTORY", directory);
     appContext.getAttributes().put("SOLR_TEMPLATE_DIRECTORY", directoryTemplate);
 
     // Application
     SolrApplication solrApp = new SolrApplication(appContext);
 
     // Attachment
     appManager.attachApplication(solrApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_SOLR_URL), solrApp);
 
     // ===========================================================================
     // Gestion des commandes Administration
 
     // Store
     SitoolsStore<Order> storeOrd = (SitoolsStore<Order>) settings.getStores().get(Consts.APP_STORE_ORDER);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_ORDERS_ADMIN_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeOrd);
 
     // Application
     OrderAdministration orderAdminApplication = new OrderAdministration(appContext);
 
     // Attachment
     appManager.attachApplication(orderAdminApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_ORDERS_ADMIN_URL), orderAdminApplication);
 
     // ===========================================================================
     // Poster des commandes Utilisateurs
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_ORDERS_USER_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeOrd);
     
     // Application
     UserOrderApplication userOrderApplication = new UserOrderApplication(appContext);
     // Attachment
     appManager.attachApplication(userOrderApplication);
     component.getInternalRouter().attach(settings.getString(Consts.APP_ORDERS_USER_URL), userOrderApplication);
 
     
     // ===========================================================================
     // Administration des espaces de stockage
 
     // Store
     UserStorageStore storeUS = (UserStorageStore) settings.getStores().get(Consts.APP_STORE_USERSTORAGE);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_USERSTORAGE_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put("USER_STORAGE_ROOT", settings.getVariableStoreDIR(Consts.USERSTORAGE_ROOT));
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeUS);
 
     // Application
     UserStorageManagement userStorageManagement = new UserStorageManagement(appContext);
 
     // Attachment
     appManager.attachApplication(userStorageManagement);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_USERSTORAGE_URL), userStorageManagement);
 
     // ===========================================================================
     // Exposition des espaces de stockage utilisateurs >> UserStorageApplication
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_USERSTORAGE_USER_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeUS);
     appContext.getAttributes().put("USER_STORAGE_ROOT", settings.getVariableStoreDIR(Consts.USERSTORAGE_ROOT));
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     // Application
     UserStorageApplication userStorageApplication = new UserStorageApplication(appContext);
 
     // Attachment
     appManager.attachApplication(userStorageApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_USERSTORAGE_USER_URL), userStorageApplication);
 
     // ===========================================================================
     // Exposition des plugins de convertisseurs sur un dataset
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_CONVERTERS_PLUGINS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     ConverterPluginsApplication converterPluginsApp = new ConverterPluginsApplication(appContext);
 
     // Attachment
     appManager.attachApplication(converterPluginsApp);
 
     // ===========================================================================
     // Exposition des plugins de filtres de recherche sur un dataset
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_FILTERS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     FilterPluginsApplication datasetFilterPluginsApp = new FilterPluginsApplication(appContext);
 
     // Attachment
     appManager.attachApplication(datasetFilterPluginsApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASETS_FILTERS_PLUGINS_URL),
         datasetFilterPluginsApp);
 
     // ===========================================================================
     // Exposition des classes de plugins de resources
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PLUGINS_RESOURCES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     ResourceClassPluginApplication resourcePluginsApp = new ResourceClassPluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(resourcePluginsApp);
 
     // ===========================================================================
     // Exposition des classes de plugins de filtres
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_PLUGINS_FILTERS_CLASSES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     FilterClassPluginApplication filterClassPluginsApp = new FilterClassPluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(filterClassPluginsApp);
 
     // ===========================================================================
     // Application Data Storage
 
     // Store
     DataStorageStore storeDataStorage = (DataStorageStore) settings.getStores().get(Consts.APP_STORE_DATASTORAGE);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASTORAGE_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDataStorage);
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     // Application
     StorageApplication storageApplication = new StorageApplication(appContext);
 
     // Attachment
     appManager.attachApplication(storageApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASTORAGE_URL), storageApplication);
 
     // ===========================================================================
     // Application Data Storage Administration
 
     // Store
     // Idem
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASTORAGE_ADMIN_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDataStorage);
 
     appContext.getAttributes().put("DATASTORAGE_APPLICATION", storageApplication);
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     // Application
     StorageAdministration storageAdminApplication = new StorageAdministration(appContext);
 
     // Attachment
     appManager.attachApplication(storageAdminApplication);
 
     // Internal router attachment
     component.getInternalRouter().attach(settings.getString(Consts.APP_DATASTORAGE_ADMIN_URL), storageAdminApplication);
 
     // ============================
     // MAIL INTERNAL APPLICATION
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_MAIL_ADMIN_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     MailAdministration mailAdministration = new MailAdministration(appContext, component);
 
     // Attachment
     appManager.attachApplication(mailAdministration);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_MAIL_ADMIN_URL), mailAdministration);
 
     // ===========================================================================
     // Gestion des uploads
 
     // Directory
     String uploadAppDIR = settings.getStoreDIR(Consts.APP_UPLOAD_DIR);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_UPLOAD_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     UploadApplication uploadApp = new UploadApplication(appContext, uploadAppDIR);
 
     appManager.attachApplication(uploadApp);
 
     // ===========================================================================
     // Gestion du dossier temporaire
 
     // Directory
     String tempAppDIR = settings.getStoreDIR(Consts.APP_TMP_FOLDER_DIR);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_TMP_FOLDER_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     TemporaryFolderApplication tmpFolderApp = new TemporaryFolderApplication(appContext, tempAppDIR);
 
     appManager.attachApplication(tmpFolderApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_TMP_FOLDER_URL), tmpFolderApp);
 
     // ===========================================================================
     // Gestion d'un dossier pour l'administrateur
 
     // Directory
     String adminAppDIR = settings.getStoreDIR(Consts.ADMINSTORAGE_ORDERS_DIR);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_ADMINSTORAGE_ORDERS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     OrdersFilesApplication adminFolderApp = new OrdersFilesApplication(appContext, adminAppDIR);
 
     appManager.attachApplication(adminFolderApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_ADMINSTORAGE_ORDERS_URL), adminFolderApp);
 
     // =========================
     // ADMINISTRATOR APPLICATION
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_ADMINISTRATOR_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.JETTY_PROPERTIES, serverHTTP.getContext().getParameters());
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     // Application
     AdministratorApplication adminApplication = new AdministratorApplication(host, appContext);
 
     // Attachment
     appManager.attachApplication(adminApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_ADMINISTRATOR_URL), adminApplication);
 
     // ===========================================================================
     // Liste des taches pour un utilisateur donnee
 
     final String identifier = "identifier";
 
     // Store
     SitoolsStore<TaskModel> taskStore = (SitoolsStore<TaskModel>) settings.getStores().get(Consts.APP_STORE_TASK);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_USERRESOURCE_ROOT_URL) + "/{" + identifier + "}"
         + settings.getString(Consts.APP_TASK_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, taskStore);
     appContext.getAttributes().put(Consts.APP_STORE_PLUGINS_RESOURCES, resPlugStore);
 
     // To authorize cookie authentication for this application
     appContext.getAttributes().put(ContextAttributes.COOKIE_AUTHENTICATION, Boolean.TRUE);
 
     TaskApplication taskAppUser = new TaskApplication(appContext) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.USER);
         this.setName("TaskApplicationForUser");
         this.setDescription("Exposes the list of tasks for a particular user,"
             + "Only a particular User can access its tasks, he must have GET, PUT and DELETE rights on this application");
       }
 
       // On ajoute un authorizer spécifique pour qu'un user puisse avoir que ses taches.
       @Override
       public Restlet getSecure() {
         // GET, PUT and DELETE authorized for public tasks on this application
         // it means that public user can retrieve, edit or delete every public tasks, but only public tasks
         List<Method> methods = new ArrayList<Method>();
         methods.add(Method.GET);
         methods.add(Method.PUT);
         methods.add(Method.DELETE);
         methods.add(Method.OPTIONS);
         return addSecurity(this, identifier, methods);
       }
     };
 
     appManager.attachApplication(taskAppUser);
 
     // ===========================================================================
     // Liste de toutes les taches, peut être utilisé par l'administrateur
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_TASK_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, taskStore);
     appContext.getAttributes().put(Consts.APP_STORE_PLUGINS_RESOURCES, resPlugStore);
 
     TaskApplication taskAppAdmin = new TaskApplication(appContext) {
       @Override
       public void sitoolsDescribe() {
         setCategory(Category.ADMIN);
         this.setName("TaskApplicationForAdmin");
         this.setDescription("Exposes the list of all tasks");
       }
     };
 
     appManager.attachApplication(taskAppAdmin);
    
    component.getInternalRouter().attach(settings.getString(Consts.APP_TASK_URL), taskAppAdmin);

 
     // ==========================================
     // JAVAX MEASURE
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_UNITS_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
 
     // Application
     UnitsApplication unitsApp = new UnitsApplication(appContext);
 
     appManager.attachApplication(unitsApp);
 
     // ==========================================
     // Dimension Management
 
     // Store
     SitoolsStore<SitoolsDimension> storeDimension = (SitoolsStore<SitoolsDimension>) settings.getStores().get(
         Consts.APP_STORE_DIMENSION);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DIMENSIONS_ADMIN_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeDimension);
 
     // Application
     DimensionAdministration dimAdminApp = new DimensionAdministration(appContext);
 
     appManager.attachApplication(dimAdminApp);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_DIMENSIONS_ADMIN_URL), dimAdminApp);
 
     // ===========================================================================
     // Gestion des services IHM
 
     // Store
     SitoolsStore<GuiServiceModel> storeGuiService = (SitoolsStore<GuiServiceModel>) settings.getStores().get(
         Consts.APP_STORE_GUI_SERVICE);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_GUI_SERVICES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeGuiService);
 
     // Application
     GuiServiceApplication guiServiceApplication = new GuiServiceApplication(appContext);
 
     // Attachment
     appManager.attachApplication(guiServiceApplication);
 
     component.getInternalRouter().attach(settings.getString(Consts.APP_GUI_SERVICES_URL), guiServiceApplication);
 
     // ===========================================================================
     // Gestion des services IHM sur un dataset
 
     // Store
     SitoolsStore<GuiServicePluginModel> storeGuiPluginService = (SitoolsStore<GuiServicePluginModel>) settings
         .getStores().get(Consts.APP_STORE_GUI_SERVICES_PLUGIN);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{parentId}"
         + settings.getString(Consts.APP_GUI_SERVICES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeGuiPluginService);
 
     // Application
     GuiServicePluginApplication guiServicePluginApplication = new GuiServicePluginApplication(appContext);
 
     // Attachment
     appManager.attachApplication(guiServicePluginApplication);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{parentId}" + settings.getString(Consts.APP_GUI_SERVICES_URL),
         guiServicePluginApplication);
 
     // ===========================================================================
     // Gestion des services sur un dataset
 
     // Store
     SitoolsStore<ServiceCollectionModel> storeServiceCollection = (SitoolsStore<ServiceCollectionModel>) settings
         .getStores().get(Consts.APP_STORE_SERVICES);
 
     // Reference
     appReference = baseUrl + settings.getString(Consts.APP_DATASETS_URL) + "/{parentId}"
         + settings.getString(Consts.APP_SERVICES_URL);
 
     // Context
     appContext = host.getContext().createChildContext();
     appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
     appContext.getAttributes().put(ContextAttributes.APP_REGISTER, true);
     appContext.getAttributes().put(ContextAttributes.APP_STORE, storeServiceCollection);
 
     // Application
     ServiceApplication servicesApplication = new ServiceApplication(appContext);
 
     // Attachment
     appManager.attachApplication(servicesApplication);
 
     component.getInternalRouter().attach(
         settings.getString(Consts.APP_DATASETS_URL) + "/{parentId}" + settings.getString(Consts.APP_SERVICES_URL),
         servicesApplication);
 
     // Attachement of the appManager to have the security configured properly
     appManager.attachApplication(appManager);
 
     // ===========================================================================
     // Liste des resources triggers internes
 
     notificationApplication.attachTrigger("USER_CREATED", UserTrigger.class);
     notificationApplication.attachTrigger("USER_UPDATED", UserTrigger.class);
     notificationApplication.attachTrigger("USER_DELETED", UserTrigger.class);
     notificationApplication.attachTrigger("GROUP_CREATED", GroupTrigger.class);
     notificationApplication.attachTrigger("GROUP_UPDATED", GroupTrigger.class);
     notificationApplication.attachTrigger("GROUP_DELETED", GroupTrigger.class);
     notificationApplication.attachTrigger("ROLE_DELETED", RoleTrigger.class);
     notificationApplication.attachTrigger("ROLE_GROUPS_UPDATED", RoleTrigger.class);
     notificationApplication.attachTrigger("ROLE_USERS_UPDATED", RoleTrigger.class);
     notificationApplication.attachTrigger("AUTHORIZATION_CREATED", AuthorizationTrigger.class);
     notificationApplication.attachTrigger("AUTHORIZATION_UPDATED", AuthorizationTrigger.class);
     notificationApplication.attachTrigger("AUTHORIZATION_DELETED", AuthorizationTrigger.class);
     notificationApplication.attachTrigger("STORAGE_DELETED", DataStorageTrigger.class);
     notificationApplication.attachTrigger("DATASET_CREATED", DefaultGuiServicesTrigger.class);
 
 
     // ============================
     // START SERVER
 
     component.getHosts().add(host);
 
     component.start();
     
     serverHTTP.getContext().getAttributes().get("org.restlet.engine.helper");
     
     server = component;
   }
 
   private static void startServerFailed(SitoolsSettings settings, Component component, Exception e)
       throws Exception {
     // If there is an error while creating the stores, we attach an error page to the server and stop the starting
     // process
     final Exception sitoolsException = e;
     // create the virtual host
     Context vhostContext = component.getContext().createChildContext();
     VirtualHost host = Starter.initVirtualHost(vhostContext, settings);
     // SERVER STATUS SERVICE
     String baseUrl = settings.getString(Consts.APP_URL);
     Starter.initStatusService(settings, baseUrl, component);
     // attach a new Restlet to it
     host.attach(new Restlet() {
       @Override
       public void handle(Request request, Response response) {
         throw new ResourceException(sitoolsException);
       }
     });
     // ============================
     // START SERVER
     component.getHosts().add(host);
     component.start();
     server = component;
     return;
   }
 
   /**
    * STOP server
    */
   public static void stop() {
     if (server != null) {
       try {
         server.stop();
       }
       catch (Exception e) {
         e.printStackTrace();
       }
     }
     server = null;
   }
 
   /**
    * Initialize the status service on the given component
    * 
    * @param settings
    *          The SitoolsSettings object
    * @param baseUrl
    *          the baseUrl
    * @param component
    *          the component in which to add the status service
    * @throws Exception
    *           if something is wrong
    */
   public static void initStatusService(SitoolsSettings settings, String baseUrl, Component component) throws Exception {
     // ============================
     // SERVER STATUS SERVICE
 
     // settings.getString("Starter.StatusService.enable");
     SitoolsStatusService statusServ = new SitoolsStatusService(true);
     statusServ.setOverwriting(true);
     statusServ.setContactEmail(settings.getString("Starter.StatusService.CONTACT_MAIL"));
     String homeRef = settings.getString("Starter.StatusService.HOME_REF");
     if ((homeRef != null) && !(homeRef.equals(""))) {
       statusServ.setHomeRef(new Reference(baseUrl + homeRef));
     }
 
     String statusTemplatePath = settings.getRootDirectory() + settings.getString(Consts.TEMPLATE_DIR)
         + settings.getString("Starter.StatusService.TEMPLATE");
     File templateFile = new File(statusTemplatePath);
     if (templateFile == null || !templateFile.exists()) {
       component.getLogger().severe("Status template file not found :" + statusTemplatePath);
     }
 
     statusServ.setTemplate(statusTemplatePath);
     statusServ.setContext(component.getContext());
     statusServ.start();
 
     component.setStatusService(statusServ);
   }
 
   /**
    * Initialize the virtual host
    * 
    * @param vhostContext
    *          the context with which to create the host
    * @param settings
    *          the SitoolsSettings object
    * @return VirtualHost the VirtualHost created
    */
   public static VirtualHost initVirtualHost(Context vhostContext, SitoolsSettings settings) {
     vhostContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
     VirtualHost host = new VirtualHost(vhostContext);
     host.setName("SitoolsVirtualHost");
     host.setHostDomain(settings.getString("Starter.HOST_DOMAIN"));
     return host;
   }
 
 }
