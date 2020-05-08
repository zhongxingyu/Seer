 package wolves;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 
 public class RunFileGame {
 	
 	private static int NumPlayers;
 	private static int NumWolves;
 	private static Game RunningGame;
 	private static ChoiceHistory History;
 	private static WolvesUI ui;
 	private static boolean DebugMode;
 	private static String[] Players;
 
 	public static void main(String[] args) {
 		
 		// ui = new TextWolves(); //To be replaced with actual UI
 		ui = new SwingWolves();
 
 		NumPlayers = ui.getNumPlayers();
 		NumWolves = ui.getNumWolves();
 		DebugMode = ui.getDebugMode();
 		History = new ChoiceHistory();
 		getPlayerNames();
 		ui.displayPlayerIDs(getPlayerIDs());
 
 		RunningGame = new Game(NumPlayers, NumWolves);
 		// Game Object created, initialised, and probabilities updated.
 
 		boolean GameOver = false;
 
 		while(!GameOver){
 			// Each turn, one must:
 			// Take input from each player
 			RunningGame.UpdateProbabilities();
 			int[] WolfTargets = EachPlayerIO();
 			
 			RunningGame.UpdateProbabilities();
 			RunningGame.CollapseAllDead();			
 			WinCodes WinCode = RunningGame.CheckWin();
 			GameOver = (WinCode != WinCodes.GameNotOver);
 			if(GameOver) break;
 			if(DebugMode) DisplayAllStates(RunningGame.AllStatesToString());
 			
 			// Update gamestates based on attacks
 			RunningGame.AttackAllStates(WolfTargets);
 			// Wake players
 			
 			RunningGame.UpdateProbabilities();
 			RunningGame.CollapseAllDead();
 			DayTimeDisplay();
 			WinCode = RunningGame.CheckWin();
 			GameOver = (WinCode != WinCodes.GameNotOver);
 			if(GameOver) break;
 			if(DebugMode) DisplayAllStates(RunningGame.AllStatesToString());
 			
 			// Take Lynching target
 			int LynchTarget = InputLynchTarget();
 			// Update gamestates based on lynch
 			RunningGame.LynchAllStates(LynchTarget);
 			
 			RunningGame.UpdateProbabilities();
 			// run CollapseAllDead()
 			RunningGame.CollapseAllDead();
 			DayTimeDisplay();
 			WinCode = RunningGame.CheckWin();
 			GameOver = (WinCode != WinCodes.GameNotOver);
 			if(DebugMode) DisplayAllStates(RunningGame.AllStatesToString());
 		}
 		// Game is now over
 		ui.displayEndGame(RunningGame.getRoundNum(), RunningGame.CheckWin(), RunningGame.getKnownRoles());
 		DayTimeDisplay();
 		DisplayAllStates(RunningGame.AllStatesToString());
 		SelectEndGameState();
 		RunningGame.UpdateProbabilities();
 		ui.displayEndGame(RunningGame.getRoundNum(), RunningGame.CheckWin(), RunningGame.getKnownRoles());
 		printHistory();
 	}
 	
 	private static void SelectEndGameState(){
 		if(RunningGame.getNumStates() != 1) RunningGame.SelectEndState();
 	}
 	
 	public static String getPlayerName(int ID){
 		return Players[ID - 1];
 	}
 	
 	public static int getPlayerIDFromName(String inName) throws WrongNameException{
 		int n = 0;
 		boolean success = false;
 		for(int i = 0; i < NumPlayers; i++){
 			if(Players[i].equals(inName)){
 				n = i+1;
 				success = true;
 				break;				
 			}
 		}
 		if(!success) throw new WrongNameException();
 		return (n);
 	}
 	
 	public static int[] getRandomOrdering(int Size){
 		double[] randArray = new double[Size];
 		for(int n = 0; n < Size; n++){
 			randArray[n] = Math.random();
 		}
 		int[] randOrder = new int[Size];
 		double lowestRand = 1;
 		for(int i = 0; i < Size; i++){
 			for(int n = 0; n < Size; n++){
 				if(randArray[n] < lowestRand) {
 					randOrder[i] = n;
 					lowestRand = randArray[n];
 				}
 			}
 			randArray[randOrder[i]] = 1;
 			lowestRand = 1;
 		}
 		return randOrder;
 	}
 	
 	public static void getPlayerNames(){
 		// Takes input of player names from the ui, and randomly assigns them to PlayerIDs
 		Players = SetNames();
 	}
 	
 	public static String[] getPlayerIDs(){
 		return Players;
 	}
 	
 	private static int[] EachPlayerIO(){
 		// This will take inputs of visions for each player in turn, and give them their
 		// visions immediately.
 		
 		int[] randOrder = getRandomOrdering(NumPlayers);
 		 // randOrder now contains a randomised ordering of indices.
 		
 		boolean[] CanSee = RunningGame.CheckLiveSeers();
 		boolean[] CanWolf = RunningGame.CheckLiveWolves();
 		int[] Attacks = new int[NumPlayers];
 		for(int i = 0; i < NumPlayers; i++){
 			int n = randOrder[i];
 			if(CanSee[n]){
 				int Target = InputSingleVisionTarget(n+1);
 				byte Vision = RunningGame.HaveSingleVision(n+1,Target);
 				OutputSingleVision(n+1,Target,Vision);
 				RunningGame.SingleVisionAllStates(n+1, Target, Vision);
 				History.SaveVision(RunningGame.getRoundNum(), n+1, Target, Vision);
 				RunningGame.UpdateProbabilities();
 				CanSee = RunningGame.CheckLiveSeers();
 				CanWolf = RunningGame.CheckLiveWolves();
 			}
 			if(CanWolf[n]){
 				int Target = InputSingleAttackTarget(n+1);
 				Attacks[n] = Target;
 				History.SaveAttack(RunningGame.getRoundNum(), (n+1), Target);
 			} else {
 				Attacks[n] = 0;
 			}
 		}
 		
 		return Attacks;
 	}
 	
 	private static int InputSingleAttackTarget(int inPlayer){
 		return ui.InputSingleWolfTarget(inPlayer);
 	}
 	
 	private static void DisplayAllStates(String AllStateText){
 		ui.displayAllStates(AllStateText);
 	}
 	
 	private static int InputSingleVisionTarget(int Seer){
 		return ui.inputSeerTarget(Seer);
 	}
 	
 	private static void OutputSingleVision(int Seer, int Target, byte Vision){
 		ui.displaySingleVision(Seer, Target, Vision);
 	}	
 	
 	private static int InputLynchTarget(){ // return playerID for highest voted.
 		int Target = ui.inputLynchTarget();
 		History.SaveLynch(RunningGame.getRoundNum(), Target);
 		return Target;
 	}
 	
 	public static String[] SetNames(){
 		String[] Players = new String[NumPlayers];
 		int[] RandOrd = RunFileGame.getRandomOrdering(NumPlayers);
 		for(int i = 0; i < NumPlayers; i++){
 			int n = RandOrd[i];
 			boolean BadName = false;
 			String Name;
 			do{
 				Name = ui.inputName();
 				BadName = false;
 				if(Name.equals("NONE")){
 					BadName = true;
					ui.displayMessage("FUCK OFF THAT NAMES RESERVED");
 				}				
 			}while(BadName);
 			Players[n] = Name;			
 		}
 		return Players;
 	}
 	
 	private static void DayTimeDisplay(){ // Must display Good/Evil/Alive/Dead probabilities.
 		double[][] Probabilities = RunningGame.getProbabilities();
 		double[] LiveProbs = RunningGame.LivingProbabilities();
 		double DisplayProbs[][] = new double[NumPlayers][4];
 		for(int n = 0; n < NumPlayers; n++){
 			DisplayProbs[n][0] = Probabilities[n][0] + Probabilities[n][1] + Probabilities[n][2] + Probabilities[n][3];
 			DisplayProbs[n][0] *= 100;
 			DisplayProbs[n][1] = 100 - DisplayProbs[n][0];
 			DisplayProbs[n][2] = LiveProbs[n] * 100;
 			DisplayProbs[n][3] = 100 - DisplayProbs[n][2];
 		}
 		int[] RolesCodes = RunningGame.getKnownRoles();
 		for (int i = 0; i < RolesCodes.length; i++) {
 			if (RolesCodes[i] > 3) {
 				RolesCodes[i] = 3;
 			} else if (RolesCodes[i] < -3) {
 				RolesCodes[i] = -3;
 			}
 		}
 		ui.displayProbabilities(DisplayProbs, RolesCodes);		
 	}
 	
 	public static void printHistory(){
 		String HistoryText = "";
 		for(PlayerAction Action : History.ApplicableActions(RunningGame.getFirstState())){
 			HistoryText += Action.print() + "\n";
 		}
 		ui.displayHistory(HistoryText);
 	}
 	
 	public List<String> getLivePlayers(){ // returns a sorted list of all live players' names
 		List<String> output = new ArrayList<String>();
 		double[] LiveProbs = RunningGame.LivingProbabilities();
 		for(int i = 0; i < NumPlayers; i++){
 			if(LiveProbs[i] != 0) output.add(getPlayerName(i+1));
 		}
 		Collections.sort(output);
 		return output;
 	}
 }
