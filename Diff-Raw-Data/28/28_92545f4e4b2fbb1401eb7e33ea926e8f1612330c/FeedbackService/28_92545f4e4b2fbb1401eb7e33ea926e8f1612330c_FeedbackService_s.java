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
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import org.openmrs.User;
 import org.openmrs.annotation.Authorized;
 import org.openmrs.api.APIException;
 import org.openmrs.module.feedback.db.FeedbackDAO;
 
 import org.springframework.transaction.annotation.Transactional;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.util.List;
 
 @Transactional
 public interface FeedbackService {
     public void setFeedbackdDAO(FeedbackDAO dao);
 
     /**
      * Saves (creates) a new feedback module data
      *
      * @param //saying to be created
      * @throws APIException
      */
     public void saveSeverity(Severity feedbackSeverity) throws APIException;
 
     public void saveStatus(Status Status) throws APIException;
 
//    @Authorized({ "Admin Feedback" })
     public void savePredefinedSubject(PredefinedSubject PredefinedSubject) throws APIException;
 
     @Authorized({ "Add Feedback" })

     public void saveFeedback(Feedback Feedback) throws APIException;
 
     public void saveFeedbackComment(FeedbackComment FeedbackComment) throws APIException;
 
     public void saveFeedbackUser(FeedbackUser FeedbackUser) throws APIException;
 
     /**
      * Get response by internal identifier
      *
      * @param //responseId internal saying identifier
      * @return response with given internal identifier
      * @throws APIException
      */
     @Transactional(readOnly = true)
     public Severity getSeverity(Integer feedbackSeverityId) throws APIException;
 
     public Status getStatus(Integer StatusId) throws APIException;
 
     @Authorized({ "Add Feedback" })
     public PredefinedSubject getPredefinedSubject(Integer PredefinedSubjectId) throws APIException;
 
     public Feedback getFeedback(Integer feedbackId) throws APIException;
 
     public FeedbackComment getFeedbackComment(Integer feedbackCommentId) throws APIException;
 
     public List<User> getFeedbackUser(Feedback feedback) throws APIException;
 
     /**
      * Save response
      *
      * @param //response to be updated
      * @throws APIException
      */
     public void updateSeverity(Severity severity) throws APIException;
 
     public void updateStatus(Status status) throws APIException;
 
     public void updateFeedbackUser(FeedbackUser feedbackUser) throws APIException;
 
     @Authorized({ "Admin Feedback" })
     public void updatePredefinedSubject(PredefinedSubject PredefinedSubject) throws APIException;
 
     @Authorized({ "Admin Feedback" })
     public void deletePredefinedSubject(PredefinedSubject PredefinedSubject) throws APIException;
 
     public void deleteStatus(Status Status) throws APIException;
 
     public void deleteSeverity(Severity feedbackSeverity) throws APIException;
 
     public void deleteFeedback(Feedback Feedback) throws APIException;
 
     public void deleteFeedbackUser(Feedback feedback, User user) throws APIException;
 
     @Transactional(readOnly = true)
     public List<Severity> getSeverities() throws APIException;
 
     public List<Status> getStatuses() throws APIException;
 
     public List<PredefinedSubject> getPredefinedSubjects() throws APIException;
 
     public List<Feedback> getFeedbacks() throws APIException;
 
     public List<Feedback> getAssignedFeedbacks(User assignedUser) throws APIException;
 
     public List<Feedback> getFeedbacks(User user) throws APIException;
 
     public List<FeedbackComment> getFeedbackComments(Integer feedbackId) throws APIException;
 }
 
 
 //~ Formatted by Jindent --- http://www.jindent.com
