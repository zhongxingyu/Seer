 package es.fenoll.javier;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.osmdroid.util.GeoPoint;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.util.Log;
 import android.util.Xml;
 
 
 public class AlmacenDatos  {
 
 	// Used for debugging and logging
     private static final String TAG = "AlmacenDatos";
     
     private static final String pathSesiones = "correpicos" + File.separator + "sesiones";
 
 	
 	/**
      * The database that the provider uses as its underlying data store
      */
     private static final String DATABASE_NAME = "correcaminos.db";
 
     /**
      * The database version
      */
     private static final int DATABASE_VERSION = 12;
 	
     // Handle to a new DatabaseHelper.
     private DatabaseHelper mOpenHelper;
 
     
 	public AlmacenDatos(Context context) {
 		
 		// Creates a new helper object. Note that the database itself isn't opened until
 	    // something tries to access it, and it's only created if it doesn't already exist.
 	    mOpenHelper = new DatabaseHelper(context);
 	    
 	}
 	
 	// Para crear  la bbdd
 	static class DatabaseHelper extends SQLiteOpenHelper {
 
 	       DatabaseHelper(Context context) {
 	           // calls the super constructor, requesting the default cursor factory.
 	            
 	    	   super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	       }
 
 	       /**
 	        *
 	        * Creates the underlying database with table name and column names taken from the
 	        * NotePad class.
 	        */
 	       @Override
 	       public void onCreate(SQLiteDatabase db) {
 	           db.execSQL("CREATE TABLE " + EstructuraDB.Punto.TABLE_NAME + " ("
 	                   + EstructuraDB.Punto._ID + " INTEGER PRIMARY KEY,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_SECUENCIA + " INTEGER,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_LAT + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_LONG + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_DISTANCIA + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_ALTITUD + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS + " DOUBLE,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_SESION + " INTEGER,"
 	                   + EstructuraDB.Punto.COLUMN_NAME_PRECISION + " LONG"
 	                   + ");");
 	           
 	           db.execSQL("CREATE TABLE " + EstructuraDB.Sesion.TABLE_NAME + " ("
 	                   + EstructuraDB.Sesion._ID + " INTEGER PRIMARY KEY," 
 	                   + EstructuraDB.Sesion.COLUMN_NAME_FECHA + " TEXT,"
 	                   + EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA + " LONG,"
 	                   + EstructuraDB.Sesion.COLUMN_NAME_DURACION + " DOUBLE,"
 	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_POS + " INTEGER,"
 	                   + EstructuraDB.Sesion.COLUMN_ALTITUD_NEG + " INTEGER,"
 	                   + EstructuraDB.Sesion.COLUMN_NAME_FICHERO + " TEXT,"
 	                   + EstructuraDB.Sesion.COLUMN_NAME_DESC + " TEXT"
 	                   + ");");
 	           
 	           db.execSQL("CREATE TABLE " + EstructuraDB.Deportes.TABLE_NAME + " ("
 	                   + EstructuraDB.Deportes._ID + " INTEGER PRIMARY KEY," 
 	                   + EstructuraDB.Deportes.COLUMN_NAME_NOMBRE + " TEXT,"
 	                   + EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST + " INTEGER,"
 	                   + EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO + " INTEGER,"
 	                   + EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE + " BOOLEAN,"
 	                   + EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE + " DOUBLE"
 	                   
 	                   + ");");
 	           db.execSQL("INSERT INTO " + EstructuraDB.Deportes.TABLE_NAME 
	        		   + " VALUES (0,'correr',10,6, 'true', 0.5);");
 	        		   
 	           
 	           
 	       }
 
 	       /**
 	        *
 	        * Demonstrates that the provider must consider what happens when the
 	        * underlying datastore is changed. In this sample, the database is upgraded the database
 	        * by destroying the existing data.
 	        * A real application should upgrade the database in place.
 	        */
 	       @Override
 	       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
 	    	   
 	           // Logs that the database is being upgraded
 	           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 	                   + newVersion + ", which will destroy all old data");
 
 	           
     
                db.execSQL("ALTER TABLE " + EstructuraDB.Sesion.TABLE_NAME
        		   		+ " ADD COLUMN " + EstructuraDB.Sesion.COLUMN_NAME_DESC + " TEXT"
        		   		+ ";"
        		   );
                /*
                db.execSQL("ALTER TABLE " + EstructuraDB.Sesion.TABLE_NAME
           		   		+ " ADD COLUMN " + EstructuraDB.Sesion.COLUMN_ALTITUD_NEG + " INTEGER"
           		   		+ ";"
           		   );
                */
 	           /*
 	           db.execSQL("ALTER TABLE " + EstructuraDB.Deportes.TABLE_NAME
 	        		   		+ " ADD COLUMN " + EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE + " DOUBLE"
 	        		   		+ ";"
 	                   
 	        		   );
 	           */
 	           /*
 	           
 	           // Kills the table and existing data
 	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Punto.TABLE_NAME );
 	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Sesion.TABLE_NAME );
 	           db.execSQL("DROP TABLE IF EXISTS " + EstructuraDB.Deportes.TABLE_NAME );
 
 	           // Recreates the database with a new version
 	           onCreate(db);
 	           
 	           */
 	           
 	       }
 	   }
 
 	public long insertaPunto(ContentValues  valores) {
 		
 		// Opens the database object in "write" mode.
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         
         // Performs the insert and returns the ID of the new note.
         long rowId = db.insert(
         	EstructuraDB.Punto.TABLE_NAME,        // The table to insert into.
         	null,
         	valores                           // A map of column names, and the values to insert
                                              // into the columns.
         );
 
 
 		return rowId;
 		}
 	
 	//actualiza la sesion con la distancia y tiempototales al terminar
 	public void terminaSesion(long rowid, double distancia, double duracion, int altitudPos, int altitudNeg) {
 		
 		//Al tyerminar la sesion la guerdo en el gpx
 		String nomFicheroSesion = guardaSesionGPX(rowid).nomFicheroSesion;
 		
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		
 		ContentValues  valores = new ContentValues();
         valores.put(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA, distancia );
         valores.put(EstructuraDB.Sesion.COLUMN_NAME_DURACION, duracion );
         valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_POS, altitudPos );
         valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG, altitudNeg );
         valores.put(EstructuraDB.Sesion.COLUMN_NAME_FICHERO,nomFicheroSesion);
         
 		db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + rowid, null);
 		
 		db.close();
 		
 		
 	}
 	
 	//actualiza la sesion con la distancia y tiempototales al terminar
 	public void actualizaDescSesion(long rowid, String desc) {
 			
 			
 			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 			
 			ContentValues  valores = new ContentValues();
 	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DESC, desc );
 	       
 	        
 			db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + rowid, null);
 			
 			db.close();
 			
 		}
 	
 	// crea una sesion nueva y devuelve su rowid, automatimente pone la fecha de inicio
 	public long insertaSesion() {
 		
 		// Opens the database object in "write" mode.
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
           
         Calendar c = Calendar.getInstance();
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String formattedDate = df.format(c.getTime());
 
         ContentValues  valores = new ContentValues();
         valores.put(EstructuraDB.Sesion.COLUMN_NAME_FECHA, formattedDate );
         
         // Performs the insert and returns the ID of the new note.
         long rowId = db.insert(
         	EstructuraDB.Sesion.TABLE_NAME,        // The table to insert into.
         	null,
         	valores                           // A map of column names, and the values to insert
                                              // into the columns.
         );
 
 
 		return rowId;
 		}
 	
 	/*
 	 * Return a Cursor over the list of all sesiones in the database
      * 
      * @return Cursor over sesiones, si se quieren todas las sesiones, pasar un -1
      */
 	public Cursor recuperaSesiones(long sesionId) {
 		
 		// Opens the database object in "read" mode.
         SQLiteDatabase db = mOpenHelper.getReadableDatabase();
           
 		String strQuery = null;
 		String strOrder = null;
 		if (sesionId > 1) {
 			
 			strQuery = EstructuraDB.Sesion._ID + "=" + sesionId;
 		}
 		else {
 			// if multiple records order by 
 			strOrder =  EstructuraDB.Sesion.COLUMN_NAME_FECHA + " DESC" ;
 		}
 		
         
 		 return db.query(
 				 EstructuraDB.Sesion.TABLE_NAME, 
 				 new String[] {	EstructuraDB.Sesion._ID,
 						 		EstructuraDB.Sesion.COLUMN_NAME_FECHA,
 								EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA ,
 								EstructuraDB.Sesion.COLUMN_NAME_DURACION,
 								EstructuraDB.Sesion.COLUMN_ALTITUD_POS,
 								EstructuraDB.Sesion.COLUMN_ALTITUD_NEG,
 								EstructuraDB.Sesion.COLUMN_NAME_DESC}
 		 		, strQuery, null, null, null, strOrder
 		 		);
 		
 	}
 	
 	
 	// dado un id de sesion, devuelve el parser para emprezar a leer los puntos
 	public XmlPullParser  PreparaRecuperaPuntosSesionGpx(long sesionId) {
 		
 		// borro el fichero GPX 
         String NomFichero = getNomFicheroPuntosSesion(sesionId);
     	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
     	File file = new File(path, NomFichero);
         //compruebo si la SD esta bien
     	String state = Environment.getExternalStorageState();
     	if (! Environment.MEDIA_MOUNTED.equals(state)) {
     	    // MEDIA_MOUNTED means we can read and write the media
     		return null;
     	}
     	
     	InputStream gpxreader;
     	XmlPullParser parser = Xml.newPullParser();
 		
     	try {
     		
 			gpxreader = new FileInputStream(file);
 			parser.setInput(gpxreader, null);
 			
     	} catch (FileNotFoundException e) {
 			//e.printStackTrace();
 			return null;
 		} catch (XmlPullParserException e) {
 			//e.printStackTrace();
 			return null;
 		}
     	
     	return parser;
     	
 	}
 			
 			
 			
 	public PuntoGPX  RecuperaPuntoSesionGpx ( XmlPullParser parser) {
 			
 			int evento;
 			String etiqueta = null;
 			PuntoGPX elPunto = null;
 			String valor="";
 			long puntosSesion=0;
 			
 			try {
 				
 				
 				
 				
 				evento = parser.next();
 				
 				
 				while ( evento != XmlPullParser.END_DOCUMENT ) {
 					
 
 					switch (evento) {
 					
 					case XmlPullParser.END_DOCUMENT:
 						
 						return null;
 						
 					case XmlPullParser.START_DOCUMENT:
 						 
 	                    break;
 	 
 	                case XmlPullParser.START_TAG:
 	 
 	                	valor = "";
 	                    etiqueta = parser.getName();
 	 
 	                    if (etiqueta.equals("trkpt"))  {
 	                    	
 	                    	elPunto = new PuntoGPX();
 	                    	elPunto.puntosSesion = puntosSesion;
 	                    	elPunto.posicion = new GeoPoint( Double.valueOf( parser.getAttributeValue(0))  , Double.valueOf( parser.getAttributeValue(1) ) );
 	                    	
 	                        }
 	                    
 	                    break;
 	                    
 	                case XmlPullParser.TEXT:
 	                	
 	                	valor += parser.getText();
 	                	break;
 	                    
 	                case XmlPullParser.END_TAG:
 	                	
 	                	etiqueta = parser.getName();
 	               	 
 	                    if (etiqueta.equals("trkpt"))  {
 	                    	// si ha terminado el punto pues lo devuelvo
 	                    	return elPunto;
 	                    	
 	                        }
 	                    else if(etiqueta.equals("ele")) {
 	                    	elPunto.altitud = Double.valueOf(valor).longValue();
 	                    }
 	                    else if(etiqueta.equals("dist")) {
 	                    	elPunto.distancia = Double.valueOf(valor).longValue();
 	                    }
 	                    else if(etiqueta.equals("tmpt")) {
 	                    	elPunto.tiempo = Double.valueOf(valor).longValue();
 	                    }
 	                    else if(etiqueta.equals("seq")) {
 	                    	elPunto.index = Double.valueOf(valor).longValue();
 	                    }
 	                    else if(etiqueta.equals("numpuntos")) {
 	                    	puntosSesion = Double.valueOf(valor).longValue();
 	                    }
 	                    
 	                    valor = "";
 	                	
 	                	break;
 					
 					}
 					
 					evento = parser.next();
 				
 				}
 				
 	
 			} catch (XmlPullParserException e) {
 				//e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				//e.printStackTrace();
 				return null;
 			}
 			
 			
 			return elPunto;
 	
 	}
 	
 	/**
 	 * Return a Cursor over the list of all puntos of a known sesion
      * 
      * @return Cursor over all puntos de una sesion
      */
 	
 	public Cursor recuperaPuntosSesion(long sesionId) {
 		
 		// Opens the database object in "read" mode.
         SQLiteDatabase db = mOpenHelper.getReadableDatabase();
           
 		 return db.query(
 				 EstructuraDB.Punto.TABLE_NAME, 
 				 new String[] {	EstructuraDB.Punto.COLUMN_NAME_SECUENCIA,
 						 		EstructuraDB.Punto.COLUMN_NAME_LAT, 
 						        EstructuraDB.Punto.COLUMN_NAME_LONG,
 						        EstructuraDB.Punto.COLUMN_NAME_DISTANCIA,
 						        EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD,
 						        EstructuraDB.Punto.COLUMN_NAME_ALTITUD,
 						        EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS,
 						        EstructuraDB.Punto.COLUMN_NAME_PRECISION}
 		 		, EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionId , null, null, null, null
 		 		);
 		
 	}
 	
 	public String getNomFicheroPuntosSesion(long sesionId) {
 		
 		// Opens the database object in "read" mode.
         SQLiteDatabase db = mOpenHelper.getReadableDatabase();
           
         Cursor c=  db.query(
 			 EstructuraDB.Sesion.TABLE_NAME, 
 			 new String[] {	EstructuraDB.Sesion.COLUMN_NAME_FICHERO}
 	 		, EstructuraDB.Sesion._ID + "=" + sesionId, null, null, null, null
 	 		);
 		
         c.moveToFirst();
         
         String resultado = c.getString( c.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FICHERO));
         
         c.close();
         
 		return resultado ;
 	
 	}
 	
 	
 	// borra una sesion y toos sus puntos dado du id
 	public boolean borraSesion(long sesionId) {
 			
 		// Opens the database object in "write" mode.
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         
         // borro el fichero GPX 
         String NomFichero = getNomFicheroPuntosSesion(sesionId);
     	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
     	File file = new File(path, NomFichero);
         //compruebo si la SD esta bien
     	String state = Environment.getExternalStorageState();
     	if (! Environment.MEDIA_MOUNTED.equals(state)) {
     	    // MEDIA_MOUNTED means we can read and write the media
     		return false;
     	}
     	file.delete();
 
         // Borro los puntos por si los hubiese
         db.delete(
         	EstructuraDB.Punto.TABLE_NAME,        
         	EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionId,
         	null                           
         );
         
      // Borro la sesion
         db.delete(
         	EstructuraDB.Sesion.TABLE_NAME,        
         	EstructuraDB.Sesion._ID + "=" + sesionId,
         	null                           
         );
         
 		
         db.close();
         
 		return true;
 	}
 
 	public class Deporte{
 		
 		public String nombre;
 		public int gpsgapdist;
 		public int gpsgaptmp;
 		public boolean autopause;
 		public Double umbralautopause;
 		
 	}
 	
 	public class PuntoGPX{
 		
 		public GeoPoint posicion;
 		public Long distancia;
 		public Long altitud;
 		public Long index;
 		public Long tiempo;
 		public Long puntosSesion;
 		
 	}
 	
 	public class Sesion {
 		
 		public Long distancia;
 		public Long duracion;
 		public Long altitudPos;
 		public Long altitudNeg;
 		public String nomFicheroSesion;
 		
 	}
 	
 	
 	//solo abro y cierro la DB en modo escritura para que el metodo onUpgrade se lance con permisos si lo necesita
 	// este metodo se llamara nada mas arrancar la app y solo en ese momento
 	public void  ForceUpgradeDB(){
 		
 		// Opens the database object in "write" mode.
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         db.close();
 		
 	}
 	
 	
 	public Deporte recuperaDeporte(long deporteId) {
 		// Opens the database object in "read" mode.
         SQLiteDatabase db = mOpenHelper.getReadableDatabase();
         Deporte resultado = new Deporte();  
         
 		Cursor c = db.query(
 				 EstructuraDB.Deportes.TABLE_NAME, 
 				 new String[] {	EstructuraDB.Deportes.COLUMN_NAME_NOMBRE,
 						 		EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST, 
 						        EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO,
 						        EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE,
 						        EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE}
 		 		, EstructuraDB.Deportes._ID + "=" + deporteId , null, null, null, null
 		 		);
 		        
         c.moveToFirst();
         
         resultado.nombre = c.getString( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_NOMBRE));
         resultado.gpsgapdist = c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST));
         resultado.gpsgaptmp = c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO));
         
         if ( c.getInt( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE)) == 1) {
         	resultado.autopause = true;
         }
         else {
         	resultado.autopause = false;
         }
         	
         resultado.umbralautopause = c.getDouble( c.getColumnIndex(EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE));
         
         
         return resultado;
 	}
 	
 	public void actualizaDeporte(long rowid, Deporte elDeporte) {
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		
 		ContentValues  valores = new ContentValues();
         valores.put(EstructuraDB.Deportes.COLUMN_NAME_NOMBRE, elDeporte.nombre );
         valores.put(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_DIST, elDeporte.gpsgapdist );
         valores.put(EstructuraDB.Deportes.COLUMN_NAME_GPS_GAP_TIEMPO, elDeporte.gpsgaptmp );
         valores.put(EstructuraDB.Deportes.COLUMN_NAME_AUTOPAUSE, elDeporte.autopause );
         valores.put(EstructuraDB.Deportes.COLUMN_NAME_UMBRAL_AUTOPAUSE, elDeporte.umbralautopause );
         
 		db.update(EstructuraDB.Deportes.TABLE_NAME,valores,EstructuraDB.Deportes._ID + "=" + rowid, null);
 		
 		db.close();
 	}
 	
 	// guarda la sesion en GPX y devuelve el nombre del fichero (sin ruta)
 	public Sesion guardaSesionGPX(long sesionID ) {
 		
 		//compruebo si la SD esta bien
     	String state = Environment.getExternalStorageState();
     	if (! Environment.MEDIA_MOUNTED.equals(state)) {
     	    // MEDIA_MOUNTED means we can read and write the media
     		return null;
     	} 
     	
     	// obtengo lo que necesito de la sesion
     	Cursor cSesiones  = recuperaSesiones(sesionID);
                 
         if (cSesiones.getCount() != 1) {    
 			return null;          
 			}    
 		
         cSesiones.moveToFirst();
         
         String nomFicheroExport="";
         String nomSesion="";
         String comentSesion="";
         
         nomSesion = "Sesion CorrePicos ID " + sesionID;
         String distSesion = cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA)  );
         String durSesion = cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_DURACION)  );
         
         
         comentSesion = distSesion + "Km - Duracion " + durSesion;
     	
         try {
 	    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    	Date lafecha = df.parse(cSesiones.getString( cSesiones.getColumnIndex(EstructuraDB.Sesion.COLUMN_NAME_FECHA)  ))  ;
 	    	df = new SimpleDateFormat("yyyy-M-dd_HH_mm");
 			nomFicheroExport = df.format(lafecha);
 			
 			comentSesion += " - cuando " + nomFicheroExport;
 			
 			nomFicheroExport += ".gpx";
 			
         }
         catch (ParseException e) {
 			//e.printStackTrace();
 						} 
         
     	File path = new File(Environment.getExternalStorageDirectory()+ File.separator  + pathSesiones);
     	File file = new File(path, nomFicheroExport);
     	
     	Cursor cPuntos = recuperaPuntosSesion(sesionID);
     	cPuntos.moveToFirst();
     	
     	
 
          String lat = "", lon="", alt="", hdop="", seq="", dist=distSesion, vel="", tmpt=durSesion;
          Double altitudPos=0.0, altitudNeg=0.0, altitud=null, altitudAnt=null;
         
 
     	 try {
     		 
          	 path.mkdirs();
  	    	 FileWriter gpxwriter = new FileWriter(file);
  	         BufferedWriter out = new BufferedWriter(gpxwriter);
  	         
  	        
  	         //escribo las cabeceras del gpx
  	        
  	         out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
  	        
  	         out.write("<gpx\n");
  	         out.write("version='1.1'\n");
  	         out.write("creator='Correpicos - http://www.corepicos.es/'\n");
  	    	 out.write("xmlns='http://www.topografix.com/GPX/1/1'\n");
  	    	 out.write("xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n");
  	    	 out.write("xmlns:cpc='http://www.corepicos.es/gpx/CpcExtension/v1'\n");
  	    	 out.write("xsi:schemaLocation='http://www.topografix.com/GPX/1/1\n");
  	    	 out.write("http://www.topografix.com/GPX/1/1/gpx.xsd\n");
  	    	 out.write("http://www.corepicos.es/gpx/CpcExtension/v1\n");
  	    	 out.write("http://www.corepicos.es/gpx/CpcExtension/v1/CpcExtensionv1.xsd'>\n");
  	         
  	    	 out.write("<trk>\n");
  	    	 out.write("<name>" + nomSesion + "</name>\n");
  	    	 out.write("<cmt>" + comentSesion + "</cmt>\n");
  	    	
  	    	 out.write("<trkseg>\n");
  	    	
  	    	 long numPuntos = cPuntos.getCount();
  	    	 lat = ""; lon=""; alt=""; hdop=""; seq=""; dist="0"; vel="0"; tmpt="0";
  	    	 
  	    	 out.write("<extensions>\n");
         	 out.write("<cpc:CrcExtension>\n");
         	 out.write("<cpc:numpuntos>" + numPuntos + "</cpc:numpuntos>\n");  	
         	 out.write("</cpc:CrcExtension>\n");
         	 out.write("</extensions>\n");
  	    	 
  	    	 
  	    	 if ( numPuntos > 0 ) {
  	    		 
  	    	 
  	       
 	 	         do {
 	 	        	
 	 	        	 
 	 	        	 lat = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_LAT)) );
 	 	        	 lon =  Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_LONG)) );
 	 	        	 altitud = cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_ALTITUD));
 	 	        	 alt = Double.toString( altitud );
 	 	        	 hdop = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_PRECISION)) );
 	 	        	 seq = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_SECUENCIA)) );
 	 	        	 dist = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_DISTANCIA)) );
 	 	        	 vel = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_VELOCIDAD)) );
 	 	        	 tmpt = Double.toString( cPuntos.getDouble( cPuntos.getColumnIndex(EstructuraDB.Punto.COLUMN_NAME_TIEMPOTRANS)) );
 	 	        	 
 	 	        	 if (altitud != null ) {
 	 	        		 
 	 	        		 if (altitudAnt != null) {
 	 	        			 
 	 	        			double cambioAltid = altitud - altitudAnt;
 	 	        			if (cambioAltid > 0) {
 	 	        				altitudPos += cambioAltid;
 	 	        			}
 	 	        			else {
 	 	        				altitudNeg += -1 * cambioAltid;
 	 	        			}
 	 	        	 
 	 	        		 }
 	 	        		altitudAnt = altitud;
 	 	        		 
 	 	        		 
 	 	        		 
 	 	        	 }
 	 	        	 
 	 	        	 out.write("<trkpt ");
 	 	        	 out.write("lat='" +  lat + "'");
 	 	        	 out.write(" lon='" + lon + "'>\n");
 	 	        	 out.write("<ele>" + alt + "</ele>\n");
 	 	        	 out.write("<hdop>" + hdop + "</hdop>\n");
 	 	        	
 	 	        	 out.write("<extensions>\n");
 	 	        	 out.write("<cpc:CrcExtension>\n");
 	 	        	 out.write("<cpc:seq>" + seq + "</cpc:seq>\n");
 	 	        	 out.write("<cpc:dist>" + dist + "</cpc:dist>\n");
 	 	        	 out.write("<cpc:vel>" + vel + "</cpc:vel>\n");
 	 	        	 out.write("<cpc:tmpt>" + tmpt + "</cpc:tmpt>\n");     	
 	 	        	 out.write("</cpc:CrcExtension>\n");
 	 	        	 out.write("</extensions>\n");
 	 	        	 
 	 	        	 out.write("</trkpt>\n");
 	 	        	 
  	        	             	
  	         	} while (cPuntos.moveToNext() );
  	         
  	    	 }
             	
  	         
  	        out.write("</trkseg>\n");
  	        out.write("</trk>\n");
  	    	out.write("</gpx>\n");
 
  	    	out.close();
  	        
     	 }
     	 catch (IOException e) {
      	    //Log.e("salvaSesion", "Could not write file " + e.getMessage());
      	    return null;
      	 }
     	 
 
     	 // Borro los puntos
     	 SQLiteDatabase db = mOpenHelper.getWritableDatabase();
          db.delete(
          	EstructuraDB.Punto.TABLE_NAME,        
          	EstructuraDB.Punto.COLUMN_NAME_SESION + "=" + sesionID,
          	null                           
          );
 		 //db.close();
 		 
 		 Sesion returnSesion = new Sesion();
 		
 		 returnSesion.nomFicheroSesion = nomFicheroExport;
 		 returnSesion.distancia = Double.valueOf(dist).longValue();
 		 returnSesion.duracion = Double.valueOf(tmpt).longValue();
 		 returnSesion.altitudPos = altitudPos.longValue();
 		 returnSesion.altitudNeg = altitudNeg.longValue();
 		 
 		 
 		 return returnSesion ;
 		
 	}
 	
 	//actualiza la sesion con la distancia y tiempototales al terminar
 	public void PasaSesionesSD() {
 		
 			
 		
 		// Opens the database object in "read" mode.
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                 
 		Cursor cSesiones =  db.query(
 				 EstructuraDB.Sesion.TABLE_NAME, 
 				 new String[] {	EstructuraDB.Sesion._ID}
 		 		, EstructuraDB.Sesion.COLUMN_NAME_FICHERO + " is null", null, null, null, null
 		 		);
 		
 		cSesiones.moveToFirst();
 		
 		if (cSesiones.getCount() == 0) {
 			
 			db.close();
 			return;
 		}
 		
 		
 		do {
 			
 			long sesionID = cSesiones.getLong(0);
 			
 			Sesion laSesion = guardaSesionGPX(sesionID);
 			
 			ContentValues  valores = new ContentValues();
 	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DISTANCIA, laSesion.distancia );
 	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_DURACION, laSesion.duracion );
 	        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_POS, laSesion.altitudPos );
 	        valores.put(EstructuraDB.Sesion.COLUMN_ALTITUD_NEG, laSesion.altitudNeg );
 	        valores.put(EstructuraDB.Sesion.COLUMN_NAME_FICHERO,laSesion.nomFicheroSesion);
 	        
 			db.update(EstructuraDB.Sesion.TABLE_NAME,valores,EstructuraDB.Sesion._ID + "=" + sesionID, null);
 			
 			
 			
 		}while(cSesiones.moveToNext());
 	
 		
 		db.close();
 			
 		}
 		
 	
 	
 }
