 package webmenu.model;
 import java.util.*;
 
 import com.google.appengine.api.datastore.Key;
 import javax.jdo.annotations.*;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
 public class OneDayMenu
 {
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     @Persistent
     private Date day;
 
     // TODO - use internal ordering
     // @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="state asc, city asc"))
     @Persistent
     @Element(dependent = "true")
     private List<SoupItem> soupItems;
 
     // TODO - use internal ordering
     // @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering", value="state asc, city asc"))
     @Persistent
     @Element(dependent = "true")
     private List<MenuItem> menuItems;
 
     public Date getDay()
     {
         return day;
     }
 
     public List<SoupItem> getSoupItems()
     {
         return soupItems;
     }
 
     public List<MenuItem> getMenuItems()
     {
         return menuItems;
     }
 
     public void setKey(Key key)
     {
         this.key = key;
     }
 
     public void update(OneDayMenu from)
     {
         if (!this.getDay().equals(from.getDay()))
             throw new IllegalArgumentException(
                     "Cannot update OneDayMenuItem from an object for different day. " +
                     "Mine is '" + this.getDay() + "', theirs is '" + from.getDay() + "'.");
         this.soupItems = from.getSoupItems();
         this.menuItems = from.getMenuItems();
     }
 
     public OneDayMenu(Date day, List<SoupItem> soups, List<MenuItem> meals)
     {
         this.day = day;
         this.soupItems = soups;
         this.menuItems = meals;
     }
 }
