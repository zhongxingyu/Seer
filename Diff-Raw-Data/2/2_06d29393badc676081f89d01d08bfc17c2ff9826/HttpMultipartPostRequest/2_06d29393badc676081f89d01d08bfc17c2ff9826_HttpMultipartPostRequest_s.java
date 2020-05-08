 /*
  * Copyright (C) 2012 xtuaok (http://twitter.com/xtuaok)
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
 
 package xtuaok.sharegyazo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import android.util.Log;
 
 public class HttpMultipartPostRequest {
     private static final String LOG_TAG = HttpMultipartPostRequest.class.toString();
     private static final String BOUNDARY = "----BOUNDARYBOUNDARY----";
 
     private String mCgi;
     private List<NameValuePair> mPostData;
     private byte[] mByteData;
 
     public HttpMultipartPostRequest(String cgi, List<NameValuePair> postData, byte[] byteData) {
         mCgi = cgi;
         mPostData = postData;
         mByteData = byteData;
     }
 
     public String send() {
         URLConnection conn = null;
         String res = null;
         try {
             conn = new URL(mCgi).openConnection();
             conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
             ((HttpURLConnection)conn).setRequestMethod("POST");
             conn.setDoOutput(true);
             conn.connect();
 
             OutputStream os = conn.getOutputStream();
             os.write(createBoundaryMessage("imagedata").getBytes());
             os.write(mByteData);
             String endBoundary = "\r\n--" + BOUNDARY + "--\r\n";
             os.write(endBoundary.getBytes());
             os.close();
 
             InputStream is = conn.getInputStream();
             res = convertToString(is);
         } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
         } finally {
             if (conn != null) {
                 ((HttpURLConnection)conn).disconnect();
             }
         }
         return res;
     }
 
     private String createBoundaryMessage(String fileName) {
         StringBuffer res = new StringBuffer("--").append(BOUNDARY).append("\r\n");
         for (NameValuePair nv : mPostData) {
             res.append("Content-Disposition: form-data; name=\"").append(nv.getName()).append("\"\r\n")
             .append("\r\n").append(nv.getValue()).append("\r\n")
             .append("--").append(BOUNDARY).append("\r\n");
         }
         res.append("Content-Disposition: form-data; name=\"")
         .append(fileName).append("\"; filename=\"").append(fileName).append("\"\r\n\r\n");
         return res.toString();
     }
 
     private String convertToString(InputStream stream) {
         InputStreamReader streamReader = null;
         BufferedReader bufferReader = null;
         try {
             streamReader = new InputStreamReader(stream, "UTF-8");
             bufferReader = new BufferedReader(streamReader);
             StringBuilder builder = new StringBuilder();
             for (String line = null; (line = bufferReader.readLine()) != null;) {
                 builder.append(line).append("\n");
             }
             return builder.toString();
         } catch (UnsupportedEncodingException e) {
             Log.e(LOG_TAG, e.getMessage());
         } catch (IOException e) {
             Log.e(LOG_TAG, e.toString());
         } finally {
             try {
                 stream.close();
                 if (bufferReader != null) {
                     bufferReader.close();
                 }
             } catch (IOException e) {
                 // IOError
                 Log.e(LOG_TAG, e.toString());
             }
         }
         return null;
     }
 }
