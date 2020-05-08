 package controllers;
 
 import controllers.cms.Admin;
 import models.Agenda;
 import models.cms.CMSPage;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Controller that managed principal page of the site.
  */
 public class Application extends AbstractController {
 
     /**
      * Home page.
      */
     public static void index() {
         List<Agenda> agendas = Agenda.find("date > ? ORDER BY DATE ASC", new Date()).fetch(5);
         render(agendas);
     }
 
     /**
      * Home page.
      */
     public static void blog() {
        CMSPage page = CMSPage.getLastest("blog");
         render("cms/blog.html", page);
     }
 
     /**
      * Admin page.
      */
     public static void admin(){
         // check if it's an admin user
         isAdminUser();
 
         Admin.index("page");
     }
 
 }
