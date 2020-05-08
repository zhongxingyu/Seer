 package com.hifiremote.jp1;
 
 import java.awt.Dimension;
 import java.util.*;
 
 public class CmdParmFactory
 {
   public static CmdParameter createParameter( String string, DeviceParameter[] devParms, CmdParameter[] cmdParms )
   {
     CmdParameter rc = null;
 
     StringTokenizer st = new StringTokenizer( string, ":=", true );
     DefaultValue defaultValue = null;
     int bits = -1;
     int base = 10;
     String name = st.nextToken();
     Vector choices = null;
 //    Dimension d = null;
     while ( st.hasMoreTokens())
     {
       String sep = st.nextToken();
       if ( sep.equals( "=" ))
       {
         String token = st.nextToken();
         if ( token.indexOf( '{' ) != -1 )
         {
           StringTokenizer st3 = new StringTokenizer( token, "{}" );
           String indexStr = st3.nextToken();
           int dash = indexStr.indexOf( '-' );
           if ( dash != -1 )
             indexStr = indexStr.substring( 1 );
           int index = Integer.parseInt( indexStr );
 
           IndirectDefaultValue def = new IndirectDefaultValue( index, devParms[ index ] );
           def.setIsComplement( dash != -1 );
           defaultValue = def;
         }
         else if ( token.indexOf( '[' ) != -1 )
         {
           StringTokenizer st3 = new StringTokenizer( token, "[]" );
           String indexStr = st3.nextToken();
           int dash = indexStr.indexOf( '-' );
           if ( dash != -1 )
             indexStr = indexStr.substring( 1 );
           int index = Integer.parseInt( indexStr );
 
           IndirectDefaultValue def = new IndirectDefaultValue( index, cmdParms[ index ] );
           def.setIsComplement( dash != -1 );
           defaultValue = def;
         }
         else
         {
           defaultValue = new DirectDefaultValue( new Integer( token ) );
         }
       }
       else if ( sep.equals( ":" ))
       {
         String str = st.nextToken();
         if ( str.charAt( 0 ) == '$' )
         {
           base = 16;
           str = str.substring( 1 );
         }
         if ( str.indexOf( '|' ) != -1 )
         {
           StringTokenizer st2 = new StringTokenizer( str, "|", true );
           choices = new Vector();
           while ( st2.hasMoreTokens())
           {
             String val = st2.nextToken();
             if ( val.equals( "|" ))
               val = null;
             else if ( st2.hasMoreTokens())
               st2.nextToken();
             choices.add( val );
           }
         }
 //        else if ( str.indexOf( '-' ) != -1 )
 //        {
 //          StringTokenizer st3 = new StringTokenizer( str, "-" );
 //          d = new Dimension( Integer.parseInt( st3.nextToken()),
 //                             Integer.parseInt( st3.nextToken()));
 //        }
         else if ( str.length() > 0 )
         {
           bits = Integer.parseInt( str );
         }
       }
     }
     if ( choices != null )
       rc = new ChoiceCmdParm( name, defaultValue, choices );
     else if ( bits != -1 )
      rc = new NumberCmdParm( name, defaultValue, bits, base );
 //    else if ( d != null )
 //      rc = new NumberCmdParm( name, defaultValue, d.width, d.height );
     else
     {
       rc = new NumberCmdParm( name, defaultValue, 8, base );
     }
 
     return rc;
   }
 }
