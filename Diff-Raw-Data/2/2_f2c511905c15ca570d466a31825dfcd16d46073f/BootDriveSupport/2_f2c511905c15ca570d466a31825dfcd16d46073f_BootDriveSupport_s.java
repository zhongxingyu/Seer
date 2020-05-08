 /**
  * Copyright (C) 2012-2013 Dell, Inc.
  * See annotations for authorship information
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
 
 package org.dasein.cloud.cloudsigma.compute.image;
 
 import com.sun.servicetag.SystemEnvironment;
 import org.dasein.cloud.compute.*;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.cloudsigma.CloudSigma;
 import org.dasein.cloud.cloudsigma.CloudSigmaConfigurationException;
 import org.dasein.cloud.cloudsigma.CloudSigmaMethod;
 import org.dasein.cloud.cloudsigma.NoContextException;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.util.uom.storage.Storage;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Locale;
 
 /**
  * Maps CloudSigma drives to the concept of machine images. While there's a fairly huge disconnect between the
  * CloudSigma concept of drives and what Dasein Cloud thinks of as a machine image, this implementation attempts
  * to bridge that gap.
  * <p>Created by Danielle Mayne: 02/20/13 12:03 PM</p>
  * @author George Reese
  * @author Danielle Mayne
  * @version 2013.02 initial version
  * @since 2013.02
  */
 public class BootDriveSupport extends AbstractImageSupport {
     static private final Logger logger = CloudSigma.getLogger(BootDriveSupport.class);
 
     private CloudSigma provider;
 
     public BootDriveSupport(@Nonnull CloudSigma provider) {
         super(provider);
         this.provider = provider;
     }
 
     public @Nonnull JSONObject cloneDrive(@Nonnull String driveId, @Nonnull String name, Platform os) throws CloudException, InternalException {
         JSONObject currentDrive = getDrive(driveId);
 
         if (currentDrive == null) {
             throw new CloudException("No such drive: " + driveId);
         }
         try {
             logger.debug("Cloning volume: "+driveId+" with new name "+name);
             JSONObject newDrive = new JSONObject();
             newDrive.put("name", name.replaceAll("\n", " "));
 
             CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
 
             JSONObject jDrive = null;
             //dmayne 20130529: determine if this is private or library drive
             boolean publicImage = false;
             if (currentDrive.has("image_type")) {
                 publicImage = true;
                 //library image: now check it is definitely a disk and not CDROM
                 if (currentDrive.has("media") && !currentDrive.isNull("media"))   {
                     String media = currentDrive.getString("media");
                     if (media.equalsIgnoreCase("cdrom")) {
                         logger.info("Can't clone drive as image is cdrom");
                         throw new InternalException("Can't clone drive as image is cdrom");
                     }
                 }
                 else {
                     throw new CloudException("Can't clone drive: unknown media "+driveId);
                 }
             }
 
             JSONObject obj;
             if (publicImage) {
                 obj = new JSONObject(method.postString(toPublicImageURL(driveId, "action/?do=clone"), newDrive.toString()));
             }
             else {
                 obj = new JSONObject(method.postString(toDriveURL(driveId, "action/?do=clone"), newDrive.toString()));
             }if (obj != null) {
                 jDrive = (JSONObject) obj;
             }
 
             if (jDrive == null) {
                 throw new CloudException("Clone supposedly succeeded, but no drive information was provided");
             }
             return jDrive;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     public @Nullable JSONObject getDrive(String driveId) throws CloudException, InternalException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         String body = method.getString(toDriveURL(driveId, ""));
 
         try {
             JSONObject jDrive = null;
             if (body != null) {
                 jDrive = new JSONObject(body);
                 //dmayne 20130529: library drive can be returned in above call
                 // check if owner is null and if so, call the library endpoint
                 if (!jDrive.has("owner") || jDrive.isNull("owner")) {
                     jDrive = null;
                 }
             }
 
             if( jDrive == null ) {
                 logger.debug("Failed " + driveId + ", looking in library...");
                 body = method.getString(toPublicImageURL(driveId, ""));
                 if (body != null) {
                     jDrive = new JSONObject(body);
                 }
                 logger.debug("SUCCESS: " + (jDrive != null));
             }
             return jDrive;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     @Override
     public void addImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to share images");
     }
 
     @Override
     public void addPublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to make images public");
     }
 
     @Override
     public @Nonnull String bundleVirtualMachine(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Bundling of virtual machines not supported");
     }
 
     @Override
     public void bundleVirtualMachineAsync(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name, @Nonnull AsynchronousTask<String> trackingTask) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Bundling of virtual machines not supported");
     }
 
     @Override
     protected @Nonnull MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         try {
             if( task != null ) {
                 task.setStartTime(System.currentTimeMillis());
             }
             VirtualMachine vm;
             boolean restart = false;
 
             vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(options.getVirtualMachineId());
             if (vm == null) {
                 throw new CloudException("Virtual machine not found: " + options.getVirtualMachineId());
             }
             if (!VmState.STOPPED.equals(vm.getCurrentState())) {
                 restart = true;
                 provider.getComputeServices().getVirtualMachineSupport().stop(options.getVirtualMachineId());
                 try {
                     long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
                     vm = null;
                     while (timeout > System.currentTimeMillis()) {
                         vm =  provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(options.getVirtualMachineId());
                         if (vm.getCurrentState().equals(VmState.STOPPED)) {
                             System.out.println("Server stopped");
                             break;
                         }
                         try { Thread.sleep(15000L); }
                         catch( InterruptedException ignore ) { }
                     }
                 }
                 catch (Throwable ignore) {
                 }
             }
             String driveId = vm.getProviderMachineImageId();
 
             try {
                 if (driveId != null) {
                     JSONObject object = cloneDrive(driveId, options.getName(), vm.getPlatform());
 
                     String id = null;
                     if (object.has("objects")) {
                         JSONArray jDrives = object.getJSONArray("objects");
                         JSONObject actualDrive = (JSONObject) jDrives.get(0);
                         id = actualDrive.getString("uuid");
                     }
                     MachineImage img = null;
 
                     if (id != null) {
                         img = getImage(id);
                     }
                     if (img == null) {
                         throw new CloudException("Drive cloning completed, but no ID was provided for clone");
                     }
                     if( task != null ) {
                         task.completeWithResult(img);
                     }
                     return img;
                 }
                 else {
                     throw new InternalException("Drive id for cloning is null");
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
             finally {
                 if (restart) {
                     try {
                         provider.getComputeServices().getVirtualMachineSupport().start(options.getVirtualMachineId());
                     } catch (Throwable ignore) {
                         logger.warn("Failed to restart " + options.getVirtualMachineId() + " after drive cloning");
                     }
                 }
             }
         } finally {
             provider.release();
         }
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         return toMachineImage(getDrive(providerImageId));
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         switch (cls) {
             case KERNEL:
                 return "kernel image";
             case RAMDISK:
                 return "ramdisk image";
         }
         return "boot drive";
     }
 
     @Override
     public @Nonnull String getProviderTermForCustomImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         return getProviderTermForImage(locale, cls);
     }
 
     @Override
     public boolean hasPublicLibrary() {
         return true;
     }
 
     @Override
     public @Nonnull Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
         MachineImage img = getImage(machineImageId);
 
         if (img == null) {
             return false;
         }
         String owner = img.getProviderOwnerId();
 
         return (owner.equals("--public--") || owner.equals("00000000-0000-0000-0000-000000000001"));
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         return provider.getComputeServices().getVirtualMachineSupport().isSubscribed();
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         if (!cls.equals(ImageClass.MACHINE)) {
             return Collections.emptyList();
         }
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
         String me = ctx.getAccountNumber();
 
         ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/drives";
         String target = "/?fields=uuid,meta,name,status,owner";
 
         while(moreData)  {
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             JSONObject jObject = method.list(target);
 
 
             if (jObject == null) {
                 throw new CloudException("Could not identify drive endpoint for CloudSigma");
             }
             try {
                 JSONArray objects = jObject.getJSONArray("objects");
                 for (int i = 0; i < objects.length(); i++) {
                     JSONObject jImage = objects.getJSONObject(i);
                     //dmayne 20130522: check that we are looking at an image
                     //(will have an image_type attribute)
                     JSONObject metadata = jImage.getJSONObject("meta");
                     String name = jImage.getString("name");
                     if (metadata.has("image_type") || name.startsWith("esimg-")) {
                         JSONObject owner = jImage.getJSONObject("owner");
                         String id = owner.getString("uuid");
     
                         if (id != null && id.trim().equals("")) {
                             id = null;
                         }
                         if (me.equals(id)) {
                             ResourceStatus img = toStatus(jImage);
     
                             if (img != null) {
                                 list.add(img);
                             }
                         }
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObject.has("meta")) {
                     JSONObject meta = jObject.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
         if( options != null && !ImageClass.MACHINE.equals(options.getImageClass()) ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> matches = new ArrayList<MachineImage>();
         String me = getContext().getAccountNumber();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/drives/detail/";
         String target = "";
 
         while(moreData)  {
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             JSONObject jObject = method.list(target);
 
             try {
                 if (jObject == null) {
                     throw new CloudException("Could not identify drive endpoint for CloudSigma");
                 }
                 if (jObject.has("objects")){
                     JSONArray objects = jObject.getJSONArray("objects");
                     for (int i = 0; i < objects.length(); i++) {
                         JSONObject jImage = objects.getJSONObject(i);
                         //dmayne 20130522: check that we are looking at an image
                         //(will have an image_type attribute)
                         JSONObject metadata = jImage.getJSONObject("meta");
                         String name = jImage.getString("name");
                     if (metadata.has("image_type") || name.startsWith("esimg-")) {
                             String id = null;
                             if (jImage.has("owner")) {
                                 JSONObject owner = jImage.getJSONObject("owner");
                                 if (owner != null && owner.has("uuid")){
                                     id = owner.getString("uuid");
                                 }
                             }
 
                             if (id != null && id.trim().equals("")) {
                                 id = null;
                             }
                             if (me.equals(id)) {
                                 MachineImage img = toMachineImage(jImage);
     
                                 if( img != null && (options == null || options.matches(img)) ) {
                                     matches.add(img);
                                 }
                             }   
                         }
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObject.has("meta")) {
                     JSONObject meta = jObject.getJSONObject("meta");
 
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
         return matches;
     }
 
     private @Nonnull Iterable<MachineImage> listImagesComplete(@Nullable String accountId) throws CloudException, InternalException {
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
         String me = ctx.getAccountNumber();
 
         if (me.equals(accountId)) {
             return listImages(ImageClass.MACHINE);
         } else if (accountId == null || accountId.equals("")) {
             accountId = "00000000-0000-0000-0000-000000000001";
         }
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/libdrives/detail/";
         String target = "";
 
         while(moreData)  {
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             JSONObject jObj = method.list(target);
             try{
                 if (jObj == null) {
                     throw new CloudException("Could not identify drive endpoint for CloudSigma");
                 }
 
                 if (jObj.has("objects")){
                     JSONArray objects = jObj.getJSONArray("objects");
                     for (int i = 0; i < objects.length(); i++) {
                         JSONObject jImage = objects.getJSONObject(i);
                         String id = null;
                         if (jImage.has("owner") && jImage.isNull("owner")) {
                             id = "00000000-0000-0000-0000-000000000001";
                         }
                         if (accountId.equals(id)) {
                             MachineImage img = toPublicMachineImage(jImage);
 
                             if( img != null ) {
                                 list.add(img);
                             }
                         }
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObj.has("meta")) {
                     JSONObject meta = jObj.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
 
         return list;
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageFormat.OVF);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     /*
     private boolean matches(@Nonnull MachineImage image, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) {
         if (architecture != null && !architecture.equals(image.getArchitecture())) {
             return false;
         }
         if (platform != null && !platform.equals(Platform.UNKNOWN)) {
             Platform mine = image.getPlatform();
 
             if (platform.isWindows() && !mine.isWindows()) {
                 return false;
             }
             if (platform.isUnix() && !mine.isUnix()) {
                 return false;
             }
             if (platform.isBsd() && !mine.isBsd()) {
                 return false;
             }
             if (platform.isLinux() && !mine.isLinux()) {
                 return false;
             }
             if (platform.equals(Platform.UNIX)) {
                 if (!mine.isUnix()) {
                     return false;
                 }
             } else if (!platform.equals(mine)) {
                 return false;
             }
         }
         if (keyword != null) {
             keyword = keyword.toLowerCase();
             if (!image.getDescription().toLowerCase().contains(keyword)) {
                 if (!image.getName().toLowerCase().contains(keyword)) {
                     if (!image.getProviderMachineImageId().toLowerCase().contains(keyword)) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
     */
 
     @Override
     public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return Collections.singletonList(ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageType.VOLUME);
     }
 
     @Override
     public @Nonnull MachineImage registerImageBundle(@Nonnull ImageCreateOptions options) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No image registering is currently supported");
     }
 
     @Override
     public void remove(@Nonnull String machineImageId) throws CloudException, InternalException {
         remove(machineImageId, false);
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         if (method.deleteString(toDriveURL(providerImageId, ""), "") == null) {
             throw new CloudException("Unable to identify drives endpoint for removal");
         }
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 
         /*for (MachineImage img : listImages(ImageClass.MACHINE)) {
             if (img != null && matches(img, keyword, platform, architecture)) {
                 list.add(img);
             }
         }*/
         for (MachineImage img : listImagesComplete(null)) {
             if (img != null && matches(img, keyword, platform, architecture)) {
                 list.add(img);
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchImages(@Nullable String accountNumber, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 
         if (accountNumber == null) {
             for (MachineImage img : listImages(ImageClass.MACHINE)) {
                 if (img != null && matches(img, keyword, platform, architecture)) {
                     list.add(img);
                 }
             }
             for (MachineImage img : listImagesComplete(null)) {
                 if (img != null && matches(img, keyword, platform, architecture)) {
                     list.add(img);
                 }
             }
         } else {
             for (MachineImage img : listImages(ImageClass.MACHINE, accountNumber)) {
                 if (img != null && matches(img, keyword, platform, architecture)) {
                     list.add(img);
                 }
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchPublicImages(@Nonnull ImageFilterOptions options) throws InternalException, CloudException {
        if( !ImageClass.MACHINE.equals(options.getImageClass()) ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> matches = new ArrayList<MachineImage>();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/libdrives/detail/";
         String target = "";
 
         while(moreData)  {
             target = baseTarget+target;
 
             JSONObject jObject = method.list(target);
 
             try {
                 if (jObject == null) {
                     throw new CloudException("Could not identify drive endpoint for CloudSigma");
                 }
                 if (jObject.has("objects")){
                     JSONArray objects = jObject.getJSONArray("objects");
                     for (int i = 0; i < objects.length(); i++) {
                         JSONObject jImage = objects.getJSONObject(i);
                         String id = null;
                         if (jImage.has("owner") && jImage.isNull("owner")) {
                             id = "00000000-0000-0000-0000-000000000001";
                         }
                         if( id == null || id.equals("00000000-0000-0000-0000-000000000001") ) {
                             MachineImage img = toPublicMachineImage(jImage);
 
                             if( img != null && options.matches(img) ) {
                                 matches.add(img);
                             }
                         }
                     }
                 }
 
                 //dmayne 20130314: check if there are more pages
                 if (jObject.has("meta")) {
                     JSONObject meta = jObject.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
         }
 
         return matches;
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
         return true;
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
         return true;
     }
 
     private @Nullable MachineImage toMachineImage(@Nullable JSONObject drive) throws CloudException, InternalException {
         if (drive == null) {
             logger.debug("drive is null");
             return null;
         }
         try {
             if (drive.has("image_type")) {
                 //dmayne 20130529: this is a library drive
                 return toPublicMachineImage(drive);
             }
 
             ProviderContext ctx = provider.getContext();
 
             if (ctx == null) {
                 throw new NoContextException();
             }
             String regionId = ctx.getRegionId();
 
             if (regionId == null) {
                 throw new CloudSigmaConfigurationException("No region was specified for this request");
             }
             MachineImage image = new MachineImage();
 
             image.setProviderRegionId(regionId);
             image.setCurrentState(MachineImageState.PENDING);
             image.setType(MachineImageType.VOLUME);
             image.setImageClass(ImageClass.MACHINE);
             String id = drive.getString("uuid");
 
             if (id != null && !id.equals("")) {
                 image.setProviderMachineImageId(id);
             }
             String name = drive.getString("name");
             if (name != null && !name.equals("")) {
                 image.setName(name);
             }
 
             String description = null;
             String os = null;
             String install_notes = null;
             if (drive.has("meta")){
                 JSONObject meta = drive.getJSONObject("meta");
                 //dmayne 20130220: look for description tag and if not available check install_notes
                 if (meta != null) {
                     if (meta.has("description")) {
                         description = meta.getString("description");
                     }
                     if (description == null || description.length() == 0) {
                         if (meta.has("install_notes")) {
                             install_notes = meta.getString("install_notes");
                         }
                     }
                     if (meta.has("os")) {
                         os = meta.getString("os");
                     }
                     String bits = null;
                     if (meta.has("arch")) {
                         bits = meta.getString("arch");
                     }
 
                     if (bits != null && bits.contains("32")) {
                         image.setArchitecture(Architecture.I32);
                     } else {
                         image.setArchitecture(Architecture.I64);
                     }
                 }
 
 
                 if (description != null && !description.equals("")) {
                     image.setDescription(description);
                 }
                 else if (install_notes != null && !install_notes.equals("")) {
                     image.setDescription(install_notes);
                 }
             }
             String user = null;
             if (drive.has("owner") && !drive.isNull("owner")) {
                 JSONObject owner = drive.getJSONObject("owner");
                 if (owner != null && owner.has("uuid")) {
                     user = owner.getString("uuid");
                 }
             }
             if (user != null && !user.equals("")) {
                 image.setProviderOwnerId(user);
             } else {
                 image.setProviderOwnerId("00000000-0000-0000-0000-000000000001");
             }
             String s = drive.getString("status");
 
             if (s != null) {
                 if( s.equalsIgnoreCase("mounted") || s.equalsIgnoreCase("copying") ) {
                     image.setCurrentState(MachineImageState.PENDING);
                 }
                 else if( s.equalsIgnoreCase("unmounted") ) {
                     image.setCurrentState(MachineImageState.ACTIVE);
                 }
                 else if( s.equalsIgnoreCase("unavailable") ) {
                     image.setCurrentState(MachineImageState.DELETED);
                 }
                 else {
                     logger.warn("WARN: Unknown drive state for CloudSigma: " + s);
                 }
                 /*
                 if (s.equalsIgnoreCase("mounted") ) {
                     image.setCurrentState(MachineImageState.ACTIVE);
                 } else if (s.equalsIgnoreCase("unmounted") || s.equalsIgnoreCase("unavailable")) {
                     image.setCurrentState(MachineImageState.DELETED);
                 } else if (s.startsWith("copying")) {
                     image.setCurrentState(MachineImageState.PENDING);
                 } else {
                     logger.warn("WARN: Unknown drive state for CloudSigma: " + s);
                 }
                 */
             }
             if (MachineImageState.ACTIVE.equals(image.getCurrentState())) {
                 s = null;
                 //todo: dmayne 20130305: not implemented by cloudsigma yet
                 if (drive.has("imaging")) {
                     s = drive.getString("imaging");
                 }
                 if (s != null) {
                     image.setCurrentState(MachineImageState.PENDING);
                 }
             }
             String size = null;
             size = drive.getString("size");
 
             if (size != null) {
                 try {
                     image.setTag("size", new Storage<org.dasein.util.uom.storage.Byte>(Long.parseLong(size), Storage.BYTE).toString());
                 } catch (NumberFormatException ignore) {
                     logger.warn("Unknown size value: " + size);
                 }
             }
             String media = drive.getString("media");
             image.setTag("media", media);
 
             String software = null;
             if (drive.has("licenses")) {
                 JSONArray licences = drive.getJSONArray("licenses");
                 for (int i = 0; i < licences.length(); i++) {
                     JSONObject jlicense = licences.getJSONObject(i);
 
                     if (jlicense.has("licenses")) {
                         software = jlicense.getString("licenses");
                     }
                     if (software != null) {
                         image.setSoftware(software);
                         break;
                     } else {
                         image.setSoftware("");
                     }
                 }
             }
             if (image.getSoftware() == null) {
                 image.setSoftware("");
             }
 
             Platform platform = Platform.UNKNOWN;
             if (os != null && !os.equals("")) {
                 platform = Platform.guess(os);
             }
 
             if (platform.equals(Platform.UNKNOWN)) {
                 platform = Platform.guess(image.getName());
                 if (platform.equals(Platform.UNKNOWN)) {
                     //check description followed by install notes
                     platform = Platform.guess(image.getDescription());
                     if (platform.equals(Platform.UNKNOWN)){
                         platform = Platform.guess(install_notes);
                     }
                 }
             } else if (platform.equals(Platform.UNIX)) {
                 Platform p = Platform.guess(image.getName());
 
                 if (!p.equals(Platform.UNKNOWN)) {
                     platform = p;
                 }
             }
 
             image.setPlatform(platform);
 
             if (image.getProviderOwnerId() == null) {
                 image.setProviderOwnerId(ctx.getAccountNumber());
             }
             if (image.getProviderMachineImageId() == null) {
                 return null;
             }
             if (image.getName() == null) {
                 image.setName(image.getProviderMachineImageId());
             }
             if (image.getDescription() == null) {
                 image.setDescription(image.getName());
             }
             return image;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nullable MachineImage toPublicMachineImage(@Nullable JSONObject drive) throws CloudException, InternalException {
         if (drive == null) {
             logger.debug("drive is null");
             return null;
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if (ctx == null) {
                 throw new NoContextException();
             }
             String regionId = ctx.getRegionId();
 
             if (regionId == null) {
                 throw new CloudSigmaConfigurationException("No region was specified for this request");
             }
             MachineImage image = new MachineImage();
 
             image.setProviderRegionId(regionId);
             image.setCurrentState(MachineImageState.PENDING);
             image.setType(MachineImageType.VOLUME);
             image.setImageClass(ImageClass.MACHINE);
             String id = drive.getString("uuid");
 
             if (id != null && !id.equals("")) {
                 image.setProviderMachineImageId(id);
             }
             String name = drive.getString("name");
             if (name != null && !name.equals("")) {
                 image.setName(name);
             }
 
             String description = null;
             String os = null;
             String install_notes = null;
             if (drive.has("description")) {
                 description = drive.getString("description");
             }
             if (drive.has("install_notes")) {
                 install_notes = drive.getString("install_notes");
             }
             if (drive.has("os")) {
                 os = drive.getString("os");
             }
             String bits = null;
             if (drive.has("arch")) {
                 bits = drive.getString("arch");
             }
 
             if (bits != null && bits.contains("32")) {
                 image.setArchitecture(Architecture.I32);
             } else {
                 image.setArchitecture(Architecture.I64);
             }
 
             if (description != null && !description.equals("")) {
                 image.setDescription(description);
             }
             else if (install_notes != null && !install_notes.equals("")) {
                 image.setDescription(install_notes);
             }
 
             String user = null;
             if (drive.has("owner") && !drive.isNull("owner")) {
                 JSONObject owner = drive.getJSONObject("owner");
                 if (owner != null && owner.has("uuid")) {
                     user = owner.getString("uuid");
                 }
             }
             if (user != null && !user.equals("")) {
                 image.setProviderOwnerId(user);
             } else {
                 image.setProviderOwnerId("00000000-0000-0000-0000-000000000001");
             }
             String s = drive.getString("status");
 
             if (s != null) {
                 if( s.equalsIgnoreCase("copying") || s.equalsIgnoreCase("mounted")) {
                     image.setCurrentState(MachineImageState.PENDING);
                 }
                 else if( s.equalsIgnoreCase("unmounted") ) {
                     image.setCurrentState(MachineImageState.ACTIVE);
                 }
                 else if( s.equalsIgnoreCase("unavailable") ) {
                     image.setCurrentState(MachineImageState.DELETED);
                 }
                 else {
                     logger.warn("WARN: Unknown drive state for CloudSigma: " + s);
                 }
             }
 
             String size = null;
             size = drive.getString("size");
 
             if (size != null) {
                 try {
                     image.setTag("size", new Storage<org.dasein.util.uom.storage.Byte>(Long.parseLong(size), Storage.BYTE).toString());
                 } catch (NumberFormatException ignore) {
                     logger.warn("Unknown size value: " + size);
                 }
             }
 
             String media = drive.getString("media");
             image.setTag("media", media);
 
             String software = null;
             if (drive.has("licenses") && !drive.isNull("licenses")) {
                 JSONArray licences = drive.getJSONArray("licenses");
                 for (int i = 0; i < licences.length(); i++) {
                     JSONObject jlicense = licences.getJSONObject(i);
 
                     if (jlicense.has("license") && !jlicense.isNull("license")) {
                         JSONObject li = jlicense.getJSONObject("license");
                         if (li.has("long_name") && !li.isNull("long_name")) {
                             software = li.getString("long_name");
                         }
                     }
                     if (software != null) {
                         image.setSoftware(software);
                         break;
                     } else {
                         image.setSoftware("");
                     }
                 }
             }
             if (image.getSoftware() == null) {
                 image.setSoftware("");
             }
 
             Platform platform = Platform.UNKNOWN;
             if (os != null && !os.equals("")) {
                 platform = Platform.guess(os);
             }
 
             if (platform.equals(Platform.UNKNOWN)) {
                 platform = Platform.guess(image.getName());
                 if (platform.equals(Platform.UNKNOWN)) {
                     //check description followed by install notes
                     platform = Platform.guess(image.getDescription());
                     if (platform.equals(Platform.UNKNOWN)){
                         platform = Platform.guess(install_notes);
                     }
                 }
             } else if (platform.equals(Platform.UNIX)) {
                 Platform p = Platform.guess(image.getName());
 
                 if (!p.equals(Platform.UNKNOWN)) {
                     platform = p;
                 }
             }
 
             image.setPlatform(platform);
 
             if (image.getProviderOwnerId() == null) {
                 image.setProviderOwnerId(ctx.getAccountNumber());
             }
             if (image.getProviderMachineImageId() == null) {
                 return null;
             }
             if (image.getName() == null) {
                 image.setName(image.getProviderMachineImageId());
             }
             if (image.getDescription() == null) {
                 image.setDescription(image.getName());
             }
             return image;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject drive) throws CloudException, InternalException {
         if (drive == null) {
             return null;
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if (ctx == null) {
                 throw new NoContextException();
             }
 
             String id = drive.getString("uuid");
 
             if (id == null || id.equals("")) {
                 return null;
             }
             MachineImageState state = MachineImageState.PENDING;
             String s = drive.getString("status");
 
             if (s != null) {
                 if (s.equalsIgnoreCase("mounted") || s.equalsIgnoreCase("unmounted")) {
                     state = MachineImageState.ACTIVE;
                 } else if (s.equalsIgnoreCase("unavailable")) {
                     state = MachineImageState.DELETED;
                 } else if (s.startsWith("copying")) {
                     state = MachineImageState.PENDING;
                 } else {
                     logger.warn("DEBUG: Unknown drive state for CloudSigma: " + s);
                 }
             }
             return new ResourceStatus(id, state);
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nonnull String toDriveURL(@Nonnull String vmId, @Nonnull String action) throws InternalException {
         try {
             return ("/drives/" + URLEncoder.encode(vmId, "utf-8") + "/" + action);
         } catch (UnsupportedEncodingException e) {
             logger.error("UTF-8 not supported: " + e.getMessage());
             throw new InternalException(e);
         }
     }
 
     private @Nonnull String toPublicImageURL(@Nonnull String vmId, @Nonnull String action) throws InternalException {
         try {
             return ("/libdrives/" + URLEncoder.encode(vmId, "utf-8") + "/" + action);
         } catch (UnsupportedEncodingException e) {
             logger.error("UTF-8 not supported: " + e.getMessage());
             throw new InternalException(e);
         }
     }
 }
