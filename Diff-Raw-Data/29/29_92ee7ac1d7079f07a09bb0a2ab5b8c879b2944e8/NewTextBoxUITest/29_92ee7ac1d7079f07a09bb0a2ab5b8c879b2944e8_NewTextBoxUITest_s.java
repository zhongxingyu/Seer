 package game.TEST;
 
 import game.util.IO.InputState;
 import game.util.UI.NewTextBox;
 
 import java.awt.Dimension;
 import java.awt.Point;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Game;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 public class NewTextBoxUITest{
 	
 	public static void main(String[] arg) {try {
 		AppGameContainer agc = new AppGameContainer(new Game() {
 			
 			private NewTextBox tb; 
 		
 			@Override
 			public boolean closeRequested() {
 				return true;
 			}
 
 			@Override
 			public String getTitle() {
 				return "Test";
 			}
 
 			@Override
 			public void init(final GameContainer container) throws SlickException {
 				tb = new NewTextBox(new SpriteSheet("data/LoginScreen/Username.png", 300, 30));
 				tb.setLocation(new Point(10, 10));
 				tb.setDimension(new Dimension(300, 30));
 			}
 
 			@Override
 			public void render(GameContainer container, Graphics g) throws SlickException {
 				tb.render(container, g);
 				
 			}
 
 			@Override
 			public void update(GameContainer container, int delta) throws SlickException {
 				InputState.Update(container);
 				tb.update(container);
 						
 			}
 		});
 		agc.setDisplayMode(320, 50, false);
 		agc.start();
 	} catch (SlickException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	}
 
 }
 		
 	
 
 	
 	
 	
 
 	
 	
 		
 
 
 	
