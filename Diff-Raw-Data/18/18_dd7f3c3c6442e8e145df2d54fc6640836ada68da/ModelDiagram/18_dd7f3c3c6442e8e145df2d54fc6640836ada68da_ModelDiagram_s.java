 package controllers.modeldiagram;
 
 import com.google.gson.GsonBuilder;
 
 import play.mvc.*;
 import plugins.ModelDiagramPlugin;
 
 public class ModelDiagram extends Controller {
 
     public static void index() {

         String json = new GsonBuilder().setPrettyPrinting().create().toJson(ModelDiagramPlugin.models);
         render(json);
     }
 
 }
