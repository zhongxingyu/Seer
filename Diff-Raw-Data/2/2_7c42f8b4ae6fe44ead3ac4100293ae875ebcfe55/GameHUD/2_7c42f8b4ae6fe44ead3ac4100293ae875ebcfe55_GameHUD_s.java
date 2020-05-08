 package com.angrykings;
 
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.Entity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.util.color.Color;
 
 /**
  * GameHUD
  * <p/>
  * Manages the HUD (head up display)
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 31.05.13
  */
 public class GameHUD extends HUD {
 	private final AnimatedSprite aimButton;
 	private final AnimatedSprite whiteFlagButton;
 
 	private final Font statusFont;
 	private final Font playerNameFont;
 
 	private Runnable onAimTouched;
 	private Runnable onWhiteFlagTouched;
 
 	private final LifeBar leftLifeBar;
 	private final LifeBar rightLifeBar;
 
 	private final Text leftPlayerNameText;
 	private final Text rightPlayerNameText;
 	private final Text statusText;
 
 	private final static int MAX_TEXT_LENGTH = 256;
 	private final static int HUD_MARGIN = 20;
 	private final static int BUTTON_OFFSET_Y = 32;
 	private final static float PLAYER_NAME_ALPHA = 0.7f;
 
 	/**
 	 * This entity represents a slider to indicate the height of the players castle.
 	 */
 	public class LifeBar extends Entity {
 		public final static float BAR_HEIGHT = 6;
 		public final static float BAR_WIDTH = 200;
 		private final static float BAR_ALPHA = 0.5f;
 
 		private final Rectangle barFrame;
 		private final Rectangle barFilled;
 		private final boolean isLeft;
 
 		private LifeBar(float x, float y, boolean isLeft) {
 			this(x, y, new Color(0.5f, 0.5f, 0.5f, LifeBar.BAR_ALPHA), new Color(1, 0, 0, LifeBar.BAR_ALPHA), isLeft);
 		}
 
 		private LifeBar(float x, float y, Color frameColor, Color lifeColor, boolean isLeft) {
 			super(x, y);
 
 			GameContext gc = GameContext.getInstance();
 
 			this.isLeft = isLeft;
 
 			this.barFrame = new Rectangle(0, 0, BAR_WIDTH, LifeBar.BAR_HEIGHT, gc.getVboManager());
 			this.barFrame.setColor(frameColor);
 			this.attachChild(this.barFrame);
 
 			this.barFilled = new Rectangle(isLeft ? 0 : BAR_WIDTH, 0, isLeft ? LifeBar.BAR_WIDTH : -LifeBar.BAR_WIDTH, LifeBar.BAR_HEIGHT, gc.getVboManager());
 			this.barFilled.setColor(lifeColor);
 			this.attachChild(this.barFilled);
 		}
 
 		public void setValue(float value) {
			this.barFilled.setWidth((isLeft ? 1 : -1) * LifeBar.BAR_WIDTH * Math.min(value, 1.0f));
 		}
 	}
 
 	public GameHUD() {
 		super();
 
 		GameContext gc = GameContext.getInstance();
 		ResourceManager rm = ResourceManager.getInstance();
 
 		this.statusFont = rm.getStatusFont();
 		this.playerNameFont = rm.getPlayerNameFont();
 		
 		//
 		// aim button
 		//
 
 		this.aimButton = new AnimatedSprite(
 				GameHUD.HUD_MARGIN, GameHUD.HUD_MARGIN + GameHUD.BUTTON_OFFSET_Y,
 				rm.getAimButtonTexture(), gc.getVboManager()) {
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.isActionUp()) {
 					this.setCurrentTileIndex(this.getCurrentTileIndex() == 0 ? 1 : 0);
 					if (onAimTouched != null)
 						onAimTouched.run();
 				}
 				return true;
 			}
 		};
 
 		this.attachChild(this.aimButton);
 
 		//
 		// white flag button
 		//
 
 		this.whiteFlagButton = new AnimatedSprite(
 				GameConfig.CAMERA_WIDTH - rm.getWhiteFlagButtonTexture().getWidth() - GameHUD.HUD_MARGIN,
 				GameHUD.HUD_MARGIN + GameHUD.BUTTON_OFFSET_Y,
 				rm.getWhiteFlagButtonTexture(), gc.getVboManager()) {
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.isActionUp()) {
 					if (onWhiteFlagTouched != null)
 						onWhiteFlagTouched.run();
 				}
 				return true;
 			}
 		};
 
 		this.attachChild(this.whiteFlagButton);
 
 		//
 		// player slider
 		//
 
 		this.leftLifeBar = new LifeBar(
 				GameHUD.HUD_MARGIN,
 				GameHUD.HUD_MARGIN + playerNameFont.getLineHeight(), // GameConfig.CAMERA_HEIGHT / 2 - LifeBar.BAR_HEIGHT / 2,
 				true
 		);
 		this.leftLifeBar.setValue(0.5f);
 		this.attachChild(this.leftLifeBar);
 
 		this.rightLifeBar = new LifeBar(
 				GameConfig.CAMERA_WIDTH - LifeBar.BAR_WIDTH - GameHUD.HUD_MARGIN,
 				GameHUD.HUD_MARGIN + playerNameFont.getLineHeight(), //GameConfig.CAMERA_HEIGHT / 2 - LifeBar.BAR_HEIGHT / 2,
 				false
 		);
 		this.rightLifeBar.setValue(0.9f);
 		this.attachChild(this.rightLifeBar);
 
 		//
 		// players names
 		//
 
 		this.leftPlayerNameText = new Text(0, 0, this.playerNameFont, "", GameHUD.MAX_TEXT_LENGTH, gc.getVboManager());
 		this.leftPlayerNameText.setAlpha(GameHUD.PLAYER_NAME_ALPHA);
 		this.attachChild(this.leftPlayerNameText);
 
 		this.rightPlayerNameText = new Text(0, 0, this.playerNameFont, "", GameHUD.MAX_TEXT_LENGTH, gc.getVboManager());
 		this.rightPlayerNameText.setAlpha(GameHUD.PLAYER_NAME_ALPHA);
 		this.attachChild(this.rightPlayerNameText);
 
 		this.statusText = new Text(0, 0, this.statusFont, "", GameHUD.MAX_TEXT_LENGTH, gc.getVboManager());
 		this.attachChild(this.statusText);
 
 		this.setLeftPlayerName("Left Player");
 		this.setRightPlayerName("Right Player");
 		this.setStatus("Status Message");
 	}
 
 	public void setOnAimTouched(Runnable onAimTouched) {
 		this.onAimTouched = onAimTouched;
 		this.registerTouchArea(this.aimButton);
 	}
 
 	public void setOnWhiteFlagTouched(Runnable onWhiteFlagTouched) {
 		this.onWhiteFlagTouched = onWhiteFlagTouched;
 		this.registerTouchArea(this.whiteFlagButton);
 	}
 
 	public AnimatedSprite getAimButton() {
 		return aimButton;
 	}
 
 	public AnimatedSprite getWhiteFlagButton() {
 		return whiteFlagButton;
 	}
 
 	public LifeBar getRightLifeBar() {
 		return rightLifeBar;
 	}
 
 	public LifeBar getLeftLifeBar() {
 		return leftLifeBar;
 	}
 
 	public void setLeftPlayerName(String name) {
 		this.leftPlayerNameText.setText(name);
 		this.leftPlayerNameText.setPosition(
 				GameHUD.HUD_MARGIN,
 				GameHUD.HUD_MARGIN //GameConfig.CAMERA_HEIGHT - this.playerNameFont.getLineHeight() - GameHUD.HUD_MARGIN
 		);
 	}
 
 	public void setRightPlayerName(String name) {
 		this.rightPlayerNameText.setText(name);
 		this.rightPlayerNameText.setPosition(
 				GameConfig.CAMERA_WIDTH - this.rightPlayerNameText.getWidth() - GameHUD.HUD_MARGIN,
 				GameHUD.HUD_MARGIN //GameConfig.CAMERA_HEIGHT - this.playerNameFont.getLineHeight() - GameHUD.HUD_MARGIN
 		);
 	}
 
 	public void setStatus(String status) {
 		this.statusText.setText(status);
 		this.statusText.setPosition(
 				GameConfig.CAMERA_WIDTH / 2 - statusText.getWidth() / 2,
 				GameHUD.HUD_MARGIN
 		);
 	}
 }
