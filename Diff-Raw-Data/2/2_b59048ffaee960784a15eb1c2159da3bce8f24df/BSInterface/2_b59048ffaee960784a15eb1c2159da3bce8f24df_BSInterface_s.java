 package edu.smcm.gamedev.butterseal;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.math.Rectangle;
 
 /**
  * Contains program logic for the user interface.
  *
  * This class handles such things as
  *   the pause menu,
  *   the directional pad,
  *   and power selection.
  *
  * @author Sean
  *
  */
 public class BSInterface {
     public static boolean ANDROID_MODE = true;
     BSSession session;
     BSPlayer player;
 
     Music firstmusic, secondmusic, titlemusic;
     SpriteBatch cambatch;
     SpriteBatch controls;
     AssetManager assets;
     OrthographicCamera camera;
     BitmapFont font;
 
     Map<Rectangle, BSInterfaceActor> activeRegions;
 
     Sprite dpad;
     Sprite menubutton;
     Sprite title;
 
     private Sprite menu;
 
     private Sprite powerbar;
 
     public BSInterface(BSSession session) {
         font = new BitmapFont();
         assets = new AssetManager();
         cambatch = new SpriteBatch();
         controls = new SpriteBatch();
         camera = new OrthographicCamera();
         SetAssetLoaders();
         LoadAssets();
 
         BSPlayer.assets = assets;
         BSPlayer.batch = cambatch;
         BSPlayer.camera = camera;
 
         this.session = session;
         this.player = new BSPlayer(session.state);
         BSGameState.ASSETS = assets;
 
 
         final int TILE_HEIGHT=20, TILE_WIDTH=30;
         camera.setToOrtho(false, Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * TILE_WIDTH, TILE_HEIGHT);
 
         player.place("start");
         titlemusic.play();
 
         activeRegions = new HashMap<Rectangle, BSInterfaceActor>();
         LoadActiveRegions();
 
         key_state.put(Input.Keys.Z, false);
         key_state.put(Input.Keys.C, false);
         session.state.currentMap.usePower(session.state);
     }
 
     private void LoadActiveRegions() {
 
         final int VOFFSET = ANDROID_MODE ? -50 : 0;
 
 
         Rectangle r_menu_button = vFlipRectangle(menubutton.getBoundingRectangle());
         r_menu_button.height -= 23;
         r_menu_button.width -= 8;
         r_menu_button.x += 8;
 
         Rectangle r_entire_screen = new Rectangle(0, 0, 1280, 800);
 
         Rectangle r_start_game = new Rectangle(477, 348, 286, 100);
         Rectangle r_load_game = new Rectangle(477, 477, 340, 130);
         Rectangle r_quit_game = new Rectangle(450, 586, 320, 148);
         Rectangle r_resume = new Rectangle(472, 187, 385, 104);
         //Rectangle r_save = new Rectangle(472, 291, 282, 102);
         Rectangle r_quit = new Rectangle(472, 393, 282, 152);
         Rectangle r_aboutnext = new Rectangle(1164, 388, 108, 54);
         Rectangle r_credits = new Rectangle(470, 700, 280, 80);
         int h = Gdx.graphics.getHeight();
         int w = Gdx.graphics.getWidth();
         float hh = Math.abs(dpad.getHeight() - h);
         float xx = dpad.getWidth()/3;
         float yy = dpad.getHeight()/3;
         Rectangle r_dpad_left =    new Rectangle(dpad.getY() + 0, hh + yy, xx, yy);
         Rectangle r_dpad_up =      new Rectangle(dpad.getY() + xx, hh, xx, yy);
         Rectangle r_dpad_right =   new Rectangle(dpad.getY() + 2 * xx, hh + yy, xx, yy);
         Rectangle r_dpad_down =    new Rectangle(dpad.getY() + xx, hh + 2 * yy, xx, yy);
         Rectangle r_power_left =   new Rectangle(powerbar.getX(),
                                                  h - powerbar.getHeight() - powerbar.getY(),
                                                  powerbar.getHeight(), powerbar.getHeight());
         Rectangle r_power_right =  new Rectangle(powerbar.getX() + 2*powerbar.getHeight(),
                                                  h - powerbar.getHeight() - powerbar.getY(),
                                                  powerbar.getHeight(), powerbar.getHeight());
         Rectangle r_power_select = new Rectangle(powerbar.getX() + powerbar.getHeight(),
                                                  h - powerbar.getHeight() - powerbar.getY(),
                                                  powerbar.getHeight(), powerbar.getHeight());
         r_entire_screen.y += VOFFSET;
         r_start_game.y += VOFFSET;
         r_load_game.y += VOFFSET;
         r_quit_game.y += VOFFSET;
         r_resume.y += VOFFSET;
         //r_save.y += VOFFSET;
         r_quit.y += VOFFSET;
         r_aboutnext.y += VOFFSET;
         r_credits.y += VOFFSET;
 
         activeRegions.put(r_entire_screen, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 gui.session.screen = BSSessionState.TITLE;
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return  gui.session.screen == BSSessionState.ABOUT6 ||
                         gui.session.screen == BSSessionState.CREDITS;
             }
         });
         activeRegions.put(r_aboutnext, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 BSSessionState newstate = gui.session.screen;
                 switch(gui.session.screen) {
                 case ABOUT1:
                     newstate = BSSessionState.ABOUT2;
                     break;
                 case ABOUT2:
                     newstate = BSSessionState.ABOUT3;
                     break;
                 case ABOUT3:
                     newstate = BSSessionState.ABOUT4;
                     break;
                 case ABOUT4:
                     newstate = BSSessionState.ABOUT5;
                     break;
                 case ABOUT5:
                     newstate = BSSessionState.ABOUT6;
                     break;
                 case ABOUT6:
                     newstate = BSSessionState.TITLE;
                     break;
                 default:
                     throw new IllegalStateException();
                 }
                 gui.session.screen = newstate;
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return  gui.session.screen == BSSessionState.ABOUT1 ||
                         gui.session.screen == BSSessionState.ABOUT2 ||
                         gui.session.screen == BSSessionState.ABOUT3 ||
                         gui.session.screen == BSSessionState.ABOUT4 ||
                         gui.session.screen == BSSessionState.ABOUT5 ||
                         gui.session.screen == BSSessionState.ABOUT6;
             }
         });
         activeRegions.put(r_menu_button, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 firstmusic.pause();
                 secondmusic.pause();
                 gui.session.screen = BSSessionState.PAUSED;
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Pausing game.");
                 }
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_start_game, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 1) {
                     System.out.println("Starting game.");
                 }
                 gui.session.screen = BSSessionState.INGAME;
 
                 titlemusic.stop();
                 switch(gui.session.state.music) {
                 case FIRST_MUSIC:
                 	gui.session.state.music.playMusic(BSGameState.ASSETS);
                     break;
                 case SECOND_MUSIC:
                 	gui.session.state.music.playMusic(BSGameState.ASSETS);
                     break;
                 default:
                     throw new IllegalStateException();
                 }
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.TITLE;
             }
         });
         activeRegions.put(r_load_game, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Loading game");
                 }
                 gui.session.screen = BSSessionState.ABOUT1;
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.TITLE;
             }
         });
         activeRegions.put(r_quit_game, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Quitting game completely.");
                 }
                 Gdx.app.exit();
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.TITLE;
             }
         });
         activeRegions.put(r_resume, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Resuming game.");
                 }
                 gui.session.screen = BSSessionState.INGAME;
                 switch(gui.session.state.music) {
                 case FIRST_MUSIC:
                 	gui.session.state.music.playMusic(BSGameState.ASSETS);
                     break;
                 case SECOND_MUSIC:
                 	gui.session.state.music.playMusic(BSGameState.ASSETS);
                     break;
                 default:
                     throw new IllegalStateException();
                 }
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.PAUSED;
             }
         });
         activeRegions.put(r_quit, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Quitting game.");
                 }
                 gui.session.screen = BSSessionState.TITLE;
                gui.titlemusic.stop();
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.PAUSED;
             }
         });
         activeRegions.put(r_dpad_left, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Going left.");
                 }
                 gui.player.move(BSDirection.WEST);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_dpad_right, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Going right.");
                 }
                 gui.player.move(BSDirection.EAST);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_dpad_up, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Going up.");
                 }
                 gui.player.move(BSDirection.NORTH);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_dpad_down, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("Going down.");
                 }
                 gui.player.move(BSDirection.SOUTH);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_power_left, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("power left");
                 }
                 gui.player.setPower(-1);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_power_right, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("power right");
                 }
                 gui.player.setPower(1);
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_power_select, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 if (BSSession.DEBUG > 0) {
                     System.out.println("power select");
                 }
                 gui.player.usePower();
             }
 
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.INGAME;
             }
         });
         activeRegions.put(r_credits, new BSInterfaceActor() {
             @Override
             public void act(BSInterface gui) {
                 gui.session.screen = BSSessionState.CREDITS;
             }
             @Override
             public boolean active(BSInterface gui) {
                 return gui.session.screen == BSSessionState.TITLE;
             }
         });
     }
 
     /**
      * Polls the given {@link Input} for valid player interaction
      *   and handles it appropriately.
      * @param input
      */
     public void poll(Input input) {
         if (BSSession.DEBUG > 3) {
             System.out.println(session.screen);
         }
         pollKeyboard(input);
         if(input.isTouched() && !session.state.hasbeentouching) {
             session.state.hasbeentouching = true;
             for(Rectangle r : activeRegions.keySet()){
                 if (activeRegions.get(r).active(this) && isTouchingInside(input, r)){
                     activeRegions.get(r).act(this);
                     return;
                 }
             }
         } else if (!input.isTouched()) {
             session.state.hasbeentouching = false;
         }
     }
 
     Map<Integer, Boolean> key_state = new HashMap<Integer, Boolean>();
     Sprite about_screen;
     /**
      * Poll the keyboard for input.
      * @param input an input stream to analyze
      */
     private void pollKeyboard(Input input) {
         switch(session.screen) {
         case ABOUT1:
         case ABOUT2:
         case ABOUT3:
         case ABOUT4:
         case ABOUT5:
         case ABOUT6:
             break;
         case INGAME:
             if(!session.state.isMoving) {
                 // poll for movement
                 BSDirection toMove;
                 if(input.isKeyPressed(Input.Keys.RIGHT)) {
                     toMove = BSDirection.EAST;
                 } else if(input.isKeyPressed(Input.Keys.UP)) {
                     toMove = BSDirection.NORTH;
                 } else if(input.isKeyPressed(Input.Keys.LEFT)) {
                     toMove = BSDirection.WEST;
                 } else if(input.isKeyPressed(Input.Keys.DOWN)) {
                     toMove = BSDirection.SOUTH;
                 } else {
                     toMove = null;
                 }
                 if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                     input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                     player.SPEED = 5 * BSPlayer.DEFAULT_SPEED;
                 } else {
                     player.SPEED = BSPlayer.DEFAULT_SPEED;
                 }
                 player.move(toMove);
             }
 
             if(!depressed(input, Input.Keys.Z) && !depressed(input, Input.Keys.C)) {
                 // poll for power chooser
                 if(input.isKeyPressed(Input.Keys.Z)) {
                     player.setPower(-1);
                     key_state.put(Input.Keys.Z, true);
                 } else if (input.isKeyPressed(Input.Keys.C)) {
                     player.setPower(1);
                     key_state.put(Input.Keys.C, true);
                 } else {
                     session.state.isSelectingPower = false;
                     key_state.put(Input.Keys.Z, false);
                     key_state.put(Input.Keys.C, false);
                 }
             }
             if (input.isKeyPressed(Input.Keys.X)) {
                 player.usePower();
             }
             break;
         case PAUSED:
         case TITLE:
         default:
             break;
         }
 
         if (input.isKeyPressed(Input.Keys.ESCAPE)) {
             Gdx.app.exit();
         }
     }
 
     public boolean depressed(Input input, int key) {
         return input.isKeyPressed(key) && key_state.get(key);
     }
 
     /**
      *
      * @param input
      * @param region
      * @return true if input is being touched within the given region, false otherwise
      */
     public boolean isTouchingInside(Input input, Rectangle region) {
         if (!input.isTouched()) {
             return false;
         }
         int x = input.getX();
         int y = input.getY();
         return region.x < x && x < region.x + region.width
             && region.y < y && y < region.y + region.height;
     }
 
     /**
      * Draws the interface on the screen.
      */
     public void draw() {
         /* If the game is in session, make the major interface elements.
          * If the game is additionally paused, handle that as well.
          *
          * If we are not in a game, then draw the title screen.
          */
         switch(session.screen) {
         case ABOUT1:
         case ABOUT2:
         case ABOUT3:
         case ABOUT4:
         case ABOUT5:
         case ABOUT6:
             controls.begin();
             MakeAboutScreen();
             controls.end();
             break;
         case INGAME:
             session.state.currentMap.draw(camera);
             cambatch.begin();
             player.draw();
             cambatch.end();
             controls.begin();
             MakePowerBar();
             MakePowerSelector();
             MakeDirectionalPad();
             MakePauseButton();
             controls.end();
             break;
         case PAUSED:
             controls.begin();
             MakePauseScreen();
             controls.end();
             break;
         case TITLE:
             controls.begin();
             MakeTitleScreen();
             controls.end();
             break;
         case CREDITS:
             controls.begin();
             MakeCreditsScreen();
             controls.end();
             break;
         default:
             break;
         }
 
         if(BSSession.DEBUG > 3) {
             DrawActiveRegions();
         }
 
         if(session.state.isWTF) {
             camera.rotate(1f);
         }
 
         if(BSSession.DEBUG > 0) {
             controls.begin();
             printText(5,String.format("FPS: %d", Gdx.graphics.getFramesPerSecond()));
             printText(25,String.format("Selected Power: %s", session.state.selectedPower));
             controls.end();
         }
         camera.update();
         cambatch.setProjectionMatrix(camera.combined);
     }
 
     private void MakeCreditsScreen() {
         credits_screen.draw(controls);
     }
 
     private void printText(int pos, String s) {
         font.draw(controls, s, 1, Gdx.graphics.getHeight()-pos);
     }
 
     private void MakeAboutScreen() {
         Texture to = about_screen.getTexture();
         switch(session.screen) {
         case ABOUT1:
             to = assets.get(BSAsset.MENU_ABOUT1.assetPath, Texture.class);
             break;
         case ABOUT2:
             to = assets.get(BSAsset.MENU_ABOUT2.assetPath, Texture.class);
             break;
         case ABOUT3:
             to = assets.get(BSAsset.MENU_ABOUT3.assetPath, Texture.class);
             break;
         case ABOUT4:
             to = assets.get(BSAsset.MENU_ABOUT4.assetPath, Texture.class);
             break;
         case ABOUT5:
             to = assets.get(BSAsset.MENU_ABOUT5.assetPath, Texture.class);
             break;
         case ABOUT6:
             to = assets.get(BSAsset.MENU_ABOUT6.assetPath, Texture.class);
             break;
         default:
             throw new IllegalStateException();
         }
         about_screen.setTexture(to);
         about_screen.draw(controls);
     }
 
     ShapeRenderer rend = new ShapeRenderer();
     private Sprite credits_screen;
 
     private void DrawActiveRegions() {
         int h = Gdx.graphics.getHeight();
         rend.begin(ShapeType.Line);
         for(Rectangle r : activeRegions.keySet()) {
             rend.setColor(Color.RED);
             rend.rect(r.x, Math.abs(r.y-h)-r.height, r.width, r.height);
         }
         rend.end();
     }
     private void MakePowerBar() {
         switch(session.state.selectedPower) {
         case FIRE:
             powerbar.setRegion(BSAsset.POWERBAR_FIRE.getTextureRegion(assets));
             break;
         case GROWTH:
         case JUMP:
         case LIGHT:
         case STRENGTH:
         case SWIMMING:
         case ACTION:
         default:
             powerbar.setRegion(BSAsset.POWERBAR_ACTION.getTextureRegion(assets));
             break;
         }
         powerbar.draw(controls);
     }
     private void MakePowerSelector() {
 
     }
     private void MakeDirectionalPad() {
         dpad.draw(controls);
     }
     private void MakePauseButton() {
         menubutton.draw(controls);
     }
     /**
      * Dims the screen and displays the pause menu
      */
     private void MakePauseScreen() {
         menu.draw(controls);
     }
     private void MakeTitleScreen() {
         title.draw(controls);
     }
     /**
      * Sets all the loaders needed for the {@link #assetManager}.
      */
     private void SetAssetLoaders() {
         assets.setLoader(TiledMap.class,
                                new TmxMapLoader(
                                  new InternalFileHandleResolver()));
 
     }
     /**
      * Loads all game assets
      */
     private void LoadAssets() {
         for(BSAsset asset : BSAsset.values()) {
             if(asset.assetPath.endsWith(".png")) {
                 assets.load(asset.assetPath, Texture.class);
             } else if (asset.assetPath.endsWith(".tmx")) {
                 assets.load(asset.assetPath, TiledMap.class);
             } else if (asset.assetPath.endsWith(".mp3") ||
                        asset.assetPath.endsWith(".ogg")) {
                 assets.load(asset.assetPath, Music.class);
             } else {
                 System.err.print("No loader found for " + asset.assetPath);
                 System.exit(1);
             }
         }
         assets.finishLoading();
 
         dpad = new Sprite(BSAsset.DIRECTIONAL_PAD.getTextureRegion(assets));
         dpad.setBounds(0, 0, 256, 256);
         menubutton = new Sprite(BSAsset.MENU_BUTTON.getTextureRegion(assets));
         menubutton.setPosition(Gdx.graphics.getWidth()  - menubutton.getWidth(),
                                Gdx.graphics.getHeight() - menubutton.getHeight());
         title = new Sprite(BSAsset.TITLE.getTextureRegion(assets));
         menu = new Sprite(BSAsset.MENU.getTextureRegion(assets));
 
         powerbar = new Sprite(BSAsset.POWERBAR_ACTION.getTextureRegion(assets));
         powerbar.setPosition(Gdx.graphics.getWidth() - powerbar.getWidth() + 50, 25);
 
         about_screen = new Sprite(BSAsset.MENU_ABOUT1.getTextureRegion(assets));
         credits_screen = new Sprite(BSAsset.CREDITS_SCREEN.getTextureRegion(assets));
 
         firstmusic = assets.get(BSAsset.FIRST_MUSIC.assetPath);
         firstmusic.setLooping(true);
         secondmusic = assets.get(BSAsset.SECOND_MUSIC.assetPath);
         secondmusic.setLooping(true);
 
         titlemusic = assets.get(BSAsset.TITLE_MUSIC.assetPath);
         titlemusic.setLooping(true);
     }
     public void dispose() {
         for(BSMap m : BSMap.values()) {
             if(BSSession.DEBUG > 1) {
                 System.out.printf("Unloading map %s%n", m.asset.assetPath);
             }
             m.map.dispose();
         }
         //cambatch.dispose();
         //controls.dispose();
         assets.dispose();
     }
 
     /**
      * Flips a rectangle so that it uses the bottom-left as its origin (instead of top-left)
      * @param rect
      * @return
      */
     private static Rectangle vFlipRectangle(Rectangle rect) {
         Rectangle r = new Rectangle(rect);
         r.y = Math.abs(Gdx.graphics.getHeight()-r.y) - r.height;
         return r;
     }
 
     @SuppressWarnings("unused")
     private static Rectangle vFlipRectangle(float x, float y, float width, float height) {
         return vFlipRectangle(new Rectangle(x, y, width, height));
     }
 }
 
 // Local Variables:
 // indent-tabs-mode: nil
 // End:
