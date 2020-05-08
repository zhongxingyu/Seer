 package dragonsreign.scene;
 
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.color.Color;
 
 import dragonsreign.scene.BaseScene;
 import dragonsreign.character.characterclass.WarriorClass;
 import dragonsreign.manager.SceneManager;
 import dragonsreign.manager.SceneManager.SceneType;
 
 public class BattleScene extends BaseScene implements IOnMenuItemClickListener
 {
 
 	
 	private MenuScene battleMenuChildScene, abilitiesChildScene, itemsChildScene;
 	private Rectangle character1HealthBar, character2HealthBar;
 
 	private Sprite teamMember1, teamMember2, teamMember3, enemy1, enemy2, enemy3,  leftArrow1, leftArrow2, leftArrow3,
 	  rightArrow1, rightArrow2, rightArrow3;
 	
 	private ScaleMenuItemDecorator abilitiesButton, itemsButton, swapButton, 
 								   fleeButton,basicAttack, skillOne, skillTwo, 
 						 		   skillThree, skillFour, skillFive,
 						 		   item1, item2, item3, item4, item5, item6;
 
 
 	private Text abilitiesText, itemsText,
 	 			 swapText, fleeText, 
 	 			 basicAttackText, skillOneText, skillTwoText,
 	 			 skillThreeText, skillFourText,skillFiveText,
 	 			 item1Text,item2Text, item3Text, item4Text,
 	 			 item5Text, item6Text, teamMember1CurrentHp, teamMember2CurrentHp, teamMember3CurrentHp, enemy1CurrentHp, enemy2CurrentHp, enemy3CurrentHp,
 	 			teamMember1MaxHp, teamMember2MaxHp, teamMember3MaxHp, enemy1MaxHp, enemy2MaxHp, enemy3MaxHp, teamMember1CurrentRes, teamMember2CurrentRes,
 	 			teamMember3CurrentRes, teamMember1MaxRes, teamMember2MaxRes, teamMember3MaxRes, teamMember1Name, teamMember2Name, teamMember3Name, enemy1Name,
 	 			enemy2Name, enemy3Name, teamMember1Lvl, teamMember2Lvl, teamMember3Lvl, enemy1Lvl, enemy2Lvl, enemy3Lvl;
 	private BoundCamera mcamera;
 	
 	private enum BUTTONS
 	{
 		ABILITIES(0),
 		ITEMS(1),
 		SWAP(2) ,
 		FLEE(3) ,
 		BASIC_ATTACK(4) ,
 		SKILL_ONE(5) ,
 		SKILL_TWO(6) ,
 		SKILL_THREE(7) ,
 		SKILL_FOUR(8) ,
 		SKILL_FIVE(9) ,
 		ITEM_1(10) ,
 		ITEM_2(11) ,
 		ITEM_3(12) ,
 		ITEM_4(13) ,
 		ITEM_5(14) ,
 		ITEM_6(15);
 		
 		private final int value;
 		
 		private BUTTONS(final int newValue) 
 		{
             value = newValue;
         }
 
         private final int getValue() 
         { 
         	return value; 
         	
         }
 	}
 
 	
 	
 	@Override
 	public void createScene() 
 	{
 		mcamera = new BoundCamera(0, 0, ((DragonsReignActivity)activity).CAMERA_WIDTH, ((DragonsReignActivity)activity).CAMERA_HEIGHT);
 	    /////////////////////////////////////////////////////////////////////////////////////
 	    //Create ChildScenes
 	    /////////////////////////////////////////////////////////////////////////////////////
 		battleMenuChildScene = new MenuScene(mcamera);
 		abilitiesChildScene = new MenuScene(mcamera);
 		itemsChildScene = new MenuScene(mcamera);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Set First Child Scene
 		/////////////////////////////////////////////////////////////////////////////////////
 		setChildScene(battleMenuChildScene);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Add On Menu Item Click Listeners For Each Child Scene
 		/////////////////////////////////////////////////////////////////////////////////////
 		battleMenuChildScene.setOnMenuItemClickListener(this);
 		abilitiesChildScene.setOnMenuItemClickListener(this);
 		itemsChildScene.setOnMenuItemClickListener(this);
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Player and Enemy Sprites
 		/////////////////////////////////////////////////////////////////////////////////////
 		teamMember1 = new Sprite(0, 0, resourcesManager.teamMember1, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
                 		//attachChild(leftArrow1);
                 		if (leftArrow1.isVisible()){
                 			leftArrow1.setVisible(false);
                 		}else leftArrow1.setVisible(true);
 //                		
 //                		
                 		break;
                 	
 
                 }
                 return true;
            
             }
         };
 		teamMember2 = new Sprite(0, 0, resourcesManager.teamMember2, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
                 		//attachChild(leftArrow2);
                 		if (leftArrow2.isVisible()){
                 			leftArrow2.setVisible(false);
                 		}else leftArrow2.setVisible(true);
                 	
                 	
 
                 }
                 return true;
            
             }
         };
 		teamMember3 = new Sprite(0, 0, resourcesManager.teamMember3, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
                 		//attachChild(leftArrow3);
                 		if (leftArrow3.isVisible()){
                 			leftArrow3.setVisible(false);
                 		}else leftArrow3.setVisible(true);
 //                		
                 		break;
                 	
 
                 }
                 return true;
            
             }
         };
 		enemy1 = new Sprite(0, 0, resourcesManager.enemy1, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
                 		//attachChild(rightArrow1);
                 		if (rightArrow1.isVisible()){
                 			rightArrow1.setVisible(false);
                 		}else rightArrow1.setVisible(true);
                 		
                 		break;
                 	
 
                 }
                 return true;
            
             }
         };
 		enemy2 = new Sprite(0, 0, resourcesManager.enemy2, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
                 		//attachChild(rightArrow2);
                 		if (rightArrow2.isVisible()){
                 			rightArrow2.setVisible(false);
                 		}else rightArrow2.setVisible(true);
                 		
                 		break;
                 	
 
                 }
                 return true;
            
             }
         };
 		enemy3 = new Sprite(0, 0, resourcesManager.enemy3, this.engine.getVertexBufferObjectManager()){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
             {
             	switch (pSceneTouchEvent.getAction()) 
             	{
                 	case TouchEvent.ACTION_DOWN:
 					
                 		//rightArrow3.setVisible(true);
 //					// 	attachChild(rightArrow3);
                 		if (rightArrow3.isVisible()){
                 			rightArrow3.setVisible(false);
                 		}else rightArrow3.setVisible(true);
 //                		
                 			
                 			
                 		break;
                 	
 
                 }
                 return true;
            
             }
         };
 		
 		leftArrow1 = new Sprite(0, 0, resourcesManager.leftArrow1, this.engine.getVertexBufferObjectManager());
 		leftArrow2 = new Sprite(0, 0, resourcesManager.leftArrow2, this.engine.getVertexBufferObjectManager());
 		leftArrow3 = new Sprite(0, 0, resourcesManager.leftArrow3, this.engine.getVertexBufferObjectManager());
 		
 		rightArrow1 = new Sprite(0, 0, resourcesManager.rightArrow1, this.engine.getVertexBufferObjectManager());
 		rightArrow2 = new Sprite(0, 0, resourcesManager.rightArrow2, this.engine.getVertexBufferObjectManager());
 		rightArrow3 = new Sprite(0, 0, resourcesManager.rightArrow3, this.engine.getVertexBufferObjectManager());
 		
 		//TODO add names and levels to each
 		teamMember1Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1Name.setText("Warrior ");
 		teamMember1Name.setScale((float) 0.5);
 		teamMember1Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1Lvl.setText(" Lvl: 1");
 		teamMember1Lvl.setScale((float) 0.5);
 		teamMember1CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1CurrentHp.setText("100 /");
 		teamMember1CurrentHp.setScale((float).5);
 		teamMember1MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1MaxHp.setText("160");
 		teamMember1MaxHp.setScale((float).5);
 		teamMember1CurrentRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1CurrentRes.setText("100 /");
 		teamMember1CurrentRes.setScale((float).5);
 		teamMember1MaxRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember1MaxRes.setText("100");
 		teamMember1MaxRes.setScale((float).5);
 				
 		teamMember2Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2Name.setText(" Cleric");
 		teamMember2Name.setScale((float) 0.5);
 		teamMember2Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2Lvl.setText("Lvl: 1");
 		teamMember2Lvl.setScale((float) 0.5);
 		teamMember2CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2CurrentHp.setText("100 /");
 		teamMember2CurrentHp.setScale((float) 0.5);
 		teamMember2MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2MaxHp.setText("160");
 		teamMember2MaxHp.setScale((float) 0.5);
 		teamMember2CurrentRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2CurrentRes.setText("100 /");
 		teamMember2CurrentRes.setScale((float) 0.5);
 		teamMember2MaxRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember2MaxRes.setText("100");
 		teamMember2MaxRes.setScale((float) 0.5);
 		
 		teamMember3Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3Name.setText("Ranger ");
 		teamMember3Name.setScale((float) 0.5);
 		teamMember3Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3Lvl.setText(" Lvl: 1");
 		teamMember3Lvl.setScale((float) 0.5);
 		teamMember3CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3CurrentHp.setText("100 /");
 		teamMember3CurrentHp.setScale((float) 0.5);
 		teamMember3MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3MaxHp.setText("160");
 		teamMember3MaxHp.setScale((float) 0.5);
 		teamMember3CurrentRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3CurrentRes.setText("100 /");
 		teamMember3CurrentRes.setScale((float) 0.5);
 		teamMember3MaxRes = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		teamMember3MaxRes.setText("100");
 		teamMember3MaxRes.setScale((float) 0.5);
 		
 		enemy1Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy1Name.setText("Enemy 1 ");
 		enemy1Name.setScale((float) 0.5);
 		enemy1Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy1Lvl.setText("Lvl: 1");
 		enemy1Lvl.setScale((float) 0.5);
 		enemy1CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy1CurrentHp.setText("100 /");
 		enemy1CurrentHp.setScale((float) 0.5);
 		enemy1MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy1MaxHp.setText("170");
 		enemy1MaxHp.setScale((float) 0.5);
 		
 		enemy2Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy2Name.setText("Enemy 2 ");
 		enemy2Name.setScale((float) 0.5);
 		enemy2Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy2Lvl.setText("Lvl: 1");
 		enemy2Lvl.setScale((float) 0.5);
 		enemy2CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy2CurrentHp.setText("100 /");
 		enemy2CurrentHp.setScale((float) 0.5);
 		enemy2MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy2MaxHp.setText("170");
 		enemy2MaxHp.setScale((float) 0.5);
 		
 		enemy3Name = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy3Name.setText("Enemy 3 ");
 		enemy3Name.setScale((float) 0.5);
 		enemy3Lvl = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy3Lvl.setText("Lvl: 1");
 		enemy3Lvl.setScale((float) 0.5);
 		enemy3CurrentHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy3CurrentHp.setText("100 /");
 		enemy3CurrentHp.setScale((float) 0.5);
 		enemy3MaxHp = new Text(0,0, resourcesManager.battleFont, "", 150, new TextOptions(HorizontalAlign.RIGHT), vbom);
 		enemy3MaxHp.setText("170");
 		enemy3MaxHp.setScale((float) 0.5);
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Set Team Member and Enemy Positions
 		/////////////////////////////////////////////////////////////////////////////////////
 		teamMember1.setPosition(150, 0);
 		teamMember2.setPosition(150, 100);		
 		teamMember3.setPosition(150, 200);
 		enemy1.setPosition(650, 0);
 		enemy2.setPosition(650, 100);
 		enemy3.setPosition(650, 200);
 		
 		leftArrow1.setPosition(225, 25);
 		leftArrow1.setVisible(false);
 		leftArrow2.setPosition(225, 125);
 		leftArrow2.setVisible(false);
 		leftArrow3.setPosition(225, 225);
 		leftArrow3.setVisible(false);
 		
 		rightArrow1.setPosition(575, 25);
 		rightArrow1.setVisible(false);
 		rightArrow2.setPosition(575, 125);
 		rightArrow2.setVisible(false);
 		rightArrow3.setPosition(575, 225);
 		rightArrow3.setVisible(false);
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		//Text positions
 		////////////////////////////////////////////////////////////////////////////////////
 		teamMember1Name.setPosition(30, 10);
 		teamMember1Lvl.setPosition(80, 10);
 		teamMember1CurrentHp.setPosition(50, 40); 
 		teamMember1MaxHp.setPosition(100, 40);
 		teamMember1CurrentRes.setPosition(50, 70);
 		teamMember1MaxRes.setPosition(100, 70); 
 		
 		teamMember2Name.setPosition(30, 110);
 		teamMember2Lvl.setPosition(80, 110);
 		teamMember2CurrentHp.setPosition(50, 140);
 		teamMember2MaxHp.setPosition(100, 140);
 		teamMember2CurrentRes.setPosition(50, 170);
 		teamMember2MaxRes.setPosition(100, 170); 
 		
 		teamMember3Name.setPosition(30, 210);
 		teamMember3Lvl.setPosition(80, 210);
 		teamMember3CurrentHp.setPosition(50, 240);
 		teamMember3MaxHp.setPosition(100, 240); 
 		teamMember3CurrentRes.setPosition(50, 270); 
 		teamMember3MaxRes.setPosition(100, 270); 
 		
 		enemy1Name.setPosition(710, 10);
 		enemy1Lvl.setPosition(780, 10);
 		enemy1CurrentHp.setPosition(730, 40);
 		enemy1MaxHp.setPosition(780, 40); 
 		
 		enemy2Name.setPosition(710, 110);
 		enemy2Lvl.setPosition(780, 110);
 		enemy2CurrentHp.setPosition(730, 140);
 		enemy2MaxHp.setPosition(780, 140); 
 		
 		enemy3Name.setPosition(710, 210);
 		enemy3Lvl.setPosition(780, 210);
 		enemy3CurrentHp.setPosition(730, 240); 
 		enemy3MaxHp.setPosition(780, 240); 
 		 
 		
 		
 		 
 		
 		
 		
 		
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		//Register the Touch Areas
 		////////////////////////////////////////////////////////////////////////////////////
 		registerTouchArea(teamMember1);
 		registerTouchArea(teamMember2);
 		registerTouchArea(teamMember3);
 		registerTouchArea(enemy1);
 		registerTouchArea(enemy2);
 		registerTouchArea(enemy3);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Sprites to the Screen
 		/////////////////////////////////////////////////////////////////////////////////////
 		attachChild(teamMember1);
 		attachChild(teamMember2);
 		attachChild(teamMember3);
 		attachChild(enemy1);
 		attachChild(enemy2);
 		attachChild(enemy3);
 	
 		attachChild(leftArrow1);
 		attachChild(leftArrow2);
 		attachChild(leftArrow3);
 		attachChild(rightArrow1);
 		attachChild(rightArrow2);
 		attachChild(rightArrow3);
 		
 		attachChild(teamMember1Name);
 		attachChild(teamMember1Lvl);
 		attachChild(teamMember1CurrentHp);
 		attachChild(teamMember1MaxHp);
 		attachChild(teamMember1CurrentRes);
 		attachChild(teamMember1MaxRes);
 		
 		attachChild(teamMember2Name);
 		attachChild(teamMember2Lvl);
 		attachChild(teamMember2CurrentHp);
 		attachChild(teamMember2MaxHp);
 		attachChild(teamMember2CurrentRes);
 		attachChild(teamMember2MaxRes);
 		
 		attachChild(teamMember3Name);
 		attachChild(teamMember3Lvl);
 		attachChild(teamMember3CurrentHp);
 		attachChild(teamMember3MaxHp);
 		attachChild(teamMember3CurrentRes);
 		attachChild(teamMember3MaxRes);
 		
 		attachChild(enemy1Name);
 		attachChild(enemy1Lvl);
 		attachChild(enemy1CurrentHp);
 		attachChild(enemy1MaxHp);
 		
 		attachChild(enemy2Name);
 		attachChild(enemy2Lvl);
 		attachChild(enemy2CurrentHp);
 		attachChild(enemy2MaxHp);
 		
 		attachChild(enemy3Name);
 		attachChild(enemy3Lvl);
 		attachChild(enemy3CurrentHp);
 		attachChild(enemy3MaxHp);
 		
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create the Child Scenes
 		/////////////////////////////////////////////////////////////////////////////////////
 		createBattleView();
 		createBattleMenuView();
 		createAbilitiesMenuView();
 		createItemsMenuView();
 	
 	}
 
 	public void createBattleView()
 	{
 	
 		setBackground(new Background(Color.BLUE));
 		
 	}
 	public void createBattleMenuView()
 	{
 		
 		battleMenuChildScene.setPosition(0, 300);
 		battleMenuChildScene.setBackgroundEnabled(false);
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Battle Menu Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		abilitiesButton = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ABILITIES.getValue(), resourcesManager.abilitiesButton, vbom), 1.0f, 1);
 		itemsButton = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEMS.getValue(), resourcesManager.itemsButton, vbom), 1.2f, 1);
 		swapButton = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SWAP.getValue(), resourcesManager.swapButton, vbom), 1.2f, 1);
 		fleeButton = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.FLEE.getValue(), resourcesManager.fleeButton, vbom), 1.2f, 1);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Button Texts
 		/////////////////////////////////////////////////////////////////////////////////////
 		abilitiesText = new Text(40,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		abilitiesText.setText("Abilities");
 		
 		itemsText = new Text(40,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		itemsText.setText("Items");
 		
 		swapText = new Text(40,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		swapText.setText("Swap");
 		
 		fleeText = new Text(40,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		fleeText.setText("Flee");
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Button Texts to Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		abilitiesButton.attachChild(abilitiesText);
 		itemsButton.attachChild(itemsText);
 		swapButton.attachChild(swapText);
 		fleeButton.attachChild(fleeText);
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Set Button Positions
 		/////////////////////////////////////////////////////////////////////////////////////
 		abilitiesButton.setPosition(100, 20);
 		itemsButton.setPosition(500, 20);
 		swapButton.setPosition(100, 100);
 		fleeButton.setPosition(500, 100);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Buttons to Child Scene
 		/////////////////////////////////////////////////////////////////////////////////////
 		battleMenuChildScene.addMenuItem(abilitiesButton);
 		battleMenuChildScene.addMenuItem(itemsButton);
 		battleMenuChildScene.addMenuItem(swapButton);
 		battleMenuChildScene.addMenuItem(fleeButton);
 		
 		
 		battleMenuChildScene.setVisible(true);
 
 	}
 	public void createAbilitiesMenuView()
 	{
 
 		abilitiesChildScene.setPosition(0, 300);
 		abilitiesChildScene.setBackgroundEnabled(false);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Battle Menu Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		basicAttack = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.BASIC_ATTACK.getValue(), resourcesManager.basicAttackButton, vbom), 1.2f, 1);
 		skillOne = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SKILL_ONE.getValue(), resourcesManager.skillOneButton, vbom), 1.2f, 1);
 		skillTwo = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SKILL_TWO.getValue(), resourcesManager.skillTwoButton, vbom), 1.2f, 1);
 		skillThree = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SKILL_THREE.getValue(), resourcesManager.skillThreeButton, vbom), 1.2f, 1);
 		skillFour = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SKILL_FOUR.getValue(), resourcesManager.skillFourButton, vbom), 1.2f, 1);
 		skillFive = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.SKILL_FIVE.getValue(), resourcesManager.skillFiveButton, vbom), 1.2f, 1);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Button Texts
 		/////////////////////////////////////////////////////////////////////////////////////
 		basicAttackText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		basicAttackText.setText("Attack");
 		
 		skillOneText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillOneText.setText("Skill One");
 		
 		skillTwoText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillTwoText.setText("Skill Two");
 		
 		skillThreeText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillThreeText.setText("Skill Three");
 		
 		skillFourText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillFourText.setText("Skill Four");
 		
 		skillFiveText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillFiveText.setText("Skill Five");
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Button Texts to Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		basicAttack.attachChild(basicAttackText);
 		skillOne.attachChild(skillOneText);
 		skillTwo.attachChild(skillTwoText);
 		skillThree.attachChild(skillThreeText);
 		skillFour.attachChild(skillFourText);
 		skillFive.attachChild(skillFiveText);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Set Button Positions
 		/////////////////////////////////////////////////////////////////////////////////////
 		basicAttack.setPosition(50, 20);
 		skillOne.setPosition(50, 100);
 		skillTwo.setPosition(300, 20);
 		skillThree.setPosition(300, 100);
 		skillFour.setPosition(550, 20);
 		skillFive.setPosition(550, 100);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Buttons to Child Scene
 		/////////////////////////////////////////////////////////////////////////////////////
 		abilitiesChildScene.addMenuItem(basicAttack);
 		abilitiesChildScene.addMenuItem(skillOne);
 		abilitiesChildScene.addMenuItem(skillTwo);
 		abilitiesChildScene.addMenuItem(skillThree);
 		abilitiesChildScene.addMenuItem(skillFour);
 		abilitiesChildScene.addMenuItem(skillFive);
 
 	}
 	public void createItemsMenuView()
 	{
 		itemsChildScene.setPosition(0, 300);
 		itemsChildScene.setBackgroundEnabled(false);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Battle Menu Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		item1 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_1.getValue(), resourcesManager.basicAttackButton, vbom), 1.2f, 1);
 		item2 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_2.getValue(), resourcesManager.skillOneButton, vbom), 1.2f, 1);
 		item3 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_3.getValue(), resourcesManager.skillTwoButton, vbom), 1.2f, 1);
 		item4 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_4.getValue(), resourcesManager.skillThreeButton, vbom), 1.2f, 1);
 		item5 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_5.getValue(), resourcesManager.skillFourButton, vbom), 1.2f, 1);
 		item6 = new ScaleMenuItemDecorator(new SpriteMenuItem(BUTTONS.ITEM_6.getValue(), resourcesManager.skillFiveButton, vbom), 1.2f, 1);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Create Button Texts
 		/////////////////////////////////////////////////////////////////////////////////////
 		item1Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item1Text.setText("Item 1");
 		
 		item2Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item2Text.setText("Item 2");
 		
 		item3Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item3Text.setText("Item 3");
 		
 		item4Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item4Text.setText("Item 4");
 		
 		item5Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item5Text.setText("Item 5");
 		
 		item6Text = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		item6Text.setText("Item 6");
 		
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Button Texts to Buttons
 		/////////////////////////////////////////////////////////////////////////////////////
 		item1.attachChild(item1Text);
 		item2.attachChild(item2Text);
 		item3.attachChild(item3Text);
 		item4.attachChild(item4Text);
 		item5.attachChild(item5Text);
 		item6.attachChild(item6Text);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Set Button Positions
 		/////////////////////////////////////////////////////////////////////////////////////
 		item1.setPosition(50, 20);
 		item2.setPosition(50, 100);
 		item3.setPosition(300, 20);
 		item4.setPosition(300, 100);
 		item5.setPosition(550, 20);
 		item6.setPosition(550, 100);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		//Attach Buttons to Child Scene
 		/////////////////////////////////////////////////////////////////////////////////////
 		itemsChildScene.addMenuItem(item1);
 		itemsChildScene.addMenuItem(item2);
 		itemsChildScene.addMenuItem(item3);
 		itemsChildScene.addMenuItem(item4);
 		itemsChildScene.addMenuItem(item5);
 		itemsChildScene.addMenuItem(item6);
 	
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) 
 	{
 		if (pMenuItem.getID() == BUTTONS.ABILITIES.getValue()) 
 		{
 			setChildScene(abilitiesChildScene);
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEMS.getValue()) 
 		{
 			setChildScene(itemsChildScene);
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SWAP.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.FLEE.getValue()) 
 		{
 			onBackKeyPressed();
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.BASIC_ATTACK.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SKILL_ONE.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SKILL_TWO.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SKILL_THREE.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SKILL_FOUR.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.SKILL_FIVE.getValue()) 
 		{
 			setChildScene(battleMenuChildScene);
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEM_1.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEM_2.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEM_3.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEM_4.getValue()) 
 		{
 			return true;
 		}
 		else if (pMenuItem.getID() == BUTTONS.ITEM_5.getValue()) 
 		{
 			return true;
 		} 
 		else if (pMenuItem.getID() == BUTTONS.ITEM_6.getValue()) 
 		{
 			setChildScene(battleMenuChildScene);
 			return true;
 		} 
 		else 
 		{
 			return false;
 		}
 	}
 	
 	@Override
 	public void onBackKeyPressed() 
 	{
 		// TODO Auto-generated method stub
 		SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_GAME);
 		
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		// TODO Auto-generated method stub
 		return SceneType.SCENE_BATTLE;
 	}
 
 	@Override
 	public void disposeScene() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
