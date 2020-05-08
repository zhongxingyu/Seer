 package com.htb.cnk.lib;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Environment;
 import android.util.Log;
 
 import com.htb.cnk.data.CnkDbHelper;
 
 /**
  * @author josh
  *
  */
 public class DBFile {
 	private final String TAG = "DBFile";
 	private Context mContext;
 	private CnkDbHelper mDbHelper;
 	private String mDbName;
 	
 	public DBFile(Context context, String name) {
 	    this.mContext = context;
 	    mDbName = name;
 	}
 	
 	public void creatDBifNotExist() {
 		mDbHelper = new CnkDbHelper(mContext, mDbName, null, 1);
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		db.close();
 	}
 	
 	public int copyDatabase(String name) {
         // 获得正在使用的数据库路径，我的是 sdcard 目录下的 /dlion/db_dlion.db
 		// 默认路径是 /data/data/(包名)/databases/*.db
         File dbFile = mContext.getDatabasePath(name);
         File exportDir = new File(Environment
                 .getExternalStorageDirectory().getAbsolutePath()
                 + "/cainaoke/");
         
         creatDBifNotExist();
         
         if (!exportDir.exists()) {
             exportDir.mkdirs();
         }
         
         File backup = new File(exportDir, dbFile.getName());
         try {
             fileCopy(backup, dbFile);
            Log.i(TAG, "copy db to db dir:success,src:"+exportDir+dbFile.getName()+" dest"+dbFile.getPath());
             return 0;
         } catch (Exception e) {
             e.printStackTrace();
            Log.e(TAG, "copy db to db dir:fail,src:"+exportDir+dbFile.getName()+" dest"+dbFile.getPath());
             return -1;
         }
         
     }
 
 	public int backup(String name) {
 		File dbFile = mContext.getDatabasePath("cnk.db");
         File exportDir = new File(Environment
                 .getExternalStorageDirectory().getAbsolutePath()
                 + "/cainaoke/backup");
         
         if (!exportDir.exists()) {
             exportDir.mkdirs();
         }
         
         File backup = new File(exportDir, name);
         try {
             fileCopy(dbFile, backup);
             Log.i(TAG, "backup:success");
             return 0;
         } catch (Exception e) {
             e.printStackTrace();
             Log.e(TAG, "backup:fail");
             return -1;
         }
 	}
 	
     private void fileCopy(File dbFile, File backup) throws IOException {
         FileChannel inChannel = new FileInputStream(dbFile).getChannel();
         FileChannel outChannel = new FileOutputStream(backup).getChannel();
         try {
             inChannel.transferTo(0, inChannel.size(), outChannel);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (inChannel != null) {
                 inChannel.close();
             }
             if (outChannel != null) {
                 outChannel.close();
             }
         }
     }
 }
