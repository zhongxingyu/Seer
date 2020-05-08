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
	public void init(GameContainer gc, StateBasedGame arg1) throws SlickException {
 		image = new Image("/res/Diary/Epilogue.png");
		handle = Play.getHandler();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame arg1, Graphics arg2) throws SlickException {
 		image.draw();
 	}
 
 	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int Delta) throws SlickException {
 		
		if(handle.Up() == true) {
			System.out.println("test");
 			gc.exit();
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return 3;
 	}
 
 }
