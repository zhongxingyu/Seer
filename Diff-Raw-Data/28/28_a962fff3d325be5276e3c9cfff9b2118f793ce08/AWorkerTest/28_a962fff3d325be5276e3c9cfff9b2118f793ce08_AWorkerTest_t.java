 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.core.threading;
 
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
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
 	
 	public void testWorkerTriggeringPoolClosed() throws InterruptedException {
 		final ICommand command = mock(ICommand.class);
 		final DummyWorker worker = new DummyWorker();
 		
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
 	
 	public void testWorkerTriggeringFilteredCommandPoolClosed() throws InterruptedException {
 		final ICommand command = mock(ICommand.class);
 		final DummyWorker worker = new DummyWorker();
 		
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
 		final DummyWorker worker = new DummyWorker();
 		final Semaphore waitforReturnToPool = new Semaphore(0);
 		
 		checking(new Expectations(){{
 			// allow enqueuing and dequeueing of exactly one command
 			one(inQueue).dequeue(); will(returnValue(command));
 			one(outQueue).enqueue(with(same(command)));
 			
 			// pool is not closed
 			one(pool).closed(); will(returnValue(false));
 			
 			// worker must return itself into pool
 			one(pool).returnWorker(with(same(worker)));
 			will(new Action(){
 				public void describeTo(Description arg0) {}
 
 				public Object invoke(Invocation invocation) throws Throwable {
 					waitforReturnToPool.release();
 					return null;
 				}
 				
 			});
 			
 			one(pool).invalidateWorker(with(same(worker)));
 		}});
 		
 		// init worker
 		worker.setInQueue(this.inQueue);
 		worker.setOutQueue(this.outQueue);
 		worker.setPool(this.pool);
 		
 		// trigger worker to dequeue and process new command
 		worker.trigger();		
 		
 		// wait until worker has returned itself into pool
 		assertTrue(waitforReturnToPool.tryAcquire(5, TimeUnit.SECONDS));
 		
 		// terminate worker
 		worker.terminate();
 		assertTrue(worker.commandProcessed);
 	}
 }
 
 class DummyWorker extends AWorker<ICommand> {
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	public boolean commandProcessed = false;
 	
 	@Override
 	protected void execute(ICommand cmd) {
 		this.logger.info("Dummy processing of command ...");
 		this.commandProcessed = true;
 	}	
 }
