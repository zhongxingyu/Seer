 /*
  * Copyright (C) 2013 SCALITY SA. All rights reserved.
  * http://www.scality.com
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY SCALITY SA ``AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SCALITY SA OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation
  * are those of the authors and should not be interpreted as representing
  * official policies, either expressed or implied, of SCALITY SA.
  *
  * https://github.com/scality/CaDMIum
  */
 package com.scality.cdmi.connector;
 
 import java.io.FileNotFoundException;
 import java.io.OutputStream;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.util.ByteArrayBuffer;
 import org.apache.http.util.EntityUtils;
 
 import com.scality.cdmi.api.CdmiConnectionException;
 import com.scality.cdmi.impl.metadata.CdmiMetadata;
 import com.scality.cdmi.impl.metadata.CdmiMetadataReader;
 
 /**
  * An {@link OutputStream} implementation for writing CDMI data objects. It
  * stores the data in an internal buffer until
  * {@link CdmiOutputStream#maxPutSize} bytes are stored before sending out the
  * actual PUT request to the CDMI server. NonCDMI operations are used for better
  * performance. The actual PUT request to the CDMI server is done in a separate
  * thread to allow continuously filling the buffer.
  * 
  * @author ziad.bizri@ezako.com for Scality
  */
 public class CdmiOutputStream extends OutputStream {
 	private CdmiConnector connector;
 	/**
 	 * The maximum content length, in bytes, of each request to the CDMI server.
 	 */
 	private final int maxPutSize;
	private final int maxPutThreads;
 	private String path;
 	private ByteArrayBuffer buffer;
 	private int pos_in_buffer;
 	private long pos_in_target;
 	private boolean closed;
 	private ThreadPoolExecutor executor;
 	private CdmiMetadataReader metareader;
 
 	private static void writeOut(CdmiConnector connector, String path,
 			long offset, byte[] data) throws CdmiConnectionException {
 		HttpResponse response = connector.updateObjectNonCdmi(path, offset,
 				data.length, data);
 		int status = response.getStatusLine().getStatusCode();
 		if (HttpStatus.SC_NO_CONTENT != status
 				&& HttpStatus.SC_CREATED != status) {
 			throw new CdmiConnectionException("Impossible to write path "
 					+ path + " at offset " + offset + " length " + data.length
 					+ " got response " + response.getStatusLine());
 		}
 		EntityUtils.consumeQuietly(response.getEntity());
 	}
 
 	private class PutThread extends Thread {
 		private CdmiConnector connector;
 		private long offset;
 		private byte[] data;
 
 		public PutThread(CdmiConnector connector, long offset, byte[] data) {
 			this.connector = connector;
 			this.offset = offset;
 			this.data = data;
 		}
 
 		@Override
 		public void run() {
 			try {
 				writeOut(connector, path, offset, data);
 			} catch (CdmiConnectionException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	public CdmiOutputStream(String path, long offset, CdmiConnector connector,
 			int maxPutSize, int maxPutThreads) throws CdmiConnectionException {
 		this.connector = connector;
 		this.maxPutSize = maxPutSize;
		this.maxPutThreads = maxPutThreads;
 		this.path = path;
 		this.buffer = null;
 		this.pos_in_buffer = 0;
 		this.pos_in_target = offset;
 		this.closed = false;
 		this.buffer = new ByteArrayBuffer(maxPutSize);
 		if (connector.isMultiThreaded()) {
 			// FIXME: remove hardcoded value.
 			executor = new ThreadPoolExecutor(maxPutThreads, maxPutThreads, 0L,
 					TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
 							maxPutThreads),
 					new ThreadPoolExecutor.CallerRunsPolicy());
 		} else {
 			executor = null;
 		}
 		metareader = new CdmiMetadataReader(this.connector);
 	}
 
 	/**
 	 * A default constructor that should only be used in tests.
 	 */
 	protected CdmiOutputStream() {
 		maxPutSize = 1;
		maxPutThreads = 1;
 		// For tests.
 	}
 
 	private void reinit() {
 		buffer.clear();
 		pos_in_buffer = 0;
 	}
 
 	@Override
 	public void write(byte[] b, int offset, int length)
 			throws CdmiConnectionException {
 		if (b == null) {
 			throw new NullPointerException("Source buffer cannot be null");
 		}
 		if (offset < 0) {
 			throw new IndexOutOfBoundsException("Offset cannot be negative");
 		}
 		if (length < 0) {
 			throw new IndexOutOfBoundsException("Length cannot be negative");
 		}
 		if (offset + length > b.length) {
 			throw new IndexOutOfBoundsException(
 					"Offset + length cannot be greater than buffer liength");
 		}
 		if (closed) {
 			throw new CdmiConnectionException("Output stream is already closed");
 		}
 
 		int remaining = length;
 		while (remaining > 0) {
 			int towrite = Math.min(remaining, maxPutSize - pos_in_buffer);
 			buffer.append(b, offset, towrite);
 			pos_in_buffer += towrite;
 			if (pos_in_buffer == maxPutSize) {
 				writeout();
 			}
 			remaining -= towrite;
 			offset += towrite;
 		}
 	}
 
 	@Override
 	public void write(byte[] b) throws CdmiConnectionException {
 		write(b, 0, b.length);
 	}
 
 	@Override
 	public void write(int b) throws CdmiConnectionException {
 		if (closed) {
 			throw new CdmiConnectionException("Output stream is already closed");
 		}
 
 		int towrite = b % 256;
 		buffer.append(towrite);
 		if (++pos_in_buffer == maxPutSize) {
 			writeout();
 		}
 	}
 
 	@Override
 	public void flush() throws CdmiConnectionException {
 		if (closed) {
 			throw new CdmiConnectionException("Output stream is already closed");
 		}
 		writeout();
 
 		if (executor != null) {
 			executor.shutdown();
 			while (!executor.isTerminated()) {
 			}
			executor = new ThreadPoolExecutor(maxPutThreads, maxPutThreads, 0L,
 					TimeUnit.MILLISECONDS,
					new ArrayBlockingQueue<Runnable>(maxPutThreads),
 					new ThreadPoolExecutor.CallerRunsPolicy());
 		}
 
 		try {
 			CdmiMetadata meta = metareader.readMetadata(path);
 			connector.forceFlush(path, meta.getSize());
 		} catch (FileNotFoundException e) {
 			// Hard to believe that it can happen.
 			throw new CdmiConnectionException(e);
 		}
 	}
 
 	/**
 	 * Close the associated stream. close() can be called multiple times on the
 	 * same stream.
 	 */
 	@Override
 	public void close() throws CdmiConnectionException {
 		if (!closed) {
 			flush();
 		}
 		closed = true;
 	}
 
 	private void writeout() throws CdmiConnectionException {
 		int length = buffer.length();
 		if (length > 0) {
 			if (connector.isMultiThreaded()) {
 				executor.execute(new PutThread(connector, pos_in_target, buffer
 						.toByteArray()));
 			} else {
 				writeOut(connector, path, pos_in_target, buffer.toByteArray());
 			}
 			pos_in_target += length;
 			reinit();
 		}
 	}
 }
