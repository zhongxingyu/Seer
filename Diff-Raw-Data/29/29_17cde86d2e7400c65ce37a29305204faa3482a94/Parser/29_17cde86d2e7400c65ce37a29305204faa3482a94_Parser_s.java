package thesaurus.parsing;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 //use boolean flags to indicate to characters method what it should parse - switch statement
 
 public class Parser {
 	
 	String path;
 	Handler handler;
 	SAXParserFactory factory = SAXParserFactory.newInstance();
 	SAXParser parser;
 	List<Vertex> currentlyInUse = new LinkedList<Vertex>();
 	
 	public Parser(String path)
 	{
 		this.path = path;
 		try
 		{
 			 parser = factory.newSAXParser();
 		}
 		catch(Exception s) {System.out.println("couldn't create a parser");};
 	}
 	
 	public Parser(File f)
 	{
 		this.path = f.getPath();
 		try
 		{
 			 parser = factory.newSAXParser();
 		}
 		catch(Exception s) {System.out.println("couldn't create a parser");};
 	}
 	
 	public List<Vertex> getAll()
 	{
 		handler = new Handler();
 		try 
 		{
 			parser.parse(path, handler);
 		} catch (Exception e) 
 		{ System.out.println("parsing went wrong");
 		}
 		currentlyInUse.clear();
 		currentlyInUse.addAll(handler.nodes);
 		return handler.nodes;
 	}
 	
 	public List<Vertex> getRange(int range)
 	{
 		handler = new Handler(range);	
 		try
 		{
 			parser.parse(path,handler);
 		}
 		catch(Exception e)
 		{
 			System.out.println("get range messed up");
 		}
 		currentlyInUse.clear();
 		currentlyInUse.addAll(handler.nodes);
 		return handler.nodes;
 	}
 	
 	public List<Vertex> getCurrentlyInUse(){return currentlyInUse;}	
 	
 	/* Breadth First Search */
 	public List<Vertex> getSynmsFor(String s, int max)
 	{
 		LinkedList<Vertex> workQueue = new LinkedList<Vertex>();
 		List<Vertex> results = new LinkedList<Vertex>();
 		this.getAll();
 		Vertex start = handler.getVertex(s);
 		workQueue.add(start);
 		
 		while(!workQueue.isEmpty() && results.size()<max)
 		{
 			Vertex current = workQueue.remove();
 			results.add(current);
 			for(AdjListNode n : current.getAdjList())
 			{
 				Vertex child = handler.getVertex(n.getVertexNumber());
 				if(!workQueue.contains(child) && !results.contains(child))
 				{
 					workQueue.add(child);
 				}
 			}
 		}
 		currentlyInUse.clear();
 		currentlyInUse.addAll(results);
 		return results;
 	}
  	
 	public List<Vertex> getSynmsFor(String s){return getSynmsFor(s, 100);}
 		
 	//dictionary key is word, value is linkedlist of synomns	
 	public HashMap<String, List<String>> getTableData()
 	{	
 		HashMap<String,List<String>> tableData = new HashMap<String, List<String>>();
 		for(Vertex v : currentlyInUse)
 		{
 			String k = v.getWord();
 			LinkedList<String> synms = new LinkedList<String>();
 			for(AdjListNode n : v.getAdjList())
 			{
 				synms.add(handler.getVertex(n.getVertexNumber()).word);
 			}
 			tableData.put(k,synms);
 		}	
 		return tableData;
 	}	
 	
 }
