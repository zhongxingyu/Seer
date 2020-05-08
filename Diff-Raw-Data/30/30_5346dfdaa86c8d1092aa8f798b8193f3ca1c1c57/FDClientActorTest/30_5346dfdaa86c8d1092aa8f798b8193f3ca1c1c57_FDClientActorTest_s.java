 package br.ufmg.dcc.vod.spiderpig.distributed.fd;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.ufmg.dcc.vod.spiderpig.common.ServiceIDUtils;
 import br.ufmg.dcc.vod.spiderpig.common.Tuple;
 import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
 import br.ufmg.dcc.vod.spiderpig.queue.QueueService;
 
 public class FDClientActorTest {
 
 	private ArrayList<QueueService> services;
 
 	@Before
 	public void setUp() {
 		services = new ArrayList<>();
 	}
 	
 	@After
 	public void tearDown() {
 		for (QueueService s : services)
 			s.waitUntilWorkIsDoneAndStop(1);
 	}
 	
 	public Tuple<Set<ServiceID>, Set<MockFDServerActor>> 
 			startToMonitor(int numServers, int basePort, boolean isDown) 
 						throws IOException {
 		RemoteMessageSender sender = new RemoteMessageSender();
 		Set<ServiceID> sids = new HashSet<>();
 		Set<MockFDServerActor> actors = new HashSet<>();
 		for (int i = 0; i < numServers; i++) {
 			QueueService service = new QueueService("localhost", basePort + i);
 			
 			MockFDServerActor fdactor = new MockFDServerActor(sender, isDown);
 			fdactor.withSimpleQueue(service).startProcessors(1);
 			
 			services.add(service);
 			actors.add(fdactor);
 			sids.add(ServiceIDUtils.toResolvedServiceID("localhost", 
 					basePort + i, FDServerActor.HANDLE));
 		}
 		return new Tuple<Set<ServiceID>, Set<MockFDServerActor>>(sids, actors);
 	}
 	
 	private class MockFDServerActor extends FDServerActor {
 
 		private AtomicBoolean down;
 
 		public MockFDServerActor(RemoteMessageSender sender, boolean isDown) {
 			super(sender);
 			this.down = new AtomicBoolean(isDown);
 		}
 		
 		@Override
 		public void process(PingPong t) {
 			if (!this.down.get()) {
 				super.process(t);
 			}
 		}
 		
 		public void setDown() {
 			this.down.set(true);
 		}
 		
 		public void setUp() {
 			this.down.set(false);
 		}
 
 		public void changeID() {
 			PingPong build = PingPong.newBuilder().mergeFrom(this.msg) 
 				.setSessionID(this.msg.getSessionID() + 1)
 				.build();
 			this.msg = build;
 		}
 	}
 	
 	private class Listener implements FDListener {
 
 		private final CountDownLatch upLatch;
 		private final CountDownLatch downLatch;
 
 		final List<ServiceID> ups = 
 				Collections.synchronizedList(new ArrayList<ServiceID>());
 
 		final List<ServiceID> downs = 
 				Collections.synchronizedList(new ArrayList<ServiceID>());
 		
 		public Listener(CountDownLatch upLatch, CountDownLatch downLatch) {
 			this.upLatch = upLatch;
 			this.downLatch = downLatch;
 		}
 		
 		@Override
 		public void isUp(ServiceID serviceID) {
 			ups.add(serviceID);
 			this.upLatch.countDown();
 		}
 
 		@Override
 		public void isSuspected(ServiceID serviceID) {
 			downs.add(serviceID);
 			this.downLatch.countDown();
 		}
 		
 	}
 	
 	@Test
 	public void testUpOne() throws IOException, InterruptedException {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(1, 5000, false);
 		Set<ServiceID> toMonitor = tuple.first;
 		
 		CountDownLatch upLatch = new CountDownLatch(1);
 		CountDownLatch downLatch = new CountDownLatch(0);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4000);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
 		upLatch.await();
 		downLatch.await();
 		
 		Assert.assertEquals(1, listener.ups.size());
 		Assert.assertTrue(toMonitor.containsAll(listener.ups));
 		service.waitUntilWorkIsDone(1);
 		actor.stopTimer();
 	}
 
 	@Test
 	public void testUp100() throws Exception {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(100, 6000, false);
 		Set<ServiceID> toMonitor = tuple.first;
 		
 		CountDownLatch upLatch = new CountDownLatch(100);
 		CountDownLatch downLatch = new CountDownLatch(0);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4001);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
 		
 		upLatch.await();
 		downLatch.await();
 		
 		Assert.assertEquals(100, listener.ups.size());
 		Assert.assertTrue(toMonitor.containsAll(listener.ups));
 		
 		service.waitUntilWorkIsDone(1);
 		actor.stopTimer();
 	}
 	
 	@Test
 	public void testUpDown100() throws Exception {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(100, 7000, false);
 		Set<ServiceID> toMonitor = tuple.first;
 		
 		CountDownLatch upLatch = new CountDownLatch(100);
 		CountDownLatch downLatch = new CountDownLatch(100);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4002);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
		
 		upLatch.await();
		
 		Assert.assertEquals(100, listener.ups.size());
 		Assert.assertTrue(toMonitor.containsAll(listener.ups));
 		
 		for (MockFDServerActor fdax : tuple.second)
 			fdax.setDown();
 		
 		downLatch.await();
 		
 		Assert.assertEquals(100, listener.downs.size());
 		Assert.assertTrue(toMonitor.containsAll(listener.downs));
 		
 		service.waitUntilWorkIsDone(1);
 		actor.stopTimer();
 	}
 	
 	@Test
 	public void testUpDownUp100() throws Exception {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(100, 8000, false);
 		Set<ServiceID> toMonitor = tuple.first;
 		
 		CountDownLatch upLatch = new CountDownLatch(200);
 		CountDownLatch downLatch = new CountDownLatch(100);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4003);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
 		
 		while (upLatch.getCount() > 100);
 		
 		Assert.assertEquals(100, upLatch.getCount());
 		Assert.assertEquals(100, listener.ups.size());
 		
 		for (MockFDServerActor fdax : tuple.second)
 			fdax.setDown();
 		
 		downLatch.await();
 		
 		Assert.assertEquals(100, listener.downs.size());
 		Assert.assertTrue(toMonitor.containsAll(listener.downs));
 
 		for (MockFDServerActor fdax : tuple.second)
 			fdax.setUp();
 		
 		upLatch.await();
 		
 		Assert.assertEquals(200, listener.ups.size());
 		Assert.assertEquals(100, listener.downs.size());
 		
 		service.waitUntilWorkIsDone(1);
 		actor.stopTimer();
 	}
 	
 	@Test
 	public void testUp() throws Exception {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(1, 9000, false);
 		Set<ServiceID> toMonitor = tuple.first;
 		
 		CountDownLatch upLatch = new CountDownLatch(2);
 		CountDownLatch downLatch = new CountDownLatch(1);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4004);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
 		
 		while (upLatch.getCount() > 1);
 		Assert.assertEquals(1, upLatch.getCount());
 		Assert.assertEquals(1, listener.ups.size());
 
 		tuple.second.iterator().next().changeID();
 		
 		downLatch.await();	
 		upLatch.await();
 		
 		Assert.assertEquals(2, listener.ups.size());
 		Assert.assertEquals(1, listener.downs.size());
 		
 		service.waitUntilWorkIsDone(1);
 		actor.stopTimer();
 	}
 	
 	@Test
 	public void testInitialDownMsg() throws Exception {
 		Tuple<Set<ServiceID>, Set<MockFDServerActor>> tuple = 
 				startToMonitor(1, 2000, true);
 		
 		Set<ServiceID> toMonitor = tuple.first;
 		CountDownLatch upLatch = new CountDownLatch(0);
 		CountDownLatch downLatch = new CountDownLatch(1);
 		
 		Listener listener = new Listener(upLatch, downLatch);
 		FDClientActor actor = new FDClientActor(5, 1, TimeUnit.SECONDS, 
 				listener, new RemoteMessageSender());
 		QueueService service = new QueueService("localhost", 4005);
 		actor.withSimpleQueue(service).startProcessors(1);
 		for (ServiceID serviceID : toMonitor)
 			actor.watch(serviceID);
 		
 		actor.startTimer();
 		
 		upLatch.await();
 		downLatch.await();	
 		
 		Assert.assertEquals(1, listener.downs.size());
 	}
 }
