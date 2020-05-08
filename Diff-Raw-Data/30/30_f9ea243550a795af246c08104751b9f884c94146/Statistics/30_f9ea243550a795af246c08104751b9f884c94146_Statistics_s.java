 package com.htb.cnk.data;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Environment;
 import android.util.Log;
 import android.util.SparseArray;
 
 import com.htb.cnk.lib.DBFile;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.ErrorNum;
 import com.htb.constant.Server;
 
 /**
  * @author josh
  *
  */
 public class Statistics {
 	final String TAG = "statistics";
 	class SalesRow {
 		int did;
 		String dName;
 		float quantity;
 		float salesAmount;
 		
 		public SalesRow(int did, float amount, float quantity) {
 			this.did = did;
 			salesAmount = amount;
 			this.quantity = quantity;
 		}
 	}
 	
 	public final static int BY_DISH = 0;
 	public final static int BY_STUFF = 1;
 	public final static int BY_CATEGORY = 2;
 	public final static int BY_PRINTER = 3;
 	public final static int CATEGORY_DETAIL = 4;
 	
 	private Context mContext;
 
 	private List<SalesRow> mSalesData = new ArrayList<SalesRow>(); 
 	private CnkDbHelper mCnkDbMenu;
 	private SQLiteDatabase mDbMenu;
 	private SparseArray<String> stuffs;
 	private float mTotalAmount = 0;
 	private int tableUsage;
 	private int servedPersons;
 	
 	
 	public Statistics(Context context) {
 		mContext = context;
 		mCnkDbMenu = new CnkDbHelper(context, CnkDbHelper.DB_MENU,
 				null, 1);
 		mDbMenu = mCnkDbMenu.getReadableDatabase();
 	}
 	
 	public int downloadDB(String serverDBName, String localDBName) {
 		String filePath = Environment
                 .getExternalStorageDirectory().getAbsolutePath()
                 + "/cainaoke/";
 		File file = mContext.getDatabasePath(localDBName);
         file.delete();
         File fileDownload = new File(filePath + localDBName);
         fileDownload.delete();
 		DBFile mDBFile = new DBFile(mContext, localDBName);;
         try {
         	FTPClient ftpClient = new FTPClient();
 
         	try {
         	    ftpClient.connect(InetAddress.getByName(Server.SERVER_IP));
         	    ftpClient.login(Server.FTP_USERNAME, Server.FTP_PWD);
         	    ftpClient.changeWorkingDirectory(Server.FTP_DB_DIR);
 
         	    if (ftpClient.getReplyString().contains("250")) {
         	        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
         	        BufferedOutputStream buffIn = null;
         	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
         	        buffIn = new BufferedOutputStream(new FileOutputStream(filePath+localDBName));
         	        ftpClient.enterLocalPassiveMode();
         	        Log.i(TAG, "downloading db, src:"+serverDBName+" dest:"+filePath+localDBName);
         	        boolean ret = ftpClient.retrieveFile(serverDBName, buffIn);
         	        buffIn.close();
         	        ftpClient.logout();
         	        ftpClient.disconnect();
         	        if (!ret) {
         	        	return ErrorNum.DOWNLOAD_DB_FAILED;
         	        }
         	    } else {
         	    	Log.e(TAG, "ftp reply" + ftpClient.getReplyString());
         	    	return ErrorNum.DOWNLOAD_DB_FAILED;
         	    }
         	} catch (SocketException e) {
         	    //Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
         		e.printStackTrace();
         		return ErrorNum.DOWNLOAD_DB_FAILED;
         	} catch (UnknownHostException e) {
         	    //Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
         		e.printStackTrace();
         		return ErrorNum.DOWNLOAD_DB_FAILED;
         	} catch (IOException e) {
         	    //Log.e(SorensonApplication.TAG, e.getStackTrace().toString());
         		e.printStackTrace();
         		return ErrorNum.DOWNLOAD_DB_FAILED;
         	}
             if (mDBFile.copyDatabase(localDBName) < 0) {
             	return ErrorNum.COPY_DB_FAILED;
             }
             return 0;
         } catch (Exception e) {
             e.printStackTrace();
             return ErrorNum.DOWNLOAD_DB_FAILED;
         }
 	}
 	
 	public int perpareResult(String json, int type) { 
 
 		if (json == null) {
 			Log.d(TAG, "json == null");
 			return -1;
 		}
 		mSalesData.clear();
 		JSONObject obj;
 		try {
 			obj = new JSONObject(json);
 		
 			JSONArray data = obj.getJSONArray("data");
 			if (type == CATEGORY_DETAIL) {
 				
 			} else {
 				mTotalAmount = obj.getInt("total");
 				servedPersons = obj.getInt("personCount");
 				tableUsage = obj.getInt("tableCount");
 			}
 			int len = data.length();
 			JSONObject item;
 			for (int i=0; i<len; i++) {
 				item = data.getJSONObject(i);
 				SalesRow salesRow = new SalesRow(item.getInt("id"),
 						item.getLong("total"), item.getLong("quantity"));
 				String dishName = getDishName(salesRow.did);
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 			}
 		} catch (JSONException e) {
 			Log.d(TAG, json);
 			e.printStackTrace();
 			return -1;
 		}
 		
 		return 0;
 	}
 	
 	public int perparePerformanceResult(String json) {
 
 		if (json == null) {
 			Log.d(TAG, "json == null");
 			return -1;
 		}
 		mSalesData.clear();
 		JSONObject obj;
 		try {
 			obj = new JSONObject(json);
 		
 			JSONArray data = obj.getJSONArray("data");
 			mTotalAmount = obj.getInt("total");
 			servedPersons = obj.getInt("personCount");
 			tableUsage = obj.getInt("tableCount");
 			int len = data.length();
 			JSONObject item;
 			for (int i=0; i<len; i++) {
 				item = data.getJSONObject(i);
 				SalesRow salesRow = new SalesRow(item.getInt("id"),
 						item.getLong("total"), item.getLong("quantity"));
 				String dishName = getWaiterName(salesRow.did);
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		
 		return 0;
 	}
 
 	public int perpareStatisticsByCategory(String json) {
 		if (json == null) {
 			Log.d(TAG, "json == null");
 			return -1;
 		}
 		mSalesData.clear();
 		JSONObject obj;
 		try {
 			obj = new JSONObject(json);
 		
 			JSONArray data = obj.getJSONArray("data");
 			mTotalAmount = obj.getInt("total");
 			servedPersons = obj.getInt("personCount");
 			tableUsage = obj.getInt("tableCount");
 			int len = data.length();
 			JSONObject item;
 			for (int i=0; i<len; i++) {
 				item = data.getJSONObject(i);
 				SalesRow salesRow = new SalesRow(item.getInt("id"),
 						item.getLong("total"), item.getLong("quantity"));
 				String dishName = item.getString("name");
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		return 0;
 	}
 	
 	public String loadStatisticsResultJson(Calendar start, Calendar end, int action) {
 		JSONObject msg = new JSONObject();
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		String startDT = date.format(start.getTime()) + " " + time.format(start.getTime());
 		Date endDate = end.getTime();
 		endDate.setMinutes(endDate.getMinutes() + 1);
 		String endDT = date.format(end.getTime()) + " " + time.format(endDate);
 		
 		try {
 			msg.put("start", startDT);
 			msg.put("end", endDT);
 			msg.put("type", action);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		String respond;
 		if (action == BY_STUFF) {
 			if (this.stuffs == null) {
 				this.stuffs = new SparseArray<String>();
 			}
 			respond = Http.get(Server.STUFF, "");
 			if (respond == null) {
 				Log.e(TAG, "get stuff failed");
 				return null;
 			}
 			try {
 				JSONArray stuffs = new JSONArray(respond);
 				JSONObject stuff;
 				int stuffNum = stuffs.length();
 				this.stuffs.clear();
 				for (int i=0; i<stuffNum; i++) {
 					stuff = stuffs.getJSONObject(i);
 					this.stuffs.put(stuff.getInt("id"), stuff.getString("name"));
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return null;
 			}
 			
 		}
 		
 		respond = Http.post(Server.STATISTIC, msg.toString());
 		if (respond == null) {
 			Log.e(TAG, "get statistics failed");
 			return null;
 		}
 		
 		
 		return respond;
 	}
 	
 	public String loadStatisticsResultJson(Calendar start, Calendar end, int action, int id) {
 		JSONObject msg = new JSONObject();
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		String startDT = date.format(start.getTime()) + " " + time.format(start.getTime());
 		Date endDate = end.getTime();
 		endDate.setMinutes(endDate.getMinutes() + 1);
 		String endDT = date.format(end.getTime()) + " " + time.format(endDate);
 		
 		try {
 			msg.put("start", startDT);
 			msg.put("end", endDT);
 			msg.put("type", action);
 			msg.put("id", id);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		String respond;
 		if (action == BY_STUFF) {
 			if (this.stuffs == null) {
 				this.stuffs = new SparseArray<String>();
 			}
 			respond = Http.get(Server.STUFF, "");
 			if (respond == null) {
 				Log.e(TAG, "get stuff failed");
 				return null;
 			}
 			try {
 				JSONArray stuffs = new JSONArray(respond);
 				JSONObject stuff;
 				int stuffNum = stuffs.length();
 				this.stuffs.clear();
 				for (int i=0; i<stuffNum; i++) {
 					stuff = stuffs.getJSONObject(i);
 					this.stuffs.put(stuff.getInt("id"), stuff.getString("name"));
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				return null;
 			}
 			
 		}
 		
 		respond = Http.post(Server.STATISTIC, msg.toString());
 		if (respond == null) {
 			Log.e(TAG, "get statistics failed");
 			return null;
 		}
 		
 		
 		return respond;
 	}
 	
 	public int perpareStatisticsByPrinter(String json) {
 		mSalesData.clear();
 		JSONObject obj;
 		try {
 			obj = new JSONObject(json);
 		
 			JSONArray data = obj.getJSONArray("data");
 			mTotalAmount = obj.getInt("total");
 			servedPersons = obj.getInt("personCount");
 			tableUsage = obj.getInt("tableCount");
 			int len = data.length();
 			JSONObject item;
 			for (int i=0; i<len; i++) {
 				item = data.getJSONObject(i);
 				SalesRow salesRow = new SalesRow(0,
 						item.getLong("total"), item.getLong("quantity"));
 				String dishName = item.getString("name");
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		return 0;
 	}
 	
 	public int print(Calendar start, Calendar end) {
 		JSONObject salesData = new JSONObject();
 		int ret;
 		
 		if (mSalesData.size() <= 0) {
 			return -1;
 		}
 		
 		ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_STATISTICS);
 		if (ret < 0) {
 			Log.e(TAG, "get PrinterStatus failed:" + ret);
 			return -1;
 		}
 		try {
 			SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			salesData.put("total", mTotalAmount);
 			salesData.put("timeStart", timestamp.format(start.getTime()));
 			salesData.put("timeEnd", timestamp.format(end.getTime()));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		JSONArray rows = new JSONArray();
 		try {
 			for (int i=0; i<mSalesData.size(); i++) {
 				JSONObject row = new JSONObject();
 				row.put("name", getName(i));
 				row.put("count", mSalesData.get(i).quantity);
 				row.put("amount", mSalesData.get(i).salesAmount);
 				row.put("percentage", mSalesData.get(i).salesAmount/mTotalAmount);
 				rows.put(row);
 			}
 			salesData.put("rows", rows);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
  
 		String response = Http.post(Server.STATISTICS_PRINT, salesData.toString());
 		if (response == null) {
 			Log.e(TAG, "Respond:die/ok");
 			return -1;
 		} else if ("".equals(response)) {
 			//Log.e("Respond",response);
 		} else { 
 			Log.e(TAG, "Respond:" + response);
 			return -1;
 		}
 		return 0;
 	}
 
 	public int getTableUsage() {
 		return tableUsage;
 	}
 	
 	public int getServedPersons() {
 			return servedPersons;
 	}
 	
 	public int getId(int index) {
 		return mSalesData.get(index).did;
 	}
 	
 	public String getName(int index) {
 		return mSalesData.get(index).dName;
 	}
 	
 	public float getAmount(int index) {
 		return mSalesData.get(index).salesAmount;
 	}
 	
 	public float getQuantity(int index) {
 		return mSalesData.get(index).quantity;
 	}
 	
 	public float getTotalAmount() {
 		return mTotalAmount;
 	}
 	
 	public int count() {
 		return mSalesData.size();
 	}
 	
 	public String getDishName(int did) {
 		String name = getDishNameFromDB(did);
 		if (name == null) {
 			return "菜名错误";
 		}
 		return name;
 	}
 	
 	public String getWaiterName(int uid) {
 		String name = stuffs.get(uid, null);
 		if (name == null) {
 			return "服务员名称错误";
 		}
 		return name;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		mDbMenu.close();
 		
 		super.finalize();
 	}
 
 	private String getDishNameFromDB(int id) {
 		Cursor cur = mDbMenu.query(CnkDbHelper.TABLE_DISH_INFO, new String[] {CnkDbHelper.DISH_NAME},
 				  	CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
 		
 		if (cur.moveToNext()) {
 			return cur.getString(0);
 		}
 		return null;
 	}
 	
 	class CategoryStatistic {
 		String name;
 		int id;
 		float count;
 		float total;
 	}
 }
