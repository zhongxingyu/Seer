 package mobile.group1.DB;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import android.util.Log;
 
 import com.mobdb.android.MobDB;
 import com.mobdb.android.GetRowData;
 import com.mobdb.android.MobDBResponseListener;
 
 public class ScavengerDB 
 {
 	private final String APP_KEY    = "MIRoAA-5T3-uym202kOKKkKuIiZxZErELos-popgfD77YeatrtTsp6WoBmmM";
 	private final String TABLE_NAME = "user";
 	
 	public void addUser(User user)
 	{
 		
 	}
 	
 	public User getUser(String username)
 	{
 		GetRowData getRowData = new GetRowData(TABLE_NAME);
 		getRowData.whereEqualsTo("test", username);
 		
 		MobDB.getInstance().execute(APP_KEY, getRowData, null, false, new MobDBResponseListener()
 		{
 			@Override public void mobDBSuccessResponse() {}
 			@Override public void mobDBResponse(String jsonObj) {}
 			@Override public void mobDBFileResponse(String fileName, byte[] fileData) {}
 
 
 			@Override
 			public void mobDBErrorResponse(Integer errValue, String errMsg) {
 				Log.i("", "error");
 			}
 
 			
 
 			@Override
 			public void mobDBResponse(Vector<HashMap<String, Object[]>> result) {
 				if(result.size() > 0)
 				{
 					HashMap<String, Object[]> item = result.get(0);
					User user = new User((String)item.get("username")[0],
 									(String)item.get("password")[0],
 									(String)item.get("game")[0],
 									(String)item.get("score")[0],
 									(String)item.get("found")[0],
 									(String)item.get("p1")[0],
 									(String)item.get("p2")[0]);
 				}
 			}
 			
 		});
 		
 		
 		return null;
 	}
 	
 	public void addGame(Game game)
 	{
 		
 	}
 	
 	public Game getGame(String game)
 	{
 		return null;
 	}
 	
 	public Item getItem(int id)
 	{
 		return null;
 	}
 	
 	public void addItem(Item item)
 	{
 		
 	}
 	
 	public ScavengerDB()
 	{
 	}
 }
