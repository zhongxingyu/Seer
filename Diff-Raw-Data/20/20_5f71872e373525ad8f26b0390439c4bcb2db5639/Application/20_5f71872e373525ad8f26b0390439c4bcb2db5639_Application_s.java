 package controllers;
 
 import fs.FileFinder;
 import fs.Files;
 import models.User;
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.io.File;
 import java.util.regex.Pattern;
 
 @With(Secure.class)
 public class Application extends Controller {
 
     private static String filesRootPath = Play.configuration.getProperty("files.dir");
     private static File pathToFiles = new File(Play.applicationPath, filesRootPath);
 
     @Before
     static void setConnectedUser() {
         if(Secure.Security.isConnected()) {
             User user = User.find("byUsername", Secure.Security.connected()).first();
             renderArgs.put("user", user.fullname);
         }
     }
 
     public static void index() {
         Files files = new FileFinder(pathToFiles).find(Pattern.compile(".*\\.rtf$"));
         renderArgs.put("filesRootPath", filesRootPath);
         render(files);
     }
 
 }
