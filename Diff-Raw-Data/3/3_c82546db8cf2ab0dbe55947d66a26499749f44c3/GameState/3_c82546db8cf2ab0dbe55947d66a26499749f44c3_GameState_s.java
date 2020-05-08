 package state;
 
 import helper.LevelHelper;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import other.LevelController;
 import other.Translator;
 import app.Game;
 import entity.Level;
 import entity.Menu;
 import entity.MenuItem;
 import entity.MessageBox;
 
 public class GameState extends BasicGameState {
 
     private int stateId;
     private Level level = null;
     private boolean showMenu = false;
     private boolean showGameOverMenu = false;
     private Menu menu = null;
     private Menu gameOverMenu = null;
     private Translator translator;
     private LevelController levelController;
     private MessageBox messageBox;
     private boolean wasFinished = false;
 
     public GameState(int stateId) {
         this.stateId = stateId;
         this.translator = Translator.getInstance();
         this.levelController = LevelController.getInstance();
     }
 
     @Override
     public void init(final GameContainer container, final StateBasedGame game)
             throws SlickException {
         this.messageBox = new MessageBox(container);
         this.messageBox.setBackgroundColor(Color.lightGray);
         MenuItem continueItem = new MenuItem(this.translator.translate("Continue"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent arg0) {
                         GameState.this.showMenu = false;
                         GameState.this.showGameOverMenu = false;
                     }
                 });
         MenuItem repeatLevel = new MenuItem(this.translator.translate("Repeat level"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.initLevel(container, game);
                         GameState.this.showMenu = false;
                         GameState.this.showGameOverMenu = false;
                     }
                 });
         MenuItem subMenu = new MenuItem(this.translator.translate("Sub menu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.showMenu = false;
                         GameState.this.showGameOverMenu = false;
                         game.enterState(Game.MENU_FOR_GAME_STATE);
                     }
                 });
         MenuItem mainMenu = new MenuItem(this.translator.translate("Main menu"),
                 new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         GameState.this.showMenu = false;
                         GameState.this.showGameOverMenu = false;
                         game.enterState(Game.MENU_STATE);
                     }
                 });
         List<MenuItem> menuItems = new ArrayList<MenuItem>();
         menuItems.add(continueItem);
         menuItems.add(repeatLevel);
         menuItems.add(subMenu);
         menuItems.add(mainMenu);
         for (MenuItem item : menuItems) {
             item.setMargin(30);
         }
         this.menu = new Menu(menuItems, container);
         this.menu.setBackgroundColor(Color.lightGray);
 
         List<MenuItem> gameOverMenuItems = new ArrayList<MenuItem>();
         gameOverMenuItems.add(repeatLevel);
         gameOverMenuItems.add(subMenu);
         gameOverMenuItems.add(mainMenu);
         for (MenuItem item : gameOverMenuItems) {
             item.setMargin(30);
         }
         this.gameOverMenu = new Menu(gameOverMenuItems, container);
         this.gameOverMenu.setBackgroundColor(Color.lightGray);
 
         this.initLevel(container, game);
     }
 
     private void initLevel(GameContainer container, final StateBasedGame game) {
         try {
             this.wasFinished = false;
             this.level = this.levelController.getCurrentLevel();
             int itemSize = this.level.getOriginalImageSize();
             float scale = LevelHelper.computeScale(container, this.level.getOriginalImageSize(),
                     new Dimension(this.level.getWidth(), this.level.getHeight()));
             this.level.setScale(scale);
             int width = this.level.getWidth() * (int) (itemSize * scale);
             int height = this.level.getHeight() * (int) (itemSize * scale);
             this.level.setMarginLeft((container.getWidth() - width) / 2);
             this.level.setMarginTop((container.getHeight() - height) / 2);
             if (!this.level.isValid()) {
                 this.messageBox.showConfirm(this.translator
                         .translate("Level is not valid. Do you wanna edit this level?"),
                         new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent arg0) {
                                 game.enterState(Game.EDITOR_STATE);
                             }
                         }, new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_FOR_GAME_STATE);
                             }
                         });
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g)
             throws SlickException {
         this.level.render(container, game, g);
         if (this.showMenu) {
             this.menu.render(container, game, g);
         }
         if (this.showGameOverMenu) {
             this.gameOverMenu.render(container, game, g);
         }
         this.messageBox.render(container, game, g);
     }
 
     @Override
     public void update(final GameContainer container, final StateBasedGame game, int delta)
             throws SlickException {
         Input input = container.getInput();
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             this.showMenu = true;
         }
         if (this.level.isOver()) {
             this.showGameOverMenu = true;
         }
         if (this.level.isFinished()) {
             if (this.levelController.nextLevelExist()) {
                 this.messageBox.showConfirm(this.translator
                         .translate("Level was finished. Do you wanna continue to next level?"),
                         new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 GameState.this.levelController.loadNextLevel();
                                 GameState.this.initLevel(container, game);
                             }
                         }, new ActionListener() {
 
                             @Override
                             public void actionPerformed(ActionEvent e) {
                                 game.enterState(Game.MENU_STATE);
                             }
                         });
             } else {
                 this.messageBox
                         .showConfirm(
                                 this.translator
                                         .translate("Congratulation!!! Package was finished. Do you wanna continue?"),
                                 new ActionListener() {
 
                                     @Override
                                     public void actionPerformed(ActionEvent e) {
                                         game.enterState(Game.MENU_FOR_GAME_STATE);
                                     }
                                 }, new ActionListener() {
 
                                     @Override
                                     public void actionPerformed(ActionEvent e) {
                                         game.enterState(Game.MENU_STATE);
                                     }
                                 });
             }
             if (!this.wasFinished) {
                 this.wasFinished = true;
                 this.levelController.updateProgress();
             }
         }
         if (this.showMenu) {
             this.menu.update(container, game, delta);
         } else if (this.showGameOverMenu) {
             this.gameOverMenu.update(container, game, delta);
         } else {
             this.level.update(container, game, delta);
         }
         this.messageBox.update(container, game, delta);
     }
 
     @Override
     public int getID() {
         return this.stateId;
     }
 
 }
