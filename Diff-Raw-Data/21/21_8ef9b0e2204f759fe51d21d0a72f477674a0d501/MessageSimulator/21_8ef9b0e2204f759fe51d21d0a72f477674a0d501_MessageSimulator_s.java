 package sw.server.simulator;
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Random;
 
 import sw.server.MessageBuffer;
 import sw.server.ServerCLI;
 import sw.server.db.DBConnection;
 import sw.server.db.SheepDao;
 import sw.server.db.UserDao;
 import sw.server.models.Message;
 import sw.server.models.Sheep;
 import sw.server.models.User;
 
 /**
  * Handles simulation operations
  * 
  * @author Lars Kristian
  * 
  */
 public class MessageSimulator implements Runnable {
 
 	private ServerCLI ui;
 	private volatile boolean run;
 	private volatile MessageBuffer buffer;
 
 	private Random rand = new Random();
 
 	private long maxRfid = 1;
 	private long nextRfid = 1;
 	private long msgId;
 	private long updateInterval;
 
 	public MessageSimulator(ServerCLI ui, MessageBuffer buffer) {
 		this.buffer = buffer;
 		this.ui = ui;
 		Config.load();
 	}
 
 	/**
 	 * Resets the database by emptying all tables and inserting new data
 	 */
 	public void reset() {
 		resetDb();
 		generateInserts();
 	}
 
 	/**
 	 * Generates a random latitude value within config bounds
 	 * 
 	 * @return The generated value
 	 */
 	private double genLatVal() {
 		double diff = Config.LAT_MAX - Config.LAT_MIN;
 		diff *= rand.nextDouble();
 		return Config.LAT_MIN + diff;
 
 	}
 
 	/**
 	 * Generates a random longitude value withing config bounds
 	 * 
 	 * @return The generated value
 	 */
 	private double genLongVal() {
 		double diff = Config.LONG_MAX - Config.LONG_MIN;
 		diff *= rand.nextDouble();
 		return Config.LONG_MIN + diff;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		run = true;
 		updateInterval = UpdateInterval();
 		while (run) {
 			try {
 				Thread.sleep(updateInterval);
 			} catch (InterruptedException e) {
 				run = false;
 				return;
 			}
 			if (run) {
 				buffer.put(generateMessage());
 			}
 		}
 	}
 
 	/**
 	 * Inserts a new alarm for the specified rfid into the message queue
 	 * 
 	 * @param rfid The RFID to generate an alarm for
 	 */
 	public void alarm(long rfid) {
 		if (rfid < maxRfid) {
 			buffer.put(generateAlertMessage(rfid));
 		} else {
 			ui.print("RFID does not exist.");
 			ui.prompt();
 		}
 	}
 
 	/**
 	 * Generates an alarm message for the specified RFID
 	 * 
 	 * @param rfid The RFID to generate an alarm for
 	 * @return The generated Message
 	 */
 	private Message generateAlertMessage(long rfid) {
 		return new Message(msgId++, rfid, 1, genLatVal(), genLongVal(), 0, 40);
 	}
 
 	/**
 	 * Calculates the normalized time between updates
 	 * 
 	 * @return
 	 */
 	public long UpdateInterval() {
 		if (Config.DAILY_UPDATES > 0) {
			return 3600000 / ((Config.NUM_PRODUCERS * Config.NUM_SHEEP * Config.DAILY_UPDATES) / 24);
 		} else {
 			run = false;
 			return 0;
 		}
 	}
 
 	/**
 	 * Signals the simulator to stop
 	 */
 	public void stop() {
 		run = false;
 	}
 
 	/**
 	 * Generates an update message, cycling through all RFIDs before starting over
 	 * 
 	 * @return The generated Message
 	 */
 	private Message generateMessage() {
 		nextRfid = nextRfid > maxRfid ? 1 : nextRfid;
 		return new Message(msgId++, nextRfid++, 0, genLatVal(), genLongVal(), 100, 40);
 	}
 
 	/**
 	 * Empties all tables
 	 */
 	private void resetDb() {
 		try {
 			DBConnection db = new DBConnection();
 			db.getStatement().execute("DELETE FROM Event");
 			db.getStatement().execute("DELETE FROM Sheep");
 			db.getStatement().execute("DELETE FROM Contact");
 			db.getStatement().execute("DELETE FROM User");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ui.print("Reset completed.");
 		ui.prompt();
 	}
 
 	/**
 	 * Generated data for all tables, as specified by configuration
 	 */
 	private void generateInserts() {
 		UserDao userDao = new UserDao();
 		SheepDao sheepDao = new SheepDao();
		for (int i = 1; i <= Config.NUM_PRODUCERS; ++i) {
 			User user = new User(i, Config.USER_PREFIX + "@" + i, Config.USER_PASSWORD, "name", 5);
 			userDao.insert(user);
 			for (int j = 1; j <= Config.NUM_SHEEP; ++j) {
 				sheepDao.insert(new Sheep(user.getId(), j, maxRfid++, "name", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), "notes", 50,
 						false));
 			}
 		}
 		ui.print("Insert completed.");
 	}
 }
