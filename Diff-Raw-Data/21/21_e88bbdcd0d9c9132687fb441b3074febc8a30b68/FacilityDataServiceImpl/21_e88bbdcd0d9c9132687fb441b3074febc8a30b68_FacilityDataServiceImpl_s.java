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
 package org.openmrs.module.facilitydata.service.impl;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.log4j.Logger;
 import org.openmrs.Location;
 import org.openmrs.api.context.Context;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.facilitydata.dao.FacilityDataDAO;
 import org.openmrs.module.facilitydata.model.FacilityDataFormQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSchema;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSection;
 import org.openmrs.module.facilitydata.model.FacilityDataQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataReportFormData;
 import org.openmrs.module.facilitydata.model.FacilityDataValue;
 import org.openmrs.module.facilitydata.service.FacilityDataService;
 import org.openmrs.module.facilitydata.util.FacilityDataUtil;
 
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 
 /**
  * This class serves as the layer just below the api layer but above the database layer.
  */
 @SuppressWarnings({"ALL"})
 public class FacilityDataServiceImpl extends BaseOpenmrsService implements FacilityDataService {
     private static final Logger log = Logger.getLogger(FacilityDataServiceImpl.class);
 
     private FacilityDataDAO dao;
 
     public void setDao(FacilityDataDAO dao) {
         this.dao = dao;
     }
 
     /**
      * Saves a form schema
      *
      * @param formSchema
      * @return the saved schema
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should save the specified formSchema
      * @should return the saved schema
      */
     public FacilityDataFormSchema saveFacilityDataFormSchema(FacilityDataFormSchema formSchema) {
         if (formSchema == null)
             throw new IllegalArgumentException("formSchema cannot be null");
         List<FacilityDataFormSection> formSections = formSchema.getFormSections();
         // for now we'll do this; later on a handler will be written to do this.
         if (formSections != null) {
             for (FacilityDataFormSection section : formSections) {
                  log.info(section);
                 // set the form question auditing data; handler needs to be written.
                 if (section.getCreator() == null)
                     section.setCreator(Context.getAuthenticatedUser());
 
                 if (section.getDateCreated() == null)
                     section.setDateCreated(new Date());
 
                 if (section.getUuid() == null)
                     section.setUuid(UUID.randomUUID().toString());
 
                 for (FacilityDataFormQuestion question : section.getQuestions()) {
                     if (question.getCreator() == null)
                         question.setCreator(Context.getAuthenticatedUser());
 
                     if (question.getDateCreated() == null)
                         question.setDateCreated(new Date());
 
                     if (question.getUuid() == null)
                         question.setUuid(UUID.randomUUID().toString());
                     FacilityDataQuestion facilityDataQuestion = question.getQuestion();
                     if (facilityDataQuestion != null) {
                         if (facilityDataQuestion.getCreator() == null)
                             facilityDataQuestion.setCreator(Context.getAuthenticatedUser());
 
                         if (facilityDataQuestion.getDateCreated() == null)
                             facilityDataQuestion.setDateCreated(new Date());
 
                         if (facilityDataQuestion.getUuid() == null)
                             facilityDataQuestion.setUuid(UUID.randomUUID().toString());
                     }
 
                 }
             }
         }
         return dao.saveFacilityDataFormSchema(formSchema);
     }
 
     /**
      * Get a specified form achema
      *
      * @param id the id of the schema to retrieve
      * @return the schema or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the specified object with that id or null if it doesn't exist
      */
     public FacilityDataFormSchema getFacilityDataFormSchema(Integer id) {
         if (id == null)
             throw new IllegalArgumentException("id cannot be null");
         return dao.getFacilityDataFormSchema(id);
     }
 
     /**
      * Gets a specified form schema using its {@link UUID}.
      *
      * @param uuid the UUID of the schema to retrieve.
      * @return the schema or null if it does exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the schema with the specified uuid or null if it doesn't exist
      */
     public FacilityDataFormSchema getFacilityDataFormSchemaByUUID(String uuid) {
         if (uuid == null)
             throw new IllegalArgumentException("uuid cannot be null");
         return dao.getFacilityDataFormSchemaByUUID(uuid);
     }
 
     /**
      * Get all Form Schemas in the system.
      *
      * @return a {@link List} containing all schemas.
      * @throws IllegalArgumentException if passed a null parameter
      * @should get all schemas
      */
     public List<FacilityDataFormSchema> getAllFacilityDataFormSchemas() {
         return dao.getAllFacilityDataFormSchemas();
     }
 
 
     /**
      * Retire a schema
      *
      * @param schema the schema
      * @should throw an exception if passed a null parameter
      * @should retire the given schema then save it.
      */
     public void retireFacilityDataFormSchema(FacilityDataFormSchema schema, String reason) {
         if (schema == null || reason == null) throw new IllegalArgumentException("null parameters are not allowed.");
         schema.setRetired(true);
         schema.setRetireReason(reason);
         Context.getService(FacilityDataService.class).saveFacilityDataFormSchema(schema);
     }
 
 
     /**
      * un-retire a schema
      *
      * @param schema the schema
      * @should throw an exception if passed a null parameter
      * @should unretire the given schema then save it
      */
     public void unretireFacilityDataFormSchema(FacilityDataFormSchema schema) {
         if (schema == null) throw new IllegalArgumentException("null parameters are not allowed.");
         schema.setRetired(false);
         Context.getService(FacilityDataService.class).saveFacilityDataFormSchema(schema);
     }
 
     /**
      * Delete a schema
      *
      * @param formSchema the schema to delete.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the specified schema
      */
     public void deleteFacilityDataFormSchema(FacilityDataFormSchema formSchema) {
         if (formSchema == null)
             throw new IllegalArgumentException("formSchema cannot be null");
         dao.deleteFacilityDataFormSchema(formSchema);
     }
 
     /**
      * Save a section
      *
      * @param formSection the section to save
      * @return the saved section
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should save the specified section
      * @should return the saved section
      */
     public FacilityDataFormSection saveFacilityDataFormSection(FacilityDataFormSection formSection) {
         if (formSection == null)
             throw new IllegalArgumentException("formSection cannot be null");
         for (FacilityDataFormQuestion question : formSection.getQuestions()) {
             if (question.getCreator() == null)
                 question.setCreator(Context.getAuthenticatedUser());
 
             if (question.getDateCreated() == null)
                 question.setDateCreated(new Date());
 
             if (question.getUuid() == null)
                 question.setUuid(UUID.randomUUID().toString());
             if (question.getId() != null) {
                 question.setChangedBy(Context.getAuthenticatedUser());
                 question.setDateChanged(new Date());
             }
         }
         return dao.saveFacilityDataFormSection(formSection);
     }
 
     /**
      * Get a section with the specified id
      *
      * @param id
      * @return the specified section with that id
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an error if passed a null parameter
      * @should return the section with the specified id or null if it doesn't exist
      */
     public FacilityDataFormSection getFacilityDataFormSection(Integer id) {
         if (id == null)
             throw new IllegalArgumentException("id cannot be null");
         return dao.getFacilityDataFormSection(id);
     }
 
     /**
      * Retrieve the section using its {@link UUID}.
      *
      * @param uuid
      * @return the section with the specified uuid
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the specified section with the specified uuid or null if it doesn't exist
      */
     public FacilityDataFormSection getFacilityDataFormSectionByUUID(String uuid) {
         if (uuid == null)
             throw new IllegalArgumentException("uuid cannot be null");
         return dao.getFacilityDataFormSectionByUUID(uuid);
     }
 
     /**
      * Get all sections which are currently saved.
      *
      * @return a {@link List} containing all sections saved.
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all sections.
      */
     public List<FacilityDataFormSection> getAllFacilityDataFormSections() {
         return dao.getAllFacilityDataFormSections();
     }
 
 
     /**
      * Retire a section
      *
      * @param section
      * @should throw an exception if passed a null parameter
      * @should retire the given section then save it
      */
     public void retireFacilityDataFormSection(FacilityDataFormSection section, String reason) {
         if (section == null || reason == null) throw new IllegalArgumentException("null parameters are not allowed.");
         section.setRetired(true);
         section.setRetireReason(reason);
         saveFacilityDataFormSection(section);
     }
 
 
     /**
      * un-retire a section
      *
      * @param section
      * @should throw an exception if passed a null parameter
      * @should unretire the given section then save it
      */
     public void unretireFacilityDataFormSection(FacilityDataFormSection section) {
         if (section == null) throw new IllegalArgumentException("null paramaters are not allowed.");
         section.setRetired(false);
         Context.getService(FacilityDataService.class).saveFacilityDataFormSection(section);
     }
 
     /**
      * Delete the specified form section.
      *
      * @param formSection
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the passed section from the database.
      */
     public void deleteFacilityDataFormSection(FacilityDataFormSection formSection) {
         if (formSection == null)
             throw new IllegalArgumentException("formSection cannot be null");
         dao.deleteFacilityDataFormSection(formSection);
     }
 
     /**
      * Save the Form Question
      *
      * @param formQuestion the form question to save
      * @return the saved question
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should save the specifed form question and then return it.
      */
     public FacilityDataFormQuestion saveFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion) {
         if (formQuestion == null)
             throw new IllegalArgumentException("formquestion cannot be null");
         return dao.saveFacilityDataFormQuestion(formQuestion);
     }
 
     /**
      * Get the form question using its id.
      *
      * @param id the id
      * @return the form question with the specified id or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the form question with the specified id or null if it does exist.
      */
     public FacilityDataFormQuestion getFacilityDataFormQuestion(Integer id) {
         if (id == null)
             throw new IllegalArgumentException("id cannot be null");
         return dao.getFacilityDataFormQuestion(id);
     }
 
     /**
      * Get the form question using its uuid
      *
      * @param uuid the {@link UUID}
      * @return the form question with the specified UUID or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the form question with the specified UUID or null if it does exist.
      */
     public FacilityDataFormQuestion getFacilityDataFormQuestionByUUID(String uuid) {
         if (uuid == null)
             throw new IllegalArgumentException("uuid cannot be null");
         return dao.getFacilityDataFormQuestionByUUID(uuid);
     }
 
 
     /**
      * Get all form questions.
      *
      * @return a {@link List} containing all form questions
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all form questions.
      */
     public List<FacilityDataFormQuestion> getAllFacilityDataFormQuestions() {
         return dao.getAllFacilityDataFormQuestions();
     }
 
 
     /**
      * Retire a form question
      *
      * @param formQuestion
      * @should throw an exception if passed a null parameter
      * @should retire the given form question then save it
      */
     public void retireFacilityDataFormQuestion(FacilityDataFormQuestion question, String reason) {
         if (question == null || reason == null) throw new IllegalArgumentException("id cannot be null");
         question.setRetired(true);
         question.setRetireReason(reason);
         Context.getService(FacilityDataService.class).saveFacilityDataFormQuestion(question);
     }
 
 
     /**
      * un-retire a form question
      *
      * @param formQuestion
      * @should throw an exception if passed a null parameter
      * @should unretire the form question then save it
      */
     public void unretireFacilityDataFormQuestion(FacilityDataFormQuestion question) {
         if (question == null) throw new IllegalArgumentException("id cannot be null");
         question.setRetired(false);
         Context.getService(FacilityDataService.class).saveFacilityDataFormQuestion(question);
     }
 
     /**
      * Delete a form question.
      *
      * @param formQuestion the form question to delete
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the specified form question
      */
     public void deleteFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion) {
         if (formQuestion == null)
             throw new IllegalArgumentException("formQuestion cannot be null");
         dao.deleteFacilityDataFormQuestion(formQuestion);
     }
 
 
     /**
      * Save a question to the database.
      *
      * @param question the question to be saved
      * @return the question which was just saved.
      * @throws IllegalArgumentException if passed a null question
      * @should throw an exception if the question passed is null or if a null parameter is passed.
      * @should return the saved question
      */
     public FacilityDataQuestion saveQuestion(FacilityDataQuestion question) {
         if (question == null)
             throw new IllegalArgumentException("question cannot be null");
         return dao.saveQuestion(question);
     }
 
     /**
      * Get a question
      *
      * @param id
      * @return the question or null if it does not exist
      * @should return the question with that id
      * @should return null if a question with that id does not exist
      * @should throw an exception if passed in a null parameter
      */
     public FacilityDataQuestion getQuestion(Integer id) {
         if (id == null)
             throw new IllegalArgumentException("id cannot be null");
         return dao.getQuestion(id);
     }
 
     /**
      * Get a question using its UUID
      *
      * @param uuid
      * @return the question pertaining to that specific UUID.
      * @throws IllegalArgumentException if passed a null parameter or empty String
      * @should throw an error if passed an empty string or a null parameter
      */
     public FacilityDataQuestion getQuestionByUUID(String uuid) {
         if (uuid == null || uuid.equals(""))
             throw new IllegalArgumentException("uuid cannot be null or empty");
         return dao.getQuestionByUUID(uuid);
     }
 
 
     /**
      * Get all questions
      *
      * @return a list containing all questions
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all sections.
      */
     public List<FacilityDataQuestion> getAllFacilityDataQuestions() {
         return dao.getAllFacilityDataQuestions();
     }
 
 
     /**
      * Retire a question
      *
      * @param question
      * @should throw an exception if passed a null parameter
      * @should retire a question then save it
      */
     public void retireFacilityDataQuestion(FacilityDataQuestion question, String reason) {
         if (question == null || reason == null) throw new IllegalArgumentException("null paramaters are not allowed.");
         question.setRetired(true);
         question.setRetireReason(reason);
         Context.getService(FacilityDataService.class).saveQuestion(question);
     }
 
 
     /**
      * un-retire a question
      *
      * @param question
      * @should throw an exception if passed a null parameter
      * @should unretire a question then save it
      */
     public void unretireFacilityDataQuestion(FacilityDataQuestion question) {
         if (question == null) throw new IllegalArgumentException("id cannot be null");
         question.setRetired(false);
         Context.getService(FacilityDataService.class).saveQuestion(question);
     }
 
     /**
      * Delete a question
      *
      * @param question the question object to be deleted
      * @should delete the question object
      * @should throw an exception if a null paremeter is passed
      */
     public void deleteQuestion(FacilityDataQuestion question) {
         if (question == null)
             throw new IllegalArgumentException("question cannot be null");
         dao.deleteQuestion(question);
     }
 
     /**
      * Save form data to the database
      *
      * @param value the <code>FacilityDataValue</code> object to save.
      * @return the saved object
      * @throws IllegalArgumentException if passed a null parameter
      * @should save the object to the database
      * @should throw an exception if passed a null parameter
      */
     public FacilityDataValue saveFacilityDataValue(FacilityDataValue value) {
         if (value == null)
             throw new IllegalArgumentException("value cannot be null");
         return dao.saveFacilityDataValue(value);
     }
 
     /**
      * Get a <code>FacilityDataValue</code> object with a given id.
      *
      * @param id
      * @return an object with that id or null if it does not exist
      * @throws IllegalArgumentException if passed a null parameter
      * @should return the object if it one exists with that given id
      * @should return null if an object with that object does not exist
      * @should throw an exception if passed a null parameter
      */
     public FacilityDataValue getFacilityDataValue(Integer id) {
         if (id == null)
             throw new IllegalArgumentException("id cannot be null");
         return dao.getFacilityDataValue(id);
     }
 
     /**
      * Get all {@link FacilityDataValue} references
      *
      * @return a list containing all the references
      * @should Return all values
      */
     public List<FacilityDataValue> getAllFacilityDataValues() {
         return dao.getAllFacilityDataValues();
     }
 
 
     /**
      * void a FacilityDataValue
      *
      * @param value
      * @should throw an exception if passed a null parameter
      * @should void then save
      */
     public void voidFacilityDataFormValue(FacilityDataValue value, String reason) {
         if (value == null || reason == null) throw new IllegalArgumentException("null paramaters are not allowed.");
         value.setVoided(true);
         value.setVoidReason(reason);
         Context.getService(FacilityDataService.class).saveFacilityDataValue(value);
 
     }
 
 
     /**
      * un-void a FacilityDataValue
      *
      * @param value
      * @should throw an exception if passed a null parameter
      * @should void then save
      */
     public void unvoidFacilityDataFormValue(FacilityDataValue value) {
         if (value == null) throw new IllegalArgumentException("null paramaters are not allowed.");
         value.setVoided(false);
         Context.getService(FacilityDataService.class).saveFacilityDataValue(value);
     }
 
 
     /**
      * Delete a <code>FacilityDataValue</code> from the database
      *
      * @param value
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the given object from the database
      */
     public void deleteFacilityValue(FacilityDataValue value) {
         if (value == null)
             throw new IllegalArgumentException("value cannot be null");
         dao.deleteFacilityValue(value);
     }
 
     /**
      * Used to save the data that is entered into the reports.
      * @param question the associated question being answered.
      * @param value the existing answer -- if any.
      * @param val the value.
      * @param comments the comment such as why, etc.
      * @param location the facility
      * @param fromDate the start of the period.
      * @param toDate the end of the period.
      */
    public void saveValues(FacilityDataQuestion question, FacilityDataValue value, String val, String comments, Location location,
                                   Date fromDate, Date toDate) {
         // don't process empty questions; this prevents false positives when determining completion status
         if ("".equals(val)) return;
         //TODO: validate input; it is assumed that valid input is provided for the time being.
         if (FacilityDataUtil.isNumericQuestion(question)) {
             if (value != null) {
                 if (value.getValueNumeric() != null && value.getValueNumeric() != Double.parseDouble(val)) {
                     FacilityDataUtil.getService().voidFacilityDataFormValue(value, String.format("Report was edited by %s on %s", Context.getAuthenticatedUser(),
                             Context.getDateFormat().format(new Date())));
                 } else {
                     return;
                 }
                 value = new FacilityDataValue();
                 // allow empty values; assumes valid input
                 if (!val.equals(""))
                     value.setValueNumeric(Double.parseDouble(val));
                 if (comments != null) {
                     value.setComments(comments);
                 }
             } else {
                 value = new FacilityDataValue();
                 // allow empty values; assumes valid input
                 if (!val.equals(""))
                     value.setValueNumeric(Double.parseDouble(val));
                 if (comments != null) {
                     value.setComments(comments);
                 }
             }
             value.setFacility(location);
             value.setFromDate(fromDate);
             value.setToDate(toDate);
             value.setQuestion(question);
             FacilityDataUtil.getService().saveFacilityDataValue(value);
         } else if (FacilityDataUtil.isBooleanCodedQuestion(question)) {
             if (value != null) {
                 if (!value.getValueText().equals(val)) {
                     FacilityDataUtil.getService().voidFacilityDataFormValue(value, String.format("Report was edited by %s on %s", Context.getAuthenticatedUser(),
                             Context.getDateFormat().format(new Date())));
                 } else {
                     return;
                 }
                 value = new FacilityDataValue();
                 value.setValueText(val);
                 if (comments != null) {
                     value.setComments(comments);
                 }
             } else {
                 value = new FacilityDataValue();
                 value.setValueText(val);
                 if (comments != null) {
                     value.setComments(comments);
                 }
             }
             value.setFacility(location);
             value.setFromDate(fromDate);
             value.setToDate(toDate);
             value.setQuestion(question);
             FacilityDataUtil.getService().saveFacilityDataValue(value);
         } else if (FacilityDataUtil.isStockQuestion(question)) {
             if (value != null) {
                 if (!value.getValueText().equals(val) || !value.getComments().equals(comments)) {
                     FacilityDataUtil.getService().voidFacilityDataFormValue(value, String.format("Report was edited by %s on %s", Context.getAuthenticatedUser(),
                             Context.getDateFormat().format(new Date())));
                 } else {
                     return;
                 }
                 value = new FacilityDataValue();
                 value.setValueText(val);
                 // in the case of stock questions comment is ALWAYS filled out.
                 value.setComments(comments);
             } else {
                 value = new FacilityDataValue();
                 value.setValueText(val);
                 // in the case of stock questions comment is ALWAYS filled out.
                 value.setComments(comments);
             }
             value.setFacility(location);
             value.setFromDate(fromDate);
             value.setToDate(toDate);
             value.setQuestion(question);
             FacilityDataUtil.getService().saveFacilityDataValue(value);
         }
     }
 
     /**
      * Gets the number of questions that have been filled out for a given report.
      * @param formData
      * @param schema
      * @return
      */
     public int getNumberOfQuestionedFilledOut(FacilityDataReportFormData formData, FacilityDataFormSchema schema) {
         if (formData == null || schema == null) return 0;
         int cnt = 0;
         for (FacilityDataFormSection section : schema.getFormSections()) {
             for (FacilityDataFormQuestion formQuestion : section.getQuestions()) {
                 for (FacilityDataValue value : formData.getValues()) {
                     if (formQuestion.getQuestion().getUuid().equals(value.getQuestion().getUuid()))
                         cnt++;
                 }
             }
         }
         return cnt;
     }
 
     public int getNumberOfQuestionsInReport(FacilityDataFormSchema schema) {
         int cnt = 0;
         for (FacilityDataFormSection section : schema.getFormSections()) {
             for (FacilityDataFormQuestion formQuestion : section.getQuestions()) {
                 cnt++;
             }
         }
         return cnt;
     }
 
     /**
      * Utility method to get all values associated with each question in a {@link org.openmrs.module.facilitydata.model.FacilityDataFormSchema}.
      *
      * @param schema    the schema
      * @param startDate
      * @param endDate
      * @param location  the location of the clinic
      * @return an instance of {@link org.openmrs.module.facilitydata.model.FacilityDataReportFormData}.
      */
     public FacilityDataReportFormData getFacilityDataReportFormData(FacilityDataFormSchema schema, Date startDate, Date endDate, Location location) {
         FacilityDataReportFormData formData = new FacilityDataReportFormData();
         formData.setSchema(schema);
         formData.setStartDate(startDate);
         formData.setEndDate(endDate);
         formData.setLocation(location);
         List<FacilityDataValue> valueList = Lists.newArrayList();
         for (FacilityDataFormSection section : schema.getFormSections()) {
             for (FacilityDataFormQuestion question : section.getQuestions()) {
                 for (FacilityDataValue value : FacilityDataUtil.getService().getAllFacilityDataValues()) {
                     // first check to see if a question with this {@link UUID} has a {@link FacilityDataValue}.
                     if (question.getQuestion().getUuid().equals(value.getQuestion().getUuid()) && !value.isVoided()) {
                         // now check the rest of the constraints.
                         if (value.getFacility().equals(location)
                                 && DateUtils.isSameDay(value.getFromDate(), startDate)
                                 && DateUtils.isSameDay(value.getToDate(), endDate)) {
                             // it is associated with this question and matches all the criteria.
                             valueList.add(value);
                         }
                     }
                 }
             }
         }
         formData.setValues(valueList);
         return formData;
     }
 }
