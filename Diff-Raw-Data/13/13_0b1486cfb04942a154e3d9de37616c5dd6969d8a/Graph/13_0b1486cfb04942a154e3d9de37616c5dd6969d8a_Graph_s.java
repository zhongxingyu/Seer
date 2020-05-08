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
 	 
 	 public void debug()
 	 {
 		 for(int i=0;i<nodes.size();i++)
 		 {
 			 System.out.println("List index: "+i);
 		 }
 		 for(Vertex v : nodes)
 		 {
 			 System.out.println("Graph index:" + v.getID());
 		 }
 	 }
 	 
 	 
 	 void removeVertex(Vertex v)
 	 {
 		 for(int i=0;i<nodes.size();i++)
 		 {
 			 if(v.getID().equals(nodes.get(i).getID())) 
 			 {
 				 nodes.remove(i);
 			 }
 		 }
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
 			if(v.getID().equals(x.getID()))
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
 			if(v.getID().equals(index)) return v;
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
 	 
 	 public HashMap<String,HashMap<String, LinkedList<String>>> getTableData()
 		{	
 		 HashMap<String, HashMap<String, LinkedList<String>>> tableData = new HashMap<String, HashMap<String, LinkedList<String>>>();
 		 	for(Vertex v : this.getList())
 		 	{
 		 		HashMap<String,LinkedList<String>> words = new HashMap<String, LinkedList<String>>();	
 		 		LinkedList<String> syns = new LinkedList<String>();
 		 		LinkedList<String> ants = new LinkedList<String>();
  		 		for(Vertex s : v.getSynomyns())
 		 		{
  		 			if (s==null) continue;
 		 			syns.add(s.getWord());
 		 		}
 		 		words.put("synomyns", syns);
 		 		for(Vertex a : v.getAntonyms())
 		 		{
 		 			if(a==null) continue;
 		 			ants.add(a.getWord());
 		 		}
 		 		words.put("antonyms", ants);
 		 	tableData.put(v.getWord(), words);
 		 	}
 			return tableData;
 		}	
 	
 }
