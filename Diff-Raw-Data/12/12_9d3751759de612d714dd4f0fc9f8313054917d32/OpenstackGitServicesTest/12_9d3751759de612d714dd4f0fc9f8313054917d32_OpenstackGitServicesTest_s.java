 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack.examples;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.examples.Ec2GitServicesTest;
 
 public class OpenstackGitServicesTest extends Ec2GitServicesTest {
 
     @Override
     protected String getCloudName() {
         return "openstack";
     }
 }
