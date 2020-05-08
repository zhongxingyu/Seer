 /**********************************************************************************
  * $URL$
  * $Id$
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
 
 package org.sakaiproject.assignment2.tool.params;
 
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 
 /**
  * View parameters for going to the end page of the helper.
  * 
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class FinishedHelperViewParameters extends SimpleViewParameters {
 
    /**
     * The name of the gradebook item (most likely just created)
     */
    public String gbItemName;
    
    /**
     * The time that the gradebook item is due. "" indicates no due date.
	 * This value will be the # milliseconds since January 1, 1970 00:00:00 GMT
     */
    public String gbItemDueTime;
 
     public FinishedHelperViewParameters() {}
 
    public FinishedHelperViewParameters(String viewId, String gbItemName, String gbItemDueTime){
         super(viewId);
        this.gbItemName = gbItemName;
        this.gbItemDueTime = gbItemDueTime;
     }
 }
