 package edu.berkeley.gamesman.parallel;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
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
 		private static final long SLEEP_TIME = 300000L;
 		private final Thread myThread;
 		private final NodeRunnable myRunnable;
 
 		public TimeOutChecker(Thread t, NodeRunnable nr) {
 			myThread = t;
 			myRunnable = nr;
 		}
 
 		@Override
 		public void run() {
 			long lastPrinted = System.currentTimeMillis();
 			while (myThread.isAlive()) {
 				try {
 					Thread.sleep(SLEEP_TIME);
 					if (myThread.isAlive()) {
 						long nextPrinted = myRunnable.lastPrinted;
 						if (nextPrinted <= lastPrinted && myRunnable.et != null
 								&& !myRunnable.et.hadErrors) {
 							myRunnable.et
 									.error("Connection timed out after "
 											+ (System.currentTimeMillis() - myRunnable.lastPrinted)
 											/ 1000 + " seconds");
 							myRunnable.p.destroy();
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
 		private static final long SLEEP_TIME = 10000L;
 		public long lastPrinted;
 		private Process p;
 		private final String name;
 		public ErrorThread et;
 
 		public NodeRunnable(String name) {
 			this.name = name;
 		}
 
 		public void run() {
 			Pair<Long, Long> slice = getSlice();
 			while (slice != null) {
 				lastPrinted = System.currentTimeMillis();
 				String command = "ssh -q "
 						+ (user == null ? name : (user + "@" + name));
 				command += " java -cp " + path + "/bin ";
 				if (maxMem > Integer.MAX_VALUE) {
 					command += "-d64 ";
 				}
 				command += "-Xmx" + maxMem + " ";
 				command += TierSlave.class.getName() + " ";
 				command += jobFile + " " + tier;
 				boolean onFinish = false;
 				p = null;
 				et = new ErrorThread(null, name) {
 					@Override
 					public void error(String error) {
 						if (!hadErrors)
 							new Thread() {
 								public void run() {
 									try {
 										sleep(2000);
 									} catch (InterruptedException e) {
 										e.printStackTrace();
 									}
 									if (p != null)
 										p.destroy();
 								}
 							}.start();
 						super.error(error);
 					}
 				};
 				try {
 					p = Runtime.getRuntime().exec(command);
 					InputStream es = p.getErrorStream();
 					et.setStream(es);
 					et.start();
 					OutputStream os = p.getOutputStream();
 					InputStream is = p.getInputStream();
 					os.write(dbTrack.getHeader(slice.car, slice.cdr).toBytes());
 					os.flush();
 					PrintStream ps = new PrintStream(os, true);
 					Scanner scan = new Scanner(is);
 					String next = scan.next();
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
 						} else
 							System.out.println(name + ": " + next
 									+ scan.nextLine());
 						if (et.hadErrors) {
 							break;
 						}
 						next = scan.next();
 					}
 					if (et.hadErrors) {
 						sliceFailed(slice);
 						try {
 							Thread.sleep(SLEEP_TIME);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					} else {
 						lastPrinted = System.currentTimeMillis();
 						String filePath = scan.next();
 						long firstRecord = scan.nextLong();
 						long numRecords = scan.nextLong();
 						if (et.hadErrors) {
 							sliceFailed(slice);
 							try {
 								Thread.sleep(SLEEP_TIME);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						} else {
 							et = null;
 							onFinish = true;
 							finishDb(
 									(user == null ? name : (user + "@" + name))
 											+ ":" + path + ":" + filePath,
 									firstRecord, numRecords);
 						}
 					}
 				} catch (Throwable t) {
 					if (onFinish) {
 						t.printStackTrace();
 						System.exit(1);
 					} else if (!et.hadErrors) {
						et.error("local " + t.getStackTrace());
 						sliceFailed(slice);
 						try {
 							Thread.sleep(SLEEP_TIME);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				slice = getSlice();
 			}
 		}
 	}
 
 	private final Configuration conf;
 	private final NodeRunnable[] nodes;
 	private final String user;
 	private final String path;
 	private final String jobFile;
 	private final SplitDatabase dbTrack;
 	private final int numSplits;
 	private final long maxMem;
 	private final int minSplitSize;
 	private final LinkedList<Pair<Long, Long>> failedSlices = new LinkedList<Pair<Long, Long>>();
 	private final Semaphore semaphore;
 	private SplitDatabase prevTierDb;
 	private SplitDatabase curTierDb;
 	private int tier;
 	private int finishedSlices = 0;
 	private long[] divides;
 	private int sliceNum = 0;
 	private boolean released;
 	private File tmpFile;
 	private PrintStream tmpFileOs;
 	private File curTmpFile;
 	private PrintStream curTmpFileOs;
 
 	public TierMaster(Configuration conf, String jobFile, String[] nodeNames)
 			throws ClassNotFoundException, FileNotFoundException {
 		this.conf = conf;
 		user = conf.getProperty("gamesman.remote.user", null);
 		path = conf.getProperty("gamesman.remote.path");
 		if (!(jobFile.startsWith("/") || jobFile.startsWith(path)))
 			jobFile = path + "/" + jobFile;
 		numSplits = (int) (conf.getFloat("gamesman.parallel.multiple", 1) * nodeNames.length);
 		maxMem = (long) (conf.getLong("gamesman.memory", 100000000) * 1.2);
		minSplitSize = conf.getInteger("gamesman.parallel.minimum.split", Math
				.max(1 << 20, conf.getInteger("gamesman.minimum.split", 1024)));
 		this.jobFile = jobFile;
 		dbTrack = SplitDatabase.openSplitDatabase(conf, true, true);
 		String dbUri = conf.getProperty("gamesman.db.uri");
 		tmpFile = new File(dbUri + ".tmp");
 		curTmpFile = new File(dbUri + ".cur");
 		if (tmpFile.exists()) {
 			FileInputStream fis = new FileInputStream(tmpFile);
 			Scanner scan = new Scanner(fis);
 			if (scan.hasNext())
 				tier = scan.nextInt();
 			else
 				tier = ((TierGame) conf.getGame()).numberOfTiers();
 			curTierDb = SplitDatabase.openSplitDatabase(conf, true, false);
 			String dbType = null;
 			if (scan.hasNext())
 				dbType = scan.next();
 			while (scan.hasNext()) {
 				String uri = scan.next();
 				long firstRecord = scan.nextLong();
 				long numRecords = scan.nextLong();
 				curTierDb.addDb(dbType, uri, firstRecord, numRecords);
 				dbType = scan.next();
 				if (dbType.equals("end")) {
 					dbTrack.addDatabasesFirst(curTierDb);
 					if (prevTierDb != null)
 						prevTierDb.close();
 					prevTierDb = curTierDb;
 					curTierDb = SplitDatabase.openSplitDatabase(conf, true,
 							false);
 					if (scan.hasNext())
 						tier = scan.nextInt();
 					else
 						break;
 					dbType = scan.next();
 				}
 			}
 			scan.close();
 			if (curTmpFile.exists()) {
 				fis = new FileInputStream(curTmpFile);
 				scan = new Scanner(fis);
 				while (scan.hasNext()) {
 					dbType = scan.next();
 					String uri = scan.next();
 					long firstRecord = scan.nextLong();
 					long numRecords = scan.nextLong();
 					curTierDb.insertDb(dbType, uri, firstRecord, numRecords);
 					finishedSlices++;
 				}
 				scan.close();
 			}
 		} else {
 			tier = ((TierGame) conf.getGame()).numberOfTiers();
 			curTierDb = SplitDatabase.openSplitDatabase(conf, true, false);
 		}
 		tmpFileOs = new PrintStream(new FileOutputStream(tmpFile, true), true);
 		curTmpFileOs = new PrintStream(new FileOutputStream(curTmpFile, true),
 				true);
 		tier--;
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
 		curTmpFileOs.println(RemoteDatabase.class.getName() + " " + uri + " "
 				+ firstRecord + " " + numRecords);
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
 			while (sliceNum < divides.length - 1
 					&& curTierDb.containsDb(divides[sliceNum]))
 				sliceNum++;
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
		TierMaster tm = new TierMaster(conf, jobFile, nodes
				.toArray(new String[nodes.size()]));
 		tm.run();
 	}
 
 	public void run() {
 		TierGame g = (TierGame) conf.getGame();
 		Thread[] joinThreads = new Thread[nodes.length];
 		for (; tier >= 0; tier--) {
 			long start = g.hashOffsetForTier(tier);
 			long length = g.numHashesForTier(tier);
 			divides = curTierDb.splitRange(start, length, numSplits,
 					minSplitSize);
 			sliceNum = 0;
 			while (sliceNum < divides.length - 1
 					&& curTierDb.containsDb(divides[sliceNum]))
 				sliceNum++;
 			released = false;
 			semaphore.release(divides.length - 1 - finishedSlices);
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
 			finishedSlices = 0;
 			tmpFileOs.println(tier);
 			tmpFileOs.println(curTierDb.makeStream(start, length));
 			curTierDb = SplitDatabase.openSplitDatabase(conf, true, false);
 			curTmpFileOs.close();
 			try {
 				curTmpFileOs = new PrintStream(new FileOutputStream(curTmpFile,
 						false), true);
 			} catch (FileNotFoundException e) {
 				throw new Error(e);
 			}
 		}
 		if (prevTierDb != null)
 			prevTierDb.close();
 		dbTrack.close();
 		tmpFile.delete();
 		curTmpFile.delete();
 	}
 }
