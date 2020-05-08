 /*******************************************************************************
  * 2011 Ivan Shubin http://mindengine.net
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
  * along with Oculus Experior.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package net.mindengine.oculus.grid.agent;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Properties;
 import java.util.concurrent.TimeoutException;
 
 import net.mindengine.jeremy.registry.Lookup;
 import net.mindengine.jeremy.registry.Registry;
 import net.mindengine.jeremy.starter.RegistryStarter;
 import net.mindengine.oculus.grid.GridProperties;
 import net.mindengine.oculus.grid.GridUtils;
 import net.mindengine.oculus.grid.agent.taskrunner.TaskRunner;
 import net.mindengine.oculus.grid.domain.agent.AgentId;
 import net.mindengine.oculus.grid.domain.agent.AgentInformation;
 import net.mindengine.oculus.grid.domain.agent.AgentStatus;
 import net.mindengine.oculus.grid.domain.task.SuiteTask;
 import net.mindengine.oculus.grid.domain.task.Task;
 import net.mindengine.oculus.grid.domain.task.TaskStatus;
 import net.mindengine.oculus.grid.domain.task.TestStatus;
 import net.mindengine.oculus.grid.service.AgentServerRemoteInterface;
 import net.mindengine.oculus.grid.service.ServerAgentRemoteInterface;
 import net.mindengine.oculus.grid.service.exceptions.IncorrectTaskException;
 import net.mindengine.oculus.grid.storage.DefaultAgentStorage;
 import net.mindengine.oculus.grid.storage.Storage;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Test Run Manager Agent.<br>
  * Used for running tasks and automation suites.<br>
  * Manages the RMI connection with the server. All the RMI configuration is
  * defined in "grid.agent.properties" file
  * 
  * @author Ivan Shubin
  * 
  */
 public class Agent implements ServerAgentRemoteInterface, AgentTestRunnerListener {
 	
 	private Log logger = LogFactory.getLog(getClass());
 
 	private AgentInformation agentInformation = new AgentInformation();
 	private AgentServerRemoteInterface server;
 	
 	private AgentConnectionChecker agentConnectionChecker = new AgentConnectionChecker();
 	private Task task;
 	private TaskStatus taskStatus;
 	private AgentId agentId = null;
 	
 	private String serverName;
 	private String serverHost;
 	private Integer serverPort;
 	
 	private String agentRemoteName;
     private String agentHost;
     private String agentName;
     private String agentDescription;
     private Integer agentPort;
     private Integer agentReconnectionTimeout = 5;
     
     private String agentStoragePath;
     private String agentOculusGridLibrary;
     private String AgentOculusRunner;
     
     private Storage storage;
     private Registry registry = GridUtils.createDefaultRegistry();
     
     //Flag that specifies should agent stop running or not
     private volatile boolean shouldRun = true;
     
 	/**
 	 * Flag which is used by the oculus-runner in order to check if it should proceed running all next tests
 	 */
 	private volatile Boolean shouldCurrentTaskProceed = true;
 	
 	/**
 	 * Abstract task runner which will be instantiated with each new task
 	 */
 	private TaskRunner taskRunner;
 	private Lookup lookup;
 
 	public Agent() {
 	}
 
 	public void startConnection() throws Exception {
 		// Detecting the machines name
 	    if(agentHost==null || agentHost.trim().isEmpty()) {
 	        InetAddress addr = InetAddress.getLocalHost();
 	        agentHost = addr.getHostName();
 	    }
 	    
 	    getAgentInformation().setHost(agentHost);
 		getAgentInformation().setName(agentName);
 		getAgentInformation().setRemoteName(agentRemoteName);
 		getAgentInformation().setDescription(agentDescription);
 		getAgentInformation().setPort(agentPort);
 
 		logger.info("Starting agent: " + getAgentInformation());
 
 		//Sending also the previous agentId in case if there was a reconnection
 		AgentId newAgentId = server.registerAgent(getAgentInformation(), getAgentId());
 		this.setAgentId(newAgentId);
 		logger.info("Registered on server with id = " + getAgentId().getId()+" and token = "+getAgentId().getToken());
 	}
 
 	@Override
 	public AgentStatus getAgentStatus() throws Exception {
 		AgentStatus agentStatus = new AgentStatus();
 		agentStatus.setAgentInformation(getAgentInformation());
 
 		if (task != null) {
 			agentStatus.setState(AgentStatus.BUSY);
 		}
 		else
 			agentStatus.setState(AgentStatus.FREE);
 
 		return agentStatus;
 	}
 
 	@Override
 	public void killAgent() {
 		System.exit(0);
 	}
 	
 	protected boolean synchronizeProjectForTask(SuiteTask suiteTask) throws Exception {
 	    
 	    if(suiteTask.getProjectName()==null || suiteTask.getProjectName().isEmpty()) {
 	        throw new IncorrectTaskException("Project name should not be empty");
 	    }
 	    
 	    if(suiteTask.getProjectVersion()==null || suiteTask.getProjectVersion().isEmpty()) {
             throw new IncorrectTaskException("Project version should not be empty");
         }
 	    
 	    String serverControlCode = server.getProjectControlCode(suiteTask.getProjectName(), suiteTask.getProjectVersion());
 	    if(serverControlCode==null) {
 	        throw new Exception("There is no project found on a server storage");
 	    }
 	    String storageControlKey = storage.readProjectControlKey(suiteTask.getProjectName(), suiteTask.getProjectVersion());
 	    
 	    if(serverControlCode.equals(storageControlKey)) {
 	        return true;
 	    }
 	    return false;
 	}
 
 	@Override
 	public void runSuiteTask(SuiteTask task) throws Exception {
 	    
 	    Boolean isSynced = synchronizeProjectForTask(task);
 	    shouldCurrentTaskProceed = true;
 	    
 		this.task = task;
 		logger.info("Running task " + task);
 		taskStatus = task.getTaskStatus();
 		taskRunner = TaskRunner.createTaskRunner(task);
 		taskRunner.setAgent(this);
 		if (task instanceof SuiteTask) {
 			SuiteTask suiteTask = (SuiteTask) task;
 
 			suiteTask.getSuite().setAgentName(getAgentInformation().getName());
 		}
 		
 		taskRunner.setProjectSyncNeeded(!isSynced);
 		taskRunner.start();
 	}
 
 	@Override
 	public void stopCurrentTask() {
 		shouldCurrentTaskProceed = false;
 	}
 	
 	@Override
 	public Boolean shouldProceed() {
 	    return shouldCurrentTaskProceed;
 	}
 
 	public void setServer(AgentServerRemoteInterface server) {
 		this.server = server;
 	}
 
 	public AgentServerRemoteInterface getServer() {
 		return server;
 	}
 
 	@Override
 	public void onTestAction(String name, Integer percent) {
 		logger.info(name);
 
 		taskStatus.setStatus(TaskStatus.ACTIVE);
 		try {
 			server.updateTaskStatus(task.getId(), taskStatus);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
     public void onTestStarted(TestStatus testStatus) {
         logger.info(testStatus.getName());
         try {
             taskStatus.getSuiteInformation().changeTestStatus(testStatus.getCustomId(), testStatus);
             server.updateTaskStatus(task.getId(), taskStatus);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 	
 	@Override
 	public void onTestFinished(TestStatus testStatus) {
 		logger.info(testStatus.getName());
 		
 		try {
 		    taskStatus.getSuiteInformation().changeTestStatus(testStatus.getCustomId(), testStatus);
 			server.updateTaskStatus(task.getId(), taskStatus);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onTaskFinished(Long suiteId) {
 		logger.info("Task is finished");
 		taskStatus.setStatus(TaskStatus.COMPLETED);
 		try {
 			server.updateTaskStatus(task.getId(), taskStatus);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		task = null;
 
 	}
 	
 	/**
 	 * Invoked in case if an error occured during task execution
 	 */
 	public void onTaskError(Throwable error) {
 	    logger.info("Task is finished with error");
         taskStatus.setStatus(TaskStatus.COMPLETED);
         taskStatus.setMessage(error.getClass().getName()+": "+error.getMessage());
         try {
             server.updateTaskStatus(task.getId(), taskStatus);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         task = null;
 	}
 	
 
 	public static void verifyResource(Properties properties, String key) throws Exception {
 		String path = properties.getProperty(key);
 		if (path == null)
 			throw new Exception("The " + key + " property wasn't specified");
 
 		File file = new File(path);
 		if (!file.exists()) {
 			throw new Exception("The " + key + " property refers to unexistent path: " + path);
 		}
 		else if (!file.isDirectory()) {
 			throw new Exception("The " + key + " property refers to not a directory: " + path);
 		}
 	}
 	
 	/**
 	 * This method will be used each 30 seconds after the connection to
 	 * TRMServer is lost
 	 * 
 	 * @throws Exception
 	 */
 	public void reconnect() throws Exception {
 		logger.info("Connecting to " + serverName);
 		
 		this.server = lookup.getRemoteObject(serverName, AgentServerRemoteInterface.class);
 		startConnection();
 	}
 
 	public static void saveZip(String path, byte[] bytes) throws IOException {
 		File fileTemp = new File(path);
 		if (fileTemp.exists()) {
 			fileTemp.delete();
 		}
 		fileTemp.createNewFile();
 
 		FileOutputStream fos = new FileOutputStream(fileTemp);
 		fos.write(bytes);
 		fos.flush();
 		fos.close();
 	}
 
 		
 	public void stopAgent() throws Exception {
 	    agentConnectionChecker.stopConnectionChecker();
 	    registry.stop();
 	    shouldRun = false;
 	}
 	
 	public void startAgent() throws Exception {
 	    shouldRun = true;
 	    if(this.storage==null) {
 	        throw new IllegalArgumentException("Storage is not defined");
 	    }
         Lookup lookup = GridUtils.createDefaultLookup();
         lookup.setUrl("http://"+serverHost+":"+serverPort);
         
         this.server = (AgentServerRemoteInterface) lookup.getRemoteObject(serverName, AgentServerRemoteInterface.class);
         this.lookup = lookup;
         
         registry.addObject(agentRemoteName, this);
         registry.setPort(agentPort);
         
         RegistryStarter registryStarter = new RegistryStarter();
         registryStarter.setRegistry(registry);
         
         registryStarter.startRegistry();
         int count = 0;
         while(!registryStarter.getRegistry().isRunning()) {
             //Waiting for Registry to start
             Thread.sleep(100);
             count++;
             if(count>600) {
                 throw new TimeoutException("Registry is not started");
             }
         }
         
         startConnection();
         agentConnectionChecker.setAgent(this);
         agentConnectionChecker.start();
         
         
         while(shouldRun) {
             //This is just to keep agent running
         }
 	}
 	
 	public String getServerName() {
         return serverName;
     }
 
     public String getServerHost() {
         return serverHost;
     }
 
     public Integer getServerPort() {
         return serverPort;
     }
 
     public String getAgentRemoteName() {
         return agentRemoteName;
     }
 
     public String getAgentHost() {
         return agentHost;
     }
 
     public String getAgentName() {
         return agentName;
     }
 
     public Integer getAgentPort() {
         return agentPort;
     }
 
     public void setServerName(String serverName) {
         this.serverName = serverName;
     }
 
     public void setServerHost(String serverHost) {
         this.serverHost = serverHost;
     }
 
     public void setServerPort(Integer serverPort) {
         this.serverPort = serverPort;
     }
 
     public void setAgentRemoteName(String agentRemoteName) {
         this.agentRemoteName = agentRemoteName;
     }
 
     public void setAgentHost(String agentHost) {
         this.agentHost = agentHost;
     }
 
     public void setAgentName(String agentName) {
         this.agentName = agentName;
     }
 
     public void setAgentPort(Integer agentPort) {
         this.agentPort = agentPort;
     }
 
     public static void main(String[] args) throws Exception {
         Agent agent = new Agent();
         Properties properties = new Properties();
         properties.load(new FileReader(new File("grid.agent.properties")));
         agent.serverHost = properties.getProperty(AgentProperties.SERVER_HOST);
         agent.serverPort = Integer.parseInt(properties.getProperty(AgentProperties.SERVER_PORT));
         agent.serverName = properties.getProperty(AgentProperties.SERVER_NAME);
         
         agent.agentHost = properties.getProperty(AgentProperties.AGENT_HOST);
         agent.agentPort = Integer.parseInt(properties.getProperty(AgentProperties.AGENT_PORT));
         agent.agentName = properties.getProperty(AgentProperties.AGENT_NAME);
         agent.agentRemoteName = properties.getProperty(AgentProperties.AGENT_REMOTE_NAME);
         agent.agentReconnectionTimeout = Integer.parseInt(properties.getProperty(AgentProperties.AGENT_RECONNECT_TIMEOUT));
         agent.agentDescription = properties.getProperty(AgentProperties.AGENT_DESCRIPTION);
         agent.agentStoragePath = properties.getProperty(GridProperties.STORAGE_PATH);
        agent.agentOculusGridLibrary = properties.getProperty(AgentProperties.AGENT_OCULUS_GRID_LIBRARY);
         agent.AgentOculusRunner = properties.getProperty(AgentProperties.AGENT_OCULUS_RUNNER);
         
         DefaultAgentStorage storage = new DefaultAgentStorage();
             
         storage.setStoragePath(properties.getProperty(GridProperties.STORAGE_PATH));
         agent.storage = storage;
         agent.startAgent();
     }
 
     public void setAgentInformation(AgentInformation agentInformation) {
         this.agentInformation = agentInformation;
     }
 
     public AgentInformation getAgentInformation() {
         return agentInformation;
     }
 
     public void setAgentId(AgentId agentId) {
         this.agentId = agentId;
     }
 
     public AgentId getAgentId() {
         return agentId;
     }
 
     public void setAgentReconnectionTimeout(Integer agentReconnectionTimeout) {
         this.agentReconnectionTimeout = agentReconnectionTimeout;
     }
 
     public Integer getAgentReconnectionTimeout() {
         return agentReconnectionTimeout;
     }
 
     public Storage getStorage() {
         return storage;
     }
 
     public void setStorage(Storage storage) {
         this.storage = storage;
     }
 
     public String getAgentDescription() {
         return agentDescription;
     }
 
     public void setAgentDescription(String agentDescription) {
         this.agentDescription = agentDescription;
     }
 
     public String getAgentOculusRunner() {
         return AgentOculusRunner;
     }
 
     public void setAgentOculusRunner(String agentOculusRunner) {
         AgentOculusRunner = agentOculusRunner;
     }
 
     public String getAgentOculusGridLibrary() {
         return agentOculusGridLibrary;
     }
 
     public void setAgentOculusGridLibrary(String agentOculusGridLibrary) {
         this.agentOculusGridLibrary = agentOculusGridLibrary;
     }
 
     public String getAgentStoragePath() {
         return agentStoragePath;
     }
 
     public void setAgentStoragePath(String agentStoragePath) {
         this.agentStoragePath = agentStoragePath;
     }
 }
