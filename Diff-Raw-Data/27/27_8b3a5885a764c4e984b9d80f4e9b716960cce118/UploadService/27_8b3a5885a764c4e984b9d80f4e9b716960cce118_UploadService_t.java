 package xtuaok.sharegyazo;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.preference.PreferenceManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap.CompressFormat;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Handler;
 
 public class UploadService extends IntentService {
     private static final String LOG_TAG = "GyazoUploadService";
     private static final String DEFAULT_UPLOADER = "http://gyazo.com/upload.cgi";
 
     private static final int NOTIFY_UPLOADING = 0x0;
     private static final int NOTIFY_DONE      = 0x1;
     
     private Handler mHandler;
     private Context mContext;
     private String mGyazoCGI;
     private String mGyazoID;
     private boolean mCopyURL;
     private boolean mOpenURL;
     private boolean mShareURL;
     private int mNotifyAction;
     private boolean mNotifyClose;
     private boolean mNotification;
    private Bitmap mBitmap = null;
     private NotificationManager mNotificationManager;
     private String mErrorMessage = "";
 
     private static final int MAX_IMAGE_PIXEL = 1920; // HD
 
     public UploadService(String name) {
         super(name);
     }
 
     public UploadService() {
         super("UploadService");
         mHandler = new Handler();
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         String result = "";
 
         Uri uri = intent.getData();
 
         mContext = getApplicationContext();
         mGyazoCGI = prefs.getString(GyazoPreference.PREF_GYAZO_CGI, DEFAULT_UPLOADER);
         mGyazoID  = prefs.getString(GyazoPreference.PREF_GYAZO_ID, "");
         mCopyURL  = prefs.getBoolean(GyazoPreference.PREF_COPY_URL, true);
         mOpenURL  = prefs.getBoolean(GyazoPreference.PREF_OPEN_BROWSER, false);
         mShareURL = prefs.getBoolean(GyazoPreference.PREF_SHARE_TEXT, true);
         mNotifyAction = Integer.parseInt(prefs.getString(GyazoPreference.PREF_NOTIFY_ACTION, "0"));
         mNotifyClose = prefs.getBoolean(GyazoPreference.PREF_AUTO_CLOSE_NOTIFICATION, false);
         mNotification = prefs.getBoolean(GyazoPreference.PREF_SHOW_NOTIFICATION, true);
 
         mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 
         // Notify UPLOADING
         Intent notifIntent = new Intent();
         notifIntent.addCategory(Intent.CATEGORY_DEFAULT);
         notifIntent.setAction(Intent.ACTION_VIEW);
         notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         notifIntent.setData(uri);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
         builder.setContentTitle(getString(R.string.dialog_message_uploading))
             .setSmallIcon(android.R.drawable.ic_menu_upload)
             .setTicker(getString(R.string.dialog_message_uploading))
             .setContentIntent(pendingIntent)
             .setOngoing(true)
             .setAutoCancel(mNotifyClose);
 
         if (mNotification) {
             mNotificationManager.notify(NOTIFY_UPLOADING, builder.build());
         }
         mHandler.post(new Runnable() {
             public void run() {
                 Toast.makeText(mContext, getString(R.string.dialog_message_uploading), Toast.LENGTH_LONG).show();                
             }
         });
 
         result = doUpload(mGyazoCGI, uri);
         done(result, uri);
     }
     
     @SuppressWarnings("deprecation")
     private void done(String result, Uri uri) {
         Intent intent = new Intent();
         PendingIntent intentView = null;
         PendingIntent intentSend = null;
         PendingIntent intentCopy = null;
 
         Log.i(LOG_TAG, "Result: " + result);
         if (mErrorMessage != "") {
             mHandler.post(new Runnable() {            
                 public void run() {
                     Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_LONG).show();
                 }
             });
         }
 
         if (result == null || !result.startsWith("http")) {
             mNotificationManager.cancel(NOTIFY_UPLOADING);
 
             Intent notifIntent = new Intent();
             notifIntent.addCategory(Intent.CATEGORY_DEFAULT);
             notifIntent.setAction(Intent.ACTION_VIEW);
             notifIntent.setData(uri);
 
             PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
             NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
             builder.setContentTitle(getString(R.string.failed_to_upload))
                 .setSmallIcon(android.R.drawable.ic_menu_report_image)
                 .setTicker(getString(R.string.failed_to_upload))
                 .setContentIntent(pendingIntent)
                 .setAutoCancel(true)
                 .setAutoCancel(mNotifyClose);
 
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                 builder.setLargeIcon(mBitmap);
             }
 
             if (mNotification) {
                 mNotificationManager.notify(NOTIFY_UPLOADING, builder.build());
             }
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
             mHandler.post(new Runnable() {            
                 public void run() {
                     Toast.makeText(mContext, getString(R.string.failed_to_upload), Toast.LENGTH_LONG).show();
                 }
             });
             return;
         }
 
         if (mCopyURL) {
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                 ((android.text.ClipboardManager)(getSystemService(CLIPBOARD_SERVICE))).setText(result);
             } else {
                 ClipData clip = ClipData.newPlainText("url text", result);
                 ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                 cm.setPrimaryClip(clip);
             }
             mHandler.post(new Runnable() {
                 public void run() {
                     Toast.makeText(getApplicationContext(),
                             getString(R.string.toast_copy_url), Toast.LENGTH_LONG).show();
                 }
             });
         }
 
         if (mOpenURL) {
             intent.setAction(Intent.ACTION_VIEW);
             intent.addCategory(Intent.CATEGORY_BROWSABLE);
             intent.setData(Uri.parse(result));
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             mContext.startActivity(intent);
         } else if (mShareURL) {
             intent.setAction(Intent.ACTION_SEND);
             intent.setType("text/plain");
             intent.addCategory(Intent.CATEGORY_DEFAULT);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             intent.putExtra(Intent.EXTRA_TEXT, result);
             mContext.startActivity(intent);
         }
 
         if (!mNotification) {
             return;
         }
         // Notifications
         Intent notifyIntent;
 
         // VIEW
         notifyIntent = new Intent();
         notifyIntent.setAction(Intent.ACTION_VIEW);
         notifyIntent.addCategory(Intent.CATEGORY_BROWSABLE);
         notifyIntent.setData(Uri.parse(result));
         notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intentView = PendingIntent.getActivity(mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         // SEND
         notifyIntent = new Intent();
         notifyIntent.setAction(Intent.ACTION_SEND);
         notifyIntent.setType("text/plain");
         notifyIntent.addCategory(Intent.CATEGORY_DEFAULT);
         notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         notifyIntent.putExtra(Intent.EXTRA_TEXT, result);
         intentSend = PendingIntent.getActivity(mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         // COPY
         notifyIntent = new Intent(mContext, ClipText.class);
         notifyIntent.setAction(Intent.ACTION_MAIN);
         notifyIntent.putExtra(Intent.EXTRA_TEXT, result);
         notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intentCopy = PendingIntent.getActivity(mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         // build
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
             NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
             builder.setContentTitle(getString(R.string.message_uploaded))
                 .setSmallIcon(android.R.drawable.ic_menu_gallery)
                 .addAction(android.R.drawable.ic_menu_view, getString(R.string.open), intentView)
                 .addAction(android.R.drawable.ic_menu_share, getString(R.string.share), intentSend)
                 .addAction(android.R.drawable.ic_menu_set_as, getString(R.string.copy), intentCopy)
                 .setTicker(result)
                 .setContentText(result)
                 .setAutoCancel(mNotifyClose);
 
             // Tap actions
             if (mNotifyAction == 1) {
                 builder.setContentIntent(intentView);
             } else if (mNotifyAction == 2) {
                 builder.setContentIntent(intentSend);
             } else if (mNotifyAction == 3) {
                 builder.setContentIntent(intentCopy);
             }
 
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                 builder.setLargeIcon(mBitmap);
             }
             mNotificationManager.cancel(NOTIFY_UPLOADING);
             mNotificationManager.notify(result, NOTIFY_DONE, builder.build());
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
         } else { // for JB or higher
             Notification.Builder builder = new Notification.Builder(this);
             builder.setContentTitle(getString(R.string.message_uploaded))
                 .setSmallIcon(android.R.drawable.ic_menu_gallery)
                 .addAction(android.R.drawable.ic_menu_view, getString(R.string.open), intentView)
                 .addAction(android.R.drawable.ic_menu_share, getString(R.string.share), intentSend)
                 .addAction(android.R.drawable.ic_menu_set_as, getString(R.string.copy), intentCopy)
                 .setTicker(result)
                 .setContentText(result)
                 .setAutoCancel(mNotifyClose);
             // Tap actions
             if (mNotifyAction == 1) {
                 builder.setContentIntent(intentView);
             } else if (mNotifyAction == 2) {
                 builder.setContentIntent(intentSend);
             } else if (mNotifyAction == 3) {
                 builder.setContentIntent(intentCopy);
             }
             builder.setStyle(new Notification.BigPictureStyle()
             .bigPicture(mBitmap)
             .bigLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
             .setBigContentTitle(getString(R.string.message_uploaded))
             .setSummaryText(result));
             mNotificationManager.cancel(NOTIFY_UPLOADING);
             mNotificationManager.notify(result, NOTIFY_DONE, builder.build());
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
         }
     }
 
     private String doUpload(String cgi, Uri uri) {
         String result = "";
         InputStream in;
         try {
             in = getContentResolver().openInputStream(uri);
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inJustDecodeBounds = true;
             Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
             in.close();
             in = getContentResolver().openInputStream(uri);
             Log.d(LOG_TAG, "ImageType: " + options.outMimeType);
 
             if (options.outMimeType.endsWith("png")) {
                 byte[] img_byte = readBytes(in);
                 in.close();
                 // for notification image
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                     int scale = 1;
                     int h = options.outHeight;
                     int w = options.outWidth;
                     while (true) {
                        if (h / scale > MAX_IMAGE_PIXEL ||
                            w / scale > MAX_IMAGE_PIXEL) {
                             scale = scale * 2;
                         } else {
                             break;
                         }
                     }
                     Log.d(LOG_TAG, "Scale: " + scale);
                     options.inSampleSize = scale;
                     options.inJustDecodeBounds = false;
                     mBitmap = BitmapFactory.decodeByteArray(img_byte, 0, img_byte.length, options);
                 }
                 result = postGyazo(cgi, img_byte);
             } else {
                 // TODO: more better solution for OutOfMemory.
                 ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                 int scale = 1;
                 int h = options.outHeight;
                 int w = options.outWidth;
                 while (true) {
                     if (h / scale > MAX_IMAGE_PIXEL || w / scale > MAX_IMAGE_PIXEL) {
                         scale = scale * 2;
                     } else {
                         break;
                     }
                 }
                 Log.d(LOG_TAG, "Scale: " + scale);
                 options.inSampleSize = scale;
                 options.inJustDecodeBounds = false;
                 bitmap = BitmapFactory.decodeStream(in, null, options);
                 in.close();
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mBitmap = Bitmap.createBitmap(bitmap);
                 }
                 bitmap.compress(CompressFormat.PNG, 100, byteBuffer);
                 result = postGyazo(cgi, byteBuffer.toByteArray());
             }
         } catch (OutOfMemoryError e0) {
             e0.printStackTrace();
             mErrorMessage = "Out of Memory";
         } catch (java.io.FileNotFoundException e1) {
             e1.printStackTrace();
             mErrorMessage = "File not found or not local";
         } catch (IOException e2) {
             e2.printStackTrace();
             mErrorMessage = "IOError";
         }
         return result;
     }
 
     private String postGyazo(String cgi, byte[] imgData) throws ClientProtocolException, IOException {
         List<NameValuePair> nValuePairs = new ArrayList<NameValuePair>();
         nValuePairs.add(new BasicNameValuePair("id", mGyazoID));  
         String ret;
         HttpMultipartPostRequest request = new HttpMultipartPostRequest(cgi, nValuePairs, imgData);
         Log.d(LOG_TAG, "send POST request");
         ret = request.send();
         return ret;
     }
 
     // copy pasted from Stackoverflow.com
     private byte[] readBytes(InputStream inputStream) throws IOException {
         // this dynamically extends to take the bytes you read
         ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
 
         // this is storage overwritten on each iteration with bytes
         int bufferSize = 1024;
         byte[] buffer = new byte[bufferSize];
 
         // we need to know how may bytes were read to write them to the byteBuffer
         int len = 0;
         while ((len = inputStream.read(buffer)) != -1) {
             byteBuffer.write(buffer, 0, len);
         }
 
         // and then we can return your byte array.
         return byteBuffer.toByteArray();
     }
 
 }
