 package org.sukrupa.app.students;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.sukrupa.student.*;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Map;
 
 import static java.lang.String.format;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 @RequestMapping("/students")
 public class StudentsController {
 
     private StudentService studentService;
 
     @Autowired
     public StudentsController(StudentService studentService) {
         this.studentService = studentService;
     }
 
     @RequestMapping
     public String list(@RequestParam(required = false, defaultValue = "1", value = "page") int pageNumber,
                        @ModelAttribute("searchParam") StudentSearchParameter searchParam,
                        Map<String, Object> model, HttpServletRequest request) {
         StudentListPage students = studentService.getPage(searchParam, pageNumber, request.getQueryString());
 
         if (students.getStudents().isEmpty()) {
             return "students/listEmpty";
         }
 
         model.put("page", students);
 
         return "students/list";
     }
 
 
     // @RequestMapping(value = "promote" , method = POST) feature not available yet
     public String promoteClass(Map<String, Integer> model) {
         int promoteStudentsCount = studentService.promoteStudentsToNextClass();
         model.put("numberOfStudentsUpdated", promoteStudentsCount);
         return "redirect:/students/update-successful";
     }
 
     @RequestMapping(value = "update-successful")
     public String updateSuccessful(@RequestParam("numberOfStudentsUpdated") int promoteStudentCount, Map<String, Integer> model) {
 
         model.put("numberOfStudentsUpdated", promoteStudentCount);
         return "students/updateSuccessful";
     }
 
     @RequestMapping(value = "{id}", method = POST)
     public String confirmUpdateStudent(
             @PathVariable String id,
             @ModelAttribute("updateStudent") StudentUpdateParameter studentUpdateParam,
             Map<String, Object> model) {
 
         Student updatedStudent = studentService.update(studentUpdateParam);
 
         if (updatedStudent != null) {
             model.put("student", updatedStudent);
             model.put("studentUpdatedSuccesfully", true);
             return format("redirect:/students/%s", id);
         } else {
             model.put("message", "Error updating student");
             return format("redirect:/students/%s/edit", id);
         }
     }
 
 
     @RequestMapping(value = "search")
     public void search(Map<String, Object> model) {
         model.put("formhelper", studentService.getReferenceData());
     }
 
     @RequestMapping(value = "{id}/edit", method = GET)
     public String edit(@PathVariable String id,
                        @RequestParam(required = false, defaultValue = "") String noteUpdateStatus,
                        @RequestParam(required = false) boolean noteAddedSuccesfully,
                        Map<String, Object> model) {
 
         Student student = studentService.load(id);
        //[Karthik,Suhas] Find a better way to recognize when users tries to edit non-existent student
        student.getStudentId();
         model.put("student", student);
         model.put("formhelper", formHelperFor(student));
         model.put("noteUpdateStatus", noteUpdateStatus);
         model.put("noteAddedSuccesfully", noteAddedSuccesfully);
 
         return "students/edit";
     }
 
     private StudentEditFormHelper formHelperFor(Student theStudent) {
         return new StudentEditFormHelper(theStudent, studentService.getReferenceData());
     }
 
 
     @RequestMapping(value = "{id}", method = GET)
     public String view(@PathVariable String id,
                        @RequestParam(required = false) boolean studentUpdatedSuccesfully,
                        Map<String, Object> model) {
         Student student = studentService.load(id);
         if (student != null) {
             model.put("student", student);
             model.put("studentUpdatedSuccesfully", studentUpdatedSuccesfully);
             return "students/view";
         }
         return "students/viewFailed";
     }
 
 }
