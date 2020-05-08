 package View;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 
 import Control.GlobalClassSelector;
 import Model.Player;
 import Model.PlayerModel;
 import Model.Classes.*;
 import Model.Obstacles.Obstacle;
 import Model.Obstacles.ObstaclePillar;
 
 public class ClassSelectionView extends BasicGameState implements ActionListener{
 
 	private String mouse = "No input yet";
 	
 	Player player = null;
 //	private String classType = null;
 
 	Image backgroundImage;
 	Image selectButton;
 	Image classImage;
 	
 	String classDescription = "";
 	String title = "";
 
 	Obstacle[] obstacles = new Obstacle[100];
 	
 	
 	public ClassSelectionView (int state){
 
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 
 		backgroundImage = new Image("res/miscImages/sakura001.png");
 		selectButton = new Image("res/buttons/playButtons.png");
 		classImage = new Image("res/classImages/classes.png");
 		title = "Choose your class!";
 		
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		g.setColor(Color.black);
 		g.drawImage(backgroundImage, 0, 0);
 		
 		g.drawString(mouse, 500, 20);
 		
 		g.drawImage(selectButton, 500, 550);
 		g.drawImage(classImage, 336, 100);
 		
 		g.drawString(classDescription, 336, 450);
 		g.drawString(title, 550, 75);
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		/*Random obsGenerator = new Random();
 		for(int i=0; i<obsGenerator.nextInt(50); i++){
 			obstacles[i] = new ObstaclePillar(obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1);
 		}*/
 		Input input = gc.getInput();
 	//	Control = new PlayerController("Player", obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1, obstacles, "Warrior");
 		
 		if((500<xPos && xPos<750) && (550<yPos && yPos<604)){
 			selectButton = new Image("res/buttons/playButton_hover.png");
 			if(player != null && input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				selectButton = new Image("res/buttons/playButton_pressed.png");
 				
 //				System.out.println(GlobalClassSelector.getController().getPlayers().size());
 				
 				GlobalClassSelector.getController().resetPlayers();
 				
 //				System.out.println(GlobalClassSelector.getController().getPlayers().size());
 				GlobalClassSelector.getController().addPlayer(player, 0);
 				
 //				System.out.println(GlobalClassSelector.getController().getPlayers().size());
 				
 				//Addition of AI player
				GlobalClassSelector.getController().addPlayer(new ClassWarrior("Enemy", 600, 600, 1), 1);
 //				System.out.println(GlobalClassSelector.getController().getPlayers().size());
 				
 				sbg.enterState(1);
 			}
 		}else{
 			selectButton = new Image("res/buttons/playButtons.png");
 		}
 		if((340<xPos && xPos<517) && (106<yPos && yPos<425)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				classImage = new Image("res/classImages/warriorSelected.png");
 				classDescription = "The heart of a warrior is filled with courage and strength. \n" +
 						"Your skills with weapons in close combat makes you a powerful \n" +
 						"force on the battlefield and a durable opponent for all who dares \n" +
 						"cross your way.";
				player = new ClassWarrior("Player", 120, 100, 0);
 		//		Control = new PlayerController("WarriorMan", obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1, obstacles, "Warrior");
 			}
 		}
 		if((518<xPos && xPos<719) && (106<yPos && yPos<425)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				classImage = new Image("res/classImages/hunterSelected.png");
 				classDescription = "Hunters are stealthy, wealthy and wise to the ways of \n" +
 						"their opponents. Able to take down tyrants without blinking an eye \n" +
 						"or breaking a bowstring, you'll range far and wide with this class.";
 		//		classType = "Hunter";
				player = new ClassHunter("Player", 120, 100, 0);
 		//		Control = new PlayerController("HunterMan", obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1, obstacles, "Hunter");
 			}
 		}
 		if((720<xPos && xPos<938) && (106<yPos && yPos<425)){
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				classImage = new Image("res/classImages/mageSelected.png");
 				classDescription = "Mages are the quintessential magic user. They attack from \n" +
 						"a distance, able to cause great harm or restrict a targets actions using \n" +
 						"their supreme knowledge of the elements.";
 		//		classType = "Wizard";
				player = new ClassWizard("Player", 120, 100, 0);
 		//		Control = new PlayerController("WizardMan", obsGenerator.nextInt(1280), obsGenerator.nextInt(719) + 1, obstacles, "Wizard");
 			}
 		}
 	}
 	
 	public int getID(){
 		return 3;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 	}
 }
