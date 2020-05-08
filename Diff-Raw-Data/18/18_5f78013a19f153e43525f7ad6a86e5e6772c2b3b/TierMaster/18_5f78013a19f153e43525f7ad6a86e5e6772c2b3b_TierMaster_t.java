 package edu.berkeley.gamesman.parallel;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.Semaphore;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.database.RemoteDatabase;
 import edu.berkeley.gamesman.database.SplitDatabase;
 import edu.berkeley.gamesman.game.TierGame;
 import edu.berkeley.gamesman.util.ErrorThread;
 import edu.berkeley.gamesman.util.Pair;
 
 public class TierMaster implements Runnable {
 	private class TimeOutChecker extends Thread {
		private static final long WAIT_TIME = 20000L;
 		private final Thread myThread;
 		private final NodeRunnable myRunnable;
 
 		public TimeOutChecker(Thread t, NodeRunnable nr) {
 			myThread = t;
 			myRunnable = nr;
 		}
 
 		@Override
 		public synchronized void run() {
 			long lastPrinted = System.currentTimeMillis();
 			while (myThread.isAlive()) {
 				try {
 					wait(WAIT_TIME);
 					if (myThread.isAlive()) {
 						long nextPrinted = myRunnable.lastPrinted;
 						if (nextPrinted <= lastPrinted && myRunnable.et != null
 								&& !myRunnable.et.hadErrors) {
 							myRunnable.et
 									.error("Connection timed out after "
 											+ (System.currentTimeMillis() - myRunnable.lastPrinted)
 											/ 1000 + " seconds");
 						} else
 							lastPrinted = nextPrinted;
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private class NodeRunnable implements Runnable {
 		private static final long WAIT_TIME = 10000L;
 		public long lastPrinted;
 		private final String name;
 		public ErrorThread et;
 
 		public NodeRunnable(String name) {
 			this.name = name;
 		}
 
 		public synchronized void run() {
 			try {
 				Pair<Long, Long> slice = getSlice();
 				while (slice != null) {
 					lastPrinted = System.currentTimeMillis();
 					String command = "ssh "
 							+ (user == null ? name : (user + "@" + name));
 					command += " java -cp " + path + "/bin "
 							+ TierSlave.class.getName() + " ";
 					command += slaveJobFile + " " + tier;
 					final Process p = Runtime.getRuntime().exec(command);
 					InputStream es = p.getErrorStream();
 					et = new ErrorThread(es, name) {
 						@Override
 						public void error(String error) {
 							super.error(error);
 							p.destroy();
 						}
 					};
 					et.start();
 					OutputStream os = p.getOutputStream();
 					InputStream is = p.getInputStream();
 					os.write(dbTrack.getHeader(slice.car, slice.cdr).toBytes());
 					os.flush();
 					PrintStream ps = new PrintStream(os);
 					Scanner scan = new Scanner(is);
 					String next = scan.next();
 					try {
 						while (!next.equals("finished:")) {
 							if (et.hadErrors) {
 								break;
 							}
 							lastPrinted = System.currentTimeMillis();
 							if (next.equals("fetch:")) {
 								long firstRecord = scan.nextLong();
 								long numRecords = scan.nextLong();
 								ps.println(prevTierDb.makeStream(firstRecord,
 										numRecords));
 								ps.flush();
 							} else
 								System.out.println(name + ": " + next + " "
 										+ scan.nextLine());
 							if (et.hadErrors) {
 								break;
 							}
 							next = scan.next();
 						}
 					} catch (Exception e) {
 						if (!et.hadErrors)
 							et.error("local " + e.getStackTrace());
 					}
 					if (et.hadErrors) {
 						try {
 							wait(WAIT_TIME);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						sliceFailed(slice);
 					} else {
 						lastPrinted = System.currentTimeMillis();
 						String filePath = scan.next();
 						long firstRecord = scan.nextLong();
 						long numRecords = scan.nextLong();
 						if (et.hadErrors) {
 							try {
 								wait(WAIT_TIME);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							sliceFailed(slice);
 						} else {
 							et = null;
 							finishDb(
 									(user == null ? name : (user + "@" + name))
 											+ ":" + path + ":" + filePath,
 									firstRecord, numRecords);
 						}
 					}
 					slice = getSlice();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private final Configuration conf;
 	private final NodeRunnable[] nodes;
 	private final String user;
 	private final String path;
 	private final String slaveJobFile;
 	private final SplitDatabase dbTrack;
 	private final int numSplits;
 	private final LinkedList<Pair<Long, Long>> failedSlices = new LinkedList<Pair<Long, Long>>();
 	private final Semaphore semaphore;
 	private SplitDatabase prevTierDb;
 	private SplitDatabase curTierDb;
 	private int tier;
 	private int finishedSlices;
 	private long[] divides;
 	private int sliceNum = 0;
	private boolean released;
 
 	public TierMaster(Configuration conf, String[] nodeNames)
 			throws ClassNotFoundException {
 		this.conf = conf;
 		user = conf.getProperty("gamesman.remote.user", null);
 		path = conf.getProperty("gamesman.remote.path");
 		String slaveJobFile = conf.getProperty("gamesman.parallel.slave.job");
 		if (!(slaveJobFile.startsWith("/") || slaveJobFile.startsWith(path)))
 			slaveJobFile = path + "/" + slaveJobFile;
 		numSplits = (int) (conf.getFloat("gamesman.parallel.multiple", 1) * nodeNames.length);
 		this.slaveJobFile = slaveJobFile;
 		dbTrack = new SplitDatabase(conf, true);
 		nodes = new NodeRunnable[nodeNames.length];
 		for (int i = 0; i < nodeNames.length; i++) {
 			nodes[i] = new NodeRunnable(nodeNames[i]);
 		}
 		semaphore = new Semaphore(0);
 	}
 
 	private synchronized void finishDb(String uri, long firstRecord,
 			long numRecords) {
 		curTierDb.insertDb(RemoteDatabase.class.getName(), uri, firstRecord,
 				numRecords);
 		finishedSlices++;
 	}
 
 	private Pair<Long, Long> getSlice() {
 		if (finishedSlices >= divides.length - 1) {
			synchronized (this) {
				if (!released) {
					released = true;
					semaphore.release(nodes.length - 1);
				} else
					semaphore.acquireUninterruptibly();
				return null;
			}
 		}
 		semaphore.acquireUninterruptibly();
 		if (finishedSlices >= divides.length - 1)
 			return null;
 		synchronized (this) {
 			if (!failedSlices.isEmpty())
 				return failedSlices.removeFirst();
 			long firstRecord = divides[sliceNum++];
 			long numRecords = divides[sliceNum] - firstRecord;
 			return new Pair<Long, Long>(firstRecord, numRecords);
 		}
 	}
 
 	private synchronized void sliceFailed(Pair<Long, Long> slice) {
 		failedSlices.addLast(slice);
 		semaphore.release();
 	}
 
 	public static void main(String[] args) throws ClassNotFoundException,
 			FileNotFoundException {
 		String jobFile = args[0];
 		String nodeFile = args[1];
 		Configuration conf = new Configuration(jobFile);
 		ArrayList<String> nodes = new ArrayList<String>();
 		Scanner scan = new Scanner(new File(nodeFile));
 		while (scan.hasNext())
 			nodes.add(scan.next());
 		scan.close();
 		TierMaster tm = new TierMaster(conf, nodes.toArray(new String[nodes
 				.size()]));
 		tm.run();
 	}
 
 	public void run() {
 		TierGame g = (TierGame) conf.getGame();
 		Thread[] joinThreads = new Thread[nodes.length];
 		for (tier = g.numberOfTiers() - 1; tier >= 0; tier--) {
 			long start = g.hashOffsetForTier(tier);
 			long length = g.numHashesForTier(tier);
 			curTierDb = new SplitDatabase(conf, start, length, false);
 			divides = curTierDb.splitRange(start, length, numSplits);
 			finishedSlices = 0;
			released = false;
 			semaphore.release(divides.length - 1);
 			sliceNum = 0;
 			int i = 0;
 			for (NodeRunnable nr : nodes) {
 				Thread t = new Thread(nr);
 				joinThreads[i++] = t;
 				TimeOutChecker toc = new TimeOutChecker(t, nr);
 				toc.setDaemon(true);
 				t.start();
 				toc.start();
 			}
 			for (Thread t : joinThreads) {
 				while (t.isAlive())
 					try {
 						t.join();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 			}
 			dbTrack.addDatabasesFirst(curTierDb);
 			if (prevTierDb != null)
 				prevTierDb.close();
 			prevTierDb = curTierDb;
 		}
 		if (prevTierDb != null)
 			prevTierDb.close();
 		dbTrack.close();
 	}
 }
