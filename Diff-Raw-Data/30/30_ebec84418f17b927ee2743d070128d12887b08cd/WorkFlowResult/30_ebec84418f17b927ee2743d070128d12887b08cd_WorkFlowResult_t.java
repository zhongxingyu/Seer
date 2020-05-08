 package org.sakaiproject.assignment2.tool;
 
 /**
  * Because our workflow of POST forms goes between the Instructor 
  * and Student layer (such as an Instructor previewing what an asnn
  * would look like as a Student) we are centralizing all the possible
  * action returns here.
  * 
  * @author sgithens
  *
  */
 public enum WorkFlowResult {
     /*
      * Student Authoring and Submission.
      */
     STUDENT_SUBMIT_SUBMISSION,
     STUDENT_CONTINUE_EDITING_SUBMISSION,
     STUDENT_PREVIEW_SUBMISSION,
     STUDENT_SAVE_DRAFT_SUBMISSION,
     STUDENT_SUBMISSION_FAILURE,
    STUDENT_CANCEL_SUBMISSION,
     
     /*
      * Instructor Assignment Authoring
      */
     INSTRUCTOR_POST_ASSIGNMENT,
     INSTRUCTOR_PREVIEW_ASSIGNMENT,
     INSTRUCTOR_SAVE_DRAFT_ASSIGNMENT,
     INSTRUCTOR_CANCEL_ASSIGNMENT,
     INSTRUCTOR_ASSIGNMENT_FAILURE,
     INSTRUCTOR_CONTINUE_EDITING_ASSIGNMENT, 
     INSTRUCTOR_ASSIGNMENT_VALIDATION_FAILURE,
     
     /*
      * Uploading Grades as a CSV File
      */
     UPLOADALL_CSV_UPLOAD,
     UPLOADALL_CSV_UPLOAD_FAILURE,
     UPLOADALL_CSV_CONFIRM_AND_SAVE,
     UPLOADALL_CSV_BACK_TO_UPLOAD
     
 }
