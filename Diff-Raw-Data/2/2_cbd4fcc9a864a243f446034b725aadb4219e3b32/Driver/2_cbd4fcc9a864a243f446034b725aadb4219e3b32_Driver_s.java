 /*This file holds the class Driver, which holds the main method
  * for SpaceFarmer3000
  */
 package app.util;
 
 import app.factory.UniverseFactory;
 import app.model.Game;
 import app.view.Display;
 import conf.GameVariables;
 import conf.PlanetNames;
 import conf.SystemNames;
 
 import java.awt.*;
 
 /**
  * This class starts the SpaceFarmer game; it has the program's main method.
  * 
  * @author Mark McDonald, Andrew Wilder
  * @version 1.0
  */
public class driver {
 
 	/**
 	 * Launch the application.
 	 * @param args Unused
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					// There is a singleton Game (holds all information) and
 					// Display (Holds all the views and the layout
 					// configuration)s.
 					final Display frame = new Display();
 					frame.setVisible(true);
 					final Game game = new Game();
 					Display.setGame(game);
 					UniverseFactory.createUniverse(
 							PlanetNames.getPlanetNamesAsList(),
 							SystemNames.getSystemNamesAsList(),
 							GameVariables.NUM_PLANETS,
 							GameVariables.NUM_PLANETARY_SYSTEMS,
 							GameVariables.SYSTEM_ROWS,
 							GameVariables.SYSTEM_COLUMNS,
 							GameVariables.UNIVERSE_ROWS,
 							GameVariables.UNIVERSE_COLUMNS,
 							GameVariables.QUADRANT_X_DIMENSION,
 							GameVariables.QUADRANT_Y_DIMENSION,
 							GameVariables.MINIMUM_SYSTEM_DISTANCE);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * @return Information about this object as a String.
 	 */
 	public String toString() {
 		return "Driver";
 	}
 }
