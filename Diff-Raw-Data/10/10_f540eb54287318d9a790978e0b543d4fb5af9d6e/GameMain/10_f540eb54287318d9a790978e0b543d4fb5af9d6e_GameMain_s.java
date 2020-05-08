 package com.jesse.game;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Random;
 
import org.lwjgl.LWJGLUtil;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.jesse.game.data.GameSnapshot;
 import com.jesse.game.data.JoinCommand;
 import com.jesse.game.data.PlayerHolder;
 import com.jesse.game.listeners.ConnectionStatusListener;
 import com.jesse.game.listeners.OnUpdateReceivedListener;
 import com.jesse.game.net.ServerReceiver;
 import com.jesse.game.objects.Vector2i;
 import com.jesse.game.states.GameplayState;
 import com.jesse.game.states.LoadingScreen;
 import com.jesse.game.states.SplashPage;
 import com.jesse.game.utils.Constants;
 
 public class GameMain extends StateBasedGame implements ConnectionStatusListener {
 
 	private GameplayState mGameplayState;
 	
 	private GameSnapshot mGameSnapshot;
 	private GameSnapshot mNextSnapshot;
 	
 	private HashMap<PlayerHolder, String> mMessageQueue = new HashMap<PlayerHolder, String>();
 	
 	private PlayerHolder mThisPlayer;
  	
 	public PrintWriter outWriter;
 	private ServerReceiver mReceiver;
 	
 	private OnUpdateReceivedListener mUpdateListener;
 	
 	public static final int SCREEN_WIDTH = 1024;
 	public static final int SCREEN_HEIGHT = 576;
 	public static final int SCALE = 2;
 	
 	/**
 	 * Launcher
 	 * @throws SlickException 
 	 */
 	
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new GameMain("Pokemon"));
 		app.setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 		app.setTargetFrameRate(240);
 		app.start();
 	}
 
 	public GameMain(String name) {
 		super(name);
 	}
 	
 	@Override
 	public void initStatesList(GameContainer arg0) throws SlickException {
 		addState(new SplashPage(Constants.SPLASH_STATE_ID));
 		addState(new LoadingScreen(Constants.LOADING_STATE_ID));
 	}
 	
 	@Override
 	public synchronized void preUpdateState(GameContainer gc, int delta) throws SlickException {
 		if(mReceiver != null) {
 			if(mNextSnapshot != null) {
 				mGameSnapshot.update(mNextSnapshot, this);
 				
 				if(mUpdateListener != null)
 					mUpdateListener.onUpdateReceived(mGameSnapshot);
 				
 				mNextSnapshot = null;
 				notifyAll();
 			}
 		}
 	}
 	
 	public void connectToServer(String name, String ip) {
 		long time = System.currentTimeMillis();
 //		enterState(Constants.LOADING_STATE_ID);
 		
 		mGameSnapshot = new GameSnapshot();
 		mThisPlayer = new PlayerHolder(new Random().nextInt(1000), new Vector2i(17, 16), name);
 		mGameSnapshot.addPlayer(mThisPlayer);
 		
 		Socket socket = null;
 		BufferedReader in = null;
 		
 		try {
 			socket = new Socket(ip, 7377);
 			outWriter = new PrintWriter(socket.getOutputStream(), true);
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		//TODO this section is a disaster
 		
 		ServerReceiver receiver = new ServerReceiver(in, this, this, true);
 		Thread thread = new Thread(receiver);
 		thread.start();
 		JoinCommand join = new JoinCommand(mThisPlayer);
 		outWriter.println(join.getGson());
 		try {
 			thread.join();
 			while(System.currentTimeMillis() - time < 100l) { }
 			onJoined();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 //		receiver.stop();
 		
 		mReceiver = new ServerReceiver(in, this, this, false);
 		mReceiver.hasJoined();
 		new Thread(mReceiver).start();
 		
 //		JoinCommand join = new JoinCommand(mThisPlayer);
 //		outWriter.println(join.getGson());
 	}
 	
 	public void setUpdateSnapshot(GameSnapshot update) {
 		mNextSnapshot = update;
 	}
 	
 	public GameSnapshot getUpdateSnapshot() {
 		return mNextSnapshot;
 	}
 	
 	public void setGameSnapshot(GameSnapshot snapshot) {
 		mGameSnapshot = snapshot;
 	}
 	
 	public GameSnapshot getGameSnapshot() {
 		return mGameSnapshot;
 	}
 
 	@Override
 	public void onJoined() {
 		try {
 			mGameplayState = new GameplayState(Constants.GAME_STATE_ID);
 			mGameplayState.init(getContainer(), this);
 			mGameplayState.loadPlayer(mThisPlayer);
 			addState(mGameplayState);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		enterState(Constants.GAME_STATE_ID);
 	}
 
 
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setOnUpdateListener(GameplayState state) {
 		mUpdateListener = state;
 	}
 	
 	public PlayerHolder getUserPlayerHolder() {
 		return mThisPlayer;
 	}
 	
 	public void showLoadingScreen() {
 		enterState(Constants.LOADING_STATE_ID);
 	}
 	
 	public synchronized HashMap<PlayerHolder, String> getMessageQueue() {
 		return mMessageQueue;
 	}
 	
 	public synchronized void loadMessageQueue(HashMap<String, String> map) {
 		for (String id : map.keySet()) {
 			PlayerHolder holder = mGameSnapshot.getPlayers().get(Integer.valueOf(id));
 			if(holder != null)
 				mMessageQueue.put(holder, map.get(id));
 		}
 	}
 	
 	public synchronized void addSystemMessageToQueue(String msg) {
 		mMessageQueue.put(null, msg);
 	}
 	
 }
