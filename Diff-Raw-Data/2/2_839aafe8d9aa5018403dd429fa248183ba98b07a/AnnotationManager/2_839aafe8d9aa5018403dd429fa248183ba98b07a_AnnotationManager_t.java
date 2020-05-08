 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 // this class is created to support the go term detail information from annotation
 // this pattern is superior to AnnotationParser
 // I think the 'regular' annotation information managed in AnnotationParser
 // should be implemented this way
 public class AnnotationManager {
 	static Log log = LogFactory.getLog(AnnotationManager.class);
 
 	static private WeakHashMap<DSMicroarraySet<DSMicroarray>, HashMap<Integer, Set<String>>> goTerm2GeneMap = new WeakHashMap<DSMicroarraySet<DSMicroarray>, HashMap<Integer, Set<String>>>();
 	static private WeakHashMap<DSMicroarraySet<DSMicroarray>, Map<String, GeneDetails>> geneDetailMap = new WeakHashMap<DSMicroarraySet<DSMicroarray>, Map<String, GeneDetails>>();
 
 	static public HashMap<Integer, Set<String>> getGoTermToGene(
 			final DSMicroarraySet<DSMicroarray> dataset) {
 		HashMap<Integer, Set<String>> goTermToGene = goTerm2GeneMap
 				.get(dataset);
 		if (goTermToGene != null) {
 			return goTermToGene;
 		}
 
 		goTermToGene = new HashMap<Integer, Set<String>>();
 
 		// first time querying on this dataset, there is no detail.
 		// parse the detail
 		for (DSGeneMarker marker : dataset.getMarkers()) {
 
 			String[] geneSymbols = marker.getShortNames();
 			String markerLabel = marker.getLabel();
 
 			parseOneGoTermField(goTermToGene, dataset,
 					AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS,
 					markerLabel, geneSymbols);
 			parseOneGoTermField(goTermToGene, dataset,
 					AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT,
 					markerLabel, geneSymbols);
 			parseOneGoTermField(goTermToGene, dataset,
 					AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION,
 					markerLabel, geneSymbols);
 		}
 
 		goTerm2GeneMap.put(dataset, goTermToGene);
 		return goTermToGene;
 	}
 
 	static public Map<String, GeneDetails> getGeneDetail(
 			final DSMicroarraySet<DSMicroarray> dataset) {
 		Map<String, GeneDetails> detail = geneDetailMap.get(dataset);
 		if (detail != null) {
 			return detail;
 		}
 
 		detail = new HashMap<String, GeneDetails>();
 		int countUnexpectedEntrezId = 0;
 
 		// first time querying on this dataset, there is no detail.
 		// parse the detail
 		for (DSGeneMarker marker : dataset.getMarkers()) {
 
 			String[] geneSymbols = marker.getShortNames();
 			String markerLabel = marker.getLabel();
 
 			String[] geneTitles = AnnotationParser.getInfo(dataset,
 					markerLabel, AnnotationParser.DESCRIPTION);
 			String[] entrezIds = AnnotationParser.getInfo(dataset, markerLabel,
 					AnnotationParser.LOCUSLINK);
 			for (int i = 0; i < geneSymbols.length; i++) {
 				String geneTitle = "";
 				if (i < geneTitles.length)
 					geneTitle = geneTitles[i];
 				int entrezId = 0;
 				if (i < entrezIds.length) {
 					try {
 						entrezId = Integer.parseInt(entrezIds[i].trim());
 					} catch (NumberFormatException e) {
 						log.debug("unexpected entrezId field " + entrezIds[i]);
 						countUnexpectedEntrezId++;
 						continue;
 					}
 				}
 				if (!detail.containsKey(geneSymbols[i]) && entrezId != 0) {
 					detail.put(geneSymbols[i], new GeneDetails(geneTitle,
 							entrezId));
 				}
 			}
 		}
 		if (countUnexpectedEntrezId > 0)
 			log.warn("total count of unexpected entrezId "
 					+ countUnexpectedEntrezId);
 
 		geneDetailMap.put(dataset, detail);
 		return detail;
 	}
 
 	private static void parseOneGoTermField(HashMap<Integer, Set<String>> map,
 			DSMicroarraySet<DSMicroarray> dataset, String fieldName,
 			String markerLabel, String[] geneSymbols) {
 		String[] goTerms = AnnotationParser.getInfo(dataset, markerLabel,
 				fieldName);
 		if (goTerms != null) {
 			for (String oneGoTerm : goTerms) {
 				if (oneGoTerm.trim().equals("---"))
 					continue;
 
 				String[] fields = oneGoTerm.split("//");
 				int goTermId = Integer.parseInt(fields[0].trim());
 				Set<String> genes = map.get(goTermId);
 				if (genes == null) {
 					genes = new HashSet<String>();
 					map.put(goTermId, genes);
 				}
 				for (String s : geneSymbols) {
					if(s.endsWith("---")) continue;
					
 					if (!genes.contains(s))
 						genes.add(s);
 				}
 			}
 		}
 	}
 
 	public static Set<String> getAnnotatedGenes(
 			DSMicroarraySet<DSMicroarray> dataset, int goTermId) {
 		return getGoTermToGene(dataset).get(goTermId);
 	}
 
 	public static String getGeneDetail(DSMicroarraySet<DSMicroarray> dataset,
 			String gene) {
 		GeneDetails detail = getGeneDetail(dataset).get(gene);
 		if (detail == null)
 			return "";
 		return detail.toString();
 	}
 
 	public static int getEntrezId(DSMicroarraySet<DSMicroarray> dataset,
 			String gene) {
 		return getGeneDetail(dataset).get(gene).getEntrezId();
 	}
 
 	// this is necessary because there may be need to include more details
 	private static class GeneDetails {
 		public GeneDetails(String geneTitle, int entrezId) {
 			this.geneTitle = geneTitle;
 			this.entrezId = entrezId;
 		}
 
 		private String geneTitle;
 		private int entrezId;
 
 		public int getEntrezId() {
 			return entrezId;
 		}
 
 		public String toString() {
 			return geneTitle;
 		}
 	}
 
 }
