 package Model.Skills.Wizard;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import Model.Skill;
 
 public class SkillFirestorm extends Skill{
 
 	public SkillFirestorm(){
 		//String name, int cd, int range, double speed, int aoe, int cost, int damage, StatusEffect SE
		super("Firestorm", 9000, 350, 100, 8, 5, 15,"Firestorm:\nA dangerous explosion that \ndeals damage to an area for a \nlimited time." +
 				"\nLevel 1: 15 damage\n" +
 				"Level 2: 25 damage\n" +
 				"Level 3: 35 damage\n" +
 				"Level 4: 45 damage");
 		
 		Image attackImage = null;
 		Image[] animation = new Image[27];
 		Image[] skillBar = new Image[3];
 		
 		
 		try {
 			attackImage = new Image("res/animations/firestorm/firestorm1.png");
 			
 			animation[0] = new Image("res/animations/firestorm/firestorm1.png");
 			animation[1] = new Image("res/animations/firestorm/firestorm2.png");
 			animation[2] = new Image("res/animations/firestorm/firestorm3.png");
 			animation[3] = new Image("res/animations/firestorm/firestorm4.png");
 			animation[4] = new Image("res/animations/firestorm/firestorm5.png");
 			animation[5] = new Image("res/animations/firestorm/firestorm6.png");
 			animation[6] = new Image("res/animations/firestorm/firestorm7.png");
 			animation[7] = new Image("res/animations/firestorm/firestorm8.png");
 			animation[8] = new Image("res/animations/firestorm/firestorm9.png");
 			animation[9] = new Image("res/animations/firestorm/firestorm10.png");
 			animation[10] = new Image("res/animations/firestorm/firestorm11.png");
 			animation[11] = new Image("res/animations/firestorm/firestorm12.png");
 			animation[12] = new Image("res/animations/firestorm/firestorm13.png");
 			animation[13] = new Image("res/animations/firestorm/firestorm14.png");
 			animation[14] = new Image("res/animations/firestorm/firestorm15.png");
 			animation[15] = new Image("res/animations/firestorm/firestorm16.png");
 			animation[16] = new Image("res/animations/firestorm/firestorm17.png");
 			animation[17] = new Image("res/animations/firestorm/firestorm18.png");
 			animation[18] = new Image("res/animations/firestorm/firestorm19.png");
 			animation[19] = new Image("res/animations/firestorm/firestorm20.png");
 			animation[20] = new Image("res/animations/firestorm/firestorm21.png");
 			animation[21] = new Image("res/animations/firestorm/firestorm22.png");
 			animation[22] = new Image("res/animations/firestorm/firestorm23.png");
 			animation[23] = new Image("res/animations/firestorm/firestorm24.png");
 			animation[24] = new Image("res/animations/firestorm/firestorm25.png");
 			animation[25] = new Image("res/animations/firestorm/firestorm26.png");
 			animation[26] = new Image("res/animations/firestorm/firestorm27.png");
 			
 			
 			skillBar[0] = new Image("res/skillIcons/firestorm.png");
 			skillBar[1] = new Image("res/skillIcons/firestorm_active.png");
 			skillBar[2] = new Image("res/skillIcons/firestorm_disabled.png");
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.setImage(attackImage);
 		super.setEndState(animation, 1800, 30);
 		super.setSkillBarImages(skillBar);
 	}
 
 	private int lvl2 = 7;
 	private int lvl3 = 9;
 	private int lvl4 = 11;
 	
 	@Override
 	public void upgradeSkill() {
 		if(super.getCurrentLvl() < 4){
 			super.incCurrentLvl();
 			
 			switch(super.getCurrentLvl()){
 			case 2:
 				super.setDamage(lvl2);
 				break;
 			case 3:
 				super.setDamage(lvl3);
 				break;
 			case 4:
 				super.setDamage(lvl4);
 				break;
 			}
 		}
 	}
 }
