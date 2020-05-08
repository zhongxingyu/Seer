 package web.servlets;
 
 import galleryService.interfaces.ImageService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import persistence.exception.ValidationException;
 import persistence.struct.User;
 import web.utils.JSPUtils;
 import web.utils.SessionUtils;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: agnidash
  * Date: 4/25/13
  * Time: 1:38 PM
  */
 @MultipartConfig
 @Controller
 @RequestMapping("/ImageUpload")
 public class ImageUpload extends HttpServlet {
 
     //Session params
     public static final String UPLOAD_MESSAGE = "upload_message";
 
     //Action
     public static final String UPLOAD = "upload";
 
     //Fields
     public static final String NAME = "name";
     public static final String COMMENT = "comment";
     public static final String PATH = "path";
 
     //Logger
     private static final Logger LOGGER = Logger.getLogger(ImageUpload.class);
 
     @Required
     public void setService(ImageService service) {
         this.service = service;
     }
 
     private ImageService service;
 
     @RequestMapping(method = RequestMethod.GET)
     protected void doGet(HttpServletRequest in, HttpServletResponse out) throws ServletException, IOException {
         /*
          Ignore.
          */
         JSPUtils.showHomePage(out);
     }
 
     @RequestMapping(method = RequestMethod.POST)
     protected void doPost(HttpServletRequest in, HttpServletResponse out,
                           @RequestParam(value = PATH) MultipartFile file) throws ServletException, IOException {
         /*
         Always remove upload error message
         */
         SessionUtils.removeAttribute(in, UPLOAD_MESSAGE);
 
         byte[] data = loadFile(file);
 
         if (data != null) {
             saveImage(in, out, data);
 
         } else {
             onFail(in, out, "can't upload file. Some error occurs.");
         }
     }
 
     private byte[] loadFile(MultipartFile file) {
         byte[] image = null;
         try {
             image = file.getBytes();
         } catch (IOException e) {
             LOGGER.warn("Image can't be uploaded. ", e);
         }
         return image;
     }
 
     private void saveImage(HttpServletRequest in, HttpServletResponse out, byte[] data) throws IOException {
         User user = getUser(in);
 
         String name = in.getParameter(NAME);
         String comment = in.getParameter(COMMENT);
 
         ImageService imageService = getService();
 
         try {
             imageService.addImage(user, name, comment, data);
 
             onSuccess(in, out);
         } catch (ValidationException e) {
             onFail(in, out, e.getMessage());
 
         } catch (IOException e) {
             onFail(in, out, e.getMessage());
 
         } catch (RuntimeException e) {
            e.printStackTrace();

             LOGGER.error("Failed on upload image. ", e);
 
             onFail(in, out, e.getMessage());
         }
     }
 
     private User getUser(HttpServletRequest in) {
         return (User) SessionUtils.getAttribute(in, LogIn.USER);
     }
 
     private void onSuccess(HttpServletRequest in, HttpServletResponse out) throws IOException {
         addMessage(in, "File uploaded");
 
         JSPUtils.showHomePage(out);
     }
 
     private void onFail(HttpServletRequest in, HttpServletResponse out, String message) throws IOException {
         addMessage(in, "Upload failed because " + message);
 
         JSPUtils.showHomePage(out);
     }
 
     private void addMessage(HttpServletRequest in, String message) {
         SessionUtils.addAttribute(in, UPLOAD_MESSAGE, message);
     }
 
     public ImageService getService() {
         return service;
     }
 
 
 }
