 package org.cagrid.gridgrouper.test;
 
 import edu.internet2.middleware.GrouperInit;
 import gov.nih.nci.cagrid.metadata.ServiceMetadata;
 import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.cxf.configuration.security.KeyStoreType;
 import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFileExtendOption;
 import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFileReplacementOption;
 import org.cagrid.core.common.security.CredentialFactory;
 import org.cagrid.core.common.security.X509Credential;
 import org.cagrid.core.soapclient.SingleEntityKeyManager;
 import org.cagrid.gridgrouper.model.StemDescriptor;
 import org.cagrid.gridgrouper.model.StemIdentifier;
 import org.cagrid.gridgrouper.service.GridGrouperService;
 import org.cagrid.gridgrouper.soapclient.GridGrouperSoapClientFactory;
 import org.cagrid.gridgrouper.wsrf.stubs.GetStemRequest;
 import org.cagrid.gridgrouper.wsrf.stubs.GridGrouperPortType;
 import org.cagrid.gridgrouper.wsrf.stubs.GridGrouperRuntimeFaultFaultMessage;
 import org.cagrid.gridgrouper.wsrf.stubs.StemNotFoundFaultFaultMessage;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.CoreOptions;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.Configuration;
 import org.ops4j.pax.exam.junit.ExamReactorStrategy;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
 
 import javax.net.ssl.KeyManager;
 import java.io.File;
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.ops4j.pax.exam.CoreOptions.maven;
 
 @RunWith(JUnit4TestRunner.class)
 @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
 public class GridGrouperTest extends CaGridTestSupport {
 
     private static final String GRIDGROUPER_URL = "https://localhost:7738/gridgrouper";
 
     private static final String HOST = "etc/cagrid-grid-grouper/grid-grouper-host.jks";
     private static final String TRUSTSTORE = "etc/cagrid-grid-grouper/truststore.jks";
     private static final String TRUSTSTORETYPE = "JKS";
     private static final String KEYALIAS = "host";
     private static final String TRUSTSTOREPASSWORD = "changeit";
     private static final String KEYSTOREPASSWORD = "changeit";
     private static final String KEYPASSWORD = "changeit";
 
     @Override
     @Configuration
     public Option[] config() {
         Option[] options = new Option[] {
                 // need at least internet2 loaded so I can initialize the DB from the test (GrouperInit is part of the internet2 bunle)
                 new KarafDistributionConfigurationFileExtendOption("etc/org.apache.karaf.features.cfg", "featuresRepositories", ","
                         + maven().groupId("org.cagrid").artifactId("cagrid-features").versionAsInProject().classifier("features").type("xml").getURL()),
                 new KarafDistributionConfigurationFileExtendOption("etc/org.apache.karaf.features.cfg", "featuresBoot", ",cagrid-gridgrouper-internet2"),
                 CoreOptions.mavenBundle("org.apache.ant", "com.springsource.org.apache.tools.ant", "1.7.0"),
 
                 // Get our resource files to the "etc" area
                 new KarafDistributionConfigurationFileReplacementOption("etc/cagrid.gridgrouper.wsrf.cfg", new File("src/test/resources/cagrid.gridgrouper.wsrf.cfg")),
                new KarafDistributionConfigurationFileReplacementOption("etc/cagrid.gridgrouper.service.cfg", new File("src/test/resources/cagrid.gridgrouper.service.cfg")),
                 new KarafDistributionConfigurationFileReplacementOption(HOST, new File("src/test/resources/grid-grouper-host.jks")),
                 new KarafDistributionConfigurationFileReplacementOption("etc/cagrid-grid-grouper/legacy-grid-grouper-host.jks", new File("src/test/resources/legacy-grid-grouper-host.jks")),
                 new KarafDistributionConfigurationFileReplacementOption(TRUSTSTORE, new File("src/test/resources/truststore.jks")),
 
                 // work around smx vs jre soap conflict
                 new KarafDistributionConfigurationFileExtendOption("etc/jre.properties", "jre-1.6", ",javax.xml.soap;version=\"1.3\""),
                 new KarafDistributionConfigurationFileExtendOption("etc/jre.properties", "jre-1.7", ",javax.xml.soap;version=\"1.3\"")
         };
         return CaGridTestSupport.concatAll(super.config(), options);
     }
 
     @Test
     public void testGrouper() throws Exception {
         try {
             //
             // DB must be initialized before we deploy the service
             //
             GrouperInit.main(new String[]{
                     "schema-export.sql",
                     "../../../src/test/resources/grouper.hibernate.properties",
                     "../../../src/test/resources/hibernate"});
 
             installAndAssertFeature("cagrid-gridgrouper", 30000L);
             System.err.println(executeCommand("features:list"));
             assertBundleInstalled("cagrid-gridgrouper-api");
             assertBundleInstalled("cagrid-gridgrouper-service");
             assertBundleInstalled("cagrid-gridgrouper-wsrf");
 
             GridGrouperService gridGrouperService = getOsgiService(GridGrouperService.class, 30000L);
             assertNotNull(gridGrouperService);
 
             // grab its metadata
             ServiceMetadata metadata = gridGrouperService.getServiceMetadata();
             Assert.assertNotNull(metadata);
             assertEquals("Service metadata name was not as expected.", "GridGrouper", metadata.getServiceDescription().getService().getName());
             ServiceSecurityMetadata securityMetadata = gridGrouperService.getServiceSecurityMetadata();
             Assert.assertNotNull(securityMetadata);
 
             // get soap client
             GridGrouperPortType gridGrouperSoapClient = getGridGrouperSoapClient();
             assertNotNull(gridGrouperSoapClient);
 
             // get stem
             StemDescriptor stem = getStem(gridGrouperSoapClient, "grouperadministration");
             assertNotNull(stem);
             assertEquals("grouperadministration", stem.getName());
             assertEquals("Grouper Administration", stem.getDisplayName());
             assertEquals("grouperadministration", stem.getExtension());
             assertEquals("Grouper Administration", stem.getDisplayExtension());
             assertEquals("GrouperSystem", stem.getCreateSubject());
             assertEquals("GrouperSystem", stem.getModifySubject());
 
 //            doNothing();
         } catch(Throwable t) {
             fail(ExceptionUtils.getFullStackTrace(t));
         }
     }
 
     private void doNothing() throws Exception {
         try {
             System.out.println("Sleeping......");
             Thread.sleep(1000l * 60l * 60l * 24l);//TWENTY_FOUR_HOURS_IN_MILLISECONDS);
         } catch (InterruptedException e) {
             System.out.println("sleep interrupted");
         }
     }
 
     private GridGrouperPortType getGridGrouperSoapClient() throws GeneralSecurityException, IOException {
         KeyStoreType truststore = new KeyStoreType();
         truststore.setFile(TRUSTSTORE);
         truststore.setType(TRUSTSTORETYPE);
         truststore.setPassword(TRUSTSTOREPASSWORD);
 
         X509Credential credential = CredentialFactory.getCredential(
                 HOST,
                 KEYSTOREPASSWORD,
                 KEYALIAS,
                 KEYPASSWORD);
 
         KeyManager keyManager = new SingleEntityKeyManager(KEYALIAS, credential);
 
         return GridGrouperSoapClientFactory.createSoapClient(GRIDGROUPER_URL, truststore, keyManager);
     }
 
     public StemDescriptor getStem(GridGrouperPortType gridGrouper, String name) throws StemNotFoundFaultFaultMessage, GridGrouperRuntimeFaultFaultMessage {
         StemIdentifier id = new StemIdentifier();
         id.setStemName(name);
 
         GetStemRequest.Stem value = new GetStemRequest.Stem();
         value.setStemIdentifier(id);
 
         GetStemRequest request = new GetStemRequest();
         request.setStem(value);
         return gridGrouper.getStem(request).getStemDescriptor();
     }
 }
