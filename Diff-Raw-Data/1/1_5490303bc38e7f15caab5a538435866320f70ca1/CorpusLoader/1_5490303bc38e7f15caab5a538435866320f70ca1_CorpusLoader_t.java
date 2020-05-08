 package com.googlecode.whatswrong;
 
 import com.googlecode.whatswrong.io.*;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 import java.util.List;
 
 /**
  * A CorpusLoader is responsible for loading and managing corpora. A corpus is implemented as a list of NLPInstance
  * objects. Each CorpusLoader maintains a list of such corpora, which can be extended by loading corpora from files. The
  * loader displays the corpora and allows the user to select one such corpus. The selected corpus will then be used by
  * other components (such as the {@link com.googlecode.whatswrong.CorpusNavigator} to pick and render NLPInstance
  * objects from.
  * <p/>
  * <p>A CorpusLoader sends out messages to {@link com.googlecode.whatswrong.CorpusLoader.Listener} objects whenever a
  * new corpus is added, removed or selected.
  * <p/>
  * <p>The CorpusLoader loads files using {@link com.googlecode.whatswrong.io.CorpusFormat} objects. Each such object
  * provides an swing panel that will be used in the file dialog to configure how the particular format needs to be
  * loaded.
  *
  * @author Sebastian Riedel
  */
 @SuppressWarnings({"MissingMethodJavaDoc"})
 public class CorpusLoader extends JPanel {
 
     /**
      * The current selected corpus.
      */
     private List<NLPInstance> selected;
     /**
      * The set of all loaded corpora.
      */
     private List<List<NLPInstance>> corpora;
 
     /**
      * The file names the corpora came from, stored in a list model.
      */
     private DefaultListModel fileNames;
 
     /**
      * A mapping from names to CorpusFormat objects that will load corpora when the user chooses the corresponding name.
      */
     private HashMap<String, CorpusFormat>
         formats = new HashMap<String, CorpusFormat>();
 
     /**
      * The list of listeners of this loader.
      */
     private ArrayList<Listener> changeListeners = new ArrayList<Listener>();
 
     /**
      * The button that removes the selected corpus.
      */
     private JButton remove;
 
     /**
      * The file chooser dialog.
      */
     private JFileChooser fileChooser;
 
     /**
      * The id of this loader (used when loading properties from the user configuration file).
      */
     private String id;
 
     /**
      * The file dialog accessory to define the range of instances.
      */
     private LoadAccessory accessory;
 
 
     /**
      * A CorpusLoader.Listener listens to events of this loader.
      */
     public static interface Listener {
         /**
          * Called when a new corpus is added.
          *
          * @param corpus the corpus that was added.
          * @param src    the loader which added the corpus.
          */
         void corpusAdded(final List<NLPInstance> corpus, final CorpusLoader src);
 
         /**
          * Called when a corpus is removed.
          *
          * @param corpus the corpus which was removed.
          * @param src    the loader which removed the corpus.
          */
         void corpusRemoved(final List<NLPInstance> corpus, final CorpusLoader src);
 
         /**
          * Called when a corpus is selected.
          *
          * @param corpus the selected corpus.
          * @param src    the loader which selected the corpus.
          */
         void corpusSelected(final List<NLPInstance> corpus, final CorpusLoader src);
     }
 
     /**
      * Adds a listener to this loader.
      *
      * @param changeListener the listener to add.
      */
     public void addChangeListener(final Listener changeListener) {
         changeListeners.add(changeListener);
     }
 
     /**
      * Notifies all listeners that a corpus was added.
      *
      * @param corpus the added corpus.
      */
     private void fireAdded(final List<NLPInstance> corpus) {
         for (Listener listener : changeListeners) {
             listener.corpusAdded(corpus, this);
         }
     }
 
     /**
      * Notifies all listeners that a corpus was removed.
      *
      * @param corpus the removed corpus.
      */
     private void fireRemoved(final List<NLPInstance> corpus) {
         for (Listener listener : changeListeners) {
             listener.corpusRemoved(corpus, this);
         }
     }
 
     /**
      * Notifies all listeners that a corpus was selected.
      *
      * @param corpus the selected corpus.
      */
     private void fireSelected(final List<NLPInstance> corpus) {
         for (Listener listener : changeListeners) {
             listener.corpusSelected(corpus, this);
         }
     }
 
     /**
      * Returns the currently selected corpus or null if no corpus is selected.
      *
      * @return the currently selected corpus or null if no corpus is selected.
      */
     public List<NLPInstance> getSelected() {
         return selected == null ? null : Collections.unmodifiableList(selected);
     }
 
     /**
      * The LoadAccessory contains fields to define the first and last instance, allows us to select the format to load and
      * displays an internal format-specific accessory.
      */
     private class LoadAccessory extends JPanel {
         /**
          * The combo box to pick the format from.
          */
         private JComboBox filetypeComboBox;
         /**
          * The spinner to pick the first instance.
          */
         private JSpinner start;
         /**
          * The spinner to pick the last instance.
          */
         private JSpinner end;
         /**
          * The accessories of each format are stored in a card layout of this panel.
          */
         private JPanel accessoryCards;
 
         /**
          * Creates a new LoadAccessory.
          */
         public LoadAccessory() {
             setLayout(new GridBagLayout());
             setBorder(new TitledBorder(new EtchedBorder(), "Parameters"));
             int y = 0;
             add(new JLabel("Format:"), new SimpleGridBagConstraints(y, true));
 
             filetypeComboBox = new JComboBox(new Vector<Object>(formats.values()));
             filetypeComboBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     ((CardLayout) accessoryCards.getLayout()).show(accessoryCards,
                         filetypeComboBox.getSelectedItem().toString());
                 }
             });
             start = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
             start.setPreferredSize(new Dimension(100, (int) start.getPreferredSize().getHeight()));
             end = new JSpinner(new SpinnerNumberModel(200, 0, Integer.MAX_VALUE, 1));
             end.setPreferredSize(new Dimension(100, (int) start.getPreferredSize().getHeight()));
 
             accessoryCards = new JPanel(new CardLayout());
             for (CorpusFormat f : formats.values())
                 accessoryCards.add(f.getAccessory(), f.toString());
             ((CardLayout) accessoryCards.getLayout()).show(accessoryCards, filetypeComboBox.getSelectedItem().toString());
 
             add(filetypeComboBox, new SimpleGridBagConstraints(y++, false));
             add(new JSeparator(), new SimpleGridBagConstraints(0, y++, 2, 1));
             add(accessoryCards, new SimpleGridBagConstraints(0, y++, 2, 1));
             add(new JSeparator(), new SimpleGridBagConstraints(0, y++, 2, 1));
             add(new JLabel("From:"), new SimpleGridBagConstraints(y, true));
             add(start, new SimpleGridBagConstraints(y++, false));
             add(new JLabel("To:"), new SimpleGridBagConstraints(y, true));
             add(end, new SimpleGridBagConstraints(y, false));
         }
 
         /**
          * Gets the currently chosen format.
          *
          * @return the currently chosen CorpusFormat.
          */
         public CorpusFormat getFormat() {
             return (CorpusFormat) filetypeComboBox.getSelectedItem();
         }
 
         /**
          * Gets the index of the first instance.
          *
          * @return the index of the first instance.
          */
         public int getStart() {
             return (Integer) start.getModel().getValue();
         }
 
         /**
          * Gets the index of the last instance.
          *
          * @return the index of the last instance.
          */
         public int getEnd() {
             return (Integer) end.getModel().getValue();
         }
 
     }
 
     /**
      * Adds a CorpusFormat.
      *
      * @param format the format to add.
      */
     public void addFormat(final CorpusFormat format) {
         formats.put(format.getName(), format);
     }
 
     /**
      * Sets the directory to use in the file dialog.
      *
      * @param dir the directory of the file dialog.
      */
     public void setDirectory(final String dir) {
         fileChooser.setCurrentDirectory(new File(dir));
     }
 
     /**
      * gets the directory to use in the file dialog.
      *
      * @return the directory of the file dialog.
      */
     public String getDirectory() {
         return fileChooser.getCurrentDirectory().getPath();
     }
 
     /**
      * Loads the properties of this loader from the properties object.
      *
      * @param properties the properties to load this loader's properties from.
      */
     public void loadProperties(Properties properties) {
         setDirectory(properties.getProperty(property("dir"), ""));
         String formatString = properties.getProperty(property("format"), "TAB-separated");
         if (formatString.equals("CoNLL"))
             formatString = "TAB-separated";
         accessory.filetypeComboBox.setSelectedItem(formats.get(formatString));
         for (CorpusFormat format : formats.values())
             format.loadProperties(properties, id);
     }
 
     /**
      * Returns a qualified version of the given name to be used as keys in {@link Properties} objects.
      *
      * @param name the name to qualify.
      * @return a name qualified using the id of this loader.
      */
     private String property(final String name) {
         return id + "." + name;
     }
 
     /**
      * Saves the properties of this loader to the given Properties object.
      *
      * @param properties the Properties object to store this loader's properties to.
      */
     public void saveProperties(final Properties properties) {
         properties.setProperty(property("dir"), getDirectory());
         properties.setProperty(property("format"), accessory.filetypeComboBox.getSelectedItem().toString());
         for (CorpusFormat format : formats.values())
             format.saveProperties(properties, id);
     }
 
     /**
      * Creates a new CorpusLoader with the given title. The title is used to derive an id from.
      *
      * @param title the title of this CorpusLoader.
      */
     public CorpusLoader(String title) {
         this.id = title.replaceAll(" ", "_").toLowerCase();
         setLayout(new GridBagLayout());
         setBorder(new EmptyBorder(5, 5, 5, 5));
 
         //setBorder(new TitledBorder(new EtchedBorder(), title));
         GridBagConstraints c = new GridBagConstraints();
 
 
         TabFormat tabFormat = new TabFormat();
         TheBeastFormat theBeastFormat = new TheBeastFormat();
 
 
         addFormat(tabFormat);
         addFormat(theBeastFormat);
         addFormat(new LispSExprFormat());
         addFormat(new GaleAlignmentFormat());
         addFormat(new BioNLP2009SharedTaskFormat());
 
         corpora = new ArrayList<List<NLPInstance>>();
         c.gridx = 0;
         c.gridy = 0;
         c.gridwidth = 2;
         add(new JLabel(title), c);
 
         //file list
         c.gridy = 1;
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 0.5;
         c.weighty = 0.5;
         fileNames = new DefaultListModel();
         final JList files = new JList(fileNames);
         files.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 if (files.getSelectedIndex() == -1) {
                     selected = null;
                     remove.setEnabled(false);
                     fireSelected(null);
 
                 } else {
                     selected = corpora.get(files.getSelectedIndex());
                     remove.setEnabled(true);
                     fireSelected(selected);
                 }
             }
         });
         files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane pane = new JScrollPane(files);
         pane.setPreferredSize(new Dimension(150, 100));
         //pane.setMinimumSize(new Dimension(150, 50));
         add(pane, c);
 
         //add files
         fileChooser = new JFileChooser();
         fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         fileChooser.setDialogTitle("Load Corpus");
         accessory = new LoadAccessory();
         fileChooser.setAccessory(accessory);
         c.gridwidth = 1;
         c.gridx = 0;
         c.gridy = 2;
         c.fill = GridBagConstraints.NONE;
         c.weighty = 0;
         JButton add = new JButton("Add");
         add.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int returnVal = fileChooser.showOpenDialog(CorpusLoader.this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     final ProgressMonitor monitor = new ProgressMonitor(CorpusLoader.this,
                         "Loading data", null, 0, accessory.getEnd() - 1);
                     new Thread(new Runnable() {
                         public void run() {
                             CorpusFormat format = new TheBeastFormat();
                             try {
                                 monitor.setProgress(0);
                                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                 format = accessory.getFormat();
                                 format.setMonitor(new CorpusFormat.Monitor() {
                                     public void progressed(int index) {
                                         monitor.setProgress(index);
                                     }
                                 });
                                 List<NLPInstance> corpus = format.load(fileChooser.getSelectedFile(),
                                     accessory.getStart(), accessory.getEnd());
                                 if (corpus.size() == 0)
                                     throw new RuntimeException("No instances in corpus.");
                                monitor.close();
                                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                 corpora.add(corpus);
                                 fileNames.addElement(fileChooser.getSelectedFile().getName());
                                 files.setSelectedIndex(fileNames.size() - 1);
                                 fireAdded(corpus);
                             } catch (FileNotFoundException e1) {
                                 e1.printStackTrace();
                             } catch (IOException e1) {
                                 e1.printStackTrace();
                             } catch (Exception e) {
                                 e.printStackTrace();
                                 JOptionPane.showMessageDialog(CorpusLoader.this,
                                     "<html>Data could not be loaded with the <br><b>" +
                                         format.getLongName() +
                                         "</b> format.\nThis means that either you chose the wrong " +
                                         "format, \nthe format of file you selected is broken, \nor we " +
                                         "made a terrible mistake.", "Corpus format problem",
                                     JOptionPane.ERROR_MESSAGE);
                             }
                         }
                     }).start();
                 }
             }
         });
         add(add, c);
         c.gridx = 1;
         remove = new JButton("Remove");
         remove.setEnabled(false);
         remove.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int index = files.getSelectedIndex();
                 if (index != -1) {
                     fileNames.remove(index);
                     List<NLPInstance> corpus = corpora.remove(index);
                     fireRemoved(corpus);
                     //repaint();
                 }
             }
         });
         add(remove, c);
 
         //setSize(new Dimension(50, 200));
         //setMinimumSize(new Dimension(150, 10));
     }
 
 
 }
