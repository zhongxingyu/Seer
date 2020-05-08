 package de.hotware.hotsound.audio.player;
 
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 
 import de.hotware.hotsound.audio.data.IAudioFile;
 import de.hotware.hotsound.audio.data.RecordAudioFile;
 
 
 public class RecordSong implements ISong {
 	
 	protected AudioFormat mAudioFormat;
 	
	public RecordSong(AudioFormat pAudioFormat) {
 		this.mAudioFormat = pAudioFormat;
 	}
 
 	@Override
 	public IAudioFile getAudioFile() throws IOException {
		return new RecordAudioFile(this.mAudioFormat);
 	}
 
 }
