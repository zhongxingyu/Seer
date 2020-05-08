 package thesaurus.parser;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public class InternalRepresentation
 {
 	private Graph nodes = new Graph();
 	private XmlRead read;
 	private XmlWrite write;
 
 	
 	public InternalRepresentation(File f)
 	{
 		read = new XmlRead(f);
 		nodes.setNodes(read.getAllNodes());
 		write = new XmlWrite(f, nodes);
 	}
 	/**
 	 * Adds a vertex to the xml file. The synomyns
 	 * must be valid vertices in the xml, too.
 	 * 
 	 * @param w			The word belonging to the vertex
 	 * @param synomyns	The synonyms for the vertex 
 	 */
 	public void addVertex(String w, String synomyns, String antonyms)
 	{
		String index = read.getLastVertexIndex();
 		Vertex n = new Vertex(index);
 		n.setWord(w);
 		for(String s : parseCsvToArray(synomyns))
 		{
 			Vertex syn = nodes.getVertexFromWord(s);
 			if(syn==null)
 			{
 				Vertex v = new Vertex(read.getLastVertexIndex());
 				v.setWord(s);
 				n.addSynonym(v);
 				System.out.println("Vertex to be created and saved: "+v);
 				write.addVertex(v);
 			}
 			else
 			{
 				n.addSynonym(syn);
 			}
 		}
 		for(String a : parseCsvToArray(antonyms))
 		{
 			Vertex ant = nodes.getVertexFromWord(a);
 			if(ant==null)
 			{
 				Vertex v = new Vertex(read.getLastVertexIndex());
 				v.setWord(a);
 				n.addSynonym(v);
 				write.addVertex(v);
 			}
 			else
 			{
 				n.addAntonym(ant);
 			}
 		}
 		System.out.println(n.getWord());
 		System.out.println("Vertex to be added" + n);
 		
 		nodes.add(n);
 		write.addVertex(n);
 	}
 	
 	
 	
 	public String createNew()
 	{
 		StringBuilder x = new StringBuilder();
		x.append("<graphml><graph></graph></graphml>");
 		return x.toString();
 	}
 	
 	/**
 	 * Edits the word of a vertex. This method
 	 * edits the word directly, it doesn't create a new vertex
 	 * and delete the old one.
 	 * 
 	 * @param oldWord The current word of the vertex to be edited
 	 * @param newWord The new word you wish the vertex to contain
 	 */
 	
 	public void editVertex(String oldWord, String newWord)
 	{
 		Vertex old = nodes.getVertexFromWord(oldWord);
 		old.setWord(newWord);
 		write.editVertex(oldWord, newWord);
 	}
 	
 	/**
 	 * Removes a vertex from the xml file, including
 	 * all the relevant edges.
 	 * 
 	 * @param w the word belonging to the vertex to be deleted.
 	 */
 	public void removeVertex(String w)
 	{
 		Vertex r = nodes.getVertexFromWord(w);
 		write.removeVertex(w);
 		nodes.removeVertex(r);
 	}
 	
 	//need to alter xml too
 	public void addSynonym(String word, String synonym)
 	{
 		System.out.println(word+" "+synonym);
 		Vertex w = nodes.getVertexFromWord(word);
 		Vertex s = nodes.getVertexFromWord(synonym);
 		w.addSynonym(s);
 		write.addEdge(word, synonym,"s");
 	}
 	
 	public void addAntonym(String word, String antonym)
 	{
 		Vertex w = nodes.getVertexFromWord(word);
 		Vertex a = nodes.getVertexFromWord(antonym);
 		w.addAntonym(a);
 		write.addEdge(word, antonym, "a");
 	}
 	
 	public void debug()
 	{
 		nodes.debug();
 	}
 	
 	/* facade pattern */
 	public LinkedList<Vertex> getListOfSynomyns(String s, int max)
 	{
 		return nodes.getListOfSynomyns(s, max);
 	}
 	
 	public Vertex getFirst()
 	{
 		return nodes.getNodes().getFirst();
 	}
 	
 	public Vertex getOneSynomyn(String s)
 	{
 		System.out.println("method called for "+s);
 		if(s.equalsIgnoreCase("")) return null;
 		if(nodes.getListOfSynomyns(s,1)!=null) return nodes.getListOfSynomyns(s, 1).getFirst();
 		return null;
 	}
  	
 	public LinkedList<Vertex> getListOfSynomyns(String s){return getListOfSynomyns(s, 100);}
 	
 	public HashMap<String, HashMap<String, LinkedList<String>>> getTableData()
 	{
 		return nodes.getTableData();
 	}
 	
 	 private String[] parseCsvToArray(String inputCsvString){
          String[] hold = inputCsvString.split(",");
          String[] toReturn = new String[hold.length];
          int i = 0;
          for(String s:hold){
                  toReturn[i++] = s.replaceAll("\\s","");
          }
          return toReturn;
 	 }
 }
