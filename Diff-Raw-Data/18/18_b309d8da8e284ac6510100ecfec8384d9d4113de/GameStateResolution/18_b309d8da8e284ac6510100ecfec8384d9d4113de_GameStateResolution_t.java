 package gui;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.SlickCallable;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import de.lessvoid.nifty.EndNotify;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.input.mouse.MouseInputEvent;
 import de.lessvoid.nifty.lwjglslick.render.RenderDeviceLwjgl;
 import de.lessvoid.nifty.lwjglslick.sound.SlickSoundDevice;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import de.lessvoid.nifty.sound.SoundSystem;
 import de.lessvoid.nifty.spi.input.InputSystem;
 import de.lessvoid.nifty.tools.TimeProvider;
 
 /**
  * Game state to alter the resolution
 * @deprecated This is currently not used
  */
 public class GameStateResolution extends BasicGameState implements ScreenController {
   public static final int ID = 1;
   private GameContainer container;
   private Nifty nifty;
   private List<MouseInputEvent> mouseEvents = new ArrayList<MouseInputEvent>();
   private int mouseX;
   private int mouseY;
   private boolean mouseDown;
   private boolean showFPS = false;
   private StateBasedGame game;
 
   
   @Override
 public int getID() {
     return ID;
   }
 
   @Override
 public void init(GameContainer container, StateBasedGame game) throws SlickException {
     this.container = container;
     this.game = game;
     // create nifty (gui)
     nifty = new Nifty(new RenderDeviceLwjgl(), new SoundSystem(new SlickSoundDevice()), new InputSystem() {
       @Override
 	public List<MouseInputEvent> getMouseEvents() {
         ArrayList<MouseInputEvent> result = new ArrayList<MouseInputEvent>(mouseEvents);
         mouseEvents.clear();
         return result;
       }
     }, new TimeProvider());
     nifty.fromXml("ressources/gui/resolutionMenu.xml", "start", this);
   }
 
   @Override
 public void render(GameContainer container, StateBasedGame game, Graphics g) {
     SlickCallable.enterSafeBlock();
     nifty.render(false);
     SlickCallable.leaveSafeBlock();
   }
 
   @Override
 public void update(GameContainer container, StateBasedGame game, int delta) {
 	  container.setShowFPS(this.showFPS);
   }
 
   @Override
 public void keyReleased(int key, char c) {
     
      /*
     if (key == Input.KEY_1) {
       currentColor = Color.red;
       getElement("red").startEffect(EffectEventId.onCustom);
     }
     if (key == Input.KEY_2) {
       currentColor = Color.green;
       getElement("green").startEffect(EffectEventId.onCustom);
     }
     */
     if (key == Input.KEY_F) {
     	if(this.showFPS) {
     		this.showFPS = false;
     	}
     	else {
     		this.showFPS = true;
     	}
     }
     
     
   }
 
 
   @Override
 public void mouseMoved(final int oldx, final int oldy, final int newx, final int newy) {
 	mouseX = newx;
     mouseY = newy;
     forwardMouseEventToNifty(mouseX, mouseY, mouseDown);
   }
 
   @Override
 public void mousePressed(final int button, final int x, final int y) {
     mouseX = x;
     mouseY = y;
     mouseDown = true;
     forwardMouseEventToNifty(mouseX, mouseY, mouseDown);
   }
 
   @Override
 public void mouseReleased(final int button, final int x, final int y) {
 	mouseX = x;
     mouseY = y;
     mouseDown = false;
     forwardMouseEventToNifty(mouseX, mouseY, mouseDown);
   }
 
   private void forwardMouseEventToNifty(final int mouseX, final int mouseY, final boolean mouseDown) {
 	  mouseEvents.add(new MouseInputEvent(mouseX, container.getHeight() - mouseY, mouseDown));
   }
 
   @Override
 public void bind(Nifty nifty, Screen screen) {
   }
 
   @Override
 public void onEndScreen() {
   }
 
   @Override
 public void onStartScreen() {
   }
 
   public void quit() {
     nifty.getCurrentScreen().endScreen(new EndNotify() {
       @Override
 	public void perform() {
         container.exit();
       }
     });
   }
   
   public void exit() {
 	  nifty.getCurrentScreen().endScreen(new EndNotify() {
 	      @Override
 		public void perform() {
 	        game.enterState(0);
 	      }
 	    });
   }
 }
