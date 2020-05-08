 package easyzk.zk;
 
 import easyzk.exception.EasyZkException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class ZKClusterManager {
 
     private static final Logger logger = LoggerFactory.getLogger(ZKClusterManager.class);
 
     private static final String propertiesFile = "easyzk.properties";
 
     private static final Map<String, ZKCluster> clusterCache = new ConcurrentHashMap<String, ZKCluster>();
 
     private static final ZKClusterManager instance = new ZKClusterManager();
 
     private ZKClusterManager(){
         loadClusterInStartup();
     }
 
     public static ZKClusterManager getInstance(){
         return instance;
     }
 
     private void loadClusterInStartup(){
         try{
             logger.debug("Attempting to load saved cluster info");
             loadCluster();
         }catch(EasyZkException e){
             logger.debug("Failed to load saved cluster info during sartup");
         }
 
     }
 
     private void loadCluster() throws EasyZkException{
         logger.info("Loading saved cluster info");
         File f = new File(propertiesFile);
         if(!f.exists()){
             logger.info("Saved cluster info not found");
         }
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(f));
         } catch (IOException e) {
             logger.warn("Failed to read saved cluster information", e);
             throw new EasyZkException("Failed to read cluster information", e);
         }
 
         //create the cluster info
         Set<String> clusters = props.stringPropertyNames();
         for(String cluster : clusters){
             String zkString = (String)props.get(cluster);
             clusterCache.put(cluster, new ZKCluster(cluster, zkString));
         }
     }
 
     private void saveClusterInfo() throws EasyZkException{
         File f = new File(propertiesFile);
         Properties props = new Properties();
         for(ZKCluster cluster : clusterCache.values()){
             props.put(cluster.getClusterName(), cluster.getZKString());
         }
         try {
             props.store(new FileOutputStream(f),"");
         } catch (IOException e) {
             logger.warn("Failed to save the cluster info", e);
             throw new EasyZkException("Failed to save the cluster info", e);
         }
     }
 
 
     public void addCluster(String clusterName, String zkString) throws EasyZkException{
         if(clusterName == null || clusterName.isEmpty() || zkString == null || zkString.isEmpty()){
             logger.info("Invalid clustername or zkstring");
             throw new EasyZkException("Invalid cluster name or zkstring");
         }
 
         if(clusterCache.containsKey(clusterName)){
             logger.info("Attempting to add already existing cluster");
             throw new EasyZkException("Cluster "+ clusterName +" already exists. Please choose another name");
         }
         clusterCache.put(clusterName, new ZKCluster(clusterName, zkString));
         saveClusterInfo();
     }
 
     public void deleteCluster(String clusterName) throws EasyZkException{
         if(clusterName == null || clusterName.isEmpty()){
             throw new EasyZkException("Invalid cluster name");
         }
         if(clusterCache.containsKey(clusterName)){
             clusterCache.remove(clusterName);
             saveClusterInfo();
         }
     }
 
     public String getZkString(String clusterName) throws EasyZkException {
         ZKCluster cluster = clusterCache.get(clusterName);
         if(cluster == null){
             loadCluster();
             cluster = clusterCache.get(clusterName);
         }
         if(cluster == null){
             logger.info("Cluster {} is not added yet.", clusterName);
             throw new EasyZkException("Cluster not found. name : "+clusterName);
         }
         return cluster.getZKString();
     }
 
     public List<String> getAllClustersName(){
         List<String> clustersList = new ArrayList<String>();
         for(String clusterName : clusterCache.keySet()){
             clustersList.add(clusterName);
         }
         return clustersList;
     }
 }
