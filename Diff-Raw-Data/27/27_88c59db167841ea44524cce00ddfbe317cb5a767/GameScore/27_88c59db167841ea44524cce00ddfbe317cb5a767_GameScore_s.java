 package game;
 
 import java.util.Observable;
 import events.ScoreEvent;
 import events.ScoreObserver;
 
 
 public class GameScore extends Observable{
 
 	//game events registered here
 	public static final int ORGAN_HIT = 0;
 	public static final int FAT_BUST = 1;
 	public static final int RED_EDGE = 2;
 	public static final int VIRUS_HIT = 3;
 	public static final int VIRUS_CLEAR = 4;
 	public static final int WHITE_TRAP = 5;
 	public static final int PLATELET_SHRINK = 6;
 	public static final int RED_BOUNDS = 7;
 	public static final int FAT_GROW = 8;
 	public static final int FAT_SHRINK = 9;
 	public static final int PLATELET_BOUNDS = 10;
 	public static final int FAT_BUST_RICH = 11;
 	
	private final int NO_POS_EVENTS = 11	;
 	
 	private final int MAX_EVENTS = 14;
 	private int curIndex = 0;
 	private int readScore = curIndex;
 	private ScoreEvent scoreStack[] = new ScoreEvent[MAX_EVENTS];
 	private int count[] = new int[NO_POS_EVENTS];
 	private final long MAX_TIME_FRAME = 3200; //IN ms
 	
 	
 	private int tempOrgansCount =0;
 	private int tempFatCount = 0;
 	private int tempVirusCount =0;
 	private ScoreObserver anObserver = null;
 	
 	private GameModel model;
 	
 	//variables to manage the score change
 	float score;
 	float scoreVel;
 	//what happens if we pause game and resume?
 	long runTime = 0;
 	
 	final float SCORE_VEL_DEFAULT =  0.07f; //points/ms
 	final int MAX_FIB_SET = 14; //ALOT OF VIRUSES
 	int fibNumbs[] = new int[MAX_FIB_SET];
 	
 	public GameScore(GameModel _model)
 	{
 		model = _model;
 		
 		Init();
 	}
 	
 	private void Init()
 	{
 		//collect events here for counting
 		for(int i =0; i< MAX_EVENTS; ++i)
 		{
 			scoreStack[i] = new ScoreEvent();
 			//set time to something easy to find if this event is active yet
 			scoreStack[i].setTimeStamp(-this.MAX_TIME_FRAME);
 		}
 		//array to store count for comparison
 		for(int i = 0; i< NO_POS_EVENTS; ++i)
 		{
 			count[i] = 0;
 		}
 		scoreVel = SCORE_VEL_DEFAULT;
 		composeFib();
 		resetCount();
 	}
 	
 	public void update(long dt){
 		runTime += dt;
 		score += (scoreVel * dt);
 	}
 	
 	public void calScore( long timeStep )
 	{
 		//infrequent check on score. check status of game
 		//increase or decrease increment for score(Never zero || < 0)
 		//check is on game objects and delta of game objects
 
 		if(model.getVirusCount() == 0 && tempVirusCount > 0)
 			this.regEvent(GameScore.VIRUS_CLEAR);
 		
 		
 		long hisTime = scoreStack[readScore].getTimeStamp();
 		while( (runTime - hisTime) < MAX_TIME_FRAME )
 		{
 			if(readScore == curIndex)
 				break;
 			//this loops is crashing sometimes!
 			hisTime = scoreStack[readScore].getTimeStamp();
 			count[scoreStack[readScore].getEvent()] += 1;
 			if(--readScore < 0)
 				readScore = MAX_EVENTS - 1;
 			
 		}
 		
 		if(count[GameScore.VIRUS_CLEAR] > 0)
 			scoreVel += (count[GameScore.VIRUS_CLEAR] * 0.4);
 		else
 			{
 				scoreVel *= ( 0.4 * colFib(tempVirusCount));
 			}
 		if(count[GameScore.VIRUS_HIT] > 0)
 			{
 				scoreVel += (count[GameScore.VIRUS_HIT] * 0.4);
 			}
 		if(count[GameScore.ORGAN_HIT] > 0)
 			{
 				scoreVel += (count[GameScore.ORGAN_HIT] * 0.4);
 			}
 		if(count[GameScore.RED_BOUNDS] > 0)
 		{
 			scoreVel *= 0.7;
 		}
 		if(count[GameScore.FAT_BUST] > 0)
 		{
 			if(count[GameScore.FAT_BUST] > 6)
 				scoreVel += (count[GameScore.VIRUS_HIT] * 1.4);
 			else
 				scoreVel += (count[GameScore.VIRUS_HIT] * 0.4);
 		}
 		if(count[GameScore.FAT_BUST_RICH] > 0)
 			scoreVel += (count[GameScore.VIRUS_HIT] * 0.7);
 		tempVirusCount = model.getVirusCount();
 
 		resetCount();
 		
 		this.notifyObservers();
 	}
 	
 	public void regEvent( int eventType )
 	{
 		scoreStack[curIndex].setTimeStamp(runTime);
 		scoreStack[curIndex].setEvent(eventType);
 		if(++curIndex >= MAX_EVENTS)
 			curIndex = 0;
 	}
 	
 	private void resetCount()
 	{
 		readScore = curIndex;
 		if(--readScore < 0)
 			readScore = MAX_EVENTS - 1;
 		for( int i= 0; i< NO_POS_EVENTS; ++i )
 			count[i] = 0;
 	}
 	
 	public void notifyObservers()
 	{
 		if(anObserver != null)
 			anObserver.UpdateScore(this.score, this.scoreVel);
 	}
 	
 	public void addObserver( ScoreObserver newObserver)
 	{
 		//currently just one, but expecting at least one more
 		anObserver = newObserver;
 	}
 	
 	private int colFib(int fibSet)
 	{
 		int i = 0;
 		while(fibNumbs[i] < fibSet)
 		{
 			++i;
 			if(i >= MAX_FIB_SET)
 				break;
 		}
 		return i;
 	}
 	
 	private void composeFib()
 	{
 		int f0 = 0;
 		int f1 = 1;
 
 		for (int i = 0; i < MAX_FIB_SET; ++i) {
 
 		fibNumbs[i] = f1;	
 			
 		final int temp = f1;
 		f1 += f0;
 		f0 = temp;
 		}
 		
 		fibNumbs[10] = 610;//55 is a lot of viruses
 
 	}
 }
