 package com.example.projectbat;
 
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.util.Log;
 
 public class StreamingRecorder
 {
 	// supposedly supported by all devices
 	private final int sampleRate = 44100;
 	private AudioRecord record;
 	private int offset = 0;
 	private int bufferSize;
 	
 	StreamingRecorder()
 	{
 		bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
 				AudioFormat.ENCODING_PCM_16BIT);
		// Hold recording data for a minimum of 1/4 of a second
		if(sampleRate / 2 > bufferSize) bufferSize = sampleRate / 4;
 		
 		Log.i("StreamingRecorder", Integer.toString(bufferSize));
 		
 		try
 		{
 			record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
 					AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
 		}
 		catch(IllegalArgumentException e)
 		{
 			Log.e("StreamingRecorder", e.getMessage());
 		}
 	}
 	
 	public void start()
 	{
 		try { record.startRecording(); }
 		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
 	}
 	
 	public void stop()
 	{
 		try { record.stop(); }
 		catch(IllegalStateException e) { Log.e("StreamingRecorder", e.getMessage()); }
 	}
 	
	public final int get(short[] buffer, final int sizeInShorts)
 	{
 		final int shortsRead = record.read(buffer, offset, sizeInShorts);
 		offset += shortsRead;
 		offset %= bufferSize;
 		
 		return shortsRead;
 	}
 }
