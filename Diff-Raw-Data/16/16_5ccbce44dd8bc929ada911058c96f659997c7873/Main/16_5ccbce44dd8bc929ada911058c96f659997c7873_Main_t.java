 import java.io.*;
 import java.util.*;
 
 class Main {
 	public static void main (String[] args) throws Exception {
 		new Main();
 	}
 
 	PrintStream out;
 	Scanner in;
 	public Main () {
 		out = System.out;
 		in = new Scanner(System.in);
 
 		while (in.hasNextInt()) {
 			play(in.nextInt());
 		}
 	}
 
 	private void play (int goal) {
		stan(goal, 1);
 	}
 
 	private void constructResults (List<Integer> results, int numSteps, int goal, int p) {
 		if (p >= goal) {
 			results.add(numSteps);
 			return;
 		} else {
 			constructResults(results, numSteps+1, goal, p*2);
 			constructResults(results, numSteps+1, goal, p*9);
 		}
 	}
 
 	private void ollie (int goal, int p) {
 		if (p >= goal) {
 			out.println("Stan wins.");
			return;
 		}
 		List<Integer> resultsIfNine = new LinkedList<Integer>();
 		constructResults(resultsIfNine, 0, goal, p*9);
 		int winsIfNine = countWins(resultsIfNine);
 
 		List<Integer> resultsIfTwo = new LinkedList<Integer>();
 		constructResults(resultsIfTwo, 0, goal, p*2);
 		int winsIfTwo = countWins(resultsIfTwo);
 
 		if (winsIfNine >= winsIfTwo) {
 			ollie(goal, p*9);
 		} else {
 			ollie(goal, p*2);
 		}
 	}
 
 	private void stan (int goal, int p) {
 		if (p >= goal) {
 			out.println("Ollie wins.");
			return;
 		}
 		List<Integer> resultsIfNine = new LinkedList<Integer>();
 		constructResults(resultsIfNine, 0, goal, p*9);
 		int winsIfNine = countWins(resultsIfNine);
 
 		List<Integer> resultsIfTwo = new LinkedList<Integer>();
 		constructResults(resultsIfTwo, 0, goal, p*2);
 		int winsIfTwo = countWins(resultsIfTwo);
 
 		if (winsIfNine >= winsIfTwo) {
 			ollie(goal, p*9);
 		} else {
 			ollie(goal, p*2);
 		}
 	}
 
 	int countWins(List<Integer> results) {
 		int wins = 0;
 		for (int i : results) {
 			if (i % 2 != 0) {
 				wins++;
 			}
 		}
 		return wins;
 	}
 }
