 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
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
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.TextBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.textfield.builder.TextFieldBuilder;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 public class InputState extends AbstractAppState implements ScreenController {
 
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
     private String titulos[][] = {{"Comienzo", "Start"},
         {"Estadísticas", "Statistics"}, {"¿?", "?¿"}, {"Salir", "Quit"}};
     private int i = 0;
     public static boolean b=false;
 
     public InputState(MainApp game) {
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
         this.game = (MainApp) game; // can cast Application to something more specific
 
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
             game.getInputManager().addMapping("SIMPLEAPP_Exit1", new KeyTrigger(KeyInput.KEY_0));
         }
 
 
         
             niftyDisplay = new NiftyJmeDisplay(
                     assetManager, inputManager, audioRenderer, guiViewPort);
 
         
         nifty = niftyDisplay.getNifty();
         guiViewPort.addProcessor(niftyDisplay);
         flyCam.setDragToRotate(true);
         nifty.loadStyleFile("nifty-default-styles.xml");
         nifty.loadControlFile("nifty-default-controls.xml");
 
         inputManager.setCursorVisible(true);
 
 
 
         nifty.addScreen("InputScreen", new ScreenBuilder("IScreen") {
             {
                 //controller(new GUI.PowdersScreenController()); // This connects the Java class StartingScreen and the GUI screen.     
                 controller(new MenuState(game));
 
 
                 layer(new LayerBuilder("Layer_ID2") {
                     {
                         childLayoutVertical(); // layer properties, add more...
 
                         // <panel_1>
                         panel(new PanelBuilder("Panel_TITLE2") {
                             {
                                 childLayoutCenter();
                                 alignCenter();
                                 height("15%");
                                 width("50%");
 
                                 // add text
 
                                 text(new TextBuilder() {
                                     {
                                         text("SIGN UP");
                                         font("Interface/Fonts/Default.fnt");
                                         height("100%");
                                         width("100%");
                                     }
                                 });
                             }
                         }); // </panel_1>
 
                         panel(new PanelBuilder("Panel_TITLE2") {
                             {
                                 childLayoutCenter();
                                 alignCenter();
                                 height("10%");
                                 width("50%");
 
 
                             }
                         }); // </panel_1>
 
                         // <panel_2>
                         panel(new PanelBuilder("Panel_BUTTONS2") {
                             {
                                 childLayoutVertical(); // panel properties, add more...               
                                 alignCenter();
                                 height("60%");
                                 width("50%");
 
                                 panel(new PanelBuilder("Panel_EMPTY2") {
                                     {
                                         childLayoutCenter();
                                         height("16%");
                                         width("55%");
                                         alignCenter();
                                         valignCenter();
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 text("Insert name:");
                                                 font("Interface/Fonts/Default.fnt");
                                                 height("100%");
                                                 width("100%");
                                             }
                                         });
 
                                     }
                                 });
 
                                 panel(new PanelBuilder("Panel_START2") {
                                     {
                                         alignCenter();
                                         valignCenter();
                                         childLayoutCenter();
                                         height("16%");
                                         width("55%");
 
 
                                         control(new TextFieldBuilder("NameInput1", "nombre") {
                                             {
                                                 interactOnClick("");
                                                 width("200px");
                                             }
                                         });
 
                                     }
                                 });// </panel_2.1>
 
                                 // <panel_2.2>
                                 panel(new PanelBuilder("Panel_STATISTICS2") {
                                     {
                                         childLayoutCenter();
                                         alignCenter();
                                         valignCenter();
                                         height("16%");
                                         width("55%");
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 text("Insert nickname:");
                                                 font("Interface/Fonts/Default.fnt");
                                                 height("100%");
                                                 width("100%");
                                             }
                                         });
                                     }
                                 });// </panel_2.2>
 
                                 // <panel_2.3>
                                 panel(new PanelBuilder("Panel_2") {
                                     {
                                         childLayoutCenter();
                                         alignCenter();
                                         valignCenter();
                                         height("16%");
                                         width("55%");
 
 
                                         control(new TextFieldBuilder("NameInput2", "nick") {
                                             {
                                                 width("200px");
                                             }
                                         });
 
 
 
                                     }
                                 });// </panel_2.3>
 
                                 // <panel_2.4>
                                 panel(new PanelBuilder("Panel_QUIT2") {
                                     {
                                         childLayoutHorizontal();
                                         alignCenter();
                                         valignCenter();
                                         height("16%");
                                         width("55%");
 
                                         // GUI element
                                         control(new ButtonBuilder("Button_OK2", "OK") {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 backgroundColor("#f108");
                                                 height("50%");
                                                 width("40%");
                                                 visibleToMouse(true);
                                                 interactOnClick("exit()");
 
                                             }
                                         });
 
                                         panel(new PanelBuilder("Panel_PANELEMPTY2") {
                                             {
                                                 childLayoutHorizontal();
                                                 alignCenter();
                                                 valignCenter();
                                                 height("16%");
                                                 width("20%");
                                                 interactOnClick("start");
                                             }
                                         });
 
                                         control(new ButtonBuilder("Button_QUIT2", "Back") {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 backgroundColor("#f108");
                                                 height("50%");
                                                 width("40%");
                                                 visibleToMouse(true);
                                                 interactOnClick("loadMenu2()");
 
                                             }
                                         });
                                     }
                                 });// </panel_2.4>
 
                             }
                         }); // </panel_2>
 
                         // <panel_3>
                         panel(new PanelBuilder("Panel_LANGUAGES2") {
                             {
                                 childLayoutHorizontal();
                                 alignCenter();
                                 height("15%");
                                 width("100%");
 
                                 // <panel_3.1>
                                 panel(new PanelBuilder("Panel_EMPTY3.12") {
                                     {
                                         childLayoutCenter();
                                         valignCenter();
                                         height("50%");
                                         width("50%");
                                     }
                                 }); // </panel_3.1>
 
                                 // <panel_3.2>
                                 panel(new PanelBuilder("Panel_IN_LANGUAGES2") {
                                     {
                                         childLayoutHorizontal();
                                         valignCenter();
                                         height("50%");
                                         width("50%"); ////////////////// NESPÂÑOL ENGLIH
 
                                         // <panel_3.2.1>
                                         panel(new PanelBuilder("Panel_EMPTY3.2.12") {
                                             {
                                                 childLayoutHorizontal();
                                                 valignCenter();
                                                 height("100%");
                                                 width("70%");
 
 
                                             }
                                         }); // </panel_3.2.1>
 
                                         // <panel_3.2.2>
                                         panel(new PanelBuilder("Panel_UK2") {
                                             {
                                                 childLayoutHorizontal();
                                                 valignCenter();
                                                 height("75%");
                                                 width("10%");
 
                                                 // add image
                                                 image(new ImageBuilder() {
                                                     {
                                                         valignCenter();
                                                         alignRight();
                                                         height("100%");
                                                         width("100%");
                                                         filename("Pictures/uk.png");
                                                         visibleToMouse(true);
                                                         interactOnClick("i = getInt1()");
                                                         interactOnRelease("aplicacion.restart()");
                                                     }
                                                 });
 
 
                                             }
                                         }); // </panel_3.2.2>
 
                                         // <panel_3.2.3>
                                         panel(new PanelBuilder("Panel_EMPTY3.2.32") {
                                             {
                                                 childLayoutHorizontal();
                                                 valignCenter();
                                                 height("100%");
                                                 width("5%");
 
 
                                             }
                                         }); // </panel_3.2.3>
 
                                         // <panel_3.2.4>
                                         panel(new PanelBuilder("Panel_ESP2") {
                                             {
                                                 childLayoutHorizontal();
                                                 valignCenter();
                                                 height("75%");
                                                 width("10%");
 
                                                 // add image
                                                 image(new ImageBuilder() {
                                                     {
                                                         valignCenter();
                                                         alignRight();
                                                         height("100%");
                                                         width("100%");
                                                         filename("Pictures/esp.jpg");
                                                         System.out.print("DddESAPAÑA");
                                                         visibleToMouse(true);
 
                                                         interactOnClick("aplicacion.getI()");
 
                                                     }
                                                 });
 
 
                                             }
                                         }); // </panel_3.2.4>
 
                                         // <panel_3.2.5>
                                         panel(new PanelBuilder("Panel_EMPTY3.2.52") {
                                             {
                                                 childLayoutHorizontal();
                                                 valignCenter();
                                                 height("100%");
                                                 width("5%");
 
 
                                             }
                                         }); // </panel_3.2.6>
 
                                     }
                                 }); // </panel_3.2>
                             }
                         });// </panel_3>
                     }
                 });
                 // </layer>
             }
         }.build(nifty));
         // </screen>
  
      game.getGUIViewPort().addProcessor(niftyDisplay);
         nifty.gotoScreen("InputScreen"); // it is used to start the screen
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
         //  game.getInputManager().addListener(new AppActionListener(), "SIMPLEAPP_Exit1");
         game.getViewPort().attachScene(rootNode);
         game.getGUIViewPort().attachScene(guiNode);
         if (b==true)
         {
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
 
     public void startInput() {
 
         //nifty.exit();
        game.loadInput();
         nifty.removeScreen("MenuScreen");
 
     }
 
     public void quitInput() {
         game.stop();
     }
 
     public void loadInput() {
         game.loadInput();
     }
 
    
 }
