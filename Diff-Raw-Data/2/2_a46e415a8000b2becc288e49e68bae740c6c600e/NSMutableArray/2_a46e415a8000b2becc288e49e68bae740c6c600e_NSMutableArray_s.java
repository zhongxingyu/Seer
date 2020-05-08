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
 package org.cocoa4android.ns;
 
 import java.util.ArrayList;
 
 public class NSMutableArray extends NSArray {
	ArrayList<Object> list = null;
	
 	public static NSMutableArray array(){
 		return new NSMutableArray();
 	}
 	public static NSMutableArray arrayWithObject(Object object){
 		return new NSMutableArray(object);
 	}
 	public static NSMutableArray arrayWithObjects(Object ...objects){
 		return new NSMutableArray(objects);
 	}
 	public static NSMutableArray arrayWithArray(NSArray array){
 		return new NSMutableArray(array);
 	}
 	
 	public static NSMutableArray array(int capacity){
 		return new NSMutableArray(capacity);
 	}
 	public NSMutableArray(NSArray array){
 		this.list = array.list;
 	}
 	public NSMutableArray(Object ...objects){
 		list = new ArrayList<Object>(objects.length);
 		for (Object object : objects) {
 			list.add(object);
 		}
 	}
 	public NSMutableArray(Object object){
 		list = new ArrayList<Object>(1);
 		list.add(object);
 	}
 	
 	
 	public NSMutableArray(int capacity){
 		list = new ArrayList<Object>(capacity);
 	}
 	public NSMutableArray(){
 		list = new ArrayList<Object>();
 	}
 	public void addObject(Object anObject){
 		list.add(anObject);
 	}
 	
 }
