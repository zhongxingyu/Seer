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
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestPart;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
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
 
     @Autowired
     public HomeController(ExcelService excelService, RenderService renderService, EmailService emailService) {
         this.excelService = excelService;
         this.renderService = renderService;
         this.emailService = emailService;
     }
 
     @RequestMapping(value = "/upload", method = RequestMethod.POST)
     public
     @ResponseBody
     String convertToPDf(@RequestPart("file") MultipartFile excelFile) throws IOException {
         File file = new File(excelFile.getName());
         excelFile.transferTo(file);
         final List<SalarySlip> salarySlips = excelService.fetchSalarySlips(file);
         final List<String> personNames = renderService.renderPDF(salarySlips, path);
 
         return personNames.toString();
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
                emailService.sendMail(file, emailId, "Salary Slip", "Hi User, hope you get paid!!!");
             }
         }
        return "E-Mail Sent to "+ emailId;
     }
 }
