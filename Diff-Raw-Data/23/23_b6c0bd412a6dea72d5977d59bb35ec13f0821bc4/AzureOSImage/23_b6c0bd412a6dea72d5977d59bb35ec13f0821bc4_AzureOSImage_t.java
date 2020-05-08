 package org.dasein.cloud.azure.compute.image;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.azure.Azure;
 import org.dasein.cloud.azure.AzureConfigException;
 import org.dasein.cloud.azure.AzureMethod;
 import org.dasein.cloud.azure.AzureService;
 import org.dasein.cloud.azure.compute.vm.AzureVM;
 import org.dasein.cloud.compute.*;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.util.Jiterator;
 import org.dasein.util.JiteratorPopulator;
 import org.dasein.util.PopulatorThread;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * Implements support for Azure OS images through the Dasein Cloud machine image API.
  * @author George Reese (george.reese@imaginary.com)
  * @since 2012.04.1
  * @version 2012.04.1
  */
 public class AzureOSImage extends AbstractImageSupport {
     static private final Logger logger = Azure.getLogger(AzureOSImage.class);
 
     static private final String IMAGES = "/services/images";
     static private final String MICROSOFT = "--microsoft--";
 
     private Azure provider;
     
     public AzureOSImage(Azure provider) {
         super(provider);
         this.provider = provider;
     }
 
     @Override
     public void addImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to share images");
     }
 
     @Override
     public void addPublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to make images public");
     }
 
     @Nonnull
     @Override
     public String bundleVirtualMachine(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to bundle vms");
     }
 
     @Override
     public void bundleVirtualMachineAsync(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name, @Nonnull AsynchronousTask<String> trackingTask) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to bundle vms");
     }
 
     @Override
     protected @Nonnull MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         try {
             if( task != null ) {
                 task.setStartTime(System.currentTimeMillis());
             }
 
             String vmid = options.getVirtualMachineId();
             String name = options.getName();
             String description = options.getDescription();
 
             VirtualMachine vm;
 
             vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(options.getVirtualMachineId());
             if (vm == null) {
                 throw new CloudException("Virtual machine not found: " + options.getVirtualMachineId());
             }
            if (!vm.getCurrentState().equals(VmState.STOPPED)) {
                throw new CloudException("Server must be stopped before making an image of it");
            }
             try {
                 ProviderContext ctx = provider.getContext();
 
                 if( ctx == null ) {
                     throw new AzureConfigException("No context was set for this request");
                 }
 
                 String vmId = vm.getProviderVirtualMachineId();
                 String[] parts = vmId.split(":");
                 String serviceName, deploymentName, roleName;
 
                 if (parts.length == 3)    {
                     serviceName = parts[0];
                     deploymentName = parts[1];
                     roleName= parts[2];
                 }
                 else if( parts.length == 2 ) {
                     serviceName = parts[0];
                     deploymentName = parts[1];
                     roleName = serviceName;
                 }
                 else {
                     serviceName = vmId;
                     deploymentName = vmId;
                     roleName = vmId;
                 }
                 String resourceDir = AzureVM.HOSTED_SERVICES + "/" + serviceName + "/deployments/" +  deploymentName + "/roleInstances/" + roleName + "/Operations";
                 AzureMethod method = new AzureMethod(provider);
                 StringBuilder xml = new StringBuilder();
 
                 xml.append("<CaptureRoleOperation xmlns=\"http://schemas.microsoft.com/windowsazure\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">");
                 xml.append("<OperationType>CaptureRoleOperation</OperationType>\n");
                 xml.append("<PostCaptureAction>Delete</PostCaptureAction>\n");
                 xml.append("<TargetImageLabel>").append(name).append("</TargetImageLabel>\n");
                xml.append("<TargetImageName>").append(name).append("</TargetImageName>\n");
                 xml.append("</CaptureRoleOperation>\n");
 
                 method.post(ctx.getAccountNumber(), resourceDir, xml.toString());
 
                 MachineImage img = getMachineImage(name);;
 
                 if (img == null) {
                     throw new CloudException("Drive cloning completed, but no ID was provided for clone");
                 }if( task != null ) {
                     task.completeWithResult(img);
                 }
                 return img;
             }
             finally {
                 if( logger.isTraceEnabled() ) {
                     logger.trace("EXIT: " + AzureOSImage.class.getName() + ".launch()");
                 }
             }
         } finally {
             provider.release();
         }
     }
 
     @Override
     public MachineImage getImage(@Nonnull String machineImageId) throws CloudException, InternalException {
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         PopulatorThread<MachineImage> populator;
 
         provider.hold();
         populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
             public void populate(@Nonnull Jiterator<MachineImage> iterator) throws CloudException, InternalException {
                 try {
 
                     populateImages(ctx, iterator, MICROSOFT, ctx.getAccountNumber(), "--public--",
                             "--Canonical--", "--RightScaleLinux--", "--RightScaleWindows--",
                             "--OpenLogic--", "--SUSE--");
                 }
                 finally {
                     provider.release();
                 }
             }
         });
         populator.populate();
         for( MachineImage img : populator.getResult() ) {
             if( machineImageId.equals(img.getProviderMachineImageId()) ) {
                 logger.debug("Found image i'm looking for "+machineImageId);
                 img.setImageClass(ImageClass.MACHINE);
                 return (AzureMachineImage)img;
             }
         }
         return null;
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale) {
         return "OS image";
     }
 
     @Nonnull
     @Override
     public String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         return "OS image";
     }
 
     @Nonnull
     @Override
     public String getProviderTermForCustomImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         return "OS image";
     }
 
     @Override
     public boolean hasPublicLibrary() {
         return true;
     }
 
     @Nonnull
     @Override
     public Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
         MachineImage img = getMachineImage(machineImageId);
         
         return (img != null &&
                 (MICROSOFT.equals(img.getProviderOwnerId())
                     || "--public--".equals(img.getProviderOwnerId())
                     || "--Canonical--".equals(img.getProviderOwnerId())
                     || "--RightScaleLinux--".equals(img.getProviderOwnerId())
                     || "--RightScaleWindows--".equals(img.getProviderOwnerId())
                     || "--OpenLogic--".equals(img.getProviderOwnerId())
                     || "--SUSE--".equals(img.getProviderOwnerId())
                 )
                 );
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         return provider.getDataCenterServices().isSubscribed(AzureService.COMPUTE);
     }
 
     @Nonnull
     @Override
     public Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         if (!cls.equals(ImageClass.MACHINE) ) {
              return Collections.emptyList();
         }
 
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
 
         final String owner = ctx.getAccountNumber();
 
         AzureMethod method = new AzureMethod(provider);
 
         Document doc = method.getAsXML(ctx.getAccountNumber(), IMAGES);
 
         if( doc == null ) {
             throw new CloudException(CloudErrorType.AUTHENTICATION, HttpServletResponse.SC_FORBIDDEN, "Illegal Access", "Illegal access to requested resource");
         }
         NodeList entries = doc.getElementsByTagName("OSImage");
 
         for( int i=0; i<entries.getLength(); i++ ) {
             Node entry = entries.item(i);
             ResourceStatus status = toStatus(ctx, entry);
 
             if( status != null ) {
                 list.add(status);
             }
         }
         return list;
     }
 
     public ResourceStatus toStatus(@Nonnull ProviderContext ctx, @Nullable Node entry) throws CloudException, InternalException {
         String regionId = ctx.getRegionId();
         String id = "";
 
         NodeList attributes = entry.getChildNodes();
         for( int j=0; j<attributes.getLength(); j++ ) {
             Node attribute = attributes.item(j);
             if(attribute.getNodeType() == Node.TEXT_NODE) continue;
             String nodeName = attribute.getNodeName();
 
             if( nodeName.equalsIgnoreCase("name") && attribute.hasChildNodes() ) {
                 id = (attribute.getFirstChild().getNodeValue().trim());
             }
             else if( nodeName.equalsIgnoreCase("category") && attribute.hasChildNodes() ) {
                 if (!"user".equalsIgnoreCase(attribute.getFirstChild().getNodeValue().trim())) {
                     return null;
                 }
             }
             else if( nodeName.equalsIgnoreCase("location") && attribute.hasChildNodes() ) {
                 if (!regionId.equalsIgnoreCase(attribute.getFirstChild().getNodeValue().trim())) {
                     return null;
                 }
             }
         }
         return new ResourceStatus(id, MachineImageState.ACTIVE);
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> listImages(@Nullable ImageFilterOptions imageFilterOptions) throws CloudException, InternalException {
         if (!imageFilterOptions.getImageClass().equals(ImageClass.MACHINE)) {
             return Collections.emptyList();
         }
 
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
         AzureMethod method = new AzureMethod(provider);
 
         Document doc = method.getAsXML(ctx.getAccountNumber(), IMAGES);
 
         if( doc == null ) {
             throw new CloudException(CloudErrorType.AUTHENTICATION, HttpServletResponse.SC_FORBIDDEN, "Illegal Access", "Illegal access to requested resource");
         }
         NodeList entries = doc.getElementsByTagName("OSImage");
 
         for( int i=0; i<entries.getLength(); i++ ) {
             Node entry = entries.item(i);
             AzureMachineImage image = toImage(ctx, entry);
 
             if (image != null) {
                 image.setImageClass(ImageClass.MACHINE);
 
                 if (imageFilterOptions.matches(image)) {
                    if (imageFilterOptions.getAccountNumber() == null) {
                        if (ctx.getAccountNumber().equals(image.getProviderOwnerId())) {
                            images.add(image);
                        }
                    }
                    else if (image.getProviderOwnerId().equalsIgnoreCase(imageFilterOptions.getAccountNumber())) {
                            images.add(image);
                    }
                 }
             }
         }
         return images;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> listImages(@Nonnull ImageClass cls) throws CloudException, InternalException {
         if (!cls.equals(ImageClass.MACHINE)) {
             return Collections.emptyList();
         }
 
         ProviderContext ctx = provider.getContext();
 
         String me = ctx.getAccountNumber();
         ArrayList<MachineImage> allImages = listMachineImages();
 
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 
         for (MachineImage img : allImages) {
             if (img.getProviderOwnerId().equalsIgnoreCase(me)) {
                 img.setImageClass(ImageClass.MACHINE);
                 list.add(img);
             }
         }
         return list;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> listImages(@Nonnull ImageClass cls, @Nonnull String ownedBy) throws CloudException, InternalException {
         if (!cls.equals(ImageClass.MACHINE)) {
             return Collections.emptyList();
         }
 
         ProviderContext ctx = provider.getContext();
 
         String me = ctx.getAccountNumber();
         ArrayList<MachineImage> allImages = listMachineImages();
 
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 
         for (MachineImage img : allImages) {
             if (img.getProviderOwnerId().equalsIgnoreCase(ownedBy)) {
                 img.setImageClass(ImageClass.MACHINE);
                 list.add(img);
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull ArrayList<MachineImage> listMachineImages() throws CloudException, InternalException {
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
         AzureMethod method = new AzureMethod(provider);
 
         Document doc = method.getAsXML(ctx.getAccountNumber(), IMAGES);
 
         if( doc == null ) {
             throw new CloudException(CloudErrorType.AUTHENTICATION, HttpServletResponse.SC_FORBIDDEN, "Illegal Access", "Illegal access to requested resource");
         }
         NodeList entries = doc.getElementsByTagName("OSImage");
 
         for( int i=0; i<entries.getLength(); i++ ) {
             Node entry = entries.item(i);
             AzureMachineImage image = toImage(ctx, entry);
 
             if( image != null ) {
                 if( ctx.getAccountNumber().equalsIgnoreCase(image.getProviderOwnerId())) {
                     list.add(image);
                 }
             }
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         final String[] accounts = (accountId == null ? new String[] { ctx.getAccountNumber() } : new String[] { accountId });
 
         PopulatorThread<MachineImage> populator;
 
         provider.hold();
         populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
             public void populate(@Nonnull Jiterator<MachineImage> iterator) throws CloudException, InternalException {
                 try {
                     populateImages(ctx, iterator, accounts);
                 }
                 finally {
                     provider.release();
                 }
             }
         });
         populator.populate();
         return populator.getResult();
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
          //dmayne 20130417: seems to be the right type
        return Collections.singletonList(MachineImageFormat.VHD);
        // return Collections.singletonList(MachineImageFormat.AWS); // nonsense, I know
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<String> listShares(@Nonnull String forMachineImageId) throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Nonnull
     @Override
     public Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return Collections.singletonList(ImageClass.MACHINE);
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageType.VOLUME);
     }
 
     @Nonnull
     @Override
     public MachineImage registerImageBundle(@Nonnull ImageCreateOptions options) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No image registering is currently supported");
     }
 
     private void populateImages(@Nonnull ProviderContext ctx, @Nonnull Jiterator<MachineImage> iterator, @Nullable String ... accounts) throws CloudException, InternalException {
         AzureMethod method = new AzureMethod(provider);
 
         Document doc = method.getAsXML(ctx.getAccountNumber(), IMAGES);
 
         if( doc == null ) {
             throw new CloudException(CloudErrorType.AUTHENTICATION, HttpServletResponse.SC_FORBIDDEN, "Illegal Access", "Illegal access to requested resource");
         }
         NodeList entries = doc.getElementsByTagName("OSImage");
 
         for( int i=0; i<entries.getLength(); i++ ) {
             Node entry = entries.item(i);
             AzureMachineImage image = toImage(ctx, entry);
 
             if( image != null ) {            	
             	if(accounts != null){            		
             		for( String accountNumber : accounts ) {
             			if( accountNumber.equalsIgnoreCase(image.getProviderOwnerId())) {
             				iterator.push(image);
                             break;
             			}
             		}            	
             	}
                 else if( image.getProviderOwnerId() == null || MICROSOFT.equals(image.getProviderOwnerId()) || "--public--".equals(image.getProviderOwnerId()) ) {
             		iterator.push(image);
             	}         
             }
         }
     }
 
     @Override
     public void remove(@Nonnull String machineImageId) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + AzureOSImage.class.getName() + ".remove(" + machineImageId + ")");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new AzureConfigException("No context was specified for this request");
             }
 
             MachineImage image = getMachineImage(machineImageId);
 
             if( image == null ) {
                 throw new CloudException("No such machine image: " + machineImageId);
             }
 
             AzureMethod method = new AzureMethod(provider);
 
             //dmayne 20130425: delete image blob too
             method.invoke("DELETE",ctx.getAccountNumber(), IMAGES + "/" + machineImageId+"?comp=media", null);
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + AzureOSImage.class.getName() + ".launch()");
             }
         }
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeAllImageShares(@Nonnull String providerImageId) throws CloudException, InternalException {
         //No-OP
     }
 
     @Override
     public void removeImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to share images");
     }
 
     @Override
     public void removePublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("No ability to share images");
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
         final ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new AzureConfigException("No context was specified for this request");
         }
 
         PopulatorThread<MachineImage> populator;
 
         provider.hold();
         populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
             public void populate(@Nonnull Jiterator<MachineImage> iterator) throws CloudException, InternalException {
                 try {
                    populateImages(ctx, iterator, MICROSOFT, "--public--",
                             "--Canonical--", "--RightScaleLinux--", "--RightScaleWindows--",
                             "--OpenLogic--", "--SUSE--");
                 }
                 finally {
                     provider.release();
                 }
             }
         });
         populator.populate();
         for( MachineImage img : populator.getResult() ) {
             if( architecture != null ) {
                 if( !architecture.equals(img.getArchitecture()) ) {
                     continue;
                 }
             }
             if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
                 Platform p = img.getPlatform();
 
                 if( p.equals(Platform.UNKNOWN) ) {
                     continue;
                 }
                 else if( platform.isWindows() ) {
                     if( !p.isWindows() ) {
                         continue;
                     }
                 }
                 else if( platform.equals(Platform.UNIX) ) {
                     if( !p.isUnix() ) {
                         continue;
                     }
                 }
                 else if( !platform.equals(p) ) {
                     continue;
                 }
             }
             if( keyword != null ) {
                 if( !img.getName().contains(keyword) ) {
                     if( !img.getDescription().contains(keyword) ) {
                         if( !img.getProviderMachineImageId().contains(keyword) ) {
                             continue;
                         }
                     }
                 }
             }
             images.add(img);
         }
         return images;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> searchPublicImages(@Nonnull ImageFilterOptions imageFilterOptions) throws InternalException, CloudException {
         Platform platform = imageFilterOptions.getPlatform();
         ImageClass cls = imageFilterOptions.getImageClass();
         return searchPublicImages(null, platform, null, cls);
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> searchImages(@Nullable String accountNumber, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> searchPublicImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
         for (ImageClass a: imageClasses) {
             Iterable<MachineImage> images = searchMachineImages(keyword, platform, architecture);
             for (MachineImage img : images) {
                 if (isImageSharedWithPublic(img.getProviderMachineImageId())) {
                     list.add(img);
                 }
             }
         }
         return list;
     }
 
     @Override
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsDirectImageUpload() throws CloudException, InternalException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
         if (type.equals(MachineImageType.VOLUME)) {
             return true;
         }
         return false;
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
         return true;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void updateTags(@Nonnull String imageId, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void updateTags(@Nonnull String[] strings, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeTags(@Nonnull String s, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeTags(@Nonnull String[] strings, @Nonnull Tag... tags) throws CloudException, InternalException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
     
     private @Nullable AzureMachineImage toImage(@Nonnull ProviderContext ctx, @Nullable Node entry) throws CloudException, InternalException {
         if( entry == null ) {
             return null;
         }
 
         String regionID = ctx.getRegionId();
         AzureMachineImage image= new AzureMachineImage();
 
         HashMap<String,String> tags = new HashMap<String,String>();
         image.setCurrentState(MachineImageState.ACTIVE);
         image.setProviderRegionId(ctx.getRegionId());
         image.setArchitecture(Architecture.I64);
 
         NodeList attributes = entry.getChildNodes();
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             if(attribute.getNodeType() == Node.TEXT_NODE) continue;
             String nodeName = attribute.getNodeName();
 
             if( nodeName.equalsIgnoreCase("name") && attribute.hasChildNodes() ) {
                 image.setProviderMachineImageId(attribute.getFirstChild().getNodeValue().trim());
             }
             if( nodeName.equalsIgnoreCase("category") && attribute.hasChildNodes() ) {
                 String c = attribute.getFirstChild().getNodeValue().trim();
                 if( "user".equalsIgnoreCase(c) ) {
                     image.setProviderOwnerId(ctx.getAccountNumber());
                 }
                 else if( c.toLowerCase().contains("microsoft") ) {
                     image.setProviderOwnerId(MICROSOFT);
                 }
                 else if( c.toLowerCase().contains("partner") ) {
                     image.setProviderOwnerId("--public--");
                 }
                 else if( c.toLowerCase().contains("canonical") ) {
                     image.setProviderOwnerId("--Canonical--");
                 }
                 else if( c.toLowerCase().contains("rightscale with linux") ) {
                     image.setProviderOwnerId("--RightScaleLinux--");
                 }
                 else if( c.toLowerCase().contains("rightscale with windows") ) {
                     image.setProviderOwnerId("--RightScaleWindows--");
                 }
                 else if( c.toLowerCase().contains("openlogic") ) {
                     image.setProviderOwnerId("--OpenLogic--");
                 }
                 else if( c.toLowerCase().contains("suse") ) {
                     image.setProviderOwnerId("--SUSE--");
                 }
             }
             else if( nodeName.equalsIgnoreCase("label") && attribute.hasChildNodes() ) {
                 image.setName(attribute.getFirstChild().getNodeValue().trim());
 
             }
             else if( nodeName.equalsIgnoreCase("description") && attribute.hasChildNodes() ) {
                 image.setDescription(attribute.getFirstChild().getNodeValue().trim());
             }
             else if( nodeName.equalsIgnoreCase("location") && attribute.hasChildNodes() ) {
                 String location = attribute.getFirstChild().getNodeValue().trim();
                 String[] locations = location.split(";");
                 boolean found = false;
                 for (String loc : locations) {
                     if (regionID.equalsIgnoreCase(loc)) {
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     return null;
                 }
             }
             else if( nodeName.equalsIgnoreCase("medialink") && attribute.hasChildNodes() ) {
                 image.setMediaLink(attribute.getFirstChild().getNodeValue().trim());
             }
             else if( nodeName.equalsIgnoreCase("os") && attribute.hasChildNodes() ) {
                 String os = attribute.getFirstChild().getNodeValue().trim();
 
                 if( os.equalsIgnoreCase("windows") ) {
                     image.setPlatform(Platform.WINDOWS);
                 }
                 else if( os.equalsIgnoreCase("linux") ) {
                     image.setPlatform(Platform.UNIX);
                 }
             }
         }
         if( image.getProviderMachineImageId() == null ) {
             return null;
         }
         if( image.getName() == null ) {
             image.setName(image.getProviderMachineImageId());
         }
         if( image.getDescription() == null ) {
             image.setDescription(image.getName());
         }
         String descriptor = image.getProviderMachineImageId() + " " + image.getName() + " " + image.getDescription();
 
         if( image.getPlatform() == null || image.getPlatform().equals(Platform.UNIX) ) {
             Platform p = Platform.guess(descriptor);
 
             if( image.getPlatform() == null || !Platform.UNKNOWN.equals(p) ) {
                 image.setPlatform(p);
             }
         }
         image.setSoftware(descriptor.contains("SQL Server") ? "SQL Server" : "");
         image.setTags(tags);
         image.setType(MachineImageType.VOLUME);
 
       //  AzureMachineImage img = (AzureMachineImage) MachineImage.getMachineImageInstance(image.getProviderOwnerId(), image.getProviderRegionId(), image.getProviderMachineImageId(), image.getCurrentState(), image.getName(), image.getDescription(), image.getArchitecture(), image.getPlatform());
        // img.setMediaLink(image.getMediaLink());
        // img.withSoftware(image.getSoftware());
 
         return image;
     }
 }
