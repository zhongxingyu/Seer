 package org.exoplatform.services.rest.bonita;
 
 import org.bonitasoft.engine.api.IdentityAPI;
 import org.bonitasoft.engine.api.ProcessManagementAPI;
 import org.bonitasoft.engine.api.ProcessRuntimeAPI;
 import org.bonitasoft.engine.api.TenantAPIAccessor;
 import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
 import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
 import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
 import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
 import org.bonitasoft.engine.exception.*;
 import org.bonitasoft.engine.identity.*;
 import org.bonitasoft.engine.platform.LoginException;
 import org.bonitasoft.engine.search.SearchResult;
 import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
 import org.bonitasoft.engine.session.APISession;
 import org.bonitasoft.engine.session.InvalidSessionException;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.rest.resource.ResourceContainer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Romain Denarie
  * Date: 02/10/13
  * Time: 09:18
  * To change this template use File | Settings | File Templates.
  */
 
 @Path("bonitaService")
 public class BonitaService implements ResourceContainer {
 
 	private static Log logger = ExoLogger.getLogger(BonitaService.class);
 
 
 
 
 	/** The Constant LAST_MODIFIED_PROPERTY. */
 	private static final String LAST_MODIFIED_PROPERTY = "Last-Modified";
 
 	/** The Constant IF_MODIFIED_SINCE_DATE_FORMAT. */
 	private static final String IF_MODIFIED_SINCE_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
 
 
 
 
 	public static final int MAX_HOST_CONNECTIONS = 20;
 	public static final String REST_USER = System.getProperty("org.exoplatform.bonita.systemuser", "restuser").trim();
 	public static final String REST_PASS = System.getProperty("org.exoplatform.bonita.systempassword", "restbpm").trim();
 	public static final int PORT = Integer.parseInt(System.getProperty("org.exoplatform.bonita.port", "8080").trim());
 	public static final String HOST = System.getProperty("org.exoplatform.bonita.host", "localhost").trim();
 	private static final String DEFAULT_USER_PASSWORD = System.getProperty("org.exoplatform.bonita.default.password", "!p@ssw0rd!").trim();
	private static final String DEFAULT_GROUP = System.getProperty("org.exoplatform.bonita.default.group", "consulting").trim();
 	private static final String DEFAULT_ROLE = System.getProperty("org.exoplatform.bonita.default.role", "member").trim();
 
 	private APISession session = null;
 
 
 	private OrganizationService organizationService_;
 
 	public BonitaService(OrganizationService organizationService) {
 		this.organizationService_ = organizationService;
 	}
 
 
 	@GET
 	@Path("getProcessList")
 	public Response getProcessList(@Context HttpServletRequest request) throws IOException {
 
 		String username = request.getRemoteUser();
 
 		try {
 
 			if (isExpired(session)) {
 				session=TenantAPIAccessor.getLoginAPI().login(REST_USER,REST_PASS);
 			}
 			User user = getUser(username);
 
 			//getProcessList
 			ProcessManagementAPI processManagementAPI = TenantAPIAccessor.getProcessAPI(session);
 
 			Set<Long> usersIdSet = new HashSet<Long>();
 			usersIdSet.add(user.getId());
 			List<ProcessDeploymentInfo> processDeploymentInfos = processManagementAPI.getProcessDeploymentInfos(0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
 
 
 			SearchResult<ProcessDeploymentInfo> processDeploymentInfoSearchResult = processManagementAPI.searchProcessDeploymentInfosUsersManagedByCanStart(user.getId(), new SearchOptionsImpl(0, 10));
 
 			if (logger.isDebugEnabled()) {
 				logger.debug(processDeploymentInfos.size()+" processes.");
 				logger.debug("User "+username+" can starts "+processDeploymentInfoSearchResult.getCount()+" processes.");
 
 			}
 
 			List<ProcessDeploymentInfo> process = processDeploymentInfoSearchResult.getResult();
 
 
 			//in javascript, long are rounded => we lost id like processId information
 			//So we return an object ExoProcessDeploymentInfo, which contains ProcessDeploymentInfo and
 			//extract long for store it in String
 
 			List<ExoProcessDeploymentInfo> eXoProcess = new ArrayList<ExoProcessDeploymentInfo>();
 			for (ProcessDeploymentInfo pdi : process) {
 				eXoProcess.add(new ExoProcessDeploymentInfo(pdi, HOST, PORT,username));
 			}
 
 			DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
 			return Response.ok(eXoProcess,MediaType.APPLICATION_JSON_TYPE).header(LAST_MODIFIED_PROPERTY, dateFormat.format(new Date())).build();
 
 
 
 		} catch (UnknownAPITypeException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 
 		} catch (SearchException e) {
 			logger.error("Error when searching : "+e.getMessage());
 			e.printStackTrace();
 
 		} catch (BonitaHomeNotSetException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 		} catch (ServerAPIException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 		} catch (LoginException e) {
 			logger.error("Unable to log on bonita : "+e.getMessage());
 			e.printStackTrace();
 		} catch (AlreadyExistsException e) {
 			logger.error("Unable to create user on bonita. He already exists : "+e.getMessage());
 			e.printStackTrace();
 		} catch (CreationException e) {
 			logger.error("Error when creating user in bonita : "+e.getMessage());
 			e.printStackTrace();
 		}
 
 		return Response.serverError().build();
 
 	}
 
 	@GET
 	@Path("getTaskList")
 	public Response getTaskList(@Context HttpServletRequest request) throws IOException {
 
 
 		String username = request.getRemoteUser();
 
 		try {
 
 			if (isExpired(session)) {
 				session=TenantAPIAccessor.getLoginAPI().login(REST_USER,REST_PASS);
 			}
 			User user = getUser(username);
 
 			//getTaskList
 			ProcessRuntimeAPI processRuntimeAPI = TenantAPIAccessor.getProcessAPI(session);
 			List<HumanTaskInstance> assignedHumanTaskInstances = processRuntimeAPI.getAssignedHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
 
 
 			if (logger.isDebugEnabled()) {
 				logger.debug("User "+username+" can do "+assignedHumanTaskInstances.size()+" tasks.");
 
 			}
 
 			//in javascript, long are rounded => we lost id like processId information
 			//So we return an object ExoProcessDeploymentInfo, which contains ProcessDeploymentInfo and
 			//extract long for store it in String
 			List<ExoHumanTaskInstance> eXohumanTasks = new ArrayList<ExoHumanTaskInstance>();
 			for (HumanTaskInstance hti : assignedHumanTaskInstances) {
 				eXohumanTasks.add(new ExoHumanTaskInstance(hti, HOST,PORT,username));
 			}
 
 
 			DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
 			return Response.ok(eXohumanTasks,MediaType.APPLICATION_JSON_TYPE).header(LAST_MODIFIED_PROPERTY, dateFormat.format(new Date())).build();
 
 		} catch (UnknownAPITypeException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 
 		} catch (BonitaHomeNotSetException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 		} catch (ServerAPIException e) {
 			logger.error("Error in getProcessList : "+e.getMessage());
 			e.printStackTrace();
 		} catch (LoginException e) {
 			logger.error("Unable to log on bonita : "+e.getMessage());
 			e.printStackTrace();
 		} catch (AlreadyExistsException e) {
 			logger.error("Unable to create user on bonita. He already exists : "+e.getMessage());
 			e.printStackTrace();
 		} catch (CreationException e) {
 			logger.error("Error when creating user in bonita : "+e.getMessage());
 			e.printStackTrace();
 		}
 
 
 		return Response.serverError().build();
 
 	}
 
 	private User getUser(String username) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, CreationException, LoginException {
 		IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
 		boolean addUser = false;
 		User user = null;
 
 		try {
 			user = identityAPI.getUserByUserName(username);
 			if (logger.isDebugEnabled()) {
 				logger.debug("User "+username+" exists in Bonita.");
 			}
 		} catch (UserNotFoundException e) {
 			if (logger.isDebugEnabled()) {
 				logger.debug("User "+username+" does'nt exist in Bonita => add it");
 			}
 			addUser=true;
 		} catch (InvalidSessionException e) {
 			//this catch error when session is not ok (for example bonita server has restart without exo restart
 			//so create a new session and return getUser
 			session=TenantAPIAccessor.getLoginAPI().login(REST_USER,REST_PASS);
 			return getUser(username);
 
 
 		}
 
 		if (addUser) {
 			String firstname="";
 			String lastname ="";
 			try {
 				org.exoplatform.services.organization.User eXoUser = organizationService_.getUserHandler().findUserByName(username);
 				firstname = eXoUser.getFirstName();
 				lastname = eXoUser.getLastName();
 			} catch (Exception e) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("User not founded in eXo user database. Should not arrived because we are already connected with this user");
 				}
 			}
 
 			user = identityAPI.createUser(username, DEFAULT_USER_PASSWORD, firstname,lastname);
 
 			try {
 				Group defaultGroup = identityAPI.getGroupByPath(DEFAULT_GROUP);
 				Role defaultRole = identityAPI.getRoleByName(DEFAULT_ROLE);
 
 				identityAPI.addUserMembership(user.getId(), defaultGroup.getId(),defaultRole.getId());
 
 			} catch (GroupNotFoundException e) {
 				logger.debug("Default Group not founded on Bonita : "+DEFAULT_GROUP);
 				e.printStackTrace();
 			} catch (RoleNotFoundException e) {
 				logger.debug("Default Role not founded on Bonita : "+DEFAULT_ROLE);
 				e.printStackTrace();
 			}
 
 
 		}
 		return user;
 	}
 
 	private boolean isExpired(APISession session) {
 		//La session peut ne pas avoir expirer et etre invalide (par exemple si le serveur bonita a restart)
 		//TODO traiter ce cas.
 
 		if (session!=null) {
 			Date creationDate = session.getCreationDate();
 			long duration = session.getDuration();
 			Date endOfSession = new Date(creationDate.getTime() + duration + 60000);
 			//je considere que la session est expire 1 min avant le time reel d'expiration
 			//si cest superieur a now, je la renouvelle
 
 			Date now =new Date();
 
 			return now.after(endOfSession);
 
 		}
 		return true;
 	}
 
 
 
 }
