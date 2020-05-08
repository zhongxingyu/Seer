 package org.xmodel.net;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.xmodel.DepthFirstIterator;
 import org.xmodel.IDispatcher;
 import org.xmodel.IModelObject;
 import org.xmodel.ManualDispatcher;
 import org.xmodel.ModelAlgorithms;
 import org.xmodel.ModelObject;
 import org.xmodel.Xlate;
 import org.xmodel.external.CachingException;
 import org.xmodel.external.ExternalReference;
 import org.xmodel.external.ICachingPolicy;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.log.Log;
 import org.xmodel.net.stream.Connection;
 import org.xmodel.net.stream.TcpClient;
 import org.xmodel.xml.XmlIO;
 import org.xmodel.xpath.XPath;
 import org.xmodel.xpath.expression.Context;
 import org.xmodel.xpath.expression.IExpression;
 
 /**
  * A class that implements the network caching policy protocol.
  */
 public class Client extends Protocol
 {
   /**
    * Create a CachingClient to connect to the specified server.
    * @param host The server host.
    * @param port The server port.
    */
   public Client( String host, int port) throws IOException
   {
     if ( client == null) 
     {
       client = new TcpClient();
       client.start();
     }
     
     this.host = host;
     this.port = port;
     
     this.keys = new WeakHashMap<IModelObject, String>();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.stream.ITcpListener#onConnect(org.xmodel.net.stream.Connection)
    */
   @Override
   public void onConnect( Connection connection)
   {
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.stream.ITcpListener#onClose(org.xmodel.net.stream.Connection)
    */
   @Override
   public void onClose( Connection connection)
   {
     if ( attached != null)
     {
       dispatcher.execute( new DisconnectEvent( attached));
       attached = null;
     }
   }
 
   /**
    * Connect (or reconnect) to the remote host.
    */
   public void connect( int timeout) throws IOException
   {
     if ( connection != null && connection.isOpen()) 
     {
       client.reconnect( connection, timeout);
     }
     else
     {
       connection = client.connect( host, port, timeout, this);
     }
   }
 
   /**
    * Disconnect form the remote host.
    */
   public void disconnect() throws IOException
   {
     if ( isConnected()) connection.close();
   }
   
   /**
    * @return Returns true if the client is connected.
    */
   public boolean isConnected()
   {
     return connection != null && connection.isOpen();
   }
   
   /**
    * Attach to the element on the specified xpath.
    * @param xpath The XPath expression.
    * @param reference The reference.
    */
   public void attach( String xpath, IExternalReference reference) throws IOException
   {
     connect( 30000);
    if ( connection.isOpen())
     {
       sendVersion( connection, (byte)1);
       dispatcher = reference.getModel().getDispatcher();
       attached = reference;
       sendAttachRequest( connection, xpath);
     }
     else
     {
       throw new CachingException( "Unable to connect to server.");
     }
   }
 
   /**
    * Detach from the element on the specified path.
    * @param xpath The XPath expression.
    * @param reference The reference.
    */
   public void detach( String xpath, IExternalReference reference) throws IOException
   {
     sendDetachRequest( connection, xpath);
   }
 
   /**
    * Request that the server IExternalReference represented by the specified client IExternalReference be sync'ed.
    * @param reference The client IExternalReference.
    */
   public void sync( IExternalReference reference) throws IOException
   {
     String key = keys.get( reference);
     sendSyncRequest( connection, key);
   }
 
   /**
    * Send a debugStepIn message.
    */
   public void sendDebugStepIn() throws IOException
   {
     if ( connection == null) connection = client.connect( host, port, 30000, this);
     sendDebugStepIn( connection);
   }
   
   /**
    * Send a debugStepOver message.
    */
   public void sendDebugStepOver() throws IOException
   {
     if ( connection == null) connection = client.connect( host, port, 30000, this);
     sendDebugStepOver( connection);
   }
   
   /**
    * Send a debugStepOut message.
    */
   public void sendDebugStepOut() throws IOException
   {
     if ( connection == null) connection = client.connect( host, port, 30000, this);
     sendDebugStepOut( connection);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.nu.Protocol#handleError(org.xmodel.net.stream.Connection, java.lang.String)
    */
   @Override
   protected void handleError( Connection sender, String message)
   {
     log.error( message);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.nu.Protocol#handleAttachResponse(org.xmodel.net.stream.Connection, org.xmodel.IModelObject)
    */
   @Override
   protected void handleAttachResponse( Connection sender, IModelObject element)
   {
     if ( attached != null)
     {
       ICachingPolicy cachingPolicy = attached.getCachingPolicy();
       cachingPolicy.update( attached, decode( element));
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.Protocol#handleSyncResponse(org.xmodel.net.stream.Connection)
    */
   @Override
   protected void handleSyncResponse( Connection connection)
   {
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.Protocol#handleQueryResponse(org.xmodel.net.Connection, byte[])
    */
   @Override
   protected void handleQueryResponse( Connection sender, byte[] bytes)
   {
     // TODO: finish this
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.net.Protocol#handleAddChild(org.xmodel.net.Connection, java.lang.String, byte[], int)
    */
   @Override
   protected void handleAddChild( Connection sender, String xpath, byte[] child, int index)
   {
     dispatcher.execute( new AddChildEvent( xpath, child, index));
   }
 
   /**
    * Process an add child event in the appropriate thread.
    * @param connection The conn
    * @param xpath The xpath of the parent.
    * @param bytes The child that was added.
    * @param index The index of insertion.
    */
   private void processAddChild( String xpath, byte[] bytes, int index)
   {
     if ( attached == null) return;
     
     IExpression parentExpr = XPath.createExpression( xpath);
     if ( parentExpr != null)
     {
       IModelObject parent = parentExpr.queryFirst( attached);
       IModelObject child = connection.getCompressor().decompress( bytes, 0);
       if ( parent != null) parent.addChild( decode( child), index);
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.nu.Protocol#handleRemoveChild(org.xmodel.net.stream.Connection, java.lang.String, int)
    */
   @Override
   protected void handleRemoveChild( Connection sender, String xpath, int index)
   {
     dispatcher.execute( new RemoveChildEvent( xpath, index));
   }
 
   /**
    * Process an remove child event in the appropriate thread.
    * @param xpath The xpath of the parent.
    * @param index The index of insertion.
    */
   private void processRemoveChild( String xpath, int index)
   {
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
   
   /* (non-Javadoc)
    * @see org.xmodel.net.nu.Protocol#handleChangeAttribute(org.xmodel.net.stream.Connection, java.lang.String, java.lang.String, java.lang.Object)
    */
   @Override
   protected void handleChangeAttribute( Connection sender, String xpath, String attrName, Object attrValue)
   {
     dispatcher.execute( new ChangeAttributeEvent( xpath, attrName, attrValue));
   }
 
   /**
    * Process an change attribute event in the appropriate thread.
    * @param xpath The xpath of the element.
    * @param attrName The name of the attribute.
    * @param attrValue The new value.
    */
   private void processChangeAttribute( String xpath, String attrName, Object attrValue)
   {
     if ( attached == null) return;
     
     IExpression elementExpr = XPath.createExpression( xpath);
     if ( elementExpr != null)
     {
       IModelObject element = elementExpr.queryFirst( attached);
       if ( element != null) element.setAttribute( attrName, attrValue);
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.nu.Protocol#handleClearAttribute(org.xmodel.net.stream.Connection, java.lang.String, java.lang.String)
    */
   @Override
   protected void handleClearAttribute( Connection sender, String xpath, String attrName)
   {
     dispatcher.execute( new ClearAttributeEvent( xpath, attrName));
   }
 
   /**
    * Process a clear attribute event in the appropriate thread.
    * @param xpath The xpath of the element.
    * @param attrName The name of the attribute.
    */
   private void processClearAttribute( String xpath, String attrName)
   {
     if ( attached == null) return;
     
     IExpression elementExpr = XPath.createExpression( xpath);
     if ( elementExpr != null)
     {
       IModelObject element = elementExpr.queryFirst( attached);
       if ( element != null) element.removeAttribute( attrName);
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.Protocol#handleChangeDirty(org.xmodel.net.stream.Connection, java.lang.String, boolean)
    */
   @Override
   protected void handleChangeDirty( Connection connection, String xpath, boolean dirty)
   {
     dispatcher.execute( new ChangeDirtyEvent( xpath, dirty));
   }
   
   /**
    * Process a change dirty event in the appropriate thread.
    * @param xpath The xpath of the element.
    * @param dirty The new dirty state.
    */
   private void processChangeDirty( String xpath, boolean dirty)
   {
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
 
   /**
    * Interpret the content of the specified server encoded subtree.
    * @param root The root of the encoded subtree.
    * @return Returns the decoded element.
    */
   private IModelObject decode( IModelObject root)
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
         
         NetworkCachingPolicy cachingPolicy = ((NetworkCachingPolicy)attached.getCachingPolicy()).getNested();
         cachingPolicy.setStaticAttributes( getStaticAttributes( lNode));
         reference.setCachingPolicy( cachingPolicy);
         
         boolean dirty = Xlate.get( reference, "net:dirty", false);
         reference.removeAttribute( "net:dirty");
         reference.setDirty( dirty);
         
         keys.put( reference, key);
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
   
   private final class AddChildEvent implements Runnable
   {
     public AddChildEvent( String xpath, byte[] child, int index)
     {
       this.xpath = xpath;
       this.child = child;
       this.index = index;
     }
     
     public void run()
     {
       processAddChild( xpath, child, index);
     }
     
     private String xpath;
     private byte[] child;
     private int index;
   }
   
   private final class RemoveChildEvent implements Runnable
   {
     public RemoveChildEvent( String xpath, int index)
     {
       this.xpath = xpath;
       this.index = index;
     }
     
     public void run()
     {
       processRemoveChild( xpath, index);
     }
     
     private String xpath;
     private int index;
   }
   
   private final class ChangeAttributeEvent implements Runnable
   {
     public ChangeAttributeEvent( String xpath, String attrName, Object attrValue)
     {
       this.xpath = xpath;
       this.attrName = attrName;
       this.attrValue = attrValue;
     }
     
     public void run()
     {
       processChangeAttribute( xpath, attrName, attrValue);
     }
     
     private String xpath;
     private String attrName;
     private Object attrValue;
   }
   
   private final class ClearAttributeEvent implements Runnable
   {
     public ClearAttributeEvent( String xpath, String attrName)
     {
       this.xpath = xpath;
       this.attrName = attrName;
     }
     
     public void run()
     {
       processClearAttribute( xpath, attrName);
     }
     
     private String xpath;
     private String attrName;
   }
   
   private final class ChangeDirtyEvent implements Runnable
   {
     public ChangeDirtyEvent( String xpath, boolean dirty)
     {
       this.xpath = xpath;
       this.dirty = dirty;
     }
     
     public void run()
     {
       processChangeDirty( xpath, dirty);
     }
     
     private String xpath;
     private boolean dirty;
   }
   
   private final class DisconnectEvent implements Runnable
   {
     public DisconnectEvent( IExternalReference attached)
     {
       this.attached = attached;
     }
     
     public void run()
     {
       System.out.println( "mark dirty");
       attached.setDirty( true);
       System.out.println( "resync");
       attached.getChildren();
     }
     
     private IExternalReference attached;
   }
   
   private static TcpClient client;
   
   private String host;
   private int port;
   private Connection connection;
   private IDispatcher dispatcher;
   private IExternalReference attached;
   private Map<IModelObject, String> keys;
   
   private static Log log = Log.getLog(  "org.xmodel.net");
   
   public static void main( String[] args) throws Exception
   {
     String xml = "<config host=\"127.0.0.1\" port=\"27613\" xpath=\".\"/>";
     XmlIO xmlIO = new XmlIO();
     IModelObject config = xmlIO.read( xml);
     
     IExternalReference reference = new ExternalReference( "parent");
     NetworkCachingPolicy cachingPolicy = new NetworkCachingPolicy();
     cachingPolicy.configure( new Context( config), config);
     reference.setCachingPolicy( cachingPolicy);
     reference.setDirty( true);
 
     ManualDispatcher dispatcher = new ManualDispatcher();
     reference.getModel().setDispatcher( dispatcher);
     
     reference.getChildren();
     
     while( true)
     {
       System.out.println( xmlIO.write( reference));
       dispatcher.process();
       Thread.sleep( 1000);
     }
   }
 }
