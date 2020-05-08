 package core;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.LayoutStyle;
 import javax.swing.WindowConstants;
 
 import abc.midi.TunePlayer;
 import abc.notation.Tune;
 import abc.parser.TuneBook;
 import abc.ui.swing.JScoreComponent;
 
 /**
  * 
  * @author davidjones
  *
  */
 public class GUI extends javax.swing.JFrame
 {
 	private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());
 	
     public static JLabel contentPane;
     public static List<String> wavFiles;
     public static List<String> abcFiles;
     public static JComboBox fileList;
     public static JCheckBox isAudio;
     public static boolean audio;
     public static boolean sheet;
     public static JCheckBox isSheet;
     public static JLabel audioLabel;
     public static JLabel sheetLabel;
     public static JTextArea processingDetails;
     private static JButton transcribeButton;
     private static JButton displayButton;
     private static JTextArea tuneOutput;
     private static JLabel jLabel1;
     private static JLabel jLabel2;
     public static JScrollPane jScrollPane1;
     private static JScrollPane tunePane;
     private static JLabel titleLabel;
     private static JComboBox mode;
     private static ImageIcon a = new ImageIcon("Images/Music-icon.png");
 
     public static String directory = "";
 
     /**
      * 
      * @throws FileNotFoundException
      * @throws IOException
      */
     public GUI() throws FileNotFoundException, IOException
     {
     	
     	String OS = System.getProperty("os.name").toLowerCase();
     	// Set directory depending on OS       
 		if (OS.indexOf("win") >= 0) {
 			directory = "C:\\Users\\David\\git\\AMT-Desktop\\AMT\\src\\testmedia";
 		} else if (OS.indexOf("mac") >= 0) {
 			directory = "/Users/davidjones/Automatic Music Transcription/src/examples/Media";
 		} 
     	
         initComponents();
 
         abcFiles = FileRetriever.getFiles(directory);
         for(int i = 0; i < abcFiles.size(); i++)
         {
             if(!abcFiles.get(i).endsWith(".abc"))
             {
                 abcFiles.remove(i);
                 i--;
             }
         }
     }
 
     /**
      * 
      * @throws FileNotFoundException
      * @throws IOException
      */
     private void initComponents() throws FileNotFoundException, IOException
     {
         titleLabel = new JLabel();
         jScrollPane1 = new JScrollPane();
         processingDetails = new JTextArea();
         fileList = new JComboBox();
         mode = new JComboBox();
         transcribeButton = new JButton();
         displayButton = new JButton();
         tunePane = new JScrollPane();
         tuneOutput = new JTextArea();
         jLabel1 = new JLabel();
         jLabel2 = new JLabel();
         contentPane = new JLabel();
         isAudio = new JCheckBox();
         isSheet = new JCheckBox();
         audioLabel = new JLabel("Play audio upon completion.");
         sheetLabel = new JLabel("Display sheet music upon completion.");
         sheet = false;
         audio = false;
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Automatic Music Transcription");
 
         titleLabel.setText("Pick your file:");
 
         processingDetails.setColumns(20);
         processingDetails.setEditable(false);
         processingDetails.setRows(5);
         jScrollPane1.setViewportView(processingDetails);
 
         wavFiles = FileRetriever.getFiles(directory);
         for(int i = 0; i < wavFiles.size(); i++)
         {
             if(!wavFiles.get(i).endsWith(".wav"))
             {
                 wavFiles.remove(i);
                 i--;
             }
         }
 
         fileList.setModel(new DefaultComboBoxModel(wavFiles.toArray()));
         List<String> modes = new ArrayList<String>();
         modes.add("Pure Tone");
         modes.add("Natural Instrument");
 
         mode.setModel(new DefaultComboBoxModel(modes.toArray()));
 
         isAudio.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(final ItemEvent ie) {
                 final int state = ie.getStateChange();
                 if(state == ItemEvent.SELECTED){
                     audio = true;
                 }
                 else if(state == ItemEvent.DESELECTED){
                     audio = false;
                 }
             }
         });
 
         isSheet.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(final ItemEvent ie) {
                 final int state = ie.getStateChange();
                 if(state == ItemEvent.SELECTED){
                     sheet = true;
                 }
                 else if(state == ItemEvent.DESELECTED){
                     sheet = false;
                 }
             }
         });
 
         displayButton.setText("Display Sheet Music");
         displayButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(final ActionEvent evt)
             {
                 try {
                     displayButtonActionPerformed(evt);
                 } catch (FileNotFoundException ex) {
                     Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (IOException ex) {
                     Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
         transcribeButton.setText("Transcribe");
         transcribeButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(final ActionEvent evt)
             {
                 try
                 {
                     try
                     {
                         try {
                             transcribeButtonActionPerformed(evt);
                         } catch (MidiUnavailableException ex) {
                             Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     } 
                     catch (UnsupportedAudioFileException ex)
                     {
                         Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } 
                 catch (IOException ex)
                 {
                     Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });

        final ImageIcon one = new ImageIcon("Images/Sheet-Music.jpg");
 
         contentPane.setIcon(one);
         contentPane.setLayout(new BorderLayout());
         add(contentPane);
         setContentPane(contentPane);
 
        //tunePane.setViewportView(TuneOutput);
         jLabel1.setText("Processing Information:");
 
         jLabel2.setText("Select Input Type:");
         final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                     .addComponent(jLabel2)
                     .addComponent(mode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                     .addComponent(displayButton)
                     .addGap(46, 46, 46))
                 .addGroup(layout.createSequentialGroup()
                     .addComponent(titleLabel)
                     .addContainerGap()))
             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                     .addComponent(isAudio)
                     .addComponent(audioLabel)))
             .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                     .addComponent(isSheet)
                     .addComponent(sheetLabel)))
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addContainerGap(292, Short.MAX_VALUE))
             .addGroup(layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(fileList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                         .addComponent(transcribeButton)
                         .addGap(46, 46, 46))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(titleLabel)
                         .addContainerGap())))
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addContainerGap(325, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(titleLabel)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(transcribeButton)
                     .addComponent(fileList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addGap(3, 3, 3)
                 .addComponent(jLabel1)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                 .addGap(6, 6, 6)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(displayButton)
                                                        .addComponent(jLabel2)

                    .addComponent(mode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
)
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(isAudio)
                     .addComponent(audioLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                     .addComponent(isSheet)
                     .addComponent(sheetLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
         );
        setResizable(true);
         pack();
     }
 
     /**
      * 
      * @param evt
      * @throws FileNotFoundException
      * @throws IOException
      */
     private void displayButtonActionPerformed(final ActionEvent evt) throws FileNotFoundException, IOException{
         final Object[] options = abcFiles.toArray();
         final String displayString = (String)JOptionPane.showInputDialog(
                     this,
                     "Please choose sheet music to display",
                     "Choose Sheet Music",
                     JOptionPane.PLAIN_MESSAGE,
                     a,
                     options,
                     "ham");
 
         
         JScoreComponent score = AbcOutput.getMusic(new File(directory + "/" + displayString));
         score.setJustification(true);
         JFrame myFrame = new JFrame();
         myFrame.add(tunePane);
         tuneOutput.setColumns(score.getWidth());
         tuneOutput.setEditable(false);
         tuneOutput.setRows(score.getHeight());
         tuneOutput.add(score);
         tunePane.setViewportView(tuneOutput);
         myFrame.pack();
         myFrame.setVisible(true);
 
     }
 
     private void transcribeButtonActionPerformed(final ActionEvent evt) throws IOException, UnsupportedAudioFileException, MidiUnavailableException
     {
         boolean cont = false;
         transcribeButton.setEnabled(false);
 
         String name= JOptionPane.showInputDialog(this, "What would you like to name your transcription?");
         if(abcFiles.contains(name + ".abc"))
         {
             int result = JOptionPane.showConfirmDialog(this, "This file name already exists, are you sure you want to proceed?");
             if(result == JOptionPane.YES_OPTION)
             {
             	LOGGER.fine("user selected YES");
             	cont = true;
             }
             else if(result == JOptionPane.NO_OPTION)
             {
             	LOGGER.fine("user selected NO");
             }
             else if(result == JOptionPane.CANCEL_OPTION)
             {
             	LOGGER.fine("user selected CANCEL");
             }
 
         }
         else
         {
             cont = true;
         }
         if(mode.getSelectedItem().equals("Natural Instrument") && cont)
         {
             processingDetails.append("Now transcribing: " + fileList.getSelectedItem().toString() + "\n");
             File myAudio = new File(directory + "/" + fileList.getSelectedItem().toString());
             String totalTune = IncProcessor.getTune(myAudio);
             List<String> finalAbc = AbcManager.convertToAbc(totalTune);
             // Perform track smoothing on finalAbc
            // finalAbc = CorrectDisturbance.smoothTrack(finalAbc);
             AbcManager.createAbcFile(finalAbc, name);
             
             if(sheet)
             {
                 JScoreComponent score = AbcOutput.getMusic(new File(directory + "/" + name +".abc"));
                 score.setJustification(true);
                 JFrame myFrame = new JFrame();
                 myFrame.add(tunePane);
                 tuneOutput.setColumns(score.getWidth());
                 tuneOutput.setEditable(false);
                 tuneOutput.setRows(score.getHeight());
                 tuneOutput.add(score);
                 tunePane.setViewportView(tuneOutput);
                 myFrame.pack();
                 myFrame.setVisible(true);
             }
 
             File abcFile = new File(directory + "/" + name +".abc");
             //creates a tunebook from the previous file
             TuneBook book = new TuneBook(abcFile);
             //retrieves the first tune notation of the test.abc file
             Tune tune = book.getTune(0);
             //creates a midi player to play tunes
             TunePlayer player = new TunePlayer();
             //starts the player and play the tune
             player.start();
             player.play(tune);
         }
         else if(mode.getSelectedItem().equals("Pure Tone") && cont)
         {
             File myAudio = new File(directory + "/" + fileList.getSelectedItem().toString());
             String totalTune = PureProcessor.getTune(myAudio);
             List<String> finalAbc = AbcManager.convertToAbc(totalTune);
             //finalAbc = CorrectDisturbance.smoothTrack(finalAbc);
             AbcManager.createAbcFile(finalAbc, name);
 
             if(sheet)
             {
                 JScoreComponent score = AbcOutput.getMusic(new File(directory + "/" + name +".abc"));
                 score.setJustification(true);
                 JFrame myFrame = new JFrame();
                 myFrame.add(tunePane);
                 tuneOutput.setColumns(score.getWidth());
                 tuneOutput.setEditable(false);
                 tuneOutput.setRows(score.getHeight());
                 tuneOutput.add(score);
                 tunePane.setViewportView(tuneOutput);
                 myFrame.setTitle(name);
 
                 myFrame.pack();
                 myFrame.setVisible(true);
             }
 
 
             File abcFile = new File(directory + "/" + name +".abc");
             //creates a tunebook from the previous file
             TuneBook book = new TuneBook(abcFile);
             //retrieves the first tune notation of the test.abc file
             Tune tune = book.getTune(0);
             //creates a midi player to play tunes
             TunePlayer player = new TunePlayer();
             //starts the player and play the tune
             player.start();
             player.play(tune);
         }
         
         abcFiles = FileRetriever.getFiles(directory);
         for(int i = 0; i < abcFiles.size(); i++)
         {
             if(!abcFiles.get(i).endsWith(".abc"))
             {
                 abcFiles.remove(i);
                 i--;
             }
         }
         
         fileList.setModel(new DefaultComboBoxModel(wavFiles.toArray()));
         transcribeButton.setEnabled(true);
 
         JOptionPane.showMessageDialog(this, "Transcription Complete", "Success!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("tick_icon.png"));
     }
 }
