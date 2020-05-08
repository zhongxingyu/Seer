 package is.ru.app.db;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 
 public class PuzzleAdapter {
	 SQLiteDatabase db;
 	    DBHelper dbHelper;
 	    Context  context;
 
 	    public PuzzleAdapter( Context c ) {
 	        context = c;
 	    }
 
 	    public PuzzleAdapter openToRead() {
 	        dbHelper = new DBHelper( context );
 	        db = dbHelper.getReadableDatabase();
 	        return this;
 	    }
 
 	    public PuzzleAdapter openToWrite() {
 	        dbHelper = new DBHelper( context );
 	        db = dbHelper.getWritableDatabase();
 	        return this;
 	    }
 
 	    public void close() {
 	        db.close();
 	    }
 
	    public long insertPuzzle( int sid, String setup, int level, boolean solved ) {
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        ContentValues contentValues = new ContentValues();
 	        contentValues.put( cols[1], ((Integer)sid).toString() );
 	        contentValues.put( cols[2], setup );
 	        contentValues.put( cols[3], level );
 	        contentValues.put( cols[4], solved ? "1" : "0" );
 	        openToWrite();
 	        long value = db.insert(DBHelper.TablePuzzles, null, contentValues );
 	        close();
 	        return value;
 	    }
 
 	    public long updatePuzzle( int sid, String setup, int level, boolean solved ) {
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        ContentValues contentValues = new ContentValues();
 	        contentValues.put( cols[1], ((Integer)sid).toString() );
 	        contentValues.put( cols[2], setup );
 	        contentValues.put( cols[3], level );
 	        contentValues.put( cols[4], solved ? "1" : "0" );
 	        openToWrite();
 	        long value = db.update(DBHelper.TablePuzzles, contentValues, cols[1] + "=" + sid, null );
 	        close();
 	        return value;
 	    }
 	    
 	    public long updatePuzzle( int sid, boolean playing ) {
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        ContentValues contentValues = new ContentValues();
 	        contentValues.put( cols[1], ((Integer)sid).toString() );
 	        contentValues.put( cols[5], playing ? "1" : "0" );
 	        openToWrite();
 	        long value = db.update(DBHelper.TablePuzzles, contentValues, cols[1] + "=" + sid, null );
 	        close();
 	        return value;
 	    }
 	    
 	    public long updatePuzzleRestPlaying( ) {
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        ContentValues contentValues = new ContentValues();
 	        contentValues.put( cols[5], "0" );
 	        openToWrite();
 	        long value = db.update(DBHelper.TablePuzzles, contentValues, null, null );
 	        close();
 	        return value;
 	    }
 
 	    public Cursor queryPuzzles() {
 	        openToRead();
 	        Cursor cursor = db.query( DBHelper.TablePuzzles,
 	                                  DBHelper.TablePuzzlesCols, null, null, null, null, null );
 	        return cursor;
 	    }
 
 	    public Cursor queryPuzzle( int sid ) {
 	        openToRead();
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        Cursor cursor = db.query( DBHelper.TablePuzzles,
 	                                  cols, cols[1] + "=" + sid , null, null, null, null );
 	        return cursor;
 	    }
 	    
 	    public Cursor queryPuzzle( boolean playing ) {
 	        openToRead();
 	        String[] cols = DBHelper.TablePuzzlesCols;
 	        Cursor cursor = db.query( DBHelper.TablePuzzles,
 	                                  cols, cols[5] + "=" + (playing ? 1 : 0) , null, null, null, null );
 	        return cursor;
 	    }
 }
