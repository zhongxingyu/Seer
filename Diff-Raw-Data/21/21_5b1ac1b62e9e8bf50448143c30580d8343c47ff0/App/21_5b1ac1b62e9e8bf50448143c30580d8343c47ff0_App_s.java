 package game;
 
 import save.Comptes;
 import xml.OptionXMLParser;
 import xml.XMLFileStore;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.KeyInput;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.input.event.TouchEvent;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.system.AppSettings;
 
 import de.lessvoid.nifty.Nifty;
 
 public class App extends SimpleApplication {
 
 	private Nifty nifty;
 	private NiftyJmeDisplay niftyDisplay;
 
 	public App() {
 		AppSettings set = new AppSettings(true);
 		
 		/* Options */
 		OptionXMLParser.loadAppOptions(XMLFileStore.OPTION_SAVE_FILE);
 		set.setResolution(OptionXMLParser.screenResolution.width, OptionXMLParser.screenResolution.height);
 		set.setTitle(StringStore.APP_TITLE);
 		set.setFullscreen(OptionXMLParser.fullScreen);
 		this.setSettings(set);
 		this.setShowSettings(false);
 	}
 	
 	@Override
 	public void simpleInitApp() {
 
 		/* Lancement application */
 		niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager,
 				audioRenderer, guiViewPort);
 		nifty = niftyDisplay.getNifty();
 
 		/******* DEBUG ********/
 		XMLFileStore.validateXMLFiles(nifty);
 //		nifty.setDebugOptionPanelColors(true);
 		addXMLFiles();
 		/****** FIN DEBUG *****/
 		
 		/* Keyboard Mappings */
 		setInputMappings();
 
 		gotoStart();
 //		gotoOptions();
 		//gotoCrtProfil();
 		/*if (Comptes.getListProfil().size() == 0) {
 			gotoCrtProfil();
 		} else {
 			gotoAffProfil();
 		}*/
 //		gotoCrtProfil();
 //		gotoAffProfil();		
 		
 		// disable the fly cam
 		flyCam.setEnabled(false);
 
 		// attach the nifty display to the gui view port as a processor
 		guiViewPort.addProcessor(niftyDisplay);
 		inputManager.setCursorVisible(false);
 	}
 	
 	void setInputMappings() {
 		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
 		inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_HIDE_STATS);
 	}
 	
 	@Override
 	public void stop() {
 		super.stop();
 	}
 	
 	public void addXMLFiles() {
 		nifty.addXml(XMLFileStore.START_SCREEN_FILE);
 		nifty.addXml(XMLFileStore.OPTION_SCREEN_FILE);
 		nifty.addXml(XMLFileStore.FREE_4_ALL_FILE);
 		nifty.addXml(XMLFileStore.HALF_GAME_FILE);
 		nifty.addXml(XMLFileStore.QUARTER_GAME_FILE);
 		nifty.addXml(XMLFileStore.CREATE_PROFIL_FILE);
 		nifty.addXml(XMLFileStore.CHOOSE_PROFIL_FILE);
 		nifty.addXml(XMLFileStore.ACHAT_FILE);
 		nifty.addXml(XMLFileStore.GARAGE_FILE);
 		nifty.addXml(XMLFileStore.TUTORIEL_FILE);
 	}
 	
 	public void gotoGame(String mode) {
 		if(mode.equals("half")) {
 			nifty.gotoScreen("halfScreen");
 			AbstractGameScreenState gameScreenController = (AbstractGameScreenState) nifty
 					.getScreen("halfScreen").getScreenController();
 			stateManager.attach(gameScreenController);
 		} else {
 			nifty.gotoScreen("quarterScreen");
 			AbstractGameScreenState gameScreenController = (AbstractGameScreenState) nifty
 					.getScreen("quarterScreen").getScreenController();
 			stateManager.attach(gameScreenController);
 		}
 	}
 
 	public void gotoFreeForAll() {
 		nifty.gotoScreen("freeForAllScreen");
 		AbstractGameScreenState gameScreenController = (AbstractGameScreenState) nifty
 				.getScreen("freeForAllScreen").getScreenController();
 		stateManager.attach(gameScreenController);
 	}
 	
 	public void gotoOptions() {
         nifty.gotoScreen("options");
         OptionScreenController gameScreenController = (OptionScreenController) nifty.getScreen("options").getScreenController();
         stateManager.attach(gameScreenController);
 	}
 
 	public void gotoStart() {
 		nifty.gotoScreen("start");
 		StartScreenState startScreenController = (StartScreenState) nifty.getScreen("start").getScreenController();
 		//StartScreenState startScreenController = (StartScreenState) nifty
 		//		.getCurrentScreen().getScreenController();
 		stateManager.attach(startScreenController);
 	}
 
 	public void gotoCrtProfil() {
 		nifty.gotoScreen("createprofil");
         CreateProfilScreen cpfscreen = (CreateProfilScreen) nifty.getScreen("createprofil").getScreenController();
         stateManager.attach(cpfscreen);
 	}
 	
 	public void gotoAffProfil() {
         nifty.gotoScreen("chooseprofil");
         ProfilsScreen pfscreen = (ProfilsScreen) nifty.getScreen("chooseprofil").getScreenController();
         stateManager.attach(pfscreen);
 	}
 	
 	public void gotoAchat() {
 		nifty.addXml(XMLFileStore.ACHAT_FILE);
         nifty.gotoScreen("achat");
         Achat achat = (Achat) nifty.getScreen("achat").getScreenController();
         stateManager.attach(achat);
 	}
 	
 	public void gotoGarage() {
 		nifty.addXml(XMLFileStore.GARAGE_FILE);
         nifty.gotoScreen("garage");
         Garage garage = (Garage) nifty.getScreen("garage").getScreenController();
         stateManager.attach(garage);
 	}
 	
 	public void gotoTutoriel() {
         nifty.gotoScreen("tutoriel");
         TutorielScreen tutoriel = (TutorielScreen) nifty.getScreen("tutoriel").getScreenController();
         stateManager.attach(tutoriel);
 	}
 }
