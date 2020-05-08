 package org.iplantc.core.uicommons.client.models;
 
 import java.util.Map;
 
 public class DEProperties {
     /**
      * The prefix used in each of the property names.
      */
     private static final String PROPERTY_NAME_PREFIX = "org.iplantc.discoveryenvironment."; //$NON-NLS-1$
 
     /**
      * The base URL used to access the Mule services.
      */
     private static final String MULE_SERVICE_BASE_URL = PROPERTY_NAME_PREFIX + "muleServiceBaseUrl"; //$NON-NLS-1$
 
     /**
      * The base URL used to access the Mule services.
      */
     private static final String UNPROTECTED_MULE_SERVICE_BASE_URL = PROPERTY_NAME_PREFIX
             + "unprotectedMuleServiceBaseUrl"; //$NON-NLS-1$
 
     /**
      * Properties key used to indicate if server-push messaging is enabled. (under development)
      */
     private static final String SERVER_PUSH_ENABLED = PROPERTY_NAME_PREFIX + "serverPushEnabled"; //$NON-NLS-1$
 
     /**
      * Properties key of the base URL of the data management services.
      */
     private static final String DATA_MGMT_BASE_URL = "org.iplantc.services.de-data-mgmt.base"; //$NON-NLS-1$
 
     /**
      * Properties key of the notification polling interval
      */
     private static final String NOTIFICATION_POLL_INTERVAL = "org.iplantc.discoveryenvironment.notifications.poll-interval"; //$NON-NLS-1$
 
     /**
      * Properties key of the context click enabled option
      */
 
     private static final String CONTEXT_CLICK_ENABLED = "org.iplantc.discoveryenvironment.contextMenu.enabled";//$NON-NLS-1$
 
     /**
      * The prefix used in each of the private workspace property names.
      */
     private static final String WORKSPACE_PREFIX = "org.iplantc.discoveryenvironment.workspace."; //$NON-NLS-1$
 
     /**
      * Properties key for the private workspace
      */
     private static final String PRIVATE_WORKSPACE = WORKSPACE_PREFIX + "rootAnalysisGroup"; //$NON-NLS-1$
 
     /**
      * Properties key for the private workspace items
      */
     private static final String PRIVATE_WORKSPACE_ITEMS = WORKSPACE_PREFIX + "defaultAnalysisGroups"; //$NON-NLS-1$
 
     /**
      * Properties key for the default Beta Category ID
      */
     private static final String DEFAULT_BETA_CATEGORY_ID = WORKSPACE_PREFIX
             + "defaultBetaAnalysisGroupId"; //$NON-NLS-1$
 
     /**
      * Properties key for the default output folder name
      */
     private static final String DEFAULT_OUTPUT_FOLDER_NAME = WORKSPACE_PREFIX
             + "defaultOutputFolderName"; //$NON-NLS-1$
 
     /**
      * The prefix used for each of the keepalive configuration parameters.
      */
     private static final String KEEPALIVE_PREFIX = PROPERTY_NAME_PREFIX + "keepalive.";
 
     /**
      * The URL that we use for keepalive requests.
      */
     private static final String KEEPALIVE_TARGET = KEEPALIVE_PREFIX + "target";
 
     /**
      * The number of minutes between keepalive requests.
      */
     private static final String KEEPALIVE_INTERVAL = KEEPALIVE_PREFIX + "interval";
 
     /**
      * The single instance of this class.
      */
     private static DEProperties instance;
 
     /**
      * The base URL of the data management services.
      */
     private String dataMgmtBaseUrl;
 
     /**
      * The polling interval
      */
     private int notificationPollInterval;
 
     /**
      * Context click option
      */
     private boolean contextClickEnabled;
 
     /**
      * private workspace name
      */
     private String privateWorkspace;
 
     /**
      * private workspace items
      * 
      */
     private String privateWorkspaceItems;
 
     /**
      * ID of the default Beta Workspace Category
      */
     private String defaultBetaCategoryId;
 
     /**
      * 
      * Default output folder name
      * 
      */
    private String defaultOutputFolderName;
 
     /**
      * @return the contextClickEnabled
      */
     public boolean isContextClickEnabled() {
         return contextClickEnabled;
     }
 
     /**
      * The base URL used to access the DE Mule services.
      */
     private String muleServiceBaseUrl;
 
     /**
      * The base URL used to access the DE Unprotected Mule services.
      */
     private String unproctedMuleServiceBaseUrl;
 
     /**
      * @return the unproctedMuleServiceBaseUrl
      */
     public String getUnproctedMuleServiceBaseUrl() {
         return unproctedMuleServiceBaseUrl;
     }
 
     /**
      * Indicates if the server-push messaging heartbeat is enabled. (under development)
      */
     private boolean serverPushEnabled;
 
     /**
      * The target URL that we use for keepalive requests.
      */
     private String keepaliveTarget;
 
     /**
      * The number of minutes between keepalive requests.
      */
     private int keepaliveInterval;
 
     /**
      * Force the constructor to be private.
      */
     private DEProperties() {
     }
 
     /**
      * Gets the single instance of this class.
      * 
      * @return the instance.
      */
     public static DEProperties getInstance() {
         if (instance == null) {
             instance = new DEProperties();
         }
         return instance;
     }
 
     /**
      * Initializes this class from the given set of properties.
      * 
      * @param properties the properties that were fetched from the server.
      */
     public void initialize(Map<String, String> properties) {
         dataMgmtBaseUrl = properties.get(DATA_MGMT_BASE_URL);
         muleServiceBaseUrl = properties.get(MULE_SERVICE_BASE_URL);
         unproctedMuleServiceBaseUrl = properties.get(UNPROTECTED_MULE_SERVICE_BASE_URL);
         serverPushEnabled = Boolean.parseBoolean(properties.get(SERVER_PUSH_ENABLED));
         privateWorkspace = properties.get(PRIVATE_WORKSPACE);
         privateWorkspaceItems = properties.get(PRIVATE_WORKSPACE_ITEMS);
         defaultBetaCategoryId = properties.get(DEFAULT_BETA_CATEGORY_ID);
        defaultOutputFolderName = properties.get(DEFAULT_OUTPUT_FOLDER_NAME);
         contextClickEnabled = getBoolean(properties, CONTEXT_CLICK_ENABLED, false);
         notificationPollInterval = getInt(properties, NOTIFICATION_POLL_INTERVAL, 60);
         keepaliveTarget = properties.get(KEEPALIVE_TARGET);
         keepaliveInterval = getInt(properties, KEEPALIVE_INTERVAL, -1);
     }
 
     /**
      * Obtains a boolean property value.
      * 
      * @param properties the property map.
      * @param name the name of the property.
      * @param defaultValue the default value to use.
      * @return the property value or its default value.
      */
     private boolean getBoolean(Map<String, String> properties, String name, boolean defaultValue) {
         try {
             return Boolean.parseBoolean(properties.get(name));
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
     /**
      * Obtains an integer property value.
      * 
      * @param properties the property map.
      * @param name the name of the property.
      * @param defaultValue the default value to use.
      * @return the property value or its default value.
      */
     private int getInt(Map<String, String> properties, String name, int defaultValue) {
         try {
             return Integer.parseInt(properties.get(name));
         } catch (Exception e) {
             return defaultValue;
         }
     }
 
     /**
      * Gets the polling interval for the MessagePoller of the Notification agent service.
      * 
      * @return the poll interval in seconds as an int.
      */
     public int getNotificationPollInterval() {
         return notificationPollInterval;
     }
 
     /**
      * Gets the base URL of the data management services.
      * 
      * @return the URL as a string.
      */
     public String getDataMgmtBaseUrl() {
         return dataMgmtBaseUrl;
     }
 
     /**
      * Gets the base URL used to access the DE Mule services.
      * 
      * @return the URL as a string.
      */
     public String getMuleServiceBaseUrl() {
         return muleServiceBaseUrl;
     }
 
     /**
      * Gets a boolean indicating if server-push messaging is enabled.
      * 
      * @return true, server-push messaging is enabled; otherwise, false.
      */
     public boolean isServerPushEnabled() {
         return serverPushEnabled;
     }
 
     /**
      * @return the privateWorkspace
      */
     public String getPrivateWorkspace() {
         return privateWorkspace;
     }
 
     /**
      * @return the privateWorkspaceItems
      */
     public String getPrivateWorkspaceItems() {
         return privateWorkspaceItems;
     }
 
     /**
      * @return the unique ID for the Beta category.
      */
     public String getDefaultBetaCategoryId() {
         return defaultBetaCategoryId;
     }
 
     /**
     * @param defaultOutputFolderName the defaultOutputFolderName to set
      */
    public void setDefaultOutputFolderName(String defaultOutputFolderName) {
        this.defaultOutputFolderName = defaultOutputFolderName;
     }
 
     /**
     * @return the defaultOutputFolderName
      */
    public String getDefaultOutputFolderName() {
        return defaultOutputFolderName;
     }
 
     /**
      * @return the URL that we use for keepalive requests.
      */
     public String getKeepaliveTarget() {
         return keepaliveTarget;
     }
 
     /**
      * @return the number of minutes between keepalive requests.
      */
     public int getKeepaliveInterval() {
         return keepaliveInterval;
     }
 }
