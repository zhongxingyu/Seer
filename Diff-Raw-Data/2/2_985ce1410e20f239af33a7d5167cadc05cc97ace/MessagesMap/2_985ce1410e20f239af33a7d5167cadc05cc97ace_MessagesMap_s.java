 package com.allplayers.android;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.allplayers.objects.MessageData;
 import com.google.gson.Gson;
 
 public class MessagesMap {
     private ArrayList<MessageData> mMessageList = new ArrayList<MessageData>();
     private String[] mNames;
 
     public MessagesMap(String jsonResult) {
         Gson gson = new Gson();
 
         try {
             JSONObject jsonObject = new JSONObject(jsonResult);
 
             mNames = getNames(jsonObject);
 
             if (mNames.length > 0) {
                 for (int i = 0; i < mNames.length; i++) {
                     MessageData message = gson.fromJson(jsonObject.getString(mNames[i]), MessageData.class);
 
                     if (message.isNew(mMessageList)) {
                         mMessageList.add(message);
                     }
                 }
             }
         } catch (JSONException ex) {
             System.err.println("MessagesMap/" + ex);
         }
     }
 
     private static String[] getNames(JSONObject jo) {
         int length = jo.length();
 
         if (length == 0) {
             return null;
         }
 
         Iterator<?> iterator = jo.keys();
         String[] names = new String[length];
         int i = 0;
 
         while (iterator.hasNext()) {
             names[i] = (String)iterator.next();
             i++;
         }
         return names;
     }
 
     public ArrayList<MessageData> getMessageData() {
         return mMessageList;
     }
     
     public int size() {
        return mail.size();
     }
 }
