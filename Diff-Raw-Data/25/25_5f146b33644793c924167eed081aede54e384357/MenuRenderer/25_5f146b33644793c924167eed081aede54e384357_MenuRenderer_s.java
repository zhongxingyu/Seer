 package org.synyx.minos.core.web.tags.menu;
 
 /**
  * Interface for rendering a menu tag
  *
  * @author David Linsin - linsin@synyx.de
  */
 public interface MenuRenderer {
 
     /**
      * Called before menu is rendered
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
     String beforeMenu(MenuMetaInfo info);
 
 
     /**
      * Called before each menu item
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
     String beforeMenuItem(MenuMetaInfo info);
 
 
     /**
      * Called rendering each item
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
     String renderItem(MenuMetaInfo info);
 
 
     /**
      * Called after each menu item
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
     String afterMenuItem(MenuMetaInfo info);
 
 
     /**
      * Called after menu is rendered
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
     String afterMenu(MenuMetaInfo info);
 
 
     /**
      * Called after each menu item
      *
      * @param info MenuMetaInfo instance
      * @return true if proceeding with rendering of sub menus, else false
      */
     boolean proceedWithRenderingSubmenus(MenuMetaInfo info);
 
 
     /**
      * Called before each rendering submenues of a menuitem
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
    String beforeSubmenueItems(MenuMetaInfo info);
 
 
     /**
      * Called before after rendering submenues of a menuitem
      *
      * @param info MenuMetaInfo instance
      * @return String containing the rendering result
      */
    String afterSubmenueItems(MenuMetaInfo info);
 }
