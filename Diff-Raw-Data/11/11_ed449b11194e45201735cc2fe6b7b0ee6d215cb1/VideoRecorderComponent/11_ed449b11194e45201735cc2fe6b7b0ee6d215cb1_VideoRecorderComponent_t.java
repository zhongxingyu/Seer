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
 
 package com.kurento.mscontrol.kas.mediacomponent.internal;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 
 import com.kurento.commons.config.Parameters;
 import com.kurento.kas.media.rx.RxPacket;
 import com.kurento.kas.media.rx.VideoFrame;
 import com.kurento.mscontrol.commons.MsControlException;
 import com.kurento.mscontrol.kas.mediacomponent.AndroidInfo;
 
 public class VideoRecorderComponent extends RecorderComponentBase implements
 		Recorder, VideoRecorder {
 
 	private static final String LOG_TAG = "VideoRecorderComponent";
 
 	private final SurfaceView videoSurfaceRx;
 	private final SurfaceHolder surfaceHolder;
 	private final ViewGroup surfaceContainer;
 
 	private RecorderController controller;
 
	private int screenWidth = 0;
	private int screenHeight = 0;
 	private int widthInfo = 0;
 	private int heightInfo = 0;
 	private SurfaceControl surfaceControl = null;
 
 	private final BlockingQueue<VideoFeeder> feedersQueue;
 
 	public View getVideoSurfaceRx() {
 		return videoSurfaceRx;
 	}
 
 	private synchronized int getWidthInfo() {
 		return widthInfo;
 	}
 
 	private synchronized void setWidthInfo(int widthInfo) {
 		this.widthInfo = widthInfo;
 	}
 
 	private synchronized int getHeightInfo() {
 		return heightInfo;
 	}
 
 	private synchronized void setHeightInfo(int heightInfo) {
 		this.heightInfo = heightInfo;
 	}
 
 	@Override
 	public boolean isStarted() {
 		return isRecording();
 	}
 
 	public VideoRecorderComponent(int maxDelay, boolean syncMediaStreams,
 			Parameters params) throws MsControlException {
 		super(maxDelay, syncMediaStreams);
 
 		if (params == null)
 			throw new MsControlException("Parameters are NULL");
 
 		surfaceContainer = params.get(VIEW_SURFACE_CONTAINER).getValue();
 		if (surfaceContainer == null)
 			throw new MsControlException(
 					"Params must have VideoRecorderComponent.VIEW_SURFACE param");
 		this.videoSurfaceRx = new SurfaceView(surfaceContainer.getContext());
 		videoSurfaceRx.setLayoutParams(new LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		surfaceContainer.addView(videoSurfaceRx);
 		surfaceHolder = videoSurfaceRx.getHolder();
 
 		this.packetsQueue = new LinkedBlockingQueue<RxPacket>();
 		this.feedersQueue = new LinkedBlockingQueue<VideoFeeder>();
 	}
 
 	@Override
 	public void putVideoFrame(VideoFrame videoFrame, VideoFeeder feeder) {
 		if (!isRecording()) {
 			if (feeder != null)
 				feeder.freeVideoFrameRx(videoFrame);
 			return;
 		}
 		long ptsNorm = calcPtsMillis(videoFrame);
 		setLastPtsNorm(ptsNorm);
 		caclEstimatedStartTime(ptsNorm, videoFrame.getRxTime());
 
 		packetsQueue.offer(videoFrame);
 		this.feedersQueue.offer(feeder);
 	}
 
 	@Override
 	public void start() {
 		if (videoSurfaceRx.isShown()) {
 			doStart();
 		}
 
 		surfaceHolder.addCallback(surfaceHolderCallback);
 	}
 
 	private void doStart() {
 		if (surfaceControl != null && surfaceControl.isInterrupted())
 			return;
 
		screenWidth = videoSurfaceRx.getWidth();
		screenHeight = videoSurfaceRx.getHeight();
 
 		surfaceControl = new SurfaceControl();
 		surfaceControl.start();
 		setRecording(true);
 		controller = getRecorderController();
 		controller.addRecorder(this);
 	}
 
 	@Override
 	public void stop() {
 		stopRecord();
 		if (controller != null)
 			controller.deleteRecorder(this);
 		if (surfaceControl != null)
 			surfaceControl.interrupt();
 
 		flushAll();
 	}
 
 	private class SurfaceControl extends Thread {
 		@Override
 		public void run() {
 			Bitmap srcBitmap = null;
 
 			try {
 				if (surfaceHolder == null) {
 					Log.e(LOG_TAG, "mSurfaceReceive is null");
 					return;
 				}
 
 				VideoFrame videoFrameProcessed;
 				int[] rgb;
 				int width, height, heightAux = 0, widthAux = 0;
 				int lastHeight = 0;
 				int lastWidth = 0;
 				double aux;
 
 				Canvas canvas = null;
 				Rect dirty = null;
 
 				for (;;) {
 					if (!isRecording()) {
 						synchronized (controll) {
 							controll.wait();
 						}
 						continue;
 					}
 
 					if (packetsQueue.isEmpty())
 						Log.v(LOG_TAG,
 								"Jitter buffer underflow: Video RX frames queue is empty");
 
 					long targetTime = getTargetTime();
 					if (targetTime != -1) {
 						long ptsMillis = calcPtsMillis(packetsQueue.peek());
 						if ((ptsMillis == -1)
 								|| (ptsMillis + getEstimatedStartTime() > targetTime)) {
 							synchronized (controll) {
 								controll.wait();
 							}
 							continue;
 						}
 					}
 
 					videoFrameProcessed = (VideoFrame) packetsQueue.take();
 
 					rgb = videoFrameProcessed.getDataFrame();
 					width = videoFrameProcessed.getWidth();
 					height = videoFrameProcessed.getHeight();
 
 					setWidthInfo(width);
 					setHeightInfo(height);
 
 					if (rgb == null || rgb.length == 0)
 						continue;
 
 					try {
 						canvas = surfaceHolder.lockCanvas(null);
 						if (canvas == null) {
 							surfaceHolder.unlockCanvasAndPost(canvas);
 							continue;
 						}
 
 						if (height != lastHeight) {
 							if (width != lastWidth || srcBitmap == null) {
 								if (srcBitmap != null)
 									srcBitmap.recycle();
 								try {
 									Log.d(LOG_TAG, "create bitmap");
 									srcBitmap = Bitmap.createBitmap(width,
 											height, Bitmap.Config.ARGB_8888);
 									Log.d(LOG_TAG, "create bitmap OK");
 								} catch (OutOfMemoryError e) {
 									e.printStackTrace();
 									Log.w(LOG_TAG,
 											"Can not create bitmap. No such memory.");
 									Log.w(LOG_TAG, e);
 									surfaceHolder.unlockCanvasAndPost(canvas);
 
 									VideoFeeder feeder = feedersQueue.poll();
 									if (feeder != null)
 										feeder.freeVideoFrameRx(videoFrameProcessed);
 
 									continue;
 								}
 								lastWidth = width;
 								if (srcBitmap == null)
 									Log.w(LOG_TAG, "srcBitmap is null");
 							}
 
 							float aspectScreen = (float) screenWidth
 									/ (float) screenHeight;
 							float aspectFrame = (float) width / (float) height;
 							if (aspectFrame > aspectScreen) {
 								aux = (double) screenWidth / (double) width;
 								heightAux = (int) (aux * height);
 								widthAux = screenWidth;
 							} else {
 								aux = (double) screenHeight / (double) height;
 								heightAux = screenHeight;
 								widthAux = (int) (aux * width);
 							}
 
 							int left = (videoSurfaceRx.getWidth() - widthAux) / 2;
 							int top = (videoSurfaceRx.getHeight() - heightAux) / 2;
 							dirty = new Rect(left, top, widthAux + left,
 									heightAux + top);
 
 							lastHeight = height;
 						}
 						if (srcBitmap != null) {
 							srcBitmap.setPixels(rgb, 0, width, 0, 0, width,
 									height);
 							canvas.drawBitmap(srcBitmap, null, dirty, null);
 						}
 						surfaceHolder.unlockCanvasAndPost(canvas);
 					} catch (IllegalArgumentException e) {
 						Log.e(LOG_TAG, "Exception: " + e.toString(), e);
 					}
 
 					VideoFeeder feeder = feedersQueue.poll();
 					if (feeder != null)
 						feeder.freeVideoFrameRx(videoFrameProcessed);
 				}
 			} catch (InterruptedException e) {
 				Log.d(LOG_TAG, "SurfaceControl stopped");
 				if (srcBitmap != null) {
 					srcBitmap.recycle();
 					srcBitmap = null;
 				}
 				System.gc();
 			}
 		}
 	}
 
 	@Override
 	public void flushAll() {
 		VideoFrame vf = (VideoFrame) packetsQueue.peek();
 		while (vf != null) {
 			VideoFeeder feeder = feedersQueue.poll();
 			if (feeder != null)
 				feeder.freeVideoFrameRx(vf);
 			packetsQueue.remove(vf);
 			vf = (VideoFrame) packetsQueue.peek();
 		}
 	}
 
 	@Override
 	public void flushTo(long time) {
 		VideoFrame vf = (VideoFrame) packetsQueue.peek();
 		while (vf != null) {
 			if ((calcPtsMillis(vf) + getEstimatedStartTime()) > time)
 				break;
 			VideoFeeder feeder = feedersQueue.poll();
 			if (feeder != null)
 				feeder.freeVideoFrameRx(vf);
 			packetsQueue.remove(vf);
 			vf = (VideoFrame) packetsQueue.peek();
 		}
 	}
 
 	@Override
 	public Object getInfo(AndroidInfo info) throws MsControlException {
 		if (AndroidInfo.FRAME_RX_WIDTH.equals(info)) {
 			return getWidthInfo();
 		} else if (AndroidInfo.FRAME_RX_HEIGHT.equals(info)) {
 			return getHeightInfo();
 		} else
 			return super.getInfo(info);
 	}
 
 	private final Callback surfaceHolderCallback = new Callback() {
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			Log.d(LOG_TAG, "Surface destroyed");
 			stop();
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder) {
 			Log.d(LOG_TAG, "Surface created");
 
 			doStart();
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 			Log.d(LOG_TAG, "Surface changed");
 		}
 	};
 
 }
