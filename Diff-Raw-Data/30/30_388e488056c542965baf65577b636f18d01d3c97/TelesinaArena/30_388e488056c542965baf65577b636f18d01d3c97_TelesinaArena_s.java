 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.juhokall.telesina.app;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.juhokall.telesina.ai.TelesinaAI;
 import com.juhokall.telesina.io.PlayableTelesinaGame;
 import com.juhokall.telesina.io.TelesinaHumanizer;
 import com.juhokall.telesina.model.Situation;
 import com.juhokall.telesina.model.Solution;
 import com.juhokall.telesina.model.SolutionType;
 import com.juhokall.telesina.model.TelesinaHand;
 import com.juhokall.telesina.model.core.Telesina;
 import java.util.Scanner;
 
 /**
  *
  * @author juho
  */
 public class TelesinaArena {
 
 	private static PlayableTelesinaGame game;
 	private static TelesinaHumanizer humanizer;
 	private static TelesinaAI ai;
 	private static Scanner scanner;
 	private final static int HERO = 0;
 	private final static int VILLAIN = 1;
 	private final static int PLAYER_COUNT = 2;
 	private final static int ANTE_SIZE = 2;
 	private static int villainsHoleCard;
 	private static int herosHoleCard;
 	private static Situation situation;
 	private static int street = 0;
 
 	public static void main(String[] args) {
 		initialize();
		game.dealNextStreet();
 		herosHoleCard = game.dealHoleCard(HERO);
 		takeAntes();
 		System.out.println("");
 		printStatus();
 		do {
 			if (situation.getStreet() > street) {
				game.dealNextStreet();
 				printStatus();
 			}
 			printLastAction();
 			if (situation.getActivePlayerId() == HERO) {
 				processHeroTurn();
 			} else {
 				processVillainTurn();
 			}
 		} while (situation.getPlayerCount() > 1 && situation.getStreet() <= Telesina.STREET_COUNT);
 		
 	}
 
 	private static void printStatus() {
 		printVillainHand();
 		printPlayersStack(VILLAIN);
 		printHeroHand(herosHoleCard);
 		printPlayersStack(HERO);
 		printPotSize();
 		System.out.println("");
 	}
 	private static void printLastAction() {
 		Solution lastSolution = situation.getLastSolution();
 		if(lastSolution.getSolutionType() == SolutionType.BET || lastSolution.getSolutionType() == SolutionType.RAISE || lastSolution.getSolutionType() == SolutionType.TAKE_ANTES) {
 			System.out.println(lastSolution.getSolutionSize() + "$ to call.");
 		}	
 		
 	}
 
 	private static void processHeroTurn() {
 		System.out.println("It is your turn");
 		String decision = scanner.nextLine();
 		while (!processDecision(decision)) {
 			decision = scanner.nextLine();
 		}
 	}
 
 	private static boolean processDecision(String decision) {
 		Solution solution;
 		if (decision.equals("c")) {
 			SolutionType lastSolution = situation.getLastSolution().getSolutionType();
 			if (lastSolution == SolutionType.CHECK) {
 				solution = new Solution(SolutionType.CHECK);
 			} else {
 				solution = new Solution(SolutionType.CALL, situation);
 			}
 		} else if (decision.startsWith("b")) {
 			solution = new Solution(SolutionType.BET, situation);
 		} else if (decision.startsWith("f")) {
 			solution = new Solution(SolutionType.FOLD);
 		} else {
 			return false;
 		}
 		situation = game.solveSituation(situation, solution);
 		printSolution(solution);
 		return true;
 	}
 
 	private static void takeAntes() {
 		situation = game.solveSituation(situation, new Solution(SolutionType.TAKE_ANTES, ANTE_SIZE));
 		for (int i = 0; i < PLAYER_COUNT; i++) {
 			System.out.println(ANTE_SIZE + "$ taken as ante.");
 		}
 	}
 
 	private static void printPotSize() {
 		System.out.println("Pot size: " + situation.getPotSize() + "$");
 	}
 
 	private static void processVillainTurn() {
 		System.out.println("It is opponent's turn.");
 		Solution solution = ai.getSolution(situation, villainsHoleCard);
 		situation = game.solveSituation(situation, solution);
 		printSolution(solution);
 	}
 
 	private static void printSolution(Solution solution) {
 		SolutionType solutionType = solution.getSolutionType();
 		String output = solutionType.name();
 		if (solutionType == SolutionType.BET || solutionType == SolutionType.RAISE) {
 			output += " " + solution.getSolutionSize() + "$";
 		}
 		System.out.println(output);
 	}
 
 	private static void printHeroHand(int holeCard) {
 		String holeCardHumanized = humanizer.humanizeCard(holeCard);
 		String output = "Your cards: (" + holeCardHumanized + ") ";
 		output += getPlayersHand(HERO);
 		System.out.println(output);
 	}
 
 	private static void printPlayersStack(int playerNumber) {
 		System.out.println("Stack: " + situation.getPlayer(playerNumber).getStack() + "$");
 	}
 
 	private static void printVillainHand() {
 		String output = "Opponent's cards: ( ) ";
 		output += getPlayersHand(VILLAIN);
 		System.out.println(output);
 
 	}
 
 	private static String getPlayersHand(int playerNumber) {
 		String output = "";
 		TelesinaHand hand = game.getPlayersHand(playerNumber);
 		for (int cardIndex = 0; cardIndex < hand.getNumberOfCardsDealt(); cardIndex++) {
 			String humanizedCard = humanizer.humanizeCard(hand.getCard(cardIndex));
 			output += humanizedCard + " ";
 		}
 		return output;
 
 	}
 
 	private static void initialize() {
 		Injector injector = Guice.createInjector();
 		game = injector.getInstance(PlayableTelesinaGame.class);
 		humanizer = injector.getInstance(TelesinaHumanizer.class);
 		scanner = new Scanner(System.in);
 		ai = injector.getInstance(TelesinaAI.class);
 		game.setNewGame(PLAYER_COUNT);
 		villainsHoleCard = game.dealHoleCard(VILLAIN);
 		situation = game.getSituation();
 	}
 }
