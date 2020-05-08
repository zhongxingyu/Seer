 package jcue.domain.audiocue;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.util.List;
 import java.util.TreeMap;
 import jcue.domain.AbstractCue;
 import jcue.domain.SoundDevice;
 import jcue.domain.fadecue.ParameterEnvelope;
 import jcue.ui.WaveformPanel;
 import jouvieje.bass.Bass;
 import jouvieje.bass.defines.*;
 import jouvieje.bass.structures.BASS_CHANNELINFO;
 import jouvieje.bass.structures.HSTREAM;
 import jouvieje.bass.structures.HSYNC;
 import jouvieje.bass.utils.BufferUtils;
 
 /**
  * Handles audio playback.
  *
  * @author Jaakko
  */
 public class AudioStream {
 
     private TreeMap<SoundDevice, VirtualOutput> outputs;
     private HSTREAM masterStream;
     
     private BASS_CHANNELINFO streamInfo;
     
     private HSYNC stopSync, fadeOutSync;
     private String filePath;
     
     private double length;
     
     private double volume, pan;
     
     private BufferedImage waveformImg;
 
     public AudioStream(List<SoundDevice> outputs) {
         this.outputs = new TreeMap<SoundDevice, VirtualOutput>();
         
         for (SoundDevice sd : outputs) {
             this.outputs.put(sd, new VirtualOutput(sd));
         }
 
         this.volume = 1.0;
         this.pan = 0.0;
     }
 
     /**
      * Loads a new audio file to this cue.
      *
      * @param path Path to the file
      * @throws Exception
      */
     public void loadFile(String path) throws Exception {
         //Create master stream for linking real outputs
         if (this.masterStream != null) {
             Bass.BASS_StreamFree(this.masterStream);
         }
         
         Bass.BASS_SetDevice(0);     //Use "no sound" device
         this.masterStream = Bass.BASS_StreamCreateFile(false, path, 0, 0, 0);
         Bass.BASS_ChannelSetAttribute(this.masterStream.asInt(), BASS_ATTRIB.BASS_ATTRIB_VOL, 0);
         
         //Load file for outputs
         for (SoundDevice sd : this.outputs.keySet()) {
             VirtualOutput vo = this.outputs.get(sd);
             
             vo.loadFile(path);
             vo.link(this.masterStream);     //Link to master stream
             
             //Update volume and pan
             vo.updateVolume(this.volume);
             vo.setPan(vo.getPan());
         }
 
         //Get information on the stream
         HSTREAM tmpStream = this.masterStream;
         if (tmpStream != null) {
             this.streamInfo = BASS_CHANNELINFO.allocate();
             Bass.BASS_ChannelGetInfo(tmpStream.asInt(), streamInfo);
             
             double bytePos = Bass.BASS_ChannelGetLength(tmpStream.asInt(), BASS_POS.BASS_POS_BYTE);
             this.length = Bass.BASS_ChannelBytes2Seconds(tmpStream.asInt(), (long) bytePos);
 
             this.filePath = path;
 
             createWaveformImg();
         }
     }
 
     /**
      * Adds a new output.
      *
      * @param sd Output device to be added
      */
     public void addOutput(SoundDevice sd) {
         //If audio doesn't already contain the output
         if (!this.outputs.containsKey(sd)) {
             VirtualOutput vo = new VirtualOutput(sd);
             this.outputs.put(sd, vo);
             
             //If there's already a file loaded, load it to the new output also
             if (this.filePath != null && !this.filePath.isEmpty()) {
                 try {
                     vo.loadFile(this.filePath);
                     vo.link(this.masterStream);
                     vo.updateVolume(this.volume);
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
         if (!this.outputs.containsKey(sd)) {
             return;
         }
 
         VirtualOutput vo = this.outputs.get(sd);
         vo.unlink(this.masterStream);
         
         this.outputs.remove(sd);
     }
 
     /**
      * Starts playing audio through all outputs.
      */
     public void play() {
         //Set volume to initial level before starting
         for (VirtualOutput vo : this.outputs.values()) {
             vo.updateVolume(this.volume);
         }
         
         //If stream loaded start it. Links automatically handle starting other
         //outputs.
         if (this.masterStream != null) {
             Bass.BASS_ChannelPlay(this.masterStream.asInt(), false);
         }
     }
 
     /**
      * Pauses audio.
      */
     public void pause() {
         if (this.masterStream != null) {
             Bass.BASS_ChannelPause(this.masterStream.asInt());
         }
     }
 
     /**
      * Stops all outputs.
      */
     public void stop() {
         if (this.masterStream != null) {
             Bass.BASS_ChannelStop(this.masterStream.asInt());
         }
     }
 
     /**
      * Sets the position of the audio.
      *
      * @param pos New position in seconds
      */
     public void setPosition(double pos) {
         if (this.masterStream != null) {
             long bytePos = Bass.BASS_ChannelSeconds2Bytes(this.masterStream.asInt(), pos);
             Bass.BASS_ChannelSetPosition(this.masterStream.asInt(), bytePos, BASS_POS.BASS_POS_BYTE);
 
             for (VirtualOutput vo : this.outputs.values()) {
                 vo.setPosition(pos);
             }
         }
     }
 
     /**
      * Gets the current position of the audio.
      *
      * @return Current audio position in seconds
      */
     public double getPosition() {
         if (this.masterStream != null) {
             long bytePos = Bass.BASS_ChannelGetPosition(this.masterStream.asInt(), BASS_POS.BASS_POS_BYTE);
             return Bass.BASS_ChannelBytes2Seconds(this.masterStream.asInt(), bytePos);
         }
 
         return -1;
     }
 
     /**
      * Mutes the specified output.
      *
      * @param sd Output to be muted
      */
     public void muteOutput(SoundDevice sd) {
         if (this.outputs.containsKey(sd)) {
             this.outputs.get(sd).mute();
         }
     }
 
     /**
      * Unmutes the specified output.
      *
      * @param sd Output to be unmuted
      */
     public void unmuteOutput(SoundDevice sd) {
         if (this.outputs.containsKey(sd)) {
             this.outputs.get(sd).unmute(this.volume);
         }
     }
 
     /**
      * Returns current state of the output.
      * 
      * @param output Output which state to get
      * @return Output's state (muted or not)
      */
     public boolean isMuted(SoundDevice output) {
         if (!this.outputs.containsKey(output)) {
             return false;
         }
         
         return this.outputs.get(output).isMuted();
     }
 
     /**
      * Sets the out position for the audio.
      *
      * @param seconds New out position in seconds
      */
     public void setOutPosition(double seconds, AbstractCue cue) {
         if (this.masterStream != null) {
             if (this.stopSync != null) {
                 Bass.BASS_ChannelRemoveSync(this.masterStream.asInt(), this.stopSync);
                 this.stopSync = null;
             }
 
             long bytePos = Bass.BASS_ChannelSeconds2Bytes(this.masterStream.asInt(), seconds);
             this.stopSync = Bass.BASS_ChannelSetSync(this.masterStream.asInt(), BASS_SYNC.BASS_SYNC_POS, bytePos, new StopCallback(cue), null);
         }
     }
     
     public void setFadeOut(double startPos, ParameterEnvelope curve) {
         if (this.masterStream != null) {
             if (this.fadeOutSync != null) {
                 Bass.BASS_ChannelRemoveSync(this.masterStream.asInt(), this.fadeOutSync);
                 this.fadeOutSync = null;
             }
             
             long bytePos = Bass.BASS_ChannelSeconds2Bytes(this.masterStream.asInt(), startPos);
             this.fadeOutSync = Bass.BASS_ChannelSetSync(this.masterStream.asInt(), BASS_SYNC.BASS_SYNC_POS, bytePos, new FadeOutCallback(curve), null);
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
 
     public BASS_CHANNELINFO getStreamInfo() {
         return streamInfo;
     }
 
     
     /**
      * Returns floating point data of the audio.
      *
      * @return Stream data
      */
     private FloatBuffer getStreamData() {
         HSTREAM tmp = Bass.BASS_StreamCreateFile(false, this.filePath, 0, 0, BASS_STREAM.BASS_STREAM_DECODE | BASS_SAMPLE.BASS_SAMPLE_FLOAT);
         long dataLength = Bass.BASS_ChannelGetLength(tmp.asInt(), BASS_POS.BASS_POS_BYTE);
         int size = (int) (dataLength);
 
         ByteBuffer buffer = BufferUtils.newByteBuffer(size);
 
         Bass.BASS_ChannelGetData(tmp.asInt(), buffer, size);
 
         FloatBuffer streamData = buffer.asFloatBuffer();
         
         return streamData;
     }
 
     /**
      * Sets the output volume for a specific output device.
      *
      * @param deviceVolume New volume from 0 to 1
      * @param sd Output to be adjusted
      */
     public void setDeviceVolume(double deviceVolume, SoundDevice sd) {
         if (this.outputs.containsKey(sd)) {
             this.outputs.get(sd).setVolume(deviceVolume, this.volume);
         }
     }
     
     public void setDeviceVolumeDirect(double volume, SoundDevice sd) {
         if (this.outputs.containsKey(sd)) {
             this.outputs.get(sd).setVolumeDirect(this.volume * volume);
         }
     }
 
     /**
      * Sets the master volume for this audio.
      *
      * @param volume New volume from 0 to 1
      */
     public void setMasterVolume(double volume) {
         this.volume = volume;
 
         for (VirtualOutput vo : this.outputs.values()) {
             vo.updateVolume(volume);
         }
     }
     
     public void setMasterVolumeDirect(double volume) {
         for (VirtualOutput vo : this.outputs.values()) {
            vo.updateVolume(volume);
         }
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
         return this.outputs.get(sd).getVolume();
     }
 
     /**
      * Sets the panning for specific output.
      * 
      * @param pan New pan value from -1 (left) to 1 (right)
      * @param sd Output to be adjusted
      */
     public void setDevicePan(double pan, SoundDevice sd) {
         if (this.outputs.containsKey(sd)) {
             this.outputs.get(sd).setPan(pan);
         }
     }
     
     /**
      * Return the panning of a specific output.
      * 
      * @param sd Output which pan to get
      * @return Panning of the output
      */
     public double getDevicePan(SoundDevice sd) {
         return this.outputs.get(sd).getPan();
     }
     
     private void createWaveformImg() {
         FloatBuffer streamData = getStreamData();
         if (this.waveformImg == null) {
             int tmpW = Math.min(4000, streamData.capacity());
             this.waveformImg = new BufferedImage(tmpW, WaveformPanel.PREF_HEIGHT, BufferedImage.TYPE_INT_RGB);
         }
         
         Graphics cg = this.waveformImg.getGraphics();
         
         int width = this.waveformImg.getWidth();
         int height = this.waveformImg.getHeight();
 
         cg.setColor(WaveformPanel.backgroundColor);
         cg.fillRect(0, 0, width, height);
 
         if (streamData != null) {
             int step = (int) Math.ceil(streamData.capacity() / width);
 
             cg.setColor(WaveformPanel.waveformColor);
             for (int i = 0; i < width; i++) {
                 float maxValue = 0.0f;
 
                 for (int k = (i * step); k < (i * step + step); k++) {
                     if (streamData.get(k) > maxValue) {
                         maxValue = streamData.get(k);
                     }
                 }
 
                 cg.drawLine(i, height / 2, i, (int) ((height / 2) + (height / 2) * maxValue));
                 cg.drawLine(i, height / 2, i, (int) ((height / 2) - (height / 2) * maxValue));
             }
         }
     }
 
     public BufferedImage getWaveformImg() {
         return waveformImg;
     }
 }
