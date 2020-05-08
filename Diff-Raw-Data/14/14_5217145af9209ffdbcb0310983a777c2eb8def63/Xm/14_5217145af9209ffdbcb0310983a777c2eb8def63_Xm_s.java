/* $Id: Xm.java,v 1.10 2003/08/21 09:27:06 fredde Exp $
  * Copyright (C) 2000-2003 Fredrik Ehnbom
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.gjt.fredde.silence.format.xm;
 
 import java.io.*;
 import java.net.URL;
 
 import org.gjt.fredde.silence.format.AudioFormat;
 
 /**
  * The general xm class
  *
  * @author Fredrik Ehnbom
 * @version $Revision: 1.10 $
  */
 public class Xm
 	extends AudioFormat
 {
 	public String title = "";
 	int patorder[];
 
 	int defaultTempo = 0;
 	int defaultBpm = 0;
 
 	int tick = 0;
 
 	int deviceSampleRate = 0;
 	int samplesPerTick;
 
 	int playingPatternPos = 0;
 	int playingPattern = 0;
 	int patternPos = 0;
 	int globalVolume = 64;
 
 	private int restTick = 0;
 
 
 	private	Pattern[]	pattern;
 	protected Instrument[]	instrument;
 	private	Channel[]	channel;
 
 	public Xm() {
 	}
 
 	public static final byte[] read(BufferedInputStream in, int len)
 		throws IOException
 	{
 		byte[] b = new byte[len];
 		for (int i = 0; i < len; i += in.read(b, i, len-i));
 		return b;
 	}
 
 	public static final int make32Bit(byte[] b) {
 		return make32Bit(b, 0);
 	}
 	public static final int make32Bit(byte[] b, int off) {
 		return (
 				((b[off + 0] & 0xff) << 0) +
 				((b[off + 1] & 0xff) << 8) +
 				((b[off + 2] & 0xff) << 16) +
 				((b[off + 3] & 0xff) << 24)
 			);
 	}
 
 	public static final int make16Bit(byte[] b) {
 		return make16Bit(b, 0);
 	}
 
 	public static final int make16Bit(byte[] b, int off) {
 		return (
 				((b[off + 0] & 0xff) << 0) +
 				((b[off + 1] & 0xff) << 8)
 			);
 	}
 
 	/**
 	 * Load the file into memory
  	 *
 	 * @param is The InputStream to read the file from
 	 */
 	public void load(BufferedInputStream in)
 		throws IOException
 	{
 		readGeneralInfo(in);
 
 		for (int i = 0; i < pattern.length; i++) {
 			pattern[i] = new Pattern(in);
 		}
 
 		for (int i = 0; i < instrument.length; i++) {
 			instrument[i] = new Instrument(in);
 		}
 		in.close();
 	}
 
 	private void readGeneralInfo(BufferedInputStream in)
 		throws IOException
 	{
 		// "Extended Module: "
 		byte b[] = read(in, 17);
 
 		if (!(new String(b)).equalsIgnoreCase("Extended Module: ")) {
 			throw new IOException("This is not a xm file!");
 		}
 
 		// Module name
 		b = read(in, 20);
 		title = new String(b);
 
 		in.read();
 
 		// Tracker name
 		b = read(in, 20);
 
 		// version
 		in.read();
 		in.read();
 
 		// Header size
 		read(in, 4);
 
 		// Song length (in pattern order table)
 		patorder = new int[make16Bit(read(in, 2))];
 
 		// Restart position
 		read(in, 2);
 
 		// Number of channels (2,4,6,8,10,...,32)
 		channel = new Channel[make16Bit(read(in, 2))];
 
 		for (int i = 0; i < channel.length; i++) {
 			channel[i] = new Channel(this);
 		}
 
 		// Number of patterns (max 128)
 		pattern = new Pattern[make16Bit(read(in, 2))];
 
 		// Number of instruments (max 128)
 		instrument = new Instrument[make16Bit(read(in, 2))];
 
 		// Flags: bit 0: 0 = Amiga frequency table;
 		//               1 = Linear frequency table
 		read(in, 2);
 
 		// Default tempo
 		defaultTempo = make16Bit(read(in, 2));
 
 		// Default BPM
 		defaultBpm = make16Bit(read(in, 2));
 
 		// Pattern order table
 		b = read(in, 256);
 
 		for (int i = 0; i < patorder.length; i++) {
 			patorder[i] = b[i];
 		}
 		playingPatternPos = 0;
 		playingPattern = patorder[playingPatternPos];
 	}
 
 	/**
 	 * Play...
 	 */
 	public int read(int[] buffer, int off, int len) {
 		int realLen = len;
 
 		for (int i = off; i < off+len; i++) {
 			buffer[i] = 0;
 		}
 
 		if (restTick > 0) {
 			int read = restTick > len ? len : restTick;
 			for (int j = 0; j < channel.length; j++)  {
 				channel[j].play(buffer, off, read);
 			}
 
 			off += read;
 			len -= read;
 			restTick -= read;
 		}
 
 		for (int i = off; i < off+len; i += samplesPerTick) {
			if (++tick == defaultTempo) {
 				for (int j = 0; j < channel.length; j++)  {
 					patternPos = channel[j].update(pattern[playingPattern], patternPos);
 
 					if (channel[j].currentEffect == 0x0D) { // pattern break
 						for (int rest = j+1; rest < channel.length; rest++) {
 							patternPos = channel[rest].update(pattern[playingPattern], patternPos);
 						}
 						playingPatternPos++;
 						playingPattern = patorder[playingPatternPos];
 						channel[j].currentEffect = -1;
 
 						patternPos = 0;
 
 						int endRow = channel[j].currentEffectParam;
 
 						for (int rows = 0; rows < endRow; rows++) {
 							for (int chan = 0; chan < channel.length; chan++) {
 								patternPos = channel[chan].skip(pattern[playingPattern], patternPos);
 							}
 						}
 
 						break;
 					}
 				}
 
 				if (patternPos == pattern[playingPattern].data.length) {
 					patternPos = 0;
 					playingPatternPos++;
 					if (playingPatternPos == patorder.length) {
 						// TODO: end.....
 						playingPatternPos = 0;
 					}
 					playingPattern = patorder[playingPatternPos];
 				}
 				tick = 0;
 			}
 			int read = samplesPerTick;
 			if (read > off+len-i) {
 				read = off+len-i;
 				restTick = samplesPerTick - read;
 			}
 
 			for (int j = 0; j < channel.length; j++)  {
 				channel[j].updateTick();
 				channel[j].play(buffer, i, read);
 			}
 		}
 
 		return realLen;
 	}
 
 	public void setDevice(org.komplex.audio.AudioOutDevice device) {
 		super.setDevice(device);
 		deviceSampleRate = device.getSampleRate();
 		samplesPerTick = (5 * deviceSampleRate) / (2 * defaultBpm);
 	}
 
 	public void close() {}
 
 	public String toString() {
 		return "Extended Module";
 	}
 }
 /*
  * ChangeLog:
  * $Log: Xm.java,v $
  * Revision 1.10  2003/08/21 09:27:06  fredde
  * tempo -> tick
  *
  * Revision 1.9  2001/01/11 20:25:37  fredde
  * added custom toString
  *
  * Revision 1.8  2001/01/08 19:49:35  fredde
  * updated now that the AudioFormat just saves the device
  * instead of lots of data in different variables
  *
  * Revision 1.7  2001/01/04 18:56:42  fredde
  * added close method
  *
  * Revision 1.6  2000/12/21 17:21:42  fredde
  * load(in) is public
  *
  * Revision 1.5  2000/10/14 19:12:24  fredde
  * added the make[16|32]Bit functions
  * removed debugging messages
  *
  * Revision 1.4  2000/10/08 18:03:10  fredde
  * fixed "pattern break" (Dxx) command
  *
  * Revision 1.3  2000/10/07 13:55:07  fredde
  * Fixed to read in the data correctly.
  * Fixed to play the .xm correctly.
  *
  * Revision 1.2  2000/10/01 17:09:31  fredde
  * added lots of code for acctual playback
  *
  * Revision 1.1.1.1  2000/09/25 16:34:33  fredde
  * initial commit
  *
  */
