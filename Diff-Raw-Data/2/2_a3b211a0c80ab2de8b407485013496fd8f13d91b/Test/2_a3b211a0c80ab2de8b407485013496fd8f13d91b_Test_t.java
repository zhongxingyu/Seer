 package Model;
 
 import java.util.ArrayList;
 
 public class Test {
 
 	//Just a class to unit test functionality of the other classes
 	
 	public static void main(String[] args) {
 		
 		//create airport and 4 runways
 		Airport a = new Airport("Heathrow");
 		Runway r = new Runway("27R", 1, 2, 3, 4, 5);
 		Runway r1 = new Runway("09L", 6, 7, 8, 9, 10);
 		Runway r2 = new Runway("27L", 11, 12, 13, 14, 15);
 		Runway r3 = new Runway("09R", 16, 17, 18, 19, 20);
 		
 		PhysicalRunway one = new PhysicalRunway("27R/09L", r, r1);
 		PhysicalRunway two = new PhysicalRunway("27L/09R", r2, r3);
 		
 		//add physical runways to airport
 		a.addPhysicalRunway(one);
 		a.addPhysicalRunway(two);
 		
 		
 		//iterate over the runways in the airport and print all values
 		System.out.println("\n******************\nThis is the airport and its runways:\n" + a.getName());
 		for (Object o : a.getPhysicalRunways()) {
 			System.out.println(((PhysicalRunway) o).getId() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getName() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getLDA(1)
 	
 					+" "+ ((PhysicalRunway) o).getRunway(1).getName()
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getLDA(1)
 					
 					);
 		}
 		
 		//remove runway with name "one"
 		a.removePhysicalRunway("27R/09L");
 		
 		System.out.println("\nThis is the same airport with runway one removed:\n" + a.getName());
 		//iterate over the runways in the airport and print all values
 		for (Object o : a.getPhysicalRunways()) {
 			System.out.println(((PhysicalRunway) o).getId() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getName() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getLDA(1)
 	
 					+" "+ ((PhysicalRunway) o).getRunway(1).getName()
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getLDA(1)
 					
 					);
 		}
 		
 		//save airport a to xml file
 		a.saveToXML();
 		
 		//create a second airport and a loadxmlfile object
 		//load a file
 		Airport a2 = null;
 		LoadXMLFile lf = new LoadXMLFile();
 		try {
 			a2 = lf.loadAirport();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("\nThis is the loaded airport:\n" + a2.getName());
 		//iterate over the runways in the loaded airport and print all values
 		
 		for (Object o : a2.getPhysicalRunways()) {
 			System.out.println(((PhysicalRunway) o).getId() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getName() 
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(0).getLDA(1)
 	
 					+" "+ ((PhysicalRunway) o).getRunway(1).getName()
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTORA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getASDA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getTODA(1)
 					+" "+ ((PhysicalRunway) o).getRunway(1).getLDA(1)
 					
 					);
 		}
 		
 		Obstacle obs = new Obstacle("boeing 747", 56.0);
 		obs.saveToXML();
 		
 		LoadXMLFile lof = new LoadXMLFile();
 		Obstacle obs1 = null;
 		
 		lof.silentLoadObstacle("/Users/oscarmariani/Desktop/obstacle.xml");
 		
 		try {
 			obs1 = lof.loadObstacle();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println(obs1.getName() +
 				obs1.getHeight() + obs1.getWidth() + obs1.getLength());
 		
 		Contact cont1 = new Contact("oscar", "mariani", "mariani.oscar@gmail.com");
 		Contact cont2 = new Contact("bob", "squarepants", "squarepants@gmail.com");
 		ArrayList<Contact> contactsList = new ArrayList<Contact>();
 		contactsList.add(cont1);
 		contactsList.add(cont2);
 		
 		try {
			SaveToXMLFile saveTo = new SaveToXMLFile(contactsList, false);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		ArrayList<Contact> conts = null;
 		LoadXMLFile lof1 = new LoadXMLFile();
 		try {
 			conts = lof1.loadContacts();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(conts.get(0).getFirstName() + conts.get(0).getLastName() + conts.get(0).getEmail());
 		System.out.println(conts.get(1).getFirstName() + conts.get(1).getLastName() + conts.get(1).getEmail());
 		
 	}
 
 }
