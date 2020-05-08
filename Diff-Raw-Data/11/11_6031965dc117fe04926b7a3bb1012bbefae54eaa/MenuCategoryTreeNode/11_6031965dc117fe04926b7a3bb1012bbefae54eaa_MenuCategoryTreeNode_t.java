 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.pos.model.menu;
 
 import com.pos.action.MenuItemAction;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Josh
  */
 public class MenuCategoryTreeNode {
     private int uid;
     private int puid;
     private String categoryName;
     private List<MenuCategoryTreeNode> childeren;
     private List<MenuItem> items;
     private static String htmlMenu = null;
     private MenuItemAction menuItemAction;
     
     public MenuCategoryTreeNode(int uid, int puid, String name){
         this.puid = puid;
         this.uid = uid;
         this.categoryName = name;
         childeren = new ArrayList<MenuCategoryTreeNode>();
         if (htmlMenu == null){
             htmlMenu = "";
         }
         menuItemAction = new MenuItemAction();
         //Pull item array for this category/node according to its uid
         items = menuItemAction.getMenuItems(Integer.toString(uid));
         
     }
     
     public void addChild(MenuCategoryTreeNode childNode){
         childeren.add(childNode);
     }
     
     public String outputHTML(){
         
         if (!this.getCategoryName().equals("root")){
             htmlMenu += "<li>" + this.getCategoryName();
         }
         
         if(!items.isEmpty()){
             htmlMenu += "<ol data-role=\"listmenu\">";
             
             for(MenuItem item : items){
                 htmlMenu += "<li "// id=\"" + item.getName() 
                    /* + "\" class=\"" + item.getCategoryUID() + "\""*/ 
                    + "onclick=\"javascript:addItemToOrder(\'" 
                    + item.getName() + "\',\' " + item.getPrice() + "\')\">" 
                     + item.getName() + "</li>";
             }
             
             htmlMenu += "</ol>";
         }
         
         if(!childeren.isEmpty()){
             if(!this.getCategoryName().equals("root")){
                 htmlMenu += "<ul data-role=\"listmenu\">";
             }
             
             for(MenuCategoryTreeNode node : childeren){
                 node.outputHTML();
             }
             htmlMenu += "</ul>";
         }
         
         htmlMenu += "</li>";
         
         return htmlMenu;
     }
     
     public int getUid(){
         return uid;
     }
     
     public int getPuid(){
         return puid;
     }
     
     public String getCategoryName(){
         return categoryName;
     }
     
     //This should only be used from rootNode before building htmlMenu String.
     public void resetHTML(){
         htmlMenu = "";
     }
 }
