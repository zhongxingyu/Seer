 /*
  * Copyright 2013 Mat Booth <mbooth@apache.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.gpm.calameo;
 
 public class ItemList<T> {
   private String total;
   private int start;
   private int step;
   private T[] items;
 
   public ItemList() {
   }
 
   public String getTotal() {
     return total;
   }
 
   public int getStart() {
     return start;
   }
 
   public int getStep() {
     return step;
   }
 
   public T[] getItems() {
     return items;
   }
 
   @Override
   public String toString() {
     StringBuilder s = new StringBuilder();
    for (T item : getItems()) {
      if (s.toString().isEmpty()) {
        s.append(String.format("{ Total: %s, Start: %d, Step: %d, Items: [ ", getTotal(), getStart(), getStep()));
      } else {
        s.append(", ");
       }
      s.append(item.toString());
     }
    s.append(" ] }");
     return s.toString();
   }
 }
