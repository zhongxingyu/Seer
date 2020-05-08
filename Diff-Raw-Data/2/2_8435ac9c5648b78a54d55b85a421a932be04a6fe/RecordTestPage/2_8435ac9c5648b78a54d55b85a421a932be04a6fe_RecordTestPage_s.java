 package com.core.test;
 
 import com.core.util.*;
 import com.core.lesson.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import org.json.JSONObject;
 
 
 public class RecordTestPage extends IntroPage implements EventListener{
     /**
      * The String-based action command for the 'Next' button.
      */
     public static final String RECORD_BUTTON_ACTION_COMMAND = "RecordButtonActionCommand";
     public static final String STOP_BUTTON_ACTION_COMMAND = "StopButtonActionCommand";
     public static final String PLAY_BUTTON_ACTION_COMMAND = "PlayButtonActionCommand";
 
     private JLabel blankSpace;
 
     private JButton recordButton;
     private JButton stopButton;
     private JButton playButton;
 
     private JEditorPane introArea;
     private JPanel contentPanel;
 
     private RecordPlay recorder;
 
     public RecordTestPage(JSONObject json) {
         super(json);
         contentPanel = getContentPanel();
         contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 
         setLayout(new java.awt.BorderLayout());
 
 
         JPanel secondaryPanel = new JPanel();
         secondaryPanel.add(contentPanel, BorderLayout.NORTH);
         add(secondaryPanel, BorderLayout.CENTER);
     }
    public void eventTriggered(){
         stopRecording();
     }
 
 
     private JPanel getContentPanel() {
 
         JPanel contentPanel = new JPanel();
         contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, BoxLayout.Y_AXIS));
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         blankSpace = new JLabel();
 
         contentPanel.add(blankSpace, c);
 
         JPanel introPanel = createIntroPanel();
         JPanel buttons = createRecordTestPanel();
         contentPanel.add(introPanel, c);
         contentPanel.add(buttons, c);
 
         return contentPanel;
 
     }
 
     private RecordPlay getRecorder() {
         if (recorder == null) {
             recorder = new RecordPlay();
             //30 seconds
             recorder.setTimeout(30000);
             recorder.addListener(this);
         }
         return recorder;
     }
 
 
     private void stopRecording() {
         recorder.stop();
         playButton.setEnabled(true) ;
         recordButton.setText("RECORD") ;
         recordButton.setEnabled(true) ;
     }
     private JPanel createRecordTestPanel() {
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setPreferredSize(new Dimension(600, 40));
         //buttonsPanel.setLayout(new java.awt.BorderLayout());
         recordButton = new JButton("RECORD");
         stopButton = new JButton("STOP");
         playButton = new JButton("PLAY");
         playButton.setEnabled(false) ;
 
         recordButton.setActionCommand(RECORD_BUTTON_ACTION_COMMAND);
         stopButton.setActionCommand(STOP_BUTTON_ACTION_COMMAND);
 
         //recordButton.addActionListener(LessonController);
         recordButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
 
                 stopButton.setEnabled(true) ;
                 //start record
                 RecordPlay recorder = getRecorder();
                 if (recorder.isInRecording()) {
                     recorder.stop();
                     playButton.setEnabled(true) ;
                     recordButton.setText("RECORD") ;
 
                 } else {
                     recorder.capture() ;
                     recordButton.setText("STOP") ;
                 }
             }
         }) ;
         //regist the PlayEvent
         playButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
 
                 //play record
                 RecordPlay recorder = getRecorder();
                 if (!recorder.isInRecording())
                    recorder.stop();
                 recorder.play() ;
             }
         }) ;
         buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
 
         //buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
         buttonsPanel.add(recordButton);
         //buttonsPanel.add(stopButton);
         buttonsPanel.add(playButton);
         return buttonsPanel;
     }
 
 
     private JPanel createIntroPanel() {
         JPanel introPanel = new JPanel();
         introPanel.setPreferredSize(new Dimension(600, 500));
         introPanel.setLayout(new java.awt.BorderLayout());
         introArea = new JEditorPane();
         introArea.setContentType("text/html; charset=utf-8");
         introArea.setText(textContent);
         introArea.setEditable(false);
         //Get JFrame background color  
         Color color = getBackground();
         introArea.setBackground(color);
         introPanel.add(introArea, java.awt.BorderLayout.CENTER);
         return introPanel;
     }
 
 }
