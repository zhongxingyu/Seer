 package org.beavers.ingame;
 
 import java.util.ArrayDeque;
 
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
 import org.anddev.andengine.entity.modifier.IEntityModifier;
 import org.anddev.andengine.entity.modifier.MoveModifier;
 import org.anddev.andengine.entity.modifier.RotationByModifier;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
 import org.anddev.andengine.util.path.IPathFinder;
 import org.anddev.andengine.util.path.ITiledMap;
 import org.anddev.andengine.util.path.Path;
 import org.beavers.Textures;
 import org.beavers.gameplay.GameScene;
 
 /**
  * soldier sprite
  * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
  * @author <a href="https://github.com/winniehell/">winniehell</a>
  */
 public class Soldier extends AnimatedSprite implements GameObject {
 
 	/**
 	 * default constructor
 	 * @param pTeam team this soldier is in
 	 * @param pInitialPosition initial position
 	 */
 	public Soldier(final int pTeam, final TMXTile pInitialPosition) {
 		super(GameScene.getTileCenterX(pInitialPosition),
 				GameScene.getTileCenterY(pInitialPosition),
 				getTexture(pTeam));
 
 		setPosition(getX() - getWidth()/2, getY() - getHeight()/2);
 
 		wayPoints = new ArrayDeque<WayPoint>();
 		wayPoints.push(new WayPoint(this, null, pInitialPosition));
 
 		//Selection Circle
 		selectionMark = new Sprite(0, 0, Textures.SOLDIER_SELECTION_CIRCLE.deepCopy());
 		selectionMark.setPosition((getWidth()-selectionMark.getWidth())/2, (getHeight()-selectionMark.getHeight())/2+5);
 
 		stopAnimation(0);
 		setRotationCenter(getWidth()/2, getHeight()/2);
 		setZIndex(10);
 	}
 
 	/**
 	 * adds a waypoint for this soldier
 	 * @param pWayPoint waypoint to add
 	 */
 	public void addWayPoint(final WayPoint pWayPoint)
 	{
 		wayPoints.getLast().isLast = false;
 		wayPoints.addLast(pWayPoint);
 	}
 
 	/**
 	 * draws the waypoints and paths in between to the scene
 	 * @param pGameScene scene to draw on
 	 */
 	public void drawWaypoints(final GameScene pGameScene) {
 	    for(final WayPoint waypoint : wayPoints)
 		{
 	    	if(waypoint == wayPoints.getFirst())
 	    	{
 	    		continue;
 	    	}
 
 			pGameScene.drawPath(waypoint.getPath(), waypoint);
 			pGameScene.attachChild(waypoint);
 		}
 	}
 
 	/**
 	 * turn soldier to face target tile
 	 * @param pTarget target tile
 	 * @param pListener listener for rotation
 	 */
 	public void faceTarget(final TMXTile pTarget, final IModifierListener<IEntity> pListener){
 		final float angleX = GameScene.getTileCenterX(pTarget)-(getX()+getWidth()/2);
 		final float angleY = GameScene.getTileCenterY(pTarget)-(getY()+getHeight()/2);
 		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
 
 
 		if((angle-getRotation())>180)angle=(angle-getRotation())-360;
 		else if((angle-getRotation())<-180)angle=360+(angle-getRotation());
 		else angle=angle-getRotation();
 
 		if(angle == 0)
 		{
 			if(pListener != null)
 			{
 				pListener.onModifierFinished(null, this);
 			}
 		}
 		else
 		{
 			final RotationByModifier rotation = new RotationByModifier(Math.abs(angle)/ROTATION_SPEED, angle);
 
 			if(pListener != null)
 			{
 				rotation.addModifierListener(pListener);
 			}
 
 			lastModifier = rotation;
 			registerEntityModifier(rotation);
 		}
 	}
 
 	/**
 	 * @param pPathFinder path finder to use
 	 * @param pTarget target position
 	 * @return a path from last waypoint to the target position (or null if there is none)
 	 */
 	@Override
 	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
 		return wayPoints.getLast().findPath(pPathFinder, pTarget);
 	}
 
 	/**
 	 * fire shot to target tile
 	 * @param pShot shot object
 	 * @param pTarget target tile
 	 */
 	public void fireShot(final Shot pShot, final TMXTile pTarget){
 		faceTarget(pTarget, new IModifierListener<IEntity>() {
 			@Override
 			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
 
 			}
 
 			@Override
 			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
 				pShot.fire(pTarget);
 			}
 		});
 	}
 
 	/**
 	 * TODO not yet used
 	 */
 	public int getHealthPercentage()
 	{
 		return 1/0;
 	}
 
 	/**
 	 * @param pMap tile map to walk on
 	 * @param pFrom start position
 	 * @param pTo end position
 	 * @return step cost for soldier and given tiles
 	 */
 	@Override
 	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
 		return wayPoints.getLast().getStepCost(pMap, pFrom, pTo);
 	}
 
 	/**
 	 * @return current soldier position
 	 */
 	@Override
 	public TMXTile getTile() {
 		return wayPoints.getFirst().getTile();
 	}
 
 	/**
 	 * TODO not yet used
 	 */
 	public Weapon getWeapon()
 	{
 		return null;
 	}
 
 	/**
 	 * add selectionMark
 	 */
 	public void markSelected(){
 		attachChild(selectionMark);
 	}
 
 	/**
 	 * remove selectionMark and waypoints from GameScene
 	 */
 	public void markDeselected(){
 		detachChild(selectionMark);
 
 	    for(final WayPoint waypoint : wayPoints)
 		{
 	    	if(waypoint == wayPoints.getFirst())
 	    	{
 	    		continue;
 	    	}
 
 			waypoint.detachChildren();
 			waypoint.detachSelf();
 		}
 	}
 
 	/**
 	 * move to target tile
 	 * @param pTarget target tile
 	 */
 	public void move(final TMXTile pTarget) {
 		move(pTarget, new IModifierListener<IEntity>() {
 			@Override
 			public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
 
 			}
 
 			@Override
 			public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
 				Soldier.this.stopAnimation(0);
 			}
 		});
 	}
 
 	/**
 	 * move to target tile
 	 * @param pTarget target tile
 	 * @param pListener listener for movement
 	 */
 	public void move(final TMXTile pTarget, final IModifierListener<IEntity> pListener) {
 		final float distx = Math.abs(GameScene.getTileCenterX(pTarget) - (getX()+getWidth()/2));
 		final float disty = Math.abs(GameScene.getTileCenterY(pTarget) - (getY()+getHeight()/2));
 
 		final MoveModifier movement = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/WALKING_SPEED), getX(),
 				GameScene.getTileCenterX(pTarget)-getWidth()/2, getY(), GameScene.getTileCenterY(pTarget)-getHeight()/2);
 
 		if(pListener != null)
 		{
 			movement.addModifierListener(pListener);
 		}
 
 		faceTarget(pTarget, new IModifierListener<IEntity>() {
 			@Override
 			public void onModifierStarted(final IModifier<IEntity> pModifier,
 					final IEntity pItem) {
 
 			}
 
 			@Override
 			public void onModifierFinished(final IModifier<IEntity> pModifier,
 					final IEntity pItem) {
 				lastModifier = movement;
 
 				Soldier.this.registerEntityModifier(movement);
 				Soldier.this.animate(new long[]{200, 200}, 1, 2, true);
 			}
 		});
 	}
 
 	/**
 	 * remove first waypoint
 	 * @return removed waypoint
 	 */
 	public WayPoint popWayPoint() {
 		if(wayPoints.size() > 1)
 		{
 			wayPoints.removeFirst();
 			return wayPoints.getFirst();
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * remove last waypoint
 	 */
 	public void removeWayPoint()
 	{
 		if(wayPoints.size() > 1)
 		{
 			final WayPoint waypoint = wayPoints.removeLast();
 			waypoint.detachChildren();
 			waypoint.detachSelf();
 
 			wayPoints.getLast().isLast = true;
 		}
 	}
 
 	/**
 	 * stop moving or turning
 	 */
 	public void stop()
 	{
 		stopAnimation(0);
 
 		if(lastModifier != null)
 		{
 			if(!lastModifier.isFinished())
 			{
 				unregisterEntityModifier(lastModifier);
 			}
 			lastModifier = null;
 		}
 	}
 
 	/** speed for movement in pixel per second */
 	private static final int WALKING_SPEED = 80;
 	/** speed for rotation in degree per second */
 	private static final int ROTATION_SPEED = 240;
 
 	/** assigned waypoints */
 	private final ArrayDeque<WayPoint> wayPoints;
 
 	/** last initiated movement or rotation */
 	private IEntityModifier lastModifier;
 	/** token to mark the selected soldier */
 	private final Sprite selectionMark;
 
 	/**
 	 * @return TextureRegion for given team
 	 */
 	private static TiledTextureRegion getTexture(final int pTeam) {
 		switch (pTeam) {
 		case 0:
 			return Textures.SOLDIER_TEAM0.deepCopy();
 		default:
 			return null;
 		}
 	}
 }
