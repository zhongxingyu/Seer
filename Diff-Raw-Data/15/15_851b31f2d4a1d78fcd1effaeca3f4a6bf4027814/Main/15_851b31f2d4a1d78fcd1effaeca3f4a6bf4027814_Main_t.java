 package vooga.scroller.level_editor;
 
 import java.awt.Dimension;
 import games.scroller.marioGame.spritesDefinitions.MarioLib;
 import games.scroller.marioGame.spritesDefinitions.players.Mario;
 import games.scroller.mr_fish.sprites.FishLib;
 import util.Location;
 import vooga.scroller.level_editor.controllerSuite.LEController;
 import vooga.scroller.level_editor.library.BackgroundLib;
 import vooga.scroller.scrollingmanager.OmniScrollingManager;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.superclasses.Player;
 import vooga.scroller.util.PlatformerConstants;
 import vooga.scroller.view.GameView;
 
 
 public class Main {
 
     /**
      * @param args
      */
     public static void main (String[] args) {
 
        String backgroundPath = "/games/scroller/mr_fish/images/";
         String[] filenames = new String[] { "underwater1.jpg",
                                            "underwater2.jpg",
                                            "underwater3.jpg",
                                            "bikini_bottom.jpg" };
         ScrollingManager sm = new OmniScrollingManager();
         GameView display = new GameView(PlatformerConstants.DEFAULT_WINDOW_SIZE, sm);
         sm.initView(display);
         Player sample = new Mario(new Dimension(20, 32),
                                   display, sm);
 
         LEController.runLevelEditor(new MarioLib(),
                                     new BackgroundLib(backgroundPath, filenames),
                                     sample);
 
     }
 }
