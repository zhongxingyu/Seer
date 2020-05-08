 package vahdin.data;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.sqlcontainer.SQLContainer;
 import com.vaadin.data.util.sqlcontainer.query.TableQuery;
 
 public class Mark implements Item {
 
     private static final SQLContainer container;
     private static final Logger logger = Logger.getGlobal();
 
     static {
         logger.info("Initializing users");
         TableQuery table = new TableQuery("Mark", DB.pool);
         table.setVersionColumn("version");
         try {
             container = new SQLContainer(table);
         } catch (SQLException e) {
             throw new Error(e);
         }
     }
 
     private Item row;
 
     private int voteCount = 0;
     private ArrayList<Bust> busts;
 
     @SuppressWarnings("unchecked")
     public Mark(String name, Date time, String description, int photoId, int id) {
         row = container.getItem(container.addItem());
         row.getItemProperty("Name").setValue(name);
         row.getItemProperty("CreationTime").setValue(time);
         row.getItemProperty("Description").setValue(description);
     }
 
     public String getTitle() {
         return (String) getItemProperty("Name").getValue();
     }
 
     public String getTime() {
         return (String) getItemProperty("CreationTime").getValue();
     }
 
     public String getDescription() {
         return (String) getItemProperty("Description").getValue();
     }
 
     public int getPhotoId() {
         return getId();
     }
 
     public int getVoteCount() {
         return this.voteCount; // TODO:
     }
 
     public int getId() {
        Integer id = (Integer) row.getItemProperty("Id").getValue();
        return id == null ? 0 : id;
     }
 
     public ArrayList<Bust> getBusts() {
         return this.busts; // TODO:
     }
 
     public void addBust(Bust bust) {
         this.busts.add(bust); // TODO:
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
