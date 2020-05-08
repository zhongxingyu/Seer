 package org.geworkbench.util.sequences;
 
 import org.geworkbench.util.RandomNumberGenerator;
 import org.geworkbench.bison.datastructure.bioobjects.markers.SequenceMarker;
 import org.geworkbench.engine.parsers.sequences.SequenceResource;
 import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.DSCollection;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.panels.CSSequentialItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.engine.resource.Resource;
 
 import javax.swing.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * <p>Title: </p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: </p>
  *
  * @author not attributable
  * @version 1.0
  */
 
 public class SequenceDB extends CSDataSet<DSSequence> implements DSCollection<DSSequence> {
     private final static ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("dirty", boolean.class), new ObjectStreamField("isDNA", boolean.class), new ObjectStreamField("label", String.class), new ObjectStreamField("sequenceNo", int.class), new ObjectStreamField("file", File.class), new ObjectStreamField("sequences", ArrayList.class), new ObjectStreamField("maxLength", int.class), new ObjectStreamField("genePanel", DSPanel.class)};
 
     static private ImageIcon icon = new ImageIcon(SequenceDB.class.getResource("dna.GIF"));
     static private HashMap databases = new HashMap();
     private boolean dirty = false;
     private SequenceResource seqResource = null;
     private boolean isDNA = true;
     //private ArrayList sequences = new ArrayList();
     private int maxLength = 0;
     private String label = "Undefined";
     private int sequenceNo = 0;
     private File file = null;
     private DSItemList<org.geworkbench.bison.datastructure.bioobjects.markers.SequenceMarker> markerList = null;
     //added by xiaoqing for bug fix to create subsetSequenceDB. which matchs the old sequences index with new temp sub seqenceDB index.
     //Need rewrite to fit with caWorkbench3.
     private int[] matchIndex;
     private int[] reverseIndex;
 
     public SequenceDB() {
         setID(RandomNumberGenerator.getID());
     }
 
     public String getDataSetName() {
         return label;
     }
 
     public void addASequence(CSSequence sequence) {
         if (!sequence.isDNA()) {
             isDNA = false;
         }
         this.add(sequence);
         sequence.setSerial(this.indexOf(sequence));
         SequenceMarker marker = new org.geworkbench.bison.datastructure.bioobjects.markers.SequenceMarker();
         marker.parseLabel(sequence.getLabel());
         marker.setSerial(this.size());
 
         if (sequence.length() > maxLength) {
             maxLength = sequence.length();
         }
     }
 
     public int getSequenceNo() {
 
         return this.size();
     }
 
     public CSSequence getSequence(int i) {
         if ((this.size() == 0) && (file != null)) {
             readFASTAfile(file);
         }
         if (i < this.size() && i >= 0) {
             return (CSSequence) this.get(i);
         } else {
             return null;
         }
     }
 
     public int getMaxLength() {
         return maxLength;
     }
 
     public boolean isDNA() {
         return isDNA;
     }
 
     public static SequenceDB createFASTAfile(File file) {
         SequenceDB seqDB = new SequenceDB();
         seqDB.readFASTAfile(file);
         return seqDB;
     }
 
     public void readFASTAfile(File inputFile) {
         file = inputFile;
         label = file.getName();
 
         try {
             BufferedReader reader = new BufferedReader(new FileReader(file));
             CSSequence sequence = null;
             String data = new String();
             String s = reader.readLine();
             int id = 0;
             while (reader.ready()) {
                 if (s.trim().length() == 0) {
 
                 } else if (s.startsWith(">")) {
                     if (sequence != null) {
                         sequence.setSequence(data);
                         addASequence(sequence);
                     }
                     sequence = new CSSequence();
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
             System.out.println("Exception: " + ex);
         }
         parseMarkers();
         databases.put(file.getPath(), this);
     }
 
     public void parseMarkers() {
         markerList = new CSSequentialItemList<SequenceMarker>();
         for (int markerId = 0; markerId < size(); markerId++) {
             SequenceMarker marker = new SequenceMarker();
             marker.parseLabel(this.get(markerId).getLabel());
             marker.setSerial(markerId);
             markerList.add(markerId, marker);
         }
     }
 
     /**
      * initIndexArray
      */
     private void initIndexArray() {
         int size = size();
         matchIndex = new int[size];
         reverseIndex = new int[size];
         for (int i = 0; i < size; i++) { //init.
             matchIndex[i] = -1;
             reverseIndex[i] = -1;
         }
 
     }
 
     public void setResource(Resource resource) {
         seqResource = (SequenceResource) resource;
     }
 
     public void removeResource(Resource resource) {
         seqResource = null;
     }
 
     public void readFromResource() {
 
     }
 
     public void writeToResource() {
 
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
 
     public ImageIcon getIcon() {
         return icon;
     }
 
     static public SequenceDB getSequenceDB(File file) {
         SequenceDB sequenceDB = (SequenceDB) databases.get(file.getPath());
         if (sequenceDB == null) {
             sequenceDB = new SequenceDB();
             sequenceDB.readFASTAfile(file);
         }
         return sequenceDB;
     }
 
     public String getFASTAFileName() {
         return file.getAbsolutePath();
     }
 
     public void setFASTAFile(File f) {
         file = f;
         readFASTAfile(file);
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
 
     public int[] getMatchIndex() {
         return matchIndex;
     }
 
     public int[] getReverseIndex() {
         return reverseIndex;
     }
 
     public SequenceDB createSubSetSequenceDB(boolean[] included) {
         SequenceDB newDB = new SequenceDB();
         int newIndex = 0;
         initIndexArray();
         for (int i = 0; i < included.length; i++) {
             if (included[i]) {
                 newDB.addASequence(getSequence(i));
                 matchIndex[i] = newIndex;
                 reverseIndex[newIndex] = i;
 
                 newIndex++;
             }
         }
         return newDB;
     }
 
 
     public void writeToFile(String fileName) {
         file = new File(fileName);
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(file));
             for (int i = 0; i < this.getSequenceNo(); i++) {
                 CSSequence s = this.getSequence(i);
                 out.write(">" + s.getLabel() + "\n");
                 out.write(s.getSequence() + "\n");
             }
             out.close();
         } catch (IOException ex) {
             System.out.println("Error opening file: " + fileName);
         }
 
     }
 }
