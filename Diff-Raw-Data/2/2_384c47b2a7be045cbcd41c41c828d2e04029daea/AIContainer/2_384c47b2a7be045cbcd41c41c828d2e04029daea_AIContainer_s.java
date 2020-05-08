 package de.cloudarts.aibattleai;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Random;
 import java.util.Scanner;
 
 public class AIContainer {
 	
 	private static final long SLEEP_MILLIS = 200;
 	
 	private String _playerName = "Wojtek";
 	private int _matchID = 0;
 	private String _token = "";
 	
 	public AIContainer(String playerName_)
 	{
 		if( !playerName_.isEmpty() )
 		{
 			_playerName = playerName_;	
 		}		
 	}
 	
 	public void start()
 	{		
 		if( !requestNewMatch() )
 		{
 			return;
 		}
 		
 		// actual game loop
 		while(true)
 		{
 			//get game status
 			String lastGameStatusAnswerString = requestMatchStatus();
 			
 			if( isErrorAnswer(lastGameStatusAnswerString) )
 			{
 				return;
 			}
 			
 			if( isDrawGame(lastGameStatusAnswerString) )
 			{
 				return;
 			}
 			
 			if( !isItMyTurn(lastGameStatusAnswerString) )
 			{
 				//wait a sec, then try again
 				try 
 				{
 					Thread.sleep(SLEEP_MILLIS);
 				} 
 				catch (InterruptedException e) 
 				{
 					e.printStackTrace();
 				}
 				continue;
 			}
 			
 			//so it's my turn
 			
 			// get game status
 			
 			// compute next action
 			String[] letters = {"a", "b", "c", "d", "e", "f", "g"};
 			Random generator = new Random();
 			int actionIndex = generator.nextInt(7);
 			String action = letters[actionIndex];
 			
 			// send next action
 			System.out.println("sending action: " + action);
			System.out.println(postAction(action));
 			
 			// continue in loop
 		}
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
 		
 	    System.out.println( "requestMatchStatus: received answer:" + r );
 	    
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
 		String currentTurnPlayer = rawStatusString_.substring(0, _playerName.length());
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
 }
