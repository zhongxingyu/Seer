 package edu.asu.tankgame;
 
 import android.util.Log;
 
 public class GameManager {
 
 	private static GameManager INSTANCE;
 	
 	private boolean [][] level;
 	private float[] playerHealth;
 	private int[] playerPosition;
 	private float[] playerAngle;		// 0 is left pointing level, 180 is right pointing level
 	private float[] playerPower;
 	private String[] playerName;
 	private int[] playerScore;
 	private int currentPlayer;
 
 	public static final int maxPlayers = 2;	
 	public static final float initialPlayerHealth = 100;
 	
 	GameManager()
 	{
 		playerHealth = new float[maxPlayers];
 		playerPosition = new int[maxPlayers];
 		playerAngle = new float[maxPlayers];
 		playerPower = new float[maxPlayers];
 		playerName = new String[maxPlayers];
 		playerScore = new int[maxPlayers];
 		
 		currentPlayer = 0;
 		
 		for(int i = 0; i < maxPlayers; i++)
 		{
 			playerHealth[i] = initialPlayerHealth;
 			playerPower[i] = 75;
 			if(i%2 == 0)
 				playerAngle[i] = 30;
 			else
 				playerAngle[i] = 120;
 		}
 	}
 	
 	public void setPlayerName(int player, String name)
 	{
 		if(player > 0 && player <= maxPlayers)
 			playerName[player - 1] = name;	
 	}
 	
 	public String getPlayerName(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return this.playerName[player - 1];
 		else
 			return null;
 	}
 
 	public int getPlayerScore(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return playerScore[player - 1];	
 		else
 			return -1;
 	}
 	
 	private void incrementPlayerScore(int player, int score)
 	{
 		if(player > 0 && player <= maxPlayers)
 			playerScore[player -1] += score;
 	}
 
 	
 	public static GameManager getInstance(){
 		if(INSTANCE == null){
 			INSTANCE = new GameManager();
 		}
 		return INSTANCE;
 	}
 	
 	public boolean[][] generateLevel(int height, int width)
 	{
 		level = new boolean [(int) Math.ceil(width/48.0)][(int) Math.ceil(height/48.0)];
 		
 		// TODO Simple code for randomly generating level maybe change to a polynomial to create level.		
 //		Boolean allSet = false;
 //		while(allSet == false)
 //		{
 //			for(int i = 0; i < maxPlayers; i++)
 //				playerPosition[i] = (int) (Math.random() * level.length);
 //		}
 		playerPosition[0] = 2;
 		playerPosition[1] = level.length - 3;
 		
 		int limit;
 		for(int i = 0; i < level.length; i++)
 			for(int j = 0; j < level[i].length; j++)
 				level[i][j] = false;
 		
 		for(int i = 0; i < level.length; i++)
 		{
 //			limit = (int) (Math.random() * (level[i].length - 2));
 			if(i != 2 && i != (level.length -3))
 				limit = 3;
 			else
 				limit = 7;
 			if(limit < 2)
 				limit = 2;
 			for(int j = 0;j <= limit; j++)
 			{
 				level[i][j] = true;
 			}
 		}
 
 		return this.level;
 	}
 	
 	public boolean[][] getLevel()
 	{
 		return level;
 	}
 	
 	public float getPlayerHealth(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return this.playerHealth[player - 1];
 		else
 			return 0;
 	}
 
 	public float getPlayerHealth( )
 	{
 		return this.playerHealth[currentPlayer];
 	}
 
 	
 	public int getPlayerPosition(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return this.playerPosition[player - 1];
 		else
 			return 0;
 	}
 	
 	public int getPlayerPosition( )
 	{
 		return this.playerPosition[currentPlayer];
 	}
 
 	
 	public void damagePlayer(int player, float damage, int dealtBy)
 	{
 		if(player > 0 && player <= maxPlayers)
 		{
 			if(playerHealth[player - 1] > 0)
 			{
 				playerHealth[player - 1] = playerHealth[player - 1] - damage;
 				incrementPlayerScore(dealtBy, (int)damage*10);
 			}
 		}
 	}
 	
 	public void damageByImpact(int player, float x, float y)
 	{
 		double damage = Math.sqrt((x*x)+(y*y));
 		if(damage > 1)
 		{
 			Log.w("Damage player " + player , "impact x: " + x  + " impact y: " + y);
 			damagePlayer(player, (float) damage , (player%2)+1);
 		}
 	}
 	
 	public float getPlayerAngle(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return this.playerAngle[player - 1];
 		else
 			return -1;		
 	}
 	
 	public float getPlayerAngle( )
 	{
 		return this.playerAngle[currentPlayer];
 	}
 
 	
 	public boolean changePlayerAngle(int player, float angle)
 	{
		angle = -angle; // Mike's tweak (1/2) to fix inverted angle bar
 		if(player > 0 && player <= maxPlayers)
 		{
 			float originalPower = this.playerAngle[player -1];
 			this.playerAngle[player - 1] = this.playerAngle[player - 1] + angle;
 			if(this.playerAngle[player - 1] < 0)
 				this.playerAngle[player - 1] = 0;
 			else if(this.playerAngle[player - 1] > 180)
 				this.playerAngle[player - 1] = 180;

			//Log.w("changePlayerAngle","P" + player + " angle now " + this.playerAngle[player - 1] + ", delta " + angle);
 			// This checks to see if there was a transition from left facing to right or vice versa.
 			if((originalPower <= 90) ^ (this.playerAngle[player - 1]  <= 90))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean changePlayerAngle(float angle)
 	{
 		float originalPower = this.playerAngle[currentPlayer];
 		this.playerAngle[currentPlayer] = this.playerAngle[currentPlayer] + angle;
 		if(this.playerAngle[currentPlayer] < 0)
 			this.playerAngle[currentPlayer] = 0;
 		else if(this.playerAngle[currentPlayer] > 180)
 			this.playerAngle[currentPlayer] = 180;
 		// This checks to see if there was a transition from left facing to right or vice versa.
 		if((originalPower <= 90) ^ (this.playerAngle[currentPlayer]  <= 90))
 			return true;
 		else
 			return false;
 	}
 
 	
 	public float getPlayerPower(int player)
 	{
 		if(player > 0 && player <= maxPlayers)
 			return this.playerPower[player - 1];
 		else
 			return -1;		
 	}
 	public float getPlayerPower( )
 	{
 		return this.playerPower[currentPlayer];
 	}
 	
 	public void changePlayerPower(int player, float power)
 	{
 		if(player > 0 && player <= maxPlayers)
 		{
 			this.playerPower[player - 1] = this.playerPower[player - 1] + power;
 			if(this.playerPower[player - 1] < 0)
 				this.playerPower[player - 1] = 0;
 			else if(this.playerPower[player - 1] > 100)
 				this.playerPower[player - 1] = 100;
 		}
 	}
 	
 	public void changePlayerPower(float power)
 	{
 			this.playerPower[currentPlayer] = this.playerPower[currentPlayer] + power;
 			if(this.playerPower[currentPlayer] < 0)
 				this.playerPower[currentPlayer] = 0;
 			else if(this.playerPower[currentPlayer] > 100)
 				this.playerPower[currentPlayer] = 100;
 	}
 
 
 	public void togglePlayer()
 	{
 		currentPlayer++;
 		currentPlayer%=maxPlayers;
 	}
 	
 	public int getCurrentPlayer()
 	{
 		return currentPlayer + 1;
 	}
 }
