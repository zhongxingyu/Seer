 package me.bevilacqua.ld48;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class EndGame extends BasicGameState {
 
 	private Image image;
 	InputHandler handle;
 	@Override
	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 		image = new Image("/res/Diary/Epilogue.png");
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame arg1, Graphics arg2) throws SlickException {
 		image.draw();
		handle = new InputHandler(gc);
 	}
 
 	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int arg2) throws SlickException {
 		
		System.out.println("test");
		
		if(handle.Jump()) {
 			gc.exit();
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return 3;
 	}
 
 }
