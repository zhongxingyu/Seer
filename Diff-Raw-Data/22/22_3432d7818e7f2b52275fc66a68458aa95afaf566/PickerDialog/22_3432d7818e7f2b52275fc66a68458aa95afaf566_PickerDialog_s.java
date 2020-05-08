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
 import com.axiastudio.pypapi.db.Controller;
 import com.axiastudio.pypapi.db.IController;
 import com.axiastudio.pypapi.db.Store;
 import com.axiastudio.pypapi.ui.widgets.PyPaPiDateEdit;
 import com.trolltech.qt.core.*;
 import com.trolltech.qt.core.Qt.CheckState;
 import com.trolltech.qt.gui.*;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.GroupLayout;
 
 
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 class EntitySelectorUtility {
     private final Class klass;
     private final QWidget parent;
     private Object selected=null;
     private final QLabel label;
     private final QToolButton clearButton;
     
     public EntitySelectorUtility(Class entityClass, QWidget parent, QLabel label, QToolButton clearButton){
         this.klass = entityClass;
         this.parent = parent;
         this.label = label;
         this.clearButton = clearButton;
     }
     
     public Object select(){
         Controller controller = (Controller) Register.queryUtility(IController.class, this.klass.getName(), true);
         PickerDialog pd = new PickerDialog(this.parent, controller);
         int res = pd.exec();
         Object entity = null;
         if ( res == 1 ){
             for( int i=0; i<pd.getSelection().size(); i++ ){
                 entity = pd.getSelection().get(i);
                 this.selected = entity;
                 this.label.setText(entity.toString());
                 this.clearButton.setEnabled(true);
             }
         }
         return entity;
     }
     
     public void clear(){
         this.selected = null;
         this.label.setText("-");
         this.clearButton.setEnabled(false);
     }
 
     public Object getSelected() {
         return selected;
     }
     
     
 }
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public class PickerDialog extends QDialog {
     
     private final String STYLE="QLineEdit {"
             + "image: none;"
             + "}";
 
     private List selection = new ArrayList();;
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
     private QToolButton buttonExport;
     private QToolButton buttonQuickInsert;
     private Boolean isCriteria;
     private HashMap<Column,QWidget> criteriaWidgets = new HashMap();
     private HashMap<String, QWidget> joinCriteriaWidgets = new HashMap();
     private HashMap<Column, Object> filters = new HashMap();
     private HashMap<String, EntitySelectorUtility> entitySelectorUtilities = new HashMap();
     private QComboBox comboBoxLimit;
     private List<Column> mergedCriteria;
 
     public PickerDialog(Controller controller) {
         this(null, controller);
     }
     
     public PickerDialog(QWidget parent, Controller controller) {
         super(parent);
         this.controller = controller;
         this.init();
         this.buttonQuickInsert.setEnabled(false);
         EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.controller.getClassName());
         List<Column> criteria = behavior.getCriteria();
         HashMap<String, String> joinCriteria = behavior.getJoinCriteria();
         this.isCriteria = false;
         if( (criteria == null || criteria.size() == 0) &&
             (joinCriteria == null || joinCriteria.keySet().size() == 0) ){
             this.executeSearch();
             this.buttonSearch.setEnabled(false);
         } else {
             this.addCriteria(criteria, joinCriteria);
             this.buttonAccept.setEnabled(false);
             this.buttonExport.setEnabled(false);
             this.isCriteria = true;            
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
 
     
     @Override
     public void accept() {
         this.disposeAll();
         super.accept();
     }
 
     @Override
     public void reject() {
         this.disposeAll();
         super.reject();
     }
     
     private void disposeAll(){
         if( this.tableView.model() != null ){
             this.selectionModel.disposeLater();
             this.tableView.model().disposeLater();
             this.tableView.setModel(null);
         }
     }
 
     private void init(){
         this.setWindowTitle(tr("RESEARCH_AND_SELECTION"));
         this.layout = new QVBoxLayout(this);
         this.layout.setSpacing(4);
         this.tableView = new QTableView();
         this.tableView.setSizePolicy(new QSizePolicy(QSizePolicy.Policy.Expanding,
                 QSizePolicy.Policy.Expanding));
         this.tableView.setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
         this.tableView.setMinimumHeight(250);
         this.tableView.setMinimumWidth(800);
         this.tableView.setSortingEnabled(true);
         this.tableView.installEventFilter(this);
         this.layout.addWidget(this.tableView, 1);
         this.filterLineEdit = new QLineEdit();
         this.filterLineEdit.textChanged.connect(this, "applyFilter(String)");
         QLabel filterLabel = new QLabel();
         filterLabel.setPixmap(new QPixmap("classpath:com/axiastudio/pypapi/ui/resources/toolbar/find.png"));
         this.searchLogLabel = new QLabel();
         this.buttonSearch = new QToolButton(this);
         this.buttonSearch.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/filter.png"));
         this.buttonSearch.clicked.connect(this, "executeSearch()");
         this.buttonSearch.setShortcut(QKeySequence.StandardKey.Find);
         this.buttonCancel = new QToolButton(this);
         this.buttonCancel.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/cancel.png"));
         this.buttonCancel.clicked.connect(this, "reject()");
         this.buttonAccept = new QToolButton(this);
         this.buttonAccept.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/accept.png"));
         this.buttonAccept.clicked.connect(this, "accept()");
         this.tableView.doubleClicked.connect(this, "accept()");
         this.buttonExport = new QToolButton(this);
         this.buttonExport.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/export.png"));
         this.buttonExport.clicked.connect(this, "export()");
         this.buttonQuickInsert = new QToolButton(this);
         this.buttonQuickInsert.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/add.png"));
         this.buttonQuickInsert.clicked.connect(this, "quickInsert()");
         this.comboBoxLimit = new QComboBox(this);
         this.comboBoxLimit.addItem("10");
         this.comboBoxLimit.addItem("100");
         this.comboBoxLimit.addItem("1000");
         this.comboBoxLimit.addItem("no limit");
         this.comboBoxLimit.setCurrentIndex(2);
         QHBoxLayout buttonLayout = new QHBoxLayout();
         buttonLayout.setSpacing(4);
         buttonLayout.addWidget(this.filterLineEdit);
         buttonLayout.addWidget(filterLabel);
         QSpacerItem spacer = new QSpacerItem(20, 20, QSizePolicy.Policy.Expanding,
                 QSizePolicy.Policy.Minimum);
         buttonLayout.addItem(spacer);
         buttonLayout.addWidget(new QLabel("max"));
         buttonLayout.addWidget(this.comboBoxLimit);
         buttonLayout.addWidget(this.buttonSearch);
         buttonLayout.addWidget(this.buttonExport);
         QSpacerItem spacer2 = new QSpacerItem(20, 20, QSizePolicy.Policy.Minimum,
                 QSizePolicy.Policy.Minimum);
         buttonLayout.addItem(spacer2);
         buttonLayout.addWidget(this.buttonQuickInsert);
         QSpacerItem spacer3 = new QSpacerItem(40, 20, QSizePolicy.Policy.Minimum,
                 QSizePolicy.Policy.Minimum);
         buttonLayout.addItem(spacer3);
         buttonLayout.addWidget(this.buttonCancel);
         buttonLayout.addWidget(this.buttonAccept);
         this.layout.addLayout(buttonLayout);
         this.resize(500, 300);
     }
     
     private void addCriteria(List<Column> criteria, HashMap<String, String> joinCriteria){
         QGridLayout grid = new QGridLayout();
         Map<String, Class> joinClassMap = new HashMap();
         mergedCriteria = new ArrayList();
         mergedCriteria.addAll(criteria);
         for( String field: joinCriteria.keySet() ){
             String[] split = joinCriteria.get(field).split(",");
             for( String single: split ){
                 String fields = field + "." + single;
                 fields = fields.substring(1); 
                 String[] tkns = fields.split("\\.");
                 String label = tkns[tkns.length-1];
                 Class klass = controller.getEntityClass();
                 for( String tkn: tkns ){
                     Method method = Resolver.getterFromFieldName(klass, tkn);
                     Class newClass = method.getReturnType();
                     if( Collection.class.isAssignableFrom(newClass) ){
                         klass = Resolver.collectionClassFromReference(klass, tkn);
                     } else {
                         klass = newClass;
                     }
                 }
                 label = label.substring(0,1).toUpperCase() + label.substring(1);
                 Column column = new Column(fields, label, label);
                 joinClassMap.put(fields, klass);
                 if( String.class.isAssignableFrom(klass) ){
                     column.setEditorType(CellEditorType.STRING);
                 } else if( Enum.class.isAssignableFrom(klass) ) {
                     column.setEditorType(CellEditorType.CHOICE);
                 } else if( Boolean.class.isAssignableFrom(klass) ) {
                     column.setEditorType(CellEditorType.BOOLEAN);
                 } else if( Long.class.isAssignableFrom(klass) ) {
                     column.setEditorType(CellEditorType.LONG);
                 } else if( Integer.class.isAssignableFrom(klass) ) {
                     column.setEditorType(CellEditorType.INTEGER);
                 } else if( Date.class.isAssignableFrom(klass) ) {
                     column.setEditorType(CellEditorType.DATE);
                 } else {
                     column.setEditorType(CellEditorType.CHOICE);
                 }
                 if( column.getEditorType() != null ){
                     mergedCriteria.add(column);
                 }
             }
         }
         int i=-1;
         String currentFrame = null;
         for (int j=0; j<mergedCriteria.size(); j++){
             i++;
             Column column = mergedCriteria.get(j);
             if( column.getName().contains(".") ){
                 String token = column.getName().split("\\.")[0];
                 if( currentFrame==null || !currentFrame.equals(token) ){
                     QFrame frame = new QFrame();
                     frame.setFrameShape(QFrame.Shape.HLine);
                     grid.addWidget(frame, i, 0, 1, 2);
                     i++;
                     QLabel groupLabel = new QLabel();
                     String labelText;
                     if( token.endsWith("Collection") ){
                         labelText = token.substring(0,1).toUpperCase()+token.substring(1, token.length()-10);
                     } else {
                         labelText = token.substring(0,1).toUpperCase()+token.substring(1);
                     }
                     groupLabel.setText(labelText);
                     groupLabel.setAlignment(Qt.AlignmentFlag.createQFlags(Qt.AlignmentFlag.AlignHCenter));
                     grid.addWidget(groupLabel, i, 0, 1, 2);
                     i++;
                     currentFrame = token;
                 }
             }
             QLabel criteriaLabel = new QLabel(column.getLabel());
             grid.addWidget(criteriaLabel, i, 0);
             QHBoxLayout criteriaLayout = new QHBoxLayout();
             QWidget widget=null;
             if( column.getEditorType().equals(CellEditorType.STRING) ){
                 widget = new QLineEdit();
             } else if( column.getEditorType().equals(CellEditorType.BOOLEAN) ){
                 widget = new QCheckBox();
                 ((QCheckBox) widget).setTristate(true);
                 ((QCheckBox) widget).setCheckState(CheckState.PartiallyChecked);
             } else if( column.getEditorType().equals(CellEditorType.INTEGER) || column.getEditorType().equals(CellEditorType.LONG) ){
                 widget = new QSpinBox();
                 ((QSpinBox) widget).setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
             } else if( column.getEditorType().equals(CellEditorType.DATE) ){
                 widget = new QWidget();
                 QHBoxLayout hbox= new QHBoxLayout();
                 PyPaPiDateEdit dateEdit = new PyPaPiDateEdit();
                 dateEdit.setCalendarPopup(true);
                 dateEdit.setDate(dateEdit.minimumDate());
                 hbox.addWidget(dateEdit);
                 QComboBox comboBox1 = new QComboBox();
                 comboBox1.addItem("1");
                 comboBox1.addItem("2");
                 comboBox1.addItem("3");
                 comboBox1.addItem("4");
                 comboBox1.addItem("5");
                 comboBox1.addItem("6");
                 hbox.addWidget(comboBox1);
                 QComboBox comboBox2 = new QComboBox();
                 comboBox2.addItem(tr("DAYS"));
                 comboBox2.addItem(tr("WEEKS"));
                 comboBox2.addItem(tr("MONTHS"));
                 comboBox2.addItem(tr("YEARS"));
                 hbox.addWidget(comboBox2);
                 widget.setLayout(hbox);
             } else if( column.getEditorType().equals(CellEditorType.CHOICE) || column.getEditorType().equals(CellEditorType.LOOKUP) ){
                 Class entityClass=null;
                 if( !column.getName().contains(".") ){
                     Class klass = this.controller.getEntityClass();
                     entityClass = Resolver.entityClassFromReference(klass, column.getName());
                 } else {
                     entityClass = joinClassMap.get(column.getName());
                 }
                 if( entityClass.isEnum() ){
                     widget = new QComboBox();
                     for( Object object:  entityClass.getEnumConstants() ){
                         String key = object.toString();
                         ((QComboBox) widget).addItem(key, object);
                     }
                     ((QComboBox) widget).addItem("-", null);
                     ((QComboBox) widget).setCurrentIndex(((QComboBox) widget).count()-1);
                 } else {
                     widget = new QWidget();
                     QHBoxLayout hbox= new QHBoxLayout();
                     hbox.setContentsMargins(4, 4, 4, 4);
                     QLabel label = new QLabel(tr("SELECT"));
                     hbox.addWidget(label);
                     QToolButton button = new QToolButton();
                     button.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/find.png"));
                     hbox.addWidget(button);
                     QToolButton button2 = new QToolButton();
                     button2.setEnabled(false);
                     button2.setIcon(new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/cancel.png"));
                     hbox.addWidget(button2);
                     EntitySelectorUtility esu = new EntitySelectorUtility(entityClass, this, label, button2);
                     this.entitySelectorUtilities.put(column.getName(), esu);
                     button.clicked.connect(esu, "select()");
                     button2.clicked.connect(esu, "clear()");
                     widget.setLayout(hbox);
                 }
             }
             if( widget != null ){
                 this.criteriaWidgets.put(column, widget);
                 criteriaLayout.addWidget(widget);            
                 grid.addLayout(criteriaLayout, i, 1);
             }
         }
         this.layout.addLayout(grid);
     }        
     
     public void addFilter(Column column, Object value){
         if( value instanceof String ){
             // Differential strategy
             String stringValue = (String) value;
             if( "true".equals(stringValue) ){
                 column.setEditorType(CellEditorType.BOOLEAN);
                 this.filters.put(column, true);
             } else if( "false".equals(stringValue) ){
                 column.setEditorType(CellEditorType.BOOLEAN);
                 this.filters.put(column, false);
             }
         } else {
             column.setEditorType(CellEditorType.STRING);
             this.filters.put(column, value);
         }
     }
     
     public final void executeSearch(){
         Store supersetStore=null;
         EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.controller.getClassName());
         List<Column> columns = behavior.getSearchColumns();
         if (!this.isCriteria){
             supersetStore = this.controller.createFullStore();
         } else {
             HashMap criteriaMap = new HashMap();
             for (Column column: mergedCriteria){
                 QWidget widget = (QWidget) this.criteriaWidgets.get(column);
                 if( column.getEditorType().equals(CellEditorType.STRING) ){
                     String value = ((QLineEdit) widget).text();
                     if (!"".equals(value)){
                         criteriaMap.put(column, value);
                     }
                 } else if ( column.getEditorType().equals(CellEditorType.BOOLEAN) ){
                     CheckState checkState = ((QCheckBox) widget).checkState();
                     if( !checkState.equals(CheckState.PartiallyChecked) ){
                         Boolean state = checkState.equals(CheckState.Checked) && true || false;
                         criteriaMap.put(column, state);
                     }
                 } else if ( column.getEditorType().equals(CellEditorType.INTEGER) ){
                     Integer value = ((QSpinBox) widget).value();
                     if( value != 0 ){
                         criteriaMap.put(column, value);
                     }
                 } else if ( column.getEditorType().equals(CellEditorType.LONG) ){
                     Long value = ((Integer) ((QSpinBox) widget).value()).longValue();
                     if( value != 0 ){
                         criteriaMap.put(column, value);
                     }
                 } else if ( column.getEditorType().equals(CellEditorType.DATE) ){
                     PyPaPiDateEdit dateEdit = (PyPaPiDateEdit) widget.layout().itemAt(0).widget();
                     QComboBox comboBox1 = (QComboBox) widget.layout().itemAt(1).widget();
                     QComboBox comboBox2 = (QComboBox) widget.layout().itemAt(2).widget();
                     QDate date = dateEdit.date();
                     if( !date.equals(dateEdit.minimumDate()) ){
                         Integer n = comboBox1.currentIndex() + 1;
                         Integer idx = comboBox2.currentIndex();
                         Integer days = null;
                         if( idx == 0 ) {
                             days = n;
                         } else if( idx == 1 ) {
                             days = 7 * n;
                         } else if( idx == 2 ) {
                             days = 30 * n; // XXX
                         } else if( idx == 3 ) {
                             days = 365 * n; // XXX
                         }
                         int year = date.year();
                         int month = date.month();
                         int day = date.day();
                         GregorianCalendar gc = new GregorianCalendar(date.year(),
                                                         date.month()-1, date.day());
                         List values = new ArrayList();
                         values.add(gc);
                         values.add(days);
                         criteriaMap.put(column, values);
                     }
                 } else if ( column.getEditorType().equals(CellEditorType.CHOICE) || column.getEditorType().equals(CellEditorType.LOOKUP) ){
                     Object data;
                     if( widget.getClass().equals(QComboBox.class )){
                         int idx = ((QComboBox) widget).currentIndex();
                         data = ((QComboBox) widget).itemData(idx);
                     } else {
                         data = this.entitySelectorUtilities.get(column.getName()).getSelected();
                     }
                     if( data != null ){
                         criteriaMap.put(column, data);
                     }
                 }
             }
             criteriaMap.putAll(this.filters);
             Integer limit = 0;
             if( this.comboBoxLimit.currentIndex() != this.comboBoxLimit.count()-1 ){
                 limit = Integer.parseInt(this.comboBoxLimit.currentText());
             }
             supersetStore = this.controller.createCriteriaStore(criteriaMap, limit);
         }
         TableModel model = new TableModel(supersetStore, columns);
         model.setEditable(false);
         ProxyModel proxy = new ProxyModel();
         proxy.setSourceModel(model);
         this.tableView.setModel(proxy);
             Integer sortColumn = behavior.getSortColumn();
             if( sortColumn != null ){
                 Qt.SortOrder sortOrder;
                 if( behavior.getSortOrder() == -1 ){
                     this.tableView.sortByColumn(sortColumn, Qt.SortOrder.DescendingOrder);
                 } else {
                     this.tableView.sortByColumn(sortColumn, Qt.SortOrder.AscendingOrder);
                 }
             }
         this.selectionModel = new QItemSelectionModel(proxy);
         this.tableView.setSelectionModel(this.selectionModel);
         this.selectionModel.selectionChanged.connect(this,
                 "selectRows(QItemSelection, QItemSelection)");
         this.buttonQuickInsert.setEnabled(true);
     }
     
     public final void export(){
         EntityBehavior behavior = (EntityBehavior) Register.queryUtility(IEntityBehavior.class, this.controller.getClassName());
         List<Column> columns = behavior.getExports();
         String content = Util.exportToCvs(this.selection, columns, this.controller.getEntityClass());
         if( content == null ){
             QMessageBox.information(this, tr("EXPORT_ERROR"), tr("EXPORT_ERROR_MESSAGE"));
             return;
         }
         String saveFileName = QFileDialog.getSaveFileName(this, tr("EXPORT_CSV_FILE"), ".", new QFileDialog.Filter("CSV file (*.csv)"));
         if( saveFileName != null ){
             QFile csvFile = new QFile(saveFileName);
             if( csvFile.open(new QFile.OpenMode(QFile.OpenModeFlag.WriteOnly, QFile.OpenModeFlag.Unbuffered)) ){
                 if( csvFile.write(new QByteArray(content)) > 0 ){
                     csvFile.close();
                     Util.informationBox(this, tr("CSV_EXPORTED"), tr("CSV_EXPORTED_DESCRIPTION"));
                 }
             }
         }
     }
     
     private void quickInsert(){
         try {
             Class klass = (Class) Register.queryUtility(IQuickInsertDialog.class, this.controller.getClassName());
             IQuickInsertDialog form = (IQuickInsertDialog) klass.getDeclaredConstructor(QWidget.class).newInstance(this);
             int res = form.exec();
             if( res == 1 ){
                 this.selection.clear();
                 this.selection.add(form.getEntity());
                 this.accept();
             }
         } catch (IllegalAccessException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         }catch (NoSuchMethodException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         }catch (SecurityException ex) {
             Logger.getLogger(PickerDialog.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private void applyFilter(String text){
        TableModel model = (TableModel) this.tableView.model();
         if(text.length()==0){
             for(int i=0; i<model.rowCount(); i++){
                 this.tableView.setRowHidden(i, false);
             }
         } else {
             for(int i=0; i<model.rowCount(); i++){
                 Boolean toHide=true;
                 for(int j=0; j<model.columnCount(); j++){
                    if( toHide==true && model.data(i, j).toString().toUpperCase().contains(text.toUpperCase()) ){
                        toHide=false;
                    }
                 }
                 this.tableView.setRowHidden(i, toHide);
             }
         }
     }
     
     private void selectRows(QItemSelection selected, QItemSelection deselected){
         ITableModel model = (ITableModel) this.tableView.model();
         List<Integer> selectedIndexes = new ArrayList();
         List<Integer> deselectedIndexes = new ArrayList();
         for (QModelIndex i: selected.indexes()){
            System.out.println("selezione: "+i.row());
             if( model instanceof ProxyModel ){ 
                 i = ((ProxyModel) model).mapToSource(i);
                System.out.println("trasformato: "+i.row());
             }
             if(!selectedIndexes.contains(i.row())){
                 selectedIndexes.add(i.row());
             }
         }
         for (QModelIndex i: deselected.indexes()){
            System.out.println("de-selezione: "+i.row());
             if( model instanceof ProxyModel ){ 
                 i = ((ProxyModel) model).mapToSource(i);
                System.out.println("trasformato: "+i.row());
             }
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
         Boolean isSelection = this.selection.size()>0;
         this.buttonAccept.setEnabled(isSelection);
         this.buttonExport.setEnabled(isSelection);
        System.out.println(selection);
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
