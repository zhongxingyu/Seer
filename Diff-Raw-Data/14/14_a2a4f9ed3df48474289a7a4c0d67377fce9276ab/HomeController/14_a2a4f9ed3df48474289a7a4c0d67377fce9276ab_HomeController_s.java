 package com.ee.excellentpdf.web;
 
 import com.ee.excellentpdf.domain.RenderService;
 import com.ee.excellentpdf.domain.SalarySlip;
 import com.ee.excellentpdf.email.EmailService;
 import com.ee.excellentpdf.reader.ExcelService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @Component
 @Controller
 public class HomeController {
 
     private ExcelService excelService;
     private RenderService renderService;
     private EmailService emailService;
 
     @Value("${email}")
     private String emailId;
 
     @Value("${path}")
     private String path;
     private String emailSubject;
     private String emailBody;
 
     @Autowired
     public HomeController(ExcelService excelService, RenderService renderService, EmailService emailService) {
         this.excelService = excelService;
         this.renderService = renderService;
         this.emailService = emailService;
     }
 
     @RequestMapping(value = "/upload", method = RequestMethod.POST)
     public
     @ResponseBody
     String convertToPDf(@RequestPart("file") MultipartFile excelFile, @ModelAttribute("task") String task) throws IOException {
         File file = new File(excelFile.getName());
         excelFile.transferTo(file);
         final List<SalarySlip> salarySlips = excelService.fetchSalarySlips(file);
         final Map<String,String> emailAndFiles = renderService.renderPDF(salarySlips, path);
         if("uploadAndEmail".equals(task)){
             emailService.sendEmailToAll(emailAndFiles, emailSubject, emailBody);
         }
        return renderService.getFileNames(emailAndFiles);
     }
 
     @RequestMapping(value = "/email")
     public
     @ResponseBody
     String sendEmail() {
         File folder = new File(path);
         File[] listOfFiles = folder.listFiles();
         int numberOfFiles =  listOfFiles.length;
         if(numberOfFiles <= 0){
             return "There are no files to email.";
         }
 
         for (int i = 0; i < numberOfFiles; i++)
         {
             if (listOfFiles[i].isFile())
             {
                 File file = new File(path+"/"+listOfFiles[i].getName());
                 emailSubject = "Salary Slip";
                 emailBody = "Hi User, hope you get paid!!!";
                 emailService.sendMail(file, emailId, emailSubject, emailBody);
             }
         }
         return "E-Mail Sent to "+ emailId;
     }
 }
