 package iie.mm.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 import java.util.TimerTask;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Pipeline;
 import redis.clients.jedis.Tuple;
 import redis.clients.jedis.exceptions.JedisException;
 
 public class ProfileTimerTask extends TimerTask {
 	private ServerConf conf;
 	public int period;
 	private double lastWn = 0;
 	private long lastDl = 0;
 	private long lastDnr = 0;
 	private long lastTs = System.currentTimeMillis();
 	private String profileDir = "log/";
 	private Jedis jedis;
 	private String hbkey;
 	
 	public ProfileTimerTask(ServerConf conf, int period) throws JedisException {
 		super();
 		this.conf = conf;
 		File dir = new File(profileDir);
 		if (!dir.exists())
 			dir.mkdirs();
 		
 		// 向redis的数据库1中插入心跳信息
 		jedis = new RedisFactory(conf).getDefaultInstance();
 		if (jedis == null)
 			throw new JedisException("Get default jedis instance failed.");
 		
 		hbkey = "mm.hb." + conf.getNodeName() + ":" + conf.getServerPort();
 		Pipeline pi = jedis.pipelined();
 		pi.set(hbkey, "1");
 		pi.expire(hbkey, period + 5);
 		pi.sync();
 
 		// determine the ID of ourself, register ourself
 		String self = conf.getNodeName() + ":" + conf.getServerPort();
 		Long sid;
 		if (jedis.zrank("mm.active", self) == null) {
 			sid = jedis.incr("mm.next.serverid");
 			// FIXME: if two server start with the same port, fail!
 			jedis.zadd("mm.active", sid, self);
 		}
 		// reget the sid
 		sid = jedis.zscore("mm.active", self).longValue();
 		ServerConf.serverId = sid;
 		System.out.println("Got ServerID " + sid + " for Server " + self);
 		
 		// use the same serverID to register in mm.active.http
 		self = conf.getNodeName() + ":" + conf.getHttpPort();
 		jedis.zadd("mm.active.http", sid, self);
 		System.out.println("Register HTTP server " + self + " done.");
 		
 		Set<Tuple> active = jedis.zrangeWithScores("mm.active.http", 0, -1);
 		if (active != null && active.size() > 0) {
 			for (Tuple t : active) {
 				ServerConf.servers.put((long)t.getScore(), t.getElement());
 				System.out.println("Got HTTP Server " + (long)t.getScore() + " " + t.getElement());
 			}
 		}
 		this.period = period;
 	}
 
 	@Override
 	public void run() {
 		long cur = System.currentTimeMillis();
 		double wn = ServerProfile.writtenBytes.longValue() / 1024.0;			//单位转换成KB
 		double bw = (wn - lastWn) / ((cur - lastTs) / 1000.0);
 		long dnr = ServerProfile.readN.longValue();
 		long dl = ServerProfile.readDelay.longValue();
 		
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String s = df.format(new Date());
 		String info = s + " avg write bandwidth "+ bw + " KB/s";
 		
 		if ((dl - lastDl) == 0)
 			info += ", no read requests.";
 		else
 			info += ", avg read latency " + (double)(dl - lastDl) / (dnr - lastDnr) + " ms";
 		System.out.println(info);
 		
 		lastWn = wn;
 		lastDnr = dnr;
 		lastDl = dl;
 		lastTs = cur;
 		
 		// 发送server的心跳信息
 		try {
 			if (jedis == null)
 				jedis = new RedisFactory(conf).getDefaultInstance();
 			if (jedis == null)
 				info += ", redis down?";
			else {
				Pipeline pi = jedis.pipelined();
				pi.set(hbkey, "1");
				pi.expire(hbkey, period + 5);
				pi.sync();
			}
 			// update server list
 			Set<Tuple> active = jedis.zrangeWithScores("mm.active.http", 0, -1);
 			if (active != null && active.size() > 0) {
 				for (Tuple t : active) {
 					ServerConf.servers.put((long)t.getScore(), t.getElement());
 				}
 			}
 		} catch (Exception e) {
			jedis = RedisFactory.putBrokenInstance(jedis);
 		}
 
 		//把统计信息写入文件,每一天的信息放在一个文件里
 		String profileName = s.substring(0, 10)+".log";
 		PrintWriter pw = null;
 		try {
 			//追加到文件尾
 			pw = new PrintWriter(new FileOutputStream(profileDir + profileName, true));
 			pw.println(info);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			if(pw != null)
 				pw.close();
 		}
 	}
 }
