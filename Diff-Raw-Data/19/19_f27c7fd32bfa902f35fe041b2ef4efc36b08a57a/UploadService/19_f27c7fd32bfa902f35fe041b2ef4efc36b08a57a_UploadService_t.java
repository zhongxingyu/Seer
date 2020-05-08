 package com.appspot.manup.signup;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.FormBodyPart;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.appspot.manup.signup.data.DataManager;
 import com.appspot.manup.signup.data.DataManager.Member;
 
 public final class UploadService extends IntentService
 {
     private static final String TAG = UploadService.class.getSimpleName();
 
     private static final String IMAGE_MIME = "image/png";
 
     private final ConfiguredHttpClient mClient = new ConfiguredHttpClient();
 
     public UploadService()
     {
         super(TAG);
     } // constructor()
 
     @Override
     protected void onHandleIntent(final Intent intent)
     {
         Cursor c = null;
         try
         {
             c = DataManager.getDataManager(this).getMembersWithSignatures();
             if (c == null)
             {
                 Log.w(TAG, "Failed to get captured signatures. Aborting.");
                 return;
             } // if
 
             final int idColumn = c.getColumnIndexOrThrow(Member._ID);
             final int personIdColumn = c.getColumnIndexOrThrow(Member.PERSON_ID);
 
             while (c.moveToNext())
             {
                 try
                 {
                     uploadSignature(c.getLong(idColumn), c.getString(personIdColumn));
                 } // try
                 catch (final FileNotFoundException e)
                 {
                     Log.w(TAG, "Could not find signature file. Skipping", e);
                } // catch
                 catch (final IOException e)
                 {
                     Log.w(TAG, "Failed to upload signature. Aborting.", e);
                     return;
                 } // catch
             } // while
 
         } // try
         finally
         {
             if (c != null)
             {
                 c.close();
             } // if
         } // finally
     } // uploadSignatures
 
     private void uploadSignature(final long id, final String personId)
             throws FileNotFoundException, IOException
     {
         final Preferences prefs = new Preferences(this);
         if (!prefs.hasHost())
         {
             throw new IOException("Host not set");
         }
         if (!prefs.hasPort())
         {
             throw new IOException("Port not set");
         }
         final URI uri;
         try
         {
             uri = new URI(
                     "http",
                     null /* userInfo */,
                     prefs.getHost(),
                     prefs.getPort(),
                     "/members/" + personId,
                     null /* query */,
                     null /* fragment */);
         } // try
         catch (final URISyntaxException e)
         {
             throw new AssertionError(e);
         } // catch
 
         final DataManager db = DataManager.getDataManager(this);
         final File imageFile = db.getSignatureFile(id);
         if (imageFile == null)
         {
             throw new IOException("Failed to get image file for " + id);
         } // if
 
         FileInputStream pngStream = null;
         HttpResponse response = null;
         try
         {
             pngStream = new FileInputStream(imageFile);
             final MultipartEntity entity = new MultipartEntity();
             entity.addPart(new FormBodyPart("signature", new FileBody(imageFile, IMAGE_MIME)));
             final HttpPost post = new HttpPost(uri);
             post.setEntity(entity);
             response = mClient.execute(post);
             final int statusCode = response.getStatusLine().getStatusCode();
             if (statusCode != HttpStatus.SC_OK)
             {
                 throw new IOException("Server returned " + statusCode);
             } // if
         } // try
         finally
         {
             if (pngStream != null)
             {
                 try
                 {
                     pngStream.close();
                 } // try
                 finally
                 {
                     if (response != null)
                     {
                         final HttpEntity entity = response.getEntity();
                         if (entity != null)
                         {
                             entity.getContent().close();
                         } // if
                     } // if
                 } // finally
             } // if
         } // finally
         if (!db.setSignatureUploaded(id))
         {
             throw new IOException("Failed to update signature state for " + id);
         } // if
     } // uploadSignature
 
     @Override
     public IBinder onBind(final Intent intent)
     {
         return null;
     } // onBind
 
 } // SignatureUploadService
