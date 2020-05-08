 package controllers.modeldiagram;
 
 import com.google.gson.GsonBuilder;
 
import play.Play;
 import play.mvc.*;
 import plugins.ModelDiagramPlugin;
 
 public class ModelDiagram extends Controller {
 
     public static void index() {
    	if(Play.mode.isDev())
    	{
         String json = new GsonBuilder().setPrettyPrinting().create().toJson(ModelDiagramPlugin.models);
         render(json);
    	}
    	else
    	{
    		notFound();
    	}
     }
 
 }
