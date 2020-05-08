 package base;
 
 import java.util.HashMap;
 import java.util.Collection;
 
 import test.TestLoader;
 
 /**
 * Organizations are the top level component of the SeatApp.  Organizations
 * are made up of a set of areas and a set of people.
 */
 public class Organization {
 	private HashMap<String,Area> areaHash;
 	private HashMap<String,Person> personHash;
 	private String name;
 
 	/**
 	* Initializes a new empty Organization.
 	*/
 	public Organization(String name) {
 		areaHash = new HashMap<String,Area>();
 		personHash = new HashMap<String,Person>();
 		this.name = name;
 	}
 	
 	/**
 	* Uses an {@link OrgLoader} to create an Organization with predefined Areas
 	* and People.
 	*/
 	public Organization(OrgLoader ol) {
 		this(ol.getName());
 		while(!ol.areaStackEmpty()) {
 			addArea(ol.popArea());
 		}
 		while(!ol.personStackEmpty()) {
 			addPerson(ol.popPerson());
 		}
 	}
 	
 	/**
 	*  Places a person into an area based on a person key and an area key. This
 	*  method can't be used to specify a specific chair in the selected Area.
 	*/
 
 	public void seatPerson(String user_name, String area_name) {
 		if(areaExists(area_name)) {
 			Area area = areaHash.get(area_name);
 			Person person = personHash.get(user_name);
 			Seat s = area.findEmptySeat();
 			s.setOccupant(person);
 		}
 	}
 	
 	/**
 	*	Adds a person to the Organization.
 	*/
 	public void addPerson(String name) {
 		addPerson(new Person(name));
 	}
 
 	public void addPerson(Person person) {
 		if(!personHash.containsKey(person.getUserName())) {
 			personHash.put(person.getUserName(), person);
 		}
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	*	
 	*/
 	public Area getArea(String area_name) {
 		Area result = null;
 		if(areaHash.containsKey(area_name)) {
 			result = areaHash.get(area_name);
 		}
 		return result;
 	}
 	
 	public Collection<Person> getPeople() {
 		return personHash.values();
 	}
 	
 	public Collection<Area> getAreas() {
 		return areaHash.values();
 	}
 	
 	public void addArea(Area area) {
 		areaHash.put(area.getName(), area);
 	}
 	
 	public boolean areaExists(String area_name) {
 		return areaHash.containsKey(area_name);
 	}
 
 	public Area removeArea(String area_name) {
 		Area result = null;
 		if (areaHash.containsKey(area_name)) {
 			result = areaHash.remove(area_name);
 		}
 		return result;
 	}
 
 	public Person removePerson(String user_name) {
 		Person result = null;
 		if (personHash.containsKey(user_name)) {
 			result = personHash.remove(user_name);
 		}
 		return result;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public String toString() {
 		String result = "";
 		result = name+"\n\nPeople:\n";
 		for (Person p : personHash.values()) {
 			result += p+"\n";
 		}
 		result += "\nAreas:\n";
 		for (Area a : areaHash.values()) {
 			result += a+"\n";
 		}
 		return result;
 	}

 	public static void main (String args[]) {
 		Organization org = new Organization(new TestLoader());
 		System.out.print(org);
	}	
 }
