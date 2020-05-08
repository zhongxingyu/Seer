 package org.geworkbench.parsers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
 import org.geworkbench.util.AffyAnnotationUtil;
 
 /**
  * @author Nikhil
  * @version $Id$
  */
 public class SOFTSeriesParser {
 
 	static Log log = LogFactory.getLog(SOFTFileFormat.class);
 	private static final String commentSign1 = "#";
 	private static final String commentSign2 = "!";
 	private static final String commentSign3 = "^";
 
 	static final char ABSENT = 'A';
 	static final char PRESENT = 'P';
 	static final char MARGINAL = 'M';
 	static final char UNDEFINED = '\0';
 
 	transient private String errorMessage = null;
 
 	private String choosePlatform(File file) throws InterruptedIOException {
 
 		BufferedReader br = null;
 
 		List<String> p = null;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			String line = br.readLine();
 			if (line == null) {
 				errorMessage = "no content in file";
 				return null;
 			}
 			while (line != null) {
 				if (line.startsWith("!Series_platform_id")) {
 					if (p == null) {
 						p = new ArrayList<String>();
 					}
 					String platformName = line.split("\\s")[2];
 					p.add(platformName);
 				} else if (p != null) {
 					// finish processing platform id
 					break;
 				}
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			errorMessage = e.getMessage();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			errorMessage = e.getMessage();
 			return null;
 		} finally {
 			try {
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (p == null) {
 			errorMessage = "invalid content in file";
 			return null;
 		} else if (p.size() == 1) {
 			return p.get(0);
 		} else {
 			// ask user to choose
 			PlatformChooser chooser = new PlatformChooser(
 					p.toArray(new String[0]));
 			try {
 				SwingUtilities.invokeAndWait(chooser);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				return null;
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 				return null;
 			}
 			platformChoice = chooser.choice;
 
 			if (platformChoice == null)
 				errorMessage = "No choice was made.";
 
 			return platformChoice;
 		}
 	}
 
 	private volatile String platformChoice;
 
 	private static boolean isComment(String line) {
 		if (line.startsWith(commentSign1) || line.startsWith(commentSign2)
 				|| line.startsWith(commentSign3))
 			return true;
 		else
 			return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
 	 */
 	public DSMicroarraySet parseSOFTSeriesFile(File file)
 			throws InputFileFormatException, InterruptedIOException {
 
 		String platformChosen = choosePlatform(file);
 		if (platformChosen == null) {
 			throw new InputFileFormatException(errorMessage);
 		}
 
 		BufferedReader in = null;
 		final int extSeperater = '.';
 		String fileName = file.getName();
 		int dotIndex = fileName.lastIndexOf(extSeperater);
 		if (dotIndex != -1) {
 			fileName = fileName.substring(0, dotIndex);
 		}
 
 		CSMicroarraySet maSet = new CSMicroarraySet();
 		maSet.setLabel(fileName);
 		Map<String, List<CSExpressionMarkerValue>> arrayToMarkers = new HashMap<String, List<CSExpressionMarkerValue>>();
 		List<String> markers = new ArrayList<String>();
 		int m = 0;
 		int valueIndex = -1;
 		int callIndex = -1;
 		int pValueIndex = -1;
 
 		try {
 			in = new BufferedReader(new FileReader(file));
 
 			int counter = 0;
 			String line = in.readLine();
 
 			String currentSample = null;
 			String currentSampleTitle = null;
 			String currentPlatform = null;
 			String currentArrayName = null;
 			boolean rightSample = false;
 			
 			boolean insideSampleTable = false;
 
 			while (line != null) {
 				/*
 				 * Adding comments to Experiment Information tab.We will ignore
 				 * the line which start with '!platform_table_end',
 				 * '!platform_table_begin', '!sample_table_begin'and
 				 * '!sample_table_end'
 				 */
 				if (line.startsWith(commentSign1)
 						|| line.startsWith(commentSign2)) {
 					if (!line.equalsIgnoreCase("!platform_table_end")
 							&& !line.equalsIgnoreCase("!platform_table_begin")
 							&& !line.equalsIgnoreCase("!sample_table_begin")
 							&& !line.equalsIgnoreCase("!sample_table_end")) {
 						// to be consistent, this detailed information should be used else where instead of as "description" field
 						// maSet.setDescription(line.substring(1));
 					}
 				}
 
 				final String sampleTag = "^SAMPLE = ";
 				if (line.startsWith(sampleTag)) {
 					currentSample = line.substring(sampleTag.length());
 					rightSample = false;
 					line = in.readLine();
 					continue;
 				}
 
 				final String sampleTitleTag = "!Sample_title = ";
 				if (line.startsWith(sampleTitleTag)) {
 					currentSampleTitle = line
 							.substring(sampleTitleTag.length());
 					line = in.readLine();
 					continue;
 				}
 
 				final String platformTag = "!Sample_platform_id = ";
 				if (line.startsWith("!Sample_platform_id = ")) {
 					currentPlatform = line.substring(platformTag.length());
 					rightSample = (currentPlatform.equals(platformChosen));
 					if (rightSample) {
 						// pre-condition: sample title goes before platform id
 						// in the file
 						currentArrayName = currentSample + ": "
 								+ currentSampleTitle;
 						arrayToMarkers.put(currentArrayName,
 								new ArrayList<CSExpressionMarkerValue>());
 					}
 					line = in.readLine();
 					continue;
 				}
 
 				final String sampleTableBeginTag = "!sample_table_begin";
 				final String sampleTableEndTag = "!sample_table_end";
 				
 				if (isComment(line) || !rightSample) {
 					if (!insideSampleTable && line.startsWith(sampleTableBeginTag)) {
 						insideSampleTable = true;
 					}
 					if (insideSampleTable && line.startsWith(sampleTableEndTag)) {
 						insideSampleTable = false;
 					}
 					
 					line = in.readLine();
 					continue;
 				}
 
 				if(!insideSampleTable) {
 					line = in.readLine();
 					continue;
 				}
 				
 				if (line.startsWith("ID_REF")) {
 					if (counter == 0) {
 						String[] valueLabels = line.split("\t");
 						for (int p = 0; p < valueLabels.length; p++) {
 							if (valueLabels[p].equals("VALUE")) {
 								valueIndex = p;
 							}
 							if (valueLabels[p].equals("DETECTION_CALL")
 									|| valueLabels[p].equals("ABS_CALL")) {
 								callIndex = p;
 							}
 							if (valueLabels[p].equals("DETECTION P-VALUE")
 									|| valueLabels[p].equals("DETECTION_P")) {
 								pValueIndex = p;
 							}
 						}
 					}
 					counter++;
 				}
 
 				if (!line.startsWith("ID_REF")) {
 					String[] markerToken = line.split("\t");
 					if (counter == 1) {
 						markers.add(markerToken[0]);
 						String markerName = new String(markerToken[0].trim());
 						CSExpressionMarker marker = new CSExpressionMarker(m);
 						marker.setLabel(markerName);
 						maSet.getMarkers().add(m, marker);
 						m++;
 					}
 
 					float value = Float.NaN;
 					try {
 						if (valueIndex < markerToken.length) {
 							value = Float.parseFloat(markerToken[valueIndex]
 									.trim());
 						}
 					} catch (NumberFormatException nfe) {
 					}
 
 					// create marker values
 					CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
 							value);
 					if (Float.isNaN(value)) {
 						markerValue.setMissing(true);
 					} else {
 						markerValue.setPresent();
 					}
 
 					boolean pValueFound = false;
 					if (pValueIndex >= 0) {
 						Double value1 = Double.valueOf(markerToken[pValueIndex]
 								.trim());
 						markerValue.setConfidence(value1);
 						pValueFound = true;
 					}
 					if (callIndex >= 0) {
 						String ca = markerToken[callIndex].trim();
 						char Call = Character.toUpperCase(ca.charAt(0));
 						if (!pValueFound) {
 							switch (Call) {
 							case PRESENT:
 								markerValue.setPresent();
 								break;
 							case ABSENT:
 								markerValue.setAbsent();
 								break;
 							case MARGINAL:
 								markerValue.setMarginal();
 								break;
 							}
 						}
 					}
 					List<CSExpressionMarkerValue> list = arrayToMarkers
 							.get(currentArrayName);
 					list.add(markerValue);
 
 				}
 
 				line = in.readLine();
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			try {
 				in.close();
 			} catch (IOException e) {
 			}
 		}
 
 		final int markerCount = markers.size();
 		int arrayIndex = 0;
 		for (String arrayName : arrayToMarkers.keySet()) {
 			CSMicroarray array = new CSMicroarray(arrayIndex, markerCount,
 					arrayName, DSMicroarraySet.affyTxtType);
 			List<CSExpressionMarkerValue> markerList = arrayToMarkers
 					.get(arrayName);			
 			if (markerList.size()>array.getMarkerValues().length){
				errorMessage = "Inconsistent number of markers between two samples: " + markerList.size() + " vs " + array.getMarkerValues().length;
 				throw new InputFileFormatException(errorMessage);
 			}
 			for (int markerIndex = 0; markerIndex < markerList.size(); markerIndex++) {				
 					array.setMarkerValue(markerIndex, markerList.get(markerIndex));
 			}
 
 			maSet.add(array);
 		}
 
 		// both the second and the third arguments of matchChipType are in
 		// fact ignored
 		String annotationFilename = AffyAnnotationUtil.matchAffyAnnotationFile(maSet);
 		maSet.setCompatibilityLabel(annotationFilename);
 
 		for (DSGeneMarker marker : maSet.getMarkers()) {
 			String token = marker.getLabel();
 			String[] locusResult = AnnotationParser.getInfo(token,
 					AnnotationParser.LOCUSLINK);
 			String locus = "";
 			if ((locusResult != null)
 					&& (!locusResult[0].trim().equals(""))) {
 				locus = locusResult[0].trim();
 			}
 			if (locus.compareTo("") != 0) {
 				try {
 					marker.setGeneId(Integer.parseInt(locus));
 				} catch (NumberFormatException e) {
 					log.info("Couldn't parse locus id: " + locus);
 				}
 			}
 			String[] geneNames = AnnotationParser.getInfo(token,
 					AnnotationParser.ABREV);
 			if (geneNames != null) {
 				marker.setGeneName(geneNames[0]);
 			}
 
 			marker.getUnigene().set(token);
 		}
 
 		return maSet;
 	}
 }
