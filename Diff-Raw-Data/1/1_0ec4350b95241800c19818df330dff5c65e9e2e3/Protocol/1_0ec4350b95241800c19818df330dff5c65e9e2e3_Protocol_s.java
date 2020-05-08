 package org.xmodel.net;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.WeakHashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 import org.xmodel.DepthFirstIterator;
 import org.xmodel.IDispatcher;
 import org.xmodel.IModelObject;
 import org.xmodel.ImmediateDispatcher;
 import org.xmodel.ModelAlgorithms;
 import org.xmodel.ModelObject;
 import org.xmodel.PathSyntaxException;
 import org.xmodel.Xlate;
 import org.xmodel.compress.DefaultSerializer;
 import org.xmodel.compress.ICompressor;
 import org.xmodel.compress.ISerializer;
 import org.xmodel.compress.TabularCompressor;
 import org.xmodel.external.CachingException;
 import org.xmodel.external.ExternalReference;
 import org.xmodel.external.ICachingPolicy;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.external.NonSyncingListener;
 import org.xmodel.log.Log;
 import org.xmodel.log.SLog;
 import org.xmodel.xaction.IXAction;
 import org.xmodel.xaction.XAction;
 import org.xmodel.xaction.XActionDocument;
 import org.xmodel.xaction.XActionException;
 import org.xmodel.xaction.debug.Debugger;
 import org.xmodel.xaction.debug.Debugger.Operation;
 import org.xmodel.xml.IXmlIO.Style;
 import org.xmodel.xml.XmlIO;
 import org.xmodel.xpath.XPath;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.IExpression;
 import org.xmodel.xpath.expression.StatefulContext;
 
 /**
  * The protocol class for the NetworkCachingPolicy protocol.
  */
 public class Protocol implements ILink.IListener
 {
   public final static short version = 1;
   
   /**
    * Message type field (must be less than 32).
    */
   public enum Type
   {
     sessionOpenRequest,
     sessionOpenResponse,
     sessionCloseRequest,
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
     executeRequest,
     executeResponse,
     debugRequest,
     debugResponse
   }
 
   /**
    * Create an instance of the protocol.
    * @param timeout The timeout for network operations in milliseconds.
    */
   public Protocol( int timeout)
   {
     this.context = new StatefulContext();
     this.sessions = new ConcurrentHashMap<ILink, List<SessionInfo>>();
     this.sessionInitQueues = new ConcurrentHashMap<Long, BlockingQueue<Response>>();
     
     this.timeout = timeout;
     this.random = new Random();
     
     // allocate less than standard mtu
     buffer = ByteBuffer.allocate( 4096);
     buffer.order( ByteOrder.BIG_ENDIAN);
     
     dispatcher = new ImmediateDispatcher();
     serializer = new DefaultSerializer();
     packageNames = new ArrayList<String>();
     
     //Log.getLog( Protocol.class).setLevel( Log.all);
   }
   
   /**
    * Set the context in which remote xpath expressions will be bound.
    * @param context The context.
    */
   public void setServerContext( IContext context)
   {
     this.context = context;
     dispatcher = context.getModel().getDispatcher();
   }
   
   /**
    * Set the execution privilege (null disables execution restrictions).
    * @param privilege The privilege.
    */
   public void setExecutePrivilege( ExecutePrivilege privilege)
   {
     this.privilege = privilege;
   }
   
   /**
    * Set the dispatcher. It is not necessary to set the dispatcher using this method if either the
    * <code>setServerContext</code> method or <code>attach</code> method is called. 
    * @param dispatcher The dispatcher.
    */
   public void setDispatcher( IDispatcher dispatcher)
   {
     this.dispatcher = dispatcher;
   }
   
   /**
    * @return Returns null or the dispatcher.
    */
   public IDispatcher getDispatcher()
   {
     return dispatcher;
   }
   
   /**
    * Set the serializer.
    * @param serializer The serializer.
    */
   public void setSerializer( ISerializer serializer)
   {
     this.serializer = serializer;
   }
 
   /**
    * Add an XAction package to the packages that will be searched when a remote XAction invocation request is received.
    * This method is not thread-safe and should be called on the server-side before the server is started.
    * @param packageName The package name.
    */
   public void addPackage( String packageName)
   {
     packageNames.add( packageName);
   }
   
   /**
    * Open a session over the specified link and return the session.
    * @param link The link.
    * @return Returns the session.
    */
   public Session openSession( ILink link) throws IOException
   {
     int id = sendSessionOpenRequest( link, version);
     
     SessionInfo info = new SessionInfo();
     List<SessionInfo> list = sessions.get( link);
     if ( list == null)
     {
       list = new ArrayList<SessionInfo>( 1);
       sessions.put( link, list);
     }
     
     for( int i=list.size(); i<=id; i++) list.add( null);
     list.set( id, info);
     
     return new Session( this, link, id);
   }
   
   /**
    * Close a session over the specified link and return session.
    * @param link The link.
    * @param session The session number.
    */
   public void closeSession( ILink link, int session) throws IOException
   {
     sendSessionCloseRequest( link, session);
   }
   
   /**
    * Attach to the element on the specified xpath.
    * @param link The link.
    * @param session The session number.
    * @param xpath The XPath expression.
    * @param reference The reference.
    */
   public void attach( ILink link, int session, String xpath, IExternalReference reference) throws IOException
   {
     if ( link != null && link.isOpen())
     {
       SessionInfo info = getSession( link, session);
       if ( info.xpath != null) 
       {
         throw new IOException( "Protocol only supports one concurrent attach operation per session.");
       }
       
       info.xpath = xpath;
       info.element = reference;
       info.isAttachClient = true;
       info.dispatcher = reference.getModel().getDispatcher();
       
       if ( info.dispatcher == null) 
       {
         throw new IllegalStateException( "Client must define dispatcher.");
       }
       
       sendAttachRequest( link, session, xpath);
       
       info.listener = new Listener( link, session, xpath, reference);
       info.listener.install( reference);
     }
     else
     {
       throw new IOException( "Link not open.");
     }
   }
 
   /**
    * Detach from the element on the specified path.
    * @param link The link.
    * @param session The session number.
    */
   public void detach( ILink link, int session) throws IOException
   {
     sendDetachRequest( link, session);
     
     SessionInfo info = getSession( link, session);
     if ( info != null)
     {
       info.xpath = null;
       info.element = null;
       info.dispatcher = null;
     }
   }
 
   /**
    * Perform a remote query.
    * @param link The link.
    * @param session The session number.
    * @param context The local context.
    * @param query The query string.
    * @param timeout The timeout to wait for a response.
    * @return Returns null or the response.
    */
   public Object query( ILink link, int session, IContext context, String query, int timeout) throws IOException
   {
     if ( link != null && link.isOpen())
     {
       return sendQueryRequest( link, session, context, query, timeout);
     }
     else
     {
       throw new IOException( "Link not open.");
     }
   }
 
   /**
    * Perform a remote invocation of the specified script.
    * @param link The link.
    * @param session The session number.
    * @param context The local execution context.
    * @param variables The variables to be passed.
    * @param script The script to be executed.
    * @param timeout The timeout to wait for a response.
    * @return Returns null or the response.
    */
   public Object[] execute( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, int timeout) throws IOException
   {
     if ( link != null && link.isOpen())
     {
       return sendExecuteRequest( link, session, context, variables, script, timeout);
     }
     else
     {
       throw new IOException( "Link not open.");
     }
   }
 
   /**
    * Find the element on the specified xpath and attach listeners.
    * @param sender The sender.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param xpath The xpath expression.
    */
   protected void doAttach( ILink sender, int session, int correlation, String xpath) throws IOException
   {
     try
     {
       IExpression expr = XPath.compileExpression( xpath);
       IModelObject target = expr.queryFirst( context);
       IModelObject copy = null;
       if ( target != null)
       {
         SessionInfo info = getSession( sender, session);
         info.xpath = xpath;
         info.element = target;
         
         // make sure target is synced since that is what we are requesting
         target.getChildren();
         copy = encode( sender, session, target, true);
         
         info.listener = new Listener( sender, session, xpath, target);
         info.listener.install( target);
       }
       sendAttachResponse( sender, session, correlation, copy);
     }
     catch( Exception e)
     {
       sendError( sender, session, correlation, e.getMessage());
     }
   }
   
   /**
    * Remove listeners from the element on the specified xpath.
    * @param sender The sender.
    * @param session The session number.
    */
   protected void doDetach( ILink sender, int session)
   {
     SessionInfo info = getSession( sender, session);
     if ( info != null)
     {
       info.recvMap.clear();
       info.sendMap.clear();
       
       if ( info.listener != null)
       {
         info.listener.uninstall();
         info.listener = null;
       }
       
       info.xpath = null;
       info.element = null;
     }
   }
 
   /**
    * Execute the specified query and return the result.
    * @param sender The sender.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param request The query request.
    */
   protected void doQuery( ILink sender, int session, int correlation, IModelObject request) throws IOException
   {
     try
     {
       IExpression query = QueryProtocol.readRequest( request, context);
       
       Object result = null;
       switch( query.getType( context))
       {
         case NODES:   result = query.evaluateNodes( context); break;
         case STRING:  result = query.evaluateString( context); break;
         case NUMBER:  result = query.evaluateNumber( context); break;
         case BOOLEAN: result = query.evaluateBoolean( context); break;
       }
 
       sendQueryResponse( sender, session, correlation, result);
     }
     catch( PathSyntaxException e)
     {
       try { sendError( sender, session, correlation, e.getMessage());} catch( IOException e2) {}
     }
   }
   
   /**
    * Execute the specified script.
    * @param sender The sender.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param context The execution context.
    * @param script The script.
    */
   protected void doExecute( ILink sender, int session, int correlation, IContext context, IModelObject script)
   {
     XActionDocument doc = new XActionDocument( script);
     for( String packageName: packageNames)
       doc.addPackage( packageName);
 
     Object[] results = null;
     
     try
     {
       IXAction action = doc.getAction( script);
       if ( action == null) 
       {
         throw new XActionException( String.format(
           "Unable to resolve IXAction class: %s.", script.getType()));
       }
       
       context.set( "session", session);
       results = action.run( context);
     }
     catch( Throwable t)
     {
       SLog.errorf( this, "Execution failed for script: %s", XmlIO.write( Style.compact, script));
       SLog.exception( this, t);
       
       try
       {
         sendError( sender, session, correlation, String.format( "%s: %s", t.getClass().getName(), t.getMessage()));
       }
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
     
     try
     {
       sendExecuteResponse( sender, session, correlation, context, results);
     } 
     catch( IOException e)
     {
       SLog.errorf( this, "Unable to send execution response for script: %s", XmlIO.write( Style.compact, script));
       SLog.exception( this, e);
     }
   }
   
   /**
    * Close the specified session.
    * @param sender The sender.
    * @param session The session number.
    */
   protected void doSessionClose( ILink sender, int session)
   {
     deallocSession( sender, session);
   }
   
   /**
    * Close the specified session.
    * @param link The link.
    * @param session The session.
    */
   protected void doClose( ILink link, int session)
   {
     IExternalReference reference = null;
     
     SessionInfo info = getSession( link, session);
     if ( info != null && info.isAttachClient) reference = (IExternalReference)info.element;
     
     doDetach( link, session);
     deallocSession( link, session);
     
     if ( reference != null) reference.setDirty( true);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.IConnection.IListener#onClose(org.xmodel.net.IConnection)
    */
   @Override
   public void onClose( ILink link)
   {
     List<SessionInfo> list = getSessions( link);
     for( int i=0; i<list.size(); i++)
     {
       SessionInfo info = list.get( i);
       if ( info != null)
       {
         dispatch( info, new CloseRunnable( link, i));
       }
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.IConnection.IListener#onReceive(org.xmodel.net.IConnection, java.nio.ByteBuffer)
    */
   @Override
   public void onReceive( ILink link, ByteBuffer buffer)
   {
     buffer.mark();
     while( handleMessage( link, buffer)) buffer.mark();
     buffer.reset();
   }
   
   /**
    * Parse and handle one message from the specified buffer.
    * @param link The link.
    * @param buffer The buffer.
    * @return Returns true if a message was handled.
    */
   private final boolean handleMessage( ILink link, ByteBuffer buffer)
   {
     try
     {
       int byte0 = buffer.get();
       Type type = Type.values()[ byte0 & 0x1f];
       
       int session = readMessageSession( byte0, buffer);
       int correlation = readMessageCorrelation( byte0, buffer);
       
       int length = readMessageLength( byte0, buffer);
       if ( length > buffer.remaining()) return false;
       
       if ( SLog.isLevelEnabled( this, Log.verbose)) 
       {
         String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
         SLog.verbosef( this, "recv: session=%d, correlation=%d, content-length=%d\n%s", session, correlation, length, bytes);
       }
 
       switch( type)
       {
         case sessionOpenRequest:  handleSessionOpenRequest( link, buffer, length); return true;
         case sessionOpenResponse: handleSessionOpenResponse( link, session, buffer, length); return true;
         case sessionCloseRequest: handleSessionCloseRequest( link, session, buffer, length); return true;
         
         case error:           handleError( link, session, buffer, length); return true;
         case attachRequest:   handleAttachRequest( link, session, correlation, buffer, length); return true;
         case attachResponse:  handleAttachResponse( link, session, correlation, buffer, length); return true;
         case detachRequest:   handleDetachRequest( link, session, buffer, length); return true;
         case syncRequest:     handleSyncRequest( link, session, correlation, buffer, length); return true;
         case syncResponse:    handleSyncResponse( link, session, correlation, buffer, length); return true;
         case addChild:        handleAddChild( link, session, buffer, length); return true;
         case removeChild:     handleRemoveChild( link, session, buffer, length); return true;
         case changeAttribute: handleChangeAttribute( link, session, buffer, length); return true;
         case clearAttribute:  handleClearAttribute( link, session, buffer, length); return true;
         case changeDirty:     handleChangeDirty( link, session, buffer, length); return true;
         case queryRequest:    handleQueryRequest( link, session, correlation, buffer, length); return true;
         case queryResponse:   handleQueryResponse( link, session, correlation, buffer, length); return true;
         case executeRequest:  handleExecuteRequest( link, session, correlation, buffer, length); return true;
         case executeResponse: handleExecuteResponse( link, session, correlation, buffer, length); return true;
         
         case debugRequest:  handleDebugRequest( link, session, correlation, buffer, length); return true;
         case debugResponse: handleDebugResponse( link, session, correlation, buffer, length); return true;
       }
     }
     catch( BufferUnderflowException e)
     {
     }
     
     return false;
   }
   
   /**
    * Send an session open request.
    * @param link The link.
    * @param version The version.
    * @return Returns the session number from the session response.
    */
   public final int sendSessionOpenRequest( ILink link, short version) throws IOException
   {
     //
     // Create large random client identifier for demultiplexing session open responses.
     //
     long client = random.nextLong();
     
     initialize( buffer);
     buffer.putShort( version);
     buffer.putLong( client);
     finalize( buffer, Type.sessionOpenRequest, 0, 10);
     
     // log
     SLog.debugf( this, "Send Session Open Request: version=%d, client=%X", version, client);
     
     Response response = send( link, client, buffer, timeout);
     return response.correlation;
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSessionOpenRequest( ILink link, ByteBuffer buffer, int length)
   {
     int version = buffer.getShort();
     long client = buffer.getLong();
     if ( version != Protocol.version)
     {
       link.close();
       return;
     }
 
     try
     {
       sendSessionOpenResponse( link, allocSession( link), client);
     }
     catch( IOException e)
     {
       SLog.exception( this, e);
     }
   }
   
   /**
    * Send the session open response.
    * @param link The link.
    * @param session The session id.
    * @param client The client identifier.
    */
   public final void sendSessionOpenResponse( ILink link, int session, long client) throws IOException
   {
     initialize( buffer);
     buffer.putLong( client);
     finalize( buffer, Type.sessionOpenResponse, session, 8);
     
     // log
     SLog.debugf( this, "Send Session Open Response: session=%d, client=%X", session, client);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSessionOpenResponse( ILink link, int session, ByteBuffer buffer, int length)
   {
     long client = buffer.getLong();
     queueResponse( link, client, session, buffer, length - 8);
   }
   
   /**
    * Send an session close request.
    * @param link The link.
    * @param session The session number.
    */
   public final void sendSessionCloseRequest( ILink link, int session) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.sessionCloseRequest, session, 0);
     
     SLog.debugf( this, "Send Session Close Request: session=%d", session);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSessionCloseRequest( ILink link, int session, ByteBuffer buffer, int length)
   {
     dispatch( getSession( link, session), new SessionCloseRunnable( link, session));
   }
 
   /**
    * Send an error message.
    * @param link The link.
    * @param session The session number.
    * @param message The error message.
    */
   public final void sendError( ILink link, int session, int correlation, String message) throws IOException
   {
     initialize( buffer);
     byte[] bytes = message.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.error, session, correlation, bytes.length);
 
     // log
     SLog.debugf( this, "Send Error: session=%d, correlation=%d, message=%s", session, correlation, message);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleError( ILink link, int session, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     handleError( link, new String( bytes));
   }
   
   /**
    * Handle an error message.
    * @param link The link.
    * @param message The message.
    */
   protected void handleError( ILink link, String message)
   {
     SLog.error( this, message);
   }
   
   /**
    * Send an attach request message.
    * @param link The link.
    * @param session The session number.
    * @param query The query.
    */
   public final void sendAttachRequest( ILink link, int session, String query) throws IOException
   {
     SessionInfo info = getSession( link, session);
     
     initialize( buffer);
     byte[] bytes = query.getBytes();
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.attachRequest, session, ++info.correlation, bytes.length);
 
     // log
     SLog.debugf( this, "Attach Request: session=%d, correlation=%d, query=%s", session, info.correlation, query);
     
     // send and wait for response
     byte[] response = send( link, session, info.correlation, buffer, timeout);
     if ( response != null)
     {
       ICompressor compressor = info.compressor;
       IModelObject element = compressor.decompress( new ByteArrayInputStream( response));
       handleAttachResponse( link, session, element);
     }
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAttachRequest( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     handleAttachRequest( link, session, correlation, new String( bytes));
   }
   
   /**
    * Handle an attach request.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param xpath The xpath.
    */
   protected void handleAttachRequest( ILink link, int session, int correlation, String xpath)
   {
     dispatch( getSession( link, session), new AttachRunnable( link, session, correlation, xpath));
   }
 
   /**
    * Send an attach response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param element The element.
    */
   public final void sendAttachResponse( ILink link, int session, int correlation, IModelObject element) throws IOException
   {
     initialize( buffer);
     
     ICompressor compressor = getSession( link, session).compressor;
     byte[] bytes = compressor.compress( element);
     ByteBuffer content = ByteBuffer.wrap( bytes);
     
     finalize( buffer, Type.attachResponse, session, correlation, bytes.length);
 
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, element);
       SLog.debugf( this, "Send Attach Response: session=%d, correlation=%d, response=%s", session, correlation, xml);
     }
     
     send( link, buffer, session);
     send( link, content, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAttachResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     queueResponse( link, session, correlation, buffer, length);
   }
   
   /**
    * Handle an attach response.
    * @param link The link.
    * @param session The session number.
    * @param element The element.
    */
   protected void handleAttachResponse( ILink link, int session, IModelObject element)
   {
     IExternalReference attached = (IExternalReference)getSession( link, session).element;
     if ( attached != null)
     {
       ICachingPolicy cachingPolicy = attached.getCachingPolicy();
       cachingPolicy.update( attached, decode( link, session, element));
     }
   }
   
   /**
    * Send an detach request message.
    * @param link The link.
    * @param session The session number.
    */
   public final void sendDetachRequest( ILink link, int session) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.detachRequest, session, 0);
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDetachRequest( ILink link, int session, ByteBuffer buffer, int length)
   {
     handleDetachRequest( link, session);
   }
   
   /**
    * Handle an attach request.
    * @param link The link.
    * @param session The session number.
    */
   protected void handleDetachRequest( ILink link, int session)
   {
     dispatch( getSession( link, session), new DetachRunnable( link, session));
   }
   
   /**
    * Send an sync request message.
    * @param link The link.
    * @param session The session number.
    * @param key The key assigned to this reference.
    * @param reference The local reference.
    */
   public final void sendSyncRequest( ILink link, int session, Long key, IExternalReference reference) throws IOException
   {
     SessionInfo info = getSession( link, session);
     
     initialize( buffer);
     buffer.putInt( key.intValue());
     finalize( buffer, Type.syncRequest, session, ++info.correlation, 4);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, reference);
       SLog.debugf( this, "Send Sync Request: session=%d, reference=%s", session, info.correlation, xml);
     }
     
     // send and wait for response
     byte[] response = send( link, session, info.correlation, buffer, timeout);
     if ( response != null) handleSyncResponse( link, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSyncRequest( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     handleSyncRequest( link, session, correlation, key);
   }
   
   /**
    * Handle a sync request.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param key The reference key.
    */
   protected void handleSyncRequest( ILink sender, int session, int correlation, long key)
   {
     dispatch( getSession( sender, session), new SyncRunnable( sender, session, correlation, key));
   }
   
   /**
    * Send an sync response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    */
   public final void sendSyncResponse( ILink link, int session, int correlation) throws IOException
   {
     initialize( buffer);
     finalize( buffer, Type.syncResponse, session, correlation, 0);
     
     // log
     SLog.debugf( this, "Send Sync Response: session=%d, correlation=%d", session, correlation);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleSyncResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     queueResponse( link, session, correlation, buffer, length);
   }
   
   /**
    * Handle a sync response.
    * @param link The link.
    * @param session The session number.
    */
   protected void handleSyncResponse( ILink link, int session)
   {
     // Nothing to do here
   }
   
   /**
    * Send an add child message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param element The element.
    * @param index The insertion index.
    */
   public final void sendAddChild( ILink link, int session, long key, IModelObject element, int index) throws IOException
   {
     initialize( buffer);
     int length = 4;
     buffer.putInt( (int)key);
     length += writeElement( getSession( link, session).compressor, element);
     buffer.putInt( index); length += 4;
     finalize( buffer, Type.addChild, session, length);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, element);
       SLog.debugf( this, "Send Add Child: session=%d, parent=%X, index=%d, element=%s", session, key, index, xml);
     }
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleAddChild( ILink link, int session, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     byte[] bytes = readBytes( buffer, false);
     int index = buffer.getInt();
     handleAddChild( link, session, key, bytes, index);
   }
   
   /**
    * Handle an add child message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param child The child that was added as a compressed byte array.
    * @param index The insertion index.
    */
   protected void handleAddChild( ILink link, int session, long key, byte[] child, int index)
   {
     dispatch( getSession( link, session), new AddChildEvent( link, session, key, child, index));
   }
   
   /**
    * Process an add child event in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param bytes The child that was added.
    * @param index The index of insertion.
    */
   private void processAddChild( ILink link, int session, long key, byte[] bytes, int index)
   {
     try
     {
       updating = link;
       
       SessionInfo info = getSession( link, session);
       IModelObject attached = info.element;
       if ( attached == null) return;
       
       IModelObject parent = info.recvMap.get( key);
       IModelObject child = info.compressor.decompress( bytes, 0);
       if ( parent != null) 
       {
         IModelObject childElement = decode( link, session, child);
         parent.addChild( childElement, index);
       }
     }
     finally
     {
       updating = null;
     }
   }
   
   /**
    * Send an add child message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param index The insertion index.
    */
   public final void sendRemoveChild( ILink link, int session, long key, int index) throws IOException
   {
     initialize( buffer);
     int length = 4;
     buffer.putInt( (int)key);
     buffer.putInt( index); length += 4;
     finalize( buffer, Type.removeChild, session, length);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       SLog.debugf( this, "Send Remove Child: session=%d, parent=%X, index=%d", session, key, index);
     }
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleRemoveChild( ILink link, int session, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     int index = buffer.getInt();
     handleRemoveChild( link, session, key, index);
   }
   
   /**
    * Handle an remove child message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param index The insertion index.
    */
   protected void handleRemoveChild( ILink link, int session, long key, int index)
   {
     dispatch( getSession( link, session), new RemoveChildEvent( link, session, key, index));
   }
   
   /**
    * Process an remove child event in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param index The index of insertion.
    */
   private void processRemoveChild( ILink link, int session, long key, int index)
   {
     try
     {
       updating = link;
       
       SessionInfo info = getSession( link, session);
       IModelObject attached = info.element;
       if ( attached == null) return;
       
       IModelObject parent = info.recvMap.get( key);
       if ( parent != null) parent.removeChild( index);
     }
     finally
     {
       updating = null;
     }    
   }
   
   /**
    * Send an change attribute message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param node The attribute node.
    */
   public final void sendChangeAttribute( ILink link, int session, Long key, IModelObject node) throws IOException
   {
     byte[] bytes = serialize( node);
     
     initialize( buffer);
     int length = 4;
     buffer.putInt( key.intValue());
     length += writeString( node.getType());
     length += writeBytes( bytes, 0, bytes.length, true);
     finalize( buffer, Type.changeAttribute, session, length);
     
     // log
     SLog.debugf( this, "Send Change Attribute: session=%d, object=%X, attr=%s, value=%s", session, key, node.getType(), node.getValue());
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleChangeAttribute( ILink link, int session, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     String attrName = readString( buffer);
     byte[] attrValue = readBytes( buffer, true);
     handleChangeAttribute( link, session, key, attrName, deserialize( attrValue));
   }
   
   /**
    * Handle an attribute change message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param attrName The name of the attribute.
    * @param attrValue The attribute value.
    */
   protected void handleChangeAttribute( ILink link, int session, long key, String attrName, Object attrValue)
   {
     dispatch( getSession( link, session), new ChangeAttributeEvent( link, session, key, attrName, attrValue));
   }
   
   /**
    * Process an change attribute event in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param attrName The name of the attribute.
    * @param attrValue The new value.
    */
   private void processChangeAttribute( ILink link, int session, long key, String attrName, Object attrValue)
   {
     try
     {
       updating = link;
 
       SessionInfo info = getSession( link, session);
       IModelObject attached = info.element;
       if ( attached == null) return;
       
       // the empty string and null string both serialize as byte[ 0]
       if ( attrValue == null) attrValue = "";
 
       IModelObject element = info.recvMap.get( key);
       if ( element != null) element.setAttribute( attrName, attrValue);
     }
     finally
     {
       updating = null;
     }        
   }
   
   /**
    * Send a clear attribute message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param attrName The name of the attribute.
    */
   public final void sendClearAttribute( ILink link, int session, long key, String attrName) throws IOException
   {
     initialize( buffer);
     int length = 4;
     buffer.putInt( (int)key);
     length += writeString( attrName);
     finalize( buffer, Type.clearAttribute, session, length);
     
     // log
     SLog.debugf( this, "Send Clear Attribute: session=%d, object=%X, attr=%s", session, key, attrName);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleClearAttribute( ILink link, int session, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     String attrName = readString( buffer);
     handleClearAttribute( link, session, key, attrName);
   }
   
   /**
    * Handle an attribute change message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param attrName The name of the attribute.
    */
   protected void handleClearAttribute( ILink link, int session, long key, String attrName)
   {
     dispatch( getSession( link, session), new ClearAttributeEvent( link, session, key, attrName));
   }
 
   /**
    * Process a clear attribute event in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param attrName The name of the attribute.
    */
   private void processClearAttribute( ILink link, int session, long key, String attrName)
   {
     try
     {
       updating = link;
       
       SessionInfo info = getSession( link, session);
       IModelObject attached = info.element;
       if ( attached == null) return;
       
       IModelObject element = info.recvMap.get( key);
       if ( element != null) element.removeAttribute( attrName);
     }
     finally
     {
       updating = null;
     }    
   }
   
   /**
    * Send a change dirty message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param dirty The dirty state.
    */
   public final void sendChangeDirty( ILink link, int session, long key, boolean dirty) throws IOException
   {
     initialize( buffer);
     int length = 4;
     buffer.putInt( (int)key);
     buffer.put( dirty? (byte)1: 0); length++;
     finalize( buffer, Type.changeDirty, session, length);
     
     // log
     SLog.debugf( this, "Send Change Dirty: session=%d, object=%X, dirty=%s", session, key, Boolean.toString( dirty));
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleChangeDirty( ILink link, int session, ByteBuffer buffer, int length)
   {
     long key = buffer.getInt();
     boolean dirty = buffer.get() != 0;
     handleChangeDirty( link, session, key, dirty);
   }
   
   /**
    * Handle a change dirty message.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param dirty The dirty state.
    */
   protected void handleChangeDirty( ILink link, int session, long key, boolean dirty)
   {
     dispatch( getSession( link, session), new ChangeDirtyEvent( link, session, key, dirty));
   }
 
   /**
    * Process a change dirty event in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param key The remote node key.
    * @param dirty The new dirty state.
    */
   private void processChangeDirty( ILink link, int session, long key, boolean dirty)
   {
     try
     {
       updating = link;
 
       SessionInfo info = getSession( link, session);
       IModelObject attached = info.element;
       if ( attached == null) return;
       
       IModelObject element = info.recvMap.get( key);
       if ( element != null)
       {
         IExternalReference reference = (IExternalReference)element;
         reference.setDirty( dirty);
       }
     }
     finally
     {
       updating = null;
     }    
   }
 
   /**
    * Send a query request message.
    * @param link The link.
    * @param session The session number.
    * @param context The local query context.
    * @param query The query string.
    * @param timeout The timeout in milliseconds.
    */
   public final Object sendQueryRequest( ILink link, int session, IContext context, String query, int timeout) throws IOException
   {
     SessionInfo info = getSession( link, session);
     
     initialize( buffer);
     
     IModelObject request = QueryProtocol.buildRequest( context, query);
     ICompressor compressor = info.compressor;
     byte[] bytes = compressor.compress( request);
     buffer.put( bytes);
     
     finalize( buffer, Type.queryRequest, session, ++info.correlation, bytes.length);
     
     // log
     SLog.debugf( this, "Send Query Request: session=%d, query=%s", session, query);
     
     byte[] content = send( link, session, info.correlation, buffer, timeout);
     if ( content != null)
     {
       ModelObject response = (ModelObject)compressor.decompress( content, 0);
       return QueryProtocol.readResponse( response);
     }
     
     return null;
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleQueryRequest( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     
     ICompressor compressor = getSession( link, session).compressor;
     ModelObject request = (ModelObject)compressor.decompress( bytes, 0);
     
     handleQueryRequest( link, session, correlation, request);
   }
   
   /**
    * Handle a query requset.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param request The query request.
    */
   protected void handleQueryRequest( ILink link, int session, int correlation, IModelObject request)
   {
     dispatch( getSession( link, session), new QueryRunnable( link, session, correlation, request));
   }
   
   /**
    * Send a query response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param result The query result.
    */
   public final void sendQueryResponse( ILink link, int session, int correlation, Object object) throws IOException
   {
     initialize( buffer);
     ICompressor compressor = getSession( link, session).compressor;
     IModelObject response = QueryProtocol.buildResponse( object);
     byte[] bytes = compressor.compress( response);
     buffer.put( bytes);
     finalize( buffer, Type.queryResponse, session, correlation, bytes.length);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, response);
       SLog.debugf( this, "Send Query Response: session=%d, correlation=%d, response=%s", session, correlation, xml);
     }
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleQueryResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     queueResponse( link, session, correlation, buffer, length);
   }
   
   /**
    * Send an execute request message.
    * @param link The link.
    * @param session The session number.
    * @param context The local execution context.
    * @param variables The variables to be passed.
    * @param script The script to execute.
    * @param timeout The amount of time to wait for a response.
    * @return Returns null or the execution results.
    */
   public final Object[] sendExecuteRequest( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, int timeout) throws IOException
   {
     SessionInfo info = getSession( link, session);
     
     initialize( buffer);
     
     IModelObject request = ExecutionProtocol.buildRequest( context, variables, script);
     ICompressor compressor = info.compressor;
     byte[] bytes = compressor.compress( request);
     buffer.put( bytes);
     
     finalize( buffer, Type.executeRequest, session, ++info.correlation, bytes.length);
 
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, request);
       SLog.debugf( this, "Send Execute Request: session=%d, correlation=%d, request=%s", session, info.correlation, xml);
     }
     
     if ( timeout > 0)
     {
       byte[] response = send( link, session, info.correlation, buffer, timeout);
       if ( response != null)
       {
         ModelObject element = (ModelObject)compressor.decompress( response, 0);
         return ExecutionProtocol.readResponse( element, context);
       }
     }
     else
     {
       send( link, buffer, session);
     }
     
     return null;
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleExecuteRequest( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     byte[] content = new byte[ length];
     buffer.get( content);
     
     ICompressor compressor = getSession( link, session).compressor;
     IModelObject request = compressor.decompress( content, 0);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, request);
       SLog.debugf( this, "Handle Execute Request: session=%d, correlation=%d, request=%s", session, correlation, xml);
     }
     
     StatefulContext context = new StatefulContext( this.context);
     IModelObject script = ExecutionProtocol.readRequest( request, context);
     handleExecuteRequest( link, session, correlation, context, script);
   }
   
   /**
    * Handle an execute request.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param context The execution context.
    * @param script The script to execute.
    */
   protected void handleExecuteRequest( ILink link, int session, int correlation, IContext context, IModelObject script)
   {
     // check privilege
     if ( privilege != null && !privilege.isPermitted( context, script))
     {
       try
       {
         sendError( link, session, correlation, "Script contains restricted opertaions.");
       }
       catch( Exception e)
       {
         SLog.exception( this, e);
       }
     }
     
     // dispatch
     dispatch( getSession( link, session), new ExecuteRunnable( link, session, correlation, context, script));
   }
   
   /**
    * Send an attach response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param element The element.
    */
   public final void sendExecuteResponse( ILink link, int session, int correlation, IContext context, Object[] results) throws IOException
   {
     initialize( buffer);
     
     IModelObject response = ExecutionProtocol.buildResponse( context, results);
     ICompressor compressor = getSession( link, session).compressor;
     byte[] bytes = compressor.compress( response);
     
     int required = buffer.position() + bytes.length;
     if ( required >= buffer.limit())
     {
       buffer.flip();
       ByteBuffer larger = ByteBuffer.allocate( (int)(required * 1.5));
       larger.put( buffer);
       buffer = larger;
     }
     
     buffer.put( bytes);
     finalize( buffer, Type.executeResponse, session, correlation, bytes.length);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, response);
       SLog.debugf( this, "Send Execute Response: session=%d, correlation=%d, response=%s", session, correlation, xml);
     }
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleExecuteResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     queueResponse( link, session, correlation, buffer, length);
   }
   
   /**
    * Send an debug request message.
    * @param link The link.
    * @param session The session number.
    * @param operation The debug operation.
    * @param timeout The amount of time to wait for a response.
    * @return Returns null or the execution results.
    */
   public final IModelObject sendDebugRequest( ILink link, int session, Operation operation, int timeout) throws IOException
   {
     SessionInfo info = getSession( link, session);
     
     initialize( buffer);
     buffer.put( (byte)operation.ordinal());
     finalize( buffer, Type.debugRequest, session, ++info.correlation, 1);
 
     // log
     SLog.debugf( this, "Send Debug Request: operation=%s", operation.name());
     
     if ( timeout > 0)
     {
       byte[] response = send( link, session, info.correlation, buffer, timeout);
       if ( response != null) return (ModelObject)info.compressor.decompress( response, 0);
     }
     else
     {
       send( link, buffer, session);
     }
     
     return null;
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDebugRequest( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     Operation operation = Operation.values()[ buffer.get()];
     SLog.debugf( this, "Handle Debug Request: operation=%s", operation.name());
     handleDebugRequest( link, session, correlation, operation);
   }
   
   /**
    * Handle a debug request.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param operation The debug operation.
    */
   protected void handleDebugRequest( ILink link, int session, int correlation, Operation operation)
   {
     Debugger debugger = XAction.getDebugger();
     if ( debugger == null) return;
 
     switch( operation)
     {
       case stepOver: debugger.stepOver(); break;
       case stepIn:   debugger.stepIn(); break;
       case stepOut:  debugger.stepOut(); break;
       case resume:   debugger.resume(); break;
       case pause:    debugger.pause(); break;
     }
     
     try
     {
       //
       // Set the default context to the debugging context.
       //
       sendDebugResponse( link, session, correlation, debugger.getStack());
     }
     catch( IOException e)
     {
       SLog.exception( this, e);
     }
   }
   
   /**
    * Send a debug response.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param stack The stack.
    */
   public final void sendDebugResponse( ILink link, int session, int correlation, IModelObject stack) throws IOException
   {
     initialize( buffer);
     
     ICompressor compressor = getSession( link, session).compressor;
     byte[] bytes = compressor.compress( stack);
     
     int required = buffer.position() + bytes.length;
     if ( required >= buffer.limit())
     {
       buffer.flip();
       ByteBuffer larger = ByteBuffer.allocate( (int)(required * 1.5));
       larger.put( buffer);
       buffer = larger;
     }
     
     buffer.put( bytes);
     finalize( buffer, Type.debugResponse, session, correlation, bytes.length);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.printable, stack);
       SLog.debugf( this, "Send Debug Response: session=%d, correlation=%d\n%s", session, correlation, xml);
     }
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleDebugResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     queueResponse( link, session, correlation, buffer, length);
   }
   
   /**
    * Send a session open request and wait for a response.
    * @param link The link.
    * @param client The session initialization client identifier.
    * @param buffer The buffer to send.
    * @param timeout The timeout in milliseconds.
    * @return Returns null or the response buffer.
    */
   private Response send( ILink link, long client, ByteBuffer buffer, int timeout) throws IOException
   {
     try
     {
       SynchronousQueue<Response> queue = new SynchronousQueue<Response>();      
       sessionInitQueues.put( client, queue);
       
       send( link, buffer, -1);
       
       Response response = queue.poll( timeout, TimeUnit.MILLISECONDS);
       sessionInitQueues.remove( client);
       if ( response != null) return response;
     }
     catch( InterruptedException e)
     {
       return null;
     }
     
     throw new IOException( "Network request timeout.");
   }
   
   /**
    * Send and wait for a response.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer to send.
    * @param timeout The timeout in milliseconds.
    * @return Returns null or the response bytes.
    */
   private byte[] send( ILink link, int session, int correlation, ByteBuffer buffer, int timeout) throws IOException
   {
     if ( SLog.isLevelEnabled( this, Log.verbose)) 
     {
       String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
       int length = buffer.limit() - buffer.position();
       SLog.verbosef( this, "send: session=%d, correlation=%d, total-length=%d\n%s", session, correlation, length, bytes);
     }
     
     try
     {
       SessionInfo info = getSession( link, session);
       
       // drain response queue before sending next message
       info.responseQueue.clear();
       
       // send
       send( link, buffer, session);
 
       //
       // A protocol session is always used by the same model thread, so there will never
       // be more than one outstanding synchronous request.  This is why the queue is cleared
       // above before the message is sent.  Still, it is possible that a message with an 
       // old correlation will show up.
       //
       Response response = info.responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
       while( response != null)
       {
         if ( response.correlation == correlation) return response.bytes;
         response = info.responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
       } 
     }
     catch( InterruptedException e)
     {
       return null;
     }
     
     throw new IOException( "Network request timeout.");
   }
   
   /**
    * Send the specified buffer to the specified link.
    * @param link The link.
    * @param buffer The buffer.
    * @param session The session number.
    */
   private void send( ILink link, ByteBuffer buffer, int session) throws IOException
   {
     if ( SLog.isLevelEnabled( this, Log.verbose)) 
     {
       String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
       int length = buffer.limit() - buffer.position();
       SLog.verbosef( this, "send: session=%d, total-length=%d\n%s", session, length, bytes);
     }
     
     link.send( buffer);
   }
   
   /**
    * Queue a synchronous response.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param buffer The buffer containing the response.
    * @param length The length of the response.
    */
   private void queueResponse( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     SessionInfo info = getSession( link, session);
     if ( info != null) 
     {
       info.responseQueue.offer( new Response( correlation, bytes));
     }
   }
   
   /**
    * Queue a synchronous response.
    * @param link The link.
    * @param client The client identifier.
    * @param session The session number.
    * @param buffer The buffer containing the response.
    * @param length The length of the response.
    */
   private void queueResponse( ILink link, long client, int session, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     try 
     {
       BlockingQueue<Response> queue = sessionInitQueues.get( client);
       if ( queue != null) queue.put( new Response( session, bytes));
     } 
     catch( InterruptedException e) 
     {
     }
   }
   
   /**
    * Returns the serialized attribute value.
    * @param node The attribute node.
    * @return Returns the serialized attribute value.
    */
   private byte[] serialize( IModelObject node)
   {
     Object object = node.getValue();
     if ( object == null) return new byte[ 0];
     
     // use java serialization
     if ( object instanceof Serializable)
     {
       try
       {
         ByteArrayOutputStream bs = new ByteArrayOutputStream();
         DataOutputStream ds = new DataOutputStream( bs);
         serializer.writeObject( ds, node);
         ds.close();
         return bs.toByteArray();
       }
       catch( Exception e)
       {
         SLog.exception( this, e);
       }
     }
     
     return new byte[ 0];
   }
   
   /**
    * Returns the deserialized attribute value.
    * @param bytes The serialized attribute value.
    * @return Returns the deserialized attribute value.
    */
   private Object deserialize( byte[] bytes)
   {
     if ( bytes.length == 0) return null;
     
     // use java serialization
     try
     {
       ByteArrayInputStream bs = new ByteArrayInputStream( bytes);
       DataInputStream ds = new DataInputStream( bs);
       return serializer.readObject( ds);
     }
     catch( Exception e)
     {
       throw new CachingException( "Unable to deserialize object.", e);
     }
   }
   
   /**
    * Reserve the maximum amount of space for the header.
    * @param buffer The buffer.
    */
   public static void initialize( ByteBuffer buffer)
   {
     buffer.clear();
     buffer.position( 13);
   }
   
   /**
    * Read the correlation number from the specified buffer. This value is meaningless
    * if it equals Integer.MIN_VALUE. 
    * @param byte0 The first byte of the message.
    * @param buffer The buffer.
    * @return Returns the correlation number.
    */
   private static int readMessageCorrelation( int byte0, ByteBuffer buffer)
   {
     if ( (byte0 & correlationHeaderMask) != 0) return buffer.getInt();
     return Integer.MIN_VALUE;
   }
   
   /**
    * Read the session number from the buffer.
    * @param byte0 The first byte of the message.
    * @param buffer The buffer.
    * @return Returns the session number.
    */
   public static int readMessageSession( int byte0, ByteBuffer buffer)
   {
     int mask = byte0 & sessionHeaderMask;
     if ( mask == 0) return ((int)buffer.get()) & 0xFF;
     return buffer.getInt();
   }
   
   /**
    * Read the message length from the buffer.
    * @param byte0 The first byte of the message.
    * @param buffer The buffer.
    * @return Returns the message length.
    */
   public static int readMessageLength( int byte0, ByteBuffer buffer)
   {
     int mask = byte0 & lengthHeaderMask;
     if ( mask == 0) return ((int)buffer.get()) & 0xFF;
     return buffer.getInt();
   }
   
   /**
    * Finalize the message by writing the header (without correlation) and preparing the buffer to be read.
    * @param buffer The buffer.
    * @param type The message type.
    * @param session The session number.
    * @param length The message length.
    */
   public static void finalize( ByteBuffer buffer, Type type, int session, int length)
   {
     finalize( buffer, type, session, Integer.MIN_VALUE, length);
   }
   
   /**
    * Finalize the message by writing the header and preparing the buffer to be read.
    * @param buffer The buffer.
    * @param type The message type.
    * @param session The session number.
    * @param correlation The correlation number (Integer.MIN_VALUE to opt out).
    * @param length The message length.
    */
   public static void finalize( ByteBuffer buffer, Type type, int session, int correlation, int length)
   {
     buffer.limit( buffer.position());
     
     int mask = 0;
     int position = 13;
     if ( length < 256)
     {
       position -= 1;
       buffer.put( position, (byte)length);
     }
     else
     {
       mask |= lengthHeaderMask;
       position -= 4;
       buffer.putInt( position, length);
     }
     
     if ( correlation != Integer.MIN_VALUE)
     {
       mask |= correlationHeaderMask;
       position -= 4;
       buffer.putInt( position, correlation);
     }
     
     if ( session < 256)
     {
       position -= 1;
       buffer.put( position, (byte)session);
     }
     else
     {
       mask |= sessionHeaderMask;
       position -= 4;
       buffer.putInt( position, session);
     }
     
     position -= 1;
     buffer.put( position, (byte)(type.ordinal() | mask));
     
     buffer.position( position);
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
   static int writeLength( ByteBuffer buffer, int length, boolean small)
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
       ByteBuffer larger = ByteBuffer.allocate( (int)(required * 1.5));
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
    * @param link The link.
    * @param session The session number.
    * @param isRoot True if the element is the root of the attachment.
    * @param element The element to be copied.
    * @return Returns the copy.
    */
   protected IModelObject encode( ILink link, int session, IModelObject element, boolean isRoot)
   {
     element.getModel().setSyncLock( true);
 
     SessionInfo info = getSession( link, session);
     Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
     try
     {
       DepthFirstIterator iter = new DepthFirstIterator( element);
       while( iter.hasNext())
       {
         IModelObject lNode = iter.next();
         IModelObject rNode = map.get( lNode);
         IModelObject rParent = map.get( lNode.getParent());
         
         rNode = new ModelObject( lNode.getType());
         
         long key = System.identityHashCode( lNode);
         rNode.setAttribute( "net:key", key);
         info.recvMap.put( key, lNode);
         
         if ( lNode instanceof IExternalReference)
         {
           IExternalReference lRef = (IExternalReference)element;
           
           // enumerate static attributes for client
           for( String attrName: lRef.getStaticAttributes())
           {
             IModelObject entry = new ModelObject( "net:static");
             entry.setValue( attrName);
             rNode.addChild( entry);
           }
           
           // copy only static attributes if reference is dirty
           Xlate.set( rNode, "net:dirty", lNode.isDirty()? "1": "0");
           if ( lNode.isDirty())
           {
             // copy static attributes
             for( String attrName: lRef.getStaticAttributes())
             {
               Object attrValue = element.getAttribute( attrName);
               if ( attrValue != null) rNode.setAttribute( attrName, attrValue);
             }
           }
         }
         else
         {
           ModelAlgorithms.copyAttributes( lNode, rNode);
         }
         
         map.put( lNode, rNode);
         if ( rParent != null) rParent.addChild( rNode);
       }
     }
     finally
     {
       element.getModel().setSyncLock( false);
     }
     
     return map.get( element);
   }
   
   /**
    * Interpret the content of the specified server encoded subtree.
    * @param link The link.
    * @param session The session number.
    * @param root The root of the encoded subtree.
    * @return Returns the decoded element.
    */
   protected IModelObject decode( ILink link, int session, IModelObject root)
   {
     root.getModel().setSyncLock( true);
     
     SessionInfo info = getSession( link, session);
     Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
     try
     {
       DepthFirstIterator iter = new DepthFirstIterator( root);
       while( iter.hasNext())
       {
         IModelObject lNode = iter.next();
         if ( lNode.isType( "net:static")) continue;
         
         IModelObject rNode = map.get( lNode);
         IModelObject rParent = map.get( lNode.getParent());
         
         Long key = (Long)lNode.getAttribute( "net:key");
         Object dirty = lNode.getAttribute( "net:dirty");
         
         if ( dirty != null)
         {
           ExternalReference reference = new ExternalReference( lNode.getType());
           ModelAlgorithms.copyAttributes( lNode, reference);
           reference.removeAttribute( "net:key");
           reference.removeChildren( "net:static");
           
           NetKeyCachingPolicy cachingPolicy = new NetKeyCachingPolicy( this, link, session, key);
           cachingPolicy.setStaticAttributes( getStaticAttributes( lNode));
           reference.setCachingPolicy( cachingPolicy);
           
           reference.removeAttribute( "net:dirty");
           reference.setDirty( dirty.equals( "1"));
           
           rNode = reference;
         }
         else
         {
           rNode = new ModelObject( lNode.getType());
           ModelAlgorithms.copyAttributes( lNode, rNode);
         }
         
         info.sendMap.put( rNode, key);
         
         map.put( lNode, rNode);
         if ( rParent != null) rParent.addChild( rNode);
       }
     }
     finally
     {
       root.getModel().setSyncLock( false);
     }
     
     return map.get( root);
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
     public SyncRunnable( ILink sender, int session, int correlation, long key)
     {
       this.sender = sender;
       this.session = session;
       this.key = key;
     }
     
     public void run()
     {
       SessionInfo info = getSession( sender, session);
       IModelObject reference = info.recvMap.get( key);
       if ( reference != null)
       {
         try
         {
           reference.getChildren();
           sendSyncResponse( sender, session, correlation);
         }
         catch( IOException e)
         {
           SLog.exception( this, e);
         }
       }
     }
     
     private ILink sender;
     private int session;
     private int correlation;
     private long key;
   }
   
   private final class AddChildEvent implements Runnable
   {
     public AddChildEvent( ILink link, int session, long key, byte[] child, int index)
     {
       this.link = link;
       this.session = session;
       this.key = key;
       this.child = child;
       this.index = index;
     }
     
     public void run()
     {
       processAddChild( link, session, key, child, index);
     }
     
     private ILink link;
     private int session;
     private long key;
     private byte[] child;
     private int index;
   }
   
   private final class RemoveChildEvent implements Runnable
   {
     public RemoveChildEvent( ILink link, int session, long key, int index)
     {
       this.link = link;
       this.session = session;
       this.key = key;
       this.index = index;
     }
     
     public void run()
     {
       processRemoveChild( link, session, key, index);
     }
     
     private ILink link;
     private int session;
     private long key;
     private int index;
   }
   
   private final class ChangeAttributeEvent implements Runnable
   {
     public ChangeAttributeEvent( ILink link, int session, long key, String attrName, Object attrValue)
     {
       this.link = link;
       this.session = session;
       this.key = key;
       this.attrName = attrName;
       this.attrValue = attrValue;
     }
     
     public void run()
     {
       processChangeAttribute( link, session, key, attrName, attrValue);
     }
     
     private ILink link;
     private int session;
     private long key;
     private String attrName;
     private Object attrValue;
   }
   
   private final class ClearAttributeEvent implements Runnable
   {
     public ClearAttributeEvent( ILink link, int session, long key, String attrName)
     {
       this.link = link;
       this.session = session;
       this.key = key;
       this.attrName = attrName;
     }
     
     public void run()
     {
       processClearAttribute( link, session, key, attrName);
     }
     
     private ILink link;
     private int session;
     private long key;
     private String attrName;
   }
   
   private final class ChangeDirtyEvent implements Runnable
   {
     public ChangeDirtyEvent( ILink link, int session, long key, boolean dirty)
     {
       this.link = link;
       this.session = session;
       this.key = key;
       this.dirty = dirty;
     }
     
     public void run()
     {
       processChangeDirty( link, session, key, dirty);
     }
     
     private ILink link;
     private int session;
     private long key;
     private boolean dirty;
   }
   
   private final class AttachRunnable implements Runnable
   {
     public AttachRunnable( ILink sender, int session, int correlation, String xpath)
     {
       this.sender = sender;
       this.session = session;
       this.correlation = correlation;
       this.xpath = xpath;
     }
     
     public void run()
     {
       try
       {
         doAttach( sender, session, correlation, xpath);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
 
     private ILink sender;
     private int session;
     private int correlation;
     private String xpath;
   }
   
   private final class DetachRunnable implements Runnable
   {
     public DetachRunnable( ILink sender, int session)
     {
       this.sender = sender;
       this.session = session;
     }
     
     public void run()
     {
       doDetach( sender, session);
     }
 
     private ILink sender;
     private int session;
   }
   
   private final class QueryRunnable implements Runnable
   {
     public QueryRunnable( ILink sender, int session, int correlation, IModelObject request)
     {
       this.sender = sender;
       this.session = session;
       this.correlation = correlation;
       this.request = request;
     }
     
     public void run()
     {
       try
       {
         doQuery( sender, session, correlation, request);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
     
     private ILink sender;
     private int session;
     private int correlation;
     private IModelObject request;
   }
   
   private final class ExecuteRunnable implements Runnable
   {
     public ExecuteRunnable( ILink sender, int session, int correlation, IContext context, IModelObject script)
     {
       this.sender = sender;
       this.session = session;
       this.correlation = correlation;
       this.context = context;
       this.script = script;
     }
     
     public void run()
     {
       doExecute( sender, session, correlation, context, script.cloneTree());
     }
     
     private ILink sender;
     private int session;
     private int correlation;
     private IContext context;
     private IModelObject script;
   }
     
   private final class SessionCloseRunnable implements Runnable
   {
     public SessionCloseRunnable( ILink sender, int session)
     {
       this.sender = sender;
       this.session = session;
     }
     
     public void run()
     {
       doSessionClose( sender, session);
     }
     
     private ILink sender;
     private int session;
   }
   
   private final class CloseRunnable implements Runnable
   {
     public CloseRunnable( ILink sender, int session)
     {
       this.sender = sender;
       this.session = session;
     }
     
     public void run()
     {
       doClose( sender, session);
     }
     
     private ILink sender;
     private int session;
   }
   
   protected class Listener extends NonSyncingListener
   {
     public Listener( ILink sender, int session, String xpath, IModelObject root)
     {
       this.sender = sender;
       this.session = session;
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
       
       if ( updating == sender) return;
       
       try
       {
         IModelObject clone = encode( sender, session, child, false);
         long key = System.identityHashCode( parent);
         sendAddChild( sender, session, key, clone, index);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
      */
     @Override
     public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
     {
       super.notifyRemoveChild( parent, child, index);
       
       if ( updating == sender) return;
       
       try
       {
         long key = System.identityHashCode( parent);
         sendRemoveChild( sender, session, key, index);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
      */
     @Override
     public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
     {
       if ( updating == sender) return;
 
       try
       {
         long key = System.identityHashCode( object);
         sendChangeAttribute( sender, session, key, object.getAttributeNode( attrName));
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
 
     /* (non-Javadoc)
      * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
      */
     @Override
     public void notifyClear( IModelObject object, String attrName, Object oldValue)
     {
       if ( updating == sender) return;
       
       try
       {
         long key = System.identityHashCode( object);
         sendClearAttribute( sender, session, key, attrName);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
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
       ICachingPolicy cachingPolicy = ((IExternalReference)object).getCachingPolicy();
       if ( cachingPolicy instanceof NetworkCachingPolicy) return;
       if ( cachingPolicy instanceof NetKeyCachingPolicy) return;
       
       if ( updating == sender) return;
       
       try
       {
         long key = System.identityHashCode( object);
         sendChangeDirty( sender, session, key, dirty);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
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
 
     private ILink sender;
     private int session;
     private String xpath;
     private IModelObject root;
   };
   
   /**
    * Dispatch the specified Runnable using either the session dispatcher or the context dispatcher.
    * @param info The session state object.
    * @param runnable The runnable.
    */
   protected void dispatch( SessionInfo info, Runnable runnable)
   {
     if ( info.dispatcher != null)
     {
       info.dispatcher.execute( runnable);
     }
     else if ( dispatcher != null) 
     {
       dispatcher.execute( runnable);
     }
   }
   
   /**
    * Allocate a session.
    * @param link The link.
    * @return Returns the session number.
    */
   private final int allocSession( ILink link)
   {
     synchronized( sessions)
     {
       List<SessionInfo> list = sessions.get( link);
       if ( list == null)
       {
         list = new ArrayList<SessionInfo>();
         sessions.put( link, list);
       }
       
       for( int i=0; i<list.size(); i++)
       {
         if ( list.get( i) == null)
         {
           list.set( i, new SessionInfo());
           return i;
         }
       }
       
       list.add( new SessionInfo());
       return list.size() - 1;
     }
   }
   
   /**
    * Deallocate a session.
    * @param link The link.
    * @param session The session number.
    */
   private final void deallocSession( ILink link, int session)
   {
     synchronized( sessions)
     {
       List<SessionInfo> list = sessions.get( link);
       if ( list != null) list.set( session, null);
     }
   }
   
   /**
    * Returns the SessionInfo object for the specified session on the specified link.
    * @param link The link.
    * @param session The session.
    * @return Returns the SessionInfo object for the specified session on the specified link.
    */
   protected final SessionInfo getSession( ILink link, int session)
   {
     synchronized( sessions)
     {
       List<SessionInfo> list = sessions.get( link);
       if ( list != null) return list.get( session);
       return null;
     }
   }
   
   /**
    * Returns the SessionInfo objects on the specified link.
    * @param link The link.
    * @return Returns the SessionInfo objects on the specified link.
    */
   protected final List<SessionInfo> getSessions( ILink link)
   {
     synchronized( sessions)
     {
       List<SessionInfo> list = sessions.get( link);
       if ( list == null) return Collections.emptyList();
       
       List<SessionInfo> result = new ArrayList<SessionInfo>();
       for( SessionInfo info: list)
       {
         if ( info != null) result.add( info);
       }
       
       return result;
     }
   }
   
   protected class SessionInfo
   {
     public SessionInfo()
     {
       responseQueue = new SynchronousQueue<Response>();
       compressor = new TabularCompressor();
       recvMap = new HashMap<Long, IModelObject>();
       sendMap = new WeakHashMap<IModelObject, Long>();
     }
 
     public int correlation;
     public BlockingQueue<Response> responseQueue;
     public IDispatcher dispatcher;
     public String xpath;
     public IModelObject element;
     public boolean isAttachClient;
     public Map<Long, IModelObject> recvMap;
     public Map<IModelObject, Long> sendMap;
     public TabularCompressor compressor;
     public Listener listener;
   }
   
   protected final class Response
   {
     public Response( int correlation, byte[] bytes)
     {
       this.correlation = correlation;
       this.bytes = bytes;
     }
     
     public final int correlation;
     public final byte[] bytes;
   }
   
   private final static int lengthHeaderMask = 0x20;
   private final static int sessionHeaderMask = 0x40;
   private final static int correlationHeaderMask = 0x80;
   
   private Map<ILink, List<SessionInfo>> sessions;
   private Map<Long, BlockingQueue<Response>> sessionInitQueues;
   private IContext context;
   private ByteBuffer buffer;
   private int timeout;
   private Random random;
   private IDispatcher dispatcher;
   private ISerializer serializer;
   private List<String> packageNames;
   private ILink updating;
   private ExecutePrivilege privilege;
 }
