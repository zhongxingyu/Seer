 /**********************************************************************************
  * $URL$
  * $Id$
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
 
 package org.sakaiproject.assignment2.taggable.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.taggable.api.TaggableActivity;
 import org.sakaiproject.taggable.api.TaggableActivityProducer;
 
 public class AssignmentActivityImpl implements TaggableActivity {
 
 	private static final Log logger = LogFactory
     	.getLog(AssignmentActivityImpl.class);
 	
     protected Assignment2 assignment;
 
     protected TaggableActivityProducer producer;
 
     public AssignmentActivityImpl(Assignment2 assignment,
             TaggableActivityProducer producer) {
         this.assignment = assignment;
         this.producer = producer;
     }
 
     public boolean equals(Object object) {
         if (object instanceof TaggableActivity) {
             TaggableActivity activity = (TaggableActivity) object;
             return activity.getReference().equals(this.getReference());
         }
         return false;
     }
 
     public String getContext() {
         return assignment.getContextId();
     }
 
     public String getDescription() {
         return assignment.getInstructions();
     }
 
     public Object getObject() {
         return assignment;
     }
 
     public TaggableActivityProducer getProducer() {
         return producer;
     }
 
     public String getReference() {
         return assignment.getReference();
     }
 
     public String getTitle() {
         return assignment.getTitle();
     }
 
     public String getActivityDetailUrl()
     {
         //http://localhost:8080/direct/view-assignment2/1234
         
     	String url = ServerConfigurationService.getServerUrl() + "/direct/view-assignment2/" + 
     	Long.toString(assignment.getId());
     	
         return url;
     }
 
     public String getTypeName()
     {
         return producer.getName();
     }
     
     private Site getSite(String siteId) {
 		Site site = null;
 		try {
 			site = SiteService.getSite(siteId);
 		} catch (IdUnusedException e) {
 			logger.error("Unable to get Site object from site id: " + siteId, e);
 		}
 		return site;
 	}
 
	@Override
	public String getActivityDetailUrlParams() {
		return "";
	}

	@Override
	public boolean getUseDecoration() {
		return false;
	}

 }
