 package com.app.example.db.lib;
 
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.StrictMode;
 import android.util.Log;
 
 import com.app.example.bookmarksWallet.models.ActionLog;
 import com.app.example.bookmarksWallet.models.Link;
 import com.app.example.common.lib.SharedData;
 import com.app.example.http.client.CustomHttpClient;
 
 public class DatabaseConnectionCommon {
 	private static final String TAG = "linksParserJSONData_TAG";
    /**ONLINE DB fx*/
 
 	/**FETCH DATA**/
 	//TODO change this to JSON fetch data 
 	public static String fetchDataFromDb(int choicedDB){
      	try {
      		//check if db is right
      		if(choicedDB!=SharedData.LINKS_DB && choicedDB!=SharedData.USERS_DB)
      			Log.e("fetchDataFromDb_TAG", "NO DB FOUND - u must define the right database name");
      		
      		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
      		postParameters.add(new BasicNameValuePair("choicedDB",""+choicedDB));
      		postParameters.add(new BasicNameValuePair("selectAllRowFromDB",""+SharedData.SELECT_ALL_ROW_FROM_DB));
      		if(choicedDB==SharedData.LINKS_DB){
      			//get my userId to fetch all liks I stored before
      			
 //     			if(userObj!=null)
 //     				userIdTMP=userObj.getUserId();
      			//TODO remove this userIdTmp
      			int userIdTMP=1;
      			postParameters.add(new BasicNameValuePair("userId",""+userIdTMP));
      		}	
      		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      	    StrictMode.setThreadPolicy(policy);
      		
      	    return CustomHttpClient.executeHttpPost(SharedData.DBUrl,postParameters).toString();
      	}catch (Exception e) {
      		Log.e("fetchDataFromDb_TAG","Error in http connection!!" + e.toString());     
      	}
      	return null;
 	}
 	/**PARSERING DATA**/
 	public static int usersParserJSONData(String usernameTypedIn,String passwordTypedIn){
 	    try{
 	    	JSONArray jArray = new JSONArray(DatabaseConnectionCommon.fetchDataFromDb(SharedData.USERS_DB));
 	    	for(int i=0;i<jArray.length();i++){
 	    		JSONObject json_data = jArray.getJSONObject(i); 
 	    		int userIdTmp = json_data.getInt("user_id");
 	    		String usernameTmp = json_data.getString("username");
 	    		String passwordTmp = json_data.getString("password");
                
 //	    		Log.i("usersParserJSONData_TAG","id: "+userIdDb+
 //                        ", usrname: "+usernameDb+
 //                        ", pswd: "+passwordDb
 //	    				);
 	       	    if(usernameTmp.compareTo(usernameTypedIn)==0 && passwordTmp.compareTo(passwordTypedIn)==0)
 	       	    	return userIdTmp;
 	    	}
 	    }catch(JSONException e){
 	    	Log.e(TAG+"- usersParserJSONData_TAG", "Error parsing data "+e.toString());
 	    }
     	return SharedData.USER_LOGIN_FAILED;
 	}
 
 	/**GET LINKS LIST**/
 	public static ArrayList<Link> getLinksListFromJSONData(){
 		//TODO set this fx visible only if user is logged in
 	   //TODO TEST values cahnge or rm
 	   boolean isDeletedLink=false;
 	   String delIconPathDb="";    	
    	
 	   try{
 		   	ArrayList<Link> linksObjList=new ArrayList<Link>();
 		   	String JSONdata = DatabaseConnectionCommon.fetchDataFromDb(SharedData.LINKS_DB);
 		   	if(JSONdata==null)
 		   		return null;
 		   	JSONArray jArray = new JSONArray(JSONdata);
 	    	for(int i=0;i<jArray.length();i++){
 	    		//get links data
 	    		JSONObject json_data = jArray.getJSONObject(i); 
 	    		Link linkObj=new Link(json_data.getInt("link_id"),
 	    				json_data.getString("iconPath"),
 	    				json_data.getString("linkName"),
 	    				json_data.getString("linkUrl"),
 	    				json_data.getInt("links_user_id"),
 	    				delIconPathDb,
 	    				isDeletedLink);
 	    		linksObjList.add(linkObj);
 	    		//get LINK icon from URL
 //	            try{
 //	            	URL linkURLObj=new URL(linkUrlDb);
 //		            infoURL=linkURLObj.getUserInfo();
 //	            }catch(Exception e){
 //	            	Log.v("URL_TAG","error - "+e);
 //				}
 
 //	    		Log.d(TAG+"- getLinksListFromJSONData","id: "+linkObj.getLinkId()+
 //	    				", iconPath: "+linkObj.linkIconPath+
 //                        ", linkUrl: "+linkObj.getLinkUrl()+
 //                        ", userId: "+linkObj.getLinkId()+
 //                        ", linkName: "+linkObj.getLinkName()
 //	    				);
 	    	}
 	    	return linksObjList;
 	   }catch(JSONException e){
 	    	Log.e(TAG+"- getLinksListFromJSONData", "Error parsing data "+e.toString());
 	   }
 	   return null;
 	}
 
 	/**INSERT URL INTO DB**/
 	//TODO change this to JSON fetch data 
 	public static boolean insertUrlEntryOnDb(int choicedDB,String urlString){
 	   if(SharedData.isUserLoggedIn()){
 		   try{
 		  		if(choicedDB!=SharedData.LINKS_DB && choicedDB!=SharedData.USERS_DB)
 		  			Log.e(TAG, "NO DB FOUND - u must define the right database name");
 	
 		  		//add choicedDB params
 		  		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 		  		postParameters.add(new BasicNameValuePair("insertUrlOnDb",""+SharedData.INSERT_URL_ON_DB));
 		  		postParameters.add(new BasicNameValuePair("choicedDB",""+choicedDB));
 		  		
 		  		if(choicedDB==SharedData.LINKS_DB){
 		  			//get my userId to fetch all liks I stored before
 	  				int userIdTMP=SharedData.getUser().getUserId();
 		  			postParameters.add(new BasicNameValuePair("userId",""+userIdTMP));
 	
 		  			//get url to be stored
 		  			if(urlString!=null)
 		  				postParameters.add(new BasicNameValuePair("linkUrl",""+urlString)); 			
 
 		  			String linkNameTMP=SharedData.getLinkNameByUrl(urlString);
 		  			if(linkNameTMP!=null)
 		  				postParameters.add(new BasicNameValuePair("linkName",""+linkNameTMP));
 		  		}	
 		  		
 		  		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 		  	    StrictMode.setThreadPolicy(policy);
 		   		String response = CustomHttpClient.executeHttpPost(SharedData.DBUrl,postParameters);
 		   		Log.d(TAG,response);
 		   		return true;
 
 		  	}catch (Exception e) {
 		  		Log.e("insertUrlEntryOnDb_TAG","Error in http connectionx!!" + e.toString());     
 	   		return false;
 		  	}
 	   }
 	   return false;
 	}
 	
 	/**DELETE ENTRY FROM DB**/
 	//TODO change this to JSON fetch data 
 	public static boolean deleteUrlEntryFromDb(int choicedDB,int linkId){
 	   if(SharedData.isUserLoggedIn()){
 		   try{
 		  		//check if db is right
 		  		if(choicedDB!=SharedData.LINKS_DB && choicedDB!=SharedData.USERS_DB)
 		  			Log.e(TAG, "NO DB FOUND - u must define the right database name");
 	
 		  		//add choicedDB params
 		  		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 		  		postParameters.add(new BasicNameValuePair("deleteUrlFromDb",""+SharedData.DELETE_URL_FROM_DB));
 		  		postParameters.add(new BasicNameValuePair("choicedDB",""+choicedDB));
 		  		
 	 			//get my userId to fetch all liks I stored before
 	 			int userIdTMP=SharedData.getUser().getUserId();
 	 			if(userIdTMP==SharedData.EMPTY_USERID)
 	 				return false;
  				postParameters.add(new BasicNameValuePair("links_user_id",""+userIdTMP));
 	 			
 	 			//check linkId!=null
 	 			if(linkId==SharedData.EMPTY_LINKID)
 	 				return false;
  				postParameters.add(new BasicNameValuePair("linkId",""+linkId));
 	 			
 		  		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 		  	    StrictMode.setThreadPolicy(policy);
 		  	    
 		   		String response = CustomHttpClient.executeHttpPost(SharedData.DBUrl,postParameters);
 		   		
 	 			Log.d(TAG,""+linkId);
 		   		Log.d(TAG,"this is the result" + response);
 			   
 		   		return true;
 		  	}catch (Exception e) {
 		  		Log.e(TAG+"- deleteUrlEntryFromDb_TAG","Error in http connection!!" + e.toString());     
 		  		return false;
 		  	}
    		}
 	   return false;
 	}
 
 	
 	/***-----------------------------------------------**/
 	
 	/***				LOCAL DB						*/
 	
 	/***-----------------------------------------------**/
     /**DB functions - used to get all db row, insert a 
      * new one or update and delete one*/
     
     /**INSERT ROW in db*/
     public static void insertLinkWrappLocalDb(DatabaseAdapter db,ActionLogDbAdapter actionLogDb,Link linkObj){
     	if(linkObj!=null){
     		
             db.open();
             actionLogDb.open();
 
             actionLogDb.insertActionLog(SharedData.ADD_LABEL,SharedData.LINK_LABEL,linkObj.getLinkId());
 //            SharedData.setLogDbStored(sharedPref, SharedData.ADD_LABEL,SharedData.LINK_LABEL,linkObj.getLinkId());
     		
             //TODO remove static init of linkOrderInList
             String linkOrderInList=Integer.toString(SharedData.EMPTY_LINKID);
             db.insertLink(linkObj.getLinkId(),linkOrderInList, linkObj.getLinkName(), linkObj.getIconPath(),
             		linkObj.getLinkUrl(),Integer.toString(linkObj.getUserId()),linkObj.getLinkDeletedStatus());
 
             actionLogDb.close();
             db.close();
     	}
     }
     /**INSERT ROW in db - overloading insert function*/
     public static boolean insertLinkWrappLocalDb(DatabaseAdapter db,ActionLogDbAdapter actionLogDb,int linkId,int linkOrderInList,String linkName,String iconPath,String linkUrl,int linksUserId,SharedPreferences sharedPref){
         db.open();
         actionLogDb.open();
 
         actionLogDb.insertActionLog(SharedData.ADD_LABEL,SharedData.LINK_LABEL,linksUserId);
 
         db.insertLink(linkId,Integer.toString(linkOrderInList), linkName, iconPath,
         		linkUrl,Integer.toString(linksUserId),false);
 
         actionLogDb.close();
         db.close();
         return true;
     }
 
     /**GET ALL ROWS from db**/
     public int getNumbOfRowsLocalDb(DatabaseAdapter db){
         db.open();
  
         Cursor c=db.getLinks();
         int count= c.getCount();
         
         db.close();
         return count;
     }
     
     /**GET ALL ROWS from db**/
     public static ArrayList<Link> getLinksWrappLocalDb(DatabaseAdapter db){
     	boolean emptyDb=true;
     	ArrayList<Link> linkList = new ArrayList<Link>();
         db.open();
         
         Cursor c=db.getLinks();
         if(c.moveToFirst()){
         	emptyDb=false;
         	do{
         		//TODO add c.getInt(1) in Link obj - linkOrderInList
         		//TODO to be fixed inconPath pos 3 in db but must be in pos 2
         		linkList.add(new Link(c.getInt(0),c.getString(3),c.getString(2),c.getString(4),c.getInt(5),null,false));
         	}while(c.moveToNext());
         }
         
         db.close();
         if(emptyDb)
         	return null;
         return linkList;
     }
     /**GET ONE ROW from db**/
     public static Link getLinkByIdWrappLocalDb(DatabaseAdapter db,int idRow){
     	Link linkObj=null;
         db.open();
 
         Cursor c=db.getLinkById(idRow);
         if(c.moveToFirst())
         	linkObj=new Link(c.getInt(0),c.getString(2),c.getString(3),c.getString(4),c.getInt(5),null,false);
         
         db.close();
         return linkObj;
     }
 
     /**GET ONE ROW from db**/
     public static boolean deleteLinksWrappLocalDb(DatabaseAdapter db){
         db.open();
     	
         
 	  	db.dropDbTable();
         db.deleteLinks();
 
     	db.close();
     	return true;
     }
     /**GET ONE ROW from db**/
     public static boolean deleteLinkByIdWrappLocalDb(DatabaseAdapter db,ActionLogDbAdapter actionLogDb,int linkId,SharedPreferences sharedPref){
         db.open();
         actionLogDb.open();
 
         //SET delete all links ID
        actionLogDb.insertActionLog(SharedData.DELETE_LABEL,SharedData.LINK_LABEL,linkId);
 
 	  	db.dropDbTable();
         db.deleteLinkById(linkId);
         
         actionLogDb.close();
     	db.close();
     	return true;
     }
     public static void updateLinkByIdWrappLocalDb(DatabaseAdapter db,ActionLogDbAdapter actionLogDb,Link linkObj,SharedPreferences sharedPref){
         db.open();
         actionLogDb.open();
         
        actionLogDb.insertActionLog(SharedData.EDIT_LABEL,SharedData.LINK_LABEL,linkObj.getLinkId());
     	
         //TODO not sure if linkId is the same as rowId
         long rowId=linkObj.getLinkId();
 		db.updateLink(rowId,Integer.toString( linkObj.getLinkOrderInList()), linkObj.getLinkName(), linkObj.getIconPath(), linkObj.getLinkUrl(), Integer.toString(linkObj.getUserId()),linkObj.getLinkDeletedStatus());
         
 		actionLogDb.close();
     	db.close();
     }
 
     /**GET ONE ROW from db**/
     public static int getMaxOnLinkIdWrappLocalDb(DatabaseAdapter db){
     	db.open();
     	int linkId;
     	boolean emptyDb = true;
     	Cursor mCursor = db.getMaxOnLinkId();
     	
 		linkId=SharedData.EMPTY_LINKID;
     	if (mCursor!=null) {
 			mCursor.moveToFirst();
 			linkId=mCursor.getInt(0);
 		}
 		db.close();
     	
     	if(emptyDb)
     		return 1;
     	return linkId;
     }
     
 	/***-----------------------------------------------
 					ActionLogDb						
 	-----------------------------------------------**/
 
 
     /**GET ALL ROWS from LOGdb**/
     public static ArrayList<ActionLog> getActionLogWrappLocalDb(ActionLogDbAdapter db){
     	boolean emptyDb=true;
     	ArrayList<ActionLog> actionLogList = new ArrayList<ActionLog>();
         db.open();
         
         Cursor c=db.getActionLogs();
         if(c.moveToFirst()){
         	emptyDb=false;
         	do{
         		//TODO add c.getInt(1) in Link obj - linkOrderInList
         		//TODO to be fixed inconPath pos 3 in db but must be in pos 2
         		actionLogList.add(new ActionLog(c.getInt(0),c.getString(1),c.getString(2),c.getInt(3)));
         	}while(c.moveToNext());
         }
         
         db.close();
         if(emptyDb)
         	return null;
         return actionLogList;
     }
 
     
     /**GET ONE ROW from db**/
     public static void deleteAllActionLogWrappLocalDb(ActionLogDbAdapter db){
         db.open();
         
         db.deleteActionLogs();
         
         db.close();
     }
     
     
     
 //    public void displayLinkLocalDb(Cursor c){
 //    	Toast.makeText(this,"id "+ c.getString(0)+" icon "+ c.getString(1)+" bool "+ 
 //    			c.getString(2)+"  bool "+ c.getString(3)+" bool "+ c.getString(4)+
 //    			"  bool "+ c.getString(5)+" bool " + c.getString(6)+" bool "+ 
 //    			c.getString(7), Toast.LENGTH_LONG).show();
 //    }
 
 //    
 //    public void ToastMessageWrapper(String message){
 //    	Toast.makeText(this,message,Toast.LENGTH_LONG).show();
 //    }
 //    
 //    public void DBTestFunctionLocalDb(DatabaseAdapter db){
 //    	
 //       	//generate new db - insert rows into it
 //       	//insertOneRowDb(db,"aaaa","title1","author1");
 //       	//insertOneRowDb(db,"zzzz","title2","author2");
 //       	//db.deleteAllRows();
 //		
 //       	//get all row from db
 //       	//getOneRowDb(db,3);
 //    	//getAllRowsDb(db);
 //    	deleteAllRowsDb(db);
 //       	//insertOneRowDb(db,"aaaa","title1","author1");
 //       	getAllRowsDb(db);
 //    }    
 }
