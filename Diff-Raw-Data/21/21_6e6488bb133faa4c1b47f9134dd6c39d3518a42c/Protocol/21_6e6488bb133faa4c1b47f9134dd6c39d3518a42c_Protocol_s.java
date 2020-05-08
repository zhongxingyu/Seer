 package org.xmodel.net;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.xmodel.BreadthFirstIterator;
 import org.xmodel.DepthFirstIterator;
 import org.xmodel.IDispatcher;
 import org.xmodel.IModelObject;
 import org.xmodel.ModelAlgorithms;
 import org.xmodel.ModelObject;
 import org.xmodel.PathSyntaxException;
 import org.xmodel.Xlate;
 import org.xmodel.compress.DefaultSerializer;
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
 import org.xmodel.xaction.XActionDocument;
 import org.xmodel.xaction.XActionException;
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
   public final static short version = 3;
   
   /**
    * Message type field (must be less than 32).
    */
   public enum Type
   {
     closeSession,
     pingRequest,
     pingResponse,
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
     executeResponse
   }
   
   public enum ExecuteStatus { success, timeout, error};
 
   /**
    * Create an instance of the protocol.
    */
   public Protocol()
   {
     this.sessionMap = new HashMap<ILink, List<SessionInfo>>();
     this.context = new StatefulContext();
     this.random = new Random();
     this.serializer = new DefaultSerializer();
     this.packageNames = new ArrayList<String>();
     
     //Log.getLog( this).setLevel( Log.problems | Log.debug);
   }
   
   /**
    * Set the context in which remote xpath expressions will be bound.
    * @param context The context.
    */
   public void setServerContext( IContext context)
   {
     this.context = context;
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
     if ( dispatcher == null) dispatcher = context.getModel().getDispatcher();
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
   public synchronized Session openSession( ILink link) throws IOException
   {
     List<SessionInfo> sessions = sessionMap.get( link);
     if ( sessions == null)
     {
       sessions = new ArrayList<SessionInfo>();
       sessionMap.put( link, sessions);
     }
     
     int session = sessions.size();
     for( int i=0; i<sessions.size(); i++)
     {
       if ( sessions.get( i) == null)
       {
         session = i;
         break;
       }
     }
 
     if ( session >= 256) throw new IOException( "Maximum 256 sessions supported.");
     
     if ( session == sessions.size()) sessions.add( null);
     sessions.set( session, new SessionInfo( session));
    
     return new Session( this, link, session);
   }
   
   /**
    * Close a session over the specified link and return session.
    * @param link The link.
    * @param session The session number.
    */
   public synchronized void closeSession( ILink link, int session) throws IOException
   {
     List<SessionInfo> sessions = sessionMap.get( link);
     if ( sessions != null) sessions.set( session, null);
   }
   
   /**
    * Returns the session for the specified link.
    * @param link The link.
    * @param session The session.
    * @return Returns the session.
    */
   private synchronized SessionInfo getSessionInfo( ILink link, int session)
   {
     List<SessionInfo> sessions = sessionMap.get( link);
     if ( sessions == null)
     {
       sessions = new ArrayList<SessionInfo>();
       sessionMap.put( link, sessions);
     }
     
     if ( sessions.size() <= session)
     {
       for( int i=sessions.size(); i<=session; i++)
         sessions.add( null);
     }
     
     SessionInfo info = sessions.get( session);
     if ( info == null)
     {
       info = new SessionInfo( session);
       sessions.set( session, info);
     }
     
     return info;
   }
   
   /**
    * Attach to the element on the specified xpath.  An attachment functions in one of two ways: mirroring, or
    * publish/subscribe.  A mirroring attachment mirrors updates between the client and the server.  In the
    * publish/subscribe mode, updates are only sent from the server to the client, and only add-child events
    * are sent.  Furthermore, add-child events do not include an insertion index - the child is assumed to be
    * added to the end of the list of children.  This is an efficient mechanism for sending events.  The client
    * and the server are responsible for managing the size of their event lists by deleting event elements that
    * have been processed.
    * @param link The link.
    * @param session The session number.
    * @param timeout The timeout in milliseconds.
    * @param readonly True if attachment is read-only.
    * @param xpath The XPath expression.
    * @param reference The reference.
    */
   public void attach( ILink link, int session, int timeout, boolean readonly, String xpath, IExternalReference reference) throws IOException
   {
     if ( link != null && link.isOpen())
     {
       SessionInfo info = getSessionInfo( link, session);
       if ( info.xpath != null) 
       {
         throw new IOException( "Protocol only supports one concurrent attach operation per session.");
       }
       
       info.xpath = xpath;
       info.element = new WeakReference<IModelObject>( reference);
       info.isAttachClient = true;
       info.isAttachReadonly = readonly;
       info.dispatcher = reference.getModel().getDispatcher();
       
       if ( info.dispatcher == null) 
       {
         throw new IllegalStateException( "Client must define dispatcher.");
       }
       
       sendAttachRequest( link, session, timeout, readonly, xpath);
 
       if ( !readonly)
       {
         info.listener = new Listener( link, session, readonly, xpath, reference, random);
         info.listener.install( reference);
       }
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
     
     SessionInfo info = getSessionInfo( link, session);
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
    * Perform a remote invocation of the specified script.
    * @param link The link.
    * @param session The session number.
    * @param context The local execution context.
    * @param variables The variables to be passed.
    * @param script The script to be executed.
    * @param callback The callback.
    * @param timeout The timeout to wait for a response.
    */
   public void execute( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, ICallback callback, int timeout) throws IOException
   {
     if ( link != null && link.isOpen())
     {
       sendExecuteRequest( link, session, context, variables, script, callback, timeout);
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
    * @param readonly True if attachment is publish/subscribe.
    * @param xpath The xpath expression.
    */
   protected void doAttach( ILink sender, int session, int correlation, boolean readonly, String xpath) throws IOException
   {
     try
     {
       IExpression expr = XPath.compileExpression( xpath);
       IModelObject target = expr.queryFirst( context);
       if ( target != null)
       {
         SessionInfo info = getSessionInfo( sender, session);
         info.isAttachReadonly = readonly;
         info.xpath = xpath;
         info.element = new WeakReference<IModelObject>( target);
         
         IModelObject copy = encode( sender, session, target, true);
         sendAttachResponse( sender, session, correlation, copy);
         
         info.listener = new Listener( sender, session, readonly, xpath, target, null);
         info.listener.install( target);
       }
       else
       {
         sendAttachResponse( sender, session, correlation, null);
       }
     }
     catch( Exception e)
     {
       SLog.exception( this, e);
       sendError( sender, session, correlation, e.getMessage());
     }
   }
   
   /**
    * Remove listeners from the element on the specified xpath.
    * @param sender The sender.
    * @parma info The SessionInfo instance.
    */
   protected void doDetach( ILink sender, SessionInfo info)
   {
     info.attachMap.clear();
     
     if ( info.listener != null)
     {
       info.listener.uninstall();
       info.listener = null;
     }
     
     info.xpath = null;
     info.element = null;
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
    * Process a sync request in the appropriate thread.
    * @param link The link.
    * @param session The session number.
    * @param correlation The message correlation number.
    * @param key The remote node key.
    */
   protected void doSync( ILink link, int session, int correlation, long key)
   {
     SessionInfo info = getSessionInfo( link, session);
     WeakReference<IModelObject> elementRef = info.attachMap.get( key);
     if ( elementRef != null)
     {
       IModelObject reference = elementRef.get();
       if ( reference != null)
       {
         try
         {
           reference.getChildren();
           sendSyncResponse( link, session, correlation);
         }
         catch( IOException e)
         {
           SLog.exception( this, e);
         }
       }
     }
   }
   
   /**
    * Execute the specified script.
    * @param sender The sender.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param request The request.
    */
   protected void doExecute( ILink sender, int session, int correlation, IModelObject request)
   {
     // denature request
     BreadthFirstIterator iter = new BreadthFirstIterator( request);
     while( iter.hasNext()) iter.next().clearModel();
     
     // create execution context and extract script from request
     StatefulContext context = new StatefulContext( this.context);
     IModelObject script = ExecutionProtocol.readRequest( request, context);
 
     // check privilege
     if ( privilege != null && !privilege.isPermitted( context, script))
     {
       try
       {
         sendError( sender, session, correlation, "Script contains restricted operations.");
       }
       catch( Exception e)
       {
         SLog.exception( this, e);
       }
     }
     
     // compile script and execute
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
 
       //
       // Store the session in the context so that the caller can implement an asynchronous callback
       // by invoking a remote run back through the calling session.
       //
       IModelObject holder = new ModelObject( "holder");
       holder.setValue( new Session( this, sender, session));
       context.set( "session", holder);
       
       // execute
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
     finally
     {
       // cleanup
       context.set( "session", Collections.<IModelObject>emptyList());
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
   
   /* (non-Javadoc)
    * @see org.xmodel.net.IConnection.IListener#onClose(org.xmodel.net.IConnection)
    */
   @Override
   public void onClose( ILink link)
   {
     List<SessionInfo> sessions = new ArrayList<SessionInfo>();
     
     synchronized( this) 
     { 
       List<SessionInfo> list = sessionMap.remove( link);
       if ( list != null) sessions.addAll( list);
     }
     
     for( int i=0; i<sessions.size(); i++)
     {
       SessionInfo info = sessions.get( i);
       if ( info != null) dispatch( info, new CloseSessionRunnable( link, info));
     }
   }
 
   /**
    * Close the specified session.
    * @param link The link.
    * @param info The SessionInfo instance.
    */
   protected void doCloseSession( ILink link, SessionInfo info)
   {
     IExternalReference reference = info.isAttachClient? (IExternalReference)info.getAttached(): null;
     doDetach( link, info);
     if ( reference != null) reference.setDirty( true);
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
 
       Log log = Log.getLog( this);
       if ( log.isLevelEnabled( Log.debug)) 
       {
         log.debugf( "recv: session=%X, correlation=%d, content-length=%d", session, correlation, length);
       }
       if ( log.isLevelEnabled( Log.verbose)) 
       {
         String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
         log.verbosef( "bytes received:\n%s", bytes);
       }
       
       switch( type)
       {
         case closeSession:    handleCloseSession( link, session); return true;
         
         case pingRequest:     handlePingRequest( link); return true;
         case pingResponse:    return true;
         
         case error:           handleError( link, session, correlation, buffer, length); return true;
         
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
       }
     }
     catch( BufferUnderflowException e)
     {
     }
     
     return false;
   }
   
   /**
    * Send a message telling the server to free the specified session.
    * @param link The link.
    * @param session The session number.
    */
   public final void sendCloseSession( ILink link, int session) throws IOException
   {
     SLog.debugf( this, "Send Close Session: session=%X", session);
     ByteBuffer buffer = initialize( 0);
     finalize( buffer, Type.closeSession, session, 0);
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message type.
    * @param link The link.
    * @param session The session number.
    */
   private final void handleCloseSession( ILink link, int session)
   {
     SessionInfo info = getSessionInfo( link, session);
     dispatch( info, new CloseSessionRunnable( link, info));
   }
   
   /**
    * Handle the specified message type.
    * @param link The link.
    */
   private final void handlePingRequest( ILink link)
   {
     SLog.debugf( this, "Handle Ping Request for %s", link);
     
     try
     {
       sendPingResponse( link);
     }
     catch( IOException e)
     {
       SLog.infof( this, "Failed to send ping response: %s", e.getMessage());
     }
   }
   
   /**
    * Send a ping request.
    * @param link The link.
    */
   public final void sendPingRequest( ILink link) throws IOException
   {
     SLog.debug( this, "Send Ping Request");
     ByteBuffer buffer = initialize( 0);
     finalize( buffer, Type.pingRequest, -1);
     send( link, buffer, -1);
   }
   
   /**
    * Send a ping response.
    * @param link The link.
    */
   public final void sendPingResponse( ILink link) throws IOException
   {
     SLog.debug( this, "Send Ping Response");
     ByteBuffer buffer = initialize( 0);
     finalize( buffer, Type.pingResponse, -1);
     send( link, buffer, -1);
   }
   
   /**
    * Send an error message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The message correlation number.
    * @param message The error message.
    */
   public final void sendError( ILink link, int session, int correlation, String message) throws IOException
   {
     if ( message == null) message = "";
     
     byte[] bytes = message.getBytes();
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.error, session, correlation);
 
     // log
     SLog.debugf( this, "Send Error: session=%X, correlation=%d, message=%s", session, correlation, message);
     
     send( link, buffer, session);
   }
   
   /**
    * Handle the specified message buffer.
    * @param link The link.
    * @param session The session number.
    * @param correlation The message correlation number.
    * @param buffer The buffer.
    * @param length The length of the message.
    */
   private final void handleError( ILink link, int session, int correlation, ByteBuffer buffer, int length)
   {
     byte[] bytes = new byte[ length];
     buffer.get( bytes);
     handleError( link, session, correlation, new String( bytes));
   }
   
   /**
    * Handle an error message.
    * @param link The link.
    * @param correlation The message correlation number.
    * @param message The message.
    */
   protected void handleError( ILink link, int session, int correlation, String message)
   {
     SLog.error( this, message);
     
     SessionInfo info = getSessionInfo( link, session);
     if ( info != null)
     {
       AsyncCallback runnable = info.callbacks.remove( correlation);
       if ( runnable != null && (runnable.task == null || runnable.task.cancel()))
       {
         runnable.setError( message);
         runnable.context.getModel().getDispatcher().execute( runnable);
       }
     }
   }
   
   /**
    * Send an attach request message.
    * @param link The link.
    * @param session The session number.
    * @param timeout The timeout in milliseconds.
    * @param pubsub True if attachment is publish/subscribe.
    * @param query The query.
    */
   public final void sendAttachRequest( ILink link, int session, int timeout, boolean pubsub, String query) throws IOException
   {
     SessionInfo info = getSessionInfo( link, session);
     int correlation = info.correlation.incrementAndGet();
     
     byte[] bytes = query.getBytes();
     ByteBuffer buffer = initialize( 1 + bytes.length);
     buffer.put( pubsub? (byte)1: (byte)0);
     buffer.put( bytes, 0, bytes.length);
     finalize( buffer, Type.attachRequest, session, correlation);
 
     // log
     SLog.debugf( this, "Attach Request: session=%X, correlation=%d, pubsub=%s, query=%s", session, correlation, pubsub, query);
     
     // send and wait for response
     byte[] response = send( link, session, correlation, buffer, timeout);
     if ( response != null)
     {
       if ( response.length > 0)
       {
         IModelObject element = info.compressor.decompress( new ByteArrayInputStream( response));
         handleAttachResponse( link, session, element);
       }
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
     boolean pubsub = (buffer.get() == 1)? true: false;
     byte[] bytes = new byte[ length - 1];
     buffer.get( bytes);
     handleAttachRequest( link, session, correlation, pubsub, new String( bytes));
   }
   
   /**
    * Handle an attach request.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param pubsub True if attachment is publish/subscribe.
    * @param xpath The xpath.
    */
   protected void handleAttachRequest( ILink link, int session, int correlation, boolean pubsub, String xpath)
   {
     dispatch( getSessionInfo( link, session), new AttachRunnable( link, session, correlation, pubsub, xpath));
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
     SessionInfo info = getSessionInfo( link, session);
     
     byte[] bytes = (element != null)? info.compress( element): new byte[ 0];
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.attachResponse, session, correlation);
 
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = (element != null)? XmlIO.write( Style.compact, element): "(null)";
       SLog.debugf( this, "Send Attach Response: session=%X, correlation=%d, response=%s", session, correlation, xml);
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
     SessionInfo info = getSessionInfo( link, session);
     IExternalReference attached = (IExternalReference)info.getAttached();
     if ( attached != null)
     {
       ICachingPolicy cachingPolicy = attached.getCachingPolicy();
       long key = Xlate.get( element, "net:key", 0L);
       IModelObject decoded = decode( link, session, element);
       info.attachMap.put( key, new WeakReference<IModelObject>( attached));
       cachingPolicy.update( attached, decoded);
     }
   }
   
   /**
    * Send an detach request message.
    * @param link The link.
    * @param session The session number.
    */
   public final void sendDetachRequest( ILink link, int session) throws IOException
   {
     ByteBuffer buffer = initialize( 0);
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
     SessionInfo info = getSessionInfo( link, session);
     dispatch( info, new DetachRunnable( link, info));
   }
   
   /**
    * Send an sync request message.
    * @param link The link.
    * @param session The session number.
    * @param timeout The timeout in milliseconds.
    * @param key The key assigned to this reference.
    * @param reference The local reference.
    */
   public final void sendSyncRequest( ILink link, int session, int timeout, Long key, IExternalReference reference) throws IOException
   {
     SessionInfo info = getSessionInfo( link, session);
     int correlation = info.correlation.incrementAndGet();
     
     ByteBuffer buffer = initialize( 4);
     buffer.putInt( key.intValue());
     finalize( buffer, Type.syncRequest, session, correlation);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, reference);
       SLog.debugf( this, "Send Sync Request: session=%X, reference=%s", session, correlation, xml);
     }
     
     // send and wait for response
     byte[] response = send( link, session, correlation, buffer, timeout);
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
     dispatch( getSessionInfo( sender, session), new SyncRunnable( sender, session, correlation, key));
   }
   
   /**
    * Send an sync response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    */
   public final void sendSyncResponse( ILink link, int session, int correlation) throws IOException
   {
     ByteBuffer buffer = initialize( 0);
     finalize( buffer, Type.syncResponse, session, correlation);
     
     // log
     SLog.debugf( this, "Send Sync Response: session=%X, correlation=%d", session, correlation);
     
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
     SessionInfo info = getSessionInfo( link, session);
     byte[] bytes = info.compress( element);
     
     ByteBuffer buffer = initialize( 12 + bytes.length);
     buffer.putInt( (int)key);
     writeBytes( buffer, bytes, 0, bytes.length, false);
     buffer.putInt( index);
     finalize( buffer, Type.addChild, session);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, element);
       SLog.debugf( this, "Send Add Child: session=%X, parent=%X, index=%d, element=%s", session, key, index, xml);
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
     SLog.verbosef( this, "handleAddChild( %X, %d, %X, %d)\n", link.hashCode(), session, key, index);
     dispatch( getSessionInfo( link, session), new AddChildEvent( link, session, key, child, index));
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
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       info.listener.setEnabled( false);
       
       IModelObject attached = info.getAttached();
       if ( attached == null) return;
       
       WeakReference<IModelObject> parentRef = info.attachMap.get( key);
       if ( parentRef != null)
       {
         IModelObject parent = parentRef.get();
         IModelObject child = info.decompress( bytes, 0);
         
         SLog.verbosef( this, "processAddChild( %X, %d, %X, %s, %s, %d)\n", link.hashCode(), session, key, parent, child, index);
         
         if ( parent != null) 
         {
           IModelObject childElement = decode( link, session, child);
           parent.addChild( childElement, index);
         }
       }
     }
     finally
     {
       info.listener.setEnabled( true);
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
     ByteBuffer buffer = initialize( 8);
     buffer.putInt( (int)key);
     buffer.putInt( index);
     finalize( buffer, Type.removeChild, session, 8);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       SLog.debugf( this, "Send Remove Child: session=%X, parent=%X, index=%d", session, key, index);
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
     dispatch( getSessionInfo( link, session), new RemoveChildEvent( link, session, key, index));
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
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       info.listener.setEnabled( false);
       
       IModelObject attached = info.getAttached();
       if ( attached == null) return;
       
       WeakReference<IModelObject> parentRef = info.attachMap.get( key);
       if ( parentRef != null)
       {
         IModelObject parent = parentRef.get();
         
         SLog.verbosef( this, "processRemoveChild( %X, %d, %X, %s, %d)\n", link.hashCode(), session, key, parent, index);
         
         if ( parent != null) parent.removeChild( index);
       }
     }
     finally
     {
       info.listener.setEnabled( true);
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
     byte[] typeBytes = node.getType().getBytes();
     byte[] valueBytes = serialize( node);
     
     ByteBuffer buffer = initialize( 12 + typeBytes.length + valueBytes.length);
     buffer.putInt( key.intValue());
     writeBytes( buffer, typeBytes, 0, typeBytes.length, true);
     writeBytes( buffer, valueBytes, 0, valueBytes.length, false);
     finalize( buffer, Type.changeAttribute, session);
     
     // log
     SLog.debugf( this, "Send Change Attribute: session=%X, object=%X, attr=%s, value=%s", session, key, node.getType(), node.getValue());
     
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
     String attrName = new String( readBytes( buffer, true));
     byte[] attrValue = readBytes( buffer, false);
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
     dispatch( getSessionInfo( link, session), new ChangeAttributeEvent( link, session, key, attrName, attrValue));
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
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       info.listener.setEnabled( false);
       
       IModelObject attached = info.getAttached();
       if ( attached == null) return;
       
       // the empty string and null string both serialize as byte[ 0]
       if ( attrValue == null) attrValue = "";
 
       WeakReference<IModelObject> elementRef = info.attachMap.get( key);
       if ( elementRef != null)
       {
         IModelObject element = elementRef.get();
         if ( element != null) element.setAttribute( attrName, attrValue);
       }
     }
     finally
     {
       info.listener.setEnabled( true);
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
     byte[] nameBytes = attrName.getBytes();
     
     ByteBuffer buffer = initialize( 8 + nameBytes.length);
     buffer.putInt( (int)key);
     writeBytes( buffer, nameBytes, 0, nameBytes.length, true);
     finalize( buffer, Type.clearAttribute, session);
     
     // log
     SLog.debugf( this, "Send Clear Attribute: session=%X, object=%X, attr=%s", session, key, attrName);
     
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
     String attrName = new String( readBytes( buffer, true));
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
     dispatch( getSessionInfo( link, session), new ClearAttributeEvent( link, session, key, attrName));
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
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       info.listener.setEnabled( false);
       
       IModelObject attached = info.getAttached();
       if ( attached == null) return;
       
       WeakReference<IModelObject> elementRef = info.attachMap.get( key);
       if ( elementRef != null)
       {
         IModelObject element = elementRef.get();
         if ( element != null) element.removeAttribute( attrName);
       }
     }
     finally
     {
       info.listener.setEnabled( true);
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
     ByteBuffer buffer = initialize( 5);
     buffer.putInt( (int)key);
     buffer.put( dirty? (byte)1: 0);
     finalize( buffer, Type.changeDirty, session);
     
     // log
     SLog.debugf( this, "Send Change Dirty: session=%X, object=%X, dirty=%s", session, key, Boolean.toString( dirty));
     
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
     dispatch( getSessionInfo( link, session), new ChangeDirtyEvent( link, session, key, dirty));
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
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       info.listener.setEnabled( false);
       
       IModelObject attached = info.getAttached();
       if ( attached == null) return;
       
       WeakReference<IModelObject> elementRef = info.attachMap.get( key);
       if ( elementRef != null)
       {
         IModelObject element = elementRef.get();
         if ( element != null)
         {
           IExternalReference reference = (IExternalReference)element;
           reference.setDirty( dirty);
         }
       }
     }
     finally
     {
       info.listener.setEnabled( true);
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
     SessionInfo info = getSessionInfo( link, session);
     int correlation = info.correlation.incrementAndGet();
     
     IModelObject request = QueryProtocol.buildRequest( context, query);
     byte[] bytes = info.compress( request);
     
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.queryRequest, session, correlation);
     
     // log
     SLog.debugf( this, "Send Query Request: session=%X, query=%s", session, query);
     
     byte[] content = send( link, session, correlation, buffer, timeout);
     if ( content != null)
     {
       ModelObject response = (ModelObject)info.decompress( content, 0);
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
     
     SessionInfo info = getSessionInfo( link, session);
     ModelObject request = (ModelObject)info.decompress( bytes, 0);
     
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
     dispatch( getSessionInfo( link, session), new QueryRunnable( link, session, correlation, request));
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
     SessionInfo info = getSessionInfo( link, session);
     IModelObject response = QueryProtocol.buildResponse( object);
     byte[] bytes = info.compress( response);
     
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.queryResponse, session, correlation);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, response);
       SLog.debugf( this, "Send Query Response: session=%X, correlation=%d, response=%s", session, correlation, xml);
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
    * Send an asynchronous execute request message.
    * @param link The link.
    * @param session The session number.
    * @param context The local execution context.
    * @param variables The variables to be passed.
    * @param script The script to execute.
    * @param timeout The timeout in milliseconds.
    * @param callback The callback interface.
    */
   public final void sendExecuteRequest( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, ICallback callback, int timeout) throws IOException
   {
     SessionInfo info = getSessionInfo( link, session);
     int correlation = info.correlation.incrementAndGet();
     
     IModelObject request = ExecutionProtocol.buildRequest( context, variables, script);
     byte[] bytes = info.compress( request);
     
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.executeRequest, session, correlation);
 
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, request);
       SLog.debugf( this, "Send Async Execute Request: session=%X, correlation=%d, request=%s", session, correlation, xml);
     }
 
     AsyncCallback runnable = new AsyncCallback( info, context, callback);
     context.getModel();
     
     if ( timeout != Integer.MAX_VALUE)
       scheduleTimeout( timeout, new TimeoutTask( runnable));
     
     info.callbacks.put( correlation, runnable);
     send( link, buffer, session);
   }
 
   /**
    * Schedule an asynchronous callback timeout.
    * @param timeout The timeout.
    * @param task The callback.
    */
   private synchronized void scheduleTimeout( int timeout, TimeoutTask task)
   {
     if ( asyncTimeoutTimer == null) asyncTimeoutTimer = new Timer();
     asyncTimeoutTimer.schedule( task, timeout);
   }
   
   /**
    * Send a synchronous execute request message.
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
     SessionInfo info = getSessionInfo( link, session);
     int correlation = info.correlation.incrementAndGet();
     
     IModelObject request = ExecutionProtocol.buildRequest( context, variables, script);
     byte[] bytes = info.compress( request);
     
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.executeRequest, session, correlation);
 
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, request);
       SLog.debugf( this, "Send Sync Execute Request: session=%X, correlation=%d, request=%s", session, correlation, xml);
     }
     
     if ( timeout > 0)
     {
       byte[] response = send( link, session, correlation, buffer, timeout);
       if ( response != null)
       {
         ModelObject element = (ModelObject)info.decompress( response, 0);
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
     
     SessionInfo info = getSessionInfo( link, session);
     IModelObject request = info.decompress( content, 0);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, request);
       SLog.debugf( this, "Handle Execute Request: session=%X, correlation=%d, request=%s", session, correlation, xml);
     }
 
     // dispatch
     dispatch( info, new ExecuteRunnable( link, session, correlation, request));
   }
   
   /**
    * Send an attach response message.
    * @param link The link.
    * @param session The session number.
    * @param correlation The correlation number.
    * @param context The context.
    * @param results The execution results.
    */
   public final void sendExecuteResponse( ILink link, int session, int correlation, IContext context, Object[] results) throws IOException
   {
     SessionInfo info = getSessionInfo( link, session);
     
     IModelObject response = ExecutionProtocol.buildResponse( context, results);
     byte[] bytes = info.compress( response);
 
     ByteBuffer buffer = initialize( bytes.length);
     buffer.put( bytes);
     finalize( buffer, Type.executeResponse, session, correlation);
     
     // log
     if ( SLog.isLevelEnabled( this, Log.debug))
     {
       String xml = XmlIO.write( Style.compact, response);
       SLog.debugf( this, "Send Execute Response: session=%X, correlation=%d, response=%s", session, correlation, xml);
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
     SessionInfo info = getSessionInfo( link, session);
     if ( info != null)
     {
       byte[] bytes = new byte[ length];
       buffer.get( bytes);
       
       BlockingQueue<byte[]> responseQueue = info.responseQueues.get( correlation);
       if ( responseQueue != null) 
       {
         // synchronous
         responseQueue.offer( bytes);
       }
       else
       {
         // asynchronous
         AsyncCallback runnable = info.callbacks.remove( correlation);
         if ( runnable != null && (runnable.task == null || runnable.task.cancel()))
         {
           runnable.setResponse( bytes);
           runnable.context.getModel().getDispatcher().execute( runnable);
         }
       }
     }
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
     Log log = Log.getLog( this);
     if ( log.isLevelEnabled( Log.debug)) 
     {
       int length = buffer.limit() - buffer.position();
       log.debugf( "send: session=%X, correlation=%d, total-length=%d", session, correlation, length);
     }
     if ( log.isLevelEnabled( Log.verbose)) 
     {
       String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
       log.verbosef( "bytes sent:\n%s", bytes);
     }
     
     SessionInfo info = getSessionInfo( link, session);
     try
     {
       SynchronousQueue<byte[]> responseQueue = new SynchronousQueue<byte[]>();
       info.responseQueues.put( correlation, responseQueue);
       
       // send and wait
       send( link, buffer, session);
       return responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
     }
     catch( InterruptedException e)
     {
       return null;
     }
     finally
     {
       info.responseQueues.remove( correlation);
     }
   }
   
   /**
    * Send the specified buffer to the specified link.
    * @param link The link.
    * @param buffer The buffer.
    * @param session The session number.
    */
   private void send( ILink link, ByteBuffer buffer, int session) throws IOException
   {
     Log log = Log.getLog( this);
     if ( log.isLevelEnabled( Log.debug)) 
     {
       int length = buffer.limit() - buffer.position();
       log.debugf( "send: session=%X, total-length=%d", session, length);
     }
     if ( log.isLevelEnabled( Log.verbose)) 
     {
       String bytes = org.xmodel.net.stream.Util.dump( buffer, "\t");
       log.verbosef( "bytes sent:\n%s", bytes);
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
     SessionInfo info = getSessionInfo( link, session);
     if ( info != null)
     {
       BlockingQueue<byte[]> responseQueue = info.responseQueues.get( correlation);
       if ( responseQueue != null) 
       {
         byte[] bytes = new byte[ length];
         buffer.get( bytes);
         responseQueue.offer( bytes);
       }
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
    * Create a new buffer with the specified payload capacity.
    * @return Returns the new, initialized buffer.
    */
   private static ByteBuffer initialize( int size)
   {
     ByteBuffer buffer = ByteBuffer.allocate( maxHeaderLength + size);
     initialize( buffer);
     return buffer;
   }
   
   /**
    * Reserve the maximum amount of space for the header.
    * @param buffer The buffer.
    */
   private static void initialize( ByteBuffer buffer)
   {
     buffer.clear();
     buffer.position( maxHeaderLength);
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
   private static int readMessageSession( int byte0, ByteBuffer buffer)
   {
     int mask = byte0 & sessionHeaderMask;
     if ( mask == 0) return -1;
     return ((int)buffer.get()) & 0xFF;
   }
   
   /**
    * Read the message length from the buffer.
    * @param byte0 The first byte of the message.
    * @param buffer The buffer.
    * @return Returns the message length.
    */
   private static int readMessageLength( int byte0, ByteBuffer buffer)
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
   private static void finalize( ByteBuffer buffer, Type type, int session)
   {
     finalize( buffer, type, session, Integer.MIN_VALUE);
   }
   
   /**
    * Finalize the message by writing the header and preparing the buffer to be read.
    * @param buffer The buffer.
    * @param type The message type.
    * @param session The session number.
    * @param correlation The correlation number (Integer.MIN_VALUE to opt out).
    */
   private static void finalize( ByteBuffer buffer, Type type, int session, int correlation)
   {
     int length = buffer.position() - maxHeaderLength;
     buffer.limit( buffer.position());
     
     int mask = 0;
     int position = maxHeaderLength;
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
     
     if ( session >= 0)
     {
       mask |= sessionHeaderMask;
       position -= 1;
       buffer.put( position, (byte)session);
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
   private static byte[] readBytes( ByteBuffer buffer, boolean small)
   {
     int length = readLength( buffer, small);
     byte[] bytes = new byte[ length];
     buffer.get( bytes, 0, length);
     return bytes;
   }
   
   /**
    * Write a block of bytes to the message.
    * @param buffer The output buffer.
    * @param bytes The bytes.
    * @param offset The offset.
    * @param length The length.
    */
   private int writeBytes( ByteBuffer buffer, byte[] bytes, int offset, int length, boolean small)
   {
     int prefix = writeLength( buffer, length, small);
     buffer.put( bytes, offset, length);
     return prefix + length;
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
     //
     // Make sure that the root element is synced before engaging the sync lock.
     //
     element.getChildren();
     element.getModel().setSyncLock( true);
 
     SessionInfo info = getSessionInfo( link, session);
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
         info.attachMap.put( key, new WeakReference<IModelObject>( lNode));
         
         if ( lNode instanceof IExternalReference)
         {
           IExternalReference lRef = (IExternalReference)lNode;
           
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
               Object attrValue = lRef.getAttribute( attrName);
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
     
     SessionInfo info = getSessionInfo( link, session);
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
         
         long key = Xlate.get( lNode, "net:key", 0L);
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
           rNode.removeAttribute( "net:key");
         }
         
         info.attachMap.put( key, new WeakReference<IModelObject>( rNode));
         
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
       this.correlation = correlation;
       this.key = key;
     }
     
     public void run()
     {
       doSync( sender, session, correlation, key);
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
     public AttachRunnable( ILink sender, int session, int correlation, boolean pubsub, String xpath)
     {
       this.sender = sender;
       this.session = session;
       this.correlation = correlation;
       this.pubsub = pubsub;
       this.xpath = xpath;
     }
     
     public void run()
     {
       try
       {
         doAttach( sender, session, correlation, pubsub, xpath);
       } 
       catch( IOException e)
       {
         SLog.exception( this, e);
       }
     }
 
     private ILink sender;
     private int session;
     private int correlation;
     private boolean pubsub;
     private String xpath;
   }
   
   private final class DetachRunnable implements Runnable
   {
     public DetachRunnable( ILink sender, SessionInfo info)
     {
       this.sender = sender;
       this.info = info;
     }
     
     public void run()
     {
       doDetach( sender, info);
     }
 
     private ILink sender;
     private SessionInfo info;
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
     public ExecuteRunnable( ILink sender, int session, int correlation, IModelObject request)
     {
       this.sender = sender;
       this.session = session;
       this.correlation = correlation;
       this.request = request;
     }
     
     public void run()
     {
       doExecute( sender, session, correlation, request);
     }
     
     private ILink sender;
     private int session;
     private int correlation;
     private IModelObject request;
   }
   
   private final class CloseSessionRunnable implements Runnable
   {
     public CloseSessionRunnable( ILink sender, SessionInfo info)
     {
       this.sender = sender;
       this.info = info;
     }
     
     public void run()
     {
       doCloseSession( sender, info);
     }
     
     private ILink sender;
     private SessionInfo info;
   }
   
   protected class Listener extends NonSyncingListener
   {
     public Listener( ILink sender, int session, boolean pubsub, String xpath, IModelObject root, Random random)
     {
       this.sender = sender;
       this.session = session;
       this.pubsub = pubsub;
       this.xpath = xpath;
       this.root = root;
       this.random = random;
       this.enabled = true;
     }
     
     public void uninstall()
     {
       uninstall( root);
     }
     
     public void setEnabled( boolean enabled)
     {
       this.enabled = enabled;
     }
     
     /* (non-Javadoc)
      * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
      */
     @Override
     public void notifyAddChild( IModelObject parent, IModelObject child, int index)
     {
       super.notifyAddChild( parent, child, index);
       
       if ( !enabled) return;
       
       try
       {
         IModelObject clone = encode( sender, session, child, false);
         long key = Xlate.get( parent, "net:key", 0L);
         if ( key == 0) key = (random != null)? random.nextLong(): System.identityHashCode( parent);
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
       
       if ( !enabled) return;
       
       //
       // In publish/subscribe mode, the root is considered a topic, and the children are considered to be events.
       // Events are removed by the client and the server independently, and are always added to the end of the 
       // client list.
       //
       if ( pubsub && parent == root) return;
       
       try
       {
         long key = Xlate.get( parent, "net:key", 0L);
         if ( key == 0) key = (random != null)? random.nextLong(): System.identityHashCode( parent);
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
       if ( !enabled) return;
 
       try
       {
         long key = Xlate.get( object, "net:key", 0L);
         if ( key == 0) key = (random != null)? random.nextLong(): System.identityHashCode( object);
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
       if ( !enabled) return;
       
       try
       {
         long key = Xlate.get( object, "net:key", 0L);
         if ( key == 0) key = (random != null)? random.nextLong(): System.identityHashCode( object);
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
       
       if ( !enabled) return;
       
       try
       {
         long key = Xlate.get( object, "net:key", 0L);
         if ( key == 0) key = (random != null)? random.nextLong(): System.identityHashCode( object);
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
     private boolean pubsub;
     private String xpath;
     private IModelObject root;
     private Random random;
     private boolean enabled;
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
     else
     {
       IDispatcher dispatcher = getDispatcher();
       if ( dispatcher != null)
       {
         dispatcher.execute( runnable);
       }
       else
       {
         SLog.error( this, "Dispatcher not defined.");
       }
     }
   }
   
   protected class TimeoutTask extends TimerTask
   {
     public TimeoutTask( AsyncCallback callback)
     {
       this.callback = callback;
       this.callback.task = this;
     }
     
     /* (non-Javadoc)
      * @see java.util.TimerTask#run()
      */
     @Override
     public void run()
     {
       callback.setError( "timeout");
       callback.context.getModel().getDispatcher().execute( callback);
     }
 
     private AsyncCallback callback;
   }
   
   protected class AsyncCallback implements Runnable
   {
     public AsyncCallback( SessionInfo info, IContext context, ICallback callback)
     {
       this.info = info;
       this.context = context;
       this.callback = callback;
     }
     
     /**
      * Set the response.
      * @param response.
      */
     public void setResponse( byte[] response)
     {
       this.response = response;
     }
     
     /**
      * Set the error message.
      * @param error The error message.
      */
     public void setError( String error)
     {
       this.error = error;
     }
     
     /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run()
     {
      IModelObject element = info.decompress( response, 0);
      Object[] result = ExecutionProtocol.readResponse( element, context);
      if ( result.length > 0) context.getScope().set( "result", result[ 0]);
       
       if ( error != null) context.getScope().set( "error", error);
       
       callback.onComplete( context);
       if ( error == null) callback.onSuccess( context); else callback.onError( context);
     }
     
     private SessionInfo info;
     private ICallback callback;
     private IContext context;
     private byte[] response;
     private String error;
     protected TimeoutTask task;
   }
   
   protected static class SessionInfo
   {
     public SessionInfo( int id)
     {
       this.id = id;
       correlation = new AtomicInteger();
       responseQueues = Collections.synchronizedMap( new HashMap<Integer, BlockingQueue<byte[]>>());
       callbacks = Collections.synchronizedMap( new HashMap<Integer, AsyncCallback>());
       compressor = new TabularCompressor( true);
       attachMap = new HashMap<Long, WeakReference<IModelObject>>();
     }
     
     /**
      * @return Returns null or the attached element.
      */
     public IModelObject getAttached()
     {
       if ( element == null) return null;
       return element.get();
     }
     
     /**
      * Compress the specified element.
      * @param element The element.
      * @return Returns the compressed bytes.
      */
     public synchronized byte[] compress( IModelObject element)
     {
       return compressor.compress( element);
     }
     
     /**
      * Decompress the specified bytes.
      * @param bytes The compressed bytes.
      * @param offset The offset into the bytes.
      * @return Returns the decompressed element.
      */
     public synchronized IModelObject decompress( byte[] bytes, int offset)
     {
       return compressor.decompress( bytes, offset);
     }
     
     public int id;
     public AtomicInteger correlation;
     public Map<Integer, BlockingQueue<byte[]>> responseQueues;
     public Map<Integer, AsyncCallback> callbacks;
     public IDispatcher dispatcher;
     public String xpath;
     public boolean isAttachClient;
     public boolean isAttachReadonly;
     public WeakReference<IModelObject> element;
     public Map<Long, WeakReference<IModelObject>> attachMap;
     public Listener listener;
     public TabularCompressor compressor;
   }
   
   private final static int lengthHeaderMask = 0x20;
   private final static int sessionHeaderMask = 0x40;
   private final static int correlationHeaderMask = 0x80;
   private final static int maxHeaderLength = 10;
 
   private static Timer asyncTimeoutTimer;
 
   private Map<ILink, List<SessionInfo>> sessionMap;
   private IContext context;
   private Random random;
   private IDispatcher dispatcher;
   private ISerializer serializer;
   private List<String> packageNames;
   private ExecutePrivilege privilege;
 }
