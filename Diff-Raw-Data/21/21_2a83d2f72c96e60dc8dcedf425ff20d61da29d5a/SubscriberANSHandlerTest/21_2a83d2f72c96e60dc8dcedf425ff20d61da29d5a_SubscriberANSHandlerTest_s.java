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
import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.security.MessageDigest;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import mockit.Expectations;
 import mockit.Mock;
 import mockit.MockUp;
 import mockit.NonStrictExpectations;
 
import javax.xml.soap.MimeHeader;

 import org.beepcore.beep.core.AbortChannelException;
 import org.beepcore.beep.core.InputDataStream;
 import org.beepcore.beep.core.InputDataStreamAdapter;
 import org.beepcore.beep.core.Message;
 import org.junit.Test;
 
 import com.tresys.jalop.jnl.DigestStatus;
 import com.tresys.jalop.jnl.IncompleteRecordException;
 import com.tresys.jalop.jnl.RecordInfo;
 import com.tresys.jalop.jnl.SubscribeRequest;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.SubscriberSession;
 import com.tresys.jalop.jnl.impl.subscriber.SubscriberSessionImpl;
 
 /**
  * Tests for SubscriberANSHandler class.
  */
 public class SubscriberANSHandlerTest {
 
 	@Test
 	public void testSubscriberANSHandlerWorks(MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds) throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 	}
 
 	public class FakeGoodSubscriber implements Subscriber {
 		@Override
 		public boolean notifySysMetadata(SubscriberSession sess,
 				final RecordInfo recordInfo, InputStream sysMetaData) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyAppMetadata(SubscriberSession sess,
 				final RecordInfo recordInfo, InputStream appMetaData) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyPayload(SubscriberSession sess,
 				final RecordInfo recordInfo, InputStream payload) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyDigest(SubscriberSession sess,
 				final RecordInfo recordInfo, final byte[] digest) {
 			return true;
 		}
 
 		@Override
 		public boolean notifyDigestResponse(SubscriberSession sess,
 				final Map<String, DigestStatus> statuses) {
 			return false;
 
 		}
 
 		@Override
 		public SubscribeRequest getSubscribeRequest(SubscriberSession sess) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	@Test
 	public void testReceiveANSLogRecordWorks(MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		sh.receiveANS(msg);
 	}
 
 	@Test
 	public void testReceiveANSLogRecordWorksNoPayload(MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
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
 			// int numTimes = 0;
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 				// dsa.read();
 				// result = 0;
 				dsa.read(new byte[5], 0, 5);
 				dsa.read();
 				result = -1;
 			}
 		};
 
 		sh.receiveANS(msg);
 	}
 
 	@Test
 	public void testReceiveANSAuditRecordWorks(MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		sh.receiveANS(msg);
 	}
 
 	@Test
 	public void testReceiveANSJournalRecordWorks(MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		sh.receiveANS(msg);
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveANSLogRecordThrowsErrorWithDataAfterLastBreak(
 			MessageDigest md, final SubscriberSessionImpl subsess,
 			final InputDataStream ds, final Message msg,
 			final InputDataStreamAdapter dsa) throws Exception {
 
 		final FakeGoodSubscriber sub = new FakeGoodSubscriber();
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		new MockUp<InputDataStreamAdapter>() {
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		sh.receiveANS(msg);
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveANSThrowsExceptionOnUnknownType(MessageDigest md,
 			final SubscriberSessionImpl subsess, final InputDataStream ds,
 			final Message msg, final InputDataStreamAdapter dsa)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
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
 
 		sh.receiveANS(msg);
 	}
 
 	@Test(expected = IncompleteRecordException.class)
 	public void testGetRecordDigestThrowsIncompleteRecordExceptionWhenPayloadNotComplete(
 			MessageDigest md, SubscriberSessionImpl subsess, InputDataStream ds)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		sh.getRecordDigest();
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveRPYThrowsException(MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds, Message msg)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		sh.receiveRPY(msg);
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveERRThrowsException(MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds, Message msg)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		sh.receiveERR(msg);
 	}
 
 	@Test(expected = AbortChannelException.class)
 	public void testReceiveNULThrowsException(MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds, Message msg)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		sh.receiveNUL(msg);
 	}
 
 	@Test
 	public void testJalopDataStreamWorks(MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds) throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(1234);
 		assertNotNull(jds);
 	}
 
 	@Test
 	public void testJalopDataStreamReadWorks(MessageDigest md,
 			SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
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
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertTrue(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test
 	public void testJalopDataStreamReadReturnsNegativeWhenFinished(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new Expectations() {
 			byte b[] = new byte[5];
 			{
 				ds.getInputStream();
 				result = is;
 			}
 		};
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 
 		frf.setAccessible(true);
 		frf.setBoolean(jds, true);
 
 		int ret = jds.read(new byte[5], 0, 5);
 		assertEquals(-1, ret);
 
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadPayloadNotCorrectWhenBREA(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREA".getBytes("utf-8");
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
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
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertFalse(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadThrowsIOExceptionUponFailure(
 			MessageDigest md, SubscriberSessionImpl subsess,
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 		jds.read();
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorks(MessageDigest md,
 			SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 
 		byte b[] = new byte[5];
 		int read = jds.read(b, 0, 5);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertTrue(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorksLengthLargerThanPayload(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 
 		byte b[] = new byte[5];
 		int read = jds.read(b, 0, 10);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertTrue(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadByteArrayPayloadIncorrectWhenRcvBREA(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 
 		byte b[] = new byte[5];
 		int read = jds.read(b, 0, 5);
 		assertEquals(5, read);
 		assertEquals("12345", new String(b, "utf-8"));
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertFalse(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test(expected = IOException.class)
 	public void testJalopDataStreamReadByteArrayThrowsIOExceptionUponFailure(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read(byte[] b, int off, int len) {
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 		jds.read(new byte[5], 0, 5);
 	}
 
 	@Test
 	public void testJalopDataStreamReadByteArrayOffsetWorksWithDataSizeLargerThanBuffer(
 			MessageDigest md, SubscriberSessionImpl subsess,
 			final InputDataStream ds, final InputDataStreamAdapter is)
 			throws Exception {
 		new MockUp<InputDataStreamAdapter>() {
 			int count = 0;
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
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
 
				System.out.println(len);
				System.out.println("count: " + count);
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		InputStream jds = sh.getJalopDataStreamInstance(6);
 
 		byte b[] = new byte[3];
 		int read = jds.read(b, 0, 3);
 		assertEquals(3, read);
 		assertEquals("123", new String(b, "utf-8"));
 
 		byte b2[] = new byte[3];
 		read = jds.read(b2, 0, 3);
 		assertEquals(3, read);
 		assertEquals("456", new String(b2, "utf-8"));
 
 		Field pcf = sh.getClass().getDeclaredField("payloadCorrect");
 		pcf.setAccessible(true);
 		boolean pc = ((Boolean) pcf.get(sh)).booleanValue();
 		assertTrue(pc);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 	}
 
 	@Test
 	public void testJalopDataStreamFlushWorks(MessageDigest md,
 			SubscriberSessionImpl subsess, final InputDataStream ds,
 			final InputDataStreamAdapter is) throws Exception {
 
 		new MockUp<InputDataStreamAdapter>() {
 			@Mock
 			int read() {
 				return 1;
 			}
 
 			@Mock
 			int read(byte[] b, int off, int len) throws Exception {
 				byte[] b2 = "BREAK".getBytes("utf-8");
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		InputStream jds = sh.getJalopDataStreamInstance(5);
 
 		Method flush = jds.getClass().getDeclaredMethod("flush");
 		flush.invoke(jds);
 
 		Field frf = jds.getClass().getDeclaredField("finishedReading");
 		frf.setAccessible(true);
 		boolean fr = ((Boolean) frf.get(jds)).booleanValue();
 		assertTrue(fr);
 		flush.invoke(jds);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testJalopDataStreamThrowsExceptionWithDataSizeLessThanZero(
 			MessageDigest md, SubscriberSessionImpl subsess, InputDataStream ds)
 			throws Exception {
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		sh.getJalopDataStreamInstance(-1);
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorks(final InputDataStreamAdapter dsa,
 			MessageDigest md, SubscriberSessionImpl subsess,
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		List<MimeHeader> mhl = sh.getAdditionalHeaders();
 		assertNotNull(mhl);
 		assertEquals(1, mhl.size());
 
 		MimeHeader mh = mhl.get(0);
 		assertEquals("asdf", mh.getName());
 		assertEquals("hello", mh.getValue());
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorksWithBlankValue(
 			final InputDataStreamAdapter dsa, MessageDigest md,
 			SubscriberSessionImpl subsess, final InputDataStream ds)
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		List<MimeHeader> mhl = sh.getAdditionalHeaders();
 		assertNotNull(mhl);
 		MimeHeader mh = mhl.get(0);
 		assertEquals("asdf", mh.getName());
 		assertEquals("", mh.getValue());
 	}
 
 	@Test
 	public void testGetAdditionalHeadersWorksWithBlankName(
 			final InputDataStreamAdapter dsa, MessageDigest md,
 			SubscriberSessionImpl subsess, final InputDataStream ds)
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
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 		List<MimeHeader> mhl = sh.getAdditionalHeaders();
 		assertNotNull(mhl);
 		MimeHeader mh = mhl.get(0);
 		assertEquals("", mh.getName());
 		assertEquals("hello", mh.getValue());
 	}
 
 	@Test
 	public void testGetRecordDigestWorks(final MessageDigest md,
 			SubscriberSessionImpl subsess, InputDataStream ds) throws Exception {
 
 		new Expectations() {
 			byte[] b = "DIGEST".getBytes();
 			{
 				md.digest();
 				result = b;
 			}
 		};
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		Field payloadComplete = sh.getClass().getDeclaredField(
 				"payloadComplete");
 		payloadComplete.setAccessible(true);
 		payloadComplete.setBoolean(sh, true);
 
 		Field payloadCorrect = sh.getClass().getDeclaredField("payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		payloadCorrect.setBoolean(sh, true);
 
 		byte[] digest = sh.getRecordDigest();
 		assertEquals("DIGEST", new String(digest));
 	}
 
 	@Test(expected = IncompleteRecordException.class)
 	public void testGetRecordDigestThrowsExceptionWhenPayloadNotComplete(
 			MessageDigest md, SubscriberSessionImpl subsess, InputDataStream ds)
 			throws Exception {
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		Field payloadComplete = sh.getClass().getDeclaredField(
 				"payloadComplete");
 		payloadComplete.setAccessible(true);
 		payloadComplete.setBoolean(sh, false);
 
 		sh.getRecordDigest();
 	}
 
 	@Test(expected = IncompleteRecordException.class)
 	public void testGetRecordDigestThrowsExceptionWhenPayloadNotCorrect(
 			MessageDigest md, SubscriberSessionImpl subsess, InputDataStream ds)
 			throws Exception {
 
 		SubscriberANSHandler sh = new SubscriberANSHandler(md, subsess, ds);
 		assertNotNull(sh);
 
 		Field payloadComplete = sh.getClass().getDeclaredField(
 				"payloadComplete");
 		payloadComplete.setAccessible(true);
 		payloadComplete.setBoolean(sh, true);
 
 		Field payloadCorrect = sh.getClass().getDeclaredField("payloadCorrect");
 		payloadCorrect.setAccessible(true);
 		payloadCorrect.setBoolean(sh, false);
 
 		sh.getRecordDigest();
 	}
 }
