 package thesaurus.parser;
 
 import javax.xml.parsers.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 public class XmlFile {
 	
 	private Document xml;
 	
 	
 	public XmlFile (String path)
 	{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		try 
 		{
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			this.xml = docBuilder.parse(path);
 		} catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public String toString()
 	{
		Node graphML = this.xml.getFirstChild();
		System.out.println(graphML.toString());
 		return null;
 	}
 
 }
