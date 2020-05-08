 package com.lazan.acme.slots;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.lazan.acme.slots.internal.BagTotalEventHandler;
 import com.lazan.acme.slots.internal.BagVoltEventHandler;
 import com.lazan.acme.slots.internal.DisruptorCashCounterInputListener;
 import com.lazan.acme.slots.internal.InMemoryCashCounterRepository;
 import com.lazan.acme.slots.internal.SleepingVoltService;
 import com.lazan.acme.slots.internal.UnmatchedBagWorker;
 import com.lmax.disruptor.EventHandler;
 import com.lmax.disruptor.RingBuffer;
 import com.lmax.disruptor.dsl.Disruptor;
 
 public class CashCounterIntegrationTest {
 	private static final int RING_SIZE = 1024 * 1024;
 	private static final long MAX_UNMATCHED_MILLIS = 1000;
 	private CashCounterInputListener inputListener;
 	private Collection<String> unmatchedBagIds;
 	private Map<String, String> bagVolts;
 	private Map<DenominationType, Integer> runningTotals;
 	
 	class MockCashCounterOutputListener implements CashCounterOutputListener {
 		public void bagNotMatched(String bagId, int bagTotal) {
 			unmatchedBagIds.add(bagId);
 		}
 		
 		public void voltAssigned(String bagId, String voltId) {
 			bagVolts.put(bagId,  voltId);
 		}
 		
 		public void runningTotal(DenominationType denominationType, int total) {
 			runningTotals.put(denominationType, total);
 		}
 	}
 
 	@Before
 	public void before() {
 		unmatchedBagIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
 		bagVolts = new ConcurrentHashMap<String, String>();
 		runningTotals = new ConcurrentHashMap<DenominationType, Integer>();
 
 		VoltService voltService = new SleepingVoltService();
 		CashCounterOutputListener outputListener = new MockCashCounterOutputListener();
 		CashCounterRepository repository = new InMemoryCashCounterRepository();
 		EventHandler<InputEvent> bagTotalHandler = new BagTotalEventHandler(repository, outputListener);
 		EventHandler<InputEvent> bagVoltHandler = new BagVoltEventHandler(repository, voltService, outputListener);
 		Runnable unmatchedBagWorker = new UnmatchedBagWorker(MAX_UNMATCHED_MILLIS, repository, outputListener);
 		ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
 		Disruptor<InputEvent> disruptor = new Disruptor<InputEvent>(InputEvent.EVENT_FACTORY, RING_SIZE, executor);
 		disruptor.handleEventsWith(bagTotalHandler).then(bagVoltHandler);
 		executor.scheduleAtFixedRate(unmatchedBagWorker, 0, MAX_UNMATCHED_MILLIS, TimeUnit.MILLISECONDS);
 		RingBuffer<InputEvent> ringBuffer = disruptor.start();
 		
 		inputListener = new DisruptorCashCounterInputListener(ringBuffer, repository);
 	}	
 	
 	@Test
 	public void testMatch() {
 		assertRunningTotals(0, 0);
 		sendBag("B1", Denomination.NOTE_1);
 		sleep(50);
 		assertRunningTotals(0, 100);
 
 		sendBag("B2", Denomination.COIN_10, Denomination.COIN_20);
 		sleep(50);
 		assertRunningTotals(30, 100);
 
 		sendBag("B3", Denomination.COIN_20, Denomination.COIN_50);
 		sleep(50);
 		assertRunningTotals(100, 100);
 		
 		sleep(2000);
 		
 		assertSameVolt("B1", "B2", "B3");
 	}
 	
 	@Test
 	public void testUnmatched() {
 		sendBag("B1", Denomination.NOTE_1);
		sleep(50);
 		assertTrue(unmatchedBagIds.isEmpty());
 		sleep(2000);
 
 		assertEquals(Collections.singleton("B1"), unmatchedBagIds);
 		assertRunningTotals(0, 100);
 	}
 
 	private void assertRunningTotals(int coinTotal, int noteTotal) {
 		Integer actualCoinTotal = runningTotals.get(DenominationType.COIN);
 		Integer actualNoteTotal = runningTotals.get(DenominationType.NOTE);
 		assertEquals("Checking coin total", coinTotal, actualCoinTotal == null ? 0 : actualCoinTotal);
 		assertEquals("Checking note total", noteTotal, actualNoteTotal == null ? 0 : actualNoteTotal);
 	}
 
 	private void assertSameVolt(String... bagIds) {
 		String firstVoltId = null;
 		for (String bagId : bagIds) {
 			String voltId = bagVolts.get(bagId);
 			assertNotNull("Checking volt for bagId " + bagId, voltId);
 			if (firstVoltId == null) {
 				firstVoltId = voltId;
 			} else {
 				assertEquals("Checking volt for bagId " + bagId, firstVoltId, voltId);
 			}
 		}
 	}
 	
 	private void sleep(int millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {}
 	}
 	
 	private void sendBag(String bagId, Denomination... denoms) {
 		inputListener.startBag(bagId);
 		for (Denomination denom : denoms) {
 			inputListener.bagEntry(bagId, denom);
 		}
 		inputListener.endBag(bagId);
 	}
 }
