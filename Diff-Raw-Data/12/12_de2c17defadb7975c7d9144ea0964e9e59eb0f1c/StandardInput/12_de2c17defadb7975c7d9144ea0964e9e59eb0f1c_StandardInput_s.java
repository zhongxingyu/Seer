 package pleocmd;
 
 import java.awt.EventQueue;
 import java.io.IOException;
 import java.io.InputStream;
 
 import pleocmd.itfc.gui.MainFrame;
 import pleocmd.pipe.data.DataQueue;
 
 /**
  * In console mode, the standard input gets simply wrapped by this class.<br>
  * In GUI mode, data coming from the GUI's {@link javax.swing.JTextField} will
  * be cached inside a ring buffer of this {@link StandardInput} to later be read
  * by {@link #read()}.
  * 
  * @author oliver
  * @see DataQueue
  */
 public final class StandardInput extends InputStream {
 
 	private static StandardInput stdin;
 
 	/**
 	 * This array represents a ring buffer.
 	 */
 	private byte[] buffer;
 
 	/**
 	 * The position of the next byte to read from {@link #buffer}.
 	 */
 	private int readPos;
 
 	/**
 	 * The position of the next byte to write to {@link #buffer}.
 	 */
 	private int writePos;
 
 	/**
 	 * Only true if the cache has been closed, i.e. the remaining data in
 	 * {@link #buffer} can still be read, but no new data can be put into the
 	 * {@link #buffer} and if {@link #readPos} catches up {@link #writePos}
 	 * {@link #available()} will return 0.
 	 */
 	private boolean closed;
 
 	private StandardInput() {
 		stdin = this;
 		resetCache();
 	}
 
 	/**
 	 * Returns or creates the singleton instance.
 	 * 
 	 * @return instance of {@link StandardInput}
 	 */
 	public static StandardInput the() {
 		if (stdin == null) new StandardInput();
 		return stdin;
 	}
 
 	/**
 	 * True if the input stream has been closed.
 	 * 
 	 * @return true if closed
 	 * @see #close()
 	 */
 	public synchronized boolean isClosed() {
 		return closed;
 	}
 
 	/**
 	 * Appends a "close" to the ring buffer in GUI mode and has no effect in
 	 * console mode.<br>
 	 * The remaining {@link #available()} data in the ring buffer can still be
 	 * {@link #read()} but no new data can be {@link #put(byte)} into it. After
 	 * no more data is {@link #available()} {@link #read()} throws an
 	 * {@link IOException}.<br>
 	 * Has no effect if the {@link StandardInput} is already closed.
 	 */
 	@Override
 	public void close() throws IOException {
 		if (MainFrame.hasGUI()) synchronized (this) {
 			Log.detail("Sending close to ring-buffer '%s'", this);
 			closed = true;
 		}
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				if (MainFrame.hasGUI()) MainFrame.the().updateState();
 			}
 		});
 	}
 
 	/**
 	 * Clears and (if currently closed) reopens the input stream.<br>
 	 * All data in the ring buffer not yet read will be lost.<br>
 	 * Has no effect in console mode.
 	 */
 	public void resetCache() {
 		synchronized (this) {
 			Log.detail("Resetting ring-buffer '%s'", this);
			buffer = new byte[1]; // TODO bigger default
 			readPos = 0;
 			writePos = 0;
 			closed = false;
 			Log.detail("Reset ring-buffer '%s'", this);
 		}
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				if (MainFrame.hasGUI()) MainFrame.the().updateState();
 			}
 		});
 	}
 
 	/**
 	 * Returns the number of bytes ready to read via {@link #read()}.<br>
 	 * Blocks until data is available or (in GUI mode) {@link #close()} or
 	 * {@link #resetCache()} has been called.<br>
 	 * In GUI mode, it should <b>not</b> be called from the GUI thread.
 	 * 
 	 * @return available bytes
 	 */
 	@Override
 	public int available() throws IOException {
 		if (MainFrame.hasGUI()) while (true) {
 			// check if read catches up write?
 			int avail;
 			synchronized (this) {
 				avail = (writePos - readPos) % buffer.length;
 			}
 			// Java's mod doesn't work as expected with negative numbers
 			if (avail < 0) avail += buffer.length;
 			if (avail > 0) {
 				Log.detail("%d bytes available in '%s'", avail, this);
 				return avail;
 			}
 			synchronized (this) {
 				if (closed)
 				// no need to wait any longer - there can never be any new data
 					return 0;
 			}
 			// block until data available
 			try {
 				Thread.sleep(30);
 			} catch (final InterruptedException e) {
 				throw new IOException("Interrupted while waiting for input", e);
 			}
 		}
 		return System.in.available(); // CS_IGNORE
 	}
 
 	/**
 	 * Reads one byte from the input stream or (in GUI mode) the ring buffer.<br>
 	 * Blocks until the byte is available.<br>
 	 * In GUI mode, it should <b>not</b> be called from the GUI thread.
 	 * 
 	 * @return the next byte
 	 */
 	@Override
 	public int read() throws IOException {
 		if (MainFrame.hasGUI()) {
 			while (true) {
 				// check if read catches up write?
 				synchronized (this) {
 					if (readPos != writePos) break;
					if (closed)
						throw new IOException("StandardInput is closed");
 				}
 				// block until data available
 				try {
 					Thread.sleep(30);
 				} catch (final InterruptedException e) {
 					throw new IOException(
 							"Interrupted while waiting for input", e);
 				}
 			}
 			synchronized (this) {
				final int b = buffer[readPos];
 				Log.detail("Read from %03d 0x%02X in '%s'", readPos, b, this);
 				readPos = (readPos + 1) % buffer.length;
 				return b;
 			}
 		}
 		return System.in.read(); // CS_IGNORE
 	}
 
 	/**
 	 * Puts one byte into the ringbuffer in GUI mode, so it can be read by
 	 * {@link #read()}.<br>
 	 * Should only be called in GUI mode and from the GUI thread.
 	 * 
 	 * @param b
 	 *            byte to put into the ring buffer
 	 * @throws IOException
 	 *             if the stream has been closed
 	 */
 	public synchronized void put(final byte b) throws IOException {
 		assert MainFrame.hasGUI();
 		if (closed) throw new IOException("StandardInput is closed");
 		buffer[writePos] = b;
 		Log.detail("Put at %03d 0x%02X in '%s'", writePos, b, this);
 		writePos = (writePos + 1) % buffer.length;
 		// check if write catches up read?
 		if (writePos == readPos) {
 			// we need to increase our ring buffer:
 			// we "insert space" between the current write and
 			// read position so writePos stays the same while
 			// readPos moves.
 			final byte[] newbuf = new byte[buffer.length * 2];
 			readPos += newbuf.length - buffer.length;
 			System.arraycopy(buffer, 0, newbuf, 0, writePos);
 			System.arraycopy(buffer, writePos, newbuf, readPos, buffer.length
 					- writePos);
 			buffer = newbuf;
 			Log.detail("Increased from '%s'", this);
 		}
 	}
 
 	/**
 	 * Puts a series of bytes into the ring buffer as an atomic operation (i.e.
 	 * completely synchronized).
 	 * 
 	 * @param bytes
 	 *            data to put into the ring buffer
 	 * @throws IOException
 	 *             if the stream has been closed
 	 * @see #put(byte)
 	 */
 	public synchronized void put(final byte[] bytes) throws IOException {
 		for (final byte b : bytes)
 			put(b);
 	}
 
 	@Override
 	public String toString() {
 		return String.format("cap: %d, read: %d, write: %d",
 				buffer == null ? -1 : buffer.length, readPos, writePos);
 	}
 
 }
