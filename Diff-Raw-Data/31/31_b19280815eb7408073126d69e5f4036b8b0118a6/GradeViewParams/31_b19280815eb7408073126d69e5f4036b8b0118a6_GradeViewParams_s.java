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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.tool.beans.Assignment2Bean;
 import org.sakaiproject.assignment2.tool.producers.GradeProducer;
 
 public class GradeViewParams extends SimpleViewParameters implements VerifiableViewParams {
 	
 	private static final Log LOG = LogFactory.getLog(Assignment2Bean.class);
 	
 	public Long assignmentId;
 	public String userId;
	public Long submissionId;
 	
 	public GradeViewParams(){}
 	
 	public GradeViewParams(String viewId, Long assignmentId, String userId){
 		super(viewId);
 		this.assignmentId = assignmentId;
 		this.userId = userId;
 	}
 	
	public GradeViewParams(String viewId, Long assignmentId, String userId, Long submissionId) {
 		super(viewId);
 		this.assignmentId = assignmentId;
 		this.userId = userId;
		this.submissionId = submissionId;
 	}
 	
 	public String getParseSpec(){
		return super.getParseSpec() + ",@1:assignmentId,@2:userId,submissionId";
 	}
 
 	public Boolean verify()
 	{
 		if (GradeProducer.VIEW_ID.equals(this.viewID) && (this.assignmentId == null || this.userId == null)) {
 			LOG.error("Null assignmentId or userId in viewparameters while attempting to load GradeProducer");
 			return Boolean.FALSE;
 		}
 		return Boolean.TRUE;
 	}
 	
 }
