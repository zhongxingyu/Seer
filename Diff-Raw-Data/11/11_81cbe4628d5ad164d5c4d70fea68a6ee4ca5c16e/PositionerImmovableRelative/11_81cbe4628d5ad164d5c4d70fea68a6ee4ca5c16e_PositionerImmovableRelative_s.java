 package org.andengine.limbo.utils.positioner;
 
 import org.andengine.entity.IEntity;
import org.andengine.util.adt.transformation.Transformation;
 import org.andengine.util.math.MathUtils;
 /**
  * (c) 2013 Michal Stawinski (nazgee)
  *
  * @author Michal Stawinski
  * @since 20:35:05 08 - 08.05.2013
  */
 public class PositionerImmovableRelative extends BasePositioner {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	private static PositionerImmovableRelative INSTANCE;
 
 	static float[] TMP_ENTITY_ANCHORED = new float[2];
 	static float[] TMP_ENTITY_MOVABLE = new float[2];
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	private PositionerImmovableRelative() {
 
 	}
 
 	public static PositionerImmovableRelative getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new PositionerImmovableRelative();
 		}
 		return INSTANCE;
 	}
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	@Override
 	public void place(IEntity pImmovable, IEntity pMovable, AnchorPointsPair pAnchorsPair,
 			final float tX, final float tY, boolean pMoveVertically, boolean pMoveHorizontally) {
 		
 		pImmovable.convertLocalCoordinatesToSceneCoordinates(
 				pAnchorsPair.anchorA.X * pImmovable.getWidth(),
 				pAnchorsPair.anchorA.Y * pImmovable.getHeight(), TMP_ENTITY_ANCHORED);
 
 		pMovable.convertLocalCoordinatesToSceneCoordinates(
 				pAnchorsPair.anchorB.X * pMovable.getWidth(),
 				pAnchorsPair.anchorB.Y * pMovable.getHeight(), TMP_ENTITY_MOVABLE);
 
 		TMP_ENTITY_MOVABLE[0] = !pMoveHorizontally ? 0 : TMP_ENTITY_MOVABLE[0] - TMP_ENTITY_ANCHORED[0];
 		TMP_ENTITY_MOVABLE[1] = !pMoveVertically ? 0 : TMP_ENTITY_MOVABLE[1] - TMP_ENTITY_ANCHORED[1];
 	
 		final float rotation = pImmovable.getLocalToSceneTransformation().getRotation();
 		MathUtils.rotateAroundCenter(TMP_ENTITY_MOVABLE, rotation, 0, 0);
 
 		pMovable.setPosition(pMovable.getX() - TMP_ENTITY_MOVABLE[0] + tX, pMovable.getY() - TMP_ENTITY_MOVABLE[1] + tY);
 
 	}
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
