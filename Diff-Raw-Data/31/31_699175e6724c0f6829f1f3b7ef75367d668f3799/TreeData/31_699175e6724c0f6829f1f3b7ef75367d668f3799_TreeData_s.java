 /*
  * Created on Oct 4, 2006
  * @author
  *
  */
 
 package edu.common.dynamicextensions.ui.webui.util;
 
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author preeti_munot
  *
 
  */
 public class TreeData
 {
 	private final String EXPANDED_CLASS_NAME = "folderOpen";
 	private final String COLLAPSED_CLASS_NAME = "folder";
	
 	private String folder = "/images";
 	private String color = "navy";
 	private TreeNodesList nodes;
 	private String target = "";
 	private int length = 0;
 	private StringBuffer buf = null;
 	private String nodeClickedFunction = null;
 
 	public String getNodeClickedFunction()
 	{
 		return this.nodeClickedFunction;
 	}
 
 	public void setNodeClickedFunction(String nodeClickedFunction)
 	{
 		this.nodeClickedFunction = nodeClickedFunction;
 	}
 
 	/**
 	 * Create a new Tree Data object
 	 *
 	 */
 	public TreeData()
 	{
 		folder = "images/";
 		nodes = new TreeNodesList();
 	}
 
 	/**
 	 * 
 	 * @param url URL for the image files
 	 */
 	public void setImagesUrl(String url)
 	{
 		this.folder = url;
 	}
 	/**
 	 * 
 	 * @return URL of image files
 	 */
 	public String getImagesUrl()
 	{
 		return (this.folder);
 	}
 
 	/**
 	 * Add a new tree node
 	 * @param node : Tree Node Object
 	 */
 	public void add(TreeNode node)
 	{
 		nodes.add(node);
 		length++;
 	}
 
 	/**
 	 * Add a new tree node with given text and sequence number
 	 * @param text  : Text for the node
 	 * @param seqno : Node sequence number
 	 */
 	public void add(String text, int seqno)
 	{
 		add(new TreeNode(text, seqno));
 	}
 
 	/**
 	 * Create a new tree node with given text and sequence number
 	 * @param text  : Text for the node
 	 * @param seqno : Node sequence number
 	 * @return newly created Tree Node
 	 */
 	public TreeNode createNode(String text, int seqno)
 	{
 		return (new TreeNode(text, seqno));
 	}
 
 	/**
 	 * 
 	 * @param text  : Text for the node
 	 * @param href : refernce for the node link
 	 * @param toolTip : tooltip for tree node
 	 * @param seqno : Sequence number
 	 * @return : newly created tree node
 	 */
 	public TreeNode createNode(String text, String href, String toolTip, int seqno)
 	{
 		return (new TreeNode(text, href, toolTip, seqno));
 	}
 
 	/**
 	 * 
 	 * @param text text to be added to buffer
 	 */
 	private void print(String text)
 	{
 		buf.append(text);
 	}
 
 	/**
 	 * 
 	 * @return : String having the HTML code for the tree like representation of the data
 	 */
 	public String getTree(String treeName,String fieldForSelectedObject,String strIsTreeExpanded,String nodeClickedFunction)
 	{
 		buf = new StringBuffer();
		
 		print("<style>ul.collapsedTree{display:none;margin-left:17px;}ul.expandedTree{margin-left:17px;}li.folder{list-style-image: url("
 				+ folder
 				+ "/plus.gif);}li.folderOpen{list-style-image: url("
 				+ folder
 				+ "/minus.gif);}li.file{FONT-WEIGHT:normal;list-style-image: url("
 				+ folder
 				+ "/dot.gif);}a.treeview{color:"
 				+ color
 				+ ";font-family:verdana;font-size:9pt;}a.treeview:link {text-decoration:none;}a.treeview:visited{text-decoration:none;}a.treeview:hover {text-decoration:underline;}</style>");
		
 		this.setNodeClickedFunction(nodeClickedFunction);
 		boolean isTreeExpanded = false;
 		if((strIsTreeExpanded!=null)&&(strIsTreeExpanded.equals("true")))
 		{
 			isTreeExpanded = true;
 		}
 		if (nodes != null)
 		{
 			loopThru(nodes, "0",treeName,fieldForSelectedObject,isTreeExpanded);
 		}
 		else
 		{
 			Logger.out.error("Nodes List Is null(get Tree)");
 		}
 
 		return buf.toString();
 	}
 
 	/**
 	 * 
 	 * @param nodeList :  List of tree nodes
 	 * @param parent : Name of parent node
 	 */
 	private void loopThru(TreeNodesList nodeList, String parent,String treeName,String fieldForSelectedObject,boolean isTreeExpanded)
 	{
 		if (nodeList != null)
 		{
 			boolean hasChild;
 			boolean displayRadioButton = false;
 			String defaultClassName = COLLAPSED_CLASS_NAME;
 			if(isTreeExpanded)
 			{
 				defaultClassName = EXPANDED_CLASS_NAME;
 			}
 			String style=null;
 			String id = "";
 			if (parent != "0")
 			{
 				id = treeName + "N" + parent;
 				if(isTreeExpanded)
 				{
 					print("<ul class=expandedTree id='" + id + "' >");
 				}
 				else
 				{
 					print("<ul class=collapsedTree id='" + id + "' >");
 				}
 			}
 			else
 			{
 				id = treeName + "N" + parent;
 				print("<ul  id='" + id + "' >");
 			}
 			for (int i = 0; i < nodeList.getLength(); i++)
 			{
 				TreeNode node = nodeList.item(i);
 				if (node != null)
 				{
 					displayRadioButton = node.isShowRadioBtn();
 					if (node.getChildNodes().getLength() > 0)
 					{
 						hasChild = true;
 					}
 					else
 					{
 						hasChild = false;
 					}
 
 					if (node.getImageUrl()== "")
 					{
 						style = "style = 'FONT-WEIGHT:normal;'";
 					}
 					else
 					{
 						style = "style='list-style-image: url(" + node.getImageUrl() + ");'";
 					}
 					if (hasChild)
 					{
 						id = treeName + "P" + parent + i;
 						print("<li " + style + " class='"+defaultClassName+"' id='" + id + "'>");
 						if(displayRadioButton)
 						{
 							print("<input type='radio' name='selectedObjectId' id='selectedObjectId' value='" + node.getSequenceNumber() +"' />");
 						}
						print("<a class=treeview href=\"javascript:toggle('"+fieldForSelectedObject + "','"+treeName+"N" + parent + "_" + i
 								+ "','"+treeName+"P" + parent + i + "');"+ getNodeClickedFunction()+"('"+ fieldForSelectedObject+"Name') \">" + node.getText() + "</a>");
 					}
 					else
 						//Means it is a leaf node
 					{
 						id =treeName +  "L" + parent + i;
						node.setHref("javascript:changeSelection('"+fieldForSelectedObject+"','" + id + "','" + node.getSequenceNumber() + "');"+ getNodeClickedFunction()+"('"+ fieldForSelectedObject+"Name')");
 						if (node.getTarget() == "")
 						{
 							node.setTarget(target);
 						}
 						print("<li " + style + " class=file>");
 						if(displayRadioButton)
 						{
 							print("<input type='radio' name='selectedObjectId' id='selectedObjectId' value='" + node.getSequenceNumber() +"' />");
 						}
 						print("<a class=treeview href=\"" + node.getHref() + "\"  title=\"" + node.getToolTip() + "\" id='"
 								+ id + "'>" + node.getText() + "</a>");
 					}
 
 					if (hasChild)
 					{
 						loopThru(node.getChildNodes(), parent + "_" + i,treeName,fieldForSelectedObject,isTreeExpanded);
 					}
 
 					print("</li>");
 				}//If node != null
 			}//End of For
 			print("</ul>");
 		}//End of if nodelist ! null
 	}
 
 }
 
 
 
 
