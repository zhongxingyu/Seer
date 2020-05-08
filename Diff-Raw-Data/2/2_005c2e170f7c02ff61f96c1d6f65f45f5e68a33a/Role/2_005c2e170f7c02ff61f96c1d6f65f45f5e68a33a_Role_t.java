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
 public class Role implements Comparable<Role>{
 
 
     /* ====================================================================== *\
                                Role definitions.
     \* ====================================================================== */
 
     // Mimicing
    public static Role MIMIC = new Role("links.mimic",
         new Link("links.mimic","/mimic")
     );
 
     // Anon
     public static Role LOGIN = new Role();
     public static Role REGISTER = new Role();
 
     // Authenticated
     public static Role LANDINGPAGE = new Role();
     public static Role SETTINGS = new Role("links.settings.title",
         new Link("links.settings.editinfo", "/settings/editinfo"),
         new Link("links.settings.changepassword", "/settings/passwedit")
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
 
     public static Role MANAGEUSERS = new Role(
         "links.manageusers.title",
         new Link("links.manageusers.users","/manage/users")
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
         "links.contestmanager.title",
         new Link("links.contestmanager.overview", "/contests"),
         new Link("links.contestmanager.create", "/contests/new/contest")
     );
 
     // Contest view
     public static Role VIEWCONTESTS = new Role(
         "links.contestmanager.title",
         new Link("links.contestmanager.overview", "/contests")
     );
 
     /* Statistic Roles */
     public static Role VIEWSTATS = new Role(
         "statistics.title",
         new Link("links.statistics", "/statistics")
     );
 
     // Contest taking
     public static Role TAKINGCONTESTS = new Role(
         "links.contesttaking.title",
         new Link("links.contesttaking.overview", "/available-contests"),
         new Link("links.contesttaking.history", "/available-contests/history")
     );
     
     //Classes view for pupils
     public static Role PUPILCLASSVIEW = new Role(
         "classes.pupil.classes.list",
         new Link("classes.pupil.classes.list","/pclasses/view")
     );
     
     //Upgrade to teacher request
     public static Role UPGRADETOTEACHER = new Role(
     		"contact.upgraderequest",
     		new Link("contact.upgraderequest","/upgrade")
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
 
     @Override
     public int compareTo(Role o) {
         if(this.mtitle == null) return -1;
         if(o.mtitle == null) return 1;
         return this.mtitle.compareTo(o.mtitle);
     }
 
 }
