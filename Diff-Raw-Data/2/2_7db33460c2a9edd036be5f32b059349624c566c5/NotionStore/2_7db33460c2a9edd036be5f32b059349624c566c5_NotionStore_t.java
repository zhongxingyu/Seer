 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wikicrawler;
 
 import java.util.*;
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.*;
 import org.apache.hadoop.hbase.client.*;
 import org.apache.hadoop.hbase.util.*;
 
 /**
  *
  * @author spy
  */
 public class NotionStore {
     private static String notionTable = "notions";
     private static String linkTable = "links";
     
     private static HashMap<URL, Notion> collection = new HashMap();
     
     private static HTable notions;
     private static HTable links;
     
     public static void init() throws IOException {
         Configuration conf = HBaseConfiguration.create();
         HBaseAdmin admin = null;
         try {
             admin = new HBaseAdmin(conf);
             notions = new HTable(conf, notionTable);
            links = new HTable(conf, linkTable);
         }
         catch ( Exception e ) {
             admin.close();
         }
     }
     
     public static void store(Notion n) throws IOException {
         System.out.println("Added notion: " + n.title);
         Put put = new Put(Bytes.toBytes(n.url.toExternalForm()));
         put.add(Bytes.toBytes("notion"), Bytes.toBytes("title"), Bytes.toBytes(n.title));
         putLinks(put, n);
         notions.put(put);
     }
     
     public static boolean exist(URL u) throws IOException {
         Get g = new Get(Bytes.toBytes(u.toExternalForm()));
         Result r = notions.get(g);
         return !r.isEmpty();
     }
     
     private static void putLink(URL u, Link l) throws IOException {
         byte[] key = Bytes.toBytes(u.toExternalForm() + "\t" + l.url.toExternalForm());
         Put put = new Put(key);
         put.add(Bytes.toBytes("link"), Bytes.toBytes("title"), Bytes.toBytes(l.title));
         put.add(Bytes.toBytes("link"), Bytes.toBytes("text"), Bytes.toBytes(l.text));
         put.add(Bytes.toBytes("link"), Bytes.toBytes("content"), Bytes.toBytes(l.content));
         links.put(put);
     }
     
     private static void putLinks(Put p, Notion n) throws IOException {
         String linksText = "";
         for ( Map.Entry<URL, ArrayList<Link>> e : n.links.entrySet() ) {
             linksText += e.getValue().size() + "," + e.getKey() + "\t";
             for ( Link l : e.getValue() ) {
                 putLink(n.url, l);
             }
         }
         p.add(Bytes.toBytes("notion"), Bytes.toBytes("links"), Bytes.toBytes(linksText));
     }
 }
