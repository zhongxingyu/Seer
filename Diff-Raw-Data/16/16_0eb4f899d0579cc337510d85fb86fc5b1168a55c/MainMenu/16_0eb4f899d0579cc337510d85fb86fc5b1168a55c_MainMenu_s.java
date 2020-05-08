 /**
  * 
  */
 package trivia;
 
 import java.awt.Graphics;
 
 /**
  * @author Cody Swendrowski
  *
  */
 public class MainMenu extends GameMode {
 	
 	
 	
 	public MainMenu(GameEngine eng){
 		super(eng);
 		buttons = new Button[1];
 		buttons[0] = new Button("Start Questioning", 10, 10, 100, 30);
 	}
 	
 	/* (non-Javadoc)
 	 * @see trivia.GameMode#run()
 	 */
 	@Override
 	public void run() {
 		if(buttons[0].isClicked()){
			engine.setMode(engine.MAIN_GAME);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see trivia.GameMode#paint(java.awt.Graphics)
 	 */
 	@Override
 	public void paint(Graphics g) {
 		buttons[0].draw(g);
 		// TODO Auto-generated method stub
 
 	}
 	
 	public String toString(){
 		return "Main Menu";
 	}
 	
 }
