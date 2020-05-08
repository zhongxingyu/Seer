 package de.hotware.hotsound.audio.player;
 
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
 
 import de.hotware.hotsound.audio.data.IAudioFile;
 import de.hotware.hotsound.audio.data.RecordAudioFile;
 
 
 public class RecordSong implements ISong {
 	
 	protected AudioFormat mAudioFormat;
	protected Mixer mMixer;
 	
	public RecordSong(Mixer pMixer, AudioFormat pAudioFormat) {
		this.mMixer = pMixer;
 		this.mAudioFormat = pAudioFormat;
 	}
 
 	@Override
 	public IAudioFile getAudioFile() throws IOException {
		return new RecordAudioFile(this.mMixer, this.mAudioFormat);
 	}
 
 }
