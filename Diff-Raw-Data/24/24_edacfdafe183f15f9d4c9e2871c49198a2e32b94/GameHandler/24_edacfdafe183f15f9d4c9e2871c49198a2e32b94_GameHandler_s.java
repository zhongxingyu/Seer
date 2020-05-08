 package com.suckless;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector2;
 
 
 public class GameHandler {
 	
 	// St�rrelsen p� arrayet
 	public int xSquares = 10;
 	public int ySquares = 10;
 	public StateHandler handle;
 	public GameObject[] player1;
 	public GameObject[] player2;
 	public Player[] players;
 	public Dictionary<Player,Command> commandDict;
 	private ShufflerHeleDagen megaShuffler;
 	
 	// public SelectorGrande selector;
 	private class playerEventHandler implements EventListener{
 
 		Player playerObject;
 		GameHandler gameHandler;
 		playerEventHandler(Player plyObj, GameHandler handler){
 			playerObject = plyObj;
 			gameHandler = handler;
 			
 		}
 		@Override
 		public void Invoke() {
 			gameHandler.onPlayerSelectEvent(playerObject);
 			
 		}
 	}
 	List<playerEventHandler> playerEventHandlers;
 	GameHandler(Player[] players1){
 		handle = new StateHandler(10,10);
 		playerEventHandlers = new LinkedList<playerEventHandler>();
 		players = players1;
 		for(Player ply : players){
 			playerEventHandler pListener = new playerEventHandler(ply,this);
 			playerEventHandlers.add(pListener);
 			EventBase evt = ply.getEvent();
 			if(evt != null){
 				evt.AddListener(pListener);
 			}
 		}
 		commandDict = new Hashtable<Player,Command>();
 		
 	}
 	void onPlayerSelectEvent(Player player){
 		Command cmd = commandDict.get(player);
 		commandDict.put(player,cmd.Select());
 	}
 	
 //	// Init g�gl
 //	GameHandler(GameObject[] x1, GameObject[] x2){
 //		// Init players
 //		player1 = x1;
 //		player2 = x2;
 //		
 //		
 //		// Initialiser statehandler-baben
 //		handle = new StateHandler(xSquares,ySquares);
 //		
 //		// Send st�rrelsen videre ned til gameobject
 //		this.Handle();
 //	}
 	
 	// Handle functionen til at k�re the stuff
 	public void  Handle()
 	{
 		// update all gameobjects function
		for (int i=0; i < xSquares; i++)
		{
			for (int j=0; j < ySquares; j++)
			{
				if (handle.stateArray[i][j].gameobject != null){
					for (int k=0; k<handle.stateArray[i][j].gameobject.length; k++){
						handle.stateArray[i][j].gameobject[k].Update();
					}
 				}
 			}
 		}
 		for(int i = 0; i<players.length;i++)
 		{
 			players[i].getWorldData(handle.stateArray);
 		}
 	}
 	
 	// Handle functionen til at k�re the stuff
 	public void  updateSelection()
 	{
 		//new GameObject(new Vector2(1,1),1,1,1);
 		for(int i = 0; i<players.length;i++)
 		{
 			Command obj = commandDict.get(players[i]).Update(handle.stateArray, players[i]);
 			// aktiver shuffling
 			
 			if(obj == null){
 				ShufflerHeleDagen shd = new ShufflerHeleDagen(null);	
 				obj = shd;
 			}
 			commandDict.put(players[i],obj);
 			players[i].CommandChanged(commandDict.get(players[i]));
 		}
 	}
 	
 }
 
