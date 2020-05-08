 package com.evervoid.client.views.solar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.client.EverNode;
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.gamedata.SpriteInfo;
 
 public class UIShipLinearTrail extends UIShipTrail
 {
 	protected List<EverNode> aGradualSprites = new ArrayList<EverNode>();
 	private float aGradualState = 0f;
 
 	public UIShipLinearTrail(final UIShip ship, final Iterable<SpriteInfo> sprites)
 	{
 		super(ship);
 		ship.addNode(this);
 		getNewTransform().translate(aShip.getTrailAttachPoint()); // Attach point offset
 		for (final SpriteInfo s : sprites) {
 			addSprite(s);
 		}
 	}
 
 	@Override
	public EverNode addSprite(final String image, final float x, final float y)
 	{
 		final EverNode spr = super.addSprite(image, x, y);
 		aGradualSprites.add(spr);
 		computeGradual();
 		return spr;
 	}
 
 	protected void computeGradual()
 	{
 		final float grad = aGradualState * getNumberOfFrames() - 1;
 		for (final Transform t : aSpriteTransforms.values()) {
 			t.setAlpha(0);
 		}
 		final int currentSprite = (int) grad;
 		setAlphaOfFrame(currentSprite - 1, 1 - grad + currentSprite);
 		setAlphaOfFrame(currentSprite, 1);
 		setAlphaOfFrame(currentSprite + 1, grad - currentSprite);
 	}
 
 	private void setAlphaOfFrame(final int index, final float alpha)
 	{
 		if (index >= 0 && index < getNumberOfFrames()) {
 			aSpriteTransforms.get(aGradualSprites.get(index)).setAlpha(alpha);
 		}
 	}
 
 	public void setGradualState(final float gradualState)
 	{
 		aGradualState = MathUtils.clampFloat(0, gradualState, 1);
 		computeGradual();
 	}
 
 	@Override
 	public void shipMove()
 	{
 		setGradualState(aShip.getMovingSpeed());
 	}
 }
