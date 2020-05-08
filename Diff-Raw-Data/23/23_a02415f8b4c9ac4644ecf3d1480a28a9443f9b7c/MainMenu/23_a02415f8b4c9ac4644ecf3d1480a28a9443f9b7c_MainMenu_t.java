 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package thirteenducks.cor.mainmenu;
 
 import thirteenducks.cor.mainmenu.components.*;
 import thirteenducks.cor.mainmenu.components.AnimatedImage;
 import java.util.HashMap;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import thirteenducks.cor.game.server.ServerCore;
 
 /**
  * Grafisches Hauptmenü
  *
  * @author michael
  */
 public class MainMenu extends Container {
 
     /**
      * Grafikdingens
      */
     MainMenuGraphics graphics;
     /**
      * Die einzelnen Menüs
      */
     HashMap<String, Container> menus;
     /**
      * X-Bildschirmauflösung
      */
     private int resX;
     /**
      * Y-Bildschirmauflösung
      */
     private int resY;
 
     /**
      * Konstruktor
      */
     public MainMenu(int resX, int resY) {
         super(null, 0, 0, (double) resX, (double) resY);
 
         super.setMainMenuReference(this);
 
         this.resX = resX;
         this.resY = resY;
 
         menus = new HashMap<String, Container>();
 
         /**
          * Komponenten initialisieren:
          */
         initComponents();
     }
 
     /**
      * Setzt die Referenz auf die Grafikkomponente
      * Wird vom Konstruktor von MainMenuGraphics aufgerufen
      *
      * @param g - Referenz auf MAinMenuGraphics
      */
     public void setMainMenuGraphics(MainMenuGraphics g) {
         graphics = g;
 
     }
 
     @Override
     /**
      * Render-Funktion
      * rendert alle SubKomponenten
      */
     public void render(Graphics g) {
         for (Component c : super.getComponents()) {
             c.render(g);
         }
     }
 
     @Override
     public void init(GameContainer c) {
         for (Component comp : super.getComponents()) {
             comp.init(c);
         }
     }
 
     @Override
     /**
      * Wird bei Mausklicks aufgerufen
      */
     public void mouseClicked(int button, int x, int y, int clickCount) {
         for (Component c : super.getComponents()) {
             c.mouseClickedAnywhere(button, x, y, clickCount);
         }
     }
 
     @Override
     /**
      * Wird bei Tastendruck aufgerufen
      */
     public void keyPressed(int key, char c) {
         for (Component comp : super.getComponents()) {
             comp.keyPressed(key, c);
         }
 
     }
 
     /**
      * Initialisiert die Komponenten des Hauptmenüs
      */
     private void initComponents() {
 
 
         /**********************************************************************
          * Hintergrund:
          *********************************************************************/
         // Animierter Hintergrund:
        super.addComponent(new AnimatedImage(this, "img/mainmenu/test.png"));
 
         // Rahmen:
         // aus irgendeinem Grund funktioniert nur 99,999% statt 100%....
         super.addComponent(new Frame(this, 0, 0, 99.9999f, 99.9999f));
 
         // Mauskoordiaten anzeigen:
         super.addComponent(new CoordinateView(this));
 
         // Koordinatenanzeige:
         super.addComponent(new CoordinateView(this));
 
 
         /**********************************************************************
          * Menüs:
          *********************************************************************/
         // Hauptmenü:
         Container startScreen = new StartScreen(this);
         menus.put("startscreen", startScreen);
         super.addComponent(startScreen);
         startScreen.fadeIn();
 
         // StartServerscreen
         Container startServerScreen = new StartServerScreen(this);
         menus.put("startserverscreen", startServerScreen);
         super.addComponent(startServerScreen);
         startServerScreen.fadeOut();
 
         // RandomMapBuilder
         Container randomMapBuilderScreen = new RandomMapBuilderScreen(this);
         menus.put("randommapbuilderscreen", randomMapBuilderScreen);
         super.addComponent(randomMapBuilderScreen);
         randomMapBuilderScreen.fadeOut();
 
         // Mehrspieler:
         Container MultiplayerScreen = new MultiplayerScreen(this);
         menus.put("multiplayerscreen", MultiplayerScreen);
         super.addComponent(MultiplayerScreen);
         MultiplayerScreen.fadeOut();
 
         // Lobby
         LobbyScreen lobbyScreen;
         lobbyScreen = new LobbyScreen(this);
         menus.put("lobbyscreen", lobbyScreen);
         super.addComponent(lobbyScreen);
         lobbyScreen.fadeOut();
     }
 
     /**
      * Gibt ein bestimmtes Menü zurück
      *
      * @param name - Name des Menüs
      * @return     - Das Menü 
      */
     public Container getMenu(String name) {
         return menus.get(name);
     }
 
     /**
      * Getter für X-Auflösung
      * @return X-Auflösung
      */
     public int getResX() {
         return resX;
     }
 
     /**
      * Getter für Y-Auflösung
      * @return Y-Auflösung
      */
     public int getResY() {
         return resY;
     }
 
     /**
      * Startet einen Server
      *
      * @param debug - soll der Server im Debug-Modus gestartet werden?
      * @param map   - der Name der Map, z.B. "/map/main/Randommap.map"
      */
     public void startServer(final boolean debug, final String map) {
         System.out.println("Starting Server with debug=" + debug + " and map=" + map);
 
         Thread serverThread = new Thread(new Runnable() {
 
             public void run() {
                 new ServerCore(debug, map);
             }
         });
 
         serverThread.setName("serverThread");
 
         serverThread.start();
     }
 
     /**
      * Tritt einer Partie bei
      */
     public void joinServer(String server) {
         {
             System.out.println("Joining Server...");
             // @TODO: ClientCore.joinServer() rufen, bei erfolg lobby anzeigen
         }
     }
 }
