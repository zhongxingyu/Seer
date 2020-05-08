 package Model.Skills;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class SkillSuperSlowTestSkill extends Skill{
 	
 	public SkillSuperSlowTestSkill(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
		super("SuperSlowTestSkill", 1000, 800, 0.1, 3, 0, 1, null);
 		
 		Image attackImage = null;
 		try {
 			attackImage = new Image("res/awesomeGreenSquare.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		super.setImage(attackImage, attackImage.getHeight(), attackImage.getWidth());
 	}
 
 }
