 package edu.teco.dnd.module.tests;
 
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.Collection;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.ValueDestination;
 import edu.teco.dnd.meeting.BeamerOperatorBlock;
import edu.teco.dnd.module.BlockDescription;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.ModuleApplicationManager;
 import edu.teco.dnd.module.config.ConfigReader;
 import edu.teco.dnd.module.config.tests.TestConfigReader;
 import edu.teco.dnd.module.messages.generalModule.MissingApplicationNak;
 import edu.teco.dnd.module.messages.generalModule.ShutdownModuleAck;
 import edu.teco.dnd.module.messages.generalModule.ShutdownModuleMessage;
 import edu.teco.dnd.module.messages.generalModule.ShutdownModuleNak;
 import edu.teco.dnd.module.messages.infoReq.ApplicationListResponse;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessageAdapter;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationListMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMessage;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationNak;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppAck;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppNak;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockAck;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockNak;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassAck;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassNak;
 import edu.teco.dnd.module.messages.values.BlockFoundResponse;
 import edu.teco.dnd.module.messages.values.ValueAck;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.ValueMessageAdapter;
 import edu.teco.dnd.module.messages.values.ValueNak;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.network.codecs.MessageAdapter;
 import edu.teco.dnd.network.messages.Message;
 import edu.teco.dnd.util.Base64Adapter;
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 import edu.teco.dnd.util.NetConnection;
 import edu.teco.dnd.util.NetConnectionAdapter;
 
 /**
  * Test to check that classes are properly de/encoded with json. Does however not check json Syntax, just whether
  * input== output. All tests are of the form: construct a message, encode it to JSon, decode it again, compare input to
  * output.
  * 
  * @author Marvin Marx
  * 
  */
 public class MessageDeEnCodingTest implements Serializable {
 
 	private static final long serialVersionUID = -6437521431147083820L;
 
 	private static final Logger LOGGER = LogManager.getLogger(MessageDeEnCodingTest.class);
 
 	private static final UUID TEST_MODULE_UUID = UUID.fromString("00000000-9abc-def0-1234-56789abcdef0");
 	private static final UUID TEST_APP_UUID = UUID.fromString("11111111-9abc-def0-1234-56789abcdef0");
 	private static final UUID TEST_FUNBLOCK_UUID = UUID.fromString("99999999-9abc-def0-1234-56789abcdef0");
 
 	private static final Gson GSON;
 	private static final MessageAdapter MSG_ADAPTER = new MessageAdapter();
 
 	static {
 
 		ModuleApplicationManager appMan;
 		try {
 			appMan = new ModuleApplicationManager(TestConfigReader.getPredefinedReader(), null, null) {
 				public ClassLoader getAppClassLoader(UUID appId) {
 					return null;
 				};
 			};
 		} catch (SocketException e) {
 			throw new Error(e);
 		}
 
 		GsonBuilder builder = new GsonBuilder();
 		builder.setPrettyPrinting();
 		builder.registerTypeAdapter(Message.class, MSG_ADAPTER);
 		builder.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		builder.registerTypeAdapter(NetConnection.class, new NetConnectionAdapter());
 		builder.registerTypeAdapter(byte[].class, new Base64Adapter());
 		builder.registerTypeAdapter(ValueMessage.class, new ValueMessageAdapter(appMan));
 		builder.registerTypeAdapter(ModuleInfoMessage.class, new ModuleInfoMessageAdapter());
 		GSON = builder.create();
 	}
 
 	@Test
 	public void applicationListResponseTest() {
 
 		Map<UUID, String> modIds = new TreeMap<UUID, String>();
 		Map<UUID, Collection<UUID>> appBlocksRunning = new TreeMap<UUID, Collection<UUID>>();
		Map<UUID, String> uuidToBlockType = new HashMap<UUID, String>();
		Map<String, String> uBlockIDToBlockName = new HashMap<String, String>();
 		Set<UUID> blockMap = new TreeSet<UUID>();
 		blockMap.add(TEST_FUNBLOCK_UUID);
 		appBlocksRunning.put(TEST_APP_UUID, blockMap);
		uuidToBlockType.put(TEST_FUNBLOCK_UUID, "testOperator");
		uBlockIDToBlockName.put(BlockDescription.getUniqueBlockID(TEST_APP_UUID, TEST_FUNBLOCK_UUID), "block1Name");
 
 		modIds.put(TEST_APP_UUID, "APP_1");
 		MSG_ADAPTER.addMessageType(ApplicationListResponse.class);
		testEnDeCoding(new ApplicationListResponse(TEST_MODULE_UUID, modIds, appBlocksRunning, uuidToBlockType,
				uBlockIDToBlockName));
 	}
 
 	@Test
 	public void requestApplicationListMessageTest() {
 
 		MSG_ADAPTER.addMessageType(RequestApplicationListMessage.class);
 		testEnDeCoding(new RequestApplicationListMessage());
 	}
 
 	@Test
 	public void shutdownModuleMessageTest() {
 		MSG_ADAPTER.addMessageType(ShutdownModuleMessage.class);
 		testEnDeCoding(new ShutdownModuleMessage());
 	}
 
 	@Test
 	public void shutdownModuleNakTest() {
 		MSG_ADAPTER.addMessageType(ShutdownModuleNak.class);
 		testEnDeCoding(new ShutdownModuleNak());
 	}
 
 	@Test
 	public void shutdownModuleAckTest() {
 		MSG_ADAPTER.addMessageType(ShutdownModuleAck.class);
 		testEnDeCoding(new ShutdownModuleAck());
 	}
 
 	@Test
 	public void missingApplicationNakTest() {
 		MSG_ADAPTER.addMessageType(MissingApplicationNak.class);
 		testEnDeCoding(new MissingApplicationNak(TEST_APP_UUID));
 	}
 
 	@Test
 	public void moduleInfoMessageTest() {
 		MSG_ADAPTER.addMessageType(ModuleInfoMessage.class);
 		try {
 			final ConfigReader configReader = TestConfigReader.getPredefinedReader();
 			testEnDeCoding(new ModuleInfoMessage(new Module(configReader.getUuid(), configReader.getName(),
 					configReader.getBlockRoot())));
 		} catch (SocketException e) {
 			e.printStackTrace();
 			throw new Error(e);
 		}
 
 	}
 
 	@Test
 	public void requestModuleInfoMessageTest() {
 
 		MSG_ADAPTER.addMessageType(RequestModuleInfoMessage.class);
 		testEnDeCoding(new RequestModuleInfoMessage());
 	}
 
 	@Test
 	public void joinApplicationMessageTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		MSG_ADAPTER.addMessageType(JoinApplicationMessage.class);
 		testEnDeCoding(jam);
 	}
 
 	@Test
 	public void joinApplicationAckTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		MSG_ADAPTER.addMessageType(JoinApplicationAck.class);
 		testEnDeCoding(new JoinApplicationAck(jam));
 
 	}
 
 	@Test
 	public void joinApplicationNakTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		MSG_ADAPTER.addMessageType(JoinApplicationNak.class);
 		testEnDeCoding(new JoinApplicationNak(jam));
 
 	}
 
 	@Test
 	public void startApplicationMessageTest() {
 
 		MSG_ADAPTER.addMessageType(StartApplicationMessage.class);
 		testEnDeCoding(new StartApplicationMessage(TEST_APP_UUID));
 
 	}
 
 	@Test
 	public void killAppMessageTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 
 		MSG_ADAPTER.addMessageType(KillAppMessage.class);
 		testEnDeCoding(kam);
 	}
 
 	@Test
 	public void killAppAckTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 		MSG_ADAPTER.addMessageType(KillAppAck.class);
 		testEnDeCoding(new KillAppAck(kam));
 
 	}
 
 	@Test
 	public void killAppNakTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 		MSG_ADAPTER.addMessageType(KillAppNak.class);
 		testEnDeCoding(new KillAppNak(kam));
 
 	}
 
 	@Test
 	public void blockAckTest() {
 		MSG_ADAPTER.addMessageType(BlockAck.class);
 		testEnDeCoding(new BlockAck());
 
 	}
 
 	@Test
 	public void blockMessageTest() {
 
 		Map<String, String> options = new TreeMap<String, String>();
 		options.put("Option_key", "option_val");
 		Set<ValueDestination> valueDest = new HashSet<ValueDestination>();
 		valueDest.add(new ValueDestination(TEST_FUNBLOCK_UUID, "input_Name"));
 		Map<String, Collection<ValueDestination>> outputs = new TreeMap<String, Collection<ValueDestination>>();
 		outputs.put("Outp_key", valueDest);
 
 		MSG_ADAPTER.addMessageType(BlockMessage.class);
		testEnDeCoding(new BlockMessage(TEST_APP_UUID, "BeamerActor", "Block2Name", TEST_FUNBLOCK_UUID, options,
				outputs, 3));
 	}
 
 	@Test
 	public void blockNakTest() {
 
 		MSG_ADAPTER.addMessageType(BlockNak.class);
 		testEnDeCoding(new BlockNak());
 	}
 
 	@Test
 	public void loadClassAckTest() {
 
 		MSG_ADAPTER.addMessageType(LoadClassAck.class);
 		testEnDeCoding(new LoadClassAck("ClassName", TEST_APP_UUID));
 	}
 
 	@Test
 	public void loadClassMessageTest() {
 		byte[] b = "Hello testing world".getBytes();
 
 		MSG_ADAPTER.addMessageType(LoadClassMessage.class);
 		testEnDeCoding(new LoadClassMessage("ClassName", b, TEST_APP_UUID));
 	}
 
 	@Test
 	public void loadClassNakTest() {
 
 		MSG_ADAPTER.addMessageType(LoadClassNak.class);
 		testEnDeCoding(new LoadClassNak("ClassName", TEST_APP_UUID));
 	}
 
 	@Test
 	public void blockFoundMessageTest() {
 
 		MSG_ADAPTER.addMessageType(BlockFoundResponse.class);
 		testEnDeCoding(new BlockFoundResponse(TEST_MODULE_UUID));
 	}
 
 	@Test
 	public void valueAckTest() {
 
 		MSG_ADAPTER.addMessageType(ValueAck.class);
 		testEnDeCoding(new ValueAck(TEST_APP_UUID));
 	}
 
 	@Test
 	public void valueNakTest() {
 
 		MSG_ADAPTER.addMessageType(ValueNak.class);
 		testEnDeCoding(new ValueNak(TEST_APP_UUID, ValueNak.ErrorType.WRONG_MODULE, TEST_FUNBLOCK_UUID, "InputName"));
 
 	}
 
 	@Test
 	public void whoHasBlockMessageTest() {
 
 		MSG_ADAPTER.addMessageType(WhoHasBlockMessage.class);
 		testEnDeCoding(new WhoHasBlockMessage(TEST_APP_UUID, TEST_FUNBLOCK_UUID));
 
 	}
 
 	@Test
 	public void valueMessageTest() { // gets special handling because we are dealing with serializables and they do not
 										// provide equals.
 
 		@SuppressWarnings({ "serial" })
 		class Seri implements Serializable {
 			int a = 42;
 			Long l = 12L;
 			FunctionBlock con = new BeamerOperatorBlock();
 
 			@Override
 			public String toString() {
 				return "Seri [a=" + a + ", l=" + l + ", con=" + con + "]";
 			}
 
 		}
 
 		Seri oldSeri = new Seri();
 
 		MSG_ADAPTER.addMessageType(ValueMessage.class);
 		ValueMessage msg = new ValueMessage(TEST_APP_UUID, TEST_FUNBLOCK_UUID, "InputName", oldSeri);
 
 		String gsonHolder;
 		ValueMessage decodedMsg;
 
 		try {
 			gsonHolder = GSON.toJson(msg, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("Encoding Error in MSG: {} .FAIL: {}", msg, msg.getClass());
 			throw new Error(ex);
 		}
 		try {
 			decodedMsg = (ValueMessage) GSON.fromJson(gsonHolder, Message.class);
 		} catch (Exception ex) {
			LOGGER.fatal("{}\nDecoding Error. Encoded Gson: \n\n{}\nFAIL: {}", msg, gsonHolder, msg.getClass());
 			throw new Error(ex);
 		}
 		boolean isEqual = true;
 
 		if (!decodedMsg.getApplicationID().equals(msg.getApplicationID())) {
 			isEqual = false;
 		}
 		if (!decodedMsg.blockId.equals(msg.blockId)) {
 			isEqual = false;
 		}
 		if (!decodedMsg.input.equals(msg.input)) {
 			isEqual = false;
 		}
 		Seri decodedSeri = (Seri) decodedMsg.value;
 		if (decodedSeri == null || decodedSeri.a != oldSeri.a || !decodedSeri.l.equals(oldSeri.l)
 				|| !decodedSeri.con.equals(oldSeri.con)) {
 			isEqual = false;
 			LOGGER.warn("value before: " + oldSeri);
 			LOGGER.warn("value after:  " + decodedSeri);
 		}
 
 		Assert.assertTrue("Decoded " + msg + " wrong.(Or messages equal methode is broken)\nWas decoded to: "
 				+ decodedMsg, isEqual);
 
 	}
 
 	/**
 	 * The helper method doing the en-/decoding part for nearly all the tests. Takes a message, converts it to json and
 	 * back and compares the results.
 	 * 
 	 * @param msg
 	 *            the message to test encoding of.
 	 */
 	private static void testEnDeCoding(Message msg) {
 		String gsonHolder;
 		Message decodedMsg;
 
 		try {
 			gsonHolder = GSON.toJson(msg, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("Encoding Error in MSG: {} .\nFAIL: {}", msg, msg.getClass());
 			throw new Error(ex);
 		}
 		try {
 			decodedMsg = GSON.fromJson(gsonHolder, Message.class);
 		} catch (Exception ex) {
			LOGGER.fatal("{}\nDecoding Error. Encoded Gson: \n\n{}\nFAIL: {}", msg, gsonHolder, msg.getClass());
 			throw new Error(ex);
 		}
 
 		Assert.assertEquals("Decoded " + msg + " wrong.(Or messages equal methode is broken)\nWas decoded to: "
 				+ decodedMsg, msg, decodedMsg);
 
 	}
 }
