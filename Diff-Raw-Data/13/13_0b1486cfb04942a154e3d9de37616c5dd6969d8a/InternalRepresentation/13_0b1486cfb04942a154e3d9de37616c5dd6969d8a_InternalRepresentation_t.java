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
 			n.addSynonym(syn);
 		}
 		for(String a : parseCsvToArray(antonyms))
 		{
 			Vertex ant = nodes.getVertexFromWord(a);
 			n.addAntonym(ant);
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
		//r is correct
 		write.removeVertex(w);
		nodes.removeVertex(r);
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
 	
 	public Vertex getOneSynomyn(String s)
 	{
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
