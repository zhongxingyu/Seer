 package kr.ggogun.gamebasic.source;
 
 import java.util.ArrayList;
 
 import kr.ggogun.gamebasic.constants.BasicConstants;
 import kr.ggogun.gamebasic.constants.PointConstants;
 
 import org.cocos2d.layers.CCLayer;
 import org.cocos2d.menus.CCMenu;
 import org.cocos2d.menus.CCMenuItemImage;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCSprite;
 
 import android.util.Log;
 
 public class TutorialLayer extends CCLayer{
 	
 	private ArrayList<CCSprite> backgroundList = new ArrayList<CCSprite>();
 	
 	private CCMenuItemImage backButton;
 	private CCMenuItemImage nextButton;
 	private CCMenuItemImage preButton;
 	
 	private int depth=0;
 	private int length =-1;
 	
 	public TutorialLayer(){
 		super();
 		
 		
 		
 		for(String backgroundName: BasicConstants.TUTORIAL_IMG_LIST){
			backgroundList.add(CCSprite.sprite(BasicConstants.TUTORIAL_IMG_LIST[length+1]));
 			length++;
 		}
 		backButton = CCMenuItemImage.item(BasicConstants.BACK_BUTTON_IMG, BasicConstants.BACK_BUTTON_IMG, this, "backCallback");
 		
 		preButton = CCMenuItemImage.item(BasicConstants.PRE_BUTTON_IMG, BasicConstants.PRE_BUTTON_IMG, this, "preCallback");
 		nextButton = CCMenuItemImage.item(BasicConstants.NEXT_BUTTON_IMG, BasicConstants.NEXT_BUTTON_IMG, this, "nextCallback");
 		
 
 		for(CCSprite background: backgroundList){
 			background.setPosition(PointConstants.CENTER_POINT);
 		}
 		backButton.setPosition(PointConstants.BACK_BUTTON_POSITION);
 		preButton.setPosition(PointConstants.PREVIOUS_BUTTON_POSITION);
 		nextButton.setPosition(PointConstants.NEXT_BUTTON_POSITION);
 		
 		CCMenu menu = CCMenu.menu(backButton, nextButton, preButton);
 		
 		
 		menu.setPosition(0, 0);
 		for(CCSprite background: backgroundList){
			addChild(background);
 			background.setOpacity(0);
 		}
		addChild(menu);
 		checkButtonVisible();
 		
 	}
 	
 	public void backCallback(Object sender){
 		
 		Log.d("PBS","pop");
 		CCDirector.sharedDirector().popScene();
 	}
 	
 	public void preCallback(Object sender){
 		
 		if(depth > 0){
 			backgroundList.get(depth).setOpacity(0);
 			depth--;
 			backgroundList.get(depth).setOpacity(255);
 		}
 		
 		checkButtonVisible();
 		
 		
 	}
 	
 	
 	public void nextCallback(Object sender){
 		
 		if(depth < length){
 			backgroundList.get(depth).setOpacity(0);
 			depth++;
 			backgroundList.get(depth).setOpacity(255);
 		}
 		
 		
 		checkButtonVisible();
 		
 	}
 	
 	private void checkButtonVisible() {
 		
 		if(depth == 0){
 			backgroundList.get(0).setOpacity(255);
 			preButton.setVisible(false);
 			nextButton.setVisible(true);
 		}else if(depth == length){
 			preButton.setVisible(true);
 			nextButton.setVisible(false);
 		}else{
 			preButton.setVisible(true);
 			nextButton.setVisible(true);
 		}
 		
 	}
 

 }
