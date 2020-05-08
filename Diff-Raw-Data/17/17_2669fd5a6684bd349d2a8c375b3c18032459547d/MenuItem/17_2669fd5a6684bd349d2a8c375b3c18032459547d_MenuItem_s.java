 package org.synyx.minos.core.web.menu;
 
 import org.springframework.util.StringUtils;
 
 import org.synyx.minos.util.Assert;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * Class to define a {@link MenuItem}. Allows definition of basic properties like title, description and so on.
  * {@link MenuItem}s can be ordered using the {@code position} property. Title and description will be resolved via
  * {@link java.util.ResourceBundle}s.
  *
  * @author Oliver Gierke - gierke@synyx.de
  * @author Marc Kannegiesser - kannegiesser@synyx.de
  */
 public final class MenuItem implements Comparable<MenuItem> {
 
     public static final String PATH_SEPARATOR = "/";
 
     private static final String TITLE_POSTFIX = ".title";
     private static final String DESCRIPTION_POSTFIX = ".description";
 
     private final String id;
     private String path;
 
     private UrlResolver urlStrategy;
     private String title;
    private String desciption;
     private Integer position = 0;
 
     private List<String> permissions = new ArrayList<String>();
 
     private MenuItem(String id) {
 
         this.id = id;
     }
 
     public String getId() {
 
         return id;
     }
 
 
     /**
      * Returns the {@link UrlResolver} for the {@link MenuItem} shall link to.
      *
      * @return the {@link UrlResolver} of this
      */
     public UrlResolver getUrlResolver() {
 
         return urlStrategy;
     }
 
 
     /**
      * Returns the title of the {@link MenuItem}. Will be resolved against a resource bundle.
      *
      * @return the title
      */
     public String getTitle() {
 
         return title;
     }
 
 
     /**
      * Returns the description of the {@link MenuItem}.
      *
     * @return the desciption
      */
     public String getDescription() {
 
        return desciption;
     }
 
 
     /**
      * Returns the position of the menu item. 0 means first one.
      *
      * @return the position
      */
     public int getPosition() {
 
         return position;
     }
 
 
     /**
      * Returns the permission required to access this {@link MenuItem}. Includes permissions from parent menu items,
      * too.
      *
      * @return the permissions
      */
     public List<String> getPermissions() {
 
         return Collections.unmodifiableList(permissions);
     }
 
 
     @Override
     public int compareTo(MenuItem item) {
 
         return this.position.compareTo(item.getPosition());
     }
 
 
     @Override
     public String toString() {
 
         StringBuilder builder = new StringBuilder(title);
         builder.append(", URL: ").append("...who knows yet...");
         builder.append(", Position: ").append(position);
         builder.append(", Permissions: ").append(StringUtils.collectionToCommaDelimitedString(getPermissions()));
 
         return builder.toString();
     }
 
 
     @Override
     protected Object clone() throws CloneNotSupportedException {
 
         return typeSaveClone();
     }
 
 
     protected MenuItem typeSaveClone() {
 
         return deepCopy(this).build();
     }
 
 
     @Override
     public int hashCode() {
 
         return getPath().hashCode();
     }
 
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
 
         if (obj == null)
             return false;
 
         if (getClass() != obj.getClass())
             return false;
 
         MenuItem other = (MenuItem) obj;
 
         return other.path.equals(path);
     }
 
 
     public String getPath() {
 
         return path == null ? id : path;
     }
 
 
     public String getParentPath() {
 
         String path = getPath();
 
         if (!path.contains(PATH_SEPARATOR)) {
             return "";
         }
 
         int pos = path.lastIndexOf(PATH_SEPARATOR);
 
         return path.substring(0, pos);
     }
 
 
     public static MenuItemBuilder create(String id) {
 
         return new MenuItemBuilder(id);
     }
 
 
     public static MenuItemBuilder deepCopy(MenuItem item) {
 
         MenuItemBuilder clone = MenuItem.create(item.id);
 
         clone.menuItem.path = item.getPath();
         clone.menuItem.urlStrategy = item.urlStrategy;
         clone.menuItem.title = item.title;
        clone.menuItem.desciption = item.desciption;
         clone.menuItem.position = item.position;
 
         clone.menuItem.permissions = new ArrayList<String>();
 
         for (String permission : item.permissions) {
             clone.menuItem.permissions.add(permission);
         }
 
         return clone;
     }
 
 
     public MenuItemBuilder createChild(MenuItem item) {
 
         MenuItemBuilder clone = deepCopy(item);
         clone.menuItem.path = getPath() + PATH_SEPARATOR + item.id;
 
         return clone;
     }
 
     /**
      * Builder class to create {@link MenuItem}s in a step-by-step fashion but keep the actual {@link MenuItem} class
      * immutable.
      *
      * @author Marc Kannegiesser - kannegiesser@synyx.de
      * @author Oliver Gierke
      */
     public static final class MenuItemBuilder {
 
         private final MenuItem menuItem;
 
         /**
          * Creates a new {@link MenuItemBuilder}.
          *
          * @param id
          */
         private MenuItemBuilder(String id) {
 
             Assert.notNull(id);
             menuItem = new MenuItem(id);
         }
 
         /**
          * Builds the {@link MenuItem}. The instance is frozen after this method was called.
          *
          * @return
          */
         public MenuItem build() {
 
             if (menuItem.urlStrategy == null) {
                 throw new IllegalStateException("Either URL or UrlResolver must be set.");
             }
 
             return menuItem;
         }
 
 
         public MenuItemBuilder withTitle(String title) {
 
             menuItem.title = title;
 
             return this;
         }
 
 
         public MenuItemBuilder withDescription(String description) {
 
            menuItem.desciption = description;
 
             return this;
         }
 
 
         public MenuItemBuilder withPosition(Integer position) {
 
             Assert.notNull(position);
             menuItem.position = position;
 
             return this;
         }
 
 
         public MenuItemBuilder withPermissions(List<String> permissions) {
 
             Assert.notNull(permissions);
             menuItem.permissions = permissions;
 
             return this;
         }
 
 
         public MenuItemBuilder withPermission(String permission) {
 
             menuItem.permissions.add(permission);
 
             return this;
         }
 
 
         public MenuItemBuilder withKeyBase(String keyBase) {
 
             Assert.notNull(keyBase);
             menuItem.title = keyBase + TITLE_POSTFIX;
            menuItem.desciption = keyBase + DESCRIPTION_POSTFIX;
 
             return this;
         }
 
 
         /**
          * Sets the {@link UrlResolver} to be used to determine the URL the {@link MenuItem} shall link to.
          *
          * @param strategy
          * @return
          */
         public MenuItemBuilder withUrlResolver(UrlResolver strategy) {
 
             Assert.notNull(strategy);
             menuItem.urlStrategy = strategy;
 
             return this;
         }
 
 
         /**
          * Lets the {@link MenuItem} link to a static URL.
          *
          * @param url
          * @return
          */
         public MenuItemBuilder withUrl(String url) {
 
             Assert.notNull(url);
             menuItem.urlStrategy = new SimpleUrlResolver(url);
 
             return this;
         }
 
 
         public MenuItemBuilder withParent(MenuItem parent) {
 
             Assert.notNull(parent);
             menuItem.path = String.format("%s%s%s", parent.getPath(), PATH_SEPARATOR, menuItem.getId());
 
             return this;
         }
     }
 }
