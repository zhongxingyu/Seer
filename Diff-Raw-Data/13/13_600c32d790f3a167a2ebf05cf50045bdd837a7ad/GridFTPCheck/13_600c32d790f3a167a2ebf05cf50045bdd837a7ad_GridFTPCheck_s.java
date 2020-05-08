 package de.zib.gndmc;
 
 import de.zib.gndms.gritserv.delegation.DelegationAux;
 import de.zib.gndms.gritserv.util.GlobusCredentialProviderImpl;
 import de.zib.gndms.gritserv.util.LogAux;
 import de.zib.gndms.kit.access.CredentialProvider;
 import de.zib.gndms.kit.application.AbstractApplication;
 import de.zib.gndms.kit.network.NonblockingClientFactory;
 import de.zib.gndms.model.gorfx.types.GORFXConstantURIs;
 import org.globus.ftp.FeatureList;
 import org.globus.ftp.FileInfo;
 import org.globus.ftp.GridFTPClient;
 import org.globus.gsi.GlobusCredential;
 import org.kohsuke.args4j.Option;
 
 import java.util.Vector;
 
 /**
  * @author Maik Jorra <jorra@zib.de>
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 29.07.2010, Time: 12:48:59
  */
 public class GridFTPCheck extends AbstractApplication {
 
     @Option( name="-d", required=true, usage="dir to list"  )
     String dir;
 
     @Option( name="-h", required=false, usage="hostname" )
     String host="csr-pc35";
 
     @Option( name="-t", required=false, usage="Create Client in diffrent thread")
     boolean threaded = false;
 
     @Option( name="-u", required=true, usage="numeric uid" )
     String uid;
 
     GridFTPClient c;
 
 
     public static void main( String[] args ) throws Exception {
         ( new GridFTPCheck() ).run( args );
         System.exit( 0 );
     }
 
     public void run( ) throws Exception {
 
         LogAux.stdSetupLogger( this.getClass() );
         GlobusCredential cred = DelegationAux.findCredential(
             DelegationAux.defaultProxyFileName( uid ) );
         CredentialProvider cp =
             new GlobusCredentialProviderImpl( GORFXConstantURIs.FILE_TRANSFER_URI, cred );
 
         if( threaded ) {
             System.out.println( "Threaded client creation" );
             NonblockingClientFactory nbc = new NonblockingClientFactory();
             c = nbc.createClient( host, 2811, cp );
         } else {
             System.out.println( "08/15 client creation" );
             c = new GridFTPClient( host, 2811 );
             cp.installCredentials( c );
         }
         System.out.println( "Cliend created" );
 
         // GridFTPClientCreator cc =
         //     new GridFTPClientCreator( "csr-pc35",  2811, cp );
         //c = cc.call();
 
         System.err.println( "requesting features: " );
         FeatureList l = c.getFeatureList();
 
         System.err.println( "chdir to " + dir );
         c.changeDir( dir );
         //Vector v = c.mlsd();
         //for( Object o: v ) {
         //    System.out.println( (String) o );
         //}
         list();
     }
 
     
     private void list( ) throws Exception {
         System.err.println( "DONE. Requesing listing" );
 
         // TreeMap<String,String> files = new TreeMap<String,String>( );
         Vector<FileInfo> inf = c.list( );
         for( FileInfo fi: inf ) {
             if( fi.isFile() ) {
                 String n = fi.getName();
                 System.out.println( n );
          //       files.put( , null );
             }
         }
     }
 
 }
