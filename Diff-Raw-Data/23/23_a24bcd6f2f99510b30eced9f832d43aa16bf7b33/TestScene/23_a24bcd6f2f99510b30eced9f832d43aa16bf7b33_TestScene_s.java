 package com.hifreshday.android.test.scene;
 
 import android.content.res.Resources;

 import com.hifreshday.android.pge.entity.scene.BitmapBgScreen;
import com.hifreshday.android.pge.entity.scene.Scene;
import com.hifreshday.android.pge.entity.shape.sprite.Sprite;
 import com.hifreshday.android.test.res.PicResDemo;
 import com.hifreshday.android.test.sprite.TestSprite;
 
 public class TestScene extends BitmapBgScreen {
 	
 	private PicResDemo picresDemo;
 	private TestSprite sprite;
 	
 	public TestScene(Resources res) {
 		
 		onLoadResources(res);
 	}
 
 	@Override
 	public void onLoadResources(Resources res) {
 		picresDemo = new PicResDemo(res);
		sprite = new TestSprite(picresDemo, 50, 50, picresDemo.getDefalutBitmap().getWidth(), picresDemo.getDefalutBitmap().getHeight());
 		registerTouch(sprite);
 		attachChild(sprite);
 	}
 
 	@Override
 	public void onUnloadResources() {
 		picresDemo = null;
 		sprite = null;
 	}
 
 
 }
