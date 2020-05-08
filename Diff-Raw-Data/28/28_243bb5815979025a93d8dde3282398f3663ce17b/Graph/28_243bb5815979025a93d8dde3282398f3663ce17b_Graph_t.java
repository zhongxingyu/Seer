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
 	
 	
 	/* if the xml is in the wrong order, this method will still work */
 	 Vertex getVertex(int index)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getIndex()==index) return v;
 		}
 		return null;
 	}
 	
 	 LinkedList<Vertex> getList()
 	{
 		return nodes;
 	}
 	 Vertex getVertex(String word)
 	{
 		for(Vertex v : nodes)
 		{
 			if(v.getWord().equalsIgnoreCase(word)) return v;
 		}
 		return null;
 	}
 	 
 	 
 	 /* Breadth First Search*/
 	    LinkedList<Vertex> getSynmsFor(String s, int max)
 		{
 			LinkedList<Vertex> workQueue = new LinkedList<Vertex>();
 			LinkedList<Vertex> results = new LinkedList<Vertex>();
 			Vertex start = this.getVertex(s);
			if(start==null) return null;
 			workQueue.add(start);
 			
 			while(!workQueue.isEmpty() && results.size()<max)
 			{
 				Vertex current = workQueue.remove();
 				results.add(current);
 				for(AdjListNode n : current.getAdjList())
 				{
 					Vertex child = this.getVertex(n.getVertexNumber());
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
 				for(AdjListNode n : v.getAdjList())
 				{
 					synms.add(this.getVertex(n.getVertexNumber()).word);
 				}
 				tableData.put(k,synms);
 			}	
 			return tableData;
 		}	
 	
 }
