 package jcue.domain;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.util.HashMap;
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
     private HashMap<SoundDevice, Double> deviceVolumes;
    private HashMap<SoundDevice, Boolean> deviceMuted;
     
     private HSYNC stopSync;
     private HSTREAM stream; //TODO: remove when multi-device stuff fully functional
     private String filePath;
     
     private double length;
     
     private double volume, pan;
     
     private FloatBuffer streamData;
 
     public AudioStream(List<SoundDevice> outputs) {
         this.streams = new TreeMap<SoundDevice, HSTREAM>();
         this.deviceVolumes = new HashMap<SoundDevice, Double>();
        this.deviceMuted = new HashMap<SoundDevice, Boolean>();
 
         for (SoundDevice sd : outputs) {
             this.streams.put(sd, null);
             this.deviceVolumes.put(sd, 1.0);
            this.deviceMuted.put(sd, false);
         }
         
         this.volume = 1.0;
         this.pan = 0.0;
     }
 
     /**
      * Creates a new stream from file for specified SoundDevice.
      * 
      * @param path Path to a file
      * @param sd SoundDevice used
      * @throws Exception 
      */
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
 
     /**
      * Links all streams for different outputs together.
      */
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
 
     /**
      * Unlinks a specified stream from other streams.
      * 
      * @param stream Stream to be unlinked
      */
     private void unlinkStream(HSTREAM stream) {
         for (SoundDevice sd : this.streams.keySet()) {
             HSTREAM tmp = this.streams.get(sd);
             
             if (tmp != stream) {
                 Bass.BASS_ChannelRemoveLink(stream.asInt(), tmp.asInt());
             }
         }
     }
     
     /**
      * Loads a new audio file to this cue.
      * 
      * @param path Path to the file
      * @throws Exception 
      */
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
 
     /**
      * Adds a new output.
      * 
      * @param sd Output device to be added
      */
     public void addOutput(SoundDevice sd) {
         //If audio doesn't already contain the output
         if (!this.streams.containsKey(sd)) {
             this.streams.put(sd, null);         //Add output to the map
             this.deviceVolumes.put(sd, 1.0);    //Device volume defaults to 1
            this.deviceMuted.put(sd, false);    //Device not muted by default
 
             //If there's already a file loaded, load it to the new output also
             if (this.filePath != null && !this.filePath.isEmpty()) {
                 try {
                     loadFile(this.filePath, sd);
                     linkStreams();  //Link new output to previous ones
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * Removes output if it exists.
      * 
      * @param sd Output to be removed
      */
     public void removeOutput(SoundDevice sd) {
         if (!this.streams.containsKey(sd)) {
             return;
         }
         
         HSTREAM stream = this.streams.get(sd);
         
         unlinkStream(stream);           //Unlink from other streams
         Bass.BASS_StreamFree(stream);   //Release stream
         this.streams.remove(sd);        //Remove from the collection
        this.deviceVolumes.remove(sd);
        this.deviceMuted.remove(sd);
     }
     
     /**
      * Starts playing audio through all outputs.
      */
     public void play() {
         //Get the first entry of the output map
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         //If stream loaded start it. Links automatically handle starting other
         //outputs.
         if (firstStream != null) {
             Bass.BASS_ChannelPlay(firstStream.asInt(), false);
         }
     }
 
     /**
      * Pauses audio.
      */
     public void pause() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             Bass.BASS_ChannelPause(firstStream.asInt());
         }
     }
 
     /**
      * Stops all outputs.
      */
     public void stop() {
         Entry<SoundDevice, HSTREAM> firstEntry = this.streams.firstEntry();
         HSTREAM firstStream = firstEntry.getValue();
 
         if (firstStream != null) {
             Bass.BASS_ChannelStop(firstStream.asInt());
         }
     }
 
     /**
      * Sets the position of the audio.
      * 
      * @param pos New position in seconds
      */
     public void setPosition(double pos) {
         for (HSTREAM stream : this.streams.values()) {
             if (stream != null) {
                 long bytePos = Bass.BASS_ChannelSeconds2Bytes(stream.asInt(), pos);
                 Bass.BASS_ChannelSetPosition(stream.asInt(), bytePos, BASS_POS.BASS_POS_BYTE);
             }
         }
     }
 
     /**
      * Gets the current position of the audio.
      * 
      * @return Current audio position in seconds
      */
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
 
     /**
      * Sets the output volume for a specific output device.
      * 
      * @param volume New volume from 0 to 1
      * @param sd Output to be adjusted
      */
     public void setDeviceVolume(double volume, SoundDevice sd) {
         this.deviceVolumes.put(sd, volume);
         
         double newVolume = volume * this.volume;
         HSTREAM tmp = this.streams.get(sd);
        boolean muted = this.deviceMuted.get(sd);
         
        if (!muted && tmp != null) {
             Bass.BASS_ChannelSetAttribute(tmp.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, (float) newVolume);
         }
     }
     
     /**
      * Sets the master volume for this audio.
      * 
      * @param volume New volume from 0 to 1
      */
     public void setMasterVolume(double volume) {
         this.volume = volume;
         
         for (SoundDevice sd : this.streams.keySet()) {
             double deviceVol = this.deviceVolumes.get(sd);
             
             setDeviceVolume(deviceVol, sd);
         }
     }
     
     /**
      * Mutes the specified output.
      * 
      * @param sd Output to be muted
      */
     public void muteOutput(SoundDevice sd) {
         if (this.streams.containsKey(sd)) {
             HSTREAM tmp = this.streams.get(sd);
             
            if (tmp != null) {
                Bass.BASS_ChannelSetAttribute(tmp.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, 0);
            }
            
            this.deviceMuted.put(sd, true);
         }
     }
 
     /**
      * Unmutes the specified output.
      * 
      * @param sd Output to be unmuted
      */
     public void unmuteOutput(SoundDevice sd) {
         if (this.streams.containsKey(sd)) {
            this.deviceMuted.put(sd, false);
             setDeviceVolume(getDeviceVolume(sd), sd);
         }
     }
     
     public void setPan(double pan) {
         Bass.BASS_ChannelSetAttribute(this.stream.asInt(),
                 BASS_ATTRIB.BASS_ATTRIB_PAN,
                 (float) pan);
     }
 
     /**
      * Sets the out position for the audio.
      * 
      * @param seconds New out position in seconds
      */
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
 
     /**
      * Returns the length of the audio.
      * 
      * @return Length of the audio in seconds
      */
     public double getLength() {
         return length;
     }
 
     /**
      * Returns the path to the file currently loaded.
      * 
      * @return Path to the file
      */
     public String getFilePath() {
         return filePath;
     }
 
     /**
      * Returns floating point data of the audio.
      * 
      * @return Stream data
      */
     public FloatBuffer getStreamData() {
         return this.streamData;
     }
     
     /**
      * Returns the master volume of the audio.
      * 
      * @return Master volume from 0 to 1
      */
     public double getMasterVolume() {
         return this.volume;
     }
     
     /**
      * Returns the volume of a specific output device.
      * 
      * @param sd Output device which volume to get
      * @return Outputs volume from 0 to 1
      */
     public double getDeviceVolume(SoundDevice sd) {
         return this.deviceVolumes.get(sd);
     }
     
     /**
      * Loads floating point data from the file to a FloatBuffer
      */
     private void loadStreamData() {
         HSTREAM tmp = Bass.BASS_StreamCreateFile(false, this.filePath, 0, 0, BASS_STREAM.BASS_STREAM_DECODE | BASS_SAMPLE.BASS_SAMPLE_FLOAT);
         long dataLength = Bass.BASS_ChannelGetLength(tmp.asInt(), BASS_POS.BASS_POS_BYTE);
         int size = (int) (dataLength);
 
         ByteBuffer buffer = BufferUtils.newByteBuffer(size);
 
         Bass.BASS_ChannelGetData(tmp.asInt(), buffer, size);
 
         this.streamData = buffer.asFloatBuffer();
 
         System.gc();
     }
     
     public void printVolumes() {
         System.out.println("Master: " + this.volume);
         
         for (SoundDevice sd : this.streams.keySet()) {
             System.out.println("+--- " + sd.getName() + ": " + this.deviceVolumes.get(sd));
             
             FloatBuffer buf = BufferUtils.newFloatBuffer(1);
             HSTREAM tmp = this.streams.get(sd);
             double streamVol = 0;
             
             if (tmp != null) {
                 Bass.BASS_ChannelGetAttribute(tmp.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, buf);
                 streamVol = buf.get();
             }
             System.out.println("|  +--- Result: " + streamVol);
         }
         System.out.println();
     }
 }
