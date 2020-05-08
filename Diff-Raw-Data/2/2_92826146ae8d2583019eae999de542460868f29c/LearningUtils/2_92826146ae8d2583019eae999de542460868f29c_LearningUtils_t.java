 package tw.edu.chu.csie.e_learning.util;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import tw.edu.chu.csie.e_learning.config.Config;
 import tw.edu.chu.csie.e_learning.provider.ClientDBProvider;
 import tw.edu.chu.csie.e_learning.server.BaseSettings;
 import tw.edu.chu.csie.e_learning.server.ServerAPIs;
 import tw.edu.chu.csie.e_learning.server.ServerUtils;
 import tw.edu.chu.csie.e_learning.server.exception.HttpException;
 import tw.edu.chu.csie.e_learning.server.exception.ServerException;
 
 public class LearningUtils 
 {
 	private BaseSettings bs;
 	private ServerAPIs connect;
 	private JSONDecodeUtils decode;
 	private ClientDBProvider dbcon;
 	private SettingUtils settings;
 	public LearningUtils(Context context)
 	{
 		settings = new SettingUtils(context);
 		bs = new BaseSettings(settings.getRemoteURL());
 		connect = new ServerAPIs(bs);
 		decode = new JSONDecodeUtils();
 		dbcon = new ClientDBProvider(context);
 	}
 	
 	/**
 	 * 此學習點是否為推薦的學習點
 	 * @param pointNumber
 	 * @return
 	 */
 	public boolean isInRecommandPoint(String pointNumber) {
 		// 抓取資料庫中有無此學習點
 		String[] query = dbcon.search("chu_target", "TID", "TID="+pointNumber);
 		if(query.length>0) return true;
 		else return false;
 	}
 	
 	public boolean isEntityMaterial(String pointNumber) {
 		// 抓取資料庫中有無此學習點
 		String[] query = dbcon.search("chu_target", "IsEntity", "TID="+pointNumber);
 		// 如果不是實體教材
 		if(query[0].equals("0")) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	/**
 	 * 加人數
 	 * @param pointNumber
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws HttpException
 	 * @throws JSONException 
 	 * @throws ServerException 
 	 */
 	public void addPeople(String pointNumber) throws ClientProtocolException, IOException, HttpException, JSONException, ServerException
 	{
 		connect.addPeople(pointNumber);
 	}
 	
 	/**
 	 * 減人數
 	 * @param pointNumber
 	 * @return
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws HttpException
 	 * @throws JSONException
 	 * 請用Log.d();來Debug~!!
 	 * @throws ServerException 
 	 */
 	public void subPeople(String pointNumber) throws ClientProtocolException, IOException, HttpException, JSONException, ServerException
 	{
 		connect.subPeople(pointNumber);
 	}
 	
 	/**
 	 * 取得系統推薦的下個學習點
 	 * @param userID
 	 * @param pointNumber
 	 * @throws HttpException 
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 * @throws JSONException 
 	 * @throws ServerException
 	 */
 	public void getPointIdOfLearningPoint(String userID,String pointNumber) throws ServerException, JSONException, ClientProtocolException, IOException, HttpException 
 	{
 		String message = connect.getPointIdOfLearningPoint(userID, pointNumber);
 		
		if(!message.equals("null")) {
 			decode.DecodeJSONData(message,"first");
 			dbcon.target_insert(decode.getNextPoint(),decode.getTargetName() ,decode.getMapURL(), decode.getMaterialURL(), decode.getEstimatedStudyTime(),decode.getIsEntity());
 		}
 		else {
 			dbcon.target_insert(0, "", "", "", 0, 0);
 		}
 	}
 }
