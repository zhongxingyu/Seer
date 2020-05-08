 /*
  * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.lidroid.xutils.db.sqlite;
 
 import com.lidroid.xutils.db.table.KeyValue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: wyouflf
  * Date: 13-7-29
  * Time: 上午9:35
  */
 public class WhereBuilder {
 
     private List<String> whereItems;
 
     public WhereBuilder() {
         this.whereItems = new ArrayList<String>();
     }
 
     public WhereBuilder append(KeyValue keyValue, String op) {
         StringBuffer sqlSb = new StringBuffer(keyValue.getKey()).append(" " + op + " ");
         Object value = keyValue.getValue();
         if (value == null) {
             sqlSb.append("NULL");
         } else if (value instanceof String) {
             sqlSb.append("'" + value + "'");
         }
         whereItems.add(sqlSb.toString());
         return this;
     }
 
     @Override
     public String toString() {
         if (whereItems == null || whereItems.size() < 1) {
             return "";
         }
         StringBuffer sb = new StringBuffer();
         for (String item : whereItems) {
            sb.append(" AND ");
             sb.append(item);
         }
         return sb.toString();
     }
 }
