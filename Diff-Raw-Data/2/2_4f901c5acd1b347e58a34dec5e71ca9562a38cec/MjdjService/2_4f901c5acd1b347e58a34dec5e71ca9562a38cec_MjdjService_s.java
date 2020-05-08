 /**
 Mjdj MIDI Morph API - Extension API for Mjdj MIDI Morph, an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 Licensed with GPL 3.0 with Classpath Exception
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions of the GNU General Public License cover the whole combination. 
 
 As a special exception, the copyright holders of this library give you permission to link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this library, you may extend this exception to your version of the library, but you are not obligated to do so. If you do not wish to do so, delete this exception statement from your version.
 
 You may contact the author at mjdj_api [at] confusionists.com
 */
 
 package com.confusionists.mjdjApi.util;
 
 import java.util.List;
 
 import javax.sound.midi.InvalidMidiDataException;
 
 import com.confusionists.mjdjApi.midi.MessageWrapper;
 import com.confusionists.mjdjApi.morph.Morph;
 
 /**
  * MjdjService is the gateway into the Mjdj system: all access to Mjdj is given through a provided MjdjService instance.
  * @author DanielRosenstark [at_sign] confusionists.com
  */
 public interface MjdjService {
 
 	/**
 	 *  Display to the Mjdj logging pane.
 	 */
 	void log(String text);
 
 	/**
 	 *   Display to the Mjdj logging pane.
 	 */
 	void log(String string, Exception exception);
 	
 	
 	/**
 	 *  Display to the Mjdj  logging pane if debugging is on */
 	void debugLog(String text);
 	/**
 	 *  Display to the Mjdj  logging pane if debugging is on */
 	void debugLog(String string, Exception exception);
 
 
 	/**
 	 * Send messages to the Mjdj morphs.
 	 * @param message
 	 * @param from The name of the MIDI Device that received the message. Morphs receive this name in their process method, which may be used for filtering.
 	 */
 	void morph(MessageWrapper message, String from);
 	
 	/**
 	 * @param message
 	 * @param from can be null, name of the device the message is (pretending to be) from
 	 * @param afterMorph can be null. It's a String that matches the getName() of the afterMorph. Only morphs lower on the list than the afterMorph will get the message
 	 */
 	void morph(MessageWrapper message, String from, Morph afterMorph);
 	
 	
 	/**
 	 *  Sends to all outbound MIDI devices   */
 	void send(MessageWrapper message);
 
 	/** 
 	 * @param message
 	 * @param sendToNames List of names of outbound MIDI devices which will receive the message.
 	 */
 	void send(MessageWrapper message, List<String> sendToNames);
 
 	/**
 	 * 
 	 * @param message
 	 * @param sendToName On the device matching this name is sent the message
 	 */
 	void send(MessageWrapper message, String sendToName);
 
 
 	/**
 	 * 
 	 * @param bytes Raw midi message
 	 * @param sendToNames List of names of outbound MIDI devices which will receive the message.
 	 * @throws InvalidMidiDataException
 	 */
 	void send(byte[] bytes, List<String> sendToNames)
 			throws InvalidMidiDataException;
 
 	/**
 	 * Send to all output MIDI devices
 	 * @param bytes
 	 * @throws InvalidMidiDataException
 	 */
 	void send(byte[] bytes) throws InvalidMidiDataException;
 
 	/**
 	 *  Shuts the checkbox of a Morph implementer on or off
 	 * @param name Name of other Morph as returned from getName()
 	 * @param status 
 	 */
 	void setMorphActive(String name, boolean status);
 
 
 	/**
 	 * @param name Name of other Morph as returned from getName()
 	 * @return whether the Morph in question is active (true) or not
 	 */
 	boolean isMorphActive(String name);
 	
 	/*
 	 * sends most keystrokes, otherwise just use Java.awt.Robot yourself, no
 	 * problems
 	 */
 	void sendKeystrokes(String keystrokes);
 
 	/**
 	 * @return The proportion of the beat (quarter note, generally speaking) has elapsed up until right now.
 	 */
 	float getAfterBeat();
 	
 	
 	/**
 	 * @param task 
 	 * @param beatsBeforeLaunch - schedules from RIGHT NOW plus beatsBeforeLaunch beats. Beats refer to quarter-notes, generally. "Right now" means how far after the quarter-note we are now
 	 * from the last quarter note.
 	 * @return true if scheduled, false if scheduling fails for any reason
 	 */
 	boolean schedule(MidiTimerTask task, int beatsBeforeLaunch);
 
 	/**
	 * Schedules the task to fire at beatsBeforeLaunch quarter-notes plus delayAfterBeat milliseconds.
 	 * @param task 
 	 * @param beatsBeforeLaunch
 	 * @param delayAfterBeat - in proportion of a beat
 	 * @return true if scheduled, false if scheduling fails for any reason
 	 */
 	boolean schedule(MidiTimerTask task, int beatsBeforeLaunch,
 			float delayAfterBeat);
 
 	/**
 	 * 
 	 * @param task
 	 * @param delay in milliseconds
 	 * @return false if not scheduled for any reason, otherwise true.
 	 */
 	boolean scheduleInMs(MidiTimerTask task, int delay);
 	
 	/**
 	 * 
 	 * @return true if debug mode is on inside Mjdj, otherwise false.
 	 */
 	boolean isDebug();
 
 }
