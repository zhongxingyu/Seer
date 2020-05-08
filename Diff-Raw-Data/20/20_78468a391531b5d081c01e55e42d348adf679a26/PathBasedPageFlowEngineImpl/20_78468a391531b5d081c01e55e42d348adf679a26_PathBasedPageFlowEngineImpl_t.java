 package org.flowframe.bpm.jbpm.ui.pageflow.services.impl;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.naming.Context;
 import javax.persistence.EntityManagerFactory;
 import javax.transaction.UserTransaction;
 
 import org.drools.definition.process.Node;
 import org.drools.process.instance.WorkItemHandler;
 import org.flowframe.bpm.jbpm.services.IBPMService;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.ICustomDrivenPageFlowPage;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IModelDrivenPageFlowPage;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowManager;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowPage;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowSession;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.ITaskWizard;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.event.PageFlowPageChangedEvent;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.impl.path.PageFlowPathAssessor;
import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.wizard.TaskWizard;
 import org.flowframe.kernel.common.mdm.domain.application.Feature;
 import org.flowframe.kernel.common.mdm.domain.task.TaskDefinition;
 import org.flowframe.kernel.metamodel.dao.services.IEntityTypeDAOService;
 import org.flowframe.ui.services.contribution.IMainApplication;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jndi.JndiTemplate;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.vaadin.mvp.eventbus.EventBus;
 import org.vaadin.mvp.presenter.IPresenter;
 
 public class PathBasedPageFlowEngineImpl implements IPageFlowManager {
 
 	protected Logger logger = LoggerFactory.getLogger(this.getClass());
 	private IBPMService bpmService;
 	private EntityManagerFactory conxlogisticsEMF;
 	private PlatformTransactionManager globalTransactionManager;
 	private List<IPageFlowSession> sessions;
 
 	private JndiTemplate jndiTemplate;
 	private UserTransaction userTransaction;
 	
 	private IEntityTypeDAOService entityTypeDao;
 	/** EntityManagerFactories */
 	private final Map<String, Map<String,IPageFlowPage>> pageCache = Collections
 			.synchronizedMap(new HashMap<String, Map<String,IPageFlowPage>>());
 	private IMainApplication mainApp;
 
 	public PathBasedPageFlowEngineImpl() {
 		this.sessions = new ArrayList<IPageFlowSession>();
 	}
 
 	public void setJndiTemplate(JndiTemplate jndiTemplate) {
 		this.jndiTemplate = jndiTemplate;
 	}
 	
 
 	public void setUserTransaction(UserTransaction userTransaction) {
 		this.userTransaction = userTransaction;
 	}
 
 	
 	public IBPMService getBpmService() {
 		return bpmService;
 	}
 
 	@SuppressWarnings("unused")
 	public void setBpmService(IBPMService bpmService) {
 		try {
 			this.bpmService = bpmService;
 			//bpmService.startNewProcess("","");
 			System.out.println("Test");
 		} 
 		catch (Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 		}	
 		catch (Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 		}		
 	}
 
 	public void start() {
 		try 
 		{
 			//ProcessInstanceRef pi = bpmService.newInstance("defaultPackage.goat1");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 
 	}
 
 	public void stop() {
 	}
 
 	public IMainApplication getMainApp() {
 		return mainApp;
 	}
 
 	private Map<String,IPageFlowPage> getPages(String processId) {
 		return pageCache.get(processId);
 	}
 
 	@Override
 	public IPageFlowSession closePageFlowSession(String userid,
 			Long pageFlowSessionId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IPageFlowSession resumePageFlowSession(String userid,
 			Long pageFlowSessionId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public void bindEntityTypeDAOService(IEntityTypeDAOService entityTypeDAOService, Map properties) {
 		logger.debug("bindEntityTypeDAOService()");
 		this.entityTypeDao = entityTypeDAOService;
 	}
 
 	public void unbindEntityTypeDAOService(IEntityTypeDAOService entityTypeDAOService, Map properties) {
 		logger.debug("bindEntityTypeDAOService()");
 		this.entityTypeDao = null;
 	}
 
 	public void registerModelDrivenPageFlowPage(
 			IModelDrivenPageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(IPageFlowPage.PROCESS_ID);
 		String taskName = (String)properties.get(IPageFlowPage.TASK_NAME);
 		logger.debug("registerModelDrivenPageFlowPage("+processId+","+taskName+")");		
 		Map<String,IPageFlowPage> map = this.pageCache.get(processId);
 		if (map == null) {
 			map = new HashMap<String,IPageFlowPage>();
 			pageCache.put(processId, map);
 		} 
 		map.put(taskName,page);	
 	}
 
 	public void unregisterModelDrivenPageFlowPage(
 			IModelDrivenPageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(IPageFlowPage.PROCESS_ID);
 		logger.debug("unregisterModelDrivenPageFlowPage("+processId+")");	
 		Map<String,IPageFlowPage> map = this.pageCache.get(processId);
 		String taskName = (String)properties.get(IPageFlowPage.TASK_NAME);
 		if (map != null) {
 			map.remove(taskName);
 		}
 	}
 	
 	public void registerCustomDrivenPageFlowPage(
 			ICustomDrivenPageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(IPageFlowPage.PROCESS_ID);
 		String taskName = (String)properties.get(IPageFlowPage.TASK_NAME);
 		logger.debug("registerCustomDrivenPageFlowPage("+processId+","+taskName+")");		
 		Map<String,IPageFlowPage> map = this.pageCache.get(processId);
 		if (map == null) {
 			map = new HashMap<String,IPageFlowPage>();
 			pageCache.put(processId, map);
 		} 
 		map.put(taskName,page);	
 	}
 
 	public void unregisterCustomDrivenPageFlowPage(
 			ICustomDrivenPageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(IPageFlowPage.PROCESS_ID);
 		logger.debug("unregisterCustomDrivenPageFlowPage("+processId+")");	
 		Map<String,IPageFlowPage> map = this.pageCache.get(processId);
 		String taskName = (String)properties.get(IPageFlowPage.TASK_NAME);
 		if (map != null) {
 			map.remove(taskName);
 		}
 	}
 
 	@Override
 	public IPageFlowSession startPageFlowSession(String userId, TaskDefinition td) {
 		//		ProcessInstanceRef pi = bpmService.newInstance(td.getProcessId());
 		//IPageFlowSession session = new PageFlowSessionImpl(null, userId, getPages(td.getProcessId()), this.bpmService, conxlogisticsEMF);
 		//sessions.add(session);
 		return null;
 	}
 
 	public EntityManagerFactory getConxlogisticsEMF() {
 		return conxlogisticsEMF;
 	}
 
 	public void setConxlogisticsEMF(EntityManagerFactory conxlogisticsEMF) {
 		this.conxlogisticsEMF = conxlogisticsEMF;
 	}
 
 	public PlatformTransactionManager getGlobalTransactionManager() {
 		return globalTransactionManager;
 	}
 
 	public void setGlobalTransactionManager(
 			PlatformTransactionManager globalTransactionManager) {
 		this.globalTransactionManager = globalTransactionManager;
 	}
 
 	@Override
 	public ITaskWizard createTaskWizard(Map<String, Object> properties) throws Exception {
 		ProcessInstanceRef pi = null;
 		IPageFlowSession session = null;
 
 		String processId = (String)properties.get("processId");
 		String userId = (String)properties.get("userId");
 		Feature   onCompletionFeature = (Feature)properties.get("onCompletionFeature");
 		@SuppressWarnings("unchecked")
 		IPresenter<?, ? extends EventBus> onCompletionCompletionViewPresenter = (IPresenter<?, ? extends EventBus>)properties.get("onCompletionViewPresenter");
 		
 		
 		Context ctx = jndiTemplate.getContext();
 		UserTransaction ut = this.userTransaction;//(UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 
 		// 1. Create process instance		
 		try
 		{
 			ut.begin();
 			
 			Map<String, Object> input = new HashMap<String, Object>();
 			Map<String, Object> paramsMap = new HashMap<String, Object>();
 			paramsMap.put("onCompletionFeatureId", onCompletionFeature.getId());
 			input.put("paramsMap", paramsMap);
 			pi = bpmService.newInstance(processId,input);
 			
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);				 
 			ut.rollback();
 			throw e;
 		}
 
 		// 2. Create session
 		PageFlowPathAssessor pathAssessor = null;	
 		try
 		{
 			ut.begin();
 
 			//Get registered pages
 			Map<String,IPageFlowPage> pageList = pageCache.get(processId);
 			Map<String,WorkItemHandler> wihCache = this.bpmService.getRegisteredWIHByDefinitionId(processId);			
 			
 			//All process paths
 			Map<String, List<Node>> paths = this.bpmService.findAllNodePaths(processId);
 
 			
 			//Create start path
 			if (paths.size() == 1)//No splits
 			{
 				pathAssessor = new PageFlowPathAssessor(
 										 pi.getId(),
 										 bpmService,
 										 paths.keySet().iterator().next(),
 									     paths.values().iterator().next(),
 									     paths,
 									     pageCache.get(processId));
 			}
 			else //At least one split is in the process
 			{
 				SortedSet<String> orderedPathSet = new TreeSet<String>(paths.keySet());
 				String startPathKey = orderedPathSet.iterator().next();//Get smallest
 				pathAssessor = new PageFlowPathAssessor(
 						 pi.getId(),
 						 bpmService,
 						 startPathKey,
 					     paths.get(startPathKey),
 					     paths,
 					     pageCache.get(processId));				
 			}
 			
 			session = new PathBasedPageFlowSessionImpl(userId,
 													   pi,
 													   this, 
 													   pathAssessor,
 													   onCompletionFeature,
 													   onCompletionCompletionViewPresenter);
 
 			sessions.add(session);	
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			ut.rollback();
 			throw e;
 		}		 
 
 		// 3. Start first task in process
 		try
 		{
 			ut.begin();
 
 			session.start();
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			ut.rollback();
 			throw e;
 		}		
 		
 		
 		// 4. Create wizard
 		TaskWizard wizard = new TaskWizard(session, properties);
 		wizard.setSizeFull();
 
 
 		return wizard;
 	}
 	
 	@Override
 	public ITaskWizard resumeProcessInstanceTaskWizard(String processInstanceId, Map<String, Object> properties) throws Exception {
 		ProcessInstanceRef pi = null;
 		IPageFlowSession session = null;
 
 		String processId = (String)properties.get("processId");
 		String userId = (String)properties.get("userId");
 		
 		Feature   onCompletionFeature = (Feature)properties.get("onCompletionFeature");
 		IPresenter<?, ? extends EventBus>   onCompletionCompletionViewPresenter = (IPresenter<?, ? extends EventBus>)properties.get("onCompletionViewPresenter");
 		
 
 		Context ctx = jndiTemplate.getContext();
 		UserTransaction ut = this.userTransaction;//(UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 
 		// 1. Create process instance		
 		try
 		{
 			ut.begin();
 
 			pi = bpmService.getProcessInstance(processInstanceId);
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);				 
 			ut.rollback();
 			throw e;
 		}
 
 		// 2. Create session
 		PageFlowPathAssessor pathAssessor = null;	
 		try
 		{
 			ut.begin();
 
 			//Get registered pages
 			Map<String,IPageFlowPage> pageList = pageCache.get(processId);
 			Map<String,WorkItemHandler> wihCache = this.bpmService.getRegisteredWIHByDefinitionId(processId);			
 			
 			//All process paths
 			Map<String, List<Node>> paths = this.bpmService.findAllNodePaths(processId);
 
 			
 			//Create start path
 			if (paths.size() == 1)//No splits
 			{
 				pathAssessor = new PageFlowPathAssessor(
 										 pi.getId(),
 										 bpmService,						
 										 paths.keySet().iterator().next(),
 									     paths.values().iterator().next(),
 									     paths,
 									     pageCache.get(processId));
 			}
 			else //At least one split is in the process
 			{
 				SortedSet<String> orderedPathSet = new TreeSet<String>(paths.keySet());
 				String startPathKey = orderedPathSet.iterator().next();//Get smallest
 				pathAssessor = new PageFlowPathAssessor(
 						 pi.getId(),
 						 bpmService,						
 						 startPathKey,
 					     paths.get(startPathKey),
 					     paths,
 					     pageCache.get(processId));				
 			}
 	
 			
 			session = new PathBasedPageFlowSessionImpl(userId,
 													   pi,
 													   this, 
 													   pathAssessor,
 													   onCompletionFeature,
 													   onCompletionCompletionViewPresenter);
 			
 			//-- Reset session to instance state
 			session.getNextTaskAndUpdatePagesPath(ut);			
 
 			sessions.add(session);	
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			ut.rollback();
 			throw e;
 		}		 
 		
 		
 		// 3. Create wizard
 		TaskWizard wizard = new TaskWizard(session, properties);
 		wizard.setSizeFull();
 		
 
 		return wizard;
 	}
 	
 
 	@Override
 	public ITaskWizard executeTaskWizard(ITaskWizard tw, Object data) throws Exception {
 		if (((TaskWizard)tw).currentStepIsLastStep())
 		{
 			((TaskWizard)tw).getSession().completeProcess(this.userTransaction,data);
 		}
 		else
 		{
 			boolean pagesChanged = ((TaskWizard)tw).getSession().executeNext(this.userTransaction,data);
 			if (pagesChanged)
 			{
 				((TaskWizard)tw).onPagesChanged();
 			}
 		}
 		return tw;
 	}
 	
 
 	@Override
 	public Map<String, Object> updateProcessInstanceVariables(ITaskWizard tw,
 			Map<String, Object> varsToUpdate) throws Exception {
 		Map<String, Object> procInstVars = ((TaskWizard)tw).getSession().updateProcessInstanceVariables(this.userTransaction,varsToUpdate);
 		((TaskWizard) tw).fireOnPageFlowChanged(new PageFlowPageChangedEvent(procInstVars));
 		return procInstVars;
 	}
 
 	@Override
 	public EntityManagerFactory getConXEntityManagerfactory() {
 		return conxlogisticsEMF;
 	}
 
 	@Override
 	public PlatformTransactionManager getJTAGlobalTransactionManager() {
 		return globalTransactionManager;
 	}
 
 	@Override
 	public IBPMService getBPMService() {
 		return bpmService;
 	}
 
 	public void setEntityTypeDAOService(IEntityTypeDAOService entityTypeDaoService) {
 		this.entityTypeDao = entityTypeDaoService;
 	}
 
 	@Override
 	public IEntityTypeDAOService getEntityTypeDAOService() {
 		return entityTypeDao;
 	}
 
 	@Override
 	public UserTransaction getUserTransaction() {
 		return this.userTransaction;
 	}
 }
