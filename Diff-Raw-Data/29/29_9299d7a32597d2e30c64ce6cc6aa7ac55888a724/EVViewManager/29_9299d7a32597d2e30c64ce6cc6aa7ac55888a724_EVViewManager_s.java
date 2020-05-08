 package com.evervoid.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.evervoid.client.views.EverView;
 import com.jme3.math.Vector2f;
 
 public class EVViewManager
 {
 	public enum ViewType
 	{
 		GAME, MAINMENU
 	}
 
 	private static EVViewManager sInstance;
 
 	private static EVViewManager getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVViewManager();
 		}
 		return sInstance;
 	}
 
 	public static void onMouseClick(final Vector2f position, final float tpf)
 	{
 		getInstance().aInputRelay.onMouseClick(position, tpf);
 	}
 
 	public static void onMouseMove(final float tpf, final Vector2f position)
 	{
 		getInstance().aInputRelay.onMouseMove(tpf, position);
 	}
 
 	public static void onMouseRelease(final Vector2f position, final float tpf)
 	{
 		getInstance().aInputRelay.onMouseRelease(position, tpf);
 	}
 
 	public static void onMouseWheelDown(final float delta, final float tpf, final Vector2f position)
 	{
 		getInstance().aInputRelay.onMouseWheelDown(delta, tpf, position);
 	}
 
 	public static void onMouseWheelUp(final float delta, final float tpf, final Vector2f position)
 	{
 		getInstance().aInputRelay.onMouseWheelUp(delta, tpf, position);
 	}
 
 	public static void registerView(final ViewType type, final EverView view)
 	{
		if (getInstance().aViewMap.containsKey(type)) {
			EverVoidClient.delRootNode(view);
		}
 		getInstance().aViewMap.put(type, view);
 		getInstance().aInputRelay = view;
 		EverVoidClient.addRootNode(view.getNodeType(), view);
 	}
 
 	public static void switchTo(final ViewType type)
 	{
 		for (final ViewType t : ViewType.values()) {
 			if (getInstance().aViewMap.containsKey(t)) {
 				EverVoidClient.delRootNode(getInstance().aViewMap.get(t));
 			}
 		}
 		final EverView newView = getInstance().aViewMap.get(type);
 		getInstance().aInputRelay = newView;
 		EverVoidClient.addRootNode(newView.getNodeType(), newView);
 	}
 
 	private InputListener aInputRelay;
 	private final Map<ViewType, EverView> aViewMap;
 
 	private EVViewManager()
 	{
 		aInputRelay = null;
 		aViewMap = new HashMap<EVViewManager.ViewType, EverView>();
 	}
 }
