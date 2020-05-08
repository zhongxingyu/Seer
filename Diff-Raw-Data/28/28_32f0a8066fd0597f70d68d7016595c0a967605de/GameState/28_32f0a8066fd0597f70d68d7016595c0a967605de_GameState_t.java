 package wolves;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 public class GameState {
 	// This class stores information about a particular game state.
 	
 	private int RoundNum; // Round numbers are incremented from zero upon each lynching.
 	private int NumPlayers; // Total number of players, living or dead.
 	private int NumWolves;
 	private int[] PlayerRoles;
 	
 	public GameState(int[] Perm, int inNumPlayers){
 		RoundNum = 0;
 		NumWolves = Perm.length - 1;
 		NumPlayers = inNumPlayers;
 		PlayerRoles = new int[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			PlayerRoles[n] = 1; // Initialise all players to be living innocents.
 		}
 		for(int i = 0; i < Perm.length; i++){
 			int n =  Perm[i];			
 			PlayerRoles[n - 1] = i + 2;			
 		}
 	}
 	
 	public int getRoundNum() {
 		return RoundNum;
 	}
 
 	public int playerTest(int inPlayer){ // returns 1 if innocent, 2 if Seer, and rest are wolves
 		// Negative results are dead.
 		return this.PlayerRoles[inPlayer - 1];	
 	}
 	
 	public int[] AllPlayers() { // Returns an array of ints, each being the code for the corresponding player ID. 
 		// e.g. [1, 2, -3] would indicate that Player 1 is an innocent, p2 the seer, and p3 a dead  alpha wolf.
 		return PlayerRoles;
 	}
 	
 	public boolean Lynch(int inPlayer){ // increments the RoundNum and changes inPlayer to be dead.
 		// returns true if this is allowed, false if they are already dead, and the state is removed.
 		RoundNum++;
 		if(this.playerTest(inPlayer) > 0){
 			PlayerRoles[inPlayer - 1] = (-1) * (PlayerRoles[inPlayer - 1]);
 			return true;
 		} else return false;
 	}
 	
 	public boolean SurviveVisions(int[] inTargets, byte[] inVisions){ // returns true if this set of visions is compatible 
 		// with this GameState.  In the boolean array, 1 means innocent, 2 means wolf, 0 means no target.
 		boolean output = true;
 		for(int n = 0; n < NumPlayers; n++){
 			if(this.playerTest(n+1) == 2){
 				if(inTargets[n] == 0) return true; // No vision was had, therefore no conflict.
 				output = ((playerTest(inTargets[n]) <= (-3)) || (playerTest(inTargets[n]) >= 3)); // output is now 
 				// true if the Seer saw a wolf.
				output = (((inVisions[n] == 2) && output) || ((inVisions[n] == 1) && !output));
 			}
 		}
 		return output;
 	}
 	
 	public boolean SurviveSingleVision(int inSeer, int inTarget, byte inVision){
 		boolean output = true;
 		if(this.playerTest(inSeer+1) == 2){
 			if(inTarget == 0) return true; // No vision was had, therefore no conflict.
 			output = ((playerTest(inTarget) <= (-3)) || (playerTest(inTarget) >= 3)); // output is now 
 			// true if the Seer saw a wolf.
			output = (((inVision == 2) && output) || ((inVision == 1) && !output));
 		}
 		return output;
 	}
 	
 	private int LeadWolf(){ // returns the player ID of the highest ranking living wolf{
 		int output = 0;
 		for(int n = 0; n < NumWolves;n++){
 			for(int i = 0; i < NumPlayers; i++){
 				if(PlayerRoles[i] == (n+3)){
 					output = (i+1);
 					n = NumWolves;
 				}
 			}
 		}
 		
 		return output;
 	}
 
 	public boolean SurviveCollapse(int inTarget, int inRole){ // returns true if this role is compatible
 		return (inRole == ((-1) * playerTest(inTarget)));
 	}
 	
 	public boolean WolfAttack(int [] inTargets){ // carries out the lead wolf's murder. returns true if allowed
 		int TargetRole = PlayerRoles[inTargets[this.LeadWolf() - 1] - 1];
 		if ((TargetRole >= 3) || (TargetRole < (0))){
 			return false; // Target is either a wolf, or dead.  This gamestate should be removed.
 		} else {
 			PlayerRoles[inTargets[this.LeadWolf() - 1] - 1] = -TargetRole;
 			return true;
 		}
 	}
 	public String RolesToString(){
 		String output = "";
 		for(int i = 0; i < NumPlayers; i++){
			output += "(" + (i+1);
 			if(PlayerRoles[i] < 0) output += " Dead";
 			switch(PlayerRoles[i]){
 			case 1: output += " Villager";
 			break;
 			case 2: output += " Seer";
 			break;
			case -1: output += " Villager";
			break;
			case -2: output += " Seer";
			break;
 			}
			if(PlayerRoles[i] >= 3) output += " " + (PlayerRoles[i] - 2) + "-Wolf";
			if(PlayerRoles[i] <= -3) output += " " + (-1 * (PlayerRoles[i] - 2)) + "-Wolf";
 			output += ")";
 		}		
 		return output;
 	}
 }
