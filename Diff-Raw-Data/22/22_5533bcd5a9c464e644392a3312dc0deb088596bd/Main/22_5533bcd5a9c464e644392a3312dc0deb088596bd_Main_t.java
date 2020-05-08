 package net.praqma.ccanalyzer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.praqma.monkit.MonKit;
 import net.praqma.util.option.Option;
 import net.praqma.util.option.Options;
 
 public class Main {
 
     /**
      * @param args
      * @throws IOException 
      */
     public static void main(String[] args) throws IOException {
 	
         Options o = new Options( Server.textualVersion );
 
         Option oport = new Option( "port", "p", false, 1, "The port, default is 44444" );
         Option ohost = new Option( "host", "H", false, -1, "The host name/IP" );
         Option oname = new Option( "name", "n", false, -1, "The name/title" );
         Option osite = new Option( "site", "s", false, 1, "Use a named configuration defined site" );
         Option occ   = new Option( "clearcase", "C", false, 1, "The ClearCase host" );
         Option ofile = new Option( "file", "f", false, 1, "The name of the MonKit file output" );
         Option oconf = new Option( "config", "c", false, 1, "The config file, default is config.xml" );
 
         o.setOption( ohost );
         o.setOption( oname );
         o.setOption( ofile );
         o.setOption( oconf );
         o.setOption( occ );
         o.setOption( osite );
 
         o.setDefaultOptions();
 
         o.setSyntax( "Main [-p <port number>] [-H <list of hosts>] [-n <list of names>] [-C <host of the ClearCase server] [-c <path to config>]" );
         o.setHeader( "Query a CCAnalyser server" );
         String desc = "Given a set of hosts and a set of names(there's a one to one correspondence between hosts and names),\n" +
                       "the hosts a queried for a configured set of performance and/or ClearCase counters.\n\nFx Main -H 127.0.0.1 10.10.1.83 -n localhost CC_CLIENT2 -c \"c:\\config.xml\"\n\n" +
                       "The hosts and names are given as white space separated lists.";
         o.setDescription( desc );
 
         o.parse( args );
 
         try {
             o.checkOptions();
         } catch( Exception e ) {
             System.err.println( "Incorrect option: " + e.getMessage() );
             o.display();
             System.exit( 1 );
         }
         
         Integer port = Server.defaultPort;
         if( oport.isUsed() ) {
             port = oport.getInteger();
         }
 
         List<String> hosts = null;
         List<String> names = null;
         if( ohost.isUsed() && oname.isUsed() ) {
         	if( o.isVerbose() ) System.out.println("Using CLI defined hosts");
 	        hosts = ohost.getStrings();
 	        names = oname.getStrings();
 	
 	        if( hosts.size() != names.size() ) {
 	            System.err.println( "The number of hosts must the same as the number of names." );
 	            System.exit( 1 );
 	        }
 	    } else {
         	if( o.isVerbose() ) System.out.println("Using config defined hosts");
         	hosts = new ArrayList<String>();
         	names = new ArrayList<String>();
         }
 
         MonKit mk = new MonKit();
         
         ConfigurationReader cr = null;
         
         if( oconf.isUsed() ) {
         	if( o.isVerbose() ) System.out.println("Using configuration file " + oconf.getString());
             cr = new ConfigurationReader( new File( oconf.getString() ) );
         } else {
             cr = new ConfigurationReader( new File( "config.xml" ) );
         }
         
         if( o.isVerbose() ) System.out.println("Site: " + osite.getString());
        try {
        	cr.initialize( hosts, names, osite.getString(), "" );
        } catch( CCAnalyzerException e ) {
            System.err.println( "Could not initialize configuration: " + e.getMessage() );
            System.exit( 1 );
        }
 
         /* If any hosts defined to analyze */
         if( hosts.size() > 0 ) {
             for( int i = 0; i < hosts.size(); ++i ) {
                 try {
                     PerformanceClient c = new PerformanceClient( port, hosts.get( i ), names.get( i ), mk );
         
                     c.start( cr );
                 } catch( CCAnalyzerException e ) {
                     System.out.println( "Unable to connect to server: " + e.getMessage() );
                 }
             }
         }
         
         /* Do the ClearCase */
         if( occ.isUsed() ) {
             try {
                 ClearCaseClient c = new ClearCaseClient( port, occ.getString(), "CC", mk );
                 
                 c.start( cr );
             } catch( CCAnalyzerException e ) {
                 System.out.println( "Unable to connect to server: " + e.getMessage() );
             }
         }
         
         /* Save the MonKit file */
         if( ofile.isUsed() ) {
             mk.save( new File( ofile.getString() ) );
         } else {
             mk.save();
         }
     }
 
 }
