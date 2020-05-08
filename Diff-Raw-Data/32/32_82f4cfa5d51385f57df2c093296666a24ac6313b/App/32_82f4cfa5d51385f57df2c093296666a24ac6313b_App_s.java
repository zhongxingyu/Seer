 package ru.madzi.lab.game;
 
 import java.awt.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import javax.swing.ImageIcon;
 import ru.madzi.lab.game.stages.GameStage;
 import ru.madzi.lab.game.stages.IntroStage;
 import ru.madzi.lab.game.stages.MenuStage;
 import ru.madzi.lab.game.stages.RecordsStage;
 import ru.madzi.lab.game.stages.SetupStage;
 import ru.madzi.lab.game.stages.StatisticsStage;
 import ru.madzi.lab.util.Core;
 import ru.madzi.lab.util.input.InputManager;
 import ru.madzi.lab.util.stages.StageManager;
 
 /**
  * Точка входа в программу.
  */
 public class App extends Core {
 
     private static final Logger _LOG = Logger.getLogger(App.class.getName());
 
     private InputManager inputManager;
 
     private StageManager stageManager;
 
     private ResourceManager resourceManager;
 
     /**
      * Главная функция.
      *
      * @param args аргументы командной строки
      */
     public static void main( String[] args ) {
         _LOG.info("Start application");
         new App().run();
     }
 
     @Override
     public void init() {
         _LOG.setLevel(Level.INFO);
         _LOG.info("Init application");
         isFullscreen = false;
         displayMode = new DisplayMode(800, 600, DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN);
         super.init();
 
         _LOG.info("Init input manager");
         inputManager = new InputManager((Component) screenManager.getWindow());
         _LOG.info("Init resource manager");
         resourceManager = new ResourceManager(screenManager.getWindow().getGraphicsConfiguration());
         _LOG.info("Init stage manager");
         stageManager = new StageManager(inputManager, resourceManager.loadImage("/images/6backtest.bmp"));
 
         _LOG.info("Intro stage init...");
         stageManager.addStage(new IntroStage());
 
         _LOG.info("Menu stage init...");
         stageManager.addStage(new MenuStage());
 
         _LOG.info("Setup stage init...");
         stageManager.addStage(new SetupStage());
 
         _LOG.info("Game stage init...");
         stageManager.addStage(new GameStage());
 
         _LOG.info("Records stage init...");
         stageManager.addStage(new RecordsStage());
 
         _LOG.info("Statistics stage init...");
         stageManager.addStage(new StatisticsStage());
 
         stageManager.setStage("_intro");
     }
 
     @Override
     public void stop() {
         _LOG.info("Stop application");
         super.stop();
     }
 
     /**
      * Обновление состояния сцены.
      *
      * @param elapsedTime время, прошедшее с момента прошлого обновления
      */
     @Override
     public void update(long elapsedTime) {
         if (stageManager.isDone()) {
             stop();
         } else {
             stageManager.update(elapsedTime);
         }
     }
 
     /**
      * Отрисовка сцены на холсте.
      *
      * @param g холст
      */
     @Override
     public void draw(Graphics2D g) {
         stageManager.draw(g);
     }
 
 }
