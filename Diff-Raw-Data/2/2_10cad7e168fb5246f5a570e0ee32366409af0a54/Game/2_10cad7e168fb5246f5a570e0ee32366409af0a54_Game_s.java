 package wolves;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 // This is a change.
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 public class Game {
 	
 	private int NumPlayers;
 	private int NumWolves;
 	private ArrayList<GameState> AllStates;
 	private double[][] Probabilities;
 	private int RoundNum;
 	
 	public Game(int inPlayers, int inWolves){
 		
 		RoundNum = 1;
 		NumPlayers = inPlayers;
 		NumWolves = inWolves;
 		this.Initialise(); // AllStates now contains all possible initial gamestates.
 		this.UpdateProbabilities(); // Probabilities[][] now contains Player role probabilities:
 		// First index is the playerID, second is: 0-innocent 1-seer 2,3-dead versions, 4to(3+NumWolves) are wolves 
 		// (4+NumWolves)to(3+2*NumWolves) are dead wolves
 		
 	}
 	
 	private void Initialise(){
 		// Fill Vector with all possible game states for round one.
 		
 		AllStates = new ArrayList<GameState>();
 		
 		int[] Perms = new int[NumWolves + 1];	
 		for(int i = 0; i < Perms.length; i++){
 			Perms[i] = i + 1;
 		}
 		
 		boolean p = false;
 		do {
 			AllStates.add(new GameState(Perms, NumPlayers));
 			p = nextPerm(Perms);
 		} while (p);
 		
 	}
 	
 	public int getRoundNum(){
 		return RoundNum;
 	}
 	
 	
 	private boolean nextPerm(int[] inPerm){
 		// returns true if this was successful in finding the next permutation
 		
 		int[] outPerm = new int[inPerm.length];
 		for(int i = 0; i < outPerm.length; i++){
 			outPerm[i] = inPerm[i];
 		}
 		
 		// have function (incrementWolf) which returns the next wolf ID, or zero if none
 		// run increment wolf on last wolf
 		// if zero, run on previous until not zero.
 		// then run on wolves skipped over to end.
 		int p = 0;
 		int i = inPerm.length;
 		do {
 			i--;
 			p = incrementID(i, inPerm);
 			if(p == -1) break;
 			outPerm[i] = p;
 		} while((p == 0) && (i != -1));
 		if(i == -1) return false; // This was the final permutation, inPerm remains unchanged.
 		for(i = 0; i < inPerm.length; i++){
 			if(outPerm[i] == 0){
 				outPerm[i] = incrementID(i, outPerm);
 			}
 		} // outPerm now contains the next permutation
 		
 		for(int n = 0; n < inPerm.length; n++){
 			inPerm[n] = outPerm[n];
 		} // inPerm is updated to next permutation 
 		
 		return true;		
 	}
 	
 	private int incrementID(int inIndex, int[] inPerm) {
 		// returns the next possible ID not used by a previous index.
 		
 		if(inIndex == -1) return -1;
 		int n = inPerm[inIndex];
 		boolean p = false;
 		do{			
 			n = (n + 1) % (NumPlayers + 1);
 			p = false;
 			for(int i = 0; i < inIndex;i++){ // check for duplicates
 				if(n == inPerm[i]) p = true;
 			}
 		} while(p);
 		
 		return n;
 		
 	}
 	
 	public void UpdateProbabilities(){
 		// Compute probabilities of each player being each role.
 		// Do this by calling AllPlayers method from GameState
 		// Compile a tally of each player being each thing against total states
 		
 		int[][] RoleCount = new int[NumPlayers][4 + (2 * NumWolves)];
 		// First index is the playerID, second is: 0-innocent 1-seer 2,3-dead versions,
 		// 4to(4+NumWolves) are live wolves, and 4 + NumWolves to 4 + (2 * NumWolves) are dead wolves.
 		int n = 0;
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			for(int j = 0; j < NumPlayers; j++){
 				int b = a.AllPlayers()[j];
 				if(b == 1){
 					RoleCount[j][0]++; // Live innocent
 				} else if(b == 2){
 					RoleCount[j][1]++; // live seer
 				} else if(b == -1){
 					RoleCount[j][2]++; // dead innocent	
 				} else if(b == -2){
 					RoleCount[j][3]++; // dead seer
 				} else {
 					int m = 1;
 					if(b < 0){
 						m = m + NumWolves;
 						b = (-1) * b;
 					}
 					RoleCount[j][m+b]++;
 				}
 			}
 			n++;
 		}
 		Probabilities = new double[NumPlayers][4 + (2 * NumWolves)];
 		for(int m = 0; m < NumPlayers; m++){
 			for(int j = 0; j < (4 + (2 * NumWolves)); j++){
 				Probabilities[m][j] = ((double)RoleCount[m][j]) / ((double)n);
 			}
 		}
 	}
 	
 	public double[][] getProbabilities(){
 		return this.Probabilities;
 	}
 	
 	public int getNumStates(){
 		return AllStates.size();
 	}
 	
 	public void SelectEndState(){
 		Random Rand = new Random();
 		int StateIndex =  Rand.nextInt(getNumStates());
 		Iterator<GameState> i = AllStates.iterator();
 		int n = 0;
 		while(i.hasNext()){
 			i.next();
 			if(n == StateIndex){
 				// state is chosen
 			} else { //state was not chosen, and is removed.
 				i.remove();
 			}
 			n++;
 		}
 	}
 	
 	public GameState getFirstState(){
 		return AllStates.get(0);
 	}
 		
 	public byte HaveSingleVision(int Seer, int inTarget){
 		// 1 means innocent i.e. Villager or seer, 2 means wolf
 		if(inTarget == 0){			
 			return 0;
 		} else {
 			double TargetProbGood = 0;
 			for(int i = 0; i < 4; i++){
 				TargetProbGood += Probabilities[inTarget - 1][i];
 			}
 			return (byte) ((Math.random() < TargetProbGood) ? 1 : 2);
 		}
 	}
 
 	public byte[] HaveVisions(int[] inTargets){ // 1 means innocent, 2 means wolf, and 0 means an input of zero, i.e. 
 		// player having vision cannot be seer.
 		byte[] output = new byte[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			int TargetID = inTargets[n];
 			if(TargetID == 0){
 				output[n] = 0;
 			} else {
 				double TargetProbGood = 0;
 				for(int i = 0; i < 4; i++){
 					TargetProbGood += Probabilities[TargetID - 1][i];
 				}
 				output[n] = (byte) ((Math.random() < TargetProbGood) ? 1 : 2);
 			}
 		}
 		
 		return output;
 	}
 	
 	public void	LynchAllStates(int inTarget){
 		RoundNum++;
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			if(a.Lynch(inTarget)){
 				// state is allowed
 			} else { //state was not allowed, and is removed.
 				i.remove();
 			}
 		}
 	}
 	
 	public double[] LivingProbabilities(){
 		double[] output = new double[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			double ProbLiving = 0;
 			for(int i = 0; i < 4 + NumWolves; i++){
 				if((i == 2) || (i == 3)) {
 					// These are dead roles, ignore.
 				} else {
 					ProbLiving += Probabilities[n][i];
 				}
 			}
 			output[n] = ProbLiving;
 		}
 		return output;
 	}
 	
 	private int CharacterCollapse(int inTarget){ // computes and returns role code of the freshly deceased.
 		double[] CumulProbs = new double[4 + (2*NumWolves)];
 		CumulProbs[0] = Probabilities[inTarget - 1][0];
 		for(int i = 1; i < (4 + (2*NumWolves)); i++){
 			CumulProbs[i] = Probabilities[inTarget - 1][i] + CumulProbs[i-1];
 		}
 		double ran = Math.random();
 		int i;
 		for(i = 0; i < (4 + (2*NumWolves)); i++){
 			if(ran <= CumulProbs[i]) break;
 		}
 		int Role = 0;
 		if(i == 0 || i == 2){
 			Role = 1;
 		} else if (i == 1 || i == 3) {
 			Role = 2;
 		} else if(i < (4 + NumWolves)){
 			Role = i - 1;
 		} else if(i >= (4 + NumWolves)){
 			Role = i - NumWolves - 1;
 		}
 		return Role;
 	}
 	
 	public void AllStateCharCollapse(int inTarget){
 		int inRole = CharacterCollapse(inTarget);
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			if(a.SurviveCollapse(inTarget, inRole)){
 				// do nothing, state is allowed.
 			} else {
 				i.remove();
 			}
 		}
 	}
 	
 	public void AttackAllStates(int[] inTargets){
 //		Iterator<GameState> i = AllStates.iterator();
 //		while(i.hasNext()){
 //			GameState a = i.next();
 //			if(a.WolfAttack(inTargets)){
 //				// state is allowed, and has been updated.
 //			} else { //state was not allowed, and is removed
 //				i.remove();
 //			}
 //		}
 		
 		int[] RandOrd = RunFileGame.getRandomOrdering(NumPlayers);
 		for(int i = 0; i < NumPlayers; i++){
 			int n = RandOrd[i];
 			Iterator<GameState> it = AllStates.iterator();
 			while(it.hasNext()){
 				GameState a = it.next();
				if(n == a.LeadWolf()){
 					if(a.WolfAttack(inTargets)){
 						// state is allowed, and has been updated.
 					} else { //state was not allowed, and is removed
 						it.remove();
 					}
 				} else {
 					// do nothing
 				}
 				UpdateProbabilities();
 				CollapseAllDead();
 				boolean GameOver = (CheckWin() != WinCodes.GameNotOver);
 				if(GameOver) {
 					i = NumPlayers; // breaks for
 					break; // breaks while
 				}
 			}
 		}
 	}
 	
 	
 	public void VisionAllStates(int[] inTargets, byte[] inVisions){
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			if(a.SurviveVisions(inTargets, inVisions)){
 				// state is allowed, and has been updated.
 			} else { //state was not allowed, and is removed
 				i.remove();
 			}
 		}
 	}
 	
 	public void SingleVisionAllStates(int inSeer, int inTarget, byte inVision){
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			if(a.SurviveSingleVision(inSeer, inTarget, inVision)){
 				// state is allowed, and has been updated.
 			} else { //state was not allowed, and is removed
 				i.remove();
 			}
 		}
 	}
 	
 
 	public int[] getKnownRoles(){
 		int[] output = new int[NumPlayers];
 		
 		for(int n = 0; n < NumPlayers; n++){
 			int index = -1;
 			int role = 0;
 			for(int i = 0; i < (4 + 2*NumWolves); i++){
 				if(Probabilities[n][i] == 1) index = i;
 			}
 			if(index == 0) role = 1;
 			if(index == 1) role = 2;
 			if(index == 2) role = -1;
 			if(index == 3) role = -2;
 			if((index > 3) && (index <= (3 + NumWolves))) role = index - 1;
 			if((index > (3 + NumWolves))) role = (index - 1 - NumWolves) *-1;
 			output[n] = role;			
 		}		
 		return output;
 	}
 	
 	public void CollapseAllDead(){ // Collapses the roles of any dead characters
 		UpdateProbabilities();
 		int[] KnownRoles = getKnownRoles();
 		double[] ProbLive = LivingProbabilities();
 		boolean repeat = true;
 		
 		while(repeat){
 			repeat = false;
 			// Generating a random ordering:
 			double[] randArray = new double[NumPlayers];
 			for(int n = 0; n < NumPlayers; n++){
 				randArray[n] = Math.random();
 			}
 			int[] randOrder = new int[NumPlayers];
 			double lowestRand = 1;
 			for(int i = 0; i < NumPlayers; i++){
 				for(int n = 0; n < NumPlayers; n++){
 					if(randArray[n] < lowestRand) {
 						randOrder[i] = n;
 						lowestRand = randArray[n];
 					}
 				}
 				randArray[randOrder[i]] = 1;
 				lowestRand = 1;
 			} // randOrder now contains a randomised ordering of indices.
 			
 			
 			for(int i = 0; i < NumPlayers; i++){
 				int n = randOrder[i]; // This steps over the array in a random order.
 				if(ProbLive[n] == 0){
 					if(KnownRoles[n] == 0){ // This player needs collapsing
 						AllStateCharCollapse(n+1); // This changes probabilities, but does not update them.
 						UpdateProbabilities();
 						repeat = true;
 					}
 				}
 			}
 			UpdateProbabilities();
 			KnownRoles = getKnownRoles();
 			ProbLive = LivingProbabilities();
 		}
 		
 	}
 	
 	public WinCodes CheckWin(){
 		
 		// If there is a non-zero probability of one person from either team being alive, then return zero
 		double probLiveInnocent = 0;
 		double probLiveWolf = 0;
 		for(int n = 0; n < NumPlayers; n++){
 			probLiveInnocent += Probabilities[n][0] + Probabilities[n][1];
 			for(int i = 0; i < NumWolves; i++){
 				probLiveWolf += Probabilities[n][i+4];
 			}
 		}
 		boolean LiveInnocentsExist = (probLiveInnocent != 0);
 		boolean LiveWolvesExist = (probLiveWolf != 0);
 		if(LiveInnocentsExist && LiveWolvesExist) return WinCodes.GameNotOver;
 		if(LiveInnocentsExist && !LiveWolvesExist) return WinCodes.InnocentsWon;
 		if(!LiveInnocentsExist && LiveWolvesExist) return WinCodes.WolvesWon;
 		if(!LiveInnocentsExist && !LiveWolvesExist) return WinCodes.NoStatesRemain;
 		return WinCodes.ERROR;
 	}
 	
 	private boolean CheckLiveSeer(int inTarget){ // returns true if there is a chance player can be a live seer.
 		boolean output = (Probabilities[inTarget - 1][1] != 0);
 		return output;
 	}
 	public boolean[] CheckLiveSeers(){
 		boolean[] output = new boolean[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			output[n] = CheckLiveSeer(n+1);
 		}
 		return output;
 	}
 	
 	private boolean CheckLiveWolf(int inTarget){ // returns true if there is a chance player can be a live wolf.
 		double probLiveWolf = 0;
 		for(int i = 0; i < NumWolves; i++){
 			probLiveWolf += Probabilities[inTarget - 1][i+4];
 		}
 		return (probLiveWolf != 0);
 	}
 	public boolean[] CheckLiveWolves(){
 		boolean[] output = new boolean[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			output[n] = CheckLiveWolf(n+1);
 		}
 		return output;
 	}
 	
 	public List<GameState> getAllStates(){
 		return AllStates;
 	}
 	
 	public String AllStatesToString(){
 		String output = "All Current States: \n";
 		Iterator<GameState> i = AllStates.iterator();
 		while(i.hasNext()){
 			GameState a = i.next();
 			output +=((a.RolesToString()) + "\n");
 		}
 		return output;
 	}
 	
 }
