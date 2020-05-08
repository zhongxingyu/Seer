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
  * Large portions of this software are based upon public domain software
  * https://sip-communicator.dev.java.net/
  *
  */
 package net.sourceforge.gjtapi.raw.sipprovider.media;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.media.CaptureDeviceInfo;
 import javax.media.DataSink;
 import javax.media.Format;
 import javax.media.IncompatibleSourceException;
 import javax.media.Manager;
 import javax.media.MediaLocator;
 import javax.media.NoDataSourceException;
 import javax.media.NoProcessorException;
 import javax.media.Player;
 import javax.media.Processor;
 import javax.media.control.TrackControl;
 import javax.media.format.AudioFormat;
 import javax.media.format.VideoFormat;
 import javax.media.protocol.ContentDescriptor;
 import javax.media.protocol.DataSource;
 import javax.media.rtp.RTPManager;
 import javax.media.rtp.SessionAddress;
 import javax.sdp.Connection;
 import javax.sdp.Media;
 import javax.sdp.MediaDescription;
 import javax.sdp.Origin;
 import javax.sdp.SdpConstants;
 import javax.sdp.SdpException;
 import javax.sdp.SdpFactory;
 import javax.sdp.SdpParseException;
 import javax.sdp.SessionDescription;
 import javax.sdp.SessionName;
 import javax.sdp.TimeDescription;
 import javax.sdp.Version;
 
 import net.sourceforge.gjtapi.raw.sipprovider.common.Console;
 import net.sourceforge.gjtapi.raw.sipprovider.common.NetworkAddressManager;
 import net.sourceforge.gjtapi.raw.sipprovider.common.Utils;
 import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaErrorEvent;
 import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaEvent;
 import net.sourceforge.gjtapi.raw.sipprovider.media.event.MediaListener;
 
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
 public class MediaManager implements Serializable {
     static final long serialVersionUID = 0L; // never serialized
 
     protected static Console console = Console.getConsole(MediaManager.class);
     protected ArrayList listeners = new ArrayList();
     protected Vector avTransmitters = new Vector();
     protected AVReceiver avReceiver;
     protected SdpFactory sdpFactory;
     protected ProcessorUtility procUtility = new ProcessorUtility("MediaManager");
     //media devices
     protected CaptureDeviceInfo audioDevice = null;
     protected CaptureDeviceInfo videoDevice = null;
     //Sdp Codes of all formats supported for
     //transmission by the selected datasource
 
     protected ArrayList transmittableAudioFormats = new ArrayList();
     //Sdp Codes of all formats that we can receive
     //i.e.  all formats supported by JMF
     protected String[] receivableVideoFormats = new String[] {
                                                 //sdp format 							   		// corresponding JMF Format
                                                 Integer.toString(SdpConstants.
             H263), // javax.media.format.VideoFormat.H263_RTP
                                                 Integer.toString(SdpConstants.
             JPEG), // javax.media.format.VideoFormat.JPEG_RTP
                                                 Integer.toString(SdpConstants.
             H261) // javax.media.format.VideoFormat.H261_RTP
     };
     protected String[] receivableAudioFormats = new String[] {
                                                 //sdp format
                                                 Integer.toString(SdpConstants.
             GSM), // javax.media.format.AudioFormat.GSM_RTP;// corresponding JMF Format
                                                 Integer.toString(SdpConstants.
             G723), // javax.media.format.AudioFormat.G723_RTP
                                                 Integer.toString(SdpConstants.
             PCMU), // javax.media.format.AudioFormat.ULAW_RTP;
                                                 Integer.toString(SdpConstants.
             DVI4_8000), // javax.media.format.AudioFormat.DVI_RTP;
                                                 Integer.toString(SdpConstants.
             DVI4_16000), // javax.media.format.AudioFormat.DVI_RTP;
                                                 Integer.toString(SdpConstants.
             PCMA), // javax.media.format.AudioFormat.ALAW;
                                                 Integer.toString(SdpConstants.
             G728), //, // javax.media.format.AudioFormat.G728_RTP;
                                                 //g729 is not suppported by JMF
                                                 Integer.toString(SdpConstants.
             G729) // javax.media.format.AudioFormat.G729_RTP
     };
 
     /**
      * A list of currently active RTPManagers mapped against Local session addresses.
      * The list is used by transmitters and receivers so that receiving and transmitting
      * from the same port simultaneousl is possible
      */
     protected Map activeRtpManagers = new Hashtable();
     protected Map sessions = new Hashtable();
     protected RTPManager rtpManager;
 
     protected String mediaSource = null;
     protected DataSource avDataSource = null;
     protected Processor processor = null;
     protected boolean isStarted = false;
     protected Properties sipProp;
     protected String audioPort;
     protected Vector transmitters = new Vector();
     protected Vector receivers = new Vector();
     ;
     protected DataSink sink = null;
     private MediaLocator dest;
 
     public MediaManager(Properties sipProp) {
         audioPort = sipProp.getProperty(
                 "net.java.sip.communicator.media.AUDIO_PORT");
         this.sipProp = new Properties();
         this.sipProp.putAll(sipProp);
     }
 
 
     /**
      * Reads the audio from the given url and publishes it to all transmitters.
      * @param url the url pointing to an audio data source
      * @throws MediaException
      *         Error playing the audio.
      */
     public void play(String url) throws MediaException {
 
         console.logEntry();
 
         MediaLocator locator = new MediaLocator(url);
         avDataSource = createDataSource(locator);
         if (avDataSource != null) {
             initProcessor(avDataSource);
         }
 
         for (int i = avTransmitters.size() - 1; i >= 0; i--) {
             try {
                 ((AVTransmitter) avTransmitters.elementAt(i)).play(processor);
             } catch (net.sourceforge.gjtapi.raw.sipprovider.media.
                      MediaException ex) {
                 console.debug(ex.toString());
             }
             console.logExit();
 
         }
     }
 
     public void stopPlaying() {
 
         console.logEntry();
         for (int i = avTransmitters.size() - 1; i >= 0; i--)
 
         {
             try {
                 ((AVTransmitter) avTransmitters.elementAt(i)).stopPlaying();
             } catch (Exception ex) {
                 console.debug(ex.toString());
             }
 
         }
     }
 
     public void record(String url) {
 
         console.logEntry();
         try {
 
             DataSource mergeDs = this.getDataSource();
             // append "file:/" to URL if it is not already there
             String fullUrl = (url.indexOf("file:") == 0) ? url : "file:/" + url;
             dest = new MediaLocator(fullUrl);
             sink = Manager.createDataSink(mergeDs, dest);
             sink.open();
             sink.start();
 
             for (int i = receivers.size() - 1; i >= 0; i--) {
                 try {
                     Processor pro = ((AVReceiver) receivers.elementAt(i)).
                                     getProcessor();
 
                     pro.start();
                 } catch (Exception ex) {
                     console.debug(ex.toString());
                 }
 
             }
 
         } catch (Exception ex) {
             console.debug(ex.toString());
         }
         console.logExit();
     }
 
     /**
      * Gets the datasource for a Sip session.
      * @return
      */
     public DataSource getDataSource() throws IncompatibleSourceException,
             IOException {
         DataSource dsTab[] = new DataSource[receivers.size()];
         DataSource ds = null;
         for (int i = receivers.size() - 1; i >= 0; i--) {
             Processor pro = ((AVReceiver) receivers.elementAt(i)).getProcessor();
 
             ds = pro.getDataOutput();
             dsTab[i] = ds;
         }
 
         DataSource mergeDs = Manager.createMergingDataSource(dsTab);
         mergeDs.connect();
         mergeDs.start();
         return mergeDs;
     }
 
     public void stopRecording() {
         try {
             sink.stop();
             sink.close();
         } catch (Exception ex) {
             console.debug(ex.toString());
         }
     }
 
     public void start() throws MediaException {
         try {
             console.logEntry();
             sdpFactory = SdpFactory.getInstance();
 
             mediaSource = sipProp.getProperty(
                     "net.java.sip.communicator.media.MEDIA_SOURCE");
             //Init Capture devices
             //DataSource audioDataSource = null;
 
             isStarted = true;
        } catch (SdpException sdpEx) {
        	throw new MediaException(sdpEx);
         } finally {
             console.logExit();
         }
     }
 
     /**
      * Creates the {@link DataSource} for the given {@link MediaLocator}.
      * @param locator the medi loactor
      * @return created data source.
      */
     protected DataSource createDataSource(MediaLocator locator) {
         try {
             console.logEntry();
             try {
                 if (console.isDebugEnabled()) {
                     console.debug("Creating datasource for:"
                                   + locator != null
                                   ? locator.toExternalForm()
                                   : "null");
                 }
                 return Manager.createDataSource(locator);
             } catch (NoDataSourceException ex) {
                 //The failure only concerns us
                 if (console.isDebugEnabled()) {
                     console.debug("Coud not create data source for " +
                                   locator.toExternalForm(), ex);
                 }
                 return null;
             } catch (IOException ex) {
                 //The failure only concens us
                 if (console.isDebugEnabled()) {
                     console.debug("Coud not create data source for " +
                                   locator.toExternalForm(), ex);
                 }
                 return null;
             }
         } finally {
             console.logExit();
         }
     }
 
     public void openMediaStreams(String sdpData) throws MediaException {
         try {
             console.logEntry();
 
             if (console.isDebugEnabled()) {
                 console.debug("sdpData arg - " + sdpData);
             }
             checkIfStarted();
             SessionDescription sessionDescription = null;
             if (sdpData == null) {
                 console.error("The SDP data was null! Cannot open " +
                               "a stream withour an SDP Description!");
                 throw new MediaException(
                         "The SDP data was null! Cannot open " +
                         "a stream withour an SDP Description!");
             }
             try {
                 sessionDescription = sdpFactory.createSessionDescription(
                         sdpData);
             } catch (SdpParseException ex) {
                 console.error("Incorrect SDP data!", ex);
                 throw new MediaException("Incorrect SDP data!", ex);
             }
             Vector mediaDescriptions;
             try {
                 mediaDescriptions = sessionDescription.
                                     getMediaDescriptions(true);
             } catch (SdpException ex) {
                 console.error(
                         "Failed to extract media descriptions from provided session description!",
                         ex);
                 throw new MediaException(
                         "Failed to extract media descriptions from provided session description!",
                         ex);
             }
             Connection connection = sessionDescription.getConnection();
             if (connection == null) {
                 console.error(
                         "A connection parameter was not present in provided session description");
                 throw new MediaException(
                         "A connection parameter was not present in provided session description");
             }
             String remoteAddress = null;
             try {
                 remoteAddress = connection.getAddress();
             } catch (SdpParseException ex) {
                 console.error(
                         "Failed to extract the connection address parameter"
                         + "from privided session description", ex);
                 throw new MediaException(
                         "Failed to extract the connection address parameter"
                         + "from privided session description", ex);
             }
             int mediaPort = -1;
             boolean atLeastOneTransmitterStarted = false;
             ArrayList ports = new ArrayList();
             ArrayList formatSets = new ArrayList();
             for (int i = 0; i < mediaDescriptions.size(); i++) {
                 Media media = ((MediaDescription) mediaDescriptions.get(i)).
                               getMedia();
                 //Media Type
                 String mediaType = null;
                 try {
                     mediaType = media.getMediaType();
                 } catch (SdpParseException ex) {
                     console.error(
                             "Failed to extract the media type for one of the provided media descriptions!\n"
                             + "Ignoring description!",
                             ex);
                     fireNonFatalMediaError(new MediaException(
                             "Failed to extract the media type for one of the provided media descriptions!\n"
                             + "Ignoring description!",
                             ex
                                            ));
                     continue;
                 }
                 //Find ports
                 try {
                     mediaPort = media.getMediaPort();
                 } catch (SdpParseException ex) {
                     console.error("Failed to extract port for media type ["
                                   + mediaType + "]. Ignoring description!",
                                   ex);
                     fireNonFatalMediaError(new MediaException(
                             "Failed to extract port for media type ["
                             + mediaType + "]. Ignoring description!",
                             ex
                                            ));
                     continue;
                 }
                 //Find  formats
                 Vector sdpFormats = null;
                 try {
                     sdpFormats = media.getMediaFormats(true);
                 } catch (SdpParseException ex) {
                     console.error(
                             "Failed to extract media formats for media type ["
                             + mediaType + "]. Ignoring description!",
                             ex);
                     fireNonFatalMediaError(new MediaException(
                             "Failed to extract media formats for media type ["
                             + mediaType + "]. Ignoring description!",
                             ex
                                            ));
                     continue;
                 }
                 //START TRANSMISSION
                 try {
                     if (isMediaTransmittable(mediaType)) {
                         ports.add(new Integer(mediaPort));
                         formatSets.add(extractTransmittableJmfFormats(
                                 sdpFormats));
                     } else {
                         //nothing to transmit here so skip setting the flag
                         //bug report and fix - Gary M. Levin - Telecordia
                         continue;
                     }
                 } catch (MediaException ex) {
                     console.error(
                             "Could not start a transmitter for media type ["
                             + mediaType + "]\nIgnoring media [" + mediaType +
                             "]!",
                             ex
                             );
                     fireNonFatalMediaError(new MediaException(
                             "Could not start a transmitter for media type ["
                             + mediaType + "]\nIgnoring media [" + mediaType +
                             "]!",
                             ex
                                            ));
                     continue;
                 }
                 atLeastOneTransmitterStarted = true;
             }
             //startReceiver(remoteAddress);
             //open corrects ports for RTP Session
             startReceiver(remoteAddress, ports);
             if (!atLeastOneTransmitterStarted) {
                 console.error(
                         "Apparently all media descriptions failed to initialise!\n" +
                         "SIP COMMUNICATOR won't be able to open a media stream!");
                 throw new MediaException(
                         "Apparently all media descriptions failed to initialise!\n" +
                         "SIP COMMUNICATOR won't be able to open a media stream!");
             } else {
 
                 startTransmitter(remoteAddress, ports, formatSets);
             }
         } finally {
             console.logExit();
         }
     }
 
     protected void closeProcessor() {
         try {
             console.logEntry();
             if (processor != null) {
                 processor.stop();
                 processor.close();
             }
             if (avDataSource != null) {
                 avDataSource.disconnect();
             }
         } finally {
             console.logExit();
         }
     }
 
     public void stop() throws MediaException {
         try {
             console.logEntry();
             //   closeStreams();
             closeProcessor();
 
         } finally {
             console.logExit();
         }
     }
 
 
     public void closeStreams(String sdpData) throws MediaException {
         SessionDescription sessionDescription = null;
         int mediaPort = -1; //remote port
         String remoteAddress = null; //remote address
 
         try {
             sessionDescription = sdpFactory.createSessionDescription(sdpData);
         } catch (SdpParseException ex) {
             console.error("Incorrect SDP data!", ex);
         }
         Vector mediaDescriptions = null;
         try {
             mediaDescriptions = sessionDescription.getMediaDescriptions(true);
         } catch (SdpException ex) {
             console.error(
                     "Failed to extract media descriptions from provided session description!",
                     ex);
 
         }
         Connection connection = sessionDescription.getConnection();
         if (connection == null) {
             console.error(
                     "A connection parameter was not present in provided session description");
             throw new MediaException(
                     "A connection parameter was not present in provided session description");
         }
         try {
             remoteAddress = connection.getAddress();
         } catch (SdpParseException ex) {
             console.error(
                     "Failed to extract the connection address parameter"
                     + "from privided session description", ex);
             throw new MediaException(
                     "Failed to extract the connection address parameter"
                     + "from privided session description", ex);
         }
 
         //boolean atLeastOneTransmitterStarted = false;
         //ArrayList ports = new ArrayList();
         //ArrayList formatSets = new ArrayList();
         for (int i = 0; i < mediaDescriptions.size(); i++) {
             Media media = ((MediaDescription) mediaDescriptions.get(i)).
                           getMedia();
             //Media Type
             String mediaType = null;
             try {
                 mediaType = media.getMediaType();
             } catch (SdpParseException ex) {
                 console.error(
                         "Failed to extract the media type for one of the provided media descriptions!\n"
                         + "Ignoring description!",
                         ex);
                 fireNonFatalMediaError(new MediaException(
                         "Failed to extract the media type for one of the provided media descriptions!\n"
                         + "Ignoring description!",
                         ex
                                        ));
                 continue;
             }
             //Find ports
             try {
                 mediaPort = media.getMediaPort();
             } catch (SdpParseException ex) {
                 console.error("Failed to extract port for media type ["
                               + mediaType + "]. Ignoring description!",
                               ex);
                 fireNonFatalMediaError(new MediaException(
                         "Failed to extract port for media type ["
                         + mediaType + "]. Ignoring description!",
                         ex
                                        ));
                 continue;
             }
 
         }
 
         //======================
         try {
             //removeAllRtpManagers();
             console.logEntry();
             SessionAddress addToStop = new SessionAddress(InetAddress.getByName(
                     remoteAddress), mediaPort);
             stopTransmitters(addToStop);
             if (avTransmitters.size() == 0) {
                 stopReceiver("localhost");
             }
             firePlayerStopped();
         }
 
         catch (java.net.UnknownHostException ex) {
             console.debug(ex.toString());
         }
 
         finally {
             console.logExit();
         }
     }
 
 
     protected void startTransmitter(String destHost, ArrayList ports,
                                     ArrayList formatSets) throws MediaException {
         try {
             console.logEntry();
 
             AVTransmitter transmitter = new AVTransmitter(processor, destHost,
                     ports, formatSets);
             transmitter.setMediaManagerCallback(this);
             avTransmitters.add(transmitter);
             console.debug("Starting transmission.");
             transmitter.start();
             transmitters.add(transmitter);
 
         } finally {
             console.logExit();
         }
     }
 
     protected void stopTransmitters(SessionAddress addToStop) {
         try {
             console.logEntry();
             for (int i = avTransmitters.size() - 1; i >= 0; i--) {
                 try {
                     ((AVTransmitter) avTransmitters.elementAt(i)).stop(
                             addToStop);
                 } //Catch everything that comes out as we wouldn't want
                 //Some null pointer prevent us from closing a device and thus
                 //render it unusable
                 catch (Exception exc) {
                     console.error("Could not close transmitter " + i, exc);
                 }
                 avTransmitters.removeElementAt(i);
                 transmitters.removeElementAt(i);
             }
         } finally {
             console.logExit();
         }
     }
 
     protected void startReceiver(String remoteAddress, ArrayList ports) {
         try {
             console.logEntry();
             avReceiver = new AVReceiver(new String[] {
                                         remoteAddress + "/" + getAudioPort() +
                                         "/1"}, sipProp);
             avReceiver.setMediaManager(this);
             //avReceiver.initialize();
             try {
                 avReceiver.initialize2(ports);
             } catch (MediaException ex) {
                 ex.printStackTrace();
             }
 
             receivers.add(avReceiver);
         } finally {
             console.logExit();
         }
     }
 
     protected void stopReceiver(String LocalAddress) {
         /*try
                  {
             console.logEntry();
             if (avReceiver != null)
             {
                 //on ferme la reception dans lengthcas ou il n'u a plus de transm
                 if(avTransmitters.size()==0)
                     avReceiver.close(LocalAddress);
                 //avReceiver = null;
                 //avReceiver.
             }
                  }
                  finally
                  {
             console.logExit();
                  }*/
 
         try {
             console.logEntry();
             for (int i = receivers.size() - 1; i >= 0; i--) {
                 try {
                     ((AVReceiver) receivers.elementAt(i)).close(LocalAddress);
                 } //Catch everything that comes out as we wouldn't want
                 //Some null pointer prevent us from closing a device and thus
                 //render it unusable
                 catch (Exception exc) {
                     console.error("Could not close receiver " + i, exc);
                 }
 
                 receivers.removeElementAt(i);
             }
         } finally {
             console.logExit();
         }
     }
 
     protected void stopReceiver() {
         try {
             console.logEntry();
             if (avReceiver != null) {
                 avReceiver.close();
                 avReceiver = null;
 
             }
         } finally {
             console.logExit();
         }
     }
 
     /**
      * Only stops the receiver without deleting it. After calling this method
      * one can call softStartReceiver to relauch reception.
      */
     public void softStopReceiver() {
         try {
             console.logEntry();
             if (avReceiver != null) {
                 avReceiver.close();
                 this.firePlayerStopped();
             } else {
                 console.debug(
                         "Attempt to soft stop reception for a null avReceiver");
             }
         } finally {
             console.logExit();
         }
     }
 
 
     /**
      * Starts a receiver that has been stopped using softStopReceiver().
      */
     public void softStartReceiver() {
         try {
             console.logEntry();
             if (avReceiver != null) {
                 avReceiver.initialize();
             } else {
                 console.error(
                         "acReceiver is null. Use softStartReceiver only for receivers "
                         + "that had been stopped using softStopReceiver()");
             }
         } finally {
             console.logExit();
         }
     }
 
     void firePlayerStarting(Player player) {
         try {
             console.logEntry();
             MediaEvent evt = new MediaEvent(player);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ((MediaListener) listeners.get(i)).playerStarting(evt);
             }
         } finally {
             console.logExit();
         }
     }
 
     void firePlayerStopped() {
         try {
             console.logEntry();
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ((MediaListener) listeners.get(i)).playerStopped();
             }
         } finally {
             console.logExit();
         }
     }
 
     void fireNonFatalMediaError(Throwable cause) {
         try {
             console.logEntry();
             MediaErrorEvent evt = new MediaErrorEvent(cause);
             for (int i = listeners.size() - 1; i >= 0; i--) {
                 ((MediaListener) listeners.get(i)).nonFatalMediaErrorOccurred(
                         evt);
             }
         } finally {
             console.logExit();
         }
     }
 
     public void addMediaListener(MediaListener listener) {
         try {
             console.logEntry();
             listeners.add(listener);
         } finally {
             console.logExit();
         }
     }
 
     InetAddress getLocalHost() throws MediaException {
         try {
             console.logEntry();
             String hostAddress = sipProp.getProperty(
                     "net.java.sip.communicator.media.IP_ADDRESS");
             InetAddress lh;
             lh = InetAddress.getByName(hostAddress);
             console.debug(hostAddress);
             return lh;
         } catch (Exception ex) {
             ex.toString();
             return null;
         }
     }
 
     public String generateSdpDescription() throws MediaException {
         try {
             console.logEntry();
             checkIfStarted();
             try {
                 SessionDescription sessDescr = sdpFactory.
                                                createSessionDescription();
                 //"v=0"
                 Version v = sdpFactory.createVersion(0);
 
                 InetSocketAddress publicAudioAddress = NetworkAddressManager.
                         getPublicAddressFor(Integer.parseInt(getAudioPort()));
                 InetAddress publicIpAddress = publicAudioAddress.getAddress();
                 String addrType = publicIpAddress instanceof Inet6Address ?
                                   "IP6" : "IP4";
 
                 //spaces in the user name mess everything up.
                 //bug report - Alessandro Melzi
                 Origin o = sdpFactory.createOrigin(
                         Utils.getProperty("user.name").replace(' ', '_'), 0, 0,
                         "IN", addrType, publicIpAddress.getHostAddress());
                 //"s=-"
                 SessionName s = sdpFactory.createSessionName("-");
                 //c=
                 Connection c = sdpFactory.createConnection("IN", addrType,
                         publicIpAddress.getHostAddress());
                 //"t=0 0"
                 TimeDescription t = sdpFactory.createTimeDescription();
                 Vector timeDescs = new Vector();
                 timeDescs.add(t);
                 //--------Audio media description
                 //make sure preferred formats come first
                 surfacePreferredEncodings(getReceivableAudioFormats());
                 String[] formats = getReceivableAudioFormats();
                 MediaDescription am = sdpFactory.createMediaDescription("audio",
                         publicAudioAddress.getPort(), 1, "RTP/AVP", formats);
                 if (!isAudioTransmissionSupported()) {
                     am.setAttribute("recvonly", null);
                     //--------Video media description
                 }
                 surfacePreferredEncodings(getReceivableVideoFormats());
                 //"m=video 22222 RTP/AVP 34";
                 //String[] vformats = getReceivableVideoFormats();
 
 
                 Vector mediaDescs = new Vector();
 
                 mediaDescs.add(am);
 
                 sessDescr.setVersion(v);
                 sessDescr.setOrigin(o);
                 sessDescr.setConnection(c);
                 sessDescr.setSessionName(s);
                 sessDescr.setTimeDescriptions(timeDescs);
                 if (mediaDescs.size() > 0) {
                     sessDescr.setMediaDescriptions(mediaDescs);
                 }
                 if (console.isDebugEnabled()) {
                     console.debug("Generated SDP - " + sessDescr.toString());
                 }
                 return sessDescr.toString();
             } catch (SdpException exc) {
                 console.error(
                         "An SDP exception occurred while generating local sdp description",
                         exc);
                 throw new MediaException(
                         "An SDP exception occurred while generating local sdp description",
                         exc);
             }
         } finally {
             console.logExit();
         }
     }
 
     public String getAudioPort() {
         try {
             console.logEntry();
             String audioPort = this.audioPort;
             return audioPort == null ? "22224" : audioPort;
         } finally {
             console.logExit();
         }
     }
 
 
     protected void finalize() {
         try {
             console.logEntry();
             try {
                 if (avDataSource != null) {
                     avDataSource.disconnect();
                 }
             } catch (Exception exc) {
                 console.error("Failed to disconnect data source:" +
                               exc.getMessage());
             }
         } finally {
             console.logExit();
         }
     }
 
     public boolean isStarted() {
         return isStarted;
     }
 
     protected void checkIfStarted() throws MediaException {
         if (!isStarted()) {
             console.error("The MediaManager had not been properly started! "
                           + "Impossible to continue");
             throw new MediaException(
                     "The MediaManager had not been properly started! "
                     + "Impossible to continue");
         }
     }
 
     protected boolean isAudioTransmissionSupported() {
         return transmittableAudioFormats.size() > 0;
     }
 
 
     protected boolean isMediaTransmittable(String media) {
         if (media.equalsIgnoreCase("audio")
             /*&& isAudioTransmissionSupported()*/) {
             return true;
         } else {
             return false;
         }
     }
 
     protected String[] getReceivableAudioFormats() {
         return receivableAudioFormats;
     }
 
     protected String[] getReceivableVideoFormats() {
         return receivableVideoFormats;
     }
 
     protected String findCorrespondingJmfFormat(String sdpFormatStr) {
         int sdpFormat = -1;
         try {
             sdpFormat = Integer.parseInt(sdpFormatStr);
         } catch (NumberFormatException ex) {
             return null;
         }
         switch (sdpFormat) {
         case SdpConstants.PCMU:
             return AudioFormat.ULAW_RTP;
         case SdpConstants.GSM:
             return AudioFormat.GSM_RTP;
         case SdpConstants.G723:
             return AudioFormat.G723_RTP;
         case SdpConstants.DVI4_8000:
             return AudioFormat.DVI_RTP;
         case SdpConstants.DVI4_16000:
             return AudioFormat.DVI_RTP;
         case SdpConstants.PCMA:
             return AudioFormat.ALAW;
         case SdpConstants.G728:
             return AudioFormat.G728_RTP;
         case SdpConstants.G729:
             return AudioFormat.G729_RTP;
         case SdpConstants.H263:
             return VideoFormat.H263_RTP;
         case SdpConstants.JPEG:
             return VideoFormat.JPEG_RTP;
         case SdpConstants.H261:
             return VideoFormat.H261_RTP;
         default:
             return null;
         }
     }
 
     protected String findCorrespondingSdpFormat(String jmfFormat) {
         if (jmfFormat == null) {
             return null;
         } else if (jmfFormat.equals(AudioFormat.ULAW_RTP)) {
             return Integer.toString(SdpConstants.PCMU);
         } else if (jmfFormat.equals(AudioFormat.GSM_RTP)) {
             return Integer.toString(SdpConstants.GSM);
         } else if (jmfFormat.equals(AudioFormat.G723_RTP)) {
             return Integer.toString(SdpConstants.G723);
         } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
             return Integer.toString(SdpConstants.DVI4_8000);
         } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
             return Integer.toString(SdpConstants.DVI4_16000);
         } else if (jmfFormat.equals(AudioFormat.ALAW)) {
             return Integer.toString(SdpConstants.PCMA);
         } else if (jmfFormat.equals(AudioFormat.G728_RTP)) {
             return Integer.toString(SdpConstants.G728);
         } else if (jmfFormat.equals(AudioFormat.G729_RTP)) {
             return Integer.toString(SdpConstants.G729);
         } else if (jmfFormat.equals(VideoFormat.H263_RTP)) {
             return Integer.toString(SdpConstants.H263);
         } else if (jmfFormat.equals(VideoFormat.JPEG_RTP)) {
             return Integer.toString(SdpConstants.JPEG);
         } else if (jmfFormat.equals(VideoFormat.H261_RTP)) {
             return Integer.toString(SdpConstants.H261);
         } else {
             return null;
         }
     }
 
     /**
      * @param sdpFormats
      * @return
      * @throws MediaException
      */
     protected ArrayList extractTransmittableJmfFormats(Vector sdpFormats) throws
             MediaException {
         try {
             console.logEntry();
             ArrayList jmfFormats = new ArrayList();
             for (int i = 0; i < sdpFormats.size(); i++) {
 
                 String jmfFormat =
                         findCorrespondingJmfFormat(sdpFormats.elementAt(i).
                         toString());
                 if (jmfFormat != null) {
                     jmfFormats.add(jmfFormat);
                 }
             }
             if (jmfFormats.size() == 0) {
                 throw new MediaException(
                         "None of the supplied sdp formats for is supported by SIP COMMUNICATOR");
             }
             return jmfFormats;
         } finally {
             console.logExit();
         }
     }
 
     //This is the data source that we'll be using to transmit
     //let's see what can it do
 
 
     protected void initProcessor(DataSource dataSource) throws MediaException {
         try {
             console.logEntry();
             try {
                 try {
                     dataSource.connect();
                 }
                 //Thrown when operation is not supported by the OS
                 catch (NullPointerException ex) {
                     console.error(
                             "An internal error occurred while"
                             + " trying to connec to to datasource!", ex);
                     throw new MediaException(
                             "An internal error occurred while"
                             + " trying to connec to to datasource!", ex);
                 }
                 processor = Manager.createProcessor(dataSource);
                 processor.configure();
                 boolean success =
                     procUtility.waitForState(processor, Processor.Configured);
                 if (!success) {
                     throw new MediaException(
                             "Media manager could not create a processor\n"
                             + "for the specified data source");
                 }
             } catch (NoProcessorException ex) {
                 console.error(
                         "Media manager could not create a processor\n"
                         + "for the specified data source",
                         ex
                         );
                 throw new MediaException(
                         "Media manager could not create a processor\n"
                         + "for the specified data source", ex);
             } catch (IOException ex) {
                 console.error(
                         "Media manager could not connect "
                         + "to the specified data source",
                         ex);
                 throw new MediaException("Media manager could not connect "
                                          + "to the specified data source", ex);
             }
             processor.setContentDescriptor(new ContentDescriptor(
                     ContentDescriptor.RAW_RTP));
             TrackControl[] trackControls = processor.getTrackControls();
 
             if (console.isDebugEnabled()) {
                 console.debug("We will be able to transmit in:");
             }
             for (int i = 0; i < trackControls.length; i++) {
                 Format[] formats = trackControls[i].getSupportedFormats();
                 for (int j = 0; j < formats.length; j++) {
                     Format format = formats[j];
                     String encoding = format.getEncoding();
                     if (format instanceof AudioFormat) {
                         String sdp = findCorrespondingSdpFormat(encoding);
                         if (sdp != null &&
                             !transmittableAudioFormats.contains(sdp)) {
                             if (console.isDebugEnabled()) {
                                 console.debug("Audio=[" + (j + 1) + "]=" +
                                               encoding + "; sdp=" + sdp);
                             }
                             transmittableAudioFormats.add(sdp);
                         }
                     }
 
                 }
             }
         } finally {
             console.logExit();
         }
 
     }
 
     /**
      * Returns a cached instance of an RtpManager bound on the specified local
      * address. If no such instance exists null is returned.
      * @param localAddress the address where the rtp manager must be bound locally.
      * @return an rtp manager bound on the specified address or null if such an
      * instance was not found.
      */
     synchronized RTPManager getRtpManager(SessionAddress localAddress) {
         return (RTPManager) activeRtpManagers.get(localAddress);
     }
 
     /**
      * Maps the specified rtp manager against the specified local address so
      * that it may be later retrieved in case someone wants to operate
      * (transmit/receive) on the same port.
      * @param localAddress the address where the rtp manager is bound
      * @param rtpManager the rtp manager itself
      */
     synchronized void putRtpManager(SessionAddress localAddress,
                                     RTPManager rtpManager) {
         activeRtpManagers.put(localAddress, rtpManager);
     }
 
     /**
      * Removes all rtp managers from the rtp manager cache.
      */
     synchronized void removeAllRtpManagers() {
         activeRtpManagers.clear();
     }
 
 
     /**
      * Moves formats with the specified encoding to the top of the array list
      * so that they are the ones chosen for transmission (if supported by the
      * remote party) (feature request by Vince Fourcade)
      */
     protected void surfacePreferredEncodings(String[] formats) {
         try {
             console.logEntry();
             String preferredAudioEncoding = sipProp.getProperty(
                     "net.java.sip.communicator.media.PREFERRED_AUDIO_ENCODING");
             String preferredVideoEncoding = sipProp.getProperty(
                     "sipProp.java.sip.communicator.media.PREFERRED_VIDEO_ENCODING");
             if (preferredAudioEncoding == null && preferredVideoEncoding == null) {
                 return;
             }
             for (int i = 0; i < formats.length; i++) {
                 String encoding = formats[i];
                 if ((preferredAudioEncoding != null
                      && encoding.equalsIgnoreCase(preferredAudioEncoding))
                     || (preferredVideoEncoding != null
                         && encoding.equalsIgnoreCase(preferredVideoEncoding))) {
                     formats[i] = formats[0];
                     formats[0] = encoding;
                     if (console.isDebugEnabled()) {
                         console.debug("Encoding  [" +
                                       findCorrespondingJmfFormat(encoding) +
                                       "] is set as preferred.");
                     }
                     break;
                 }
             }
         } finally {
             console.logExit();
         }
     }
 }
