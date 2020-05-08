 package com.ouchadam.bookkeeper;
 
 import com.ouchadam.bookkeeper.progress.ProgressValues;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 class FileDownloader {
 
     private static final int DOWNLOAD_BUFFER_SIZE = 1024;
     private static final float PERCENTAGE_MULTIPLIER = 100f;
     public static final int BYTE_OFFSET = 0;
 
     private final FileDownloadProgressWatcher progressWatcher;
 
     private int downloadedSize;
     private int totalSize;
     private File file;
     private FileOutputStream fileOutput;
     private InputStream inputStream;
 
     public interface FileDownloadProgressWatcher {
         void onUpdate(ProgressValues progressValues);
     }
 
     public FileDownloader(FileDownloadProgressWatcher progressWatcher) {
         this.progressWatcher = progressWatcher;
     }
 
     public void download(URL from, File to) throws FileDownloadException {
         init(from, to);
         try {
             writeStreamToFile();
         } catch (IOException e) {
             deleteFile(file);
             throw new FileDownloadException(e.getMessage());
         }
     }
 
     private void init(URL fileUrl, File file) {
         this.file = file;
         try {
             HttpURLConnection urlConnection = initConnection(fileUrl);
             fileOutput = getFileOutputStream(file);
             inputStream = urlConnection.getInputStream();
             totalSize = urlConnection.getContentLength();
         } catch (IOException e) {
             e.printStackTrace();
             throw new IllegalStateException(this.getClass().getSimpleName() + " could not be initialised with : " + fileUrl + " & " + file);
         }
     }
 
     private HttpURLConnection initConnection(URL fileUrl) throws IOException {
         HttpURLConnection urlConnection = (HttpURLConnection) fileUrl.openConnection();
 
         urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
 
         urlConnection.connect();
         return urlConnection;
     }
 
     private FileOutputStream getFileOutputStream(File file) throws FileNotFoundException {
         return new FileOutputStream(file);
     }
 
     private void writeStreamToFile() throws IOException {
         byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
         int bufferLength;
         while ((bufferLength = inputStream.read(buffer)) > 0) {
             fileOutput.write(buffer, BYTE_OFFSET, bufferLength);
             downloadedSize += bufferLength;
             updateProgress();
         }
         fileOutput.close();
     }
 
     private void updateProgress() {
         ProgressValues progressValues = new ProgressValues(downloadedSize, getDownloadedPercentage(downloadedSize), totalSize);
         progressWatcher.onUpdate(progressValues);
     }
 
     private int getDownloadedPercentage(float downloadedSize) {
         float percent = (downloadedSize / (float) totalSize) * PERCENTAGE_MULTIPLIER;
         return (int) percent;
     }
 
     private void deleteFile(File file) {
         if (file != null) {
             file.delete();
         }
     }
 
     static class FileDownloadException extends IOException {
 
         FileDownloadException(String message) {
             super(message);
         }
 
     }
 
 }
