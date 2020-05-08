 package net.sf.jpam;
 
 import org.eel.kitchen.pam.PamReturnValue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.util.EnumSet;
 
 import static org.testng.Assert.*;
 
 
 public class PamTest
     extends AbstractPamTest
 {
     private static final Logger LOG = LoggerFactory.getLogger(PamTest.class);
 
     @Test
     public void testSharedLibraryInstalledInLibraryPath()
     {
         final String libraryPath = System.getProperty("java.library.path");
         final String pathSeparator = System.getProperty("path.separator");
         final String libraryName = Pam.getLibraryName();
         final String[] pathElements = libraryPath.split(pathSeparator);
         boolean found = false;
         for (final String pathElement : pathElements) {
             final File sharedLibraryFile = new File(
                 pathElement + File.separator + libraryName);
             if (sharedLibraryFile.exists()) {
                 found = true;
                 LOG.info("Library " + libraryName + " found in " + pathElement);
             }
         }
         assertTrue(found);
     }
 
     @Test
     public void testJNIWorking()
         throws PamException
     {
         final Pam pam = new Pam();
         assertTrue(pam.isSharedLibraryWorking());
     }
 
     @Test
     public void testUserAuthenticated()
         throws PamException
     {
         final Pam pam = new Pam();
         assertEquals(pam.authenticate(user, passwd),
             PamReturnValue.PAM_SUCCESS);
     }
 
     @Test
     public void testUserWithBadCredentialsNotAuthenticated()
         throws PamException
     {
         final Pam pam = new Pam();
         assertNotEquals(pam.authenticate(user, badPasswd),
             PamReturnValue.PAM_SUCCESS);
     }
 
     @Test
     public void testUserWithNullCredentials()
         throws PamException
     {
         final Pam pam = new Pam();
         try {
             pam.authenticate(user, null);
             fail("No exception thrown");
         } catch (PamException e) {
             assertEquals(e.getMessage(), "credentials are null");
         }
     }
 
     @Test
     public void testUserWithEmptyCredentials()
         throws PamException
     {
         final EnumSet<PamReturnValue> set
             = EnumSet.of(PamReturnValue.PAM_USER_UNKNOWN,
                 PamReturnValue.PAM_AUTH_ERR);
 
         final Pam pam = new Pam();
        final PamReturnValue retval = pam.authenticate(user, "");
         assertTrue(set.contains(retval));
     }
 
     @Test
     public void testUserWithNullUsername()
         throws PamException
     {
         final Pam pam = new Pam();
         try {
             pam.authenticate(null, "whatever");
             fail("No exception thrown");
         } catch (PamException e) {
             assertEquals(e.getMessage(), "user name is null");
         }
     }
 
     @Test
     public void testUserWithEmptyUsername()
         throws PamException
     {
         final EnumSet<PamReturnValue> set
             = EnumSet.of(PamReturnValue.PAM_PERM_DENIED,
             PamReturnValue.PAM_AUTH_ERR);
 
         final Pam pam = new Pam();
         final PamReturnValue retval = pam.authenticate(user, "");
         assertTrue(set.contains(retval));
     }
 
     @Test
     public void testNullService()
     {
         try {
             new Pam(null);
             fail("No exception thrown");
         } catch (PamException e) {
             assertEquals(e.getMessage(), "service name is null");
         }
     }
 
     @Test
     public void testEmptyServiceName()
     {
         try {
             new Pam("");
             fail("No exception thrown");
         } catch (PamException e) {
             assertEquals(e.getMessage(), "service name is empty");
         }
     }
 }
