 package edu.teco.dnd.tests;
 
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.junit.Test;
 import org.junit.Assert;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.meeting.BeamerOperatorBlock;
 import edu.teco.dnd.module.ModuleApplicationManager;
 import edu.teco.dnd.module.messages.BlockMessageDeserializerAdapter;
 import edu.teco.dnd.module.messages.BlockMessageSerializerAdapter;
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.config.ConfigReader;
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
 import edu.teco.dnd.module.messages.values.AppBlockIdFoundMessage;
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
 
 public class GsonDeEnCodingTest implements Serializable {
 
 	private static final long serialVersionUID = -6437521431147083820L;
 
 	private static final Logger LOGGER = LogManager.getLogger(GsonDeEnCodingTest.class);
 
 	private static final UUID TEST_MODULE_UUID = UUID.fromString("00000000-9abc-def0-1234-56789abcdef0");
 	private static final UUID TEST_APP_UUID = UUID.fromString("11111111-9abc-def0-1234-56789abcdef0");
 	private static final UUID TEST_FUNBLOCK_UUID = UUID.fromString("99999999-9abc-def0-1234-56789abcdef0");
 
 	private static final Gson gson;
 	private static final MessageAdapter msgAdapter = new MessageAdapter();
 
 	static {
 
 		ModuleApplicationManager appMan;
 		try {
 			appMan = new ModuleApplicationManager(TestConfigReader.getPredefinedReader(), null) {
 				public ClassLoader getAppClassLoader(UUID appId) {
 					return null;
 				};
 			};
 		} catch (SocketException e) {
 			throw new Error(e);
 		}
 
 		GsonBuilder builder = new GsonBuilder();
 		builder.setPrettyPrinting();
 		builder.registerTypeAdapter(Message.class, msgAdapter);
 		builder.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		builder.registerTypeAdapter(NetConnection.class, new NetConnectionAdapter());
 		builder.registerTypeAdapter(byte[].class, new Base64Adapter());
 		builder.registerTypeAdapter(BlockMessage.class, new BlockMessageSerializerAdapter());
 		builder.registerTypeAdapter(BlockMessage.class, new BlockMessageDeserializerAdapter(appMan));
 		builder.registerTypeAdapter(ValueMessage.class, new ValueMessageAdapter(appMan));
 		builder.registerTypeAdapter(ModuleInfoMessage.class, new ModuleInfoMessageAdapter());
 		gson = builder.create();
 	}
 
 	@Test
 	public void ApplicationListResponseTest() {
 
 		Map<UUID, String> modIds = new TreeMap<UUID, String>();
 		modIds.put(TEST_APP_UUID, "APP_1");
 		msgAdapter.addMessageType(ApplicationListResponse.class);
 		testEnDeCoding(new ApplicationListResponse(TEST_MODULE_UUID, modIds));
 	}
 
 	@Test
 	public void RequestApplicationListMessageTest() {
 
 		msgAdapter.addMessageType(RequestApplicationListMessage.class);
 		testEnDeCoding(new RequestApplicationListMessage());
 	}
 	
 	@Test
 	public void ShutdownModuleMessageTest() {
 		msgAdapter.addMessageType(ShutdownModuleMessage.class);
 		testEnDeCoding(new ShutdownModuleMessage());
 	}
 	
 	@Test
 	public void ShutdownModuleNakTest() {
 		msgAdapter.addMessageType(ShutdownModuleNak.class);
 		testEnDeCoding(new ShutdownModuleNak());
 	}
 	
 	@Test
 	public void ShutdownModuleAckTest() {
 		msgAdapter.addMessageType(ShutdownModuleAck.class);
 		testEnDeCoding(new ShutdownModuleAck());
 	}
 	
 	@Test
 	public void MissingApplicationNakTest() {
 		msgAdapter.addMessageType(MissingApplicationNak.class);
 		testEnDeCoding(new MissingApplicationNak(TEST_APP_UUID));
 	}
 
 
 	@Test
 	public void ModuleInfoMessageTest() {
 		msgAdapter.addMessageType(ModuleInfoMessage.class);
 		try {
 			final ConfigReader configReader = TestConfigReader.getPredefinedReader();
 			testEnDeCoding(new ModuleInfoMessage(new Module(configReader.getUuid(), configReader.getName(), configReader.getBlockRoot())));
 		} catch (SocketException e) {
 			e.printStackTrace();
 			throw new Error(e);
 		}
 
 	}
 
 	@Test
 	public void RequestModuleInfoMessageTest() {
 
 		msgAdapter.addMessageType(RequestModuleInfoMessage.class);
 		testEnDeCoding(new RequestModuleInfoMessage());
 	}
 
 	@Test
 	public void JoinApplicationMessageTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		msgAdapter.addMessageType(JoinApplicationMessage.class);
 		testEnDeCoding(jam);
 	}
 
 	@Test
 	public void JoinApplicationAckTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		msgAdapter.addMessageType(JoinApplicationAck.class);
 		testEnDeCoding(new JoinApplicationAck(jam));
 
 	}
 
 	@Test
 	public void JoinApplicationNakTest() {
 		JoinApplicationMessage jam = new JoinApplicationMessage("appName", TEST_APP_UUID);
 		msgAdapter.addMessageType(JoinApplicationNak.class);
 		testEnDeCoding(new JoinApplicationNak(jam));
 
 	}
 
 	@Test
 	public void StartApplicationMessageTest() {
 
 		msgAdapter.addMessageType(StartApplicationMessage.class);
 		testEnDeCoding(new StartApplicationMessage(TEST_APP_UUID));
 
 	}
 
 	@Test
 	public void KillAppMessageTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 
 		msgAdapter.addMessageType(KillAppMessage.class);
 		testEnDeCoding(kam);
 	}
 
 	@Test
 	public void KillAppAckTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 		msgAdapter.addMessageType(KillAppAck.class);
 		testEnDeCoding(new KillAppAck(kam));
 
 	}
 
 	@Test
 	public void KillAppNakTest() {
 		KillAppMessage kam = new KillAppMessage(TEST_APP_UUID);
 		msgAdapter.addMessageType(KillAppNak.class);
 		testEnDeCoding(new KillAppNak(kam));
 
 	}
 
 	@Test
 	public void BlockAckTest() {
 		msgAdapter.addMessageType(BlockAck.class);
 		testEnDeCoding(new BlockAck());
 
 	}
 
 	@Test
 	public void BlockMessageTest() {
 
 		msgAdapter.addMessageType(BlockMessage.class);
 		testEnDeCoding(new BlockMessage(TEST_APP_UUID, new BeamerOperatorBlock(TEST_FUNBLOCK_UUID)));
 	}
 
 	@Test
 	public void BlockNakTest() {
 
 		msgAdapter.addMessageType(BlockNak.class);
 		testEnDeCoding(new BlockNak());
 	}
 
 	@Test
 	public void LoadClassAckTest() {
 
 		msgAdapter.addMessageType(LoadClassAck.class);
 		testEnDeCoding(new LoadClassAck("ClassName", TEST_APP_UUID));
 	}
 
 	@Test
 	public void LoadClassMessageTest() {
 		byte[] b = "Hello testing world".getBytes();
 
 		msgAdapter.addMessageType(LoadClassMessage.class);
 		testEnDeCoding(new LoadClassMessage("ClassName", b, TEST_APP_UUID));
 	}
 
 	@Test
 	public void LoadClassNakTest() {
 
 		msgAdapter.addMessageType(LoadClassNak.class);
 		testEnDeCoding(new LoadClassNak("ClassName", TEST_APP_UUID));
 	}
 
 	@Test
 	public void AppBlockIdFoundMessageTest() {
 
 		msgAdapter.addMessageType(AppBlockIdFoundMessage.class);
 		testEnDeCoding(new AppBlockIdFoundMessage(TEST_APP_UUID, TEST_MODULE_UUID, TEST_FUNBLOCK_UUID));
 	}
 
 	@Test
 	public void BlockFoundMessageTest() {
 
 		msgAdapter.addMessageType(BlockFoundResponse.class);
		testEnDeCoding(new BlockFoundResponse(TEST_MODULE_UUID));
 	}
 
 	@Test
 	public void ValueAckTest() {
 
 		msgAdapter.addMessageType(ValueAck.class);
 		testEnDeCoding(new ValueAck(TEST_APP_UUID));
 	}
 
 	@Test
 	public void ValueNakTest() {
 
 		msgAdapter.addMessageType(ValueNak.class);
 		testEnDeCoding(new ValueNak(TEST_APP_UUID, ValueNak.ErrorType.WRONG_MODULE, TEST_FUNBLOCK_UUID, "InputName"));
 
 	}
 
 	@Test
 	public void WhoHasBlockMessageTest() {
 
 		msgAdapter.addMessageType(WhoHasBlockMessage.class);
 		testEnDeCoding(new WhoHasBlockMessage(TEST_APP_UUID, TEST_FUNBLOCK_UUID));
 
 	}
 
 	@Test
 	public void ValueMessageTest() { // gets special handling because we are dealing with serializables and they do not
 										// provide equals.
 
 		@SuppressWarnings({ "serial" })
 		class Seri implements Serializable {
 			int a = 42;
 			Long l = 12L;
 			FunctionBlock con = new BeamerOperatorBlock(TEST_FUNBLOCK_UUID);
 
 			@Override
 			public String toString() {
 				return "Seri [a=" + a + ", l=" + l + ", con=" + con + "]";
 			}
 
 		}
 
 		Seri oldSeri = new Seri();
 
 		msgAdapter.addMessageType(ValueMessage.class);
 		ValueMessage msg = new ValueMessage(TEST_APP_UUID, TEST_FUNBLOCK_UUID, "InputName", oldSeri);
 
 		String gsonHolder;
 		ValueMessage decodedMsg;
 
 		try {
 			gsonHolder = gson.toJson(msg, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("Encoding Error in MSG: {} .FAIL: {}", msg, msg.getClass());
 			throw new Error(ex);
 		}
 		try {
 			decodedMsg = (ValueMessage) gson.fromJson(gsonHolder, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("{}\nDecoding Error.Encoded Gson: \n\n{}\nFAIL: {}", msg, gsonHolder, msg.getClass());
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
 
 	public static void testEnDeCoding(Message msg) {
 		String gsonHolder;
 		Message decodedMsg;
 
 		try {
 			gsonHolder = gson.toJson(msg, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("Encoding Error in MSG: {} .\nFAIL: {}", msg, msg.getClass());
 			throw new Error(ex);
 		}
 		try {
 			decodedMsg = gson.fromJson(gsonHolder, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("{}\nDecoding Error.Encoded Gson: \n\n{}\nFAIL: {}", msg, gsonHolder, msg.getClass());
 			throw new Error(ex);
 		}
 
 		Assert.assertEquals("Decoded " + msg + " wrong.(Or messages equal methode is broken)\nWas decoded to: "
 				+ decodedMsg, msg, decodedMsg);
 
 	}
 }
