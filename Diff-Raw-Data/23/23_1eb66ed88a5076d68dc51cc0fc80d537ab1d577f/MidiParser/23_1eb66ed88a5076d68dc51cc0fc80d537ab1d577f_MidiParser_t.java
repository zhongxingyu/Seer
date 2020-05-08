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
 
 package org.jfugue.parsers;
 
 import javax.sound.midi.MetaMessage;
 import javax.sound.midi.MidiEvent;
 import javax.sound.midi.MidiMessage;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.ShortMessage;
 import javax.sound.midi.SysexMessage;
 import javax.sound.midi.Track;
 
 import org.jfugue.TimeFactor;
 import org.jfugue.elements.ChannelPressure;
 import org.jfugue.elements.Controller;
 import org.jfugue.elements.Instrument;
 import org.jfugue.elements.Note;
 import org.jfugue.elements.PitchBend;
 import org.jfugue.elements.PolyphonicPressure;
 import org.jfugue.elements.SystemExclusive;
 import org.jfugue.elements.Tempo;
 import org.jfugue.elements.Time;
 import org.jfugue.elements.Voice;
 
 import org.apache.log4j.Logger;
 
 /**
  * Parses MIDI data, whether from a file, a connected device, or some other stream.
  * 
  * @version 4.0.3 - A Note event with 0 duration is now sent when a note is first encountered
  */
 public final class MidiParser extends Parser
 {
     long[][] tempNoteRegistry = new long[16][255];
     byte[][] tempNoteAttackRegistry = new byte[16][255];
     int tempo;
     int resolution;
     private static final int DEFAULT_TEMPO = 120;
 
     public MidiParser()
     {
         this.tempo = DEFAULT_TEMPO;
         this.resolution = DEFAULT_TEMPO;
 
         // Create a two dimensional array of bytes [ track, note ] - when a NoteOn event is found,
         // populate the proper spot in the array with the note's start time.  When a NoteOff event 
         // is found, new Time and Note objects are constructed and added to the composition
         for (int m=0; m < 16; m++) {
             for (int n=0; n < 255; n++) {
                 tempNoteRegistry[m][n] = 0L;
                 tempNoteAttackRegistry[m][n] = (byte)0;
             }
         }
     }
 
     /**
      * Parses a <code>Sequence</code> and fires events to subscribed <code>ParserListener</code>
      * interfaces.  As the Sequence is parsed, events are sent
      * to <code>ParserListener</code> interfaces, which are responsible for doing
      * something interesting with the music data, such as adding notes to a pattern.
      *
      * @param sequence the <code>Sequence</code> to parse
      * @throws Exception if there is an error parsing the pattern
      */
     public void parse(Sequence sequence) 
     {
     	this.tempo = DEFAULT_TEMPO;
 
         // Get the MIDI tracks from the sequence.  Expect a maximum of 16 tracks.
         Track[] tracks = sequence.getTracks();
         this.resolution = sequence.getResolution();
         
         if (sequence.getDivisionType() == Sequence.PPQ) { 
         	this.tempo = resolution;
             Logger.getRootLogger().trace("PPQ with Tempo = " + resolution);
         }
         else this.tempo = DEFAULT_TEMPO;
         
         // Compute the size of this adventure for the ParserProgressListener
         long totalCount = 0;
         long counter = 0;
         for (byte i=0; i < tracks.length; i++)
         {
             totalCount += tracks[i].size();
         }
         
         // And now to parse the MIDI!
         for (int t = 0; t < tracks.length; t++)
         {
             int trackSize = tracks[t].size();
             if (trackSize > 0)
             {
                 fireVoiceEvent(new Voice((byte)t)); 
 
                 for (int ev = 0; ev < trackSize; ev++)
                 {
                     counter++;
                     fireProgressReported("Parsing MIDI...", counter, totalCount);
                     
                     MidiEvent event = tracks[t].get(ev);
                     MidiMessage message = event.getMessage();
                     
                     Logger.getRootLogger().trace("Message received: " + message);
                     
                    // *** ISSUE 55 MARKUP ***
                    // (Remove when the issue is resolved - but replace with some descriptive comments!)

                    // Here's the original line that was called next:
//                  parse(message, event.getTick());  
                    
                    // Here's a revised line that seems to work better, but the question
                    // is what value should resolution be divided by.
                    //
                    // For taha_picturebook.mid (available in Issue 55), a value of 120.0 works
                    // (actually, 120.0 results in music that sounds a little too fast)
                    // The Resolution of taha_picturebook.mid is 960, the MIDI File Type is 1, and
                    // the Division Type is PPQ.
                    //
                    // For hiphopBb4.mid (available in Issue 55), a value of 30.0 works.
                    // The Resolution of hiphopBb4.mid is 120, the MIDI File Type is 1, and
                    // the Division Type is PPQ.
                    parse(message, (int)(event.getTick() / (resolution / 120.0)));   
                 }
             }
         }
     }
 
     /** 
      * Delegator method that calls specific parsers depending on the
      * type of MidiMessage passed in.
      * @param message the message to parse
      * @param timestamp the time at which the message was encountered in this track
      */
     public void parse(MidiMessage message, long timestamp)
     {
         if (message instanceof ShortMessage)
         {
             parseShortMessage((ShortMessage)message, timestamp);
         }
         else if (message instanceof SysexMessage)
         {
             parseSysexMessage((SysexMessage)message, timestamp);
         }
         else if (message instanceof MetaMessage)
         {
             parseMetaMessage((MetaMessage)message, timestamp);
         }
     }
     
     /**
      * Parses instances of ShortMessage. 
      * @param message The message to parse
      * @param timestamp the time at which the message was encountered in this track
      */
     private void parseShortMessage(ShortMessage message, long timestamp)
     {
         int track = message.getChannel();
         
         switch (message.getCommand())
         {
             case ShortMessage.PROGRAM_CHANGE :                  // 0xC0, 192
                 Logger.getRootLogger().trace("Program change to " + message.getData1());
                 Instrument instrument = new Instrument((byte)message.getData1());
                 fireTimeEvent(new Time(timestamp));
                 fireVoiceEvent(new Voice((byte)track));
                 fireInstrumentEvent(instrument);
                 break;
                 
             case ShortMessage.CONTROL_CHANGE :                  // 0xB0, 176
                 Logger.getRootLogger().trace("Controller change to " + message.getData1() + ", value = " + message.getData2());                                    
                 Controller controller = new Controller((byte)message.getData1(), (byte)message.getData2());
                 fireTimeEvent(new Time(timestamp));
                 fireVoiceEvent(new Voice((byte)track));
                 fireControllerEvent(controller);
                 break;
             case ShortMessage.NOTE_ON :                         // 0x90, 144
                 if (message.getData2() == 0) {
                     // A velocity of zero in a note-on event is a note-off event
                     noteOffEvent(timestamp, track, message.getData1(), message.getData2());
                 } else {
                     noteOnEvent(timestamp, track, message.getData1(), message.getData2());
                 }
                 break;
             case ShortMessage.NOTE_OFF :                        // 0x80, 128
                 noteOffEvent(timestamp, track, message.getData1(), message.getData2());
                 break;
             case ShortMessage.CHANNEL_PRESSURE :                // 0xD0, 208
                 Logger.getRootLogger().trace("Channel pressure, pressure = " + message.getData1());                                    
                 ChannelPressure pressure = new ChannelPressure((byte)message.getData1());
                 fireTimeEvent(new Time(timestamp));
                 fireVoiceEvent(new Voice((byte)track));
                 fireChannelPressureEvent(pressure);
                 break;
             case ShortMessage.POLY_PRESSURE :                   // 0xA0, 160
                 Logger.getRootLogger().trace("Poly pressure on key " + message.getData1() + ", pressure = " + message.getData2());                                    
                 PolyphonicPressure poly = new PolyphonicPressure((byte)message.getData1(), (byte)message.getData2());
                 fireTimeEvent(new Time(timestamp));
                 fireVoiceEvent(new Voice((byte)track));
                 firePolyphonicPressureEvent(poly);
                 break;
             case ShortMessage.PITCH_BEND :                      // 0xE0, 224
                 Logger.getRootLogger().trace("Pitch Bend, data1= " + message.getData1() + ", data2= " + message.getData2());
                 PitchBend bend = new PitchBend((byte)message.getData1(),  (byte)message.getData2());
                 fireTimeEvent(new Time(timestamp));
                 fireVoiceEvent(new Voice((byte)track));
                 firePitchBendEvent(bend);
                 break;
             default : 
                 Logger.getRootLogger().trace("Unparsed message: " + message.getCommand());
                 break;
         }
     }
 
     private void noteOnEvent(long timestamp, int track, int data1, int data2)
     {
         Logger.getRootLogger().trace("Note on " + data1 + " - attack is " + data2);
         tempNoteRegistry[track][data1] = timestamp;
         tempNoteAttackRegistry[track][data1] = (byte)data2;
 
         // Added 9/27/2008 - fire a Note with duration 0 to signify a that a Note was pressed 
         Note note = new Note((byte)data1, 0);
         note.setDecimalDuration(0);
         note.setAttackVelocity((byte)data2);
         fireNoteEvent(note);
     }
     
     private void noteOffEvent(long timestamp, int track, int data1, int data2)
     {
         long time = tempNoteRegistry[track][data1];
         Logger.getRootLogger().trace("Note off " + data1 + " - decay is " + data2 + ". Duration is " + (timestamp - time)+"ms");
 
         fireTimeEvent(new Time(time));
         fireVoiceEvent(new Voice((byte)track));
         Note note = new Note((byte)data1, (long)(timestamp - time));
 //        double decimalDuration = (timestamp - time)*1.0 / (resolution * 4.0); // TODO: This will work for PPQ, but what about SMPTE division type?
         double decimalDuration = (timestamp - time)*1.0 / resolution; // TODO: This will work for PPQ, but what about SMPTE division type?
         note.setDecimalDuration(decimalDuration);
         note.setAttackVelocity(tempNoteAttackRegistry[track][data1]);
         note.setDecayVelocity((byte)data2);
         fireNoteEvent(note);
         tempNoteRegistry[track][data1] = 0L;
     }
         
     /**
      * Parses instances of SysexMessage. 
      * @param message The message to parse
      * @param timestamp the time at which the message was encountered in this track
      */
     private void parseSysexMessage(SysexMessage message, long timestamp)
     {
     	SystemExclusive sysex = new SystemExclusive(message.getData());
     	fireSystemExclusiveEvent(sysex);
     }
 
     /**
      * Parses instances of MetaMessage. 
      * @param message The message to parse
      * @param timestamp the time at which the message was encountered in this track
      */
     private void parseMetaMessage(MetaMessage message, long timestamp)
     {
         switch (message.getType())
         {
           case 0x51 : parseTempo(message, timestamp); break;
           case 0x59 : Logger.getRootLogger().trace("KeySignature received but not parsed by JFugue (doesn't use them)");
            // Even though we care about Key Signatures, we don't want to read one in from a MIDI file,
            // because the notes that we'll receive will already be adjusted for the key signature.
            // MIDI's Key Signature is more about notating sheet music that influencing the played notes.
           default : 
               Logger.getRootLogger().trace("MetaMessage " + message.getType() + " (0x" + Integer.toHexString(message.getType()) + ") received but not parsed by JFugue (doesn't use them)");
               break;
         }
     }
     
     private void parseTempo(MetaMessage message, long timestamp)
     {
         int beatsPerMinute = (int)(TimeFactor.convertMicrosecondsPerBeatToBPM(TimeFactor.parseMicrosecondsPerBeat(message)) * 4.0);
         Logger.getRootLogger().trace("Tempo Event, bpm = " + beatsPerMinute);
         fireTimeEvent(new Time(timestamp));
         fireTempoEvent(new Tempo(beatsPerMinute));
         this.tempo = beatsPerMinute;
     }
 }
