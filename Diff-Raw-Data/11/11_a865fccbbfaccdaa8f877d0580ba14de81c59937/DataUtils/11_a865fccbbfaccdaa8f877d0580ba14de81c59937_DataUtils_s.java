 package org.zend.webapi.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.ApplicationServer;
 import org.zend.webapi.core.connection.data.ApplicationServers;
 import org.zend.webapi.core.connection.data.ApplicationsList;
 import org.zend.webapi.core.connection.data.Backtrace;
 import org.zend.webapi.core.connection.data.CodeTrace;
 import org.zend.webapi.core.connection.data.CodeTracingList;
 import org.zend.webapi.core.connection.data.CodeTracingStatus;
 import org.zend.webapi.core.connection.data.DebugRequest;
 import org.zend.webapi.core.connection.data.DeployedVersion;
 import org.zend.webapi.core.connection.data.DeployedVersions;
 import org.zend.webapi.core.connection.data.Event;
 import org.zend.webapi.core.connection.data.EventsGroup;
 import org.zend.webapi.core.connection.data.EventsGroupDetails;
 import org.zend.webapi.core.connection.data.EventsGroups;
 import org.zend.webapi.core.connection.data.GeneralDetails;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.core.connection.data.IResponseData.ResponseType;
 import org.zend.webapi.core.connection.data.Issue;
 import org.zend.webapi.core.connection.data.IssueDetails;
 import org.zend.webapi.core.connection.data.IssueList;
 import org.zend.webapi.core.connection.data.LicenseInfo;
 import org.zend.webapi.core.connection.data.MessageList;
 import org.zend.webapi.core.connection.data.Parameter;
 import org.zend.webapi.core.connection.data.ParameterList;
 import org.zend.webapi.core.connection.data.ProfileRequest;
 import org.zend.webapi.core.connection.data.RequestSummary;
 import org.zend.webapi.core.connection.data.RouteDetail;
 import org.zend.webapi.core.connection.data.RouteDetails;
 import org.zend.webapi.core.connection.data.ServerInfo;
 import org.zend.webapi.core.connection.data.ServersList;
 import org.zend.webapi.core.connection.data.Step;
 import org.zend.webapi.core.connection.data.SuperGlobals;
 import org.zend.webapi.core.connection.data.SystemInfo;
 import org.zend.webapi.core.connection.data.values.ApplicationStatus;
 import org.zend.webapi.core.connection.data.values.LicenseInfoStatus;
 import org.zend.webapi.core.connection.data.values.ServerStatus;
 import org.zend.webapi.core.connection.data.values.SystemEdition;
 import org.zend.webapi.core.connection.data.values.SystemStatus;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 
 public class DataUtils {
 
 	private static List<String> componentStatus = new ArrayList<String>();
 
 	static {
 		componentStatus.add("Active");
 		componentStatus.add("Inactive");
 	}
 
 	public static void checkValidServersList(ServersList serversList) {
 		Assert.assertNotNull(serversList);
 		List<ServerInfo> servers = serversList.getServerInfo();
 		for (ServerInfo serverInfo : servers) {
 			checkValidServerInfo(serverInfo);
 		}
 	}
 
 	public static void checkValidClusterServerStatus(
 			ServersList clusterServerStatus) {
 		Assert.assertNotNull(clusterServerStatus);
 		Assert.assertEquals(IResponseData.ResponseType.SERVERS_LIST,
 				clusterServerStatus.getType());
 		List<ServerInfo> servers = clusterServerStatus.getServerInfo();
 		for (ServerInfo serverInfo : servers) {
 			checkValidServerInfo(serverInfo);
 		}
 	}
 
 	public static void checkValidServerInfo(ServerInfo serverInfo) {
 		Assert.assertNotNull(serverInfo);
 		Assert.assertTrue(Integer.valueOf(serverInfo.getId()) instanceof Integer);
 		Assert.assertNotNull(serverInfo.getAddress());
 		Assert.assertNotNull(serverInfo.getName());
 		Assert.assertNotSame(ServerStatus.UNKNOWN, serverInfo.getStatus());
 		checkValidMessageList(serverInfo.getMessageList());
 	}
 
 	public static void checkValidSystemInfo(SystemInfo systemInfo) {
 		Assert.assertNotNull(systemInfo);
 		checkValidEdition(systemInfo.getEdition());
 		checkValidLicenceInfo(systemInfo.getLicenseInfo());
 		checkValidLicenceInfo(systemInfo.getManagerLicenseInfo());
 		Assert.assertNotNull(systemInfo.getOperatingSystem());
 		Assert.assertNotNull(systemInfo.getPhpVersion());
 		checkValidSystemStatus(systemInfo.getStatus());
 		checkValidApiVersions(systemInfo.getSupportedApiVersions());
 		Assert.assertNotNull(systemInfo.getVersion());
 		checkValidMessageList(systemInfo.getMessageList());
 	}
 
 	public static void checkValidApiVersions(
 			List<WebApiVersion> supportedApiVersions) {
 		Assert.assertNotNull(supportedApiVersions);
 		for (WebApiVersion webApiVersion : supportedApiVersions) {
 			Assert.assertNotNull(webApiVersion.getFullName());
 			Assert.assertNotNull(webApiVersion.getVersionName());
 		}
 	}
 
 	public static void checkValidSystemStatus(SystemStatus status) {
 		Assert.assertNotSame(SystemStatus.UNKNOWN,
 				SystemStatus.byName(status.getTitle()));
 	}
 
 	public static void checkValidLicenceInfo(LicenseInfo licenseInfo) {
 		Assert.assertNotNull(licenseInfo);
 		LicenseInfoStatus status = licenseInfo.getStatus();
 		Assert.assertNotSame(LicenseInfoStatus.UNKNOWN,
 				LicenseInfoStatus.byName(status.getName()));
 		if (status != LicenseInfoStatus.EXPIRED) {
 			Assert.assertNotNull(licenseInfo.getOrderNumber());
 			// Assert.assertNotNull(licenseInfo.getValidUntil());
 			Assert.assertTrue(licenseInfo.getServerLimit() >= 0);
 		}
 	}
 
 	public static void checkValidEdition(SystemEdition edition) {
 		Assert.assertNotNull(edition);
 		boolean isCorrect = edition == SystemEdition.ZEND_SERVER
 				|| edition == SystemEdition.ZEND_SERVER_CLUSER_MANAGER
 				|| edition == SystemEdition.ZEND_SERVER_COMMUNITY_EDITION;
 		Assert.assertTrue(isCorrect);
 	}
 
 	public static void checkValidMessageList(MessageList messageList) {
 		Assert.assertNotNull(messageList);
 		List<String> errors = messageList.getError();
 		if (errors != null) {
 			for (String error : errors) {
 				Assert.assertNotNull(error);
 			}
 		}
 		List<String> infoList = messageList.getInfo();
 		if (infoList != null) {
 			for (String info : infoList) {
 				Assert.assertNotNull(info);
 			}
 		}
 		List<String> warnings = messageList.getWarning();
 		if (warnings != null) {
 			for (String warining : warnings) {
 				Assert.assertNotNull(warining);
 			}
 		}
 	}
 
 	public static void checkValidApplicationsList(
 			ApplicationsList applicationsList) {
 		Assert.assertNotNull(applicationsList);
 		List<ApplicationInfo> appsInfo = applicationsList.getApplicationsInfo();
 		for (ApplicationInfo applicationInfo : appsInfo) {
 			checkValidApplicationInfo(applicationInfo);
 		}
 	}
 
 	public static void checkValidApplicationInfo(ApplicationInfo applicationInfo) {
 		Assert.assertNotNull(applicationInfo);
 		Assert.assertNotNull(applicationInfo.getAppName());
 		Assert.assertNotNull(applicationInfo.getUserAppName());
 		Assert.assertNotNull(applicationInfo.getBaseUrl());
 		Assert.assertNotNull(applicationInfo.getInstalledLocation());
 		Assert.assertNotSame(ApplicationStatus.UNKNOWN,
 				ApplicationStatus.byName(applicationInfo.getStatus().getName()));
 		if (applicationInfo.getMessageList() != null) {
 			checkValidMessageList(applicationInfo.getMessageList());
 		}
 		if (applicationInfo.getDeployedVersions() != null) {
 			checkValidDeployedVersions(applicationInfo.getDeployedVersions());
 		}
 		if (applicationInfo.getServers() != null) {
 			checkValidApplicationServers(applicationInfo.getServers());
 		}
 	}
 
 	public static void checkValidApplicationServers(ApplicationServers servers) {
 		Assert.assertNotNull(servers);
 		List<ApplicationServer> serversList = servers.getApplicationServers();
 		for (ApplicationServer server : serversList) {
 			checkValidApplicationServer(server);
 		}
 	}
 
 	public static void checkValidApplicationServer(ApplicationServer server) {
 		Assert.assertNotNull(server);
 		Assert.assertNotNull(server.getDeployedVersion());
 		Assert.assertNotSame(ApplicationStatus.UNKNOWN,
 				ApplicationStatus.byName(server.getStatus().getName()));
 	}
 
 	public static void checkValidDeployedVersions(DeployedVersions versionsList) {
 		Assert.assertNotNull(versionsList);
 		List<DeployedVersion> versions = versionsList.getDeployedVersions();
 		if (versions != null) {
 			for (DeployedVersion versionInfo : versions) {
 				checkValidDeployedVersion(versionInfo);
 			}
 		}
 	}
 
 	public static void checkValidDeployedVersion(
 			DeployedVersion deployedVersionInfo) {
 		Assert.assertNotNull(deployedVersionInfo);
 		Assert.assertNotNull(deployedVersionInfo.getVersion());
 	}
 
 	// TODO check if use On|Off or 1|0
 	public static void checkValidCodeTracingStatus(CodeTracingStatus status) {
 		Assert.assertNotNull(status);
 		Assert.assertEquals(ResponseType.CODE_TRACING_STATUS, status.getType());
		Assert.assertNotNull(status.getAlwaysDump());
 		Assert.assertNotNull(status.getAwaitsRestart());
 		Assert.assertTrue(componentStatus.contains(status.getComponentStatus()));
 		Assert.assertNotNull(status.getDeveloperMode());
 		Assert.assertNotNull(status.getTraceEnabled());
 	}
 
 	public static void checkValidCodeTrace(CodeTrace trace) {
 		Assert.assertNotNull(trace);
 		Assert.assertEquals(ResponseType.CODE_TRACE, trace.getType());
 		Assert.assertNotNull(trace.getApplicationId());
 		Assert.assertNotNull(trace.getCreatedBy());
 		Assert.assertNotNull(trace.getDate());
 		Assert.assertNotNull(trace.getId());
 		Assert.assertNotNull(trace.getUrl());
 	}
 
 	public static void checkValidCodeTracingList(CodeTracingList traces) {
 		Assert.assertNotNull(traces);
 		Assert.assertEquals(ResponseType.CODE_TRACING_LIST, traces.getType());
 		List<CodeTrace> tracesList = traces.getTraces();
		Assert.assertNotNull(tracesList);
		for (CodeTrace codeTrace : tracesList) {
			checkValidCodeTrace(codeTrace);
 		}
 	}
 
 	public static void checkValidRequestSummary(RequestSummary summary) {
 		Assert.assertNotNull(summary);
 		Assert.assertEquals(ResponseType.REQUEST_SUMMARY, summary.getType());
 		Assert.assertNotNull(summary.getEventsCount());
 		Assert.assertNotNull(summary.getCodeTracing());
 		List<Event> events = summary.getEvents().getEvents();
 		if (events != null) {
 			for (Event event : events) {
 				checkValidEvent(event);
 			}
 		}
 	}
 
 	public static void checkValidEvent(Event event) {
 		Assert.assertNotNull(event);
 		Assert.assertEquals(ResponseType.EVENT, event.getType());
 		Assert.assertNotNull(event.getDescription());
 		Assert.assertNotNull(event.getEventsGroupId());
 		Assert.assertNotNull(event.getEventType());
 		Assert.assertNotNull(event.getSeverity());
 		checkValidBacktrace(event.getBacktrace());
 		checkValidSuperGlobals(event.getSuperGlobals());
 	}
 
 	public static void checkValidBacktrace(Backtrace backtrace) {
 		Assert.assertNotNull(backtrace);
 		Assert.assertEquals(ResponseType.BACKTRACE, backtrace.getType());
 		List<Step> steps = backtrace.getSteps();
 		for (Step step : steps) {
 			checkValidStep(step);
 		}
 	}
 
 	public static void checkValidStep(Step step) {
 		Assert.assertNotNull(step);
 		Assert.assertEquals(ResponseType.STEP, step.getType());
 		Assert.assertNotNull(step.getClassId());
 		Assert.assertNotNull(step.getFile());
 		Assert.assertNotNull(step.getFunction());
 		Assert.assertNotNull(step.getObjectId());
 	}
 
 	public static void checkValidSuperGlobals(SuperGlobals superGlobals) {
 		Assert.assertNotNull(superGlobals);
 		Assert.assertEquals(ResponseType.SUPER_GLOBALS, superGlobals.getType());
 		checkValidParametersList(superGlobals.getGet());
 		checkValidParametersList(superGlobals.getCookie());
 		checkValidParametersList(superGlobals.getPost());
 		checkValidParametersList(superGlobals.getServer());
 		checkValidParametersList(superGlobals.getSession());
 	}
 
 	public static void checkValidParametersList(ParameterList list) {
 		Assert.assertNotNull(list);
 		Assert.assertEquals(ResponseType.PARAMETER_LIST, list.getType());
 		List<Parameter> parameters = list.getParameters();
 		if (parameters != null) {
 			for (Parameter parameter : parameters) {
 				Assert.assertNotNull(parameter);
 				Assert.assertEquals(ResponseType.PARAMETER, parameter.getType());
 				Assert.assertNotNull(parameter.getName());
 				Assert.assertNotNull(parameter.getValue());
 			}
 		}
 	}
 
 	public static void checkValidIssueList(IssueList issueList) {
 		Assert.assertNotNull(issueList);
 		Assert.assertEquals(ResponseType.ISSUE_LIST, issueList.getType());
 		List<Issue> issues = issueList.getIssues();
 		for (Issue issue : issues) {
 			checkValidIssue(issue);
 		}
 	}
 
 	public static void checkValidIssue(Issue issue) {
 		Assert.assertNotNull(issue);
 		Assert.assertEquals(ResponseType.ISSUE, issue.getType());
 		Assert.assertNotNull(issue.getRule());
 		Assert.assertNotNull(issue.getSeverity());
 		Assert.assertNotNull(issue.getStatus());
 		checkValidRouteDetails(issue.getRouteDetails());
 		checkValidGeneralDetails(issue.getGeneralDetails());
 	}
 
 	public static void checkValidGeneralDetails(GeneralDetails generalDetails) {
 		Assert.assertNotNull(generalDetails);
 		Assert.assertEquals(ResponseType.GENERAL_DETAILS,
 				generalDetails.getType());
 		Assert.assertNotNull(generalDetails.getAggregationHint());
 		Assert.assertNotNull(generalDetails.getErrorString());
 		Assert.assertNotNull(generalDetails.getErrorType());
 		Assert.assertNotNull(generalDetails.getFunction());
 		Assert.assertNotNull(generalDetails.getSourceFile());
 		Assert.assertNotNull(generalDetails.getUrl());
 	}
 
 	public static void checkValidRouteDetails(RouteDetails routeDetails) {
 		Assert.assertNotNull(routeDetails);
 		Assert.assertEquals(ResponseType.ROUTE_DETAILS, routeDetails.getType());
 		List<RouteDetail> details = routeDetails.getDetails();
 		if (details != null) {
 			for (RouteDetail routeDetail : details) {
 				checkValidRouteDetail(routeDetail);
 			}
 		}
 	}
 
 	public static void checkValidRouteDetail(RouteDetail routeDetail) {
 		Assert.assertNotNull(routeDetail);
 		Assert.assertEquals(ResponseType.ROUTE_DETAIL, routeDetail.getType());
 		Assert.assertNotNull(routeDetail.getKey());
 		Assert.assertNotNull(routeDetail.getValue());
 	}
 
 	public static void checkValidIssueDetails(IssueDetails issueDetails) {
 		Assert.assertNotNull(issueDetails);
 		Assert.assertEquals(ResponseType.ISSUE_DETAILS, issueDetails.getType());
 		checkValidIssue(issueDetails.getIssue());
 		checkValidEventsGroups(issueDetails.getEventsGroups());
 	}
 
 	public static void checkValidEventsGroups(EventsGroups eventsGroups) {
 		Assert.assertNotNull(eventsGroups);
 		Assert.assertEquals(ResponseType.EVENTS_GROUPS, eventsGroups.getType());
 		List<EventsGroup> groups = eventsGroups.getGroups();
 		if (groups != null) {
 			for (EventsGroup eventsGroup : groups) {
 				checValidEventsGroup(eventsGroup);
 			}
 		}
 	}
 
 	public static void checValidEventsGroup(EventsGroup eventsGroup) {
 		Assert.assertNotNull(eventsGroup);
 		Assert.assertEquals(ResponseType.EVENTS_GROUP, eventsGroup.getType());
 		Assert.assertNotNull(eventsGroup.getClassId());
 		Assert.assertNotNull(eventsGroup.getEventsGroupId());
 		Assert.assertNotNull(eventsGroup.getJavaBacktrace());
 		Assert.assertNotNull(eventsGroup.getLoad());
 		Assert.assertNotNull(eventsGroup.getUserData());
 	}
 
 	public static void checkValidEventsGroupDetails(EventsGroupDetails details) {
 		Assert.assertNotNull(details);
 		Assert.assertEquals(ResponseType.EVENTS_GROUP_DETAILS,
 				details.getType());
 		Assert.assertNotNull(details.getCodeTracing());
 		checkValidEvent(details.getEvent());
 		checValidEventsGroup(details.getEventsGroup());
 	}
 
 	public static void checkValidDebugRequest(DebugRequest debugRequest) {
 		Assert.assertNotNull(debugRequest);
 		Assert.assertEquals(ResponseType.DEBUG_REQUEST, debugRequest.getType());
 		Assert.assertNotNull(debugRequest.getSuccess());
 		Assert.assertNotNull(debugRequest.getMessage());
 	}
 
 	public static void checkValidProfileRequest(ProfileRequest profileRequest) {
 		Assert.assertNotNull(profileRequest);
 		Assert.assertEquals(ResponseType.PROFILE_REQUEST,
 				profileRequest.getType());
 		Assert.assertNotNull(profileRequest.getSuccess());
 		Assert.assertNotNull(profileRequest.getMessage());
 	}
 
 }
