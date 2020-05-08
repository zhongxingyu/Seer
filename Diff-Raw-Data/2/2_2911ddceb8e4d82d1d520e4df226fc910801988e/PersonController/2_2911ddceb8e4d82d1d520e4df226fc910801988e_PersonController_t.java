 package de.htwg.seapal.person.controllers.mock;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import de.htwg.seapal.person.models.IPerson;
 import de.htwg.seapal.person.controllers.AbstractPersonController;
 
 @Singleton
 public class PersonController extends AbstractPersonController {
 	
 	@Inject
 	public PersonController(IPerson person) {
		super.setPerson(person);
 	}
 
 	@Override
 	public Map<String, String> getPersonList() {
 		
 		Map<String,String> personMap = new HashMap<String,String>();
 		personMap.put("PERSON-1", "Hans Müller");
 		personMap.put("PERSON-2", "Peter Maier");
 		personMap.put("PERSON-3", "Friedrich Schiller");
 		
 		return personMap;
 	}
 	
 	@Override
 	public String getPersonFirstname(String personId) {
 		return "Hans";
 	}
 
 	@Override
 	public void setPersonFirstname(String personId,String firstname) {}
 
 	@Override
 	public String getPersonLastname(String personId) {
 		return "Müller";
 	}
 
 	@Override
 	public void setPersonLastname(String personId, String lastname) {}
 
 	@Override
 	public void addPerson() {
 	}
 
 }
