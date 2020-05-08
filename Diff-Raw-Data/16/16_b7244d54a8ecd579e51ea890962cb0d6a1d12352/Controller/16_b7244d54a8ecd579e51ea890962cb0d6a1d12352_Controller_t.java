 package dk.stacktrace.risk.controller;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import dk.stacktrace.risk.Main;
 import dk.stacktrace.risk.game_logic.Board;
 import dk.stacktrace.risk.game_logic.Player;
 import dk.stacktrace.risk.game_logic.RiskGame;
 import dk.stacktrace.risk.game_logic.Territory;
 import dk.stacktrace.risk.game_logic.enumerate.ArmyColor;
 import dk.stacktrace.risk.game_logic.enumerate.GamePhase;
 import dk.stacktrace.risk.gui.Army;
 
 public class Controller implements OnTouchListener{
 	
 	private RiskGame game;
 	private Board board;
 	private Main main;
 
 	public Controller(Main main) {
 		this.main = main;
 	}
 
 	
 	
 	public ArrayList<Territory> getAllTerritories()
 	{
 		return board.getAllTerritories();
 	}
 	
 	
 	public void createTestGame()
 	{
 		Player p1 = new Player("Hypestar", ArmyColor.PURPLE);
 		Player p2 = new Player("Napoleon", ArmyColor.BLUE);
 		Player p3 = new Player("Adolf", ArmyColor.GREEN);
 		Player p4 = new Player("Patton", ArmyColor.YELLOW);
 		Player p5 = new Player("Ghenkis Khan", ArmyColor.ORANGE);
 		Player p6 = new Player("Bush", ArmyColor.CYAN);
 		
 		ArrayList<Player> players = new ArrayList<Player>();
 
 		players.add(p1);
 		players.add(p2);
 		//players.add(p3);
 		//players.add(p4);
 		//players.add(p5);
 		//players.add(p6);
 		
 		game = new RiskGame(players);
 		board = game.getBoard();
 		
 		dealTerritories();
 		game.setInitialReinforcementForAllPlayers();
 	}
 	
 	public void dealTerritories()
 	{
 		game.dealTerritories();
 	}
 	
 	public int calcReinforcementBonus(Player player)
 	{
 		return board.calcReinforcementBonus(player);
 	}
 	
 	public Board getBoard() {
 		return board;
 	}
 	
 	public Player getActivePlayer()
 	{
 		return game.getActivePlayer();
 	}
 
 
 
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		if (v instanceof Army && event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			Army army = (Army) v;
 			
 			switch (game.getGamePhase()) {
 			
 			case INITIAL_REINFORCEMENT:
 				Log.v("Controller", "INITIAL_REINFORCEMENT");
 				if (army.getTerritory().getOwner().equals(getActivePlayer()))
 				{
 					army.getTerritory().reinforce(getActivePlayer().deploy());
 					if(game.initialReinforcementPhaseIsDone())
 					{
 						game.setGamePhase(GamePhase.REINFORCEMENT);
 						Log.v("ON Touch", "Initial Deployment phase is done");
 					}
 					endTurn();
 					main.update();
 					return true;
 				}	
 				break;
 				
 				
 			case REINFORCEMENT:
 				Log.v("Controller", "REINFORCEMENT");
 				if (army.getTerritory().getOwner().equals(getActivePlayer()))
 				{
 					army.getTerritory().reinforce(getActivePlayer().deploy());
 					if(!game.getActivePlayer().hasTroopsToDeploy())
 					{
 						game.setGamePhase(GamePhase.ATTACK);
 						Log.v("ON Touch", "Reinforcement phase is done - advancing to attack phase");
 					}
 					main.update();
 					return true;
 				}
 				break;
 				
				
 			case ATTACK:
 				Log.v("Controller", "ATTACK");
 				if (army.getTerritory().getOwner().equals(getActivePlayer()) && game.getSourceTerritory() == null)
 				{
 					game.setSourceTerritory(army.getTerritory());
 					return true;
 				}
 				else if (!army.getTerritory().getOwner().equals(getActivePlayer()) && game.getSourceTerritory() != null)
 				{
 					game.setTargetTerritory(army.getTerritory());
 					Log.v("ATTACK", game.getSourceTerritory().getName() + " is attacking " + game.getTargetTerritory().getName());
					Log.v("ATTACK", "For testing purposes we are now going to tacticalmove phase");
					game.setGamePhase(GamePhase.TACTICALMOVE);
					game.setSourceTerritory(null);
					game.setTargetTerritory(null);
 				}
 				break;
				
				
 			case TACTICALMOVE:
 				Log.v("Controller", "TACTICALMOVE");
 				if (army.getTerritory().getOwner().equals(getActivePlayer()))
 				{
 					if(army.getTerritory() == game.getSourceTerritory())
 					{
 						Log.v("TATICALMOVE", "Source territory " + game.getSourceTerritory().getName() + " has been deselected");
						game.setSourceTerritory(null);
 					}
 					else if(game.getSourceTerritory() == null)
 					{
 						game.setSourceTerritory(army.getTerritory());
						Log.v("TATICALMOVE", "Source territory has been set to : " + game.getSourceTerritory().getName());
 					}
 					else
 					{
 						game.setTargetTerritory(army.getTerritory());
 						Log.v("TATICALMOVE", "Ready to move troops between " + game.getSourceTerritory().getName() + " and " + game.getTargetTerritory().getName());
 					}
 					return true;
 				}
 				break;
 
 			default:
 				break;
 			}
 		}
 		
 		
 		//////////////////////////////
 		
 		return false;
 	}
 	
 	public void endTurn()
 	{
 		game.endTurn();
 	}
 	
 }
