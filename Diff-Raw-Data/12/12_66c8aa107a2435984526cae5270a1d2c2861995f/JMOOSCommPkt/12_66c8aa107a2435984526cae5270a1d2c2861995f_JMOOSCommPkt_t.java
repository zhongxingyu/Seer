 package com.robots.MOOS;
 
 import java.nio.*;
import java.nio.ByteBuffer;
 import java.util.Vector;
 
 public class JMOOSCommPkt
 {
 	//Native Methods
 	private native ByteBuffer 	_serializeMessages( long ptr, long[] ptrs, int size );
 	//:Constructor
 	private native long	    	_createMOOSPkt();
 	//:Methods
 	private native int	    	_getBytesRequired( long ptr );
 	private native void	    	_fill( long ptr, byte[] data, int size );
 	private native void	    	_clear( long ptr, byte[] data, int size );
 	private native long	    	_serialize( long ptr );
     private native void         deleteMessage( long ptr );
 
 
 	//Member Variables
 	private long			ptr;
 	private ByteBuffer		_bytebuffer;
     private boolean         bDeleted;
     public	byte[]			data;
     public  JMOOSMSG_LIST   tmp;
 
 	public JMOOSCommPkt()
 	{
		ByteBuffer dummy = ByteBuffer.allocate(0);
 		ptr = _createMOOSPkt();
 	}
 	static
 	{
 		System.loadLibrary("MOOS");	
 	}
 
     //CHANGE THIS TO SERIALIZE/DESERIALIZE
 	public boolean serialize( JMOOSMSG_LIST list, boolean toStream )
 	{
 		if ( toStream )
 		{
 			long[] ptrs = new long[ list.size() ];
 
 			for ( int i =0; i< list.size(); i++ )
 			{
 				//Get the local pointers
 				long ptr = list.pointerAt(i);
 				ptrs[i] = ptr;
 			}
 
 			_bytebuffer = _serializeMessages( ptr, ptrs, list.size() );
 
 			//Fill the byte array and make it available
 			data = new byte[ _bytebuffer.capacity() ];
 			_bytebuffer.get( data, 0, data.length );
 
             _bytebuffer = null;
 
             //TODO:
             //Clear the bytebuffer
             //_clear( ptr, data, nt size );
 
 			return true;
 		}
 		else
 		{
             tmp = new JMOOSMSG_LIST( _serialize(ptr) );
             return true;
 		}
 	}
     
     /*
      *Helper Functions
      */
     public boolean fill( byte[] data, int size )
 	{
 		_fill( ptr, data, size );
 		return true;
 	}
 
 	public byte[] getBytes()
 	{
 		return data;
 	}
 	
 	public int getBytesRequired()
 	{
 		return  _getBytesRequired( ptr );
 	}
 
     public void Destroy() 
     {
         if( tmp != null )
             tmp.Destroy();
 
         deleteMessage( ptr );
         System.err.println( "MOOSCommPkt(c++) deleted");
         
         bDeleted = true;
     }
 
     protected void finalize() throws Throwable
     {
         if ( !bDeleted )
         {
                 //Delete our embedded object
                 deleteMessage( ptr );
         }
     }
 }
 
 
