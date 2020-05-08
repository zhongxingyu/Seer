package thesaurus.parsing;
 
 import java.util.LinkedList;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 
 
 public class Handler extends DefaultHandler
 {
 	int noNodes=Integer.MAX_VALUE;
 	int edges=0;
 	boolean getWord=false;
 	LinkedList<Vertex> nodes = new LinkedList<Vertex>();
 	
 	public Handler(int range)
 	{
 		noNodes=range++;
 	}
 	
 	public Handler(){}
 	
 	public void startDocument ()
 	{
 		//System.out.println("Started parsing");
 		
 	}
 	
 	public void endDocument ()
 	{
 		//System.out.println("Finished parsing");
 	}
 	
 	public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException
 	{
 		if(qname.equalsIgnoreCase("node") && nodes.size()<noNodes)
 			{		
 				for(int i=0;i<attributes.getLength();i++)
 				{
 					if(attributes.getQName(i).equalsIgnoreCase("id"))
 					{
 						String id = attributes.getValue(i);
 						 Vertex v = new Vertex(id);
 						 nodes.add(v);			 
 					}
 				}
 			}
 		
 		//need the nodes check to stop it trying to add to vertexs that don't exist
 		if(qname.equalsIgnoreCase("edge"))
 		{
 			int source = 0;
 			int target = 0;
 			for(int i=0;i<attributes.getLength();i++)
 			{
 				if(attributes.getQName(i).equalsIgnoreCase("source"))
 				{
 					source = Integer.parseInt(attributes.getValue(i));
 				}
 				if(attributes.getQName(i).equalsIgnoreCase("target"))
 				{
 					target = Integer.parseInt(attributes.getValue(i));
 				}
 			}
 			//check to see vertexs exist
 			if (getVertex(source)!=null && getVertex(target)!=null)
 			{
 				Vertex v = this.getVertex(source);
 				v.addToAdjList(target);
 			}
 		}
 		if(qname.equalsIgnoreCase("data")) getWord = true;
 	}
 	
 	public void endElement(String uri, String localName, String qname) throws SAXException{
 		//System.out.println("leaving"+qname);
 	}
 	
 	
 	/* if the xml is in the wrong order, this method will still work */
 	public Vertex getVertex(int index)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getIndex()==index) return v;
 		}
 		return null;
 	}
 	
 	public Vertex getVertex(String word)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getWord().equalsIgnoreCase(word)) return v;
 		}
 		return null;
 	}
 	
 	public void characters(char ch[], int start, int length)
 	{
 		if(getWord)
 		{
 			String word = new String(ch,start,length);
 			nodes.getLast().setWord(word);
 			getWord=false;
 		}
 	}
 }
