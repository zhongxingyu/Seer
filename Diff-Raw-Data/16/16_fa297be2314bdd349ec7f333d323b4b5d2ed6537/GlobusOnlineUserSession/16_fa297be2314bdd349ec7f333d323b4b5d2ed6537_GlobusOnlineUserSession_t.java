 package nz.org.nesi.goji.control;
 
 import grisu.grin.model.InfoManager;
 import grisu.jcommons.exceptions.CredentialException;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.EndpointHelpers;
 import grisu.model.info.dto.Directory;
 import grisu.model.info.dto.FileSystem;
 import grith.jgrith.cred.Cred;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import nz.org.nesi.goji.GlobusOnlineConstants;
 import nz.org.nesi.goji.exceptions.CommandException;
 import nz.org.nesi.goji.exceptions.FileSystemException;
 import nz.org.nesi.goji.model.Endpoint;
 import nz.org.nesi.goji.model.Transfer;
 
import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class GlobusOnlineUserSession extends GlobusOnlineSession {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(GlobusOnlineUserSession.class.getName());
 
 	public final InformationManager informationManager;
 
 	private final String endpoint_username;
 
 	private final Set<Directory> directories = Sets.newTreeSet();
 
 	private final Map<FileSystem, Set<String>> filesystems = Maps.newTreeMap();
 
 	public GlobusOnlineUserSession(String go_username, char[] certPassphrase,
 			InformationManager im) throws CredentialException {
 		super(go_username, certPassphrase);
 
 		this.informationManager = im;
 
 		this.endpoint_username = go_username;
 
 		init();
 	}
 
 	public GlobusOnlineUserSession(String go_username,
 			String endpoint_username, Cred cred, InformationManager im)
 			throws CredentialException {
 		super(go_username, cred, null);
 
 		this.informationManager = im;
 
 		this.endpoint_username = endpoint_username;
 
 		init();
 	}
 
 	public GlobusOnlineUserSession(String go_username, Cred cred,
 			String go_url, InformationManager im) throws CredentialException {
 
 		super(go_username, cred, go_url);
 
 		this.informationManager = im;
 
 		this.endpoint_username = go_username;
 
 		init();
 
 	}
 
 	public GlobusOnlineUserSession(String go_username, InformationManager im)
 			throws CredentialException {
 		super(go_username);
 
 		this.informationManager = im;
 
 		this.endpoint_username = go_username;
 
 		init();
 	}
 
 	public GlobusOnlineUserSession(String go_username, String pathToCredential,
 			InformationManager im) throws CredentialException {
 		super(go_username, pathToCredential);
 
 		this.informationManager = im;
 
 		this.endpoint_username = go_username;
 
 		init();
 	}
 
 	/**
 	 * Activates the specified endpoint with the provided credential.
 	 * 
 	 * @param ep
 	 *            the endpoint name
 	 * @param cred
 	 *            the credential
 	 * @throws CommandException
 	 *             if the endpoint can't be activated
 	 */
 	@Override
 	public void activateEndpoint(String ep, Cred cred) throws CommandException {
 		activateEndpoint(endpoint_username + "#" + ep, cred);
 	}
 
 	/**
 	 * Activates all endpoints configured in the InfoManger instance for this
 	 * session.
 	 * 
 	 * This one will not re-activate already activated endpoints, and it will
 	 * wait all endpoints to be activated before returning.
 	 * 
 	 * Note, the endpoints can only be activated with the default session
 	 * credential or a voms-enabled credential that is derived from it. The
 	 * voms-specific stuff is done automatically (i.e. the proper voms proxy
 	 * created for every Endpoint).
 	 * 
 	 * @throws CommandException
 	 *             if not all endpoints could be activated
 	 */
 
 	public void activateAllEndpoints() throws CommandException {
 		activateAllEndpoints(false, true, false);
 	}
 
 	public void activateAllEndpoints(boolean forceReactivate, boolean wait,
 			boolean reloadEndpoints) throws CommandException {
 
 		activateEndpoints(endpoint_username, getDirectories(), forceReactivate,
 				wait, reloadEndpoints);
 	}
 
 	public String getEndpointUsername() {
 		return endpoint_username;
 	}
 
 	public void activateEndpointNames(final Collection<String> eps,
 			final boolean forceReactivate, boolean waitToFinish,
 			boolean reloadEndpoints) throws CommandException {
 
 		List<Directory> dirs = Lists.newArrayList();
 
 		for (String ep : eps) {
 			try {
 				dirs.add(getDirectory(ep));
 			} catch (FileSystemException e) {
 				throw new CommandException("Can't get directory for: " + ep);
 			}
 		}
 		activateEndpoints(endpoint_username, dirs, forceReactivate,
 				waitToFinish, reloadEndpoints);
 	}
 
 	/**
 	 * Dectivates all endpoints configured in the InfoManger instance for this
 	 * session.
 	 * 
 	 * Note, the endpoints can only be activated with the default session
 	 * credential or a voms-enabled credential that is derived from it. The
 	 * voms-specific stuff is done automatically (i.e. the proper voms proxy
 	 * created for every Endpoint).
 	 * 
 	 * @param waitToFinish
 	 *            whether to wait for the deactivation to finish
 	 * @throws CommandException
 	 *             if not all endpoints could be activated
 	 */
 	public void deactivateAllEndpoints(boolean waitToFinish)
 			throws CommandException {
 
 		Collection<Directory> dirs = getDirectories();
 
 		Collection<String> temp = Collections2.transform(dirs,
 				new Function<Directory, String>() {
 
 					public String apply(Directory input) {
 						return endpoint_username + "#" + input.getAlias();
 					}
 
 				});
 
 		deactivateEndpoints(temp, waitToFinish);
 
 	}
 
 	@Override
 	public Endpoint getEndpoint(String endpoint) throws CommandException {

		if (StringUtils.isBlank(EndpointHelpers.extractUsername(endpoint))) {
			return super.getEndpoint(endpoint_username + "#" + endpoint);
		} else {
			return super.getEndpoint(endpoint);
		}

 	}
 
 	/**
 	 * Makes sure that one endpoint exists for each of the {@link Directory}s
 	 * that are configured for the set of groups of the user in the
 	 * {@link InfoManager} instance for this session.
 	 * 
 	 * If an endpoint doesn't exist, it is created using either the alias for
 	 * the directory, or, if the directory doesn't have one, an auto-created
 	 * name ({@link EndpointHelpers#translateFromEndpointName(String, String)}.
 	 * 
 	 * @throws CommandException
 	 *             if either the endpoint username for this object is not the
 	 *             same as the globus username for the GO session or at least
 	 *             one endpoint could not be created
 	 */
 	public void createAllEndpoints() throws CommandException {
 
 		Set<Endpoint> allEndpoints = getAllUserEndpoints(endpoint_username,
 				true);
 
 		for (Directory d : getDirectories()) {
 
 			String alias = d.getAlias();
 
 			boolean alreadyExists = false;
 			for (Endpoint ep : allEndpoints) {
 				if (ep.getName().equals(alias)) {
 					alreadyExists = true;
 					break;
 				}
 			}
 
 			if (!alreadyExists) {
 				addEndpoint(d);
 			}
 		}
 	}
 
 	public String ensureGlobusUrl(String url) throws FileSystemException {
 
 		Directory d = getDirectory(url);
 		if (d == null) {
 			throw new FileSystemException(
 					"File can't be mapped to one of the existing filesystems: "
 							+ url);
 		}
 		return endpoint_username + "#" + d.getAlias() + d.getPath()
 				+ Directory.getRelativePath(d, url);
 	}
 
 	public void removeAllEndpoints() throws CommandException {
 		removeAllEndpoints(endpoint_username);
 	}
 
 	public String ensureGsiftpUrl(String url) throws FileSystemException {
 
 		Directory d = getDirectory(url);
 		return d.toUrl() + Directory.getRelativePath(d, url);
 	}
 
 	public Set<Endpoint> filterUnusableEndpoints(Collection<Endpoint> original) {
 
 		Set<Endpoint> result = Sets.newLinkedHashSet();
 
 		for (Endpoint ep : original) {
 			try {
 				getDirectory(ep.getFullName());
 				result.add(ep);
 			} catch (FileSystemException fse) {
 				myLogger.debug("Ignoring: " + ep.getFullName());
 			}
 		}
 
 		return result;
 	}
 
 	public Collection<Directory> getDirectories() {
 
 		return Collections2.filter(directories, new Predicate<Directory>() {
 
 			public boolean apply(Directory d) {
 				String go_endpoint = Directory.getOption(d,
 						GlobusOnlineConstants.DIRECTORY_IS_GLOBUS_ENDPOINT_KEY);
 
 				boolean result = Boolean.parseBoolean(go_endpoint);
 
 				return result;
 			}
 		});
 
 	}
 
 	/**
 	 * Gets all of the endpoints that are owned by the endpoint user specified
 	 * when creating this object.
 	 * 
 	 * Doesn't refresh the endpoint list if it is already loaded.
 	 * 
 	 * @return the endpoints
 	 * @throws CommandException
 	 *             if the endpoints can't be retrieved
 	 */
 	public Set<Endpoint> getAllUserEndpoints() throws CommandException {
 		return getAllUserEndpoints(endpoint_username, false);
 	}
 
 	/**
 	 * Returns the directory that can be accessed with the specified endpoint or
 	 * url.
 	 * 
 	 * Note, this only works for endpoints that are auto-created by Goji, since
 	 * otherwise we can't figure out which VO to use to access the filesystem.
 	 * 
 	 * @param endpointName_or_url
 	 *            the endpoint or a url
 	 * @return the directory or null if no directory can be found
 	 * @throws FileSystemException
 	 */
 	public Directory getDirectory(String endpointName_or_url)
 			throws FileSystemException {
 
 		for (Directory d : getDirectories()) {
 
 			if (endpointName_or_url.startsWith("gsiftp")) {
 				String url = d.toUrl();
 				if (endpointName_or_url.startsWith(url)) {
 					return d;
 				}
 			} else {
 				// String fqan = d.getFqan();
 				// String host = d.getFilesystem().getHost();
 				//
 				// String name = EndpointHelpers.translateIntoEndpointName(host,
 				// fqan);
 				String name = d.getAlias();
 
 				if (endpointName_or_url.startsWith(name)
 						|| endpointName_or_url.startsWith(endpoint_username
 								+ "#" + name)) {
 					return d;
 				}
 			}
 		}
 
 		throw new FileSystemException(
 				"Url can't be mapped to any of the existing filesystems: "
 						+ endpointName_or_url);
 	}
 
 	/**
 	 * Returns all the filesystems a user has access to.
 	 * 
 	 * Keys are all filesystems, values are sets of fqans that can be used to
 	 * access them. This is important because dependent on the fqan that is used
 	 * to access the filesystem, a different user mapping will occur on the
 	 * target site. So in GlobusOnline those filesystems need to be available as
 	 * seperate endpoints and they need to be activated with a different proxy.
 	 * 
 	 * @return the filesystems
 	 */
 	public Map<FileSystem, Set<String>> getFileSystems() {
 		return filesystems;
 	}
 
 	private void init() {
 		// getting VOs of user
 		getFqans();
 
 		// calculate all the directories a user has access to
 		directories.clear();
 		getFileSystems().clear();
 		for (String fqan : getFqans()) {
 			directories.addAll(informationManager.getDirectoriesForVO(fqan));
 		}
 		for (Directory dir : directories) {
 			Set<String> fqans = getFileSystems().get(dir.getFilesystem());
 			if (fqans == null) {
 				fqans = Sets.newTreeSet();
 				getFileSystems().put(dir.getFilesystem(), fqans);
 			}
 			// fqans.add(dir.getFqan());
 
 		}
 
 	}
 
 	@Override
 	public Transfer transfer(String sourceUrl, String targetUrl)
 			throws CommandException {
 		try {
 			sourceUrl = ensureGlobusUrl(sourceUrl);
 			targetUrl = ensureGlobusUrl(targetUrl);
 		} catch (FileSystemException e) {
 			throw new CommandException(e);
 		}
 		return super.transfer(sourceUrl, targetUrl);
 	}
 
 }
