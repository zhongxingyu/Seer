 package com.robots.MOOS;
 
 import java.nio.*;
 import java.io.*;
 import java.util.*;
 
 public class JMOOSMSG_LIST
 {
 	private native int		_getNumberMessages( long ptr );
 	private native long		_getPointerToMOOSMsg( long ptr, int i );
 
 	public Vector<JMOOSMsg>	messages;
 	private boolean			bEmbedded;
 	private boolean			bVerbose;
 	public 	boolean			bInitialised;
 	private long			ptr;
 	private int			    iNumMessages;
     final   int             iMaxMessages = 100;
 
 
 	public JMOOSMSG_LIST()
 	{
 		iNumMessages = 0;
         messages = new Vector();
 
 		bEmbedded = false;
 	
     }
 
 	public JMOOSMSG_LIST( Vector<JMOOSMsg> MSG_LIST )
 	{
 		messages = MSG_LIST;
 
 		iNumMessages = messages.size();
 
 		bEmbedded = false;
 		bInitialised = true;
 	}
 	
 	public JMOOSMSG_LIST( long ptr )
 	{
 		ptr = ptr;
 
 		iNumMessages = _getNumberMessages( ptr );
 		
		//if ( bVerbose )
 			System.out.println( iNumMessages + " AVAILABLE" );
 
 		messages = new Vector();
 
 		//Need to iterate through and create MOOSMsg's from the embedded list
 		for ( int i = 0; i < iNumMessages; i++ )
 		{
 				
 			long _ptr = _getPointerToMOOSMsg( ptr, i );
 			
 			JMOOSMsg msg = new JMOOSMsg( _ptr );
 
 			messages.add( msg );
 	
         }
 
 		bEmbedded = true;
 		bInitialised = true;
 	}
 
     public boolean add( JMOOSMsg msg )
     {
         if ( messages.size() < iMaxMessages )
         {
             messages.add( msg );
 
             iNumMessages = messages.size();
 
             return true;
         }
         
         return false;
     }
 
     public JMOOSMsg front()
     {
         if ( iNumMessages > 0 )
         {
             return messages.firstElement(); 
         }
         else
         {
             return null;
         }
     }
 
     public JMOOSMsg elementAt( int index )
     {
         if ( iNumMessages >= index )
         {
             return messages.elementAt( index ); 
         }
         else
         {
             return null;
         }
     }
 
 
 
    
     public boolean clear()
     {
         messages = null;
 
         messages = new  Vector();
     
         return true;
     }
     
     public void Trace()
     {
         for ( int i =0; i< messages.size(); i++ )
         {
             messages.elementAt(i).Trace();
         }
 
     }
 
     /*
      *Helper Functions
      */
     public int size()
 	{
 		return iNumMessages;
 	}
 
 	public long pointerAt( int i )
 	{
 		if ( i > iNumMessages )
 		{
 			System.err.println( "ARRAY INDEX VIOLATION");
 			return 0;
 		}
 
 		//if ( bEmbedded )
 			//return _getPointerToMOOSMsg( ptr, i );
 		//else
 		return messages.get(i).getNativePointer();
 	}
 }
 
