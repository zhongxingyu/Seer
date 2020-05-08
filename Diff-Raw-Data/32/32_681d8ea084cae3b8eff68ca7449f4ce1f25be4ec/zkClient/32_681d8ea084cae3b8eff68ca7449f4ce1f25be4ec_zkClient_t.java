 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.slf4j.*;
 
 
 
 public class zkClient implements Watcher{
 	static final String[] machines = {"172.22.138.16", "172.22.138.18", "172.22.138.52"};
 	static final String[] ports = {"12182", "22181", "32181"};
 	
 	/**
 	 * 2D array of dimensions 3*3
 	 * each row stands for the replica for which client wants to send read/write request
 	 * 1st column in each row stands for the replica which is on SSD
 	 * 2nd and 3rd columns are for HDD replicas
 	 */
 	private ZooKeeper[][] paxosInstances = new ZooKeeper[3][3];
 	
 	
 	static long HKey = 0;
 	static long LKey = 0;
 	static final long maxKey = 268435456;		//hex - 10000000
 	//static final long maxKey = ;		//hex - 10000000
 	
 	public static String randKey() {
 		long randomKey;
 		randomKey = (long)(Math.random() * (zkClient.maxKey) + 1);
 		return new Long(randomKey).toString();
 	}
 	
 	public static void main(String[] args) {
         if (args.length > 2) {
             System.err.println("USAGE: Executor hostPort  [args ...]");
             System.exit(2);
         }
         int[] distribution = new int[(int)(maxKey/10000) + 1];  
         //System.out.println(distribution[10009]);
         String hostPort1 = args[0];
         String hostPort2 = args[1];
         
         String znode = "/db1";
         //long key;
         
         
         zkClient zkClientObj1 = new zkClient();
         zkClient zkClientObj2 = new zkClient();
         ZooKeeper zk1 = null;
         ZooKeeper zk2 = null;
         int reads = 0, writes = 1;
         byte[] tmp = null;
 		try {
 			zk1 = new ZooKeeper(hostPort1, 30000, zkClientObj1);
 			zk2 = new ZooKeeper(hostPort2, 30000, zkClientObj2);
 			tmp = new String("db1").getBytes("UTF-16");
 			if (zk2.exists(znode, null) == null) {
 				zk2.create(znode, tmp, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (KeeperException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         
         //String filename = args[2];
         //String exec[] = new String[args.length - 3];
         //System.arraycopy(args, 3, exec, 0, exec.length);
         try {
         	
         	long startTime = System.currentTimeMillis();
         	long localStartTime = 0;
         	long totalReadTime = 0;
         	long totalWriteTime = 0;
         	
         	long readMin = 0;
         	long writeMin = 0;
         	long readMax = 0;
         	long writeMax = 0;
         	
         	System.out.println("Start Time : " + startTime);
         	
         	String randKey = null;
         	byte[] retData = null;
         	ByteArrayOutputStream bOut = new ByteArrayOutputStream(100);
         	
         	for (int  i = 0; i < maxKey; i++) {
 
         		LKey++;
         		randKey = String.valueOf(HKey) + String.valueOf(LKey);
         		bOut.reset();
         		bOut.write(randKey.getBytes());
         		
         		byte[] key = randKey.getBytes();
         		byte[] value = bOut.toByteArray();
         		
         		byte[] c = new byte[116];
         		Arrays.fill(c, (byte)'0');
         		
         		System.arraycopy(key, 0, c, 16-key.length, key.length);
         		System.arraycopy(value, 0, c, 116-value.length, value.length);
         		
         		localStartTime = System.currentTimeMillis();
     			zk2.setData(znode, c, -1);
     			totalWriteTime += (System.currentTimeMillis() - localStartTime);        		
     			writes++;
     			
     			
         		if(i % 100 == 0) {      			
         			System.out.println(" Progress : " + (float) i*100/maxKey+ " % " + " KeyValue:  " + new String(c) + " KeyValueLength:  " + c.length + " Writes : " + writes + " *** Write latency : " + totalWriteTime/(writes));
         		}
         		//randKey = zkClient.randKey();
         		//distribution[(int)((Long.parseLong(randKey)/10000))] += 1;
         		/*
         		if (Long.parseLong(randKey) % 2 == 0) {
         			localStartTime = System.currentTimeMillis();
         			retData = zk.getDataByKey(znode, randKey);
         			totalReadTime += (System.currentTimeMillis() - localStartTime);
         			reads++;
         		    
         		    
         			//zk.setData(znode, randKey.getBytes(), -1);
         		}else {
         			writes++;
         			localStartTime = System.currentTimeMillis();
         			zk1.setData(znode, randKey.getBytes(), -1);
         			totalWriteTime += (System.currentTimeMillis() - localStartTime);
         		    
         		    
         			//retData = zk.getDataByKey(znode, randKey);
         		}
         		
         		if(i % 100 == 0 && reads  > 0 && writes > 0) {      			
         			System.out.println(" Progress : " + (float) i/30+ " % " + " KeyValue:  " + new String(randKey) + " Reads : " +reads  +  " Writes : " + writes +   " *** Read latency : "+ totalReadTime/(reads)+ " *** Write latency : " + totalWriteTime/(writes));
         		}*/
         		
         		//zk.setData(znode, tmp , 0);   
         		//byte[] tmp1 = zk.getData(znode, false, null);
         		//System.out.println("Output:" + new String(tmp1,"UTF-16"));
         	}
         	System.out.println("Total Time : " + (System.currentTimeMillis() - startTime));
         	//System.out.println(Arrays.toString(distribution));
         	//System.out.println(" Reads : "+ reads+ " Total Read Time : "  + totalReadTime + "\nWrites : "+ writes +" Total Write Time : "  + totalWriteTime);
         	System.out.println("\nWrites : "+ writes +" Total Write Time : "  + totalWriteTime);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
	//@Override
 	public void process(WatchedEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
