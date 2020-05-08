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
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team
  * @version 1.0
  *
  *      Modification history Initial implementation kim.ong@ngc.com
  *
  */
 
 // Note: Version with the has more (...) nodes feature.
 
 public class ViewInHierarchyUtils {
 	int has_more_node_knt = 0;
 
     public ViewInHierarchyUtils() {
 		has_more_node_knt = 0;
 	}
 
     private static void println(PrintWriter out, String text) {
         gov.nih.nci.evs.browser.servlet.AjaxServlet.println(out, text);
     }
 
 
     private String replaceNodeID(String code) {
 		code = code.replaceAll(":", "_");
        code = code.replaceAll("-", "_");
 		return code;
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
 
         String namespace = DataUtils.getNamespaceByCode(codingScheme, version, code);
 
 System.out.println("(*************) namespace: " + namespace);
 
         LexEvsTree tree = service.getTree(codingScheme, csvt, code, namespace);
         List<LexEvsTreeNode> listEvsTreeNode =
                 service.getEvsTreeConverter()
                     .buildEvsTreePathFromRootTree(tree.getCurrentFocus());
 
         LexEvsTreeNode root = null;
         printTree(out, "", code, root, listEvsTreeNode);
 
     }
 
     private void printTree(PrintWriter out, String indent, String focus_code, LexEvsTreeNode parent, List<LexEvsTreeNode> nodes) {
         for (LexEvsTreeNode node : nodes) {
            char c = ' ';
            if (node.getExpandableStatus() == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
                c = node.getPathToRootChildren() != null ? '-' : '+';
            }
            printTreeNode(out, indent, focus_code, node, parent);
            List<LexEvsTreeNode> list_children = node.getPathToRootChildren();
            if (list_children != null) {
                 printTree(out, indent + "  ", focus_code, node, list_children);
            }
         }
     }
 
 
     private void printTreeNode(PrintWriter out, String indent, String focus_code, LexEvsTreeNode node, LexEvsTreeNode parent) {
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
 			    parent_id = replaceNodeID("N_" + parent.getCode());
 		    }
 
 			String code = node.getCode();
 			boolean isHasMoreNode = false;
 			if (code.compareTo("...") == 0) {
 				isHasMoreNode = true;
 				has_more_node_knt++;
 				if (parent == null) {
 					code = "root" + "_dot_" + new Integer(has_more_node_knt).toString();
 				} else {
 				    code = parent.getCode() + "_dot_" + new Integer(has_more_node_knt).toString();
 				}
 			}
 
 			String node_id = replaceNodeID("N_" + code);
 		    String node_label = node.getEntityDescription();
 		    String indentStr = indent + "      ";
 		    String symbol = getNodeSymbol(node);
 
 		    println(out, "");
             println(out, indentStr + "// " + symbol + " " + node_label + "(" + code + ")");
 		    println(out, indentStr + "newNodeDetails = \"javascript:onClickTreeNode('" + code + "');\";");
 		    println(out, indentStr + "newNodeData = { label:\"" + node_label + "\", id:\"" + code + "\", href:newNodeDetails };");
 		    if (expanded) {
 			    println(out, indentStr + "var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", true);");
 
 		    } else if (isHasMoreNode) {
 			    println(out, indentStr + "var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", false);");
 		    } else {
 			    println(out, indentStr + "var " + node_id + " = new YAHOO.widget.TextNode(newNodeData, " + parent_id + ", false);");
 		    }
 
 		    if (expandable || isHasMoreNode) {
 			    println(out, indentStr + node_id + ".isLeaf = false;");
 			    println(out, indentStr + node_id + ".ontology_node_child_count = 1;");
 
 			    //if (node.getPathToRootChildren() == null && !isHasMoreNode)
 			    if (node.getPathToRootChildren() == null) {
 			        println(out, indentStr + node_id + ".setDynamicLoad(loadNodeData);");
 				}
 		    } else {
 				println(out, indentStr + node_id + ".ontology_node_child_count = 0;");
 			    println(out, indentStr + node_id + ".isLeaf = true;");
 		    }
 
 
 		    if (focus_code.compareTo(code) == 0) {
 			    println(out, indentStr + node_id + ".labelStyle = \"ygtvlabel_highlight\";");
 		    }
 		} catch (Exception ex) {
 
 		}
 
     }
 
     private static String getNodeSymbol(LexEvsTreeNode node) {
         String symbol = "@";
         if (node.getExpandableStatus() == LexEvsTreeNode.ExpandableStatus.IS_EXPANDABLE) {
             symbol = node.getPathToRootChildren() != null ? "-" : "+";
         }
         return symbol;
     }
 
 
     public static void main(String[] args) throws Exception {
           new ViewInHierarchyUtils("NCI_Thesaurus", "11.09d", "C37927"); // Color
     }
 
 }
 
