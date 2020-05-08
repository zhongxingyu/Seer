 package core;
 
 import java.io.PrintWriter;
 
 import ec.util.MersenneTwister;
 
 public class MooreMachine implements Cloneable {
 	private int startState;
 	private int significantMask;
 	private int[][] nextState;
 	private Turn[] moves;
 
 	public MooreMachine(int startState, int significantMask, int[][] nextState,
 			Turn[] moves) {
 		this.startState = startState;
 		this.significantMask = significantMask;
 		this.nextState = nextState;
 		this.moves = moves;
 	}
 
 	public MooreMachine() {
 		final MersenneTwister rand = Constants.rand;
 		final int states = Constants.STATES_NUMBER;
 		startState = rand.nextInt(states);
 		significantMask = 0;
 		final int vis = Constants.VISIBLE_CELLS;
 		for (int i = 0; i < Constants.SIGNIFICANT_INPUTS; ++i) {
 			int currentBit = rand.nextInt(vis);
 			while (((significantMask >> currentBit) & 1) == 1) {
 				currentBit = rand.nextInt(vis);
 			}
 			significantMask |= (1 << currentBit);
 		}
 		final int sign = Constants.SIGNIFICANT_INPUTS;
 		nextState = new int[states][1 << sign];
 		for (int i = 0; i < states; ++i) {
 			for (int j = 0; j < (1 << sign); ++j) {
 				nextState[i][j] = rand.nextInt(states);
 			}
 		}
 		moves = new Turn[states];
 		Turn[] values = Turn.values();
 		for (int i = 0; i < states; ++i) {
 			moves[i] = values[rand.nextInt(values.length)];
 		}
 	}
 
 	@Override
 	public MooreMachine clone() {
 		int[][] nextSt = new int[nextState.length][];
 		for (int i = 0; i < nextSt.length; ++i) {
 			nextSt[i] = new int[nextState[i].length];
 			for (int j = 0; j < nextSt[i].length; ++j) {
 				nextSt[i][j] = nextState[i][j];
 			}
 		}
 		Turn[] mv = new Turn[moves.length];
 		for (int i = 0; i < moves.length; ++i) {
 			mv[i] = moves[i];
 		}
 		return new MooreMachine(startState, significantMask, nextSt, mv);
 	}
 
 	public int getStartState() {
 		return startState;
 	}
 
 	public int getSignificantMask() {
 		return significantMask;
 	}
 
 	public Turn getMove(int state) {
 		return moves[state];
 	}
 
 	public int getNextState(int state, int mask) {
 		return nextState[state][mask];
 	}
 
 	private void nextStateMutation(double p) {
 		final MersenneTwister rand = Constants.rand;
 		final int states = Constants.STATES_NUMBER;
 		for (int i = 0; i < states; ++i) {
 			for (int j = 0; j < 1 << Constants.SIGNIFICANT_INPUTS; ++j) {
 				if (rand.nextDouble() < p) {
 					nextState[i][j] = rand.nextInt(states);
 				}
 			}
 		}
 	}
 
 	private void actionMutation(double p) {
 		final MersenneTwister rand = Constants.rand;
 		final Turn[] values = Turn.values();
 		for (int i = 0; i < Constants.STATES_NUMBER; ++i) {
 			if (rand.nextDouble() < p) {
 				moves[i] = values[rand.nextInt(values.length)];
 			}
 		}
 	}
 
 	private void significantPredicateMutation(double p) {
 		final MersenneTwister rand = Constants.rand;
 		if (rand.nextDouble() < p) {
 			significantMask = 0;
 			while (Integer.bitCount(significantMask) < Constants.SIGNIFICANT_INPUTS) {
				significantMask |= 1 << rand.nextInt(Constants.STATES_NUMBER);
 			}
 		}
 	}
 
 	private void startStateMutation(double p) {
 		final MersenneTwister rand = Constants.rand;
 		if (rand.nextDouble() < p) {
 			startState = rand.nextInt(Constants.STATES_NUMBER);
 		}
 	}
 
 	public void mutate(Mutation m, double p) {
 		switch (m) {
 		case NEXT_STATE_MUTATION:
 			nextStateMutation(p);
 			return;
 		case ACTION_MUTATION:
 			actionMutation(p);
 			return;
 		case SIGNIFICANT_PREDICATE_MUTATION:
 			significantPredicateMutation(p);
 			return;
 		case START_STATE_MUTATION:
 			startStateMutation(p);
 			return;
 		}
 	}
 
 	public static String getBitString(int mask, int length) {
 		char[] ans = new char[length];
 		for (int i = 0; i < length; ++i) {
 			ans[length - 1 - i] = (char) ('0' + ((mask >> i) & 1));
 		}
 		return new String(ans);
 	}
 
 	public void print(PrintWriter out) {
 		out.println("Start state = " + startState);
 		out.print("Significant inputs:");
 		final int vis = Constants.VISIBLE_CELLS;
 		for (int i = 0; i < vis; ++i) {
 			if (((significantMask >> i) & 1) == 1) {
 				out.print(" " + i);
 			}
 		}
 		out.println();
 		final int states = Constants.STATES_NUMBER;
 		out.print("Moves:");
 		for (int i = 0; i < states; ++i) {
 			out.print(" " + moves[i]);
 		}
 		out.println();
 		out.printf("%15s%15s%15s", "state", "inputs", "next state");
 		out.println();
 		final int sign = Constants.SIGNIFICANT_INPUTS;
 		for (int i = 0; i < states; ++i) {
 			for (int j = 0; j < (1 << sign); ++j) {
 				out.printf("%15d%15s%15d", i, getBitString(j,
 						Constants.SIGNIFICANT_INPUTS), nextState[i][j]);
 				out.println();
 			}
 		}
 	}
 }
