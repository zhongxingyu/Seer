 package org.openmrs.module.shr.contenthandler.api;
 
 import static org.junit.Assert.*;
 import static com.github.tomakehurst.wiremock.client.WireMock.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.zip.Deflater;
 import java.util.zip.DeflaterOutputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.openmrs.module.shr.contenthandler.api.Content.CompressionFormat;
 import org.openmrs.module.shr.contenthandler.api.Content.Representation;
 
 import com.github.tomakehurst.wiremock.junit.WireMockRule;
 
 public class ContentTest {
 	
 	private static final String TEST_DATA = "<test>This is a test string. It is awesome.</test>";
 	
 	@Rule
 	public WireMockRule wireMockRule = new WireMockRule(8001);
 	
 	private void setupContentMockService(byte[] body) {
 		stubFor(get(urlEqualTo("/resource"))
 			.willReturn(aResponse()
 				.withStatus(200)
 				.withBody(body)
 			)
 		);
 	}
 	
 	
 	/**
 	 * @see Content#getRawData()
 	 * @verifies decode the data if it is base64 encoded
 	 */
 	@Test
 	public void getRawData_shouldDecodeTheDataIfItIsBase64Encoded()
 			throws Exception {
 		String encoded = Base64.encodeBase64String(TEST_DATA.getBytes());
 		Content content = new Content(encoded, false, "xml", "text/xml", null, Representation.B64, null, null);
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 	}
 
 	/**
 	 * @see Content#getRawData()
 	 * @verifies decompress the data if it is compressed
 	 */
 	@Test
 	public void getRawData_shouldDecompressTheDataIfItIsCompressed()
 			throws Exception {
 		testCompressedPayload(compressDeflate(TEST_DATA), CompressionFormat.DF);
 		testCompressedPayload(compressGZip(TEST_DATA), CompressionFormat.GZ);
 		testCompressedPayload(compressZLib(TEST_DATA), CompressionFormat.ZL);
 	}
 	
 	private void testCompressedPayload(byte[] compressedPayload, CompressionFormat format) throws IOException {
 		String payload = Base64.encodeBase64String(compressedPayload);
 		Content content = new Content(payload, false, "xml", "text/xml", null, Representation.B64, format, null);
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 	}
 
 	/**
 	 * @see Content#getRawData()
 	 * @verifies retrieve the data from a url if the payload is a url
 	 */
 	@Test
 	public void getRawData_shouldRetrieveTheDataFromAUrlIfThePayloadIsAUrl()
 			throws Exception {
 		//Plain text
 		setupContentMockService(TEST_DATA.getBytes());
 		Content content = new Content("http://localhost:8001/resource", true, "xml", "text/xml", null, Representation.TXT, null, null);
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 		
 		//Base64
 		setupContentMockService(Base64.encodeBase64(TEST_DATA.getBytes()));
 		content = new Content("http://localhost:8001/resource", true, "xml", "text/xml", null, Representation.B64, null, null);
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 		
 		//Compressed
 		testCompressedURL(compressDeflate(TEST_DATA), CompressionFormat.DF);
 		testCompressedURL(compressGZip(TEST_DATA), CompressionFormat.GZ);
 		testCompressedURL(compressZLib(TEST_DATA), CompressionFormat.ZL);
		testCompressedURL_nonBase64(compressDeflate(TEST_DATA), CompressionFormat.DF);
		testCompressedURL_nonBase64(compressGZip(TEST_DATA), CompressionFormat.GZ);
		testCompressedURL_nonBase64(compressZLib(TEST_DATA), CompressionFormat.ZL);
 	}
 	
 	private void testCompressedURL(byte[] compressedPayload, CompressionFormat format) throws IOException {
 		setupContentMockService(Base64.encodeBase64(compressedPayload));
 		Content content = new Content("http://localhost:8001/resource", true, "xml", "text/xml", null, Representation.B64, format, null);
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 	}
	
	private void testCompressedURL_nonBase64(byte[] compressedPayload, CompressionFormat format) throws IOException {
		setupContentMockService(compressedPayload);
		Content content = new Content("http://localhost:8001/resource", true, "xml", "text/xml", null, Representation.BINARY, format, null);
		assertEquals(TEST_DATA, new String(content.getRawData()));
	}
 
 	/**
 	 * @see Content#getRawData()
 	 * @verifies return the payload string as a byte array if it's not encoded or compressed
 	 */
 	@Test
 	public void getRawData_shouldReturnThePayloadStringAsAByteArrayIfItsNotEncodedOrCompressed()
 			throws Exception {
 		Content content = new Content(TEST_DATA, "xml", "text/xml");
 		assertEquals(TEST_DATA, new String(content.getRawData()));
 	}
 	
 	
 	private static byte[] compressDeflate(String content) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DeflaterOutputStream deflateOut = new DeflaterOutputStream(out, new Deflater(0, true));
 		deflateOut.write(content.getBytes());
 		IOUtils.closeQuietly(deflateOut);
 		return out.toByteArray();
 	}
 	
 	private static byte[] compressGZip(String content) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		GZIPOutputStream gzipOut = new GZIPOutputStream(out);
 		gzipOut.write(content.getBytes());
 		IOUtils.closeQuietly(gzipOut);
 		return out.toByteArray();
 	}
 	
 	private static byte[] compressZLib(String content) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		DeflaterOutputStream zlibOut = new DeflaterOutputStream(out);
 		zlibOut.write(content.getBytes());
 		IOUtils.closeQuietly(zlibOut);
 		return out.toByteArray();
 	}
 }
