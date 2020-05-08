 package thesaurus.parser;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
 public class XmlFile {
 	
 	private Document xml;
 	
 	
 	public XmlFile (String path)
 	{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		try 
 		{
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			this.xml = docBuilder.parse(getClass().getResource(path).getPath());
 		} catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public String toString()
 	{
		NodeList n = this.xml.getElementsByTagName("node");
		for(int i=0;i<n.getLength();i++)
		{
			System.out.println(n.item(i).getNodeName());
			System.out.println(n.item(i).getFirstChild().getNodeName());
		}
 		return null;
 	}
 
 }
