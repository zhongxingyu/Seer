 package test;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 public class TestDataProvider extends SortableDataProvider<User, String> {
 
     private static final long serialVersionUID = 1L;
     private static List<User> userList = new ArrayList<User>();
     
     static {
         userList.add(new User("Alice", "Tester"));
         userList.add(new User("Bob", "Tester"));
         userList.add(new User("Carol", "Tester"));
         userList.add(new User("Debbie", "Tester"));
         userList.add(new User("Eve", "Tester"));
     }
     
     @Override
     public Iterator<? extends User> iterator(long first, long count) {
        return userList.subList((int)first, (int)(first + count)).iterator();
     }
 
     @Override
     public long size() {
        return userList.size();
     }
 
     @Override
     public IModel<User> model(User user) {
         return Model.of(user);
     }
 
 }
