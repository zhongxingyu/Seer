 package org.geworkbench.bison.datastructure.biocollections.sequences;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.markers.SequenceMarker;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.panels.CSSequentialItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.bison.util.SequenceUtils;
 
 /**
  * 
  * @author not attributable
  * @version $Id$
  */
 
 public class CSSequenceSet<T extends DSSequence> extends CSDataSet<T> implements
 		DSSequenceSet<T> {
 	private static final long serialVersionUID = -2426885649247874087L;
 
 	static private HashMap<String, CSSequenceSet<?>> databases = new HashMap<String, CSSequenceSet<?>>();
 	private boolean dirty = false;
 	private boolean isDNA = true;
 
 	private int maxLength = 0;
 	private String label = "Undefined";
 
 	private File file = null;
 	private DSItemList<SequenceMarker> markerList = null;
 
 	private int[] matchIndex;
 	private int[] reverseIndex;
 	
	/**
	 * Designates if the marker subselection imposed by the activated panels is
	 * imposed on the this sequence set view.
	 */
	private boolean useMarkerPanel = false;


 	public CSSequenceSet() {
 		setID(RandomNumberGenerator.getID());
 	}
 
 	public void setDNA(boolean DNA) {
 		isDNA = DNA;
 	}
 
 	public String getDataSetName() {
 		return label;
 	}
 
 	public void addASequence(T sequence) {
 		if (!SequenceUtils.isValidDNASeqForBLAST(sequence)) {
 			isDNA = false;
 		}
 		this.add(sequence);
 		sequence.setSerial(this.indexOf(sequence));
 
 		if (sequence.length() > maxLength) {
 			maxLength = sequence.length();
 		}
 	}
 
 	public int getSequenceNo() {
 
 		return this.size();
 	}
 
 	public T getSequence(int i) {
 		if ((this.size() == 0) && (file != null)) {
 			readFASTAFile(file);
 		}
 		if (i < this.size() && i >= 0) {
 			return this.get(i);
 		} else {
 			return null;
 		}
 	}
 
 	public T getSequence(DSGeneMarker marker) {
 		if ((this.size() == 0) && (file != null)) {
 			readFASTAFile(file);
 		}
 		if (markerList != null && markerList.contains(marker)) {
 			int i = markerList.indexOf(marker);
 			return this.get(i);
 		} else {
 			return null;
 		}
 	}
 
 	public DSSequenceSet<DSSequence> getActiveSequenceSet(
 			DSPanel<? extends DSGeneMarker> markerPanel) {
 		CSSequenceSet<DSSequence> sequenceDB = new CSSequenceSet<DSSequence>();
 		if (markerPanel != null && markerPanel.size() > 0) {
 			for (DSGeneMarker marker : markerPanel) {
 
 				T newSequence = this.getSequence(marker);
 				if (newSequence != null) {
 					sequenceDB.addASequence(newSequence);
 				}
 			}
 			sequenceDB.setFASTAFile(file);
 		}
 		return sequenceDB;
 	}
 
 	public int getMaxLength() {
 		return maxLength;
 	}
 
 	public boolean isDNA() {
 		return isDNA;
 	}
 
 	public static DSSequenceSet<DSSequence> createFASTAfile(File file) {
 		CSSequenceSet<DSSequence> seqDB = new CSSequenceSet<DSSequence>();
 		seqDB.readFASTAFile(file);
 		return seqDB;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void readFASTAFile(File inputFile) {
 		file = inputFile;
 		label = file.getName();
 
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			T sequence = null;
 			String data = new String();
 			String s = reader.readLine();
 
 			while (reader.ready()) {
 				if (s.trim().length() == 0) {
 
 				} else if (s.startsWith(">")) {
 					if (sequence != null) {
 						sequence.setSequence(data);
 						addASequence(sequence);
 					}
 					sequence = (T) (new CSSequence());
 					sequence.setLabel(s);
 					data = new String();
 				} else {
 					data += s;
 				}
 				s = reader.readLine();
 
 			}
 			if (sequence != null) {
 				sequence.setSequence(data + s);
 				addASequence(sequence);
 			}
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		parseMarkers();
 		databases.put(file.getPath(), this);
 		setDescription("# of sequences: " + size());
 	}
 
 	public void parseMarkers() {
 		markerList = new CSSequentialItemList<SequenceMarker>();
 		for (int markerId = 0; markerId < size(); markerId++) {
 			SequenceMarker marker = new SequenceMarker();
 			DSSequence sequence = this.get(markerId);
 			marker.parseLabel(sequence.getLabel());
 			sequence.setDescription(sequence.getLabel());
 			// Use the short label as the label for the sequence as well (bug
 			// #251)
 			if ((marker.getLabel() != null) && (marker.getLabel().length() > 0)) {
 				sequence.setLabel(marker.getLabel());
 			}
 			marker.setSerial(markerId);
 			markerList.add(markerId, marker);
 		}
 	}
 
 	public void readFromResource() {
 
 	}
 
 	public void writeToResource() {
 
 	}
 	
	/**
	 * Set/resets marker subselection based on activated panels.
	 * 
	 * @param status
	 */
	public void useMarkerPanel(boolean status) {
		useMarkerPanel = status;
	}

	/**
	 * Gets the status of marker activation
	 * 
	 * @return the status of marker activation
	 */
	public boolean useMarkerPanel() {
		return useMarkerPanel;
	}

	

 	public boolean isDirty() {
 		return dirty;
 	}
 
 	public void setDirty(boolean flag) {
 		dirty = flag;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public void setMatchIndex(int[] matchIndex) {
 		this.matchIndex = matchIndex;
 	}
 
 	public void setReverseIndex(int[] reverseIndex) {
 		this.reverseIndex = reverseIndex;
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public String toString() {
 		if (file != null) {
 			return file.getName();
 		} else {
 			return label;
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	static public CSSequenceSet getSequenceDB(File file) {
 		CSSequenceSet<?> sequenceDB = databases
 				.get(file.getPath());
 		if (sequenceDB == null) {
 			sequenceDB = new CSSequenceSet<DSSequence>();
 			sequenceDB.readFASTAFile(file);
 		}
 		return sequenceDB;
 	}
 
 	public String getFASTAFileName() {
 		return file.getAbsolutePath();
 	}
 
 	public void setFASTAFile(File f) {
 		file = f;
 
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	/**
 	 * getCompatibilityLabel
 	 * 
 	 * @return String
 	 */
 	public String getCompatibilityLabel() {
 		return "FASTA";
 	}
 
 	public DSItemList<? extends DSGeneMarker> getMarkerList() {
 		return markerList;
 	}
 
 	public T get(String label) {
 		DSGeneMarker marker = getMarkerList().get(label);
 		if (marker != null)
 			return get(marker.getSerial());
 		return super.get(label);
 	}
 
 	public int[] getMatchIndex() {
 		return matchIndex;
 	}
 
 	public int[] getReverseIndex() {
 		return reverseIndex;
 	}
 
 	public void writeToFile(String fileName) {
 		file = new File(fileName);
 		String lineBreak = System.getProperty("line.separator");
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(file));
 			for (int i = 0; i < this.getSequenceNo(); i++) {
 				T s = this.getSequence(i);
 				out.write(">" + s.getLabel() + lineBreak);
 				out.write(s.getSequence() + lineBreak);
 			}
 			out.close();
 		} catch (IOException ex) {
 			JOptionPane.showMessageDialog(null, "File " + fileName
 					+ " is not saved due to IOException " + ex.getMessage(),
 					"File Saving Failed", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 }
