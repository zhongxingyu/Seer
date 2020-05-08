 package controllers.cmscore;
 
 import models.cmscore.Leaf;
 import play.data.validation.Required;
 import play.mvc.Controller;
 
 import java.util.List;
 
 public class Core extends Controller {
 
     public static void leafList() {
        List<Leaf> leaves = Leaf.findAllCurrentVersions();
 
        render(leaves);
     }
 
     public static void leaf(@Required String uuid){
 
         //Load leafModel
 
         //Find all classes with BeforeLeafLoaded watches
         //LeafHelper.dispatchBeforeLoaded
 
 
         Leaf leaf = Leaf.findWithUuidLatestVersion(uuid);
 
         render(leaf);
     }
 
     public static void leafVersions(@Required String uuid){
 
        List<Leaf> leaves = Leaf.findWithUuidAllVersions(uuid);
 
        render(leaves);
     }
 
     public static void leafVersion(@Required String uuid, @Required int version){
 
         Leaf leaf = Leaf.findWithUuidSpecificVersion(uuid, version);
 
         render(leaf);
     }
 }
