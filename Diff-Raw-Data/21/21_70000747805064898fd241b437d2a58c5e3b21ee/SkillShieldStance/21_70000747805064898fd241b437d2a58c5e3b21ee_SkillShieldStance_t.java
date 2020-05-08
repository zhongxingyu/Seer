 package Model.Skills.Warrior;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skill;
 import Model.StatusEffects.StatusEffectShieldstance;
 
 public class SkillShieldStance extends Skill {
 	public SkillShieldStance(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
 
 				super("Shield stance", 11000, 0, 0.4, 3, 60, 0,"Shieldstance \nIncreases armor but decreases \nmovementspeed (20%armor)\n" +
 
 						"Level 1: 2 seconds \n" +
 						"Level 2: 4 seconds\n" +
 						"Level 3: 6 seconds\n" +
 						"Level 4: 8 seconds");
 				
 				Image attackImage = null;
 				Image[] animation = new Image[4];
 				Image[] skillBar = new Image[3];
 				
 				super.setToggled();
 
				super.setSelfAffectingStatusEffectShell(new StatusEffectShieldstance(this, -0.75, 0.2));
 				
 				try {
 					attackImage = new Image("res/animations/Shieldstance/Shieldstance4.png");
 					
 					animation[0] = new Image("res/animations/Shieldstance/Shieldstance4.png");
 					animation[1] = new Image("res/animations/Shieldstance/Shieldstance3.png");
 					animation[2] = new Image("res/animations/Shieldstance/Shieldstance2.png");
 					animation[3] = new Image("res/animations/Shieldstance/Shieldstance1.png");
 				
 					
 					skillBar[0] = new Image("res/skillIcons/shieldstance.png");
 					skillBar[1] = new Image("res/skillIcons/shieldstance_active.png");
 					skillBar[2] = new Image("res/skillIcons/shieldstance_disabled.png");
 				} catch (SlickException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				super.setImage(attackImage);
 				super.setEndState(animation, 200, 50);
 				super.setSkillBarImages(skillBar);
 			}
 
 	private int lvl2 = 0;
 	private int lvl3 = 0;
 	private int lvl4 = 0;
 	
 	@Override
 	public void upgradeSkill() {
 		if(super.getCurrentLvl() < 4){
 			super.incCurrentLvl();
 			
 			switch(super.getCurrentLvl()){
 			case 2:
				super.setSelfAffectingStatusEffectShell(new StatusEffectShieldstance(this, -0.75, 0.23));
 				super.setDamage(lvl2);
 				break;
 			case 3:
				super.setSelfAffectingStatusEffectShell(new StatusEffectShieldstance(this, -0.75, 0.26));
 				super.setDamage(lvl3);
 				break;
 			case 4:
				super.setSelfAffectingStatusEffectShell(new StatusEffectShieldstance(this, -0.75, 0.3));
 				super.setDamage(lvl4);
 				break;
 			}
 		}
 	}
 }
