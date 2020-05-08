 /*
  * OnlineAction.java
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package webim.struts2.actions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import webim.WebimClient;
 import webim.config.WebimConfig;
 import webim.WebimEndpoint;
 import webim.WebimException;
 import webim.WebimGroup;
 import webim.service.WebimService;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 
 /**
  * 用户上线: /Webim/online.do
  * 
  * @author Ery Lee <ery.lee at gmail.com>
  * @since 1.0
  */
 @SuppressWarnings("serial")
 public class OnlineAction extends ActionSupport {
 
 	private Map<String, Object> data;
 	
 	public Map<String, Object> getData() {
 		return data;
 	}
 	
 	public void setData(Map<String, Object> data) {
 		this.data = data;
 	}
 	
 	private List<String> buddyIds(List<WebimEndpoint> buddies) {
 		List<String> ids = new ArrayList<String>();
 		for(WebimEndpoint b : buddies) {
 			ids.add(b.getId());
 		}
 		return ids;
 	}
 	
 	private List<String> groupIds(List<WebimGroup> groups) {
 		List<String> ids = new ArrayList<String>();
 		for(WebimGroup g : groups) {
 			ids.add(g.getId());
 		}
 		return ids;
 	}
 	
     public String execute() throws Exception {
     	 long uid = WebimService.instance().currentUid();
          List<WebimEndpoint> buddies = WebimService.instance().getBuddies(uid);
          List<WebimGroup> groups = WebimService.instance().getGroups(uid);
          groups.addAll(WebimService.instance().getTmpGroups(uid));
          //Forward Online to IM Server
          WebimClient client = WebimService.instance().currentClient("");
          List<String> buddyIds = buddyIds(buddies);
          List<String> groupIds = groupIds(groups);
          data = new HashMap<String, Object>();
          try
          {
              JSONObject json = client.online(buddyIds, groupIds);
              System.out.println(json.toString());
              Map<String,String> conn = new HashMap<String,String>();
              conn.put("ticket", json.getString("ticket"));
              conn.put("domain", client.getDomain());
              conn.put("jsonpd", json.getString("jsonpd"));
              conn.put("server", json.getString("jsonpd"));
              conn.put("websocket", json.getString("websocket"));
 
              //Online Buddies
              Map<String, WebimEndpoint> buddyMap = new HashMap<String, WebimEndpoint>();
              for (WebimEndpoint e : buddies)
              {
                  buddyMap.put(e.getId(), e);
              }
 
              JSONObject bObj = json.getJSONObject("buddies");
              Iterator<String> it = bObj.keys();
              while (it.hasNext()) {
             	 String key = it.next();
             	 String show = bObj.getString(key);
             	 buddyMap.get(key).setShow(show);
              }
              Collection<WebimEndpoint> rtBuddies;
              if(WebimConfig.SHOW_UNAVAILABLE) {
             	 rtBuddies = buddyMap.values();
              } else {
             	 rtBuddies = new ArrayList<WebimEndpoint>();
             	 for(WebimEndpoint e : buddyMap.values()) {
             		 if(e.getShow() == "available") rtBuddies.add(e);
             	 }
              }
              
              //Groups with count
              Map<String, WebimGroup> groupMap = new HashMap<String, WebimGroup>();
              for (WebimGroup g : groups)
              {
                  groupMap.put(g.getId(), g);
              }
              List<WebimGroup> groups1 = new ArrayList<WebimGroup>();
              JSONObject gObj = json.getJSONObject("groups");
              it = gObj.keys();
              while(it.hasNext()) {
                  String gid = it.next();
                  WebimGroup group = groupMap.get(gid);
                  group.setCount(gObj.getInt(gid));
                  groups1.add(group);
              }
 
              //{"success":true,
              // "connection":{
              // "ticket":"fcc493f7a7b17cfadbf4|admin",
              // "domain":"webim20.cn",
              // "server":"http:\/\/webim20.cn:8000\/packets"},
              // "buddies":[
              //           {"uid":"5","id":"demo","nick":"demo","group":"stranger","url":"home.php?mod=space&uid=5","pic_url":"picurl","status":"","presence":"online","show":"available"}],
              // "rooms":[],
              // "server_time":1370751451399.4,
              // "user":{"uid":"1","id":"admin","nick":"admin","pic_url":"pickurl","show":"available","url":"home.php?mod=space&uid=1","status":""},
              // "new_messages":[]}
 
              data.put("success", true);
              data.put("connection", conn);
              data.put("buddies", rtBuddies.toArray());
              data.put("groups", groups1.toArray());
              data.put("rooms", groups1.toArray());
             data.put("server_time", System.currentTimeMillis()); //TODO: 1000.0
              data.put("user", client.getEndpoint());
          }
          catch (WebimException e)
          {
         	 e.printStackTrace();
              data.put("success", false);
              data.put("error_msg", "IM Server is not found");
          }
          
     	return SUCCESS;
     }
     
 }
