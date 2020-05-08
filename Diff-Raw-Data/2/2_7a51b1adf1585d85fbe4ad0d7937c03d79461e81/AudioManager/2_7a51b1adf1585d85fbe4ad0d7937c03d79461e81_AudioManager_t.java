 package com.soundrecorder.libraries;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.soundrecorder.ressources.FileFormats;
 
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.util.Log;
 
 public class AudioManager {
 	
 	private static final String LOG_TAG = "AudioManager";
 	private MediaRecorder recorder = null;
 	private boolean isRecording = false;
 	private boolean isSongLoaded = false;
 	private boolean isPlaying = false;
 	private boolean isPaused = false;
 	private boolean stereo = false;
 	private float recordVolume = 100;
 	private File audiofile = null;
 	private MediaPlayer Player = null;
 	private String tmpCurrentSong = null;
 	private String rootFolder;
 	
 	public boolean getStereo() {
 		return stereo;
 	}
 	
 	public void setStereo(boolean nstereo) {
 		stereo = nstereo;
 	}
 	
 	public boolean isSongLoad() {
 		return isSongLoaded;
 	}
 
 	public boolean isPlaying() {
 		return isPlaying;
 	}
 	
 	public boolean isPaused() {
 		return isPaused;
 	}
 	
 	public boolean isRecording() {
 		return isRecording;
 	}
 
 	public void setRecording(boolean isRecording) {
 		this.isRecording = isRecording;
 	}
 	
 	public int loadSong(String Path) {
 		Player = new MediaPlayer();
 		try {
 			Player.setDataSource(Path);
 			Player.prepare();
 			Log.e(LOG_TAG, "song loaded : " + Path);
 		}
 		catch (IOException e) {
             Log.e(LOG_TAG, "setDataSource() failed");
             return -1;
         }
 		isSongLoaded = true;
 		return 1;
 	}
 	
 	public void playSong() {
 		if (isSongLoad()) {
 			if (isPaused) 
 			{
 				Player.start();
 				isPaused = false;
 				isPlaying = true;
 			}
 			else
 			{
 				Player.start();	
 				isPlaying = true;
 			}
 		}
 	}
 	
 	public void pauseSong() {
 		if (isSongLoad()) {
 				Player.pause();
 				isPaused = true;
 		}
 		isPlaying = false;
 	}
 	
 	public int getCurrentTime() {
 		return Player.getCurrentPosition();
 	}
 	
 	public void setCurrentTime(int msec) {
 		Player.seekTo(msec);
 	}
 	
 	public int getDuration() {
 		return Player.getDuration();
 	}
 	
 	public void setRecordVolume(float volume) {
 		Player.setVolume(volume, volume);
 		recordVolume = volume;
 	}
 	
 	public float getRecordVolume() {
 		return recordVolume;
 	}
 	
 	public void stopSong() {
 		Player.release();
 		Player = null;
 		isPaused = false;
 		isPlaying = false;
 	}
 	
 	public void setRootFolder(String folder)
 	{
 		this.rootFolder = folder;
 	}
 	
 	public String getRootFolder()
 	{
 		return (rootFolder);
 	}
 	
 
 	public void recordCall(String OutputFilename, int bitrates, FileFormats format) {
 		if (isRecording == false) {
 		File dir = new File(rootFolder);
 		String formatsuf = null;
 		recorder = new MediaRecorder();
 		recorder.setAudioEncodingBitRate(bitrates);
 		if (stereo) {
 			recorder.setAudioChannels(2);
 		} else {
 			recorder.setAudioChannels(1);
 		}
 		recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
         switch (format) {
         	case GPP:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         		formatsuf = "3gpp";
         		break;
         	case AMR:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
         		formatsuf = "amr";
         		break;
         	case MP4:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         		formatsuf = "mp4";
         		break;
         	default:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         		formatsuf = "3gpp";
         		break;
         	}
         tmpCurrentSong = dir.getAbsolutePath() + "/" + OutputFilename + "." + formatsuf;
         audiofile = new File(tmpCurrentSong);
         recorder.setOutputFile(audiofile.getAbsolutePath());
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         try {
             recorder.prepare();
         } catch (IOException e) {
             Log.v(LOG_TAG, "prepare() failed");
             return;
         }
         recorder.start();
         setRecording(true);
 		}
 	}
 	
 	public void recordMic(String OutputFilename, int bitrates, FileFormats format) {
 		if (isRecording == false) {
 		File dir = new File(rootFolder);
 		String formatsuf = null;
 		recorder = new MediaRecorder();
 		recorder.setAudioEncodingBitRate(bitrates);
 		if (stereo) {
			recorder.setAudioChannels(2);
 		} else {
 			recorder.setAudioChannels(1);
 		}
 		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         switch (format) {
         	case GPP:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         		formatsuf = "3gpp";
         		break;
         	case AMR:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
         		formatsuf = "amr";
         		break;
         	case MP4:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         		formatsuf = "mp4";
         		break;
         	default:
         		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         		formatsuf = "3gpp";
         		break;
         	}
         tmpCurrentSong = dir.getAbsolutePath() + "/" + OutputFilename + "." + formatsuf;
         audiofile = new File(tmpCurrentSong);
         recorder.setOutputFile(audiofile.getAbsolutePath());
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         try {
             recorder.prepare();
         } catch (IOException e) {
             Log.v(LOG_TAG, "prepare() failed");
             return;
         }
         recorder.start();
         setRecording(true);
 		}
 	}
 	
 	public void stopRecording() {
 		recorder.stop();
 		recorder.release();
 		recorder = null;
 		setRecording(false);
 	}
 }
