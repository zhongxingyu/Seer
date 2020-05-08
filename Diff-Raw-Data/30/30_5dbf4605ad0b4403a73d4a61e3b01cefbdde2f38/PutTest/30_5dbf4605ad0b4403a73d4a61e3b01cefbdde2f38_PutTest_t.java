 /**
  * Copyright (c) 2012 Mark S. Kolich
  * http://mark.koli.ch
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.kolich.havalo.client.api;
 
 import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
 import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;
 import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
 import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.ETAG;
 import static org.apache.http.HttpHeaders.IF_MATCH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
 import static org.apache.http.HttpStatus.SC_CONFLICT;
 import static org.apache.http.HttpStatus.SC_NO_CONTENT;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayOutputStream;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.message.BasicHeader;
 import org.junit.Test;
 
 import com.kolich.common.either.Either;
 import com.kolich.havalo.client.HavaloClientTestCase;
 import com.kolich.havalo.client.entities.FileObject;
 import com.kolich.http.common.response.HttpFailure;
 
 public class PutTest extends HavaloClientTestCase {
 	
 	private static final String SAMPLE_JSON_OBJECT =
 		"{\"dog\":\"cat\",\"integer\":2}";
 	
 	public PutTest() throws Exception {
 		super();
 	}
 	
 	@Test
 	public void put() throws Exception {
 		// PUT a sample object
 		final Either<HttpFailure,FileObject> put = 
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 				"test", "jsonObject/hmm");
 		assertTrue("Failed to PUT sample object.", put.success());
 		// GET the object to make sure it actually worked.
 		final ByteArrayOutputStream os = new ByteArrayOutputStream();
 		final Either<HttpFailure,List<Header>> get =
 			client_.getObject(os, "test", "jsonObject/hmm");
 		assertTrue("Failed to GET sample object after PUT.", get.success());
 		assertTrue("Failed to GET sample object after PUT -- no headers!?",
 			get.right().size() > 0L);
 		final String receivedString = newStringUtf8(os.toByteArray());
 		assertTrue("Object received on GET did not match object " +
 			"sent on PUT (sent="+ SAMPLE_JSON_OBJECT + ", received=" +
 				receivedString + ")", receivedString.equals(SAMPLE_JSON_OBJECT));
 		// Tear down
 		final Either<HttpFailure,Integer> delete = 
 			client_.deleteObject("test", "jsonObject/hmm");		
 		assertTrue("Failed to DELETE sample object.", delete.success());
 	}
 	
 	@Test
 	public void putLongName() throws Exception {
 		// Trying to stay under the default Tomcat maxHttpHeaderSize of 8KB
 		// http://stackoverflow.com/questions/6837505/setting-max-http-header-size-with-ajp-tomcat-6-0
 		// http://stackoverflow.com/questions/1730158/how-to-set-the-ajp-packet-size-in-tomcat
 		// Apparently the max header size in Jetty is less around 4KB
 		final String[] prefixes = getRandomPrefix(200);
 		// PUT a sample object
 		final Either<HttpFailure,FileObject> put = 
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 					prefixes);
 		assertTrue("Failed to PUT long sample object.", put.success());
 		// Tear down
 		final Either<HttpFailure,Integer> delete =
 			client_.deleteObject(prefixes);
 		assertTrue("Failed to DELETE long sample object.", delete.success());
 		assertTrue("Failed to DELETE long sample object -- bad response code",
 			delete.right() == SC_NO_CONTENT);
 	}
 	
 	@Test
 	public void putWithSpacesInName() throws Exception {
 		final String[] prefixes = new String[]{"   ////// ", "kewl",
 			"  +++    !!//  should still totally work  ++++ ^^^^ && foobar"}; 
 		// PUT a sample object
 		final Either<HttpFailure,FileObject> put = 
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 					prefixes);
 		assertTrue("Failed to PUT object with crap in name.", put.success());
 		// Tear down
 		final Either<HttpFailure,Integer> delete =
 			client_.deleteObject(prefixes);
 		assertTrue("Failed to DELETE object with crap in name.",
 			delete.success());
 		assertTrue("Failed to DELETE object with crap in name -- bad " +
 			"response code", delete.right() == SC_NO_CONTENT);
 	}
 	
 	@Test
 	public void putNoName() throws Exception {
 		// PUT a sample object
 		final Either<HttpFailure,FileObject> put = 
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 				"");
 		assertFalse("Failed to PUT object with no name.", put.success());
		// Expected 400 Bad Request
 		assertTrue("Failed to PUT object with no name -- bad " +
			"response code", put.left().getStatusCode() == SC_BAD_REQUEST);
 	}
 	
 	@Test
 	public void putEmptyObject() throws Exception {
 		// PUT a sample "empty" object (an object with a length of zero)
 		final Either<HttpFailure,FileObject> put = 
 			client_.putObject(new byte[]{}, "empty.json");
 		assertTrue("Failed to PUT empty object of zero length.", put.success());
 		// Call HEAD on the object to fetch and validate its meta data
 		final Either<HttpFailure,List<Header>> head =
 			client_.getObjectMetaData("empty.json");
 		// Validate that the Content-Length exists and is zero
		final String length = getHeader(head.right(), CONTENT_LENGTH);
 		assertNotNull("Content-Length on empty object was null.", length);
 		assertTrue("Content-Length on empty object was not zero.",
 			Long.parseLong(length) == 0L);
 		// Tear down
 		final Either<HttpFailure,Integer> delete =
 			client_.deleteObject("empty.json");
 		assertTrue("Failed to DELETE empty object.", delete.success());
 		assertTrue("Failed to DELETE empty object -- bad " +
 			"response code", delete.right() == SC_NO_CONTENT);
 	}
 	
 	@Test
 	public void putWithCorrectETag() throws Exception {
 		// PUT a sample object
 		final String[] prefixes = getRandomPrefix(50);
 		final Either<HttpFailure,FileObject> put =
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 					prefixes);
 		assertTrue("Failed to PUT sample object.", put.success());
 		// Extract the eTag from the resulting PUT
		final String eTag = put.right().getFirstHeader(ETAG);
 		assertNotNull("ETag header on PUT was null.", eTag);
 		// Call HEAD on the object to fetch and validate its meta data.
 		final Either<HttpFailure,List<Header>> head =
 			client_.getObjectMetaData(prefixes);
 		assertTrue("Failed to HEAD sample object.", head.success());
 		// Validate that the eTag we sent in was returned with the response
 		// meta data on the HEAD request to the API.
 		assertTrue("HEAD Fetched Etag does not match ETag delivered on PUT",
			eTag.equals(getHeader(head.right(), ETAG)));
 		// Do another put with the correct If-Match ETag.
 		final Either<HttpFailure,FileObject> putETag =
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json"),
 					// Object in repo must match incoming ETag
 					new BasicHeader(IF_MATCH, eTag)},
 					prefixes);
 		assertTrue("Failed to PUT sample object with correct eTag.",
 			putETag.success());
 		final Either<HttpFailure,Integer> delete =
 			client_.deleteObject(prefixes);
 		assertTrue("Failed to DELETE object with crap in name.",
 			delete.success());
 		assertTrue("Failed to DELETE object with crap in name -- bad " +
 			"response code", delete.right() == SC_NO_CONTENT);
 	}
 	
 	@Test
 	public void putWithConflictingETag() throws Exception {
 		// PUT a sample object
 		final String[] prefixes = getRandomPrefix(50);
 		final Either<HttpFailure,FileObject> put =
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
 					prefixes);
 		assertTrue("Failed to PUT sample object.", put.success());
 		// Extract the eTag from the resulting PUT
		final String eTag = put.right().getFirstHeader(ETAG);
 		assertNotNull("ETag header on PUT was null.", eTag);
 		// Call HEAD on the object to fetch and validate its meta data.
 		final Either<HttpFailure,List<Header>> head =
 			client_.getObjectMetaData(prefixes);
 		assertTrue("Failed to HEAD sample object.", head.success());
 		// Validate that the eTag we sent in was returned with the response
 		// meta data on the HEAD request to the API.
 		assertTrue("HEAD Fetched Etag does not match ETag delivered on PUT",
			eTag.equals(getHeader(head.right(), ETAG)));
 		// Do another put with a totally wrong ETag in the If-Match
 		// request header (SHOULD FAIL).
 		final Either<HttpFailure,FileObject> putETag =
 			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
 				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json"),
 					// Totally non-sense ETag -- should fail.
 					new BasicHeader(IF_MATCH, "totallybogusetag")},
 					prefixes);
 		assertFalse("PUT with known bad ETag succeeded?", putETag.success());
 		assertTrue("PUT with known bad ETag did not return a 409 " +
 			"Conflict as expected.", putETag.left().getStatusCode() == SC_CONFLICT);
 		// Tear down
 		final Either<HttpFailure,Integer> delete =
 			client_.deleteObject(prefixes);
 		assertTrue("Failed to DELETE object with crap in name.",
 			delete.success());
 		assertTrue("Failed to DELETE object with crap in name -- bad " +
 			"response code", delete.right() == SC_NO_CONTENT);
 	}
 
 	private static final String[] getRandomPrefix(final int length) {
 		final String[] prefixes = new String[length];
 		for(int i = 0, l = prefixes.length; i < l; i++) {
 			prefixes[i] = randomAscii(10);
 		}
 		return prefixes;
 	}
 	
 	private static final String getHeader(final List<Header> headers,
 		final String headerName) {
 		for(final Header h : headers) {
 			if(h.getName().equals(headerName)) {
 				return h.getValue();
 			}
 		}
 		return null;
 	}
 	
 }
