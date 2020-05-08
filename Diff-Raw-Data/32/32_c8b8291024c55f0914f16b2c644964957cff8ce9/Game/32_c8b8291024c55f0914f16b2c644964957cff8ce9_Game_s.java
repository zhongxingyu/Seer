 package pacmon.model;
 
 import java.util.LinkedList;
 
 import pacmon.GraphicsManager;
 import pacmon.model.level.Level;
 import pacmon.model.level.LevelMode;
 import pacmon.model.level.LevelState;
 import pacmon.model.maze.MazeItem;
 import pacmon.model.maze.MazeManager;
 import pacmon.sound.SoundLoader;
 
 public class Game 
 {
 
 	private static final int LEVEL_START_DELEY = 300;
 	private static final int LEVEL_COMPLETE_DELEY = 100;
 	private static final int PAC_MAN_DEATH_DELEY = 150;
 	
	private static final int PAC_MAN_LIVES = 0;
 	
 	public static Game getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new Game();
 		}
 		return instance;
 	}
 	
 	private static Game instance;
 	
 	public GameState getState()
 	{
 		return state;
 	}
 	
 	public void setState(GameState state)
 	{	
 		this.state = state;
 	}
 	
 	public void reset()
 	{
 		instance = new Game();
 		System.out.println("game reset");
 	}
 	
 	public void clear()
 	{
 		instance = null;
 		System.out.println("game cleared");
 	}
 	
 	public Level getLevel()
 	{
 		return level;
 	}
 	
 	private Game()
 	{
 		GameState state = new GameState(1, 0, PAC_MAN_LIVES);	
 		setState(state);
 		
 		paused = false;
 		level = new Level();
 		level.setState(levelStates[0]);
 		gameOver = false;
 		
 		levelStartCountdown = LEVEL_START_DELEY;
 		
 		levelCompleteCountdown = 0;
 		pacMonDeathCountdown = 0;
 		
 		started = false;
 		
 		soundEventQueue = new LinkedList<GameSoundEvent>();
 	}
 	
 	public GameSoundEvent pollSoundEvent() 
 	{
 		return this.soundEventQueue.poll();
 	}
 	
 	public boolean hasSoundEvent() {
 		return !this.soundEventQueue.isEmpty();
 	}
 	
 	public void addSoundEvent(int type) {
 		this.addSoundEvent(type, null);
 	}
 	
 	public void addSoundEvent(int type, String name) {
 		this.addSoundEvent(type, name, false);
 	}
 	
 	public void addSoundEvent(int type, String name, boolean loop) {
 		this.soundEventQueue.add(new GameSoundEvent(type, name, loop));
 	}
 	
 	// Game control
 	public void controlUp()
 	{
 		if (!this.paused)
 			level.controlUp();
 	}
 	
 	public void controlLeft()
 	{
 		if (!this.paused)
 			level.controlLeft();
 	}
 	
 	public void controlDown()
 	{
 		if (!this.paused)
 			level.controlDown();
 	}
 	
 	public void controlRight()
 	{
 		if (!this.paused)
 			level.controlRight();
 	}
 	
 	public void controlPause()
 	{
 		paused = !paused;
 		
 		if (paused)
 		{
 			addSoundEvent(GameSoundEvent.TYPE_PAUSE);
 			//SoundLoader.getInstance().setPaused(true);
 		}
 		else 
 		{
 			addSoundEvent(GameSoundEvent.TYPE_UNPAUSE);
 			//SoundLoader.getInstance().setPaused(false);
 		}
 	}
 	
 	public boolean isPaused()
 	{
 		return paused;
 	}
 	
 	public boolean isGameOver()
 	{
 		return gameOver;
 	}
 	
 	public boolean isInLevelStartDelay()
 	{
 		return levelStartCountdown > 0;
 	}
 	
 	public boolean isInPacManDeath()
 	{
 		return pacMonDeathCountdown > 0;
 	}
 	
 	public void update()
 	{		
 		if (!started)
 		{
 			//SoundManager.getInstance().stopAll();
 			//SoundManager.getInstance().play(SoundManager.BEGINNING, false);
 			this.addSoundEvent(GameSoundEvent.TYPE_STOP_ALL);
 			this.addSoundEvent(GameSoundEvent.TYPE_PLAY, SoundLoader.BEGINNING);
 			
 			started = true;
 		}
 		
 		if (!paused && !gameOver)
 		{
 			if (levelStartCountdown > 0)
 			{
 				levelStartCountdown--;
 				
 				if (levelStartCountdown == 0)
 				{
 					System.out.println("--- Level "+state.getLevelNum()+" ---");
 					
 					System.out.println(level.getState().toString());
 					
 					System.out.println("---");
 				}
 			}
 			else if (levelCompleteCountdown > 0)
 			{
 				levelCompleteCountdown--;
 				
 				if (levelCompleteCountdown == 0)
 				{
 					int nextLevel = state.getLevelNum() + 1;
 					
 					LevelState currentState = level.getState();	
 					
 					LevelState nextState = null;
 					if (nextLevel >= levelStates.length)
 					{
 						nextState = levelStates[levelStates.length - 1];	
 					}
 					else
 					{
 						nextState = levelStates[nextLevel - 1];
 					}
 					
 					level = new Level();					
 					level.setState(nextState);
 					
 					state.setLevelNum(nextLevel);
 					state.addToTotalScore(currentState.getScore());
					state.setLivesLeft(3);				
 					
 					levelStartCountdown = LEVEL_START_DELEY;
 					//SoundManager.getInstance().stopAll();
 					//SoundManager.getInstance().play(SoundManager.BEGINNING, false);
 					this.addSoundEvent(GameSoundEvent.TYPE_STOP_ALL);
 					this.addSoundEvent(GameSoundEvent.TYPE_PLAY, SoundLoader.BEGINNING);
 					
 					System.out.println("Level "+state.getLevelNum());
 				}
 			}
 			else if (pacMonDeathCountdown > 0)
 			{
 				pacMonDeathCountdown--;
 				
 				if (pacMonDeathCountdown == 0)
 				{
 					LevelState currentState = level.getState();
 					currentState.setMode(LevelMode.SCATTER);
 					
 					if (state.getLivesLeft() == 0)
 					{
 						gameOver = true;
 					}
 					else
 					{
 						//level = new Level(this);
 						levelStartCountdown = LEVEL_START_DELEY;
 						//SoundManager.getInstance().stopAll();
 						//SoundManager.getInstance().play(SoundManager.BEGINNING, false);
 						this.addSoundEvent(GameSoundEvent.TYPE_STOP_ALL);
 						this.addSoundEvent(GameSoundEvent.TYPE_PLAY, SoundLoader.BEGINNING);
 						
 						state.setLivesLeft(state.getLivesLeft() - 1);
 						level.setState(currentState);
 					}
 				}
 			}
 			else
 			{
 				level.update();
 				
 				if (level.isComplete())
 				{
 					levelCompleteCountdown = LEVEL_COMPLETE_DELEY;
 					//SoundManager.getInstance().stopAll();
 					this.addSoundEvent(GameSoundEvent.TYPE_STOP_ALL);
 					
 					System.out.println("Level "+state.getLevelNum() + " complete");
 				}
 				
 				if (level.isPacMonDead())
 				{
 					pacMonDeathCountdown = PAC_MAN_DEATH_DELEY;
 
 					//SoundManager.getInstance().stopAll();
 					//SoundManager.getInstance().play(SoundManager.DEATH, false);					
 					//this.addSoundEvent(GameSoundEvent.TYPE_STOP_ALL);
 					this.addSoundEvent(GameSoundEvent.TYPE_STOP, SoundLoader.INTERMISSION);
 					this.addSoundEvent(GameSoundEvent.TYPE_PLAY, SoundLoader.DEATH);
 				}
 			}
 		}
 	}	
 	
 	private LinkedList<GameSoundEvent> soundEventQueue;
 	
 	private GameState state;
 	
 	private Level level;
 	
 	public boolean paused;
 	
 	private boolean gameOver;
 	
 	private boolean started;
 	
 	private int levelStartCountdown;
 	private int levelCompleteCountdown;
 	private int pacMonDeathCountdown;
 	
 	private LevelState[] levelStates = {
 			// 1
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_2)
 					, LevelMode.SCATTER
 					, 0.8f
 					, 0.9f
 					, 0.75f
 					, 0.5f
 					, 0.4f
 					, 0.8f
 					, 0.85f
 					, true
 					, 360
 					, 5
 					, 420
 					, 1200
 					, 420
 					, 1200
 					, 300
 					, 1200
 					, 2
 					, MazeItem.BonusWeed  // cherries
 					, 2)
 			,
 			// 2
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 0.9f
 					, 0.95f
 					, 0.85f
 					, 0.55f
 					, 0.45f
 					, 0.9f
 					, 0.95f
 					, true
 					, 360
 					, 5
 					, 420
 					, 1200
 					, 420
 					, 1200
 					, 300
 					, 61980
 					, 2
 					, MazeItem.BonusEcstacy
 					, 2)
 			,
 			// 3
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 0.9f
 					, 0.95f
 					, 0.85f
 					, 0.55f
 					, 0.45f
 					, 0.9f
 					, 0.95f
 					, true
 					, 360
 					, 5
 					, 420
 					, 1200
 					, 420
 					, 1200
 					, 300
 					, 61980
 					, 2
 					, MazeItem.BonusShroom
 					, 2)
 			,
 			// 4
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 0.9f
 					, 0.95f
 					, 0.85f
 					, 0.55f
 					, 0.45f
 					, 0.9f
 					, 0.95f
 					, true
 					, 360
 					, 5
 					, 420
 					, 1200
 					, 420
 					, 1200
 					, 300
 					, 61980
 					, 2
 					, MazeItem.BonusShroom
 					, 2)
 			,
 			// 5
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_3, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusLSD
 					, 2)
 			,
 			// 6
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusLSD
 					, 2)
 			,
 			// 7
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusCoke
 					, 2)
 			,
 			// 8
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_3, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusCoke
 					, 2)
 			,
 			// 9
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusSteroids
 					, 2)
 			,
 			// 10
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusSteroids
 					, 2)
 			,
 			// 11
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusHeroin
 					, 2)
 			,
 			// 12
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_3, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusHeroin
 					, 2)
 			,
 			// 13
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 14
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 15
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_3, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 16
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 17
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 0f
 					, 0.95f
 					, 0f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, false
 					, 0
 					, 0
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 18
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 1f
 					, 0.95f
 					, 0.6f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, true
 					, 360
 					, 5
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 19
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 0f
 					, 0.95f
 					, 0f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, false
 					, 0
 					, 0
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 20
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_1, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 1f
 					, 0f
 					, 0.95f
 					, 0f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, false
 					, 0
 					, 0
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			,
 			// 21+
 			new LevelState(MazeManager.getInstance().buildMaze(MazeManager.MAZE_2, GraphicsManager.TILESET_1)
 					, LevelMode.SCATTER
 					, 0.9f
 					, 0f
 					, 0.95f
 					, 0f
 					, 0.5f
 					, 1f
 					, 1.05f
 					, false
 					, 0
 					, 0
 					, 300
 					, 1200
 					, 300
 					, 1200
 					, 300
 					, 62220
 					, 2
 					, MazeItem.BonusMeth
 					, 2)
 			
 			
 	};
 }
