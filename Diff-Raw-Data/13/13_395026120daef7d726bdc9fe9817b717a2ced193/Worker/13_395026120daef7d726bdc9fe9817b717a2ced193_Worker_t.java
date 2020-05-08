 package worker;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import javax.net.SocketFactory;
 
 import model.BSMap;
 import model.BSScrimmageSet;
 import model.MatchResultImpl;
 import model.STATUS;
 import networking.Controller;
 import networking.Dependencies;
 import networking.DependencyHashes;
 import networking.Network;
 import networking.Packet;
 import networking.PacketCmd;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import common.BSUtil;
 import common.Config;
 import common.NetworkMatch;
 
 /**
  * Connects to master and runs matches
  * @author stevearc
  *
  */
 public class Worker implements Controller, Runnable {
 	private Logger _log = Logger.getLogger(Worker.class);
 	private String serverAddr;
 	private Network network;
 	private int cores;
 	private int dataPort;
 	private MatchRunner[] running;
 	private boolean runWorker = true;
 	private SocketFactory sf;
 
 	public Worker(String serverAddr, int dataPort, int cores) throws Exception{
 		this.serverAddr = serverAddr;
 		this.dataPort = dataPort;
 		this.cores = cores;
 		running = new MatchRunner[cores];
 		sf = SocketFactory.getDefault();
 	}
 
 	public synchronized void matchFailed(MatchRunner runner, int core, NetworkMatch match) {
 		matchFinish(runner, core, match, null, null, null, null);
 	}
 
 	/**
 	 * Send match data to master
 	 * @param mr
 	 * @param match
 	 * @param status
 	 * @param winner
 	 * @param win_condition
 	 * @param a_points
 	 * @param b_points
 	 * @param data
 	 */
 	public synchronized void matchFinish(MatchRunner runner, int core, NetworkMatch match, STATUS status, 
 			MatchResultImpl result, byte[] data, byte[] outputData) {
 		// If the runner is out of date, we should ignore it
 		if (running[core] != runner) {
 			return;
 		}
 		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, result, data, outputData});
 		network.send(p);
 		running[core].stop();
 		running[core] = null;
 	}
 
 	public synchronized void matchAnalyzed(MatchRunner runner, int core, BSScrimmageSet scrim, STATUS status) {
 		// If the runner is out of date, we should ignore it
 		if (running[core] != runner) {
 			return;
 		}
 		Packet p = new Packet(PacketCmd.ANALYZE_REPLY, new Object[] {scrim, status});
 		network.send(p);
 		running[core].stop();
 		running[core] = null;
 	}
 
 	@Override
 	public void run() {
 		cleanWorkingFiles();
 		while (runWorker) {
 			try {
 				if (network == null || !network.isConnected()) {
 					try {
 						Socket socket = sf.createSocket(serverAddr, dataPort);
 						network = new Network(this, socket);
 						new Thread(network).start();
 						_log.info("Connecting to master");
 						network.send(new Packet(PacketCmd.INIT, new Object[]{cores}));
 					} catch (UnknownHostException e) {
 					} catch (IOException e) {
 					}
 				}
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				_log.warn("Interrupt exception", e);
 			}
 		}
 	}
 	
 	private boolean bsTesterUpToDate(DependencyHashes deps) {
 		if (deps == null) {
 			return true;
 		}
 		try {
 			if (!deps.bsTesterHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum("bs-tester.jar")))) {
 				return false;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			_log.error("Could not find SHA1 algorithm", e);
 			return false;
 		} catch (FileNotFoundException e) {
 			_log.error("Error can't find bs-tester.jar", e);
 			return false;
 		} catch (IOException e) {
 			_log.error("Error hashing bs-tester.jar", e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean battlecodeUpToDate(DependencyHashes deps) {
 		if (deps == null) {
 			return true;
 		}
 		try {
 			if (!deps.battlecodeServerHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(Config.battlecodeServerFile)))) {
 				return false;
 			}
 			if (!deps.allowedPackagesHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(Config.allowedPackagesFile)))) {
 				return false;
 			}
 			if (!deps.disallowedClassesHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(Config.disallowedClassesFile)))) {
 				return false;
 			}
 			if (!deps.methodCostsHash.equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(Config.methodCostsFile)))) {
 				return false;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			_log.error("Could not find SHA1 algorithm", e);
 			return false;
 		} catch (FileNotFoundException e) {
 			// If the files don't exist, we obviously need to update them
 			return false;
 		} catch (IOException e) {
 			_log.error("Error hashing battlecode install files", e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean haveMap(BSMap map) {
 		File mapFile = new File(Config.mapsDir + map.getMapName() + ".xml");
 		if (mapFile.exists()) {
 			try {
 				return map.getHash().equals(BSUtil.convertToHex(BSUtil.SHA1Checksum(mapFile.getAbsolutePath())));
 			} catch (NoSuchAlgorithmException e) {
 				_log.error("Can't find SHA1 hash!", e);
 			} catch (IOException e) {
 				_log.error("Can't read map file!", e);
 			}
 		}
 		return false;
 	}
 
 	private boolean havePlayer(String player) {
 		File playerFile = new File(Config.teamsDir + player + ".jar");
 		return playerFile.exists();
 	}
 
 	/**
 	 * Checks to make sure we have the right files.  Requests them if not.
 	 * @param match
 	 * @return
 	 */
 	private boolean resolveDependencies(NetworkMatch match, DependencyHashes deps) {
 		boolean allClear = true;
 		boolean needUpdateBsTester = false;
 		boolean needUpdate = false;
 		boolean needMap = false;
 		boolean needTeamA = false;
 		boolean needTeamB = false;
 		if (!bsTesterUpToDate(deps)) {
 			allClear = false;
 			needUpdateBsTester = true;
 		}
 		if (!battlecodeUpToDate(deps)) {
 			allClear = false;
 			needUpdate = true;
 		}
 		if (!haveMap(match.map)) {
 			allClear = false;
 			needMap = true;
 		}
 		if (!havePlayer(match.team_a)) {
 			allClear = false;
 			needTeamA = true;
 		}
 		if (!havePlayer(match.team_b)) {
 			allClear = false;
 			needTeamB = true;
 		}
 		if (!allClear) {
 			_log.info("Requesting " + 
 					(needUpdateBsTester ? "bs-tester.jar, " : "") + 
 					(needUpdate ? "battlecode files, " : "") + 
 					(needMap ? "map: " + match.map + ", " : "") + 
 					(needTeamA ? "player: " + match.team_a + ", " : "") + 
 					(needTeamB ? "player: " + match.team_b : ""));
 			Packet requestPacket = new Packet(PacketCmd.REQUEST_DEPENDENCIES, new Object[]{match, needUpdateBsTester, needUpdate, needMap, needTeamA, needTeamB});
 			network.send(requestPacket);
 		}
 		return allClear;
 	}
 
 	private boolean resolveDependencies(DependencyHashes deps) {
		boolean needUpdate = !battlecodeUpToDate(deps);
		boolean needUpdateBsTester = !bsTesterUpToDate(deps);
 		if (needUpdate) {
			_log.info("Requesting " + 
					(needUpdateBsTester ? "bs-tester.jar, " : "") + 
					(needUpdate ? "battlecode files" : ""));
			Packet requestPacket = new Packet(PacketCmd.REQUEST_DEPENDENCIES, new Object[]{null, needUpdateBsTester, needUpdate, false, false, false});
 			network.send(requestPacket);
 		}
 		return !needUpdate;
 	}
 
 	private boolean fileEqualsData(File file, byte[] data) throws IOException {
 		if (file.length() != data.length) {
 			return false;
 		}
 		byte[] fileData = new byte[data.length];
 		FileInputStream fis = new FileInputStream(file);
 		fis.read(fileData);
 		return Arrays.equals(fileData, data);
 	}
 
 	private void writeDataToFile(byte[] data, String fileName) throws IOException {
 		_log.info("Writing file: " + fileName);
 		File outputFile = new File(fileName);
 		if (outputFile.exists()) {
 			if (!fileEqualsData(outputFile, data)){
 				outputFile.delete();
 			}
 		}
 		FileOutputStream ostream = new FileOutputStream(fileName);
 		ostream.write(data);
 		ostream.close();
 	}
 	
 	private void cleanWorkingFiles() {
 		File[] garbageDirs = new File("teams").listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return !pathname.getAbsolutePath().endsWith(".jar");
 			}
 		});
 		for (File f: garbageDirs) {
 			try {
 				FileUtils.deleteDirectory(f);
 			} catch (IOException e) {
 				_log.warn("Error cleaning up compiled player dir: " + f.getAbsolutePath(), e);
 			}
 		}
 		garbageDirs = new File(".").listFiles(new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getAbsolutePath().endsWith(".rms") || pathname.getAbsolutePath().endsWith(".out");
 			}
 		});
 		for (File f: garbageDirs) {
 			if (!f.delete()) {
 				_log.warn("Error deleting file: " + f.getPath());
 			}
 		}
 	}
 
 	/**
 	 * Write dependency file data to the file system.  
 	 * @param dep
 	 * @return true if battlecode-server.jar was updated and we need to restart
 	 */
 	private boolean writeDependencies(Dependencies dep) {
 		if (dep == null) {
 			return false;
 		}
 		boolean needRestart = false;
 		try {
 			if (dep.battlecodeServer != null) {
 				writeDataToFile(dep.battlecodeServer, Config.battlecodeServerFile);
 				needRestart = true;
 			}
 			if (dep.allowedPackages != null) {
 				writeDataToFile(dep.allowedPackages, Config.allowedPackagesFile);
 			}
 			if (dep.disallowedClasses != null) {
 				writeDataToFile(dep.disallowedClasses, Config.disallowedClassesFile);
 			}
 			if (dep.methodCosts != null) {
 				writeDataToFile(dep.methodCosts, Config.methodCostsFile);
 			}
 			if (dep.map != null) {
 				writeDataToFile(dep.map, Config.mapsDir + dep.mapName + ".xml");
 			}
 			if (dep.teamA != null) {
 				writeDataToFile(dep.teamA, Config.teamsDir + dep.teamAName + ".jar");
 			}
 			if (dep.teamB != null) {
 				writeDataToFile(dep.teamB, Config.teamsDir + dep.teamBName + ".jar");
 			}
 			if (dep.bsTester != null) {
 				writeDataToFile(dep.bsTester, "bs-tester.jar");
 				needRestart = true;
 			}
 
 		} catch (IOException e) {
 			_log.error("Could not create player or map file", e);
 		}
 		return needRestart;
 	}
 	
 	private void compilePlayers(String teamA, String teamB) throws IOException {
 		// Make sure we have compiled the players
 		String team_a = teamA.replaceAll("\\W", "_");
 		String team_b = teamB.replaceAll("\\W", "_");
 		if (!new File(Config.teamsDir + "A" + team_a).exists() || 
 				!new File(Config.teamsDir + "B" + team_b).exists()) {
 			String aJar = Config.teamsDir + teamA + ".jar";
 			String bJar = Config.teamsDir + teamB + ".jar";
 			MatchRunner.compilePlayer("A" + team_a, aJar);
 			MatchRunner.compilePlayer("B" + team_b, bJar);
 		}
 	}
 
 	@Override
 	public synchronized void addPacket(Packet p) {
 		int core;
 		switch (p.getCmd()) {
 		case RUN:
 			// Find a free core
 			for (core = 0; core < cores; core++) {
 				if (running[core] == null)
 					break;
 			}
 			// Could not find free core
 			if (core == cores)
 				break;
 
 			NetworkMatch match = (NetworkMatch) p.get(0);
 			DependencyHashes deps = (DependencyHashes) p.get(1);
 			MatchRunner m = new MatchRunner(this, match, core);
 			running[core] = m;
 			if (resolveDependencies(match, deps)) {
 				try {
 					compilePlayers(match.team_a, match.team_b);
 					new Thread(m).start();
 				} catch (IOException e) {
 					_log.error("Error compiling players", e);
 					matchFailed(m, core, match);
 				}
 			}
 			break;
 		case ANALYZE:
 			// Find a free core
 			for (core = 0; core < cores; core++) {
 				if (running[core] == null)
 					break;
 			}
 			// Could not find free core
 			if (core == cores)
 				break;
 			BSScrimmageSet scrim = (BSScrimmageSet) p.get(0);
 			byte[] fileData = (byte[]) p.get(1);
 			DependencyHashes depHashes = (DependencyHashes) p.get(2);
 			MatchRunner mr = new MatchRunner(this, scrim, fileData, core);
 			running[core] = mr;
 			if (resolveDependencies(depHashes)) {
 				new Thread(mr).start();
 			}
 			break;
 		case DEPENDENCIES:
 			Dependencies dep = (Dependencies) p.get(0);
 
 			boolean needRestart = writeDependencies(dep);
 			if (needRestart) {
 				_log.info("battlecode-server.jar updated, restarting worker");
 				System.exit(Config.RESTART_STATUS);
 			}
 			for (MatchRunner matchRunner: running) {
 				if (!matchRunner.isRunning()) {
 					try {
 						compilePlayers(matchRunner.getMatch().team_a, matchRunner.getMatch().team_b);
 						new Thread(matchRunner).start();
 					} catch (IOException e) {
 						_log.error("Error compiling players", e);
 						matchFailed(matchRunner, matchRunner.getCore(), matchRunner.getMatch());
 					}
 				}
 			}
 			break;
 		case STOP:
 			_log.info("Received stop command");
 			for (int i = 0; i < cores; i++) {
 				if (running[i] != null) {
 					running[i].stop();
 					running[i] = null;
 				}
 			}
 			cleanWorkingFiles();
 			break;
 		default:
 			_log.warn("Unrecognized packet command: " + p.getCmd());
 		}
 	}
 
 	@Override
 	public synchronized void onDisconnect() {
 		for (int i = 0; i < cores; i++) {
 			if (running[i] != null) {
 				running[i].stop();
 				running[i] = null;
 			}
 		}
 		_log.info("Disconnected from master");
 	}
 }
