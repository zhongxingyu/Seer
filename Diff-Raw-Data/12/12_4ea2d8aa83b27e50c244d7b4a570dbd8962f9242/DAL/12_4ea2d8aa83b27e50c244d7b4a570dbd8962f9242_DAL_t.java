 package coupling.app.data;
 
 import android.database.Cursor;
 import coupling.app.App;
import coupling.app.Utils;
 import coupling.app.data.Enums.CategoryType;
 
 public class DAL {
 
 	private static DAL dal;
 	private DBHandler dbHandler;
 
 	private DAL(){
 		dbHandler = new DBHandler(App.getAppCtx());
 	}
 	
 	public static DAL getInstance(){
 		if(dal == null)
 			dal = new DAL();
 		return dal;
 	}
 	
 	public Long getLocalId(CategoryType type , Long GlobalId){
 		Cursor c = dbHandler.getWritableDatabase().rawQuery("SELECT * FROM " + getTableName(type) + " WHERE UId = " + GlobalId, null);
		Utils.Log("GetLocalId", "SELECT * FROM " + getTableName(type) + " WHERE UId = " + GlobalId + " Cursor: " + c.getCount());
 		if(c.getCount() > 0){
 			c.moveToFirst();
 			return c.getLong(c.getColumnIndex("_id"));
 		}
 		return null;
 	}
 	
 	public String getTableName(CategoryType type){
 		if(type == CategoryType.SHOPLIST)
 			return "ShopList";
 		if(type == CategoryType.SHOPLIST_OVERVIEW)
 			return "ShopListOverview";
 		return null;
 	}
 }
