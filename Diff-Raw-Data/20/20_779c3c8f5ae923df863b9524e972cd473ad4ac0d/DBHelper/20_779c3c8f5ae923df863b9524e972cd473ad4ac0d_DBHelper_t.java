 package in.grat.esd2;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 //import android.util.Log;
 
 public class DBHelper extends SQLiteOpenHelper {
 		 
 	//The Android's default system path of your application database.
 	static final String DATABASE_NAME = "esd";
     static final String TABLE_NAME = "esd";
     static final String LANG_TABLE_NAME = "languages";
     static final String KEY_DATE = "date";
     static final String KEY_TEXT = "dailytext";
     static final String KEY_REFS = "refs";
     static final String KEY_LANG = "lang";
 	
     String mPath;	    
 	private SQLiteDatabase myDataBase; 
 	private final Context myContext;
 	private VerseParts mVerseParts;
 	public boolean PartsExtractedOK;
 	 
     /**
      * Constructor
      * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
      * @param context
      */
     public DBHelper(Context context) {
     	super(context, DATABASE_NAME, null, 1);
         this.myContext = context;
         mPath = "/data/data/in.grat.esd2/databases/esd";
         try {
 			createDataBase();
 			myDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
     }
  
     /**
      * Creates a empty database on the system and rewrites it with your own database.
      * */
     public void createDataBase() throws IOException{
     	boolean dbExist = checkDataBase();
     	if(dbExist){
     		//do nothing - database already exist
     	} else {
            	try {
            		this.getReadableDatabase();
     			copyDataBase();
     		} catch (IOException e) {
         		throw new Error("Error copying database");
         	}
     	}
     }
  
     /**
      * Check if the database already exist to avoid re-copying the file each time you open the application.
      * @return true if it exists, false if it doesn't
      */
     private boolean checkDataBase(){
     	SQLiteDatabase checkDB = null;
     	try{
     		checkDB = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
  
     	} catch (SQLiteException e){
     		//database does't exist yet. 
     	}
     	if(checkDB != null){
     		checkDB.close();
     	}
     	return checkDB != null ? true : false;
     }
  
     /**
      * Copies your database from your local assets-folder to the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transfering bytestream.
      * */
     private void copyDataBase() throws IOException{
     	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
     	OutputStream myOutput = new FileOutputStream(mPath);
     	//transfer bytes from the inputfile to the outputfile
     	byte[] buffer = new byte[1024];
     	int length;
     	while ((length = myInput.read(buffer))>0){
     		myOutput.write(buffer, 0, length);
     	}
     	//Close the streams
     	myOutput.flush();
     	myOutput.close();
     	myInput.close();
     }
  
 //    public void openDataBase() throws SQLException{
 //    	//Open the database
 //        String myPath = DATABASE_PATH + DATABASE_NAME;
 //    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 //    }
  
 
     
     @Override
 	public synchronized void close() {
     	if(myDataBase != null)
     		myDataBase.close();
    	    super.close();
 	}
  
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		mPath = db.getPath();
 		
 		// db.execSQL("CREATE TABLE "+TABLE_NAME+" (_id integer primary key autoincrement, "+KEY_DATE+" TEXT, "+KEY_TEXT+" TEXT, "+KEY_REFS+" TEXT);");
 	}
 	public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
 	    List<String> ar = null;
 	    Cursor c = null;
 	    try {
 	        c = db.rawQuery("select * from " + tableName + " limit 1", null);
 	        if (c != null) {
 	            ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
 	        }
 	    } catch (Exception e) {
 	        //Log.v(tableName, e.getMessage(), e);
 	        e.printStackTrace();
 	    } finally {
 	        if (c != null)
 	            c.close();
 	    }
 	    return ar;
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		List<String> columns = GetColumns(db, TABLE_NAME);
 		if (! columns.contains(new String("lang"))) 
 			db.execSQL("ALTER TABLE "+TABLE_NAME+" ADD COLUMN lang TEXT DEFAULT 'E';");
 	}
 
 	public String[] fetchText(String date, String type, String lang) {
 		String doc = type.equalsIgnoreCase("document") ? KEY_TEXT : KEY_REFS;
 		String[] results = null;
 		try {
 			Cursor mCursor = myDataBase.query(TABLE_NAME, 
 				new String[] { KEY_DATE, doc },
 				KEY_DATE + "='"+date+"' AND "+ KEY_LANG + "='"+lang.toLowerCase()+"'", 
 				null, null, null, null, null);
 			
 			if (mCursor.getCount() < 1) {
 				mCursor.close();
 			} else {
 				mCursor.moveToFirst();
 				results = new String[] { mCursor.getString(mCursor
 						.getColumnIndex(doc)) };
 				mVerseParts = extractParts(results[0]);
 				mCursor.close();
 			}
 			
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 		return results;
 
 	}
 	
 	
 	public class VerseParts { 
 		public String date, verse, text;
 		public VerseParts(String date, String verse, String text) {
 			this.date = date;
 			this.verse = verse;
 			this.text = text;
 		}
 	}
 	
 	public VerseParts getVerseParts(String date, String type, String lang) {
 		PartsExtractedOK = false;
 		fetchText(date, type, lang);
 		return mVerseParts;
 	}
 	
 	public VerseParts extractParts(String doc) {
 		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
     	builderFactory.setNamespaceAware(true);
     	InputSource inStream = new InputSource();
     	String sVerse = "verse not found";
     	String sDate = "unidentified date";
    	String sText = "";
     	DocumentBuilder builder;
 		try {
 			inStream.setCharacterStream(new StringReader(doc));
 			builder = builderFactory.newDocumentBuilder();
 			Document document = builder.parse(inStream);
 			NodeList nodes = document.getElementsByTagName("p");
 			for (int i=0; i<nodes.getLength(); i++) {
 				Node cls = nodes.item(i).getAttributes().getNamedItem("class");
 				if ( cls != null ) {
 					TransformerFactory transFactory = TransformerFactory.newInstance();
 					Transformer transformer = transFactory.newTransformer();
 					StringWriter buffer = new StringWriter();
 					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 					transformer.transform(new DOMSource(nodes.item(i)), new StreamResult(buffer));
 					if (cls.getNodeValue().equalsIgnoreCase("sa")) { 
 						sVerse = buffer.toString();
 					} else if (cls.getNodeValue().equalsIgnoreCase("ss")) { 
 						sDate = buffer.toString();
 					} else if (cls.getNodeValue().equalsIgnoreCase("sb")) {
						sText += buffer.toString();
 					}
 					
  				}
 			}
			sText = sText.length() < 1 ? "No text found!" : sText;
 			PartsExtractedOK = true;
 			return new VerseParts(sDate, sVerse, sText);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 		return null;
 			
 	}
 	
 	public Cursor removeEntriesFor(String year, String lang) {
 		String q = "DELETE FROM " + TABLE_NAME + " WHERE date LIKE '" + year + "%' AND lang='" + lang + "' COLLATE NOCASE";
 		Cursor mCursor = myDataBase.rawQuery(q, null);
 		return mCursor;
 	}
 
 	public Cursor fetchLanguages() throws SQLException {
 		String q =  "SELECT DISTINCT id as _id, name FROM "+LANG_TABLE_NAME+" INNER JOIN "+TABLE_NAME+" on "+LANG_TABLE_NAME+".id = "+TABLE_NAME+".lang COLLATE NOCASE";
 		Cursor mCursor = myDataBase.rawQuery(q, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 
 	public String getLanguageById(String langId) {
 		String q = "SELECT name FROM "+LANG_TABLE_NAME+" WHERE id='"+langId.toUpperCase()+"'";
 		Cursor mCursor = myDataBase.rawQuery(q, null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 			return mCursor.getString(0);
 		}
 		return "Unidentified Language!";
 	}
 	
 }
