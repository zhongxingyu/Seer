 package pl.agh.enrollme.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Service;
 
 import pl.agh.enrollme.model.Person;
 
 @Service
 public class PersonService {
 
 	private static List<Person> cache = new ArrayList<Person>();
 	
 	static {
 		cache.add(new Person(0, "Jamie", "Carr"));
 		cache.add(new Person(1, "Jean", "Cobbs"));
 		cache.add(new Person(2, "John", "Howard"));
 		cache.add(new Person(3, "John", "Mudra"));
 		cache.add(new Person(4, "Julia", "Webber"));
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
 	
 }
