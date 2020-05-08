 package edu.unc.lib.staging;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class Stages {
 	private static final Logger log = LoggerFactory.getLogger(Stages.class);
 	private List<URL> repositoryConfigURLs = new ArrayList<URL>();
 	private Map<URL, Map<URI, SharedStagingArea>> areas = new HashMap<URL, Map<URI, SharedStagingArea>>();
 	private List<URIPattern> uriPatterns;
 	private LocalResolver resolver;
 	private String localConfig;
 	private Map<URI, URI> storageMappings = new HashMap<URI, URI>();
 	public static URL LOCAL_CONFIG_URL = null;
 	// private String hostnameTemplate2 =
 	// "http://hostname/stagingLocations.json";
 	private String hostnameTemplate1 = "https://hostname/static/stages.json";
 
 	// you can listen
 	private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();
 
 	static {
 		try {
 			LOCAL_CONFIG_URL = new URL("http://localhost/stagingLocations.json");
 		} catch (MalformedURLException e) {
 			throw new Error(e);
 		}
 	}
 
 	public Stages(String localConfig, LocalResolver resolver)
 			throws StagingException {
 		this.resolver = resolver;
 		if (resolver.getURLStreamHandlerFactory() != null) {
 			try {
 				URL.setURLStreamHandlerFactory(resolver
 						.getURLStreamHandlerFactory());
 			} catch (SecurityException ignored) {
 			} catch (Error ignored) {
 			}
 		}
 		this.uriPatterns = loadPatterns();
 		this.localConfig = localConfig;
 		if (localConfig != null) {
 			loadLocalConfig();
 		}
 		for (URL configURL : this.repositoryConfigURLs) {
 			try {
 				loadStages(configURL);
 			} catch (Exception e) {
 				log.error("Error loading remote staging config", e);
 			}
 		}
 	}
 
 	public void addRepositoryConfigURL(String remoteConfigURL)
 			throws StagingException {
 		// can be a full URL to JSON or a repository base URL
 		if (remoteConfigURL.startsWith("http")
 				|| remoteConfigURL.startsWith("file:")) {
 			// leave it
 		} else { // assume this is a hostname
 			remoteConfigURL = this.hostnameTemplate1.replace("hostname",
 					remoteConfigURL);
 		}
 		try {
 			URL config = new URL(remoteConfigURL);
 			loadStages(config);
 			this.repositoryConfigURLs.add(config);
 			notifyListeners();
 		} catch (MalformedURLException e) {
 			throw new StagingException("Not a URL", e);
 		}
 	}
 
 	public void removeRepositoryConfigURL(URL repositoryConfigURL) {
 		this.repositoryConfigURLs.remove(repositoryConfigURL);
 		this.areas.remove(repositoryConfigURL);
 		notifyListeners();
 	}
 
 	private static List<URIPattern> loadPatterns() {
 		ArrayList<URIPattern> result = new ArrayList<URIPattern>();
 		result.add(new IrodsURIPattern());
 		result.add(new TagURIPattern());
 		result.add(new RelativeURIPattern());
 		result.add(new FileURIPattern());
 		return result;
 	}
 
 	public Map<URI, SharedStagingArea> getAllAreas() {
 		Map<URI, SharedStagingArea> result = new HashMap<URI, SharedStagingArea>();
 		for (Map<URI, SharedStagingArea> val : this.areas.values()) {
 			result.putAll(val);
 		}
 		return Collections.unmodifiableMap(result);
 	}
 
 	public Map<URI, SharedStagingArea> getAreas(URL repositoryConfigURL) {
 		if(this.areas.get(repositoryConfigURL) != null) {
 			return Collections.unmodifiableMap(this.areas.get(repositoryConfigURL));
 		} else {
 			return Collections.emptyMap();
 		}
 	}
 
 	private void loadLocalConfig() throws StagingException {
 		Map<URI, SharedStagingArea> localAreas = new HashMap<URI, SharedStagingArea>();
 		this.areas.put(LOCAL_CONFIG_URL, localAreas);
 		try {
 			JsonNode rnode;
 			ObjectMapper om = new ObjectMapper();
 			rnode = om.readTree(this.localConfig);
 			if (rnode.has("customMappings")) {
 				@SuppressWarnings("rawtypes")
 				Map mappings = om.treeToValue(rnode.get("customMappings"),
 						Map.class);
 				for (Object key : mappings.keySet()) {
 					URI stageURI = URI.create((String) key);
 					URI mappedURI = URI.create((String) mappings.get(key));
 					this.storageMappings.put(stageURI, mappedURI);
 				}
 			}
 			if (rnode.has("repositoryConfigurations")) {
 				JsonNode rconfigs = rnode.get("repositoryConfigurations");
 				repositoryConfigURLs = new ArrayList<URL>(rconfigs.size());
 				for (int i = 0; i < rconfigs.size(); i++) {
 					URL config = new URL(rconfigs.get(i).asText());
 					repositoryConfigURLs.add(config);
 				}
 			}
 			if (rnode.has("stagingAreas")) {
 				JsonNode node = rnode.get("stagingAreas");
 				for (Iterator<String> areaIter = node.fieldNames(); areaIter
 						.hasNext();) {
 					String uriStr = areaIter.next();
 					JsonNode areaNode = node.get(uriStr);
 					URI uri = URI.create(uriStr);
 					SharedStagingArea stage = parseSharedStagingArea(om, uri,
 							areaNode, LOCAL_CONFIG_URL);
 					if (this.storageMappings.containsKey(uri)) {
 						stage.setStorageMapping(this.storageMappings.get(uri));
 					}
 					localAreas.put(uri, stage);
 				}
 			}
 		} catch (IOException e) {
 			log.error("Cannot read local staging config.", e);
 			throw new StagingException("Cannot read local staging config.", e);
 		}
 	}
 
 	protected URIPattern findURIPattern(URI uri) {
 		for (URIPattern p : uriPatterns) {
 			if (p.matches(uri))
 				return p;
 		}
 		return null;
 	}
 
 	private SharedStagingArea parseSharedStagingArea(ObjectMapper om, URI uri,
 			JsonNode areaNode, URL configURL) throws StagingException {
 		try {
 			SharedStagingArea stage = om.treeToValue(areaNode,
 					SharedStagingArea.class);
 			URIPattern p = findURIPattern(uri);
 			if (p == null)
 				throw new StagingException(
 						"Cannot find a URI pattern for stage: " + uri);
 			stage.setStages(this);
 			stage.setUriPattern(p);
 			stage.setConfigURL(configURL);
 			stage.setUri(uri);
 			stage.setResolver(this.resolver);
 			if (this.storageMappings.containsKey(uri)) {
 				stage.setStorageMapping(this.storageMappings.get(uri));
 			}
 			stage.init();
 			return stage;
 		} catch (JsonProcessingException e) {
 			throw new StagingException("Cannot parse staging area config '"
 					+ uri + "' (" + configURL + ")", e);
 		}
 	}
 
 	public List<URL> getRepositoryConfigURLs() {
 		return Collections.unmodifiableList(this.repositoryConfigURLs);
 	}
 
 	/**
 	 * Export the current configuration of local mappings and locally defined
 	 * stages. This does not include stages defined remotely, but may include
 	 * their mappings.
 	 * 
 	 * "tag:count0.irods.grid,2013:/irodsStaging/":{
 	 * "name":"Personal iRODS Stage", "mappings":[
 	 * "irods:cdr-stage.lib.unc.edu:5555/stagingZone/home/count0" ],
 	 * "ingestCleanupPolicy":"DELETE_INGESTED_FILES" }
 	 * 
 	 * @return
 	 */
 	public String getLocalConfig() {
 		StringBuilder result = new StringBuilder();
 		result.append("{\n");
 		ObjectMapper om = new ObjectMapper();
 		try {
 			// customMappings
 			result.append("\"customMappings\":");
 			result.append(om.writeValueAsString(this.storageMappings)).append(
 					",\n");
 			// repositoryConfigurations
 			result.append("\"repositoryConfigurations\":");
 			result.append(om.writeValueAsString(this.repositoryConfigURLs))
 					.append(",\n");
 			// TODO stagingAreas
 			result.append("\"stagingAreas\":\n");
 			Map<URI, SharedStagingArea> locals = this.areas
 					.get(LOCAL_CONFIG_URL);
 			result.append(om.writeValueAsString(locals));
 		} catch (JsonProcessingException ignored) {
 		}
 		result.append("}");
 		return result.toString();
 	}
 
 	public void setStorageMapping(URI stageURI, URI storageURI)
 			throws StagingException {
 		if (getAllAreas().get(stageURI) != null) {
 			this.storageMappings.put(stageURI, storageURI);
 			getAllAreas().get(stageURI).setStorageMapping(storageURI);
 			notifyListeners();
 		} else {
 			throw new StagingException(
 					"No configuration found for the stage with id: " + stageURI);
 		}
 	}
 
 	private void loadStages(URL configURL) throws StagingException {
 		JsonNode node;
 		ObjectMapper om = new ObjectMapper();
 		try {
 			node = om.readTree(configURL.openStream());
 		} catch (IOException e) {
 			throw new StagingException("Cannot load config from " + configURL,
 					e);
 		}
 		HashMap<URI, SharedStagingArea> stages = new HashMap<URI, SharedStagingArea>();
 		for (Iterator<String> areaIter = node.fieldNames(); areaIter.hasNext();) {
 			String uriStr = areaIter.next();
 			if (!uriStr.endsWith("/"))
 				throw new StagingException(configURL + " contains area "
 						+ uriStr + " which must end in /");
 			JsonNode areaNode = node.get(uriStr);
 			URI uri = URI.create(uriStr);
 			SharedStagingArea stage = this.parseSharedStagingArea(om, uri,
 					areaNode, configURL);
 			if (this.storageMappings.containsKey(uri)) {
 				stage.setStorageMapping(this.storageMappings.get(uri));
 			}
 			stages.put(uri, stage);
 		}
 		areas.put(configURL, stages);
 	}
 
 	/**
 	 * Find the URL that maps to the staged URI.
 	 * 
 	 * @param stagedURI
 	 */
 	public URI getStorageURI(URI manifestURI) throws StagingException {
		log.debug("getting storage URI for {}", manifestURI);
 		URI result = null;
 		List<StagingArea> possible = new ArrayList<StagingArea>();
 		// look through the staging areas, see if id matches above
 		for (StagingArea area : this.getAllAreas().values()) {
 			if (area.isWithinManifestNamespace(manifestURI)) {
 				possible.add(area);
 			}
 		}
 		if (possible.size() == 0) {
 			throw new StagingException(
 					"No known staging areas match the supplied URI: "
 							+ manifestURI);
 		}
 		// see if any matching areas are connected
 		List<StagingArea> problems = new ArrayList<StagingArea>();
 		for (StagingArea a : possible) {
 			if (!a.isConnected()) {
 				problems.add(a);
 			}
 		}
 		possible.removeAll(problems);
 		if (possible.size() == 0) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(problems.remove(0).getName());
 			for (StagingArea a : problems)
 				sb.append(", ").append(a.getName());
 			throw new StagingException(sb.append(
 					" matched the supplied URI, but are not connected.")
 					.toString());
 		}
 		// convert stagedURI to localURI
 		result = possible.get(0).getStorageURI(manifestURI);
		log.debug("returning storage URI of {}", result);
 		return result;
 	}
 
 	public StagingArea getStage(URI baseURI) {
 		StagingArea result = null;
 		if (this.areas.get(Stages.LOCAL_CONFIG_URL).containsKey(baseURI)) {
 			return this.areas.get(Stages.LOCAL_CONFIG_URL).get(baseURI);
 		}
 		for (Map<URI, SharedStagingArea> val : this.areas.values()) {
 			if (val.containsKey(baseURI)) {
 				result = val.get(baseURI);
 				break;
 			}
 		}
 		return result;
 	}
 
 	public SharedStagingArea findMatchingArea(URI stagedFileOrManifestURI) {
 		SharedStagingArea result = null;
 		for (SharedStagingArea area : getAllAreas().values()) {
 			if (area.isWithinManifestNamespace(stagedFileOrManifestURI)
 					|| area.isWithinStorage(stagedFileOrManifestURI)) {
 				result = area;
 				break;
 			}
 		}
 		return result;
 	}
 
 	public void connect(URI stageid) {
 		SharedStagingArea stage = this.getAllAreas().get(stageid);
 		if (stage != null) {
 			stage.connect();
 			notifyListeners();
 		}
 	}
 
 	protected void notifyListeners() {
 		for (PropertyChangeListener name : listener) {
 			name.propertyChange(new PropertyChangeEvent(this, "everything",
 					areas, areas));
 		}
 	}
 
 	public void addChangeListener(PropertyChangeListener newListener) {
 		listener.add(newListener);
 	}
 
 }
