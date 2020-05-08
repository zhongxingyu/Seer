 package com.github.joakimpersson.tda367.model.player;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.GameModeType;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 
 /**
  * A class handling all the attributes for a player.
  * 
  * @author joakimpersson
  * @modified adderollen
  * 
  */
 public class PlayerAttributes {
 
 	/** Every Attribute is associated with an integer or level */
 	private Map<Attribute, Integer> matchAttr;
 	private Map<Attribute, Integer> roundAttr;
 
 	/**
 	 * Create a new player attribute object and instantiate the default values
 	 * for the match and round enummap
 	 */
 	public PlayerAttributes() {
 
 		matchAttr = new EnumMap<Attribute, Integer>(Attribute.class);
 		roundAttr = new EnumMap<Attribute, Integer>(Attribute.class);
 
 		this.initDefaultMatchValues();
 		this.initDefaultRoundValues();
 	}
 
 	/**
 	 * Creates a clone of a PlayerAttributes object.
 	 * 
 	 * @param player
 	 *            The PlayerAttributes object that is to be cloned.
 	 */
 	public PlayerAttributes(PlayerAttributes playerAttributes) {
 		this.matchAttr = new EnumMap<Attribute, Integer>(
 				playerAttributes.matchAttr);
 		this.roundAttr = new EnumMap<Attribute, Integer>(
 				playerAttributes.roundAttr);
 	}
 
 	/**
 	 * Upgrade either an round or match attribute with one level
 	 * 
 	 * @param attribute
 	 *            The attribute to be upgraded
 	 * @param type
 	 *            The type of the upgrade
 	 */
 	public void upgradeAttr(Attribute attribute, GameModeType type) {
 		switch (type) {
 		case Round:
 			updateRoundAttr(attribute);
 			break;
 		case Match:
 			updateMatchAttr(attribute);
 			break;
 		default:
 			// Should not happen!
 			break;
 		}
 	}
 
 	/**
 	 * Upgraded an match attribute with one level
 	 * 
 	 * @param attribute
 	 *            The attribute to be upgraded
 	 */
 	private void updateMatchAttr(Attribute attribute) {
 		if (matchAttr.containsKey(attribute)) {
 			int value = matchAttr.get(attribute) + 1;
 			matchAttr.put(attribute, value);
 
 		} else {
 			matchAttr.put(attribute, 1);
 		}
 	}
 
 	/**
 	 * Upgraded an round attribute with one level
 	 * 
 	 * @param attribute
 	 *            The attribute to be upgraded
 	 */
 	private void updateRoundAttr(Attribute attribute) {
 		int value = roundAttr.get(attribute) + 1;
 		roundAttr.put(attribute, value);
 	}
 
 	/**
 	 * Get an attributes level, higher is better
 	 * 
 	 * @param attribute
 	 *            The attribute which value you want
 	 * @return The attributes level or 0 if the specified attribute is not
 	 *         activated
 	 */
 	public int getAttrValue(Attribute attribute) {
 
 		int roundAttributeValue = roundAttr.get(attribute) == null ? 0
 				: roundAttr.get(attribute);
 		int matchAttributeValue = matchAttr.get(attribute) == null ? 0
 				: matchAttr.get(attribute);
 
 		return matchAttributeValue + roundAttributeValue;
 	}
 
 	/**
 	 * 
 	 * After either a round or a match reset the players attribute to its
 	 * standard values
 	 * 
 	 * @param type
 	 *            What kind of reset of the attribute is it
 	 */
 	public void resetAttr(GameModeType type) {
 		switch (type) {
 		case Match:
 			resetMatchAttr();
 			resetRoundAttr();
 			break;
 		case Round:
 			resetRoundAttr();
 			break;
 		default:
 			// Should not happen!
 			break;
 		}
 	}
 
 	/**
 	 * Reset the match map to its standard values
 	 */
 	private void resetMatchAttr() {
 		initDefaultMatchValues();
 	}
 
 	/**
 	 * Reset the round map to its standard values
 	 */
 	private void resetRoundAttr() {
 		initDefaultRoundValues();
 	}
 
 	/**
 	 * Set the match map values to the standard values from the Parameter class
 	 */
 	private void initDefaultMatchValues() {
		Parameters p = Parameters.INSTANCE;
		matchAttr.put(Attribute.Speed, p.getInitSpeed());
		matchAttr.put(Attribute.BombStack, p.getStartingBombs());
		matchAttr.put(Attribute.Health, p.getInitHealth());
		matchAttr.put(Attribute.BombRange, p.getInitBombRange());
		matchAttr.put(Attribute.BombPower, p.getInitBombPower());
 		matchAttr.put(Attribute.AreaBombs, 0);
 	}
 
 	/**
 	 * Set the round map values to the standard values which is zero
 	 */
 	private void initDefaultRoundValues() {
 
 		roundAttr.put(Attribute.Speed, 0);
 		roundAttr.put(Attribute.BombStack, 0);
 		roundAttr.put(Attribute.BombRange, 0);
 
 	}
 
 	/**
 	 * Get a list of all the players attribute
 	 * 
 	 * @return List of the players match attributes
 	 */
 	public List<Attribute> getAttributes() {
 		List<Attribute> attributeList = new ArrayList<Attribute>();
 		for (Attribute attribute : matchAttr.keySet()) {
 			attributeList.add(attribute);
 		}
 		return new ArrayList<Attribute>(attributeList);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		PlayerAttributes other = (PlayerAttributes) obj;
 		return this.matchAttr.equals(other.matchAttr)
 				&& this.roundAttr.equals(other.roundAttr);
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 
 		for (Attribute attribute : matchAttr.keySet()) {
 			sum += matchAttr.get(attribute) * 5;
 		}
 
 		for (Attribute attribute : roundAttr.keySet()) {
 			sum += roundAttr.get(attribute) * 7;
 		}
 		return sum;
 	}
 }
