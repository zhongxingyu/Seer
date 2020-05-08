 package org.paxle.core.threading;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hamcrest.Description;
 import org.jmock.Expectations;
 import org.jmock.api.Action;
 import org.jmock.api.Invocation;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.IInputQueue;
 import org.paxle.core.queue.IOutputQueue;
 
 public class AWorkerTest extends MockObjectTestCase {
 	
 	private IPool<ICommand> pool = null; 
 	private IInputQueue<ICommand> inQueue = null;
 	private IOutputQueue<ICommand> outQueue = null;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		this.pool = mock(IPool.class);
 		this.inQueue = mock(IInputQueue.class);
 		this.outQueue = mock(IOutputQueue.class);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testWorkerTriggeringPoolClosed() throws InterruptedException {
 		final ICommand command = mock(ICommand.class);
		final TestWorker worker = new TestWorker();
 		
 		checking(new Expectations(){{
 			// allow enqueuing and dequeueing of exactly one command
 			one(inQueue).dequeue(); will(returnValue(command));
 			one(outQueue).enqueue(with(same(command)));
 			
 			// pool is closed
 			one(pool).closed(); will(returnValue(true));
 			
 			// worker must invalidate itself
 			one(pool).invalidateWorker(with(same(worker)));
 		}});
 		
 		worker.setInQueue(this.inQueue);
 		worker.setOutQueue(this.outQueue);
 		worker.setPool(this.pool);
 		
 		// trigger worker to dequeue and process new command
 		worker.trigger();		
 				
 		worker.join();
 		assertTrue(worker.commandProcessed);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testWorkerTriggeringFilteredCommandPoolClosed() throws InterruptedException {
 		final ICommand command = mock(ICommand.class);
		final TestWorker worker = new TestWorker();
 		
 		checking(new Expectations(){{
 			// allow dequeueing of exactly one command
 			one(inQueue).dequeue(); will(returnValue(null));	
 			
 			// enqueue must not be called because command was filtered
 			never(outQueue).enqueue(with(same(command)));
 			
 			// pool is closed
 			one(pool).closed(); will(returnValue(true));
 			
 			// worker must invalidate itself
 			one(pool).invalidateWorker(with(same(worker)));
 		}});
 		
 		worker.setInQueue(this.inQueue);
 		worker.setOutQueue(this.outQueue);
 		worker.setPool(this.pool);
 		
 		// trigger worker to dequeue and process new command
 		worker.trigger();		
 		
 		worker.join();
 		assertFalse(worker.commandProcessed);
 	}	
 	
 	public void testWorkerTriggeredAndReturnToPool() throws InterruptedException {
 		final ICommand command = mock(ICommand.class);
		final TestWorker worker = new TestWorker();
 		final WaitForReturnToPool waitforReturnToPool = new WaitForReturnToPool();
 		
 		checking(new Expectations(){{
 			// allow enqueuing and dequeueing of exactly one command
 			one(inQueue).dequeue(); will(returnValue(command));
 			one(outQueue).enqueue(with(same(command)));
 			
 			// pool is not closed
 			one(pool).closed(); will(returnValue(false));
 			
 			// worker must return itself into pool
 			one(pool).returnWorker(with(same(worker)));
 			will(waitforReturnToPool);
 			
 			one(pool).invalidateWorker(with(same(worker)));
 		}});
 		
 		// init worker
 		worker.setInQueue(this.inQueue);
 		worker.setOutQueue(this.outQueue);
 		worker.setPool(this.pool);
 		
 		// trigger worker to dequeue and process new command
 		worker.trigger();		
 		
 		// wait until worker has returned itself into pool
 		waitforReturnToPool.waitForWorker();
 		
 		// terminate worker
 		worker.terminate();
 		assertTrue(worker.commandProcessed);
 	}
 }
 
class TestWorker extends AWorker<ICommand> {
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	public boolean commandProcessed = false;
 	
 	@Override
 	protected void execute(ICommand cmd) {
 		this.logger.info("Dummy processing of command ...");
 		this.commandProcessed = true;
 	}	
 }
 
 class WaitForReturnToPool implements Action {
 	private boolean returned = false;
 
 	public synchronized void waitForWorker() throws InterruptedException {
 		if (!returned) this.wait(1000);
 		if (!returned) throw new IllegalStateException("Worker never returned!");
 	}
 	
 	public void describeTo(Description arg0) {}
 
 	public synchronized Object invoke(Invocation invocation) throws Throwable {
 		this.returned = true;
 		this.notifyAll();
 		return null;
 	}
 	
 }
