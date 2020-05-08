 package com.testgame.sprite;
 
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.sprite.ButtonSprite;
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.text.AutoWrap;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import android.util.Log;
 
 import com.testgame.resource.ResourcesManager;
 
 public class GameDialogBox {
 	
 	private HUD hud;
 	
 	private ButtonSprite[] buttons;
 
 	private Sprite backgroundSprite;
 	
 	private Text messageText;
 	
 	private float textHeight;
 	
 	private float j;
 	
 	private boolean text;
 	
 	
 	public GameDialogBox(HUD hud, String message, int back, boolean text, ButtonSprite ... buttons) {
 		super();
 		this.buttons = buttons;	
 		this.hud = hud;
 		this.text = text;
 		
 		ResourcesManager resourcesManager = ResourcesManager.getInstance();
 		
 		// Attach Background
 		switch (back){ 
 			
 			case 1:
 				hud.attachChild(backgroundSprite = new Sprite(240, 400, resourcesManager.dialog_background, resourcesManager.vbom));
 				break;
 				
 			case 2:
 				if(resourcesManager.dialog_background2 == null){
 					Log.d("Null", "null");
 				}
 				
 				hud.attachChild(backgroundSprite = new Sprite(240, 400, resourcesManager.dialog_background2, resourcesManager.vbom));
 				break;
 				
 			case 3:
 				hud.attachChild(backgroundSprite = new Sprite(240, 400, resourcesManager.dialog_background3, resourcesManager.vbom));
 				break;
 			default:
 				break;
 		}
 		if(text){
 			
 			messageText = new Text(240, 400, resourcesManager.cartoon_font_white, message, new TextOptions(AutoWrap.WORDS, backgroundSprite.getWidth()-100, HorizontalAlign.CENTER, Text.LEADING_DEFAULT), resourcesManager.vbom);
 			
 			hud.attachChild(messageText);
 			
 			messageText.setPosition(240, 400 + backgroundSprite.getHeight() / 2 - 75);
 			
 			
 			
			j = 400 + backgroundSprite.getHeight()/2 - 75 - messageText.getHeight();
 			
 		}
 		else {
 			float h = buttons[0].getHeight();
 			 j = 400 + ((buttons.length  * 100)/2);
 		}
 		
 		int i = 0;
 		for(ButtonSprite button : buttons){
 			hud.attachChild(button);
 			button.setPosition(240, j - (100*i));
 			hud.registerTouchArea(button);
 			i++;
 
 		}
 		
 		
 		
 		
 		
 		//hud.registerTouchArea(okayButton);
 	}
 
 	public void dismiss() {
 
 		ResourcesManager.getInstance().engine.runOnUpdateThread(new Runnable() {
 			@Override
 			public void run() {
 				hud.detachChild(backgroundSprite);
 
 				if(text)
 					hud.detachChild(messageText);
 
 
 				if (buttons != null) {
 					for(ButtonSprite button: buttons){
 						hud.detachChild(button);
 						hud.unregisterTouchArea(button);
 					}
 				}
 				
 
 			}
 		});
 	}
 
 	
 }
