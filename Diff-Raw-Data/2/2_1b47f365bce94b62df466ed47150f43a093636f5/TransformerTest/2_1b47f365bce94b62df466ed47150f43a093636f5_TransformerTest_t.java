 package org.twuni.common.crypto.rsa;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.twuni.common.util.Base64;
 
 public class TransformerTest {
 
 	private Transformer trusted;
 	private Transformer untrusted;
 
 	@Before
 	public void setUp() {
 		PrivateKey privateKey = new KeyGenerator().generate( 512 );
 		PublicKey publicKey = privateKey.getPublicKey();
 		trusted = new Transformer( privateKey );
 		untrusted = new Transformer( publicKey );
 	}
 
 	@Test
 	public void testMultipleBlockRoundTripString() throws IOException {
 
 		String expected = generateRandomString( 4096 );
 		String actual = decrypt( encrypt( expected ) );
 
 		Assert.assertEquals( expected, actual );
 
 	}
 
 	@Test
 	public void testMultipleBlockRoundTripStringTwoLayers() throws IOException {
 
 		String expected = generateRandomString( 4096 );
 		String actual = decrypt( decrypt( encrypt( encrypt( expected ) ) ) );
 
 		Assert.assertEquals( expected, actual );
 
 	}
 
 	@Test
 	public void testKeyThatHasBeenKnownToGenerateInvalidEncryptionBlocks() throws IOException {
 
		PrivateKey privateKey = PrivateKey.deserialize( "RSA|AOJs7R7IdPtqsxp8v5VIP/UYG+I+zD5usvBzUPFHc5fV|ANiY+/6eTInKw5lge7hHrNdC7XFbmsATValP9XNRqa7X|AQAB" );
 		PublicKey publicKey = privateKey.getPublicKey();
 		trusted = new Transformer( privateKey );
 		untrusted = new Transformer( publicKey );
 
 		byte [] expected = Base64.decode( "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" );
 		byte [] actual = decrypt( encrypt( expected ) );
 
 		Assert.assertArrayEquals( expected, actual );
 
 	}
 
 	@Test
 	public void testMultipleBlockRoundTripBytes() throws IOException {
 
 		byte [] expected = generateRandomBytes( 4096 );
 		byte [] actual = decrypt( encrypt( expected ) );
 
 		Assert.assertArrayEquals( expected, actual );
 
 	}
 
 	@Test
 	public void testMultipleBlockRoundTripBytesTwoLayers() throws IOException {
 
 		byte [] expected = generateRandomBytes( 4096 );
 		byte [] actual = decrypt( decrypt( encrypt( encrypt( expected ) ) ) );
 
 		Assert.assertArrayEquals( expected, actual );
 
 	}
 
 	private byte [] decrypt( byte [] message ) throws IOException {
 		return untrusted.decrypt( message );
 	}
 
 	private byte [] encrypt( byte [] message ) throws IOException {
 		return trusted.encrypt( message );
 	}
 
 	private String decrypt( String message ) throws IOException {
 		return untrusted.decrypt( message );
 	}
 
 	private String encrypt( String message ) throws IOException {
 		return trusted.encrypt( message );
 	}
 
 	private String generateRandomString( int length ) {
 		return Base64.encode( generateRandomBytes( length * 2 ) ).substring( 0, length );
 	}
 
 	private byte [] generateRandomBytes( int length ) {
 		Random random = new Random();
 		byte [] buffer = new byte [length];
 		random.nextBytes( buffer );
 		return buffer;
 	}
 
 }
