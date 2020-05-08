 package fi.helsinki.cs.oato;
 
 import fi.helsinki.cs.oato.gui.MainGUI;
 
 public class GuiDebug {
 
 	/**
 	 * Runs the GUI with given data.
 	 * 
 	 * @param args null
 	 */
 	public static void main(String[] args) {
		MainGUI m = new MainGUI();
 		m.addEvent( EventFixtures.createDentistAppointment() );
 		m.addEvent( EventFixtures.createSoftwareEngineeringLecture() );

 	}
 
 }
