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
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.filters.IFilter;
 
 public class FilterPipelineStepAdaptor extends BasePipelineStep {
 	private IFilter filter;
 	
 	public FilterPipelineStepAdaptor(IFilter filter) {
 		this.filter = filter;
 	}
 	
 	public IFilter getFilter() {
 		return filter;
 	}
 	
 	public String getName() {		
 		return filter.getName();
 	}		
 
 	@Override
 	public Event handleEvent(Event event) {
		if (( event != null ) && 
			( event.getEventType() == EventType.START || event.getEventType() == EventType.FINISHED )) {
 			return super.handleEvent(event);
 		}
		//TODO: What about hasNext()???
 		return filter.next();
 	}
 	
 	public void preprocess() {}
 
 	public void postprocess() {
 		filter.close();
 	}
 	
 	public void cancel() {
 		filter.cancel();
 	}
 
 	public void pause() {
 	}
 
 	public void resume() {
 	}
 }
