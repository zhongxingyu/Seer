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
 
 package net.sf.okapi.common.pipeline;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.MemMappedCharSequence;
 
 public class Pipeline implements IPipeline {
 	List<IPipelineStep> steps;
 	IPipelineStep initialStep;
 	boolean cancel = false;
 	boolean first = true;
 	boolean startEventSent = false;
 
 	public Pipeline() {
 		steps = new ArrayList<IPipelineStep>();
 		initialize();
 	}
 
 	private void initialize() {
 		cancel = false;
 		first = true;
 	}
 
 	public void addStep(IPipelineStep step) {
 		if (first) {
			if (!(step instanceof IInitialStep)) {
 				throw new RuntimeException("Intial step must implement IInitialStep");
 			}
 			initialStep = step;
 			first = false;
 		} else {
 			steps.add(step);
 		}
 	}
 
 	public void cancel() {
 		// TODO handle cancel - do we put execute on its own thread?
 		cancel = true;
 	}
 
 	private void execute() {
 		initialize();
 
 		// START event is only sent once during the lifecycle of the pipeline
 		// the pipeline produces this event on behalf of the intial step
 		if (!startEventSent) {
 			startEventSent = true;
 			Event event = new Event(EventType.START);			
 			for (IPipelineStep step : steps) {
 				step.handleEvent(event);
 			}
 		}
 		// loop through the events until we run out
 		while (((IInitialStep) initialStep).hasNext()) {
 			Event event = initialStep.handleEvent(null);
 			for (IPipelineStep step : steps) {
 				step.handleEvent(event);
 			}
 		}
 	}
 
 	public PipelineReturnValue getState() {
 		if (cancel)
 			return PipelineReturnValue.CANCELLED;
 		
 		return PipelineReturnValue.SUCCEDED;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#process(java.net.URI)
 	 */
 	public void process(URI input) {
 		((IInitialStep) initialStep).setInput(input);
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#process(java.io.InputStream)
 	 */
 	public void process(InputStream input) {
 		((IInitialStep) initialStep).setInput(input);
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sf.okapi.common.pipeline.IPipeline#process(net.sf.okapi.common.
 	 * MemMappedCharSequence)
 	 */
 	public void process(MemMappedCharSequence input) {
 		((IInitialStep) initialStep).setInput(input);
 		execute();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seenet.sf.okapi.common.pipeline.IPipeline#process(CharSequence)
 	 */
 	public void process(CharSequence input) {
 		((IInitialStep) initialStep).setInput(input);
 		execute();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#close()
 	 */
 	public void close() {
 		// send FINISH event
 		Event event = new Event(EventType.FINISHED);		
 		for (IPipelineStep step : steps) {
 			step.handleEvent(event);
 		}
 
 		initialStep.close();
 		for (IPipelineStep step : steps) {
 			step.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#postprocess()
 	 */
 	public void postprocess() {
 		initialStep.postprocess();
 		for (IPipelineStep step : steps) {
 			step.postprocess();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.pipeline.IPipeline#preprocess()
 	 */
 	public void preprocess() {
 		initialStep.preprocess();
 		for (IPipelineStep step : steps) {
 			step.preprocess();
 		}
 	}
 }
