 package pegasus.eventbus.rabbitmq;
 
 import static com.jayway.awaitility.Awaitility.to;
 import static com.jayway.awaitility.Awaitility.waitAtMost;
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.junit.*;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
import pegasus.eventbus.amqp.ConnectionParameters;
 import pegasus.eventbus.rabbitmq.RabbitConnection.UnexpectedCloseListener;
 import pegasus.eventbus.testsupport.RabbitManagementApiHelper;
 
 public class RabbitConnection_Test implements UnexpectedCloseListener{
 
    protected ConnectionParameters          connectionParameters;
     protected RabbitManagementApiHelper     rabbitManagementApi;
     protected TestableConnection              connection;
     private FileSystemXmlApplicationContext context;
     private boolean onUnexpectedCloseCalled;
     private boolean successfullyReopened;
 
 	@Before
 	public void beforeEachTest() throws IOException {
 
 		context = new FileSystemXmlApplicationContext("src/test/resources/eventbus-context.xml");
		connectionParameters = context.getBean(ConnectionParameters.class);
 		connection = new TestableConnection(connectionParameters);
 
         rabbitManagementApi = new RabbitManagementApiHelper(connectionParameters);
         rabbitManagementApi.createVirtualHost();
         
         connection.open();
 
         connection.attachUnexpectedCloseListener(this);
 	}
 
 	@After
 	public void afterEachTest() throws IOException {
         connection.close();
 		rabbitManagementApi.deleteVirtualHost();
         context.close();
 	}
 	
 	private class TestableConnection extends RabbitConnection{
 
		public TestableConnection(ConnectionParameters connectionParameters) {
 			super(connectionParameters);
 		}
 		
 		public void simulateConnectionFailure() throws IOException{
 			connection.close();
 		}
 	}
 
 	@Test
 	public void aFailedConnectionShouldAutomaticallyReconnect() throws Exception{
 		connection.simulateConnectionFailure();
 		waitAtMost(2, TimeUnit.SECONDS).untilCall(to(this).wasOnUnexpectedCloseCalled(), equalTo(true));
 		assertTrue(successfullyReopened);
 		//ensures that connection is really open.
 		connection.createChannel();
 	}
 
 	@Test(expected=TimeoutException.class)
 	public void ClosingAConnectionShouldNotCallOnUnexpectedClose() throws Exception{
 		connection.close();
 		waitAtMost(2, TimeUnit.SECONDS).untilCall(to(this).wasOnUnexpectedCloseCalled(), equalTo(true));
 	}
 
 	@Override
 	public void onUnexpectedClose(boolean successfullyReopened) {
 		onUnexpectedCloseCalled = true;
 		this.successfullyReopened = successfullyReopened;
 	}
 
 	public boolean wasOnUnexpectedCloseCalled(){
 		return onUnexpectedCloseCalled;
 	}
 	
 }
