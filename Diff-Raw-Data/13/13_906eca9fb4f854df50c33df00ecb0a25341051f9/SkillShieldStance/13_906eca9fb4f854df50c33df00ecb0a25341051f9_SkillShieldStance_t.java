 package Model.Skills.Warrior;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skills.Skill;
 import Model.StatusEffects.StatusEffectShieldstance;
 import Model.StatusEffects.StatusEffectUnstableMagic;
 
 public class SkillShieldStance extends Skill {
 	public SkillShieldStance(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
 
 				super("Shield stance", 11000, 0, 0.4, 3, 25, 0, 0, 0, 0,"Shieldstance \n" +
 
 						"Level 1: 15 damage\n" +
 						"Level 2: 25 damage\n" +
 						"Level 3: 35 damage\n" +
 						"Level 4: 45 damage");
 				
 				Image attackImage = null;
 				Image[] animation = new Image[4];
 				Image[] skillBar = new Image[3];
 				
 				super.setSelfAffectingStatusEffectShell(new StatusEffectShieldstance(this, 5));
 				
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
 }
