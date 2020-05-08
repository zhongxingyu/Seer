 package concept.being.creature;
 
 import concept.being.Being;
 import concept.nutrient.Need;
 import concept.nutrient.Nutrient;
 import br.com.etyllica.util.SVGColor;
 
 public class BlueCreature extends Being {
 
 	public BlueCreature(int x, int y) {
 		super(x, y);
 		
 		this.interactionRadius = 20;
 
 		this.colorFill = SVGColor.DARK_BLUE;
 		
 		this.addNeed(new Need(Nutrient.WATER, 500));
		
		this.path = "purplebug.png";
 	}
 
 	
 	
 }
