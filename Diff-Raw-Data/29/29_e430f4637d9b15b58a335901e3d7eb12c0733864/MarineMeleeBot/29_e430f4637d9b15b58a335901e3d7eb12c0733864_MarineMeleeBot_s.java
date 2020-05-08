 package starcraftbot.proxybot.bot;
 
 import javax.swing.JPanel;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 
 import actions.ActionI.ACTION;
 import agents.Aagent;
 import agents.MarineAgent;
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import learning.Parameters;
 import learning.Qlearner;
 import misc.customDateFormatStamp;
 import starcraftbot.proxybot.Game;
 import starcraftbot.proxybot.Constants.Order;
 import starcraftbot.proxybot.Constants.Race;
 import starcraftbot.proxybot.ProxyBot;
 import starcraftbot.proxybot.wmes.UnitTypeWME;
 import starcraftbot.proxybot.wmes.UnitTypeWME.UnitType;
 import starcraftbot.proxybot.wmes.unit.EnemyUnitWME;
 import starcraftbot.proxybot.wmes.unit.PlayerUnitWME;
 import starcraftbot.proxybot.wmes.unit.UnitWME;
 import states.StateFull;
 import states.StateI;
 import states.StateUnitMin;
 /**
  * Example implementation of the StarCraftBot.
  * 
  * This build will tell workers to mine, build additional workers,
  * and build additional supply units.
  */
 
 
 public class MarineMeleeBot implements StarCraftBot {
 
 	/** specifies that the agent is running */
	boolean running = true;
 	
 	public JPanel getPanel() {
 		return null;
 	}
 	
 	private ArrayList<Aagent> _agents = new ArrayList<Aagent>();
 	private static int MAX_GAME_FRAMES = 500;
 	private static int games = 0, wins = 0;
 	private static int stat_hp_total = 0, stat_hp_player = 0;
 	private static int stat_units_total = 0, stat_units_player = 0;
 	
 	private Qlearner _ql = new Qlearner("db/MarineDB5.txt", new Parameters());
 
 	
 	/**
 	 * Starts the bot.
 	 * 
 	 * The bot is now the owner of the current thread.
 	 */
 	public void start(Game game) {
 		int round = 0;
 		synchronized (game) {
 			try {
 				while (game.getGameFrame() < 5)
 					game.wait();
 			} catch (InterruptedException e2) {
 				e2.printStackTrace();
 			}
 		}
 		synchronized (game) {
 			for (PlayerUnitWME unit : game.getPlayerUnits()) {
 				_agents.add(new MarineAgent(game,_ql,unit.getID()));
 			}
 		}
 		
 		System.out.println("XXX Loaded " + _agents.size() + " Agents playing vs " + game.getEnemyUnits().size() + " enemies.");
 		boolean fail = false;
		while (running) {
 			synchronized (game) {
 				try {
 					game.wait();
 				} catch (InterruptedException e2) {
 					e2.printStackTrace();
 					break;
 				}
 				
 				
 				//System.out.println("XXX Starting round " + round);
 				Iterator<Aagent> iti = _agents.iterator();
 				while (iti.hasNext()) {
 					Aagent agent = iti.next();
 					//System.out.println("XXX Controling agent " + agent.getID());
 					agent.turn();
 					if(agent.isDead()){
 						iti.remove();
 					}
 				}
 				round++;
 //				System.out.println(game.getCommandQueue().size());
 				if(round>MAX_GAME_FRAMES){
 					fail = true;
 					game.getCommandQueue().leaveGame();
 					
 				}
 					
 			}
 		}
		if(fail)
 			return;
 		StateFull finalState = new StateFull(game);
 		games++;
 		if (game.getEnemyUnits().size() == 0)
 			wins++;
 		int playerHp = finalState.playerTotalHP();
 		int enemyHp = finalState.enemyTotalHP();
 		int totalHp = playerHp + enemyHp;
 		int playerUnits = finalState.playerUnitCount();
 		int enemyUnits = finalState.enemyUnitCount();
 		int totalUnits = playerUnits + enemyUnits;
 		stat_hp_player += playerHp;
 		stat_hp_total += totalHp;
 		stat_units_player += playerUnits;
 		stat_units_total += totalUnits;
 		System.out.println("This Game Rounds: " + round + ", hp: " + playerHp + "/" + enemyHp + ", units: " + playerUnits + "/" + enemyUnits);
 		System.out.println("Overall Games: " + games + " | Ratios: Wins: " + (wins/(double)games) + ", hp: " + (stat_hp_player/(double)stat_hp_total) + " units: " + (stat_units_player/(double)stat_units_total));
 		StateUnitMin.EOG();
		_ql.persist();
 	}

 	/**
 	 * Tell the main thread to quit.
 	 */
 	public void stop() {
		running = false;
 	}
 }
