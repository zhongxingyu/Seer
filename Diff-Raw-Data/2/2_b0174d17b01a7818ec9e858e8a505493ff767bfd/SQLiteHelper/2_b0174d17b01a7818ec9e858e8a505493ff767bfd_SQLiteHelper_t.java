 package model;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.SQLDataException;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class SQLiteHelper extends SQLiteOpenHelper{
 	
	private static final String DATABASE_PATH = "/data/data/cyber.app.chamngon/databases/";
 	private static final String DATABASE_NAME = "chamngon.sqlite";
 	private static final int DATABASE_VERSION = 1;
 	
 	private SQLiteDatabase myDataBase;
 	private final Context myContext;
 	
 	//bang cham ngon
 	private static final String CH_TABLE_NAME = "ChamNgon";
 	private static final String CHID 		  = "_chid";
 	private static final String N_DUNG_VIET	  = "ndungviet";
 	private static final String N_DUNG_ANH	  = "ndunganh";
 	private static final String TACGIA		  = "tacgia";
 	private static final String CH_CID	      = "cid";
 	private static final String YEU_THICH	  = "yeuthich";
 	
 	//bang content
 	private static final String C_TABLE_NAME  = "Content";
 	private static final String CID			  = "_cid";
 	private static final String TEN			  = "ten";
 	
 	
 	public SQLiteHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		myContext= context;
 	}
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		/*//tao bang ChamNgon
 		db.execSQL("CREATE TABLE "+ CH_TABLE_NAME +" ("+ CHID 
 				+" INTEGER PRIMARY KEY AUTOINCREMENT," 
 				+CH_CID+" INTEGER," + N_DUNG_VIET +" TEXT,"
 				+N_DUNG_ANH+" TEXT,"+TACGIA+" TEXT);");
 		
 		//tao bang Content
 		db.execSQL("CREATE TABLE "+ C_TABLE_NAME +" ("+CID
 				+" INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ TEN +"TEXT);");*/
 		
 	}
 	
 	public void openDatabase() throws SQLDataException{
 		//open database
 		String myPath = DATABASE_PATH + DATABASE_NAME;
 		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
 		System.out.println("mo duoc database");
 	}
 	
 	@Override
 	public synchronized void close() {
 		if(myDataBase != null)
 		myDataBase.close();
  
 		super.close();
 		
 	}
 	
 	//kiem tra xem da co database chua
 	 private boolean checkDataBase(){ 
 		 SQLiteDatabase checkDB = null;
 		  
 		 try{
 			 String myPath = DATABASE_PATH + DATABASE_NAME;
 			 checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 		 }catch(SQLiteException e){
 			 //database chua ton tai
 		 }
 		 
 		 if(checkDB != null)
 			 checkDB.close();
 		  System.out.println("check database thanh cong");
 		 return checkDB != null ? true : false;
 	
 	 }
 	 // coppy data tu assets sang data
 	 private void copyDataBase() throws IOException{
 		 
 		 	//mo db trong thu muc assets nhu mot input stream
 			InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
 			 
 			//duong dan den db se tao
 			String outFileName = DATABASE_PATH + DATABASE_NAME;
 			 
 			//mo db giong nhu mot output stream
 			OutputStream myOutput = new FileOutputStream(outFileName);
 			 
 			//truyen du lieu tu inputfile sang outputfile
 			byte[] buffer = new byte[1024];
 			int length;
 			while ((length = myInput.read(buffer))>0)
 			{
 				myOutput.write(buffer, 0, length);
 			}
 			 
 			//Dong luon
 			myOutput.flush();
 			myOutput.close();
 			myInput.close();
 			System.out.println("copy databse thanh cong");
 			 
 	}
 	public void createDataBase() throws IOException{
 		boolean dbExist = checkDataBase(); //kiem tra db
 			  
 		if(dbExist){
 			 	//khong lam gi ca, database da co roi
 		}
 		else{
 			this.getReadableDatabase();
 			try {
 				copyDataBase(); //chep du lieu
 			} 
 			catch (IOException e) {
 			 		throw new Error("Error copying database");
 			 }
 			}
 		 }  
 
 	
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		
 	}
 	
 	public ChamNgon getChamNgon(int chid){
 		SQLiteDatabase sqliteDatabase = getWritableDatabase();
 		Cursor cursor = sqliteDatabase.rawQuery("select * from " + CH_TABLE_NAME + " where " + CHID + " = " + chid, null);
 		cursor.moveToNext();
 		
 		ChamNgon result = new ChamNgon();
 		result.setChid(cursor.getInt(cursor.getColumnIndex(CH_CID)));
 		result.setCid(cursor.getInt(cursor.getColumnIndex(CH_CID)));
 		result.setnDungAnh(cursor.getString(cursor.getColumnIndex(N_DUNG_ANH)));
 		result.setnDungViet(cursor.getString(cursor.getColumnIndex(N_DUNG_VIET)));
 		result.setTacGia(cursor.getString(cursor.getColumnIndex(TACGIA)));
 		result.setYeuthich(cursor.getInt(cursor.getColumnIndex(YEU_THICH)));
 		return result;
 	}
 
 	public ArrayList<ChamNgon> getChamNgonByContent(int cid){
 		SQLiteDatabase sd= getWritableDatabase();
 		ArrayList<ChamNgon> list= new ArrayList<ChamNgon>();
 		Cursor c= sd.rawQuery("select * from "+CH_TABLE_NAME+" where "+ CH_CID+" = " +cid ,null);
 		
 		c.moveToLast();
 		c.moveToNext();
 		
 		while(c.moveToPrevious()){
 			ChamNgon result= new ChamNgon();
 			result.setChid(c.getInt(c.getColumnIndex(CHID)));
 			result.setCid(c.getInt(c.getColumnIndex(CH_CID)));
 			result.setnDungAnh(c.getString(c.getColumnIndex(N_DUNG_ANH)));
 			result.setnDungViet(c.getString(c.getColumnIndex(N_DUNG_VIET)));
 			result.setTacGia(c.getString(c.getColumnIndex(TACGIA)));
 			result.setYeuthich(c.getInt(c.getColumnIndex(YEU_THICH)));
 			list.add(result);
 		}
 		System.out.println("danh dach cham ngon cua cid= "+ cid +" la "+ list.toString());
 		return list;
 	}
 	
 	public int getNumberChamNgonByContent(int cid){
 		SQLiteDatabase sd= getWritableDatabase();
 		ArrayList<ChamNgon> list= new ArrayList<ChamNgon>();
 		Cursor c= sd.rawQuery("select * from "+CH_TABLE_NAME+" where "+ CH_CID+" = " +cid ,null);
 		
 		c.moveToLast();
 		c.moveToNext();
 		
 		while(c.moveToPrevious()){
 			ChamNgon result= new ChamNgon();
 			result.setChid(c.getInt(c.getColumnIndex(CHID)));
 			list.add(result);
 		}
 		return list.size();
 	}
 	public ArrayList<ChamNgon> getChamNgonYeuThich(){
 		SQLiteDatabase sd= getWritableDatabase();
 		ArrayList<ChamNgon> list= new ArrayList<ChamNgon>();
 		Cursor c= sd.rawQuery("select * from "+CH_TABLE_NAME+" where "+ YEU_THICH + " = " + 1,null);
 		
 		c.moveToLast();
 		c.moveToNext();
 		
 		while(c.moveToPrevious()){
 			ChamNgon result= new ChamNgon();
 			result.setChid(c.getInt(c.getColumnIndex(CHID)));
 			result.setCid(c.getInt(c.getColumnIndex(CH_CID)));
 			result.setnDungAnh(c.getString(c.getColumnIndex(N_DUNG_ANH)));
 			result.setnDungViet(c.getString(c.getColumnIndex(N_DUNG_VIET)));
 			result.setTacGia(c.getString(c.getColumnIndex(TACGIA)));
 			result.setYeuthich(c.getInt(c.getColumnIndex(YEU_THICH)));
 			list.add(result);
 		}
 		System.out.println("danh dach cham ngon yeu thich" +" la "+ list.toString());
 		return list;
 	}
 	
 
 	public int getNumberChamNgonYeuThich(){
 		SQLiteDatabase sd= getWritableDatabase();
 		ArrayList<ChamNgon> list= new ArrayList<ChamNgon>();
 		Cursor c= sd.rawQuery("select * from "+CH_TABLE_NAME+" where "+ YEU_THICH + " = " + 1,null);
 		
 		c.moveToLast();
 		c.moveToNext();
 		
 		while(c.moveToPrevious()){
 			ChamNgon result= new ChamNgon();
 			result.setChid(c.getInt(c.getColumnIndex(CHID)));
 			list.add(result);
 		}
 		return list.size();
 	}
 	
 	
 	public void updateChamNgon(ChamNgon cn ){
 		SQLiteDatabase sd= getWritableDatabase();
 		
 		ContentValues values =new ContentValues();
 		
 		values.put(YEU_THICH, cn.getYeuthich());
 		sd.update(CH_TABLE_NAME, values, CHID+" = "+cn.getChid(), null);
 		System.out.println("update thanh cong");
 	}
 
 	
 	
 	
 	
 	public Content getContent(int cid){
 		SQLiteDatabase sd= getWritableDatabase();
 		Cursor c = sd.rawQuery("select * from "+C_TABLE_NAME+" where "+CID+" = "+ cid, null);
 		c.moveToNext();
 		
 		Content result =new Content();
 		result.setCid(c.getInt(c.getColumnIndex(CID)));
 		result.setTen(c.getString(c.getColumnIndex(TEN)));
 		System.out.println("get content thanh cong "+ result.toString());
 		return result;
 	}
 	
 	public ArrayList<Content>  getAllContent(){
 		SQLiteDatabase sd= getWritableDatabase();
 		ArrayList<Content> list = new ArrayList<Content>();
 		System.out.println("get all contennt");
 		Cursor c = sd.rawQuery("select * from "+C_TABLE_NAME, null);
 		System.out.println("thuc hien truy van thanh cong");
 		while(c.moveToNext()){
 			Content result =new Content();
 			result.setCid(c.getInt(c.getColumnIndex(CID)));
 			result.setTen(c.getString(c.getColumnIndex(TEN)));
 			
 			list.add(result);
 		}
 		return list;
 	}
 	
 	
 	//get number
 	/*public int maxIdProducts(){
 		
  		SQLiteDatabase sd = getWritableDatabase();
  		
  		Cursor c = sd.rawQuery("select max("+CHID+") from "+CH_TABLE_NAME, null);
  		c.moveToFirst();
  		if(!c.isNull(0)){
  			
  			return c.getInt(0);
  		}
  		return 0;
  		
  	}*/
 	
 }
