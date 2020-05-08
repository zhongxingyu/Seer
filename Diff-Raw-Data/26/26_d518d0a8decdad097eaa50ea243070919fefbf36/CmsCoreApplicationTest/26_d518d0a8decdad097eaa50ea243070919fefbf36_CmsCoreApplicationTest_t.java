 package controllers.cmscore;
 
 import models.cmscore.Page;
 import play.mvc.Controller;
 
 import java.util.Date;
 import java.util.List;
 
 public class CmsCoreApplicationTest extends Controller {
 
     public static void index() {
         List<Page> pages = Page.findAllCurrentVersions(new Date());
         render(pages);
     }

    public static void page(String uuid) {
        Page page = Page.findCurrentVersion(uuid, new Date());
        render(page);
    }
    
 }
