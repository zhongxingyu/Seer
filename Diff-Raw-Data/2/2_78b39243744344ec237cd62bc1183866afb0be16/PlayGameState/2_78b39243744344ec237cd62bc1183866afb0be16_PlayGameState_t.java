 package run;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import core.Player;
 
 public class PlayGameState extends BasicGameState {
 
     // states holds two stacks of Windows, one for each of the player views
     // the top state of each stack will be rendered each time render is called on this object
     public ArrayList<Stack<Window>> states;
     public Player[] players;
 
     public Image background;
 
     public boolean started;
 
     private Set<Integer> startKeys;
 
     public UnicodeFont uFont;
 
     public PlayGameState() {
         super();
         float[] p1WinSize = { 399, 600 };
         float[] p2WinSize = { 399, 600 };
         float[] p1WinPos = { 0, 0 };
         float[] p2WinPos = { 400, 0 };
 
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
         players[0] = new Player(p1WinPos, p1WinSize, p1Buttons);
         players[1] = new Player(p2WinPos, p2WinSize, p2Buttons);
 
         started = false;
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         // show a background
         Image im = this.background;
         g.drawImage(im, 0, 0);
 
         // g.fillRect(390, 0, 20, 599);
         // maintain two internal states. Render one on each side of the screen
         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             Window windowedState = stack.peek();
             windowedState.render(container, game, g, players[i]);
         }
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         this.background = new Image("resources/big-background.png");
 
         Music loop = new Music("resources/music/five-minutes_longloop.wav");
         loop.loop();
 
         this.states = new ArrayList<Stack<Window>>();
         Stack<Window> states1 = new Stack<Window>();
         Stack<Window> states2 = new Stack<Window>();
 
         states1.push(new MainWindow(players[0]));
         states2.push(new MainWindow(players[1]));
 
         states.add(states1);
         states.add(states2);
 
         String fontPath = "resources/cantarell.ttf";
         uFont = new UnicodeFont(fontPath, 20, false, false);
         uFont.addAsciiGlyphs();
         uFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         uFont.loadGlyphs();
 
         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             Window windowedState = stack.peek();
             windowedState.init(container, game, players[i]);
         }
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         Input input = container.getInput();
 
         for (int key : startKeys) {
            if (input.isKeyDown(key)) {
                 started = true;
             }
         }
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             container.exit();
         }
 
         if (started) {
             for (int i = 0; i < this.states.size(); i++) {
                 Stack<Window> stack = this.states.get(i);
                 Window windowedState = stack.peek();
 
                 // note: update before or after?
                 if (windowedState.over() == true) {
                     stack.pop();
                 }
                 windowedState.update(container, game, delta, players[i]);
             }
         }
     }
 
     protected void triggerMinigame(GameContainer container, StateBasedGame game, Player player, Window minigame)
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
     public void enter(GameContainer container, StateBasedGame game) {
         for (int i = 0; i < this.states.size(); i++) {
             Stack<Window> stack = this.states.get(i);
             Window windowedState = stack.peek();
             windowedState.enter(container, game, players[i]);
         }
     }
 }
