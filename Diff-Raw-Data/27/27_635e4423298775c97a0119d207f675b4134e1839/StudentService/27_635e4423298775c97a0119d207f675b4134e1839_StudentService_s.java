 package org.sukrupa.student;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.sukrupa.platform.DoNotRemove;
 
 @Service
 public class StudentService {
     private StudentRepository repository;
 
     @DoNotRemove
     StudentService() {}
 
     @Autowired
     public StudentService(StudentRepository repository) {
         this.repository = repository;
     }
 
     @Transactional
    public String addNoteFor(String studentId, String noteMessage) {
         Student student = repository.find(studentId);
         student.addNote(new Note(noteMessage));
        try {
            repository.saveOrUpdate(student);
            return "Note Added Successfully";
        }
        catch(Exception e){
            return "Error Adding Note";

        }
 
     }
 }
