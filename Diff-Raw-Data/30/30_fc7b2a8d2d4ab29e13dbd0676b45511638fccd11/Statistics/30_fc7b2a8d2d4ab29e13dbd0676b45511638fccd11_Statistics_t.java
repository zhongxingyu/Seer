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
 	
 	private Context mContext;
 
 	private List<SalesRow> mSalesData = new ArrayList<SalesRow>(); 
 	private CnkDbHelper mCnkDbMenu;
 	private CnkDbHelper mCnkDbSales;
 	private CnkDbHelper mCnkDbUser;
 	private SQLiteDatabase mDbSales;
 	private SQLiteDatabase mDbMenu;
 	private SQLiteDatabase mDbUser;
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
 	
 	public int perpareResult(Calendar start, Calendar end) {
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		String startDT = date.format(start.getTime()) + " " + time.format(start.getTime());
 		Date endDate = end.getTime();
 		endDate.setMinutes(endDate.getMinutes() + 1);
 		String endDT = date.format(end.getTime()) + " " + time.format(endDate);
 		final int DID = 0;
 		final int TOTAL_AMOUNT = 1;
 		final int COUNT = 2;
 
 		conectDB();
 		mSalesData.clear();
 		mTotalAmount = 0;
 		try {
 			//Log.d("statictics timestamp", "start:" + startDT + " end:" + endDT);
 			Cursor resultSet = mDbSales.query(CnkDbHelper.SALES_DATA, new String[] {"dish_id",
 					  "sum(price*quantity)",
 					  "sum(quantity)"},
 					  "DATETIME(timestamp)>='"+startDT +"' and DATETIME(timestamp)<='" + endDT+"'",
 					  null, "dish_id", null, null, null);
 			while (resultSet.moveToNext()) {
 				SalesRow salesRow = new SalesRow(resultSet.getInt(DID),
 						resultSet.getFloat(TOTAL_AMOUNT), resultSet.getFloat(COUNT));
 				String dishName = getDishName(salesRow.did);
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 				
 				mTotalAmount += resultSet.getFloat(TOTAL_AMOUNT);
 			}
 		} catch (Exception e) {
			e.printStackTrace();
 			return -1;
 		}
 		
 		return perpareSummary(startDT, endDT);
 	}
 	
 	public int perparePerformanceResult(Calendar start, Calendar end) {
 		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		String startDT = date.format(start.getTime()) + " " + time.format(start.getTime());
 		Date endDate = end.getTime();
 		endDate.setMinutes(endDate.getMinutes() + 1);
 		String endDT = date.format(end.getTime()) + " " + time.format(endDate);
 		final int DID = 0;
 		final int TOTAL_AMOUNT = 1;
 		final int COUNT = 2;
 
 		conectDB();
 		mSalesData.clear();
 		mTotalAmount = 0;
 		try {
 			//Log.d("statictics timestamp", "start:" + startDT + " end:" + endDT);
 			Cursor resultSet = mDbSales.query(CnkDbHelper.SALES_DATA, new String[] {"waiter_id",
 					  "sum(price*quantity)",
 					  "sum(quantity)"},
 					  "DATETIME(timestamp)>='"+startDT +"' and DATETIME(timestamp)<='" + endDT+"'",
 					  null, "waiter_id", null, null, null);
 			while (resultSet.moveToNext()) {
 				SalesRow salesRow = new SalesRow(resultSet.getInt(DID),
 						resultSet.getFloat(TOTAL_AMOUNT), resultSet.getFloat(COUNT));
 				String waiterName = getWaiterName(salesRow.did);
 				salesRow.dName = waiterName;
 				mSalesData.add(salesRow);
 				
 				mTotalAmount += resultSet.getFloat(TOTAL_AMOUNT);
 			}
 		} catch (Exception e) {
			e.printStackTrace();
 			return -1;
 		}
 		
 		return perpareSummary(startDT, endDT);
 	}
 
 	public int perpareSummary(String startDT, String endDT) {
 		int ret = getTableUsage(startDT, endDT);
 		if (ret < 0) {
 			return -1;
 		} else {
 			tableUsage = ret;
 		}
 		
 		ret = getPersons(startDT, endDT);
 		if (ret < 0) {
 			return -1;
 		} else {
 			servedPersons = ret;
 		}
 		return 0;
 	}
 	public int getTableUsage() {
 		return tableUsage;
 	}
 	
 	public int getServedPersons() {
 		return servedPersons;
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
 		String name = getWaiterNameFromDB(uid);
 		if (name == null) {
			return "服务员名称错误";
 		}
 		return name;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		mDbMenu.close();
 		if (mCnkDbSales != null) {
 			mDbSales.close();
 		}
		
		if (mCnkDbUser != null) {
			mDbSales.close();
		}
 		super.finalize();
 	}
 
 	private void conectDB() {
 		if (mCnkDbSales == null) {
 			mCnkDbSales = new CnkDbHelper(mContext, CnkDbHelper.DB_SALES,
 					null, 1);
 			mDbSales = mCnkDbSales.getReadableDatabase();
 		}
 	}
 
 	private String getDishNameFromDB(int id) {
 		Cursor cur = mDbMenu.query(CnkDbHelper.TABLE_DISH_INFO, new String[] {CnkDbHelper.DISH_NAME},
 				  	CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
 		
 		if (cur.moveToNext()) {
 			return cur.getString(0);
 		}
 		return null;
 	}
 	
 	private String getWaiterNameFromDB(int id) {
 		if (mCnkDbUser == null) {
 			mCnkDbUser = new CnkDbHelper(mContext, CnkDbHelper.DB_USER,
 					null, 1);
 			mDbUser = mCnkDbUser.getReadableDatabase();
 		}
 		
 		Cursor cur = mDbUser.query(CnkDbHelper.USER_TABLE, new String[] {CnkDbHelper.USER_NAME},
 			  	CnkDbHelper.USER_ID + "=" + id, null, null, null, null);
 	
 		if (cur.moveToNext()) {
 			return cur.getString(0);
 		}
 		return null;
 	}
 	
 	private int getTableUsage(String startDT, String endDT) {
 		int ret = -1;
 		try {
 			Cursor resultSet = mDbSales.query(CnkDbHelper.TABLE_INFO, new String[] {"count()"},
 					  "DATETIME(timestamp)>='"+startDT +"' and DATETIME(timestamp)<='" + endDT+"'",
 					  null, null, null, null, null);
 			if (resultSet.moveToNext()) {
 				return resultSet.getInt(0);
 			}
 		} catch (Exception e) {
 			return -1;
 		}
 		
 		return ret;
 	}
 	
 	private int getPersons(String startDT, String endDT) {
 		int ret = -1;
 		try {
 			Cursor resultSet = mDbSales.query(CnkDbHelper.TABLE_INFO, new String[] {"sum(persons)"},
 					  "DATETIME(timestamp)>='"+startDT +"' and DATETIME(timestamp)<='" + endDT+"'",
 					  null, null, null, null, null);
 			if (resultSet.moveToNext()) {
 				return resultSet.getInt(0);
 			}
 		} catch (Exception e) {
 			return -1;
 		}
 		
 		return ret;
 	}
 }
