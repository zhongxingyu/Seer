 package com.github.joakimpersson.tda367.controller.input;
 
 import org.newdawn.slick.Input;
 
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * An interface for all input methods in the game for example via keyboard or
  * via a controller
  * 
  * @author joakimpersson
  * 
  */
 public interface InputHandler {
 
 	/**
 	 * Checks if the current inputhandler contains the key/action that was
 	 * performed
 	 * 
 	 * @param input
 	 *            The input method used by the slick framework that contains the
 	 *            latest action
 	 * @return True if the handler contains the key and false otherwise
 	 */
 	boolean hasKey(Input input);
 
 	/**
 	 * Returns an inputdata object corresponding the lates input call. It is
 	 * strongly recommended to call the haskey() first
 	 * 
 	 * @precondition hasKey() == true
 	 * @param input
 	 *            The input object used by the slick framework that contains the
 	 *            latest action
 	 * @return An InputData object based on the current action
 	 */
 	InputData getData(Input input);
 
 	/**
 	 * Returns the player that is connected to the input handler
 	 * 
 	 * @return The inputhandlers player
 	 */
 	Player getPlayer();
 
 	/**
	 * Checks if  the player has pressed their proceed button
 	 * 
 	 * @param input
 	 *            The input object used by the slick framework that contains the
 	 *            latest action
	 * @return True if one of the player has pressed their proceed button,
	 *         false otherwise
 	 */
 	boolean pressedProceed(Input input);
 
 	InputData getMenuInputData(Input input);
 }
