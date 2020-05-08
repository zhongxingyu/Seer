 /**
  * 
  */
 package net.mindsoup.pathfindercharactersheet.adapters;
 
 import net.mindsoup.pathfindercharactersheet.pf.PfAttributes;
 import net.mindsoup.pathfindercharactersheet.pf.PfCharacter;
 import net.mindsoup.pathfindercharactersheet.pf.util.Calculation;
 
 /**
  * @author Valentijn
  *
  */
 public class CharacterAttributeAdapter {
 	
 	private final PfAttributes attribute;
 	private final PfCharacter character;
 	
 	public CharacterAttributeAdapter(PfAttributes attribute, PfCharacter character) {
 		this.attribute = attribute;
 		this.character = character;
 	}
 	
 	public PfAttributes getAttribute() {
 		return this.attribute;
 	}
 	
 	public void setAttribute(int value) {
 		switch(this.attribute) {
 		case CHA: 
 			character.setBaseCharisma(value - character.getRace().getChaModifier());
 			break;
 		case CON: 
 			character.setBaseConstitution(value - character.getRace().getConModifier());
 			break;
 		case DEX:
 			character.setBaseDexterity(value - character.getRace().getDexModifier());
 			break;
 		case INT:
 			character.setBaseIntelligence(value - character.getRace().getIntModifier());
 			break;
 		case STR:
 			character.setBaseStrength(value - character.getRace().getStrModifier());
 			break;
 		case WIS:
 			character.setBaseWisdom(value - character.getRace().getWisModifier());
 			break;		
 		}
 	}
 	
 	public int getValue() {
 		return character.getAttributeValue(this.attribute).sum() - character.getTempBoostFor(attribute).sum() - character.getItemBoostFor(attribute).sum();
 	}
 	
 	public int getBonus() {
 		return character.getAttributeBonus(this.getValue());
 	}
 	
 	public void setTempValue(int value) {
 		switch(this.attribute) {
 		case CHA:
 			character.setTempChaBoost(value); 
 			break;
 		case CON:
 			character.setTempConBoost(value); 
 			break;
 		case DEX:
 			character.setTempDexBoost(value); 
 			break;
 		case INT:
 			character.setTempIntBoost(value); 
 			break;
 		case STR:
 			character.setTempStrBoost(value); 
 			break;
 		case WIS:
 			character.setTempWisBoost(value); 
 			break;		
 		}
 	}
 	
 	public int getTempValue() {
		return character.getAttributeValue(attribute).sum();
 	}
 	
 	public int getTempBonus() {
		return character.getAttributeBonus(this.getTempValue());
 	}
 
 }
