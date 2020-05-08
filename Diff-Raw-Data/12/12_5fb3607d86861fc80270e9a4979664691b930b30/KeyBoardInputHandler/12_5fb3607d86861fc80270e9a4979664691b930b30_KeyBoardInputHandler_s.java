 package com.github.joakimpersson.tda367.controller.input;
 
 import org.newdawn.slick.Input;
 
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class KeyBoardInputHandler implements InputHandler {
 
 	private PlayerAction currentAction;
 	private Player player = null;
 	private int[] keyMap;
 
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
 		this.keyMap = DefaultKeyMappings.getInstance().getKeyMap(id);
 	}
 
 	public KeyBoardInputHandler(int id) {
 		this.keyMap = DefaultKeyMappings.getInstance().getKeyMap(id);
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
 	public KeyBoardInputHandler(Player player, int[] keyMap) {
 		this.player = player;
 		this.keyMap = keyMap;
 	}
 
 	/**
 	 * 
 	 * Some stupid documentation
 	 * 
 	 * @precondition keyCodes.length == 6
 	 * @precondition keyCodes[0] is Move North
 	 * @precondition keyCodes[1] is Move South
 	 * @precondition keyCodes[2] is Move West
 	 * @precondition keyCodes[3] is Move East
 	 * @precondition keyCodes[4] is Primary Action
 	 * @precondition keyCodes[5] is Secondary Action
 	 * 
 	 * @param keyCodes
 	 *            An array of keyboardscodes
 	 * @return A map with the input key as key and the action as value
 	 */
 	@Override
 	public boolean hasKey(Input input) {
 		if (input.isKeyDown(keyMap[4])) {
 			currentAction = PlayerAction.PRIMARY_ACTION;
 			return true;
 		}
 		if (input.isKeyDown(keyMap[5])) {
 			currentAction = PlayerAction.SECONDARY_ACTION;
 			return true;
 		}
 		
 		if (input.isKeyDown(keyMap[0])) {
 			currentAction = PlayerAction.MOVE_NORTH;
 			if (input.isKeyDown(keyMap[2]))
 				currentAction = PlayerAction.MOVE_NORTHWEST;
 			else if (input.isKeyDown(keyMap[3]))
 				currentAction = PlayerAction.MOVE_NORTHEAST;
 			return true;
 		} else if (input.isKeyDown(keyMap[1])) {
 			currentAction = PlayerAction.MOVE_SOUTH;
 			if (input.isKeyDown(keyMap[2]))
 				currentAction = PlayerAction.MOVE_SOUTHWEST;
 			else if (input.isKeyDown(keyMap[3]))
 				currentAction = PlayerAction.MOVE_SOUTHEAST;
 			return true;
 		} else if (input.isKeyDown(keyMap[2])) {
 			currentAction = PlayerAction.MOVE_WEST;
 			return true;
 		} else if (input.isKeyDown(keyMap[3])) {
 			currentAction = PlayerAction.MOVE_EAST;
 			return true;
 		} 
 		return false;
 	}
 
 	@Override
 	public InputData getData(Input input) {
 		if (hasKey(input)) {
 			return new InputData(player, currentAction);
 		}
 		return new InputData(player, PlayerAction.DO_NOTHING);
 	}
 
 	@Override
 	public InputData getMenuInputData(Input input) {
 		PlayerAction menuAction = PlayerAction.DO_NOTHING;
 		if (input.isKeyPressed(keyMap[0])) {
 			menuAction = PlayerAction.MOVE_NORTH;
 		} else if (input.isKeyPressed(keyMap[1])) {
 			menuAction = PlayerAction.MOVE_SOUTH;
 		} else if (input.isKeyPressed(keyMap[2])) {
 			menuAction = PlayerAction.MOVE_WEST;
 		} else if (input.isKeyPressed(keyMap[3])) {
 			menuAction = PlayerAction.MOVE_EAST;
 		} else if (input.isKeyPressed(keyMap[4])) {
 			menuAction = PlayerAction.PRIMARY_ACTION;
 		}
 		if (!menuAction.equals(PlayerAction.DO_NOTHING)) {
 			return new InputData(player, menuAction);
 		}
 		return new InputData(player, PlayerAction.DO_NOTHING);
 	}
 
 	@Override
 	public Player getPlayer() {
 		return player;
 	}
 
 	@Override
 	public boolean pressedProceed(Input input) {
 		int proceedButton = DefaultKeyMappings.getInstance().getProceedButton();
 
 		return input.isKeyPressed(proceedButton);
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
				&& this.keyMap.equals(other.keyMap);
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 
 		for (Integer i : keyMap) {
 			sum += i * 13;
 		}
 
 		sum += currentAction.hashCode() * 7;
 
 		sum += player.hashCode() * 5;
 
 		return sum;
 	}
 }
