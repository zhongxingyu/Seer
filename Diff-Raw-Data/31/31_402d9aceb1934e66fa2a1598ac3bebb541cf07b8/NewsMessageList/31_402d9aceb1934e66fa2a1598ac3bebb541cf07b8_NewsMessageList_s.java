 package com.osastudio.newshub.data;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.base.NewsItemList;
 
 public class NewsMessageList extends NewsItemList<NewsMessage> {
    
   private static final boolean DEBUG = true;
   private static boolean TEST = false;

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
      
      //XTEST
//      if (DEBUG) {
//         NewsMessage msg = new NewsMessage();
//         msg.setId("15");
//         msg.setType(1);
//         msg.setContent("鏂板鏈熷紑濮嬶紝鑷村闀夸滑鐨勪竴灏佷俊");
//         msg.setUserId("10070");
//         msg.setUserName("灏忚彶");
//         this.list.add(msg);
//         
//         msg = new NewsMessage();
//         msg.setId("1");
//         msg.setType(4);
//         msg.setContent("鍙戞帢瀛╁瓙鐨勬綔鍔涘氨鏄煿鍏诲ぉ鎵�);
//         msg.setUserId("10070");
//         msg.setUserName("灏忚彶");
//         this.list.add(msg);
//         
//         msg = new NewsMessage();
//         msg.setId("88");
//         msg.setType(5);
//         msg.setContent("璋佷簡瑙ｆ垜鐨勮鐭�);
//         msg.setUserId("10070");
//         msg.setUserName("灏忚彶");
//         this.list.add(msg);
//      }
    }
 
 }
