 package net.mindengine.oculus.grid.server;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import net.mindengine.jeremy.bin.RemoteFile;
 import net.mindengine.jeremy.registry.Lookup;
 import net.mindengine.jeremy.registry.Registry;
 import net.mindengine.oculus.grid.GridUtils;
 import net.mindengine.oculus.grid.console.ConsoleCommandScanner;
 import net.mindengine.oculus.grid.domain.agent.AgentId;
 import net.mindengine.oculus.grid.domain.agent.AgentInformation;
 import net.mindengine.oculus.grid.domain.agent.AgentStatus;
 import net.mindengine.oculus.grid.domain.task.DefaultTask;
 import net.mindengine.oculus.grid.domain.task.MultiTask;
 import net.mindengine.oculus.grid.domain.task.TaskInformation;
 import net.mindengine.oculus.grid.domain.task.TaskStatus;
 import net.mindengine.oculus.grid.domain.task.TaskUser;
 import net.mindengine.oculus.grid.service.AgentServerRemoteInterface;
 import net.mindengine.oculus.grid.service.ClientServerRemoteInterface;
 import net.mindengine.oculus.grid.service.ServerAgentRemoteInterface;
 import net.mindengine.oculus.grid.service.exceptions.IncorrectTaskException;
 
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
 
     private TaskContainer taskContainer = new TaskContainer();
     private AgentContainer agentContainer = new AgentContainer();
     private AgentHandler agentHandler = new AgentHandler();
     private TaskHandler taskHandler = new TaskHandler(this);
     private CompletedTaskCleaner completedTaskCleaner = new CompletedTaskCleaner(this);
     private ServerConsoleHandler consoleHandler = new ServerConsoleHandler(this);
 
     /**
      * The amount of time in minutes in which the completed tasks will be
      * removed from TRMServer. Configured in server.properties file
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
         task.getTask().getTaskStatus().setInterrupted(true);
         if (task != null) {
             logger.info("Stoping task " + task.getId());
 
             if (TaskStatus.WAITING.equals(task.getTask().getTaskStatus().getStatus())) {
                 taskContainer.getQueuedTasks().remove(task);
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
     public Collection<TaskInformation> getTasksList() {
         taskContainer.getTaskLock().lock();
         Collection<TaskInformation> tasksList = null;
         try {
             Collection<TaskWrapper> tasks = this.taskContainer.getTasks().values();
             tasksList = new LinkedList<TaskInformation>();
             for (TaskWrapper taskWrapper : tasks) {
                
                 tasksList.add(taskWrapper.getTaskInformation());
             }
         } catch (Throwable e) {
             throw runtimeException(e);
         } finally {
             taskContainer.getTaskLock().unlock();
         }
         return tasksList;
     }
 
     private static RuntimeException runtimeException(Throwable exception) {
         if (exception instanceof RuntimeException) {
             throw (RuntimeException) exception;
         } else
             throw new RuntimeException(exception);
     }
 
     @Override
     public Collection<TaskInformation> getAllUserTasks(Long userId) {
         taskContainer.getTaskLock().lock();
         Collection<TaskInformation> tasksList = null;
         try {
             Collection<TaskWrapper> tasks = taskContainer.getTasks().values();
             tasksList = new LinkedList<TaskInformation>();
             for (TaskWrapper taskWrapper : tasks) {
                 /*
                  * Only parent tasks should be returned to the user as they
                  * contain all their child tasks
                  */
                 if (taskWrapper.getTask().parent() == null) {
                     TaskUser taskUser = taskWrapper.getTask().getTaskUser();
                     if (taskUser == null) {
                         throw new NullPointerException("The taskUser is null");
 
                     }
                     if (userId.equals(taskUser.getId())) {
                         if (taskWrapper.getTask() instanceof MultiTask) {
                             MultiTask multiTask = (MultiTask) taskWrapper.getTask();
                             multiTask.updateTaskStatus();
                         }
                         tasksList.add(taskWrapper.getTaskInformation());
                     }
                 }
             }
         } catch (Throwable e) {
             throw runtimeException(e);
         } finally {
             taskContainer.getTaskLock().unlock();
         }
         return tasksList;
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
     public Collection<AgentStatus> getAgents() {
         agentContainer.getAgentLock().lock();
         Collection<AgentStatus> agentList = null;
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
         return agentList;
     }
 
     @Override
     public AgentId registerAgent(AgentInformation agentInformation, AgentId previousAgentId) throws Exception {
         /*
          * In case if agents needs to be reconnected it will send its
          * previous id and token and TRMServer will remove that instance of
          * agent and will create a new one.
          */
         if (previousAgentId != null) {
             agentContainer.removeAgent(previousAgentId);
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
                 MultiTask parentTask = taskWrapper.getTask().parent();
                 if (parentTask != null) {
                     parentTask.updateTaskStatus();
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
 
         registry.addObject(serverName, this);
         registry.setPort(port);
 
         logger.info("Starting server");
         handle();
         registry.start();
     }
     
     public void stopServer() throws Exception {
         registry.stop();
     }
     
     public static void main(String[] args) throws Exception {
         Server server = new Server();
         
         Properties properties = new Properties();
         properties.load(new FileReader(new File(GridUtils.getMandatoryResourceFile(Server.class, "/server.properties"))));
         Integer port = Integer.parseInt(properties.getProperty(ServerProperties.SERVER_PORT));
         String strStoreCompletedTasksTime = properties.getProperty(ServerProperties.SERVER_STORE_COMPLETED_TASKS_TIME);
         if (strStoreCompletedTasksTime == null || strStoreCompletedTasksTime.isEmpty()) {
             server.setStoreCompletedTasksTime(null);
         } else {
             server.setStoreCompletedTasksTime(Long.parseLong(strStoreCompletedTasksTime));
         }
         String serverName = properties.getProperty(ServerProperties.SERVER_NAME);
         
         server.startServer(port, serverName);
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
     public void uploadProject(String projectPath, String version, RemoteFile file) throws Exception {
         List<AgentWrapper> agentList = new LinkedList<AgentWrapper>();
         /*
          * Collecting agent to another list in order to avoid thread problems
          */
         agentContainer.getAgentLock().lock();
         for (AgentWrapper agetWrapper : agentContainer.getAgents().values()) {
             agentList.add(agetWrapper);
         }
         agentContainer.getAgentLock().unlock();
 
         /*
          * Uploading the project to each agent
          */
         for (AgentWrapper agentWrapper : agentList) {
             try {
                 agentWrapper.getAgentRemoteInterface().uploadProject(projectPath, version, file);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public String checkConnection() throws Exception {
         return "OK";
     }
 
     public void setStoreCompletedTasksTime(Long storeCompletedTasksTime) {
         this.storeCompletedTasksTime = storeCompletedTasksTime;
     }
 
     public Long getStoreCompletedTasksTime() {
         return storeCompletedTasksTime;
     }
 
     // TODO implement versioning for projects. So when the agent has to execute
     // task for project which it doesn't have - it download all needed data
     /*
      * As users could update same versions of project - we need to implement
      * some kind of custom generated keys which will identify the projects. If
      * agent has a key other than a server - it will download whole project from
      * server and only after will start executing task
      */
 }
