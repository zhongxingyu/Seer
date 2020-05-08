 package core.interfaces;
 
 import java.util.List;
 import java.util.LinkedList;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import core.Core;
 import core.models.Item;
 import core.network.ServerConnector;
 
 public abstract class AbstractManager<T extends Item> 
 {
 	public AbstractManager(Core core, String request)
 	{
 		m_core = core;
 		m_serverConnector = core.serverConnector();
 		m_items = new LinkedList<T>();
 		m_request = request;
 	}
 	
 	//Returns the item count.
 	public int count()
 	{
 		return m_items.size();
 	}
 	
 	//Returns an item at a specified index, if item doesn't exist null is returned.
 	public T item(int index)
 	{
 		T item = null;
 		
 		if (index >= 0 && index < m_items.size())
 		{
 			item = m_items.get(index);
 		}
 		
 		return item;
 	}
 	
 	//Clears current item list and loads new items from the server.
 	public boolean load()
 	{
 		boolean success = false;
 		
 		try
 		{
 			m_items.removeAll(m_items);
 			String response = m_serverConnector.httpGET(m_request + "/");
 			JSONArray jsonArray = new JSONArray(response);
 			int length = jsonArray.length();
 			
 			for (int i = 0; i < length; i++)
 			{
 				JSONObject jsonObject = jsonArray.getJSONObject(i);
 				T item = itemInstance();
 				
 				if (item.assign(jsonObject, true))
 				{
 					m_items.add(item);
 				}
 			}
 			
 			success = true;
 		}
 		catch (Exception exception)
 		{
 			
 		}
 		
 		return success;
 	}
 	
 	//Gets item details from the server.
 	public boolean details(T item)
 	{
 		boolean success = false;
 		
 		try
 		{
 			if (m_items.contains(item) && item.valid())
 			{
 				String response = m_serverConnector.httpGET(m_request + "/" + item.id() + "/");
 				JSONObject jsonObject = new JSONObject(response);
 				success = item.assign(jsonObject, true);
 			}
 		}
 		catch (Exception exception)
 		{
 			
 		}
 		
 		return success;
 	}
 	
 	//Creates a new item, automatically adds it to the item list.
 	public boolean create(T item)
 	{
 		boolean success = false;
 		
 		try
 		{
 			if (item.valid())
 			{
 				String response = m_serverConnector.httpPOST(m_request + "/", item.toJSON());
 				JSONObject jsonObject = new JSONObject(response);
 				
 				if (item.assign(jsonObject, true))
 				{
 					m_items.add(item);
 					success = true;
 				}
 			}
 		}
 		catch (Exception exception)
 		{
 			
 		}
 		
 		return success;
 	}
 	
 	//Updates an item.
 	public boolean update(T item, T newItem)
 	{
 		boolean success = false;
 		
 		try
 		{
 			if (m_items.contains(item) && item.valid())
 			{
 				String response = m_serverConnector.httpPUT(m_request + "/" + item.id() + "/", newItem.toJSON());
 				JSONObject jsonObject = new JSONObject(response);
 				success = item.assign(jsonObject, true);
 			}
 		}
 		catch (Exception exception)
 		{
 			
 		}
 		
 		return success;
 	}
 	
 	//Deletes an item, item is automatically remove from the current item list.
 	public boolean delete(T item)
 	{
 		boolean success = false;
 		
 		try
 		{
 			if (m_items.contains(item) && item.valid())
 			{
 				m_items.remove(item);
 				String response = m_serverConnector.httpDELETE(m_request + "/" + item.id() + "/");
 				success = response != null;
 			}
 		}
 		catch (Exception exception)
 		{
 			
 		}
 		
 		return success;
 	}
 	
 	protected abstract T itemInstance();
 	
 	protected Core m_core;
 	protected ServerConnector m_serverConnector;
 	protected List<T> m_items;
 	protected String m_request;
 }
