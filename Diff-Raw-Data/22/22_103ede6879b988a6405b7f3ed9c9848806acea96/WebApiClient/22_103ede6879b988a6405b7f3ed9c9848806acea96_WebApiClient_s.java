 /*******************************************************************************
  * Copyright (c) Feb 27, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.core;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.Map;
 
 import org.restlet.Context;
 import org.restlet.engine.Engine;
 import org.zend.webapi.core.configuration.ClientConfiguration;
 import org.zend.webapi.core.connection.auth.WebApiCredentials;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.ApplicationsList;
 import org.zend.webapi.core.connection.data.CodeTrace;
 import org.zend.webapi.core.connection.data.CodeTraceFile;
 import org.zend.webapi.core.connection.data.CodeTracingList;
 import org.zend.webapi.core.connection.data.CodeTracingStatus;
 import org.zend.webapi.core.connection.data.DebugRequest;
 import org.zend.webapi.core.connection.data.EventsGroupDetails;
 import org.zend.webapi.core.connection.data.Issue;
 import org.zend.webapi.core.connection.data.IssueDetails;
 import org.zend.webapi.core.connection.data.IssueFile;
 import org.zend.webapi.core.connection.data.IssueList;
 import org.zend.webapi.core.connection.data.ProfileRequest;
 import org.zend.webapi.core.connection.data.RequestSummary;
 import org.zend.webapi.core.connection.data.ServerConfig;
 import org.zend.webapi.core.connection.data.ServerInfo;
 import org.zend.webapi.core.connection.data.ServersList;
 import org.zend.webapi.core.connection.data.SystemInfo;
 import org.zend.webapi.core.connection.data.values.IssueStatus;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.dispatch.IServiceDispatcher;
 import org.zend.webapi.core.connection.request.IRequest;
 import org.zend.webapi.core.connection.request.IRequestInitializer;
 import org.zend.webapi.core.connection.request.NamedInputStream;
 import org.zend.webapi.core.connection.request.RequestFactory;
 import org.zend.webapi.core.connection.response.IResponse;
 import org.zend.webapi.core.progress.IChangeNotifier;
 import org.zend.webapi.core.service.WebApiMethodType;
 import org.zend.webapi.internal.core.connection.ServiceDispatcher;
 import org.zend.webapi.internal.core.connection.WebApiEngine;
 import org.zend.webapi.internal.core.connection.request.ApplicationDeployRequest;
 import org.zend.webapi.internal.core.connection.request.ApplicationGetStatusRequest;
 import org.zend.webapi.internal.core.connection.request.ApplicationRedeployRequest;
 import org.zend.webapi.internal.core.connection.request.ApplicationRemoveRequest;
 import org.zend.webapi.internal.core.connection.request.ApplicationRollbackRequest;
 import org.zend.webapi.internal.core.connection.request.ApplicationUpdateRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterAddServerRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterDisableServerRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterEnableServerRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterGetServerStatusRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterReconfigureServerRequest;
 import org.zend.webapi.internal.core.connection.request.ClusterRemoveServerRequest;
 import org.zend.webapi.internal.core.connection.request.CodeTracingCreateRequest;
 import org.zend.webapi.internal.core.connection.request.CodeTracingDeleteRequest;
 import org.zend.webapi.internal.core.connection.request.CodeTracingDisableRequest;
 import org.zend.webapi.internal.core.connection.request.CodeTracingEnableRequest;
 import org.zend.webapi.internal.core.connection.request.CodeTracingListRequest;
 import org.zend.webapi.internal.core.connection.request.CodetracingDownloadTraceFileRequest;
 import org.zend.webapi.internal.core.connection.request.ConfigurationImportRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorChangeIssueStatusRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorExportIssueByEventsGroupRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorGetEventGroupDetailsRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorGetIssueDetailsRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorGetIssuesListPredefinedFilterRequest;
 import org.zend.webapi.internal.core.connection.request.MonitorGetRequestSummaryRequest;
 import org.zend.webapi.internal.core.connection.request.RestartPhpRequest;
 import org.zend.webapi.internal.core.connection.request.StudioStartDebugRequest;
 import org.zend.webapi.internal.core.connection.request.StudioStartProfileRequest;
 
 /**
  * Client for accessing Zend Server Web API. All service calls made using this
  * client are blocking, and will not return until the service call completes.
  * 
  * Zend Server Web API simple web service interface allows you to obtain and
  * configure your Zend Server and Zend Server cluster manager. It provides you
  * with complete control of your PHP Web applications servers and lets you run
  * on Zend's proven computing environment.
  * 
  * Zend Server Web API is intended to allow automation of the management and
  * deployment of Zend Server and Zend Server Cluster Manager, and allow
  * integration with other Zend or 3rd party software. Call a specific service
  * method
  * 
  * @author Roy, 2011
  * 
  */
 public class WebApiClient {
 
 	private static final WebApiVersion DEFAULT_VERSION = WebApiVersion.V1_1;
 
 	private static Engine engine = null;
 
 	/**
 	 * credentials of this client
 	 */
 	private final WebApiCredentials credentials;
 
 	/**
 	 * Client configuration of this instance
 	 */
 	private final ClientConfiguration clientConfiguration;
 
 	/**
 	 * Restlet connection context
 	 */
 	private final Context context;
 
 	/**
 	 * Progress notifier
 	 */
 	private IChangeNotifier notifier;
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the specified account credentials and configurations.
 	 * 
 	 * @param credentials
 	 * @param clientConfiguration
 	 * @param ctx
 	 * @param notifier
 	 */
 	public WebApiClient(WebApiCredentials credentials,
 			ClientConfiguration clientConfiguration, Context ctx,
 			IChangeNotifier notifier) {
 		this.credentials = credentials;
 		this.clientConfiguration = clientConfiguration;
 		this.context = ctx;
 		if (engine == null) {
 			engine = new WebApiEngine();
 			Engine.setInstance(engine);
 		}
 		this.notifier = notifier;
 	}
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the the specified host and credentials.
 	 * 
 	 * @param credentials
 	 * @param host
 	 * @param notifier
 	 * @throws MalformedURLException
 	 */
 	public WebApiClient(WebApiCredentials credentials, String host,
 			IChangeNotifier notifier) throws MalformedURLException {
 		this(credentials, new ClientConfiguration(new URL(host)), null,
 				notifier);
 	}
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the the specified host and credentials.
 	 * 
 	 * @param credentials
 	 * @param userAgent
 	 * @param context
 	 * @param notifier
 	 * @throws MalformedURLException
 	 */
 	public WebApiClient(WebApiCredentials credentials, String host,
 			Context context, IChangeNotifier notifier)
 			throws MalformedURLException {
 		this(credentials, new ClientConfiguration(new URL(host)), context,
 				notifier);
 	}
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the specified account credentials and configurations.
 	 * 
 	 * @param credentials
 	 * @param userAgent
 	 */
 	public WebApiClient(WebApiCredentials credentials,
 			ClientConfiguration clientConfiguration, Context ctx) {
 		this(credentials, clientConfiguration, ctx, null);
 	}
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the the specified host and credentials.
 	 * 
 	 * @param credentials
 	 * @param userAgent
 	 * @throws MalformedURLException
 	 */
 	public WebApiClient(WebApiCredentials credentials, String host)
 			throws MalformedURLException {
 		this(credentials, new ClientConfiguration(new URL(host)), null, null);
 	}
 
 	/**
 	 * Constructs a new client to invoke service methods on Zend Server API
 	 * using the the specified host and credentials.
 	 * 
 	 * @param credentials
 	 * @param userAgent
 	 * @param context
 	 * @throws MalformedURLException
 	 */
 	public WebApiClient(WebApiCredentials credentials, String host,
 			Context context) throws MalformedURLException {
 		this(credentials, new ClientConfiguration(new URL(host)), context, null);
 	}
 
 	/**
 	 * Get information about the system, including Zend Server edition and
 	 * version, PHP version, licensing information etc. In general this method
 	 * should be available and produce similar output on all Zend Server
 	 * systems, and be as future compatible as possible
 	 * 
 	 * @see WebApiMethodType#GET_SYSTEM_INFO
 	 * @return
 	 */
 	public SystemInfo getSystemInfo() throws WebApiException {
 		final IResponse handle = this.handle(WebApiMethodType.GET_SYSTEM_INFO,
 				null);
 		return (SystemInfo) handle.getData();
 	}
 
 	/**
 	 * Get the list of servers in the cluster and the status of each one. On a
 	 * ZSCM with no valid license, this operation will fail. Note that this
 	 * operation will cause Zend Server Cluster Manager to check the status of
 	 * servers and return fresh, non-cached information. This is different from
 	 * the Servers List tab in the GUI, which may present cached information.
 	 * Users interested in reducing load by caching this information should do
 	 * in their own code.
 	 * 
 	 * @see WebApiMethodType#CLUSTER_GET_SERVER_STATUS
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList clusterGetServerStatus(final String... servers)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_GET_SERVER_STATUS,
 				servers.length == 0 ? null : new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						((ClusterGetServerStatusRequest) request)
 								.setServers(servers);
 					}
 				});
 		return (ServersList) handle.getData();
 	}
 
 	/**
 	 * Add a new server to the cluster. On a ZSCM with no valid license, this
 	 * operation will fail.
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterAddServer(final String serverName,
 			final String serverUrl, final String guiPassword,
 			final Boolean propagateSettings, final Boolean doRestart)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_ADD_SERVER, new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						final ClusterAddServerRequest r = ((ClusterAddServerRequest) request)
 								.setServerName(serverName)
 								.setServerUrl(serverUrl)
 								.setGuiPassword(guiPassword);
 						if (propagateSettings != null)
 							r.setPropagateSettings(propagateSettings);
 						if (doRestart != null)
 							r.setDoStart(doRestart);
 					}
 				});
 		return (ServerInfo) handle.getData();
 	}
 
 	/**
 	 * Add a new server to the cluster. On a ZSCM with no valid license, this
 	 * operation will fail.
 	 * 
 	 * doRestart parameter value is not specified (for more details
 	 * {@link ClusterAddServerRequest}
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterAddServer(final String serverName,
 			final String serverUrl, final String guiPassword,
 			final Boolean propagateSettings) throws WebApiException {
 		return clusterAddServer(serverName, serverUrl, guiPassword,
 				propagateSettings, null);
 	}
 
 	/**
 	 * Add a new server to the cluster. On a ZSCM with no valid license, this
 	 * operation will fail.
 	 * 
 	 * doRestart and propagateSettings parameter values are not specified (for
 	 * more details {@link ClusterAddServerRequest}
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterAddServer(final String serverName,
 			final String serverUrl, final String guiPassword)
 			throws WebApiException {
 		return clusterAddServer(serverName, serverUrl, guiPassword, null, null);
 	}
 
 	/**
 	 * Remove a server from the cluster. The removal process may be asynchronous
 	 * if Session Clustering is used – if this is the case, the initial
 	 * operation will return an HTTP 202 response. As long as the server is not
 	 * fully removed, further calls to remove the same server should be
 	 * idempotent. On a ZSCM with no valid license, this operation will fail.
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterRemoveServer(final String serverId,
 			final Boolean force) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_REMOVE_SERVER,
 				new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						final ClusterRemoveServerRequest r = ((ClusterRemoveServerRequest) request)
 								.setServerId(serverId);
 						if (force != null)
 							r.setForce(force);
 					}
 				});
 		return (ServerInfo) handle.getData();
 	}
 
 	/**
 	 * Remove a server from the cluster. The removal process may be asynchronous
 	 * if Session Clustering is used – if this is the case, the initial
 	 * operation will return an HTTP 202 response. As long as the server is not
 	 * fully removed, further calls to remove the same server should be
 	 * idempotent. On a ZSCM with no valid license, this operation will fail.
 	 * 
 	 * propagateSettings parameter value is not specified. (for more details
 	 * {@link ClusterRemoveServerRequest}
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterRemoveServer(final String serverId)
 			throws WebApiException {
 		return clusterRemoveServer(serverId, null);
 	}
 
 	/**
 	 * Remove a server from the cluster. The removal process may be asynchronous
 	 * if Session Clustering is used – if this is the case, the initial
 	 * operation will return an HTTP 202 response. As long as the server is not
 	 * fully removed, further calls to remove the same server should be
 	 * idempotent. On a ZSCM with no valid license, this operation will fail.
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterDisableServer(final String serverId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_DISABLE_SERVER,
 				new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						((ClusterDisableServerRequest) request)
 								.setServerId(serverId);
 					}
 				});
 		return (ServerInfo) handle.getData();
 	}
 
 	/**
 	 * Re-enable a cluster member. This process may be asynchronous if Session
 	 * Clustering is used – if this is the case, the initial operation will
 	 * return an HTTP 202 response. This action is idempotent. Running it on an
 	 * enabled server will result in a 200 OK response with no consequences. On
 	 * a ZSCM with no valid license, this operation will fail.
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterEnableServer(final String serverId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_ENABLE_SERVER,
 				new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						((ClusterEnableServerRequest) request)
 								.setServerId(serverId);
 					}
 				});
 		return (ServerInfo) handle.getData();
 	}
 
 	/**
 	 * Reconfigure a cluster member to match the cluster's profile. On a ZSCM
 	 * with no valid license, this operation will fail.
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterReconfigureServer(final String serverId,
 			final Boolean doRestart) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CLUSTER_RECONFIGURE_SERVER,
 				new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						final ClusterReconfigureServerRequest r = (ClusterReconfigureServerRequest) request;
 						r.setServerId(serverId);
 						if (doRestart != null)
 							r.setDoRestart(doRestart);
 					}
 				});
 		return (ServerInfo) handle.getData();
 	}
 
 	/**
 	 * Reconfigure a cluster member to match the cluster's profile. On a ZSCM
 	 * with no valid license, this operation will fail.
 	 * 
 	 * 
 	 * doRestart parameter value is not specified. (for more details
 	 * {@link ClusterReconfigureServerRequest}
 	 * 
 	 * @return server info
 	 * @throws WebApiException
 	 */
 	public ServerInfo clusterReconfigureServer(final String serverId)
 			throws WebApiException {
 		return clusterReconfigureServer(serverId, null);
 	}
 
 	/**
 	 * Restart PHP on all servers or on specified servers in the cluster. A 202
 	 * response in this case does not always indicate a successful restart of
 	 * all servers, and the user is advised to check the server(s) status again
 	 * after a few seconds using the clusterGetServerStatus command.
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList restartPhp(final Boolean parallelRestart,
 			final String... servers) throws WebApiException {
 		final IResponse handle = this.handle(WebApiMethodType.RESTART_PHP,
 				servers == null ? null : new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						((RestartPhpRequest) request).setServers(servers)
 								.setParallelRestart(parallelRestart);
 					}
 				});
 		return (ServersList) handle.getData();
 	}
 
 	/**
 	 * Restart PHP on all servers or on specified servers in the cluster. A 202
 	 * response in this case does not always indicate a successful restart of
 	 * all servers, and the user is advised to check the server(s) status again
 	 * after a few seconds using the clusterGetServerStatus command.
 	 * 
 	 * parallelRestart parameter calue is not specified. for more detailed see
 	 * {@link RestartPhpRequest}
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList restartPhp(final String... servers)
 			throws WebApiException {
 		return restartPhp(false, servers);
 	}
 
 	/**
 	 * Restart PHP on all servers or on specified servers in the cluster. A 202
 	 * response in this case does not always indicate a successful restart of
 	 * all servers, and the user is advised to check the server(s) status again
 	 * after a few seconds using the clusterGetServerStatus command.
 	 * 
 	 * parallelRestart and servers parameter values are not specified. for more
 	 * detailed see {@link RestartPhpRequest}
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList restartPhp() throws WebApiException {
 		return restartPhp((Boolean) null, (String[]) null);
 	}
 
 	/**
 	 * export the current server / cluster configuration into a file.
 	 * 
 	 * @return server config
 	 * @throws WebApiException
 	 */
 	public ServerConfig configuratioExport() throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CONFIGURATION_EXPORT, null);
 		return (ServerConfig) handle.getData();
 	}
 
 	/**
 	 * Import a saved configuration snapshot into the server
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList configuratioImport(final NamedInputStream configFile,
 			final Boolean ignoreSystemMismatch) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CONFIGURATION_IMPORT,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						((ConfigurationImportRequest) request)
 								.setConfigStream(configFile);
 
 						if (ignoreSystemMismatch != null)
 							((ConfigurationImportRequest) request)
 									.setIgnoreSystemMismatch(ignoreSystemMismatch);
 					}
 				});
 		return (ServersList) handle.getData();
 	}
 
 	/**
 	 * Import a saved configuration snapshot into the server
 	 * 
 	 * ignoreSystemMismatch parameter value is not specified. see
 	 * {@link ConfigurationImportRequest} for more details.
 	 * 
 	 * @return servers list
 	 * @throws WebApiException
 	 */
 	public ServersList configuratioImport(final NamedInputStream configFile)
 			throws WebApiException {
 		return configuratioImport(configFile, null);
 	}
 
 	/**
 	 * Get the list of applications currently deployed (or staged) on the server
 	 * or the cluster and information about each application. If application IDs
 	 * are specified, will return information about the specified applications;
 	 * If no IDs are specified, will return information about all applications.
 	 * 
 	 * @see WebApiMethodType#APPLICATION_GET_STATUS
 	 * 
 	 * @return applications list
 	 * @throws WebApiException
 	 */
 	public ApplicationsList applicationGetStatus(final String... applications)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_GET_STATUS,
 				applications.length == 0 ? null : new IRequestInitializer() {
 					public void init(IRequest request) throws WebApiException {
 						((ApplicationGetStatusRequest) request)
 								.setApplications(applications);
 					}
 				});
 		return (ApplicationsList) handle.getData();
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Boolean ignoreFailures,
 			final Map<String, String> userParam, final String userAppName,
 			final Boolean createVhost, final Boolean defaultServer)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_DEPLOY, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ApplicationDeployRequest deployRequest = (ApplicationDeployRequest) request;
 						deployRequest.setAppPackage(appPackage);
 						deployRequest.setBaseUrl(baseUrl);
 						if (ignoreFailures != null) {
 							deployRequest.setIgnoreFailures(ignoreFailures);
 						}
 						if (userParam != null) {
 							deployRequest.setUserParams(userParam);
 						}
 						if (userAppName != null) {
 							deployRequest.setUserAppName(userAppName);
 						}
 						if (createVhost != null) {
 							deployRequest.setCreateVhost(createVhost);
 						}
 						if (defaultServer != null) {
 							deployRequest.setDefaultServer(defaultServer);
 						}
 						deployRequest.setNotifier(notifier);
 					}
 				});
 		return (ApplicationInfo) handle.getData();
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * 
 	 * appUserName and userParam parameter values are not specified. for more
 	 * detailed see {@link ApplicationDeployRequest}
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Boolean ignoreFailures)
 			throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, ignoreFailures, null,
 				null, null, null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * appUserName and ignoreFailures parameter values are not specified. for
 	 * more detailed see {@link ApplicationDeployRequest}
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Map<String, String> userParam)
 			throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, null, userParam, null,
 				null, null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * ignoreFailures, userParam and userAppName parameter values are not
 	 * specified. for more detailed see {@link ApplicationDeployRequest}
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl) throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, null, null, null, null,
 				null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * ignoreFailures and userParam parameter values are not specified. for more
 	 * detailed see {@link ApplicationDeployRequest}
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final String userAppName)
 			throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, null, null, userAppName,
 				null, null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * ignoreFailures parameter value is not specified. for more detailed see
 	 * {@link ApplicationDeployRequest}.
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Map<String, String> userParam,
 			final String userAppName) throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, null, userParam,
 				userAppName, null, null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * userAppName parameter value is not specified. for more detailed see
 	 * {@link ApplicationDeployRequest}.
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Boolean ignoreFailures,
 			final Map<String, String> userParam) throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, ignoreFailures,
 				userParam, null, null, null);
 	}
 
 	/**
 	 * Deploy a new application to the server or cluster. This process is
 	 * asynchronous – the initial request will wait until the application is
 	 * uploaded and verified, and the initial response will show information
 	 * about the application being deployed – however the staging and
 	 * activation process will proceed after the response is returned. The user
 	 * is expected to continue checking the application status using the
 	 * applicationGetStatus method until the deployment process is complete.
 	 * 
 	 * userParam parameter value is not specified. for more detailed see
 	 * {@link ApplicationDeployRequest}.
 	 * 
 	 * @return information about deployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationDeploy(final NamedInputStream appPackage,
 			final String baseUrl, final Boolean ignoreFailures,
 			final String userAppName) throws WebApiException {
 		return applicationDeploy(appPackage, baseUrl, ignoreFailures, null,
 				userAppName, null, null);
 	}
 
 	/**
 	 * Update/redeploy an existing application. The package provided must be of
 	 * the same application. Additionally any new parameters or new values to
 	 * existing parameters must be provided. This process is asynchronous –
 	 * the initial request will wait until the package is uploaded and verified,
 	 * and the initial response will show information about the new version
 	 * being deployed – however the staging and activation process will
 	 * proceed after the response is returned. The user is expected to continue
 	 * checking the application status using the applicationGetStatus method
 	 * until the deployment process is complete.
 	 * 
 	 * @return information about updated application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationUpdate(final int appId,
 			final NamedInputStream appPackage, final Boolean ignoreFailures,
 			final Map<String, String> userParam) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_UPDATE, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ApplicationUpdateRequest updateRequest = (ApplicationUpdateRequest) request;
 						updateRequest.setAppId(appId);
 						updateRequest.setAppPackage(appPackage);
 						if (ignoreFailures != null) {
 							updateRequest.setIgnoreFailures(ignoreFailures);
 						}
 						if (userParam != null) {
 							updateRequest.setUserParams(userParam);
 						}
 						updateRequest.setNotifier(notifier);
 					}
 				});
 		return (ApplicationInfo) handle.getData();
 	}
 
 	/**
 	 * Update/redeploy an existing application. The package provided must be of
 	 * the same application. Additionally any new parameters or new values to
 	 * existing parameters must be provided. This process is asynchronous –
 	 * the initial request will wait until the package is uploaded and verified,
 	 * and the initial response will show information about the new version
 	 * being deployed – however the staging and activation process will
 	 * proceed after the response is returned. The user is expected to continue
 	 * checking the application status using the applicationGetStatus method
 	 * until the deployment process is complete.
 	 * 
 	 * userParam parameter value is not specified. for more detailed see
 	 * {@link ApplicationUpdateRequest}.
 	 * 
 	 * @return information about updated application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationUpdate(final int appId,
 			final NamedInputStream appPackage, final Boolean ignoreFailures)
 			throws WebApiException {
 		return applicationUpdate(appId, appPackage, ignoreFailures, null);
 	}
 
 	/**
 	 * Update/redeploy an existing application. The package provided must be of
 	 * the same application. Additionally any new parameters or new values to
 	 * existing parameters must be provided. This process is asynchronous –
 	 * the initial request will wait until the package is uploaded and verified,
 	 * and the initial response will show information about the new version
 	 * being deployed – however the staging and activation process will
 	 * proceed after the response is returned. The user is expected to continue
 	 * checking the application status using the applicationGetStatus method
 	 * until the deployment process is complete.
 	 * 
 	 * ignoreFailures parameter value is not specified. for more detailed see
 	 * {@link ApplicationUpdateRequest}.
 	 * 
 	 * @return information about updated application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationUpdate(final int appId,
 			final NamedInputStream appPackage,
 			final Map<String, String> userParam) throws WebApiException {
 		return applicationUpdate(appId, appPackage, null, userParam);
 	}
 
 	/**
 	 * Update/redeploy an existing application. The package provided must be of
 	 * the same application. Additionally any new parameters or new values to
 	 * existing parameters must be provided. This process is asynchronous –
 	 * the initial request will wait until the package is uploaded and verified,
 	 * and the initial response will show information about the new version
 	 * being deployed – however the staging and activation process will
 	 * proceed after the response is returned. The user is expected to continue
 	 * checking the application status using the applicationGetStatus method
 	 * until the deployment process is complete.
 	 * 
 	 * ignoreFailures and userParam parameter values are not specified. for more
 	 * detailed see {@link ApplicationDeployRequest}
 	 * 
 	 * @return information about updated application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationUpdate(final int appId,
 			final NamedInputStream appPackage) throws WebApiException {
 		return applicationUpdate(appId, appPackage, null, null);
 	}
 
 	/**
 	 * Remove/undeploy an existing application. This process is asynchronous –
 	 * the initial request will start the removal process and the initial
 	 * response will show information about the application being removed –
 	 * however the removal process will proceed after the response is returned.
 	 * The user is expected to continue checking the application status using
 	 * the applicationGetStatus method until the removal process is complete.
 	 * Once applicationGetStatus contains no information about the specific
 	 * application, it has been completely removed.
 	 * 
 	 * @return information about removed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationRemove(final int appId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_REMOVE, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ApplicationRemoveRequest removeRequest = (ApplicationRemoveRequest) request;
 						removeRequest.setAppId(appId);
 					}
 				});
 		return (ApplicationInfo) handle.getData();
 	}
 
 	/**
 	 * Rollback an existing application to its previous version. This process is
 	 * asynchronous � the initial request will start the rollback process and
 	 * the initial response will show information about the application being
 	 * rolled back. The user is expected to continue checking the application
 	 * status using the applicationGetStatus method until the process is
 	 * complete.
 	 * 
 	 * @return information about application which was rolled back
 	 * @throws WebApiException
 	 * @since 1.1
 	 */
 	public ApplicationInfo applicationRollback(final int appId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_ROLLBACK,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ApplicationRollbackRequest removeRequest = (ApplicationRollbackRequest) request;
 						removeRequest.setAppId(appId);
 					}
 				});
 		return (ApplicationInfo) handle.getData();
 	}
 
 	/**
 	 * Redeploy an existing application, whether in order to fix a problem or to
 	 * reset an installation. This process is asynchronous – the initial
 	 * request will start the redeploy process and the initial response will
 	 * show information about the application being redeployed – however the
 	 * redeployment process will proceed after the response is returned. The
 	 * user is expected to continue checking the application status using the
 	 * applicationGetStatus method until the process is complete.
 	 * 
 	 * @return information about redeployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationSynchronize(final int appId,
 			final Boolean ignoreFailures, final String... servers)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.APPLICATION_SYNCHRONIZE,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						ApplicationRedeployRequest deployRequest = (ApplicationRedeployRequest) request;
 						deployRequest.setAppId(appId);
 						if (ignoreFailures != null) {
 							deployRequest.setIgnoreFailures(ignoreFailures);
 						}
 						if (servers != null && servers.length > 0) {
 							deployRequest.setServers(servers);
 						}
 					}
 				});
 		return (ApplicationInfo) handle.getData();
 	}
 
 	/**
 	 * Redeploy an existing application, whether in order to fix a problem or to
 	 * reset an installation. This process is asynchronous – the initial
 	 * request will start the redeploy process and the initial response will
 	 * show information about the application being redeployed – however the
 	 * redeployment process will proceed after the response is returned. The
 	 * user is expected to continue checking the application status using the
 	 * applicationGetStatus method until the process is complete.
 	 * 
 	 * ignoreFailures parameter value is not specified. for more detailed see
 	 * {@link ApplicationRedeployRequest}.
 	 * 
 	 * @return information about redeployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationSynchronize(int appId, String... servers)
 			throws WebApiException {
 		return applicationSynchronize(appId, null, servers);
 	}
 
 	/**
 	 * Redeploy an existing application, whether in order to fix a problem or to
 	 * reset an installation. This process is asynchronous – the initial
 	 * request will start the redeploy process and the initial response will
 	 * show information about the application being redeployed – however the
 	 * redeployment process will proceed after the response is returned. The
 	 * user is expected to continue checking the application status using the
 	 * applicationGetStatus method until the process is complete.
 	 * 
 	 * ignoreFailures and servers parameter values are not specified. for more
 	 * detailed see {@link ApplicationRedeployRequest}.
 	 * 
 	 * @return information about redeployed application
 	 * @throws WebApiException
 	 */
 	public ApplicationInfo applicationSynchronize(int appId)
 			throws WebApiException {
 		return applicationSynchronize(appId, (Boolean) null);
 	}
 
 	/**
 	 * Disable the two directives necessary for creating tracing dumps, this
 	 * action does not disable the code-tracing component. This action unsets
 	 * the special zend_monitor.developer_mode &
 	 * zend_monitor.event_generate_trace_file directives.
 	 * 
 	 * @return The new code tracing directive's state
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingStatus codeTracingDisable(final Boolean restartNow)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_DISABLE, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodeTracingDisableRequest disableRequest = (CodeTracingDisableRequest) request;
 						if (restartNow != null) {
 							disableRequest.setRestartNow(restartNow);
 						}
 					}
 				});
 		return (CodeTracingStatus) handle.getData();
 	}
 
 	/**
 	 * Disable the two directives necessary for creating tracing dumps, this
 	 * action does not disable the code-tracing component. This action unsets
 	 * the special zend_monitor.developer_mode &
 	 * zend_monitor.event_generate_trace_file directives.
 	 * <p>
 	 * Limitations:
 	 * </p>
 	 * <p>
 	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
 	 * </p>
	 * <p>
	 * Limitations:
	 * </p>
	 * <p>
	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
	 * </p>
 	 * 
 	 * @return The new code tracing directive's state
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingStatus codeTracingDisable() throws WebApiException {
 		return codeTracingDisable(null);
 	}
 
 	/**
	 * Disable the two directives necessary for creating tracing dumps, this
	 * action does not disable the code-tracing component. This action unsets
	 * the special zend_monitor.developer_mode &
	 * zend_monitor.event_generate_trace_file directives. Note that this action
	 * does not deactivate the component or any other setting.
 	 * <p>
 	 * Limitations:
 	 * </p>
 	 * <p>
 	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
 	 * </p>
 	 * 
 	 * @return The new code tracing directive's state
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingStatus codeTracingEnable(final Boolean restartNow)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_ENABLE, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodeTracingEnableRequest enableRequest = (CodeTracingEnableRequest) request;
 						if (restartNow != null) {
 							enableRequest.setRestartNow(restartNow);
 						}
 					}
 				});
 		return (CodeTracingStatus) handle.getData();
 	}
 
 	/**
	 * Disable the two directives necessary for creating tracing dumps, this
	 * action does not disable the code-tracing component. This action unsets
	 * the special zend_monitor.developer_mode &
	 * zend_monitor.event_generate_trace_file directives. Note that this action
	 * does not deactivate the component or any other setting.
 	 * <p>
 	 * Limitations:
 	 * </p>
 	 * <p>
 	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
 	 * </p>
 	 * 
 	 * @return The new code tracing directive's state
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingStatus codeTracingEnable() throws WebApiException {
 		return codeTracingEnable(null);
 	}
 
 	/**
 	 * This action checks the values of the special zend_monitor.developer_mode
 	 * & zend_monitor.event_generate_trace_file directives. The action also
 	 * checks the current activity state of the component and the trace-enabled
 	 * directive value.
 	 * <p>
 	 * Limitations:
 	 * </p>
 	 * <p>
 	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
 	 * </p>
 	 * 
 	 * @return Indication for the component's current status
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingStatus codeTracingIsEnabled() throws WebApiException {
 		IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_IS_ENABLED, WebApiVersion.V1_2,
 				null);
 		return (CodeTracingStatus) handle.getData();
 	}
 
 	/**
 	 * Create a new code-tracing entry.
 	 * 
 	 * @return A code-tracing entry with full details or an error message
 	 *         explaining the failure
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTrace codeTracingCreate(final String url) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_CREATE, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodeTracingCreateRequest createRequest = (CodeTracingCreateRequest) request;
 						createRequest.setUrl(url);
 					}
 				});
 		return (CodeTrace) handle.getData();
 	}
 
 	/**
 	 * Delete a code-tracing entry.
 	 * 
 	 * @return A code-tracing entry with full details or an error message
 	 *         explaining the failure
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTrace codeTracingDelete(final String traceFile)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_DELETE, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodeTracingDeleteRequest deleteRequest = (CodeTracingDeleteRequest) request;
 						deleteRequest.setTraceFile(traceFile);
 					}
 				});
 		return (CodeTrace) handle.getData();
 	}
 
 	/**
 	 * Retrieve a list of code-tracing files available for download using
 	 * codetracingDownloadTraceFile.
 	 * 
 	 * @return list of code tracies
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTracingList codeTracingList(final Integer limit,
 			final Integer offset, final String orderBy, final String direction,
 			final String... applications) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_LIST, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodeTracingListRequest listRequest = (CodeTracingListRequest) request;
 						if (limit != null) {
 							listRequest.setLimit(limit);
 						}
 						if (offset != null) {
 							listRequest.setOffset(offset);
 						}
 						if (orderBy != null) {
 							listRequest.setOrderBy(orderBy);
 						}
 						if (direction != null) {
 							listRequest.setDirection(direction);
 						}
 						if (applications != null) {
 							listRequest.setApplications(applications);
 						}
 					}
 				});
 		return (CodeTracingList) handle.getData();
 	}
 
 	/**
 	 * Download the amf file specified by codetracing identifier. This action
 	 * used to be named monitorDownloadAmf, however this action was completely
 	 * replaced by the new codetracingDownloadTraceFile action.
 	 * MonitorDownloadAmf was completely removed and will not be accessible in
 	 * WebAPI 1.2.
 	 * 
 	 * @return code trace file
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public CodeTraceFile codeTracingDownloadTraceFile(final String traceFile)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.CODE_TRACING_DOWNLOAD_TRACE_FILE,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						CodetracingDownloadTraceFileRequest fileRequest = (CodetracingDownloadTraceFileRequest) request;
 						fileRequest.setTraceFile(traceFile);
 					}
 				});
 		return (CodeTraceFile) handle.getData();
 	}
 
 	/**
 	 * Retrieve information about a particular request's events and code
 	 * tracing. The requestUid identifier is provided in a cookie that is set in
 	 * the response to the particular request. This API action is designed to be
 	 * used with the new Studio browser toolbar.
 	 * <p>
 	 * Limitations:
 	 * </p>
 	 * <p>
 	 * This action explicitly does not work on Zend Server 5.6.0 for IBMi.
 	 * </p>
 	 * 
 	 * @return request summary
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public RequestSummary monitorGetRequestSummary(final String requestUid)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_GET_REQUEST_SUMMARY,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorGetRequestSummaryRequest monitorRequest = (MonitorGetRequestSummaryRequest) request;
 						monitorRequest.setRequestUid(requestUid);
 					}
 				});
 		return (RequestSummary) handle.getData();
 	}
 
 	/**
 	 * Retrieve a list of monitor issues according to a preset filter
 	 * identifier. The filter identifier is shared with the UI's predefined
 	 * filters. This WebAPI method may also accept ordering details and paging
 	 * limits. The response is a list of issue elements with their general
 	 * details and event-groups identifiers.
 	 * 
 	 * @return list of issues
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public IssueList monitorGetIssuesListPredefinedFilter(
 			final String filterId, final Integer limit, final Integer offset,
 			final String order, final String direction) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_GET_ISSUES_LIST_PREDEFINED_FILTER,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorGetIssuesListPredefinedFilterRequest filterRequest = (MonitorGetIssuesListPredefinedFilterRequest) request;
 						filterRequest.setFilterId(filterId);
 						if (limit != null) {
 							filterRequest.setLimit(limit);
 						}
 						if (offset != null) {
 							filterRequest.setOffset(offset);
 						}
 						if (order != null) {
 							filterRequest.setOrder(order);
 						}
 						if (direction != null) {
 							filterRequest.setDirection(direction);
 						}
 					}
 				});
 		return (IssueList) handle.getData();
 	}
 
 	/**
 	 * Retrieve an issue's details according to the issueId passed as a
 	 * parameter. Additional information about event groups is also displayed.
 	 * The response is a list of issue elements with their general details and
 	 * event-groups identifiers.
 	 * 
 	 * 
 	 * @return issue details
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public IssueDetails monitorGetIssueDetails(final int issueId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_GET_ISSUE_DETAILS, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorGetIssueDetailsRequest monitorRequest = (MonitorGetIssueDetailsRequest) request;
 						monitorRequest.setIssueId(issueId);
 					}
 				});
 		return (IssueDetails) handle.getData();
 	}
 
 	/**
 	 * Retrieve an issue's details according to the issueId passed as a
 	 * parameter. Additional information about event groups is also displayed.
 	 * The response is a list of issue elements with their general details and
 	 * event-groups identifiers.
 	 * 
 	 * 
 	 * @return request summary
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public EventsGroupDetails monitorGetEventGroupDetails(final String issueId,
 			final int eventGroupId) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_GET_EVENT_GROUP_DETAILS,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorGetEventGroupDetailsRequest monitorRequest = (MonitorGetEventGroupDetailsRequest) request;
 						monitorRequest.setIssueId(issueId);
 						monitorRequest.setEventGroupId(eventGroupId);
 					}
 				});
 		return (EventsGroupDetails) handle.getData();
 	}
 
 	/**
 	 * Download the amf file specified by codetracing identifier. This action
 	 * used to be named monitorDownloadAmf, however this action was completely
 	 * replaced by the new codetracingDownloadTraceFile action.
 	 * MonitorDownloadAmf was completely removed and will not be accessible in
 	 * WebAPI 1.2.
 	 * 
 	 * @return issue file
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public IssueFile monitorExportIssueByEventsGroup(final int eventGroupId)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_EXPORT_ISSUE_BY_EVENTS_GROUP,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorExportIssueByEventsGroupRequest fileRequest = (MonitorExportIssueByEventsGroupRequest) request;
 						fileRequest.setEventGroupId(eventGroupId);
 					}
 				});
 		return (IssueFile) handle.getData();
 	}
 
 	/**
 	 * Modify an Issue's status code based on an Issue's Id and a status code.
 	 * Response is an issue element's updated details.
 	 * 
 	 * @return updated issue
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public Issue monitorChangeIssueStatus(final int issueId,
 			final IssueStatus newStatus) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.MONITOR_CHANGE_ISSUE_STATUS,
 				WebApiVersion.V1_2, new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						MonitorChangeIssueStatusRequest changeRequest = (MonitorChangeIssueStatusRequest) request;
 						changeRequest.setIssueId(issueId);
 						changeRequest.setNewStatus(newStatus);
 					}
 				});
 		return (Issue) handle.getData();
 	}
 
 	/**
 	 * Start a debug session for specific issue.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public DebugRequest studioStartDebug(final String eventsGroupId,
 			final Boolean noRemote, final String overrideHost)
 			throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.STUDIO_START_DEBUG, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						StudioStartDebugRequest debugRequest = (StudioStartDebugRequest) request;
 						debugRequest.setEventsGroupId(eventsGroupId);
 						if (noRemote != null) {
 							debugRequest.setNoRemote(noRemote);
 						}
 						if (overrideHost != null) {
 							debugRequest.setOverrideHost(overrideHost);
 						}
 					}
 				});
 		return (DebugRequest) handle.getData();
 	}
 
 	/**
 	 * Start a debug session for specific issue.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public DebugRequest studioStartDebug(final String eventsGroupId,
 			final Boolean noRemote) throws WebApiException {
 		return studioStartDebug(eventsGroupId, noRemote, null);
 	}
 
 	/**
 	 * Start a debug session for specific issue.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public DebugRequest studioStartDebug(final String eventsGroupId,
 			final String overrideHost) throws WebApiException {
 		return studioStartDebug(eventsGroupId, null, overrideHost);
 	}
 
 	/**
 	 * Start a debug session for specific issue.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public DebugRequest studioStartDebug(final String eventsGroupId)
 			throws WebApiException {
 		return studioStartDebug(eventsGroupId, null, null);
 	}
 
 	/**
 	 * Start a profiling session with Zend Studio's integration using an
 	 * event-group's identifier. This action has the peculiar behavior of being
 	 * synchronous and hanging until the profiling session is completed.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public ProfileRequest studioStartProfile(final String eventsGroupId,
 			final String overrideHost) throws WebApiException {
 		final IResponse handle = this.handle(
 				WebApiMethodType.STUDIO_START_PROFILE, WebApiVersion.V1_2,
 				new IRequestInitializer() {
 
 					public void init(IRequest request) throws WebApiException {
 						StudioStartProfileRequest profileRequest = (StudioStartProfileRequest) request;
 						profileRequest.setEventsGroupId(eventsGroupId);
 						if (overrideHost != null) {
 							profileRequest.setOverrideHost(overrideHost);
 						}
 					}
 				});
 		return (ProfileRequest) handle.getData();
 	}
 
 	/**
 	 * Start a profiling session with Zend Studio's integration using an
 	 * event-group's identifier. This action has the peculiar behavior of being
 	 * synchronous and hanging until the profiling session is completed.
 	 * 
 	 * @return debug request
 	 * @throws WebApiException
 	 * @since 1.2
 	 */
 	public ProfileRequest studioStartProfile(final String eventsGroupId)
 			throws WebApiException {
 		return studioStartProfile(eventsGroupId, null);
 	}
 
 	/**
 	 * Zend Server Web API is intended to allow automation of the management and
 	 * deployment of Zend Server and Zend Server Cluster Manager, and allow
 	 * integration with other Zend or 3rd party software. Call a specific
 	 * service method
 	 * 
 	 * @param methodType
 	 *            the method to be called
 	 * @param initializer
 	 *            initializer of this request
 	 * @return the response object
 	 * @throws WebApiException
 	 */
 	public IResponse handle(WebApiMethodType methodType,
 			IRequestInitializer initializer) throws WebApiException {
 		return handle(methodType, DEFAULT_VERSION, initializer);
 	}
 
 	/**
 	 * Zend Server Web API is intended to allow automation of the management and
 	 * deployment of Zend Server and Zend Server Cluster Manager, and allow
 	 * integration with other Zend or 3rd party software. Call a specific
 	 * service method
 	 * 
 	 * @param methodType
 	 *            the method to be called
 	 * @param WebAPI
 	 *            client API version
 	 * @param initializer
 	 *            initializer of this request
 	 * @return the response object
 	 * @throws WebApiException
 	 */
 	public IResponse handle(WebApiMethodType methodType, WebApiVersion version,
 			IRequestInitializer initializer) throws WebApiException {
 
 		// create request
 		IRequest request = RequestFactory.createRequest(methodType, version,
 				new Date(), this.credentials.getKeyName(),
 				this.clientConfiguration.getUserAgent(),
 				getWebApiAddress(this.clientConfiguration.getHost()),
 				this.credentials.getSecretKey());
 
 		if (initializer != null) {
 			initializer.init(request);
 		}
 
 		// apply request
 		IServiceDispatcher dispatcher = new ServiceDispatcher(context);
 		IResponse response = dispatcher.dispatch(request);
 
 		// return response data to caller
 		return response;
 	}
 
 	private final String getWebApiAddress(URL host) {
 		String hostname = host.toString();
 		if (host.getPort() == -1) {
 			if ("https".equalsIgnoreCase(host.getProtocol())) {
 				hostname += ":10082";
 			} else {
 				hostname += ":10081";
 			}
 		}
 		return hostname;
 	}
 
 }
 
