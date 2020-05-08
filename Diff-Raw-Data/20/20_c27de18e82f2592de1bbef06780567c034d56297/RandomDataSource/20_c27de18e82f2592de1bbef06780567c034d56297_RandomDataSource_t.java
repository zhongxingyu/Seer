 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.albaniliu.chuangxindemo.data;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore.Images;
 import android.provider.MediaStore.Video;
 import android.util.Log;
 
 import com.albaniliu.chuangxindemo.ImageShow.ShowingNode;
 import com.albaniliu.chuangxindemo.util.Utils;
 import com.albaniliu.chuangxindemo.widget.SlideShow;
 
 public class RandomDataSource implements SlideShow.DataSource {
 	private static final String TAG = "RandomDataSource";
 	private static final String USER_AGENT = "Demo-ImageDownload";
 
     public static final String BASE_CONTENT_STRING_IMAGES = (Images.Media.EXTERNAL_CONTENT_URI).toString() + "/";
     public static final String BASE_CONTENT_STRING_VIDEOS = (Video.Media.EXTERNAL_CONTENT_URI).toString() + "/";
 	private static final int CONNECTION_TIMEOUT = 20000; // ms.
     public static final HttpParams HTTP_PARAMS;
     public static final SchemeRegistry SCHEME_REGISTRY;
     public static final int THUMBNAIL_ID_INDEX = 0;
     public static final int THUMBNAIL_DATE_MODIFIED_INDEX = 1;
     public static final int THUMBNAIL_DATA_INDEX = 2;
     public static final int THUMBNAIL_ORIENTATION_INDEX = 3;
     public static final String[] THUMBNAIL_PROJECTION = new String[] { Images.ImageColumns._ID, Images.ImageColumns.DATE_ADDED,
             Images.ImageColumns.DATA, Images.ImageColumns.ORIENTATION };
 
    public static final String LIANG_BUCKET_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1";
     public static final int LIANG_BUCKET_ID = getBucketId(LIANG_BUCKET_NAME);
     
     private ArrayList<String> mFilesPath = new ArrayList<String>();
    private String mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/liangdemo1";
     private File mTestFolder;
     private boolean mFromDB = false;
     /**
      * Matches code in MediaProvider.computeBucketValues. Should be a common
      * function.
      */
     public static int getBucketId(String path) {
         return (path.toLowerCase(Locale.ENGLISH).hashCode());
     }
     
     public RandomDataSource() {
         super();
         mTestFolder = new File(mPath);
     }
     
     public RandomDataSource(ArrayList<ShowingNode> nodes) {
         super();
         createTestFile(nodes);
     }
     
     private static ImageList sList;
     static {
         // Prepare HTTP parameters.
         HttpParams params = new BasicHttpParams();
         HttpConnectionParams.setStaleCheckingEnabled(params, false);
         HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
         HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
         HttpClientParams.setRedirecting(params, true);
         HttpProtocolParams.setUserAgent(params, USER_AGENT);
         HTTP_PARAMS = params;
 
         // Register HTTP protocol.
         SCHEME_REGISTRY = new SchemeRegistry();
         SCHEME_REGISTRY.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
     }
 
     public Bitmap getBitmapForIndex(Context context, int currentSlideshowCounter) {
         Bitmap retVal = null;
         if (mFromDB) {
             ImageList list = createTestList(context);
             // Once we have the id and the thumbid, we can return a bitmap
             // First we select a random numbers
             if (list.ids == null)
                 return null;
             double random = Math.random();
             random *= list.ids.length;
             int index = (int) random;
             long cacheId = list.thumbids[index];
             final String uri = BASE_CONTENT_STRING_IMAGES + list.ids[index];
             try {
                 retVal = createFromUri(context, uri, 1024, 1024, cacheId, null);
                 if (retVal != null) {
                     retVal = Utils.rotate(retVal, list.orientation[index]);
                 }
             } catch (OutOfMemoryError e) {
                 ;
             } catch (IOException e) {
                 ;
             } catch (URISyntaxException e) {
                 ;
             }
         } else {
             if (mFilesPath.size() == 0) {
                 createTestFile(mTestFolder);
             }
             double random = Math.random();
             random *= mFilesPath.size();
             int index = (int) random;
             
             try {
                 retVal = Utils.createBitmapByFilePath(mFilesPath.get(index), 1024);
             } catch (OutOfMemoryError e) {
                 ;
             }
         }
         return retVal;
     }
 
     private void createTestFile(File folder) {
         if (folder.isDirectory()) {
             File[] files = folder.listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File pathname) {
                     String path = pathname.getName();
                     return path.endsWith(".jpeg") || path.endsWith(".jpg");
                 }
             });
             for (File file : files) {
                 mFilesPath.add(file.getPath());
             }
         }
     }
     
     private void createTestFile(ArrayList<ShowingNode> nodes) {
         if (nodes == null || nodes.size() == 0){
             return ;
         }
         for (ShowingNode node : nodes) {
             mFilesPath.add(node.getPath());
         }
     }
 
     private ImageList createTestList(Context context) {
     	if (sList != null) {
     		return sList;
     	}
     	ImageList list = new ImageList();
     	final Uri uriImages = Images.Media.EXTERNAL_CONTENT_URI;
         final ContentResolver cr = context.getContentResolver();
         StringBuffer whereString = new StringBuffer(Images.ImageColumns.BUCKET_ID + " = ");
         whereString.append(LIANG_BUCKET_ID);
         String whereClause = whereString.toString();
         try {
             final Cursor cursorImages = cr.query(uriImages, THUMBNAIL_PROJECTION, whereClause, null, null);
             if (cursorImages != null && cursorImages.moveToFirst()) {
                 final int size = cursorImages.getCount();
                 final long[] ids = new long[size];
                 final long[] thumbnailIds = new long[size];
                 final long[] timestamp = new long[size];
                 final int[] orientation = new int[size];
                 int ctr = 0;
                 do {
                     if (Thread.interrupted()) {
                         break;
                     }
                     ids[ctr] = cursorImages.getLong(THUMBNAIL_ID_INDEX);
                     timestamp[ctr] = cursorImages.getLong(THUMBNAIL_DATE_MODIFIED_INDEX);
                     thumbnailIds[ctr] = Utils.Crc64Long(cursorImages.getString(THUMBNAIL_DATA_INDEX));
                     orientation[ctr] = cursorImages.getInt(THUMBNAIL_ORIENTATION_INDEX);
                     ++ctr;
                 } while (cursorImages.moveToNext());
                 cursorImages.close();
                 list.ids = ids;
                 list.thumbids = thumbnailIds;
                 list.timestamp = timestamp;
                 list.orientation = orientation;
             }
         } catch (Exception e) {
             // If the database operation failed for any reason
             ;
         }
         if (list != null) {
         	sList = list;
         }
         return list;
     }
     
     public static final Bitmap createFromUri(Context context, String uri, int maxResolutionX, int maxResolutionY, long cacheId,
             ClientConnectionManager connectionManager) throws IOException, URISyntaxException, OutOfMemoryError {
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inScaled = false;
         options.inPreferredConfig = Bitmap.Config.RGB_565;
         options.inDither = true;
         long crc64 = 0;
         Bitmap bitmap = null;
         if (uri.startsWith(ContentResolver.SCHEME_CONTENT)) {
             // We need the filepath for the given content uri
             crc64 = cacheId;
         } else {
             crc64 = Utils.Crc64Long(uri);
         }
         bitmap = createFromCache(crc64, maxResolutionX);
         if (bitmap != null) {
             return bitmap;
         }
         final boolean local = uri.startsWith(ContentResolver.SCHEME_CONTENT) || uri.startsWith("file://");
 
         // Get the input stream for computing the sample size.
         BufferedInputStream bufferedInput = null;
         if (uri.startsWith(ContentResolver.SCHEME_CONTENT) ||
                 uri.startsWith(ContentResolver.SCHEME_FILE)) {
             // Get the stream from a local file.
             bufferedInput = new BufferedInputStream(context.getContentResolver()
                     .openInputStream(Uri.parse(uri)), 16384);
         } else {
             // Get the stream from a remote URL.
             bufferedInput = createInputStreamFromRemoteUrl(uri, connectionManager);
         }
 
         // Compute the sample size, i.e., not decoding real pixels.
         if (bufferedInput != null) {
             options.inSampleSize = computeSampleSize(bufferedInput, maxResolutionX, maxResolutionY);
         } else {
             return null;
         }
 
         // Get the input stream again for decoding it to a bitmap.
         bufferedInput = null;
         if (uri.startsWith(ContentResolver.SCHEME_CONTENT) ||
                 uri.startsWith(ContentResolver.SCHEME_FILE)) {
             // Get the stream from a local file.
             bufferedInput = new BufferedInputStream(context.getContentResolver()
                     .openInputStream(Uri.parse(uri)), 16384);
         } else {
             // Get the stream from a remote URL.
             bufferedInput = createInputStreamFromRemoteUrl(uri, connectionManager);
         }
 
         // Decode bufferedInput to a bitmap.
         if (bufferedInput != null) {
             options.inDither = false;
             options.inJustDecodeBounds = false;
             Thread timeoutThread = new Thread("BitmapTimeoutThread") {
                 public void run() {
                     try {
                         Thread.sleep(6000);
                         options.requestCancelDecode();
                     } catch (InterruptedException e) {
                     }
                 }
             };
             timeoutThread.start();
 
             bitmap = BitmapFactory.decodeStream(bufferedInput, null, options);
         }
 
         if ((options.inSampleSize > 1 || !local) && bitmap != null) {
             writeToCache(crc64, bitmap, maxResolutionX / options.inSampleSize);
         }
         return bitmap;
     }
 
     private static final BufferedInputStream createInputStreamFromRemoteUrl(
             String uri, ClientConnectionManager connectionManager) {
         InputStream contentInput = null;
         if (connectionManager == null) {
             try {
                 URL url = new URI(uri).toURL();
                 URLConnection conn = url.openConnection();
                 conn.connect();
                 contentInput = conn.getInputStream();
             } catch (Exception e) {
                 Log.w(TAG, "Request failed: " + uri);
                 e.printStackTrace();
                 return null;
             }
         } else {
             // We create a cancelable http request from the client
             final DefaultHttpClient mHttpClient = new DefaultHttpClient(connectionManager, HTTP_PARAMS);
             HttpUriRequest request = new HttpGet(uri);
             // Execute the HTTP request.
             HttpResponse httpResponse = null;
             try {
                 httpResponse = mHttpClient.execute(request);
                 HttpEntity entity = httpResponse.getEntity();
                 if (entity != null) {
                     // Wrap the entity input stream in a GZIP decoder if
                     // necessary.
                     contentInput = entity.getContent();
                 }
             } catch (Exception e) {
                 Log.w(TAG, "Request failed: " + request.getURI());
                 return null;
             }
         }
         if (contentInput != null) {
             return new BufferedInputStream(contentInput, 4096);
         } else {
             return null;
         }
     }
     
     
     public static final String createFilePathFromCrc64(long crc64, int maxResolution) {
         return "/sdcard/demo" + crc64 + "_" + maxResolution + ".cache";
     }
 
     public static boolean isCached(long crc64, int maxResolution) {
         String file = null;
         if (crc64 != 0) {
             file = createFilePathFromCrc64(crc64, maxResolution);
             try {
                 new FileInputStream(file);
                 return true;
             } catch (FileNotFoundException e) {
                 return false;
             }
         }
         return false;
     }
 
     public static Bitmap createFromCache(long crc64, int maxResolution) {
         try {
             String file = null;
             Bitmap bitmap = null;
             final BitmapFactory.Options options = new BitmapFactory.Options();
             options.inScaled = false;
             options.inPreferredConfig = Bitmap.Config.RGB_565;
             options.inDither = true;
             if (crc64 != 0) {
                 file = createFilePathFromCrc64(crc64, maxResolution);
                 bitmap = BitmapFactory.decodeFile(file, options);
             }
             return bitmap;
         } catch (Exception e) {
             return null;
         }
     }
     
     
     private static int computeSampleSize(InputStream stream, int maxResolutionX,
             int maxResolutionY) {
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inJustDecodeBounds = true;
             BitmapFactory.decodeStream(stream, null, options);
             int maxNumOfPixels = maxResolutionX * maxResolutionY;
             int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
             return Utils.computeSampleSize(options, minSideLength, maxNumOfPixels);
         }
     
     public static void writeToCache(long crc64, Bitmap bitmap, int maxResolution) {
         String file = createFilePathFromCrc64(crc64, maxResolution);
         if (bitmap != null && file != null && crc64 != 0) {
             try {
                 File fileC = new File(file);
                 fileC.createNewFile();
                 final FileOutputStream fos = new FileOutputStream(fileC);
                 final BufferedOutputStream bos = new BufferedOutputStream(fos, 16384);
                 bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                 bos.flush();
                 bos.close();
                 fos.close();
             } catch (Exception e) {
 
             }
         }
     }
 }
