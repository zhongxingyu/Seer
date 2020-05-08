 package com.sun.darkstar.example.snowman.client.handler.message;
 
 import com.sun.darkstar.example.snowman.client.handler.ClientHandler;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
 import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
 import com.sun.darkstar.example.snowman.common.protocol.processor.IClientProcessor;
 import com.sun.darkstar.example.snowman.game.state.enumn.EGameState;
 import com.sun.darkstar.example.snowman.game.task.enumn.ETask;
 import com.sun.darkstar.example.snowman.game.task.util.TaskManager;
 
 /**
  * <code>MessageProcessor</code> is a processing unit responsible for
  * processing all the messages received by the <code>MessageListener</code>.
  * <p>
  * <code>MessageProcessor</code> generates <code>ITask</code> based on
  * the received messages and buffers these <code>ITask</code> inside
  * <code>TaskManager</code> for processing.
  * <p>
  * <code>MessageProcessor</code> is created and attached to its parent
  * <code>ClientHandler</code>.
  * 
  * @author Yi Wang (Neakor)
  * @version Creation date: 05-27-2008 11:57 EST
  * @version Modified date: 08-07-2008 15:10 EST
  */
 public class MessageProcessor implements IClientProcessor {
 	/**
 	 * The <code>ClientHandler</code> this processor is attached to.
 	 */
 	private final ClientHandler handler;
 	/**
 	 * The ID number of the local controlled player.
 	 */
 	private int myID;
 
 	/**
 	 * Constructor of <code>MessageProcessor</code>.
 	 * @param handler The <code>ClientHandler</code> this processor is attached to.
 	 */
 	public MessageProcessor(ClientHandler handler) {
 		this.handler = handler;
 	}
 
 	@Override
 	public void ready() {
 		TaskManager.getInstance().createTask(ETask.Ready);
 	}
 
 	@Override
 	public void newGame(int myID, String mapname) {
 		this.myID = myID;
 		TaskManager.getInstance().createTask(ETask.GameState, EGameState.BattleState);
 	}
 
 	@Override
 	public void startGame() {
 		TaskManager.getInstance().createTask(ETask.StartGame);
 	}
 
 	@Override
 	public void endGame(EEndState endState) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addMOB(int objectID, float x, float y, EMOBType objType, ETeamColor team) {
 		TaskManager.getInstance().createTask(ETask.AddMOB, objectID, objType, team, x, y, (objectID == this.myID));
 	}
 
 	@Override
 	public void moveMOB(int objectID, float startx, float starty, float endx, float endy) {
 		if(objectID == this.myID) return;
 		TaskManager.getInstance().createTask(ETask.MoveCharacter, objectID, startx, starty, endx, endy);
 	}
 
 	@Override
 	public void removeMOB(int objectID) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void stopMOB(int objectID, float x, float y) {
 		TaskManager.getInstance().createTask(ETask.Correction, objectID, x, y);
 	}
 
 	@Override
 	public void attachObject(int sourceID, int targetID) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void attacked(int sourceID, int targetID, int hp) {
		TaskManager.getInstance().createTask(ETask.Attacking, sourceID, targetID, 10, (sourceID == this.myID));
 	}
 
 	@Override
 	public void respawn(int objectID, float x, float y) {
 		TaskManager.getInstance().createTask(ETask.Respawn, objectID, x, y, (objectID == this.myID));
 	}
 }
