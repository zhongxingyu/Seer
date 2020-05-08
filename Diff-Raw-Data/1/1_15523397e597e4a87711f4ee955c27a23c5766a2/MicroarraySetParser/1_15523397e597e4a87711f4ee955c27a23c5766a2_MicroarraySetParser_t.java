 package org.geworkbench.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.annotation.CSAnnotationContext;
 import org.geworkbench.bison.annotation.CSAnnotationContextManager;
 import org.geworkbench.bison.annotation.DSAnnotationContext;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSRangeMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
 import org.geworkbench.bison.util.Range;
 import org.geworkbench.util.AffyAnnotationUtil;
 
 /**
  * 
  * Parser of .exp file.
  * 
  * @author zji
  * @version $Id$
  * 
  */
 // TODO The exceptional cases are not handled very well by this parser. Returning null does not provide much information.
 public class MicroarraySetParser {
 	private static Log log = LogFactory.getLog(MicroarraySetParser.class);
 
 	/** Parse without associate to annotation file. */
 	// FIXME this method is only used by ConsensusClustering, which does not handle either InputFileFormatException or returned null.
 	public DSMicroarraySet parseCSMicroarraySet(File file) {
 
 		DSMicroarraySet microarraySet = new CSMicroarraySet();
 
 		microarraySet.setFile( file ); // this seems only used by "View in Editor"
 		microarraySet.setLabel(file.getName());
 
 		AnnotationParser.setCurrentDataSet(microarraySet);
 
 		try {
 			if (!readFile(file))
 				return null;
 		} catch (InputFileFormatException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		populateDataset(microarraySet);
 
 		return microarraySet;
 	}
 	
 	/** Parse existing microarraySet without associate to annotation file. 
 	 * @throws InputFileFormatException */
 	public void parseExistingCSMicroarraySet(File file, DSMicroarraySet microarraySet) throws InputFileFormatException {
 		if(microarraySet == null) return;
 
 		microarraySet.setFile( file ); // this seems only used by "View in Editor"
 		microarraySet.setLabel(file.getName());
 
 		AnnotationParser.setCurrentDataSet(microarraySet);
 
 		if (!readFile(file))
 			return;
 
 		populateDataset(microarraySet);
 	}
 
 	/** Parse when invoked from ExpressionFileFormat. */
 	DSMicroarraySet parseCSMicroarraySet(File file, String compatibilityLabel) throws InputFileFormatException {
 		DSMicroarraySet microarraySet = new CSMicroarraySet();
 		if (compatibilityLabel != null) {
 			microarraySet.setCompatibilityLabel(compatibilityLabel);
 		} else {			 
 			String chiptype = AffyAnnotationUtil
 					.matchAffyAnnotationFile(microarraySet);
 			if (chiptype == null) { // this is never null
 				log.error("annotation returned as null");
 			}
 			microarraySet.setCompatibilityLabel(chiptype);
 		}
 
 		microarraySet.setFile( file ); // this seems only used by "View in Editor"
 		microarraySet.setLabel(file.getName());
 
 		AnnotationParser.setCurrentDataSet(microarraySet);
 
 		if (!readFile(file)) {
 			throw new InputFileFormatException();
 		}
 
 		populateDataset(microarraySet);
 
 		return microarraySet;
 	}
 
 	transient boolean pValueExists = false;
 	private boolean readFile(File file) throws InputFileFormatException {
 		markerNumber = 0;
 		
 		pValueExists = false;
 
 		String line = null;
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			markerValues = new ArrayList<DSMarkerValue[]>();
 			while ((line = reader.readLine()) != null) {
 				if (!line.trim().equalsIgnoreCase("")) {
 
 					parseLine(line);
 
 				}
 			}
 		} catch (IOException ioe) {
 			log.error("Error while parsing line: " + line);
 			ioe.printStackTrace();
 			return false;
 		} finally {
 			try {
 				if(reader!=null)
 					reader.close();
 			} catch (IOException e) {
 				// nothing further necessary
 			}
 		}
 		return true;
 	}
 
 	private void populateDataset(DSMicroarraySet microarraySet) {
 		// just to make it is clear it is final here
 		final int markerCount = markerNumber;
 		microarraySet.initializeMarkerVector(markerCount); // only way to set marker
 													// count
 		for (int i = 0; i < arrayNames.size(); i++) {
 			microarraySet.add(i, (DSMicroarray) new CSMicroarray(i,
 					markerCount, arrayNames.get(i),
 					DSMicroarraySet.expPvalueType));
 		}
 
 		CSAnnotationContextManager manager = CSAnnotationContextManager
 				.getInstance();
 		for (String phLabel : arrayInfo.keySet()) {
 			DSAnnotationContext<DSMicroarray> context = manager.getContext(
 					microarraySet, phLabel);
 			CSAnnotationContext.initializePhenotypeContext(context);
 			String[] labels = arrayInfo.get(phLabel);
 			for (int arrayIndex = 0; arrayIndex < labels.length; arrayIndex++) {
 				if (labels[arrayIndex] == null
 						|| labels[arrayIndex].length() == 0)
 					continue;
 				
 				if (labels[arrayIndex].indexOf("|") > -1) {
 					for (String tok : labels[arrayIndex].split("\\|")) {
 						context.labelItem(microarraySet.get(arrayIndex), tok);
 					}
 				} else {
 					context.labelItem(microarraySet.get(arrayIndex),
 							labels[arrayIndex]);
 				}
 			}
 		}
 
 		for (int markerIndex = 0; markerIndex < markerCount; markerIndex++) {
 			microarraySet.getMarkers().set(markerIndex,
 					markers.get(markerIndex));
 		}
 		if(microarraySet instanceof CSMicroarraySet) {
 			((CSMicroarraySet)microarraySet).getMarkers().correctMaps();
 		}
 		microarraySet.sortMarkers(markerCount);
 
 		int markerIndex = 0;
 		for (DSMarkerValue[] markerValue : markerValues) {
 			for (int arrayIndex = 0; arrayIndex < arrayNames.size(); arrayIndex++) {
 				// missing data should not be set as null
 				if(markerValue[arrayIndex]==null) continue;
 				
 				microarraySet.get(arrayIndex).setMarkerValue(
 						microarraySet.getNewMarkerOrder()[markerIndex],
 						markerValue[arrayIndex]);
 			}
 			markerIndex++;
 		}
 	}
 
 	private transient int markerNumber = 0;
 
 	private transient List<String> arrayNames = new ArrayList<String>();
 	// use LinkedHashMap so to maintain the order
 	private transient Map<String, String[]> arrayInfo = new LinkedHashMap<String, String[]>();
 	private transient List<DSGeneMarker> markers = new ArrayList<DSGeneMarker>();
 	private transient List<DSMarkerValue[]> markerValues = null;
 
 	private DSMarkerValue[] parseValue(String[] fields) throws InputFileFormatException {
 
 		// This handles individual gene lines with (value, pvalue) pairs
 		// separated by tabs
 		if(markerNumber==0) { // only do this for the first line
 			if(fields.length-2 == arrayNames.size()) {
 				pValueExists = false;
 			} else if(fields.length-2 == arrayNames.size()*2) {
 				pValueExists = true;
 			} else {
 				throw new InputFileFormatException("Field number (first line) is incorrect.");
 			}
 		} else {
 			if(fields.length-2 == arrayNames.size() ) {
 				if(pValueExists)
 					throw new InputFileFormatException("Value field number is not double the header field number for the case of p-value existing.");
 			} else if(fields.length-2 == arrayNames.size()*2 ) {
 				if(!pValueExists)
 					throw new InputFileFormatException("Value field number is double the header field number for the case of no p-value.");
 			} else {
 				throw new InputFileFormatException("Field number is incorrect.");
 			}
 		}
 		boolean pValueExists = (fields.length-2 > arrayNames.size());
 		DSMarkerValue[] values = new DSMarkerValue[arrayNames.size()];
 		int arrayIndex = 0;
 		int step = 1;
 		if(pValueExists) step = 2;
 		for(int i=2; i<fields.length; i += step) {
 
 			String value = fields[i];
 			String status; // p-value or letter status
 			if (pValueExists) {
 				status = fields[i+1];
 			} else {
 				// If no p-value is present, assume that the detection
 				// call is "Present"
 				status = 0.000001 + "";
 			}
 
 			DSMutableMarkerValue markerValue = createMarkerValue(value, status);
 			// markerValue does not really need to be mutable. it is
 			// DSRangeMarker's mistake
 			((DSRangeMarker) markers.get(markerNumber)).updateRange(markerValue);
 			values[arrayIndex++] = markerValue;
 		}
 		return values;
 	}
 
 	/**
 	 * initialize microarrays; initializer markers (probe sets); prompt for affy
 	 * annotation.
 	 * @throws InputFileFormatException 
 	 */
 	private void parseLine(String line) throws InputFileFormatException {
 
 		if (line.charAt(0) == '#') {
 			return;
 		}
 
 		int startindx = line.indexOf('\t');
 		if (startindx <= 0)
 			return;
 
 		String[] fields = line.split( "\t", -1 );
 
 		if (line.substring(0, 6).equalsIgnoreCase("AffyID")) {
 			for(int i=2; i<fields.length; i++) {
				if(fields[i].trim().length()==0) break;
 				arrayNames.add(fields[i]);
 			}
 		} else if (line.substring(0, 11).equalsIgnoreCase("Description")) {
 			// This handles all the phenotype definition lines
 			String[] f = line.split("\t", -1);
 			String[] labels = new String[(arrayNames.size())];
 			for(int i=0; i<Math.min(labels.length, f.length-2); i++) {
 				labels[i] = f[i+2];
 			}
 			String phLabel = new String(f[1]);
 			arrayInfo.put(phLabel, labels);
 		} else if (line.charAt(0) != '\t') {
 
 			CSExpressionMarker marker = new CSExpressionMarker(markerNumber);
 			marker.setLabel(fields[0]);
 			// set the annotation field of current marker
 			marker.setDescription(fields[1]);
 			marker.getUnigene().set(fields[0]);
 			
 			/* this is correct because CSExpressionMarker equal is based on label only */
 			if(markers.contains(marker)) {
 				throw new InputFileFormatException("duplicate probeset names");
 			}
 
 			String[] entrezIds = AnnotationParser.getInfo(fields[0],
 					AnnotationParser.LOCUSLINK);
 			if ((entrezIds != null) && (!entrezIds[0].trim().equals(""))) {
 				try {
 					marker.setGeneId(Integer.parseInt(entrezIds[0].trim()));
 				} catch (NumberFormatException e) {
 					log.debug("Invalid locus link for gene " + markerNumber);
 				}
 			}
 
 			String[] geneNames = AnnotationParser.getInfo(fields[0],
 					AnnotationParser.ABREV);
 			if (geneNames != null) {
 				marker.setGeneName(geneNames[0].trim());
 			}
 
 			String[] annotations = AnnotationParser.getInfo(fields[0],
 					AnnotationParser.DESCRIPTION);
 			if (annotations != null) {
 				marker.setAnnotation(annotations[0].trim());
 			}
 
 			markers.add(marker);
 
 			markerValues.add(parseValue(fields));
 
 			markerNumber++;
 		}
 	}
 
 	private DSMutableMarkerValue createMarkerValue(String value, String status) {
 		CSMarkerValue markerValue = new CSExpressionMarkerValue(0);
 
 		// support status a in letter and extra text before the actual value
 		if (Character.isLetter(status.charAt(0))) {
 			String[] parseableValue = value.split(":");
 			value = parseableValue[parseableValue.length - 1];
 		}
 
 		try {
 			double v = Double.parseDouble(value);
 			Range range = ((DSRangeMarker) markers.get(markerNumber))
 					.getRange();
 
 			markerValue.setValue(v);
 			range.max = Math.max(range.max, v);
 			range.min = Math.min(range.min, v);
 		} catch (NumberFormatException e) {
 			markerValue.setValue(0.0);
 			markerValue.setMissing(true);
 			return markerValue;
 		}
 
 		char c = status.charAt(0);
 		if (Character.isLetter(c)) {
 			try {
 				if (Character.isLowerCase(c)) {
 					markerValue.mask();
 				}
 				switch (Character.toUpperCase(c)) {
 				case 'P':
 					markerValue.setPresent();
 					break;
 				case 'A':
 					markerValue.setAbsent();
 					break;
 				case 'M':
 					markerValue.setMarginal();
 					break;
 				default:
 					markerValue.setMissing(true);
 					break;
 				}
 			} catch (NumberFormatException e) {
 				markerValue.setValue(0.0);
 				markerValue.setMissing(true);
 			}
 		} else {
 			try {
 				double p = Double.parseDouble(status);
 				markerValue.setConfidence(p);
 			} catch (NumberFormatException e) {
 				markerValue.setValue(0.0);
 				markerValue.setMissing(true);
 			}
 		}
 		return markerValue;
 	}
 
 }
