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
		this.buffer = buffer;re
 		this.ui = ui;
 	}
 
 	public void reset() {
 		resetDb();
 	}
 
 	private double genLatVal() {
 		double diff = Config.LAT_MAX - Config.LAT_MIN;
 		diff *= rand.nextDouble();
 		return Config.LAT_MIN + diff;
 
 	}
 
 	private double genLongVal() {
 		double diff = Config.LONG_MAX - Config.LONG_MIN;
 		diff *= rand.nextDouble();
 		return Config.LONG_MIN + diff;
 	}
 
 	public void run() {
 		resetDb();
 		generateInserts();
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
 
 	public void alarm(long rfid) {
 		if (rfid < maxRfid) {
 			buffer.put(generateAlert(rfid));
 		} else {
 			ui.print("RFID does not exist.");
 			ui.prompt();
 		}
 
 	}
 
 	private Message generateAlert(long rfid) {
 		return new Message(msgId++, rfid, 1, genLatVal(), genLongVal(), 0, 40);
 	}
 
 	public long UpdateInterval() {
 		if (Config.DAILY_UPDATES > 0) {
 			return 3600000 / ((Config.NUM_PRODUCERS * Config.NUM_SHEEP * Config.DAILY_UPDATES) / 24);
 		} else {
 			run = false;
 			return 0; 
 		}
 	}
 
 	public void stop() {
 		run = false;
 	}
 
 	private Message generateMessage() {
 		nextRfid = nextRfid > maxRfid ? 1 : nextRfid;
 		return new Message(msgId++, nextRfid++, 0, genLatVal(), genLongVal(), 100, 40);
 	}
 
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
 
 	private void generateInserts() {
 		UserDao userDao = new UserDao();
 		SheepDao sheepDao = new SheepDao();
 		for (int i = 1; i <= Config.NUM_PRODUCERS; ++i) {
 			User user = new User(i, Config.USER_PREFIX + "@" + i, Config.USER_PASSWORD, "name", 5);
 			userDao.insert(user);
 			for (int j = 1; j <= Config.NUM_SHEEP; ++j) {
 				sheepDao.insert(new Sheep(user.getId(), j, maxRfid++, "name", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), "notes", 50, false));
 			}
 		}
 		ui.print("Insert completed.");
 		ui.prompt();
 	}
 
 }
