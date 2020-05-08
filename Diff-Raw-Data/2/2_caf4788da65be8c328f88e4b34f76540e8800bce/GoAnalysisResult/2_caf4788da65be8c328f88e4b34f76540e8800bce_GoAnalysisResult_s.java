 /**
  * 
  */
 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 
 /**
  * Go Terms Analysis Result.
  * interface DSAncillaryDataSet is the minimal requirement to fit in to geWorkbench analysis's result output framework
  * 
  * wrapping for the result from ontologizer 2.0.
  * 
  * @author zji
  * @version $Id$
  */
 public class GoAnalysisResult extends CSAncillaryDataSet<CSMicroarray> {
 	private static final long serialVersionUID = -337000604982427702L;
 	static Log log = LogFactory.getLog(GoAnalysisResult.class);
 	
 	Set<String> changedGenes;
 	Set<String> referenceGenes;
 
 	static class ResultRow implements Serializable {
 		private static final long serialVersionUID = 8340126713281389148L;
 		
 		String name;
 		String namespace;
 		double p;
 		double pAdjusted;
 		int popCount;
 		int studyCount;
 		
 		ResultRow(String name, String namespace, double p, double pAdjusted, int popCount, int studyCount) {
 			this.name = name;
 			this.namespace = namespace;
 			this.p = p;
 			this.pAdjusted = pAdjusted;
 			this.popCount = popCount;
 			this.studyCount = studyCount;
 		}
 		
 		public String toString() {
 			return name+"|"+namespace+"|"+p+"|"+pAdjusted+"|"+popCount+"|"+studyCount;
 		}
 	}
 
 	ResultRow getRow(Integer goId) {
 		return result.get(goId);
 	}
 	
 	private Map<Integer, ResultRow> result = null;
 	
 	Map<Integer, ResultRow> getResult() {return result; }
 
 	public int getCount() {
 		if(result!=null)
 			return result.size();
 		else return 0;
 	}
 
 	/**
 	 * Constructor for a result not populated yet.
 	 * @param parent
 	 * @param label
 	 */
 	public GoAnalysisResult(DSDataSet<CSMicroarray> parent, String label) {
 		super(parent, label);
 		referenceGenes = new HashSet<String>();
 		changedGenes = new HashSet<String>();
 
 		result = new HashMap<Integer, ResultRow>();
 	}
 
 	void addResultRow(int goId, ResultRow row) {
 		result.put(goId, row);
 	}
 	
 	public File getDataSetFile() {
 		// no use. required by the interface
 		return null;
 	}
 
 	public void setDataSetFile(File file) {
 		// no use. required by the interface
 	}
 	
 	/**
 	 * return gene detail for a give gene symbol
 	 */
 	static public String getGeneDetail(String geneSymbol) {
 		if(geneDetails==null || geneDetails.get(geneSymbol)==null)
 			return "";
 		return geneDetails.get(geneSymbol).toString();
 	}
 	
 	/**
 	 * return entrez ID for a given gene symbol
 	 * @param geneSymbol
 	 * @return
 	 */
 	static public int getEntrezId(String geneSymbol) {
 		if(geneDetails==null || geneDetails.get(geneSymbol)==null)
 			return 0;
 		return geneDetails.get(geneSymbol).getEntrezId();
 	}
 
 	/**
 	 * return list of child IDs for a GO term ID
 	 * @param goTermId
 	 * @return
 	 */
 	static public List<Integer> getOntologyChildren(int goTermId) {
 		return ontologyChild.get(goTermId);
 	}
 	
 	/**
 	 * return GO term name for a given GO term ID
 	 * 
 	 */
 	static public String getGoTermName(int goTermId) {
 		GoTermDetail detail = termDetail.get(goTermId);
 		if(detail!=null) return detail.name;
 		else return "";
 	}
 
 	/**
 	 * return the set of gene annotated to the given Go Term ID
 	 * @param goId
 	 * @return
 	 */
 	static public Set<String> getAnnotatedGenes(int goTermId) {
 		return term2Gene.get(goTermId);
 	}
 	
 	static public  Set<Integer>  getNamespaceIds() {
 		return namespaceIds;
 	}
 
 	private static HashMap<Integer, Set<String>> term2Gene = new HashMap<Integer, Set<String> >();
 	private static Map<Integer, GoTermDetail> termDetail = new HashMap<Integer, GoTermDetail>();
 	private static Map<Integer, List<Integer> > ontologyChild = new HashMap<Integer, List<Integer> >();
 	private static Set<Integer> namespaceIds = new TreeSet<Integer>();
 	
 	private static final String PARSING_MARKER_GO = " GO:";
 	private static final String PARSING_MARKER_RELATIONSHIP = "relationship: ";
 	private static final String NAMESPACE_LABEL = "namespace: ";
 
 	public static void parseOboFile(String goTermsOBOFile) {
 		termDetail.clear();
 		ontologyChild.clear();
 		namespaceIds.clear();
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(goTermsOBOFile));
 			String line = br.readLine();
 			Integer id = null;
 			while(line!=null) {
 				if(line.startsWith("id: GO:")) {
 					id = Integer.valueOf( line.substring(7) );
 					String name = br.readLine().substring("name: ".length());
 					String thisNameSpace = br.readLine().substring(NAMESPACE_LABEL.length());
 					String def = br.readLine().trim();
 					while(!def.startsWith("def:") && def.length()>0)
 						def = br.readLine();
 						
 					if(def.length()>0) {
 						def = def.substring("def:\"".length(), def.indexOf("\" ["));
 					}
 					GoAnalysisResult.termDetail.put( id, new GoTermDetail(name, def) );
 
 					if(namespace.contains(name)) {
 						namespaceIds.add(id);
 						if(!thisNameSpace.equals(name)) {
 							log.error("namespace not match namespce node");
 						}
 						List<Integer> children = ontologyChild.get(0);
 						if(children==null) {
 							children = new ArrayList<Integer>();
 							ontologyChild.put(0, children);
 						}
 						children.add(id);
 					}
 				} else if (line.startsWith("is_a: GO:")) {
 					Integer parent = Integer.valueOf( line.substring("is_a: GO:".length(), line.indexOf("!")).trim() );
 					List<Integer> children = ontologyChild.get(parent);
 					if(children==null) {
 						children = new ArrayList<Integer>();
 						ontologyChild.put(parent, children);
 					}
 					children.add(id);
 				} else if (line.startsWith(PARSING_MARKER_RELATIONSHIP)) {
 					String relationship = line.substring(
 							PARSING_MARKER_RELATIONSHIP.length(), line
 									.indexOf(PARSING_MARKER_GO));
 					if (parentRelationshipTypes.contains(relationship)) {
 						int markerLength = PARSING_MARKER_RELATIONSHIP.length()
 								+ relationship.length()
 								+ PARSING_MARKER_GO.length();
 						Integer parent = Integer.valueOf(line.substring(
 								markerLength, line.indexOf("!")).trim());
 						List<Integer> children = ontologyChild.get(parent);
 						if (children == null) {
 							children = new ArrayList<Integer>();
 							ontologyChild.put(parent, children);
 						}
 						children.add(id);
 					}
 				} 
 				line = br.readLine();
 			}
 			br.close();
 			log.debug("term count "+GoAnalysisResult.termDetail.size());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			log.error("Ontology tree is not successfullly created due to FileNotException: "+e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			log.error("Ontology tree is not successfullly created due to IOException: "+e.getMessage());
 		}
 		staticOboFilename = goTermsOBOFile;
 	}
 
 	private static final Set<String> parentRelationshipTypes;
 	static {
 		parentRelationshipTypes = new TreeSet<String>();
 		parentRelationshipTypes.add("part_of");
 		parentRelationshipTypes.add("regulates");
 		parentRelationshipTypes.add("negatively_regulates");
 		parentRelationshipTypes.add("positively_regulates");
 	}
 	
 	private static final Set<String> namespace;
 	static {
 		namespace = new TreeSet<String>();
 		namespace.add("molecular_function");
 		namespace.add("biological_process");
 		namespace.add("cellular_component");
 	};
 	
 	static class GoTermDetail {
 		String name;
 		String def;
 		
 		GoTermDetail(String name, String def) {
 			this.name = name;
 			this.def = def;
 		}
 	}
 	
 	private static int countUnexpectedEntrezId = 0;
 
 	public static void parseAnnotation(String annotationFileName) {
 		term2Gene.clear();
 		geneDetails.clear();
 		
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(annotationFileName));
 			String line = br.readLine();
 			int count = 0;
 			countUnexpectedEntrezId = 0;
 			while(line!=null) {
 				while(line.startsWith("#"))
 					line = br.readLine();
 				line = line.substring(1, line.length()-2); // trimming the leading and trailing quotation mark
 				String[] fields = line.split("\",\"");
 				String geneSymbolField = fields[ANNOTATION_INDEX_GENE_SYMBOL];
 				String biologicalProcess = fields[ANNOTATION_INDEX_BIOLOGICAL_PROCESS];
 				String cellularComponent = fields[ANNOTATION_INDEX_CELLULAR_COMPONENT];
 				String molecularFunction = fields[ANNOTATION_INDEX_MOLECULAR_FUNCTION];
 				if (count > 0 && !geneSymbolField.equals("---") ) {
 					String[] geneSymbols = geneSymbolField.split("///");
 					String[] geneTitles = fields[ANNOTATION_INDEX_GENE_TITLE].trim().split("///");
 					String[] entrezIds = fields[ANNOTATION_INDEX_ENTREZ_ID].trim().split("///");
 					for(int i=0; i<geneSymbols.length; i++)
 						geneSymbols[i] = geneSymbols[i].trim(); 
 					parseOneNameSpace(biologicalProcess, geneSymbols, geneTitles, entrezIds);
 					parseOneNameSpace(cellularComponent, geneSymbols, geneTitles, entrezIds);
 					parseOneNameSpace(molecularFunction, geneSymbols, geneTitles, entrezIds);
 				}
 				count++;
 				
 				line = br.readLine();
 			}
 			br.close();
 			log.debug("total records in annotation file is "+count);
 			if(countUnexpectedEntrezId>0)
 				log.warn("total count of unexpected entrezId "+countUnexpectedEntrezId);
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			log.error("Annotation map is not successfullly created due to FileNotException: "+e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			log.error("Annotation map is not successfullly created due to IOException: "+e.getMessage());
 		}
 		staticAnnotationFileName = annotationFileName;
 	}
 
 	private final static int ANNOTATION_INDEX_ENTREZ_ID = 18;
 	private final static int ANNOTATION_INDEX_GENE_TITLE = 13;
 	private final static int ANNOTATION_INDEX_GENE_SYMBOL = 14;
 	private final static int ANNOTATION_INDEX_BIOLOGICAL_PROCESS = 30;
 	private final static int ANNOTATION_INDEX_CELLULAR_COMPONENT = 31;
 	private final static int ANNOTATION_INDEX_MOLECULAR_FUNCTION = 32;
 
 	private static void parseOneNameSpace(String namespaceAnnotation, String[] geneSymbols, String[] geneTitles, String[] entrezIds) {
 		for (Integer goId : getGoId(namespaceAnnotation)) {
 			Set<String> genes = term2Gene.get(goId);
 			if (genes == null) {
 				genes = new HashSet<String>();
 				term2Gene.put(goId, genes);
 			}
 			for(int i=0; i<geneSymbols.length; i++) {
 				String geneSymbol = geneSymbols[i];
 				String geneTitle = "";
 				if(i<geneTitles.length)
 					geneTitle = geneTitles[i];
 				int entrezId = 0;
 				if(i<entrezIds.length) {
 					try {
 						entrezId = Integer.parseInt(entrezIds[i].trim());
 					} catch (NumberFormatException e) {
 						log.debug("unexpected entrezId field "+entrezIds[i]);
 						countUnexpectedEntrezId++;
 						continue;
 					}
 				}
 				genes.add(geneSymbol);
 				if(!geneDetails.containsKey(geneSymbol) && entrezId!=0) {
 					GeneDetails details = new GeneDetails(geneTitle, entrezId);
 					geneDetails.put(geneSymbol, details);
 				}
 			}
 		}
 		
 	}
 
 	private static List<Integer> getGoId(String annotationField) {
 		List<Integer> ids = new ArrayList<Integer>();
 
 		String[] goTerm = annotationField.split("///");
 		for(String g: goTerm) {
 			String[] f = g.split("//");
 			try {
 				ids.add( Integer.valueOf(f[0].trim()) );
 			} catch (NumberFormatException e) {
 				// do nothing for non-number case, like "---"
 				// log.debug("non-number in annotation "+f[0]);
 			}
 		}
 		return ids;
 	}
 
 	private static Map<String, GeneDetails> geneDetails = new HashMap<String, GeneDetails>();
 	// this is necessary because there may be need to include more details
 	private static class GeneDetails {
 		public GeneDetails(String geneTitle, int entrezId) {
 			this.geneTitle = geneTitle;
 			this.entrezId = entrezId;
 		}
 
 		private String geneTitle;
 		private int entrezId;
 		
 		public int getEntrezId() {return entrezId; }
 		
 		public String toString() {return geneTitle; }
 	}
 	
 	// used by GoAnalysis to build up the result object
 	public void addChangedGenes(String gene) {
 		changedGenes.add(gene);
 	}
 
 	// used by GoAnalysis to build up the result object
 	public void addReferenceGenes(String gene) {
 		referenceGenes.add(gene);
 	}
 
 	// used by GoAnalysis to build up the result object
 	public void addResultRow(int termId, String termName, String namespace, double p, double adjustedP, int popCount, int studyCount) {
 		ResultRow row = new ResultRow(termName, namespace, p, adjustedP, popCount, studyCount);
 		result.put(termId, row);
 	}
 	
 	public String getRowAsString(int goId) {
 		GoAnalysisResult.ResultRow row = getRow(goId);
 		if(row==null)return null;
 		
 		return row.name+" ("+row.studyCount+"/"+row.popCount+") ("+row.pAdjusted+")";
 	}
 	
 	public Set<String> getChangedGenes() {
 		return changedGenes;
 	}
 
 	public Set<String> getReferenceGenes() {
 		return referenceGenes;
 	}
 	
 	public Object[][] getResultAsArray() {
 		int rowCount = getCount();
 		List<Object[]> rows = new ArrayList<Object[]>();
 		
 		for(Integer goId: result.keySet()) {
 			GoAnalysisResult.ResultRow resultRow = getRow(goId);
 			Object[] array = new Object[COLUMN_COUNT];
 			array[TABLE_COLUMN_INDEX_GO_ID] = goId;
 			array[TABLE_COLUMN_INDEX_GO_TERM_NAME] = resultRow.name;
 			array[TABLE_COLUMN_INDEX_NAMESPACE] = resultRow.namespace;
 			array[TABLE_COLUMN_INDEX_P] = resultRow.p;
 			array[TABLE_COLUMN_INDEX_ADJUSTED_P] = resultRow.pAdjusted;
 			array[TABLE_COLUMN_INDEX_POP_COUNT] = resultRow.popCount;
 			array[TABLE_COLUMN_INDEX_STUDY_COUNT] = resultRow.studyCount;
 			rows.add(array);
 		}
 		log.debug("total rows: "+rowCount);
 		Collections.sort(rows, new AdjustedPComparator());
 		int row = 0;
 		Object[][] data = new Object[rowCount][COLUMN_COUNT];
 		for(Object[] array: rows) {
 			data[row] = array;
 			row++;
 		}
 		return data;
 	}
 
 	// only used for getRwoAsArray
 	private static final int COLUMN_COUNT = 7;
 	private static final int TABLE_COLUMN_INDEX_GO_ID = 0;
 	private static final int TABLE_COLUMN_INDEX_GO_TERM_NAME = 1;
 	private static final int TABLE_COLUMN_INDEX_NAMESPACE = 2;
 	private static final int TABLE_COLUMN_INDEX_P = 3;
 	private static final int TABLE_COLUMN_INDEX_ADJUSTED_P = 4;
 	private static final int TABLE_COLUMN_INDEX_POP_COUNT = 5;
 	private static final int TABLE_COLUMN_INDEX_STUDY_COUNT = 6;
 	
 	private static class AdjustedPComparator implements Comparator<Object[]> {
 
 		public int compare(Object[] o1, Object[] o2) {
 			Double d1 = (Double) o1[TABLE_COLUMN_INDEX_ADJUSTED_P];
 			Double d2 = (Double) o2[TABLE_COLUMN_INDEX_ADJUSTED_P];
 			if(d1<d2)return -1;
 			else if(d1>d2)return 1;
 			else return 0;
 		}
 	}
 
 	// implement saving workspace
 	private String oboFilename = null;
 	private String annotationFileName = null;
 	private static String staticOboFilename = null;
 	private static String staticAnnotationFileName = null;
 	
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		oboFilename = staticOboFilename;
 		annotationFileName = staticAnnotationFileName;
 		
 		out.defaultWriteObject();
 	}
      
 	private void readObject(ObjectInputStream in) throws IOException,
 			ClassNotFoundException {
 		in.defaultReadObject();
 		parseOboFile(oboFilename);
 		parseAnnotation(annotationFileName);
 	}
 	
 }
