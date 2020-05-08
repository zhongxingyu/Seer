 package voldemort.store.parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import voldemort.utils.ByteArray;
 import voldemort.versioning.VectorClock;
 
 import com.google.common.collect.Maps;
 
 public class TrackLogParser {
 
     private Map<Integer, String> logFiles;
 
     public TrackLogParser(Map<Integer, String> files) {
         logFiles = files;
     }
 
     public void init() {
         Map<ByteArray, List<AccessNode>> nodes = Maps.newHashMap();
         Map<String, ClientNode> clients = Maps.newHashMap();
        Pattern p = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3})\\] INFO /(\\d+(?:\\.\\d+){3})\\:\\d+ ((?:Put)|(?:Get)) \\[(\\d+(?:\\, \\d+)+)\\] \\[(\\d+(?:\\, \\d+)+)\\]");
 
         for(Integer id: logFiles.keySet()) {
             String path = logFiles.get(id);
             File logFile = new File(path);
             try {
                 BufferedReader reader = new BufferedReader(new FileReader(logFile));
                 String line;
                 while((line = reader.readLine()) != null) {
                     Matcher m = p.matcher(line);
                     if(m.find()) {
                        System.out.println(m.group(0) + "####" + m.group(1) + " " + m.group(2)
                                           + " " + m.group(4));
 
                         String[] keyParts = m.group(4).split(", ");
                         byte[] keys = new byte[keyParts.length];
                         for(int i = 0; i < keyParts.length; i++) {
                             keys[i] = Byte.parseByte(keyParts[i]);
                         }
                         ByteArray key = new ByteArray(keys);
 
                         String[] versionParts = m.group(5).split(", ");
                         byte[] versions = new byte[versionParts.length];
                         for(int i = 0; i < versionParts.length; i++) {
                             versions[i] = Byte.parseByte(versionParts[i]);
                         }
                         VectorClock version = new VectorClock(versions, 0);
 
                         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
                         Date d = sdf.parse(m.group(1));
                         long timestamp = d.getTime();
                         String client = m.group(2);
                         if(!clients.containsKey(client)) {
                             ClientNode cNode = new ClientNode(client);
                             clients.put(client, cNode);
                         }
 
                         AccessType accType;
                         if(m.group(3).equals("Put")) {
                             accType = AccessType.SET;
                         } else {
                             accType = AccessType.GET;
                         }
 
                         AccessNode node = new AccessNode(accType, client, key, version, timestamp);
                         node.addClientNode(clients.get(client));
                         clients.get(client).addAccess(node);
 
                         if(nodes.containsKey(key)) {
                             List<AccessNode> accList = nodes.get(key);
                             for(AccessNode n: accList) {
                                 n.addSuccessor(node);
                             }
                             accList.add(node);
                         } else {
                             List<AccessNode> accList = new ArrayList<AccessNode>();
                             accList.add(node);
                             nodes.put(key, accList);
                         }
 
                     }
                 }
             } catch(Exception e) {
                 System.out.println(e);
             }
         }
 
     }
 }
