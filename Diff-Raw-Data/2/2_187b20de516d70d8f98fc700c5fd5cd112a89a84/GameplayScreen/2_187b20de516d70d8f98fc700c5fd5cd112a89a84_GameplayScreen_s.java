 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lamprey.seprphase3.GUI.Screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import eel.seprphase2.Simulator.GameManager;
 import eel.seprphase2.Simulator.PlantController;
 import eel.seprphase2.Simulator.PlantStatus;
 import lamprey.seprphase3.GUI.BackyardReactor;
 import lamprey.seprphase3.GUI.GameplayListeners;
 import lamprey.seprphase3.GUI.Images.HoverButton;
 import lamprey.seprphase3.GUI.Images.MechanicImage;
 import lamprey.seprphase3.GUI.Images.TurbineImage;
 
 /**
  *
  * @author Simeon
  */
 public class GameplayScreen extends AbstractScreen {
     GameplayListeners listeners;
     
     Texture gamebgTexture;
     Texture borderTexture;
     Texture condenserTexture;
     Texture coolerTexture;
     Texture pipesTexture;
     Texture poweroutTexture;
     Texture reactorbackTexture;
     Texture pauseTexture;
     Texture consolebackTexture;
     Texture crUpTexture;
     Texture crDownTexture;
     Texture pump1Texture;
     Texture pump2Texture;
     Texture valve1Texture;
     Texture valve2Texture;
     Texture sErrorTexture;
     Image gamebgImage;
     Image borderImage;
     HoverButton condenserImage;
     Image coolerImage;
     Image pipesImage;
     Image poweroutImage;
     HoverButton reactorImage;
     TurbineImage turbineImage;
     MechanicImage mechanicImage;
     HoverButton pauseImage;
     Image consolebackImage;
     HoverButton crUpImage;
     HoverButton crDownImage;
     HoverButton pump1Image;
     HoverButton pump2Image;
     HoverButton valve1Image;
     HoverButton valve2Image;
     Image sErrorImage;
     
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
        sErrorTexture      = new Texture(Gdx.files.internal("assets\\game\\softwareError.png"));
         
 
         
         gamebgImage      = new Image(gamebgTexture);
         borderImage      = new Image(borderTexture);
         condenserImage   = new HoverButton(condenserTexture, false);
         coolerImage      = new HoverButton(coolerTexture, false);
         pipesImage       = new Image(pipesTexture);
         poweroutImage    = new Image(poweroutTexture);
         reactorImage     = new HoverButton(reactorbackTexture, false);
         turbineImage     = new TurbineImage(this.getPlantStatus());
         mechanicImage    = new MechanicImage();
         pauseImage       = new HoverButton(pauseTexture,  true);
         consolebackImage = new Image(consolebackTexture);
         crUpImage        = new HoverButton(crUpTexture,   false);
         crDownImage      = new HoverButton(crDownTexture, false);
         pump1Image       = new HoverButton(pump1Texture,  false);
         pump2Image       = new HoverButton(pump2Texture,  false);
         valve1Image      = new HoverButton(valve1Texture, false);
         valve2Image      = new HoverButton(valve2Texture, false);
         sErrorImage      = new Image(sErrorTexture);
         
         gamebgImage.setPosition(0, 0);
         borderImage.setPosition(0, 0);
         condenserImage.setPosition(523, 110);
         coolerImage.setPosition(803, 122);
         pipesImage.setPosition(132, 149);
         poweroutImage.setPosition(703, 405);
         reactorImage.setPosition(33, 113);
         turbineImage.setPosition(436, 404);
         mechanicImage.setPosition(630, 75);
         mechanicImage.moveMechanicTo(630f); //ensures the mechanic is initially not moving
         pauseImage.setPosition(17, 15);
         consolebackImage.setPosition(260, 0);
         crUpImage.setPosition(545, 75);
         crDownImage.setPosition(560, 21);
         pump1Image.setPosition(323, 71);
         pump2Image.setPosition(373, 76);
         valve1Image.setPosition(300, 22);
         valve2Image.setPosition(353, 22);
         sErrorImage.setPosition(433, 18);
         
         condenserImage.addListener(listeners.getCondenserListener());
 //        coolerImage.addListener(listeners.getCoolerListener());
         reactorImage.addListener(listeners.getReactorListener());
         pauseImage.addListener(listeners.getPauseListener());
         crUpImage.addListener(listeners.getConrolRodsUpListener());
         crDownImage.addListener(listeners.getConrolRodsDownListener());
         valve1Image.addListener(listeners.getValve1Listener());
         valve2Image.addListener(listeners.getValve2Listener());
         pump1Image.addListener(listeners.getPump1Listener());
         pump2Image.addListener(listeners.getPump2Listener());
         
     }
     
     @Override
     public void show() {
         super.show();
         
         stage.addActor(gamebgImage);
         stage.addActor(borderImage);
         stage.addActor(pipesImage);
         stage.addActor(coolerImage);
         stage.addActor(poweroutImage);
         stage.addActor(reactorImage);
         stage.addActor(turbineImage);
         stage.addActor(condenserImage);
         stage.addActor(mechanicImage);
         stage.addActor(pauseImage);
         stage.addActor(consolebackImage);
         stage.addActor(crUpImage);
         stage.addActor(crDownImage);
         stage.addActor(pump1Image);
         stage.addActor(pump2Image);
         stage.addActor(valve1Image);
         stage.addActor(valve2Image);
         stage.addActor(sErrorImage);
     }
     
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
     }
     
     @Override
     public void render(float delta) {
         super.render(delta);
     }
     
     @Override
     public void hide() {
         stage.clear();
     }
     
     public void moveMechanicTo(float destination) {
         mechanicImage.moveMechanicTo(destination);
     }
     
     public BackyardReactor getGame() {
         return this.game;
     }
 }
