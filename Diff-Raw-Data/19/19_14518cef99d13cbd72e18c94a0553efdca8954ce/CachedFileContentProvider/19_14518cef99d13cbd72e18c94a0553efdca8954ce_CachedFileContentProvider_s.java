 package com.hackforchange.providers;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.ParcelFileDescriptor;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 // Source: http://stephendnicholas.com/archives/974
 public class CachedFileContentProvider extends ContentProvider {
 
  private static final String CLASS_NAME = "CachedFileProvider";

   // The authority is the symbolic name for the provider class
  public static final String AUTHORITY = "com.hackforchange.providers";
 
   // UriMatcher used to match against incoming requests
   private UriMatcher uriMatcher;
 
   @Override
   public boolean onCreate() {
     uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
     // Add a URI to the matcher which will match against the form
     // 'content://com.stephendnicholas.gmailattach.provider/*'
     // and return 1 in the case that the incoming Uri matches this pattern
     uriMatcher.addURI(AUTHORITY, "*", 1);
 
     return true;
   }
 
   @Override
   public ParcelFileDescriptor openFile(Uri uri, String mode)
     throws FileNotFoundException {
 
     // Check incoming Uri against the matcher
     switch (uriMatcher.match(uri)) {
 
       // If it returns 1 - then it matches the Uri defined in onCreate
       case 1:
 
         // The desired file name is specified by the last segment of the
         // path
         // E.g.
         // 'content://com.hackforchange.providers/Data_Report.csv'
         // Take this and build the path to the file
         String fileLocation = getContext().getCacheDir() + File.separator
           + uri.getLastPathSegment();
 
         // Create & return a ParcelFileDescriptor pointing to the file
         // Note: I don't care what mode they ask for - they're only getting
         // read only
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(
          fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
         return pfd;
 
       // Otherwise unrecognised Uri
       default:
        throw new FileNotFoundException("Unsupported uri: "
          + uri.toString());
     }
   }
 
   // Not supported / used / required for this example
   @Override
   public int update(Uri uri, ContentValues contentvalues, String s,
                     String[] as) {
     return 0;
   }
 
   @Override
   public int delete(Uri uri, String s, String[] as) {
     return 0;
   }
 
   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
     return null;
   }
 
   @Override
   public String getType(Uri uri) {
     return null;
   }
 
   @Override
   public Cursor query(Uri uri, String[] projection, String s, String[] as1,
                       String s1) {
     return null;
   }
 }
