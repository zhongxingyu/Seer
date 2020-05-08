 import java.awt.Color;
 import java.awt.Font;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.ResourceLoader;
 
 public class GameState extends BasicGameState {
 
     // states holds two stacks of Windows, one for each of the player views
     // the top state of each stack swill be rendered each time render is called
     // on this object
     public ArrayList<Stack<Window>> states;
     public NodeWindow masterState;
     public Player[] players;
 
     public Audio music;
     
     public Image background;
 
     public boolean started;
     public Player wonPlayer;
 
     private Set<Integer> startKeys;
 
     private PopUp currentPopUp;
 
     public UnicodeFont uFont;
 
     public int[] delay = { 0, 0 };
     public float maxDelay;
 
     public Sound levelUp;
     int stateID;
 
     public GameState(int stateID) {
         super();
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g)
             throws SlickException {
         // show a background
         // Image im = this.background;
         // g.drawImage(im, 0, 0);
         this.masterState.render(container, game, g, players);
 
         // maintain two internal states. Render one on each side of the screen
         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             if (stack.size() > 0) {
                 Window windowedState = stack.peek();
                 windowedState.render(container, game, g, players[i]);
             }
         }
         /*
          * for (int i = 0; i < this.states.size(); i++) { if (delay[i] > 0) {
          * g.draw(new Rectangle(50 + container.getWidth()/2 * i, 275, delay[i] *
          * 300 / maxDelay, 50)); } }
          */
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void init(GameContainer container, StateBasedGame game)
             throws SlickException {
 
         float[] p1WinSize = { container.getWidth() / 2, container.getHeight() };
         float[] p2WinSize = { container.getWidth() / 2, container.getHeight() };
         float[] p1WinPos = { 0, 0 };
         float[] p2WinPos = { container.getWidth() / 2, 0 };

	    try {
			music = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("Assets/Town2.wav"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
         this.stateID = stateID;
 
         maxDelay = 60;
 
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
 
         startKeys = new TreeSet<Integer>();
         startKeys.addAll(p1Buttons.values());
         startKeys.addAll(p2Buttons.values());
 
         players = new Player[2];
         players[0] = new Player(p1WinPos, p1WinSize, p1Buttons, 1);
         players[1] = new Player(p2WinPos, p2WinSize, p2Buttons, 2);
         started = false;
         wonPlayer = null;
 
         // levelUp = new Sound("resources/music/levelup.wav");
 
        // Music loop = new Music("resources/music/five-minutes_longloop.wav");
        // loop.loop();
         this.masterState = new TownWindow(players);
         this.states = new ArrayList<Stack<Window>>();
         Stack<Window> states1 = new Stack<Window>();
         Stack<Window> states2 = new Stack<Window>();
 
         // states1.push(new DodgeWindow(players[0]));
         // states2.push(new DodgeWindow(players[1]));
 
         states.add(states1);
         states.add(states2);
 
         uFont = MainGame.loadFont("Arial Monospaced", Font.BOLD, 40,
                 Color.WHITE);
 
        masterState.init(container, game, players);

         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             if (stack.size() != 0) {
                 Window windowedState = stack.peek();
                 windowedState.init(container, game, players[i]);
             }
         }
 
         currentPopUp = null;
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta)
             throws SlickException {
         Input input = container.getInput();
 
		music.playAsSoundEffect(1.0f, .5f, false);

 		for (int key : startKeys) {
             if (input.isKeyDown(key)) {
                 started = true;
             }
         }
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             container.exit();
         }
 
         masterState.update(container, game, delta, players);
         if (masterState.over == true) {
             game.enterState(0);
         }
 
         if (started && wonPlayer == null) {
             for (int i = 0; i < this.states.size(); i++) {
                 Stack<Window> stack = this.states.get(i);
                 if (stack.size() > 0) {
                     Window windowedState = stack.peek();
                     // note: update before or after?
                     if (windowedState.over() == true) {
                         stack.pop();
                         masterState.inNode[i] = true;
                         delay[i] = (int) maxDelay;
                     }
 
                     if (delay[i] > 0) {
                         delay[i]--;
                     } else {
                         windowedState
                                 .update(container, game, delta, players[i]);
                     }
                 }
             }
 
         }
     }
 
     protected void triggerMinigame(GameContainer container,
             StateBasedGame game, Player player, Window minigame)
             throws SlickException {
         int playerIndex = (player == players[0]) ? 0 : 1;
         this.states.get(playerIndex).push(minigame);
         minigame.init(container, game, player);
     }
 
     @Override
     public int getID() {
         return 1;
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game)
             throws SlickException {
         this.masterState = new TownWindow(players);
         this.states = new ArrayList<Stack<Window>>();
         Stack<Window> states1 = new Stack<Window>();
         Stack<Window> states2 = new Stack<Window>();
 
         // states1.push(new DodgeWindow(players[0]));
         // states2.push(new DodgeWindow(players[1]));
 
         states.add(states1);
         states.add(states2);
 
         masterState.init(container, game, players);
 
         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             if (stack.size() != 0) {
                 Window windowedState = stack.peek();
                 windowedState.init(container, game, players[i]);
             }
             // TODO Auto-generated catch block
         }
     }
 }
