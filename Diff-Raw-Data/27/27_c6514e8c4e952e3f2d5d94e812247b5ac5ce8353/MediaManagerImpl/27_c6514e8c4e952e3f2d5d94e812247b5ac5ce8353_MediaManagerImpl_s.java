 /* ====================================================================
  * The Apache Software License, Version 1.1
  *
  * Copyright (c) 2000 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself,
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Apache" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  *
  * Portions of this software are based upon public domain software
  * originally written at the National Center for Supercomputing Applications,
  * University of Illinois, Urbana-Champaign.
  *
  * Copyright 2007 Sun Microsystems, Inc. 
  */
 package com.sun.mc.softphone.media;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 import java.util.Vector;
 
 import com.sun.mc.softphone.SipCommunicator;
 
 import com.sun.mc.softphone.common.*;
 
 import com.sun.mc.softphone.sip.SipManager;
 
 import java.io.InputStreamReader;
 
 import  javax.sound.sampled.LineUnavailableException;
 
 import com.sun.stun.NetworkAddressManager;
 
 import com.sun.voip.Logger;
 import com.sun.voip.MediaInfo;
 import com.sun.voip.RtpPacket;
 import com.sun.voip.RtpSocket;
 import com.sun.voip.RtpSenderPacket;
 import com.sun.voip.SdpManager;
 import com.sun.voip.SdpInfo;
 import com.sun.voip.SpeechDetector;
 import com.sun.voip.Util;
 
 import java.text.ParseException;
 
 /**
  * <p>Title: SIP COMMUNICATOR</p>
  * <p>Description:JAIN-SIP Audio/Video phone application</p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Organisation: LSIIT laboratory (http://lsiit.u-strasbg.fr) </p>
  * <p>Network Research Team (http://www-r2.u-strasbg.fr))</p>
  * <p>Louis Pasteur University - Strasbourg - France</p>
  * <p>Division Chief: Thomas Noel </p>
  * @author Emil Ivov (http://www.emcho.com)
  * @version 1.1
  *
  */
 public class MediaManagerImpl implements MediaManager, MicrophoneListener {
     private static Console console = Console.getConsole(MediaManagerImpl.class);
     private ArrayList listeners = new ArrayList();
 
     private AudioTransmitter audioTransmitter;
     private AudioReceiver audioReceiver;
     private AudioFilePlayer player;
     private RtpSocket rtpSocket;
     private Microphone microphone;
     private Speaker speaker;
 
     private String encryptionKey;
     private String encryptionAlgorithm;
     private boolean disableAudio = false;
 
     private SdpManager sdpManager;
 
     private SdpInfo remoteSdpInfo;
 
     private int speakerSampleRate;
     private int speakerChannels;
 
     private int microphoneSampleRate;
     private int microphoneChannels;
 
     private SipCommunicator sipCommunicator;
     private SipManager sipManager;
 
     boolean started;
 
     public MediaManagerImpl() {
 	String logLevel = 
 	    Utils.getPreference("com.sun.mc.softphone.media.LOG_LEVEL");
 
 	try {
 	    Logger.logLevel = Integer.parseInt(logLevel);
 	} catch (NumberFormatException e) {
 	    Logger.println("Can't set log level to " + logLevel);
 	}
  
         initSdpManager();
 
 	/*
 	 * Initialize monitor microphone to false.
 	 * It's set automatically when the VU Meter is shown.
 	 * It can also be changed while the program is running
 	 * if the user really wants to monitor the microphone.
 	 */
         Utils.setPreference(
 	    "com.sun.mc.softphone.media.MONITOR_MICROPHONE", "false");
     }
 
     public void setSipCommunicator(SipCommunicator sipCommunicator) {
         this.sipCommunicator = sipCommunicator;
     }
 
     public void setSipManager(SipManager sipManager) {
         this.sipManager = sipManager;
     }
 
     private void initSdpManager() {
         Vector supportedMedia = new Vector();
 
         supportedMedia.add(new MediaInfo(
             (byte)0, RtpPacket.PCMU_ENCODING, 8000, 1, false));
 
 	String s = Utils.getPreference(
 	    "com.sun.mc.softphone.USE_TELEPHONE_EVENT_PAYLOAD");
 
 	if (s == null || s.equalsIgnoreCase("false") == false) {
             supportedMedia.add(new MediaInfo(
                 (byte)101, RtpPacket.PCMU_ENCODING, 8000, 1, true));
 
 	    SdpManager.useTelephoneEvent(true);
 	} else {
 	    SdpManager.useTelephoneEvent(false);
 	}
 
         supportedMedia.add(new MediaInfo(
             (byte)102, RtpPacket.PCM_ENCODING, 8000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)103, RtpPacket.PCM_ENCODING, 16000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)104, RtpPacket.PCM_ENCODING, 16000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)105, RtpPacket.PCM_ENCODING, 32000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)106, RtpPacket.PCM_ENCODING, 32000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)107, RtpPacket.PCM_ENCODING, 44100, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)108, RtpPacket.PCM_ENCODING, 44100, 2, false));
 
 if (false) {
         supportedMedia.add(new MediaInfo(
             (byte)109, RtpPacket.PCM_ENCODING, 48000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)110, RtpPacket.PCM_ENCODING, 48000, 2, false));
 }
 
         supportedMedia.add(new MediaInfo(
             (byte)111, RtpPacket.PCMU_ENCODING, 8000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)112, RtpPacket.PCMU_ENCODING, 16000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)113, RtpPacket.PCMU_ENCODING, 16000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)114, RtpPacket.PCMU_ENCODING, 32000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)115, RtpPacket.PCMU_ENCODING, 32000, 2, false));
 
 if (false) {
         supportedMedia.add(new MediaInfo(
             (byte)116, RtpPacket.PCMU_ENCODING, 44100, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)117, RtpPacket.PCMU_ENCODING, 44100, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)118, RtpPacket.PCMU_ENCODING, 48000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)119, RtpPacket.PCMU_ENCODING, 48000, 2, false));
 }
 
         supportedMedia.add(new MediaInfo(
             (byte)120, RtpPacket.SPEEX_ENCODING, 8000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)121, RtpPacket.SPEEX_ENCODING, 8000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)122, RtpPacket.SPEEX_ENCODING, 16000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)123, RtpPacket.SPEEX_ENCODING, 16000, 2, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)124, RtpPacket.SPEEX_ENCODING, 32000, 1, false));
 
         supportedMedia.add(new MediaInfo(
             (byte)125, RtpPacket.SPEEX_ENCODING, 32000, 2, false));
 
         sdpManager = new SdpManager(supportedMedia);
     } 
 
     boolean mediaChanged = false;
 
     public void mediaChanged(String speakerEncoding, int speakerSampleRate, 
 	    int speakerChannels, boolean notifyRemote) {
 
         mediaChanged = true;
 
 	/*
 	 * If there's a call in progress, changes will take effect next call.
 	 */
 	if (isStarted()) {
 	    return;
 	}
 
 	/*
 	 * We need to restart everything.
 	 */
 	try {
 	    restart();
 	} catch (IOException e) {
 	    Logger.println("Media change re-initialization failed! " 
 		+ e.getMessage());
 	}
     }
 
     public void initialize(String encryptionKey, String encryptionAlgorithm,
 	    boolean disableAudio) throws IOException {
 
 	this.encryptionKey = encryptionKey;
 	this.encryptionAlgorithm = encryptionAlgorithm;
 	this.disableAudio = disableAudio;
 
 	initialize();
     }
 
     private int lastAudioPort = 0;
 
     private void initialize() throws IOException {
 	//dontPlayFiles = false;
 
         int speakerSampleRate = 
 	    Utils.getIntPreference("com.sun.mc.softphone.media.SAMPLE_RATE");
 
 	if (speakerSampleRate == 0) {
 	    speakerSampleRate = (int) DEFAULT_SAMPLE_RATE;
 	}
 	
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Speaker sample rate:  " + speakerSampleRate);
 	}
 
         int speakerChannels = 
 	    Utils.getIntPreference("com.sun.mc.softphone.media.CHANNELS");
 
 	if (speakerChannels == 0) {
 	    speakerChannels = (int) DEFAULT_CHANNELS;
 	}
 	
 	String s = Utils.getPreference("com.sun.mc.softphone.media.ENCODING");
 
         int speakerEncoding = RtpPacket.PCM_ENCODING;
 
         if (s != null) {
             if (s.equalsIgnoreCase("PCMU")) {
                 speakerEncoding = RtpPacket.PCMU_ENCODING;
             } else if (s.equalsIgnoreCase("SPEEX")) {
                 speakerEncoding = RtpPacket.SPEEX_ENCODING;
 	    }
         }
 
 	if (speakerSampleRate == 8000 && speakerChannels == 1) {
             speakerEncoding = RtpPacket.PCMU_ENCODING;
 	}
 
 	if (speakerSampleRate != 0 && speakerChannels != 0) {
 	    try {
 	        sdpManager.setPreferredMedia(speakerEncoding, 
 		    speakerSampleRate, speakerChannels);
 	    } catch (ParseException e) {
 		Logger.println("Can't set preferred media to " 
 		    + speakerSampleRate + "/" + speakerChannels);
 	    }
 	}
 
         int microphoneSampleRate =
             Utils.getIntPreference(
 		"com.sun.mc.softphone.media.TRANSMIT_SAMPLE_RATE");
 
         if (microphoneSampleRate == 0) {
             microphoneSampleRate = (int) DEFAULT_SAMPLE_RATE;
         }
 
         int microphoneChannels =
             Utils.getIntPreference(
 		"com.sun.mc.softphone.media.TRANSMIT_CHANNELS");
 
         if (microphoneChannels == 0) {
             microphoneChannels = (int) DEFAULT_CHANNELS;
         }
 
         s = Utils.getPreference(
 	    "com.sun.mc.softphone.media.TRANSMIT_ENCODING");
 
         int microphoneEncoding = RtpPacket.PCM_ENCODING;
 
         if (s != null) {
             if (s.equalsIgnoreCase("PCMU")) {
                 microphoneEncoding = RtpPacket.PCMU_ENCODING;
             } else if (s.equalsIgnoreCase("SPEEX")) {
                 microphoneEncoding = RtpPacket.SPEEX_ENCODING;
 	    }
         }
 
         if (microphoneSampleRate == 8000 && microphoneChannels == 1) {
             microphoneEncoding = RtpPacket.PCMU_ENCODING;
         }
 
 	try {
             sdpManager.setTransmitMediaInfo(microphoneEncoding, 
 		microphoneSampleRate, microphoneChannels);
 
 	    if (Logger.logLevel >= Logger.LOG_INFO) {
 	        Logger.println("xmit mediainfo set to "
 		    + microphoneEncoding + "/" + microphoneSampleRate + "/"
 		    + microphoneChannels);
 	    }
         } catch (ParseException e) {
             Logger.println("Can't set transmit media info to " 
 		+ SipCommunicator.getPrivateLocalAddress() + microphoneChannels);
         }
 
 	try {
             s = Utils.getPreference("com.sun.mc.softphone.media.AUDIO_PORT");
 
 	    int audioPort = 0;
 
 	    if (s != null && s.length() > 0) {
 		audioPort = Integer.parseInt(s);
 
 		if (audioPort == -1) {
 		    audioPort = 0;
 		}
 	    }
 
 	    if (audioPort == 0) {
 		audioPort = lastAudioPort;
 	    }
 
             if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("audioPort is " + audioPort);
 	    }
 
 	    rtpSocket = new RtpSocket(SipCommunicator.getPrivateLocalAddress(), 
 		audioPort);
 
 	    lastAudioPort = rtpSocket.getInetSocketAddress().getPort();
 
 	    //if (disableAudio == false) {
             //    setupAudio(speakerSampleRate, speakerChannels,
 	    //	      microphoneSampleRate, microphoneChannels);
 	    //}
 
 	    if (audioReceiver != null) {
 		audioReceiver.done();
 	    }
 
 	    audioReceiver = new AudioReceiver(rtpSocket, 
 		encryptionKey, encryptionAlgorithm);
 
             if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("new audioReceiver... " + audioReceiver);
 	    }
 
 	    if (audioTransmitter != null) {
 		audioTransmitter.done();
 	    }
 
 	    audioTransmitter = new AudioTransmitter(rtpSocket, 
 		encryptionKey, encryptionAlgorithm);
 
             if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("new audioTransmitter... " + audioTransmitter);
 	    }
 
 	    audioReceiver.setAudioTransmitter(audioTransmitter);
 	    audioTransmitter.setAudioReceiver(audioReceiver);
 	} catch (SocketException e) {
 	    throw new IOException(e.getMessage());
 	} catch (IOException e) {
             if (speaker != null) {
                 speaker.done();
             }
 
             if (microphone != null) {
                 microphone.done();
             }
 
             if (audioTransmitter != null) {
                 audioTransmitter.done();
             }
 
             throw new IOException(e.getMessage());
         }
     }
 
     private void setupAudio(int speakerSampleRate, int speakerChannels,
 	    int microphoneSampleRate, int microphoneChannels) 
 	    throws IOException {
 
 	if (isStarted() && sameParameters(speakerSampleRate, speakerChannels,
 		microphoneSampleRate, microphoneChannels)) {
 
 	    return;  // if there's a call in progress don't shutdown
 	}
 	
 	shutdownAudio();
 
 	this.speakerSampleRate = speakerSampleRate;
 	this.speakerChannels = speakerChannels;
 
 	this.microphoneSampleRate = microphoneSampleRate;
 	this.microphoneChannels = microphoneChannels;
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Setting speaker to " + speakerSampleRate
 	        + "/" +  speakerChannels);
 
 	    Logger.println("Setting microphone to " + microphoneSampleRate
 	        + "/" +  microphoneChannels);
 	}
 
         int microphoneBufferSizeMillis = Utils.getIntPreference(
             Microphone.BUFFER_SIZE_PROPERTY);
 
         if (microphoneBufferSizeMillis <= 0) {
             microphoneBufferSizeMillis = Microphone.DEFAULT_BUFFER_SIZE;
         }
 
 	/*
 	 * On Windows and Solaris we intentionally keep the microphone 
 	 * buffer size small because a read of 20ms of data doesn't 
 	 * complete until the buffer is full.
 	 * A bigger buffer means more latency.
 	 */
 	if (Utils.isLinux() || Utils.isMacOS()) {
 	    int min = Utils.getIntPreference(
 		Microphone.MINIMUM_BUFFER_SIZE_PROPERTY);
 
 	    if (min <= 0) {
 		min = Microphone.DEFAULT_MINIMUM_BUFFER_SIZE;
 
 		if (Logger.logLevel >= Logger.LOG_INFO) {
 		    Logger.println("Setting default minimum Mac Buffer size to " 
 			+ min);
 		}
 	    }
 		
 	    if (microphoneBufferSizeMillis < min) {
 	        Logger.println("Microphone buffer size milliseconds " 
 		    + microphoneBufferSizeMillis + " is too small. "
 		    + " Resetting to " + min + ".");
 
 		microphoneBufferSizeMillis = min;
 
 	        Utils.setPreference(Microphone.BUFFER_SIZE_PROPERTY,
 		    String.valueOf(min));
 	    }
 	}
 
         int speakerBufferSizeMillis = Utils.getIntPreference(
             Speaker.BUFFER_SIZE_PROPERTY);
 
         if (speakerBufferSizeMillis <= 0) {
             speakerBufferSizeMillis = Speaker.DEFAULT_BUFFER_SIZE;
         }
 
         int microphoneBytesPerPacket = 
 	    RtpPacket.getDataSize(RtpPacket.PCM_ENCODING, microphoneSampleRate, 
 		microphoneChannels);
 
         int microphonePackets = 
 	    microphoneBufferSizeMillis / RtpPacket.PACKET_PERIOD;
 
         int microphoneBufferSize = microphonePackets * microphoneBytesPerPacket;
 
 	int speakerBytesPerPacket =
 	    RtpPacket.getDataSize(RtpPacket.PCM_ENCODING, speakerSampleRate, 
 	    speakerChannels);
 
         int speakerPackets = speakerBufferSizeMillis / RtpPacket.PACKET_PERIOD;
 
         int speakerBufferSize = speakerPackets * speakerBytesPerPacket;
 
         AudioFactory audioFactory = AudioFactory.getInstance();
 
 	try {
 	    audioFactory.initialize(speakerSampleRate, speakerChannels, 
 	        microphoneSampleRate, microphoneChannels, microphoneBufferSize,
 	        speakerBufferSize);
 	} catch (IOException e) {
 	    Console.showErrorUI("Softphone Error!",
 		   "The softphone couldn't initialize the audio system.\n"
 		   + "The softphone will not work!",
 		   e.getMessage());
 	    throw e;
 	}
 
         microphone = audioFactory.getMicrophone();
         speaker = audioFactory.getSpeaker();
     }
 
     private void shutdownAudio() {
 	if (microphone != null) {
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("closing microphone");
 	    }
 	    microphone.removeListener(this);
 	    microphone.done();
 	    microphone = null;
 	}
 
 	if (speaker != null) {
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("closing speaker");
 	    }
 	    speaker.done();
 	    speaker = null;
 	}
 
         AudioFactory audioFactory = AudioFactory.getInstance();
 
         audioFactory.shutdown();
     }
 	
     private boolean sameParameters(int speakerSampleRate, int speakerChannels,
 	    int microphoneSampleRate, int microphoneChannels) {
 
         return microphone != null && 
 	    microphone.getSampleRate() == microphoneSampleRate &&
             microphone.getChannels() == microphoneChannels &&
             speaker != null && speaker.getSampleRate() == speakerSampleRate &&
             speaker.getChannels() == speakerChannels;
     }
 
     public Microphone getMicrophone() throws IOException {
         if (microphone != null) {
             return microphone;
         }
 
         return getMicrophone((int) DEFAULT_SAMPLE_RATE, DEFAULT_CHANNELS);
     }
 
     public Microphone getMicrophone(int microphoneSampleRate, 
 	    int microphoneChannels) throws IOException {
 
 	if (speakerSampleRate == 0) {
 	    speakerSampleRate = microphoneSampleRate;
 	}
 
 	if (speakerChannels == 0) {
 	    speakerChannels = microphoneChannels;
 	}
 
         setupAudio(speakerSampleRate, speakerChannels, 
 	    microphoneSampleRate, microphoneChannels);
 
         return microphone;
     }
 
     public Speaker getSpeaker() throws IOException {
         if (speaker != null) {
             return speaker;
         }
 
         return getSpeaker((int) DEFAULT_SAMPLE_RATE, DEFAULT_CHANNELS);
     }
 
     public Speaker getSpeaker(int speakerSampleRate, int speakerChannels)
             throws IOException {
 
 	if (microphoneSampleRate == 0) {
 	    microphoneSampleRate = speakerSampleRate;
 	}
 
 	if (microphoneChannels == 0) {
 	    microphoneChannels = speakerChannels;
 	}
 
         setupAudio(speakerSampleRate, speakerChannels, 
 	    microphoneSampleRate, microphoneChannels);
 
         return speaker;
     }
 
     public void setMicrophoneVolume(double volume) {
 	try {
 	    getMicrophone().setVolumeLevel(volume);
 	} catch (IOException e) {
 	    Logger.println("Can't set Microphone volume " + e.getMessage());
 	}
     }
 
     public double getMicrophoneVolume() {
 	try {
 	   return getMicrophone().getVolumeLevel();
 	} catch (IOException e) {
 	    Logger.println("Can't get Microphone volume " + e.getMessage());
 	    return 0;
 	}
     }
 
     public void setSpeakerVolume(double volume) {
 	try {
             getSpeaker().setVolumeLevel(volume);
 	} catch (IOException e) {
 	    Logger.println("Can't set Speaker volume " + e.getMessage());
 	}
 	
     }
 
     public double getSpeakerVolume() {
 	try {
             return getSpeaker().getVolumeLevel();
 	} catch (IOException e) {
 	    Logger.println("Can't get Speaker volume " + e.getMessage());
 	    return 0;
 	}
     }
 
     public synchronized void restart() throws IOException {
 	stop();
 	initSdpManager();
 	initialize();
     }
 
     public void startVuMeter(boolean startVuMeter) {
 	if (microphone == null) {
 	    return;
 	}
 
 	if (startVuMeter == true) {
 	    microphone.addListener(this);
 	} else {
 	    microphone.removeListener(this);
 	}
     }
 
     public void microphoneData(byte[] linearData, int offset, int length) {
 	if (microphone == null) {
 	    return;
 	}
 
 	if (isMuted) {
 	    System.out.println("VuMeterData:" + 0.0);
 	} else {
 	    System.out.println("VuMeterData:" + LevelTest.processChunk(linearData, offset, length,
 	        microphone.getSampleSizeInBits() / 8 * microphone.getChannels()));
 	}
     }
 
     public synchronized void start() throws IOException {
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Media manager starting...");
 	}
 
 	if (remoteSdpInfo == null) {
 	    Logger.println("No remote SDP!");
 
 	    throw new IOException("No remote SDP!");
 	}
 
 	stopPlayingFile();
 
 	/*
 	 * Both sides have agreed on sampleRate and channels for
 	 * the speaker and microphone.
 	 *
 	 * There's no need to resample.
 	 */
 	MediaInfo speakerMediaInfo = remoteSdpInfo.getMediaInfo();
 
 	int speakerSampleRate = speakerMediaInfo.getSampleRate();
 	int speakerChannels = speakerMediaInfo.getChannels();
 
 	/*
 	 * This MediaInfo is what's determined from the TRANSMIT preferences.
 	 */
         MediaInfo microphoneMediaInfo = null;
 
 	if (remoteSdpInfo.getTransmitMediaInfoOk() == true) {
             microphoneMediaInfo = sdpManager.getTransmitMediaInfo();
 
 	    if (Logger.logLevel >= Logger.LOG_INFO) {
 	        Logger.println("Remote media says okay to use separate xmit media "
 		    + microphoneMediaInfo);
 	    }
 	} else {
 	    sdpManager.setTransmitMediaInfo((MediaInfo) null);
 	}
 
 	if (microphoneMediaInfo == null || 
 		speakerMediaInfo.getPayload() == RtpPacket.PCMU_PAYLOAD) {
 
 	    microphoneMediaInfo = speakerMediaInfo;
 	} else {
 	    int micEncoding =  microphoneMediaInfo.getEncoding();
 	    int micSampleRate = microphoneMediaInfo.getSampleRate();
 	    int micChannels = microphoneMediaInfo.getChannels();
 
 	    if (micSampleRate > speakerMediaInfo.getSampleRate()) {
 	        micSampleRate = speakerMediaInfo.getSampleRate();
 
 		Logger.println("limiting transmit sample rate to " 
 		    + micSampleRate);
 	    }
 
 	    if (micChannels > speakerMediaInfo.getChannels()) {
 	        micChannels = speakerMediaInfo.getChannels();
 
 		Logger.println("limiting channels to " + micChannels);
 	    }
 
 	    microphoneMediaInfo = MediaInfo.findMediaInfo(
 		micEncoding, micSampleRate, micChannels);
 	}
 
 	int microphoneSampleRate = microphoneMediaInfo.getSampleRate();
 	int microphoneChannels = microphoneMediaInfo.getChannels();
 
 	if (speakerMediaInfo.getPayload() == microphoneMediaInfo.getPayload()) {
 	    Logger.println("Connected:  Transmit and Receive media set to "
 	    + speakerMediaInfo.getEncodingString() + "/" + speakerSampleRate
 	    + "/" + speakerChannels + ", media payload is " 
 	    + speakerMediaInfo.getPayload()); 
 	} else {
             Logger.println("Connected:  Transmit media set to "
             + microphoneMediaInfo.getEncoding()
             + "/" + microphoneSampleRate
             + "/" + microphoneChannels
             + ", media payload is " + microphoneMediaInfo.getPayload()
 	    + ", Receiver media set to "
 	    + speakerMediaInfo.getEncodingString() + "/" + speakerSampleRate
 	    + "/" + speakerChannels + ", media payload is " 
 	    + speakerMediaInfo.getPayload()); 
 	} 
 
 	try {
 	    if (disableAudio == false) {
 		if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		    Logger.println("Starting audio system " 
 			+ speakerSampleRate + "/" + speakerChannels);
 		}
 
 		setupAudio(speakerSampleRate, speakerChannels, 
 		    microphoneSampleRate, microphoneChannels);
 
 	        //Logger.println("Init rec");
 	    }
 
 	    audioReceiver.initialize(speakerMediaInfo, speaker,
 		remoteSdpInfo.getRemoteHost(), remoteSdpInfo.getRemotePort());
 
 	    audioTransmitter.initialize(remoteSdpInfo, microphoneMediaInfo,
 		microphone);
 
 	    started = true;
 	    remoteSdpInfo = null;
 
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Media manager started");
 	    }
 	} catch (LineUnavailableException e) {
             e.printStackTrace();
 	    throw new IOException("Line unavailable " + e.getMessage());
         }
     }
 
     public boolean isStarted() {
         return started;
     }   
     
     public synchronized void stop() throws IOException {
         try {
             console.logEntry();
             
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Stopping, started is " + started);
 	    }
 
             if (started) {
                 started = false;
                 
 		/*
 		 * On Linux, we have to shutdown the audio first
 		 */
 		if (Utils.isLinux()) {
 		    shutdownAudio();
 		}
 
                 audioTransmitter.done();
                 audioReceiver.done();
 		
 		/*
 		 * On non-Linux, we have to shutdown the audio last
 		 */
 		if (Utils.isLinux() == false) {
 		    shutdownAudio();
 		}
 
 		notifyCallDoneListeners();
 	    }
                 
 	    if (rtpSocket != null) {
                 rtpSocket.close();
 
 	        if (!rtpSocket.isClosed()) {
                     if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		        Logger.println("Waiting for Rtp socket to close");
 		    }
 
 		    while (!rtpSocket.isClosed()) {
 		        try {
 		    	    Thread.sleep(100);
 		        } catch (InterruptedException e) {
 		        }
 		    }
 
                     if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 		        Logger.println("Rtp socket closed");
 		    }
 	        }
 
 	        rtpSocket = null;
 	    }
         } finally {
             console.logExit();
         }   
     }   
 
     public int getMicVolumeLevel() {
 	if (audioTransmitter == null) {
 	    return 0;
 	}
 
 	return audioTransmitter.getMicVolumeLevel();
     }
 
     public int getPacketsReceived() {
 	if (audioReceiver == null) {
 	    return 0;
 	}
 
 	return audioReceiver.getPacketsReceived();
     }
 
     public int getNumberMissingPackets() {
 	if (audioReceiver == null) {
 	    return 0;
 	}
 
 	return audioReceiver.getNumberMissingPackets();
     }
 
     public int getPacketsSent() {
 	if (audioTransmitter == null) {
 	    return 0;
 	}
 
 	return audioTransmitter.getPacketsSent();
     }
 
     public int getMicOverflow() {
 	if (audioTransmitter == null) {
 	    return 0;
 	}
 
 	return audioTransmitter.getMicOverflow();
     }
 	
     public int getJitterBufferSize() {
 	if (audioReceiver == null) {
 	    return 0;
 	}
 
 	return audioReceiver.getJitterBufferSize();
     }
 
     public void startDtmf(String dtmf) {
         if(audioTransmitter != null) {
             audioTransmitter.startDtmf(dtmf);
         }
 
         if(audioReceiver != null) {
             audioReceiver.startDtmf(dtmf);
         }
     }
 
     public void stopDtmf() {
         if(audioTransmitter != null) {
             audioTransmitter.stopDtmf();
         }
 
         if(audioReceiver != null) {
             audioReceiver.stopDtmf();
         }
     }
 
     private boolean isMuted;
 
     public void mute(boolean isMuted) {
 	if (audioTransmitter != null) {
 	    this.isMuted = isMuted;
 
 	    audioTransmitter.mute(isMuted);
 	}
     }
 
     public boolean isMuted() {
 	if (audioTransmitter != null) {
 	    return audioTransmitter.isMuted();
 	}
 	return false;
     }
 
     public void startPlayingFile(String file) throws IOException {
         startPlayingFile(file, 0);
     }
 
     public void startPlayingFile(String file, int repeatCount) 
         throws IOException 
     {
 	if (disableAudio == true) {
 	    return;
 	}
 
         if (player != null) {
             player.done();
         }
         
         player = new AudioFilePlayer(file, repeatCount, getSpeaker());
     }
 
     public void stopPlayingAllFiles() {
 	stopPlayingFile();
     }
 
     public void stopPlayingFile() {
         if (player != null) {
 	    if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	        Logger.println("Stop playing file...");
 	    }
             player.done();
             player = null;
         }
     }
     
     public void playTreatment(String audioFile, int repeats) 
         throws IOException 
     {
 	if (audioTransmitter == null) {
 	    throw new IOException("MediaManager not initialized");
 	}
 
 	audioTransmitter.playTreatment(audioFile, repeats);
     }
 
     public void pauseTreatment(boolean pause) {
 	if (audioTransmitter == null) {
 	    return;
 	}
 
 	audioTransmitter.pauseTreatment(pause);
     }
 	
     public void stopTreatment() {
         if (audioTransmitter == null) {
             return;
         }
 
         audioTransmitter.stopTreatment();
     }
 
     public void setRemoteSdpData(String remoteSdpData) {
	if (remoteSdpInfo != null) {
	    return;
	}

 	try {
 	    remoteSdpInfo = sdpManager.parseSdp(remoteSdpData);
 
 	    String conferenceId = remoteSdpInfo.getConferenceId();
 
             if (conferenceId != null) {
 		String c = Utils.getPreference("com.sun.mc.softphone.LAST_CONFERENCE");
 
 		if (c.equals(conferenceId) == false) {
                     Utils.setPreference("com.sun.mc.softphone.LAST_CONFERENCE",
                         conferenceId);
 		}
             }
 	} catch (ParseException e) {
 	    Logger.println("Invalid SDP! " + e.getMessage());
 	    return;
 	}
     }
 
     public synchronized String generateSdp(String callee) throws IOException {
         //InetSocketAddress isa = new InetSocketAddress(
         //    SipCommunicator.getPrivateLocalAddress().getHostAddress(),
         //    rtpSocket.getInetSocketAddress().getPort());
 
 	InetSocketAddress isa = new InetSocketAddress(
 	    rtpSocket.getDatagramSocket().getLocalAddress(),
 	    rtpSocket.getDatagramSocket().getLocalPort());
 
 	String registrarAddress = sipManager.getRegistrarAddress();
 	int registrarPort = sipManager.getRegistrarPort();
 
 	Logger.println("generateSdp:  registrarAddress " + registrarAddress
 	    + " port " + registrarPort + " is stun=" 
 	    + sipManager.isRegistrarStunServer());
 
 	if (registrarAddress != null && sipManager.isRegistrarStunServer()) {
 	    try {
 	        isa = NetworkAddressManager.getPublicAddressFor(
 		    new InetSocketAddress(registrarAddress, registrarPort), 
 		    rtpSocket.getDatagramSocket());
 	    } catch (IOException e) {
 	        Logger.println("generateSdp couldn't get public address "
 		    + e.getMessage());
 	    }
 	}
 
         Logger.println("generateSdp:  Our isa " + isa + " callee " + callee);
 
 	String ourSdp = sdpManager.generateSdp(sipCommunicator.getUserName(), isa);
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("generateSdp:  Our sdp:  " + ourSdp);
 	}
 
 	rtpSocket.startRtcpReceiver();
 	return ourSdp;
     }
 
     public synchronized String generateSdp(boolean answer) throws IOException {
         String s = Utils.getPreference("com.sun.mc.softphone.media.AUDIO_PORT");
 
 	if (s.equals("-1")) {
 	    /*
 	     * Try to use the same port as the remote.
 	     */
 	    lastAudioPort = remoteSdpInfo.getRemotePort();
 
 	    initialize();
 	}
 
 	while (remoteSdpInfo == null) {
 	    /*
 	     * XXX This is a bug.  We should have the remote SDP.
 	     * Try sleeping in case this is a timing problem.
 	     */
 	    Logger.println("Remote sdp is null, sleeping...");
 
 	    try {
 		Thread.sleep(1000);
 	    } catch (InterruptedException e) {
 	    }
 
 	    if (remoteSdpInfo == null) {
 	        Logger.println("Remote sdp is still null...");
 	    }
 	}
 
 	InetSocketAddress isa = new InetSocketAddress(
 	    rtpSocket.getDatagramSocket().getLocalAddress(),
 	    rtpSocket.getDatagramSocket().getLocalPort());
 
 	try {
 	    isa = NetworkAddressManager.getPublicAddressFor(
 	        new InetSocketAddress(remoteSdpInfo.getRemoteHost(), 
 		remoteSdpInfo.getRemotePort()), 
 	        rtpSocket.getDatagramSocket());
 	} catch (IOException e) {
 	    Logger.println("generateSdp couldn't get public address "
 		+ e.getMessage());
 
 	    if (sipManager.isRegistrarStunServer()) {
 		Console.showErrorUI("Softphone Error!",
 		   "The softphone couldn't get the public address for "
 		   + rtpSocket.getDatagramSocket().getLocalAddress() + ":"
 		   + rtpSocket.getDatagramSocket().getLocalPort() + "\n"
 		   + "The softphone will not work!",
 		   "No response from STUN server "
 		   + remoteSdpInfo.getRemoteHost() + ":"
 		   + remoteSdpInfo.getRemotePort() + "\n\n"
 		   +  "This could be a network problem such as being unable\n"
                    + "to send and receive UDP packets through a firewall\n");
 	    }
 	}
 
 	Logger.println("generateSdp:  Our isa " + isa);
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("remoteSdp " + remoteSdpInfo);
 	}
 
 	String ourSdp = sdpManager.generateSdp(sipCommunicator.getUserName(), isa, 
 	    remoteSdpInfo);
 
 	isa = new InetSocketAddress(remoteSdpInfo.getRemoteHost(), 
 	    remoteSdpInfo.getRemotePort() + 1); 
 
 	String rtcpAddress = getRtcpAddress(isa);
 
 	if (rtcpAddress != null) {
 	    ourSdp += "a=rtcpAddress:" + rtcpAddress + "\r\n";
 	}
 
 	rtpSocket.startRtcpReceiver();
 
 	if (Logger.logLevel >= Logger.LOG_MOREINFO) {
 	    Logger.println("Our sdp:  " + ourSdp);
 	}
 
 	return ourSdp;
     }
 
     private String getRtcpAddress(InetSocketAddress isa) {
 	try {
 	    InetSocketAddress rtcpAddress = NetworkAddressManager.getPublicAddressFor(
 		isa, rtpSocket.getRtcpDatagramSocket());
 
 	    Logger.println("rtcp address is " + rtcpAddress);
 
 	    return rtcpAddress.getAddress().getHostAddress() + ":" +
 		rtcpAddress.getPort();
 	} catch (IOException e) {
 	    Logger.println("couldn't get rtcp public address "
 		+ e.getMessage());
 	}
 
         return null;
     }
 
     private Vector callDoneListeners = new Vector();
 
     public void addCallDoneListener(CallDoneListener listener) {
 	callDoneListeners.add(listener);
     }
    
     public void removeCallDoneListener(CallDoneListener listener) {
 	callDoneListeners.remove(listener);
     }
 
     private void notifyCallDoneListeners() {
 	for (int i = 0; i < callDoneListeners.size(); i++) {
 	    CallDoneListener listener = (CallDoneListener)
 		callDoneListeners.get(i);
 
 	    listener.callDone();
 	}
     }
 
     private LocalPlayer localPlayer;
 
     public void playRecording(String path, boolean isLocal,
 	    PacketGeneratorListener listener) throws IOException {
 
 	if (isLocal) {
 	    if (isStarted()) {
 		Logger.println(
 		    "Can't play a file while a call is in progress!");
 
 		throw new IOException (
 		    "Can't play local file while call is in progress!");
 	    }
 
 	    localPlayer = new LocalPlayer(path, listener);
 	    return;
 	}
 
 	audioTransmitter.playFile(path, listener);
     }
 
     public void stopPlaying(boolean isLocal) {
 	if (isLocal && localPlayer != null) {
 	    localPlayer.done();
 	    localPlayer = null;
 	    return;
 	}
 
 	audioTransmitter.stopPlaying();
     }
 
     public void startRecording(String path, String recordingType, 
 	    boolean recordingMic, CallDoneListener listener) throws IOException {
 
 	if (listener != null) {
 	    addCallDoneListener(listener);
 	}
 
 	if (recordingMic) {
 	    audioTransmitter.startRecording(path, recordingType);
 	} else {
 	    audioReceiver.startRecording(path, recordingType);
 	}
     }
 
     public void pauseRecording(boolean isMicrophone) {
 	if (isMicrophone) {
 	    audioTransmitter.pauseRecording();
 	} else {
 	    audioReceiver.pauseRecording();
 	}
     }
 
     public void resumeRecording(boolean isMicrophone) {
 	if (isMicrophone) {
 	    audioTransmitter.resumeRecording();
 	} else {
 	    audioReceiver.resumeRecording();
 	}
     }
 
     public void stopRecording(boolean isMicrophone) {
 	if (isMicrophone) {
 	    audioTransmitter.stopRecording();
 	} else {
 	    audioReceiver.stopRecording();
 	}
     }
 
     class LocalPlayer extends Thread {
 
 	//private AudioReceiver audioReceiver;
 	private RtpSenderPacket rtpSenderPacket;
 	private PacketGenerator packetGenerator;
 	private MediaInfo mediaInfo;
 	private RtpSocket rtpSocket;
 	private InetSocketAddress isa;
 
 	private boolean done;
 
 	public LocalPlayer(String path, PacketGeneratorListener listener) 
 		throws IOException {
 
 	    rtpSocket = null;
 
             try {
                 String s = Utils.getPreference(
 		    "com.sun.mc.softphone.media.AUDIO_PORT");
 
                 int audioPort = 0;
 
                 if (s != null && s.length() > 0) {
                     audioPort = Integer.parseInt(s);
 
 		    if (audioPort == -1) {
 			audioPort = 0;
 		    } 
                 }
 
                 if (Logger.logLevel >= Logger.LOG_MOREINFO) {
                     Logger.println("audioPort is " + audioPort);
                 }
 
                 rtpSocket = new RtpSocket(
 		    SipCommunicator.getPrivateLocalAddress(), audioPort);
             } catch (IOException e) {
 	        Logger.println("Can't createRtpSocket!  " + e.getMessage());
 	        throw new IOException("Can't createRtpSocket!  " 
 		    + e.getMessage());
 	    }
 
 	    packetGenerator = new PacketGenerator(path);
 	    packetGenerator.addListener(listener);
 
 	    setName("Local Player");
 	    setPriority(Thread.MAX_PRIORITY);
 	    start();
         }
 
 	private void initialize() throws IOException {
 	    mediaInfo = packetGenerator.getMediaInfo();
 
 	    Speaker speaker = null;
 
 	    try {
 	        speaker = getSpeaker(mediaInfo.getSampleRate(), 
 		    mediaInfo.getChannels());
             } catch (IOException e) {
 	        Logger.println("Can't get speaker!");
 	        throw new IOException("Can't get speaker! " + e.getMessage());
 	    }
 
 	    audioReceiver = new AudioReceiver(rtpSocket, null, null);
 
 	    audioReceiver.setOldSynchronizationSource(0);
 
 	    isa = rtpSocket.getInetSocketAddress();
 
     	    audioReceiver.initialize(mediaInfo, speaker, isa.getHostName(), 
 		isa.getPort());
 
 	    Logger.println("host " + isa.getHostName() + " port " 
 	        + isa.getPort());
 	}
 
 	public void run() {
 	    int n = 0;
 
 	    //try {
 	    //	Thread.sleep(1000);  // let receiver initialize
 	    //} catch (InterruptedException e) {
 	    //}
 
 	    while (!done) {
 	        try {
 	   	    rtpSenderPacket = packetGenerator.getPacket();
 
 		    if (n == 0) {
 	    		rtpSenderPacket.setMark();
 			initialize();
 		    } 
 
 	    	    rtpSenderPacket.setSocketAddress(isa);
                 } catch (IOException e) {
 		    Logger.println("Local player:  " + e.getMessage());
 		    break;
 	        }
 
 	        try {
 		    rtpSocket.send(rtpSenderPacket);
 	        } catch (IOException e) {
 		    if (!done) {
 		        Logger.println("Can't send! " + e.getMessage());
 		    }
 		    break;
 	        }
 
 	        n++;
 	    }
 
 	    done();
         }
 
 	public void done() {
 	    if (done) {
 		return;
 	    }
 
 	    done = true;
 
 	    if (audioReceiver != null) {
 	        audioReceiver.done();
 	    }
 
 	    if (rtpSocket != null) {
 	        rtpSocket.close();
 	    }
 
 	    try {
 	        audioReceiver = new AudioReceiver(rtpSocket, null, null);
 	    } catch (IOException e) {
 		Logger.println("Failed to create new AudioReceiver:  "
 		    + e.getMessage());
 	    }
 	}
 
     }
 
 }
