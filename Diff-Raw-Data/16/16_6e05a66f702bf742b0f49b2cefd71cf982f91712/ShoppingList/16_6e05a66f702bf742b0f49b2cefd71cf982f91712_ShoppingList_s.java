 package com.bulbview.recipeplanner.ui.manager;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bulbview.recipeplanner.ui.presenter.ShoppingListPresenter;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Window;
 
 @Component
 public class ShoppingList extends ViewManager<ShoppingListPresenter> {
 
     private GridLayout shoppingListGrid;
     @Autowired
     private Window     shoppingListWindow;
 
     public void addCategory(final ShoppingListCategory shoppingListCategory) {
        shoppingListGrid.addComponent(shoppingListCategory.getTopLevelPanel());
     }
 
     @Override
     public void init() {
         shoppingListWindow.setContent(shoppingListGrid);
         shoppingListGrid.setMargin(true);
     }
 
     @Autowired
     public void setShoppingListGrid(final GridLayout shoppingListGrid) {
         this.shoppingListGrid = shoppingListGrid;
     }
 
 }
