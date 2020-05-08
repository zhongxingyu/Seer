 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core;
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * @author jpaoletti
  * 
  * This class encapsulate an entity, its list and everything associated to an entity. An instance
 * of this class is inserted in web session under demand and stay in session for fast reference.
  * 
  */
 public class EntityContainer {
 
     private String id;
     private String sid;
     private Entity entity;
     private PaginatedList list;
     private Set<Integer> selectedIndexes;
     private EntityInstanceWrapper selected;
     private boolean selectedNew;
     private EntityFilter filter;
     private EntityContainer owner;
     private Operation operation;
 
     /**
      * Main constructor
      * 
      * @param entity The contained entity
      * @param sid The session id
      */
     public EntityContainer(Entity entity, String sid) {
         super();
         setEntity(entity);
         setSid(sid);
         setId(buildId(sid, entity.getId()));
         setSelectedNew(false);
     }
 
     /**
      * Builds a string based on a session id and the entity id. Not implemented.
      * 
      * @param sid A session id
      * @param eid The entity id
      * @return The resulting string
      */
     public static String buildId(String sid, String eid) {
         //return sid.substring(0,20) + eid.hashCode() + sid.substring(20);
         return eid;
     }
 
     /**
      * Getter for the id
      * @return The id
      */
     public String getId() {
         return id;
     }
 
     /**
      *
      * @param id
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * Getter for the entity
      * @return The entity
      */
     public Entity getEntity() {
         return entity;
     }
 
     /**
      *
      * @param entity
      */
     public void setEntity(Entity entity) {
         this.entity = entity;
     }
 
     /**
      * Getter for the list
      * @return The list
      */
     public PaginatedList getList() {
         return list;
     }
 
     /**
      *
      * @param list
      */
     public void setList(PaginatedList list) {
         this.list = list;
     }
 
     /**
      *
      * @param sid
      */
     public void setSid(String sid) {
         this.sid = sid;
     }
 
     /**
      * Getter for the session id
      * @return The  session id
      */
     public String getSid() {
         return sid;
     }
 
     /**
      *
      * @param selected
      */
     public void setSelected(EntityInstanceWrapper selected) {
         this.selected = selected;
         setSelectedNew(false);
     }
 
     /**
      * Getter for the selected instance wrapper
      * @return The wrapper
      */
     public EntityInstanceWrapper getSelected() {
         return selected;
     }
 
     /**
      *
      * @param new_
      */
     public void setSelectedNew(boolean new_) {
         this.selectedNew = new_;
     }
 
     /**
      * Indicate if the actual selected is new
      * @return true when selected is new
      */
     public boolean isSelectedNew() {
         return selectedNew;
     }
 
     /**
      * @param filter the filter to set
      */
     public void setFilter(EntityFilter filter) {
         this.filter = filter;
     }
 
     /**
      * @return the filter
      */
     public EntityFilter getFilter() {
         return filter;
     }
 
     /**
      * @param selectedIndexes the selectedIndexes to set
      */
     public void setSelectedIndexes(Set<Integer> selectedIndexes) {
         this.selectedIndexes = selectedIndexes;
     }
 
     /**
      * @return the selectedIndexes
      */
     public Set<Integer> getSelectedIndexes() {
         if (selectedIndexes == null) {
             selectedIndexes = new HashSet<Integer>();
         }
         return selectedIndexes;
     }
 
     /**
      * Getter for the owner
      * @return The owner
      */
     public EntityContainer getOwner() {
         return owner;
     }
 
     /**
      *
      * @param owner
      */
     public void setOwner(EntityContainer owner) {
         this.owner = owner;
     }
 
     /**
      * Getter for the operation
      * @return The operation
      */
     public Operation getOperation() {
         return operation;
     }
 
     /**
      *
      * @param operation
      */
     public void setOperation(Operation operation) {
         this.operation = operation;
     }
 }
