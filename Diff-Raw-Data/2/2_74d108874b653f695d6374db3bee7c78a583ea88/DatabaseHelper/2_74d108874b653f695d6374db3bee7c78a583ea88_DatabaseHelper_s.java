 package ve.gob.cnti.android.database;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static String DB_NAME = "GobMovil.sqlite";
	public static final String DB_PATH = "/data/data/ve.gob.cnti.movil.app/databases/";
 	private SQLiteDatabase database;
 	private final Context context;
 
 	public DatabaseHelper(Context context) {
 		super(context, DB_NAME, null, 1);
 		this.context = context;
 	}
 
 	/**
 	 * Metodo Parar validar que el archivo se en cuentra en la ruta
 	 * predeterminada
 	 * 
 	 * @return
 	 */
 	private boolean checkDataBase() {
 		File dbFile = new File(DB_PATH + DB_NAME);
 		return dbFile.exists();
 	}
 
 	/**
 	 * Metodo que se utiliza en el Main de la aplicacion para crear la base de
 	 * datos dentro de ella
 	 * 
 	 * @throws IOException
 	 */
 	public void createDataBase(Context context) throws IOException {
 		boolean dbExist = checkDataBase();
 		if (!dbExist) {
 			try {
 				this.getReadableDatabase();
 				copyDataBase();
 			} catch (Exception e) {
 				throw new Error(e.getMessage());
 			}
 		}
 
 	}
 
 	/**
 	 * Metodop que permite copiar el archvivo de la base de datos en la carpeta
 	 * de instalacion del Sistema
 	 * 
 	 * @throws IOException
 	 */
 	public void copyDataBase() throws IOException {
 		/**
 		 * Abrimos la base de datos de la carpeta assets con un objeto de la
 		 * clase InputStream.
 		 */
 		InputStream myInput = context.getAssets().open(DB_NAME);
 		/**
 		 * Carpeta de destino donde hemos creado la base de datos vacía.
 		 */
 		String outFileName = DB_PATH + DB_NAME;
 		/**
 		 * Abrimos la base de datos vacía con un objeto de la clase
 		 * OutputStream.
 		 */
 		OutputStream myOutput = new FileOutputStream(outFileName);
 		/**
 		 * Transfiere los bytes entre el stream de entrada y el de salida.
 		 */
 		byte[] buffer = new byte[1024];
 		int length;
 		while ((length = myInput.read(buffer)) > 0) {
 			myOutput.write(buffer, 0, length);
 		}
 		/**
 		 * Cerramos los ficheros abiertos.
 		 */
 		myOutput.flush();
 		myOutput.close();
 		myInput.close();
 	}
 
 	/**
 	 * Metodo Para Abrir la base de datos
 	 * 
 	 * @throws SQLException
 	 */
 	public void openDatabaBase() throws SQLException {
 		String myPath = DB_PATH + DB_NAME;
 		database = SQLiteDatabase.openDatabase(myPath, null,
 				SQLiteDatabase.OPEN_READONLY);
 	}
 
 	/**
 	 * Metodo Utilizado para poder cerra la base de datos
 	 */
 	public synchronized void close() {
 		if (database != null)
 			database.close();
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
