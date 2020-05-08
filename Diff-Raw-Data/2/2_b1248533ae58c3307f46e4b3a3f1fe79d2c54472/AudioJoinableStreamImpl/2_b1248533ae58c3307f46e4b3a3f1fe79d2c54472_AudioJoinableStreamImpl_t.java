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
 
 public class AudioJoinableStreamImpl extends JoinableStreamBase implements
 		AudioSink, AudioRx {
 
 	public final static String LOG_TAG = "AudioJoinableStream";
 
 	private AudioInfoTx audioInfo;
 	private SessionSpec localSessionSpec;
 
 	private AudioRxThread audioRxThread = null;
 
 	public AudioInfoTx getAudioInfoTx() {
 		return audioInfo;
 	}
 
 	public AudioJoinableStreamImpl(JoinableContainer container,
 			StreamType type, SessionSpec remoteSessionSpec,
 			SessionSpec localSessionSpec) {
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
 			if ((Mode.SENDRECV.equals(audioMode) || Mode.SENDONLY
 					.equals(audioMode)) && audioProfile != null) {
 				AudioInfoTx audioInfo = new AudioInfoTx(audioProfile);
 				audioInfo.setOut(remoteRTPInfo.getAudioRTPDir());
 				audioInfo.setPayloadType(remoteRTPInfo.getAudioPayloadType());
 				audioInfo.setFrameSize(MediaTx.initAudio(audioInfo));
 				if (audioInfo.getFrameSize() < 0) {
					Log.e(LOG_TAG, "Error in initAudio");
 					MediaTx.finishAudio();
 					return;
 				}
 				this.audioInfo = audioInfo;
 			}
 
 			if ((Mode.SENDRECV.equals(audioMode) || Mode.RECVONLY
 					.equals(audioMode))) {
 				this.audioRxThread = new AudioRxThread(this);
 				this.audioRxThread.start();
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
 
 		public AudioRxThread(AudioRx audioRx) {
 			this.audioRx = audioRx;
 		}
 
 		@Override
 		public void run() {
 			Log.d(LOG_TAG, "startVideoRx");
 			if (!SpecTools.filterMediaByType(localSessionSpec, "audio")
 					.getMediaSpec().isEmpty()) {
 				String sdpAudio = SpecTools.filterMediaByType(localSessionSpec,
 						"audio").toString();
 				MediaRx.startAudioRx(sdpAudio, this.audioRx);
 			}
 		}
 	}
 
 }
