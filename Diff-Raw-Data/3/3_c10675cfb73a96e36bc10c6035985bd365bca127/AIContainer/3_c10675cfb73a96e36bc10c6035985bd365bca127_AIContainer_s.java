 package de.cloudarts.aibattleai;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class AIContainer {
 	
 	private String _playerName = "Pitbull";
 	private String _opponentName = "opponent";
 	private int _playerNumber = 1;	// is the AI player1 or player 2?
 	private boolean _isStartPlayer = false;	// has the AI made the first move?
 	private int _matchID = 0;
 	private String _token = "";
 	
 	//which AI to use
 	private IAIProfile _profile = null;
 	
 	public AIContainer(IAIProfile profile_)
 	{
             _profile = profile_;
             
             if( _profile == null )
             {
             	System.err.println("Error! No AI profile passed!");
             }
             
             _playerName = _profile.getProfileName();
 	}
 	
 	public void start()
 	{		
 		if( !requestNewMatch() )
 		{
 			return;
 		}
 		
 		String gameStatusFromLastLoop = "";
 		
 		// actual game loop
 		while(true)
 		{
 			//get game status
 			String lastGameStatusAnswerString = requestMatchStatus();
 			if( lastGameStatusAnswerString.equals(gameStatusFromLastLoop))
 			{
 				System.out.print(".");
 				continue;
 			}
 			
 			gameStatusFromLastLoop = lastGameStatusAnswerString;
 			
 			// get game status
             int[] grid = createGrid(lastGameStatusAnswerString);
             
             AITools.visualizeGrid(grid);
 			
 			if( isErrorAnswer(lastGameStatusAnswerString) )
 			{
 				return;
 			}
 			
 			if( isDrawGame(lastGameStatusAnswerString) )
 			{
 				return;
 			}
 			
 			int winNumber = isGameWon(lastGameStatusAnswerString);
 			if( winNumber != 0 )
 			{
 				if( winNumber == _playerNumber )
 				{
 					System.out.println("I've won!");
 				}
 				else
 				{
 					System.out.println(_opponentName + " has won...");
 				}
 				return;
 			}
 			
 			if( !isItMyTurn(lastGameStatusAnswerString) )
 			{
 				//wait a sec, then try again
 				try 
 				{
 					Thread.sleep(AITools.SLEEP_MILLIS);
 				} 
 				catch (InterruptedException e) 
 				{
 					e.printStackTrace();
 				}
 				continue;
 			}
 			
 			//so it's my turn
 			String action = _profile.getNextAction(grid, _playerNumber);
 			
 			// send next action
 			System.out.println("sending action: " + action);
 			postAction(action);
 			
 			// continue in loop
 		}
 	}
         
         private int[] createGrid(String answerString_)
         {
             int[] grid = new int[AITools.GRID_ROWS*AITools.GRID_COLUMNS];
             
             //initialize the grid with zeros
             for( int i = 0; i < grid.length; i++ )
             {
             	grid[i] = 0;
             }
             
             String[] items = answerString_.split(";");
             
             // extract opponent name
             String playerNames = items[1];
             int versusIndex = playerNames.indexOf(" vs ");
             
             if( versusIndex != -1 )
             {
             	String player1Name = playerNames.substring(0, versusIndex);
             	String player2Name = playerNames.substring(versusIndex + 4);
             	if( player1Name.equals(_playerName) )
             	{
             		_playerNumber = 1;
             		_opponentName = player2Name;
             	}
             	else
             	{
             		_playerNumber = 2;
             		_opponentName = player1Name;
             	}
             }
             
             //first item is match status, second item is player names
             //parse only actions, so start with index 2
             ArrayList<String> actions = new ArrayList<>();
             
             if( actions.size() > 2 )
             {
             	//if no actions have been made yet but it's my turn -> I am the first player to make a move
             	_isStartPlayer = true;
             }
             
             for( int i = 2; i < items.length; i++ )
             {
                 String item = items[i];
                 String[] subitems = item.split(":");
                 String player = subitems[0];
                 String action = subitems[1];    //first subitem is player number
                 actions.add(action);
                 
                 if( i == 2 && player == String.valueOf(_playerNumber) )
                 {
                 	_isStartPlayer = true;
                 }
             }           
             
             //fill gridArray with actions
             //start with correct player number
             int playerNumber = 1;
             if( _isStartPlayer )
             {
             	playerNumber = _playerNumber;
             }
             else
             {
             	if( _playerNumber == 1)
             	{
             		playerNumber = 2;
             	}
             	else
             	{
             		playerNumber = 1;
             	}
             }
             
             for( int i = 0; i < actions.size(); i++ )
             {
                 int col = AITools.actionToColumn(actions.get(i));
                 int row = AITools.GRID_ROWS-1;      //start at the bottom
                 //work your way up until you hit an empty space
                 int gridIndex = AITools.coordsToIndex(col, row);
                 
                 try
                 {
                     while( grid[gridIndex] != 0 )
                     {
                         row--;
                         gridIndex = AITools.coordsToIndex(col, row);
                     }
                 }
                 catch(ArrayIndexOutOfBoundsException ex)
                 {
                     System.err.println("Impossible action found: " + ex);
                     return null;
                 }
                 
                 grid[gridIndex] = playerNumber;
                 playerNumber++;
                 if( playerNumber > 2 )
                 {
                     playerNumber = 1;
                 }
             }
             
             return grid;
         }
         
         
 
 	private Boolean requestNewMatch()
 	{
             URL u = URLCreator.getRequestMatchURL(_playerName);
             if (u == null )
             {
                     System.err.println("Error! could not create request url!");
                     return false;
             }
 		
 	    String r = "";
 	    
             try 
             {
                     r = new Scanner( u.openStream() ).useDelimiter( "\\Z" ).next();
             } 
             catch (IOException e) 
             {
                     e.printStackTrace();
                     return false;
             }
 		
 	    System.out.println( "requestNewMatch: received answer:" + r );
 	    
 	    if( isErrorAnswer(r) )
 	    {
 	    	return false;
 	    }
 	    
 	    // split answer string in matchID and secret token
 	    String[] temp = r.split(";");
 	    
 	    if( temp.length != 2 )
 	    {
 	    	System.err.println("requestNewMatch: did expect 2 answer items, but received " + temp.length);
 	    	return false;
 	    }
 	    
 	    _matchID = Integer.valueOf(temp[0]);
 	    _token = temp[1];
 	    
 	    System.out.println("requestNewMatch: received new matchID " + _matchID + " and token " + _token);
 	    
 	    return true;
 	}
 	
 	private String requestMatchStatus()
 	{
             URL u = URLCreator.getRequestMatchStatusURL(_matchID);
             if( u == null )
             {
                     return "error";
             }
 
             String r = "";
 
             try 
             {
                     r = new Scanner( u.openStream() ).useDelimiter( "\\Z" ).next();
             } 
             catch (IOException e) 
             {
                     e.printStackTrace();
                     return "error";
             }
 		
 	    //System.out.println( "requestMatchStatus: received answer:" + r );
 	    
 	    return r;		
 	}
 
 	
 	private String postAction(String action_)
 	{
             URL u = URLCreator.getPostActionURL(_matchID, _playerName, _token, action_);
             if( u == null )
             {
                     return "error";
             }
 
             String r = "";
 
             try 
             {
                     r = new Scanner( u.openStream() ).useDelimiter( "\\Z" ).next();
             } 
             catch (IOException e) 
             {
                     e.printStackTrace();
                     return "error";
             }
 		
 	    System.out.println( "postAction: received answer:" + r );
 		return r;
 	}
 	
 	private Boolean isItMyTurn(String rawStatusString_)
 	{
 		String currentTurnPlayer = rawStatusString_.split(";")[0];
 		if( currentTurnPlayer.equals(_playerName) )
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private Boolean isErrorAnswer(String answer_)
 	{
 		if( answer_.substring(0, "error".length()).equals("error") )
 		{
 			return true;
 		}
 		
 		return false;
 	}
 
 	private boolean isDrawGame(String answer_) {
 		if( answer_.substring(0, "draw game".length()).equals("draw game") )
 		{
 			return true;
 		}
 		
 		return false;
 	}
 
 	private int isGameWon(String answer_) {
             String[] items = answer_.split(";");
             String status = items[0];
             
             String last3Letters = status.substring(status.length()-3, status.length());
             
             if( last3Letters.equals("won") )
             {
                 //System.out.println(status);
                 String winPlayerName = status.substring("player ".length(), status.length()-4);
                 
                 //return correct player number
                 if( winPlayerName.equals(_playerName) )
                 {
                 	return _playerNumber;
                 }
                 else		//opponent has won, so, which number does he have?
                 {
                 	if( _playerNumber == 1 )
                 	{
                 		return 2;
                 	}
                 	else
                 	{
                 		return 1;
                 	}
                 }
             }
 		
             return 0;
 	}
 
     
 }
