 package de.paluch.midi.relay.midi;
 
 import de.paluch.midi.relay.relay.ETHRLY16;
 import org.apache.log4j.Logger;
 import org.apache.log4j.helpers.OptionConverter;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequencer;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.*;
 
 /**
  * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
  * @since 09.11.12 19:50
  */
 public class MidiPlayer {
 
     private Logger log = Logger.getLogger(getClass());
     private String midiDirectory;
     private Sequencer sequencer;
     private ETHRLY16 receiver;
     private boolean run = false;
 
     public String getMidiDirectory() {
         return midiDirectory;
     }
 
     public void setMidiDirectory(String midiDirectory) {
         this.midiDirectory = midiDirectory;
     }
 
     public Sequencer getSequencer() {
         return sequencer;
     }
 
     public void setSequencer(Sequencer sequencer) {
         this.sequencer = sequencer;
     }
 
     public ETHRLY16 getReceiver() {
         return receiver;
     }
 
     public void setReceiver(ETHRLY16 receiver) {
         this.receiver = receiver;
     }
 
     public void play(String fileName) {
 
 
         if (run) {
             log.info("Already running");
             return;
         }
 
         run = true;
         File[] files = getFiles(fileName);
 
         receiver.off(0);
         if (files != null) {
             List<File> theFiles = Arrays.asList(files);
             Collections.sort(theFiles, new Comparator<File>() {
                 @Override
                 public int compare(File o1, File o2) {
                     return o1.getName().compareToIgnoreCase(o2.getName());
                 }
             });
 
             for (File theFile : theFiles) {
                 if (run) {
                     try {
                         receiver.off(0);
                         Thread.sleep(2000);
                         playFile(theFile);
                         Thread.sleep(2000);
                         receiver.off(0);
                     } catch (Exception e) {
                         log.warn(theFile.getName() + ": " + e.getMessage(), e);
                     }
                 }
             }
         }
 
         receiver.on(0);
 
     }
 
     public boolean isRunning() {
         if (run && sequencer.isRunning()) {
             return true;
         }
 
         return false;
     }
 
     private void playFile(File theFile) throws InvalidMidiDataException, IOException {
         log.info("Starting file " + theFile.getName());
         sequencer.setSequence(MidiSystem.getSequence(theFile));
         sequencer.start();
 
 
         while (isRunning()) {
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                 e.printStackTrace();
                 break;
             }
         }
 
         DecimalFormat df = new DecimalFormat();
         df.setMinimumFractionDigits(0);
         df.setMaximumFractionDigits(0);
         df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.GERMAN));
         df.setGroupingSize(3);
         df.setGroupingUsed(true);
         log.info("Finished file " + theFile.getName());
         log.info("Bytes sent: " + df.format(receiver.getBytesSent()));
     }
 
     private File[] getFiles(String fileName) {
 
         if (fileName == null) {
             File file = new File(midiDirectory);
 
 
             return file.listFiles(new FilenameFilter() {
                 @Override
                 public boolean accept(File dir, String name) {
                     return name.toLowerCase().endsWith(".mid");
                 }
             });
         }
 
 
         File file = new File(midiDirectory, fileName);
         if (file.exists()) {
             return new File[] { file };
         }
 
         return new File[0];
 
     }
 
 
     public void stop() {
         sequencer.stop();
         run = false;
     }
 
 
 }
