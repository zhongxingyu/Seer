 package edu.teco.dnd.module.config;
 
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 public abstract class ConfigReader {
 
 	private static final Logger LOGGER = LogManager.getLogger(ConfigReader.class);
 
 	/**
 	 * restores the config from a savefile
 	 * 
 	 * @param path
 	 *            path the savefile is stored in (can be e.g. a url if concrete childclass is
 	 *            prepared to handle this.)
 	 * @return false only if action failed. True if unsure/successfull
 	 */
	public abstract boolean load(String path);
 
 	/** optional, override if desired */
 	public boolean store(String path) {
 		LOGGER.warn("saving not implemented.");
 		return false;
 	}
 
 	public abstract String getName();
 
 	public abstract UUID getUuid();
 
 	public abstract InetSocketAddress[] getListen();
 
 	public abstract InetSocketAddress[] getAnnounce();
 
 	public abstract NetConnection[] getMulticast();
 
 	/**
 	 * @return a set of blocks allowed to run and their amounts (encoded in
 	 *         BlockType). Key is the <i>name</i> of the block/(group of blocks)
 	 *         (by definition).
 	 */
 	public abstract Map<String, BlockType> getAllowedBlocks();
 
 	public class NetConnection {
		InetSocketAddress socket = null;
		NetworkInterface interf = null;
 		
 		public NetConnection() {}
 		public NetConnection(InetSocketAddress socket, NetworkInterface interf) {
 			this.socket = socket;
 			this.interf = interf;
 		}
 	}
 
 }
