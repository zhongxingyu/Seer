 package dragonsreign.scene;
 
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.scene.ITouchArea;
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
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.color.Color;
 
 import android.provider.OpenableColumns;
 import android.test.UiThreadTest;
 import android.util.Log;
 import android.widget.Toast;
 import dragonsreign.character.Enemy;
 import dragonsreign.character.PlayerCharacter;
 import dragonsreign.character.characterclass.ClericClass;
 import dragonsreign.character.characterclass.RangerClass;
 import dragonsreign.character.characterclass.WarriorClass;
 import dragonsreign.item.consumable.Potion;
 import dragonsreign.manager.SceneManager;
 import dragonsreign.manager.SceneManager.SceneType;
 import dragonsreign.util.AbilityData;
 import dragonsreign.util.BattleCharacterContainer;
 import dragonsreign.util.RandomNumber;
 import dragonsreign.util.enums.ABILITYFLAGS;
 import dragonsreign.util.enums.ENEMIES;
 import dragonsreign.util.enums.POTIONS;
 
 public class BattleScene extends BaseScene implements IOnMenuItemClickListener {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private enum BUTTONS
 	{
 		ABILITIES(0),
 		ITEMS(1),
 		SWAP(2),
 		FLEE(3),
 		BASIC_ATTACK(4),
 		SKILL_ONE(5),
 		SKILL_TWO(6),
 		SKILL_THREE(7),
 		SKILL_FOUR(8),
 		SKILL_FIVE(9),
 		ITEM_1(10),
 		ITEM_2(11),
 		ITEM_3(12),
 		ITEM_4(13),
 		ITEM_5(14),
 		ITEM_6(15);
 		
 		private final int value;
 
 		private BUTTONS(final int newValue) {
 			value = newValue;
 		}
 
 		private final int getValue() {
 			return value;
 
 		}
 	}
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	/////////////////
 	// Classes/enums
 	/////////////////
 	private MenuScene battleMenuChildScene, abilitiesChildScene,
 			itemsChildScene;
 
 	private BattleCharacterContainer partyMem[], enemyPlyr[];
 	private BattleCharacterContainer focusedPartyMem;
 	private BattleCharacterContainer abilityTarget;
 
 	private ABILITYFLAGS targetFlag;
 
 	private AbilityData abilityData;
 
 	private Sprite teamMember1, teamMember2, teamMember3, enemy1, enemy2,
 			enemy3, leftArrow1, leftArrow2, leftArrow3, rightArrow1,
 			rightArrow2, rightArrow3, focusArrow, exitButton;
 
 	private ScaleMenuItemDecorator abilitiesButton, itemsButton, swapButton,
 			fleeButton, basicAttack, skillOne, skillTwo, skillThree, skillFour,
 			skillFive, item1, item2, item3, item4, item5, item6;
 
 	private Text abilitiesText, itemsText, swapText, fleeText, basicAttackText,
 			skillOneText, skillTwoText, skillThreeText, skillFourText,
 			skillFiveText, item1Text, item2Text, item3Text, item4Text,
 			item5Text, item6Text, teamMember1Info, teamMember2Info,
 			teamMember3Info, enemy1Info, enemy2Info, enemy3Info;
 
 	private BoundCamera mcamera;
 	
 	private Sprite background;
 
 	/////////////////
 	// Basic datatypes
 	/////////////////
 	private int focusPlyrIdx, enemyIdx, enemyCount;
 
 	private Boolean playerTurn, firstEnemyUpdatePass;
 	
 	//Access to game
 	GameScene parent;
 	
 	// Ability String Data to output
 	private String[] plyrAbilities;
 	private String abilityUser, ability, target;
 	
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	BattleScene(GameScene pParentScene) {
 		super();
 		parent = pParentScene;
 	}
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public void createScene() {
 		// Set up a camera
 		camera.setChaseEntity(null);
 		camera.offsetCenter(camera.getCenterX() * -1, camera.getCenterY() * -1);
 		
 		mcamera = new BoundCamera(0, 0, ((DragonsReignActivity)activity).CAMERA_WIDTH, ((DragonsReignActivity)activity).CAMERA_HEIGHT);
 	    
 		//////////////////////
 		// Create Background
 		//////////////////////
 		createBattleView();
 		
 		//////////////////////
 	    // Create ChildScenes
 	    //////////////////////
 		battleMenuChildScene = new MenuScene(mcamera);
 		abilitiesChildScene = new MenuScene(mcamera);
 		itemsChildScene = new MenuScene(mcamera);
 		
 		//////////////////////
 		// Set First Child Scene
 		//////////////////////
 		setChildScene(battleMenuChildScene);
 		
 		//////////////////////
 		// Add On Menu Item Click Listeners For Each Child Scene
 		//////////////////////
 		battleMenuChildScene.setOnMenuItemClickListener(this);
 		abilitiesChildScene.setOnMenuItemClickListener(this);
 		itemsChildScene.setOnMenuItemClickListener(this);
 		
 		//////////////////////
 		// Create players of the battle
 		//////////////////////
 		
 		// Our party members
 		partyMem = new BattleCharacterContainer[3];
 		enemyPlyr = new BattleCharacterContainer[3];
 		
 		// The currently focused party member
 		focusedPartyMem = new BattleCharacterContainer();
 		
 		// The receiver of an ability
 		abilityTarget  = new BattleCharacterContainer();
 		
 		// TODO
 		// This needs to bring in currently active party members
 		if (((DragonsReignActivity) activity).getPartyMember(0) != null) {
 			partyMem[0] = new BattleCharacterContainer(
 					((DragonsReignActivity) activity).getPartyMember(0),
 					(DragonsReignActivity) activity);
 		}
 		if (((DragonsReignActivity) activity).getPartyMember(1) != null) {
 			partyMem[1] = new BattleCharacterContainer(
 					((DragonsReignActivity) activity).getPartyMember(1));
 		}
 		if (((DragonsReignActivity) activity).getPartyMember(2) != null) {
 			partyMem[2] = new BattleCharacterContainer(
 					((DragonsReignActivity) activity).getPartyMember(2));
 		}
 		// Generates random enemies based on our zone and gives us a count
 		generateRandomEnemies();
 		
 		// Don't give focus to a non-existent or dead party member when we start
 		// a new battle
 		
 		// If first party member exists
 		if (partyMem[0] != null) {
 			// First party member is not dead
 			if (!partyMem[0].isDead()) {
 				// Receive focus
 				focusPlyrIdx = 0;
 			} else if (partyMem[0].isDead()) {
 				// First party member is dead, pass focus
 
 				// If second party member exists
 				if (partyMem[1] != null) {
 					// Second party member is not dead
 					if (!partyMem[1].isDead()) {
 						// Receive focus
 						focusPlyrIdx = 1;
 					} else if (partyMem[1].isDead()) {
 						// Second party member is dead, pass focus
 
 						// If third party member exists
 						if (partyMem[2] != null) {
 							// Third party member is not dead
 							if (!partyMem[2].isDead()) {
 								// Receive focus
 								focusPlyrIdx = 2;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// The focused party member is what was determined by the conditions
 		// above
 		focusedPartyMem = partyMem[focusPlyrIdx];
 	
 		//////////////////////
 		// Create touch areas
 		//////////////////////
 		createTouchAreas();
 		
 		updateInfoText();
 		
 		//////////////////////
 		//Create the Child Scenes
 		//////////////////////
 		
 		createBattleMenuView();
 		createAbilitiesMenuView();
 		createItemsMenuView();
 		
 		playerTurn = true;
		// Determine who is going to go first
		hasteCheck();
		//;
 		if(playerTurn){
 			focusArrow.setVisible(true);
 			playerTurn();
 		}
 
 		// If it's not our turn right away, hide the buttons
 		if(!playerTurn){
 			battleMenuChildScene.setVisible(false);
 		}
 		
 		// Everything has been set up at this point, enter our battle
 		//BattleLoop();
 		
 		//Initialize enemyIdx
 		enemyIdx = 0;
 		firstEnemyUpdatePass = true;
 		
 		
 		registerUpdateHandler(new IUpdateHandler() {
 			
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				// TODO Auto-generated method stub
 
 				if (!playerTurn) {
 					if(enemyIdx == 0){
 						enemyTurn();
 					}
 					// Battle Menu visible = false
 					battleMenuChildScene.setVisible(false);
 					battleMenuChildScene.setOnMenuItemClickListener(null);
 					// Focus arrow visible = false
 					focusArrow.setVisible(false);
 					
 					
 					if(firstEnemyUpdatePass){
 						//Exits the update loop to clear the screen
 						firstEnemyUpdatePass = false;
 						return;
 					}
 
 					if (enemyPlyr[enemyIdx] != null && !enemyPlyr[enemyIdx].isDead()
 							&& enemyPlyr[enemyIdx].hasTurn()) {
 						int randAbilityIdx = 0;
 						int randPlyrTarget = 0;
 
 						// Random attack
 						abilityData = new AbilityData();
 						randAbilityIdx = RandomNumber.generateRandomInt(0, 1);
 						targetFlag = enemyPlyr[enemyIdx].useAbility(randAbilityIdx,
 								abilityData);
 
 						abilityUser = enemyPlyr[enemyIdx].getName();
 
 						plyrAbilities = enemyPlyr[enemyIdx].getAbilityNames();
 						ability = plyrAbilities[randAbilityIdx];
 
 						// Random Target
 						do {
 							randPlyrTarget = RandomNumber.generateRandomInt(0,
 									2);
 						} while (partyMem[randPlyrTarget] == null
 								|| partyMem[randPlyrTarget].isDead());
 
 						abilityTarget = partyMem[randPlyrTarget];
 						target = partyMem[randPlyrTarget].getName();
 						// ApplyDamage
 						applyAbilityData();
 
 						try {
 							Thread.sleep(3000);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						
 						
 
 					}
 					
 					enemyIdx++;
 					
 					if(enemyIdx == 3){
 						playerTurn();
 						firstEnemyUpdatePass = true;
 						enemyIdx = 0;
 						//backToGame();
 					}
 				}
 				
 				
 				//TODO
 				if(checkEnemyDeath()){
 					playerWinsBattle();
 				}else if (checkPlayerDeath()){
 					playerLosesBattle();
 				}
 			}
 
 			@Override
 			public void reset() {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 	
 	
 	
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
 		if (pMenuItem.getID() == BUTTONS.ABILITIES.getValue()) {
 			setChildScene(abilitiesChildScene);
 			
 			// Exit button available for this scene in order to return
 			exitButton.setVisible(true);
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEMS.getValue()) {
 			setChildScene(itemsChildScene);
 			
 			// Exit button available for this scene in order to return
 			exitButton.setVisible(true);
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SWAP.getValue()) {
 			// Gets us to the next party member with an available turn
 			swap();
 
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.FLEE.getValue()) {
 			// Determines if we can flee the battle
 			flee();
 			
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.BASIC_ATTACK.getValue()) {
 			if(focusedPartyMem.getLevel() >= 1) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 0
 				targetFlag = focusedPartyMem.useAbility(0, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[0];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 1 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SKILL_ONE.getValue()) {
 			if(focusedPartyMem.getLevel() >= 1) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 1
 				targetFlag = focusedPartyMem.useAbility(1, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[1];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 1 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SKILL_TWO.getValue()) {
 			if(focusedPartyMem.getLevel() >= 10) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 2
 				targetFlag = focusedPartyMem.useAbility(2, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[2];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 10 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SKILL_THREE.getValue()) {
 			if(focusedPartyMem.getLevel() >= 10) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 3
 				targetFlag = focusedPartyMem.useAbility(3, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[3];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 10 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SKILL_FOUR.getValue()) {
 			if(focusedPartyMem.getLevel() >= 20) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 4
 				targetFlag = focusedPartyMem.useAbility(4, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[4];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 20 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.SKILL_FIVE.getValue()) {
 			if(focusedPartyMem.getLevel() >= 20) {
 				abilityData = new AbilityData();
 				
 				// Determine what we just did in the battle using ability 5
 				targetFlag = focusedPartyMem.useAbility(5, abilityData);
 				
 				// String data for messages
 				abilityUser = focusedPartyMem.getName();
 				ability = plyrAbilities[5];
 				clearTargetSelection();
 				targetSelect();
 			} else {
 				writeToScreen("Must be level 20 to use this ability.");
 			}
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_1.getValue()) {
 			// TODO
 			// Add functionality for items after inventory completed
 			
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_2.getValue()) {
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_3.getValue()) {
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_4.getValue()) {
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_5.getValue()) {
 			return true;
 		} else if (pMenuItem.getID() == BUTTONS.ITEM_6.getValue()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public void onBackKeyPressed() {
 		// TODO Auto-generated method stub
 		//SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_GAME);
 		backToGame();
 
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
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	// Creates touch areas where sprites are on the screen
 	public void createTouchAreas() {
 		exitButton = new Sprite(0, 0, resourcesManager.exitButton, this.engine.getVertexBufferObjectManager())
 		{
 	        @Override
 	        public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
 	        {
 	        	switch (pSceneTouchEvent.getAction()) 
 	        	{
 	            	case TouchEvent.ACTION_DOWN:
 	            		setChildScene(battleMenuChildScene);
 	            		
 	            		exitButton.setVisible(false);
 					break;
 
 	            }
 	            return true;
 	       
 	        }
 		};
 		
 		if (partyMem[0] != null) {
 			//teamMember1 = partyMem1.getCharacter().getSprite();
 			teamMember1 = new Sprite(0, 0, partyMem[0].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						
 						abilityTarget = partyMem[0];
 						clearTargetSelection();
 						target = partyMem[0].getName();
 						
 						applyAbilityData();
 						break;
 	
 					}
 					return true;
 	
 				}
 			};
 		}
 
 		if (partyMem[1] != null) {
 			// teamMember2 = partyMem2.getCharacter().getSprite();
 			teamMember2 = new Sprite(0, 0, partyMem[1].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						
 						abilityTarget = partyMem[1];
 						clearTargetSelection();
 						target = partyMem[1].getName();
 						
 						applyAbilityData();
 						
 						break;
 	
 					}
 					return true;
 	
 				}
 			};
 		}
 
 		if (partyMem[2] != null) {
 			// teamMember3 = partyMem3.getCharacter().getSprite();
 			teamMember3 = new Sprite(0, 0, partyMem[2].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						
 						abilityTarget = partyMem[2];
 						clearTargetSelection();
 						target = partyMem[2].getName();
 						
 						applyAbilityData();
 						break;
 	
 					}
 					return true;
 	
 				}
 			};
 		}
 
 		if (enemyPlyr[0] != null) {
 			// enemy1 = enemyPlyr1.getCharacter().getSprite();
 			enemy1 = new Sprite(0, 0, enemyPlyr[0].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						
 						abilityTarget = enemyPlyr[0];
 						clearTargetSelection();
 						target = enemyPlyr[0].getName();
 
 						applyAbilityData();
 						break;
 
 					}
 					return true;
 
 				}
 			};
 			
 		}
 		
 		if (enemyPlyr[1] != null) {
 			// enemy2 = enemyPlyr2.getCharacter().getSprite();
 			enemy2 = new Sprite(0, 0, enemyPlyr[1].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 
 						abilityTarget = enemyPlyr[1];
 						clearTargetSelection();
 						target = enemyPlyr[1].getName();
 						
 						applyAbilityData();
 						break;
 
 					}
 					return true;
 
 				}
 			};
 		}
 		
 		if (enemyPlyr[2] != null) {
 			// enemy3 = enemyPlyr3.getCharacter().getSprite();
 			enemy3 = new Sprite(0, 0, enemyPlyr[2].getCharacter().getSprite(),
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 
 						abilityTarget = enemyPlyr[2];
 						clearTargetSelection();
 						target = enemyPlyr[2].getName();
 
 						applyAbilityData();
 						break;
 
 					}
 					return true;
 
 				}
 			};
 		}
 		
 		leftArrow1 = new Sprite(0, 0, resourcesManager.leftArrow1, this.engine.getVertexBufferObjectManager());
 		leftArrow2 = new Sprite(0, 0, resourcesManager.leftArrow2, this.engine.getVertexBufferObjectManager());
 		leftArrow3 = new Sprite(0, 0, resourcesManager.leftArrow3, this.engine.getVertexBufferObjectManager());
 		
 		rightArrow1 = new Sprite(0, 0, resourcesManager.rightArrow1, this.engine.getVertexBufferObjectManager());
 		rightArrow2 = new Sprite(0, 0, resourcesManager.rightArrow2, this.engine.getVertexBufferObjectManager());
 		rightArrow3 = new Sprite(0, 0, resourcesManager.rightArrow3, this.engine.getVertexBufferObjectManager());
 		
 		focusArrow = new Sprite(0, 0, resourcesManager.focusArrow, this.engine.getVertexBufferObjectManager());
 		
 		registerTouchArea(exitButton);
 
 		// Set position, scale, and visibility
 		exitButton.setPosition(-12, 305);
 		exitButton.setScale(0.5f);
 		exitButton.setVisible(false);
 		
 		focusArrow.setPosition(225, (focusPlyrIdx * 100) + 25);
 		focusArrow.setVisible(false);
 		
 		// Players
 		if (partyMem[0] != null) {
 			teamMember1Info = new Text(-10,-10, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.RIGHT), vbom);
 			teamMember1Info.setScale(.66f);
 			
 			teamMember1.setPosition(125, 0);// 150
 			leftArrow1.setPosition(225, 25);
 			leftArrow1.setVisible(false);
 		}
 		if (partyMem[1] != null) {
 			teamMember2Info = new Text(-10,90, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.RIGHT), vbom);
 			teamMember2Info.setScale(.66f);
 			
 			teamMember2.setPosition(125, 100);
 			leftArrow2.setPosition(225, 125);
 			leftArrow2.setVisible(false);
 		}
 		if (partyMem[2] != null) {
 			teamMember3Info = new Text(-10,190, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.RIGHT), vbom);
 			teamMember3Info.setScale(.66f);
 			
 			teamMember3.setPosition(125, 200);
 			leftArrow3.setPosition(225, 225);
 			leftArrow3.setVisible(false);
 		}
 
 		// Enemies
 		if (enemyPlyr[0] != null) {
 			enemy1Info = new Text(650,10, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.LEFT), vbom);
 			enemy1Info.setScale(.66f);
 			
 			enemy1.setPosition(578, 0);// 650
 			rightArrow1.setPosition(511, 25);
 			rightArrow1.setVisible(false);
 		}
 		if (enemyPlyr[1] != null) {
 			enemy2Info = new Text(650,110, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.LEFT), vbom);
 			enemy2Info.setScale(.66f);
 			
 			enemy2.setPosition(578, 100);
 			rightArrow2.setPosition(511, 125);
 			rightArrow2.setVisible(false);
 		}
 		if (enemyPlyr[2] != null) {
 			enemy3Info = new Text(650,210, resourcesManager.battleFont, "", 200, new TextOptions(HorizontalAlign.LEFT), vbom);
 			enemy3Info.setScale(.66f);
 			
 			enemy3.setPosition(578, 200);
 			rightArrow3.setPosition(511, 225);
 			rightArrow3.setVisible(false);
 		}
 		
 		// Attach children
 		attachChild(exitButton);
 		
 		if (partyMem[0] != null) {
 			attachChild(teamMember1);
 			attachChild(leftArrow1);
 			attachChild(teamMember1Info);
 		}
 		if (partyMem[1] != null) {
 			attachChild(teamMember2);
 			attachChild(leftArrow2);
 			attachChild(teamMember2Info);
 		}
 		if (partyMem[2] != null) {
 			attachChild(teamMember3);
 			attachChild(leftArrow3);
 			attachChild(teamMember3Info);
 		}
 
 		if (enemyPlyr[0] != null) {
 			attachChild(enemy1);
 			attachChild(rightArrow1);
 			attachChild(enemy1Info);
 		}
 		if (enemyPlyr[1] != null) {
 			attachChild(enemy2);
 			attachChild(rightArrow2);
 			attachChild(enemy2Info);
 		}
 		if (enemyPlyr[2] != null) {
 			attachChild(enemy3);
 			attachChild(rightArrow3);
 			attachChild(enemy3Info);
 		}
 		
 		attachChild(focusArrow);
 	}
 	
 	// Sets up the background view
 	public void createBattleView() {
 		//setBackground(new Background(Color.CYAN));
 		background = new Sprite(0, 0, resourcesManager.plainsBattleBackground, vbom)
 		{
     	    @Override
     	    protected void preDraw(GLState pGLState, Camera pCamera) 
     	    {
     	       super.preDraw(pGLState, pCamera);
     	       pGLState.enableDither();
     	    }
     	};
     	background.setPosition(0, 0);
     	attachChild(background);
 	}
 	
 	public void createBattleMenuView() {
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
 	}
 	
 	public void createAbilitiesMenuView() {
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
 		plyrAbilities = focusedPartyMem.getAbilityNames();
 		
 		basicAttackText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillOneText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillTwoText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillThreeText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillFourText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 		skillFiveText = new Text(20,10, resourcesManager.battleFont, "" ,150, new TextOptions(), vbom);
 
 		updateAbilityButtons();
 		
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
 	
 	public void createItemsMenuView() {
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
 		// TODO
 		// Update Item text when inventory is done
 		
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
 	
 	// Ease of use function to create a toast onto the screen based on a string
 	// and how long it lasts (0 or 1 length)
 	// Consider moving elsewhere for other places to use this
 	public void writeToScreen(final CharSequence pText) {
 		
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 					Toast.makeText(activity, pText, Toast.LENGTH_SHORT).show();
 				
 			}
 		});
 	}
 	
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 	
 	// Generates random enemies and based on where we are and what our luck of
 	// the draw was
 	private void generateRandomEnemies(){
 		// Create the enemies scaled to the players
 		int accumulativeHealth, accumulativeDmg, accumulativeArmor = 0;
 		
 		if(partyMem[2] != null) {
 			accumulativeHealth = partyMem[0].getCharacter().getMaxResources().getHealth() + partyMem[1].getCharacter().getMaxResources().getHealth() + partyMem[2].getCharacter().getMaxResources().getHealth();
 			accumulativeDmg = partyMem[0].getCharacter().getCurrentStats().getDamage() + partyMem[1].getCharacter().getCurrentStats().getDamage() + partyMem[2].getCharacter().getCurrentStats().getDamage();
 			accumulativeArmor = partyMem[0].getCharacter().getCurrentStats().getArmor() + partyMem[1].getCharacter().getCurrentStats().getArmor() + partyMem[2].getCharacter().getCurrentStats().getArmor();
 			
 		} else {
 			accumulativeHealth = partyMem[0].getCharacter().getMaxResources().getHealth() + partyMem[1].getCharacter().getMaxResources().getHealth();
 			accumulativeDmg = partyMem[0].getCharacter().getCurrentStats().getDamage() + partyMem[1].getCharacter().getCurrentStats().getDamage();
 			accumulativeArmor = partyMem[0].getCharacter().getCurrentStats().getArmor() + partyMem[1].getCharacter().getCurrentStats().getArmor();
 			
 		}
 		
 		int plyrLvl = partyMem[0].getLevel();
 		
 		// Generates a random amount of enemies, in a range of 1-3
		//enemyCount = 3;
		enemyCount = RandomNumber.generateRandomInt(1, 3);
 		
 		// If we have an enemy, we fill out their array, otherwise they are null
 		switch (enemyCount) {
 		case 1:
 			enemyPlyr[0] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 			enemyPlyr[1] = null;
 			enemyPlyr[2] = null;
 			
 			break;
 		case 2:
 			enemyPlyr[0] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 			enemyPlyr[1] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 			enemyPlyr[2] = null;
 			
 			break;
 		case 3:
 			enemyPlyr[0] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 			enemyPlyr[1] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 			enemyPlyr[2] = new BattleCharacterContainer(new Enemy(plyrLvl, accumulativeHealth, accumulativeDmg, accumulativeArmor, ENEMIES.values()[RandomNumber.generateRandomInt(0, 6)]));
 
 			break;
 		}
 	}
 	
 	// Updates the battle scene info of players and enemies
 	private void updateInfoText() {
 		if(partyMem[0] != null){
 			teamMember1Info.setText(partyMem[0].getName() + "\nLvl: " + partyMem[0].getLevel() + "\nHP: " + partyMem[0].getCurrentHealth() + " / " + partyMem[0].getMaxHealth() + "\nRP: " + partyMem[0].getCurrentResource() + " / " + partyMem[0].getMaxResource());
 		}
 		if(partyMem[1] != null){
 			teamMember2Info.setText(partyMem[1].getName() + "\nLvl: " + partyMem[1].getLevel() + "\nHP: " + partyMem[1].getCurrentHealth() + " / " + partyMem[1].getMaxHealth() + "\nRP: " + partyMem[1].getCurrentResource() + " / " + partyMem[1].getMaxResource());
 		}
 		if(partyMem[2] != null){
 			teamMember3Info.setText(partyMem[2].getName() + "\nLvl: " + partyMem[2].getLevel() + "\nHP: " + partyMem[2].getCurrentHealth() + " / " + partyMem[2].getMaxHealth() + "\nRP: " + partyMem[2].getCurrentResource() + " / " + partyMem[2].getMaxResource());
 		}
 		
 		if(enemyPlyr[0] != null){
 			enemy1Info.setText(enemyPlyr[0].getName() + "\nLvl: " + enemyPlyr[0].getLevel() + "\nHP: " + enemyPlyr[0].getCurrentHealth() + " / " + enemyPlyr[0].getMaxHealth());
 			//Log.e("Enemy 1", enemyPlyr[0].getName() + " damage = " + enemyPlyr[0].getCharacter().getCurrentStats().getDamage());
 			//Log.e("Enemy 1", enemyPlyr[0].getName() + " armor = " + enemyPlyr[0].getCharacter().getCurrentStats().getArmor());
 		}
 		if(enemyPlyr[1] != null){
 			enemy2Info.setText(enemyPlyr[1].getName() + "\nLvl: " + enemyPlyr[1].getLevel() + "\nHP: " + enemyPlyr[1].getCurrentHealth() + " / " + enemyPlyr[1].getMaxHealth());
 			//Log.e("Enemy 2", enemyPlyr[1].getName() + " damage = " + enemyPlyr[1].getCharacter().getCurrentStats().getDamage());
 			//Log.e("Enemy 2", enemyPlyr[1].getName() + " armor = " + enemyPlyr[1].getCharacter().getCurrentStats().getArmor());
 		}
 		if(enemyPlyr[2] != null){
 			enemy3Info.setText(enemyPlyr[2].getName() + "\nLvl: " + enemyPlyr[2].getLevel() + "\nHP: " + enemyPlyr[2].getCurrentHealth() + " / " + enemyPlyr[2].getMaxHealth());
 			//Log.e("Enemy 3", enemyPlyr[2].getName() + " damage = " + enemyPlyr[2].getCharacter().getCurrentStats().getDamage());
 			//Log.e("Enemy 3", enemyPlyr[2].getName() + " armor = " + enemyPlyr[2].getCharacter().getCurrentStats().getArmor());
 		}
 
 	}
 	
 	// Update the abilities accordingly to each character class
 	private void updateAbilityButtons() {
 		plyrAbilities = focusedPartyMem.getAbilityNames();
 
 		basicAttackText.setText(plyrAbilities[0]);
 		skillOneText.setText(plyrAbilities[1]);
 		skillTwoText.setText(plyrAbilities[2]);
 		skillThreeText.setText(plyrAbilities[3]);
 		skillFourText.setText(plyrAbilities[4]);
 		skillFiveText.setText(plyrAbilities[5]);
 	}
 	
 	private void hasteCheck() {
 		// Determine the turn order based on the haste level
 		playerTurn = ((PlayerCharacter)(focusedPartyMem.getCharacter())).compareHasteToEnemy((Enemy) enemyPlyr[0].getCharacter());
 		
 		if(playerTurn){
 			// Player had a higher or equivalent haste
 			writeToScreen("Your turn.");
 			focusArrow.setVisible(true);
 		} else {
 			// Enemy had a higher or equivalent haste
 			writeToScreen("Enemy's turn.");
 		}
 	}
 	
 	private void BattleLoop() {
 		// Infinite loop when we've finally reached the battle
 		// Kicks out during a player or enemy turn if win/loss conditions are
 		// met
 		while (true) {
 			if (playerTurn) {
 				// Show the battle menu if it's our turn
 				battleMenuChildScene.setVisible(true);
 
 				playerTurn();
 			} else {
 				// Hide the battle menu if it's the enemy's turn
 				battleMenuChildScene.setVisible(false);
 
 				enemyTurn();
 			}
 		}
 	}
 	
 	private void playerTurn() {
 		//set playerTurn to true
 		playerTurn = true;
 		
 		
 		for (int idx = 0; idx < 3; idx++) {
 			if (partyMem[idx] != null && !partyMem[idx].isDead()) {
 				//set turns to true
 				partyMem[idx].setHasTurn(true);
 				//Apply BattleEffects
 				partyMem[idx].ApplyBattleEffects();
 				//Add Resources (Max - current * .20f)
 				partyMem[idx].getCharacter().addResource();
 			}
 		}
 		
 		//Battle Menu visible = true
 		setChildScene(battleMenuChildScene);
 		battleMenuChildScene.setVisible(true);
 		battleMenuChildScene.setOnMenuItemClickListener(this);
 		//focus arrow = true
 		focusArrow.setVisible(true);
 		focusArrow.setPosition(225, (focusPlyrIdx * 100) + 25);
 		focusedPartyMem = partyMem[focusPlyrIdx];
 		
 		updateAbilityButtons();		
 				
 		updateInfoText();
 	}
 	
 	private void enemyTurn() {
 		//TODO
 //		//Battle Menu visible = false
 //		battleMenuChildScene.setVisible(false);
 //		//Focus arrow visible = false
 //		focusArrow.setVisible(false);
 		
 		
 		for (int idx = 0; idx < 3; idx++) {
 			if (enemyPlyr[idx] != null && !enemyPlyr[idx].isDead()) {
 				//Set enemy turn to true
 				enemyPlyr[idx].setHasTurn(true);
 				//Apply BattleEffects
 				enemyPlyr[idx].ApplyBattleEffects();
 				
 			}
 		}
 		
 //		playerTurn = false;
 		
 		
 //		writeToScreen("Enemy Turn", 1);
 		
 //		for (int idx = 0; idx < enemyCount; idx++) {
 //			if (enemyPlyr[idx] != null && !enemyPlyr[idx].isDead() && enemyPlyr[idx].hasTurn()) {
 //				int randAbilityIdx = 0;
 //				int randPlyrTarget = 0;
 //				
 //				// Random attack
 //				AbilityData abilityData = new AbilityData();
 //				randAbilityIdx = RandomNumber.generateRandomInt(0, 1);
 //				targetFlag = enemyPlyr[idx].useAbility(randAbilityIdx, abilityData);
 //				
 //				abilityUser = enemyPlyr[idx].getName();
 //				
 //				plyrAbilities = enemyPlyr[idx].getAbilityNames();
 //				ability = plyrAbilities[randAbilityIdx];
 //				
 //				// Random Target
 //				do{
 //					randPlyrTarget = RandomNumber.generateRandomInt(0, 2);
 //				} while( partyMem[randPlyrTarget] == null || partyMem[randPlyrTarget].isDead());
 //				
 //				abilityTarget = partyMem[randPlyrTarget];
 //				target = partyMem[randPlyrTarget].getName();
 //				// ApplyDamage
 //				applyAbilityData();
 //				try {
 //					Thread.sleep(2000);
 //				} catch (InterruptedException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //				
 //			
 //				
 //			}
 //		}
 	
 //		playerTurn();
 	}
 	
 	// Allows the touch areas and targets to become active based on the ability
 	private void targetSelect() {
 		switch(targetFlag) {
 		// Buffing all party members requires no touch event
 		case BUFF_ALL:
 			applyAbilityData();
 			break;
 			
 		// Damaging all enemies requires no touch event
 		case DAMAGE_ALL:
 			applyAbilityData();
 			break;
 			
 		// Dealing single target damage requires an enemy to be touched
 		case DAMAGE_HEAL_SINGLE:
 		case DAMAGE_SINGLE:
 
 			if (enemyPlyr[0] != null && !enemyPlyr[0].isDead()) {
 				registerTouchArea(enemy1);
 				rightArrow1.setVisible(true);
 			}
 			if (enemyPlyr[1] != null && !enemyPlyr[1].isDead()) {
 				registerTouchArea(enemy2);
 				rightArrow2.setVisible(true);
 			}
 			if (enemyPlyr[2] != null && !enemyPlyr[2].isDead()) {
 				registerTouchArea(enemy3);
 				rightArrow3.setVisible(true);
 			}
 
 			break;
 			
 		// Unused for demo
 		case DEBUFF:
 			// applyAbilityData();
 			break;
 
 		// Healing all party members requires no touch event
 		case HEAL_ALL:
 			applyAbilityData();
 			break;
 			
 		// Dealing a single target heal requires a party member to be touched
 		case HEAL_SINGLE:
 		
 			// Party member exists & they are not dead & current health is less
 			// than max health, then they can be healed
 			if (partyMem[0] != null && !partyMem[0].isDead() && (partyMem[0].getCurrentHealth() < partyMem[0].getMaxHealth())) {
 				registerTouchArea(teamMember1);
 				leftArrow1.setVisible(true);
 			}
 
 			if (partyMem[1] != null && !partyMem[1].isDead() && (partyMem[1].getCurrentHealth() < partyMem[1].getMaxHealth())) {
 				registerTouchArea(teamMember2);
 				leftArrow2.setVisible(true);
 			}
 
 			if (partyMem[2] != null && !partyMem[2].isDead() && (partyMem[2].getCurrentHealth() < partyMem[2].getMaxHealth())) {
 				registerTouchArea(teamMember3);
 				leftArrow3.setVisible(true);
 			}
 			
 			break;
 			
 		// Not enough resource sends us back to the main battle scene with a
 		// message
 		case NOT_ENOUGH_RESOURCE:
 			writeToScreen("Insufficient resource.");
 			setChildScene(battleMenuChildScene);
 			break;
 			
 		// Reviving requires a party member to be touched
 		case REVIVE:
 			if (partyMem[0] != null && partyMem[0].isDead()) {
 				registerTouchArea(teamMember1);
 				leftArrow1.setVisible(true);
 			}
 
 			if (partyMem[1] != null && partyMem[1].isDead()) {
 				registerTouchArea(teamMember2);
 				leftArrow2.setVisible(true);
 			}
 
 			if (partyMem[2] != null && partyMem[2].isDead()) {
 				registerTouchArea(teamMember3);
 				leftArrow3.setVisible(true);
 			}
 			
 			break;
 			
 		// Unused for demo
 		case SELF_CAST:
 			break;
 		default:
 			break;
 		}
 	}
 	
 	// Clears any active touch areas or targets
 	private void clearTargetSelection(){
 		if(partyMem[0] != null){
 			unregisterTouchArea(teamMember1);			
 		}
 		
 		if(partyMem[1] != null){
 			unregisterTouchArea(teamMember2);
 			
 		}
 		if(partyMem[2] != null){
 			unregisterTouchArea(teamMember3);
 			
 		}
 		if(enemyPlyr[0] != null){
 			unregisterTouchArea(enemy1);
 			
 		}
 		if(enemyPlyr[1] != null){
 			unregisterTouchArea(enemy2);
 			
 		}
 		if(enemyPlyr[2] != null){
 			unregisterTouchArea(enemy3);
 			
 		}
 		
 		leftArrow1.setVisible(false);
 		leftArrow2.setVisible(false);
 		leftArrow3.setVisible(false);
 		
 		rightArrow1.setVisible(false);
 		rightArrow2.setVisible(false);
 		rightArrow3.setVisible(false);
 	}
 
 	// Applies ability data to player or enemy 
 	private void applyAbilityData(){
 		// If we pressed a registered touch area, apply this type of ability data
 		switch (targetFlag) {
 		// Buff all team members
 		case BUFF_ALL:
 			// As long as a party member exists and they aren't dead, then they
 			// receive the buff
 			if (partyMem[0] != null && !partyMem[0].isDead()) {
 				partyMem[0].recieveAbilityData(abilityData);
 			}
 
 			if (partyMem[1] != null && !partyMem[1].isDead()) {
 				partyMem[1].recieveAbilityData(abilityData);
 
 			}
 			if (partyMem[2] != null && !partyMem[2].isDead()) {
 				partyMem[2].recieveAbilityData(abilityData);
 
 			}
 			
 			writeToScreen(abilityUser + " used " + ability + " on party.");
 			break;
 		
 		// AOE damage to all enemies
 		case DAMAGE_ALL:
 			// As long as an enemy exists and they aren't dead, then they
 			// receive the AOE damage
 			if (enemyPlyr[0] != null && !enemyPlyr[0].isDead()) {
 				enemyPlyr[0].recieveAbilityData(abilityData);
 			}
 			if (enemyPlyr[1] != null && !enemyPlyr[1].isDead()) {
 				enemyPlyr[1].recieveAbilityData(abilityData);
 			}
 			if (enemyPlyr[2] != null && !enemyPlyr[2].isDead()) {
 				enemyPlyr[2].recieveAbilityData(abilityData);
 			}
 			
 			writeToScreen(abilityUser + " used " + ability + " on all enemies.");
 			break;
 
 		// Life steal mechanic
 		case DAMAGE_HEAL_SINGLE:
 			abilityTarget.recieveAbilityData(abilityData);
 
 			// Heal for half of the damage done
 			abilityData.setHealingDone((abilityData.getDamageDone() / 2));
 			abilityData.setHealed(true);
 			abilityData.setDamageDone(0);
 
 			focusedPartyMem.recieveAbilityData(abilityData);
 
 			writeToScreen(abilityUser + " used " + ability + " on " + target + ". ");
 			break;
 			
 		// Deal damage to a single target
 		case DAMAGE_SINGLE:
 			abilityTarget.recieveAbilityData(abilityData);
 			
 			writeToScreen(abilityUser + " used " + ability + " on " + target + ".");
 			break;
 		
 		// Unused for demo, will apply in full game
 		case DEBUFF:
 			break;
 			
 		// Heals all party members
 		case HEAL_ALL:
 			// As long as a party member exists and they aren't dead, then they
 			// receive the heal
 			if (partyMem[0] != null && !partyMem[0].isDead()) {
 				partyMem[0].recieveAbilityData(abilityData);
 			}
 
 			if (partyMem[1] != null && !partyMem[1].isDead()) {
 				partyMem[1].recieveAbilityData(abilityData);
 			}
 
 			if (partyMem[2] != null && !partyMem[2].isDead()) {
 				partyMem[2].recieveAbilityData(abilityData);
 			}
 
 			writeToScreen(abilityUser + " used " + ability + " on party.");
 			break;
 
 		// Heals a single party member
 		case HEAL_SINGLE:
 			abilityTarget.recieveAbilityData(abilityData);
 			
 			writeToScreen(abilityUser+ " used " + ability + " on " + target + ".");
 			break;
 		case NOT_ENOUGH_RESOURCE:
 			break;
 		
 		// Revives a fallen party member
 		case REVIVE:
 			// Rather than having a potion and a spell perform the same
 			// functionality, we use a new potion that we create that revives
 			// the user to half health
 			abilityTarget.useItem(new Potion(POTIONS.BASIC_REVIVE_POTION));
 			
 			writeToScreen(abilityUser+ " used " + ability + " on " + target + ".");
 			break;
 			
 		// Unused for demo, will apply in full game
 		case SELF_CAST:
 			break;
 		default:
 			break;
 
 		}
 		
 		
 		////////////////////////////////////////////
 		// AT THIS POINT, WE DETERMINE THE GAME FLOW 
 		////////////////////////////////////////////
 		
 		// If it's the player's turn, then the battle menu is active
 		if(playerTurn){
 			battleMenuChildScene.setVisible(true);
 			exitButton.setVisible(false);
 			
 			setChildScene(battleMenuChildScene);
 			//Remove Resources
 			focusedPartyMem.getCharacter().useResource(abilityData.getResourceUsed());
 			swap();
 		} 
 		
 		// Updates our current stats and resources to reflect what just happened
 		updateInfoText();
 		clearTargetSelection();
 		
 		//TODO
 		//Death Checks
 		//Toast when someone dies
 		//Win/loss stuff
 	}
 	
 	// Handles current player turns
 	private void swap() {
 		// Gets set to 0 every time swap() is called
 		int plyrsWithoutTurn = 0;
 		
 		// Determine the focused player and who has a turn
 		do {
 			// Iterate the focus
 			focusPlyrIdx += 1;
 			
 			// If the index is higher than the third member, set it back to the first
 			if (focusPlyrIdx > ((DragonsReignActivity)activity).getPartySize() - 1)
 				focusPlyrIdx = 0;
 			
 			// our focused party member is set to whoever our focused index value is
 			focusedPartyMem = partyMem[focusPlyrIdx];
 		
 			// Add on to the players without a turn
 			plyrsWithoutTurn++;
 		} while (plyrsWithoutTurn < 4 && partyMem[focusPlyrIdx] != null && !(focusedPartyMem.hasTurn()) || focusedPartyMem.isDead() );
 
 		// If all players have exhausted their turn, it's now the enemy's turn
 		if(plyrsWithoutTurn == 4){
 			playerTurn = false;		
 			// It is now the enemy's turn
 //			enemyTurn();
 		} else {
 			// If we got in here, that means the player turn is still happening
 			
 			// Show updated buttons
 			updateAbilityButtons();
 			
 			// Show whose turn it is
 			focusArrow.setVisible(true);
 			focusArrow.setPosition(225, (focusPlyrIdx * 100) + 25);
 		}
 	}
 	
 	// Whether or not we can escape the current battle
 	private void flee() {
 		// TODO
 		// Have a check if we are in a boss battle
 		// If we are, do not allow the player to flee
 		
 		int avgEnemyLevel = 0;
 
 		// Figure out how many enemies we have
 		for (int enemyIdx = 0; enemyIdx < enemyCount; enemyIdx++) {
 
 			// Add on the to average level with the enemy player level if they
 			// exist
 			if (enemyPlyr[enemyIdx] != null)
 				avgEnemyLevel += enemyPlyr[enemyIdx].getLevel();
 		}
 
 		// Divide the average enemy level by how many there are
 		avgEnemyLevel /= enemyCount;
 		
 		// Choose a random number from 1 to 100
 		int fleeChanceCalc = RandomNumber.generateRandomInt(1, 100);
 		
 		// Start out with a 0% chance to flee
 		int chanceToFlee = 0;
 		
 		// Base all of the calculation on the first party member
 		if (partyMem[0].getLevel() > avgEnemyLevel) {
 			// 75% chance to flee
 			chanceToFlee = 100;
 		} else if (partyMem[0].getLevel() == avgEnemyLevel) {
 			// 50% chance to flee
 			chanceToFlee = 100;
 		} else if (partyMem[0].getLevel() < avgEnemyLevel) {
 			// 25% chance to flee
 			chanceToFlee = 100;
 		}
 
 		// If our random number is less than or equal to the chance we have to
 		// flee (based on the average enemy level)
 		if (fleeChanceCalc <= chanceToFlee) {
 			writeToScreen("Successfully fled the battle.");
 
 			// Take us back to the zone
 			onBackKeyPressed();
 		} else {
 			writeToScreen("Failed to flee the battle.");
 
 			// We failed to flee the battle and have lost our turn globally
 			if(partyMem[0] != null) {
 				partyMem[0].setHasTurn(false);
 			}
 			
 			if(partyMem[1] != null) {
 				partyMem[1].setHasTurn(false);
 			}
 			
 			if(partyMem[2] != null) {
 				partyMem[2].setHasTurn(false);
 			}
 			
 			playerTurn = false;
 		}
 	}
 
 	private void backToGame() {
 
 		parent.goBackToGame();
 	}
 	
 	private boolean checkPlayerDeath(){
 		
 		int plrsDead = 0;
 		
 		for(int idx = 0; idx < 3; idx++){
 			if(partyMem[idx] == null || partyMem[idx].isDead()){
 				plrsDead += 1;
 			}
 		}
 		
 		if(plrsDead == 3){
 			return true;
 		} else{
 			return false;
 		}
 			
 	}
 	
 	private boolean checkEnemyDeath(){
 		
 		int enemyDead = 0;
 		
 		for(int idx = 0; idx < enemyCount; idx++){
 			if(enemyPlyr[idx].isDead()){
 				enemyDead += 1;
 			}
 		}
 		
 		if(enemyDead == enemyCount){
 			return true;
 		} else{
 			return false;
 		}
 	}
 	
 	
 	private void playerWinsBattle(){
 		
 		int expEarned = 0;
 		for(int idx = 0; idx < enemyCount; idx++){
 			expEarned += ((Enemy)enemyPlyr[idx].getCharacter()).getExperience();
 		}
 		
 		for(int idx = 0; idx < ((DragonsReignActivity)activity).getPartySize(); idx++){
 			if(partyMem[idx] != null){
 				if( !partyMem[idx].isDead()){
 					//Add Experience
 				}else{
 					//Add 1/2 experience
 				}
 				
 			}
 		}
 		
 		//ChildScene with gear and stuff
 		//Add health/resources once characters are persistent
 		
 		
 	}
 	
 	private void playerLosesBattle(){
 		writeToScreen("You lost the battle.");
 		//Back to start
 	}
 	
 }
