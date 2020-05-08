 /**
  * Copyright (C) 2011 PROCESSBASE Ltd.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.processbase.ui.core;
 
 //import com.sun.appserv.security.ProgrammaticLogin; //if executed other than glassfish this will throw class not found exception ?
 import java.io.File;
 import java.io.FileOutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.login.LoginContext;
 
 import org.apache.commons.collections.iterators.EntrySetMapIterator;
 import org.apache.log4j.LogManager;
 import org.ow2.bonita.facade.BAMAPI;
 import org.ow2.bonita.facade.CommandAPI;
 import org.ow2.bonita.facade.IdentityAPI;
 import org.ow2.bonita.facade.exception.UndeletableProcessException;
 import org.ow2.bonita.facade.ManagementAPI;
 import org.ow2.bonita.facade.QueryDefinitionAPI;
 import org.ow2.bonita.facade.QueryRuntimeAPI;
 import org.ow2.bonita.facade.RepairAPI;
 import org.ow2.bonita.facade.RuntimeAPI;
 import org.ow2.bonita.facade.WebAPI;
 import org.ow2.bonita.facade.def.element.BusinessArchive;
 import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
 import org.ow2.bonita.facade.def.majorElement.DataFieldDefinition;
 import org.ow2.bonita.facade.def.majorElement.ParticipantDefinition;
 import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
 import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
 import org.ow2.bonita.facade.exception.ActivityNotFoundException;
 import org.ow2.bonita.facade.exception.DeploymentException;
 import org.ow2.bonita.facade.exception.IllegalTaskStateException;
 import org.ow2.bonita.facade.exception.InstanceNotFoundException;
 import org.ow2.bonita.facade.exception.MetadataNotFoundException;
 import org.ow2.bonita.facade.exception.ParticipantNotFoundException;
 import org.ow2.bonita.facade.exception.ProcessNotFoundException;
 import org.ow2.bonita.facade.exception.TaskNotFoundException;
 import org.ow2.bonita.facade.exception.UserAlreadyExistsException;
 import org.ow2.bonita.facade.exception.UserNotFoundException;
 import org.ow2.bonita.facade.exception.VariableNotFoundException;
 import org.ow2.bonita.facade.runtime.ActivityInstance;
 import org.ow2.bonita.facade.runtime.ActivityState;
 import org.ow2.bonita.facade.runtime.ProcessInstance;
 import org.ow2.bonita.facade.runtime.TaskInstance;
 import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
 import org.ow2.bonita.facade.uuid.DocumentUUID;
 import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
 import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
 import org.ow2.bonita.services.DocumentationManager;
 import org.ow2.bonita.services.Folder;
 import org.ow2.bonita.util.AccessorUtil;
 import org.ow2.bonita.facade.exception.UndeletableInstanceException;
 import org.ow2.bonita.facade.identity.Group;
 import org.ow2.bonita.facade.identity.Membership;
 import org.ow2.bonita.facade.identity.ProfileMetadata;
 import org.ow2.bonita.facade.identity.Role;
 import org.ow2.bonita.facade.identity.User;
 import org.ow2.bonita.facade.privilege.Rule;
 import org.ow2.bonita.facade.privilege.Rule.RuleType;
 import org.ow2.bonita.facade.runtime.Category;
 import org.ow2.bonita.facade.runtime.Comment;
 import org.ow2.bonita.facade.runtime.InstanceState;
 import org.ow2.bonita.facade.uuid.ActivityDefinitionUUID;
 import org.ow2.bonita.identity.auth.DomainOwner;
 import org.ow2.bonita.identity.auth.UserOwner;
 import org.ow2.bonita.light.LightActivityInstance;
 import org.ow2.bonita.light.LightProcessDefinition;
 import org.ow2.bonita.light.LightProcessInstance;
 import org.ow2.bonita.light.LightTaskInstance;
 import org.ow2.bonita.util.GroovyException;
 import org.ow2.bonita.facade.runtime.AttachmentInstance;
 import org.ow2.bonita.facade.runtime.InitialAttachment;
 import org.ow2.bonita.facade.uuid.AbstractUUID;
 import org.ow2.bonita.util.BusinessArchiveFactory;
 import org.ow2.bonita.util.Command;
 import org.ow2.bonita.util.EnvTool;
 import org.ow2.bonita.util.GroovyExpression;
 import org.ow2.bonita.util.Misc;
 import org.ow2.bonita.util.SimpleCallbackHandler;
 import org.processbase.commands.documents.DeleteDocumentCommand;
 import org.processbase.engine.bam.command.DeleteMetaDim;
 import org.processbase.ui.core.bonita.diagram.Diagram;
 
 
 import com.sun.appserv.security.AppservRealm;
 import com.sun.appserv.security.ProgrammaticLogin;
 import com.sun.enterprise.security.auth.realm.Realm;
 
 import org.apache.log4j.Logger;
 /**
  *
  * @author mgubaidullin
  */
 public class BPMModule {
 	public static final String USER_GUEST = "guest";
     final RuntimeAPI runtimeAPI;
     final QueryRuntimeAPI queryRuntimeAPI;
     final ManagementAPI managementAPI;
     final QueryDefinitionAPI queryDefinitionAPI;
     final RepairAPI repairAPI;
     final WebAPI webAPI;
     final IdentityAPI identityAPI;
     final BAMAPI bamAPI;
     final CommandAPI commandAPI;
     private String currentUserUID;
 	final static Logger logger = Logger.getLogger(BPMModule.class);
 	//private DocumentationManager documentatinManager;
 
     public BPMModule(String currentUserUID) {
         if (!Constants.LOADED) {
             Constants.loadConstants();
         }
         this.currentUserUID = currentUserUID;
         try {
             initContext();
         } catch (Exception ex) {
 			logger.error("constructor", ex);
             //Logger.getLogger(BPMModule.class.getName()).log(Level.SEVERE, ex.getMessage());
         }
         runtimeAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getRuntimeAPI();
         queryRuntimeAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getQueryRuntimeAPI();
         managementAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getManagementAPI();
         queryDefinitionAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getQueryDefinitionAPI();
         repairAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getRepairAPI();
         webAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getWebAPI();
         identityAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getIdentityAPI();
         bamAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getBAMAPI();
         commandAPI = AccessorUtil.getAPIAccessor(Constants.BONITA_EJB_ENV).getCommandAPI();
         
         
     }
     
     private Class tryClass(String name)
     {
         try
         {
             return Class.forName(name);
         }
         catch (ClassNotFoundException e)
         {
             return null;
         }
     }
 
 
     private void initContext() throws Exception {
     	if (Constants.APP_SERVER.startsWith("GLASSFISH")) {
     		
     		/*try {
     			Subject subject=new Subject()
     			LoginContext ctx = new LoginContext("processBaseRealm", new ProcessbaseAuthCallbackHandler());
         		ctx.login();
         			
 			} catch (Exception e) {
 				// TODO: handle exception
 			}
     		Class authClass = tryClass("com.sun.appserv.security.ProgrammaticLogin");
     		if(authClass!=null)
     		{
     			com.sun.appserv.security.ProgrammaticLogin pl=new ProgrammaticLogin();
     			
     			Method login=authClass.getMethod("login");
     			login.invoke(authClass.newInstance(), currentUserUID, "".toCharArray(), "processBaseRealm", false);        		
     		}
     		 
     		try {
     			ProgrammaticLogin programmaticLogin = new ProgrammaticLogin();
                 programmaticLogin.login(currentUserUID, "".toCharArray(), "processBaseRealm", false);	
 			} catch (Exception e) {
 				
 			}
     		if(Realm.isValidRealm("processBaseRealm"))
     		{
     			Realm r=Realm.getInstance("processBaseRealm")
     		}
     		LoginContext loginContext = null;
     		Configuration config = Configuration.getConfiguration();
             CallbackHandler handler = new SimpleCallbackHandler(currentUserUID, "");
             Subject subject=new Subject();
     		 try {
     			 
     			 AppConfigurationEntry[] entries = config.getAppConfigurationEntry("ProcessBaseAuth");
     			 if(entries!=null)
     			 {
 					for (AppConfigurationEntry entry : entries) {
 						String loginModule=entry.getLoginModuleName();
 						Class c=Class.forName(loginModule);
 						Object[] args = { };
 						 Constructor constructor = c.getConstructor();
 						 Object module=constructor.newInstance(args);   
 						 Method init=findMethod("init", c);
 							if(init!=null)
 							{	
 								Properties property=new Properties();
 								for(Entry<String,?> param:entry.getOptions().entrySet())
 									property.put(param.getKey(), param.getValue());
 								init.invoke(module, property);
 							}
 							Method login=findMethod("login", c);
 							if(login!=null)
 								login.invoke(module, null);
 					}
 						 
 					
     				 
     			 }
     		 }
     			 catch(Exception e)
     			 {
     				 
     			 }*/
     	            /*loginContext = new LoginContext("ProcessBaseAuth", subject, handler, config);
     	            loginContext.login();
     	            loginContext.logout();
     	            
     	        } catch (javax.security.auth.login.LoginException e) {
     	            try {
     	                // make another try after refresh
     	                javax.security.auth.login.Configuration.getConfiguration().refresh();
     	                loginContext = new LoginContext("ProcessBaseAuth", subject, handler, config);
     	                loginContext.login();
     	                loginContext.logout();
     	            } catch (javax.security.auth.login.LoginException e2) {
     	            
     	                throw new RuntimeException("Authentication failed for user '" + currentUserUID + "'");
     	            }
     	        }*/
     		
 
     	}
         DomainOwner.setDomain(Constants.BONITA_DOMAIN);
         UserOwner.setUser(currentUserUID);
     }
     
     private Method findMethod(String name, Class c)
     {
     	for (Method method : c.getMethods()) 
     	{
 			if(method.getName()==name)
 				return method;
 		}
     	 return null;
     }
 
     public Set<ProcessDefinition> getProcessDefinitions() throws Exception {
         initContext();
         return queryDefinitionAPI.getProcesses();
     }
 
     public boolean isUserAdmin() throws Exception {
     	logger.debug("isUserAdmin");
         initContext();
         return managementAPI.isUserAdmin(currentUserUID);
     }
     
     
 
 
     public Set<ProcessDefinition> getProcessDefinitions(ProcessState state) throws Exception {
     	logger.debug("getProcessDefinitions");
         initContext();
         return queryDefinitionAPI.getProcesses(state);
     }
 
     public LightProcessDefinition getLightProcessDefinition(ProcessDefinitionUUID pdUUID) throws Exception {
     	logger.debug("getLightProcessDefinition");
         initContext();
         return queryDefinitionAPI.getLightProcess(pdUUID);
     }
 
     public Set<LightProcessDefinition> getLightProcessDefinitions() throws Exception {
     	logger.debug("getLightProcessDefinitions");
         initContext();
         return queryDefinitionAPI.getLightProcesses();
     }
 
     public Set<LightProcessDefinition> getAllowedLightProcessDefinitions(Group groupFilter) throws Exception {
     	logger.debug("getAllowedLightProcessDefinitions");
         initContext();
         User user = identityAPI.findUserByUserName(currentUserUID);
         Set<String> membershipUUIDs = new HashSet<String>();
         for (Membership membership : user.getMemberships()) {
         	if(groupFilter==null)
         		membershipUUIDs.add(membership.getUUID());
         	else if(membership.getGroup().getName().equals(groupFilter.getName()))
         		membershipUUIDs.add(membership.getUUID());
         }
         List<Rule> userRules = managementAPI.getApplicableRules(RuleType.PROCESS_START, null, null, null, membershipUUIDs, null);
 
         Set<String> processException;
         Set<ProcessDefinitionUUID> processUUIDException = new HashSet<ProcessDefinitionUUID>();
         for (Rule r : userRules) {
             processException = r.getEntities();
             for (String processID : processException) {
                 processUUIDException.add(new ProcessDefinitionUUID(processID));
             }
         }
         Set<LightProcessDefinition> result = new HashSet<LightProcessDefinition>();
         for (LightProcessDefinition lpd : queryDefinitionAPI.getLightProcesses(processUUIDException)) {
             if (lpd.getState().equals(ProcessState.ENABLED)) {
                 result.add(lpd);
             }
         }
         return result;
     }
 
     public Set<LightProcessDefinition> getLightProcessDefinitions(ProcessState state) throws Exception {
     	logger.debug("getLightProcessDefinitions");
         initContext();
         return queryDefinitionAPI.getLightProcesses(state);
     }
 
     public void disableProcessDefinitions(ProcessDefinitionUUID uuid) throws Exception {
     	logger.debug("disableProcessDefinitions");
         initContext();
         managementAPI.disable(uuid);
     }
 
     public void enableProcessDefinitions(ProcessDefinitionUUID uuid) throws Exception {
     	logger.debug("enableProcessDefinitions");
         initContext();
         managementAPI.enable(uuid);
     }
 
     public void archiveProcessDefinitions(ProcessDefinitionUUID uuid) throws Exception {
     	logger.debug("archiveProcessDefinitions");
         initContext();
         managementAPI.archive(uuid);
     }
 
     public ProcessInstanceUUID startNewProcess(ProcessDefinitionUUID uuid, Map<String, Object> vars) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("startNewProcess");
         initContext();
         return runtimeAPI.instantiateProcess(uuid, vars);
     }
 
     public ProcessInstanceUUID startNewProcess(ProcessDefinitionUUID uuid, Map<String, Object> vars, Collection<InitialAttachment> initialAttachments) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("startNewProcess");
         initContext();
         return runtimeAPI.instantiateProcess(uuid, vars, initialAttachments);
     }
 
     public ProcessInstanceUUID startNewProcess(ProcessDefinitionUUID uuid) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("startNewProcess");
         initContext();
         return runtimeAPI.instantiateProcess(uuid);
     }
 
     public void saveProcessVariables(TaskInstance task, Map<String, Object> vars) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("saveProcessVariables");
         for (String key : vars.keySet()) {
             setProcessInstanceVariable(task.getProcessInstanceUUID(), key, vars.get(key));
         }
     }
 
     public void saveProcessVariables2(ActivityInstance activity, Map<String, Object> vars) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("saveProcessVariables2");
         for (String key : vars.keySet()) {
             setProcessInstanceVariable(activity.getProcessInstanceUUID(), key, vars.get(key));
         }
     }
 
     public Set<DataFieldDefinition> getProcessDataFields(ProcessDefinitionUUID uuid) throws ProcessNotFoundException, Exception {
     	logger.debug("getProcessDataFields");
         initContext();
         return queryDefinitionAPI.getProcessDataFields(uuid);
     }
 
     public Set<DataFieldDefinition> getActivityDataFields(ActivityDefinitionUUID aduuid) throws ProcessNotFoundException, Exception {
     	logger.debug("getActivityDataFields");
         initContext();
         return queryDefinitionAPI.getActivityDataFields(aduuid);
     }
 
     public DataFieldDefinition getProcessDataField(ProcessDefinitionUUID uuid, String varName) throws ProcessNotFoundException, Exception {
     	logger.debug("getProcessDataField");
         initContext();
         return queryDefinitionAPI.getProcessDataField(uuid, varName);
     }
 
     public Map<String, ActivityDefinition> getProcessInitialActivities(ProcessDefinitionUUID uuid) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("getProcessInitialActivities");
         initContext();
         return queryDefinitionAPI.getProcess(uuid).getInitialActivities();
     }
 
     public Set<ActivityDefinition> getProcessActivities(ProcessDefinitionUUID uuid) throws ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("getProcessActivities");
         initContext();
         return queryDefinitionAPI.getProcessActivities(uuid);
     }
     
    
 
     public Collection<TaskInstance> getTaskList(ActivityState state) throws Exception {
     	logger.debug("getTaskList");
         initContext();
         return queryRuntimeAPI.getTaskList(state);
     }
 
     public Collection<LightTaskInstance> getLightTaskList(ActivityState state) throws Exception {
     	logger.debug("getLightTaskList");
         initContext();
         return queryRuntimeAPI.getLightTaskList(state);
     }
 
     public Set<ProcessInstance> getUserInstances() throws Exception {
     	logger.debug("getUserInstances");
         initContext();
         return queryRuntimeAPI.getUserInstances();
     }
 
     public Set<LightProcessInstance> getLightUserInstances() throws Exception {
     	logger.debug("getLightUserInstances");
         initContext();
         return queryRuntimeAPI.getLightUserInstances();
     }
 
     public TaskInstance startTask(ActivityInstanceUUID activityInstanceUUID, boolean b) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("startTask");
         initContext();
         TaskInstance ti = getTaskInstance(activityInstanceUUID);
         if (ti != null && ti.getState().equals(ActivityState.READY)) {
             runtimeAPI.startTask(activityInstanceUUID, b);
             return getTaskInstance(activityInstanceUUID);
         }
         return ti;
     }
     
     public TaskInstance nextUserTask(ProcessInstanceUUID processInstanceUUID, String currentUserName)  throws Exception {
     	logger.debug("nextUserTask");
         initContext();
     	Set<ActivityInstance> activities= queryRuntimeAPI.getActivityInstances(processInstanceUUID);
     	Collection<TaskInstance> taskList = queryRuntimeAPI.getTaskList(processInstanceUUID, ActivityState.EXECUTING);
     	for (ActivityInstance instance : activities) {    		
     		if(instance.getState() == ActivityState.READY 
     				|| instance.getState() == ActivityState.EXECUTING 
 					|| instance.getState()==ActivityState.SUSPENDED)
 			{
 				TaskInstance task=instance.getTask();
 				if(task.getTaskCandidates().contains(currentUserName))
 					return assignAndStartTask(task.getUUID(), currentUserName);				
 			}
 		}
 		return null;
 		
 	} 
 
     public void finishTask(ActivityInstanceUUID activityInstanceUUID, boolean b) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("finishTask");
         initContext();
         runtimeAPI.finishTask(activityInstanceUUID, b);
     }
 
    public void finishTask(TaskInstance task, boolean b, Map<String, Object> pVars, Map<String, Object> aVars) throws TaskNotFoundException, IllegalTaskStateException, InstanceNotFoundException, VariableNotFoundException, Exception {
 	   logger.debug("finishTask");
        initContext();
         //runtimeAPI.setProcessInstanceVariables(task.getProcessInstanceUUID(), pVars);
         //runtimeAPI.setActivityInstanceVariables(task.getUUID(), aVars);
         setProcessAndActivityInstanceVariables(task, pVars, aVars);
         runtimeAPI.finishTask(task.getUUID(), b);
     }
 
 	private void setProcessAndActivityInstanceVariables(TaskInstance task,
 			Map<String, Object> pVars, Map<String, Object> aVars)
 			throws InstanceNotFoundException, VariableNotFoundException,
 			ActivityNotFoundException {
 		logger.debug("setProcessAndActivityInstanceVariables");
         for (Map.Entry<String, Object> entry : pVars.entrySet()) {
             runtimeAPI.setProcessInstanceVariable(task.getProcessInstanceUUID(), entry.getKey(), entry.getValue());
 		}
         for (Map.Entry<String, Object> entry : aVars.entrySet()) {
             runtimeAPI.setActivityInstanceVariable(task.getUUID(), entry.getKey(), entry.getValue());
         }
 	}
 	
     public void finishTask(TaskInstance task, boolean b, Map<String, Object> pVars, Map<String, Object> aVars, Map<AttachmentInstance, byte[]> attachments) throws TaskNotFoundException, IllegalTaskStateException, InstanceNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("finishTask");
         initContext();
         //runtimeAPI.setProcessInstanceVariables(task.getProcessInstanceUUID(), pVars);
         //runtimeAPI.setActivityInstanceVariables(task.getUUID(), aVars);
         setProcessAndActivityInstanceVariables(task, pVars, aVars);
         for (AttachmentInstance a : attachments.keySet()) {
         	logger.debug(a.getProcessInstanceUUID() + " " + a.getName() + " " + a.getFileName() + " " + attachments.get(a).length);
         }
         runtimeAPI.addAttachments(attachments);
         runtimeAPI.finishTask(task.getUUID(), b);
     }
 
     public void addAttachment(ProcessInstanceUUID instanceUUID, String name, String fileName, String mimeType, byte[] value) throws Exception {
     	logger.debug("addAttachment");
         initContext();
         Map<String, String> metadata=new Hashtable<String, String>();
         metadata.put("content-type", mimeType);
 		//runtimeAPI.addAttachment(instanceUUID, name, fileName, value);
         runtimeAPI.addAttachment(instanceUUID, name, null, null, fileName, metadata, value);
     }
 
     public byte[] getAttachmentValue(String processUUID, String name) throws Exception {
     	logger.debug("getAttachmentValue");
     	initContext();
     	AttachmentInstance attachmentInstance = queryRuntimeAPI.getLastAttachment(new ProcessInstanceUUID(processUUID), name, new Date());
     	return queryRuntimeAPI.getAttachmentValue(attachmentInstance);
     }
     
     public AttachmentInstance getAttachment(String processUUID, String name) throws Exception {
     	logger.debug("getAttachmentValue");
         initContext();
         AttachmentInstance attachmentInstance = queryRuntimeAPI.getLastAttachment(new ProcessInstanceUUID(processUUID), name, new Date());
         return attachmentInstance;
         //return queryRuntimeAPI.getAttachmentValue(attachmentInstance);
     }
     
     public byte[] getAttachmentBytes(AttachmentInstance attachmentInstance) throws Exception {
     	logger.debug("getAttachmentValue");
         initContext();
         return queryRuntimeAPI.getAttachmentValue(attachmentInstance);
     }
 
     public List<AttachmentInstance> getLastAttachments(ProcessInstanceUUID instanceUUID, String regex) throws Exception {
     	logger.debug("getLastAttachments");
         initContext();
         return new ArrayList<AttachmentInstance>(queryRuntimeAPI.getLastAttachments(instanceUUID, regex));
     }
 
     public org.ow2.bonita.facade.runtime.Document getDocument(DocumentUUID docId) throws Exception 
     {
     	initContext();
     	org.ow2.bonita.facade.runtime.Document doc = queryRuntimeAPI.getDocument(docId);
     	return doc;
     }
     public List<AttachmentInstance> getLastAttachments(ProcessInstanceUUID instanceUUID, Set<String> attachmentNames) throws Exception {
     	logger.debug("getLastAttachments");
         initContext();
         return new ArrayList<AttachmentInstance>(queryRuntimeAPI.getLastAttachments(instanceUUID, attachmentNames));
     }
 
 	public TaskInstance assignTask(ActivityInstanceUUID activityInstanceUUID, String user) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("assignTask");
         initContext();
         TaskInstance ti = getTaskInstance(activityInstanceUUID);
         if (ti != null && ti.isTaskAssigned() && !ti.getTaskUser().equals(user)) {
             return null;
         }
         runtimeAPI.assignTask(activityInstanceUUID, user);
         return getTaskInstance(activityInstanceUUID);
     }
 
     public TaskInstance assignAndStartTask(ActivityInstanceUUID activityInstanceUUID, String user) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("assignAndStartTask");
         initContext();
         TaskInstance ti = getTaskInstance(activityInstanceUUID);
         if (ti != null && ti.isTaskAssigned() && !ti.getTaskUser().equals(user)) {
             return null;
         }
         runtimeAPI.assignTask(activityInstanceUUID, user);
         if (ti != null && ti.getState().equals(ActivityState.READY)) {
             runtimeAPI.startTask(activityInstanceUUID, true);
         }
         return getTaskInstance(activityInstanceUUID);
     }
 
     public TaskInstance resumeTask(ActivityInstanceUUID activityInstanceUUID, boolean b) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("resumeTask");
         initContext();
         runtimeAPI.resumeTask(activityInstanceUUID, b);
         return getTaskInstance(activityInstanceUUID);
     }
 
     public TaskInstance suspendTask(ActivityInstanceUUID activityInstanceUUID, boolean b) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("suspendTask");
         initContext();
         runtimeAPI.suspendTask(activityInstanceUUID, b);
         return getTaskInstance(activityInstanceUUID);
     }
 
     public void setProcessInstanceVariable(ProcessInstanceUUID piUUID, String varName, Object varValue) throws InstanceNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("setProcessInstanceVariable");
         initContext();
         runtimeAPI.setProcessInstanceVariable(piUUID, varName, varValue);
     }
 
     public void setActivityInstanceVariable(ActivityInstanceUUID aiuuid, String varName, Object varValue) throws InstanceNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("setActivityInstanceVariable");
         initContext();
         runtimeAPI.setActivityInstanceVariable(aiuuid, varName, varValue);
     }
 
     public Map<String, Object> getActivityInstanceVariables(ActivityInstanceUUID aiUUID) throws ActivityNotFoundException, Exception, ActivityNotFoundException, ActivityNotFoundException, ActivityNotFoundException, ActivityNotFoundException {
     	logger.debug("getActivityInstanceVariables");
         initContext();
         return queryRuntimeAPI.getActivityInstanceVariables(aiUUID);
     }
 
     public Map<String, Object> getProcessInstanceVariables(ProcessInstanceUUID piUUID) throws InstanceNotFoundException, Exception {
     	logger.debug("getProcessInstanceVariables");
         initContext();
         return queryRuntimeAPI.getProcessInstanceVariables(piUUID);
     }
 
     public Object getProcessInstanceVariable(ProcessInstanceUUID piUUID, String varName) throws InstanceNotFoundException, Exception {
     	logger.debug("getProcessInstanceVariable");
         initContext();
         return queryRuntimeAPI.getProcessInstanceVariable(piUUID, varName);
     }
 
     public ActivityDefinition getProcessActivity(ProcessDefinitionUUID pdUUID, String ActivityName) throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getProcessActivity");
         initContext();
         return queryDefinitionAPI.getProcessActivity(pdUUID, ActivityName);
     }
 
     public ParticipantDefinition getProcessParticipant(ProcessDefinitionUUID pdUUID, String participant) throws ParticipantNotFoundException, ProcessNotFoundException, Exception {
     	logger.debug("getProcessParticipant");
         initContext();
         return queryDefinitionAPI.getProcessParticipant(pdUUID, participant);
     }
 
     public Set<ProcessDefinition> getProcesses() throws Exception {
     	logger.debug("getProcess");
         initContext();
         return queryDefinitionAPI.getProcesses();
     }
 
     public Set<ProcessDefinition> getProcesses(ProcessState ps) throws Exception {
     	logger.debug("getProcesses");
         initContext();
         return queryDefinitionAPI.getProcesses(ps);
     }
 
     public ProcessDefinition getProcessDefinition(ProcessDefinition pd) throws ProcessNotFoundException, Exception {
     	logger.debug("getProcessDefinition");
         initContext();
         return queryDefinitionAPI.getProcess(pd.getUUID());
     }
 
     public ProcessDefinition deploy(BusinessArchive bar) throws DeploymentException, ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("deploy");
         initContext();
         ProcessDefinition result = managementAPI.deploy(bar);
         return result;
     }
 
     public ProcessDefinition deploy(BusinessArchive bar, String emptyCategoryName) throws DeploymentException, ProcessNotFoundException, VariableNotFoundException, Exception {
     	logger.debug("deploy");
         initContext();
         ProcessDefinition result = managementAPI.deploy(bar);
         // add to empty category
         if (result.getCategoryNames().isEmpty()) {
             Set<String> emptyCategory = new HashSet<String>(1);
             emptyCategory.add(emptyCategoryName);
             if (webAPI.getCategories(emptyCategory).isEmpty()) {
                 webAPI.addCategory(emptyCategoryName, "", "", "");
             }
             webAPI.setProcessCategories(result.getUUID(), emptyCategory);
         }
         // create PROCESS_START rule for process
         Set<ProcessDefinitionUUID> processes = new HashSet<ProcessDefinitionUUID>(1);
         processes.add(result.getUUID());
         Rule rule = managementAPI.createRule(result.getUUID().toString(), result.getName(), "PROCESS_START Rule for ProcessDefinitionUUID" + result.getUUID().toString(), RuleType.PROCESS_START);
         managementAPI.addExceptionsToRuleByUUID(rule.getUUID(), processes);
         return result;
     }
 
     public Rule createRule(String name, String label, String description, RuleType type) throws Exception{
     	logger.debug("createRule");
         initContext();
         return managementAPI.createRule(name, label, description, type);
     }
     
     public Rule findRule(String name, Collection<String> memberships, String entityId, RuleType type) throws Exception{
     	logger.debug("createRule");
         initContext();
 
         List<Rule>rules=managementAPI.getApplicableRules(type, null, null, null, memberships, entityId);
 		if(rules==null) return null;
 		for (Rule rule : rules) {
 			if(rule.getName().equalsIgnoreCase(name))
 				return rule;
 		}
         return null;
     }
 
     public <E extends AbstractUUID> void addExceptionsToRuleByUUID(final String ruleUUID, final Set<E> exceptions) throws Exception{
     	logger.debug("addExceptionsToRuleByUUID");
         initContext();
         managementAPI.addExceptionsToRuleByUUID(ruleUUID, exceptions);
     }
 
     public <E extends AbstractUUID> void removeExceptionsFromRuleByUUID(final String ruleUUID, final Set<E> exceptions) throws Exception{
     	logger.debug("removeExceptionsFromRuleByUUID");
         initContext();
         managementAPI.removeExceptionsFromRuleByUUID(ruleUUID, exceptions);
     }
 
     public void deployJar(String jarName, byte[] body) throws Exception {
     	logger.debug("deployJar");
         initContext();
         if (managementAPI.getAvailableJars().contains(jarName)) {
             managementAPI.removeJar(jarName);
         }
         managementAPI.deployJar(jarName, body);
     }
 
     public void removeJar(String jarName) throws Exception {
     	logger.debug("removeJar");
         initContext();
         if (managementAPI.getAvailableJars().contains(jarName)) {
             managementAPI.removeJar(jarName);
         }
         managementAPI.removeJar(jarName);
     }
 
     public void deleteProcess(ProcessDefinition pd) throws UndeletableInstanceException, UndeletableProcessException, ProcessNotFoundException, Exception {
     	logger.debug("deleteProcess");
         initContext();
         managementAPI.deleteProcess(pd.getUUID());
         Rule rule = findRule(pd.getUUID().toString());
         managementAPI.deleteRuleByUUID(rule.getUUID());
         
         //new bonita 5.5 uses xCMIS and when deleting the process, API does not remove the folder from xCMIS this will fix it.
         execute(new DeleteDocumentCommand(pd.getUUID().toString()));
     }
 
     public void deleteAllProcessInstances(ProcessDefinition pd) throws Exception {
     	logger.debug("deleteAllProcessInstances");
         initContext();
         runtimeAPI.deleteAllProcessInstances(pd.getUUID());
     }
 
     public Set<ProcessInstance> getProcessInstances() throws Exception {
     	logger.debug("getProcessInstances");
         initContext();
         return queryRuntimeAPI.getProcessInstances();
     }
 
     public Set<LightProcessInstance> getLightProcessInstances() throws Exception {
     	logger.debug("getLightProcessInstances");
         initContext();
         return queryRuntimeAPI.getLightProcessInstances();
     }
 
     public Set<LightProcessInstance> getLightProcessInstances(ProcessDefinitionUUID pduuid) throws Exception {
     	logger.debug("getLightProcessInstances");
         initContext();
         return queryRuntimeAPI.getLightProcessInstances(pduuid);
     }
 
     public Set<ProcessInstance> getProcessInstancesByUUID(ProcessDefinitionUUID piUUID) throws Exception {
     	logger.debug("getProcessInstancesByUUID");
         initContext();
         return queryRuntimeAPI.getProcessInstances(piUUID);
     }
 
     public Set<ProcessInstance> getProcessInstances(ProcessDefinitionUUID piUUID, InstanceState state) throws ProcessNotFoundException, Exception {
     	logger.debug("getProcessInstances");
         initContext();
         Set<ProcessInstance> result = new HashSet<ProcessInstance>();
         Set<ProcessInstance> pis = queryRuntimeAPI.getProcessInstances(piUUID);
         for (ProcessInstance pi : pis) {
             if (pi.getInstanceState().equals(state)) {
                 result.add(pi);
             }
         }
         return result;
     }
 
     public Set<LightProcessInstance> getProcessInstancesByStatus(InstanceState state) throws Exception {
     	logger.debug("getProcessInstancesByStatus");
         initContext();
         Set<LightProcessInstance> result = new HashSet<LightProcessInstance>();
         Set<LightProcessInstance> pis = getLightProcessInstances();
         for (LightProcessInstance pi : pis) {
             if (pi.getInstanceState().equals(state)) {
                 result.add(pi);
             }
         }
         return result;
     }
 
     public Set<LightActivityInstance> getActivityInstances() throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getActivityInstances");
         initContext();
         Set<LightActivityInstance> result = new HashSet();
         try {
             Set<LightProcessInstance> pis = queryRuntimeAPI.getLightProcessInstances();
             for (LightProcessInstance pi : pis) {
                 result.addAll(queryRuntimeAPI.getLightActivityInstances(pi.getProcessInstanceUUID()));
             }
         } catch (InstanceNotFoundException ex) {
             ex.printStackTrace();
         }
         return result;
     }
 
     public Set<LightActivityInstance> getActivityInstances(ProcessDefinitionUUID pduuid) throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getActivityInstances");
         initContext();
         Set<LightActivityInstance> result = new HashSet();
         try {
             Set<LightProcessInstance> pis = queryRuntimeAPI.getLightProcessInstances(pduuid);
             for (LightProcessInstance pi : pis) {
                 result.addAll(queryRuntimeAPI.getLightActivityInstances(pi.getProcessInstanceUUID()));
             }
         } catch (InstanceNotFoundException ex) {
             ex.printStackTrace();
         }
         return result;
     }
 
     public Set<ActivityInstance> getActivityInstances(ProcessInstanceUUID processInstanceUUID) throws Exception {
     	logger.debug("getActivityInstances");
         initContext();
         return queryRuntimeAPI.getActivityInstances(processInstanceUUID);
     }
 
     public Set<LightActivityInstance> getLightActivityInstances(ProcessInstanceUUID processInstanceUUID) throws Exception {
     	logger.debug("getLightActivityInstances");
         initContext();
         return queryRuntimeAPI.getLightActivityInstances(processInstanceUUID);
     }
 
     public ActivityInstance getActivityInstance(ActivityInstanceUUID activityInstanceUUID) throws ActivityNotFoundException, Exception {
     	logger.debug("getActivityInstance");
         initContext();
         return queryRuntimeAPI.getActivityInstance(activityInstanceUUID);
     }
 
     public TaskInstance getTaskInstance(ActivityInstanceUUID activityInstanceUUID) throws ProcessNotFoundException, Exception {
     	logger.debug("getTaskInstance");
         initContext();
         try {
             return queryRuntimeAPI.getTask(activityInstanceUUID);
         } catch (TaskNotFoundException tex) {
             tex.printStackTrace();
             return null;
         }
     }
 
     public void deleteProcessInstance(ProcessInstanceUUID piUUID) throws InstanceNotFoundException, InstanceNotFoundException, InstanceNotFoundException, UndeletableInstanceException, Exception {
     	logger.debug("deleteProcessInstance");
         initContext();
         runtimeAPI.deleteProcessInstance(piUUID);
     }
 
     public ProcessDefinition getProcessDefinition(ProcessDefinitionUUID pdUUID) throws ProcessNotFoundException, Exception {
     	logger.debug("getProcessDefinition");
         initContext();
         return queryDefinitionAPI.getProcess(pdUUID);
     }
 
     public ActivityDefinition getProcessActivityDefinition(ActivityInstance ai) throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getProcessActivityDefinition");
         initContext();
         return queryDefinitionAPI.getProcessActivity(ai.getProcessDefinitionUUID(), ai.getActivityName());
     }
 
     public ActivityDefinition getProcessActivityDefinition(LightActivityInstance lai) throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getProcessActivityDefinition");
         initContext();
         return queryDefinitionAPI.getProcessActivity(lai.getProcessDefinitionUUID(), lai.getActivityName());
     }
 
     public ActivityDefinition getTaskDefinition(ActivityInstance ai) throws ProcessNotFoundException, ActivityNotFoundException, Exception {
     	logger.debug("getTaskDefinition");
         initContext();
         return queryDefinitionAPI.getProcessActivity(ai.getProcessDefinitionUUID(), ai.getActivityName());
     }
 
     public void assignTask(ActivityInstanceUUID activityInstanceUUID, Set<String> users) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("assignTask");
         initContext();
         runtimeAPI.assignTask(activityInstanceUUID, users);
     }
 
     public void assignTask(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("assignTask");
         initContext();
         runtimeAPI.assignTask(activityInstanceUUID);
     }
 
     public void setActivityInstancePriority(ActivityInstanceUUID activityInstanceUUID, int priority) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("setActivityInstancePriority");
         initContext();
         runtimeAPI.setActivityInstancePriority(activityInstanceUUID, priority);
     }
 
     public TaskInstance unassignTask(ActivityInstanceUUID activityInstanceUUID) throws TaskNotFoundException, IllegalTaskStateException, Exception {
     	logger.debug("unassignTask");
         initContext();
         runtimeAPI.unassignTask(activityInstanceUUID);
         return getTaskInstance(activityInstanceUUID);
     }
 
     public void addProcessMetaData(ProcessDefinitionUUID processDefinitionUUID, String key, String value) throws Exception {
     	logger.debug("addProcessMetaData");
         initContext();
         runtimeAPI.addProcessMetaData(processDefinitionUUID, key, value);
     }
 
     public void deleteProcessMetaData(ProcessDefinitionUUID processDefinitionUUID, String key) throws Exception {
     	logger.debug("deleteProcessMetaData");
         initContext();
         runtimeAPI.deleteProcessMetaData(processDefinitionUUID, key);
     }
 
     public Map<String, String> getProcessMetaData(ProcessDefinitionUUID processDefinitionUUID) throws Exception {
     	logger.debug("getProcessMetaData");
         initContext();
         return queryDefinitionAPI.getProcess(processDefinitionUUID).getMetaData();
     }
     
     
 
     public byte[] getProcessDiagramm(LightProcessInstance pi) throws Exception {
     	logger.debug("getProcessDiagramm");
         initContext();
         Map<String, byte[]> resource = queryDefinitionAPI.getBusinessArchive(pi.getProcessDefinitionUUID()).getResources();
         byte[] img = null;
         byte[] proc = null;
         for (String key : resource.keySet()) {
             if (key.substring(key.length() - 4, key.length()).equals("proc")) {
                 proc = resource.get(key);
             } else if (key.equals(pi.getProcessDefinitionUUID().toString() + ".png")) {
                 img = resource.get(key);
             }
         }
         File x = new File("AAA.png");
         FileOutputStream fos = new FileOutputStream(x);
         fos.write(img);
         fos.close();
 
         Diagram d = new Diagram(img, proc, queryRuntimeAPI.getLightActivityInstances(pi.getRootInstanceUUID()));
         return d.getImage();
     }
 
 //    public XMLTaskDefinition getXMLTaskDefinition(ProcessDefinitionUUID pdUUID, String stepName) throws Exception {
 //        XMLProcessDefinition process = getXMLProcessDefinition(pdUUID);
 //        return process.getTasks().get(stepName);
 //    }
 //
 //    public XMLProcessDefinition getXMLProcessDefinition(ProcessInstanceUUID processInstanceUUID) throws Exception {
 //        return getXMLProcessDefinition(processInstanceUUID.getProcessDefinitionUUID());
 //    }
 //
 //    public XMLProcessDefinition getXMLProcessDefinition(ProcessDefinitionUUID processDefinitionUUID) throws Exception {
 //        initContext();
 //        Map<String, byte[]> resource = queryDefinitionAPI.getBusinessArchive(processDefinitionUUID).getResources();
 //        byte[] proc = null;
 //        for (String key : resource.keySet()) {
 //            if (key.substring(key.length() - 4, key.length()).equals("proc")) {
 //                proc = resource.get(key);
 //            }
 //        }
 //        BonitaFormParcer bfb = new BonitaFormParcer(proc);
 //        return bfb.getProcess();
 //    }
 
     public byte[] getProcessDiagramm(ProcessDefinitionUUID processDefinitionUUID) throws Exception {
     	logger.debug("getProcessDiagramm");
         initContext();
         Map<String, byte[]> resource = queryDefinitionAPI.getBusinessArchive(processDefinitionUUID).getResources();
         byte[] img = null;
        for (String key : resource.keySet()) {
            if (key.substring(key.length() - 3, key.length()).equals("png")) {
                img = resource.get(key);
             }
         }
        return img;
     }
     
     public Map<String, byte[]> getBusinessArchive(ProcessDefinitionUUID processDefinitionUUID) throws Exception {
     	logger.debug("getBusinessArchive");
         initContext();
         return queryDefinitionAPI.getBusinessArchive(processDefinitionUUID).getResources();
     }
     public byte[] getBusinessArchiveFile(ProcessDefinitionUUID uuid) throws Exception {
     	logger.debug("getBusinessArchiveFile");
         initContext();
     	BusinessArchive ba= queryDefinitionAPI.getBusinessArchive(uuid);
     	File file=new File(ba.getProcessDefinition().getName()+".bar");
     	byte[] barContent = Misc.generateJar(ba.getResources());
     	return barContent;
 	}
     public void stopExecution(ProcessInstanceUUID piUUID, String stepName) throws Exception {
     	logger.debug("stopExecution");
         initContext();
         repairAPI.stopExecution(piUUID, stepName);
     }
 
     public ActivityInstanceUUID startExecution(ProcessInstanceUUID piUUID, String stepName) throws Exception {
     	logger.debug("startExecution");
         initContext();
         return repairAPI.startExecution(piUUID, stepName);
     }
 
     public ActivityInstanceUUID reStartExecution(ProcessInstanceUUID piUUID, String stepName) throws Exception {
     	logger.debug("reStartExecution");
         initContext();
         repairAPI.stopExecution(piUUID, stepName);
         return repairAPI.startExecution(piUUID, stepName);
     }
 
     public LightProcessDefinition setProcessCategories(ProcessDefinitionUUID pduuid, Set<String> set) throws Exception {
     	logger.debug("setProcessCategories");
         initContext();
         return webAPI.setProcessCategories(pduuid, set);
     }
 
     public void deleteCategories(Set<String> set) throws Exception {
     	logger.debug("deleteCategories");
         initContext();
         webAPI.deleteCategories(set);
     }
 
     public void addCategory(String string, String string1, String string2, String string3) throws Exception {
     	logger.debug("addCategory");
         initContext();
         webAPI.addCategory(string, string1, string2, string3);
     }
 
     public Set<Category> getAllCategories() throws Exception {
     	logger.debug("getAllCategories");
         initContext();
         return webAPI.getAllCategories();
     }
 
     public Object evaluateGroovyExpression(String expression, ActivityInstance ai, boolean propagate) throws InstanceNotFoundException, GroovyException, Exception {
     	logger.debug("evaluateGroovyExpression");
         initContext();
         return runtimeAPI.evaluateGroovyExpression(expression, ai.getUUID(), false, propagate);
 
     }
 
     public Object evaluateExpression(String expression, ActivityInstance ai, boolean propagate) throws InstanceNotFoundException, GroovyException, Exception {
     	logger.debug("evaluateExpression");
         if (expression != null && GroovyExpression.isGroovyExpression(expression)) {
             initContext();
             return runtimeAPI.evaluateGroovyExpression(expression, ai.getUUID(), false, propagate);
         } else {
             return expression;
         }
     }
 
     public Object evaluateExpression(String expression, ProcessDefinitionUUID pduuid) throws InstanceNotFoundException, GroovyException, Exception {
     	logger.debug("evaluateExpression");
         if (expression != null && GroovyExpression.isGroovyExpression(expression)) {
             initContext();
             return runtimeAPI.evaluateGroovyExpression(expression, pduuid);
         } else {
             return expression;
         }
     }
 
     public Object evaluateGroovyExpression(String expression, ProcessDefinitionUUID pduuid) throws InstanceNotFoundException, GroovyException, Exception {
     	logger.debug("evaluateGroovyExpression");
         initContext();
         return runtimeAPI.evaluateGroovyExpression(expression, pduuid);
     }
 
      public Map<String, Object> evaluateGroovyExpressions(Map<String, String> expressions,
             ActivityInstanceUUID activityUUID, Map<String, Object> context, boolean useActivityScope, boolean propagate)
             throws InstanceNotFoundException, ActivityNotFoundException, GroovyException {
     	 logger.debug("evaluateGroovyExpressions");
          if (!expressions.isEmpty()) {
         	Map<String, Object> results=new HashMap<String, Object>();
 	           for (Map.Entry<String, String> entry : expressions.entrySet()) {
 	        	   Object result=runtimeAPI.evaluateGroovyExpression(entry.getValue(), activityUUID, context, useActivityScope, propagate);
 	               results.put(entry.getKey(), result);
 	           }
              return results;
             //return runtimeAPI.evaluateGroovyExpressions(expressions, activityUUID, useActivityScope, propagate);
         } else {
             return null;
         }
     }
 
     public Map<String, Object> evaluateGroovyExpressions(Map<String, String> expressions, ProcessDefinitionUUID processDefinitionUUID, Map<String, Object> context, boolean useInitialVariableValues)
             throws InstanceNotFoundException, ProcessNotFoundException, GroovyException {
     	logger.debug("evaluateGroovyExpressions");
         Map<String, Object> results=new HashMap<String, Object>();
 	        for (Map.Entry<String, String> entry : expressions.entrySet()) {
 	                results.put(entry.getKey(),
 	                    runtimeAPI.evaluateGroovyExpression(entry.getValue(), processDefinitionUUID, context));
 	            }
 	        return results;
         //return runtimeAPI.evaluateGroovyExpressions(expressions, processDefinitionUUID, context, useInitialVariableValues);
     }
 
     public void cancelProcessInstance(ProcessInstanceUUID piuuid) throws Exception {
     	logger.debug("cancelProcessInstance");
         initContext();
         runtimeAPI.cancelProcessInstance(piuuid);
     }
 
     public void addComment(ProcessInstanceUUID piuuid, String message, String userId) throws InstanceNotFoundException, Exception {
     	logger.debug("addComment");
         initContext();
         runtimeAPI.addComment(piuuid, message, userId);
     }
 
     public void addComment(ActivityInstanceUUID aiuuid, String message, String userId) throws InstanceNotFoundException, Exception {
     	logger.debug("addComment");
         initContext();
         runtimeAPI.addComment(aiuuid, message, userId);
     }
 
     public List<Comment> getCommentFeed(ProcessInstanceUUID piuuid) throws InstanceNotFoundException, Exception {
     	logger.debug("getCommentFeed");
         initContext();
         return queryRuntimeAPI.getCommentFeed(piuuid);
     }
 
     public ProcessInstance getProcessInstance(ProcessInstanceUUID piuuid) throws Exception {
     	logger.debug("getProcessInstance");
         initContext();
         return queryRuntimeAPI.getProcessInstance(piuuid);
     }
 
     public List<User> getAllUsers() throws Exception {
     	logger.debug("getAllUsers");
         initContext();
         return identityAPI.getAllUsers();
     }
 
     public List<Role> getAllRoles() throws Exception {
     	logger.debug("getAllRoles");
         initContext();
         return identityAPI.getAllRoles();
     }
     
     public Role findRoleByName(String name)throws Exception{
     	logger.debug("findRoleByName");
     	for(Role role:getAllRoles())
     	{
     		if(role.getName().equalsIgnoreCase(name))
     			return role;
     	}
     	return null;
     }
     
     public Group findGroupByName(String name)throws Exception{
     	logger.debug("findGroupByName");
     	for(Group g:getAllGroups()){
     		if(g.getName().equalsIgnoreCase(name))
     			return g;
     	}
     	return null;
     }
 
     public List<Group> getAllGroups() throws Exception {
     	logger.debug("getAllGroups");
         initContext();
         return identityAPI.getAllGroups();
     }
 
     public List<ProfileMetadata> getAllProfileMetadata() throws Exception {
     	logger.debug("getAllProfileMetadata");
         initContext();
         return identityAPI.getAllProfileMetadata();
     }
 
     public User addUser(String username, String password, String firstName, String lastName, String title, String jobTitle, String managerUserUUID, Map<String, String> profileMetadata) throws Exception {
     	logger.debug("addUser");
         initContext();
         return identityAPI.addUser(username, password, firstName, lastName, title, jobTitle, managerUserUUID, profileMetadata);
     }
 
     public void removeUserByUUID(String userUUID) throws Exception {
     	logger.debug("removeUserByUUID");
         initContext();
         identityAPI.removeUserByUUID(userUUID);
     }
 
     public ProfileMetadata addProfileMetadata(String name, String label) throws Exception {
     	logger.debug("addProfileMetadata");
         initContext();
         return identityAPI.addProfileMetadata(name, label);
     }
 
     public ProfileMetadata addProfileMetadata(String name) throws Exception {
     	logger.debug("addProfileMetadata");
         initContext();
         return identityAPI.addProfileMetadata(name);
     }
 
     public void removeProfileMetadataByUUID(String profileMetadataUUID) throws Exception {
     	logger.debug("removeProfileMetadataByUUID");
         initContext();
         identityAPI.removeProfileMetadataByUUID(profileMetadataUUID);
     }
 
     public Role addRole(String name, String label, String description) throws Exception {
     	logger.debug("addRole");
         initContext();
         return identityAPI.addRole(name, label, description);
     }
 
     public void removeRoleByUUID(String roleUUID) throws Exception {
     	logger.debug("removeRoleByUUID");
         initContext();
         identityAPI.removeRoleByUUID(roleUUID);
     }
 
     public Role updateRoleByUUID(String roleUUID, String name, String label, String description) throws Exception {
     	logger.debug("updateRoleByUUID");
         initContext();
         return identityAPI.updateRoleByUUID(roleUUID, name, label, description);
     }
 
     public ProfileMetadata updateProfileMetadataByUUID(String profileMetadataUUID, String name, String label) throws Exception {
     	logger.debug("updateProfileMetadataByUUID");
         initContext();
         return identityAPI.updateProfileMetadataByUUID(profileMetadataUUID, name, label);
     }
 
     public Group addGroup(String name, String label, String description, String parentGroupUUID) throws Exception {
     	logger.debug("addGroup");
         initContext();
         return identityAPI.addGroup(name, label, description, parentGroupUUID);
     }
 
     public Group addGroup(String name, String parentGroupUUID) throws Exception {
     	logger.debug("addGroup");
         initContext();
         return identityAPI.addGroup(name, parentGroupUUID);
     }
 
     public Group updateGroupByUUID(String groupUUID, String name, String label, String description, String parentGroupUUID) throws Exception {
     	logger.debug("updateGroupByUUID");
         initContext();
         return identityAPI.updateGroupByUUID(groupUUID, name, label, description, parentGroupUUID);
     }
 
     public void removeGroupByUUID(String groupUUID) throws Exception {
     	logger.debug("removeGroupByUUID");
         initContext();
         identityAPI.removeGroupByUUID(groupUUID);
     }
 
     public void updateUserProfessionalContactInfo(String userUUID, String email, String phoneNumber, String mobileNumber, String faxNumber, String building, String room, String address, String zipCode, String city, String state, String country, String website) throws Exception {
     	logger.debug("updateUserProfessionalContactInfo");
         initContext();
         identityAPI.updateUserProfessionalContactInfo(userUUID, email, phoneNumber, mobileNumber, faxNumber, building, room, address, zipCode, city, state, country, website);
     }
 
     public void updateUserPersonalContactInfo(String userUUID, String email, String phoneNumber, String mobileNumber, String faxNumber, String building, String room, String address, String zipCode, String city, String state, String country, String website) throws Exception {
     	logger.debug("updateUserPersonalContactInfo");
         initContext();
         identityAPI.updateUserPersonalContactInfo(userUUID, email, phoneNumber, mobileNumber, faxNumber, building, room, address, zipCode, city, state, country, website);
     }
 
     public User updateUserByUUID(String userUUID, String username, String firstName, String lastName, String title, String jobTitle, String managerUserUUID, Map<String, String> profileMetadata) throws Exception {
     	logger.debug("updateUserByUUID");
         initContext();
         return identityAPI.updateUserByUUID(userUUID, username, firstName, lastName, title, jobTitle, managerUserUUID, profileMetadata);
     }
 
     public User updateUserPassword(String userUUID, String password) throws Exception {
     	logger.debug("updateUserPassword");
         initContext();
         return identityAPI.updateUserPassword(userUUID, password);
     }
     
     public void updateUserMetadata(User user, String metaKey, String metaValue) throws Exception{
     	initContext();
     	
     	ProfileMetadata foundProfileMetadata=null;
     	List<ProfileMetadata> profileMetadatas = getAllProfileMetadata();
     	for (ProfileMetadata profileMetadata : profileMetadatas) {
 			if(metaKey.equals(profileMetadata.getName()))
 				{
 					foundProfileMetadata=profileMetadata;
 					break;
 				}
 		}
     	if(foundProfileMetadata==null){
     		addProfileMetadata(metaKey, metaKey);
     	}
     	
     	
     	Map<String, String> metadata=new Hashtable<String, String>();
     	 for (ProfileMetadata profileMetadata1 : user.getMetadata().keySet()) {
              if (profileMetadata1.getName().equals(metaKey)) {
                  metadata.put(metaKey, metaValue);
              }
              else
              {
             	 metadata.put(profileMetadata1.getName(), user.getMetadata().get(profileMetadata1.getName()));
              }
          }
     	if(!metadata.containsKey(metaKey)){
     		metadata.put(metaKey, metaValue);
     	}
 		identityAPI.updateUserByUUID(user.getUUID(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getTitle(), user.getJobTitle(), user.getManagerUUID(), metadata);
     }
 
     public void setUserMemberships(String userUUID, Collection<String> membershipUUIDs) throws Exception {
     	logger.debug("setUserMemberships");
         initContext();
         identityAPI.setUserMemberships(userUUID, membershipUUIDs);
     }
 
     public void addMembershipToUser(String userUUID, String membershipUUID) throws Exception {
     	logger.debug("addMembershipToUser");
         initContext();
         identityAPI.addMembershipToUser(userUUID, membershipUUID);
     }
 
     public void addMembershipsToUser(String userUUID, Collection<String> membershipUUIDs) throws Exception {
     	logger.debug("addMembershipsToUser");
         initContext();
         identityAPI.addMembershipsToUser(userUUID, membershipUUIDs);
     }
 
     public Membership getMembershipByUUID(String membershipUUID) throws Exception {
     	logger.debug("getMembershipByUUID");
         initContext();
         return identityAPI.getMembershipByUUID(membershipUUID);
     }
 
     public void removeMembershipFromUser(String userUUID, String membershipUUID) throws Exception {
     	logger.debug("removeMembershipFromUser");
         initContext();
         identityAPI.removeMembershipFromUser(userUUID, membershipUUID);
     }
 
     public void removeMembershipsFromUser(String userUUID, Collection<String> membershipUUIDs) throws Exception {
     	logger.debug("removeMembershipsFromUser");
         initContext();
         identityAPI.removeMembershipsFromUser(userUUID, membershipUUIDs);
     }
 
     public Membership getMembershipForRoleAndGroup(String roleUUID, String groupUUID) throws Exception {
     	logger.debug("getMembershipForRoleAndGroup");
         initContext();
         return identityAPI.getMembershipForRoleAndGroup(roleUUID, groupUUID);
     }
     
     public String getUserMetadataValue(User user, String metadataName) {
     	 
          for (ProfileMetadata profileMetadata : user.getMetadata().keySet()) {
              if (profileMetadata.getName().equals(metadataName)) {
                  return user.getMetadata().get(profileMetadata);
              }
          }
          return null;
     }
 
     public User findUserByUserName(String userName) throws Exception {
     	logger.debug("findUserByUserName:"+ userName);
         initContext();
         return identityAPI.findUserByUserName(userName);
     }
 
     public boolean checkUserCredentials(String username, String password) throws Exception {
     	logger.debug("checkUserCredentials");
     	initContext();
     	if(username==USER_GUEST){
     		if(findUserByUserName(USER_GUEST) == null)
     			addUser(username, "guest", "", "", "", "", null, null);
     		return true;
     	}        
         return managementAPI.checkUserCredentials(username, password);
     }
 
     public Rule findRule(String ruleName) throws Exception {
     	logger.debug("findRule");
         initContext();
         for (Rule rule : managementAPI.getAllRules()) {
             if (rule.getName().equals(ruleName)) {
                 return rule;
             }
         }
         return null;
     }
 
     public void applyRuleToEntities(final String ruleUUID, final Collection<String> userUUIDs, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws Exception {
     	logger.debug("applyRuleToEntities");
         initContext();
         managementAPI.applyRuleToEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
     }
 
     public void removeRuleFromEntities(final String ruleUUID, final Collection<String> userUUIDs, final Collection<String> roleUUIDs, final Collection<String> groupUUIDs, final Collection<String> membershipUUIDs, final Collection<String> entityIDs) throws Exception {
     	logger.debug("removeRuleFromEntities");
         initContext();
         managementAPI.removeRuleFromEntities(ruleUUID, userUUIDs, roleUUIDs, groupUUIDs, membershipUUIDs, entityIDs);
     }
 
     public void addMetaData(String key, String value) throws Exception {
     	logger.debug("addMetaData");
         initContext();
         managementAPI.addMetaData(key, value);
     }
 
     public String  getMetaData(String key) throws Exception {
     	logger.debug("getMetaData");
         initContext();
         return managementAPI.getMetaData(key);
     }
 
     public String getUserMetadata(String metadataName) throws Exception {
     	logger.debug("getUserMetadata");
         initContext();
         User user = identityAPI.findUserByUserName(currentUserUID);
         for (ProfileMetadata profileMetadata : user.getMetadata().keySet()) {
             if (profileMetadata.getName().equals(metadataName)) {
                 return user.getMetadata().get(profileMetadata);
             }
         }
         return null;
     }
     
     public <T extends Object> T execute(Command<T> cmnd) throws Exception{
     	logger.debug("execute");
         initContext();
         return commandAPI.execute(cmnd);
     }
 
 	
 
 	
 }
