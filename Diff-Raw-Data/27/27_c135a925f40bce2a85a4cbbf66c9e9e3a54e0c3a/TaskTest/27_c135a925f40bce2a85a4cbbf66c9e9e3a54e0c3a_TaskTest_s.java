 package net.mindengine.oculus.grid.test.suites;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.mindengine.oculus.experior.suite.Suite;
 import net.mindengine.oculus.experior.suite.XmlSuiteParser;
 import net.mindengine.oculus.grid.client.GridClient;
 import net.mindengine.oculus.grid.domain.task.DefaultTask;
 import net.mindengine.oculus.grid.domain.task.SuiteTask;
 import net.mindengine.oculus.grid.domain.task.TaskInformation;
 import net.mindengine.oculus.grid.server.Server;
 import net.mindengine.oculus.grid.service.ClientServerRemoteInterface;
 import net.mindengine.oculus.grid.service.exceptions.IncorrectTaskException;
 
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TaskTest {
     static final Server server = new Server();
     
     static Thread serverThread = new Thread(new Runnable() {
         @Override
         public void run() {
             try {
                 server.startServer(8100, "server");
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     });
     
     @BeforeClass
     public static void startServer() throws InterruptedException {
         serverThread.start();
         Thread.sleep(4000);
     }
     
     @SuppressWarnings("deprecation")
     @AfterClass
     public static void stopServer() throws Exception {
         server.stopServer();
         serverThread.stop();
         Thread.sleep(4000);
     }
     
     @Test
    public void serveSendsRecievesTask() throws Exception{
         GridClient client = new GridClient();
         client.setServerHost("localhost");
         client.setServerPort(8100);
         client.setServerName("server");
         
         ClientServerRemoteInterface remote = client.getServer();
         
         assertNotNull(remote);
         
         DefaultTask task = new DefaultTask();
         task.setName("sample task1");
         
         task.setAgentNames(new String[]{"agent1"});
         task.setCreatedDate(new Date(1234567));
         
         List<SuiteTask> suiteTasks = new LinkedList<SuiteTask>();
         SuiteTask suiteTask = new SuiteTask();
         suiteTasks.add(suiteTask);
         
         suiteTask.setAgentNames(new String[]{"agent2", "agent3"});
         suiteTask.setName("sample suite task");
         
         Suite suite = XmlSuiteParser.parse(new File(getClass().getResource("/sample-suite.xml").toURI()));
         suiteTask.setSuite(suite);
         task.setSuiteTasks(suiteTasks);
         
         Long taskId = remote.runTask(task);
         assertNotNull(taskId);
         assertTrue(taskId>0L);
         Thread.sleep(1000);
         
         Collection<TaskInformation> taskList = remote.getTasksList();
         assertNotNull(taskList);
         assertEquals(1, taskList.size());
     }
     
     @Test(expected=IncorrectTaskException.class)
     public void emptyTaskGivesError() throws Exception {
         GridClient client = new GridClient();
         client.setServerHost("localhost");
         client.setServerPort(8100);
         client.setServerName("server");
         
         ClientServerRemoteInterface remote = client.getServer();
         assertNotNull(remote);
         
         DefaultTask task = new DefaultTask();
         task.setAgentNames(new String[]{"agent1"});
         task.setCreatedDate(new Date(1234567));
         List<SuiteTask> suiteTasks = new LinkedList<SuiteTask>();
         task.setSuiteTasks(suiteTasks);
         
         
         
         remote.runTask(task);
     }
     
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        String str = "{\"suiteTasks\":[],\"name\":null,\"id\":null,\"taskStatus\":{\"interrupted\":false,\"message\":null,\"status\":0,\"assignedAgent\":null,\"suiteIds\":null,\"suiteId\":null,\"percent\":0,\"taskId\":null,\"completedTests\":[],\"taskName\":null},\"taskUser\":null,\"agentNames\":[\"agent1\"],\"createdDate\":1234567,\"startedDate\":null,\"completedDate\":null}}";
        str ="{\"name\":\"h\"}";
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.readValue(str, Suite.class);
    }
 }
