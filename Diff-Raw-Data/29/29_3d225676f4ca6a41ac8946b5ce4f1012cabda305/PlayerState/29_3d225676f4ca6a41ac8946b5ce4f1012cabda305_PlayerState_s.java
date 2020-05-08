 package de.nordakademie.nakjava.server.shared.serial;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
 import de.nordakademie.nakjava.client.shared.PlayerStateListener;
 
 public class PlayerState implements Serializable {
 	private PlayerModel model;
 	private List<ActionContext> actions;
 	private long batch;
 	private PlayerStateListener stateListener;
 	private boolean dirty = false;
 
	private static ExecutorService threadPool = Executors.newFixedThreadPool(5);

 	public PlayerState(PlayerStateListener stateListener) {
 		this.stateListener = stateListener;
 		model = new PlayerModel();
 	}
 
 	public PlayerModel getModel() {
 		return model;
 	}
 
	public void setModel(PlayerModel model) {
 		this.model = model;
 		dirty = true;
 	}
 
	public void setBatch(long batch) {
 		this.batch = batch;
 	}
 
 	public long getBatch() {
 		return batch;
 	}
 
 	public List<ActionContext> getActions() {
 		return new ArrayList<ActionContext>(actions);
 	}
 
	public void setActions(List<ActionContext> actions) {
 		this.actions = actions;
 		dirty = true;
 	}
 
	public void triggerChangeEvent() {
 		final PlayerState playerState = this;
 		if (dirty) {

			threadPool.execute(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
						stateListener.stateChanged(playerState);
 					} catch (RemoteException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				}
			});
 
 			dirty = false;
 		}
 	}
 }
