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
        RootLeaf leaf = RootLeaf.findWithUuidLatestPublishedVersion(uuid, new Date());
 
        render(leaf);
     }
 
     //@Get("/rootLeaf/{uuid}/all")
     public static void leafVersions(@Required String uuid) {
 
         List<RootLeaf> leaves = RootLeaf.findWithUuidAllVersions(uuid);
 
         render(leaves);
     }
 
     //@Get("/rootLeaf/{uuid}/{<[0-9]+>version}")
     public static void leafVersion(@Required String uuid, @Required Long version) {
 
        RootLeaf leaf = RootLeaf.findWithUuidSpecificVersion(uuid, version);
 
        render(leaf);
     }
 
 }
