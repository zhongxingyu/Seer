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
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.ui.widgets.PyPaPiTableView;
 import com.trolltech.qt.core.QFile;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.designer.QUiLoader;
 import com.trolltech.qt.designer.QUiLoaderException;
 import com.trolltech.qt.gui.QCheckBox;
 import com.trolltech.qt.gui.QComboBox;
 import com.trolltech.qt.gui.QDateTimeEdit;
 import com.trolltech.qt.gui.QHeaderView;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QMainWindow;
 import com.trolltech.qt.gui.QSpinBox;
 import com.trolltech.qt.gui.QTextEdit;
 import com.trolltech.qt.gui.QWidget;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * UIInspector is a utility that parses the ui file generated with Qt Designer,
  * extracts and registers the dynamic properties (such "lookup", "search",
  * "validator", "private") in a EntityBehaviour object.
  * 
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class UIInspector {
     
     private String entityClassName;
     private List<Column> entities = new ArrayList();;
 
     public UIInspector(Class factory, String entityClassName) {
         this.entityClassName = entityClassName;
         QMainWindow window = AutoLayout.createWindowLayout(factory);
         this.register(window);
     }
     
     public UIInspector(String uiFile, String entityClassName) {
         this.entityClassName = entityClassName;
         QWidget window = null;
         QFile file = Util.ui2jui(new QFile(uiFile));
         try {
             window = QUiLoader.load(file);
         } catch (QUiLoaderException ex) {
             Logger.getLogger(UIInspector.class.getName()).log(Level.SEVERE, null, ex);
         }
         if( window != null ){
             this.register(window);
         }
     }
     
     private Map<QObject, QLabel> extractBuddies(QWidget win){
         Map<QObject, QLabel> buddies = new HashMap();
         List children = win.findChildren();
         for (int i=0; i<children.size(); i++){
             QObject child = (QObject) children.get(i);
             if( child instanceof QLabel ){
                 QLabel label = (QLabel) child;
                 if( label.buddy() != null ){
                     buddies.put(label.buddy(), label);
                 }
             }
         }
         return buddies;
     }
     
     private void register(QWidget win){
         EntityBehavior behavior = new EntityBehavior(this.entityClassName);
         List<Column> columns = new ArrayList();
         List<Column> criteria = new ArrayList();
         List<String> privates = new ArrayList();
         List<Column> exports = new ArrayList();
         List<Column> searchColumns = new ArrayList();
         Map<QObject, QLabel> buddies = this.extractBuddies(win);
         HashMap<String, String> joinCriteria = new HashMap();
         List children = win.findChildren();
         for (int i=0; i<children.size(); i++){
             QObject child = (QObject) children.get(i);
             Column column=null;
             Object columnProperty = child.property("column");
             Object entityProperty = child.property("entity");
             if (columnProperty != null){
                 String lookupPropertyName=null;
                 String columnPropertyName = this.capitalize((String) columnProperty);
                 Object lookupProperty = child.property("lookup");
                 if ( lookupProperty != null){
                     lookupPropertyName = this.capitalize((String) lookupProperty);
                 }
                 String label=columnPropertyName;
                 String description=columnPropertyName;
                 if( buddies.keySet().contains(child) ){
                     label = buddies.get(child).text();                    
                     if( buddies.get(child).toolTip() != null ){
                         description = buddies.get(child).toolTip();
                     } else {
                         description = label;
                     }
                 }
                 column = new Column(columnPropertyName, label, description,
                         lookupPropertyName);
                 columns.add(column);
                 // Reg Exp validator
                 Object validatorProperty = child.property("validator");
                 if( validatorProperty != null){
                     behavior.setReValidator(columnPropertyName, (String) validatorProperty);
                 }
             }
             if (entityProperty != null){
                 String entityPropertyName = this.capitalize((String) entityProperty);
                 String label=entityPropertyName;
                 String description=entityPropertyName;
                 if( buddies.keySet().contains(child) ){
                     label = buddies.get(child).text();                    
                     if( buddies.get(child).toolTip() != null ){
                         description = buddies.get(child).toolTip();
                     } else {
                         description = label;
                     }
                 }
                 column = new Column(entityPropertyName, label, description);
                 columns.add(column);
             }
             // search dynamic property
             Object searchProperty = child.property("search");
             if (searchProperty != null){
                 if ((Boolean) searchProperty){
                     if( PyPaPiTableView.class.isInstance(child) ){
                         Object searchfieldsProperty = child.property("searchfields");
                         if( searchfieldsProperty != null ){
                             String searchfields = (String) searchfieldsProperty;
                             joinCriteria.put((String) entityProperty, searchfields);
                         }
                     } else {
                         if(QLineEdit.class.isInstance(child)||QTextEdit.class.isInstance(child)){
                             column.setEditorType(CellEditorType.STRING);
                         } else if(QCheckBox.class.isInstance(child)){
                             column.setEditorType(CellEditorType.BOOLEAN);
                         } else if(QSpinBox.class.isInstance(child)){
                            column.setEditorType(CellEditorType.INTEGER);
                         } else if(QDateTimeEdit.class.isInstance(child)){
                             column.setEditorType(CellEditorType.DATE);
                         } else if(QComboBox.class.isInstance(child)){
                             column.setEditorType(CellEditorType.CHOICE);
                         } else {
                             column.setEditorType(CellEditorType.UNKNOW);
                         }
                         criteria.add(column);
                     }
                 }
             }
             // private dynamic property
             Object privateProperty = child.property("private");
             if (privateProperty != null){
                 if ((Boolean) privateProperty){
                     if (entityProperty != null){
                         String entityPropertyName = this.capitalize((String) entityProperty);
                         privates.add(entityPropertyName);
                     } else if( column != null ){
                         privates.add(column.getName());
                     }
                 }
             }
             // private dynamic property
             Object exportProperty = child.property("export");
             if (exportProperty != null){
                 if ((Boolean) exportProperty){
                     exports.add(column);
                 }
             }
         }
         // search columns and order
         Object searchColumnsProperty = win.property("searchcolumns");
         if (searchColumnsProperty != null){
             String[] columnNames = ((String) searchColumnsProperty).split(",");
             for(String name: columnNames){
                 QHeaderView.ResizeMode resizeMode = Util.extractResizeMode(name);
                 name = Util.cleanColumnName(name);
                 name = this.capitalize(name);
                 Column searchColumn=null;
                 for( Column column: columns ){
                     if( name.equals(column.getName()) ){
                         searchColumn = column;
                         break;
                     }
                 }
                 if( searchColumn != null ){
                     searchColumns.add(searchColumn);
                 } else {
                     // it's a fake column
                     searchColumns.add(new Column(name, name, name, null, resizeMode.value()));
                 }
             }
         }
         Object sortOrderProperty = win.property("sortorder");
         if( sortOrderProperty != null ){
             String sortOrder = (String) sortOrderProperty;
             if( sortOrder.startsWith("-") || sortOrder.startsWith(">") ){
                 behavior.setSortOrder(-1);
             } else if( sortOrder.startsWith("+") || sortOrder.startsWith("<") ) {
                 behavior.setSortOrder(+1);
             }
         }
         Object sortColumnProperty = win.property("sortcolumn");
         if( sortColumnProperty != null ){
             Integer sortColumn = (Integer) sortColumnProperty;
             behavior.setSortColumn(sortColumn);
         }
         behavior.setColumns(columns);
         behavior.setCriteria(criteria);
         behavior.setPrivates(privates);
         behavior.setExports(exports);
         behavior.setSearchColumns(searchColumns);
         behavior.setJoinCriteria(joinCriteria);
         Register.registerUtility(behavior, IEntityBehavior.class, this.entityClassName);
     }
     
     private String capitalize(String s) {
         return s.substring(0,1).toUpperCase() + s.substring(1);
     }
     
 }
