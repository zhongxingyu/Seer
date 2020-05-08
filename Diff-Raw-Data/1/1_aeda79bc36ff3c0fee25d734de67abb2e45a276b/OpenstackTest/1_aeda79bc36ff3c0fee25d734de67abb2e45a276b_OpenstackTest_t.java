 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import junit.framework.Assert;
 
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackNetworkClient;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackException;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackJsonSerializationException;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.SecurityGroup;
 import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.hpgrizzly.HpGrizzlyCloudService;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class OpenstackTest extends NewAbstractCloudTest {
 
     private static final String IP_REGEX = "([1-9][0-9]{0,2}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})";
     private HpGrizzlyCloudService service;
 
     @Override
     protected String getCloudName() {
         return "hp-grizzly";
     }
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }
 
     @Override
     @BeforeClass(alwaysRun = true)
     protected void bootstrap() throws Exception {
         service = new HpGrizzlyCloudService();
         super.bootstrap(service, null);
     }
 
     @Override
     @AfterClass(alwaysRun = true)
     protected void teardown() throws Exception {
         super.teardown();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testInstallSampleSecurityGroupService() throws Exception {
 
         final String relativePath = "src/main/resources/apps/USM/usm/networks/openstack/secgroups";
         final String servicePath = CommandTestUtils.getPath(relativePath);
 
         final String restUrl = getRestUrl();
 
         final ServiceInstaller installer = new ServiceInstaller(restUrl, "securityGroups", "hp-grizzly");
         installer.recipePath(servicePath);
         installer.waitForFinish(true);
         installer.install();
 
         // Assert that the service is installed
         String command = "connect " + restUrl + ";list-services";
         String output = CommandTestUtils.runCommandAndWait(command);
         final Service service = ServiceReader.getServiceFromFile(new File(relativePath, "securityGroup-service.groovy"));
         assertTrue("the service " + service.getName() + " is not running", output.contains(service.getName()));
 
         this.assertSecurityGroups();
 
         installer.uninstall();
     }
 
     private void assertSecurityGroups() throws OpenstackException {
 
         final OpenStackNetworkClient quantum = this.createQuantumClient();
 
         final List<SecurityGroup> securityGroups = quantum.getSecurityGroupsByPrefix(this.service.getMachinePrefix());
         assertTrue("No security groups found", !securityGroups.isEmpty());
         // Expect 5 secgroups: management, agent, cluster, application, service.
         // There can be more secgroups if other test has failed to uninstall, in that case there can be remaining secgroup of other service/application.
         assertTrue("Expected at least 5 security groups, got " + securityGroups.size(), securityGroups.size() >= 5);
 
         final SecurityGroup management = this.retrieveSecgroup(securityGroups, "management");
         assertNotNull("No management security group found", management);
         assertNotNull("No rules found in management security group", management.getSecurityGroupRules());
         // > 2 because there is 2 default egress rules
         assertTrue("There should be rules in management security group", management.getSecurityGroupRules().length > 2);
 
         final SecurityGroup agent = this.retrieveSecgroup(securityGroups, "agent");
         assertNotNull("No agent security group found", agent);
         assertNotNull("No rules found in agent security group", agent.getSecurityGroupRules());
         // > 2 because there is 2 default egress rules
         assertTrue("There should be rules in agent security group", agent.getSecurityGroupRules().length > 2);
 
         final SecurityGroup cluster = this.retrieveSecgroup(securityGroups, "cluster");
         assertNotNull("No cluster security group found", cluster);
         assertNotNull("No rules found in cluster security group", cluster.getSecurityGroupRules());
         // There is 2 egress rules ipv4 and ipv6
         assertTrue("There should be 2 rules in cluster security group (got " + cluster.getSecurityGroupRules().length + ")"
                 , cluster.getSecurityGroupRules() == null || cluster.getSecurityGroupRules().length == 2);
 
         final SecurityGroup appli = this.retrieveSecgroup(securityGroups, "default");
         assertNotNull("No application security group found", appli);
         assertNotNull("No rules found in application security group", appli.getSecurityGroupRules());
         // There is 2 egress rules ipv4 and ipv6
         assertEquals("There should 2 rules in application security group (got " + appli.getSecurityGroupRules().length + ")"
                 , 2, appli.getSecurityGroupRules().length);
 
         final SecurityGroup serviceSecgroup = this.retrieveSecgroup(securityGroups, "securityGroups");
         assertNotNull("No service security group found", serviceSecgroup);
         assertNotNull("No rules found in service security group", serviceSecgroup.getSecurityGroupRules());
         // There is 2 egress rules, port 22 for the management network and 6 others declared in the service DSL
         assertEquals("There should 9 rules in application security group (got " + serviceSecgroup.getSecurityGroupRules().length + ")"
                 , 9, serviceSecgroup.getSecurityGroupRules().length);
     }
 
     private SecurityGroup retrieveSecgroup(List<SecurityGroup> securityGroups, String suffix) {
         for (SecurityGroup securityGroup : securityGroups) {
             if (securityGroup.getName().endsWith(suffix)) {
                 return securityGroup;
             }
         }
         return null;
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testFloatingIp() throws Exception {
         String serviceName = "floatingips";
 
         String relativePath = "src/main/resources/apps/USM/usm/networks/openstack/floatingips";
         String servicePath = CommandTestUtils.getPath(relativePath);
         this.installServiceAndWait(servicePath, serviceName);
 
         // Assert that the service is installed
         String command = "connect " + getRestUrl() + ";list-services";
         String output = CommandTestUtils.runCommandAndWait(command);
         final Service service = ServiceReader.getServiceFromFile(new File(relativePath, "floatingips-service.groovy"));
         assertTrue("the service " + service.getName() + " is not running", output.contains(service.getName()));
 
         // Get the public ip
         output = this.invoke(serviceName, "getPublicAddress");
         Pattern compile = Pattern.compile(".*Result: " + IP_REGEX);
         Matcher matcher = compile.matcher(output);
         assertTrue("Cannot retrieve public ip address", matcher.find());
         assertNotNull("Could not parse public ip address", matcher.group(1));
         String publicAddress = matcher.group(1);
         assertTrue("Port on " + publicAddress + ":22 should be occupied ", ServiceUtils.isPortOccupied(publicAddress, 22));
 
         // Assert unassignment and release of a floating ip
         output = this.invoke(serviceName, "releaseFloatingIp");
         sleep(20000L);
         assertTrue("Port on " + publicAddress + ":22 should be free ", ServiceUtils.isPortFree(publicAddress, 22));
 
         // Assert allocate and assignment of a floating ip
         output = this.invoke(serviceName, "assignFloatingIp");
         compile = Pattern.compile(".*Result: " + IP_REGEX);
         matcher = compile.matcher(output);
         Assert.assertTrue("Cannot retrieve the floating ip", matcher.find());
         String newFloatingIp = matcher.group(1);
         assertNotNull("Could not parse the floating ip", newFloatingIp);
         sleep(10000L);
         assertTrue("Port on " + newFloatingIp + ":22 should be occupied ", ServiceUtils.isPortOccupied(newFloatingIp, 22));
 
         // Assert application NIC existence
         output = this.invoke(serviceName, "getApplicationNetworkIp");
         compile = Pattern.compile(".*Result: " + IP_REGEX);
         matcher = compile.matcher(output);
         assertTrue("Cannot retrieve application ip address", matcher.find());
         assertNotNull("Could not parse application ip address", matcher.group(1));
         this.uninstallServiceAndWait(serviceName);
     }
 
     private String invoke(String serviceName, String customCommand) throws IOException, InterruptedException {
         String command = String.format("connect %s; invoke %s %s", getRestUrl(), serviceName, customCommand);
         String output = CommandTestUtils.runCommandAndWait(command);
         return output;
 
     }
 
     private OpenStackNetworkClient createQuantumClient() throws OpenstackJsonSerializationException {
         final String imageId = this.getCloudProperty(HpGrizzlyCloudService.IMAGE_PROP);
         final String region = imageId.split("/")[0];
         final OpenStackNetworkClient client = new OpenStackNetworkClient(
                 this.getCloudProperty(HpGrizzlyCloudService.ENDPOINT_PROP),
                 this.getCloudProperty(HpGrizzlyCloudService.USER_PROP),
                 this.getCloudProperty(HpGrizzlyCloudService.API_KEY_PROP),
                 this.getCloudProperty(HpGrizzlyCloudService.TENANT_PROP),
                 region);
         return client;
     }
 
     public String getCloudProperty(String key) {
         return service.getCloudProperty(key);
     }
 
 }
