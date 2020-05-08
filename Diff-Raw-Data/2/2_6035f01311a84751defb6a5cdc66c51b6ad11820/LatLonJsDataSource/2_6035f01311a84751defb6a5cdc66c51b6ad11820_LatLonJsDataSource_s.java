 package net.relet.freimap;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 /**
  * A {@link DataSource} implementation that reads node data from the
  * <a href="http://www.layereight.de/software.php">FreifunkMap</a> plugin 
  * latlon.js file. You will need either a local copy or have it available
  * on a web server.
  * 
  * Since it works with URLs, downloaded data can be used as well.
  *  
  * @author Thomas Hirsch (thomas hirsch gmail com)
  *
  */
 public class LatLonJsDataSource implements DataSource {
 
   Vector<FreiNode> nodes = new Vector<FreiNode>();
   HashMap<String, FreiNode> nodeByName = new HashMap<String, FreiNode>();
 
   public void init(HashMap<String, Object> configuration) {
     String sServerURL = null;
     try {
       sServerURL = Configurator.getS("url", configuration);
       
       System.out.println("fetching node data from URL: " + sServerURL);
       System.out.print("This may take a while ... ");
       
      BufferedReader in = new BufferedReader(new FileReader(sServerURL));
       while (true) {
         String line=in.readLine();
         if (line==null) break;
         if ((line.length()>4) && (line.substring(0,4).equals("Node"))) {
           StringTokenizer st = new StringTokenizer(line.substring(5,line.length()-2), ",", false);
           String ip = st.nextToken();
           double lat  = Double.parseDouble(st.nextToken());
           double lon  = Double.parseDouble(st.nextToken());
           int foo   = Integer.parseInt(st.nextToken());
           String foo2  = st.nextToken(); //gateway?
           String fqid  = st.nextToken();
 
           ip = ip.substring(1,ip.length()-1); //strip single quotes
           fqid = fqid.substring(1,fqid.length()-1);
 
           // Use ip or coordinates as fqid if tooltip is missing
           if (ip.equals("")) ip=null;
           if (fqid == null) {
             if (ip == null) {
               fqid = lat+","+lon;
             } else {
               fqid = ip;
             }
           }
           if (ip == null) { //we need at least one identifier
             ip = fqid;
           }
 
           FreiNode nnode = new FreiNode(ip, fqid, lon, lat);
           nodes.add(nnode);
           nodeByName.put(nnode.id, nnode);
 
         }
       }
 
       System.out.println("finished.");
       
     } catch (MalformedURLException mue) {
       System.out.println("failed!");
       throw new IllegalStateException("Invalid server URL: " + sServerURL);
     } catch (IOException ioe) {
       System.out.println("failed! IOException in LatLonJSDataSource");
       ioe.printStackTrace();
     }
   }
   
   public void addDataSourceListener(DataSourceListener dsl) {
     // TODO: Implement me.
   }
 
   public long getClosestUpdateTime(long time) {
     // TODO: Implement me.
     return 1;
   }
 
   public long getFirstUpdateTime() {
     // TODO: Implement me.
     return 1;
   }
 
   public long getFirstAvailableTime() {
     // TODO: Implement me.
     return 1;
   }
 
   public long getLastAvailableTime() {
     // TODO: Implement me.
     return 1;
   }
 
   public long getLastUpdateTime() {
     // TODO: Implement me.
     return 1;
   }
 
   public FreiNode getNodeByName(String id) {
     return nodeByName.get(id);
   }
 
   public void addNode(FreiNode node) {
     nodes.remove(node); //just in case
     nodes.add(node);
     nodeByName.put(node.id, node);
   }
 
 
   public void getLinkCountProfile(FreiNode node, NodeInfo info) {
     // TODO: Implement me.
     info.setLinkCountProfile(new LinkedList<LinkCount>());
   }
 
   public void getLinkProfile(FreiLink link, LinkInfo info) {
     // TODO: Implement me.
     info.setLinkProfile(new LinkedList<LinkData>());
   }
 
   public Vector<FreiLink> getLinks(long time) {
     // TODO: Implement me.
     return new Vector<FreiLink>();
   }
 
   public Hashtable<String, Float> getNodeAvailability(long time) {
     // TODO: Implement me.
     return new Hashtable<String, Float>();
   }
 
   public Vector<FreiNode> getNodeList() {
     return nodes;
   }
 
 }
