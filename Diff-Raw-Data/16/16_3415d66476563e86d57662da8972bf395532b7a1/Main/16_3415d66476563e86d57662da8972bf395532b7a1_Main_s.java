 package mygame;
 
 
 import AudioNodes.MusicAudioNode;
 import bs.Characters;
 import bs.StandardMatchState;
 import bs.StartState;
 import com.aurellem.capture.Capture;
 import com.aurellem.capture.IsoTimer;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.VideoRecorderAppState;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.Trigger;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.RenderManager;
 import com.jme3.system.AppSettings;
 import de.lessvoid.nifty.Nifty;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 //import com.aurellem.capture.Capture;
 //import com.aurellem.capture.IsoTimer;
 import menu.MainMenu;
 
 
 /**
  * test
  *
  * @author normenhansen
  */
 public class Main extends SimpleApplication 
 {
 
 
     public static Characters charList;
     private StartState startState;
     private boolean isRunning = false;
     private Trigger enterTrigger = new KeyTrigger(KeyInput.KEY_RETURN);
     private static Nifty nifty;
     private NiftyJmeDisplay niftyDisplay;
     private static MusicAudioNode music;
     public static String musicSelection = "Sounds/Music/Fire and Ice.wav";
     private static float musicVolume = 1.0f;
     private static AppSettings settings = new AppSettings(true); // Made this outside of main method so it could be acquired with getSettings()
     
     //Temp Input Mappigns for testing
     public static int[] player1Mappings = new int[]{KeyInput.KEY_W,KeyInput.KEY_A,KeyInput.KEY_D,
         KeyInput.KEY_1,KeyInput.KEY_S,KeyInput.KEY_O,KeyInput.KEY_2,KeyInput.KEY_P};
 
 
     public static void main(String[] args) throws IOException
     {
 //        AppSettings settings = new AppSettings(true);
         
 
         settings.setResolution(800, 600);
         settings.setUseInput(true);
         settings.setTitle("Bionicle Showdown");
         
         settings.setSettingsDialogImage("Textures/Menu/FullVoidLogo.png");
         SimpleApplication app = new Main();
         
         app.setSettings(settings);
         
         /*try {
             //Testing Audio+Video capture
             File video = File.createTempFile("Test", ".avi");
             File audio = File.createTempFile("TestingAudio",".wav");
             app.setTimer(new IsoTimer(60));
             app.setShowSettings(false);
             Capture.captureVideo(app,video);
             Capture.captureAudio(app,audio);
             System.out.println("Vid" + video.getCanonicalPath());
             System.out.println("Aud" + audio.getCanonicalPath());
             
         } catch (IOException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }*/
         
         
         
         app.start();
     }
 
 
     @Override
     public void simpleInitApp () 
     {
         
        //stateManager.attach(new VideoRecorderAppState()); //starts recording(remove when not needed)
         startState = new StartState();
         setDisplayStatView(false);
         music = new MusicAudioNode(assetManager, "Sounds/Music/Fire and Ice.wav", true);
         music.play();
 
         charList = new Characters((SimpleApplication)this);
         initKeys();
         niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
         nifty = niftyDisplay.getNifty();
         guiViewPort.addProcessor(niftyDisplay);
         stateManager.attach(startState);               //Attach the first state
 
         
         flyCam.setEnabled(false);
     }
     /*This ActionListener will handle all the switching of states*/
     private ActionListener actionListener = new ActionListener() 
     {
         public void onAction(String name, boolean isPressed, float tpf) 
         {
             if (name.equals("Start Game") && !isPressed) 
             {
                 if (!isRunning) 
                 {
                     stateManager.detach(startState);
                     isRunning = !isRunning;
                 }
             }
         }
     };
     
     /*Getter for list of characters
      * currently unlocked
      */
     public static Characters getCharList()
     {
         return(charList);
     }
     
     /*Get Nifty Gui
      * 
      */
     public static Nifty getNifty() 
     {
         return (nifty);
     }
     
     public static MusicAudioNode getMusic()
     {
         return (music);
     }
     
     public static String getMusicSelection()
     {
         return musicSelection;
     }
     
     public static void changeMusic(String newMusicSelection)
     {
         music.stop();
         musicSelection = newMusicSelection;
     }
     
     public static void setMusicVolume(float volume)
     {
         musicVolume = volume;
     }
 
 
     private void initKeys() 
     {
         inputManager.addMapping("Start Game", enterTrigger);
         inputManager.addListener(actionListener, new String[]{"Start Game"});
     }
     
     /**
      * Returns the settings for use by the menu; so buttons are set up properly.
      * If not available, the isClickable() method in Class Button will not work correctly.
      * @return The game's settings. 
      */
     public static AppSettings getSettings()
     {
         return settings;
     }
 
 
     @Override
     public void simpleUpdate(float tpf) 
     {
 //        if (musicShouldStop)
 //        {
 //            music.stop();
 //        }
         // Couldn't you just use music.setLooping(true); ? That would loop it without having to check, I believe.
         if(music.getStatus() == MusicAudioNode.Status.Stopped)
         {
             music = new MusicAudioNode(assetManager, musicSelection, true);
             music.setVolume(musicVolume);
             music.play();
         }
     }
 
 
     @Override
     public void simpleRender(RenderManager rm) 
     {
         //TODO: add render code
     }
 }
 
 
