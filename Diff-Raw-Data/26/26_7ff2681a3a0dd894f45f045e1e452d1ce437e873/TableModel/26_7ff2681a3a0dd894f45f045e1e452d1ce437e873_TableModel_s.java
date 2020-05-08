 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
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
 package com.axiastudio.pypapi.ui;
 
 import com.axiastudio.pypapi.db.Store;
 import com.trolltech.qt.QVariant;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.core.Qt.ItemFlags;
 import com.trolltech.qt.core.Qt.Orientation;
 import com.trolltech.qt.gui.QAbstractTableModel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class TableModel extends QAbstractTableModel {
     
     private List<Column> columns;
     private Store store;
     private HashMap cache;
     private Context contextHandle;
     private boolean editable=true;
 
     @Override
     public Object headerData(int i, Orientation orntn, int i1) {
         if( i1 == Qt.ItemDataRole.DisplayRole && orntn == Qt.Orientation.Horizontal ){
             return this.columns.get(i).getLabel();
         }
         return super.headerData(i, orntn, i1);
     }
 
     public Context getContextHandle() {
         return contextHandle;
     }
 
     public void setContextHandle(Context context) {
         this.contextHandle = context;
     }
 
     public TableModel(Store store, List columns){
         if( store == null ) store = new Store(new ArrayList());
         this.store = store;
         if (columns != null) this.setColumns(columns);
         this.cache = new HashMap();
     }
 
     public void setStore(Store store){
         this.store = store;
         this.beginResetModel();
         this.purgeItemCache();
         this.endResetModel();
     }
     public Store getStore(){
         return this.store;
     }
 
     public List<Column> getColumns(){
         return this.columns;
     }
 
     public final void setColumns(List columns){
         this.columns = columns;
     }
 
     @Override
     public QModelIndex index(int row, int column, QModelIndex parent){
         assert parent == null;
         if (row<0 || row>this.store.size()-1){
             QModelIndex qmi = null;
             return qmi;
         }
         QModelIndex qmi = this.createIndex(row, column);
         return qmi;
     }
 
     @Override
     public int rowCount(QModelIndex qmi) {
         return this.store.size();
     }
 
     @Override
     public int columnCount(QModelIndex qmi) {
         return this.columns.size();
     }
 
     @Override
     public Object data(QModelIndex qmi, int role) {
         Object value = new QVariant();
         Item item = null;
         if(qmi == null) return value;
         try {
             item = (Item) this.get(qmi.row(), (Column) this.columns.get(qmi.column()));
         } catch (Exception ex) {
             Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             value = item.get(role);
         } catch (Exception ex) {
             Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         return value;
 
     }
 
     @Override
     public boolean setData(QModelIndex qmi, Object value, int role){
         ItemEditable item = null;
         if(qmi == null) return false;
         try {
             item = (ItemEditable) this.get(qmi.row(), (Column) this.columns.get(qmi.column()));
         } catch (Exception ex) {
             Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         try {
             Object oldValue = item.get(role);
             if (oldValue == null || !oldValue.equals(value)){
                 boolean res = item.set(role, value);
             } else return true;
         } catch (Exception ex) {
             Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         // clean cache
         this.purgeItemCache(this.store.get(qmi.row()), (Column) this.columns.get(qmi.column()));
         this.dataChanged.emit(qmi, qmi);
         return true;
     }
 
     @Override
     public Qt.ItemFlags flags(QModelIndex qmi){
         ItemEditable item = null;
         try {
             item = (ItemEditable) this.get(qmi.row(), (Column) this.columns.get(qmi.column()));
         } catch (Exception ex) {
             Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         ItemFlags flags = item.getFlags();
         if( !this.editable && flags.isSet(Qt.ItemFlag.ItemIsEditable) ){
             flags.clear(Qt.ItemFlag.ItemIsEditable);
         }
         return flags;
     }
 
     private Object get(int row, Column column) throws Exception{
         Object entity = this.store.get(row);
         Object result = this.getByEntity(entity, column);
         return result;
     }
 
     public Object getByEntity(Object entity, Column column) throws Exception {
         Item result;
         HashMap hm;
         if (this.cache.containsKey(entity)) {
             hm = (HashMap) this.cache.get(entity);
             if (hm.containsKey(column)){
                 result = (Item) hm.get(column);
             } else {
                 result = column.bind(entity);
                 hm.put(column, result);
             }
         } else {
             result = column.bind(entity);
             hm = new HashMap();
             hm.put(column, result);
             if( entity.hashCode() != 0){
                 this.cache.put(entity, hm);
             }
         }
         return result;
     }
 
     public Object getEntityByRow(int row){
         return this.store.get(row);
     }
 
     public void replaceEntity(int row, Object entity){
         this.store.remove(row);
         this.store.add(row, entity);
         this.purgeItemCache(entity);
     }
     
     public void purgeItemCache(){
         this.cache = new HashMap();
     }
 
     public void purgeItemCache(Object entity){
         this.cache.remove(entity);
     }
 
     public void purgeItemCache(Object entity, Column column){
         HashMap entityHM = (HashMap) this.cache.get(entity);
         if( entityHM != null ){
             entityHM.remove(column);
         }
     }
 
     /*
      * replace the store with a store based on the passed entity list
      */
     public void replaceRows(List entities){
         this.setStore(new Store(entities));
     }
 
     /*
      * insert entities at the row position
      */
     public boolean insertRows(int row, int count, QModelIndex parent, List entities){
         int first = row;
         int last = row + count - 1;
         this.beginInsertRows(parent, first, last);
         for (int i=0; i<entities.size(); i++){
             Object entity = entities.get(i);
                 this.store.add(first+i, entity);
             }
         this.endInsertRows();
         return true;
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
     
     
 }
 
 
