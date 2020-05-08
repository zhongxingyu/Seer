 package no.eirikb.bomberman.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import no.eirikb.bomberman.client.game.Bomb;
 import no.eirikb.bomberman.client.game.BoomBrick;
 import no.eirikb.bomberman.client.game.Explosion;
 import no.eirikb.bomberman.client.game.Game;
 import no.eirikb.bomberman.client.game.GameListener;
 import no.eirikb.bomberman.client.game.handler.GameHandler;
 import no.eirikb.bomberman.client.game.Player;
 import no.eirikb.bomberman.client.game.Settings;
 import no.eirikb.bomberman.client.game.Sprite;
 import no.eirikb.bomberman.client.game.Way;
 import no.eirikb.bomberman.client.game.logic.BombBuilder;
 import no.eirikb.bomberman.client.loading.LoadingPanel;
 import no.eirikb.bomberman.client.game.logic.PlayerBuilder;
 import no.eirikb.bomberman.client.loading.LoadListener;
 
 /**
  * This class is a MESS!
  * Started out OK, ended up like this... What heppened
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Bomberman implements EntryPoint {
 
     private LoadingPanel loadingPanel;
     private Player player;
     private KeyHack keyHack;
     private SettingsPanel settingsPanel;
     private GamePanel gamePanel;
     private Game game;
     private VerticalPanel panel;
     private CheckBox useKeyHack;
     private TextBox textBox;
 
     public void onModuleLoad() {
         RootPanel.get().add(loadingPanel = new LoadingPanel(new LoadListener() {
 
             public void complete(Sprite[][] sprites, int imgSize) {
                 init(sprites, imgSize);
             }
         }));
         loadingPanel.initLoad();
     }
 
     public void init(Sprite[][] sprites, int imgSize) {
         RootPanel.get().remove(loadingPanel);
 
         panel = new VerticalPanel();
         RootPanel.get().add(panel);
 
         panel.add(new PushButton("Show/Hide settings", new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 settingsPanel.setVisible(!settingsPanel.isVisible());
             }
         }));
 
         settingsPanel = new SettingsPanel(new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 settingsPanel.upateSettings();
                 RootPanel.get().remove(panel);
                 RootPanel.get().add(loadingPanel);
                 loadingPanel.afterLoad();
             }
         });
         settingsPanel.setVisible(false);
         panel.add(settingsPanel);
 
         textBox = new TextBox();
 
         textBox.addKeyDownHandler(new KeyDownHandler() {
 
             public void onKeyDown(KeyDownEvent event) {
                 if (KeyDownEvent.isArrow(event.getNativeKeyCode())) {
                     if (keyHack != null) {
                         keyHack.arrowKeyDown(event);
                     } else {
                         arrowKeyDown(event);
                     }
                 } else if (event.getNativeKeyCode() == 32) {
                     Bomb bomb = BombBuilder.createBomb(game.getSprites(), player);
                     if (bomb != null) {
                         game.addBomb(bomb);
                     }
                    keyHack.setAnotherKeyPresses(true);
                 }
             }
         });
 
         textBox.addKeyUpHandler(new KeyUpHandler() {
 
             public void onKeyUp(KeyUpEvent event) {
                 textBox.setText("");
                 if (KeyUpEvent.isArrow(event.getNativeKeyCode())) {
                     if (keyHack != null) {
                         keyHack.arrowKeyUp(event);
                     } else {
                         arrowKeyUp();
                     }
                 }
             }
         });
 
         panel.add(new Label("Put marker inside textbox and use arrows to move, press space for bomb:"));
         panel.add(textBox);
         textBox.setFocus(true);
         startGame(sprites, imgSize);
     }
 
     private void startGame(Sprite[][] sprites, int imgSize) {
         Settings settings = Settings.getInstance();
         game = new Game(sprites, settings.getMapWidth(), settings.getMapHeight(), imgSize);
         player = PlayerBuilder.createPlayer(settings, "eirikb");
         game.addPlayer(player);
         gamePanel = new GamePanel(imgSize, settings.getMapWidth(), settings.getMapHeight());
         gamePanel.getElement().getStyle().setBackgroundColor("#abcdef");
         final GameHandler gameHandler = new GameHandler(game, gamePanel);
 
         final Bomberman bomberman = this;
         useKeyHack = new CheckBox("Use KeyHack");
         useKeyHack.setValue(true);
         useKeyHack.addClickHandler(new ClickHandler() {
 
             public void onClick(ClickEvent event) {
                 if (useKeyHack.getValue()) {
                     keyHack = new KeyHack(bomberman);
                     gameHandler.setKeyHackCallback(keyHack);
                 } else {
                     gameHandler.setKeyHackCallback(null);
                     keyHack = null;
                 }
                 textBox.setFocus(true);
             }
         });
         panel.add(useKeyHack);
         if (useKeyHack.getValue()) {
             keyHack = new KeyHack(this);
             gameHandler.setKeyHackCallback(keyHack);
         }
         BombAmountPanel bombAmountPanel = new BombAmountPanel(player);
         game.addGameListener(bombAmountPanel);
         panel.add(bombAmountPanel);
         gameHandler.start();
         panel.add(gamePanel);
         killCheck();
     }
 
     public void arrowKeyDown(KeyDownEvent event) {
         if (event.isLeftArrow()) {
             player.setWay(Way.LEFT);
         } else if (event.isUpArrow()) {
             player.setWay(Way.UP);
         } else if (event.isRightArrow()) {
             player.setWay(Way.RIGHT);
         } else if (event.isDownArrow()) {
             player.setWay(Way.DOWN);
         }
     }
 
     public void arrowKeyUp() {
         player.setWay(Way.NONE);
     }
 
     private void killCheck() {
         game.addGameListener(new GameListener() {
 
             public void addPlayer(Player player) {
             }
 
             public void removePlayer(final Player player) {
                 gamePanel.remove(player.getImage());
                 final DialogBox dialogBox = new DialogBox();
                 dialogBox.setText("Oh noes!");
                 VerticalPanel v = new VerticalPanel();
                 v.add(new Image("img/ohnoes.jpg"));
                 v.add(new Label("Congratulations! You just died"));
                 Button resurectButton = new Button("Resurect!", new ClickHandler() {
 
                     public void onClick(ClickEvent event) {
                         player.setSpriteX(0);
                         player.setSpriteY(0);
                         gamePanel.add(player.getImage(), 0, 0);
                         game.addPlayer(player);
                         dialogBox.setVisible(false);
                         dialogBox.hide();
                         textBox.setFocus(true);
                     }
                 });
                 v.add(resurectButton);
                 dialogBox.setWidget(v);
                 dialogBox.setAnimationEnabled(true);
                 dialogBox.showRelativeTo(gamePanel);
                 resurectButton.setFocus(true);
             }
 
             public void addBomb(Bomb bomb) {
             }
 
             public void removeBomb(Bomb bomb) {
             }
 
             public void addExplosion(Explosion explosion) {
             }
 
             public void addBoomBrick(BoomBrick boomBrick) {
             }
         });
     }
 }
