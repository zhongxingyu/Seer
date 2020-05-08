 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
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
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioButtonBuilder;
 import de.lessvoid.nifty.controls.radiobutton.builder.RadioGroupBuilder;
 import de.lessvoid.nifty.controls.textfield.builder.TextFieldBuilder;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import game.MainApp;
 
 public class LogInState extends AbstractAppState implements ScreenController {
 
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
 
     public LogInState(MainApp game) {
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
 
 
 
         nifty.addScreen("LogInScreen", new ScreenBuilder("LogInScreen") {
             {
                 //controller(new GUI.PowdersScreenController()); // This connects the Java class StartingScreen and the GUI screen.     
                 controller(new MenuState(game));
 
 
                 layer(new LayerBuilder("LayerLogIn") {
                     {
                         childLayoutVertical(); // layer properties, add more...
 
                         // <panel_1>
                         panel(new PanelBuilder("Panel_Title_Powders") {
                             {
                                 childLayoutCenter();
                                 alignCenter();
                                 height("35%");
                                 width("100%");
                                 image(new ImageBuilder() {
                                     {
                                         this.filename("/Pictures/Titulo.png");
                                         valignCenter();
                                         alignCenter();
                                         height("70%");
                                         width("60%");
                                     }
                                 });
 
 
                             }
                         }); // </panel_1>
 
                         panel(new PanelBuilder("Panel_Title_LogIn") {
                             {
                                 childLayoutCenter();
                                 alignCenter();
                                 height("10%");
                                 width("50%");
 
                                 // add text
                                 text(new TextBuilder() {
                                     {
                                         text("LOG IN");
                                         font("Interface/Fonts/Default.fnt");
                                         height("100%");
                                         width("100%");
                                     }
                                 });
                             }
                         }); // </panel_1>
 
                         // <panel_2>
                         panel(new PanelBuilder("Panel_RegisterProcess") {
                             {
                                 childLayoutVertical(); // panel properties, add more...               
                                 alignCenter();
                                 valignCenter();
                                 height("45%");
                                 width("60%");
 
                                 panel(new PanelBuilder("Panel_Text") {
                                     {
                                         childLayoutVertical();
                                         height("30%");
                                         width("50%");
                                         alignCenter();
                                         valignCenter();
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 font("Interface/Fonts/Default.fnt");
                                                 text("Insert your nick and password if you are alredy registered.");
                                                 height("20%");
                                                 width("80%");
                                             }
                                         });
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 text("If not, press the \"REGISTER\" button to start the registration process.");
                                                 font("Interface/Fonts/Default.fnt");
                                                 height("20%");
                                                 width("80%");
                                             }
                                         });
                                     }
                                 }); // </panel_2>
 
 
                                 panel(new PanelBuilder("Panel_InsertData1") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         height("7%");
                                         width("50%");
                                         alignCenter();
                                         valignCenter();
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 text("NICK: ");
                                                 font("Interface/Fonts/Default.fnt");
                                                 height("70%");
                                                 width("30%");
                                             }
                                         });
 
                                         control(new TextFieldBuilder("NickLogIn") {
                                             {
                                                 width("50%");
                                                 height("100%");
                                             }
                                         });
                                     }
                                 });
 
                                 panel(new PanelBuilder("Panel_Empty") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("3%");
                                         width("50%");
 
                                     }
                                 });
 
 
                                 panel(new PanelBuilder("Panel_InsertData12") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("7%");
                                         width("50%");
 
                                         text(new TextBuilder() {
                                             {
                                                 alignCenter();
                                                 valignCenter();
                                                 text("PASS: ");
                                                 font("Interface/Fonts/Default.fnt");
                                                 height("70%");
                                                 width("30%");
                                             }
                                         });
 
                                         control(new TextFieldBuilder("PassLogIn") {
                                             {
                                                 width("50%");
                                                 height("100%");
                                             }
                                         });
                                     }
                                 }); // </panel_2>
 
 
                                 panel(new PanelBuilder("Panel_ERRORES") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         alignCenter();
                                         valignBottom();
                                         height("16%");
                                         width("50%");
 
                                         /*
                                          text(new TextBuilder("labelErrores") {
                                          {
                                          alignCenter();
                                          valignCenter();
                                          text("- ");
                                          font("Interface/Fonts/Default.fnt");
                                          height("90%");
                                          width("30%");
                                          }
                                          });*/
 
                                     }
                                 }); // </panel_2>
 
                                 panel(new PanelBuilder("Panel_Buttons") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("12%");
                                         width("100%");
 
                                         control(new ButtonBuilder("Button_REGISTER", "REGISTER") {
                                             {
                                                 alignCenter();
                                                 valignBottom();
                                                 backgroundColor("#f108");
                                                 height("75%");
                                                 width("30%");
                                                 visibleToMouse(true);
                                                 interactOnClick("loadInputFromLogIn()");
                                                 interactOnRelease("initializaOnlineBoolean()");
                                             }
                                         });
 
                                         panel(new PanelBuilder("Panel_Space") {
                                             {
                                                 childLayoutHorizontal(); // panel properties, add more...               
                                                 alignCenter();
                                                 valignCenter();
                                                 height("2%");
                                                 width("6%");
 
                                             }
                                         });
 
                                         control(new ButtonBuilder("Button_LogIn", "LOG IN") {
                                             {
                                                 alignCenter();
                                                 valignBottom();
                                                 backgroundColor("#f108");
                                                 height("75%");
                                                 width("30%");
                                                 visibleToMouse(true);
                                                 interactOnClick("cargarUsuario()");
                                                 interactOnRelease("initializaOnlineBoolean()");
                                             }
                                         });
 
                                         panel(new PanelBuilder("Panel_Space") {
                                             {
                                                 childLayoutHorizontal(); // panel properties, add more...               
                                                 alignCenter();
                                                 valignCenter();
                                                 height("2%");
                                                 width("6%");
 
                                             }
                                         });
 
                                         control(new ButtonBuilder("Button_QUIT", "EXIT") {
                                             {
                                                 alignCenter();
                                                 valignBottom();
                                                 backgroundColor("#f108");
                                                 height("75%");
                                                 width("30%");
                                                 visibleToMouse(true);
                                                 interactOnClick("exit()");
                                             }
                                         });
 
                                     }
                                 });
 
                                 panel(new PanelBuilder("Panel_Space") {
                                     {
                                         childLayoutCenter(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("7%");
                                         width("30%");
                                     }
                                 });
                                control(new RadioGroupBuilder("RadioGroup"));
                                 panel(new PanelBuilder("Panel_Space") {
                                     {
                                         childLayoutHorizontal(); // panel properties, add more...               
                                         alignCenter();
                                         valignCenter();
                                         height("18%");
                                         width("18%");
 

                                         control(new LabelBuilder("LocalL", "Local"));
                                         control(new RadioButtonBuilder("LocalR") {
                                             {
 
                                                 group("RadioGroup"); // the id of the RadioButtonGroup to link this RadioButton to
                                                interactOnRelease("setOffline()");
                                            }
                                        });
                                        control(new LabelBuilder("OnlineL", "Online"));
                                        control(new RadioButtonBuilder("OnlineR") {
                                            {

                                                group("RadioGroup"); // the id of the RadioButtonGroup to link this RadioButton to
                                                interactOnRelease("setOnline()");
                                             }
                                         });
 
                                         panel(new PanelBuilder("Panel_Space") {
                                             {
 
                                                 childLayoutHorizontal(); // panel properties, add more...               
                                                 alignCenter();
                                                 valignCenter();
                                                 height("2%");
                                                 width("30%");
 
                                             }
                                         });
 
 
                                     }
                                 });
 
                             }
                         }); // </panel_2>
 
                         // <panel_3>
                         panel(new PanelBuilder("Panel_Botones") {
                             {
                                 childLayoutVertical();
                                 alignCenter();
                                 valignBottom();
                                 height("5%");
                                 width("100%");
                                 // GUI element
 
                                 text(new TextBuilder() {
                                     {
                                         this.style("icon-system");
                                         text("Developers: Imanol Barriuso, Jon Ander Novella, Jesus Pereira and Jokin Sainz.");
                                         font("Interface/Fonts/Default.fnt");
                                         height("47%");
                                         width("100%");
                                     }
                                 });
                                 panel(new PanelBuilder("Panel_Botones") {
                                     {
                                         childLayoutHorizontal();
                                         alignCenter();
                                         valignBottom();
                                         height("47%");
                                         width("16%");
 
                                         /*text(new TextBuilder() {
                                          {
                                          text("© Copyright. All rights reserved");
                                          font("Interface/Fonts/Default.fnt");
                                          height("100%");
                                          width("100%");
                                          }
                                          });*/
                                     }
                                 });
                                 panel(new PanelBuilder("Panel_Botones") {
                                     {
                                         childLayoutVertical();
                                         alignCenter();
                                         valignBottom();
                                         height("6%");
                                         width("100%");
                                     }
                                 });
 
 
                             }
                         });// </panel_3>
                     }
                 });
                 // </layer>
 
                 layer(new LayerBuilder("Layer2LogIn") {
                     {
                         childLayoutCenter();
                         alignCenter();
                         valignCenter();
 
                         /*control(new WindowBuilder("NoticeWindow", "WELCOME TO POWDERS.") {
                          {
                          width("320px"); // windows will need a size
                          height("18px");
                          alignCenter();
                          valignBottom();
                          closeable(false);
                          backgroundColor("#000088ff");
 
                          /*text(new TextBuilder() {
                          {
                          text("The nick you entered is already in use by another user.");
                          style("base-font");
                          color("#fffa");
                          valignCenter();
                          width("100%");
                          }
                          });
                          }
                          });*/
                     }
                 });
             }
         }.build(nifty));
         // </screen>
 
         new PopupBuilder("popupUserError") {
             {
                 childLayoutCenter();
                 backgroundColor("#000a");
 
 
                 panel(new PanelBuilder("PanelPopup") {
                     {
                         childLayoutVertical();
                         alignCenter();
                         backgroundImage("/Pictures/FondoDialog.png");
                         valignCenter();
                         height("20%");
                         width("40%");
 
                         text(new TextBuilder() {
                             {
                                 alignCenter();
                                 color("f043");
                                 text("Wrong nick or password.");
                                 font("Interface/Fonts/Default.fnt");
                                 height("50%");
                                 width("27%");
                             }
                         });
 
                         control(new ButtonBuilder("Btn1", "OK") {
                             {
                                 alignCenter();
                                 valignCenter();
                                 height("30%");
                                 width("40%");
                                 interactOnClick("closePopupUserError()");
                             }
                         });
 
                     }
                 });
 
             }
         }.registerPopup(nifty);
 
         game.getGUIViewPort().addProcessor(niftyDisplay);
         nifty.gotoScreen("LogInScreen"); // it is used to start the screen
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
 
     public void startInput() {
         //nifty.exit();
         nifty.removeScreen("MenuScreen");
         game.loadInput();
 
     }
 
     public void quitInput() {
         game.stop();
     }
 
     public void loadInput() {
         game.loadInput();
     }
 }
