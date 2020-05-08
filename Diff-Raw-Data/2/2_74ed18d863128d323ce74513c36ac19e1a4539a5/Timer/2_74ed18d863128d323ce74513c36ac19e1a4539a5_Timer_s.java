 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.uml.cs.slo;
 
 import java.util.Vector;
 
 /**
  *
  * @author mattvaughan
  */
 public class Timer implements Runnable {
 
     Thread t;
 
     Timer() {
         t = new Thread(this, "Timing Thread");
         t.start();
     }
 
     public void run() {
         int numerator = MusicTime.musicTime.getTimeNumerator();
         int mNum = 1;
         long beatLength = MusicTime.musicTime.beatLength();
         long[] beats = new long[100];
         for( int i = 0; i < 100; i++) {
             beats[i] = 0;
         }
         //long beatTwo = 0, beatThree = 0, beatFour = 0;
 
         while (true) {
             
             if (MusicTime.musicTime.getCurrentState()) {  // Our On/Off boolean
                 beatLength = MusicTime.musicTime.beatLength();               
 
                 Vector<TimeMessagePair> ourVec = TimeMessagePairs.getPairs();
                 for (int i = 0; i < ourVec.size(); ++i) {
                     
                     for( int j = numerator - 1; j > 0; j-- ) {
                         if( beats[j] <= System.currentTimeMillis() && (MusicTime.musicTime.getCurrentBeat() < j+1)) {
                             MusicTime.musicTime.setCurrentBeat(j+1);
                             break;
                         }
                     }
                     //if (beatFour <= System.currentTimeMillis() && (MusicTime.musicTime.getCurrentBeat() < 4)) {
                     //    MusicTime.musicTime.setCurrentBeat(4);
                     //} else if (beatThree <= System.currentTimeMillis() && (MusicTime.musicTime.getCurrentBeat() < 3)) {
                     //    MusicTime.musicTime.setCurrentBeat(3);
                     //} else if (beatTwo <= System.currentTimeMillis() && (MusicTime.musicTime.getCurrentBeat() < 2)) {
                     //    MusicTime.musicTime.setCurrentBeat(2);
                     //}
 
                     // if it's time, send the message and remove the timeMessagePair from the vector
                     if (ourVec.get(i).getTime() <= System.currentTimeMillis()) {
                         ourVec.get(i).sendMessage();
                         ourVec.remove(i);
                         --i;
                     }
                 }
 
                 for( int j = numerator - 1; j > 0; j-- ) {
                     if( beats[j] <= System.currentTimeMillis() && (MusicTime.musicTime.getCurrentBeat() < j+1)) {
                         MusicTime.musicTime.setCurrentBeat(j+1);
                         break;
                     }
                 }
 
                 // if we're past the next measure, advance the next measure
                 if (MusicTime.musicTime.getNextMeasure() <= System.currentTimeMillis()) {
                     MusicTime.musicTime.advanceNextMeasure();
                     MusicTime.musicTime.setCurrentMeasure(mNum++);
                     MusicTime.musicTime.setCurrentBeat(1);
                     numerator = MusicTime.musicTime.getTimeNumerator();
                     beats[1] = beatLength + System.currentTimeMillis();
                     for( int j = 2; j < numerator; j++ ) {
                         beats[j] = beats[j-1] + beatLength;
                     }
                     //beatTwo = beatLength + System.currentTimeMillis();
                     //beatThree = beatLength + beatTwo;
                     //beatFour = beatLength + beatThree;
                 }
             } else {
                 if (MusicTime.musicTime.getCurrentBeat() != 1) {
                     MusicTime.musicTime.setCurrentBeat(1);
                     for( int j = 1; j < numerator; j++ ) {
                         beats[j] = 0;
                     }
                 }
                if (MusicTime.musicTime.getCurrentMeasure() != 1) {
                     MusicTime.musicTime.setCurrentMeasure(1);
                     mNum = 1;
                 }
             }
         }
         /*
          * Alas, ConcurrentModificationExceptions are unfriendly with iterators 
          * while (true) { 
          * // lets get an iterator 
          * Iterator<TimeMessagePair> it =
          * TimeMessagePairs.getPairs().iterator();
          *
          * // for each message, check the time while (it.hasNext()) {
          *
          * TimeMessagePair timeMsg = it.next();
          *
          * // if the time is right send the message if (timeMsg.getTime() <=
          * System.currentTimeMillis()) { timeMsg.sendMessage(); it.remove(); } } }
          */
     }
 }
