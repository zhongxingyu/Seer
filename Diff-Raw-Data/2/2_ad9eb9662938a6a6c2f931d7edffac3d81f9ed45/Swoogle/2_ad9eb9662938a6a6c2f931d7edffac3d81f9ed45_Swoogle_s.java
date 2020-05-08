 package controllers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.Play;
 import play.libs.Akka;
 import play.libs.Json;
 import play.libs.WS;
 import play.libs.WS.Response;
 import play.libs.WS.WSRequestHolder;
 import play.mvc.Controller;
 import play.mvc.Result;
 import service.SwoogleREST;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import common.DownloadUtils;
 import common.FileUtils;
 import common.Utils;
 
 public class Swoogle extends Controller {
   private static final String swoogleOntologyPath = initPath();
   
   public static Result submit(String keyword) {
     if(Utils.isBlank(keyword) || keyword.split(" ").length > 1) {
       return(badRequest(Json.newObject()));
     }
     
     final String validatedKeyword = keyword.trim();
     
     return(async(Akka.future(new Callable<Result>() {
 
       @Override
       public Result call() throws Exception {
         
         final String keyword = validatedKeyword;
 
         /* Stat variables */
         int totalCount = 0;
         int downloadFailedCount = 0;
         int not200Count = 0;
         int unparsableOntCount = 0;
         int duplicateCount = 0;
         
         Set<String> tbDownloaded = SwoogleREST.searchOntologySync(keyword);
         List<WSRequestHolder> wsList = new ArrayList<WSRequestHolder>();
         for(String downl: tbDownloaded) {
           wsList.add(WS.url(downl));
         }
         List<Response> responses = DownloadUtils.concurrentDownload(wsList);
         List<Response> validResponses = DownloadUtils.removeBadResponses(responses);
         
         totalCount = tbDownloaded.size();
         downloadFailedCount = totalCount - responses.size();
         not200Count = responses.size() - validResponses.size();
         
         try {
           FileUtils.deleteDirectory(new File(swoogleOntologyPath));
         } catch (Exception e) {
           Logger.error("Couldn't empty ontology directory!", e);
         }
         
         Set<String> md5s = new HashSet<>();
         
         for(Response r: validResponses) {
           final String uri = r.getUri().toString();
           try {
             Model model = ModelFactory.createOntologyModel();
             model.read(r.getBodyAsStream(), r.getUri().toString());
           } catch (Exception e) {
             Logger.error("Error parsing file as ontology " + uri, e);
             unparsableOntCount++;
             continue;
           }
           
           try {
             String folder = r.getUri().getHost();
             String name = r.getUri().getPath();
             if(name.startsWith(File.separator)) {
               name = name.substring(1);
             }
            name.replace(File.separator, "_");
             File target = new File(swoogleOntologyPath + File.separator + folder + File.separator + name);
             FileUtils.touch(target);
 
             String md5 = FileUtils.writeAndMD5(r.getBodyAsStream(), target);
             if(md5s.contains(md5)) {
               Logger.error("Duplicate ontology! MD5 match: " + r.getUri().toString() + " md5: " + md5);
               duplicateCount++;
               target.delete();
               continue;
             }
             md5s.add(md5);
           } catch (Exception e) {
            Logger.error("Runtime while saving ontology to file: " + uri, e);
           }
         }
         
         Logger.info("***STATS FOR SWOOGLE!***");
         Logger.info("Total found count: " + totalCount);
         Logger.info("Failed download count: " + downloadFailedCount);
         Logger.info("Downloaded but not 200 count: " + not200Count);
         Logger.info("Duplicate count: " + duplicateCount);
         Logger.info("Downloaded but not ontology count: " + unparsableOntCount);
         Logger.info("OK count: " + String.valueOf(totalCount - downloadFailedCount - not200Count - duplicateCount - unparsableOntCount));
 
         ObjectNode result = Json.newObject();
         result.put("status", "OK");
         result.put("total", totalCount);
         result.put("failed",  downloadFailedCount);
         result.put("not200", not200Count);
         result.put("duplicate", duplicateCount);
         result.put("notOntology", unparsableOntCount);
         result.put("ok", totalCount - downloadFailedCount - not200Count - duplicateCount - unparsableOntCount);
         return ok(result);        
       }
     })));
   }
 
   private static String initPath() {
     final String swooglePathFromConf = Play.application().configuration().getString("swoogle.ontologyPath");
     return Play.application().path() + File.separator + swooglePathFromConf;
   }
 }
