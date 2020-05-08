 package p4idea.perforce;
 
 import com.intellij.openapi.vfs.VirtualFile;
 import com.perforce.p4java.client.IClient;
 import com.perforce.p4java.core.CoreFactory;
 import com.perforce.p4java.core.IChangelist;
 import com.perforce.p4java.core.file.IFileSpec;
 import com.perforce.p4java.exception.*;
 import com.perforce.p4java.impl.generic.core.file.FileSpec;
 import com.perforce.p4java.server.*;
 import p4idea.P4Logger;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class P4Wrapper
 {
   private static final P4Wrapper P4 = new P4Wrapper();
   protected final List<String> _messages = new ArrayList<>();
   private final boolean _autoDisconnect;
   private IServer _server;
   private String _clientSpec;
   private String _p4port;
   private File _p4root;
   private String _username;
 
   P4Wrapper()
   {
     _autoDisconnect = false;
   }
 
   public static P4Wrapper getP4()
   {
     return P4;
   }
 
   public P4Wrapper initialize( P4Settings settings ) throws ConnectionException, AccessException
   {
     disconnect();
 
     _clientSpec = settings.getP4client();
     _p4port = settings.getP4port();
     _p4root = null;
     _username = settings.getP4user();
 
     return this;
   }
 
   public File getP4Root() throws ConnectionException, AccessException
   {
     if ( null == _p4root )
     {
       IClient client = getP4Server().getCurrentClient();
       if ( null != client )
       {
         _p4root = new File( client.getRoot() );
       }
     }
     return _p4root;
   }
 
   public void login( String password ) throws ConnectionException, RequestException, URISyntaxException,
       ResourceException, AccessException, ConfigException, NoSuchObjectException
   {
     try
     {
       getP4Server( password );
     }
     finally
     {
       attemptDisconnect();
     }
   }
 
   /**
    * Returns a Perforce server connection for the Perforce server. The connection will not be authenticated unless an
    * (optional null) password is specified. The caller is responsible for calling {@link IServer#disconnect()} on the
    * returned connection when it is no longer needed.
    */
   protected IServer getP4Server() throws AccessException, ConnectionException
   {
     try
     {
       return getP4Server( null );
     }
     catch ( NoSuchObjectException | URISyntaxException | ResourceException | ConfigException | RequestException e )
     {
       throw new ConnectionException( e );
     }
   }
 
   private IServer getP4Server( String password ) throws ConnectionException, ConfigException, NoSuchObjectException,
       ResourceException, URISyntaxException, RequestException, AccessException
   {
     if ( null != _server )
     {
       return _server;
     }
     _server = ServerFactory.getServer( String.format( "p4java://%s", _p4port ), null );
     _server.setUserName( _username );
 
     _server.connect();
 
     if ( null != password )
     {
       // Password never gets stored, just used here for authentication
       _server.login( password );
     }
 
     if ( null != _clientSpec )
     {
       IClient client = _server.getClient( _clientSpec );
       _server.setCurrentClient( client );
     }
 
     return _server;
   }
 
   public void disconnect() throws ConnectionException, AccessException
   {
     disconnect( true );
   }
 
   protected void attemptDisconnect() throws ConnectionException, AccessException
   {
     disconnect( false );
   }
 
   private void disconnect( boolean force ) throws ConnectionException, AccessException
   {
     flushMessages();
 
     if ( !force && !_autoDisconnect )
     {
       return;
     }
 
     if ( null != _server )
     {
       if ( _server.isConnected() )
       {
         _server.disconnect();
       }
       _server = null;
       flushMessages();
     }
   }
 
   private void flushMessages()
   {
     for ( String message : _messages )
     {
       P4Logger.getInstance().log( "P4: " + message );
     }
     _messages.clear();
   }
 
   public IServerInfo showServerInfo() throws ConnectionException, RequestException, AccessException
   {
     try
     {
       IServerInfo info = getP4Server().getServerInfo();
       if ( info != null )
       {
         P4Logger logger = P4Logger.getInstance();
         logger.log( String.format( "Server: %s", info.getServerAddress() ) );
         logger.log( String.format( "User: %s", info.getUserName() ) );
         logger.log( String.format( "Client: %s", info.getClientName() ) );
         logger.log( String.format( "Root: %s", info.getClientRoot() ) );
         logger.log( String.format( "Version: %s", info.getServerVersion() ) );
         logger.log( String.format( "Uptime: %s", info.getServerUptime() ) );
         logger.log( String.format( "Server Root: %s", info.getServerRoot() ) );
       }
       else
       {
         P4Logger.getInstance().log( "Unable to determine server info" );
       }
       return info;
     }
     finally
     {
       attemptDisconnect();
     }
   }
 
   public IChangelist createChangelist( String description ) throws P4JavaException
   {
     try
     {
       IClient client = getP4Server().getCurrentClient();
       return CoreFactory.createChangelist( client, description, true );
     }
     finally
     {
       attemptDisconnect();
     }
   }
 
   private IChangelist getDefaultChangelist() throws ConnectionException, AccessException, RequestException
   {
     return getP4Server().getChangelist( IChangelist.DEFAULT );
   }
 
   public List<IFileSpec> openForEdit( VirtualFile[] files ) throws P4JavaException
   {
    return openForEdit( getDefaultChangelist(), files );
   }
 
  public List<IFileSpec> openForEdit( IChangelist changelist, VirtualFile[] files ) throws ConnectionException,
       RequestException, AccessException
   {
    P4Logger.getInstance().log( "Using changelist " + changelist.getId() + ": " + changelist.getDescription() );
     List<IFileSpec> fileSpecs = fromVirtualFiles( files );
     try
     {
       IClient client = getP4Server().getCurrentClient();
       return client.editFiles( fileSpecs, false, false, changelist.getId(), null );
     }
     finally
     {
       attemptDisconnect();
     }
   }
 
   private List<IFileSpec> fromVirtualFiles( VirtualFile[] virtualFiles )
   {
     List<IFileSpec> fileSpecs = new ArrayList<>();
     for ( VirtualFile virtualFile : virtualFiles )
     {
       P4Logger.getInstance().log( "File: " + virtualFile.getCanonicalPath() );
       fileSpecs.add( new FileSpec( virtualFile.getCanonicalPath() ) );
     }
     return fileSpecs;
   }
}
