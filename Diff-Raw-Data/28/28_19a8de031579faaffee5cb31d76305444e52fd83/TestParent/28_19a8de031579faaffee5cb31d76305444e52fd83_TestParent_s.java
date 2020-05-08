 package zfs.java;
 
 import de.javastream.javassh.parser.ProcessParser;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * TestParent 20.10.2012
  *
  * @author Philipp Haussleiter
  *
  */
 public class TestParent {
 
     private static final Logger LOG = Logger.getLogger(TestParent.class.getName());
     private static final String SEP = File.separator;
 
     protected void parseFile(String path, ProcessParser pp) {
         File file = new File("src" + SEP + "test" + SEP + path);
         if (file.exists()) {
             LOG.log(Level.INFO, "\n\nReading: {0}\n\n", path);
             try {
                 BufferedReader br = new BufferedReader(new FileReader(file));
                 pp.parse(br);
                 br.close();
             } catch (FileNotFoundException ex) {
                 LOG.log(Level.SEVERE, "{0} file: {1}", new Object[]{ex.getLocalizedMessage(), path});
             } catch (IOException ex) {
                 LOG.log(Level.SEVERE, "{0} file: {1}", new Object[]{ex.getLocalizedMessage(), path});
             }
         } else {
             LOG.log(Level.SEVERE, "file: {0} does not exists!", file.getAbsolutePath());
         }
     }
 
     protected Object get(int index, final Object collection) {
         if (collection != null) {
             if (collection instanceof List) {
                 List list = (List) collection;
                 if (list.size() > index) {
                     return list.get(index);
                 }
             }
             if (collection instanceof Set) {
                 Set set = (Set) collection;
                 if (set.size() > index) {
                     return set.toArray()[index];
                 }
             }
             if (collection instanceof Map) {
                 Map map = (Map) collection;
                 if (map.size() > index) {
                     return map.get(map.keySet().toArray()[index]);
                 }
             }
         }
         return null;
     }
 
     protected Object first(final Object collection) {
         return get(0, collection);
     }
 
     protected Object entry(final Object collection, final int index) {
         if (collection != null) {
             if (collection instanceof List) {
                 List list = (List) collection;
                 if (list.size() >= index) {
                     return list.get(index);
                 }
             }
             if (collection instanceof Set) {
                 Set set = (Set) collection;
                 if (set.size() >= index) {
                     return set.toArray()[index];
                 }
 
             }
             if (collection instanceof Map) {
                 Map map = (Map) collection;
                 if (map.size() >= index) {
                     return map.get(map.keySet().toArray()[index]);
                 }
 
             }
         }
         return null;
     }
 
     protected Object last(final Object collection) {
         if (collection != null) {
             if (collection instanceof List) {
                 List list = (List) collection;
                 if (list.size() > 0) {
                     return list.get(list.size() - 1);
                 }
             }
             if (collection instanceof Set) {
                 Set set = (Set) collection;
                 if (set.size() > 0) {
                     return set.toArray()[set.size() - 1];
                 }
             }
             if (collection instanceof Map) {
                 Map map = (Map) collection;
                 if (map.size() > 0) {
                     return map.get(map.keySet().toArray()[map.size() - 1]);
                 }
             }
         }
         return null;
     }
 
     protected void debugJson(Object obj, String name) {
         try {
             ObjectMapper mapper = new ObjectMapper();
             LOG.log(Level.INFO, name + ": => JSON:\n\n {0}\n\n", mapper.writeValueAsString(obj));
         } catch (IOException ex) {
             LOG.log(Level.SEVERE, null, ex);
         }
     }
 
     protected void debug(Object obj) {
         debug(obj, (Object) null);
     }
 
     protected void debug(Object obj, Object... args) {
         StringBuilder sb = new StringBuilder("\n");
         if (args != null && args.length > 0) {
             sb.append("Debugging '").append(args[0]).append("': \n");
         }
         sb = debugEntry(sb, obj);
         LOG.log(Level.INFO, sb.toString());
     }
 
     protected StringBuilder debugEntry(StringBuilder sb, Object obj) {
         if (obj == null) {
             sb.append("\tis NULL \n");
         } else if (obj instanceof List) {
             sb.append("\tList: \n");
             List l = (List) obj;
             for (Object value : l) {
                 sb.append("\t\t").append(value.toString()).append("\n");
             }
         } else if (obj instanceof Set) {
             sb.append("\tSet: \n");
             Set s = (Set) obj;
             for (Object value : s) {
                 sb.append("\t\t").append(value.toString()).append("\n");
             }
         } else if (obj instanceof Map) {
             sb.append("\tMap: \n");
             Map m = (Map) obj;
             for (Object key : m.keySet()) {
                 sb.append("\t\t").append(key.toString()).append(":\n").append(m.get(key).toString()).append("\n");
             }
         } else {
             sb.append("\t").append(obj.getClass().getName()).append(": \n");
             sb.append(prefixLine("\t", obj.toString())).append("\n");
         }
         return sb;
     }
 
     private String prefixLine(final String prefix, final String output) {
         StringBuilder sb = new StringBuilder();
         for (String line : output.split("\n")) {
             sb.append(prefix).append(line).append("\n");
         }
         return sb.toString();
     }
 }
