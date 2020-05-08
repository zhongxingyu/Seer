 package edu.teco.dnd.tests;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import edu.teco.dnd.module.config.BlockTypeHolder;
 import edu.teco.dnd.module.config.ConfigReader;
 import edu.teco.dnd.util.NetConnection;
 
 public class TestConfigReader extends ConfigReader {
 
 	private String name;
 	private UUID moduleUuid = UUID.randomUUID();
 	private int maxAppthreads = 0;
 	private boolean allowNIO = true;
 	private InetSocketAddress[] listen;
 	private InetSocketAddress[] announce;
 	private NetConnection[] multicast;
 	private BlockTypeHolder allowedBlocks; // the rootBlock
 
 	private static transient final Logger LOGGER = LogManager.getLogger(TestConfigReader.class);
 
 	private transient Map<String, BlockTypeHolder> blockQuickaccess = new HashMap<String, BlockTypeHolder>();
 
 	public TestConfigReader(String name, UUID moduleUuid, int maxAppthreads, boolean allowNIO,
 			InetSocketAddress[] listen, InetSocketAddress[] announce, NetConnection[] multicast,
 			BlockTypeHolder allowedBlocks) {
 
 		this.name = name;
 		this.moduleUuid = moduleUuid;
 		this.maxAppthreads = maxAppthreads;
 		this.allowNIO = allowNIO;
 		this.listen = listen;
 		this.announce = announce;
 		this.multicast = multicast;
 		this.allowedBlocks = allowedBlocks;
 
 		if (allowedBlocks != null) {
 			fillTransientVariables(blockQuickaccess, allowedBlocks);
 		}
 	}
 
 	/**
 	 * Convenience method to get a TestConfigReader, with some arbitrarily chosen values in it.
 	 * 
 	 * @return the TestConfigReader
 	 * @throws SocketException
 	 */
 	public static TestConfigReader getPredefinedReader() throws SocketException {
 		String name = "ConfReadName";
 		UUID moduleUuid = UUID.fromString("12345678-9abc-def0-1234-56789abcdef0");
 		int maxAppthreads = 0;
 
 		InetSocketAddress[] listen = new InetSocketAddress[2];
 		listen[0] = new InetSocketAddress("localhost", 8888);
 		listen[1] = new InetSocketAddress("127.0.0.1", 4242);
 		InetSocketAddress[] announce = new InetSocketAddress[1];
 		announce[0] = new InetSocketAddress("localhost", 8888);
 		NetConnection[] multicast = new NetConnection[1];
		multicast[0] = new NetConnection(new InetSocketAddress("255.0.0.1", 1212), NetworkInterface.getByIndex(0));
 
 		Set<BlockTypeHolder> secondLevelChild = new HashSet<BlockTypeHolder>();
 		secondLevelChild.add(new BlockTypeHolder("child1TYPE", 2));
 		secondLevelChild.add(new BlockTypeHolder("child2TYPE", 2));
 
 		Set<BlockTypeHolder> firstLevelChild = new HashSet<BlockTypeHolder>();
 		firstLevelChild.add(new BlockTypeHolder("child2TYPE", 1));
 
 		firstLevelChild.add(new BlockTypeHolder(secondLevelChild, 1));
 		BlockTypeHolder allowedBlocks = new BlockTypeHolder(firstLevelChild, 0);
 
 		return new TestConfigReader(name, moduleUuid, maxAppthreads, true, listen, announce, multicast, allowedBlocks);
 	}
 
 	private void fillTransientVariables(Map<String, BlockTypeHolder> blockQuickaccess,
 			final BlockTypeHolder currentBlock) {
 		Set<BlockTypeHolder> children = currentBlock.getChildren();
 		if (children == null) {
 			blockQuickaccess.put(currentBlock.type, currentBlock);
 		} else {
 			for (BlockTypeHolder child : currentBlock.getChildren()) {
 				child.setParent(currentBlock);
 				fillTransientVariables(blockQuickaccess, child);
 			}
 		}
 
 	}
 
 	@Override
 	public void load(String path) throws IOException {
 		throw LOGGER.throwing(new NotImplementedException());
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public UUID getUuid() {
 		return moduleUuid;
 	}
 
 	@Override
 	public int getMaxThreadsPerApp() {
 		return (maxAppthreads > 0) ? maxAppthreads : ConfigReader.DEFAULT_THREADS_PER_APP;
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
 
 	@Override
 	public BlockTypeHolder getBlockRoot() {
 		return allowedBlocks;
 	}
 
 	@Override
 	public Map<String, BlockTypeHolder> getAllowedBlocks() {
 		return blockQuickaccess;
 	}
 
 	@Override
 	public boolean getAllowNIO() {
 		return allowNIO;
 	}
 
 }
