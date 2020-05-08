 package com.bulbview.recipeplanner.ui.manager;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.datamodel.NamedEntity;
 import com.bulbview.recipeplanner.ui.presenter.ICategoryListPresenter;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Table.TableDragMode;
 import com.vaadin.ui.TextArea;
 
 @Component
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class CategorisedItemList extends GenericListView<NamedEntity, ICategoryListPresenter> {
     
     private String categoryName;
     
     public CategorisedItemList() {
         super(NamedEntity.class);
     }
     
     @Override
     public void init() {
         super.init();
         topLevelPanel.addComponent(newItemTextFieldPanel());
         presenter.setCategory(categoryName);
         genericListTable.setDragMode(TableDragMode.ROW);
         setVisibleColumns("name");
         // TODO switch so presenter init view
         presenter.init();
     }
     
     public void setCategoryName(final String categoryName) {
         this.categoryName = categoryName;
     }
     
     @Override
     @Autowired
     public void setGenericListTable(final Table genericListTable) {
         super.setGenericListTable(genericListTable);
     }
     
     @Override
     @Autowired
    public void setPresenter(final ICategoryListPresenter categoryListPresenter) {
        super.setPresenter(categoryListPresenter);
     }
     
     @Autowired
     @Override
     public void setTopLevelPanel(final Panel panel) {
         super.setTopLevelPanel(panel);
     }
     
     public void showErrorMessage(final String message) {
         // TODO Auto-generated method stub
     }
     
     @SuppressWarnings("serial")
     private com.vaadin.ui.Component newItemTextFieldPanel() {
         final HorizontalLayout horizontalLayout = new HorizontalLayout();
         horizontalLayout.setMargin(true);
         horizontalLayout.setSpacing(true);
         // final TextField itemNameTextField = new TextField();
         final TextArea itemNameTextField = new TextArea();
         itemNameTextField.setWidth("190px");
         itemNameTextField.setHeight("25px");
         itemNameTextField.setInputPrompt("<Enter new item name>");
         horizontalLayout.addComponent(itemNameTextField);
         
         final Button button = new Button("Save");
         button.addListener(new ClickListener() {
             
             @Override
             public void buttonClick(final ClickEvent event) {
                 presenter.addItemByName(itemNameTextField.getValue().toString());
                 itemNameTextField.setValue("");
             }
         });
         button.setClickShortcut(KeyCode.ENTER);
         horizontalLayout.addComponent(button);
         return horizontalLayout;
     }
 }
