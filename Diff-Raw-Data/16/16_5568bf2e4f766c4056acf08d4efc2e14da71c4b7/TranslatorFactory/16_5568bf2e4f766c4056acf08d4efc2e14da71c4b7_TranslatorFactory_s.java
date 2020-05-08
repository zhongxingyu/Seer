 package com.hifiremote.jp1.translate;
 
 import java.lang.reflect.Constructor;
 import java.util.StringTokenizer;
 
 // TODO: Auto-generated Javadoc
 /**
  * A factory for creating Translator objects.
  */
 public class TranslatorFactory
 {
 
   /**
    * Creates a new Translator object.
    * 
    * @param text
    *          the text
    * @return the translate[]
    */
   public static Translate[] createTranslators( String text )
   {
     StringTokenizer st = new StringTokenizer( text );
     int count = st.countTokens();
     Translate[] translators = new Translate[ count ];
     for ( int i = 0; i < count; i++ )
     {
       String temp = st.nextToken();
       StringTokenizer st2 = new StringTokenizer( temp, "()" );
       String name = st2.nextToken();
       String[] parms = null;
       if ( st2.hasMoreTokens() )
       {
         temp = st2.nextToken();
         StringTokenizer st3 = new StringTokenizer( temp, "," );
         int parmCount = st3.countTokens();
         parms = new String[ parmCount ];
         for ( int j = 0; j < parmCount; j++ )
         {
           parms[ j ] = st3.nextToken();
         }
       }
       else
       {
         parms = new String[ 0 ];
       }
 
       try
       {
         if ( name.indexOf( '.' ) == -1 )
         {
          name = "com.hifiremote.jp1." + name;
         }
 
         Class< ? > cl = Class.forName( name );
         Class< ? extends Translate > cl2 = cl.asSubclass( Translate.class );
         Class< ? >[] classes =
         {
           String[].class
         };
         Constructor< ? extends Translate > ct = cl2.getConstructor( classes );
         Object[] ctParms =
         {
           parms
         };
         translators[ i ] = ct.newInstance( ctParms );
       }
       catch ( Exception e )
       {
         System.err.println( "TranslatorFactory couldn't create an instance of " + name );
         e.printStackTrace( System.err );
       }
     }
     return translators;
   }
 }
