 /*
  * Kwaak3
  * Copyright (C) 2010 Roderick Colenbrander
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package org.kwaak3;
 
import java.nio.ByteBuffer;

 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.util.Log;
 
 public class KwaakAudio {
 	private KwaakAudioTrack mAudioTrack;
 	private int bytesWritten=0;
	byte[] mAudioData = new byte[8192];
 	
 	public int getPos()
 	{
 		return 0;
 	}
 	
 	public void initAudio()
 	{
 		if(mAudioTrack != null) return;
 		
 		int sampleFreq = 44100; //AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
 		int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
 		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
 		int bufferSize = 2*AudioTrack.getMinBufferSize(sampleFreq, channelConfig, audioFormat);
 		
 		mAudioTrack = new KwaakAudioTrack(AudioManager.STREAM_MUSIC,
 				sampleFreq,
 				channelConfig,
 				audioFormat,
 				bufferSize,
 				AudioTrack.MODE_STREAM);
 		
 		mAudioTrack.play();
 		mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener()
 		{
 			@Override
 			public void onMarkerReached(AudioTrack track) {
 			}
 
 			@Override
 			public void onPeriodicNotification(AudioTrack track) {
 				/* We pull audio from Quake3 when we can use it */
 				KwaakJNI.requestAudioData();
 			} });
 		mAudioTrack.setPositionNotificationPeriod(2048);
 	}
 
	public void writeAudio(ByteBuffer audioData, int offset, int len)
 	{
 		if(mAudioTrack == null)
 			return;
	
		/* The Quake3 audio buffer is shared with the Java side. Set the audioData to the right position
		 * and read a chunk of size len to a normal byte array */
		audioData.position(offset);
		audioData.get(mAudioData, 0, len);
		bytesWritten += mAudioTrack.write(mAudioData, 0, len);
 	}
 	
 	public void pause()
 	{
 		if(mAudioTrack == null)
 			return;
 		
 		mAudioTrack.pause();
 	}
 	
 	public void resume()
 	{
 		if(mAudioTrack == null)
 			return;
 		
 		mAudioTrack.play();
 	}
 }
 
 class KwaakAudioTrack extends AudioTrack
 {
 	private int mFrameSize;
 
 	public KwaakAudioTrack(int streamType, int sampleRateInHz,
 			int channelConfig, int audioFormat, int bufferSizeInBytes, int mode)
 			throws IllegalArgumentException {
 		super(streamType, sampleRateInHz, channelConfig, audioFormat,
 				bufferSizeInBytes, mode);
 		
 		if(audioFormat == AudioFormat.ENCODING_PCM_16BIT)
 			mFrameSize = 2*this.getChannelCount();
 		else
 			mFrameSize = this.getChannelCount();
 	}
 	
 	@Override
 	public void play() throws IllegalStateException {
 		super.play();
 
 		/* Audio starts playing after the buffer has been filled once.
 		 * Manually fill the buffer to start requesting Audio samples from
 		 * the Quake3 engine. */
 		initBuffer();
 	}
 
	public void initBuffer()
 	{
 		byte[] audioData = new byte[getNativeFrameCount()*mFrameSize];
 		write(audioData, 0, audioData.length);
 	}
 	
 	public int getFrameSize()
 	{
 		return mFrameSize;
 	}
 }
