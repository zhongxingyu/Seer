 package com.tencent.qqlive.streaming.spout;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import backtype.storm.spout.SpoutOutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.IRichSpout;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Values;
 
 import com.tencent.qqlive.streaming.util.Config;
 import com.tencent.qqlive.streaming.util.Utils;
 import com.tencent.qqlive.streaming.util.ZkClient;
 
 public class HinaSourceSpout implements IRichSpout {
 	private static final Logger logger = LoggerFactory
 			.getLogger(HinaSourceSpout.class);
 
 	public static final String STREAM_ID_MultDevVod = "MultDevVod";
 
 	private BlockingQueue<HinaEvent> messageQueue = new LinkedBlockingQueue<HinaEvent>(
 			100000);
 
 	private AtomicReference<HashSet<String>> playIdsRef = new AtomicReference<HashSet<String>>();
 
 	private CountDownLatch latch = null;
 
 	private SpoutOutputCollector collector = null;
 	private Map conf = null;
 	private TopologyContext context = null;
 	
 	private HinaSourceServer server = null;
 	private ZkClient zkc = null;
 	private Connection conn = null;
 
 	private ScheduledExecutorService executor = null;
 
 	private int writeInterval = 300;
 
 	private String dbHost = null;
 	private String dbPort = null;
 	private String dbUser = null;
 	private String dbPassword = null;
 	
 	private SpoutStatics statics = null;
 
 	public void open(Map conf, TopologyContext context,
 			SpoutOutputCollector collector) {
 		this.collector = collector;
 		this.conf = conf;
 		this.context = context;
 		
 		statics = new SpoutStatics();
 		
 		String zkHost = (String) conf.get("zk.host");
 		String zkPath = (String) conf.get("zk.path");
 		String reloadIntervalStr = (String) conf.get("reload.interval");
 		String writeIntervalStr = (String) conf.get("db.write.interval");
 
 		if (zkHost == null || zkPath == null || reloadIntervalStr == null
 				|| writeIntervalStr == null)
 			throw new RuntimeException("failed to read config");
 
 		dbHost = (String) conf.get("db.host");
 		dbPort = (String) conf.get("db.port");
 		dbUser = (String) conf.get("db.user");
 		dbPassword = (String) conf.get("db.password");
 
 		if (dbHost == null || dbPort == null || dbUser == null
 				|| dbPassword == null)
 			throw new RuntimeException("failed to load db config");
 
 		writeInterval = Integer.valueOf(writeIntervalStr);
 
 		try {
 			zkc = new ZkClient(zkHost);
 			zkc.connect();
 
 			latch = new CountDownLatch(1);
 			executor = Executors.newSingleThreadScheduledExecutor();
 			executor.scheduleAtFixedRate(new Runnable() {
 
 				public void run() {
 					
 					logger.info("Spout Statics: " + HinaSourceSpout.this.context.getThisTaskId());
 					logger.info(statics.toStr());
 					
 					statics.reset();				
 					
 					// connect to db
 					String dbUrl = String.format("jdbc:mysql://%s:%s/", dbHost,
 							dbPort);
 					try {
 						Class.forName("com.mysql.jdbc.Driver");
 					} catch (ClassNotFoundException e) {
 						throw new RuntimeException(
 								"failed to load mysql jdbc driver");
 					}
 
 					HashSet<String> ids = new HashSet<String>();
 
 					try {
 						conn = DriverManager.getConnection(dbUrl, dbUser,
 								dbPassword);
 						Statement statement = conn.createStatement();
 						ResultSet rs = statement
								.executeQuery("SELECT DISTINCT c_data6 FROM d_live_manage.t_live_comm_dzd WHERE CHAR_LENGTH(c_data6) = 11;");
 
 						StringBuilder sb = new StringBuilder();
 						int count = 0;
 						while (rs.next()) {
 							String playID = rs.getString(1);
 							ids.add(playID);
 							sb.append(playID);
 							sb.append(",");
 							count++;
 						}
 						logger.info("load " + count + " playids: "
 								+ sb.toString());
 						conn.close();
 					} catch (SQLException e) {
 						logger.error("failed to execute sql: "
 								+ Utils.stringifyException(e));
 					}
 
 					playIdsRef.set(ids);
 
 					latch.countDown();
 				}
 
 			}, 0, Integer.valueOf(reloadIntervalStr), TimeUnit.SECONDS);
 
 			logger.debug("waiting for loading playid from database");
 			latch.await();
 
 			server = new HinaSourceServer(messageQueue, statics);
 
 			server.startServer();
 			logger.info("start server " + server.getAddress() + ":"
 					+ server.getPort());
 
 			zkc.register(zkPath + Config.SPOUT_REGISTER_PATH,
 					server.getAddress(), server.getPort());
 			logger.info("register in zookeeper " + zkHost + " path " + zkPath
 					+ Config.SPOUT_REGISTER_PATH);
 		} catch (Exception e) {
 			logger.error("failed to open spout: "
 					+ Utils.stringifyException(e));
 			// e.printStackTrace();
 		}
 	}
 
 	public void close() {
 		try {
 			if (zkc != null) {
 				zkc.disconnect();
 			}
 		} catch (Exception e) {
 			logger.error("failed to stop server: " + e.toString());
 			// e.printStackTrace();
 		}
 
 		if (server != null) {
 			server.stopServer();
 		}
 	}
 
 	public void activate() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void deactivate() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void nextTuple() {
 		HinaEvent event = messageQueue.poll();
 		if (event == null)
 			return;
 		
 		statics.handlePacket.getAndIncrement();
 		
 		if (event.getProperties().get("category") == null 
 				|| !event.getProperties().get("category").equals(STREAM_ID_MultDevVod)) {
 			statics.wrongStreamPacket.getAndIncrement();
 			return;
 		}
 
 		LogEntry entry = parseBody(new String(event.getBody()));
 		if (entry == null 
 				|| System.currentTimeMillis() - entry.getTimestamp() > 4 * writeInterval * 1000 ) {
 			statics.timeoutPacket.getAndIncrement();
 			return;
 		}
 		
 		if (!playIdsRef.get().contains(entry.getPlayid())) {
 			statics.invalidPlayidPacket.getAndIncrement();
 			return;
 		}
 
 //		logger.info("log: " + new String(event.getBody()));
 		collector.emit(STREAM_ID_MultDevVod, new Values(entry.getTimestamp(),
 				entry.getAlbum(), entry.getPlayid(), entry.getDevtype()));
 		statics.emitPacket.getAndIncrement();
 	}
 
 	private LogEntry parseBody(String body) {
 		LogEntry entry = new LogEntry();
 
 		String[] items = body.split("\\s+");
 		String timestamp = items[0] + " " + items[1];
 		SimpleDateFormat formatter = new SimpleDateFormat(
 				"yyyy-MM-dd HH:mm:ss.S");
 		Date date = null;
 		try {
 			date = formatter.parse(timestamp);
 		} catch (ParseException e) {
 			logger.warn("failed to parse timestamp: "
 					+ Utils.stringifyException(e));
 			return null;
 		}
 		entry.setTimestamp(date.getTime());
 
 		for (int i = 2; i < items.length; i++) {
 			if (items[i].startsWith("album")) {
 				String[] keyVal = items[i].split("\\s*=\\s*");
 				if (keyVal.length != 2)
 					continue;
 
 				entry.setAlbum(keyVal[1]);
 			} else if (items[i].startsWith("playid")) {
 				String[] keyVal = items[i].split("\\s*=\\s*");
 				if (keyVal.length != 2)
 					continue;
 
 				entry.setPlayid(keyVal[1]);
 			} else if (items[i].startsWith("devtype")) {
 				String[] keyVal = items[i].split("\\s*=\\s*");
 				if (keyVal.length != 2)
 					continue;
 
 				entry.setDevtype(keyVal[1]);
 			}
 		}
 
 		return entry;
 	}
 
 	public void ack(Object msgId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void fail(Object msgId) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declareStream(STREAM_ID_MultDevVod, new Fields("timestamp",
 				"album", "playid", "devtype"));
 	}
 
 	public Map<String, Object> getComponentConfiguration() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static void main(String[] args) throws Exception {
 		BufferedReader reader = new BufferedReader(new FileReader(
 				"D:\\file.txt"));
 
 		String line = null;
 
 		while ((line = reader.readLine()) != null) {
 
 			String[] items = line.split("\\s+");
 			String timestamp = items[0] + " " + items[1];
 
 			int count = 0;
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < items.length; i++) {
 				if (items[i].startsWith("album")) {
 					String[] keyVal = items[i].split("\\s*=\\s*");
 					if (keyVal.length != 2)
 						continue;
 
 					sb.append(" album " + keyVal[1]);
 				} else if (items[i].startsWith("playid")) {
 					String[] keyVal = items[i].split("\\s*=\\s*");
 					if (keyVal.length != 2)
 						continue;
 
 					sb.append(" playid " + keyVal[1]);
 				} else if (items[i].startsWith("devtype")) {
 					String[] keyVal = items[i].split("\\s*=\\s*");
 					if (keyVal.length != 2)
 						continue;
 
 					sb.append(" devtype " + keyVal[1]);
 				}
 			}
 			System.out.println(sb.toString());
 		}
 	}
 }
