 package controllers;
 
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.With;
 import play.vfs.VirtualFile;
 
 import java.util.List;
 
 @With(OnlyInDevMode.class)
 public class Mockups extends Controller {
 
     public static void showList(){
         List<VirtualFile> mockups = Play.getVirtualFile("/app/views/Mockups").list();
         render(mockups);
     }
 
     public static void showMockup(String mockup){
        render("/Mockups/" + mockup + ".html");
     }
 }
