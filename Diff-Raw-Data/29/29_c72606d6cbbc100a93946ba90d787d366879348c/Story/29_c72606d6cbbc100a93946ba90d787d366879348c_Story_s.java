 package controllers;
 
 import java.util.Iterator;
 
 import models.Fibonacci;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 import com.orientechnologies.orient.core.id.ORecordId;
 
 /**
  * Created with IntelliJ IDEA.
  * User: han
  * Date: 11/7/12
  * Time: 11:09 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class Story extends Controller {
 	
 	@Before(only={"save","create"})
 	static void validate(){
 		validation.required("story_name", params.get("story_name"));
 		validation.match("story_storyPoints", params.get("story_storyPoints"), "(0|1|2|3|5|8|13|21|40|100)").message("Story points must be a valid Fibonacci number");
		validation.match("story_actualHours", params.get("story_actualHours"), "[\\d]*").message("Actual Hours must be a number");
 		
 		if(validation.hasErrors()) {
 			params.flash();
 		}
 	}
 	
	@Before
	static void clearMessage() {
		flash.clear();
	}
	
 	public static void add(ORecordId iteration_id) {
         models.Iteration iteration = models.Iteration.findById(iteration_id);
         if(iteration == null) {
         	iteration = new models.Iteration();
         }
         iteration.name = params.get("iteration_name");
         iteration.description = params.get("iteration_description");
         
         models.Story story = new models.Story();
         
 		CRUD mode = CRUD.CREATE;
 		renderTemplate("Story/story.html", mode, iteration, story);
 	}
 	
     public static void create(ORecordId iteration_id) {
     	CRUD mode = CRUD.UPDATE;
     	
     	models.Iteration iteration = models.Iteration.findById(iteration_id);
     	flash.put("iteration_description", iteration.description);
     	flash.put("iteration_name", iteration.name);
     	
     	models.Story story = new models.Story();
     	story.name = params.get("story_name");
     	story.description = params.get("story_description");
     	try {
     		story.storyPoints = Fibonacci.valueOf(Integer.parseInt(params.get("story_storyPoints")));
    		story.actualHours = Integer.parseInt(params.get("story_actualHours"));
    	}catch(NumberFormatException nfe) {}
 
     	iteration.stories.add(story);
     	
     	if(validation.hasErrors()) {
     		mode = CRUD.CREATE;
     		renderTemplate("Story/story.html", mode, iteration, story);
     		return;
     	}
     	
         iteration.save();
         
         flash.put("message","Story created");
         renderTemplate("Iteration/iteration.html", mode, iteration);
     }
 
     public static void save(ORecordId id, ORecordId iteration_id) {
     	CRUD mode = CRUD.UPDATE;
     	
     	models.Iteration iteration = models.Iteration.findById(iteration_id);
     	flash.put("iteration_description", iteration.description);
     	flash.put("iteration_name", iteration.name);
  
     	models.Story story = models.Story.findById(id);
     	story.name = params.get("story_name");
         story.description = params.get("story_description");
         try {
	        story.actualHours = Integer.parseInt(params.get("story_actualHours"));
 	        story.storyPoints = Fibonacci.valueOf(Integer.parseInt(params.get("story_storyPoints")));
        }catch(NumberFormatException nfe) {}
         
         populateFlash(story);
 
         if(validation.hasErrors()) {
     		renderTemplate("Story/story.html", mode, iteration, story);
     		return;
     	}
         
         story.save();
         
         flash.put("message","Story saved");
         renderTemplate("Iteration/iteration.html", mode, iteration);
     }
 
     public static void edit(ORecordId id, ORecordId iteration_id) {
     	CRUD mode = CRUD.UPDATE;
     	 
         models.Iteration iteration = models.Iteration.findById(iteration_id);
         flash.put("iteration_id", iteration.getIdentity());
         
         models.Story story = models.Story.findById(id);
        
         populateFlash(story);
          
         renderTemplate("Story/story.html", mode, iteration, story);
     }
 
     public static void delete(ORecordId id, ORecordId iteration_id) {
     	CRUD mode = CRUD.UPDATE;
     	
     	models.Iteration iteration = models.Iteration.findById(iteration_id);
     	flash.put("iteration_description", iteration.description);
     	flash.put("iteration_name", iteration.name);
     	
     	Iterator<models.Story> it = iteration.stories.iterator();
     	
     	while(it.hasNext()) {
     		models.Story story = it.next();
     		if(id.equals(story.getIdentity())) {
     			it.remove();
     			story.delete();
     		}
     	}
 
         flash.put("message","Story deleted");
         
         renderTemplate("Iteration/iteration.html", mode, iteration);
     }
     
     public static void deleteSelected(ORecordId iteration_id, String[] selected) {
     	
     	CRUD mode = CRUD.UPDATE;
     	flash.clear();
     	models.Iteration iteration = null;
     	
     	if(selected != null && iteration_id != null) {
     		iteration = models.Iteration.findById(iteration_id);
         	flash.put("iteration_description", iteration.description);
         	flash.put("iteration_name", iteration.name);
 
     		for(int i=0;i<selected.length;i++) {
     			Iterator<models.Story> it = iteration.stories.iterator();
     			while(it.hasNext()) {
     				models.Story story = it.next();
         			if(story.getIdentity().toString().equals(selected[i])) {
         				it.remove();
         			}
     			}
     		}		
     		
     		iteration.save();
     		
         	flash.put("message","Stories deleted");
         	
     	} else {
     		flash.put("error","No items selected!");
     	}
     	
 		renderTemplate("Iteration/iteration.html", mode, iteration);
 	}
     
     private static void populateFlash(models.Story story) {
     	flash.put("story_name", story.name);
     	flash.put("story_description", story.description);
     	flash.put("story_storyPoints", story.storyPoints);
     	flash.put("story_actualHours", story.actualHours);
     }
 }
