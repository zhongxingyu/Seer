 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tresys.jalop.jnl.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.security.MessageDigest;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.soap.MimeHeader;
 
 import mockit.Deencapsulation;
 import mockit.Expectations;
 import mockit.Mock;
 import mockit.MockUp;
 import mockit.NonStrictExpectations;
 import mockit.Verifications;
import mockit.VerificationsInOrder;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.beepcore.beep.core.AbortChannelException;
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.Channel;
 import org.beepcore.beep.core.InputDataStream;
 import org.beepcore.beep.core.InputDataStreamAdapter;
 import org.beepcore.beep.core.Message;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.tresys.jalop.jnl.DigestStatus;
 import com.tresys.jalop.jnl.IncompleteRecordException;
 import com.tresys.jalop.jnl.RecordInfo;
 import com.tresys.jalop.jnl.SubscribeRequest;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.SubscriberSession;
 import com.tresys.jalop.jnl.impl.SubscriberANSHandler.Dispatcher;
 import com.tresys.jalop.jnl.impl.subscriber.SubscriberSessionImpl;
 
 /**
  * Tests for SubscriberANSHandler class.
  */
 public class SubscriberANSHandlerTest {
 
 	@Before
 	public void setUp() {
 		// Disable logging so the build doesn't get spammed.
 		Logger.getRootLogger().setLevel(Level.OFF);
 	}
 
 	@Test
 	public void testSubscriberANSHandlerWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess) throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 	}
 
 	public class FakeGoodSubscriber implements Subscriber {
 		@Override
 		public boolean notifySysMetadata(final SubscriberSession sess,
 				final RecordInfo recordInfo, final InputStream sysMetaData) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyAppMetadata(final SubscriberSession sess,
 				final RecordInfo recordInfo, final InputStream appMetaData) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyPayload(final SubscriberSession sess,
 				final RecordInfo recordInfo, final InputStream payload) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyDigest(final SubscriberSession sess,
 				final RecordInfo recordInfo, final byte[] digest) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyDigestResponse(final SubscriberSession sess,
 				final Map<String, DigestStatus> statuses) {
 			return false;
 
 		}
 
 		@Override
 		public SubscribeRequest getSubscribeRequest(final SubscriberSession sess) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	@Test
 	public void testReceiveANSWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final Message msg)
 			throws Exception {
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		// mock up thread since this function is supposed to spawn a new thread, but
         // don't actually want it to do that.
         new MockUp<Thread>() {
             @Mock
             public void start() {
                 // do nothing
             }
         };
 
 		sh.receiveANS(msg);
 	}
 
 	@Test
 	public void testDispatcherRunLogWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Message");
 				result = "log-record";
 				dsa.getHeaderValue("JAL-Log-Length");
 				result = "1";
 				subsess.getSubscriber();
 				result = sub;
 				dsa.getHeaderValue("JAL-Serial-Id");
 				result = "1";
 				ds.getInputStream();
 				result = dsa;
 			}
 		};
 
 		new Expectations() {
 			{
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = -1;
 			}
 		};
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 	}
 
 	@Test
 	public void testDispatcherRunLogRecordWorksNoPayload(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Message");
 				result = "log-record";
 				dsa.getHeaderValue("JAL-Log-Length");
 				result = "0";
 				subsess.getSubscriber();
 				result = sub;
 				dsa.getHeaderValue("JAL-Serial-Id");
 				result = "1";
 				ds.getInputStream();
 				result = dsa;
 			}
 		};
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 
 		};
 
 		new Expectations() {
 			{
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = -1;
 			}
 		};
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 	}
 
 	@Test
 	public void testDispatcherRunAuditRecordWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Message");
 				result = "audit-record";
 				dsa.getHeaderValue("JAL-Audit-Length");
 				result = "1";
 				subsess.getSubscriber();
 				result = sub;
 				dsa.getHeaderValue("JAL-Serial-Id");
 				result = "1";
 				ds.getInputStream();
 				result = dsa;
 			}
 		};
 
 		new Expectations() {
 			{
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = -1;
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 	}
 
 	@Test
 	public void testDispatcherRunJournalRecordWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Message");
 				result = "journal-record";
 				dsa.getHeaderValue("JAL-Journal-Length");
 				result = "1";
 				subsess.getSubscriber();
 				result = sub;
 				dsa.getHeaderValue("JAL-Serial-Id");
 				result = "1";
 				ds.getInputStream();
 				result = dsa;
 			}
 		};
 
 		new Expectations() {
 			{
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = -1;
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 	}
 
 	@Test
 	public void testDispatcherRunLogRecordThrowsErrorWithDataAfterLastBreak(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final Message msg,
 			final InputDataStreamAdapter dsa) throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1";
 				dsa.getHeaderValue("JAL-Message");
 				result = "log-record";
 				dsa.getHeaderValue("JAL-Log-Length");
 				result = "1";
 				subsess.getSubscriber();
 				result = sub;
 				dsa.getHeaderValue("JAL-Serial-Id");
 				result = "1";
 				ds.getInputStream();
 				result = dsa;
 			}
 		};
 
 		new Expectations() {
 			{
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = 1;
 			}
 		};
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 
 		new Verifications() {
 			{
 				new IOException(anyString);
 			}
 		};
 	}
 
 	@Test
 	public void testDispatcherRunThrowsExceptionOnUnknownType(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream();
 				result = ds;
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderValue("JAL-System-Metadata-Length");
 				result = "1234";
 				dsa.getHeaderValue("JAL-Application-Metadata-Length");
 				result = "1234";
 				dsa.getHeaderValue("JAL-Message");
 				result = "fake-record";
 			}
 		};
 
 
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).run();
 
 		new Verifications() {
 			{
 				new AbortChannelException(anyString);
 			}
 		};
 	}
 
 	@Test(expected = IncompleteRecordException.class)
 	public void testGetRecordDigestThrowsExceptionWhenPayloadNotComplete(
 			final MessageDigest md, final SubscriberSessionImpl subsess, final InputDataStream ds)
 			throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).getRecordDigest(md);
 	}
 
 	@Test(expected = IncompleteRecordException.class)
 	public void testGetRecordDigestThrowsExceptionWhenPayloadNotCorrect(
 			final MessageDigest md, final SubscriberSessionImpl subsess, final InputDataStream ds)
 			throws Exception {
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 
 		final Field payloadComplete = dispatcher.getClass().getDeclaredField(
 				"payloadComplete");
 		payloadComplete.setAccessible(true);
 		payloadComplete.setBoolean(dispatcher, true);
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		payloadCorrect.setBoolean(dispatcher, false);
 
 		((Dispatcher) dispatcher).getRecordDigest(md);
 	}
 
 	@Test
 	public void testGetRecordDigestWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds) throws Exception {
 
 		new Expectations() {
 			byte[] b = "DIGEST".getBytes();
 			{
 				md.digest();
 				result = b;
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 
 		final Field payloadComplete = dispatcher.getClass().getDeclaredField(
 				"payloadComplete");
 		payloadComplete.setAccessible(true);
 		payloadComplete.setBoolean(dispatcher, true);
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		payloadCorrect.setBoolean(dispatcher, true);
 
 		final byte[] digest = ((Dispatcher) dispatcher).getRecordDigest(md);
 		assertEquals("DIGEST", new String(digest));
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveRPYThrowsException(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final Message msg)
 			throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 		sh.receiveRPY(msg);
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveERRThrowsException(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final Message msg)
 			throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 		sh.receiveERR(msg);
 	}
 
 	@Test
	public void testReceiveNULWorks(final MessageDigest md, final Channel channel,
 			final SubscriberSessionImpl subsess, final Message msg)
 			throws BEEPException {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);

		new NonStrictExpectations() {
			{
				msg.getChannel(); result = channel;
			}
		};

 		sh.receiveNUL(msg);

		new VerificationsInOrder() {
			{
				channel.close();
			}
		};
 	}
 
 	@Test
 	public void testJalopDataStreamWorks(final MessageDigest md, final InputDataStream ds,
 			final SubscriberSessionImpl subsess) throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(1234, ds, md);
 		assertNotNull(jds);
 	}
 
 	@Test
 	public void testJalopDataStreamReadWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read();
 				is.read();
 				is.read();
 				is.read();
 				is.read();
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		int b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(-1, b);
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertTrue((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test
 	public void testJalopDataStreamReadReturnsNegativeWhenFinished(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		frf.setBoolean(jds, true);
 
 		final int ret = jds.read(new byte[5], 0, 5);
 		assertEquals(-1, ret);
 
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadPayloadNotCorrectWhenBREA(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREA".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read();
 				is.read();
 				is.read();
 				is.read();
 				is.read();
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 		int b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(1, b);
 		b = jds.read();
 		assertEquals(-1, b);
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertTrue((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadThrowsIOExceptionUponFailure(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return -1;
 			}
 
 		};
 
 		new Expectations() {
 			{
 				ds.getInputStream();
 				result = is;
 				is.read();
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 		jds.read();
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				byte[] b2 = null;
 
 				switch (count) {
 				case 0:
 					b2 = "12345".getBytes("utf-8");
 					break;
 				case 1:
 					b2 = "BREAK".getBytes("utf-8");
 					break;
 				}
 				count += 1;
 				if (count > 1)
 					count = 0;
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return b2.length;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read(b, 0, 5);
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		final byte b[] = new byte[5];
 		final int read = jds.read(b, 0, 5);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertTrue((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorksLengthLargerThanPayload(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				byte[] b2 = null;
 
 				switch (count) {
 				case 0:
 					b2 = "12345".getBytes("utf-8");
 					break;
 				case 1:
 					b2 = "BREAK".getBytes("utf-8");
 					break;
 				}
 				count += 1;
 				if (count > 1)
 					count = 0;
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return b2.length;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read(b, 0, 5);
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		final byte b[] = new byte[5];
 		final int read = jds.read(b, 0, 10);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertTrue((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadByteArrayPayloadIncorrectWhenRcvBREA(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				byte[] b2 = null;
 
 				switch (count) {
 				case 0:
 					b2 = "12345".getBytes("utf-8");
 					break;
 				case 1:
 					b2 = "BREA".getBytes("utf-8");
 					break;
 				}
 				count += 1;
 				if (count > 1)
 					count = 0;
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return b2.length;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read(b, 0, 5);
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		final byte b[] = new byte[5];
 		final int read = jds.read(b, 0, 5);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertFalse((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadByteArrayThrowsIOExceptionUponFailure(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read(final byte[] b, final int off, final int len) {
 				return 1;
 			}
 
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read(b, 0, b.length);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 		jds.read(new byte[5], 0, 5);
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorksWithDataSizeLargerThanBuffer(
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				byte[] b2 = null;
 
 				switch (count) {
 				case 0:
 					b2 = "123".getBytes("utf-8");
 					break;
 				case 1:
 					b2 = "456".getBytes("utf-8");
 					break;
 				case 2:
 					b2 = "BREAK".getBytes("utf-8");
 					break;
 				}
 				count += 1;
 				if (count > 2)
 					count = 0;
 
 				System.arraycopy(b2, 0, b, 0, b2.length);
 
 				return b2.length;
 			}
 		};
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 				is.read(b, 0, 3);
 				is.read(b, 0, 3);
 				is.read(b, 0, 5);
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(6, ds, md);
 
 		final byte b[] = new byte[3];
 		int read = jds.read(b, 0, 3);
 		assertEquals(3, read);
 		assertEquals("123", new String(b, "utf-8"));
 
 		final byte b2[] = new byte[3];
 		read = jds.read(b2, 0, 3);
 		assertEquals(3, read);
 		assertEquals("456", new String(b2, "utf-8"));
 
 		final Field payloadCorrect = dispatcher.getClass().getDeclaredField(
 				"payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		assertTrue((Boolean) payloadCorrect.get(dispatcher));
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 	}
 
 	@Test
 	public void testJalopDataStreamFlushWorks(final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(final byte[] b, final int off, final int len) throws Exception {
 				final byte[] b2 = "BREAK".getBytes("utf-8");
 				System.arraycopy(b2, 0, b, 0, b2.length);
 				return len;
 			}
 		};
 
 		new NonStrictExpectations() {
 			{
 				ds.getInputStream();
 				result = is;
 				is.read();
 				result = 1;
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		final InputStream jds = ((Dispatcher) dispatcher).getJalopDataStreamInstance(5, ds, md);
 
 		final Method flush = jds.getClass().getDeclaredMethod("flush");
 		flush.invoke(jds);
 
 		final Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		assertTrue((Boolean) frf.get(jds));
 		flush.invoke(jds);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testJalopDataStreamThrowsExceptionWithDataSizeLessThanZero(
 			final MessageDigest md, final SubscriberSessionImpl subsess, final InputDataStream ds)
 			throws Exception {
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		final Object dispatcher = Deencapsulation.newInnerInstance("Dispatcher", sh, ds, md);
 		((Dispatcher) dispatcher).getJalopDataStreamInstance(-1, ds, md);
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorks(final InputDataStreamAdapter dsa,
 			final MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds) throws Exception {
 
 		final Vector<String> v = new Vector<String>();
 		v.add("JAL-Application-Metadata-Length");
 		v.add("asdf");
 
 		new NonStrictExpectations() {
 			{
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderNames();
 				result = v.elements();
 				dsa.getHeaderValue((String) any);
 				result = "hello";
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 		final List<MimeHeader> mhl = sh.getAdditionalHeaders(ds);
 		assertNotNull(mhl);
 		assertEquals(1, mhl.size());
 
 		final MimeHeader mh = mhl.get(0);
 		assertEquals("asdf", mh.getName());
 		assertEquals("hello", mh.getValue());
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorksWithBlankValue(
 			final InputDataStreamAdapter dsa, final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds)
 			throws Exception {
 
 		final Vector<String> v = new Vector<String>();
 		v.add("asdf");
 
 		new NonStrictExpectations() {
 			{
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderNames();
 				result = v.elements();
 				dsa.getHeaderValue((String) any);
 				result = "";
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 		final List<MimeHeader> mhl = sh.getAdditionalHeaders(ds);
 		assertNotNull(mhl);
 		final MimeHeader mh = mhl.get(0);
 		assertEquals("asdf", mh.getName());
 		assertEquals("", mh.getValue());
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorksWithBlankName(
 			final InputDataStreamAdapter dsa, final MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds)
 			throws Exception {
 
 		final Vector<String> v = new Vector<String>();
 		v.add("");
 
 		new NonStrictExpectations() {
 			{
 				ds.getInputStream();
 				result = dsa;
 				dsa.getHeaderNames();
 				result = v.elements();
 				dsa.getHeaderValue((String) any);
 				result = "hello";
 			}
 		};
 
 		final SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess);
 		assertNotNull(sh);
 		final List<MimeHeader> mhl = sh.getAdditionalHeaders(ds);
 		assertNotNull(mhl);
 		final MimeHeader mh = mhl.get(0);
 		assertEquals("", mh.getName());
 		assertEquals("hello", mh.getValue());
 	}
 
 }
