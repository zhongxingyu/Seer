 package thesaurus.parser;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 public class InternalRepresentation {
 	private HashGraph nodes = new HashGraph();
 	private XmlRead read;
 	private XmlWrite write;
 	private Catergories catergories;
 	private boolean emptyFile;
 
 	public InternalRepresentation(File f) {
 		read = new XmlRead(f);
 		this.emptyFile = read.emptyFile;
 		nodes.setNodes(read.getAllNodes());
 
 		write = new XmlWrite(f, nodes);
 		this.catergories = new Catergories();
 	}
 
 	public void addCatergory(String catergory) {
 		catergories.addCatergory(catergory);
 	}
 	
 	public String getCatergory(Vertex v)
 	{
 		return catergories.getCatergories(v);
 	}
 
 	public void removeCatergory(String catergory) {
 		catergories.removeCatergory(catergory);
 	}
 
 	public void addVertexToCatergory(String c, String word) {
 		catergories.addVertexToCatergory(c, word);
 	}
 
 	public void removeVertexToCatergory(String c, String word) {
 		catergories.removeVertexFromCategory(c, word);
 	}
 
 	/**
 	 * Adds a vertex to the xml file. The synomyns must be valid vertices in the
 	 * xml, too.
 	 * 
 	 * @param w
 	 *            The word belonging to the vertex
 	 * @param synomyns
 	 *            The synonyms for the vertex
 	 */
 	public void addVertex(String w, String synonyms, String antonyms,
 			String groupings) {
 		Vertex n;
 		boolean exists = false;
 		if (nodes.contains(nodes.getVertexFromWord(w))) {
 			n = nodes.getVertexFromWord(w);
 			exists = true;
 		} else {
 			String index = read.getIndex();
 			n = new Vertex(index);
 			n.setWord(w);
 		}
 		if (!synonyms.isEmpty()) {
 			addSynonyms(n, synonyms);
 		}
 		if (!antonyms.isEmpty()) {
 			addAntonyms(n, antonyms);
 		}
 		if (!groupings.isEmpty()) {
 			addGroupings(n, groupings);
 		}
 		nodes.add(n);
 		write.addVertex(n, exists);
 	}
 
 	private void addSynonyms(Vertex n, String synonyms) {
 		for (String s : parseCsvToList(synonyms)) {
 			Vertex syn = nodes.getVertexFromWord(s);
 			if (syn == null) {
 				Vertex v = new Vertex(read.getIndex());
 				v.setWord(s);
 				n.addSynonym(v);
 				nodes.add(v);
 				write.addVertex(v, false);
 			} else {
 				n.addSynonym(syn);
 				write.addVertex(n,true);
 			}
 		}
 	}
 
 	private void addAntonyms(Vertex n, String antonyms) {
 		for (String a : parseCsvToList(antonyms)) {
 			Vertex ant = nodes.getVertexFromWord(a);
 			if (ant == null) {
 				Vertex v = new Vertex(read.getIndex());
 				v.setWord(a);
 				n.addAntonym(v);
 				nodes.add(v);
 				write.addVertex(v, false);
 			} else {
 				n.addAntonym(ant);
 				write.addVertex(n,true);
 			}
 		}
 	}
 
 	private void addGroupings(Vertex n, String catergory) {
 
 		LinkedList<String> vertices = this.catergories.getCatergory(catergory);
 		if (vertices == null) {
 			this.catergories.addCatergory(catergory).add(n.getWord());
 			return;
 		}
 		this.catergories.addVertexToCatergory(catergory, n.getWord());
 
 		for(String s:vertices){
 			Vertex added = nodes.getVertexFromWord(s);
 			ArrayList<Vertex> temp = new ArrayList<Vertex>(); 
 			for(String t:vertices){
 				Vertex toAdd = nodes.getVertexFromWord(t);
 				if(!added.getGroupings().contains(toAdd)){
 					temp.add(toAdd);
 				}
 			}
 			for(Vertex v:temp){
 				added.addGrouping(v);
 			}
 		}
 		write.addVertex(n, true);
 	}
 	
 	public boolean isEmptyFile() {
 		return emptyFile;
 	}
 
 	public void setEmptyFile(boolean emptyFile) {
 		this.emptyFile = emptyFile;
 	}
 
 	public String createNew() {
 		StringBuilder x = new StringBuilder();
 		x.append("<graphml><graph></graph></graphml>");
 		return x.toString();
 	}
 
 	/**
 	 * Edits the word of a vertex. This method edits the word directly, it
 	 * doesn't create a new vertex and delete the old one.
 	 * 
 	 * @param oldWord
 	 *            The current word of the vertex to be edited
 	 * @param newWord
 	 *            The new word you wish the vertex to contain
 	 */
 
 	public void editVertex(String w, String synonyms, String antonyms,
 			String groupings) {
 		Vertex editVertex = nodes.getVertexFromWord(w);
 		editVertex.getSynomyns().clear();
 		editVertex.getAntonyms().clear();
 		editVertex.getGroupings().clear();
 		addSynonyms(editVertex, synonyms);
 		addAntonyms(editVertex, antonyms);
 		addGroupings(editVertex, groupings);
 		/*
 		 * for (String s : synArr) { Vertex temp = nodes.getVertexFromWord(s);
 		 * editVertex.addSynonym(temp); } for (String a : antArr) {
 		 * editVertex.addAntonym(nodes.getVertexFromWord(a)); } for (String g :
 		 * grpArr) { editVertex.addGrouping(nodes.getVertexFromWord(g)); }
 		 */
 		write.removeVertex(w);
 		write.addVertex(editVertex, false);
 	}
 
 	public void editVertexWord(String oldWord, String newWord) {
 		Vertex old = nodes.getVertexFromWord(oldWord);
 		old.setWord(newWord);
 		write.editVertex(oldWord, newWord);
 	}
 
 	/**
 	 * Removes a vertex from the xml file, including all the relevant edges.
 	 * 
 	 * @param w
 	 *            the word belonging to the vertex to be deleted.
 	 */
 	public void removeVertex(String w) {
 
 		Vertex r = nodes.getVertexFromWord(w);
 		write.removeVertex(w);
 		nodes.removeVertex(r);
 	}
 
 	// need to alter xml too
 	public void addSynonym(String word, String synonym) {
 		Vertex w = nodes.getVertexFromWord(word);
 		Vertex s = nodes.getVertexFromWord(synonym);
 		w.addSynonym(s);
 		write.addEdge(word, synonym, "s");
 	}
 
 	public void addAntonym(String word, String antonym) {
 		Vertex w = nodes.getVertexFromWord(word);
 		Vertex a = nodes.getVertexFromWord(antonym);
 		w.addAntonym(a);
 		write.addEdge(word, antonym, "a");
 	}
 
 	/*
 	 * public void debug() { nodes.debug(); }
 	 */
 
 	/* facade pattern */
 	/*
 	 * public LinkedList<Vertex> getListOfSynomyns(String s, int max) { return
 	 * nodes.getListOfSynomyns(s, max); }
 	 */
 
 	public Vertex getFirst() {
		if (!nodes.getNodes().isEmpty()){
			return nodes.getRandVertex();
		}else{
			return null;
		}
		
 		// return nodes.getNodes().getFirst();
 	}
 
 	public Vertex getOneSynomyn(String s) {
 		System.out.println("method called for " + s);
 		if (s.equalsIgnoreCase(""))
 			return null;
 		return nodes.getVertexFromWord(s);
 		// if(nodes.getListOfSynomyns(s,1)!=null) return
 		// nodes.getListOfSynomyns(s, 1).getFirst();
 		// return null;
 	}
 
 	// public LinkedList<Vertex> getListOfSynomyns(String s){return
 	// getListOfSynomyns(s, 100);}
 
 	/*
 	 * public HashMap<String, HashMap<String, LinkedList<String>>>
 	 * getTableData() { return nodes.getTableData(); }
 	 */
 
 	/*
 	 * String[] parseCsvToArray(String inputCsvString) { String[] hold =
 	 * inputCsvString.split(","); String[] toReturn = new String[hold.length];
 	 * int i = 0; for (String s : hold) { toReturn[i++] = s.replaceAll("\\s",
 	 * ""); } return toReturn; }
 	 */
 
 	LinkedList<String> parseCsvToList(String inputCsvString) {
 		if (inputCsvString.isEmpty()) {
 			return new LinkedList<String>();
 		}
 		String[] split = inputCsvString.split(",");
 		LinkedList<String> syns = new LinkedList<String>();
 		for (String s : split) {
 			syns.add(s);
 		}
 		return syns;
 	}
 }
