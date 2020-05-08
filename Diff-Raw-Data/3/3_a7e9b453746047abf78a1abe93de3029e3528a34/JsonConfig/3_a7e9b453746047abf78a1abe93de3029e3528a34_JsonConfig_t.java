 package edu.teco.dnd.module.config;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 
 public class JsonConfig extends ConfigReader {
 	private String name;
 	private UUID uuid;
 	private InetSocketAddress[] listen;
 	private InetSocketAddress[] announce;
 	private NetConnection[] multicast;
 	private BlockType allowedBlocks; // the rootBlock
 
 	private static transient final Logger LOGGER = LogManager.getLogger(JsonConfig.class);
 	private static transient final Gson gson;
 	static {
 		GsonBuilder builder = new GsonBuilder();
 		builder.setPrettyPrinting();
 		builder.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		gson = builder.create();
 	}
 	
 	private transient Map<String, BlockType> blockQuickaccess = new HashMap<String, BlockType>();
 
 	public JsonConfig() {
 	}
 
 	public JsonConfig(String path) throws IOException {
 		this.load(path);
 	}
 
 	public void setTo(JsonConfig oldConf) {
 		if (oldConf == null) {
 			LOGGER.warn("Invalid Config to set(config was null)");
 			throw new NullPointerException();
 		}
 
 		this.name = oldConf.name;
 		this.uuid = oldConf.uuid;
 		this.listen = oldConf.listen;
 		this.announce = oldConf.announce;
 		this.multicast = oldConf.multicast;
 		this.allowedBlocks = oldConf.allowedBlocks;
 	}
 
 	@Override
 	public boolean load(String path) {
 		FileReader reader = null;
 		try {
 			reader = new FileReader(path);
 			setTo((JsonConfig) gson.fromJson(reader, this.getClass()));
 		} catch (FileNotFoundException e) {
 			LOGGER.catching(e);
 			return false;
 		} finally {
 			try {
 				reader.close();
 			} catch (Exception e) {
 			}
 		}
 
 		/*
 		 * //TODO allowedBlocks = new BlockType(0); BlockType b = new
 		 * BlockType(1); b.addChild(new BlockType("child1Type", 2));
 		 * b.addChild(new BlockType("child2Type", 2));
 		 * allowedBlocks.addChild(b); allowedBlocks.addChild(new
 		 * BlockType("child2TYPE", 1));
 		 * 
 		 * 
 		 * 
 		 * 
 		 * //////////////////////////
 		 */
 		this.listen = new InetSocketAddress[3];
 		listen[0] = new InetSocketAddress("localhost", 8888);
 		listen[1] = new InetSocketAddress(4242);
 		try {
 			listen[2] = new InetSocketAddress(InetAddress.getByName("localhost"), 1212);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		this.announce = new InetSocketAddress[2];
 		announce[0] = new InetSocketAddress("localhost", 8888);
 		announce[1] = new InetSocketAddress(4242);
 
 		multicast = new NetConnection[1];
 		try {
			multicast[0] = new NetConnection(new InetSocketAddress("localhost", 1111),
					NetworkInterface.getNetworkInterfaces().nextElement());
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		this.store("newmod.cfg");
 
 		// ////////////////////
 
 		if (allowedBlocks != null) {
 			fillInternalVariables(blockQuickaccess, allowedBlocks);
 		}
 
 		return true;
 	}
 
 	private void fillInternalVariables(Map<String, BlockType> blockQuickaccess, final BlockType currentBlock) {
 		Set<BlockType> children = currentBlock.getChildren();
 		if (children == null) {
 			blockQuickaccess.put(currentBlock.type, currentBlock);
 		} else {
 			for (BlockType child : currentBlock.getChildren()) {
 				child.setParent(currentBlock);
 				fillInternalVariables(blockQuickaccess, child);
 			}
 		}
 	}
 
 	@Override
 	public boolean store(String path) {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(path);
 			gson.toJson(this, writer);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			try {
 				writer.close();
 			} catch (Exception e) {
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public UUID getUuid() {
 		return uuid;
 	}
 
 	@Override
 	public InetSocketAddress[] getListen() {
 		return listen;
 	}
 
 	@Override
 	public InetSocketAddress[] getAnnounce() {
 		return announce;
 	}
 
 	@Override
 	public NetConnection[] getMulticast() {
 		return multicast;
 	}
 
 	public BlockType getBlockRoot() {
 		return allowedBlocks;
 	}
 
 	public Map<String, BlockType> getAllowedBlocks() {
 		return blockQuickaccess;
 	}
 
 }
