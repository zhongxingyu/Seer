 package globalswrapper;
 
 import java.util.ArrayList;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.intersys.globals.NodeReference;
 
 public class ListWorker {
 
 	public Long ProjectId;
 	public String TableName;
 	
 	public ListWorker(Long projectId, String tableName)
 	{
 		ProjectId = projectId;
 		TableName = tableName;
 	}
 	
 	public ArrayList<JsonObject> GetList(FilterExpression expression, SortCondition sort, PageInfo requiredPage)
 	{
 		ArrayList<JsonObject> list = ApplyFilter(expression);
 		list = SortItems(list, sort);
 		list = PaginateItems(list, requiredPage);
 		return list;
 	}
 	
 	public ArrayList<JsonObject> ApplyFilter(FilterExpression expression)
 	{
 		ArrayList<JsonObject> list = new ArrayList<JsonObject>();
 		
 		String globalName = Utils.TableNameToGlobalsName(TableName+SchemaManager.Instance().GetProjectPrefix(ProjectId));  
 		NodeReference node = ConnectionManager.Instance().getConnection().createNodeReference(globalName);
 		
 		Long key = (long)0;
 		
 		//FilterApplicator applicator = new FilterApplicator(ProjectId, TableName, condition);
 		//FilterExpression expression = new FilterExpression(conditions, projectId)
 		while (true)
 		{
 			String strKey = node.nextSubscript(key);
 			if (strKey.equals(""))
 				break;
 			key = Long.parseLong(strKey);
 			String nodeValue = node.getObject(key, "JSON").toString();
 			JsonObject obj = new JsonParser().parse(nodeValue).getAsJsonObject();
 			
			if (expression != null)
 			{
 				if (expression.IsValid(obj))
 				{
 					list.add(obj);
 				}
 			}
			else
			{
				System.out.println("We have a problem: null expression");
			}
 		}
 		
 		return list;
 		
 	}
 	
 	public ArrayList<JsonObject> SortItems(ArrayList<JsonObject> items, SortCondition condition)
 	{
 		if (condition != null)
 			condition.sort(items);
 		return items;
 	}
 	
 	public ArrayList<JsonObject> PaginateItems(ArrayList<JsonObject> items, PageInfo requiredPage)
 	{
 		if (requiredPage == null)
 			return items;
 		
 		int hiIndex = requiredPage.GetHiIndex();
 		if (hiIndex == 0)
 			return items;
 		
 		int lowIndex = requiredPage.GetLowIndex();
 		
 		hiIndex = Math.min(hiIndex, items.size());
 		lowIndex = Math.min(lowIndex, items.size());
 		return new ArrayList<JsonObject>(items.subList(lowIndex, hiIndex));
 	
 	}
 	
 	public void WriteList(ArrayList<JsonObject> items)
 	{
 		for (int i=0; i<items.size(); i++)
 		{
 			System.out.println(items.get(i));
 		}
 		
 	}
 }
