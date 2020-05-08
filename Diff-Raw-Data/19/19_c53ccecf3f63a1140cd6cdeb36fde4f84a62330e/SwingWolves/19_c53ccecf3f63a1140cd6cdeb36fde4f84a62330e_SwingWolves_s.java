 package wolves;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.WindowConstants;
 
 public class SwingWolves implements WolvesUI {
 	
 	private int players;
 	private int wolves;
 	
 	public SwingWolves(){
 		//I think we should use this space as a convenient alternative for commit messages
 		// If you like, but it is no longer empty...
 		
 		boolean startGame = false;
 		while(!startGame){
 			Object[] buttons = {"Instructions", "About", "Lets play this game!"};
 			int UserInput = JOptionPane.showOptionDialog(null,
 						    "Welcom to this Java implementation of Quantum Werewolves",
 						    "Quantum Werewolves",
 						    JOptionPane.YES_NO_CANCEL_OPTION,
 						    JOptionPane.QUESTION_MESSAGE,
 						    null,
 						    buttons,
 						    buttons[2]);
 			switch(UserInput){
 			case JOptionPane.YES_OPTION : displayHelp();
 			break;
 			case JOptionPane.NO_OPTION : displayAbout();
 			break;
 			case JOptionPane.CLOSED_OPTION : System.exit(0);
 			break;
 			case JOptionPane.CANCEL_OPTION : startGame = true;		
 			}
 		}
 	}
 
 	private String getUserInput(String prompt) {
 		Object output = JOptionPane.showInputDialog(null, prompt, "Quantum Werewolves", JOptionPane.QUESTION_MESSAGE);
 		if(output == null){
 			System.exit(0); // terminate program...
 			return null; // why is this nessecary...  (I can't spell...)
 		}
 		else return (String) output;
 	}
 	
 	private int getPlayerIDFromUser(String prompt, String[] arrplay) {
 				
 		while(true){		
 			try {
 				String Name = PlayerSelectFrame.choosePlayer(prompt, arrplay);
 				if (Name.equals("NONE")) return 0;
 				return RunFileGame.getPlayerIDFromName(Name);
 			} catch (WrongNameException e) {
 				displayError("FUCK OFF THATS NOT A NAME");
 			}
 		}
 	}
 	
 	
 	
 	private int getIntFromUser(String prompt) {
 		while (true) {
 			try {
 				return Integer.parseInt(getUserInput(prompt));
 			} catch (Exception e) {
 				displayError("FUCK OFF THATS NOT A NUMBER");
 			}
 		}
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
 
 	@Override
 	public void displayEndGame(int RoundNum, WinCodes WinCode, int[] knownRoles) {
 		String text = "";
 		
 		if(WinCode == WinCodes.NoStatesRemain){
 			text = "No Gamestates remain.";
 		} else {
 			String WinningTeam = null;
 			switch(WinCode){
 			case InnocentsWon : WinningTeam = "Innocents";
 			break;
 			case WolvesWon : WinningTeam = "Wolves";
 			break;
 			case ERROR : WinningTeam = "ERROR";
 			text = ("SOMETHING HAS GONE WRONG");
 			break;
 			}
 			text = ("Game Over. The " + WinningTeam + " have won after " + RoundNum + " rounds.\n");
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
 				text += ("PLAYER " + (i+1) + " (" + RunFileGame.getPlayerName(i+1) + ")" + " IS " + getAdjective() + " " + dead + role + "\n");
 			}
 		}
 		
 		displayString(text);
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
 		List<String> p = RunFileGame.getLivePlayers();
 		String[] arrplay = new String[p.size()];
 		for (int i = 0; i < arrplay.length; i++) {
 			arrplay[i] = p.get(i);
 		}
 		return getPlayerIDFromUser("PLEASE CHOOSE WHO IS LUNCHED", arrplay);
 	}
 
 	@Override
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles) {
 		String rolesText = "<html>";
 
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
 //				text += ("PLAYER " + (i+1) + name + " IS " + getAdjective() + " " + dead + role + "\n");
 				rolesText += ("PLAYER " + (i+1) + name + " IS " + getAdjective() + " " + dead + role + "<br>");
 			}
 		}
 		@SuppressWarnings("unused")
 		DaytimeDisplayFrame testFrame = new DaytimeDisplayFrame(probabilities, rolesText + "</html>");
 	}
 	
 
 	@Override
 	public int inputSeerTarget(int inSeer) {
 		List<String> p = RunFileGame.getLivePlayers();
 		String[] arrplay = new String[p.size() + 1];
 		arrplay[0] = "NONE";
 		for (int i = 1; i < arrplay.length; i++) {
 			arrplay[i] = p.get(i - 1);
 		}
 		return getPlayerIDFromUser("\nPLEASE CHOOSE WHO IS SAW BY PLAYER " + inSeer + " (" + RunFileGame.getPlayerName(inSeer) + ")", arrplay);
 	}
 
 	@Override
 	public void displaySingleVision(int Seer, int Target, byte Vision) {
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
 			displayString("PLAYER " + Seer + " (" + RunFileGame.getPlayerName(Seer) + ") SEES THAT " + RunFileGame.getPlayerName(Target) + " IS " + getAdjective() + " " + role);
 		}
 
 	}
 
 	@Override
 	public void displayAllStates(String AllStateText) {
 		displayString(AllStateText);
 	}
 	
 	@Override
 	public void displayAllStates(List<GameState> AllStates){
 		final String newline = "<br>";
 		String output = "<html>";
 		for(GameState gameState: AllStates){
 			int[] PlayerRoles = gameState.AllPlayers();
 			for(int i = 0; i < PlayerRoles.length; i++){
 				output += "(" + (i+1) + " " + RunFileGame.getPlayerName(i+1);
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
 				if(PlayerRoles[i] <= -3) output += " " + (-1 * (PlayerRoles[i] + 2)) + "-Wolf";
 				output += ")";
 			}	
 			output += newline;
 		}
 		output += "</html>";
 		
 		JScrollPane ScrPane = new JScrollPane(new JLabel(output));
 		final JDialog dialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);	
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		dialog.setLocationRelativeTo(null);
 		
 		JPanel somePanel = new JPanel();
 		somePanel.setLayout(new BorderLayout());
 		somePanel.add(new JLabel("This is a list of all currently possible gamestates:"),BorderLayout.NORTH);
 		somePanel.add(ScrPane, BorderLayout.CENTER);
 		
 		JButton button = new JButton("Ok");
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dialog.setVisible(false);
 			}				
 		});
 		somePanel.add(button, BorderLayout.SOUTH);
 		dialog.add(somePanel);
 		dialog.pack();
 		dialog.setSize(new Dimension(600,325));
 		somePanel.setSize(new Dimension(600,300));
 		somePanel.setMaximumSize(new Dimension(600,300));
 		ScrPane.setMaximumSize(new Dimension(600,300));
 		ScrPane.setPreferredSize(new Dimension(600,300));
 		somePanel.setPreferredSize(new Dimension(600,300));
 		
 		dialog.setVisible(true);
		dialog.setLocationRelativeTo(null);
 	}
 
 	@Override
 	public boolean getDebugMode() {
 		
 		Object[] buttons = {"Debug mode", "Play Normally"};
 		int userInput = JOptionPane.showOptionDialog(null,
 					    "Would you like to use debug mode? \n (This will display all game-states at every update.)",
 					    "Quantum Werewolves",
 					    JOptionPane.YES_NO_OPTION,
 					    JOptionPane.QUESTION_MESSAGE,
 					    null,
 					    buttons,
 					    buttons[1]);
 		switch(userInput){
 		case JOptionPane.YES_OPTION : return true;
 		case JOptionPane.NO_OPTION : return false;
 		case JOptionPane.CLOSED_OPTION : System.exit(0);
 		return false;
 		}
 		return false;
 		
 	}
 
 	@Override
 	public int InputSingleWolfTarget(int inPlayer) {
 
 		List<String> LivePlayers = RunFileGame.getLivePlayers();
 		LivePlayers.remove(RunFileGame.getPlayerName(inPlayer));
 		List<Integer> TargetIDs = RunFileGame.getWolfPastTargets(inPlayer);
 		List<String> TargetNames = new ArrayList<String>();
 		for(Integer ID : TargetIDs){
 			TargetNames.add(RunFileGame.getPlayerName(ID));
 		}
 		LivePlayers.removeAll(TargetNames);
 		String[] arrplay = new String[LivePlayers.size()];
 		for (int i = 0; i < arrplay.length; i++) {
 			arrplay[i] = LivePlayers.get(i);
 		}
 
 		return getPlayerIDFromUser("\nPLEASE CHOOSE WHO IS WOLFED DOWN BY PLAYER " + inPlayer + " (" + RunFileGame.getPlayerName(inPlayer) + ")", arrplay);
 	}
 
 	@Override
 	public String inputName() {
 		return getUserInput("PLEASE ENTER A NAME OF PLAYER");
 	}
 
 	@Override
 	public void displayPlayerIDs(String[] inArray) {
 		String text = "";
 		
 		text = ("Assigned player names: \n");
 		for(int i = 0; i < players; i++){
 			text += ((i+1) + " - " + inArray[i] +"\n");
 		}
 		
 		displayString(text);
 	}
 
 	@Override
 	public String[] SetNames() {
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
 					displayError("FUCK OFF THAT NAMES RESERVED");
 				}				
 			}while(BadName);
 			Players[n] = Name;			
 		}
 		return Players;
 	}
 
 	@Override
 	public void displayHistory(List<PlayerAction> AllActions, List<PlayerAction> ReleventActions) {		
 		final String newline = "<br>";
 		String HistoryText = "<html>";
 		for(PlayerAction Action : ReleventActions){
 			HistoryText += Action.print() + newline;
 		}
 		HistoryText += "</html>";
 		
 		String CompleteText = "<html>";
 		for(PlayerAction Action : AllActions){
 			CompleteText += Action.print() + newline;
 		}
 		CompleteText += "</html>";
 		
 		final JDialog dialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);	
 		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		dialog.setLocationRelativeTo(null);
 		
 		JScrollPane ReleventTab = new JScrollPane(new JLabel(HistoryText));
 		JScrollPane CompleteTab = new JScrollPane(new JLabel(CompleteText));
 		JTabbedPane TabPane = new JTabbedPane();
 		TabPane.addTab("Relevent History", ReleventTab);
 		TabPane.addTab("Complete History", CompleteTab);
 		
 		dialog.add(TabPane);
 		dialog.pack();
 		dialog.setSize(new Dimension(600,300));
		dialog.setVisible(true);
 		dialog.setLocationRelativeTo(null);
 				
 	}
 	
 	public void displayString(String text){
 		JOptionPane.showMessageDialog(null,
 			    text,
 			    "Quantum Wolves",
 			    JOptionPane.INFORMATION_MESSAGE);
 	
 	}
 	
 	public void displayError(String message){
 		JOptionPane.showMessageDialog(null,
 			    message,
 			    "Quantum Wolves",
 			    JOptionPane.ERROR_MESSAGE);
 	}
 	
 	private void displayHelp(){
 		final String newline = "\n";
 		
 		String HelpText = "How to play Quantum Werewolves" + newline + newline 
 				+ "There are a minimum of 2 players, although around 7 is advisable." + newline 
 				+ "One of these players is a Seer, and a fraction are wolves." + newline
 				+ "The wolves form a pack, with a leader, second in command, etc." + newline
 				+ "Each night, the leading living wolf makes a kill, and the Seer has a vision." + newline
 				+ "The vision will either be a 'thumbs up' for innocent," + newline
 				+ "or a 'thumbs down' for a wolf." + newline
 				+ "Each day, the players vote and lynch someone, killing them." + newline + newline
 				+ "However, the quantum twist comes in the fact that at the start of the game" + newline
 				+ "everyone has the same chance as anyone else of being any particular character" + newline
 				+ "(wolf/Seer/villager), and it is everyone's actions which determine the" + newline
 				+ "character assigments as play proceeds." + newline + newline
 				+ "Characters are determined according to the following rules:" + newline
 				+ "- A wolf cannot attack another wolf" + newline
 				+ "- Every vision the true Seer had when alive is correct" + newline
 				+ "- When a player becomes 100% dead, their character is chosen based on" + newline
 				+ "       the probabilities at that time." + newline
 				+ "- Players are given choices based only on things they can still do." + newline
 				+ "- A Seer may choose to have a vision of anyone, even themselves, or nobody.";	
 		displayString(HelpText);
 		
 		HelpText = "Gameplay:" + newline + newline 
 				+ "Enter the number of players, wolves, and player names." + newline
 				+ "Then, tell everyone to close their eyes" + newline
 				+ "'Wake' each player in turn by name, and indicate their assigned number" + newline
 				+ "For each player choice prompt, 'wake' the player by number," + newline 
 				+ "       and tell them what choice they can make, but NOT their options." + newline
 				+ "After each player has pointed at their choice, enter it," + newline
 				+ "       and give them an appropriate thumbs up or down for visions," + newline
 				+ "       then tell them to sleep." + newline
 				+ "After the final choice, it is daytime, and all players may wake." + newline
 				+ "They must now use the probability table to vote and lynch by name, not number." + newline
 				+ "After lynching, the probabilities update, and then the night begins again." + newline
 				+ "Any dead players need not close their eyes, but may not contribute to decisions.";
 		displayString(HelpText);
 	}
 	
 	private void displayAbout(){
 		displayString("Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html \n" +
 				"this is a 'quantum' version of the classic werewoves/mafia game of asymmetric information.");
 	}
 
 }
