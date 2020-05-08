 package geospace.states;
 
 import geospace.GeoSpace;
 import geospace.audio.AudioManager;
 import geospace.render.EffectManager;
 import geospace.control.agent.AbstractAgent;
 import geospace.control.CurrentGameState;
 import geospace.control.agent.service.ServiceAgentManager;
 import geospace.entity.Point;
 import geospace.entity.Ship;
 import geospace.render.elements.Stage;
 import geospace.render.DrawManager;
 import geospace.entity.EntityManager;
 import geospace.entity.EntityModel.EntityState;
 import geospace.gui.GUIManager;
 import geospace.render.elements.EnergyBar;
 import geospace.render.elements.TimerClock;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class PlayingState extends BasicGameState {
 
     private int stateId;
     private Stage gameStage;
     public static final int TITLE_HEIGHT = 50;
     public static final int BORDER_SIZE = 10;
     private List<Player> players;
     private List<Thread> playerThreads;
     private final CurrentGameState currentGameState;
     private GameMode currentGameMode;
     private TimerClock timerClock;
     private Thread timerThread;
 
     public enum GameMode {
 
         DUEL,
         BATTLE_ROYALE
     }
 
     public PlayingState(int stateId) {
         this.stateId = stateId;
 
         this.players = new LinkedList<Player>();
         this.playerThreads = new LinkedList<Thread>();
         this.currentGameState = new CurrentGameState();
 
         this.currentGameMode = GameMode.DUEL;
     }
 
     public void setPlayerControllers(List<AbstractAgent> agents) {
         this.players.clear();
         for (AbstractAgent agent : agents) {
             players.add(new Player(agent, this.currentGameState));
         }
     }
 
     public void setCurrentGameMode(GameMode currentGameMode) {
         this.currentGameMode = currentGameMode;
     }
 
     @Override
     public int getID() {
         return this.stateId;
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
     }
 
     @Override
     public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
         super.enter(gc, sbg);
 
         this.gameStage = new Stage(new Point(BORDER_SIZE, BORDER_SIZE + TITLE_HEIGHT), gc.getWidth() - (2 * BORDER_SIZE), gc.getHeight() - (2 * BORDER_SIZE) - TITLE_HEIGHT);
 
         AudioManager.getInstance().playRandomMusic(false);
 
         for (Player player : this.players) {
            player.setShip(
                    this.spawnShip(
                    new Ship(player.getAgent(), 0, 0, 0, player.getPlayerColor())));
 
             Thread playerThread = new Thread(player);
             playerThreads.add(playerThread);
             playerThread.start();
         }
 
         initializeGameMode();
 
         int timelimit = 180;
         try {
             Properties props = new Properties();
             props.load(new FileInputStream("./resources/geospace.properties"));
             timelimit = Integer.parseInt(props.getProperty("timelimit"));
         }
         catch (IOException ex) {
             Logger.getLogger(PlayingState.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         this.timerClock = new TimerClock(timelimit, this.gameStage.getStageWidth() / 2, (TITLE_HEIGHT / 2) + BORDER_SIZE);
         this.timerThread = new Thread(this.timerClock);
         this.timerThread.start();
     }
 
     @Override
     public void leave(GameContainer container, StateBasedGame sbg) throws SlickException {
         for (Thread playerThread : this.playerThreads) {
             playerThread.interrupt();
         }
         this.playerThreads.clear();
         this.players.clear();
 
         StateJanitor.cleanupState();
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
         this.gameStage.update(gc, delta);
 
         this.gameStage.setStageClipping(gc.getGraphics(), true);
         EffectManager.getInstance().update(gc, delta);
         this.gameStage.setStageClipping(gc.getGraphics(), false);
 
         this.updateGameMode(sbg);
     }
 
     @Override
     public void render(GameContainer gc, StateBasedGame sbg, Graphics graphics) throws SlickException {
         DrawManager.getInstance().renderImages();
 
         this.renderGameMode(graphics);
         this.timerClock.render(graphics);
 
         this.gameStage.setStageClipping(graphics, true);
         this.gameStage.render(graphics);
         if (!DrawManager.getInstance().isEnableWireframe()) {
             EffectManager.getInstance().setOffset(this.gameStage.getStageOffset());
             EffectManager.getInstance().render(graphics);
         }
         this.gameStage.setStageClipping(graphics, false);
         graphics.resetTransform();
 
         GUIManager.getInstance().render();
 
         synchronized (this.currentGameState) {
             this.currentGameState.updateState(this.timerClock.getTimeLeft());
             this.currentGameState.notifyAll();
         }
 
         ServiceAgentManager.getInstance().setLastGameState(currentGameState);
     }
 
     private Ship spawnShip(Ship ship) {
         Random randomGenerator = new Random();
 
         ship.resetShip(
                 100 + (randomGenerator.nextFloat() * (this.gameStage.getStageWidth() - 200)),
                 100 + (randomGenerator.nextFloat() * (this.gameStage.getStageHeight() - 200)),
                 randomGenerator.nextFloat() * (float)(2 * Math.PI));
 
         EntityManager.getInstance().addEntity(ship);
         EffectManager.getInstance().renderSpawn(ship.getCenter().getX(), ship.getCenter().getY());
 
         return ship;
     }
 
     private void initializeGameMode() {
         switch (this.currentGameMode) {
             case DUEL:
                 this.players.get(0).setEnergyBar(new EnergyBar(this.players.get(0).getShip(), new Point(BORDER_SIZE, BORDER_SIZE), (this.gameStage.getStageWidth() / 2) - 100, TITLE_HEIGHT - BORDER_SIZE));
                 this.players.get(1).setEnergyBar(new EnergyBar(this.players.get(1).getShip(), new Point((this.gameStage.getStageWidth() / 2) + 100 + BORDER_SIZE, BORDER_SIZE), (this.gameStage.getStageWidth() / 2) - 100, TITLE_HEIGHT - BORDER_SIZE));
                 break;
         }
 
     }
 
     private void updateGameMode(StateBasedGame sbg) {
         switch (this.currentGameMode) {
             case DUEL:
                 updatePlayer(this.players.get(0), this.players.get(1));
                 updatePlayer(this.players.get(1), this.players.get(0));
                 
                 if (this.timerThread.getState() == Thread.State.TERMINATED) {
                     if(this.players.get(0).getScore() != this.players.get(1).getScore()) {
                         sbg.enterState(GeoSpace.MENU_STATE);
                     }
                     else {
                         this.gameStage.shrinkStage(0.25f);
                     }
                 }
                 break;
         }
     }
 
     private void updatePlayer(Player player, Player enemy) {
         if (player.getShip().getState() == EntityState.DEAD) {
             enemy.updateScore();
             this.spawnShip(player.getShip());
         }
     }
 
     private void renderGameMode(Graphics graphics) {
         switch (this.currentGameMode) {
             case DUEL:
                 for (Player player : this.players) {
                     DrawManager.getInstance().renderPlayerInfo(graphics, player);
                 }
                 
                 break;
         }
     }
 }
