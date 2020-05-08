 package edu.unsw.triangle.service;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.unsw.triangle.data.ConnectionManager;
 import edu.unsw.triangle.data.DataSourceException;
 import edu.unsw.triangle.data.DerbyDaoManager;
 import edu.unsw.triangle.model.Item;
import edu.unsw.triangle.model.Item.ItemStatus;
 import edu.unsw.triangle.model.Query;
 
 public class SearchService 
 {
 	public static List<Item> search(Query query) throws DataSourceException, SQLException
 	{
 		List<Item> items = new ArrayList<Item>();
		List<Item> searchResults = new ArrayList<Item>();
 		DerbyDaoManager daoManager = null;
 		try
 		{
 			daoManager = new DerbyDaoManager(ConnectionManager.getInstance());
 			items = daoManager.getItemDao().findByTitle(query.getFindByTitle());
			
			// Filter items that only active
			
			for (Item item : items)
			{
				if (item.getStatus() == ItemStatus.ACTIVE)
				{
					searchResults.add(item);
				}
			}
 		}
 		finally
 		{
 			if(daoManager != null)
 				daoManager.close();
 		}
 		
		return searchResults;
 	}
 
 }
