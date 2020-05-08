 package com.summ.sge.graphics.engine.software;
 
 import com.summ.sge.graphics.core.IRendererListener;
 
 public class SwingRenderer {
 
 	private static final String RENDERER_THREAD_NAME = "Renderer_thread";
 
 	private Thread mRenderThread;
 
 	private IRendererListener mListener;
 
 	private volatile boolean isRunning;
 
 	private class RendererRunnable implements Runnable {
 		@Override
 		public void run() {
 			System.out.println(Thread.currentThread().getName() + " start");
 
 			mListener.onInit();
 
 			while(!Thread.interrupted() && isRunning) {
 				mListener.onDraw();
 			}
 
 			System.out.println(Thread.currentThread().getName() + " destroy");
 			mListener.onDestroy();
 			System.out.println(Thread.currentThread().getName() + " end");
 		}
 	}
 
 	public SwingRenderer(IRendererListener listener) {
 		this(listener, false);
 	}
 
 	public SwingRenderer(IRendererListener listener, boolean useSeparateThread) {
 		mListener = listener;
 
 		if (useSeparateThread) mRenderThread = new Thread(new RendererRunnable(), RENDERER_THREAD_NAME);
 	}
 
 	public void start() {
 		isRunning = true;
 
 		if (mRenderThread == null) {
 			new RendererRunnable().run();
 		} else {
 			mRenderThread.start();
 		}
 	}
 
 	public void stop() {
 		isRunning = false;
 	}
 
 	public void drawFrame() {
 
 	}
 }
