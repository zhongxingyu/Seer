 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package utils;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
 import org.jclouds.openstack.nova.v2_0.domain.Ingress;
 import org.jclouds.openstack.nova.v2_0.domain.IpProtocol;
 import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
 import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
 import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
 import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
 import org.jclouds.rest.RestContext;
 
 import server.ApplicationContext;
 import server.exceptions.ServerException;
 import beans.config.ServerConfig.CloudBootstrapConfiguration;
 
 import com.google.common.collect.FluentIterable;
 
 
 /**
  * This class provides different static cloudify utilities methods.
  * @author adaml
  *
  */
 public class CloudifyUtils {
 	
 	/**
 	 * Creates a cloud folder containing all necessary credentials 
 	 * for bootstrapping to the HP cloud.
 	 * 
 	 * @param cloudConf
 	 * 			The configuration used to start the cloud.
 	 * @return
 	 * 			A path to the newly created cloud folder.
 	 * @throws IOException
 	 */
 	public static File createCloudFolder(String userName, String apiKey, ComputeServiceContext context) throws IOException {
 		
 		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
		String cloudifyBuildFolder = ApplicationContext.get().conf().server.environment.cloudifyHome;
 		File cloudifyEscFolder = new File(cloudifyBuildFolder, cloudConf.cloudifyEscDirRelativePath);
 
 		//copy the content of hp configuration files to a new folder
 		File destFolder = new File(cloudifyEscFolder, cloudConf.cloudName + getTempSuffix()); 
 		FileUtils.copyDirectory(new File(cloudifyEscFolder, cloudConf.cloudName), destFolder); 
 
 		// create new pem file using new credentials.
 		File pemFolder = new File(destFolder, cloudConf.cloudifyHpUploadDirName);
 		File newPemFile = createPemFile( context );
 		FileUtils.copyFile(newPemFile, new File(pemFolder, newPemFile.getName() +".pem"), true);
 
 		int colonIndex = userName.indexOf(":");
 		List<String> cloudProperties = new ArrayList<String>();
 		cloudProperties.add("tenant=" + '"' + userName.substring(0, colonIndex) + '"');
 		cloudProperties.add("user=" + '"' + userName.substring(colonIndex + 1, userName.length()) + '"');
 		cloudProperties.add("apiKey=" + '"' + apiKey + '"');
 		cloudProperties.add("keyFile=" + '"' + newPemFile.getName() +".pem" + '"');
 		cloudProperties.add("keyPair=" + '"' + newPemFile.getName() + '"');
 		cloudProperties.add("securityGroup=" + '"' + cloudConf.securityGroup + '"');
 		cloudProperties.add("hardwareId=" + '"' + cloudConf.hardwareId + '"');
 		cloudProperties.add("linuxImageId=" + '"' + cloudConf.linuxImageId + '"');
 
 		//create new props file and init with custom credentials. 
 		File newPropertiesFile = new File(destFolder, cloudConf.cloudPropertiesFileName + ".new" );
 		newPropertiesFile.createNewFile();
 		FileUtils.writeLines(newPropertiesFile, cloudProperties);
 
 		//delete old props file
 		File propertiesFile = new File(destFolder, cloudConf.cloudPropertiesFileName );
 		if (propertiesFile.exists()) {
 			propertiesFile.delete();
 		}
 
 		//rename new props file.
 		if (!newPropertiesFile.renameTo(propertiesFile)){
 			throw new ServerException("Failed creating custom cloud folder." +
 					" Failed renaming custom cloud properties file.");
 		}
 		return destFolder;
 	}
 	
 	/**
 	 * returns the private key used for starting the remote machines.
 	 * 
 	 * @param cloudFolder 
 	 * 			The folder used to bootstrap to the cloud.
 	 * @param cloudBootstrapConfig
 	 * 			The cloud configuration used to bootstrap to the cloud.
 	 * @return
 	 * 			The private key used for starting the remote machines
 	 * @throws IOException
 	 */
 	public static String getCloudPrivateKey(File cloudFolder) throws IOException {
 		File pemFile = getPemFile(cloudFolder);
 		if (pemFile == null) {
 			return null;
 		}
 		return FileUtils.readFileToString(pemFile);
 	}
 	
 	// creates a new pem file for a given hp cloud account.
 	private static File createPemFile( ComputeServiceContext context ){
 		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
 		try {
 			RestContext<NovaApi, NovaAsyncApi> novaClient = context.unwrap();
 			NovaApi api = novaClient.getApi();
 			KeyPairApi keyPairApi = api.getKeyPairExtensionForZone( cloudConf.zoneName ).get();
 			KeyPair keyPair = keyPairApi.create( cloudConf.keyPairName + getTempSuffix()); 
 
 			File pemFile = new File(System.getProperty("java.io.tmpdir"), keyPair.getName());
 			pemFile.createNewFile();
 			FileUtils.writeStringToFile(pemFile, keyPair.getPrivateKey());
 			return pemFile;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * 
 	 * Create a security group with all ports open.
 	 * 
 	 * @param context The jClouds context.
 	 */
 	public static void createCloudifySecurityGroup( ComputeServiceContext context ) {
 		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
 		try {
 			RestContext<NovaApi, NovaAsyncApi> novaClient = context.unwrap();
 			NovaApi novaApi = novaClient.getApi();
 			SecurityGroupApi securityGroupClient = novaApi.getSecurityGroupExtensionForZone(cloudConf.zoneName).get();
 			//Check if group already exists.
 			FluentIterable<? extends SecurityGroup> groupsList = securityGroupClient.list();
 			for (Object group : groupsList) {
 				if (((SecurityGroup)group).getName().equals(cloudConf.securityGroup)) {
 					return;
 				}
 			}
 			//Create a new security group with open port range of 80-65535.
 			Ingress ingress = Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(80).toPort(65535).build();
 			SecurityGroup securityGroup = securityGroupClient.createWithDescription(cloudConf.securityGroup, "All ports open.");
 			securityGroupClient.createRuleAllowingCidrBlock(securityGroup.getId(), ingress, "0.0.0.0/0");
 		} 
 		catch (Exception e) {
 			throw new RuntimeException("Failed creating security group.", e);
 		} 
 	}
 	
 	/**
 	 * Create an HP cloud context.
 	 * @param userName HP cloud username.
 	 * @param apiKey HP cloud API key.
 	 * @return the HP lClouds compute context.
 	 */
 	public static ComputeServiceContext createJcloudsContext(String userName,
 			String apiKey) {
 		CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
 		ComputeServiceContext context;
 		Properties overrides = new Properties();
 		overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
 		context = ContextBuilder.newBuilder( cloudConf.cloudProvider )
 				.credentials( userName, apiKey )
 				.overrides(overrides)
 				.buildView(ComputeServiceContext.class);
 
 		return context;
 	}
 
 	private static String getTempSuffix() {
 		String currTime = Long.toString(System.currentTimeMillis());
 		return currTime.substring(currTime.length() - 4);
 	}
 
 
 
 	private static File getPemFile(File cloudFolder) {
 		final CloudBootstrapConfiguration cloudConf = ApplicationContext.get().conf().server.cloudBootstrap;
 		File uploadDir = new File(cloudFolder, cloudConf.cloudifyHpUploadDirName);
 		File[] filesList = uploadDir.listFiles(new FilenameFilter() {
 
 			@Override
 			public boolean accept(File dir, String name) {
 
 				return name.startsWith(cloudConf.keyPairName)
 						&& name.endsWith( "pem" );
 			}
 		});
 
 		if ( filesList.length == 0 || filesList.length > 1) {
 			return null;
 		}
 		return filesList[0];
 	}
 }
