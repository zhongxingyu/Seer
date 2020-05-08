 package com.itkweb.xwinga.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class XWingDbTables extends SQLiteOpenHelper {
 
 	private static final int DATABASE_VERSION = 2;
 
 	public static final String TABLE_BOX = "box";
 	public static final String COL_ID = "ID";
 	public static final String COL_NAME = "Name";
 	public static final String COL_PICTURE = "Picture";
 
 	private static final String CREATE_BDD = "CREATE TABLE " + TABLE_BOX + " ("
 			+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_NAME
			+ " TEXT NOT NULL "+ COL_PICTURE
 			+ " TEXT NOT NULL);";
 
 	public XWingDbTables(Context context) {
 		super(context, TABLE_BOX, null, DATABASE_VERSION);
 		init();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// on cre la table  partir de la requte crite dans la variable
 		// CREATE_BDD
 		db.execSQL(CREATE_BDD);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// On peut faire ce qu'on veut ici moi j'ai dcid de supprimer la table
 		// et de la recrer
 		// comme a lorsque je change la version les id repartent de 0
 		db.execSQL("DROP TABLE " + TABLE_BOX + ";");
 		onCreate(db);
 	}
 
 	public void init() {
 		SQLiteDatabase db = getWritableDatabase();
 		// Cration d'un ContentValues (fonctionne comme une HashMap)
 		ContentValues values = new ContentValues();
 		// on lui ajoute une valeur associ  une cl (qui est le nom de la
 		// colonne dans laquelle on veut mettre la valeur)
 
 		for (BoxSet box : BoxSet.values()) {
 			values.put(COL_NAME, box.getName());
 			values.put(COL_PICTURE, box.getPicture());
 		}
 
 		// on insre l'objet dans la BDD via le ContentValues
 		db.insert(TABLE_BOX, null, values);
 
 		db.close();
 	}
 
 	public enum BoxSet {
 	    b1 ("Bote de base","SWX01.png"),
 	    b2 ("X-wing","SWX02.png"),
 	    b3 ("Chasseur TIE","SWX03.png"),
 	    b4 ("Y-wing","SWX04.png"),
 	    b5 ("TIE advanced","SWX05.png"),
 	    b6 ("Faucon Millenium","SWX06.png"),
 	    b7 ("Slave 1","SWX07.png"),
 	    b8 ("A-wing","SWX08.png"),
 	    b9 ("Intercepteur TIE","SWX09.png");
 
 	    private final String name;  
 	    private final String  picture; 
 	    BoxSet(String name, String picture) {
 	        this.name = name;
 	        this.picture = picture;
 	    }
 	    public String getName() { return name; }
 	    public String getPicture() { return picture; }
 
 	}
 	
 	
 }
