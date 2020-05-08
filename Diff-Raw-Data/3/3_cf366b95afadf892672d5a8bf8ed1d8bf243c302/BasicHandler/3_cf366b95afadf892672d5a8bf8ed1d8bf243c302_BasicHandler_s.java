 /*******************************************************************************
  * This file is part of Champions.
  *
  *     Champions is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Champions is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Champions.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.github.championsdev.champions.library;
 
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.Set;
 
 /**
  * @author YoshiGenius
  */
 public class BasicHandler<T> {
     private HashMap<String, T> objectMap = new HashMap<>();
 
     public void register(String id, T object) {
         objectMap.put(id, object);
     }
     
     public T get(String id) {
         return objectMap.get(id);
     }
 
     public Collection<T> getAll() {
         return objectMap.values();
     }
 
     public void remove(String id) {
         objectMap.remove(id);
     }
 
     public boolean isRegistered(String id) {
         return objectMap.containsKey(id);
     }
 
 }
