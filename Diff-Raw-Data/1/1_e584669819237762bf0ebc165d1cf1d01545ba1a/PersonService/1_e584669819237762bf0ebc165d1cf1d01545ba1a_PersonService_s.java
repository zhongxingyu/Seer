 package pl.agh.enrollme.service;
 
 import org.primefaces.event.RowEditEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.repository.IPersonDAO;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class PersonService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
 
     @Autowired
     private IPersonDAO personDAO;
 
 	private static List<Person> cache = new ArrayList<Person>();
 
 	static {
 //		cache.add(new Person(0, "Jamie", "Carr"));
 //		cache.add(new Person(1, "Jean", "Cobbs"));
 //		cache.add(new Person(2, "John", "Howard"));
 //		cache.add(new Person(3, "John", "Mudra"));
 //		cache.add(new Person(4, "Julia", "Webber"));
 	}
 	
 	public List<String> suggestNames(String text) {
 		List<String> results = new ArrayList<String>();
 		for (int i = 0; i < 10; i++) {
 			results.add(text + i);
 		}
 		return results;
 	}
 
 	public List<Person> suggestPeople(String text) {
 		List<Person> results = new ArrayList<Person>();
 		for (Person p : cache) {
 			if ((p.getFirstName() + " " + p.getLastName()).toLowerCase().startsWith(text.toLowerCase())) {
 				results.add(p);
 			}
 		}
 		return results;
 	}
 
     public void onEdit(RowEditEvent event) {
         LOGGER.debug("Row edited");
         Person editedPerson = (Person)event.getObject();
 
         if (editedPerson != null) {
             LOGGER.debug("Updating person with id " + editedPerson.getId());
             personDAO.update(editedPerson);
         }
     }
 
     public void setEncodedPassword(Person person, String password) {
         LOGGER.debug("Jestem w setencodedpassword");
         PasswordEncoder encoder = new ShaPasswordEncoder(256);
         String encodedPassword = encoder.encodePassword(password, null);
         person.setPassword(encodedPassword);
     }
 
     public void setBooleans(Person person, Boolean enabled, Boolean credentialsNonExpired, Boolean accountNonExpired,
                             Boolean accountNonLocked) {
         person.setEnabled(enabled);
         person.setAccountNonExpired(accountNonExpired);
         person.setAccountNonLocked(accountNonLocked);
         person.setCredentialsNonExpired(credentialsNonExpired);
     }
 	
 }
