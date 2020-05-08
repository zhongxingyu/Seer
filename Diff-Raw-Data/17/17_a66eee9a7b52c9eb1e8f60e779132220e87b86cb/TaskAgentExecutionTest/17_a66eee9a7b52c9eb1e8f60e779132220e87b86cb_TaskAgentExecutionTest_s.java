 package net.mindengine.oculus.grid.test.suites;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.mindengine.jeremy.registry.Lookup;
 import net.mindengine.oculus.experior.suite.Suite;
 import net.mindengine.oculus.experior.suite.XmlSuiteParser;
 import net.mindengine.oculus.grid.GridUtils;
 import net.mindengine.oculus.grid.agent.Agent;
 import net.mindengine.oculus.grid.client.GridClient;
 import net.mindengine.oculus.grid.domain.task.DefaultTask;
 import net.mindengine.oculus.grid.domain.task.SuiteStatistic;
 import net.mindengine.oculus.grid.domain.task.SuiteTask;
 import net.mindengine.oculus.grid.domain.task.TaskInformation;
 import net.mindengine.oculus.grid.domain.task.TaskStatus;
 import net.mindengine.oculus.grid.runner.DefaultOculusRunner;
 import net.mindengine.oculus.grid.server.Server;
 import net.mindengine.oculus.grid.service.AgentServerRemoteInterface;
 import net.mindengine.oculus.grid.storage.DefaultAgentStorage;
 import net.mindengine.oculus.grid.storage.DefaultGridStorage;
 import net.mindengine.oculus.grid.storage.Project;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TaskAgentExecutionTest {
     static final Server server = new Server();
     static final Agent agent = new Agent();
     
     private static final String OCULUS_TEST_HOME = System.getenv("OCULUS_TEST_HOME");
 
     static Thread serverThread = new Thread(new Runnable() {
         @Override
         public void run() {
             try {
                 server.startServer(8200, "server");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     });
     
     static Thread agentThread = new Thread(new Runnable() {
         @Override
         public void run() {
             try {
                 agent.setAgentHost("localhost");
                 agent.setAgentPort(8201);
                 agent.setAgentName("Agent 1");
                 agent.setAgentReconnectionTimeout(10);
                 agent.setAgentRemoteName("agent");
                 DefaultAgentStorage storage = new DefaultAgentStorage();
                 storage.setStoragePath(OCULUS_TEST_HOME+"/data/storage-agent-1");
                 agent.setAgentStoragePath(OCULUS_TEST_HOME+"/data/storage-agent-1");
                 agent.setAgentOculusGridLibrary(OCULUS_TEST_HOME+"/data/grid-library/oculus-grid.jar");
                 agent.setAgentOculusRunner(DefaultOculusRunner.class.getName());
                 agent.setStorage(storage);
                 agent.setServerHost("localhost");
                 agent.setServerPort(8200);
                 agent.setServerName("server");
                 agent.startAgent();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     });
     
     @BeforeClass
     public static void startServer() throws InterruptedException {
         DefaultGridStorage storage = new DefaultGridStorage();
         
         storage.setStoragePath(OCULUS_TEST_HOME+"/data/storage-server");
         server.setStorage(storage);
         serverThread.start();
         Thread.sleep(2000);
         
         //Starting agent
         
         agentThread.start();
         Thread.sleep(2000);
     }
     
     @SuppressWarnings("deprecation")
     @AfterClass
     public static void stopServer() throws Exception {
         server.stopServer();
         serverThread.stop();
         Thread.sleep(4000);
     }
     
     public GridClient createClient() {
         GridClient client = new GridClient();
         client.setServerHost("localhost");
         client.setServerPort(8200);
         client.setServerName("server");
         return client;
     }
     
     public DefaultTask createBasicTask(String suitePath) throws URISyntaxException, Exception {
         DefaultTask task = new DefaultTask();
         task.setName("sample task1");
         
         task.setAgentNames(new String[]{"agent1"});
         task.setCreatedDate(new Date(1234567));
         
         List<SuiteTask> suiteTasks = new LinkedList<SuiteTask>();
         SuiteTask suiteTask = new SuiteTask();
         suiteTasks.add(suiteTask);
         
         suiteTask.setName("sample suite task");
         
         Suite suite = XmlSuiteParser.parse(new File(getClass().getResource("/"+suitePath).toURI()));
         suiteTask.setSuite(suite);
         suiteTask.setProjectName("sample-project");
         suiteTask.setProjectVersion("current");
         task.setSuiteTasks(suiteTasks);
         return task;
     }
     
     
     @Test
     public void canDownloadProjectFromServerStorage() throws Exception{
         Lookup lookup = GridUtils.createDefaultLookup();
         lookup.setUrl("http://localhost:8200");
         AgentServerRemoteInterface server = lookup.getRemoteObject("server", AgentServerRemoteInterface.class);
         
         Project project = server.downloadProject("sample-project", "current");
         assertNotNull(project);
         assertNotNull(project.getBytes());
         assertTrue(project.getBytes().length>1000);
         assertEquals("iamacontrolkey", project.getControlKey());
         assertEquals("sample-project-current.zip", project.getName());
         assertEquals("sample-project", project.getProjectName());
         assertEquals("current", project.getProjectVersion());
     }
     
     public void assertFileExists(String filePath){
         File file = new File(filePath);
         assertTrue(file.exists());
     }
     
     @Test
     public void runsSuccessfullyATestSuite() throws Exception {
         File projectInAgentStorage = new File(OCULUS_TEST_HOME+"/data/storage-agent-1/sample-project");
         if(projectInAgentStorage.exists()) {
             FileUtils.deleteDirectory(projectInAgentStorage);
         }
         
         File testLogFile = new File(OCULUS_TEST_HOME+"/data/logs/net.mindengine.oculus.test.sample.TestWith3Actions.log");
         if(testLogFile.exists()) {
             testLogFile.delete();
         }
         
         DefaultTask task = createBasicTask("oculus-sample-project-suite-1.xml");
         GridClient client = createClient();
         
         Long taskId = client.getServer().runTask(task);
         Thread.sleep(25000);
         
         TaskStatus taskStatus = client.getServer().getTaskStatus(taskId);
         
         assertEquals(TaskStatus.COMPLETED, taskStatus.getStatus());
         
         assertFileExists(OCULUS_TEST_HOME+"/data/storage-agent-1/sample-project/current/.gridproject");
         assertFileExists(OCULUS_TEST_HOME+"/data/storage-agent-1/sample-project/current/sample-project-current.jar");
         assertFileExists(OCULUS_TEST_HOME+"/data/storage-agent-1/sample-project/current/experior.properties");
         
         assertFileExists(testLogFile.getAbsolutePath());
         String log = FileUtils.readFileToString(testLogFile);
         assertEquals("[beforeTest][action1][action2][action3][afterTest]", log);
         
         
         TaskInformation[] tasks = client.getServer().getTasks(taskId);
         assertNotNull(tasks);
         assertEquals(1, tasks.length);
         TaskStatus suiteTaskStatus = client.getServer().getTaskStatus(tasks[0].getTaskId());
         
         assertNotNull(suiteTaskStatus.getSuiteInformation());
         SuiteStatistic suiteStatistic = suiteTaskStatus.getSuiteInformation().calculateStatistics();
        assertEquals(0, (int)suiteStatistic.getFailed());
         assertEquals(0, (int)suiteStatistic.getWarning());
         assertEquals(1, (int)suiteStatistic.getPassed());
        assertEquals(0, (int)suiteStatistic.getPostponed());
        assertEquals(1, (int)suiteStatistic.getTotal());
        
        //TODO Use more complicated suite with failures, warning and postponed tests
         
     }
     
 }
