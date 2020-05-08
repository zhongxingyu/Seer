 package gunslinger.g3_v3;
 import java.util.*;
 import java.io.*;
 
 public class Player extends gunslinger.sim.Player
 {
     private int nplayers, nfriends, nenemies, nneutrals;
     private int[] friends, enemies, neutrals, allegiance;
 	private int roundNum = -1, oldRounds = 3;
     private int[][] history, shooter;
 
 	boolean createLog;
 	private PrintWriter outfile;
 
     private static int versions = 0;
     private int version = versions++;
    public String name() { return "g3" + (versions > 1 ? " v" + version : ""); }
 
     public void init(int nplayers, int[] friends, int enemies[])
     {
 		createLog = true;
 
 		this.nplayers = nplayers;
         this.friends = friends.clone();
         this.enemies = enemies.clone();
 		nfriends = friends.length;
 		nenemies = enemies.length;
 		nneutrals = nplayers - nfriends - nenemies;
 
 		allegiance = new int[nplayers];
 		for(int i = 0; i < nfriends; i++)
 			allegiance[friends[i]] = 1;
 		for(int i = 0; i < nenemies; i++)
 			allegiance[enemies[i]] = -1;
 		allegiance[id] = 2;
 
 		history = new int[100][nplayers];
 		for (int i = 0; i < history.length; i++)
 			for (int j = 0; j < nplayers; j++)
 				history[i][j] = -1;
 
 		shooter = new int[oldRounds][nplayers];
 
 		if(createLog) try {
 			FileWriter fstream = new FileWriter("gunslinger/g3/log.txt", true);
 			outfile = new PrintWriter(fstream);
 			//outfile.println("Init:");
 			//outfile.println(Integer.toString(id));
 			outfile.println("\n" + Arrays.toString(allegiance));
 			outfile.flush();
 		} catch (Exception e){ }
 	}
 
 	private final double ACTION_THRESHOLD      = +0.7;
 	private final double ELIMINATION_THRESHOLD = +1.4;
 
     public int shoot(int[] prevRound, boolean[] alive)
     {
 		roundNum++;
 
 		if(createLog) try {
 			//outfile.println("\nRound " + Integer.toString(roundNum) + ":");
 			outfile.println(Arrays.toString(prevRound));
 			//outfile.println(Arrays.toString(alive));
 			outfile.flush();
 		} catch (Exception e){ }
 
 		if(prevRound == null)
 			return -1;
 
 		updateHistory(prevRound);
 		double[] expectedShots = getExpectedShots(alive);
 
 		if(createLog) try {
 			//outfile.println(Arrays.toString(shooter[0]));
 			outfile.println(Arrays.toString(expectedShots));
 			//outfile.flush();
 		} catch (Exception e){ }
 
 		if(shooter[0][id] != -1 && alive[shooter[0][id]] && expectedShots[id] < ELIMINATION_THRESHOLD && allegiance[shooter[0][id]] != 1)
 			return shooter[0][id];
 
 		int friendId = -1;
 		for(int k = 0, i = -1; k < nfriends; k++)
 		{
 			i = friends[k];
 			if(alive[i] && shooter[0][i] != -1 && alive[shooter[0][i]] && expectedShots[shooter[0][i]] >= ACTION_THRESHOLD && expectedShots[i] < ELIMINATION_THRESHOLD)
 			{
 				if(friendId == -1)
 					friendId = i;
 				else if(expectedShots[shooter[0][i]] < ELIMINATION_THRESHOLD)
 				{
 					if(shooter[0][shooter[0][i]] == id)
 						return shooter[0][i];
 					else if(allegiance[shooter[0][i]] < allegiance[shooter[0][friendId]])
 						friendId = i;
 					else if(allegiance[shooter[0][i]] == allegiance[shooter[0][friendId]] && expectedShots[shooter[0][i]] > expectedShots[shooter[0][friendId]])
 						friendId = i;
 				}
 			}
 		}
 		if(friendId != -1)
 			return shooter[0][friendId];
 
 		int enemyId = -1;
 		for(int k = 0, i = -1; k < nenemies; k++)
 		{
 			i = enemies[k];
 			if(alive[i] && expectedShots[i] >= ACTION_THRESHOLD)
 			{
 				if(enemyId == -1)
 					enemyId = i;
 				else if(expectedShots[i] < ELIMINATION_THRESHOLD)
 				{
 					if(shooter[0][enemyId] == -1)
 					{
 						if(shooter[0][i] != -1 || expectedShots[i] > expectedShots[enemyId])
 							enemyId = i;
 					}
 					else if(shooter[0][i] != -1)
 					{
 						if(allegiance[shooter[0][i]] > allegiance[shooter[0][enemyId]])
 							enemyId = i;
 						else if(allegiance[shooter[0][i]] == allegiance[shooter[0][enemyId]] && expectedShots[i] > expectedShots[enemyId])
 							enemyId = i;
 					}
 				}
 			}
 		}
 		if(enemyId != -1)
 			return enemyId;
 
 		return -1;
 	}
 
 	private void updateHistory(int[] prevRound)
 	{
 		int prevRoundNum = roundNum - 1;
 		for (int i = 0; i < nplayers; i++)
 			history[prevRoundNum][i] = prevRound[i];
 		if(roundNum == 100)
 		{
 			for(int r = 50; r < 100; r++)
 				for (int i = 0; i < nplayers; i++)
 					history[r - 50][i] = history[r][i];
 			roundNum = roundNum - 50;
 		}
 	}
 
 	private final double REPEAT_FACTOR       = +1.0;
 	private final double DEFENSE_FACTOR      = -0.3;
 	private final double RETALIATION_FACTOR  = +0.7;
 	private final double INERTIA_FACTOR      = -0.3;
 	private double[] getExpectedShots(boolean[] alive)
 	{
 		double[] expectedShots = new double[nplayers];
 
 		for(int r = 0; r < oldRounds; r++)
 			for(int i = 0; i < nplayers; i++)
 					shooter[r][i] = -1;
 
 		for(int r = 0; r < oldRounds; r++)
 			for(int i = 0; i < nplayers; i++)
 					if((roundNum - r) > 0 && history[roundNum -r -1][i] != -1 && alive[history[roundNum -r -1][i]])
 						shooter[r][history[roundNum -r -1][i]] = i;
 
 		int playersShot = 0, playersAlive = 0;
 		for(int i = 0; i < nplayers; i++)
 			if(alive[i])
 			{
 				playersAlive++;
 				if(shooter[0][i] != -1)
 					playersShot++;
 			}
 
 		for(int i = 0; i < nplayers; i++)
 			if(alive[i])
 			{
 				if(shooter[0][i] != -1 && shooter[0][i] != id)
 				{
 					expectedShots[i] = expectedShots[i] + REPEAT_FACTOR;
 					if(shooter[0][shooter[0][i]] != -1 && shooter[0][shooter[0][i]] != i)
 						expectedShots[i] = expectedShots[i] + DEFENSE_FACTOR;
 				}
 				if(history[roundNum -1][i] != -1 && history[roundNum -1][i] != id && alive[history[roundNum -1][i]] && history[roundNum -1][i] != shooter[0][i])
 				{
 					expectedShots[i] = expectedShots[i] + RETALIATION_FACTOR;
 					int target = history[roundNum -1][i];
 					if(history[roundNum -1][target] != -1 && alive[history[roundNum -1][target]])
 						expectedShots[i] = expectedShots[i] + INERTIA_FACTOR;
 				}
 			}
 		return expectedShots;
 	}
 }
