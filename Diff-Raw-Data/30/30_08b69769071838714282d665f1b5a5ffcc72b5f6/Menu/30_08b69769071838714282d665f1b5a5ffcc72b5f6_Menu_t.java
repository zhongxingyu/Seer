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
 	
 	public Menu (int state){
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		
 		backgroundImage = new Image("res/miscImages/bg.png");
		startGameButton = new Image("res/buttons/startgame.png");
 		exitButton = new Image("res/buttons/exitButton.png");
 		titleText = new Image("res/miscImages/title.png");
 		
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
 		g.drawString(mouse, 500, 20);
 		
 		g.drawImage(titleText, 380, 100);
		g.drawImage(startGameButton, 500, 325);
		g.drawImage(exitButton, 500, 425);
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		
 		int xPos = Mouse.getX();
 		int yPos = 720 - Mouse.getY();
 		
 		mouse = "Mouse position: (" + xPos + "," + yPos + ")";
 		
 		Input input = gc.getInput();  
 		// Escape key quits the game
         if(input.isKeyDown(Input.KEY_ESCAPE)) gc.exit();
         
 		
		if((500<xPos && xPos<750) && (325<yPos && yPos<379)){
			startGameButton = new Image("res/buttons/startgame_pressed.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				sbg.enterState(3);
 			}
		} else if((500<xPos && xPos<750) && (425<yPos && yPos<479)){
 			exitButton = new Image("res/buttons/exitButton_pressed.png");
 			if(input.isMousePressed(0)){ // 0 = leftclick, 1 = rightclick
 				System.exit(0);
 			}
 		}else{
			startGameButton = new Image("res/buttons/startgame.png");
 			exitButton = new Image("res/buttons/exitButton.png");
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
