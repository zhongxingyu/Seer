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
  * @param <T> The state type for the game
  */
 public class TierSolver<T> extends Solver {
 
 	protected TieredGame<T> myGame;
 
 	protected Configuration conf;
 
 	/**
 	 * The number of positions to go through between each update/reset
 	 */
	public static final int STEP_SIZE = 10000;
 
 	@Override
 	public WorkUnit prepareSolve(Configuration inconf, Game<Object> game) {
 
 		myGame = Util.checkedCast(game);
 		tier = myGame.numberOfTiers() - 1;
 		offset = myGame.hashOffsetForTier(tier);
 		updater = new TierSolverUpdater();
 		conf = inconf;
 
 		return new TierSolverWorkUnit();
 	}
 
 	protected void solvePartialTier(TieredGame<T> game, long start, long end,
 			TierSolverUpdater t) {
 		long current = start - 1;
 
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
 
 				Record newVal = game.combine(conf, vals);
 				db.putRecord(current, newVal);
 			} else {
 				Record prim = new Record(conf, pv);
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
 
 	protected long offset;
 
 	protected CyclicBarrier barr;
 
 	protected int tier;
 
 	private boolean needs2Sync;
 
 	protected Pair<Long, Long> nextSlice() {
 		if (needs2Sync) {
 			assert Util.debug(DebugFacility.THREADING,
 					"Thread waiting to tier-sync");
 			try {
 				if (barr != null)
 					barr.await();
 			} catch (InterruptedException e) {
 				Util.fatalError(
 						"TierSolver thread was interrupted while waiting!", e);
 			} catch (BrokenBarrierException e) {
 				Thread.yield();
 				return nextSlice(); // Retry
 			}
 		}
 
 		synchronized (this) {
 			if (tier < 0)
 				return null;
 			final long step = STEP_SIZE;
 			long ret = offset, end;
 			offset += step;
 			end = ret + step;
 			if (end >= myGame.lastHashValueForTier(tier)) {
 				end = myGame.lastHashValueForTier(tier);
 				tier--;
 				if (tier >= 0)
 					offset = myGame.hashOffsetForTier(tier);
 				needs2Sync = true;
 			}
 			return new Pair<Long, Long>(ret, end);
 		}
 	}
 
 	private final class TierSolverWorkUnit implements WorkUnit {
 
 		private int index;
 
 		TierSolverWorkUnit() {
 
 			// if(!(g instanceof TieredGame))
 			// Util.fatalError("Attempted to use tiered solver on non-tiered
 			// game");
 			this.index = nextIndex++;
 			barr = new CyclicBarrier(nextIndex);
 		}
 
 		@SuppressWarnings("synthetic-access")
 		public void conquer() {
 			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
 					+ index + ")");
 			Thread.currentThread().setName(
 					"Solver (" + index + "): " + myGame.toString());
 
 			Pair<Long, Long> slice;
 			while ((slice = nextSlice()) != null) {
 				assert Util.debug(DebugFacility.THREADING,
 						"Beginning to solve slice " + slice + " in thread "
 								+ index);
 				solvePartialTier(myGame, slice.car, slice.cdr, updater);
 				db.flush();
 			}
 
 			synchronized (TierSolver.this) {
 				if (barr != null) {
 					updater.complete();
 					barr.reset();
 					barr = null;
 				}
 			}
 		}
 
 		public List<WorkUnit> divide(int num) {
 			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
 			arr.add(this);
 			for (int i = 1; i < num; i++)
 				arr.add(new TierSolverWorkUnit());
 			return arr;
 		}
 	}
 
 	final class TierSolverUpdater {
 
 		private long total = 0;
 
 		private Task t;
 
		private long lastUpdate = 0;

 		TierSolverUpdater() {
 			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
 			t.setTotal(myGame.lastHashValueForTier(myGame.numberOfTiers() - 1));
 		}
 
 		synchronized void calculated(int howMuch) {
 			total += howMuch;
			if (t != null
					&& System.currentTimeMillis() - lastUpdate > STEP_SIZE) {
 				t.setProgress(total);
				lastUpdate = System.currentTimeMillis();
 			}
 		}
 
 		public synchronized void complete() {
 			if (t != null)
 				t.complete();
 			t = null;
 		}
 	}
 }
