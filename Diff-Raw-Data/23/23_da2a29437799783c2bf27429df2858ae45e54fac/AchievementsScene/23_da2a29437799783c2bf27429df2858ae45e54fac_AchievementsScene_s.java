 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madgear.ninjatrials;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.EntityBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import com.madgear.ninjatrials.achievements.Achievement;
 import com.madgear.ninjatrials.achievements.AchievementSetNinjaTrial;
 import com.madgear.ninjatrials.hud.SelectionStripe;
 import com.madgear.ninjatrials.managers.GameManager;
 import com.madgear.ninjatrials.managers.ResourceManager;
 import com.madgear.ninjatrials.managers.SceneManager;
 import com.madgear.ninjatrials.test.TestingScene;
 
 public class AchievementsScene extends GameScene {
 
     private final static float WIDTH = ResourceManager.getInstance().cameraWidth;
     private final static float HEIGHT = ResourceManager.getInstance().cameraHeight;
     private AchievementGrid achievementGrid;
     private AchievementDetail achievementDetail;
     private Text achievTittle;
     
     public AchievementsScene() {
         super(0);
     }
     
     @Override
     public Scene onLoadingScreenLoadAndShown() {
         return null;
     }
 
     @Override
     public void onLoadingScreenUnloadAndHidden() {}
 
     @Override
     public void onLoadScene() {
         ResourceManager.getInstance().loadMenuAchievementsResources();
     }
 
     @Override
     public void onShowScene() {
         // Bg:
         // TODO: background
         getBackground().setColor(0.5f, 0.3f, 0.8f);
         
         //
         // Diff tittle:
         achievTittle = new Text(1540, HEIGHT - 166,
                 ResourceManager.getInstance().fontMedium, "Achievements",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         attachChild(achievTittle);
         
         // Main achievements grid:
         achievementGrid = new AchievementGrid(662, HEIGHT/2, GameManager.player1achiev);
         attachChild(achievementGrid);
         
         // Achiev detail:
         achievementDetail = new AchievementDetail(1550, HEIGHT - 740);
         attachChild(achievementDetail);
     }
 
     @Override
     public void onHideScene() {}
 
     @Override
     public void onUnloadScene() {
         ResourceManager.getInstance().unloadMenuAchievementsResources();
     }
 
     // INTERFACE -------------------------------------------
     
     /**
      * Returns to the main options screen when menu button is pressed.
      */
     @Override
     public void onPressButtonMenu() {
         if (ResourceManager.getInstance().engine != null)
             SceneManager.getInstance().showScene(new TestingScene());
     }
     
     @Override
     public void onPressDpadLeft() {
         achievementGrid.moveMarkLeft();
        achievementDetail.update();
     }
 
     @Override
     public void onPressDpadRight() {
         achievementGrid.moveMarkRight();
        achievementDetail.update();
     }
 
     @Override
     public void onPressDpadUp() {
         achievementGrid.moveMarkUp();
        achievementDetail.update();
     } 
 
     @Override
     public void onPressDpadDown() {
         achievementGrid.moveMarkDown();
        achievementDetail.update();
     }  
     
     
     
     // AUX CLASS ---------------------------------------------
     
     class AchievementGrid extends Entity {
         private static final int COLS = 7;
         private static final int ROWS = 5;
         private static final float BORDER_ICON_GAP = 20;
         private static final float PUSH_DELAY_TIME = 0.3f;
         private float iconSpaceX, iconSpaceY;
         private Sprite containerIconsSprite;
         private float itemX, itemY;
         private AchievementItem items[][];
         //private AchievSelectionMark selectionMark;
         private int selectedCol = 0;
         private int selectedRow = 0;
         private Sprite selectionMarkSprite;
         private boolean moveEnabled = true;
         private TimerHandler timerHandler;
         
         
         public AchievementGrid(float x, float y, AchievementSetNinjaTrial set) {
             containerIconsSprite = new Sprite(x, y, 
                     ResourceManager.getInstance().menuAchievementsContainerIcons,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             attachChild(containerIconsSprite);
             
             iconSpaceX = (containerIconsSprite.getWidth() - BORDER_ICON_GAP * 2) / COLS;
             iconSpaceY = (containerIconsSprite.getHeight() - BORDER_ICON_GAP * 2) / ROWS;
             
             items = new AchievementItem[COLS][ROWS];
             
             for(int i = 0; i < COLS; i++)
                 for (int j = 0; j < ROWS; j++) {
 /*                    itemX = containerIconsSprite.getX() - containerIconsSprite.getWidth() /2 
                             + BORDER_ICON_GAP + iconSpaceX * (0.5f + i);
                     itemY = containerIconsSprite.getY() + containerIconsSprite.getHeight() /2
                             - (BORDER_ICON_GAP + iconSpaceY * (0.5f + j));*/
                     
                     itemX = getXfromCol(i);
                     itemY = getYfromRow(j);
                     
                     items[i][j] = new AchievementItem(itemX, itemY, i, j, 
                             set.achievements[j * COLS + i]);
                     attachChild(items[i][j]);
             }
             
             selectionMarkSprite = new Sprite(getXfromCol(0), getYfromRow(0), 
                     ResourceManager.getInstance().menuAchievementsSelectionMark,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             attachChild(selectionMarkSprite);
 
             
         }
 
         public AchievementItem getCurrentItem() {
             return items[selectedCol][selectedRow];
         }
         
         public int getCol() {
             return selectedCol;
         }
         
         public int getRow() {
             return selectedRow;
         }
         
         
         public float getXfromCol(int col) {
             return containerIconsSprite.getX() - containerIconsSprite.getWidth() /2 
                     + BORDER_ICON_GAP + iconSpaceX * (0.5f + col);
         }
         
         public float getYfromRow(int row) {
             return containerIconsSprite.getY() + containerIconsSprite.getHeight() /2
             - (BORDER_ICON_GAP + iconSpaceY * (0.5f + row));
         }
         
         public void setMarkPosition(int col, int row) {
             selectedCol = col;
             selectedRow = row;
             updateMarkPosition();
         }
         
         public void moveMarkUp() {
             if(selectedRow > 0 && moveEnabled) selectedRow--;
             updateMarkPosition();
         }
         
         public void moveMarkDown() {
             if(selectedRow < ROWS - 1 && moveEnabled) selectedRow++;
             updateMarkPosition();
         }
         
         public void moveMarkLeft() {
             if(selectedCol > 0 && moveEnabled) selectedCol--;
             updateMarkPosition();
         }
         
         public void moveMarkRight() {
             if(selectedCol < COLS - 1 && moveEnabled) selectedCol++;
             updateMarkPosition();
         }
         
         public void updateMarkPosition() {
             selectionMarkSprite.setX(getXfromCol(selectedCol));
             selectionMarkSprite.setY(getYfromRow(selectedRow));
             addDelay();
         }
         
         private void addDelay() {
             moveEnabled  = false;
             timerHandler = new TimerHandler(PUSH_DELAY_TIME, true, new ITimerCallback() {
                 @Override
                 public void onTimePassed(final TimerHandler pTimerHandler) {
                     moveEnabled = true;
                     AchievementGrid.this.unregisterUpdateHandler(timerHandler);
                 } 
             });
             registerUpdateHandler(timerHandler);
         }
     }
     
     
     class AchievementItem extends Entity {
         private final int ICON_SIZE = ResourceManager.getInstance().MENU_ACHIEV_ICON_SIZE;
         private TiledSprite achievItemSprite;
         private Achievement achievement;
         
         AchievementItem(float x, float y, int col, int row, Achievement achiev) {
             
             achievItemSprite = new TiledSprite(x, y,
                     ResourceManager.getInstance().menuAchievementsIconsArray[col][row], 
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             if(achiev.completed)
                 achievItemSprite.setCurrentTileIndex(1);
             else 
                 achievItemSprite.setCurrentTileIndex(0);    
             attachChild(achievItemSprite);
         }
         
         public Achievement getAchievement() {
             return achievement;
         }
         
 /*        public float getX() {
             return achievItemSprite.getX();
         }
         
         public float getY() {
             return achievItemSprite.getY();
         }*/
     }
     
 
     
     class AchievementDetail extends Entity {
         private Sprite containerDescriptionSprite;
         private Sprite successSprite;
         private TiledSprite iconsSprite;
         private Text tittle;
         private Text description;
         private Text successSentence;
         private Text progress, progressTotal;
 
         
         AchievementDetail(float x, float y) {
             containerDescriptionSprite = new Sprite(x, y,
                     ResourceManager.getInstance().menuAchievementsContainerDescription,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             attachChild(containerDescriptionSprite);
             
             iconsSprite = new TiledSprite(
                     containerDescriptionSprite.getX() - 210,
                     containerDescriptionSprite.getY() + 118,
                     ResourceManager.getInstance().menuAchievementsIconsBig,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             attachChild(iconsSprite);
             
             successSprite = new Sprite(
                     containerDescriptionSprite.getX() + 210,
                     containerDescriptionSprite.getY() - 118,
                     ResourceManager.getInstance().menuAchievementsSuccessStamp,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             successSprite.setVisible(false);
             attachChild(successSprite);
             
             tittle = new Text(
                     containerDescriptionSprite.getX() + 60,
                     containerDescriptionSprite.getY() + 104,
                     ResourceManager.getInstance().fontSmall, "TittlePlaceHolderXXXXXXX",
                     new TextOptions(HorizontalAlign.CENTER),
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             tittle.setVisible(false);
             attachChild(tittle);
             
             //TODO: control the text size to keep into the container box.
             description = new Text(
                     containerDescriptionSprite.getX(),
                     containerDescriptionSprite.getY() - 10,
                     ResourceManager.getInstance().fontSmall,
                    "TextDescriptionPlaceHolderXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                     new TextOptions(HorizontalAlign.CENTER),
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
             description.setVisible(false);
             attachChild(description); 
             
             update();
         }
         
         public void update() {
             Achievement achiev = achievementGrid.getCurrentItem().getAchievement();
             
             iconsSprite.setCurrentTileIndex(achievementGrid.getCol() + achievementGrid.getRow()*7);
             
             if(achiev.completed) {
                 tittle.setText(achiev.name);
                 description.setText(achiev.description);
                 successSentence.setText(achiev.successSentence);
                 tittle.setVisible(true);
                 description.setVisible(true);
                 successSentence.setVisible(true);
                 successSprite.setVisible(true);
                 progress.setVisible(false);
                 progressTotal.setVisible(false);
             }
             else
                 if(achiev.isSecret) {
                     // not completed but secret
                     tittle.setText(achiev.clueTittle);
                     description.setText(achiev.clueDescription);
                 }
                 else {
                     // not completed not secret
                 }
         }
     }
 
 
     
     
 }
