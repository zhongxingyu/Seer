 package gui;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import logic.*;
 import logic.algorithm.FiFoAlgorithm;
 import logic.algorithm.PickUpFifoAlgorithm;
 import definition.*;
 import gui.FrameMain;
 
 /**
  * 
  * @author BFH-Boys
  *
  */
 public class main {
 
 	// Logger
 	static Logger log4j = Logger.getLogger("ch.bfh.proj1.elevator.gui");
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws SAXException 
 	 */
 	public static void main(String[] args) {
 		runTestConfig();
 		//runApplication();
 	}
 	
 	
 	public static void runTestConfig(){
 		testGui();
 	}
 	
 	public static void testGui(){
 		try {
 			Building building = new Tower(new Elevator(1, 12, 10, 1));
			building.addElevator(new Elevator(1, 3, 8, 3,20f,0.5f));
			building.addElevator(new Elevator(1, 3, 6,2,60f,2f));
 			//building.addElevator(new Elevator(-1, 6,12,4,30f,1f));
 			//building.addElevator(new Elevator(-1, 6,4,4,20f,0.2f));
 			//building.addElevator(new Elevator(-1, 6, 2,5));
 			//building.addElevator(new Elevator(-1, 6, 12,5));
 			building.setSimulationSpeed(10);
 
 //			
 //			Building building = new Tower(new Elevator(1, 3, Integer.MAX_VALUE, 1));
 //			building.addElevator(new Elevator(1, 3, Integer.MAX_VALUE, 3));
 //			building.addElevator(new Elevator(1, 3, Integer.MAX_VALUE,3));
 //			building.addElevator(new Elevator(-1, 6, Integer.MAX_VALUE,5));
 //			building.addElevator(new Elevator(-1, 6, Integer.MAX_VALUE,5));
 //			building.addElevator(new Elevator(-1, 6, Integer.MAX_VALUE,5));
 //			building.addElevator(new Elevator(-1, 6, Integer.MAX_VALUE,5));
 			
 
			//ElevatorController controller = new ElevatorController(building, FiFoAlgorithm.class);
			ElevatorController controller = new ElevatorController(building, PickUpFifoAlgorithm.class);
 			controller.startController();
 
 			Thread t = new Thread(new FrameMain(building, controller));
 			t.start();
 		} catch (Exception ex) {
 			log4j.error(ex);
 		}
 	}
 
 }
