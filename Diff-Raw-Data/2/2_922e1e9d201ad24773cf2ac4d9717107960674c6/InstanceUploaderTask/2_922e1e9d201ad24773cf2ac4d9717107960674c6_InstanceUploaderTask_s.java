 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.radicaldynamic.groupinform.tasks;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.protocol.HttpContext;
 import org.ektorp.Attachment;
 import org.ektorp.AttachmentInputStream;
 import org.ektorp.DbAccessException;
 import org.ektorp.DocumentNotFoundException;
 
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormInstance;
 import com.radicaldynamic.groupinform.documents.Generic;
 import com.radicaldynamic.groupinform.logic.ODKInstanceAttributes;
 import com.radicaldynamic.groupinform.utilities.FileUtilsExtended;
 
 import com.radicaldynamic.groupinform.R;
 import org.odk.collect.android.listeners.InstanceUploaderListener;
 import org.odk.collect.android.preferences.PreferencesActivity;
 import org.odk.collect.android.utilities.FileUtils;
 import org.odk.collect.android.utilities.WebUtils;
 
 import android.content.ContentValues;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.webkit.MimeTypeMap;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.Map.Entry;
 
 /**
  * Background task for uploading completed forms.
  * 
  * @author Carl Hartung (carlhartung@gmail.com)
  */
 // BEGIN custom
 //public class InstanceUploaderTask extends AsyncTask<Long, Integer, HashMap<String, String>> {
 public class InstanceUploaderTask extends AsyncTask<String, Integer, HashMap<String, String>> {
 // END custom
 
     private static String t = "InstanceUploaderTask";
     private InstanceUploaderListener mStateListener;
     private static final int CONNECTION_TIMEOUT = 30000;
     private static final String fail = "FAILED: ";
 
     private URI mAuthRequestingServer;
     HashMap<String, String> mResults;
 
 
     // TODO: This method is like 350 lines long, down from 400.
     // still. ridiculous. make it smaller.
     @Override
     // BEGIN custom
 //    protected HashMap<String, String> doInBackground(Long... values) {
     protected HashMap<String, String> doInBackground(String... values) {
     // END custom    
         mResults = new HashMap<String, String>();
 
         // BEGIN custom
 //        String selection = InstanceColumns._ID + "=?";
 //        String[] selectionArgs = new String[values.length];
 //        for (int i = 0; i < values.length; i++) {
 //            if (i != values.length - 1) {
 //                selection += " or " + InstanceColumns._ID + "=?";
 //            }
 //            selectionArgs[i] = values[i].toString();
 //        }        
         // END custom
 
         // get shared HttpContext so that authentication and cookies are retained.
         HttpContext localContext = Collect.getInstance().getHttpContext();
         HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);
 
         Map<URI, URI> uriRemap = new HashMap<URI, URI>();
 
         // BEGIN custom        
 //      Cursor c =
 //      Collect.getInstance().getContentResolver()
 //              .query(InstanceColumns.CONTENT_URI, null, selection, selectionArgs, null);
 //
 //  if (c.getCount() > 0) {
 //      c.moveToPosition(-1);
 //      next_submission: while (c.moveToNext()) {
 //          if (isCancelled()) {
 //              return mResults;
 //          }        
 //          publishProgress(c.getPosition() + 1, c.getCount());
 //          String instance = c.getString(c.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
 //          String id = c.getString(c.getColumnIndex(InstanceColumns._ID));
 //          Uri toUpdate = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);
 //
 //          String urlString = c.getString(c.getColumnIndex(InstanceColumns.SUBMISSION_URI));
         
             next_submission: for (int i = 0; i < values.length; i++) {
                 if (isCancelled()) {
                     return mResults;
                 }
                 
                 publishProgress(i + 1, values.length);
                 
                 FormInstance instanceDoc = null;
                 String id = values[i];
                 
                 try {
                     instanceDoc = Collect.getInstance().getDbService().getDb().get(FormInstance.class, id);
                 } catch (DocumentNotFoundException e) {
                     Log.w(Collect.LOGTAG, t + "unable to retrieve instance: " + e.toString());
                     mResults.put(id, fail + "warning: document not found :: details: " + e.getMessage());
                     continue;
                 } catch (DbAccessException e) {
                     Log.w(Collect.LOGTAG, t + "unable to access database: " + e.toString());
                     mResults.put(id, fail + "error: could not acess database :: details: " + e.getMessage());
                     continue;
                 } catch (Exception e) {                    
                     Log.e(Collect.LOGTAG, t + "unexpected exception: " + e.toString());                    
                     e.printStackTrace();
                     mResults.put(id, fail + "unexpected error :: details: " + e.getMessage());
                     continue;
                 }
                 
                 String urlString = instanceDoc.getOdk().getUploadUri();
                 // END custom
                 
                 if (urlString == null) {
                     SharedPreferences settings =
                         PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                    urlString = settings.getString(PreferencesActivity.KEY_SERVER_URL, null);
                     String submissionUrl =
                         settings.getString(PreferencesActivity.KEY_SUBMISSION_URL, "/submission");
                     urlString = urlString + submissionUrl;
                 }
 
                 ContentValues cv = new ContentValues();
                 URI u = null;
                 try {
                     URL url = new URL(urlString);
                     u = url.toURI();
                 } catch (MalformedURLException e) {
                     e.printStackTrace();
                     mResults.put(id,
                         fail + "invalid url: " + urlString + " :: details: " + e.getMessage());
                     // BEGIN custom
 //                    cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                    Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                     
                     try {
                         instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                         Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                     } catch (Exception e1) {
                         Log.e(Collect.LOGTAG, t + ": could not record upload failed because of MalformedURLException for " + id + ": " + e1.toString());
                     }                    
                     // END custom
                     continue;
                 } catch (URISyntaxException e) {
                     e.printStackTrace();
                     mResults.put(id,
                         fail + "invalid uri: " + urlString + " :: details: " + e.getMessage());
 //                    cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                    Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                     
                     try {
                         instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                         Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                     } catch (Exception e1) {
                         Log.e(Collect.LOGTAG, t + ": could not record upload failed because of URISyntaxException for " + id + ": " + e1.toString());
                     }   
                     // END custom
                     continue;
                 }
 
                 boolean openRosaServer = false;
                 if (uriRemap.containsKey(u)) {
                     // we already issued a head request and got a response,
                     // so we know the proper URL to send the submission to
                     // and the proper scheme. We also know that it was an
                     // OpenRosa compliant server.
                     openRosaServer = true;
                     u = uriRemap.get(u);
                 } else {
                     // we need to issue a head request
                     HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);
 
                     // prepare response
                     HttpResponse response = null;
                     try {
                         response = httpclient.execute(httpHead, localContext);
                         int statusCode = response.getStatusLine().getStatusCode();
                         if (statusCode == 401) {
                             // we need authentication, so stop and return what we've
                             // done so far.
                             mAuthRequestingServer = u;
                         } else if (statusCode == 204) {
                             Header[] locations = response.getHeaders("Location");
                             if (locations != null && locations.length == 1) {
                                 try {
                                     URL url = new URL(locations[0].getValue());
                                     URI uNew = url.toURI();
                                     if (u.getHost().equalsIgnoreCase(uNew.getHost())) {
                                         openRosaServer = true;
                                         // trust the server to tell us a new location
                                         // ... and possibly to use https instead.
                                         uriRemap.put(u, uNew);
                                         u = uNew;
                                     } else {
                                         // Don't follow a redirection attempt to a different host.
                                         // We can't tell if this is a spoof or not.
                                         mResults.put(
                                             id,
                                             fail
                                                     + "Unexpected redirection attempt to a different host: "
                                                     + uNew.toString());
                                         // BEGIN custom
 //                                        cv.put(InstanceColumns.STATUS,
 //                                            InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                                        Collect.getInstance().getContentResolver()
 //                                                .update(toUpdate, cv, null, null);
                                         
                                         try {
                                             instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                                             Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                                         } catch (Exception e1) {
                                             Log.e(Collect.LOGTAG, t + ": could not record upload failed because of redirection error for " + id + ": " + e1.toString());
                                         }  
                                         // END custom
                                         continue;
                                     }
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                     mResults.put(id, fail + e.getMessage());
                                     // BEGIN custom
 //                                    cv.put(InstanceColumns.STATUS,
 //                                        InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                                    Collect.getInstance().getContentResolver()
 //                                            .update(toUpdate, cv, null, null);
                                     
                                     try {
                                         instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                                         Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                                     } catch (Exception e1) {
                                         Log.e(Collect.LOGTAG, t + ": could not record upload failed because of unexpected exception for " + id + ": " + e1.toString());
                                     } 
                                     // END custom
                                     continue;
                                 }
                             }
                         } else {
                             // may be a server that does not handle
                             try {
                                 // have to read the stream in order to reuse the connection
                                 InputStream is = response.getEntity().getContent();
                                 // read to end of stream...
                                 final long count = 1024L;
                                 while (is.skip(count) == count)
                                     ;
                                 is.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
 
                             Log.w(t, "Status code on Head request: " + statusCode);
                             if (statusCode >= 200 && statusCode <= 299) {
                                 mResults.put(id, fail + "network login? ");
                                 // BEGIN custom
 //                                cv.put(InstanceColumns.STATUS,
 //                                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                                Collect.getInstance().getContentResolver()
 //                                        .update(toUpdate, cv, null, null);
                                 
                                 try {
                                     instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                                     Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                                 } catch (Exception e1) {
                                     Log.e(Collect.LOGTAG, t + ": could not record upload failed because of network login error for " + id + ": " + e1.toString());
                                 } 
                                 // END custom
                                 continue;
                             }
                         }
                     } catch (ClientProtocolException e) {
                         e.printStackTrace();
                         mResults.put(id, fail + "client protocol exeption?");
                         // BEGIN custom
 //                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                         
                         try {
                             instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                             Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                         } catch (Exception e1) {
                             Log.e(Collect.LOGTAG, t + ": could not record upload failed because of client protocol exception for " + id + ": " + e1.toString());
                         }
                         // END custom
                         continue;
                     } catch (Exception e) {
                         e.printStackTrace();
                         mResults.put(id, fail + "generic excpetion.  great");
 //                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                         
                         try {
                             instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                             Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                         } catch (Exception e1) {
                             Log.e(Collect.LOGTAG, t + ": could not record upload failed because of (generic) unexpected exception for " + id + ": " + e1.toString());
                         }
                         // END custom
                         continue;
                     }
                 }
 
                 // At this point, we may have updated the uri to use https.
                 // This occurs only if the Location header keeps the host name
                 // the same. If it specifies a different host name, we error
                 // out.
                 //
                 // And we may have set authentication cookies in our
                 // cookiestore (referenced by localContext) that will enable
                 // authenticated publication to the server.
                 //
                 // BEGIN custom                
                 String uploadFolder = FileUtilsExtended.ODK_UPLOAD_PATH + File.separator + UUID.randomUUID();
                 FileUtils.createFolder(uploadFolder);
                 
                 try {                    
                     HashMap<String, Attachment> attachments = (HashMap<String, Attachment>) instanceDoc.getAttachments();
                     
                     // Download files from database
                     for (Entry<String, Attachment> entry : attachments.entrySet()) {                    
                         String key = entry.getKey();
 
                         AttachmentInputStream ais = Collect.getInstance().getDbService().getDb().getAttachment(id, key);
 
                         // ODK code below expects the XML instance to have a .xml extension
                         if (key.equals("xml")) 
                             key = id + ".xml";
 
                         FileOutputStream file = new FileOutputStream(new File(uploadFolder, key));
                         byte[] buffer = new byte[8192];
                         int bytesRead = 0;                    
 
                         while ((bytesRead = ais.read(buffer)) != -1) {
                             file.write(buffer, 0, bytesRead);
                         }
 
                         ais.close();
                         file.close();
                     }
                 } catch (DocumentNotFoundException e) {
                     Log.w(Collect.LOGTAG, t + "unable to retrieve attachment: " + e.toString());
                     mResults.put(id, fail + "warning: attachment not found :: details: " + e.getMessage());
                     continue;
                 } catch (DbAccessException e) {
                     Log.w(Collect.LOGTAG, t + "unable to access database: " + e.toString());
                     mResults.put(id, fail + "error: could not acess database :: details: " + e.getMessage());
                     continue;
                 } catch (Exception e) {                    
                     Log.e(Collect.LOGTAG, t + "unexpected exception: " + e.toString());                    
                     e.printStackTrace();
                     mResults.put(id, fail + "unexpected error :: details: " + e.getMessage());
                     continue;
                 }
                 // END custom
 
                 // get instance file
                 // BEGIN custom
 //                File instanceFile = new File(instance);
                 File instanceFile = new File(uploadFolder, id + ".xml");
                 // END custom
 
                 if (!instanceFile.exists()) {
                     mResults.put(id, fail + "instance XML file does not exist!");
                     // BEGIN custom
 //                    cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                    Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                     
                     try {
                         instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                         Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                     } catch (Exception e1) {
                         Log.e(Collect.LOGTAG, t + ": could not record upload failed because of missing instance file for " + id + ": " + e1.toString());
                     }
                     // END custom
                     continue;
                 }
 
                 // find all files in parent directory
                 File[] allFiles = instanceFile.getParentFile().listFiles();
 
                 // add media files
                 List<File> files = new ArrayList<File>();
                 for (File f : allFiles) {
                     String fileName = f.getName();
 
                     int dotIndex = fileName.lastIndexOf(".");
                     String extension = "";
                     if (dotIndex != -1) {
                         extension = fileName.substring(dotIndex + 1);
                     }
 
                     if (fileName.startsWith(".")) {
                         // ignore invisible files
                         continue;
                     }
                     if (fileName.equals(instanceFile.getName())) {
                         continue; // the xml file has already been added
                     } else if (openRosaServer) {
                         files.add(f);
                     } else if (extension.equals("jpg")) { // legacy 0.9x
                         files.add(f);
                     } else if (extension.equals("3gpp")) { // legacy 0.9x
                         files.add(f);
                     } else if (extension.equals("3gp")) { // legacy 0.9x
                         files.add(f);
                     } else if (extension.equals("mp4")) { // legacy 0.9x
                         files.add(f);
                     } else {
                         Log.w(t, "unrecognized file type " + f.getName());
                     }
                 }
 
                 boolean first = true;
                 int j = 0;
                 while (j < files.size() || first) {
                     first = false;
 
                     HttpPost httppost = WebUtils.createOpenRosaHttpPost(u);
 
                     MimeTypeMap m = MimeTypeMap.getSingleton();
 
                     long byteCount = 0L;
 
                     // mime post
                     MultipartEntity entity = new MultipartEntity();
 
                     // add the submission file first...
                     FileBody fb = new FileBody(instanceFile, "text/xml");
                     entity.addPart("xml_submission_file", fb);
                     Log.i(t, "added xml_submission_file: " + instanceFile.getName());
                     byteCount += instanceFile.length();
 
                     for (; j < files.size(); j++) {
                         File f = files.get(j);
                         String fileName = f.getName();
                         int idx = fileName.lastIndexOf(".");
                         String extension = "";
                         if (idx != -1) {
                             extension = fileName.substring(idx + 1);
                         }
                         String contentType = m.getMimeTypeFromExtension(extension);
 
                         // we will be processing every one of these, so
                         // we only need to deal with the content type determination...
                         if (extension.equals("xml")) {
                             fb = new FileBody(f, "text/xml");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added xml file " + f.getName());
                         } else if (extension.equals("jpg")) {
                             fb = new FileBody(f, "image/jpeg");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added image file " + f.getName());
                         } else if (extension.equals("3gpp")) {
                             fb = new FileBody(f, "audio/3gpp");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added audio file " + f.getName());
                         } else if (extension.equals("3gp")) {
                             fb = new FileBody(f, "video/3gpp");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added video file " + f.getName());
                         } else if (extension.equals("mp4")) {
                             fb = new FileBody(f, "video/mp4");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added video file " + f.getName());
                         } else if (extension.equals("csv")) {
                             fb = new FileBody(f, "text/csv");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added csv file " + f.getName());
                         } else if (extension.equals("xls")) {
                             fb = new FileBody(f, "application/vnd.ms-excel");
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t, "added xls file " + f.getName());
                         } else if (contentType != null) {
                             fb = new FileBody(f, contentType);
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.i(t,
                                 "added recognized filetype (" + contentType + ") " + f.getName());
                         } else {
                             contentType = "application/octet-stream";
                             fb = new FileBody(f, contentType);
                             entity.addPart(f.getName(), fb);
                             byteCount += f.length();
                             Log.w(t, "added unrecognized file (" + contentType + ") " + f.getName());
                         }
 
                         // we've added at least one attachment to the request...
                         if (j + 1 < files.size()) {
                             if (byteCount + files.get(j + 1).length() > 10000000L) {
                                 // the next file would exceed the 10MB threshold...
                                 Log.i(t, "Extremely long post is being split into multiple posts");
                                 try {
                                     StringBody sb = new StringBody("yes", Charset.forName("UTF-8"));
                                     entity.addPart("*isIncomplete*", sb);
                                 } catch (Exception e) {
                                     e.printStackTrace(); // never happens...
                                 }
                                 ++j; // advance over the last attachment added...
                                 break;
                             }
                         }
                     }
 
                     httppost.setEntity(entity);
 
                     // prepare response and return uploaded
                     HttpResponse response = null;
                     try {
                         response = httpclient.execute(httppost, localContext);
                         int responseCode = response.getStatusLine().getStatusCode();
 
                         try {
                             // have to read the stream in order to reuse the connection
                             InputStream is = response.getEntity().getContent();
                             // read to end of stream...
                             final long count = 1024L;
                             while (is.skip(count) == count)
                                 ;
                             is.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                         Log.i(t, "Response code:" + responseCode);
                         // verify that the response was a 201 or 202.
                         // If it wasn't, the submission has failed.
                         if (responseCode != 201 && responseCode != 202) {
                             if (responseCode == 200) {
                                 mResults.put(id, fail + "Network login failure?  again?");
                             } else {
                                 mResults.put(id, fail + responseCode + " returned "
                                         + response.getStatusLine().getReasonPhrase());
                             }
                             // BEGIN custom
 //                            cv.put(InstanceColumns.STATUS,
 //                                InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                            Collect.getInstance().getContentResolver()
 //                                    .update(toUpdate, cv, null, null);
                             
                             try {
                                 instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                                 Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                             } catch (Exception e1) {
                                 Log.e(Collect.LOGTAG, t + ": could not record upload failed because of network login error for " + id + ": " + e1.toString());
                             }
                             // END custom
                             continue next_submission;
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         mResults.put(id, fail + "generic exception... " + e.getMessage());
                         // BEGIN custom
 //                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
 //                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                         
                         try {
                             instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.failed);
                             Collect.getInstance().getDbService().getDb().update(instanceDoc);                        
                         } catch (Exception e1) {
                             Log.e(Collect.LOGTAG, t + ": could not record upload failed because of generic exception for " + id + ": " + e1.toString());
                         }
                         // END custom
                         continue next_submission;
                     }
                 }
 
                 // if it got here, it must have worked
                 mResults.put(id, Collect.getInstance().getString(R.string.success));
                 // BEGIN custom
 //                cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
 //                Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                 // END custom
 
                 // BEGIN custom
                 instanceDoc.getOdk().setUploadStatus(ODKInstanceAttributes.UploadStatus.complete);                
                 instanceDoc.getOdk().setUploadDate(Generic.generateTimestamp());
                 
                 try {
                     Collect.getInstance().getDbService().getDb().update(instanceDoc);    
                 } catch (Exception e) {
                     Log.e(Collect.LOGTAG, t + "unable to setUploadDate of successful upload: " + e.toString());
                     e.printStackTrace();
                 } finally {
                     FileUtilsExtended.deleteFolder(uploadFolder);
                 }
                 // END custom
             }
             // BEGIN custom
 //            if (c != null) {
 //                c.close();
 //            }
 //
 //        } // end while
             // END custom
         
         return mResults;
     }
 
 
     @Override
     protected void onPostExecute(HashMap<String, String> value) {
         synchronized (this) {
             if (mStateListener != null) {
                 if (mAuthRequestingServer != null) {
                     mStateListener.authRequest(mAuthRequestingServer, mResults);
                 } else {
                     mStateListener.uploadingComplete(value);
                 }
             }
         }
     }
 
 
     @Override
     protected void onProgressUpdate(Integer... values) {
         synchronized (this) {
             if (mStateListener != null) {
                 // update progress and total
                 mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
             }
         }
     }
 
 
     public void setUploaderListener(InstanceUploaderListener sl) {
         synchronized (this) {
             mStateListener = sl;
         }
     }
 }
