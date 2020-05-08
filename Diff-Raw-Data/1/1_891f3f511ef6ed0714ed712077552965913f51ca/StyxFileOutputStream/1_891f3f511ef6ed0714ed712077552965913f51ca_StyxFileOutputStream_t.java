 package com.v2soft.styxlib.library;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.concurrent.TimeoutException;
 
 import com.v2soft.styxlib.library.core.Messenger;
 import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
 import com.v2soft.styxlib.library.exceptions.StyxException;
 import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
 import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
 import com.v2soft.styxlib.library.messages.base.StyxMessage;
 import com.v2soft.styxlib.library.types.ULong;
 
 public class StyxFileOutputStream extends OutputStream {
     private long mTimeout = StyxClientManager.DEFAULT_TIMEOUT;
 	private StyxFile mFile;
 	private byte[] buffer;
 	private int index;
 	private ULong offset = ULong.ZERO;
 	private int mIOUnit;
 	private StyxClientManager mManager;
 	
 	public StyxFileOutputStream(StyxClientManager manager, StyxFile file, int iounit)
 	{
 		mManager = manager;
 		mFile = file;
 		mIOUnit = iounit;
 	}
 	
 	public StyxFile getFile()
 	{
 		return mFile;
 	}
 	
 	public int getIOUnit()
 	{
 		return mIOUnit;
 	}
 	
 	private void writeBuffer() throws IOException, InterruptedException, StyxException, TimeoutException
 	{
 		StyxFile file = getFile();
 		ByteArrayInputStream is = new ByteArrayInputStream(buffer, 0, index);
 		
 		StyxTWriteMessage tWrite = new StyxTWriteMessage(mManager.getActiveTags().getTag(),
 				file.getFID(), offset, is);
 		
 		Messenger messenger = mManager.getMessenger();
 		messenger.send(tWrite);
 		StyxMessage rMessage = tWrite.waitForAnswer(mTimeout);
 		StyxErrorMessageException.doException(rMessage);
 		
 		StyxRWriteMessage rWrite = (StyxRWriteMessage) rMessage;
 		offset = offset.add(rWrite.getCount());
 	}
 	
 	@Override
 	public void write(byte[] b, int off, int len) throws IOException {
 		try
 		{
 			int iounit = getIOUnit();
 			for (int i=0; i<len; i++)
 			{
 				if (buffer != null && index >= buffer.length)
 				{
 					writeBuffer();
 					buffer = null;
 				}
 				
 				if (buffer == null)
 				{
 					buffer = new byte[iounit];
 					index = 0;
 				}
 				
 				buffer[index] = b[i + off];
 				index++;
 			}
 		} catch (IOException e)
 		{
 			throw e;
 		} catch (Exception e)
 		{
 			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
 		}
 	}
 
 	@Override
 	public void write(int b) throws IOException {
 		try
 		{
 			if (buffer != null && index >= buffer.length)
 			{
 				writeBuffer();
 				buffer = null;
 			}
 		
 			if (buffer == null)
 			{
 				buffer = new byte[getIOUnit()];
 				index = 0;
 			}
 			
 			buffer[index] = (byte) b;
 			index++;
 		} catch (IOException e)
 		{
 			throw e;
 		} catch (Exception e)
 		{
 			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
 		}
 	}
 	
 	@Override
 	public void flush() throws IOException {
 		try
 		{
 			writeBuffer();
 			super.flush();
 		} catch (IOException e)
 		{
 			throw e;
 		} catch (Exception e)
 		{
 			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
 		}
 	}
 
     public long getTimeout() {
         return mTimeout;
     }
 
     public void setTimeout(long mTimeout) {
         this.mTimeout = mTimeout;
     }
     
     @Override
     public void close() throws IOException {
	flush();
     	mFile.close();
     	super.close();
     }
 }
