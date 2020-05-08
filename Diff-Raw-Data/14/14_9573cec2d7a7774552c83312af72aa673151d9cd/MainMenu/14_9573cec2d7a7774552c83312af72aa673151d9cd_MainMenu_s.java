 package com.lunaticedit.legendofwaffles.implementations.scene;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.lunaticedit.legendofwaffles.contracts.Scene;
 import com.lunaticedit.legendofwaffles.factories.SpriteBatchFactory;
 import com.lunaticedit.legendofwaffles.helpers.Constants;
 import com.lunaticedit.legendofwaffles.implementations.Input;
 import com.lunaticedit.legendofwaffles.implementations.MusicPlayer;
 import com.lunaticedit.legendofwaffles.implementations.generators.TilesetGraphicsGenerator;
 
 public final class MainMenu implements Scene {
     private final Texture _texture;
     private int _menuSelection = 0;
 
     public MainMenu() {
         _texture = new Texture(Constants.MainMenuBackgroundFile);
 
         MusicPlayer.getInstance().stopSong();
         MusicPlayer.getInstance().playSong(Constants.MainMenuSong);
     }
 
     @Override
     public void render() {
         final SpriteBatch s = new SpriteBatchFactory().generate();
         final TilesetGraphicsGenerator g = (new TilesetGraphicsGenerator());
         // Render the background image
         s.draw(_texture, -128f, -110f);
 
         // Render the main menu options.
        g.drawText(110, 90,  "NEW GAME");
        g.drawText(110, 115, "CONTINUE");
        g.drawText(110, 140, " CONFIG ");
        g.drawText(110, 165, "  QUIT  ");
 
         // Render the menu selector if not on a touch screen
         if (Gdx.app.getType() == Application.ApplicationType.Android) {
 
         } else {
            g.drawTile(94, 90 + (_menuSelection * 25), 1);
            g.drawTile(180, 90 + (_menuSelection * 25), 1);
         }
     }
 
     @Override
     public void update() {
         if (Input.getInstance().getKeyState(Keys.DPAD_UP)) {
             Input.getInstance().setKeyState(Keys.DPAD_UP, false);
             moveSelectionUp();
         } else if (Input.getInstance().getKeyState(Keys.DPAD_DOWN)) {
             Input.getInstance().setKeyState(com.badlogic.gdx.Input.Keys.DPAD_DOWN, false);
             moveSelectionDown();
         } else if (Input.getInstance().getKeyState(Keys.ENTER) ||
                 Input.getInstance().getKeyState(Keys.BUTTON_A)) {
             Input.getInstance().setKeyState(Keys.ENTER, false);
             Input.getInstance().setKeyState(Keys.BUTTON_A, false);
             activateMenuOption();
         }
     }
 
     private void moveSelectionUp() {
         if (--_menuSelection < 0)
         { _menuSelection = 3; }
     }
 
     private void moveSelectionDown() {
         if (++_menuSelection > 3)
         { _menuSelection = 0; }
     }
 
     private void activateMenuOption() {
         switch (_menuSelection) {
             case 0: {
                 // Start new game
             } break;
             case 1: {
                 // Continue existing game
             } break;
             case 2: {
                 // Configure
             } break;
             case 3: {
                 // Quit
                 Gdx.app.exit();
             } break;
         }
     }
 }
