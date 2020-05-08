 package controllers;
 
 import fs.FileFinder;
 import fs.Files;
 import models.User;
 import play.Play;
 import play.mvc.Before;
import play.mvc.Catch;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
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
        renderArgs.put("updateInfo",updateInfo());
         render(files);
     }
 
    private static String updateInfo()  {
        StringBuilder content = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File("public/data/update.html"));
            while (scanner.hasNextLine())
                content.append(scanner.nextLine());

        } catch (FileNotFoundException e) {

        }
        return content.toString();
    }

 }
