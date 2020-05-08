 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.icbat.game.tradesong.screens.*;
 import com.icbat.game.tradesong.stages.HUDStage;
 import com.icbat.game.tradesong.stages.InventoryStage;
 import com.icbat.game.tradesong.stages.WorkshopStage;
 
 
 /**
  * This class:
  *  - sets up the game initially
  *  - tracks/exposes game variables
  *  - loads common/global assets
  * */
 public class Tradesong extends Game {
 
 
 
     private static final String PARAM_DELAY_GATHER = "gatherDelay";
     private static final String PARAM_DELAY_CRAFT = "craftDelay";
 
     public static GameStateManager gameState;
     public static AssetManager assetManager = new AssetManager();
 
     private static LevelScreen currentMap;
     private static ScreenTypes currentScreenType;
 
     private HUDStage hud;
     private InventoryStage inventoryStage;
     private WorkshopStage workshopStage;
 
 
     @Override
 	public void create() {
         initializeAssets();
 
         gameState = new GameStateManager();
 		goToScreen(ScreenTypes.MAIN_MENU);
 
         hud = new HUDStage(this);
         inventoryStage = new InventoryStage();
         workshopStage = new WorkshopStage();
 
         GameStateManager.updateMusic(getMusic(MUSIC.TITLE_THEME));
 	}
 
     private void initializeAssets() {
 
         for (TEXTURE texture : TEXTURE.values()) {
             assetManager.load(texture.path, Texture.class);
         }
 
         for (SOUND sound : SOUND.values()) {
             assetManager.load(sound.path, Sound.class);
         }
 
         for (MUSIC music : MUSIC.values()) {
             assetManager.load(music.path, Music.class);
         }
         assetManager.finishLoading(); // Blocks until finished
     }
 
 
 
 
     /* Screen management methods */
 
     /** Things that can be passed to goToScreen */
     public static enum ScreenTypes {
         MAIN_MENU,
         SETTINGS,
 
         LEVEL,
         TOWN,
 
         WORKSHOP,
         INVENTORY,
         STORE,
     }
 
     public void goToScreen(ScreenTypes screen) {
 
 
         if (screen.equals(currentScreenType)) {
 
             Gdx.app.log("", "found current screen");
 
             if (screen.equals(ScreenTypes.LEVEL)) {
                 goToOverlap(new MainMenuScreen(this));
                 currentScreenType = ScreenTypes.MAIN_MENU;
             } else {
                 leaveOverlap();
                 currentScreenType = ScreenTypes.LEVEL;
             }
 
         } else {
 
             currentScreenType = screen;
 
             switch(screen) {
                 case MAIN_MENU:
                     goToOverlap(new MainMenuScreen(this));
                     break;
                 case TOWN:
                     goToMap("town_hub");
                     break;
                 case WORKSHOP:
                     goToOverlap(new WorkshopScreen(hud, inventoryStage, workshopStage));
                     break;
                 case INVENTORY:
                     goToOverlap(new InventoryScreen(hud, inventoryStage));
                     break;
                 case STORE:
                     goToOverlap(new StoreScreen(hud, inventoryStage));
                    break;
                 case SETTINGS:
                     goToOverlap(new SettingsScreen(this));
                    break;
             }
         }
     }
 
     public void goToMap(String mapName) {
         Gdx.app.log("", "Going to map " + mapName);
 
         currentMap = new LevelScreen(mapName, hud, this);
         setScreen(currentMap);
     }
 
     public void goToOverlap(AbstractScreen newScreen) {
         setScreen(newScreen);
     }
 
     public void leaveOverlap() {
         setScreen(currentMap);
     }
 
     public void goBackAScreen() {
         //TODO impl
     }
 
     public static String getParamDelayGather() {
         return PARAM_DELAY_GATHER;
     }
 
     public static String getParamDelayCraft() {
         return PARAM_DELAY_CRAFT;
     }
 
 
     /** All the assets */
     public static enum TEXTURE {
         ITEMS("items"),
         FRAME("frame"),
         WORKSHOP_ARROW("workshop-arrow"),
         MAP_ARROW("map-arrow"),
         ICON_HAMMER("hammer-drop"),
         ICON_WRENCH("auto-repair"),
         ICON_BOOK("burning-book"),
         CHAR("character"),
         COIN("goldCoin5"),
         SLIDER_HEAD("slider-icon"),
         SLIDER_BG("slider-bg"),;
 
         private String path;
 
         private TEXTURE(String pathVal) {
             this.path = "sprites/" + pathVal + ".png";
         }
     }
     
     public static enum SOUND {
         GATHER_CLINK("hammering");
 
         private String path;
 
         private SOUND(String pathVal) {
             this.path = "sounds/" + pathVal + ".ogg";
         }
 
     }
 
     public static enum MUSIC {
         TITLE_THEME("Thatched Villagers");
 
         private String path;
 
         private MUSIC(String pathVal) {
             this.path = "music/" + pathVal + ".mp3";
         }
     }
 
     /** Convenience method to prevent having to call assetManager.get(longConstantName)
      *
      * @throws Error if the file could not be found
      * @return the object in assetManager by that name
      * */
 
     public static Texture getTexture(TEXTURE toFind) {
         return assetManager.get(toFind.path);
     }
 
     public static Sound getSound(SOUND toFind) {
         return assetManager.get(toFind.path);
     }
 
     public static Music getMusic(MUSIC toFind) {
         return assetManager.get(toFind.path);
     }
 }
