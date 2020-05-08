 package com.osastudio.newshub.data;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.base.NewsItemList;
 
 public class NewsMessageList extends NewsItemList<NewsMessage> {
    
    public NewsMessageList(JSONObject jsonObject) {
       super(jsonObject);
 
       if (isSuccess()) {
          try {
             if (!jsonObject.isNull(JSON_KEY_LIST)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_LIST);
                NewsMessage msg = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                   try {
                      if (!jsonArray.isNull(i)) {
                         msg = NewsMessage.parseJsonObject(jsonArray
                               .getJSONObject(i));
                         if (msg != null) {
                            this.list.add(msg);
                         }
                      }
                   } catch (JSONException e) {
                      continue;
                   }
                }
                setList(this.list);
             }
          } catch (JSONException e) {
 
          }
       }
    }
 
 }
