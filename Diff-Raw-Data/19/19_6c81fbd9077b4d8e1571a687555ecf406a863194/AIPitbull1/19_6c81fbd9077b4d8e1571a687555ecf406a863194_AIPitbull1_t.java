 package de.cloudarts.aibattleai;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 public class AIPitbull1 extends AIPitbull0 implements IAIProfile {
 	
 	private static long situations = 0;
 	private static long situationsLogCounter = 0;
 	private static int playerNumber = 0;
 	private static Situation bestSituation = null;
 
 	@Override
 	public String getProfileName() {
 		return "Pitbull_Adam";
 	}
 
 
 	@Override
 	public String getNextAction(int[] grid, int _playerNumber) {
 		
		String ret = "X";	// as fallback, make a random move
 		
 		playerNumber = _playerNumber;
 		
 		//now try to come up with something more clever
 		long startTime = new Date().getTime();
 		situations = 0;
 		IAICallback logCallBack = new IAICallback() {
 			
 			@Override
 			public void callback(String message) {
 				System.out.println(message);				
 			}
 		};
 		int opponentNumber = playerNumber -1;
 		if( opponentNumber == 0 )
 		{
 			opponentNumber = 2;
 		}
 		Situation rootSituation = new Situation(-1, opponentNumber, grid, 0, null);
 		bestSituation = rootSituation;	//may never be null!
 		rootSituation.callback = logCallBack;
 		rootSituation.computeNextSituations(7);
 		long timePassed = new Date().getTime() - startTime;
 		double seconds = (double)timePassed/1000.0;
 		double sps = (double)situations / seconds;
 		System.out.println("computed " + situations + " situation in " + timePassed + "ms: " + sps + " situations per second.");
 		
 		//found something!
		if( bestSituation._parentSituation._followingSituationsAreWins != 0 )
 		{
 			ret = getNextStepToSituation(bestSituation);
 		}
		else
		{
			System.out.println("Couldn't find any good moves... spamming random move.");
			ret = computeRandomAction();
		}
		
 		
 		return ret;
 	}
 	
 	private String getNextStepToSituation(Situation situation_) {
 		System.out.println("retrieving first step to win...");
 		int lastStepRecorded = -1;
 				
 		while( situation_._parentSituation != null )
 		{
 			lastStepRecorded = situation_._actionLedToThisSituation;
 			situation_ = situation_._parentSituation;
 			System.out.println("going one step back...");
 		}
 		
 		return AITools.columnToAction(lastStepRecorded);
 	}
 
 	/**
 	 * a situation can be given a callback objection to print every now and then something to System.out
 	 * @author wojciech
 	 *
 	 */
 	private static interface IAICallback
 	{
 		public void callback(String message);
 	}
 	
 	protected static class Situation
 	{
 		public int[] _grid;
 		
 		public int _score = 0;	// score after evaluation of this situation
 		
 		public int _actionLedToThisSituation = -1;
 		
 		public int _playerNumberMadeThisMove = 0;
 		
 		public ArrayList<Situation> _nextSituations = null;
 		
 		public IAICallback callback = null;
 		
 		//for score evaluation
 		public Situation _parentSituation = null;
 		public int _followingSituationsAreWins = 0;
 		public int _followingSituationsAreLosses = 0;
 		
 		
 		
 		/**
 		 * in the tree, which row are you in? 0 = root situation
 		 */
 		public int _order = 0;
 		
 		public Situation(int actionLedToThisSituation_, int playerNumberMadeThisMove_, int[] grid_, int order_, Situation parentSituation_)
 		{
 			_order = order_;
 			_grid = grid_;
 			_actionLedToThisSituation = actionLedToThisSituation_;
 			_playerNumberMadeThisMove = playerNumberMadeThisMove_;
 			_parentSituation = parentSituation_;
 			_score = 0;
 			AIPitbull1.situations++;
 			situationsLogCounter++;
 			
 			if( situationsLogCounter >= 1000000 )
 			{
 				situationsLogCounter = 0;
 
 				System.out.println("situations: " + situations);
 			}
 		}
 		
 		public void computeNextSituations(int maxOrder_)
 		{
 			if( _order >= maxOrder_ )
 			{
 				return;
 			}
 			
 			int nextPlayer = _playerNumberMadeThisMove == 1 ? 2 : 1;
 			
 			//check all possible actions
 			for( int col = 0; col < AITools.GRID_COLUMNS; col++ )
 			{
 				if( callback != null )
 				{
 					callback.callback("beginning col " + col);
 				}
 				
 				//next free row for this column
 				int nextFreeIndex = AITools.coordsToIndex(col, AITools.GRID_ROWS-1);
 				while( _grid[nextFreeIndex] != 0  )
 				{
 					nextFreeIndex = AITools.getGridIndexOnTopOf(nextFreeIndex);
 					if( nextFreeIndex < 0)	//can't throw in this row. no possible situation
 					{
 						break;
 					}
 				}
 				if( nextFreeIndex < 0)	//can't throw in this row. no possible situation
 				{
 					continue;
 				}
 				
 				//copy grid and apply new action
 				int[] newGrid = _grid.clone();
 				newGrid[nextFreeIndex] = nextPlayer;
 				
 				if( _nextSituations == null )
 				{
 					_nextSituations = new ArrayList<Situation>();
 				}
 				Situation newSituation = new Situation(col, nextPlayer, newGrid, _order+1, this);
 				_nextSituations.add(newSituation);
 				newSituation._score = 0;
 				
 				int gameStatus = AITools.getGameStatus(newGrid);
 				
 				//compute follow Situations only if not a an end point
 				if( gameStatus == -1)
 				{
 					newSituation.computeNextSituations(maxOrder_);
 				}		
 				else if( gameStatus == playerNumber)
 				{
					
					this._followingSituationsAreWins++;
					
 					//check: if this situation has the order 1 (next move) and is a win, then take it!
 					if( newSituation._order == 1 )
 					{
 						bestSituation = newSituation;
 						System.out.println("TAKE THE WIN: next move will finish match!");
 						AITools.visualizeGrid(newGrid);
 						
 						break;
 					}
 					
 					if( bestSituation._parentSituation == null || this._followingSituationsAreWins > bestSituation._parentSituation._followingSituationsAreWins )
 					{
 						bestSituation = newSituation;
 						System.out.println("new best Situation with " + this._followingSituationsAreWins + " win chances with one piece.");
 						AITools.visualizeGrid(newGrid);
 					}
 					
 				}
 				else if( gameStatus != 0)		//not draw game -> opponent wins
 				{
 					this._followingSituationsAreLosses++;
 					
 					//System.out.println("found situation with score: " + newSituation._score);
 					//AITools.visualizeGrid(newGrid);
 				}
 			}
 		}
 	}
 }
