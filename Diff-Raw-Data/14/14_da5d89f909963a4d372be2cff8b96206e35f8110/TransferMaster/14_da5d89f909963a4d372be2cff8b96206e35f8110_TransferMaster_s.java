 package edu.berkeley.gamesman.parallel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.Semaphore;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.database.GZippedFileDatabase;
 import edu.berkeley.gamesman.database.SplitDatabase;
 import edu.berkeley.gamesman.util.ChunkInputStream;
 import edu.berkeley.gamesman.util.ErrorThread;
 import edu.berkeley.gamesman.util.UndeterminedChunkInputStream;
 
 public final class TransferMaster implements Runnable {
 
 	private final HashMap<String, LinkedList<TransferProcess>> processMap = new HashMap<String, LinkedList<TransferProcess>>();
 	private final SplitDatabase readFrom;
 	private final Configuration conf;
 	private final SplitDatabase writeTo;
 	private final int numSplits;
 	private final String parentFile;
 	private final int entrySize;
 	private GZippedFileDatabase db;
 
 	private TransferMaster(String oldUri, String newUri, int numSplits,
 			String parentFile) {
 		readFrom = SplitDatabase.openSplitDatabase(oldUri);
 		conf = readFrom.getConfiguration();
 		writeTo = SplitDatabase.openSplitDatabase(newUri, conf, true, true);
 		this.numSplits = numSplits;
 		this.parentFile = parentFile;
 		entrySize = conf.getInteger("gamesman.db.zip.entryKB", 64) << 10;
 	}
 
 	private class TransferProcess {
 		private final String user;
 		private final String host;
 		private final String path;
 		private final String file;
 		private final long firstRecord;
 		private final long numRecords;
 		public LinkedList<TransferProcess> nextProcess;
 		private final Semaphore ready = new Semaphore(-1);
 		private boolean skip = false;
 		private Process p = null;
 		private PrintStream ps;
 		private InputStream in;
 		private ErrorThread et = null;
		private boolean prepareCalled = false;
 
 		public TransferProcess(String uri, long firstRecord, long numRecords) {
 			String[] hostFile = uri.split(":");
 			String host = hostFile[0];
 			path = hostFile[1];
 			String file = hostFile[2];
 			if (!file.startsWith("/") && !file.startsWith(path))
 				file = path + "/" + file;
 			String user = null;
 			if (host.contains("@")) {
 				String[] userHost = host.split("@");
 				user = userHost[0];
 				host = userHost[1];
 			}
 			this.host = host;
 			this.user = user;
 			this.file = file;
 			this.firstRecord = firstRecord;
 			this.numRecords = numRecords;
 		}
 
		public void prepare() {
			if (prepareCalled)
				throw new Error("Called twice");
			prepareCalled = true;
 			String command = "ssh "
 					+ (user == null ? host : (user + "@" + host));
 			command += " java -cp " + path + File.separator + "bin ";
 			command += TransferSlave.class.getName() + " " + file + " "
 					+ firstRecord + " " + numRecords;
 			do {
 				System.out.println(command);
 				try {
 					if (p != null) {
 						try {
 							wait(2000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						p.destroy();
 					}
 					p = Runtime.getRuntime().exec(command);
 					et = new ErrorThread(p.getErrorStream(), host);
 					et.start();
 					ps = new PrintStream(p.getOutputStream(), true);
 					in = p.getInputStream();
 					UndeterminedChunkInputStream ucis = new UndeterminedChunkInputStream(
 							in);
 					Scanner readScan = new Scanner(ucis);
 					String neededChunk = readScan.next();
 					while (!(neededChunk.equals("ready") || neededChunk
 							.equals("skip"))) {
 						if (neededChunk.equals("fetch:")) {
 							long firstRequestedRecord = readScan.nextLong();
 							long numRequestedRecords = readScan.nextLong();
 							ps.println(readFrom.makeStream(
 									firstRequestedRecord, numRequestedRecords));
 						} else
 							System.out.println(host + ": " + neededChunk
 									+ readScan.nextLine());
 						neededChunk = readScan.next();
 					}
 					System.out.println(host + ": " + neededChunk);
 					ucis.finish();
 					if (neededChunk.equals("skip"))
 						skip = true;
 					ready.release();
 				} catch (Throwable t) {
					throw new Error(t);
				}
				if (et.hadErrors) {
					System.err.println("Had errors");
 				}
 			} while (et.hadErrors);
 		}
 
 		public void go() {
 			ready.acquireUninterruptibly();
 			if (skip)
 				return;
 			ps.println("go");
 			try {
 				ChunkInputStream cis = new ChunkInputStream(in);
 				db.putZippedBytes(cis);
 			} catch (IOException e) {
 				et.error("local io error");
 				e.printStackTrace();
 			}
 			if (et.hadErrors)
 				System.exit(1);
 		}
 
 		public void setNextProcess(LinkedList<TransferProcess> lp) {
 			nextProcess = lp;
 			ready.release();
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		TransferMaster tm = new TransferMaster(args[0], args[1], Integer
 				.parseInt(args[2]), args[3]);
 		tm.run();
 	}
 
 	public void run() {
 		final long hashes = conf.getGame().numHashes();
 		long lastForDb = 0;
 		File parentFold = new File(parentFile);
 		if (!parentFold.exists())
 			parentFold.mkdir();
 		for (int i = 0; i < numSplits; i++) {
 			long firstForDb = lastForDb;
 			lastForDb = (i + 1) * hashes / numSplits;
 			String dbUri = parentFile + File.separator + "s" + firstForDb
 					+ ".db";
 			try {
 				db = new GZippedFileDatabase(dbUri, conf, readFrom.getHeader(
 						firstForDb, lastForDb - firstForDb));
 				System.out.println("Db "
 						+ i
 						+ " should have "
 						+ ((lastForDb + entrySize - 1) / entrySize - firstForDb
 								/ entrySize) + " entries");
 				Scanner dbScan = new Scanner(readFrom.makeStream(firstForDb,
 						lastForDb - firstForDb));
 				TransferProcess lastTp = null;
 				String nextType = dbScan.next();
 				LinkedList<TransferProcess> firstProcess = null;
 				while (!nextType.equals("end")) {
 					String uri = dbScan.next();
 					dbScan.nextLong();
 					dbScan.nextLong();
 					final TransferProcess tp = new TransferProcess(uri,
 							firstForDb, lastForDb - firstForDb);
 					LinkedList<TransferProcess> pList = processMap.get(tp.host);
 					if (pList == null || pList.isEmpty()) {
 						if (pList == null) {
 							pList = new LinkedList<TransferProcess>();
 							processMap.put(tp.host, pList);
 						}
 						pList.add(tp);
 						new Thread() {
 							@Override
 							public void run() {
 								tp.prepare();
 							}
 						}.start();
 					} else
 						pList.add(tp);
 					if (lastTp == null)
 						firstProcess = pList;
 					else
 						lastTp.setNextProcess(pList);
 					nextType = dbScan.next();
 					lastTp = tp;
 				}
 				lastTp.setNextProcess(null);
 				LinkedList<TransferProcess> nextProcess = firstProcess;
 				while (nextProcess != null) {
 					TransferProcess tp = nextProcess.remove();
 					if (!nextProcess.isEmpty()) {
 						final TransferProcess thisProcess = nextProcess.peek();
 						new Thread() {
 							public void run() {
 								thisProcess.prepare();
 							}
 						}.start();
 					}
 					tp.go();
 					nextProcess = tp.nextProcess;
 				}
 			} catch (IOException e) {
 				throw new Error(e);
 			}
 			writeTo.addDb(GZippedFileDatabase.class.getName(), dbUri,
 					firstForDb, lastForDb - firstForDb);
 			db.close();
 		}
 		readFrom.close();
 		writeTo.close();
 	}
 }
