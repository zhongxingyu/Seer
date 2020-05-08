 package com.hifiremote.jp1.importer;
 
 import java.lang.reflect.Constructor;
 import java.util.StringTokenizer;
 
 // TODO: Auto-generated Javadoc
 /**
  * A factory for creating Importer objects.
  */
 public class ImporterFactory
 {
 
   /**
    * Creates a new Importer object.
    * 
    * @param text
    *          the text
    * @return the importer[]
    */
   public static Importer[] createImporters( String text )
   {
     StringTokenizer st = new StringTokenizer( text );
     int count = st.countTokens();
     Importer[] importers = new Importer[ count ];
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
          name = "com.hifiremote.jp1.importer" + name;
         }
 
         Class< ? > cl = Class.forName( name );
         Class< ? extends Importer > cl2 = cl.asSubclass( Importer.class );
         Class< ? >[] classes =
         {
           String[].class
         };
         Constructor< ? extends Importer > ct = cl2.getConstructor( classes );
         Object[] ctParms =
         {
           parms
         };
         importers[ i ] = ct.newInstance( ctParms );
       }
       catch ( Exception e )
       {
         System.err.println( "ImporterFactory couldn't create an instance of " + name );
         e.printStackTrace( System.err );
       }
     }
     return importers;
   }
 }
