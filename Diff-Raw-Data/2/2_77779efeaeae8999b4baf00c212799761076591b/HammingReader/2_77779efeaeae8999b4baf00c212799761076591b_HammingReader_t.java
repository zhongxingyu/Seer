 package ch.wurmlo.week2;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.jgrapht.UndirectedGraph;
 import org.jgrapht.graph.SimpleGraph;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.List;
 
 public class HammingReader {
 
 	private static final Logger log = LoggerFactory.getLogger(HammingReader.class);
 
 	private final UndirectedGraph<LongPoint, Distance> g;
 
 	public HammingReader(String fileName) throws IOException {
 		@SuppressWarnings("unchecked")
 		List<String> list = IOUtils.readLines(HammingReader.class.getResourceAsStream(fileName));
 
         String firstLine = list.get(0);
         String[] firstLineSplit = StringUtils.split(firstLine, " ");
         int numNodes = Integer.valueOf(firstLineSplit[0]);
         int numBits = Integer.valueOf(firstLineSplit[1]);
 
 		g = new SimpleGraph<LongPoint, Distance>(Distance.class);
 		for (String s : list.subList(1, list.size())) {
 			String[] split = StringUtils.split(s, " ");
             if(split.length != numBits) {
                 throw new IOException("lines does not match specified bits: " + s);
             }
             LongPoint nodeA = new LongPoint(Long.parseLong(StringUtils.join(split), 2));
             LongPoint nodeB = new LongPoint(Integer.valueOf(split[1]));
 			int distance = Integer.valueOf(split[2]);
 			g.addVertex(nodeA);
 			g.addVertex(nodeB);
 			log.debug("adding edge with cost {} between vertex {} and {}", distance, nodeA, nodeB);
 			g.addEdge(nodeA, nodeB, new Distance(distance));
 		}
 		if(g.vertexSet().size() != numNodes) {
 			throw new IOException("added nodes does not match number specified in first line");
 		}
 	}
 
	public UndirectedGraph<LongPoint, Distance> getGraph() {
 		return g;
 	}
 }
