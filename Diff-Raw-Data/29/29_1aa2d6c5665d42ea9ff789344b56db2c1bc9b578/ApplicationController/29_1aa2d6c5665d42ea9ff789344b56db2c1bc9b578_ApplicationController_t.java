 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 @With({Security.class})
 public class ApplicationController extends Controller {
 	
 	private static Edition selectedEdition; //this is populated in getBrowseData()
 	
 	@Before 
 	static void getJournalConfig() {
 		JournalConfiguration jc = JournalConfiguration.all().first();
 		renderArgs.put("jc", jc);	
 	}
 	
    public static void index() {
         List<Volume> volumes = Volume.find(
             "order by ID desc"
         ).fetch(5); //this repeats db values if not enough entries in db. Solutions?
 		String user = Security.connected();
 		Volume selectedVolume = volumes.get(0);
 		List<Edition> editions = selectedVolume.getEditions();
		Edition selectedEdition = editions.get(editions.size()-1);
 		List<Published> publishedArticles = selectedEdition.getPublished();
 
         render(volumes, editions, publishedArticles);
    }
 }
