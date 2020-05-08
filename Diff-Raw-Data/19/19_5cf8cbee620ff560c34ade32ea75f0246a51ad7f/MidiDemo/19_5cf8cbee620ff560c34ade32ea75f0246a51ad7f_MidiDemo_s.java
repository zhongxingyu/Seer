 package net.cscott.sdr.sound;
 /*
  *	Derived from LoopingMidiPlayer15.java, from jsresources.org
  *      Requires JDK1.5 or later.
  */
 
 /* Original file copyright (c) 1999 - 2001 by Matthias Pfisterer
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /* for our test files, tempo is 120bpm, and resolution is 384 PPQ.  This
  * means that a midi tick is 1/768s, which is more than adequate resolution
  * for (say) 15-30fps animation. */
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.midi.MetaEventListener;
 import javax.sound.midi.MetaMessage;
 import javax.sound.midi.Sequence;
 import javax.sound.midi.Sequencer;
 import javax.sound.midi.Soundbank;
 import javax.sound.midi.Synthesizer;
 import javax.sound.midi.Receiver;
 import javax.sound.midi.Transmitter;
 
 
 
 
 /**	<titleabbrev>MidiDemo</titleabbrev>
 	<title>Looping a MIDI file (JDK1.5 and later)</title>
 
 	<formalpara><title>Purpose</title>
 
 	<para>Loops a MIDI file using the new looping methods in the
 	Sequencer of the JDK1.5.</para></formalpara>
 
 	<formalpara><title>Usage</title>
 	<para>
 	<cmdsynopsis>
 	<command>java MidiDemo</command>
 	<arg choice="plain"><replaceable>midifile</replaceable></arg>
 	</cmdsynopsis>
 	</para></formalpara>
 
 	<formalpara><title>Parameters</title>
 	<variablelist>
 	<varlistentry>
 	<term><option><replaceable>midifile</replaceable></option></term>
 	<listitem><para>the name of the MIDI file that should be
 	played</para></listitem>
 	</varlistentry>
 	</variablelist>
 	</formalpara>
 
 	<formalpara><title>Bugs, limitations</title>
 
 	<para>This program always uses the default Sequencer and the default
 	Synthesizer to play on. For using non-default sequencers,
 	synthesizers or to play on an external MIDI port, see
 	<olink targetdoc="MidiPlayer"
 	targetptr="MidiPlayer">MidiPlayer</olink>.</para>
 
 	<para>This program requires the JDK 1.5. For looping using the JDK
 	1.4 or earlier, see <olink targetdoc="LoopingMidiPlayer14"
 	targetptr="LoopingMidiPlayer14">LoopingMidiPlayer14</olink>.</para>
 
 	</formalpara>
 
 	<formalpara><title>Source code</title>
 	<para>
 	<ulink url="MidiDemo.java.html">MidiDemo.java</ulink>
 	</para>
 	</formalpara>
 
 */
 public class MidiDemo
 {
     public static void main(String[] args)
 	throws MidiUnavailableException, InvalidMidiDataException, IOException
     {
 	final Sequencer sequencer;
 	final Synthesizer synthesizer;
 
 	/* Use high-quality soundbank. */
 	Soundbank soundbank = MidiSystem.getSoundbank
 	    (MidiDemo.class.getClassLoader().getResource
 	     ("net/cscott/sdr/sound/soundbank-deluxe.gm"));
 	soundbank=null;
 
 	/* We read in the MIDI file to a Sequence object.  This object
 	 * is set at the Sequencer later.
 	 */
 	Sequence sequence = MidiSystem.getSequence
 	    (MidiDemo.class.getClassLoader().getResource
 	     ("net/cscott/sdr/sound/saturday-night.midi"));
 
 	// print out some info about timing resolution.
 	System.out.println("Division type: "+sequence.getDivisionType());
 	System.out.println("Resolution: "+sequence.getResolution());
 	assert sequence.getDivisionType()==sequence.PPQ :
 	    "don't know how to sync non-PPQ tracks";
 	int ticksPerBeat = sequence.getResolution();
 
 	/* Now, we need a Sequencer to play the sequence.  Here, we
 	 * simply request the default sequencer without an implicitly
 	 * connected synthesizer
 	 */
 	sequencer = MidiSystem.getSequencer(false);
 
 	/* The Sequencer is still a dead object.  We have to open() it
 	 * to become live.  This is necessary to allocate some
 	 * ressources in the native part.
 	 */
 	sequencer.open();
 
 	/* Next step is to tell the Sequencer which Sequence it has to
 	 * play. In this case, we set it as the Sequence object
 	 * created above.
 	 */
 	sequencer.setSequence(sequence);
 	System.out.println("Initial tempo: "+sequencer.getTempoInMPQ()+" MPQ");
 	// Mississippi Sawyer is *half note*=120; Java doesn't seem to grok
 	// this.  Something like the following is needed for these cases:
 	// sequencer.setTempoFactor(2f);
 
 	/* We try to get the default synthesizer, open() it and chain
 	 * it to the sequencer with a Transmitter-Receiver pair.
 	 */
 	synthesizer = MidiSystem.getSynthesizer();
 	synthesizer.open();
 	boolean loaded = false;
 	if (soundbank!=null)
 	    loaded = synthesizer.loadAllInstruments(soundbank);
 	System.out.println("Instruments "+(loaded?"":"not ")+"loaded.");
 	Receiver	synthReceiver = synthesizer.getReceiver();
 	Transmitter	seqTransmitter = sequencer.getTransmitter();
 	seqTransmitter.setReceiver(synthReceiver);
 
 	/* To free system resources, it is recommended to close the
 	 * synthesizer and sequencer properly.
 	 *
 	 * To accomplish this, we register a Listener to the
 	 * Sequencer. It is called when there are "meta" events. Meta
 	 * event 47 is end of track.
 	 *
 	 * Thanks to Espen Riskedal for finding this trick.
 	 */
 	sequencer.addMetaEventListener(new MetaEventListener()
 	    {
 		public void meta(MetaMessage event)
 		{
 		    if (event.getType() == 47)
 			{
 			    sequencer.close();
 			    if (synthesizer != null)
 				{
 				    synthesizer.close();
 				}
 			    //System.exit(0);
 			}
 		}
 	    });
 
 	/* Here, we set the loop points to loop over the whole
 	 * sequence. Setting the loop end point to -1 means using the
 	 * last tick of the sequence as end point of the loop.
 	 *
 	 * Furthermore, we set the number of loops to loop infinitely.
 	 */
 	sequencer.setLoopStartPoint(0);
 	sequencer.setLoopEndPoint(-1);
 	sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
 
 	/* Now, we can start over.
 	 */
 	sequencer.start();
 
 	/* check timer */
 	MidiTimer mt = new MidiTimer(sequencer);
 	while(true) {
 	    System.out.println(mt.getTime());
 	    try {
 		Thread.sleep(1000);
 	    } catch (InterruptedException ie) { /* ignore */ }
 	}
     }
 


     private static void out(String strMessage)
     {
 	System.out.println(strMessage);
     }
 }
 
 
 
 /*** MidiDemo.java ***/
 
