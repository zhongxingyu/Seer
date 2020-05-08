 package com.github.joakimpersson.tda367.controller.input;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Input;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class InputManager {
 	private static InputManager instance = null;
 	private List<InputHandler> inputHandlers = null;
 	private boolean defaultEnabled = false;
 
 	private InputManager() {
 		init();
 	}
 
 	/**
 	 * Init the inputmanager
 	 */
 	private void init() {
 		inputHandlers = new ArrayList<InputHandler>();
 		createDeafaultInputs();
 	}
 
 	private void createDeafaultInputs() {
 		defaultEnabled = true;
 		inputHandlers = new ArrayList<InputHandler>();
 		for (int i = 0; i < 4; i++) {
 			inputHandlers.add(new X360InputHandler(i));
 		}
 		for (int i = 0; i < 2; i++) {
 			inputHandlers.add(new KeyBoardInputHandler(i));
 		}
 	}
 
 	/**
 	 * Get the games central inputmangers single instance
 	 * 
 	 * @return the inputmanager instance
 	 */
 	public static InputManager getInstance() {
 		if (instance == null) {
 			instance = new InputManager();
 		}
 		return instance;
 	}
 
 	/**
 	 * Adds an inputhandler to the list of active inputhandlers in the game. If
 	 * a player that is already connected to a inputhandler is connected to a
 	 * new inputhandler the previous one is replaced with the new one.
 	 * 
 	 * @param inputObj
 	 */
 	public void addInputObject(InputHandler inputObj) {
 		if (defaultEnabled) {
 			this.inputHandlers.clear();
 			this.defaultEnabled = false;
 		}
 		if (!playerIsUsedPrev(inputObj) && !inputHandlers.contains(inputObj)) {
 			inputHandlers.add(inputObj);
 		} else if (playerIsUsedPrev(inputObj)) {
 			int index = getPrevPlayerIndex(inputObj);
 			inputHandlers.set(index, inputObj);
 		}
 	}
 
 	/**
 	 * Checks if a player of the new input handler object is already associated
 	 * with a already used input handler
 	 * 
 	 * @param inputObj
 	 *            A new input handler
 	 * @return True if the player is associated with a previous inputhandler
 	 */
 	private boolean playerIsUsedPrev(InputHandler inputObj) {
 		return getPrevPlayerIndex(inputObj) > 0;
 	}
 
 	/**
 	 * Iterates over the collection of inputhandlers and searches after
 	 * inputhandlers that contains the same player as the new inputhandler
 	 * object
 	 * 
 	 * @param inputObj
 	 *            A new input handler
 	 * @return An int > 0 if an inputhandler contains the same player as an
 	 *         argument An int < 0 if it didn't find a player
 	 */
 	private int getPrevPlayerIndex(InputHandler inputObj) {
 		Player newPlayer = inputObj.getPlayer();
 
 		for (InputHandler handler : inputHandlers) {
 			Player oldPlayer = handler.getPlayer();
 			if (oldPlayer.equals(newPlayer)) {
 				return inputHandlers.indexOf(handler);
 			}
 		}
 		return -1;
 
 	}
 
 	/**
 	 * Delets the supplied inputhandler from the list of active inputhandlers
 	 * 
 	 * @param inputObj
 	 *            The object to be deleted
 	 */
 	public void removeInputObject(InputHandler inputObj) {
 		inputHandlers.remove(inputObj);
 	}
 
 	/**
 	 * Returns data from the inputhandlers where there has been performed an
 	 * action
 	 * 
 	 * @param input
 	 *            The input object used by the slick framework that contains the
 	 *            latest action
 	 * @return A list of all the performed actions
 	 */
 	public List<InputData> getData(Input input) {
 		List<InputData> dataList = new ArrayList<InputData>();
 		for (InputHandler handler : inputHandlers) {
 			if (handler.hasKey(input)) {
 				InputData data = handler.getData(input);
 
 				dataList.add(data);
 			}
 		}
 		return dataList;
 	}
 
 	public List<InputData> getMenuInputData(Input input) {
 		List<InputData> dataList = new ArrayList<InputData>();
 		for (InputHandler handler : inputHandlers) {
 			InputData data = handler.getMenuInputData(input);
 
 			dataList.add(data);
 		}
 		return dataList;
 	}
 
 	/**
 	 * Checks if one of the players has pressed their proceed button
 	 * 
 	 * @param input
 	 *            The input object used by the slick framework that contains the
 	 *            latest action
 	 * @return True if one of the players has pressed their proceed button,
 	 *         false otherwise
 	 */
 	public boolean pressedProceed(Input input) {
 		for (InputHandler handler : inputHandlers) {
 			if (handler.pressedProceed(input)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void removeAllInputHandlers() {
 		inputHandlers.clear();
 		createDeafaultInputs();
 	}
 
 	public int getAssignedControllers() {
 		if (defaultEnabled) {
 			return inputHandlers.size() - 6;
 		} else {
 			return inputHandlers.size();
 		}
 	}
 }
