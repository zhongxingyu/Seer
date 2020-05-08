 package com.rakuten.rit.roma.romac4j.routing;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.log4j.Logger;
 
 import com.rakuten.rit.roma.romac4j.Receiver;
 import com.rakuten.rit.roma.romac4j.StringReceiver;
 import com.rakuten.rit.roma.romac4j.ValueReceiver;
 import com.rakuten.rit.roma.romac4j.pool.Connection;
 import com.rakuten.rit.roma.romac4j.pool.SocketPoolSingleton;
 
 public final class Routing extends Thread {
     protected static Logger log = Logger.getLogger(Routing.class.getName());
     private SocketPoolSingleton sps = SocketPoolSingleton.getInstance();
     private String mklHash;
     private RoutingData routingData;
     private Random rnd = new Random(System.currentTimeMillis());
     private boolean status = false;
 
     /**
      * 
      * @param props
      */
     public Routing(String nodeId) {
        routingData = new RoutingData();
         routingData.setNumOfNodes(1);
        routingData.setNodeId(new String[] { nodeId });
     }
 
     /**
 	 * 
 	 */
     public void run() {
         while (status == false) {
             try {
                 String mklHash = getMklHash();
                 if (mklHash != null && !mklHash.equals(this.mklHash)) {
                     this.mklHash = mklHash;
                     RoutingData tempBuff = getRoutingDump();
                     synchronized (routingData) {
                         routingData = tempBuff;
                     }
                     log.debug("Routing change!");
                 } else {
                     log.debug("Routing no change!");
                 }
             } catch (Exception e) {
                 log.debug("run() Error.");
                 e.printStackTrace();
             }
             try {
                 // TODO: const...
                 Thread.sleep(5000);
             } catch (InterruptedException e) {
             }
         }
     }
 
     /**
      * 
      * @param key
      * @return long vn
      * @throws NoSuchAlgorithmException
      */
     public long getVn(String key) throws NoSuchAlgorithmException {
         int divBits = 0;
         int dgstBits = 0;
         synchronized (routingData) {
             divBits = routingData.getDivBits();
             dgstBits = routingData.getDgstBits();
         }
         long mask = ((1L << divBits) - 1) << (dgstBits - divBits);
         MessageDigest md = MessageDigest.getInstance("SHA1");
         md.update(key.getBytes());
         byte[] b = md.digest();
         long h = ((long) b[b.length - 7] << 48) & 0xff000000000000L
                 | ((long) b[b.length - 6] << 40) & 0xff0000000000L
                 | ((long) b[b.length - 5] << 32) & 0xff00000000L
                 | ((long) b[b.length - 4] << 24) & 0xff000000L
                 | ((long) b[b.length - 3] << 16) & 0xff0000L
                 | ((long) b[b.length - 2] << 8) & 0xff00L
                 | (long) b[b.length - 1] & 0xffL;
         return h & mask;
     }
 
     /**
      * 
      * @param status
      */
     public void setStatus(boolean status) {
         this.status = status;
     }
 
     public Connection getConnection() {
         Connection con = null;
         String[] nodeId = null;
         int rndVal = rnd.nextInt(routingData.getNumOfNodes());
         try {
             synchronized (routingData) {
                 nodeId = routingData.getNodeId();
             }
             con = sps.getConnection(nodeId[rndVal]);
         } catch (Exception ex) {
             // TODO: Exception throw??
         }
         return con;
     }
 
     public Connection getConnection(String key) {
         Connection con = null;
         String[] nodeId = null;
         int[] vNode = null;
         try {
             long vn = getVn(key);
             synchronized (routingData) {
                 nodeId = routingData.getNodeId();
                 vNode = routingData.getVNode().get(vn);
             }
            con = sps.getConnection(nodeId[vNode[0]]);
             con.setNodeId(nodeId[vNode[0]]);
         } catch (NoSuchAlgorithmException ex) {
             // TODO: Exception throw??
         }
 
         return con;
     }
 
     public void returnConnection(Connection con) {
         sps.returnConnection(con);
     }
 
     public void failCount(Connection con) {
         // TODO
     }
 
     private String getMklHash() {
         Connection con = null;
         Receiver rcv = new StringReceiver();
         try {
             con = getConnection();
             con.write("mklhash 0", null, null, null, -1);
             rcv.receive(con);
             returnConnection(con);
         } catch (TimeoutException e) {
         }
         return rcv.toString();
     }
 
     private RoutingData getRoutingDump() {
         RoutingData routingData = new RoutingData();
         Connection con = null;
         Receiver rcv = new ValueReceiver();
         String str = null;
         byte[] buff = null;
         try {
             con = getConnection();
             con.write("routingdump bin", null, null, null, -1);
             rcv.receive(con);
             buff = ((ValueReceiver) rcv).getValue();
 
             // ByteData bd;
             // bd.receive(con, len);
             //
             // if(bd.getString(2).equals("RT"){
             // }
             // ver = bd.getInt(2);
             // dgstBits = db.getInt(1);
 
             // # 2 bytes('RT'):magic code
             int pos = 0;
             str = new String(new byte[] { buff[pos], buff[pos + 1] });
             if (!str.equals("RT")) {
                 log.debug("This is not RT Data.");
                 throw new Exception("Illegal Format.");
             }
             pos += 2;
 
             // # unsigned short:format version
             int formatVer = (buff[pos] << 8) & 0xff00 | buff[pos + 1] & 0xff;
             pos += 2;
             log.debug("formatVer:" + formatVer);
 
             // # unsigned char:dgst_bits
             short dgstBits = buff[pos];
             pos += 1;
             log.debug("dgstBits:" + dgstBits);
 
             // unsigned char:div_bits
             short divBits = buff[pos];
             pos += 1;
             log.debug("divBits:" + divBits);
 
             // unsigned char:rn
             short rn = buff[pos];
             pos += 1;
             log.debug("rn:" + rn);
 
             // # unsigned short:number of nodes
             int numOfNodes = (buff[pos] << 8) & 0xff00 | buff[pos + 1] & 0xff;
             pos += 2;
             log.debug("numOfNodes:" + numOfNodes);
 
             // # string:node-id
             String[] nodeId = new String[numOfNodes];
             for (int i = 0; i < numOfNodes; i++) {
                 int tmpLen = (buff[pos] << 8) & 0xff00 | buff[pos + 1] & 0xff;
                 pos += 2;
                 byte[] tmpByte = new byte[tmpLen];
                 for (int j = 0; j < tmpLen; j++) {
                     tmpByte[j] = buff[pos + j];
                 }
                 nodeId[i] = new String(tmpByte);
                 pos += tmpLen;
             }
             log.debug("nodeId:");
             for (int i = 0; i < numOfNodes; i++) {
                 log.debug(nodeId[i]);
             }
 
             // int32_v_clk / index of nodes
             // map key=vnode val=[0]v_clk, [1..n]node_id
             HashMap<Long, Long> vClk = new HashMap<Long, Long>();
             HashMap<Long, int[]> vNode = new HashMap<Long, int[]>();
             int[] tmpNodes = null;
             for (int i = 0; i < Math.pow(2, divBits); i++) {
                 long vn = (long) i << (dgstBits - divBits);
                 // log.debug("vn:" + vn);
                 long tmpClk = (buff[pos] << 24) & 0xff000000L
                         | (buff[pos + 1] << 16) & 0xff0000L
                         | (buff[pos + 2] << 8) & 0xff00L | buff[pos + 3]
                         & 0xffL;
                 // log.debug("tmpClk:" + tmpClk);
                 short tmpNumOfNodes = buff[pos + 4];
                 // log.debug("tmpNumOfNodes:" + tmpNumOfNodes);
                 pos += 5;
                 vClk.put(vn, tmpClk);
                 tmpNodes = new int[rn];
                 for (int j = 0; j < tmpNumOfNodes; j++) {
                     int tmpIdx = (buff[pos] << 8) & 0xff00 | buff[pos + 1]
                             & 0xff;
                     pos += 2;
                     tmpNodes[j] = tmpIdx;
                     // log.debug("tmpIdx:" + tmpIdx);
                 }
                 vNode.put(vn, tmpNodes);
             }
 
             // Store to HashMap
             routingData.setFormatVer(formatVer);
             routingData.setDgstBits(dgstBits);
             routingData.setDivBits(divBits);
             routingData.setRn(rn);
             routingData.setNumOfNodes(numOfNodes);
             routingData.setNodeId(nodeId);
             routingData.setVClk(vClk);
             routingData.setVNode(vNode);
         } catch (Exception e) {
             e.printStackTrace();
             // TODO:
             // throw new Exception("RoutingDump Exception.");
         }
         return routingData;
     }
 }
