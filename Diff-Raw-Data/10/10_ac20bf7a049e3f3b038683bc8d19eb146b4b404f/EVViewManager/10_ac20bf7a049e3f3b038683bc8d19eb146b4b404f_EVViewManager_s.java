 package com.evervoid.client;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.client.interfaces.EVGlobalMessageListener;
 import com.evervoid.client.views.EverView;
 import com.evervoid.client.views.GameView;
 import com.evervoid.client.views.LoadingView;
 import com.evervoid.client.views.lobby.LobbyView;
 import com.evervoid.json.Json;
 import com.evervoid.state.Color;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.player.Player;
 import com.jme3.math.Vector2f;
 
 /**
  * Only handles switch between Game view, Main menu view, etc. Does not handle switching between subviews of the Game view.
  */
 public class EVViewManager implements EVGlobalMessageListener, EVFrameObserver
 {
 	public enum ViewType
 	{
 		GAME, LOADING, LOBBY, MAINMENU
 	}
 
 	private static EVViewManager sInstance;
 
 	protected static EVViewManager getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVViewManager();
 		}
 		return sInstance;
 	}
 
 	public static void onKeyPress(final KeyboardKey key, final float tpf)
 	{
 		getInstance().aActiveView.onKeyPress(key, tpf);
 	}
 
 	public static void onKeyRelease(final KeyboardKey key, final float tpf)
 	{
 		getInstance().aActiveView.onKeyRelease(key, tpf);
 	}
 
 	public static void onLeftClick(final Vector2f position, final float tpf)
 	{
 		getInstance().aActiveView.onLeftClick(position, tpf);
 	}
 
 	public static void onLeftRelease(final Vector2f position, final float tpf)
 	{
 		getInstance().aActiveView.onLeftRelease(position, tpf);
 	}
 
 	public static void onMouseMove(final Vector2f position, final float tpf)
 	{
 		getInstance().aActiveView.onMouseMove(position, tpf);
 	}
 
 	public static void onMouseWheelDown(final float delta, final float tpf, final Vector2f position)
 	{
 		getInstance().aActiveView.onMouseWheelDown(delta, tpf, position);
 	}
 
 	public static void onMouseWheelUp(final float delta, final float tpf, final Vector2f position)
 	{
 		getInstance().aActiveView.onMouseWheelUp(delta, tpf, position);
 	}
 
 	public static void onRightClick(final Vector2f position, final float tpf)
 	{
 		getInstance().aActiveView.onRightClick(position, tpf);
 	}
 
 	public static void onRightRelease(final Vector2f position, final float tpf)
 	{
 		getInstance().aActiveView.onRightRelease(position, tpf);
 	}
 
 	public static void registerView(final ViewType type, final EverView view)
 	{
 		getInstance().register(type, view);
 	}
 
 	/**
 	 * Schedule a UI job to be executed on the next frame update
 	 * 
 	 * @param job
 	 */
 	public static void schedule(final Runnable job)
 	{
 		getInstance().aUIJobs.add(job);
 	}
 
 	public static void switchTo(final ViewType type)
 	{
 		getInstance().aUIJobs.add(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				getInstance().switchView(type);
 			}
 		});
 	}
 
 	private EverView aActiveView = null;
 	private ViewType aActiveViewType = null;
 	private final Map<EverView, AnimatedAlpha> aAlphaAnimations = new HashMap<EverView, AnimatedAlpha>();
 	private final BlockingQueue<Runnable> aUIJobs = new LinkedBlockingQueue<Runnable>();
 	private final Map<ViewType, EverView> aViewMap = new EnumMap<ViewType, EverView>(ViewType.class);
 
 	private EVViewManager()
 	{
 		sInstance = this;
 		EVFrameManager.register(this);
 		final LoadingView loadingView = new LoadingView();
 		register(ViewType.LOADING, loadingView);
 		switchView(ViewType.LOADING);
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		while (!aUIJobs.isEmpty()) {
 			final Runnable job = aUIJobs.poll();
 			job.run();
 		}
 	}
 
 	@Override
 	public void receivedChat(final String player, final Color playerColor, final String message)
 	{
 		schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if (aActiveViewType.equals(ViewType.LOBBY)) {
 					((LobbyView) aActiveView).receivedChat(player, playerColor, message);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void receivedGameState(final EVGameState gameState)
 	{
 		// TODO - find the right name
 		final Player p = gameState.getPlayerByName(EverVoidClient.getSettings().getNickname());
 		// FIXME - this is a hack, revert to above when player names are not preset
		// final Player p = gameState.getPlayerByName("Player1");
 		schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// TODO: This shouldn't always start a game. it should only start it if it's not in progress already
 				final GameView gameView = new GameView(gameState, p);
 				register(ViewType.GAME, gameView);
 				switchView(ViewType.GAME);
 			}
 		});
 	}
 
 	@Override
 	public void receivedQuit(final Json quitMessage)
 	{
 		// TODO: warn user that someone has quit
 	}
 
 	public void register(final ViewType type, final EverView view)
 	{
 		aViewMap.put(type, view);
 		final AnimatedAlpha animation = view.getNewAlphaAnimation();
 		animation.setDuration(1).setAlpha(0);
 		aAlphaAnimations.put(view, animation);
 	}
 
 	private void switchView(final ViewType type)
 	{
 		if (type.equals(aActiveViewType)) {
 			return;
 		}
 		aActiveViewType = type;
 		if (aActiveView != null) {
 			final EverView oldActive = aActiveView; // Need a final reference to it in Runnable
 			aAlphaAnimations.get(oldActive).setTargetAlpha(0).start(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					EverVoidClient.delRootNode(oldActive);
 				}
 			});
 		}
 		final EverView newView = aViewMap.get(type);
 		aActiveView = newView;
 		EverVoidClient.addRootNode(newView.getNodeType(), newView);
 		aAlphaAnimations.get(aActiveView).setTargetAlpha(1).start();
 	}
 }
