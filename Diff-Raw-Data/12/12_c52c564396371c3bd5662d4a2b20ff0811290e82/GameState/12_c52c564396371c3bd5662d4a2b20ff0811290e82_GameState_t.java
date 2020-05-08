 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package states;
 
 import game.CacheTool;
 import game.Campaign;
 import game.entities.Entity;
 import game.entities.Players;
 import game.factories.EntityFactory;
 import game.factories.WorldFactory;
 import game.types.GameTime;
 import game.types.Player;
 import game.world.World;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import loader.data.json.CampaignData;
 import loader.parser.ParserException;
 import main.Launcher;
 import main.Locator;
 import math.Rectangle;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import ui.hud.BlackBox;
 import ui.hud.PlayerUI;
 import ui.hud.UI;
 import util.Node;
 
 public class GameState implements IState {
   private enum ACTION { NEXT_LEVEL, MAIN_MENU, CREDITS }
   private ACTION nextAction;
 
   private final UI ui;
 
   private final Campaign campaign;
   private final Image background, statics;
   private World world;
 
   private final WorldFactory worldFactory;
 
   /**
    * Describes the entire area available to the game (non-UI stuff).
    * Relative to the upper left corner of the game window.
    */
   private final Rectangle arenaRect;
 
   /**
    * How much time have elapsed since we started.
    */
   private long elapsed;
 
   public GameState(StateManager stateManager, CampaignData data, boolean twoPlayer,
                    int windowWidth, int windowHeight)
       throws ParserException, IOException, SlickException {
     if (data.levels.isEmpty()) {
       throw new IllegalArgumentException("No levels in campaign");
     }
 
     ui = new UI(windowWidth, windowHeight);
     Locator.registerUI(ui);
     ui.addStatic(new BlackBox(0, 0, Launcher.WIDTH, PlayerUI.HEIGHT));
     ui.addStatic(new BlackBox(0, Launcher.HEIGHT - PlayerUI.HEIGHT, Launcher.WIDTH, PlayerUI.HEIGHT));
 
     int arenaWidth  = windowWidth;
     int arenaHeight = windowHeight - PlayerUI.HEIGHT * 2;
 
     int left   = data.constraints[0];
     int top    = data.constraints[1];
     int bottom = data.constraints[2];
     int right  = data.constraints[3];
 
     campaign   = new Campaign(data);
     background = CacheTool.getImage(Locator.getCache(), data.background);
     statics    = new Image(arenaWidth, arenaHeight);
 
     arenaRect = new Rectangle(0, PlayerUI.HEIGHT, arenaWidth, arenaHeight);
     Rectangle worldRect = new Rectangle(
       left, top,
       arenaWidth - left - right,
       arenaHeight - top - bottom
     );
 
     elapsed = 0;
 
     EntityFactory entityFactory = new EntityFactory(worldRect,
                                                     statics.getGraphics());
 
     ArrayList<Entity> players = new ArrayList<>();
     Player player1 = entityFactory.makePlayer("blue", Locator.getConfig().player1);
     ui.addDynamic(player1.infoBar);
     ui.addStatic(new PlayerUI(0, 0, Launcher.WIDTH, player1.shopUI, player1.inventory));
     players.add(player1.entity);
 
     if (twoPlayer) {
       Player player2 = entityFactory.makePlayer("red", Locator.getConfig().player2);
       ui.addDynamic(player2.infoBar);
       ui.addStatic(new PlayerUI(0, 0, Launcher.WIDTH, player1.shopUI, player1.inventory));
       players.add(player2.entity);
     }
 
     Players.reposition(players, worldRect);
 
     worldFactory = new WorldFactory(this, stateManager, entityFactory,
                                     worldRect, players);
   }
 
   @Override
   public void start(StateManager stateManager) {
     try {
       nextLevel();
     } catch (ParserException | IOException ex) {
       stateManager.handleException(ex);
     }
   }
 
   @Override
   public void end(StateManager stateManager) {
     // Do nothing
   }
 
   /**
    * The update iteration of the game loop.
    * Updates entities, switches levels, etc.
    *
    * @param dt Approximate length of the current frame in milliseconds
    */
   @Override
   public void update(StateManager stateManager, int dt) {
     elapsed += dt;
     world.update(new GameTime(dt / 1000.0f, dt, elapsed));
     ui.update();
 
     if (nextAction == ACTION.NEXT_LEVEL) {
      nextAction = null;

       try {
         nextLevel();
       } catch (ParserException | IOException ex) {
         stateManager.handleException(ex);
       }
     } else if (nextAction == ACTION.CREDITS) {
       nextAction = null;
      stateManager.enterCredits();
     } else if (nextAction == ACTION.MAIN_MENU || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
       nextAction = null;
      stateManager.enterMainMenu();
     }
   }
 
   @Override
   public void render(Graphics g) {
     g.pushTransform();
     g.translate(arenaRect.getX1(), arenaRect.getY1());
 
     g.drawImage(background, 0, 0);
     g.drawImage(statics, 0, 0);
     world.render(g);
     ui.renderDynamics(g);
 
     g.popTransform();
 
     ui.renderStatics(g);
   }
 
   private void nextLevel() throws ParserException, IOException {
     if (campaign.hasMoreLevels()) {
       campaign.nextLevel();
 
       world = worldFactory.makeLevel(campaign.getCurrentLevel());
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
 
   @Override
   public String debugString() {
     return "GameState, time " + String.valueOf(elapsed);
   }
 
   @Override
   public Node<String> debugTree() {
     Node<String> parent = new Node<>(debugString());
     parent.nodes.add(world.debugTree());
 
     return parent;
   }
 }
