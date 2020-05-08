 package com.github.joakimpersson.tda367.controller.input;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Input;
 
 import com.github.joakimpersson.tda367.model.constants.PlayerAction;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class DefaultKeyMappings {
 	private static DefaultKeyMappings instance = null;
 	private List<Map<Integer, PlayerAction>> mappings = null;
 	private ArrayList<PlayerAction> actions = null;
 
 	public static DefaultKeyMappings getInstance() {
 		if (instance == null) {
 			instance = new DefaultKeyMappings();
 		}
 		return instance;
 	}
 
 	private DefaultKeyMappings() {
 		init();
 	}
 
 	private void init() {
 		mappings = new ArrayList<Map<Integer, PlayerAction>>();
 		actions = new ArrayList<PlayerAction>();
 
 		for (PlayerAction pa : PlayerAction.values()) {
 			actions.add(pa);
 		}
 
 		int[][] keyCodes = {
 				{ Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT,
 						Input.KEY_RIGHT, Input.KEY_SPACE },
 				{ Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D,
 						Input.KEY_F } };
 		for (int i = 0; i < 2; i++) {
 			mappings.add(createKeyMap(keyCodes[i]));
 		}
 	}
 
 	/**
 	 * 
 	 * Some stupid documentation
 	 * 
 	 * @precondition keyCodes.length == 5
 	 * @precondition keyCodes[0] is Move Up
 	 * @precondition keyCodes[1] is Move Down
 	 * @precondition keyCodes[2] is Move Left
 	 * @precondition keyCodes[3] is Move Right
 	 * @precondition keyCodes[4] is PlaceBomb/Action
 	 * 
 	 * @param keyCodes
 	 *            An array of keyboardscodes
 	 * @return A map with the input key as key and the action as value
 	 */
 	public Map<Integer, PlayerAction> createKeyMap(int[] keyCodes) {
 		Map<Integer, PlayerAction> tmp = new HashMap<Integer, PlayerAction>();
 
 		int index = 0;
 		for (Integer i : keyCodes) {
 			tmp.put(i, actions.get(index));
 			index++;
 		}
 
 		return tmp;
 	}
 
 	public Map<Integer, PlayerAction> getKeyActionMap(int index) {
 		int i = index % mappings.size();
 		return mappings.get(i);
 	}
 
	public Integer getProceedButton() {
 		return Input.KEY_ENTER;
 	}
 }
