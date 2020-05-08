 package dk.kb.yggdrasil;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.UUID;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.mockito.Mockito;
 
 import dk.kb.yggdrasil.db.PreservationRequestState;
 import dk.kb.yggdrasil.db.StateDatabase;
 import dk.kb.yggdrasil.exceptions.YggdrasilException;
 import dk.kb.yggdrasil.json.PreservationRequest;
 import dk.kb.yggdrasil.messaging.MQ;
 import dk.kb.yggdrasil.xslt.Models;
 
 /**
  * Created by jatr on 2/24/14.
  */
 @RunWith(JUnit4.class)
 public class WorkflowTest {
 
     private static Workflow workflow;
     private static String RABBITMQ_CONF_FILE = "src/test/resources/config/rabbitmq.yml";
     private static File generalConfigFile = new File("config/yggdrasil.yml");
     private static MQ mq;
     private static RabbitMqSettings settings; 
 
     @BeforeClass
     public static void beforeClass() throws YggdrasilException, IOException {
    	System.setProperty("dk.kb.yggdrasil.runningmode", "test");
 
         Config config = new Config(generalConfigFile);
         FileUtils.deleteDirectory(config.getDatabaseDir());
 
         File rabbitMQConfig = new File(RABBITMQ_CONF_FILE);
         settings = new RabbitMqSettings(rabbitMQConfig);
         mq = new MQ(settings);
         StateDatabase stateDatabase = new StateDatabase(config.getDatabaseDir());
         Bitrepository bitrepository = Mockito.mock(Bitrepository.class);
         Models models = new Models(new File("config/models.yml"));
 
         workflow = new Workflow(mq, stateDatabase, bitrepository, config, models);
     }
 
     @Test
     public void correctlyFormattedPreservationResponse() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, YggdrasilException {
         PreservationRequest preservationRequest = new PreservationRequest();
         preservationRequest.Model = "Book";
         preservationRequest.Valhal_ID = "Valhal:1";
         preservationRequest.File_UUID = "ertret345645645er456456rty";
         State newPreservationState = State.PRESERVATION_RESOURCES_DOWNLOAD_SUCCESS;
         PreservationRequestState prs = new PreservationRequestState(preservationRequest, newPreservationState, UUID.randomUUID().toString());
 
         Method updateRemotePreservationState = Workflow.class.getDeclaredMethod("updateRemotePreservationState", 
         		PreservationRequestState.class, State.class);
         updateRemotePreservationState.setAccessible(true);
         updateRemotePreservationState.invoke(workflow, prs, newPreservationState);
 
         //now read message from queue and check content is correct for this use case
         
     }
     
     @Test
     public void handlingRequestMessage() throws NoSuchMethodException, SecurityException, YggdrasilException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
     	Method getNextRequest = Workflow.class.getDeclaredMethod("getNextRequest");
     	getNextRequest.setAccessible(true);
     	
     	String UUID = "444cd730-3f15-0131-5772-0050562881f4";
     	String profile = "simple";
     	String ValhalId = "valhal:1";
     	String model = "Book";
     	String message = "{\"UUID\":\"" + UUID + "\","
     			+ "\"Preservation_profile\":\"" + profile + "\","
     			+ "\"Valhal_ID\":\"" + ValhalId + "\","
     			+ "\"Model\":\"" + model + "\","
     			+ "\"metadata\": \"<metadata></metadata>\""
     			+ "}";
     	
     	mq.publishOnQueue(settings.getPreservationDestination(), 
     			message.getBytes(), 
     			MQ.PRESERVATIONREQUEST_MESSAGE_TYPE);
    	
     	PreservationRequest req = (PreservationRequest) getNextRequest.invoke(workflow);
     	Assert.assertNotNull(req);
     	Assert.assertEquals(req.UUID, UUID);
     	Assert.assertEquals(req.Preservation_profile, profile);
     	Assert.assertEquals(req.Valhal_ID, ValhalId);
     	Assert.assertEquals(req.Model, model);
     	Assert.assertNull(req.File_UUID);
     	Assert.assertNull(req.Content_URI);
     }
 }
