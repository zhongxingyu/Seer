 import java.util.Vector;
 
 /**
  * Local implementation of arbiter.
  *
  * This class is actual arbiter implementation.
  */
 class LocalArbiter
 	implements Arbiter,
 			   LocalArbiterListener.Parent,
 			   BoardLogic.Parent
 {
 	/**
 	 * Custom exception.
 	 */
 	public class CreationError extends Exception {
 		public CreationError(String s) {
 			super(s);
 		}
 	}
 
 	protected int nextMoveColor = BoardLogic.COLOR_BLACK;
 	protected Parent parent;
 
 	protected GameController blackController;
 	protected GameController whiteController;
 
 	protected LocalArbiterListener listener;
 	protected LocalArbiterPoller poller;
 	protected BoardLogic boardLogic;
 
 	protected Vector connectedControllers = new Vector();
 
 	public LocalArbiter(Parent parent) throws CreationError {
 		this.parent = parent;
 		poller = new LocalArbiterPoller();
 		poller.start();
 
 		boardLogic = new BoardLogic(this);
 
 		try {
 			listener = LocalArbiterListenerFactory.Create(
 				this, LocalArbiterListener.BLUETOOTH
 				);
 		} catch (InvalidArgumentException e) {
 			throw new CreationError(e.getMessage());
 		}
 		listener.start();
 	}
 
 	public void handleListenerUp() {
 		parent.handleArbiterInitFinished();
		parent.handleArbiterMsg("server started");
 	}
 
 	public void connected(GameController ngc) {
 		ngc.connectedTo(this);
 		initGameControllerBoard(ngc);
 		appendToControllersList(ngc);
 	}
 
 	protected void appendToControllersList(GameController ngc) {
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 				connectedControllers.elementAt(i)
 				);
 			if (gc == null) {
 				connectedControllers.setElementAt(ngc, i);
 				return;
 			}
 		}
 		// no nulls, just grow
 		connectedControllers.addElement(ngc);
 	}
 
 	public void disconnected(GameController gc) {
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			if (connectedControllers.elementAt(i) == gc) {
 				connectedControllers.setElementAt(null, i);
 			}
 		}
 
 		if (whiteController == gc) {
 			whiteController = null;
 			gameInfo("white color is now free");
 		}
 		if (blackController == gc) {
 			blackController = null;
 			gameInfo("black color is now free");
 		}
 		gc.shutdown();
 	}
 
 	public void shutdown() {
 		poller.stop();
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 					connectedControllers.elementAt(i)
 					);
 			if (gc != null) {
 				gc.shutdown();
 			}
 		}
 	}
 
 	public void handleColor(GameController gc, int color) {
 		switch (color) {
 			case COLOR_WHITE:
 				if (whiteController == null) {
 					whiteController = gc;
 					gameInfo("white color is now occupied");
 				} else {
 					gameInfo(gc, "color already occupied");
 				}
 				break;
 			case COLOR_BLACK:
 				if (blackController == null) {
 					gameInfo("black color is now occupied");
 					blackController = gc;
 				} else {
 					gameInfo(gc, "color already occupied");
 				}
 				break;
 		}
 	}
 
 	public void unhandleColor(GameController gc, int color) {
 		switch (color) {
 			case COLOR_WHITE:
 				if (whiteController == gc) {
 					whiteController = null;
 					gameInfo("white color is now free");
 				} else {
 					gameInfo(gc, "you are not playing white");
 				}
 				break;
 			case COLOR_BLACK:
 				if (blackController == gc) {
 					blackController = null;
 					gameInfo("black color is now free");
 				} else {
 					gameInfo(gc, "you are not playing black");
 				}
 				break;
 		}
 	}
 
 	protected boolean checkMoveConditions(GameController gcx) {
 		if (whiteController == null || blackController == null) {
 			gcx.gameInfo("both colors need to be occupied");
 			return false;
 		}
 
 		if (gcx == whiteController && gcx == blackController) {
 		} else if (gcx == whiteController) {
 			if (nextMoveColor != BoardLogic.COLOR_WHITE) {
 				gcx.gameInfo("not your turn");
 				return false;
 			}
 		} else if (gcx == blackController) {
 			if (nextMoveColor != BoardLogic.COLOR_BLACK) {
 				gcx.gameInfo("not your turn");
 				return false;
 			}
 		} else {
 			gcx.gameInfo("pick your side first");
 			return false;
 		}
 		return true;
 	}
 
 	protected void switchCurrentColor() {
 		if (nextMoveColor == BoardLogic.COLOR_BLACK) {
 			nextMoveColor = BoardLogic.COLOR_WHITE;
 		} else {
 			nextMoveColor = BoardLogic.COLOR_BLACK;
 		}
 	}
 
 	public void passRequest(GameController gcx) {
 		if (!checkMoveConditions(gcx)) {
 			return;
 		}
 
 		try {
 			boardLogic.passRequest(nextMoveColor);
 			switchCurrentColor();
 		} catch (InvalidArgumentException e) {
 			gcx.gameInfo("illegal move");
 		}
 	}
 
 
 	public void moveRequest(GameController gcx, int x, int y) {
 
 		if (!checkMoveConditions(gcx)) {
 			return;
 		}
 
 		try {
 			boardLogic.moveRequest(x, y, nextMoveColor);
 			switchCurrentColor();
 		} catch (InvalidArgumentException e) {
 			gcx.gameInfo("illegal move");
 		}
 	}
 
 	public void initGameControllerBoard(GameController gc) {
 		gc.clearBoard();
 
 		for (int x = 0; x < boardLogic.getSize(); ++x) {
 			for (int y = 0; y < boardLogic.getSize(); ++y) {
 				int c = boardLogic.getColor(x, y);
 				int s = boardLogic.getState(x, y);
 				if (c != BoardLogic.COLOR_NONE) {
 					gc.placeStone(x, y, c, s);
 				}
 			}
 		}
 	}
 
 	public void gameInfo(String s) {
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 				connectedControllers.elementAt(i)
 				);
 			if (gc != null) {
 				gameInfo(gc, s);
 			}
 		}
 	}
 
 	public void gameInfo(GameController gc, String s) {
 		gc.gameInfo(s);
 	}
 
 	public void handleListenerInfo(LocalArbiterListener l, String msg) {
 	}
 
 	public void handleListenerUp(LocalArbiterListener l) {
 		parent.handleArbiterInitFinished();
 	}
 
 	public void handleListenerDown(LocalArbiterListener l, String msg) {
 	}
 
 	public void handleControllerConnected(RemoteGameController c) {
 	}
 
 	public void handleControllerTransportConnected(
 			RemoteGameControllerTransport transport
 			) {
 		parent.handleArbiterMsg("new client connected");
 		poller.registerNewRemoteGameControllerTransport(
 				transport
 				);
 		connected(transport.gameController());
 	}
 
 	public void handleControllerDisconnected(RemoteGameController c) {
 		parent.handleArbiterMsg("client disconnected");
 	}
 
 	public void handleRemoteGameControllerInfo(
 			RemoteGameController c, String s
 			) {
 		parent.handleArbiterMsg("arbiter: " + s);
 	}
 
 	public void handleBoardStoneChange (
 			BoardLogic bl, int x, int y, int color, int state
 			)
 	{
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 					connectedControllers.elementAt(i)
 					);
 			if (gc != null) {
 				gc.placeStone(x, y, color, state);
 			}
 		}
 	}
 
 	public void handleMoveCommited(int x, int y, int c)
 	{
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 					connectedControllers.elementAt(i)
 					);
 			if (gc != null) {
 				gc.move(x, y, c);
 			}
 		}
 	}
 
 	public void handlePassCommited(int c)
 	{
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 					connectedControllers.elementAt(i)
 					);
 			if (gc != null) {
 				gc.pass(c);
 			}
 		}
 	}
 
 	public void handleBoardResize(
 			BoardLogic bl, int new_size
 			)
 	{
 		// TODO: implement
 	}
 
 	public void handleBoardClear(
 			BoardLogic bl
 			)
 	{
 		nextMoveColor = BoardLogic.COLOR_BLACK;
 		for (int i = 0; i < connectedControllers.size(); ++i) {
 			GameController gc = (GameController)(
 					connectedControllers.elementAt(i)
 					);
 			if (gc != null) {
 				gc.clearBoard();
 			}
 		}
 	}
 
 }
