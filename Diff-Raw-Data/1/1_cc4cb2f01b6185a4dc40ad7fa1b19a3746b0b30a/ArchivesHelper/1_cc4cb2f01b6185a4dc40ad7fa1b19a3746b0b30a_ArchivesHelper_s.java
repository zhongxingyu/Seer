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
 package mc.lib.archives;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import mc.lib.interfaces.OnCompleteListener;
 import mc.lib.interfaces.OnProgressListener;
 import mc.lib.stream.StreamHelper;
 import android.os.AsyncTask;
 import android.util.Log;
 
 // TODO: doesn't works with subdirectories in zip file
 public class ArchivesHelper extends AsyncTask<Object, Integer, Object>
 {
     private static final String LOGTAG = ArchivesHelper.class.getSimpleName();
 
     public static void unpackZip(String inputFile, String outputDir)
     {
         unpackZip(inputFile, outputDir, null, null);
     }
 
     public static void unpackZip(String inputFile, String outputDir, OnCompleteListener<File> listener)
     {
         unpackZip(inputFile, outputDir, listener, null);
     }
 
     public static void unpackZip(String inputFile, String outputDir, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         File inputFileObj = new File(inputFile);
         File outputDirObj = new File(outputDir);
         unpackZip(inputFileObj, outputDirObj, clistener, plistener);
     }
 
     public static void unpackZip(File inputFile, File outputDir)
     {
         unpackZip(inputFile, outputDir, null);
     }
 
     public static void unpackZip(File inputFile, File outputDir, OnCompleteListener<File> listener)
     {
         unpackZip(inputFile, outputDir, listener, null);
     }
 
     public static void unpackZip(File inputFile, File outputDir, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         new ArchivesHelper(outputDir, inputFile, clistener, plistener).execute(plistener);
     }
 
     private File outputDir;
     private File inputFile;
     private OnProgressListener plistener;
     private OnCompleteListener<File> clistener;
 
     ArchivesHelper(File outputDir, File inputFile, OnCompleteListener<File> clistener, OnProgressListener plistener)
     {
         super();
         this.outputDir = outputDir;
         this.inputFile = inputFile;
         this.plistener = plistener;
         this.clistener = clistener;
     }
 
     @Override
     protected Object doInBackground(Object... params)
     {
         checkDir(outputDir);
 
         ZipEntry ze = null;
         InputStream is = null;
         ZipInputStream zis = null;
         FileOutputStream fout = null;
         ByteArrayOutputStream baos = null;
         try
         {
             is = new FileInputStream(inputFile);
             zis = new ZipInputStream(new BufferedInputStream(is));
             int fileLength = (int)inputFile.length();
             int readLast, readTotal = 0;
 
             ze = zis.getNextEntry();
             while(ze != null)
             {
                 baos = new ByteArrayOutputStream();
                 byte[] buffer = new byte[StreamHelper.BUFFER_SIZE];
 
                 String filename = outputDir + "/" + ze.getName();
                 fout = new FileOutputStream(filename);
                 readLast = zis.read(buffer);
                 while(readLast != -1)
                 {
                     baos.write(buffer, 0, readLast);
                     fout.write(baos.toByteArray());
                     baos.reset();
                     readTotal += readLast;
                     publishProgress(readTotal, fileLength);
                     readLast = zis.read(buffer);
                 }
 
                 StreamHelper.close(fout);
                 zis.closeEntry();
                 ze = zis.getNextEntry();
             }
             publishProgress(Integer.MAX_VALUE, Integer.MAX_VALUE);
         }
         catch(IOException e)
         {
             Log.e(LOGTAG, "Error on UnZipping file " + inputFile, e);
         }
         finally
         {
             StreamHelper.close(fout);
             StreamHelper.close(is);
             StreamHelper.close(zis);
             StreamHelper.close(baos);
         }
         return null;
     }
 
     @Override
     protected void onProgressUpdate(Integer... data)
     {
         super.onProgressUpdate(data);
         if(plistener != null)
             plistener.notifyProgress(data[0], data[1]);
     }
 
     @Override
     protected void onPostExecute(Object result)
     {
         super.onPostExecute(result);
         if(clistener != null)
             clistener.complete(outputDir);
     }
 
     private static void checkDir(File dir)
     {
         if(!dir.exists())
             dir.mkdir();
         if(!dir.canWrite())
             Log.e(LOGTAG, "Unsufficient permissions to write to dir " + dir);
     }
 }
