 package Model.Skills.Hunter;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.Skill;
 import Model.StatusEffects.StatusEffectMovement;
 import Model.StatusEffects.StatusEffectImmobilize;
 
 public class SkillCripplingTrap extends Skill {
 	public SkillCripplingTrap(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
		super("Crippling trap", 1000, 0, 0.4, 3, 25, 150, 300, 300, 300,"Crippling trap: \nA trap which slows the enemy.\n" +
 				"Level 1: 150 damage\n" +
 				"Level 2: 300 damage\n" +
 				"Level 3: 300 damage\n" +
 				"Level 4: 300 damage");
 		
 		Image attackImage = null;
 		Image[] animation = new Image[1];
 		Image[] skillBar = new Image[3];
 		
 		super.setOffensiveStatusEffectShell(new StatusEffectMovement(this, -0.3, 1),true);
 		
 		try {
 			attackImage = new Image("res/animations/arrow.png");
 			
 			animation[0] = new Image("res/animations/trap/slowingtrap.png");
 			
 			skillBar[0] = new Image("res/skillIcons/cripplingshot.png");
 			skillBar[1] = new Image("res/skillIcons/cripplingshot_active.png");
 			skillBar[2] = new Image("res/skillIcons/cripplingshot_disabled.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		super.setImage(attackImage);
 		super.setEndState(animation, 20000, 1010);
 		super.setSkillBarImages(skillBar);
 	}
 }
