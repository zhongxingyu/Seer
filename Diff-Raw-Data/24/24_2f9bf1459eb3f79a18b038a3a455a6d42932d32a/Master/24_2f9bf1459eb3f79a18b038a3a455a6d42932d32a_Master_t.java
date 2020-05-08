 package master;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 
 import model.BSMap;
 import model.BSMatch;
 import model.BSPlayer;
 import model.BSRun;
 import model.BSScrimmageSet;
 import model.MatchResultImpl;
 import model.STATUS;
 import model.TEAM;
 import networking.Dependencies;
 import networking.DependencyHashes;
 import networking.Packet;
 
 import org.apache.log4j.Logger;
 
 import common.BSUtil;
 import common.HibernateUtil;
 import common.NetworkMatch;
 
 
 /**
  * Handles distribution of load to connected workers
  * @author stevearc
  *
  */
 public class Master extends AbstractMaster {
 	private static Logger _log = Logger.getLogger(Master.class);
 	private NetworkHandler handler;
 	private HashSet<WorkerRepr> workers = new HashSet<WorkerRepr>();
 	private Date mapsLastModifiedDate;
 	private File pendingBattlecodeServerFile;
 	private File pendingIdataFile;
 	private boolean initialized;
 
 	public static Master createMaster(int dataPort) throws Exception {
 		if (singleton != null) {
 			if (singleton instanceof Master) {
 				Master am = (Master)singleton;
 				if (am.handler.getDataPort() != dataPort) {
 					throw new Exception("A Master already exists on a different port!");
 				}
 				return am;
 			}
 		}
 		return new Master(dataPort);
 	}
 
 	private Master(int dataPort) throws Exception {
 		handler = new NetworkHandler(dataPort);
 	}
 
 	/**
 	 * Start running the master
 	 * @throws IOException 
 	 * @throws NoSuchAlgorithmException 
 	 */
 	public synchronized void start() throws NoSuchAlgorithmException, IOException {
 		new Thread(handler).start();
 		startRun();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while (true) {
 					kickoffUpdateMaps();
 					try {
 						Thread.sleep(10000);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		}).start();
 		WebSocketChannelManager.startHeartbeatManager();
 		initialized = true;
 	}
 
 	public synchronized boolean isInitialized() {
 		return initialized;
 	}
 
 	/**
 	 * Add a run to the queue
 	 * @param run
 	 * @param seeds
 	 * @param mapNames
 	 */
 	@Override
 	public synchronized void queueRun(Long teamAId, Long teamBId, List<Long> seeds, List<Long> mapIds) {
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSRun newRun = new BSRun();
 		BSPlayer teamA = em.find(BSPlayer.class, teamAId);
 		BSPlayer teamB = em.find(BSPlayer.class, teamBId);
 		newRun.setTeamA(teamA);
 		newRun.setTeamB(teamB);
 		newRun.setaWins(0l);
 		newRun.setbWins(0l);
 		newRun.setStatus(STATUS.QUEUED);
 		newRun.setStarted(new Date());
 		em.persist(newRun);
 
 		for (Long mapId: mapIds) {
 			BSMap map = em.find(BSMap.class, mapId);
 			for (Long seed: seeds) {
 				BSMatch match = new BSMatch();
 				match.setMap(map);
 				match.setSeed(seed);
 				match.setRun(newRun);
 				match.setStatus(STATUS.QUEUED);
 				em.persist(match);
 			}
 		}
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 		em.close();
 		WebSocketChannelManager.broadcastMsg("index", "INSERT_TABLE_ROW", newRun.getId() + "," + 
 				teamA.getPlayerName() + "," +  teamB.getPlayerName());
 		startRun();
 		_log.info("Queued new run: " + newRun);
 	}
 
 	@Override
	public synchronized void analyzeScrimmageMatch(BSScrimmageSet scrim) {
 		EntityManager em = HibernateUtil.getEntityManager();
 		em.persist(scrim);
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 		em.refresh(scrim);
 		em.close();
 		WebSocketChannelManager.broadcastMsg("scrimmage", "INSERT_TABLE_ROW", scrim.getId() + "," + scrim.getFileName());
 		startRun();
 	}
 
 	@Override
 	protected synchronized void updateBattlecodeFiles(File battlecode_server, File idata) {
 		pendingBattlecodeServerFile = battlecode_server;
 		pendingIdataFile = idata;
 		if (getCurrentRun() == null) {
 			writeBattlecodeFiles();
 		}
 	}
 
 	private void writeBattlecodeFiles() {
 		try {
 			if (pendingBattlecodeServerFile != null && pendingIdataFile != null) {
 				_log.info("Writing updated battlecode files");
 				BSUtil.writeFileData(pendingBattlecodeServerFile, "lib" + File.separator + "battlecode-server.jar");
 				BSUtil.writeFileData(pendingIdataFile, "idata");
 				pendingBattlecodeServerFile = null;
 				pendingIdataFile = null;
 			}
 		} catch (IOException e) {
 			_log.error("Error updating battlecode version", e);
 		}
 	}
 
 	/**
 	 * Delete run data
 	 * @param runId
 	 */
 	@Override
 	protected synchronized void deleteRun(Long runId) {
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSRun run = em.find(BSRun.class, runId);
 		// If it's running right now, just cancel it
 		if (run.getStatus() == STATUS.RUNNING) {
 			_log.info("canceling run");
 			stopCurrentRun(STATUS.CANCELED);
 			startRun();
 		} else {
 			// otherwise, delete it
 			// first delete rms files
 			for (BSMatch match: run.getMatches()) {
 				if (match.getStatus() == STATUS.COMPLETE) {
 					File matchFile = new File("matches" + File.separator + match.toMatchFileName());
 					if (matchFile.exists()) {
 						matchFile.delete();
 					}
 				}
 			}
 
 			// Then delete database entries
 			em.getTransaction().begin();
 			em.remove(run);
 			em.flush();
 			em.getTransaction().commit();
 			em.close();
 			WebSocketChannelManager.broadcastMsg("index", "DELETE_TABLE_ROW", ""+runId);
 		}
 	}
 
 	@Override
 	public synchronized void deleteScrimmage(Long scrimId) {
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSScrimmageSet scrim = em.find(BSScrimmageSet.class, scrimId);
 		if (scrim == null)
 			return;
 		// delete rms file
 		File f = new File(scrim.toPath());
 		if (f.exists()) {
 			f.delete();
 		}
 
 		// Then delete database entries
 		em.getTransaction().begin();
 		em.remove(scrim);
 		em.remove(scrim.getScrim1());
 		if (scrim.getScrim2() != null)
 			em.remove(scrim.getScrim2());
 		if (scrim.getScrim3() != null)
 			em.remove(scrim.getScrim3());
 		em.flush();
 		em.getTransaction().commit();
 		em.close();
 		WebSocketChannelManager.broadcastMsg("scrimmage", "DELETE_TABLE_ROW", ""+scrim.getId());
 	}
 
 	/**
 	 * Process a worker connecting
 	 * @param worker
 	 */
 	@Override
 	synchronized void workerConnect(WorkerRepr worker) {
 		_log.info("Worker connected: " + worker);
 		workers.add(worker);
 		WebSocketChannelManager.broadcastMsg("connections", "INSERT_TABLE_ROW", worker.toHTML());
 	}
 
 	/**
 	 * Process a worker disconnecting
 	 * @param worker
 	 */
 	@Override
 	synchronized void workerDisconnect(WorkerRepr worker) {
 		_log.info("Worker disconnected: " + worker);
 		WebSocketChannelManager.broadcastMsg("connections", "DELETE_TABLE_ROW", ""+worker.getId());
 		workers.remove(worker);
 	}
 
 	@Override
 	public synchronized void matchAnalyzed(WorkerRepr worker, Packet p) {
 		BSScrimmageSet scrim = (BSScrimmageSet) p.get(0);
 		STATUS status = (STATUS) p.get(1);
 		WebSocketChannelManager.broadcastMsg("connections", "REMOVE_MAP", worker.getId() + "," + scrim.getFileName());
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSScrimmageSet dbScrim = em.find(BSScrimmageSet.class, scrim.getId());
 		if (dbScrim == null || dbScrim.getStatus() != STATUS.RUNNING) {
 			// Match was already analyzed by another worker or it was canceled
 		} else if (status == STATUS.COMPLETE) {
 			em.persist(scrim.getScrim1());
 			if (scrim.getScrim2() != null)
 				em.persist(scrim.getScrim2());
 			if (scrim.getScrim3() != null)
 				em.persist(scrim.getScrim3());
 			em.getTransaction().begin();
 			em.flush();
 			em.getTransaction().commit();
 			em.merge(scrim);
 			scrim.getScrim1().setScrimmageSet(scrim);
 			if (scrim.getScrim2() != null)
 				scrim.getScrim2().setScrimmageSet(scrim);
 			if (scrim.getScrim3() != null)
 				scrim.getScrim3().setScrimmageSet(scrim);
 			em.getTransaction().begin();
 			em.flush();
 			em.getTransaction().commit();
 			_log.info("Match analyzed: " + scrim.getFileName());
 			WebSocketChannelManager.broadcastMsg("scrimmage", "FINISH_SCRIMMAGE", scrim.getId() + "," + scrim.getPlayerA() + "," + 
 					scrim.getPlayerB() + "," + scrim.getStatus() + "," + scrim.getWinner());
 		}
 		sendWorkerMatches(worker);
 		em.close();
 	}
 
 	/**
 	 * Save data from a finished match
 	 * @param worker
 	 * @param p
 	 */
 	@Override
 	public synchronized void matchFinished(WorkerRepr worker, Packet p) {
 		NetworkMatch m = (NetworkMatch) p.get(0);
 		STATUS status = (STATUS) p.get(1);
 		MatchResultImpl result = (MatchResultImpl) p.get(2);
 		byte[] data = (byte[]) p.get(3);
 		WebSocketChannelManager.broadcastMsg("connections", "REMOVE_MAP", worker.getId() + "," + m.toMapString());
 		try {
 			EntityManager em = HibernateUtil.getEntityManager();
 			BSMatch match = em.find(BSMatch.class, m.id);
 			if (match.getStatus() != STATUS.RUNNING || match.getRun().getStatus() != STATUS.RUNNING) {
 				// Match was already finished by another worker
 			} else if (status == STATUS.COMPLETE) {
 				em.getTransaction().begin();
 				em.persist(result);
 				em.flush();
 				em.getTransaction().commit();
 				match.setResult(result);
 				match.setStatus(STATUS.COMPLETE);
 				BSRun run = match.getRun();
 				if (match.getResult().getWinner() == TEAM.A) {
 					run.setaWins(run.getaWins() + 1);
 				} else {
 					run.setbWins(run.getbWins() + 1);
 				}
 				_log.info("Match finished: " + m + " winner: " + result.getWinner());
 				File outFile = new File("static" + File.separator + "matches" + File.separator + match.getRun().getId() + match.getMap().getMapName() + m.seed + ".rms");
 				outFile.createNewFile();
 				FileOutputStream fos = new FileOutputStream(outFile);
 				fos.write(data);
 				em.getTransaction().begin();
 				em.merge(match);
 				em.merge(run);
 				em.flush();
 				em.getTransaction().commit();
 
 				// Calculate percent finished and find win status
 				String winRecord = run.getaWins() + "/" + run.getbWins();
 				List<Object[]> resultList = em.createQuery("select match.status, count(*) from BSMatch match where match.run.status = ? group by match.status", Object[].class)
 				.setParameter(1, STATUS.RUNNING)
 				.getResultList();
 				long currentMatches = 0;
 				long totalMatches = 0;
 				for (Object[] valuePair: resultList) {
 					if (valuePair[0] == STATUS.COMPLETE) {
 						currentMatches += (Long) valuePair[1];
 					}
 					totalMatches += (Long) valuePair[1];
 				}
 				String percent = currentMatches*100/totalMatches + "%";
 				WebSocketChannelManager.broadcastMsg("index", "MATCH_FINISHED", m.run_id + "," + 
 						percent + "," + winRecord);
 			} else {
 				_log.warn("Match " + m + " on worker " + worker + " failed");
 			}
 
 			Long matchesLeft = em.createQuery("select count(*) from BSMatch match where match.run = ? and match.status != ?", Long.class)
 			.setParameter(1, match.getRun())
 			.setParameter(2, STATUS.COMPLETE)
 			.getSingleResult();
 			// If finished, start next run
 			if (matchesLeft == 0) {
 				stopCurrentRun(STATUS.COMPLETE);
 				startRun();
 			} else {
 				sendWorkerMatches(worker);
 			}
 			em.close();
 		} catch (IOException e) {
 			_log.error("Error writing match file", e);
 		}
 	}
 
 	/**
 	 * Send matches to a worker until they are saturated
 	 * @param worker
 	 */
 	@Override
 	protected synchronized void sendWorkerMatches(WorkerRepr worker) {
 		EntityManager em = HibernateUtil.getEntityManager();
 		String battlecodeServerHash;
 		String idataHash;
 		try {
 			battlecodeServerHash = BSUtil.convertToHex(BSUtil.SHA1Checksum("lib" + File.separator + "battlecode-server.jar"));
 			idataHash = BSUtil.convertToHex(BSUtil.SHA1Checksum("idata"));
 		} catch (FileNotFoundException e) {
 			_log.warn(e);
 			return;
 		} catch (NoSuchAlgorithmException e) {
 			_log.error("Cannot find SHA1 algorithm!", e);
 			return;
 		} catch (IOException e) {
 			_log.error("Error hashing battlecode-server.jar or idata", e);
 			return;
 		}
 		DependencyHashes deps = new DependencyHashes(battlecodeServerHash, idataHash);
 
 		sendBlock:
 		{
 			// First check queued scrimmage matches
 			List<BSScrimmageSet> queuedScrimmages = em.createQuery("from BSScrimmageSet scrim where scrim.status = ?", BSScrimmageSet.class)
 			.setParameter(1, STATUS.QUEUED)
 			.getResultList();
 			for (BSScrimmageSet s: queuedScrimmages) {
 				if (!worker.isFree())
 					break sendBlock;
 				_log.info("Sending scrimmage match " + s.getFileName() + " to worker " + worker);
 				try {
 					worker.analyzeMatch(s, BSUtil.getFileData(s.toPath()), deps);
 					s.setStatus(STATUS.RUNNING);
 					em.merge(s);
 					WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.getId() + "," + s.getFileName());
 					WebSocketChannelManager.broadcastMsg("scrimmage", "START_SCRIMMAGE", "" + s.getId());
 				} catch (IOException e) {
 					_log.warn("Error reading scrimmage match " + s.toPath(), e);
 				}
 			}
 			if (!worker.isFree())
 				break sendBlock;
 
 			// Then check queued runs
 			List<BSMatch> queuedMatches = em.createQuery("from BSMatch match inner join fetch match.map inner join fetch match.run " +
 					"where match.status = ? and match.run.status = ?", BSMatch.class)
 					.setParameter(1, STATUS.QUEUED)
 					.setParameter(2, STATUS.RUNNING)
 					.getResultList();
 
 			for (BSMatch m: queuedMatches) {
 				if (!worker.isFree())
 					break sendBlock;
 				NetworkMatch nm = m.buildNetworkMatch();
 				_log.info("Sending match " + nm + " to worker " + worker);
 				WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.getId() + "," + nm.toMapString());
 				worker.runMatch(nm, deps);
 				m.setStatus(STATUS.RUNNING);
 				em.merge(m);
 			}
 			if (!worker.isFree())
 				break sendBlock;
 
 			// If we are currently running all necessary maps, add some redundancy by
 			// Sending this worker random maps that other workers are currently running
 			Random r = new Random();
 			List<BSMatch> runningMatches = em.createQuery("from BSMatch match inner join fetch match.map inner join fetch match.run " +
 					"where match.status = ? and match.run.status = ?", BSMatch.class)
 					.setParameter(1, STATUS.RUNNING)
 					.setParameter(2, STATUS.RUNNING)
 					.getResultList();
 			while (!runningMatches.isEmpty() && worker.isFree() && 
 					worker.getRunningMatches().size() < runningMatches.size()) {
 				BSMatch m = null;
 				NetworkMatch nm = null;
 				do {
 					m = runningMatches.get(r.nextInt(runningMatches.size()));
 					nm = m.buildNetworkMatch();
 				} while (worker.getRunningMatches().contains(nm));
 				_log.info("Sending redundant match " + nm + " to worker " + worker);
 				WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.getId() + "," + nm.toMapString());
 				worker.runMatch(nm, deps);
 			}
 			if (!worker.isFree())
 				break sendBlock;
 
 			// Lastly, try sending redundant scrimmage matches
 			List<BSScrimmageSet> runningScrimmages = em.createQuery("from BSScrimmageSet scrim where scrim.status = ?", BSScrimmageSet.class)
 			.setParameter(1, STATUS.RUNNING)
 			.getResultList();
 			while (!runningScrimmages.isEmpty() && worker.isFree() && 
 					worker.getAnalyzingMatches().size() < runningScrimmages.size()) {
 				BSScrimmageSet s;
 				do {
 					s = runningScrimmages.get(r.nextInt(runningScrimmages.size()));
 				} while (worker.getAnalyzingMatches().contains(s));
 				try {
 					worker.analyzeMatch(s, BSUtil.getFileData(s.toPath()), deps);
 					_log.info("Sending redundant scrimmage match " + s.getFileName() + " to worker " + worker);
 					WebSocketChannelManager.broadcastMsg("connections", "ADD_MAP", worker.getId() + "," + s.getFileName());
 				} catch (IOException e) {
 					_log.warn("Error reading scrimmage match " + s.toPath(), e);
 				}
 			}
 
 		}
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	@Override
 	public synchronized void sendWorkerDependencies(WorkerRepr worker, NetworkMatch match, boolean needUpdate, boolean needMap, boolean needTeamA, boolean needTeamB) {
 		Dependencies dep;
 		byte[] map = null;
 		byte[] teamA = null;
 		byte[] teamB = null;
 		byte[] battlecodeServer = null;
 		byte[] idata = null;
 		try {
 			if (needUpdate) {
 				battlecodeServer = BSUtil.getFileData("lib" + File.separator + "battlecode-server.jar");
 				idata = BSUtil.getFileData("idata");
 			}
 			if (match != null) {
 				if (needMap) {
 					map = BSUtil.getFileData("maps" + File.separator + match.map.getMapName() + ".xml");
 				}
 				if (needTeamA) {
 					teamA = BSUtil.getFileData("teams" + File.separator + match.team_a + ".jar");
 				}
 				if (needTeamB) {
 					teamB = BSUtil.getFileData("teams" + File.separator + match.team_b + ".jar");
 				}
 				dep = new Dependencies(battlecodeServer, idata, match.map.getMapName(), map, match.team_a, teamA, match.team_b, teamB);
 			} else {
 				dep = new Dependencies(battlecodeServer, idata, null, map, null, teamA, null, teamB);
 			}
 			_log.info("Sending " + worker + " " + dep);
 			worker.sendDependencies(dep);
 		} catch (IOException e) {
 			_log.error("Could not read data file", e);
 			WebSocketChannelManager.broadcastMsg("connections", "REMOVE_MAP", worker.getId() + "," + match.toMapString());
 			worker.stopAllMatches();
 			sendWorkerMatches(worker);
 		}
 	}
 
 	private void startRun() {
 		if (getCurrentRun() != null) {
 			// Already running
 			return;
 		}
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSRun nextRun = null;
 		try {
 			List<BSRun> queued = em.createQuery("from BSRun run where run.status = ? order by run.id asc limit 1", BSRun.class)
 			.setParameter(1, STATUS.QUEUED)
 			.getResultList();
 			if (queued.size() > 0) {
 				nextRun = queued.get(0);
 			}
 		} catch (NoResultException e) {
 			// pass
 		}
 		if (nextRun != null) {
 			nextRun.setStatus(STATUS.RUNNING);
 			nextRun.setStarted(new Date());
 			em.merge(nextRun);
 			em.getTransaction().begin();
 			em.flush();
 			em.getTransaction().commit();
 			WebSocketChannelManager.broadcastMsg("index", "START_RUN", nextRun.getId() + "");
 			for (WorkerRepr c: workers) {
 				sendWorkerMatches(c);
 			}
 		}
 		else // Check for scrimmages to analyze
 		{
 			Long scrimmagesQueued = em.createQuery("select count(*) from BSScrimmageSet scrim where scrim.status = ? or scrim.status = ?", Long.class)
 			.setParameter(1, STATUS.QUEUED)
 			.setParameter(2, STATUS.RUNNING)
 			.getSingleResult();
 			if (scrimmagesQueued > 0) {
 				for (WorkerRepr c: workers) {
 					sendWorkerMatches(c);
 				}
 			}
 		}
 		em.close();
 	}
 
 	private BSRun getCurrentRun() {
 		EntityManager em = HibernateUtil.getEntityManager();
 		BSRun currentRun = null;
 		try {
 			currentRun = em.createQuery("from BSRun run where run.status = ?", BSRun.class).setParameter(1, STATUS.RUNNING).getSingleResult();
 		} catch (NoResultException e) {
 			// pass
 		} finally {
 			em.close();
 		}
 		return currentRun;
 	}
 
 	private void stopCurrentRun(STATUS status) {
 		_log.info("Stopping current run");
 		BSRun currentRun = getCurrentRun();
 		EntityManager em = HibernateUtil.getEntityManager();
 		currentRun.setStatus(status);
 		currentRun.setEnded(new Date());
 		em.merge(currentRun);
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 
 		// Remove unfinished matches
 		em.getTransaction().begin();
 		em.createQuery("delete from BSMatch m where m.run = ? and m.status != ?")
 		.setParameter(1, currentRun)
 		.setParameter(2, STATUS.COMPLETE)
 		.executeUpdate();
 		em.flush();
 		em.getTransaction().commit();
 		em.close();
 		WebSocketChannelManager.broadcastMsg("index", "FINISH_RUN", currentRun.getId() + "," + status.toString());
 		WebSocketChannelManager.broadcastMsg("connections", "FINISH_RUN", "");
 		for (WorkerRepr c: workers) {
 			c.stopAllMatches();
 		}
 		writeBattlecodeFiles();
 	}
 
 	/**
 	 * Update the list of available maps
 	 */
 	@Override
 	public synchronized void updateMaps() {
 		File file = new File("maps");
 		if (new Date(file.lastModified()).equals(mapsLastModifiedDate))
 			return;
 		mapsLastModifiedDate = new Date(file.lastModified());
 		File[] mapFiles = file.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().endsWith(".xml");
 			}
 		});
 		ArrayList<BSMap> newMaps = new ArrayList<BSMap>();
 		if (mapFiles == null)
 			return;
 		for (File m: mapFiles) {
 			try {
 				newMaps.add(new BSMap(m));
 			} catch (Exception e) {
 				_log.warn("Error parsing map", e);
 			}
 		}
 
 		EntityManager em = HibernateUtil.getEntityManager();
 
 		List<String> mapNames = em.createQuery("select map.mapName from BSMap map", String.class).getResultList();
 		for (BSMap map: newMaps) {
 			if (!mapNames.contains(map.getMapName())) {
 				em.persist(map);
 				WebSocketChannelManager.broadcastMsg("index", "ADD_MAP", map.getMapName());
 			}
 		}
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 		em.close();
 	}
 
 	/**
 	 * 
 	 * @return Currently connected workers
 	 */
 	@Override
 	protected Set<WorkerRepr> getConnections() {
 		return workers;
 	}
 
 }
