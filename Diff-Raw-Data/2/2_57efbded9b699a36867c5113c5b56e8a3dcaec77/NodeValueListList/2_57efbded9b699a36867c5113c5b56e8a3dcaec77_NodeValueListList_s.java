 package dna.series.lists;
 
 import java.io.IOException;
 
 import dna.io.filesystem.Files;
 import dna.series.data.NodeValueList;
 
 /**
  * An NodeValueListList lists NodeValueList objects.
  * 
  * @author Rwilmes
  * @date 04.07.2013
  */
 public class NodeValueListList extends List<NodeValueList> {
 
 	public NodeValueListList() {
 		super();
 	}
 
 	public NodeValueListList(int size) {
 		super(size);
 	}
 
 	public void write(String dir) throws IOException {
 		for (NodeValueList n : this.getList()) {
			n.write(dir, Files.getDistributionFilename(n.getName()));
 		}
 	}
 
 	public static NodeValueListList read(String dir, boolean readValues)
 			throws IOException {
 		String[] NodeValueLists = Files.getNodeValueLists(dir);
 		NodeValueListList list = new NodeValueListList(NodeValueLists.length);
 		for (String nodeValueList : NodeValueLists) {
 			list.add(NodeValueList.read(dir, nodeValueList,
 					Files.getNodeValueListName(nodeValueList), readValues));
 		}
 		return list;
 	}
 }
