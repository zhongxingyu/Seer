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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 
 public class Pipeline implements IPipeline {
 	List<IPipelineStep> steps;
 	IPipelineStep initialStep;
 	boolean cancel = false;
 	boolean pause = false;
 	boolean stop = false;
 	boolean first = true;
 
 	public Pipeline() {
 		steps = new ArrayList<IPipelineStep>();
 	}
 
 	public void addStep(IPipelineStep step) {
 		if (first) {
 			initialStep = step;
 			first = false;
 		} else {
 			steps.add(step);
 		}
 	}
 
 	public void cancel() {
 		cancel = true;
 	}
 
 	public void execute() {
 		// send intial start event
		Event startEvent = new Event(EventType.START);		
 		for (IPipelineStep step : steps) {
 			step.handleEvent(startEvent);
 		}
 		
 		while (!stop) {
 			if (pause)
 				continue;
 			Event event = initialStep.handleEvent(null);
 			if (event != null) {
 				for (IPipelineStep step : steps) {
 					step.handleEvent(event);
 				}
 	
 			} else stop = true;			
 		}
 		
 		Event finishEvent = new Event(EventType.FINISHED);
 		initialStep.handleEvent(finishEvent);
 		for (IPipelineStep step : steps) {
 			step.handleEvent(finishEvent);
 		}
 	}
 
 	public PipelineReturnValue getState() {
 		return null;
 	}
 
 	public void pause() {
 		pause = true;
 	}
 
 	public void resume() {
 		pause = false;
 	}
 }
