 package coupling.app.BL;
 
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.database.Cursor;
 import android.util.Log;
 import coupling.app.Ids;
 import coupling.app.Utils;
 import coupling.app.com.API;
 import coupling.app.com.AppFeature;
 import coupling.app.com.IBLConnector;
 import coupling.app.com.ITask;
 import coupling.app.com.Message;
 import coupling.app.data.DALShopList;
 import coupling.app.data.Enums.ActionType;
 import coupling.app.data.Enums.CategoryType;
 import static coupling.app.com.Constants.*;
 
 public class BLShopList extends AppFeature{
 
 	private static final String GLOBAL_LIST_ID = "GlobalListId";
 	private static final String ITEM_NAME = "ItemName";
 	private static final String QUANTITY = "Quantity";
 	private static final String IS_DONE = "IsDone";
 
 	private DALShopList dataSource;
 	private API api;
 	private ITask tasker;
 	private Long GlobalListId;
 
 	IBLConnector connector;
 
 	public BLShopList(){
 	}
 	
 	public BLShopList(long listId){
 		dataSource = new DALShopList(listId);
 		api = API.getInstance();
 		categoryType = CategoryType.SHOPLIST;
 		GlobalListId = dataSource.getGlobalListId();
 	}
 	
 	public void setBLConnector(IBLConnector connector){
 		this.connector = connector;
 	}
 	public void unsetBLConnector(){
 		this.connector = null;
 	}
 	
 
 	public boolean createItem(String name, int quantity){
 		return createItem(null,name, quantity, true);
 	}
 
 	public boolean createItem(Long UId, String name, int quantity, boolean remote){
 		long localId = dataSource.createItem(UId, name, quantity);
 		boolean createdSuccessfuly = (localId != -1);
 		
 		if(remote && createdSuccessfuly) {
 			Message message = new Message();
 			
 			GlobalListId = (GlobalListId == null) ? dataSource.getGlobalListId() : GlobalListId;
 			
 			message.getData().put(GLOBAL_LIST_ID, GlobalListId);
 			message.getData().put(LOCALID, localId);
 			message.getData().put(UID, UId);
 			message.getData().put(ITEM_NAME, name);
 			message.getData().put(QUANTITY, quantity);
 			
 			message.setCategoryType(categoryType);
 			message.setActionType(ActionType.CREATE);
 
 			api.sync(message);
 		}
 		return createdSuccessfuly;
 	}
 	public boolean updateItem(Ids ids, String name, Integer quantity, Boolean isDone){
 		return updateItem(ids, name, quantity, isDone, true);
 	}
 	public boolean updateItem(Ids ids, String name, Integer quantity, Boolean isDone, boolean remote){
 		boolean res = dataSource.updateItem(ids, name, quantity, isDone);
 		if(remote && res){
 			Message message = new Message();
 			
 			GlobalListId = (GlobalListId == null) ? dataSource.getGlobalListId() : GlobalListId;
 			
 			message.getData().put(GLOBAL_LIST_ID, GlobalListId);
 			message.getData().put(UID, ids.getGlobalId());
 			if(name != null)
 				message.getData().put(ITEM_NAME, name);
 			if(quantity != null)
 				message.getData().put(QUANTITY, quantity);
 			if(isDone != null)
 				message.getData().put(IS_DONE, isDone);
 			
 			message.setCategoryType(categoryType);
 			message.setActionType(ActionType.UPDATE);
 			api.sync(message);
 		}
 		return res;
 	}
 
 	public boolean deleteItem(Ids ids){
 		return deleteItem(ids, true);
 	}
 
 	public boolean deleteItem(Ids ids, boolean remote){
 		boolean res = dataSource.deleteItem(ids);
 		if(remote && res){
 			Message message = new Message();
 			
 			GlobalListId = (GlobalListId == null) ? dataSource.getGlobalListId() : GlobalListId;
 			
 			message.getData().put(GLOBAL_LIST_ID, GlobalListId);
 			message.getData().put(UID, ids.getGlobalId());
 			
 			message.setCategoryType(categoryType);
 			message.setActionType(ActionType.DELETE);
 
 			api.sync(message);
 		}
 		return res;
 	}
 
 	public boolean updateId(Ids ids){
 		return dataSource.updateId(ids);
 	}
 	
 	public Cursor getSource(){
 		return dataSource.getSource();
 	}
 
 	
 	
 	@Override
 	public void recieveData(JSONObject data, ActionType actionType) {
 		try{
 			
 			Ids ids = new Ids();
			if(data.has(UID) && !data.get(UID).equals("null"))
 					ids.setGlobalId(data.getLong(UID));
 			
 			String itemName = null;
 			Integer quantity = null;
 			Boolean isDone = null;
 			
 			if(data.has(ITEM_NAME)) itemName = data.getString(ITEM_NAME);
 			if(data.has(QUANTITY)) quantity = data.getInt(QUANTITY);
 			if(data.has(IS_DONE)) isDone = data.getBoolean(IS_DONE);
 				
 			
 			switch (actionType) {
 			case CREATE:
 				createItem(ids.getGlobalId(), itemName, quantity, false);
 				break;
 
 			case UPDATE:	
 				updateItem(ids, itemName, quantity , isDone, false);
 				break;
 			case DELETE:
 				deleteItem(ids, false);
 				break;
 			}
 
 			if(connector != null)
 				connector.Refresh();
 		}catch(JSONException e){
 			e.printStackTrace();
 		}
 	}
 }
