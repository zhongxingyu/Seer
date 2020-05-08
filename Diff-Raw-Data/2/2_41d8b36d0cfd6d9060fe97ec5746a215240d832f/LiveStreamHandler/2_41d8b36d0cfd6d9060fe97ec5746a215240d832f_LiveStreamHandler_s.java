 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver.http;
 
 import anhttpserver.HttpRequestContext;
 import jstreamserver.utils.Config;
 import jstreamserver.utils.HttpUtils;
 import jstreamserver.utils.ffmpeg.FFMpegSegmenter;
 import jstreamserver.utils.ffmpeg.FrameMessage;
 import jstreamserver.utils.ffmpeg.ProgressListener;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 import java.util.TimeZone;
 
 /**
  * HTTP Live stream handler
  * which implements <a href="http://developer.apple.com/resources/http-streaming/">HTTP Live Streaming</a>
  * {@code FFMpeg} and some implementation of {@code video segmenter} are used on backend.
  *
  * @author Sergey Prilukin
  */
 public final class LiveStreamHandler extends BaseHandler {
 
     public static final String HANDLE_PATH = "/livestream";
     public static final String LIVE_STREAM_FILE_PREFIX = "stream";
     public static final String PLAYLIST_EXTENSION = "m3u8";
     public static final String LIVE_STREAM_FILE_PATH = HANDLE_PATH.substring(1) + "/" + LIVE_STREAM_FILE_PREFIX;
     public static final String PLAYLIST_FULL_PATH = HANDLE_PATH + "/" + LIVE_STREAM_FILE_PREFIX + "." + PLAYLIST_EXTENSION;
 
     public static final SimpleDateFormat FFMPEG_SS_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
     static {
         FFMPEG_SS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
     }
 
     private FFMpegSegmenter ffMpegSegmenter;
     private final Object ffmpegSegmenterMonitor = new Object();
     private final Object playListCreatedMonitor = new Object();
     private ProgressListener progressListener = new LiveStreamProgressListener();
 
     private SegmenterKiller segmenterKiller;
 
     public LiveStreamHandler() {
         super();
     }
 
     @Override
     public InputStream getResponseInternal(HttpRequestContext httpRequestContext) throws IOException {
 
         if (PLAYLIST_FULL_PATH.equals(httpRequestContext.getRequestURI().getPath())) {
             return getPlayList(LIVE_STREAM_FILE_PATH + "." + PLAYLIST_EXTENSION, httpRequestContext);
         } else if (HANDLE_PATH.equals(httpRequestContext.getRequestURI().getPath())) {
             Map<String, String> params = HttpUtils.getURLParams(httpRequestContext.getRequestURI().getRawQuery());
 
             String fileString = params.get("file");
             String audioStreamId = params.get("stream");
             File file = getFile(URLDecoder.decode(fileString, HttpUtils.DEFAULT_ENCODING));
             if (!file.exists() || !file.isFile() || file.isHidden()) {
                 return rendeResourceNotFound(fileString, httpRequestContext);
             } else {
                 //String startTime = params.containsKey("time") ? params.get("time") : FFMPEG_SS_DATE_FORMAT.format(new Date(0)); // start from the beginning by default
                 return getLiveStream(file, fileString, audioStreamId, httpRequestContext);
             }
         } else {
             String path = httpRequestContext.getRequestURI().getPath();
             File file = new File(path.substring(1));
             if (file.exists() && file.isFile()) {
                 updateSegmenterKiller();
                 return getResource(file, httpRequestContext);
             } else {
                 return rendeResourceNotFound(path, httpRequestContext);
             }
         }
     }
 
 
     /*
      * Playlist file is written at the same time by another thread (by segmenter namely)
      * and thus this thread can read non-completed version of the file.
      * In this method we ensure that last line of playlist matches one of the possible formats
      */
     private InputStream getPlayList(String playlist, HttpRequestContext httpRequestContext) throws IOException {
 
         boolean fileIsOk = false;
         StringBuilder sb = null;
 
         while (!fileIsOk) {
             BufferedReader reader = new BufferedReader(new FileReader(playlist));
             sb = new StringBuilder();
 
             String line = "";
 
             while (true) {
                 String temp = reader.readLine();
 
                 if (temp != null) {
                     line = temp;
                     sb.append(line).append("\n");
                 } else {
                     fileIsOk = line.matches("^(.*\\.ts|#.*)$");
                     reader.close();
                     break;
                 }
             }
         }
 
         String extension = FilenameUtils.getExtension(new File(playlist).getName());
         String mimeType = getMimeProperties().getProperty(extension.toLowerCase());
 
         setContentType(mimeType != null ? mimeType : "application/octet-stream", httpRequestContext);
         setResponseHeader("Expires", HTTP_HEADER_DATE_FORMAT.get().format(new Date(0)), httpRequestContext);
         setResponseHeader("Pragma", "no-cache", httpRequestContext);
         setResponseHeader("Cache-Control", "no-store,private,no-cache", httpRequestContext);
         setResponseHeader("Connection", "keep-alive", httpRequestContext);
         setResponseHeader("Content-Disposition", "attachment", httpRequestContext);
         setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
 
         return new ByteArrayInputStream(sb.toString().getBytes());
     }
 
     private void cleanResources() {
         synchronized (ffmpegSegmenterMonitor) {
             if (ffMpegSegmenter != null) {
                 ffMpegSegmenter.destroy();
                 ffMpegSegmenter = null;
             }
         }
 
         File streamDir = new File(HANDLE_PATH.substring(1));
         if (!streamDir.exists()) {
             streamDir.mkdirs();
         }
 
         //Remove all .ts and .m3u8 files
         String[] files = streamDir.list(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 String extension = FilenameUtils.getExtension(name);
                 return extension.equals(PLAYLIST_EXTENSION) || extension.equals("ts");
             }
         });
 
         for (String fileName: files) {
             File file = new File(streamDir + "/" + fileName);
             if (file.exists()) {
                 file.delete();
             }
         }
     }
 
     private InputStream getLiveStream(File file, String fileString, String audioStreamId, HttpRequestContext httpRequestContext) throws IOException {
 
         cleanResources();
 
         String ffmpegMapStreamParams = audioStreamId != null ? String.format(Config.FFMPEG_AUDIO_STREAM_SELECTION_FORMAT, audioStreamId) : "";
 
         synchronized (ffmpegSegmenterMonitor) {
             ffMpegSegmenter = new FFMpegSegmenter();
 
             ffMpegSegmenter.start(
                     getConfig().getFfmpegLocation(),
                     getConfig().getSegmenterLocation(),
                     String.format(getConfig().getFfmpegParams(), file.getAbsolutePath(), ffmpegMapStreamParams),
                     String.format(getConfig().getSegmenterParams(), LIVE_STREAM_FILE_PATH, PLAYLIST_FULL_PATH.substring(1)),
                     progressListener);
 
             try {
                 synchronized (playListCreatedMonitor) {
                     playListCreatedMonitor.wait();
                 }
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
 
         byte[] result = null;
         try {
             JSONObject jsonObject = new JSONObject();
             JSONObject liveStreamSource = new JSONObject();
             liveStreamSource.put("url", PLAYLIST_FULL_PATH);
             liveStreamSource.put("type", getMimeProperties().getProperty(PLAYLIST_EXTENSION));
 
             String extension = FilenameUtils.getExtension(file.getPath());
             JSONObject originalSource = new JSONObject();
             originalSource.put("url", "/?path=" + fileString);
             originalSource.put("type", getMimeProperties().getProperty(extension));
 
             JSONArray sources = new JSONArray();
             sources.put(liveStreamSource);
             sources.put(originalSource);
 
             jsonObject.put("sources", sources);
 
             //getFile
             String subtitlesString = fileString.substring(0, fileString.length() - extension.length() - 1) + ".srt";
             File subtitles = getFile(URLDecoder.decode(subtitlesString, HttpUtils.DEFAULT_ENCODING));
 
             if (subtitles.exists()) {
                 FileInputStream fis = new FileInputStream(subtitles);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 IOUtils.copy(fis, baos);
                 jsonObject.put("subtitle", baos.toString(getConfig().getDefaultTextCharset()));
             } else {
                jsonObject.put("subtitle", null);
             }
 
             result = jsonObject.toString().getBytes();
             setResponseCode(HttpURLConnection.HTTP_OK, httpRequestContext);
             setContentType("text/x-json", httpRequestContext);
             return renderCompressedView(new ByteArrayInputStream(result), httpRequestContext);
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void updateSegmenterKiller() {
         if (segmenterKiller == null) {
             segmenterKiller = new SegmenterKiller();
             segmenterKiller.start();
         } else {
             segmenterKiller.clearTimeout();
         }
     }
 
     class SegmenterKiller extends Thread {
         private boolean timeoutFlag = false;
 
         public void clearTimeout() {
             synchronized (ffmpegSegmenterMonitor) {
                 timeoutFlag = false;
                 ffmpegSegmenterMonitor.notify();
             }
         }
 
         @Override
         public void run() {
             try {
                 synchronized (ffmpegSegmenterMonitor) {
                     while (true) {
                         timeoutFlag = true;
                         ffmpegSegmenterMonitor.wait(getConfig().getSegmenterMaxtimeout());
                         if (timeoutFlag && ffMpegSegmenter != null) {
                             System.out.println("Destroying idle ffmpeg segmenter...");
                             timeoutFlag = false;
                             cleanResources();
                             ffmpegSegmenterMonitor.wait();
                         }
                     }
                 }
             } catch (InterruptedException e) {
                 /* do nothing */
             }
         }
     }
 
     class LiveStreamProgressListener implements ProgressListener {
         @Override
         public void onFrameMessage(FrameMessage frameMessage) {
         }
 
         @Override
         public void onProgress(String progressString) {
         }
 
         @Override
         public void onPlayListCreated() {
             synchronized (playListCreatedMonitor) {
                 playListCreatedMonitor.notify();
             }
         }
 
         @Override
         public void onFinish(int exitCode) {
             System.out.println("Segmenter finished. Exit code: " + exitCode);
         }
     }
 }
