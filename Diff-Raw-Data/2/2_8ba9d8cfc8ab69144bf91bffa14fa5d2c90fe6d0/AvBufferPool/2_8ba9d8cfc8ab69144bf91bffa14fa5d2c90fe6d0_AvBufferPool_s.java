 package com.limelight.nvstream.av;
 
 import java.util.LinkedList;
 
 public class AvBufferPool {
 	private LinkedList<byte[]> bufferList = new LinkedList<byte[]>();
 	private int bufferSize;
 	
 	public AvBufferPool(int size)
 	{
 		this.bufferSize = size;
 	}
 	
 	public synchronized byte[] allocate()
 	{
 		if (bufferList.isEmpty())
 		{
 			return new byte[bufferSize];
 		}
 		else
 		{
 			return bufferList.removeFirst();
 		}
 	}
 	
 	public synchronized void free(byte[] buffer)
 	{
		bufferList.addFirst(buffer);
 	}
 }
