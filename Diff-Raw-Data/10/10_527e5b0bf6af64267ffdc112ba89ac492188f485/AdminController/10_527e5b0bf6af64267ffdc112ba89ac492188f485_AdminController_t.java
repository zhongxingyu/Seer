 package org.sukrupa.app.admin;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.sukrupa.app.services.EmailService;
 import org.sukrupa.student.*;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Map;
 
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
 
     private StudentService studentService;
     private EmailService emailService;
 
     @Autowired
     public AdminController(StudentService studentService, EmailService emailService) {
         this.studentService = studentService;
         this.emailService = emailService;
     }
 
 
     @RequestMapping()
     public String list(){
         return "admin/adminPage";
     }
 
     @RequestMapping("/monthlyreports")
     public String monthlyReports(@RequestParam(required = false, defaultValue = "1", value = "page") int pageNumber,
                                            @ModelAttribute("searchParam") StudentSearchParameter searchParam,
                                            Map<String, Object> model, HttpServletRequest request){
 
         model.put("searchCriteria", searchParam.getValidCriteria());
 
         StudentListPage students = studentService.getPage(searchParam, pageNumber, request.getQueryString());
 
         model.put("page", students);
 
         return "admin/monthlyreportsPage";
     }
 
     @RequestMapping("/sendnewsletter")
     public String sendNewsletter() {
         return "admin/sendnewsletterPage";
     }
 
     @RequestMapping(value = "/sendnewsletteremail", method = POST)
     public String sendNewsletterEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String comments, @RequestParam ("attach")MultipartFile file) throws MessagingException, IOException {
        String fileAttachmentFilePath = System.getProperty("user.dir")+file.getOriginalFilename();
         File webserverSideCopyOfClientSideFileAttachment = new File(fileAttachmentFilePath);
         file.transferTo(webserverSideCopyOfClientSideFileAttachment);
         emailService.sendNewsLetter(to, subject,comments,fileAttachmentFilePath);
         webserverSideCopyOfClientSideFileAttachment.delete();
          return "admin/thankyou";
 
     }
 
     @RequestMapping("/endofsponsorshipform")
     public String showEndOfSponsorshipForm() {
         return "admin/endofsponsorshipform";
     }
 
     @RequestMapping(value = "/endofsponsorshipmailsentPage")
     public String sendEndOfSponsorShipEmailAndShowConfirmPage(@RequestParam("to") String toAddress,
                                                               @RequestParam String subject, @RequestParam String comments) throws MessagingException {
         emailService.sendEmail(toAddress, subject, comments);
         return "/admin/endofsponsorshipmailsentPage";
     }
 
 }
