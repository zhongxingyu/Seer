 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.network;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 
 import mc.lib.interfaces.OnCompleteListener;
 import mc.lib.interfaces.OnProgressListener;
 import mc.lib.log.Log;
 import mc.lib.stream.StreamHelper;
 import android.os.AsyncTask;
 
 public class AsyncFileDownloader extends AsyncTask<Void, Integer, Boolean>
 {
    private static final String LOGTAG = AsyncFileDownloader.class.getSimpleName();
     private static final int BUFFER_SIZE = 4096;
 
     private static int id;
     private static Map<Integer, AsyncFileDownloader> map = new HashMap<Integer, AsyncFileDownloader>();
 
     public static synchronized int downloadFile(String url, String outputFilePath, OnCompleteListener clistener, OnProgressListener plistener)
     {
         try
         {
             return downloadFile(new URL(url), new File(outputFilePath), clistener, plistener);
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url", e);
         }
         return -1;
     }
 
     public static synchronized int downloadFile(String url, File output, OnCompleteListener clistener, OnProgressListener plistener)
     {
         try
         {
             return downloadFile(new URL(url), output, clistener, plistener);
         }
         catch(MalformedURLException e)
         {
             Log.e(LOGTAG, "Wrong url", e);
         }
         return -1;
     }
 
     public static synchronized int downloadFile(URL url, String outputFilePath, OnCompleteListener clistener, OnProgressListener plistener)
     {
         return downloadFile(url, new File(outputFilePath), clistener, plistener);
     }
 
     public static synchronized int downloadFile(URL url, File output, OnCompleteListener clistener, OnProgressListener plistener)
     {
         if(url != null && output != null)
         {
             AsyncFileDownloader afd = new AsyncFileDownloader(url, output, clistener, plistener);
             id++;
             map.put(id, afd);
             afd.execute((Void[])null);
             return id;
         }
         else
         {
             Log.e(LOGTAG, "URL or Output file is NULL");
         }
         return -1;
     }
 
     public static synchronized void cancelDownloadTask(int id, boolean callCompleteListener)
     {
         AsyncFileDownloader afd = map.get(id);
         if(afd == null)
         {
             Log.w(LOGTAG, "Cannot find download task with id id");
             return;
         }
         afd.cancel = true;
         afd.callCompleteListener = callCompleteListener;
     }
 
     private URL url;
     private File file;
     private OnCompleteListener clistener;
     private OnProgressListener plistener;
     private boolean callCompleteListener;
     private boolean cancel;
 
     private AsyncFileDownloader(URL url, File file, OnCompleteListener clistener, OnProgressListener plistener)
     {
         super();
         this.url = url;
         this.file = file;
         this.clistener = clistener;
         this.plistener = plistener;
         this.callCompleteListener = true;
     }
 
     @Override
     protected Boolean doInBackground(Void... v)
     {
         try
         {
             URLConnection c = url.openConnection();
             int lenght = c.getContentLength();
             if(lenght > 0)
             {
                 Log.i(LOGTAG, "Lenght of file to download:" + lenght);
             }
             int read = 0, readTotal = 0;
             InputStream is = url.openStream();
             int bufferSize = BUFFER_SIZE;
             byte buffer[] = new byte[bufferSize];
             BufferedInputStream bis = new BufferedInputStream(is, bufferSize);
             OutputStream os = new FileOutputStream(file);
             if((read = bis.read(buffer)) < 1)
             {
                 wait(c.getReadTimeout());
                 read = bis.read(buffer);
             }
             while(read > 0)
             {
                 os.write(buffer, 0, read);
                 readTotal += read;
                 publishProgress(readTotal, lenght);
                 if((read = bis.read(buffer)) < 1)
                 {
                     wait(c.getReadTimeout());
                     read = bis.read(buffer);
                 }
                 if(cancel)
                 {
                     break;
                 }
             }
             StreamHelper.close(os);
             StreamHelper.close(bis);
             StreamHelper.close(is);
             if(cancel)
             {
                 Log.i(LOGTAG, "Download task canceled. Url:" + url);
                 file.delete();
                 return false;
             }
         }
         catch(Exception e)
         {
             Log.i(LOGTAG, "Error on loading file:" + url, e);
             return false;
         }
         return true;
     }
 
     @Override
     protected void onProgressUpdate(Integer... p)
     {
         if(plistener != null && !cancel)
         {
             plistener.notifyProgress(p[0], p[1]);
         }
     }
 
     @Override
     protected void onPostExecute(Boolean result)
     {
         if(result)
         {
             Log.i(LOGTAG, "Download complere sucessfully");
         }
         if(clistener != null && callCompleteListener)
         {
             clistener.complete(getClass());
         }
     }
 
     private static void wait(int waitTime)
     {
         try
         {
             Thread.sleep(waitTime);
         }
         catch(InterruptedException e)
         {
             Log.w(LOGTAG, "Stream reading timeout interupted");
         }
     }
 }
