 package ar.edu.itba.pod.legajo51190.impl;
 
 import java.rmi.RemoteException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import junit.framework.Assert;
 
 import org.jgroups.ChannelListener;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import ar.edu.itba.pod.api.Result;
 import ar.edu.itba.pod.api.Signal;
 import ar.edu.itba.pod.signal.source.RandomSource;
 
 public class HighPerformanceSignalProcessorTest {
 
 	private static NodeTestController controller;
 	private final RandomSource src = new RandomSource(12345);
 
 	@Before
 	public void init() throws Exception {
 		if (controller == null) {
 			controller = new NodeTestController(new SignalNodeTestFactory() {
 				@Override
 				public SignalNode getNewSignalNode(final SyncListener listener) {
 					try {
 						Set<ChannelListener> listeners = new HashSet<>();
 						listeners.add(listener);
 						return new CompositeTestableSignalNode(
 								new MultiThreadedDistributedSignalProcessor(1,
 										listeners, listener), listener);
 					} catch (Exception e) {
 						e.printStackTrace();
 						return null;
 					}
 				}
 			});
 		}
 
 	}
 
 	@After
 	public void clear() {
 		controller.disconnectAllNodesFromChannel();
 		controller.getNodesToTest().clear();
 	}
 
 	@Test
	@Ignore
 	public void testMultipleAddConsistency() {
 
 		controller.addNewNodes(2);
 		SignalNode first = controller.getNodesToTest().getFirst();
 		SignalNode last = controller.getNodesToTest().getLast();
 
 		controller.addSignalsToNode(first, 10000);
 
 		try {
 			Signal sig = src.next();
 			Result r1 = first.findSimilarTo(sig);
 			Result r2 = last.findSimilarTo(sig);
 			Assert.assertEquals(r1, r2);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Test
 	public void testDuringAddConsistency() throws InterruptedException {
 
 		final AtomicInteger finishedCount = new AtomicInteger(0);
 
 		controller.addNewNodes(1);
 		final SignalNode first = controller.getNodesToTest().getFirst();
 		final Signal sig = src.next();
 
 		controller.addSignalsToNode(first, 10000);
 
 		CountDownLatch newNodeAwaitLatch = new CountDownLatch(1);
 
 		controller.getListener().setNewNodeLatch(newNodeAwaitLatch);
 
 		SignalNode node = controller.getNewSignalNode();
 		try {
 			node.join("testChannel");
 			controller.getNodesToTest().add(node);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		final SignalNode lastNode = node;
 
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Result last = null;
 				while (!Thread.interrupted()) {
 
 					try {
 						Result newResult = first.findSimilarTo(sig);
 						System.out.println("====== I GOT A RESULT!!!!");
 						if (last != null) {
 							Assert.assertEquals(last, newResult);
 							System.out.println("They are equal");
 						}
 						last = newResult;
 
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				finishedCount.incrementAndGet();
 			}
 		});
 
 		Thread t2 = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Result last = null;
 				while (!Thread.interrupted()) {
 
 					try {
 						Result newResult = lastNode.findSimilarTo(sig);
 						System.out.println("====== I GOT A RESULT!!!!");
 						if (last != null) {
 							Assert.assertEquals(last, newResult);
 							System.out.println("They are equal");
 						}
 						last = newResult;
 
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				finishedCount.incrementAndGet();
 			}
 		});
 
 		t2.start();
 		t.start();
 
 		try {
 			if (!newNodeAwaitLatch.await(120, TimeUnit.SECONDS)) {
 				throw new InterruptedException();
 			}
 		} catch (InterruptedException e) {
 			throw new RuntimeException("Something didn't sync right");
 		} finally {
 			try {
 				Thread.sleep(10 * 1000);
 				controller.addNewNodes(1);
 				Thread.sleep(10 * 1000);
 				controller.addNewNodes(1);
 				Thread.sleep(10 * 1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} finally {
 				t.interrupt();
 				t2.interrupt();
 			}
 		}
 
 		t.join();
 		t2.join();
 		Assert.assertEquals(2, finishedCount.get());
 	}
 }
