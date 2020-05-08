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
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
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
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import javax.swing.border.TitledBorder;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.text.ParseException;
 
 import com.sun.voip.Recorder;
 
 import com.sun.mc.softphone.common.Utils;
 import com.sun.mc.softphone.common.Console;
 
 import com.sun.mc.softphone.media.MediaManager;
 import com.sun.mc.softphone.media.MediaManagerFactory;
 import com.sun.mc.softphone.media.CallDoneListener;
 
 /**
  * Creates a dialog that prompts for a file for recording
  */
 public class RecordDialog extends JDialog implements CallDoneListener {
 
     private MediaManager mediaManager;
 
     private JTextField recordFilePath;
     
     private JButton recordMicButton;
     private JButton recordSpeakerButton;
     private JButton stopRecordButton;
     private JButton cancelButton;
 
     private ButtonGroup wizardGroup;
     private JTextPane wizardDescription;
     private StyledDocument wizDoc;
     private Style basicStyle;
     private Style boldStyle;
 
     private String recordingType = "au";
 
     private boolean recordingMic;
 
     private static final Console console = Console.getConsole(RecordDialog.class);
 
     /**
      * Class constructor.
      *
      * @param parent the parent of this dialog
      * @param modal if true, this dialog box is modal
      * @param title the title for the dialog
      */
     public RecordDialog(Frame parent, boolean modal) {
         super(parent, false);
         
 	setTitle("Record");
 
 	addComponentListener(new ComponentAdapter () {
 		public void componentHidden(ComponentEvent e) {	
 		}
 
 		public void componentShown(ComponentEvent e) {	
 		}
             });        
 
         addWindowListener(new WindowAdapter () {
                 public void windowClosing(WindowEvent event) {
 		    System.out.println("Window Listener " + event);
 		    setVisible(false);
 		    dispose();
                 }
             });        
 
         createRecordInfoPanel();
         pack();
 
 	mediaManager = MediaManagerFactory.getInstance();
     }
 
     /**
      * Creates the recording info panel.
      */
     void createRecordInfoPanel() {
         Container contentPane = getContentPane();
         GridBagLayout gridBag = new GridBagLayout();
         GridBagConstraints constraints;
         Insets insets;
 
         contentPane.setLayout(gridBag);
         
        recordFilePath = new JTextField(20);
 
         recordFilePath.getDocument().addDocumentListener(
 	    new DocumentListener() {
                 public void changedUpdate(DocumentEvent evt) {
                     recordTextChanged();
                 }
                 public void insertUpdate(DocumentEvent evt) {
                     recordTextChanged();
                 }
                 public void removeUpdate(DocumentEvent evt) {
                     recordTextChanged();
                 }
             });
 
         JLabel recordLabel = new JLabel("Recording File:");
         recordLabel.setDisplayedMnemonic('R');
         recordLabel.setLabelFor(recordFilePath);
         
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, 0, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady        
         gridBag.setConstraints(recordLabel, constraints);
         contentPane.add(recordLabel);
 
         insets = new Insets(12, 7, 0, 12);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             1, 0, 1, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.HORIZONTAL,  // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(recordFilePath, constraints);
         contentPane.add(recordFilePath);
 
         JLabel typeLabel = new JLabel("Recording type:");
 
         insets = new Insets(12, 12, 0, 0);  // top, left, bottom, right
         constraints = new GridBagConstraints(
             0, 2, 1, 1,                     // x, y, width, height
             0.0, 0.0,                       // weightx, weighty
             GridBagConstraints.WEST,        // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         gridBag.setConstraints(typeLabel, constraints);
         //contentPane.add(typeLabel);
 
 	Box wizard = Box.createHorizontalBox();
 	Box choices = Box.createHorizontalBox();
 
 	wizardGroup = new ButtonGroup();
 
 	JRadioButton rb = new JRadioButton(setAudioAction);
 
 	rb.setAlignmentY(0);
 	rb.setSelected(true);
 	choices.add(rb);
 	wizardGroup.add(rb);
 
 	rb = new JRadioButton(setRtpAction);
 	rb.setAlignmentY(0);
 	choices.add(rb);
 	wizardGroup.add(rb);
 
         choices.setPreferredSize(new Dimension(150, 100));
         choices.setMinimumSize(new Dimension(150, 50));
 
         choices.setBorder(BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(), "Recording Type",
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
             1, 2, 1, 1,                     // x, y, width, height
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
             0, 3, 2, 1,                     // x, y, width, height
             1.0, 1.0,                       // weightx, weighty
             GridBagConstraints.CENTER,      // anchor
             GridBagConstraints.NONE,        // fill
             insets,                         // insets
             0, 0);                          // ipadx, ipady
         JPanel buttonPanel = createButtonPanel();
         gridBag.setConstraints(buttonPanel, constraints);
         contentPane.add(buttonPanel);

        String s = Utils.getPreference("com.sun.mc.softphone.gui.LAST_FILE_PLAYED");

        if (s != null && s.length() > 0) {
            recordFilePath.setText(s);
            recordMicButton.setEnabled(true);
            recordSpeakerButton.setEnabled(true);
        } 
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
 
         recordMicButton = new JButton("Record Mic");
         recordMicButton.setEnabled(false);
         recordMicButton.setMnemonic('L');
         recordMicButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    recordingMic = true;
 		    startRecording();
                 }
             });
         panel.add(recordMicButton);
 
         recordSpeakerButton = new JButton("Record Speaker");
         recordSpeakerButton.setEnabled(false);
         recordSpeakerButton.setMnemonic('R');
         recordSpeakerButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    recordingMic = false;
                     startRecording();
                 }
             });
         panel.add(recordSpeakerButton);
 
         stopRecordButton = new JButton("Stop");
         stopRecordButton.setEnabled(false);
         stopRecordButton.setMnemonic('S');
         stopRecordButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    stopRecording();
                 }
             });
         panel.add(stopRecordButton);
 
         cancelButton = new JButton("Cancel");
         cancelButton.setMnemonic('C');
         cancelButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
 		    setVisible(false);
 	            dispose();
                 }
             });
         panel.add(cancelButton);
 
         getRootPane().setDefaultButton(recordMicButton);        
         
         return panel;
     }
 
     private void startRecording() {
         String path = recordFilePath.getText();
 
 	Utils.setPreference("com.sun.mc.softphone.gui.LAST_FILE_RECORDED", path);
 
 	try {
 	    mediaManager.startRecording(path, recordingType, recordingMic, this);
 	} catch (IOException e) {
 	    console.showErrorUI("Unable to start recording!",
                 e.getMessage(), "");
 
 	    return;
 	}
 
         stopRecordButton.setEnabled(true);               
 	recordMicButton.setEnabled(false);
 	recordSpeakerButton.setEnabled(false);
 	recordFilePath.setEnabled(false);
 	cancelButton.setText("Ok");
     }
 
     private void stopRecording() {
 	mediaManager.stopRecording(recordingMic);
 
 	done();
     }
 
     public void callDone() {
 	done();
     }
 
     private void done() {
 	recordFilePath.setEnabled(true);
 	recordMicButton.setEnabled(true);
 	recordSpeakerButton.setEnabled(true);
         stopRecordButton.setEnabled(false);
         cancelButton.setText("Cancel");
     }
 
     private Action setRtpAction =
         new AbstractAction("Rtp") {
             public void actionPerformed(ActionEvent evt) {
                 recordingType = "rtp";
             }
     };
 
     private Action setAudioAction =
         new AbstractAction("Audio") {
             public void actionPerformed(ActionEvent evt) {
                 recordingType = "au";
             }
     };
 
     private void recordTextChanged() {
 	recordMicButton.setEnabled(recordFilePath.getText().length() > 0);
 	recordSpeakerButton.setEnabled(recordFilePath.getText().length() > 0);
     }
     
 }
