 package controllers;
 
 import controllers.abstracts.UtilController;
import controllers.security.LoggedAccess;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class Application extends UtilController {
 
     private static final String APPLICATION_NAME = "Ramses";
 
     private static final String APPLICATION_VERSION = "0.1";
 
     @Before
     public static void initGlobalValues() {
         renderArgs.put("application_name", APPLICATION_NAME);
         renderArgs.put("application_version", APPLICATION_VERSION);
     }

    @LoggedAccess
    public static void about() {

    }

    @LoggedAccess
    public static void reportBug() {

    }
 }
