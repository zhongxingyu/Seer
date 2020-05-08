 import java.io.File;
 import java.io.FileOutputStream;
 import java.security.SecureRandom;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class VmBackup
 {
 
   private String host;
   private String user;
   private String password;
 
   /**
    * @param args
    */
   public static void main( String[] args ) throws Exception
   {
     Options options = createOptions();
 
     CommandLineParser parser = new GnuParser();
     try
     {
       // parse the command line arguments
       CommandLine line = parser.parse( options, args );
 
       // validate that block-size has been set
       if ( !line.hasOption( "h" ) || !line.hasOption( "u" ) || !line.hasOption( "p" ) )
       {
         printHelp( options );
         System.exit( -1 );
       }
 
       if ( !line.hasOption( "l" ) && !line.hasOption( "v" ) )
       {
         printHelp( options );
         System.exit( -1 );
       }
 
       SSLContext ctx = SSLContext.getInstance( "TLS" );
       ctx.init( new KeyManager[0], new TrustManager[]{ new DefaultTrustManager() }, new SecureRandom() );
       SSLContext.setDefault( ctx );
 
       VmBackup vmBackup = new VmBackup();
 
       vmBackup.host = line.getOptionValue( "h" );
       vmBackup.user = line.getOptionValue( "u" );
       vmBackup.password = line.getOptionValue( "p" );
 
       if ( line.hasOption( "l" ) )
       {
 
         System.out.println( "Found following vms on host " + vmBackup.host + ": \n" );
 
         Map<String, String> vms = vmBackup.getVms();
         for ( String vm : vms.keySet() )
         {
           System.out.println( vm );
         }
       }
       else if ( line.hasOption( "v" ) )
       {
 
         String vm = line.getOptionValue( "v" );
 
         Map<String, String> vms = vmBackup.getVms();
 
         if ( !vms.containsKey( vm ) )
         {
           System.out.println( "ERROR: could not find vm " + vm );
         }
 
         String target = "";
 
         if ( line.hasOption( "t" ) )
         {
           target = line.getOptionValue( "t" );
 
           if ( !target.endsWith( "/" ) )
           {
             target = target + "/";
           }
         }
 
         target = target + vm;
 
         File targetDir = new File( target );
 
         if ( !targetDir.exists() )
         {
           if ( !targetDir.mkdirs() )
           {
             System.out.println( "ERROR: Could not create " + targetDir.getAbsolutePath() );
             System.exit( 1 );
           }
         }
 
         List<VmWareFile> files = vmBackup.getVmFiles( vms.get( vm ) );
 
         System.out.println( "Will backup " + vm + " to " + targetDir.getAbsolutePath() );
 
         for ( VmWareFile file : files )
         {
           vmBackup.backupFile( file, targetDir );
         }
 
       }
     }
     catch ( ParseException exp )
     {
       // oops, something went wrong
       System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
     }   
   }
 
   private void backupFile( VmWareFile pFile, File targetDir )
   {
     try
     {
 
       File targetFile = new File( targetDir.getAbsolutePath() + File.separator + pFile.name );
       System.out.println( pFile.url + " > " + targetFile.getAbsolutePath() );
 
       HttpClient client = createHttpClient();
 
       GetMethod get = new GetMethod( "https://" + host + pFile.url.replaceAll( "amp;", "" ) );
       int code = client.executeMethod( get );
       if ( code == 200 )
       {
         IOUtils.copy( get.getResponseBodyAsStream(), new FileOutputStream( targetFile ) );
       }
       else
       {
         System.out.println( "ERROR: got " + code + " for " + pFile.url );
       }
       
       if(FileUtils.sizeOf( targetFile ) == pFile.size) {
         System.out.println("Filesize check: OK");
       } else {
         System.out.println("ERROR: File size differ " +  pFile.url + "=" + pFile.size + " != " + targetFile.getAbsolutePath() + "=" + FileUtils.sizeOf( targetFile ) );
       }
     }
     catch ( Exception e )
     {
       System.err.println( e );
     }
   }
 
   private static void printHelp( Options options )
   {
     HelpFormatter h = new HelpFormatter();
     h.printHelp( "vmbackup [OPTION]", options );
 
     System.out.println( "\nExamples:\n" );
     System.out.println( "vmbackup --host=192.168.82.41 --user=root --password=god --list" );
     System.out
         .println( "vmbackup --host=192.168.82.41 --user=root --password=god --vm=vmlinzh03 --target=/data/vmbackup" );
   }
 
   private static Options createOptions()
   {
 
     Options options = new Options();
 
     options.addOption( "h", "host", true, "The vm host ip adress" );
     options.addOption( "u", "user", true, "Username to use for authentication" );
     options.addOption( "p", "password", true, "Password to use for authentication" );
     options.addOption( "l", "list", false, "List all vms from the host" );
     options.addOption( "v", "vm", true, "Vm to backup" );
     options.addOption( "t", "target", true, "Target directory to backup to" );
 
     return options;
   }
 
   private String getUrl( String url )
   {
     try
     {
       url = url.replaceAll( "amp;", "" );
       HttpClient client = createHttpClient();
       GetMethod get = new GetMethod( url );
       int code = client.executeMethod( get );
       if ( code == 200 )
       {
         return IOUtils.toString( get.getResponseBodyAsStream() );
       }
       else
       {
         System.out.println( "ERROR: got " + code + " for " + url );
       }
     }
     catch ( Exception e )
     {
       System.out.println( "ERROR: " + e.getMessage() );
       System.exit( 1 );
     }
 
     return null;
   }
 
   private HttpClient createHttpClient()
   {
     HttpClient client = new HttpClient();
     Credentials defaultcreds = new UsernamePasswordCredentials( user, password );
     client.getState().setCredentials( new AuthScope( host, 443, AuthScope.ANY_REALM ), defaultcreds );
     return client;
   }
 
   private List<VmWareFile> getVmFiles( String vmUrl )
   {
 
     String data = getUrl( "https://" + host + vmUrl );
 
     List<VmWareFile> files = new ArrayList<VmBackup.VmWareFile>();
 
     Pattern pattern =
       Pattern
           .compile( "<a href=\"([^\"]+)\">([^<]+)</a></td><td align=\"right\">[^<]+</td><td align=\"right\">([\\d]+)</td></tr>" );
     Matcher matcher = pattern.matcher( data );
 
     while ( matcher.find() )
     {
 
       VmWareFile file = new VmWareFile();
 
       file.url = matcher.group( 1 );
       file.name = matcher.group( 2 );
       file.size = Long.parseLong( matcher.group( 3 ) );
 
       files.add( file );
     }
 
     return files;
   }
 
   private Map<String, String> getVms()
   {
 
     Map<String, String> datastores = getDatastores();
     Map<String, String> vms = new HashMap<String, String>();
 
     for ( String datastore : datastores.keySet() )
     {
       String datastoreUrl = datastores.get( datastore );
       String data = getUrl( "https://" + host + datastoreUrl );
       Pattern pattern = Pattern.compile( "<a href=\"([^\"]+)\">([^<]+)/</a>" );
       Matcher matcher = pattern.matcher( data );
 
       while ( matcher.find() )
       {
         vms.put( matcher.group( 2 ), matcher.group( 1 ) );
       }
     }
 
     return vms;
   }
 
   private Map<String, String> getDatastores()
   {
     String url = "https://" + host + "/folder?dcPath=ha-datacenter";
     String tData = getUrl( url );
    Pattern pattern = Pattern.compile( "<a href=\"([^\"]+)\">(datastore[^<]+)</a>" );
 
     Matcher matcher = pattern.matcher( tData );
 
     Map<String, String> datastores = new HashMap<String, String>();
 
     while ( matcher.find() )
     {
       datastores.put( matcher.group( 2 ), matcher.group( 1 ) );
     }
 
     return datastores;
   }
 
   private static class VmWareFile
   {
     String name;
     String url;
     Long   size;
 
     @Override
     public String toString()
     {
 
       return name + "(" + size + "): " + url;
     }
   }
 
   private static class DefaultTrustManager implements X509TrustManager
   {
 
     public void checkClientTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException
     {
     }
 
     public void checkServerTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException
     {
     }
 
     public X509Certificate[] getAcceptedIssuers()
     {
       return null;
     }
   }
 }
