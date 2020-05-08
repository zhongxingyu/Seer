 package wikipedia.analysis.pagenetwork;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.graphstream.algorithm.Dijkstra;
 import org.graphstream.graph.implementations.DefaultGraph;
 import org.graphstream.graph.implementations.MultiGraph;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import util.MapSorter;
 import wikipedia.database.DBUtil;
 import wikipedia.network.GraphEdge;
 import wikipedia.network.TimeFrameGraph;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 /**
  * Builds a Network structure based on all pages in the given categories
  */
 public final class NetworkBuilder {
 
     private static final Logger LOG = LoggerFactory.getLogger(NetworkBuilder.class.getName());
 
     private final String revisionDateTime;
     private final DBUtil database;
     private static final int MIN_INDEGREE = 120;
 
     private final Map<Integer, String> allPagesInNetwork;
 
     private static final int NUM_THREADS = 8;
     private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
 
     private final DateTime dateTime;
 
     public NetworkBuilder(final Map<Integer, String> allPagesInNetwork, final String lang,
             final DateMidnight dateMidnight, final DBUtil database) {
         this.dateTime = dateMidnight.toDateTime();
         this.revisionDateTime = dateMidnight.toString(DBUtil.MYSQL_DATETIME_FORMATTER);
         this.database = database;
         this.allPagesInNetwork = allPagesInNetwork;
     }
 
     public TimeFrameGraph getGraphAtDate(final List<String> nodeDebug) {
             List<GraphEdge> allLinksInNetwork = buildAllLinksWithinNetwork(allPagesInNetwork, revisionDateTime);
         Map<String, List<String>> indegreeMatrix = initIndegreeMatrix(allLinksInNetwork);
         Map<String, Integer> nameIndexMap = Maps.newLinkedHashMap();
 
         final int numberOfLinks = allLinksInNetwork.size();
         Map<String, Float> pageIndegMap = Maps.newHashMap();
         for (String targetPage : indegreeMatrix.keySet()) {
             /*if (nodeQualifiedForGraph(indegreeMatrix, targetPage, nodeDebug, numberOfLinks)
                     /*&& !nameIndexMap.containsKey(targetPage)) {
                 nameIndexMap.put(targetPage, nodeIndex++);
             }*/
             List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
             int totalNumberOfLinks = allIncommingLinks.size();
             final float magicNumber = ((float) totalNumberOfLinks / (float) numberOfLinks) * 1000;
             pageIndegMap.put(targetPage, magicNumber);
         }
 
         // create graph using gs
         DefaultGraph graph = new MultiGraph("g1", false, true);
         Random r = new Random();
         for (GraphEdge edge : allLinksInNetwork) {
             graph.addEdge(edge.getFrom() + edge.getTo() + r.nextDouble(), edge.getFrom(), edge.getTo());
         }
 
        Dijkstra d = new Dijkstra(Dijkstra.Element.edge, "weight", "Justin Bieber");
         LOG.info("BEFORE INIT");
         d.init(graph);
         LOG.info("BEFORE COMPUTE");
         d.compute();
 
         //indeg
         int nodeIndex = 0;
         Map<String, Float> allPagesOrderedByIndeg = new MapSorter<String, Float>().sortByValue(pageIndegMap);
         for (Entry<String, Float> pageIndegEntry : allPagesOrderedByIndeg.entrySet()) {
             if (nodeIndex >= 30) {
                 break;
             }
             nameIndexMap.put(pageIndegEntry.getKey(), nodeIndex++);
         }
 
         //direct neighbors
         for (String pageName : pageIndegMap.keySet()) {
             double shortestPathLength = d.getShortestPathLength(graph.getNode(pageName));
             //LOG.info("calucladed shortest path: " + shortestPathLength);
             if ((shortestPathLength <= 1 && allPagesOrderedByIndeg.get(pageName) >= 1.5 && !nameIndexMap.containsKey(pageName)) || shortestPathLength < 1) {
                 LOG.info("adding close neighbor: " + pageName + "magic Number: " + allPagesOrderedByIndeg.get(pageName));
                 nameIndexMap.put(pageName, nodeIndex++);
             }
         }
 
 
 
 
 
         List<GraphEdge> edgeOutput = Lists.newArrayList();
         // TODO loop not optimal
         for (Entry<String, List<String>> entry : indegreeMatrix.entrySet()) {
             String targetPageName = entry.getKey();
             List<String> incommingLinks = entry.getValue();
             for (String sourcePageName : incommingLinks) {
                 if (sourcePageName.equals(targetPageName)) {
                     continue;
                 }
                 if (nameIndexMap.get(sourcePageName) != null
                         && nameIndexMap.get(targetPageName) != null) {
                     edgeOutput.add(new GraphEdge(sourcePageName, targetPageName));
                 }
             }
         }
         return new TimeFrameGraph(nameIndexMap, edgeOutput, dateTime);
     }
 
 
     private boolean nodeQualifiedForGraph(final Map<String, List<String>> indegreeMatrix,
                                           final String targetPage, final List<String> nodeDebug, final int numberOfLinks) {
         List<String> allIncommingLinks = indegreeMatrix.get(targetPage);
         int totalNumberOfLinks = allIncommingLinks.size();
         //final boolean nodeQualified = inDegree >= MIN_INDEGREE;
         final float magicNumber = ((float) totalNumberOfLinks / (float) numberOfLinks) * 1000;
         final boolean nodeQualified = magicNumber >= 1.35f;
        // Set<String> selectedPeople = Sets.newHashSet("Michael Jackson", "Justin Bieber", "Lady Gaga", "Bob Dylan", "Elvis Presley");
 //        if(selectedPeople.contains(targetPage)) {
         if (magicNumber > 1.25) {
             nodeDebug.add(revisionDateTime + "=" + targetPage + "=" + totalNumberOfLinks + "=" + magicNumber);
         }
         return nodeQualified;
     }
 
     private Map<String, List<String>> initIndegreeMatrix(final List<GraphEdge> allLinksInNetwork) {
         // In-degree matrix key:target_page, value:source_page
         // value: all pages that point to target_page (key)
         Map<String, List<String>> indegreeMatrix = Maps.newHashMap();
         for (GraphEdge link : allLinksInNetwork) {
             String from = link.getFrom();
             String to = link.getTo();
             if (indegreeMatrix.containsKey(to)) {
                 indegreeMatrix.get(to).add(from);
             } else {
                 indegreeMatrix.put(to, Lists.newArrayList(from));
             }
         }
         return indegreeMatrix;
     }
 
     private List<GraphEdge> buildAllLinksWithinNetwork(final Map<Integer, String> allPagesInNetworkList, final String revisionDateTime) {
         Set<String> allPageNamesInNetwork = Sets.newHashSet(allPagesInNetworkList.values());
         final List<GraphEdge> allLinksInNetwork = Collections.synchronizedList(Lists
                 .<GraphEdge> newArrayList());
         LOG.info("Number of Tasks: " + allPageNamesInNetwork.size());
         int taskCounter = 1;
         try {
             for (Entry<Integer, String> entry : allPagesInNetworkList.entrySet()) {
                 final int pageId = entry.getKey();
                 final String pageName = entry.getValue();
                 threadPool.execute(new SQLExecutor(pageId, allPageNamesInNetwork, pageName,
                         allLinksInNetwork, taskCounter++, revisionDateTime));
             }
         } finally {
             shutdownThreadPool();
         }
         return allLinksInNetwork;
     }
 
     private void shutdownThreadPool() {
         threadPool.shutdown();
         try {
             threadPool.awaitTermination(1, TimeUnit.MINUTES);
         } catch (InterruptedException e) {
             LOG.error("Error while shutting down Threadpool", e);
         }
         while (!threadPool.isTerminated()) {
             LOG.debug("Waiting for all Tasks to terminate");
         }
     }
 
     /**
      * Fetches Information from DB and calculates all incomming links to a given
      * page in the network
      */
     private final class SQLExecutor implements Runnable {
         private static final int LOG_MODULO = 4000;
         private final int pageId;
         private final Set<String> allPageNamesInNetwork;
         private final String pageName;
         private final List<GraphEdge> allLinksInNetwork;
         private final int counter;
         private final String revisionDateTime;
 
         private SQLExecutor(final int pageId, final Set<String> allPageNamesInNetwork,
                 final String pageName, final List<GraphEdge> allLinksInNetwork2, final int counter,
                 final String revisionDateTime) {
             this.pageId = pageId;
             this.allPageNamesInNetwork = allPageNamesInNetwork;
             this.pageName = pageName;
             this.allLinksInNetwork = allLinksInNetwork2;
             this.counter = counter;
             this.revisionDateTime = revisionDateTime;
         }
 
         @Override
         public void run() {
             if (counter % LOG_MODULO == 0) {
                 LOG.info("Task: " + counter);
             }
             Collection<String> allOutgoingLinksOnPage = database.getAllLinksForRevision(pageId,
                     revisionDateTime);
             for (String outgoingLink : allOutgoingLinksOnPage) {
                 if (allPageNamesInNetwork.contains(outgoingLink)) {
                     allLinksInNetwork.add(new GraphEdge(pageName, outgoingLink));
                 }
             }
         }
     }
 
 }
