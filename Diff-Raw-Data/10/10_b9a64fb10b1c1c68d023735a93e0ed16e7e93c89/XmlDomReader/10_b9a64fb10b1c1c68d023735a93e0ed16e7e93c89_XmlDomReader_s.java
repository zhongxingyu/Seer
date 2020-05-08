 package edu.isi.bmkeg.utils.xml;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
import com.sun.org.apache.xpath.internal.XPathAPI;

 /*
  * 
  */
 public class XmlDomReader {
 	public boolean debug = false;
 	protected DocumentBuilderFactory dbf;
 	protected DocumentBuilder docBuilder;
 	protected Document doc;
 	protected Element rootTag;
 
 	/*
 	 * 
 	 */
 	public XmlDomReader(File inputFile) throws ParserConfigurationException,
 			SAXException, IOException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		docBuilder = dbf.newDocumentBuilder();
 		if (debug)
 			System.out.println(inputFile.getAbsolutePath() + " parsing");
 		doc = docBuilder.parse(inputFile);
 
 		rootTag = doc.getDocumentElement();
 		if (debug)
 			System.out.println("DEBUG: " + rootTag.getNodeName());
 	}
 
 	public XmlDomReader(String xmlText) throws ParserConfigurationException,
 			SAXException, IOException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		docBuilder = dbf.newDocumentBuilder();
 		if (debug)
 			System.out.println("parsing" + xmlText);
 		InputStream in = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
 		doc = docBuilder.parse(in);
 
 		rootTag = doc.getDocumentElement();
 		if (debug)
 			System.out.println("DEBUG: " + rootTag.getNodeName());
 	}
 
 	/**
 	 * given an xpath expression it will return the nodelist that matches the
 	 * path expr
 	 * 
 	 * @param xpathExpr
 	 * @return
 	 */
 	public NodeList getNodes(String xpathExpr) {
 		NodeList nodesFound = null;
		try {
 			nodesFound = XPathAPI.selectNodeList(rootTag, xpathExpr);
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
		}
 		return nodesFound;
 	}
 	
 }
