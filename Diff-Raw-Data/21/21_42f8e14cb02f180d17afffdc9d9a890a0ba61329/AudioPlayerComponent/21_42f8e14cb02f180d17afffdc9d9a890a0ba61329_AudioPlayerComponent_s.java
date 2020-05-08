 /*
  * Kurento Android MSControl: MSControl implementation for Android.
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.kas.mscontrol.mediacomponent.internal;
 
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.util.Log;
 
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.Joinable;
 import com.kurento.kas.media.tx.AudioInfoTx;
 import com.kurento.kas.mscontrol.join.AudioJoinableStreamImpl;
 
 public class AudioPlayerComponent extends MediaComponentBase {
 
 	private static final String LOG_TAG = "AudioPlayer";
 
 	private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
 	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
 	private AudioRecord audioRecord;
 	private short[] buffer;
 	private int frameSize;
 
 	private AudioCapture audioCapture;
 
 	@Override
 	public boolean isStarted() {
 		if (audioCapture == null)
 			return false;
 		return audioCapture.isPlaying();
 	}
 
 	public AudioPlayerComponent() throws MsControlException {
 	}
 
 	/**
 	 * 
 	 * @param minBufferSize
 	 * @param frameSizeEncode
 	 * @return the size, where: size % frameSizeEncode = 0 and size >=
 	 *         minBufferSize
 	 */
 	private int calculateBufferSize(int minBufferSize, int frameSizeEncode) {
 		int finalSize = frameSizeEncode;
 		while (finalSize < minBufferSize)
 			finalSize += frameSizeEncode;
 		return finalSize;
 	}
 
	// private void releaseAudioRecord() {
	// Log.d(LOG_TAG, "ReleaseAudio");
	// if (audioRecord != null) {
	// audioRecord.stop();
	// audioRecord.release();
	// audioRecord = null;
	// }
	// }
 
 	@Override
 	public void start() throws MsControlException {
 		AudioInfoTx audioInfo = null;
 		for (Joinable j : getJoinees(Direction.SEND))
 			if (j instanceof AudioJoinableStreamImpl) {
 				audioInfo = ((AudioJoinableStreamImpl) j).getAudioInfoTx();
 			}
 		if (audioInfo == null)
 			throw new MsControlException("Cannot get audio info.");
 
 		this.frameSize = audioInfo.getFrameSize();
 		int frequency = audioInfo.getAudioProfile().getSampleRate();
 
 		int minBufferSize = AudioRecord.getMinBufferSize(frequency,
 				channelConfiguration, audioEncoding);
 
 		int bufferSize = calculateBufferSize(minBufferSize, this.frameSize);
 
 		buffer = new short[bufferSize];
 		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
 				channelConfiguration, audioEncoding, bufferSize);
 
 		audioCapture = new AudioCapture();
 		audioCapture.start();
 	}
 
 	@Override
 	public synchronized void stop() {
 		if (audioCapture != null)
 			audioCapture.stopRecording();
 	}
 
 	private class AudioCapture extends Thread {
 
 		private boolean isPlaying = false;
 
 		public synchronized boolean isPlaying() {
 			return isPlaying;
 		}
 
 		private synchronized void setPlaying(boolean isPlaying) {
 			this.isPlaying = isPlaying;
 		}
 
 		public void stopRecording() {
 			setPlaying(false);
 		}
 
 		@Override
 		public void run() {
 			startRecording();
 		}
 
 		private int readFully(short[] audioData, int sizeInShorts) {
 			if (audioRecord == null)
 				return -1;
 
 			int shortsRead = 0;
 			int shortsLess = sizeInShorts;
 			while (shortsRead < sizeInShorts) {
 				int read = audioRecord.read(audioData, shortsRead, shortsLess);
 				shortsRead += read;
 				shortsLess -= read;
 			}
 			return shortsRead;
 		}
 
 		private void startRecording() {
 			if (audioRecord == null)
 				return;
 			audioRecord.startRecording();
 			setPlaying(true);
 			try {
 				while (isPlaying()) {
 					int bufferReadResult = readFully(buffer, frameSize);
 					for (Joinable j : getJoinees(Direction.SEND))
 						if (j instanceof AudioSink)
 							((AudioSink) j).putAudioSamples(buffer,
 									bufferReadResult);
 				}
				if (audioRecord != null)
					audioRecord.stop();
 			} catch (Throwable t) {
 				Log.e(LOG_TAG, "Recording error:" + t.toString());
 			}
 		}
 	}
 
 }
