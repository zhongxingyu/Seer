 package gov.nih.nci.evs.browser.utils;
 
 import java.io.*;
 import java.util.*;
 
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.lexevs.tree.evstree.EvsTreeConverterFactory;
 import org.lexevs.tree.model.LexEvsTree;
 import org.lexevs.tree.model.LexEvsTreeNode;
 import org.lexevs.tree.service.TreeService;
 import org.lexevs.tree.service.TreeServiceFactory;
 
 import org.lexevs.tree.dao.iterator.ChildTreeNodeIterator;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 
 public class ViewInHierarchyUtil {
     private String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
 	private List tree = null;
 
 	private List tree_data = null;
 
 
     private void log(String line) {
 		if (tree == null) {
 			tree = new ArrayList();
 		}
 		tree.add(line);
 	}
 
     private void logData(int expandable, int depth, boolean expanded, String code, String name) {
 		if (tree_data == null) {
 			tree_data = new ArrayList();
 		}
 		String image = "+";
 		if (expandable == 1) {
 			if (expanded) {
 				image = "-";
 			}
 		} else {
 			image = ".";
 		}
 
 		String line = "" + expandable + "|" + depth + "|" + image + "|" + code + "|" + name;
 		tree_data.add(line);
 	}
 
 
     private void printLexEvsTreeNode(LexEvsTreeNode node, int depth) {
 		if (node == null) return;
 		try {
 			LexEvsTreeNode.ExpandableStatus node_status = node.getExpandableStatus();
 			String image = "[+]";
 			int expandable = 1;
 			if (node_status != LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				image = ".";
 				expandable = 0;
 			}
 
 			boolean expanded = false;
 			if (node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				ChildTreeNodeIterator itr = node.getChildIterator();
 				try {
 					if(itr != null && itr.hasNext()) {
 						expanded = true;
 					}
 				} catch (Exception e) {
 
 				}
 			}
 
 			if (expanded) {
 				image = "[-]";
 			}
 
 
 			log(buildPrefix(depth) + " " + image + " " + node.getEntityDescription() + "(" + node.getCode() + ")");
             logData(expandable, depth, expanded, node.getCode(), node.getEntityDescription());
 
 			if (node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				ChildTreeNodeIterator itr = node.getChildIterator();
 				if (itr != null) {
 					HashSet hset = new HashSet();
 					while(itr.hasNext()){
 						LexEvsTreeNode child = itr.next();
 
 						expanded = true;
 						String child_code = child.getCode();
 						if (!hset.contains(child_code)) {
 							hset.add(child_code);
 							printLexEvsTreeNode(child, depth+1);
 						} else {
 							break;
 						}
 					}
 				}
 			}
 
 
 
 		} catch (Exception ex) {
             //ex.printStackTrace();
 		}
 	}
 
 
     public String getTree(String basePath, String codingScheme, String version, String code, int format) {
 		tree = new ArrayList();
 		tree_data = new ArrayList();
 
 		tree_data = getTree(codingScheme, version, code, 1);
 
 		System.out.println("(********* getTree returns " + tree_data.size());
 		return toHTML(basePath, tree_data);
 	}
 
 
 
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 //  View In Hierarchy Tree
 
 
 
 
     private void printTreeNode(PrintWriter out, String code, String node_label, String parent_code, boolean expand) {
       String node_id = "N_" + code;
       String parent_id = null;
 
       if (parent_code == null) {
 		  parent_id = "root";
 	  } else {
 		  parent_id = "N_" + parent_code;
 	  }
 
       out.println("newNodeDetails = \"javascript:onClickTreeNode('" + code + "');\";");
 
       out.println("newNodeData = { label:\"" + node_label + "\", id:\"" + code + "\", href:newNodeDetails };");
       if (expand) {
 	      out.println("    var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", true);");
 	  } else {
 	      out.println("    var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", false);");
 	  }
 
   }
 
 
 
 
     private void printLexEvsTreeNode(PrintWriter out, LexEvsTreeNode node, LexEvsTreeNode parent) {
 		if (node == null) return;
 		try {
 			LexEvsTreeNode.ExpandableStatus node_status = node.getExpandableStatus();
 			String image = "[+]";
 			int expandable = 1;
 			if (node_status != LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				image = ".";
 				expandable = 0;
 			}
 
 			boolean expanded = false;
 			if (node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				ChildTreeNodeIterator itr = node.getChildIterator();
 				try {
 					if(itr != null && itr.hasNext()) {
 						expanded = true;
 					}
 				} catch (Exception e) {
 
 				}
 			}
 
 			if (expanded) {
 				image = "[-]";
 			}
 
 
             String parent_code = null;
             if (parent != null) {
 			    parent_code = parent.getCode();
 			}
			// tree node expanded?
		    //if (expandable == 1) expanded = true;
 
             printTreeNode(out, node.getCode(), node.getEntityDescription(), parent_code, expanded);
 
 			if (node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 				ChildTreeNodeIterator itr = node.getChildIterator();
 				if (itr != null) {
 					HashSet hset = new HashSet();
 					while(itr.hasNext()){
 						LexEvsTreeNode child = itr.next();
 
 						expanded = true;
 						String child_code = child.getCode();
 						if (!hset.contains(child_code)) {
 							hset.add(child_code);
 							printLexEvsTreeNode(out, child, node);
 						} else {
 							break;
 						}
 					}
 				}
 			}
 
 
 
 		} catch (Exception ex) {
             //ex.printStackTrace();
 		}
 	}
 
 
 
     public void printTree(PrintWriter out, String codingScheme, String version, String code) {
 
 
 		out.println("var newNodeDetails = \"\";");
 		out.println("var newNodeData = \"\";");
 
         tree = new ArrayList();
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (version != null)
             versionOrTag.setVersion(version);
 
         TreeService treeService =
             TreeServiceFactory.getInstance().getTreeService(
                 RemoteServerUtil.createLexBIGService());
 
         LexEvsTree lexEvsTree = treeService.getTree(codingScheme, versionOrTag, code);
 
         LexEvsTreeNode top_node = lexEvsTree.findNodeInTree("@@");
         if (top_node == null) top_node = lexEvsTree.findNodeInTree("@");
 
 		LexEvsTreeNode.ExpandableStatus top_node_status = top_node.getExpandableStatus();
 		if (top_node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 
 			ChildTreeNodeIterator itr = top_node.getChildIterator();
 			HashSet hset = new HashSet();
 
 			while(itr.hasNext()){
 				LexEvsTreeNode child = itr.next();
 
 				String child_code = child.getCode();
 
 				if (!hset.contains(child_code)) {
 					hset.add(child_code);
                     printLexEvsTreeNode(out, child, null);
 				} else {
 					//System.out.println("DUPLICATES?????? " + child_code);
 					break;
 				}
 		    }
 		}
     }
 
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 
     public List getTree(String codingScheme, String version, String code) {
 		return getTree(codingScheme, version, code, 0);
 	}
 
     public List getTree(String codingScheme, String version, String code, int format) {
         tree = new ArrayList();
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (version != null)
             versionOrTag.setVersion(version);
 
         TreeService treeService =
             TreeServiceFactory.getInstance().getTreeService(
                 RemoteServerUtil.createLexBIGService());
 
         LexEvsTree lexEvsTree = treeService.getTree(codingScheme, versionOrTag, code);
 
         LexEvsTreeNode top_node = lexEvsTree.findNodeInTree("@@");
         if (top_node == null) top_node = lexEvsTree.findNodeInTree("@");
 
 		LexEvsTreeNode.ExpandableStatus top_node_status = top_node.getExpandableStatus();
 		if (top_node_status == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
 
 			ChildTreeNodeIterator itr = top_node.getChildIterator();
 			HashSet hset = new HashSet();
 
 			while(itr.hasNext()){
 				LexEvsTreeNode child = itr.next();
 
 				String child_code = child.getCode();
 
 				if (!hset.contains(child_code)) {
 					hset.add(child_code);
                     printLexEvsTreeNode(child, 0);
 				} else {
 
 					System.out.println("DUPLICATES?????? " + child_code);
 					break;
 				}
 		    }
 		}
 		if (format == 0) return tree;
 		return tree_data;
     }
 
 
 	private String buildPrefix(int depth) {
 		String prefix = "";
 		for (int i = 0; i < depth; i++) {
 			prefix = prefix + " -> ";
 		}
 		return prefix;
 	}
 
 
 	private void printNode(LexEvsTreeNode node, int depth) {
 		System.out.println(buildPrefix(depth) + "Code: " + node.getCode()
 				+ ", Description: " + node.getEntityDescription() + " Hash: "
 				+ node.hashCode());
 	}
 
 	private void printMyTree(List<LexEvsTreeNode> nodes, int depth) {
 		for (LexEvsTreeNode node : nodes) {
 		   printNode(node, depth);
 		   List<LexEvsTreeNode> list_children = node.getPathToRootChildren();
 		   if (list_children != null) {
 				printMyTree(list_children, depth + 1);
 		   }
 		}
 	}
 
 	private String buildPrefix(String depth_str) {
 		int depth = Integer.parseInt(depth_str);
 		String prefix = "";
 		for (int i = 0; i < depth; i++) {
 			prefix = prefix + INDENT;
 		}
 		return prefix;
 	}
 
 	public String toHTML(String basePath, List tree_data) {
         String t = "";
 		for (int i=0; i<tree_data.size(); i++) {
 			String line = (String) tree_data.get(i);
 			List a = toHTML(basePath, line);
 			t = t + "<tr><td>";
 			for (int k=0; k<a.size(); k++) {
 				String s = (String) a.get(k);
 				t = t + s;
 			}
 			t = t + "</td></tr>";
 		}
         return t;
 	}
 
 
 
 	public List toHTML(String basePath, String line) {
 		Vector v = parseData(line);
 		String expandable = (String) v.elementAt(0);
 		String depth = (String) v.elementAt(1);
 		String image = (String) v.elementAt(2);
 		String code = (String) v.elementAt(3);
 		String name = (String) v.elementAt(4);
 
 		List list = new ArrayList();
 
 		//String s = "<div id=\"" + code + "\">";
 		//list.add(s);
 		String s = "";
 		String image_file = "plus.gif";
 		if (image.compareTo("-") == 0) {
 			image_file = "minus.gif";
 		} else if (image.compareTo(".") == 0) {
 			image_file = "leaf.gif";
 		}
 
 		int level = Integer.parseInt(depth);
 		if (image.compareTo(".") == 0) {
 		    s = buildPrefix(depth) + "&nbsp;" + name;
 
 /*
 		} else if (image.compareTo("+") == 0) {
 		    s = buildPrefix(depth) + "<a onClick=\"toggle(this)\"><img src=\"" + basePath + "/images/" + image_file + "\" /></a>&nbsp;" + name;
 
 		} else if (image.compareTo("-") == 0) {
 
 		    s = buildPrefix(depth) + "<img src=\"" + basePath + "/images/" + image_file + "\" onClick=\"collapse(this, '" +
 		        code + "', '" + name
 		        + "')\"/>&nbsp;" + name;
 
 		    //s = buildPrefix(depth) + "<a onClick=\"toggle(this)\"><img src=\"" + basePath + "/images/" + image_file + "\" /></a>&nbsp;" + name;
 */
 
         } else if (image.compareTo("-") == 0) {
             s = buildPrefix(depth) + "<a onClick=\"toggle(this)\">&nbsp;<img src=\"" + basePath + "/images/" + image_file + "\" /></a><div>";
 
         } else if (image.compareTo("+") == 0) {
             s = buildPrefix(depth) + "<a onClick=\"toggle(this)\">&nbsp;<img src=\"" + basePath + "/images/" + image_file + "\" /></a><div>";
 
 	    }
 		list.add(s);
 		//list.add("</div>");
 
         return list;
 	}
 
 
     public Vector<String> parseData(String line) {
 		if (line == null) return null;
         String tab = "|";
         return parseData(line, tab);
     }
 
     public Vector<String> parseData(String line, String tab) {
 		if (line == null) return null;
         Vector data_vec = new Vector();
         StringTokenizer st = new StringTokenizer(line, tab);
         while (st.hasMoreTokens()) {
             String value = st.nextToken();
             if (value.compareTo("null") == 0)
                 value = " ";
             data_vec.add(value);
         }
         return data_vec;
     }
 
 
 
     public String get_tree(String basePath, String codingScheme, String version, String code, int format) {
             System.out.println("\n============================= util.getTree(codingScheme, version, code, 1) ");
 			List tree_data = getTree(codingScheme, version, code, 1);
 			String html_line = toHTML(basePath, tree_data);
 			System.out.println(html_line);
 			return html_line;
     }
 
 
 
 	public static void main(String[] args) throws Exception {
 
         String codingScheme = "NCI Thesaurus";
         String version = "11.09d";
         String code = "C37927";
         String basePath = "/ncitbrowser";
 
         System.out.println("\n============================= util.getTree(codingScheme, version, code) ");
 		List tree = new ViewInHierarchyUtil().getTree(codingScheme, version, code);
 		for (int i=0; i<tree.size(); i++) {
 			int j = i+1;
 			System.out.println("(" + j + ")" + (String) tree.get(i));
 		}
 
         System.out.println("\n============================= util.getTree(codingScheme, version, code, 1) ");
         ViewInHierarchyUtil util = new ViewInHierarchyUtil();
 		List tree_data = util.getTree(codingScheme, version, code, 1);
 		for (int i=0; i<tree_data.size(); i++) {
 			int j = i+1;
 			System.out.println("(" + j + ")" + (String) tree_data.get(i));
 		}
 
 		String html_line = new ViewInHierarchyUtil().toHTML(basePath, tree_data);
 		System.out.println(html_line);
 
 
         String ans = new ViewInHierarchyUtil().get_tree(basePath, codingScheme, version, code, 1);
 		System.out.println(ans);
 
 /*
         System.out.println("\n=============================");
 		List html = util.toHTML(basePath, tree_data);
 		for (int i=0; i<html.size(); i++) {
 			System.out.println((String) html.get(i));
 		}
 */
 /*
 		System.out.println("\n=============================");
 		html_line = util.getTree(basePath, codingScheme, version, code, 1);
 		System.out.println(html_line);
 */
 
 
 /*
 
 		tree_data = util.getTree(codingScheme, version, code, 1);
 		html_line = util.toHTML(basePath, tree_data);
 		System.out.println(html_line);
 
 */
 
 	}
 
 }
 
 
