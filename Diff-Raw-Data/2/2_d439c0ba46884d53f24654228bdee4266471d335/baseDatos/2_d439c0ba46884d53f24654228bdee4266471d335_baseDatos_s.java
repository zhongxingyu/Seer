 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 
 public class baseDatos extends SQLiteOpenHelper {
 
 	public static String DB_PATH="/data/data/com.example.gastos/databases/";
 	public static String DB_NAME="db_gastos";
 	private final Context myContext;
 	public static int v_db=2;
 	
 	public baseDatos(Context context, String name, CursorFactory factory,int version) {
 		super(context, name, factory, version);
 		this.myContext=context;
 	}
 
 	
 	//--------------LOCAL---------------///
 	String sqlCreate="CREATE TABLE grupos (id_grupo INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			"nombre TEXT, miembros TEXT,sincronizacion INTEGER) "; 
 	String sqlCreate2="CREATE TABLE gastos (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 			"quien_pago TEXT, para_quien TEXT, cuanto INTEGER, concepto STRING, fecha NUMERIC, hora NUMERIC, foto TEXT, ubicacion TEXT)";
 	//falta deudas!!!!!!!!!!!!!!!		
	
 	
 	//String sqlUpdate="ALTER TABLE grupos ADD COLUMN " //PARA ACTUALZIAZ
 			
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		if(db.isReadOnly()){
 			db=getWritableDatabase();
 		}
 		db.execSQL(sqlCreate);
 		db.execSQL(sqlCreate2);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if(newVersion>oldVersion){
 		//	db.execSQL(sqlUpdate);
 		}
 		
 	}
 
 }
