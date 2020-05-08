 package easyzk.zk;
 
 import easyzk.exception.EasyZkException;
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.data.Stat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class ZKHandler {
     private static final Logger logger = LoggerFactory.getLogger(ZKHandler.class);
 
     private static final int ZK_TIMEOUT = 60000;
     private static final String DEFAULT_CHARSET = "utf-8";
     private static final String SEPARATOR = "/";
 
     private final String zkString;
 
     private ZooKeeper zk;
 
     private static final Map<String, ZKHandler> instanceMap = new ConcurrentHashMap();
 
     private static final Object lock = new Object();
 
     private class ZKWatcher implements Watcher{
 
         @Override
         public void process(WatchedEvent watchedEvent) {
             if(watchedEvent.getType() == Event.EventType.None){
                 switch(watchedEvent.getState()){
                     case Expired:
                         reconnect();
                         break;
                     default:
                         break;
                 }
             }
         }
     }
 
     private ZKHandler(String zkString) throws EasyZkException{
         this.zkString = zkString;
         connect();
     }
 
     private void connect() throws EasyZkException{
         if(zkString == null || zkString.isEmpty()){
             throw new EasyZkException("zkString cannot be null or empty");
         }
 
         try {
             zk = new ZooKeeper(zkString, ZK_TIMEOUT, new ZKWatcher());
         } catch (IOException e) {
             throw new EasyZkException("Failed to connect to zookeeper", e);
         }
     }
 
     private void reconnect(){
         if(zk != null){
             try {
                 zk.close();
             } catch (InterruptedException e) {
                 //ignore
                 logger.debug("Interrupted", e);
             }
         }
         try {
             zk = new ZooKeeper(zkString, ZK_TIMEOUT, new ZKWatcher());
         } catch (IOException e) {
             logger.warn("Not connected to the zookeeper", e);
         }
     }
 
     public static ZKHandler getInstance(String zkClusterName) throws EasyZkException{
         ZKHandler instance = instanceMap.get(zkClusterName);
         if(instance == null){
             synchronized (lock){
                 instance = instanceMap.get(zkClusterName);
                 if(instance == null){
                     instance = new ZKHandler(ZKClusterManager.getInstance().getZkString(zkClusterName));
                     instanceMap.put(zkClusterName, instance);
                 }
             }
         }
         return instance;
     }
 
     public NodeInfo get(String node) throws EasyZkException{
         try {
             Stat stat = zk.exists(node, false);
             if(stat != null){
                 byte[] rawData = zk.getData(node, false, null);
                String data = rawData == null ? "" : new String(rawData, DEFAULT_CHARSET);
                 return new NodeInfo(node, String.valueOf(stat.getVersion()), String.valueOf(stat.getCtime()), stat.getNumChildren(), data);
             }
         } catch (KeeperException e) {
             logger.warn("Failed to get node information {}", node, e);
             throw new EasyZkException(e);
         } catch (InterruptedException e) {
             logger.debug("Interrupted");
             throw new EasyZkException(e);
         } catch (UnsupportedEncodingException e) {
             logger.warn("Failed to get node information {}", node, e);
             throw new EasyZkException(e);
         }
         return null;
     }
 
     public void add(String parent, String node, String data) throws EasyZkException{
         String parent2 = parent.endsWith(SEPARATOR) ? parent.substring(0, parent.lastIndexOf(SEPARATOR)) : parent;
         String[] allNodes = parent2.split(SEPARATOR);
         String tmp = "";
         try{
             for(String n : allNodes){
                 if(n.trim().isEmpty())continue;
                 tmp +=  SEPARATOR + n;
                 logger.info("Chekcing parent node {}", tmp);
                 if(zk.exists(tmp, false) == null){
                     logger.info("Creating parent node {}", tmp);
                     byte[] putData = tmp.equals(node) ? data.getBytes(DEFAULT_CHARSET) : new byte[0];
                     zk.create(tmp, putData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 
                 }
             }
 
             //check all nodes are created too
             String[] newNodes = node.split(SEPARATOR);
             tmp = "" ;
             for(String newNode : newNodes){
                 if(newNode.trim().isEmpty())continue;
                 tmp += SEPARATOR + newNode;
                 String createNode = parent2 + tmp;
                 byte[] rawData =  tmp.equals(SEPARATOR + node) ? data.getBytes(DEFAULT_CHARSET) : new byte[0];
                 zk.create(createNode, rawData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
             }
         } catch (KeeperException e) {
             logger.warn("Failed to add node {} to {}",node, parent);
             throw new EasyZkException(e);
         } catch (InterruptedException e) {
             logger.debug("Interrupted");
             throw new EasyZkException(e);
         } catch (UnsupportedEncodingException e) {
             logger.warn("Failed to add node {} to {}", node, parent);
             throw new EasyZkException(e);
         }
     }
 
     public List<String> getAllChildren(String node) throws EasyZkException{
         List<String> childList = new ArrayList<String>();
         try {
             Stat stat = zk.exists(node, false);
             if(stat != null){
                 childList.addAll(zk.getChildren(node, false));
             }
         } catch (KeeperException e) {
             logger.warn("Failed to get child list for {}", node, e);
             throw new EasyZkException(e);
         } catch (InterruptedException e) {
             logger.debug("Interrupted", e);
             throw new EasyZkException(e);
         }
         return childList;
     }
 
     public void put(String node, String data) throws EasyZkException{
         try {
             Stat stat = zk.exists(node, false);
             if(stat == null){
                 logger.warn("Cannot update non existing node");
                 throw new EasyZkException("Node "+ node +" does not exist");
             }
             zk.setData(node, data.getBytes(DEFAULT_CHARSET), stat.getVersion());
         } catch (KeeperException e) {
             logger.warn("Failed to update node {} ", node, e);
             throw new EasyZkException(e);
         } catch (InterruptedException e) {
             logger.debug("Interrupted");
             throw new EasyZkException(e);
         } catch (UnsupportedEncodingException e) {
             logger.warn("Failed to update node {} ", node, e);
             throw new EasyZkException(e);
         }
 
     }
     public void deleteNode(String node) throws EasyZkException{
         try {
             Stat stat = zk.exists(node, false);
             if (stat != null){
                 List<String> childNodes = zk.getChildren(node, false);
                 if(childNodes.size() == 0){
                     logger.info("Deleting node : {}", node);
                     zk.delete(node, stat.getVersion());
                 }else{
                     for(String child : childNodes){
                         deleteNode(node + SEPARATOR + child);
                     }
                     zk.delete(node, stat.getVersion());
                 }
             }
         } catch (KeeperException e) {
             logger.warn("Failed to delete node {} ", node, e);
             throw new EasyZkException(e);
         } catch (InterruptedException e) {
             logger.debug("Interrupted");
             throw new EasyZkException(e);
         }
     }
 }
