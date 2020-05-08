 package OOP;
 
 /**
 * &uuml;berklasse f&uuml;r Warehouse, damit in Order f&uuml;r from und to auch andere Klassen 
  * wie Kunde oder Lieferant(die zur zeit nicht existieren) benutzt werden
 * knnen.
  * @author sebastian
  *
  */
public class Location {
 	protected String name;
 	
 	public Location(String name) {
 		this.setName(name);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 }
