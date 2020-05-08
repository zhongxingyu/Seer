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
 import com.axiastudio.pypapi.Resolver;
 import com.axiastudio.pypapi.db.*;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.gui.QDataWidgetMapper;
 import com.trolltech.qt.gui.QMainWindow;
 import com.trolltech.qt.gui.QWidget;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public final class Context extends QObject {
 
     private TableModel model;
     private QDataWidgetMapper mapper;
     private Class rootClass;
     private String name;
     private Object currentEntity;
     private Boolean isDirty;
     private Boolean atBof=true;
     private Boolean atEof=true;
     private Boolean noDelete=false;
     private Boolean noInsert=false;
     private Boolean readOnly =false;
     private Context primaryDc;
     private IForm parent;
 
     Controller controller;
 
     public Context(IForm parent, Class rootClass, String name, List columns){
         this(parent, rootClass, name, columns, null);
     }
 
     public Context(IForm parent, Class rootClass, String name, List columns, Store store){
         //Logger.getLogger(Context.class.getName()).log(Level.INFO, "Create {0} context", rootClass.toString()+name);
         this.parent = parent;
         this.rootClass = rootClass;
         this.name = name;
        store = initializeController(store);
 
         this.model = this.createModel(store);
         this.mapper = new QDataWidgetMapper(this.parent());
         this.mapper.setSubmitPolicy(QDataWidgetMapper.SubmitPolicy.AutoSubmit);
         this.mapper.setModel(this.model);
         this.model.setColumns(columns);
         this.initializeContext();
         this.isDirty = false;
         if( ".".equals(this.name) && this.model.getStore().isEmpty() ){
             this.insertElement();
         }
     }
 
     public Class getRootClass() {
         return rootClass;
     }
 
     public Controller getController() {
         return controller;
     }
 
    private Store initializeController(Store store) {
         Database db = (Database) Register.queryUtility(IDatabase.class);
         controller = db.createController(rootClass);
         if( store != null ){
            if( store.size() == 1 ){
                Object entity = store.get(0);
                Long id = controller.getId(entity);
                if( id != null && !controller.getEntityManager().contains(entity) ) {
                    Object newEntity = controller.get(id);
                    store = new Store(new ArrayList());
                    store.add(newEntity);
                }
            }
             for( Object obj: store ){
                 if( obj.hashCode() != 0 ){
                     controller.getEntityManager().merge(obj);
                 }
             }
         }
        return store;
     }
 
     private void initializeContext(){
         this.mapper.currentIndexChanged.connect(this, "indexChanged(int)");
         if(".".equals(this.name)){
             this.primaryDc = this;
             this.model.dataChanged.connect(primaryDc, "modelDataChanged(QModelIndex, QModelIndex)");
             this.model.rowsRemoved.connect(primaryDc, "modelRowsRemovedChanged(QModelIndex, int, int)");
             this.model.rowsInserted.connect(primaryDc, "modelRowsInsertedChanged(QModelIndex, int, int)");
         } else {
             this.primaryDc = (Context) Register.queryRelation((QWidget) this.parent, ".");
             this.primaryDc.mapper.currentIndexChanged.connect(this, "parentIndexChanged(int)");
             this.model.dataChanged.connect(this.primaryDc.model.dataChanged);
             this.model.rowsInserted.connect(this.primaryDc.model.rowsInserted);
             this.model.rowsRemoved.connect(this.primaryDc.model.rowsRemoved);
         }
     }
 
 
     private TableModel createModel(Store store){
         TableModel tableModel;
 
         /* resolve entity class */
         if(".".equals(this.name)){
             if( store == null ){
                 store = controller.createFullStore();
             }
             tableModel = new TableModel(store, null);
         } else {
             tableModel = new TableModel(null, null);
         }
         tableModel.setContextHandle(this);
         
         return tableModel;
     }
 
     private void indexChanged(int row){
         int cnt = this.model.rowCount();
         if(".".equals(this.name)){
             if(cnt==0){
                 this.atBof = true;
                 this.atEof = true;
             } else {
                 this.atBof = (row<1);
                 this.atEof = (row>=cnt-1);
             }
         }
         if(cnt>0){
             this.currentEntity = this.model.getEntityByRow(row);
         }
     }
 
     public void refreshContext(){
         this.parentIndexChanged(0);
     }
     /*
      *  When the parent index changed, all the child contexts have to reset
      *  their stores.
      * 
      */
     private void parentIndexChanged(int row) {
         try {
             Method getter = Resolver.getterFromFieldName(this.primaryDc.currentEntity.getClass(), this.name.substring(1));
             Method setter = Resolver.setterFromFieldName(this.primaryDc.currentEntity.getClass(), this.name.substring(1), Collection.class);
             List result = (List) getter.invoke(this.primaryDc.currentEntity);
             if( result == null ){
                 result = new ArrayList();
                 setter.invoke(this.primaryDc.currentEntity, result);
             }
             Store store = new Store(result);
             this.model.setStore(store);
             this.firstElement();
         } catch (IllegalAccessException ex) {
             Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void modelDataChanged(QModelIndex topLeft, QModelIndex bottomRight){
         this.isDirty = true;
     }
 
     private void modelRowsRemovedChanged(QModelIndex topLeft, int start, int end){
         this.isDirty = true;
         this.model.dataChanged.emit(topLeft, topLeft);
     }
 
     private void modelRowsInsertedChanged(QModelIndex topLeft, int start, int end){
         this.isDirty = true;
         this.model.dataChanged.emit(topLeft, topLeft);
     }
     
     public void firstElement(){
         this.mapper.toFirst();
     }
 
     public void nextElement(){
         this.mapper.toNext();
     }
 
     public void previousElement(){
         this.mapper.toPrevious();
     }
 
     public void lastElement(){
         this.mapper.toLast();
     }
 
     public void insertElement(){
         this.insertElement(null);
     }
     
     public void insertElement(Object newEntity){
         Object entity = null;
         QModelIndex idx=null;
         if ( newEntity == null ){
             String entityName = this.rootClass.getName();
             Class cls = (Class) Register.queryUtility(IFactory.class, entityName);
             try {
                 entity = cls.newInstance();
             } catch (InstantiationException ex) {
                 Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             entity = newEntity;
         }
         List entities = new ArrayList();
         boolean add = entities.add(entity);
         this.model.insertRows(this.model.rowCount(), 1, idx, entities);
         this.mapper.toLast();
         this.isDirty = true;
     }
 
     public void deleteElement(){
         QModelIndex idx=null;
         controller.delete(this.primaryDc.currentEntity);
         int row = this.mapper.currentIndex();
         if( this.mapper.model().rowCount() > 1 ){
             if( !this.atBof ){
                 this.mapper.toPrevious();
             } else {
                 this.mapper.toNext();
             }
             this.model.removeRows(row, 1, idx);
             this.isDirty = false;
             this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
         } else {
             this.model.removeRows(row, 1, idx);
             this.insertElement();
         }
     }
 
     public void updateElement(Object entity, Integer row) {
         this.model.replaceEntity(row, entity);
         this.mapper.currentIndexChanged.emit(row);
         this.isDirty = true;
     }
 
 
     public void commitChanges(){
         Validation val = controller.commit(this.primaryDc.currentEntity);
         if( val.getResponse() == false ){
             Util.warningBox((QWidget) this.parent, "Error", val.getMessage());
             //this.refreshElement();
         } else {
             this.isDirty = false;
             this.primaryDc.currentEntity = val.getEntity();
             this.model.replaceEntity(this.mapper.currentIndex(), this.primaryDc.currentEntity);
             this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
             this.mapper.revert();
         }
     }
 
     public void cancelChanges(){
         Boolean res = Util.questionBox((QWidget) this.parent, tr("CANCEL_CHANGES_QUESTION"), tr("CANCEL_CHANGES_MESSAGE"));
         if( res ) {
             if (this.primaryDc.currentEntity.hashCode() == 0) {
                 QModelIndex idx = null;
                 int row = this.mapper.currentIndex();
                 this.mapper.toPrevious();
                 this.model.removeRows(row, 1, idx);
                 this.isDirty = false;
                 if( model.getStore().size() == 0 ){
                     insertElement();
                 }
                 this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
             } else {
                 this.refreshElement();
             }
         }
     }
     
     public void refreshElement(){
         controller.refresh(this.primaryDc.currentEntity);
         this.model.refresh(this.primaryDc.currentEntity);
         this.isDirty = false;
         this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
         this.mapper.revert();
     }
     
     public void search(){
         Boolean doSearch=true;
         if( isDirty ){
             doSearch = Util.questionBox((QWidget) this.parent, tr("CANCEL_CHANGES_QUESTION"), tr("CANCEL_CHANGES_MESSAGE"));
         }
         if( doSearch ) {
             PickerDialog pd = new PickerDialog((QWidget) this.parent, controller);
             int res = pd.exec();
             if (res == 1) {
                 this.model.replaceRows(pd.getSelection());
                 isDirty = false;
                 this.firstElement();
             }
             pd.dispose();
         }
     }
     
     public void getDirty(){
         int idx = this.mapper.currentIndex();
         QModelIndex index = this.getModel().index(idx, 0);
         this.getModel().dataChanged.emit(index, index);
         this.mapper.currentIndexChanged.emit(idx);
     }
     
     public TableModel getModel() {
         return model;
     }
 
     public QDataWidgetMapper getMapper() {
         return mapper;
     }
 
     public Object getCurrentEntity() {
         return currentEntity;
     }
 
     public Boolean getAtBof() {
         return atBof;
     }
 
     public Boolean getAtEof() {
         return atEof;
     }
 
     public Boolean getIsDirty() {
         return isDirty;
     }
 
     public Boolean getNoDelete() {
         return noDelete;
     }
 
     public void setNoDelete(Boolean noDelete) {
         this.noDelete = noDelete;
     }
 
     public Boolean getReadOnly() {
         return readOnly;
     }
 
     public void setReadOnly(Boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     public Boolean getNoInsert() {
         return noInsert;
     }
 
     public void setNoInsert(Boolean noInsert) {
         this.noInsert = noInsert;
     }
 
     public Context getPrimaryContext() {
         return primaryDc;
     }
 }
