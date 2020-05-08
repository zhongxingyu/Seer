 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mc.softphone.media;
 
 import java.io.IOException;
 
 import java.lang.reflect.Constructor;
 
 import java.util.prefs.Preferences;
 import java.util.ArrayList;
 
 import com.sun.voip.Logger;
 
 import com.sun.mc.softphone.common.Utils;
 
 import com.sun.mc.softphone.media.alsa.AlsaAudioServiceProvider;
 import com.sun.mc.softphone.media.coreaudio.CoreAudioAudioServiceProvider;
 import com.sun.mc.softphone.media.javasound.JavasoundAudioServiceProvider;
 
 public class AudioFactory {
 
     private static AudioServiceProvider audioServiceProvider;
 
     private AudioFactory() {
 	if (audioServiceProvider != null) {
 	    return;
 	}
 
 	String s = System.getProperty(
 	    "com.sun.mc.softphone.media.AUDIO_SERVICE_PROVIDER_CLASS");
 
 	if (s != null) {
 	    try {
                 Class micClass = Class.forName(s);
                 Class[] params = new Class[] { };
 
                 Constructor constructor = micClass.getConstructor(params);
 
                 if (constructor != null) {
                     Object[] args = new Object[] { };
 
                     audioServiceProvider = (AudioServiceProvider) 
 			constructor.newInstance(args);
 
 		    Logger.println("Using specified audio service provider:  "
 			+ s);
 		    return;
 		}
 
                 Logger.println("constructor not found for: " + s);
             } catch (Exception e) {
                 Logger.println("Error loading '" + s + "': " 
 		    + e.getMessage());
             }
 	}
 
         if (Utils.isMacOS()) {
 	    try {
                 audioServiceProvider = new CoreAudioAudioServiceProvider();
             } catch (Throwable e) {
		Logger.println("Unable to load Mac Core Audio "
		    + "native library: " + e.getMessage());
		Logger.println("THE SOFTPHONE WILL NOT WORK PROPERLY!");
 	    }
 	} else if (Utils.isLinux()) {
 	    try {
 		if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		    Logger.println("Using AlsaAudioServiceProvider");
 		}
 	        audioServiceProvider = new AlsaAudioServiceProvider();
             } catch (Throwable e) {
                 Logger.println("Unable to load ALSA Audio "
 		    + "native library, resorting to javasound. " 
 		    + e.getMessage());
 	    }
 	}
 
 	if (audioServiceProvider == null) {
 	    audioServiceProvider = new JavasoundAudioServiceProvider();
 	}
     }
 
     public static AudioFactory getInstance() {
 	return new AudioFactory();
     }
 
     public void initialize(int sampleRate, int channels, 
 	    int microphoneSampleRate, int microphoneChannels,
 	    int microphoneBufferSize, int speakerBufferSize) 
 	    throws IOException {
 
 	audioServiceProvider.initialize(sampleRate, channels,
 	    microphoneSampleRate, microphoneChannels,
 	    microphoneBufferSize, speakerBufferSize);
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Audio system initialized " 
 	        + sampleRate + "/" + channels);
 	}
     }
 
     public void shutdown() {
 	audioServiceProvider.shutdown();
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Audio system shutdown");
 	}
     }
 
     public Microphone getMicrophone() {
 	return audioServiceProvider.getMicrophone();
     }
 
     public String[] getMicrophoneList() {
 	return audioServiceProvider.getMicrophoneList();
     }
 
     public Speaker getSpeaker() {
 	return audioServiceProvider.getSpeaker();
     }
 
     public String[] getSpeakerList() {
 	return audioServiceProvider.getSpeakerList();
     }
 
 }
