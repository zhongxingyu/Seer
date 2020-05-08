 package com.bulbview.recipeplanner.ui.manager;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.ui.presenter.ShoppingListPresenter;
import com.vaadin.ui.Alignment;
 import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
 import com.vaadin.ui.Window;
 
 @Component
 public class ShoppingList extends ViewManager<ShoppingListPresenter> {
 
     private GridLayout shoppingListGrid;
     @Autowired
     private Window     shoppingListWindow;
 
     public void addCategory(final ShoppingListCategory shoppingListCategory) {
        shoppingListGrid.addComponent(categoryPanel(shoppingListCategory));
        shoppingListGrid.setComponentAlignment(categoryPanel(shoppingListCategory), Alignment.MIDDLE_CENTER);
     }
 
     @Override
     public void init() {
         shoppingListWindow.setContent(shoppingListGrid);
         shoppingListGrid.setMargin(true);
        shoppingListGrid.setSpacing(true);
        shoppingListGrid.setHeight("100%");
        shoppingListGrid.setWidth("100%");
     }
 
     @Autowired
     public void setShoppingListGrid(final GridLayout shoppingListGrid) {
         this.shoppingListGrid = shoppingListGrid;
     }
 
    private Panel categoryPanel(final ShoppingListCategory shoppingListCategory) {
        return shoppingListCategory.getTopLevelPanel();
    }

 }
