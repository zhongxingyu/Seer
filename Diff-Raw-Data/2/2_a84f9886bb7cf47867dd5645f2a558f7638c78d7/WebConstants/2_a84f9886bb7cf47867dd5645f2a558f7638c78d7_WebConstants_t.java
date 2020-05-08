 package test.webui.resources;
 
 
 public final class WebConstants {
 	
 	public final static String RED = "#ed1c24";
 	public final static String RED_ORANGE = "#f26522";
 	public final static String YELLOW_ORANGE = "#f7941d";
 	public final static String YELLOW = "#fff200";
 	public final static String PEA_GREEN = "#8dc73f";
 	public final static String YELLOW_GREEN = "#39b54a";
 	public final static String GREEN = "#00a651";
 	public final static String GREEN_CYAN = "#00a99d";
 	public final static String CYAN = "#00aeef";
 	public final static String CYAN_BLUE = "#0072bc";
 	public final static String BLUE = "#0054a6";
 	public final static String BLUE_VIOLET = "#2e3192";
 	public final static String VIOLET = "#662d91";
 	public final static String VIOLET_MAGENTA = "#92278f";
 	public final static String MAGENTA = "#ec008c";
 	public final static String MAGENTA_RED = "#ed145b";
 	public final static String GREY = "#666666";
 	public static final String CHROME = "Chrome";
 	public static final String FIREFOX = "Firefox";
 	
 	public static final class ID {
 		public static final String topologyPanel = "gs-tab-item-topology";
 		public static final String logOutButton = "gs-logout-button";
 		public static final String usernameInput = "user_name-input";
 		public static final String passwordInput = "password-input";
 		public static final String numberOfInstInput = "num_of_instances-input";
 		public static final String maxInsPerVmInput = "max_instances_per_vm-input";
 		public static final String maxInstPerMachineInput = "max_inst_per_machine-input";
 		public static final String dataGridNameInput = "data_grid_name-input";
 		public static final String deployEDGOption = "deploy_edg_menu_item";
 		public static final String deployMemcachedOption = "deploy_memcached_menu_item";
 		public static final String deployProcessingUnitOption = "deploy_processing_unit_menu_item";
 		public static final String usernameLogginInput = "username-input";
 		public static final String passwordLogginInput = "passowrd-input";
 		public static final String jiniGroupInput = "jini_group-input";
 		public static final String locatorsInput = "locators-input";
 		public static final String puTreeGrid = "pu_tree_grid";
 		public static final String undeployPuButton = "undeploy_processing_unit";
 		public static final String startGSC = "start_gsc_menu_item";
 		public static final String startGSM = "start_gsm_menu_item";
 		public static final String startLUS = "start_lookup_menu_item";
 		public static final String spaceUrlInput = "space_url-input";
 		public static final String terminateComponent = "terminate_gsa_agent_menu_item";
 		public static final String restartComponent = "restart_gsa_agent_menu_item";
 		public static final String restartPuInstance = "restart_pu_instance_menu_item";
 		public static final String alertsGridBody = "gs-alerts-grid-body";
 		public static final String resourceDistribHosts = "resource_distrib-grid_null_hosts";
 		public static final String resourceDistribGSA = "resource_distrib-grid_null_gsa";
 		public static final String resourceDistribGSM = "resource_distrib-grid_null_gsm";
 		public static final String resourceDistribGSC = "resource_distrib-grid_null_gsc";
 		public static final String resourceDistribLUS = "resource_distrib-grid_null_lus";
 		public static final String moduleWeb = "modules-status-grid_null_web";
 		public static final String moduleStateful = "modules-status-grid_null_stateful";
 		public static final String moduleStateless = "modules-status-grid_null_stateless";
 		public static final String moduleMirror = "modules-status-grid_null_mirror";
 		public static final String moduleCustom = "modules-status-grid_null_custom";
 		
 		public static final String moduleLoadBalancer    = "modules-status-grid_null_loadBalancer";
 		public static final String moduleWebServer       = "modules-status-grid_null_webServer";
 		public static final String moduleSecurityServer  = "modules-status-grid_null_securityServer";
 		public static final String moduleAppServer       = "modules-status-grid_null_appServer";
 		public static final String moduleEsbServer       = "modules-status-grid_null_esbServer";
 		public static final String moduleMessageBus      = "modules-status-grid_null_messageBus";
 		public static final String moduleDatabase        = "modules-status-grid_null_database";
 		public static final String moduleNoSqlDb         = "modules-status-grid_null_noSqlDb";
 		
 		public static final String logsPanel = "gs-tab-item-logs";
 
 		public static final String habytesPerSecond = "high-avail-grid_null_sendBytesPerSec";
 		public static final String hapacketsPerSecond = "high-avail-grid_null_sendPacketsPerSec";
 		public static final String edsBytesPerSecond = "eds-grid_null_sendBytesPerSec";
 		public static final String edsPacketsPerSecond = "eds-grid_null_sendPacketsPerSec";
 		public static final String edsOpPerSecond = "eds-grid_null_successfulOperationsPerSec";
 		public static final String criticalIcon = "gs-status-icon-critical";
 		public static final String okIcon = "gs-status-icon-ok";
 		public static final String warningIcon = "gs-status-icon-alert";
 		public static final String naIcon = "gs-status-icon-not-initialized";
 		public static final String healthGaugePointer = "health-gauge-pointer";
 		public static final String applicationsMenuPanel = "gs-applications-menu-panel";
 //		public static final String applicationMap = "app-map-canvas";
 		public static final String applicationMap = "graph-canvas";
 		public static final String nodeStatusOk = "node-status-ok";
 		public static final String statusGrid = "gs-window-grid-status";
 		public static final String nodeStatusWarning = "node-status-warn";
 		public static final String nodeStatusBroken = "node-status-critical";
 //		public static final String TopologySubPanel = "topology_bottom_tab_panel";	// not found in DOM
 		public static final String nodePath = "node-shape-path-";
 		public static final String detailsPanel = "gs-side-panel-details";
 		public static final String healthPanelToggle = "gs-tab-item-health-toggler-button";
 		public static final String physicalPanelToggle = "gs-tab-item-physical-toggler-button";
 		public static final String logsPanelToggle = "gs-tab-item-logs-toggler-button";
 		public static final String logicalPanelToggle = "gs-tab-item-logical-toggler-button";
 		public static final String healthPanel = "gs-sub-panel-health";
 		public static final String physicalPanel = "gs-tab-item-physical";
 //		public static final String topMetric = "metricPanelTop";	// not found in DOM
 //		public static final String bottomMetric = "metricPanelBottom";	// not found in DOM
 		public static final String spaceInstancesGrid = "gs-space-instances-grid";
 		public static final String spaceConfigurationGrid = "gs-space-info-grid";
 		public static final String spaceTypesGrid = "gs-space-types-grid";
 		public static final String spaceTransactionsGrid = "gs-space-transactions-grid";
 		public static final String sidePanelSpacesTree = "gs-side-panel-space-tree";
 		public static final String goToLogsItemID = "gs-logical-navigate_to_logs";
 		public static final String restartPuInstanceItem = "gs-logical-restart-pu-instance";
 		public static final String topologyCombobox = "gs-topology-application-combo";
 		public static final String logoutButton = "gs-logout-button";
 		public static final String resourceDistribESM = "resource_distrib-grid_null_esm";
 		public static final String comparisonMetricTop = "comparisonMetricPanelTop";
 		public static final String comparisonMetricBottom = "comparisonMetricPanelBottom";
 		
 		public static String getActionToolBoxId(String name) {
 			return "node-tool-ACTIONS-" + name;
 		}
 		public static String getInfoToolBoxId(String name) {
 			return "node-tool-INFO-" + name;
 		}
 		
 		public static String getPuBoxRectId(String puName) {
 			return "gs-color-box-" + puName;
 		}
 		
 		public static String getPuNodeId(String nodeName) {
 			return "gs-services-logs-selection-tree_pu_name_" + nodeName;
 		}
 		public static String getLogsMachineId(String machineName, String puName) {
 			return "gs-services-logs-selection-tree_" + puName + "_" + machineName;
 		}
 		public static String getLogServiceId(String serviceName) {
 			return "gs-services-logs-selection-tree_" + serviceName;
 		}
 		public static String getPuInstanceId(String name) {
 			return "gs-logical-grid_" + name;
 		}
 		
 		public static String getHostId(String name) {
 			return "gs-physical-grid_" + name;
 		}
 		public static String getLogsContianerId(String contianerId, String agentId, 
 				String puName) {
 			return "gs-services-logs-selection-tree_" + puName + "_gsc-" + agentId + "[" + contianerId + "]";
 		}
 	}
 	
 	public static final class Xpath {
 		public static final String pathToTopologyPanel = "//div[@id='" + ID.topologyPanel + "']";
 		public static final String deployMenuButton = "//table[@id='deploy_button']/tbody/tr[2]/td[2]/em";
 		public static final String deployPUButton = "//table[@id='deployButton']/tbody/tr[2]/td[2]/em/button";
 		public static final String closeWindowButton = "//table[@id='deployButton']/tbody/tr[2]/td[2]/em/button";
 		public static final String clusterSchemaCombo = "//div[@id='cluster_schema']/img";
 		public static final String numberOfBackupsInc = "//div[@id='num_of_backups']/span/img[1]";
 		public static final String isSecuredCheckbox = "//div[@id='secured_check_box']/input";
 		public static final String annonymusCheckbox = "//div[@id='anonymousCB']/input";
 		public static final String loginButton = "//table[@id='login_button']";
 		public static final String discoveryLegend = "//legend/div";
 		public static final String dashBoardButton = "//li[@id='gs-tab-item-dashboard-button']/a[2]/em/span/span";
 		public static final String topologyButton = "//li[@id='gs-tab-item-topology-button']/a[2]/em/span/span";
 		public static final String servicesButton = "//li[@id='gs-tab-item-services-button']/a[2]/em/span/span";
 		public static final String consoleButton = "//li[@id='gs-tab-item-console-button']/a[2]/em/span/span";
 		public static final String deoployFromListRadioButton = "//div[@id='pu_list_rb']/input";
 		public static final String existingPuCombo = "//div[@id='existing_list_of_pu']/img";
 		public static final String okAlert = "//button[text()='OK']";
 		public static final String acceptAlert = "//button[text()='Yes']";
 		public static final String dismissAlert = "//button[text()='No']";
 		public static final String pathToAlerts = "//div[@id='gs-alerts-grid-body']";
 		public static final String pathToAlertSeverity = "/table/tbody/tr/td[1]";
 		public static final String pathToAlertType = "/table/tbody/tr/td[2]";
 		public static final String pathToAlertDescription = "/table/tbody/tr/td[3]";
 		public static final String pathToAlertLocation = "/table/tbody/tr/td[4]";
 		public static final String pathToAlertLastUpdated = "/table/tbody/tr/td[5]";
 		public static final String pathToAlertExpansionButton = "/table/tbody/tr/td[1]/div/div/div/img[2]";
 		public static final String pathToHosts = "//div[@id='" + ID.resourceDistribHosts + "']";
 		public static final String pathToGSA = "//div[@id='" + ID.resourceDistribGSA + "']";
 		public static final String pathToGSM = "//div[@id='" + ID.resourceDistribGSM + "']";
 		public static final String pathToGSC = "//div[@id='" + ID.resourceDistribGSC + "']";
 		public static final String pathToLUS = "//div[@id='" + ID.resourceDistribLUS + "']";
 		public static final String pathToESM = "//div[@id='" + ID.resourceDistribESM + "']";
 		public static final String pathToWebModule = "//div[@id='" + ID.moduleWeb + "']";
 		public static final String pathToStatefullModule = "//div[@id='" + ID.moduleStateful + "']";
 		public static final String pathToStatelessModule = "//div[@id='" + ID.moduleStateless + "']";
 		public static final String pathToMirrorModule = "//div[@id='" + ID.moduleMirror + "']";
 		public static final String pathToCustomModule = "//div[@id='" + ID.moduleCustom + "']";
 		
 		public static final String pathToLoadBalancerModule = "//div[@id='" + ID.moduleLoadBalancer + "']";
 		public static final String pathToAppServerModule = "//div[@id='" + ID.moduleAppServer + "']";
 		public static final String pathToWebServerModule = "//div[@id='" + ID.moduleWebServer + "']";
 		public static final String pathToSecurityServerModule = "//div[@id='" + ID.moduleSecurityServer + "']";
 		public static final String pathToEsbServerModule = "//div[@id='" + ID.moduleEsbServer + "']";
 		public static final String pathToMessageBusModule = "//div[@id='" + ID.moduleMessageBus + "']";
 		public static final String pathToDatabaseModule = "//div[@id='" + ID.moduleDatabase + "']";
 		public static final String pathToNoSqlDbModule = "//div[@id='" + ID.moduleNoSqlDb + "']";
 
 		public static final String pathTohaBytesPerSecond = "//div[@id='" + ID.habytesPerSecond + "']";
 		public static final String pathTohaPacketsPerSecond = "//div[@id='" + ID.hapacketsPerSecond + "']";
 		public static final String pathToEDSPacketsPerSecond = "//div[@id='" + ID.edsPacketsPerSecond + "']";
 		public static final String pathToEDSBytesPerSecond = "//div[@id='" + ID.edsBytesPerSecond + "']";
 		public static final String pathToEDSOpPerSecond = "//div[@id='" + ID.edsOpPerSecond + "']";
 		public static final String pathToCpuCoresInGridStatus = "//div[@id='" + ID.statusGrid + "']/div[2]/div/div[2]/div[2]/div/div[1]";
 		public static final String pathToGridHealthInGridStatus = "//div[@id='" + ID.statusGrid + "']/div[2]/div/div/div[2]/div/div[2]/span";
 		public static final String pathToMemoryInGridStatus = "//div[@id='" + ID.statusGrid + "']/div[2]/div/div[2]/div[2]/div/div[3]";
 		public static final String pathToIconInResourcesPanelOfGridStatus = "/div/span";
 		public static final String pathToNumberInResourcesPanelOfGridStatus = "/div[2]/div/table/tbody/tr[1]/td[2]";
 		public static final String pathToNumberInResourceGrid = "/table/tbody/tr/td[3]";
 		public static final String pathToIconInResourceGrid = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToNumberInModulesGrid = "/table/tbody/tr/td[3]";
 		public static final String pathToIconInModulesGrid = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToIconInHighAvGrid = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToCountInHighAvGrid = "/table/tbody/tr/td[2]/div/span";
 		public static final String pathToNumberInEDSGrid = "/table/tbody/tr/td[2]";
 		public static final String pathToIconInEDSGrid = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToAlertIcon = "/table/tbody/tr/td[1]/div/div/div/span[3]/span[2]";
 		public static final String pathToStatusColumnInAlertsGrid = "//div[1]/div/div[3]/div[2]/div[1]/div/div/div[1]/div[1]/div[1]/div[1]/div/div/table/tbody/tr/td[1]/div/span";
 		public static final String pathToApplicationNameInPanel = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToApplicationInstancesInPanel = "/table/tbody/tr/td[2]/div/span";
 		public static final String pathToApplicationMap = "//div[@id='" + ID.applicationMap + "']/svg";
 		public static final String pathToPuTreeGrid = "//div[@id='" + ID.puTreeGrid + "']";
 		public static final String pathToPuName = "/table/tbody/tr/td[1]";
 		public static final String pathToPuType = "/table/tbody/tr/td[3]";
 		public static final String pathToPuPlannedInstances = "/table/tbody/tr/td[4]";
 		public static final String pathToPuActualInstances = "/table/tbody/tr/td[5]";
 		public static final String pathToPuStatus = "/table/tbody/tr/td[2]/div/img";
 		public static final String pathToPuOptionsButton = "/table/tbody/tr/td[1]/div/div/div/span[3]/a/img";
 		public static final String pathToMetricType = "//div/span";
 		public static final String pathToMetricBalance = "div[2]/div/div[2]";
 //		public static final String pathToTopologySubPanel = "//div[@id='" + ID.TopologySubPanel + "']";
 		public static final String pathToApplicationComboBox = "";
 		public static final String pathToDetailsPanel = "//div[@id='" + ID.detailsPanel + "']";
 		public static final String pathToMetricSelection = "//li[1]/img";
 		public static final String pathToServiceDetails = "//li[2]/img";
 		public static final String pathToComparisonCharts = "//li[3]/img";
 		public static final String pathToAttributeName = "/table/tbody/tr/td[1]/div/span";
 		public static final String pathToAttributeValue = "/table/tbody/tr/td[2]/div/span";
 		public static final String pathToApplicationMenuButtonSelection = "/div/div/div/div/table/tbody/tr/td/div/img";
 		public static final String pathToGroupName = "/div[1]/div/span";
 		public static final String pathToTopologySubPanelName = "//div/label";
 		public static final String pathToHealthPanelButton = "//table[@id='" + ID.healthPanelToggle + "']/tbody/tr[2]/td[2]/em/button";
 		public static final String pathToPhysicalPanelButton = "//table[@id='" + ID.physicalPanelToggle + "']/tbody/tr[2]/td[2]/em/button";
 		public static final String pathToHealthPanel = "//div[@id='" + ID.healthPanel + "']";
 		public static final String pathToPhysicalPanel = "//div[@id='" + ID.physicalPanel + "']";
 		public static final String pathToOs = "/div/table/tbody/tr/td[2]/div/img";
 		public static final String pathToHostName = "/table/tbody/tr/td[3]";
 		public static final String pathToCores = "/table/tbody/tr/td[6]";
 		public static final String pathToPUIs = "/table/tbody/tr/td[7]";
 		public static final String pathToGSCCount = "/table/tbody/tr/td[8]";
 		public static final String pathToGSACount = "/table/tbody/tr/td[9]";
 		public static final String pathToGSMCount = "/table/tbody/tr/td[10]";
 		public static final String pathToUndeployNode = "//a[contains(text(),'Undeploy')]";
 		public static final String pathToGenerateNodeDump = "//a[contains(text(),'Generate dump')]";
 		public static final String generateDumpButton = "//button[text()='Generate']";
 		public static final String closeWindow = "//button[text()='Close']";
 		public static final String pathToTopMetricSelection = "//div[@id='" + ID.detailsPanel + "']/div[2]/div/div/div[2]/div/div/div/div[1]";
 		public static final String pathToBottomMetricSelection = "//div[@id='" + ID.detailsPanel + "']/div[2]/div/div/div[2]/div/div/div/div[3]";
 //		public static final String pathToBottomMetric = "//div[@id='" + ID.bottomMetric + "']/div";
 //		public static final String pathToTopMetric = "//div[@id='" + ID.topMetric + "']/div";
 		public static final String pathToSpaceInstanceGrid = "//div[@id='" + ID.spaceInstancesGrid + "']";
 		public static final String pathToSpaceConfigurationGrid = "//div[@id='" + ID.spaceConfigurationGrid + "']";
 		public static final String pathToSpaceTypesGrid = "//div[@id='" + ID.spaceTypesGrid + "']";
 		public static final String pathToSpaceTransactionsGrid = "//div[@id='" + ID.spaceTransactionsGrid + "']";
 		public static final String pathToSpaceTreeSidePanel = "//div[@id='" + ID.sidePanelSpacesTree + "']";
 		public static final String pathToLogsSamplingButton = "//div[@id='gs-services-logs-tool-bar']/table/tbody/tr/td/table/tbody/tr[2]/td[2]/em/button";
 		public static final String pathToApplicationNameInLogsPanel = "//div[@id='gs-tab-item-logs']/div/div/div[1]/span";
 		public static final String pathToLogsMachineExpandButton = "/div[1]/img[2]";
 
 		public static final String getPathToMenuSelection(String st) {
 			return "//a[contains(text(),'" + st + "')]";
 		}
 		
 		
 		public static final String getPathToAlertByIndex(int index) {
 			return Xpath.pathToAlerts + "/div[" + index + "]";
 		}
 		
 		public static String getPathToRowNumber(int index) {
 			return "//div[" + index + "]/table/tbody/tr[1]/td[1]/div/div";
 		}
 		
 		public static String getPathToComboSelection(String selection) {
 			return "//div[text()='" + selection + "']";
 		}
 		
 		public static String getPathToComboSelectionInServicesTab(String selection) {
 			return "//div[text()='" + selection + "']";
 		}
 		
 		public static String getPathToGsaOption(long gsaPid) {
 			return "//div[@id='hosts_tree_grid_gsa[" + gsaPid + "]']/div/span[3]/a/img";
 		}
 		
 		public static String getPathToGscOption(String gscPid, int gscIndex) {
 			return "//div[@id='hosts_tree_grid_gsc-" + gscIndex + "[" + gscPid + "]']/div/span[3]/a/img";
 		}
 		
 		public static String getPathToHostnameOptions(String hostname) {
 			return "//div[@id='hosts_tree_grid_host_" + hostname + "']/div/img[2]";
 		}
 		
 		public static String getPathToUndeployPuButton(String processingUnitName) {
 			return "//div[@id='pu_tree_grid_pu_" + processingUnitName + "']/div/span[3]/a/img";
 		}
 		
 		public static String getPathToProcessingUnitByIndex(int index) {
 			return Xpath.pathToPuTreeGrid + "/div/div/div[2]/div/div[" + index + "]";
 		}
 		
 		public static String getPathToPartition(int partition, String puName) {
 			return "//div[@id='pu_tree_grid_" + puName + "_partition " + partition + "']/div/img[2]";
 		}
 		
 		public static String getPathToInstanceOptions(int partition, String puName) {
 			return "//div[@id='pu_tree_grid_" + puName + "_" + puName + ". " + partition + " [1]']/div/span[3]/a/img";
 		}
 		
 		public static String getPathToNumberOfHosts() {
 			return "//div[@id='" + ID.resourceDistribHosts + "']/table/tbody/tr/td[3]";
 		}
 		
 		public static String getPathToNumberOfGSA() {
 			return "//div[@id='" + ID.resourceDistribGSA + "']/table/tbody/tr/td[3]";
 		}
 		
 		public static String getPathToNumberOfGSM() {
 			return "//div[@id='" + ID.resourceDistribGSM + "']/table/tbody/tr/td[3]";
 		}
 		
 		public static String getPathToNumberOfGSC() {
 			return "//div[@id='" + ID.resourceDistribGSC + "']/table/tbody/tr/td[3]";
 		}
 		
 		public static String getPathToNumberOfLUS() {
 			return "//div[@id='" + ID.resourceDistribLUS + "']/table/tbody/tr/td[3]";
 		}
 		
 		public static String getPathToNumberOfWebModules() {
 			return "//div[@id='" + ID.moduleWeb + "']/table/tbody/tr/td[2]";
 		}
 		
 		public static String getPathToNumberOfStatefullModules() {
 			return "//div[@id='" + ID.moduleStateful + "']/table/tbody/tr/td[2]";
 		}
 		
 		public static String getPathToNumberOfStatelessModules() {
 			return "//div[@id='" + ID.moduleStateless + "']/table/tbody/tr/td[2]";
 		}
 		
 		public static String getPathToNumberOfMirrorModules() {
 			return "//div[@id='" + ID.moduleMirror + "']/table/tbody/tr/td[2]";
 		}	
 		
 		public static String getPathToApplicationInPanel(int index) {
 			return "//div[@id='" + ID.applicationsMenuPanel  +  "']/div/div/div/div/div[2]/div/div[" + index + "]";
 		}
 		
 		public static String getPathToApplicationSelectionButton(String appName) {
 			return "//button[text()='" + appName + "']";
 		}
 
 		public static String getPathToMetricByIndex(int i) {
 			if (i <= 3) return WebConstants.Xpath.pathToHealthPanel + "/div[2]/div/div/div[1]/div[" + i + "]";
 			return WebConstants.Xpath.pathToHealthPanel + "/div[2]/div/div/div[2]/div[" + (i - 3) + "]";
 		}
 
 		public static String getPathToPuInstanceButton(String name, int partition) {
 			if (partition == 0) {
 				return "//div[@id='pu_tree_grid_" + name + "_" + name + "[1]']/div/span[3]/a/img";
 			}
 			else return "//div[@id='pu_tree_grid_" + name + "_" + name + "." + partition  + " " + "[1]" + "']/div/span[3]/a/img";
 		}
 
 		public static String getPathToPuButton(String name) {
 			return "//div[@id='pu_tree_grid_pu_name_" + name + "']/div/img[2]";
 		}
 
 		public static String getPathToPartitionButton(String name, int partition) {
 			return "//div[@id='pu_tree_grid_" + name + "_partition " + partition + "']/div/img[2]";
 		}
 
 		public static String getPathToDetailsGroup(int i) {
 			return Xpath.pathToDetailsPanel + "/div/div[2]/div[2]/div/div/div/div/div[2]/div/div[" + i + "]";
 		}
 
 		public static String getPathToAttributeInGroup(int j) {
 			return "/div[2]/div[" + j + "]";
 		}
 
 		public static String getPathToHostByIndex(int i) {
 			return Xpath.pathToPhysicalPanel + "/div/div[2]/div/div/div/div/div[2]/div[" + i + "]";
 		}
 
 
 		public static String getPathToSpaceInstanceByIndex(int i) {
 			return Xpath.pathToSpaceInstanceGrid + "/div/div/div[2]/div/div[" + i + "]";
 		}
 
 
 		public static String getPathToSpaceTreeNodeByIndex(int i) {
 			return Xpath.pathToSpaceTreeSidePanel + "/div/table/tbody/tr[3]/td/div/div[" + i + "]";
 		}
 		public static String getPathToPuLogsExapndButton(String puName) {
 			return "//div[@id='" + ID.getPuNodeId(puName) + "']/div[1]/img[2]";
 		}
 		public static String getPathToLogsMachine(String machineName, String puName) {
 			return "//div[@id='" + ID.getLogsMachineId(machineName, puName) + "']";
 		}
 		
 		public static String getPathToLogsContianer(String contianerId, String agentId, String puName) {
 			return "//div[@id='" + ID.getLogsContianerId(contianerId, agentId, puName) + "']";
 		}
 
 		public static String getPathToLogsMachineServiceByIndex(int i) {
 			return "/div[2]/div[" + i + "]";
 		}
 		
 		public static String getPathToLogsContianerPuInstanceByIndex(int i) {
 			return "/div[2]/div[" + i + "]";
 		}
 	}
 	
 	public static final class DOM {
 		
 		
 	}
 	
 	public static final class JQuery {
 		
 		public static final String puIntnacesPhysicalTabSelector = "$('div.x-grid3-cell-inner.x-grid3-col-processing_unit_instances > div > svg > text > tspan').text()";
 
 		public static String getMouseOverMetricTypeScript(String type) {
 			return "$(" + '"' + "a:contains('" + type + "')" + '"' + ").trigger('mousover')";
 		}
 		
 		public static String getMetricBalanceScript(String type) {	
 			return "return $('." + type + " .gs-cursor-pointer text:last').text()";
 		}
 		
 	}
 	
 	public static final class ClassNames {
 		
 		public static final String primarySpaceImage = "gs-space-instance-icon-primary";
 		public static final String backupSpaceImage = "gs-space-instance-icon-backup";
 		public static final String selectedItemInLogsTree = "x-ftree2-selected";
 		public static final String barLineChartContainer = "highcharts-container";
 		public static final String balanceGauge = "gs-cursor-pointer";
 		
 		public static final String win32OS = "gs-os-icon-Win32";
 		
 		public static final String getHostClassName(String name) {
 			return "gs-physical-grid-row-" + name;
 		}
 		
 		public static final String getMetricClassName(String name) {
			return "gs-metric-title-" + name;
 		}
 		
 		public static final String getMetricClassNameByIndex(int index) {
 			return "gs-monitoring-component-" + index;
 		}
 
 		public static String getPuInstanceClassName(String name) {
 			return "gs-physical-grid-row-" + name.replaceAll(" ", "").replaceAll("\\.", "_");
 		}
 
 	}
 
 }
