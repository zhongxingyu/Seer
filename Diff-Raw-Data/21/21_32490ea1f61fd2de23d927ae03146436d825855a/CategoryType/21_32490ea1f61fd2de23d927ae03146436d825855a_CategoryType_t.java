 /*
  * Copyright (C) 2013 Jack Wakefield
  *
  * This file is part of TorrentFreak Reader.
  *
  * TorrentFreak Reader is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TorrentFreak Reader is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TorrentFreak Reader.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.torrentfreak.reader.free.categories;
 
 import java.lang.Integer;
 import java.util.HashMap;
 import java.util.Map;
 
 public enum CategoryType {
     Category(0),
    LatestNews(1),
    NewsBits(2);
 
     /**
      * The category types mapped to their numerical values.
      */
     private static final Map<Integer, CategoryType> map;
 
     /**
      * The numerical representation of the value.
      */
     private final int value;
 
     static {
         map = new HashMap<Integer, CategoryType>();
 
         // loop through each category type adding it to the map of values
         for (CategoryType categoryType : CategoryType.values()) {
             map.put(categoryType.getValue(), categoryType);
         }
     }
 
     private CategoryType(int value) {
         this.value = value;
     }
 
     public int getValue() {
         return value;
     }
 
     public static CategoryType fromValue(int value) {
         return map.get(value);
     }
 }
