 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack;
 
 import static org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.OpenstackCloudifyDriverLauncher.COMPUTE_SOME_INTERNAL_NETWORK_1;
 import static org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.OpenstackCloudifyDriverLauncher.COMPUTE_SOME_INTERNAL_NETWORK_2;
 import static org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.OpenstackCloudifyDriverLauncher.TEMPLATE_APPLICATION_NET;
 import static org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.OpenstackCloudifyDriverLauncher.TEMPLATE_APPLICATION_NET2;
 import iTests.framework.utils.AssertUtils;
 
 import org.cloudifysource.domain.ComputeDetails;
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.domain.cloud.Cloud;
 import org.cloudifysource.domain.cloud.network.NetworkConfiguration;
 import org.cloudifysource.esc.driver.provisioning.CloudProvisioningException;
 import org.cloudifysource.esc.driver.provisioning.MachineDetails;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackResourcePrefixes;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.junit.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * To run this test, you will have to create the credential file here : ./src/main/resources/credentials/cloud/openstack/openstack.properties.<br />
  * An property example:
  * 
  * <pre>
  * user=_your_user_
  * tenant=_your_tenant_
  * apiKey=_your_apiKey_
  * keyPair=_your_keyPair_
  * 
  * endpoint=http://_openstack_endpoint_address_:5000/v2.0/
  * 
  * hardwareId=RegionOne/2
  * imageId=RegionOne/8b62f8c3-1275-47c3-a884-a295167d109a
  * </pre>
  * 
  * @author victor
  * 
  */
 public class OpenstackCloudifyDriverTest extends AbstractTestSupport {
 
     private OpenstackCloudifyDriverLauncher launcher;
     private Cloud cloud;
 
     @BeforeMethod
     public void init() throws Exception {
         launcher = new OpenstackCloudifyDriverLauncher();
     }
 
     @AfterMethod
     public void clean() throws Exception {
         launcher.cleanOpenstackResources(cloud);
     }
 
     // ***************************************************************
     // ******** startManagementMachines tests
     // ***************************************************************
 
     /**
      * <p>
      * Start management machine only.
      * </p>
      * <ul>
      * <li>Use network template.</li>
      * </ul>
      * 
      */
     @Test
     public void testStartManagementMachinesWithNetworkTemplate() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("range \"177.86.0.0/24\"", "range \"177.70.0.0/24\"");
         additionalProps.put("\"gateway\" : \"177.86.0.111\"", "\"gateway\" : \"177.70.0.1\"");
         additionalProps.put("range \"160.0.0.0/24\"", "range \"160.1.0.0/24\"");
 
         cloud = launcher.createCloud(null, additionalProps);
 
         MachineDetails[] mds = launcher.startManagementMachines(cloud);
 
         String prefix = cloud.getProvider().getManagementGroup();
         NetworkConfiguration networkConfiguration = cloud.getCloudNetwork().getManagement().getNetworkConfiguration();
 
         launcher.assertRouterExistsByFullName(launcher.getManagementRouterNameFromDsl(cloud));
         launcher.assertNetworkExists(prefix + networkConfiguration.getName());
         launcher.assertSubnetExists(prefix + networkConfiguration.getName(), networkConfiguration.getSubnets().get(0).getName());
         launcher.assertSubnetSize(prefix + networkConfiguration.getName(), 1);
 
         for (MachineDetails md : mds) {
             assertNotNull("Machine id is null", md.getMachineId());
             assertNotNull("Private ip is null", md.getPrivateAddress());
             assertTrue("Private ip is not from subnet 177.70.0.0/24", md.getPrivateAddress().startsWith("177.70.0."));
             assertNotNull("Public ip is null", md.getPublicAddress());
             launcher.assertFloatingIpBindToServer(md);
             launcher.assertVMBoundToNetwork(md.getMachineId(), prefix + networkConfiguration.getName());
         }
 
         launcher.stopManagementMachines(cloud);
     }
 
     /**
      * <p>
      * Start management machine only.
      * </p>
      * <ul>
      * <li>Use network template with multiple subnets.</li>
      * </ul>
      *
      * Disabled. There is a limitation on starting 2 subnets on one network.
      */
     @Test(enabled = false)
     public void testStartManagementMachinesWithNetworkTemplateAndMultipleSubnets() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("subnet \\{\r\n" +
                 "\t\t\t\t\t\tname \"Cloudify-Management-Subnet\"\r\n" +
                 "\t\t\t\t\t\trange \"177.86.0.0/24\"\r\n" +
                 "\t\t\t\t\t\toptions \\(\\[ \"gateway\" : \"177.86.0.111\" ]\\)\r\n" +
                 "\t\t\t\t\t}", "subnet {\n" +
                 "                        name \"Cloudify-Management-Subnet1\"\n" +
                 "                        range \"177.70.0.0/24\"\n" +
                 "                        options ([ \"gateway\" : \"177.70.0.1\" ])\n" +
                 "                    },\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Management-Subnet2\"\n" +
                 "                        range \"177.80.0.0/24\"\n" +
                 "                        options ([ \"gateway\" : \"null\" ])\n" +
                 "                    }");
         additionalProps.put("\"APPLICATION_NET\" : networkConfiguration \\{\r\n" +
                 "\t\t\t\tname  \"Cloudify-Application-Network\"\r\n" +
                 "\t\t\t\tsubnets \\{\r\n" +
                 "\t\t\t\t\tsubnet \\{\r\n" +
                 "\t\t\t\t\t\tname \"Cloudify-Application-Subnet\"\r\n" +
                 "\t\t\t\t\t\trange \"160.0.0.0/24\"\r\n" +
                 "\t\t\t\t\t\toptions \\{ gateway \"null\" \\}\r\n" +
                 "\t\t\t\t\t\\}\r\n" +
                 "\t\t\t\t\\}\r\n" +
                 "\t\t\t\tcustom \\(\\[ \"associateFloatingIpOnBootstrap\" : \"true\" ]\\)\r\n" +
                 "\t\t\t\\}", "\"APPLICATION_NET\" : networkConfiguration {\n" +
                 "                name \"Cloudify-Application-Network\"\n" +
                 "                subnets {\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet1\"\n" +
                 "                        range \"160.1.0.0/24\"\n" +
                 "                    }\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet2\"\n" +
                 "                        range \"160.2.0.0/24\"\n" +
                 "                    }\n" +
                 "                }\n" +
                 "                custom ([ \"associateFloatingIpOnBootstrap\" : \"true\" ])\n" +
                 "            },\n" +
                 "            \"APPLICATION_NET2\" : networkConfiguration {\n" +
                 "                name \"Cloudify-Application-Network2\"\n" +
                 "                subnets {\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet1\"\n" +
                 "                        range \"162.1.0.0/24\"\n" +
                 "                    }\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet2\"\n" +
                 "                        range \"162.2.0.0/24\"\n" +
                 "                    }\n" +
                 "                }\n" +
                 "                custom ([ \"associateFloatingIpOnBootstrap\" : \"true\" ])\n" +
                 "            }");
 
         cloud = launcher.createCloud(null, additionalProps);
 
         MachineDetails[] mds = launcher.startManagementMachines(cloud);
 
         String prefix = cloud.getProvider().getManagementGroup();
 
         for (MachineDetails md : mds) {
             assertNotNull("Machine id is null", md.getMachineId());
             assertNotNull("Private ip is null", md.getPrivateAddress());
             assertTrue("Private ip is not from subnet 177.70.0.0/24", md.getPrivateAddress().startsWith("177.70.0."));
             assertNotNull("Public ip is null", md.getPublicAddress());
             launcher.assertFloatingIpBindToServer(md);
 
             NetworkConfiguration networkConfiguration = cloud.getCloudNetwork().getManagement().getNetworkConfiguration();
             launcher.assertRouterExistsByFullName(launcher.getManagementRouterNameFromDsl(cloud));
             launcher.assertNetworkExists(prefix + networkConfiguration.getName());
             launcher.assertSubnetExists(prefix + networkConfiguration.getName(), networkConfiguration.getSubnets().get(0).getName());
             launcher.assertSubnetExists(prefix + networkConfiguration.getName(), networkConfiguration.getSubnets().get(1).getName());
             launcher.assertSubnetSize(prefix + networkConfiguration.getName(), 2);
             launcher.assertVMBoundToNetwork(md.getMachineId(), prefix + networkConfiguration.getName());
         }
 
         launcher.stopManagementMachines(cloud);
     }
 
     /**
      * <p>
      * Start management machine only.
      * </p>
      * <ul>
      * <li>Use computeNetworks.</li>
      * </ul>
      * <ul>
      * <li>The test creates 2 networks and uses the computeNetwork closure to connect the management machine to them.</li>
      * </ul>
      *
      *
      */
     @Test(enabled = true)
     public void testStartManagementMachinesWithComputeNetwork() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("// Optional. Use existing networks.", "computeNetwork {\n" +
                 "\t\t\t\t\tnetworks ([\"SOME_INTERNAL_NETWORK_1\",\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "\t\t\t\t }\n // Optional. Use existing networks.");
        additionalProps.put("keyFile keyFile", "keyFile keyFile\n" +
                "                fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP");
         cloud = launcher.createCloud(null, additionalProps);
 
         try {
             launcher.createExpectedExistingNetworks();
 
             MachineDetails[] mds = launcher.startManagementMachines(cloud);
             for (MachineDetails md : mds) {
                 assertNotNull("Machine id is null", md.getMachineId());
                 assertNotNull("Private ip is null", md.getPrivateAddress());
                 assertTrue("Private ip is not from subnet 177.86.0.0/24", md.getPrivateAddress().startsWith("177.86.0."));
                 launcher.assertAssignedFloatingIp(md.getMachineId());
                 launcher.assertVMBoundToNetwork(md.getMachineId(), COMPUTE_SOME_INTERNAL_NETWORK_1);
                 launcher.assertVMBoundToNetwork(md.getMachineId(), COMPUTE_SOME_INTERNAL_NETWORK_2);
             }
             launcher.stopManagementMachines(cloud);
         } finally {
             launcher.cleanExpectedExistingNetworks();
         }
     }
 
     /**
      * <p>
      * Start multiple management machines.
      * </p>
      * <ul>
      * <li>Use network templates</li>
      * </ul>
      * <ul>
      * <li>The test creates 2 networks and uses the computeNetwork closure to connect multiple management machines to them.</li>
      * </ul>
      */
     @Test
     public void testStartMultipleManagementMachinesWithComputeNetwork() throws Exception {
         int nbManagementMachines = 3;
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("// Optional. Use existing networks.", "computeNetwork {\n" +
                 "\t\t\t\t\tnetworks ([\"SOME_INTERNAL_NETWORK_1\",\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "\t\t\t\t }\n // Optional. Use existing networks.");
        additionalProps.put("keyFile keyFile", "keyFile keyFile\n" +
                "                fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP");
         cloud = launcher.createCloud(null, additionalProps);
 
         cloud.getProvider().setNumberOfManagementMachines(nbManagementMachines);
         try {
             launcher.createExpectedExistingNetworks();
             MachineDetails[] mds = launcher.startManagementMachines(cloud);
             for (MachineDetails md : mds) {
                 assertNotNull("Machine id is null", md.getMachineId());
                 assertNotNull("Private ip is null", md.getPrivateAddress());
                 assertTrue("Private ip is not from subnet 177.86.0.0/24", md.getPrivateAddress().startsWith("177.86.0."));
                 launcher.assertAssignedFloatingIp(md.getMachineId());
                 launcher.assertVMBoundToNetwork(md.getMachineId(), COMPUTE_SOME_INTERNAL_NETWORK_1);
                 launcher.assertVMBoundToNetwork(md.getMachineId(), COMPUTE_SOME_INTERNAL_NETWORK_2);
             }
             launcher.stopManagementMachines(cloud);
         } finally {
             launcher.cleanExpectedExistingNetworks();
         }
     }
 
     /**
      * <p>
      * Start management machine only.
      * </p>
      * <ul>
      * <li>Use computeNetworks but networks does not exists.</li>
      * </ul>
      * <ul>
      * <li>The test tries to start a management machine which is defined to connect to networks that does not exist.
      * machine start expected to fail.</li>
      * </ul>
      */
     @Test(expectedExceptions = CloudProvisioningException.class)
     public void testStartManagementWithComputeTemplateButNetworksDoNotExist() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
         additionalProps.put("// Optional. Use existing networks.", "computeNetwork {\n" +
                 "\t\t\t\t\tnetworks ([\"SOME_INTERNAL_NETWORK_1\",\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "\t\t\t\t }\n // Optional. Use existing networks.");
        additionalProps.put("keyFile keyFile", "keyFile keyFile\n" +
                "                fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP");
         cloud = launcher.createCloud(null, additionalProps);
         launcher.startManagementMachines(cloud);
     }
 
     // ***************************************************************
     // ******** startMachine Tests
     // ***************************************************************
 
     /**
      * <ul>
      * <li>Start management machine.</li>
      * <li>Start agent.</li>
      * <li>Use network template.</li>
      * <ul>
      * <li>This test starts a management machine and installs a service with network access rules.</li>
      * </ul>
      * </ul>
      */
     @Test
     public void testStartMachineWithNetworkTemplate() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("range \"177.86.0.0/24\"", "range \"177.70.0.0/24\"");
         additionalProps.put("\"gateway\" : \"177.86.0.111\"", "\"gateway\" : \"177.70.0.1\"");
         additionalProps.put("range \"160.0.0.0/24\"", "range \"160.1.0.0/24\"");
         cloud = launcher.createCloud(null, additionalProps);
 
         Service service = launcher.getService("secgroups/securityGroup-service.groovy");
         MachineDetails md = launcher.startMachineWithManagement(service, cloud);
 
         assertNotNull("Machine id is null", md.getMachineId());
         assertNotNull("Private ip is null", md.getPrivateAddress());
         assertTrue("Private ip is not from subnet 177.70.0.0/24. Got " + md.getPrivateAddress(), md.getPrivateAddress().startsWith("177.70.0."));
         assertNotNull("Public ip is null", md.getPublicAddress());
         launcher.assertFloatingIpBindToServer(md);
 
         String prefix = cloud.getProvider().getManagementGroup();
         OpenStackResourcePrefixes prefixes = new OpenStackResourcePrefixes(prefix, OpenstackCloudifyDriverLauncher.DEFAULT_APPLICATION_NAME, service.getName());
         launcher.assertSecurityGroupIncomingRulesNumber(prefixes.getServiceName(), service.getNetwork().getAccessRules().getIncoming().size());
 
         NetworkConfiguration networkConfiguration = cloud.getCloudNetwork().getTemplates().get(TEMPLATE_APPLICATION_NET);
         String appliNetworkName = prefixes.getApplicationName() + "-" + networkConfiguration.getName();
         launcher.assertNetworkExists(appliNetworkName);
         launcher.assertSubnetExists(appliNetworkName, networkConfiguration.getSubnets().get(0).getName());
         launcher.assertSubnetSize(appliNetworkName, 1);
 
         launcher.stopMachineWithManagement(service, cloud, md.getPrivateAddress());
     }
 
     /**
      * <ul>
      * <li>Start management machine.</li>
      * <li>Start agent.</li>
      * <li>Use network template with multiple subnets.</li>
      * </ul>
      *
      * Disabled. There is a limitation on starting 2 subnets on one network.
      */
     @Test(enabled = false)
     public void testStartMachineWithMultipleSubnets() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("subnet \\{\r\n" +
                 "\t\t\t\t\t\tname \"Cloudify-Management-Subnet\"\r\n" +
                 "\t\t\t\t\t\trange \"177.86.0.0/24\"\r\n" +
                 "\t\t\t\t\t\toptions \\(\\[ \"gateway\" : \"177.86.0.111\" ]\\)\r\n" +
                 "\t\t\t\t\t}", "subnet {\n" +
                 "                        name \"Cloudify-Management-Subnet1\"\n" +
                 "                        range \"177.70.0.0/24\"\n" +
                 "                        options ([ \"gateway\" : \"177.70.0.1\" ])\n" +
                 "                    },\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Management-Subnet2\"\n" +
                 "                        range \"177.80.0.0/24\"\n" +
                 "                        options ([ \"gateway\" : \"null\" ])\n" +
                 "                    }");
         additionalProps.put("\"APPLICATION_NET\" : networkConfiguration \\{\r\n" +
                 "\t\t\t\tname  \"Cloudify-Application-Network\"\r\n" +
                 "\t\t\t\tsubnets \\{\r\n" +
                 "\t\t\t\t\tsubnet \\{\r\n" +
                 "\t\t\t\t\t\tname \"Cloudify-Application-Subnet\"\r\n" +
                 "\t\t\t\t\t\trange \"160.0.0.0/24\"\r\n" +
                 "\t\t\t\t\t\toptions \\{ gateway \"null\" \\}\r\n" +
                 "\t\t\t\t\t\\}\r\n" +
                 "\t\t\t\t\\}\r\n" +
                 "\t\t\t\tcustom \\(\\[ \"associateFloatingIpOnBootstrap\" : \"true\" ]\\)\r\n" +
                 "\t\t\t\\}", "\"APPLICATION_NET\" : networkConfiguration {\n" +
                 "                name \"Cloudify-Application-Network\"\n" +
                 "                subnets {\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet1\"\n" +
                 "                        range \"160.1.0.0/24\"\n" +
                 "                    }\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet2\"\n" +
                 "                        range \"160.2.0.0/24\"\n" +
                 "                    }\n" +
                 "                }\n" +
                 "                custom ([ \"associateFloatingIpOnBootstrap\" : \"true\" ])\n" +
                 "            },\n" +
                 "            \"APPLICATION_NET2\" : networkConfiguration {\n" +
                 "                name \"Cloudify-Application-Network2\"\n" +
                 "                subnets {\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet1\"\n" +
                 "                        range \"162.1.0.0/24\"\n" +
                 "                    }\n" +
                 "                    subnet {\n" +
                 "                        name \"Cloudify-Application-Subnet2\"\n" +
                 "                        range \"162.2.0.0/24\"\n" +
                 "                    }\n" +
                 "                }\n" +
                 "                custom ([ \"associateFloatingIpOnBootstrap\" : \"true\" ])\n" +
                 "            }");
 
         cloud = launcher.createCloud(null, additionalProps);
         Service service = launcher.getService("secgroups/securityGroup-service.groovy");
         service.getNetwork().setTemplate(TEMPLATE_APPLICATION_NET2);
         MachineDetails md = launcher.startMachineWithManagement(service, cloud);
 
         assertNotNull("Machine id is null", md.getMachineId());
         assertNotNull("Private ip is null", md.getPrivateAddress());
         assertTrue("Private ip is not from subnet 177.70.0.0/24. Got " + md.getPrivateAddress(), md.getPrivateAddress().startsWith("177.70.0."));
         assertNotNull("Public ip is null", md.getPublicAddress());
         launcher.assertFloatingIpBindToServer(md);
 
         String prefix = cloud.getProvider().getManagementGroup();
         OpenStackResourcePrefixes prefixes = new OpenStackResourcePrefixes(prefix, OpenstackCloudifyDriverLauncher.DEFAULT_APPLICATION_NAME, service.getName());
         launcher.assertSecurityGroupIncomingRulesNumber(prefixes.getServiceName(), service.getNetwork().getAccessRules().getIncoming().size());
 
         NetworkConfiguration mngNetConfig = cloud.getCloudNetwork().getManagement().getNetworkConfiguration();
         NetworkConfiguration netConfig = cloud.getCloudNetwork().getTemplates().get(TEMPLATE_APPLICATION_NET2);
         String appliNetworkName = prefixes.getApplicationName() + "-" + netConfig.getName();
         launcher.assertNetworkExists(appliNetworkName);
         launcher.assertSubnetExists(appliNetworkName, netConfig.getSubnets().get(0).getName());
         launcher.assertSubnetExists(appliNetworkName, netConfig.getSubnets().get(1).getName());
         launcher.assertSubnetSize(appliNetworkName, 2);
         launcher.assertVMBoundToNetwork(md.getMachineId(), prefix + mngNetConfig.getName());
         launcher.assertVMBoundToNetwork(md.getMachineId(), appliNetworkName);
 
         launcher.stopMachineWithManagement(service, cloud, md.getPrivateAddress());
     }
 
     /**
      * <ul>
      * <li>Start management machine.</li>
      * <li>Start agent.</li>
      * <li>Use management network</li>
      * <li>Use computeNetwork for application network.</li>
      * </ul>
      */
     @Test
     public void testStartMachineWithComputeNetworkUsingManagerNetwork() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("managementMachineTemplate \"MEDIUM_LINUX\"", "managementMachineTemplate \"MANAGER\"");
         additionalProps.put("MEDIUM_LINUX : computeTemplate", "MANAGER : computeTemplate");
        additionalProps.put("keyFile keyFile", "keyFile keyFile\n" +
                "                fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP");
         additionalProps.put("// Optional. Use existing networks.", "computeNetwork {\n" +
                 "\t\t\t\t\tnetworks ([\"SOME_INTERNAL_NETWORK_1\",\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "\t\t\t\t }\n // Optional. Use existing networks.");
         additionalProps.put("custom \\(\\[\"openstack.compute.zone\":availabilityZone]\\)", "custom ([\"openstack.compute.zone\":availabilityZone])\n" +
                 "\t\t\t\\},\n" +
                 "            APPLI : computeTemplate{\n" +
                 "                imageId imageId\n" +
                 "                remoteDirectory \"/root/gs-files\"\n" +
                 "                machineMemoryMB 1600\n" +
                 "                hardwareId hardwareId\n" +
                 "                localDirectory \"upload\"\n" +
                 "                keyFile keyFile\n" +
                 "                username \"ubuntu\"\n" +
                 "                options ([\n" +
                 "                    \"keyPairName\" : keyPair\n" +
                 "                ])\n" +
                 "                overrides ([\n" +
                 "                    \"openstack.endpoint\": openstackUrl\n" +
                 "                ])\n" +
                 "                computeNetwork {\n" +
                 "                    networks ([\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "                }");
 
         cloud = launcher.createCloud(null, additionalProps);
         Service service = launcher.getService("secgroups/securityGroup-service.groovy");
         ComputeDetails computeDetails = new ComputeDetails();
         computeDetails.setTemplate("APPLI");
         service.setCompute(computeDetails);
         service.getNetwork().setTemplate(null);
 
         try {
             launcher.createExpectedExistingNetworks();
 
             MachineDetails md = launcher.startMachineWithManagement(service, cloud);
             assertNotNull("Machine id is null", md.getMachineId());
             assertNotNull("Private ip is null", md.getPrivateAddress());
             // Private IP network should be "SOME_INTERNAL_NETWORK_2"
             assertTrue("Private ip is not from subnet 177.86.0.114/24. Got " + md.getPrivateAddress(), md.getPrivateAddress().startsWith("177.86.0."));
             AssertUtils.assertNull("Public ip should be null", md.getPublicAddress());
             launcher.assertNoFloatingIp(md.getMachineId());
 
             String prefix = cloud.getProvider().getManagementGroup();
             OpenStackResourcePrefixes prefixes = new OpenStackResourcePrefixes(prefix, OpenstackCloudifyDriverLauncher.DEFAULT_APPLICATION_NAME,
                     service.getName());
             launcher.assertSecurityGroupIncomingRulesNumber(prefixes.getServiceName(), service.getNetwork().getAccessRules().getIncoming().size());
             launcher.assertNoRouter(prefix);
 
             launcher.stopMachineWithManagement(service, cloud, md.getPrivateAddress());
         } finally {
             launcher.cleanExpectedExistingNetworks();
         }
     }
 
     @Test
     public void testStartMachineWithComputeNetwork() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("managementMachineTemplate \"MEDIUM_LINUX\"", "managementMachineTemplate \"MANAGER\"");
         additionalProps.put("MEDIUM_LINUX : computeTemplate", "MANAGER : computeTemplate");
        additionalProps.put("keyFile keyFile", "keyFile keyFile\n" +
                "                fileTransfer org.cloudifysource.domain.cloud.FileTransferModes.SCP");
         additionalProps.put("// Optional. Use existing networks.", "computeNetwork {\n" +
                 "\t\t\t\t\tnetworks ([\"SOME_INTERNAL_NETWORK_1\",\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "\t\t\t\t }\n // Optional. Use existing networks.");
         additionalProps.put("custom \\(\\[\"openstack.compute.zone\":availabilityZone]\\)", "custom ([\"openstack.compute.zone\":availabilityZone])\n" +
                 "\t\t\t\\},\n" +
                 "            APPLI : computeTemplate{\n" +
                 "                imageId imageId\n" +
                 "                remoteDirectory \"/root/gs-files\"\n" +
                 "                machineMemoryMB 1600\n" +
                 "                hardwareId hardwareId\n" +
                 "                localDirectory \"upload\"\n" +
                 "                keyFile keyFile\n" +
                 "                username \"ubuntu\"\n" +
                 "                options ([\n" +
                 "                    \"keyPairName\" : keyPair\n" +
                 "                ])\n" +
                 "                overrides ([\n" +
                 "                    \"openstack.endpoint\": openstackUrl\n" +
                 "                ])\n" +
                 "                computeNetwork {\n" +
                 "                    networks ([\"SOME_INTERNAL_NETWORK_2\"])\n" +
                 "                }");
 
         cloud = launcher.createCloud(null, additionalProps);
         Service service = launcher.getService("secgroups/securityGroup-service.groovy");
 
         // Remove networkTemplate
         service.getNetwork().setTemplate(null);
 
         // Modify computeTemplate
         ComputeDetails compute = new ComputeDetails();
         compute.setTemplate("APPLI");
         service.setCompute(compute);
 
         try {
             launcher.createExpectedExistingNetworks();
 
             MachineDetails md = launcher.startMachineWithManagement(service, cloud);
             assertNotNull("Machine id is null", md.getMachineId());
             assertNotNull("Private ip is null", md.getPrivateAddress());
             assertTrue("Private ip is not from subnet 177.86.0.114/24. Got " + md.getPrivateAddress(), md.getPrivateAddress().startsWith("177.86.0."));
             Assert.assertNull("Public ip should be null", md.getPublicAddress());
             launcher.assertVMBoundToNetwork(md.getMachineId(), COMPUTE_SOME_INTERNAL_NETWORK_2);
 
         } finally {
             launcher.cleanExpectedExistingNetworks();
         }
     }
 
     @Test
     public void testStartMachineWithOnlyManagementNetwork() throws Exception {
         Map<String, String> additionalProps = new HashMap<String, String>();
 
         additionalProps.put("range \"177.86.0.0/24\"", "range \"177.70.0.0/24\"");
         additionalProps.put("\"gateway\" : \"177.86.0.111\"", "\"gateway\" : \"177.70.0.1\"");
         additionalProps.put("Cloudify-Management-Subnet", "Cloudify-Management-Subnet1");
 
         cloud = launcher.createCloud(null, additionalProps);
         Service service = launcher.getService("secgroups/securityGroup-service.groovy");
 
         // Remove networkTemplate
         service.getNetwork().setTemplate(null);
 
         MachineDetails md = launcher.startMachineWithManagement(service, cloud);
         assertNotNull("Machine id is null", md.getMachineId());
         assertNotNull("Private ip is null", md.getPrivateAddress());
         assertTrue("Private ip is not from subnet 177.70.0.0/24. Got " + md.getPrivateAddress(), md.getPrivateAddress().startsWith("177.70.0."));
         assertNotNull("Public ip is null", md.getPublicAddress());
         launcher.assertFloatingIpBindToServer(md);
 
         String prefix = cloud.getProvider().getManagementGroup();
         OpenStackResourcePrefixes prefixes = new OpenStackResourcePrefixes(prefix, OpenstackCloudifyDriverLauncher.DEFAULT_APPLICATION_NAME, service.getName());
         launcher.assertSecurityGroupIncomingRulesNumber(prefixes.getServiceName(), service.getNetwork().getAccessRules().getIncoming().size());
 
         NetworkConfiguration networkConfiguration = cloud.getCloudNetwork().getManagement().getNetworkConfiguration();
         launcher.assertNetworkExists(prefix + networkConfiguration.getName());
         launcher.assertVMBoundToNetwork(md.getMachineId(), prefix + networkConfiguration.getName());
         launcher.assertSubnetExists(prefix + networkConfiguration.getName(), networkConfiguration.getSubnets().get(0).getName());
         launcher.assertSubnetSize(prefix + networkConfiguration.getName(), 1);
 
         launcher.stopMachineWithManagement(service, cloud, md.getPrivateAddress());
     }
 }
