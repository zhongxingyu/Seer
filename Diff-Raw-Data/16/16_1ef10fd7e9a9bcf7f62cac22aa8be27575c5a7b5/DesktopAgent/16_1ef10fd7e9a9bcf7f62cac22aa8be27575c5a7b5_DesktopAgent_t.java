 package net.sourceforge.gjtapi.raw.javasound.desktopAgent;
 
 import java.util.HashMap;
 
 import net.sourceforge.gjtapi.raw.javasound.JavaSoundCallId;
 import net.sourceforge.gjtapi.raw.javasound.JavaSoundProvider;
 import net.sourceforge.gjtapi.util.SizedPipedInputStream;
 
 import javax.telephony.ConnectionEvent;
 import javax.telephony.Event;
 
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.TargetDataLine;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.DataLine.Info;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.IOException;
 import java.util.Timer;
 import javax.sound.sampled.AudioInputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 
 
 /**
  * <p>Title: Desktop Agent</p>
  *
  * <p>Description: </p>
  *
  * <p>Copyright: Copyright (c) 2008</p>
  *
  * <p>Company: L2F | INESC-ID</p>
  *
  * @author Drio Marcelino
  * @version 1.0
  */
 public class DesktopAgent {
     public final int BUFFER_SIZE = 1024;
     //public final int BUFFER_SIZE = 1280;
     //public final int BUFFER_SIZE = 160;
     public final AudioFormat DEFAULT_FORMAT = new AudioFormat(AudioFormat.
             Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false);
 
     private String address;
     private HashMap<String, Mixer> playbackMixers;
     private HashMap<String, Mixer> captureMixers;
     private JavaSoundProvider provider;
 
     //Selected mixers
    private Mixer selPlaybackMixer;
    private Mixer selCaptureMixer;
 
     //GUI
     private DesktopAgentGUI gui;
 
     /** Id from current call (It's assumed an agent can only handle one call at a time) */
     JavaSoundCallId callID = null;
 
     //destinatary addr - Saves the address of an outgoing call
     private String destAddress;
 
     //media stop
     private boolean play = false;
     private boolean record = false;
 
     private boolean playing = false;
     private boolean recording = false;
 
     //Audio format
     //int buffer = 4096;
     private AudioFormat format;
     private Info playbackLineInfo;
     private Info captureLineInfo;
 
     public DesktopAgent(String address, HashMap<String, Mixer> playbackMixers,
             HashMap<String, Mixer> captureMixers, JavaSoundProvider provider) {
         this.address = address;
         this.playbackMixers = playbackMixers;
         this.captureMixers = captureMixers;
         this.provider = provider;
 
         gui = new DesktopAgentGUI(this);
         gui.run();
     }
 
     /**
      * DesktopAgent
      *
      * @param string String
      * @param desktopAgentProps DesktopAgentProps
      * @param javaSoundProvider JavaSoundProvider
      */
     public DesktopAgent(DesktopAgentProps daProps, HashMap<String, Mixer>
             playbackMixers,
             HashMap<String, Mixer> captureMixers, JavaSoundProvider provider) {
         this.address = daProps.getAddress();
         this.playbackMixers = playbackMixers;
         this.captureMixers = captureMixers;
         this.provider = provider;
 
         if ((format = daProps.getFormat()) != null) {
             playbackLineInfo = new Info(SourceDataLine.class, format,
                                         AudioSystem.NOT_SPECIFIED);
             captureLineInfo = new Info(TargetDataLine.class, format,
                                        AudioSystem.NOT_SPECIFIED);
         } else {
             playbackLineInfo = new Info(SourceDataLine.class, DEFAULT_FORMAT,
                                         AudioSystem.NOT_SPECIFIED);
             captureLineInfo = new Info(TargetDataLine.class, DEFAULT_FORMAT,
                                        AudioSystem.NOT_SPECIFIED);
         }
 
         this.selectPlaybackMixer(daProps.getPlaybackDevice());
         this.selectCaptureMixer(daProps.getCaptureDevice());
 
         gui = new DesktopAgentGUI(this);
         gui.run();
     }
 
     public void setJavaSoundCallId(JavaSoundCallId id) {
         callID = id;
     }
 
     public JavaSoundCallId getJavaSoundCallId() {
         return callID;
     }
 
     /**
      * Accept incoming call
      */
     public void accept() {
         gui.accepted();
 
         provider.connectionConnected(callID, destAddress,
                                      ConnectionEvent.CAUSE_NORMAL);
         provider.callActive(callID, Event.CAUSE_NORMAL);
     }
 
     /**
      * Hangup current call
      */
     public void hangup() {
         gui.hangup();
         provider.connectionDisconnected(callID, address, Event.CAUSE_NORMAL);
         callID = null;
     }
 
     public void call(JavaSoundCallId callID, String address) {
         this.callID = callID;
         destAddress = address;
         gui.call();
 
         /** When an outgoing call is remotly ringing */
         provider.terminalConnectionCreated(callID, address,
                                            address,
                                            ConnectionEvent.CAUSE_NORMAL);
         provider.connectionInProgress(callID, address,
                                       Event.CAUSE_NORMAL);
         provider.connectionAlerting(callID, address,
                                     ConnectionEvent.CAUSE_NORMAL);
     }
 
     /**
      * Play from an InputStream
      *
      * @param is InputStream
      */
     AudioInputStream ais;
     InputStream rtpStream;
     public void play(InputStream inStream, long duration) {
         play = true;
         playing = true;
         Info pbLineInfo = playbackLineInfo;
         rtpStream = inStream;
 
         Timer timer;
         if (duration != javax.telephony.media.ResourceConstants.v_Forever) {
             timer = new Timer("PlayDuration");
             timer.schedule(new StopPlayTask(this), duration);
             //System.out.println("Play: Duration set to: " + duration);
         }
 
         SourceDataLine source_line;
         AudioInputStream convertedStream = null;
 
         byte[] b;
         boolean conversion = false; //flag indicating audio conversion will occur
 
         if (!selPlaybackMixer.isLineSupported(pbLineInfo)) {
             pbLineInfo = new Info(SourceDataLine.class, DEFAULT_FORMAT,
                                         AudioSystem.NOT_SPECIFIED);
             if (selPlaybackMixer.isLineSupported(pbLineInfo)) {
                 //System.out.println("AudioLine not supported by this Mixer, using conversion.");
                 conversion = true;
             } else {
                 System.err.println("ERROR: AudioLine not supported by this Mixer!");
             }
         }
         try {
             source_line = (SourceDataLine) selPlaybackMixer.getLine(
                     pbLineInfo);
             source_line.open();
             b = new byte[BUFFER_SIZE];
             //System.out.println("Buffer length: " + b.length);
             source_line.start();
 
             if (conversion) {
                 try {
                     //From 'format' to 'DEFAULT_FORMAT'
                     convertedStream = AudioSystem.getAudioInputStream(
                             DEFAULT_FORMAT,
                             new AudioInputStream(inStream, format, -1));
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 } finally {
                     if (convertedStream == null) {
                         throw new RuntimeException("null converted stream");
                     }
                 }
                 ais = convertedStream;
             }
             else
                 ais = new AudioInputStream(inStream, format, -1);
 
             //cicle
             int a, c;
             while ((a = ais.read(b)) != -1 /*&& isPlaying()*/) {
                     c = source_line.write(b, 0, a);
                     //System.out.println(a + " bytes read @ play");
                 //System.out.println(c + " bytes actually written");
                 if (a == 0) {
                     Thread.currentThread().sleep(15);
                 }
             }
 
             System.out.println("DesktopAgent: ais.read(b) = -1");
 
            /*if (play == true){ // if there was no stopPlay()
                 ais.close();
             }*/
 
             source_line.drain();
             source_line.stop();
             source_line.close();
 
             play = false;
             playing = false;
 
         } catch (IOException e) {
             System.err.println(
                     "IOException: Pipe closed?!?!?!?!");
             e.printStackTrace();
         } catch (LineUnavailableException e) {
             System.err.println(
                     "ERROR: LineUnavailableException at AudioSender()");
             e.printStackTrace();
         } catch (InterruptedException ex) {
         }
     }
 
     /**
      * Record to an OutputStream
      *
      * @param os OutputStream
      */
     public void record(OutputStream outStream, long duration) {
         record = true;
         recording = true;
         Info cLineInfo = captureLineInfo;
 
         Timer timer;
         if (duration != javax.telephony.media.ResourceConstants.v_Forever) {
             timer = new Timer("RecordDuration");
             timer.schedule(new StopRecordTask(this), duration);
             System.out.println("Record: Duration set to: " + duration);
         }
 
         TargetDataLine target_line;
         OutputStream os = outStream;
         byte[] b;
         boolean conversion = false; //flag indicating audio conversion will occur
 
         if (!selCaptureMixer.isLineSupported(captureLineInfo)) {
             cLineInfo = new Info(TargetDataLine.class, DEFAULT_FORMAT,
                                        AudioSystem.NOT_SPECIFIED);
             if (selCaptureMixer.isLineSupported(cLineInfo)) {
                 System.out.println(
                         "AudioLine not supported by this Mixer, using conversion.");
                 conversion = true;
             } else {
                 System.err.println(
                         "ERROR: AudioLine not supported by this Mixer.");
             }
         }
 
         try {
             target_line = (TargetDataLine) selCaptureMixer.getLine(
                     cLineInfo);
             target_line.open();
             b = new byte[BUFFER_SIZE];
             //System.out.println("Buffer length: " + b.length);
             target_line.start();
 
             if (conversion) {
                 PipedOutputStream pos = null;
 
                 // Use the SizedPipedInputStream, which is a backported version
                 // of the Java 6 PipedInputStream.
                 // This can be moved back once Java 6 is the norm for GJTAPI
                 PipedInputStream pis = new SizedPipedInputStream((int) (format.
                         getChannels() * format.getSampleSizeInBits() / 8 *
                         format.getSampleRate()));
 
                 try {
                     pos = new PipedOutputStream(pis);
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
 
                 AudioInputStream convertedStream = AudioSystem.
                         getAudioInputStream(format,
                                             new AudioInputStream(pis,
                         DEFAULT_FORMAT, -1));
                 int frameSize = convertedStream.getFormat().getFrameSize();
 
                 //cicle
                 int a;
                 while (isRecording()) {
                     //read from speaker
                     a = target_line.read(b, 0, b.length);
                     //System.out.println(a + " bytes read from mic @ record");
 
                     //conversion
                     pos.write(b, 0, a);
                     a = convertedStream.available();
                     //System.out.println(a + " available in convertedStream @ record");
                     float b_size = (Math.min(a,
                             b.length * frameSize) * ((float)format.getSampleSizeInBits() / (float)DEFAULT_FORMAT.getSampleSizeInBits()));
                     byte[] convBuffer = new byte[(int)b_size];
                     a = convertedStream.read(convBuffer);
                     //System.out.println(a + " bytes read from convertedStream @ record");
 
                     //write to stream
                     if (a != 0) {
                        os.write(convBuffer, 0, a);
                        //System.out.println(a + " bytes wrote @ record");
                     } else {
                         Thread.currentThread().sleep(5);
                     }
                 }
             } else {
 
                 //cicle
                 int a;
                 while (isRecording()) {
                     a = target_line.read(b, 0, b.length);
                     os.write(b, 0, a);
                     //System.out.println(a + " bytes read @ record");
                     if (a == 0) {
                         Thread.currentThread().sleep(5);
                     }
                 }
             }
 
             //When it's finished
             os.close();
             target_line.stop();
             target_line.close();
             record = false;
             recording = false;
 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (LineUnavailableException e) {
             System.err.println(
                     "ERROR: LineUnavailableException at AudioSender()");
             e.printStackTrace();
         } catch (InterruptedException ex) {
         }
     }
 
     /**
      * Stop Play and Recording
      */
     public void stop() {
         stopPlay();
         stopRecord();
     }
 
     /**
      * stopPlay
      */
     public void stopPlay() {
         System.out.println("DesktopAgent: StopPlay");
         play = false;
         try {
             long time = System.currentTimeMillis();
             //rtpStream.close();
            if (ais != null) ais.close();
             System.out.println(">>>>>>>>>>>>> StopPlay, close >>>>>>>>>>>>> " + (System.currentTimeMillis() - time)/1000 + "s.");
         } catch (IOException ex1) {
         }
 
         while (playing == true) {
             try {
                 Thread.currentThread().sleep(10);
             } catch (InterruptedException ex) {
             }
         }
         System.out.println("DesktopAgent: PlayStopped!");
     }
 
     /**
      * stopRecord
      */
     public void stopRecord() {
         System.out.println("DesktopAgent: StopRecord");
         record = false;
         while (recording == true) {
            try {
                Thread.currentThread().sleep(10);
            } catch (InterruptedException ex) {
            }
        }
     }
 
     /**
      * isPlaying
      */
     public boolean isPlaying() {
         return play;
     }
 
     /**
      * isRecording
      */
     public boolean isRecording() {
         //System.out.println("DesktopAgent: Record" + record);
         return record;
     }
 
 
 //------------ GUI related methods -----------------------------------------
 
     /**
      * Returns the agent Address
      *
      * @return String
      */
     public String getAddress() {
         return address;
     }
 
     /**
      * Returns a string with the Playback Mixers
      *
      * @return String[]
      */
     public String[] getPlaybackMixers() {
         String[] ret = new String[playbackMixers.size()];
         int i = 0;
         for (String st : playbackMixers.keySet()) {
             ret[i] = st;
             i++;
         }
         return ret;
     }
 
     /**
      * Returns a string with the Capture Mixers
      *
      * @return String[]
      */
     public String[] getCaptureMixers() {
         String[] ret = new String[captureMixers.size()];
         int i = 0;
         for (String st : captureMixers.keySet()) {
             ret[i] = st;
             i++;
         }
         return ret;
     }
 
     /**
      * Incoming call from the simulated outside client
      */
     public void incomingCall() {
         callID = new JavaSoundCallId();
         provider.terminalConnectionRinging(callID, address,
                                            address,
                                            ConnectionEvent.CAUSE_NORMAL);
         provider.connectionInProgress(callID, address,
                                       Event.CAUSE_NORMAL);
         provider.connectionAlerting(callID, address,
                                     ConnectionEvent.CAUSE_NORMAL);
     }
 
     /**
      * Hang up by the simulated outside client
      */
     public void remoteHangup() {
         /** When a call has been locally or remotely closed */
         provider.connectionDisconnected(callID, address, Event.CAUSE_NORMAL);
         callID = null;
     }
 
     /**
      * selectPlaybackMixer
      *
      * @param string String
      */
     public void selectPlaybackMixer(String selectedMixer) {
         selPlaybackMixer = playbackMixers.get(selectedMixer);
         if (selPlaybackMixer == null) {
             selPlaybackMixer = (Mixer) playbackMixers.values().toArray()[0];
         }
     }
 
     public String getSelPlaybackMixer() {
         return selPlaybackMixer.getMixerInfo().getName();
     }
 
 
     /**
      * Selects de Capture Mixer
      *
      * @param string selectedMixer
      */
     public void selectCaptureMixer(String selectedMixer) {
         selCaptureMixer = captureMixers.get(selectedMixer);
         if (selCaptureMixer == null) {
             selCaptureMixer = (Mixer) captureMixers.values().toArray()[0];
         }
     }
 
     public String getSelCaptureMixer() {
         return selCaptureMixer.getMixerInfo().getName();
     }
 
     /**
      * CAll accepted by the simulated outside client
      */
     public void accepted() {
         /** When an outgoing call has been accepted */
         provider.connectionConnected(callID, destAddress,
                                      ConnectionEvent.CAUSE_NORMAL);
         provider.callActive(callID, Event.CAUSE_NORMAL);
     }
 
     /**
      * DesktopAgent shutdown
      */
     public void shutdown() {
         if (callID != null) {
             remoteHangup();
         }
         provider.removeDesktopAgent(address);
     }
 
 
 }
