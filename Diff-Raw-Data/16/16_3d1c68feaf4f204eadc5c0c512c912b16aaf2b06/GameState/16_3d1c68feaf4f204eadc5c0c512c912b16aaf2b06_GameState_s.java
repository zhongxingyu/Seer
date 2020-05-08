 package core;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import transitions.TransitionWindowOne;
 import framework.DialogBox;
 import framework.HubWindow;
 import framework.Player;
 import framework.TownWindow;
 import framework.TransitionWindow;
 //import java.util.Random;
 
 public class GameState extends BasicGameState {
 	/*-has list of hubWindows
 	 -variable currentHubWindow -which hub we're currently in
 	 -variable eventWindow - which event we're currently in
 	 -list of transitionWindows (explained later)
 	 -queue of strings to be displayed - related to what dialogue/text is
 	 shown during a particular 'event' (also explained later)*/
 	public Player[] players;
 	public DialogBox[] dialogBoxes;
 	public ArrayList<HubWindow> hubWindows;
 	public ArrayList<TransitionWindow> transitionWindows;
 	public HubWindow currentHubWindow;
 	public TransitionWindow currentTransition;
 	public ArrayList<String> textBoxes;
 	public boolean started;
 	public UnicodeFont font;
 	
 	public GameState(int stateID) {
 		super();
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		float[] p1WinSize = { container.getWidth() / 2, container.getHeight() };
 		float[] p2WinSize = { container.getWidth() / 2, container.getHeight() };
 		float[] p1WinPos = { 0, 0 };
 		float[] p2WinPos = { container.getWidth() / 2, 0 };
 
 		HashMap<String, Integer> p1Buttons = new HashMap<String, Integer>();
 		p1Buttons.put("up", Input.KEY_W);
 		p1Buttons.put("left", Input.KEY_A);
 		p1Buttons.put("down", Input.KEY_S);
 		p1Buttons.put("right", Input.KEY_D);
 		p1Buttons.put("action", Input.KEY_T);
 		HashMap<String, Integer> p2Buttons = new HashMap<String, Integer>();
 		p2Buttons.put("up", Input.KEY_UP);
 		p2Buttons.put("left", Input.KEY_LEFT);
 		p2Buttons.put("down", Input.KEY_DOWN);
 		p2Buttons.put("right", Input.KEY_RIGHT);
 		p2Buttons.put("action", Input.KEY_PERIOD);
 
 		players = new Player[2];
 		players[0] = new Player(p1WinPos, p1WinSize, p1Buttons, 1);
 		players[1] = new Player(p2WinPos, p2WinSize, p2Buttons, 2);
 		started = false;
 
 		// levelUp = new Sound("resources/music/levelup.wav");
 		initHubs(container, game);
 		initTransitions();
 
 	}
 
 	// /TODO: ADD MORE HUBS HERE
 	public void initHubs(GameContainer container, StateBasedGame game) {
 		hubWindows = new ArrayList<HubWindow>();
 		try {
 			hubWindows.add(new TownWindow(players, dialogBoxes, new int[] { 5, 14 },
 					new int[] { 10, 14 }));
 			hubWindows.add(new TownWindow(players, dialogBoxes, new int[] { 5, 14 },
 					new int[] { 10, 14 }));
 			currentHubWindow = hubWindows.get(0);
 			currentHubWindow.init(container, game, players);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// TODO: ADD MORE TRANSITION SCREENS
 	public void initTransitions() {
 		transitionWindows = new ArrayList<TransitionWindow>();
 		transitionWindows.add(new TransitionWindowOne(players, dialogBoxes, new int[] {5,12}, new int[] {10,12}));
 		currentTransition = transitionWindows.get(0);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		if ( (hubWindows.size() - 1) == transitionWindows.size()) {
 			currentHubWindow.render(container, game, g);
 		} else {
 			currentTransition.render(container, game, g);
 		}
 		
 		dialogBoxes[0].render(container, g);
 		dialogBoxes[1].render(container, g);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 		started = true;
 		Input input = container.getInput();
 
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			container.exit();
 		}
 		
 		if (currentHubWindow.over()) {
 			hubWindows.remove(0);
 			currentHubWindow = hubWindows.get(0);
 			currentTransition.init(container, game, players);
 		}
 
 		if (currentTransition.over()) {
 			transitionWindows.remove(0);
 			currentTransition = transitionWindows.get(0);
 		}
 
 		if ( (hubWindows.size() -1 ) == transitionWindows.size()) {
 			currentHubWindow.update(container, game, players, delta);
 		} else {
 			currentTransition.update(container, game, players, delta);
 		}
 		
 		dialogBoxes[0].update(container, delta);
 		dialogBoxes[1].update(container, delta);
 	}
 
 	@Override
 	public int getID() {
 		return 1;
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		
         Font awtFont = new Font("Arial Monospaced", Font.BOLD, 18);
 	    font = new UnicodeFont(awtFont);
 	    font.getEffects().add(new ColorEffect(java.awt.Color.black));
 	    font.addAsciiGlyphs();
 	    font.loadGlyphs();
 
 		float[] p1WinSize = { container.getWidth() / 2 - 16,
 				container.getHeight() };
 		float[] p2WinSize = { container.getWidth() / 2 - 16,
 				container.getHeight() };
 		float[] p1WinPos = { 0, 0 };
 		float[] p2WinPos = { container.getWidth() / 2 + 16, 0 };
 
 		HashMap<String, Integer> p1Buttons = new HashMap<String, Integer>();
 		p1Buttons.put("up", Input.KEY_W);
 		p1Buttons.put("left", Input.KEY_A);
 		p1Buttons.put("down", Input.KEY_S);
 		p1Buttons.put("right", Input.KEY_D);
 		p1Buttons.put("action", Input.KEY_T);
 		HashMap<String, Integer> p2Buttons = new HashMap<String, Integer>();
 		p2Buttons.put("up", Input.KEY_UP);
 		p2Buttons.put("left", Input.KEY_LEFT);
 		p2Buttons.put("down", Input.KEY_DOWN);
 		p2Buttons.put("right", Input.KEY_RIGHT);
 		p2Buttons.put("action", Input.KEY_PERIOD);
 
 		players = new Player[2];
 		players[0] = new Player(p1WinPos, p1WinSize, p1Buttons, 1);
 		players[1] = new Player(p2WinPos, p2WinSize, p2Buttons, 2);
 		
 		dialogBoxes = new DialogBox[2];
		dialogBoxes[0] = new DialogBox(0, container.getHeight()-100, container.getWidth() / 2 - 16, 100, font, players[0].getButton("right"));
		dialogBoxes[1] = new DialogBox( container.getWidth() / 2 + 16, container.getHeight()-100, container.getWidth() / 2 - 16, 100, font, players[1].getButton("right"));
 		
 		started = false;
 
 		// levelUp = new Sound("resources/music/levelup.wav");
 		initHubs(container, game);
 		initTransitions();
 
 	}
 
 	@Override
 	public void leave(GameContainer container, StateBasedGame game)
 			throws SlickException {
 	}
 
 }
