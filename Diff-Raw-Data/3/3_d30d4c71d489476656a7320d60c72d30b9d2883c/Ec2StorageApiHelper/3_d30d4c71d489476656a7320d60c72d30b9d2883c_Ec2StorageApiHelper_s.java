 package org.cloudifysource.quality.iTests.framework.utils.storage;
 
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.esc.driver.provisioning.storage.StorageProvisioningException;
 import org.cloudifysource.esc.driver.provisioning.storage.VolumeDetails;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.ec2.EC2ApiMetadata;
 import org.jclouds.ec2.EC2Client;
 import org.jclouds.ec2.domain.Attachment;
 import org.jclouds.ec2.domain.Volume;
 import org.jclouds.ec2.features.TagApi;
 import org.jclouds.ec2.services.ElasticBlockStoreClient;
 import org.jclouds.rest.ResourceNotFoundException;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 4/10/13
  * Time: 6:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Ec2StorageApiHelper extends JcloudsStorageApiHelper {
 
     private String region;
     private ComputeServiceContext context;
     private TagApi tagApi;
     private ElasticBlockStoreClient client;
 
     public Ec2StorageApiHelper(final Cloud cloud,
                                   final String templateName,
                                   final String region,
                                   final ComputeServiceContext computeContext) {
         super(cloud, templateName);
         this.region = region;
         this.context = computeContext;
         this.tagApi = getTagApi();
         this.client = EC2Client.class.cast(context.unwrap(EC2ApiMetadata.CONTEXT_TOKEN).getApi()).getElasticBlockStoreServices();
 
     }
 
     @Override
     public Set<VolumeDetails> getVolumesByPrefix(String prefix) throws StorageProvisioningException {
 
         Set<VolumeDetails> volumes = new HashSet<VolumeDetails>();
 
         Set<Volume> allVolumes = getAllVolumes();
         for (Volume vol : allVolumes) {
             if (vol.getId() != null) {
                 String volName = getVolumeName(vol.getId());
                 if (!StringUtils.isBlank(volName) &&  volName.toLowerCase().contains(prefix)) {
                     VolumeDetails details = new VolumeDetails();
                     details.setId(vol.getId());
                     volumes.add(details);
                 }
             }
         }
         return volumes;
     }
 
     @Override
     public VolumeDetails getVolumeById(String volumeId) {
         Volume vol = getById(volumeId);
         VolumeDetails volumeDetails = new VolumeDetails();
         volumeDetails.setId(vol.getId());
         return volumeDetails;
     }
 
     @Override
     public boolean isVolumeDeleting(String volumeId) {
         return getVolumeStatus(volumeId).equals(Volume.Status.DELETING.toString());
     }
 
     @Override
     public boolean isVolumeAvailable(String volumeId) {
         return getVolumeStatus(volumeId).equals(Volume.Status.AVAILABLE.toString());
     }
 
     @Override
     public String getVolumeStatus(String volumeId) {
         return getById(volumeId).getStatus().toString();
     }
 
     @Override
     public Set<String> getVolumeAttachments(String volumeId) {
 
         Set<String> attachments = new HashSet<String>();
         Set<Attachment> attachments1 = getById(volumeId).getAttachments();
         for (Attachment att : attachments1) {
             attachments.add(att.getId());
         }
         return attachments;
     }
 
     private TagApi getTagApi() {
         if (this.tagApi == null) {
             this.tagApi = EC2Client.class.cast(this.context.unwrap(EC2ApiMetadata.CONTEXT_TOKEN)
                     .getApi()).getTagApiForRegion(this.region).get();
         }
         return this.tagApi;
     }
 
     private Volume getById(final String volumeId) {
         Set<Volume> volumes;
         try {
             volumes = client.describeVolumesInRegion(region, volumeId);
         } catch (final ResourceNotFoundException e) {
             return null;
         }
         if (volumes == null || volumes.isEmpty()) {
             return null;
         }
 
         return volumes.iterator().next();
     }
 
     private Set<Volume> getAllVolumes() {
         return client.describeVolumesInRegion(this.region, (String[])null);
     }
 }
