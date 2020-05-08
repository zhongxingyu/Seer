 package iie.mm.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 import java.util.Random;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Transaction;
 import redis.clients.jedis.Response;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 import redis.clients.jedis.exceptions.JedisException;
 
 public class StorePhoto {
 	private ServerConf conf;
 	private String localHostName;
 	private int serverport;							//本机监听的端口,在这里的作用就是构造存图片时返回值
 	private String destRoot = "./mm_data/";
 	private Set<String> storeArray = new HashSet<String>();
 	private String[] diskArray;						//代表磁盘的数组
 	private long blocksize;							//文件块的大小，单位是B
 	
 	//一级的hash,集合 + 磁盘->上下文
 	//不能放在构造函数里初始化,不然会每次创建一个storephoto类时,它都被初始化一遍
 	private static Map<String, StoreSetContext> writeContextHash = new ConcurrentHashMap<String, StoreSetContext>();		
 	private Map<String, RandomAccessFile> readRafHash;			//读文件时的随机访问流，用哈希来缓存
 	private Jedis jedis;
 	
 	private TimeLimitedCacheMap lookupCache = new TimeLimitedCacheMap(10, 60, 300, TimeUnit.SECONDS);
 	
 	public static class RedirectException extends Exception {
 		/**
 		 * serialVersionUID
 		 */
 		private static final long serialVersionUID = 3092261885247728350L;
 		public long serverId;
 		public String info;
 	
 		public RedirectException(long serverId, String info) {
 			this.serverId = serverId;
 			this.info = info;
 		}
 	}
 
 	public class StoreSetContext {
 		public String key;
 		public String disk;
 		
 		//当前可写的块
 		private long curBlock = -1;
 		private long offset = 0;
 		//代表要写的块的文件
 		private File newf = null;
 		//写当前的块的随机访问流
 		private RandomAccessFile raf = null;
 		
 		private String path = null;
 		
 		public StoreSetContext(String set, String disk) {
 			// 根据set和md5构造存储的路径
 			StringBuffer sb = new StringBuffer();
 			sb.append(disk);
 			sb.append("/");
 			sb.append(destRoot);
 			sb.append(set);
 			sb.append("/");
 			path = sb.toString();
 			//存储文件的文件夹的相对路径，不包含文件名
 			File dir = new File(path);
 			if (!dir.exists()) 
 				dir.mkdirs();
 			
 			this.key = set + ":" + disk;
 			this.disk = disk;
 		}
 	}
 	
 	public StorePhoto(ServerConf conf) {
 		this.conf = conf;
 		serverport = conf.getServerPort();
 		blocksize = conf.getBlockSize();
 		storeArray = conf.getStoreArray();
 		if (storeArray.size() == 0) {
 			storeArray.add(".");
 		}
 		diskArray = storeArray.toArray(new String[0]);
 		//jedis = new RedisFactory(conf).getDefaultInstance();
 
 		localHostName = conf.getNodeName();
 		readRafHash = new ConcurrentHashMap<String, RandomAccessFile>();
 	}
 	
 	public void reconnectJedis() throws IOException {
 		if (jedis == null) {
 			jedis = new RedisFactory(conf).getDefaultInstance();
 		}
 		if (jedis == null)
 			throw new IOException("Connection to MMM Server failed.");
 	}
 
 	/**
 	 * 把content代表的图片内容,存储起来,把小图片合并成一个块,块大小由配置文件中blocksize指定.
 	 * 文件存储在destRoot下，然后按照set分第一层子目录
 	 * @param set	集合名
 	 * @param md5	文件的md5
 	 * @param content	文件内容
 	 * @return		type@set@serverid@block@offset@length@disk,这几个信息通过redis存储,分别表示元信息类型,该图片所属集合,所在节点,
 	 * 				节点的端口号,所在相对路径（包括完整文件名）,位于所在块的偏移的字节数，该图片的字节数,磁盘
 	 */
 	public String storePhoto(String set, String md5, byte[] content, int coff, 
 			int clen) {
 		String returnStr = "+FAIL: unknown error.";
 		int err = 0;
 		
 		try {
 			reconnectJedis();
 		} catch (IOException e2) {
 			return "+FAIL: MMM Server can not be reached.";
 		}
 		StringBuffer rVal = new StringBuffer(128);
 		
 		//随机选一个磁盘
 		int diskid = new Random().nextInt(diskArray.length);
 		StoreSetContext ssc = null;
 		synchronized (writeContextHash) {
 			ssc = writeContextHash.get(set + ":" + diskArray[diskid]);
 			
 			if (ssc == null) {
 				ssc = new StoreSetContext(set, diskArray[diskid]);
 				writeContextHash.put(ssc.key, ssc);
 			}
 		}
 		synchronized (ssc) {
 			//找到当前可写的文件块,如果当前不够大,或不存在,则新创建一个,命名block＿id,id递增,redis中只存储id
 			//用curBlock缓存当前可写的块，减少查询jedis的次数
 		
 			try {
 				if (ssc.curBlock < 0) {
 					//需要通过节点名字来标示不同节点上相同名字的集合
 					String reply = jedis.get(set + ".blk." + localHostName + "." + ssc.disk);
 					if (reply != null) {
 						ssc.curBlock = Long.parseLong(reply);
 						ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 						ssc.offset = ssc.newf.length();
 					} else {
 						ssc.curBlock = 0;
 						ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 						//把集合和它所在节点记录在redis的set里,方便删除,set.srvs表示set所在的服务器的位置
 						jedis.sadd(set + ".srvs", localHostName + ":" + serverport);
 						jedis.set(set + ".blk." + localHostName + "." + ssc.disk, "" + ssc.curBlock);
 						ssc.offset = 0;
 					}
 					ssc.raf = new RandomAccessFile(ssc.newf, "rw");
 					ssc.raf.seek(ssc.offset);
 				}
 				if (ssc.offset + content.length > blocksize) {
 					ssc.curBlock++;
 					ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 					//如果换了一个新块,则先把之前的关掉
 					if(ssc.raf != null)
 						ssc.raf.close();
 					ssc.raf = new RandomAccessFile(ssc.newf, "rw");
 					//当前可写的块号加一
 					jedis.incr(set + ".blk." + localHostName + "." + ssc.disk);
 					ssc.offset = 0;
 				}
 				
 				//在每个文件前面写入它的md5和offset length，从而恢复元数据
 				//md5 32个字节，offset:length分配20个字节
 //				ssc.offset += 52;
 				// 统计写入的字节数
 				ServerProfile.addWrite(clen);
 				// 构造返回值
 				rVal.append("1@"); // type
 				rVal.append(set);
 				rVal.append("@");
 				rVal.append(ServerConf.serverId);
 				rVal.append("@");
 				rVal.append(ssc.curBlock);
 				rVal.append("@");
 				rVal.append(ssc.offset);
 				rVal.append("@");
 				rVal.append(clen);
 				rVal.append("@");
 				//磁盘,现在存的是磁盘的名字,读取的时候直接拿来构造路径
 				rVal.append(diskArray[diskid]);
 				
 				ssc.raf.write(content, coff, clen);
 	
 				ssc.offset += clen;
 			} catch (JedisConnectionException e) {
 				System.out.println("Jedis connection broken in storeObject.");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 				err = -1;
 				returnStr = "#FAIL:" + e.getMessage();
 			} catch (JedisException e) {
 				err = -1;
 				returnStr = "#FAIL:" + e.getMessage();
 			} catch (Exception e) {
 				err = -1;
 				returnStr = "#FAIL:" + e.getMessage();
 			} finally {
 				if (err < 0) {
 					jedis = RedisFactory.putBrokenInstance(jedis);
 					return returnStr;
 				}
 			}
 		}
 		
 		try {
			String returnVal = rVal.toString();
 			Transaction t1 = jedis.multi();
 			
			Response<Long> r1 = t1.hsetnx(set, md5, returnVal);
 			Response<String> r2 = t1.hget(set,md5);
 			t1.exec();
 			if (r1.get() != 1) {
				returnVal = r2.get() + "#" + returnVal;
				jedis.hset(set, md5, returnVal);
 				/*
 				 * Concurrent modify:
 				 * 
 				 * SETNX set@md5 aaa
 				 * WATCH set@md5
 				 * R = HGET set@md5
 				 * R += returnVal
 				 * MULTI
 				 * HSET set md5 R
 				 * DEL set@md5
 				 * EXEC
 				 */
 			}
 		} catch (JedisConnectionException e) {
 			System.out.println("Jedis connection broken in storeObject.");
 			e.printStackTrace();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e1) {
 			}
 			err = -1;
 			returnStr = "#FAIL:" + e.getMessage();
 		} catch (JedisException e) {
 			err = -1;
 			returnStr = "#FAIL:" + e.getMessage();
 		} catch (Exception e) {
 			e.printStackTrace();
 			err = -1;
 			returnStr = "#FAIL:" + e.getMessage();
 		} finally {
 			if (err < 0) {
 				jedis = RedisFactory.putBrokenInstance(jedis);
 			} else
 				jedis = RedisFactory.putInstance(jedis);
 		}
 		return returnStr;
 	}
 	
 	/**
 	 * 
 	 * @param set
 	 * @param md5
 	 * @param content
 	 * @return 出现任何错误返回null，出现错误的话不知道哪些存储成功，哪些不成功
 	 */
 	public String[] mstorePhoto(String set, String[] md5, byte[][] content) {
 		try {
 			reconnectJedis();
 		} catch (IOException e2) {
 			e2.printStackTrace();
 			return null;
 		}
 
 		if (!(md5.length == content.length && md5.length == content.length)) {
 			System.out.println("Array lengths in arguments mismatch.");
 			return null;
 		}
 		String[] returnVal = new String[content.length];
 		int diskid = new Random().nextInt(diskArray.length);
 		StoreSetContext ssc = null;
 		synchronized (writeContextHash) {
 			ssc = writeContextHash.get(set + ":" + diskArray[diskid]);
 
 			if (ssc == null) {
 				ssc = new StoreSetContext(set, diskArray[diskid]);
 				writeContextHash.put(ssc.key, ssc);
 			}
 		}
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		Map<String, String> mcs = new HashMap<String, String>();
 		synchronized (ssc) {
 			for (int i = 0; i < content.length; i++) {
 				StringBuffer rVal = new StringBuffer(128);
 
 				try {
 					if (ssc.curBlock < 0) {
 						// 需要通过节点名字来标示不同节点上相同名字的集合
 						String reply = jedis.get(set + ".blk." + localHostName
 								+ "." + ssc.disk);
 						if (reply != null) {
 							ssc.curBlock = Long.parseLong(reply);
 							ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 							ssc.offset = ssc.newf.length();
 						} else {
 							ssc.curBlock = 0;
 							ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 							// 把集合和它所在节点记录在redis的set里,方便删除,set.srvs表示set所在的服务器的位置
 							jedis.sadd(set + ".srvs", localHostName + ":"
 									+ serverport);
 							jedis.set(set + ".blk." + localHostName + "."
 									+ ssc.disk, "" + ssc.curBlock);
 							ssc.offset = 0;
 						}
 						ssc.raf = new RandomAccessFile(ssc.newf, "rw");
 						ssc.raf.seek(ssc.offset);
 
 					}
 
 					if (ssc.offset + content[i].length > blocksize) {
 						ssc.curBlock++;
 						ssc.newf = new File(ssc.path + "b" + ssc.curBlock);
 						// 如果换了一个新块,先把之前的写进去，然后再关闭流
 						if (ssc.raf != null) {
 							ssc.raf.write(baos.toByteArray());
 							ssc.raf.close();
 							baos.reset();
 						}
 						ssc.raf = new RandomAccessFile(ssc.newf, "rw");
 						// 当前可写的块号加一
 						jedis.incr(set + ".blk." + localHostName + "."
 								+ ssc.disk);
 						ssc.offset = 0;
 					}
 
 					// 在每个文件前面写入它的md5和offset length，从而恢复元数据
 					// md5 32个字节，offset:length分配20个字节
 					// ssc.offset += 52;
 					// 统计写入的字节数
 					ServerProfile.addWrite(content[i].length);
 					// 构造返回值
 					rVal.append("1@"); // type
 					rVal.append(set);
 					rVal.append("@");
 					rVal.append(ServerConf.serverId);
 					rVal.append("@");
 					rVal.append(ssc.curBlock);
 					rVal.append("@");
 					rVal.append(ssc.offset);
 					rVal.append("@");
 					rVal.append(content[i].length);
 					rVal.append("@");
 					// 磁盘,现在存的是磁盘的名字,读取的时候直接拿来构造路径
 					rVal.append(diskArray[diskid]);
 
 					// ssc.raf.write(content, coff, clen);
 					baos.write(content[i]);
 					returnVal[i] = rVal.toString();
 					ssc.offset += content[i].length;
 
 					Transaction t1 = jedis.multi();
 					Response<Long> r1 = t1.hsetnx(set, md5[i], returnVal[i]);
 					Response<String> r2 = t1.hget(set, md5[i]);
 					t1.exec();
 					if (r1.get() == 0) {
 						returnVal[i] = r2.get() + "#" + returnVal[i];
 						mcs.put(md5[i], returnVal[i]);
 					}
 
 				} catch (JedisConnectionException e) {
 					System.out
 							.println("Jedis connection broken in storeObject.");
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e1) {
 					}
 					jedis = RedisFactory.putBrokenInstance(jedis);
 					return null;
 				} catch (JedisException e) {
 					jedis = RedisFactory.putBrokenInstance(jedis);
 					return null;
 				} catch (Exception e) {
 					jedis = RedisFactory.putInstance(jedis);
 					return null;
 				}
 
 			}
 			try {
 				ssc.raf.write(baos.toByteArray());
 				// 结果一次存入redis
 				if (mcs.size() > 0 && jedis != null)
 					jedis.hmset(set, mcs);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			} finally {
 				jedis = RedisFactory.putInstance(jedis);
 			}
 
 		}
 		return returnVal;
 	}
 	
 	/**
 	 *获得md5值所代表的图片的内容
 	 * @param md5		与storePhoto中的参数md5相对应
 	 * @return			该图片的内容,与storePhoto中的参数content对应
 	 */
 	public byte[] getPhoto(String set, String md5) throws RedirectException {
 		try {
 			reconnectJedis();
 		} catch (IOException e2) {
 			return null;
 		}
 		String info = null;
 		int err = 0;
 		
 		// Step 1: check the local lookup cache
 		info = (String) lookupCache.get(set + "." + md5);
 		if (info == null) {
 			try {
 				info = jedis.hget(set, md5);
 			} catch (JedisConnectionException e) {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 				}
 				err = -1;
 				return null;
 			} catch (JedisException e) {
 				err = -1;
 				return null;
 			} finally {
 				if (err < 0)
 					jedis = RedisFactory.putBrokenInstance(jedis);
 				else
 					jedis = RedisFactory.putInstance(jedis);
 			}
 			
 			if (info == null) {
 				System.out.println("MM: md5:" + md5 + " doesn't exist in set:" + set + ".");
 				return null;
 			} else {
 				// append this entry to lookup cache
 				lookupCache.put(set + "." + md5, info);
 			}
 		}
 		// split if it is complex uri
 		String savedInfo = null;
 		Long savedId = -1L;
 		for (String i : info.split("#")) {
 			String[] is = i.split("@");
 			
 			if (Long.parseLong(is[2]) == ServerConf.serverId)
 				return searchPhoto(i, is);
 			else {
 				savedInfo = i;
 				savedId = Long.parseLong(is[2]);
 			}
 		}
 
 		throw new RedirectException(savedId, savedInfo);
 	}
 	/**
 	 * 获得图片内容
 	 * @param info		对应storePhoto的type@set@serverid@block@offset@length@disk格式的返回值
 	 * @return			图片内容content
 	 */
 	public byte[] searchPhoto(String info, String[] infos) throws RedirectException {
 		long start = System.currentTimeMillis();
 		if (infos == null)
 			infos = info.split("@");
 		
 		if (infos.length != 7) {
 			System.out.println("Invalid INFO string: " + info);
 			return null;
 		}
 		if (Long.parseLong(infos[2]) != ServerConf.serverId) {
 			// this request should be send to another server
 			throw new RedirectException(Long.parseLong(infos[2]), info);
 		}
 		String path = infos[6] + "/" + destRoot + infos[1] + "/b" + infos[3];
 		RandomAccessFile readr = null;
 		byte[] content = new byte[Integer.parseInt(infos[5])];
 	
 		try {
 			//用哈希缓存打开的文件随机访问流
 			if (readRafHash.containsKey(path)) {
 				readr = readRafHash.get(path);
 			} else {
 				//构造路径时加上磁盘 
 				readr = new RandomAccessFile(path, "r");
 				readRafHash.put(path, readr);
 			}
 			readr.seek(Long.parseLong(infos[4]));
 			readr.read(content);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		ServerProfile.updateRead(content.length, System.currentTimeMillis() - start);
 
 		return content;
 	}
 	
 	public void delSet(String set) {
 		for(String d : diskArray)		//删除每个磁盘上的该集合
 			delFile(new File(d + "/" + destRoot + set));
 		//删除一个集合后,同时删除关于该集合的全局的上下文
 		for(String d : diskArray)
 			writeContextHash.remove(set+":"+d);			
 	}
 	
 	/**
 	 * 删除文件.如果是一个文件,直接删除,如果是文件夹,递归删除子文件夹和文件
 	 * @param f
 	 */
 	private void delFile(File f) {
 
 		if(!f.exists())
 			return;
 		if(f.isFile())
 			f.delete();
 		else {
 			for (File a : f.listFiles())
 				if (a.isFile())
 					a.delete();
 				else
 					delFile(a);
 			f.delete();
 		}
 	}
 	
 	//关闭jedis连接,关闭文件访问流
 	public void close() {
 		try {
 			//该变量是静态的,因此这段代码会关闭所有的raf,导致其他线程在写入时异常
 
 			for (Map.Entry<String, RandomAccessFile> entry : readRafHash.entrySet()) {
 				entry.getValue().close();
 			}
 		} catch(IOException e){
 			e.printStackTrace();
 		} finally {
 			if (jedis != null)
 				RedisFactory.putInstance(jedis);
 		}
 	}
 	
 }
