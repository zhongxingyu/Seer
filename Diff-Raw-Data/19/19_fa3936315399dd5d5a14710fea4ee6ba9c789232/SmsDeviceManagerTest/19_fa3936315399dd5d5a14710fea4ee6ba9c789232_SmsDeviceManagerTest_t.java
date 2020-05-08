 /**
  * 
  */
 package net.frontlinesms.smsdevice;
 
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.smslib.CIncomingMessage;
 
 import net.frontlinesms.data.domain.Message;
 import net.frontlinesms.junit.BaseTestCase;
 import net.frontlinesms.smsdevice.internet.SmsInternetService;
 
 import static org.mockito.Mockito.*;
 
 /**
  * Test class for {@link SmsDeviceManager}.
  * @author aga
  */
 public class SmsDeviceManagerTest extends BaseTestCase {
 	/**
 	 * Test that all messages sent will be given to the {@link SmsInternetService} in preference to
 	 * any {@link SmsModem}s available.
 	 */
 	public void testMessageDispatchPriorities_text() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 
 		SmsInternetService sisNoSend = createMockSmsInternetService(false, true);
 		manager.addSmsInternetService(sisNoSend);
 		SmsInternetService sisNoSendNoBinary = createMockSmsInternetService(false, false);
 		manager.addSmsInternetService(sisNoSendNoBinary);
 		SmsInternetService sisBinary = createMockSmsInternetService(true, true);
 		manager.addSmsInternetService(sisBinary);
 		SmsInternetService sisNoBinary = createMockSmsInternetService(true, false);
 		manager.addSmsInternetService(sisNoBinary);
 		
 		SmsModem modem = createMockModem(true, true, true, true);
 		addModem(manager, modem, "TestModem1");
 		
 		sendSms(manager, generateMessages(20, MessageType.GSM7BIT_TEXT));
 		
 		manager.doRun();
 		
 		// Check that all messages were sent with the TWO functioning internet services, and nothing else
 		verify(modem, never()).sendSMS(any(Message.class));
 		verify(sisNoSend, never()).sendSMS(any(Message.class));
 		verify(sisNoSendNoBinary, never()).sendSMS(any(Message.class));
 		verify(sisBinary, times(10)).sendSMS(any(Message.class));
 		verify(sisNoBinary, times(10)).sendSMS(any(Message.class));
 	}
 	
 	/**
 	 * Test that all messages sent will be given to the {@link SmsInternetService} in preference to
 	 * any {@link SmsModem}s available.
 	 */
 	public void testMessageDispatchPriorities_binary() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 
 		SmsInternetService sisNoSend = createMockSmsInternetService(false, true);
 		manager.addSmsInternetService(sisNoSend);
 		SmsInternetService sisNoSendNoBinary = createMockSmsInternetService(false, false);
 		manager.addSmsInternetService(sisNoSendNoBinary);
 		SmsInternetService sisBinary = createMockSmsInternetService(true, true);
 		manager.addSmsInternetService(sisBinary);
 		SmsInternetService sisNoBinary = createMockSmsInternetService(true, false);
 		manager.addSmsInternetService(sisNoBinary);
 		
 		SmsModem modem = createMockModem(true, true, true, true);
 		addModem(manager, modem, "TestModem1");
 		
 		sendSms(manager, generateMessages(20, MessageType.BINARY));
 		
 		manager.doRun();
 		
 		// Check that all messages were sent with the ONE internet services which is functioning and sends binary, and nothing else
 		verify(modem, never()).sendSMS(any(Message.class));
 		verify(sisNoSend, never()).sendSMS(any(Message.class));
 		verify(sisNoSendNoBinary, never()).sendSMS(any(Message.class));
 		verify(sisNoBinary, never()).sendSMS(any(Message.class));
 		verify(sisBinary, times(20)).sendSMS(any(Message.class));
 	}
 	
 	/** Test that text messages are sent only with suitable modems. */
 	public void testModemSend_text() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 		
 		SmsModem disconnectedModem = createMockModem(false, false, true, true);
 		addModem(manager, disconnectedModem, "Disconnected.");
 		SmsModem gsmOnlyModem = createMockModem(true, true, false, false);
 		addModem(manager, gsmOnlyModem, "GsmOnly");
 		SmsModem ucs2Modem = createMockModem(true, true, false, true);
 		addModem(manager, ucs2Modem, "ucs2");
 		SmsModem binaryModem = createMockModem(true, true, true, false);
 		addModem(manager, binaryModem, "binary");
 		SmsModem everythingModem = createMockModem(true, true, true, true);
 		addModem(manager, everythingModem, "everything");
 		
 		// Sending no messages
 		manager.doRun();
 		verify(disconnectedModem, never()).sendSMS(any(Message.class));
 		verify(gsmOnlyModem, never()).sendSMS(any(Message.class));
 		verify(ucs2Modem, never()).sendSMS(any(Message.class));
 		verify(binaryModem, never()).sendSMS(any(Message.class));
 		verify(everythingModem, never()).sendSMS(any(Message.class));
 
 		// Send some simple text messages, and make sure that they were send with the expected modems
 		Collection<Message> gsm7bitMessages = generateMessages(8, MessageType.GSM7BIT_TEXT);
 		sendSms(manager, gsm7bitMessages);
 		manager.doRun();
 		verify(disconnectedModem, never()).sendSMS(any(Message.class));
 		verify(gsmOnlyModem, times(2)).sendSMS(any(Message.class));
 		verify(ucs2Modem, times(2)).sendSMS(any(Message.class));
 		verify(binaryModem, times(2)).sendSMS(any(Message.class));
 		verify(everythingModem, times(2)).sendSMS(any(Message.class));
 	}
 	
 	/** Test that binary messages are sent only with suitable modems. */
 	public void testModemSend_binary() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 		
 		SmsModem disconnectedModem = createMockModem(false, false, true, true);
 		addModem(manager, disconnectedModem, "Disconnected.");
 		SmsModem gsmOnlyModem = createMockModem(true, true, false, false);
 		addModem(manager, gsmOnlyModem, "GsmOnly");
 		SmsModem ucs2Modem = createMockModem(true, true, false, true);
 		addModem(manager, ucs2Modem, "ucs2");
 		SmsModem binaryModem = createMockModem(true, true, true, false);
 		addModem(manager, binaryModem, "binary");
 		SmsModem everythingModem = createMockModem(true, true, true, true);
 		addModem(manager, everythingModem, "everything");
 		
 		// Send some binary messages
 		Collection<Message> binaryMessages = generateMessages(8, MessageType.BINARY);
 		sendSms(manager, binaryMessages);
 		manager.doRun();
 		verify(disconnectedModem, never()).sendSMS(any(Message.class));
 		verify(gsmOnlyModem, never()).sendSMS(any(Message.class));
 		verify(ucs2Modem, never()).sendSMS(any(Message.class));
 		verify(binaryModem, times(4)).sendSMS(any(Message.class));
 		verify(everythingModem, times(4)).sendSMS(any(Message.class));
 	}
 	
 	/** Test that binary messages are sent only with suitable modems. */
 	public void testModemSend_ucs2() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 		
 		SmsModem disconnectedModem = createMockModem(false, false, true, true);
 		addModem(manager, disconnectedModem, "Disconnected.");
 		SmsModem gsmOnlyModem = createMockModem(true, true, false, false);
 		addModem(manager, gsmOnlyModem, "GsmOnly");
 		SmsModem ucs2Modem = createMockModem(true, true, false, true);
 		addModem(manager, ucs2Modem, "ucs2");
 		SmsModem binaryModem = createMockModem(true, true, true, false);
 		addModem(manager, binaryModem, "binary");
 		SmsModem everythingModem = createMockModem(true, true, true, true);
 		addModem(manager, everythingModem, "everything");
 		
 		// Send some UCS2 messages
 		Collection<Message> ucs2Messages = generateMessages(8, MessageType.UCS2_TEXT);
 		sendSms(manager, ucs2Messages);
 		manager.doRun();
 		verify(disconnectedModem, never()).sendSMS(any(Message.class));
 		verify(gsmOnlyModem, never()).sendSMS(any(Message.class));
 		verify(ucs2Modem, times(4)).sendSMS(any(Message.class));
 		verify(binaryModem, never()).sendSMS(any(Message.class));
 		verify(everythingModem, times(4)).sendSMS(any(Message.class));
 	}
 	
 	/** Test that messages are polled from all modems who have message receiving enabled. */
 	public void testModemMessageReceive() {
 		SmsDeviceManager manager = new SmsDeviceManager();
 		
 		SmsModem[] receiveModems = new SmsModem[10];
 		for (int i = 0; i < receiveModems.length; i++) {
 			SmsModem modem = createMockModem(i%2==0, true, i%3==0, i%5==0);
 			receiveModems[i] = modem;
 			addModem(manager, modem, "Receive " + i);
 		}
 		SmsModem[] nonReceiveModems = new SmsModem[10];
 		for (int i = 0; i < nonReceiveModems.length; i++) {
 			SmsModem modem = createMockModem(i%2==0, false, i%3==0, i%5==0);
 			nonReceiveModems[i] = modem;
 			addModem(manager, modem, "NonReceive " + i);
 		}
 		
 		// Now create some modems with messages
 		CIncomingMessage mockMessage = mock(CIncomingMessage.class);
 		
 		SmsModem modemWith1Message = createMockModem(false, true, false, false);
 		when(modemWith1Message.nextIncomingMessage())
 				.thenReturn(mockMessage)
 				.thenReturn(null);
 		addModem(manager, modemWith1Message, "ModemWith1Message");
 		
 		SmsModem modemWith3Messages = createMockModem(false, true, false, false);
 		when(modemWith3Messages.nextIncomingMessage())
 				.thenReturn(mockMessage)
 				.thenReturn(mockMessage)
 				.thenReturn(mockMessage)
 				.thenReturn(null);
 		addModem(manager, modemWith3Messages, "ModemWith3Messages");
 
 		
 		manager.doRun();
 
 		for(SmsModem modem : receiveModems) {
 			verify(modem).nextIncomingMessage();
 		}
 		for(SmsModem modem : nonReceiveModems) {
 			verify(modem, never()).nextIncomingMessage();
 		}
 		verify(modemWith1Message, times(2)).nextIncomingMessage();
 		verify(modemWith3Messages, times(4)).nextIncomingMessage();
 	}
 	
	/** Tests that when there are no SMS devices, the messages are left in outbox. */
	public void testNoSmsDevices() {
		SmsDeviceManager manager = new SmsDeviceManager();
		Message m = Message.createOutgoingMessage(System.currentTimeMillis(), "+123456", "+987654", "Hi");
		manager.sendSMS(m);
		manager.doRun();
		
		assertEquals(Message.STATUS_OUTBOX, m.getStatus());
	}
	
 	
 //> PRIVATE HELPER METHODS
 	/** @return a mock {@link SmsInternetService} with certain important methods stubbed */
 	private SmsInternetService createMockSmsInternetService(boolean useForSending, boolean supportsBinary) {
 		SmsInternetService mock = mock(SmsInternetService.class);
 		when(mock.isConnected()).thenReturn(true);
 		when(mock.isUseForSending()).thenReturn(useForSending);
 		when(mock.isBinarySendingSupported()).thenReturn(supportsBinary);
 		return mock;
 	}
 
 	/** @return a mock {@link SmsModem} with certain important methods stubbed */
 	private SmsModem createMockModem(boolean useForSending, boolean useForReceiving, boolean supportsBinary, boolean supportsUcs2) {
 		SmsModem mock = mock(SmsModem.class);
 		when(mock.isConnected()).thenReturn(true);
 		when(mock.isUseForSending()).thenReturn(useForSending);
 		when(mock.isUseForReceiving()).thenReturn(useForReceiving);
 		when(mock.isBinarySendingSupported()).thenReturn(supportsBinary);
 		when(mock.isUcs2SendingSupported()).thenReturn(supportsUcs2);
 		return mock;
 	}
 	
 	/** Adds a {@link SmsModem} to {@link SmsDeviceManager#phoneHandlers} by reflection. */
 	@SuppressWarnings("unchecked")
 	private void addModem(SmsDeviceManager manager, SmsModem modem, String modemId) {
 		try {
 			Field handlerField = SmsDeviceManager.class.getDeclaredField("phoneHandlers");
 			handlerField.setAccessible(true);
 			Map<String, SmsModem> handlers = (Map<String, SmsModem>) handlerField.get(manager);
 			handlers.put(modemId, modem);
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	/** @return some generated SMS messages */
 	private Collection<Message> generateMessages(int count, MessageType type) {
 		HashSet<Message> messages = new HashSet<Message>();
 		while(--count >= 0) {
 			Message m;
 			long now = System.currentTimeMillis();
 			String recipientMsisdn = "Recipient " + count;
 			String senderMsisdn = "Sender " + count;
 			if(type == MessageType.BINARY) {
 				byte[] data = new byte[count];
 				for (int i = 0; i < data.length; i++) {
 					data[i] = (byte) i;
 				}
 				m = Message.createBinaryOutgoingMessage(now, senderMsisdn, recipientMsisdn, 0, data);
 			} else {
 				String content = "Content " + count;
 				if(type == MessageType.UCS2_TEXT) {
 					// Add some random arabic letters to the text content
 					content += "\u0634\u0626\u0647\u0629";
 				} 
 				m = Message.createOutgoingMessage(now, senderMsisdn, recipientMsisdn, content);
 			}
 			messages.add(m);
 		}
 		return messages;
 	}
 
 	/** Send multiple SMS to the manager */
 	private void sendSms(SmsDeviceManager manager, Collection<Message> messages) {
 		for(Message m : messages) manager.sendSMS(m);
 	}
 }
