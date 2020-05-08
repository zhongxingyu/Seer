 package px.tileEngineV2.desktop.core;
 
 import px.tileEngineV2.desktop.graphics.Renderer_Desktop;
 
 public class GameLauncher {
     
     /**@param args */
     public static void main(String[] args) {
         GameLauncher launcher = new GameLauncher();
         launcher.init();
     }
     
     // ++++ ++++ Data ++++ ++++
     
     private Renderer_Desktop renderer;
     private GameCore_Desktop gameCore;
     private Frame frame;
     
     // ++++ ++++ Initialization ++++ ++++
     
     private GameLauncher() {
         frame = new Frame();
         gameCore = new GameCore_Desktop();
         renderer = new Renderer_Desktop();
     }
     
     /**Performs all synchronized initialization. */
     private void init() {
         gameCore.assignRenderer(renderer);
         frame.setupBorderless();
        //frame.setupFullscreen(GraphicsEnvironment.getLocalGraphicsEnvironment()
        //.getDefaultScreenDevice().getDisplayMode());
        //frame.setupWindowed(new Dimension(1280, 720));
         gameCore.setFrame(frame);
         renderer.start();
     }
 }
