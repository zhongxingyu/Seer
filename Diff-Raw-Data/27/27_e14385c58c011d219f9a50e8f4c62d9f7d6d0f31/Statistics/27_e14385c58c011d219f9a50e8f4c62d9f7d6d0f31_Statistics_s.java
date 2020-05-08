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
 	class SalesRow {
 		int did;
 		String dName;
 		int quantity;
 		double salesAmount;
 		
 		public SalesRow(int did, int amount, int quantity) {
 			this.did = did;
 			salesAmount = amount;
 			this.quantity = quantity;
 		}
 	}
 	
 	private Context mContext;
 
 	private List<SalesRow> mSalesData = new ArrayList<SalesRow>(); 
 	private CnkDbHelper mCnkDbMenu;
 	private CnkDbHelper mCnkDbSales;
 	private SQLiteDatabase mDbSales;
 	private SQLiteDatabase mDbMenu;
 	private double mTotalAmount = 0;
 	
 	
 	public Statistics(Context context) {
 		mContext = context;
 		mCnkDbMenu = new CnkDbHelper(context, CnkDbHelper.DB_MENU,
 				null, 1);
 		mDbMenu = mCnkDbMenu.getReadableDatabase();
 	}
 	
 	public int downloadDB(String serverDBName) {
 		String filePath = Environment
                 .getExternalStorageDirectory().getAbsolutePath()
                 + "/cainaoke/";
 		File file = mContext.getDatabasePath(CnkDbHelper.DB_SALES);
         file.delete();
         File fileDownload = new File(filePath + Server.DB_SALES);
         fileDownload.delete();
 		DBFile mDBFile = new DBFile(mContext, CnkDbHelper.DB_SALES);;
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
         	        buffIn = new BufferedOutputStream(new FileOutputStream(filePath+Server.SERVER_DB_SALES));
         	        ftpClient.enterLocalPassiveMode();
         	        boolean ret = ftpClient.retrieveFile(serverDBName, buffIn);
         	        buffIn.close();
         	        ftpClient.logout();
         	        ftpClient.disconnect();
         	        if (!ret) {
         	        	return ErrorNum.DOWNLOAD_DB_FAILED;
         	        }
         	    } else {
         	    	Log.d("ftp reply", ftpClient.getReplyString());
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
             if (mDBFile.copyDatabase(CnkDbHelper.DB_SALES) < 0) {
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
 			Log.d("statictics timestamp", "start:" + startDT + " end:" + endDT);
 			Cursor resultSet = mDbSales.query(CnkDbHelper.SALES_DATA, new String[] {"dish_id",
 					  "sum(price*quantity)",
 					  "sum(quantity)"},
 					  "DATETIME(timestamp)>='"+startDT +"' and DATETIME(timestamp)<='" + endDT+"'",
 					  null, "dish_id", null, null, null);
 			while (resultSet.moveToNext()) {
 				SalesRow salesRow = new SalesRow(resultSet.getInt(DID),
 						resultSet.getInt(TOTAL_AMOUNT), resultSet.getInt(COUNT));
 				String dishName = getDishName(salesRow.did);
 				salesRow.dName = dishName;
 				mSalesData.add(salesRow);
 				
 				mTotalAmount += resultSet.getInt(TOTAL_AMOUNT);
 			}
 		} catch (Exception e) {
 			return -1;
 		}
 		return 0;
 	}
 	
 	public int print(Calendar start, Calendar end) {
 		JSONObject salesData = new JSONObject();
 		
 		if (mSalesData.size() <= 0) {
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
  
 		Log.d("JSON",salesData.toString());
 		String response = Http.post(Server.STATISTICS_PRINT, salesData.toString());
 		if (response == null) {
 			Log.d("Respond", "die/ok");
		} else {
 			Log.d("Respond",response);
 		}
 		return 0;
 	}
 	
 	public String getName(int index) {
 		return mSalesData.get(index).dName;
 	}
 	
 	public double getAmount(int index) {
 		return mSalesData.get(index).salesAmount;
 	}
 	
 	public int getQuantity(int index) {
 		return mSalesData.get(index).quantity;
 	}
 	
 	public double getTotalAmount() {
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
 	
 	@Override
 	protected void finalize() throws Throwable {
 		mDbMenu.close();
 		if (mCnkDbSales != null) {
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
 		Cursor cur = mDbMenu.query(CnkDbHelper.DISH_TABLE_NAME, new String[] {CnkDbHelper.DISH_NAME},
 				  	CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
 		
 		if (cur.moveToNext()) {
 			return cur.getString(0);
 		}
 		return null;
 	}
 	
 }
