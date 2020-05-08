 package wolves;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 public class TextWolves implements WolvesUI {
 	
 	private int players;
 	private int wolves;
 	private int[] recentTargets;
 	
 	@Override
 	public int getNumPlayers() {
 		boolean p = true;
 		while (p) {
 			try {
 				this.players = Integer.parseInt(getUserInput("PLEASE CHOOSE HOW MANY OF PEOPLE"));
 				p = false;
 				return players;
 			} catch (Exception e) {
 				System.out.println("FUCK OFF THATS NOT A NUMBER");
 				p = true;
 			}
 		}
		return 0;
 	}
 	
 	@Override
 	public int getNumWolves() {
 		boolean p = true;
 		while (p) {
 			try {
 				this.wolves = Integer.parseInt(getUserInput("PLEASE CHOOSE HOW MANY OF WOLVES"));
 				p = false;
 				return wolves;
 			} catch (Exception e) {
 				System.out.println("FUCK OFF THATS NOT A NUMBER");
 				p = true;
 			}
 		}
		return 0;
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
 	public int[] inputWolfTargets() {
 		int[] targets = new int[players];
 		for (int i = 0; i < players; i++) {
 			boolean p = true;
 			while (p) {
 				try {
 					targets[i] = Integer.parseInt(getUserInput("PLEASE CHOOSE WHO IS WOLVED BY PLAYER " + i));
 					p = false;
 				} catch (Exception e) {
 					System.out.println("FUCK OFF THATS NOT A NUMBER");
 					p = true;
 				}
 			}
 		}
 		return targets;
 	}
 	
 	@Override
 	public int[] inputSeerTargets() {
 		int[] targets = new int[players];
 		for (int i = 0; i < players; i++) {
 			boolean p = true;
 			while (p) {
 				try {
 					targets[i] = Integer.parseInt(getUserInput("PLEASE CHOOSE WHO IS SEED BY PLAYER " + (i + 1)));
 					p = false;
 				} catch (Exception e) {
 					System.out.println("FUCK OFF THATS NOT A NUMBER");
 					p = true;
 				}
 			}
 		}
 		this.recentTargets = targets;
 		return targets;
 	}
 	
 	@Override
 	public void displayVisions(byte[] visions) {
 		for (int i = 0; i < players; i++) {
 			String role = null;
 			switch (visions[i]) {
 			case WolvesUI.VISION_INNO:
 				role = "VILLAGER";
 			case WolvesUI.VISION_WOLF:
 				role = "WHEREWOLF";
 			}
 			if (visions[i] != 0) {
 				System.out.println("PLAYER " + i + " SEES THAT PLAYER " + recentTargets[i] + " IS " + getAdjective() + " " + role);
 			}
 		}
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
 		boolean p = true;
 		while (p) {
 			try {
 				return Integer.parseInt(getUserInput("PLEASE CHOOSE WHO IS MADE OF LYNCHED"));
				//p = false;
 			} catch (Exception e) {
 				System.out.println("FUCK OFF THATS NOT A NUMBER");
 				p = true;
 			}
 		}
		return 0;
 	}
 	
 	@Override
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles) {
 		System.out.println("SORRY THIS ARENT ALIGNED");
 		System.out.println("PLAYER GOOD EVIL ALIVE DEAD");
 		for (int i = 0; i < players; i++) {
 			System.out.print(i + " ");
 			for (int j = 0; j < 4; i++) {
 				System.out.print(probabilities[i] + " ");
 			}
 			System.out.println();
 		}
 		System.out.println("KNOWN ROLLS");
 		for (int i = 0; i < players; i++) {
 			String role = null;
 			switch (knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 			case ROLE_SEER:
 				role = "SEER";
 			case ROLE_WOLF:
 				role = "WOLF";
 			}
 			if (knownRoles[i] != 0) {
 				System.out.println("PLAYER " + i + " IS " + getAdjective() + role);
 			}
 		}
 	}
 	
 }
