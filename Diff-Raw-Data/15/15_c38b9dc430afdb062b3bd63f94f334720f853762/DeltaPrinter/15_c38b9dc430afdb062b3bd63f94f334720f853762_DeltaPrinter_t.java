 package wikipedia.analysis.pagenetwork;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateMidnight;
 
 import wikipedia.network.GraphEdge;
 import wikipedia.network.TimeFrameGraph;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class DeltaPrinter {
 
     public static void main(final String[] args) {
         List<DateMidnight> allTimeFrames = Lists.newArrayList(new DateMidnight(2010, 12, 1));//,
 //                                                          new DateMidnight(2011, 1, 1),
 //                                                          new DateMidnight(2011, 2, 1),
 //                                                          new DateMidnight(2011, 3, 1),
 //                                                          new DateMidnight(2011, 4, 1));
 
         final List<String> categories = CategoryLists.ENGLISH_MUSIC;
         Map<DateMidnight, TimeFrameGraph> dateGraphMap = Maps.newHashMap();
 
         for (DateMidnight dateTime : allTimeFrames) {
             dateGraphMap.put(dateTime, new NetworkBuilder(categories, "en", dateTime).getGraphAtDate());
         }
 
         System.out.println(generateTimeFrameInformation(Lists.newArrayList(dateGraphMap.values())));
     }
 
 
     public static String generateTimeFrameInformation(final List<TimeFrameGraph> allFrameGraphs) {
         StringBuilder jsonOutput = new StringBuilder();
         jsonOutput.append(printInitialGraph(allFrameGraphs.get(0)));
         jsonOutput.append(printAllDeltaGraphs());
         return jsonOutput.toString();
     }
 
     private static String printAllDeltaGraphs() {
         // TODO Auto-generated method stub
         return "";
 
     }
 
     private static String printInitialGraph(final TimeFrameGraph timeFrameGraph) {
         return printCompleteGraphAsJSON(timeFrameGraph);
     }
 
     private static String printCompleteGraphAsJSON(final TimeFrameGraph timeFrameGraph) {
         StringBuilder completeGraphJson = new StringBuilder();
 
         completeGraphJson.append("var initialGraph = {");
         completeGraphJson.append("nodes: [");
 
         //print all nodes
        List<String> allNodes = Lists.newArrayList();
         Set<String> allNodeNames = timeFrameGraph.getNameIndexMap().keySet();
         for (String nodeName : allNodeNames) {
             String fixedName = StringUtils.replace(nodeName, "\"", ""); // FIXME will break graph
            allNodes.add("{nodeName: \"" + fixedName + "\", group: 1}");
         }
        completeGraphJson.append(StringUtils.join(allNodes, ", \n"));
         completeGraphJson.append("], links:[");
 
         //print all edges
        List<String> allEdges = Lists.newArrayList();
         Map<String, Integer> nameIndexMap = timeFrameGraph.getNameIndexMap();
         for (GraphEdge edge : timeFrameGraph.getAllEdges()) {
            allEdges.add("{source: " + nameIndexMap.get(edge.getFrom()) + ", target: " + nameIndexMap.get(edge.getTo()) + ", value: " + 1 + "}");
         }
        completeGraphJson.append(StringUtils.join(allEdges, ", \n"));
 
         completeGraphJson.append("] };");
         return completeGraphJson.toString();
     }
 
 }
