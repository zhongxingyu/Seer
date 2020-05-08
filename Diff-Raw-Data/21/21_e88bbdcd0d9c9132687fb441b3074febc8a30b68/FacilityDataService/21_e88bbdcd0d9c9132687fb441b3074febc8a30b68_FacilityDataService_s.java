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
 package org.openmrs.module.facilitydata.service;
 
 import org.openmrs.Location;
 import org.openmrs.annotation.Authorized;
 import org.openmrs.api.OpenmrsService;
 import org.openmrs.module.facilitydata.model.FacilityDataFormQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSchema;
 import org.openmrs.module.facilitydata.model.FacilityDataFormSection;
 import org.openmrs.module.facilitydata.model.FacilityDataQuestion;
 import org.openmrs.module.facilitydata.model.FacilityDataReportFormData;
 import org.openmrs.module.facilitydata.model.FacilityDataValue;
 import org.openmrs.module.facilitydata.util.FacilityDataConstants;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * This interface serves as the layer between the api and data access layers.
  */
 public interface FacilityDataService extends OpenmrsService {
 
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
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     FacilityDataFormSchema saveFacilityDataFormSchema(FacilityDataFormSchema formSchema);
 
     /**
      * Get a specified form achema
      *
      * @param id the id of the schema to retrieve
      * @return the schema or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the specified object with that id or null if it doesn't exist
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormSchema getFacilityDataFormSchema(Integer id);
 
     /**
      * Gets a specified form schema using its {@link UUID}.
      *
      * @param uuid the UUID of the schema to retrieve.
      * @return the schema or null if it does exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the schema with the specified uuid or null if it doesn't exist
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormSchema getFacilityDataFormSchemaByUUID(String uuid);
 
     /**
      * Get all Form Schemas in the system.
      *
      * @return a {@link List} containing all schemas.
      * @throws IllegalArgumentException if passed a null parameter
      * @should get all schemas
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     List<FacilityDataFormSchema> getAllFacilityDataFormSchemas();
 
     /**
      * Retire a schema
      *
      * @param schema the schema
      * @should throw an exception if passed a null parameter
      * @should retire the given schema then save it.
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void retireFacilityDataFormSchema(FacilityDataFormSchema schema, String reason);
 
     /**
      * un-retire a schema
      *
      * @param schema the schema
      * @should throw an exception if passed a null parameter
      * @should unretire the given schema then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void unretireFacilityDataFormSchema(FacilityDataFormSchema schema);
 
     /**
      * Delete a schema
      *
      * @param formSchema the schema to delete.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the specified schema
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void deleteFacilityDataFormSchema(FacilityDataFormSchema formSchema);
 
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
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     FacilityDataFormSection saveFacilityDataFormSection(FacilityDataFormSection formSection);
 
     /**
      * Get a section with the specified id
      *
      * @param id
      * @return the specified section with that id
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an error if passed a null parameter
      * @should return the section with the specified id or null if it doesn't exist
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormSection getFacilityDataFormSection(Integer id);
 
     /**
      * Retrieve the section using its {@link UUID}.
      *
      * @param uuid
      * @return the section with the specified uuid
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the specified section with the specified uuid or null if it doesn't exist
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormSection getFacilityDataFormSectionByUUID(String uuid);
 
     /**
      * Get all sections which are currently saved.
      *
      * @return a {@link List} containing all sections saved.
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all sections.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     List<FacilityDataFormSection> getAllFacilityDataFormSections();
 
     /**
      * Retire a section
      *
      * @param section
      * @should throw an exception if passed a null parameter
      * @should retire the given section then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void retireFacilityDataFormSection(FacilityDataFormSection section, String reason);
 
     /**
      * un-retire a section
      *
      * @param section
      * @should throw an exception if passed a null parameter
      * @should unretire the given section then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void unretireFacilityDataFormSection(FacilityDataFormSection section);
 
     /**
      * Delete the specified form section.
      *
      * @param formSection
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the passed section from the database.
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void deleteFacilityDataFormSection(FacilityDataFormSection formSection);
 
     /**
      * Save the Form Question
      *
      * @param formQuestion the form question to save
      * @return the saved question
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should save the specifed form question and then return it.
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     FacilityDataFormQuestion saveFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion);
 
     /**
      * Get the form question using its id.
      *
      * @param id the id
      * @return the form question with the specified id or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the form question with the specified id or null if it does exist.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormQuestion getFacilityDataFormQuestion(Integer id);
 
     /**
      * Get the form question using its uuid
      *
      * @param uuid the {@link UUID}
      * @return the form question with the specified UUID or null if it does not exist.
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should return the form question with the specified UUID or null if it does exist.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataFormQuestion getFacilityDataFormQuestionByUUID(String uuid);
 
     /**
      * Get all form questions.
      *
      * @return a {@link List} containing all form questions
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all form questions.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     List<FacilityDataFormQuestion> getAllFacilityDataFormQuestions();
 
     /**
      * Retire a form question
      *
      * @param formQuestion
      * @should throw an exception if passed a null parameter
      * @should retire the given form question then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void retireFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion, String reason);
 
     /**
      * un-retire a form question
      *
      * @param formQuestion
      * @should throw an exception if passed a null parameter
      * @should unretire the form question then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void unretireFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion);
 
     /**
      * Delete a form question.
      *
      * @param formQuestion the form question to delete
      * @throws IllegalArgumentException if passed a null parameter
      * @should throw an exception if passed a null parameter
      * @should delete the specified form question
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void deleteFacilityDataFormQuestion(FacilityDataFormQuestion formQuestion);
 
     /**
      * Save a <code>FacilityDataQuestion</code> to the database.
      *
      * @param question the <code>FacilityDataQuestion</code> to be saved
      * @return the <code>FacilityDataQuestion</code> which was just saved.
      * @throws IllegalArgumentException if passed a null <code>FacilityDataQuestion</code>
      * @should throw an exception if the <code>FacilityDataQuestion</code> passed is null or if a null parameter is passed.
      * @should return the saved <code>FacilityDataQuestion</code>
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     FacilityDataQuestion saveQuestion(FacilityDataQuestion question);
 
     /**
      * Get a <code>FacilityDataQuestion</code>
      *
      * @param id
      * @return the <code>FacilityDataQuestion</code> or null if it does not exist
      * @throws IllegalArgumentException if passed a null parameter
      * @should return the <code>FacilityDataQuestion</code> with that id
      * @should return null if a <code>FacilityDataQuestion</code> with that id does not exist
      * @should throw an exception if passed in a null parameter
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataQuestion getQuestion(Integer id);
 
     /**
      * Get a question using its UUID
      *
      * @param uuid
      * @return the question pertaining to that specific UUID.
      * @throws IllegalArgumentException if passed a null parameter or empty String
      * @should throw an error if passed an empty string or a null parameter
      * @should return the question with the specified uuid or null.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataQuestion getQuestionByUUID(String uuid);
 
     /**
      * Get all questions
      *
      * @return a list containing all questions
      * @throws IllegalArgumentException if passed a null parameter
      * @should return a list of all sections.
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     List<FacilityDataQuestion> getAllFacilityDataQuestions();
 
     /**
      * Retire a question
      *
      * @param question
      * @should throw an exception if passed a null parameter
      * @should retire a question then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void retireFacilityDataQuestion(FacilityDataQuestion question, String reason);
 
     /**
      * un-retire a question
      *
      * @param question
      * @should throw an exception if passed a null parameter
      * @should unretire a question then save it
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void unretireFacilityDataQuestion(FacilityDataQuestion question);
 
     /**
      * Delete a <code>FacilityDataQuestion</code>
      *
      * @param question the <code>FacilityDataQuestion</code> object to be deleted
      * @throws IllegalArgumentException if passed a null parameter
      * @should delete the <code>FacilityDataQuestion</code> object
      * @should throw an exception if a null paremeter is passed
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void deleteQuestion(FacilityDataQuestion question);
 
 
     /**
      * Save form data to the database
      *
      * @param value the <code>FacilityDataValue</code> object to save.
      * @return the saved object
      * @throws IllegalArgumentException if passed a null parameter
      * @should save the object to the database
      * @should throw an exception if passed a null parameter
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.ENTER})
     FacilityDataValue saveFacilityDataValue(FacilityDataValue value);
 
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
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     FacilityDataValue getFacilityDataValue(Integer id);
 
     /**
      * Get all {@link FacilityDataValue} references.
      *
      * @return a list of all values entered.
      * @should return a list of all values entered.
      */
     @Transactional(readOnly = true)
     @Authorized({FacilityDataConstants.MANAGE, FacilityDataConstants.VIEW})
     List<FacilityDataValue> getAllFacilityDataValues();
 
     /**
      * void a FacilityDataValue
      *
      * @param value
      * @should throw an exception if passed a null parameter
      * @should void then save
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void voidFacilityDataFormValue(FacilityDataValue value, String reason);
 
     /**
      * un-void a FacilityDataValue
      *
      * @param value
      * @should throw an exception if passed a null parameter
      * @should void then save
      */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void unvoidFacilityDataFormValue(FacilityDataValue value);
 
     /* Delete a value from the database.
     *
     * @param value the object containing the form data entered for this report.
     * @throws IllegalArgumentException if passed a null parameter
     * @should throw an exception if passed a null parameter
     * @should delete the given object from the database
     */
     @Transactional
     @Authorized({FacilityDataConstants.MANAGE})
     void deleteFacilityValue(FacilityDataValue value);
 
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
    void saveValues(FacilityDataQuestion question, FacilityDataValue value, String val, String comments, Location location,
                            Date fromDate, Date toDate);
 
     /**
      * Gets the number of questions that have been filled out for a given report.
      * @param formData
      * @param schema
      * @return
      */
     int getNumberOfQuestionedFilledOut(FacilityDataReportFormData formData, FacilityDataFormSchema schema);
 
     int getNumberOfQuestionsInReport(FacilityDataFormSchema schema);
 
     /**
      * Utility method to get all values associated with each question in a {@link FacilityDataFormSchema}.
      *
      * @param schema    the schema
      * @param startDate
      * @param endDate
      * @param location  the location of the clinic
      * @return an instance of {@link FacilityDataReportFormData}.
      */
     public FacilityDataReportFormData getFacilityDataReportFormData(FacilityDataFormSchema schema, 
                                                                            Date startDate,
                                                                            Date endDate, Location location);
 }
