 /*===========================================================================*/
 /* Copyright (C) 2008 by the Okapi Framework contributors                    */
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
 /* Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA               */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.common.threadedpipeline;
 
 import java.util.concurrent.BlockingQueue;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.pipeline.PipelineReturnValue;
 
 public class ProducerConsumerPipelineStepAdaptor extends BaseThreadedPipelineStepAdaptor implements IProducer,
 		IConsumer {
 	private BlockingQueue<Event> producerQueue;
 	private BlockingQueue<Event> consumerQueue;
 
 	public ProducerConsumerPipelineStepAdaptor(IPipelineStep step) {
 		super(step);
 	}
 
 	public void setConsumerQueue(BlockingQueue<Event> consumerQueue) {
 		this.consumerQueue = consumerQueue;
 	}
 
 	public void setProducerQueue(BlockingQueue<Event> producerQueue) {
 		this.producerQueue = producerQueue;
 	}
 
 	protected void addToQueue(Event event) {
 		if (producerQueue == null) {
 			throw new RuntimeException("This class is a consumer not a producer");
 		}
 		try {
 			producerQueue.put(event);
 		} catch (InterruptedException e) {
 			throw new RuntimeInterruptedException(e);
 		}
 	}
 
 	protected Event takeFromQueue() {
 		if (consumerQueue == null) {
 			throw new RuntimeException("This class is a producer not a consumer");
 		}
 
 		Event event;
 		try {
 			event = consumerQueue.take();
 		} catch (InterruptedException e) {
 			throw new RuntimeInterruptedException(e);
 		}
 		return event;
 	}
 
 	public Event handleEvent(Event event) {
 		Event e;
 		e = takeFromQueue();
 		step.handleEvent(e);
 		addToQueue(e);
 		return e;
 	}
 
 	public String getName() {
 		return null;
 	}
 
 	@Override
 	protected PipelineReturnValue processBlockingQueue() {
 		Event event = handleEvent(null);
		if (event == null) {
 			return PipelineReturnValue.SUCCEDED;
 		}
 		return PipelineReturnValue.RUNNING;
 	}
 }
