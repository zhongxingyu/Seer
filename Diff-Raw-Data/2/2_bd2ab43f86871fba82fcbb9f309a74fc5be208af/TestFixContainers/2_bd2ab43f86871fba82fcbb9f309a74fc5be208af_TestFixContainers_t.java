 package org.tomac.protocol.fix;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.nio.ByteBuffer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.tomac.protocol.fix.messaging.FixLogon;
 import org.tomac.protocol.fix.messaging.FixMessageInfo;
 import org.tomac.protocol.fix.messaging.FixMessageListenerImpl;
 import org.tomac.protocol.fix.messaging.FixMessageParser;
 import org.tomac.protocol.fix.messaging.FixMessagePool;
 import org.tomac.protocol.fix.messaging.FixTestRequest;
 
 public class TestFixContainers {
 	TestFixMessageListener listener;
 	FixValidationError err;
 	FixMessagePool<FixInMessage> pool;
 	
 	FixInMessage inMsg;
 	FixMessage outMsg;
 	
 	@Before
 	public void setUp() throws Exception {
 		listener = new TestFixMessageListener();
 		err = new FixValidationError();
 		FixUtils.validateChecksum = false;
 		if ((new String(FixMessageInfo.BEGINSTRING_VALUE).equals("FIX.4.2") ) ) FixUtils.isNasdaqOMX =  true;
 		else FixUtils.isNasdaqOMX = false;
 		pool = new FixMessagePool<FixInMessage>();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testParseValidFixInMessages() {
 		
 		FixMessageParser parser = new FixMessageParser();
 		
 		String[] fixMessages  = {
 			"8=FIXT.1.1\u00019=0106\u000135=A\u000149=MSI\u000156=TOMAC\u00011137=9\u000134=1\u000152=20101007-06:00:07.732\u000150=STOM1\u000198=0\u0001108=60\u0001141=N\u0001553=MSITS1\u0001554=MWAT\u000110=241\u0001",
 			"8=FIXT.1.1\u00019=0061\u000135=0\u000149=MSI\u000156=TOMAC\u000134=2\u000152=20101007-06:01:07.836\u000150=STOM1\u000110=237\u0001",
 			"8=FIXT.1.1\u00019=0154\u000135=D\u000149=MSI\u000156=TOMAC\u000134=230\u000152=20101007-15:43:36.290\u000150=STOM1\u000111=GEDVOO81\u000154=1\u000140=2\u000159=0\u000155=OMXS300K\u000138=15\u000144=1090\u000160=20101007-15:43:36.242\u0001432=20101007\u000110=056\u0001"
 		};
 
 		for (String s : fixMessages) {
 			if (!s.contains(new String(FixMessageInfo.BEGINSTRING_VALUE)) ) continue; 
 			
 			ByteBuffer refBuf = ByteBuffer.wrap(s.getBytes());
 			ByteBuffer parseBuf = ByteBuffer.wrap(s.getBytes());
 
 			inMsg = pool.getFixMessage(refBuf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 
 			if (inMsg instanceof FixTestRequest) 
 				assertTrue(((FixTestRequest)inMsg).toString().length() > 0);
 			if (inMsg instanceof FixLogon) {
 				FixLogon l = (FixLogon)inMsg;
 				assertTrue(l.toString().length() > 0);
 			}
 			
 			parser.parse(parseBuf, err, listener);
 			assertFalse(err.hasError());
 			err.clear();
 		}
 	}
 
 	@Test
 	public void testParseValidStreamOfFixInMessages() {
 		
 		FixMessageParser parser = new FixMessageParser();
 		
 		String[] fixMessages = { 
 			"8=FIXT.1.1\u00019=0106\u000135=A\u000149=MSI\u000156=TOMAC\u00011137=9\u000134=1\u000152=20101007-06:00:07.732\u000150=STOM1\u000198=0\u0001108=60\u0001141=N\u0001553=MSITS1\u0001554=MWAT\u000110=241\u0001",
 			"8=FIXT.1.1\u00019=0061\u000135=0\u000149=MSI\u000156=TOMAC\u000134=2\u000152=20101007-06:01:07.836\u000150=STOM1\u000110=237\u0001"
 		};
 
 		String ss = "";
 		int count = 0;
 		for (String s : fixMessages) {
 			if (!s.contains(new String(FixMessageInfo.BEGINSTRING_VALUE)) ) continue; 
 			count++;
 			ss += s;
 		}
 
 		ByteBuffer buf = ByteBuffer.wrap(ss.getBytes());
 
 		while(buf.hasRemaining()) {
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(inMsg.getLastFixValidationError().hasError());
 
 			ByteBuffer old = buf.duplicate();
 			old.flip();
 			parser.parse(old, err, listener);
 			assertFalse(err.hasError());
 
 			err.clear();
 			
 			count--;
 		}
 		assertEquals(0, count);
 	}
 	
 	
 	@Test
 	public void testParseValidSessionFixOutMessages() {
 		
 		String[] fixMessages = { 
 				"8=FIXT.1.1\u00019=0106\u000135=A\u000149=MSI\u000156=TOMAC\u00011137=9\u000134=1\u000152=20101007-06:00:07.732\u000150=STOM1\u000198=0\u0001108=60\u0001141=N\u0001553=MSITS1\u0001554=MWAT\u000110=241\u0001",
 				"8=FIXT.1.1\u00019=000052\u000135=0\u000149=TOMAC\u000156=MSI\u000134=2\u000152=20101007-06:01:08.787\u000110=066\u0001",
 				"8=FIXT.1.1\u00019=000095\u000135=1\u000149=TOMAC\u000156=MSI\u000134=218\u000152=20101007-09:39:36.644\u0001112=d9a1fd5f-e538-4a44-a552-fabc6c258167\u000110=053\u0001"
 		};
 		
 		for (String s : fixMessages) {
 
 			ByteBuffer buf = ByteBuffer.wrap(s.getBytes());
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 			
 			outMsg = (FixMessage) inMsg.clone();
 
 			if (outMsg instanceof FixTestRequest) 
 				assertTrue(((FixTestRequest)outMsg).toString().length() > 0);
 			if (outMsg instanceof FixLogon) 
 				assertTrue(((FixLogon)outMsg).toString().length() > 0);
 
 			byte[] bytes = new byte[1024 * 8];
 			FixUtils.fillSpace(bytes);
 			ByteBuffer out = ByteBuffer.wrap(bytes);
 			outMsg.encode(out);
 			// ?
 			assertFalse(err.hasError());
 			
 			out.position(0);
 			inMsg = pool.getFixMessage(out, err);
 
 
 			assertFalse(err.hasError());
 
 		}
 	}
 
 	
 	@Test
 	public void testParseValidExecutionReportFixOutMessages() {
 		
 		String[] fixMessages = { 
 				"8=FIXT.1.1\u00019=000327\u000135=AE\u000149=TOMAC\u000156=MSI\u000134=255\u000157=STOM1\u000152=20101007-16:07:41.781\u0001571=191\u00011003=113:378\u0001487=0\u0001856=0\u0001828=0\u0001829=2\u0001855=1\u0001570=N\u000155=OMXS300K\u000148=21758906\u000122=M\u000138=10.0000000\u000132=10.0000000\u000131=1089.5000000\u000175=20101007\u0001715=20101007\u000160=20101007-16:07:41.000\u0001573=0\u0001552=1\u000154=2\u000137=51C9A9C1055581A4\u0001453=1\u0001448=MSI\u0001447=D\u0001452=1\u0001802=1\u0001523=SE\u0001803=32\u000110=168\u0001",
 				"8=FIXT.1.1\u00019=000198\u000135=8\u000149=TOMAC\u000156=MSI\u000134=217\u000152=20101007-09:38:59.908\u000137=NONE\u000111=GEDESZOT\u000117=6\u0001150=8\u000139=8\u0001103=0\u000155=[N/A]\u000154=1\u0001151=0\u000114=0\u00016=0\u000160=20101007-09:38:59.908\u000158=REJ- <-110023> : Illegal transaction at this time\u000110=044\u0001",
 				"8=FIXT.1.1\u00019=000236\u000135=8\u000149=TOMAC\u000156=MSI\u000134=252\u000157=STOM1\u000152=20101007-16:07:22.816\u000137=51C9A9C123448104\u000111=GEDWSAMA\u000117=174\u0001150=0\u000139=0\u000155=OMXS300K\u000148=21758906\u000122=M\u000154=1\u000138=10.0000000\u000140=2\u000144=1086.7500000\u000159=0\u0001151=10.0000000\u000114=0\u00016=0\u000160=20101007-16:07:22.816\u000110=236\u0001",
 				"8=FIXT.1.1\u00019=000219\u000135=8\u000149=TOMAC\u000156=MSI\u000134=364\u000157=STOM1\u000152=20101007-17:25:27.070\u000137=51C9A9C106677149\u000111=GEDYPUL9\u000117=855\u0001150=0\u000139=0\u000155=OMXC200K\u000148=7213980\u000122=M\u000154=1\u000138=11.0000000\u000140=1\u000159=3\u0001151=11.0000000\u000114=0\u00016=0\u000160=20101007-17:25:27.070\u000110=204\u00018=FIXT.1.1\u00019=000210\u000135=8\u000149=TOMAC\u000156=MSI\u000134=365\u000157=STOM1\u000152=20101007-17:25:27.070\u000137=51C9A9C100099119\u000111=GEDYPUL9\u000117=857\u0001150=4\u000139=4\u000155=OMXC200K\u000148=7213980\u000122=M\u000154=1\u000138=11.0000000\u000140=1\u000159=3\u0001151=0\u000114=0\u00016=0\u000160=20101007-17:25:27.070\u000110=030\u0001"
 		};
 		
 		for (String s : fixMessages) {
 
 			ByteBuffer buf = ByteBuffer.wrap(s.getBytes());
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 			
 			outMsg = (FixMessage) inMsg.clone();
 
 			if (outMsg instanceof FixTestRequest) 
 				assertTrue(((FixTestRequest)outMsg).toString().length() > 0);
 			if (outMsg instanceof FixLogon) 
 				assertTrue(((FixLogon)outMsg).toString().length() > 0);
 
 			byte[] bytes = new byte[1024 * 8];
 			FixUtils.fillSpace(bytes);
 			ByteBuffer out = ByteBuffer.wrap(bytes);
 			outMsg.encode(out);
 			// ?
 			assertFalse(err.hasError());
 			
 			out.position(0);
 			inMsg = pool.getFixMessage(out, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 
 		}
 	}
 
 	@Test
 	public void testParseValidRaquirdeTagMissignFixOutMessages() {
 		
 		String[] fixMessages = { 
 				"8=FIXT.1.1\u00019=000198\u000135=8\u000149=TOMAC\u000156=MSI\u000134=217\u000152=20101007-09:38:59.908\u000137=NONE\u000111=GEDESZOT\u000117=6\u0001150=8\u000139=8\u0001103=0\u0001151=0\u000114=0\u00016=0\u000160=20101007-09:38:59.908\u000158=REJ- <-110023> : Illegal transaction at this time\u000110=044\u0001", // \u000155=[N/A]
 				"8=FIXT.1.1\u00019=000236\u000135=8\u000149=TOMAC\u000156=MSI\u000134=252\u000157=STOM1\u000152=20101007-16:07:22.816\u000137=23445660008104\u000111=TOMACSAMA\u000117=174\u0001150=0\u000139=0\u000155=OMXS300K\u000148=21758906\u000122=M\u000138=10.0000000\u000140=2\u000144=1086.7500000\u000159=0\u0001151=10.0000000\u000114=0\u00016=0\u000160=20101007-16:07:22.816\u000110=236\u0001" // \u000154=1
 		};
 		
 		for (String s : fixMessages) {
 
 			ByteBuffer buf = ByteBuffer.wrap(s.getBytes());
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertTrue(err.hasError());
			assertEquals(err.sessionRejectReason, (int)FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING);
 
 		}
 	}
 
 	@Test
 	public void testParseValidOrderRejectFixOutMessages() {
 		
 		String[] fixMessages = { 
 				"8=FIXT.1.1\u00019=0187\u000135=9\u000134=388\u000152=20110211-07:00:08\u000149=TOMAC\u000150=S\u000156=AAL\u000157=AOR001\u000111=2011003345605\u000137=NONE\u000139=8\u000141=2011003345603\u000158=0203 Unknown Original Client Order ID (OrigClOrdID) \u0001102=1\u0001109=AAL\u0001434=1\u000110=170\u0001",
 		};
 		
 		for (String s : fixMessages) {
 
 			ByteBuffer buf = ByteBuffer.wrap(s.getBytes());
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 			
 			outMsg = (FixMessage) inMsg.clone();
 
 			if (outMsg instanceof FixTestRequest) 
 				assertTrue(((FixTestRequest)outMsg).toString().length() > 0);
 			if (outMsg instanceof FixLogon) 
 				assertTrue(((FixLogon)outMsg).toString().length() > 0);
 
 			byte[] bytes = new byte[1024 * 8];
 			FixUtils.fillSpace(bytes);
 			ByteBuffer out = ByteBuffer.wrap(bytes);
 			outMsg.encode(out);
 			// ?
 			assertFalse(err.hasError());
 			
 			out.position(0);
 			inMsg = pool.getFixMessage(out, err);
 
 			assertNotNull(inMsg);
 			assertFalse(err.hasError());
 
 		}
 	}
 	
 	@Test
 	public void testParseUnknownTagFixInMessages() {
 		
 		FixMessageParser parser = new FixMessageParser();
 		
 		String[] fixMessages = { 
 			"8=FIXT.1.1\u00019=0106\u000135=A\u000149=MSI\u000156=TOMAC\u00011137=9\u000134=1\u000152=20101007-06:00:07.732\u000150=STOM1\u000198=0\u0001108=60\u0001141=N\u0001553=MSITS1\u0001554=MWAT\u000110=241\u0001",
 			"8=FIXT.1.1\u00019=0061\u000135=0\u000149=MSI\u000156=TOMAC\u000134=2\u000152=20101007-06:01:07.836\u000150=STOM1\u000110=237\u0001",
 			"8=FIXT.1.1\u00019=0154\u000135=D\u000149=MSI\u000156=TOMAC\u000134=230\u000152=20101007-15:43:36.290\u000150=STOM1\u000111=GEDVOO81\u000154=1\u000140=2\u000159=0\u000155=OMXS300K\u000138=15\u000144=1090\u000160=20101007-15:43:36.242\u000158=Limit,Stock,Day,Buy.\u0001432=20101007\u000110=056\u0001"
 			
 		};
 		// 35=D contains unknown tag Text(58)
 		
 		for (String s : fixMessages) {
 
 			ByteBuffer buf = ByteBuffer.wrap(s.getBytes());
 			
 			int startPos = buf.position();
 			
 			inMsg = pool.getFixMessage(buf, err);
 
 			assertNotNull(inMsg);
 			assertFalse(inMsg.getLastFixValidationError().hasError());
 
 			buf.position(startPos);
 			parser.parse(buf, err, listener);
 			assertFalse(err.hasError());
 
 		}
 	}
 
 	class TestFixMessageListener extends FixMessageListenerImpl {
 
 		@Override
 		public void onFixLogon(FixLogon m) {
 			super.onFixLogon(m);
 			//assertTrue(m.equals(inMsg));
 		}
 	}
 }
