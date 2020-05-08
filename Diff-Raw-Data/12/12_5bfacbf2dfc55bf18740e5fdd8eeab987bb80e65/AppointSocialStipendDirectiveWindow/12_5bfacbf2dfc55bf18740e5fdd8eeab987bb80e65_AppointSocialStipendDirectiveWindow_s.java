 package ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components.orders;
 
 import com.extjs.gxt.ui.client.Style;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.dnd.GridDragSource;
 import com.extjs.gxt.ui.client.dnd.GridDropTarget;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.grid.*;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Element;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.mvc.events.CommonEvents;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components.NavigationPanel;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components.StudentsPanel;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model.StudentModel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: hd KhurtinDN (dog) gmail.com
  * Date: 3/14/11
  * Time: 2:47 PM
  */
 public class AppointSocialStipendDirectiveWindow extends AbstractDirectiveWindow {
     private ListStore<StudentModel> studentListStore = new ListStore<StudentModel>();
 
     public AppointSocialStipendDirectiveWindow() {
         setHeading("Назначение стипендий");
         setSize(1000, 650);
         setMaximizable(true);
     }
 
     @Override
     protected void onRender(Element parent, int pos) {
         super.onRender(parent, pos);
 
         StudentsPanel studentsPanel = new StudentsPanel(true);
         studentsPanel.setupGridDragSource();
 
         NavigationPanel navigationPanel = new NavigationPanel(studentsPanel);
 
         final EditorGrid<StudentModel> studentGrid = createStudentGrid();
 
         ContentPanel captainsContentPanel = new ContentPanel(new FitLayout());
        captainsContentPanel.setHeading("Студенты на степендию");
         captainsContentPanel.setHeight(400);
         captainsContentPanel.add(studentGrid);
         captainsContentPanel.addButton(new Button("Изменить начальную дату", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 StudentModel studentModel = studentGrid.getSelectionModel().getSelectedItem();
 
                 if (studentModel == null) {
                     Dispatcher.forwardEvent(CommonEvents.InfoWithConfirmation,
                             "Выберите, пожалуйста, студента со стипендией!");
                 } else {
                     studentGrid.startEditing(studentListStore.indexOf(studentModel), 2);
                 }
             }
         }));
        captainsContentPanel.addButton(new Button("Изменить сонечную дату", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 StudentModel studentModel = studentGrid.getSelectionModel().getSelectedItem();
 
                 if (studentModel == null) {
                     Dispatcher.forwardEvent(CommonEvents.InfoWithConfirmation,
                             "Выберите, пожалуйста, студента со стипендией!");
                 } else {
                     studentGrid.startEditing(studentListStore.indexOf(studentModel), 3);
                 }
             }
         }));
         captainsContentPanel.addButton(new Button("Удалить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 StudentModel studentModel = studentGrid.getSelectionModel().getSelectedItem();
 
                 if (studentModel == null) {
                     Dispatcher.forwardEvent(CommonEvents.InfoWithConfirmation,
                             "Выберите, пожалуйста, старосту для удаления!");
                 } else {
                     studentGrid.stopEditing();
                     studentListStore.remove(studentModel);
                 }
             }
         }));
 
 
         BorderLayoutData westLayoutData = new BorderLayoutData(Style.LayoutRegion.WEST, 200, 150, 400);
         westLayoutData.setCollapsible(true);
         westLayoutData.setSplit(true);
 
         BorderLayoutData eastLayoutData = new BorderLayoutData(Style.LayoutRegion.EAST, 400, 200, 600);
         eastLayoutData.setCollapsible(true);
         eastLayoutData.setSplit(true);
 
         BorderLayoutData centerLayoutData = new BorderLayoutData(Style.LayoutRegion.CENTER);
 
         add(navigationPanel, westLayoutData);
         add(studentsPanel, centerLayoutData);
         add(captainsContentPanel, eastLayoutData);
 
         navigationPanel.reloadMenuData();
     }
 
     private EditorGrid<StudentModel> createStudentGrid() {
         ColumnConfig nnColumnConfig = new ColumnConfig("nn", "#", 40);
         nnColumnConfig.setSortable(false);
         nnColumnConfig.setRenderer(new GridCellRenderer() {
             @Override
             public Object render(ModelData model, String property, ColumnData config,
                                  int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                 return rowIndex + 1;
             }
         });
 
         ColumnConfig nameColumnConfig = new ColumnConfig("fullName", "Имя", 200);
 
         ColumnConfig startDateColumnConfig = new ColumnConfig("startDate", "С", 100);
         startDateColumnConfig.setEditor(new CellEditor(new DateField()));
         startDateColumnConfig.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM));
 
         ColumnConfig stopDateColumnConfig = new ColumnConfig("stopDate", "По", 100);
         stopDateColumnConfig.setEditor(new CellEditor(new DateField()));
         stopDateColumnConfig.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM));
 
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
         columns.add(nnColumnConfig);
         columns.add(nameColumnConfig);
         columns.add(startDateColumnConfig);
         columns.add(stopDateColumnConfig);
 
         EditorGrid<StudentModel> studentGrid = new EditorGrid<StudentModel>(studentListStore, new ColumnModel(columns));
         studentGrid.setSelectionModel(new GridSelectionModel<StudentModel>());
         studentGrid.setClicksToEdit(EditorGrid.ClicksToEdit.TWO);
         studentGrid.setBorders(true);
         studentGrid.setAutoExpandColumn("fullName");
         studentGrid.setAutoExpandMax(2000);
 
         new GridDropTarget(studentGrid);
         new GridDragSource(studentGrid);
 
         return studentGrid;
     }
 }
