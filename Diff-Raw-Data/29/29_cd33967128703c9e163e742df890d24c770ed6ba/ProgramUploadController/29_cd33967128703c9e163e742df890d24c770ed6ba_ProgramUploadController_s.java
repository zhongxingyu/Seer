 package by.bsu.project.mvc;
 
 import by.bsu.project.entity.ProgramFilesEntity;
 import by.bsu.project.entity.UserInfoEntity;
 import by.bsu.project.model.SpringUser;
 import by.bsu.project.service.UserInfoService;
 import by.bsu.project.validator.Validator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Alina Glumova
  */
 
 @Controller
 public class ProgramUploadController {
 
     @Autowired
     private UserInfoService userInfoService;
     private List<String> errors;
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         // Convert multipart object to byte[]
         binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
     }
 
     @RequestMapping(value = "/e-Testing/UploadProgram")
     public ModelAndView displayUploadFile(
             UserInfoEntity userInfoEntity,
             ProgramFilesEntity programFilesEntity,
             Model model) throws Exception {
 
         SpringUser user = (SpringUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         userInfoEntity = userInfoService.getStudentById(user.getId());
         programFilesEntity = new ProgramFilesEntity();
         model.addAttribute("student", userInfoEntity);
         return new ModelAndView("UploadProgram", "program", programFilesEntity);
     }
 
     @RequestMapping(value = "/e-Testing/SaveProgram")
     public ModelAndView processUploadPreview(
             @RequestParam(value = "file", required = false) MultipartFile file,
             @RequestParam(value = "studentId", required = false) Long studentId,
             @ModelAttribute("UploadProgram") ProgramFilesEntity programFilesEntity,
             Model model) throws Exception {
 
         String status;
 
         UserInfoEntity userInfoEntity = userInfoService.getStudentById(studentId);
         errors = Validator.validateFile(file, programFilesEntity.getProgramName());
 
         if (errors.size() != 0) {
             model.addAttribute("errors", errors);
             model.addAttribute("student", userInfoEntity);
             return new ModelAndView("UploadProgram", "program", programFilesEntity);
         }
 
         if (checkCppFile(file)) {
             status = "passed";
         } else status = "fail";
 
         programFilesEntity.setFile(file.getBytes());
         programFilesEntity.setFileName(file.getOriginalFilename());
         programFilesEntity.setContentType(file.getContentType());
         programFilesEntity.setStatus(status);
         userInfoEntity.getProgramFiles().add(programFilesEntity);
 
         userInfoService.save(userInfoEntity);
         return new ModelAndView("redirect:/e-Testing/UploadProgramStatus.html?status=" + status);
     }
 
     @RequestMapping(value = "/e-Testing/UploadProgramStatus")
     public ModelAndView processUploadPreview(
             @RequestParam(value = "status", required = false) String status) {
         return new ModelAndView("UploadProgramStatus", "status", status);
     }
 
     private boolean checkCppFile(MultipartFile program) throws Exception {
 
         File file = new File("C:/files/" + program.getOriginalFilename());
 
         program.transferTo(file);
 
         Process p = Runtime.getRuntime().exec("cmd /C C:/Dev-Cpp/bin/c++.exe C:/files/" + program.getOriginalFilename());
         p.waitFor();
 
         System.out.println(p.getOutputStream().toString());
 
         String[] cmd = {"C:/tomcat/bin/a.exe"};
         Process p1 = Runtime.getRuntime().exec(cmd);
         p1.waitFor();
 
         return validate();
     }
 
     private boolean validate() throws Exception {
         File out = new File("C:/tomcat/bin/out.txt");
         File right = new File("C:/tomcat/bin/hello.txt");
         List<String> out1 = getList(out);
         List<String> right1 = getList(right);
         if (out1.size() != right1.size()) {
             return false;
         }
         for (int i = 0; i < out1.size(); ++i) {
             if (!out1.get(i).equals(right1.get(i))) {
                 return false;
             }
         }
         return true;
     }
 
     private List getList(File file) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
         List<String> result = new ArrayList<String>();
         String line = br.readLine();
         while (line != null) {
             result.add(line);
             line = br.readLine();
         }
 
         br.close();
         return result;
     }
 
 }
