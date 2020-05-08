 package org.realityforge.tarrabah;
 
 import com.google.gson.JsonObject;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import javax.inject.Inject;
 import org.testng.annotations.Test;
 import static org.realityforge.tarrabah.JsonTestUtil.*;
 import static org.testng.Assert.*;
 
 public class SyslogHandlerTest
   extends AbstractContainerTest
 {
   @Inject
   private SyslogHandler _handler;
 
   @Test
   public void generateJsonMessage_withMinimalMessage()
     throws Exception
   {
     final int remotePort = 2002;
     final int localPort = 2003;
 
     final InetAddress localHost = InetAddress.getLocalHost();
     final InetSocketAddress remoteAddress = new InetSocketAddress( localHost, remotePort );
     final InetSocketAddress localAddress = new InetSocketAddress( localHost, localPort );
     final String rawMessage = "X";
     final JsonObject object = _handler.generateJsonMessage( remoteAddress, localAddress, rawMessage );
 
     assertNotNull( object );
 
     assertSourceMatches( localAddress, object );
     assertFalse( object.has( "hostname" ) );
     assertFalse( object.has( "appName" ) );
     assertTrue( object.has( "message" ) );
     assertEquals( getAsString( object, "message" ), "X" );
     assertFalse( object.has( "msgId" ) );
     assertFalse( object.has( "procId" ) );
     assertFalse( object.has( "facility" ) );
     assertFalse( object.has( "severity" ) );
     assertFalse( object.has( "timestamp" ) );
     assertFalse( object.has( "timestamp_epoch" ) );
   }
 
   private void assertSourceMatches( final InetSocketAddress localAddress, final JsonObject object )
   {
     assertEquals( getAsString( object, "@source" ),
                   "syslog:" + localAddress.getAddress().getHostAddress() + ":" + localAddress.getPort() );
   }
 
   @Test
   public void generateJsonMessage_withRFC3164Message()
     throws Exception
   {
     final int remotePort = 2002;
     final int localPort = 2003;
 
     final InetAddress localHost = InetAddress.getLocalHost();
     final InetSocketAddress remoteAddress = new InetSocketAddress( localHost, remotePort );
     final InetSocketAddress localAddress = new InetSocketAddress( localHost, localPort );
     final String rawMessage = "<34>Oct 11 22:14:15 mymachine su[21]: 'su root' failed for lonvick on /dev/pts/8";
     final JsonObject object = _handler.generateJsonMessage( remoteAddress, localAddress, rawMessage );
 
     assertNotNull( object );
 
     assertSourceMatches( localAddress, object );
     assertTrue( object.has( "hostname" ) );
     assertEquals( getAsString( object, "hostname" ), "mymachine" );
     assertTrue( object.has( "appName" ) );
     assertEquals( getAsString( object, "appName" ), "su" );
     assertTrue( object.has( "message" ) );
     assertEquals( getAsString( object, "message" ), "'su root' failed for lonvick on /dev/pts/8" );
     assertFalse( object.has( "msgId" ) );
     assertTrue( object.has( "procId" ) );
     assertEquals( getAsString( object, "procId" ), "21" );
     assertTrue( object.has( "facility" ) );
     assertEquals( getAsString( object, "facility" ), "auth" );
     assertTrue( object.has( "severity" ) );
     assertEquals( getAsString( object, "severity" ), "crit" );
     assertTrue( object.has( "timestamp" ) );
    //Strip off year and test against remaining timestamp component
    assertTrue( getAsString( object, "timestamp" ).substring( 4 ).startsWith( "-10-11T22:14:15" ) );
     assertTrue( object.has( "timestamp_epoch" ) );
   }
 
   @Test
   public void generateJsonMessage_withStructuredSyslogMessage()
     throws Exception
   {
     final int remotePort = 2002;
     final int localPort = 2003;
 
     final InetAddress localHost = InetAddress.getLocalHost();
     final InetSocketAddress remoteAddress = new InetSocketAddress( localHost, remotePort );
     final InetSocketAddress localAddress = new InetSocketAddress( localHost, localPort );
     final String rawMessage = "<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 " +
                               "[exampleSDID@32473 iut=\"3\" eventSource=\"Application\" eventID=\"1011\"][examplePriority@32473 class=\"high\"]" +
                               " BOM'su root' failed for lonvick on /dev/pts/8";
     final JsonObject object = _handler.generateJsonMessage( remoteAddress, localAddress, rawMessage );
 
     assertNotNull( object );
 
     assertSourceMatches( localAddress, object );
     assertTrue( object.has( "hostname" ) );
     assertEquals( getAsString( object, "hostname" ), "mymachine.example.com" );
     assertTrue( object.has( "appName" ) );
     assertEquals( getAsString( object, "appName" ), "su" );
     assertTrue( object.has( "message" ) );
     assertEquals( getAsString( object, "message" ), "BOM'su root' failed for lonvick on /dev/pts/8" );
     assertTrue( object.has( "msgId" ) );
     assertEquals( getAsString( object, "msgId" ), "ID47" );
     assertFalse( object.has( "procId" ) );
     assertTrue( object.has( "facility" ) );
     assertEquals( getAsString( object, "facility" ), "auth" );
     assertTrue( object.has( "severity" ) );
     assertEquals( getAsString( object, "severity" ), "crit" );
     assertTrue( object.has( "timestamp" ) );
     assertEquals( getAsString( object, "timestamp" ), "2003-10-11T22:14:15.003Z" );
     assertTrue( object.has( "timestamp_epoch" ) );
     assertEquals( getAsString( object, "timestamp_epoch" ), "1065910455" );
 
     assertTrue( object.has( "_exampleSDID@32473" ) );
     assertEquals( getAsString( object.getAsJsonObject( "_exampleSDID@32473" ), "eventSource" ), "Application" );
     assertEquals( getAsString( object.getAsJsonObject( "_exampleSDID@32473" ), "eventID" ), "1011" );
 
     assertTrue( object.has( "_examplePriority@32473" ) );
     assertEquals( getAsString( object.getAsJsonObject( "_examplePriority@32473" ), "class" ), "high" );
   }
 }
