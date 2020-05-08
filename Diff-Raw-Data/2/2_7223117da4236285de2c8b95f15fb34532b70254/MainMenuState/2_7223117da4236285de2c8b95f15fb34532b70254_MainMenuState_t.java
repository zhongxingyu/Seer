 package game.states;
 
 import initial3d.engine.xhaust.MouseArea;
 import initial3d.engine.xhaust.Pane;
 import initial3d.engine.xhaust.Picture;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
import soundengine.SimpleAudioPlayer;

 import game.Game;
 import game.GameState;
 
 /***
  * This is the main menu state, on steriods. It will have a GUI
  * 
  * From this state, the user chooses to create a game, or join an existing game. It should switch to the corrosponding state.
  * @author Ben and Patrick
  *
  */
 public class MainMenuState extends GameState {
 
 	static MouseArea joinArea = new MouseArea(267, 345, 260, 35);
 	static MouseArea startArea = new MouseArea(235, 407, 571-235, 35);
 	Picture pic;
 	Picture pleftArrow;
 	Picture prightArrow;
 	BufferedImage mainmenu = null;
 	//BufferedImage joinmenu = null;
 	//BufferedImage startmenu = null;
 	Picture leftTop, leftBottom, rightTop, rightBottom;
 
 	
 
 	private final ActionListener l = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(e.getActionCommand().equals("released")){
 				if(e.getSource().equals(joinArea)){
 					SimpleAudioPlayer.stopMusic();
 				SimpleAudioPlayer.play("resources/music/menuSelect.wav", false);
 					Game.getInstance().changeState(new FindGameState());
 				}
 				else if(e.getSource().equals(startArea)){
 					SimpleAudioPlayer.stopMusic();
 
 					SimpleAudioPlayer.play("resources/music/menuSelect.wav", false);
 					Game.getInstance().changeState(new CreateGameState());
 				}
 
 			}
 			else if(e.getActionCommand().equals("mouseover")){
 				if(e.getSource().equals(joinArea)){
 				//	pic.setPicture(joinmenu);
 					leftTop.setVisible();
 					rightTop.setVisible();
 					leftBottom.setHidden();
 					rightBottom.setHidden();
 					
 					repaint();
 					
 				}
 					
 
 				else if(e.getSource().equals(startArea)){
 					//pic.setPicture(startmenu);
 					leftTop.setHidden();
 					rightTop.setHidden();
 					leftBottom.setVisible();
 					rightBottom.setVisible();
 					
 					repaint();
 				}
 					
 
 			}
 		}
 	};
 
 	public MainMenuState() {
 	}
 
 	@Override
 	public void initalise() {
 		SimpleAudioPlayer.play("resources/menumusic.wav", true);
 		Pane p = new Pane(800, 600);
 
 		//stuff
 		
 
 
 		try {
 			mainmenu = ImageIO.read(new File("resources/mainmenu.png"));
 			//joinmenu = ImageIO.read(new File("resources/mainmenujoin.png"));
 			//startmenu = ImageIO.read(new File("resources/mainmenustart.png"));
 			BufferedImage rightArrow = ImageIO.read(new File("resources/rightarrow.png"));
 			BufferedImage leftArrow = ImageIO.read(new File("resources/leftarrow.png"));
 			leftTop = new Picture(leftArrow, 240, 345,35,35);
 			leftBottom = new Picture(leftArrow, 225, 407,35,35);
 			rightTop = new Picture(rightArrow, 525, 345,35,35);
 			rightBottom = new Picture(rightArrow, 540, 407,35,35);
 
 			
 
 		} catch (IOException e) {
 			throw new AssertionError(e);
 		}
 		
 		leftTop.setHidden();
 		rightTop.setHidden();
 		leftBottom.setHidden();
 		rightBottom.setHidden();
 		pic = new Picture(mainmenu,0,0, 800,600);
 		p.getRoot().add(pic);
 		//input from user
 
 		joinArea.addActionListener(l);
 		p.getRoot().add(joinArea);
 		startArea.addActionListener(l);
 		p.getRoot().add(startArea);
 		p.getRoot().add(leftTop);
 		p.getRoot().add(rightTop);
 		p.getRoot().add(leftBottom);
 		p.getRoot().add(rightBottom);
 	
 		scene.addDrawable(p);
 
 		p.requestVisible(true);
 
 
 	}
 
 	public void repaint(){
 		pic.repaint();
 		leftTop.repaint();
 		rightTop.repaint();
 		leftBottom.repaint();
 		rightBottom.repaint();
 	}
 
 	@Override
 	public void update(double delta) {
 	}
 
 	@Override
 	public void destroy() {
 	}	
 }
