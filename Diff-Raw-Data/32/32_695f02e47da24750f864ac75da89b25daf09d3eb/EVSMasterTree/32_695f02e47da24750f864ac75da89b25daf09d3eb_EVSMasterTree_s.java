 // Copyright (c) 2000 ScenPro, Inc.
 package com.scenpro.NCICuration;
 import gov.nih.nci.EVS.domain.*;
 import gov.nih.nci.EVS.search.*;
 import gov.nih.nci.EVS.exception.*;
 import gov.nih.nci.common.util.*;
 import gov.nih.nci.common.exception.*;
 import java.sql.*;
 import java.util.*;
 import javax.servlet.http.*;
 import javax.servlet.ServletContext.*;
 
 /**
  * EVSMasterTree class generates trees and renders the trees as HTML.
  * @author Tom Phillips
  */
  
 /*
  * The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro)  
 Copyright Notice.  The software subject to this notice and license includes both
 human readable source code form and machine readable, binary, object code form
 (the CaCORE Software).  The CaCORE Software was developed in conjunction with
 the National Cancer Institute (NCI) by NCI employees and employees of SCENPRO.
 To the extent government employees are authors, any rights in such works shall
 be subject to Title 17 of the United States Code, section 105.    
 This CaCORE Software License (the License) is between NCI and You.  You (or Your)
 shall mean a person or an entity, and all other entities that control, are 
 controlled by, or are under common control with the entity.  Control for purposes
 of this definition means (i) the direct or indirect power to cause the direction
 or management of such entity, whether by contract or otherwise, or (ii) ownership
 of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial 
 ownership of such entity.  
 This License is granted provided that You agree to the conditions described below.
 NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge,
 irrevocable, transferable and royalty-free right and license in its rights in the
 CaCORE Software to (i) use, install, access, operate, execute, copy, modify, 
 translate, market, publicly display, publicly perform, and prepare derivative 
 works of the CaCORE Software; (ii) distribute and have distributed to and by 
 third parties the CaCORE Software and any modifications and derivative works 
 thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to 
 third parties, including the right to license such rights to further third parties.
 For sake of clarity, and not by way of limitation, NCI shall have no right of 
 accounting or right of payment from You or Your sublicensees for the rights 
 granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above
 copyright notice, this list of conditions and the disclaimer and limitation of
 liability of Article 6, below.  Your redistributions in object code form must
 reproduce the above copyright notice, this list of conditions and the disclaimer
 of Article 6 in the documentation and/or other materials provided with the 
 distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment: This product includes software developed 
 by SCENPRO and the National Cancer Institute.  If You do not include such end-user
 documentation, You shall include this acknowledgment in the Software itself, 
 wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" ScenPro, Inc.
 and "SCENPRO" to endorse or promote products derived from this Software.  
 This License does not authorize You to use any trademarks, service marks, trade names,
 logos or product names of either NCI or SCENPRO, except as required to comply with
 the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this
 Software into Your proprietary programs and into any third party proprietary 
 programs.  However, if You incorporate the Software into third party proprietary
 programs, You agree that You are solely responsible for obtaining any permission
 from such third parties required to incorporate the Software into such third party
 proprietary programs and for informing Your sublicensees, including without 
 limitation Your end-users, of their obligation to secure any required permissions
 from such third parties before incorporating the Software into such third party
 proprietary software programs.  In the event that You fail to obtain such permissions,
 You agree to indemnify NCI for any claims against NCI by such third parties, 
 except to the extent prohibited by law, resulting from Your failure to obtain
 such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own 
 copyright statement to Your modifications and to the derivative works, and You 
 may provide additional or different license terms and conditions in Your sublicenses
 of modifications of the Software, or any derivative works of the Software as a 
 whole, provided Your use, reproduction, and distribution of the Work otherwise 
 complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  
 IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR AFFILIATES 
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 public class EVSMasterTree
 {
   // class variables
 	private static Hashtable m_treesHash; // nodeName, with tree of it's children
 	private static Hashtable m_treeNodesHash;	// holds an easy-lookup of open nodes while rendering HTML
   private static Hashtable m_treesHashParent; // nodeName, with tree of it's children
 	private static Hashtable m_treeNodesHashParent;	// h
   private static Stack m_expandedTreeNodes;
   private static Stack m_expandedTreeNodesParent;
   private static Vector m_expandedTreeNodesVector;
   private String treeName;
 	private String collapsedImage="plus.png";			// image for collapsed nodes
 	private String openedImage="ominus.png";			// image for expanded nodes
 	private String leafImage="dot.png";				// image for leaf nodes
 	private String detailsImage="details.gif";				
   private HttpServletRequest m_classReq = null;
   private HttpServletResponse m_classRes = null;
   private String m_dtsVocab = "";
   private String urlSearch ="/cdecurate/NCICurationServlet?reqType=treeSearch&&keywordID="; //&&vocab=
   private String urlExpand ="/cdecurate/NCICurationServlet?reqType=treeExpand&&nodeName="; //&&vocab=
   private String urlCollapse ="/cdecurate/NCICurationServlet?reqType=treeCollapse&&nodeName="; //&&vocab=
   private int level = 1;
   private int nodeID = 1;
   private javax.servlet.ServletContext m_ServletContext;
   private Vector m_vRootNames = new Vector();
   private Vector m_vRootCodes = new Vector();
   private String sSearchAC = "";
   private NCICurationServlet m_servlet = null;
  
   /**
    * Constructs a new instance.
    * @param req   The HttpServletRequest object.
    * @param dtsVocab  Vocabulary name.
    * @param CurationServlet  The Curation Tool servlet.
    */
   public EVSMasterTree(HttpServletRequest req, String dtsVocab, NCICurationServlet CurationServlet)
   {
     m_servlet = CurationServlet;
     m_classReq = req;
     m_dtsVocab = dtsVocab;
     
     // Use the servlet's official Vocabulary names
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
      || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = m_servlet.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = m_servlet.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = m_servlet.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = m_servlet.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = m_servlet.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = m_servlet.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = m_servlet.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = m_servlet.m_VOCAB_HL7;
       
     // retrieve info stored in servletContext 
     if(m_ServletContext != null)
       m_treesHash = ( Hashtable)m_ServletContext.getAttribute("treesHash");
     if(m_treesHash == null) 
       m_treesHash = new Hashtable();
     if(m_ServletContext != null)
      m_treeNodesHash = (Hashtable)m_ServletContext.getAttribute("treeNodesHash");    
     if(m_treeNodesHash == null) 
       m_treeNodesHash = new Hashtable();
     if(m_ServletContext != null)
      m_expandedTreeNodes = (Stack)m_ServletContext.getAttribute("expandedTreeNodes");    
     if(m_expandedTreeNodes == null) 
       m_expandedTreeNodes = new Stack();
       
      if(m_ServletContext != null)
       m_treesHashParent = ( Hashtable)m_ServletContext.getAttribute("treesHashParent");
     if(m_treesHashParent == null) 
       m_treesHashParent = new Hashtable();
     if(m_ServletContext != null)
      m_treeNodesHashParent = (Hashtable)m_ServletContext.getAttribute("treeNodesHashParent");    
     if(m_treeNodesHashParent == null) 
       m_treeNodesHashParent = new Hashtable();
     if(m_ServletContext != null)
      m_expandedTreeNodesParent = (Stack)m_ServletContext.getAttribute("expandedTreeNodesParent");    
     if(m_expandedTreeNodesParent == null) 
       m_expandedTreeNodesParent = new Stack();
   }
   
 /**
    * Sets the tree name
    * @param str   The Tree name.
 */
   public void	setTreeName(String str) 
   { 
     this.treeName = str; 
   }
 	
   /**
    * Uses dtsVocab to retrieve the root nodes of that vocab, checks whether each
    * is a node or a leaf (no children) then displays the Tree, with appropriate gif
    * for node or leaf.
    * @param dtsVocab    The Vocabulary name.
    * @return rendHTML   The string of html which displays the Tree.
    */
 	public String populateTreeRoots(String dtsVocab) 
   {	
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
      || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = m_servlet.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = m_servlet.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = m_servlet.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = m_servlet.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = m_servlet.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = m_servlet.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = m_servlet.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = m_servlet.m_VOCAB_HL7;   
 		Tree dtsTree = new Tree("dtsTree");
     dtsVocab = filterName(dtsVocab, "js");
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
     Vector vRoot = new Vector();
     Vector vSubConceptNames = new Vector();
     Vector vSubConcepts2 = new Vector();
     boolean moreChildren = true;
     String rendHTML = "";
     if(dtsVocab == null || dtsVocab.equals(""))
       dtsVocab = m_dtsVocab;
 		try 
     {    
       // See if this Vocab's tree already has been built and stored in ServletContext
       Tree vocabTree = (Tree)m_treesHash.get(dtsVocab);
       if(vocabTree != null)
       {
         rendHTML = renderHTML(vocabTree);
       }
       else
       {  
         // Get the Root concepts, names and codes.
         m_vRootNames = serAC.getRootConcepts(dtsVocab, true);
         m_vRootCodes = serAC.getRootConcepts(dtsVocab, false);
         // For each Root, get the Subconcepts. If subconcepts exist, build a Node
         // object, else build a Leaf object
         for(int j=0; j < m_vRootNames.size(); j++) 
         { 
           vSubConceptNames = serAC.getSubConceptNames(dtsVocab, (String)m_vRootNames.elementAt(j), "", (String)m_vRootCodes.elementAt(j), "");          
           if(vSubConceptNames.size()>0)
           {
             TreeNode tn = new TreeNode(nodeID++, (String)m_vRootNames.elementAt(j), (String)m_vRootCodes.elementAt(j), level);
             tn.setExpanded(false);
             tn.setVisible(true);
             tn.getChildren().setLevel(level);
             //Each node is added to the vocab tree
             dtsTree.addChild(tn);
             //Every node created is stored in m_treeNodesHash, for quick retrieval
             m_treeNodesHash.put((String)m_vRootNames.elementAt(j), tn);
             //Every node has a tree of Children, which is stored in m_treesHash
             m_treesHash.put((String)m_vRootNames.elementAt(j), tn.getChildren()); //add the node's children tree to hash table
           }
           else
           { 
             TreeLeaf tl = new TreeLeaf((String)m_vRootNames.elementAt(j), (String)m_vRootCodes.elementAt(j), level);
             //Each leaf is added to the vocab tree
             dtsTree.addChild(tl);
             tl.setVisible(true);
           }
         } 
         // Store the tree then render the html
         this.setTreeName(dtsVocab);   
         dtsTree.setName(dtsVocab); 
         //Put the vocab tree in m_treesHash 
         m_treesHash.put(treeName, dtsTree);	// Store the tree in the static Hashtable 
         //Keep track of number of nodes in m_treesHash (nodeID is incremented above, each time new node is created
         Integer nodeid = new Integer(nodeID);    
         m_treesHash.put("nodeID", nodeid);
         if(dtsTree != null)
         {  
           rendHTML = renderHTML(dtsTree);  
         } 
         //Store the hash tables in servler context, so they are available to all clients using the servlet
         if(m_ServletContext != null)
         {
           m_ServletContext.setAttribute("treesHash", m_treesHash);
           m_ServletContext.setAttribute("treeNodesHash", m_treeNodesHash);
         }
       }
 		} 
     catch(Exception e) 
     {
 			System.out.println("Error in populateTreeRoots: " + e.toString());
 		}
   return rendHTML;	
 	}
 
 	/**
 	 * Renders the specified tree as HTML by iterating over all it's children. Will call itself
 	 * recursively for any visible node children.
    * @param tree    The Tree object to render.
    * @return buf.toString()   The string buffer of html
 	 */
 	private String renderHTML(Tree tree) 
   {
     if(tree == null)
       return "";
     String sSearchAC = "";
     String displayName1 = "";
     String displayName2 = "";
     try
     {
       if(m_classReq != null)
       {
         HttpSession session2 = m_classReq.getSession();
         sSearchAC = (String)session2.getAttribute("creSearchAC");
         if(sSearchAC == null) sSearchAC = "";
       }
     }
     catch(Exception e) 
     {
 			//System.out.println("Error in renderHTML: " + e.toString());
 		}
 		StringBuffer buf = new StringBuffer();
     boolean moreChildren = true;
     int numChildren = 0;
     
     Tree nodeTree = new Tree("nodeTree");
     if(m_dtsVocab != null)
       displayName1 = filterName(m_dtsVocab, "display");
     if(displayName1 != null)
     {
       if(displayName1.equals("Thesaurus/Metathesaurus"))
         displayName1 = "Thesaurus";
     }
     m_dtsVocab = filterName(m_dtsVocab, "js");
     //Build the html in a string buffer
     displayName2 = " Root Concepts";
     if(sSearchAC.equals("ParentConceptVM"))
       displayName2 =  " Parent Concept";  
 		buf.append("\n<TABLE width=\"100%\" border=\"0\"");
 		buf.append(">\n");
     buf.append("<tr></tr>");
     String jsEnd = "');";
     String js = "javascript:refreshTree('";
     String sRefresh = js + m_dtsVocab + jsEnd;
     if(!sSearchAC.equals("ParentConceptVM"))
     {
       buf.append( "<a ");
       buf.append("href=").append(sRefresh).append(">");
       buf.append(displayName1).append("</a>").append(displayName2);
     }
     else
        buf.append(displayName1).append(displayName2);
 		for(int i=0; i < tree.size(); i++) 
     {
 			TreeObject treeObject = tree.getChild(i);
 			if(treeObject.getType() == tree.NODE ) 
       {
 				TreeNode node = (TreeNode)treeObject;
         if(node.isVisible())
         {     
           // render root level 1xxxxxxxxxxxxxxxxxxxxxxxxxxxx
           buf.append(renderNodeHTML(node));
           nodeTree = node.getChildren();
           numChildren = nodeTree.size();
           if(numChildren > 0)
           {
             for(int j=0;j<numChildren;j++)
             {
               if(nodeTree.getChild(j).getType() == tree.NODE)
               {
                TreeNode tn = (TreeNode)nodeTree.getChild(j);
                if(tn.isVisible())
                 buf.append(renderNodeHTML(tn));   
                // render root level 2 and beyond yyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
                Tree nodeTree2 = tn.getChildren();
                int numChildren2 = nodeTree2.size();
                if(numChildren2 > 0) //
                   renderSubNodes(tn, buf);
                // end level 2 yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
               }
               else if(nodeTree.getChild(j).getType() == tree.LEAF)
               {
                 TreeLeaf tl = (TreeLeaf)nodeTree.getChild(j);
                 if(tl.isVisible())
                   buf.append(renderLeafHTML(tl));
               }
               // end level 1 xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
             }
           }
         }
 			}
 			else 
       {
         TreeLeaf leaf = (TreeLeaf)treeObject;
         if(leaf.isVisible())
           buf.append(renderLeafHTML(leaf));
 			}
 		}
 		buf.append("\n</TABLE>\n");
 		return buf.toString();
 	}
   
 /**
 	* Renders the subNodes, given a Node. Attaches the html to the StringBuffer passed in.
   * @param tn    TreeNode object.
   * @param buf   StringBuffer
 */
 private void renderSubNodes(TreeNode tn, StringBuffer buf) 
 {
     Tree nodeTree2 = tn.getChildren();
     int numChildren2 = nodeTree2.size();
     if(numChildren2 > 0) //
     {
       for(int k=0;k<numChildren2;k++)
       {
         if(nodeTree2.getChild(k).getType() == 0)  // node
         {
           TreeNode tn2 = (TreeNode)nodeTree2.getChild(k);
           if(tn2.isVisible())
             buf.append(renderNodeHTML(tn2));
           Tree nodeTree3 = tn2.getChildren(); //next level of recursion
           if(nodeTree3.size()>0)
             renderSubNodes(tn2, buf);
         }
         else if(nodeTree2.getChild(k).getType() == 1) // leaf
         {
           TreeLeaf tl2 = (TreeLeaf)nodeTree2.getChild(k);
           if(tl2.isVisible())
             buf.append(renderLeafHTML(tl2));
         }
       }
     }  
 }
   
   
 	/**
 	 * Renders the specified tree node as HTML.
    * @param node    TreeNode object.
    * @return buf.toString()   The stringbuffer
 	 */
 	private String renderNodeHTML(TreeNode node)
   {
 		StringBuffer buf = new StringBuffer();
     String sSearch = "";
     String sExpand = "";
     String sCollapse = "";
     String nodeCCode = node.getCode();
     int nodeID = node.getId();
     String sNodeName = node.getName();
     String sJSName = filterName(sNodeName, "js");
     UtilService util = new UtilService();
     sJSName = util.parsedStringSingleQuote(sJSName);
     m_dtsVocab = filterName(m_dtsVocab, "js");
     String displayName = filterName(sNodeName, "display");
     // Handle the 'GO' exception, which actually displays "_" of Root concepts
     if(displayName.equals("Gene Ontology")) displayName = "Gene_Ontology";
 		buf.append( "<tr><td>");
     // This gives the correct indentation between levels of the tree
     if(node.getLevel() ==2) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==3) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==4) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==5) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==6) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==7) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==8) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==9) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==10) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==11) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(node.getLevel() ==12) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     String js = "javascript:doTreeAction('search','";
     String jsEnd = "');";
     sSearch = js + nodeCCode + "','" + m_dtsVocab + "','" + sJSName + jsEnd;
     buf.append( "<a ");
 		if(node.isExpanded())
     {
       js = "javascript:doTreeAction('collapse','";
       sCollapse = js + nodeCCode + "','" + m_dtsVocab + "','" + sJSName + jsEnd;
       buf.append("href=").append(sCollapse).append(" >");
       buf.append( "<img src='../../cdecurate/Assets/").append(openedImage).append("' border='0'>" );
     }
     else
     {
       js = "javascript:doTreeAction('expand','";
       sExpand = js + nodeCCode + "','" + m_dtsVocab + "','" + sJSName + jsEnd;
       buf.append("href=").append(sExpand).append(" >");
       buf.append( "<img src='../../cdecurate/Assets/").append(collapsedImage).append("' border='0'>" );
     }
 		buf.append( "</a>" ); 
     buf.append( "&nbsp;");
     buf.append( "<a ");
     buf.append("href=").append(sSearch).append(">");
     if(node.isBold())
       buf.append( " " ).append("<b>").append(displayName).append("</b>");
     else
       buf.append( " " ).append(displayName);
 		buf.append( "</a>" );
 		buf.append( "</td></tr>" );
 		return buf.toString();
 	}
   
   /**
 	 * Puts in and takes out "_"
    *  @param nodeName   The name of node.
    *  @param type       'display' with no underscores between words, or 'js' with underscores.
    *  @return nodeName  The new name of the node
 	 */
 	private String filterName(String nodeName, String type)
   {
     if(type.equals("display"))
       nodeName = nodeName.replaceAll("_"," ");
     else if(type.equals("js"))
       nodeName = nodeName.replaceAll(" ","_");
       return nodeName;
   }
 
 	/**
 	 * Renders the specified tree leaf as HTML.
    *  @param TreeLeaf leaf
    *  @return buf.toString()   The stringbuffer
    */
 	private final String renderLeafHTML(TreeLeaf leaf) 
   {
     StringBuffer buf = new StringBuffer();
     String nodeCCode = leaf.getCode();
     String sLeafName = leaf.getName();
     m_dtsVocab = filterName(m_dtsVocab, "js");
     String sJSName = filterName(sLeafName, "js");
     UtilService util = new UtilService();
     sJSName = util.parsedStringSingleQuote(sJSName);
     String displayName = filterName(sLeafName, "display");
      // Handle the 'GO' exception, which actually displays "_" of Root concepts on DTS Browser
     if(displayName.equals("is a")) displayName = "is_a";
     else if(displayName.equals("part of")) displayName = "part_of";
 		buf.append( "<tr><td>");
     // This gives the correct indentation between levels of the tree
     if(leaf.getLevel() ==2) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==3) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==4) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==5) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==6) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==7) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==8) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==9) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==10) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==11) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     else if(leaf.getLevel() ==12) buf.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
     String js = "javascript:doTreeAction('search','";
     String jsEnd = "');";
     String sSearch = js + nodeCCode + "','" + m_dtsVocab + "','" + sJSName + jsEnd;
 		buf.append( "<img src='../../cdecurate/Assets/").append(leafImage).append("' border='0'>" );
     buf.append( "&nbsp;");
     buf.append( "<a ");
     buf.append("href=").append(sSearch).append(">");
     if(leaf.isBold())
       buf.append( " " ).append("<b>").append(displayName).append("</b>");
     else
       buf.append( " " ).append(displayName);
 		buf.append( "</a>" );
 		buf.append( "</td></tr>" );
 		return buf.toString();
 	}
   
   
 /**
 	 * When user clicks on the node's + sign, this method checks if the node's children
    * tree already exists, if so, it sets them visible. Otherwise it retrieves them,
    * then renders html.
    *  @param nodeName            The name of node.
    *  @param dtsVocab            The Vocabulary of the node.
    *  @param strRenderHTML       'Yes' to render as html, 'No' to not.
    *  @param nodeCode            The concept code of the node
    *  @param sCodeToFindInTree   Used to expand the node to a certain subconcept, then stop
    *  @return rendHTML           The rendered html.
 */
   public String expandNode(String nodeName, String dtsVocab, String strRenderHTML, String nodeCode, String sCodeToFindInTree) 
   {
     HttpSession session = m_classReq.getSession();
     if(dtsVocab.equals(""))
       dtsVocab = m_dtsVocab;
     if(!dtsVocab.equals("Thesaurus/Metathesaurus") && !dtsVocab.equals("NCI_Thesaurus")
      && !dtsVocab.equals("NCI Thesaurus"))
       nodeName = filterName(nodeName, "display");
     else if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("") 
     || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       nodeName = filterName(nodeName, "js");
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("") 
     || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = m_servlet.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = m_servlet.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = m_servlet.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = m_servlet.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = m_servlet.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = m_servlet.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = m_servlet.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = m_servlet.m_VOCAB_HL7;
       
     // Handle the 'GO' exception, which actually displays "_" of Root concepts
     if(nodeName.equals("Gene Ontology")) nodeName = "Gene_Ontology";
     
     String sSearchAC = (String)session.getAttribute("creSearchAC");
     if(sSearchAC == null) sSearchAC = "";
     Integer id = new Integer(0);
// System.out.println("expandNode dtsVocab: " + dtsVocab + " nodeName: " + nodeName + "nodeCode: " + nodeCode + " m_dtsVocab: " + m_dtsVocab + " sSearchAC: " + sSearchAC);
     if(sSearchAC.equals("ParentConceptVM"))
       id = (Integer)m_treesHashParent.get("nodeID");
     else
       id = (Integer)m_treesHash.get("nodeID"); // for new nodes will need the last used nodeID
     if(id == null) id = new Integer(0);
     int newNodeID = id.intValue();
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
     if(nodeCode.equals("") && !nodeName.equals(""))
      nodeCode = serAC.do_getEVSCode(nodeName, dtsVocab);   
   
 	  String rendHTML = "";
     Vector vRoot = new Vector();
     Vector vSubNames = new Vector();
     Vector vSubCodes = new Vector();
     Vector vSubConcepts = new Vector();
     Vector vSubConcepts2 = new Vector();
     Tree nodeChildrenTree = new Tree(1);
   try
   {
     if(sSearchAC.equals("ParentConceptVM"))
       nodeChildrenTree = (Tree)m_treesHashParent.get(nodeName);
     else
       nodeChildrenTree = (Tree)m_treesHash.get(nodeName);
     // Root level of Thesaurus has "_" in names
     if(nodeChildrenTree == null)
     {
       nodeName = filterName(nodeName, "js");
       if(sSearchAC.equals("ParentConceptVM"))
         nodeChildrenTree = (Tree)m_treesHashParent.get(nodeName);
       else
         nodeChildrenTree = (Tree)m_treesHash.get(nodeName);
     }
     TreeNode expandedNode = new TreeNode(1, "", "",1);
     if(m_treeNodesHash != null)
     {
       if(sSearchAC.equals("ParentConceptVM"))
         expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
       else
         expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
     }
     // if null, look for the nodeName with "_"
     if(expandedNode == null)
     {
       nodeName = filterName(nodeName, "js");
       if(sSearchAC.equals("ParentConceptVM"))
         expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
       else
         expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
     }  
     int nodeLevel = 0;
     int numChildren = 0;
     String subNodeCode = ""; 
     String subNodeName = ""; 
     if(expandedNode != null)
     {
         nodeLevel = expandedNode.getLevel();
         expandedNode.setExpanded(true);
         if(sSearchAC.equals("ParentConceptVM"))
            m_expandedTreeNodesParent.push(nodeName);
         else
           m_expandedTreeNodes.push(nodeName);
     }
     if(nodeChildrenTree != null)
         numChildren = nodeChildrenTree.size();   
     if(numChildren > 0 && nodeChildrenTree != null) //children already exist, set them visible
     {
         for(int i=0;i<numChildren;i++)
         {
           if(nodeChildrenTree.getChild(i).getType() == 0) //node
           {
             TreeNode tn = (TreeNode)nodeChildrenTree.getChild(i);
             tn.setExpanded(false);
             tn.setVisible(true);
             if(!sCodeToFindInTree.equals(""))
               if(tn.getCode().equals(sCodeToFindInTree) || tn.getName().equals(nodeName))
                 tn.setBold(true);
           }
           else if(nodeChildrenTree.getChild(i).getType() == 1) // leaf
           {
             TreeLeaf tl = (TreeLeaf)nodeChildrenTree.getChild(i);
             tl.setVisible(true);
              if(!sCodeToFindInTree.equals(""))
               if(tl.getCode().equals(sCodeToFindInTree) || tl.getName().equals(nodeName))
                 tl.setBold(true);
           }
         }
       }
       else if(nodeChildrenTree != null)
       {
         vSubNames = serAC.getSubConceptNames(dtsVocab, nodeName, "", nodeCode, "");
         if(vSubNames != null && vSubNames.size()>0 && vSubNames.size()<10)
         {
           for(int j=0; j < vSubNames.size(); j++) 
           { 
             subNodeName = (String)vSubNames.elementAt(j);
             subNodeCode = serAC.do_getEVSCode(subNodeName, dtsVocab);   
             vSubConcepts2 = serAC.getSubConceptNames(dtsVocab, subNodeName, "", subNodeCode, "");
             if(vSubConcepts2.size()>0)
             {
               TreeNode tn = new TreeNode(newNodeID++, subNodeName, subNodeCode, nodeLevel+1);
               tn.setExpanded(false);
               tn.setVisible(true);
               if(!sCodeToFindInTree.equals(""))
                 if(tn.getCode().equals(sCodeToFindInTree) || tn.getName().equals(nodeName))
                   tn.setBold(true);
               tn.getChildren().setLevel(nodeLevel+2);
               nodeChildrenTree.addChild(tn);
               if(sSearchAC.equals("ParentConceptVM"))
               {
                 m_treeNodesHashParent.put(subNodeName, tn);
                 m_treesHashParent.put(subNodeName, tn.getChildren()); 
               }
               else
               {
                 m_treeNodesHash.put(subNodeName, tn);
                 m_treesHash.put(subNodeName, tn.getChildren()); //add the node's children tree to hash table      
               }
             }
             else
             {
               TreeLeaf tl = new TreeLeaf(subNodeName, subNodeCode, nodeLevel+1);
               nodeChildrenTree.addChild(tl);
               tl.setVisible(true);
                if(!sCodeToFindInTree.equals(""))
               if(tl.getCode().equals(sCodeToFindInTree) || tl.getName().equals(nodeName))
                   tl.setBold(true);
               tl.setLevel(nodeLevel+1);
             }
           }
         }
         else if(vSubNames != null && vSubNames.size()>9)
         {
           for(int j=0; j < vSubNames.size(); j++) 
           { 
             subNodeName = (String)vSubNames.elementAt(j);
             subNodeCode = "";  //serAC.do_getEVSCode(subNodeName, dtsVocab);   
             TreeNode tn = new TreeNode(newNodeID++, subNodeName, subNodeCode, nodeLevel+1);
             tn.setExpanded(false);
             tn.setVisible(true);
             if(!sCodeToFindInTree.equals(""))
               if(tn.getCode().equals(sCodeToFindInTree) || tn.getName().equals(nodeName))
                 tn.setBold(true);
             tn.getChildren().setLevel(nodeLevel+2);
             nodeChildrenTree.addChild(tn);
             if(sSearchAC.equals("ParentConceptVM"))
             {
               m_treeNodesHashParent.put(subNodeName, tn);
               m_treesHashParent.put(subNodeName, tn.getChildren()); 
             }
             else
             {
               m_treeNodesHash.put(subNodeName, tn);
               m_treesHash.put(subNodeName, tn.getChildren()); //add the node's children tree to hash table      
             }
           }
         }
         else if(nodeChildrenTree != null)
         {
           TreeLeaf tl = new TreeLeaf(nodeName, nodeCode, nodeLevel+1);
           nodeChildrenTree.addChild(tl);
           tl.setVisible(true);
            if(!sCodeToFindInTree.equals(""))
               if(tl.getCode().equals(sCodeToFindInTree) || tl.getName().equals(nodeName))
                 tl.setBold(true);
           tl.setLevel(nodeLevel+1);
         }
     }
     if(nodeChildrenTree != null)
     {
       Tree baseTree = (Tree)m_treesHash.get(dtsVocab);    
       if(sSearchAC.equals("ParentConceptVM"))
       {
         m_treesHashParent.put(nodeName, nodeChildrenTree);	// Store the tree in the static Hashtable
         Integer nodeid = new Integer(newNodeID);
         m_treesHashParent.put("nodeID", nodeid);   
       }
       else
       {
         m_treesHash.put(nodeName, nodeChildrenTree);	// Store the tree in the static Hashtable
         Integer nodeid = new Integer(newNodeID);
         m_treesHash.put("nodeID", nodeid);        
         if(baseTree == null)
         {
           m_dtsVocab = filterName(m_dtsVocab, "display");   
           baseTree = new Tree(m_dtsVocab);  
         }
       }
       // Expand a node in Parent Concept Tree 
       if(sSearchAC.equals("ParentConceptVM"))
       {
         nodeName= (String)session.getAttribute("ParentConcept");
         if(nodeName != null && !nodeName.equals(""))
         {
 
           baseTree = (Tree)m_treesHashParent.get("parentTree" + nodeName);
           if(baseTree == null)
           {
             baseTree = new Tree("parentTree" + nodeName);
           }
           m_treesHashParent.put("parentTree" + nodeName, baseTree);
         }
     }
     if(!strRenderHTML.equals("No"))
         rendHTML = renderHTML(baseTree); 
     }
   } 
   catch(Exception e) 
   {
     System.out.println("ERROR expandNode: " + e.toString());
   }
   return rendHTML;	
 }
 
 /**
 	 * When user clicks on the node's - sign, this method iterates the node's children
    * tree, sets them not visible, then renders as html.
    *  @param nodeName       The name of node.
    *  @param dtsVocab       The name of vocabulary.
    *  @param shouldRender   'Yes' should, 'No' not.
    *  @return rendHTML      Html string
 */
 public String collapseNode(String nodeName, String dtsVocab, String shouldRender) 
 {
 	  HttpSession session = m_classReq.getSession();
     String rendHTML = "";
     String sSearchAC = (String)session.getAttribute("creSearchAC");
     if(sSearchAC == null) sSearchAC = "";
     if(nodeName.equals("Gene Ontology")) nodeName = "Gene_Ontology";
     boolean isExpanded;
      if(!dtsVocab.equals("Thesaurus/Metathesaurus") && !dtsVocab.equals("NCI_Thesaurus")
       && !dtsVocab.equals("NCI Thesaurus"))
       nodeName = filterName(nodeName, "display");
      else if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("") 
     || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       nodeName = filterName(nodeName, "js");     
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
      || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = m_servlet.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = m_servlet.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = m_servlet.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = m_servlet.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = m_servlet.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = m_servlet.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = m_servlet.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = m_servlet.m_VOCAB_HL7;   
 
     session = m_classReq.getSession();
     TreeNode expandedNode = new TreeNode(1, "", "",1);
     if(sSearchAC.equals("ParentConceptVM"))
       expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
     else
       expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
     // if null, look for the nodeName with "_"
     if(expandedNode == null)
     {
       nodeName = filterName(nodeName, "js");
       if(sSearchAC.equals("ParentConceptVM"))
         expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
       else
         expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
     }
     if(expandedNode != null)
       expandedNode.setExpanded(false);
     Tree nodeChildrenTree = new Tree(1);
     try 
     { 
       if(sSearchAC.equals("ParentConceptVM"))
         nodeChildrenTree = (Tree)m_treesHashParent.get(nodeName);
       else
         nodeChildrenTree = (Tree)m_treesHash.get(nodeName);
       if(nodeChildrenTree == null)
       {
         nodeName = filterName(nodeName, "display");
         if(sSearchAC.equals("ParentConceptVM"))
           nodeChildrenTree = (Tree)m_treesHashParent.get(nodeName);
         else
           nodeChildrenTree = (Tree)m_treesHash.get(nodeName);
       }
       int numChildren = 0;
       if(nodeChildrenTree != null)
         numChildren = nodeChildrenTree.size();
       if(numChildren > 0) //children already exist, set them visible
       {
         for(int i=0;i<numChildren;i++)
         {
           if(nodeChildrenTree.getChild(i).getType() == 0) //node
           {
             TreeNode tn = (TreeNode)nodeChildrenTree.getChild(i);
             tn.setExpanded(false);
             tn.setVisible(false);
             collapseNode(tn.getName(), dtsVocab, "false");
           }
           else
           {
             TreeLeaf tl = (TreeLeaf)nodeChildrenTree.getChild(i);
             tl.setVisible(false);
           }
         }
       }
       if(sSearchAC.equals("ParentConceptVM"))
          m_treesHashParent.put(nodeName, nodeChildrenTree);
       else
         m_treesHash.put(nodeName, nodeChildrenTree);	// Store the tree in the static Hashtable
       Tree baseTree = (Tree)m_treesHash.get(dtsVocab);
       if(sSearchAC.equals("ParentConceptVM"))
       {
         nodeName= (String)session.getAttribute("ParentConcept");
         if(nodeName != null && !nodeName.equals(""))
         {
           baseTree = (Tree)m_treesHashParent.get("parentTree" + nodeName);
           if(baseTree == null)
           {
             nodeName = filterName(nodeName, "js");
             baseTree = (Tree)m_treesHashParent.get("parentTree" + nodeName);
             if(baseTree == null)
             {
               baseTree = new Tree("parentTree" + nodeName);
             }
           }   
         }
       }
       if(!shouldRender.equals("false") && baseTree != null)
       {
         rendHTML = renderHTML(baseTree);          
       }
 		} 
     catch(Exception e) 
     {
 			System.out.println(e.toString());
 		}
   return rendHTML;	
 }
 
 /**
 	 * This method retrieves all open nodes from a stack, then sets them not visible
 */
 public void collapseAllNodes() 
 {
    HttpSession session = m_classReq.getSession();
   String sSearchAC = (String)session.getAttribute("creSearchAC");
   if(sSearchAC == null) sSearchAC = "";
   String nodeName = "";
   TreeNode expandedNode = new TreeNode(1, "", "",1);
   try 
   { 
     if(!sSearchAC.equals("ParentConceptVM"))
     {
       while(m_expandedTreeNodes.size()>0)
       {    
         nodeName = (String)m_expandedTreeNodes.pop();
         if(m_treeNodesHash != null)
             expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
         // if null, look for the nodeName with "_"
         if(expandedNode == null)
         {
           nodeName = filterName(nodeName, "js");
           expandedNode = (TreeNode)m_treeNodesHash.get(nodeName);
         }
         if(expandedNode != null)
           expandedNode.setExpanded(false);       
         Tree nodeChildrenTree = new Tree(1);
         nodeChildrenTree = (Tree)m_treesHash.get(nodeName);
         int numChildren = nodeChildrenTree.size();
         if(numChildren > 0) //children already exist, set them visible
         {
           for(int i=0;i<numChildren;i++)
           {
             if(nodeChildrenTree.getChild(i).getType() == 0) //node
             {
               TreeNode tn = (TreeNode)nodeChildrenTree.getChild(i);
               tn.setExpanded(false);
               tn.setVisible(false);
               tn.setBold(false);
             }
             else
             {
               TreeLeaf tl = (TreeLeaf)nodeChildrenTree.getChild(i);
               tl.setVisible(false);
               tl.setBold(false);
             }
           }
         }
         m_treesHash.put(nodeName, nodeChildrenTree);	// Store the tree in the static Hashtable
         if(m_ServletContext != null)
         {
             m_ServletContext.setAttribute("expandedTreeNodes", m_expandedTreeNodes);
         }
       }
     }
     else
     {
       while(m_expandedTreeNodesParent.size()>0)
       {
          if(sSearchAC.equals("ParentConceptVM"))
          {
           nodeName = (String)m_expandedTreeNodesParent.pop();
           if(m_treeNodesHashParent != null)
             expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
          }
         // if null, look for the nodeName with "_"
         if(expandedNode == null)
         {
           nodeName = filterName(nodeName, "js");
           if(sSearchAC.equals("ParentConceptVM"))
             expandedNode = (TreeNode)m_treeNodesHashParent.get(nodeName);
         }
         if(expandedNode != null)
           expandedNode.setExpanded(false);       
         Tree nodeChildrenTree = new Tree(1);
         if(sSearchAC.equals("ParentConceptVM"))
           nodeChildrenTree = (Tree)m_treesHashParent.get(nodeName);
       
         int numChildren = nodeChildrenTree.size();
         if(numChildren > 0) //children already exist, set them visible
         {
           for(int i=0;i<numChildren;i++)
           {
             if(nodeChildrenTree.getChild(i).getType() == 0) //node
             {
               TreeNode tn = (TreeNode)nodeChildrenTree.getChild(i);
               tn.setExpanded(false);
               tn.setVisible(false);
               tn.setBold(false);
             }
             else
             {
               TreeLeaf tl = (TreeLeaf)nodeChildrenTree.getChild(i);
               tl.setVisible(false);
               tl.setBold(false);
             }
           }
         }
         if(sSearchAC.equals("ParentConceptVM"))
            m_treesHashParent.put(nodeName, nodeChildrenTree);
       
         if(m_ServletContext != null)
         {
           if(sSearchAC.equals("ParentConceptVM"))
             m_ServletContext.setAttribute("expandedTreeNodesParent", m_expandedTreeNodesParent);
         }
       }
     }
   } 
   catch(Exception e) 
   {
     System.out.println(e.toString());
   } 
  }
   
 /**
 	 * This method takes the CCode and vocab, and calls method getSuperConcepts until
    * there are no more superconcepts(i.e. is a Root). Each concept returned is put on a stack.
    * When a root is reached, concepts are popped off the stack one by one, and expandNode is 
    * called until the beginning concept is visible.
    *  @param sCCode       The concept code.
    *  @param sCCodeDB     The vocab.
    *  @param sCCodeName   The name of code.
    *  @return rendHTML    The rendered html string.
 */
   public String openTreeToConcept(String sCCode, String sCCodeDB, String sCCodeName) 
   {
     String rendHTML = "";
     String sMatch = "false";
     String sRoot = "false";
     String sSuperConceptName = "";
     String sNameToFindInTree = "";
     String sCodeToFindInTree = "";
     String dtsVocab = "";
     sCodeToFindInTree = sCCode;
     Stack stackSuperConcepts = new Stack();
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
     Vector vSuperConceptNames = new Vector();
     if(sCCodeDB.equals("Thesaurus/Metathesaurus") || sCCodeDB.equals("")
      || sCCodeDB.equals("NCI Thesaurus") || sCCodeDB.equals("NCI_Thesaurus"))
       sCCodeDB = m_servlet.m_VOCAB_NCI;   
     // Get the superConcepts of the concept. Occasionally more than one superConcepts, so returns a Vector
     while(sRoot.equals("false"))
     {
       try
       {
         vSuperConceptNames = serAC.getSuperConceptNames(sCCodeDB, sCCodeName, sCCode, "");
       }
       catch(Exception e) 
       {
         System.out.println("error in openTreeToConcept getSuperConceptNames: " + e.toString());
       } 
       if(vSuperConceptNames != null && vSuperConceptNames.size() > 0)
       {
         sSuperConceptName = (String)vSuperConceptNames.elementAt(0);
         stackSuperConcepts.push(sSuperConceptName);
         sCCodeName = sSuperConceptName;
       }
       else //case of no superConcept, so concept is a Root
       {
         sRoot = "true";
         sCCodeName = filterName(sCCodeName, "display");
         stackSuperConcepts.push(sCCodeName);
       }
     }
     if(sRoot.equals("true"))
     {
       rendHTML = expandTreeToConcept(stackSuperConcepts, sCCodeDB, sCCode, sCodeToFindInTree);
     }
     return rendHTML;
   }
    
 /**
 	 * This method takes the CCode and vocab, and calls method getSuperConcepts until
    * there are no more superconcepts(i.e. is a Root). Each concept returned is put on a stack.
    * When a root is reached, concepts are popped off the stack one by one, and expandNode is 
    * called until the beginning concept is visible.
    *  @param sCCode       The concept code.
    *  @param sCCodeDB     The vocab.
    *  @param sCCodeName   The name of code.
    *  @return rendHTML    The rendered html string.
 */
   public String openParentTreeToConcept(String sCCode, String sCCodeDB, String sCCodeName) 
   {
     // Add "_" back in to Thesaurus concepts
     if(sCCodeDB.equals("NCI Thesaurus") || sCCodeDB.equals("Thesaurus/Metathesaurus") ||
     sCCodeDB.equals("NCI_Thesaurus")) 
       sCCodeName = filterName(sCCodeName, "js");
     String rendHTML = "";
     String sMatch = "false";
     String sRoot = "false";
     String parentName= "";
     String sSuperConceptName = "";
     Stack stackSuperConcepts = new Stack();
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
     Vector vSuperConceptNames = new Vector();
     String sNameToFindInTree = "";
     String dtsVocab = "";
     dtsVocab = sCCodeDB;
     
     if(dtsVocab.equals("Thesaurus/Metathesaurus") || dtsVocab.equals("")
     || dtsVocab.equals("NCI Thesaurus") || dtsVocab.equals("NCI_Thesaurus"))
       dtsVocab = m_servlet.m_VOCAB_NCI; //"NCI_Thesaurus";
     else if(dtsVocab.equals("VA NDFRT"))
       dtsVocab = m_servlet.m_VOCAB_VA;  //"VA_NDFRT";
     else if(dtsVocab.equals("UWD VISUAL ANATOMIST") || dtsVocab.equals("UWD_VISUAL_ANATOMIST"))
       dtsVocab = m_servlet.m_VOCAB_UWD;  //"UWD_Visual_Anatomist";
     else if(dtsVocab.equals("MGED")) 
       dtsVocab = m_servlet.m_VOCAB_MGE;  //"MGED_Ontology";
     else if(dtsVocab.equals("GO"))
       dtsVocab = m_servlet.m_VOCAB_GO;
     else if(dtsVocab.equals("LOINC"))
       dtsVocab = m_servlet.m_VOCAB_LOI;
     else if(dtsVocab.equals("MedDRA"))
       dtsVocab = m_servlet.m_VOCAB_MED;
     else if(dtsVocab.equals("HL7_V3")) 
       dtsVocab = m_servlet.m_VOCAB_HL7;
       
       DescLogicConcept dlc = null;
       dlc = new DescLogicConcept();
       try
       {
       if(sCCodeName == null || sCCodeName.equals(""))
         sCCodeName = dlc.getConceptNameByCode(dtsVocab, sCCode);
       }
       catch(Exception ea) 
       {
         System.out.println("error in openTreeToConcept getConceptNames: " + ea.toString());
       } 
       if(sCCodeName == null) sCCodeName = "";
       sNameToFindInTree = sCCodeName;    
     HttpSession session = m_classReq.getSession();
     sSearchAC = (String)session.getAttribute("creSearchAC");
     if(sSearchAC == null) sSearchAC = "";
     if(sSearchAC.equals("ParentConceptVM"))
       parentName= (String)session.getAttribute("ParentConcept");     
     // Get the superConcepts of the concept. Occasionally more than one superConcepts, so returns a Vector
     while(sRoot.equals("false"))
     {
       try
       {
         vSuperConceptNames = serAC.getSuperConceptNames(sCCodeDB, sCCodeName, sCCode, "");
       }
       catch(Exception e) 
       {
         System.out.println("error in openTreeToConcept getSuperConceptNames: " + e.toString());
       } 
       if(vSuperConceptNames != null && vSuperConceptNames.size() > 0)
       {
         sSuperConceptName = (String)vSuperConceptNames.elementAt(0);
         if(sSuperConceptName.equals(parentName))
         {
           sRoot = "true";
           stackSuperConcepts.push(parentName);
         }
         else
         {
           stackSuperConcepts.push(sSuperConceptName);
           sCCodeName = sSuperConceptName;
         }
       }
     }
     if(sRoot.equals("true"))  
       rendHTML = expandParentTreeToConcept(stackSuperConcepts, parentName, sCCode, sNameToFindInTree);  
     return rendHTML;
   }
     
 /**
 	 * This method looks for a Parent tree (a tree where the Parent concept is the single
    * root node); if the parent tree has not been created, it creates one, then renders as html.
    *  @param sCCode       The concept code.
    *  @param sCCodeDB     The vocab.
    *  @param sCCodeName   The name of code.
    *  @return rendHTML    The rendered html string.
 */
   public String showParentConceptTree(String sCCode, String sCCodeDB, String sCCodeName) 
   {
     if(sCCodeDB.equals("NCI Thesaurus") || sCCodeDB.equals("Thesaurus/Metathesaurus") ||
     sCCodeDB.equals("NCI_Thesaurus") || sCCodeDB.equals(""))
     {
       sCCodeName = filterName(sCCodeName, "js");
       sCCodeDB = m_servlet.m_VOCAB_NCI; 
     }
 //System.err.println(" showParentConceptTree: sCCodeDB: " + sCCodeDB + " sCCodeName: " + sCCodeName + " sCCode: " + sCCode);
     Tree parentTree = new Tree("parentTree" + sCCodeName);
     parentTree.setName("parentTree" + sCCodeName);
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
     Vector vRoot = new Vector();
     Vector vSubConceptNames = new Vector();
     Vector vSubConcepts2 = new Vector();
     boolean moreChildren = true;
     String rendHTML = "";
 		try 
     {     
       parentTree = (Tree)m_treesHashParent.get("parentTree" + sCCodeName);
       if(parentTree != null)
       {  
         rendHTML = renderHTML(parentTree);        
       }
       else
       {  
           parentTree = new Tree("parentTree" + sCCodeName);
           vSubConceptNames = serAC.getSubConceptNames(sCCodeDB, sCCodeName, "", sCCode, ""); 
           if(vSubConceptNames.size()>0)
           {
             TreeNode tn = new TreeNode(nodeID++, sCCodeName, sCCode, level);
             tn.setExpanded(false);
             tn.setVisible(true);
             tn.getChildren().setLevel(level);
             parentTree.addChild(tn);
             m_treeNodesHashParent.put(sCCodeName, tn);
             m_treesHashParent.put(sCCodeName, tn.getChildren()); //add the node's children tree to hash table
           }
           else
           { 
             TreeLeaf tl = new TreeLeaf(sCCodeName, sCCode, level);
             parentTree.addChild(tl);
             tl.setVisible(true);
           }
         }
         m_treesHashParent.put("parentTree" + sCCodeName, parentTree);	// Store the tree in the static Hashtable
         Integer nodeid = new Integer(nodeID);
         m_treesHashParent.put("nodeID", nodeid);
         rendHTML = renderHTML(parentTree);
         HttpSession session = m_classReq.getSession();
         if(m_ServletContext != null)
         {
           m_ServletContext.setAttribute("treesHashParent", m_treesHashParent);
           m_ServletContext.setAttribute("treeNodesHashParent", m_treeNodesHashParent);
         }
 		} 
     catch(Exception e) 
     {
 			System.out.println(e.toString());
 		}
   return rendHTML;	
   }
    
   /**
 	 *  This method takes a stack of conceptNames, with Root concept on top, and one
    *  by one expands the tree, using each conceptName from stack as a node.
    *  @param stackSuperConcepts     A stack of superconcept names.
    *  @param sCCodeDB               The dtsVocab.
    *  @param sCCode                 The concept code
    *  @param sCodeToFindInTree      The code of the concept to find in tree.
    *  @return rendHTML              The string of html
 */
   public String expandTreeToConcept(Stack stackSuperConcepts, String sCCodeDB, String sCCode, String sCodeToFindInTree) 
   {
     String rendHTML = "";
     String sMatch = "false";
     String sSuperConceptName = "";
     HttpSession session = m_classReq.getSession();
     String sSearchAC = (String)session.getAttribute("creSearchAC");
     if(sSearchAC == null) sSearchAC = "";
  //System.out.println("expandTreeToConcept  sCCodeDB: " + sCCodeDB + " sSearchAC: " + sSearchAC + " sCodeToFindInTree: " + sCodeToFindInTree);  
 //System.out.println("expandTreeToConcept sCCode: " + sCCode);
     if(sCCodeDB.equals("NCI Thesaurus") || sCCodeDB.equals("NCI_Thesaurus")
      || sCCodeDB.equals("Thesaurus/Metathesaurus")) 
         sCCodeDB = "NCI_Thesaurus";
     Tree baseTree = new Tree(1);
     while(stackSuperConcepts.size()>0)
     {
       sSuperConceptName = (String)stackSuperConcepts.pop();
       if(stackSuperConcepts.size()>0)
       {
        rendHTML = this.expandNode(sSuperConceptName, sCCodeDB, "No", sCCode, sCodeToFindInTree);  
       }
       else
       {
         rendHTML = this.expandNode(sSuperConceptName, sCCodeDB, "No", sCCode, sCodeToFindInTree);
         if(sSearchAC.equals("ParentConceptVM"))
           baseTree = (Tree)m_treesHashParent.get(sCCodeDB);  
         else
           baseTree = (Tree)m_treesHash.get(sCCodeDB);   
         if(baseTree != null)
         {
           rendHTML = renderHTML(baseTree);
         }
         else
         {
           if(sCCodeDB.equals("Thesaurus/Metathesaurus")) 
             sCCodeDB = "NCI_Thesaurus";
           else if(sCCodeDB.equals("MGED")) 
             sCCodeDB = "MGED_Ontology";
           baseTree = (Tree)m_treesHash.get(sCCodeDB);
           if(baseTree != null)
             rendHTML = renderHTML(baseTree);
         }
       }
     }
     return rendHTML;
   }
   
   /**
 	 *  This method takes a stack of conceptNames, with Root concept on top, and one
    *  by one expands the tree, using each conceptName from stack as a node.
    *  @param stackSuperConcepts   The stack of concept names.
    *  @param parentName           The name of the parent
    *  @param sCCode.              The concept code.
    *  @param sNameToFindInTree    The name of the concept to find in tree, then stop expanding
    *  @return rendHTML            The string of html
 */
   public String expandParentTreeToConcept(Stack stackSuperConcepts, String parentName, String sCCode, String sNameToFindInTree) 
   {
     String rendHTML = "";
     String sMatch = "false";
     String sSuperConceptName = "";
    
     while(stackSuperConcepts.size()>0)
     {
       sSuperConceptName = (String)stackSuperConcepts.pop();
       rendHTML = this.expandNode(sSuperConceptName, m_dtsVocab, "No", sCCode, sNameToFindInTree);
     }
     Tree baseTree = (Tree)m_treesHashParent.get("parentTree" + parentName);
     if(baseTree != null)
       rendHTML = renderHTML(baseTree);
     return rendHTML;
   } 
   
 
 /**
    * Removes the Vocab's tree from Hashtable, then recreates tree and stores it
    * @param dtsVocab   The Vocabulary name
    * @return rendHTML  The string of html
    */
 	public String refreshTree(String dtsVocab, String sRender) 
   {		
 		Tree dtsTree = new Tree("dtsTree");
     GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);  
     String rendHTML = "";
 		try 
     {     
       Tree vocabTree = (Tree)m_treesHash.remove(dtsVocab);
       if(!sRender.equals("false"))
         rendHTML = this.populateTreeRoots(dtsVocab);
     }
     catch(Exception e) 
     {
 			System.out.println(e.toString());
 		}
     return rendHTML;
   } 
     
 }
 
