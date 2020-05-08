 package com.delcyon.capo.webapp.models;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.delcyon.capo.xml.XPath;
 
 import eu.webtoolkit.jwt.ItemDataRole;
 import eu.webtoolkit.jwt.Orientation;
 import eu.webtoolkit.jwt.WAbstractItemModel;
 import eu.webtoolkit.jwt.WLink;
 import eu.webtoolkit.jwt.WModelIndex;
 import eu.webtoolkit.jwt.WText;
 /**
  * 
  * @author jeremiah
  *To implement a custom model, you need to reimplement the following methods:
 
 getIndex() and getParent() methods that allow one to navigate the model
 getColumnCount() and getRowCount() to specify the top level geometry and the nested geometry at every item
 getData() to return the data for an item
 optionally, getHeaderData() to return row and column header data
 optionally, getFlags() to indicate data options
  */
 public class DomItemModel extends WAbstractItemModel
 {
 
 	public enum DomUse
 	{
 		NAVIGATION,
 		ATTRIBUTES
 	}
 	private Element rootElement;
 	private DomUse domUse = null;
 	public DomItemModel(Element rootElement,DomUse domUse)
 	{
 		this.rootElement = rootElement;
 		this.domUse = domUse;
 	}
 	
 	@Override
 	public Object getHeaderData(int section, Orientation orientation, int role)
 	{
	    if(ItemDataRole.DisplayRole == role)
	    {
	        return rootElement.getLocalName();
	    }
	    else
	    {
	        return null;
	    }
 	}
 	
 	@Override
 	public int getColumnCount(WModelIndex parent)
 	{
 		if (domUse == DomUse.NAVIGATION)
 		{
 			return 1;
 		}
 		else if (domUse == DomUse.ATTRIBUTES)
 		{
 			return 2;
 		}
 		else
 		{
 			return rootElement.getAttributes().getLength();
 		}
 	}
 
 	@Override
 	public int getRowCount(WModelIndex parent)
 	{
 		Node parentNode = null;
 		if(parent == null)
 		{
 			parentNode = rootElement;						
 		}
 		else
 		{
 			parentNode = (Node) parent.getInternalPointer();
 		}
 		
 		if (domUse == DomUse.ATTRIBUTES)
 		{
 			if(parentNode instanceof Element)
 			{
 				return ((Element)parentNode).getAttributes().getLength();
 			}
 			else
 			{
 				return 0;
 			}
 		}
 		else
 		{
 			return parentNode.getChildNodes().getLength();
 		}
 	}
 
 	//this is really about what happens when given a child, trying to figure out it's parents index
 	//column will ALWAYS be 0
 	@Override
 	public WModelIndex getParent(WModelIndex index)
 	{
 		Node node = (Node) index.getInternalPointer();
 		if(node.getParentNode() == null)
 		{
 			return null;
 		}
 		
 		if(node instanceof Attr)
 		{
 			node = node.getParentNode();
 		}
 		
 		//if (node instanceof Element)
 		{
 			int count = -1;
 			Node startNode = node.getParentNode();
 			do
 			{
 				count++;
 				startNode = startNode.getPreviousSibling();
 			}while(startNode != null);
 			
 			return createIndex(count, 0, node.getParentNode());
 		}
 		//else 
 	}
 
 	@Override
 	public Object getData(WModelIndex index, int role)
 	{
 		if (role == ItemDataRole.DisplayRole)
 		{	
 			if (domUse == DomUse.ATTRIBUTES)
 			{
 				Attr attr = (Attr) index.getInternalPointer();
 				if (index.getColumn() == 0)
 				{
 					return attr.getLocalName();
 				}
 				else
 				{
 					return attr.getValue();
 				}
 			}
 			else
 			{
 				String nodeValue = ((Node)index.getInternalPointer()).getNodeValue();
 
 				if (nodeValue == null)
 				{
 					nodeValue = ((Node) index.getInternalPointer()).getLocalName();
 				}
 				return nodeValue;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	@Override
 	//we're making in index here using the our id as the ptr in create index
 	public WModelIndex getIndex(int row, int column, WModelIndex parent)
 	{
 		if (domUse == DomUse.ATTRIBUTES)
 		{
 			 //if (parent == null)
 			 {
 				 return createIndex(row, column, rootElement.getAttributes().item(row));
 			 }
 		}
 		else
 		{
 			Element parentElement = null;
 			if (parent != null)
 			{
 				parentElement = (Element) parent.getInternalPointer();
 			}
 			else
 			{
 				parentElement = rootElement;
 			}
 
 			if (parentElement.getChildNodes().getLength() > 0)
 			{
 				Element childElement = (Element) parentElement.getChildNodes().item(row);
 				if (column > 0)
 				{
 					Attr attr = (Attr) childElement.getAttributes().item(column);
 					return createIndex(row, column, attr);
 				}
 				else
 				{
 					return createIndex(row, column, childElement);	
 				}
 			}
 			return null;
 		}
 	}
 
 }
