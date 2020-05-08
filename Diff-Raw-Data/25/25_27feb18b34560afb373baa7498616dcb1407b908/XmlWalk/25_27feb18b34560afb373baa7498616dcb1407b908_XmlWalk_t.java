 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * 
  * @author DarkCthulhu
  * 
  */
 
 public class XmlWalk {
 	String xmlString;
 	String[] ignoredTags;
 	DocumentBuilder builder;
 	XPathFactory xpf;
 	Document doc;
 	ArrayList<ExtendedNode> NodeList;
 	
 	/**
 	 * Construction of XmlWalk object from xmlFile
 	 * @param xmlFile: xmlFile supplied
 	 * @param ignoredTags: tags to ignore in traversal
 	 * @throws XPathExpressionException: Attribute name must be well-formed
 	 */
 	public XmlWalk(File xmlFile, String[] ignoredTags) throws XPathExpressionException{
 		this(ignoredTags); //call to private default constructor
 		try {
 			this.doc = this.builder.parse(xmlFile);
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		traverseXml();
 	}
 	
 	/**
 	 * String xml Constructor
 	 * @param xmlContent
 	 * @param ignoredTags
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 */
 	public XmlWalk(String xmlContent, String[] ignoredTags) throws SAXException, IOException, XPathExpressionException{
 		this(ignoredTags); //call to private default constructor
 		this.doc = this.builder.parse(new InputSource(new StringReader(xmlContent)));
 		traverseXml();
 	}
 	
 	/***
 	 * Private constructor, called by other 2. Common initialization
 	 * @param ignoredTags
 	 */
 	private XmlWalk(String[] ignoredTags){
 		try {
 			this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		this.ignoredTags = ignoredTags;
 		this.xpf = XPathFactory.newInstance();	
 		this.NodeList = new ArrayList<ExtendedNode>();
 	}
 	
 	/**
 	 * Main logic performing traversal and adding elements to the Arraylist
 	 * @throws XPathExpressionException
 	 */
 	private void traverseXml() throws XPathExpressionException{
 		XPath xpr = this.xpf.newXPath();
 		String xpathQuery = buildQuery();
 		NodeList nodes = (NodeList)xpr.evaluate(xpathQuery, doc, XPathConstants.NODESET);
 		int left = 1, prevItem = 0;
 		
 		//got the nodes, now let's iterate!
 		for (int i=0, len=nodes.getLength(); i<len; i++) {
 			Node item = nodes.item(i);
 			
 			//when traverse level changes by a negative quantity, adjustment needs to be made to numbering
 			int levelChange = (getNodeLevel(item) - prevItem);
 			if(levelChange <= 0){
 				left += Math.abs(levelChange)+1;
 			}
 			
 			//store into arraylist, node and left, right values
 			int right = left + 2*getChildren(item.getChildNodes(), 0) + 1;
 			ExtendedNode enode = new ExtendedNode(item, left, right);
 			this.NodeList.add(enode);
 
 			//ready for the next iteration
 			prevItem = getNodeLevel(item);
 			left++;
         }
 	}
 	
 	/**
 	 * Calculates right-count
 	 * @param nl
 	 * @param count
 	 * @return
 	 */
     private int getChildren(NodeList nl, int count){
     	for(int x = 0; x < nl.getLength(); x++){
        	if(nl.item(x).getNodeType() == Node.ELEMENT_NODE){
        		for(String str : this.ignoredTags){
        			if(nl.item(x).getNodeName().equals(str)){
        				count--;
        				break;
        			}
        		}
     			count++;
         		if(nl.item(x).hasChildNodes())
         			count = getChildren(nl.item(x).getChildNodes(), count);
         	}
         }
     	return count;
     }
     
     /**
      * Gets level of any node with respect to root
      * @param node
      * @return
      */
     private int getNodeLevel(Node node){
     	int count = 0;
     	node = node.getParentNode();
     	while(node!=null){
     		count++;
     		node = node.getParentNode();
 	    }
     	return count;
     }
     
     /***
      * Build XPath query with ignoreTags to be ignored
      * E.g. //*[not(self::a or self::html or self::div)]
      * @return
      */
 	private String buildQuery(){
 		String selector = "//*";
		ArrayList<String> temp = new ArrayList<String>();

 		if(this.ignoredTags == null || this.ignoredTags.length == 0){
 			return selector;
 		}
 		
 		for (int i = 0; i < this.ignoredTags.length; i++)
			temp.add("self::" + this.ignoredTags[i]);
 		
		String[] xpathExclude = new String[ temp.size() ];
		return selector + "[not(" + join(temp.toArray(xpathExclude), " or ") +")]";
 	}
 	
 	/**
 	 * Gets result as an arrayList
 	 * @return
 	 */
 	public ArrayList<ExtendedNode> getResult(){
 		return this.NodeList;
 	}
 	/**
 	 * http://stackoverflow.com/a/8692882/759019
 	 * @param r
 	 * @param d
 	 * @return
 	 */
 	private static String join(String r[],String d)
 	{
 	        if (r.length == 0) return "";
 	        StringBuilder sb = new StringBuilder();
 	        int i;
 	        for(i=0;i<r.length-1;i++)
 	            sb.append(r[i]+d);
 	        return sb.toString()+r[i];
 	}
 }
