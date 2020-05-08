 package gt.general.aura;
 
 import gt.general.Character;
 import gt.general.CharacterAttributes;
 import gt.general.Hero;
 /**
  * Effect for slowing down gnome bearer, and draining his stamina
  */
 public class GnomeCarrierEffect implements Effect {
	// lowers the speed by 1/16
	public final static double SLOWRATE = 0.0625;
 
 	@Override
 	public void performTick() {
 	}
 
 	@Override
 	public int remainingTicks() {
 		return 1; // never expire
 	}
 
 	@Override
 	public void takeEffect(final Character character) {
 		
 		if(character instanceof Hero) {
 			Hero hero = (Hero)character;
 
			hero.addToAttribute(CharacterAttributes.SPEED,
								hero.getBaseAttribute(CharacterAttributes.SPEED) * -SLOWRATE);

 			hero.scaleAttribute(CharacterAttributes.JUMPMULTIPLIER, 0.0);
 			hero.addToAttribute(CharacterAttributes.JUMPMULTIPLIER, -1);
 		}
 
 	}
 
 
 
 }
