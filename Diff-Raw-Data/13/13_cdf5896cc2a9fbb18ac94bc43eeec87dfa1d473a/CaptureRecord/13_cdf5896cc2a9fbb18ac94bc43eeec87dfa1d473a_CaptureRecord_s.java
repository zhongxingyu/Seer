 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.capture;
 
 import android.content.Context;
 import android.util.Base64;
 import android.util.Pair;
 import com.janrain.android.Jump;
 import com.janrain.android.utils.JsonUtils;
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 
 import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
 import static com.janrain.android.capture.Capture.FetchJsonCallback;
 import static com.janrain.android.capture.Capture.InvalidApidChangeException;
 import static com.janrain.android.utils.JsonUtils.copyJsonVal;
 import static com.janrain.android.utils.AndroidUtils.urlEncode;
 import static com.janrain.android.utils.JsonUtils.unsafeJsonObjectToString;
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 public class CaptureRecord extends JSONObject {
     private static final SimpleDateFormat CAPTURE_API_SIGNATURE_DATE_FORMAT;
     private static final String JR_CAPTURE_SIGNED_IN_USER_FILENAME = "jr_capture_signed_in_user";
 
     static {
         CAPTURE_API_SIGNATURE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         CAPTURE_API_SIGNATURE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     private JSONObject original;
 
     /*package*/ String accessToken;
     /*package*/ String refreshSecret;
 
     private CaptureRecord(){}
 
     /**
      * Instantiates a new CaptureRecord model from a JSON representation of the record
      * @param jo a JSON representation of a Capture record, e.g. as from the response to oauth/auth_native
      */
     /*package*/ CaptureRecord(JSONObject jo, String accessToken, String refreshSecret) {
         super();
 
         original = (JSONObject) copyJsonVal(jo);
         JsonUtils.deepCopy(original, this);
         this.accessToken = accessToken;
         this.refreshSecret = refreshSecret;
     }
 
     /**
      * Loads a Capture user from a well-known filename on disk.
      * @param applicationContext the context from which to interact with the disk
      * @return the loaded record, or null
      */
     public static CaptureRecord loadFromDisk(Context applicationContext) {
         String fileContents = null;
         try {
             FileInputStream fis = applicationContext.openFileInput(JR_CAPTURE_SIGNED_IN_USER_FILENAME);
             fileContents = CaptureStringUtils.readFully(fis);
             JSONObject serializedVersion = new JSONObject(fileContents);
             CaptureRecord loadedRecord = new CaptureRecord();
             loadedRecord.original = serializedVersion.getJSONObject("original");
             loadedRecord.accessToken = serializedVersion.getString("accessToken");
            loadedRecord.refreshSecret = serializedVersion.getString("refreshSecret");
             JsonUtils.deepCopy(serializedVersion.getJSONObject("this"), loadedRecord);
             fis.close();
         } catch (FileNotFoundException ignore) {
         } catch (UnsupportedEncodingException e) {
             throwDebugException(new RuntimeException("Unexpected", e));
             return null;
         } catch (IOException e) {
             throwDebugException(new RuntimeException("Unexpected", e));
         } catch (JSONException ignore) {
             LogUtils.loge("Bad CaptureRecord file contents:\n" + fileContents, ignore);
             // Will happen on corrupted file
         }
         return null;
     }
 
     /**
      * Saves the Capture record to a well-known private file on disk.
      * @param applicationContext the context to use to write to disk
      */
     public void saveToDisk(Context applicationContext) {
         try {
             FileOutputStream fos = applicationContext.openFileOutput(JR_CAPTURE_SIGNED_IN_USER_FILENAME, 0);
             JSONObject serializedVersion = new JSONObject();
             serializedVersion.put("original", original);
             serializedVersion.put("accessToken", accessToken);
             serializedVersion.put("refreshSecret", refreshSecret);
             serializedVersion.put("this", this);
             fos.write(serializedVersion.toString().getBytes("UTF-8"));
             fos.close();
         } catch (JSONException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (IOException e) {
             throw new RuntimeException("Unexpected", e);
         }
     }
 
     /**
      * Deletes the record saved to disk
      * @param applicationContext the context with which to delete the saved record
      */
     public static void deleteFromDisk(Context applicationContext) {
         applicationContext.deleteFile(JR_CAPTURE_SIGNED_IN_USER_FILENAME);
     }
 
     /**
      * Uses this record's refresh secret to refresh its access token
      * @param callback your handler, invoked upon completion
      */
     public void refreshAccessToken(final CaptureApiRequestCallback callback) {
         //accessToken = "6bunfwu42h2rwgbq";
         //refreshSecret = "a";
         //String domain = "test-multi.janraincapture.com";
         String domain = Jump.getCaptureDomain();
 
         CaptureApiConnection c = new CaptureApiConnection("https://" + domain + "/access/getAccessToken");
         String date = CAPTURE_API_SIGNATURE_DATE_FORMAT.format(new Date());
         Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
         //params.add(new Pair<String, String>("application_id", "fvbamf9kkkad3gnd9qyb4ggw6w"));
         params.add(new Pair<String, String>("access_token", accessToken));
         params.add(new Pair<String, String>("Signature", urlEncode(getRefreshSignature(date))));
         params.add(new Pair<String, String>("Date", urlEncode(date)));
         c.addAllToParams(params);
         c.fetchResponseAsJson(new FetchJsonCallback() {
             public void run(JSONObject response) {
                 if (response == null) {
                     if (callback != null) callback.onFailure(CaptureApiError.INVALID_API_RESPONSE);
                     return;
                 }
 
                 if ("ok".equals(response.opt("stat"))) {
                     accessToken = (String) response.opt("access_token");
                     if (callback != null) callback.onSuccess();
                 } else {
                     new CaptureApiError(response);
                 }
             }
         });
     }
 
     private String getRefreshSignature(String date) {
         String stringToSign = "refresh_access_token\n" + date + "\n" + accessToken + "\n";
 
         byte[] hash;
         try {
             Mac mac = Mac.getInstance("HmacSHA1");
             SecretKeySpec secret = new SecretKeySpec(refreshSecret.getBytes("UTF-8"), mac.getAlgorithm());
             mac.init(secret);
             hash = mac.doFinal(stringToSign.getBytes("UTF-8"));
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (InvalidKeyException e) {
             throw new RuntimeException("Unexpected", e);
         }
 
         return Base64.encodeToString(hash, Base64.DEFAULT);
     }
 
     /**
      * Synchronizes the Capture record with the Capture service
      * Note that this sends any local changes to the service, but does not retrieve updates from the service.
      * @param callback your callback handler
      * @throws InvalidApidChangeException
      */
     public void synchronize(final CaptureApiRequestCallback callback)
             throws InvalidApidChangeException {
         Set<ApidChange> changeSet = getApidChangeSet();
         List<ApidChange> changeList = new ArrayList<ApidChange>();
         changeList.addAll(changeSet);
 
         if (accessToken == null) throwDebugException(new IllegalStateException());
         fireNextChange(changeList, callback);
 
         // add params to initinstance or something to init capture settings
         // plumb tokenURL to capture
         // plumb handler to construct this
         // add method for trad sign-in
     }
 
     private void fireNextChange(final List<ApidChange> changeList, final CaptureApiRequestCallback callback) {
         if (changeList.size() == 0) {
             if (callback != null) callback.onSuccess();
             return;
         }
 
         final ApidChange change = changeList.get(0);
         Set<Pair<String, String>> params = new HashSet<Pair<String, String>>(change.getBodyParams());
         params.add(new Pair<String, String>("access_token", accessToken));
 
         Capture.FetchJsonCallback jsonCallback = new FetchJsonCallback() {
             public void run(JSONObject content) {
                 if (content.opt("stat").equals("ok")) {
                     LogUtils.logd("Capture", change.toString());
                     LogUtils.logd("Capture", unsafeJsonObjectToString(content, 2));
                     fireNextChange(changeList.subList(1, changeList.size()), callback);
                 } else {
                     if (callback != null) callback.onFailure(new CaptureApiError(content));
                 }
             }
         };
 
         CaptureApiConnection connection = new CaptureApiConnection(change.getUrlFor().toString());
         connection.addAllToParams(params);
         connection.fetchResponseAsJson(jsonCallback);
     }
 
     private Set<ApidChange> collapseApidChanges(Set<ApidChange> changeSet) {
         HashMap<String, Set<ApidUpdate>> subentityUpdateBuckets = new HashMap<String, Set<ApidUpdate>>();
 
         Set<ApidChange> collapsedChangeSet = new HashSet<ApidChange>();
         for (ApidChange change : changeSet) {
             if (change instanceof ApidUpdate) {
                 String parent = change.findClosestParentSubentity();
                 ApidUpdate rewritten =
                         rewriteUpdateForParent((ApidUpdate) change, parent);
                 Set<ApidUpdate> bucket = subentityUpdateBuckets.get(parent);
                 if (bucket == null) {
                     subentityUpdateBuckets.put(parent, bucket = new HashSet<ApidUpdate>());
                 }
                 bucket.add(rewritten);
             } else if (change instanceof ApidReplace) {
                 collapsedChangeSet.add(change);
             } else if (change instanceof ApidDelete) {
                 collapsedChangeSet.add(change);
             }
         }
 
         collapsedChangeSet.addAll(collapseApidUpdateBuckets(subentityUpdateBuckets));
 
         return collapsedChangeSet;
     }
 
     private static Set<? extends ApidChange> collapseApidUpdateBuckets(
             Map<String, Set<ApidUpdate>> subentityUpdateBuckets) {
         Set<ApidChange> collapsedApidUpdates = new HashSet<ApidChange>();
         for (String subentity : subentityUpdateBuckets.keySet()) {
             ApidUpdate collapsedUpdate = null;
             for (ApidUpdate update : subentityUpdateBuckets.get(subentity)) {
                 if (collapsedUpdate == null) {
                     collapsedUpdate = new ApidUpdate(update.newVal, update.attrPath);
                 } else {
                     collapsedUpdate = collapsedUpdate.collapseWith(update);
                 }
             }
 
             if (collapsedUpdate != null) {
                 collapsedApidUpdates.add(collapsedUpdate);
             } else {
                 throw new RuntimeException("Unexpected null collapsed update");
             }
         }
 
         return collapsedApidUpdates;
     }
 
     private static ApidUpdate rewriteUpdateForParent(ApidUpdate update, String parent) {
         String subObjectPath = update.attrPath.replaceFirst(parent, "");
         String[] flattenedObjectPaths = subObjectPath.split("/");
         Object newVal = update.newVal;
         for (int i = flattenedObjectPaths.length - 1; i >= 0; i--) {
             String s = flattenedObjectPaths[i];
             if (s.equals("")) continue; // ignore bad~ path components from .split
             JSONObject wrapper = new JSONObject();
             try {
                 wrapper.put(s, newVal);
             } catch (JSONException e) {
                 throw new RuntimeException("Unexpected");
             }
             newVal = wrapper;
         }
         return new ApidUpdate(newVal, parent);
     }
 
     private Set<ApidChange> getApidChangeSet() throws InvalidApidChangeException {
         return collapseApidChanges(CaptureJsonUtils.compileChangeSet(original, this));
     }
 }
