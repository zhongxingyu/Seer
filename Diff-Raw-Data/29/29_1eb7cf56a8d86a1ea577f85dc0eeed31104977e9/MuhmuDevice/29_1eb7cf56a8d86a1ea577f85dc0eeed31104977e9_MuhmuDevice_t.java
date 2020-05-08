 /* MuhmuDevice.java - An audiodevice which uses MuhmuAudio
  * Copyright (C) 2000 Fredrik Ehnbom
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
 package silence.devices;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Hashtable;
 
 import org.komplex.audio.AudioConstants;
 import org.komplex.audio.AudioOutDevice;
 import org.komplex.audio.AudioOutDeviceFactory;
 import org.komplex.audio.PullAudioSource;
 
 import silence.AudioException;
 import silence.format.AudioFormat;
 
 /**
  * An audiodevice which uses MuhmuAudio by Jarno Heikkinen
  * &lt;<a href="mailto:jarnoh@komplex.org">jarnoh@komplex.org</a>&gt;.<br>
  * For more information about MuhmuAudio please visit
  * <a href="http://muhmuaudio.sourceforge.net">http://muhmuaudio.sourceforge.net</a>
  * @author Fredrik Ehnbom
 * @version $Id: MuhmuDevice.java,v 1.5 2000/08/20 17:55:48 quarn Exp $
  */
 public class MuhmuDevice extends AudioDevice {
 
 	private AudioOutDevice  device  = null;
 	private AudioFormat 	     format = null;
 
 	/**
 	 * Create a new MuhmuDevice
 	 */
 	public MuhmuDevice() {
 	}
 
 	/**
 	 * Returns the name of this device
 	 */
	public String toString() {
 		return "MuhmuAudio";
 	}
 
 	/**
 	 * Returns the home url for this device
 	 */
 	public String getUrl() {
 		 return "http://muhmuaudio.sourceforge.net";
 	}
 
 	/**
 	 * Init the AudioDevice
 	 * @param sound If we should play the sound or if we are in nosound mode
 	 */
 	public void init(boolean sound) throws AudioException {
 		AudioOutDeviceFactory factory = new AudioOutDeviceFactory();
 
 		if (sound == false) {
 			String[] args = {"-nosound"};
 			factory.initArgs(args);
 		}
 
 		while (device == null) {
 			try {
 				device = factory.getAudioOutDevice();
 
 				if (device == null) {
 					break;
 				}
 
 				// directsound requires a component reference
 				Hashtable hash = new Hashtable();
 				hash.put(AudioConstants.PROP_COMPONENT, this);
 				device.setProperties(hash);
 
 				device.init(device.FORMAT_PCM44K16S);
 
 				break;
 			} catch(Throwable e) {
 				factory.disableDevice(device);
 				device = null;
 			}
 		}
 
 		if (device == null) {
 			throw new AudioException("Could not find a device to use...");
 		}
 	}
 
 	/**
	 * Loads the file
	 * @param file The file to load
 	 */
	public void load(String file) throws AudioException {
 		if (file.indexOf(".") == -1) throw new AudioException(file + " can not be played by this device");
 
 		String end = file.substring(file.lastIndexOf("."), file.length());
 
 		// get the AudioFormat for this file
 		format = AudioFormat.getFormat(end);
 
 		if (format == null) throw new AudioException(file + " can not be played by this device");
 
		format.setSampleRate(device.getSampleRate());

 		// load the file
 		try {
 			format.load(file);
 		} catch (Exception e) {
 			throw new AudioException(e.toString());
 		}
 
 		device.setPullSource(format);
	}
 
	/**
	 * Start playing the file
	 * @param loop Wheter to loop or not
	 */
	public void play(boolean loop) throws AudioException {
 		try {
 			device.start();
 		} catch (org.komplex.audio.AudioException e) {
 			throw new AudioException(e.toString());
 		}
 	}
 
 	/**
 	 * Stop playing the file
 	 */
 	public void stop() {
 		device.stop();
 	}
 
 	/**
 	 * Pause the playing of the file
 	 */
 	public void pause() {}
 
 	/**
 	 * Sets the volume
 	 * @param volume The new volume
 	 */
 	public void setVolume(int volume) {}
 
 	/**
 	 * Close and cleanup
 	 */
 	public void close() {}
 }
 /*
  * ChangeLog:
  * $Log: MuhmuDevice.java,v $
 * Revision 1.5  2000/08/20 17:55:48  quarn
 * split play into play and load, some fixes
 *
  * Revision 1.4  2000/07/21 09:40:00  quarn
  * now uses the AudioFormat system
  *
  * Revision 1.3  2000/06/25 18:39:08  quarn
  * removed the sync method
  *
  * Revision 1.2  2000/06/20 23:22:05  quarn
  * no need to throw an exception on pause
  *
  */
