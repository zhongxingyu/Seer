 package net.daboross.will.pokemon;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.Sequencer;
 
 public class MidiPlayer implements Runnable {
 
     private Sequencer sequencer;
     private String filename;
     private boolean bgm;
     /**
      * 1 for start, and 2 for stop.
      */
     private List<Integer> nextAction = new ArrayList<>();
     private boolean threadRunning;
 
     public MidiPlayer(String file, boolean b) {
         filename = file;
         bgm = b;
     }
 
     public void start() {
         nextAction.add(1);
        if (threadRunning = false) {
             Thread th = new Thread(this);
             th.start();
         }
         synchronized (this) {
             this.notifyAll();
         }
     }
 
     public void stop() {
         nextAction.add(2);
         synchronized (this) {
             this.notifyAll();
         }
     }
 
     @Override
     public void run() {
         while (true) {
             threadRunning = true;
             if (!nextAction.isEmpty()) {
                 int action = nextAction.remove(0);
                 if (action == 1) {
                     try {
                         Sequence sequence = MidiSystem.getSequence(getClass().getResource(filename));
                         sequencer = MidiSystem.getSequencer();
                         sequencer.open();
                         sequencer.setSequence(sequence);
                         if (bgm == true) {
                             sequencer.setLoopCount(999);
                         }
                         sequencer.start();
                     } catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {
                     }
                 } else if (action == 2) {
                     sequencer.stop();
                 }
             }
             if (nextAction.isEmpty()) {
                 synchronized (this) {
                     try {
                         this.wait();
                     } catch (InterruptedException ex) {
                         System.out.println("Exception in MidiPlayer Thread");
                         threadRunning = false;
                     }
                 }
             }
         }
     }
 }
