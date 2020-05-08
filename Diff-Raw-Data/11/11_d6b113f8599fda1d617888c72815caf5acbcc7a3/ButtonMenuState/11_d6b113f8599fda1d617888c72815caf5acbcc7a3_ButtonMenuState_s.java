 package indaprojekt;
 
 import indaprojekt.Button.ActionPerformer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public abstract class ButtonMenuState extends BasicGameState 
 {
 	private int stateID;
 	
 	protected Image background;
 	private List<Button> buttons;
 	private Map<Integer, ActionPerformer> keyMap;
 	private Sound theme;
 	
 	public ButtonMenuState(int stateID) 
 	{
 		this.stateID = stateID;
 		buttons = new ArrayList<Button>();
 		keyMap = new HashMap<Integer, ActionPerformer>();
 	}
 	
 	protected void setTheme(Sound theme)
 	{
 		this.theme = theme;
 	}
 	
 	protected void addButton(Button button)
 	{
 		buttons.add(button);
 	}
 
 	
 	protected void mapKey(int key, ActionPerformer action)
 	{
 		keyMap.put(key, action);
 	}
 	
 	@Override
 	public void init(final GameContainer gc, final StateBasedGame game)
 			throws SlickException 
 	{
 		background = new Image("res//images//bakgrund.png");
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g)
 			throws SlickException 
 	{
 		background.draw(0, 0);
 		
 		for (Button button : buttons) {
 			button.draw();
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException 
 	{
 		Input input = gc.getInput();
 
 		for (Button button : buttons) {
 			button.doLogic(delta, input);
 		}
 		
 		for (Integer i : keyMap.keySet()) {
 			if (input.isKeyPressed(i)) {
 				keyMap.get(i).doAction();
 			}
 		}
 		
		if (!theme.playing()) {
 			theme.loop();
 		}
 	}
 
 	@Override
 	public int getID() 
 	{
 		return stateID;
 	}
 
 }
