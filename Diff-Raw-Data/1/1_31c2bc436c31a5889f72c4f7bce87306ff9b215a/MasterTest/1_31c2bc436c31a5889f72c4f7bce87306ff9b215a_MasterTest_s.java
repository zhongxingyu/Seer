 package org.paxle.core.threading.impl;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.IOutputQueue;
 import org.paxle.core.queue.impl.FilterInputQueue;
 import org.paxle.core.threading.AWorker;
 import org.paxle.core.threading.IPool;
import org.paxle.core.threading.IWorker;
 
 public class MasterTest extends MockObjectTestCase {
 
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	public void testTriggerWorker() throws Exception {
 		// init needed mocks
 		final ICommand command = mock(ICommand.class);
 		final IPool<ICommand> pool = mock(IPool.class);
 		final FilterInputQueue<ICommand> inQueue = new FilterInputQueue<ICommand>(8);
 		final IOutputQueue<ICommand> outQueue = mock(IOutputQueue.class);
 		final TestWorker worker = new TestWorker(true);
 		worker.setInQueue(inQueue);
 		worker.setOutQueue(outQueue);
 		
 		// define expectations
 		checking(new Expectations(){{
 			// allow the master to fetch a worker 
 			one(pool).getWorker(); will(returnValue(worker));
 			one(pool).close();
 			
 			// allow the worker to enqueue the processed command into the out-queue
 			allowing(command).getResult(); will(returnValue(ICommand.Result.Passed));
 			one(outQueue).enqueue(with(same(command)));
 		}});
 		
 		// init and start master
 		final Master master = new Master<ICommand>(pool, inQueue, true);
 		
 		// enqueue a command
 		inQueue.putData(command);
 		
 		// wait until the worker was triggered
 		worker.wasTriggered();
 		
 		// terminate master
 		master.terminate();
 	}
 }
 
 class TestWorker extends AWorker<ICommand> {
 	private boolean triggerMode = true;
 	
 	private Object triggerSync = new Object();
 	private boolean wasTriggered = false;
 	
 	public TestWorker(boolean useTriggerMode) {
 		this.triggerMode = useTriggerMode;
 	}
 	
 	@Override
 	public void assign(ICommand cmd) {
 		if (this.triggerMode) {
 			throw new RuntimeException("This function must not be called in this testcase.");
 		} else {
 			super.assign(cmd);
 		}
 	}
 	
 	public void wasTriggered() throws InterruptedException {
 		synchronized (triggerSync) {
 			if (!this.wasTriggered) triggerSync.wait(1000);
 			if (!this.wasTriggered) throw new IllegalStateException("Worker was not triggered!");
 		}
 	}
 	
 	@Override
 	public void trigger() throws InterruptedException {
 		super.trigger();
 		synchronized(triggerSync) {
 			this.wasTriggered = true;
 			triggerSync.notify();		
 		}
 	}
 
 	@Override
 	protected void execute(ICommand cmd) {
 		// nothing to do here
 	}
 	
 }
