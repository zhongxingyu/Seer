 package com.gamezgalaxy.GGS.API.action;
 
 import com.gamezgalaxy.GGS.API.Cancelable;
 import com.gamezgalaxy.GGS.iomodel.Player;
 import com.gamezgalaxy.GGS.server.Server;
 
 /**
  * Actions are things the player will do and will give a response with.
  * For example, if you want to wait for a player to say something and get
  * what he said, use an action instead of an Event.
  * 
  * Call the {@link Action#waitForResponse()} method and it will hang/sleep until
  * a response is gotten.
  */
 public abstract class Action<T> implements Cancelable {
 	
 	private boolean _cancel;
 	
 	private Player p;
 	
 	public Action() { }
 	
 	/**
 	 * Wait for a response from the player chosen in {@link Action#setPlayer(Player)}
 	 * @return
 	 *        The response from the player
 	 * @throws InterruptedException
 	 *                             If any thread interrupted the current thread before or while the current thread was waiting for a response. 
 	 *                             The interrupted status of the current thread is cleared when this exception is thrown.
 	 * @throws IllegalAccessException
 	 *                               This exception can be thrown for two reasons, if no player is set or the player is null <b>OR</b> you are executing this method on the same thread as a packet reading thread. 
 	 */
 	public synchronized T waitForResponse() throws InterruptedException, IllegalAccessException {
 		if (p == null)
 			throw new IllegalAccessException("No target found! The player was not set. Please call setPlayer(Player) before-hand");
 		if (invalidCall(p.getServer(), Thread.currentThread().getId()))
 			throw new IllegalAccessException("You cant use this method on the same thread as the packet reading thread!");
 		setup();
 		while (true) {
 			if (isCompleted() || isCancelled())
 				break;
 			super.wait(0L);
 		}
 		return getResponse();
 	}
 	
 	private boolean invalidCall(Server server, long ID) {
 		for (Player p : server.players) {
 			if (p.getReaderThreadID() == ID)
 				return true;
 		}
 		return false;
 	}
 	
 	protected synchronized void wakeUp() {
 		super.notify();
 	}
 	
 	/**
 	 * Set the player to get a response from.
 	 * @param p
 	 *         The player
 	 */
 	public void setPlayer(Player p) {
 		this.p = p;
 	}
 	
 	/**
 	 * The player to get a response from.
 	 * @return
 	 *        The player
 	 */
 	public Player getPlayer() {
 		return p;
 	}
 	
 	protected abstract void setup();
 	
 	protected abstract T getResponse();
 	
 	/**
 	 * If a response from a player was found.
 	 * @return
 	 *        True if the player responded, false if the player did not.
 	 */
 	public abstract boolean isCompleted();
 	
 	@Override
 	public boolean isCancelled() {
 		return _cancel;
 	}
 	
 	@Override
 	public void Cancel(boolean cancel) {
 		this._cancel = cancel;
		super.notify();
 	}
 
 }
