 package com.testgame.sprite;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.MoveModifier;
 
 import com.testgame.mechanics.unit.AUnit;
 
 public class WalkMoveModifier extends MoveModifier {
 	
 	
 	public int xDirection;
 	public int yDirection;
 	
 	boolean horiz;
 
 	public WalkMoveModifier(float pDuration, float pFromX, float pFromY, float pToX, float pToY, boolean horiz) {
 		super(pDuration, pFromX, pFromY, pToX, pToY);
 		if (pToX >= pFromX) { // walking right
 			xDirection = 1;
 		} else { // walking left
 			xDirection = -1;
 		}
 		
 		if (pToY >= pFromY) { // walking right
 			yDirection = 1;
 		} else { // walking left
 			yDirection = -1;
 		}
 		
 		this.horiz = horiz;
 	}
 	
 	@Override
 	protected void onModifierStarted(IEntity pItem) {
 		super.onModifierStarted(pItem);
 		if (horiz) ((AUnit) pItem).walkAnimate(xDirection, 0);
 		else ((AUnit) pItem).walkAnimate(0, yDirection);
 	}
 	
 	@Override
 	protected void onModifierFinished(IEntity pItem) {
 		super.onModifierFinished(pItem);
 		((CharacterSprite)pItem).stopAnimation();
 	}
 
 }
