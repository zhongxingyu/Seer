 /**
  * Copyright (C) 2009-2012 enStratus Networks Inc
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
 
 package org.dasein.cloud.openstack.nova.os.compute;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.compute.AbstractImageSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ComputeServices;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.ImageFilterOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VirtualMachineSupport;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.openstack.nova.os.NovaException;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class NovaImage extends AbstractImageSupport {
     static private final Logger logger = NovaOpenStack.getLogger(NovaImage.class, "std");
 
     NovaImage(NovaOpenStack provider) {
         super(provider);
     }
 
     public @Nullable String getImageRef(@Nonnull String machineImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.getImageRef");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/images", machineImageId, true);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("image") ) {
                     JSONObject image = ob.getJSONObject("image");
                     JSONArray links = image.getJSONArray("links");
                     String def = null;
                     
                     for( int j=0; j<links.length(); j++ ) {
                         JSONObject link = links.getJSONObject(j);
                         
                         if( link.getString("rel").equals("self") ) {
                             return link.getString("href");
                         }
                         else if( def == null ) {
                             def = link.optString("href");
                         }
                     }
                     return def;
                 }
                 return null;
             }
             catch( JSONException e ) {
                 logger.error("getImageRef(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for images: " + e.getMessage());
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     protected MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.capture");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             HashMap<String,Object> action = new HashMap<String,Object>();
 
             action.put("name", options.getName());
             if( task != null ) {
                 task.setStartTime(System.currentTimeMillis());
             }
             String vmId = options.getVirtualMachineId();
             Platform platform = null;
 
             if( vmId != null ) {
                 long timeout = (System.currentTimeMillis() + CalendarWrapper.MINUTE*10L);
 
                 while( timeout > System.currentTimeMillis() ) {
                     try {
                         ComputeServices services = getProvider().getComputeServices();
                         VirtualMachine vm = null;
 
                         if( services != null ) {
                             VirtualMachineSupport support = services.getVirtualMachineSupport();
 
                             if( support != null ) {
                                 vm = support.getVirtualMachine(vmId);
                             }
                         }
                         if( vm == null ) {
                             throw new CloudException("No such virtual machine: " + vmId);
                         }
                         platform = vm.getPlatform();
                         if( !VmState.PENDING.equals(vm.getCurrentState()) ) {
                             break;
                         }
                     }
                     catch( Throwable ignore ) {
                         // ignore
                     }
                     try { Thread.sleep(15000L); }
                     catch( InterruptedException ignore ) { }
                 }
             }
             JSONObject result;
 
             if( ((NovaOpenStack)getProvider()).isPostCactus() ) {
                 HashMap<String,Object> json = new HashMap<String,Object>();
                 HashMap<String,String> metaData = new HashMap<String,String>();
 
                 metaData.put("org.dasein.description", options.getDescription());
                 if( platform != null ) {
                     metaData.put("org.dasein.platform", platform.name());
                 }
                 action.put("metadata", metaData);
                 json.put("createImage", action);
 
                 result = method.postServers("/servers", vmId, new JSONObject(json), true);
             }
             else {
                 HashMap<String,Object> json = new HashMap<String,Object>();
 
                 action.put("serverId", String.valueOf(vmId));
                 json.put("image", action);
                 result = method.postServers("/images", null, new JSONObject(json), true);
             }
             if( result != null && result.has("image") ) {
                 try {
                     JSONObject img = result.getJSONObject("image");
                     MachineImage image = toImage(img);
 
                     if( image != null ) {
                         if( task != null ) {
                             task.completeWithResult(image);
                         }
                         return image;
                     }
                 }
                 catch( JSONException e ) {
                     throw new CloudException(e);
                 }
             }
             else if( result != null && result.has("location") ) {
                 try {
                     long timeout = System.currentTimeMillis() + CalendarWrapper.MINUTE * 20L;
                     String location = result.getString("location");
                     int idx = location.lastIndexOf('/');
 
                     if( idx > 0 ) {
                         location = location.substring(idx+1);
                     }
 
                     while( timeout > System.currentTimeMillis() ) {
                         MachineImage image = getImage(location);
 
                         if( image != null ) {
                             if( task != null ) {
                                 task.completeWithResult(image);
                             }
                             return image;
                         }
                         try { Thread.sleep(15000L); }
                         catch( InterruptedException ignore ) { }
                     }
                 }
                 catch( JSONException e ) {
                     throw new CloudException(e);
                 }
             }
             logger.error("No image was created by the imaging attempt, and no error was returned");
             throw new CloudException("No image was created");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.getImage");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/images", providerImageId, true);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("image") ) {
                     JSONObject server = ob.getJSONObject("image");
                     MachineImage img = toImage(server);
 
                     if( img != null ) {
                         return img;
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("getMachineImage(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for images: " + e.getMessage());
             }
             return null;
         }
         finally {
             APITrace.begin(getProvider(), "Image.getImage");
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         switch( cls ) {
             case MACHINE: return "machine image";
             case KERNEL: return "kernel image";
             case RAMDISK: return "ramdisk image";
         }
         return "image";
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
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.isSubscribed");
         try {
             return (getProvider().testContext() != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.listImageStatus");
         try {
             if( !cls.equals(ImageClass.MACHINE) ) {
                 return Collections.emptyList();
             }
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/images", null, true);
             ArrayList<ResourceStatus> images = new ArrayList<ResourceStatus>();
 
             try {
                 if( ob != null && ob.has("images") ) {
                     JSONArray list = ob.getJSONArray("images");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject image = list.getJSONObject(i);
                         ResourceStatus img = toStatus(image);
 
                         if( img != null ) {
                             images.add(img);
                         }
 
                     }
                 }
             }
             catch( JSONException e ) {
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for images: " + e.getMessage());
             }
             return images;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.listImages");
         try {
             String account = (options == null ? null : options.getAccountNumber());
 
             if( account == null ) {
                 if( options == null ) {
                     options = ImageFilterOptions.getInstance().withAccountNumber(getContext().getAccountNumber());
                 }
                 else {
                     options.withAccountNumber(getContext().getAccountNumber());
                 }
             }
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/images", null, true);
             ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
             try {
                 if( ob != null && ob.has("images") ) {
                     JSONArray list = ob.getJSONArray("images");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject image = list.getJSONObject(i);
                         MachineImage img = toImage(image);
 
                         if( img != null && options.matches(img) ) {
                             images.add(img);
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for images: " + e.getMessage());
             }
             return images;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         ArrayList<ImageClass> values = new ArrayList<ImageClass>();
 
         Collections.addAll(values, ImageClass.values());
         return values;
     }
 
     @Override
     public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageType.VOLUME);
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.remove");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             long timeout = System.currentTimeMillis() + CalendarWrapper.HOUR;
 
             do {
                 try {
                     method.deleteServers("/images", providerImageId);
                     return;
                 }
                 catch( NovaException e ) {
                     if( e.getHttpCode() != HttpServletResponse.SC_CONFLICT ) {
                         throw e;
                     }
                 }
                 try { Thread.sleep(CalendarWrapper.MINUTE); }
                 catch( InterruptedException e ) { /* ignore */ }
             } while( System.currentTimeMillis() < timeout );
         }
         finally {
             APITrace.end();
         }
     }
 
     public @Nonnull Iterable<MachineImage> searchPublicImages(@Nonnull ImageFilterOptions options) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Image.searchPublicImages");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getServers("/images", null, true);
             ArrayList<MachineImage> images = new ArrayList<MachineImage>();
             String me = getContext().getAccountNumber();
 
             try {
                 if( ob != null && ob.has("images") ) {
                     JSONArray list = ob.getJSONArray("images");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject image = list.getJSONObject(i);
                         MachineImage img = toImage(image);
 
                         if( img != null && !img.getProviderOwnerId().equals(me) && options.matches(img) ) {
                             images.add(img);
                         }
                     }
                 }
             }
             catch( JSONException e ) {
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for images: " + e.getMessage());
             }
             return images;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsDirectImageUpload() throws CloudException, InternalException {
         return false; // need Glance support
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return true;
     }
 
     public @Nullable MachineImage toImage(@Nullable JSONObject json) throws CloudException, InternalException {
         Logger logger = NovaOpenStack.getLogger(NovaImage.class, "std");
         
         if( logger.isTraceEnabled() ) {
             logger.trace("enter - " + NovaImage.class.getName() + ".toImage(" + json + ")");
         }
         try {
             if( json == null ) {
                 return null;
             }
             try {
                 String imageId = (json.has("id") ? json.getString("id") : null);
                 String name = (json.has("name") ? json.getString("name") : null);
                 String description = (json.has("description") ? json.getString("description") : null);
                 JSONObject md = (json.has("metadata") ? json.getJSONObject("metadata") : null);
                 String owner = getContext().getAccountNumber();
                 Architecture architecture = Architecture.I64;
                 Platform platform = Platform.UNKNOWN;
 
                 if( md != null ) {
                     if( description == null && md.has("org.dasein.description") ) {
                         description = md.getString("org.dasein.description");
                     }
                     if( md.has("org.dasein.platform") ) {
                         try {
                             platform = Platform.valueOf(md.getString("org.dasein.platform"));
                         }
                         catch( Throwable ignore ) {
                             // ignore
                         }
                     }
                     String[] akeys = { "arch", "architecture", "org.openstack__1__architecture", "com.hp__1__architecture" };
                     String a = null;
 
                     for( String key : akeys ) {
                         if( md.has(key) ) {
                             a = md.getString(key);
                             if( a != null ) {
                                 break;
                             }
                         }
                     }
                     if( a != null ) {
                         a = a.toLowerCase();
                         if( a.contains("32") ) {
                             architecture = Architecture.I32;
                         }
                         else if( a.contains("sparc") ) {
                             architecture = Architecture.SPARC;
                         }
                         else if( a.contains("power") ) {
                             architecture = Architecture.POWER;
                         }
                     }
                     if( md.has("os_type") ) {
                         Platform p = Platform.guess(md.getString("os_type"));
 
                         if( !p.equals(Platform.UNKNOWN) ) {
                             if( platform.equals(Platform.UNKNOWN) ) {
                                 platform = p;
                             }
                             else if( platform.equals(Platform.UNIX) && !p.equals(Platform.UNIX) ) {
                                 platform = p;
                             }
                         }
                     }
                     if( md.has("owner") ) {
                         owner = md.getString("owner");
                     }
                 }
 
                 long created = (json.has("created") ? ((NovaOpenStack)getProvider()).parseTimestamp(json.getString("created")) : -1L);
 
                 MachineImageState currentState = MachineImageState.PENDING;
 
                 if( json.has("status") ) {
                     String s = json.getString("status").toLowerCase();
 
                     if( s.equals("saving") ) {
                         currentState = MachineImageState.PENDING;
                     }
                     else if( s.equals("active") || s.equals("queued") || s.equals("preparing") ) {
                         currentState = MachineImageState.ACTIVE;
                     }
                     else if( s.equals("deleting") ) {
                         currentState = MachineImageState.PENDING;
                     }
                     else if( s.equals("failed") ) {
                         return null;
                     }
                     else {
                         logger.warn("toImage(): Unknown image status: " + s);
                         currentState = MachineImageState.PENDING;
                     }
                 }
 
                 if( imageId == null ) {
                     return null;
                 }
                 if( name == null ) {
                     name = imageId;
                 }
                 if( description == null ) {
                     description = name;
                 }
                 if( platform.equals(Platform.UNKNOWN) ) {
                     platform = Platform.guess(name + " " + description);
                 }
                 else if( platform.equals(Platform.UNIX) ) {
                     Platform p = Platform.guess(name + " " + description);
 
                     if( !p.equals(Platform.UNKNOWN) ) {
                         platform = p;
                     }
                 }
                 MachineImage image = MachineImage.getMachineImageInstance(owner, getContext().getRegionId(), imageId, currentState, name, description, architecture, platform).createdAt(created);
 
                 if( md != null ) {
                     String[] names = JSONObject.getNames(md);
 
                     if( names != null && names.length > 0 ) {
                         for( String key : names ) {
                             String value = md.getString(key);
 
                             if( value != null ) {
                                 image.setTag(key, value);
                             }
                         }
                     }
                 }
                 return image;
             }
             catch( JSONException e ) {
                 throw new CloudException(e);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("exit - " + NovaImage.class.getName() + ".toImage()");
             }            
         }
     }
 
     public @Nullable ResourceStatus toStatus(@Nullable JSONObject json) throws CloudException, InternalException {
 
         if( json == null ) {
             return null;
         }
         String owner = getContext().getAccountNumber();
         MachineImageState state = MachineImageState.PENDING;
         String id = null;
 
         try {
             if( json.has("id") ) {
                 id = json.getString("id");
             }
             if( id == null ) {
                 return null;
             }
             JSONObject md = (json.has("metadata") ? json.getJSONObject("metadata") : null);
 
            if( md != null && md.has("owner") ) {
                 owner = md.getString("owner");
             }
             if( json.has("status") ) {
                 String s = json.getString("status").toLowerCase();
 
                 if( s.equals("saving") ) {
                     state = MachineImageState.PENDING;
                 }
                 else if( s.equals("active") || s.equals("queued") || s.equals("preparing") ) {
                     state = MachineImageState.ACTIVE;
                 }
                 else if( s.equals("deleting") ) {
                     state = MachineImageState.PENDING;
                 }
                 else if( s.equals("failed") ) {
                     return null;
                 }
                 else {
                     state = MachineImageState.PENDING;
                 }
             }
         }
         catch( JSONException e ) {
             throw new InternalException(e);
         }
         if( !owner.equals(getContext().getAccountNumber()) ) {
             return null;
         }
         return new ResourceStatus(id, state);
     }
 }
