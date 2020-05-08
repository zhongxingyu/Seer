 package org.xmodel.net.stream;
 
 import java.nio.ByteBuffer;
 
 public class Util
 {
   /**
    * Dump the content of the specified buffer.
    * @param buffer The buffer.
    * @param indent The indentation before each line.
    * @return Returns a string containing the dump.
    */
   public final static String dump( ByteBuffer buffer, String indent)
   {
     StringBuilder sb = new StringBuilder();
     sb.append( String.format( "%s[%d, %d] ", indent, buffer.position(), buffer.limit()));
     
     int n=0;
     for( int i=buffer.position(); i<buffer.limit(); i++, n++)
     {
       if ( n == 8) sb.append( " ");
       if ( n == 16) sb.append( "  ");
       if ( n == 32) 
       { 
         sb.append( String.format( "\n%s[%d, %d] ", indent, i, buffer.limit()));
         n=0;
       }
       
       sb.append( String.format( "%02X", buffer.get( i)));
       
      if ( (i - buffer.position()) > 128)
       {
         sb.append( "...");
         break;
       }
     }
     
     return sb.toString();
   }
 }
