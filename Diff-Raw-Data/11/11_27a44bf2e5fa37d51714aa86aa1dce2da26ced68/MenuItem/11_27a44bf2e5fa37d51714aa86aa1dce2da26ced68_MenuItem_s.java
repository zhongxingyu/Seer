 package au.com.gaiaresources.bdrs.model.menu;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import au.com.gaiaresources.bdrs.security.Role;
 import au.com.gaiaresources.bdrs.util.StringUtils;
 
 /**
  * A MenuItem represents an item in a menu in a view.
  * This object contains its name, submenus, link to a page to display on selection, 
  * and a set of {@link Role Roles} that define who can view the item.
  * 
  * @author stephanie
  */
 public class MenuItem implements Comparable<MenuItem>, Serializable {
 
     /**
      * 
      */
     private static final long serialVersionUID = -7120509046578687956L;
 
     /**
      * The text that will be displayed on the menu item in the view.
      */
     private String name;
     
     /**
      * The path to the page to link to on selection.  By default this is null.
      */
     private String path = null;
     
     /**
      * The description of the menu item.
      */
     private String description = null;
     
     /**
      * Items contained in this menu's submenu.
      */
     private List<MenuItem> items = new ArrayList<MenuItem>();
     
     /**
      * Creates a MenuItem with the given name and path with no submenu.
      * @param name
      * @param path
      */
     public MenuItem(String name, String path) {
         this(name, path, null);
     }
     /**
      * Creates a MenuItem object with the given name, path, and submenu items.
      * @param name The display name for the menu item
      * @param path The path to the page to display on selection or null if none
      * @param subMenu A list of menu items to display in the sublist
      */
     public MenuItem(String name, String path, List<MenuItem> subMenu) {
         this(name, path, null, subMenu);
     }
     
     /**
      * Creates a MenuItem object with the given name, path, description, 
      * and submenu items.
      * @param name The display name for the menu item
      * @param path The path to the page to display on selection or null if none
      * @param description The description for the menu item
      * @param subMenu A list of menu items to display in the sublist
      */
     public MenuItem(String name, String path, String description, List<MenuItem> subMenu) {
         this.name = name;
         this.description = description;
         if (path != null) {
             this.path = path;
         }
         if (subMenu != null) {
             this.items = subMenu;
         }
     }
 
     /**
      * Gets the {@link #name}
      * @return {@link #name}
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the {@link #name}
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Gets the {@link #path}
      * @return {@link #path}
      */
     public String getPath() {
         return path;
     }
 
     /**
      * Sets the {@link #description}
      * @param description the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
     
     /**
      * Gets the {@link #description}
      * @return {@link #description}
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Sets the {@link #path}
      * @param path the path to set
      */
     public void setPath(String path) {
         if (path == null) {
             throw new IllegalArgumentException("Path cannot be null.");
         }
         this.path = path;
     }
 
     /**
      * Gets the {@link #items}
      * @return {@link #items}
      */
     public List<MenuItem> getItems() {
         return items;
     }
 
     /**
      * Sets the {@link #items}
      * @param items the items to set
      */
     public void setItems(List<MenuItem> items) {
         this.items = items;
     }
     
     /*
      * (non-Javadoc)
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     @Override
     public int compareTo(MenuItem o) {
         // compares two menu items by name to determine sorting order
         // the name Bulk Data always sorts to the end of the collection
         String thisName = this.getName();
         String thatName = o.getName();
         if (StringUtils.nullOrEmpty(thisName) && StringUtils.nullOrEmpty(thatName)) {
             return 0;
         } else if (StringUtils.nullOrEmpty(thisName)) {
             return -1;
         } else if (StringUtils.nullOrEmpty(thatName)) {
             return 1;
        } else if ("BulkData".equals(thisName)) {
             return 1;
        } else if ("BulkData".equals(thatName)) {
             return -1;
         } else {
             return thisName.compareTo(thatName);
         }
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof MenuItem)) {
             return false;
         }
         
         MenuItem that = (MenuItem) obj;
         // two menu items with the same name should be the same object
         return this.getName().equals(that.getName());
     }
     
     @Override
     public int hashCode() {
         return super.hashCode();
     }
 }
