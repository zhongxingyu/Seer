 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.mvc.Scope.Session;
 import support.LogWriter;
 
 import java.lang.reflect.Field;
 import java.util.*;
 
 import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import core.ClickStreamFilter;
 import core.DataFinder;
 import core.DataFinder.ConditionTypes;
 import core.DataWorker;
 
 import models.*;
 
 public class StreamEvents extends Controller {
 	
 	public static void index() {
 		render();
 	}
 	
 	public static void addEvent(JsonObject body) {
 	    	
 	    	ClickStreamEvent e = handleJsonAsObject(body);
 	    	if (e== null)
 	    	{
 	    		//render("Empty!!!");
 	    		render(body);
 	    		// add error message
 	    		return;
 	    	}
 	    	e.sessionId = Session.current().getId();
 	   
 	    	e.Save(); 
 	    } 
 	  
 	public static void handleJson(JsonObject body) 
 	{        
 	    	renderText(body.getAsJsonPrimitive("name").getAsString());    
 	}        
 	
 	public static ClickStreamEvent handleJsonAsObject(JsonObject body) 
 	{        
 		ClickStreamEvent e = new Gson().fromJson(body, ClickStreamEvent.class);        
 		return e; 
 	}
 	
 	public static void Find(JsonObject body)
 	{
 		ClickStreamFilter filter = new Gson().fromJson(body, ClickStreamFilter.class);
 
 		ArrayList<ClickStreamEvent> array = new ArrayList<ClickStreamEvent>();
 		DataFinder finder = new DataFinder(ClickStreamEvent.class).OrderByIdDesc();
 		try 
 		{
 			if (!filter.elementType.isEmpty())
 			{
 				finder = finder.Where("elementType", ConditionTypes.Equals, filter.elementType);
 			}
 			
 			if (!filter.elementId.isEmpty())
 			{
 				finder = finder.Where("elementId", ConditionTypes.Equals, filter.elementId);
 			}
 			
 			if (!filter.elementClass.isEmpty())
 			{
 				finder = finder.Where("elementClass", ConditionTypes.Equals, filter.elementClass);
 			}
 			
 			if (!filter.createdAtStart.isEmpty())
 			{
 				finder = finder.Where("CreatedOn", ConditionTypes.GreaterOrEqual, filter.createdAtStart.substring(0, filter.createdAtStart.length()-1).concat(":00"));
 			}
 			
 			if (!filter.createdAtFinish.isEmpty())
 			{
 				finder = finder.Where("CreatedOn", ConditionTypes.LessOrEqual, filter.createdAtFinish.substring(0, filter.createdAtFinish.length()-1).concat(":59"));
 			}
 			
 			ClickStreamEvent event = null;
 			while (true) 
 			{
 				event = (ClickStreamEvent) finder.Next();
 				if (event == null)
 					break;
 				array.add(event);
 			} 
 		
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		
 		if (array.size() == 0)
 		{
 		
 			renderJSON("[]");
 			return;
 		}
 		renderJSON(array);
 		
 	}
 	
 	public static void getAddedEvents(JsonObject body) 
 	{
 		String sid = body.getAsJsonPrimitive("Id").getAsString();  
 		long id = Long.parseLong(sid);
 		DataFinder finder = new DataFinder(ClickStreamEvent.class);
 		//System.out.println("Step1");
 		ArrayList<Long> ids = finder.getAddedIdsSinceId(id);
 		ClickStreamEvent event = new ClickStreamEvent();
 		ArrayList<ClickStreamEvent> results = new ArrayList<ClickStreamEvent>();
 		//System.out.println("Step2");
		for (int i = 0; i<ids.size(); i++)
 		{
 			try 
 			{
 				event = (ClickStreamEvent) DataWorker.Instance().LoadObjectById(ids.get(i), event);
 			} 
 			catch (IllegalAccessException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			results.add(event);
 			
 		}
 		
 		if (results.size() == 0)
 		{
 		
 			renderJSON("[]");
 			return;
 		}
 		renderJSON(results);
 	}
 	
 }
