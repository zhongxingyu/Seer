 package org.j2t.buganalyzer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class BugAnalyzerHelper
 {
     public static String[] removeEmptyLines( String[] errorLines )
     {
         ArrayList<String> err = new ArrayList<String>( Arrays.asList( errorLines ) );
         for( String s : err )
         {
             if( s.equals( "" ) )
             {
                 err.remove( s );
             }
         }
         return err.toArray( new String[err.size( )] );
     }
     
     public static Bug[] createBugsFromError( String error )
     {
         String[] errorLines = removeEmptyLines( error.split( "\r?\n" ) );
        // variable denoting the no. of bugs found by counting lines not  beginning with tab.
         int count = 0;
         for( String s : errorLines )
         {
             if( ! ( s.startsWith( "\t" ) ) )
             {
                 count++;
             }
         }
         Bug[] errors = new Bug[count];
         for( int k = 0; k < errors.length; ++k )
         {
             errors[k] = new Bug( "", "", "" );
         }
         int i = 0;
         int j = 0;
         while( i < errorLines.length )
         {
             // If current line does not begin with tab, it is the title of the current bug
             if( ! ( errorLines[i].startsWith( "\t" ) ) )
             {
                 errors[j].setTitle( "\t" + errorLines[i] );
             }
             // If it does begin with tab...
             else
             {
                 // ...and if it's the last line or if the next line does not begins with tab...
                 if( i == errorLines.length - 1 ? true : ! ( errorLines[i+1].startsWith( "\t" ) ) )
                 {
                     // ...Set it as location of current bug and move on to next one...
                     errors[j++].setLocation( errorLines[i] );
                     // ...If bugs created equals the length of errors array, exit the while loop
                     if( j == errors.length )
                     {
                         break;
                     }
                 }
                 // ...Or else (in case it's not last line or the next line begins with tab )...
                 else
                 {
                     // ...Set it as details of current bug.
                     errors[j].setDetails( errors[j].getDetails( ) + errorLines[i] + "\n" );
                 }
             }
             i++;
         }
         return errors;
     }
 }
