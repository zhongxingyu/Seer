 package wolves;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 
 public class RunFileGame {
 	
 	private static int NumPlayers;
 	private static int NumWolves;
 	private static Game RunningGame;
 	private static ChoiceHistory History;
 	private static WolvesUI ui;
 	private static boolean DebugMode;
 
 	public static void main(String[] args) {
 		
 		ui = new TextWolves(); //To be replaced with actual UI
 
 		NumPlayers = ui.getNumPlayers();
 		NumWolves = ui.getNumWolves();
 		DebugMode = ui.getDebugMode();
 		
 
 		RunningGame = new Game(NumPlayers,NumWolves);
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
 		ui.displayEndGame(RunningGame.getRoundNum(), RunningGame.CheckWin());
 		DayTimeDisplay();
 		DisplayAllStates(RunningGame.AllStatesToString());
 		SelectEndGameState();
 		DisplayAllStates(RunningGame.AllStatesToString());
 	}
 	
 	private static void SelectEndGameState(){
 		if(RunningGame.getNumStates() != 1) RunningGame.SelectEndState();
 	}
 	
 	private static int[] EachPlayerIO(){
 		// This will take inputs of visions for each player in turn, and give them their
 		// visions immediately.
 		
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
 
 		
 		boolean[] CanSee = RunningGame.CheckLiveSeers();
 		boolean[] CanWolf = RunningGame.CheckLiveWolves();
 		int[] Attacks = new int[NumPlayers];
 		for(int i = 0; i < NumPlayers; i++){
 			int n = randOrder[i];
 			if(CanSee[n]){
 				int Target = InputSingleVisionTarget(n+1);
 				byte Vision = RunningGame.HaveSingleVision(n+1,Target);
 				OutputSingleVision(n+1,Target,Vision);
 				RunningGame.SingleVisionAllStates(n, Target, Vision);
				// History.SaveVision(RunningGame.getRoundNum(), n, Target, Vision);
 				RunningGame.UpdateProbabilities();
 				CanSee = RunningGame.CheckLiveSeers();
 				CanWolf = RunningGame.CheckLiveWolves();
 			}
 			if(CanWolf[n]){
 				int Target = InputSingleAttackTarget(n+1);
 				Attacks[n] = Target;
				// History.SaveAttack(RunningGame.getRoundNum(), (n +1), Target);
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
		//History.SaveLynch(RunningGame.getRoundNum(), Target);
 		return Target;
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
 }
