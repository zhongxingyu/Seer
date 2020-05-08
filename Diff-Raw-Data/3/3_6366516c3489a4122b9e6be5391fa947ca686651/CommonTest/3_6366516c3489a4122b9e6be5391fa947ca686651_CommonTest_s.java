 package org.ow2.chameleon.everest.fileSystem.test;
 
 import org.junit.Test;
 import org.ops4j.pax.exam.Option;
 import org.ow2.chameleon.everest.services.*;
 import org.ow2.chameleon.testing.helpers.BaseTest;
 
 import javax.inject.Inject;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.ops4j.pax.exam.CoreOptions.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: colin
  * Date: 19/08/13
  * Time: 15:59
  * To change this template use File | Settings | File Templates.
  */
 public class CommonTest extends BaseTest {
 
     /**
      * The everest services.
      */
     @Inject
     EverestService everest;
 
     /**
      * Common test options.
      */
     @Override
     protected Option[] getCustomOptions() {
 
         return options(  // everest bundles
                 mavenBundle("org.ow2.chameleon.everest", "everest-core").versionAsInProject(),
                 mavenBundle("org.ow2.chameleon.everest", "everest-fs").versionAsInProject(),
                mavenBundle("org.ow2.chameleon.everest", "everest-client-API").versionAsInProject(),
                // Fest assert JARs wrapped as bundles
                 wrappedBundle(mavenBundle("org.easytesting", "fest-util").versionAsInProject()),
                 wrappedBundle(mavenBundle("org.easytesting", "fest-assert").versionAsInProject()),
                 mavenBundle("org.apache.felix", "org.apache.felix.eventadmin").versionAsInProject(),
                 bootDelegationPackage("com.intellij.rt.coverage.data")
         );
     }
 
 
     @Override
     public boolean deployTestBundle() {
         return false;
     }
 
     @Test
     public void True() {
         assertThat("true").isEqualTo("true");
     }
 
 
 
 }
