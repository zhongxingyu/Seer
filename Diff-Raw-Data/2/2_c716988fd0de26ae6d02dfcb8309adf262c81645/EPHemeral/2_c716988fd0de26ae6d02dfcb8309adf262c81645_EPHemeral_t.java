 /*
  * Copyright 2013, Sebastian Kreisel. All rights reserved.
  * If you intend to use, modify or redistribute this file contact kreisel.sebastian@gmail.com
  */
 
 package com.elfeck.ephemeral;
 
 import java.util.List;
 
 import com.elfeck.ephemeral.glContext.EPHRenderContext;
 import com.elfeck.ephemeral.glContext.EPHVertexArrayObject;
 
 
 public class EPHemeral {
 
 	public static final String VERSION = "0.00.1";
 
 	private int width, height;
 	private EPHSurface surface;
 	private EPHRenderContext renderContext;
 	private EPHRunnableContext renderJob, logicJob;
 	private Thread renderThread, logicThread;
 
 	public EPHemeral(int width, int height, int fpsCap, int lpsCap, String shaderParentPath, String title) {
 		this.width = width;
 		this.height = height;
 		renderContext = EPHRenderContext.createRenderContext(this, Math.min(1000, Math.max(1, fpsCap)), shaderParentPath, title);
 		renderThread = new Thread(renderJob = new EPHRenderJob(this, Math.max(1, (int) Math.round((1.0 / fpsCap) * 1000 - 5))), "RenderThread");
 		logicThread = new Thread(logicJob = new EPHLogicJob(this, Math.max(1, (int) Math.round((1.0 / lpsCap) * 1000))), "LogicThread");
 		renderThread.setPriority(Thread.MAX_PRIORITY);
 		logicThread.setPriority(Thread.MIN_PRIORITY);
 		surface = null;
 		start();
 	}
 	public EPHemeral(int width, int height, String title) {
 		this(width, height, 60, 1000, "shader/", title);
 	}
 
 	private void start() {
 		renderThread.start();
 		logicThread.start();
 	}
 
 	protected synchronized void reqLogic(long delta) {
 		if (surface != null) surface.execLogic(delta);
 	}
 
 	protected synchronized void reqRender() {
 		renderContext.glRender();
 		if (renderContext.wasResized()) {
 			width = renderContext.getWidth();
 			height = renderContext.getHeight();
 		}
 	}
 
 	public void updateVaos() {
		if (surface != null) surface.updateVaos();
 	}
 
 	public void glDestroyVaos() {
 		surface.destroyVaos();
 		EPHVertexArrayObject.glDisposeShaderPrograms();
 	}
 
 	public synchronized void destroy() {
 		renderContext.glDestroy();
 		renderJob.destroy();
 		logicJob.destroy();
 	}
 
 	public List<EPHVertexArrayObject> getVaos() {
 		return surface.getVaos();
 	}
 
 	public void setSurface(EPHSurface surface) {
 		this.surface = surface;
 	}
 
 	public EPHSurface getSurface() {
 		return surface;
 	}
 
 	public void setDebug(int renderPrintDelay, int logicPrintDelay, int renderWarningThreshold, int logicWarningThreshold) {
 		renderJob.setConsoleDebug(renderPrintDelay, renderWarningThreshold);
 		logicJob.setConsoleDebug(logicPrintDelay, logicWarningThreshold);
 	}
 
 	public void setResizable(boolean resizable) {
 		renderContext.setResizable(resizable);
 	}
 
 	public void resize(int width, int height) {
 		this.width = width;
 		this.height = height;
 		renderContext.resize();
 	}
 
 	public boolean wasResized() {
 		return renderContext.wasResized();
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 }
