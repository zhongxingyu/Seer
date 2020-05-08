 package Model.Skills.Wizard;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.Skill;
 import Model.StatusEffects.*;
 
 public class SkillTeleport extends Skill {
 
 	public SkillTeleport() {
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
 		super("Teleport", /*20000*/1000, 500, 100, 8, 0, 0, 0, 0, 0, "TAFLEPoRT!", true);
 		
 		Image attackImage = null;
 		Image[] animation = new Image[7];
 		Image[] skillBar = new Image[3];
 		
 		super.setStatusEffect(new StatusEffectTeleport(null, this, 0, 0));
 		
 		try {
 			attackImage = new Image("res/miscImages/awesomeMiniRedSquare.png");
 			
			animation[0] = new Image("res/animations/mage_stand.png");
			animation[1] = new Image("res/animations/mage_walk1.png");
			animation[2] = new Image("res/animations/mage_walk2.png");
			animation[3] = new Image("res/animations/mage_stand.png");
			animation[4] = new Image("res/animations/mage_walk1.png");
			animation[5] = new Image("res/animations/mage_walk2.png");
			animation[6] = new Image("res/animations/mage_walk2.png");
 			
 			
 			skillBar[0] = new Image("res/skillIcons/teleport.png");
 			skillBar[1] = new Image("res/skillIcons/teleport_active.png");
 			skillBar[2] = new Image("res/skillIcons/teleport_disabled.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		super.setImage(attackImage, attackImage.getHeight(), attackImage.getWidth());
 		super.setEndState(animation, 200, 200);
 		super.setSkillBarImages(skillBar);
 	}
 
 }
