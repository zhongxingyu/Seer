 package com.googlecode.whatswrong.io;
 
 import com.googlecode.whatswrong.NLPInstance;
 import com.googlecode.whatswrong.SimpleGridBagConstraints;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.util.*;
import java.util.List;
 
 /**
  * A TabFormat loads data from text files where token properties are represented as white-space/tab separated values.
  * This includes formats such as the CoNLL shared task formats or the MALT-Tab format. This class represents the generic
  * framework to process such tab separated data. To implement a concrete format clients have to implement the {@link
  * TabProcessor} interface.
  *
  * @author Sebastian Riedel
  */
 @SuppressWarnings({"MissingMethodJavaDoc", "MissingFieldJavaDoc"})
 public class TabFormat implements CorpusFormat {
 
     private JPanel accessory;
     private SortedMap<String, TabProcessor> processors = new TreeMap<String, TabProcessor>();
     private JComboBox type;
     private JCheckBox open;
     private Monitor monitor;
 
 
     public TabFormat() {
         addProcessor("CoNLL 2009", new CoNLL2009());
         addProcessor("CoNLL 2008", new CoNLL2008());
         addProcessor("CoNLL 2006", new CoNLL2006());
         //addProcessor("CoNLL 2005", new CoNLL2005());
         addProcessor("CoNLL 2004", new CoNLL2004());
         addProcessor("CoNLL 2002", new CoNLL2002());
         addProcessor("CoNLL 2003", new CoNLL2003());
         addProcessor("CoNLL 2000", new CoNLL2000());
         addProcessor(new MaltTab());
 
         accessory = new JPanel(new GridBagLayout());
         type = new JComboBox(new Vector<Object>(processors.values()));
         type.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 open.setEnabled(((TabProcessor) type.getSelectedItem()).supportsOpen());
             }
         });
         open = new JCheckBox("open", false);
         open.setToolTipText("If checked an additional file with same name but .open extension is also loaded");
         open.setEnabled(((TabProcessor) type.getSelectedItem()).supportsOpen());
 
         accessory.add(new JLabel("Type:"), new SimpleGridBagConstraints(0, true));
         accessory.add(type, new SimpleGridBagConstraints(0, false));
         accessory.add(open, new SimpleGridBagConstraints(1, false));
 
     }
 
     public void addProcessor(String name, TabProcessor processor) {
         processors.put(name, processor);
     }
 
     public void addProcessor(TabProcessor processor) {
         processors.put(processor.toString(), processor);
     }
 
     public String toString() {
         return getName();
     }
 
     public String getName() {
         return "TAB-separated";
     }
 
     public String getLongName() {
         return getName() + "(" + type.getSelectedItem().toString() + ")";
     }
 
     public JComponent getAccessory() {
         return accessory;
     }
 
     public void setMonitor(Monitor monitor) {
         this.monitor = monitor;
     }
 
     public void loadProperties(Properties properties, String prefix) {
         String yearString = properties.getProperty(prefix + ".tab.type", "CoNLL 2008");
         type.setSelectedItem(processors.get(yearString));
     }
 
 
     public void saveProperties(Properties properties, String prefix) {
         properties.setProperty(prefix + ".tab.type", type.getSelectedItem().toString());
 
     }
 
 
     public java.util.List<NLPInstance> load(File file, int from, int to) throws IOException {
         TabProcessor processor = (TabProcessor) type.getSelectedItem();
         java.util.List<NLPInstance> result = loadTabs(file, from, to, processor, false);
         if (open.isSelected()) {
             String filename = file.getName().substring(0, file.getName().lastIndexOf('.')) + ".open";
             File openFile = new File(file.getParent() + "/" + filename);
             java.util.List<NLPInstance> openCorpus = loadTabs(openFile, from, to, processor, true);
             for (int i = 0; i < openCorpus.size(); ++i) {
                 result.get(i).merge(openCorpus.get(i));
             }
 
         }
         return result;
     }
 
     private java.util.List<NLPInstance> loadTabs(File file, int from, int to, TabProcessor processor, boolean open)
         throws IOException {
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
         ArrayList<NLPInstance> corpus = new ArrayList<NLPInstance>();
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
         int instanceNr = 0;
         for (String line = reader.readLine(); line != null && instanceNr < to; line = reader.readLine()) {
             line = line.trim();
             if (line.equals("")) {
                 monitor.progressed(instanceNr);
                 if (instanceNr++ < from) continue;
                 NLPInstance instance = open ? processor.createOpen(rows) : processor.create(rows);
                 corpus.add(instance);
                 rows.clear();
             } else {
                 if (instanceNr < from) continue;
                rows.add(Arrays.asList(line.split("\\s+")));
             }
 
         }
         if (rows.size() > 0)
             corpus.add(open ? processor.createOpen(rows) : processor.create(rows));
         return corpus;
 
     }
 
     public static void extractSpan03(java.util.List<? extends java.util.List<String>> rows,
                                      int column,
                                      String type,
                                      NLPInstance instance) {
         int index;
         index = 0;
         boolean inChunk = false;
         int begin = 0;
         String currentChunk = "";
         for (java.util.List<String> row : rows) {
             String chunk = row.get(column);
             int minus = chunk.indexOf('-');
             if (minus != -1) {
                 String bio = chunk.substring(0, minus);
                 String label = chunk.substring(minus + 1);
                 if (inChunk) {
                     //start a new chunk and finish old one
                     if ("B".equals(bio) || "I".equals(bio) && !label.equals(currentChunk)) {
                         instance.addSpan(begin, index - 1, currentChunk, type);
                         begin = index;
                         currentChunk = label;
                     }
                 } else {
                     inChunk = true;
                     begin = index;
                     currentChunk = label;
                 }
             } else {
                 if (inChunk) {
                     instance.addSpan(begin, index - 1, currentChunk, type);
                     inChunk = false;
                 }
             }
             ++index;
         }
         if (inChunk) {
             instance.addSpan(begin, index - 1, currentChunk, type);
         }
     }
 
 
     public static void extractSpan00(java.util.List<? extends java.util.List<String>> rows,
                                      int column,
                                      String type,
                                      NLPInstance instance) {
         int index;
         index = 0;
         boolean inChunk = false;
         int begin = 0;
         String currentChunk = "";
         for (java.util.List<String> row : rows) {
             String chunk = row.get(column);
             int minus = chunk.indexOf('-');
             if (minus != -1) {
                 String bio = chunk.substring(0, minus);
                 String label = chunk.substring(minus + 1);
                 if ("B".equals(bio)) {
                     if (inChunk) {
                         instance.addSpan(begin, index - 1, currentChunk, type);
                     }
                     begin = index;
                     currentChunk = label;
                     inChunk = true;
                 }
             } else {
                 if (inChunk) {
                     instance.addSpan(begin, index - 1, currentChunk, type);
                     inChunk = false;
                 }
             }
             ++index;
         }
     }
 
     public static void extractSpan05(java.util.List<? extends java.util.List<String>> rows,
                                      int column,
                                      String type,
                                      String prefix,
                                      NLPInstance instance) {
         int index = 0;
         int begin = 0;
         String currentChunk = "";
         for (java.util.List<String> row : rows) {
             String chunk = row.get(column);
             if (chunk.startsWith("(")) {
                 currentChunk = chunk.substring(1, chunk.indexOf("*"));
                 begin = index;
             }
             if (chunk.endsWith(")")) {
                 instance.addSpan(begin, index, prefix + currentChunk, type);
             }
             ++index;
         }
     }
 
 }
 
