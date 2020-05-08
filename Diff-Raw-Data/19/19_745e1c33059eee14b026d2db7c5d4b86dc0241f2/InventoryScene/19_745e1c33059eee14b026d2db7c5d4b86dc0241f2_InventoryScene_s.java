 package dragonsreign.scene;
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.HorizontalAlign;
 
 import android.widget.Toast;
 
 import dragonsreign.scene.BaseScene;
 import dragonsreign.character.PlayerCharacter;
 import dragonsreign.character.characterclass.ClericClass;
 import dragonsreign.character.characterclass.RangerClass;
 import dragonsreign.character.characterclass.WarriorClass;
 import dragonsreign.manager.SceneManager;
 import dragonsreign.manager.SceneManager.SceneType;
 
 public class InventoryScene extends BaseScene {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private MenuScene inventoryChildScene;
 
 	private Sprite mPlayerPortrait[], inventoryArea, equipmentArea, statsArea,
 			exitButton;
 
 	private Rectangle mPlayerHealthBar[], mPlayerResourceBar[], mPlayerXpBar[];
 
 	private Text mPlayerInfo[];
 
 	private PlayerCharacter mPlayer[];
 	private int mPlayerSelected;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public void createScene() {
 		camera.setChaseEntity(null);
 		camera.offsetCenter(camera.getCenterX() * -1, camera.getCenterY() * -1);
 
 		inventoryChildScene = new MenuScene(camera);
 		setChildScene(inventoryChildScene);
 
 		// TODO
 		// use imported characters
 		mPlayer = new PlayerCharacter[3];
 
 		mPlayer[0] = new WarriorClass();
 		mPlayer[1] = new RangerClass();
 		mPlayer[2] = new ClericClass();
 
 		mPlayerSelected = 0;
 
 		// Got here
 		createTouchAreas();
 
 		// Create player one
 		if (mPlayer[0] != null) {
 			mPlayerPortrait[0].setPosition(0, 50);
 			attachChild(mPlayerPortrait[0]);
 
 			mPlayerHealthBar[0] = new Rectangle(0, 0, mPlayer[0].getHealthPercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerHealthBar[0].setColor(1.0f, 0, 0);
 
 			mPlayerResourceBar[0] = new Rectangle(0, 0, mPlayer[0].getResourcePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerResourceBar[0].setColor(0, 0, 1.0f);
 
 			mPlayerXpBar[0] = new Rectangle(0, 0, mPlayer[0].getExperiencePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerXpBar[0].setColor(0, 1.0f, 1.0f);
 
 			mPlayerHealthBar[0].setPosition(60, 50);
 			mPlayerResourceBar[0].setPosition(60, 70);
 			mPlayerXpBar[0].setPosition(60, 90);
 
 			attachChild(mPlayerHealthBar[0]);
 			attachChild(mPlayerResourceBar[0]);
 			attachChild(mPlayerXpBar[0]);
 
 		}
 
 		// Create player two
 		if (mPlayer[1] != null) {
			mPlayerPortrait[1].setPosition(174, 50);
 			attachChild(mPlayerPortrait[1]);
 
 			mPlayerHealthBar[1] = new Rectangle(0, 0, mPlayer[1].getHealthPercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerHealthBar[1].setColor(1.0f, 0, 0);
 
 			mPlayerResourceBar[1] = new Rectangle(0, 0, mPlayer[1].getResourcePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerResourceBar[1].setColor(0, 0, 1.0f);
 
 			mPlayerXpBar[1] = new Rectangle(0, 0, mPlayer[1].getExperiencePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerXpBar[1].setColor(0, 1.0f, 1.0f);
 
			mPlayerHealthBar[1].setPosition(238, 50);
			mPlayerResourceBar[1].setPosition(238, 70);
			mPlayerXpBar[1].setPosition(238, 90);
 
 			attachChild(mPlayerHealthBar[1]);
 			attachChild(mPlayerResourceBar[1]);
 			attachChild(mPlayerXpBar[1]);
 
 		}
 
 		// Create player three
 		if (mPlayer[2] != null) {
			mPlayerPortrait[2].setPosition(348, 50);
 			attachChild(mPlayerPortrait[2]);
 
 			mPlayerHealthBar[2] = new Rectangle(0, 0, mPlayer[2].getHealthPercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerHealthBar[2].setColor(1.0f, 0, 0);
 
 			mPlayerResourceBar[2] = new Rectangle(0, 0, mPlayer[2].getResourcePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerResourceBar[2].setColor(0, 0, 1.0f);
 
 			mPlayerXpBar[2] = new Rectangle(0, 0, mPlayer[2].getExperiencePercentage(), 20,
 					this.engine.getVertexBufferObjectManager());
 			mPlayerXpBar[2].setColor(0, 1.0f, 1.0f);
 
			mPlayerHealthBar[2].setPosition(412, 50);
			mPlayerResourceBar[2].setPosition(412, 70);
			mPlayerXpBar[2].setPosition(412, 90);
 
 			attachChild(mPlayerHealthBar[2]);
 			attachChild(mPlayerResourceBar[2]);
 			attachChild(mPlayerXpBar[2]);
 		}
 
 		// Exit button
 		exitButton.setPosition(-12, -12);
 		exitButton.setScale(0.5f);
 		attachChild(exitButton);
 
 		createInventoryChildScene();
 
 		inventoryChildScene.attachChild(mPlayerInfo[0]);
 		inventoryChildScene.attachChild(mPlayerInfo[1]);
 		inventoryChildScene.attachChild(mPlayerInfo[2]);
 		
 		// Update the inventory
 		updateInventory();
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_GAME);
 
 		camera.setChaseEntity(GameScene.player);
 		camera.updateChaseEntity();
 	}
 
 	@Override
 	public SceneType getSceneType() {
 
 		return null;
 	}
 
 	@Override
 	public void disposeScene() {
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	public void createInventoryChildScene() {
 		// Set up child scene
 		createPlayerStatsArea();
 
 		inventoryChildScene.setPosition(0, 140);
 
 		// Create sprites
 		statsArea = new Sprite(0, 0, resourcesManager.statsArea,
 				this.engine.getVertexBufferObjectManager());
 		equipmentArea = new Sprite(0, 0, resourcesManager.equipmentArea,
 				this.engine.getVertexBufferObjectManager());
 		inventoryArea = new Sprite(0, 0, resourcesManager.inventoryArea,
 				this.engine.getVertexBufferObjectManager());
 
 		// Attach entities
 		inventoryChildScene.attachChild(equipmentArea);
 		inventoryChildScene.attachChild(statsArea);
 		inventoryChildScene.attachChild(inventoryArea);
 
 		inventoryChildScene.setBackgroundEnabled(false);
 
 		// Set positions
 		statsArea.setPosition(0, 0);
 		equipmentArea.setPosition(210, 0);
 		inventoryArea.setPosition(420, 0);
 		
 		inventoryChildScene.setVisible(true);
 
 	}
 
 	public void createTouchAreas() {
 		mPlayerPortrait = new Sprite[3];
 		mPlayerHealthBar = new Rectangle[3];
 		mPlayerResourceBar = new Rectangle[3];
 		mPlayerXpBar = new Rectangle[3];
 		mPlayerInfo = new Text[3];
 
 		if (mPlayer[0] != null) {
 			mPlayerPortrait[0] = new Sprite(0, 0,
 					resourcesManager.character1Portrait,
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						mPlayerSelected = 0;
 
 						updateInventory();
 
 						break;
 
 					}
 					return true;
 
 				}
 			};
 
 			registerTouchArea(mPlayerPortrait[0]);
 		}
 
 		if (mPlayer[1] != null) {
 			mPlayerPortrait[1] = new Sprite(0, 0,
 					resourcesManager.character2Portrait,
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						mPlayerSelected = 1;
 
 						updateInventory();
 
 						// strengthText.setText("Str: " + 555);
 
 						break;
 
 					}
 					return true;
 
 				}
 			};
 
 			registerTouchArea(mPlayerPortrait[1]);
 		}
 
 		if (mPlayer[2] != null) {
 			mPlayerPortrait[2] = new Sprite(0, 0,
 					resourcesManager.character3Portrait,
 					this.engine.getVertexBufferObjectManager()) {
 				@Override
 				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 						final float pTouchAreaLocalX,
 						final float pTouchAreaLocalY) {
 					switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN:
 						mPlayerSelected = 2;
 
 						updateInventory();
 
 						// strengthText.setText("Str: " + 2000);
 						break;
 
 					}
 					return true;
 
 				}
 			};
 
 			registerTouchArea(mPlayerPortrait[2]);
 		}
 
 		exitButton = new Sprite(0, 0, resourcesManager.exitButton,
 				this.engine.getVertexBufferObjectManager()) {
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					onBackKeyPressed();
 
 					break;
 
 				}
 				return true;
 
 			}
 		};
 
 		registerTouchArea(exitButton);
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	private void updateInventory() {
 		// Name, level, str, dex, int, vit, dmg, armor
 		if (mPlayer[0] != null && mPlayerSelected == 0) {
 			mPlayerInfo[0].setText(mPlayer[0].getName() + "\n" + "Level: "
 					+ mPlayer[0].getLevel() + "\n" + "Str: "
 					+ mPlayer[0].getCurrentStats().getStrength() + "\n"
 					+ "Dex: " + mPlayer[0].getCurrentStats().getDexterity()
 					+ "\n" + "Int: "
 					+ mPlayer[0].getCurrentStats().getIntelligence() + "\n"
 					+ "Vit: " + mPlayer[0].getCurrentStats().getVitality()
 					+ "\n" + "Dmg: " + mPlayer[0].getCurrentStats().getDamage()
 					+ "\n" + "Armor: "
 					+ mPlayer[0].getCurrentStats().getArmor());
 			
 			mPlayerInfo[0].setVisible(true);
 			mPlayerInfo[1].setVisible(false);
 			mPlayerInfo[2].setVisible(false);
 
 		} else if (mPlayer[1] != null && mPlayerSelected == 1) {
 			mPlayerInfo[1].setText(mPlayer[1].getName() + "\n" + "Level: "
 					+ mPlayer[1].getLevel() + "\n" + "Str: "
 					+ mPlayer[1].getCurrentStats().getStrength() + "\n"
 					+ "Dex: " + mPlayer[1].getCurrentStats().getDexterity()
 					+ "\n" + "Int: "
 					+ mPlayer[1].getCurrentStats().getIntelligence() + "\n"
 					+ "Vit: " + mPlayer[1].getCurrentStats().getVitality()
 					+ "\n" + "Dmg: " + mPlayer[1].getCurrentStats().getDamage()
 					+ "\n" + "Armor: "
 					+ mPlayer[1].getCurrentStats().getArmor());
 
 			mPlayerInfo[0].setVisible(false);
 			mPlayerInfo[1].setVisible(true);
 			mPlayerInfo[2].setVisible(false);
 			
 		} else if (mPlayer[2] != null && mPlayerSelected == 2) {
 			mPlayerInfo[2].setText(mPlayer[2].getName() + "\n" + "Level: "
 					+ mPlayer[2].getLevel() + "\n" + "Str: "
 					+ mPlayer[2].getCurrentStats().getStrength() + "\n"
 					+ "Dex: " + mPlayer[2].getCurrentStats().getDexterity()
 					+ "\n" + "Int: "
 					+ mPlayer[2].getCurrentStats().getIntelligence() + "\n"
 					+ "Vit: " + mPlayer[2].getCurrentStats().getVitality()
 					+ "\n" + "Dmg: " + mPlayer[2].getCurrentStats().getDamage()
 					+ "\n" + "Armor: "
 					+ mPlayer[2].getCurrentStats().getArmor());
 
 			mPlayerInfo[0].setVisible(false);
 			mPlayerInfo[1].setVisible(false);
 			mPlayerInfo[2].setVisible(true);
 		}
 
 	}
 
 	private void createPlayerStatsArea() {
 		if (mPlayer[0] != null) {
 			mPlayerInfo[0] = new Text(0, 0, resourcesManager.inventoryFont,
 					"", 150, new TextOptions(HorizontalAlign.LEFT), vbom);
 
 			mPlayerInfo[0].setPosition(10, 10);
 
 			mPlayerInfo[0].setVisible(false);
 		}
 
 		if (mPlayer[1] != null) {
 			mPlayerInfo[1] = new Text(0, 0, resourcesManager.inventoryFont,
 					"", 150, new TextOptions(HorizontalAlign.LEFT), vbom);
 
 			mPlayerInfo[1].setPosition(10, 10);
 
 			mPlayerInfo[1].setVisible(false);
 			
 		}
 
 		if (mPlayer[2] != null) {
 			mPlayerInfo[2] = new Text(0, 0, resourcesManager.inventoryFont,
 					"", 150, new TextOptions(HorizontalAlign.LEFT), vbom);
 
 			mPlayerInfo[2].setPosition(10, 10);
 
 			mPlayerInfo[2].setVisible(false);
 		}
 
 	}
 
 	/*
 	 * private void showTeamMemberOneStatsArea() {
 	 * 
 	 * 
 	 * strengthText.setPosition(10, 30); dexterityText.setPosition(10, 80);
 	 * intelligenceText.setPosition(10, 130); vitalityText.setPosition(10, 180);
 	 * damageText.setPosition(10, 230); armorText.setPosition(10, 280);
 	 * 
 	 * 
 	 * inventoryChildScene.attachChild(strengthText);
 	 * inventoryChildScene.attachChild(dexterityText);
 	 * inventoryChildScene.attachChild(intelligenceText);
 	 * inventoryChildScene.attachChild(vitalityText);
 	 * inventoryChildScene.attachChild(damageText);
 	 * inventoryChildScene.attachChild(armorText);
 	 * 
 	 * }
 	 */
 
 }
