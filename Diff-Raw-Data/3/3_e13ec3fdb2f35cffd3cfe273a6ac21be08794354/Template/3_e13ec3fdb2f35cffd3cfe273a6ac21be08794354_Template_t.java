 package org.dasein.cloud.terremark.compute;
 
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageFormat;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageSupport;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.dc.DataCenter;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.terremark.EnvironmentsAndComputePools;
 import org.dasein.cloud.terremark.Terremark;
 import org.dasein.cloud.terremark.TerremarkException;
 import org.dasein.cloud.terremark.TerremarkMethod;
 import org.dasein.cloud.terremark.TerremarkMethod.HttpMethodName;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Template  implements MachineImageSupport {
 
 	public enum ImageType {
 		TEMPLATE, CATALOG_ENTRY;
 	}
 
 	// API Calls
 	public final static String TEMPLATES            = "templates";
 	public final static String CATALOG              = "catalog";
 	public final static String CONFIGURATION        = "configuration";
 
 	// Response Tags
 	public final static String TEMPLATE_TAG         = "Template";
 	public final static String CATALOG_ENTRY_TAG    = "CatalogEntry";
 	public final static String NETWORK_MAPPING_TAG  = "NetworkMapping";
 
 	// Types
 	public final static String TEMPLATE_TYPE        = "application/vnd.tmrk.cloud.template";
 
 	// Operation Names
 	public final static String CREATE_CATALOG_OPERATION = "Create Catalog Item";
 
 	public final static String NETWORK_MAPPING_NAME = "NetworkMappingName";
 
 	// Default Task Wait Times
 	public final static long DEFAULT_SLEEP             = CalendarWrapper.SECOND * 30;
 	public final static long DEFAULT_TIMEOUT           = CalendarWrapper.HOUR * 6;
 
 	static Logger logger = Terremark.getLogger(Template.class);
 
 	private Terremark provider;
 
 	Template(Terremark provider) {
 		this.provider = provider;
 	}
 
 	/**
 	 * Adds the specified account number to the list of accounts with which this image is shared.
 	 * @param providerImageId the unique ID of the image to be shared
 	 * @param accountNumber the account number with which the image will be shared
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support sharing images with other accounts
 	 */
 	@Override
 	public void addImageShare(String providerImageId, String accountNumber) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot share images");
 	}
 
 	/**
 	 * Shares the specified image with the public.
 	 * @param providerImageId the unique ID of the image to be made public
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support sharing images with the public
 	 */
 	@Override
 	public void addPublicShare(String providerImageId) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot share images");
 	}
 
 	/**
 	 * Bundles the specified virtual machine to cloud storage so it may be registered as a machine image. Upon completion
 	 * of this task, there should be a file or set of files that capture the target virtual machine in a file format
 	 * that can later be registered into a machine image.
 	 * @param virtualMachineId the virtual machine to be bundled
 	 * @param format the format in which the VM should be bundled
 	 * @param bucket the bucket to which the VM should be bundled
 	 * @param name the name of the object to be created or the prefix for the name
 	 * @return the location of the bundle file or a manifest to the bundle file
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public String bundleVirtualMachine(String virtualMachineId, MachineImageFormat format, String bucket, String name) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot bundle images to cloud storage");
 	}
 
 	/**
 	 * Bundles the specified virtual machine to cloud storage so it may be registered as a machine image. Upon completion
 	 * of this task, there should be a file or set of files that capture the target virtual machine in a file format
 	 * that can later be registered into a machine image.
 	 * @param virtualMachineId the virtual machine to be bundled
 	 * @param format the format in which the VM should be bundled
 	 * @param bucket the bucket to which the VM should be bundled
 	 * @param name the name of the object to be created or the prefix for the name
 	 * @param trackingTask the task against which progress for bundling will be tracked
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public void bundleVirtualMachineAsync(String virtualMachineId, MachineImageFormat format, String bucket, String name, AsynchronousTask<String> trackingTask) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot bundle images to cloud storage");
 	}
 
 	/**
 	 * Captures a virtual machine as a machine image. If the underlying cloud requires the virtual machine to change state
 	 * (a common example is that the VM must be {@link VmState#STOPPED}), then this method will make sure the VM is in
 	 * that state. This method blocks until the cloud API has provided a reference to the machine image, regardless of
 	 * what state it is in.
 	 * @param options the options used in capturing the virtual machine
 	 * @return a newly created machine image
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support custom image creation
 	 */
 	@Override
 	public MachineImage captureImage(ImageCreateOptions options) throws CloudException, InternalException {
 		ProviderContext ctx = provider.getContext();
 
 		if( ctx == null ) {
 			throw new CloudException("No context was set for this request");
 		}
 		return captureImage(ctx, options, null);
 	}
 
 	private @Nonnull MachineImage captureImage(@Nonnull ProviderContext ctx, @Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
 		APITrace.begin(provider, "captureImage");
 		try {
 			if( task != null ) {
 				task.setStartTime(System.currentTimeMillis());
 			}
 			VirtualMachine vm = null;
 			String vmId = options.getVirtualMachineId();
 			String name = options.getName();
 
 			long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 30L);
 
 			while( timeout > System.currentTimeMillis() ) {
 				try {
 					vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
 					if( vm == null ) {
 						break;
 					}
 					if( VmState.STOPPED.equals(vm.getCurrentState()) ) {
 						break;
 					}
 					else if ( VmState.RUNNING.equals(vm.getCurrentState()) ) {
 						provider.getComputeServices().getVirtualMachineSupport().stop(vm.getProviderVirtualMachineId());
 					}
 				}
 				catch( Throwable error ) {
 					logger.warn(error.getMessage());
 				}
 				try { Thread.sleep(15000L); }
 				catch( InterruptedException ignore ) { }
 			}
 			if( vm == null ) {
 				throw new CloudException("No such virtual machine: " + vmId);
 			}
 
 			String url = "/" + VMSupport.VIRTUAL_MACHINES + "/" + vmId + "/" + Terremark.ACTION + "/export";
 			String body = "";
 			MachineImage image = null;
 
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder;
 			try {
 				docBuilder = docFactory.newDocumentBuilder();
 				Document doc = docBuilder.newDocument();
 
 				Element rootElement = doc.createElement("ExportVirtualMachineRequest");
 
 				Element catalogNameElement = doc.createElement("CatalogName");
 				catalogNameElement.appendChild(doc.createTextNode(name));
 				rootElement.appendChild(catalogNameElement);			
 
 				doc.appendChild(rootElement);
 
 				StringWriter stw = new StringWriter(); 
 				Transformer serializer = TransformerFactory.newInstance().newTransformer(); 
 				serializer.transform(new DOMSource(doc), new StreamResult(stw)); 
 				body = stw.toString();
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 			} catch (TransformerException e) {
 				e.printStackTrace();
 			}
 
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.POST, url, null, body);
 			Document doc;
 			try {
 				doc = method.invoke();
 			}
 			catch( CloudException e ) {
 				logger.error(e.getMessage());
 				throw new CloudException(e);
 			}
 			String catalogEntryId = Terremark.hrefToId(doc.getElementsByTagName(CATALOG_ENTRY_TAG).item(0).getAttributes().getNamedItem(Terremark.HREF).getNodeValue());
 			String taskHref = Terremark.getTaskHref(doc, CREATE_CATALOG_OPERATION);
 			provider.waitForTask(taskHref, DEFAULT_SLEEP, DEFAULT_TIMEOUT);
 			String imageId = catalogEntryId + "::" + ImageType.CATALOG_ENTRY.name();
 			image = getImage(imageId);
 			if (image == null) {
 				throw new CloudException("No image exists for " + imageId + " as created during the capture process");
 			}
 			return image;
 
 		}
 		finally {
 			APITrace.end();
 		}
 	}
 
 
 	/**
 	 * Executes the process of {@link #captureImage(ImageCreateOptions)} as an asynchronous process tracked using an
 	 * asynchronous task object. This method is expected to return immediately and provide feedback to a client on the
 	 * progress of the machine image capture process.
 	 * @param options the options to be used in capturing the virtual machine
 	 * @param taskTracker the asynchronous task for tracking the progress of this operation
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support custom image creation
 	 */
 	@Override
 	public void captureImageAsync(final ImageCreateOptions options, final AsynchronousTask<MachineImage> taskTracker) throws CloudException, InternalException {
 		final ProviderContext ctx = provider.getContext();
 
 		if( ctx == null ) {
 			throw new CloudException("No context was set for this request");
 		}
 
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					taskTracker.completeWithResult(captureImage(ctx, options, taskTracker));
 				}
 				catch( Throwable t ) {
 					taskTracker.complete(t);
 				}
 				finally {
 					provider.release();
 				}
 			}
 		};
 
 		provider.hold();
 		t.setName("Imaging " + options.getVirtualMachineId() + " as " + options.getName());
 		t.setDaemon(true);
 		t.start();
 	}
 
 	private MachineImage catalogEntryToMachineImage(Node catalogEntryNode) throws CloudException, InternalException {
 		logger.trace("enter - catalogEntryToMachineImage()");
 		MachineImage catalogEntry = new MachineImage(); 
 		NamedNodeMap catalogEntryAtrs = catalogEntryNode.getAttributes();
 		String imageId = Terremark.hrefToId(catalogEntryAtrs.getNamedItem(Terremark.HREF).getNodeValue());
 		String name = catalogEntryAtrs.getNamedItem(Terremark.NAME).getNodeValue();
 		catalogEntry.setProviderMachineImageId(imageId + "::" + ImageType.CATALOG_ENTRY.name());
 		catalogEntry.setName(name);
 		logger.debug("catalogEntryToMachineImage() - Image ID = " + catalogEntry.getProviderMachineImageId() + " Name " + catalogEntry.getName());
 		catalogEntry.setProviderOwnerId(provider.getContext().getAccountNumber());
 		catalogEntry.setProviderRegionId(provider.getContext().getRegionId());
 		catalogEntry.setDescription(name);
 		catalogEntry.setType(MachineImageType.VOLUME);
 		catalogEntry.setImageClass(ImageClass.MACHINE);
 		NodeList ceChildren = catalogEntryNode.getChildNodes();
 		for (int i=0; i<ceChildren.getLength(); i++) {
 			Node ceChild = ceChildren.item(i);
 			if (ceChild.getNodeName().equals("Status")) {
 				String status = ceChild.getTextContent();
 				if (status.equalsIgnoreCase("Completed")) {
 					catalogEntry.setCurrentState(MachineImageState.ACTIVE);
 				}
 				else if (status.equalsIgnoreCase("Failed")) {
 					catalogEntry.setCurrentState(MachineImageState.DELETED);
 				}
 				else {
 					catalogEntry.setCurrentState(MachineImageState.PENDING);
 				}
 			}
 			else if (ceChild.getNodeName().equals("CatalogType")) {
 				String type = ceChild.getTextContent();
 				Tag networkMappingTag = new Tag();
 				networkMappingTag.setKey("CatalogType");
 				networkMappingTag.setValue(type);
 				logger.debug("Adding tag: " + networkMappingTag);
 				catalogEntry.addTag(networkMappingTag);
 			}
 		}
 		logger.debug("catalogEntryToMachineImage() - Getting catalog configuration for " + catalogEntry);
 		String url = "/" + Terremark.ADMIN + "/" + CATALOG + "/" +  imageId + "/" + CONFIGURATION;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 
 			String os = doc.getElementsByTagName("OperatingSystem").item(0).getTextContent();
 			String osDescription = name + " " + os;
 			catalogEntry.setPlatform(Platform.guess(osDescription));
 			logger.debug("catalogEntryToMachineImage(): ID = " + catalogEntry.getProviderMachineImageId() + " OS = " + os + " Platform = " + catalogEntry.getPlatform());
 			if( osDescription == null || osDescription.indexOf("32 bit") != -1 || osDescription.indexOf("32-bit") != -1 ) {
 				catalogEntry.setArchitecture(Architecture.I32);            
 			}
 			else if( osDescription.indexOf("64 bit") != -1 || osDescription.indexOf("64-bit") != -1 ) {
 				catalogEntry.setArchitecture(Architecture.I64);
 			}
 			else {
 				catalogEntry.setArchitecture(Architecture.I64);
 			}
 			NodeList networkMappingElements = doc.getElementsByTagName(NETWORK_MAPPING_TAG);
 			Tag networkMappingCountTag = new Tag();
 			networkMappingCountTag.setKey("NetworkMappingCount");
 			networkMappingCountTag.setValue(String.valueOf(networkMappingElements.getLength()));
 			logger.debug("Adding tag: " + networkMappingCountTag);
 			catalogEntry.addTag(networkMappingCountTag);
 			for (int i=0; i<networkMappingElements.getLength(); i++) {
 				String networkMappingName = networkMappingElements.item(i).getFirstChild().getTextContent();
 				Tag networkMappingTag = new Tag();
 				networkMappingTag.setKey(NETWORK_MAPPING_NAME + "-" + i);
 				networkMappingTag.setValue(networkMappingName);
 				logger.debug("Adding tag: " + networkMappingTag);
 				catalogEntry.addTag(networkMappingTag);
 			}
 			catalogEntry.setSoftware("");
 		}
 		logger.trace("exit - catalogEntryToMachineImage()");
 		return catalogEntry;
 	}
 	
 	private ResourceStatus catalogEntryToMachineImageStatus(Node catalogEntryNode) throws CloudException, InternalException {
 		logger.trace("enter - catalogEntryToMachineImageStatus()");
 		String resourceId;
 		MachineImageState resourceState = MachineImageState.PENDING;
 		NamedNodeMap catalogEntryAtrs = catalogEntryNode.getAttributes();
 		String imageId = Terremark.hrefToId(catalogEntryAtrs.getNamedItem(Terremark.HREF).getNodeValue());
 		resourceId = imageId + "::" + ImageType.CATALOG_ENTRY.name();
 
 		NodeList ceChildren = catalogEntryNode.getChildNodes();
 		for (int i=0; i<ceChildren.getLength(); i++) {
 			Node ceChild = ceChildren.item(i);
 			if (ceChild.getNodeName().equals("Status")) {
 				String status = ceChild.getTextContent();
 				if (status.equalsIgnoreCase("Completed")) {
 					resourceState = MachineImageState.ACTIVE;
 				}
 				else if (status.equalsIgnoreCase("Failed")) {
 					resourceState = MachineImageState.DELETED;
 				}
 				else {
 					resourceState = MachineImageState.PENDING;
 				}
 				break;
 			}
 		}
 		logger.trace("exit - catalogEntryToMachineImageStatus()");
 		return new ResourceStatus(resourceId, resourceState);
 	}
 
 	/**
 	 * Provides access to the current state of the specified image.
 	 * @param providerImageId the cloud provider ID uniquely identifying the desired image
 	 * @return the image matching the desired ID if it exists
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public @Nullable MachineImage getImage(String providerImageId) throws CloudException, InternalException {
 		if (providerImageId == null){
 			logger.debug("getMachineImage(): The image id is null");
 			return null;
 		}
 		MachineImage template = null;
 		String imageId = null;
 		String dataCenterId = null;
 		String imageType = null;
 
 		if (providerImageId.contains(":")){
 			String[] imageIds = providerImageId.split(":");
 			imageId = imageIds[0];
 			dataCenterId = imageIds[1];
 			imageType = imageIds[2];
 		}
 		else {
 			logger.error("getMachineImage(): Invalid machineImageId " + providerImageId + ". Must be of the form: <templateId>:<computePoolId>:<image_type>");
 			return null;
 		}
 		if (imageType.equalsIgnoreCase(ImageType.TEMPLATE.name())) {
 			String url = "/" + TEMPLATES + "/" + imageId + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + dataCenterId;
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = null;
 			try {
 				doc = method.invoke();
 			} catch (TerremarkException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			} catch (CloudException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			} catch (InternalException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			}
 			if (doc != null){
 				template = templateToMachineImage(doc);
 			}
 		}
 		else if (imageType.equalsIgnoreCase(ImageType.CATALOG_ENTRY.name())) {
 			String url = "/" + Terremark.ADMIN + "/" + CATALOG + "/" + imageId;
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = null;
 			try {
 				doc = method.invoke();
 			} catch (TerremarkException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			} catch (CloudException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			} catch (InternalException e) {
 				logger.warn("Failed to get template " + providerImageId);
 			}
 			if (doc != null){
 				Node catalogEntryNode = doc.getElementsByTagName(CATALOG_ENTRY_TAG).item(0);
 				template = catalogEntryToMachineImage(catalogEntryNode);
 			}
 		}
 
 		return template;
 	}
 
 	/**
 	 * Provides access to the current state of the specified image.
 	 * @param providerImageId the cloud provider ID uniquely identifying the desired image
 	 * @return the image matching the desired ID if it exists
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @deprecated Use {@link #getImage(String)}
 	 */
 	@Override
 	public @Nullable MachineImage getMachineImage(@Nonnull String machineImageId) throws CloudException, InternalException {
 		return getImage(machineImageId);
 	}
 
     /**
      * Provides the cloud provider specific term for a custom image of the specified image class.
      * @param locale the locale for which the term should be translated
      * @param cls the image class for the desired type
      * @return the term used by the provider to describe a custom image
      */
 	@Override
 	public String getProviderTermForCustomImage(Locale locale, ImageClass cls) {
 		return "catalog entry";
 	}
 
 	/**
 	 * Provides the cloud provider specific term for a machine image.
 	 * @param locale the locale for which the term should be translated
 	 * @return the term used by the provider to describe a machine image
 	 * @deprecated Use {@link #getProviderTermForImage(Locale, ImageClass)}
 	 */
 	@Override
 	public String getProviderTermForImage(Locale arg0) {
 		return "template/catalog entry";
 	}
 
     /**
      * Provides the cloud provider specific term for a public image of the specified image class.
      * @param cls the image class for the desired type
      * @return the term used by the provider to describe a public image
      */
 	@Override
 	public String getProviderTermForImage(Locale locale, ImageClass cls) {
 		return "template";
 	}
 
 	/**
 	 * Indicates whether or not a public image library of {@link ImageClass#MACHINE} is supported.
 	 * @return true if there is a public library
 	 * @deprecated Use {@link #supportsPublicLibrary(ImageClass)}
 	 */
 	@Override
 	public boolean hasPublicLibrary() {
 		return true;
 	}
 
 	/**
 	 * Identifies if you can bundle a virtual machine to cloud storage from within the VM. If you must bundle local to the
 	 * virtual machine (as with AWS), this should return {@link Requirement#REQUIRED}. If you must be external, this
 	 * should return {@link Requirement#NONE}. If both external and local are supported, this method
 	 * should return {@link Requirement#OPTIONAL}.
 	 * @return how local bundling is supported
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein cloud implementation
 	 */
 	@Override
 	public Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
 		return Requirement.NONE;
 	}
 
 	/**
 	 * Creates a machine image from a virtual machine. This method simply calls {@link #captureImageAsync(ImageCreateOptions, AsynchronousTask)}
 	 * using the task it returns to you.
 	 * @param vmId the unique ID of the virtual machine to be imaged
 	 * @param name the name to give the new image
 	 * @param description the description to give the new image
 	 * @return an asynchronous task for tracking the progress of the imaging
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support custom image creation
 	 * @deprecated Use {@link #captureImage(ImageCreateOptions)} or {@link #captureImageAsync(ImageCreateOptions, AsynchronousTask)}
 	 */
 	@Override
 	public @Nonnull AsynchronousTask<String> imageVirtualMachine(String vmId, String name, String description) throws CloudException, InternalException {
 		VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
 
 		if( vm == null ) {
 			throw new CloudException("No such virtual machine: " + vmId);
 		}
 		final AsynchronousTask<MachineImage> task = new AsynchronousTask<MachineImage>();
 		final AsynchronousTask<String> oldTask = new AsynchronousTask<String>();
 
 		captureImageAsync(ImageCreateOptions.getInstance(vm,  name, description), task);
 
 		Thread t = new Thread() {
 			public void run() {
				final long startTime = System.currentTimeMillis();
				while( (startTime + DEFAULT_TIMEOUT) > System.currentTimeMillis() ) {
 					try { Thread.sleep(15000L); }
 					catch( InterruptedException ignore ) { }
 					oldTask.setPercentComplete(task.getPercentComplete());
 
 					Throwable error = task.getTaskError();
 					MachineImage img = task.getResult();
 
 					if( error != null ) {
 						oldTask.complete(error);
 						return;
 					}
 					else if( img != null ) {
 						oldTask.completeWithResult(img.getProviderMachineImageId());
 						return;
 					}
 					else if( task.isComplete() ) {
 						oldTask.complete(new CloudException("Task completed without info"));
 						return;
 					}
 				}
 				oldTask.complete(new CloudException("Image creation task timed out"));
 			}
 		};
 
 		t.setDaemon(true);
 		t.start();
 
 		return oldTask;
 	}
 
 	/**
 	 * Indicates whether or not the specified image is shared publicly. It should return false when public image sharing
 	 * simply isn't supported by the underlying cloud.
 	 * @param providerImageId the machine image being checked for public status
 	 * @return true if the target machine image is shared with the general public
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein cloud implementation
 	 */
 	@Override
 	public boolean isImageSharedWithPublic(String machineImageId) throws CloudException, InternalException {
 		String imageType = null;
 		boolean isPublic = true;
 		if (machineImageId.contains(":")){
 			String[] imageIds = machineImageId.split(":");
 			imageType = imageIds[2];
 		}
 		else {
 			logger.error("getMachineImage(): Invalid machineImageId " + machineImageId + ". Must be of the form: <templateId>:<computePoolId>:<image_type>");
 		}
 		if (imageType.equalsIgnoreCase(ImageType.CATALOG_ENTRY.name())) {
 			isPublic = false;
 		}
 		return isPublic;
 	}
 
 	/**
 	 * Indicates whether or not this account has access to any image services that might exist in this cloud.
 	 * @return true if the account is subscribed
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein cloud implementation
 	 */
 	@Override
 	public boolean isSubscribed() throws CloudException, InternalException {
 		return true;
 	}
 	
 	private Collection<MachineImage> listCatalogItems() throws CloudException, InternalException {
 		logger.trace("enter - listCatalogItems()");
 		ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 		ProviderContext ctx = provider.getContext();
 		String locationId = provider.getDataCenterServices().getRegionLocation(ctx.getRegionId());
 		String url = "/" + Terremark.ADMIN + "/" + CATALOG + "/" + Terremark.ORGANZIATIONS + "/" + provider.getOrganization().getId() + "/locations/" + locationId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList catalogEntries = doc.getElementsByTagName(CATALOG_ENTRY_TAG);
 			logger.debug("listCatalogItems() - Found " + catalogEntries.getLength() + " catalog entries.");
 			for (int i=0; i<catalogEntries.getLength(); i++) {
 				MachineImage image = null;
 				try {
 					image = catalogEntryToMachineImage(catalogEntries.item(i));
 				}
 				catch (CloudException e) {
 					logger.warn("listCatalogItems(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				catch (InternalException e) {
 					logger.warn("listCatalogItems(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				catch (RuntimeException e) {
 					logger.warn("listCatalogItems(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				if (image != null) {
 					images.add(image);
 				}
 			}
 		}
 		logger.trace("exit - listCatalogItems()");
 		return images;
 	}
 
 	private Collection<ResourceStatus> listCatalogItemsStatus() throws CloudException, InternalException {
 		logger.trace("enter - listCatalogItemsStatus()");
 		ArrayList<ResourceStatus> imagesStatus = new ArrayList<ResourceStatus>();
 		ProviderContext ctx = provider.getContext();
 		String locationId = provider.getDataCenterServices().getRegionLocation(ctx.getRegionId());
 		String url = "/" + Terremark.ADMIN + "/" + CATALOG + "/" + Terremark.ORGANZIATIONS + "/" + provider.getOrganization().getId() + "/locations/" + locationId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 		Document doc = method.invoke();
 		if (doc != null) {
 			NodeList catalogEntries = doc.getElementsByTagName(CATALOG_ENTRY_TAG);
 			logger.debug("listCatalogItemsStatus() - Found " + catalogEntries.getLength() + " catalog entries.");
 			for (int i=0; i<catalogEntries.getLength(); i++) {
 				ResourceStatus imageState = null;
 				try {
 					imageState = catalogEntryToMachineImageStatus(catalogEntries.item(i));
 				}
 				catch (CloudException e) {
 					logger.warn("listCatalogItemsStatus(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				catch (InternalException e) {
 					logger.warn("listCatalogItemsStatus(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				catch (RuntimeException e) {
 					logger.warn("listCatalogItemsStatus(): Skipping catalog item: " + e.getMessage());
 					if (logger.isDebugEnabled()) {
 						e.printStackTrace();
 					}
 				}
 				if (imageState != null) {
 					imagesStatus.add(imageState);
 				}
 			}
 		}
 		logger.trace("exit - listCatalogItemsStatus()");
 		return imagesStatus;
 	}
 
 	/**
 	 * Lists all images in my library. This generally includes all images belonging to me as well any explicitly shared
 	 * with me. In clouds without a public library, it's all images I can see.
 	 * @param cls the class of image being listed
 	 * @return the list of images in my image library of the specified image class
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImage> listImages(ImageClass cls) throws CloudException, InternalException {
 		logger.trace("enter - listImages()");
 		Collection<MachineImage> images = new ArrayList<MachineImage>();
 		images.addAll(listCatalogItems());
 		logger.trace("exit - listImages()");
 		return images;
 	}
 
 	/**
 	 * Lists all images that I can see belonging to the specified account owner. These images may either be public or
 	 * explicitly shared with me.
 	 * @param cls the class of the image being listed
 	 * @param ownedBy the account number of the owner of the image
 	 * @return the list of images I can see belonging to the specified account owner of the specified image class
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImage> listImages(ImageClass cls, String ownedBy) throws CloudException, InternalException {
 		logger.trace("enter - listImages(" + cls + ", " + ownedBy + ")");
 		Iterable<MachineImage> images;
 		if( ownedBy == null || ownedBy.equals(provider.getContext().getAccountNumber()) ) {
 			images = listCatalogItems();
 		}
 		else {
 			images = Collections.emptyList();
 		}
 		logger.trace("exit - listImages(" + cls + ", " + ownedBy + ")");
 		return images;
 	}
 
 	/**
 	 * Lists the current status for all images in my library. The images returned should be the same list provided by
 	 * {@link #listImages(ImageClass)}, except that this method returns a list of {@link ResourceStatus} objects.
 	 * @param cls the image class of the target images
 	 * @return a list of status objects for the images in the library
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<ResourceStatus> listImageStatus(ImageClass cls) throws CloudException, InternalException {
 		logger.trace("enter - listImageStatus()");
 		Collection<ResourceStatus> imagesStatus = new ArrayList<ResourceStatus>();
 		imagesStatus.addAll(listCatalogItemsStatus());
 		logger.trace("exit - listImageStatus()");
 		return imagesStatus;
 	}
 
 	/**
 	 * Lists all images of class {@link ImageClass#MACHINE} in my library.
 	 * @return the list of machine machine images
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @deprecated Use {@link #listImages(ImageClass)} with {@link ImageClass#MACHINE}
 	 */
 	@Override
 	public Iterable<MachineImage> listMachineImages() throws CloudException, InternalException {
 		return listImages(ImageClass.MACHINE);
 	}
 
 	/**
 	 * Lists all images of class {@link ImageClass#MACHINE} that I can see belonging to the specified account owner.
 	 * These images may either be public or explicitly shared with me. You may specify no accountId to indicate you
 	 * are looking for the public library.
 	 * @param accountId the accountId of the owner of the target images or <code>null</code> indicating you want the public library
 	 * @return the list of machine machine images belonging to the specified account owner
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @deprecated Use {@link #listImages(ImageClass,String)}
 	 */
 	@Override
 	public @Nonnull Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
 		return listImages(ImageClass.MACHINE, accountId);
 	}
 
 	/**
 	 * Provides the account numbers for all accounts which which the specified machine image has been shared. This method
 	 * should return an empty list when sharing is unsupported.
 	 * @param providerImageId the unique ID of the image being checked
 	 * @return a list of account numbers with which the target image has been shared
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<String> listShares(String forMachineImageId) throws CloudException, InternalException {
 		return Collections.emptyList();
 	}
 
 	/**
 	 * Lists all machine image formats for any uploading/registering of machine images that might be supported.
 	 * If uploading/registering is not supported, this method will return any empty set.
 	 * @return the list of supported formats you can upload to the cloud
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
 		Collection<MachineImageFormat> formats = new ArrayList<MachineImageFormat>();
 		formats.add(MachineImageFormat.OVF);
 		formats.add(MachineImageFormat.VMDK);
 		return formats;
 	}
 
 	/**
 	 * Lists all machine image formats that can be used in bundling a virtual machine. This should be a sub-set
 	 * of formats specified in {@link #listSupportedFormats()} as you need to be able to register images of this format.
 	 * If bundling is not supported, this method will return an empty list.
 	 * @return the list of supported formats in which you can bundle a virtual machine
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
 		Collection<MachineImageFormat> formats = new ArrayList<MachineImageFormat>();
 		formats.add(MachineImageFormat.OVF);
 		formats.add(MachineImageFormat.VMDK);
 		return formats;
 	}
 
 	/**
 	 * Lists the image classes supported in this cloud.
 	 * @return the supported image classes
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
 		Collection<ImageClass> formats = new ArrayList<ImageClass>();
 		formats.add(ImageClass.MACHINE);
 		return formats;
 	}
 
 	/**
 	 * Enumerates the types of images supported in this cloud.
 	 * @return the list of supported image types
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
 		Collection<MachineImageType> formats = new ArrayList<MachineImageType>();
 		formats.add(MachineImageType.VOLUME);
 		return formats;
 	}
 
 
 	private Collection<MachineImage> listTemplates() throws InternalException, CloudException {
 		logger.trace("enter - listTemplates()");
 		ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 		ArrayList<String> templateIds = new ArrayList<String>();
 		Collection<DataCenter> dcs = provider.getDataCenterServices().listDataCenters(provider.getContext().getRegionId());
 		logger.debug("listTemplates(): dcs size = " + dcs.size());
 		for (DataCenter dc : dcs){
 			String url = "/" + TEMPLATES + "/" + EnvironmentsAndComputePools.COMPUTE_POOLS + "/" + dc.getProviderDataCenterId();
 			TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.GET, url, null, null);
 			Document doc = method.invoke();
 			NodeList templates = doc.getElementsByTagName(TEMPLATE_TAG);
 			logger.debug("listTemplates(): templates length = " + templates.getLength());
 			for (int i = 0; i<templates.getLength(); i++){
 				String templateHref = templates.item(i).getAttributes().getNamedItem(Terremark.HREF).getNodeValue();
 				templateIds.add(Terremark.getTemplateIdFromHref(templateHref));
 			}
 		}
 		logger.debug("listTemplates(): templateIds size = " + templateIds.size());
 		for (String templateId : templateIds){
 			MachineImage image = getImage(templateId);
 			if (image != null){
 				logger.debug("listTemplates(): adding image = " + image);
 				images.add(image);
 			}
 			else {
 				logger.debug("listTemplates(): image is null.");
 			}
 		}
 		logger.trace("exit - listTemplates()");
 		return images;
 	}
 
 	@Override
 	public String[] mapServiceAction(ServiceAction arg0) {
 		return new String[0];
 	}
 
 	/**
 	 * Registers the bundled virtual machine stored in object storage as a machine image in the cloud.
 	 * @param options the options used in registering the machine image
 	 * @return a newly created machine image
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support registering image from object store bundles
 	 */
 	@Override
 	public MachineImage registerImageBundle(ImageCreateOptions options) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Not yet supported.");
 		//TODO: Implement. The implementation should expect a zip file with an OVF and a VMDK file and unzip it and load both parts.
 	}
 
 	/**
 	 * Permanently removes all traces of the target image. This method should remove both the image record in the cloud
 	 * and any cloud storage location in which the image resides for staging.
 	 * @param providerImageId the unique ID of the image to be removed
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public void remove(@Nonnull String machineImageId) throws CloudException, InternalException {
 		if (machineImageId == null){
 			throw new InternalException("getMachineImage(): The image id is null");
 		}
 
 		String imageId = null;
 		String imageType = null;
 
 		if (machineImageId.contains(":")){
 			String[] imageIds = machineImageId.split(":");
 			imageId = imageIds[0];
 			imageType = imageIds[2];
 		}
 		else {
 			throw new InternalException("getMachineImage(): Invalid machineImageId " + machineImageId + ". Must be of the form: <templateId>:<computePoolId>:<imageType>");
 		}
 
 		if (imageType.equals(ImageType.TEMPLATE.name())) {
 			throw new CloudException("Deleting template type images is not supported");
 		}
 
 		String url = "/" + Terremark.ADMIN + "/" + CATALOG + "/" + imageId;
 		TerremarkMethod method = new TerremarkMethod(provider, HttpMethodName.DELETE, url, null, "");
 		method.invoke();
 	}
 
 	  /**
 	   * Permanently removes all traces of the target image. This method should remove both the image record in the cloud
 	   * and any cloud storage location in which the image resides for staging.
 	   * @param providerImageId the unique ID of the image to be removed
 	   * @param checkState if the state of the machine image should be checked first
 	   * @throws CloudException an error occurred with the cloud provider
 	   * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	   */
 	@Override
 	public void remove(String providerImageId, boolean checkState) throws CloudException, InternalException {
 	    if ( checkState ) {
 	        long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 30L);
 
 	        while ( timeout > System.currentTimeMillis() ) {
 	          try {
 	            MachineImage img = getImage( providerImageId );
 
 	            if ( img == null || MachineImageState.DELETED.equals( img.getCurrentState() ) ) {
 	              return;
 	            }
 	            if ( MachineImageState.ACTIVE.equals( img.getCurrentState() ) ) {
 	              break;
 	            }
 	          } catch ( Throwable ignore ) {
 	            // ignore
 	          }
 	          try {
 	            Thread.sleep( 15000L );
 	          } catch ( InterruptedException ignore ) {
 	          }
 	        }
 	      }
 
 	      remove( providerImageId );
 	}
 
 	/**
 	 * Removes ALL specific account shares for the specified image. NOTE THAT THIS METHOD WILL NOT THROW AN EXCEPTION
 	 * WHEN IMAGE SHARING IS NOT SUPPORTED. IT IS A NO-OP IN THAT SCENARIO.
 	 * @param providerImageId the unique ID of the image to be unshared
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public void removeAllImageShares(String providerImageId) throws CloudException, InternalException {
 		// Not supported.
 	}
 
 	/**
 	 * Removes the specified account number from the list of accounts with which this image is shared.
 	 * @param providerImageId the unique ID of the image to be unshared
 	 * @param accountNumber the account number to be removed
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support sharing images with other accounts
 	 */
 	@Override
 	public void removeImageShare(String providerImageId, String accountNumber) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Terremark does not support sharing images with other accounts");
 	}
 
 	/**
 	 * Removes the public share (if shared) for this image. Safe to call even if the image is not shared or sharing
 	 * is not supported.
 	 * @param providerImageId the unique ID of the image to be unshared
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public void removePublicShare(String providerImageId) throws CloudException, InternalException {
 		// Not supported.
 	}
 
 	/**
 	 * Searches images owned by the specified account number (if null, all visible images are searched). It will match against
 	 * the specified parameters. Any null parameter does not constrain the search.
 	 * @param accountNumber the account number to search against or null for searching all visible images
 	 * @param keyword a keyword on which to search
 	 * @param platform the platform to match
 	 * @param architecture the architecture to match
 	 * @param imageClasses the image classes to search for (null or empty list for all)
 	 * @return all matching machine images
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImage> searchImages(String accountNumber, String keyword, Platform platform, Architecture architecture, ImageClass... imageClasses) throws CloudException, InternalException {
 		logger.trace("enter - searchImages(" + accountNumber + ", " + keyword + ", " + platform + ", " + architecture + ")");
 
 		boolean machineClass = false;
 
 		for (ImageClass imageClass : imageClasses) {
 			if (imageClass == ImageClass.MACHINE) {
 				machineClass = true;
 				break;
 			}
 		}
 
 		ArrayList<MachineImage> results = new ArrayList<MachineImage>();
 		
 		if (machineClass) {
 			logger.debug("searchImages(): Calling list templates");
 			Collection<MachineImage> images = new ArrayList<MachineImage>();
 			if (accountNumber == null || accountNumber == provider.getContext().getAccountNumber()) {
 				images.addAll(listCatalogItems());
 			}
 					
 			for( MachineImage image : images ) {
 				if( keyword != null ) {
 					if( !image.getProviderMachineImageId().contains(keyword) && !image.getName().contains(keyword) && !image.getDescription().contains(keyword) ) {
 						continue;
 					}
 				}
 				if( platform != null ) {
 					Platform p = image.getPlatform();
 
 					if( !platform.equals(p) ) {
 						if( platform.isWindows() ) {
 							if( !p.isWindows() ) {
 								continue;
 							}
 						}
 						else if( platform.equals(Platform.UNIX) ){
 							if( !p.isUnix() ) {
 								continue;
 							}
 						}
 						else {
 							continue;
 						}
 					}
 				}
 				if (architecture != null) {
 					if (architecture != image.getArchitecture()) {
 						continue;
 					}
 				}
 				results.add(image);
 			}
 		}
 		logger.trace("exit - searchImages()");
 		return results;
 	}
 
 	/**
 	 * Searches all machine images visible, public or otherwise, to this account for ones that match the specified values.
 	 * If a search parameter is null, it doesn't constrain on that parameter.
 	 * @param keyword a keyword on which to search
 	 * @param platform the platform to be matched
 	 * @param architecture the architecture to be matched
 	 * @return any matching machine images (images of class {@link ImageClass#MACHINE})
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @deprecated Use {@link #searchImages(String, String, Platform, Architecture, ImageClass...)} and/or {@link #searchPublicImages(String, Platform, Architecture, ImageClass...)}
 	 */
 	@Override
 	public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
 		Collection<MachineImage> imageResults = new ArrayList<MachineImage>();
 		Iterable<MachineImage> privateImages = searchImages(null, keyword, platform, architecture, ImageClass.MACHINE);
 		Iterator<MachineImage> imageItr = privateImages.iterator();
 		while (imageItr.hasNext()) {
 			imageResults.add(imageItr.next());
 		}
 		Iterable<MachineImage> publicImages = searchPublicImages(keyword, platform, architecture, ImageClass.MACHINE);
 		imageItr = publicImages.iterator();
 		while (imageItr.hasNext()) {
 			imageResults.add(imageItr.next());
 		}
 		return imageResults;
 	}
 
 	/**
 	 * Searches the public machine image library. It will match against the specified parameters. Any null parameter does
 	 * not constrain the search.
 	 * @param keyword a keyword on which to search
 	 * @param platform the platform to match
 	 * @param architecture the architecture to match
 	 * @param imageClasses the image classes to search for (null or empty list for all)
 	 * @return all matching machine images
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 */
 	@Override
 	public Iterable<MachineImage> searchPublicImages(String keyword, Platform platform, Architecture architecture, ImageClass... imageClasses) throws CloudException, InternalException {
 		logger.trace("enter - searchPublicImages(" + keyword + ", " + platform + ", " + architecture + ")");
 
 		boolean machineClass = false;
 
 		if (imageClasses == null || imageClasses.length == 0) {
 			machineClass = true;
 		}
 		else {
 			for (ImageClass imageClass : imageClasses) {
 				if (imageClass.equals(ImageClass.MACHINE)) {
 					machineClass = true;
 					break;
 				}
 			}
 		}
 
 		ArrayList<MachineImage> results = new ArrayList<MachineImage>();
 		
 		if (machineClass) {
 			logger.debug("searchMachineImages(): Calling list templates");
 			Iterable<MachineImage> images = listTemplates();
 			for( MachineImage image : images ) {
 				if( keyword != null ) {
 					if( !image.getProviderMachineImageId().contains(keyword) && !image.getName().contains(keyword) && !image.getDescription().contains(keyword) ) {
 						continue;
 					}
 				}
 				if( platform != null ) {
 					Platform p = image.getPlatform();
 
 					if( !platform.equals(p) ) {
 						if( platform.isWindows() ) {
 							if( !p.isWindows() ) {
 								continue;
 							}
 						}
 						else if( platform.equals(Platform.UNIX) ){
 							if( !p.isUnix() ) {
 								continue;
 							}
 						}
 						else {
 							continue;
 						}
 					}
 				}
 				if (architecture != null) {
 					if (architecture != image.getArchitecture()) {
 						continue;
 					}
 				}
 				logger.debug("searchMachineImages(): Adding image " + image + " to results");
 				results.add(image);
 			}
 		}
 		logger.trace("exit - searchPublicImages()");
 		return results;
 	}
 
 	/**
 	 * Adds or removes sharing for the specified image with the specified account or the public. This method simply delegates to the
 	 * newer {@link #addImageShare(String, String)}, {@link #removeImageShare(String, String)},
 	 * {@link #addPublicShare(String)}, or {@link #removePublicShare(String)} methods.
 	 * @param providerImageId the image to be shared/unshared
 	 * @param withAccountId the account with which the image is to be shared/unshared (null if the operation is for a public share)
 	 * @param allow true if the image is to be shared, false if it is to be unshared
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException a local error occurred in the Dasein Cloud implementation
 	 * @throws OperationNotSupportedException the cloud does not support sharing images with other accounts
 	 * @deprecated Use {@link #addImageShare(String, String)}, {@link #removeImageShare(String, String)}, {@link #addPublicShare(String)}, or {@link #removePublicShare(String)}
 	 */
 	@Override
 	public void shareMachineImage(@Nonnull String machineImageId, @Nonnull String withAccountId, boolean allow) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot share images");
 	}
 
 	/**
 	 * Indicates whether or not the cloud supports the ability to capture custom images.
 	 * @return true if you can capture custom images in this cloud
 	 * @throws CloudException an error occurred with the cloud provider when checking this capability
 	 * @throws InternalException an error occurred within the Dasein cloud implementation while check this capability
 	 * @deprecated Use {@link #supportsImageCapture(MachineImageType)}
 	 */
 	@Override
 	public boolean supportsCustomImages() {
 		return true;
 	}
 
 	/**
 	 * Supports the ability to directly upload an image into the cloud and have it registered as a new image. When
 	 * doing this, you construct your create options using {@link ImageCreateOptions#getInstance(MachineImageFormat, InputStream, Platform, String, String)}.
 	 * @return true if you can do direct uploads into the cloud
 	 * @throws CloudException an error occurred with the cloud provider when checking this capability
 	 * @throws InternalException an error occurred within the Dasein cloud implementation while check this capability
 	 */
 	@Override
 	public boolean supportsDirectImageUpload() throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether capturing a virtual machine as a custom image of type {@link ImageClass#MACHINE} is supported in
 	 * this cloud.
 	 * @param type the type of image you are checking for capture capabilities
 	 * @return true if you can capture custom images in this cloud
 	 * @throws CloudException an error occurred with the cloud provider when checking this capability
 	 * @throws InternalException an error occurred within the Dasein cloud implementation while check this capability
 	 */
 	@Override
 	public boolean supportsImageCapture(MachineImageType type) throws CloudException, InternalException {
 		return true;
 	}
 
 	/**
 	 * Indicates whether or not this cloud supports sharing images with specific accounts.
 	 * @return true if you can share your images with another account
 	 * @throws CloudException an error occurred with the cloud provider when checking this capability
 	 * @throws InternalException an error occurred within the Dasein cloud implementation while check this capability
 	 */
 	@Override
 	public boolean supportsImageSharing() {
 		return false;
 	}
 
 	/**
 	 * Indicates whether or not this cloud supports making images publicly available to all other accounts.
 	 * @return true if you can share your images publicly
 	 * @throws CloudException an error occurred with the cloud provider when checking this capability
 	 * @throws InternalException an error occurred within the Dasein cloud implementation while check this capability
 	 */
 	@Override
 	public boolean supportsImageSharingWithPublic() {
 		return false;
 	}
 
 	/**
 	 * Indicates whether a library of public images of the specified class should be expected. If true,
 	 * {@link #listImages(ImageClass)} should provide a non-empty list of that type.
 	 * @param cls the image class of the images being checked
 	 * @return true if a public image library exists
 	 * @throws CloudException an error occurred with the cloud provider
 	 * @throws InternalException an error occurred within the Dasein cloud implementation
 	 */
 	@Override
 	public boolean supportsPublicLibrary(ImageClass cls) throws CloudException, InternalException {
 		return cls.equals(ImageClass.MACHINE);
 	}
 
 	private MachineImage templateToMachineImage(Document templateDoc) throws CloudException, InternalException {
 		MachineImage template = new MachineImage(); 
 		Node templateNode = templateDoc.getElementsByTagName(TEMPLATE_TAG).item(0);
 		String href = templateNode.getAttributes().getNamedItem(Terremark.HREF).getTextContent();
 		template.setProviderMachineImageId(Terremark.getTemplateIdFromHref(href));
 		logger.debug("toMachineImage(): Image ID = " + template.getProviderMachineImageId());
 		template.setName(templateNode.getAttributes().getNamedItem(Terremark.NAME).getTextContent());
 		logger.debug("toMachineImage(): ID = " + template.getProviderMachineImageId() + " Image Name = " + template.getName());
 		String description = templateDoc.getElementsByTagName("Description").item(0).getTextContent();
 		template.setDescription(description);
 		logger.debug("toMachineImage(): ID = " + template.getProviderMachineImageId() + " Image Description = " + template.getDescription());
 		String osName = templateDoc.getElementsByTagName("OperatingSystem").item(0).getAttributes().getNamedItem(Terremark.NAME).getTextContent();
 		String osDescription = osName + " " + description;
 		template.setPlatform(Platform.guess(osDescription));
 		logger.debug("toMachineImage(): ID = " + template.getProviderMachineImageId() + " OS = " + osName + " Platform = " + template.getPlatform());
 		if( osDescription == null || osDescription.indexOf("32 bit") != -1 || osDescription.indexOf("32-bit") != -1 ) {
 			template.setArchitecture(Architecture.I32);            
 		}
 		else if( osDescription.indexOf("64 bit") != -1 || osDescription.indexOf("64-bit") != -1 ) {
 			template.setArchitecture(Architecture.I64);
 		}
 		else {
 			template.setArchitecture(Architecture.I64);
 		}
 		logger.debug("toVirtualMachine(): ID = " + template.getProviderMachineImageId() + " OS = " + osName + " Architecture = " + template.getArchitecture());
 		template.setProviderOwnerId(provider.getContext().getAccountNumber());
 		logger.debug("toMachineImage(): ID = " + template.getProviderMachineImageId() + " Image Owner = " + template.getProviderOwnerId());
 		template.setProviderRegionId(provider.getContext().getRegionId());
 		logger.debug("toMachineImage(): ID = " + template.getProviderMachineImageId() + " Image Region = " + template.getProviderRegionId());
 
 		template.setCurrentState(MachineImageState.ACTIVE);
 		NodeList softwareNodes = templateDoc.getElementsByTagName("Software");
 		String[] software = new String[softwareNodes.getLength()];
 		for (int i=0; i<softwareNodes.getLength();i++){
 			NodeList softwareChildren = softwareNodes.item(i).getChildNodes();
 			for (int j=0; j<softwareChildren.getLength(); j++){
 				if (softwareChildren.item(j).getNodeName().equals("Description")){
 					software[i] = softwareChildren.item(j).getTextContent();
 				}
 			}
 		}
 		template.setSoftware(Arrays.toString(software));
 		template.setType(MachineImageType.VOLUME);
 		template.setImageClass(ImageClass.MACHINE);
 		return template;
 	}
 
 	/**
 	 * Updates meta-data for a image with the new values. It will not overwrite any value that currently
 	 * exists unless it appears in the tags you submit.
 	 * @param imageId the image to update
 	 * @param tags the meta-data tags to set
 	 * @throws CloudException an error occurred within the cloud provider
 	 * @throws InternalException an error occurred within the Dasein Cloud API implementation
 	 */
 	@Override
 	public void updateTags(String imageId, Tag... tags) throws CloudException, InternalException {
 		throw new OperationNotSupportedException("Cannot set image tags");
 	}
 
 }
