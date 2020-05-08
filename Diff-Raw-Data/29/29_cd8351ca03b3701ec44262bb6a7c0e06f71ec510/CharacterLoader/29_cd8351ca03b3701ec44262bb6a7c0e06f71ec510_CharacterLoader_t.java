 package vooga.fighter.model.loaders;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import vooga.fighter.model.ModelConstants;
 import vooga.fighter.model.objects.AttackObject;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.utils.State;
 
 /**
  * Loads data associated with a character object to be passed to CharacterObject.
  * 
  * @author David Le, alanni
  *
  */
 public class CharacterLoader extends ObjectLoader {
 		
 	private CharacterObject myChar;
 
 	/**
 	 * Constructs the character loader with the id to be loaded and the character which the
 	 * loader will modify.
 	 * @param charId
 	 * @param character
 	 */
 	public CharacterLoader (String charName, CharacterObject character) {
 		super("CharacterPath");
 		myChar = character;
 		load(charName);
 	}
 
 	/**
 	 * Loads the character associated with the id
 	 * @param charId is the id of the character to be loaded
 	 */
 	protected void load(String charName) {
 		Document doc = getDocument();
 		NodeList charNodes = doc.getElementsByTagName(getResourceBundle().getString("Character"));
 
 		for (int i = 0; i < charNodes.getLength(); i++) {
 			Node node = charNodes.item(i);
 			String name = getAttributeValue(node, getResourceBundle().getString("CharacterName"));
 			if (charName.equals(name)) {
 				int maxHealth = Integer.parseInt(getAttributeValue(node, getResourceBundle().getString("MaxHealth")));
 				int movespeed= Integer.parseInt(getAttributeValue(node, getResourceBundle().getString("Movespeed")));
 				int mass= Integer.parseInt(getAttributeValue(node, getResourceBundle().getString("Mass")));
 				myChar.addProperty(getResourceBundle().getString("Movespeed"), movespeed);
 				myChar.addProperty(getResourceBundle().getString("Mass"), mass);
 				myChar.setHealth(maxHealth);
 				NodeList stateNodes = ((Element) node).getElementsByTagName(getResourceBundle().getString("State"));
 				addStates(stateNodes, myChar);
 				NodeList attackNodes = ((Element) node).getElementsByTagName(getResourceBundle().getString("Attack"));
 				addAttacks(attackNodes);
 			}
 		}
 	}
 	
 	/**
 	 * Loads and adds attacks to the list of attacks that a character can perform
 	 * @param attackNodes
 	 */
 	private void addAttacks(NodeList attackNodes) {
 		for (int i = 0; i < attackNodes.getLength(); i++) {
 			Element attack = (Element) attackNodes.item(i);
 			String attackName = getAttributeValue(attackNodes.item(i), getResourceBundle().getString("AttackName"));
 			int attackDmg = Integer.parseInt(getAttributeValue(attackNodes.item(i), getResourceBundle().getString("Damage")));
 			int attackDuration = Integer.parseInt(getAttributeValue(attackNodes.item(i), getResourceBundle().getString("Duration")));
			int attackSpeed = Integer.parseInt(getAttributeValue(attackNodes.item(i), getResourceBundle().getString("Attackspeed")));
 			NodeList frameNodes = attack.getElementsByTagName(getResourceBundle().getString("Frame"));
 			AttackObject newAttack = new AttackObject();
 			State newState = new State(myChar, frameNodes.getLength());
 			newAttack.addProperty(ModelConstants.ATTACK_PROPERTY_SPEED, attackSpeed);
 			newAttack.addProperty(ModelConstants.ATTACK_PROPERTY_POWER, attackDmg);
 			newAttack.addProperty(ModelConstants.ATTACK_PROPERTY_DURATION, attackDuration);
 			getImageAndHitboxProperties(frameNodes, newState);
 			newAttack.addState(attackName, newState);
 			newAttack.setCurrentState(attackName);
 			myChar.addAttack(attackName, newAttack);
 		}
 	}
 }
