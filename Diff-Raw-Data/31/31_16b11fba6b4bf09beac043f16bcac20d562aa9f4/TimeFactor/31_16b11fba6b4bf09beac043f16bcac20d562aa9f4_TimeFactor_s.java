 /*
  * JFugue - API for Music Programming
  * Copyright (C) 2003-2008  David Koelle
  *
  * http://www.jfugue.org 
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *  
  */
 
 package org.jfugue;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sound.midi.MetaMessage;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.Sequence;
 
 public final class TimeFactor
 {
     public static double DEFAULT_BPM = 128.0d;
     public static int QUARTER_DURATIONS_IN_WHOLE = 4;
     
     public static final double getTimeFactor(Sequence sequence, double bpm)
     {
         double divisionType = sequence.getDivisionType();
         double resolution = sequence.getResolution();
 
         // If division type is PPQ, resolution is ticks per beat.
         // Since a beat is the length of time given to a one quarter note, this essentially
         // means that ticks per beat == pulses per quarter note (PPQ or PPQN)
         if (divisionType == Sequence.PPQ)
         {
 //            System.out.println("DivisionType is PPQ");
 //            System.out.println("Resolution is "+resolution);
         } else {
 //            System.out.println("DivisionType is SMPTE");
         }
         // Useful resources: http://www.borg.com/~jglatt/tech/midifile/tempo.htm and http://www.borg.com/~jglatt/tech/midifile/ppqn.htm
         
 //        If bit 15 of division is a zero, the bits 14 thru 0 represent the number of delta-time ticks which make up a 
 //        quarter-note. For instance, if division is 96, then a time interval of an eighth-note between two events 
 //        in the file would be 48.
 //
 //        If bit 15 of division is a one, delta-times in a file correspond to subdivisions of a second, in a way consistent 
 //        with SMPTE and MIDI time code. Bits 14 thru 8 contain one of the four values -24, -25, -29, or -30, corresponding 
 //        to the four standard SMPTE and MIDI time code formats (-29 corresponds to 30 drop frame), and represents the number
 //        of frames per second. These negative numbers are stored in two's complement form. The second byte (stored positive) 
 //        is the resolution within a frame: typical values may be 4 (MIDI time code resolution), 8, 10, 80 (bit resolution), 
 //        or 100. This system allows exact specification of time- code-based tracks, but also allows millisecond-based tracks
 //        by specifying 25 frames/sec and a resolution of 40 units per frame. If the events in a file are stored with bit 
 //        resolution of thirty-frame time code, the division word would be E250 hex.         
 
         if (bpm == 0.0) 
         { 
             bpm = DEFAULT_BPM; 
         }
         
         return 60000.0 / (resolution * bpm);
     }
     
     public static final byte[] convertBPMToBytes(int tempo)
     {
     	// This block is original code from JFugue 4.0.3 
         double tempoInMsPerBeat = TimeFactor.convertBPMToMicrosecondsPerBeat(tempo);
         double d1 = Math.floor(tempoInMsPerBeat / 16384.0);
         double d2 = Math.floor((tempoInMsPerBeat % 16384.0) / 128.0);
         double d3 = Math.floor((tempoInMsPerBeat % 16384.0) % 128.0);
         return new byte[] { (byte)d1, (byte)d2, (byte)d3 }; 
 
     	// This block is suggested by Apex.Mao, but makes music 4 times as fast 
 //        double tempoInMsPerBeat = TimeFactor.convertBPMToMicrosecondsPerBeat(tempo);
 //        int i1 = (int)Math.floor(tempoInMsPerBeat / 65536);
 //        tempoInMsPerBeat %= 65536;
 //        int i2 = (int)Math.floor(tempoInMsPerBeat/256);
 //        tempoInMsPerBeat %= 256;
 //        int i3 = (int)Math.floor(tempoInMsPerBeat);
 //        return new byte[] { (byte)i1, (byte)i2, (byte)i3 };
     }    
     
    public static final int parseMicrosecondsPerBeat(MetaMessage message, long timestamp)
     {
     	byte bytes[] = message.getMessage();
     	int microseconds = (int)bytes[3] * 65536 + (int)bytes[4]*256 + (int)bytes[5];
     	return microseconds;
     }
     
     /** Converts microseconds per beat to BPM -- and vice versa */
     public static final double convertMicrosecondsPerBeatToBPM(double value) {
         double microsecondsPerMinute = 60000000.0D;
         if (value == 0.0d) {
             return 0.0d;
         }
         return microsecondsPerMinute / value;
     }
     
     /** Converts microseconds per beat to BPM -- and vice versa */
     public static final double convertBPMToMicrosecondsPerBeat(int bpm) {
         double microsecondsPerMinute = 60000000.0D;
         if (bpm == 0) {
             return 0;
         }
         return microsecondsPerMinute / bpm;
     }
     
     /**
      * Takes all of the MIDI events in the given Sequence, sorts them according to
      * when they are to be played, and sends the events to the MidiMessageRecipient
      * when the each event is ready to be played.
      * 
      * @param sequence The Sequence with messages to sort and deliver
      * @param recipient the handler of the delivered message
      */
     public static final void sortAndDeliverMidiMessages(Sequence sequence, MidiMessageRecipient recipient) 
     {
        double timeFactor = 1.0;
        
         Map<Long, List<MidiEvent>> timeMap = new HashMap<Long, List<MidiEvent>>();
         long longestTime = TimeEventManager.sortSequenceByTimestamp(sequence, timeMap);
         
         long lastTime = 0;
        for (long time=0; time < longestTime; time++)
         {
             List<MidiEvent> midiEventList = (List<MidiEvent>)timeMap.get(time);
             if (midiEventList != null)
             {
                 for (MidiEvent event : midiEventList)
                 {
                     MidiMessage message = event.getMessage(); 
                     if ((message.getMessage().length >= 2) && (message.getMessage()[1] == 0x51) && (message instanceof MetaMessage)) 
                     {
                        int bpm = parseMicrosecondsPerBeat((MetaMessage)message, time);
                         timeFactor = TimeFactor.getTimeFactor(sequence, bpm);
                     }
                     recipient.messageReady(message, time);
                 }
 
                 try
                 {
                    long sleepTime = (int)(((time - lastTime) * (4*TimeFactor.QUARTER_DURATIONS_IN_WHOLE+0.20))); // TODO - Make the numbers here less magical
                    // System.out.println("Sleep time is "+sleepTime+" time diff is "+(time-lastTime));
                     Thread.sleep(sleepTime); 
                     lastTime = time;
                 } catch (Exception ex)
                 {
                     throw new JFugueException(JFugueException.ERROR_SLEEP);
                 }
             }
         }
     }
 }
