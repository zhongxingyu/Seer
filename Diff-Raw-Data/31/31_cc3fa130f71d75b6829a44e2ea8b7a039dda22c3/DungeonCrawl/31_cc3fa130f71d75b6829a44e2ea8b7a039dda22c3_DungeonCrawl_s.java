 package edu.sru.andgate.bitbot.gametypes;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.customdialogs.VictoryDialog;
 import edu.sru.andgate.bitbot.graphics.GameActivity;
 import edu.sru.andgate.bitbot.graphics.NickGameActivity;
 import edu.sru.andgate.bitbot.graphics.TileMap;
 import edu.sru.andgate.bitbot.tools.Constants;
 
 public class DungeonCrawl extends GameTypes
 {
 	private Context context;
 	private int totalBots;
 	private String userBotFile;
 	private Bot[] bots;
 	private VictoryDialog vd;
 	private long start, elapsed;
 	private Bot userBot;
 	private float defaultZ;
 	private TileMap tileMap;
 	private boolean victory;
 	Random generator;
 	public NickGameActivity _game;
 	
 	public DungeonCrawl(Context context, TileMap tileMap, String mapFile, String userBotFile)
 	{
 		this.userBotFile = userBotFile;
 		tileMap = new TileMap();
 		tileMap.loadMapFile(mapFile, context);
 		this.tileMap = tileMap;
 		this.context = context;
 		this.generator = new Random();
 		victory = false;
 		defaultZ = -5.0f;
 	}
 	
 	
 	@Override
 	public void Initialize(NickGameActivity ga)
 	{
 		this._game = ga;
 		Log.v("GameTypes", "GameType Accepted");
 		int randomIndex;
		tileMap.setSpawnPoints();
 		this.totalBots = generator.nextInt(tileMap.enemySpawnPointsX.size()-1) + 1;
 		
 		if(totalBots >= tileMap.enemySpawnPointsX.size())
 			totalBots = tileMap.enemySpawnPointsX.size();
 		
 		bots = new Bot[totalBots];
 			
 		//create number of bots for game from enemy bot file
 		for(int i = 0; i < totalBots; i++){
 			bots[i] = Bot.CreateBotFromXML(context, "enemy_bot.xml");
 			randomIndex = generator.nextInt(tileMap.enemySpawnPointsX.size());
 			bots[i].getDrawableBot().setTranslation(tileMap.enemySpawnPointsX.get(randomIndex), tileMap.enemySpawnPointsY.get(randomIndex), defaultZ);  
 			tileMap.enemySpawnPointsX.remove(randomIndex);
 			tileMap.enemySpawnPointsY.remove(randomIndex);
 		}
 		
 		/*
 		 * create users bot
 		 * set users bot spawn point
 		 */
 		userBot = Bot.CreateBotFromXML(context, userBotFile);
 		randomIndex = generator.nextInt(tileMap.userSpawnPointsX.size());
 		userBot.getDrawableBot().setTranslation(tileMap.userSpawnPointsX.get(randomIndex), tileMap.userSpawnPointsY.get(randomIndex), defaultZ);
 		start = System.currentTimeMillis();
 
 	}
 
 	@Override
 	public void Update() {
 		int botsLeft = bots.length;
 		//check if victory conditions have been met, etc
 		for(int i = 0; i < bots.length;i++){
 			if(!bots[i].getDrawableBot().isAlive()){
 				botsLeft--;
 			}
 		}
 		
 		if(botsLeft == 0 && victory == false){
 			victory = true;
 			Constants.finished_missions.add(_game.missionType);
 			elapsed = System.currentTimeMillis() - start;
 			Finalize();
 		}
 	}
 	
 
 	@Override
 	public void Finalize() {
 		// Not sure here yet
 		Log.v("GameTypes", "Victory is Yours");
 		_game.runOnUiThread(new Runnable(){
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				vd = new VictoryDialog(_game,_game, R.style.CustomDialogTheme, _game.kills, _game.accuracy, elapsed);	
 				vd.show();	
 			}
 		});	
 		
 	}
 
 	@Override
 	public TileMap getMap(){
 		return this.tileMap;
 	}
 	
 	@Override
 	public Bot[] getBots(){
 		return this.bots;
 	}
 	
 	@Override
 	public Bot getBot(){
 		return this.userBot;
 	}
 	
 	@Override
 	public boolean hasVictory(){
 		return this.victory;
 	}
 	
 }
