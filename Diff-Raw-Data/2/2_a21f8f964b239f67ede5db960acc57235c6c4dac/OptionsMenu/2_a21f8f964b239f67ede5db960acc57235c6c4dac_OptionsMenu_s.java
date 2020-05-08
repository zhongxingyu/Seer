 package yaz.game.states;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import yaz.game.handling.ResourceHandling;
 import yaz.game.main.yaz;
 
 public class OptionsMenu extends BasicGameState {
 
 	ResourceHandling reshandle = null;
 	protected int OPTIONSMENU = 0;
 	
 	boolean IsInBackButton = false;
 	boolean IsInDebugButton = false;
 	
	Rectangle Res = new Rectangle(48, 275, 275, 20);
 
 	private int MouseX, MouseY;
 	
 	public OptionsMenu(int optionsMenuState){
 		this.OPTIONSMENU = optionsMenuState;
 	}
 	
 	@Override
 	public int getID() {
 		return OPTIONSMENU;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame stg) throws SlickException {
 		this.reshandle = new ResourceHandling();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame stg, Graphics g) throws SlickException {
 		ResourceHandling.OPT_Background.draw(0, 0);
 		g.drawString("Current Resolution: "+ yaz.ScreenWidth + ", " + yaz.ScreenHeight, 50, 250);
 		g.draw(Res);
 		ResourceHandling.GAME_BackButton.draw(10, 650);
 		if(IsInBackButton)
 			ResourceHandling.GAME_BackButton_Alt.draw(10, 650);
 		else
 			ResourceHandling.GAME_BackButton.draw(10, 650);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame stg, int delta) throws SlickException {
 		Input i = gc.getInput();
 		MouseX = i.getMouseX();
 		MouseY = i.getMouseY();
 		if(i.isKeyPressed(Input.KEY_ENTER) || IsInBackButton && i.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			stg.enterState(1, new FadeOutTransition(), new FadeInTransition());
 		}
 		
 		if((MouseX >= 10 && MouseX <= 10 + ResourceHandling.GAME_BackButton.getWidth()) && (MouseY >= 650 && MouseY <= 650 + ResourceHandling.GAME_BackButton.getHeight())) {
 			IsInBackButton = true;
 		}else{
 			IsInBackButton = false;
 		}
 		
 		if(i.isMousePressed(Input.MOUSE_LEFT_BUTTON)){
 			if(IsInDebugButton){
 				yaz.DebugMode = !yaz.DebugMode;
 			}
 		}
 		
 	}
 }
