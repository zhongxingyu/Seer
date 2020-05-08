 package jcue.domain;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import jouvieje.bass.Bass;
 import jouvieje.bass.defines.*;
 import jouvieje.bass.structures.HSTREAM;
 import jouvieje.bass.structures.HSYNC;
 import jouvieje.bass.utils.BufferUtils;
 
 /**
  * Handles audio playback.
  *
  * @author Jaakko
  */
 public class AudioStream {
 
     private TreeMap<SoundDevice, HSTREAM> streams;
     private HSYNC stopSync;
     private HSTREAM stream; //TODO: remove when multi-device stuff fully functional
     private String filePath;
     
     private double length;
     
     private double volume, pan;
     
     private FloatBuffer streamData;
 
     public AudioStream(List<SoundDevice> outputs) {
         this.streams = new TreeMap<SoundDevice, HSTREAM>();
 
         for (SoundDevice sd : outputs) {
             this.streams.put(sd, null);
         }
         
         this.volume = 1.0;
         this.pan = 0.0;
     }
 
     //Creates a new stream from file for specified SoundDevice
     private void loadFile(String path, SoundDevice sd) throws Exception {
         Bass.BASS_SetDevice(sd.getId());    //Change currently used device
         HSTREAM newStream = Bass.BASS_StreamCreateFile(false, path, 0, 0, 0);   //Create the stream
 
         //Stream creation failed -> throw an exception
         if (newStream == null) {
             throw new Exception("Error! Loading audio file " + path + " failed! Device: " + sd.getName());
         }
 
         //Add the stream to the hashmap
         this.streams.put(sd, newStream);
     }
 
     //Links all streams together
     private void linkStreams() {
         //Loop through all streams
         Iterator<SoundDevice> it = this.streams.keySet().iterator();
         while (it.hasNext()) {
             SoundDevice sd = it.next();
 
             //Link the stream to all of the other streams
             Iterator<SoundDevice> it2 = this.streams.keySet().iterator();
             while (it2.hasNext()) {
                 SoundDevice sd2 = it2.next();
 
                 if (!sd.equals(sd2)) {
                     HSTREAM stream1 = this.streams.get(sd);
                     HSTREAM stream2 = this.streams.get(sd2);
 
                     Bass.BASS_ChannelSetLink(stream1.asInt(), stream2.asInt());
                 }
             }
         }
     }
 
     //Used to load a new audio file for use
     public void loadFile(String path) throws Exception {
         //Free possible existing streams
         for (SoundDevice sd : this.streams.keySet()) {
             HSTREAM tmp = this.streams.get(sd);
 
             if (tmp != null) {
                 Bass.BASS_StreamFree(tmp);
                 this.streams.put(sd, null);
             }
         }
 
         //Create a new stream for every device used
         for (SoundDevice sd : this.streams.keySet()) {
             loadFile(path, sd);
         }
 
         //Get information on the stream
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM tmpStream = firstEntry.getValue();
         if (tmpStream != null) {
             double bytePos = Bass.BASS_ChannelGetLength(tmpStream.asInt(), BASS_POS.BASS_POS_BYTE);
             this.length = Bass.BASS_ChannelBytes2Seconds(tmpStream.asInt(), (long) bytePos);
 
             this.filePath = path;
 
             loadStreamData();   //Get data for waveform
         }
 
         //Finally link all streams together and load stream data for thw waveform
         linkStreams();
     }
 
     //Adds a new output for this stream
     public void addOutput(SoundDevice sd) {
         this.streams.put(sd, null);
 
         if (this.filePath != null && !this.filePath.isEmpty()) {
             try {
                 loadFile(this.filePath, sd);
                 linkStreams();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     public void play() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             Bass.BASS_ChannelPlay(firstStream.asInt(), false);
         }
     }
 
     public void pause() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             Bass.BASS_ChannelPause(firstStream.asInt());
         }
     }
 
     public void stop() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             Bass.BASS_ChannelStop(firstStream.asInt());
         }
     }
 
     public void setPosition(double pos) {
         for (HSTREAM stream : this.streams.values()) {
             if (stream != null) {
                 long bytePos = Bass.BASS_ChannelSeconds2Bytes(stream.asInt(), pos);
                 Bass.BASS_ChannelSetPosition(stream.asInt(), bytePos, BASS_POS.BASS_POS_BYTE);
             }
         }
     }
 
     public double getPosition() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM stream = firstEntry.getValue();
 
         if (stream != null) {
             long bytePos = Bass.BASS_ChannelGetPosition(stream.asInt(), BASS_POS.BASS_POS_BYTE);
             return Bass.BASS_ChannelBytes2Seconds(stream.asInt(), bytePos);
         }
 
         return -1;
     }
 
     //TODO: change to work with multi-device shit
     public void startVolumeChange(double newVolume, double duration) {
         if (this.stream != null) {
             Bass.BASS_ChannelSlideAttribute(this.stream.asInt(),
                     BASS_ATTRIB.BASS_ATTRIB_VOL,
                     (float) newVolume,
                     (int) (duration * 1000));
         }
     }
 
     public void startPanChange(double newPan, double duration) {
         if (this.stream != null) {
             Bass.BASS_ChannelSlideAttribute(this.stream.asInt(),
                     BASS_ATTRIB.BASS_ATTRIB_PAN,
                     (float) newPan,
                     (int) (duration * 1000));
         }
     }
 
     public void setDeviceVolume(double volume, SoundDevice sd) {
         double newVolume = volume;
         HSTREAM tmp = this.streams.get(sd);
         
         if (tmp != null) {
             Bass.BASS_ChannelSetAttribute(tmp.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, (float) newVolume);
         }
     }
     
     public void setMasterVolume(double volume) {
         this.volume = volume;
         
         for (SoundDevice sd : this.streams.keySet()) {
             setDeviceVolume(volume, sd);
         }
     }
 
     public void setPan(double pan) {
         Bass.BASS_ChannelSetAttribute(this.stream.asInt(),
                 BASS_ATTRIB.BASS_ATTRIB_PAN,
                 (float) pan);
     }
 
     public void setOutPosition(double seconds) {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             if (this.stopSync != null) {
                 Bass.BASS_ChannelRemoveSync(firstStream.asInt(), this.stopSync);
                 this.stopSync = null;
             }
 
             long bytePos = Bass.BASS_ChannelSeconds2Bytes(firstStream.asInt(), seconds);
             this.stopSync = Bass.BASS_ChannelSetSync(firstStream.asInt(), BASS_SYNC.BASS_SYNC_POS, bytePos, new StopCallback(), null);
         }
     }
 
     public double getLength() {
         return length;
     }
 
     public String getFilePath() {
         return filePath;
     }
 
     public FloatBuffer getStreamData() {
         return this.streamData;
     }
     
     public double getMasterVolume() {
         return this.volume;
     }
     
     public double getDeviceVolume(SoundDevice sd) {
         FloatBuffer buf = BufferUtils.newFloatBuffer(1);
         HSTREAM tmp = this.streams.get(sd);
         
         if (tmp != null) {
             Bass.BASS_ChannelGetAttribute(tmp.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, buf);
 
             return buf.get();
         }
         
         return 0;
     }
 
     private void loadStreamData() {
         HSTREAM tmp = Bass.BASS_StreamCreateFile(false, this.filePath, 0, 0, BASS_STREAM.BASS_STREAM_DECODE | BASS_SAMPLE.BASS_SAMPLE_FLOAT);
         long dataLength = Bass.BASS_ChannelGetLength(tmp.asInt(), BASS_POS.BASS_POS_BYTE);
        int size = (int) (dataLength / 4);
 
         ByteBuffer buffer = BufferUtils.newByteBuffer(size);
 
         Bass.BASS_ChannelGetData(tmp.asInt(), buffer, size);
 
         this.streamData = buffer.asFloatBuffer();
 
         System.gc();
     }
 }
