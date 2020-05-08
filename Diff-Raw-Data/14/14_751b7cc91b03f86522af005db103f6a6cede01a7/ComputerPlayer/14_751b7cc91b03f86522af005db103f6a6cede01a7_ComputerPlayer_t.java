 package com.testgame.player;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import android.graphics.Point;
 import android.util.Log;
 
 import com.testgame.OnlineGame;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.mechanics.unit.Base;
 import com.testgame.mechanics.unit.Ditz;
 import com.testgame.mechanics.unit.Jock;
 import com.testgame.mechanics.unit.Nerd;
 
 public class ComputerPlayer extends APlayer {
 	
 	JSONArray actionsToPerform;
 	JSONArray array;
 	OnlineGame game;
 	private boolean turn = false;
 	
 	public ComputerPlayer(String name) {
 		super(name);
 	}
 	
 	public void startTurn(JSONArray array){
 		if(turn || game.getPlayer().isTurn())
 			return;
 		turn = true;
 		Log.d("Array", array.toString());
 		Log.d("Array", array.length()+"");
 		this.actionsToPerform = array;
 		this.beginTurn();
 		performNext(); // perform all of the animations
 		
 		
 		
 		
 		
 	}
 	
 	public void performNext() {
 		if(actionsToPerform.length() == 0){
 			if(game.isFirstTurn()) 
 				game.incrementCount();
 			this.endTurn();
 			if(game.endGame()){
 				return;
 			}
 			
 			 // this calls turn init on all the units
 			game.getGameScene().activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	game.getGameScene().endTurnDialog("Begin Turn!");
         	    	turn = false;
           			 
         	    }
         	});
			return;
 		}
 		for (int i = 0; i < actionsToPerform.length(); i++) {
 			
 
 			if (actionsToPerform.isNull(i)) {
 				if (i == actionsToPerform.length() - 1) {
 					if(game.isFirstTurn()) 
 						game.incrementCount();
 					
 					this.endTurn();
 					 // this calls turn init on all the units
 					if(game.endGame())
 						return;
 					game.getGameScene().activity.runOnUiThread(new Runnable() {
 		        	    @Override
 		        	    public void run() {
 		        	    	game.getGameScene().endTurnDialog("Begin Turn!");
 		        	    	turn = false;
 		          			 
 		        	    }
 		        	});
 				}
 				else continue; // performed this action already.
 			}
 			
 			else {
 		
 				try {
 					
 					JSONObject nextAction = actionsToPerform.getJSONObject(i);
 					String moveType = nextAction.getString("MoveType");
 					
 					int unitX = nextAction.getInt("UnitX");
 					int unitY = nextAction.getInt("UnitY");
 					
 					Log.d("Unitx", unitX+"");
 					Log.d("Unity", unitY+"");
 					AUnit unit = game.gameMap.getOccupyingUnit(unitX, unitY);
 					
 					
 					if (moveType.equals("MOVE")) {
 						//Log.d("Moving", "Moving");
 						
 						int destX = nextAction.getInt("DestX");
 						int destY = nextAction.getInt("DestY");
 						int energy = nextAction.getInt("Energy");
 						
 						actionsToPerform.put(i, null); // finished action, clear it out
 						if(unit == null){
 							Log.d("Unit", "Null");
 						}
 						unit.computerMove(destX, destY, energy, this);
 					}
 					
 					else if (moveType.equals("ATTACK")) {
 						
 						
 						int targetX = nextAction.getInt("OppX");
 						int targetY = nextAction.getInt("OppY");
 						int energy = nextAction.getInt("Energy");
 						int attack = nextAction.getInt("Attack");
 						
 						AUnit target = game.gameMap.getOccupyingUnit(targetX, targetY);
 						
 						actionsToPerform.put(i, null); // finished action, clear it out
 						
 						unit.computerAttack(target, attack, energy, this);
 					}
 					
 					return;
 					
 				} catch (JSONException e) {
 					// Failure getting the next move
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	public void init(JSONArray array) {
 		
 		
 		
 		Point[] spawns;
 		
 		if(game.isFirstTurn()){
 			spawns = game.resourcesManager.getSpawn1(game.resourcesManager.mapString);
 		}
 		else{
 			spawns = game.resourcesManager.getSpawn2(game.resourcesManager.mapString);
 		}
 		try{
 			int j = 0;
 			for(Point i : spawns){
 				if(j == spawns.length-1){
 					AUnit unitbase = new Base(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unitbase.init();
 					game.getCompPlayer().setBase(unitbase);
 				}
 				else if(array.getInt(j) == 1){
 					AUnit unit = new Nerd(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init(); 
 					game.getCompPlayer().addUnit(unit);
 					
 				}
 				else if(array.getInt(j) == 2){
 					AUnit unit = new Ditz(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init();
 					game.getCompPlayer().addUnit(unit);
 					
 				}
 				else if(array.getInt(j) == 0){
 					AUnit unit = new Jock(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init(); 
 					game.getCompPlayer().addUnit(unit);
 
 				}
 				j++;
 		 	}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		
 		
 		game.incrementCount();
 		if(game.isFirstTurn()) {
 			
 			game.getGameScene().activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	game.getGameScene().endTurnDialog("Your Turn!");
           			 
         	    }
         	});
 		}
 		
 	}
 	
 	public void setGame(OnlineGame game){
 		this.game = game;
 	}
 
 }
