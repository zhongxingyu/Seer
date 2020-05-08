 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package ui;
 
 import game.CacheTool;
 
 import java.io.IOException;
 
 import loader.parser.ParserException;
 import main.GameStateManager;
 import main.IGameState;
 import main.Locator;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import ui.menu.Menu;
 import ui.menu.MenuButton;
 import ui.menu.MenuItemDisabled;
 
 public class MenuState implements IGameState {
   private final Image background;
   private final Menu  menu;
 
  public MenuState(final GameStateManager manager)
   throws ParserException, IOException {
     background = CacheTool.getImage(Locator.getCache(), "textures/menu/main.png");
 
     menu = new Menu();
 
     menu.add(new MenuButton("singleplayer", 60, 280, new UIEvent() {
       @Override public void fire() {
         manager.enterSinglePlayer();
       }
     }));
     menu.add(new MenuItemDisabled("coop", 60, 380));
     menu.add(new MenuItemDisabled("options", 60, 480));
     menu.add(new MenuButton("exit", 60, 580, new UIEvent() {
       @Override public void fire() {
         manager.quit();
       }
     }));
   }
 
   @Override
   public void update(GameStateManager stateGame, float delta) {
     if (Keyboard.isKeyDown(Keyboard.KEY_F2)) {
       stateGame.quit();
     } else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
       menu.up();
     } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
       menu.down();
     } else if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
       menu.click();
     }
   }
 
   @Override
   public void render(Graphics g) throws SlickException {
     g.drawImage(background, 0, 0);
     menu.render(g);
   }
 }
