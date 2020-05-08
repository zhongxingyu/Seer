 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core.data;
 
 import java.nio.ByteBuffer;
 import java.security.NoSuchAlgorithmException;
 
 import me.footlights.core.crypto.Fingerprint;
 import me.footlights.core.crypto.SecretKey;
 import me.footlights.core.data.FormatException;
 import me.footlights.core.data.Link;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 
 /**
  * Unit tests for {@link Link}.
  * @author Jonathan Anderson (jon@footlights.me)
  */
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Fingerprint.class)
 public class LinkTest
 {
 	@Before public void setUp() throws NoSuchAlgorithmException
 	{
 		fingerprint = mock(Fingerprint.class);
 		when(fingerprint.encode()).thenReturn(FAKE_ID);
 
 		PowerMockito.mockStatic(Fingerprint.class);
 		when(Fingerprint.newBuilder()).thenCallRealMethod();
 		when(Fingerprint.decode(Mockito.eq(FAKE_ID))).thenReturn(fingerprint);
 
 		key = SecretKey.newGenerator().setAlgorithm(ALGORITHM).setBytes(KEY_BYTES).generate();
 	}
 
 	/** Ensure that the simplest legal {@link Link} can be built. */
 	@Test public void buildTrivialLink() throws FormatException, NoSuchAlgorithmException
 	{
 		Link.newBuilder().setFingerprint(fingerprint).setKey(key).build();
 	}
 
 	/** Try to build a simpler-than-is-allowed {@link Link}. */
 	@Test public void buildDegenerateLink()
 	{
 		try
 		{
 			Link.newBuilder().build();
 			fail("URI-less Link should not build");
 		}
		catch (IllegalArgumentException e) {}
 	}
 
 	/** Test parsing a very simple {@link Link}. */
 	@Test public void parseSimpleLink() throws Throwable
 	{
 		// 256b key (32B)
 		final byte[] key =
 		{
 				1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8,
 				1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8,
 		};
 
 		final byte[] bytes = new byte[]
 		{
 			'L', 'I', 'N', 'K', '\r', '\n',       // magic
 			0x27, 0x00,                           // link length: 39 B
 			0x07, 0x00,                           // fingerprint: 7 bytes
 			0, 0,                                 // algorithm unspecified
 			0x00, 0x01,                           // key length: 256b (0x0100)
 			's', 'o', 'm', 'e', '_', 'I', 'D',    // ID: "some_ID"
 		};
 		
 		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + key.length)
 			.put(bytes)
 			.put(key);
 		buffer.position(0);    // reset to beginning
 
 		Link l = Link.newBuilder()
 			.parse(buffer)
 			.build();
 
 		PowerMockito.verifyStatic();
 
 		assertEquals(fingerprint, l.fingerprint());
 		assertEquals("", l.key().getAlgorithm());
 	}
 
 	public static Fingerprint foo(String p)
 	{
 		throw new RuntimeException("foo: '" + p + "'");
 	}
 
 	/** Test parsing a {@link Link} with fewer bytes than advertised. */
 	@Test public void parseTooSmall() throws Throwable
 	{
 		final byte[] bytes = new byte[]
 	   {
 			'L', 'I', 'N', 'K', '\r', '\n',       // magic
 			0x27, 0x00,                           // length: 39 (0x0027)
 			0, 0,                                 // algorithm unspecified
 			0x07, 0x00,                           // ID length: 7
 			0x00, 0x01,                           // key length: 256b (0x0100)
 			0, 0,                                 // IV unspecified
 			's', 'o', 'm', 'e', '_', 'I', 'D',    // ID: "some ID"
 		};
 
 		try
 		{
 			Link.newBuilder().parse(ByteBuffer.wrap(bytes)).build();
 			fail("Parsing a too-small ByteBuffer should fail");
 		}
 		catch (FormatException e) { /* expected exception */ }
 	}
 
 	/** Test {@link Link#getBytes()}. */
 	@Test public void exportLink() throws FormatException, NoSuchAlgorithmException
 	{
 		final short EXPECTED_CONTENT_LENGTH = (short)
 				(FAKE_ID.length() + ALGORITHM.length() + KEY_BYTES.length);
 
 		Link l = Link.newBuilder()
 			.setFingerprint(fingerprint)
 			.setKey(key)
 			.build();
 
 		ByteBuffer buffer = l.getBytes();
 		assertEquals(EXPECTED_CONTENT_LENGTH + Link.minimumLength(), buffer.remaining());
 
 		byte[] magic = new byte[LINK_MAGIC.length];
 		buffer.get(magic);
 		assertArrayEquals(LINK_MAGIC, magic);
 
 		assertEquals(EXPECTED_CONTENT_LENGTH, buffer.getShort());
 		assertEquals(FAKE_ID.length(),        buffer.getShort());
 		assertEquals(ALGORITHM.length(),      buffer.getShort());
 		assertEquals(8 * KEY_BYTES.length,    buffer.getShort());
 
 		byte[] fingerprint = new byte[FAKE_ID.length()];
 		buffer.get(fingerprint);
 		assertArrayEquals(FAKE_ID.getBytes(), fingerprint);
 
 		byte[] algorithm = new byte[ALGORITHM.length()];
 		buffer.get(algorithm);
 		assertArrayEquals(ALGORITHM.getBytes(), algorithm);
 
 		byte[] key = new byte[KEY_BYTES.length];
 		buffer.get(key);
 		assertArrayEquals(KEY_BYTES, key);
 	}
 
 	@Test public void serializeAndDeserialize() throws FormatException, NoSuchAlgorithmException
 	{
 		Link original = Link.newBuilder()
 			.setFingerprint(fingerprint)
 			.setKey(key)
 			.build();
 
 		Link copy = Link.parse(original.getBytes());
 		assertEquals(original, copy);
 	}
 
 	private static final byte[] LINK_MAGIC = { 'L', 'I', 'N', 'K', '\r', '\n' };
 	private static final String FAKE_ID = "some_ID";
 	private static final String ALGORITHM = "FOO encryption algorithm";
 	private static final byte[] KEY_BYTES = { 1, 2, 3, 4, 42, 79 };
 
 	private Fingerprint fingerprint;
 	private SecretKey key;
 }
