 package net.cscott.sdr.anim;
 
 import net.cscott.sdr.Version;
 import net.cscott.sdr.anim.TextureText.JustifyX;
 import net.cscott.sdr.anim.TextureText.JustifyY;
 
 import com.jme.image.Texture;
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.input.Mouse;
 import com.jme.input.action.InputAction;
 import com.jme.input.action.InputActionEvent;
 import com.jme.input.action.KeyInputAction;
 import com.jme.input.action.MouseInputAction;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.shape.Quad;
 import com.jme.scene.state.TextureState;
 import com.jme.util.TextureManager;
 import com.jmex.game.state.GameState;
 
 /** The {@link MenuState} displays a cursor on the screen and an appropriate
  *  menu of options.  It uses ORTHO mode and does not reset the camera,
  *  so some other camera-controlling state should also be active for
  *  background visuals.
  * @author C. Scott Ananian
 * @version $Id: MenuState.java,v 1.12 2007-03-07 19:17:20 cananian Exp $
  */
 public class MenuState extends BaseState {
 
     public MenuState(Game game) {
         super(Version.PACKAGE_NAME+" Menu");
         initInput(game);
         initStar();
         initMenus();
         initCursor();
         rootNode.updateRenderState();
         rootNode.updateGeometricState(0, true);
     }
         
     /** The cursor node which holds the mouse gotten from input. */
     private Node cursor;
 
     private InputHandler input;
     private Mouse mouse;
 
     private Star star;
     private float starAngle = 0;
     private Quaternion starRot = new Quaternion();
 
     private Quad selectShade;
     private MenuItem[] menu;
     private int selectTarget;
 
     /**
     * @see com.jmex.game.state.StandardGameState#onActivate()
      */
     public void onActivate() {
         display.setTitle(Version.PACKAGE_STRING+" Main Menu");
     }
         
     /**
      * Inits the input handler we will use for navigation of the menu.
      */
     protected void initInput(Game game) {
         input = new MenuHandler( game );
 
         mouse = new AbsoluteMouse("Mouse Input", display.getWidth(),
                 display.getHeight());
         mouse.registerWithInputHandler( input );
         KeyBindingManager.getKeyBindingManager().set( "menu_up",
                 KeyInput.KEY_UP );
         KeyBindingManager.getKeyBindingManager().set( "menu_down",
                 KeyInput.KEY_DOWN );
         KeyBindingManager.getKeyBindingManager().set( "menu_left",
                 KeyInput.KEY_LEFT );
         KeyBindingManager.getKeyBindingManager().set( "menu_right",
                 KeyInput.KEY_RIGHT );
         input.addAction(new KeyInputAction() {
             public void performAction(InputActionEvent arg0) {
                 if (selectTarget<menu.length-1)
                     doSelect(selectTarget+1);
             }
         }, "menu_up", false);
         input.addAction(new KeyInputAction() {
             public void performAction(InputActionEvent arg0) {
                 if (selectTarget>0)
                     doSelect(selectTarget-1);
             }
         }, "menu_down", false);
         input.addAction(new KeyInputAction() {
             public void performAction(InputActionEvent arg0) {
                 menu[selectTarget].dec();
             }
         }, "menu_left", false);
         input.addAction(new KeyInputAction() {
             public void performAction(InputActionEvent arg0) {
                 menu[selectTarget].inc();
             }
         }, "menu_right", false);
 
         input.addAction(new InputAction() {
             public void performAction(InputActionEvent arg0) {
                 Vector3f mousePos = mouse.getHotSpotPosition();
                 if (mousePos.y > y(127-25) && mousePos.y < y(127+5*64+25)) {
                     int nTarget = (int) (mousePos.y-y(127-32))/64;
                     doSelect(nTarget);
                 }
             }
         }, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, 1/*yaxis*/,
         false);
     }
 
     /**
      * Create a star slowly rotating in the background.
      */
     private void initStar() {
         star = new Star("menu/star", 5, y(400));
         star.getLocalTranslation().set
         (x(320), y(250), 0);
         starAngle = 0;
         starRot.fromAngleAxis(starAngle,Vector3f.UNIT_Z);
         star.setLocalRotation(starRot);
         
         Texture texture =
             TextureManager.loadTexture(
                     MenuState.class.getClassLoader().getResource(
                     "net/cscott/sdr/anim/star-grad.png"),
                     Texture.MM_NONE,
                     Texture.FM_LINEAR);
         
         TextureState ts = display.getRenderer().createTextureState();
         ts.setEnabled(true);
         ts.setTexture(texture);
         star.setRenderState(ts);
         star.setRenderState(mkAlpha());
         star.updateRenderState();
 
         rootNode.attachChild(star);
     }
     
     /**
      * Creates a pretty cursor.
      */
     private void initCursor() {             
         Texture texture =
             TextureManager.loadTexture(
                     MenuState.class.getClassLoader().getResource(
                     "net/cscott/sdr/anim/cursor1.png"),
                     Texture.MM_LINEAR_LINEAR,
                     Texture.FM_LINEAR);
         
         TextureState ts = display.getRenderer().createTextureState();
         ts.setEnabled(true);
         ts.setTexture(texture);
         
         mouse.setRenderState(ts);
         mouse.setRenderState(mkAlpha());
         mouse.setLocalScale(new Vector3f(1, 1, 1));
         
         cursor = new Node("Cursor");
         cursor.attachChild( mouse );
         
         rootNode.attachChild(cursor);
     }
     
     /**
      * Inits the button placed at the center of the screen.
      */
     private void initMenus() {
         mkText("menu/bottom:",
                 "Say \"Square Up\" or press Enter to start, Esc to quit.",
                 128, JustifyX.CENTER, JustifyY.BOTTOM, x(320), y(3), x(620), y(36));
         
         // top one at y center 446
         // bottom one y center 127
         // 6 ovals.  64 pixels between. bottom at 127
         menu = new MenuItem[6];
         String[] labels = { "Judging Difficulty", "Dancers", "Venue", "Dance Level", "Music", "Microphone" };
         String[][] values = { {"Easy","Moderate","Hard"}, {"Checkers"}, {"Mountains"}, {"4-dancer Basic","4-dancer Mainstream","4-dancer Plus","8-dancer Basic","8-dancer Mainstream","8-dancer Plus"}, {"No Music","Saturday Night"}, {"Line Input","Mic Input","USB Input"} };
         for (int i=0; i<6; i++) {
             float y = y(127+64*i);
             RedOval ro = new RedOval("menu/oval", x(509),y(50));
             ro.getLocalTranslation().set(x(320),y,0);
             rootNode.attachChild(ro);
             // XXX lock red ovals in update thread.
         }
         
         selectShade=mkShade("menu/selected", x(320), y(127+5*64), x(640), y(50));
         
         for (int i=0; i<menu.length; i++) {
             float y = y(127+64*i);
             // menu label
             menu[i] = new MenuItem("menu/item "+i, labels[i], this, values[i]);
             menu[i].getLocalTranslation().set(x(320),y,0);
             rootNode.attachChild(menu[i]);
             MouseInputAction mia = menu[i].getMouseInputAction();
             mia.setMouse(mouse);
             input.addAction(mia, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL, InputHandler.AXIS_ALL, false);
         }
         selectTarget = menu.length-1;
         menu[selectTarget].setEnabled(true);
     }
     
     /**
      * Updates input and button.
      * 
      * @param tpf The time since last frame.
      * @see GameState#update(float)
      */
     protected void stateUpdate(float tpf) {
         // rotate the star
         starAngle+=.005f; // slowly rotate the star
         starRot.fromAngleAxis(starAngle,Vector3f.UNIT_Z);
         star.setLocalRotation(starRot);
         // select appropriate menu item
         // XXX accept keyboard input to alter selectTarget as well.
         
         // update the input handler
         input.update(tpf);
         // Check if the button has been pressed.
         rootNode.updateGeometricState(tpf, true);
     }
     private void doSelect(int nTarget) {
         assert nTarget >= 0 && nTarget < menu.length;
         if (selectTarget==nTarget) return;
         menu[selectTarget].setEnabled(false);
         selectTarget = nTarget;
         menu[selectTarget].setEnabled(true);
         selectShade.getLocalTranslation().set(x(320),y(127+64*selectTarget),0);
     }
 }
