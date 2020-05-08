 package org.sukrupa.app.students;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 import org.sukrupa.platform.date.Date;
 import org.sukrupa.student.StudentForm;
 import org.sukrupa.student.StudentRepository;
 
 @Service
 public class StudentValidator implements Validator {
 
     private StudentRepository studentRepository;
 
     @Autowired
     public StudentValidator(StudentRepository studentRepository) {
         this.studentRepository = studentRepository;
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return StudentForm.class.isAssignableFrom(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
 
         StudentForm studentParam = (StudentForm) target;
         String dateOfBirthString = studentParam.getDateOfBirth();
 
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.required", "Missing Student Name. Please re-enter.") ;
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "studentId", "studentId.required", "Missing Student ID. Please re-enter.") ;
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "gender.required", "Please select a gender.") ;
 
        if (!dateOfBirthString.matches("\\d\\d-\\d\\d-\\d\\d\\d\\d")) {
             errors.rejectValue("dateOfBirth","dateOfBirth.required","Please enter a valid date format.");
        } else {
             Date dateOfBirth = Date.parse(dateOfBirthString,"");
            if (!dateOfBirth.isInThePast()) {
                 errors.rejectValue("dateOfBirth","dateOfBirth.required","Please use a date in the past.");
             }
         }
 
         if(null != studentRepository.findByStudentId(studentParam.getStudentId())){
             errors.rejectValue("studentId", "studentID.duplicate", "Student with the same ID already exists.");
         }
 
     }
 
 
 }
