 package hudson.plugins.googlecode.scm;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 import hudson.plugins.googlecode.GoogleCodeProjectProperty;
 import hudson.scm.SubversionSCM.ModuleLocation;
 
 
 public class GoogleCodeSCMExTest {
 
 	@Test
     public void testNewInstance() {
         GoogleCodeSCMEx scm = GoogleCodeSCMEx.DescriptorImpl.newInstance(new GoogleCodeProjectProperty("http://code.google.com/p/leetdev3da/"), "trunk");
         assertThat(scm.getLocations().length, is(1));
         ModuleLocation location = scm.getLocations()[0];
         assertThat(location.getLocalDir(), is("."));
        assertThat(location.getURL(), is("https://leetdev3da.googlecode.com/svn/trunk"));
     }
 
 }
