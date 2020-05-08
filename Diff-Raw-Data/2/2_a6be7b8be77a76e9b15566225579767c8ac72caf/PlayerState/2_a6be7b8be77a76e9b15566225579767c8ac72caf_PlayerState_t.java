 package de.nordakademie.nakjava.server.shared.serial;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.nordakademie.nakjava.client.shared.PlayerStateListener;
 
 //it is ensured that all methods in this class are called from a single threaded-context
 //so only write operations need to be synchronized so that the new created thread in triggerChangeEvent() that invokes RMI gets consistent data when finally executed
 public class PlayerState implements Serializable {
 	private PlayerModel model;
 	private List<ActionContext> actions;
 	private long batch;
 	private PlayerStateListener stateListener;
 	private boolean dirty = false;
 
 	public PlayerState(PlayerStateListener stateListener) {
 		this.stateListener = stateListener;
 		model = new PlayerModel();
 	}
 
 	public PlayerModel getModel() {
 		return model;
 	}
 
 	public synchronized void setModel(PlayerModel model) {
 		this.model = model;
 		dirty = true;
 	}
 
 	public synchronized void setBatch(long batch) {
 		this.batch = batch;
 	}
 
 	public long getBatch() {
 		return batch;
 	}
 
 	public List<ActionContext> getActions() {
 		return new ArrayList<ActionContext>(actions);
 	}
 
 	public synchronized void setActions(List<ActionContext> actions) {
 		this.actions = actions;
 		dirty = true;
 	}
 
 	public synchronized void triggerChangeEvent() {
 		final PlayerState playerState = this;
 
 		if (dirty) {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
						synchronized (playerState) {
 							stateListener.stateChanged(playerState);
 						}
 					} catch (RemoteException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				}
 			}).start();
 
 			dirty = false;
 		}
 	}
 }
