 package edu.teco.dnd.module.config;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.util.NetConnection;
 
 public abstract class ConfigReader {
 
 	private static final Logger LOGGER = LogManager.getLogger(ConfigReader.class);
 	public static final int DEFAULT_THREADS_PER_APP = 3;
 
 	/**
 	 * restores the config from a savefile
 	 * 
 	 * @param path
 	 *            path the savefile is stored in (can be e.g. a url if concrete childclass is prepared to handle this.)
 	 * @return false only if action failed. True if unsure/successfull
 	 */
 	public abstract void load(String path) throws IOException;
 
 	/** optional, override if desired */
 	public boolean store(String path) {
 		LOGGER.warn("saving not implemented.");
 		return false;
 	}
 
 	public abstract String getName();
 
 	public abstract UUID getUuid();
 
 	public abstract int getMaxThreadsPerApp();
 
 	public abstract boolean getAllowNIO();
 
 	public abstract InetSocketAddress[] getListen();
 
 	public abstract InetSocketAddress[] getAnnounce();
 
 	public abstract NetConnection[] getMulticast();
 
 	/**
 	 * @return a set of blocks allowed to run and their amounts (encoded in BlockType). Key is the <i>name</i> of the
 	 *         block.
 	 */
 	public abstract Map<String, BlockTypeHolder> getAllowedBlocks();
 
 	/**
 	 * @return a tree structure of blocks and their allowed amount (encoded in blocktype). This returns the root of the
 	 *         tree. See getAllowedBlocks() for a Map version.
 	 */
 	public abstract BlockTypeHolder getBlockRoot();
 
 	/**
 	 * @return a set of blocks allowed to run and their amounts (encoded in BlockType). Key is the <i>internal id
 	 *         number</i> of the block.
 	 */
 	public abstract Map<Integer, BlockTypeHolder> getAllowedBlocksById();
 
 	public abstract int getAnnounceInterval();
 
 }
