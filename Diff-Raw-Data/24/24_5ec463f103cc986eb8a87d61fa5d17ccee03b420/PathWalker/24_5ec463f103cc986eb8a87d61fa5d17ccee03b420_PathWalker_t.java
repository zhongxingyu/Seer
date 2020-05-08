 package org.beavers.ingame;
 
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
 import org.anddev.andengine.entity.modifier.MoveModifier;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.IModifier.IModifierListener;
 import org.anddev.andengine.util.path.Path.Step;
 import org.beavers.gameplay.GameScene;
 
 /**
  * let a soldier walk its waypoints
  * @author <a href="https://github.com/winniehell/">winniehell</a>
  */
 public class PathWalker implements IModifierListener<IEntity> {
 
 	public PathWalker(final GameScene pGameScene, final Soldier pSoldier) {
 		gameScene = pGameScene;
 		soldier = pSoldier;
 
 		waypoint = null;
 		stepIndex = 0;
 	}
 
 	@Override
 	public void onModifierStarted(final IModifier<IEntity> pModifier, final IEntity pItem) {
 
 	}
 
 	@Override
 	public void onModifierFinished(final IModifier<IEntity> pModifier, final IEntity pItem) {
 		if(pModifier instanceof MoveModifier)
 		{
 			soldier.stopAnimation(0);
 
 			nextTile();
 
 			if(tile != null)
 			{
 				soldier.move(tile, this);
 			}
 		}
 	}
 
 	public void start()
 	{
 		nextWaypoint();
 		nextTile();
 
 		soldier.move(tile, this);
 	}
 
 	private final GameScene gameScene;
 	private final Soldier soldier;
 
 	private WayPoint waypoint;
 
 	private int stepIndex;
 	private TMXTile tile;
 
 	private void nextWaypoint()
 	{
		if(waypoint != null)
		{
			waypoint.detachChildren();
			gameScene.removeObject(waypoint);
		}

 		stepIndex = 1;
 		waypoint = soldier.popWayPoint();
 	}
 
 	public void nextTile() {
 		if(stepIndex >= waypoint.getPath().getLength())
 		{
 			nextWaypoint();
 		}
 
 		if(waypoint != null)
 		{
 			final Step nextStep = waypoint.getPath().getStep(stepIndex);
 
 			++stepIndex;
 
 			tile = gameScene.getTMXLayer().getTMXTile(nextStep.getTileColumn(), nextStep.getTileRow());
 		}
 		else
 		{
 			tile = null;
 		}
 	}
 }
