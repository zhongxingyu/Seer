 /**
  * Copyright (C) 2010-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.vsphere.compute;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.compute.AbstractImageSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.ImageFilterOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageFormat;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.vsphere.PrivateCloud;
 
 import com.vmware.vim25.InvalidProperty;
 import com.vmware.vim25.RuntimeFault;
 import com.vmware.vim25.VirtualMachineConfigInfo;
 import com.vmware.vim25.VirtualMachineGuestOsIdentifier;
 import com.vmware.vim25.VirtualMachinePowerState;
 import com.vmware.vim25.VirtualMachineRuntimeInfo;
 import com.vmware.vim25.mo.Folder;
 import com.vmware.vim25.mo.InventoryNavigator;
 import com.vmware.vim25.mo.ManagedEntity;
 import com.vmware.vim25.mo.ServiceInstance;
 import com.vmware.vim25.mo.VirtualMachine;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 public class Template extends AbstractImageSupport {
 
     private PrivateCloud provider;
     
     Template(@Nonnull PrivateCloud cloud) {
         super(cloud);
         provider = cloud;
     }
 
     private @Nonnull ServiceInstance getServiceInstance() throws CloudException, InternalException {
         ServiceInstance instance = provider.getServiceInstance();
 
         if( instance == null ) {
             throw new CloudException(CloudErrorType.AUTHENTICATION, HttpServletResponse.SC_UNAUTHORIZED, null, "Unauthorized");
         }
         return instance;
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         ServiceInstance instance = getServiceInstance();
 
         Folder folder = provider.getVmFolder(instance);
         ManagedEntity[] mes;
 
         try {
             mes = new InventoryNavigator(folder).searchManagedEntities("VirtualMachine");
         }
         catch( InvalidProperty e ) {
             throw new CloudException("No virtual machine support in cluster: " + e.getMessage());
         }
         catch( RuntimeFault e ) {
             throw new CloudException("Error in processing request to cluster: " + e.getMessage());
         }
         catch( RemoteException e ) {
             throw new CloudException("Error in cluster processing request: " + e.getMessage());
         }
 
         if( mes != null && mes.length > 0 ) {
             for( ManagedEntity entity : mes ) {
                 VirtualMachine template = (VirtualMachine)entity;
                if( template != null  && template.getConfig().getUuid().equals(providerImageId)) {
                     VirtualMachineConfigInfo cfg = null;
 
                     try {
                         cfg = template.getConfig();
                         if( cfg != null && cfg.isTemplate() ) {
                             template.destroy_Task();
                         }
                     }
                     catch(RuntimeException e) {
                         e.printStackTrace();
                     }
                     catch(RemoteException ex){
                         throw new CloudException(ex);
                     }
                 }
             }
         }
 
         /*
         ServiceInstance service = getServiceInstance();
 
         com.vmware.vim25.mo.VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(service, providerImageId);
 
         if( vm == null ) {
             throw new CloudException("No such template: " + providerImageId);
         }
         try {
             vm.destroy_Task();
         }
         catch( RemoteException e ) {
             throw new CloudException(e);
         }
         */
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         for( ImageClass cls : listSupportedImageClasses() ) {
             for( MachineImage image : listImages(cls) ) {
                 if( image.getProviderMachineImageId().equals(providerImageId) ) {
                     return image;
                 }
             }
         }
         return null;
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         return "template";
     }
 
     @Override
     public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return Collections.singletonList(ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageType.VOLUME);
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
     
     private @Nullable MachineImage toMachineImage(@Nullable VirtualMachine template) throws InternalException, CloudException {
         if( template != null ) {
             VirtualMachineConfigInfo vminfo;
             MachineImage image = new MachineImage();
             VirtualMachineGuestOsIdentifier os;
             Platform platform;
 
             try {
                 vminfo = template.getConfig();
             }
             catch( RuntimeException e ) {
                 return null;
             }
             try {
                 os = VirtualMachineGuestOsIdentifier.valueOf(vminfo.getGuestId());
                 platform = Platform.guess(vminfo.getGuestFullName());
             }
             catch( IllegalArgumentException e ) {
                 System.out.println("DEBUG: No such guest in enum: " + vminfo.getGuestId());
                 os = null;
                 platform = Platform.guess(vminfo.getGuestId());
             }
             if( os == null ) {
                 image.setArchitecture(vminfo.getGuestId().contains("64") ? Architecture.I32 : Architecture.I64);
             }
             else {
                 image.setArchitecture(provider.getComputeServices().getVirtualMachineSupport().getArchitecture(os));
             }
             image.setImageClass(ImageClass.MACHINE);
             image.setDescription(template.getName());
             image.setName(template.getName());
             image.setProviderOwnerId(getContext().getAccountNumber());
             image.setPlatform(platform);
             image.setProviderMachineImageId(vminfo.getUuid());
             image.setType(MachineImageType.VOLUME);
             image.setProviderRegionId(getContext().getRegionId());
             image.setSoftware("");
             image.setTags(new HashMap<String,String>());
             
             VirtualMachineRuntimeInfo runtime = template.getRuntime();
             VirtualMachinePowerState state = VirtualMachinePowerState.poweredOff;
             
             if( runtime != null ) {
                 state = runtime.getPowerState();
             }
             if( state.equals(VirtualMachinePowerState.poweredOff) ) {
                 image.setCurrentState(MachineImageState.ACTIVE);
             }
             else {
                 image.setCurrentState(MachineImageState.PENDING);
             }
             return image;
         }
         return null;
     }
 
     @Override
     public boolean hasPublicLibrary() {
         return false;
     }
 
     @Override
     public @Nonnull Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     protected MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         String vmId = options.getVirtualMachineId();
 
         if( vmId == null ) {
             throw new CloudException("You must specify a virtual machine to capture");
         }
         ServiceInstance service = getServiceInstance();
 
         com.vmware.vim25.mo.VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(service, vmId);
 
         if( vm == null ) {
             throw new CloudException("No such virtual machine for imaging: " + vmId);
         }
         MachineImage img = toMachineImage(provider.getComputeServices().getVirtualMachineSupport().clone(service, vm, options.getName(), true));
 
         if( img == null ) {
             throw new CloudException("Failed to identify newly created template");
         }
         if( task != null ) {
             task.completeWithResult(img);
         }
         return img;
     }
 
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         ArrayList<ResourceStatus> status = new ArrayList<ResourceStatus>();
 
         for( MachineImage img : listImages(cls) ) {
             status.add(new ResourceStatus(img.getProviderMachineImageId(), img.getCurrentState()));
         }
         return status;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
         ArrayList<MachineImage> machineImages = new ArrayList<MachineImage>();
         ServiceInstance instance = getServiceInstance();
 
         Folder folder = provider.getVmFolder(instance);
         ManagedEntity[] mes;
 
         try {
             mes = new InventoryNavigator(folder).searchManagedEntities("VirtualMachine");
         }
         catch( InvalidProperty e ) {
             throw new CloudException("No virtual machine support in cluster: " + e.getMessage());
         }
         catch( RuntimeFault e ) {
             throw new CloudException("Error in processing request to cluster: " + e.getMessage());
         }
         catch( RemoteException e ) {
             throw new CloudException("Error in cluster processing request: " + e.getMessage());
         }
 
         if( mes != null && mes.length > 0 ) {
             for( ManagedEntity entity : mes ) {
                 VirtualMachine template = (VirtualMachine)entity;
 
                 if( template != null ) {
                     VirtualMachineConfigInfo cfg = null;
 
                     try {
                         cfg = template.getConfig();
                     }
                     catch( RuntimeException e ) {
                         e.printStackTrace();
                     }
                     if( cfg != null && cfg.isTemplate() ) {
                         MachineImage image = toMachineImage(template);
 
                         if( image != null && (options == null || options.matches(image)) ) {
                             machineImages.add(image);
                         }
                     }
                 }
             }
         }
 
         return machineImages;
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageFormat.VMDK);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
         return searchImages(null, keyword, platform, architecture, ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchPublicImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsDirectImageUpload() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
         return type.equals(MachineImageType.VOLUME);
     }
 
     @Override
     public boolean supportsImageSharing() {
         return false;
     }
 
     @Override
     public boolean supportsImageSharingWithPublic() {
         return false;
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return false;
     }
 }
