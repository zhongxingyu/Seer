 package com.bajoneando.lnramirez.web.controllers;
 
 import com.bajoneando.lnramirez.files.MongoStoredFile;
 import com.bajoneando.lnramirez.files.services.FileService;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author lrmonterosa
  */
 @Controller
 @RequestMapping("/images")
 public class ImagesController {
     
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView get() {
         ModelAndView modelAndView = new ModelAndView("/images/upload");
         modelAndView.addObject("mongoStoredFile", new MongoStoredFile());
         return modelAndView;
     }
     
     @RequestMapping(value="/upload", method=RequestMethod.POST, headers="Accept=application/json")
     @ResponseStatus(HttpStatus.OK)
     public void upload(@RequestParam("file") MultipartFile multipartFile, final HttpServletResponse response) throws IOException {
         if (multipartFile == null) {
             throw new RuntimeException("file null");
         }
         MongoStoredFile image = new MongoStoredFile();
         image.setName(multipartFile.getOriginalFilename());
         image.setContentType(multipartFile.getContentType());
         image.setData(multipartFile.getBytes());
         fileService.save(image);
     }
     
     @RequestMapping(value="/download/{objectId}", method=RequestMethod.GET)
     public void download(final HttpServletResponse response, 
             @PathVariable("objectId") String objectId) throws IOException {
         OutputStream outputStream = response.getOutputStream();
         MongoStoredFile mongoStoredFile = fileService.getFile(objectId);
         response.setContentType(mongoStoredFile.getContentType());
         FileCopyUtils.copy(mongoStoredFile.getInputStream(),outputStream);
         outputStream.flush();
         outputStream.close();
     }
     
     @RequestMapping(value="/list/{page}", method=RequestMethod.GET, headers="Accept=application/json")
     @ResponseBody
     public List<MongoStoredFile> getImages(@PathVariable("page") Integer page) {
         Pageable pageable = new PageRequest(page, 5);
         return fileService.getFiles(pageable);
     }
     
     @Autowired private FileService fileService;
     
 }
