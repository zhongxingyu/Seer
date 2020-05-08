 /**
  * Hauptklasse des Memoryspiels.
  * Implementiert Einsprungspunkt, erzeugt Model, View und Controller
  * und startet Spiel
  * 
  * @author Mathias Garbe
  *
  */
 
 public class Memory {
 	/**
 	 * Einsprungspunkt.
 	 * 
 	 * @param argv Nicht benutzt
 	 */
 	public static void main(String argv[])
 	{	
 		MemoryModel model = new MemoryModel(140);
 		MemoryView view = new MemoryView(model);
 		MemoryController controller = new MemoryController(model, view);
 		
		controller.starteSpiel();
 	}
 }
