 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bs;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioNode;
 import com.jme3.audio.AudioNode.Status;
 import com.jme3.audio.AudioRenderer;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.input.InputManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.system.AppSettings;
 import com.jme3.system.lwjgl.LwjglTimer;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import de.lessvoid.nifty.tools.SizeValue;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import menu.Match;
 import mygame.Main;
 
 /**
  *
  * @author CotA
  */
 
 // All Match States must extend InGameState (which in turn extends AbstractAppState)
 public class StandardMatchState extends InGameState implements ScreenController
 {
 
     private static final Logger logger = Logger.getLogger(StandardMatchState.class.getName());
     private SimpleApplication app;
     private AssetManager assetManager;
     private Node rootNode;
     private Node guiNode;
     private Node localRootNode = new Node("In Game RootNode");
     private Node localGuiNode = new Node("In Game GuiNode");
     private AppSettings settings;
     private ViewPort viewPort;
     private Stage loadStage;
     private ArrayList ledges;
     private NiftyJmeDisplay niftyDisplay;
     private Screen screen;
     private Nifty nifty;
     private BulletAppState bulletAppState;
     private InputManager inputManager;
     private AudioRenderer audioRenderer;
     private ViewPort guiViewPort;
     private Camera cam;
     private Camera flyCam;
     private AudioNode music;
     private LwjglTimer time;
     private PlayerPhysics[] players = new PlayerPhysics[4];
     
     private Match currentMatch;
     
     private int numberOfPlayers = 2;
     
     // Stock Options (no, not the financial kind :P)
     private int initialStock = 10;
     public int currentStock = initialStock;
     
     private boolean isStockMatch = false;
     
     // Time Options
 //    private LwjglTimer gameTimer;
     private String currentTime;
     private int matchDuration = 120;
     
     private boolean isTimeMatch = false;
 
     public StandardMatchState() 
     {
         
     }
     
     public StandardMatchState(Match currentMatch)
     {
         this.currentMatch = currentMatch;
         isStockMatch = currentMatch.isStockMatch();
         initialStock = currentMatch.getStock();
         isTimeMatch = currentMatch.isTimeMatch();
         matchDuration = currentMatch.getTime();
     }
 
 
     @Override
     public void initialize(AppStateManager stateManager, Application app) 
     {
         super.initialize(stateManager, app);
         this.app = (SimpleApplication)app;
         this.rootNode = this.app.getRootNode();
         this.guiNode = this.app.getGuiNode();
         this.assetManager = this.app.getAssetManager();
         this.settings = this.app.getContext().getSettings();
         this.viewPort = this.app.getViewPort();
         this.inputManager = this.app.getInputManager();
         this.audioRenderer = this.app.getAudioRenderer();
         this.guiViewPort = this.app.getViewPort();
         this.cam = this.app.getCamera();
         
         time = new LwjglTimer();  
 //        gameTimer = new LwjglTimer();
         
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
 
         rootNode.attachChild(localRootNode);    //Creates rootNode 
         guiNode.attachChild(localGuiNode);      //Creates guiNode
 
          // Can use same music node across all menus. Not sure if ideal, but it is functional, and seems to work well.
         Main.changeMusic("Sounds/Music/Super Smash Bionicle Main Theme 2.wav"); 
 
         //Initialize enviornment
         createEnviron();
 
         //Attach stage
         localRootNode.attachChild(loadStage.getStageNode());
         players[1].getPlayer().getControl(AIController.class).findTarget(); //TO CHANGE AFTER AI TESTING
         //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 
         //Get Main's nifty
         nifty = Main.getNifty();
         nifty.registerScreenController(this);
         nifty.addXml("Interface/GUIS/StandardMatchHUD.xml");
         nifty.gotoScreen("standardMatchHud");
         
         //Add the InGameHUD xml file, and go this screen
         nifty.setDebugOptionPanelColors(false); // Added this so the true on the Menu won't make the screen look weird.
 
         //Initialize GUI
         guiInitiate();
         playerStockInitialize();
 //        adjustStock(initialStock, 1);
         
         //Rotate the camera to start position
         cam.setLocation(new Vector3f(0, 10, 70));
         cam.setRotation(new Quaternion(0f, -1f, 0f, 0f));
     }
     
     /*Create players algo
      * First, check the length,
      * and with each increment in length
      * create the next player appropriately
      */
     public void createEnviron()         /**STILL NEEDS ADJUSTING **/
     {
         
         //players are all null at start
         
         players[0] = null; 
         players[1] = null;
         players[2] = null;
         players[3] = null;
         
         
         players[0] = new PlayerPhysics(localRootNode,currentMatch.getPlayer1(), bulletAppState, inputManager, cam, this, false);
         for(int i = 1; i < 4; i++)
         {
             switch(i){
                 case 1:
                     if(!currentMatch.getPlayer2().canPlay()){
                         break;
                     } else if(currentMatch.getPlayer2().sameCharacter(currentMatch.getPlayer1())){
                         players[1]= new PlayerPhysics(localRootNode,currentMatch.getPlayer2(), bulletAppState, inputManager, cam, this, true);
                     } else {
                         players[1]= new PlayerPhysics(localRootNode,currentMatch.getPlayer2(), bulletAppState, inputManager, cam, this, false);
                     }
                     break;
                 case 2:
                     if(!currentMatch.getPlayer3().canPlay()){
                         break;
                     }else if(currentMatch.getPlayer3().sameCharacter(currentMatch.getPlayer2()) || currentMatch.getPlayer3().sameCharacter(currentMatch.getPlayer1())){
                        // players[2]= new PlayerPhysics(currentMatch.getPlayer3(), bulletAppState, inputManager, cam,true);
                     } else {
                        // players[2]= new PlayerPhysics(currentMatch.getPlayer3(), bulletAppState, inputManager, cam,false);
                     }
                     break;
                 case 3:
                     if(!currentMatch.getPlayer4().canPlay()){
                         break;
                     }else if(currentMatch.getPlayer4().sameCharacter(currentMatch.getPlayer3()) || currentMatch.getPlayer4().sameCharacter(currentMatch.getPlayer2()) || currentMatch.getPlayer4().sameCharacter(currentMatch.getPlayer1())){
                       //  players[3]= new PlayerPhysics(currentMatch.getPlayer3(), bulletAppState, inputManager, cam,true);
                     } else {
                       //  players[3]= new PlayerPhysics(currentMatch.getPlayer3(), bulletAppState, inputManager, cam,false);
                     }
                     break;
                     
             }
         }
         
         loadStage = new Stage(assetManager.loadModel("Scenes/StageScenes/ShowdownScene.j3o"), bulletAppState);  
         ledges = loadStage.getLedges();
         loadStage.setInGameState(this);
         
         for(int i = 0; i < 4; i++){
             switch (i){
                 case 0:
                     if(players[0] != null){
                         loadStage.getp1Spawn().attachChild(players[i].getPlayer());
                         players[i].getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp1Spawn()).getWorldTranslation()));
                     }
                     break;
                 case 1:
                     if(players[1] != null){
                         loadStage.getp2Spawn().attachChild(players[i].getPlayer());
                         players[i].getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp2Spawn()).getWorldTranslation()));
                     }
                     break;
                 case 2:
                     if(players[2] != null){
                         loadStage.getp3Spawn().attachChild(players[i].getPlayer());
                         players[i].getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp1Spawn()).getWorldTranslation()));
                     }
                     break;
                 case 3:
                     if(players[3] != null){
                         loadStage.getp4Spawn().attachChild(players[i].getPlayer());
                         players[i].getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp1Spawn()).getWorldTranslation()));
                     }
                     break;
             }
             
         }
             
         //loadStage.getp2Spawn().attachChild(two.getPlayer());
         //loadStage.getp3Spawn().attachChild(three.getPlayer());
 
         
         //two.getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp2Spawn()).getWorldTranslation()));
         //three.getCharacterControl().setPhysicsLocation((((Spatial) loadStage.getp3Spawn()).getWorldTranslation()));   
     }
     
     @Override
     public void update(float tpf) 
     {
         for(int i =0; i < ledges.size(); i++){
             if(((Spatial)ledges.get(i)).getControl(GhostControl.class).getOverlappingCount() == 0 && (Boolean)((Spatial)ledges.get(i)).getUserData("ledgeGrabbed")){
                 ((Spatial)ledges.get(i)).setUserData("ledgeGrabbed",false);
                 players[(Integer)((Spatial)ledges.get(i)).getUserData("playerGrabbing")-1].getPlayer().getControl(PlayerControl.class).resetGravity();
                 
             }
         }
         
         //JME says looping streamed music isn't possible. So instead
         //look for when the music is stopped, create a new
         //instance of that music, and play it again.
 //        if(music.getStatus() == Status.Stopped)
 //        {
 //            music = new AudioNode(assetManager, "Sounds/Music/Super Smash Bionicle Main Theme 2.wav", true);
 //            music.play();
 //        }
         
         time.update();
         currentTime = calculateTime(time.getTimeInSeconds());
         screen.findElementByName("CurrentTime").getRenderer(TextRenderer.class).setText("" + currentTime + "");
         
     }
 
     @Override
     public void cleanup() 
     {
         rootNode.detachChild(localRootNode);
         guiNode.detachChild(localRootNode);
 
     }
 
     @Override
     public void setEnabled(boolean enabled) 
     {
         super.setEnabled(enabled);
 
         if (enabled) {
             System.out.println("enable");
         } else {
             System.out.println("disabled");
         }
 
     }
 
     public void bind(Nifty nifty, Screen screen) 
     {
         this.nifty = nifty;
         this.screen = screen;
     }
 
     public void onStartScreen() 
     {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onEndScreen() 
     {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     
     public void guiInitiate()
     {
         setStatusPanelLocations();
         setStockImageHeight();
         setPlayerAvatars();
         setPlayerEmblems();
     }
     
     public void setStockImageHeight()
     {
         String width = "" + screen.findElementByName("Player1ProgressItem1").getWidth() + "px";
         String image = "";
         
         for (int i = 1; i <= 4; i++) 
         {
             for (int j = 1; j <= 5; j++) 
             {
                 image = "Player" + i + "StockIcon" + j;
                 screen.findElementByName(image).setConstraintHeight(new SizeValue(width));
                 screen.findElementByName(image).getParent().layoutElements();
                 
                 if (players[i - 1] != null)
                 {
                     NiftyImage icon = nifty.getRenderEngine().createImage("Interface/CharacterInGame/StockIcon/" + players[i - 1].getMenuPlayer().costume + ".png", false);
                     screen.findElementByName(image).getRenderer(ImageRenderer.class).setImage(icon);
                 }
                 
             }  
         }
         
     }
     
     public void setPlayerAvatars()
     {
         String image = "";
         
         for (int i = 1; i <= 4; i++) 
         {
             image = "Player" + i + "Avatar";
             
             if (players[i - 1] != null)
             {
                 NiftyImage emblem = nifty.getRenderEngine().createImage("Interface/CharacterInGame/Avatar/" + players[i - 1].getMenuPlayer().costume + ".png", false);
                 screen.findElementByName(image).getRenderer(ImageRenderer.class).setImage(emblem);
             }
             
         }
     }
     
     public void setPlayerEmblems()
     {
         String image = "";
         
         for (int i = 1; i <= 4; i++) 
         {
             image = "Player" + i + "Emblem";
             
             if (players[i - 1] != null)
             {
                 NiftyImage emblem = nifty.getRenderEngine().createImage("Interface/CharacterInGame/Emblem/" + players[i - 1].getMenuPlayer().costume + ".png", false);
                 screen.findElementByName(image).getRenderer(ImageRenderer.class).setImage(emblem);
             }
             
         }
     }
     
     public void setStatusPanelLocations()
     {
         if (numberOfPlayers == 2)
         {
             screen.findElementByName("Player1StatusConstantPanel").setConstraintX(new SizeValue("24.0583%"));
             screen.findElementByName("Player1StatusConstantPanel").getParent().layoutElements();
             screen.findElementByName("Player1StatusUnderlayPanel").setConstraintX(new SizeValue("24.0583%"));
             screen.findElementByName("Player1StatusUnderlayPanel").getParent().layoutElements();
             screen.findElementByName("Player1StatusOverlayPanel").setConstraintX(new SizeValue("24.0583%"));
             screen.findElementByName("Player1StatusOverlayPanel").getParent().layoutElements();
             
             screen.findElementByName("Player2StatusConstantPanel").setConstraintX(new SizeValue("57.2916%"));
             screen.findElementByName("Player2StatusConstantPanel").getParent().layoutElements();
             screen.findElementByName("Player2StatusUnderlayPanel").setConstraintX(new SizeValue("57.2916%"));
             screen.findElementByName("Player2StatusUnderlayPanel").getParent().layoutElements();
             screen.findElementByName("Player2StatusOverlayPanel").setConstraintX(new SizeValue("57.2916%"));
             screen.findElementByName("Player2StatusOverlayPanel").getParent().layoutElements();
         }
         else if (numberOfPlayers == 3)
         {
             screen.findElementByName("Player1StatusConstantPanel").setConstraintX(new SizeValue("15.625%"));
             screen.findElementByName("Player1StatusConstantPanel").getParent().layoutElements();
             screen.findElementByName("Player1StatusUnderlayPanel").setConstraintX(new SizeValue("15.625%"));
             screen.findElementByName("Player1StatusUnderlayPanel").getParent().layoutElements();
             screen.findElementByName("Player1StatusOverlayPanel").setConstraintX(new SizeValue("15.625%"));
             screen.findElementByName("Player1StatusOverlayPanel").getParent().layoutElements();
             
             screen.findElementByName("Player2StatusConstantPanel").setConstraintX(new SizeValue("40.625%"));
             screen.findElementByName("Player2StatusConstantPanel").getParent().layoutElements();
             screen.findElementByName("Player2StatusUnderlayPanel").setConstraintX(new SizeValue("40.625%"));
             screen.findElementByName("Player2StatusUnderlayPanel").getParent().layoutElements();
             screen.findElementByName("Player2StatusOverlayPanel").setConstraintX(new SizeValue("40.625%"));
             screen.findElementByName("Player2StatusOverlayPanel").getParent().layoutElements();
             
             screen.findElementByName("Player3StatusConstantPanel").setConstraintX(new SizeValue("65.625%"));
             screen.findElementByName("Player3StatusConstantPanel").getParent().layoutElements();
             screen.findElementByName("Player3StatusUnderlayPanel").setConstraintX(new SizeValue("65.625%"));
             screen.findElementByName("Player3StatusUnderlayPanel").getParent().layoutElements();
             screen.findElementByName("Player3StatusOverlayPanel").setConstraintX(new SizeValue("65.625%"));
             screen.findElementByName("Player3StatusOverlayPanel").getParent().layoutElements();
         }
     }
     
     public void playerStockInitialize()
     {
         for (int j = 0; j < 4; j++) 
             {
                 if (players[j] != null)
                 {
                     players[j].setStock(initialStock);
                     adjustStock(initialStock, "Player" + (j + 1));
                 }
                 else
                 {
                     screen.findElementByName("Player" + (j + 1) + "StatusConstantPanel").hide();
                     screen.findElementByName("Player" + (j + 1) + "StatusUnderlayPanel").hide();
                     screen.findElementByName("Player" + (j + 1) + "StatusOverlayPanel").hide();
                 }
             }
     }
     
     public void adjustStock(int newStock, String playerNumber) // Perhaps should check by character instead, and use the respawn method in PlayerControl
     {
         // Adjust alignment for large numbers
         
         // Add stuff for when below 5 (5 can just be left alone)
         
         currentStock = newStock;
         int stockAmount = newStock;
         
         if (stockAmount > 5)
         {
             screen.findElementByName(playerNumber + "StockIcon1").hide();
             screen.findElementByName(playerNumber + "StockIcon3").hide();
             screen.findElementByName(playerNumber + "StockIcon4").hide();
             screen.findElementByName(playerNumber + "StockIcon5").hide();
             screen.findElementByName(playerNumber + "StockLeftNumber").getRenderer(TextRenderer.class).setText("" + stockAmount + "");
             
         }
         
         if (stockAmount == 5)
         {
             screen.findElementByName(playerNumber + "StockIcon1").show();
             screen.findElementByName(playerNumber + "StockIcon3").show();
             screen.findElementByName(playerNumber + "StockIcon4").show();
             screen.findElementByName(playerNumber + "StockIcon5").show();
             
         }
         
         if (stockAmount <= 4)
         {
             screen.findElementByName(playerNumber + "StockIcon5").hide();
 //          
         }
         
         if (stockAmount <= 3)
         {
             screen.findElementByName(playerNumber + "StockIcon4").hide();
             screen.findElementByName(playerNumber + "StockLeftNumber").hide();
         }
         
         if (stockAmount <= 2)
         {
             screen.findElementByName(playerNumber + "StockIcon3").hide();
             screen.findElementByName(playerNumber + "StockLeftX").hide();
         }
         
         if (stockAmount <= 1)
         {
             screen.findElementByName(playerNumber + "StockIcon2").hide();
         }
         
         if (stockAmount <= 0)
         {
             screen.findElementByName(playerNumber + "StockIcon1").hide();
             screen.findElementByName(playerNumber + "StatusUnderlayPanel").hide();
             screen.findElementByName(playerNumber + "StatusOverlayPanel").hide();
             screen.findElementByName(playerNumber + "AvatarPanel").hide();
             screen.findElementByName(playerNumber + "NamePanel").hide();
         }
     }
 
     private String calculateTime(float time) 
     {
         int totalSecondsRemaining = matchDuration - (int) time; 
         
         if (totalSecondsRemaining <= 0)
         {
             return "";
         }
         
         float millisecondsPassed = time - (int) time;
         
         int hoursRemaining = totalSecondsRemaining / 3600;
         int minutesRemaining = (totalSecondsRemaining / 60) - (hoursRemaining * 60);
         int secondsRemaining = totalSecondsRemaining - (minutesRemaining * 60) - (hoursRemaining * 3600);
         int millisecondsRemaining = 100 - (int) (millisecondsPassed * 100);
         
         
         String hours = "";
         String minutes = "";
         String seconds = "";
         String milliseconds = "";
         
         if (hoursRemaining > 0)
         {
             hours = "" + hoursRemaining + ":";
         }
         if (minutesRemaining < 10)
         {
             minutes = "0" + minutesRemaining + ":";
         }
         else
         {
             minutes = "" + minutesRemaining + ":";
         }
         if (secondsRemaining < 10)
         {
             seconds = "0" + secondsRemaining + ":";
         }
         else
         {
             seconds = "" + secondsRemaining + ":";
         }
         if (millisecondsRemaining < 10)
         {
             milliseconds = "0" + millisecondsRemaining;
         }
         else
         {
            if (millisecondsRemaining == 100)
            {
                milliseconds = "00";
            }
            else
            {
                milliseconds = "" + millisecondsRemaining + "";
            } 
         }
         
         return hours + minutes + seconds + milliseconds;
         
 //        int totalTimeInSeconds = (int) time;
 //        int totalTimeInMinutes = (int) (totalTimeInSeconds / 60);
 //        int totalTimeInHours = (int) (totalTimeInMinutes / 60);
 //        
 //        int timeInHours = totalTimeInHours;
 //        int timeInMinutes = totalTimeInMinutes - (totalTimeInHours * 60);
 //        int timeInSeconds = totalTimeInSeconds - (totalTimeInMinutes * 60);
 //        
 //        String hours = "";
 //        String minutes = "";
 //        String seconds = "";
 //        
 //        if (timeInHours > 0)
 //        {
 //            hours = "" + timeInHours + ":";
 //        }
 //        if (timeInMinutes < 10)
 //        {
 //            minutes = "0" + timeInMinutes + ":";
 //        }
 //        else
 //        {
 //            minutes = "" + timeInMinutes + ":";
 //        }
 //        if (timeInSeconds < 10)
 //        {
 //            minutes = "0" + timeInSeconds;
 //        }
 //        else
 //        {
 //            seconds = "" + timeInSeconds + "";
 //        }
 //        return hours + minutes + seconds;
     }
     
     
 }
