 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/api/logic/src/java/org/sakaiproject/assignment2/logic/ExternalGradebookLogic.java $
  * $Id: ExternalGradebookLogic.java 12544 2006-05-03 15:06:26Z wagnermr@iupui.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
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
 
 package org.sakaiproject.assignment2.logic;
 
 
 import org.sakaiproject.assignment2.model.Assignment2;
 
 /**
  * This is the interface for logic which is related to the integration
  * with the Announcements tool
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public interface ExternalAnnouncementLogic {
 	
 	/**
 	 * Add an announcement of the given assignment's open date to the 
 	 * Annoucements tool
 	 * @param assignment
 	 * @param contextId
 	 * @param announcementSubject
 	 * @param announcementBody
 	 * @throws AnnouncementPermissionException
 	 * 		if the current user is not authorized to add an announcement
 	 * @return the id of the newly created announcement
 	 */
 	public String addOpenDateAnnouncement(Assignment2 assignment, String contextId, 
			String announcementSubject, String announcementBody);
 	
 	/**
 	 * Update an announcement for the given assignment. Announcements must be
 	 * updated when the title or open date of the assignment changes.
 	 * @param assignment
 	 * @param contextId
 	 * @param announcementSubject
 	 * @param announcementBody
 	 * @throws AnnouncementPermissionException
 	 * 		if the current user is not authorized to update an announcement
 	 * @return the id of the updated announcement
 	 */
 	public String updateOpenDateAnnouncement(Assignment2 assignment, String contextId,
			String announcementSubject, String announcementBody);
 	
 	/**
 	 * Delete an existing announcement associated with the given assignment.
 	 * @param assignment
 	 * @param contextId
 	 * * @throws AnnouncementPermissionException
 	 * 		if the current user is not authorized to delete an announcement
 	 */
	public void deleteOpenDateAnnouncement(Assignment2 assignment, String contextId);
 }
