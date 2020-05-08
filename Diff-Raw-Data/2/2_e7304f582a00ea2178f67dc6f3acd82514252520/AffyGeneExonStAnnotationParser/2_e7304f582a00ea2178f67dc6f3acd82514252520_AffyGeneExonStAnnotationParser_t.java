 /**
  * 
  */
 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
  
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory; 
 
  
 /**
  * Actual parser of affy annotation.
  * 
  * This used to be in class AnnotationParser, whose main role is no longer
  * parsing but the name stuck.
  * 
  * @author zji
  * @version $Id: AffyAnnotationParser.java 9425 2012-05-04 18:22:56Z zji $
  * 
  */
 public class AffyGeneExonStAnnotationParser extends AffyAnnotationParser {
 
 	static Log log = LogFactory.getLog(AffyGeneExonStAnnotationParser.class);
 	
 	// at this time, the field names in AnnotationParser are those actually used
 	// the field names here are to be used in the future
 	private static final String PROBESET_ID = "probeset_id";
 	private static final String UNIGENE = "unigene"; // subfield a or b.
 
 	private static final String GO_BIOLOGICAL_PROCESS = "GO_biological_process";
 
 	private static final String GO_CELLULAR_COMPONENT = "GO_cellular_component";
 
 	private static final String GO_MOLECULAR_FUNCTION = "GO_molecular_function";
 
 	// subfield b(genesymbol), c(genetitle), e(entrezid)
 	private static final String GENE_ASSIGNMENT = "gene_assignment";
 
 	private static final String SWISSPROT = "swissprot"; // swissprot
 
 	private static final String PATHWAY = "Pathway";
 
 	// "RefSeq Transcript ID" = mrna_assignment, subfield a, where subfield b =
 	// "RefSeq"
 	private static final String MRNA_ASSIGNMENT = "mrna_assignment";
 
 	private static final String MAIN_DELIMITER = "///";
 
 	private static final String SUB_DELIMITER = "//";
 
 	// columns read into geWorkbench
 	// probe id must be first column read in, and the rest of the columns must
 	// follow the same order
 	// as the columns in the annotation file.
 	private static final String[] labels = {
 			PROBESET_ID, // probe id must be the first item in this list
 			UNIGENE, GENE_ASSIGNMENT, MRNA_ASSIGNMENT, SWISSPROT, PATHWAY,
 			GO_BIOLOGICAL_PROCESS, GO_CELLULAR_COMPONENT, GO_MOLECULAR_FUNCTION };
 
 	AffyGeneExonStAnnotationParser() {
 		this.annotationFileType = AnnotationParser.AFFY_GENE_EXON_10_ST;
 
 	}
  
 	AnnotationFields parseOneLine() {
 		 
 		affyId = parser.getValueByLabel(labels[0]);
 		if (affyId == null)
 			return null;
 		affyId = affyId.trim();
 		AnnotationFields fields = new AnnotationFields();
 		for (int i = 1; i < labels.length; i++) {
 			String label = labels[i];
 			String val = parser.getValueByLabel(label);			 
 			if (label.equals(GO_BIOLOGICAL_PROCESS)
 					|| label.equals(GO_CELLULAR_COMPONENT)
 					|| label.equals(GO_MOLECULAR_FUNCTION)) {				 
 				val = rebuildGoString(val);				 
 			}
 			if (label.equals(GENE_ASSIGNMENT))
 				fields = parseGeneAssignment(val, fields);
 			else if (label.equals(SWISSPROT))
 				fields = parseSwissprot(val, fields);
 			else if (label.equals(AnnotationParser.DESCRIPTION))
 				fields.setDescription(val);
 			else if (label.equals(GO_MOLECULAR_FUNCTION))
 				fields.setMolecularFunction(val);
 			else if (label.equals(GO_CELLULAR_COMPONENT))
 				fields.setCellularComponent(val);
 			else if (label.equals(GO_BIOLOGICAL_PROCESS))
 				fields.setBiologicalProcess(val);
 			else if (label.equals(UNIGENE))
 				fields = parseUnigene(val, fields);
 			else if (label.equals(MRNA_ASSIGNMENT))
 				fields = parseMrnaAssignment(val, fields);
 		}
 
 		return fields;
 	}   
 	
 	
 
 	private String rebuildGoString(String goString) {
 		StringBuilder sb = new StringBuilder();
 		if (goString != null && !goString.equals("---")) {
 			String[] subStrs = goString.split(MAIN_DELIMITER);
 			sb.append(subStrs[0].split("GO:")[1].trim());
 			for (int i = 0; i < subStrs.length; i++) {
 
 				if (subStrs[i].split("GO:").length < 2
 						|| subStrs[i].contains("WARNING: THIS FIELD TRUNCATED"))
					log.info("This string is skipped: " +  subStrs[i]);
 				else
 					sb.append(MAIN_DELIMITER
 							+ subStrs[i].split("GO:")[1].trim());
 			}
 		}
 
 		if (sb.length() > 0)
 			return sb.toString();
 		else
 			return "---";
 	}
 
 	private AnnotationFields parseGeneAssignment(String val,
 			AnnotationFields fields) {
 		String geneSymbol = "---";
 		String description = "---";
 		String entrezId = "---";
 
 		List<String> geneList = new ArrayList<String>();
 
 		if (val != null && !val.trim().equals("") && !val.equals("---")) {
 			String[] subStrs = val.split(MAIN_DELIMITER);
 			for (int i = 0; i < subStrs.length; i++) {
 				String[] subfields = subStrs[i].split(SUB_DELIMITER);
 				if (subfields.length < 5 || subStrs[i].contains("WARNING: THIS FIELD TRUNCATED"))
 					log.info("This string is skipped: " +  subStrs[i]);
 				else {
 					if (i == 0) {
 						geneSymbol = subfields[1].trim();
 						description = subfields[2].trim();
 						entrezId = subfields[4].trim();
 						geneList.add(geneSymbol);
 					} else {
 						if (!geneList.contains(subfields[1].trim())) {
 							geneSymbol += MAIN_DELIMITER + subfields[1].trim();
 							description += MAIN_DELIMITER + subfields[2].trim();
 							entrezId += MAIN_DELIMITER + subfields[4].trim();
 							geneList.add(subfields[1].trim());
 						}
 					}
 				}
 			}
 		}
 
 		fields.setLocusLink(entrezId);
 		fields.setGeneSymbol(geneSymbol);
 		fields.setDescription(description);
 
 		return fields;
 	}
 
 	private AnnotationFields parseSwissprot(String val, AnnotationFields fields) {
 		String swissprot = "---";
 
 		if (val != null && !val.trim().equals("") && !val.equals("---")) {
 			String[] subStrs = val.split(MAIN_DELIMITER);
 			for (int i = 0; i < subStrs.length; i++) {
 				String[] subfields = subStrs[i].split(SUB_DELIMITER);
 				if (subfields.length < 2 || subStrs[i].contains("WARNING: THIS FIELD TRUNCATED"))
 					log.info("This string is skipped: " +  subStrs[i]);
 				else {
 					if (i == 0)
 						swissprot = subfields[1];
 					else
 						swissprot += MAIN_DELIMITER + subfields[1];
 				}
 
 			}
 		}
 
 		fields.setSwissProt(swissprot);
 
 		return fields;
 	}
 
 	private AnnotationFields parseUnigene(String val, AnnotationFields fields) {
 		String unigene = "---";
 
 		if (val != null && !val.trim().equals("") && !val.equals("---")) {
 			String[] subStrs = val.split(MAIN_DELIMITER);
 			for (int i = 0; i < subStrs.length; i++) {
 				String[] subfields = subStrs[i].split(SUB_DELIMITER);
 				if (subfields.length < 2 || subStrs[i].contains("WARNING: THIS FIELD TRUNCATED"))
 					log.info("This string is skipped: " +  subStrs[i]);
 				else {
 					if (i == 0)
 						unigene = subfields[1];
 					else
 						unigene += MAIN_DELIMITER + subfields[1];
 				}
 
 			}
 		}
 
 		fields.setUniGene(unigene);
 
 		return fields;
 	}
 
 	private AnnotationFields parseMrnaAssignment(String val,
 			AnnotationFields fields) {
 		String refSeq = "---";
 
 		if (val != null && !val.trim().equals("") && !val.equals("---")) {
 			String[] subStrs = val.split(MAIN_DELIMITER);
 			boolean needDelomiter = false;
 			for (int i = 0; i < subStrs.length; i++) {
 				String[] subfields = subStrs[i].split(SUB_DELIMITER);
 				if (subfields.length < 2 || subStrs[i].contains("WARNING: THIS FIELD TRUNCATED"))
 					log.info("This string is skipped: " +  subStrs[i]);
 				else {
 					if (subfields[1].trim().equalsIgnoreCase("RefSeq")) {
 						if (!needDelomiter) {
 							refSeq = subfields[0];
 							needDelomiter = true;
 						} else
 							refSeq += MAIN_DELIMITER + subfields[0];
 					}
 				}
 			}
 		}
 
 		fields.setRefSeq(refSeq);
 
 		return fields;
 	}
 	 
 
 }
