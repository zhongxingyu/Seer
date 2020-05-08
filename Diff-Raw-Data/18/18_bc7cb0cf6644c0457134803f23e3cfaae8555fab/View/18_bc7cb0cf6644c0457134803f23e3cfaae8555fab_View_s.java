 package view;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import model.GFFParser.ParseException;
 
 import controller.Controller;
 
 @SuppressWarnings("serial")
 public class View extends JDialog {
    protected final int DIALOG_HEIGHT = 400, DIALOG_WIDTH = 500;
 
    // Selected tab "enum"
    protected final int GC_CONTENT_TAB = 0, CALCULATIONS_TAB = 1, PROTEINS_TAB = 2, FIND_REPEATS_TAB = 3;
 
    protected Controller controller;
 
    protected Container mPane;
 
    protected Box mSequenceFileBox;
    protected JTextField mSequenceFile;
    protected boolean mValidSequenceFile;
    protected JButton mBrowseSequenceButton;
 
    protected Box mGffFileBox;
    protected JTextField mGffFile;
    protected boolean mValidGffFile;
    protected JButton mBrowseGffButton;
 
    protected JTabbedPane mTabbedPane;
    protected GCContentInfoPanel mGcContentInfoPanel;
    protected CalculationsPanel mCalculationsPanel;
    protected ProteinsPanel mProteinsPanel;
    protected FindRepeatsPanel mFindRepeatsPanel;
 
    protected JButton mRunButton, mSaveButton, mQuitButton;
 
    public View(final Controller controller) {
       this.controller = controller;
 
       setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
       setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
       setResizable(false);
       setLocationRelativeTo(null);
 
       mPane = getContentPane();
       mPane.setLayout(new BoxLayout(mPane, BoxLayout.Y_AXIS));
       mPane.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
 
       // Took this out of initializeControlsBox() for now, since the tabbed pane
       // change listener needs to update it. Will refactor later.
       mRunButton = new JButton("Run");
 
       initializeSequenceBox(); // Box is member variable (for hiding)
       initializeGffBox(); // Box is member variable (for hiding)
       initializeTabbedPane(); // Tabbed pane is member variable
       Box controlsBox = initializeControlsBox();
 
       mPane.add(mSequenceFileBox);
       mPane.add(mGffFileBox);
       mPane.add(mTabbedPane);
       mPane.add(controlsBox);
       mPane.validate();
    }
 
    protected void initializeSequenceBox() {
       mSequenceFile = new JTextField(20);
       mSequenceFile.setEditable(false);
       mValidSequenceFile = false;
 
       mBrowseSequenceButton = new JButton("Browse");
       mBrowseSequenceButton.addActionListener(browseSequenceButtonActionListener);
 
       mSequenceFileBox = Box.createHorizontalBox();
       mSequenceFileBox.add(new JLabel("Sequence File:"));
       mSequenceFileBox.add(mSequenceFile);
       mSequenceFileBox.add(mBrowseSequenceButton);
    }
 
    protected void initializeGffBox() {
       mGffFile = new JTextField(20);
       mGffFile.setEditable(false);
       mValidGffFile = false;
 
       mBrowseGffButton = new JButton("Browse");
       mBrowseGffButton.addActionListener(browseGffButtonActionListener);
 
       mGffFileBox = Box.createHorizontalBox();
       mGffFileBox.add(new JLabel("GFF File:"));
       mGffFileBox.add(mGffFile);
       mGffFileBox.add(mBrowseGffButton);
       mGffFileBox.setVisible(false);
    }
 
    protected void initializeTabbedPane() {
       mGcContentInfoPanel = new GCContentInfoPanel();
       mCalculationsPanel = new CalculationsPanel();
       mProteinsPanel = new ProteinsPanel();
       mFindRepeatsPanel = new FindRepeatsPanel();
 
       mTabbedPane = new JTabbedPane();
       mTabbedPane.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
             updateRunButton();
 
             switch (mTabbedPane.getSelectedIndex()) {
             case GC_CONTENT_TAB:
                mSequenceFileBox.setVisible(true);
                mGffFileBox.setVisible(false);
                break;
             case FIND_REPEATS_TAB:
             case CALCULATIONS_TAB:
             case PROTEINS_TAB:
                mSequenceFileBox.setVisible(true);
                mGffFileBox.setVisible(true);
                break;
             default:
                assert false;
             }
          }
       });
 
       mTabbedPane.addTab("GC Content", mGcContentInfoPanel);
       mTabbedPane.addTab("Calculations", mCalculationsPanel);
       mTabbedPane.addTab("Proteins", mProteinsPanel);
       mTabbedPane.addTab("Find Repeats", mFindRepeatsPanel);
    }
 
    protected Box initializeControlsBox() {
       mRunButton.setEnabled(false);
       mRunButton.addActionListener(runButtonActionListener);
 
       mSaveButton = new JButton("Save");
       mSaveButton.addActionListener(saveButtonActionListener);
 
       mQuitButton = new JButton("Quit");
       mQuitButton.addActionListener(quitButtonActionListener);
 
       Box box = Box.createHorizontalBox();
       box.add(mRunButton);
       box.add(mSaveButton);
       box.add(mQuitButton);
       box.setAlignmentX(Component.CENTER_ALIGNMENT);
 
       return box;
    }
 
    /**
     * The Run button is enabled conditionally on valid Sequence/GFF files. 
     * GC Content: Requires valid sequence file
     * Calculations: Requires valid GFF file
     * Proteins: Requires both
     */
    protected void updateRunButton() {
       switch (mTabbedPane.getSelectedIndex()) {
       case GC_CONTENT_TAB:
          mRunButton.setEnabled(mValidSequenceFile);
          break;
       case FIND_REPEATS_TAB:
       case CALCULATIONS_TAB:
       case PROTEINS_TAB:
          mRunButton.setEnabled(mValidSequenceFile && mValidGffFile);
          break;
       default:
          assert false;
       }
    }
 
    // Action listeners ////////////////////////////////////////////////////////
 
    protected ActionListener browseSequenceButtonActionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser();
          int returnVal = chooser.showOpenDialog(chooser);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
             String filename = chooser.getSelectedFile().getAbsolutePath();
             mSequenceFile.setText(filename);
 
             try {
                controller.useSequenceFile(mSequenceFile.getText());
                mValidSequenceFile = true;
                updateRunButton();
             } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, ex.toString(), "Error",
                      JOptionPane.ERROR_MESSAGE);
                mValidSequenceFile = false;
                updateRunButton();
                return;
             }
          }
       }
    };
 
    protected ActionListener browseGffButtonActionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser();
          int returnVal = chooser.showOpenDialog(chooser);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
             String filename = chooser.getSelectedFile().getAbsolutePath();
             mGffFile.setText(filename);
 
             try {
                controller.useGffFile(mGffFile.getText());
                mValidGffFile = true;
                updateRunButton();
             } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "Error",
                      JOptionPane.ERROR_MESSAGE);
                mValidGffFile = false;
                updateRunButton();
                return;
             } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "Error",
                      JOptionPane.ERROR_MESSAGE);
                mValidGffFile = false;
                updateRunButton();
                return;
             }
          }
       }
    };
 
    protected ActionListener runButtonActionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          if (mSequenceFile.getText().equals("")) {
             JOptionPane.showMessageDialog(null, "No FASTA file was selected", "Invalid File",
                   JOptionPane.ERROR_MESSAGE);
             return;
          }
 
          switch (mTabbedPane.getSelectedIndex()) {
          case GC_CONTENT_TAB:
             runGCContent();
             break;
          case CALCULATIONS_TAB:
             runCalculations();
             break;
          case PROTEINS_TAB:
             runProteins();
             break;
          case FIND_REPEATS_TAB:
             runFindRepeats();
             break;
          default:
             assert false;
          }
       }
    };
 
    protected void runGCContent() {
       mGcContentInfoPanel.setDisplay(controller.getGcContent(mGcContentInfoPanel.getStartPos(),
             mGcContentInfoPanel.getEndPos(), mGcContentInfoPanel.getUseSlidingWindow(),
             mGcContentInfoPanel.getWinSize(), mGcContentInfoPanel.getShiftIncr()));
 
    }
 
    protected void runCalculations() {
       mCalculationsPanel.setNucleotides(controller.getNucleotides());
       mCalculationsPanel.setGenes(controller.getGenes());
       mCalculationsPanel.setIsoforms(controller.getIsoforms());
       mCalculationsPanel.setAvgGene(controller.avgGeneSize());
       mCalculationsPanel.setAvgCds(controller.avgCdsSize());
       mCalculationsPanel.setAvgExon(controller.avgExonSize());
       mCalculationsPanel.setAvgIntron(controller.avgIntronSize());
       mCalculationsPanel.setAvgIntergenic(controller.avgIntergenicRegionSize());
       mCalculationsPanel.setGeneDensity(controller.geneDensity());
       mCalculationsPanel.setCdsDensity(controller.cdsDensity());
       mCalculationsPanel.setGenesPerKilobase(controller.genesPerKilobase());
       mCalculationsPanel.setKilobasesPerGene(controller.kilobasesPerGene());
    }
 
    protected void runProteins() {
       mProteinsPanel.setDisplay(controller.getProteins());
    }
 
    protected void runFindRepeats() {
       String maxDistanceFromStartText = mFindRepeatsPanel
             .getMaximumDistanceToStartText();
       int maxDistanceFromStart = 0;
       if (maxDistanceFromStartText.isEmpty()) {
          maxDistanceFromStart = -1;
       } else {
          try {
             maxDistanceFromStart = Integer.parseInt(maxDistanceFromStartText);
          } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(null,
                   "Max distance to next mRNA start must be a number.", "Error",
                   JOptionPane.ERROR_MESSAGE);
             return;
          }
       }
       if (mFindRepeatsPanel.isMatchExactString()) {
          // Find incidences of exact string match
         controller.matchString(mFindRepeatsPanel.getMatchStringText(),
               maxDistanceFromStart);
       } else {
          String minRepeatLengthText = mFindRepeatsPanel.getMinimumLengthText();
          int minRepeatLength = 0;
          if (!minRepeatLengthText.isEmpty()) {
             try {
                minRepeatLength = Integer.parseInt(minRepeatLengthText);
             } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                      "Minimum length must be a number.", "Error",
                      JOptionPane.ERROR_MESSAGE);
                return;
             }
          }
          // Find all indices of repeats that meat filter criteria.
          mFindRepeatsPanel.setDisplay(controller.getRepeats(minRepeatLength,
                maxDistanceFromStart));
       }
    }
 
    protected ActionListener saveButtonActionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          switch (mTabbedPane.getSelectedIndex()) {
          case GC_CONTENT_TAB:
             saveGcContent();
             break;
          case CALCULATIONS_TAB:
             saveCalculations();
             break;
          case PROTEINS_TAB:
             saveProteins();
             break;
          default:
             assert false;
          }
       }
    };
 
    protected void saveGcContent() {
       saveString(mGcContentInfoPanel.getDisplay());
    }
 
    protected void saveCalculations() {
       StringBuilder sb = new StringBuilder();
       
       for (String key : mCalculationsPanel.getKeys()) {
          sb.append(key);
          sb.append(",");
       }
       sb.append("\n");
 
       for (String value : mCalculationsPanel.getValues()) {
          sb.append(value);
          sb.append(",");
       }
       sb.append("\n");
       
       saveString(sb.toString());
    }
 
    protected void saveProteins() {
       saveString(mProteinsPanel.getDisplay());
    }
 
    protected void saveString(String data) {
       if (data.equals("")) {
          JOptionPane.showMessageDialog(null, "No output to save", "Empty output",
                JOptionPane.ERROR_MESSAGE);
          return;
       }
 
       JFileChooser chooser = new JFileChooser();
       int ret = chooser.showSaveDialog(mPane);
 
       if (ret == JFileChooser.APPROVE_OPTION) {
          try {
             FileWriter writer = new FileWriter(chooser.getSelectedFile());
             writer.write(data);
             writer.close();
          } catch (java.io.IOException ioErr) {
             JOptionPane.showMessageDialog(null, "Encountered unknown error when saving output",
                   "Unable to save output", JOptionPane.ERROR_MESSAGE);
          }
       } else if (ret == JFileChooser.ERROR_OPTION) {
          JOptionPane.showMessageDialog(null, "Encountered unknown error when saving output",
                "Unable to save output", JOptionPane.ERROR_MESSAGE);
       }
    }
 
    protected ActionListener quitButtonActionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
          dispose(); // closes the dialog window
          return;
       }
    };
 }
