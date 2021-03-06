 package org.servalproject.batphone;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Stack;
 
 import android.content.Context;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import android.os.Process;
 import android.util.Log;
 
 public class AudioPlayer implements Runnable {
 	static final int MIN_BUFFER = 20000000;
 	static final int SAMPLE_RATE = 8000;
 	static final int MIN_QUEUE_LEN = 200;
 
 	boolean playing = false;
 
 	private final Context context;
 	private int bufferSize;
 	private int audioFrameSize;
 	private int writtenAudioFrames;
 	private int playbackLatency;
 	private int lastSampleEnd;
 	Thread playbackThread;
 
 	// Add packets (primarily) to the the start of the list, play them from the
 	// end
 	// assuming that packet re-ordering is rare we shouldn't have to traverse
 	// the list very much to add a packet.
 	LinkedList<AudioBuffer> playList = new LinkedList<AudioBuffer>();
 	Stack<AudioBuffer> reuseList = new Stack<AudioBuffer>();
 
 	int lastQueuedSampleEnd = 0;
 
 	class AudioBuffer implements Comparable<AudioBuffer> {
 		byte buff[] = new byte[VoMP.MAX_AUDIO_BYTES];
 		int dataLen;
 		int sampleStart;
 		int sampleEnd;
 
 		@Override
 		public int compareTo(AudioBuffer arg0) {
 			if (0 < arg0.sampleStart - this.sampleStart)
 				return -1;
 			else if (this.sampleStart == arg0.sampleStart)
 				return 0;
 			return 1;
 		}
 	}
 
 	public AudioPlayer(Context context) {
 		this.context = context;
 	}
 
 	private void checkPlaybackRate(AudioTrack a, boolean before) {
 		int headFramePosition = a.getPlaybackHeadPosition();
 		playbackLatency = writtenAudioFrames - headFramePosition;
 
 		if (headFramePosition == writtenAudioFrames)
 			Log.v("VoMPCall", "Playback buffer empty!!");
 		else {
 			// Log.v("VoMPCall", "Playback buffer latency; " + playbackLatency
 			// + " ("
 			// + (playbackLatency / (double) SAMPLE_RATE) + ")");
 		}
 	}
 
 	public int receivedAudio(int local_session, int start_time,
 			int end_time,
 			int codec, DataInputStream in, int byteCount) throws IOException {
 
 		int ret = 0;
 
 		if (!playing) {
 			Log.v("VoMPCall", "Dropping audio as we are not currently playing");
 			return 0;
 		}
 
 		switch (codec) {
 		case VoMP.VOMP_CODEC_PCM: {
 
 			if (end_time == lastQueuedSampleEnd || end_time <= lastSampleEnd) {
 				// Log.v("VoMPCall", "Ignoring buffer");
 				return 0;
 			}
 			AudioBuffer buff;
 			if (reuseList.size() > 0)
 				buff = reuseList.pop();
 			else
 				buff = new AudioBuffer();
 			in.readFully(buff.buff, 0, byteCount);
 			ret = byteCount;
 			buff.dataLen = byteCount;
 			buff.sampleStart = start_time;
 			buff.sampleEnd = end_time;
 
 			synchronized (playList) {
 				if (playList.isEmpty()
 						|| buff.compareTo(playList.getFirst()) < 0) {
 
 					// add this buffer to play *now*
 					if (playList.isEmpty())
 						lastQueuedSampleEnd = end_time;
 
 					playList.addFirst(buff);
 					if (playbackThread != null)
 						playbackThread.interrupt();
 				} else if (buff.compareTo(playList.getLast()) > 0) {
 					// yay, packets arrived in order
 					lastQueuedSampleEnd = end_time;
 					playList.addLast(buff);
 				} else {
 					// find where to insert this item
 					ListIterator<AudioBuffer> i = playList.listIterator();
 					while (i.hasNext()) {
 						AudioBuffer compare = i.next();
 						switch (buff.compareTo(compare)) {
 						case -1:
 							i.previous();
 							i.add(buff);
 							return ret;
 						case 0:
 							reuseList.push(buff);
 							return ret;
 						}
 					}
 					reuseList.push(buff);
 				}
 			}
 
 			break;
 		}
 		}
 		return ret;
 	}
 
 	private void writeAudio(AudioTrack a, byte buff[], int len) {
 		int offset = 0;
 		while (offset < len) {
 			int ret = a.write(buff, offset, len - offset);
 			if (ret < 0)
 				break;
 			offset += ret;
 			writtenAudioFrames += ret / this.audioFrameSize;
 		}
 	}
 
 	public synchronized void startPlaying() {
 		if (playbackThread == null) {
 			playbackThread = new Thread(this, "Playback");
 			playing = true;
 			playbackThread.start();
 		}
 	}
 
 	public synchronized void stopPlaying() {
 		playing = false;
 		if (playbackThread != null)
 			playbackThread.interrupt();
 
 	}
 
 	@Override
 	public void run() {

 		AudioTrack a;
 		byte silence[];
 
 		synchronized (this) {
 			Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
 
 			bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
 					AudioFormat.CHANNEL_OUT_MONO,
 					AudioFormat.ENCODING_PCM_16BIT);
 
 			audioFrameSize = 2; // 16 bits per sample, with one channel
 			writtenAudioFrames = 0;
 
 			Log.v("VoMPCall",
 					"Minimum reported playback buffer size is "
 							+ bufferSize
 							+ " = "
 							+ (bufferSize / (double) (audioFrameSize * SAMPLE_RATE))
 							+ " seconds");
 
 			// ensure 60ms minimum playback buffer
 			if (bufferSize < 8 * 60 * audioFrameSize)
 				bufferSize = 8 * 60 * audioFrameSize;
 
 			Log.v("VoMPCall",
 					"Setting playback buffer size to "
 							+ bufferSize
 							+ " = "
 							+ (bufferSize / (double) (audioFrameSize * SAMPLE_RATE))
 							+ " seconds");
 
 			a = new AudioTrack(
 					AudioManager.STREAM_VOICE_CALL,
 					SAMPLE_RATE,
 					AudioFormat.CHANNEL_OUT_MONO,
 					AudioFormat.ENCODING_PCM_16BIT,
 					bufferSize, AudioTrack.MODE_STREAM);
 
			AudioManager am = (AudioManager) context
 					.getSystemService(Context.AUDIO_SERVICE);
 			am.setMode(AudioManager.MODE_IN_CALL);
 			am.setSpeakerphoneOn(false);
			am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
					am.getStreamMaxVolume
							(AudioManager.STREAM_VOICE_CALL), 0);

 			a.play();
 			silence = new byte[bufferSize];
			// fill the audio buffer once.
 			writeAudio(a, silence, bufferSize);
 		}
 		lastSampleEnd = 0;
 		StringBuilder sb = new StringBuilder();
 
 		int smallestQueue = 0;
 		int largestQueue = 0;
 		while (playing) {
 
 			if (sb.length() >= 128) {
 				Log.v("VoMPCall",
 						smallestQueue + " " + largestQueue + " "
 								+ sb.toString());
 				sb.setLength(0);
 			}
 
 			AudioBuffer buff = null;
 			long now = 0;
 			int generateSilence = 0;
 			long audioRunsOutAt;
 
 			synchronized (playList) {
 				if (!playList.isEmpty())
 					buff = playList.getFirst();
 
 				now = System.nanoTime();
 				playbackLatency = writtenAudioFrames
 						- a.getPlaybackHeadPosition();
 
 				// work out when we must make a decision about playing some
 				// extra silence
 				audioRunsOutAt = now - MIN_BUFFER
 						+ (long) (playbackLatency * 1000000.0 / SAMPLE_RATE);
 
 				// calculate an absolute maximum delay based on our maximum
 				// extra latency
 				int queuedLengthInMs = lastQueuedSampleEnd - lastSampleEnd;
 				if (queuedLengthInMs < smallestQueue)
 					smallestQueue = queuedLengthInMs;
 
 				if (queuedLengthInMs > largestQueue)
 					largestQueue = queuedLengthInMs;
 
 				if (buff != null) {
 					int silenceGap = buff.sampleStart - (lastSampleEnd + 1);
 
 					if (silenceGap > 0) {
 
 						// try to wait until the last possible moment before
 						// giving up and playing the buffer we have
 						if (audioRunsOutAt <= now) {
 							sb.append("M");
 							generateSilence = silenceGap;
 							lastSampleEnd = buff.sampleStart - 1;
 						}
 						buff = null;
 					} else {
 						// we either need to play it or skip it, so remove it
 						// from the queue
 						playList.removeFirst();
 
 						if (silenceGap < 0) {
 							// sample arrived too late, we might get better
 							// audio if we add a little extra latency
 							reuseList.push(buff);
 							sb.append("L");
 							continue;
 						}
 
 						if (smallestQueue > MIN_QUEUE_LEN) {
 							// if we don't need the buffer, drop some audio
 							// but count it as played so we
 							// don't immediately play silence or try to wait for
 							// this "missing" audio packet to arrive
 
 							// TODO shrink each buffer instead of dropping a
 							// whole one?
 
 							sb.append("F");
 							smallestQueue -= (buff.sampleEnd + 1 - buff.sampleStart);
 							lastSampleEnd = buff.sampleEnd;
 							reuseList.push(buff);
 							continue;
 						}
 					}
 				} else {
 					// this thread can sleep for a while to wait for more audio
 
 					// But if we've got nothing else to play, we should play
 					// some silence to increase our latency buffer
 					if (audioRunsOutAt <= now) {
 						sb.append("X");
 						generateSilence = 20;
 					}
 
 				}
 			}
 
 			if (generateSilence > 0) {
 				// write some audio silence, then check the packet queue again
 				// (8 samples per millisecond, 2 bytes per sample)
 				int silenceDataLength = generateSilence * 16;
 				smallestQueue++;
 				largestQueue -= 5;
 				sb.append("{" + generateSilence + "}");
 				while (silenceDataLength > 0) {
 					int len = silenceDataLength > silence.length ? silence.length
 							: silenceDataLength;
 					writeAudio(a, silence, len);
 					silenceDataLength -= len;
 				}
 				continue;
 			}
 
 			if (buff != null) {
 				// write the audio sample, then check the packet queue again
 				lastSampleEnd = buff.sampleEnd;
 				writeAudio(a, buff.buff, buff.dataLen);
 				smallestQueue++;
 				largestQueue -= 5;
 				sb.append(".");
 				synchronized (playList) {
 					reuseList.push(buff);
 				}
 				continue;
 			}
 
 			// check the clock again, then wait only until our audio buffer is
 			// getting close to empty
 			now = System.nanoTime();
 			long waitFor = audioRunsOutAt - now;
 			if (waitFor <= 0)
 				continue;
 			sb.append(" ");
 			long waitMs = waitFor / 1000000;
 			int waitNs = (int) (waitFor - waitMs * 1000000);
 
 			try {
 				Thread.sleep(waitMs, waitNs);
 			} catch (InterruptedException e) {
 			}
 		}
 		a.stop();
 		a.release();
 		playList.clear();
 		reuseList.clear();
 		playbackThread = null;
 	}
 
 }
