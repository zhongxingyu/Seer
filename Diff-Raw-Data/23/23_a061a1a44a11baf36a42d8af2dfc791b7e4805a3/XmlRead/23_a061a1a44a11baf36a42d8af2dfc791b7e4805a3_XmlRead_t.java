 package thesaurus.parser;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class XmlRead
 {
 	private Document xml;
 	private XPathFactory xfactory = XPathFactory.newInstance();
 	private XPath xpath = xfactory.newXPath();
 	private Graph nodes = new Graph();
 	
 	public XmlRead(File f)
 	{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		try 
 		{
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			this.xml = docBuilder.parse(f.getPath());
 		} catch (IOException e) 
 		{
 			System.out.println("bad file");
 		}
 		catch(SAXException s)
 		{
 			System.out.println("Invalid file");
 		}
 		catch(IllegalArgumentException a)
 		{
 			System.out.println("illegal Argument");
 		}
 		catch(ParserConfigurationException p)
 		{
 			p.printStackTrace();
 		}
 		getVertices();
 		getEdges();
 	}
 	
	String getLastVertexIndex() throws NullPointerException
 	{
 		String newID = null;
 		try
 		{
 			XPathExpression expr = xpath.compile("//graphml/graph/node[position()=last()]");
 			Node result = (Node) expr.evaluate(this.xml,XPathConstants.NODE);
 			String id = result.getAttributes().getNamedItem("id").getTextContent();
 			int tempID = Integer.parseInt(id);
 			tempID++;
 			newID = Integer.toString(tempID);
 		} 
 		catch (XPathExpressionException e)
 		{
 			e.printStackTrace();
 		}
 		return newID;
 	}
 	
 	
 	
 	public LinkedList<Vertex> getAllNodes()
 	{
 		return nodes.getNodes();
 	}
 	
 	
 	private void getVertices()
 	{
 		NodeList nodes = null;
 		LinkedList<Vertex> vertices = new LinkedList<Vertex>();
 		try
 		{
 			XPathExpression expr = xpath.compile("//graphml/graph/node");
 		    nodes = (NodeList) expr.evaluate(this.xml, XPathConstants.NODESET);
 		} catch (XPathExpressionException e)
 		{
 			e.printStackTrace();
 		}
 		
 		for(int i=0;i<nodes.getLength();i++)
 		{
 			Node n = nodes.item(i);
 			String id = n.getAttributes().getNamedItem("id").getTextContent();
 			Vertex v = new Vertex(id);
 			for(int j=0;j<n.getChildNodes().getLength();j++)
 			{
 				Node d = n.getChildNodes().item(j);
 				if(d.getNodeName().equals("data"))
 				{
 					String word = d.getTextContent();
 					v.setWord(word);
 				}
 			}
 			if(!this.nodes.contains(v)) vertices.add(v);
 		}
 		this.nodes.setNodes(vertices);
 	}	
 	
 	
 	private void getEdges()
 	{
 		NodeList edges = null;
 		boolean isSynomyn = false;
 		try 
 		{
 			XPathExpression expr = xpath.compile("//graphml/graph/edge");
 		    edges = (NodeList) expr.evaluate(this.xml, XPathConstants.NODESET);
 		} 
 		catch (XPathExpressionException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		for(int i=0;i<edges.getLength();i++)
 		{
 			Node e = edges.item(i);
 			//loop in case e has several child nodes
 			for(int j=0;j<e.getChildNodes().getLength();j++)
 			{
 				Node d = e.getChildNodes().item(j);
 				if(d==null)
 				{
 					System.out.println("skipping dodgy edge");
 					continue;
 				}
 				if(d.getNodeName().equals("data"))
 						{
 							if(d.getTextContent().equals("s"))
 							{
 								isSynomyn = true;
 							}
 							else if(d.getTextContent().equals("a"))
 							{
 								isSynomyn = false;
 							}
 						}
 			}
 			String source = e.getAttributes().getNamedItem("source").getTextContent();
 			String target = e.getAttributes().getNamedItem("target").getTextContent();
 			Vertex s = nodes.getVertexFromIndex(source);
 			Vertex t = nodes.getVertexFromIndex(target); 
 			if(isSynomyn)
 			{
 				if(!s.getSynomyns().contains(t))
 				s.addSynonym(t);
 			}
 			else
 			{	
 				if(!s.getAntonyms().contains(t))
 				s.addAntonym(t);
 			}
 		}
 	}	
 }
 
 
