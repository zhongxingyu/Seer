 package edu.unc.lib.staging;
 
 import java.net.URI;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 /**
  * These are staging areas common to a work group. Files staged in these spaces
  * may be accessed from many platforms and clients, making manifests and therefore
  * projects more portable between users and software installs.
  * 
  * @author count0
  */
 public class SharedStagingArea implements StagingArea {
 	private static final Logger log = LoggerFactory.getLogger(SharedStagingArea.class);
 	@JsonIgnore	Stages stages; // injected at runtime
 	@JsonIgnore	URL configURL; // injected at runtime
 	@JsonIgnore URI uRI;
 	@JsonIgnore URI connectedStorageURI; // determined at runtime
 	String name;
 	CleanupPolicy ingestCleanupPolicy;
 	String confirmFile;
 	boolean readOnly = false;
 	
 	@JsonIgnore	URIPattern uriPattern; // determined at runtime
 
 	private static final String disconnectedStatus = Messages
 			.getString("SharedStagingArea.Disconnected"); //$NON-NLS-1$
 	private static final String notAutoconnectedStatus = Messages
 			.getString("SharedStagingArea.NotAutoconnected"); //$NON-NLS-1$
 	private static final String notVerifiedStatus = Messages
 			.getString("SharedStagingArea.Unverified"); //$NON-NLS-1$
 	private static final String failedStatus = Messages
 			.getString("SharedStagingArea.ConnectionFailed"); //$NON-NLS-1$
 	private static final String connectedStatus = Messages
 			.getString("SharedStagingArea.Connected"); //$NON-NLS-1$
 	private static final String connectedVerifiedStatus = Messages
 			.getString("SharedStagingArea.ConnectedVerified"); //$NON-NLS-1$
 
 	@JsonIgnore	private boolean isConnected = false;
 	
 	@JsonIgnore	private String status = disconnectedStatus;
 
 	public URIPattern getUriPattern() {
 		return this.uriPattern;
 	}
 
 	public void setUriPattern(URIPattern p) {
 		this.uriPattern = p;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getKeyFile()
 	 */
 	public String getConfirmFile() {
 		return confirmFile;
 	}
 
 	public void setConfirmFile(String confirmFile) {
 		this.confirmFile = confirmFile;
 	}
 
 	List<URI> mappings = new ArrayList<URI>();
 	
 	@JsonIgnore	URI storageMapping;
 	private LocalResolver resolver;
 
 	public void init() throws StagingException {
 		if (this.uriPattern.isAutoconnected()) {
 			connect();
 		} else {
 			this.status = notAutoconnectedStatus;
 		}
 	}
 
 	public URL getConfigURL() {
 		return configURL;
 	}
 
 	public void setConfigURL(URL configURL) {
 		this.configURL = configURL;
 	}
 	
 	public boolean isReadOnly() {
 		return this.readOnly;
 	}
 	
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 	
 	public void setStages(Stages stages) {
 		this.stages = stages;
 	}
 
 	@Override
 	public URI getConnectedStorageURI() {
 		return this.connectedStorageURI;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getUri()
 	 */
 	@JsonIgnore public URI getURI() {
 		return uRI;
 	}
 
 	public void setUri(URI uri) {
 		this.uRI = uri;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getName()
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getIngestCleanupPolicy()
 	 */
 	public CleanupPolicy getIngestCleanupPolicy() {
 		return ingestCleanupPolicy;
 	}
 
 	public void setIngestCleanupPolicy(CleanupPolicy ingestCleanupPolicy) {
 		this.ingestCleanupPolicy = ingestCleanupPolicy;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getMappings()
 	 */
 	public List<URI> getMappings() {
 		return mappings;
 	}
 
 	public void setMappings(List<URI> mappings) {
 		this.mappings = mappings;
 	}
 
 	@JsonIgnore public boolean isConnected() {
 		return isConnected;
 	}
 
 	protected void connect() {
 		this.status = disconnectedStatus;
 		this.isConnected = false;
 		if (this.storageMapping != null) {
 			if (this.resolver.exists(this.storageMapping)) {
 				this.connectedStorageURI = this.storageMapping;
 				this.isConnected = true;
 				this.status = connectedStatus;
 			} else {
 				this.status = MessageFormat.format(failedStatus,
 						"Custom mapping unreachable (" + this.storageMapping
 								+ ")");
 				return;
 			}
 		}
 		if (!this.isConnected) {
 			if (this.mappings != null && this.mappings.size() > 0) {
 				for (URI mapping : this.mappings) {
 					if (this.resolver.exists(mapping)) {
 						this.connectedStorageURI = mapping;
 						this.isConnected = true;
 						this.status = connectedStatus;
 						break;
 					}
 				}
 				this.status = MessageFormat.format(failedStatus,
 						"Common folder mappings not found.");
 				return;
 			}
 		}
 
 		if (this.uriPattern instanceof RelativeURIPattern) {
 			this.connectedStorageURI = uRI;
 			this.isConnected = true;
 			this.status = connectedStatus;
 		}
 
 		// not a mapped thing
 		if (!this.isConnected && !this.uriPattern.isLocallyMapped()) {
 			URI testURI = uRI;
 			if (this.resolver.exists(testURI)) {
 				// some are shared and local, i.e. iRODS
 				this.connectedStorageURI = this.uriPattern.makeURI(uRI, "");
 				this.isConnected = true;
 				this.status = connectedStatus;
 			}
 		}
 
 		if (!this.isConnected) {
 			this.status = MessageFormat.format(failedStatus, "Not found");
 			return;
 		}
 
 		// Verify if applicable
 		if (this.confirmFile != null) {
 			URI keyFileURI = connectedStorageURI.resolve(this.confirmFile);
 			if (this.resolver.exists(keyFileURI)) {
 				this.status = connectedVerifiedStatus;
 				return;
 			} else {
 				this.isConnected = false;
 				this.status = MessageFormat.format(notVerifiedStatus,
 						connectedStorageURI, keyFileURI.toString());
 			}
 		}
 	}
 
 	public void setResolver(LocalResolver resolver) {
 		this.resolver = resolver;
 	}
 
 	protected void setStorageMapping(URI localURI) {
 		this.storageMapping = localURI;
 		connect();
 	}
 
 	public URI getStorageMapping() {
 		return this.storageMapping;
 	}
 
 	/**
 	 * Parse the manifest file URI and extract the decoded path relative to the staging
 	 * area base URI.
 	 * 
 	 * @param manifestURI
 	 *            a manifest URI referencing a file in this stage
 	 * @return the path
 	 */
 	public String getRelativePath(URI manifestURI) {
 		return this.uriPattern.getRelativePath(this.uRI, manifestURI);
 	}
 
 	public URI getStorageURI(URI manifestURI) throws StagingException {
 		if(!this.isConnected()) throw new StagingException("Stage is not yet connected: "+this.status);
 		String relativePath = this.getRelativePath(manifestURI);
		log.debug("GOT RELATIVE PATH: {}", relativePath);
 		URIPattern storageURIPattern = stages.findURIPattern(this.connectedStorageURI);
 		return storageURIPattern.makeURI(this.connectedStorageURI, relativePath);
 	}
 
 	public boolean isWithinManifestNamespace(URI manifestURI) {
 		return this.uriPattern.isWithin(this.uRI, manifestURI);
 	}
 	
 	public boolean isWithinStorage(URI storageURI) {
 		if(this.uriPattern.isLocallyMapped()) {
 			if(!this.isConnected()) return false;
 			URIPattern p = this.stages.findURIPattern(this.getConnectedStorageURI());
 			return p.isWithin(this.getConnectedStorageURI(), storageURI);
 		} else {
 			return this.uriPattern.isWithin(this.uRI, storageURI);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.unc.lib.staging.StagingArea#getSharedURI(java.lang.String)
 	 */
 	public URI getManifestURI(URI stagedURI) throws StagingException {
 		if(!this.isConnected()) throw new StagingException("Stage is not yet connected: "+this.status);
 		// find the right uri pattern for the storage URI
 		URIPattern p = this.stages.findURIPattern(this.connectedStorageURI);
 		if(p == null) throw new StagingException("Unrecognized URIPattern for URI: "+stagedURI);
 		
 		String relPath = p.getRelativePath(this.connectedStorageURI, stagedURI);
 		return this.uriPattern.makeURI(uRI, relPath);
 	}
 
 	public URI makeURI(String... relativePath) {
 		return this.uriPattern.makeURI(uRI, relativePath);
 	}
 
 	public String getStatus() {
 		return this.status;
 	}
 
 	public URI makeStorageURI(String... pathParts) throws StagingException {
 		if(!this.isConnected()) throw new StagingException("Stage is not yet connected: "+this.status);
 		URIPattern storePattern = stages.findURIPattern(getConnectedStorageURI());
 		URI result = storePattern.makeURI(getConnectedStorageURI(), pathParts);
		log.debug("make storage uri returning {}", result);
 		return result;
 	}
 
 	@Override
 	@JsonIgnore public String getScheme() {
 		return this.uriPattern.getScheme();
 	}
 
 }
