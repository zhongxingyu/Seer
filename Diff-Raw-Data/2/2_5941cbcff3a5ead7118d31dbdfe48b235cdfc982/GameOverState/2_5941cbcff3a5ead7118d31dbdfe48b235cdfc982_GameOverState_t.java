 package states;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioRenderer;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.input.FlyByCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.material.RenderState;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.ImageBuilder;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.PopupBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.TextBuilder;
 import de.lessvoid.nifty.controls.TextField;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.textfield.builder.TextFieldBuilder;
 import de.lessvoid.nifty.controls.window.builder.WindowBuilder;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import de.lessvoid.nifty.tools.Color;
 import game.MainApp;
 
 public class GameOverState extends AbstractAppState implements ScreenController {
     
     protected Node rootNode = new Node("Root Node");
     protected Node guiNode = new Node("Gui Node");
     protected BitmapText menuText;
     protected BitmapFont menuFont;
     private AssetManager assetManager;
     private AppStateManager stateManager;
     private InputManager inputManager;
     private ViewPort viewPort;
     private MainApp game = null;
     private AudioRenderer audioRenderer;
     private ViewPort guiViewPort;
     private NiftyJmeDisplay niftyDisplay;
     private FlyByCamera flyCam;
     private Nifty nifty;
     public static boolean b = false;
     
     public GameOverState(MainApp game) {
         this.game = game;
     }
     
     public void bind(Nifty nifty, Screen screen) {
         this.nifty = nifty;
     }
     
     public void onStartScreen() {
     }
     
     public void onEndScreen() {
     }
     
     private class AppActionListener implements ActionListener {
         
         public void onAction(String name, boolean value, float tpf) {
             if (!value) {
                 return;
             }
             if(name.equals("Exit"))
            System.exit(0);
         }
     }
     
     public void loadFPSText() {
         menuFont = game.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
         menuText = new BitmapText(menuFont, false);
         menuText.setSize(menuFont.getCharSet().getRenderedSize());
         menuText.setLocalTranslation(0, (game.getContext().getSettings().getHeight() / 2f) - (menuText.getLineHeight() / 2f), 0);
         menuText.setText("Frames per second");
         guiNode.attachChild(menuText);
     }
     
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.game = (MainApp) game;
         
         this.assetManager = this.game.getAssetManager();
         this.stateManager = this.game.getStateManager();
         this.inputManager = this.game.getInputManager();
         this.viewPort = this.game.getViewPort();
         this.audioRenderer = this.game.getAudioRenderer();
         this.guiViewPort = this.game.getGuiViewPort();
         this.flyCam = new FlyByCamera(game.getCamera());
 
         // enable depth test and back-face culling for performance
         game.getRenderer().applyRenderState(RenderState.DEFAULT);
 
 
         // Init input
         if (game.getInputManager() != null) {
             game.getInputManager().addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
         }
         
         
         niftyDisplay = new NiftyJmeDisplay(
                 assetManager, inputManager, audioRenderer, guiViewPort);
         
         
         nifty = niftyDisplay.getNifty();
         guiViewPort.addProcessor(niftyDisplay);
         flyCam.setDragToRotate(true);
         nifty.loadStyleFile("nifty-default-styles.xml");
         nifty.loadControlFile("nifty-default-controls.xml");
         
         inputManager.setCursorVisible(true);
         
         
         
         nifty.addScreen("GameOverScreen", new ScreenBuilder("GameOverScreen") {
             {
                 //controller(new GUI.PowdersScreenController()); // This connects the Java class StartingScreen and the GUI screen.     
                 controller(new MenuState(game));
                 layer(new LayerBuilder("LayerGameOver") {
                     {
                         backgroundColor(Color.BLACK);
                     }
                 });
                 layer(new LayerBuilder("LayerGameOver") {
                     {
                         childLayoutVertical(); // layer properties, add more...
 
                         panel(new PanelBuilder("Panel_TITLE") {
                             {
                                 childLayoutCenter();
                                 alignCenter();
                                 valignBottom();
                                 height("10%");
                                 width("90%");
 
                                 // add text
 
                                 text(new TextBuilder() {
                                     {
                                         text("GAME OVER");
                                         font("Interface/Fonts/Default.fnt");
                                         height("100%");
                                         width("100%");
                                     }
                                 });
                             }
                         }); // </panel_1>
 
                         panel(new PanelBuilder("Panel_Buttons") {
                             {
                                 childLayoutVertical(); // panel properties, add more...               
                                 alignCenter();
                                 valignCenter();
                                 height("20%");
                                 width("100%");
                             }
                         }); // </panel_1>
 
                         panel(new PanelBuilder("Panel_Buttons") {
                             {
                                 childLayoutVertical(); // panel properties, add more...               
                                 alignCenter();
                                 valignCenter();
                                 height("60%");
                                 width("100%");
                                 
                                 control(new ButtonBuilder("PLAY AGAIN", "PLAY AGAIN") {
                                     {
                                         alignCenter();
                                         valignTop();
                                         backgroundColor("#f108");
                                         height("15%");
                                         width("30%");
                                         visibleToMouse(true);
                                         interactOnClick("loadGameFromGameOver()");
                                         
                                     }
                                 });
                                 panel(new PanelBuilder("Panel_Buttons") {
                                     {
                                         childLayoutVertical(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("20%");
                                         width("100%");
                                     }
                                 }); // </panel_1>
 
                                 
                                 control(new ButtonBuilder("BACK TO MENU", "BACK TO MENU") {
                                     {
                                         alignCenter();
                                         valignCenter();
                                         backgroundColor("#f108");
                                         height("15%");
                                         width("30%");
                                         visibleToMouse(true);
                                         interactOnClick("loadMenuFromGaveOver()");
                                         
                                     }
                                 });
                                 
                                 panel(new PanelBuilder("Panel_Buttons") {
                                     {
                                         childLayoutVertical(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("20%");
                                         width("100%");
                                     }
                                 }); // </panel_1>
 
                                 control(new ButtonBuilder("EXIT", "EXIT") {
                                     {
                                         alignCenter();
                                         valignBottom();
                                         backgroundColor("#f108");
                                         height("15%");
                                         width("30%");
                                         visibleToMouse(true);
                                         interactOnClick("exit()");
                                         
                                     }
                                 });
                                 
                                 
                                 
                             }
                         }); // </panel_2>
 
                         
                     }
                 });
             }
         }.build(nifty));
         
         
         game.getGUIViewPort().addProcessor(niftyDisplay);
         nifty.gotoScreen("GameOverScreen"); // it is used to start the screen
     }
     
     public void update(float tpf) {
         super.update(tpf);
 
         // simple update and root node
 
         rootNode.updateLogicalState(tpf);
         guiNode.updateLogicalState(tpf);
         rootNode.updateGeometricState();
         guiNode.updateGeometricState();
         
     }
     
     @Override
     public void stateAttached(AppStateManager stateManager) {
         super.stateAttached(stateManager);
          game.getInputManager().addListener(new AppActionListener(), "Exit");
         game.getViewPort().attachScene(rootNode);
         game.getGUIViewPort().attachScene(guiNode);
         if (b == true) {
             game.getGUIViewPort().addProcessor(niftyDisplay);
         }
 //      
     }
     
     @Override
     public void stateDetached(AppStateManager stateManager) {
         super.stateDetached(stateManager);
         game.getViewPort().detachScene(rootNode);
         game.getGUIViewPort().detachScene(guiNode);
         game.getGUIViewPort().removeProcessor(niftyDisplay);
     }
     
     public void render(RenderManager rm) {
     }
 }
