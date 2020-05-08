 package thesaurus.parser;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 /*
  * 
  */
 
 public class Graph 
 {
 	private LinkedList<Vertex> nodes = new LinkedList<Vertex>();
 
 	 LinkedList<Vertex> getNodes() 
 	{
 		return nodes;
 	}
 
 	void setNodes(LinkedList<Vertex> nodes)
 	{
 		this.nodes = nodes;
 	}
 	
 	 void clear()
 	{
 		nodes.clear();
 	}
 	
 	 void add(Vertex v)
 	{
 		nodes.add(v);
 	}
 	
 	int size()
 	{
 		return nodes.size();
 	}
 	
 	void setLastWord(String word)
 	{
 		nodes.getLast().setWord(word);
 	}
 	
 	boolean contains(Vertex v)
 	{
 		for(Vertex x : nodes)
 		{
 			if(v.getIndex().equals(x.getIndex()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	 Vertex getVertexFromIndex(String index)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getIndex().equals(index)) return v;
 		}
 		return null;
 	}
 	
 	 LinkedList<Vertex> getList()
 	{
 		return nodes;
 	}
 	 Vertex getVertexFromWord(String word)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getWord().equalsIgnoreCase(word)) return v;
 		}
 		return null;
 	}
 	 
 	 
 	 /* Breadth First Search*/
 	    LinkedList<Vertex> getListOfSynomyns(String s, int max)
 		{
 			LinkedList<Vertex> workQueue = new LinkedList<Vertex>();
 			LinkedList<Vertex> results = new LinkedList<Vertex>();
 			Vertex start = this.getVertexFromWord(s);
 			if(start==null) return null;
 			workQueue.add(start);
 			while(!workQueue.isEmpty() && results.size()<max)
 			{
 				Vertex current = workQueue.remove();
 				results.add(current);
 				for(Vertex child : current.getSynomyns())
 				{
 					if(!workQueue.contains(child) && !results.contains(child))
 					{
 						workQueue.add(child);
 					}
 				}
 			}
 			return results;
 		}
 	 
	 public HashMap<String, LinkedList<String>> getTableData()
 		{	
 			HashMap<String,LinkedList<String>> tableData = new HashMap<String, LinkedList<String>>();
 			for(Vertex v : this.getList())
 			{
 				String k = v.getWord();
 				LinkedList<String> synms = new LinkedList<String>();
 				for(Vertex n : v.getSynomyns())
 				{
 					synms.add(n.getWord());
 				}
 				tableData.put(k,synms);
 			}	
 			return tableData;
 		}	
 	
 }
