 package com.kii.push;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Environment;
 
 import com.kii.cloud.storage.APNSMessage;
 import com.kii.cloud.storage.GCMMessage;
 import com.kii.cloud.storage.KiiPushMessage;
 import com.kii.cloud.storage.KiiPushMessage.Data;
 
 public class MessageTemplateLoader {
 
     private static final String filepath = "com.kii.push/messagetemplate.txt";
     private static final File templateFile = new File(
             Environment.getExternalStorageDirectory(), filepath);
 
     public static JSONObject loadMessageTemplate() throws IOException,
             JSONException {
         BufferedInputStream bis = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try {
             bis = new BufferedInputStream(new FileInputStream(templateFile));
             byte[] buff = new byte[8192];
             while (true) {
                 int len = bis.read(buff);
                 if (len < 1) {
                     break;
                 }
                 baos.write(buff, 0, len);
             }
             String jsonStr = baos.toString("UTF-8");
             return new JSONObject(jsonStr);
         } finally {
             try {
                 if (bis != null)
                     bis.close();
                 baos.close();
             } catch (IOException e1) {
             }
         }
     }
 
     public static KiiPushMessage loadMessageFromTemplate() throws IOException,
             JSONException {
         JSONObject obj = loadMessageTemplate();
         JSONObject dataJson = obj.getJSONObject("data");
         JSONObject gcmJson = obj.getJSONObject("gcm");
         JSONObject apnsJson = obj.getJSONObject("apns");
        KiiPushMessage message = KiiPushMessage
                 .buildWith(generateData(dataJson))
                 .withAPNSMessage(generateAPNSMessage(apnsJson))
                .withGCMMessage(generateGCMMessage(gcmJson)).build();

         return message;
     }
 
     private static GCMMessage generateGCMMessage(JSONObject json)
             throws JSONException {
         GCMMessage.Builder builder = GCMMessage.builder();
         builder.enable(json.getBoolean("enabled"));
         if (json.has("deleyWhileIdle")) {
             builder.delayWhileIdle(json.getBoolean("deleyWhileIdle"));
         }
         if (json.has("collapseKey")) {
             builder.withCollapseKey(json.getString("collapseKey"));
         }
         if (json.has("timeToLive")) {
             builder.withTimeToLive(json.getInt("timeToLive"));
         }
         if (json.has("restrictedPackageName")) {
             builder.withRestrictedPackageName(json
                     .getString("restrictedPackageName"));
         }
         if (json.has("dryRun")) {
             builder.delayWhileIdle(json.getBoolean("dryRun"));
         }
         if (json.has("data")) {
             builder.withData(generateData(json.getJSONObject("data")));
         }
 
         return builder.build();
     }
 
     private static APNSMessage generateAPNSMessage(JSONObject json)
             throws JSONException {
         APNSMessage.Builder builder = APNSMessage.builder();
         builder.enable(json.getBoolean("enabled"));
         if (json.has("sound")) {
             builder.withSound(json.getString("sound"));
         }
         if (json.has("badge")) {
             builder.withBadge(json.getInt("badge"));
         }
         if (json.has("alert")) {
             JSONObject alertJson = json.getJSONObject("alert");
             if (alertJson.has("body"))
                 builder.withAlertBody(alertJson.getString("body"));
             if (alertJson.has("actionLocKey"))
                 builder.withAlertActionLocKey(alertJson
                         .getString("actionLocKey"));
             if (alertJson.has("locKey"))
                 builder.withAlertLocKey(alertJson.getString("locKey"));
             if (alertJson.has("locArgs")) {
                 JSONArray array = alertJson.getJSONArray("locArgs");
                 String[] strArray = new String[array.length()];
                 for (int i = 0; i < array.length(); i++) {
                     strArray[i] = array.getString(i);
                 }
                 builder.withAlertLocArgs(strArray);
             }
         }
         return builder.build();
     }
 
     private static Data generateData(JSONObject json) throws JSONException {
         KiiPushMessage.Data data = new KiiPushMessage.Data();
         Iterator it = json.keys();
         while (it.hasNext()) {
             String key = (String) it.next();
             data.put(key, json.getString(key));
         }
         return data;
     }
 
     public static void launchEditor(Context ctx) {
         Intent i = new Intent();
         i.setAction(Intent.ACTION_VIEW);
         i.setDataAndType(Uri.fromFile(templateFile),
                 "text/plain");
         i.putExtra(Intent.ACTION_VIEW, i.getDataString());
         i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         ctx.startActivity(i);
     }
 
 }
