 package com.bensoft.mulg2.screen;
 
import org.andengine.entity.scene.background.Background;
import org.andengine.util.adt.color.Color;

 import com.bensoft.mulg2.textures.LevelSelectMenuTextureBundle;
 
 public class LevelSelectMenuScreen extends Screen<LevelSelectMenuTextureBundle> {
 
     public LevelSelectMenuScreen(LevelSelectMenuTextureBundle tex) {
         super(tex);
        scene.setBackgroundEnabled(true);
        scene.setBackground(new Background(Color.WHITE));
     }
 
     @Override
     public void onDestroy() {
     }
 
 }
