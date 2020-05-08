 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.engine.preferences.PreferencesManager;
 import org.geworkbench.engine.properties.PropertiesManager;
 import org.geworkbench.util.CsvFileFilter;
 
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 import com.jgoodies.forms.builder.ButtonBarBuilder;
 
 /**
  *
  * Description:This Class is for retrieving probe annotation information from
  * default annotation files provided by Affymetrix.
  *
  * @author Xuegong Wang
  * @author manjunath at genomecenter dot columbia dot edu
  * @version $Id$
  */
 
 public class AnnotationParser implements Serializable {
 	private static final long serialVersionUID = -117234619759135916L;
 
 	static Log log = LogFactory.getLog(AnnotationParser.class);
 
 	public static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";
 
 	public static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";
 
 	public static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";
 
 	public static final String GENE_SYMBOL = "Gene Symbol";
 
 	public static final String PROBE_SET_ID = "Probe Set ID";
 
 	public static final String MAIN_DELIMITER = "///";
 
 	// field names
 	public static final String DESCRIPTION = "Gene Title"; // (full name)
 
 	public static final String ABREV = GENE_SYMBOL; // title(short name)
 
 	public static final String PATHWAY = "Pathway"; // pathway
 
 	public static final String GOTERM = GENE_ONTOLOGY_BIOLOGICAL_PROCESS; // Goterms
 
 	public static final String UNIGENE = "UniGene ID"; // Unigene
 
 	public static final String UNIGENE_CLUSTER = "Archival UniGene Cluster";
 
 	public static final String LOCUSLINK = "Entrez Gene"; // LocusLink
 
 	public static final String SWISSPROT = "SwissProt"; // swissprot
 
 	public static final String REFSEQ = "RefSeq Transcript ID"; // RefSeq
 
 	public static final String TRANSCRIPT = "Transcript Assignments";
 
 	public static final String SCIENTIFIC_NAME = "Species Scientific Name";
 
 	public static final String GENOME_VERSION = "Genome Version";
 
 	public static final String ALIGNMENT = "Alignments";
 
 	// columns read into geWorkbench
 	// probe id must be first column read in, and the rest of the columns must
 	// follow the same order
 	// as the columns in the annotation file.
 	private static final String[] labels = {
 			PROBE_SET_ID // probe id must be the first item in this list
 			, SCIENTIFIC_NAME, UNIGENE_CLUSTER, UNIGENE, GENOME_VERSION,
 			ALIGNMENT, DESCRIPTION, GENE_SYMBOL, LOCUSLINK, SWISSPROT, REFSEQ,
 			GENE_ONTOLOGY_BIOLOGICAL_PROCESS, GENE_ONTOLOGY_CELLULAR_COMPONENT,
 			GENE_ONTOLOGY_MOLECULAR_FUNCTION, PATHWAY, TRANSCRIPT };
 
 	// TODO all the DSDataSets handled in this class should be DSMicroarraySet
 	// FIELDS
 	private static DSDataSet<? extends DSBioObject> currentDataSet = null;
 	private static Map<DSDataSet<? extends DSBioObject>, String> datasetToChipTypes = new HashMap<DSDataSet<? extends DSBioObject>, String>();
 	private static Map<String, MarkerAnnotation> chipTypeToAnnotation = new TreeMap<String, MarkerAnnotation>();
 	// END FIELDS
 
 	/* The reason that we need APSerializable is that the status fields are designed as static. */
 	public static APSerializable getSerializable() {
 		return new APSerializable(currentDataSet, datasetToChipTypes,
 				chipTypeToAnnotation);
 	}
 
 	public static void setFromSerializable(APSerializable aps) {
 		currentDataSet = aps.currentDataSet;
 		datasetToChipTypes = aps.datasetToChipTypes;
 		chipTypeToAnnotation = aps.chipTypeToAnnotation;
 	}
 
 	private static final String ANNOT_DIR = "annotDir";
 
 	public static DSDataSet<? extends DSBioObject> getCurrentDataSet() {
 		return currentDataSet;
 	}
 
 	public static void setCurrentDataSet(DSDataSet<DSBioObject> currentDataSet) {
 		if(!(currentDataSet instanceof CSMicroarraySet)) {
 			AnnotationParser.currentDataSet = null;
 		}
 		AnnotationParser.currentDataSet = currentDataSet;
 	}
 
 	public static String getCurrentChipType() {
 		if (currentDataSet != null) {
 			return datasetToChipTypes.get(currentDataSet);
 		} else {
 			return null;
 		}
 	}
 
 	public static String getChipType(DSDataSet<? extends DSBioObject> dataset) {
 		return datasetToChipTypes.get(dataset);
 	}
 
 	public static void setChipType(DSDataSet<? extends DSBioObject> dataset, String chiptype) {
 		datasetToChipTypes.put(dataset, chiptype);
 		currentDataSet = dataset;
 	}
 
 	/* this is used to handle annotation file when the real dataset is chosen after annotation. */
 	private static CSMicroarraySet<? extends DSBioObject> dummyMicroarraySet = new CSMicroarraySet<DSMicroarray>();
 	public static String getLastAnnotationFileName () {
 		return dummyMicroarraySet.getAnnotationFileName();
 	}
 
 	/* if the annotation file is given, this method is called directly without GUI involved */
 	public static void loadAnnotationFile(
 			DSDataSet<? extends DSBioObject> dataset, File annotationData) {
 		if (!annotationData.exists()) { // data file is found
 			log.error("Annotation file " + annotationData + " does not exist.");
 			return;
 		}
 
 		BufferedInputStream bis = null;
 		String chipType = annotationData.getName();
 		try {
 			bis = new BufferedInputStream(new FileInputStream(annotationData));
 
 			CSVParser cvsParser = new CSVParser(bis);
 
 			cvsParser.setCommentStart("#;!");// Skip all comments line.
 												// XQ. The bug is reported
 												// by Bernd.
 
 			LabeledCSVParser parser = new LabeledCSVParser(cvsParser);
 
 			MarkerAnnotation markerAnnotation = new MarkerAnnotation();
 
 			boolean ignoreAll = false;
 			boolean cancelAnnotationFileProcessing = false;
 			while ((parser.getLine() != null)
 					&& !cancelAnnotationFileProcessing) {
 				String affyId = parser.getValueByLabel(labels[0]);
 				affyId = affyId.trim();
 				AnnotationFields fields = new AnnotationFields();
 				for (int i = 1; i < labels.length; i++) {
 					String label = labels[i];
 					String val = parser.getValueByLabel(label);
 					if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)
 							|| label.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)
 							|| label.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
 						// get rid of leading 0's
 						while (val.startsWith("0") && (val.length() > 0)) {
 							val = val.substring(1);
 						}
 					}
 					if (label.equals(GENE_SYMBOL))
 						fields.setGeneSymbol(val);
 					else if (label.equals(LOCUSLINK))
 						fields.setLocusLink(val);
 					else if (label.equals(SWISSPROT))
 						fields.setSwissProt(val);
 					else if (label.equals(DESCRIPTION))
 						fields.setDescription(val);
 					else if (label.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION))
 						fields.setMolecularFunction(val);
 					else if (label.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT))
 						fields.setCellularComponent(val);
 					else if (label.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS))
 						fields.setBiologicalProcess(val);
 					else if (label.equals(UNIGENE))
 						fields.setUniGene(val);
 					else if (label.equals(REFSEQ))
 						fields.setRefSeq(val);
 				}
 
 				if (markerAnnotation.containsMarker(affyId)) {
 					if (!ignoreAll) {
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
 						if (code == 1) {
 							ignoreAll = true;
 						}
 						if (code == 2) {
 							cancelAnnotationFileProcessing = true;
 						}
 					}
 				} else {
 					markerAnnotation.addMarker(affyId, fields);
 				}
 			}
 
 			if (!cancelAnnotationFileProcessing) {
 				chipTypeToAnnotation.put(chipType, markerAnnotation);
 			}
 
 			// all fine.
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		} finally {
 			try {
 				bis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		datasetToChipTypes.put(dataset, chipType);
 		currentDataSet = dataset;
 		if (dataset == null) {
 			dummyMicroarraySet.setAnnotationFileName(annotationData
 					.getAbsolutePath());
 		}
 		if (dataset instanceof CSMicroarraySet) {
 			CSMicroarraySet<?> d = (CSMicroarraySet<?>) dataset;
 			d.setAnnotationFileName(annotationData.getAbsolutePath());
 		}
 	}
 
 	/* !!! return value of this method depends on currentDataSet, which could be suprising if not careful */
 	public static String getGeneName(String id) {
 		try {
 			String chipType = datasetToChipTypes.get(currentDataSet);
 			return chipTypeToAnnotation.get(chipType).getFields(id).getGeneSymbol();
 		} catch (NullPointerException e) {
 			return id;
 		}
 	}
 
 	/**
 	 * This method returns required annotation field for a given affymatrix marker ID .
 	 *
 	 * @param affyid
 	 *            affyID as string
 	 * @param fieldID
 	 *
 	 */
 	// this method used to depend on chipTypeToAnnotations, which take unnecessary large memory
 	// the first step is to re-implement this method so it does not use chipTypeToAnnotations
 	static public String[] getInfo(String affyID, String fieldID) {
 		try {
 			String chipType = datasetToChipTypes.get(currentDataSet);
 			String field = "";
 
 			AnnotationFields fields = chipTypeToAnnotation.get(chipType).getFields(affyID);
 			// individual field to be process separately to eventually get rid of the large map
 			if(fieldID.equals(ABREV)) { // same as GENE_SYMBOL
 				field = fields.getGeneSymbol();
 			} else if(fieldID.equals(LOCUSLINK)) {
 				field = fields.getLocusLink();
 			} else if(fieldID.equals(DESCRIPTION)) {
 				field = fields.getDescription();
 			} else if(fieldID.equals(GENE_ONTOLOGY_MOLECULAR_FUNCTION)) {
 				field = fields.getMolecularFunction();
 			} else if(fieldID.equals(GENE_ONTOLOGY_CELLULAR_COMPONENT)) {
 				field = fields.getCellularComponent();
 			} else if(fieldID.equals(GENE_ONTOLOGY_BIOLOGICAL_PROCESS)) {
 				field = fields.getBiologicalProcess();
 			} else if(fieldID.equals(UNIGENE)) {
 				field = fields.getUniGene();
 			} else if(fieldID.equals(REFSEQ)) {
 				field = fields.getRefSeq();
 			} else if(fieldID.equals(SWISSPROT)) {
 				field = fields.getSwissProt();
 			} else {
 				log.error("trying to retreive unsupported field "+fieldID+" from marker annotation. null is returned.");
 				return null;
 			}
 			return field.split(MAIN_DELIMITER);
 		} catch (Exception e) {
 			if (affyID != null) {
 				log
 						.debug("Error getting info for affyId (" + affyID
 								+ "):" + e);
 			}
 			return null;
 		}
 	}
 
 	static public String getInfoAsString(String affyID, String fieldID) {
 		String[] result = getInfo(affyID, fieldID);
 
 		String info = " ";
 		if (result == null) {
 			return affyID;
 		}
 
 		if (result.length > 0) {
 			info = result[0];
 			for (int i = 1; i < result.length; i++) {
 				info += "/" + result[i];
 			}
 		}
 
 		return info;
 	}
 
 	public static Set<String> getSwissProtIDs(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
 		HashSet<String> set = new HashSet<String>();
 			String[] ids = chipTypeToAnnotation.get(chipType).getFields(markerID).getSwissProt().split("///");
 			for (String s : ids) {
 				set.add(s.trim());
 			}
 		return set;
 	}
 
 	public static Set<String> getGeneIDs(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
		HashSet<String> set = new HashSet<String>();
 		MarkerAnnotation annotation = chipTypeToAnnotation.get(chipType);
 		AnnotationFields fields = annotation.getFields(markerID);
 		if(fields==null) {
 			return set;
 		}
 		String locus = fields.getLocusLink();
 		if(locus==null) {
 			return set;
 		}
 		String[] ids = locus.split("///");
 		for (String s : ids) {
 			set.add(s.trim());
 		}
 		return set;
 	}
 
 	public static Map<String, List<Integer>> getGeneIdToMarkerIDMapping(
 			DSMicroarraySet<? extends DSMicroarray> microarraySet) {
 		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
 		DSItemList<DSGeneMarker> markers = microarraySet.getMarkers();
 		int index = 0;
 		for (DSGeneMarker marker : markers) {
 			if (marker != null && marker.getLabel() != null) {
 				Set<String> geneIDs = getGeneIDs(marker.getLabel());
 				for (String s : geneIDs) {
 					List<Integer> list = map.get(s);
 					if (list == null) {
 						list = new ArrayList<Integer>();
 						list.add(index);
 						map.put(s, list);
 					} else {
 						list.add(index);
 					}
 				}
 				index++;
 			}
 		}
 		return map;
 
 	}
 	
 	public static Map<String, List<Integer>> getGeneNameToMarkerIDMapping(
 			DSMicroarraySet<? extends DSMicroarray> microarraySet) {
 		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
 		DSItemList<DSGeneMarker> markers = microarraySet.getMarkers();
 		int index = 0;
 		for (DSGeneMarker marker : markers) {
 			if (marker != null && marker.getLabel() != null) {			 
 				try {
 					
 					Set<String> geneNames = getGeneNames(marker.getLabel());							
 					for (String s : geneNames) {
 						List<Integer> list = map.get(s);
 						if(list==null) {
 							list = new ArrayList<Integer>();
 							list.add(index);
 							map.put(s, list);
 						} else {
 							list.add(index);
 						}
 					}
 					index++;
 				} catch (Exception e) {					 
 					continue;
 				}
 			}
 		}
 		return map;
 	}	
 	
 	
 	public static Set<String> getGeneNames(String markerID) {
 		String chipType = datasetToChipTypes.get(currentDataSet);
 
 		HashSet<String> set = new HashSet<String>();
 			String[] ids = chipTypeToAnnotation.get(chipType).getFields(markerID).getGeneSymbol().split("///");
 			for (String s : ids) {
 				set.add(s.trim());
 			}
 		return set;
 	}
 
 
 	public static String matchChipType(final DSDataSet<? extends DSBioObject> dataset, String id,
 			boolean askIfNotFound) {
 		PreferencesManager preferencesManager = PreferencesManager
 				.getPreferencesManager();
 		File prefDir = preferencesManager.getPrefDir();
 		final File annotFile = new File(prefDir, "annotations.prefs");
 		if (!annotFile.exists()) {
 			try {
 				SwingUtilities.invokeAndWait(new Runnable() {
 
 					public void run() {
 						boolean dontShowAgain = showAnnotationsMessage();
 						if (dontShowAgain) {
 							try {
 								annotFile.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 				});
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (InvocationTargetException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 		currentDataSet = dataset;
 
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					userFile = selectUserDefinedAnnotation(dataset);
 				}
 			});
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (userFile != null) {
 			loadAnnotationFile(dataset, userFile);
 			return userFile.getName();
 		} else {
 			return "Other";
 		}
 	}
 
 	private volatile static File userFile = null;
 
 	public static boolean showAnnotationsMessage() {
 		String message = "To process Affymetrix files many geWorkbench components require information from the associated chip annotation files. Annotation files can be downloaded from the Affymetrix web site, <a href='http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays' target='_blank'>http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays</a> (due to the Affymetrix license we are precluded from shipping these files with geWorkbench). Place downloaded files to a directory of your choice; when prompted by geWorkbench point to the appropriate annotation file to be associated with the microarray data you are about to load into the application. Your data will load even if you do not associate them with an annotation file; in that case, some geWorkbench components will not be fully functional.<br>\n"
 				+ "<br>\n"
 				+ "NOTE: Affymetrix requires users to register in order to download annotation files from its web site. Registration is a one time procedure. The credentials (user id and password) acquired via the registration process can then be used in subsequent interactions with the site.<br>\n"
 				+ "<br>\n"
 				+ "Each chip type in the Affymetrix site can have several associated annotation files (with names like \"...Annotations, BLAST\", \"...Annotations, MAGE-ML XML\", etc). Only annotation files named \"...Annotations, CSV\" need to be downloaded (these are the only files that geWorkbench can process).<br>";
 		final JDialog window = new JDialog((Frame) null,
 				"Annotations Information");
 		Container panel = window.getContentPane();
 		JEditorPane textarea = new JEditorPane("text/html", message);
 		textarea.setEditable(false);
 		textarea.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					openURL("http://www.affymetrix.com/support/technical/byproduct.affx?cat=arrays");
 				}
 			}
 		});
 		// textarea.setLineWrap(true);
 		// textarea.setWrapStyleWord(true);
 		panel.add(textarea, BorderLayout.CENTER);
 		ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
 		JCheckBox dontShow = new JCheckBox("Don't show this again");
 		builder.addFixed(dontShow);
 		builder.addGlue();
 		JButton jButton = new JButton("Continue");
 		builder.addFixed(jButton);
 		jButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				window.dispose();
 			}
 		});
 		panel.add(builder.getPanel(), BorderLayout.SOUTH);
 		int width = 500;
 		int height = 450;
 		window.pack();
 		window.setSize(width, height);
 		window
 				.setLocation(
 						(Toolkit.getDefaultToolkit().getScreenSize().width - width) / 2,
 						(Toolkit.getDefaultToolkit().getScreenSize().height - height) / 2);
 		window.setModal(true);
 		window.setVisible(true);
 		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
 		return dontShow.isSelected();
 	}
 
 	public static void openURL(String url) {
 		String osName = System.getProperty("os.name");
 		try {
 			if (osName.startsWith("Mac OS")) {
 				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
 				Method openURL = fileMgr.getDeclaredMethod("openURL",
 						new Class<?>[] { String.class });
 				openURL.invoke(null, new Object[] { url });
 			} else if (osName.startsWith("Windows")) {
 				Runtime.getRuntime().exec(
 						"rundll32 url.dll,FileProtocolHandler " + url);
 			} else { // assume Unix or Linux
 				String[] browsers = { "firefox", "opera", "konqueror",
 						"epiphany", "mozilla", "netscape" };
 				String browser = null;
 				for (int count = 0; count < browsers.length && browser == null; count++)
 					if (Runtime.getRuntime().exec(
 							new String[] { "which", browsers[count] })
 							.waitFor() == 0)
 						browser = browsers[count];
 				if (browser == null)
 					throw new Exception("Could not find web browser");
 				else
 					Runtime.getRuntime().exec(new String[] { browser, url });
 			}
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Unable to open browser"
 					+ ":\n" + e.getLocalizedMessage());
 		}
 	}
 
 	private static File selectUserDefinedAnnotation(DSDataSet<? extends DSBioObject> dataset) {
 		PropertiesManager properties = PropertiesManager.getInstance();
 		String annotationDir = System.getProperty("user.dir"); ;
 		try {
 			annotationDir = properties.getProperty(AnnotationParser.class,
 					ANNOT_DIR, annotationDir);
 		} catch (IOException e) {
 			e.printStackTrace(); // To change body of catch statement use
 									// File | Settings | File Templates.
 		}
 
 		JFileChooser chooser = new JFileChooser(annotationDir);
 		chooser.setFileFilter(new CsvFileFilter());
 		chooser.setDialogTitle("Please select the annotation file");
 		int returnVal = chooser.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File userAnnotations = chooser.getSelectedFile();
 			try {
 				properties.setProperty(AnnotationParser.class, ANNOT_DIR,
 						userAnnotations.getParent());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return userAnnotations;
 		} else {
 			return null;
 		}
 	}
 
 	static private class AnnotationFields implements Serializable {
 		private static final long serialVersionUID = -3571880185587329070L;
 
 		String getMolecularFunction() {
 			return molecularFunction;
 		}
 
 		void setMolecularFunction(String molecularFunction) {
 			this.molecularFunction = molecularFunction;
 		}
 
 		String getCellularComponent() {
 			return cellularComponent;
 		}
 
 		void setCellularComponent(String cellularComponent) {
 			this.cellularComponent = cellularComponent;
 		}
 
 		String getBiologicalProcess() {
 			return biologicalProcess;
 		}
 
 		void setBiologicalProcess(String biologicalProcess) {
 			this.biologicalProcess = biologicalProcess;
 		}
 
 		String getUniGene() {
 			return uniGene;
 		}
 
 		void setUniGene(String uniGene) {
 			this.uniGene = uniGene;
 		}
 
 		String getDescription() {
 			return description;
 		}
 
 		void setDescription(String description) {
 			this.description = description;
 		}
 
 		String getGeneSymbol() {
 			return geneSymbol;
 		}
 
 		void setGeneSymbol(String geneSymbol) {
 			this.geneSymbol = geneSymbol;
 		}
 
 		String getLocusLink() {
 			return locusLink;
 		}
 
 		void setLocusLink(String locusLink) {
 			this.locusLink = locusLink;
 		}
 
 		String getSwissProt() {
 			return swissProt;
 		}
 
 		void setSwissProt(String swissProt) {
 			this.swissProt = swissProt;
 		}
 
 		public void setRefSeq(String refSeq) {
 			this.refSeq = refSeq;
 		}
 
 		public String getRefSeq() {
 			return refSeq;
 		}
 
 		private String molecularFunction, cellularComponent, biologicalProcess;
 		private String uniGene, description, geneSymbol, locusLink, swissProt;
 		private String refSeq;
 	}
 
 	static class MarkerAnnotation implements Serializable {
 		private static final long serialVersionUID = 1350873248604803043L;
 
 		private Map<String, AnnotationFields> annotationFields;
 
 		MarkerAnnotation() {
 			annotationFields = new TreeMap<String, AnnotationFields>();
 		}
 
 		void addMarker(String marker, AnnotationFields fields) {
 			annotationFields.put(marker, fields);
 		}
 
 		boolean containsMarker(String marker) {
 			return annotationFields.containsKey(marker);
 		}
 
 		AnnotationFields getFields(String marker) {
 			return annotationFields.get(marker);
 		}
 
 		Set<String> getMarkerSet() {
 			return annotationFields.keySet();
 		}
 	}
 
 	public static void cleanUpAnnotatioAfterUnload(DSDataSet<DSBioObject> dataset) {
 		String annotationName = datasetToChipTypes.get(dataset);
 		datasetToChipTypes.remove(dataset);
 
 		for(DSDataSet<? extends DSBioObject> dset: datasetToChipTypes.keySet() ) {
 			if(datasetToChipTypes.get(dset).equals(annotationName)) return;
 		}
 
 		// if not returned, then it is not used anymore, clean it up
 		if(annotationName!=null)
 			chipTypeToAnnotation.put(annotationName, null);
 	}
 
 }
