 package vahdin.data;
 
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.ObjectProperty;
 import com.vaadin.data.util.PropertysetItem;
 import com.vaadin.data.util.sqlcontainer.SQLContainer;
 import com.vaadin.data.util.sqlcontainer.query.TableQuery;
 
 public class User implements Item {
 
     private static final SQLContainer container;
     private static final Logger logger = Logger.getGlobal();
 
     static {
         logger.info("Initializing users");
        TableQuery table = new TableQuery("User", DB.pool);
         table.setVersionColumn("version");
         try {
             container = new SQLContainer(table);
         } catch (SQLException e) {
             throw new Error(e);
         }
     }
 
     public static User load(String id) {
         return new User(container.getItem(id));
     }
 
     public static User guest() {
         PropertysetItem item = new PropertysetItem();
         item.addItemProperty("id", new ObjectProperty<String>("guest"));
         item.addItemProperty("screenname", new ObjectProperty<String>("guest"));
         return new User(item);
     }
 
     private Item row;
     private boolean loggedIn = false;
 
     private User(Item item) {
         row = item;
     }
 
     /**
      * Returns true if the user is currently marked as logged in.
      * 
      * @return The current login status of the user.
      */
     public boolean isLoggedIn() {
         return loggedIn;
     }
 
     /** Marks the user as logged in. */
     public void markLoggedIn() {
         loggedIn = true;
     }
 
     /** Marks the user as logged out. */
     public void markLoggedOut() {
         loggedIn = false;
     }
 
     @SuppressWarnings("rawtypes")
     @Override
     public Property getItemProperty(Object id) {
         return row.getItemProperty(id);
     }
 
     @Override
     public Collection<?> getItemPropertyIds() {
         return row.getItemPropertyIds();
     }
 
     @Override
     public boolean addItemProperty(Object id, @SuppressWarnings("rawtypes")
     Property property) throws UnsupportedOperationException {
         return row.addItemProperty(id, property);
     }
 
     @Override
     public boolean removeItemProperty(Object id)
             throws UnsupportedOperationException {
         return row.removeItemProperty(id);
     }
 
 }
