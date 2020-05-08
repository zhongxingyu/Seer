 /***
 === BrainGame ===
 
 Copyright (C) 2011 Giovanni Amati
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/
 
 package org.anddev.andengine.braingamelite.scene;
 
 import org.anddev.andengine.braingamelite.singleton.Enviroment;
 import org.anddev.andengine.braingamelite.singleton.Resource;
 import org.anddev.andengine.braingamelite.util.MyChangeableText;
 import org.anddev.andengine.braingamelite.util.MySound;
 import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.LoopEntityModifier;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.util.modifier.IModifier;
 
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.net.Uri;
 
 import com.openfeint.api.ui.Dashboard;
 
 public class MainMenu extends Scene implements IOnAreaTouchListener {
 	private MySound mDone;
 	
 	public MainMenu() {
 		super(1);
 		Enviroment.instance().reInitVariables(); // init vars
 		
 		this.mDone = Resource.instance().getSound("ok");
 		int x = Enviroment.CAMERA_WIDTH / 2;
 		
     	Sprite back = new Sprite(0, 0, Resource.instance().texBack);
     	Sprite title = new Sprite(0, 0, Resource.instance().texTitle);
 		title.setPosition(x - title.getWidthScaled() / 2, 47);
 		title.registerEntityModifier(
 				new LoopEntityModifier(
 						null, 
 						-1, 
 						null,
 						new SequenceEntityModifier(
 								new ScaleModifier(0.7f, 1f, 1.04f),
 								new ScaleModifier(0.7f, 1.04f, 1f)
 						)
 				)
 		);
 		
     	int y = 422;
     	
     	MyChangeableText diff = new MyChangeableText(0, 0, Resource.instance().fontMainMenu, "EASY", 6);
     	diff.setPosition(x - diff.getWidthScaled() / 2, y);
     	diff.setColor(1.0f, 1.0f, 0.6f);
     	
     	MyChangeableText play = new MyChangeableText(0, 0, Resource.instance().fontMainMenu, "PLAY", 4);
     	play.setPosition(x - play.getWidthScaled() / 2, y + 70);
     	play.setColor(1.0f, 1.0f, 0.6f);
     	MyChangeableText score = new MyChangeableText(0, 0, Resource.instance().fontMainMenu, "SCORE", 5);
     	score.setPosition(x - score.getWidthScaled() / 2, y + 140);
     	score.setColor(1.0f, 1.0f, 0.6f);
     	
     	MyChangeableText player= new MyChangeableText(0, 0, Resource.instance().fontMainMenu, "FULL VERS.", 10);
     	player.setPosition(x - player.getWidthScaled() / 2, y + 210);
     	player.setColor(1.0f, 1.0f, 0.6f);
     	
     	getLastChild().attachChild(back);
     	getLastChild().attachChild(title);
     	getLastChild().attachChild(diff);
     	getLastChild().attachChild(player);
     	getLastChild().attachChild(play);
     	getLastChild().attachChild(score);
     	
     	setOnAreaTouchListener(this);
     	
     	registerTouchArea(diff);
     	registerTouchArea(player);
     	registerTouchArea(play);
     	registerTouchArea(score);
 	}
 	
 	@Override
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 			final MyChangeableText item = (MyChangeableText) pTouchArea;
 			item.setColor(1f, 0.7f, 0.7f);
 			this.mDone.play();
 			item.registerEntityModifier(
 					new SequenceEntityModifier(
 							new IEntityModifierListener() {
 								@Override
 								public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 									item.setColor(1.0f, 1.0f, 0.6f);
 									MainMenu.this.manageTouch(pTouchArea);
 								}
 							},
 							new ScaleModifier(0.1f, 1f, 1.3f),
 							new ScaleModifier(0.1f, 1.3f, 1f)
 			));
 			return true;
 		}
 		return false;
 	}
 	
 	private void manageTouch(ITouchArea pTouchArea) {
 		MyChangeableText item = (MyChangeableText) pTouchArea;
 		if ((int)item.getY() == 422) {
 			Enviroment.instance().toggleDifficult();
 			if (Enviroment.instance().getDifficult() == 0)
 				item.setText("EASY");
 			else if (Enviroment.instance().getDifficult() == 1)
 				item.setText("NORMAL");
 			else
 				item.setText("HARD");
 			item.setPosition(Enviroment.CAMERA_WIDTH / 2 - item.getWidthScaled() / 2, item.getY());
 		} else if ((int)item.getY() == 422 + 210) {
 			try{
 				Enviroment.instance().getGame().startActivity(new Intent (Intent.ACTION_VIEW, Uri.parse("market://details?id=org.anddev.andengine.braingame")));
 			} catch (ActivityNotFoundException e) {
 			}
 		} else if ((int)item.getY() == 422 + 70) {
 			Enviroment.instance().setScene(new Start());
 		} else if ((int)item.getY() == 422 + 140) {
 			try {
 				Dashboard.open();
 			} catch (Exception e) {
 			
 			}
 		}
 	}
 	
 }
