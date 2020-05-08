 /*
  * Copyright (C) 2014 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.rs.key;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 public class ConnectionEntity {
 
     @JsonProperty
     private Map<String, List<SelectEntity<PhoenixEntity>>> connectionMap;
 
     @JsonProperty
     private Map<String, Object> connectionAttribute;
 
     public ConnectionEntity() {
         this.connectionMap = new HashMap<String, List<SelectEntity<PhoenixEntity>>>();
         this.connectionAttribute = new HashMap<String, Object>();
     }
 
     @SuppressWarnings("unchecked")
    public <T extends PhoenixEntity> ConnectionEntity addSelectEntity(Class<T> clazz, SelectEntity<? extends PhoenixEntity> entity) {
         List<SelectEntity<PhoenixEntity>> list = connectionMap.get(clazz.getName());
         if (list == null) {
             list = new ArrayList<SelectEntity<PhoenixEntity>>();
             connectionMap.put(clazz.getName(), list);
         }
         list.add((SelectEntity<PhoenixEntity>) entity);
 
         return this;
     }
 
    public <T extends PhoenixEntity> ConnectionEntity addSelectEntities(Class<T> clazz, List<SelectEntity<T>> entityList) {
         for (SelectEntity<? extends PhoenixEntity> selectEntity : entityList) {
             addSelectEntity(clazz, selectEntity);
         }
 
         return this;
     }
 
     public ConnectionEntity addEntity(PhoenixEntity entity) {
         this.addSelectEntity(entity.getClass(), KeyReader.createSelect(entity));
 
         return this;
     }
 
     public ConnectionEntity addEntities(List<? extends PhoenixEntity> entities) {
         for (PhoenixEntity phoenixEntity : entities) {
             this.addEntity(phoenixEntity);
         }
 
         return this;
     }
 
     public <T extends PhoenixEntity> List<SelectEntity<T>> getSelectEntities(Class<T> clazz) {
         List<SelectEntity<PhoenixEntity>> list = connectionMap.get(clazz.getName());
         return convert(list);
     }
 
     public <T extends PhoenixEntity> SelectEntity<T> getFirstSelectEntity(Class<T> clazz) {
         List<SelectEntity<T>> list = getSelectEntities(clazz);
         return list == null ? null : list.get(0);
     }
 
     public ConnectionEntity addAttribute(String name, Object object) {
         this.connectionAttribute.put(name, object);
 
         return this;
     }
 
     public <T> T getAttribute(String name) {
         @SuppressWarnings("unchecked")
         T t = (T) connectionAttribute.get(name);
         return t;
     }
 
     // This is just ugly
 
     // TODO: Think about a not memory, time and code wasting way!
     @SuppressWarnings("unchecked")
     private <T extends PhoenixEntity> List<SelectEntity<T>> convert(List<SelectEntity<PhoenixEntity>> selectEntities) {
         List<SelectEntity<T>> t = new ArrayList<SelectEntity<T>>(selectEntities.size());
         for (SelectEntity<PhoenixEntity> e : selectEntities) {
             t.add((SelectEntity<T>) e);
         }
         return t;
     }
 }
