 package pl.agh.enrollme.repository;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Repository;
 import pl.agh.enrollme.model.*;
 import pl.agh.enrollme.utils.Color;
 import pl.agh.enrollme.utils.DayOfWeek;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceUnit;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author Michal Partyka
  */
 @Repository
 public class GroupDAO implements IGroupDAO {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GroupDAO.class.getName());
 
     @PersistenceContext
     EntityManager em;
 
     @Override
     public void addGroup(Subject subject) {
         // LOGGER.error(subject.getName() + "\n" + "\n");
         List<Person> people = new ArrayList<Person>();
         Person guy1 = new Person();
         guy1.setFirstName("Edward");
         people.add(guy1);
         Person guy2 = new Person();
         guy2.setFirstName("Edmund");
         people.add(guy2);
 
         em.persist(new Group(people, subject));
     }
 
     @Override
     public List<Group> getGroups(Subject subject) {
 
         /*List<Group> groups = new ArrayList<Group>();
 
         List<Person> people = new ArrayList<Person>();
         Person guy1 = new Person();
         guy1.setFirstName("Jasiu");
         people.add(guy1);
         Person guy2 = new Person();
         guy2.setFirstName("Jozek");
         people.add(guy2);
 
         groups.add(new Group(people, subject));
 
         List<Person> newPeople = new ArrayList<Person>();
         Person guy3 = new Person();
         guy3.setFirstName("Ryszard");
         newPeople.add(guy3);
         Person guy4 = new Person();
         guy4.setFirstName("Marian");
         newPeople.add(guy4);
 
         groups.add(new Group(newPeople, subject));      */
 
         //return groups;
        return em.createQuery("FROM Group").getResultList();
 
     }
 
     @Override
     public void tryToAddCurrentUserToGroup(Group group) {
         //System.out.println(group.getPersons().get(0).getFirstName());
         LOGGER.error(group.getPersons().get(0).getFirstName() + "\n" + "\n");
     }
 
 }
