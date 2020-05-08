 package com.vaps.action;
 
 import java.util.List;
 import com.vaps.bean.Items;
 import com.vaps.dao.ItemsDAO;
 
 
 public class ItemsListAction {
 	ItemsDAO dao;
 	
 	public ItemsListAction(ItemsDAO items){
 		this.dao = items;
 	}
 	
 	public List<Items> getItemsList(){
		//java.lang.NullPointerException
 		return dao.getItemsList();
 	}
 	
 }
