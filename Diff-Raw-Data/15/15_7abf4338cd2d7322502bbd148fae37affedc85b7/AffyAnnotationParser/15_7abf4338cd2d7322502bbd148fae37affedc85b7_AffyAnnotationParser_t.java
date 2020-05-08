 /**
  * 
  */
 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 
 /**
  * Actual parser of affy annotation.
  * 
  * This used to be in class AnnotationParser, whose main role is no longer parsing but the name stuck.
  * 
  * @author zji
  * @version $Id$
  *
  */
 public class AffyAnnotationParser {
 
 	// at this time, the field names in AnnotationParser are those actually used
 	// the field names here are to be used in the future
 	private static final String PROBE_SET_ID = "Probe Set ID";
 	private static final String SCIENTIFIC_NAME = "Species Scientific Name";
 	private static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";
 	private static final String GENOME_VERSION = "Genome Version";
 	private static final String ALIGNMENT = "Alignments";
 	private static final String PATHWAY = "Pathway"; // pathway
 	private static final String TRANSCRIPT = "Transcript Assignments";
 
 	// columns read into geWorkbench
 	// probe id must be first column read in, and the rest of the columns must
 	// follow the same order
 	// as the columns in the annotation file.
 	private static final String[] labels = {
 			PROBE_SET_ID, // probe id must be the first item in this list
 			SCIENTIFIC_NAME, UNIGENE_CLUSTER, AnnotationParser.UNIGENE,
 			GENOME_VERSION, ALIGNMENT, AnnotationParser.DESCRIPTION,
 			AnnotationParser.GENE_SYMBOL, AnnotationParser.LOCUSLINK,
 			AnnotationParser.SWISSPROT, AnnotationParser.REFSEQ,
 			AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS,
 			AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT,
 			AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION, PATHWAY,
 			TRANSCRIPT };
 	
 	private final File file;
 	
 	AffyAnnotationParser(final File file) {
 		this.file = file;
 	}
 	
 	// this supports the complete unfinished parsing
 	private transient LabeledCSVParser parser;
 	private transient String affyId;
 	
 	Map<String, AnnotationFields> parse(boolean ignoreDuplicate) {
 		Map<String, AnnotationFields> markerAnnotation = new HashMap<String, AnnotationFields>();
 
 		BufferedInputStream bis = null;
 		try {
 			bis = new BufferedInputStream(new FileInputStream(file));
 
 			CSVParser cvsParser = new CSVParser(bis);
 
 			cvsParser.setCommentStart("#;!");
 
 			parser = new LabeledCSVParser(cvsParser);
 
 			while (parser.getLine() != null) {
 				AnnotationFields fields = parseOneLine();
 				if (!ignoreDuplicate && markerAnnotation.containsKey(affyId)) {
 					String[] options = { "Skip duplicate",
 							"Skip all duplicates", "Cancel", };
 					int code = JOptionPane
 							.showOptionDialog(
 									null,
 									"Duplicate entry. Probe Set ID="
 											+ affyId
 											+ ".\n"
 											+ "Skip duplicate - will ignore this entry\n"
 											+ "Skip all duplicates - will ignore all duplicate entries.\n"
 											+ "Cancel - will cancel the annotation file processing.",
 									"Duplicate entry in annotation file",
 									0, JOptionPane.QUESTION_MESSAGE, null,
 									options, "Proceed");
 					if (code == 0) {  // only ignore this duplicate
 						continue;
 					} else if (code == 1) {  // ignore all duplicates
 						ignoreDuplicate = true;
 						continue;
 					} if (code == 2) {
 						return null;
 					}
 				} else {
 					markerAnnotation.put(affyId, fields);
 				}
 			}
 			// all fine.
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			try {
 				bis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return markerAnnotation;
 	}
 	
 	private AnnotationFields parseOneLine() {
 		affyId = parser.getValueByLabel(labels[0]);
 		if(affyId==null)
 			return null;
 		affyId = affyId.trim();
 		AnnotationFields fields = new AnnotationFields();
 		for (int i = 1; i < labels.length; i++) {
 			String label = labels[i];
 			String val = parser.getValueByLabel(label);
 			if (label.equals(AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS)
 					|| label.equals(AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT)
 					|| label.equals(AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
 				// get rid of leading 0's
				while (val!=null && val.startsWith("0") && (val.length() > 0)) {
 					val = val.substring(1);
 				}
 			}
 			if (label.equals(AnnotationParser.GENE_SYMBOL))
 				fields.setGeneSymbol(val);
 			else if (label.equals(AnnotationParser.LOCUSLINK))
 				fields.setLocusLink(val);
 			else if (label.equals(AnnotationParser.SWISSPROT))
 				fields.setSwissProt(val);
 			else if (label.equals(AnnotationParser.DESCRIPTION))
 				fields.setDescription(val);
 			else if (label.equals(AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION))
 				fields.setMolecularFunction(val);
 			else if (label.equals(AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT))
 				fields.setCellularComponent(val);
 			else if (label.equals(AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS))
 				fields.setBiologicalProcess(val);
 			else if (label.equals(AnnotationParser.UNIGENE))
 				fields.setUniGene(val);
 			else if (label.equals(AnnotationParser.REFSEQ))
 				fields.setRefSeq(val);
 		}
 
 		return fields;
 	}
 
 }
