 package org.j2t.buganalyzer;
 
 import java.io.IOException;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 
 public class App
 {
     public static final String DB_PATH = "target/neo4j-db";
     public static GraphDatabaseService gds = new EmbeddedGraphDatabase( DB_PATH );
     private static void registerShutdownHook( final GraphDatabaseService graphDb )
     {
         Runtime.getRuntime( ).addShutdownHook( new Thread( )
         {
             @Override
             public void run( )
             {
                 graphDb.shutdown( );
             }
         } );
     }
 
     public static void main( String[] args ) throws IOException, InterruptedException
     {
         String projectPath;
         registerShutdownHook( gds );
         //Scanner sc = new Scanner( System.in );
         //System.out.println( "Enter path to sample project" );
         projectPath = "c:/HelloWorld.jar";
         //sc.close( );
         System.out.println( "Project Path:\t" + projectPath );
         SampleProjectRunner spr = new SampleProjectRunner( projectPath );
         spr.runProject( );
         if( spr.getError( ) != null )
         {
             Bug[] b = BugAnalyzerHelper.createBugsFromError( spr.getError( ) );
            int  count = 1;
             for( Bug a : b  )
             {
                System.out.println( "-----Bug " + count + "------" );
                 System.out.println( "Bug Title:\n" + a.getTitle( ) );
                 System.out.println( "Bug Details:\n" + a.getDetails( ) );
                 System.out.println( "Bug Location:\n" + a.getLocation( ) );
                count++;
             }
         }
         gds.shutdown( );
     }
 }
