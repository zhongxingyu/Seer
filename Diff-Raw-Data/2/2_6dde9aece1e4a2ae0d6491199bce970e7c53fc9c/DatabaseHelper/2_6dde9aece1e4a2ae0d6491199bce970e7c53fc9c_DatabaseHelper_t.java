 package com.b50.hoproll;
 
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import com.b50.hoproll.R;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import android.widget.Toast;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	public static final String DATABASE_NAME = "hops_catalog";
 	public static final int DATABASE_VERSION = 2;
 	private Context context;
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String s;
 		try {
 			InputStream in = this.context.getResources().openRawResource(R.raw.sql);
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(in, null);
 			NodeList statements = doc.getElementsByTagName("statement");
 			for (int i = 0; i < statements.getLength(); i++) {
 				s = statements.item(i).getChildNodes().item(0).getNodeValue();
 				db.execSQL(s);
 			}
 		} catch (Throwable t) {
 			Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		//added for version #2
		db.execSQL("INSERT INTO 'hops' VALUES(100,'Zythos','New IPA style hop blend created to optimize and exceed the aroma characteristics of the traditional, and sometimes hard to get, IPA hops.','Amarillo, Columbus, Cascade','9.5 to 12','IPAs','Bittering and Aroma', '');");
 	}
 	
 	public static void cleanupResources(SQLiteDatabase db, Cursor cursor){
 		try{ cursor.close(); } catch (Exception e) {}		
 		try{ db.close(); }catch(Exception e){}
 	}
 	
 	public static void cleanupCurosr(Cursor cursor){
 		try {		
 			if (cursor !=null && !cursor.isClosed()) {			
 				cursor.close();
 			}
 		} catch (Exception e) {
 			Log.d("hoproll", "exception in so closing cursor: " + e.getLocalizedMessage());
 		}
 	}
 
 }
