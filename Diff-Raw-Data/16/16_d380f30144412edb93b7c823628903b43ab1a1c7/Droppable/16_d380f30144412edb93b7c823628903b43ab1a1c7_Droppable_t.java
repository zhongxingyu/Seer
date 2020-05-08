 /*
  * Original Code developed and contributed by Prime Technology.
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
 
 package org.icefaces.ace.component.dnd;
 
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.FacesContext;
 import javax.faces.component.UINamingContainer;
 import javax.el.ValueExpression;
 import javax.el.MethodExpression;
 import javax.faces.event.AbortProcessingException;
 import java.util.List;
 import java.util.ArrayList;
import org.icefaces.ace.event.DragDropEvent;
 
 public class Droppable extends DroppableBase {
 	@Override
     public void broadcast(javax.faces.event.FacesEvent event) throws AbortProcessingException {
 		super.broadcast(event);
 
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		MethodExpression me = getDropListener();
 
		if (me != null && event instanceof DragDropEvent) {
 			me.invoke(facesContext.getELContext(), new Object[] {event});
 		}
 	}
 }
