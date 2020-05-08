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
 
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.core.Qt.ItemFlags;
 import java.lang.reflect.Method;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class BooleanItemField extends ItemField {
     
     public BooleanItemField(Column column, Object value, Method setterMethod,
             Object entity){
         super(column, value, setterMethod, entity);
     }
     
     @Override
     public Qt.CheckState getCheckstate(){
         Boolean b = (Boolean) this.get();
         if( b == null ){
             return Qt.CheckState.PartiallyChecked;        
         } else if( b == true ){
             return Qt.CheckState.Checked;
         } else if( b == false ){
             return Qt.CheckState.Unchecked;
         }
         return Qt.CheckState.PartiallyChecked;    
     }
 
     @Override
     public Boolean getEdit(){
         Boolean b = (Boolean) this.get();
         if( b == null || b == false ){
             return false;
         }
         return true;
     }
     
     @Override
     public boolean setEdit(Object objValue) {
         Boolean state = (Boolean) objValue;
         return this.set(state);
     }
     
     
     public Boolean setCheckstate(Object checkState){
         Boolean boolValue = null;
         if( checkState.equals(Qt.CheckState.Checked.value()) ){
             boolValue = true;
         } else if( checkState.equals(Qt.CheckState.Unchecked.value()) ){
             boolValue = false;
         }
         return this.set(boolValue);
     }
 
     public Boolean setCheckstate(Boolean objValue){
         return this.set(objValue);
     }
     
     @Override
     public Object getDisplay(){
         // XXX: different return for the true value
         /*
          * Different return value to permit that the generic implementation of
          * sort() works with Boolean items.
          * 
          * see:
          * http://qt-project.org/doc/qt-4.8/qsortfilterproxymodel.html#details
          * 
          * QSortFilterProxyModel provides a generic sort() reimplementation that
          * operates on the sortRole() (Qt::DisplayRole by default) of the items 
          * and that understands several data types, including int, QString, and 
          * QDateTime.
          * 
          */
        if( this.value != null && (Boolean) this.value ){
             return "";
         }
         return null;
     }
 
     @Override
     protected ItemFlags getFlags() {
         ItemFlags flags = Qt.ItemFlag.createQFlags();
         flags.set(Qt.ItemFlag.ItemIsSelectable);
         flags.set(Qt.ItemFlag.ItemIsEnabled);
         flags.set(Qt.ItemFlag.ItemIsEditable);
         flags.set(Qt.ItemFlag.ItemIsUserCheckable);
         return flags;
     }
     
 }
