 package org.cloudifysource.quality.iTests.framework.utils;
 
 /**
  * User: nirb
  * Date: 17/02/13
  */
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.Service;
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.dsl.cloud.storage.StorageTemplate;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.esc.driver.provisioning.storage.BaseStorageDriver;
 import org.cloudifysource.esc.driver.provisioning.storage.StorageProvisioningException;
 import org.cloudifysource.esc.driver.provisioning.storage.VolumeDetails;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.ec2.domain.Volume;
 
 
 public class StorageUtils {
 
     //TODO not a specific cloud
     private static BaseStorageDriver storageProvisioningDriver;
 
     private static Cloud cloud;
     private static CloudService cloudService;
     private static final long DURATION = 30;
 
     private static Map<String, Set<String>> serviceToMachines;
     private static Set<String> machinesBeforeInstall;
     private static Set<String> machinesAfterInstall;
 
     public static void init(final Cloud otherCloud, final String computeTemplateName, final String storageTemplateName, CloudService otherCloudService) throws Exception{
         cloud = otherCloud;
         cloudService = otherCloudService;
         storageProvisioningDriver = (BaseStorageDriver) Class.forName(cloud.getConfiguration().getStorageClassName()).newInstance();
         storageProvisioningDriver.setConfig(otherCloud, computeTemplateName);
 
         serviceToMachines = new HashMap<String, Set<String>>();
         machinesBeforeInstall = new HashSet<String>();
         machinesAfterInstall = new HashSet<String>();
 
         JCloudsUtils.createContext(otherCloudService);
     }
     
     public static String getVolumeName(Volume vol) throws StorageProvisioningException {
     	return storageProvisioningDriver.getVolumeName(vol.getId());
     }
 
     public static void close(){
         JCloudsUtils.closeContext();
         storageProvisioningDriver = null;
     }
 
     public static boolean isInitialized(){
         return storageProvisioningDriver != null;
     }
 
     private static void deleteVolume(final String location, final String volumeId, final long duration, final TimeUnit timeUnit) throws Exception{
         storageProvisioningDriver.deleteVolume(location, volumeId, duration, timeUnit);
     }
 
     public static void scanAndDeleteLeakedVolumes(boolean expectedLeak) throws Exception{
 
         if (cloudService == null) {
             LogUtils.log("No leaked volume scan was executed as the cloud service for this class was not created");
             return;
         }
 
         List<StorageTemplate> storageTemplates = new ArrayList<StorageTemplate>(cloud.getCloudStorage().getTemplates().values());
         Set<String> namePrefixes = new HashSet<String>();
         boolean foundLeak = false;
 
         for(StorageTemplate st : storageTemplates){
             namePrefixes.add(st.getNamePrefix());
         }
 
         Set<VolumeDetails> volumes = storageProvisioningDriver.listAllVolumes();
 
         for(VolumeDetails vd : volumes){
             for(String prefix : namePrefixes){
                 if(vd.getName() != null && vd.getName().startsWith(prefix)){
                     foundLeak = !expectedLeak;
                     LogUtils.log("leaked volume found. Name: " + vd.getName() + ", id: " + vd.getId() + ", location: " + vd.getLocation());
                     deleteVolume(vd.getLocation(), vd.getId(), DURATION, TimeUnit.SECONDS);
                 }
             }
         }
 
         if(foundLeak){
             AssertUtils.assertFail("found leaking volume(s) and deleted it.");
         }
     }
 
     public static void scanAndDeleteLeakedVolumes() throws Exception{
         scanAndDeleteLeakedVolumes(false);
     }
 
     public static void beforeServiceInstallation(){
 
         machinesBeforeInstall.clear();
         Set<? extends NodeMetadata> allNodes = JCloudsUtils.getAllRunningNodes();
 
         for(NodeMetadata nm : allNodes){
             if((nm.getName() != null) && nm.getName().toLowerCase().contains(cloudService.getMachinePrefix().toLowerCase())){
                 machinesBeforeInstall.add(nm.getPrivateAddresses().iterator().next());
             }
         }
     }
 
     public static void afterServiceInstallation(String serviceName){
 
         machinesAfterInstall.clear();
         Set<? extends NodeMetadata> allNodes = JCloudsUtils.getAllRunningNodes();
 
         for(NodeMetadata nm : allNodes){
             if((nm.getName() != null) && nm.getName().toLowerCase().contains(cloudService.getMachinePrefix().toLowerCase())){
                 machinesAfterInstall.add(nm.getPrivateAddresses().iterator().next());
             }
         }
 
         AssertUtils.assertTrue("installation didn't start any machine", !machinesAfterInstall.isEmpty());
 
         Set<String> difference = new HashSet<String>(machinesAfterInstall);
         difference.removeAll(machinesBeforeInstall);
 
         ///////debug
         LogUtils.log("inserting " + serviceName + " to map");
         ///////debug
 
         serviceToMachines.put(serviceName, difference);
         machinesAfterInstall.clear();
         machinesBeforeInstall.clear();
     }
 
     public static void afterServiceUninstallation(String serviceName){
         serviceToMachines.remove(serviceName);
     }
 
     public static boolean verifyVolumeConfiguration(String serviceFilePath) throws Exception{
 
         boolean result = true;
         File serviceFile = new File(serviceFilePath);
         Service service = ServiceReader.readService(serviceFile);
         Set<String> machinesIps = serviceToMachines.get(service.getName());
 
         String storageTemplateName = service.getStorage().getTemplate();
         String computeTemplateName = service.getCompute().getTemplate();
 
         for(String machineIp : machinesIps){
             result = verifyVolumeSize(storageTemplateName, machineIp) &&
                     verifyVolumeLocation(computeTemplateName, machineIp) &&
                     verifyVolumePrefix(storageTemplateName, machineIp);
         }
 
         return result;
     }
     private static boolean verifyVolumeSize(String templateName, String machineIp) throws Exception{
 
         String prefix = cloud.getCloudStorage().getTemplates().get(templateName).getNamePrefix();
         int expectedSize = cloud.getCloudStorage().getTemplates().get(templateName).getSize();
         Set<VolumeDetails> volumes = storageProvisioningDriver.listVolumes(machineIp, DURATION, TimeUnit.SECONDS);
 
         for(VolumeDetails vd : volumes){
             if(vd.getName().startsWith(prefix)){
                 if(vd.getSize() != expectedSize){
                     LogUtils.log("the volume " + vd.getId() + " has the wrong size. expected: " + expectedSize + " actual: " + vd.getSize());
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     private static boolean verifyVolumeLocation(String templateName, String machineIp) throws Exception{
 
         String expectedLocation = cloud.getCloudCompute().getTemplates().get(templateName).getLocationId();
         Set<VolumeDetails> volumes = storageProvisioningDriver.listVolumes(machineIp, DURATION, TimeUnit.SECONDS);
 
         for(VolumeDetails vd : volumes){
             if(!vd.getLocation().startsWith(expectedLocation)){
                 LogUtils.log("the volume " + vd.getId() + " has the wrong location. expected: " + expectedLocation + " actual: " + vd.getLocation());
                 return false;
             }
         }
 
         return true;
     }
 
     private static boolean verifyVolumePrefix(String templateName, String machineIp) throws Exception{
 
         String expectedPrefix = cloud.getCloudStorage().getTemplates().get(templateName).getNamePrefix();
         Set<VolumeDetails> volumes = storageProvisioningDriver.listVolumes(machineIp, DURATION, TimeUnit.SECONDS);
 
         for(VolumeDetails vd : volumes){
             if(!vd.getName().isEmpty() && !vd.getName().startsWith(expectedPrefix)){
                 LogUtils.log("the volume " + vd.getId() + " starts with a wrong prefix. expected: " + expectedPrefix + " actual: " + storageProvisioningDriver.getVolumeName(vd.getId()));
                 return false;
             }
         }
 
         return true;
     }
 
     public static Map<String, Set<String>> getServicesToMachines(){
         return serviceToMachines;
     }
 
     public static Set<VolumeDetails> getServiceNamedVolumes(String serviceFilePath) throws Exception {
 
         Set<VolumeDetails> serviceVolumes = new HashSet<VolumeDetails>();
 
         String serviceTemplateName = getStorageTemplateName(serviceFilePath);
         StorageTemplate storageTemplate = cloud.getCloudStorage().getTemplates().get(serviceTemplateName);
         String volumeNamePrefix = storageTemplate.getNamePrefix();
 
         Set<VolumeDetails> allVolumes = storageProvisioningDriver.listAllVolumes();
         for(VolumeDetails vd : allVolumes){
             if(vd != null && !vd.getName().isEmpty() && vd.getName().startsWith(volumeNamePrefix))
             {
                 serviceVolumes.add(vd);
             }
         }
 
         return serviceVolumes;
     }
 
     public static String getStorageTemplateName(String serviceFilePath) throws Exception {
 
         File serviceFile = new File(serviceFilePath);
         Service service = ServiceReader.readService(serviceFile);
         return service.getStorage().getTemplate();
     }
 
     public static void detachVolume(final String volumeId, final String ip, final long duration, final TimeUnit timeUnit) throws Exception {
         storageProvisioningDriver.detachVolume(volumeId, ip, duration, timeUnit);
     }
 
    public static void attachVolume(final String volumeId, final String device, final String machineIp, final long duration, final TimeUnit timeUnit) throws Exception {
        storageProvisioningDriver.attachVolume(volumeId, device, machineIp, duration, timeUnit);
     }
 
     public static Set<VolumeDetails> listVolumes(final String machineIp, final long duration, final TimeUnit timeUnit) throws Exception {
         return storageProvisioningDriver.listVolumes(machineIp, duration, timeUnit);
     }
 }
 
