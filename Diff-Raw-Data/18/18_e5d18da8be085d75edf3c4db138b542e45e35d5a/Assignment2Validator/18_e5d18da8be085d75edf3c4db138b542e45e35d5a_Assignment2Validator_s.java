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
 
 package org.sakaiproject.assignment2.tool.beans;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 public class Assignment2Validator  {
 	
 	public boolean validate(Assignment2 assignment, TargettedMessageList messages) {
 	  
 		boolean valid = true;
 		String key = "";
 		if (assignment.getId() == null){
 			key = "new 1";
 		} else {
 			key = assignment.getId().toString();
 		}
 		
 		
 		//check for empty title
 		if (assignment.getTitle() == null || assignment.getTitle().equals("")) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_title_empty",
 					new Object[] { assignment.getTitle() }, "Assignment2." + key + ".title"));
 			valid = false;
 		}
 		
 		//check for graded but no gradable object id
 		if (assignment.isGraded() && (assignment.getGradableObjectId() == null || 
				assignment.getGradableObjectId().equals("") || assignment.getGradableObjectId().longValue() < 1)) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_graded_no_gb_item", 
 					new Object[] {}, "Assignment2."+ key + ".gradableObjectId"));
 			valid = false;
 		}
 		
 		// check for due date after open date
 		if (assignment.getDueDate() != null && assignment.getOpenDate() != null
 				&& assignment.getDueDate().before(assignment.getOpenDate())) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_due_before_open"));
 			valid = false;
 		}
 		
 		if (assignment.getAcceptUntilDate() != null && assignment.getOpenDate() != null
 				&& assignment.getAcceptUntilDate().before(assignment.getOpenDate())) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_accept_before_open"));
 			valid = false;
 		}
 		
 		// check for due date before or equal to accept until
 		if (assignment.getDueDate() != null && assignment.getAcceptUntilDate() != null
 			&& assignment.getAcceptUntilDate().before(assignment.getDueDate())) {
 			messages.addMessage(new TargettedMessage("assignment2.assignment_accept_before_due"));
 			valid = false;
 		}
 		
 		return valid;
 	}
   
 }
