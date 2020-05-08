 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 @With({ApplicationController.class, Security.class})
 public class BrowseController extends Controller {
 	
 	public static Edition selectedEdition;	
 	
     public static void index(Long selectedVolumeID, Long selectedEditionID) {
 				
 		//check for case when no volumes or no editions
 		
 
 		//Find all volumes
 		List<Volume> volumes = Volume.find("order by ID desc").fetch();
 		
 		Volume selectedVolume; 
 		
 		//If no volume id passed than select first found volume
 		if(selectedVolumeID==null){
 			selectedVolumeID = volumes.get(0).id;
 			selectedVolume = volumes.get(0);	
 		}
 		else {
 			selectedVolume = Volume.findById(selectedVolumeID);	
 		}
 		
 		if(selectedVolume!=null) {
 			Edition selectedEdition = null;
 			
 			//If no edition id passed, select first edition of selected volume
 			if(selectedEditionID==null) {
 				List<Edition> editions = selectedVolume.getEditions();
 				if(editions.size()>1) {
 					selectedEdition = editions.get(0); 
 					selectedEditionID = selectedEdition.id;
 				}
 			}
 			else selectedEdition = Edition.findById(selectedEditionID);
 			
 			List<Published> publishedArticles = null;
 			
 			if(selectedEdition!=null)  publishedArticles = selectedEdition.getPublished();
 					
 			render(volumes, publishedArticles, selectedVolumeID, selectedEditionID);
 		}
 		
 	}	
 
     public static void journalGoals() {
 		JournalConfiguration jc  = JournalConfiguration.all().first();
     	render("BrowseController/journalGoals.html", jc);	
 	}	
 
     public static void templates() {
 		JournalConfiguration jc  = JournalConfiguration.all().first();
     	render("BrowseController/templates.html", jc);
 	}	
 }
