 package com.mustangexchange.polymeal;
 
 import java.util.ArrayList;
 
 public class ItemSet {
     private String title;
     private ArrayList<Item> items = new ArrayList<Item>();
 
     public ItemSet(String title, ArrayList<Item> items)
     {
         this.title = title;
         this.items = items;
     }
 
     public ItemSet() {}
 
     public ArrayList<Item> getItems()
     {
         return items;
     }
     public int size()
     {
         return items.size();
     }
 
     public void add(Item item)
     {
        items.add(item);
     }
 
     public void addAt(Item item,int index)
     {
         items.add(index,item);
     }
 
     public String getTitle()
     {
         return this.title;
     }
 
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     public Item getItem(int index)
     {
         return items.get(index);
     }
 
     public void clear()
     {
         title = null;
         items.clear();
     }
 
 }
