 package View;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.state.*;
 import org.newdawn.slick.util.ResourceLoader;
 
 import Model.MainHub;
 
 
 
 
 
 public class Menu extends BasicGameState implements ActionListener{
 
 	private String mouse = "No input yet";
 
 	/** The wav sound effect */
 	private Audio wavEffect;
 	
 	private boolean startMusic = true;
 	
 	Image bg;
 
 	Image backgroundImage;
 	Image startGameButton;
 	Image exitButton;
 	Image titleText;
 	Image warriorImage;
 	
 	public Menu (int state){
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		
 		backgroundImage = new Image("res/miscImages/bg-gates.png");
 		startGameButton = new Image("res/buttons/startgame.png");
 		exitButton = new Image("res/buttons/exit.png");
 		titleText = new Image("res/miscImages/menuText.png");
 		
 		try {
 			wavEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("res/sounds/bg-music.wav"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException{
 		gc.setFullscreen(false);
 		g.setColor(Color.black);
 		g.drawImage(backgroundImage, 0, 0);
 
 		/*if(startMusic){
 			wavEffect.playAsSoundEffect(1.0f, 1.0f, true);
 			startMusic = false;
 		}*/
 		
 		g.drawImage(titleText, gc.getWidth()/2 - titleText.getWidth()/2, 150);
 		g.drawImage(startGameButton, gc.getWidth()/2 - startGameButton.getWidth()/2, 325);
 		g.drawImage(exitButton, gc.getWidth()/2 - exitButton.getWidth()/2, 425);
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		Input input = gc.getInput();  
 		// Escape key quits the game
         if(input.isKeyDown(Input.KEY_ESCAPE)) gc.exit();
         
 		
 		if((580<xPos && xPos<700) && (325<yPos && yPos<370)){
 			startGameButton = new Image("res/buttons/startgame_p.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				sbg.enterState(3);
 			}
 		} else if((580<xPos && xPos<700) && (425<yPos && yPos<470)){
 			exitButton = new Image("res/buttons/exit_p.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
				gc.exit();
 			}
 		}else{
 			startGameButton = new Image("res/buttons/startgame.png");
 			exitButton = new Image("res/buttons/exit.png");
 		}
 	}
 	
 	public int getID(){
 		return 0;
 	}
 	
 	public int getWidth(Image image){
 		return image.getWidth();
 	}
 	
 	public int getHeight(Image image){
 		return image.getHeight();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 	}
 }
