 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * Game engine.
  */
 package engine;
 
 import celestial.Celestial;
 import celestial.Jumphole;
 import celestial.Planet;
 import celestial.Ship.CargoPod;
 import celestial.Ship.Explosion;
 import celestial.Ship.Projectile;
 import celestial.Ship.Ship;
 import celestial.Ship.Station;
 import gdi.CargoWindow;
 import gdi.CommWindow;
 import gdi.EquipmentWindow;
 import gdi.FuelWindow;
 import gdi.HealthWindow;
 import gdi.MenuHomeWindow;
 import gdi.OverviewWindow;
 import gdi.PropertyWindow;
 import gdi.StandingWindow;
 import gdi.StarMapWindow;
 import gdi.TradeWindow;
 import gdi.component.AstralWindow;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.FileInputStream;
import java.io.InputStream;
 import java.io.ObjectInputStream;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import lib.AstralIO;
 import lib.AstralMessage;
 import lib.Binling;
 import lib.Conversation;
 import lib.Parser;
 import lib.Parser.Term;
 import lib.Soundling;
 import universe.SolarSystem;
 import universe.Universe;
 
 /**
  *
  * @author Nathan Wiehoff
  */
 public class Engine {
     //aspect ratio constants
 
     public static final double WIDE = 1.7778;
     public static final double STD = 1.3333;
     //graphics and threading
     BufferStrategy bf;
     Element element;
     //HUD
     protected HUD hud = new HUD(this);
     //Sound
     protected SoundEngine sound = new SoundEngine(this);
     //dimensions
     private int uiX;
     private int uiY;
     //game entities
     ArrayList<Entity> entities;
     private Universe universe;
     //engine state
     double dilation = 1; // time dilation
     double dx = 0; //render x differential
     double dy = 0; //render y differential
     double pvx = 0; //player x velocity
     double pvy = 0; //player y velocity
     private Ship playerShip;
     //mouse
     private int mouseX;
     private int mouseY;
     //button state toggles
     private boolean allStopPressed = false;
     private boolean firing = false;
 
     public HUD getHud() {
         return hud;
     }
 
     public void setHud(HUD hud) {
         this.hud = hud;
     }
 
     enum State {
 
         MENU,
         RUNNING,
         PAUSED
     }
     State state = State.MENU;
     //io
     AstralIO io = new AstralIO();
 
     public Engine(BufferStrategy bf, int uiX, int uiY) {
         //store graphics
         this.uiX = uiX;
         this.uiY = uiY;
         this.bf = bf;
         //initialize entities
         entities = new ArrayList<>();
         //create components
         element = new Element();
         //halt components
         stop();
     }
 
     public void suicide() {
         /*
          * Cleans out everything! Yaay!
          */
         entities = new ArrayList<>();
         universe = null;
         playerShip = null;
         state = State.PAUSED;
     }
 
     public void resurrect() {
         /*
          * Brings the engine back to life after suicide called. This is used
          * under the assumption a game is being loaded.
          */
         for (int a = 0; a < entities.size(); a++) {
             entities.get(a).init(true);
         }
         state = State.RUNNING;
     }
 
     public void addEntity(Entity entity) {
         if (!entities.contains(entity)) {
             entities.add(entity);
         }
     }
 
     public void removeEntity(Entity entity) {
         if (entities.contains(entity)) {
             entities.remove(entity);
         }
     }
 
     public Universe getUniverse() {
         return universe;
     }
 
     public void setUniverse(Universe universe) {
         this.universe = universe;
         //add this universe's systems to the entity list
         for (int a = 0; a < universe.getSystems().size(); a++) {
             entities.add(universe.getSystems().get(a));
         }
         //store the player reference
         playerShip = universe.getPlayerShip();
     }
 
     /*
      * Enters the menu state
      */
     public final void menu() {
         if (state != state.MENU) {
             state = State.MENU;
             hud.pack();
         } else {
             state = State.RUNNING;
             hud.pack();
         }
     }
 
     /*
      * Begins execution of the simulation
      */
     public final void start() {
         state = State.RUNNING;
         hud.pack();
     }
 
     public void load(String savePath) {
         try {
             String home = System.getProperty("user.home") + "/.highalbedo/";
             System.out.println("Starting Quickload.");
             //get everything
             AstralIO.Everything everything;
             FileInputStream fis = new FileInputStream(home + savePath);
             ObjectInputStream ois = new ObjectInputStream(fis);
             everything = (AstralIO.Everything) ois.readObject();
             //unpack universe
             Universe universe = everything.getUniverse();
             //restore transient objects
             loadUniverse(universe);
             System.out.println("Quickload Complete.");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void loadUniverse(Universe universe) {
         suicide();
         setUniverse(universe);
         resurrect();
     }
 
     public void newGame() {
         universe = new Universe();
         setUniverse(universe);
         //send welcome message
         Conversation welcome = new Conversation(universe.getPlayerShip(), "Introduction", "WelcomeMessage0");
         universe.getPlayerShip().setConversation(welcome);
     }
 
     /*
      * Pauses execution of the simulation
      */
     public final void stop() {
         state = State.PAUSED;
     }
 
     /*
      * Sound class, makes sense to put it here
      */
     public class SoundEngine implements EngineElement {
 
         private Clip music;
         private SolarSystem lastSys;
         private String ambientTrack = "audio/music/Menu Noises.wav";
         private String dangerTrack = "audio/music/Committing.wav";
         boolean isAmbient = true;
 
         public SoundEngine(Engine engine) {
             try {
                 music = AudioSystem.getClip();
                 //load menu track
                URL snURL =  getClass().getClassLoader().getResource("resource/"+ambientTrack);
                AudioInputStream stream = AudioSystem.getAudioInputStream(snURL);
                 music.open(stream);
                 //start
                 music.loop(Clip.LOOP_CONTINUOUSLY);
             } catch (Exception ex) {
                 Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                 System.err.println("This likely means music is disabled.");
             }
         }
 
         @Override
         public void periodicUpdate() {
             try {
                 if (state == State.RUNNING) {
                     if (universe.getSettings().MUSIC) {
                         updateMusic();
                     } else {
                         if (music.isRunning()) {
                             music.stop();
                         }
                     }
                     if (universe.getSettings().SOUND_EFFECTS) {
                         checkForSoundSignals();
                     }
                 } else if (state == State.MENU) {
                 } else {
                     //do nothing
                 }
             } catch (Exception e) {
                 System.out.println("Audio engine encountered a problem.");
                 e.printStackTrace();
             }
         }
 
         private void updateMusic() throws Exception {
             /*
              * See if the player has changed systems
              */
             if (lastSys != null) {
                 if (lastSys != playerShip.getCurrentSystem()) {
                     lastSys = playerShip.getCurrentSystem();
                     //update tracks
                 }
             } else {
                 lastSys = playerShip.getCurrentSystem();
             }
             /*
              * Music is determined by region. Each owner has their own music
              * as defined in
              */
             boolean danger = false;
             ArrayList<Ship> tests = playerShip.getShipsInSensorRange();
             for (int a = 0; a < tests.size(); a++) {
                 if (playerShip.getStandingsToMe(tests.get(a)) < -2) {
                     danger = true;
                     break;
                 }
             }
             if (!danger) {
                 if (ambientTrack.matches(playerShip.getCurrentSystem().getAmbientMusic()) && isAmbient) {
                     //do nothing
                 } else {
                     //stop current track
                     music.stop();
                     //free the music line
                     music.close();
                     //load the correct track
                     ambientTrack = playerShip.getCurrentSystem().getAmbientMusic();
                     music = AudioSystem.getClip();
                     //load stream
                    AudioInputStream stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("resource/"+ambientTrack));
                     music.open(stream);
                     //start
                     music.loop(Clip.LOOP_CONTINUOUSLY);
                     isAmbient = true;
                 }
             } else if (danger) {
                 if (dangerTrack.matches(playerShip.getCurrentSystem().getDangerMusic()) && !isAmbient) {
                     //do nothing
                 } else {
                     //stop current track
                     music.stop();
                     //free the music line
                     music.close();
                     //load the correct track
                     dangerTrack = playerShip.getCurrentSystem().getDangerMusic();
                     music = AudioSystem.getClip();
                     //load stream
                    AudioInputStream stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource("resource/"+dangerTrack));
                     music.open(stream);
                     //start
                     music.loop(Clip.LOOP_CONTINUOUSLY);
                     isAmbient = false;
                 }
             }
         }
 
         private void checkForSoundSignals() {
             /*
              * Ships have to request a sound be played. The sound engine will
              * then evaluate that request to determine whether or not to play
              * the sound
              */
 
             //get list of ships in the player's system
             ArrayList<Entity> tmp = playerShip.getCurrentSystem().getShipList();
             //use an array because it's faster
             Ship[] ships = new Ship[tmp.size()];
             for (int a = 0; a < ships.length; a++) {
                 ships[a] = (Ship) tmp.get(a);
             }
             //iterate through each ship
             for (int a = 0; a < ships.length; a++) {
                 //get the sound que
                 ArrayList<Soundling> que = ships[a].getSoundQue();
                 //does it have anything waiting?
                 if (que != null) {
                     if (que.size() > 0) {
                         //is this ship in range of the sound system?
                         double distance = playerShip.distanceTo(ships[a]);
                         if (distance < Math.max(uiX, uiY)) {
                             //yes, play each clip in the list
                             for (int c = 0; c < que.size(); c++) {
                                 //get soundling
                                 Soundling snd = que.get(c);
                                 if (!snd.isPlaying()) {
                                     snd.play();
                                 }
                                 //pop
                                 que.remove(snd);
                             }
                         } else {
                             //out of range
                         }
                     } else {
                         //nope
                     }
                 }
             }
         }
     }
 
     /*
      * HUD class, makes sense to put it here.
      */
     public class HUD implements EngineElement {
         //Window list
 
         ArrayList<AstralWindow> windows = new ArrayList<>();
         //menu windows
         MenuHomeWindow homeWindow;
         //in-game windows
         HealthWindow healthWindow = new HealthWindow();
         FuelWindow fuelWindow = new FuelWindow();
         OverviewWindow overviewWindow = new OverviewWindow();
         EquipmentWindow equipmentWindow = new EquipmentWindow();
         CargoWindow cargoWindow = new CargoWindow();
         TradeWindow tradeWindow = new TradeWindow();
         StarMapWindow starMapWindow = new StarMapWindow();
         StandingWindow standingWindow = new StandingWindow();
         PropertyWindow propertyWindow = new PropertyWindow();
         CommWindow commWindow = new CommWindow();
 
         public HUD(Engine engine) {
             homeWindow = new MenuHomeWindow(engine);
         }
 
         public void pack() {
             windows.clear();
             if (state == State.RUNNING) {
                 windows.add(healthWindow);
                 windows.add(fuelWindow);
                 windows.add(overviewWindow);
                 windows.add(equipmentWindow);
                 cargoWindow.setVisible(false);
                 windows.add(cargoWindow);
                 windows.add(tradeWindow);
                 windows.add(starMapWindow);
                 windows.add(standingWindow);
                 standingWindow.setVisible(false);
                 windows.add(propertyWindow);
                 propertyWindow.setVisible(false);
                 windows.add(commWindow);
             } else if (state == State.MENU) {
                 windows.add(homeWindow);
                 homeWindow.setVisible(true);
             }
         }
 
         public void render(Graphics f) {
             if (state == State.RUNNING) {
                 //position health window
                 healthWindow.setX((uiX / 2) - healthWindow.getWidth() / 2);
                 healthWindow.setY(uiY - 55);
                 //position fuel window
                 fuelWindow.setX((uiX / 2) - fuelWindow.getWidth() / 2);
                 fuelWindow.setY(uiY - 40);
                 //position overview window
                 overviewWindow.setX((uiX - (overviewWindow.getWidth() + 20)));
                 overviewWindow.setY(uiY - (overviewWindow.getHeight() + 20));
                 //position equipment window
                 equipmentWindow.setX(20);
                 equipmentWindow.setY(uiY - (equipmentWindow.getHeight() + 20));
                 //position cargo window
                 cargoWindow.setX((uiX / 2) - cargoWindow.getWidth() / 2);
                 cargoWindow.setY((uiY / 2) - cargoWindow.getHeight() / 2);
                 //position trade window
                 tradeWindow.setX((uiX / 2) - tradeWindow.getWidth() / 2);
                 tradeWindow.setY((uiY / 2) - tradeWindow.getHeight() / 2);
                 //position map window
                 starMapWindow.setX((uiX / 2) - starMapWindow.getWidth() / 2);
                 starMapWindow.setY((uiY / 2) - starMapWindow.getHeight() / 2);
                 //position standing window
                 standingWindow.setX((uiX / 2) - standingWindow.getWidth() / 2);
                 standingWindow.setY((uiY / 2) - standingWindow.getHeight() / 2);
                 //position property window
                 propertyWindow.setX((uiX / 2) - propertyWindow.getWidth() / 2);
                 propertyWindow.setY((uiY / 2) - propertyWindow.getHeight() / 2);
                 //position comm window
                 commWindow.setX(20);
                 commWindow.setY(20);
             } else if (state == State.MENU) {
                 //position home window
                 homeWindow.setX((uiX / 2) - homeWindow.getWidth() / 2);
                 homeWindow.setY((uiY / 2) - homeWindow.getHeight() / 2);
             }
             //render
             for (int a = windows.size() - 1; a >= 0; a--) {
                 if (windows.get(a).isVisible()) {
                     windows.get(a).render(f);
                 }
             }
         }
 
         public void periodicUpdate() {
             if (state == State.RUNNING) {
                 //push hud changes
                 if (healthWindow.isVisible()) {
                     healthWindow.updateHealth((playerShip.getShield() / playerShip.getMaxShield()),
                             (playerShip.getHull() / playerShip.getMaxHull()));
                 }
                 if (fuelWindow.isVisible()) {
                     fuelWindow.updateFuel(playerShip.getFuel() / playerShip.getMaxFuel());
                 }
                 if (overviewWindow.isVisible()) {
                     overviewWindow.updateOverview(playerShip);
                 }
                 if (equipmentWindow.isVisible()) {
                     equipmentWindow.update(playerShip);
                 }
                 if (cargoWindow.isVisible()) {
                     cargoWindow.update(playerShip);
                 }
                 if (tradeWindow.isVisible()) {
                     tradeWindow.update(playerShip);
                 }
                 if (starMapWindow.isVisible()) {
                     starMapWindow.updateMap(universe);
                 }
                 if (standingWindow.isVisible()) {
                     standingWindow.update(playerShip);
                 }
                 if (propertyWindow.isVisible()) {
                     propertyWindow.update(playerShip);
                 }
                 if (commWindow.isVisible()) {
                     commWindow.update(playerShip);
                 }
                 //update
                 for (int a = 0; a < windows.size(); a++) {
                     windows.get(a).periodicUpdate();
                 }
             } else if (state == State.MENU) {
             }
         }
 
         /*
          * The following are window event handlers. Do not add game logic to them.
          */
         public void checkFocusChanges() {
             /*
              * Window focus is determined based on mouse position.
              */
             Rectangle mRect = new Rectangle(mouseX, mouseY, 1, 1);
             boolean foundOne = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).intersects(mRect) && windows.get(a).isVisible() && !foundOne) {
                     windows.get(a).setFocused(true);
                     windows.get(a).setOrder(0);
                     foundOne = true;
                 } else {
                     windows.get(a).setFocused(false);
                     windows.get(a).setOrder(windows.get(a).getOrder() - 1);
                 }
             }
             if (foundOne) {
                 /*
                  * Since sorting can be expensive, I only resort windows when the focus is known
                  * to have changed.
                  */
                 AstralWindow arr[] = new AstralWindow[windows.size()];
                 for (int a = 0; a < windows.size(); a++) {
                     arr[a] = windows.get(a);
                 }
                 for (int a = 0; a < arr.length; a++) {
                     for (int b = 0; b < arr.length; b++) {
                         if (arr[a].getOrder() > arr[b].getOrder()) {
                             AstralWindow tmp = arr[b];
                             arr[b] = arr[a];
                             arr[a] = tmp;
                         }
                     }
                 }
                 windows.clear();
                 windows.addAll(Arrays.asList(arr));
             }
         }
 
         public void handleMouseMovedEvent(MouseEvent me) {
             //store mouse position
             mouseX = me.getX();
             mouseY = me.getY();
             //check to see if this needs to be intercepted
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleMouseMovedEvent(me);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
             }
         }
 
         public void handleMousePressedEvent(MouseEvent me) {
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleMousePressedEvent(me);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
             }
         }
 
         public void handleMouseReleasedEvent(MouseEvent me) {
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleMouseReleasedEvent(me);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
             }
         }
 
         public void handleMouseClickedEvent(MouseEvent me) {
             checkFocusChanges();
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleMouseClickedEvent(me);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
                 Rectangle mRect = new Rectangle((int) dx + me.getX(), (int) dy + me.getY(), 1, 1);
                 //check to see if it intersected any ships or objects
                 ArrayList<Entity> tmpE = playerShip.getCurrentSystem().getEntities();
                 for (int a = 0; a < tmpE.size(); a++) {
                     if (tmpE.get(a) instanceof Ship) {
                         Ship tmp = (Ship) tmpE.get(a);
                         if (tmp.collideWith(mRect) && tmp != playerShip) {
                             playerShip.setTarget(tmp);
                             break;
                         }
                     }
                 }
             }
         }
 
         public void handleKeyTypedEvent(KeyEvent ke) {
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleKeyTypedEvent(ke);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
             }
         }
 
         public void handleKeyPressedEvent(KeyEvent ke) {
             boolean windowIntercepted = false;
             for (int a = 0; a < windows.size(); a++) {
                 if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                     windows.get(a).handleKeyPressedEvent(ke);
                     windowIntercepted = true;
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
                 /*
                  * In-space
                  */
                 if (!playerShip.isDocked()) {
                     if (ke.getKeyCode() == KeyEvent.VK_UP) {
                         playerShip.setThrustRear(true);
                     } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                         playerShip.setThrustForward(true);
                     } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                         playerShip.setRotatePlus(true);
                     } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                         playerShip.setRotateMinus(true);
                     } else if (ke.getKeyCode() == KeyEvent.VK_HOME) {
                         playerShip.setAutopilot(Ship.Autopilot.NONE);
                         playerShip.setBehavior(Ship.Behavior.NONE);
                         if (playerShip.getPort() != null) {
                             playerShip.getPort().setClient(null);
                             playerShip.setPort(null);
                         }
                         allStopPressed = true;
                     } else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
                         firing = true;
                     }
                 } else {
                     /*
                      * Docked
                      */
                 }
             }
         }
 
         public void handleKeyReleasedEvent(KeyEvent ke) {
             boolean windowIntercepted = false;
             if (ke.getKeyCode() == KeyEvent.VK_F1) {
                 menu();
             } else if (ke.getKeyCode() == KeyEvent.VK_F5) {
                 //defocus all windows
                 for (int a = 0; a < windows.size(); a++) {
                     windows.get(a).setFocused(false);
                 }
             } else if (ke.getKeyCode() == KeyEvent.VK_F6) {
                 //defocus all windows and hide them
                 for (int a = 0; a < windows.size(); a++) {
                     windows.get(a).setFocused(false);
                     windows.get(a).setVisible(false);
                 }
                 //show these since they are always visible
                 healthWindow.setVisible(true);
                 fuelWindow.setVisible(true);
             } else {
                 for (int a = 0; a < windows.size(); a++) {
                     if (windows.get(a).isFocused() && windows.get(a).isVisible()) {
                         windows.get(a).handleKeyReleasedEvent(ke);
                         windowIntercepted = true;
                     }
                 }
             }
             /*
              * Now game logic
              */
             if (!windowIntercepted) {
                 /*
                  * In-space
                  */
                 if (!playerShip.isDocked()) {
                     if (ke.getKeyCode() == KeyEvent.VK_UP) {
                         playerShip.setThrustRear(false);
                     } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                         playerShip.setThrustForward(false);
                     } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                         playerShip.setRotatePlus(false);
                     } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                         playerShip.setRotateMinus(false);
                     } else if (ke.getKeyCode() == KeyEvent.VK_HOME) {
                         allStopPressed = false;
                     }/*
                      * weapon keys
                      */ else if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
                         firing = false;
                     } /*
                      * targeting keys
                      */ else if (ke.getKeyCode() == KeyEvent.VK_R) {
                         playerShip.targetNearestHostileShip();
                     } /*
                      * comms keys
                      */ else if (ke.getKeyCode() == KeyEvent.VK_D) {
                         playerShip.cmdDock(playerShip.getTarget());
                     } else if (ke.getKeyCode() == KeyEvent.VK_H) {
                         if (playerShip.getTarget() != null) {
                             playerShip.getTarget().hail();
                             commWindow.setVisible(true);
                         }
                     }
                 } else {
                     /*
                      * docked
                      */
                     if (ke.getKeyCode() == KeyEvent.VK_D) {
                         playerShip.cmdUndock();
                     }
                 }
                 /*
                  * The following commands are independent of being docked or not. Use with
                  * extreme caution and watch for overlap.
                  */
                 if (ke.getKeyCode() == KeyEvent.VK_E) {
                     equipmentWindow.setVisible(!equipmentWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_S) {
                     overviewWindow.setVisible(!overviewWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_M) {
                     starMapWindow.setVisible(!starMapWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_C) {
                     cargoWindow.setVisible(!cargoWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_T) {
                     if (playerShip.isDocked()) {
                         tradeWindow.setVisible(!tradeWindow.isVisible());
                     }
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_L) {
                     standingWindow.setVisible(!standingWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_P) {
                     propertyWindow.setVisible(!propertyWindow.isVisible());
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_I) {
                     commWindow.setVisible(!commWindow.isVisible());
                 }
                 /*
                  * Time dilation keys
                  */
                 if (ke.getKeyCode() == KeyEvent.VK_1) {
                     dilation = 0.25;
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_2) {
                     dilation = 0.5;
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_3) {
                     dilation = 1;
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_4) {
                     dilation = 2;
                 }
                 if (ke.getKeyCode() == KeyEvent.VK_5) {
                     dilation = 4;
                 }
             }
         }
     }
     /*
      * Responsible for drawing and updating the universe.
      */
 
     private class Element implements EngineElement {
         //timing
 
         private long lastFrame;
         //rendering helpers
         BufferedImage frame = new BufferedImage(uiX, uiY, BufferedImage.TYPE_INT_RGB); //double buffered frame
         Graphics2D f = (Graphics2D) frame.getGraphics(); //graphics context for the frame
         Rectangle clip = null;
         //per system
         Image backplate;
         String lastPlate;
         //the thread thing
         Thread th = new Thread(new Runnable() {
             @Override
             public void run() {
                 while (true) {
                     try {
                         periodicUpdate();
                         //Thread.sleep(5);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
 
         public Element() {
             th.start();
         }
 
         @Override
         public void periodicUpdate() {
             try {
                 //game logic
                 logic();
                 //god
                 god();
                 //render final
                 render();
             } catch (IllegalStateException e) {
                 //lol
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
         public void generateBackdrop() {
             //generate the backdrop
             createStars();
         }
 
         private double getAspectRatio() {
             return ((double) uiX / (double) uiY);
         }
 
         private void createStars() {
             if (state == State.RUNNING) {
                 if (playerShip != null) {
                     //determine aspect ratio
                     double aspect = getAspectRatio();
                     //determine whether we are closer to 16x9/16x10 or 4x3
                     String plateGroup = "";
                     if (Math.abs(aspect - STD) < Math.abs(aspect - WIDE)) {
                         //aproximately std ratio
                         plateGroup = "std";
                     } else {
                         //approximately wide ratio
                         plateGroup = "wide";
                     }
                     //for safety reasons grab the generic plate first
                     Image stars = null;
                     try {
                         stars = io.loadImage("plate/" + plateGroup + "/base_plate.png");
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     //get base plate
                     try {
                         Image test = io.loadImage("plate/" + plateGroup + "/" + playerShip.getCurrentSystem().getBack());
                         stars = test;
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     //scale backplate to screen size
                     stars = stars.getScaledInstance(uiX, uiY, Image.SCALE_SMOOTH);
                     backplate = stars;
                     lastPlate = playerShip.getCurrentSystem().getBack();
                 }
             } else if (state == State.MENU) {
                 double aspect = getAspectRatio();
                 //determine whether we are closer to 16x9/16x10 or 4x3
                 String plateGroup = "";
                 if (Math.abs(aspect - STD) < Math.abs(aspect - WIDE)) {
                     //aproximately std ratio
                     plateGroup = "std";
                 } else {
                     //approximately wide ratio
                     plateGroup = "wide";
                 }
                 //for safety reasons grab the generic plate first
                 Image stars = null;
                 try {
                     stars = io.loadImage("plate/" + plateGroup + "/base_plate.png");
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 //get a list of base plates
                 Parser sky = Universe.getCache().getSkyCache();
                 ArrayList<Term> skyTypes = sky.getTermsOfType("Skybox");
                 int pick = new Random().nextInt(skyTypes.size());
                 //get the asset
                 String asset = skyTypes.get(pick).getValue("asset");
                 //store
                 try {
                     Image test = io.loadImage("plate/" + plateGroup + "/" + asset);
                     stars = test;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 //scale backplate to screen size
                 stars = stars.getScaledInstance(uiX, uiY, Image.SCALE_SMOOTH);
                 backplate = stars;
                 lastPlate = asset;
             }
         }
 
         /*
          * Rendering
          */
         private void render() throws Exception {
             //setup strategy
             /*f.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);*/
             f.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             //setup clip
             if (clip != null) {
                 clip = new Rectangle(0, 0, uiX, uiY);
             }
             //setup graphics
             Graphics g = bf.getDrawGraphics();
             g.setClip(clip);
             if (state == State.RUNNING) {
                 /*
                  * This section renders the game
                  */
                 //backplate
                 if (lastPlate != null && playerShip != null) {
                     if (lastPlate.matches(playerShip.getCurrentSystem().getBack())) {
                         f.drawImage(backplate, 0, 0, null);
                     } else {
                         element.generateBackdrop();
                     }
                 } else {
                     element.generateBackdrop();
                 }
                 //update render view
                 Rectangle view = new Rectangle((int) dx, (int) dy, uiX, uiY);
                 //render entities in current solar system
                 try {
                     if (playerShip != null) {
                         SolarSystem current = playerShip.getCurrentSystem();
                         if (current != null) {
                             ArrayList<Entity> celestialList = current.getCelestialList();
                             ArrayList<Entity> asteroidList = current.getAsteroidList();
                             ArrayList<Entity> stationList = current.getStationList();
                             ArrayList<Entity> shipList = current.getShipList();
                             ArrayList<Entity> jumpholeList = current.getJumpholeList();
 
                             /*
                              * Render celestials first
                              */
 
                             for (int a = 0; a < celestialList.size(); a++) {
                                 if (celestialList.get(a).collideWith(view)) {
                                     celestialList.get(a).render(f, dx, dy);
                                 }
                             }
 
                             for (int a = 0; a < asteroidList.size(); a++) {
                                 if (asteroidList.get(a).collideWith(view)) {
                                     asteroidList.get(a).render(f, dx, dy);
                                 }
                             }
 
                             /*
                              * Now render stations
                              */
 
                             for (int a = 0; a < stationList.size(); a++) {
                                 if (stationList.get(a).getState() != Entity.State.DEAD) {
                                     if (stationList.get(a).collideWith(view)) {
                                         if (stationList.get(a) == playerShip.getTarget()) {
                                             renderTargetMarker();
                                         } else {
                                             renderIFFMarker((Ship) stationList.get(a));
                                         }
                                         stationList.get(a).render(f, dx, dy);
                                     }
                                 }
                             }
 
                             /*
                              * Now render ships
                              */
 
                             for (int a = 0; a < shipList.size(); a++) {
                                 if (shipList.get(a).getState() != Entity.State.DEAD) {
                                     if (shipList.get(a).collideWith(view)) {
                                         if (shipList.get(a) == playerShip.getTarget()) {
                                             renderTargetMarker();
                                         } else {
                                             renderIFFMarker((Ship) shipList.get(a));
                                         }
                                         shipList.get(a).render(f, dx, dy);
                                     }
                                 }
                             }
 
                             /*
                              * Render jumpholes last
                              */
                             for (int a = 0; a < jumpholeList.size(); a++) {
                                 if (jumpholeList.get(a).collideWith(view)) {
                                     jumpholeList.get(a).render(f, dx, dy);
                                 }
                             }
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else if (state == State.MENU) {
                 /*
                  * This section renders the main menu
                  */
                 if (lastPlate != null) {
                     f.drawImage(backplate, 0, 0, null);
                 } else {
                     element.generateBackdrop();
                 }
 
             }
             Toolkit.getDefaultToolkit().sync();
             //render HUD
             getHud().render(f);
             //use ui graphics context to draw
             if (!bf.contentsLost()) {
                 g.drawImage(frame, 0, 0, null);
                 bf.show();
             }
         }
 
         /*
          * Updating
          */
         public void logic() {
             //get time since last frame
             long dt = System.nanoTime() - lastFrame;
             //store current time
             lastFrame = System.nanoTime();
             //calculate time per frame
             double tpf = Math.abs(dt / 1000000000.0) * dilation;
             //hard limit
             if (tpf > 0.1) {
                 tpf = 0.1;
             }
             if (state == State.RUNNING && universe != null) {
                 //handle player events
                 handlePlayerEvents();
                 //collission test
                 try {
                     collissionTest(tpf);
                 } catch (Exception e) {
                     System.out.println("Collission tester dun goof'd");
                 }
                 //update game entities
                 for (int a = 0; a < entities.size(); a++) {
                     entities.get(a).periodicUpdate(tpf);
                     if (entities.get(a).getState() == Entity.State.DEAD) {
                         //remove the entity
                         entities.remove(a);
                     }
                 }
                 //update sound
                 sound.periodicUpdate();
                 //update hud
                 hud.periodicUpdate();
                 //update differentials for rendering
                 dx = (int) playerShip.getX() - (uiX / 2) + (playerShip.getWidth() / 2);
                 dy = (int) playerShip.getY() - (uiY / 2) + (playerShip.getHeight() / 2);
                 pvx = playerShip.getVx();
                 pvy = playerShip.getVy();
                 //recover player ship
                 playerShip = universe.getPlayerShip();
                 //update player missions
                 for (int a = 0; a < universe.getPlayerMissions().size(); a++) {
                     universe.getPlayerMissions().get(a).periodicUpdate(tpf);
                 }
             } else if (state == State.MENU) {
                 //update HUD
                 getHud().periodicUpdate();
             }
         }
 
         private void god() {
             if (universe != null) {
                 if (universe.getGod() != null) {
                     universe.getGod().periodicUpdate();
                 }
             }
         }
 
         private void collissionTest(double tpf) throws Exception {
             /*
              * 1. Collissions are not tested on planets
              * 2. Collissions are only tested between entities in the same solar system.
              */
             for (int a = 0; a < universe.getSystems().size(); a++) {
                 ArrayList<Entity> objects = universe.getSystems().get(a).getEntities();
                 for (int b = 0; b < objects.size(); b++) {
                     if ((objects.get(b) instanceof Planet == false) || (objects.get(b) instanceof Jumphole)) {
                         for (int c = 0; c < objects.size(); c++) {
                             if ((objects.get(c) instanceof Planet == false) || (objects.get(c) instanceof Jumphole)) {
                                 if (objects.get(b) != objects.get(c)) {
                                     if (objects.get(b).collideWith(objects.get(c))) {
                                         elasticCollision(objects.get(b), objects.get(c), tpf);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
         private void elasticCollision(Entity a, Entity b, double tpf) {
             //inform them of the collision for any special events
             if (!(a instanceof Explosion) && !(b instanceof Explosion)) {
                 a.informOfCollisionWith(b);
                 b.informOfCollisionWith(a);
             }
             //physics time
             if (a instanceof Celestial && b instanceof Celestial) {
                 /*
                  * I don't really want this physics being applied between projectiles
                  * and their targets.
                  */
                 if (!(a instanceof CargoPod || b instanceof CargoPod)) {
                     if (!(a instanceof Projectile || b instanceof Projectile)) {
                         if (!(a instanceof Jumphole || b instanceof Jumphole)) {
                             if (!(a instanceof Explosion || b instanceof Explosion)) {
                                 Celestial dummyA = (Celestial) a;
                                 Celestial dummyB = (Celestial) b;
                                 //get velocity and mass
                                 double aVx = dummyA.getVx();
                                 double aVy = dummyA.getVy();
                                 double aM = dummyA.getMass();
                                 double bVx = dummyB.getVx();
                                 double bVy = dummyB.getVy();
                                 double bM = dummyB.getMass();
                                 //push them apart to avoid double counting and overlap
                                 dummyA.setX(dummyA.getX() - aVx * tpf * 2.0);
                                 dummyA.setY(dummyA.getY() - aVy * tpf * 2.0);
                                 dummyB.setX(dummyB.getX() - bVx * tpf * 2.0);
                                 dummyB.setY(dummyB.getY() - bVy * tpf * 2.0);
                                 //determine center of mass's velocity
                                 double cVx = (aVx * aM + bVx * bM) / (aM + bM);
                                 double cVy = (aVy * aM + bVy * bM) / (aM + bM);
                                 //reverse directions and de-reference frame
                                 double aVx2 = -aVx + cVx;
                                 double aVy2 = -aVy + cVy;
                                 double bVx2 = -bVx + cVx;
                                 double bVy2 = -bVy + cVy;
                                 //store
                                 dummyA.setVx(aVx2);
                                 dummyA.setVy(aVy2);
                                 dummyB.setVx(bVx2);
                                 dummyB.setVy(bVy2);
                             }
                         }
                     }
                 }
             }
         }
 
         /*
          * Ongoing event handling
          */
         private void handlePlayerEvents() {
             try {
                 if (!playerShip.isDocked()) {
                     if (allStopPressed) {
                         playerShip.decelerate();
                     }
                     if (firing) {
                         playerShip.fireActiveGuns(playerShip.getTarget());
                         playerShip.fireActiveTurrets(playerShip.getTarget());
                     }
                 }
             } catch (Exception e) {
                 System.out.println("Failure to pass player input event.");
             }
         }
 
         /*
          * special marker rendering
          */
         protected void renderTargetMarker() {
             //draw a marker around the player's target
             if (playerShip.getTarget() != null) {
                 if (!(playerShip.getTarget() instanceof Station)) {
                     int tx = (int) (playerShip.getTarget().getX() - dx);
                     int ty = (int) (playerShip.getTarget().getY() - dy);
                     int tw = playerShip.getTarget().getWidth();
                     int th = playerShip.getTarget().getHeight();
                     f.setColor(Color.YELLOW);
                     f.setStroke(new BasicStroke(3));
                     f.drawOval(tx, ty, tw, th);
                 }
             }
         }
 
         protected void renderIFFMarker(Ship ship) {
             if (!(ship instanceof Projectile) && ship != playerShip) {
                 if (!(ship instanceof Explosion)) {
                     if (!(ship instanceof Station)) {
                         //draw a marker to indicate standings
                         int tx = (int) (ship.getX() - dx);
                         int ty = (int) (ship.getY() - dy);
                         int tw = ship.getWidth();
                         int th = ship.getHeight();
                         int standing = ship.getStandingsToMe(playerShip);
                         if (!ship.getFaction().matches("Player")) {
                             if (standing >= 3) {
                                 f.setColor(Color.GREEN);
                             } else if (standing <= -3) {
                                 f.setColor(Color.RED);
                             } else {
                                 f.setColor(Color.GRAY);
                             }
                         } else {
                             f.setColor(Color.MAGENTA);
                         }
                         f.setStroke(new BasicStroke(2));
                         f.drawOval(tx, ty, tw, th);
                     }
                 }
             }
         }
     }
 
     /*
      * Player state modifiers
      */
     public void setActivePlayerShip(Ship ship) {
         playerShip = ship;
     }
 
     public Ship getActivePlayerShip() {
         return playerShip;
     }
 }
