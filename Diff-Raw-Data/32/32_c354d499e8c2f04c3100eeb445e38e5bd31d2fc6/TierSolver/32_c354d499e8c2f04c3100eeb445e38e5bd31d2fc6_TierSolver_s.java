 package edu.berkeley.gamesman.solver;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.core.Solver;
 import edu.berkeley.gamesman.core.TieredGame;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Task;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * TierSolver documentation stub
  * 
  * @author Steven Schlansker
  * @param <T>
  *            The state type for the game
  */
 public class TierSolver<T> extends Solver {
 
 	protected TieredGame<T> myGame;
 
 	/**
 	 * The number of positions to go through between each update/reset
 	 */
 	private int tierSplit;
 
 	private int count;
 
 	@Override
 	public WorkUnit prepareSolve(Configuration inconf, Game<Object> game) {
 
 		myGame = Util.checkedCast(game);
 		tier = myGame.numberOfTiers() - 1;
 		updater = new TierSolverUpdater();
 
 		return new TierSolverWorkUnit(inconf);
 	}
 
 	protected void solvePartialTier(Configuration conf, long start, long end,
 			TierSolverUpdater t) {
 		long current = start - 1;
 		TieredGame<T> game = Util.checkedCast(conf.getGame());
 
 		while (current < end) {
 			current++;
 
 			if (current % STEP_SIZE == 0)
 				t.calculated(STEP_SIZE);
 
 			T state = game.hashToState(current);
 
 			PrimitiveValue pv = game.primitiveValue(state);
 
 			if (pv.equals(PrimitiveValue.UNDECIDED)) {
 				assert Util.debug(DebugFacility.SOLVER,
 						"Primitive value for state " + current
 								+ " is undecided");
 				Collection<Pair<String, T>> children = game.validMoves(state);
 				ArrayList<Record> vals = new ArrayList<Record>(children.size());
 				for (Pair<String, T> child : children) {
 					vals.add(db.getRecord(game.stateToHash(child.cdr)));
 				}
 				Record[] theVals = new Record[vals.size()];
 				Record newVal = game.combine(vals.toArray(theVals), 0,
 						theVals.length);
 				db.putRecord(current, newVal);
 			} else {
 				Record prim = game.newRecord();
 				prim.set(RecordFields.VALUE, pv.value());
 				assert Util.debug(DebugFacility.SOLVER,
 						"Primitive value for state " + current + " is " + prim);
 				db.putRecord(current, prim);
 			}
 		}
 		assert Util.debug(DebugFacility.THREADING,
 				"Reached end of partial tier at " + end);
 
 	}
 
 	protected int nextIndex = 0;
 
 	protected TierSolverUpdater updater;
 
 	protected CyclicBarrier barr;
 
 	private final Runnable flusher = new Runnable() {
 		public void run() {
 			db.flush();
 			needs2Sync = false;
 			if (tier == -1)
 				updater.complete();
 			else
 				needs2Reset = true;
 		}
 	};
 
 	protected int tier;
 
 	private boolean needs2Sync = false;
 
 	private boolean needs2Reset = false;
 
 	protected Pair<Long, Long> nextSlice(Configuration conf) {
 		while (true) {
 			if (needs2Sync) {
 				assert Util.debug(DebugFacility.THREADING,
 						"Thread waiting to tier-sync");
 				try {
 					if (barr != null) {
 						barr.await();
 						synchronized (this) {
 							if (needs2Reset) {
 								needs2Reset = false;
 								barr.reset();
 							}
 						}
 					}
 				} catch (InterruptedException e) {
 					Util.fatalError(
 							"TierSolver thread was interrupted while waiting!",
 							e);
 				} catch (BrokenBarrierException e) {
 					Util.fatalError("Barrier Broken", e);
 				}
 			}
 			synchronized (this) {
 				if (!needs2Sync) {
 					if (tier < 0)
 						return null;
 					long tierStart = myGame.hashOffsetForTier(tier);
 					long tierSize = myGame.lastHashValueForTier(tier) + 1
 							- tierStart;
 					int tierSplit = (int) Math.min(this.tierSplit, Math.max(
 							tierSize / conf.recordsPerGroup, 1));
 					long start = tierStart + tierSize * count / tierSplit;
 					long adjustStart = start - start % conf.recordsPerGroup;
 					long end = tierStart + tierSize * (count + 1) / tierSplit;
 					long adjustEnd = end - end % conf.recordsPerGroup;
 					if (count > 0)
 						start = adjustStart;
 					if (count < tierSplit - 1) {
 						end = adjustEnd;
 						++count;
 					} else {
 						count = 0;
 						tier--;
 						needs2Sync = true;
 					}
 					--end;
					return new Pair<Long, Long>(start, end);
 				}
 			}
 		}
 	}
 
 	private final class TierSolverWorkUnit implements WorkUnit {
 
 		private int index;
 
 		Configuration conf;
 
 		TierSolverWorkUnit(Configuration conf) {
 			this.conf = conf;
 			this.index = nextIndex++;
 		}
 
 		public void conquer() {
 			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
 					+ index + ")");
 			Thread.currentThread().setName(
 					"Solver (" + index + "): " + myGame.toString());

 			Pair<Long, Long> slice;
			int lastTier = tier;
 			while ((slice = nextSlice(conf)) != null) {
				assert Util.debug(DebugFacility.THREADING,
						"Beginning to solve slice " + slice + " at count "
								+ count + " for tier " + lastTier);
 				if (slice.car <= slice.cdr) {
 					solvePartialTier(conf, slice.car, slice.cdr, updater);
 				}
				lastTier = tier;
 			}
 
 			try {
 				barr.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (BrokenBarrierException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public List<WorkUnit> divide(int num) {
 			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
 			arr.add(this);
 			for (int i = 1; i < num; i++)
 				arr.add(new TierSolverWorkUnit(conf.cloneAll()));
 			barr = new CyclicBarrier(num, flusher);
 			return arr;
 		}
 	}
 
 	final class TierSolverUpdater {
 
 		private long total = 0;
 
 		private Task t;
 
 		TierSolverUpdater() {
 			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
 			t.setTotal(myGame.lastHashValueForTier(myGame.numberOfTiers() - 1));
 		}
 
 		synchronized void calculated(int howMuch) {
 			total += howMuch;
 			if (t != null) {
 				t.setProgress(total);
 			}
 		}
 
 		public synchronized void complete() {
 			if (t != null)
 				t.complete();
 			t = null;
 		}
 	}
 
 	@Override
 	public void initialize(Configuration conf) {
 		super.initialize(conf);
 		tierSplit = conf.getInteger("gamesman.tierSplit", conf.getInteger(
 				"gamesman.threads", 1));
 	}
 }
