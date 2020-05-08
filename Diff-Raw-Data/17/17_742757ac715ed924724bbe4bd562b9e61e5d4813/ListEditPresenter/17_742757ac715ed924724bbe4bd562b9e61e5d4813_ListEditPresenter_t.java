 /**
  * This file is part of Flash Cart.
  *
  * Copyright (C) 2011 Javier Estévez
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.valdaris.shoppinglist.presenter;
 
 import java.util.List;
 
 import com.valdaris.shoppinglist.dao.IDataHandler;
 import com.valdaris.shoppinglist.model.ListItem;
 import com.valdaris.shoppinglist.model.ShoppingList;
 import com.valdaris.shoppinglist.view.IListEdit;
 
 /**
  * @author Javier Estévez
  * 
  */
 public class ListEditPresenter {
 
     IListEdit view;
     IDataHandler dataHandler;
     private ShoppingList list;
 
     public ListEditPresenter(IListEdit view, IDataHandler dataHandler) {
         this.view = view;
         this.dataHandler = dataHandler;
         this.list = new ShoppingList();
     }
 
     public void fillList(int listId) {
 
         list.setId(listId);
 
         List<ListItem> products = dataHandler.getListProducts(list);
 
         view.fillList(products);
     }
 
     public void saveList() {
 
         List<ListItem> products = view.getProducts();
 
         dataHandler.setListProducts(list, products);
 
     }
     
     public void addProduct(ListItem product) {
         
     }
     

 
 }
