 package controllers;
 
 import java.util.ArrayList;
 
 import globalswrapper.CRUDManager;
 import globalswrapper.FilterCondition;
 import globalswrapper.FilterConverter;
 import globalswrapper.ListWorker;
 import globalswrapper.SchemaManager;
 import globalswrapper.SortCondition;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import play.mvc.results.Error;
 
 public class CRUD extends BaseController{
 
 	// TODO: conditions
 	public static void list(Long projectId, String tableName, JsonObject jsonParam)
 	{

 		try {
			
			if (jsonParam==null)
			{
 				jsonParam = new JsonObject();
 			}
 			JsonArray jsonArray = new JsonArray(); 
 			ListWorker listWorker = new ListWorker(projectId, tableName);
 			
 			FilterConverter filterManager = FilterConverter.Instance(projectId, tableName, jsonParam); 
 		
 			ArrayList<JsonObject> result = listWorker.GetList(filterManager.Filter, filterManager.Sort, filterManager.Page);
 
 			for (int i=0; i<result.size(); i++)
 			{
 				jsonArray.add(result.get(i));
 			}
 			
 			renderJSON(jsonArray.toString());
 		}
 		catch (Exception ex){
 			ex.printStackTrace();
 			System.out.println(ex.toString());
 			internalError();
 		}
 	}
 	
 	public static void show(Long projectId, String tableName, long id)
 	{
 		try	{
 			CRUDManager manager = CRUDManager.Instance();
 			JsonObject result = manager.Read(projectId, tableName, id);
 			if (result == null){
 				notFound();
 			}
 			else{
 				renderJSON(result);
 			}
 		}catch (Exception ex){
 			internalError();
 		}
 	}
 
 	public static void create(Long projectId, String tableName, JsonObject body)
 	{
 		try{
 			CRUDManager manager = CRUDManager.Instance();
 			JsonObject result = new JsonObject();
 			result = manager.Create(projectId, tableName, body);
 			if (result != null){
 				ok();
 			}
 		}catch(Exception ex){
 			System.out.println(ex.toString());
 			internalError();
 		}
 	}
 	
 	public static void delete(Long projectId, String tableName, long id)
 	{
 		try{
 			CRUDManager manager = CRUDManager.Instance();
 			manager.Delete(projectId, tableName, id);
 			ok();
 		}catch(Exception ex){
 			internalError();
 		}
 	}
 	
 	public static void update(Long projectId, String tableName, JsonObject object)
 	{	
 		try{
 			CRUDManager manager = CRUDManager.Instance();
 			manager.Update(projectId, tableName, object);
 			ok();
 		}catch(Exception ex){
 			internalError();
 		}
 	}
 }
