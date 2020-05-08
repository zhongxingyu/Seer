 package ee.mattijagula.mikker.ui;
 
 import ee.mattijagula.mikker.recorder.Recorder;
 import ee.mattijagula.mikker.recorder.RecordingEvent;
 import ee.mattijagula.mikker.recorder.RecordingListener;
 import ee.mattijagula.mikker.upload.ProgressListener;
 import ee.mattijagula.mikker.upload.UploadFailedException;
 import ee.mattijagula.mikker.upload.Uploader;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class SaveButton extends JButton implements ActionListener, RecordingListener {
     private final ImageIcon save = new Icons().getSaveIcon();
     private final ImageIcon ok = new Icons().getOkIcon();
    private final ImageIcon error = new Icons().getErrorIcon();
    
     private static final Color PROGRESS_COLOR = new Color(0, 0xaa, 0, 127);
 
     private final Recorder recorder;
     private final Uploader uploader;
     private UploadListener listener;
 
     public SaveButton(Recorder soundRecorder, Uploader uploader, UploadListener listener) {
         setIcon(save);
 
         this.recorder = soundRecorder;
         this.uploader = uploader;
         this.listener = listener;
 
         setToolTipText("Upload the recorded clip");
         setEnabled(false);
 
         addActionListener(this);
     }
 
    private Icon overlayIcon(final ImageIcon overlayWith) {
         return new Icon() {
             public void paintIcon(Component component, Graphics graphics, int x, int y) {
                 save.paintIcon(component, graphics, x, y);
                int topLeftPosition = y + (save.getIconHeight() - overlayWith.getIconHeight());
                overlayWith.paintIcon(component, graphics, x, topLeftPosition);
             }
 
             public int getIconWidth() {
                 return save.getIconWidth();
             }
 
             public int getIconHeight() {
                 return save.getIconHeight();
             }
         };
     }
 
     public void actionPerformed(ActionEvent actionEvent) {
         final LevelDisplayingIcon level = new LevelDisplayingIcon(save, PROGRESS_COLOR);
         setIcon(level);
         setEnabled(false);
 
         listener.onUploadStarted();
 
         final ProgressListener progressListener = new ProgressListener() {
             public void transferred(long transferred, long total) {
                 int percent = (int) (100.0 * transferred / total);
                 level.displayLevel(percent);
                 repaint();
             }
 
             public void finished() {
                 System.out.println("finished");
                setIcon(overlayIcon(ok));
                 recorder.deleteRecording();
             }
         };
 
         launchUploadingThread(progressListener);
     }
 
     private void launchUploadingThread(final ProgressListener progressListener) {
         new Thread() {
             public void run() {
                 try {
                     uploader.upload(recorder.getRecording(), progressListener);
                 } catch (UploadFailedException e) {
                    setIcon(overlayIcon(error));
                     e.printStackTrace();
                 }
             }
         }.start();
     }
 
     public void onRecordingEvent(RecordingEvent event) {
         if (event.isNewRecordingFinished()) {
             setIcon(save);
             setEnabled(true);
         }
         if (!event.isRecordingAvailable()) {
             setEnabled(false);
         }
     }
 
     public interface UploadListener {
         void onUploadStarted();
     }
 }
