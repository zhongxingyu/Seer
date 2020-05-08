 package cm.generic;
 
 
 import java.util.*;
 import cm.media.html.*;
 
 public class StringBuffersPool 
 {
 	public static int DEFAULT_POOL_SIZE	=	64;
 	public Vector bufferPool = new Vector();
 	
 	int	bufferSize;
 	int	poolSize;
 	
 	public StringBuffersPool(int bufferSize)
 	{
 	   this(bufferSize, DEFAULT_POOL_SIZE);
 	}
 	public StringBuffersPool(int bufferSize, int poolSize)
 	{
 	   this.bufferSize	= bufferSize;
 	   for(int i = 0 ; i < poolSize; i++)
 	   {
 	      bufferPool.add(new StringBuffer(bufferSize));
 	   }	
 	}
 	
 	public StringBuffer nextBuffer()
 	{
	   synchronized (bufferPool)
 	   {
	      int freeIndex = bufferPool.size() - 1;
	      if (freeIndex == -1)
		 return (new StringBuffer(bufferSize));
	      StringBuffer b = (StringBuffer) bufferPool.remove(freeIndex);
	      return b;
 	   }
 	}
 	
 	public void release(StringBuffer b)
 	{
 	   bufferPool.add(b);		
 	}
 }
