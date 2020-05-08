 package commons.sim.components;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Field;
 import java.util.Queue;
 
 import org.easymock.Capture;
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import util.ValidConfigurationTest;
 
 import commons.cloud.MachineType;
 import commons.cloud.Request;
 import commons.io.Checkpointer;
 import commons.sim.jeevent.JEEvent;
 import commons.sim.jeevent.JEEventScheduler;
 import commons.sim.jeevent.JEEventType;
 
 /**
  * Backlog size = 2
  * Number of Threads = 3
  * @author Ricardo Ara&uacute;jo Santos - ricardo@lsd.ufcg.edu.br
  */
 public class RanjanMachineTest extends ValidConfigurationTest {
 	
 	private MachineDescriptor descriptor;
 	
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		buildFullRanjanConfiguration();
 		this.descriptor = new MachineDescriptor(1, false, MachineType.M1_SMALL, 0);
 	}
 
 	@Test
 	public void testConstructor() {
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), descriptor, null);
 		assertEquals(descriptor, machine.getDescriptor());
 		assertNull(machine.getLoadBalancer());
 		assertNotNull(machine.getProcessorQueue());
 		assertTrue(machine.getProcessorQueue().isEmpty());
 	}
 
 	@Test
 	public void testSmallRequestExecutionWithSingleCoreMachine(){
 	
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		Capture<Request> captured = new Capture<Request>();
 		loadBalancer.reportRequestFinished(EasyMock.capture(captured));
 		EasyMock.replay(loadBalancer);
 		
 		Request request = new Request(0, 0, 0, 0, 10, 100, new long[]{50, 50});
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), descriptor, loadBalancer);
 		
 		machine.sendRequest(request);
 		Queue<Request> queue = machine.getProcessorQueue();
 		assertNotNull(queue);
 		assertTrue(queue.isEmpty());
 		
 		Checkpointer.loadScheduler().start();
 		
 		assertEquals(request, captured.getValue());
 		assertEquals(50, Checkpointer.loadScheduler().now());
 	}
 
 	/**
 	 * This method verifies that a single request is correctly added to a machine, without already
 	 * processing requests, since the limit of threads is not reached
 	 * @throws Exception 
 	 */
 	@Test
 	public void testBigRequestExecutionWithSingleCoreMachine(){
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		Capture<Request> captured = new Capture<Request>();
 		loadBalancer.reportRequestFinished(EasyMock.capture(captured));
 		EasyMock.replay(loadBalancer);
 		
 		Request request = new Request(0, 0, 0, 0, 10, 100, new long[]{2500, 2500});
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), descriptor, loadBalancer);
 		
 		machine.sendRequest(request);
 		Queue<Request> queue = machine.getProcessorQueue();
 		assertNotNull(queue);
 		assertTrue(queue.isEmpty());
 		
 		Checkpointer.loadScheduler().start();
 		
 		assertEquals(request, captured.getValue());
 		assertEquals(2500, Checkpointer.loadScheduler().now());
 	}
 	
 	@Test
 	public void testSendRequestToBacklogSingleCoreMachine() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
 		Request request = new Request(0, 0, 0, 0, 10, 100, new long[]{5000, 5000});
 		Request request2 = new Request(0, 0, 0, 0, 10, 100, new long[]{5000, 5000});
 		Request request3 = new Request(0, 0, 0, 0, 10, 100, new long[]{5000, 5000});
 		Request request4 = new Request(0, 0, 0, 0, 10, 100, new long[]{5000, 5000});
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
		loadBalancer.reportRequestQueued(request);
		loadBalancer.reportRequestQueued(request2);
		loadBalancer.reportRequestQueued(request3);
		loadBalancer.reportRequestQueued(request4);
 		EasyMock.replay(loadBalancer);
 		
 		Queue<Request> queue = EasyMock.createStrictMock(Queue.class);
 		Capture<Request> captured = new Capture<Request>();
 		EasyMock.expect(queue.size()).andReturn(0);
 		EasyMock.expect(queue.add(EasyMock.capture(captured))).andReturn(true);
 		EasyMock.expect(queue.isEmpty()).andReturn(false);
 		EasyMock.expect(queue.poll()).andReturn(request4);
 		EasyMock.expect(queue.isEmpty()).andReturn(true).times(3);
 		EasyMock.replay(queue);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), new MachineDescriptor(1, false, MachineType.M1_SMALL, 0), loadBalancer);
 		
 		Field declaredField = TimeSharedMachine.class.getDeclaredField("backlog");
 		declaredField.setAccessible(true);
 		declaredField.set(machine, queue);
 		
 		machine.sendRequest(request);
 		machine.sendRequest(request2);
 		machine.sendRequest(request3);
 		machine.sendRequest(request4);
 		
 		Queue<Request> processorQueue = machine.getProcessorQueue();
 		assertEquals(2, processorQueue.size());
 		assertEquals(request2, processorQueue.poll());
 		assertEquals(request3, processorQueue.poll());
 		
 		assertEquals(request4, captured.getValue());
 		
 		Checkpointer.loadScheduler().start();
 		
 		assertEquals(20000, Checkpointer.loadScheduler().now());
 		
 		EasyMock.verify(queue);
 	}
 	
 	@Test
 	public void testSendRequestToBacklogMultiCoreMachine() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
 		Request request = new Request(1, 0, 0, 0, 10, 100, new long[]{5000, 5000, 5000, 5000, 5000});
 		Request request2 = new Request(2, 0, 0, 0, 10, 100, new long[]{5000, 5000, 5000, 5000, 5000});
 		Request request3 = new Request(3, 0, 0, 0, 10, 100, new long[]{5000, 5000, 5000, 5000, 5000});
 		Request request4 = new Request(4, 0, 0, 0, 10, 100, new long[]{5000, 5000, 5000, 5000, 5000});
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		loadBalancer.reportRequestFinished(request);
 		loadBalancer.reportRequestFinished(request2);
 		loadBalancer.reportRequestFinished(request3);
		loadBalancer.reportRequestQueued(request4);
 		EasyMock.replay(loadBalancer);
 		
 		Queue<Request> queue = EasyMock.createStrictMock(Queue.class);
 		Capture<Request> captured = new Capture<Request>();
 		EasyMock.expect(queue.size()).andReturn(0);
 		EasyMock.expect(queue.add(EasyMock.capture(captured))).andReturn(true);
 		EasyMock.expect(queue.isEmpty()).andReturn(false);
 		EasyMock.expect(queue.poll()).andReturn(request4);
 		EasyMock.expect(queue.isEmpty()).andReturn(true).times(3);
 		EasyMock.replay(queue);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), new MachineDescriptor(1, false, MachineType.M1_LARGE, 0), loadBalancer);
 		
 		Field declaredField = TimeSharedMachine.class.getDeclaredField("backlog");
 		declaredField.setAccessible(true);
 		declaredField.set(machine, queue);
 		
 		machine.sendRequest(request);
 		machine.sendRequest(request2);
 		machine.sendRequest(request3);
 		machine.sendRequest(request4);
 		
 		Queue<Request> processorQueue = machine.getProcessorQueue();
 		assertFalse(processorQueue.isEmpty());
 		assertEquals(request3, processorQueue.poll());

 		assertEquals(request4, captured.getValue());
 		
 		Checkpointer.loadScheduler().start();
 		
 		assertEquals(12500, Checkpointer.loadScheduler().now());
 
 		EasyMock.verify(queue);
 	}
 	
 	@Test
 	public void testShutdownWithEmptyMachine(){
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		EasyMock.expect(loadBalancer.getHandlerId()).andReturn(scheduler.registerHandler(loadBalancer));
 		Capture<JEEvent> captured = new Capture<JEEvent>();
 		loadBalancer.handleEvent(EasyMock.capture(captured));
 		EasyMock.expectLastCall();
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.shutdownOnFinish();
 		
 		scheduler.start();
 		
 		JEEvent event = captured.getValue();
 		assertNotNull(event);
 		assertEquals(JEEventType.MACHINE_TURNED_OFF, event.getType());
 		assertEquals(machine.getDescriptor(), event.getValue()[0]);
 		
 		assertEquals(0, Checkpointer.loadScheduler().now());
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testShutdownWithNonEmptyMachine(){
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		Request request = new Request(0, 0, 0, 0, 10, 100, new long[]{5000, 5000});
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		loadBalancer.reportRequestFinished(request);
 		EasyMock.expect(loadBalancer.getHandlerId()).andReturn(scheduler.registerHandler(loadBalancer));
 		Capture<JEEvent> captured = new Capture<JEEvent>();
 		loadBalancer.handleEvent(EasyMock.capture(captured));
 		EasyMock.expectLastCall();
 		EasyMock.replay(loadBalancer);
 
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		machine.shutdownOnFinish();
 		
 		scheduler.start();
 		
 		JEEvent event = captured.getValue();
 		assertNotNull(event);
 		assertEquals(JEEventType.MACHINE_TURNED_OFF, event.getType());
 		assertEquals(machine.getDescriptor(), event.getValue()[0]);
 		
 		assertEquals(5000, Checkpointer.loadScheduler().now());
 		
 		EasyMock.verify(loadBalancer);
 	}
 
 	/**
 	 * With one core and a number of threads equals to three, three requests stay
 	 * in processor processing queue and one is stored in the backlog
 	 */
 	@Test
 	public void testHandlePreemptionWithMoreRequestThanCanRunWithOneCore(){
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request firstRequest = new Request(1, 0, 0, 0, 10, 100, new long[]{50, 50});
 		Request secondRequest = new Request(2, 0, 0, 0, 10, 100, new long[]{200, 200});
 		Request thirdRequest = new Request(3, 0, 0, 0, 10, 100, new long[]{100, 100});
 		Request fourthRequest = new Request(4, 0, 0, 0, 10, 100, new long[]{100, 100});
 		
 		loadBalancer.reportRequestFinished(firstRequest);
 		loadBalancer.reportRequestFinished(thirdRequest);
 		loadBalancer.reportRequestFinished(fourthRequest);
 		loadBalancer.reportRequestFinished(secondRequest);
 		
 		EasyMock.replay(loadBalancer);
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_SMALL, 0), loadBalancer);
 		machine.sendRequest(firstRequest);
 		machine.sendRequest(secondRequest);
 		machine.sendRequest(thirdRequest);
 		machine.sendRequest(fourthRequest);
 
 		Queue<Request> queue = machine.getProcessorQueue();
 		assertEquals(2, queue.size());
 		assertEquals(secondRequest, queue.poll());
 		assertEquals(thirdRequest, queue.poll());
 		assertNull(queue.poll());
 		
 		scheduler.start();
 		
 		assertTrue(machine.getProcessorQueue().isEmpty());
 
 		EasyMock.verify(loadBalancer);
 	}
 	
 	/**
 	 * With one core and a number of threads equals to six, three requests are
 	 * in the processor queue, two requests stay waiting in the backlog and one request is rejected
 	 */
 	@Test
 	public void testReportLostRequestWithSingleCoreMachine(){
 
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 	
 		Request firstRequest = new Request(0, 0, 0, 0, 100, 100, new long[]{50, 50});
 		Request secondRequest = new Request(1, 0, 0, 0, 100, 100, new long[]{100, 100});
 		Request thirdRequest = new Request(2, 0, 0, 0, 100, 100, new long[]{100, 100});
 		Request fourthRequest = new Request(3, 0, 0, 0, 100, 100, new long[]{100, 100});
 		Request fifthRequest = new Request(4, 0, 0, 0, 100, 100, new long[]{100, 100});
 		Request sixthRequest = new Request(5, 0, 0, 0, 100, 100, new long[]{50, 50});
 
 		EasyMock.expect(loadBalancer.getHandlerId()).andReturn(Checkpointer.loadScheduler().registerHandler(loadBalancer));
 		
 		Capture<JEEvent> captured = new Capture<JEEvent>();
 		loadBalancer.handleEvent(EasyMock.capture(captured));
 		
 		loadBalancer.reportRequestFinished(firstRequest);
 		loadBalancer.reportRequestFinished(secondRequest);
 		loadBalancer.reportRequestFinished(thirdRequest);
 		loadBalancer.reportRequestFinished(fourthRequest);
 		loadBalancer.reportRequestFinished(fifthRequest);
 
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), new MachineDescriptor(1, false, MachineType.M1_SMALL, 0), loadBalancer);
 		machine.sendRequest(firstRequest);
 		machine.sendRequest(secondRequest);
 		machine.sendRequest(thirdRequest);
 		machine.sendRequest(fourthRequest);
 		machine.sendRequest(fifthRequest);
 		machine.sendRequest(sixthRequest);
 		
 		Queue<Request> queue = machine.getProcessorQueue();
 		assertEquals(2, queue.size());
 		assertEquals(secondRequest, queue.poll());
 		assertEquals(thirdRequest, queue.poll());
 		assertNull(queue.poll());
 		
 		Checkpointer.loadScheduler().start();
 		
 		JEEvent event = captured.getValue();
 		assertNotNull(event);
 		assertEquals(JEEventType.REQUESTQUEUED, event.getType());
 		assertEquals(sixthRequest, event.getValue()[0]);
 		
 		assertTrue(machine.getProcessorQueue().isEmpty());
 		
 		assertEquals(450, Checkpointer.loadScheduler().now());
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testReportLostRequestWithMultiCoreMachine(){
 	
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 	
 		Request firstRequest = new Request(0, 0, 0, 0, 100, 100, new long[]{50, 50, 50, 50, 50});
 		Request secondRequest = new Request(1, 0, 0, 0, 100, 100, new long[]{100, 100, 100, 100, 100});
 		Request thirdRequest = new Request(2, 0, 0, 0, 100, 100, new long[]{100, 100, 100, 100, 100});
 		Request fourthRequest = new Request(3, 0, 0, 0, 100, 100, new long[]{100, 100, 100, 100, 100});
 		Request fifthRequest = new Request(4, 0, 0, 0, 100, 100, new long[]{100, 100, 100, 100, 100});
 		Request sixthRequest = new Request(5, 0, 0, 0, 100, 100, new long[]{50, 50, 50, 50, 50});
 	
 		EasyMock.expect(loadBalancer.getHandlerId()).andReturn(Checkpointer.loadScheduler().registerHandler(loadBalancer));
 		
 		Capture<JEEvent> captured = new Capture<JEEvent>();
 		loadBalancer.handleEvent(EasyMock.capture(captured));
 		
 		loadBalancer.reportRequestFinished(firstRequest);
 		loadBalancer.reportRequestFinished(secondRequest);
 		loadBalancer.reportRequestFinished(thirdRequest);
 		loadBalancer.reportRequestFinished(fourthRequest);
 		loadBalancer.reportRequestFinished(fifthRequest);
 	
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), new MachineDescriptor(1, false, MachineType.M1_LARGE, 0), loadBalancer);
 		machine.sendRequest(firstRequest);
 		machine.sendRequest(secondRequest);
 		machine.sendRequest(thirdRequest);
 		machine.sendRequest(fourthRequest);
 		machine.sendRequest(fifthRequest);
 		machine.sendRequest(sixthRequest);
 		
 		Queue<Request> queue = machine.getProcessorQueue();
 		assertFalse(queue.isEmpty());
 		assertEquals(thirdRequest, queue.poll());
 		
 		
 		Checkpointer.loadScheduler().start();
 		
 		JEEvent event = captured.getValue();
 		assertNotNull(event);
 		assertEquals(JEEventType.REQUESTQUEUED, event.getType());
 		assertEquals(sixthRequest, event.getValue()[0]);
 		
 		assertTrue(machine.getProcessorQueue().isEmpty());
 		
 		assertEquals(250, Checkpointer.loadScheduler().now());
 		
 		EasyMock.verify(loadBalancer);
 	}
 
 	@Test
 	public void testComputeUtilisationOfEmptyMachine(){
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		EasyMock.replay(loadBalancer);
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		assertEquals(Double.NaN, machine.computeUtilisation(scheduler.now()), 0.0001);
 		assertEquals(0, machine.computeUtilisation(scheduler.now() + 300000), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	/**
 	 * As the request finish event has not happened, the time that is an attribute of computeUtilisation
 	 * is used as the computing time
 	 */
 	@Test
 	public void testComputeUtilisationOfMachineWithRunningRequestAndOneCore(){
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.expect(scheduler.registerHandler(EasyMock.isA(TimeSharedMachine.class))).andReturn(1);
 		EasyMock.expect(scheduler.now()).andReturn(0l).times(3);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
 		EasyMock.expect(scheduler.now()).andReturn(100l).times(2);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{150, 150});
 
 		EasyMock.replay(loadBalancer, scheduler);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		assertEquals(Double.NaN, machine.computeUtilisation(0), 0.0001);
 		assertEquals(1, machine.computeUtilisation(50), 0.0001);
 		
 		//Simulating a preemption event
 		machine.handleEvent(new JEEvent(JEEventType.PREEMPTION, machine, 100l, 100l, request));
 		
 		assertEquals(1, machine.computeUtilisation(100), 0.0001);
 		assertEquals(1, machine.computeUtilisation(150), 0.0001);
 		
 		EasyMock.verify(loadBalancer, scheduler);
 	}
 	
 	@Test
 	public void testComputeUtilisationOfMachineWithRunningRequestAndOneCore2(){
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.expect(scheduler.registerHandler(EasyMock.isA(TimeSharedMachine.class))).andReturn(1);
 		EasyMock.expect(scheduler.now()).andReturn(0l).times(3);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
 		EasyMock.expect(scheduler.now()).andReturn(100l).times(2);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
		EasyMock.expect(scheduler.now()).andReturn(150l).times(2);
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		loadBalancer.reportRequestFinished(EasyMock.isA(Request.class));
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{150, 150});
 		
 		EasyMock.replay(loadBalancer, scheduler);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		//Simulating a preemption event
 		machine.handleEvent(new JEEvent(JEEventType.PREEMPTION, machine, 100l, 100l, request));
 		
 		//As a compute utilisation is performed, the next compute utilisation will start from this time!
 		assertEquals(1, machine.computeUtilisation(100), 0.0001);
 		
 		machine.handleEvent(new JEEvent(JEEventType.PREEMPTION, machine, 150l, 50l, request));
 		assertEquals(0.25, machine.computeUtilisation(300), 0.0001);
 		
 		EasyMock.verify(loadBalancer, scheduler);
 	}
 
 	/**
 	 * As the request finish event has not happened, the time that is an attribute of computeUtilisation
 	 * is used as the computing time
 	 */
 	@Test
 	public void testComputeUtilisationOfMachineWithRunningRequestAndDualCore(){
 		
 		JEEventScheduler scheduler = EasyMock.createStrictMock(JEEventScheduler.class);
 		EasyMock.expect(scheduler.registerHandler(EasyMock.isA(TimeSharedMachine.class))).andReturn(1);
 		EasyMock.expect(scheduler.now()).andReturn(0l).times(3);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
 		EasyMock.expect(scheduler.now()).andReturn(100l).times(2);
 		scheduler.queueEvent(EasyMock.isA(JEEvent.class));
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{150, 150, 150});
 
 		EasyMock.replay(loadBalancer, scheduler);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_LARGE, 0), loadBalancer);
 		machine.sendRequest(request);
 		
 		assertEquals(Double.NaN, machine.computeUtilisation(0), 0.0001);
 		assertEquals(0.5, machine.computeUtilisation(50), 0.0001);
 		
 		//Simulating a preemption event
 		machine.handleEvent(new JEEvent(JEEventType.PREEMPTION, machine, 100l, 100l, request));
 		
 		assertEquals(0.5, machine.computeUtilisation(100), 0.0001);
 		assertEquals(0.5, machine.computeUtilisation(150), 0.0001);
 		
 		EasyMock.verify(loadBalancer, scheduler);
 	}
 
 	/**
 	 * As the request finish event has not happened, the time that is an attribute of computeUtilisation
 	 * is used as the computing time
 	 */
 	@Test
 	public void testComputeUtilisationOfMachineWithRunningRequestAndQuadCore(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50, 50, 50, 50});
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_XLARGE, 0), loadBalancer);
 		machine.sendRequest(request);
 		
 		assertEquals(Double.NaN, machine.computeUtilisation(scheduler.now()), 0.0001);
 		assertEquals(0.25, machine.computeUtilisation(scheduler.now() + 50), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 
 	@Test
 	public void testComputeUtilisationOfMachineAsRequestFinishesAndOneCore(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		scheduler.start();
 		
 		assertEquals(1, machine.computeUtilisation(50), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(99), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testComputeUtilisationOfMachineAsRequestFinishesAndQuadCore(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50, 50, 50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_XLARGE, 0), loadBalancer);
 		machine.sendRequest(request);
 
 		scheduler.start();
 		
 		assertEquals(0.25, machine.computeUtilisation(50), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(299), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 
 	@Test
 	public void testComputeUtilisationOfMachineAfterRequestFinishesAndOneCore(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		scheduler.start();
 		
 		//Computing utilisation
 		assertEquals(0.5, machine.computeUtilisation(100), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(150), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testComputeUtilisationOfMachineAfterRequestFinishesAndQuadCore(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50, 50, 50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_XLARGE, 0), loadBalancer);
 		machine.sendRequest(request);
 		
 		scheduler.start();
 		
 		//Computing utilisation
 		assertEquals(0.125, machine.computeUtilisation(100), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(150), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 
 	@Test
 	public void testComputeUtilisationOfMachineAfterRequestFinishesAndOneCore2(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, descriptor, loadBalancer);
 		machine.sendRequest(request);
 		
 		scheduler.start();
 		
 		assertEquals(1.0/3, machine.computeUtilisation(150), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(160), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testComputeUtilisationOfMachineAfterRequestFinishesAndDualCore2(){
 		
 		JEEventScheduler scheduler = Checkpointer.loadScheduler();
 		
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(scheduler, new MachineDescriptor(1, false, MachineType.M1_LARGE, 0), loadBalancer);
 		
 		machine.sendRequest(request);
 		
 		scheduler.start();
 		
 		assertEquals(1.0/6, machine.computeUtilisation(150), 0.0001);
 		//After computing utilization, counters are reseted for next period ...
 		assertEquals(0.0, machine.computeUtilisation(151), 0.0001);
 		
 		EasyMock.verify(loadBalancer);
 	}
 	
 	@Test
 	public void testMachineIsBusyAfterRequestFinishes(){
 		LoadBalancer loadBalancer = EasyMock.createStrictMock(LoadBalancer.class);
 		
 		Request request = new Request(0, 0, 0, 0, 100L, 100000L, new long[]{50, 50, 50});
 		
 		loadBalancer.reportRequestFinished(request);
 		
 		EasyMock.replay(loadBalancer);
 		
 		TimeSharedMachine machine = new TimeSharedMachine(Checkpointer.loadScheduler(), descriptor, loadBalancer);
 		
 		machine.sendRequest(request);
 		
 		assertTrue(machine.isBusy());//Verifying if machine is busy
 		
 		Checkpointer.loadScheduler().start();
 		
 		assertFalse(machine.isBusy());//Verifying if machine is busy
 		
 		EasyMock.verify(loadBalancer);
 	}
 }
