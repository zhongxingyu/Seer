 /*
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>
  */
 package com.googlecode.prmf.corleone.game.state;
 
 import static com.googlecode.prmf.merapi.util.Iterators.filter;
 import static com.googlecode.prmf.merapi.util.Iterators.iterator;
 import static com.googlecode.prmf.merapi.util.Iterators.map;
 
 import com.googlecode.prmf.corleone.connection.IOThread;
 import com.googlecode.prmf.corleone.game.Game;
 import com.googlecode.prmf.corleone.game.Player;
 import com.googlecode.prmf.corleone.game.util.Action;
 import com.googlecode.prmf.corleone.game.util.VoteTracker;
 import com.googlecode.prmf.merapi.dp.Iterator;
 import com.googlecode.prmf.merapi.util.Filter;
 import com.googlecode.prmf.merapi.util.Mapper;
 import com.googlecode.prmf.merapi.util.Strings;
 
 public class Day implements MafiaGameState {
 	//TODO why are there non-private fields here?
 	VoteTracker tracker;
 	Player[] players;
 	boolean killed; // if anyone was killed the previous night
 	String dead; // who was killed , if anyone;
 	IOThread inputOutputThread; 
 	//TODO: Day, and other classes, have inputOutputThreads, inputThreads, and IOThreads. we need to pick a name and keep it
 	//TODO better yet, they shouldn't all keep a reference to it if you ask me
 
 	public Day(Player[] players, IOThread inputThread) {
 		tracker = new VoteTracker(players);
 		this.players = players;
 		this.inputOutputThread = inputThread;
 	}
 	public boolean receiveMessage(Game game, String line) {
 		boolean ret = false;
 		String speaker = line.substring(1, line.indexOf("!"));
 		String[] msg = line.split(" ");
 		
 		if(msg[1].startsWith("NICK") )
 		{
 			changeNick(speaker,msg[2]);
 			return false;
 		}
 		
 		int returnCode = parseMessage(line, speaker);
 		//TODO why do we have this part in a separate method? =\
 		if(returnCode >= 0) {
 			inputOutputThread.sendMessage(inputOutputThread.getChannel(), players[returnCode] + " was lynched :(");
 			players[returnCode].setAlive(false);
 			ret = true;
 		}
 		else if(returnCode == -2) {
 			inputOutputThread.sendMessage(inputOutputThread.getChannel(), "The majority has voted for no lynching today!");
 			ret = true;
 		}
 		else if(returnCode == -1) {
 			ret = false;
 		}
 		else if(returnCode == -3) {
 			inputOutputThread.sendMessage(inputOutputThread.getChannel(), "What?");
 			ret = false;
 		}
 		else if(returnCode == -4) {
 			inputOutputThread.sendMessage(inputOutputThread.getChannel(), speaker + " has been removed from the game!");
 		}
 		// Must handle all cases of parseMessage return such as -3,-2,-1, >=0
 
 		// TODO actually, it would be better to use enums
 		// http://java.sun.com/docs/books/tutorial/java/javaOO/enum.html
 		if (ret)
 		{
 			endState(game);
 		}
 		return ret;
 	}
         
 	//get ID number of the player for syncing with votes
 	private int searchPlayers(String name) {
 		int ret = -3;
 		
 		// TODO replace with the sexier for-each syntax
 		for(int i = 0; i < players.length; ++i) {
 			if(players[i].getName().equals(name)) { 
 				ret = i;
 				break;
 			}
 		}
 		return ret;
 	}
     private int parseMessage(String instruc, String speaker)
     {
     	// TODO this method looks like a perfect application of Java enums
     	//TODO listen to ^^^
     	int ret = -3; //this is the default "nothing happens" return.
 	    String[] instrucTokens = instruc.split(" ",5); //split into parts, parts being speaker, type of action, place it was sent to, etc
 	    String command = instrucTokens[3]; //command is "lynch" "unvote" etc
 	    String target=""; //target of lynch etc
 	    if(command.equals(":~lynch"))
 	    {
 	    	if(instrucTokens.length >= 5)
 	    		target = instrucTokens[4];
 	    	else
 	    		return -3; //no lynch target = bad vote
 	    }
 	    //extract the player and the target from the message
 	    int speakerId = searchPlayers(speaker);
 	    int targetId = searchPlayers(target);
 	    
 	    //should be impossible though
 	    if(speakerId == -3)
 	    	return -3;
 	    
 	    // TODO I hope you realize there are better ways to do this than a bunch of if/else statements.
 	    //TODO: change this to use the Class.forName() method. I think that's a much nicer solution than the one we have
 	    //change this to be similar to Pregame's receiveMessage()
 	    if( command.equals(":~lynch") )
 	    {
 	    	ret = processVote(speakerId, targetId);
 	    }
 	    else if( command.equals(":~nolynch") )
 	    {
 	    	ret = processVote(speakerId, -2);
 	    
 	    }
 	    else if( command.equals(":~unvote") )
 	    {
 	    	ret = processVote(speakerId, -1);
 	    }
 	    else if( command.equals(":~quit") )
 	    {
 	    	//TODO: check game status after this, make sure game isn't over
 	    	//kill speaker
 	    	players[speakerId].setAlive(false);
 	    	tracker.status(inputOutputThread);
 	    	ret = -4;
 	    }
 	    
 	    return ret;
     }
    
     private int processVote(int voter, int voted)
     {
     	/** int voted values:
     	 *  -4 , player quit command ..
     	 *  -3 , voted player does not exist
     	 *  -2 , vote to nolynch
     	 *  -1 , command to retract vote
     	 *  i>=0 , player ID
     	 */ 
     	
     		//check to make sure voted name exists
     		if(voted == -3)
     			return -3;
     		
     		return tracker.newVote( voter, voted , inputOutputThread);   
     		/**return values:   -3 , bad vote, not processed
     		 * 					-2, +1 nolynch
     		 * 					-1 , no majority
     		 * 					 i>=0  , i lynched.
 			 */
     }
     
     public void status() {
     	inputOutputThread.sendMessage(inputOutputThread.getChannel(), "It is now day!");
     	
     	//gives a list of players
     	if(players.length >= 1)
     		inputOutputThread.sendMessage(inputOutputThread.getChannel(), "The following people are still alive:");
     	else
     	{
     		//TODO: wtf, how is this even possible? someone copy/pasted this from pregame i think. fix it.
     		inputOutputThread.sendMessage(inputOutputThread.getChannel(), "There is no one registered yet!");
     		return;
     	}
     	
     	//wtf?
     	//this block makes a list of all the players alive, then prints it
     	//i'm not sure exactly how it works though, hopefully someone else will explain
     	//TODO: someone else explain
 		Filter<Player> live = new Filter<Player>() {
 			@Override
 			public boolean keep(Player p) {
 				return p.isAlive();
 			}
 		};
 		Mapper<Player,String> getNames = new Mapper<Player,String>() {
 			@Override
 			public String map(Player p) {
 				return p.getName();
 			}
 		};
 
 		Iterator<Player> playersIter = iterator(players);
 		Iterator<Player> livingPlayers = filter(live, playersIter);
 		Iterator<String> livingPlayersNames = map(getNames, livingPlayers);
 
 		String livingPeople = Strings.join(", ", livingPlayersNames);
 
 		inputOutputThread.sendMessage(inputOutputThread.getChannel(), livingPeople);
     }
     
     class LynchAction implements Action {
 
     	private int voter;
     	private int voted;
     	public LynchAction(int voter, int voted)
     	{
     		this.voter=voter;
     		this.voted=voted;
     	}
     	public void handle() {
     		//just pass the vote to the tracker
     		tracker.newVote(voter, voted, inputOutputThread);
     	}
     }
 	private void changeNick(String oldNick , String newNick)
 	{
 		System.err.println(oldNick + " to " + newNick);
 		for(int i=0;i<players.length;++i)
 		{
 			if(players[i].getName().equals(oldNick))
 			{
 				players[i].setName(newNick.substring(1));
 				return;
 			}
 		}
 	}
 	
 	public void endState(Game game)
 	{
 		game.setState(new Night(players, inputOutputThread));
 		for(Player p : game.getPlayerList())
 		{
 			//unvoice the players since NO TALKING DURING THE NIGHT
 			inputOutputThread.sendMessage("MODE",inputOutputThread.getChannel(), "-v "+p.getName());
 		}
		game.startTimer();
 	}
 }
 
 
