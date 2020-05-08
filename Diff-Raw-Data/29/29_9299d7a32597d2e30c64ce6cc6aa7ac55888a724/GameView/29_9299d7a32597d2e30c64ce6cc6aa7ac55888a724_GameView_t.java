 package com.evervoid.client.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.evervoid.client.EverVoidClient;
 import com.evervoid.client.EverVoidClient.NodeType;
 import com.evervoid.client.views.galaxy.GalaxyView;
 import com.evervoid.client.views.solar.SolarSystemView;
 import com.evervoid.state.Dimension;
 import com.evervoid.state.EverVoidGameState;
 import com.evervoid.state.SolarSystem;
 import com.jme3.collision.CollisionResults;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector2f;
 
 public class GameView extends ComposedView
 {
 	public enum GameViewType
 	{
 		GALAXY, PLANET, SOLAR;
 	}
 
 	private static GameView sInstance = null;
 
 	public static void changeView(final GameViewType type)
 	{
 		changeView(type, null);
 	}
 
 	public static void changeView(final GameViewType type, final Object arg)
 	{
 		sInstance.switchView(type, arg);
 	}
 
 	public static void collideWithRay(final Ray ray, final CollisionResults results)
 	{
 		sInstance.aActiveView.collideWith(ray, results);
 	}
 
 	private EverView aActiveView = null;
 	/**
 	 * the galaxy view, stored as player will often be returning to this
 	 */
 	private final GalaxyView aGalaxyView;
 	private final Map<SolarSystem, SolarSystemView> aSolarViews = new HashMap<SolarSystem, SolarSystemView>();
 	private final EverVoidGameState aState;
 
 	public GameView(final EverVoidGameState state)
 	{
 		sInstance = this;
 		aState = state;
 		addView(new TopBarView(new Dimension(getHeight() / 10, getWidth())));
 		aGalaxyView = new GalaxyView(aState.getGalaxy());
 		changeView(GameViewType.SOLAR, aState.getTempSolarSystem());
 	}
 
 	private SolarSystemView getSolarSystemView(final SolarSystem ss)
 	{
 		if (!aSolarViews.containsKey(ss)) {
 			aSolarViews.put(ss, new SolarSystemView(ss));
 		}
 		return aSolarViews.get(ss);
 	}
 
 	@Override
 	public boolean onMouseClick(final Vector2f position, final float tpf)
 	{
 		if (super.onMouseClick(position, tpf)) {
 			return true;
 		}
 		if (aActiveView == null) {
 			return false;
 		}
 		return aActiveView.onMouseClick(position, tpf);
 	}
 
 	@Override
 	public boolean onMouseMove(final float tpf, final Vector2f position)
 	{
 		if (super.onMouseMove(tpf, position)) {
 			return true;
 		}
 		if (aActiveView == null) {
 			return false;
 		}
 		return aActiveView.onMouseMove(tpf, position);
 	}
 
 	@Override
 	public boolean onMouseRelease(final Vector2f position, final float tpf)
 	{
 		if (super.onMouseRelease(position, tpf)) {
 			return true;
 		}
 		if (aActiveView == null) {
 			return false;
 		}
 		return aActiveView.onMouseRelease(position, tpf);
 	}
 
 	@Override
 	public boolean onMouseWheelDown(final float delta, final float tpf, final Vector2f position)
 	{
 		if (super.onMouseWheelDown(delta, tpf, position)) {
 			return true;
 		}
 		if (aActiveView == null) {
 			return false;
 		}
 		return aActiveView.onMouseWheelDown(delta, tpf, position);
 	}
 
 	@Override
 	public boolean onMouseWheelUp(final float delta, final float tpf, final Vector2f position)
 	{
 		if (super.onMouseWheelUp(delta, tpf, position)) {
 			return true;
 		}
 		if (aActiveView == null) {
 			return false;
 		}
 		return aActiveView.onMouseWheelUp(delta, tpf, position);
 	}
 
 	/**
 	 * Switches to a new view in-game. This has no effect if ViewManager isn't in in-game mode
 	 * 
 	 * @param type
 	 *            The type of view to switch to
 	 * @param arg
 	 *            If the specified view type requires an argument
 	 */
 	private void switchView(final GameViewType type, Object arg)
 	{
		if (aActiveView != null) {
			EverVoidClient.delRootNode(aActiveView);
		}
 		switch (type) {
 			case GALAXY:
 				aActiveView = aGalaxyView;
				EverVoidClient.addRootNode(NodeType.THREEDIMENSION, aActiveView);
 				break;
 			case SOLAR:
 				if (arg == null) {// FIXME: hax
 					arg = aState.getTempSolarSystem();
 				}
 				aActiveView = getSolarSystemView((SolarSystem) arg);
 				addNode(aActiveView);
 				break;
 		}
 	}
 }
