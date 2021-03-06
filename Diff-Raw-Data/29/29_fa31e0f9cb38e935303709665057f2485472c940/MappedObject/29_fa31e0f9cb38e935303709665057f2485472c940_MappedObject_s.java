 /*
  * Copyright (c) 2002-2011 LWJGL Project
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'LWJGL' nor the names of
  *   its contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.lwjgl.util.mapped;
 
 import org.lwjgl.LWJGLUtil;
 
 import java.nio.ByteBuffer;
 
 /**
  * Base superclass of all mapped objects. Classes that require
  * data mapping should extend this class and also be annotated
  * with {@link MappedType}.
  * <p/>
  * Subclasses may only specify the default constructor. Any code
  * inside that constructor is optional, but will not run when the
  * view is instantiated, see {@link #runViewConstructor()}.
  * <p/>
  * Bounds checking may be enabled through a JVM system property: org.lwjgl.util.mapped.Checks=true
  *
  * @author Riven
  */
 public class MappedObject {
 
 	static final boolean CHECKS = LWJGLUtil.getPrivilegedBoolean("org.lwjgl.util.mapped.Checks");
 
 	public MappedObject() {
 		//
 	}
 
 	/** The mapped object base memory address, in bytes. Read-only. */
 	public long baseAddress;
 
 	/** The mapped object view memory address, in bytes. Read-only. */
 	public long viewAddress;
 
 	/** The mapped object stride, in bytes. Read-only. */
 	public int stride;
 
 	/** The mapped object memory alignment, in bytes. Read-only. */
 	public int align;
 
 	/**
 	 * Holds the value of sizeof of the sub-type of this MappedObject<br>
 	 * <br>
 	 * The behavior of this (transformed) method does not follow the normal Java behavior.<br>
 	 * <code>Vec2.SIZEOF</code> will yield 8 (2 floats)<br>
 	 * <code>Vec3.SIZEOF</code> will yield 12 (3 floats)<br>
 	 * This (required) notation might cause compiler warnings, which can be suppressed with @SuppressWarnings("static-access").<br>
 	 * Using Java 5.0's static-import on this method will break functionality.
 	 */
 	public static int SIZEOF = -1; // any method that calls these field will have its call-site modified ('final' per subtype)
 
 	/**
 	 * The mapped object view offset, in elements. Read/write.
 	 * This is a virtual field, used as a convenient getter/setter for {@see viewAddress}.
 	 */
 	public int view;
 
 	/** Moves the current view to the next element. */
 	public final void next() {
 		setViewAddress(this.viewAddress + this.stride);
 	}
 
 	final void setViewAddress(final long address) {
 		if ( CHECKS )
 			checkAddress(address);
 		this.viewAddress = address;
 	}
 
 	final void checkAddress(final long address) {
		if ( preventGC.capacity() < (address + stride - baseAddress) )
 			throw new IndexOutOfBoundsException();
 	}
 
 	/**
 	 * Creates a MappedObject instance, mapping the memory region of the specified direct ByteBuffer.
 	 * <p/>
 	 * The behavior of this (transformed) method does not follow the normal Java behavior.<br>
 	 * <code>Vec2.map(buffer)</code> will return a mapped Vec2 instance.<br>
 	 * <code>Vec3.map(buffer)</code> will return a mapped Vec3 instance.<br>
 	 * This (required) notation might cause compiler warnings, which can be suppressed with @SuppressWarnings("static-access").<br>
 	 * Using Java 5.0's static-import on this method will break functionality.
 	 */
 	@SuppressWarnings("unused")
 	public static <T extends MappedObject> T map(ByteBuffer bb) {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Creates a MappedObject instance, mapping the memory region specified. This is useful for mapping
 	 * arbitrary regions in memory, e.g. OpenCL CLMem objects, without creating a ByteBuffer first.
 	 * <p/>
 	 * The behavior of this (transformed) method does not follow the normal Java behavior.<br>
 	 * <code>Vec2.map(buffer)</code> will return a mapped Vec2 instance.<br>
 	 * <code>Vec3.map(buffer)</code> will return a mapped Vec3 instance.<br>
 	 * This (required) notation might cause compiler warnings, which can be suppressed with @SuppressWarnings("static-access").<br>
 	 * Using Java 5.0's static-import on this method will break functionality.
 	 */
 	@SuppressWarnings("unused")
 	public static <T extends MappedObject> T map(long address, int capacity) {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Creates a MappedObject instance, mapping the memory region of an allocated direct ByteBuffer with a capacity of <code>elementCount*SIZEOF</code>
 	 * <p/>
 	 * The behavior of this (transformed) method does not follow the normal Java behavior.<br>
 	 * <code>Vec2.malloc(int)</code> will return a mapped Vec2 instance.<br>
 	 * <code>Vec3.malloc(int)</code> will return a mapped Vec3 instance.<br>
 	 * This (required) notation might cause compiler warnings, which can be suppressed with @SuppressWarnings("static-access").<br>
 	 * Using Java 5.0's static-import on this method will break functionality.
 	 */
 	@SuppressWarnings("unused")
 	public static <T extends MappedObject> T malloc(int elementCount) {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Creates an identical new MappedObject instance, comparable to the
 	 * contract of {@link ByteBuffer#duplicate}. This is useful when more than one
 	 * views of the mapped object are required at the same time, e.g. in
 	 * multithreaded access.
 	 */
 	public final <T extends MappedObject> T dup() {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Creates a new MappedObject instance, with a base offset equal to
 	 * the offset of the current view, comparable to the contract of  {@link ByteBuffer#slice}.
 	 */
 	public final <T extends MappedObject> T slice() {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Any code in the default constructor will not run automatically. This method
 	 * can be used to run execute that code on the current view.
 	 */
 	public final <T extends MappedObject> void runViewConstructor() {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Copies and amount of <code>SIZEOF</code> bytes, from the current
 	 * mapped object, to the specified mapped object.
 	 */
 	@SuppressWarnings("unused")
 	public final <T extends MappedObject> void copyTo(T target) {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Copies and amount of <code>SIZEOF*instances<c/ode> bytes, from the
 	 * current mapped object, to the specified mapped object.
 	 */
 	@SuppressWarnings("unused")
 	public final <T extends MappedObject> void copyRange(T target, int instances) {
 		// any method that calls this method will have its call-site modified
 		throw new InternalError("type not registered");
 	}
 
 	/**
 	 * Creates an {@link Iterable} <MappedObject> that will step through
 	 * <code>elementCount</code> views, leaving the <code>view</code> at
 	 * the last valid value.<br>
 	 * <p/>
 	 * For convenience you are encouraged to static-import this specific method:
 	 * <code>import static org.lwjgl.util.mapped.MappedObject.foreach;</code>
 	 */
 	public static <T extends MappedObject> MappedForeach<T> foreach(T mapped, int elementCount) {
 		return new MappedForeach<T>(mapped, elementCount);
 	}
 
 	/**
 	 * Configures a newly initiated mapped object with the specified stride and offset.
 	 *
 	 * @throws IllegalStateException if view is not at index 0
 	 */
 	public static <T extends MappedObject> T configure(T mapped, int stride, int offset) {
 		if ( mapped.baseAddress != mapped.viewAddress )
 			throw new IllegalStateException("view must be zero");
 
 		if ( offset < 0 )
 			throw new IllegalStateException("offset must not be negative: " + offset);
 		if ( offset % mapped.align != 0 )
 			throw new IllegalStateException("offset not a multiple of alignment: " + offset);
 
 		if ( stride < mapped.stride )
 			throw new IllegalStateException("new stride must not be smaller than current stride: " + stride);
 		if ( stride % mapped.align != 0 )
 			throw new IllegalStateException("stride not a multiple of alignment: " + stride);
 
 		mapped.baseAddress += offset;
 		mapped.viewAddress += offset;
 		mapped.stride = stride;
 
 		return mapped;
 	}
 
 	ByteBuffer preventGC;
 
 	/**
 	 * Returns the {@link ByteBuffer} that backs this mapped object.
 	 *
 	 * @return the backing buffer
 	 */
 	public ByteBuffer backingByteBuffer() {
 		return this.preventGC;
 	}
 
 }
