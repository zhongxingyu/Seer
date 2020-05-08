 package net.jumperz.app.MFind2;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 import net.jumperz.util.MSystemUtil;
 
 public class MLauncher
 extends MAbstractLogAgent
 {
 private Socket socket;
 //--------------------------------------------------------------------------------
 public static void main( String[] args )
 throws Exception
 {
 ( new MLauncher() ).start( args );
 }
 //--------------------------------------------------------------------------------
 public void start( String[] args )
 throws Exception
 {
 for( int i = 0; i < 3; ++i )
 	{
 	try
 		{
 		MSystemUtil.sleep( 500 + ( 500 * i ) );
 		socket = new Socket( MFind2.ADDR, MFind2.PORT );
 		break;	
 		}
 	catch( Exception e )
 		{
 		}
 	}
 
 if( socket == null )
 	{
 	System.err.println( "Connection failed." );
 	return;
 	}
 
 BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
 System.out.println( reader.readLine() );
 socket.close();
 }
 //--------------------------------------------------------------------------------
 }
