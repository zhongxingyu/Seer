 /*
  * Original Code Copyright Prime Technology.
  * Subsequent Code Modifications Copyright 2011-2012 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 
 /*
  * Generated, Do Not Modify
  */
 
 package org.icefaces.ace.component.tooltip;
 
 import javax.faces.context.FacesContext;
 import javax.faces.component.UINamingContainer;
 import javax.el.ValueExpression;
 import javax.el.MethodExpression;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import java.util.List;
 import java.util.ArrayList;
import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.AbortProcessingException;
 
 public class Tooltip extends TooltipBase{
 
 	private static final String OPTIMIZED_PACKAGE = "org.icefaces.ace.component.";
 
 	protected FacesContext getFacesContext() {
 		return FacesContext.getCurrentInstance();
 	}
 	
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         super.broadcast(event);
		if (event instanceof ActionEvent) {
			MethodExpression displayListener = getDisplayListener();
			if (displayListener != null) {
				displayListener.invoke(getFacesContext().getELContext(), null);
			}
 		}
 	}
 }
