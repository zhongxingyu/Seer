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
 
 package com.kurento.kas.mscontrol.join;
 
 import java.util.Map;
 
 import android.util.Log;
 
 import com.kurento.commons.media.format.SessionSpec;
 import com.kurento.commons.media.format.SpecTools;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.Joinable;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.commons.sdp.enums.MediaType;
 import com.kurento.commons.sdp.enums.Mode;
 import com.kurento.kas.media.codecs.AudioCodecType;
 import com.kurento.kas.media.profiles.AudioProfile;
 import com.kurento.kas.media.rx.AudioRx;
 import com.kurento.kas.media.rx.MediaRx;
 import com.kurento.kas.media.tx.AudioInfoTx;
 import com.kurento.kas.media.tx.MediaTx;
 import com.kurento.kas.mscontrol.mediacomponent.internal.AudioSink;
 import com.kurento.kas.mscontrol.networkconnection.internal.RTPInfo;
 
 public class AudioJoinableStreamImpl extends JoinableStreamBase implements AudioSink, AudioRx {
 
 	public final static String LOG_TAG = "AudioJoinableStream";
 
 	private AudioInfoTx audioInfo;
 	private SessionSpec localSessionSpec;
 
 	private AudioRxThread audioRxThread = null;
 
 	public AudioInfoTx getAudioInfoTx() {
 		return audioInfo;
 	}
 
 	public AudioJoinableStreamImpl(JoinableContainer container,
 			StreamType type, SessionSpec remoteSessionSpec,
 			SessionSpec localSessionSpec, Integer maxDelayRx) {
 		super(container, type);
 		this.localSessionSpec = localSessionSpec;
 
 		Map<MediaType, Mode> mediaTypesModes = SpecTools
 				.getModesOfFirstMediaTypes(localSessionSpec);
 		Mode audioMode = mediaTypesModes.get(MediaType.AUDIO);
 		RTPInfo remoteRTPInfo = new RTPInfo(remoteSessionSpec);
 
 		if (audioMode != null) {
 			AudioCodecType audioCodecType = remoteRTPInfo.getAudioCodecType();
 			AudioProfile audioProfile = AudioProfile
 					.getAudioProfileFromAudioCodecType(audioCodecType);
 
 			if (audioProfile != null) {
 				this.audioInfo = new AudioInfoTx(audioProfile);
 				audioInfo.setOut(remoteRTPInfo.getAudioRTPDir());
 				audioInfo.setPayloadType(remoteRTPInfo.getAudioPayloadType());
 
 				if (Mode.SENDRECV.equals(audioMode) || Mode.SENDONLY.equals(audioMode)) {
 					audioInfo.setFrameSize(MediaTx.initAudio(audioInfo));
 					if (audioInfo.getFrameSize() < 0) {
 						Log.e(LOG_TAG, "Error in initAudio");
 						MediaTx.finishAudio();
 						return;
 					}
 				}
 
 				if ((Mode.SENDRECV.equals(audioMode) || Mode.RECVONLY.equals(audioMode))) {
 					this.audioRxThread = new AudioRxThread(this, maxDelayRx);
 					this.audioRxThread.start();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void putAudioSamples(short[] in_buffer, int in_size) {
 		MediaTx.putAudioSamples(in_buffer, in_size);
 	}
 
 	@Override
 	public void putAudioSamplesRx(byte[] audio, int length) {
 		try {
 			for (Joinable j : getJoinees(Direction.SEND))
 				if (j instanceof AudioRx)
 					((AudioRx) j).putAudioSamplesRx(audio, length);
 		} catch (MsControlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void stop() {
 		Log.d(LOG_TAG, "finishAudio");
 		MediaTx.finishAudio();
 		Log.d(LOG_TAG, "stopAudioRx");
 		MediaRx.stopAudioRx();
 	}
 
 	private class AudioRxThread extends Thread {
 		private AudioRx audioRx;
 		private int maxDelayRx;
 
 		public AudioRxThread(AudioRx audioRx, int maxDelayRx) {
 			this.audioRx = audioRx;
 			this.maxDelayRx = maxDelayRx;
 		}
 
 		@Override
 		public void run() {
			Log.d(LOG_TAG, "startVideoRx");
 			if (!SpecTools.filterMediaByType(localSessionSpec, "audio").getMediaSpec().isEmpty()) {
 				String sdpAudio = SpecTools.filterMediaByType(localSessionSpec, "audio").toString();
 				MediaRx.startAudioRx(sdpAudio, maxDelayRx, this.audioRx);
 			}
 		}
 	}
 
 }
