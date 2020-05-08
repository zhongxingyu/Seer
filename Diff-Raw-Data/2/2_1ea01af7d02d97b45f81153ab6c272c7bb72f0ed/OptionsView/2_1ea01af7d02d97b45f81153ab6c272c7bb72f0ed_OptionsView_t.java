 package View;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import Model.MainHub;
 
 
 public class OptionsView extends BasicGameState {
 	
 	
 	Image background;
 	Image readyButton;
 	
 	Image easyButton;
 	Image normalButton;
 	Image hardButton;
 	
 	Image Map1;
 	Image Map2;
 	Image Map3;
 
 	Image MapView;
 	
 	public OptionsView (int state){
 			
 	}
 	
 	public int getID(){
 		return 5;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException {
 		
 		background = new Image("res/miscImages/OptionsBackground.png");
 		
 				
 		
 		easyButton = new Image("res/buttons/Easy_1.png");
 		normalButton = new Image("res/buttons/Normal_3.png");
 		hardButton = new Image("res/buttons/Hard_1.png");
 		
 		readyButton = new Image ("res/buttons/Ready.png");
 		
 		MainHub.getController().setDifficultySelected(2);
 		MainHub.getController().setMapIndex(0);
 		
 		Map1= new Image ("res/Maps/Map_HazardCross2.png");
 		Map2= new Image ("res/Maps/Map_SlaughterField2_unavailable.png");
 		Map3= new Image ("res/Maps/Map_Volcano2_unavailable.png");
 	}
 
 	public void enter(GameContainer container, StateBasedGame game){
 	     
 
 	   }
 	@Override
 	//586x67
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)throws SlickException {
 		g.drawImage(background, 0, 0);
 		
 		g.drawImage(readyButton, 1120, 670);		
 		g.drawImage(easyButton, 75,615);		
 		g.drawImage(normalButton,275,615);		
 		g.drawImage(hardButton,475,615);
 		
 		g.drawImage(Map1,57,92);
 		g.drawImage(Map2,57,161);
 		g.drawImage(Map3,57,230);
 		
 		g.drawImage(MapView,703,66);
 		
 		
 
 		
 	
 	
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)throws SlickException {
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		Input input = gc.getInput();
 		
 		//Readybutton Check
 		if((1120<xPos && xPos<1240) && (670<yPos && yPos<715)){
 			readyButton = new Image("res/buttons/ReadyOver.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				readyButton = new Image("res/buttons/Ready.png");
 				sbg.enterState(4);
 			}		
 		}
 		//Easybutton Check 159x38
 		if((75<xPos && xPos<234) && (615<yPos && yPos<653)&& MainHub.getController().getDiffcultySelected()!= 1){
 			easyButton = new Image("res/buttons/Easy_2.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				easyButton = new Image("res/buttons/Easy_3.png");
 				MainHub.getController().setDifficultySelected(1);
 			}
 		}else if (MainHub.getController().getDiffcultySelected()!= 1){
 			easyButton = new Image("res/buttons/Easy_1.png");
 		}
 		//Normalbutton Check
 		if((275<xPos && xPos<434) && (615<yPos && yPos<653)&& MainHub.getController().getDiffcultySelected() != 2){
 			normalButton = new Image("res/buttons/Normal_2.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				normalButton = new Image("res/buttons/Normal_3.png");
 				MainHub.getController().setDifficultySelected(2);
 			}	
 		}else if (MainHub.getController().getDiffcultySelected() != 2){
 			normalButton = new Image("res/buttons/Normal_1.png");
 		}
 		//Hardbutton Check
 		if((475<xPos && xPos<634) && (615<yPos && yPos<653)&& MainHub.getController().getDiffcultySelected() != 3){
 			hardButton = new Image("res/buttons/Hard_2.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				hardButton = new Image("res/buttons/Hard_3.png");
 				MainHub.getController().setDifficultySelected(3);
 			}
 		}else if (MainHub.getController().getDiffcultySelected() != 3){
 			hardButton = new Image("res/buttons/Hard_1.png");
 		}	
 		
 		//586x67
 		//Map checks
 		if((57<xPos && xPos<643) && (92<yPos && yPos<159)&& input.isMousePressed(0)){			
 			Map1 = new Image("res/Maps/Map_HazardCross2.png");
 			Map2 = new Image("res/Maps/Map_SlaughterField2_unavailable.png");
 			Map3 = new Image("res/Maps/Map_Volcano2_unavailable.png");
 			MainHub.getController().setMapIndex(0);	
 		}	
 		if((57<xPos && xPos<643) && (161<yPos && yPos<228) && input.isMousePressed(0)){
 			Map1 = new Image("res/Maps/Map_HazardCross.png");
 			Map2 = new Image("res/Maps/Map_SlaughterField2_unavailable.png");
 			Map3 = new Image("res/Maps/Map_Volcano2_unavailable.png");
 			MainHub.getController().setMapIndex(1);
 		}
 		if((57<xPos && xPos<643) && (230<yPos && yPos<297)&& input.isMousePressed(0)){
 			Map1 = new Image("res/Maps/Map_HazardCross.png");
 			Map2 = new Image("res/Maps/Map_SlaughterField2_unavailable.png");
 			Map3 = new Image("res/Maps/Map_Volcano2_unavailable.png");
 			MainHub.getController().setMapIndex(2);
 		}	
 		if(MainHub.getController().getMapSelected().getName()== "Hazard Cross"){
 			MapView = new Image ("res/Maps/MapHazardCross.png");
 		}else if(MainHub.getController().getMapSelected().getName()== "Slaughter Field"){
 	//		MapView = new Image ("res/Maps/MapSlaughterField.png");
 			MapView = new Image ("res/Maps/MapUnavailable.png");
 		}else if(MainHub.getController().getMapSelected().getName()== "Volcano"){
 			MapView = new Image ("res/Maps/MapUnavailable.png");
 		}
 	}
 }
