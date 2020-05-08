 package com.core.test;
 
 import com.core.lesson.*;
 import com.core.util.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import javazoom.jl.player.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.net.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 
 public class SpeakingQuestionPage extends LessonPage implements EventListener {
     /**
      * The String-based action command for the 'Next' button.
      */
     public static final String RECORD_BUTTON_ACTION_COMMAND = "RecordButtonActionCommand";
     public static final String STOP_BUTTON_ACTION_COMMAND = "StopButtonActionCommand";
 
     private JLabel blankSpace;
     private JLabel pagePositionLabel;
     private JEditorPane questionLabel;
     private JTextArea questionTranslationLabel;
 
     private JButton recordButton;
     private JButton stopButton;
 
     private JLabel positionLabel;
     private JLabel countDownLabel;
     private JPanel contentPanel;
 
     private countDownThread countDownTimer;
     private playAudioThread playAudio;
     private RecordPlay recorder;
     private String prepareTimeRemaining = "Prepare Time Remaining";
     private String recordTimeRemaining = "Recording Time Remaining";
     private String questionFile;
     private String questionAudio;
     private String recordNowAudio;
     private boolean showRecordCountDown = true;
     private boolean showPrepareCountDown = false;
 
     //Tests model data
     private String TestID = "1";
     private String recordFileName = "";
     private int totalTests = 1;
     private long actionStart = 0;
     private long actionEnd = 0;
     private long pauseTime = 0;
     private long recordTime = 0;
     private long recordTimeout = 120;
     private long prepareTimeout = 120;
 
     public SpeakingQuestionPage(JSONObject json) {
         try {
             TestID = json.getString("id");
             totalTests = json.getInt("totalTest");
             questionFile = json.getString("page");
             questionAudio = json.getString("QuestionAudio");
             recordNowAudio = json.getString("RecordNowAudio");
             showRecordCountDown = json.getBoolean("showRecordCountDown");
             showPrepareCountDown = json.getBoolean("showPrepareCountDown");
             recordFileName = json.getString("recordFileName");
             recordTimeout = json.getInt("recordTimeout");
             prepareTimeout = json.getInt("prepareTimeout");
             recordTimeRemaining = json.getString("recordTimeRemaining");
             prepareTimeRemaining = json.getString("prepareTimeRemaining");
         } catch (JSONException e) {
             System.out.println(e.getMessage());
         }
 
         contentPanel = getContentPanel();
         contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
 
         JPanel secondaryPanel = new JPanel();
         secondaryPanel.add(contentPanel, BorderLayout.NORTH);
         add(secondaryPanel, BorderLayout.CENTER);
     }
 
 
     private JPanel getContentPanel() {
 
         JPanel contentPanel = new JPanel();
         contentPanel.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         JPanel jPanel1 = new JPanel();
 
         blankSpace = new JLabel();
 
         contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, BoxLayout.Y_AXIS));
 
         //buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
 
         JPanel headerPanel = createHeaderPanel();
         JPanel questionPanel = createQuestionPanel();
         JPanel recordPanel = createRecordButtons();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         contentPanel.add(headerPanel, c);
         contentPanel.add(questionPanel, c);
         contentPanel.add(recordPanel, c);
         //contentPanel.add(questionTranslationPanel, c);
 
         return contentPanel;
 
     }
 
 
     private Object getResource(String key) {
 
         URL url = null;
         String name = key;
 
         if (name != null) {
 
             try {
                 Class c = Class.forName("com.core.test.Main");
                 url = c.getResource(name);
             } catch (ClassNotFoundException cnfe) {
                 System.err.println("Unable to find Main class");
             }
             return url;
         } else
             return null;
 
     }
 
     public void setTestID(String id) {
         TestID = id;
     }
 
     public String getTestID() {
         return TestID;
     }
 
     public void setTotalTests(int cnt) {
         totalTests = cnt;
     }
 
     class countDownThread extends Thread {
         // Must be volatile:
         private long countStart = 0;
         private long countStop = 0;
         private volatile boolean stop = false;
         private EventListener listener;
 
         public long getCountTime() {
             return countStop - countStart;
         }
         public void addListener(EventListener lsn) {
             listener = lsn;
         }
 
         public void run(){
             TimerThread count_down = new TimerThread(TimerThread.COUNT_DOWN);
             int timeout = 2;
             if (recorder == null) {
                 timeout = (int)prepareTimeout;
             } else {
                 timeout = (int)recordTimeout;
             }
             count_down.setRemainingSeconds(timeout);
             countStart = System.currentTimeMillis();
             count_down.start();
 
             while (!stop && count_down.isAlive()) {
                 //show counting down clock
                 if (showPrepareCountDown && recorder == null) {
                     countDownLabel.setText(prepareTimeRemaining+": "+count_down.getClock());
                 } else if (recorder != null && showRecordCountDown) {
                     countDownLabel.setText(recordTimeRemaining+": "+count_down.getClock());
                 }
                 try {
                     sleep(100);
                 } catch (InterruptedException e) {
                     System.out.println("Interrupted.");
                 }
             }
             //timeout
             if (listener != null && !stop)
                 listener.eventTriggered("countDownEnd");
             stop = true;
         }
 
         public void requestStop() {
             //count stop
             if (!stop) {
                 stop = true;
                 countStop = System.currentTimeMillis();
             }
         }
 
     }
 
     public void startTimer() {
         if (countDownTimer != null) {
             countDownTimer.requestStop();
             pauseTime = countDownTimer.getCountTime()/1000;
             //System.out.println("pause time="+pauseTime);
         }
         countDownTimer = new countDownThread();
         countDownTimer.addListener(this);
         countDownTimer.start();
     }
 
     class playAudioThread extends Thread {
         // Must be volatile:
         private volatile boolean stop = false;
         private Player player;
         private EventListener listener;
         private String audioFile;
         public playAudioThread(String fFilename) {
             audioFile = fFilename;
         }
         public void addListener(EventListener lsn) {
             listener = lsn;
         }
         private void playFile(String fFilename) {
 
             try {
                 if ((new File(fFilename)).exists()) {
                     FileInputStream fin = new FileInputStream(fFilename);
                     BufferedInputStream bin = new BufferedInputStream(fin);
                     AudioDevice dev = FactoryRegistry.systemRegistry().createAudioDevice();
                     player = new Player(bin);
                     //System.out.println("playing "+fFilename+"...");
                     player.play();
                 } else {
                     System.out.println("not exist file "+fFilename+"...");
                 }
             } catch (IOException ex) {
                 //throw new Exception("Problem playing file "+fFilename, ex);
                 System.out.println("Problem playing file "+fFilename);
             } catch (Exception ex) {
                 //throw new Exception("Problem playing file "+fFilename, ex);
                 System.out.println("Problem playing file "+fFilename);
             }
         }
 
         public void run(){
             playFile(audioFile);
             if (listener != null)
                 listener.eventTriggered("playDone");
         }
 
         public void requestStop() {
             //stop the timer
             stopTimer();
             if (player != null)
                 player.close();
         }
 
         public String getAudioFile() {
             return audioFile;
         }
     }
 
     public void playAudio() {
         try {
             playAudio = new playAudioThread(getQuestionAudio());
             playAudio.addListener(this);
             playAudio.start();
         } catch (Exception ex) {
             System.out.println("Problem playing file");
         }
     }
 
     public void stopAudio() {
         if (playAudio != null) {
             playAudio.requestStop();
         }
     }
 
     public void stopTimer() {
 
         if (countDownTimer != null) {
             countDownTimer.requestStop();
         }
     }
 
     private JPanel createRecordButtons() {
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
         buttonsPanel.setPreferredSize(new Dimension(400, 60));
         recordButton = new JButton((String)ResourceManager.getTestResource("record"));
         recordButton.setEnabled(false) ;
         stopButton = new JButton((String)ResourceManager.getTestResource("stop"));
         stopButton.setEnabled(false) ;
 
         recordButton.setActionCommand(RECORD_BUTTON_ACTION_COMMAND);
         stopButton.setActionCommand(STOP_BUTTON_ACTION_COMMAND);
 
         //recordButton.addActionListener(LessonController);
         recordButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 startRecord();
             }
         }) ;
         stopButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 stopRecord();
             }
         }) ;
         buttonsPanel.add(recordButton);
         buttonsPanel.add(stopButton);
         return buttonsPanel;
     }
 
     public HashMap getSubmit() {
         LinkedHashMap data = new LinkedHashMap();
         if (recorder != null)
             recordTime = recorder.getDuration()/1000;
         //System.out.println("record time="+recordTime);
         data.put("Record", TestID);
         data.put("pauseTime", pauseTime + " second(s)");
         data.put("recordTime", recordTime + " seconds(s)");
         data.put("recordFile", getRecordFileName());
         data.put("linebreak", "\n");
         return data;
     }
 
     public void eventTriggered(String event){
         if (event.equals("playDone")) {
             if (playAudio.getAudioFile().equals(this.getRecordingNowAudio())) {
                 startRecord();
             } else {
                 //after end of playing
                 startTimer();
                 //enable the record button
                 recordButton.setEnabled(true) ;
             }
         } else if (event.equals("recordTimeout")){
             stopRecord();
         } else if (event.equals("countDownEnd")) {
             if (recorder == null) {
                playAudio = new playAudioThread(getRecordingNowAudio());
                playAudio.addListener(this);
                 playAudio.start();
             } else {
                 if (recorder.hasCaptured()) {
                     stopRecord();
                 }
             }
         }
     }
 
     private JPanel createHeaderPanel() {
         JPanel headerPanel = new JPanel();
         headerPanel.setPreferredSize(new Dimension(400, 40));
 
         headerPanel.setLayout(new java.awt.BorderLayout());
         positionLabel = new JLabel();
         positionLabel.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 11));
         //setPosition();
         countDownLabel = new JLabel();
         //headerPanel.add(positionLabel, java.awt.BorderLayout.WEST);
         headerPanel.add(countDownLabel, java.awt.BorderLayout.EAST);
         return headerPanel;
     }
 
     private void setPosition() {
         String position = (String) ResourceManager.getTestResource("indicator");
         position = position.replace("%currentTest%", TestID);
         position = position.replace("%totalTests%", Integer.toString(totalTests));
         positionLabel.setText(position);
     }
 
     private void startRecord() {
         startTimer();
         recordButton.setEnabled(false) ;
         stopButton.setEnabled(true) ;
         //start record
         RecordPlay recorder = getRecorder();
         recorder.capture() ;
         //recording countdown start
     }
 
     private void stopRecord() {
         //recordButton.setEnabled(true) ;
         stopButton.setEnabled(false) ;
         recorder.stop() ;
         //stop the countdown
         stopTimer();
         //save the file
         recorder.save(ResourceManager.getUserPath() + "/" + getRecordFileName());
         sendMessage("READY");
     }
 
     private JPanel createQuestionPanel() {
         JPanel questionPanel = new JPanel();
         questionPanel.setPreferredSize(new Dimension(600, 500));
         questionPanel.setLayout(new java.awt.BorderLayout());
         questionLabel = new JEditorPane();
         questionLabel.setContentType("text/html; charset=EUC-JP");
         questionLabel.setText(getQuestionID());
         questionLabel.setEditable(false);
         //Get JFrame background color
         Color color = getBackground();
         questionLabel.setBackground(color);
         questionPanel.add(questionLabel, java.awt.BorderLayout.CENTER);
         return questionPanel;
     }
 
     private RecordPlay getRecorder() {
         if (recorder == null) {
             recorder = new RecordPlay();
             recorder.setTimeout(120000);
             recorder.addListener(this);
         }
         return recorder;
     }
 
     private String getQuestionID() {
         return ResourceManager.getPageText(questionFile);
     }
 
     private String getQuestionAudio() {
         return ResourceManager.getAudioResourcePath() + "/" + questionAudio;
     }
 
     private String getRecordingNowAudio() {
         return ResourceManager.getAudioResourcePath() + "/" + recordNowAudio;
     }
 
     private String getQuestionTranslationID() {
         return (String)ResourceManager.getTestResource("questionTranslation_"+TestID);
     }
 
     private String getRecordFileName() {
         if (recordFileName.equals("")) {
             return "Test_" + TestID + ".wav";
         } else {
             return recordFileName + ".wav";
         }
     }
 
 }
