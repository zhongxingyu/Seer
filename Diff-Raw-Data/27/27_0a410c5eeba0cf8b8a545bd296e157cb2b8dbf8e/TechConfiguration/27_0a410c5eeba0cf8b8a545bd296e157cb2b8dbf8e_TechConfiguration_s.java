 package linewars.gamestate.tech;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import linewars.display.IconConfiguration;
 import linewars.gamestate.Player;
 import linewars.gamestate.Race;
 
 import configuration.Configuration;
 import configuration.Property;
 import configuration.Usage;
 
 public class TechConfiguration extends Configuration implements Observer {
 		
 	/**
 	 * Regenerate this if and only if you change instance variables
 	 */
 	private static final long serialVersionUID = 1632034550432070187L;
 	
 	private static final String modificationKey = "modification";
 	private static final String costKey = "cost";
 	private static final String nameKey = "name";
 	private static final String tooltipKey = "tooltip";
 	private static final String iconsKey = "icons";
 	private static final String raceKey = "race";
 	
 	//The Modification to be made to the owner's race
 	private ModifierConfiguration modification;
 	//The cost to research this this Tech
 	private double cost;
 	
 	//The name of this Tech
 	private String name;
 	//The tooltip to be displayed when the user hovers the mouse over this Tech
 	private String tooltip;
 	
 	//The Configuration object that stores the icon configuration for this tech
 	private IconConfiguration icons;
 	
 	//The Configuration object that stores the Race that will be modified.
 	private Race race;
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(arg0 != this) return;
 		if(!(arg1 instanceof String)) return;
 		String propertyName = (String) arg1;
		Object value = this.getPropertyForName(propertyName);
 
 		if(propertyName == modificationKey){
 			modification = (ModifierConfiguration) value;
 		}else if(propertyName == costKey){
 			cost = (Double) value;
 		}else if(propertyName == nameKey){
 			name = (String) value;
 		}else if(propertyName == tooltipKey){
 			tooltip = (String) value;
 		}else if(propertyName == iconsKey){
 			icons = (IconConfiguration) value;
 		}else if(propertyName == raceKey){
 			race = (Race) value;
 		}
 	}
 	
 	public void research(Player owner){
 		Race toModify = owner.getRace();
 		//TODO assert toModify.equals(race) ???
 		modification.applyTo(new Property(Usage.CONFIGURATION, toModify));
 	}
 	
 	public ModifierConfiguration getModification() {
 		return modification;
 	}
 	
 	public void setModification(ModifierConfiguration modification) {
 		this.setPropertyForName(modificationKey, new Property(Usage.CONFIGURATION, modification));
 	}
 	
 	public double getCost() {
 		return cost;
 	}
 	
 	public void setCost(double cost) {
 		this.setPropertyForName(costKey, new Property(Usage.NUMERIC_FLOATING_POINT, cost));
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.setPropertyForName(nameKey, new Property(Usage.STRING, name));
 	}
 	
 	public String getTooltip() {
 		return tooltip;
 	}
 	
 	public void setTooltip(String tooltip) {
 		this.setPropertyForName(tooltipKey, new Property(Usage.STRING, tooltip));
 	}
 	
 	public IconConfiguration getIcons(){
 		return icons;
 	}
 	
 	public void setIcons(IconConfiguration icons){
 		this.setPropertyForName(iconsKey, new Property(Usage.CONFIGURATION, icons));
 	}
 	
 	public Race getRace(){
 		return race;
 	}
 	
 	public void setRace(Race race){
 		this.race = race;
 	}
 }
