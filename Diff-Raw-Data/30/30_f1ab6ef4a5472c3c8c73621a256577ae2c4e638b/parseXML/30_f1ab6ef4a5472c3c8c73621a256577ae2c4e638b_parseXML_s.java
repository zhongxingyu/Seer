 /*
    This code parses b.xml to get the location of another file a.xml.
    The file a.xml contains a tree, this code parses a.xml to create that tree.
  */
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.util.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 import tree.Node;
 
 public class parseXML 
 {	
 	public static void main(String[] args) 
 	{
 		String locationFile = "b.xml" ;
 		String XPATH = "location";
 		String xmlFile = null;
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		docFactory.setNamespaceAware(true);
 		DocumentBuilder builder;
 		
 		
 		//reading b.xml to get the location of a.xml
 		try {
 			builder = docFactory.newDocumentBuilder();
 			Document doc = builder.parse(locationFile);
 			XPathExpression expr = XPathFactory.newInstance().newXPath().compile(XPATH);
 			Object hits = expr.evaluate(doc, XPathConstants.NODESET ) ;
 			if ( hits instanceof NodeList ) {
 				NodeList list = (NodeList) hits ;
 				for (int i = 0; i < list.getLength(); i++ ) {
 					Element element = (Element) list.item(i);
 					xmlFile = element.getTextContent();
 					//System.out.println(xmlFile);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 
 		//parsing a.xml
 		
 		/*
 		   The while loop updates the XPATH to get the nodes at level 0 then level 1 and so on .... 
 		   it breaks out of the while loop when there are no nodes found at some level.
 		   */
 		
 		XPATH="";
 		String elementAttribute="";
 		String elementValue="";
 		int level = 0;
 		Node root = new Node();
 
 		while(true)
 		{
 			XPATH+="/node";
 			try {
 				builder = docFactory.newDocumentBuilder();
 				Document doc = builder.parse(xmlFile);
 				XPathExpression expr = XPathFactory.newInstance().newXPath().compile(XPATH);
 				Object hits = expr.evaluate(doc, XPathConstants.NODESET ) ;
 				if ( hits instanceof NodeList ) {
 					NodeList list = (NodeList) hits ;
 					
 					if(list.getLength()==0)
 						break;
 					
 					//System.out.println("\n\nAt level "+level+" ....");
 					
 					//getting all the nodes at level (level-1), they would be parent nodes for the current level
 					
 					//please note that the node with attribute "Target" won't be a parent node for any node. 
 					//Because when we reach the "Target" node, we already have a class, hence decision tree won't branch any further.
 					//Thus this approach would work with unbalanced (height balance) trees as well.
 
 					//The list will act as a queue. We start with root and at the end we have all the elements of the desired level
 					
 					List<Node> currLevelNodes = new ArrayList<Node>();
 					if(level!=0)
 					{
 						currLevelNodes.add(root);
 						while(currLevelNodes.get(0).getLevel()!=(level-1))
 						{
							if(currLevelNodes.get(0).leftChild.getAttribute()!="Target")
 							{
 								currLevelNodes.add(currLevelNodes.get(0).leftChild);
 								currLevelNodes.add(currLevelNodes.get(0).rightChild);
 							}
 							currLevelNodes.remove(0);
 						}
 					}
 
 
 					for (int i = 0; i < list.getLength(); i++ ) {
 						Element element = (Element) list.item(i);
 						elementAttribute = element.getAttribute("attribute");
 						//System.out.println(elementAttribute);
 						elementValue = element.getAttribute("value");
 						//System.out.println(elementValue);
 
 						Node newNode = new Node();
 						newNode.setValue(Float.parseFloat(elementValue));
 						newNode.setAttribute(elementAttribute);
 						newNode.setLevel(level);
 
 						//inserting current level nodes in the tree
 						if(level==0)
 							root = newNode;
 						else
 						{
 							if(i%2==0)
 								currLevelNodes.get(i/2).leftChild=newNode;
 							else
 								currLevelNodes.get(i/2).rightChild=newNode;
 						}
 
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			level++;
 		}
 	}
 }
