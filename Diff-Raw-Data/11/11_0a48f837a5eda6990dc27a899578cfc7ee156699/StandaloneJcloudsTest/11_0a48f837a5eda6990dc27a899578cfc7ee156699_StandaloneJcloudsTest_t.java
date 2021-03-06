 package brooklyn.location.basic.jclouds;
 
 import static org.testng.Assert.assertNotNull;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 
 import org.jclouds.Constants;
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ExecResponse;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.Template;
 import org.jclouds.compute.domain.TemplateBuilder;
 import org.jclouds.compute.options.RunScriptOptions;
 import org.jclouds.compute.options.TemplateOptions;
 import org.jclouds.domain.Credentials;
 import org.jclouds.domain.LoginCredentials;
 import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
 import org.jclouds.scriptbuilder.domain.Statement;
 import org.jclouds.scriptbuilder.domain.Statements;
 import org.jclouds.scriptbuilder.statements.login.AdminAccess;
 import org.jclouds.sshj.config.SshjSshClientModule;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import brooklyn.config.BrooklynProperties;
 import brooklyn.util.text.Identifiers;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Iterables;
 import com.google.common.io.Files;
 
 public class StandaloneJcloudsTest {
 
    // FIXME Why do this?
    // Were we seeing bugs in jclouds for which this was easier to debug and report
    // Is it because testProvisioningVmWithCustomUsername is disabled and not working?
    
     public static final Logger LOG = LoggerFactory.getLogger(StandaloneJcloudsTest.class);
     
     static BrooklynProperties globals = BrooklynProperties.Factory.newDefault();
 
     String identity = globals.getFirst("brooklyn.jclouds.aws-ec2.identity");
     String credential = globals.getFirst("brooklyn.jclouds.aws-ec2.credential");
     
     @Test(groups={"WIP","Live"})
     public void createVm() {
         String groupId = "mygroup-"+System.getProperty("user.name")+"-"+UUID.randomUUID().toString();
  
         Properties properties = new Properties();
         properties.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, Boolean.toString(true));
         properties.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, Boolean.toString(true));
         // handy to list all images... but very slow!
 //        properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY, "state=available;image-type=machine");
 
         ComputeServiceContext computeServiceContext = ContextBuilder.newBuilder("aws-ec2").
                 modules(Arrays.asList(new SshjSshClientModule(), new SLF4JLoggingModule())).
                 credentials(identity, credential).
                 overrides(properties).
                 build(ComputeServiceContext.class);
         
         final ComputeService computeService = computeServiceContext.getComputeService();
         
         NodeMetadata node = null;
         try {
             LOG.info("Creating VM for "+identity);
 
             TemplateBuilder templateBuilder = computeService.templateBuilder();
             templateBuilder.locationId("eu-west-1");
             
             Template template = templateBuilder.build();
             Set<? extends NodeMetadata> nodes = computeService.createNodesInGroup(groupId, 1, template);
             node = Iterables.getOnlyElement(nodes, null);
             if (node == null) throw new IllegalStateException("No nodes returned");
 
             assertNotNull(node.getOperatingSystem());
 
             Credentials nodeCredentials = node.getCredentials();
             final LoginCredentials expectedCredentials = LoginCredentials.fromCredentials(nodeCredentials);
             
             LOG.info("Started VM, waiting for it to be sshable");
             boolean reachable = false;
             for (int i=0; i<120; i++) {
                 try {
                     Statement statement = Statements.newStatementList(Statements.exec("date"));
                     ExecResponse response = computeService.runScriptOnNode(node.getId(), statement,
                             RunScriptOptions.Builder.overrideLoginCredentials(expectedCredentials));
                     if (response.getExitStatus() == 0) {
                         LOG.info("ssh 'date' succeeded");
                         reachable = true;
                         break;
                     }
                     LOG.info("ssh 'date' failed, exit "+response.getExitStatus()+", but still in retry loop");
                 } catch (Exception e) {
                     if (i<120)
                         LOG.info("ssh 'date' failed, but still in retry loop: "+e);
                     else {
                         LOG.error("ssh 'date' failed after timeout: "+e, e);
                         Throwables.propagate(e);
                     }
                 }
                 Thread.sleep(1000);
             }
         
             if (!reachable) {
                 throw new IllegalStateException("SSH failed, never reachable");
             }
 
         } catch (RunNodesException e) {
             if (e.getNodeErrors().size() > 0) {
                 node = Iterables.get(e.getNodeErrors().keySet(), 0);
             }
             LOG.error("Failed to start VM: "+e, e);
             throw Throwables.propagate(e);
         } catch (Exception e) {
             LOG.error("Failed to start VM: "+e, e);
             throw Throwables.propagate(e);
         } finally {
             LOG.info("Now destroying VM: "+node);
             computeService.destroyNode( node.getId() );
 
             computeService.getContext().close();
         }
         
     }
     
     @Test(groups={"WIP","Live"})
     public void createVmWithAdminUser() {
         String groupId = "mygroup-"+System.getProperty("user.name")+"-"+UUID.randomUUID().toString();
  
         Properties properties = new Properties();
         properties.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, Boolean.toString(true));
         properties.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, Boolean.toString(true));
 
         ComputeServiceContext computeServiceContext = ContextBuilder.newBuilder("aws-ec2").
                 modules(Arrays.asList(new SshjSshClientModule(), new SLF4JLoggingModule())).
                 credentials(identity, credential).
                 overrides(properties).
                 build(ComputeServiceContext.class);
         
         final ComputeService computeService = computeServiceContext.getComputeService();
         
         NodeMetadata node = null;
         try {
             LOG.info("Creating VM for "+identity);
             String myPubKey = Files.toString(new File(System.getProperty("user.home")+"/.ssh/aws-id_rsa.pub"), Charset.defaultCharset());
             String myPrivKey = Files.toString(new File(System.getProperty("user.home")+"/.ssh/aws-id_rsa"), Charset.defaultCharset());
 
             TemplateBuilder templateBuilder = computeService.templateBuilder();
             templateBuilder.locationId("us-east-1");
             TemplateOptions opts = new TemplateOptions();
             
 //            templateBuilder.imageId("us-east-1/ami-2342a94a");  //rightscale
             // either use above, or below
             templateBuilder.imageId("us-east-1/ami-f95cf390");  //private one (to test when user isn't autodetected)
             opts.overrideLoginUser("ec2-user");
             
             AdminAccess.Builder adminBuilder = AdminAccess.builder().
                     adminUsername("bob").
                     grantSudoToAdminUser(true).
                     authorizeAdminPublicKey(true).adminPublicKey(myPubKey).
                     // items below aren't wanted but values for some are required otherwise AdminAccess uses all defaults
                     lockSsh(true).adminPassword(Identifiers.makeRandomId(12)).
                     resetLoginPassword(false).loginPassword(Identifiers.makeRandomId(12)).
                     installAdminPrivateKey(false).adminPrivateKey("ignored");
             opts.runScript(adminBuilder.build());
             
             templateBuilder.options(opts);
             
             Template template = templateBuilder.build();
             Set<? extends NodeMetadata> nodes = computeService.createNodesInGroup(groupId, 1, template);
             node = Iterables.getOnlyElement(nodes, null);
             if (node == null) throw new IllegalStateException("No nodes returned");
 
             LOG.info("Started VM, waiting for it to be sshable on "+node.getPublicAddresses());
             final LoginCredentials crds =
 //                    node.getCredentials();
                     LoginCredentials.builder().user("bob").privateKey(myPrivKey).build();
             boolean reachable = false;
             for (int i=0; i<120; i++) {
                 try {
                     Statement statement = Statements.newStatementList(Statements.exec("date"));
                     ExecResponse response = computeService.runScriptOnNode(node.getId(), statement,
                             RunScriptOptions.Builder.overrideLoginCredentials(crds));
                     if (response.getExitStatus() == 0) {
                         LOG.info("ssh 'date' succeeded");
                         reachable = true;
                         break;
                     }
                     LOG.info("ssh 'date' failed, exit "+response.getExitStatus()+", but still in retry loop");
                 } catch (Exception e) {
                     if (i<120)
                         LOG.info("ssh 'date' failed, but still in retry loop: "+e);
                     else {
                         LOG.error("ssh 'date' failed after timeout: "+e, e); 
                         Throwables.propagate(e);
                     }
                 }
                 Thread.sleep(1000);
             }
         
             if (!reachable) {
                 throw new IllegalStateException("SSH failed, never reachable");
             }
             
         } catch (RunNodesException e) {
             if (e.getNodeErrors().size() > 0) {
                 node = Iterables.get(e.getNodeErrors().keySet(), 0);
             }
             LOG.error("Failed to start VM: "+e, e);
             throw Throwables.propagate(e);
         } catch (Exception e) {
             LOG.error("Failed to start VM: "+e, e);
             throw Throwables.propagate(e);
         } finally {
             LOG.info("Now destroying VM: "+node);
             computeService.destroyNode( node.getId() );
 
             computeService.getContext().close();
         }
         
     }
 
 }
