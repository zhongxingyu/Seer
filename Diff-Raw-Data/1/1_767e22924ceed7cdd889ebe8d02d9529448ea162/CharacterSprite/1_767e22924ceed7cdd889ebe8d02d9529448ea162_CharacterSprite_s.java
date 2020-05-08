 package com.testgame.sprite;
 
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.testgame.resource.ResourcesManager;
 import com.testgame.scene.GameScene;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.player.APlayer;
 
import android.util.Log;
 import android.view.MotionEvent;
 
 public class CharacterSprite extends AnimatedSprite {
 	
 	public CharacterSprite(float pX, float pY,
 			ITextureRegion pTextureRegion,
 			VertexBufferObjectManager pVertexBufferObjectManager) {
 		super(pX, pY, (ITiledTextureRegion) pTextureRegion, pVertexBufferObjectManager);
 		resourcesManager = ResourcesManager.getInstance();
 		inSelectedCharactersAttackRange = false;
 	}
 
 	private ResourcesManager resourcesManager;
 	private Text energyText;
 	
 	protected APlayer player;
 	
 	protected GameScene game;
 	
 	public boolean inSelectedCharactersAttackRange;
 	
 	public void initializeText(int Energy, int Health){
 		if (this.game.vbom == null) Log.d("AndEngine", "VBOM NULL"); 
 		this.energyText = new Text(0, 0, this.resourcesManager.font, Energy+"/"+Health, this.game.vbom);
 		this.attachChild(energyText);
 	}
 	
 	public void setText(int Energy, int Health) {
 		this.energyText.setText(Energy+"/"+Health);
 	}
 	
 	@Override
     public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 		Log.d("AndEngine", "Sprite touched!");
 		
 		APlayer p = this.player;
 		boolean isTurn = p.isTurn();
 		
 		if (game.animating) return true;
 		
 		if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {
 			
 			ResourcesManager.getInstance().touch_sound.play();
 			
 			if (isTurn) {
 				Log.d("AndEngine", "Our turn, calling activate and select");
 				if (this.game.getSelectedCharacter() == this) {
 					this.game.deselectCharacter(true);
 				} else {
 					this.game.activateAndSelect(this);
 				}
 			} else {
 				if (inSelectedCharactersAttackRange){
 					Log.d("AndEngine", "being attacked!");
 					// TODO : attack..
 					((AUnit) this.game.getSelectedCharacter()).attack((AUnit) this);
 				} else {
 					Log.d("AndEngine", "Not our turn, just selecting.");
 					if (this.game.getSelectedCharacter() == this) {
 						this.game.deselectCharacter(true);
 					} else {
 						this.game.setSelectedCharacter((AUnit) this);
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
