 package gov.nih.nci.evs.browser.utils;
 
 import java.io.*;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.LexBIG.Utility.Constructors;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.lexevs.tree.json.JsonConverter;
 import org.lexevs.tree.json.JsonConverterFactory;
 import org.lexevs.tree.model.LexEvsTree;
 import org.lexevs.tree.model.LexEvsTreeNode;
 import org.lexevs.tree.model.LexEvsTreeNode.ExpandableStatus;
 import org.lexevs.tree.service.TreeService;
 import org.lexevs.tree.service.TreeServiceFactory;
 
 public class ViewInHierarchyUtils {
 	int has_more_node_knt = 0;
 
     public ViewInHierarchyUtils() {
 		has_more_node_knt = 0;
 	}
 
     public ViewInHierarchyUtils(String codingScheme, String version, String code) {
 		has_more_node_knt = 0;
         try {
 			PrintWriter pw = new PrintWriter(System.out, true);
             printTree(pw, codingScheme, version, code);
         } catch (Exception e) {
             System.out.println(e.getClass().getName() + ": " + e.getMessage());
         }
     }
 
     public void printTree(PrintWriter out, String codingScheme, String version, String code) {
         TreeService service =
                 TreeServiceFactory.getInstance().getTreeService(
                     RemoteServerUtil.createLexBIGService());
 
         long start = System.currentTimeMillis();
         CodingSchemeVersionOrTag csvt = null;
         if (version != null && version.length() > 0)
             csvt = Constructors.createCodingSchemeVersionOrTagFromVersion(version);
         LexEvsTree tree = service.getTree(codingScheme, csvt, code);
         List<LexEvsTreeNode> listEvsTreeNode =
                 service.getEvsTreeConverter()
                     .buildEvsTreePathFromRootTree(tree.getCurrentFocus());
 
         LexEvsTreeNode root = null;
         printTree(out, code, root, listEvsTreeNode);
 
     }
 
     private void printTree(PrintWriter out, String focus_code, LexEvsTreeNode parent, List<LexEvsTreeNode> nodes) {
         for (LexEvsTreeNode node : nodes) {
            char c = ' ';
            if (node.getExpandableStatus() == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
                c = node.getPathToRootChildren() != null ? '-' : '+';
            }
            printTreeNode(out, focus_code, node, parent);
            List<LexEvsTreeNode> list_children = node.getPathToRootChildren();
            if (list_children != null) {
                 printTree(out, focus_code, node, list_children);
            }
         }
     }
 
     private void printTreeNode(PrintWriter out, String focus_code, LexEvsTreeNode node, LexEvsTreeNode parent) {
 		if (node == null) return;
 
 
 		try {
 			LexEvsTreeNode.ExpandableStatus node_status = node.getExpandableStatus();
 			String image = "[+]";
 			boolean expandable = true;
 			if (node_status != LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				image = ".";
 				expandable = false;
 			}
 
 			boolean expanded = false;
 
 			if (node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 
 				List<LexEvsTreeNode> list_children = node.getPathToRootChildren();
 				if (list_children != null && list_children.size() > 0) {
 					expanded = true;
 				}
 			}
 
             String parent_code = null;
             if (parent != null) {
 			    parent_code = parent.getCode();
 			}
 
             String parent_id = null;
 		    if (parent == null) {
 			    parent_id = "root";
 		    } else {
 			    parent_id = "N_" + parent.getCode();
 		    }
 
 			String code = node.getCode();
 			if (code.compareTo("...") == 0) {
 				has_more_node_knt++;
 				if (parent == null) {
 					code = "root" + "_dot_" + new Integer(has_more_node_knt).toString();
 				} else {
 				    code = parent.getCode() + "_dot_" + new Integer(has_more_node_knt).toString();
 				}
 			}
 
 			String node_id = "N_" + code;
 		    String node_label = node.getEntityDescription();
 
 		    out.println("newNodeDetails = \"javascript:onClickTreeNode('" + code + "');\";");
 
 		    out.println("newNodeData = { label:\"" + node_label + "\", id:\"" + code + "\", href:newNodeDetails };");
 		    if (expanded) {
 			    out.println("    var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", true);");
 		    } else {
 			    out.println("    var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", false);");
 		    }
 
 		    if (expandable) {
 			    out.println(node_id + ".isLeaf = false;");
			    //KLO
			    out.println(node_id + ".ontology_node_child_count = 1;");

 			    out.println(node_id + ".setDynamicLoad(loadNodeData);");
 		    } else {
				out.println(node_id + ".ontology_node_child_count = 0;");
 			    out.println(node_id + ".isLeaf = true;");
 		    }
 
 		    if (focus_code.compareTo(code) == 0) {
 			    out.println(node_id + ".labelStyle = \"ygtvlabel_highlight\";");
 		    }
 		} catch (Exception ex) {
 
 		}
 
     }
 
 
     public static void main(String[] args) throws Exception {
           new ViewInHierarchyUtils("NCI_Thesaurus", "11.09d", "C37927"); // Color
     }
 
 }
 
