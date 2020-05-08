 package net.gisnas.oystein.ibm;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MalformedObjectNameException;
 import javax.management.Notification;
 import javax.management.NotificationListener;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.ibm.websphere.management.AdminClient;
 import com.ibm.websphere.management.application.AppConstants;
 import com.ibm.websphere.management.application.AppManagement;
 import com.ibm.websphere.management.application.AppManagementProxy;
 import com.ibm.websphere.management.application.AppNotification;
 import com.ibm.websphere.management.exception.AdminException;
 import com.ibm.websphere.management.exception.ConnectorException;
 import com.ibm.websphere.management.wlm.ClusterMemberData;
 import com.ibm.ws.management.application.client.MapModulesToServers;
 
 /**
  * Low level management client for WebSphere applications
  * 
  * Convenience wrapper around {@link AppManagementProxy} Adds basic exception
  * handling and logging For higher level operations, see {@link AppManager}
  */
 public class AppManagementClient implements NotificationListener {
 
 	private static final Logger logger = LoggerFactory.getLogger(AppManagementClient.class);
 	private static final long MAX_WAIT_TIME = 86400000L;
 
 	private AppManagement proxy;
 	private AdminClient adminClient;
 
 	private AppNotification appNotification;
 
 	public AppManagementClient(AdminClient adminClient) {
 		this.adminClient = adminClient;
 		try {
 			proxy = AppManagementProxy.getJMXProxyForClient(adminClient);
 		} catch (Exception e) {
 			throw new RuntimeException("Could not obtain JMX proxy AppManagement", e);
 		}
 	}
 
 	private String getAppNotificationStatus(String task) {
 		while (appNotification == null || (!task.equals(appNotification.taskName) || !AppNotification.STATUS_COMPLETED.equals(appNotification.taskStatus))
 				&& !AppNotification.STATUS_FAILED.equals(appNotification.taskStatus)) {
 			try {
 				wait(MAX_WAIT_TIME);
 			} catch (InterruptedException e) {
 				logger.trace("Thread was interrupted while waiting for appNotificationStatus");
 			}
 		}
 		return appNotification.taskStatus;
 	}
 
 	private synchronized void setAppNotificationStatus(AppNotification appNotification) {
 		synchronized (this) {
 			this.appNotification = appNotification;
 			notify();
 		}
 	}
 
 	protected AppManagement getProxy() {
 		return proxy;
 	}
 
 	private ObjectName getMBean() {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=AppManagement,*");
 			Iterator<?> iter = adminClient.queryNames(query, null).iterator();
 			if (!iter.hasNext()) {
 				throw new RuntimeException("MBean not found with query " + query);
 			}
 			return (ObjectName) iter.next();
 		} catch (MalformedObjectNameException e) {
 			throw new RuntimeException("Could not query MBean", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 	private Set<?> getManagedServers() {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=Server,processType=ManagedProcess,*");
 			Set<?> result = adminClient.queryNames(query, null);
 			return result;
 		} catch (MalformedObjectNameException e) {
 			throw new RuntimeException("An error occured while querying servers", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 	private Set<?> getUnmanagedServers() {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=Server,processType=UnManagedProcess,*");
 			Set<?> result = adminClient.queryNames(query, null);
 			return result;
 		} catch (MalformedObjectNameException e) {
 			throw new RuntimeException("An error occured while querying servers", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 	private Set<?> getClusters() {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=Cluster,*");
 			Set<?> result = adminClient.queryNames(query, null);
 			return result;
 		} catch (MalformedObjectNameException e) {
 			throw new RuntimeException("An error occured while querying clusters", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 	public ObjectName lookupCluster(String cluster) {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=Cluster,name=" + cluster + ",*");
 			Iterator<?> iter = adminClient.queryNames(query, null).iterator();
 			if (!iter.hasNext()) {
 				throw new RuntimeException("Cluster " + cluster + " not found");
 			}
 			return (ObjectName) iter.next();
 		} catch (MalformedObjectNameException e) {
 			throw new RuntimeException("An error occured while querying cluster", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 	/**
 	 * Start application on all deployment targets
 	 * 
 	 * Requires administrator role Operator, Deployer or Administrator 
 	 * 
 	 * @param appName
 	 * @see AppManagement#startApplication(String, java.util.Hashtable, String)
 	 */
 	public void startApplication(String appName) {
 		try {
 			String result = proxy.startApplication(appName, null, null);
 			if (result == null) {
 				throw new RuntimeException("Could not start application '" + appName + "'. Please consult server logs");
 			}
 			logger.debug("startApplication result: {}", result);
 		} catch (AdminException e) {
 			throw new RuntimeException("Could not start application", e);
 		}
 	}
 
 	/**
 	 * Stop application on all deployment targets
 	 * 
 	 * Requires administrator role Operator, Deployer or Administrator 
 	 * 
 	 * @param appName
 	 * @see AppManagement#stopApplication(String, java.util.Hashtable, String)
 	 */
 	public void stopApplication(String appName) {
 		try {
 			String result = proxy.stopApplication(appName, null, null);
 			if (result == null) {
 				throw new RuntimeException("Could not stop application '" + appName + "'. Please consult server logs");
 			}
 			logger.debug("stopApplication result: {}", result);
 		} catch (AdminException e) {
 			throw new RuntimeException("Could not start application", e);
 		}
 	}
 
 	/**
 	 * Install application on deployment manager with one cluster or one server
 	 * 
 	 * If there are multiple servers and one clusters, the cluster will be
 	 * chosen as the deployment target.
 	 * 
 	 * Requires administrator role Configurator, Deployer or Administrator 
 	 * 
 	 * @param earPath
 	 * @param redeploy
 	 * @param appName
 	 */
 	public void installApplication(String earPath, boolean redeploy, String appName) {
 		String deploymentTarget = findDeploymentTarget();
 		logger.debug("Found deployment target {}", deploymentTarget);
 		installApplication(earPath, redeploy, appName, deploymentTarget);
 	}
 
 	/**
 	 * Looking for single deployment target. Search order:
 	 * 1. Clusters
 	 * 2. Managed servers
 	 * 3. Unmanaged servers
 	 * 
 	 * @return Object name of single deployment target
 	 * @throws RuntimeException If no unambiguous target (unique target in category) found 
 	 */
 	private String findDeploymentTarget() {
 		Set<?> clusters = getClusters();
 		if (clusters.size() == 1) {
 			return createClusterString((ObjectName) clusters.iterator().next());
 		}
 
 		Set<?> managedServers = getManagedServers();
 		if (managedServers.size() == 1) {
 			return createServerString((ObjectName) managedServers.iterator().next());
 		}
 
 		Set<?> unmanagedServers = getUnmanagedServers();
 		if (unmanagedServers.size() == 1) {
 			return createServerString((ObjectName) unmanagedServers.iterator().next());
 		}
 		
 		throw new RuntimeException("Unambiguous server/cluster target, found " + clusters.size() + " clusters, " + managedServers.size() + " managed servers and " + unmanagedServers.size() + " unmanaged servers. Please specifiy target.");
 	}
 
 	public static String createClusterString(ObjectName clusterON) {
 		String cell = clusterON.getKeyProperty("cell");
 		String cluster = clusterON.getKeyProperty("name");
 		String deploymentTarget = "WebSphere:cell=" + cell + ",cluster=" + cluster;
 		return deploymentTarget;
 	}
 
 	public static String createServerString(ObjectName serverON) {
 		String cell = serverON.getKeyProperty("cell");
 		String node = serverON.getKeyProperty("node");
 		String server = serverON.getKeyProperty("name");
 		String deploymentTarget = "WebSphere:cell=" + cell + ",node=" + node + ",server=" + server;
 		return deploymentTarget;
 	}
 
 	/**
 	 * Install application on deployment manager with one cluster or one server
 	 * 
 	 * If there are multiple servers and one clusters, the cluster will be
 	 * chosen as the deployment target.
 	 * 
 	 * Requires administrator role Configurator, Deployer or Administrator 
 	 * 
	 * @param earPath
 	 * @param redeploy
 	 * @param appName
 	 * @param target
 	 */
 	public void installApplication(String earFile, boolean redeploy, String appName, String target) {
 		try {
 			Hashtable<String, String> module2server = new Hashtable<>();
 			module2server.put("*", target);
 			Hashtable<String, Object> props = new Hashtable<>();
 			props.put(AppConstants.APPDEPL_ARCHIVE_UPLOAD, true);
 			props.put(AppConstants.APPDEPL_MODULE_TO_SERVER, module2server);
 			adminClient.addNotificationListener(getMBean(), this, null, null);
 			appNotification = null;
 			synchronized (this) {
 				if (redeploy) {
 					proxy.redeployApplication(earFile, appName, props, null);
 				} else {
 					proxy.installApplication(earFile, appName, props, null);
 				}
 				switch (getAppNotificationStatus(AppNotification.INSTALL)) {
 				case AppNotification.STATUS_COMPLETED:
 					logger.debug("Installation of {} completed successfully", earFile);
 					break;
 				case AppNotification.STATUS_FAILED:
 					throw new RuntimeException("Installation of " + earFile + " failed, see log messages for details");
 				default:
 					throw new RuntimeException("Received no conclusive status from application installation");
 				}
 			}
 		} catch (AdminException e) {
 			throw new RuntimeException("An error occured while installing the application " + earFile, e);
 		} catch (InstanceNotFoundException e) {
 			throw new RuntimeException("Could not find MBean " + getMBean(), e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("Communication with deployment manager failed", e);
 		} finally {
 			try {
 				adminClient.removeNotificationListener(getMBean(), this);
 			} catch (Exception e) {
 				logger.warn("Unable to remove notification listener: {}", e);
 			}
 		}
 	}
 
 	/**
 	 * Uninstall application on deployment manager
 	 * 
 	 * Requires administrator role Configurator, Deployer or Administrator 
 	 * 
 	 * @param appName
 	 */
 	public void uninstallApplication(String appName) {
 		try {
 			adminClient.addNotificationListener(getMBean(), this, null, null);
 			synchronized (this) {
 				proxy.uninstallApplication(appName, new Hashtable<String, Object>(), null);
 				switch (getAppNotificationStatus(AppNotification.UNINSTALL)) {
 				case AppNotification.STATUS_COMPLETED:
 					logger.debug("Uninstallation of {} completed successfully", appName);
 					break;
 				case AppNotification.STATUS_FAILED:
 					throw new RuntimeException("Uninstallation of " + appName + " failed, see log messages for details");
 				default:
 					throw new RuntimeException("Received no conclusive status from application uninstallation");
 				}
 			}
 		} catch (AdminException e) {
 			throw new RuntimeException("Uninstallation of application " + appName + " failed", e);
 		} catch (InstanceNotFoundException e) {
 			throw new RuntimeException("MBean not found", e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		} finally {
 			try {
 				adminClient.removeNotificationListener(getMBean(), this);
 			} catch (Exception e) {
 				logger.warn("Unable to remove notification listener: {}", e);
 			}
 		}
 	}
 
 	public void handleNotification(Notification notification, Object handback) {
 		AppNotification appNotification = (AppNotification) notification.getUserData();
 		logger.trace("AppNotification received: {}", appNotification);
 		switch (appNotification.taskStatus) {
 		case AppNotification.STATUS_INPROGRESS:
 			logger.debug("{}", appNotification.message);
 			break;
 		case AppNotification.STATUS_COMPLETED:
 			logger.debug("{}", appNotification.message);
 			setAppNotificationStatus(appNotification);
 			break;
 		case AppNotification.STATUS_WARNING:
 			logger.warn("{}", appNotification.message);
 			break;
 		case AppNotification.STATUS_FAILED:
 			logger.error("{}", appNotification.message);
 			setAppNotificationStatus(appNotification);
 			break;
 		default:
 			logger.warn("Uknown status for AppNotification {}", appNotification);
 		}
 	}
 
 	/**
 	 * Check if application exists in cell
 	 * 
 	 * @param appName
 	 * @return true if application exists
 	 */
 	public boolean checkIfAppExists(String appName) {
 		try {
 			return proxy.checkIfAppExists(appName, null, null);
 		} catch (AdminException e) {
 			throw new RuntimeException("Unable to check if app " + appName + " exists", e);
 		}
 	}
 
 	/**
 	 * Check if application is distributed to all nodes
 	 * 
 	 * Blocks until distribution is completed
 	 * 
 	 * @param appName
 	 * @return true if aplication is distributed to all nodes
 	 */
 	public boolean isAppReady(String appName) {
 		try {
 			logger.debug("Checking distribution status for {}", appName);
 			adminClient.addNotificationListener(getMBean(), this, null, null);
 			synchronized (this) {
 				proxy.getDistributionStatus(appName, new Hashtable<String, Object>(), null);
 				switch (getAppNotificationStatus(AppNotification.DISTRIBUTION_STATUS_NODE)) {
 				case AppNotification.STATUS_COMPLETED:
 					String compositeStatus = appNotification.props.getProperty(AppNotification.DISTRIBUTION_STATUS_COMPOSITE);
 					logger.debug("Received composite distribution status for application {}: {}", appName, compositeStatus);
 					return determineDistributionStatus(compositeStatus);
 				case AppNotification.STATUS_FAILED:
 					throw new RuntimeException("getDistributionStatus " + appName + " failed, see log messages for details");
 				default:
 					throw new RuntimeException("Received no conclusive status from application distribution");
 				}
 			}
 		} catch (AdminException | InstanceNotFoundException | ConnectorException e) {
 			throw new RuntimeException("Unable to check distribution status for " + appName, e);
 		}
 	}
 
 	private boolean determineDistributionStatus(String compositeStatus) {
 		if (compositeStatus == null || "".equals(compositeStatus.trim())) {
 			return false;
 		} else {
 			String[] nodeStatus = compositeStatus.split("\\+");
 			try {
 				int distCount = 0;
 				for (int i = 0; i < nodeStatus.length; i++) {
 					ObjectName status = new ObjectName(nodeStatus[i]);
 					String distributionStatus = status.getKeyProperty(AppNotification.DISTRIBUTION_STATUS);
 					switch (distributionStatus) {
 					case AppNotification.DISTRIBUTION_DONE:
 						distCount++;
 						continue;
 					case AppNotification.DISTRIBUTION_NOT_DONE:
 						return false;
 					case AppNotification.DISTRIBUTION_UNKNOWN:
 						return false;
 					default:
 						throw new RuntimeException("Uknown distribution status " + distributionStatus);
 					}
 				}
 				if (distCount == 0) {
 					return false;
 				} else {
 					return true;
 				}
 			} catch (MalformedObjectNameException e) {
 				throw new RuntimeException("Error parsing distribution status object name", e);
 			}
 		}
 	}
 
 	public Set<String> getAppAssociation(String appName) {
 		Set<String> targets = new HashSet<>();
 		try {
 			Vector<?> info = proxy.getApplicationInfo(appName, null, null);
 			for (Object i : info) {
 				if (i instanceof MapModulesToServers) {
 					MapModulesToServers map = (MapModulesToServers) i;
 					String[][] taskData = map.getTaskData();
 					for (String[] strings : taskData) {
 						String target = strings[2];
 						if (target.equals("server")) {
 							continue;
 						}
 						logger.debug("Found target {} in ModulesToServer mapping for {}", target, appName);
 						ObjectName targetON = new ObjectName(target);
 						if (targetON.getKeyProperty("cluster") != null) {
 							targets.addAll(getClusterMembers(targetON.getKeyProperty("cluster")));
 						} else {
 							targets.add(target);
 						}
 					}
 				}
 			}
 		} catch (AdminException | MalformedObjectNameException e) {
 			throw new RuntimeException("Unable to determine app association for " + appName, e);
 		}
 		return targets;
 	}
 
 	private Set<String> getClusterMembers(String clusterName) {
 		try {
 			ObjectName query = new ObjectName("WebSphere:type=Cluster,name="+clusterName+",*");
 			Set<?> result = adminClient.queryNames(query, null);
 			if (result.size() != 1){
 				throw new RuntimeException("Didn't expect result size " + result.size() + " when querying cluster " + clusterName);
 			}
 			ObjectName clusterON = (ObjectName) result.iterator().next();
 			ClusterMemberData[] clusterMemberData = (ClusterMemberData[]) adminClient.invoke(clusterON, "getClusterMembers", null, null);
 			Set<String> clusterMembers = new HashSet<>(clusterMemberData.length);
 			for (ClusterMemberData c : clusterMemberData) {
 				ObjectName clusterMember = c.memberObjectName;
 				clusterMembers.add(createServerString(clusterMember));
 			}
 			return clusterMembers;
 		} catch (MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException e) {
 			throw new RuntimeException("An error occured while querying cluster " + clusterName, e);
 		} catch (ConnectorException e) {
 			throw new RuntimeException("An error occured in the communication with the deployment manager", e);
 		}
 	}
 
 }
