 package game;
 
 import input.KeyboardListener;
 import java.io.IOException;
 import settings.Settings;
 import actor.ActorSet;
 import actor.Asteroid;
 
 
 public class Game {
     private static graphics.Renderer renderer;
     private static input.KeyboardListener input;
     private static Player player;
     private static Map map;
     private static ActorSet actors;
     private static GameThread game;
 
     //for HUD radar testing, will be removed later
     static actor.Asteroid a;
 
     public static void init() {
         System.out.println(Runtime.getRuntime().availableProcessors() + " available cores detected");
         try {
             Settings.init();
         } catch (IOException e) {
             e.printStackTrace();
         }
         map = Map.load("example_1");
         player = new Player();
         actors = new ActorSet();
         actors.addAll(map.actors);
         player.respawn(actors, map.getSpawnPosition());
         
         renderer = new graphics.Renderer(player.getCamera());
         input = new KeyboardListener();
         graphics.Model.loadModels();
        sound.Manager.initialize(player.getCamera());
 
         game = new GameThread(actors);
         // CL - We need to get input even if the game is paused,
         //      that way we can unpause the game.
         game.addCallback(input);
         game.addCallback(new Updateable() {
             public void update() {
                 player.updateCamera();
             }
         });
         game.addCallback(new Updateable() {
             public void update() {
                 sound.Manager.processEvents();
             }
         });
     }
     //for HUD radar testing, will be removed later
     public static Asteroid getAsteroid() {
         return a;
     }
 
     public static void joinServer(String server) {
         player = new Player();
         map = Map.load("example_1");
 
         network.ClientServerThread.joinServer(server, player);
 
         //renderer = new graphics.Renderer();
         input = new KeyboardListener();
         graphics.Model.loadModels();
 
         
         a.setPosition(new math.Vector3f(0.0f,0.0f,-10.0f));
         //actor.Actor.addActor(a);
         
         //new GameThread().start();
     }
 
     public static void start() {
         game.start();
         renderer.start();
     }
 
     public static KeyboardListener getInputHandler(){
         return input;
     }
 
     public static Player getPlayer() {
         return player;
     }
 
     public static boolean isPaused() {
         return game.getGameState() == GameThread.STATE_PAUSED;
     }
 
     public static void quitToMenu() {
         game.setGameState(GameThread.STATE_STOPPED);
     }
 
     public static void togglePause() {
         if (isPaused())
             game.setGameState(GameThread.STATE_RUNNING);
         else
             game.setGameState(GameThread.STATE_PAUSED);
     }
 
     public static void exit() {
         System.exit(0);
     }
 
     public static Map getMap() {
         return map;
     }
     
     public static void setMap(Map m) {
         map = m;
     }
 
     public static void main (String []args) throws IOException {
         Game.init();
         Game.start();
     }
 
     public static graphics.Renderer getRenderer() {
         return renderer;
     }
     
     public static ActorSet getActors() {
         return actors;
     }
 }
