package xtremweb.roles.examples.test;
 
 import java.io.File;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.apache.commons.net.io.CopyStreamException;
 import junit.framework.TestCase;
 
 import org.junit.*;
 
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.api.bitdew.BitDewException;
 import xtremweb.api.transman.TransferManager;
 import xtremweb.api.transman.TransferManagerException;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.role.examples.FTPServer;
 import xtremweb.role.examples.PutGet;
 
 public class FTPServerTest extends TestCase {
 
     private FTPServer ftp;
 
     private final String testhost = "ftp.lip6.fr";
     private final String testlogin = "anonymous";
     private final String testpath = "/pub/linux/distributions/slackware/slackware-current";
 
     // TODO look for allowing username and password, currently just anonymous
     // connection
     public void setUp() {
 	
	ftp = new FTPServer(true,null);;
 	
 	try {
 	    ftp.connect();
 	    ftp.login();
 	} catch (Exception e) {
 	    fail();
 	    e.printStackTrace();
 	}
     }
 
     @Test
     public void testChangeDirectory() {
 
 	try {
 	    String curr=ftp.changeDirectory();
 	    assertEquals(curr, testpath);
 	} catch (Exception e) {
 	    fail();
 	    e.printStackTrace();
 	}
     }
 
     @Test
     public void testGetSignatures() {
 	Properties p;
 	try {
 	    ftp.changeDirectory();
 	    p = ftp.getSignatures();
 	    assertNotNull(p);
 	    assertNotNull(p.get("COPYRIGHT.TXT"));
 	    assertNotNull(p.get("README_LVM.TXT"));
 	    assertNotNull(p.get("README_CRYPT.TXT"));
 	    assertNotNull(p.get("SPEAK_INSTALL.TXT"));
 	    assertEquals(p.get("COPYRIGHT.TXT"),
 		    "4a79c816389ed23235c36244f4210c81");
 	    assertEquals(p.get("README_LVM.TXT"),
 		    "1e5ebcf8675508efc07cc6b869dbe6e6");
 	    assertEquals(p.get("README_CRYPT.TXT"),
 		    "f7a6a57330634d5eb77eae3f3d5c32bc");
 	    assertEquals(p.get("SPEAK_INSTALL.TXT"),
 		    "058f7b10e84d0d51d7725e923ed11cc9");
 	    //assertNotNull(p.get("test.txt"));
 	} catch (Exception e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
     }
 
 
     @Test
     public void testMakeAvailable() {
 	
 	PutGet pg=null;
 	try {
 	     pg = new PutGet("localhost",4325);
 	} catch (Exception e1) {
 	    // TODO Auto-generated catch block
 	    e1.printStackTrace();
 	}
 	 try {
 	     ftp.changeDirectory();
 	     Vector v = ftp.makeAvailable();
 	for (int i = 0; i < 1; i++) 
 	{
 	    String s = (String)v.get(i);
 	    System.out.println("Trying  " + s);
 	    BitDew bd = new BitDew(ComWorld.getMultipleComms("localhost","rmi",4325,"dr","dt","dc"));
 	    Data d = bd.searchDataByUid(s);
 	    assertNotNull(d);
 	    //pg.get("newfile"+i+".txt",s);
 	   // file = new File("newfile"+i+".txt");
 	    //assertTrue(file.length() > 0);
 	    //log.info("data has been successfully copied to " + fileName);
 	    
 	  /*  File file = new File(fileName);
 
 		//retreive the data object
 		Data data = bitdew.searchDataByUid(dataUid);	    
 		//copy the remote data into the local file
 		bitdew.get(data, file);	
 		//wait for the data transfer to complete and stop the transfer manager
 		transferManager.waitFor(data);
 		transferManager.stop();
 		log.info("data has been successfully copied to " + fileName);*/
 	    
 	    
 	}
 	} 
 	 catch (BitDewException e) {	
 		e.printStackTrace();
 		fail();
 	    } catch (TransferManagerException e) {
 		
 	    e.printStackTrace();
 	    fail();
 	} catch (Exception e) {	   
 		e.printStackTrace();
 		 fail();
 	    }
     }
 
     public void tearDown() {
 	//ftp.disconnect();
     }
 
 }
