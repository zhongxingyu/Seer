 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.tools.cli;
 
 import com.elasticgrid.model.ClusterAlreadyRunningException;
 import com.elasticgrid.model.NodeProfile;
 import com.elasticgrid.model.NodeProfileInfo;
 import com.elasticgrid.model.NodeType;
 import com.elasticgrid.model.ec2.EC2NodeType;
 import com.elasticgrid.platforms.ec2.config.EC2Configuration;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import org.jets3t.service.S3Service;
 import org.jets3t.service.S3ServiceException;
 import org.jets3t.service.impl.rest.httpclient.RestS3Service;
 import org.jets3t.service.model.S3Bucket;
 import org.jets3t.service.model.S3Object;
 import org.jets3t.service.security.AWSCredentials;
 import org.rioproject.tools.cli.OptionHandler;
 
 import java.io.*;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 public class StartClusterHandler extends AbstractHandler
     implements OptionHandler {
 
     public static final String AGENT_OVERRIDE_FILE_NAME = "start-agent-override.groovy";
     public static final String MONITOR_OVERRIDE_FILE_NAME = "start-monitor-override.groovy";
 
     /**
      * Process the option.
      *
      * @param input Parameters for the option, may be null
      * @param br An optional BufferdReader, used if the option requires input.
      * if this is null, the option handler may create a BufferedReader to handle
      * the input
      * @param out The PrintStream to use if the option prints results or choices
      * for the user. Must not be null
      * @return The result of the action.
      */
     public String process(String input, BufferedReader br, PrintStream out) {
         File overridesDir = null;
         StringTokenizer tok = new StringTokenizer(input);
         // first token is "start-cluster"
         tok.nextToken();
         // second token is cluster name
         String clusterName = null;
         if (tok.hasMoreTokens())
             clusterName = tok.nextToken();
         List<NodeProfileInfo> nodeProfileInfo = new ArrayList<NodeProfileInfo>();
         if (clusterName == null) {
             try {
                 clusterName = getClusterName(br, out);
             } catch (IOException e) {
                 e.printStackTrace();
                 return "unexpected parsing exception, check log";
             }
             if (clusterName == null) {
                 out.println("Cluster name not provided, start-cluster cancelled");
                 return "";
             }
             nodeProfileInfo.addAll(interactiveClusterSetup(br, out));
         } else if (!tok.hasMoreTokens()) {
             nodeProfileInfo.addAll(interactiveClusterSetup(br, out));
         } else {
 
             while (tok.hasMoreTokens()) {
                 String value = tok.nextToken();
                 if (value.startsWith("mon:")) {
                     if (!processOption(value.substring(4),
                                        nodeProfileInfo,
                                        NodeProfile.MONITOR,
                                        out))
                         return getUsage();
 
                 } else if (value.startsWith("agent:")) {
                     if (!processOption(value.substring(6),
                                        nodeProfileInfo,
                                        NodeProfile.AGENT,
                                        out))
                         return getUsage();
                 } else if (value.startsWith("monAgent:")) {
                     if (!processOption(value.substring(9),
                                        nodeProfileInfo,
                                        NodeProfile.MONITOR_AND_AGENT,
                                        out))
                         return getUsage();
                 } else {
                     /* process overrides*/
                     if(value.startsWith("~")) {
                         value = System.getProperty("user.home")+value.substring(1);
                     }
 
                     overridesDir = new File(value);
                     if(!overridesDir.exists() || !overridesDir.isDirectory())
                         return ("The ["+value+"] directory does not exist, or " +
                                 "is not a directory. Please provide a valid " +
                                 "location for cluster configuration overrides");
                     /* Check for groovy override configuration files */
 
                     for(String s : overridesDir.list()) {
                         if(s.equals(AGENT_OVERRIDE_FILE_NAME)) {
                             setHasOverride(NodeProfile.AGENT, nodeProfileInfo);
                             setHasOverride(NodeProfile.MONITOR_AND_AGENT, nodeProfileInfo);
                         }
                         if(s.equals(MONITOR_OVERRIDE_FILE_NAME)) {
                             setHasOverride(NodeProfile.MONITOR, nodeProfileInfo);
                             setHasOverride(NodeProfile.MONITOR_AND_AGENT, nodeProfileInfo);
                         }
                     }
                     //out.println("Unknown option " + value);
                     //return getUsage();
                 }
             }
         }
 
         if (clusterName.length() == 0)
             return ("Cluster must have a name, start-cluster cancelled");
         int numMonitors = countMonitors(nodeProfileInfo);
         int numAgents = countAgents(nodeProfileInfo);
         int numMonitorAgents = countMonitorAgents(nodeProfileInfo);
         if (numMonitors > 0 || numAgents > 0 || numMonitorAgents > 0) {
             try {
                 if(overridesDir!=null) {
                     uploadOverrides(overridesDir, clusterName, out);
                 }
                 out.println("\nStarting cluster [" + clusterName + "] ...");
                 getClusterManager().startCluster(clusterName, nodeProfileInfo);
                 return "Cluster [" + clusterName + "] started with " +
                        numMonitors + " Monitor(s), " +
                        numAgents + " Agent(s), " +
                        numMonitorAgents + " Monitor Agent(s)";
             } catch (ClusterAlreadyRunningException e) {
                 return "cluster already running!";
             } catch (Exception e) {
                 e.printStackTrace(out);
                 return "unexpected cluster exception";
             }
         } else {
             return ("Cluster [" + clusterName + "] has no configured cluster " +
                     "services, start-cluster cancelled");
         }
 
     }
 
     /**
      * Get the usage of the command
      *
      * @return Command usage
      */
     public String getUsage() {
         return ("usage: start-cluster [clusterName " +
                 "[mon:count[:s|m|l|xl]] " +
                 "[agent:count[:s|m|l|xl]] " +
                 "[monAgent:count[:s|m|l|xl]] [overrides-dir] ]\n");
     }
 
     private boolean processOption(String option,
                                   List<NodeProfileInfo> nodeProfileInfo,
                                   NodeProfile nodeProfile,
                                   PrintStream out) {
         int num;
         try {
             num = getNumber(option);
         } catch (NumberFormatException e) {
             out.println("Not a valid number: " + option);
             return false;
         }
         String type = getType(option);
         if (type == null) {
             out.println("Bad type: " + option);
             return false;
         }
         addNodeProfileInfo(nodeProfileInfo,
                            nodeProfile,
                            translateToNodeType(type),
                            num);
         return true;
     }
 
     private int getNumber(String s) {
         int number;
         int ndx = s.indexOf(":");
         if (ndx > 0)
             s = s.substring(0, ndx);
         number = Integer.parseInt(s);
         return number;
     }
 
     private String getType(String s) {
         String type = null;
         int ndx = s.indexOf(":");
         if (ndx > 0) {
             s = s.substring(ndx + 1);
             String[] types = {"s", "m", "l", "xl"};
             for (String t : types) {
                 if (t.equals(s)) {
                     type = s;
                     break;
                 }
             }
         } else {
             type = "s";
         }
         return type;
     }
 
     private void addNodeProfileInfo(List<NodeProfileInfo> nodeProfileInfo,
                                     NodeProfile nodeProfile,
                                     NodeType nodeType,
                                     int number) {
         nodeProfileInfo.add(new NodeProfileInfo(nodeProfile, nodeType, number));
     }
 
     private String getClusterName(BufferedReader br, PrintStream out)
         throws IOException {
         return getResponse("Enter name of cluster: ", br, out);
     }
 
     private List<NodeProfileInfo> interactiveClusterSetup(BufferedReader br,
                                                           PrintStream out) {
         List<NodeProfileInfo> list = new ArrayList<NodeProfileInfo>();
         try {
             if (br == null)
                 br = new BufferedReader(new InputStreamReader(System.in));
 
             int numMonitors =
                 getIntegerResponse("Number of Monitors: ", br, out);
 
             String[] types = {"s", "m", "l", "xl", ""};
             if (numMonitors > 0) {
                 String monType =
                     getValidResponse("Monitor Machine type [s,m,l,xl] (s): ",
                                      types,
                                      "s",
                                      br,
                                      out);
                 list.add(new NodeProfileInfo(NodeProfile.MONITOR,
                                              translateToNodeType(monType),
                                              numMonitors));
             }
 
             int numAgents =
                 getIntegerResponse("Number of Agents: ", br, out);
             if (numAgents > 0) {
                 String agentType =
                     getValidResponse("Agent Machine type [s,m,l,xl] (s): ",
                                      types,
                                      "s",
                                      br,
                                      out);
                 list.add(new NodeProfileInfo(NodeProfile.AGENT,
                                              translateToNodeType(agentType),
                                              numAgents));
             }
 
             int numMonAgents =
                 getIntegerResponse("Number of Monitor Agents: ", br, out);
             if (numMonAgents > 0) {
                 String monAgentType =
                     getValidResponse("Monitor Agent Machine type [s,m,l,xl] (s): ",
                                      types,
                                      "s",
                                      br,
                                      out);
                 list.add(new NodeProfileInfo(NodeProfile.MONITOR_AND_AGENT,
                                              translateToNodeType(monAgentType),
                                              numMonAgents));
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return list;
     }
 
     private String getResponse(String ask, BufferedReader br, PrintStream out)
         throws IOException {
         out.print(ask);
         return br.readLine();
     }
 
     private NodeType translateToNodeType(String type) {
         NodeType nType;
         if (type.equals("s")) {
             nType = EC2NodeType.SMALL;
         } else if (type.equals("m")) {
             nType = EC2NodeType.MEDIUM_HIGH_CPU;
         } else if (type.equals("l")) {
             nType = EC2NodeType.LARGE;
         } else {
             nType = EC2NodeType.EXTRA_LARGE;
        }
         return nType;
     }
 
     private String getValidResponse(String ask,
                                     String[] validResponse,
                                     String def,
                                     BufferedReader br,
                                     PrintStream out)
         throws IOException {
         String response = null;
         while (true) {
             out.print(ask);
             String s = br.readLine();
             if (s == null)
                 break;
             for (String v : validResponse) {
                 if (v.equals(s)) {
                    response = s;
                     break;
                 }
             }
             if (response != null)
                 break;
             else
                 out.println("Invalid response [" + s + "]");
         }
         return response == null ? def : response;
     }
 
     private int getIntegerResponse(String ask,
                                    BufferedReader br,
                                    PrintStream out)
         throws IOException {
         int response = -1;
         while (true) {
             out.print(ask);
             String s = br.readLine();
             if (s == null)
                 break;
             if (s.length() == 0)
                 break;
             try {
                 response = Integer.parseInt(s);
                 if (response < 0)
                     out.println("Invalid number [" + s + "]");
                 else
                     break;
             } catch (NumberFormatException e) {
                 out.println("Invalid number [" + s + "]");
             }
         }
         return response;
     }
 
     private void setHasOverride(NodeProfile np, List<NodeProfileInfo> list) {
         for (NodeProfileInfo npi : list) {
             if (npi.getNodeProfile().equals(np))
                 npi.setOverride(true);
         }
     }
 
     private int countAgents(List<NodeProfileInfo> list) {
         int count = 0;
         for (NodeProfileInfo npi : list) {
             if (npi.getNodeProfile().equals(NodeProfile.AGENT))
                 count += npi.getNumber();
         }
         return count;
     }
 
     private int countMonitors(List<NodeProfileInfo> list) {
         int count = 0;
         for (NodeProfileInfo npi : list) {
             if (npi.getNodeProfile().equals(NodeProfile.MONITOR))
                 count += npi.getNumber();
         }
         return count;
     }
 
     private int countMonitorAgents(List<NodeProfileInfo> list) {
         int count = 0;
         for (NodeProfileInfo npi : list) {
             if (npi.getNodeProfile().equals(NodeProfile.MONITOR_AND_AGENT))
                 count += npi.getNumber();
         }
         return count;
     }
 
     private void uploadOverrides(File dir, String clusterName, PrintStream out) throws
                                                                IOException,
                                                                S3ServiceException,
                                                                NoSuchAlgorithmException {
         Properties awsConfig = AWSUtils.loadEC2Configuration();
         String awsAccessID = awsConfig.getProperty(EC2Configuration.AWS_ACCESS_ID);
         String awsSecretKey = awsConfig.getProperty(EC2Configuration.AWS_SECRET_KEY);
         if (awsAccessID == null) {
             throw new IllegalArgumentException("Could not find AWS Access ID");
         }
         if (awsSecretKey == null) {
             throw new IllegalArgumentException("Could not find AWS Secret Key");
         }
         AWSCredentials credentials = new AWSCredentials(awsAccessID, awsSecretKey);
 
         S3Service s3Service = new RestS3Service(credentials);
         String overridesBucket = awsConfig.getProperty(EC2Configuration.EG_OVERRIDES_BUCKET);
         out.println("Uploading overrides from ["+dir.getPath()+"] " +
                                 "to S3 bucket ["+overridesBucket+"] ...");
         S3Bucket s3OverridesBucket = s3Service.getOrCreateBucket(overridesBucket);
 
         for(File file : dir.listFiles()) {
             if(file.getName().endsWith(".groovy")) {
                 S3Object s3o = new S3Object(s3OverridesBucket, file);
                 out.println("Sending "+file.getName()+"...");
                 s3o.setKey("/"+clusterName+"/"+file.getName());
                 s3Service.putObject(s3OverridesBucket, s3o);
             }
         }
     }
 }
