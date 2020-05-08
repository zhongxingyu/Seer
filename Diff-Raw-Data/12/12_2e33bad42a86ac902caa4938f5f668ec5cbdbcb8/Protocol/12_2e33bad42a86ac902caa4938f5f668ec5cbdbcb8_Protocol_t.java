 package org.xmodel.net;
 
 import java.io.IOException;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Stack;
 import java.util.WeakHashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.xmodel.DepthFirstIterator;
 import org.xmodel.IDispatcher;
 import org.xmodel.IModelObject;
 import org.xmodel.IPath;
 import org.xmodel.ModelAlgorithms;
 import org.xmodel.ModelObject;
 import org.xmodel.Xlate;
 import org.xmodel.compress.ICompressor;
 import org.xmodel.compress.TabularCompressor;
 import org.xmodel.compress.TabularCompressor.PostCompression;
 import org.xmodel.external.ExternalReference;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.external.NonSyncingListener;
 import org.xmodel.log.Log;
 import org.xmodel.net.stream.Connection;
 import org.xmodel.net.stream.ITcpListener;
 import org.xmodel.util.Identifier;
 import org.xmodel.xpath.XPath;
 import org.xmodel.xpath.expression.IExpression;
 
 /**
  * The protocol class for the NetworkCachingPolicy protocol.
  */
 public abstract class Protocol implements ITcpListener
 {
   public final static int version = 1;
   
   public enum Type
   {
     version,
     error,
     attachRequest,
     attachResponse,
     detachRequest,
     syncRequest,
     syncResponse,
     addChild,
     removeChild,
     changeAttribute,
     clearAttribute,
     changeDirty,
     queryRequest,
     queryResponse,
     debugStepIn,
     debugStepOver,
     debugStepOut
   }
 
   protected Protocol()
   {
     this.timeout = 300000;
     this.responseQueue = new SynchronousQueue<byte[]>();
     this.random = new Random();
     this.keys = new WeakHashMap<IModelObject, KeyRecord>();
     this.map = new HashMap<Connection, ConnectionInfo>();
     
     // allocate less than standard mtu
     buffer = ByteBuffer.allocate( 4096);
     buffer.order( ByteOrder.BIG_ENDIAN);
     
     dispatchers = new Stack<IDispatcher>();
   }
   
   /**
    * Push the specified dispatcher on the stack.
    * @param dispatcher The dispatcher.
    */
   public void pushDispatcher( IDispatcher dispatcher)
   {
     dispatchers.push( dispatcher);
   }
   
   /**
    * Pop the last dispatcher off the stack.
    */
   public void popDispatcher()
   {
     dispatchers.pop();
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.stream.ITcpListener#onConnect(org.xmodel.net.stream.Connection)
    */
   @Override
   public void onConnect( Connection connection)
   {
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.stream.Connection)
    */
   @Override
   public void onClose( Connection connection)
   {
     ConnectionInfo info = map.get( connection);
     info.listener.uninstall();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.stream.ITcpListener#onReceive(org.xmodel.net.stream.Connection, java.nio.ByteBuffer)
    */
   @Override
   public void onReceive( Connection connection, ByteBuffer buffer)
   {
     buffer.mark();
     while( handleMessage( connection, buffer)) buffer.mark();
     buffer.reset();
   }
   
   /**
    * Parse and handle one message from the specified buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @return Returns true if a message was handled.
    */
   private final boolean handleMessage( Connection connection, ByteBuffer buffer)
   {
     try
     {
       Type type = readMessageType( buffer);
       
       int length = readMessageLength( buffer);
       if ( length > buffer.remaining()) return false;
       
       switch( type)
       {
         case version:         handleVersion( connection, buffer, length); return true;
         case error:           handleError( connection, buffer, length); return true;
         case attachRequest:   handleAttachRequest( connection, buffer, length); return true;
         case attachResponse:  handleAttachResponse( connection, buffer, length); return true;
         case detachRequest:   handleDetachRequest( connection, buffer, length); return true;
         case syncRequest:     handleSyncRequest( connection, buffer, length); return true;
         case syncResponse:    handleSyncResponse( connection, buffer, length); return true;
         case addChild:        handleAddChild( connection, buffer, length); return true;
         case removeChild:     handleRemoveChild( connection, buffer, length); return true;
         case changeAttribute: handleChangeAttribute( connection, buffer, length); return true;
         case clearAttribute:  handleClearAttribute( connection, buffer, length); return true;
         case changeDirty:     handleChangeDirty( connection, buffer, length); return true;
         case queryRequest:    handleQueryRequest( connection, buffer, length); return true;
         case queryResponse:   handleQueryResponse( connection, buffer, length); return true;
         case debugStepIn:     handleDebugStepIn( connection, buffer, length); return true;
         case debugStepOver:   handleDebugStepOver( connection, buffer, length); return true;
         case debugStepOut:    handleDebugStepOut( connection, buffer, length); return true;
       }
     }
     catch( BufferUnderflowException e)
     {
     }
     
     return false;
   }
   
   /**
    * Send the protocol version.
    * @param connection The connection.
    * @param version The version.
    */
   public final void sendVersion( Connection connection, short version) throws IOException
   {
     log.debugf( "sendVersion: %d", version);
     initialize( buffer);
     buffer.putShort( version);
     finalize( buffer, Type.version, 2);
     connection.write( buffer);
   }
   
    /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleVersion( Connection connection, ByteBuffer buffer, int length)
   {
     int version = buffer.getShort();
     log.debugf( "handleVersion: %d", version);
     if ( version != Protocol.version)
     {
       connection.close();
     }
   }
   
   /**
    * Send an error message.
    * @param connection The connection.
    * @param message The error message.
    */
   public final void sendError( Connection connection, String message) throws IOException
   {
     log.debugf( "sendError: %s", message);
     initialize( buffer);
     byte[] bytes = message.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.error, bytes.length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleError( Connection connection, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     log.debugf( "handleError: %s", new String( bytes));
     handleError( connection, new String( bytes));
   }
   
   /**
    * Handle an error message.
    * @param connection The connection.
    * @param message The message.
    */
   protected void handleError( Connection connection, String message)
   {
   }
   
   /**
    * Send an attach request message.
    * @param connection The connection.
    * @param xpath The xpath.
    */
   public final void sendAttachRequest( Connection connection, String xpath) throws IOException
   {
     log.debugf( "sendAttachRequest: %s", xpath);
     initialize( buffer);
     byte[] bytes = xpath.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.attachRequest, bytes.length);
 
     // send and wait for response
     byte[] response = send( connection, buffer, timeout);
     ICompressor compressor = map.get( connection).compressor;
     IModelObject element = compressor.decompress( response, 0);
     log.debugf( "handleAttachResponse: %s\n", element.getType());
     handleAttachResponse( connection, element);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAttachRequest( Connection connection, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     log.debugf( "handleAttachRequest: %s", new String( bytes));
     handleAttachRequest( connection, new String( bytes));
   }
   
   /**
    * Handle an attach request.
    * @param connection The connection.
    * @param xpath The xpath.
    */
   protected void handleAttachRequest( Connection connection, String xpath)
   {
   }
 
   /**
    * Send an attach response message.
    * @param connection The connection.
    * @param element The element.
    */
   public final void sendAttachResponse( Connection connection, IModelObject element) throws IOException
   {
     log.debugf( "sendAttachResponse: %s", element.getType());
     initialize( buffer);
     ICompressor compressor = map.get( connection).compressor;
     byte[] bytes = compressor.compress( element);
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.attachResponse, bytes.length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAttachResponse( Connection connection, ByteBuffer buffer, int length)
   {
     queueResponse( buffer, length);
   }
   
   /**
    * Handle an attach response.
    * @param connection The connection.
    * @param element The element.
    */
   protected void handleAttachResponse( Connection connection, IModelObject element)
   {
   }
   
   /**
    * Send an detach request message.
    * @param connection The connection.
    * @param xpath The xpath.
    */
   public final void sendDetachRequest( Connection connection, String xpath) throws IOException
   {
     initialize( buffer);
     byte[] bytes = xpath.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.detachRequest, bytes.length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDetachRequest( Connection connection, ByteBuffer buffer, int length)
   {
   }
   
   /**
    * Handle an attach request.
    * @param connection The connection.
    * @param xpath The xpath.
    */
   protected void handleDetachRequest( Connection connection, String xpath)
   {
   }
   
   /**
    * Send an sync request message.
    * @param reference The local reference.
    */
   public final void sendSyncRequest( IExternalReference reference) throws IOException
   {
     log.debugf( "sendSyncRequest: %s", reference.getType());
     
     KeyRecord key = keys.get( reference);
     
     initialize( buffer);
     byte[] bytes = key.key.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.syncRequest, bytes.length);
     
     // send and wait for response
     send( key.connection, buffer, timeout);
     handleSyncResponse( key.connection);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSyncRequest( Connection connection, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     handleSyncRequest( connection, new String( bytes));
   }
   
   /**
    * Handle a sync request.
    * @param connection The connection.
    * @param key The reference key.
    */
   protected void handleSyncRequest( Connection sender, String key)
   {
     log.debugf( "handleSyncRequest: %s", key);
     
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new SyncRunnable( sender, key));
   }
   
   /**
    * Send an sync response message.
    * @param connection The connection.
    */
   public final void sendSyncResponse( Connection connection) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.syncResponse, 0);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSyncResponse( Connection connection, ByteBuffer buffer, int length)
   {
     queueResponse( buffer, length);
   }
   
   /**
    * Handle a sync response.
    * @param connection The connection.
    */
   protected void handleSyncResponse( Connection connection)
   {
   }
   
   /**
    * Send an add child message.
    * @param connection The connection.
    * @param xpath The xpath.
    * @param element The element.
    * @param index The insertion index.
    */
   public final void sendAddChild( Connection connection, String xpath, IModelObject element, int index) throws IOException
   {
     log.debugf( "sendAddChild: %s, %s", xpath, element.getType());    
     
     initialize( buffer);
     int length = writeString( xpath);
     length += writeElement( map.get( connection).compressor, element);
     buffer.putInt( index); length += 4;
     finalize( buffer, Type.addChild, length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAddChild( Connection connection, ByteBuffer buffer, int length)
   {
     String xpath = readString( buffer);
     byte[] bytes = readBytes( buffer, false);
     int index = buffer.getInt();
     handleAddChild( connection, xpath, bytes, index);
   }
   
   /**
    * Handle an add child message.
    * @param connection The connection.
    * @param xpath The path from the root to the parent.
    * @param child The child that was added as a compressed byte array.
    * @param index The insertion index.
    */
   protected void handleAddChild( Connection connection, String xpath, byte[] child, int index)
   {
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new AddChildEvent( connection, xpath, child, index));
   }
   
   /**
    * Process an add child event in the appropriate thread.
    * @param connection The connection.
    * @param xpath The xpath of the parent.
    * @param bytes The child that was added.
    * @param index The index of insertion.
    */
   private void processAddChild( Connection connection, String xpath, byte[] bytes, int index)
   {
     try
     {
       updating = true;
       
       IModelObject attached = map.get( connection).element;
       if ( attached == null) return;
       
       IExpression parentExpr = XPath.createExpression( xpath);
       if ( parentExpr != null)
       {
         IModelObject parent = parentExpr.queryFirst( attached);
         IModelObject child = map.get( connection).compressor.decompress( bytes, 0);
         log.debugf( "processAddChild: %s, %s", xpath, child.getType());            
         if ( parent != null) 
         {
           IModelObject childElement = decode( connection, child);
           parent.addChild( childElement, index);
         }
       }
     }
     finally
     {
       updating = false;
     }
   }
   
   /**
    * Send an add child message.
    * @param connection The connection.
    * @param xpath The xpath.
    * @param index The insertion index.
    */
   public final void sendRemoveChild( Connection connection, String xpath, int index) throws IOException
   {
     initialize( buffer);
     int length = writeString( xpath);
     buffer.putInt( index); length += 4;
     finalize( buffer, Type.removeChild, length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleRemoveChild( Connection connection, ByteBuffer buffer, int length)
   {
     String xpath = readString( buffer);
     int index = buffer.getInt();
     handleRemoveChild( connection, xpath, index);
   }
   
   /**
    * Handle an remove child message.
    * @param connection The connection.
    * @param xpath The path from the root to the parent.
    * @param index The insertion index.
    */
   protected void handleRemoveChild( Connection connection, String xpath, int index)
   {
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new RemoveChildEvent( connection, xpath, index));
   }
   
   /**
    * Process an remove child event in the appropriate thread.
    * @param connection The connection.
    * @param xpath The xpath of the parent.
    * @param index The index of insertion.
    */
   private void processRemoveChild( Connection connection, String xpath, int index)
   {
     try
     {
       updating = true;
       
       IModelObject attached = map.get( connection).element;
       if ( attached == null) return;
       
       IExpression parentExpr = XPath.createExpression( xpath);
       if ( parentExpr != null)
       {
         IModelObject parent = parentExpr.queryFirst( attached);
         if ( parent != null) 
         {
           IModelObject removed = parent.removeChild( index);
           if ( removed instanceof IExternalReference) keys.remove( removed);
         }
       }
     }
     finally
     {
       updating = false;
     }    
   }
   
   /**
    * Send an change attribute message.
    * @param connection The connection.
    * @param xpath The xpath.
    * @param attrName The name of the attribute.
    * @param attrValue The new value.
    */
   public final void sendChangeAttribute( Connection connection, String xpath, String attrName, Object value) throws IOException
   {
     byte[] bytes = serialize( value);
     
     initialize( buffer);
     int length = writeString( xpath);
     length += writeString( attrName);
     length += writeBytes( bytes, 0, bytes.length, true);
     finalize( buffer, Type.changeAttribute, length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleChangeAttribute( Connection connection, ByteBuffer buffer, int length)
   {
     String xpath = readString( buffer);
     String attrName = readString( buffer);
     byte[] attrValue = readBytes( buffer, true);
     handleChangeAttribute( connection, xpath, attrName, deserialize( attrValue));
   }
   
   /**
    * Handle an attribute change message.
    * @param connection The connection.
    * @param xpath The xpath from the root to the element.
    * @param attrName The name of the attribute.
    * @param attrValue The attribute value.
    */
   protected void handleChangeAttribute( Connection connection, String xpath, String attrName, Object attrValue)
   {
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new ChangeAttributeEvent( connection, xpath, attrName, attrValue));
   }
   
   /**
    * Process an change attribute event in the appropriate thread.
    * @param connection The connection.
    * @param xpath The xpath of the element.
    * @param attrName The name of the attribute.
    * @param attrValue The new value.
    */
   private void processChangeAttribute( Connection connection, String xpath, String attrName, Object attrValue)
   {
     try
     {
       updating = true;
       
       IModelObject attached = map.get( connection).element;
       if ( attached == null) return;
       
       // the empty string and null string both serialize as byte[ 0]
       if ( attrValue == null) attrValue = "";
       
       IExpression elementExpr = XPath.createExpression( xpath);
       if ( elementExpr != null)
       {
         IModelObject element = elementExpr.queryFirst( attached);
         if ( element != null) element.setAttribute( attrName, attrValue);
       }
     }
     finally
     {
       updating = false;
     }        
   }
   
   /**
    * Send a clear attribute message.
    * @param connection The connection.
    * @param xpath The xpath.
    * @param attrName The name of the attribute.
    */
   public final void sendClearAttribute( Connection connection, String xpath, String attrName) throws IOException
   {
     initialize( buffer);
     int length = writeString( xpath);
     length += writeString( attrName);
     finalize( buffer, Type.clearAttribute, length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleClearAttribute( Connection connection, ByteBuffer buffer, int length)
   {
     String xpath = readString( buffer);
     String attrName = readString( buffer);
     handleClearAttribute( connection, xpath, attrName);
   }
   
   /**
    * Handle an attribute change message.
    * @param connection The connection.
    * @param xpath The xpath from the root to the element.
    * @param attrName The name of the attribute.
    */
   protected void handleClearAttribute( Connection connection, String xpath, String attrName)
   {
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new ClearAttributeEvent( connection, xpath, attrName));
   }
 
   /**
    * Process a clear attribute event in the appropriate thread.
    * @param connection The connection.
    * @param xpath The xpath of the element.
    * @param attrName The name of the attribute.
    */
   private void processClearAttribute( Connection connection, String xpath, String attrName)
   {
     try
     {
       updating = true;
       
       IModelObject attached = map.get( connection).element;
       if ( attached == null) return;
       
       IExpression elementExpr = XPath.createExpression( xpath);
       if ( elementExpr != null)
       {
         IModelObject element = elementExpr.queryFirst( attached);
         if ( element != null) element.removeAttribute( attrName);
       }
     }
     finally
     {
       updating = false;
     }    
   }
   
   /**
    * Send a change dirty message.
    * @param connection The connection.
    * @param xpath The xpath.
    * @param dirty The dirty state.
    */
   public final void sendChangeDirty( Connection connection, String xpath, boolean dirty) throws IOException
   {
     initialize( buffer);
     int length = writeString( xpath);
     buffer.put( dirty? (byte)1: 0); length++;
     finalize( buffer, Type.changeDirty, length);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleChangeDirty( Connection connection, ByteBuffer buffer, int length)
   {
     String xpath = readString( buffer);
     boolean dirty = buffer.get() != 0;
     handleChangeDirty( connection, xpath, dirty);
   }
   
   /**
    * Handle a change dirty message.
    * @param connection The connection.
    * @param xpath The xpath from the root to the element.
    * @param dirty The dirty state.
    */
   protected void handleChangeDirty( Connection connection, String xpath, boolean dirty)
   {
     IDispatcher dispatcher = dispatchers.peek();
     dispatcher.execute( new ChangeDirtyEvent( connection, xpath, dirty));
   }
 
   /**
    * Process a change dirty event in the appropriate thread.
    * @param connection The connection.
    * @param xpath The xpath of the element.
    * @param dirty The new dirty state.
    */
   private void processChangeDirty( Connection connection, String xpath, boolean dirty)
   {
     try
     {
       updating = true;
       
       IModelObject attached = map.get( connection).element;
       if ( attached == null) return;
       
       IExpression elementExpr = XPath.createExpression( xpath);
       if ( elementExpr != null)
       {
         IModelObject element = elementExpr.queryFirst( attached);
         if ( element != null)
         {
           IExternalReference reference = (IExternalReference)element;
           reference.setDirty( dirty);
         }
       }
     }
     finally
     {
       updating = false;
     }    
   }
 
   /**
    * Send a query request message.
    * @param connection The connection.
    * @param xpath The xpath.
    */
   public final void sendQueryRequest( Connection connection, String xpath) throws IOException
   {
     initialize( buffer);
     byte[] bytes = xpath.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.queryRequest, bytes.length);
     send( connection, buffer, timeout);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleQueryRequest( Connection connection, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     handleQueryRequest( connection, new String( bytes));
   }
   
   /**
    * Handle a query requset.
    * @param connection The connection.
    * @param xpath The query.
    */
   protected void handleQueryRequest( Connection connection, String xpath)
   {
   }
   
   /**
    * Send a query response message.
    * @param connection The connection.
    * @param result The result.
    */
   public final void sendQueryResponse( Connection connection, Object result) throws IOException
   {
     initialize( buffer);
     byte[] bytes = serialize( result);
     buffer.put( bytes);
     finalize( buffer, Type.queryResponse, bytes.length);
 
     // wait for response
     byte[] response = send( connection, buffer, timeout);
     handleQueryResponse( connection, response);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleQueryResponse( Connection connection, ByteBuffer buffer, int length)
   {
     queueResponse( buffer, length);
   }
   
   /**
    * Handle a query requset.
    * @param connection The connection.
    * @param bytes The serialized query result.
    */
   protected void handleQueryResponse( Connection connection, byte[] bytes)
   {
   }
   
   /**
    * Send a debug step message.
    * @param connection The connection.
    */
   public final void sendDebugStepIn( Connection connection) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.debugStepIn, 0);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDebugStepIn( Connection connection, ByteBuffer buffer, int length)
   {
     handleDebugStepIn( connection);
   }
   
   /**
    * Handle a debug step.
    * @param connection The connection.
    */
   protected void handleDebugStepIn( Connection connection)
   {
   }
   
   /**
    * Send a debug step message.
    * @param connection The connection.
    */
   public final void sendDebugStepOver( Connection connection) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.debugStepOver, 0);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDebugStepOver( Connection connection, ByteBuffer buffer, int length)
   {
     handleDebugStepOver( connection);
   }
   
   /**
    * Handle a debug step.
    * @param connection The connection.
    */
   protected void handleDebugStepOver( Connection connection)
   {
   }
   
   /**
    * Send a debug step message.
    * @param connection The connection.
    */
   public final void sendDebugStepOut( Connection connection) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.debugStepOut, 0);
     connection.write( buffer);
   }
   
   /**
    * Handle the specified message buffer.
    * @param connection The connection.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDebugStepOut( Connection connection, ByteBuffer buffer, int length)
   {
     handleDebugStepOut( connection);
   }
   
   /**
    * Handle a debug step.
    * @param connection The connection.
    */
   protected void handleDebugStepOut( Connection connection)
   {
   }
 
   /**
    * Send and wait for a response.
    * @param connection The connection.
    * @param buffer The buffer to send.
    * @param timeout The timeout in milliseconds.
    * @return Returns null or the response buffer.
    */
   private byte[] send( Connection connection, ByteBuffer buffer, int timeout) throws IOException
   {
     try
     {
       connection.write( buffer);
       byte[] response = responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
       if ( response != null) return response;
     }
     catch( InterruptedException e)
     {
       return null;
     }
     
     throw new IOException( "Network request timeout.");
   }
   
   /**
    * Queue a synchronous response.
    * @param buffer The buffer containing the response.
    * @param length The length of the response.
    */
   private void queueResponse( ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     try { responseQueue.put( bytes);} catch( InterruptedException e) {}
   }
   
   /**
    * Returns the serialized attribute value.
    * @param object The attribute value.
    * @return Returns the serialized attribute value.
    */
   private byte[] serialize( Object object)
   {
     if ( object == null) return new byte[ 0];
     
     // TODO: beef this up!
     return object.toString().getBytes();
   }
   
   /**
    * Returns the deserialized attribute value.
    * @param bytes The serialized attribute value.
    * @return Returns the deserialized attribute value.
    */
   private Object deserialize( byte[] bytes)
   {
     if ( bytes.length == 0) return null;
     
     // TODO: beef this up!
     return new String( bytes);
   }
   
   /**
    * Reserve the maximum amount of space for the header.
    * @param buffer The buffer.
    */
   public static void initialize( ByteBuffer buffer)
   {
     buffer.clear();
     buffer.position( 5);
   }
   
   /**
    * Read the message type from the buffer.
    * @param buffer The buffer.
    * @return Returns the message type.
    */
   public static Type readMessageType( ByteBuffer buffer)
   {
     int index = buffer.get() & 0x7f;
     return Type.values()[ index];
   }
   
   /**
    * Read the message length from the buffer.
    * @param buffer The buffer.
    * @return Returns the message length.
    */
   public static int readMessageLength( ByteBuffer buffer)
   {
     int mask = buffer.get( buffer.position() - 1) & 0x80;
     if ( mask == 0) return buffer.get();
     return buffer.getInt();
   }
   
   /**
    * Finalize the message by writing the header and preparing the buffer to be read.
    * @param buffer The buffer.
    * @param type The message type.
    * @param length The message length.
    */
   public static void finalize( ByteBuffer buffer, Type type, int length)
   {
     buffer.limit( buffer.position());
     if ( length < 128)
     {
       buffer.put( 3, (byte)type.ordinal());
       buffer.put( 4, (byte)length);
       buffer.position( 3);
     }
     else
     {
       buffer.put( 0, (byte)(type.ordinal() | 0x80));
       buffer.putInt( 1, length);
       buffer.position( 0);
     }
   }
   
   /**
    * Read a length from the buffer.
    * @param buffer The buffer.
    * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
    * @return Returns the length.
    */
   private static int readLength( ByteBuffer buffer, boolean small)
   {
     buffer.mark();
     if ( small)
     {
       int length = buffer.get();
       if ( length >= 0) return length;
     }
     else
     {
       int length = buffer.getShort();
       if ( length >= 0) return length;
     }
     
     buffer.reset();
     return -buffer.getInt();
   }
   
   /**
    * Write a length into the buffer in either one, two or four bytes.
    * @param buffer The buffer.
    * @param length The length.
    * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
    */
   private static int writeLength( ByteBuffer buffer, int length, boolean small)
   {
     if ( small)
     {
       if ( length < 128)
       {
         buffer.put( (byte)length);
         return 1;
       }
     }
     else
     {
       if ( length < 32768)
       {
         buffer.putShort( (short)length);
         return 2;
       }
     }
     
     buffer.putInt( -length);
     return 4;
   }
   
   /**
    * Read a block of bytes from the message.
    * @param buffer The buffer.
    * @return Returns the bytes read.
    */
   public static byte[] readBytes( ByteBuffer buffer, boolean small)
   {
     int length = readLength( buffer, small);
     byte[] bytes = new byte[ length];
     buffer.get( bytes, 0, length);
     return bytes;
   }
   
   /**
    * Write a block of bytes to the message.
    * @param buffer The buffer.
    * @param bytes The bytes.
    * @param offset The offset.
    * @param length The length.
    */
   public int writeBytes( byte[] bytes, int offset, int length, boolean small)
   {
     int required = buffer.position() + length;
     if ( required >= buffer.limit())
     {
       buffer.flip();
       ByteBuffer larger = ByteBuffer.allocateDirect( (int)(required * 1.5));
       larger.put( buffer);
       buffer = larger;
     }
     
     int prefix = writeLength( buffer, length, small);
     buffer.put( bytes, offset, length);
     return prefix + length;
   }
   
   /**
    * Read an String from the message.
    * @param buffer The buffer.
    * @return Returns the string.
    */
   public static String readString( ByteBuffer buffer)
   {
     return new String( readBytes( buffer, true));
   }
   
   /**
    * Write a String into the message.
    * @param buffer The buffer.
    * @param string The string.
    */
   public int writeString( String string)
   {
     byte[] bytes = string.getBytes();
     return writeBytes( bytes, 0, bytes.length, true);
   }
   
   /**
    * Read an IModelObject from the message.
    * @param compressor The compressor.
    * @param buffer The buffer.
    * @return Returns the element.
    */
   public IModelObject readElement( ICompressor compressor, ByteBuffer buffer)
   {
     byte[] bytes = readBytes( buffer, false);
     IModelObject object = compressor.decompress( bytes, 0);
     return object;
   }
 
   /**
    * Write an IModelObject to the message.
    * @param compressor The compressor;
    * @param element The element.
    */
   public int writeElement( ICompressor compressor, IModelObject element)
   {
     byte[] bytes = compressor.compress( element);
     return writeBytes( bytes, 0, bytes.length, false);
   }
 
   /**
    * Encode the specified element.
    * @param connection The connection.
    * @param root True if the element is the root of the attachment.
    * @param element The element to be copied.
    * @return Returns the copy.
    */
   protected IModelObject encode( Connection connection, IModelObject element, boolean root)
   {
     IModelObject encoded = null;
     
     if ( element instanceof IExternalReference)
     {
       IExternalReference lRef = (IExternalReference)element;
       encoded = new ModelObject( lRef.getType());
       
       // index reference so it can be synced remotely
       if ( !root) encoded.setAttribute( "net:key", index( connection, lRef));
       
       // enumerate static attributes for client
       for( String attrName: lRef.getStaticAttributes())
       {
         IModelObject entry = new ModelObject( "net:static");
         entry.setValue( attrName);
         encoded.addChild( entry);
       }
       
       // copy only static attributes if reference is dirty
       if ( element.isDirty())
       {
         Xlate.set( encoded, "net:dirty", true);
         
         // copy static attributes
         for( String attrName: lRef.getStaticAttributes())
         {
           Object attrValue = element.getAttribute( attrName);
           if ( attrValue != null) encoded.setAttribute( attrName, attrValue);
         }
       }
     }
     else
     {
       encoded = new ModelObject( element.getType());
     }
     
     // copy all attributes and children
     if ( !(element instanceof IExternalReference) || !element.isDirty())
     {
       // copy attributes
       ModelAlgorithms.copyAttributes( element, encoded);
       
       // copy children
       for( IModelObject child: element.getChildren())
       {
         encoded.addChild( encode( connection, child, false));
       }
     }
     
     return encoded;
   }
     
   /**
    * Interpret the content of the specified server encoded subtree.
    * @param connection The connection.
    * @param root The root of the encoded subtree.
    * @return Returns the decoded element.
    */
   protected IModelObject decode( Connection connection, IModelObject root)
   {
     //System.out.println( ((ModelObject)root).toXml());
     
     Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
     DepthFirstIterator iter = new DepthFirstIterator( root);
     while( iter.hasNext())
     {
       IModelObject lNode = iter.next();
       if ( lNode.isType( "net:static")) continue;
       
       IModelObject rNode = map.get( lNode);
       IModelObject rParent = map.get( lNode.getParent());
       
       String key = Xlate.get( lNode, "net:key", (String)null);
       if ( key != null)
       {
         ExternalReference reference = new ExternalReference( lNode.getType());
         ModelAlgorithms.copyAttributes( lNode, reference);
         reference.removeAttribute( "net:key");
         reference.removeChildren( "net:static");
         
         NetKeyCachingPolicy cachingPolicy = new NetKeyCachingPolicy( this);
         cachingPolicy.setStaticAttributes( getStaticAttributes( lNode));
         reference.setCachingPolicy( cachingPolicy);
         
         boolean dirty = Xlate.get( reference, "net:dirty", false);
         reference.removeAttribute( "net:dirty");
         reference.setDirty( dirty);
         
         KeyRecord keyRecord = new KeyRecord();
         keyRecord.key = key;
         keyRecord.connection = connection;
         keys.put( reference, keyRecord);
         
         rNode = reference;
       }
       else if ( lNode == root)
       {
         rNode = new ModelObject( root.getType());
         ModelAlgorithms.copyAttributes( lNode, rNode);
       }
       else
       {
         rNode = lNode;
       }
       
       map.put( lNode, rNode);
       if ( rParent != null) rParent.addChild( rNode);
     }
     
     return map.get( root);
   }
   
   /**
    * Index the specified node.
    * @param connection The connection.
    * @param node The node.
    * @return Returns the key.
    */
   private String index( Connection connection, IModelObject node)
   {
     String key = Identifier.generate( random, 13);
     map.get( connection).index.put( key, node);
     return key;
   }
   
   /**
    * Returns a list of the static attributes encoded for the specified element.
    * @param element The encoded element.
    */
   private List<String> getStaticAttributes( IModelObject element)
   {
     List<String> statics = new ArrayList<String>();
     List<IModelObject> children = element.getChildren( "net:static");
     for( IModelObject child: children) statics.add( Xlate.get( child, ""));
     return statics;
   }
   
   private final class SyncRunnable implements Runnable
   {
     public SyncRunnable( Connection sender, String key)
     {
       this.sender = sender;
       this.key = key;
     }
     
     public void run()
     {
       IModelObject reference = map.get( sender).index.get( key);
       if ( reference != null)
       {
         try
         {
           reference.getChildren();
           sendSyncResponse( sender);
         }
         catch( IOException e)
         {
           log.exception( e);
         }
       }
     }
     
     private Connection sender;
     private String key;
   }
   
   private final class AddChildEvent implements Runnable
   {
     public AddChildEvent( Connection connection, String xpath, byte[] child, int index)
     {
       this.connection = connection;
       this.xpath = xpath;
       this.child = child;
       this.index = index;
     }
     
     public void run()
     {
       processAddChild( connection, xpath, child, index);
     }
     
     private Connection connection;
     private String xpath;
     private byte[] child;
     private int index;
   }
   
   private final class RemoveChildEvent implements Runnable
   {
     public RemoveChildEvent( Connection connection, String xpath, int index)
     {
       this.connection = connection;
       this.xpath = xpath;
       this.index = index;
     }
     
     public void run()
     {
       processRemoveChild( connection, xpath, index);
     }
     
     private Connection connection;
     private String xpath;
     private int index;
   }
   
   private final class ChangeAttributeEvent implements Runnable
   {
     public ChangeAttributeEvent( Connection connection, String xpath, String attrName, Object attrValue)
     {
       this.connection = connection;
       this.xpath = xpath;
       this.attrName = attrName;
       this.attrValue = attrValue;
     }
     
     public void run()
     {
       processChangeAttribute( connection, xpath, attrName, attrValue);
     }
     
     private Connection connection;
     private String xpath;
     private String attrName;
     private Object attrValue;
   }
   
   private final class ClearAttributeEvent implements Runnable
   {
     public ClearAttributeEvent( Connection connection, String xpath, String attrName)
     {
       this.connection = connection;
       this.xpath = xpath;
       this.attrName = attrName;
     }
     
     public void run()
     {
       processClearAttribute( connection, xpath, attrName);
     }
     
     private Connection connection;
     private String xpath;
     private String attrName;
   }
   
   private final class ChangeDirtyEvent implements Runnable
   {
     public ChangeDirtyEvent( Connection connection, String xpath, boolean dirty)
     {
       this.connection = connection;
       this.xpath = xpath;
       this.dirty = dirty;
     }
     
     public void run()
     {
       processChangeDirty( connection, xpath, dirty);
     }
     
     private Connection connection;
     private String xpath;
     private boolean dirty;
   }
   
   protected class Listener extends NonSyncingListener
   {
     public Listener( Connection sender, String xpath, IModelObject root)
     {
       this.sender = sender;
       this.xpath = xpath;
       this.root = root;
     }
     
     public void uninstall()
     {
       uninstall( root);
     }
     
     /* (non-Javadoc)
      * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
      */
     @Override
     public void notifyAddChild( IModelObject parent, IModelObject child, int index)
     {
       super.notifyAddChild( parent, child, index);
       
       if ( updating) return;
       
       try
       {
         IModelObject clone = encode( sender, child, false);
         sendAddChild( sender, createPath( root, parent), clone, index);
       } 
       catch( IOException e)
       {
         log.exception( e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
      */
     @Override
     public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
     {
       super.notifyRemoveChild( parent, child, index);
       
       if ( updating) return;
       
       try
       {
         sendRemoveChild( sender, createPath( root, parent), index);
       } 
       catch( IOException e)
       {
         log.exception( e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
      */
     @Override
     public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
     {
       if ( updating) return;

      // make sure relative path is constructed correctly
      boolean revert = attrName.equals( "id");
      if ( revert) object.getModel().revert();
      String xpath = createPath( root, object);
      if ( revert) object.getModel().restore();
       
       try
       {
        sendChangeAttribute( sender, xpath, attrName, newValue);
       } 
       catch( IOException e)
       {
         log.exception( e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
      */
     @Override
     public void notifyClear( IModelObject object, String attrName, Object oldValue)
     {
       if ( updating) return;
       
       try
       {
         sendClearAttribute( sender, createPath( root, object), attrName);
       } 
       catch( IOException e)
       {
         log.exception( e);
       }
     }
     
     /* (non-Javadoc)
      * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
      */
     @Override
     public void notifyDirty( IModelObject object, boolean dirty)
     {
       //
       // Do not send notifications for network external references, otherwise the remote reference will
       // be marked not-dirty before a sync request is sent and the sync request will have no effect.
       //
       if ( updating || keys.get( object) != null) return;
       
       try
       {
         sendChangeDirty( sender, createPath( root, object), dirty);
       } 
       catch( IOException e)
       {
         log.exception( e);
       }
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals( Object object)
     {
       if ( object instanceof Listener)
       {
         Listener other = (Listener)object;
         return other.sender == sender && other.xpath.equals( xpath);
       }
       return false;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode()
     {
       return sender.hashCode() + xpath.hashCode();
     }
 
     private Connection sender;
     private String xpath;
     private IModelObject root;
   };
   
   /**
    * Create a relative xpath from the specified root to the specified element.
    * @param root The root.
    * @param element The element.
    * @return Returns the xpath string.
    */
   private final String createPath( IModelObject root, IModelObject element)
   {
     IPath path = ModelAlgorithms.createRelativePath( root, element);
     return path.toString();
   }
   
   private class KeyRecord
   {
     String key;
     Connection connection;
   }
   
   protected class ConnectionInfo
   {
     public ConnectionInfo()
     {
       index = new HashMap<String, IModelObject>();
       compressor = new TabularCompressor( PostCompression.zip);
     }
     
     public String xpath;
     public IModelObject element;
     public Map<String, IModelObject> index;
     public TabularCompressor compressor;
     public Listener listener;
   }
   
   private static Log log = Log.getLog( "org.xmodel.net");
 
   private ByteBuffer buffer;
   private BlockingQueue<byte[]> responseQueue;
   private int timeout;
   private Random random;
   protected Map<IModelObject, KeyRecord> keys;
   protected Stack<IDispatcher> dispatchers;
   protected Map<Connection, ConnectionInfo> map;
   private boolean updating;
 }
