 package org.realityforge.tarrabah;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.joda.time.DateTime;
 import org.realityforge.jsyslog.message.Facility;
 import org.realityforge.jsyslog.message.Severity;
 import org.realityforge.jsyslog.message.StructuredDataParameter;
 import org.realityforge.jsyslog.message.SyslogMessage;
 
 public final class SyslogHandler
   extends BaseInputHandler
 {
   @Inject
   private Logger _logger;
 
   @Override
   public void messageReceived( final ChannelHandlerContext context,
                                final MessageEvent e )
     throws Exception
   {
     final InetSocketAddress remoteAddress = (InetSocketAddress) e.getRemoteAddress();
 
     final ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
 
     final byte[] readable = new byte[ buffer.readableBytes() ];
     buffer.toByteBuffer().get( readable, buffer.readerIndex(), buffer.readableBytes() );
 
     final SocketAddress localAddress = context.getChannel().getLocalAddress();
     final String rawMessage = new String( readable );
     processSyslogMessage( remoteAddress, localAddress, rawMessage );
   }
 
   @Override
   public void exceptionCaught( final ChannelHandlerContext context, final ExceptionEvent e )
     throws Exception
   {
     _logger.log( Level.WARNING, "Problem handling syslog packet.", e.getCause() );
   }
 
   void processSyslogMessage( final InetSocketAddress remoteAddress,
                              final SocketAddress localAddress,
                              final String rawMessage )
   {
     final SyslogMessage message = parseSyslogMessage( rawMessage );
     final String source = "syslog:" + localAddress;
 
     final JsonObject object = createBaseMessage( remoteAddress, source );
     mergeSyslogFields( message, object );
     final Gson gson = new GsonBuilder().create();
     System.out.println( "Message: " + gson.toJson( object ) );
   }
 
   private void mergeSyslogFields( final SyslogMessage syslogMessage, final JsonObject object )
   {
     final String hostname = syslogMessage.getHostname();
     if ( null != hostname )
     {
      object.addProperty( "hostname", hostname );
     }
     final String appName = syslogMessage.getAppName();
     if ( null != appName )
     {
       object.addProperty( "appName", appName );
     }
     final String message = syslogMessage.getMessage();
     if ( null != message )
     {
       object.addProperty( "message", message );
     }
     final String msgId = syslogMessage.getMsgId();
     if ( null != msgId )
     {
       object.addProperty( "msgId", msgId );
     }
     final String procId = syslogMessage.getProcId();
     if ( null != procId )
     {
       object.addProperty( "procId", procId );
     }
     final Facility facility = syslogMessage.getFacility();
     if ( null != facility )
     {
       object.addProperty( "facility", facility.name().toLowerCase() );
     }
     final Severity severity = syslogMessage.getLevel();
     if ( null != severity )
     {
       object.addProperty( "severity", severity.name().toLowerCase() );
     }
     final DateTime timestamp = syslogMessage.getTimestamp();
     if ( null != timestamp )
     {
       object.addProperty( "timestamp", timestamp.toString() );
       object.addProperty( "timestamp_epoch", timestamp.toDate().getTime() / 1000 );
     }
     final Map<String, List<StructuredDataParameter>> structuredData = syslogMessage.getStructuredData();
     if ( null != structuredData )
     {
       for ( final Entry<String, List<StructuredDataParameter>> entry : structuredData.entrySet() )
       {
         final JsonObject value = new JsonObject();
         for ( final StructuredDataParameter parameter : entry.getValue() )
         {
           value.addProperty( parameter.getName(), parameter.getValue() );
         }
         object.add( "_" + entry.getKey(), value );
       }
     }
   }
 
   private SyslogMessage parseSyslogMessage( final String rawMessage )
   {
     try
     {
       return SyslogMessage.parseStructuredSyslogMessage( rawMessage );
     }
     catch ( final Exception e )
     {
       return SyslogMessage.parseRFC3164SyslogMessage( rawMessage );
     }
   }
 }
