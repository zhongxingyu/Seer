 package pegasus.eventbus.rabbitmq;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.io.IOException;
 
 import org.junit.*;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import pegasus.eventbus.amqp.AmqpMessageBus.UnexpectedConnectionCloseListener;
 import pegasus.eventbus.amqp.RoutingInfo;
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.rabbitmq.RabbitConnection.UnexpectedCloseListener;
 
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.Channel;
 
 public class RabbitMessageBusTest {
 
 	@Mock
 	private RabbitConnection connection;
 	@Mock
 	private Channel channel1;
 	@Mock
 	private Channel channel2;
 	@Mock
 	private UnexpectedConnectionCloseListener busCloseListener;
 	
 	private RabbitMessageBus messageBus;
 	
 	private RoutingInfo route = new RoutingInfo("TestExchange", "route1");
 	
 	private UnexpectedCloseListener conectionCloseListener;
 	
 	@Before
 	public void beforeEachTest() throws IOException {
 		
 		MockitoAnnotations.initMocks(this);
 		
 		doAnswer(new Answer<Object>(){
 			@Override
 			public Object answer(InvocationOnMock invocation) throws Throwable {
 				conectionCloseListener = (UnexpectedCloseListener) invocation.getArguments()[0];
 				return null;
 			}}).when(connection).attachUnexpectedCloseListener(any(UnexpectedCloseListener.class));
 
 		when(connection.createChannel())
 			.thenReturn(channel1)
 			.thenReturn(channel2)
 			.thenThrow(new RuntimeException("Too many channels opened!"));
 		
 		messageBus = new RabbitMessageBus(connection);
 		
 		messageBus.attachUnexpectedConnectionCloseListener(busCloseListener);
 		
 		messageBus.start();
         
 	}
 	
 	@Test
 	public void priorToAconnectionResetATheOriginalCommandChannelShouldBeUsed() throws IOException{
 		messageBus.publish(route, new Envelope());
 		verify(channel1, times(1)).basicPublish(eq(route.getExchange().getName()), eq(route.getRoutingKey()), any(BasicProperties.class), eq(new byte[0]));
 	}
 	
 	@Test
 	public void priorToAConnectionResetOnlyOneCommandChannelIsCreate() throws IOException{
 		verify(connection, times(1)).createChannel();
 	}
 
 	@Test
 	public void afterAConnectionResetANewCommandChannelShouldBeUsed() throws IOException{
 		conectionCloseListener.onUnexpectedClose(true);
 		messageBus.publish(route, new Envelope());
 		verify(channel2, times(1)).basicPublish(eq(route.getExchange().getName()), eq(route.getRoutingKey()), any(BasicProperties.class), eq(new byte[0]));
 	}
 
 	@Test
 	public void afterAConnectionResetThePreviousCommandChannelShouldNoLongerBeUsed(){
 		conectionCloseListener.onUnexpectedClose(true);
		reset(channel1);
 		messageBus.publish(route, new Envelope());
 		verifyNoMoreInteractions(channel1);
 	}
 
 	@Test
 	public void afterAconnectionResetTheXShouldBeCalled(){
 		conectionCloseListener.onUnexpectedClose(true);
 		verify(busCloseListener).onUnexpectedConnectionClose(true);
 	}
 	
 }
