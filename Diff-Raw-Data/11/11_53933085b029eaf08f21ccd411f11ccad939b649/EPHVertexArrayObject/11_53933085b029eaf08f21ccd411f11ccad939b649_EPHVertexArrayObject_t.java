 /*
  * Copyright 2013, Sebastian Kreisel. All rights reserved.
  * If you intend to use, modify or redistribute this file contact kreisel.sebastian@gmail.com
  */
 
 package com.elfeck.ephemeral.glContext;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL30.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class EPHVertexArrayObject {
 
 	private static EPHShaderProgramPool shaderProgramPool;
 
 	private boolean dead;
 	private int mode, size, usage, handle, uniformKey;
 	private int[] viewPortRect, scissorRect;
 	private List<EPHVaoEntry> entries;
 	private EPHVertexBufferObject vbo;
 	private EPHIndexBufferObject ibo;
 
 	public EPHVertexArrayObject(int mode, int usage, int[] viewPortRect, int[] scissorRect, List<EPHVertexAttribute> vertexAttributes) {
 		dead = false;
 		this.mode = mode;
 		this.size = 0;
 		this.usage = usage;
 		this.viewPortRect = viewPortRect;
 		this.scissorRect = scissorRect;
 		handle = -1;
 		uniformKey = 0;
 		entries = new ArrayList<EPHVaoEntry>();
 		vbo = new EPHVertexBufferObject(vertexAttributes);
 		ibo = new EPHIndexBufferObject();
 	}
 
 	public EPHVertexArrayObject(int[] viewPortRect, int[] scissorRect, List<EPHVertexAttribute> vertexAttributes) {
 		this(EPHRenderUtils.TYPE_TRIANGLES, EPHRenderUtils.MODE_STATIC_DRAW, viewPortRect, scissorRect, vertexAttributes);
 	}
 
 	public EPHVertexArrayObject(List<EPHVertexAttribute> vertexAttributes) {
 		this(EPHRenderUtils.TYPE_TRIANGLES, EPHRenderUtils.MODE_STATIC_DRAW,
 				EPHRenderContext.getWindowDimensions(), EPHRenderContext.getWindowDimensions(), vertexAttributes);
 	}
 
 	private void glInit() {
 		if (handle < 0) handle = glGenVertexArrays();
 		glUpdateVbo();
 		glUpdateIbo();
 		glBindVertexArray(handle);
 		vbo.glBind();
 		ibo.glBind();
 		glBindVertexArray(0);
 	}
 
 	private void glUpdateVbo() {
 		vbo.glInit(usage);
 	}
 
 	private void glUpdateIbo() {
 		ibo.glInit(usage);
 	}
 
 	private Map<Integer, String> vertexAttributesToMap(List<EPHVertexAttribute> vertexAttributes) {
 		Map<Integer, String> names = new HashMap<Integer, String>();
 		for (EPHVertexAttribute va : vertexAttributes) {
 			names.put(va.index, va.name);
 		}
 		return names;
 	}
 
 	private boolean hasVisibleEntries() {
 		boolean hasVisibleEntries = false;
 		for (EPHVaoEntry entry : entries) {
 			if (entry.isVisible()) {
 				hasVisibleEntries = true;
 				break;
 			}
 		}
 		return hasVisibleEntries;
 	}
 
 	public synchronized void glRender() {
 		if (handle < 0) glInit();
 		if (!vbo.isUpdated()) glUpdateVbo();
 		if (!ibo.isUpdated()) glUpdateIbo();
 		if (size > 0 && hasVisibleEntries()) {
 			glBindVertexArray(handle);
 			glViewport(viewPortRect[0], viewPortRect[1], viewPortRect[2], viewPortRect[3]);
 			glScissor(scissorRect[0], scissorRect[1], scissorRect[2], scissorRect[3]);
 			EPHShaderProgram currentProgram = null;
 			EPHVaoEntry current, next;
 			boolean bindRequ = true;
 			for (int i = 0; i < entries.size(); i++) {
 				current = entries.get(i);
 				if (!current.visible) continue;
 				next = i < entries.size() - 1 ? entries.get(i + 1) : null;
 				currentProgram = shaderProgramPool.getShaderProgram(current.programKey);
 				if (!currentProgram.isLinked()) currentProgram.glAttachAndLinkProgram(vertexAttributesToMap(vbo.getVertexAttributes()));
 				if (bindRequ) currentProgram.glBind();
 				currentProgram.glUseUniforms(current.uniformKey);
 				glDrawElements(mode, current.iboUpperBound - current.iboLowerBound + 1, GL_UNSIGNED_INT, current.iboLowerBound * 4);
 				bindRequ = next == null ? true : current.programKey.equals(next.programKey) ? false : true;
 				if (bindRequ) currentProgram.glUnbind();
 			}
 			glBindVertexArray(0);
 		}
 	}
 
 	public void glDispose() {
 		vbo.glDispose();
 		ibo.glDispose();
 		glDeleteBuffers(handle);
 	}
 
 	public synchronized EPHVaoEntry addData(List<Float> vertexValues, List<Integer> indices, String programKey) {
 		EPHVaoEntry entry = new EPHVaoEntry();
 		entry.programKey = programKey;
 		return addData(vertexValues, indices, entry);
 	}
 
 	public synchronized EPHVaoEntry addData(List<Float> vertexValues, List<Integer> indices, EPHVaoEntry entry) {
 		if (!EPHRenderContext.isInitialized()) {
 			synchronized (EPHRenderContext.glInitMonitor) {
 				try {
 					EPHRenderContext.glInitMonitor.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		entry.vboLowerBound = vbo.getCurrentIndex();
 		entry.vboUpperBound = vbo.getCurrentIndex() + vertexValues.size() - 1;
 		entry.iboLowerBound = ibo.getCurrentIndex();
 		entry.iboUpperBound = ibo.getCurrentIndex() + indices.size() - 1;
 		entry.uniformKey = uniformKey++;
 		entry.utb = shaderProgramPool.getShaderProgram(entry.programKey).getUniformTemplateBuffer();
 		ibo.addData(indices, vbo.addData(vertexValues));
 		size += indices.size();
 		entries.add(entry);
 		return entry;
 	}
 
 	public synchronized void removeData(EPHVaoEntry entry) {
 		ibo.removeData(entry.iboLowerBound, entry.iboUpperBound, vbo.removeData(entry.vboLowerBound, entry.vboUpperBound));
 		size -= entry.iboUpperBound - entry.iboLowerBound + 1;
 		int deletedVertexValues = entry.vboUpperBound - entry.vboLowerBound + 1;
 		int deletedIndices = entry.iboUpperBound - entry.iboLowerBound + 1;
 		for (int i = entries.indexOf(entry) + 1; i < entries.size(); i++) {
 			EPHVaoEntry currentEntry = entries.get(i);
 			currentEntry.vboLowerBound -= deletedVertexValues;
 			currentEntry.vboUpperBound -= deletedVertexValues;
 			currentEntry.iboLowerBound -= deletedIndices;
 			currentEntry.iboUpperBound -= deletedIndices;
 		}
 		entries.remove(entry);
 	}
 
	public synchronized void updateVbo(EPHVaoEntry entry, int subLower, int subUpper, List<Float> vertexValues) {
		if (subLower < 0) subLower = 0;
		if (subUpper < 0) subUpper = entry.vboUpperBound - entry.vboLowerBound + 1;
		vbo.updateData(entry.vboLowerBound + subLower, entry.vboLowerBound + subUpper, vertexValues);
	}

	public synchronized void updateIbo(EPHVaoEntry entry, int subLower, int subUpper, List<Integer> indices) {
		if (subLower < 0) subLower = 0;
		if (subUpper < 0) subUpper = entry.iboUpperBound - entry.iboLowerBound + 1;
		ibo.updateData(entry.vboLowerBound + subLower, entry.vboLowerBound + subUpper, indices);
	}
 	public synchronized void setViewportRect(int[] bounds) {
 		viewPortRect = bounds;
 	}
 
 	public synchronized void setScissorRect(int[] bounds) {
 		scissorRect = bounds;
 	}
 
 	public synchronized void resetViewPortAndScissor() {
 		viewPortRect = EPHRenderContext.getWindowDimensions();
 		scissorRect = EPHRenderContext.getWindowDimensions();
 	}
 
 	public boolean isDead() {
 		return dead;
 	}
 
 	public void setDead(boolean dead) {
 		this.dead = dead;
 	}
 
 	protected static void glInitShaderProgramPool(String parentPath) {
 		shaderProgramPool = new EPHShaderProgramPool(parentPath);
 		shaderProgramPool.glInit();
 	}
 
 	/*
 	 * Not feeling good about this accessibility. Needed for program switching
 	 * in EPHVaoEntry.
 	 */
 	protected static EPHShaderProgramPool getShaderProgramPool() {
 		return shaderProgramPool;
 	}
 
 	public static void glDisposeShaderPrograms() {
 		shaderProgramPool.glDisposeShaderPrograms();
 	}
 
 }
