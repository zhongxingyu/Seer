 package net.yeputons.cscenter.dbfall2013.scaling;
 
 import org.yaml.snakeyaml.Yaml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.security.InvalidParameterException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Egor Suvorov
  * Date: 12.10.13
  * Time: 19:11
  * To change this template use File | Settings | File Templates.
  */
 public class ShardingConfiguration {
     protected final static MessageDigest md;
     static {
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     public final static int DEFAULT_PORT = 33131;
 
     public TreeMap<String, ShardDescription> shards;
 
     public ShardingConfiguration() {
         shards = new TreeMap<String, ShardDescription>();
     }
 
     public ShardDescription getShard(byte[] key) {
         StringBuilder digest = new StringBuilder();
        for (byte b : md.digest(key))
             digest.append(String.format("%02x", b & 0xFF));
         return shards.floorEntry(digest.toString()).getValue();
     }
 
     public void readFromFile(File f) throws FileNotFoundException {
         shards = new TreeMap<String, ShardDescription>();
         Yaml yaml = new Yaml();
         Map data = (Map)yaml.load(new FileInputStream(f));
         for (Object _node : (List)data.get("sharding")) {
             Map node = (Map)_node;
 
             String startHash = (String) node.get("startHash");
             startHash = startHash.toLowerCase();
 
             ShardDescription item = new ShardDescription();
             item.host = (String) node.get("host");
             item.port = (Integer) node.get("port");
             if (item.host == "") item.host = "localhost";
             if (item.port == 0) item.port = DEFAULT_PORT;
             shards.put(startHash, item);
         }
 
         final String zeroHash = "00000" + "00000" + "00000" + "00000";
         if (shards.floorKey(zeroHash) == null) {
             throw new InvalidParameterException("Zero hash is not included in any shard");
         }
     }
 }
