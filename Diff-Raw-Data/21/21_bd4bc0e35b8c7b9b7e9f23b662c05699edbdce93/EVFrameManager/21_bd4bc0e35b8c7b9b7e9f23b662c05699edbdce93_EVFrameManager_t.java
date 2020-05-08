 package com.evervoid.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.geometry.TransformManager;
 import com.evervoid.client.interfaces.EVFrameObserver;
 
 public class EVFrameManager
 {
 	private static final List<EVFrameObserver> sDeletedObservers = new ArrayList<EVFrameObserver>();
 	private static final List<EVFrameObserver> sNewObservers = new ArrayList<EVFrameObserver>();
 	private static final List<EVFrameObserver> sObservers = new ArrayList<EVFrameObserver>();
 	private static TransformManager sTransformManager = null;
 
 	public static void deregister(final EVFrameObserver observer)
 	{
		if (sObservers.contains(observer)) {
			sDeletedObservers.add(observer);
		}
 	}
 
 	public static void register(final EVFrameObserver observer)
 	{
		if (!sObservers.contains(observer)) {
			sNewObservers.add(observer);
		}
 	}
 
 	public static void setTransformManager(final TransformManager manager)
 	{
 		sTransformManager = manager;
 	}
 
 	public static void tick(final FrameUpdate f)
 	{
 		for (final EVFrameObserver o : sNewObservers) {
 			sObservers.add(o);
 		}
 		sNewObservers.clear();
 		for (final EVFrameObserver o : sDeletedObservers) {
 			sObservers.remove(o);
 		}
 		sDeletedObservers.clear();
 		for (final EVFrameObserver o : sObservers) {
 			o.frame(f);
 		}
 		// Always notify Transform manager last
 		if (sTransformManager != null) {
 			sTransformManager.frame(f);
 		}
 	}
 
 	private EVFrameManager()
 	{
 	}
 }
