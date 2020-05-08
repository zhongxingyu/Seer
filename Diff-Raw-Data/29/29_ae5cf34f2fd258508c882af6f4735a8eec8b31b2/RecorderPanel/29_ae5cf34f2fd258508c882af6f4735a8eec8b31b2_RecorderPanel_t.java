 package ee.mattijagula.mikker.ui;
 
 import ee.mattijagula.mikker.recorder.Recorder;
 import ee.mattijagula.mikker.upload.Configuration;
 import ee.mattijagula.mikker.upload.Uploader;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class RecorderPanel extends JPanel {
     public RecorderPanel(Configuration ctx, final Recorder soundRecorder) {
 
        Color bgColor = new Color(ctx.getBackgroundColor());
        setBackground(bgColor);
         setLayout(new BorderLayout());
 
         JPanel buttonBar = new JPanel();
        buttonBar.setBackground(bgColor);
         
         add(buttonBar, BorderLayout.NORTH);
 
         FilenameField filename = new FilenameField(ctx);
         soundRecorder.addListener(filename);
 
         add(filename, BorderLayout.SOUTH);
 
         RecordButton recordButton = new RecordButton(soundRecorder, ctx.getMaxRecordingDuration());
         soundRecorder.addListener(recordButton);
         buttonBar.add(fixButtonWidthOnWindows(recordButton));
 
         DeleteButton deleteButton = new DeleteButton();
         deleteButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 soundRecorder.deleteRecording();
                 RecorderPanel.this.transferFocus();
             }
         });
 
         soundRecorder.addListener(deleteButton);
         buttonBar.add(fixButtonWidthOnWindows(deleteButton));
 
         Uploader uploader = new Uploader(ctx);
 
         SaveButton saveButton = new SaveButton(soundRecorder, uploader, filename);
         soundRecorder.addListener(saveButton);
         buttonBar.add(fixButtonWidthOnWindows(saveButton));
     }
 
     private JButton fixButtonWidthOnWindows(JButton button) {
         /*
          * on windows, the button gets is displayer really wide. set the preferred width to be same as the height,
          * as it is on the mac and linux. 
          */
         Dimension preferredSize = button.getPreferredSize();
         button.setPreferredSize(new Dimension((int) preferredSize.getHeight(), (int) button.getPreferredSize().getHeight()));
         return button;
     }
 }
