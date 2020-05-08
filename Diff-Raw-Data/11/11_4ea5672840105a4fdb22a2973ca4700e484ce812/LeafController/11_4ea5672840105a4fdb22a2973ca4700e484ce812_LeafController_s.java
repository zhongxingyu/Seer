 package controllers.cmscore;
 
 import models.cmscore.RootLeaf;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 import java.util.Date;
 import java.util.List;
 
 public class LeafController extends Controller {
 
     //@Get("/rootLeaf")
     public static void leafList() {
 
         //Load leafModel
         List<RootLeaf> leaves = RootLeaf.findAllCurrentVersions(new Date());
 
         render(leaves);
     }
 
     //@Get("/rootLeaf/{uuid}")
     public static void leaf(@Required String uuid) {
 
         //Load leafModel
        RootLeaf rootLeaf = RootLeaf.findWithUuidLatestPublishedVersion(uuid, new Date());
 
        render(rootLeaf);
     }
 
     //@Get("/rootLeaf/{uuid}/all")
     public static void leafVersions(@Required String uuid) {
 
         List<RootLeaf> leaves = RootLeaf.findWithUuidAllVersions(uuid);
 
         render(leaves);
     }
 
     //@Get("/rootLeaf/{uuid}/{<[0-9]+>version}")
     public static void leafVersion(@Required String uuid, @Required Long version) {
 
        RootLeaf rootLeaf = RootLeaf.findWithUuidSpecificVersion(uuid, version);
 
        render(rootLeaf);
     }
 
 }
