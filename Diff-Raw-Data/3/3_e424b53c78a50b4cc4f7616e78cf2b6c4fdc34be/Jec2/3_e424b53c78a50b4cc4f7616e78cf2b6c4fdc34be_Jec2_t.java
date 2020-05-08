 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007,2008,2009,2010 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.ec2;
 
 import com.xerox.amazonws.common.AWSException;
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.jaxb.*;
 import com.xerox.amazonws.vpc.SubnetInfo;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.xml.sax.SAXException;
 
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.*;
 
 /**
  * A Java wrapper for the EC2 web services API
  */
 @SuppressWarnings({"UnusedDeclaration"})
 public class Jec2 extends AWSQueryConnection {
   /**
    * Initializes the ec2 service with your AWS login information.
    *
    * @param awsAccessId  The your user key into AWS
    * @param awsSecretKey The secret string used to generate signatures for authentication.
    */
   public Jec2(String awsAccessId, String awsSecretKey) {
     this(awsAccessId, awsSecretKey, true);
   }
 
   /**
    * Initializes the ec2 service with your AWS login information.
    *
    * @param awsAccessId  The your user key into AWS
    * @param awsSecretKey The secret string used to generate signatures for authentication.
    * @param isSecure     True if the data should be encrypted on the wire on the way to or from EC2.
    */
   public Jec2(String awsAccessId, String awsSecretKey, boolean isSecure) {
     this(awsAccessId, awsSecretKey, isSecure, "ec2.amazonaws.com");
   }
 
   /**
    * Initializes the ec2 service with your AWS login information.
    *
    * @param awsAccessId  The your user key into AWS
    * @param awsSecretKey The secret string used to generate signatures for authentication.
    * @param isSecure     True if the data should be encrypted on the wire on the way to or from EC2.
    * @param server       Which host to connect to.  Usually, this will be ec2.amazonaws.com
    */
   public Jec2(String awsAccessId, String awsSecretKey, boolean isSecure,
               String server) {
     this(awsAccessId, awsSecretKey, isSecure, server,
             isSecure ? 443 : 80);
   }
 
   /**
    * Initializes the ec2 service with your AWS login information.
    *
    * @param awsAccessId  The your user key into AWS
    * @param awsSecretKey The secret string used to generate signatures for authentication.
    * @param isSecure     True if the data should be encrypted on the wire on the way to or from EC2.
    * @param server       Which host to connect to.  Usually, this will be ec2.amazonaws.com
    * @param port         Which port to use.
    */
   public Jec2(String awsAccessId, String awsSecretKey, boolean isSecure,
               String server, int port) {
     super(awsAccessId, awsSecretKey, isSecure, server, port);
     setConnectionVersion("2011-02-28");
   }
 
   /**
    * Creates an AMI that uses an EBS root device.
    *
    * @param instanceId  An instance's id ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @param name        a name to associate with the image
    * @param description a descriptive string to attach to the image
    * @param noReboot    normally false; if set to true, instance is not shutdown first.
    *                    NOTE: filesystem integrity isn't guaranteed when noReboot=true
    * @return image ID
    * @throws EC2Exception wraps checked exceptions
    */
   public String createImage(String instanceId, String name, String description,
                             boolean noReboot) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("Name", name);
     if (description != null && !description.trim().equals("")) {
       params.put("Description", description);
     }
     if (noReboot) {
       params.put("NoReboot", "true");
     }
     CreateImageResponse response =
             makeRequestInt(new HttpGet(), "CreateImage", params, CreateImageResponse.class);
     return response.getImageId();
   }
 
   /**
    * Register an S3 based AMI.
    *
    * @param imageLocation An AMI path within S3.
    * @return A unique AMI ID that can be used to create and manage instances of this AMI.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public String registerImage(String imageLocation) throws EC2Exception {
     return registerImage(imageLocation, null, null, null, null, null, null, null);
   }
 
   /**
    * Register a snapshot as an EBS backed AMI
    *
    * @param name
    * @param description
    * @param architecture
    * @param kernelId
    * @param ramdiskId
    * @param rootDeviceName
    * @param blockDeviceMappings
    * @return A unique AMI ID that can be used to create and manage instances of this AMI.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public String registerImage(String name,
                               String description, String architecture,
                               String kernelId, String ramdiskId, String rootDeviceName,
                               List<BlockDeviceMapping> blockDeviceMappings) throws EC2Exception {
     return registerImage(null, name, description, architecture, kernelId, ramdiskId,
             rootDeviceName, blockDeviceMappings);
   }
 
   protected String registerImage(@Nullable String imageLocation, @Nullable String name,
                                  @Nullable String description, @Nullable String architecture,
                                  @Nullable String kernelId, @Nullable String ramdiskId, @Nullable String rootDeviceName,
                                  @Nullable List<BlockDeviceMapping> blockDeviceMappings) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (imageLocation != null && !imageLocation.trim().equals("")) {
       params.put("ImageLocation", imageLocation);
     }
     if (name != null && !name.trim().equals("")) {
       params.put("Name", name);
     }
     if (description != null && !description.trim().equals("")) {
       params.put("Description", description);
     }
     if (architecture != null && !architecture.trim().equals("")) {
       params.put("Architecture", architecture);
     }
     if (kernelId != null && !kernelId.trim().equals("")) {
       params.put("KernelId", kernelId);
     }
     if (ramdiskId != null && !ramdiskId.trim().equals("")) {
       params.put("RamdiskId", ramdiskId);
     }
     if (rootDeviceName != null && !rootDeviceName.trim().equals("")) {
       params.put("RootDeviceName", rootDeviceName);
     }
     if (blockDeviceMappings != null) {
       for (int i = 0; i < blockDeviceMappings.size(); i++) {
         BlockDeviceMapping bdm = blockDeviceMappings.get(i);
         params.put("BlockDeviceMapping." + (i + 1) + ".DeviceName",
                 bdm.getDeviceName());
         if (bdm.getVirtualName() != null) {
           params.put("BlockDeviceMapping." + (i + 1) + ".VirtualName",
                   bdm.getVirtualName());
         } else {
           if (bdm.getSnapshotId() != null) {
             params.put("BlockDeviceMapping." + (i + 1) + ".Ebs.SnapshotId",
                     bdm.getSnapshotId());
           }
           if (bdm.getVolumeSize() > 0) {
             params.put("BlockDeviceMapping." + (i + 1) + ".Ebs.VolumeSize",
                     "" + bdm.getVolumeSize());
           }
           params.put("BlockDeviceMapping." + (i + 1) + ".Ebs.DeleteOnTermination",
                   bdm.isDeleteOnTerminate() ? "true" : "false");
         }
       }
     }
     RegisterImageResponse response =
             makeRequestInt(new HttpGet(), "RegisterImage", params, RegisterImageResponse.class);
     return response.getImageId();
   }
 
   /**
    * Deregister the given AMI.
    *
    * @param imageId An AMI ID as returned by {@link #registerImage(String)}.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public void deregisterImage(String imageId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("ImageId", imageId);
     DeregisterImageResponse response =
             makeRequestInt(new HttpGet(), "DeregisterImage", params, DeregisterImageResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not deregister image : " + imageId + ". No reason given.");
     }
   }
 
   /**
    * Describe the given AMIs.
    *
    * @param imageIds An array of AMI IDs as returned by {@link #registerImage(String)}.
    * @return A list of {@link ImageDescription} instances describing each AMI ID.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ImageDescription> describeImages(String[] imageIds) throws EC2Exception {
     return describeImages(Arrays.asList(imageIds));
   }
 
   /**
    * Describe the given AMIs.
    *
    * @param imageIds A list of AMI IDs as returned by {@link #registerImage(String)}.
    * @return A list of {@link ImageDescription} instances describing each AMI ID.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ImageDescription> describeImages(List<String> imageIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < imageIds.size(); i++) {
       params.put("ImageId." + (i + 1), imageIds.get(i));
     }
     return describeImages(params);
   }
 
   /**
    * Describe the AMIs belonging to the supplied owners.
    *
    * @param owners A list of owners.
    * @return A list of {@link ImageDescription} instances describing each AMI ID.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ImageDescription> describeImagesByOwner(List<String> owners) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < owners.size(); i++) {
       params.put("Owner." + (i + 1), owners.get(i));
     }
     return describeImages(params);
   }
 
   /**
    * Describe the AMIs executable by supplied users.
    *
    * @param users A list of users.
    * @return A list of {@link ImageDescription} instances describing each AMI ID.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ImageDescription> describeImagesByExecutability(List<String> users) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < users.size(); i++) {
       params.put("ExecutableBy." + (i + 1), users.get(i));
     }
     return describeImages(params);
   }
 
   /**
    * Describe the AMIs that match the intersection of the criteria supplied
    *
    * @param imageIds A list of AMI IDs as returned by {@link #registerImage(String)}.
    * @param owners   A list of owners.
    * @param users    A list of users.
    * @return A list of {@link ImageDescription} instances describing each AMI ID.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ImageDescription> describeImages(List<String> imageIds, List<String> owners,
                                                List<String> users) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < imageIds.size(); i++) {
       params.put("ImageId." + (i + 1), imageIds.get(i));
     }
     for (int i = 0; i < owners.size(); i++) {
       params.put("Owner." + (i + 1), owners.get(i));
     }
     for (int i = 0; i < users.size(); i++) {
       params.put("ExecutableBy." + (i + 1), users.get(i));
     }
     return describeImages(params);
   }
 
 
   protected List<ImageDescription> describeImages(Map<String, String> params) throws EC2Exception {
     DescribeImagesResponse response =
             makeRequestInt(new HttpGet(), "DescribeImages", params, DescribeImagesResponse.class);
     List<ImageDescription> result = new ArrayList<ImageDescription>();
     DescribeImagesResponseInfoType set = response.getImagesSet();
     for (DescribeImagesResponseItemType item : set.getItems()) {
       ArrayList<String> codes = new ArrayList<String>();
       ProductCodesSetType code_set = item.getProductCodes();
       if (code_set != null) {
         for (ProductCodesSetItemType code : code_set.getItems()) {
           codes.add(code.getProductCode());
         }
       }
       ArrayList<BlockDeviceMapping> bdm = new ArrayList<BlockDeviceMapping>();
       BlockDeviceMappingType bdmType = item.getBlockDeviceMapping();
       if (bdmType != null) {
         for (BlockDeviceMappingItemType mapping : bdmType.getItems()) {
           if (mapping.getVirtualName() != null) {
             bdm.add(new BlockDeviceMapping(mapping.getVirtualName(), mapping.getDeviceName()));
           } else if (mapping.getEbs() != null) {
             EbsBlockDeviceType ebs = mapping.getEbs();
             bdm.add(new BlockDeviceMapping(mapping.getDeviceName(), ebs.getSnapshotId(),
                     ebs.getVolumeSize(), ebs.isDeleteOnTermination()));
           } else {
             bdm.add(new BlockDeviceMapping("", mapping.getDeviceName()));
           }
         }
       }
       String reason = "";
       if (item.getStateReason() != null) {
         reason = item.getStateReason().getMessage();
       }
       result.add(new ImageDescription(item.getImageId(),
               item.getImageLocation(), item.getImageOwnerId(),
               item.getImageState(), item.isIsPublic(), codes,
               item.getArchitecture(), item.getImageType(),
               item.getKernelId(), item.getRamdiskId(), item.getPlatform(),
               reason, item.getImageOwnerAlias(),
               item.getName(), item.getDescription(), item.getRootDeviceType(),
               item.getRootDeviceName(), bdm, item.getVirtualizationType()));
     }
     return result;
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    * <p/>
    * NOTE: this method defaults to the AWS desired "public" addressing type.
    * NOTE: this method defaults to the small(traditional) instance type.
    *
    * @param imageId  An AMI ID as returned by {@link #registerImage(String)}.
    * @param minCount The minimum number of instances to attempt to reserve.
    * @param maxCount The maximum number of instances to attempt to reserve.
    * @param groupSet A (possibly empty) set of security group definitions.
    * @param userData User supplied data that will be made available to the instance(s)
    * @param keyName
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(String imageId, int minCount,
                                              int maxCount, List<String> groupSet, String userData, String keyName)
           throws EC2Exception {
     return runInstances(imageId, minCount, maxCount, groupSet, userData, keyName, true, InstanceType.DEFAULT);
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    * NOTE: this method defaults to the small(traditional) instance type.
    *
    * @param imageId    An AMI ID as returned by {@link #registerImage(String)}.
    * @param minCount   The minimum number of instances to attempt to reserve.
    * @param maxCount   The maximum number of instances to attempt to reserve.
    * @param groupSet   A (possibly empty) set of security group definitions.
    * @param userData   User supplied data that will be made available to the instance(s)
    * @param keyName
    * @param publicAddr sets addressing mode to public
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(String imageId, int minCount,
                                              int maxCount, List<String> groupSet, String userData, String keyName, boolean publicAddr)
           throws EC2Exception {
     return runInstances(imageId, minCount, maxCount, groupSet, userData, keyName, publicAddr, InstanceType.DEFAULT);
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    * NOTE: this method defaults to the AWS desired "public" addressing type.
    *
    * @param imageId  An AMI ID as returned by {@link #registerImage(String)}.
    * @param minCount The minimum number of instances to attempt to reserve.
    * @param maxCount The maximum number of instances to attempt to reserve.
    * @param groupSet A (possibly empty) set of security group definitions.
    * @param userData User supplied data that will be made available to the instance(s)
    * @param keyName
    * @param type     instance type
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(String imageId, int minCount,
                                              int maxCount, List<String> groupSet, String userData, String keyName, InstanceType type)
           throws EC2Exception {
     return runInstances(imageId, minCount, maxCount, groupSet, userData, keyName, true, type);
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    *
    * @param imageId    An AMI ID as returned by {@link #registerImage(String)}.
    * @param minCount   The minimum number of instances to attempt to reserve.
    * @param maxCount   The maximum number of instances to attempt to reserve.
    * @param groupSet   A (possibly empty) set of security group definitions.
    * @param userData   User supplied data that will be made available to the instance(s)
    * @param publicAddr sets addressing mode to public
    * @param type       instance type
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(String imageId,
                                              int minCount,
                                              int maxCount,
                                              List<String> groupSet,
                                              String userData,
                                              String keyName,
                                              boolean publicAddr,
                                              InstanceType type) throws EC2Exception {
     return runInstances(imageId, minCount, maxCount, groupSet, userData, keyName, publicAddr, type, null, null, null, null);
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    *
    * @param imageId             An AMI ID as returned by {@link #registerImage(String)}.
    * @param minCount            The minimum number of instances to attempt to reserve.
    * @param maxCount            The maximum number of instances to attempt to reserve.
    * @param groupSet            A (possibly empty) set of security group definitions.
    * @param userData            User supplied data that will be made available to the instance(s)
    * @param keyName
    * @param publicAddr          sets addressing mode to public
    * @param type                instance type
    * @param availabilityZone    the zone in which to launch the instance(s)
    * @param kernelId            id of the kernel with which to launch the instance(s)
    * @param ramdiskId           id of the RAM disk with wich to launch the imstance(s)
    * @param blockDeviceMappings mappings of virtual to device names
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(String imageId, int minCount,
                                              int maxCount, List<String> groupSet, String userData, String keyName,
                                              boolean publicAddr, InstanceType type, @Nullable String availabilityZone,
                                              @Nullable String kernelId, @Nullable String ramdiskId, @Nullable List<BlockDeviceMapping> blockDeviceMappings)
           throws EC2Exception {
 
     LaunchConfiguration lc = new LaunchConfiguration(imageId);
     lc.setMinCount(minCount);
     lc.setMaxCount(maxCount);
     lc.setSecurityGroup(groupSet);
     if (userData != null) {
       lc.setUserData(userData.getBytes());
     }
     lc.setKeyName(keyName);
     lc.setInstanceType(type);
     lc.setAvailabilityZone(availabilityZone);
     lc.setKernelId(kernelId);
     lc.setRamdiskId(ramdiskId);
     lc.setBlockDevicemappings(blockDeviceMappings);
     lc.setPublicAddressing(publicAddr);
     return runInstances(lc);
   }
 
   /**
    * Requests reservation of a number of instances.
    * <p/>
    * This will begin launching those instances for which a reservation was
    * successfully obtained.
    * <p/>
    * If less than <code>minCount</code> instances are available no instances
    * will be reserved.
    *
    * @param lc object containing launch configuration
    * @return A {@link com.xerox.amazonws.ec2.ReservationDescription} describing the instances that
    *         have been reserved.
    * @throws EC2Exception wraps checked exceptions
    */
   public ReservationDescription runInstances(LaunchConfiguration lc) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     lc.prepareQueryParams("", true, params);
 
     RunInstancesResponse response = makeRequestInt(new HttpPost(), "RunInstances", params, RunInstancesResponse.class);
     return new ReservationDescription(response.getRequestId(),
             response.getOwnerId(), response.getReservationId(),
             response.getRequesterId(), response.getGroupSet(),
             response.getInstancesSet());
   }
 
   /**
    * Starts a selection of stopped instances.
    *
    * @param instanceIds An array of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<InstanceStateChangeDescription> startInstances(String[] instanceIds) throws EC2Exception {
     return this.startInstances(Arrays.asList(instanceIds));
   }
 
   /**
    * Starts a selection of stopped instances.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public List<InstanceStateChangeDescription> startInstances(List<String> instanceIds)
           throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     StartInstancesResponse response = makeRequestInt(new HttpGet(), "StartInstances", params, StartInstancesResponse.class);
     List<InstanceStateChangeDescription> res = new ArrayList<InstanceStateChangeDescription>();
     InstanceStateChangeSetType set = response.getInstancesSet();
     for (InstanceStateChangeType rsp_item : set.getItems()) {
       res.add(new InstanceStateChangeDescription(
               rsp_item.getInstanceId(), rsp_item.getPreviousState().getName(),
               rsp_item.getPreviousState().getCode(),
               rsp_item.getCurrentState().getName(),
               rsp_item.getCurrentState().getCode()));
     }
     return res;
   }
 
   /**
    * Stops a selection of running instances.
    *
    * @param instanceIds An array of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @param force       forces the instance to stop. bypasses filesystem flush. Use with caution!
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<InstanceStateChangeDescription> stopInstances(String[] instanceIds, boolean force) throws EC2Exception {
     return this.stopInstances(Arrays.asList(instanceIds), force);
   }
 
   /**
    * Stops a selection of running instances.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @param force       forces the instance to stop. bypasses filesystem flush. Use with caution!
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public List<InstanceStateChangeDescription> stopInstances(List<String> instanceIds, boolean force)
           throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     StopInstancesResponse response = makeRequestInt(new HttpGet(), "StopInstances", params, StopInstancesResponse.class);
     List<InstanceStateChangeDescription> res = new ArrayList<InstanceStateChangeDescription>();
     InstanceStateChangeSetType set = response.getInstancesSet();
     for (InstanceStateChangeType rsp_item : set.getItems()) {
       res.add(new InstanceStateChangeDescription(
               rsp_item.getInstanceId(), rsp_item.getPreviousState().getName(),
               rsp_item.getPreviousState().getCode(),
               rsp_item.getCurrentState().getName(),
               rsp_item.getCurrentState().getCode()));
     }
     return res;
   }
 
   /**
    * Terminates a selection of running instances.
    *
    * @param instanceIds An array of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<InstanceStateChangeDescription> terminateInstances(String[] instanceIds) throws EC2Exception {
     return this.terminateInstances(Arrays.asList(instanceIds));
   }
 
   /**
    * Terminates a selection of running instances.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link InstanceStateChangeDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public List<InstanceStateChangeDescription> terminateInstances(List<String> instanceIds)
           throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     TerminateInstancesResponse response =
             makeRequestInt(new HttpPost(), "TerminateInstances", params, TerminateInstancesResponse.class);
     List<InstanceStateChangeDescription> res =
             new ArrayList<InstanceStateChangeDescription>();
     InstanceStateChangeSetType set = response.getInstancesSet();
     for (InstanceStateChangeType rsp_item : set.getItems()) {
       res.add(new InstanceStateChangeDescription(
               rsp_item.getInstanceId(), rsp_item.getPreviousState().getName(),
               rsp_item.getPreviousState().getCode(),
               rsp_item.getCurrentState().getName(),
               rsp_item.getCurrentState().getCode()));
     }
     return res;
   }
 
   /**
    * Gets a list of running instances.
    * <p/>
    * If the array of instance IDs is empty then a list of all instances owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested instances only.
    *
    * @return A list of {@link com.xerox.amazonws.ec2.ReservationDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ReservationDescription> describeInstances() throws EC2Exception {
     return this.describeInstances(Collections.<String>emptyList());
   }
 
   /**
    * Gets a list of running instances.
    * <p/>
    * If the array of instance IDs is empty then a list of all instances owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested instances only.
    *
    * @param instanceIds An array of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link com.xerox.amazonws.ec2.ReservationDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ReservationDescription> describeInstances(@NotNull String[] instanceIds) throws EC2Exception {
     return this.describeInstances(Arrays.asList(instanceIds));
   }
 
   /**
    * Gets a list of running instances.
    * <p/>
    * If the list of instance IDs is empty then a list of all instances owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested instances only.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return A list of {@link com.xerox.amazonws.ec2.ReservationDescription} instances.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ReservationDescription> describeInstances(@NotNull List<String> instanceIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     DescribeInstancesResponse response = makeRequestInt(new HttpGet(), "DescribeInstances", params, DescribeInstancesResponse.class);
     List<ReservationDescription> result = new ArrayList<ReservationDescription>();
     for (ReservationInfoType item : response.getReservationSet().getItems()) {
       ReservationDescription res = new ReservationDescription(response.getRequestId(),
               item.getOwnerId(), item.getReservationId(),
               item.getRequesterId(), item.getGroupSet(),
               item.getInstancesSet());
       result.add(res);
     }
     return result;
   }
 
   /**
    * Reboot a selection of running instances.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public void rebootInstances(String[] instanceIds) throws EC2Exception {
     this.rebootInstances(Arrays.asList(instanceIds));
   }
 
   /**
    * Reboot a selection of running instances.
    *
    * @param instanceIds A list of instances ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public void rebootInstances(List<String> instanceIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     RebootInstancesResponse response =
             makeRequestInt(new HttpGet(), "RebootInstances", params, RebootInstancesResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not reboot instances. No reason given.");
     }
   }
 
   /**
    * Changes one of a variety of settings for a instance.
    *
    * @param instanceId the instance you are addressing
    * @param attribute  for now, should be (instanceType|kernel|ramdisk|userData|disableApiTermination|instanceInitatedShutdownBehavior|rootDeviceName|blockDeviceMapping)
    * @param value      value of the attribute
    * @throws EC2Exception wraps checked exceptions
    */
   public void modifyInstanceAttribute(String instanceId, String attribute, String value) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
    params.put(attribute, value);
     ModifyInstanceAttributeResponse response =
             makeRequestInt(new HttpPost(), "ModifyInstanceAttribute", params, ModifyInstanceAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not modify instance attribute : " + attribute + ". No reason given.");
     }
   }
 
   /**
    * Resets an attribute on an instance.
    *
    * @param instanceId The instance to reset the attribute on.
    * @param attribute  The attribute type to reset (kernel|ramdisk).
    * @throws EC2Exception wraps checked exceptions
    */
   public void resetInstanceAttribute(String instanceId, String attribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("Attribute", attribute);
     ResetInstanceAttributeResponse response =
             makeRequestInt(new HttpGet(), "ResetInstanceAttribute", params, ResetInstanceAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not reset instance attribute. No reason given.");
     }
   }
 
   /**
    * Describes an attribute of an instance.
    *
    * @param instanceId The instance for which the attribute is described.
    * @param attribute  The attribute to describe (createVolumePermission).
    * @return An object containing the instanceId and a list of list attribute item types and values.
    * @throws EC2Exception wraps checked exceptions
    */
   public DescribeInstanceAttributeResult describeInstanceAttribute(String instanceId,
                                                                    String attribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("Attribute", attribute);
     DescribeInstanceAttributeResponse response =
             makeRequestInt(new HttpGet(), "DescribeInstanceAttribute", params, DescribeInstanceAttributeResponse.class);
 
     return new DescribeInstanceAttributeResult(response);
   }
 
   /**
    * Get an instance's console output.
    *
    * @param instanceId An instance's id ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return {@link ConsoleOutput}
    * @throws EC2Exception wraps checked exceptions
    */
   public ConsoleOutput getConsoleOutput(String instanceId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     GetConsoleOutputResponse response =
             makeRequestInt(new HttpGet(), "GetConsoleOutput", params, GetConsoleOutputResponse.class);
     return new ConsoleOutput(response.getRequestId(), response.getInstanceId(),
             response.getTimestamp().toGregorianCalendar(),
             new String(Base64.decodeBase64(response.getOutput().getBytes())));
   }
 
   /**
    * Get a Windows instance's admin password.
    *
    * @param instanceId An instance's id ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @return password data
    * @throws EC2Exception wraps checked exceptions
    */
   public String getPasswordData(String instanceId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     GetPasswordDataResponse response =
             makeRequestInt(new HttpGet(), "GetPasswordData", params, GetPasswordDataResponse.class);
     return response.getPasswordData();
   }
 
   /**
    * Creates a security group.
    *
    * @param name The name of the security group.
    * @param desc The description of the security group.
    * @throws EC2Exception wraps checked exceptions
    */
   public void createSecurityGroup(String name, String desc) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", name);
     params.put("GroupDescription", desc);
     CreateSecurityGroupResponse response =
             makeRequestInt(new HttpGet(), "CreateSecurityGroup", params, CreateSecurityGroupResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not create security group : " + name + ". No reason given.");
     }
   }
 
   /**
    * Deletes a security group.
    *
    * @param name The name of the security group.
    * @throws EC2Exception wraps checked exceptions
    */
   public void deleteSecurityGroup(String name) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", name);
     DeleteSecurityGroupResponse response =
             makeRequestInt(new HttpGet(), "DeleteSecurityGroup", params, DeleteSecurityGroupResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not delete security group : " + name + ". No reason given.");
     }
   }
 
   /**
    * Gets a list of security groups and their associated permissions.
    *
    * @return A list of groups ({@link GroupDescription}.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<GroupDescription> describeSecurityGroups() throws EC2Exception {
     return describeSecurityGroups(Collections.<String>emptyList());
   }
 
   /**
    * Gets a list of security groups and their associated permissions.
    *
    * @param groupNames An array of groups to describe.
    * @return A list of groups ({@link GroupDescription}.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<GroupDescription> describeSecurityGroups(String[] groupNames)
           throws EC2Exception {
     return describeSecurityGroups(Arrays.asList(groupNames));
   }
 
   /**
    * Gets a list of security groups and their associated permissions.
    *
    * @param groupNames A list of groups to describe.
    * @return A list of groups ({@link GroupDescription}.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<GroupDescription> describeSecurityGroups(List<String> groupNames) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < groupNames.size(); i++) {
       params.put("GroupName." + (i + 1), groupNames.get(i));
     }
     final DescribeSecurityGroupsResponse response = makeRequestInt(new HttpGet(), "DescribeSecurityGroups", params, DescribeSecurityGroupsResponse.class);
 
     final List<GroupDescription> result = new ArrayList<GroupDescription>();
     final SecurityGroupSetType rsp_set = response.getSecurityGroupInfo();
     for (SecurityGroupItemType item : rsp_set.getItems()) {
       final GroupDescription group = new GroupDescription(item.getGroupId(), item.getGroupName(), item.getGroupDescription(), item.getOwnerId(), item.getVpcId());
       for (IpPermissionType perm : item.getIpPermissions().getItems()) {
         GroupDescription.IpPermission group_perms = group.addPermission(perm.getIpProtocol(), perm.getFromPort(), perm.getToPort());
 
         for (UserIdGroupPairType uid_group : perm.getGroups().getItems()) {
           group_perms.addUserGroupPair(uid_group.getUserId(), uid_group.getGroupName());
         }
         for (IpRangeItemType range : perm.getIpRanges().getItems()) {
           group_perms.addIpRange(range.getCidrIp());
         }
       }
       result.add(group);
     }
     return result;
   }
 
   /**
    * Adds incoming permissions to a security group.
    *
    * @param groupName       name of group to modify
    * @param secGroupName    name of security group to authorize access to
    * @param secGroupOwnerId owner of security group to authorize access to
    * @throws EC2Exception wraps checked exceptions
    */
   public void authorizeSecurityGroupIngress(String groupName, String secGroupName,
                                             String secGroupOwnerId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", groupName);
     params.put("SourceSecurityGroupOwnerId", secGroupOwnerId);
     params.put("SourceSecurityGroupName", secGroupName);
     AuthorizeSecurityGroupIngressResponse response =
             makeRequestInt(new HttpGet(), "AuthorizeSecurityGroupIngress", params, AuthorizeSecurityGroupIngressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not authorize security ingress : " + groupName + ". No reason given.");
     }
   }
 
   /**
    * Adds incoming permissions to a security group.
    *
    * @param groupName  name of group to modify
    * @param ipProtocol protocol to authorize (tcp, udp, icmp)
    * @param fromPort   bottom of port range to authorize
    * @param toPort     top of port range to authorize
    * @param cidrIp     CIDR IP range to authorize (i.e. 0.0.0.0/0)
    * @throws EC2Exception wraps checked exceptions
    */
   public void authorizeSecurityGroupIngress(String groupName, String ipProtocol,
                                             int fromPort, int toPort,
                                             String cidrIp) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", groupName);
     params.put("IpProtocol", ipProtocol);
     params.put("FromPort", "" + fromPort);
     params.put("ToPort", "" + toPort);
     params.put("CidrIp", cidrIp);
     AuthorizeSecurityGroupIngressResponse response =
             makeRequestInt(new HttpGet(), "AuthorizeSecurityGroupIngress", params, AuthorizeSecurityGroupIngressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not authorize security ingress : " + groupName + ". No reason given.");
     }
   }
 
   /**
    * Revokes incoming permissions from a security group.
    *
    * @param groupName       name of group to modify
    * @param secGroupName    name of security group to revoke access from
    * @param secGroupOwnerId owner of security group to revoke access from
    * @throws EC2Exception wraps checked exceptions
    */
   public void revokeSecurityGroupIngress(String groupName, String secGroupName,
                                          String secGroupOwnerId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", groupName);
     params.put("SourceSecurityGroupOwnerId", secGroupOwnerId);
     params.put("SourceSecurityGroupName", secGroupName);
     RevokeSecurityGroupIngressResponse response =
             makeRequestInt(new HttpGet(), "RevokeSecurityGroupIngress", params, RevokeSecurityGroupIngressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not revoke security ingress : " + groupName + ". No reason given.");
     }
   }
 
   /**
    * Revokes incoming permissions from a security group.
    *
    * @param groupName  name of group to modify
    * @param ipProtocol protocol to revoke (tcp, udp, icmp)
    * @param fromPort   bottom of port range to revoke
    * @param toPort     top of port range to revoke
    * @param cidrIp     CIDR IP range to revoke (i.e. 0.0.0.0/0)
    * @throws EC2Exception wraps checked exceptions
    */
   public void revokeSecurityGroupIngress(String groupName, String ipProtocol,
                                          int fromPort, int toPort,
                                          String cidrIp) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GroupName", groupName);
     params.put("IpProtocol", ipProtocol);
     params.put("FromPort", "" + fromPort);
     params.put("ToPort", "" + toPort);
     params.put("CidrIp", cidrIp);
     RevokeSecurityGroupIngressResponse response =
             makeRequestInt(new HttpGet(), "RevokeSecurityGroupIngress", params, RevokeSecurityGroupIngressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not revoke security ingress : " + groupName + ". No reason given.");
     }
   }
 
 
   /**
    * Creates a public/private keypair.
    *
    * @param keyName Name of the keypair.
    * @return A keypair description ({@link KeyPairInfo}).
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public KeyPairInfo createKeyPair(String keyName) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("KeyName", keyName);
     CreateKeyPairResponse response = makeRequestInt(new HttpGet(), "CreateKeyPair", params, CreateKeyPairResponse.class);
     return new KeyPairInfo(response.getKeyName(),
             response.getKeyFingerprint(),
             response.getKeyMaterial());
   }
 
   /**
    * Lists public/private keypairs.
    *
    * @param keyIds An array of keypairs.
    * @return A list of keypair descriptions ({@link KeyPairInfo}).
    * @throws EC2Exception wraps checked exceptions
    */
   public List<KeyPairInfo> describeKeyPairs(String[] keyIds) throws EC2Exception {
     return describeKeyPairs(Arrays.asList(keyIds));
   }
 
   /**
    * Lists public/private keypairs. NOTE: the KeyPairInfo.getMaterial() method will return null
    * because this API call doesn't return the keypair material.
    *
    * @param keyIds A list of keypairs.
    * @return A list of keypair descriptions ({@link KeyPairInfo}).
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public List<KeyPairInfo> describeKeyPairs(List<String> keyIds)
           throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < keyIds.size(); i++) {
       params.put("KeyName." + (i + 1), keyIds.get(i));
     }
     HttpGet method = new HttpGet();
     DescribeKeyPairsResponse response =
             makeRequestInt(method, "DescribeKeyPairs", params, DescribeKeyPairsResponse.class);
     List<KeyPairInfo> result = new ArrayList<KeyPairInfo>();
     DescribeKeyPairsResponseInfoType set = response.getKeySet();
     for (DescribeKeyPairsResponseItemType item : set.getItems()) {
       result.add(new KeyPairInfo(item.getKeyName(), item.getKeyFingerprint(), null));
     }
     return result;
   }
 
   /**
    * Deletes a public/private keypair.
    *
    * @param keyName Name of the keypair.
    * @throws EC2Exception wraps checked exceptions
    *                      TODO: need to return request id
    */
   public void deleteKeyPair(String keyName) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("KeyName", keyName);
     DeleteKeyPairResponse response =
             makeRequestInt(new HttpGet(), "DeleteKeyPair", params, DeleteKeyPairResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not delete keypair : " + keyName + ". No reason given.");
     }
   }
 
   /**
    * Enumerates image list attribute operation types.
    */
   public enum ImageListAttributeOperationType {
     add,
     remove
   }
 
   /**
    * Modifies an attribute by the given items with the given operation.
    *
    * @param imageId       The ID of the AMI to modify the attributes for.
    * @param attribute     The name of the attribute to change.
    * @param operationType The name of the operation to change. May be add or remove.
    * @throws EC2Exception wraps checked exceptions
    */
   public void modifyImageAttribute(String imageId, ImageListAttribute attribute,
                                    ImageListAttributeOperationType operationType) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("ImageId", imageId);
     if (attribute.getType().equals(ImageAttribute.ImageAttributeType.launchPermission)) {
       params.put("Attribute", "launchPermission");
       switch (operationType) {
         case add:
           params.put("OperationType", "add");
           break;
         case remove:
           params.put("OperationType", "remove");
           break;
         default:
           throw new IllegalArgumentException("Unknown attribute operation.");
       }
     } else if (attribute.getType().equals(ImageAttribute.ImageAttributeType.productCodes)) {
       params.put("Attribute", "productCodes");
     }
 
     int gNum = 1;
     int iNum = 1;
     int pNum = 1;
     for (ImageListAttributeItem item : attribute.getImageListAttributeItems()) {
       switch (item.getType()) {
         case group:
           params.put("UserGroup." + gNum, item.getValue());
           gNum++;
           break;
         case userId:
           params.put("UserId." + iNum, item.getValue());
           iNum++;
           break;
         case productCode:
           params.put("ProductCode." + pNum, item.getValue());
           pNum++;
           break;
         default:
           throw new IllegalArgumentException("Unknown item type.");
       }
     }
     ModifyImageAttributeResponse response =
             makeRequestInt(new HttpGet(), "ModifyImageAttribute", params, ModifyImageAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not reset image attribute. No reason given.");
     }
   }
 
   /**
    * Resets an attribute on an AMI.
    *
    * @param imageId        The AMI to reset the attribute on.
    * @param imageAttribute The attribute type to reset.
    * @throws EC2Exception wraps checked exceptions
    */
   public void resetImageAttribute(String imageId, ImageAttribute.ImageAttributeType imageAttribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("ImageId", imageId);
     if (imageAttribute.equals(ImageAttribute.ImageAttributeType.launchPermission)) {
       params.put("Attribute", "launchPermission");
     } else if (imageAttribute.equals(ImageAttribute.ImageAttributeType.productCodes)) {
       throw new IllegalArgumentException("Cannot reset productCodes attribute");
     }
     ResetImageAttributeResponse response =
             makeRequestInt(new HttpGet(), "ResetImageAttribute", params, ResetImageAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not reset image attribute. No reason given.");
     }
   }
 
   /**
    * Describes an attribute of an AMI.
    *
    * @param imageId        The AMI for which the attribute is described.
    * @param imageAttribute The attribute type to describe.
    * @return An object containing the imageId and a list of list attribute item types and values.
    * @throws EC2Exception wraps checked exceptions
    */
   public DescribeImageAttributeResult describeImageAttribute(String imageId,
                                                              ImageAttribute.ImageAttributeType imageAttribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("ImageId", imageId);
     if (imageAttribute.equals(ImageAttribute.ImageAttributeType.launchPermission)) {
       params.put("Attribute", "launchPermission");
     } else if (imageAttribute.equals(ImageAttribute.ImageAttributeType.productCodes)) {
       params.put("Attribute", "productCodes");
     }
     DescribeImageAttributeResponse response =
             makeRequestInt(new HttpGet(), "DescribeImageAttribute", params, DescribeImageAttributeResponse.class);
     ImageListAttribute attribute = null;
     if (response.getLaunchPermission() != null) {
       LaunchPermissionListType list = response.getLaunchPermission();
       attribute = new LaunchPermissionAttribute();
       for (LaunchPermissionItemType item : list.getItems()) {
         if (item.getGroup() != null) {
           attribute.addImageListAttributeItem(ImageListAttribute.ImageListAttributeItemType.group, item.getGroup());
         } else if (item.getUserId() != null) {
           attribute.addImageListAttributeItem(ImageListAttribute.ImageListAttributeItemType.userId, item.getUserId());
         }
       }
     } else if (response.getProductCodes() != null) {
       ProductCodeListType list = response.getProductCodes();
       attribute = new ProductCodesAttribute();
       for (ProductCodeItemType item : list.getItems()) {
         if (item.getProductCode() != null) {
           attribute.addImageListAttributeItem(ImageListAttribute.ImageListAttributeItemType.productCode,
                   item.getProductCode());
         }
       }
     }
     ArrayList<String> codes = new ArrayList<String>();
     ProductCodeListType set = response.getProductCodes();
     if (set != null) {
       for (ProductCodeItemType code : set.getItems()) {
         codes.add(code.getProductCode());
       }
     }
     NullableAttributeValueType val = response.getKernel();
     String kernel = (val != null) ? val.getValue() : "";
     val = response.getRamdisk();
     String ramdisk = (val != null) ? val.getValue() : "";
     ArrayList<BlockDeviceMapping> bdm = new ArrayList<BlockDeviceMapping>();
     BlockDeviceMappingType bdmSet = response.getBlockDeviceMapping();
     if (bdmSet != null) {
       for (BlockDeviceMappingItemType mapping : bdmSet.getItems()) {
         bdm.add(new BlockDeviceMapping(mapping.getVirtualName(), mapping.getDeviceName()));
       }
     }
 
     return new DescribeImageAttributeResult(response.getImageId(), attribute, codes, kernel, ramdisk, bdm);
   }
 
   /**
    * Returns true if the productCode is associated with the instance.
    *
    * @param instanceId  An instance's id ({@link com.xerox.amazonws.ec2.ReservationDescription.Instance#instanceId}.
    * @param productCode the code for the project you registered with AWS
    * @return null if no relationship exists, otherwise information about the owner
    * @throws EC2Exception wraps checked exceptions
    */
   public ProductInstanceInfo confirmProductInstance(String instanceId, String productCode) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("ProductCode", productCode);
     ConfirmProductInstanceResponse response =
             makeRequestInt(new HttpGet(), "ConfirmProductInstance", params, ConfirmProductInstanceResponse.class);
     if (response.isReturn()) {
       return new ProductInstanceInfo(instanceId, productCode, response.getOwnerId());
     } else return null;
   }
 
   /**
    * Returns a list of availability zones and their status.
    *
    * @param zones a list of zones to limit the results, or null
    * @return a list of zones and their availability
    * @throws EC2Exception wraps checked exceptions
    */
   public List<AvailabilityZone> describeAvailabilityZones(List<String> zones) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (zones != null && zones.size() > 0) {
       for (int i = 0; i < zones.size(); i++) {
         params.put("ZoneName." + (i + 1), zones.get(i));
       }
     }
     DescribeAvailabilityZonesResponse response =
             makeRequestInt(new HttpGet(), "DescribeAvailabilityZones", params, DescribeAvailabilityZonesResponse.class);
     List<AvailabilityZone> ret = new ArrayList<AvailabilityZone>();
     AvailabilityZoneSetType set = response.getAvailabilityZoneInfo();
     for (AvailabilityZoneItemType item : set.getItems()) {
       List<String> messages = new ArrayList<String>();
       for (AvailabilityZoneMessageType msg : item.getMessageSet().getItems()) {
         messages.add(msg.getMessage());
       }
       ret.add(new AvailabilityZone(item.getZoneName(), item.getZoneState(), item.getRegionName(), messages));
     }
     return ret;
   }
 
   /**
    * Returns a list of addresses associated with this account.
    *
    * @param addresses a list of zones to limit the results, or null
    * @return a list of addresses and their associated instance
    * @throws EC2Exception wraps checked exceptions
    */
   public List<AddressInfo> describeAddresses(List<String> addresses) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (addresses != null && addresses.size() > 0) {
       for (int i = 0; i < addresses.size(); i++) {
         params.put("PublicIp." + (i + 1), addresses.get(i));
       }
     }
     DescribeAddressesResponse response =
             makeRequestInt(new HttpGet(), "DescribeAddresses", params, DescribeAddressesResponse.class);
     List<AddressInfo> ret = new ArrayList<AddressInfo>();
     DescribeAddressesResponseInfoType set = response.getAddressesSet();
     for (DescribeAddressesResponseItemType item : set.getItems()) {
       ret.add(new AddressInfo(item.getPublicIp(), item.getInstanceId()));
     }
     return ret;
   }
 
   /**
    * Allocates an address for this account.
    *
    * @return the new address allocated
    * @throws EC2Exception wraps checked exceptions
    */
   public String allocateAddress() throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     AllocateAddressResponse response = makeRequestInt(new HttpGet(), "AllocateAddress", params, AllocateAddressResponse.class);
     return response.getPublicIp();
   }
 
   /**
    * Associates an address with an instance.
    *
    * @param instanceId the instance
    * @param publicIp   the ip address to associate
    * @throws EC2Exception wraps checked exceptions
    */
   public void associateAddress(String instanceId, String publicIp) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("PublicIp", publicIp);
     AssociateAddressResponse response =
             makeRequestInt(new HttpGet(), "AssociateAddress", params, AssociateAddressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not associate address with instance (no reason given).");
     }
   }
 
   /**
    * Disassociates an address with an instance.
    *
    * @param publicIp the ip address to disassociate
    * @throws EC2Exception wraps checked exceptions
    */
   public void disassociateAddress(String publicIp) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("PublicIp", publicIp);
     DisassociateAddressResponse response =
             makeRequestInt(new HttpGet(), "DisassociateAddress", params, DisassociateAddressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not disassociate address with instance (no reason given).");
     }
   }
 
   /**
    * Releases an address
    *
    * @param publicIp the ip address to release
    * @throws EC2Exception wraps checked exceptions
    */
   public void releaseAddress(String publicIp) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("PublicIp", publicIp);
     ReleaseAddressResponse response =
             makeRequestInt(new HttpGet(), "ReleaseAddress", params, ReleaseAddressResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not release address (no reason given).");
     }
   }
 
   /**
    * Creates an EBS volume either by size, or from a snapshot. The zone must be the same as
    * that of the instance you wish to attach it to.
    *
    * @param size       the size of the volume in gigabytes
    * @param snapshotId the snapshot from which to create the new volume
    * @param zoneName   the availability zone for the new volume
    * @return information about the volume
    * @throws EC2Exception wraps checked exceptions
    */
   public VolumeInfo createVolume(String size, String snapshotId, String zoneName) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (size != null && !size.equals("")) {
       params.put("Size", size);
     }
     if (snapshotId != null && !snapshotId.equals("")) {
       params.put("SnapshotId", snapshotId);
     }
     params.put("AvailabilityZone", zoneName);
     CreateVolumeResponse response =
             makeRequestInt(new HttpGet(), "CreateVolume", params, CreateVolumeResponse.class);
     return new VolumeInfo(response.getVolumeId(), response.getSize(),
             response.getSnapshotId(), response.getAvailabilityZone(), response.getStatus(),
             response.getCreateTime().toGregorianCalendar());
   }
 
   /**
    * Deletes the EBS volume.
    *
    * @param volumeId the id of the volume to be deleted
    * @throws EC2Exception wraps checked exceptions
    */
   public void deleteVolume(String volumeId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("VolumeId", volumeId);
     DeleteVolumeResponse response =
             makeRequestInt(new HttpGet(), "DeleteVolume", params, DeleteVolumeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not release delete volume (no reason given).");
     }
   }
 
   /**
    * Gets a list of EBS volumes for this account.
    * <p/>
    * If the array of volume IDs is empty then a list of all volumes owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested volumes only.
    *
    * @param volumeIds An array of volumes ({@link com.xerox.amazonws.ec2.VolumeInfo}.
    * @return A list of {@link com.xerox.amazonws.ec2.VolumeInfo} volumes.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<VolumeInfo> describeVolumes(String[] volumeIds) throws EC2Exception {
     return this.describeVolumes(Arrays.asList(volumeIds));
   }
 
   /**
    * Gets a list of EBS volumes for this account.
    * <p/>
    * If the list of volume IDs is empty then a list of all volumes owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested volumes only.
    *
    * @param volumeIds A list of volumes ({@link com.xerox.amazonws.ec2.VolumeInfo}.
    * @return A list of {@link com.xerox.amazonws.ec2.VolumeInfo} volumes.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<VolumeInfo> describeVolumes(List<String> volumeIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < volumeIds.size(); i++) {
       params.put("VolumeId." + (i + 1), volumeIds.get(i));
     }
     DescribeVolumesResponse response =
             makeRequestInt(new HttpGet(), "DescribeVolumes", params, DescribeVolumesResponse.class);
     List<VolumeInfo> result = new ArrayList<VolumeInfo>();
     DescribeVolumesSetResponseType res_set = response.getVolumeSet();
     for (DescribeVolumesSetItemResponseType item : res_set.getItems()) {
       VolumeInfo vol = new VolumeInfo(item.getVolumeId(), item.getSize(),
               item.getSnapshotId(), item.getAvailabilityZone(), item.getStatus(),
               item.getCreateTime().toGregorianCalendar());
       AttachmentSetResponseType set = item.getAttachmentSet();
       for (AttachmentSetItemResponseType as_item : set.getItems()) {
         vol.addAttachmentInfo(as_item.getVolumeId(),
                 as_item.getInstanceId(),
                 as_item.getDevice(),
                 as_item.getStatus(),
                 as_item.getAttachTime().toGregorianCalendar());
       }
       result.add(vol);
     }
     return result;
   }
 
   /**
    * Attaches an EBS volume to an instance.
    *
    * @param volumeId   the id of the volume
    * @param instanceId the id of the instance
    * @param device     the device name for the attached volume
    * @return the information about this attachment
    * @throws EC2Exception wraps checked exceptions
    */
   public AttachmentInfo attachVolume(String volumeId, String instanceId, String device) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("VolumeId", volumeId);
     params.put("InstanceId", instanceId);
     params.put("Device", device);
     AttachVolumeResponse response =
             makeRequestInt(new HttpGet(), "AttachVolume", params, AttachVolumeResponse.class);
     return new AttachmentInfo(response.getVolumeId(), response.getInstanceId(),
             response.getDevice(), response.getStatus(),
             response.getAttachTime().toGregorianCalendar());
   }
 
   /**
    * Detaches an EBS volume from an instance.
    *
    * @param volumeId   the id of the volume
    * @param instanceId the id of the instance
    * @param device     the device name for the attached volume
    * @param force      if true, forces the detachment, only use if normal detachment fails
    * @return the information about this attachment
    * @throws EC2Exception wraps checked exceptions
    */
   public AttachmentInfo detachVolume(String volumeId, String instanceId, String device, boolean force) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("VolumeId", volumeId);
     params.put("InstanceId", (instanceId == null) ? "" : instanceId);
     if (device != null && !device.equals("")) {
       params.put("Device", device);
     }
     params.put("Force", force ? "true" : "false");
     DetachVolumeResponse response =
             makeRequestInt(new HttpGet(), "DetachVolume", params, DetachVolumeResponse.class);
     return new AttachmentInfo(response.getVolumeId(), response.getInstanceId(),
             response.getDevice(), response.getStatus(),
             response.getAttachTime().toGregorianCalendar());
   }
 
   /**
    * Creates a snapshot of the EBS Volume.
    *
    * @param volumeId    the id of the volume
    * @param description an optional descriptive string (256 chars max)
    * @return information about the snapshot
    * @throws EC2Exception wraps checked exceptions
    */
   public SnapshotInfo createSnapshot(String volumeId, String description) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("VolumeId", volumeId);
     params.put("Description", description);
     CreateSnapshotResponse response =
             makeRequestInt(new HttpGet(), "CreateSnapshot", params, CreateSnapshotResponse.class);
     return new SnapshotInfo(response.getSnapshotId(), response.getVolumeId(),
             response.getStatus(),
             response.getStartTime().toGregorianCalendar(),
             response.getProgress(), response.getOwnerId(),
             response.getVolumeSize(), response.getDescription(),
             null);
   }
 
   /**
    * Deletes the snapshot.
    *
    * @param snapshotId the id of the snapshot
    * @throws EC2Exception wraps checked exceptions
    */
   public void deleteSnapshot(String snapshotId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("SnapshotId", snapshotId);
     DeleteSnapshotResponse response =
             makeRequestInt(new HttpGet(), "DeleteSnapshot", params, DeleteSnapshotResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not release delete snapshot (no reason given).");
     }
   }
 
   /**
    * Gets a list of EBS snapshots for this account.
    * <p/>
    * If the array of snapshot IDs is empty then a list of all snapshots owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested snapshots only.
    *
    * @param snapshotIds An array of snapshots ({@link com.xerox.amazonws.ec2.SnapshotInfo}.
    * @return A list of {@link com.xerox.amazonws.ec2.VolumeInfo} volumes.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<SnapshotInfo> describeSnapshots(String[] snapshotIds) throws EC2Exception {
     return this.describeSnapshots(Arrays.asList(snapshotIds));
   }
 
   /**
    * Gets a list of EBS snapshots for this account.
    * <p/>
    * If the list of snapshot IDs is empty then a list of all snapshots owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested snapshots only.
    *
    * @param snapshotIds A list of snapshots ({@link com.xerox.amazonws.ec2.SnapshotInfo}.
    * @return A list of {@link com.xerox.amazonws.ec2.VolumeInfo} volumes.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<SnapshotInfo> describeSnapshots(List<String> snapshotIds) throws EC2Exception {
     return describeSnapshots(snapshotIds, null, null);
   }
 
   /**
    * Gets a list of EBS snapshots for this account.
    * <p/>
    * If the list of snapshot IDs is empty then a list of all snapshots owned
    * by the caller will be returned. Otherwise the list will contain
    * information for the requested snapshots only.
    *
    * @param snapshotIds  A list of snapshots ({@link com.xerox.amazonws.ec2.SnapshotInfo}.
    * @param owner        limits results to snapshots owned by this user
    * @param restorableBy limits results to account that can create volumes from this snapshot
    * @return A list of {@link com.xerox.amazonws.ec2.VolumeInfo} volumes.
    * @throws EC2Exception wraps checked exceptions
    */
   public List<SnapshotInfo> describeSnapshots(List<String> snapshotIds,
                                               String owner, String restorableBy) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < snapshotIds.size(); i++) {
       params.put("SnapshotId." + (i + 1), snapshotIds.get(i));
     }
     if (owner != null) {
       params.put("Owner", owner);
     }
     if (restorableBy != null) {
       params.put("RestorableBy", owner);
     }
     DescribeSnapshotsResponse response =
             makeRequestInt(new HttpGet(), "DescribeSnapshots", params, DescribeSnapshotsResponse.class);
     List<SnapshotInfo> result = new ArrayList<SnapshotInfo>();
     DescribeSnapshotsSetResponseType res_set = response.getSnapshotSet();
     for (DescribeSnapshotsSetItemResponseType item : res_set.getItems()) {
       result.add(new SnapshotInfo(item.getSnapshotId(), item.getVolumeId(),
               item.getStatus(),
               item.getStartTime().toGregorianCalendar(),
               item.getProgress(), item.getOwnerId(),
               item.getVolumeSize(), item.getDescription(),
               item.getOwnerAlias()));
     }
     return result;
   }
 
   /**
    * Changes permissions settings of a snapshot.
    *
    * @param snapshotId the snapshot you are addressing
    * @param attribute  for now, should be "createVolumePermission"
    * @param opType     either add or remove
    * @param userId     optional userId (this or userGroup);
    * @param userGroup  optional userGroup (this or userId)
    * @throws EC2Exception wraps checked exceptions
    */
   public void modifySnapshotAttribute(String snapshotId, String attribute,
                                       OperationType opType, String userId,
                                       String userGroup) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("SnapshotId", snapshotId);
     if (userId != null) {
       params.put("UserId", userId);
     }
     if (userGroup != null) {
       params.put("UserGroup", userGroup);
     }
     params.put("Attribute", attribute);
     params.put("OperationType", opType.getTypeId());
     ModifySnapshotAttributeResponse response =
             makeRequestInt(new HttpGet(), "ModifySnapshotAttribute", params, ModifySnapshotAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not modify snapshot attribute : " + attribute + ". No reason given.");
     }
   }
 
   /**
    * Resets an attribute on a snapshot.
    *
    * @param snapshotId The snapshot to reset the attribute on.
    * @param attribute  The attribute to reset (currently just "createVolumePermission").
    * @throws EC2Exception wraps checked exceptions
    */
   public void resetSnapshotAttribute(String snapshotId, String attribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("SnapshotId", snapshotId);
     params.put("Attribute", attribute);
     ResetSnapshotAttributeResponse response =
             makeRequestInt(new HttpGet(), "ResetSnapshotAttribute", params, ResetSnapshotAttributeResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not reset snapshot attribute. No reason given.");
     }
   }
 
   /**
    * Describes an attribute of a snapshot.
    *
    * @param snapshotId The snapshot for which the attribute is described.
    * @param attribute  The attribute to describe (createVolumePermission).
    * @return An object containing the snapshotId and a list of list attribute item types and values.
    * @throws EC2Exception wraps checked exceptions
    */
   public DescribeSnapshotAttributeResult describeSnapshotAttribute(String snapshotId,
                                                                    String attribute) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("SnapshotId", snapshotId);
     params.put("Attribute", attribute);
     DescribeSnapshotAttributeResponse response =
             makeRequestInt(new HttpGet(), "DescribeSnapshotAttribute", params, DescribeSnapshotAttributeResponse.class);
 
     DescribeSnapshotAttributeResult ret = new DescribeSnapshotAttributeResult(response.getSnapshotId());
     List<CreateVolumePermissionItemType> list = response.getCreateVolumePermission().getItems();
     if (list != null) {
       for (CreateVolumePermissionItemType item : list) {
         ret.addCreateVolumePermission(item.getUserId(), item.getGroup());
       }
     }
 
     return ret;
   }
 
   /**
    * Returns a list of regions
    *
    * @param regions a list of regions to limit the results, or null
    * @return a list of regions and endpoints
    * @throws EC2Exception wraps checked exceptions
    */
   public List<RegionInfo> describeRegions(List<String> regions) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (regions != null && regions.size() > 0) {
       for (int i = 0; i < regions.size(); i++) {
         params.put("Region." + (i + 1), regions.get(i));
       }
     }
     DescribeRegionsResponse response =
             makeRequestInt(new HttpGet(), "DescribeRegions", params, DescribeRegionsResponse.class);
     List<RegionInfo> ret = new ArrayList<RegionInfo>();
     RegionSetType set = response.getRegionInfo();
     for (RegionItemType item : set.getItems()) {
       ret.add(new RegionInfo(item.getRegionName(), item.getRegionEndpoint()));
     }
     return ret;
   }
 
   /**
    * Sets the region to use.
    *
    * @param region the region to use, from describeRegions()
    */
   public void setRegion(RegionInfo region) {
     setServer(region.getUrl());
   }
 
   /**
    * Sets the region Url to use.
    *
    * @param regionUrl the region Url to use from RegionInfo.getUrl()
    */
   public void setRegionUrl(String regionUrl) {
     setServer(regionUrl);
   }
 
   /**
    * Initiates bundling of an instance running Windows.
    *
    * @param instanceId the Id of the instance to bundle
    * @param accessId   the accessId of the owner of the S3 bucket
    * @param bucketName the name of the S3 bucket in which the AMi will be stored
    * @param prefix     the prefix to append to the AMI
    * @param policy     an UploadPolicy object containing policy parameters
    * @return information about the bundle task
    * @throws EC2Exception wraps checked exceptions
    */
   public BundleInstanceInfo bundleInstance(String instanceId, String accessId, String bucketName, String prefix, UploadPolicy policy) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("InstanceId", instanceId);
     params.put("Storage.S3.AWSAccessKeyId", accessId);
     params.put("Storage.S3.Bucket", bucketName);
     params.put("Storage.S3.Prefix", prefix);
     String jsonPolicy = policy.getPolicyString();
     params.put("Storage.S3.UploadPolicy", jsonPolicy);
     params.put("Storage.S3.UploadPolicySignature", encode(getSecretAccessKey(), jsonPolicy, false, "HmacSHA1"));
     BundleInstanceResponse response =
             makeRequestInt(new HttpGet(), "BundleInstance", params, BundleInstanceResponse.class);
     BundleInstanceTaskType task = response.getBundleInstanceTask();
     return new BundleInstanceInfo(response.getRequestId(), task.getInstanceId(), task.getBundleId(),
             task.getState(), task.getStartTime().toGregorianCalendar(),
             task.getUpdateTime().toGregorianCalendar(), task.getStorage(),
             task.getProgress(), task.getError());
   }
 
   /**
    * Cancel a bundling operation.
    *
    * @param bundleId the Id of the bundle task to cancel
    * @return information about the cancelled task
    * @throws EC2Exception wraps checked exceptions
    */
   public BundleInstanceInfo cancelBundleInstance(String bundleId) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("BundleId", bundleId);
     CancelBundleTaskResponse response =
             makeRequestInt(new HttpGet(), "CancelBundleTask", params, CancelBundleTaskResponse.class);
     BundleInstanceTaskType task = response.getBundleInstanceTask();
     return new BundleInstanceInfo(response.getRequestId(), task.getInstanceId(), task.getBundleId(),
             task.getState(), task.getStartTime().toGregorianCalendar(),
             task.getUpdateTime().toGregorianCalendar(), task.getStorage(),
             task.getProgress(), task.getError());
   }
 
   /**
    * Returns a list of current bundling tasks. An empty array causes all tasks to be returned.
    *
    * @param bundleIds the Ids of the bundle task to describe
    * @return information about the cancelled task
    * @throws EC2Exception wraps checked exceptions
    */
   public List<BundleInstanceInfo> describeBundleTasks(String[] bundleIds) throws EC2Exception {
     return this.describeBundleTasks(Arrays.asList(bundleIds));
   }
 
   /**
    * Returns a list of current bundling tasks. An empty list causes all tasks to be returned.
    *
    * @param bundleIds the Ids of the bundle task to describe
    * @return information about the cancelled task
    * @throws EC2Exception wraps checked exceptions
    */
   public List<BundleInstanceInfo> describeBundleTasks(List<String> bundleIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < bundleIds.size(); i++) {
       params.put("BundleId." + (i + 1), bundleIds.get(i));
     }
     DescribeBundleTasksResponse response =
             makeRequestInt(new HttpGet(), "DescribeBundleTasks", params, DescribeBundleTasksResponse.class);
     List<BundleInstanceInfo> ret = new ArrayList<BundleInstanceInfo>();
     for (BundleInstanceTaskType task : response.getBundleInstanceTasksSet().getItems()) {
       ret.add(new BundleInstanceInfo(response.getRequestId(), task.getInstanceId(), task.getBundleId(),
               task.getState(), task.getStartTime().toGregorianCalendar(),
               task.getUpdateTime().toGregorianCalendar(), task.getStorage(),
               task.getProgress(), task.getError()));
     }
     return ret;
   }
 
   /**
    * Returns a list of Reserved Instance offerings that are available for purchase.
    *
    * @param instanceIds specific reserved instance offering ids to return
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ReservedInstances> describeReservedInstances(List<String> instanceIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (instanceIds != null) {
       for (int i = 0; i < instanceIds.size(); i++) {
         params.put("ReservedInstanceId." + (i + 1), instanceIds.get(i));
       }
     }
     DescribeReservedInstancesResponse response =
             makeRequestInt(new HttpGet(), "DescribeReservedInstances", params, DescribeReservedInstancesResponse.class);
     List<ReservedInstances> ret = new ArrayList<ReservedInstances>();
     for (DescribeReservedInstancesResponseSetItemType type : response.getReservedInstancesSet().getItems()) {
       ret.add(new ReservedInstances(type.getReservedInstancesId(),
               InstanceType.getTypeFromString(type.getInstanceType()),
               type.getAvailabilityZone(), type.getStart().toGregorianCalendar(),
               type.getDuration(), type.getFixedPrice(), type.getUsagePrice(),
               type.getProductDescription(),
               type.getInstanceCount().intValue(), type.getState()));
     }
     return ret;
   }
 
   /**
    * Returns a list of Reserved Instance offerings that are available for purchase.
    *
    * @param offeringIds        specific reserved instance offering ids to return
    * @param instanceType       the type of instance offering to be returned
    * @param availabilityZone   the availability zone to get offerings for
    * @param productDescription limit results to those with a matching product description
    * @return a list of product descriptions
    * @throws EC2Exception wraps checked exceptions
    */
   public List<ProductDescription> describeReservedInstancesOfferings(List<String> offeringIds,
                                                                      InstanceType instanceType, String availabilityZone,
                                                                      String productDescription) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (offeringIds != null) {
       for (int i = 0; i < offeringIds.size(); i++) {
         params.put("ReservedInstancesOfferingId." + (i + 1), offeringIds.get(i));
       }
     }
     if (instanceType != null) {
       params.put("InstanceType", instanceType.getTypeId());
     }
     if (availabilityZone != null) {
       params.put("AvailabilityZone", availabilityZone);
     }
     if (productDescription != null) {
       params.put("ProductDescription", productDescription);
     }
     DescribeReservedInstancesOfferingsResponse response =
             makeRequestInt(new HttpGet(), "DescribeReservedInstancesOfferings", params, DescribeReservedInstancesOfferingsResponse.class);
     List<ProductDescription> ret = new ArrayList<ProductDescription>();
     for (DescribeReservedInstancesOfferingsResponseSetItemType type : response.getReservedInstancesOfferingsSet().getItems()) {
       ret.add(new ProductDescription(type.getReservedInstancesOfferingId(),
               InstanceType.getTypeFromString(type.getInstanceType()),
               type.getAvailabilityZone(),
               type.getDuration(), type.getFixedPrice(), type.getUsagePrice(),
               type.getProductDescription()));
     }
     return ret;
   }
 
   /**
    * This method purchases a reserved instance offering.
    * <p/>
    * NOTE: Use With Caution!!! This can cost a lot of money!
    *
    * @param offeringId    the id of the offering to purchase
    * @param instanceCount the number of instances to reserve
    * @return id of reserved instances
    * @throws EC2Exception wraps checked exceptions
    */
   public String purchaseReservedInstancesOffering(String offeringId, int instanceCount) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("ReservedInstancesOfferingId", offeringId);
     params.put("InstanceCount", "" + instanceCount);
     PurchaseReservedInstancesOfferingResponse response =
             makeRequestInt(new HttpGet(), "PurchaseReservedInstancesOffering", params, PurchaseReservedInstancesOfferingResponse.class);
     return response.getReservedInstancesId();
   }
 
   /**
    * This method enables monitoring for some instances
    *
    * @param instanceIds the id of the instances to enable monitoring for
    * @return information about the monitoring state of those instances
    * @throws EC2Exception wraps checked exceptions
    */
   public List<MonitoredInstanceInfo> monitorInstances(List<String> instanceIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     MonitorInstancesResponseType response =
             makeRequestInt(new HttpGet(), "MonitorInstances", params, MonitorInstancesResponseType.class);
     List<MonitoredInstanceInfo> ret = new ArrayList<MonitoredInstanceInfo>();
     for (MonitorInstancesResponseSetItemType item : response.getInstancesSet().getItems()) {
       ret.add(new MonitoredInstanceInfo(item.getInstanceId(),
               item.getMonitoring().getState()));
     }
     return ret;
   }
 
   /**
    * This method disables monitoring for some instances
    *
    * @param instanceIds the id of the instances to disable monitoring for
    * @return information about the monitoring state of those instances
    * @throws EC2Exception wraps checked exceptions
    */
   public List<MonitoredInstanceInfo> unmonitorInstances(List<String> instanceIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < instanceIds.size(); i++) {
       params.put("InstanceId." + (i + 1), instanceIds.get(i));
     }
     MonitorInstancesResponseType response =
             makeRequestInt(new HttpGet(), "UnmonitorInstances", params, MonitorInstancesResponseType.class);
     List<MonitoredInstanceInfo> ret = new ArrayList<MonitoredInstanceInfo>();
     for (MonitorInstancesResponseSetItemType item : response.getInstancesSet().getItems()) {
       ret.add(new MonitoredInstanceInfo(item.getInstanceId(),
               item.getMonitoring().getState()));
     }
     return ret;
   }
 
   /**
    * Fetches desctiion of Amazon VPC Subnets as described
    * http://docs.amazonwebservices.com/AmazonVPC/2010-11-15/APIReference/
    * <p/>
    * This method may not be supported on all Amazon EC2 regisons. It will throw exception with
    * 'UnsupportedOperation' error.
    *
    * @return collection of all available subnets for current region.
    * @throws EC2Exception on error
    */
   @NotNull
   public Collection<SubnetInfo> describeSubnets() throws EC2Exception {
     DescribeSubnetsResponse response =
             makeRequestInt(new HttpGet(), "DescribeSubnets", Collections.<String, String>emptyMap(), DescribeSubnetsResponse.class);
 
     List<SubnetInfo> ret = new ArrayList<SubnetInfo>();
 
     List<SubnetType> items = response.getSubnetSet().getItems();
     if (items != null) {
       for (SubnetType item : items) {
         ret.add(new SubnetInfo(
                 item.getSubnetId(),
                 item.getState(),
                 item.getVpcId(),
                 item.getCidrBlock(),
                 item.getAvailableIpAddressCount(),
                 item.getAvailabilityZone()
         ));
       }
     }
 
     return ret;
   }
 
   public List<SpotPriceHistoryItem> describeSpotPriceHistory(Calendar start, Calendar end, String productDescription, InstanceType... instanceTypes) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     if (start != null) {
       params.put("StartTime", httpDate(start));
     }
     if (end != null) {
       params.put("EndTime", httpDate(end));
     }
     if (productDescription != null) {
       params.put("ProductDescription", productDescription);
     }
     for (int i = 0; i < instanceTypes.length; i++) {
       InstanceType instanceType = instanceTypes[i];
       params.put("InstanceType." + (i + 1), instanceType.getTypeId());
     }
 
     List<SpotPriceHistoryItem> ret = new ArrayList<SpotPriceHistoryItem>();
     DescribeSpotPriceHistoryResponse response =
             makeRequestInt(new HttpGet(), "DescribeSpotPriceHistory", params, DescribeSpotPriceHistoryResponse.class);
 
     List<SpotPriceHistorySetItemType> items = response.getSpotPriceHistorySet().getItems();
     if (items != null) {
       for (SpotPriceHistorySetItemType item : items) {
         ret.add(new SpotPriceHistoryItem(item));
       }
     }
 
     return ret;
   }
 
   public List<SpotInstanceRequest> describeSpotInstanceRequests() throws EC2Exception {
     List<SpotInstanceRequest> ret = new ArrayList<SpotInstanceRequest>();
     DescribeSpotInstanceRequestsResponse response =
             makeRequestInt(new HttpGet(), "DescribeSpotInstanceRequests", null, DescribeSpotInstanceRequestsResponse.class);
 
     List<SpotInstanceRequestSetItemType> items = response.getSpotInstanceRequestSet().getItems();
     if (items != null) {
       for (SpotInstanceRequestSetItemType item : items) {
         ret.add(new SpotInstanceRequest(item));
       }
     }
     return ret;
   }
 
   public List<SpotInstanceRequest> requestSpotInstances(SpotInstanceRequestConfiguration sirc, LaunchConfiguration lc) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     lc.prepareQueryParams("LaunchSpecification.", false, params);
     sirc.prepareQueryParams(params);
 
     List<SpotInstanceRequest> ret = new ArrayList<SpotInstanceRequest>();
     RequestSpotInstancesResponse response =
             makeRequestInt(new HttpGet(), "RequestSpotInstances", params, RequestSpotInstancesResponse.class);
 
     List<SpotInstanceRequestSetItemType> items = response.getSpotInstanceRequestSet().getItems();
     if (items != null) {
       for (SpotInstanceRequestSetItemType item : items) {
         ret.add(new SpotInstanceRequest(item));
       }
     }
 
     return ret;
   }
 
   public List<SpotInstanceCancellationResponse> cancelSpotInstanceRequests(String... sirIds) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
 
     for (int i = 0; i < sirIds.length; i++) {
       params.put("SpotInstanceRequestId." + (i + 1), sirIds[i]);
     }
 
     List<SpotInstanceCancellationResponse> ret = new ArrayList<SpotInstanceCancellationResponse>();
     CancelSpotInstanceRequestsResponse response =
             makeRequestInt(new HttpGet(), "CancelSpotInstanceRequests", params, CancelSpotInstanceRequestsResponse.class);
 
     List<CancelSpotInstanceRequestsResponseSetItemType> items = response.getSpotInstanceRequestSet().getItems();
     if (items != null) {
       for (CancelSpotInstanceRequestsResponseSetItemType item : items) {
         ret.add(new SpotInstanceCancellationResponse(item));
       }
     }
 
     return ret;
   }
 
   /**
    * This method creates the spot datafeed subscription (for spot usage logs)
    *
    * @param bucket the bucket to store the feed in
    * @param prefix the prefix used with the datafeed files
    * @return information about the subscription
    * @throws EC2Exception wraps checked exceptions
    */
   public SpotDatafeedSubscription createSpotDatafeedSubscription(String bucket, String prefix) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
 
     params.put("Bucket", bucket);
     if (prefix != null && !prefix.trim().equals("")) {
       params.put("Prefix", prefix);
     }
 
     CreateSpotDatafeedSubscriptionResponse response =
             makeRequestInt(new HttpGet(), "CreateSpotDatafeedSubscription", params, CreateSpotDatafeedSubscriptionResponse.class);
 
 
     return new SpotDatafeedSubscription(response.getRequestId(), response.getSpotDatafeedSubscription());
   }
 
   /**
    * This method describes the spot datafeed subscription
    *
    * @return information about the subscription
    * @throws EC2Exception wraps checked exceptions
    */
   public SpotDatafeedSubscription describeSpotDatafeedSubscription() throws EC2Exception {
     DescribeSpotDatafeedSubscriptionResponse response =
             makeRequestInt(new HttpGet(), "DescribeSpotDatafeedSubscription", null, DescribeSpotDatafeedSubscriptionResponse.class);
 
 
     return new SpotDatafeedSubscription(response.getRequestId(), response.getSpotDatafeedSubscription());
   }
 
   /**
    * This method deletes the spot datafeed subscription
    *
    * @throws EC2Exception wraps checked exceptions
    */
   public void deleteSpotDatafeedSubscription() throws EC2Exception {
     DeleteSpotDatafeedSubscriptionResponse response =
             makeRequestInt(new HttpGet(), "DeleteSpotDatafeedSubscription", null, DeleteSpotDatafeedSubscriptionResponse.class);
 
 
     if (!response.isReturn()) {
       throw new EC2Exception("Could not delete subscription. No reason given.");
     }
   }
 
   /**
    * Creates a placement group to launch cluster compute instances into.
    *
    * @param groupName the name of the group you're creating
    * @param strategy  placement strategy ("cluster")
    * @throws EC2Exception wraps checked exceptions
    */
   public void createPlacementGroup(String groupName, String strategy) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GropuName", groupName);
     params.put("Stategy", strategy);
     CreatePlacementGroupResponse response =
             makeRequestInt(new HttpGet(), "CreatePlacementGroup", params, CreatePlacementGroupResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not create placement group. No reason given.");
     }
   }
 
   /**
    * Deletes a placement group.
    *
    * @param groupName the name of the group you're creating
    * @throws EC2Exception wraps checked exceptions
    */
   public void deletePlacementGroup(String groupName) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     params.put("GropuName", groupName);
     DeletePlacementGroupResponse response =
             makeRequestInt(new HttpGet(), "DeletePlacementGroup", params, DeletePlacementGroupResponse.class);
     if (!response.isReturn()) {
       throw new EC2Exception("Could not delete placement group. No reason given.");
     }
   }
 
   /**
    * This method describes the placement groups.
    *
    * @param groupNames names of 1 or more groups to get information about, null for all groups
    * @return information about the groups
    * @throws EC2Exception wraps checked exceptions
    */
   public List<PlacementGroupInfo> describePlacementGroups(List<String> groupNames) throws EC2Exception {
     Map<String, String> params = new HashMap<String, String>();
     for (int i = 0; i < groupNames.size(); i++) {
       params.put("GroupName." + (i + 1), groupNames.get(i));
     }
     DescribePlacementGroupsResponse response =
             makeRequestInt(new HttpGet(), "DescribePlacementGroups", params, DescribePlacementGroupsResponse.class);
 
     List<PlacementGroupInfo> ret = new ArrayList<PlacementGroupInfo>();
     List<PlacementGroupInfoType> items = response.getPlacementGroupSet().getItems();
     if (items != null) {
       for (PlacementGroupInfoType item : items) {
         ret.add(new PlacementGroupInfo(response.getRequestId(), item.getGroupName(),
                 item.getStrategy(), item.getState()));
       }
     }
     return ret;
   }
 
   protected <T> T makeRequestInt(HttpRequestBase method, String action, Map<String, String> params, Class<T> respType)
           throws EC2Exception {
     try {
       return makeRequest(method, action, params, respType);
     } catch (AWSException ex) {
       throw new EC2Exception(ex);
     } catch (JAXBException ex) {
       throw new EC2Exception("Problem parsing returned message.", ex);
     } catch (SAXException ex) {
       throw new EC2Exception("Problem parsing returned message.", ex);
     } catch (MalformedURLException ex) {
       throw new EC2Exception(ex.getMessage(), ex);
     } catch (IOException ex) {
       throw new EC2Exception(ex.getMessage(), ex);
     } catch (HttpException ex) {
       throw new EC2Exception(ex.getMessage(), ex);
     }
   }
 }
