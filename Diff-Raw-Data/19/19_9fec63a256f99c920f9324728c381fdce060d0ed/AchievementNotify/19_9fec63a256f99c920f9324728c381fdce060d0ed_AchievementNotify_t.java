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
 
 package com.madgear.ninjatrials.hud;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.FadeOutModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import com.madgear.ninjatrials.managers.GameManager;
 import com.madgear.ninjatrials.managers.ResourceManager;
 
 /**
  * Used by game HUD.
  * @author Madgear Games
  *
  */
 public class AchievementNotify extends Entity {
     private static final float MOVE_TIME = 0.5f;
     private static final float DELAY_TIME = 3f;
 
     private Sprite achievContainerSprite;
     private Text achievContainerText;
     
     public AchievementNotify() {
         achievContainerSprite = new Sprite(0, 0,
                 ResourceManager.getInstance().hudAchievementIngameContainer,
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
 
         achievContainerText = new Text(0, 0,
                ResourceManager.getInstance().fontSmall,
                 "achievement name placeholder text achievement name placeholder text",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         
         setVisible(false);
         attachChild(achievContainerSprite);
         attachChild(achievContainerText);
     }
     
     
     /**
      * Show a box that informs the achievment is unlocked.
      * @param achievNumber The achievement number (the vector value + 1). For example, if we want
      * showing the achievement 4 (Achieve a single cut with 100% precision)
      * we must put achievNumber = 4
      */
     public void showAchievementCompleted(int achievNumber) {
         setVisible(false);
         achievContainerText.setText(GameManager.player1achiev.achievements[achievNumber - 1].name);
         setX(ResourceManager.getInstance().cameraWidth + getWidth()/2);
         setY(ResourceManager.getInstance().cameraHeight * 2/3);
        setAlpha(1);
         setVisible(true);
         
         registerEntityModifier(new SequenceEntityModifier(
                 new MoveModifier(MOVE_TIME, getX(), getY(), getX() - getWidth() - 20, getY()),
                 new DelayModifier(DELAY_TIME),
                new MoveModifier(MOVE_TIME, getX() - getWidth() - 20, getY(), getX(), getY()),
                new FadeOutModifier(0.5f)));
     }
     
     
     public float getWidth() {
         return achievContainerSprite.getWidth();
     }    
 }
