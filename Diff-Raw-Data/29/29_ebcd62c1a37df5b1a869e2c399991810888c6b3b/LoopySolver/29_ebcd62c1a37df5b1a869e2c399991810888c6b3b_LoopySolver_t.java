 package edu.berkeley.gamesman.solver;
 
 import java.util.List;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.Game;
 import edu.berkeley.gamesman.game.LoopyMutaGame;
 import edu.berkeley.gamesman.game.LoopyGameWrapper;
 import edu.berkeley.gamesman.util.qll.Pool;
 import edu.berkeley.gamesman.util.qll.Factory;
 import edu.berkeley.gamesman.util.qll.RecycleLinkedList;
 
 /**
  * @author David, Brent, Nancy, Kevin, Peter, Sharmishtha, Raji
  * 
  */
 public class LoopySolver extends Solver {
 	Pool<Record> recordPool;
 
 	protected RecycleLinkedList<Record[]> recordList; // Nancy: added this for
 														// else clause in solve
 														// loopy game function
 
 	public LoopySolver(Configuration conf) {
 		super(conf);
 	}
 
 	@Override
 	public WorkUnit prepareSolve(final Configuration conf) {
 		Game<?> g = conf.getGame();
 		final LoopyMutaGame game;
 		if (g instanceof LoopyMutaGame) {
 			game = (LoopyMutaGame) g;
 		} else {
 			game = wrapGame(conf, g);
 		}
 		recordPool = new Pool<Record>(new Factory<Record>() {
 
 			public Record newObject() {
 				return game.newRecord();
 			}
 
 			public void reset(Record t) {
 				t.value = Value.UNDECIDED;
 			}
 
 		});
 		long hashSpace = game.numHashes();
 		Record defaultRecord = game.newRecord();
 		defaultRecord.value = Value.IMPOSSIBLE;
 		writeDb.fill(conf.getGame().recordToLong(null, defaultRecord), 0,
 				hashSpace);
 
 		return new WorkUnit() {
 
 			public void conquer() {
 				solve(conf);
 			}
 
 			public List<WorkUnit> divide(int num) {
 				throw new UnsupportedOperationException();
 			}
 
 		};
 	}
 
 	private <S extends State> LoopyMutaGame wrapGame(Configuration conf,
 			Game<S> g) {
 		return new LoopyGameWrapper<S>(conf, g);
 	}
 
 	public void solve(Configuration conf) {
 		Game<?> g = conf.getGame();
 		LoopyMutaGame game;
 		if (g instanceof LoopyMutaGame) {
 			game = (LoopyMutaGame) g;
 		} else {
 			game = wrapGame(conf, g);
 		}
 		for (int startNum = 0; startNum < game.numStartingPositions(); startNum++) {
 			game.setStartingPosition(startNum);
 			solve(game, game.newRecord(), 0, readDb.getHandle(),
 					writeDb.getHandle());
 		}
 	}
 
 	/**
 	 * solve(): Solves the loopy game. Everything starts as impossible.
 	 * 
 	 * @param game
 	 * @param value
 	 * @param depth
 	 * @param readDh
 	 * @param writeDh
 	 */
 	private void solve(LoopyMutaGame game, Record value, int depth,
 			DatabaseHandle readDh, DatabaseHandle writeDh) {
 		long hash = game.getHash();
 		game.longToRecord(readDb.getRecord(readDh, hash), value);
		//System.out.println(value);
 		Record bestValue;
		System.out.println(game.displayState());
		System.out.println(value.value);
 		if (value.value == Value.IMPOSSIBLE) { // position not seen before
 			value.value = game.primitiveValue();
			System.out.println(value.value);
 			if (value.value != Value.UNDECIDED) { // if position is primitive
 				value.remoteness = 0;
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				// save value to database
 				value.previousPosition();
 				int numParents = game.unmakeMove();
 				// go to parents to update their values
 				for (int parent = 0; parent < numParents; parent++) {
 					fix(game, value, readDh, writeDh);
 					game.changeUnmakeMove();
 				}
 				if (numParents > 0) {
 					game.remakeMove();
 				}
 				value.nextPosition();
 			} else {
 				value.value = Value.DRAW; // treat current position as draw
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				bestValue = recordPool.get();
 				boolean assigned = false;
 				int numChildren = game.makeMove();
 				for (int child = 0; child < numChildren; child++) {
					System.out.println("Going to solve child " + child);
 					solve(game, value, depth + 1, readDh, writeDh);
 					value.previousPosition();
					if (!assigned || (value.compareTo(bestValue) > 0)) {
 						if (value.value.compareTo(Value.DRAW) > 0)
 							writeDb.putRecord(writeDh, hash,
 									game.recordToLong(value));
 						bestValue.set(value);
 					}
 					assigned = true;
 					game.changeMove();
 				}
 				if (numChildren > 0)
 					game.undoMove();
 				value.set(bestValue);
 				recordPool.release(bestValue);
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				value.previousPosition();
 				int numParents = game.unmakeMove();
 				for (int parent = 0; parent < numParents; parent++) {
 					fix(game, value, readDh, writeDh);
 					game.changeUnmakeMove();
 				}
 				if (numParents > 0) {
 					game.remakeMove();
 				}
 				value.nextPosition();
 			}
 		}
 	}
 
 	/**
 	 * fix(): Updates value of parent given child value. If value of child is
 	 * draw go up through parents to update values if database was changed.
 	 * 
 	 * @param game
 	 * @param value
 	 * @param readDh
 	 * @param writeDh
 	 */
 	private void fix(LoopyMutaGame game, Record value, DatabaseHandle readDh,
 			DatabaseHandle writeDh) {
 		Record dbValue = recordPool.get();
 		long hash = game.getHash();
 		game.longToRecord(readDb.getRecord(readDh, hash), dbValue);
		if ((dbValue.value != Value.IMPOSSIBLE) && game.primitiveValue() == Value.UNDECIDED) {
			System.out.println("GOING TO FIX NOW");
			System.out.println(game.displayState());
			System.out.println("Old value is " + dbValue);
			System.out.println("New value is " + value);
 			if (value.compareTo(dbValue) > 0) {
				System.out.println("USE NEW VALUE");
 				writeDb.putRecord(writeDh, hash, game.recordToLong(value));
 				// save value
 				value.previousPosition();
 				int numParents = game.unmakeMove();
 				for (int parent = 0; parent < numParents; parent++) {
 					fix(game, value, readDh, writeDh);
 					game.changeUnmakeMove();
 				}
 				if (numParents > 0) {
 					game.remakeMove();
 				}
 				value.nextPosition();
 			} else if (dbValue.value == Value.DRAW) {
				System.out.println("TEST CHILDREN TO SEE IF LOOP");
 				// if value is draw, test for all children have returned
 				boolean unassigned = true;
 				int numChildren = game.makeMove();
 				Record bestValue = recordPool.get();
 				Record childValue = recordPool.get();
 				int child;
 				for (child = 0; child < numChildren; child++) {
 					game.longToRecord(readDb.getRecord(readDh, game.getHash()),
 							childValue);
 					if (childValue.value == Value.UNDECIDED)
 						throw new Error("No undecided in loopy solver");
 					childValue.previousPosition();
 					if (childValue.value == Value.DRAW
 							|| childValue.value == Value.IMPOSSIBLE) {
						System.out.println("CHILD " + child + " is " + childValue + " so break");
 						break;
 					} else if (childValue.value.compareTo(Value.DRAW) > 0) {
 						throw new Error(
								"childValue should not be > DRAW if parent value is DRAW, child value is " + childValue.value);
 					} else if (unassigned
 							|| childValue.compareTo(bestValue) > 0) {
						System.out.println("CHILD " + child + " is " + childValue + " so now assigned");
 						bestValue.set(childValue);
 						unassigned = false;
 					}
 					game.changeMove();
 				}
 				if (numChildren > 0)
 					game.undoMove();
 				recordPool.release(childValue);
 				if (child == numChildren) {
 					writeDb.putRecord(writeDh, hash,
 							game.recordToLong(bestValue));
 					bestValue.previousPosition();
 					int numParents = game.unmakeMove();
 					for (int parent = 0; parent < numParents; parent++) {
 						fix(game, bestValue, readDh, writeDh);
 						game.changeUnmakeMove();
 					}
 					if (numParents > 0) {
 						game.remakeMove();
 					}
 					bestValue.nextPosition();
 					// Not really necessary, but if anything is ever changed,
 					// this could cause a bug which would be a nightmare to find
 				}
 				recordPool.release(bestValue);
 			}
 		}
 		recordPool.release(dbValue);
 	}
 }
