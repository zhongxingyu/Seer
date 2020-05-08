 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mc.softphone.gui;
 
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import javax.swing.border.TitledBorder;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 import java.io.IOException;
 
 import com.sun.mc.softphone.common.Console;
 import com.sun.mc.softphone.common.Utils;
 
 import com.sun.mc.softphone.media.MediaManager;
 import com.sun.mc.softphone.media.MediaManagerFactory;
 import com.sun.mc.softphone.media.PacketGeneratorListener;
 import com.sun.mc.softphone.media.Speaker;
 
 import com.sun.voip.JitterManager;
 
 /**
  * Creates a dialog that prompts for a file to play
  */
 public class PlaybackDialog extends JDialog implements PacketGeneratorListener {
 
     private MediaManager mediaManager;
 
     private JTextField playbackFilePath;
     
     private JButton playLocalButton;
     private JButton playRemoteButton;
     private JButton stopButton;
     private JButton cancelButton;
     
     private JSlider speakerBufferSlider;
     private JLabel speakerBufferLabel;
     private int speakerBufferValue;
     
     private JSlider minJitterBufferSlider;
     private JLabel minJitterBufferLabel;
     private int minJitterBufferValue;
     
     private JSlider maxJitterBufferSlider;
     private JLabel maxJitterBufferLabel;
     private int maxJitterBufferValue;
     
     JRadioButton rbCompress;
     JRadioButton rbDuplicate;
     boolean dupLastPacket;
 
     private ButtonGroup wizardGroup;
     private JTextPane wizardDescription;
     private StyledDocument wizDoc;
     private Style basicStyle;
     private Style boldStyle;
 
     private boolean isLocal;
 
     private static final Console console = 
 	Console.getConsole(PlaybackDialog.class);
 
     /**
      * Class constructor.
      *
      * @param parent the parent of this dialog
      * @param modal if true, this dialog box is modal
      */
     public PlaybackDialog(Frame parent, boolean modal) {
         super(parent, false);
         
 	setTitle("Media Player");
 
 	addComponentListener(new ComponentAdapter () {
 		public void componentHidden(ComponentEvent e) {	
 		}
 
 		public void componentShown(ComponentEvent e) {	
 		}
             });        
 
         addWindowListener(new WindowAdapter () {
                 public void windowClosing(WindowEvent event) {
 		    //setVisible(false);
 		    //dispose();
                 }
             });        
 
         mediaManager = MediaManagerFactory.getInstance();
             
         createPlaybackInfoPanel(mediaManager);
         pack();
     }
 
     /**
      * Creates the playback info panel.
      * @param manager the media manager that controls this panel
      */
     void createPlaybackInfoPanel(MediaManager manager) {
         Container contentPane = getContentPane();
         GridBagLayout gridBag = new GridBagLayout();
         GridBagConstraints constraints;
         Insets insets;
 
         contentPane.setLayout(gridBag);
         
 	String s = Utils.getPreference("com.sun.mc.softphone.gui.LAST_FILE_PLAYED");
 	
 	if (s != null && s.length() > 0) {
             playbackFilePath = new JTextField(s, 20);
 	} else {
             playbackFilePath = new JTextField(20);
 	}
 
         playbackFilePath.getDocument().addDocumentListener(
 	    new DocumentListener() {
                 public void changedUpdate(DocumentEvent evt) {
                     playbackTextChanged();
                 }
                 public void insertUpdate(DocumentEvent evt) {
                     playbackTextChanged();
                 }
                 public void removeUpdate(DocumentEvent evt) {
                     playbackTextChanged();
                 }
             });
         
         // define these two sliders at the same time since their inner
         // classes depend on each other
         minJitterBufferSlider = new JSlider(0, 50);   
         maxJitterBufferSlider = new JSlider(0, 50);
         
         // which row to display a component on
         int y = 0;
             
         /* PLAYBACK FILE
          */
         JLabel label = new JLabel("File to play:");
         label.setDisplayedMnemonic('R');
         label.setLabelFor(playbackFilePath);
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, y, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady        
         gridBag.setConstraints(label, constraints);
         contentPane.add(label);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, y++, GridBagConstraints.REMAINDER, 1,  // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(playbackFilePath, constraints);
         contentPane.add(playbackFilePath);
 
         /* SPEAKER BUFFER SIZE
          */
         speakerBufferSlider = new JSlider(0, 600);
         speakerBufferSlider.setMajorTickSpacing(100);
         speakerBufferSlider.setMinorTickSpacing(20);
         speakerBufferSlider.setPaintLabels(true);
         speakerBufferSlider.setPaintTicks(true);
         speakerBufferSlider.setSnapToTicks(true);
         speakerBufferSlider.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent ce) {
                 int value = speakerBufferSlider.getValue();
                 speakerBufferLabel.setText(value + " ms.");
                 if (!speakerBufferSlider.getValueIsAdjusting()) {
                     speakerBufferSliderChanged(value);
                 }
             }
         });
         speakerBufferLabel = new JLabel();
         
         // set initial values
         speakerBufferValue = Utils.getIntPreference(
 	    Speaker.BUFFER_SIZE_PROPERTY, 
 	    Speaker.DEFAULT_BUFFER_SIZE);
 
         speakerBufferSlider.setValue(speakerBufferValue);
         speakerBufferLabel.setText(speakerBufferValue + " ms.");
         
         label = new JLabel("Speaker Buffer Size:");
         label.setDisplayedMnemonic('S');
         label.setLabelFor(speakerBufferSlider);
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, y, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady        
         gridBag.setConstraints(label, constraints);
         contentPane.add(label);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, y, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(speakerBufferSlider, constraints);
         contentPane.add(speakerBufferSlider);
         
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             2, y++, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(speakerBufferLabel, constraints);
         contentPane.add(speakerBufferLabel);
         
         /* MINIMUM JITTER BUFFER SIZE
          */
         minJitterBufferSlider.setMajorTickSpacing(10);
         minJitterBufferSlider.setMinorTickSpacing(1);
         minJitterBufferSlider.setPaintLabels(true);
         minJitterBufferSlider.setPaintTicks(true);
         minJitterBufferSlider.setSnapToTicks(true);
         minJitterBufferSlider.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent ce) {
                 int value = minJitterBufferSlider.getValue();
                 boolean adjusting = minJitterBufferSlider.getValueIsAdjusting();
                 minJitterBufferLabel.setText(value + " packets");
                 
                 // make sure max > min plus space for some packets.
                 if (maxJitterBufferSlider != null &&
                         value > maxJitterBufferSlider.getValue() / 2) 
                 {
                     maxJitterBufferSlider.setValueIsAdjusting(adjusting);
                     maxJitterBufferSlider.setValue(value * 2);
                 }
                 
                 if (!adjusting) {
                     minJitterBufferSliderChanged(value);
             
                     // if we also changed the maximum, notify the other slider
                     if (maxJitterBufferSlider.getValueIsAdjusting()) {
                         maxJitterBufferSlider.setValueIsAdjusting(false);
                     }
                 }
             }
         });
         minJitterBufferLabel = new JLabel();
         
         // set initial values
         //try {
 	    
             minJitterBufferValue = Utils.getIntPreference(
 		"com.sun.mc.softphone.media.MIN_JITTER_BUFFER_SIZE",
 		JitterManager.DEFAULT_MIN_JITTER_BUFFER_SIZE);
 
 	    if (minJitterBufferValue < 0) {
 		minJitterBufferValue = 0;
 	    }
 
             minJitterBufferSlider.setValue(minJitterBufferValue);
             minJitterBufferLabel.setText(minJitterBufferValue + " packets");
         //} catch (IOException ioe) {
         //    ioe.printStackTrace();
         //    minJitterBufferLabel.setText("Read Error");
         //}
         
         label = new JLabel("Min Jitter Buffer Size:");
         label.setDisplayedMnemonic('S');
         label.setLabelFor(minJitterBufferSlider);
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, y, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady        
         gridBag.setConstraints(label, constraints);
         contentPane.add(label);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, y, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(minJitterBufferSlider, constraints);
         contentPane.add(minJitterBufferSlider);
         
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             2, y++, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(minJitterBufferLabel, constraints);
         contentPane.add(minJitterBufferLabel);
         
         /* MAXIMUM JITTER BUFFER SIZE
          */
         maxJitterBufferSlider.setMajorTickSpacing(10);
         maxJitterBufferSlider.setMinorTickSpacing(1);
         maxJitterBufferSlider.setPaintLabels(true);
         maxJitterBufferSlider.setPaintTicks(true);
         maxJitterBufferSlider.setSnapToTicks(true);
         maxJitterBufferSlider.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent ce) {
                 int value = maxJitterBufferSlider.getValue();
                 boolean adjusting = maxJitterBufferSlider.getValueIsAdjusting();
                 maxJitterBufferLabel.setText(value + " packets");
                 
                 // make sure max > min
                 if (minJitterBufferSlider != null &&
                         value < minJitterBufferSlider.getValue() * 2) 
                 {
 		    
                     minJitterBufferSlider.setValueIsAdjusting(adjusting);
                     minJitterBufferSlider.setValue(value / 2);
                 }
                 
                 if (!adjusting) {
                     maxJitterBufferSliderChanged(value);
             
                     // if we also changed the minimum, notify the other slider
                     if (minJitterBufferSlider.getValueIsAdjusting()) {
                         minJitterBufferSlider.setValueIsAdjusting(false);
                     }
                 }
             }
         });
         maxJitterBufferLabel = new JLabel();
         
         // set initial values
         //try {
             maxJitterBufferValue = Utils.getIntPreference(
 		"com.sun.mc.softphone.media.MAX_JITTER_BUFFER_SIZE",
 		JitterManager.DEFAULT_MAX_JITTER_BUFFER_SIZE);
 
             maxJitterBufferSlider.setValue(maxJitterBufferValue);
             maxJitterBufferLabel.setText(maxJitterBufferValue + " packets");
         //} catch (IOException ioe) {
         //    ioe.printStackTrace();
         //    maxJitterBufferLabel.setText("Read Error");
         //}
         
         label = new JLabel("Max Jitter Buffer Size:");
         label.setDisplayedMnemonic('S');
         label.setLabelFor(maxJitterBufferSlider);
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, y, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady        
         gridBag.setConstraints(label, constraints);
         contentPane.add(label);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, y, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(maxJitterBufferSlider, constraints);
         contentPane.add(maxJitterBufferSlider);
         
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             2, y++, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(maxJitterBufferLabel, constraints);
         contentPane.add(maxJitterBufferLabel);
         
         Box wizard = Box.createHorizontalBox();
         Box choices = Box.createHorizontalBox();
 
         wizardGroup = new ButtonGroup();
 
 	String dup = Utils.getPreference(
 	    "com.sun.mc.softphone.media.DUP_LAST_PACKET");
 
         rbCompress = new JRadioButton(setCompressAction);
         rbCompress.setAlignmentY(0);
 
 	if (dup == null || dup.equalsIgnoreCase("true") == false) {
             rbCompress.setSelected(true);
 	}
 
         choices.add(rbCompress);
         wizardGroup.add(rbCompress);
 
         rbDuplicate = new JRadioButton(setDuplicateAction);
         rbDuplicate.setAlignmentY(0);
 
 	if (dup != null && dup.equalsIgnoreCase("true")) {
 	    rbDuplicate.setSelected(true);
 	}
 
         choices.add(rbDuplicate);
         wizardGroup.add(rbDuplicate);
 
         rbCompress.setEnabled(true);
         rbDuplicate.setEnabled(true);
 
         choices.setPreferredSize(new Dimension(150, 100));
         choices.setMinimumSize(new Dimension(150, 50));
 
         choices.setBorder(BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), "Packet Loss Handling",
                 TitledBorder.DEFAULT_JUSTIFICATION,
                 TitledBorder.TOP));
 
         wizardDescription = new JTextPane();
         wizDoc = wizardDescription.getStyledDocument();
         Style def = StyleContext.getDefaultStyleContext().
                         getStyle(StyleContext.DEFAULT_STYLE);
         basicStyle = wizDoc.addStyle("basic", def);
         boldStyle = wizDoc.addStyle("bold", basicStyle);
         StyleConstants.setBold(boldStyle, true);
         wizardDescription.setPreferredSize(new Dimension(150, 160));
         wizardDescription.setAlignmentX(0.5f);
         wizardDescription.setFont(new Font("Sans-serif", Font.PLAIN, 12));
         wizardDescription.setEditable(false);
         wizardDescription.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
         wizardDescription.setOpaque(false);
 
         choices.setAlignmentX(0.5f);
 
         wizard.add(choices);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, y++, 1, 1,                   // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(wizard, constraints);
         contentPane.add(wizard);
 
         /* BUTTON PANEL
          */
         insets = new Insets(12, 12, 12, 12); // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, y++, 2, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.CENTER,      // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         JPanel buttonPanel = createButtonPanel();
         gridBag.setConstraints(buttonPanel, constraints);
         contentPane.add(buttonPanel);
 
 	playLocalButton.setEnabled(playbackFilePath.getText().length() > 0);
 	playRemoteButton.setEnabled(playbackFilePath.getText().length() > 0);
     }
 
     /**
      * Creates the panel of buttons that goes along the bottom
      * of the dialog.
      *
      * @return a panel of buttons
      */
     JPanel createButtonPanel() {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, 0));
 
         playLocalButton = new JButton("Local");
         playLocalButton.setEnabled(false);
         playLocalButton.setMnemonic('L');
         playLocalButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    isLocal = true;
 		    startPlaying();
                 }
             });
         panel.add(playLocalButton);
 
         playRemoteButton = new JButton("Remote");
         playRemoteButton.setEnabled(false);
         playRemoteButton.setMnemonic('R');
         playRemoteButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    isLocal = false;
 		    startPlaying();
                 }
             });
         panel.add(playRemoteButton);
 
 
         stopButton = new JButton("Stop");
         stopButton.setEnabled(false);
         stopButton.setMnemonic('S');
         stopButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    stopPlaying();
                 }
             });
         panel.add(stopButton);
 
         cancelButton = new JButton("Cancel");
         cancelButton.setMnemonic('C');
         cancelButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    setVisible(false);
 	            dispose();
                 }
             });
         panel.add(cancelButton);
 
         getRootPane().setDefaultButton(playLocalButton);        
         
         return panel;
     }
 
     private void playbackTextChanged() {
 	playLocalButton.setEnabled(playbackFilePath.getText().length() > 0);
 	playRemoteButton.setEnabled(playbackFilePath.getText().length() > 0);
     }
 
     private void startPlaying() {
         String path = playbackFilePath.getText();
 
 	Utils.setPreference("com.sun.mc.softphone.gui.LAST_FILE_PLAYED",
 	    playbackFilePath.getText());
 
 	try {
 	    mediaManager.playRecording(path, isLocal, this);
 	} catch (IOException e) {
 	    console.showErrorUI("Invalid File to play!", 
 		e.getMessage(), ""); 
 	    return;
 	}
 
         stopButton.setEnabled(true);
         playLocalButton.setEnabled(false);
         playRemoteButton.setEnabled(false);
         playbackFilePath.setEnabled(false);
         cancelButton.setText("Ok");
     }
 
     private void stopPlaying() {
 	mediaManager.stopPlaying(isLocal);
 
 	done();
     }
 
     public void packetGeneratorDone() {
 	done();
     }
 
     private void done() {
 	playbackFilePath.setEnabled(true);
         playLocalButton.setEnabled(true);
         playRemoteButton.setEnabled(true);
         stopButton.setEnabled(false);
         cancelButton.setText("Cancel");
     }
 
     /**
      * Called when the speaker buffer slider changes.
      * @param value the value it changed to
      */
     private void speakerBufferSliderChanged(int value) {
         // avoid duplicate calls
         if (value == speakerBufferValue) {
             return;
         }
         speakerBufferValue = value;
         
         // change the buffer size
         //System.out.println("Speaker buffer size: " + value);
 
 	Utils.setPreference(
 	    "com.sun.mc.softphone.media.SPEAKER_BUFFER_SIZE",
 	    String.valueOf(value));
     }
     
     /**
      * Called when the minimum jitter buffer slider changes.
      * @param value the value it changed to
      */
     private void minJitterBufferSliderChanged(int value) {
         // avoid duplicate calls
         if (value == minJitterBufferValue) {
             return;
         }
         minJitterBufferValue = value;
         
         // change the buffer size
         //System.out.println("Minimum jitter buffer size: " + value);
 
 	Utils.setPreference(
 	    "com.sun.mc.softphone.media.MIN_JITTER_BUFFER_SIZE",
 	    String.valueOf(value));
     }
     
     /**
      * Called when the minimum jitter buffer slider changes.
      * @param value the value it changed to
      */
     private void maxJitterBufferSliderChanged(int value) {
         // avoid duplicate calls
         if (value == maxJitterBufferValue) {
             return;
         }
      
         maxJitterBufferValue = value;
         
         // change the buffer size
         //System.out.println("Maximum jitter buffer size: " + value);
 
 	Utils.setPreference(
 	    "com.sun.mc.softphone.media.MAX_JITTER_BUFFER_SIZE",
 	    String.valueOf(value));
     }
     
     private Action setCompressAction =
         new AbstractAction("Compress ") {
             public void actionPerformed(ActionEvent evt) {
 		Utils.setPreference(
 		    "com.sun.mc.softphone.media.DUP_LAST_PACKET", "false");
             }
     };
 
     private Action setDuplicateAction =
         new AbstractAction("Duplicate") {
             public void actionPerformed(ActionEvent evt) {
 		Utils.setPreference(
 		    "com.sun.mc.softphone.media.DUP_LAST_PACKET", "true");
             }
     };
 
     // for testing
     public static void main(String[] args) {
         PlaybackDialog dialog = new PlaybackDialog(null, false);
         dialog.setVisible(true);
     }
 
 }
