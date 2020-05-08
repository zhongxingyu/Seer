 package models.user;
 
 import java.util.List;
 import java.util.Arrays;
 
 import models.EMessages;
 import models.data.Link;
 
 /**
  * A class with all possible roles a user can have. These roles will also
  * influence the links on a user's landing page.
  * @author Ruben Taelman
  * @author Felix Van der Jeugt
  */
 public class Role {
 
 
     /* ====================================================================== *\
                                Role definitions.
     \* ====================================================================== */
 
     // Mimicing
     public static Role MIMIC = new Role("Mimick user",
    		new Link(EMessages.get("links.mimic"),"/mimic")
     );
 
     // Anon
     public static Role LOGIN = new Role();
     public static Role REGISTER = new Role();
 
     // Authenticated
     public static Role LANDINGPAGE = new Role();
     public static Role SETTINGS = new Role("links.settings.title",
        new Link(EMessages.get("links.settings.editinfo"), "/settings/editinfo"),
        new Link(EMessages.get("links.settings.changepassword"), "/settings/passwedit")
     );
 
     // Organiser
     public static Role MANAGEQUESTIONS = new Role(
         "links.managequestions.title",
         new Link("links.managequestions.list", "/questions"),
         new Link("links.managequestions.listsubmitted", "/questionsubmits")
     );
     public static Role MANAGESERVERS = new Role(
         "links.manageserver.title",
         new Link("links.manageserver.list", "/servers"),
         new Link("links.manageserver.create", "/server/new")
     );
 
     // Author
     public static Role QUESTIONEDITOR = new Role(
         "links.questioneditor.title",
         new Link("links.questioneditor.open", "/questioneditor")
     );
 
     //ADMIN
     public static Role MANAGEFAQ = new Role(
         "faq.managefaq",
         new Link("faq.title", "/faq"),
         new Link("faq.managefaq", "/manageFAQ"),
         new Link("faq.addfaq", "/manageFAQ/new")
     );
     
     public static Role DATAMANAGER = new Role(
         "links.datamanager.title",
         new Link("links.datamanager.links", "/manage/links/show"),
         new Link("links.datamanager.diffs", "/manage/difficulties/show"),
         new Link("links.datamanager.grades", "/manage/grades/show")
     );
     
     //TEACHER
     public static Role MANAGESCHOOLS = new Role(
         "schools.title",
 	    new Link("schools.title", "/schools")
     );
 
     public static Role MANAGECLASSES = new Role(
 	    "classes.list",
 	    new Link("classes.list", "/classes")
     );
 
     // Contest management
     public static Role MANAGECONTESTS = new Role(
         EMessages.get("links.contestmanager.title"),
        new Link(EMessages.get("links.contestmanager.overview"), "/contests")
     );
 
 
 
     /* ====================================================================== *\
                              Actual implementation.
     \* ====================================================================== */
 
     private boolean landing;
     private String mtitle;
     private List<Link> pages;
 
     /**
      * Create a new Role which gives no links on the landing page.
      */
     private Role() {
         this.landing = false;
         this.mtitle  = null;
         this.pages   = null;
     }
 
     /**
      * Create a new Role.
      * @param mtitle The title for this role, as a EMessage string.
      * @param pages The pages this roles gives access to.
      */
     private Role(String mtitle, Link... pages) {
         this(mtitle, Arrays.asList(pages));
     }
 
     /**
      * @see models.user.Role.Role
      */
     private Role(String mtitle, List<Link> pages) {
         this.landing = true;
         this.mtitle  = mtitle;
         this.pages   = pages;
     }
 
     /**
      * Returns whetter this Role should show up on the landing page of the user.
      * @return On the landing page?
      */
     public boolean onLandingPage() {
         return landing;
     }
 
     /**
      * Returns the title as a translated human readable string.
      * @return The role's title.
      */
     public String title() {
         return EMessages.get(mtitle);
     }
 
     /**
      * Returns links to the pages this Role gives access to.
      * @return Accessable pages with this Role.
      */
     public List<Link> pages() {
         return pages;
     }
 
 }
