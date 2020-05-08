 package com.appspot.manup.signature;
 
 import java.io.File;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 public final class SignatureDatabase
 {
     private static final String TAG = SignatureDatabase.class.getSimpleName();
 
     private static final class Signature implements BaseColumns
     {
         private static final String TABLE_NAME = "signature";
 
         public static final String STUDENT_ID = "student_id";
         public static final String SIGNATURE_STATE = "signature_state";
 
         public static final String SIGNATURE_NONE = "n";
         public static final String SIGNATURE_CAPTURED = "c";
         public static final String SIGNATURE_UPLOADED = "u";
 
         private Signature()
         {
             super();
             throw new AssertionError();
         } // Signature
 
     } // Signature
 
     private static final class OpenHelper extends SQLiteOpenHelper
     {
         private static final String DATABASE_NAME = "manup_signatures.db";
         private static final int DATABASE_VERSION = 2;
 
         public OpenHelper(final Context context)
         {
             super(context, DATABASE_NAME, null /* factory */, DATABASE_VERSION);
         } // OpenHelper
 
         @Override
         public void onCreate(final SQLiteDatabase db)
         {
             //@formatter:off
 
             final String createTableSql =
 
             "CREATE TABLE " + Signature.TABLE_NAME + "("                                +
                 Signature._ID             + " INTEGER PRIMARY KEY AUTOINCREMENT,"       +
                 Signature.STUDENT_ID      + " TEXT UNIQUE,"                             +
                 Signature.SIGNATURE_STATE + " TEXT DEFAULT " + Signature.SIGNATURE_NONE +
             ")";
 
             //@formatter:on
 
             Log.v(TAG, createTableSql);
 
             db.execSQL(createTableSql);
         } // onCreate
 
         @Override
         public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
         {
             Log.w("DB Upgrade", "Upgrading database, this will drop tables and recreate.");
             db.execSQL("DROP TABLE IF EXISTS " + Signature.TABLE_NAME);
             onCreate(db);
         } // onUpgrade
 
     } // OpenHelper
 
     private static final Object sLock = new Object();
     private static SignatureDatabase sDataHelper = null;
 
     public static SignatureDatabase getInstance(final Context context)
     {
         synchronized (sLock)
         {
             if (sDataHelper == null)
             {
                 sDataHelper = new SignatureDatabase(context);
             } // if
         } // synchronized
         return sDataHelper;
     } // getInstance
 
     private final Object mLock = new Object();
     private final Context mContext;
     private volatile SQLiteDatabase mDb = null;
     private volatile OpenHelper mOpenHelper = null;
 
     private SignatureDatabase(final Context context)
     {
         super();
         mContext = context.getApplicationContext();
         mOpenHelper = new OpenHelper(mContext);
 
     } // DataHelper
 
     private SQLiteDatabase getDatabase()
     {
         synchronized (mLock)
         {
             if (mDb == null)
             {
                 mDb = mOpenHelper.getWritableDatabase();
             } // if
         } // synchronized
         return mDb;
     } // getDatabase
 
     public long addSignature(final String studentId)
     {
         final SQLiteDatabase db = getDatabase();
         final ContentValues cv = new ContentValues(1);
         cv.put(Signature.STUDENT_ID, studentId);
         return db.insert(Signature.TABLE_NAME, Signature.STUDENT_ID, cv);
     } // insert
 
     public File getImageFile(final long id)
     {
         final File externalDir = mContext.getExternalFilesDir(null /* type */);
         if (externalDir == null)
         {
             return null;
         } // if
         return new File(externalDir, Long.toString(id) + ".png");
     } // getImageFile
 
     public String getStudentId(final long id)
     {
         Cursor c = null;
         try
         {
             c = getDatabase().query(
                     Signature.TABLE_NAME,
                     new String[] { Signature.STUDENT_ID },
                     Signature._ID + "=?",
                     new String[] { Long.toString(id) },
                     null /* groupBy */,
                     null /* having */,
                     null /* orderBy */);
             if (c != null && c.moveToFirst())
             {
                 return c.getString(0);
             } // if
            Log.e(TAG, "Failed to get student ID for " + id);
             return null;
         } // try
         finally
         {
             if (c != null)
             {
                 c.close();
             } // if
         } // finally
     } // getStudentId
 
     private boolean signatureNone(final long id)
     {
         return updateSignatureState(id, Signature.SIGNATURE_NONE);
     } // signatureNone
 
     public boolean signatureCaptured(final long id)
     {
         return updateSignatureState(id, Signature.SIGNATURE_CAPTURED);
     } // signatureCaptured
 
     public boolean signatureUploaded(final long id)
     {
         return updateSignatureState(id, Signature.SIGNATURE_UPLOADED);
     } // signatureUploaded
 
     private boolean updateSignatureState(final long id, final String newState)
     {
         final SQLiteDatabase db = getDatabase();
         final ContentValues cv = new ContentValues(1);
         cv.put(Signature.SIGNATURE_STATE, newState);
         final int rowsUpdated = db.update(Signature.TABLE_NAME, cv, Signature._ID + "=?",
                 new String[] { Long.toString(id) });
         return rowsUpdated == 1;
     } // update
 
     public boolean deleteSignature(final long id)
     {
         final SQLiteDatabase db = getDatabase();
         final File image = getImageFile(id);
         if (image == null)
         {
             return false;
         } // if
 
         if (image.delete())
         {
             final int signaturesDeleted = db.delete(Signature.TABLE_NAME, Signature._ID + "=?",
                     new String[] { Long.toString(id) });
             return signaturesDeleted == 1;
         } // if
 
         Log.w(TAG, "Failed to delete image file for " + id);
 
         if (!signatureNone(id))
         {
             Log.e(TAG, "Failed to set signature state to NONE for " + id);
         } // if
 
         return false;
     } // deleteSignature
 
 } // SignatureDatabase
