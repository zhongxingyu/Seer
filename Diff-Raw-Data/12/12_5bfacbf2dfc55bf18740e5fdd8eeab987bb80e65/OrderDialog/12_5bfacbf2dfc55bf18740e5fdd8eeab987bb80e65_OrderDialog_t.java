 package ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components.orders;
 
 import com.extjs.gxt.ui.client.data.BaseModel;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.*;
 import com.extjs.gxt.ui.client.widget.grid.*;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.layout.*;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.utils.FormUtil;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model.DirectiveModel;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model.OrderModel;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model.PersonModel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Khurtin Denis ( KhurtinDN (a) gmail.com )
  * Date: 3/10/11
  * Time: 2:01 PM
  */
 public class OrderDialog extends Window {
     private OrderModel orderModel;
 
     private TextField<String> commentTextField = new TextField<String>();
     private TextField<String> orderNumberTextField = new TextField<String>();
     private DateField dateField = new DateField();
 
     private ModelData dean = new BaseModel();
     private ModelData rector = new BaseModel();
     private ComboBox<ModelData> signatureComboBox = new ComboBox<ModelData>();
 
     private ListStore<DirectiveModel> directiveListStore = new ListStore<DirectiveModel>();
     private ListStore<PersonModel> conformListStore = new ListStore<PersonModel>();
 
     private SelectDirectivePopup selectDirectivePopup = new SelectDirectivePopup();
 
     public OrderDialog() {
         setSize(800, 600);
         setHeading("Новый приказ");
         setLayout(new FitLayout());
 
         FormPanel formPanel = new FormPanel();
         formPanel.setHeaderVisible(false);
         formPanel.setFrame(false);
 
         LayoutContainer hBoxLayoutContainer = new LayoutContainer(new HBoxLayout());
 
         dateField.setFieldLabel("Дата");
         FormLayout dateFormLayout = new FormLayout();
         dateFormLayout.setLabelWidth(formPanel.getLabelWidth());
         LayoutContainer dateLayoutContainer = new LayoutContainer(dateFormLayout);
         dateLayoutContainer.add(dateField, FormUtil.formData);
         hBoxLayoutContainer.add(dateLayoutContainer, FormUtil.flex0);
 
         hBoxLayoutContainer.add(new Text(), FormUtil.flex1);
 
         orderNumberTextField.setFieldLabel("Номер");
         LayoutContainer orderNumberLayoutContainer = new LayoutContainer(new FormLayout(FormPanel.LabelAlign.RIGHT));
         orderNumberLayoutContainer.add(orderNumberTextField, FormUtil.formData);
         hBoxLayoutContainer.add(orderNumberLayoutContainer, FormUtil.flex0);
 
         formPanel.add(hBoxLayoutContainer, FormUtil.wh5FormData);
 
         commentTextField.setFieldLabel("Примечание");
         formPanel.add(commentTextField, FormUtil.wh10FormData);
 
         formPanel.add(createDirectiveContentPanel(), FormUtil.wh10FormData);
 
         //***************************************************************
         dean.set("title", "Декан");
         rector.set("title", "Ректор");
 
         ListStore<ModelData> store = new ListStore<ModelData>();
         store.add(dean);
         store.add(rector);
 
         signatureComboBox.setFieldLabel("На подпись");
         signatureComboBox.setEditable(false);
         signatureComboBox.setTriggerAction(ComboBox.TriggerAction.ALL);
         signatureComboBox.setDisplayField("title");
         signatureComboBox.setStore(store);
         //***************************************************************
         formPanel.add(signatureComboBox, FormUtil.h10FormData);
 
         formPanel.add(createConformContentPanel(), FormUtil.wh10FormData);
 
         add(formPanel);
 
         addButton(new Button("Создать", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: RPC call
             }
         }));
 
         addButton(new Button("Отменить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 OrderDialog.this.setVisible(false);
             }
         }));
     }
 
     private ContentPanel createDirectiveContentPanel() {
         ContentPanel contentPanel = new ContentPanel(new FitLayout());
         contentPanel.setHeading("Директивы");
         contentPanel.setBorders(true);
         contentPanel.setHeight(180);
 
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig nnColumnConfig = new ColumnConfig("nn", "#", 40);
         nnColumnConfig.setSortable(false);
         nnColumnConfig.setRenderer(new GridCellRenderer() {
             @Override
             public Object render(ModelData model, String property, ColumnData config,
                                  int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                 return rowIndex + 1;
             }
         });
         columns.add(nnColumnConfig);
 
         ColumnConfig nameColumnConfig = new ColumnConfig("name", "Название", 200);
         nameColumnConfig.setSortable(false);
         columns.add(nameColumnConfig);
 
         ColumnModel columnModel = new ColumnModel(columns);
 
         Grid<DirectiveModel> grid = new Grid<DirectiveModel>(directiveListStore, columnModel);
         grid.setAutoExpandMax(1000);
         grid.setAutoExpandColumn("name");
 
         contentPanel.add(grid);
 
         contentPanel.addButton(new Button("Добавить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: implement
                 AppointCaptainsDirectiveWindow directiveWindow = new AppointCaptainsDirectiveWindow();
                 directiveWindow.show();
             }
         }));
 
         contentPanel.addButton(new Button("Изменить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 AppointSocialStipendDirectiveWindow directiveWindow = new AppointSocialStipendDirectiveWindow();
                 directiveWindow.show();
             }
         }));
 
         contentPanel.addButton(new Button("Удалить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: implement
             }
         }));
 
         return contentPanel;
     }
 
     private ContentPanel createConformContentPanel() {
         ContentPanel contentPanel = new ContentPanel(new FitLayout());
         contentPanel.setHeading("Согласовать с");
         contentPanel.setBorders(true);
         contentPanel.setHeight(180);
 
         List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 
         ColumnConfig nnColumnConfig = new ColumnConfig("nn", "#", 40);
         nnColumnConfig.setSortable(false);
         nnColumnConfig.setRenderer(new GridCellRenderer() {
             @Override
             public Object render(ModelData model, String property, ColumnData config,
                                  int rowIndex, int colIndex, ListStore listStore, Grid grid) {
                 return rowIndex + 1;
             }
         });
         columns.add(nnColumnConfig);
 
         ColumnConfig nameColumnConfig = new ColumnConfig("name", "Должностное лицо", 200);
         nameColumnConfig.setSortable(false);
         columns.add(nameColumnConfig);
 
         ColumnModel columnModel = new ColumnModel(columns);
 
         Grid<PersonModel> grid = new Grid<PersonModel>(conformListStore, columnModel);
         grid.setAutoExpandMax(1000);
         grid.setAutoExpandColumn("name");
 
         contentPanel.add(grid);
 
         contentPanel.addButton(new Button("Добавить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: implement
             }
         }));
 
         contentPanel.addButton(new Button("Изменить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: implement
             }
         }));
 
         contentPanel.addButton(new Button("Удалить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 // todo: implement
             }
         }));
 
         return contentPanel;
     }
 
     public void clearForm() {
         orderNumberTextField.setValue(null);
         commentTextField.setValue(null);
         dateField.setValue(null);
 
         directiveListStore.removeAll();
         conformListStore.removeAll();
     }
 
     public void showNewOrderDialog() {
         clearForm();
         orderModel = new OrderModel();
 
         // todo: delete after testing
         DirectiveModel directiveModel = new DirectiveModel();
        directiveModel.set("name", "Распределение по группам");
         directiveListStore.add(directiveModel);
 
         PersonModel personModel = new PersonModel();
         personModel.set("name", "Начальник УФМЦ");
         conformListStore.add(personModel);
 
         show();
     }
 
     public void showEditOrderDialog(OrderModel orderModel) {
         // todo: implement
         show();
     }
 }
