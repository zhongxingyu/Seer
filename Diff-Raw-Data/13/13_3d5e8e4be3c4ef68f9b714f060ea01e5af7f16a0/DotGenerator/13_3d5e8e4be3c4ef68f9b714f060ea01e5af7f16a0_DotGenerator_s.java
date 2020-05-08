 package uk.me.thega.graph;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import fr.loria.GraphViz;
 
 public class DotGenerator {
 
 	private final DotColours colours = new DotColours();
 
 	private int clusterNumber = 0;
 
 	private int nodeNumber = 0;
 
 	private Map<String, String> groupIdColours = new HashMap<String, String>();
 
 	private Map<String, String> nodeNumbersByArtifact = new HashMap<String, String>();
 
 	private GraphViz gv;
 
 	static final String nodeString(final String nodeId, final String label) {
 		return "        " + nodeId + " [label=\"" + label + "\", shape=box];";
 	}
 
 	static final String linkString(final String from, final String to, final String colour, final int penwidth) {
 		return "    " + from + " -> " + to + " [color=" + colour + ",penwidth=" + penwidth + "];";
 	}
 
 	/**
 	 * End the current graph.
 	 */
 	public String end() {
 		gv.addln(gv.end_graph());
 		gv.addln();
 
 		return gv.getDotSource();
 	}
 
 	String getNodeIdForArtifact(final String groupId, final String artifactId) {
 		final String artifactUUID = groupId + ":" + artifactId;
 		if (!nodeNumbersByArtifact.containsKey(artifactUUID)) {
 			nodeNumbersByArtifact.put(artifactUUID, "Node_" + nodeNumber++);
 		}
 		return nodeNumbersByArtifact.get(artifactUUID);
 	}
 
 	public void linkNodesOnGraph(final String depGroupId, final String depArtifactId, final String groupId, final String artifactId, final int importance) {
 		final String dependeeNodeId = getNodeIdForArtifact(groupId, artifactId);
 		final String dependencyNodeId = getNodeIdForArtifact(depGroupId, depArtifactId);
		gv.addln(linkString(dependeeNodeId, dependencyNodeId, groupIdColours.get(groupId), importance));
 	}
 
 	/**
 	 * Start a new graph.
 	 */
 	public void start(final int width, final int height) {
 		gv = new GraphViz();
 		gv.addln(gv.start_graph());
 		gv.addln("    size=\"" + width + "," + height + "\";");
 
 		// Clean up
 		clusterNumber = 0;
 		nodeNumber = 0;
 		groupIdColours.clear();
 		nodeNumbersByArtifact.clear();
 	}
 
 	/**
 	 * Write all of the artifacts to the graph.
 	 * 
 	 * @param cluster do we cluster by group?
 	 * @param artifactsByGroupId the artifacts grouped by groupId
 	 */
 	public void writeNodesToGraph(final boolean cluster, final Map<String, List<String>> artifactsByGroupId) {
 		for (final String groupId : artifactsByGroupId.keySet()) {
 			final List<String> artifactIds = artifactsByGroupId.get(groupId);
 			final String groupIdColour = colours.next();
 			groupIdColours.put(groupId, groupIdColour);
 
 			if (cluster) {
 				gv.addln("    subgraph cluster_" + clusterNumber++ + " {");
 				gv.addln("        label=\"" + groupId + "\";");
 				gv.addln("        color=" + groupIdColour + ";");
 				gv.addln("        style=dashed;");
 			}
 
 			for (final String artifactId : artifactIds) {
 				final String nodeId = getNodeIdForArtifact(groupId, artifactId);
 				gv.addln(nodeString(nodeId, artifactId));
 			}
 
 			if (cluster) {
 				gv.addln("    }");
 			}
 		}
 	}
 }
