 package jp.modal.soul.KeikyuTimeTable.model;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
     /** ログ用タグ */
     public final String TAG = this.getClass().getSimpleName();
 
     /** DB名 */
     private static final String DB_NAME = "kqbus";
 
     /**
      * DBのバージョン番号
      */
     public static final int DB_VERSION = 2;
 
     private static String DB_PATH = "/data/data/jp.modal.soul.KeikyuTimeTable/databases/"; 
     
     private SQLiteDatabase mDataBase;  
     
     private final Context mContext; 
     
     private static String DB_NAME_ASSET = "kqbus.db";  
     
     /**
      * コンストラクタ
      * @param context
      */
     public DatabaseHelper(Context context) {
     	super(context, DB_NAME, null, DB_VERSION);
     	this.mContext = context;
     }
 
     public void createEmptyDataBase() throws IOException{  
         boolean dbExist = checkDataBaseExists();  
   
         if(dbExist){  
             // すでにデータベースは作成されている  
         	
         }else{  
             // このメソッドを呼ぶことで、空のデータベースが  
             // アプリのデフォルトシステムパスに作られる  
             this.getReadableDatabase();  
    
             try {  
                 // asset に格納したデータベースをコピーする  
                 copyDataBaseFromAsset();   
             } catch (IOException e) {  
                 throw new Error("Error copying database");  
             }  
         }  
     }
     
     private void copyDataBaseFromAsset() throws IOException{  
     	   
         // asset 内のデータベースファイルにアクセス  
         InputStream mInput = mContext.getAssets().open(DB_NAME_ASSET);  
    
         // デフォルトのデータベースパスに作成した空のDB  
         String outFileName = DB_PATH + DB_NAME;  
    
         OutputStream mOutput = new FileOutputStream(outFileName);  
   
         // コピー  
         byte[] buffer = new byte[1024];  
         int size;  
         while ((size = mInput.read(buffer)) > 0){  
             mOutput.write(buffer, 0, size);  
         }  
    
         //Close the streams  
         mOutput.flush();  
         mOutput.close();  
         mInput.close();  
     }  
    
     public SQLiteDatabase openDataBaseReadable() throws SQLException{  
         //Open the database  
         String myPath = DB_PATH + DB_NAME;  
         mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);  
         return mDataBase;  
     }
     public SQLiteDatabase openDataBaseWritable() throws SQLException {
     	String myPath = DB_PATH + DB_NAME;  
         mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);  
         return mDataBase;
     }
       
     @Override  
     public void onCreate(SQLiteDatabase arg0) {  
     }  
   
     @Override  
     public synchronized void close() {  
         if(mDataBase != null)  
             mDataBase.close();  
       
         super.close();  
     }  
     
     private boolean checkDataBaseExists() {  
         SQLiteDatabase checkDb = null;  
    
         try{  
             String dbPath = DB_PATH + DB_NAME;  
             checkDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);  
         }catch(SQLiteException e){  
             // データベースはまだ存在していない  
         }  
    
         if(checkDb != null){  
             checkDb.close();  
         }  
         return checkDb != null ? true : false;  
     }
     
     public boolean updateDatabase() {
     	Log.e("HOGEHOGE", "DB UPDATE NOW!!!!!!!!!!!!!!!!!!!!!!!!!!");
     	SQLiteDatabase db = getWritableDatabase();
     	HistoryDao dao = new HistoryDao(mContext);
     	// 履歴情報の待避
     	ArrayList<HistoryItem> items = dao.queryLatestHistory();
    	Log.e("HOGEHOGE", items.size() + "");
     	db.close();
     	try {
     		// データベースファイルの置き換え
 			copyDataBaseFromAsset();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
     	// 更新が古い順に並べ直し
     	Collections.reverse(items);

    	Log.e("HOGEHOGE", items.size() + "reverse");
     	SQLiteDatabase newDb = getWritableDatabase();
     	// 履歴情報を戻す
     	boolean flag = true;
     	for(HistoryItem item: items) {
     		try {
 				dao.insertOrReplace(newDb, item);
 				Log.e("HOGEHOGE", "DB UPDATE DONE!!!!!!!!!!!!!!!!!!!!!!!!!!");
 			} catch (Exception e) {
 				flag = false;
 				e.printStackTrace();
			} 
     	}
    	newDb.close();
     	return flag;
     }
 //    /**
 //     * テーブル定義用メソッド
 //     */
 //    @Override
 //    public void onCreate(SQLiteDatabase db) {
 ////    	db.beginTransaction();
 ////    	try {
 ////    		db.execSQL(BusStopDao.CREATE_TABLE);
 ////    		db.execSQL(RouteDao.CREATE_TABLE);
 ////    		db.execSQL(TimeTableDao.CREATE_TABLE);
 ////    		db.execSQL(HistoryDao.CREATE_TABLE);
 ////    		db.setTransactionSuccessful();
 ////    	} finally {
 ////    		db.endTransaction();
 ////    	}
 //
 //    }
     /**
      * マイグレーション用メソッド
      */
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     }
 }
