 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.menu.classic.application;
 
 import org.apache.commons.lang.Validate;
 
 import com.globant.katari.core.web.MenuNode;
 
 /** Display attributes for a menu node.
  *
  * It contains the menu node itself and the specific display information. It is
  * created by a menu display decider.
  */
 public class MenuNodeDisplay {
 
   /** The node to display.
    *
    * It is never null.
    */
   private MenuNode menuNode;
 
   /** The path of this menu.
    *
    * It is never null.
    */
   private String path;
 
   /** The url that this menu node points to.
    *
    * It is never null.
    */
   private String linkPath;
 
  /** True if the menu display node is enabled, otherwise false.
    *
    * A disabled menu node is displayed (rendered) but clicking on it has no
    * effect. If the node is disabled it cannot be selected.
    */
   private boolean enabled = true;
 
   /** True if the menu node is selected, otherwise false.
    *
   * Being a selected node means that the node path is base part of the current
    * selection path or the selection path itself. If the node is selected,
    * cannot be disabled.
    */
   private boolean selected = false;
 
   /** Create a display for the menu node.
    *
    * If the node is selected, cannot be disabled.
    *
    * @param theMenuNode The menu node. It cannot be null.
    *
    * @param thePath The path of the menu node. It cannot be null.
    *
    * @param theLinkPath The url that this menu node points to. It cannot be
    * null.
    *
   * @param isEnabled True if the menu node is enabled, otherwise false.
    * Disabled menu nodes are displayed but not clickable.
    *
    * @param isSelected True if the menu node is selected. A node is selected if
    * is part of the current selection path.
    */
   public MenuNodeDisplay(final MenuNode theMenuNode, final String thePath,
       final String theLinkPath, final boolean isEnabled, final boolean
       isSelected) {
 
     Validate.notNull(theMenuNode, "A menu node must be provided to create a"
         + " display node");
     Validate.notNull(thePath, "The path cannot be null");
     Validate.notNull(theLinkPath, "The link path cannot be null");
     Validate.isTrue(!isSelected || isEnabled,
         "A selected node cannot be disabled");
 
     menuNode = theMenuNode;
     path = thePath;
     linkPath = theLinkPath;
     enabled = isEnabled;
     selected = isSelected;
   }
 
   /** Obtains the menu node.
    *
    * @return the menu node. It never returns null.
    */
   public MenuNode getMenuNode() {
     return menuNode;
   }
 
   /** The path of this menu node.
    *
    * @return The path of the node, never returns null.
    */
   public String getPath() {
     return path;
   }
 
   /** The url that this menu node points to.
    *
    * @return The url of the node, never returns null.
    */
   public String getLinkPath() {
     return linkPath;
   }
 
  /** True if the menu node is enabled, false otherwise.
    *
    * A disabled menu node is displayed (rendered) but clicking on it has no
    * effect. If the node is disabled it cannot be selected.
    *
   * @return True if the menu node is enabled, otherwise false.
    */
   public boolean isEnabled() {
     return enabled;
   }
 
   /** True if the menu node is selected, otherwise false.
    *
   * This is true if the menu node is part of the current selection path. If
    * the node is selected, it cannot be disabled.
    *
    * @return True if the menu node is selected, otherwise false.
    */
   public boolean isSelected() {
     return selected;
   }
 }
 
