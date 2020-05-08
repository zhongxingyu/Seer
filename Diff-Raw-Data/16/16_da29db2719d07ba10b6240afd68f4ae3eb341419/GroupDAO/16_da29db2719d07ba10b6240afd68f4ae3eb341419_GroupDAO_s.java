 package pl.agh.enrollme.repository;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.utils.Color;
 import pl.agh.enrollme.utils.DayOfWeek;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class GroupDAO implements IGroupDAO {
 
     @Override
     public List<Group> getGroups(Subject subject) {
        List<Person> people = new ArrayList<Person>();
         List<Group> groups = new ArrayList<Group>();
         Person guy1 = new Person();
         guy1.setFirstName("Jasiu");
         people.add(guy1);
         Person guy2 = new Person();
         guy2.setFirstName("Jozek");
         people.add(guy2);
 
         groups.add(new Group(people, subject));
         return groups;
     }
 }
