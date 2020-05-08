 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package com.newdawn.controllers;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.newdawn.model.colony.Colony;
 import com.newdawn.model.personnel.team.GeologicalTeam;
 import com.newdawn.model.ships.Squadron;
 import com.newdawn.model.system.Planet;
 import com.newdawn.model.system.Satellite;
 import com.newdawn.model.system.StellarSystem;
 
 /**
  * 
  * @author Pierrick Puimean-Chieze
  */
 @Component
 public class MainController {
 
 	private static final Logger LOGGER = Logger.getLogger(MainController.class
 			.getName());
 	@Autowired(required = true)
 	private GameData gameData;
 	@Autowired
 	private CelestialBodyMovementController celestialBodyMovementController;
 	@Autowired
 	private ShipMovementController shipMovementController;
 	@Autowired
 	private OrderController orderController;
 	@Autowired
 	private Config config;
 	@Autowired
 	private ColonyController colonyIncreaseController;
 	@Autowired
 	private GeologicalController geologicalController;
 
 	public Config getConfig() {
 		return config;
 	}
 
 	public void setConfig(Config config) {
 		this.config = config;
 
 	}
 
 	public OrderController getOrderController() {
 		return orderController;
 	}
 
 	public void setOrderController(OrderController orderController) {
 		this.orderController = orderController;
 	}
 
 	public ShipMovementController getShipMovementController() {
 		return shipMovementController;
 	}
 
 	public void setShipMovementController(
 			ShipMovementController shipMovementController) {
 		this.shipMovementController = shipMovementController;
 	}
 
 	/**
 	 * Get the value of celestialBodyMovementControlelr
 	 * 
 	 * @return the value of celestialBodyMovementControlelr
 	 */
 	public CelestialBodyMovementController getCelestialBodyMovementController() {
 		return celestialBodyMovementController;
 	}
 
 	/**
 	 * Set the value of celestialBodyMovementControlelr
 	 * 
 	 * @param celestialBodyMovementControlelr
 	 *            new value of celestialBodyMovementControlelr
 	 */
 	public void setCelestialBodyMovementController(
 			CelestialBodyMovementController celestialBodyMovementController) {
 		this.celestialBodyMovementController = celestialBodyMovementController;
 	}
 
 	/**
 	 * Get the value of gameData
 	 * 
 	 * @return the value of gameData
 	 */
 	public GameData getGameData() {
 		return gameData;
 	}
 
 	/**
 	 * Set the value of gameData
 	 * 
 	 * @param gameData
 	 *            new value of gameData
 	 */
 	public void setGameData(GameData gameData) {
 		this.gameData = gameData;
 	}
 
 	public final void runIncrements(long totalLength) {
 		LOGGER.entering(MainController.class.getName(), "runIncrements",
 				totalLength);
 		final int subPulse = getConfig().getSubPulse();
 
 		if (totalLength < subPulse) {
 			throw new IllegalArgumentException(
 					"Total Length inferior to the selected Sub pulse");
 		}
 
 		if (totalLength % subPulse != 0) {
 			throw new IllegalArgumentException(
 					"Total Length not dividable by the selected Sub Pulse");
 		}
 
 		long totalIncrements = totalLength / subPulse;
 
 		runIncrements(totalIncrements, subPulse);
 		LOGGER.exiting(MainController.class.getName(), "runIncrements");
 
 	}
 
 	private void runIncrements(long increments, long subPulse) {
 		LOGGER.entering(MainController.class.getName(), "runIncrements",
 				new Object[] { increments, subPulse });
 		LOGGER.log(Level.FINEST, "Running {0} increments", increments);
 		for (int i = 0; i < increments; i++) {
 			LOGGER.finest("Running increment " + i);
 			runSubPulse(subPulse);
 		}
 		LOGGER.exiting(MainController.class.getName(), "runIncrements");
 	}
 
	//TODO use an int for the subpulse duration
 	private void runSubPulse(long second) {
 		LOGGER.entering(MainController.class.getName(), "runSubPulse",
 				new Object[] { second });
 		for (StellarSystem system : gameData.getStellarSystems()) {
 
 			for (Planet planet : system.getPlanets()) {
 				getCelestialBodyMovementController().moveBody(second, planet);
 				for (Satellite satellite : planet.getSatellites()) {
 					getCelestialBodyMovementController().moveBody(second,
 							satellite);
 				}
 			}
 
 			for (Squadron taskGroup : system.getSquadrons()) {
 				getOrderController().startExecutionOrder(taskGroup);
 				getShipMovementController().moveTaskGroup(second, taskGroup);
 				getOrderController().updateOrders(taskGroup);
 			}
 
 			for (Colony colony : system.getColonies()) {
 				colonyIncreaseController.calculateColonyPopulationAndWealth(
 						colony, second);
 			}
 		}
 		for (GeologicalTeam geologicalTeam : gameData.getGeologicalTeams()) {
			
			geologicalController.runProspection(geologicalTeam, (int)second);
 		}
 		LOGGER.exiting(MainController.class.getName(), "runSubPulse");
 	}
 }
