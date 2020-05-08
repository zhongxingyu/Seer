 /**
  * 
  */
 package esgf.node.stager.io.connectors;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import esgf.node.stager.io.RemoteFile;
 import esgf.node.stager.io.StagerException;
 import esgf.node.stager.utils.ExtendedProperties;
 import esgf.node.stager.utils.Misc;
 
 /**
  * @author k204198
  */
 public class TestFTPConnector {
 
     private static ExtendedProperties testProps;
     private static Date remoteFileDate;
     private static String remoteFileName;
     private static String remoteFileDirectory;
     private static long remoteFileSize;
 
     @BeforeClass
     public static void setupOnce() throws Exception {
         // setup the grabber
         testProps = new ExtendedProperties();
         testProps.put("remoteConnectorFactory",
                 "esgf.node.stager.io.connectors.FTPConnectorFactory");
         testProps.put("ftp.serverName", "ftp3.de.postgresql.org");
         testProps.put("ftp.serverPort", "21");
         testProps.put("ftp.serverRootDirectory", "pub");
         testProps.put("ftp.userName", Misc.transform(false, "Anonymous"));
         testProps.put("ftp.userPassword", Misc.transform(false, "none"));
         
         SimpleDateFormat dFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss 'GMT'");
         dFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
         //manually read from ftp with: modtime <file>
        remoteFileDate = dFormat.parse("09/06/2002 16:45:12 GMT");
         remoteFileName = "default_style.css";
         remoteFileDirectory = "/";
         
         remoteFileSize = 1512L;
         
     }
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
 
     }
 
     /**
      * Test method for
      * {@link esgf.node.stager.io.connectors.FTPConnector#FTPConnector(esgf.node.stager.utils.ExtendedProperties)}
      * .
      */
     @Test
     public void testFTPConnector() {
         // no props
         try {
             new FTPConnector(null);
             fail("Null properties should have failed.");
         } catch (StagerException e) {
             // ok!
         }
 
         // missing required prop
         try {
             ExtendedProperties props = (ExtendedProperties) testProps.clone();
             props.remove("ftp.serverName");
             new FTPConnector(props);
             fail("Null properties should have failed.");
         } catch (StagerException e) {}
 
         // all ok
         try {
             new FTPConnector(testProps);
         } catch (StagerException e) {
             e.printStackTrace();
             fail("Creation failed");
         }
     }
 
     /**
      * Test method for
      * {@link esgf.node.stager.io.connectors.FTPConnector#completeDataInfo(esgf.node.stager.io.RemoteFile)}
      * .
      * 
      * @throws Exception not expected
      */
     @Test
     public void testCompleteDataInfo() throws Exception {
         FTPConnector ftp = new FTPConnector(testProps);
         
         RemoteFile rf = new RemoteFile(remoteFileDirectory + remoteFileName);
         try {
             ftp.completeDataInfo(rf);
         } catch (IOException e) {
             e.printStackTrace();
             fail("Could not retrieve data from ftp server");
         }
         assertEquals(remoteFileDate, rf.getLastMod());
         assertEquals(remoteFileName, rf.getFilename());
         assertEquals(remoteFileDirectory, rf.getDirectory());
         assertEquals(remoteFileSize, rf.getSize());
         
 
     }
 
     /**
      * Test method for getServer* methods.
      * 
      * @throws StagerException not expected
      */
     @Test
     public void testGetServerAttribute() throws Exception {
         FTPConnector ftp = new FTPConnector(testProps);
 
         assertEquals(testProps.getProperty("ftp.serverName"),
                 ftp.getServerName());
         assertEquals((int) testProps.getCheckedProperty("ftp.serverPort", -1),
                 ftp.getServerPort());
         assertEquals(testProps.getProperty("ftp.serverRootDirectory"),
                 ftp.getServerRootDir());
         assertEquals(testProps.getProperty("ftp.userName"),
                 Misc.transform(false, ftp.getServerUserName()));
     }
 
     /**
      * Test method for
      * {@link esgf.node.stager.io.connectors.FTPConnector#retrieveFile(esgf.node.stager.io.RemoteFile, java.io.File)}
      * .
      */
     @Test
     public void testRetrieveFile() {
         
     }
 
 }
