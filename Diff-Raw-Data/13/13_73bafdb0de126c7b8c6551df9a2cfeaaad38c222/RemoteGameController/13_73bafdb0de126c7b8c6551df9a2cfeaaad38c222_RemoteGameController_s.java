 import javax.microedition.io.StreamConnection;
 import java.io.IOException;
 import java.lang.System;
 
 /**
  * Remote GameController from the point of view
  * of LocalArbiter.
  */
 class RemoteGameController
 	implements GameController,
 			   RemoteGameControllerTransport.Parent {
 
 	protected LocalArbiter arbiter;
 	protected RemoteGameControllerTransport transport;
 
 	/**
 	 * RemoteGameController callbacks.
 	 */
 	public interface Parent {
 		public void handleRemoteGameControllerInfo(
 				RemoteGameController rgc,
 				String str
 				);
 	}
 
 	long lastPing;
 
 	private Parent parent;
 
 	public RemoteGameController(Parent parent)
 		throws IOException {
 		this.parent = parent;
 		touchLastPing();
 	}
 
 	public void registerChildTransport(
 			RemoteGameControllerTransport transport
 			) {
 		this.transport = transport;
 	}
 
 	public boolean isActive() {
 		return arbiter != null;
 	}
 
 	public boolean isPingValid() {
 		return (lastPing + 1000 * 11) > System.currentTimeMillis();
 	}
 
 	public void connectedTo(LocalArbiter arbiter) {
 		this.arbiter = arbiter;
 	}
 
 	public void pass(int c) {
 		transport.sendMsg(
 				Protocol.PASS + " "
 				+ String.valueOf(c)
 				);
 	}
 
 	public void move(int x, int y, int c) {
 		transport.sendMsg(
 				Protocol.MOVE + " "
 				+ String.valueOf(x) + " "
 				+ String.valueOf(y) + " "
 				+ String.valueOf(c)
 				);
 	}
 
 	public void placeStone(int x, int y, int c, int s) {
 		transport.sendMsg(
 				Protocol.PLACE_STONE + " "
 				+ String.valueOf(x) + " "
 				+ String.valueOf(y) + " "
 				+ String.valueOf(c) + " "
 				+ String.valueOf(s)
 				);
 	}
 
 	public void shutdown() {
 		//
 	}
 
 	protected void touchLastPing() {
 		lastPing = System.currentTimeMillis();
 	}
 
 	public void clearBoard() {
 		transport.sendMsg(
 				Protocol.CLEAR_BOARD
 				);
 	}
 
 	public void gameInfo(String s) {
 		transport.sendMsg(
 				Protocol.GAME_INFO + " " +
 				s
 				);
 	}
 	public void sendNewPong() {
 		transport.sendMsg(Protocol.PONG);
 	}
 
 	public void receiveMsg(String msg) {
 		if (arbiter == null) {
 			parent.handleRemoteGameControllerInfo(
 					this,
 					"but the arbiter is null!"
 					);
 		}
 
 		Tokenizer s = new Tokenizer(msg);
 		String cmd = s.next();
 		if (cmd.equals(Protocol.MOVE_REQUEST)) {
 			String xs = s.next();
 			String ys = s.next();
 			if (xs.equals("") || ys.equals("")) {
 				protocolFailure("no enought arguments for cmd: " + cmd);
 				return;
 			}
 			int x = Integer.parseInt(xs);
 			int y = Integer.parseInt(ys);
 			arbiter.moveRequest(this, x, y);
 			return;
 		} else if (
 				(cmd.equals(Protocol.HANDLE_COLOR))
 				|| (cmd.equals(Protocol.UNHANDLE_COLOR))
 				)
 		{
 			String cs = s.next();
 			if (cs.equals("")) {
 				protocolFailure("no color argument for cmd: " + cmd);
 				return;
 			}
 			int c = Integer.parseInt(cs);
 			if (cmd.equals(Protocol.HANDLE_COLOR)) {
 				arbiter.handleColor(this, c);
 			} else {
 				arbiter.unhandleColor(this, c);
 			}
 			return;
 		} else if (
 				cmd.equals(Protocol.PASS_REQUEST)
 				)
 		{
 			arbiter.passRequest(this);
 			return;
 		} else if (
 				cmd.equals(Protocol.PING)
 				)
 		{
 			touchLastPing();
 			sendNewPong();
 			return;
 		}
 
 		protocolFailure("unknown command: '" + cmd + "'");
 	}
 
 	protected void protocolFailure(String s) {
 		// TODO: FIXME
 		parent.handleRemoteGameControllerInfo(
 				this,
 				"GC Transport: protocol failure: " + s
 				);
 	}
 
 	public void handleTransportDisconnected(
 			RemoteGameControllerTransport t, String s
 			) {
 
 		parent.handleRemoteGameControllerInfo(
 				this,
 				"GC Transport: disconnected: " + s
 				);
 		arbiter.disconnected(this);
 		arbiter = null;
 	}
 
 	public GameController gameController() {
 		return this;
 	}
 
 	/**
 	 * Called after connection was estabilished.
 	 */
 	public void handleTransportConnected(
 			RemoteGameControllerTransport t, String s
 			) {
 		parent.handleRemoteGameControllerInfo(
 				this,
 				"GC Transport: connected: " + s
 				);
 	}
 
 	public void handleTransportInfo(RemoteGameControllerTransport t, String s) {
 		parent.handleRemoteGameControllerInfo(
 				this,
 				"GC Transport: " + s
 				);
 	}
 
 	public String name() {
 		return "remote";
 	}
 }
