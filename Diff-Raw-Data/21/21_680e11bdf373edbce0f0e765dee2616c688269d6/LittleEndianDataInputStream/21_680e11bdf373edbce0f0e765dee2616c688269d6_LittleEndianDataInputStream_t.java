 /*
  * Copyright (c) 2008 Bradley W. Kimmel
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
 
 package ca.eandb.util.io;
 
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * An <code>InputStream</code> that reads data in little-endian format.
  * @author Brad Kimmel
  */
 public final class LittleEndianDataInputStream extends InputStream implements
 		DataInput {

 	/** The <code>InputStream</code> to read from. */
 	private final DataInputStream inner;

 	/** Temporary work buffer to hold data. */
 	private final byte[] work = new byte[8];

 	/**
 	 * Creates a new <code>LittleEndianDataInputStream</code>.
 	 * @param inner The <code>InputStream</code> to read from.
 	 */
 	public LittleEndianDataInputStream(InputStream inner) {
 		this.inner = inner instanceof DataInputStream ? (DataInputStream) inner : new DataInputStream(inner);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readBoolean()
 	 */
 	@Override
 	public boolean readBoolean() throws IOException {
 		readFully(work, 0, 1);
 		return work[0] != 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readByte()
 	 */
 	@Override
 	public byte readByte() throws IOException {
 		readFully(work, 0, 1);
 		return work[0];
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readChar()
 	 */
 	@Override
 	public char readChar() throws IOException {
 		readFully(work, 0, 2);
 		return (char) ((work[1] << 8) | (work[0] & 0xff));
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readDouble()
 	 */
 	@Override
 	public double readDouble() throws IOException {
 		return Double.longBitsToDouble(readLong());
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readFloat()
 	 */
 	@Override
 	public float readFloat() throws IOException {
 		return Float.intBitsToFloat(readInt());
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readFully(byte[])
 	 */
 	@Override
 	public void readFully(byte[] b) throws IOException {
 		if (inner.read(b) < b.length) {
 			throw new EOFException();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readFully(byte[], int, int)
 	 */
 	@Override
 	public void readFully(byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int read = inner.read(b, off, len);
			if (read <= 0) {
				throw new EOFException();
			}
			off += read;
			len -= read;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readInt()
 	 */
 	@Override
 	public int readInt() throws IOException {
 		readFully(work, 0, 4);
 		return
 				(((int) work[0]) & 0xff) |
 				(((int) work[1]) & 0xff) << 8 |
 				(((int) work[2]) & 0xff) << 16 |
 				(((int) work[3]) & 0xff) << 24;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readLine()
 	 */
 	@Override
 	public String readLine() throws IOException {
 		return inner.readLine();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readLong()
 	 */
 	@Override
 	public long readLong() throws IOException {
 		readFully(work, 0, 8);
 		return
 				(((long) work[0]) & 0xffL) |
 				(((long) work[1]) & 0xffL) << 8 |
 				(((long) work[2]) & 0xffL) << 16 |
 				(((long) work[3]) & 0xffL) << 24 |
 				(((long) work[4]) & 0xffL) << 32 |
 				(((long) work[5]) & 0xffL) << 40 |
 				(((long) work[6]) & 0xffL) << 48 |
 				(((long) work[7]) & 0xffL) << 56;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readShort()
 	 */
 	@Override
 	public short readShort() throws IOException {
 		readFully(work, 0, 2);
 		return (short) (
 				(((int) work[0]) & 0xff) |
 				(((int) work[1]) & 0xff) << 8);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readUTF()
 	 */
 	@Override
 	public String readUTF() throws IOException {
 		return inner.readUTF();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readUnsignedByte()
 	 */
 	@Override
 	public int readUnsignedByte() throws IOException {
 		return ((int) readByte()) & 0xff;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#readUnsignedShort()
 	 */
 	@Override
 	public int readUnsignedShort() throws IOException {
 		return ((int) readShort()) & 0xffff;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.DataInput#skipBytes(int)
 	 */
 	@Override
 	public int skipBytes(int n) throws IOException {
 		return (int) inner.skip(n);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read()
 	 */
 	@Override
 	public int read() throws IOException {
 		return inner.read();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#available()
 	 */
 	@Override
 	public int available() throws IOException {
 		return inner.available();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#close()
 	 */
 	@Override
 	public void close() throws IOException {
 		inner.close();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#mark(int)
 	 */
 	@Override
 	public synchronized void mark(int readlimit) {
 		inner.mark(readlimit);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#markSupported()
 	 */
 	@Override
 	public boolean markSupported() {
 		return inner.markSupported();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read(byte[], int, int)
 	 */
 	@Override
 	public int read(byte[] b, int off, int len) throws IOException {
 		return inner.read(b, off, len);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read(byte[])
 	 */
 	@Override
 	public int read(byte[] b) throws IOException {
 		return inner.read(b);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#reset()
 	 */
 	@Override
 	public synchronized void reset() throws IOException {
 		inner.reset();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#skip(long)
 	 */
 	@Override
 	public long skip(long n) throws IOException {
 		return inner.skip(n);
 	}
 
 }
