 package wolves;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 public class TextWolves implements WolvesUI {
 	
 	private int players;
 	private int wolves;
 	
 	private int getIntFromUser(String prompt) {
 		while (true) {
 			try {
 				return Integer.parseInt(getUserInput(prompt));
 			} catch (Exception e) {
 				System.out.println("FUCK OFF THATS NOT A NUMBER");
 			}
 		}
 	}
 	
 	private int getNameIDFromUser(String prompt){
 		while(true){		
 			try {
 				String Name = getUserInput(prompt);
 				if (Name.equals("NONE")) return 0;
 				return RunFileGame.getPlayerIDFromName(Name);
 			} catch (WrongNameException e) {
 				System.out.println("FUCK OFF THATS NOT A NAME");
 			}
 		}
 	}
 	
 	
 	public boolean getDebugMode() {
 		return (getIntFromUser("ENTER 1 FOR DEBUG MODE") == 1);
 	}
 	
 	@Override
 	public int getNumPlayers() {
 		this.players = getIntFromUser("PLEASE CHOOSE HOW MANY OF PEOPLE");
 		return players;
 	}
 	
 	@Override
 	public int getNumWolves() {
 		this.wolves = getIntFromUser("PLEASE CHOOSE HOW MANY OF WOLVES");
 		return wolves;
 	}
 	
 	private String getUserInput(String prompt) {
 		BufferedReader c = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println(prompt);
 		try {
 			return c.readLine();
 		} catch (IOException e) {
 			throw (new RuntimeException(e));
 		}
 	}
 			
 	@Override
 	public int inputSeerTarget(int inSeer){
 		return getNameIDFromUser("\nPLEASE CHOOSE WHO IS SAW BY PLAYER " + inSeer + " (" + RunFileGame.getPlayerName(inSeer) + "), or 'NONE' for no vision");
 	}
 
 	@Override
 	public void displayPlayerIDs(String[] inArray){
 		System.out.println("Assigned player names:");
 		for(int i = 0; i < players; i++){
 			System.out.print((i+1) + " - " + inArray[i] +"\n");
 		}
 	}
 	
 	@Override
 	public void displaySingleVision(int Seer, int Target, byte Vision){
 		if(Target == 0){
 			// Do Nothing.
 		} else {
 			String role = null;
 			switch (Vision) {
 			case WolvesUI.VISION_INNO:
 				role = "INNOCENT";
 				break;
 			case WolvesUI.VISION_WOLF:
 				role = "WHEREWOLF";
 				break;
 			}
 			System.out.println("PLAYER " + Seer + " (" + RunFileGame.getPlayerName(Seer) + ") SEES THAT " + RunFileGame.getPlayerName(Target) + " IS " + getAdjective() + " " + role);
 		}
 	}
 	
 	@Override
 	public int InputSingleWolfTarget(int inPlayer){
 		return getNameIDFromUser("\nPLEASE CHOOSE WHO IS WOLFED DOWN BY PLAYER " + inPlayer + " (" + RunFileGame.getPlayerName(inPlayer) + ")");
 	}
 	
 	@Override
 	public void displayEndGame(int RoundNum, WinCodes WinCode, int[] knownRoles){
 		if(WinCode == WinCodes.NoStatesRemain){
 			System.out.println("No Gamestates remain.");
 		} else {
 			String WinningTeam = null;
 			switch(WinCode){
 			case InnocentsWon : WinningTeam = "Innocents";
 			break;
 			case WolvesWon : WinningTeam = "Wolves";
 			break;
 			case ERROR : WinningTeam = "ERROR";
 			System.out.println("SOMETHING HAS GONE WRONG");
 			break;
 			}
 			System.out.println("Game Over. The " + WinningTeam + " have won after " + RoundNum + " rounds.");
 		}
 		int n;
 		for (int i = 0; i < players; i++) {
 			n = 1;
 			String role = null;
 			String dead  = "";
 			if (knownRoles[i] < 0) {
 				dead = "DEAD ";
 				n = -1;
 			}
 			switch (n*knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 				break;
 			case ROLE_SEER:
 				role = "SEER";
 				break;
 			}
 			if(n*knownRoles[i] >= 3) role = "WOLF";
 			if (knownRoles[i] != 0) {
 				System.out.println("PLAYER " + (i+1) + " (" + RunFileGame.getPlayerName(i+1) + ")" + " IS " + getAdjective() + " " + dead + role);
 			}
 		}
 	}
 	
 	@Override
 	public void displayAllStates(String AllStateText){
 		System.out.println(AllStateText);
 	}
 	
 	private String getAdjective() {
 		String[] words = {"A FILTHY", "A CLEAN", "A CAPRICIOUS", "A WELL-INFORMED", "AN INSANE", "A SANITARY", "A TUMULTUOUS",
 			"AN INFLAMED", "AN INFLAMMATORY", "A CALAMATOUS", "AN INCONGRUOUS", "A RADISHY", "A FERAL", "A FINGER-LICKIN'", 
 			"A PERNICIOUS", "A LOOPY", "A DIRTY", "AN ANGRY", "A FOCUSED", "AN ANTICLIMATIC", "A HORRIBLE",
 			"A SCARY", "A MONSTROUS", "A TERRIBLE", "A TERRIFYING", "A DESPICABLE", "A SHIT",
 			"AN OVERPRICED", "AN EGREGIOUS", "A GREGARIOUS", "A MISSPELLED", "A HORRIFIC", "A FUCKED-UP",
 			"AN ABOMINABLE", "A COLD-HEARTED", "A HERETICAL", "A CANTANKEROUS", "A POLISHED", "A HAPPY",
 			"A FUZZY", "A FLUFFY", "A CUTE", "A TECHNOLOGICAL", "A MEGARIFFIC", "A RURAL", "A MORBID",
 			"A GRIM", "AN ANIMALISTIC", "A FRIENDLY", "A KIND-HEARTED", "A MURDEROUS", "A DANGEROUS",
 		"A BRUTAL", "A CALM", "A PSYCHOTIC", "A PSYCOPATHIC", "AN EVIL"};
 		Random generator = new Random();
 		return words[generator.nextInt(words.length)];
 	}
 	
 	@Override
 	public int inputLynchTarget() {
 		return getNameIDFromUser("PLEASE CHOOSE WHO IS LUNCHED");
 	}
 	
 	private String pad(int i) {
 		String s = String.valueOf(i);
 		int padding = 5 - s.length();
 		for (int j = 0; j < padding; j++) {
 			s = s + " ";
 		}
 		return s;
 	}
 	
 	@Override
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles) {
 		System.out.println("\nPLAY GOOD EVIL LIVE DEAD");
 		for (int i = 0; i < players; i++) {
 			System.out.print(pad(i + 1));
 			for (int j = 0; j < 4; j++) {				
 				System.out.print(pad((int) Math.round(probabilities[i][j])));
 			}
 			System.out.println();
 		}
 		System.out.println("KNOWN ROLLS");
 		int n;
 		for (int i = 0; i < players; i++) {
 			n = 1;
 			String role = null;
 			String dead  = "";
 			String name = "";
 			if (knownRoles[i] < 0) {
 				dead = "DEAD ";
 				n = -1;
 				name = " (" + RunFileGame.getPlayerName(i+1) + ")";
 			}
 			switch (n*knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 				break;
 			case ROLE_SEER:
 				role = "SEER";
 				break;
 			case ROLE_WOLF:
 				role = "WOLF";
 				break;
 			}
 			if (knownRoles[i] != 0) {
 				System.out.println("PLAYER " + (i+1) + name + " IS " + getAdjective() + " " + dead + role);
 			}
 		}
 	}
 
 	@Override
 	public String inputName(){
 		return getUserInput("PLEASE ENTER A NAME OF PLAYER");
 		// TODO Is this okay Jamie?
 	}
 	
 	@Override
 	public String[] SetNames(){
 		String[] Players = new String[players];
 		int[] RandOrd = RunFileGame.getRandomOrdering(players);
 		for(int i = 0; i < players; i++){
 			int n = RandOrd[i];
 			boolean BadName = false;
 			String Name;
 			do{
 				Name = inputName();
 				BadName = false;
 				if(Name.equals("NONE")){
 					BadName = true;
 					System.out.println("FUCK OFF THAT NAMES RESERVED");
 				}				
 			}while(BadName);
 			Players[n] = Name;			
 		}
 		return Players;
 	}
 	
 	public void displayHistory(String HistoryText){
 		System.out.println("\nWhat just happened!?");
 		System.out.println(HistoryText);
 	}
 	
 }
