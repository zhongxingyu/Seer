 package edu.berkeley.gamesman.solver;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.TierGame;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.util.*;
 
 /**
  * @author DNSpies
  */
 public class TierSolver extends Solver {
 
 	public TierSolver(Configuration conf) {
 		super(conf);
 		maxMem = conf.getLong("gamesman.memory", 1 << 25);
 		numThreads = conf.getInteger("gamesman.threads", 1);
 		minSplit = conf.getInteger("gamesman.split", numThreads);
 		minSplitSize = conf.getInteger("gamesman.minimum.split", 1024);
 	}
 
 	protected static final double SAFETY_MARGIN = 2.0;
 
 	private int splits;
 
 	private int count;
 
 	protected final int numThreads;
 
 	private final int minSplit;
 
 	protected final long maxMem;
 
 	private long[] starts;
 
 	private File minSolvedFile = null;
 
 	boolean parallelSolving;
 
 	private long times[] = new long[7];
 
 	private final int minSplitSize;
 
 	protected void solvePartialTier(Configuration conf, long start,
 			long hashes, TierSolverUpdater t, DatabaseHandle readDh,
 			DatabaseHandle writeDh) {
 		final long firstNano;
 		long nano = 0;
 		final boolean debugSolver = Util.debug(DebugFacility.SOLVER);
 		if (debugSolver) {
 			for (int i = 0; i < 7; i++) {
 				times[i] = 0;
 			}
 			firstNano = System.nanoTime();
 			nano = firstNano;
 		} else
 			firstNano = 0;
 		TierGame game = (TierGame) conf.getGame();
 		long current = start;
 		long stepNum = current % STEP_SIZE;
 		TierState curState = game.hashToState(start);
 		game.setState(curState);
 		Record[] vals = new Record[game.maxChildren()];
 		for (int i = 0; i < vals.length; i++)
 			vals[i] = new Record(conf);
 		Record prim = new Record(conf);
 		TierState[] children = new TierState[game.maxChildren()];
 		for (int i = 0; i < children.length; i++)
 			children[i] = game.newState();
 		long lastNano;
 		if (debugSolver) {
 			lastNano = nano;
 			nano = System.nanoTime();
 			times[0] = nano - lastNano;
 		}
 		for (long count = 0L; count < hashes; count++) {
 			if (stepNum == STEP_SIZE) {
 				t.calculated(STEP_SIZE);
 				stepNum = 0;
 			}
 			Value pv = game.primitiveValue();
 			if (debugSolver) {
 				lastNano = nano;
 				nano = System.nanoTime();
 				times[1] += nano - lastNano;
 			}
 			if (pv == Value.UNDECIDED) {
 				int len = game.validMoves(children);
 				if (debugSolver) {
 					lastNano = nano;
 					nano = System.nanoTime();
 					times[2] += nano - lastNano;
 				}
 				for (int i = 0; i < len; i++) {
 					game.recordFromLong(children[i], readDb.getRecord(readDh,
 							game.stateToHash(children[i])), vals[i]);
 					vals[i].previousPosition();
 				}
 				if (debugSolver) {
 					lastNano = nano;
 					nano = System.nanoTime();
 					times[3] += nano - lastNano;
 				}
 				Record newVal = game.combine(vals, 0, len);
 				writeDb.putRecord(writeDh, current, game.getRecord(curState,
 						newVal));
 			} else if (pv == Value.IMPOSSIBLE) {
 				break;
 			} else {
 				if (debugSolver) {
 					lastNano = nano;
 					nano = System.nanoTime();
 					times[2] += nano - lastNano;
 					lastNano = nano;
 					nano = System.nanoTime();
 					times[3] += nano - lastNano;
 				}
 				prim.remoteness = 0;
 				prim.value = pv;
 				writeDb.putRecord(writeDh, current, game.getRecord(curState,
 						prim));
 			}
 			if (debugSolver) {
 				lastNano = nano;
 				nano = System.nanoTime();
 				times[4] += nano - lastNano;
 			}
 			if (count < hashes - 1) {
 				game.nextHashInTier();
 				curState.hash++;
 			}
 			++current;
 			++stepNum;
 			if (debugSolver) {
 				lastNano = nano;
 				nano = System.nanoTime();
 				times[5] += nano - lastNano;
 				lastNano = nano;
 				nano = System.nanoTime();
 				times[6] += nano - lastNano;
 			}
 		}
 		if (debugSolver) {
 			long sumTimes = nano - firstNano - times[6] * 6;
 			Util.debug(DebugFacility.SOLVER, "Initializing: " + 1000 * times[0]
 					/ sumTimes / 10D);
 			Util.debug(DebugFacility.SOLVER, "Primitive Value: " + 1000
 					* (times[1] - times[6]) / sumTimes / 10D);
 			Util.debug(DebugFacility.SOLVER, "Calculating Chilren: " + 1000
 					* (times[2] - times[6]) / sumTimes / 10D);
 			Util.debug(DebugFacility.SOLVER, "Reading Children: " + 1000
 					* (times[3] - times[6]) / sumTimes / 10D);
 			Util.debug(DebugFacility.SOLVER, "Storing records: " + 1000
 					* (times[4] - times[6]) / sumTimes / 10D);
 			Util.debug(DebugFacility.SOLVER, "Stepping: " + 1000
 					* (times[5] - times[6]) / sumTimes / 10D);
 		}
 	}
 
 	@Override
 	public WorkUnit prepareSolve(Configuration inconf) {
 		String msf = inconf.getProperty("gamesman.minSolvedFile", null);
 		if (msf == null)
 			tier = ((TierGame) inconf.getGame()).numberOfTiers();
 		else {
 			minSolvedFile = new File(msf);
 			if (minSolvedFile.exists()) {
 				try {
 					Scanner scan = new Scanner(minSolvedFile);
 					tier = scan.nextInt();
 					scan.close();
 				} catch (FileNotFoundException e) {
 					throw new Error("This should never happen", e);
 				}
 			} else {
 				tier = ((TierGame) inconf.getGame()).numberOfTiers();
 				try {
 					minSolvedFile.createNewFile();
 					FileWriter fw = new FileWriter(minSolvedFile);
 					fw.write(Integer.toString(tier));
 					fw.close();
 				} catch (IOException e) {
 					throw new Error(e);
 				}
 			}
 		}
 		updater = new TierSolverUpdater();
 		parallelSolving = false;
 		flusher.run();
 		needs2Reset = false;
 		return new TierSolverWorkUnit(inconf);
 	}
 
 	protected int nextIndex = 0;
 
 	protected TierSolverUpdater updater;
 
 	protected CyclicBarrier barr;
 
 	private final Runnable flusher = new Runnable() {
 		public void run() {
 			if (minSolvedFile != null) {
 				try {
 					FileWriter fw;
 					fw = new FileWriter(minSolvedFile);
 					fw.write(Integer.toString(tier));
 					fw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			--tier;
 			needs2Sync = false;
 			if (tier < 0) {
 				updater.complete();
 				if (minSolvedFile != null)
 					minSolvedFile.delete();
 			} else {
 				if (barr != null)
 					needs2Reset = true;
 				TierGame game = (TierGame) conf.getGame();
 				long fullStart = game.hashOffsetForTier(tier);
 				long fullSize = game.numHashesForTier(tier);
 				splits = Math.max(minSplit, numSplits(readDb == null ? 0
 						: readDb.requiredMem(game.hashOffsetForTier(tier + 1),
 								game.numHashesForTier(tier + 1)), writeDb
 						.requiredMem(fullStart, fullSize), maxMem));
 				starts = writeDb.splitRange(fullStart, fullSize, splits,
 						minSplitSize);
 			}
 		}
 	};
 
 	protected int tier;
 
 	private volatile boolean needs2Sync = false;
 
 	private volatile boolean needs2Reset = false;
 
 	private int numSplits(long lastTierSize, long thisTierSize, long maxMem) {
 		return (int) ((thisTierSize + lastTierSize
				* conf.getGame().maxChildren()) / maxMem);
 	}
 
 	protected Pair<Long, Long> nextSlice(Configuration conf) {
 		while (true) {
 			if (needs2Sync) {
 				if (parallelSolving) {
 					updater.complete();
 					return null;
 				}
 				if (barr == null)
 					flusher.run();
 				else {
 					assert Util.debug(DebugFacility.THREADING,
 							"Thread waiting to tier-sync");
 					try {
 						barr.await();
 						synchronized (this) {
 							if (needs2Reset) {
 								needs2Reset = false;
 								barr.reset();
 							}
 						}
 					} catch (InterruptedException e) {
 						throw new Error(
 								"TierSolver thread was interrupted while waiting!",
 								e);
 					} catch (BrokenBarrierException e) {
 						throw new Error("Barrier Broken", e);
 					}
 				}
 			}
 			synchronized (this) {
 				if (!needs2Sync) {
 					if (tier < 0) {
 						return null;
 					}
 					Pair<Long, Long> slice = new Pair<Long, Long>(
 							starts[count], starts[count + 1] - starts[count]);
 					assert Util.debug(DebugFacility.THREADING, "Solving "
 							+ (count + 1) + "/" + splits + " in tier " + tier
 							+ "; " + starts[count] + "-" + starts[count + 1]);
 					if (count < starts.length - 2) {
 						++count;
 					} else {
 						count = 0;
 						needs2Sync = true;
 					}
 					return slice;
 				}
 			}
 		}
 	}
 
 	private final class TierSolverWorkUnit implements WorkUnit {
 
 		private int index;
 
 		Configuration conf;
 
 		Pair<Long, Long> thisSlice;
 
 		TierSolverWorkUnit(Configuration conf) {
 			this.conf = conf;
 			this.index = nextIndex++;
 		}
 
 		public void conquer() {
 			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
 					+ index + ")");
 			Thread.currentThread().setName(
 					"Solver (" + index + "): " + conf.getGame().describe());
 			Pair<Long, Long> slice;
 			while ((slice = nextSlice(conf)) != null) {
 				thisSlice = slice;
 				DatabaseHandle myWrite = writeDb.getHandle();
 				DatabaseHandle readHandle;
 				if (readDb == null)
 					readHandle = null;
 				else
 					readHandle = readDb.getHandle();
 				solvePartialTier(conf, slice.car, slice.cdr, updater,
 						readHandle, myWrite);
 				writeDb.closeHandle(myWrite);
 			}
 			if (barr != null)
 				try {
 					barr.await();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (BrokenBarrierException e) {
 					e.printStackTrace();
 				}
 		}
 
 		public ArrayList<WorkUnit> divide(int num) {
 			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
 			arr.add(this);
 			for (int i = 1; i < num; i++)
 				arr.add(new TierSolverWorkUnit(conf.cloneAll()));
 			if (parallelSolving || numThreads == 1)
 				barr = null;
 			else
 				barr = new CyclicBarrier(numThreads, flusher);
 			return arr;
 		}
 
 		@Override
 		public String toString() {
 			String str = "WorkUnit " + index + "; slice is ";
 			if (thisSlice != null) {
 				str += "[" + thisSlice.car + "-" + thisSlice.cdr + "]";
 			} else {
 				str += "null";
 			}
 			return str;
 		}
 	}
 
 	final class TierSolverUpdater {
 
 		private long total = 0;
 
 		private Task t;
 
 		TierSolverUpdater() {
 			this(conf.getGame().numHashes());
 		}
 
 		TierSolverUpdater(long totalProgress) {
 			TierGame myGame = (TierGame) conf.getGame();
 			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
 			t.setTotal(totalProgress);
 		}
 
 		synchronized void calculated(int howMuch) {
 			total += howMuch;
 			if (t != null) {
 				t.setProgress(total);
 			}
 		}
 
 		public void complete() {
 			if (t != null)
 				t.complete();
 			t = null;
 		}
 	}
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 * @param tier
 	 *            The tier
 	 * @param startHash
 	 *            The first hash in the range to solve
 	 * @param numHashes
 	 *            The number of hashes to solve
 	 * @return A WorkUnit for solving solveSpace
 	 */
 	public WorkUnit prepareSolve(Configuration conf, int tier, long startHash,
 			long numHashes) {
 		this.tier = tier;
 		updater = new TierSolverUpdater(numHashes);
 		parallelSolving = true;
 		TierGame game = (TierGame) conf.getGame();
 		double tierFrac = 0D;
 		if (tier < game.numberOfTiers() - 1) {
 			tierFrac = ((double) game.numHashesForTier(tier + 1))
 					/ game.numHashesForTier(tier);
 		}
 		long writeRequired = writeDb.requiredMem(startHash, numHashes);
 		splits = Math.max(minSplit, numSplits(
 				tier == game.numberOfTiers() - 1 ? 0
 						: (long) (tierFrac * writeRequired), writeRequired,
 				maxMem));
 		starts = writeDb.splitRange(startHash, numHashes, splits, minSplitSize);
 		return new TierSolverWorkUnit(conf);
 	}
 }
