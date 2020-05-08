 package org.sukrupa.student;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.Map;
 
 import static java.lang.String.format;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 @RequestMapping("/students/{studentId}/notes")
 public class NoteController {
 
     private final StudentService service;
 
     @Autowired
     public NoteController(StudentService service) {
         this.service = service;
     }
 
     @RequestMapping(method = POST)
     public String all(@PathVariable String studentId, @RequestParam("new-note") String newNote, Map<String, Object> model) {
         model.put("studentId", studentId);
 
        try {
            service.addNoteFor(studentId, newNote);
            model.put("noteUpdateStatus", "Note Added Successfully");
            model.put("noteAddedSuccesfully", true);
        } catch (Exception e) {
            model.put("noteUpdateStatus", "Error Adding Note");
        }
         return format("redirect:/students/%s/edit",studentId);
     }
 }
