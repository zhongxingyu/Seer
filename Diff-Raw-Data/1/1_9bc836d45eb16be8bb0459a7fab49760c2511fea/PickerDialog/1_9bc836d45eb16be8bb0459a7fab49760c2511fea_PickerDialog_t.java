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
 import com.axiastudio.pypapi.db.Controller;
 import com.axiastudio.pypapi.db.Store;
 import com.trolltech.qt.core.QEvent;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class PickerDialog extends QDialog {
     
     private final String STYLE="QLineEdit {"
             + "image: none;"
             + "}";
 
     private List selection;
     private QTableView tableView;
     private QItemSelectionModel selectionModel;
     private QLineEdit filterLineEdit;
     private QLabel searchLogLabel;
     private Store store;
     private Controller controller;
     private QVBoxLayout layout;
     private QToolButton buttonSearch;
     private QToolButton buttonCancel;
     private QToolButton buttonAccept;
     private Boolean isCriteria;
     private HashMap criteriaWidgets;
 
     public PickerDialog(Controller controller) {
         this(null, controller);
     }
     
     public PickerDialog(QWidget parent, Controller controller) {
         super(parent);
         this.controller = controller;
         this.selection = new ArrayList();
         this.criteriaWidgets = new HashMap();
         this.init();
         EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.controller.getClassName());
         List<Column> criteria = behavior.getCriteria();
         this.isCriteria = false;
         if (criteria != null){
             if (criteria.size()>0){
                 this.addCriteria(criteria);
                 this.buttonAccept.setEnabled(false);
                 this.isCriteria = true;
             }
         } else {
             this.executeSearch();
             this.buttonSearch.setEnabled(false);
         }
         this.tableView.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.ResizeToContents);
         this.tableView.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
         this.tableView.setSortingEnabled(true);
         for( int i=0; i<behavior.getSearchColumns().size(); i++ ){
             Column c = behavior.getSearchColumns().get(i);
             this.tableView.horizontalHeader().setResizeMode(i, QHeaderView.ResizeMode.resolve(c.getResizeModeValue()));
         }
         this.setStyleSheet(this.STYLE);
     }
     
     private void init(){
         this.setWindowTitle("Research and selection");
         this.layout = new QVBoxLayout(this);
         this.layout.setSpacing(4);
         this.tableView = new QTableView();
         this.tableView.setSizePolicy(new QSizePolicy(QSizePolicy.Policy.Expanding,
                 QSizePolicy.Policy.Expanding));
         this.tableView.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
         this.tableView.setMinimumHeight(150);
         this.tableView.setSortingEnabled(true);
         this.tableView.installEventFilter(this);
         this.layout.addWidget(this.tableView, 1);
         this.filterLineEdit = new QLineEdit();
         QLabel filterLabel = new QLabel();
         filterLabel.setPixmap(new QPixmap("classpath:com/axiastudio/pypapi/ui/resources/toolbar/find.png"));
         this.searchLogLabel = new QLabel();
         this.buttonSearch = new QToolButton(this);
         this.buttonSearch.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/key.png"));
         this.buttonSearch.clicked.connect(this, "executeSearch()");
         this.buttonSearch.setShortcut(QKeySequence.StandardKey.Find);
         this.buttonCancel = new QToolButton(this);
         this.buttonCancel.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/cancel.png"));
         this.buttonCancel.clicked.connect(this, "reject()");
         this.buttonAccept = new QToolButton(this);
         this.buttonAccept.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/accept.png"));
         this.buttonAccept.clicked.connect(this, "accept()");
        this.tableView.doubleClicked.connect(this, "accept()");
         QHBoxLayout buttonLayout = new QHBoxLayout();
         buttonLayout.setSpacing(4);
         buttonLayout.addWidget(this.filterLineEdit);
         buttonLayout.addWidget(filterLabel);
         QSpacerItem spacer = new QSpacerItem(40, 20, QSizePolicy.Policy.Expanding,
                 QSizePolicy.Policy.Minimum);
         buttonLayout.addItem(spacer);
         buttonLayout.addWidget(this.buttonSearch);
         QSpacerItem spacer2 = new QSpacerItem(40, 20, QSizePolicy.Policy.Minimum,
                 QSizePolicy.Policy.Minimum);
         buttonLayout.addItem(spacer2);
         buttonLayout.addWidget(this.buttonCancel);
         buttonLayout.addWidget(this.buttonAccept);
         this.layout.addLayout(buttonLayout);
         this.resize(500, 300);
     }
     
     private void addCriteria(List<Column> criteria){
         QGridLayout grid = new QGridLayout();
         for (int i=0; i<criteria.size(); i++){
             Column c = criteria.get(i);
             QLabel criteriaLabel = new QLabel(c.getLabel());
             grid.addWidget(criteriaLabel, i, 0);
             QHBoxLayout criteriaLayout = new QHBoxLayout();
             // TODO: different types of search widget depending on the data type
             QLineEdit line = new QLineEdit();
             this.criteriaWidgets.put(c, line);
             criteriaLayout.addWidget(line);
             
             grid.addLayout(criteriaLayout, i, 1);
         }
         this.layout.addLayout(grid);
     }        
     
     public final void executeSearch(){
         Store supersetStore=null;
         EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.controller.getClassName());
         List<Column> columns = behavior.getSearchColumns();
         if (!this.isCriteria){
             supersetStore = this.controller.createFullStore();
         } else {
             List<Column> criteria = behavior.getCriteria();
             HashMap criteriaMap = new HashMap();
             for (Column criteriaColumn: criteria){
                 QWidget widget = (QWidget) this.criteriaWidgets.get(criteriaColumn);
                 // TODO: criteria with widgets other than QLIneEdit
                 if (widget.getClass() == QLineEdit.class){
                     String value = ((QLineEdit) widget).text();
                     if (!"".equals(value)){
                         criteriaMap.put(criteriaColumn, value);
                     }
                 }
             }
             supersetStore = this.controller.createCriteriaStore(criteriaMap);
         }
         TableModel model = new TableModel(supersetStore, columns);
         model.setEditable(false);
         this.tableView.setModel(model);
         this.selectionModel = new QItemSelectionModel(model);
         this.tableView.setSelectionModel(this.selectionModel);
         this.selectionModel.selectionChanged.connect(this,
                 "selectRows(QItemSelection, QItemSelection)");
         
     }
     
     private void selectRows(QItemSelection selected, QItemSelection deselected){
         TableModel model = (TableModel) this.tableView.model();
         List<Integer> selectedIndexes = new ArrayList();
         List<Integer> deselectedIndexes = new ArrayList();
         for (QModelIndex i: selected.indexes()){
             if(!selectedIndexes.contains(i.row())){
                 selectedIndexes.add(i.row());
             }
         }
         for (QModelIndex i: deselected.indexes()){
             if(!deselectedIndexes.contains(i.row())){
                 deselectedIndexes.add(i.row());
             }
         }
         for (Integer idx: selectedIndexes){
             boolean res = this.selection.add(model.getEntityByRow(idx));
         }
         for (Integer idx: deselectedIndexes){
             boolean res = this.selection.remove(model.getEntityByRow(idx));
         }
         this.buttonAccept.setEnabled(this.selection.size()>0);
 
     }
     
     public List getSelection() {
         return selection;
     }
 
     @Override
     public boolean eventFilter(QObject qo, QEvent qevent) {
         if( qevent.type() == QEvent.Type.KeyPress ){
             QKeyEvent qke = (QKeyEvent) qevent;
             if( qke.key() == Qt.Key.Key_Return.value() ){
                 if( this.getSelection().size() > 0 ){
                     this.accept();
                 }
             }
         }
         return false;
     }
     
 
 }
