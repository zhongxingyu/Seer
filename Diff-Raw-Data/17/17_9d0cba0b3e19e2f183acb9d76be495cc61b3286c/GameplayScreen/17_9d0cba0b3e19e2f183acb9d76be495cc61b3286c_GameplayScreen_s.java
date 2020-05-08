 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import eel.seprphase2.Simulator.GameOverException;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.PlantStatus;
 import lamprey.seprphase3.GUI.BackyardReactor;
 import lamprey.seprphase3.GUI.GameplayListeners;
 import lamprey.seprphase3.GUI.Images.CondenserWaterLevelImage;
 import lamprey.seprphase3.GUI.Images.ControlRodsImage;
 import lamprey.seprphase3.GUI.Images.CurrentlyRepairing;
 import lamprey.seprphase3.GUI.Images.ErrorMessagesImage;
 import lamprey.seprphase3.GUI.Images.HoverButton;
 import lamprey.seprphase3.GUI.Images.HoverButtonType;
 import lamprey.seprphase3.GUI.Images.InformationPanels;
 import lamprey.seprphase3.GUI.Images.MechanicImage;
 import lamprey.seprphase3.GUI.Images.PiggyBank;
 import lamprey.seprphase3.GUI.Images.PumpImage;
 import lamprey.seprphase3.GUI.Images.ReactorWaterLevelImage;
 import lamprey.seprphase3.GUI.Images.TurbineImage;
 import lamprey.seprphase3.GUI.Images.ValveImage;
 
 /**
  *
  * @author Simeon
  */
 public class GameplayScreen extends AbstractScreen {
     private GameplayListeners listeners;
     
     private Texture gamebgTexture;
     private Texture borderTexture;
     private Texture condenserTexture;
     private Texture coolerTexture;
     private Texture pipesTexture;
     private Texture poweroutTexture;
     private Texture reactorbackTexture;
     private Texture pauseTexture;
     private Texture consolebackTexture;
     private Texture crUpTexture;
     private Texture crDownTexture;
     private Texture pump1Texture;
     private Texture pump2Texture;
     private Texture valve1Texture;
     private Texture valve2Texture;
     private Texture piggyTexture;
     
     private Image gamebgImage;
     private Image borderImage;
     private HoverButton condenserImage;
     private CondenserWaterLevelImage condenserWaterLevel;
     private Image coolerImage;
     private Image pipesImage;
     private ValveImage valve1Image;
     private ValveImage valve2Image;
     private PumpImage pump1Image;
     private PumpImage pump2Image;
     private Image poweroutImage;
     private Image reactorImage;
     private ControlRodsImage controlRods;
     private ReactorWaterLevelImage reactorWaterLevel;
     private TurbineImage turbineImage;
     private MechanicImage mechanicImage;
     private InformationPanels infoPanels;
     private HoverButton pauseImage;
     private Image consolebackImage;
     private HoverButton crUpImage;
     private HoverButton crDownImage;
     private HoverButton pump1Button;
     private HoverButton pump2Button;
     private HoverButton valve1Button;
     private HoverButton valve2Button;
     private PiggyBank piggyImage;
     private ErrorMessagesImage errorMessages;
     
     private float deltaSum;
     
     public GameplayScreen(BackyardReactor game, PlantController controller, PlantStatus status, GameManager manager) {
         super(game, controller, status, manager);
         listeners = new GameplayListeners(this, controller, status, manager);
         
         gamebgTexture      = new Texture(Gdx.files.internal("assets\\game\\bg.png"));
         borderTexture      = new Texture(Gdx.files.internal("assets\\game\\border.png"));
         condenserTexture   = new Texture(Gdx.files.internal("assets\\game\\condenser.png"));
         coolerTexture      = new Texture(Gdx.files.internal("assets\\game\\cooler.png"));
         pipesTexture       = new Texture(Gdx.files.internal("assets\\game\\pipes.png"));
         poweroutTexture    = new Texture(Gdx.files.internal("assets\\game\\powerout.png"));
         reactorbackTexture = new Texture(Gdx.files.internal("assets\\game\\reactor_back.png"));
         pauseTexture       = new Texture(Gdx.files.internal("assets\\game\\pause.png"));
         consolebackTexture = new Texture(Gdx.files.internal("assets\\game\\consoleback.png"));
         crUpTexture        = new Texture(Gdx.files.internal("assets\\game\\controlrodup.png"));
         crDownTexture      = new Texture(Gdx.files.internal("assets\\game\\controlroddown.png"));
         pump1Texture       = new Texture(Gdx.files.internal("assets\\game\\pump1.png"));
         pump2Texture       = new Texture(Gdx.files.internal("assets\\game\\pump2.png"));
         valve1Texture      = new Texture(Gdx.files.internal("assets\\game\\valve1.png"));
         valve2Texture      = new Texture(Gdx.files.internal("assets\\game\\valve2.png"));
         piggyTexture       = new Texture(Gdx.files.internal("assets\\game\\piggybank.png"));
         
         gamebgImage         = new Image(gamebgTexture);
         borderImage         = new Image(borderTexture);
         condenserImage      = new HoverButton(condenserTexture, HoverButtonType.Component);
         condenserWaterLevel = new CondenserWaterLevelImage(this.getPlantStatus());
         coolerImage         = new HoverButton(coolerTexture, HoverButtonType.Component);
         pipesImage          = new Image(pipesTexture);
         valve1Image         = new ValveImage(this.getPlantStatus(), 1);
         valve2Image         = new ValveImage(this.getPlantStatus(), 2);
         pump1Image          = new PumpImage(this.getPlantStatus(), 1);
         pump2Image          = new PumpImage(this.getPlantStatus(), 2);
         poweroutImage       = new Image(poweroutTexture);
         reactorImage        = new Image(reactorbackTexture);
         controlRods         = new ControlRodsImage(this.getPlantStatus());
         reactorWaterLevel   = new ReactorWaterLevelImage(this.getPlantStatus());
         turbineImage        = new TurbineImage(this.getPlantStatus());
         infoPanels          = new InformationPanels(this.getPlantStatus());
         mechanicImage       = new MechanicImage(this.getPlantController());
         pauseImage          = new HoverButton(pauseTexture,  HoverButtonType.Transparent);
         consolebackImage    = new Image(consolebackTexture);
         crUpImage           = new HoverButton(crUpTexture,   HoverButtonType.NotTransparent);
         crDownImage         = new HoverButton(crDownTexture, HoverButtonType.NotTransparent);
         pump1Button         = new HoverButton(pump1Texture,  HoverButtonType.NotTransparent);
         pump2Button         = new HoverButton(pump2Texture,  HoverButtonType.NotTransparent);
         valve1Button        = new HoverButton(valve1Texture, HoverButtonType.NotTransparent);
         valve2Button        = new HoverButton(valve2Texture, HoverButtonType.NotTransparent);
         piggyImage          = new PiggyBank(piggyTexture, this.getPlantStatus());
         errorMessages       = new ErrorMessagesImage(this.getPlantStatus(), this);
         
         gamebgImage.setPosition(0, 0);
         borderImage.setPosition(0, 0);
         condenserImage.setPosition(523, 110);
         coolerImage.setPosition(803, 122);
         pipesImage.setPosition(132, 149);
         poweroutImage.setPosition(703, 405);
         reactorImage.setPosition(33, 113);
         turbineImage.setPosition(436, 404);
         valve1Image.setPosition(381, 469);
         valve2Image.setPosition(418, 385);
         pump1Image.setPosition(386, 174);
         pump2Image.setPosition(762, 243);
         infoPanels.setPosition(252, 193);
         mechanicImage.setPosition(630, 75);
         mechanicImage.moveMechanicTo(630f); //ensures the mechanic is initially not moving
         pauseImage.setPosition(17, 15);
         consolebackImage.setPosition(260, 0);
         crUpImage.setPosition(545, 75);
         crDownImage.setPosition(560, 21);
         pump1Button.setPosition(323, 71);
         pump2Button.setPosition(373, 76);
         valve1Button.setPosition(300, 22);
         valve2Button.setPosition(353, 22);
         piggyImage.setPosition(821, 10);
 //        errorMessages.setPosition(433, 18);
         
         condenserImage.addListener(listeners.getCondenserListener());
         turbineImage.addListener(listeners.getTurbineListener());
         pump1Image.addListener(listeners.getPump1Listener());
         pump2Image.addListener(listeners.getPump2Listener());
         pauseImage.addListener(listeners.getPauseListener());
 
         addButtonsListeners();
     }
     
     @Override
     public void show() {
         super.show();
 
         //Order of these matters - Actors (Images) that are added to the stage
         //later are going to be on top of ones added before that.
         stage.addActor(gamebgImage);
         stage.addActor(borderImage);
         stage.addActor(poweroutImage);
         stage.addActor(pipesImage);
         stage.addActor(infoPanels);
         stage.addActor(valve1Image);
         stage.addActor(valve2Image);
         stage.addActor(pump1Image);
         stage.addActor(pump2Image);
         stage.addActor(coolerImage);
         stage.addActor(reactorImage);
         stage.addActor(controlRods);
         stage.addActor(reactorWaterLevel);
         stage.addActor(turbineImage);
         stage.addActor(condenserImage);
         stage.addActor(condenserWaterLevel);
         stage.addActor(mechanicImage);
         stage.addActor(pauseImage);
         stage.addActor(consolebackImage);
         stage.addActor(crUpImage);
         stage.addActor(crDownImage);
         stage.addActor(pump1Button);
         stage.addActor(pump2Button);
         stage.addActor(valve1Button);
         stage.addActor(valve2Button);
         stage.addActor(piggyImage);
         stage.addActor(errorMessages);
         
         deltaSum = 0f;
     }
     
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
     }
     
     @Override
     public void render(float delta) {
         float delay = 0.2f;
         deltaSum += delta;
         try {
             if (deltaSum > delay) {
                 controller.step(delta);
                 deltaSum -= delay;
             }
         }
         catch(GameOverException e) {
             game.setScreen(game.getGameOverScreen());
         }
         super.render(delta);
     }
     
     @Override
     public void hide() {
         stage.clear();
     }
     
     public void moveMechanicTo(float destination) {
         mechanicImage.moveMechanicTo(destination);
     }
     
     public void setMechanicRepairing(CurrentlyRepairing repairing) {
         mechanicImage.setRepairing(repairing);
     }
     
     public BackyardReactor getGame() {
         return this.game;
     }
     
     public void addButtonsListeners() {
         crUpImage.addListener(listeners.getConrolRodsUpListener());
         crDownImage.addListener(listeners.getConrolRodsDownListener());
         valve1Button.addListener(listeners.getValve1Listener());
         valve2Button.addListener(listeners.getValve2Listener());
         pump1Button.addListener(listeners.getPump1ButtonListener());
         pump2Button.addListener(listeners.getPump2ButtonListener());
     }
     
     public void removeButtonsListeners() {
//        crUpImage.removeListener(crUpImage.getListeners().first());
//        crDownImage.removeListener(crDownImage.getListeners().first());
//        valve1Button.removeListener(valve1Button.getListeners().first());
//        valve2Button.removeListener(valve2Button.getListeners().first());
//        pump1Button.removeListener(pump1Button.getListeners().first());
//        pump2Button.removeListener(pump2Button.getListeners().first());
     }
 }
