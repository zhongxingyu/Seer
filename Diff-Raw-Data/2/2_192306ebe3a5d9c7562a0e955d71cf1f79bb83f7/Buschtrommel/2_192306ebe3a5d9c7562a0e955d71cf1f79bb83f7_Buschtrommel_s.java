 package de.tr0llhoehle.buschtrommel;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.logging.ConsoleHandler;
 
 import de.tr0llhoehle.buschtrommel.models.ByeMessage;
 import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
 import de.tr0llhoehle.buschtrommel.models.LocalShare;
 import de.tr0llhoehle.buschtrommel.models.Message;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
 import de.tr0llhoehle.buschtrommel.models.RemoteShare;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.Share;
 import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
 import de.tr0llhoehle.buschtrommel.network.IMessageObserver;
 import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
 import de.tr0llhoehle.buschtrommel.network.NetCache;
 import de.tr0llhoehle.buschtrommel.network.Transfer;
 import de.tr0llhoehle.buschtrommel.network.UDPAdapter;
 import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
 
 /**
  * The buschtrommel-object is the heart of the application. It manages all
  * references to share-/net-/transfer management and is the facede for the GUI
  * for all features.
  * 
  */
 public class Buschtrommel implements IMessageObserver {
 
 	private IGUICallbacks guiCallbacks;
 	private FileTransferAdapter fileTransferAdapter;
 	private UDPAdapter udpAdapter;
 	private NetCache netCache;
 	private LocalShareCache shareCache;
 	private String alias;
 	private long lastDiscoveryMulticast;
 
 	/**
 	 * Creates an instance if buschtrommel
 	 * 
 	 * @param gui
 	 * @param alias
 	 */
 	public Buschtrommel(IGUICallbacks gui, String alias) {
 		LoggerWrapper.LOGGER.addHandler(new ConsoleHandler());
 
 		if (!HashFuncWrapper.check()) {// cancel bootstrap: Hashfunction is not
 										// available!
 			throw new IllegalStateException("Hash function is not available!");
 		}
 		readOrCreateSettings(alias);
 		this.alias = alias;
 		this.guiCallbacks = gui;
 		this.shareCache = new LocalShareCache();
 	}
 
 	private void readOrCreateSettings(String alias) {
 		java.io.File cfgFile = new File("config.xml");
 		if (cfgFile.exists()) {
 			try {
 				Config.readFromFile(cfgFile);
 				if (Config.alias == null)
 					Config.alias = alias;
 				return;
 			} catch (FileNotFoundException e) {
 				LoggerWrapper.logError("Could not read config file: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		Config.alias = alias;
 		Config.defaultTTL = -1;
 		Config.maximumYoResponseTime = 3000;
 		Config.minDiscoveryMulticastIddle = 5000;
 		Config.shareCachePath = "shares.ht";
 		Config.TTLRenewalTimer = 10;
 		Config.useIPv4 = true;
 		Config.useIPv6 = true;
 		Config.FileReannounceGraceTime = 15;
 		try {
 			Config.saveToFile(cfgFile, new Config());
 		} catch (FileNotFoundException e) {
 			LoggerWrapper.logError("Could not write config file");
 		}
 	}
 
 	/**
 	 * Joins the network and sends a HI message
 	 * 
 	 * @throws IOException
 	 */
 	public void start() throws IOException {
		start(UDPAdapter.DEFAULT_PORT, UDPAdapter.DEFAULT_PORT, Config.useIPv4, Config.useIPv6);
 	}
 
 	/**
 	 * Joins the network and sends a HI message
 	 * 
 	 * @param listenUdpPort
 	 *            the UDP port to receive group network messages on
 	 * @param sendUdpPort
 	 *            the UDP port to send group network messages on
 	 * @param useIpv4
 	 *            flag whether to use IPv4 or not (IPv4 or IPv6 or both have to
 	 *            be set)
 	 * @param useIpv6
 	 *            flag whether to use IPv6 or not (IPv4 or IPv6 or both have to
 	 *            be set)
 	 * @throws IOException
 	 *             if anything goes wrong during network connect
 	 */
 	public void start(int listenUdpPort, int sendUdpPort, boolean useIpv4, boolean useIpv6) throws IOException {
 		fileTransferAdapter = new FileTransferAdapter(shareCache);
 		this.udpAdapter = new UDPAdapter(listenUdpPort, sendUdpPort, useIpv4, useIpv6);
 		this.netCache = new NetCache(this.udpAdapter, fileTransferAdapter, this.guiCallbacks);
 		this.udpAdapter.registerObserver(netCache);
 		this.udpAdapter.registerObserver(this);
 		this.fileTransferAdapter.registerObserver(this);
 		this.fileTransferAdapter.registerObserver(netCache);
 		udpAdapter.sendMulticast(new PeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI, alias,
 				fileTransferAdapter.getPort()));
 		lastDiscoveryMulticast = System.currentTimeMillis();
 	}
 
 	/**
 	 * Cancels all active transfers, leaves the network and shuts down.
 	 * 
 	 * @throws IOException
 	 */
 	public void stop() throws IOException {
 		this.sendByeMessage();
 		if (fileTransferAdapter != null)
 			fileTransferAdapter.close();
 		fileTransferAdapter = null;
 
 		if (udpAdapter != null) {
 			this.udpAdapter.closeConnection();
 			this.udpAdapter.removeObserver(netCache);
 			this.udpAdapter.removeObserver(this);
 		}
 		this.udpAdapter = null;
 	}
 
 	/***
 	 * Downloads a file from a specific host
 	 * 
 	 * @param hash
 	 *            hash of the file to download
 	 * @param targetFile
 	 *            target file to save
 	 * @param host
 	 *            the host to download this file from
 	 * @return progress-indicator or null if the hash could not be resolved
 	 */
 	public ITransferProgress DownloadFile(String hash, String targetFile, Host host) {
 		RemoteShare s = netCache.getShare(hash);
 		if (s == null) {
 			LoggerWrapper.logError("Can't start download: The share with the hash " + hash + " is not known");
 			return null;
 		}
 		if (host == null) {
 			LoggerWrapper.logError("Can't start download: The given host is null!");
 			return null;
 		}
 		if (targetFile == null) {
 			LoggerWrapper.logError("Can't start download: The given filepath is null");
 			return null;
 		}
 		Transfer result = (Transfer) fileTransferAdapter.DownloadFile(hash, host, s.getLength(), new java.io.File(
 				targetFile));
 		return result;
 	}
 
 	/**
 	 * Download a file from any available host.
 	 * 
 	 * @param hash
 	 *            hash of the file to download
 	 * @param targetFile
 	 *            target file to save
 	 * @return progress-indicator or null if the hash could not be resolved
 	 */
 	public ITransferProgress DownloadFile(String hash, String targetFile) {
 		RemoteShare s = netCache.getShare(hash);
 		if (s == null)
 			return null;
 		Transfer result = (Transfer) fileTransferAdapter.DownloadFile(hash, s.getHostList(), s.getLength(),
 				new java.io.File(targetFile));
 		return result;
 	}
 
 	/**
 	 * Creates a local share with the default TTL an announces it via multicast.
 	 * 
 	 * @param path
 	 *            path to file to share
 	 * @param dspName
 	 *            display name
 	 * @param meta
 	 *            meta information about the file
 	 * @throws IOException
 	 *             when there was an error, generating the hash
 	 * @throws IllegalArgumentException
 	 *             when the path is not a filepath or non-existent or when the
 	 *             TTL is invalid
 	 */
 	public void AddFileToShare(String path, String dspName, String meta) throws IOException, IllegalArgumentException {
 		AddFileToShare(path, dspName, meta, Config.defaultTTL);
 	}
 
 	/**
 	 * Creates a share and announces it via multicast
 	 * 
 	 * @param path
 	 *            path to the file to share
 	 * @param dspName
 	 *            display name
 	 * @param meta
 	 *            meta information about the file
 	 * @param ttl
 	 *            the time to live in seconds. -1 for infinity
 	 * @throws IllegalArgumentException
 	 *             when the path is not a filepath or non-existent or when the
 	 *             TTL is invalid
 	 */
 	public void AddFileToShare(String path, String dspName, String meta, int ttl) throws IOException,
 			IllegalArgumentException {
 		File file = new File(path);
 		if (!file.exists())
 			throw new IllegalArgumentException("path does not exist!");
 		if (!file.isFile())
 			throw new IllegalArgumentException("path is not a file!");
 		if (ttl < 0 && ttl != Share.TTL_INFINITY)
 			throw new IllegalArgumentException("TTL is invalid");
 
 		String hash = HashFuncWrapper.hash(path);
 		LocalShare share = new LocalShare(hash, file.length(), ttl, dspName, meta, path);
 		shareCache.newShare(share);
 		udpAdapter.sendMulticast(new FileAnnouncementMessage(share));
 	}
 
 	/**
 	 * Removes a file from the shares and announces, that it is not available
 	 * any more via multicast
 	 * 
 	 * @param file
 	 *            the file to remove
 	 */
 	public void RemoveFileFromShare(String hash) {
 		LocalShare localShare = shareCache.get(hash);
 		if (shareCache.remove(hash) && localShare != null) {
 			try {
 				udpAdapter.sendMulticast(new FileAnnouncementMessage(new LocalShare(localShare.getHash(), localShare
 						.getLength(), 0, localShare.getDisplayName(), localShare.getMeta(), localShare.getPath())));
 			} catch (IOException e) {
 				LoggerWrapper.logError("Could not announce file-unavailability: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * All currently incoming top-level transfers (active and inactive)
 	 * 
 	 * @return transfers
 	 */
 	public Hashtable<String, ITransferProgress> getIncomingTransfers() {
 		return fileTransferAdapter.getIncomingTransfers();
 	}
 
 	/**
 	 * All currently outgoing transfers (active and inactive)
 	 * 
 	 * @return transfers
 	 */
 	public ArrayList<ITransferProgress> getOutgoingTransfers() {
 		return fileTransferAdapter.getOutgoingTransfers();
 	}
 
 	/**
 	 * This method removes all outgoing transfers from the list, that are in
 	 * cleaned state.
 	 * 
 	 * Transfers that are no longer in progress can be set cleaned as well, if
 	 * one of the flags is set.
 	 * 
 	 * @param canceled
 	 *            also remove all outgoing transfers that are in canceled state
 	 * @param lostConnection
 	 *            also remove all outgoing transfers that are in lostConnection
 	 *            state
 	 * @param finished
 	 *            also remove all outgoing transfers that are in finished state
 	 */
 	public void cleanOutgoingTransfers(boolean canceled, boolean lostConnection, boolean finished) {
 		for (ITransferProgress t : fileTransferAdapter.getOutgoingTransfers()) {
 			if ((t.getStatus() == TransferStatus.Canceled && canceled)
 					|| (t.getStatus() == TransferStatus.LostConnection && lostConnection)
 					|| ((t.getStatus() == TransferStatus.Finished) && finished)) {
 
 				t.cancel();
 				t.cleanup();
 			}
 		}
 		fileTransferAdapter.removeCleanedOutgoingDownloads();
 	}
 
 	/**
 	 * Cleans and removes an incoming transfer!
 	 * 
 	 * @param hash
 	 *            of the transfer to remove
 	 */
 	public void cleanIncomingTransfer(String hash) {
 		fileTransferAdapter.cleanDownloadedTransfer(hash);
 	}
 
 	/**
 	 * Returns all known shares of other hosts
 	 * 
 	 * @return all known shares
 	 */
 	public Hashtable<String, RemoteShare> getRemoteShares() {
 		return this.netCache.getShares();
 	}
 
 	/**
 	 * Returns all local shares
 	 * 
 	 * @return all local shares
 	 */
 	public Hashtable<String, LocalShare> getLocalShares() {
 		return this.shareCache.getLocalShares();
 	}
 
 	/**
 	 * Returns all currently known hosts
 	 * 
 	 * @return all known hosts
 	 */
 	public Hashtable<InetAddress, Host> getHosts() {
 		return this.netCache.getHosts();
 	}
 
 	/**
 	 * Send a BYE message over the network
 	 */
 	private void sendByeMessage() {
 		try {
 			if (this.udpAdapter != null) {
 				this.udpAdapter.sendMulticast(new ByeMessage());
 			} else {
 				LoggerWrapper.logError("Could not find UDP Adapter");
 			}
 		} catch (IOException e) {
 			LoggerWrapper.logError(e.getMessage());
 		}
 	}
 
 	@Override
 	public void receiveMessage(Message message) { // respond do HI messages
 													// either via unicast or
 													// multicast
 		if (message instanceof PeerDiscoveryMessage) {
 			if (((PeerDiscoveryMessage) message).getDiscoveryMessageType() == DiscoveryMessageType.HI) {
 				if (udpAdapter == null || fileTransferAdapter == null) {
 					LoggerWrapper.logError("Can't respond to peer discovery because adapter is not initialized!");
 					return;
 				}
 
 				PeerDiscoveryMessage rsp = new PeerDiscoveryMessage(DiscoveryMessageType.YO, alias,
 						fileTransferAdapter.getPort());
 				try {
 					Thread.sleep((int) (Math.random() * Config.maximumYoResponseTime));
 					if (System.currentTimeMillis() - lastDiscoveryMulticast > Config.minDiscoveryMulticastIddle) {
 						lastDiscoveryMulticast = System.currentTimeMillis();
 						udpAdapter.sendMulticast(rsp);
 					} else {
 						udpAdapter.sendUnicast(rsp, message.getSource().getAddress());
 					}
 					
 					//autostart filelist download
 					fileTransferAdapter.downloadFilelist(new Host(message.getSource().getAddress(), ((PeerDiscoveryMessage) message).getAlias(), ((PeerDiscoveryMessage) message).getPort()));
 				} catch (IOException e) {
 					LoggerWrapper.logError("Could not response to HI message: " + e.getMessage());
 				} catch (InterruptedException e1) {
 					LoggerWrapper.logError("Error while waiting before sending YO response: " + e1.getMessage());
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the port that buschtrommel listens for incoming file transfer
 	 * requests
 	 * 
 	 * @return the port or -1, if not connected
 	 */
 	public int getTransferPort() {
 		if (fileTransferAdapter == null)
 			return -1;
 		return fileTransferAdapter.getPort();
 	}
 }
