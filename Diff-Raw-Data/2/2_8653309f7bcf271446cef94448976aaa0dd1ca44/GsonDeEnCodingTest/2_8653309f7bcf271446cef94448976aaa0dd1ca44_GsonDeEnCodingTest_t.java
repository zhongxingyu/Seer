 package edu.teco.dnd.tests;
 
 import java.io.Serializable;
 import java.lang.reflect.Modifier;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.Collection;
 import java.util.LinkedList;
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
 import edu.teco.dnd.module.messages.infoReq.ApplicationListResponse;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessage;
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
 import edu.teco.dnd.module.messages.values.BlockFoundMessage;
 import edu.teco.dnd.module.messages.values.ValueAck;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.ValueNak;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.network.codecs.MessageAdapter;
 import edu.teco.dnd.network.messages.Message;
 import edu.teco.dnd.util.Base64Adapter;
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 import edu.teco.dnd.util.NetConnection;
 import edu.teco.dnd.util.NetConnectionAdapter;
 import edu.teco.dnd.util.SerializableAdapter;
 
 public class GsonDeEnCodingTest implements Serializable {
 	private static final Logger LOGGER = LogManager.getLogger(GsonDeEnCodingTest.class);
 
 	private final static UUID TEST_MODULE_UUID = UUID.fromString("00000000-9abc-def0-1234-56789abcdef0");
 	private final static UUID TEST_APP_UUID = UUID.fromString("11111111-9abc-def0-1234-56789abcdef0");
 	private final static UUID TEST_FUNBLOCK_UUID = UUID.fromString("99999999-9abc-def0-1234-56789abcdef0");
 
 	private static final Gson gson;
 	private static final MessageAdapter msgAdapter = new MessageAdapter();
 	
 	static {
 		GsonBuilder builder = new GsonBuilder();
 		builder.setPrettyPrinting();
 		builder.registerTypeAdapter(Message.class, msgAdapter);
 		builder.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		builder.registerTypeAdapter(NetConnection.class, new NetConnectionAdapter());
 		builder.registerTypeAdapter(byte[].class, new Base64Adapter());
		builder.registerTypeAdapter(FunctionBlock.class, new SerializableAdapter(null));
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
 	public void ModuleInfoMessageTest() {
 		msgAdapter.addMessageType(ModuleInfoMessage.class);
 		try {
 			testEnDeCoding(new ModuleInfoMessage(TestConfigReader.getPredefinedReader()));
 		} catch (SocketException e) {
 			e.printStackTrace();
 			throw new Error(e);
 		}
 
 	}
 
 	// TODO change failures into exceptions
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
 		testEnDeCoding(new BlockAck("ClassName", TEST_APP_UUID));
 
 	}
 
 	@Test
 	public void BlockMessageTest() {
 
 		msgAdapter.addMessageType(BlockMessage.class);
 		testEnDeCoding(new BlockMessage("ClassName", TEST_APP_UUID, new BeamerOperatorBlock(TEST_FUNBLOCK_UUID)));
 	}
 
 	@Test
 	public void BlockNakTest() {
 
 		msgAdapter.addMessageType(BlockNak.class);
 		testEnDeCoding(new BlockNak("ClassName", TEST_APP_UUID));
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
 
 		msgAdapter.addMessageType(BlockFoundMessage.class);
 		testEnDeCoding(new BlockFoundMessage(TEST_APP_UUID, TEST_MODULE_UUID, TEST_FUNBLOCK_UUID));
 	}
 
 	@Test
 	public void ValueAckTest() {
 
 		msgAdapter.addMessageType(ValueAck.class);
 		testEnDeCoding(new ValueAck(TEST_APP_UUID));
 	}
 
 	@Test
 	public void ValueMessageTest() {
 		@SuppressWarnings({ "unused", "serial" })
 		class Seri implements Serializable {
 			int a = 42;
 			Long l = 12L;
 			FunctionBlock con = new BeamerOperatorBlock(TEST_FUNBLOCK_UUID);
 		}
 
 		
 		//FIXME: GSON can not handle multiple fields with same name in parent & childclass. Add to that serialVersionUID and we are in trouble!
 		// compare: http://code.google.com/p/google-gson/issues/detail?id=399
 		
 		msgAdapter.addMessageType(ValueMessage.class);
 		testEnDeCoding(new ValueMessage(TEST_APP_UUID, TEST_FUNBLOCK_UUID, "InputName", new Seri()));
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
 
 	private static void addTestMessages(Collection<Message> testMsgs) throws SecurityException {
 		// TODO overwrite equals/toString of Messages properly.
 
 	}
 
 	public static void testEnDeCoding(Message msg) {
 		String gsonHolder;
 		Message decodedMsg;
 
 		LOGGER.info(msg.getClass().toString());
 		try {
 			gsonHolder = gson.toJson(msg, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("Encoding Error in MSG: {} .\n#\n#\n#\n####################FAIL: {}", msg, msg.getClass());
 			throw new Error(ex);
 		}
 		LOGGER.info("Gson is:\n--\n{}\n--", gsonHolder);
 		try {
 			decodedMsg = gson.fromJson(gsonHolder, Message.class);
 		} catch (Exception ex) {
 			LOGGER.fatal("{}\nDecoding Error.Encoded Gson: \n\n{}\n\n#\n#\n#\n####################FAIL: {}", msg,
 					gsonHolder, msg.getClass());
 			throw new Error(ex);
 		}
 
 		Assert.assertEquals("Decoded " + msg + " wrong.(Or messages equal methode is broken)\nWas decoded to: "
 				+ decodedMsg, msg, decodedMsg);
 
 	}
 }
