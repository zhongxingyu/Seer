 /**
  * 
  */
 package org.geworkbench.bison.datastructure.complex.pattern;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.util.patterns.CSMatchedSeqPattern;
 
 /**
  * @author zji
  * @version $Id$
  * 
  */
 public class PatternResult extends CSAncillaryDataSet<DSSequence> implements
 		Serializable {
 
 	private static final long serialVersionUID = -66278700941966192L;
 
 	private static Log log = LogFactory.getLog(PatternResult.class);
 
 	//algorithm names
     public static final String DISCOVER = "discovery";
     public static final String EXHAUSTIVE = "exhaustive";
     
 	private List<DSMatchedSeqPattern> patterns = new ArrayList<DSMatchedSeqPattern>();
 	private File dataSetFile;
 	public DSSequenceSet<? extends DSSequence> sequenceDB;
 
 	@SuppressWarnings("unchecked")
 	public PatternResult(
 			String name, final DSDataSet<? extends DSSequence> parent,
 			int minSupport, int minTokens, int minWTokens, int window) {
 		super((DSDataSet<DSSequence>) parent, name);
 		sequenceDB = (DSSequenceSet<? extends DSSequence>) parent;
 
 		String idString = RandomNumberGenerator.getID();
 		setID(idString);
 		setLabel(name);
 		
 		this.minSupport = minSupport;
 		this.minTokens = minTokens;
 		this.minWTokens = minWTokens;
 		this.window = window;
 	}
 
 	// another constructor originally as PatternDB
 	public PatternResult(File _seqFile, DSDataSet<DSSequence> parent) {
 		super(parent, "PatternResult");
 		this.sequenceDB = (DSSequenceSet<? extends DSSequence>) parent;
 
 		dataSetFile = _seqFile; // this is only used to get track file name
 		String idString = RandomNumberGenerator.getID();
 		setID(idString);
 
 		this.minSupport = -1;
 		this.minTokens = -1;
 		this.minWTokens = -1;
 		this.window = -1;
 	}
 
 	// TODO review the necessity of these (used only in getDataSetName)
 	final private int minSupport;
 	final private int minTokens;
 	final private int minWTokens;
 	final private int window;
 	
 	@Override
 	public String getDataSetName() {
 		if(minSupport<0)
 			return "";
 		
 		return "Parms S:" + minSupport + ", T:"
 				+ minTokens + ", W["
 				+ minWTokens + "," + window
 				+ "]";
 	}
 
 	// read file whose content is File:some_file_name, weird
 	public boolean read(File _file) {
 		try {
 			file = new File(_file.getCanonicalPath());
 			label = file.getName();
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			reader.readLine();
 			String s = reader.readLine();
 			if (s.startsWith("File:")) {
 				File newFile = new File(s.substring(5));
 				if (!dataSetFile.getName().equalsIgnoreCase(newFile.getName())) {
 					JOptionPane.showMessageDialog(null,
							"The sequence dataset selected and the sequence filename in the pattern file doesn't match",
 							"Pattern Discovery", JOptionPane.WARNING_MESSAGE);
 					return false;
 				}
 				s = reader.readLine();
 			}
 			patterns.clear();
 			while (s != null) {
 				CSMatchedSeqPattern pattern = new org.geworkbench.util.patterns.CSMatchedSeqPattern(
 						s);
 				patterns.add(pattern);
 				s = reader.readLine();
 			}
 		} catch (IOException ex) {
 			log.error("IOException: " + ex);
 			return false;
 		}
 
 		setDescription("Number of Patterns found:" + patterns.size());
 		return true;
 	}
 
 	// create file that is consumed by read(File) method
 	public void write(File file) {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			int i = 0;
 			Iterator<DSMatchedSeqPattern> it = patterns.iterator();
 			String path = dataSetFile.getCanonicalPath();
 			writer.write(DISCOVER);
 			writer.newLine();
 			writer.write("File:" + path);
 			writer.newLine();
 			while (it.hasNext()) {
 				DSMatchedSeqPattern pattern = (DSMatchedSeqPattern) it.next();
 				writer.write("[" + i++ + "]\t");
 				pattern.write(writer);
 			}
 			writer.flush();
 			writer.close();
 		} catch (IOException ex) {
 			log.error("IOException: " + ex);
 		}
 	}
 
 	public int getPatternNo() {
 		return patterns.size();
 	}
 
 	public DSMatchedSeqPattern getPattern(int i) {
 		if (i>=0 && i < patterns.size()) {
 			return (DSMatchedSeqPattern) patterns.get(i);
 		}
 		return null;
 	}
 
 	public void add(DSMatchedSeqPattern pattern) {
 		patterns.add(pattern);
 	}
 
 	/**
     *
     * @param out ObjectOutputStream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
 
    /**
     *
     * @param in ObjectInputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
 
 
 	@Override
 	public int size() {
 		if (patterns != null) {
 			return patterns.size();
 		}
 		return 0;
 	}
 
     /**
      * writeToFile
      *
      * @param fileName String
      */
     @Override
     public void writeToFile(String fileName) {
     	throw new RuntimeException(
     			"Please right click on the result table and click on" +
     			"\n" + "\"Save All Patterns\" to save the result dataset");
     }
     
     public DSSequenceSet<? extends DSSequence> getParentSequenceSet() {
     	return this.sequenceDB;
     }
 
 }
