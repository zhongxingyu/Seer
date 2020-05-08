 /*
  * Copyright (C) 2012 Wu Tong
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
 package org.cocoa4android.third.sbjson;
 
 import java.util.Iterator;
 
 import org.cocoa4android.ns.NSMutableArray;
 import org.cocoa4android.ns.NSMutableDictionary;
 import org.cocoa4android.ns.NSObject;
 import org.cocoa4android.ns.NSString;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class SBJsonParser extends NSObject {
 	
 	public NSObject objectWithString(NSString repr){
 		String content = repr.getString();
 		content = content.trim();
 		if(content.startsWith("[")){
 			//NSArray
 			return this.parseArray(content);
 		}else if(content.startsWith("{")){
 			return this.parseDictionary(content);
 		}else if(!content.toLowerCase().equals("null")){
 			return repr;
 		}
 		return null;
 	}
 	private NSObject parseArray(String content){
 		try {
 			JSONArray arJsonArray = new JSONArray(content);
 			NSMutableArray array = NSMutableArray.array();
 			
 			for (int i = 0; i < arJsonArray.length(); i++) {
 				String value = arJsonArray.getString(i).trim();
 				if(value.startsWith("[")){
 					NSObject nsArray = this.parseArray(value);
 					array.add(nsArray);
 				}else if(value.startsWith("{")){
 					NSObject nsDictionary = this.parseDictionary(value);
 					array.add(nsDictionary);
 				}else if(!content.toLowerCase().equals("null")){
 					array.add(new NSString(value));
 				}else{
 					array.add(null);
 				}
 			}
 			
 			return array;
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	private NSObject parseDictionary(String content){
 		try {
 			JSONObject jsonObject = new JSONObject(content);
 			@SuppressWarnings("unchecked")
 			Iterator<String> iterator = jsonObject.keys();
 			
 			NSMutableDictionary dic = NSMutableDictionary.dictionary();
 			while (iterator.hasNext()) {
 				String key = (String) iterator.next().trim();
 				String value = jsonObject.getString(key).trim();
 				
 				if(value.startsWith("[")){
 					NSObject array = this.parseArray(content);
 					dic.setObject(array, key);
 				}else if(value.startsWith("{")){
 					NSObject object = this.parseDictionary(content);
 					dic.setObject(object, key);
 				}else if(!value.toLowerCase().equals("null")){
 					dic.setObject(new NSString(value), key);
 				}else{
 					dic.setObject(null, key);
 				}
 			}
			return dic;
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 }
