 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s,
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package dk.dbc.opensearch.tools.relations;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.trippi.TrippiException;
 
 
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
 
         //System.out.println( String.format( "%s", System.getProperties() ) );
 
         String url = System.getProperty( "url");
         String user = System.getProperty( "user" );
         String pass = System.getProperty( "pass" );
         String query = System.getProperty( "query" );
 
         System.out.println( String.format( "url  = %s", url) );
         System.out.println( String.format( "user = %s", user ) );
         System.out.println( String.format( "pass = %s", pass ) );
         System.out.println( String.format( "query  = %s", query ) );
 
 
         if( ( url == null || user == null || pass == null || query == null ) )
         {
             System.out.println( usage() );
             System.exit( 1 );
         }
 
         ItqlTool fr = new ItqlTool( url, user, pass );
         try
         {
             fr.testGetObjectRelationships( query );
         }
         catch ( TrippiException ex )
         {
             Logger.getLogger( Main.class.getName() ).log( Level.SEVERE, ex.getMessage(), ex );
         }
         catch ( MalformedURLException ex )
         {
             Logger.getLogger( Main.class.getName() ).log( Level.SEVERE, ex.getMessage(), ex );
         }
         catch ( IOException ex )
         {
             Logger.getLogger( Main.class.getName() ).log( Level.SEVERE, ex.getMessage(), ex );
         }
     }
 
     private static String usage()
     {
         String usage = "usage:\n\n";
         usage += " java -Durl=[fedora url] -Duser=[fedora user name] -Dpass=[fedora password]\n\n";
         usage += " [url]     The url to the fedora base, eg. http://sempu.dbc.dk:8080/fedora\n";
         usage += " [user]    The username with which to log in to fedora\n";
         usage += " [pass]    The password with which to authorize the user in fedora\n";
         usage += " [query]   The itql query to pass to fedora\n\n";
         usage += " eg.";
//        usage += " java -jar -Durl=http://sempu:8080/fedora -Duser=fedoraAdmin -Dpass=fedoraAdmin -Dquery=\"select \$s \$p \$o from <#ri> where \$s \$p \$o\" dist/OpenSearch_ITQL.jar";
         return usage;
     }
 }
