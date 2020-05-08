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
 
 import com.axiastudio.pypapi.Application;
 import com.axiastudio.pypapi.Consts;
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.db.Store;
 import com.axiastudio.pypapi.plugins.IPlugin;
 import com.axiastudio.pypapi.ui.widgets.PyPaPiNavigationBar;
import com.axiastudio.pypapi.ui.widgets.PyPaPiTableView;
 import com.trolltech.qt.core.QByteArray;
 import com.trolltech.qt.core.QFile;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.designer.QUiLoader;
 import com.trolltech.qt.designer.QUiLoaderException;
 import com.trolltech.qt.gui.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class Window extends QMainWindow implements IForm {
 
     protected Class entityClass;
     protected String uiFile;
     protected String title;
     private Context context;
     private HashMap<String, QObject> widgets = new HashMap();
     private HashMap<String, String> stylesheets = new HashMap();
     private List<Column> columns;
     private List<Column> entities;
     private PyPaPiNavigationBar navigationBar;
     private IForm parentForm=null;
 
     public Window(String uiFile, Class entityClass) {
         this(uiFile, entityClass, "");
     }
 
     public Window(String uiFile, Class entityClass, String title) {
         this.entityClass = entityClass;
         this.uiFile = uiFile;
         //this.title = title;
         if( uiFile != null ){
             QFile qFile = new QFile(uiFile);
             if( qFile.exists() ){
                 QFile file = Util.ui2jui(qFile);
                 this.loadUi(file);
             } else {
                 Logger.getLogger(Window.class.getName()).log(Level.SEVERE, "{0} file is not present.", uiFile);
             }
         } else {
             this.autoLayout();
         }
     }
 
     @Override
     public void init(){
         this.init(null);
     }
 
     @Override
     public void init(Store store){
         FormConfigurator configurator = new FormConfigurator(this, this.entityClass);
         configurator.configure(store, true);
         // standard navigation bar
         PyPaPiNavigationBar bar = new PyPaPiNavigationBar(tr("NAVIGATION"), this);
         bar.setMovable(true);
         this.addToolBar(bar);
         this.navigationBar = bar;
         // plugins
         List<IPlugin> plugins = (List<IPlugin>) Register.queryPlugins(this.getClass());
         for( IPlugin plugin: plugins ){
             plugin.install(this);
         }
         this.context.getMapper().currentIndexChanged.connect(this, "indexChanged(int)");
         this.context.getMapper().toFirst();
         bar.refresh();
         storeInitialized.emit();
     }
 
     private void loadUi(QFile uiFile){
         QMainWindow window = null;
         try {
             window = (QMainWindow) QUiLoader.load(uiFile);
         } catch (QUiLoaderException ex) {
             Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
         }
         for( QByteArray name: window.dynamicPropertyNames()){
             this.setProperty(name.toString(), window.property(name.toString()));
         }
         this.setCentralWidget(window.centralWidget());
         this.title = window.windowTitle();
         this.setWindowTitle(this.title);
     }
 
     private void autoLayout(){
         QMainWindow window = AutoLayout.createWindowLayout(this.entityClass);
         for( QByteArray name: window.dynamicPropertyNames()){
             this.setProperty(name.toString(), window.property(name.toString()));
         }
         this.setCentralWidget(window.centralWidget());
     }
     
     protected void indexChanged(int row){
         int idx = this.context.getMapper().currentIndex() + 1;
         int tot = this.context.getModel().rowCount();
         this.setWindowTitle(this.title + " (" + idx + " of " + tot +")"); //TODO: translate
         
         Boolean isPrivate = false;
         Method privateM = (Method) Register.queryPrivate(this.entityClass);
         if( privateM != null ){
             try {
                 isPrivate = (Boolean) privateM.invoke(null, this.context.getCurrentEntity());
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalArgumentException ex) {
                 Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InvocationTargetException ex) {
                 Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
             }
             EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.context.getRootClass().getName());
             for( String columnName: behavior.getPrivates() ){
                 QWidget widget = (QWidget) this.widgets.get(columnName);
                 if( isPrivate ){
                     widget.setEnabled(false);
                     if( !this.stylesheets.containsKey(columnName) ){
                         this.stylesheets.put(columnName, widget.styleSheet());
                     }
                     widget.setStyleSheet("color: gray; background: gray");
                 } else {
                     widget.setEnabled(true);
                     if( this.stylesheets.containsKey(columnName) ){
                         widget.setStyleSheet(this.stylesheets.get(columnName));
                     }
                 }
             }
         }
     }
 
     @Override
     public Context getContext() {
         return context;
     }
     
     public void deleteElement(){
         Boolean res = Util.questionBox(this, tr("DELETE_ELEMENT_REQUEST"), tr("DELETE_ELEMENT_REQUEST_DESCRIPTION"));// "Sure you want to delete this element?");
         if( res == true ){
             this.context.deleteElement();
         }
 
     }
     
     private void information() {
         QDialog info = new QDialog(this);
         QVBoxLayout layout = new QVBoxLayout(info);
         QPixmap pix = new QPixmap("classpath:com/axiastudio/pypapi/ui/resources/pypapi64.png");
         QLabel pypapi = new QLabel();
         pypapi.setPixmap(pix);
         pypapi.setAlignment(Qt.AlignmentFlag.AlignHCenter);
         layout.addWidget(pypapi);
         String credits = "";
         Application app = Application.getApplicationInstance();
         if( app.getCustomApplicationName() != null ){
             credits += app.getCustomApplicationName() + "<br/>";
         }
         if( app.getCustomApplicationCredits() != null ){
             credits += app.getCustomApplicationCredits() + "<br/>";
         }
         credits += Consts.CREDITS;
         QTextEdit text = new QTextEdit(credits);
         layout.addWidget(text);
         info.show();
     }
     
     @Override
     public void setColumns(List<Column> columns) {
         this.columns = columns;
     }
 
     @Override
     public void setEntities(List<Column> entities) {
         this.entities = entities;
     }
 
     @Override
     public void setWidgets(HashMap<String, QObject> widgets) {
         this.widgets = widgets;
     }
 
     @Override
     public List<Column> getColumns() {
         return columns;
     }
 
     @Override
     public List<Column> getEntities() {
         return entities;
     }
 
     @Override
     public HashMap<String, QObject> getWidgets() {
         return widgets;
     }
 
     @Override
     public void setContext(Context context) {
         this.context = context;
     }
 
     @Override
     public Column getColumn(String columnName) {
         for( Column column: getColumns() ){
             if( column.getName().equals(columnName) ){
                 return column;
             }
         }
         return null;
     }
 
     @Override
     public IForm getParentForm() {
         return parentForm;
     }
 
     @Override
     public void setParentForm(IForm parentForm) {
         this.parentForm = parentForm;
     }
 
     @Override
     protected void showEvent(QShowEvent event) {
         super.showEvent(event);
         formShown.emit();
     }
 
     @Override
     protected void closeEvent(QCloseEvent event) {
         if( getContext().getIsDirty() ) {
             Boolean res = Util.questionBox(this, tr("CLOSE_CONFIRM"), tr("CLOSE_CONFIRM_MESSAGE"));
             //Boolean res = Util.questionBox(this, tr("Conferma chiusura"), "Vuoi procedere alla chiusura della finestra?\n(i dati non salvati verranno persi)");
             if( !res ){
                 event.ignore();
                 return;
             }
         }
        // unbind context from models
        for( QObject widget: widgets.values() ){
            if( widget instanceof PyPaPiTableView ){
                PyPaPiTableView pyPaPiTableView = (PyPaPiTableView) widget;
                ((ITableModel) pyPaPiTableView.model()).unbindContext();
            }
        }
        // remove relations
        Register.removeRelations(this);
        // uninstal plugins
        List<IPlugin> plugins = (List<IPlugin>) Register.queryPlugins(this.getClass());
        for( IPlugin plugin: plugins ){
            plugin.uninstall();
        }
        this.disposeLater();
         super.closeEvent(event);
     }


    /* SIGNALS */
     public Signal0 storeInitialized = new Signal0();
     public Signal0 formShown = new Signal0();
 
     
 }
