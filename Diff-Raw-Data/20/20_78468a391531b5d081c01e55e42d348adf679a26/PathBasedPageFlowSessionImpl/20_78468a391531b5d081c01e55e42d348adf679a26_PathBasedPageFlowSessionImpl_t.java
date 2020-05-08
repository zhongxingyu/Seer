 package org.flowframe.bpm.jbpm.ui.pageflow.services.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import org.flowframe.bpm.jbpm.services.IBPMProcessInstance;
 import org.flowframe.bpm.jbpm.services.IBPMService;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowManager;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowPage;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.IPageFlowSession;
 import org.flowframe.bpm.jbpm.ui.pageflow.services.impl.path.PageFlowPathAssessor;
import org.flowframe.bpm.jbpm.ui.pageflow.vaadin.wizard.TaskWizard;
 import org.flowframe.kernel.common.mdm.domain.application.Feature;
 import org.flowframe.kernel.common.utils.Validator;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef;
 import org.jbpm.task.AccessType;
 import org.jbpm.task.Status;
 import org.jbpm.task.Task;
 import org.jbpm.task.query.TaskSummary;
 import org.jbpm.task.service.ContentData;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.vaadin.mvp.eventbus.EventBus;
 import org.vaadin.mvp.presenter.IPresenter;
 
 import com.vaadin.ui.Component;
 
 public class PathBasedPageFlowSessionImpl implements IPageFlowSession {
 	private static final int WAIT_DELAY = 1000;
 
 	private Map<String, IPageFlowPage> pages;
 	private ProcessInstanceRef processInstance;
 	private TaskWizard wizard;
 	private IBPMService bpmService;
 	private List<org.jbpm.workflow.core.node.HumanTaskNode> tasks;
 	private Task currentTask;
 	private EntityManagerFactory emf;
 
 	private Map<String, Object> processVars;
 
 	private String userId;
 
 	private Feature onCompletionFeature;
 	private IPresenter<?, ? extends EventBus> onCompletionViewPresenter;	
 
 	private IPageFlowManager engine;
 
 	private PageFlowPathAssessor pathAssessor;
 	
 	public PathBasedPageFlowSessionImpl(
 			String userId,
 			ProcessInstanceRef pi,
 			IPageFlowManager engine,
 			PageFlowPathAssessor pathAssessor,
 			Feature onCompletionFeature, 
 			IPresenter<?, ? extends EventBus> onCompletionViewPresenter) throws Exception {
 		this.onCompletionFeature = onCompletionFeature;
 		this.onCompletionViewPresenter = onCompletionViewPresenter;
 		this.engine  = engine;
 		this.processInstance = pi;
 		this.userId = userId;
 		this.pathAssessor = pathAssessor;
 		try {
 			
 			bpmService = engine.getBPMService();
 			
 			//Get current task
 			currentTask = waitForNextTask();
 			
 			// Update current task description
 			this.bpmService.addTaskComment(pathAssessor.getCurrentTaskId(),"Receive RL123/Confirm TruckInfo" /*pathAssessor.getCurrentPage().getDescription()*/);
 						
 			
 			//Nominate task
 			bpmService.nominate(currentTask.getId(), userId);
 		
 			//Get HT input VARS
 			processVars = new HashMap<String,Object>();
 			processVars.put("Content", this.bpmService.getTaskContentObject(currentTask));
 		} catch (Exception e) {
 			throw e;
 		}			
 	}
 
 	private Task waitForNextTask() throws Exception {
 		List<Task> tasks = new ArrayList<Task>();
 		int count = 0;
 		Thread.sleep(1000L);
 		while (count < 10) {
 			tasks = bpmService.getCreatedTasksByProcessId(Long
 					.parseLong(processInstance.getId()));
 			if (tasks.size() == 0) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			} else {
 				break;
 			}
 			count++;
 		}
 		if (count == 10) {
 			throw new Exception("waitForNextTask Timed out");
 		}
 		return tasks.get(0);
 	}
 	
 	private void waitForTaskCompleteness(UserTransaction ut) throws Exception {
 		Task res = null;
 		int count = 0;
 		while (count < 10) {
 			ut.begin();
 			res = this.bpmService.getTaskObjectById(currentTask.getId());
 			ut.commit();
 			if (res.getTaskData().getStatus() != Status.Completed)
 			{
 				try {
 					Thread.sleep(1000L);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			else
 				break;
 			count++;
 		}
 		if (count == 10) {
 			throw new Exception("waitForTaskCompleteness on Task("+currentTask.getId()+" Timed out");
 		}
 	}	
 	
 
 	private Object waitForContentResult() throws Exception {
 		Object res = null;
 		int count = 0;
 		Thread.sleep(2000);
 		while (count < 10) {
 			res = this.bpmService.getTaskContentObject(currentTask);
 			if (res instanceof Map)
 			{
 				if (   (((Map<String,Object>)res).size() == 1 && ((Map<String,Object>)res).containsKey("TaskName")))
 				{
 					try {
 						Thread.sleep(WAIT_DELAY);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				else
 					break;
 			}
 			count++;
 		}
 		if (count == 10) {
 			throw new Exception("Content Wait on Task("+currentTask.getId()+" Timed out");
 		}
 
 		return res;
 	}
 
 	@Override
 	public IBPMProcessInstance getBPMProcessInstance() {
 		return null;
 	}
 
 	@Override
 	public Collection<IPageFlowPage> getPages() {
 		return pathAssessor.getCurrentOrderedPageList();
 	}
 
 	@Override
 	public Component getWizardComponent() {
 		return wizard;
 	}
 
 	@Override
 	public void nextPage() {
 	}
 
 	@Override
 	public void previousPage() {
 	}
 
 	public void start() {
 		try {
 			bpmService.startTask(currentTask.getId(), userId);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void abort() {
 	}
 
 	public TaskWizard getWizard() {
 		return wizard;
 	}
 
 	public Task getCurrentTask() {
 		return currentTask;
 	}
 	
 
 	public Feature getSetOnCompletionFeature() {
 		return onCompletionFeature;
 	}
 
 	@Override
 	public Map<String, Object> getProcessVars() {
 		return processVars;
 	}
 
 	@Override
 	public boolean executeNext(UserTransaction ut, Object param) throws Exception {
 		
 		boolean pagesChanged = false;
 		
 		// 1. Complete the current task first
 		try {
 			ut.begin();
 			if (param instanceof Map) {
 				bpmService.completeTask(currentTask.getId(), (Map) param,
 						userId);
 			} else {
 				ContentData contentData = null;
 				if (param != null) {
 					ByteArrayOutputStream bos = new ByteArrayOutputStream();
 					ObjectOutputStream out;
 					try {
 						out = new ObjectOutputStream(bos);
 						out.writeObject(param);
 						out.close();
 						contentData = new ContentData();
 						contentData.setContent(bos.toByteArray());
 						contentData.setAccessType(AccessType.Inline);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				bpmService.completeTask(currentTask.getId(), contentData,
 						userId);				
 			}
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}
 		
 		waitForTaskCompleteness(ut);		
 		
 		// 2. getNextTaskAndUpdatePagesPath
 		pagesChanged = getNextTaskAndUpdatePagesPath(ut);
 		
 		// 3. If the next task exists, nominate and start it
 		if (Validator.isNotNull(currentTask)) {
 			// 3.1 Nominate the current user for this task
 			try {
 				ut.begin();
 				bpmService.nominate(currentTask.getId(), userId);	
 				// Update current task description
 				this.bpmService.addTaskComment(pathAssessor.getCurrentTaskId(),"Receive RL456/Confirm TruckInfo" /*pathAssessor.getCurrentPage().getDescription()*/);
 				ut.commit();
 			} catch (Exception e) {
 				ut.rollback();
 				throw e;
 			}
 
 			// 3.2 Start the task
 			try {
 				ut.begin();
 				bpmService.startTask(currentTask.getId(), userId);
 				ut.commit();
 			} catch (Exception e) {
 				ut.rollback();
 				throw e;
 			}
 		}
 
 		
 		return pagesChanged;
 	}
 	
 	@Override
 	public boolean getNextTaskAndUpdatePagesPath(UserTransaction ut) throws SystemException, Exception {
 		boolean pagesChanged = false;
 		// 2. Get the next task after Proc resume
 		try {
 			ut.begin();
 			currentTask = null;
 			currentTask = waitForNextTask();
 			
 			//Get current HT node
 			TaskSummary ts = this.bpmService.getTaskSummaryByTaskId(currentTask.getId());
 			Object res = this.bpmService.getTaskContentObject(currentTask);
 			//String taskName = ts.getName();
 			//HumanTaskNode htTaskNode = this.bpmService.findHumanTaskNodeForTask(taskName, processInstance.getDefinitionId());
 			
 			//Reset path and pages
 			pagesChanged = pathAssessor.restActivePages(ts);
 			
 
 			//Apply vars
 			processVars = new HashMap<String,Object>();
 			processVars.put("Content", res);			
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}
 		return pagesChanged;
 	}
 
 	private IPageFlowPage getNextPage(IPageFlowPage currentPage) {
 		for (int i = 0; i < pathAssessor.getCurrentOrderedPageList().size(); i++) {
 			if (pathAssessor.getCurrentOrderedPageList().get(i).equals(currentPage)) {
 				if (i != pathAssessor.getCurrentOrderedPageList().size() - 1) {
 					return pathAssessor.getCurrentOrderedPageList().get(i + 1);
 				}
 			}
 		}
 		return null;
 	}
 	
 	public EntityManagerFactory getEmf() {
 		return emf;
 	}
 
 	public void setEmf(EntityManagerFactory emf) {
 		this.emf = emf;
 	}
 
 	public Feature getOnCompletionFeature() {
 		return onCompletionFeature;
 	}
 
 	public void setOnCompletionFeature(Feature onCompletionFeature) {
 		this.onCompletionFeature = onCompletionFeature;
 	}
 
 	@Override
 	public void completeProcess(UserTransaction ut, Object param) throws Exception {
 		// 1. Complete the current task first
 		try {
 			ut.begin();
 			if (param instanceof Map) {
 				bpmService.completeTask(currentTask.getId(), (Map) param,
 						userId);
 			} else {
 				ContentData contentData = null;
 				if (param != null) {
 					ByteArrayOutputStream bos = new ByteArrayOutputStream();
 					ObjectOutputStream out;
 					try {
 						out = new ObjectOutputStream(bos);
 						out.writeObject(param);
 						out.close();
 						contentData = new ContentData();
 						contentData.setContent(bos.toByteArray());
 						contentData.setAccessType(AccessType.Inline);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				bpmService.completeTask(currentTask.getId(), contentData,
 						userId);
 			}
 
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}
 
 		
 		try
 		{
 			ut.begin();
 			Thread.sleep(WAIT_DELAY);
 			List<Task> tasks = bpmService.getCreatedTasksByProcessId(Long
 						.parseLong(processInstance.getId()));
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}
 		
 	}
 
 	@Override
 	public Map<String, Object> updateProcessInstanceVariables(UserTransaction ut,
 			Map<String, Object> varsToUpdate) throws Exception {
 		try
 		{
 			ut.begin();
 			bpmService.setProcessInstanceVariables(processInstance.getId(),varsToUpdate);
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}
 		
 		Map<String, Object> procInstVars = null;
 		try
 		{
 			ut.begin();
 			procInstVars = bpmService.getProcessInstanceVariables(processInstance.getId());
 			processVars = procInstVars;
 			ut.commit();
 		} catch (Exception e) {
 			ut.rollback();
 			throw e;
 		}		
 		
 		return procInstVars;
 	}
 
 	@Override
 	public PlatformTransactionManager getJTAGlobalTransactionManager() {
 		return engine.getJTAGlobalTransactionManager();
 	}
 
 	@Override
 	public EntityManagerFactory getConXEntityManagerfactory() {
 		return engine.getConXEntityManagerfactory();
 	}
 
 	@Override
 	public IPageFlowManager getPageFlowEngine() {
 		return engine;
 	}
 
 	@Override
 	public boolean isOnLastPage() {
 		return this.pathAssessor.isOnLastPage();
 	}
 }
