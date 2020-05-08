 package be.ac.ua.comp.scarletnebula.core;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 
 import be.ac.ua.comp.scarletnebula.gui.GraphPanelCache;
 import be.ac.ua.comp.scarletnebula.misc.SearchHelper;
 import be.ac.ua.comp.scarletnebula.misc.Utils;
 
 import com.jcraft.jsch.UserInfo;
 
 /**
  * Representation of a Server that is somehow connected to a CloudProvider.
  * 
  * @author ives
  * 
  */
 public class Server {
 	private static Log log = LogFactory.getLog(Server.class);
 
 	private VirtualMachine serverImpl;
 	private final Collection<ServerChangedObserver> serverChangedObservers = new ArrayList<ServerChangedObserver>();
 	private final CloudProvider provider;
 	private String friendlyName;
 	private String keypair;
 	private String sshLogin;
 	private String sshPassword;
 	private String vncPassword;
 	private Collection<String> tags;
 	private ServerStatisticsManager serverStatisticsManager;
 	private String statisticsCommand;
 	private final String preferredDatastream;
 	private boolean useSshPassword;
 	private boolean noConnection = false;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param server
 	 *            Dasein server implementation
 	 * @param inputProvider
 	 *            CloudProvider the server is running on.
 	 * @param inputKeypair
 	 *            Keypair the server is using.
 	 * @param inputFriendlyName
 	 *            Friendly name the server is known by.
 	 * @param tags
 	 *            Tags the server is tagged with.
 	 * @param useSshPassword
 	 *            True if server uses a password to connect to SSH
 	 * @param sshLogin
 	 *            Login the server can be ssh'ed to.
 	 * @param sshPassword
 	 *            Password the server can be reached at.
 	 * @param vncPassword
 	 *            Password the server can be vnc'ed with.
 	 * @param statisticsCommand
 	 *            Statistics command that will be executed to retrieve
 	 *            statistics.
 	 * @param preferredDatastream
 	 *            Preferred datastream to display in baregraph.
 	 */
 	public Server(final VirtualMachine server,
 			final CloudProvider inputProvider, final String inputKeypair,
 			final String inputFriendlyName, final Collection<String> tags,
 			final boolean useSshPassword, final String sshLogin,
 			final String sshPassword, final String vncPassword,
 			final String statisticsCommand, final String preferredDatastream) {
 		provider = inputProvider;
 		keypair = inputKeypair;
 		serverImpl = server;
 		this.useSshPassword = useSshPassword;
 		this.sshLogin = (sshLogin != null ? sshLogin : "");
 		this.sshPassword = (sshPassword != null ? sshPassword : "");
 		this.vncPassword = (vncPassword != null ? vncPassword : "");
 		this.statisticsCommand = statisticsCommand;
 		this.preferredDatastream = preferredDatastream;
 		this.tags = tags;
 		setFriendlyName(inputFriendlyName);
 	}
 
 	/**
 	 * @return A ServerStatisticsManager or null if one cannot be created.
 	 */
 	public ServerStatisticsManager getServerStatistics() {
 		if (sshWillFail() || noConnection || getStatisticsCommand() == null
 				|| getStatisticsCommand().isEmpty()) {
 			// Do nothing -- return null
 			serverStatisticsManager = null;
 		} else if (serverStatisticsManager == null) {
 			serverStatisticsManager = getNewServerStatistics(true);
 		}
 		return serverStatisticsManager;
 	}
 
 	private ServerStatisticsManager getNewServerStatistics(final boolean retry) {
 		serverStatisticsManager = new ServerStatisticsManager(this);
 		serverStatisticsManager
 				.addNoStatisticsListener(new ServerStatisticsManager.NoStatisticsListener() {
 
 					@Override
 					public void connectionFailed(
 							final ServerStatisticsManager manager) {
 						log.info("Being notified of server statistics failure.");
 						noConnection = true;
 						serverChanged();
 
 						if (!retry)
 							return;
 
 						log.info("Starting timer that will retry statistics in 120 sec");
 						final java.util.Timer twoMin = new java.util.Timer();
 						twoMin.schedule(new java.util.TimerTask() {
 							@Override
 							public void run() {
 								if (getServerStatistics() != null
 										&& getServerStatistics()
 												.getAvailableDatastreams()
 												.size() > 0)
 									return;
 
 								log.info("Retrying statistics (after 120 sec)");
 								noConnection = false;
 								serverStatisticsManager = getNewServerStatistics(false);
 								serverChanged();
 								cancel();
 							}
 						}, (120 * 1000));
 
 						log.info("Starting timer that will retry statistics in 30 sec");
 						final java.util.Timer thirtySecs = new java.util.Timer();
 						thirtySecs.schedule(new java.util.TimerTask() {
 							@Override
 							public void run() {
 								log.info("Retrying statistics (after 30 sec)");
 								noConnection = false;
 								serverStatisticsManager = getNewServerStatistics(false);
 								serverChanged();
 								cancel();
 							}
 						}, (30 * 1000));
 
 					}
 				});
 		return serverStatisticsManager;
 	}
 
 	/**
 	 * Does basic sanity checks to see if it's even remotely possible to
 	 * establish an SSH connection.
 	 * 
 	 * @return True if the SSH connection will fail, false if it might succeed.
 	 */
 	public boolean sshWillFail() {
 		return getStatus() != VmState.RUNNING
 				|| sshLogin.isEmpty()
 				|| ((useSshPassword && sshPassword.isEmpty() || (!useSshPassword && keypair
 						.isEmpty())));
 	}
 
 	/**
 	 * @param ui
 	 *            *sigh* General suckage of the SSH package I'm using. Don't
 	 *            even look. It's that bad. I tried others but although this
 	 *            one's code and architecture sucks majorly, it's the only SSH
 	 *            client I could find for java that had at least some terminal
 	 *            emulation support (and worked).
 	 * @return A new CommandConnection to this server
 	 * @throws Exception
 	 * @throws FileNotFoundException
 	 */
 	public CommandConnection newCommandConnection(final UserInfo ui)
 			throws Exception {
 		SSHCommandConnection rv = null;
 		String address;
 
 		if (serverImpl.getPublicDnsAddress() != null) {
 			address = serverImpl.getPublicDnsAddress();
 		} else if (serverImpl.getPublicIpAddresses().length >= 1) {
 			address = serverImpl.getPublicIpAddresses()[0];
 		} else {
 			log.warn("Cannot make SSH connection -- no address to connect to.");
 			return null;
 		}
 
 		if (usesSshPassword()) {
 			rv = SSHCommandConnection.newConnectionWithPassword(address,
 					sshLogin, sshPassword, ui);
 		} else {
 			rv = SSHCommandConnection.newConnectionWithKey(address, sshLogin,
 					KeyManager.getKeyFilename(provider.getName(), keypair), ui);
 		}
 
 		return rv;
 	}
 
 	/**
 	 * Factory method that will returns a server object. If we've seen this
 	 * server before and there's saved data for him, this saved data will be
 	 * loaded.
 	 * 
 	 * @param server
 	 *            Dasein server implementation.
 	 * @param provider
 	 *            The provider this server is running with .
 	 * @return The server.
 	 */
 	static Server load(final VirtualMachine server, final CloudProvider provider) {
 		final String propertiesfilename = getSaveFilename(provider, server);
 		final Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(propertiesfilename));
 		} catch (final Exception e) {
 			log.error("Save file for server " + server + " not found");
 		}
 		final String keypair = props.getProperty("keypair");
 		final String friendlyName = props.getProperty("friendlyName");
 		final boolean useSshPassword = Boolean.valueOf(props.getProperty(
 				"useSshPassword", "false"));
 		final String sshLogin = props.getProperty("sshLogin");
 		final String sshPassword = props.getProperty("sshPassword");
 		final String vncPassword = props.getProperty("vncPassword");
 		final String statisticsCommand = props.getProperty("statisticsCommand");
 		final String tagString = props.getProperty("tags");
 		final String preferredDatastream = props
 				.getProperty("preferredDatastream");
 
 		return new Server(server, // dasein server implementation
 				provider, // cloud provider
 				keypair, // ssh keypair chosen
 				friendlyName, // the servers friendly name
 				Arrays.asList(tagString.split(",")), // tags given to
 														// the server
 				useSshPassword, // true if an ssh password instead of keypair
 								// is used
 				sshLogin, // Login for SSH'ing
 				sshPassword, // Password for ssh'ing (if any)
 				vncPassword, // Password for VNC'ing
 				statisticsCommand, // Command to be executed for statistics
 				preferredDatastream); // Datastream to show in small server
 	}
 
 	/**
 	 * @return True if this server should be connected to with a password, false
 	 *         if it should be connected to with a key.
 	 */
 	public boolean usesSshPassword() {
 		return useSshPassword || keypair == null;
 	}
 
 	/**
 	 * @return A string containing the server's preferred datastream.
 	 */
 	public String getPreferredDatastream() {
 		return preferredDatastream;
 	}
 
 	/**
 	 * Returns the filename (with directory) a new instance with name
 	 * "instanceName" for CloudProvider "provider" should get.
 	 * 
 	 * @param provider
 	 *            CloudProvider this server is with
 	 * @param server
 	 *            Dasein server implementation
 	 * @return Filename for this server.
 	 */
 	static String getSaveFilename(final CloudProvider provider,
 			final VirtualMachine server) {
 		return provider.getSaveFileDir() + server.getProviderVirtualMachineId();
 	}
 
 	/**
 	 * Saves this server to its savefile.
 	 */
 	public void store() {
 		// Write key to file
 		final String dir = provider.getSaveFileDir();
 		final File dirFile = new File(dir);
 
 		// Check if the key dir already exists
 		if (!dirFile.exists()) {
 			// If it does not exist, create the directory
 			if (!dirFile.mkdirs()) {
 				log.fatal("Cannot make server directory!");
 				return;
 			}
 		}
 
 		// Write properties file.
 		try {
 			final Properties properties = new Properties();
 			properties.setProperty("friendlyName", getFriendlyName());
 			properties.setProperty("keypair", keypair);
 			properties.setProperty("providerClassName",
 					provider.getUnderlyingClassname());
 			properties.setProperty("sshLogin", sshLogin);
 			properties.setProperty("statisticsCommand", statisticsCommand);
 			properties.setProperty("sshPassword", sshPassword);
 			properties.setProperty("vncPassword", vncPassword);
 			properties.setProperty("useSshPassword",
 					new Boolean(useSshPassword).toString());
 			properties.setProperty("tags",
 					Utils.implode(new ArrayList<String>(tags), ","));
 			properties.setProperty("preferredDatastream", preferredDatastream);
 
 			final FileOutputStream outputstream = new FileOutputStream(
 					getSaveFilename(provider, serverImpl));
 			properties.store(outputstream, null);
 			outputstream.close();
 		} catch (final Exception e) {
 			log.error("Properties for " + this + " could not be stored.", e);
 		}
 
 	}
 
 	/**
 	 * Returns the server implementation's cloud specific (unfriendly) name.
 	 * 
 	 * @return The server's unfriendly (CloudProvider specific) name.
 	 */
 	public String getUnfriendlyName() {
 		return serverImpl.getProviderVirtualMachineId();
 	}
 
 	/**
 	 * @return The server's architecture.
 	 */
 	public Architecture getArchitecture() {
 		return serverImpl.getArchitecture();
 	}
 
 	/**
 	 * @return The server's platform (OS).
 	 */
 	public Platform getPlatform() {
 		return serverImpl.getPlatform();
 	}
 
 	@Override
 	public String toString() {
 		final String rv = serverImpl.getProviderVirtualMachineId() + " ("
 				+ serverImpl.getCurrentState() + ") @ "
 				+ serverImpl.getPublicDnsAddress();
 		return rv;
 	}
 
 	/**
 	 * @return A public DNS address for this server, null if none is available.
 	 */
 	public String getPublicDnsAddress() {
 		return serverImpl.getPublicDnsAddress();
 	}
 
 	/**
 	 * @return A list of public IP address for this server, empty array if none
 	 *         is available.
 	 */
 	public String[] getPublicIpAddresses() {
 		final String[] addresses = serverImpl.getPublicIpAddresses();
 
 		if (addresses == null) {
 			return new String[0];
 		} else {
 			return addresses;
 		}
 	}
 
 	/**
 	 * @return The statistics command for this server.
 	 */
 	public String getStatisticsCommand() {
 		return statisticsCommand;
 	}
 
 	/**
 	 * Sets the friendly name for this server
 	 * 
 	 * @param friendlyName
 	 */
 	final public void setFriendlyName(final String friendlyName) {
 		this.friendlyName = friendlyName;
 	}
 
 	/**
 	 * @return This server's friendly name
 	 */
 	final public String getFriendlyName() {
 		return friendlyName;
 	}
 
 	/**
 	 * Terminates this server
 	 * 
 	 * @throws InternalException
 	 * @throws CloudException
 	 */
 	public void terminate() throws InternalException, CloudException {
 		provider.terminateServer(getUnfriendlyName());
 	}
 
 	/**
 	 * @return This server's status
 	 */
 	public VmState getStatus() {
 		return serverImpl.getCurrentState();
 	}
 
 	/**
 	 * Pulls current information from the cloud this server is located in. This
 	 * just replaces the the dasein server object stored in this server. I don't
 	 * see any better way to do this in the Dasein API.
 	 * 
 	 * @throws CloudException
 	 * @throws InternalException
 	 * @throws ServerDisappearedException
 	 */
 	public void refresh() throws InternalException, CloudException,
 			ServerDisappearedException {
 		final VirtualMachine refreshedServer = provider
 				.getServerImpl(getUnfriendlyName());
 
 		// If the sever disappeared in the mean while, throw an Exception
 		if (refreshedServer == null) {
 			throw new ServerDisappearedException(this);
 		}
 
 		serverImpl = refreshedServer;
 
 		// if(!serverImpl.equals(refreshedServer))
 		serverChanged();
 	}
 
 	/**
 	 * @return The CloudProvider this server was started on
 	 */
 	public CloudProvider getCloud() {
 		return provider;
 	}
 
 	/**
 	 * @return This server's size string
 	 */
 	public String getSize() {
 		return serverImpl.getProduct().getName();
 	}
 
 	/**
 	 * @return This server's image id
 	 */
 	public String getImage() {
 		return serverImpl.getProviderMachineImageId();
 	}
 
 	/**
 	 * Pauses this server if it can be paused
 	 * 
 	 * @throws InternalException
 	 * @throws CloudException
 	 */
 	public void pause() throws InternalException, CloudException {
 		if (serverImpl.isPausable()) {
 			provider.pause(this);
 		}
 
 		return;
 	}
 
 	/**
 	 * @see CloudProvider
 	 * @throws InternalException
 	 * @throws CloudException
 	 */
 	public void resume() throws InternalException, CloudException {
 		provider.resume(this);
 
 		return;
 	}
 
 	/**
 	 * Reboots this server
 	 * 
 	 * @throws CloudException
 	 * @throws InternalException
 	 */
 	public void reboot() throws CloudException, InternalException {
 		provider.reboot(this);
 	}
 
 	/**
 	 * Add a ServerChangedObserver that will be notified when the server
 	 * changes.
 	 * 
 	 * @param sco
 	 *            The observer that will be notified when the server changes.
 	 */
 	public void addServerChangedObserver(final ServerChangedObserver sco) {
 		serverChangedObservers.add(sco);
 	}
 
 	/**
 	 * Removes an observer from the list of observers
 	 * 
 	 * @param sco
 	 *            The observer that will be deleted.
 	 */
 	public void removeServerChangedObserver(final ServerChangedObserver sco) {
 		serverChangedObservers.remove(sco);
 	}
 
 	/**
 	 * Notify all observers the server has changed.
 	 */
 	public void serverChanged() {
 		for (final ServerChangedObserver obs : serverChangedObservers) {
 			obs.serverChanged(this);
 		}
 	}
 
 	/**
 	 * Unlinks this server. This will remove the save file for this server and
 	 * will remove it from the list of linked servers the cloudprovider
 	 * maintains. This obviously does not affect the server's running state in
 	 * any way.
 	 */
 	public void unlink() {
 		getCloud().unlink(this);
 		stopConnections();
 	}
 
 	/**
 	 * Checks with the CloudManager if a server by this name is linked in *any*
 	 * CloudProvider
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static boolean exists(final String name) {
 		return CloudManager.get().serverExists(name);
 	}
 
 	public Collection<String> getTags() {
 		return tags;
 	}
 
 	public boolean match(final Collection<String> filterTerms) {
 		for (String token : filterTerms) {
 			final boolean negated = token.startsWith("-");
 
 			if (negated) {
 				token = token.substring(1);
 			}
 
 			if (token.length() == 0) {
 				continue;
 			}
 
 			final int colonPosition = token.indexOf(':');
 
 			// Prefix-based search term
 			if (colonPosition > 0) {
 				final String prefix = token.substring(0, colonPosition);
 				final String term = token.substring(colonPosition + 1);
 
 				if ("tag".equals(prefix)) {
 					return SearchHelper.matchTags(term, getTags(), negated);
 				} else if ("name".equals(prefix) || "inname".equals(prefix)) {
 					return SearchHelper.matchName(term, getFriendlyName(),
 							negated);
 				} else if ("size".equals(prefix)) {
 					return SearchHelper.matchSize(term, getSize(), negated);
 				} else if ("status".equals(prefix) || "state".equals(prefix)) {
 					return SearchHelper.matchStatus(term, getStatus(), negated);
 				} else if ("provider".equals(prefix)
 						|| "inprovider".equals(prefix)) {
 					return SearchHelper.matchCloudProvider(term, getCloud()
 							.getName(), negated);
 				} else {
 					return false;
 				}
 			} else {
				return SearchHelper
						.matchName(token, getFriendlyName(), negated)
 						|| SearchHelper.matchTags(token, getTags(), negated)
						|| SearchHelper.matchSize(token, getSize(), negated)
 						|| SearchHelper
 								.matchStatus(token, getStatus(), negated)
 						|| SearchHelper.matchCloudProvider(token, getCloud()
 								.getName(), negated);
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * The method you should call when you want to keep refreshing until Server
 	 * "server" has state "state".
 	 * 
 	 * TODO Keep some kind of a map for each state, which can be checked whem
 	 * manually refreshing. Suppose a server is refreshed and it's state is
 	 * PAUSED. The user can then resume. Later, the server's timer that checks
 	 * server state will fire, and the state will show as RUNNING. The timer
 	 * will keep on firing until the count is high enough and it gives up, which
 	 * sucks. Therefore, when manually refreshing a server S, all timers for
 	 * that server should be checked. If there's a timer waiting for S's current
 	 * state, that timer should be cancelled.
 	 * 
 	 * @param server
 	 * @param state
 	 */
 	public void refreshUntilServerHasState(final VmState state) {
 		refreshUntilServerHasState(state, 1);
 	}
 
 	private void refreshUntilServerHasState(final VmState state,
 			final int attempt) {
 		if (getStatus() == state || attempt > 20) {
 			return;
 		}
 
 		try {
 			refresh();
 		} catch (final ServerDisappearedException e) {
 			getCloud().unlink(this);
 			return;
 		} catch (final Exception e) {
 			log.error("Something happened while refreshing server " + this, e);
 		}
 
 		if (getStatus() == state) {
 			return;
 		}
 
 		// If the server's state still isn't the one we want it to be, try
 		// again, but only after waiting
 		// a logarithmic amount of time.
 		final double wait = 15.0 * (Math.log10(attempt) + 1.0);
 
 		final java.util.Timer timer = new java.util.Timer();
 		timer.schedule(new java.util.TimerTask() {
 			@Override
 			public void run() {
 				refreshUntilServerHasState(state, attempt + 1);
 				log.debug("Refreshing state for server " + getFriendlyName()
 						+ " because timer fired, waiting for state "
 						+ state.toString());
 				cancel();
 			}
 		}, (long) (wait * 1000));
 
 	}
 
 	public void setTags(final Collection<String> newTags) {
 		tags = newTags;
 		serverChanged();
 	}
 
 	public String getKeypair() {
 		return keypair;
 	}
 
 	public void assureKeypairLogin(final String username, final String keyname) {
 		sshLogin = username;
 		keypair = (keyname != null ? keyname : "");
 		useSshPassword = false;
 
 		resetConnections();
 		serverChanged();
 	}
 
 	private void stopConnections() {
 		if (serverStatisticsManager != null) {
 			serverStatisticsManager.stop();
 		}
 		serverStatisticsManager = null;
 	}
 
 	private void resetConnections() {
 		GraphPanelCache.get().clearBareServerCache(this);
 
 		stopConnections();
 		noConnection = false;
 	}
 
 	public void assurePasswordLogin(final String username, final String password) {
 		sshLogin = username;
 		sshPassword = password;
 		useSshPassword = true;
 
 		resetConnections();
 		serverChanged();
 	}
 
 	public void setStatisticsCommand(final String command) {
 		statisticsCommand = command;
 
 		final ServerStatisticsManager manager = getServerStatistics();
 		if (manager != null) {
 			manager.reset();
 		}
 
 		serverChanged();
 	}
 
 	public String getSshUsername() {
 		return sshLogin;
 	}
 
 	public String getSshPassword() {
 		return sshPassword;
 	}
 
 	public String getVNCPassword() {
 		return vncPassword;
 	}
 
 	public void setVNCPassword(final String password) {
 		vncPassword = password;
 	}
 
 	public boolean isPausable() {
 		return serverImpl.isPausable();
 	}
 
 	public boolean isRebootable() {
 		return serverImpl.isRebootable();
 	}
 }
