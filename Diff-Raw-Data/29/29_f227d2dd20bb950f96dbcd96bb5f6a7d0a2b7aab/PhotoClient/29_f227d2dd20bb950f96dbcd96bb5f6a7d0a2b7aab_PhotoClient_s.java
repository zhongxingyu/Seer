 package iie.mm.client;
 
 import iie.mm.client.ClientConf.RedisInstance;
 import iie.mm.client.PhotoClient.SocketHashEntry.SEntry;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 import redis.clients.jedis.exceptions.JedisException;
 
 public class PhotoClient {
 	private ClientConf conf;
 	private RedisFactory rf;
 	
 	public RedisFactory getRf() {
 		return rf;
 	}
 	
 	//缓存与服务端的tcp连接,服务端名称到连接的映射
 	private Map<Long, String> socketKeyHash = new HashMap<Long, String>();
 	public static class SocketHashEntry {
 		String hostname;
 		int port, cnr;
 		AtomicInteger xnr = new AtomicInteger(0);
 		Map<Long, SEntry> map;
 		AtomicLong nextId = new AtomicLong(0);
 		
 		public static class SEntry {
 			public Socket sock;
 			public long id;
 			public boolean used;
 			public DataInputStream dis;
 			public DataOutputStream dos;
 			
 			public SEntry(Socket sock, long id, boolean used, DataInputStream dis, DataOutputStream dos) {
 				this.sock = sock;
 				this.id = id;
 				this.used = used;
 				this.dis = dis;
 				this.dos = dos;
 			}
 		}
 		
 		public SocketHashEntry(String hostname, int port, int cnr) {
 			this.hostname = hostname;
 			this.port = port;
 			this.cnr = cnr;
 			this.map = new ConcurrentHashMap<Long, SEntry>();
 		}
 		
 		public void setFreeSocket(long id) {
 			SEntry e = map.get(id);
 			if (e != null) {
 				e.used = false;
 			}
 			synchronized (this) {
 				this.notify();
 			}
 		}
 		
 		public boolean probSelected() {
 			if (map.size() > 0)
 				return true;
 			else {
 				// 1/100 prob selected
 				if (new Random().nextInt(100) == 0)
 					return true;
 				else 
 					return false;
 			}
 		}
 		
 		public long getFreeSocket() throws IOException {
 			boolean found = false;
 			long id = -1;

 			do {
 				synchronized (this) {
 					for (SEntry e : map.values()) {
 						if (!e.used) {
 							// ok, it is unused
 							found = true;
 							e.used = true;
 							id = e.id;
 							break;
 						}
 					}
 				}
 	
 				if (!found) {
 					if (map.size() + xnr.get() < cnr) {
 						// do connect now
 						Socket socket = new Socket();
 						xnr.getAndIncrement();
 						try {
 							socket.connect(new InetSocketAddress(this.hostname, this.port));
 							socket.setTcpNoDelay(true);
 							id = this.addToSocketsAsUsed(socket, new DataInputStream(socket.getInputStream()), 
 										new DataOutputStream(socket.getOutputStream()));
 							System.out.println("New connection @ " + id + " for " + hostname + ":" + port);
 						} catch (SocketException e) {
 							xnr.getAndDecrement();
 							System.out.println("Connect to " + hostname + ":" + port + " failed.");
 							try {
 								Thread.sleep(1000);
 							} catch (InterruptedException e1) {
 							}
							throw e;
 						} catch (Exception e) {
 							xnr.getAndDecrement();
 							System.out.println("Connect to " + hostname + ":" + port + " failed w/ " + e.getMessage());
 							try {
 								Thread.sleep(1000);
 							} catch (InterruptedException e1) {
 							}
 							throw new IOException(e.getMessage());
 						}
 						xnr.getAndDecrement();
 					} else {
 						do {
 							try {
 								synchronized (this) {
 									//System.out.println("wait ...");
									this.wait();
 								}
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 								continue;
 							}
 							break;
 						} while (true);
 					}
 				} else {
 					break;
 				}
 			} while (id == -1);
 			
 			return id;
 		}
 		
 		public long addToSocketsAsUsed(Socket sock, DataInputStream dis, DataOutputStream dos) {
 			SEntry e = new SEntry(sock, nextId.getAndIncrement(), true, dis, dos);
 			synchronized (this) {
 				map.put(e.id, e);
 			}
 			return e.id;
 		}
 		
 		public void addToSockets(Socket sock, DataInputStream dis, DataOutputStream dos) {
 			SEntry e = new SEntry(sock, nextId.getAndIncrement(), false, dis, dos);
 			synchronized (this) {
 				map.put(e.id, e);
 			}
 		}
 		
 		public void useSocket(long id) {
 			synchronized (this) {
 				SEntry e = map.get(id);
 				if (e != null) {
 					e.used = true;
 				}
 			}
 		}
 		
 		public void delFromSockets(long id) {
 			System.out.println("Del sock @ " + id + " for " + hostname + ":" + port);
 			SEntry e = null;
 			synchronized (this) {
 				e = map.get(id);
 				map.remove(id);
 			}
 			if (e != null) {
 				try {
 					e.dis.close();
 					e.dos.close();
 					e.sock.close();
 				} catch (IOException e1) {
 				}
 			}
 		}
 	};
 	
 	private Map<String, SocketHashEntry> socketHash = new HashMap<String, SocketHashEntry>();
 	private Map<Long, String> servers = new ConcurrentHashMap<Long, String>();
 	private final ThreadLocal<Jedis> jedis =
          new ThreadLocal<Jedis>() {
              @Override protected Jedis initialValue() {
                  return null;
          }
 	};
 	
 	public PhotoClient(){
 		conf = new ClientConf();
 		rf = new RedisFactory(conf);
 	}
 
 	public PhotoClient(ClientConf conf) {
 		this.conf = conf;
 		rf = new RedisFactory(conf);
 		RedisInstance ri = conf.getRedisInstance();
 		if (ri != null)
 			jedis.set(rf.getNewInstance(ri));
 	}
 	
 	public ClientConf getConf() {
 		return conf;
 	}
 	public void setConf(ClientConf conf) {
 		this.conf = conf;
 	}
 	
 	public void addToServers(long id, String server) {
 		servers.put(id, server);
 	}
 	
 	public Map<String, SocketHashEntry> getSocketHash() {
 		return socketHash;
 	}
 	public void setSocketHash(Map<String, SocketHashEntry> socketHash) {
 		this.socketHash = socketHash;
 	}
 	
 	public Map<Long, String> getSocketKeyHash() {
 		return socketKeyHash;
 	}
 
 	public void setSocketKeyHash(Map<Long, String> socketKeyHash) {
 		this.socketKeyHash = socketKeyHash;
 	}
 
 	public Jedis getJedis() {
 		return jedis.get();
 	}
 	
 	public void setJedis(Jedis jedis) {
 		this.jedis.set(jedis);
 	}
 	
 	public void refreshJedis() throws IOException {
 		synchronized (this) {
 			if (jedis.get() == null) {
 				RedisInstance ri = conf.getRedisInstance();
 				jedis.set(rf.getNewInstance(ri));
 			}
 		}
 		if (jedis.get() == null)
 			throw new IOException("Invalid Jedis connection.");
 	}
 	
 	private byte[] __handleInput(DataInputStream dis) throws IOException {
 		int count;
 		
 		synchronized (dis) {
 			count = dis.readInt();
 			switch (count) {
 			case -1:
 				return null;
 			default:
 				return readBytes(count, dis);
 			}
 		}
 	}
 	
 	private String __syncStorePhoto(String set, String md5, byte[] content, SocketHashEntry she) throws IOException {
 		long id = she.getFreeSocket();
 		if (id == -1)
 			throw new IOException("Could not find free socket for server: " + she.hostname + ":" + she.port);
 		DataOutputStream storeos = she.map.get(id).dos;
 		DataInputStream storeis = she.map.get(id).dis;
 		
 		//action,set,md5,content的length写过去
 		byte[] header = new byte[4];
 		header[0] = ActionType.SYNCSTORE;
 		header[1] = (byte) set.length();
 		header[2] = (byte) md5.length();
 		
 		byte[] r = null;
 		
 		try {
 			synchronized (storeos) {
 				storeos.write(header);
 				storeos.writeInt(content.length);
 				
 				//set,md5,content的实际内容写过去
 				storeos.write(set.getBytes());
 				storeos.write(md5.getBytes());
 				storeos.write(content);
 				storeos.flush();
 			}
 			r = __handleInput(storeis);
 			she.setFreeSocket(id);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// remove this socket do reconnect?
 			she.delFromSockets(id);
 		}
 		
 		if (r == null) {
 			String rr = null;
 			try {
 				rr = jedis.get().hget(set, md5);
 			} catch (JedisConnectionException e) {
 				System.out.println("Jedis connection broken, wait ...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 				jedis.set(rf.putBrokenInstance(jedis.get()));
 			} catch (JedisException e) {
 				jedis.set(rf.putBrokenInstance(jedis.get()));
 			}
 			if (rr == null)
 				throw new IOException("Metadata inconsistent or connection broken?");
 			return rr;
 		}
 		
 		String s = new String(r, "US-ASCII");
 		
 		if (s.startsWith("#FAIL:")) {
 			throw new IOException("MM server failure: " + s);
 		}
 		return s;
 	}
 	
 	private void __asyncStorePhoto(String set, String md5, byte[] content, SocketHashEntry she) throws IOException {
 		long id = she.getFreeSocket();
 		if (id == -1)
 			throw new IOException("Could not get free socket for server: " + she.hostname + ":" + she.port);
 		DataOutputStream storeos = she.map.get(id).dos;
 		
 		//action,set,md5,content的length写过去
 		byte[] header = new byte[4];
 		header[0] = ActionType.ASYNCSTORE;
 		header[1] = (byte) set.length();
 		header[2] = (byte) md5.length();
 
 		try {
 			synchronized (storeos) {
 				storeos.write(header);
 				storeos.writeInt(content.length);
 				
 				//set,md5,content的实际内容写过去
 				storeos.write(set.getBytes());
 				storeos.write(md5.getBytes());
 				storeos.write(content);
 				storeos.flush();
 			}
 			she.setFreeSocket(id);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// remove this socket do reconnect?
 			she.delFromSockets(id);
 		}
 	}
 	
 	/**
 	 * 同步写
 	 * @param set
 	 * @param md5
 	 * @param content
 	 * @param sock
 	 * @return		
 	 */
 	public String syncStorePhoto(String set, String md5, byte[] content, SocketHashEntry she, boolean nodedup) throws IOException {
 		if (conf.getMode() == ClientConf.MODE.NODEDUP || nodedup) {
 			return __syncStorePhoto(set, md5, content, she);
 		} else if (conf.getMode() == ClientConf.MODE.DEDUP) {
 			String info = null;
 			int err = 0;
 			
 			refreshJedis();
 			try {
 				info = jedis.get().hget(set, md5);
 			} catch (JedisConnectionException e) {
 				System.out.println("Jedis connection broken, wait ...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 				err = -1;
 			} catch (JedisException e) {
 				err = -1;
 			} finally {
 				if (err < 0)
 					jedis.set(rf.putBrokenInstance(jedis.get()));
 				else
 					jedis.set(rf.putInstance(jedis.get()));
 			}
 		
 			if (info == null) {
 				return __syncStorePhoto(set, md5, content, she);
 			} else {
 				// NOTE: the delete unit is SET, thus, do NOT need reference 
 				//System.out.println(set + "." + md5 + " exists in MM server");
 				//jedis.hincrBy(set, "r." + md5, 1);
 				
 				return info;
 			}
 		}
 		throw new IOException("Invalid Operation Mode.");
 	}
 	
 	public void asyncStorePhoto(String set, String md5, byte[] content, SocketHashEntry she) throws IOException {
 		if (conf.getMode() == ClientConf.MODE.NODEDUP) {
 			__asyncStorePhoto(set, md5, content, she);
 		} else if (conf.getMode() == ClientConf.MODE.DEDUP) {
 			String info = null;
 			int err = 0;
 			
 			refreshJedis();
 			try {
 				info = jedis.get().hget(set, md5);
 			} catch (JedisConnectionException e) {
 				System.out.println("Jedis connection broken, wait ...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 				err = -1;
 			} catch (JedisException e) {
 				err = -1;
 			} finally {
 				if (err < 0)
 					jedis.set(rf.putBrokenInstance(jedis.get()));
 				else
 					jedis.set(rf.putInstance(jedis.get()));
 			}
 
 			if (info == null) {
 				__asyncStorePhoto(set, md5, content, she);
 			} /* else { 
 				// FIXME: this should increase reference in Server.
 				System.out.println(set + "." + md5 + " exists in redis server");
 				jedis.hincrBy(set, "r." + md5, 1);
 
 				return info;
 			}*/
 		} else {
 			throw new IOException("Invalid Operation Mode.");
 		}
 	}
 	
 	//批量存储时没有判断重复
 	public String[] mPut(String set, String[] md5s, byte[][] content, SocketHashEntry she) throws IOException
 	{
 		long id = she.getFreeSocket();
 		if (id == -1)
 			throw new IOException("Could not find free socket for server: " + she.hostname + ":" + she.port);
 		DataOutputStream dos = she.map.get(id).dos;
 		DataInputStream dis = she.map.get(id).dis;
 		
 		int n = md5s.length;
 		String[] r = new String[n];
 		byte[] header = new byte[4];
 		header[0] = ActionType.MPUT;
 		header[1] = (byte) set.length();
 		try{
 			synchronized (dos) {
 				
 				dos.write(header);
 				dos.writeInt(n);
 				dos.write(set.getBytes());
 				for(int i = 0;i<n;i++)
 				{
 					dos.writeInt(md5s[i].getBytes().length);
 					dos.write(md5s[i].getBytes());
 				}
 				for(int i = 0; i<n;i++)
 					dos.writeInt(content[i].length);
 				for(int i = 0; i<n;i++)
 					dos.write(content[i]);
 			}
 			synchronized (dis){
 				
 				int count = dis.readInt();
 				if (count == -1)
 					throw new IOException("MM server failure." );
 				r[0] = new String(readBytes(count, dis));
 				for(int i = 1;i<n;i++)
 					r[i] = new String(readBytes(dis.readInt(), dis));
 			}
 			she.setFreeSocket(id);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// remove this socket do reconnect?
 			she.delFromSockets(id);
 		}
 		return r;
 	}
 	
 	/**
 	 * 同步取
 	 * @param set	redis中的键以set开头,因此读取图片要加上它的集合名
 	 * @param md5	
 	 * @return		图片内容,如果图片不存在则返回长度为0的byte数组
 	 */
 	public byte[] getPhoto(String set, String md5) throws IOException {
 		String info = null;
 		int err = 0;
 		
 		refreshJedis();
 		try {
 			info = jedis.get().hget(set, md5);
 		} catch (JedisConnectionException e) {
 			System.out.println("Jedis connection broken, wait in getObject ...");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e1) {
 			}
 			err = -1;
 		} catch (JedisException e) {
 			err = -1;
 		} finally {
 			if (err < 0)
 				jedis.set(rf.putBrokenInstance(jedis.get()));
 			else
 				jedis.set(rf.putInstance(jedis.get()));
 		}
 		
 		if (info == null) {
 			throw new IOException(set + "@" + md5 + " doesn't exist in MMM server or connection broken.");
 		} else {
 			return searchPhoto(info);
 		}
 	}
 	
 	/**
 	 * 异步取
 	 * @param set	redis中的键以set开头,因此读取图片要加上它的集合名
 	 * @param md5	
 	 * @return		图片内容,如果图片不存在则返回长度为0的byte数组
 	 */
 	/*
 	public int iGetPhoto(String set, String md5) throws IOException {
 		String info = jedis.hget(set, md5);//拿到所有的元信息
 		if(info == null) {
 			throw new IOException(set + "." + md5 + " doesn't exist in redis server.");
 		} else {
 //			System.out.println(info);
 			return iSearchByInfo(info);
 		}
 	}
 	*/
     /**
 	 * infos是拼接的元信息，各个元信息用#隔开
 	 */
 	public byte[] searchPhoto(String infos) throws IOException {
 		byte[] r = null;
 
 		for (String info : infos.split("#")) {
 			try {
 				String[] si = info.split("@");
 				
 				r = searchByInfo(info, si);
 				if (r.length >= 0)
 					break;
 			} catch(IOException e){
 				e.printStackTrace();
 				continue;
 			}
 		}
 		if (r == null)
 			throw new IOException("Failed to search MM object.");
 		return r;
 	}
 	
 	/**
 	 * info是一个文件的元信息，没有拼接的
 	 */
 	public byte[] searchByInfo(String info, String[] infos) throws IOException {
 		if (infos.length != 7) {
 			throw new IOException("Invalid INFO string, info length is " + infos.length);
 		}
 		
 		SocketHashEntry searchSocket = null;
 		String server = servers.get(Long.parseLong(infos[2]));
 		if (server == null)
 			throw new IOException("Server idx " + infos[2] + " can't be resolved.");
 		if (socketHash.containsKey(server)) {
 			searchSocket = socketHash.get(server);
 		} else {
 			String[] s = server.split(":");
 			if (s.length == 2) {
 				Socket socket = new Socket(); 
 				socket.connect(new InetSocketAddress(s[0], Integer.parseInt(s[1])));
 				socket.setTcpNoDelay(true);
 				searchSocket = new SocketHashEntry(s[0], Integer.parseInt(s[1]), conf.getSockPerServer());
 				searchSocket.addToSocketsAsUsed(socket, 
 						new DataInputStream(socket.getInputStream()), 
 						new DataOutputStream(socket.getOutputStream()));
 				socketHash.put(server, searchSocket);
 			} else 
 				throw new IOException("Invalid server name or port.");
 		}
 
 		//action,info的length写过去
 		byte[] header = new byte[4];
 		header[0] = ActionType.SEARCH;
 		header[1] = (byte) info.getBytes().length;
 		long id = searchSocket.getFreeSocket();
 		if (id == -1)
 			throw new IOException("Could not get free socket for server " + server);
 
 		byte[] r = null;
 		try {
 			searchSocket.map.get(id).dos.write(header);
 			
 			//info的实际内容写过去
 			searchSocket.map.get(id).dos.writeBytes(info);
 			searchSocket.map.get(id).dos.flush();
 	
 			r = __handleInput(searchSocket.map.get(id).dis);
 			searchSocket.setFreeSocket(id);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// remove this socket do reconnect?
 			searchSocket.delFromSockets(id);
 		}
 		if (r == null)
 			throw new IOException("Internal error in mm server:" + server);
 		else
 			return r;
 	}
 	/*
 	public int iSearchByInfo(String info) throws IOException {
 		String[] str = info.split("#");
 		for(String s : str){
 			String[] infos = s.split("@");
 			Socket searchSocket = null;
 			if (socketHash.containsKey(infos[2] + ":" + infos[3])){
 				searchSocket = socketHash.get(infos[2] + ":" + infos[3]);
 			}
 			else {
 				// 读取图片时所用的socket
 				searchSocket = new Socket(); 
 				searchSocket.connect(new InetSocketAddress(infos[2], Integer.parseInt(infos[3])));
 				searchSocket.setTcpNoDelay(true);
 				socketHash.put(infos[2] + ":" + infos[3], searchSocket);
 			}
 			DataOutputStream searchos = new DataOutputStream(searchSocket.getOutputStream());
 
 			//action,info的length写过去
 			byte[] header = new byte[4];
 			header[0] = ActionType.IGET;
 			header[1] = (byte) s.getBytes().length;
 			searchos.write(header);
 			searchos.writeLong(id);
 			//info的实际内容写过去
 			searchos.write(s.getBytes());
 			searchos.flush();
 		}
 		return 1;
 
 	}
 	*/
 	/**
 	 * 通过keys异步取多媒体
 	 * @param count
 	 * @return
 	 */
 	/*
 	public Map<String, byte[]> wait(Set<String> keys) throws IOException {
 		Map<String, byte[]> medias = new HashMap<String, byte[]>();
 		for(String key : keys){
 			String[] infos = key.split("@");
 			Socket searchSocket = null;
 			if (socketHash.containsKey(infos[2] + ":" + infos[3])){
 //				System.out.println(infos[2] + ":" + infos[3]);
 				searchSocket = socketHash.get(infos[2] + ":" + infos[3]);
 				DataInputStream iSearchis = new DataInputStream(searchSocket.getInputStream());
 				Long id = iSearchis.readLong();
 //				System.out.println(id);
 				String info = socketKeyHash.get(id);
 //				System.out.println(info);
 				int count = iSearchis.readInt();					
 				if (count >= 0) {
 					medias.put(info, readBytes(count, iSearchis));
 				} else {
 					throw new IOException("Internal error in mm server.");
 				}
 			}else{
 				throw new IOException("no socket is cached for node:"+infos[2] + ":" + infos[3]);
 			}
 		}
 		return medias;
 	}
 	*/
 	/**
 	 * 从输入流中读取count个字节
 	 * @param count
 	 * @return
 	 */
 	private byte[] readBytes(int count, InputStream istream) throws IOException {
 		byte[] buf = new byte[count];			
 		int n = 0;
 		
 		while (count > n) {
 			n += istream.read(buf, n, count - n);
 		}
 		
 		return buf;
 	}
 	
 	/**
 	 * 关闭流、套接字和与redis的连接
 	 * 用于读和写的套接字全部都关闭
 	 */
 	public void close() {
 		try {
 			for (SocketHashEntry s : socketHash.values()){
 				for (SEntry e : s.map.values()) {
 					e.sock.close();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	public Map<String, String> getNrFromSet(String set) throws IOException {
 		int err = 0;
 		
 		refreshJedis();
 		try {
 			return jedis.get().hgetAll(set);
 		} catch (JedisConnectionException e) {
 			System.out.println("Jedis connection broken, wait ...");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e1) {
 			}
 			err = -1;
 		} catch (JedisException e) {
 			err = -1;
 		} finally {
 			if (err < 0)
 				jedis.set(rf.putBrokenInstance(jedis.get()));
 			else
 				jedis.set(rf.putInstance(jedis.get()));
 		}
 		throw new IOException("Jedis Connection broken.");
 	}
 	
 }
