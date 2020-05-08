 package OOP;
 
 /**
 * Überklasse für Warehouse, damit in Order für from und to auch andere Klassen 
  * wie Kunde oder Lieferant(die zur zeit nicht existieren) benutzt werden
 * können.
 * 
  * @author sebastian
  *
  */
public class Location extends StorageManager {
	private static final long serialVersionUID = 7158711736952755227L;
	
 	protected String name;
 	
 	public Location(String name) {
		super();
		
 		this.setName(name);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 }
