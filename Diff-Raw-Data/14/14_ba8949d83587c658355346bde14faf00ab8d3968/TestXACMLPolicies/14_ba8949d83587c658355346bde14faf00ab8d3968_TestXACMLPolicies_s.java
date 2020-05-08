 package fedora.test.api;
 
 import java.beans.IntrospectionException;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Date;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.apache.commons.betwixt.io.BeanReader;
 import org.apache.commons.logging.Log;
 import org.custommonkey.xmlunit.SimpleXpathEngine;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import fedora.client.FedoraClient;
 import fedora.server.access.FedoraAPIA;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.security.servletfilters.xmluserfile.Attribute;
 import fedora.server.security.servletfilters.xmluserfile.FedoraUsers;
 import fedora.server.security.servletfilters.xmluserfile.User;
 import fedora.server.types.gen.Property;
 import fedora.server.utilities.ServerUtility;
 import fedora.server.utilities.StreamUtility;
 import fedora.test.DemoObjectTestSetup;
 import fedora.test.FedoraServerTestCase;
 import fedora.utilities.install.InstallationFailedException;
 
 /**
  * Test of the Fedora Access Service (API-A).
  * 		describeRepository
  * 		findObjects
  * 		getDatastreamDissemination
  * 		getDissemination
  * 		getObjectHistory
  * 		getObjectProfile
  * 		listDatastreams
  * 		listMethods
  * 		resumeFindObjects
  * 
  * See: http://www.fedora.info/definitions/1/0/api/Fedora-API-A.html
  * 
  * @author Edwin Shin
  *
  */
 public class TestXACMLPolicies extends FedoraServerTestCase {
     private FedoraClient admin;
     private FedoraClient testuser1;
     private FedoraClient testuserroleA;
     private FedoraClient testuser2;
     private FedoraClient testuser3;
     private FedoraClient testuserroleB;
     private FedoraClient testuserroleC;
     private FedoraClient testuser4;
 	private static int EXPECT_FAILURE = 0;
     private static int EXPECT_SUCCESS = 1;
     private File fedoraUsersBackup = null;
     
 	public static Test suite() 
     {
 		TestSuite suite = new TestSuite("XACML Policy TestSuite");
 		suite.addTestSuite(TestXACMLPolicies.class);
 		return new DemoObjectTestSetup(suite);
 	}
 	
 	public void testXACMLAPIMAccess() throws Exception 
     {
         String dateOfFirstSuccess=null;
         String dateOfSecondSuccess=null;
         String dateOfThirdSuccess=null;
         String dateOfFourthSuccess=null;
         String URL1 = getBaseURL()+"-demo/simple-image-demo/col1.jpg";
         String URL2 = getBaseURL()+"-demo/simple-image-demo/col2.jpg";
         String URL3 = getBaseURL()+"-demo/simple-image-demo/col3.jpg";
         Class modDSArgs[] = { String.class, String.class, String[].class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.TYPE };
         Object modDSParms1[] = { "demo:5", "THUMBRES_IMG", null, null, null, null, null, null, null, null, Boolean.FALSE };
 //        Object modDSParms2[] = { "demo:5", "THUMBRES_IMG", null, ", null, null, null, null, false };
         Class purgeDSArgs[] = { String.class, String.class, String.class, String.class, String.class, Boolean.TYPE };
         Object purgeDSParms1[] = { "demo:5", "THUMBRES_IMG", null, null, null, Boolean.FALSE };
         Class setVersionableArgs[] = { String.class, String.class, Boolean.TYPE, String.class };
         Object setVersionableFalse[] = { "demo:5", "THUMBRES_IMG", Boolean.FALSE, null };
         Object setVersionableTrue[] = { "demo:5", "THUMBRES_IMG", Boolean.TRUE, null };
         
         //        Object modDSParms2[] = { "demo:5", "THUMBRES_IMG", null, null, null, null, URL2, null, false };
 //        Object modDSParms3[] = { "demo:5", "THUMBRES_IMG", null, null, null, null, null, null, false };
 //        Object modDSParms4[] = { "demo:5", "THUMBRES_IMG", null, null, null, null, URL3, null, false };
         
         // APIM access by user without access- should fail
         // testuserroleA does not have permission to modify a datastream, so this should fail
         invokeAPIMFailure(testuserroleA, "testuserroleA", "modifyDatastreamByReference", modDSArgs, modDSParms1);
         
         // APIM access by user without access- should fail
         // testuserroleA does not have permission to modify a datastream, so this should fail
      //   invokeAPIMFailure(testuser2, "testuser2", "modifyDatastreamByReference", modDSArgs, modDSParms1);
          
         //APIM accesses by users with access- should succeed
         modDSParms1[6] = URL1;
         dateOfFirstSuccess = invokeAPIMSuccessString(testuser1, "testuser1", "modifyDatastreamByReference", modDSArgs, modDSParms1);
         System.out.println("    URL = "+ modDSParms1[6]);
         assertTrue(dateOfFirstSuccess != null);
         System.out.println("  Modify datastream from testuser1 succeeded.");
 
         System.out.println("Disabling versioning.");
         invokeAPIMSuccess(admin, "admin", "setDatastreamVersionable", setVersionableArgs, setVersionableFalse);
 
         modDSParms1[6] = URL2;
         System.out.println("Testing modify datastream from admin with versioning off.");
         dateOfSecondSuccess = invokeAPIMSuccessString(admin, "admin", "modifyDatastreamByReference", modDSArgs, modDSParms1);
         System.out.println("    URL = "+ modDSParms1[6]);
         assertTrue(dateOfSecondSuccess != null);
         System.out.println("  Modify datastream from admin succeeded.");
 
         modDSParms1[6] = null;
         modDSParms1[3] = "The Colliseum with Graffiti";
         System.out.println("Testing modify datastream from admin with versioning off just changing label.");
         dateOfThirdSuccess = invokeAPIMSuccessString(admin, "admin", "modifyDatastreamByReference", modDSArgs, modDSParms1);
         System.out.println("    Label = "+ modDSParms1[3]);
         assertTrue(dateOfThirdSuccess != null);
         System.out.println("  Modify datastream from admin succeeded.");
 
         System.out.println("Re-enabling versioning.");
         invokeAPIMSuccess(admin, "admin", "setDatastreamVersionable", setVersionableArgs, setVersionableTrue);
  
         modDSParms1[6] = URL3;
         modDSParms1[3] = null;
         dateOfFourthSuccess = invokeAPIMSuccessString(testuser1, "testuser1", "modifyDatastreamByReference", modDSArgs, modDSParms1);
         System.out.println("    URL = "+ modDSParms1[6]);
         assertTrue(dateOfFourthSuccess != null);
         System.out.println("  Modify datastream from testuser1 succeeded.");
         
         // APIM access by user without access- should fail
         purgeDSParms1[2] = dateOfFirstSuccess;
         purgeDSParms1[3] = dateOfFourthSuccess;
         // testuser1 does not have permission to purge a datastream, so this should fail
         invokeAPIMFailure(testuser1, "testuser1", "purgeDatastream", purgeDSArgs, purgeDSParms1);
 
         // APIM access by user without access- should fail
         purgeDSParms1[2] = dateOfFirstSuccess;
         purgeDSParms1[3] = dateOfFourthSuccess;
         // testuser2 does not have permission to purge a datastream, so this should fail
      //  invokeAPIMFailure(testuser2, "testuser2", "purgeDatastream", purgeDSArgs, purgeDSParms1);
         
         //APIM access by user with access- should succeed
        // testuser1 does not have permission to modify a datastream, so this should fail
        String purged[] = invokeAPIMSuccessStringArray(testuserroleA, "testuserroleA", "purgeDatastream", purgeDSArgs, purgeDSParms1);
         System.out.println("    Checking number of versions purged.");
         assertEquals(purged.length, 2);
         System.out.println("    Checking dates of versions purged.");            
         assertEquals(purged[0], dateOfThirdSuccess);
         assertEquals(purged[1], dateOfFourthSuccess);
         System.out.println("Purge Datastreams successful.");            
  	}
 	
     public void testXACMLAPIAAccess() throws Exception 
     {
         if (isAPIAAuthzOn())
         {
             Class getDDArgs[] = { String.class, String.class, String.class };
             Object getDDParms[] = { "demo:5", "THUMBRES_IMG", null };
             Object getDDParms2[] = { "demo:29", "url", null };
             Object getDDParms3[] = { "demo:31", "DS1", null };
 
             Class getDissArgs[] = { String.class, String.class, String.class, Property[].class, String.class };
             Object getDissParms[] = {"demo:5", "demo:1", "getHigh", null, null };
             Object getDissParms2[] = {"demo:29", "demo:27", "grayscaleImage", null, null};
             Class modObjArgs[] = { String.class, String.class, String.class, String.class, String.class };
             Object modObjParms[] = { "demo:31", null, null, null, null };
             
             
             // APIA access by user without access- should fail
             // testuser2 does not have permission to access api-a at all, so this should fail
        //     invokeAPIAFailure(testuser2, "testuser2", "getDatastreamDissemination", getDDArgs, getDDParms);
 
             // APIA access by user without access- should fail
             // testuser3 does not have permission to access Datastreams named THUMBRES_IMG, so this should fail
             invokeAPIAFailure(testuser3, "testuser3", "getDatastreamDissemination", getDDArgs, getDDParms);
             
             // APIA access by user without access- should fail
             // testuserroleB does not have permission to access HighRes Dissemenations, so this should fail
             invokeAPIAFailure(testuserroleB, "testuserroleB", "getDissemination", getDissArgs, getDissParms);
             
             // APIA access by user without access- should fail
             // testuser4 does not have permission to access demo:29 at all, so this should fail
             invokeAPIAFailure(testuser4, "testuser4", "getDatastreamDissemination", getDDArgs, getDDParms2);
 
             // APIA access by user without access- should fail
             // testuser4 does not have permission to access demo:29 at all, so this should fail
             invokeAPIAFailure(testuser4, "testuser4", "getDissemination", getDissArgs, getDissParms2);
 
             // APIA access by user without access- should fail
             // testuser1 does not have permission to access demo:29 datastreams, so this should fail
             invokeAPIAFailure(testuser1, "testuser1", "getDatastreamDissemination", getDDArgs, getDDParms2);
 
             // APIA access by user with access- should succeed
             // testuserroleC does have permission to access demo:29 datastreams, so this should succeed
             invokeAPIASuccess(testuserroleC, "testuserroleC", "getDatastreamDissemination", getDDArgs, getDDParms2);
 
             // APIA access by user with access- should succeed
             // testuser1 does have permission to access demo:5 datastreams, so this should succeed
             invokeAPIASuccess(testuser1, "testuser1", "getDatastreamDissemination", getDDArgs, getDDParms);       
             
             // APIA access by user who is not owner should fail
             // testuser1 is not currently owner of demo:31, so this should fail
             invokeAPIAFailure(testuser1, "testuser1", "getDatastreamDissemination", getDDArgs, getDDParms3);       
             
             modObjParms[3] = "testuser1";
             String dateOfSuccess = invokeAPIMSuccessString(admin, "fedoraAdmin", "modifyObject", modObjArgs, modObjParms);
             assertTrue(dateOfSuccess != null);
             System.out.println("  Modify Object from admin succeeded.");
  
             // APIA access by user who is now the owner, should succeed
             // testuser1 is now currently owner of demo:31, so this should succeed
             invokeAPIASuccess(testuser1, "testuser1", "getDatastreamDissemination", getDDArgs, getDDParms3);       
 
             modObjParms[3] = "fedoraAdmin";
             dateOfSuccess = invokeAPIMSuccessString(admin, "fedoraAdmin", "modifyObject", modObjArgs, modObjParms);
             assertTrue(dateOfSuccess != null);
             System.out.println("  Modify Object from admin succeeded.");
         }
         else
         {
             System.out.println("Authorization is not enabled for APIA");
             System.out.println("Testing Policies for APIA access will not work.");
         }
     }
     
     public void invokeAPIMFailure(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         // APIA access by user without access- should fail
         try {
             System.out.println("Testing "+ functionToTest + " from invalid user: "+ username);
     
             FedoraAPIM apim1 = user.getAPIM();
             Method func = apim1.getClass().getMethod(functionToTest, args);
             Object result = func.invoke(apim1, parms);
             fail("Illegal access allowed");
         }
         catch (InvocationTargetException ite)
         {
             Throwable cause = ite.getCause();
             if (cause instanceof org.apache.axis.AxisFault)
             {
                 org.apache.axis.AxisFault af = (org.apache.axis.AxisFault)(cause);
                 System.out.println("    Reason = " +af.getFaultReason().substring(af.getFaultReason().lastIndexOf(".")+1));
                 assertTrue(af.getFaultReason().contains("AuthzDeniedException"));
                 System.out.println("Access denied correctly");
             }
             else
             {
                 System.out.println("Got exception: " + cause.getClass().getName());
                 fail("Illegal access dis-allowed for some other reason");
             }
         }
         catch (IOException ioe)
         {
             System.out.println("    Reason = " +ioe.getMessage()/*.substring(ioe.getMessage().lastIndexOf("["))*/);
             assertTrue(ioe.getMessage().contains("[403 Forbidden]"));
             System.out.println("Access denied correctly");
             // exception was expected, all is A-OK
         }
         catch (Exception ae)
         {
             System.out.println("Some other exception: " + ae.getClass().getName());
             fail("Some other exception");
         }
     }
     
     public String invokeAPIMSuccessString(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         Object result = invokeAPIMSuccess(user, username, functionToTest, args, parms);
         return(String)result;
     }
    
     public String[] invokeAPIMSuccessStringArray(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         Object result = invokeAPIMSuccess(user, username, functionToTest, args, parms);
         return(String[])result;
     }
    
     
     public Object invokeAPIMSuccess(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         // APIA access by user with access- should succeed
         try {
             // testuser1 does have permission to access demo:5 datastreams, so this should succeed
             System.out.println("Testing "+ functionToTest + " from valid user: "+ username);
             FedoraAPIM apim1 = user.getAPIM();
             Method func = apim1.getClass().getMethod(functionToTest, args);
             Object result = func.invoke(apim1, parms);
             assertTrue(result != null);
             return(result);
         }
         catch (InvocationTargetException ite)
         {
             Throwable cause = ite.getCause();
             if (cause instanceof org.apache.axis.AxisFault)
             {
                 org.apache.axis.AxisFault af = (org.apache.axis.AxisFault)(cause);
                 System.out.println("Got exception: " + af.getClass().getName());
                 System.out.println("Reason = " +af.getFaultReason());
                 System.out.println("Message = " +af.getMessage());
                 fail("Legal access dis-allowed");                    
             }
             else
             {
                 System.out.println("Got exception: " + cause.getClass().getName());
                 fail("Legal access dis-allowed");
             }
         }
         catch (Exception e)
         {
             System.out.println("Got exception: " +e.getClass().getName());
             fail("Legal access dis-allowed");
         }
         
         return(null);
     }   
     
     public void invokeAPIAFailure(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         // APIA access by user without access- should fail
         try {
             System.out.println("Testing "+ functionToTest + " from invalid user: "+ username);
     
             FedoraAPIA apia1 = user.getAPIA();
             Method func = apia1.getClass().getMethod(functionToTest, args);
             Object result = func.invoke(apia1, parms);
             fail("Illegal access allowed");
         }
         catch (InvocationTargetException ite)
         {
             Throwable cause = ite.getCause();
             if (cause instanceof org.apache.axis.AxisFault)
             {
                 org.apache.axis.AxisFault af = (org.apache.axis.AxisFault)(cause);
                 System.out.println("    Reason = " +af.getFaultReason().substring(af.getFaultReason().lastIndexOf(".")+1));
                 assertTrue(af.getFaultReason().contains("AuthzDeniedException"));
                 System.out.println("Access denied correctly");
             }
             else
             {
                 System.out.println("Got exception: " + cause.getClass().getName());
                 fail("Illegal access dis-allowed for some other reason");
             }
         }
         catch (IOException ioe)
         {
             System.out.println("    Reason = " +ioe.getMessage().substring(ioe.getMessage().lastIndexOf("[")));
             assertTrue(ioe.getMessage().contains("[403 Forbidden]"));
             System.out.println("Access denied correctly");
             // exception was expected, all is A-OK
         }
         catch (Exception ae)
         {
             System.out.println("Some other exception: " + ae.getClass().getName());
             fail("Illegal access dis-allowed for some other reason");
         }
     }
         
     public Object invokeAPIASuccess(FedoraClient user, String username, String functionToTest, Class args[], Object parms[])
     {
         // APIA access by user with access- should succeed
         try {
             // testuser1 does have permission to access demo:5 datastreams, so this should succeed
             System.out.println("Testing "+ functionToTest + " from valid user: "+ username);
             FedoraAPIA apia1 = user.getAPIA();
             Method func = apia1.getClass().getMethod(functionToTest, args);
             Object result = func.invoke(apia1, parms);
             assertTrue(result != null);
             System.out.println("Access succeeded");
             return(result);
         }
         catch (InvocationTargetException ite)
         {
             Throwable cause = ite.getCause();
             if (cause instanceof org.apache.axis.AxisFault)
             {
                 org.apache.axis.AxisFault af = (org.apache.axis.AxisFault)(cause);
                 System.out.println("Got exception: " + af.getClass().getName());
                 System.out.println("Reason = " +af.getFaultReason());
                 System.out.println("Message = " +af.getMessage());
                 fail("Legal access dis-allowed");                    
             }
             else
             {
                 System.out.println("Got exception: " + cause.getClass().getName());
                 fail("Legal access dis-allowed");
             }
         }
         catch (Exception e)
         {
             System.out.println("Got exception: " +e.getClass().getName());
             fail("Legal access dis-allowed");
         }
         
         return(null);
     }   
     
     public boolean isAPIAAuthzOn() throws IOException
     {
         File installProperties = new File(FEDORA_HOME,"install/install.properties");
         BufferedReader prop = null;
         try{
             prop = new BufferedReader(new FileReader(installProperties));
             String line = null;
             while ((line = prop.readLine())!= null)
             {
                 if (line.startsWith("apia.auth.required"))
                 {
                     if (line.equals("apia.auth.required=true"))
                     {
                         return(true);
                     }
                     if (line.equals("apia.auth.required=false"))
                     {
                         return(false);
                     }
                 }
             }
             return(false);
         }
         finally 
         {
             if (prop != null)  prop.close();
         }      
     }
     
 	public void installJunitPolicies()
     {
         System.out.println("Copying Policies For Testing");
         File junitDir = new File("src/test/junit/XACMLTestPolicies/junit");
         File junitsaveDir = new File(FEDORA_HOME, "data/fedora-xacml-policies/repository-policies/junit");
         if (!junitsaveDir.exists())
         {
             junitsaveDir.mkdir();
         }
         File list[] = getFilesInDir(junitDir);
         traverseAndCopy(list, junitsaveDir);
         
         System.out.println("Copying Policies succeeded");
     }
     
     private void deleteJunitPolicies()
     {
         System.out.println("Removing Policies For Testing");
         File junitsaveDir = new File(FEDORA_HOME, "data/fedora-xacml-policies/repository-policies/junit");
         if (junitsaveDir.exists())
         {
             File list[] = getFilesInDir(junitsaveDir);
             traverseAndDelete(list);
             junitsaveDir.delete();
         }
     }
     
     private File[] getFilesInDir(File dir)
     {
         File srcFiles[] = dir.listFiles(new java.io.FilenameFilter()
             {
                 public boolean accept(File dir, String name)
                 {
                     if ((name.toLowerCase().startsWith("permit") ||
                          name.toLowerCase().startsWith("deny")) &&
                          name.endsWith(".xml"))
                     {
                         return(true);
                     }
                     return(false);
                 }
             } 
         );
     
         return(srcFiles);
     }
 
     private void traverseAndCopy(File srcFiles[], File destDir)
     {
   //      assertEquals(testDir.isDirectory(), true);
         for ( int i = 0; i < srcFiles.length; i++)
         {
             File destFile = new File(destDir, srcFiles[i].getName());
             System.out.println("Copying policy: "+ srcFiles[i].getName());
             if (!destFile.exists())
             {
                 try
                 {
                     destFile.createNewFile();
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace();
                 }
             }
             copyFile(srcFiles[i], destFile);
         }        
     }
     
     private void traverseAndDelete(File newFiles[])
     {
         for ( int i = 0; i < newFiles.length; i++)
         {
             System.out.println("Deleting policy: "+ newFiles[i].getName());
             newFiles[i].delete();
         }        
     }
 
 	private boolean copyFile(File src, File dest)
     {
         InputStream in;
         try
         {
             in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest);
             StreamUtility.pipeStream(in, out, 1024);
         }
         catch (FileNotFoundException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return(false);
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return(false);
         }
         return(true);
     }
     
     private void reloadPolicies()
     {
         System.out.println("Reloading Policies...");
         try
         {
             FedoraClient client = new FedoraClient(
                     ServerUtility.getBaseURL(getProtocol()), getUsername(),
                             getPassword());
             client.reloadPolicies();
             System.out.println("  Done Reloading Policies");
         }
         catch (Exception e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     private void backupFedoraUsersFile()
     {
         fedoraUsersBackup = new File(FedoraUsers.fedoraUsersXML.getAbsolutePath()+".backup");
         System.out.println("Backing Up Fedora Users");
         if (!fedoraUsersBackup.exists())
         {
             try
             {
                 fedoraUsersBackup.createNewFile();
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
         copyFile(FedoraUsers.fedoraUsersXML, fedoraUsersBackup);                  
     }
 
     private void restoreFedoraUsersFile()
     {
         System.out.println("Restoring Fedora Users");
         if (!fedoraUsersBackup.exists())
         {
             return;
         }
         copyFile(fedoraUsersBackup, FedoraUsers.fedoraUsersXML);                  
     }
 
     private void createNewFedoraUsersFileWithTestUsers()
     {
         String sep = System.getProperty("line.seperator");
         if (sep == null) sep = "\n";
         String data = "<?xml version='1.0' ?>  " + sep +
                 "<fedora-users>" + sep +
                 "    <user name=\""+getUsername()+"\" password=\""+getPassword()+"\">" + sep +
                 "      <attribute name=\"fedoraRole\">" + sep +
                 "        <value>administrator</value>" + sep +
                 "      </attribute>" + sep +
                 "    </user>" + sep +
                 "    <user name=\"fedoraIntCallUser\" password=\"changeme\">" + sep +
                 "      <attribute name=\"fedoraRole\">" + sep +
                 "        <value>fedoraInternalCall-1</value>" + sep +
                 "        <value>fedoraInternalCall-2</value>" + sep +
                 "      </attribute>" + sep +
                 "    </user>" + sep +
                 "    <user name=\"testuser1\" password=\"testuser1\"/>" + sep +
                 "    <user name=\"testuser2\" password=\"testuser2\"/>" + sep +
                 "    <user name=\"testuser3\" password=\"testuser3\"/>" + sep +
                 "    <user name=\"testuser4\" password=\"testuser4\"/>" + sep +
                 "    <user name=\"testuserroleA\" password=\"testuserroleA\">" + sep +
                 "      <attribute name=\"fedoraRole\">" + sep +
                 "        <value>roleA</value>" + sep +
                 "      </attribute>" + sep +
                 "    </user>" + sep +
                 "    <user name=\"testuserroleB\" password=\"testuserroleB\">" + sep +
                 "      <attribute name=\"fedoraRole\">" + sep +
                 "        <value>roleB</value>" + sep +
                 "      </attribute>" + sep +
                 "    </user>" + sep +
                 "    <user name=\"testuserroleC\" password=\"testuserroleC\">" + sep +
                 "      <attribute name=\"fedoraRole\">" + sep +
                 "        <value>roleC</value>" + sep +
                 "      </attribute>" + sep +
                 "    </user>" + sep +
                 "  </fedora-users>";
         try
         {
             FileOutputStream fu = new FileOutputStream(FedoraUsers.fedoraUsersXML);
             OutputStreamWriter pw = new OutputStreamWriter(fu);
             pw.write(data);
             pw.close();
         }
         catch (FileNotFoundException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
  /*   public static FedoraUsers getFedoraUsers() 
     {
         System.out.println("Getting Fedora Users");
         FedoraUsers fu = null;
         BeanReader reader = new BeanReader();
         if (reader == null) System.out.println("Reader is null");
         reader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
         reader.getBindingConfiguration().setMapIDs(false);
         System.out.println("Got Reader");
 
         try {
             reader.registerMultiMapping(getBetwixtMapping());
             System.out.println("GotBetwixtMapping");
             fu = (FedoraUsers)reader.parse(FedoraUsers.fedoraUsersXML);
             System.out.println("Got f u ");
             if (fu == null) System.out.println("FU is null");
             
         } catch (IntrospectionException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (SAXException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return fu;
     }
     private static final String BETWIXT_MAPPING = "/fedora/server/security/servletfilters/xmluserfile/fedorausers-mapping.xml";
 
     private static InputSource getBetwixtMapping() 
     {
         InputSource is = new InputSource(FedoraUsers.class.getResourceAsStream(BETWIXT_MAPPING));
         if (is == null) System.out.println("Betwixt mapping is null");
         return(is);
     }
     
     private void addNewTestUsersToFedoraUsers() throws IOException
     {
         System.out.println("Adding new Users");
         System.out.println("Fedora Users: " + FedoraUsers.fedoraUsersXML.getAbsolutePath());
         FedoraUsers users = getFedoraUsers();
         users.addUser(makeNewUser("testuser1", "testuser1", null));
         users.addUser(makeNewUser("testuser2", "testuser2", null));
         users.addUser(makeNewUser("testuser3", "testuser3", null));
         users.addUser(makeNewUser("testuser4", "testuser4", null));
         users.addUser(makeNewUser("testuserroleA", "testuserroleA", "roleA"));
         users.addUser(makeNewUser("testuserroleB", "testuserroleB", "roleB"));
         users.addUser(makeNewUser("testuserroleC", "testuserroleC", "roleC"));
         Writer outputWriter = new BufferedWriter(new FileWriter(FedoraUsers.fedoraUsersXML));
         users.write(outputWriter);
         outputWriter.close();        
     }
     
     private User makeNewUser(String name, String password, String fedoraRole)
     {
         User user = new User();
         user.setName(name);
         user.setPassword(password);
         if (fedoraRole != null)
         {
             Attribute att = new Attribute();
             att.setName("fedoraRole");
             att.addValue(fedoraRole);
             user.addAttribute(att);
         }
         return(user);
     }
 */
     public void setUp() throws Exception {
         
         System.out.println("setting Up XACML test");
         admin = getFedoraClient();
 
         SimpleXpathEngine.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
         SimpleXpathEngine.registerNamespace("uvalibadmin", "http://dl.lib.virginia.edu/bin/admin/admin.dtd/");
         backupFedoraUsersFile();
         //addNewTestUsersToFedoraUsers();
         createNewFedoraUsersFileWithTestUsers();
         
         installJunitPolicies();
         reloadPolicies();
         System.out.println("creating alternate users");
         testuser1 = new FedoraClient(getBaseURL(), "testuser1", "testuser1");
         testuserroleA = new FedoraClient(getBaseURL(), "testuserroleA", "testuserroleA");
         testuser2 = new FedoraClient(getBaseURL(), "testuser2", "testuser2");
         testuser3 = new FedoraClient(getBaseURL(), "testuser3", "testuser3");
         testuserroleB = new FedoraClient(getBaseURL(), "testuserroleB", "testuserroleB");
         testuserroleC = new FedoraClient(getBaseURL(), "testuserroleC", "testuserroleC");
         testuser4 = new FedoraClient(getBaseURL(), "testuser4", "testuser4");
         System.out.println("done setting up");
     }
     
     public void tearDown() 
     {
     	SimpleXpathEngine.clearNamespaces();
         restoreFedoraUsersFile();
         deleteJunitPolicies();
         reloadPolicies();
     }
 	    
     public static void main(String[] args) {
 		junit.textui.TestRunner.run(TestXACMLPolicies.class);
 	}
 
 }
