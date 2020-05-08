 /**
  * 
  */
 package ecologylab.generic;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Tools for manipulating org.w3c.documents
  * 
  * @author andruid
  */
 public class DomTools extends Debug
 {
 	private static final int	TAB_WIDTH	= 3;
 	/**
 	 * Print your DOM tree in a readable way.
 	 * 
 	 * @param node
 	 */
   public static void prettyPrint(Node node) 
   {
   	prettyPrint(node, 0);
   }
   private static void prettyPrint(Node node, int level) 
   {
     try 
     {
     	if ("#document".equals(node.getNodeName()))
     	{
    		Node nextChild = node.getFirstChild();
				prettyPrint(nextChild, level);
    		if (nextChild.getNextSibling() != null)
    		{
    			warning(node, "multiple root element!");
    			while (nextChild != null)
    			{
    				prettyPrint(nextChild, level);
    				nextChild = nextChild.getNextSibling();
    			}
    		}
     		return;
     	}
     	for (int i=0; i< level; i++)
     		printTab();
     	
       System.out.print("<" + node.getNodeName());
       NamedNodeMap attrMap = node.getAttributes();
       if (attrMap != null)
 	      for (int i = 0; i < attrMap.getLength(); i++) 
 	      {
 	        Node attr = attrMap.item(i);
 	        String attrName = attr.getNodeName();
 					System.out.print(" " + attrName + "=\"" + attr.getNodeValue() + '"');
 	      }
       String value	= node.getNodeValue();
       if (value != null)
       	System.out.print(value);
       System.out.print(">");
 
       NodeList nl = node.getChildNodes();
       if (nl != null)
       {
 	      int numChildren = nl.getLength();
 	      boolean printedNewline	= false;
 	      if (numChildren > 0)
 	      {
 					for (int i = 0; i < numChildren; i++) 
 		      {
 		        Node childNode = nl.item(i);
 		        if ("#text".equals(childNode.getNodeName()))
 		        	System.out.print(childNode.getTextContent());
 		        else
 		        {
 		        	if (!printedNewline)
 		        	{
 		        		printedNewline	= true;
 		  	        System.out.print("\n");		        		
 		        	}
 		        	prettyPrint(childNode, level + 1);		        	
 		        }
 		      }
 		    	for (int i=0; i< level; i++)
 		    		printTab();
 	      }
       }
       System.out.println("</" + node.getNodeName() + ">");
     } catch (Throwable e) 
     {
       System.out.println("Cannot print!! " + e.getMessage());
       e.printStackTrace();
     }
   }
 	private static void printTab()
 	{
 		for (int i=0; i<TAB_WIDTH; i++)
 			System.out.print(' ');
 	}
 
 	public static String getAttribute(Node node, String name)
 	{
 		String result	= null;
 		if (node != null)
 		{
 			Node attrNode	= node.getAttributes().getNamedItem(name);
 			if (attrNode != null)
 			{
 				result			= attrNode.getNodeValue();
 			}
 		}
 		return result;
 	}
 	
 }
