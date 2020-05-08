 package ee.mattijagula.mikker.ui;
 
 import javax.swing.*;
 
 public class Icons {
     public ImageIcon getRecordIcon() {
         return loadIcon("icons/media-record.png", "Start recording");
     }
     public ImageIcon getStopIcon() {
         return loadIcon("icons/media-playback-stop.png", "Stop recording");
     }
     public ImageIcon getPlayIcon() {
         return loadIcon("icons/media-playback-start.png", "Replay recording");
     }
     public ImageIcon getDeleteIcon() {
         return loadIcon("icons/dialog-delete.png", "Delete");
     }
     public ImageIcon getSaveIcon() {
         return loadIcon("icons/mail-send.png", "Save");
     }
     public ImageIcon getOkIcon() {
         return loadIcon("icons/emblem-ok.png", "OK");
     }
 
     private ImageIcon loadIcon(String path, String label) {
        return new ImageIcon(Icons.class.getClassLoader().getResource(path));
     }
 }
