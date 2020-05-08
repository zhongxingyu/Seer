 package net.cscott.sdr.anim;
 
 import net.cscott.sdr.Version;
 import net.cscott.sdr.anim.TextureText.JustifyX;
 import net.cscott.sdr.anim.TextureText.JustifyY;
 
 import com.jme.image.Texture;
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.InputHandler;
 import com.jme.input.Mouse;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.scene.Node;
 import com.jme.scene.state.AlphaState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.TextureState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 import com.jmex.game.state.GameState;
 import com.jmex.game.state.StandardGameStateDefaultCamera;
 
 /** The {@link MenuState} displays a cursor on the screen and an appropriate
  *  menu of options.  It uses ORTHO mode and does not reset the camera,
  *  so some other camera-controlling state should also be active for
  *  background visuals.
  * @author C. Scott Ananian
 * @version $Id: MenuState.java,v 1.9 2006-11-13 04:27:09 cananian Exp $
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
 
     private TextureText text;
     
     private InputHandler input;
     private Mouse mouse;
 
     private Star star;
     private float starAngle = 0;
     private Quaternion starRot = new Quaternion();
     
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
     }
 
     /**
      * Create a star slowly rotating in the background.
      */
     private void initStar() {
        star = new Star("menu/star", 5, y(200));
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
         text = mkText("menu/bottom:",
                 "Say \"Square Up\" or press Enter to start, Esc to quit.",
                128, JustifyX.CENTER, JustifyY.BOTTOM, x(320), y(3), x(620), y(36));
         
         // top one at y center 446
         // bottom one y center 127
         // 6 ovals.  64 pixels between. bottom at 127
         String[] labels = { "Judging Difficulty", "Dancers", "Venue", "Dance Level", "Music", "Microphone" };
         String[] values = { "Moderate", "Checkers", "Mountains", "4-dancer Plus", "Music Off", "Line Input" };
         for (int i=0; i<6; i++) {
             float y = y(127+64*i);
             RedOval ro = new RedOval("menu/oval", x(509),y(50));
             ro.getLocalTranslation().set(x(320),y,0);
             rootNode.attachChild(ro);
         }
         
         mkShade("menu/selected", x(320), y(127+5*64), x(640), y(50));
         
         for (int i=0; i<6; i++) {
             float y = y(127+64*i);
             // menu label
             TextureText tt = mkText("menu/menu text:", labels[i], 128,
                     JustifyX.LEFT, JustifyY.MIDDLE, x(83),y,x(280),y(44));
             tt.setColor(new ColorRGBA(.95f,.95f,.95f,1));
             // menu arrows: height 44, width 24
   
             MenuArrow leftArrow = new MenuArrow
            ("menu/arrow "+i+"/left", this, true);
             leftArrow.getLocalTranslation().set(x(468-75-1),y,0);
             rootNode.attachChild(leftArrow);
 
             MenuArrow rightArrow = new MenuArrow
            ("menu/arrow "+i+"/right", this, false);
             rightArrow.getLocalTranslation().set(x(468+75+1),y,0);
             rootNode.attachChild(rightArrow);
 
             if (i==5) { leftArrow.setSelected(true); rightArrow.setSelected(true); }
             
             // menu values
             tt = mkText("menu/menu value:", values[i], 128,
                     JustifyX.CENTER, JustifyY.MIDDLE, x(468), y, x(150),y(24));
             tt.setColor(new ColorRGBA(1,1,0,1));
         }
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
         // update the input handler
         input.update(tpf);
         // Check if the button has been pressed.
         rootNode.updateGeometricState(tpf, true);
     }
 }
