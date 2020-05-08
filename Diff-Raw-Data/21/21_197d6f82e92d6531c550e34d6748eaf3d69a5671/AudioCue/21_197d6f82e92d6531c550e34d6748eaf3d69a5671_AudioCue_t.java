 package jcue.domain;
 
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import jcue.ui.AudioCueUI;
 
 /**
  * 
  *
  * @author Jaakko
  */
 public class AudioCue extends AbstractCue {
 
     private AudioStream audio;
     private double inPos, outPos;
     private double fadeIn, fadeOut;
     private CueState state;
     private double volume, pan;
     
     private ArrayList<SoundDevice> outputs;
     
     private static AudioCueUI ui = new AudioCueUI();
 
     public AudioCue(String name, String description, ArrayList<SoundDevice> outputs) {
         super(name, description, CueType.AUDIO);
         
         this.outputs = outputs;
 
         this.state = CueState.STOPPED;
         this.audio = new AudioStream(this.outputs);
         
         this.volume = 1.0;
         this.pan = 0.0;
         
         this.fadeIn = 0.0;
         this.fadeOut = 0.0;
     }
 
     public void loadAudio(String filePath) {
         if (filePath == null) {
             return;
         }
         
         try {
             this.audio.loadFile(filePath);
         } catch (Exception ex) {
             System.out.println(ex.getMessage());
         }
         
         this.inPos = 0;
         this.outPos = this.audio.getLength();
         
         ui.setOutFieldValue(this.outPos);
         
         if (this.getDescription().isEmpty()) {
             File audioFile = new File(filePath);
             String desc = audioFile.getName();
             
             this.setDescription(desc);
         }
     }
 
     @Override
     public void start(boolean delay) {
         super.start(delay);
         
         if (!delay || super.getStartDelay() == 0) {
             if (this.audio != null) {
                 //TODO: uncomment when multi-device shit works
                 //Set audio position to start if the cue is not paused
                 if (this.state == CueState.STOPPED) {
                     this.audio.setPosition(this.inPos);
                     this.audio.setMasterVolume(this.volume);
                 }
     //            
     //            //Fade in, if there is fade in time specified
     //            if (this.fadeIn != 0) {
     //                this.audio.setVolume(0);    //Start fading from silent
     //                this.audio.startVolumeChange(this.volume, this.fadeIn);
     //            }
 
                 this.audio.play();              //Start playing the audio
                 this.state = CueState.PLAYING;  //Set state to playing
             }
         }
     }
 
     @Override
     public void pause() {
         if (this.audio != null) {
             //TODO: pause fades when pausing audio
             this.audio.pause();
             this.state = CueState.PAUSED;
         }
     }
 
     @Override
     public void stop() {
         super.stop();
         
         if (this.audio != null) {
             this.audio.stop();
             this.audio.setPosition(this.inPos);
             this.state = CueState.STOPPED;
         }
     }
 
     //Getters and setters
     public double getFadeIn() {
         return fadeIn;
     }
 
     public double getFadeOut() {
         return fadeOut;
     }
 
     public double getInPos() {
         return inPos;
     }
 
     public double getOutPos() {
         return outPos;
     }
 
     public double getPan() {
         return pan;
     }
 
     public double getVolume() {
         return volume;
     }
     
     public double getDeviceVolume(SoundDevice sd) {
         return this.audio.getDeviceVolume(sd);
     }
 
     public CueState getState() {
         return state;
     }
 
     public AudioStream getAudio() {
         return audio;
     }
     
     public void setFadeIn(double fadeIn) {
         this.fadeIn = fadeIn;
     }
 
     public void setFadeOut(double fadeOut) {
         this.fadeOut = fadeOut;
     }
 
     public void setInPos(double inPos) {
         this.inPos = inPos;
     }
 
     public void setOutPos(double outPos) {
         this.outPos = outPos;
         this.audio.setOutPosition(outPos, this);
     }
 
     public void setPan(double pan) {
         this.pan = pan;
     }
 
     public void setVolume(double volume) {
         this.volume = volume;
         this.audio.setMasterVolume(volume);
     }
     
     public void setDeviceVolume(SoundDevice sd, double volume) {
         this.audio.setDeviceVolume(volume, sd);
     }
 
     public ArrayList<SoundDevice> getOutputs() {
         return outputs;
     }
     
     public void addOutput(SoundDevice sd) {
         if (!this.outputs.contains(sd)) {
             this.outputs.add(sd);
             this.audio.addOutput(sd);
         }
     }
     
     public boolean removeOutput(SoundDevice sd) {
         if (!this.outputs.contains(sd)) {
             return true;
         }
         
         if (this.outputs.size() == 1) {
             return false;
         }
         
         this.audio.removeOutput(sd);
         this.outputs.remove(sd);
         
         return true;
     }
     
     @Override
     public void updateUI(JPanel panel) {
         super.updateUI(panel);
         
        
         ui.showUI(panel);
        ui.setCurrentCue(this);
         
         ui.setFileFieldText(this.audio.getFilePath());
         ui.setLengthFieldValue(this.audio.getLength());
         
         ui.setInFieldValue(this.inPos);
         ui.setOutFieldValue(this.outPos);
         
         ui.setFadeInFieldValue(this.fadeIn);
         ui.setFadeOutFieldValue(this.fadeOut);
         
         ui.setVolumeControlValue(this.volume);
         ui.setPanControlValue(this.pan);
         
         ui.setWaveformData(this);
         
         AudioCueUI.lastPanel = panel;
     }
     
    @Override
     public void updateUI() {
         updateUI(AudioCueUI.lastPanel);
     }
 }
