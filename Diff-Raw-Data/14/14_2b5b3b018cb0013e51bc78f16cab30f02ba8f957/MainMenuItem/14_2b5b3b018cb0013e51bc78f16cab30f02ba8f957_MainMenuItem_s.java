 package scoutinghub.menu;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 
 /**
  * User: eric
  * Date: 3/12/11
  * Time: 12:34 PM
  */
 public class MainMenuItem extends MenuItem {
 
     public MainMenuItem(String controller, String labelCode, String... requiredRoles) {
         super(controller, labelCode, requiredRoles);
     }
 
     public MainMenuItem(String controller, String labelCode, Closure permissionClosure) {
         super(controller, labelCode, permissionClosure);
     }
 
     ArrayList<SubMenuItem> subItems = new ArrayList<SubMenuItem>();
 
     public MainMenuItem addMenuItem(SubMenuItem menuItem) {
         menuItem.setMainMenuItem(this);
         this.subItems.add(menuItem);
         return this;
     }
 
     @Override
     public boolean isCurrentMenuItem(String controller, String action) {
         boolean rtn = false;
         if (super.isCurrentMenuItem(controller, action)) {
             rtn = true;
         } else {
             for (SubMenuItem subItem : subItems) {
                if (subItem.getController().equalsIgnoreCase(controller) && action.equalsIgnoreCase(action)) {
                     rtn = true;
                     break;
                 }
             }
         }
         return rtn;
     }
 }
