 /**
  * Copyright (c) 2000-2007, Shakarchi Asaf
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package velo.ejb.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.EJBException;
 import javax.ejb.SessionContext;
 import javax.ejb.Stateless;
 import javax.ejb.Timeout;
 import javax.ejb.Timer;
 import javax.ejb.TimerService;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.jms.BytesMessage;
 import javax.jms.DeliveryMode;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Queue;
 import javax.jms.QueueConnection;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.QueueSession;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.transaction.TransactionManager;
 
 import org.apache.log4j.Logger;
import org.jboss.annotation.IgnoreDependency;
import org.jboss.annotation.ejb.TransactionTimeout;
 import org.jboss.ejb3.annotation.IgnoreDependency;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.Name;
 import velo.common.SysConf;
 import velo.contexts.OperationContext;
 import velo.ejb.interfaces.AdapterManagerLocal;
 import velo.ejb.interfaces.CommonUtilsManagerLocal;
 import velo.ejb.interfaces.EventManagerLocal;
 import velo.ejb.interfaces.ResourceManagerLocal;
 import velo.ejb.interfaces.ResourceOperationsManagerLocal;
 import velo.ejb.interfaces.TaskManagerLocal;
 import velo.ejb.interfaces.TaskManagerRemote;
 import velo.ejb.interfaces.TaskStatusManager;
 import velo.entity.BulkTask;
 import velo.entity.GenericTask;
 import velo.entity.ResourceTask;
 import velo.entity.SpmlTask;
 import velo.entity.Task;
 import velo.entity.TaskDefinition;
 import velo.entity.Task.TaskStatus;
 import velo.exceptions.CannotRequeueTaskException;
 import velo.exceptions.CollectionLoadingException;
 import velo.exceptions.EventExecutionException;
 import velo.exceptions.ExecutionException;
 import velo.exceptions.FactoryException;
 import velo.exceptions.NoResultFoundException;
 import velo.exceptions.OperationException;
 import velo.exceptions.TaskExecutionException;
 import velo.patterns.Factory;
 import velo.tasks.TaskExecuter;
 /**
  * A Stateless EJB bean for managing Tasks
  *
  * @author Asaf Shakarchi
  */
 /*
 @EJBs({
     //Required by the reconciliation's action. (see velo.reconcilidation.FullReconcileProcessAction.execute())
     @EJB(name="reconcileEjbRef", beanInterface=ReconcileManagerLocal.class),
     @EJB(name="resourceEjbRef", beanInterface=resourceManagerLocal.class),
     //Required by actions that are executed by this Bean in order to retrieve an adapter from the adapter pools
     @EJB(name="adapterEjbRef", beanInterface=AdapterManagerLocal.class),
     //Required by Reconcile Users
     @EJB(name="userEjbRef", beanInterface=UserManagerLocal.class),
     @EJB(name="accountEjbRef", beanInterface=AccountManagerLocal.class),
     
     //Required by RECONCILE IDENTITY ATTRIBUTES ACTION!
     @EJB(name="resourceAttributeEjbRef", beanInterface=resourceAttributeManagerLocal.class),
     @EJB(name="identityAttributeEjbRef", beanInterface=IdentityAttributeManagerLocal.class)
 })*/
         @Name("taskManager")
         @Stateless()
         @AutoCreate
         //@TransactionManagement(TransactionManagementType.BEAN)
         public class TaskBean implements TaskManagerLocal, TaskManagerRemote {
     /**
      * Injected entity manager
      */
     @PersistenceContext
     public EntityManager em;
     
     @javax.annotation.Resource
     TimerService timerService;
     
     @javax.annotation.Resource
     private SessionContext sessionContext;
     
     @EJB
     CommonUtilsManagerLocal cum;
     
     @org.jboss.annotation.IgnoreDependency
 	@IgnoreDependency
     @EJB
     EventManagerLocal eventManager;
     
     @EJB
     ResourceManagerLocal resourceManager;
     
     @EJB
     @org.jboss.annotation.IgnoreDependency
 	@IgnoreDependency
     ResourceOperationsManagerLocal resourceOperationsManager;
     
     @EJB
     AdapterManagerLocal adapterManager;
     
     @Resource
     SessionContext ejbContext; 
     
     //@Resource
     //private UserTransaction utx;
     
     
     @EJB
     TaskStatusManager taskStatusManager;
     
     
     
     private static Logger log = Logger.getLogger(TaskBean.class.getName());
     
     
     //CONFS
     private static final String eventTaskFailure = "TASK_FAILURE";
     
     
     public void persistTask(Task task) {
         em.persist(task);
     }
     
     public void updateTask(Task task) {
         em.merge(task);
     }
     
     //This is -just- the execution, not taking care of statuses or anything else.
     //Private as it's only used by this bean internally.
     private void taskExecutionOnly(Task task) throws TaskExecutionException {
     	if (task.getTaskExecuterClassName() != null) {
     		executeTaskViaTaskExecuter(task);
     	} else {
     		//For tasks that were not migrated to task executers yet.
     		//HANDLES TASK STATUS ALREADY
     		executeTaskViaQueue(task);
     	}
     }
     
     
     
     
     //This method is the public one and should be used from other external places
     public void executeTask(Task task) throws TaskExecutionException {
     	log.trace("Executing task ID '" + task.getTaskId() + "'");
     	
     	//If async, just persist the task, will be executed by the standard task queues
     	if (task.getAsync()) {
     		persistTask(task);
     	} else {
     		//execute the task now.
     		taskExecutionOnly(task);
     	}
     }
     
     
     public void resetRunningTask(Task task) {
     	if (task.getStatus() == TaskStatus.RUNNING) {
     		task.setStatus(TaskStatus.PENDING);
     		task.addLog("INFO", "Reset task status.", "Reset task status from RUNNING to PENDING.");
     		
     		setTaskStatus(TaskStatus.PENDING,task);
     	}else {
     		log.error("Cannot reset running task ID '" + task.getTaskId() + "' as its status is not RUNNING (status is '" + task.getStatus() +"')");
     	}
     }
     
     
     //This method should only be used by the MDB Task Executer.
     //It handles statuses of tasks and assume that the execution is async.
     public void executeTaskAsync(Task task) throws TaskExecutionException {
     	/*
     	UserTransaction transaction = ejbContext.getUserTransaction();
     	
     	try {
     		System.out.println("!!!!!!!!!!!!!!!!!!!!B1: " + transaction.getStatus());
     		transaction.begin();
     		System.out.println("!!!!!!!!!!!!!!!!!!!!B2: " + transaction.getStatus());
     	}catch(NotSupportedException e) {
     		e.printStackTrace();
     	}catch (SystemException e) {
     		e.printStackTrace();
     	}
     	*/
     	
     	log.trace("Making sure Task is an instance of SPML Task, that is currently the only supported type");
     	
     	if (task.getStatus() == TaskStatus.RUNNING) {
     		log.warn("Task is already in RUNNING state, skipping task execution for task ID: " + task.getTaskId());
     		return;
     	}
     	
     	if ( (task.getStatus() != TaskStatus.PENDING) && (task.getStatus() != TaskStatus.FAILURE) && (task.getStatus() != TaskStatus.FATAL_ERROR) ) {
     		log.warn("Task status is '" + task.getStatus() + "' and cannot be executed.");
     		task.addLog("WARN", "Task could not be executed", "Task status is '" + task.getStatus() + "' and cannot be executed.");
     		em.merge(task);
     		return;
     	}
     	
     	
     	InitialContext ctx = null;
     	TransactionManager tm = null;
         try {
           ctx = new InitialContext();
           tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
         }catch(Exception e) {
         	e.printStackTrace();
         }
         finally {
            if(ctx!=null) {
               try { ctx.close(); } catch (NamingException e) {}
            }
         }
            
         /*
     	//System.out.println("!!!!!!!!!!!!!!!B1: " + em.getFlushMode());
     	try {
     		//System.out.println("!!!!!!!!!!!!!!!B1: " + tm.getStatus());
     		//System.out.println("!!!!!!!!!!!!!!!B1: " + tm.getTransaction().getStatus());
     	}catch(SystemException e) {
     		e.printStackTrace();
     	}
     	*/
         indicateTaskAsRunning(task);
     	
     	
     	
         try {
         	taskExecutionOnly(task);
         	task.setLastExecutionDate(new Date());
         	indicateTaskExecutionSuccess(task);
         }catch (TaskExecutionException e) {
         	task.setLastExecutionDate(new Date());
         	indicateTaskExecutionFailure(task, e.getMessage());
         }
     }
     
     //This method really execute the task via the task executer 
     //really does the work no matter if the task is sync/async
     public void executeTaskViaTaskExecuter(Task task) throws TaskExecutionException {
     	//factory task executer
     	try {
 			TaskExecuter taskExec = (TaskExecuter)Factory.factoryInstance(task.getTaskExecuterClassName());
 			taskExec.execute(task);
 		} catch (FactoryException e) {
 			throw new TaskExecutionException(e.getMessage());
 		}
     }
     
     
     
     
     
     
     
     
     public void indicateTaskExecutionFailure(Task task, String errorMsg) {
         log.warn("Indicating task execution failure for task ID: " + task.getTaskId());
         task.addLog("ERROR", errorMsg, null);
         setTaskStatus(TaskStatus.FATAL_ERROR, task);
         
         //invoke event
         //EventDefinition ed = eventManager.find(eventTaskFailure);
         OperationContext context = new OperationContext();
 		context.addVar("task", task);
 		try {
 			eventManager.raiseSystemEvent("TASK_FAILURE", context);
 		} catch (EventExecutionException e) {
 			log.error("Error while trying to execute system event 'TASK_FAILURE': " + e.toString());
 		}
 		
 		
 //		try {
 //			//eventManager.invokeEventDefinitionResponses(ed, context);
 //			//eventManager.invokeEvent(ed, context);
 //		} catch (ScriptInvocationException e) {
 //			log.error("Error while trying to execute event definition response: " + e.toString());
 //		}
         
         
         //int failures = task.getFailureCounts() + 1;
         //task.setFailureCounts(failures);
         
         /*
         //Check whether we have more retries or not
         if (task.getFailureCounts() < task.getTaskDefinition().getFailureRetries()) {
             status = TaskStatus.FAILURE;
             
         } else {
             status = TaskStatus.FATAL_ERROR;
         }
         
         //23-01-07 -> SET VIA the EventEJB(This class), not directy
         //task.setStatus(status);
         setTaskStatus(status,task);
         
         task.setExecutionDate(new Date());
         
         //Important, unlock the task, otherwise it wont get fired by the scanner again.
         task.setInProcess(false);
         
         updateTask(task);
         */
     }
     
     
     public void indicateTaskExecutionSuccess(Task task) {
     	//currently do not delete any tasks
     	//TODO: Support task behaviors
     	setTaskStatus(TaskStatus.SUCCESS,task);
     	
         /*OLD
     	if (task.getTaskDefinition().isDeleteTaskAfterExecution()) {
             logger.info("SUCCESSFULY executed task ID: " + task.getTaskId() + ", deleting task.");
             //Set the status first (since we do not really delete the task but only flag it as deleted)
             
             //23-01-07 -> SET VIA the EventEJB(This class), not directy
             //task.setStatus("SUCCESS");
             setTaskStatus(TaskStatus.SUCCESS,task);
             task.setExecutionDate(new Date());
             
             deleteTask(task);
         } else {
             logger.info("Indicate a SUCCESS for task ID: " + task.getTaskId());
             //23-01-07 -> SET VIA the EventEJB(This class), not directy
             //task.setStatus("SUCCESS");
             setTaskStatus(TaskStatus.SUCCESS,task);
             
             task.setExecutionDate(new Date());
             updateTask(task);
             logger.trace("New STATUS is: " + task.getStatus());
         }
         */
     }
     
     public void indicateTaskAsRunning(Task task) {
     	setTaskStatus(TaskStatus.RUNNING, task);
     }
     
     
     //TODO: Support events
     //@TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public void setTaskStatus(TaskStatus newStatus, Task task) {
     	taskStatusManager.changeStatus(newStatus, task);
 
     	/*
     	task.setStatus(newStatus);
     	em.merge(task);
     	
     	UserTransaction transaction = ejbContext.getUserTransaction();
     	try {
     		transaction.begin();
         	task.setStatus(newStatus);
         	
         	em.merge(task);
         	
         	transaction.commit();
     	}catch (Exception e) {
     		try {
     	    	log.error("Transaction failed when updating stat. Try rolling back.", e);
     	    	transaction.rollback();
     	    	}
     	    	catch (Exception exp) {
     	    	log.error("Transaction failed roling back.", exp);
     	    	}
     	}
     	*/
     	
 
     	
     	
     	
     	
     	
     	
     	/*
       	// Save to database. Create manual transaction since the built in only works for 5 minutes.
     	UserTransaction transaction = ejbContext.getUserTransaction();
     	try {
     	transaction.begin();
     	
 //    	em.getTransaction().begin();
     	log.debug("Indicating task id '" + task.getTaskId() + "' as RUNNING...");
         task.setStatus(TaskStatus.RUNNING);
         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + em.getFlushMode());
         em.merge(task);
         System.out.println("!!!!WAAAAAAAAAAAAAA: " + em.contains(task));
         task.setStatus(TaskStatus.RUNNING);
         Session s = (Session)em.getDelegate();
         s.setFlushMode(org.hibernate.FlushMode.MANUAL);
         //em.flush();
         s.flush();
 //        em.getTransaction().commit();
         System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!: " + em.getFlushMode());
         log.debug("Merged and flushed task as RUNNING status.");
     	
     	
     	
     	transaction.commit();
     	}
     	catch (Exception e) {
     	try {
     	log.error("Transaction failed when updating stat. Try rolling back.", e);
     	transaction.rollback();
     	}
     	catch (Exception exp) {
     	log.error("Transaction failed roling back.", exp);
     	}
     	} 
     	*/
     	
     	
     	
     	
     	
     	
     	
     	
     	
     	
     	
     	/*
         try {
             EventDefinition ed = eventManager.findEventDefinitionByUniqueName(eventTaskStatusModificationName);
             task.setStatus(newStatus);
             
             Map<String,Object> properties =  new HashMap<String,Object>();
             properties.put("task", task);
             eventManager.createEventResponsesOfEventDefinition(ed, properties);
         } catch (NoResultFoundException nrfe) {
             logger.warn("Could not fire events for event unique name: '" + eventTaskStatusModificationName + "', the definition of this event does not exist in database, detailed message is: " + nrfe.getMessage());
         } catch (EventResponseException ex) {
             //TODO: Replace with throwing an exception, events integrity is very important!
             logger.warn("Could not fire events due to: " + ex);
         }
         */
     }
     
     
     
     //THIS NAME IS BAD, THIS IS NOT REALLY VIA QUEUE, IT'S THE OLD TASK EXECUTION THAT SHOULD DIE, INSTEAD, WE HAVE 'executeTaskViaTaskExecuter'
     public boolean executeTaskViaQueue(Task task) throws TaskExecutionException {
     	//currently only SPML tasks are supported
     	if (task instanceof SpmlTask) {
     		SpmlTask spmlTask = (SpmlTask)task;
     		
     		try {
 //    			indicateTaskAsRunning(spmlTask);
     			//System.out.println("!!!!!!!!!!!!!!!BB2: " + em.getFlushMode());
     	    	//try {
     	    		//System.out.println("!!!!!!!!!!!!!!!BB2: " + tm.getStatus());
     	    		//System.out.println("!!!!!!!!!!!!!!!BB2: " + tm.getTransaction().getStatus());
     	    	//}catch(SystemException e) {
     	    		//e.printStackTrace();
     	    	//}
     			resourceOperationsManager.performSpmlTask(spmlTask);
 //    			indicateTaskExecutionSuccess(spmlTask);
     			
     			/*
     			try {
     				transaction.commit();
     			}catch(HeuristicMixedException e) {
     				e.printStackTrace();
     			}catch (HeuristicRollbackException e) {
     				e.printStackTrace();
     			}catch(RollbackException e) {
     				e.printStackTrace();
     			}catch(SystemException e) {
     				e.printStackTrace();
     			}
     			*/
     			
     			
     			return true;
     		}
     		catch (OperationException e) {
     			/*
     			System.out.println("!!!!!!!!!!!!!!!B3: " + em.getFlushMode());
     	    	try {
     	    		System.out.println("!!!!!!!!!!!!!!!B3: " + tm.getStatus());
     	    		System.out.println("!!!!!!!!!!!!!!!B3: " + tm.getTransaction().getStatus());
     	    		
     	    		if (tm.getStatus() == 1) {
     	    			System.out.println("STARTING A NEW TRANSACTION !!!!!!!!!!!!!!");
         	    		transaction.begin();
         	    	}
     	    	}catch(SystemException eE) {
     	    		eE.printStackTrace();
     	    	}catch (Exception eee) {
     	    		eee.printStackTrace();
     	    	}
     	    	*/
     	    	
 //    			indicateTaskExecutionFailure(spmlTask, e.toString());
     			throw new TaskExecutionException(e);
 //    			return false;
     		}
     	}
     	
     	
     	
     	
     	//TODO: Replace all this crap with task executers
     	//CLEAN!
     	else if (task instanceof ResourceTask) {
     		try {
 //    			indicateTaskAsRunning(task);
     			ResourceTask resourceTask = (ResourceTask)task;
     			resourceOperationsManager.performResourceTask(resourceTask);
 //    			indicateTaskExecutionSuccess(resourceTask);
     			return true;
     		}
     		catch (OperationException e) {
 //    			indicateTaskExecutionFailure(task, e.toString());
     			throw new TaskExecutionException(e);
 //    			return false;
     		}
     	}
     	else if (task instanceof GenericTask) {
     		log.debug("Task id '" + task.getTaskId() + "', is a generic type task, executing task.");
 //    		indicateTaskAsRunning(task);
     		GenericTask genTask = (GenericTask)task;
     		try {
     			genTask.execute();
 //    			indicateTaskExecutionSuccess(genTask);
     			return true;
     		}catch(ExecutionException e) {
 //    			indicateTaskExecutionFailure(task, e.toString());
     			throw new TaskExecutionException(e);
 //    			return false;
     		}
     	}
     	/*
     	else if (task instanceof EventResponseTask) {
     		indicateTaskAsRunning(task);
     		EventResponseTask eventResponseTask = (EventResponseTask)task;
     		try {
     			eventManager.invokeEventResponseTask(eventResponseTask, null);
 //    			indicateTaskExecutionSuccess(eventResponseTask);
     			return true;
     		}catch (ScriptInvocationException e) {
 //    			indicateTaskExecutionFailure(task, e.toString());
     			throw e;
     			return false;
     		}
     	}
     	*/
     	else {
 //    		indicateTaskExecutionFailure(task, "Task type is not supported!");
     		throw new TaskExecutionException("Task type is not supported!");
 //    		return false;
     	}
     	
     	
     	
     	/*OLD
         } catch (ActionFailureException afe) {
                 //logger.warning("Action was failed, exiting with message: " + afe.getMessage());
                 //String shortMessage = "Failed to disable account name: " + account.getName() + ", on target: " +
                 //        account.getResource().getDisplayName(); String detailedMessage = shortMessage +
                 //        ", Action was failed with message: " + afe.getMessage();
             
             logger.warn("Failed to execute the action of task ID: " + task.getTaskId()
                     + "failed with message: " + afe.getMessage() + ", dumping previous task log: " + task.logsToString());
             
             //Add the error to the TaskLog
             task.addLog("WARNING","Failed to perform the action",afe.getMessage());
             
             
             //Add the message to the EventLog
             cum.addEventLog("Tasks","FAILURE","MEDIUM","Failed to execute the action of task ID: " + task.getTaskId(),"Dumping task log: " + task.logsToString());
             
             indicateTaskExecutionFailure(task);
             return false;
         } catch (ClassCastException cce) {
             logger.warn("Failed to execute the action of task ID: " + task.getTaskId()
                     + "failed with message: " + cce.getMessage() + ", dumping previois task log: " + task.logsToString());
             
             // Add the error to the TaskLog
             task.addLog("WARNING","Failed to perform the action due to ClassCastException",cce.getMessage());
             
             //Add the message to the EventLog
             cum.addEventLog("Tasks","FAILURE","MEDIUM","Failed to execute the action of task ID: " + task.getTaskId(),"Dumping task log: " + task.logsToString());
             
             indicateTaskExecutionFailure(task);
             return false;
         } catch (CompilationFailedException cfe) {
             logger.warn("Failed to execute the action of task ID: " + task.getTaskId()
                     + "failed with message: " + cfe.getMessage() + ", dumping previois task log: " + task.logsToString());
             
             // Add the error to the TaskLog
             task.addLog("WARNING","Failed to perform the action due to ClassCastException",cfe.getMessage());
             
             //Add the message to the EventLog
             cum.addEventLog("Tasks","FAILURE","MEDIUM","Failed to execute the action of task ID: " + task.getTaskId(),"Dumping task log: " + task.logsToString());
             
             indicateTaskExecutionFailure(task);
             return false;
         }
         */
     }
     
 
     //Needed by the remote performer, which has no access to em(em.find)
     public Task findTaskById(Long taskId) {
     	return (Task) em.createNamedQuery("task.findById").setParameter("taskId", taskId).getSingleResult();
     }
     
     public Long persistBulkTask(BulkTask bulkTask) {
         em.persist(bulkTask);
         em.flush();
         
         return bulkTask.getBulkTaskId();
     }
     
     
     public void reQueueTask(Task task) throws CannotRequeueTaskException {
         if (task.getStatus() != TaskStatus.FATAL_ERROR) {
             throw new CannotRequeueTaskException("Cannot re-queue task ID: " + task.getTaskId() + ", the task is still in queue (current status is: " + task.getStatus() + ")");
         }
         
         task.setFailureCounts(0);
         task.addLog("INFO","Re-queuing task!","Requested to re-queue the task!");
         //also merge
         setTaskStatus(TaskStatus.PENDING,task);
     }
 
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     //task scanner
     public void scanTasks() {
         Date currDate = new Date();
         int loadAmountTasksPerQuery = 5;
         
         //TODO: JBOSS: FIX THIS
         //int maxTasksToHandlePerScan = SysConf.getSysConf().getInt("tasks.task_scanner_max_tasks_to_fetch_per_scan");
         int maxTasksToHandlePerScan = 1;
         
         
         int currentResultRowNumberInDb = 0;
         
         Long amountOfTasksWaitingForExecutionInRepository = waitingTasksToExecuteAmount();
         
         
         List<Task> tasksReadyToQueue = new ArrayList<Task>();
         if (amountOfTasksWaitingForExecutionInRepository > 0) {
             log.trace("Amount of tasks waiting for Execution in Repository is: '" + amountOfTasksWaitingForExecutionInRepository + "'");
             Long availableIterations = amountOfTasksWaitingForExecutionInRepository / loadAmountTasksPerQuery;
             
             if ( (amountOfTasksWaitingForExecutionInRepository % loadAmountTasksPerQuery) != 0) {
                 availableIterations++;
             }
             
             log.trace("Calculated possible maximum tasks iteration per scan is '" + availableIterations + "' iterations ("+ amountOfTasksWaitingForExecutionInRepository + "/" + loadAmountTasksPerQuery + ")");
             
             Long currIteration = new Long(0);
             while ( (tasksReadyToQueue.size() < maxTasksToHandlePerScan) && (currIteration < availableIterations) ) {
                 currIteration++;
                 log.trace("Iteration number '" + currIteration + "', fetching '" + loadAmountTasksPerQuery + ", current row number '" + currentResultRowNumberInDb + "'");
                 Query tasksQuery = em.createNamedQuery("task.findAllTasksToQueue")
                         .setFirstResult(currentResultRowNumberInDb)
                         .setMaxResults(loadAmountTasksPerQuery)
                         .setParameter("currDate",currDate);
                 //Accomulate the current row in DB
                 currentResultRowNumberInDb+=loadAmountTasksPerQuery + 1;
                 
                 Collection<Task> tasksWithWaitingStatusPerQuery = tasksQuery.getResultList();
                 
                 //Fetch the tasks that are ready to be queued
                 tasksReadyToQueue.addAll(getTasksReadyToQueuePerScan(tasksWithWaitingStatusPerQuery,maxTasksToHandlePerScan));
                 
                 log.trace("Iteration number '" + currIteration + "' resulted '" + tasksReadyToQueue.size() + "' tasks ready to be executed, while the maximum allowed tasks per scan is '" + maxTasksToHandlePerScan + "'");
                 
                 //tasksReadyToQueueCounter += tasksToQueue.size();
             }
         } else {
             log.info("Task Scanner couldn't find any tasks to queue...");
         }
         
         
         if (tasksReadyToQueue.size() > 0) {
             //Lock the tasks first.
             //JB indicateTasksAsRunning(tasksReadyToQueue);
             
             
             //Queue tasks into the JMS queue so agents can handle them
             log.info("Sending -" + tasksReadyToQueue.size() + "- tasks to the task queue!");
             for (Task currTaskToQueue : tasksReadyToQueue) {
                 //First lock tasks
                 //currTaskToQueue.setLocked(true);
                 //updateTask(currTaskToQueue);
                 
             	
             	
             	
             	
             	//FIXME: REMOVED JMS FOR NOW, SOMETIMES QUEUE CRASHES, HAPPEND IN PARTNER, IL 
             	sendTaskToJmsQueue(currTaskToQueue);
             	
             	
             	
             	//em.clear();
             	//executeTaskViaQueue(currTaskToQueue);
             	//em.clear();
             }
         }
     }
     
     
     public void changeTaskScannerMode() throws OperationException {
         boolean isTasksScannerEnabled = SysConf.getSysConf().getBoolean("tasks.activate_tasks_processor_scanner");
         
         log.info("Requested to change Task Scanner Mode...");
         
         if (timerService.getTimers().size() > 0) {
             log.info("Current task Scanner mode is enabled, found '" + timerService.getTimers().size() + " timers..., clearing...");
             Iterator timersIt = timerService.getTimers().iterator();
             while (timersIt.hasNext()) {
                 Timer currTimer = (Timer)timersIt.next();
                 log.trace("Found timer with info: '" + currTimer.getInfo() + "', object: '" + currTimer + "'");
                 currTimer.cancel();
             }
         } else {
             log.info("Current task Scanner mode is disabled, found '" + timerService.getTimers().size() + " timers..., adding timer...");
             int interval = SysConf.getSysConf().getInt("tasks.task_scanner_interval_in_seconds");
             
             if (!isTasksScannerEnabled) {
             	if (!isTasksScannerEnabled) {
                     throw new OperationException("Cannot change task scanner mode since task scanner is disabled in system configuration!");
                 }
             } else {
             	createTimerScanner(interval, interval);
             }
         }
     }
     
     public boolean isTaskScannerActivate() {
         if (timerService.getTimers().size() > 0) {
             return true;
         } else {
             return false;
         }
     }
     
     public void createTimerScanner(long initialDuration, long intervalDuration) {
         //Calculating miliseconds from the specified parameters as seconds
         long msInitialDuration = initialDuration * 2000;
         long msIntervalDuration = intervalDuration * 2000;
         
         timerService.getTimers().clear();
         //Timer timer = timerService.createTimer(msInitialDuration,msIntervalDuration,
         //  "Created new timer with interval of: " + msIntervalDuration + " seconds.");
         sessionContext.getTimerService().createTimer(msInitialDuration, msInitialDuration, "task-scanner");
         
         //System.out.println("Created Task-Scanner timer over a session context object: " + sessionContext);
         log.info("Created a Task-Scanner timer with interval of '" + msInitialDuration + "' ms.");
     }
     
     
     /**
      * Queue the specified task for execution
      */
     public void sendTaskToJmsQueue(Task task) {
         boolean onlySendTaskIdInMessageBody = true;
         
         try {
         	Queue queue = null;
             QueueConnection connection = null;
             QueueSession session = null;
             MessageProducer messageProducer = null;
         	
             InitialContext ctx = new InitialContext();
             queue = (Queue) ctx.lookup("queue/velo/TasksDefaultQueue");
             QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
             connection = factory.createQueueConnection();
             session = connection.createQueueSession(false,QueueSession.AUTO_ACKNOWLEDGE);
             messageProducer = session.createProducer(queue);
             
             messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
             //messageProducer.setTimeToLive(120000);
         	
             
             //indicate the task in running mode before sending task in JMS queue
             //done by taskexecution method: indicateTaskAsRunning(task);
             if (onlySendTaskIdInMessageBody) {
                 BytesMessage bytesMsg = session.createBytesMessage();
                 bytesMsg.setLongProperty("taskId", task.getTaskId());
                 messageProducer.send(bytesMsg);
                 log.debug("Sent Task Message (ONLY ID!) to the 'TaskListQueue' queue!");
             } else {
                 ObjectMessage objMsg = session.createObjectMessage();
                 objMsg.setObject(task);
                 messageProducer.send(objMsg);
                 log.debug("Sent Task message (AS OBJECT!) to the 'TaskListQueue' queue!");
             }
             
             //Some tests
             //TextMessage msg = queueSession.createTextMessage("hello");
             //queueSender.send(mdbQueue, bm);
             
             
             
             //Close the producer/session/connection to the queue
             messageProducer.close();
             session.close();
             connection.close();
             
         } catch(Exception e) {
             //TODO PrintStackTrace?!?!?! never ever ! especially not at such critical places!
             e.printStackTrace();
         }
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     @Deprecated
     public TaskDefinition findTaskDefinition(String name) {
         try {
             return (TaskDefinition) em.createNamedQuery("taskDefinition.findByName").
                     setParameter("name", name).getSingleResult();
         } catch (NoResultException e) {
             throw new NoResultException("Couldnt find TaskDefinition for Task Definition Name: "
                     + name);
         } catch (Exception e) {
             throw new EJBException(e.getMessage());
         }
     }
     
     /*
     public boolean isTaskDefinitionExists(long taskDefinitionId) {
         if (em.find(TaskDefinition.class, taskDefinitionId) == null) {
             return false;
         }
         else {
             return true;
         }
     }
      **/
     
     
     @Deprecated
     public BulkTask findBulkTaskById(Long bulkTaskId) throws NoResultFoundException {
         try {
             return (BulkTask) em.createNamedQuery("bulkTask.findById").setHint("toplink.refresh", "true")
                     .setParameter("bulkTaskId", bulkTaskId).getSingleResult();
         } catch (NoResultException e) {
             throw new NoResultFoundException("Couldnt load BulkTask id: '" + bulkTaskId + ", no result was found for such an id.");
         }
     }
     
     
     @Deprecated
     public List<Task> loadTasksByIds(List<Long> taskIds) throws CollectionLoadingException {
         List<Task> tasks = new ArrayList<Task>();
         
         for (Long currTaskId : taskIds) {
             Task task = findTaskById(currTaskId);
             tasks.add(task);
         }
         
         return tasks;
     }
     
     @Deprecated
     public List<BulkTask> loadBulkTasksByIds(List<Long> bulkTasksIds) throws CollectionLoadingException {
         List<BulkTask> bulkTasks = new ArrayList<BulkTask>();
         
         try {
             for (Long currBulkTaskId : bulkTasksIds) {
                 BulkTask bt = findBulkTaskById(currBulkTaskId);
                 bulkTasks.add(bt);
             }
         } catch (NoResultFoundException nrfe) {
             throw new CollectionLoadingException("Couldnt load a collection of bulkTasks due to: " + nrfe);
         }
         
         return bulkTasks;
     }
     
     
     
     
     @Deprecated
     public void persistBulkTasks(Collection<BulkTask> bulkTaskList) {
         for (BulkTask currBT : bulkTaskList) {
             persistBulkTask(currBT);
         }
     }
     
     @Deprecated
     public void deleteTask(Task task) {
         log.debug("Removing TASK ID: " + task.getTaskId());
         
         
         //Serialized action taks too much place, empty it
         task.setSerializedTask(null);
         task.setScript(null);
         task.setDeleted(true);
         
         // Merge the entity
         Task mergedEntity = em.merge(task);
         //Instead of really deleting the entity, we just flag it as deleted since this history of tasks is needed
         //for task dependency (tasks need to know the status of their task dependency)
         //em.remove(mergedEntity);
         
         //Very scary, sometimes it gets here, but the task is not getting updated by em.merge...
         //maybe flush will help? :-/
         em.flush();
     }
     
     @Deprecated
     public void removeTasks(Collection<Task> tasks) {
         for (Task currTask : tasks) {
             em.remove(currTask);
         }
     }
     
     @Deprecated
     public void deleteBulkTask(BulkTask bulkTask) {
         log.trace("Removing BULK TASK ID: " + bulkTask.getBulkTaskId());
         
         // Merge the entity
         BulkTask mergedEntity = em.merge(bulkTask);
         em.remove(mergedEntity);
     }
     
     
     @Deprecated
     public void updateBulkTask(BulkTask bulkTask) {
         em.merge(bulkTask);
     }
     
 
     
     
     /*
     public void sendTasksToJmsQueue(Map<String,Task> taskMap) {
         try {
             QueueConnection queueCon = queueCF.createQueueConnection();
             QueueSession queueSession = queueCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
             
             QueueSender queueSender = queueSession.createSender(null);
             
             //TextMessage msg = queueSession.createTextMessage("hello");
             MapMessage mapMsg = queueSession.createMapMessage();
             
             log.fine("Retrieved bulk tasks to send to JMS as MAP with tasks amount -" + taskMap.size() + "-");
             for (Task currTask : taskMap.values()) {
                 mapMsg.setObject(currTask.getTaskId().toString(), currTask);
             }
             
             queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
             queueSender.setTimeToLive(20000);
             queueSender.send(mapMsg);
             //queueSender.send(mdbQueue, bm);
             
             log.fine("Sent Task message object to the 'TaskListQueue' queue!");
             
             //Close the sender/session/connection to the queue
             queueSender.close();
             queueSession.close();
             queueCon.close();
             
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
     */
     
     
     
     
     
     
     @Deprecated
     public void createTimerScanner() throws OperationException {
         boolean isTasksScannerEnabled = SysConf.getSysConf().getBoolean("tasks.activate_tasks_processor_scanner");
         
         if (isTasksScannerEnabled) {
             int interval = SysConf.getSysConf().getInt("tasks.task_scanner_interval_in_seconds");
             log.info("Starting Tasks-Scanner, Firing the task scanner each '" + interval + "' seconds");
             createTimerScanner(interval, interval);
         } else {
             throw new OperationException("Cannot start Task Scanner since it is disabled in system configuration!");
         }
     }
     
     
     
     
     
     /*
     public List<Message> getMessagesInQueue() throws JMSException {
         
         List<Message> messageListInQueue = new ArrayList<Message>();
         
         Connection connection = null;
         try {
             connection = queueCF.createConnection();
             Session session = connection.createSession(
                     false,
                     Session.AUTO_ACKNOWLEDGE);
             
             QueueBrowser browser = session.createBrowser(mdbQueue);
             Enumeration msgs = browser.getEnumeration();
             
             if (!msgs.hasMoreElements()) {
                 
             } else {
                 while (msgs.hasMoreElements()) {
                     Message tempMsg = (Message) msgs.nextElement();
                     messageListInQueue.add(tempMsg);
                 }
             }
             
             return messageListInQueue;
             
             
         } catch (JMSException e) {
             throw (e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (JMSException e) {
                 }
             }
         }
     }
     */
     
     
     //TODO:  Should move to an external bean - will be used by other task agents
     @Timeout
     @Deprecated
     @TransactionTimeout(value=9600)
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void scanTasksForQueuing(Timer timer) {
         log.info("Task scanner timed out, remaining time until next execution is: '" + timer.getTimeRemaining() + "' ms.");
         
         scanTasks();
     }
     
     /*
     public synchronized void scanTasksForQueuing(Timer timer) {
         log.info("Task scanner executed over time: '" + timer + "', remaining time until next execution: " + timer.getTimeRemaining());
         Query tasksQuery = em.createNamedQuery("task.findAllTasksToQueue")
             //.setMaxResults(maxTasksToHandlePerScan)
             .setParameter("currDate",new Date());
         Collection<Task> tasksWithWaitingStatus = tasksQuery.getResultList();
      
         //Fetch the tasks that are ready to be queued
         Collection<Task> tasksToQueue = getTasksReadyToQueue(tasksWithWaitingStatus);
      
         //Lock the tasks first.
         lockTasks(tasksToQueue);
      
         //Queue tasks into the JMS queue so agents can handle them
         if (tasksToQueue.size() > 0) {
             log.info("Sending -" + tasksToQueue.size() + "- tasks to the task queue!");
             for (Task currTaskToQueue : tasksToQueue) {
                 //First lock tasks
                 //currTaskToQueue.setLocked(true);
                 //updateTask(currTaskToQueue);
                 sendTaskToJmsQueue(currTaskToQueue);
             }
      
         } else {
             log.info("Task Scanner couldnt find any tasks to queue...");
         }
     }
      */
     
     @Deprecated
     public void sendTaskToJms(Task task) {
         /*JBindicateTaskAsRunning(task);
         updateTask(task);
         em.flush();
         sendTaskToJmsQueue(task);
         */
     }
     
     
     
     
     //Private, methods that are not exposed in EJBs services
     @Deprecated
     public Collection<Task> getTasksReadyToQueuePerScan(Collection<Task> tasks, int maxTasksToHandlePerScan) {
         
         //27-may-07:(ASAF): This code was used when -ALL WAITING TASKS- were fetched in one query
         //Now fetching specific batch amount of tasks per query.
         /*
         int maxTasksToHandlePerScan = SysConf.getSysConf().getInt("tasks.task_scanner_max_tasks_to_fetch_per_scan");
         log.fine("--- Task Scanner pooled, scanning whether there are tasks to send to QUEUE or not ---");
         log.fine("Maximum tasks to handle per scan is set to: '" + maxTasksToHandlePerScan + "'");
          
         int counter=0;
          
         Set<Task> tasksToQueue = new HashSet<Task>();
          
         for (Task currTask : tasks) {
             if (counter >= maxTasksToHandlePerScan) {
                 break;
             }
          
             if (isTaskReadyToBeExecuted(currTask)) {
                 tasksToQueue.add(currTask);
                 counter++;
             }
         }
          
         log.info("Reading to queue '" + tasks.size() + "' tasks.");
          */
         
         Set<Task> tasksToQueue = new HashSet<Task>();
         for (Task currTask : tasks) {
             if (tasksToQueue.size() >= maxTasksToHandlePerScan) {
                 break;
             }
             
             if (isTaskReadyToBeExecuted(currTask)) {
                 tasksToQueue.add(currTask);
             }
         }
         
         
         return tasksToQueue;
     }
     
     @Deprecated
     public boolean isTaskReadyToBeExecuted(Task task) {
         boolean isExecuteTask = true;
         
         if (!task.isAllDependentTasksCompletedSuccessfully()) {
             isExecuteTask = false;
             log.debug("Task is not ready to be executed, task ID: '" + task.getTaskId() +"' has a dependency failure.");
         }
         
         if (!task.isApproved()) {
             isExecuteTask = false;
             log.debug("Task is not ready to be executed, task ID: '" + task.getTaskId() + "' is not approved.");
         }
         
         return isExecuteTask;
     }
     
     
     
     
     
     //Helper classes
     @Deprecated
     private Long waitingTasksToExecuteAmount() {
         Date currDate = new Date();
         Query q = em.createNamedQuery("task.waitingTasksToExecuteAmount").setParameter("currDate",currDate);
         Long num = (Long) q.getSingleResult();
         
         return num;
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     private void commitTransaction() {
     	
     }
 }
