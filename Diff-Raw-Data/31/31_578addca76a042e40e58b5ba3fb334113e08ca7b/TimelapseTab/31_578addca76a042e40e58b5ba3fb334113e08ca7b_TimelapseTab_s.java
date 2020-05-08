 package net.sapium.livestreamprocessor.gui;
 
 import java.io.File;
 
 import net.sapium.livestreamprocessor.utils.MediaTimelapser;
 import net.sapium.livestreamprocessor.utils.ProgressChangedListener;
 import net.sapium.livestreamprocessor.utils.Timelapser;
 import net.sapium.livestreamprocessor.utils.VideoData;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class TimelapseTab extends TabContent {
     private static final int maxSpeedup = 500;
     
     private Text inputTextBox;
     private Button inputButton;
     private Text outputTextBox;
     private Button outputButton;
     private Scale speedupScale;
     private Text speedupText;
     private Scale videoLengthScale;
     private Text videoLengthText;
     private Text audioTextBox;
     private Button changeAudioRadio;
     private Button speedUpAudioRadio;
     private Button audioButton;
 
     public static final long msInHr = 1000 * 60 * 60;
     public static final long msInMn = 1000 * 60;
     public static final long msInS = 1000;
     private Button removeAudioRadio;
 
     public TimelapseTab(Composite arg0, int arg1) {
         super(arg0, arg1);
     }
 
     @Override
     protected void createContents() {
         this.setLayout(new FormLayout());
 
         Label inputLabel = new Label(this, SWT.NONE);
         FormData fd_inputLabel = new FormData();
         fd_inputLabel.top = new FormAttachment(0, 13);
         fd_inputLabel.left = new FormAttachment(0, 10);
         inputLabel.setLayoutData(fd_inputLabel);
         inputLabel.setText("Source file: ");
 
         inputTextBox = new Text(this, SWT.BORDER);
         FormData fd_inputTextBox = new FormData();
         fd_inputTextBox.left = new FormAttachment(inputLabel, 18);
         fd_inputTextBox.top = new FormAttachment(inputLabel, -3, SWT.TOP);
         inputTextBox.setLayoutData(fd_inputTextBox);
 
         inputButton = new Button(this, SWT.NONE);
         fd_inputTextBox.right = new FormAttachment(inputButton, -6);
         inputButton.setText("Browse");
         FormData fd_inputButton = new FormData();
         fd_inputButton.right = new FormAttachment(100, -10);
         fd_inputButton.top = new FormAttachment(inputLabel, -5, SWT.TOP);
         inputButton.setLayoutData(fd_inputButton);
 
         Label outputLabel = new Label(this, SWT.NONE);
         FormData fd_outputLabel = new FormData();
         fd_outputLabel.top = new FormAttachment(inputLabel, 13);
         fd_outputLabel.left = new FormAttachment(0, 10);
         outputLabel.setLayoutData(fd_outputLabel);
         outputLabel.setText("Output file: ");
 
         outputTextBox = new Text(this, SWT.BORDER);
         FormData fd_outputTextBox = new FormData();
         fd_outputTextBox.left = new FormAttachment(outputLabel, 16);
         fd_outputTextBox.top = new FormAttachment(outputLabel, -3, SWT.TOP);
         outputTextBox.setLayoutData(fd_outputTextBox);
 
         outputButton = new Button(this, SWT.NONE);
         fd_outputTextBox.right = new FormAttachment(outputButton, -6);
         outputButton.setText("Browse");
         FormData fd_outputButton = new FormData();
         fd_outputButton.right = new FormAttachment(100, -10);
         fd_outputButton.top = new FormAttachment(outputLabel, -5, SWT.TOP);
         outputButton.setLayoutData(fd_outputButton);
 
         speedupScale = new Scale(this, SWT.NONE);
         speedupScale.setMaximum(100 * maxSpeedup);
         speedupScale.setMinimum(100);
         speedupScale.setPageIncrement(100);
         FormData fd_speedupScale = new FormData();
         fd_speedupScale.top = new FormAttachment(outputTextBox, 6);
         speedupScale.setLayoutData(fd_speedupScale);
 
         speedupText = new Text(this, SWT.BORDER);
         fd_speedupScale.right = new FormAttachment(speedupText, -6);
         speedupText.setText("1.0");
         FormData fd_speedupText = new FormData();
         fd_speedupText.top = new FormAttachment(outputButton, 15);
         fd_speedupText.left = new FormAttachment(inputButton, 0, SWT.LEFT);
         fd_speedupText.right = new FormAttachment(inputButton, 0, SWT.RIGHT);
         speedupText.setLayoutData(fd_speedupText);
 
         Label lblScaleFactor = new Label(this, SWT.NONE);
         fd_speedupScale.left = new FormAttachment(lblScaleFactor, 13);
 
         FormData fd_lblScaleFactor = new FormData();
         fd_lblScaleFactor.top = new FormAttachment(speedupText, 3, SWT.TOP);
         fd_lblScaleFactor.left = new FormAttachment(inputLabel, 0, SWT.LEFT);
         lblScaleFactor.setLayoutData(fd_lblScaleFactor);
         lblScaleFactor.setText("Scale Factor:");
 
         Label lblVideoLength = new Label(this, SWT.NONE);
         lblVideoLength.setText("Video Length:");
         FormData fd_lblVideoLength = new FormData();
         fd_lblVideoLength.left = new FormAttachment(inputLabel, 0, SWT.LEFT);
         lblVideoLength.setLayoutData(fd_lblVideoLength);
 
         videoLengthScale = new Scale(this, SWT.NONE);
         videoLengthScale.setEnabled(false);
         videoLengthScale.setPageIncrement(100);
         videoLengthScale.setMaximum(1000);
         videoLengthScale.setMinimum(100);
         FormData fd_videoLengthScale = new FormData();
         fd_videoLengthScale.right = new FormAttachment(speedupScale, 0, SWT.RIGHT);
         fd_videoLengthScale.left = new FormAttachment(speedupScale, 0, SWT.LEFT);
         fd_videoLengthScale.top = new FormAttachment(speedupScale, 6);
         videoLengthScale.setLayoutData(fd_videoLengthScale);
 
         videoLengthText = new Text(this, SWT.BORDER);
         fd_lblVideoLength.top = new FormAttachment(videoLengthText, 3, SWT.TOP);
         videoLengthText.setEnabled(false);
         FormData fd_videoLengthText = new FormData();
         fd_videoLengthText.top = new FormAttachment(speedupScale, 15);
         fd_videoLengthText.right = new FormAttachment(speedupText, 0, SWT.RIGHT);
         fd_videoLengthText.left = new FormAttachment(speedupText, 0, SWT.LEFT);
         videoLengthText.setLayoutData(fd_videoLengthText);
 
         speedUpAudioRadio = new Button(this, SWT.RADIO);
         speedUpAudioRadio.setSelection(true);
         FormData fd_btnSpeedUpAudio = new FormData();
         fd_btnSpeedUpAudio.top = new FormAttachment(videoLengthScale, 6);
         fd_btnSpeedUpAudio.left = new FormAttachment(inputLabel, 0, SWT.LEFT);
         speedUpAudioRadio.setLayoutData(fd_btnSpeedUpAudio);
         speedUpAudioRadio.setText("Speed up audio");
 
         changeAudioRadio = new Button(this, SWT.RADIO);
         FormData fd_changeAudioRadio = new FormData();
         fd_changeAudioRadio.top = new FormAttachment(videoLengthScale, 6);
         fd_changeAudioRadio.left = new FormAttachment(speedUpAudioRadio, 6);
         changeAudioRadio.setLayoutData(fd_changeAudioRadio);
         changeAudioRadio.setText("Replace Audio");
 
         Label lblAudioFile = new Label(this, SWT.NONE);
         FormData fd_lblAudioFile = new FormData();
         fd_lblAudioFile.left = new FormAttachment(inputLabel, 0, SWT.LEFT);
         lblAudioFile.setLayoutData(fd_lblAudioFile);
         lblAudioFile.setText("Audio File:");
 
         audioTextBox = new Text(this, SWT.BORDER);
         audioTextBox.setEnabled(false);
         fd_lblAudioFile.top = new FormAttachment(audioTextBox, 3, SWT.TOP);
         FormData fd_audioTextBox = new FormData();
         fd_audioTextBox.right = new FormAttachment(outputTextBox, 0, SWT.RIGHT);
         fd_audioTextBox.top = new FormAttachment(speedUpAudioRadio, 6);
         fd_audioTextBox.left = new FormAttachment(inputTextBox, 0, SWT.LEFT);
         audioTextBox.setLayoutData(fd_audioTextBox);
 
         audioButton = new Button(this, SWT.NONE);
         audioButton.setEnabled(false);
         FormData fd_audioButton = new FormData();
         fd_audioButton.top = new FormAttachment(lblAudioFile, -5, SWT.TOP);
         fd_audioButton.left = new FormAttachment(inputButton, 0, SWT.LEFT);
         audioButton.setLayoutData(fd_audioButton);
         audioButton.setText("Browse");
 
         removeAudioRadio = new Button(this, SWT.RADIO);
         FormData fd_removeAudioRadio = new FormData();
         fd_removeAudioRadio.top = new FormAttachment(videoLengthScale, 6);
         fd_removeAudioRadio.left = new FormAttachment(changeAudioRadio, 6);
         removeAudioRadio.setLayoutData(fd_removeAudioRadio);
         removeAudioRadio.setText("Remove Audio");
     }
 
     @Override
     protected void addListeners() {
         inputTextBox.addModifyListener(new ModifyListener() {
             @Override
             public void modifyText(ModifyEvent e) {
                 File inFile = new File(inputTextBox.getText());
                 if (inFile.isFile() && inFile.exists()) {
                     VideoData inVideo = new VideoData(inFile);
                     if (inVideo.getDuration() > 0) {
                         videoLengthText.setEnabled(true);
                         videoLengthText.setText(TimelapseTab.millisToString(inVideo.getDuration()));

                         videoLengthScale.setEnabled(true);
                         videoLengthScale.setMaximum((int) inVideo.getDuration());
                         videoLengthScale.setMinimum((int) (inVideo.getDuration() / maxSpeedup));
                         videoLengthScale.setSelection((int) inVideo.getDuration());
                         videoLengthScale.setPageIncrement((int) (inVideo.getDuration() / maxSpeedup));
                     }
                     
                     inVideo.getReader().close();
                 } else {
                     videoLengthText.setEnabled(false);
                     videoLengthScale.setEnabled(false);
                 }
             }
         });
 
         final FileDialog inputDialog = new FileDialog((Shell) this.getParent().getParent(), SWT.OPEN);

         inputButton.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 String result = inputDialog.open();
                 if (result != null) {
                     inputTextBox.setText(result);
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         final FileDialog outputDialog = new FileDialog((Shell) this.getParent().getParent(), SWT.SAVE);
         outputDialog.setOverwrite(true);
 
         outputButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 String result = outputDialog.open();
                 if (result != null) {
                     outputTextBox.setText(result);
                 }
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         speedupScale.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 speedupText.setText("" + speedupScale.getSelection() / 100.0);
 
                 if (videoLengthScale.isEnabled()) {
                     videoLengthScale.setSelection((int) (videoLengthScale.getMaximum() / (speedupScale.getSelection() / 100.0)));
                     videoLengthText.setText(TimelapseTab.millisToString(videoLengthScale.getSelection()));
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         speedupText.addListener(SWT.FocusOut, new Listener() {
             @Override
             public void handleEvent(Event e) {
                 double val = Double.parseDouble(speedupText.getText());
 
                 if (val < speedupScale.getMinimum() / 100.0) {
                     val = speedupScale.getMinimum() / 100.0;
                 } else if (val > speedupScale.getMaximum() / 100.0) {
                     val = speedupScale.getMaximum() / 100.0;
                 } else {
                     val = ((int) (val * 100.0)) / 100.0;
                 }
 
                 speedupText.setText("" + val);
 
                 speedupScale.setSelection((int) (val * 100));
 
                 if (videoLengthScale.isEnabled()) {
                     videoLengthScale.setSelection((int) (videoLengthScale.getMaximum() / (speedupScale.getSelection() / 100.0)));
                     videoLengthText.setText(TimelapseTab.millisToString(videoLengthScale.getSelection()));
                 }
             }
         });
 
         speedupText.addKeyListener(new KeyListener() {
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.CR) {
                     speedupText.traverse(SWT.TRAVERSE_TAB_NEXT);
                     speedupText.forceFocus();
                 }
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
             }
         });
 
         videoLengthScale.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 videoLengthText.setText(TimelapseTab.millisToString(videoLengthScale.getSelection()));
 
                 speedupScale.setSelection((100 * videoLengthScale.getMaximum()) / videoLengthScale.getSelection());
                 speedupText.setText("" + speedupScale.getSelection() / 100.0);
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         videoLengthText.addListener(SWT.FocusOut, new Listener() {
             @Override
             public void handleEvent(Event event) {
                 long val = TimelapseTab.parseTime(videoLengthText.getText());
 
                 if (val < videoLengthScale.getMinimum()) {
                     val = videoLengthScale.getMinimum();
                     videoLengthText.setText(TimelapseTab.millisToString(val));
                 } else if (val > videoLengthScale.getMaximum()) {
                     val = videoLengthScale.getMaximum();
                     videoLengthText.setText(TimelapseTab.millisToString(val));
                 }
 
                 videoLengthScale.setSelection((int) val);
 
                 speedupScale.setSelection((100 * videoLengthScale.getMaximum()) / videoLengthScale.getSelection());
                 speedupText.setText("" + speedupScale.getSelection() / 100.0);
             }
         });
 
         videoLengthText.addKeyListener(new KeyListener() {
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.CR) {
                     videoLengthText.traverse(SWT.TRAVERSE_TAB_NEXT);
                     videoLengthText.forceFocus();
                 }
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
             }
         });
 
         changeAudioRadio.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 audioTextBox.setEnabled(changeAudioRadio.getSelection());
                 audioButton.setEnabled(changeAudioRadio.getSelection());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
 
         final FileDialog audioDialog = new FileDialog((Shell) this.getParent().getParent(), SWT.OPEN);
 
         audioButton.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 String result = audioDialog.open();
 
                 if (result != null) {
                     audioTextBox.setText(result);
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
         });
     }
 
     @Override
     protected void start(ProgressChangedListener listener) {
         if (!outputTextBox.getText().equals("") && !inputTextBox.getText().equals("")) {
             int audioOption = MediaTimelapser.AUDIO_REMOVE;
             String audioFile = "";
             if(changeAudioRadio.getSelection()){
                 audioOption = MediaTimelapser.AUDIO_REPLACE;
                 audioFile = audioTextBox.getText();
             }else if(speedUpAudioRadio.getSelection()){
                 audioOption = MediaTimelapser.AUDIO_SPEED_UP;
             }
             
             processor = new Timelapser(listener, new File(inputTextBox.getText()), new File(outputTextBox.getText()), Double.parseDouble(speedupText.getText()), audioOption, new File(audioFile));
             Thread timelapseThread = new Thread(processor);
             timelapseThread.start();
         }
     }
 
     /**
      * Converts a number of milliseconds into a string of the format HH:mm:ss
      * 
      * @param millis
      *            a duration in milliseconds
      * @return a string of the form HH:mm:ss with the duration of millis
      */
     public static String millisToString(long millis) {
         String result = "";
 
         {// Hours
             int hours = (int) (millis / msInHr);
 
             if (hours < 10) {
                 result += "0" + hours + ":";
             } else {
                 result += hours + ":";
             }
             millis -= hours * msInHr;
         }
 
         {// Minutes
             int mins = (int) (millis / msInMn);
 
             if (mins < 10) {
                 result += "0" + mins + ":";
             } else {
                 result += mins + ":";
             }
             millis -= mins * msInMn;
         }
 
         {// Seconds
             int secs = (int) (millis / msInS);
 
             if (secs < 10) {
                 result += "0" + secs;
             } else {
                 result += secs;
             }
         }
 
         return result;
     }
 
     /**
      * Converts from HH:mm:ss to milliseconds
      * 
      * @param time
      *            time string in the form HH:mm:ss
      * @return milliseconds in HH:mm:ss, -1 if there was an error
      */
     public static long parseTime(String time) {
         long result = 0;
         String[] sections = time.split(":");
 
         if (sections.length == 3) {
             for (int i = 0; i < sections.length; i++) {
                 long multiplier;
 
                 switch (i) {
                 case 0:
                     multiplier = msInHr;
                     break;
                 case 1:
                     multiplier = msInMn;
                     break;
                 case 2:
                     multiplier = msInS;
                     break;
                 default:
                     multiplier = 0;
                 }
 
                 try {
                     result += Integer.parseInt(sections[i]) * multiplier;
                 } catch (NumberFormatException e) {
                     result = -1;
                     break;
                 }
             }
         } else {
             result = -1;
         }
 
         return result;
     }
 }
