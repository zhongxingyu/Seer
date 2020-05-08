 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.feedback;
 
 import java.util.List;
 import org.openmrs.User;
 
 import org.openmrs.annotation.Authorized;
 import org.openmrs.api.APIException;
 import org.openmrs.module.feedback.db.FeedbackDAO;
 import org.springframework.transaction.annotation.Transactional;
 
 @Transactional
 public interface FeedbackService {
 
 	public void setFeedbackdDAO(FeedbackDAO dao);
 
 	/**
 	 * Saves (creates) a new feedback module data
 	 * 
 	 * @param saying to be created
 	 * @throws APIException
 	 */
	@Authorized({"Add Feedback Criteria"})
         public void saveSeverity (Severity feedbackSeverity) throws APIException;
         public void saveStatus (Status Status ) throws APIException;
         public void savePredefinedSubject (PredefinedSubject PredefinedSubject ) throws APIException;
         public void saveFeedback (Feedback Feedback) throws APIException;
 	/**
 	 * Get response by internal identifier
 	 * 
 	 * @param responseId internal saying identifier
 	 * @return response with given internal identifier
 	 * @throws APIException
 	 */
	@Authorized({"View Feedback Criteria"})
 	@Transactional(readOnly=true)
         public Severity getSeverity(Integer feedbackSeverityId) throws APIException;
         public Status getStatus(Integer StatusId) throws APIException;
         public PredefinedSubject getPredefinedSubject (Integer PredefinedSubjectId ) throws APIException;
         public Feedback getFeedback (Integer feedbackId ) throws APIException;
 
 
 	/**
 	 * Save response
 	 * 
 	 * @param response to be updated
 	 * @throws APIException
 	 */
	@Authorized({"Edit Feedback Criteria"})
         public void updateSeverity(Severity severity) throws APIException;
         public void updateStatus(Status status) throws APIException;
         public void updatePredefinedSubject(PredefinedSubject PredefinedSubject) throws APIException;
         
         public void deletePredefinedSubject(PredefinedSubject PredefinedSubject) throws APIException;        
         public void deleteStatus (Status Status) throws APIException;
         public void deleteSeverity (Severity feedbackSeverity) throws APIException;
 
         @Transactional(readOnly=true)
	@Authorized({"View Feedback Criteria"})
         public List<Severity> getSeverities() throws APIException;
         public List<Status> getStatuses() throws APIException;
         public List<PredefinedSubject> getPredefinedSubjects() throws APIException;
         public List<Feedback> getFeedbacks () throws APIException ;
 	public List<Feedback> getFeedbacks (User user) throws APIException ;
 
 
 
 }
