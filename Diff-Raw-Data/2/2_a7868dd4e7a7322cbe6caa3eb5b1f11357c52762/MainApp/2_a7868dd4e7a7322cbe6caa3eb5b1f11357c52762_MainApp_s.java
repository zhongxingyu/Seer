 package master;
 
 import petrinet.logic.Petrinet;
 
 public class MainApp
 {
     public static void main(String[] args) {
         Petrinet pn = new Petrinet("My Net");
 
 		//data.xml is the default petrinet file to load on start
         pn.setFilepath("src/default.xml");
 		// At this point our importer should be modifying the existing pn Petrinet object
		@SuppressWarnings("unused")
 		Importer importer = new Importer(pn.getFilepath(), pn);
 		
 
         System.out.println("\nTransitions: " + pn.getTransitions().toString());
         System.out.println("Places: " + pn.getPlaces().toString());
 		System.out.println("Arcs: " + pn.getArcs().toString());
 
         petrinet.gui.PetrinetGUI.displayPetrinet(pn);
     }
 }
