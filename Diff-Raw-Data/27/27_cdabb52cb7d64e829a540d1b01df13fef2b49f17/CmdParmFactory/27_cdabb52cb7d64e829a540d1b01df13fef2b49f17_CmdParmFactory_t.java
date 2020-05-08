 package com.hifiremote.jp1;
 
 import java.awt.Dimension;
 import java.util.StringTokenizer;
 
 public class CmdParmFactory
 {
  public static CmdParameter createParameter( String string, DeviceParameter[] devParms )
   {
     CmdParameter rc = null;
 
     StringTokenizer st = new StringTokenizer( string, ":=", true );
     DefaultValue defaultValue = null;
     int bits = -1;
     String name = st.nextToken();
     String[] choices = null;
     Dimension d = null;
     while ( st.hasMoreTokens())
     {
       String sep = st.nextToken();
       if ( sep.equals( "=" ))
       {
          String token = st.nextToken();
          if ( token.indexOf( '[' ) != -1 )
          {
            StringTokenizer st3 = new StringTokenizer( token, "[]" );
            defaultValue = new IndirectDefaultValue( devParms[ Integer.parseInt( st3.nextToken()) ] );
          }
	  else
	  {
	    defaultValue = new DirectDefaultValue( new Integer( token ) );
	  }
       }
       else if ( sep.equals( ":" ))
       {
         String str = st.nextToken();
         if ( str.indexOf( '|' ) != -1 )
         {
           StringTokenizer st2 = new StringTokenizer( str, "|" );
           int numChoices = st2.countTokens();
           choices = new String[ numChoices ];
           for ( int j = 0; j < numChoices; j++ )
           {
             choices[ j ] = st2.nextToken();
           }
         }
         else if ( str.indexOf( '-' ) != -1 )
         {
           StringTokenizer st3 = new StringTokenizer( str, "-" );
           d = new Dimension( Integer.parseInt( st3.nextToken()),
                              Integer.parseInt( st3.nextToken()));
         }
         else
         {
           bits = Integer.parseInt( str );
         }
       }
     }
     if ( choices != null )
       rc = new ChoiceCmdParm( name, defaultValue, choices );
     else if ( bits != -1 )
       rc = new NumberCmdParm( name, defaultValue, bits );
     else if ( d != null )
       rc = new NumberCmdParm( name, defaultValue, d.width, d.height );
     else
       rc = new NumberCmdParm( name, defaultValue );
 
     return rc;
   }
 }
