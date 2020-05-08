 package de.nordakademie.nakjava.server.internal;
 
 import java.rmi.RemoteException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import de.nordakademie.nakjava.client.shared.PlayerControlListener;
 import de.nordakademie.nakjava.client.shared.PlayerStateListener;
 import de.nordakademie.nakjava.server.shared.proxy.PlayerControl;
 import de.nordakademie.nakjava.server.shared.proxy.PlayerControlImpl;
 import de.nordakademie.nakjava.server.shared.serial.PlayerState;
 
 public class Player {
 	private PlayerState state;
 	private PlayerControl control;
 
 	private static ExecutorService threadPool = Executors.newCachedThreadPool();
 
 	public Player(PlayerControlListener controlListener,
 			PlayerStateListener stateListener) {
 
 		try {
 			control = new PlayerControlImpl(controlListener);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		state = new PlayerState(stateListener);
 	}
 
 	public PlayerState getState() {
 		return state;
 	}
 
 	public PlayerControl getControl() {
 		return control;
 	}
 
 	public void triggerChangeEvent() {
 		threadPool.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				state.triggerChangeEvent();
 				control.triggerChangeEvent();
 			}
 
 		});
 
 	}
 
 }
