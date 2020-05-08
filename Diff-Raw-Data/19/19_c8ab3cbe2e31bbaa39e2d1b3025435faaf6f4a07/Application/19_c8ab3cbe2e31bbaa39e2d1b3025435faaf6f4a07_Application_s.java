 package controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.List;
 
 import org.junit.experimental.categories.Categories.IncludeCategory;
 
 import controllers.CRUD.ObjectType;
 
 import models.Incident;
 import models.IncidentCategory;
 import models.Location;
 import models.User;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.mvc.Controller;
 import services.Ushahidi;
 
 public class Application extends Controller 
 {
     public static final Ushahidi USHAHIDI = new Ushahidi("https://simpelers.crowdmap.com/api", "andrew@tillnow.com", "qazwsx");
     private static Calendar CALENDAR = Calendar.getInstance();
 
     public static void index() 
     {
     	try {
     		List<Incident> all = Incident.find("order by incidentDate desc").fetch();
     		
 			render(all);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void newReport() throws Exception
     {
         Incident incident = new Incident();
         try {
            render(Incident.class, incident);
         } catch (TemplateNotFoundException e) {
         	e.printStackTrace();
         }
     }
     
    public static void saveReport(String title, String content, String direction) throws Exception
     {
    	//TODO fix me!
    	Incident i = new Incident((IncidentCategory) IncidentCategory.find("byName", "Gunshot").first(),
     			title,
     			content,
     			CALENDAR.getTime(),
     			0,
    			(Location) Location.find("byName", "Champ de Mars").first(),
     			direction,
    			(User) User.find("byFirstName", "john").first());
         
         // Validate
         validation.valid(i);
         if(validation.hasErrors()) {
             render("@form", i);
         }
         // Save
         i.save();
         index();
     }
 }
