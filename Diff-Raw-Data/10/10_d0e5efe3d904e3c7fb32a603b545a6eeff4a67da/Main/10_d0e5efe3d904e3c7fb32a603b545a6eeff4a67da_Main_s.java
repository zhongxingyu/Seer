 package main;
 
 import java.io.BufferedOutputStream;
 import java.io.Console;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.zip.GZIPOutputStream;
 
 import javax.persistence.EntityManager;
 
 import master.Master;
 import model.BSMatch;
 import model.BSPlayer;
 import model.BSRun;
 import model.BSUser;
 import model.MatchResultImpl;
 import model.STATUS;
 import model.TEAM;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.compress.archivers.ArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Layout;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import web.WebServer;
 import web.WebUtil;
 import worker.MatchRunner;
 import worker.Worker;
 
 import common.BSUtil;
 import common.Config;
 import common.HibernateUtil;
 
 
 /**
  * Starts all threads and services
  * @author stevearc
  *
  */
 
 public class Main {
 	private static Logger _log = Logger.getLogger(Main.class);
 	public static final String runMatchArg = "runmatch";
 
 	public static void main(String[] args) {
 		Options options = new Options();
 		Options allOptions = new Options();
 		HelpFormatter formatter = new HelpFormatter();
 		options.addOption("s", "server", false, "run as server");
 		options.addOption("w", "worker", true, "run as worker; specify the hostname or ip-address of the master");
 		if (Config.DEBUG) {
 			options.addOption("o", "populate", false, "populate the server DB with mock data");
 			options.addOption("m", "mock-worker", true, "run as a mock-worker; specify the hostname or ip-address of the master");
 			options.addOption("n", "mock-worker-sleep", true, "when running as mock-worker, time in seconds to take per match");
 		}
 		options.addOption("h", "help", false, "display help text");
 		options.addOption("v", "version", false, "display version number");
 		options.addOption("p", "http-port", true, "what port for the http server to listen on (default 80)");
 		options.addOption("d", "data-port", true, "what port for the master/worker to send data over (default 8888)");
 		options.addOption("c", "cores", true, "the number of cores on a worker (determines how many simultaneous matches to run)");
 		
 		for (Object o: options.getOptions()) {
 			allOptions.addOption((Option) o);
 		}
 		// This option should not be made visible to the user
 		allOptions.addOption(runMatchArg, false, "debug method for running a match.  Don't use this unless you know what you're doing.");
 
 		CommandLineParser parser = new GnuParser();
 		CommandLine cmd = null;
 		try {
 			cmd = parser.parse(allOptions, args);
 		} catch (ParseException e1) {
 			formatter.printHelp("run.sh", options);
 			return;
 		}
 		if (cmd.hasOption('h')) {
 			formatter.printHelp("run.sh", options);
 			return;
 		}
 		if (cmd.hasOption('v')) {
 			System.out.println("BSTester " + Config.VERSION);
 			return;
 		}
 		
 		// Configure logger
 		File logDir = new File(Config.logDir);
 		if (!logDir.exists()) {
 			System.out.println("Creating log dithairectory");
 			logDir.mkdirs();
 		}
 		FileAppender appender = new FileAppender();
 		if (cmd.hasOption('s')) {
 			appender.setFile(Config.logDir + "bs-server.log");
 		} else {
 			appender.setFile(Config.logDir + "bs-worker.log");
 		}
 		appender.setName("logFile");
 		Layout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
 		appender.setLayout(layout);
 		appender.activateOptions();
 		Logger rootLogger = Logger.getRootLogger();
 		rootLogger.addAppender(appender);
 		rootLogger.addAppender(new ConsoleAppender(layout));
 		Logger.getLogger("org.hibernate").setLevel(Config.DEBUG ? Level.INFO : Level.WARN);
 		Logger.getLogger("org.eclipse.jetty").setLevel(Config.DEBUG ? Level.INFO : Level.WARN);
 		Logger.getLogger("java.sql").setLevel(Config.DEBUG ? Level.INFO : Level.WARN);
 		Logger.getLogger("hsqldb.db").setLevel(Config.DEBUG ? Level.INFO : Level.WARN);
 		
 		if (cmd.hasOption(runMatchArg)) {
 			// This should only be run programmatically from MatchRunner
 			// Thus, we should know exactly what the remaining arguments are
 			long seed = Long.parseLong(args[1]);
 			String mapName = args[2];
 			String team_a = args[3];
 			String team_b = args[4];
 			try {
 				MatchRunner.runMatch(seed, mapName, team_a, team_b);
 			} catch (IOException e) {
 				_log.error("Error running a forked match " + seed + mapName, e);
 			}
 			System.exit(0);
 		}
 		
 		try {
 			int dataPort = Config.DEFAULT_DATA_PORT;
 			if (cmd.hasOption('d')) {
 				dataPort = Integer.parseInt(cmd.getOptionValue('d'));
 				if (dataPort < 1 || dataPort > 65535) {
 					_log.fatal("Invalid port number!");
 					System.exit(1);
 				}
 			}
 			createDirectoryStructure(cmd.hasOption('s'));
 
 			// -s means Start the server
 			if (cmd.hasOption('s')) {
 				_log.info("Starting server");
 				// If this is the first run, make sure the initial admin is in the DB
 				createWebAdmin();
 				createWorkerTarball();
 				int httpPort = Config.DEFAULT_HTTP_PORT;
 				if (cmd.hasOption('p')) {
 					httpPort = Integer.parseInt(cmd.getOptionValue('p'));
 					if (httpPort < 1 || httpPort > 65535) {
 						_log.fatal("Invalid port number!");
 						System.exit(1);
 					}
 				}
 
 				new Thread(new WebServer(httpPort)).start();
 				Master m = Master.createMaster(dataPort);
 				m.start();
 				if (cmd.hasOption('o')) {
 					createMockData(m);
 				}
 			} // Start the worker
 			else if (cmd.hasOption('w') || cmd.hasOption('m')){
 				if (cmd.hasOption('w') && cmd.hasOption('m')) {
 					_log.fatal("Must specify either -w OR -m");
 					System.exit(1);
 				}
 				_log.info("Starting worker");
 				Config.MOCK_WORKER = cmd.hasOption('m');
 				if (cmd.hasOption('n')) {
 					Config.MOCK_WORKER_SLEEP = Integer.parseInt(cmd.getOptionValue('n'));
 					if (Config.MOCK_WORKER_SLEEP < 0) {
 						_log.fatal("Mock worker sleep time must be greater than 0");
 						System.exit(1);
 					}
 				}
 				String serverAddr = cmd.getOptionValue('w');
 				int cores = 1;
 				if (cmd.hasOption('c')) {
 					cores = Integer.parseInt(cmd.getOptionValue('c'));
 					if (cores < 1) {
 						_log.fatal("Number of cores must be greater than 0!");
 						System.exit(1);
 					} else if (cores > 1 && BSUtil.isWindows()) {
 						_log.fatal("Windows doesn't support running on more than one core.  Try using a big-boy operating system.");
 						System.exit(1);
 					}
 				}
 				new Thread(new Worker(serverAddr, dataPort, cores)).start();
 			} else if (cmd.hasOption('m')) {
 				_log.info("Starting mock worker");
 				String serverAddr = cmd.getOptionValue('m');
 				int cores = 1;
 				if (cmd.hasOption('c')) {
 					cores = Integer.parseInt(cmd.getOptionValue('c'));
 					if (cores < 1) {
 						_log.fatal("Number of cores must be greater than 0!");
 						System.exit(1);
 					}
 				}
 				new Thread(new Worker(serverAddr, dataPort, cores)).start();
 			} else {
 				System.out.println("Must specify if running as server or worker.  Do ./run.sh -h for help.");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void createMockData(Master master) throws IOException {
 		_log.info("Populating database with mock data...");
 		EntityManager em = HibernateUtil.getEntityManager();
 		File checkFile = new File(Config.teamsDir + "mock_player.jar");
 		long numPlayers = em.createQuery("select count(*) from BSPlayer", Long.class).getSingleResult();
 		BSPlayer bsPlayer;
 		if (numPlayers == 0) {
 			checkFile.createNewFile();
 			bsPlayer = new BSPlayer();
 			bsPlayer.setPlayerName("mock_player");
 			em.persist(bsPlayer);
 			em.getTransaction().begin();
 			em.flush();
 			em.getTransaction().commit();
 			em.refresh(bsPlayer);
 		} else {
 			bsPlayer = em.createQuery("from BSPlayer", BSPlayer.class).getResultList().get(0);
 		}
 
 		List<Long> seeds = new ArrayList<Long>();
 		Random r = new Random();
 		for (int i = 0; i < 10; i++) {
 			seeds.add((long)r.nextInt(500));
 		}
 		master.updateMaps();
 		List<Long> mapIds = em.createQuery("select map.id from BSMap map", Long.class).getResultList();
 		for (int i = 0; i < 5; i++) {
 			master.queueRun(bsPlayer.getId(), bsPlayer.getId(), seeds, mapIds);
 		}
 
 		List<BSMatch> matches = em.createQuery("from BSMatch", BSMatch.class).getResultList();
 		MatchResultImpl mock;
 		int i = 0;
 		for (BSMatch m: matches) {
 			mock = MatchResultImpl.constructMockMatchResult();
 			m.setResult(mock);
 			m.setStatus(STATUS.COMPLETE);
 			em.persist(mock);
 			em.merge(m);
 			if (i++ % 50 == 0) {
 				em.getTransaction().begin();
 				em.flush();
 				em.getTransaction().commit();
 			}
 		}
 		em.getTransaction().begin();
 		em.flush();
 		em.getTransaction().commit();
 		
 		em.getTransaction().begin();
 		List<BSRun> runs = em.createQuery("from BSRun", BSRun.class).getResultList();
 		for (BSRun run: runs) {
 			run.setStatus(STATUS.COMPLETE);
 			run.setaWins(em.createQuery("select count(*) from BSMatch match where match.run = ? and match.result.winner = ?", Long.class)
 					.setParameter(1, run)
 					.setParameter(2, TEAM.A)
 					.getSingleResult());
 			run.setbWins(em.createQuery("select count(*) from BSMatch match where match.run = ? and match.result.winner = ?", Long.class)
 					.setParameter(1, run)
 					.setParameter(2, TEAM.B)
 					.getSingleResult());
 			run.setEnded(new Date(new Date().getTime() + 100000 + r.nextInt(100000000)));
 			em.merge(run);
 		}
 		em.flush();
 		em.getTransaction().commit();
 		
 		em.close();
 		_log.info("Finished creating mock data!");
 	}
 
 	/**
 	 * Take the initial user/pass from the config file and put the information into the DB
 	 */
 	private static void createWebAdmin() {
 		EntityManager em = HibernateUtil.getEntityManager();
 
 		Long numUsers = em.createQuery("select count(*) from BSUser", Long.class).getSingleResult();
 		if (numUsers == 0) {
 			System.out.println("***Create an administrator account***");
 			Console cons = System.console();
 			String username;
 			while ((username = cons.readLine("%s", "Username:")) == null || 
 					username.trim().length() == 0 ||
 					WebUtil.containsBadChar(username)) {
 				if (WebUtil.containsBadChar(username)) {
 					System.out.println("Username contains bad character");
 				}
 				// loop
 			}
 			String passwd = null;
 			String confirm = null;
 			while (passwd == null || !passwd.equals(confirm)) {
 				while ((passwd = new String(cons.readPassword("%s", "Password:"))) == null ||
 						passwd.trim().length() == 0 ||
 						WebUtil.containsBadChar(passwd)) {
 					if (WebUtil.containsBadChar(passwd)) {
 						System.out.println("***Password contains bad characters***");
 					}
 					//loop
 				}
 				confirm = new String(cons.readPassword("%s", "Confirm password:"));
 				if (!passwd.equals(confirm)) {
 					System.out.println("***Passwords do not match!***");
 					continue;
 				}
 			}
 
 			String salt = BSUtil.SHA1(""+Math.random());
 			String hashed_password = BSUtil.SHA1(passwd + salt);
 			BSUser user = new BSUser();
 			user.setUsername(username);
 			user.setHashedPassword(hashed_password);
 			user.setSalt(salt);
 			user.setPrivs(BSUser.PRIVS.ADMIN);
 			em.getTransaction().begin();
 			em.merge(user);
 			em.flush();
 			em.getTransaction().commit();
 			_log.info("Admin created");
 		}
 		em.close();
 	}
 
 	private static void createDirectoryStructure(boolean isServer) {
 		if (isServer) {
 			new File(Config.matchDir).mkdirs();
 			new File(Config.scrimmageDir).mkdirs();
 		}
 		new File(Config.teamsDir).mkdir();
 		new File(Config.mapsDir).mkdir();
 	}
 	
 	private static void archiveFile(TarArchiveOutputStream out, String prefix, String fileName, FilenameFilter filter) throws IOException {
 		if (filter != null && !filter.accept(new File(prefix), fileName)) {
 			return;
 		}
 		File file = new File(fileName);
 		if (file.isDirectory()) {
 			for (String subFile: file.list()) {
 				archiveFile(out, prefix, fileName + File.separator + subFile, filter);
 			}
 		}
 		else {
 			byte[] buffer = new byte[1024];
 			ArchiveEntry entry = null;
 			if (BSUtil.isWindows()) {
 				entry = out.createArchiveEntry(file, (prefix + fileName).replaceAll("\\\\", "/"));
 			} else {
 				entry = out.createArchiveEntry(file, prefix + fileName);
 			}
 			out.putArchiveEntry(entry);
 			FileInputStream istream = new FileInputStream(fileName);
 			int len = 0;
 			while ((len = istream.read(buffer)) != -1) {
 				out.write(buffer, 0, len);
 			}
 			istream.close();
 			out.closeArchiveEntry();
 		}
 	}
 
 	private static void createWorkerTarball() {
 		String targetName = "bs-worker.tar.gz";
 		String finalTargetName = "static" + File.separator + targetName;
 		String[] tarFiles = {"README", "COPYING", "run.sh", "run.bat", "lib", "static", "bs-tester.jar"};
 		File finalFile = new File(finalTargetName);
 		if (finalFile.exists()) {
 			return;
 		}
 		TarArchiveOutputStream out = null;
 		try {
 			out = new TarArchiveOutputStream(
 					new GZIPOutputStream(
 							new BufferedOutputStream(
 									new FileOutputStream(targetName))));
 			for (String fileName: tarFiles) {
 				archiveFile(out, "bs-worker" + File.separator, fileName, new FilenameFilter() {
					String[] prefixes = {Config.libDir + "jetty", Config.libDir + "hibernate", 
							Config.libDir + "servlet-api", Config.libDir + "antlr"};
 					@Override
 					public boolean accept(File dir, String name) {
 						for (String p: prefixes) {
 							if (name.startsWith(p)) {
 								return false;
 							}
 						}
 						return true;
 					}
 				});
 			}
 			out.finish();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (out != null)
 					out.close();
 			} catch (IOException e) {
 			}
 		}
 			// Move the tarball into static so we can serve it from the web interface
 			File tFile = new File(targetName);
 		try {
 			FileUtils.moveFile(tFile, finalFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
