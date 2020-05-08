 /*
  * Copyright 2005-7 Pi4 Technologies Ltd
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * Change History:
  * Feb 16, 2007 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.dnd;
 
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
 import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.dnd.DropTargetEvent;
 import org.savara.tools.scenario.designer.model.ModelCreationFactory;
 
 /**
  * This class implements the template transfer drop target listener
  * for the scenario.
  */
 public class ScenarioTemplateTransferDropTargetListener
 			extends TemplateTransferDropTargetListener {
 
     /**
      * Creates a new ScenarioTemplateTransferDropTargetListener instance.
      * @param viewer
      */
     public ScenarioTemplateTransferDropTargetListener(EditPartViewer viewer) {
         super(viewer);
     }
    
    public void drop(DropTargetEvent event) {   
    	// Deselect, to avoid refresh problems in ScenarioEditorPage
    	getViewer().deselectAll();
    	
    	super.drop(event);
    }
 
     /* (non-Javadoc)
      * @see org.eclipse.gef.dnd.TemplateTransferDropTargetListener#getFactory(java.lang.Object)
      */
     protected CreationFactory getFactory(Object template) {
     	CreationFactory ret=null;
     	
     	if (template instanceof Class) {
     		ret = new ModelCreationFactory((Class<?>)template);
     	} else {
     		ret = new ModelCreationFactory(template.getClass());
     	}
     
         return(ret);
     }
 }
