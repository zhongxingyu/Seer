 /**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/tool/src/java/org/sakaiproject/assignment2/tool/producers/RemoveAssignmentConfirmProducer.java $
  * $Id: $
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.producers;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 
 /**
  * This is responsible for rendering the Remove Assignments Confirmation
  * page/dialog.
  * 
  * @author sgithens
  */
 public class RemoveAssignmentConfirmProducer implements ViewComponentProducer,
 ViewParamsReporter {
     public static final String VIEW_ID = "remove-assignment-confirm";
 
     public void fillComponents(UIContainer tofill, ViewParameters viewparams,
             ComponentChecker checker) {
         
         
     }
 
     public String getViewID() {
         return VIEW_ID;
     }
 
     public ViewParameters getViewParameters() {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
