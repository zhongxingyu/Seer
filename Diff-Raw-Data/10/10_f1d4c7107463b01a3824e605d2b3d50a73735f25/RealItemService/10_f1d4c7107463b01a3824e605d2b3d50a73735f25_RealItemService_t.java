 
 package org.easetech.easytest.codegen.example;
 
 import java.util.ArrayList;
import org.easetech.easytest.codegen.example.dto.Item;
import org.easetech.easytest.codegen.example.dto.ItemId;
import org.easetech.easytest.codegen.example.dto.LibraryId;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
 
 public class RealItemService implements ItemService {
 
 
     public List<Item> getItems(LibraryId libraryId, String searchText, String itemType) {
         System.out.println("getItems Called");
         return Collections.EMPTY_LIST;
     }
 
 
     public Item findItem(LibraryId libraryId, ItemId itemId) {
         System.out.println("findItems Called");
         Item item = new Item();
         item.setDescription("Item Description Modified Again");
         item.setItemId(libraryId.getId().toString());
         item.setItemType("BOOK");
         return item;
     }
     
     public List<Item> findItemList(Item item) {
         System.out.println("findItemList Called:"+item);
         List<Item> itemList = new ArrayList<Item>();
         Item item2 = new Item();
         item2.setDescription("Item Description Modified Again");
         item2.setItemId(item.getItemId()+1);
         item2.setItemType(item.getItemType());
         
         itemList.add(item);
         itemList.add(item2);
         return itemList;
     }
     
     public void updateItem(Item item) {
         System.out.println("findItemList Called:"+item);
         List<Item> itemList = new ArrayList<Item>();
         Item item2 = new Item();
         item2.setDescription("Item Description Modified Again");
         item2.setItemId("two");
         item2.setItemType("LAPTOP");
         itemList.add(item);
         itemList.add(item2);
         //return itemList;
     }
     
     public Collection updateItemList(Collection paramItemList) {
         System.out.println("findItemList Called:"+paramItemList);
         List<Item> itemList = new ArrayList<Item>();
         Item item2 = new Item();
         item2.setDescription("Item Description Modified Again");
         item2.setItemId("two");
         item2.setItemType("LAPTOP");
         itemList.addAll(paramItemList);
         itemList.add(item2);
         return itemList;
     }
     /*
     public static void main(String[] args) {
         ArrayList<String> stringList = new ArrayList<String>();
         stringList.add(null);
         Iterator<String> strItr = stringList.iterator();
         while (strItr.hasNext()) {
             System.out.println(strItr.next());
         }
         for (String str : stringList) {
             System.out.println(str);
         }
     }*/
 
 }
