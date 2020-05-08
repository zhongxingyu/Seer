 /**
  * Java Modular Image Synthesis Toolkit (JMIST)
  * Copyright (C) 2008-2013 Bradley W. Kimmel
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.eandb.jmist.util.matlab;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Stack;
 import java.util.zip.DeflaterOutputStream;
 
 import javax.imageio.stream.ImageOutputStream;
 
 import ca.eandb.jmist.math.Complex;
 import ca.eandb.jmist.math.MathUtil;
 
 /**
  * An <code>OutputStream</code> for writing MATLAB MAT-files.
  * @author Brad Kimmel
  */
 public final class MatlabOutputStream extends OutputStream implements DataOutput {
 
 	/**
 	 * Creates a new <code>MatlabOutputStream</code> that writes to an
 	 * <code>OuptutStream</code>.
 	 * @param out The <code>OutputStream</code> to write to.
 	 * @throws IOException if writing the MAT-file header to the underlying
 	 * 		<code>OutputStream</code> fails.
 	 */
 	public MatlabOutputStream(OutputStream out) throws IOException {
 		this.streams = new Stack<DataOutputStream>();
 		this.streams.push(new DataOutputStream(out));
 		this.writeHeader();
 	}
 
 	/**
 	 * Creates a new <code>MatlabOutputStream</code> that writes to an
 	 * <code>ImageOutputStream</code>.
 	 * @param out The <code>ImageOutputStream</code> to write to.
 	 * @throws IOException if writing the MAT-file header to the underlying
 	 * 		<code>ImageOutputStream</code> fails.
 	 */
 	public MatlabOutputStream(ImageOutputStream out) throws IOException {
 		this(new ImageOutputStreamAdapter(out));
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutputStream#write(byte[], int, int)
 	 */
 	@Override
 	public synchronized void write(byte[] b, int off, int len)
 			throws IOException {
 		this.stream().write(b, off, len);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutputStream#write(int)
 	 */
 	@Override
 	public synchronized void write(int b) throws IOException {
 		this.stream().write(b);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.FilterOutputStream#write(byte[])
 	 */
 	@Override
 	public void write(byte[] b) throws IOException {
 		this.stream().write(b);
 	}
 
 	/**
 	 * Begins a new data element.
 	 * @param type The <code>MatlabDataType</code> of the element to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void beginElement(MatlabDataType type) throws IOException {
 		this.push(type);
 	}
 
 	/**
 	 * Adds a <code>DataOutputStream</code> to the <code>streams</code> stack
 	 * for writing to a data element of the specified type.
 	 * @param type The <code>MatlabDataType</code> of the element to b
 	 * 		written.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void push(MatlabDataType type) throws IOException {
 		this.streams.push(CompoundElementOutputStream.create(type));
 	}
 
 	/**
 	 * Begins a new data element of known length.
 	 * @param type The <code>MatlabDataType</code> of the element to write.
 	 * @param bytes The length (in bytes) of the element (not including the
 	 * 		tag).
 	 * @throws IOException if writing to the underlying stream fails.
 	 * @see #streams
 	 */
 	public void beginElement(MatlabDataType type, int bytes) throws IOException {
 		this.push(type, bytes);
 	}
 
 	/**
 	 * Adds a <code>DataOutputStream</code> to the <code>streams</code> stack
 	 * for writing to a data element of known length of the specified type.
 	 * @param type The <code>MatlabDataType</code> of the element to be
 	 * 		written.
 	 * @param bytes The length (in bytes) of the data element to be written.
 	 * @throws IOException if writing to the underlying stream fails.
 	 * @see #streams
 	 */
 	private void push(MatlabDataType type, int bytes) throws IOException {
 		this.streams.push(CompoundElementOutputStream.create(type, bytes, this.stream()));
 	}
 
 	/**
 	 * Ends a data element.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void endElement() throws IOException {
 		CompoundElementOutputStream top = this.pop();
 		top.writeTo(this.stream());
 	}
 
 	/**
 	 * Removes and returns the <code>CompoundElementOutputStream</code> at the
 	 * top of the <code>streams</code> stack.  The initial stream (at the
 	 * bottom of the stack) may not be removed.
 	 * @return The <code>CompoundElementOutputStream</code> that was removed.
 	 * @throws IllegalStateException if an attempt is made to pop the initial
 	 * 		stream.
 	 */
 	private CompoundElementOutputStream pop() {
 		if (this.streams.size() > 1) {
 			return (CompoundElementOutputStream) this.streams.pop();
 		} else {
 			throw new IllegalStateException("matlab element underflow");
 		}
 	}
 
 	/**
 	 * Begins a numeric or character array element.
 	 * @param name The name of the MATLAB array.
 	 * @param arrayType The <code>MatlabArrayType</code> of the array.
 	 * @param dataType The <code>MatlabDataType</code> to use to store the
 	 * 		elements of the MATLAB array.
 	 * @param complex A value indicating whether the imaginary part is to be
 	 * 		included.
 	 * @param global A value indicating whether the array is to have global
 	 * 		scope.
 	 * @param logical A value indicating whether the array is to be used for
 	 * 		logical indexing.
 	 * @param dimensions An array describing the dimensions of the array (there
 	 * 		must be at least two dimensions).  This may be null, in which case
 	 * 		the array will have the dimensions <code>{ elements, 1 }</code>.
 	 * @param elements The total number of elements in the array (must be equal
 	 * 		to the product of the dimensions).
 	 * @throws IOException if writing to the underlying stream fails.
 	 * @throws IllegalArgumentException if <code>elements</code> is not equal
 	 * 		to the product of the dimensions.
 	 * @throws IllegalArgumentException if
 	 * 		<code>dimensions.length &lt; 2</code>.
 	 */
 	public void beginArrayElement(String name, MatlabArrayType arrayType,
 			MatlabDataType dataType, boolean complex, boolean global,
 			boolean logical, int[] dimensions, int elements) throws IOException {
 
 		assert(dataType.size > 0);
 
 		if (dimensions != null) {
 			checkDimensions(dimensions, elements);
 		} else {
 			dimensions = new int[]{ elements, 1 };
 		}
 
 		int bytes = 0;
 
 		/* size of array flags */
 		bytes += MATLAB_TAG_SIZE + MATLAB_ARRAY_FLAGS_SIZE;
 
 		/* size of dimensions */
 		bytes += MATLAB_TAG_SIZE + roundToBoundary(dimensions.length * MatlabDataType.INT32.size);
 
 		/* size of array name */
 		bytes += MATLAB_TAG_SIZE + roundToBoundary(name.length());
 
 		/* size of real data */
 		bytes += MATLAB_TAG_SIZE + roundToBoundary(elements * dataType.size);
 
 		/* size of imaginary data (optional) */
 		if (complex) {
 			bytes += MATLAB_TAG_SIZE + roundToBoundary(elements * dataType.size);
 		}
 
 		this.beginElement(MatlabDataType.MATRIX, bytes);
 		this.writeArrayFlagsElement(arrayType, complex, global, logical);
 		this.writeArrayDimensionsElement(dimensions);
 		this.writeArrayNameElement(name);
 
 	}
 
 	/**
 	 * Rounds up to the next <code>MATLAB_BYTE_ALIGNMENT</code> bytes.
 	 * @param position The current stream position.
 	 * @return The rounded value.
 	 */
 	private static int roundToBoundary(int position) {
 		int n = position % MATLAB_BYTE_ALIGNMENT;
 		return n > 0 ? position + (MATLAB_BYTE_ALIGNMENT - n) : position;
 	}
 
 	/**
 	 * Ensures that <code>dimensions</code> is valid.
 	 * @param dimensions An array describing the dimensions of an array.
 	 * @param elements The total number of elements in the array.
 	 * @throws IllegalArgumentException if <code>elements</code> is not equal
 	 * 		to the product of the dimensions.
 	 * @throws IllegalArgumentException if
 	 * 		<code>dimensions.length &lt; 2</code>.
 	 */
 	private static void checkDimensions(int[] dimensions, int elements) {
 		if (dimensions.length < 2) {
 			throw new IllegalArgumentException("must have at least two dimensions");
 		}
 		int total = 1;
 		for (int i = 0; i < dimensions.length; i++) {
 			total *= dimensions[i];
 		}
 		if (total != elements) {
 			throw new IllegalArgumentException("incorrect number of elements.");
 		}
 	}
 
 	/**
 	 * Writes a <code>boolean</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(boolean[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT8, array.length);
 		this.writeBooleans(array);
 	}
 
 	/**
 	 * Writes a <code>double</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(double[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.DOUBLE, array.length);
 		this.writeDoubles(array);
 	}
 
 	/**
 	 * Writes a <code>float</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(float[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.SINGLE, array.length);
 		this.writeFloats(array);
 	}
 
 	/**
 	 * Writes a <code>byte</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(byte[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT8, array.length);
 		this.stream().write(array);
 	}
 
 	/**
 	 * Writes a <code>short</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(short[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT16, array.length);
 		this.writeShorts(array);
 	}
 
 	/**
 	 * Writes a <code>int</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(int[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT32, array.length);
 		this.writeInts(array);
 	}
 
 	/**
 	 * Writes a <code>long</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(long[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT64, array.length);
 		this.writeLongs(array);
 	}
 
 	/**
 	 * Writes an unsigned <code>byte</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(byte[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT8, array.length);
 		this.stream().write(array);
 	}
 
 	/**
 	 * Writes an unsigned <code>short</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(short[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT16, array.length);
 		this.writeShorts(array);
 	}
 
 	/**
 	 * Writes an unsigned <code>int</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(int[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT32, array.length);
 		this.writeInts(array);
 	}
 
 	/**
 	 * Writes an unsigned <code>long</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(long[] array) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT64, array.length);
 		this.writeLongs(array);
 	}
 
 	/**
 	 * Writes a character array MATLAB element to the underlying stream.
 	 * @param s The <code>String</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(String s) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT16, s.length());
 		this.stream().writeChars(s);
 	}
 
 	/**
 	 * Writes a two <code>double</code> MATLAB elements to the underlying
 	 * stream: one for the real part and one for the imaginary part.
 	 * @param array The array of <code>Complex</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(Complex[] array) throws IOException {
 
 		this.writePrimitiveElementTag(MatlabDataType.DOUBLE, array.length);
 		for (int i = 0; i < array.length; i++) {
 			this.stream().writeDouble(array[i].re());
 		}
 
 		this.writePrimitiveElementTag(MatlabDataType.DOUBLE, array.length);
 		for (int i = 0; i < array.length; i++) {
 			this.stream().writeDouble(array[i].im());
 		}
 
 	}
 
 	/**
 	 * Writes a <code>boolean</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(boolean[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT8, MathUtil.product(dims));
 		this.writeBooleans(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>double</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(double[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.DOUBLE, MathUtil.product(dims));
 		this.writeDoubles(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>float</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(float[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.SINGLE, MathUtil.product(dims));
 		this.writeFloats(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>byte</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(byte[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT8, MathUtil.product(dims));
 		this.writeBytes(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>short</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(short[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT16, MathUtil.product(dims));
 		this.writeShorts(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>int</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(int[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT32, MathUtil.product(dims));
 		this.writeInts(array, dims, strides);
 	}
 
 	/**
 	 * Writes a <code>long</code> MATLAB element to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeElement(long[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.INT64, MathUtil.product(dims));
 		this.writeLongs(array, dims, strides);
 	}
 
 	/**
 	 * Writes an unsigned <code>byte</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(byte[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT8, MathUtil.product(dims));
 		this.writeBytes(array, dims, strides);
 	}
 
 	/**
 	 * Writes an unsigned <code>short</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(short[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT16, MathUtil.product(dims));
 		this.writeShorts(array, dims, strides);
 	}
 
 	/**
 	 * Writes an unsigned <code>int</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(int[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT32, MathUtil.product(dims));
 		this.writeInts(array, dims, strides);
 	}
 
 	/**
 	 * Writes an unsigned <code>long</code> MATLAB element to the underlying
 	 * stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeUnsignedElement(long[] array, int[] dims, int[] strides) throws IOException {
 		this.writePrimitiveElementTag(MatlabDataType.UINT64, MathUtil.product(dims));
 		this.writeLongs(array, dims, strides);
 	}
 
 	/**
 	 * Writes an array flags element to the underlying stream.
 	 * @param type The <code>MatlabArrayType</code> of the array for which to
 	 * 		write the flags element.
 	 * @param complex A value indicating if the array has an imaginary part.
 	 * @param global A value indicating if the array is to be of global scope.
 	 * @param logical A value indicating if the array is to be used for logical
 	 * 		indexing.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeArrayFlagsElement(MatlabArrayType type, boolean complex, boolean global, boolean logical) throws IOException {
 
 		byte[] flags = new byte[MATLAB_ARRAY_FLAGS_SIZE];
 
 		if (complex) {
 			flags[MATLAB_ARRAY_FLAGS_INDEX] |= MATLAB_ARRAY_COMPLEX;
 		}
 
 		if (global) {
 			flags[MATLAB_ARRAY_FLAGS_INDEX] |= MATLAB_ARRAY_GLOBAL;
 		}
 
 		if (logical) {
 			flags[MATLAB_ARRAY_FLAGS_INDEX] |= MATLAB_ARRAY_LOGICAL;
 		}
 
 		flags[MATLAB_ARRAY_CLASS_INDEX] = type.value;
 
 		this.writeElementTag(MatlabDataType.UINT32, MATLAB_ARRAY_FLAGS_SIZE);
 		this.stream().write(flags);
 
 	}
 
 	/**
 	 * Writes an array dimensions element to the underlying stream.
 	 * @param dimensions The dimensions of the array.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeArrayDimensionsElement(int[] dimensions) throws IOException {
 		this.writeElement(dimensions);
 	}
 
 	/**
 	 * Writes an array name element to the underlying stream.
 	 * @param name The name of the array being written.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeArrayNameElement(String name) throws IOException {
 		this.writeElementTag(MatlabDataType.INT8, name.length());
 		this.stream().writeBytes(name);
 	}
 
 	/**
 	 * Writes the tag for a primitive <code>MatlabDataType</code> (i.e., not
 	 * a tag for a <code>MATRIX</code>, <code>COMPRESSED</code>, etc. tag)
 	 * to the underlying stream.
 	 * @param type The <code>MatlabDataType</code> of the element for which to
 	 * 		write the tag.
 	 * @param elements The number of items in the data element.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private final void writePrimitiveElementTag(MatlabDataType type, int elements) throws IOException {
 		writePrimitiveElementTagTo(this.stream(), type, elements);
 	}
 
 	/**
 	 * Writes the tag for a primitive <code>MatlabDataType</code> (i.e., not
 	 * a tag for a <code>MATRIX</code>, <code>COMPRESSED</code>, etc. tag)
 	 * to the specified <code>DataOutputStream</code>.
 	 * @param out The <code>DataOutputStream</code> to write to.
 	 * @param type The <code>MatlabDataType</code> of the element for which to
 	 * 		write the tag.
 	 * @param elements The number of items in the data element.
 	 * @throws IOException if writing to <code>out</code> fails.
 	 */
 	private static final void writePrimitiveElementTagTo(DataOutputStream out, MatlabDataType type, int elements) throws IOException {
 		assert(type.size > 0);
 		writeElementTagTo(out, type, elements * type.size);
 	}
 
 	/**
 	 * Writes the tag for an element with the specified
 	 * <code>MatlabDataType</code>.
 	 * @param type The <code>MatlabDataType</code> of the element for which to
 	 * 		write the tag.
 	 * @param bytes The length (in bytes) of the element (excluding the tag).
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private final void writeElementTag(MatlabDataType type, int bytes) throws IOException {
 		writeElementTagTo(this.stream(), type, bytes);
 	}
 
 	/**
 	 * Writes the tag for an element with the specified
 	 * <code>MatlabDataType</code>.
 	 * @param out The <code>DataOutputStream</code> to write to.
 	 * @param type The <code>MatlabDataType</code> of the element for which to
 	 * 		write the tag.
 	 * @param bytes The length (in bytes) of the element (excluding the tag).
 	 * @throws IOException if writing to <code>out</code> fails.
 	 */
 	private static final void writeElementTagTo(DataOutputStream out, MatlabDataType type, int bytes) throws IOException {
 
 		/* IMPORTANT: COMPRESSED elements are not byte aligned.  This is NOT in
 		 * the MAT-file format documentation.  This was determined through
 		 * reverse engineering of MAT-files generated from MATLAB.
 		 */
 		if (type != MatlabDataType.COMPRESSED) {
 			align(out);
 		}
 
 		out.writeInt(type.value);
 		out.writeInt(bytes);
 
 	}
 
 	/**
 	 * Writes padding (zeros) to the specified <code>DataOutputStream</code>.
 	 * @param out The <code>DataOutputStream</code> to write to.
 	 * @param amount The length (in bytes) of the padding to write.
 	 * @throws IOException if writing to <code>out</code> fails.
 	 */
 	private static void writePaddingTo(DataOutputStream out, int amount) throws IOException {
 		assert(amount >= 0);
 
 		if (amount > 0) {
 			out.write(new byte[amount]);
 		}
 	}
 
 	/**
 	 * Writes padding (zeros) to the specified position in the underlying
 	 * stream.
 	 * @param position The position within the underlying stream to pad to.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writePaddingTo(int position) throws IOException {
 		this.writePadding(position - this.stream().size());
 		assert(this.stream().size() == position);
 	}
 
 	/**
 	 * Writes padding (zeros) to the underlying stream.
 	 * @param amount The length (in bytes) of the padding to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writePadding(int amount) throws IOException {
 		assert(amount >= 0);
 
 		if (amount > 0) {
 			this.stream().write(new byte[amount]);
 		}
 	}
 
 	/**
 	 * Aligns the specified <code>DataOutputStream</code> for writing the next
 	 * element.
 	 * @param out The <code>DataOutputStream</code> to align.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private static void align(DataOutputStream out) throws IOException {
 		int n = out.size() % MATLAB_BYTE_ALIGNMENT;
 		if (n > 0) {
 			writePaddingTo(out, MATLAB_BYTE_ALIGNMENT - n);
 		}
 	}
 
 	/**
 	 * Writes the MATLAB MAT-file header to the underlying stream.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeHeader() throws IOException {
 
 		assert(this.stream().size() == 0);
 
 		String description = String.format(HEADER_FORMAT_STRING, new Date().toString());
 		this.stream().writeBytes(description);
 
 		assert(this.stream().size() < MATLAB_DESCRIPTION_SIZE);
 		this.writePaddingTo(MATLAB_DESCRIPTION_SIZE);
 
 		this.stream().writeShort(MATLAB_FILE_VERSION);
 		this.stream().writeShort(MATLAB_ENDIAN_INDICATOR);
 
 		assert(this.stream().size() == MATLAB_HEADER_SIZE);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeBoolean(boolean)
 	 */
 	public void writeBoolean(boolean arg0) throws IOException {
 		this.stream().writeBoolean(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>boolean</code> values to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeBooleans(boolean[] array) throws IOException {
 		this.writeBooleans(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>boolean</code> values to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeBooleans(boolean[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeBoolean(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>boolean</code> values to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeBooleans(boolean[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null || strides == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeBooleans(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeBooleans(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>boolean</code> values to the underlying stream.
 	 * @param array The array of <code>boolean</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeBooleans(boolean[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeBooleans(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeBoolean(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeByte(int)
 	 */
 	public void writeByte(int arg0) throws IOException {
 		this.stream().writeByte(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>byte</code> values to the underlying stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeBytes(byte[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeBytes(array, 0, dims.length - 1, dims, strides);
 		} else {
 			write(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>byte</code> values to the underlying stream.
 	 * @param array The array of <code>byte</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeBytes(byte[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeBytes(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeByte(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeBytes(java.lang.String)
 	 */
 	public void writeBytes(String arg0) throws IOException {
 		this.stream().writeBytes(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeChar(int)
 	 */
 	public void writeChar(int arg0) throws IOException {
 		this.stream().writeChar(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeChars(java.lang.String)
 	 */
 	public void writeChars(String arg0) throws IOException {
 		this.stream().writeChars(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeDouble(double)
 	 */
 	public void writeDouble(double arg0) throws IOException {
 		this.stream().writeDouble(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>double</code> values to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeDoubles(double[] array) throws IOException {
 		this.writeDoubles(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>double</code> values to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeDoubles(double[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeDouble(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>double</code> values to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeDoubles(double[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeDoubles(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeDoubles(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>double</code> values to the underlying stream.
 	 * @param array The array of <code>double</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeDoubles(double[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeDoubles(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeDouble(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeFloat(float)
 	 */
 	public void writeFloat(float arg0) throws IOException {
 		this.stream().writeFloat(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>float</code> values to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeFloats(float[] array) throws IOException {
 		this.writeFloats(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>float</code> values to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeFloats(float[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeFloat(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>float</code> values to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeFloats(float[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeFloats(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeFloats(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>float</code> values to the underlying stream.
 	 * @param array The array of <code>float</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeFloats(float[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeFloats(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeFloat(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeInt(int)
 	 */
 	public void writeInt(int arg0) throws IOException {
 		this.stream().writeInt(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>int</code> values to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeInts(int[] array) throws IOException {
 		this.writeInts(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>int</code> values to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeInts(int[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeInt(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>int</code> values to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeInts(int[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeInts(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeInts(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>int</code> values to the underlying stream.
 	 * @param array The array of <code>int</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeInts(int[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeInts(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeInt(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeLong(long)
 	 */
 	public void writeLong(long arg0) throws IOException {
 		this.stream().writeLong(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>long</code> values to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeLongs(long[] array) throws IOException {
 		this.writeLongs(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>long</code> values to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeLongs(long[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeLong(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>long</code> values to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeLongs(long[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeLongs(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeLongs(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>long</code> values to the underlying stream.
 	 * @param array The array of <code>long</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeLongs(long[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeLongs(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeLong(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeShort(int)
 	 */
 	public void writeShort(int arg0) throws IOException {
 		this.stream().writeShort(arg0);
 	}
 
 	/**
 	 * Writes an array of <code>short</code> values to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeShorts(short[] array) throws IOException {
 		this.writeShorts(array, 0, array.length);
 	}
 
 	/**
 	 * Writes an array of <code>short</code> values to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param len The number of elements of <code>array</code> to write.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeShorts(short[] array, int ofs, int len) throws IOException {
 		for (int i = 0; i < len; i++) {
 			this.writeShort(array[ofs + i]);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>short</code> values to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	public void writeShorts(short[] array, int[] dims, int[] strides) throws IOException {
 		if (strides != null) {
 			if (dims == null) {
 				throw new IllegalArgumentException("dims == null || strides == null");
 			} else if (dims.length != strides.length) {
 				throw new IllegalArgumentException("dims.length != strides.length");
 			} else if (dims.length == 0) {
 				throw new IllegalArgumentException("dims.length == 0");
 			}
 			writeShorts(array, 0, dims.length - 1, dims, strides);
 		} else {
 			writeShorts(array);
 		}
 	}
 
 	/**
 	 * Writes an array of <code>short</code> values to the underlying stream.
 	 * @param array The array of <code>short</code> values to write.
 	 * @param ofs The index of the first element in <code>array</code> to
 	 * 		write.
 	 * @param d The dimension being written.
 	 * @param dims The dimensions of the array.
 	 * @param strides The distance between consecutive array entries along each
 	 * 		dimension.
 	 * @throws IOException if writing to the underlying stream fails.
 	 */
 	private void writeShorts(short[] array, int ofs, int d, int[] dims, int[] strides) throws IOException {
 		int dim = dims[d];
 		int stride = strides[d];
 		if (d > 0) {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeShorts(array, ofs, d - 1, dims, strides);
 			}
 		} else {
 			for (int i = 0; i < dim; i++, ofs += stride) {
 				writeShort(array[ofs]);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataOutput#writeUTF(java.lang.String)
 	 */
 	public void writeUTF(String arg0) throws IOException {
 		this.stream().writeUTF(arg0);
 	}
 
 	/**
 	 * Gets the total number of bytes written to this
 	 * <code>MatlabOutputStream</code>.
 	 * @return The total number of bytes written to this
 	 * 		<code>MatlabOutputStream</code>.
 	 */
 	public int size() {
 		int total = 0;
 		for (DataOutputStream out : this.streams) {
 			total += out.size();
 		}
 		return total;
 	}
 
 	/**
 	 * Gets the <code>DataOutputStream</code> at the top of the
 	 * <code>streams</code> stack.
 	 * @return The <code>DataOutputStream</code> at the top of the
 	 * 		<code>streams</code> stack.
 	 * @see #streams
 	 */
 	private DataOutputStream stream() {
 		return this.streams.peek();
 	}
 
 	/** The byte interval on which to align data elements. */
 	private static final int MATLAB_BYTE_ALIGNMENT = 8;
 
 	/** The format string for the MATLAB MAT-file header. */
 	private static final String HEADER_FORMAT_STRING = "MATLAB 5.0 MAT-file, Platform: JAVA, Created on: %s, Created by: jMIST.";
 
 	/**
 	 * The MAT-file version (The MAT-file format documentation indicates that
 	 * third parties should write <code>0x0100</code> for this value).
 	 */
 	private static final int MATLAB_FILE_VERSION = 0x0100;
 
 	/** The MAT-file endian indicator. */
 	private static final int MATLAB_ENDIAN_INDICATOR = 0x4D49;
 
 	/** The size of the MAT-file description block in the header</code>. */
 	private static final int MATLAB_DESCRIPTION_SIZE = 124;
 
 	/** The total size of the MAT-file header. */
 	private static final int MATLAB_HEADER_SIZE = 128;
 
 	/**
 	 * An <code>OutputStream</code> adapter that wraps an
 	 * <code>ImageOutputStream</code>.
 	 * @author Brad Kimmel
 	 */
 	private static class ImageOutputStreamAdapter
 		extends OutputStream {
 
 		/**
 		 * Creates a new <code>ImageOutputStreamAdapter</code>.
 		 * @param out The <code>ImageOutputStream</code> to wrap.
 		 */
 		public ImageOutputStreamAdapter(ImageOutputStream out) {
 			this.out = out;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.io.OutputStream#write(int)
 		 */
 		@Override
 		public void write(int b) throws IOException {
 			this.out.write(b);
 		}
 
 		/* (non-Javadoc)
 		 * @see java.io.OutputStream#close()
 		 */
 		@Override
 		public void close() throws IOException {
 			this.out.close();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.io.OutputStream#flush()
 		 */
 		@Override
 		public void flush() throws IOException {
 			this.out.flush();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.io.OutputStream#write(byte[], int, int)
 		 */
 		@Override
 		public void write(byte[] b, int off, int len) throws IOException {
 			this.out.write(b, off, len);
 		}
 
 		/* (non-Javadoc)
 		 * @see java.io.OutputStream#write(byte[])
 		 */
 		@Override
 		public void write(byte[] b) throws IOException {
 			this.out.write(b);
 		}
 
 		/** The <code>ImageOutputStream</code> being decorated. */
 		private final ImageOutputStream out;
 
 	}
 
 	/**
 	 * An abstract <code>DataOutputStream</code> that can transfer its contents
 	 * to another <code>DataOutputStream</code>.
 	 * @author Brad Kimmel
 	 */
 	private static abstract class CompoundElementOutputStream
 		extends DataOutputStream {
 
 		/**
 		 * Creates a new <code>CompoundElementOutputStream</code>.
 		 * @param out The <code>OutputStream</code> to write to before the
 		 * 		contents are transferred.
 		 */
 		protected CompoundElementOutputStream(OutputStream out) {
 			super(out);
 		}
 
 		/**
 		 * Creates a new <code>CompoundElementOutputStream</code> for writing
 		 * an element of the specified <code>MatlabDataType</code>.
 		 * @param type The <code>MatlabDataType</code> of the element to be
 		 * 		written to the stream.
 		 * @return The new <code>CompoundElementOutputStream</code>.
 		 * @throws IOException if writing to the underlying stream fails.
 		 */
 		public static CompoundElementOutputStream create(MatlabDataType type) throws IOException {
 			if (type == MatlabDataType.COMPRESSED) {
 				return new CompressedCompoundElementOutputStream(new ByteArrayOutputStream());
 			} else {
 				return new BufferedCompoundElementOutputStream(type, new ByteArrayOutputStream());
 			}
 		}
 
 		/**
 		 * Creates a new <code>CompoundElementOutputStream</code> for writing
 		 * an element of known length of the specified
 		 * <code>MatlabDataType</code>.
 		 * @param type The <code>MatlabDataType</code> of the element to be
 		 * 		written to the stream.
 		 * @param bytes The length (in bytes) of the element to be written (not
 		 * 		including the header).
 		 * @param out The <code>DataOutputStream</code> to write to.
 		 * @return The new <code>CompoundElementOutputStream</code>.
 		 * @throws IOException if writing to the underlying stream fails.
 		 */
 		public static CompoundElementOutputStream create(MatlabDataType type, int bytes, DataOutputStream out) throws IOException {
 			return new FixedLengthCompoundElementOutputStream(out, type, bytes);
 		}
 
 		/**
 		 * Writes the the contents written to this
 		 * <code>CompoundElementOutputStream</code> to the specified
 		 * <code>DataOutputStream</code>, if necessary.
 		 * @param out The <code>DataOutputStream</code> to transfer the
 		 * 		contents of this stream to.
 		 * @throws IOException if writing to <code>out</code> fails.
 		 */
 		public abstract void writeTo(DataOutputStream out) throws IOException;
 
 	}
 
 	/**
 	 * A <code>CompoundElementOutputStream</code> that writes to a temporary
 	 * <code>ByteArrayOutputStream</code>.  This is to be used for compound
 	 * elements in cases where the length of the element is not known until
 	 * the element contents have been written.  The element contents are
 	 * written to a temporary buffer.  When {@link #writeTo(DataOutputStream)}
 	 * is called, the tag is written and the contents of the temporary buffer
 	 * are transferred.
 	 * @author Brad Kimmel
 	 */
 	private static final class BufferedCompoundElementOutputStream
 		extends CompoundElementOutputStream {
 
 		/**
 		 * Creates a new <code>BufferedCompoundElementOutputStream</code>.
 		 * @param type The <code>MatlabDataType</code> of the element to write.
 		 * @param bytes The temporary <code>ByteArrayOutputStream</code> to
 		 * 		write the element contents to.
 		 * @throws IOException if writing to a stream fails.
 		 */
 		public BufferedCompoundElementOutputStream(MatlabDataType type, ByteArrayOutputStream bytes) throws IOException {
 			super(bytes);
 			this.type = type;
 			this.bytes = bytes;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.util.matlab.MatlabOutputStream.CompoundElementOutputStream#writeTo(java.io.DataOutputStream)
 		 */
 		@Override
 		public void writeTo(DataOutputStream out) throws IOException {
 
 			/* From "MATLAB 7 MAT-File Format", Page 1-10:
 			 *
 			 * "For data elements representing MATLAB arrays, (type miMATRIX),
 			 * the value of the Number of Bytes field includes padding bytes in
 			 * the total.  For all other MAT-file data types, the value of the
 			 * Number of Bytes field does not include padding bytes."
 			 */
 			if (type == MatlabDataType.MATRIX) {
 				align(this);
 			}
 
 			writeElementTagTo(out, this.type, bytes.size());
 			this.bytes.writeTo(out);
 			out.flush();
 
 		}
 
 		/** The <code>MatlabDataType</code> of the element to write. */
 		private final MatlabDataType type;
 
 		/**
 		 * The temporary <code>ByteArrayOutputStream</code> to write the
 		 * element contents to.
 		 */
 		private final ByteArrayOutputStream bytes;
 
 	}
 
 	/**
 	 * A <code>CompoundElementOutputStream</code> that compresses its contents
 	 * using GZIP compression before it is written to another stream.
 	 * @author Brad Kimmel
 	 */
 	private static final class CompressedCompoundElementOutputStream
 		extends CompoundElementOutputStream {
 
 		/**
 		 * Creates a new <code>CompressedCompoundElementOutputStream</code>.
 		 * @param bytes The temporary <code>ByteArrayOutputStream</code> to
 		 * 		write the contents of
 		 * @throws IOException if writing to a stream fails.
 		 */
 		public CompressedCompoundElementOutputStream(ByteArrayOutputStream bytes) throws IOException {
 			super(new DeflaterOutputStream(bytes));
 			this.bytes = bytes;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.util.matlab.MatlabOutputStream.CompoundElementOutputStream#writeTo(java.io.DataOutputStream)
 		 */
 		@Override
 		public void writeTo(DataOutputStream out) throws IOException {
 
 			this.flush();
 
 			DeflaterOutputStream deflater = (DeflaterOutputStream) this.out;
 			deflater.finish();
 
 			writeElementTagTo(out, MatlabDataType.COMPRESSED, bytes.size());
 			bytes.writeTo(out);
 			out.flush();
 
 		}
 
 		/** The temporary <code>ByteArrayOutputStream</code> to write to. */
 		private final ByteArrayOutputStream bytes;
 
 	}
 
 	/**
 	 * A <code>CompoundElementOutputStream</code> that writes data written to
 	 * it directly to another stream.  This should be used when the length of
 	 * the element being written is known in advance.  It will be verified that
 	 * the number of bytes written to this stream matches what was indicated in
 	 * the constructor (after alignment for <code>MATRIX</code> elements).
 	 * @author Brad Kimmel
 	 */
 	public static final class FixedLengthCompoundElementOutputStream
 		extends CompoundElementOutputStream {
 
 		/**
 		 * Creates a new <code>FixedLengthCompoundElementOutputStream</code>.
 		 * @param out The <code>DataOutputStream</code> to write to.
 		 * @param type The <code>MatlabDataType</code> of the element to be
 		 * 		written.
 		 * @param bytes The length (in bytes) of the element to be written (not
 		 * 		including the tag).
 		 * @throws IOException if writing to <code>out</code> fails.
 		 */
 		public FixedLengthCompoundElementOutputStream(DataOutputStream out, MatlabDataType type, int bytes) throws IOException {
 			super(out);
 			writeElementTagTo(out, type, bytes);
 			this.type = type;
 			this.endPosition = out.size() + bytes;
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.util.matlab.MatlabOutputStream.CompoundElementOutputStream#writeTo(java.io.DataOutputStream)
 		 */
 		@Override
 		public void writeTo(DataOutputStream out) throws IOException {
 
 			/* From "MATLAB 7 MAT-File Format", Page 1-10:
 			 *
 			 * "For data elements representing MATLAB arrays, (type miMATRIX),
 			 * the value of the Number of Bytes field includes padding bytes in
 			 * the total.  For all other MAT-file data types, the value of the
 			 * Number of Bytes field does not include padding bytes."
 			 */
 			if (this.type == MatlabDataType.MATRIX) {
 				align(out);
 			}
 
 			out.flush();
 
 			if (out.size() != endPosition) {
				throw new IllegalStateException(String.format("incorrect array length, current position is %d (should be %d).", out.size(), this.endPosition));
 			}
 
 		}
 
 		/** The <code>MatlabDataType</code> of the element to write. */
 		private final MatlabDataType type;
 
 		/**
 		 * The expected position in the underlying stream after the element is
 		 * written.
 		 */
 		private final int endPosition;
 
 	}
 
 	/** The bit flag representing a complex array. */
 	private static final byte MATLAB_ARRAY_COMPLEX = 0x08;
 
 	/** The bit flag representing a global array. */
 	private static final byte MATLAB_ARRAY_GLOBAL = 0x04;
 
 	/** The bit flag representing an array to be used for logical indexing. */
 	private static final byte MATLAB_ARRAY_LOGICAL = 0x02;
 
 	/** The index into the array flags element of the flags bitmask. */
 	private static final int MATLAB_ARRAY_FLAGS_INDEX = 2;
 
 	/** The index into the array flags element of the array class indicator. */
 	private static final int MATLAB_ARRAY_CLASS_INDEX = 3;
 
 	/**
 	 * The size (in bytes) of the array flags element (not including the tag).
 	 */
 	private static final int MATLAB_ARRAY_FLAGS_SIZE = 8;
 
 	/** The size of a tag. */
 	private static final int MATLAB_TAG_SIZE = 8;
 
 	/**
 	 * The <code>Stack</code> of <code>DataOutputStream</code>s to write to.
 	 * <code>DataOutputStream</code> are pushed onto this stack as necessary
 	 * to write MATLAB data elements and then the contents transferred to the
 	 * stream below when the <code>DataOutputStream</code> is popped.  This is
 	 * so that, in some cases, the element can be written to a temporary buffer
 	 * and then transferred to the underlying stream when the size of the
 	 * element is known.
 	 */
 	private final Stack<DataOutputStream> streams;
 
 }
