 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package ptg;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import loader.Cache;
 import loader.CacheException;
 import math.Rectangle;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import ui.StateMenu;
 import credits.StateCredits;
 
 public class App extends StateBasedGame {
   public static final String NAME    = "PolhemTheGame";
   public static final String VERSION = "1.0";
 
   public static final int       MAINMENU   = 0;
   public static final int       CREDITS    = 1;
   public static final int       GAMEPLAY   = 2;
 
   public static final int       WIDTH      = 1024;
   public static final int       HEIGHT     = 768;
   public static final boolean   FULLSCREEN = false;
   public static final int       MAX_FPS    = 100;
   public static final Rectangle RECT       = new Rectangle(0, 0, WIDTH, HEIGHT);
 
   public App(final boolean skipMenu) {
     super(NAME + " - " + VERSION);
 
     addState(new StateMenu(MAINMENU));
     addState(new StateGame(GAMEPLAY));
     addState(new StateCredits(CREDITS));
 
     if (skipMenu) {
       enterState(GAMEPLAY);
     } else {
       enterState(MAINMENU);
     }
   }
 
   public static void main(final String[] args) {
     try {
       Locator.registerCache(new Cache(new Enviroment().appDir));
 
       boolean skipMenu = false;
       for (String s : args) {
         if (s.equals("-s")) {
           skipMenu = true;
         }
       }
 
       final AppGameContainer app = new AppGameContainer(new App(skipMenu));
 
       app.setDisplayMode(WIDTH, HEIGHT, FULLSCREEN);
       app.start();
     } catch (final FileNotFoundException e) {
       e.printStackTrace();
     } catch (final SlickException e) {
       e.printStackTrace();
     } finally {
       try {
         Locator.getCache().close();
       } catch (final CacheException e) {
         e.printStackTrace();
       }
     }
   }
 
   @Override
   public void initStatesList(final GameContainer gameContainer)
     throws SlickException {
     getState(MAINMENU).init(gameContainer, this);
     getState(GAMEPLAY).init(gameContainer, this);
     getState(CREDITS).init(gameContainer, this);
   }
 }
