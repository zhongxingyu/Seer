 package com.brekol.scene;
 
 import com.brekol.util.SceneType;
 import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
 import org.andengine.util.color.Color;
 
 /**
  * User: Breku
  * Date: 01.07.13
  */
 public class LoadingScene extends BaseScene {
 
     @Override
     public void createScene() {
         setBackground(new Background(Color.WHITE));
        attachChild(new Text(400,240,resourcesManager.getMediumFont(),"Loading...",vertexBufferObjectManager));
     }
 
     @Override
     public void onBackKeyPressed() {
         return;
     }
 
     @Override
     public SceneType getSceneType() {
         return SceneType.LOADING;
     }
 
     @Override
     public void disposeScene() {
     }
 }
