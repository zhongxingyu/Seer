 package org.beavers.gameplay;
 
 import java.util.Hashtable;
 
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
 import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
 import org.anddev.andengine.entity.primitive.Line;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.input.touch.detector.HoldDetector;
 import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
 import org.anddev.andengine.input.touch.detector.ScrollDetector;
 import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.util.Debug;
 import org.anddev.andengine.util.path.IPathFinder;
 import org.anddev.andengine.util.path.ITiledMap;
 import org.anddev.andengine.util.path.Path;
 import org.anddev.andengine.util.path.astar.AStarPathFinder;
 import org.beavers.AppActivity;
 import org.beavers.R;
 import org.beavers.ingame.EmptyTile;
 import org.beavers.ingame.GameObject;
 import org.beavers.ingame.PathWalker;
 import org.beavers.ingame.Soldier;
 import org.beavers.ingame.WayPoint;
 import org.beavers.ui.ContextMenuHandler;
 
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnCreateContextMenuListener;
 
 public class GameScene extends Scene
 	implements ITiledMap<GameObject>,
 		IOnSceneTouchListener,
 		IHoldDetectorListener,
 		IScrollDetectorListener,
 		OnCreateContextMenuListener
 {
 	/**
 	 * @name helper functions
 	 * @{
 	 */
 	public static int getTileCenterX(final TMXTile pTile)
 	{
 		return pTile.getTileX() + pTile.getTileWidth()/2;
 	}
 
 	public static int getTileCenterY(final TMXTile pTile)
 	{
 		return pTile.getTileY() + pTile.getTileHeight()/2;
 	}
 	/**
 	 * @}
 	 */
 
 	public GameInfo currentGame;
 
 	/**
 	 * default constructor
 	 * @param pApp
 	 */
 	public GameScene(final AppActivity pApp)
 	{
 		super();
 
 		app = pApp;
 
 		scrollDetector = new SurfaceScrollDetector(10.0f, this);
 		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
 		registerUpdateHandler(holdDetector);
 
 		gameObjects = new Hashtable<TMXTile, GameObject>();
 
 		loadMap("test");
 		loadSoldiers();
 
 		if(map != null)
 		{
 			pathFinder = new AStarPathFinder<GameObject>(this, 1600, true);
 		}
 		else
 		{
 			pathFinder = null;
 		}
 
 		setOnSceneTouchListener(this);
 	}
 
 	/**
 	 * insert a new game object on the map
 	 * @param pObject the object to insert
 	 */
 	public void addObject(final GameObject pObject)
 	{
 		gameObjects.put(pObject.getTile(), pObject);
 		attachChild(pObject);
 	}
 
 	public void drawPath(final Path path, final IEntity pParent) {
 		Path.Step step = path.getStep(0);
 		TMXTile lastTile = tmxLayer.getTMXTile(step.getTileColumn(), step.getTileRow());
 
 		for(int i = 1; i < path.getLength(); ++i)
 		{
 			step = path.getStep(i);
 			final TMXTile tile = tmxLayer.getTMXTile(step.getTileColumn(), step.getTileRow());
 
 			final Line line = new Line(getTileCenterX(lastTile) - pParent.getX(), getTileCenterY(lastTile) - pParent.getY(),
 					getTileCenterX(tile) - pParent.getX(), getTileCenterY(tile) - pParent.getY());
 
 			line.setColor(0.0f, 1.0f, 0.0f, 0.5f);
 			line.setLineWidth(2);
 			line.setZIndex(0);
 
 			pParent.attachChild(line);
 
 			lastTile = tile;
 		}
 	}
 
 	public TMXTiledMap getMap() {
 		return map;
 	}
 
 	public IPathFinder<GameObject> getPathFinder() {
 		return pathFinder;
 	}
 
 	public Soldier getSelectedSoldier() {
 		return selectedSoldier;
 	}
 
 	@Override
 	public float getStepCost(final GameObject pObject, final int pFromTileColumn,
 			final int pFromTileRow, final int pToTileColumn, final int pToTileRow) {
 
 		return pObject.getStepCost(this, tmxLayer.getTMXTile(pFromTileColumn, pFromTileRow),
 				tmxLayer.getTMXTile(pToTileColumn, pToTileRow));
 	}
 
 	@Override
 	public int getTileColumns() {
 		return map.getTileColumns();
 	}
 
 	@Override
 	public int getTileRows() {
 		return map.getTileRows();
 	}
 
 	public TMXLayer getTMXLayer() {
 		return tmxLayer;
 	}
 
 	@Override
 	public boolean isTileBlocked(final GameObject pObject, final int pTileColumn, final int pTileRow) {
 		final TMXTile tile = tmxLayer.getTMXTile(pTileColumn, pTileRow);
 
 		return ((tile == null) || (tile.getTMXTileProperties(map) != null));
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu pMenu, final View pView,
 			final ContextMenuInfo pInfo) {
 
 		if(contextMenuHandler != null)
 		{
 	        final MenuInflater inflater = app.getMenuInflater();
 	        inflater.inflate(contextMenuHandler.getMenuID(), pMenu);
 
 			for(int i = 0; i < pMenu.size(); ++i)
 			{
 				pMenu.getItem(i).setOnMenuItemClickListener(contextMenuHandler);
 			}
 
 			contextMenuHandler.onMenuCreated(pMenu);
 		}
 	}
 
 	public boolean onCreateOptionsMenu(final Menu menu) {
 	    final MenuInflater inflater = app.getMenuInflater();
 		inflater.inflate(R.menu.game_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public void onHold(final HoldDetector pHoldDetector, final long pHoldTimeMilliseconds,
 			final float pHoldX, final float pHoldY) {
 	}
 
 	@Override
 	public void onHoldFinished(final HoldDetector pHoldDetector,
 			final long pHoldTimeMilliseconds, final float pHoldX, final float pHoldY) {
 
 		Log.d("onHoldFinished", "t="+pHoldTimeMilliseconds);
 
 		final TMXTile tile = tmxLayer.getTMXTileAt(pHoldX, pHoldY);
 
 		if(tile != null)
 		{
 			if(gameObjects.containsKey(tile))
 			{
 				final GameObject obj = gameObjects.get(tile);
 
 				if(obj instanceof Soldier)
 				{
 					selectSoldier((Soldier) obj);
 				}
 				else if(obj instanceof WayPoint)
 				{
 					final WayPoint waypoint = (WayPoint) obj;
 
 					if(waypoint.getSoldier() != selectedSoldier)
 					{
 						selectSoldier(waypoint.getSoldier());
 					}
 
 					contextMenuHandler = waypoint;
 					app.showGameContextMenu();
 				}
 			}
 			else if(!isTileBlocked(null, tile.getTileColumn(), tile.getTileRow())
 					&& (selectedSoldier != null))
 			{
 				contextMenuHandler = new EmptyTile(this, tile);
 				app.showGameContextMenu();
 			}
 		}
 	}
 
 	public boolean onOptionsItemSelected(final MenuItem pItem) {
 		switch (pItem.getItemId()) {
 		case R.id.menu_execute:
 			final PathWalker walker = new PathWalker(this, selectedSoldier);
 			walker.start();
 
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
 		if(holdDetector.onSceneTouchEvent(pScene, pSceneTouchEvent)
 				| scrollDetector.onSceneTouchEvent(pScene, pSceneTouchEvent))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
  	}
 
 	@Override
 	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent,
 			final float pDistanceX, final float pDistanceY) {
 		app.getEngine().getCamera().offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
 	}
 
 	@Override
 	public void onTileVisitedByPathFinder(final int pTileColumn, final int pTileRow) {
 
 	}
 
 	public void playOutcome(final OutcomeContainer outcome)
 	{
 		// TODO Auto-generated method stub
 	}
 
	public void removeObject(final GameObject pObject) {
		gameObjects.remove(pObject.getTile());
		detachChild(pObject);
	}

 	public void startPlanningPhase() {
 		// TODO Auto-generated method stub
 	}
 
 	private final static float CAMERA_SPEED = 1.50f;
 	@SuppressWarnings("unused")
 	private static final String TAG = "GameScene";
 
 	private final AppActivity app;
 
 	private final SurfaceScrollDetector scrollDetector;
 	private final HoldDetector holdDetector;
 
 	private TMXTiledMap map;
 	private TMXLayer tmxLayer;
 	private final Hashtable<TMXTile, GameObject> gameObjects;
 	private final AStarPathFinder<GameObject> pathFinder;
 
 	private Soldier selectedSoldier;
 
 	private ContextMenuHandler contextMenuHandler;
 
 	private void loadMap(final String pMapName)
 	{
 		try {
 			final TMXLoader tmxLoader = new TMXLoader(app, app.getEngine().getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
 				@Override
 				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
 
 				}
 			});
 			map = tmxLoader.loadFromAsset(app, "tmx/"+pMapName+".tmx");
 		} catch (final TMXLoadException tmxle) {
 			Debug.e(tmxle);
 			return;
 		}
 
 		tmxLayer = map.getTMXLayers().get(0);
 		this.attachChild(tmxLayer);
 	}
 
 
 	private void loadSoldiers(){
 		addObject(new Soldier(0, tmxLayer.getTMXTile(0, 0)));
 		addObject(new Soldier(0, tmxLayer.getTMXTile(2, 0)));
 	}
 
 	private synchronized void selectSoldier(final Soldier pSoldier) {
 		if(selectedSoldier != pSoldier)
 		{
 			if(selectedSoldier != null)
 			{
 				selectedSoldier.markDeselected();
 			}
 
 			selectedSoldier = pSoldier;
 			selectedSoldier.markSelected();
 			selectedSoldier.drawWaypoints(this);
 
 			sortChildren();
 		}
 	}
 }
