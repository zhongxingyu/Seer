 /*******************************************************************************
  * Copyright (c) 2008 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Matthias Thiele - initial API and implementation
  *    Kristian Duske - initial API
  *    Jens von Pilgrim - initial API
  ******************************************************************************/
 package org.eclipse.draw3d.graphics3d;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.eclipse.draw3d.geometry.IHost3D;
 import org.eclipse.draw3d.geometry.IPosition3D;
 import org.eclipse.draw3d.geometry.Position3D;
 
 /**
  * The Graphics3DDraw interface defines common 3D draw operations in OpenGL
  * style.
  * 
  * @author Matthias Thiele
  * @version $Revision$
  * @since 06.12.2008
  */
 public interface Graphics3DDraw {
 
 	/*
 	 * These are render methods which were already abstracted from the original
 	 * OpenGL methods. Some of these method replace gl... methods, concrete
 	 * implementations have to map these methods to appropriate gl... methods.
 	 */
 
 	/**
 	 * Creates a raw representation of the position as used by the concrete
 	 * implementation. The raw representation may be cached, then the cache must
 	 * be updated or made invalid when the position has been changed (see
 	 * {@link IHost3D#positionChanged(java.util.EnumSet, org.eclipse.draw3d.geometry.IVector3f)}
 	 * ).
 	 */
 	Object createRawPosition(IPosition3D position3D);
 
 	/**
 	 * Returns true if the concrete implementation can handle the type of the
 	 * given raw position, previously created by a
 	 * {@link #createRawPosition(IPosition3D)}. Usually, the same implementation
 	 * handling the raw position also creates this position, but if the raw
 	 * position is cached by the client, it may be possible that the concrete
 	 * renderer implementation has been exchanged during creation of the raw
 	 * position and its usage.
 	 * 
 	 * @param theRawPosition May be null!
 	 * @return
 	 */
 	boolean isPositionRawCompatible(Object theRawPosition);
 
 	/**
 	 * Sets the position of an element, the raw position was previously
 	 * retrieved from a {@link Position3D} object by calling
 	 * {@link #createPositionRaw(IPosition3D)}. The buffer is rewound before
 	 * used!
 	 * <p>
 	 * OpenGL note: This method replaces
 	 * <code>glMultMatrix(FloatBuffer m)</code>. The raw position may be a
 	 * FloatBuffer, and then setting the position simply is calling glMultMatrix
 	 * with {@link Position3D#getModelMatrix()} converted to a FloatBuffer.
 	 * </p>
 	 * 
 	 * @param theRawPosition
 	 */
 	void setPosition(Object theRawPosition);
 
 	/*
 	 * Some GL drawing constants, mainly used for controlling the drawing
 	 * methods. As the default renderer is an OpenGL implementation, these
 	 * constant have the corresponding values. Other renderer implementations
 	 * have to interpret their meaning towards their specifics.
 	 */
 
 	public static final float PI = (float) Math.PI;
 
 	public static final int GL_FILL = 0x1b02; // org.lwjgl.opengl.GL11.GL_FILL;
 
 	public static final int GL_FRONT_AND_BACK = 0x408; // org.lwjgl.opengl.GL11.
 
 	// GL_FRONT_AND_BACK;
 
 	public static final int GL_LINES = 0x1; // org.lwjgl.opengl.GL11.GL_LINES;
 
 	public static final int GL_LINE_STIPPLE = 0xb24; // org.lwjgl.opengl.GL11.
 
 	// GL_LINE_STIPPLE;
 
 	public static final int GL_LINE_STRIP = 0x3; // org.lwjgl.opengl.GL11.
 
 	// GL_LINE_STRIP;
 
 	public static final int GL_MODELVIEW = 0x1700; // org.lwjgl.opengl.GL11.
 
 	// GL_MODELVIEW;
 
 	public static final int GL_QUADS = 0x7; // org.lwjgl.opengl.GL11.GL_QUADS;
 
 	public static final int GL_REPLACE = 0x1e01; // org.lwjgl.opengl.GL11.
 
 	// GL_REPLACE;
 
 	public static final int GL_RGBA = 0x1908; // org.lwjgl.opengl.GL11.GL_RGBA;
 
 	public static final int GL_TEXTURE_2D = 0xde1; // org.lwjgl.opengl.GL11.
 
 	// GL_TEXTURE_2D;
 
 	public static final int GL_TEXTURE_ENV = 0x2300; // org.lwjgl.opengl.GL11.
 
 	// GL_TEXTURE_ENV;
 
 	public static final int GL_TEXTURE_ENV_MODE = 0x2200; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_TEXTURE_ENV_MODE
 	// ;
 
 	public static final int GL_UNSIGNED_BYTE = 0x1401; // org.lwjgl.opengl.GL11.
 
 	// GL_UNSIGNED_BYTE;
 
 	public static final int GL_UNPACK_ALIGNMENT = 0xcf5; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_UNPACK_ALIGNMENT
 	// ;
 
 	public static final int GL_VERSION = 0x1f02; // org.lwjgl.opengl.GL11.
 
 	// GL_VERSION;
 
 	public static final int GL_DEPTH_TEST = 0xb71; // org.lwjgl.opengl.GL11.
 
 	// GL_DEPTH_TEST;
 
 	public static final int GL_PROJECTION = 0x1701; // org.lwjgl.opengl.GL11.
 
 	// GL_PROJECTION;
 
 	public static final int GL_LUMINANCE_ALPHA = 0x190a; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_LUMINANCE_ALPHA
 	// ;
 
 	public static final int GL_BLEND = 0xbe2; // org.lwjgl.opengl.GL11.GL_BLEND;
 
 	public static final int GL_COMPILE = 0x1300; // org.lwjgl.opengl.GL11.
 
 	// GL_COMPILE;
 
 	public static final int GL_COLOR_BUFFER_BIT = 0x4000; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_COLOR_BUFFER_BIT
 	// ;
 
 	public static final int GL_DEPTH_BUFFER_BIT = 0x100; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_DEPTH_BUFFER_BIT
 	// ;
 
 	public static final int GL_DEPTH_COMPONENT = 0x1902; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_DEPTH_COMPONENT
 	// ;
 
 	public static final int GL_RGB = 0x1907; // org.lwjgl.opengl.GL11.GL_RGB;
 
 	public static final int GL_FLOAT = 0x1406; //org.lwjgl.opengl.GL11.GL_FLOAT;
 
 	public static final int GL_DITHER = 0xbd0; //org.lwjgl.opengl.GL11.GL_DITHER
 
 	// ;
 
 	public static final int GL_MULTISAMPLE = 0x809d; // org.lwjgl.opengl.GL13.
 
 	// GL_MULTISAMPLE;
 
 	public static final int GL_LINE_LOOP = 0x2; // org.lwjgl.opengl.GL11.
 
 	// GL_LINE_LOOP;
 
 	public static final int GL_FLAT = 0x1d00; // org.lwjgl.opengl.GL11.GL_FLAT;
 
 	public static final int GL_CULL_FACE = 0xb44; // org.lwjgl.opengl.GL11.
 
 	// GL_CULL_FACE;
 
 	public static final int GL_LINE_SMOOTH_HINT = 0xc52; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_LINE_SMOOTH_HINT
 	// ;
 
 	public static final int GL_NICEST = 0x1102; //org.lwjgl.opengl.GL11.GL_NICEST
 
 	// ;
 
 	public static final int GL_LINE_SMOOTH = 0xb20; // org.lwjgl.opengl.GL11.
 
 	// GL_LINE_SMOOTH;
 
 	public static final int GL_VIEWPORT = 0xba2; // org.lwjgl.opengl.GL11.
 
 	// GL_VIEWPORT;
 
 	public static final int GL_PROJECTION_MATRIX = 0xba7; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_PROJECTION_MATRIX
 	// ;
 
 	public static final int GL_MODELVIEW_MATRIX = 0xba6; //org.lwjgl.opengl.GL11
 
 	// .
 	// GL_MODELVIEW_MATRIX
 	// ;
 
	public static final int PLATFORM_WINDOWS = 3; // org.lwjgl.LWJGLUtil.

 	// PLATFORM_WINDOWS;
 
 	public static final int GL_SRC_ALPHA = 0x302; // org.lwjgl.opengl.GL11.
 
 	// GL_SRC_ALPHA;
 
 	public static final int GL_ONE_MINUS_SRC_ALPHA = 0x303; // org.lwjgl.opengl.
 
 	// GL11.
 	// GL_ONE_MINUS_SRC_ALPHA
 	// ;
 
 	public static final int GL_PACK_ALIGNMENT = 0xd05; // org.lwjgl.opengl.GL11.
 
 	// GL_PACK_ALIGNMENT;
 
 	public static final int GL_BGR = 0x80e0; // org.lwjgl.opengl.GL12.GL_BGR;
 
 	public static final int GL_COLOR_INDEX = 0x1900; // org.lwjgl.opengl.GL11.
 
 	// GL_COLOR_INDEX;
 
 	public static final int GL_RED = 0x1903; // org.lwjgl.opengl.GL11.GL_RED;
 
 	public static final int GL_GREEN = 0x1904; //org.lwjgl.opengl.GL11.GL_GREEN;
 
 	public static final int GL_BLUE = 0x1905; // org.lwjgl.opengl.GL11.GL_BLUE;
 
 	public static final int GL_ALPHA = 0x1906; //org.lwjgl.opengl.GL11.GL_ALPHA;
 
 	public static final int GL_LUMINANCE = 0x1909; // org.lwjgl.opengl.GL11.
 
 	// GL_LUMINANCE;
 
 	public static final int GL_STENCIL_INDEX = 0x1901; // org.lwjgl.opengl.GL11.
 
 	// GL_STENCIL_INDEX;
 
 	public static final int GL_BYTE = 0x1400; // org.lwjgl.opengl.GL11.GL_BYTE;
 
 	public static final int GL_UNSIGNED_SHORT = 0x1403; //org.lwjgl.opengl.GL11.
 
 	// GL_UNSIGNED_SHORT;
 
 	public static final int GL_SHORT = 0x1402; //org.lwjgl.opengl.GL11.GL_SHORT;
 
 	public static final int GL_UNSIGNED_INT = 0x1405; // org.lwjgl.opengl.GL11.
 
 	// GL_UNSIGNED_INT;
 
 	public static final int GL_INT = 0x1404; // org.lwjgl.opengl.GL11.GL_INT;
 
 	public static final int GL_BGRA = 0x80e1; // org.lwjgl.opengl.GL12.GL_BGRA;
 
 	public static final int GL_UNSIGNED_BYTE_3_3_2 = 0x8032; //org.lwjgl.opengl.
 
 	// GL12.
 	// GL_UNSIGNED_BYTE_3_3_2
 	// ;
 
 	public static final int GL_UNSIGNED_BYTE_2_3_3_REV = 0x8362; // org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_BYTE_2_3_3_REV
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_5_6_5 = 0x8363; //org.lwjgl.opengl
 
 	// .GL12.
 	// GL_UNSIGNED_SHORT_5_6_5
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_5_6_5_REV = 0x8364; // org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_SHORT_5_6_5_REV
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033; // org.lwjgl.
 
 	// opengl.GL12.
 	// GL_UNSIGNED_SHORT_4_4_4_4
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_4_4_4_4_REV = 0x8365; //org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_SHORT_4_4_4_4_REV
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034; // org.lwjgl.
 
 	// opengl.GL12.
 	// GL_UNSIGNED_SHORT_5_5_5_1
 	// ;
 
 	public static final int GL_UNSIGNED_SHORT_1_5_5_5_REV = 0x8366; //org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_SHORT_1_5_5_5_REV
 	// ;
 
 	public static final int GL_UNSIGNED_INT_8_8_8_8 = 0x8035; //org.lwjgl.opengl
 
 	// .GL12.
 	// GL_UNSIGNED_INT_8_8_8_8
 	// ;
 
 	public static final int GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367; // org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_INT_8_8_8_8_REV
 	// ;
 
 	public static final int GL_UNSIGNED_INT_10_10_10_2 = 0x8036; // org.lwjgl.
 
 	// opengl
 	// .GL12.
 	// GL_UNSIGNED_INT_10_10_10_2
 	// ;
 
 	public static final int GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368; //org.lwjgl
 
 	// .
 	// opengl
 	// .
 	// GL12.
 	// GL_UNSIGNED_INT_2_10_10_10_REV
 	// ;
 
 	public static final int GL_POINTS = 0x0; // org.lwjgl.opengl.GL11.GL_POINTS;
 
 	public static final int GL_QUAD_STRIP = 0x8; // org.lwjgl.opengl.GL11.
 
 	// GL_QUAD_STRIP;
 
 	/*
 	 * These are the 3D drawing methods. As the default renderer implementation
 	 * is OpenGL, the methods are named accordingly and will fit well to the
 	 * OpenGL renderer. Other renderer implementation have to interpret the
 	 * methods according to their specifics.
 	 */
 
 	public abstract void glBegin(int mode);
 
 	public abstract void glBindTexture(int target, int texture);
 
 	public abstract void glColor4f(float red, float green, float blue,
 			float alpha);
 
 	public abstract void glDisable(int cap);
 
 	public abstract void glEnable(int cap);
 
 	public abstract void glEnd();
 
 	public abstract void glTexEnvi(int target, int pname, int param);
 
	public abstract void glTexCoord2f(int s, int t);
 
 	public abstract void glPolygonMode(int face, int mode);
 
 	public abstract void glVertex2f(float x, float y);
 
 	public abstract void glMatrixMode(int mode);
 
 	public abstract void glPushMatrix();
 
 	public abstract void glPopMatrix();
 
 	public abstract void glLineStipple(int factor, short pattern);
 
 	public abstract void glTranslatef(float x, float y, int z);
 
 	public abstract String glGetString(int name);
 
 	public abstract void glGetFloat(int pname, FloatBuffer params);
 
 	public abstract void glGetInteger(int pname, IntBuffer params);
 
 	public abstract void glViewport(int x, int y, int width, int height);
 
 	public abstract void glLoadIdentity();
 
 	public abstract int glGenLists(int range);
 
 	public abstract void glDeleteLists(int list, int range);
 
 	public abstract void glCallList(int list);
 
 	public abstract void glNewList(int list, int mode);
 
 	public abstract void glEndList();
 
 	public abstract boolean glIsEnabled(int cap);
 
 	public abstract void glVertex3f(float x, float y, float z);
 
 	public abstract void glNormal3f(int nx, int ny, int nz);
 
 	public abstract void glFlush();
 
 	public abstract void glShadeModel(int mode);
 
 	public abstract void glHint(int target, int mode);
 
 	public abstract void glClearColor(float red, float green, float blue,
 			float alpha);
 
 	public abstract void glClear(int mask);
 
 	public abstract int getPlatform();
 
 	public abstract void glBlendFunc(int sfactor, int dfactor);
 
 	public abstract void glClearDepth(double depth);
 
 	public abstract void glPixelStorei(int pname, int param);
 
 	public abstract void glLineWidth(float width);
 
 	public abstract void glColor3f(int red, int green, int blue);
 
 	public abstract void glFinish();
 
 	public abstract void glNormal3f(float nx, float ny, float nz);
 
 	public void useContext(Object context) throws Graphics3DException;
 
 }
