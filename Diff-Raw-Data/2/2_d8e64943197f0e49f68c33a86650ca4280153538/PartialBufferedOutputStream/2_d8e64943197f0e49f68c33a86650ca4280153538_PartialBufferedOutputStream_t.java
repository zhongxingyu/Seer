 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.util;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * <b>PartialBufferedOutputStream</b><br>
  * Oct 19, 2005<br>
  * 
  * <b>Purpose:</b><br>
  * I have modelled this class after the BufferedOutputStream. Several methods are
  * synchronized as a result. The interior of this class uses a ByteArrayOutputStream 
  * when it starts and then a BufferedOutputStream. This is essentially a decorator.
  * 
  * This class uses a temporary to-memory OutputStream for initial bufferring. This 
  * OutputStream is a ByteArrayOutputStream. Once a pre-defined BUFFER_SIZE has been 
  * reached, the output that is stored in the ByteArrayOutputStream is output to the 
  * real OutputStream to the response. Further data is then written to that response 
  * OutputStream.
  * 
  * For the first run of this, we will write to memory for the buffered part. Writing to 
  * disk is another option.
  * NOTE: If we switch to writing out to disk, we will have to clean up our temporary 
  * files with abort().
  * 
  * WARNING: IF you abuse the size of the buffer, you may leak memory if this OutputStream 
  * is not terminated. The ByteArrayOutputStream will hold onto its allocated memory even after 
  * you call reset() on it. So if you put in 60MB of buffer space, it will stay there until 
  * the object is no longer referenced and the garbage collector decides to pick it up. A
  * solution to this would be to use a FileOutputStream instead of the ByteArrayOutputStream.
  * 
  * CONTRACT:
  * -close() will NOT flush remaining bytes to the output stream, as per the contract 
  * with OutputStream. Close will remove any references to OutputStreams and its 
  * HttpServletResponse.
  * -flush() will flush the buffer first and then any information stored in the underlying 
  * OutputStream. Everything will be flushed when flush is called.
  * -abort() will succeed if the buffer is not full yet. If the buffer has been filled and
  * the information has been written out to response's OutputStream, the abort reports as
  * having failed. The point of this PartialBufferedOutputStream is to allow an abort before 
  * the information has been written out to the real OutputStream. When abort is called the
  * references to any OutputStreams and the HttpServletResponse are removed.
  * 
  * @author Brent Owens (The Open Planning Project)
  * @version 
  */
 public class PartialBufferedOutputStream extends OutputStream
 {
 	public static final int DEFAULT_BUFFER_SIZE = 50;
 	
 	/** the number of bytes in a kilobyte */
 	private final int KILOBYTE = 1024;
 	
 	/** Buffer size for the temporary output stream */
     private int BUFFER_SIZE = KILOBYTE;
     
     /** Whether we should start streaming to the user or not */
     private boolean sendToUser = false;
 
     /** Temporary output stream, the buffered one */
     private ByteArrayOutputStream out_buffer;
     
     /** Response output stream, the non-buffered one, this is passed in by the response */
     private OutputStream out_real;
     
     /** This contains the OutputStream that will be put in out_real when the buffer is full */
     private HttpServletResponse response;
     
     /** Set to true when close() is called to prevent further writing */
     private boolean closed = false;
     
 	/**
 	 * Constructor Defaults buffer size to 50KB
 	 * @param response
 	 */
 	public PartialBufferedOutputStream(HttpServletResponse response)
 	{
 		this(response, DEFAULT_BUFFER_SIZE);	// default to 50KB
 	}
 	
 	/**
 	 * @param response the response with its output stream to write to once the buffer is full
 	 * @param kilobytes size, in kilobytes, of the buffer
 	 */
 	public PartialBufferedOutputStream(HttpServletResponse response, int kilobytes)
 	{
 		if (kilobytes < 1) 
 	        throw new IllegalArgumentException("Buffer size not greater than 0: " + kilobytes);
 		
 		BUFFER_SIZE = KILOBYTE * kilobytes;
 		this.response = response;
 		out_buffer = new ByteArrayOutputStream(BUFFER_SIZE);
 	}
 
 	/**
 	 * <b>bufferCapacity</b><br>
 	 * <br>
 	 * <b>Description:</b><br>
 	 * This will return the max capacity of the buffer in kilobytes.
 	 * 
 	 * @return the capacity of the buffer in kilobytes
 	 */
 	public int bufferCapacity()
 	{
 		return BUFFER_SIZE/KILOBYTE;
 	}
 	
 	/**
 	 * <b>bufferSize</b><br>
 	 * <br>
 	 * <b>Description:</b><br>
 	 * This will return the size of the buffer, in bytes.
 	 * 
 	 * @return the size of the buffer in bytes
 	 */
 	public int bufferSize()
 	{
 		return out_buffer.size();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.io.OutputStream#write(int)
 	 * 
 	 * Remember that b is treated as a byte, the 8 low-order bits are read, 
 	 * and the 24 remaining high-order bits of b are ignored.
 	 */
 	public synchronized void write(int b) throws IOException 
 	{
 		if (closed)
 			return;
 		
 		// if we have reached our limit, stream back to the user
 		if (sendToUser || out_buffer.size() >= BUFFER_SIZE)
 		{
 			if (sendToUser)	// if we are already streaming data to the user
 			{
 				// continue sending data to the user's response stream
 				out_real.write(b);
 			}
 			else // we aren't streaming to the user yet, so start
 			{
 				sendToUser = true;
 				out_real = new BufferedOutputStream(response.getOutputStream());
 				// copy data from out_buffer to out_real
 				out_buffer.writeTo(out_real);
 				out_buffer.reset();
 				out_real.write(b);
 			}
 		}
 		else	// we have not reached our buffer limit yet, so stream to the buffer
 		{
 			out_buffer.write(b);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.io.OutputStream#flush()
 	 * 
 	 * Flush when the user says to. Even if the buffer is not maxed out yet.
 	 * We need to revisit this.
 	 */
 	public synchronized void flush() throws IOException 
 	{
 		if (closed)
 			return;
 		
 		if (out_buffer != null && out_buffer.size() > 0 && !sendToUser)
 		{
			if (out_real == null)
				out_real = new BufferedOutputStream(response.getOutputStream());
 			out_buffer.writeTo(out_real);
 			out_buffer = null;
 		}
 		
 		if (out_real != null)
 			out_real.flush();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.io.OutputStream#close()
 	 */
 	public void close() throws IOException 
 	{
 		if (closed)
 			return;
 		
 		closed = true;
 		
 		// It is not in the contract of close, for OutputStream, to call flush(), 
 		//  so I am dyking it out.
 		/*
 		try {
 			flush();
 		} 
 		catch (IOException ignored) {
 		}
 		*/
 		
 		out_buffer.close();
 		out_buffer = null;
 		out_real = null;	// get rid of our local pointer
 		response = null;	// get rid of our local pointer
 		//if (out_real != null)	// removed so the user has to close their stream
 		//	out_real.close();
     }
 	
 	
 	/**
 	 * <b>abort</b><br>
 	 * <br>
 	 * <b>Description:</b><br>
 	 * Abort is called when something bad has happened and we want to get out of
 	 * writing out to the real stream. This is why we have a buffer.
 	 * Abort will succeed if the buffer is not full yet. If that is true, then the 
 	 * buffer is cleared and closed.
 	 * 
 	 * It does NOT close the response's OutputStream
 	 * 
 	 * @throws IOException
 	 */
 	public boolean abort() throws IOException
 	{
 		if (out_buffer != null && out_buffer.size() < BUFFER_SIZE)
 		{
 			out_buffer.close();
 			out_buffer = null;
 			out_real = null;	// get rid of our local pointer
 			response = null;	// get rid of our local pointer
 			return true;	// success
 		}
 		
 		out_real = null;	// get rid of our local pointer
 		response = null;	// get rid of our local pointer
 		return false;	// buffer already full, sorry
 	}
 	
 }
