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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.Semaphore;
 
 import javax.sdp.SdpException;
 
 import android.util.Log;
 
 import com.kurento.commons.media.format.SessionSpec;
 import com.kurento.commons.media.format.conversor.SdpConversor;
 import com.kurento.commons.media.format.enums.MediaType;
 import com.kurento.commons.media.format.enums.Mode;
 import com.kurento.commons.mscontrol.MsControlException;
 import com.kurento.commons.mscontrol.join.Joinable;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.kas.media.codecs.VideoCodecType;
 import com.kurento.kas.media.profiles.VideoProfile;
 import com.kurento.kas.media.rx.MediaRx;
 import com.kurento.kas.media.rx.VideoFrame;
 import com.kurento.kas.media.rx.VideoRx;
 import com.kurento.kas.media.tx.MediaTx;
 import com.kurento.kas.media.tx.VideoFrameTx;
 import com.kurento.kas.media.tx.VideoInfoTx;
 import com.kurento.kas.mscontrol.mediacomponent.internal.VideoFeeder;
 import com.kurento.kas.mscontrol.mediacomponent.internal.VideoRecorder;
 import com.kurento.kas.mscontrol.mediacomponent.internal.VideoSink;
 import com.kurento.kas.mscontrol.networkconnection.internal.RTPInfo;
 
 public class VideoJoinableStreamImpl extends JoinableStreamBase implements
 		VideoSink, VideoRx, VideoFeeder {
 
 	public final static String LOG_TAG = "VideoJoinableStream";
 
 	private VideoProfile videoProfile = null;
 	private SessionSpec localSessionSpec;
 
 	private VideoTxThread videoTxThread = null;
 	private VideoRxThread videoRxThread = null;
 
 	private Semaphore txFinished = new Semaphore(0);
 
 	private int QUEUE_SIZE = 2;
 	private BlockingQueue<VideoFrameTx> framesQueue;
 
 	private long timeFirstFrame;
 
 	private Set<int[]> freeFrames;
 	private Map<int[], Integer> usedFrames;
 
 	public VideoProfile getVideoProfile() {
 		return videoProfile;
 	}
 
 	public VideoJoinableStreamImpl(JoinableContainer container,
 			StreamType type, ArrayList<VideoProfile> videoProfiles,
 			SessionSpec remoteSessionSpec, SessionSpec localSessionSpec,
 			Integer maxDelayRx, Integer framesQueueSize) {
 		super(container, type);
 		this.localSessionSpec = localSessionSpec;
 		if (framesQueueSize != null && framesQueueSize > QUEUE_SIZE)
 			QUEUE_SIZE = framesQueueSize;
 		Log.d(LOG_TAG, "QUEUE_SIZE: " + QUEUE_SIZE);
 
 		framesQueue = new ArrayBlockingQueue<VideoFrameTx>(QUEUE_SIZE);
 
 		Map<MediaType, Mode> mediaTypesModes = getModesOfMediaTypes(localSessionSpec);
 		Mode videoMode = mediaTypesModes.get(MediaType.VIDEO);
 		RTPInfo remoteRTPInfo = new RTPInfo(remoteSessionSpec);
 
 		if (videoMode != null) {
 			VideoCodecType videoCodecType = remoteRTPInfo.getVideoCodecType();
 			VideoProfile videoProfile = getVideoProfileFromVideoCodecType(
 					videoProfiles, videoCodecType);
 			if (remoteRTPInfo.getFrameWidth() > 0
 					&& remoteRTPInfo.getFrameHeight() > 0) {
 				videoProfile.setWidth(remoteRTPInfo.getFrameWidth());
 				videoProfile.setHeight(remoteRTPInfo.getFrameHeight());
 			}
 			if (remoteRTPInfo.getFrameRate() != null) {
 				videoProfile.setFrameRateNum(remoteRTPInfo.getFrameRate()
 						.getNumerator());
 				videoProfile.setFrameRateDen(remoteRTPInfo.getFrameRate()
 						.getDenominator());
 			}
 
 			if ((Mode.SENDRECV.equals(videoMode) || Mode.SENDONLY
 					.equals(videoMode)) && videoProfile != null) {
 				if (remoteRTPInfo.getVideoBandwidth() > 0)
 					videoProfile.setBitRate(remoteRTPInfo.getVideoBandwidth()*1000);
 				VideoInfoTx videoInfo = new VideoInfoTx(videoProfile);
 				videoInfo.setOut(remoteRTPInfo.getVideoRTPDir());
 				videoInfo.setPayloadType(remoteRTPInfo.getVideoPayloadType());
 				int ret = MediaTx.initVideo(videoInfo);
 				if (ret < 0) {
 					Log.e(LOG_TAG, "Error in initVideo");
 					MediaTx.finishVideo();
 				}
 				this.videoProfile = videoProfile;
 				this.videoTxThread = new VideoTxThread();
 				this.videoTxThread.start();
 			}
 
 			if ((Mode.SENDRECV.equals(videoMode) || Mode.RECVONLY
 					.equals(videoMode))) {
 				this.videoRxThread = new VideoRxThread(this, maxDelayRx);
 				this.videoRxThread.start();
 			}
 		}
 
 		this.timeFirstFrame = -1;
 		this.freeFrames = new CopyOnWriteArraySet<int[]>();
 		this.usedFrames = new HashMap<int[], Integer>();
 	}
 
 	@Override
 	public VideoFrameTx putVideoFrame(byte[] data, int width, int height,
 			long time) {
 		if (timeFirstFrame == -1)
 			timeFirstFrame = time;
 		if (framesQueue.size() >= QUEUE_SIZE) {
 			Log.w(LOG_TAG, "Buffer overflow: Video frames queue is full");
 			framesQueue.poll();
 		}
 		VideoFrameTx vf = new VideoFrameTx(data, width, height, time
 				- timeFirstFrame);
 		framesQueue.offer(vf);
 
 		return vf;
 	}
 
 	@Override
 	public synchronized void putVideoFrameRx(VideoFrame videoFrame) {
 		int n = 1;
 		try {
 			for (Joinable j : getJoinees(Direction.SEND))
 				if (j instanceof VideoRecorder) {
 					usedFrames.put(videoFrame.getDataFrame(), n++);
 					((VideoRecorder) j).putVideoFrame(videoFrame, this);
 				}
 		} catch (MsControlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void freeVideoFrameRx(VideoFrame videoFrame) {
 		Integer count = usedFrames.get(videoFrame.getDataFrame());
 		if (count == null)
 			return;
 		if (--count == 0) {
 			usedFrames.remove(videoFrame.getDataFrame());
 			freeFrames.add(videoFrame.getDataFrame());
 		} else
 			usedFrames.put(videoFrame.getDataFrame(), count);
 	}
 
 	// TODO: improve memory (video frame buffers) management.
 	private int[] createFrameBuffer(int length) {
 		int[] buffer = null;
 
		long size = length * Integer.SIZE / 8;
 		long freeMemory = Runtime.getRuntime().freeMemory();
 		long maxMemory = Runtime.getRuntime().maxMemory();
 		long nextTotalMemory = Runtime.getRuntime().totalMemory() + size;
 
 		try {
 			if ((freeMemory >= size) || (nextTotalMemory <= maxMemory))
 				buffer = new int[length];
 		} catch (OutOfMemoryError e) {
 			e.printStackTrace();
 			Log.w(LOG_TAG, e);
 			buffer = null;
 		}
 
 		if (buffer == null)
 			Log.w(LOG_TAG, "Can not create frame buffer. No such memory.");
 
 		return buffer;
 	}
 
 	@Override
 	public synchronized int[] getFrameBuffer(int size) {
 //		Log.d(LOG_TAG, "freeFrames.size(): " + freeFrames.size()
 //				+ " usedFrames.size(): " + usedFrames.size());
 //
 //		Log.d(LOG_TAG, "freeMemory: " + Runtime.getRuntime().freeMemory()/1024
 //				+ "KB maxMemory: " + Runtime.getRuntime().maxMemory()/1024
 //				+ "KB totalMemory: " + Runtime.getRuntime().totalMemory()/1024 + "KB");
 
 		if (size % (Integer.SIZE / 8) != 0) {
 			Log.w(LOG_TAG, "Size must be multiple of " + (Integer.SIZE / 8));
 			return null;
 			// throw new IllegalArgumentException("Size must be multiple of "
 			// + (Integer.SIZE / 8));
 		}
 
 		int l = size / (Integer.SIZE / 8);
 		if (freeFrames.isEmpty())
 			return createFrameBuffer(l);
 
 		for (int[] b : freeFrames) {
 			freeFrames.remove(b);
 			if (b.length >= l)
 				return b;
 		}
 
 		return createFrameBuffer(l);
 	}
 
 	public void stop() {
 		if (videoTxThread != null) {
 			videoTxThread.interrupt();
 			try {
 				txFinished.acquire();
 			} catch (InterruptedException e) {
 				Log.e(LOG_TAG, "Error while waiting to complete test", e);
 			}
 		}
 
 		Log.d(LOG_TAG, "finishVideo");
 		MediaTx.finishVideo();
 		Log.d(LOG_TAG, "stopVideoRx");
 		MediaRx.stopVideoRx();
 	}
 
 	private class VideoTxThread extends Thread {
 		private static final int ALPHA = 7;
 //		private static final int BETA = 10;
 
 		private long caclFrameTimeLuis(long frameTime, long it, long lastFrameTime) {
 			long currentFrameTime;
 			if (it > ALPHA)
 				currentFrameTime = (ALPHA * frameTime + lastFrameTime)
 						/ (ALPHA + 1);
 			else
 				currentFrameTime = (it * frameTime + lastFrameTime) / (it + 1);
 			return currentFrameTime;
 		}
 
 		@Override
 		public void run() {
 			int tr = 1000 / (videoProfile.getFrameRateNum() / videoProfile
 					.getFrameRateDen());
 			VideoFrameTx frameProcessed;
 
 			long tFrame = tr;
 			long t, lastT, s;
 			long h = tFrame;
 			long n = 0;
 			lastT = -1;
 
 			long nextFrame = 0;
 
 			long tFirstFrame = 0;
 			long tCurrentFrame;
 			long tInit = System.currentTimeMillis();
 			long timePts;
 			long tTotal = 0;
 
 			System.out.println("tr: " + tr); //
 
 			try {
 				for (;;) {
 					if (lastT > 0) {
 						t = System.currentTimeMillis();
 						nextFrame = (ALPHA + 1) * tr - ALPHA * h;
 						s = nextFrame - (t - lastT);
 					} else
 						s = -1;
 
 					if (s > 0)
 						sleep(s);
 					t = System.currentTimeMillis();
 					if (lastT > 0)
 						tFrame = t - lastT;
 
 					h = caclFrameTimeLuis(h, n, tFrame);
 
 					if (framesQueue.isEmpty())
 						Log.w(LOG_TAG,
 								"Buffer underflow: Video frames queue is empty");
 					frameProcessed = framesQueue.take();
 					tCurrentFrame = System.currentTimeMillis();
 
 					if (n == 0) {
 						tInit = System.currentTimeMillis();
 						tFirstFrame = tCurrentFrame;
 					}
 					timePts = tCurrentFrame - tFirstFrame;
 					frameProcessed.setTime(timePts);
 
 					MediaTx.putVideoFrame(frameProcessed);
 
 					lastT = t;
 					n++;
 				}
 			} catch (InterruptedException e) {
 				Log.d(LOG_TAG, "VideoTxThread stopped");
 				long tFinish = System.currentTimeMillis();
 				Log.i(LOG_TAG, "time total: " + (tFinish - tInit)
 						+ " n frames: " + (n + 1) + " Average fr: "
 						+ ((1000.0 * (n + 1)) / (tFinish - tInit)) + "fps"
 						+ " Average encode time: " + (tTotal / (n + 1)));
 				txFinished.release();
 			}
 
 		}
 	}
 
 	private class VideoRxThread extends Thread {
 		private VideoRx videoRx;
 		private int maxDelayRx;
 
 		public VideoRxThread(VideoRx videoRx, int maxDelayRx) {
 			this.videoRx = videoRx;
 			this.maxDelayRx = maxDelayRx;
 			Log.d(LOG_TAG, "maxDelayRx: " + maxDelayRx);
 		}
 
 		@Override
 		public void run() {
 			Log.d(LOG_TAG, "startVideoRx");
 			SessionSpec s = filterMediaByType(localSessionSpec, MediaType.VIDEO);
 			if (!s.getMediaSpecs().isEmpty()) {
 				try {
 					String sdpVideo = SdpConversor.sessionSpec2Sdp(s);
 					MediaRx.startVideoRx(sdpVideo, maxDelayRx, this.videoRx);
 				} catch (SdpException e) {
 					Log.e(LOG_TAG, "Could not start video rx " + e.toString());
 				}
 			}
 		}
 	}
 
 	private VideoProfile getVideoProfileFromVideoCodecType(
 			ArrayList<VideoProfile> videoProfiles, VideoCodecType videoCodecType) {
 		if (videoCodecType == null)
 			return null;
 		for (VideoProfile vp : videoProfiles)
 			if (videoCodecType.equals(vp.getVideoCodecType()))
 				return vp;
 		return null;
 	}
 
 }
