 package scoutinghub.menu;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * User: eric
  * Date: 3/10/11
  * Time: 9:09 PM
  */
 public abstract class MenuItem {
 
     private Closure permissionCheckClosure;
 
     public MenuItem(String controller, String labelCode, String... requiredRoles) {
         this(controller, "index", labelCode, requiredRoles);
     }
 
     public MenuItem(String controller, String labelCode, Closure permissionClosure) {
         this(controller, "index", labelCode, permissionClosure);
     }
 
     public MenuItem(String controller, String action, String labelCode, String... requiredRoles) {
         this.controller = controller;
         this.labelCode = labelCode;
         this.action = action;
         this.requiredRoles = Arrays.asList(requiredRoles);
     }
 
     public MenuItem(String controller, String action, String labelCode, Closure permissionCheckClosure) {
         this.controller = controller;
         this.labelCode = labelCode;
         this.action = action;
         this.requiredRoles = new ArrayList<String>();
         this.permissionCheckClosure = permissionCheckClosure;
     }
 
 
     protected String controller;
     protected String action;
     protected List<String> requiredRoles;
     protected String labelCode;
 
     public String getController() {
         return controller;
     }
 
     public void setController(String controller) {
         this.controller = controller;
     }
 
     public String getLabelCode() {
         return labelCode;
     }
 
     public void setLabelCode(String labelCode) {
         this.labelCode = labelCode;
     }
 
     public List<String> getRequiredRoles() {
         return requiredRoles;
     }
 
     public Closure getPermissionCheckClosure() {
         return permissionCheckClosure;
     }
 
     public void setRequiredRoles(List<String> requiredRoles) {
         this.requiredRoles = requiredRoles;
     }
 
 
     public boolean isCurrentMenuItem(String controller, String action) {
         return this.controller.equalsIgnoreCase(controller) && this.action.equalsIgnoreCase(action);
     }
 }
