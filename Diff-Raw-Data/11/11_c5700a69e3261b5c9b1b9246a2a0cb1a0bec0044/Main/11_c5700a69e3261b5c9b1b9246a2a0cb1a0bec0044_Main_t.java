 
 package vooga.scroller.level_editor;
 
 import vooga.scroller.level_editor.controllerSuite.LEController;
 import vooga.scroller.level_editor.library.BackgroundLib;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 
 
 public class Main {
 
     /**
      * @param args
      */
     public static void main (String[] args) {
         
         String[] filenames = new String[]{"background_small.png",
                                           "background.png",
                                           "forestbackground.jpg"};
         
         LEController.runLevelEditor(new MarioLib(), new BackgroundLib(filenames));
         
     }
}
