 package de.hotware.hotsound.audio.data;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
 import javax.sound.sampled.TargetDataLine;
 
 public class RecordAudioFile implements IAudioFile {
 
	protected Mixer mMixer;
 	protected AudioFormat mAudioFormat;
 	protected TargetDataLine mTargetDataLine;
 	protected boolean mStopped;
 
	public RecordAudioFile(Mixer pMixer, AudioFormat pAudioFormat) {
		this.mMixer = pMixer;
 		this.mAudioFormat = pAudioFormat;
 		this.mStopped = true;
 	}
 
 	@Override
 	public void open() throws AudioFileException {
 		if(!this.mStopped) {
 			throw new IllegalStateException("The AudioFile is already opened");
 		}
 		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
 				this.mAudioFormat);
 		try {
			this.mTargetDataLine = (TargetDataLine) this.mMixer.getLine(info);
 			this.mTargetDataLine.open(this.mAudioFormat);
 			this.mTargetDataLine.start();
 		} catch(LineUnavailableException e) {
 			throw new AudioFileException("Error during opening the TargetDataLine");
 		}
 		this.mStopped = false;
 	}
 
 	@Override
 	public AudioFormat getAudioFormat() {
 		return this.mAudioFormat;
 	}
 
 	@Override
 	public int read(byte[] pData, int pStart, int pLength) throws AudioFileException {
 		if(this.mStopped) {
 			throw new IllegalStateException("The AudioFile is not opened");
 		}
 		return this.mTargetDataLine.read(pData, pStart, pLength);
 	}
 
 	@Override
 	public void close() {
 		if(this.mStopped) {
 			throw new IllegalStateException("The AudioFile is not opened");
 		}
 		this.mTargetDataLine.close();
 		this.mStopped = true;
 	}
 
 }
