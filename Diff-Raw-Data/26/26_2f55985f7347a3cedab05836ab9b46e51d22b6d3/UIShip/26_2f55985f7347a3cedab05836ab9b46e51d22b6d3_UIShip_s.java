 package com.evervoid.client.graphics;
 
 import com.evervoid.client.graphics.geometry.AnimatedTransform.DurationMode;
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.client.graphics.geometry.MathUtils.MovementDelta;
 import com.evervoid.client.views.solar.SolarSystemGrid;
 import com.evervoid.state.GridLocation;
 import com.evervoid.state.Point;
 import com.evervoid.state.prop.Ship;
 import com.evervoid.state.prop.TrailInfo;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class UIShip extends UIShadedProp implements Colorable
 {
 	protected static enum ShipState
 	{
 		INACTIVE, MOVING, SELECTABLE, SELECTED;
 	}
 
 	private Sprite aColorableSprite;
 	private MovementDelta aMovementDelta;
 	private final Ship aShip;
 	private ShipState aState = ShipState.INACTIVE;
 	/**
 	 * Trail of the ship. The trail auto-attaches to the ship (the method for that depends on the trail type), so no need to
 	 * attach it manually in UIShip
 	 */
 	private UIShipTrail aTrail;
 
 	public UIShip(final SolarSystemGrid grid, final Ship ship)
 	{
 		super(grid, ship.getLocation());
 		aShip = ship;
 		buildProp();
 		aGridTranslation.setDuration(ship.getData().getMovingTime());
 		// Set rotation speed and mode:
 		aFaceTowards.setSpeed(ship.getData().getRotationSpeed()).setDurationMode(DurationMode.CONTINUOUS);
 		setHue(GraphicsUtils.getPlayerColor(ship.getColor()));
 	}
 
 	@Override
 	protected void buildSprite()
 	{
 		final Sprite baseSprite = new Sprite(aShip.getData().getBaseSprite());
 		addSprite(baseSprite);
 		aColorableSprite = new Sprite(aShip.getData().getColorOverlay());
 		addSprite(aColorableSprite);
 		final TrailInfo trailInfo = aShip.getTrailInfo();
 		switch (trailInfo.race) {
 			case ROUND:
 				aTrail = new UIShipBubbleTrail(this, trailInfo.baseSprite, trailInfo.distanceInterval, trailInfo.decayTime);
 				break;
 			case SQUARE:
 				aTrail = new UIShipLinearTrail(this, trailInfo.trailSprites);
 				break;
 		}
 		final Point engineOffset = aShip.getData().getEngineOffset();
 		addSprite(new Sprite(trailInfo.engineSprite, engineOffset.x, engineOffset.y));
 		final Shade shade = new Shade(aShip.getData().getBaseSprite());
 		shade.setGradientPortion(0.6f);
		shade.getNewTransform().setAlpha(0.8f);
 		addSprite(shade);
 		setShade(shade);
 		enableFloatingAnimation(1f, 2f);
 	}
 
 	@Override
 	public void computeTransforms()
 	{
 		super.computeTransforms();
 		if (aSpriteReady && aState.equals(ShipState.MOVING)) {
 			aTrail.shipMove();
 		}
 	}
 
 	@Override
 	public void faceTowards(final GridLocation target)
 	{
 		if (aState == ShipState.SELECTED) {
 			super.faceTowards(target);
 		}
 	}
 
 	public float getMovingSpeed()
 	{
 		return aGridTranslation.getMovingSpeed();
 	}
 
 	public Vector2f getTrailAttachPoint()
 	{
 		return MathUtils.getVector2fFromPoint(aShip.getData().getTrailAttachPoint()).mult(Sprite.sSpriteScale);
 	}
 
 	@Override
 	public void hasMoved()
 	{
 		super.hasMoved();
 		if (aMovementDelta != null) {
 			faceTowards(aMovementDelta.getAngle());
 		}
 		if (aSpriteReady) {
 			aTrail.shipMoveEnd();
 		}
 		// FIXME: Should be "inactive" after moving
 		// but selected for now because it's more convenient for testing
 		aState = ShipState.SELECTED;
 	}
 
 	public void moveShip(final GridLocation destination)
 	{
 		if (aState == ShipState.SELECTED) {
 			faceTowards(destination);
 			aMovementDelta = MovementDelta.fromDelta(aGridLocation, destination);
 			super.smoothMoveTo(destination);
 			aState = ShipState.MOVING;
 			if (aSpriteReady) {
 				aTrail.shipMoveStart();
 			}
 		}
 	}
 
 	public void select()
 	{
 		aState = ShipState.SELECTED;
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue)
 	{
 		aColorableSprite.setHue(hue);
 	}
 
 	@Override
 	public void setHue(final ColorRGBA hue, final float multiplier)
 	{
 		aColorableSprite.setHue(hue, multiplier);
 	}
 }
