 /**
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * This file is part of Elastic Grid.
  * 
  * Elastic Grid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or any later version.
  * 
  * Elastic Grid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Elastic Grid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.amazon.boot;
 
 import com.elasticgrid.platforms.ec2.discovery.EC2ClusterLocator;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.ec2.EC2Node;
 import com.xerox.amazonws.ec2.EC2Exception;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Bootstrapper in charge of fetching the EC2 launch parameters and generating the EG configuration files.
  * The launch parameters are available in the <tt>/tmp/user-data</tt> file.
  *
  * @author Jerome Bernard
  */
 public class Bootstrapper {
     public static final String CLUSTER_NAME = "CLUSTER_NAME";
     public static final String LAUNCH_PARAMETER_ACCESS_ID = "AWS_ACCESS_ID";
     public static final String LAUNCH_PARAMETER_SECRET_KEY = "AWS_SECRET_KEY";
     public static final String LAUNCH_PARAMETER_SQS_SECURED = "AWS_SQS_SECURED";
 
     public static final String EG_PARAMETER_ACCESS_ID = "aws.accessId";
     public static final String EG_PARAMETER_SECRET_KEY = "aws.secretKey";
     public static final String EG_PARAMETER_SQS_SECURED = "aws.sqs.secured";
     public static final String EG_MONITOR_HOST = "eg.monitor.host";
 
     public static final String EG_GROUP_MONITOR = "eg-monitor";
     public static final String EG_GROUP_AGENT = "eg-agent";
 
     public static final String LAUNCH_PARAMETERS_FILE = "/tmp/user-data";
     public static final String ELASTIC_GRID_CONFIGURATION_FILE = "config/eg.properties";
 
     private String egHome = System.getProperty("EG_HOME");
 
     public Bootstrapper() throws IOException, EC2Exception {
         // retreive EC2 parameters
         Properties launchParameters = fetchLaunchParameters();
         Properties egParameters = translateProperties(launchParameters);
 
         // save configuration
         File file = saveConfiguration(egParameters);
         System.out.printf("Elastic Cluster configuration file generated in '%s'\n", file.getAbsolutePath());
 
         // start Spring context
         ApplicationContext ctx = new ClassPathXmlApplicationContext("/com/elasticgrid/amazon/boot/applicationContext.xml");
 
         // locate monitor node
        EC2ClusterLocator locator = (EC2ClusterLocator) ctx.getBean("clusterLocator", EC2ClusterLocator.class);
         String clusterName = launchParameters.getProperty(CLUSTER_NAME);
         try {
             EC2Node monitor = locator.findMonitor(clusterName);
             InetAddress monitorHost = monitor.getAddress();
             String monitorHostAddress = monitorHost.getHostAddress();
             if (monitorHostAddress.equals(InetAddress.getLocalHost().getHostAddress())) {
                 System.out.println("This host is going to be the new monitor!");
             } else {
                 System.out.printf("Using monitor host: %s\n", monitorHost.getHostName());
                 egParameters.put(EG_MONITOR_HOST, monitorHost.getHostName());
             }
             FileUtils.writeStringToFile(new File(egHome + File.separator + "config", "monitor-host"), monitorHost.getHostName());
         } catch (ClusterException e) {
             System.err.println("Could not find monitor host!");
             System.exit(-1);
         }
     }
 
     private Properties fetchLaunchParameters() throws IOException {
         Properties launchProperties = new Properties();
         InputStream launchFile = null;
         try {
             launchFile = new FileInputStream(new File(LAUNCH_PARAMETERS_FILE));
             launchProperties.load(launchFile);
             return launchProperties;
         } finally {
             IOUtils.closeQuietly(launchFile);
         }
     }
 
     private Properties translateProperties(Properties launchParameters) {
         // translate properties
         Properties egParameters = new Properties();
         for (Map.Entry property : launchParameters.entrySet()) {
             String key = (String) property.getKey();
             if (LAUNCH_PARAMETER_ACCESS_ID.equals(key))
                 egParameters.put(EG_PARAMETER_ACCESS_ID, property.getValue());
             if (LAUNCH_PARAMETER_SECRET_KEY.equals(key))
                 egParameters.put(EG_PARAMETER_SECRET_KEY, property.getValue());
             if (LAUNCH_PARAMETER_SQS_SECURED.equals(key))
                 egParameters.put(EG_PARAMETER_SQS_SECURED, property.getValue());
         }
         egParameters.put("aws.ec2.secured", Boolean.TRUE.toString());
         egParameters.put("aws.ec2.key", "eg-key");  // todo: replace this with some dynamic!!
         egParameters.put("aws.ec2.ami32", "??");    // todo: replace this with some dynamic!!
         egParameters.put("aws.ec2.ami64", "??");    // todo: replace this with some dynamic!!
         return egParameters;
     }
 
     private File saveConfiguration(Properties egParameters) throws IOException {
         // write EG configuration
         if (egHome == null) {
             System.err.println("Could not find EG_HOME environment variable. Please fix this.");
             System.exit(-1);
         }
         File config = new File(egHome, ELASTIC_GRID_CONFIGURATION_FILE);
         FileOutputStream stream = null;
         try {
             stream = new FileOutputStream(config);
             egParameters.store(stream, "Elastic Cluster Configuration File - Generated file, please do NOT edit!");
         } finally {
             IOUtils.closeQuietly(stream);
         }
         return config;
     }
 
     public static void main(String[] args) throws IOException, EC2Exception {
         System.out.printf("Preparing Elastic Cluster environment...\n");
         new Bootstrapper();
     }
 }
