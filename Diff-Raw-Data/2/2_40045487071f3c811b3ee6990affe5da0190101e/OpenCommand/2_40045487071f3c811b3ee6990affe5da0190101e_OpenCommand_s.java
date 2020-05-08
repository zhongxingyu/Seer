 package org.cyclopsgroup.jmxterm.cmd;
 
 import java.io.IOException;
 
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cyclopsgroup.jcli.annotation.Argument;
 import org.cyclopsgroup.jcli.annotation.Cli;
 import org.cyclopsgroup.jmxterm.Command;
 import org.cyclopsgroup.jmxterm.Connection;
 import org.cyclopsgroup.jmxterm.Session;
 import org.cyclopsgroup.jmxterm.SyntaxUtils;
 
 @Cli( name = "open", description = "Open JMX session", note = "eg. open localhost:9991, or open jmx:service:..." )
 public class OpenCommand
     extends Command
 {
     private static final Log LOG = LogFactory.getLog( OpenCommand.class );
 
     private String url;
 
     public void execute( Session session )
         throws IOException
     {
         if ( url == null )
         {
             Connection con = session.getConnection();
             if ( con == null )
             {
                 session.output.println( "Not connected" );
             }
             else
             {
 
                 session.output.println( String.format( "Connected to: expr=%s, id=%s, url=%s", con.getOriginalUrl(),
                                                        con.getConnector().getConnectionId(), con.getUrl() ) );
             }
             return;
         }
        Validate.isTrue( session.getConnection() == null );
         JMXServiceURL u = SyntaxUtils.getUrl( url );
         JMXConnector connector = JMXConnectorFactory.connect( u );
         session.setConnection( new Connection( connector, u, url ) );
         LOG.info( "Connection to " + url + " is opened" );
     }
 
     public final String getUrl()
     {
         return url;
     }
 
     @Argument
     public final void setUrl( String url )
     {
         this.url = url;
     }
 }
