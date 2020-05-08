 package globalswrapper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
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
 		ArrayList<JsonObject> list = ApplyFilter2(expression);
 		list = SortItems(list, sort);
 		list = PaginateItems(list, requiredPage);
 		return list;
 	}
 	
 	/*
 	public ArrayList<JsonObject> ApplyFilter(FilterExpression expression)
 	{
 		ArrayList<JsonObject> list = new ArrayList<JsonObject>();
 		
 		String globalName = Utils.TableNameToGlobalsName(TableName+SchemaManager.Instance().GetProjectPrefix(ProjectId));  
 		NodeReference node = ConnectionManager.Instance().getConnection().createNodeReference(globalName);
 		
 		Long key = (long)0;
 		
 		//FilterApplicator applicator = new FilterApplicator(ProjectId, TableName, condition);
 		//FilterExpression expression = new FilterExpression(conditions, projectId)
 		
 		// фильтрация через индексы
 		// считаем что фильтр у нас только И, или сложнее. 
 		// 1. определяем порядок выполнения - extent\selectivity
 		// 2. функция выборки нужных значений из индекса
 		// 3. кладем в хэш выборки
 		// 4. итерационно пробегаемся по остальным индексам - и создаем новый хэш, добавляем туда значения присутствующие в индексе и в хэше 1
 		// 5. далее хэш 2 делаем основным хэшем - по сути интерсектим результат.
 		// 6. так для всех индексированных полей??
 		// 7. применяем фильтрацию на записи
 		
 		while (true)
 		{
 			String strKey = node.nextSubscript(key);
 			if (strKey.equals(""))
 				break;
 			key = Long.parseLong(strKey);
 			String nodeValue = node.getObject(key, "JSON").toString();
 			JsonObject obj = new JsonParser().parse(nodeValue).getAsJsonObject();
 
 			
 			if (expression == null)
 			{
 				list.add(obj);
 			}
 			else
 			{
 				if (expression.IsValid(obj))
 				{
 					list.add(obj);
 				}
 			}
 						
 		}
 		
 		return list;
 		
 	}*/
 	
 
 	public ArrayList<JsonObject> ApplyFilter2(FilterExpression expression)
 	{
 		if (expression == null)
 		{
 			return FullScan(expression);
 		}
 		else
 		{
 			if (expression.IndexedCondtionsByFieldName.isEmpty())
 			{
 				return FullScan(expression);
 			}
 			
 			
 			ArrayList<Long> setToScan = ExtractSubSetByIndexedCondtions(expression);
 			return ScanSet(expression, setToScan);
 		}
 	}
 	
 	// Apply on indexed fields conditions
 	private ArrayList<Long> ExtractSubSetByIndexedCondtions(FilterExpression expression)
 	{
 		ArrayList<Long> setToScan = null;
 		
 		for (Map.Entry<String, ArrayList<FilterCondition>> entry : expression.IndexedCondtionsByFieldName.entrySet()) 
 		{ 
 			setToScan = ExtractFromIndex(setToScan, expression, entry.getValue());
 			// we supply a only AND filter at the moment, that's why if searched 0 - we can stop.
 		    if (setToScan.size() == 0)
 		    	return setToScan;
 		} 
 		return setToScan;
 	}
 	
 	/// Возвращаем набор записей
 	private ArrayList<Long> ExtractFromIndex(ArrayList<Long> previousSetToScan, FilterExpression expression, ArrayList<FilterCondition> conditions)
 	{
		ArrayList<Long> setToScan = new ArrayList<>();
 		// if IsFirstRun
 		if (previousSetToScan == null)
 		{
 			// do not intersect
 		}
 		
 		return setToScan;
 	}
 	
 	
 	/// filter sub set
 	private ArrayList<JsonObject> ScanSet(FilterExpression expression, ArrayList<Long> setToScan)
 	{
 		ArrayList<JsonObject> list = new ArrayList<JsonObject>();
 		String globalName = Utils.TableNameToGlobalsName(TableName+SchemaManager.Instance().GetProjectPrefix(ProjectId));  
 		NodeReference node = ConnectionManager.Instance().getConnection().createNodeReference(globalName);
 		
 		for (int i=0;i<setToScan.size(); i++)
 		{
 			String nodeValue = node.getObject(setToScan.get(i), "JSON").toString();
 			JsonObject obj = new JsonParser().parse(nodeValue).getAsJsonObject();
 			if (expression == null)
 			{
 				list.add(obj);
 			}
 			else
 			{
 				if (expression.IsValid(obj))
 				{
 					list.add(obj);
 				}
 			}
 		}
 		return list;
 		
 	}
 	
 	/// full scan
 	private ArrayList<JsonObject> FullScan(FilterExpression expression)
 	{
 		ArrayList<JsonObject> list = new ArrayList<JsonObject>();
 		
 		String globalName = Utils.TableNameToGlobalsName(TableName+SchemaManager.Instance().GetProjectPrefix(ProjectId));  
 		NodeReference node = ConnectionManager.Instance().getConnection().createNodeReference(globalName);
 		
 		Long key = (long)0;
 		while (true)
 		{
 			String strKey = node.nextSubscript(key);
 			if (strKey.equals(""))
 				break;
 			key = Long.parseLong(strKey);
 			String nodeValue = node.getObject(key, "JSON").toString();
 			JsonObject obj = new JsonParser().parse(nodeValue).getAsJsonObject();
 
 			
 			if (expression == null)
 			{
 				list.add(obj);
 			}
 			else
 			{
 				if (expression.IsValid(obj))
 				{
 					list.add(obj);
 				}
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
