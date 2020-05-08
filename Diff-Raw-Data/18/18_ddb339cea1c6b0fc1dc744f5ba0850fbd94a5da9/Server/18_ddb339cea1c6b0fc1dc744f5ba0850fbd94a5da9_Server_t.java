 /*******************************************************************************
 * 2012 Ivan Shubin http://mindengine.net
 * 
 * This file is part of MindEngine.net Oculus Grid.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Oculus Grid.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
 package net.mindengine.oculus.grid.server;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import net.mindengine.jeremy.registry.Lookup;
 import net.mindengine.jeremy.registry.Registry;
 import net.mindengine.oculus.grid.GridUtils;
 import net.mindengine.oculus.grid.console.ConsoleCommandScanner;
 import net.mindengine.oculus.grid.domain.agent.AgentId;
 import net.mindengine.oculus.grid.domain.agent.AgentInformation;
 import net.mindengine.oculus.grid.domain.agent.AgentStatus;
 import net.mindengine.oculus.grid.domain.task.DefaultTask;
 import net.mindengine.oculus.grid.domain.task.MultiTask;
 import net.mindengine.oculus.grid.domain.task.SuiteTask;
 import net.mindengine.oculus.grid.domain.task.TaskInformation;
 import net.mindengine.oculus.grid.domain.task.TaskStatus;
 import net.mindengine.oculus.grid.domain.task.TaskUser;
 import net.mindengine.oculus.grid.service.AgentServerRemoteInterface;
 import net.mindengine.oculus.grid.service.ClientServerRemoteInterface;
 import net.mindengine.oculus.grid.service.ServerAgentRemoteInterface;
 import net.mindengine.oculus.grid.service.exceptions.AgentConnectionException;
 import net.mindengine.oculus.grid.service.exceptions.IncorrectTaskException;
 import net.mindengine.oculus.grid.storage.Storage;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Test Run Manager Server is an RMI remote object which manages task and
  * automation suites between agents
  * 
  * @author Ivan Shubin
  * 
  */
 public class Server implements ClientServerRemoteInterface, AgentServerRemoteInterface {
 
 
     private Log logger = LogFactory.getLog(getClass());
     private Storage storage;
     private TaskContainer taskContainer = new TaskContainer();
     private AgentContainer agentContainer = new AgentContainer();
     private AgentHandler agentHandler = new AgentHandler();
     private TaskHandler taskHandler = new TaskHandler(this);
     private CompletedTaskCleaner completedTaskCleaner = new CompletedTaskCleaner(this);
     private ServerConsoleHandler consoleHandler = new ServerConsoleHandler(this);
 
     /**
      * The amount of time in minutes in which the completed tasks will be
      * removed from Grid. Configured in server.properties file
      */
     private Long storeCompletedTasksTime = 300000L;
 
     private Registry registry = GridUtils.createDefaultRegistry();
     
     public Server() {
     }
 
     /**
      * Handles the console reading and executes the console command
      * 
      * 
      * @throws Exception
      */
     private void handle() throws Exception {
         agentHandler.setAgentContainer(agentContainer);
         agentHandler.setTaskContainer(taskContainer);
         agentHandler.start();
 
         taskHandler.setAgentContainer(agentContainer);
         taskHandler.setTaskContainer(taskContainer);
         taskHandler.start();
 
         if (storeCompletedTasksTime != null) {
             completedTaskCleaner.setTaskContainer(taskContainer);
             completedTaskCleaner.start();
         }
 
         ConsoleCommandScanner commandScanner = new ConsoleCommandScanner();
         commandScanner.setHandler(consoleHandler);
         commandScanner.start();
     }
 
     @Override
     public TaskStatus getTaskStatus(Long taskId) throws Exception {
         TaskWrapper taskWrapper = taskContainer.getTask(taskId);
        if ( taskWrapper == null ) {
            throw new IllegalArgumentException("Task with id " + taskId + " does not exist");
        }
 
         if (taskWrapper.getTask() instanceof MultiTask) {
             taskWrapper.taskLock.lock();
             try {
                 ((MultiTask) taskWrapper.getTask()).updateTaskStatus();
             } catch (Exception e) {
 
                 logger.error(e);
                 throw e;
             } finally {
                 taskWrapper.taskLock.unlock();
             }
         }
         if (taskWrapper != null) {
             TaskStatus taskStatus = taskWrapper.getTask().getTaskStatus();
             taskStatus.setTaskName(taskWrapper.getTask().getName());
             taskStatus.setTaskId(taskWrapper.getId());
             return taskStatus;
         }
         return null;
     }
 
     @Override
     public Long runTask(DefaultTask task) throws Exception {
         logger.info("Recieved new task");
         if(task==null || task.getSuiteTasks()==null || task.getSuiteTasks().size()==0) {
             throw new IncorrectTaskException();
         }
         
         for(SuiteTask suiteTask : task.getSuiteTasks()) {
             if(suiteTask.getProjectName()==null || suiteTask.getProjectName().isEmpty()){
                 throw new IncorrectTaskException("Project name is not specified");
             }
             if(suiteTask.getProjectVersion()==null || suiteTask.getProjectVersion().isEmpty()){
                 throw new IncorrectTaskException("Project version is not specified");
             }
         }
         
         task.initTask();
         
         TaskWrapper taskWrapper = new TaskWrapper();
         taskWrapper.setTask(task.convertToMultiTask());
         
         return taskContainer.registerNewTask(taskWrapper);
     }
 
     /**
      * Stops all tasks within specified task. Moves all queued tasks to
      * completed list.
      * 
      * @param task
      * @throws Exception
      */
     private void stopTask(TaskWrapper task) throws Exception {
        if ( task == null ) { 
            throw new IllegalArgumentException("Task does not exist");
        }
         task.getTask().getTaskStatus().setInterrupted(true);
         if (task != null) {
             logger.info("Stoping task " + task.getId());
 
             if (TaskStatus.WAITING.equals(task.getTask().getTaskStatus().getStatus())) {
                 taskContainer.moveTaskToCompleted(task);
             }
 
             if (task.getTask() instanceof MultiTask) {
                 for (TaskWrapper childTask : task.getChildren()) {
                     stopTask(childTask);
                 }
             } else {
                 AgentWrapper agent = task.getAssignedAgent();
                 if (agent != null) {
                     agent.getAgentRemoteInterface().stopCurrentTask();
                 }
             }
         }
     }
 
     @Override
     public void stopTask(Long taskId) throws Exception {
         TaskWrapper task = taskContainer.getTask(taskId);
         stopTask(task);
     }
 
     @Override
     public TaskInformation[] getTasks(Long parentTaskId) {
         taskContainer.getTaskLock().lock();
         ArrayList<TaskInformation> tasksList = null;
         try {
             Collection<TaskWrapper> tasks = this.taskContainer.getTasks().values();
             tasksList = new ArrayList<TaskInformation>();
             for (TaskWrapper taskWrapper : tasks) {
                 
                 if(parentTaskId == null && (taskWrapper.getParent()==null)) {
                     tasksList.add(taskWrapper.getTaskInformation());
                 }
                 else if(taskWrapper.getParent()!=null && taskWrapper.getParent().getId().equals(parentTaskId)) {
                     tasksList.add(taskWrapper.getTaskInformation());
                 }
                 
             }
         } catch (Throwable e) {
             throw runtimeException(e);
         } finally {
             taskContainer.getTaskLock().unlock();
         }
         return tasksList.toArray(new TaskInformation[]{});
     }
 
     private static RuntimeException runtimeException(Throwable exception) {
         if (exception instanceof RuntimeException) {
             throw (RuntimeException) exception;
         } else
             throw new RuntimeException(exception);
     }
 
     @Override
     public TaskInformation[] getAllUserTasks(Long userId) {
         taskContainer.getTaskLock().lock();
         ArrayList<TaskInformation> tasksList = null;
         try {
             Collection<TaskWrapper> tasks = taskContainer.getTasks().values();
             tasksList = new ArrayList<TaskInformation>();
             for (TaskWrapper taskWrapper : tasks) {
                 /*
                  * Only parent tasks should be returned to the user as they
                  * contain all their child tasks
                  */
                 if (taskWrapper.getParent() == null) {
                     TaskUser taskUser = taskWrapper.getTask().getTaskUser();
                     if (taskUser!=null && userId.equals(taskUser.getId())) {
                         taskWrapper.getTask().updateTaskStatus();
                         tasksList.add(taskWrapper.getTaskInformation());
                     }
                 }
             }
         } catch (Throwable e) {
             throw runtimeException(e);
         } finally {
             taskContainer.getTaskLock().unlock();
         }
         return tasksList.toArray(new TaskInformation[]{});
     }
 
     @Override
     public TaskInformation getTask(Long taskId) {
         taskContainer.getTaskLock().lock();
         TaskInformation taskInformation = null;
         try {
             TaskWrapper taskWrapper = taskContainer.getTask(taskId);
             if (taskWrapper != null) {
                 taskInformation = taskWrapper.getTaskInformation();
             }
         } catch (Exception e) {
             throw runtimeException(e);
         } finally {
             taskContainer.getTaskLock().unlock();
         }
 
         return taskInformation;
     }
 
     @Override
     public void removeCompletedTask(Long taskId) {
         taskContainer.removeCompletedTask(taskId);
     }
 
     @Override
     public AgentStatus[] getAgents() {
         agentContainer.getAgentLock().lock();
         ArrayList<AgentStatus> agentList = null;
         try {
             Collection<AgentWrapper> agents = agentContainer.getAgents().values();
             agentList = new ArrayList<AgentStatus>();
             for (AgentWrapper aw : agents) {
                 AgentStatus as = new AgentStatus();
                 as.setAgentInformation(aw.getAgentInformation());
                 as.setState(aw.getStatus());
                 agentList.add(as);
             }
 
         } catch (Exception e) {
             throw runtimeException(e);
         } finally {
             agentContainer.getAgentLock().unlock();
         }
         return agentList.toArray(new AgentStatus[]{});
     }
 
     @Override
     public AgentId registerAgent(AgentInformation agentInformation, AgentId previousAgentId) throws Exception {
         /*
          * In case if agents needs to be reconnected it will send its
          * previous id and token and Grid server will remove that instance of
          * agent and will create a new one.
          */
         if (previousAgentId != null) {
             agentContainer.removeAgent(previousAgentId);
         }
         
         if(agentInformation.getName() == null || agentInformation.getName().trim().isEmpty()) {
             throw new AgentConnectionException("Agent with empty name is not allowed in Grid");
         }
         
         /*
          * Checking that the agent name is unique.
          */
         if(agentContainer.containsAgentWithName(agentInformation.getName())){
             throw new AgentConnectionException("Agent with such name ('" + agentInformation.getName() + "') is already registered in Grid");
         }
         
 
         Lookup lookup = GridUtils.createDefaultLookup();
 
         lookup.setUrl("http://"+agentInformation.getHost() + ":" + agentInformation.getPort());
         ServerAgentRemoteInterface agentRemoteInterface = lookup.getRemoteObject(agentInformation.getRemoteName(), ServerAgentRemoteInterface.class);
 
         AgentId agentId = agentContainer.registerAgent(agentInformation, agentRemoteInterface);
         logger.info("Agent " + agentInformation.getName() + " was registered");
         
         return agentId;
     }
 
     @Override
     public void updateTaskStatus(Long taskId, TaskStatus taskStatus) throws Exception {
         TaskWrapper taskWrapper = taskContainer.getTask(taskId);
         if (taskWrapper != null) {
             taskWrapper.taskLock.lock();
             try {
 
                 taskStatus.setAssignedAgent(taskWrapper.getAssignedAgent().getAgentInformation());
                 taskWrapper.getTask().setTaskStatus(taskStatus);
 
                 if (taskStatus.getStatus().equals(TaskStatus.COMPLETED)) {
                     AgentWrapper agent = taskWrapper.getAssignedAgent();
                     agentContainer.freeAgent(agent.getAgentId().getId());
                     taskContainer.moveTaskToCompleted(taskWrapper);
                     logger.info("Task " + taskId + " is completed");
                 }
                 /*
                  * Updating the status of parent task if it exists
                  */
                 TaskWrapper parentTaskWrapper = taskWrapper.getParent();
                 if (parentTaskWrapper != null) {
                     ((MultiTask)parentTaskWrapper.getTask()).updateTaskStatus();
                 }
             } catch (Exception e) {
                 throw e;
             } finally {
                 taskWrapper.taskLock.unlock();
             }
         } else
             logger.info("Couldn't find task with id: " + taskId);
     }
 
     
     public void startServer(Integer port, String serverName) throws Exception {
         /*
          * Loading properties file
          */
         logger.info("Creating server on port: " + port);
         
         
         
         if (serverName == null || serverName.isEmpty()) {
             throw new Exception("Name of server is not specified");
         }
         if(storage==null) {
             throw new IllegalArgumentException("Storage is not specified");
         }
 
         registry.addObject(serverName, this);
         registry.setPort(port);
 
         logger.info("Starting server");
         handle();
         registry.start();
     }
     
     public void stopServer() throws Exception {
         registry.stop();
     }
     
     @Override
     public AgentInformation getAgent(Long id) {
         logger.info("Searching for agent: " + id);
         agentContainer.getAgentLock().lock();
         AgentWrapper agentWrapper = agentContainer.getAgents().get(id);
         agentContainer.getAgentLock().unlock();
 
         if (agentWrapper != null) {
             return agentWrapper.getAgentInformation();
         }
 
         return null;
     }
 
     @Override
     public void uploadProject(String projectPath, String version, File file, String userName) throws Exception {
         byte[] bytes = FileUtils.readFileToByteArray(file);
         storage.putProjectZip(projectPath, version, bytes, userName, null);
     }
 
     @Override
     public String checkConnection() throws Exception {
         return "OK";
     }
     
     @Override
     public String getProjectControlCode(String projectName, String version) throws Exception {
         return getStorage().readProjectControlKey(projectName, version);
     }
     
     @Override
     public File downloadProject(String projectName, String projectVersion) throws Exception {
         return storage.downloadProjectFromStorage(projectName, projectVersion);
     }
     
     public void quit() {
         agentHandler.setEnabled(false);
         System.exit(0);
     }
 
     protected TaskContainer getTaskContainer() {
         return taskContainer;
     }
 
     protected AgentContainer getAgentContainer() {
         return agentContainer;
     }
 
     
 
     public void setStoreCompletedTasksTime(Long storeCompletedTasksTime) {
         this.storeCompletedTasksTime = storeCompletedTasksTime;
     }
 
     public Long getStoreCompletedTasksTime() {
         return storeCompletedTasksTime;
     }
 
     public Storage getStorage() {
         return storage;
     }
 
     public void setStorage(Storage storage) {
         this.storage = storage;
     }
 }
