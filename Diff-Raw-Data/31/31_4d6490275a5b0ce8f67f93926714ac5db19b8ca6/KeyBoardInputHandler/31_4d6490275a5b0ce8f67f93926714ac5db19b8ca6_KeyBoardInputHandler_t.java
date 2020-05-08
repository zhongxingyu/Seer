 package com.github.joakimpersson.tda367.controller.input;
 
 import java.util.Map;
 
 import org.newdawn.slick.Input;
 
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class KeyBoardInputHandler implements InputHandler {
 
 	private int currentKey = -1;
 	private Player player;
 	private Map<Integer, PlayerAction> keyActionMap;
 
 	/**
 	 * Creates a new keyboard inputhandler using one of the default keymappings
 	 * 
 	 * @param player
 	 *            The player responsible for the actions
 	 * @param id
 	 *            The id of the player that is used for mapping it to a default
 	 *            keyboard
 	 */
 	public KeyBoardInputHandler(Player player, int id) {
 		this.player = player;
 		this.keyActionMap = DefaultKeyMappings.getInstance()
 				.getKeyActionMap(id);
 	}
 
 	/**
 	 * 
 	 * Creates a new keyboard inputhandler with an custom keyboard mapping
 	 * 
 	 * @param player
 	 *            The player responsible for the actions
 	 * @param keyActionMap
 	 *            The players custom made keymapping
 	 */
 	public KeyBoardInputHandler(Player player,
 			Map<Integer, PlayerAction> keyActionMap) {
 		this.player = player;
 		this.keyActionMap = keyActionMap;
 	}
 
 	@Override
 	public boolean hasKey(Input input) {
 		for (Integer i : keyActionMap.keySet()) {
 			if (input.isKeyDown(i)) {
 				currentKey = i;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public InputData getData(Input input) {
 		if (hasKey(input)) {
 			PlayerAction action = keyActionMap.get(currentKey);
 
 			return new InputData(player, action);
 		}
 		return null;
 	}
 
 	@Override
 	public Player getPlayer() {
 		return player;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 
 		KeyBoardInputHandler other = (KeyBoardInputHandler) obj;
 		return this.player.equals(other.player)
 				&& this.keyActionMap.equals(other.keyActionMap);
 	}
 
 	@Override
	public boolean pressedProceed(Input input) {
		int proceedButton = DefaultKeyMappings.getInstance().getProceedButton();
 		
		return input.isKeyPressed(proceedButton);
 	}
 }
