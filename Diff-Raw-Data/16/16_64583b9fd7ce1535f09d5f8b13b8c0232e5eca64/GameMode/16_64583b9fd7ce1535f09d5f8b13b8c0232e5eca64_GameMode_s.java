 /*
  * Copyright (c) 2010-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.modes;
 
 import game.Campaign;
 import game.Level;
 import game.entities.Players;
 import game.time.GameTime;
 
 import java.io.IOException;
 
 import loader.data.DataException;
 import loader.data.json.CampaignData;
 import loader.parser.ParserException;
 import main.Locator;
 import math.Rectangle;
 
 import org.newdawn.slick.Graphics;
 
 import states.StateManager;
 import ui.hud.ShopUI;
 import ui.hud.UI;
 
 public class GameMode implements IMode {
   private enum ACTION { NEXT_LEVEL, MAIN_MENU, CREDITS }
   private ACTION nextAction;
 
   private final UI ui;
 
   private final Campaign campaign;
   private Level level;
 
   private final Players players;
 
   /**
    * Describes the entire area availible to the game (non-ui stuff).
    * Relative to the upper left corner of the game window.
    */
   private final Rectangle arenaRect;
 
   /**
    * How much time have elapsed since we started.
    */
   private float elapsed;
 
   public GameMode(CampaignData data, int width, int height)
       throws ParserException, DataException, IOException {
     if (data.levels.isEmpty()) {
      throw new IllegalArgumentException("No levels in campaing");
     }
 
     ui = new UI();
     Locator.registerUI(ui);
 
     elapsed   = 0;
     arenaRect = new Rectangle(0, ShopUI.HEIGHT, width, height - ShopUI.HEIGHT * 2);
     campaign  = new Campaign(data);
     players   = new Players(1); // TODO: Coop
   }
 
   @Override
   public void start(StateManager stateManager) {
     try {
       campaign.nextLevel();
 
       level = new Level(
         this,
         campaign.getCurrentLevel(),
         players,
         arenaRect.getWidth(),
         arenaRect.getHeight()
       );
    } catch (ParserException ex) {
      stateManager.handleException(ex);
    } catch (IOException ex) {
      stateManager.handleException(ex);
    } catch (DataException ex) {
       stateManager.handleException(ex);
     }
   }
 
   /**
    * The update iteration of the game loop.
    * Updates entities, switches levels, etc.
    *
    * @param dt Approximate length of the current frame in seconds.
    */
   @Override
   public void update(StateManager stateManager, float dt) {
     elapsed += dt;
     level.update(new GameTime(dt, elapsed));
     ui.update();
 
     if (nextAction == ACTION.NEXT_LEVEL) {
       try {
         nextLevel();
      } catch (DataException ex) {
        stateManager.handleException(ex);
      } catch (ParserException ex) {
        stateManager.handleException(ex);
      } catch (IOException ex) {
         stateManager.handleException(ex);
       }
       nextAction = null;
     } else if (nextAction == ACTION.CREDITS) {
       stateManager.enterCredits();
       nextAction = null;
     } else if (nextAction == ACTION.MAIN_MENU) {
       stateManager.enterMainMenu();
       nextAction = null;
     }
   }
 
   @Override
   public void render(Graphics g) {
     g.pushTransform();
     g.translate(arenaRect.getX1(), arenaRect.getY1());
 
     level.render(g);
     ui.renderDynamics(g);
 
     g.popTransform();
 
     ui.renderStatics(g);
   }
 
   private void nextLevel()
       throws DataException, ParserException, IOException {
     if (campaign.hasMoreLevels()) {
       campaign.nextLevel();
       level = new Level(
         this,
         campaign.getCurrentLevel(),
         players,
         arenaRect.getWidth(),
         arenaRect.getHeight()
       );
     } else {
       goCredits();
     }
   }
 
   public void goMainMenu() {
     nextAction = ACTION.MAIN_MENU;
   }
 
   public void goCredits() {
     nextAction = ACTION.CREDITS;
   }
 
   public void goNextLevel() {
     nextAction = ACTION.NEXT_LEVEL;
   }
 }
