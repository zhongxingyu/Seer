 package gov.nih.nci.cagrid.catriptumorregistry.service.globus.resource;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 import org.apache.axis.MessageContext;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.globus.mds.servicegroup.client.ServiceGroupRegistrationParameters;
 import org.globus.wsrf.Constants;
 import org.globus.wsrf.Resource;
 import org.globus.wsrf.ResourceContext;
 import org.globus.wsrf.ResourceContextException;
 import org.globus.wsrf.ResourceProperties;
 import org.globus.wsrf.ResourceProperty;
 import org.globus.wsrf.ResourcePropertySet;
 import org.globus.wsrf.config.ContainerConfig;
 import org.globus.wsrf.container.ServiceHost;
 import org.globus.wsrf.impl.SimpleResourceProperty;
 import org.globus.wsrf.impl.SimpleResourcePropertySet;
 import org.globus.wsrf.impl.servicegroup.client.ServiceGroupRegistrationClient;
 import org.globus.wsrf.utils.AddressingUtils;
 
 import commonj.timers.Timer;
 
 import gov.nih.nci.cagrid.common.Utils;
 
 public class BaseResource implements Resource, ResourceProperties {
 
 	static final Log logger = LogFactory.getLog(BaseResource.class);
 
 	/** Stores the ResourceProperties of this service */
 	private ResourcePropertySet propSet;
 	
 	//this can be used to cancel the registration renewal
 	private Timer registrationTimer;
 
 	private ResourceConfiguration configuration;
 	
 	private URL baseURL;
 
 	//Define the metadata resource properties
 	private ResourceProperty serviceMetadataRP;
 	private gov.nih.nci.cagrid.metadata.ServiceMetadata serviceMetadataMD;
 private ResourceProperty domainModelRP;
 	private gov.nih.nci.cagrid.metadata.dataservice.DomainModel domainModelMD;
 	
 
 
 
 	// initializes the resource
 	public void initialize() throws Exception {
 		// create the resource property set
 		this.propSet = new SimpleResourcePropertySet(ResourceConstants.RESOURCE_PROPERY_SET);
 
 		// this loads the metadata from XML files
 		populateResourceProperty();
 		
 		// now add the metadata as resource properties		//init the rp
 		this.serviceMetadataRP = new SimpleResourceProperty(ResourceConstants.SERVICEMETADATA_MD_RP);
 		//add the value to the rp
 		this.serviceMetadataRP.add(this.serviceMetadataMD);
 		//add the rp to the prop set
 		this.propSet.add(this.serviceMetadataRP);
 		//init the rp
 		this.domainModelRP = new SimpleResourceProperty(ResourceConstants.DOMAINMODEL_MD_RP);
 		//add the value to the rp
 		this.domainModelRP.add(this.domainModelMD);
 		//add the rp to the prop set
 		this.propSet.add(this.domainModelRP);
 	
 
 
 		// register the service to the index sevice
 		refreshRegistration(true);
 
 	}
 
 
 	/**
 	 * This checks the configuration file, and attempts to register to the
 	 * IndexService if shouldPerformRegistration==true. It will first read the
 	 * current container URL, and compare it against the saved value. If the
 	 * value exists, it will only try to reregister if the values are different.
 	 * This exists to handle fixing the registration URL which may be incorrect
 	 * during initialization, then later corrected during invocation. The
 	 * existence of baseURL does not imply successful registration (a non-null
 	 * registrationTimer does). We will only attempt to reregister when the URL
 	 * changes (to prevent attempting registration with each invocation if there
 	 * is a configuration problem).
 	 */
 	public void refreshRegistration(boolean forceRefresh) {
 		if (getConfiguration().shouldPerformRegistration()) {
 
 			URL currentContainerURL = null;
 			try {
 				currentContainerURL = ServiceHost.getBaseURL();
 			} catch (IOException e) {
 				logger.error("Unable to determine container's URL!  Skipping registration.");
 				return;
 			}
 
 			if (this.baseURL != null) {
 				// we've tried to register before (or we are being forced to
 				// retry)
 				// do a string comparison as we don't want to do DNS lookups
 				// for comparison
				//if (forceRefresh || !this.baseURL.toExternalForm().equals(currentContainerURL.toExternalForm())) {
				if (forceRefresh || !this.baseURL.equals(currentContainerURL)) {
 					// we've tried to register before, and we have a different
 					// URL now.. so cancel the old registration (if it exists),
 					// and try to redo it.
 					if (registrationTimer != null) {
 						registrationTimer.cancel();
 					}
 
 					// save the new value
 					this.baseURL = currentContainerURL;
 					logger.info("Refreshing existing registration [container URL=" + this.baseURL + "].");
 				} else {
 					// URLs are the same (and we weren't forced), so don't try
 					// to reregister
 					return;
 				}
 
 			} else {
 				// we've never saved the baseURL (and therefore haven't tried to
 				// register)
 				this.baseURL = currentContainerURL;
 				logger.info("Attempting registration for the first time[container URL=" + this.baseURL + "].");
 			}
 
 			// register with the index service
 			ResourceContext ctx;
 			try {
 				MessageContext msgContext = MessageContext.getCurrentContext();
 				if (msgContext == null) {
 					logger.error("Unable to determine message context!");
 					return;
 				}
 
 				ctx = ResourceContext.getResourceContext(msgContext);
 			} catch (ResourceContextException e) {
 				logger.error("Could not get ResourceContext: " + e);
 				return;
 			}
 
 			EndpointReferenceType epr;
 			try {
 				// since this is a singleton, pretty sure we dont't want to
 				// register the key (allows multiple instances of same service
 				// on successive restarts)
 				// epr = AddressingUtils.createEndpointReference(ctx, key);
 				epr = AddressingUtils.createEndpointReference(ctx, null);
 			} catch (Exception e) {
 				logger.error("Could not form EPR: " + e);
 				return;
 			}
 			try {
 				// This is how registration parameters are set (read from
 				// template)
 				File registrationFile = new File(ContainerConfig.getBaseDirectory() + File.separator
 					+ getConfiguration().getRegistrationTemplateFile());
 
 				if (registrationFile.exists() && registrationFile.canRead()) {
 					logger.debug("Loading registration argumentsrmation from:" + registrationFile);
 
 					ServiceGroupRegistrationParameters params = ServiceGroupRegistrationClient
 						.readParams(registrationFile.getAbsolutePath());
 					// set our service's EPR as the registrant
 					params.setRegistrantEPR(epr);
 
 					ServiceGroupRegistrationClient client = new ServiceGroupRegistrationClient();
 					// apply the registration params to the client
 					registrationTimer = client.register(params);
 				} else {
 					logger.error("Unable to read registration file:" + registrationFile);
 				}
 			} catch (Exception e) {
 				logger.error("Exception when trying to register service (" + epr + "): " + e);
 			}
 		} else {
 			logger.info("Skipping registration.");
 		}
 	}
 
 
 
 	private void populateResourceProperty() {
 	
 		loadServiceMetadataFromFile();
 	
 		loadDomainModelFromFile();
 	
 	}
 
 
 		
 	private void loadServiceMetadataFromFile() {
 		try {
 			File dataFile = new File(ContainerConfig.getBaseDirectory() + File.separator
 					+ getConfiguration().getServiceMetadataFile());
 			this.serviceMetadataMD = (gov.nih.nci.cagrid.metadata.ServiceMetadata) Utils.deserializeDocument(dataFile.getAbsolutePath(),
 				gov.nih.nci.cagrid.metadata.ServiceMetadata.class);
 		} catch (Exception e) {
 			logger.error("ERROR: problem populating metadata from file: " + e.getMessage(), e);
 		}
 	}		
 	
 	
 	private void loadDomainModelFromFile() {
 		try {
 			File dataFile = new File(ContainerConfig.getBaseDirectory() + File.separator
 					+ getConfiguration().getDomainModelFile());
 			this.domainModelMD = (gov.nih.nci.cagrid.metadata.dataservice.DomainModel) Utils.deserializeDocument(dataFile.getAbsolutePath(),
 				gov.nih.nci.cagrid.metadata.dataservice.DomainModel.class);
 		} catch (Exception e) {
 			logger.error("ERROR: problem populating metadata from file: " + e.getMessage(), e);
 		}
 	}		
 	
 		
 
 
 	//Getters/Setters for ResourceProperties
 	
 	
 	protected ResourceProperty getServiceMetadataRP(){
 		return this.serviceMetadataRP;
 	}
 	
 	public gov.nih.nci.cagrid.metadata.ServiceMetadata getServiceMetadataMD(){
 		return this.serviceMetadataMD;
 	}
 	
 	public void setServiceMetadataMD(gov.nih.nci.cagrid.metadata.ServiceMetadata serviceMetadata ){
 		this.serviceMetadataMD=serviceMetadata;
 		getServiceMetadataRP().set(0,serviceMetadata);
 	}
 	
 	
 	protected ResourceProperty getDomainModelRP(){
 		return this.domainModelRP;
 	}
 	
 	public gov.nih.nci.cagrid.metadata.dataservice.DomainModel getDomainModelMD(){
 		return this.domainModelMD;
 	}
 	
 	public void setDomainModelMD(gov.nih.nci.cagrid.metadata.dataservice.DomainModel domainModel ){
 		this.domainModelMD=domainModel;
 		getDomainModelRP().set(0,domainModel);
 	}
 		
 
 	public ResourceConfiguration getConfiguration() {
 		if (this.configuration != null) {
 			return this.configuration;
 		}
 		MessageContext ctx = MessageContext.getCurrentContext();
 
 		String servicePath = ctx.getTargetService();
 
 		String jndiName = Constants.JNDI_SERVICES_BASE_NAME + servicePath + "/configuration";
 		logger.debug("Will read configuration from jndi name: " + jndiName);
 		try {
 			Context initialContext = new InitialContext();
 			this.configuration = (ResourceConfiguration) initialContext.lookup(jndiName);
 		} catch (Exception e) {
 			logger.error("when performing JNDI lookup for " + jndiName + ": " + e);
 		}
 
 		return this.configuration;
 	}
 
 
 	public ResourcePropertySet getResourcePropertySet() {
 		return propSet;
 	}
 
 }
