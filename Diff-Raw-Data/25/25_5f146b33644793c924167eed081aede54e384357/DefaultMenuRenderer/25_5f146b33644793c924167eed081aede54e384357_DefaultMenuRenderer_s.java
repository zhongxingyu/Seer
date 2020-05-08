 package org.synyx.minos.core.web.tags.menu;
 
 /**
  * Default implementation of MenuRenderer
  *
  * @author David Linsin
  * @version 0.0.1
  * @since 0.0.1
  */
 public class DefaultMenuRenderer implements MenuRenderer {
 
     private Integer levels = 0;
     private boolean alwaysRenderSubmenus = false;
     private String rootId = null;
 
     @Override
     public String beforeMenu(MenuMetaInfo info) {
 
         StringBuilder builder = new StringBuilder();
         builder.append("<div id='");
 
         // unless id provided default to "menu" as id
         if (info.getId() != null) {
             builder.append(rootId);
         } else {
             builder.append("menu");
         }
 
         builder.append("'><ul class=\"menu\">");
 
         return builder.toString();
     }
 
 
     @Override
     public String afterMenu(MenuMetaInfo info) {
 
         return "</ul></div>";
     }
 
 
     @Override
     public String beforeMenuItem(MenuMetaInfo info) {
 
         String aClass = info.isActive() ? " class='active'" : "";
 
         return String.format("<li%s>", aClass);
     }
 
 
     @Override
     public String afterMenuItem(MenuMetaInfo info) {
 
         return "</li>";
     }
 
 
     @Override
     public String renderItem(MenuMetaInfo info) {
 
         if (info.getUrl() != null) {
             String aClass = info.isActive() ? " class='active'" : "";
 
             return String.format("<a id='%s' href='%s' title='%s'%s>%s</a>", info.getId(), info.getUrl(),
                     info.getDescription(), aClass, info.getTitle());
         }
 
         return "";
     }
 
 
     @Override
     public boolean proceedWithRenderingSubmenus(MenuMetaInfo info) {
 
         if (isAlwaysRenderSubmenus()) {
             return true;
         }
 
         return isLevelRestrictionDisabled() || levels <= info.getDepth();
     }
 
 
     @Override
    public String beforeSubmenueItems(MenuMetaInfo info) {
 
         return "<ul class='submenu'>";
     }
 
 
     @Override
    public String afterSubmenueItems(MenuMetaInfo info) {
 
         return "</ul>";
     }
 
 
     public boolean isAlwaysRenderSubmenus() {
 
         return alwaysRenderSubmenus;
     }
 
 
     public void setAlwaysRenderSubmenus(boolean argAlwaysRenderSubmenus) {
 
         alwaysRenderSubmenus = argAlwaysRenderSubmenus;
     }
 
 
     public String getRootId() {
 
         return rootId;
     }
 
 
     public void setRootId(String argRootId) {
 
         rootId = argRootId;
     }
 
 
     public Integer getLevels() {
 
         return levels;
     }
 
 
     public void setLevels(Integer argLevels) {
 
         levels = argLevels;
     }
 
 
     /**
      * If the attribute levels is set, the tag renders submenues until the given level. This method checks if the
      * level-feature is deactivated : if <minos:menu levels="0"> is used, the number of levels rendered are unlimited.
      * This is also true for negative values of levels.
      *
      * @return
      */
     private boolean isLevelRestrictionDisabled() {
 
         return levels <= 0;
     }
 }
