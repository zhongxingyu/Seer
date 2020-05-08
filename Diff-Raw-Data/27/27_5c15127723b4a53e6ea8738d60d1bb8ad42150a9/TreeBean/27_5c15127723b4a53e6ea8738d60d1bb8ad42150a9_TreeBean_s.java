 package gov.nih.nci.cagrid.solr;
 
 import org.apache.solr.common.SolrDocument;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class TreeBean extends SolrDocument {
 
     private List<TreeNode> nodeList = new ArrayList<TreeNode>();
     public static final String NODE_LABEL = "node";
 
     public TreeBean(TreeDescriptor desc) {
         setField("label", desc.getLabel());
         setField("type", desc.getType());
         setField("nodes", nodeList);
     }
 
     public void addNode(TreeNode n) {
         this.nodeList.add(n);
     }
 
     public TreeNode getByName(String name) {
         TreeNode returnNode = null;
         for (Iterator iter = this.nodeList.listIterator(); iter.hasNext();) {
             TreeNode node = (TreeNode) iter.next();
            if (node.getName().equals(name))
                 returnNode = node;
 
         }
 
         return returnNode;
     }
 
 }
