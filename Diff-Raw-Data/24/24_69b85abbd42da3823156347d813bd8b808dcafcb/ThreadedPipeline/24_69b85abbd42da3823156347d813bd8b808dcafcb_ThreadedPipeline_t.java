 /*===========================================================================*/
 /* Copyright (C) 2008 Jim Hargrave                                           */
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA              */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.common.threadedpipeline;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.MemMappedCharSequence;
 import net.sf.okapi.common.pipeline.IInitialStep;
 import net.sf.okapi.common.pipeline.IPipeline;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.pipeline.PipelineReturnValue;
 
 public class ThreadedPipeline implements IPipeline {
 	private static final int DEFAULT_BLOCKING_QUEUE_SIZE = 50;
 
 	private final PausableThreadPoolExecutor executor;
 	private final CompletionService<PipelineReturnValue> completionService;
 	private final int blockingQueueSize;
 	private int totalThreads;
 	private PipelineReturnValue state;
 	private LinkedList<IPipelineStep> threadedSteps;
 	private LinkedList<IPipelineStep> nonThreadedSteps;
 	private IInitialStep initalStep;
 
 	private BlockingQueue<Event> previousQueue;
 
 	public ThreadedPipeline() {
 		this(PausableThreadPoolExecutor.newCachedThreadPool(), DEFAULT_BLOCKING_QUEUE_SIZE);
 	}
 
 	public ThreadedPipeline(PausableThreadPoolExecutor executor, int blockingQueueSize) {
 		totalThreads = 0;
 		this.executor = executor;
 		this.blockingQueueSize = blockingQueueSize;
 		this.completionService = new ExecutorCompletionService<PipelineReturnValue>(this.executor);
 		this.executor.pause();
 		state = PipelineReturnValue.PAUSED;
 		threadedSteps = new LinkedList<IPipelineStep>();
 		nonThreadedSteps = new LinkedList<IPipelineStep>();
 	}
 
 	public void execute() {
 		BlockingQueue<Event> queue = null;
 		for (IPipelineStep step : nonThreadedSteps) {
 			if (threadedSteps.isEmpty()) {
 				// first step is a producer wrap it with threaded adaptor
 				queue = new ArrayBlockingQueue<Event>(blockingQueueSize, false);
 				ProducerPipelineStepAdaptor producerStep = new ProducerPipelineStepAdaptor(step);
				producerStep.setProducerQueue(queue);				
 				completionService.submit(producerStep);
 				threadedSteps.add(producerStep);
 				initalStep = (IInitialStep) producerStep;
 			} else if (step == nonThreadedSteps.getLast()) {
 				// last step it is a consumer
 				if (previousQueue == null) {
 					throw new RuntimeException("Previous queue should not be null");
 				}
 				ConsumerPipelineStepAdaptor consumerStep = new ConsumerPipelineStepAdaptor(step);
 				consumerStep.setConsumerQueue(previousQueue);
 				completionService.submit(consumerStep);
 				threadedSteps.add(consumerStep);
 			} else {
 				// this is a middle step it is a Producer and Consumer
 				if (previousQueue == null) {
 					throw new RuntimeException("Previous queue should not be null");
 				}
 				ProducerConsumerPipelineStepAdaptor producerConsumerStep = new ProducerConsumerPipelineStepAdaptor(step);
 				queue = new ArrayBlockingQueue<Event>(blockingQueueSize, false);
 				producerConsumerStep.setProducerQueue(queue);
 				producerConsumerStep.setConsumerQueue(previousQueue);
 				completionService.submit(producerConsumerStep);
 				threadedSteps.add(producerConsumerStep);
 			}
 			totalThreads += 1;
 			previousQueue = queue;
 		}
 
 		executor.resume();
 		state = PipelineReturnValue.RUNNING;
 	}
 
 	public void addStep(IPipelineStep step) {
 		nonThreadedSteps.add(step);
 	}
 
 	public void cancel() {
 		executor.pause();
 		executor.shutdownNow();
 		state = PipelineReturnValue.CANCELLED;
 	}
 
 	/*
 	 * TODO: Do we want pause and resume at the pipeline level? Or should steps
 	 * handle this individually?
 	 * 
 	 * public void pause() { executor.pause(); for (IPipelineStep step :
 	 * threadedSteps) { step.pause(); } state = PipelineReturnValue.PAUSED; }
 	 * 
 	 * public void resume() { executor.resume(); for (IPipelineStep step :
 	 * threadedSteps) { step.resume(); } state = PipelineReturnValue.RUNNING; }
 	 */
 
 	public PipelineReturnValue getState() {
 		if (state == PipelineReturnValue.CANCELLED) {
 			return PipelineReturnValue.CANCELLED;
 		}
 
 		if (state == PipelineReturnValue.PAUSED) {
 			try {
 				// TODO: will this interfere with SWT GUI thread?
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 			}
 			return PipelineReturnValue.PAUSED;
 		}
 
 		try {
 			Future<PipelineReturnValue> f = completionService.poll(100, TimeUnit.MILLISECONDS);
 			if (f != null) {
 				--totalThreads;
 				PipelineReturnValue result = f.get();
 				if (result != PipelineReturnValue.SUCCEDED) {
 					return PipelineReturnValue.FAILED;
 				}
 				return PipelineReturnValue.RUNNING;
 			}
 			if (totalThreads <= 0) {
 				return PipelineReturnValue.SUCCEDED;
 			}
 			return PipelineReturnValue.RUNNING;
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			return PipelineReturnValue.INTERRUPTED;
 		} catch (ExecutionException e) {
 			return PipelineReturnValue.FAILED;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#close()
 	 */
 	public void destroy() {
 		executor.pause();
 		for (IPipelineStep step : threadedSteps) {
 			step.destroy();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#postprocess()
 	 */
 	public void postprocess() {
 		executor.pause();
 		for (IPipelineStep step : threadedSteps) {
 			step.postprocess();
 		}
 		executor.resume();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#preprocess()
 	 */
 	public void preprocess() {
 		executor.pause();
 		for (IPipelineStep step : threadedSteps) {
 			step.preprocess();
 		}
 		executor.resume();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#process(java.net.URI)
 	 */
 	public void process(URI input) {
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#process(java.io.InputStream)
 	 */
 	public void process(InputStream input) {
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sf.okapi.common.pipeline.IPipeline#process(net.sf.okapi.common.
 	 * MemMappedCharSequence)
 	 */
 	public void process(MemMappedCharSequence input) {
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sf.okapi.common.pipeline.IPipeline#process(java.lang.CharSequence)
 	 */
 	public void process(CharSequence input) {
 		execute();
 	}
 }
