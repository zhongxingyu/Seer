 /**
  * 
  */
 package org.guanxi.test.common.filters;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.guanxi.common.filters.GenericDecoderSubclass;
 import org.guanxi.common.filters.GenericEncoderSubclass;
 import org.guanxi.test.TestUtils;
 import org.junit.Test;
 
 /**
  * @author matthew
  *
  */
 public class GenericDecoderTest {
 	/**
 	 * This will test passing strings to the decoder
 	 * that do not feature the control character.
 	 * @throws IOException 
 	 */
 	@Test
 	public void testNoDecoding() throws IOException {
 		GenericDecoderSubclass decoder;
 		String input;
 		StringBuffer buffer;
 		StringWriter writer;
 		
 		writer = new StringWriter();
 		buffer = writer.getBuffer();
 		decoder = new GenericDecoderSubclass(writer, (char)0);
 		
 		for ( int i = 0;i < 100;i++ ) {
 			input = TestUtils.randomString(100);
 			
 			decoder.write(input);
 			assertEquals("GenericDecoder has altered the input in an invalid way", input, buffer.toString());
 			buffer.delete(0, buffer.length());
 		}
 	}
 	
 	/**
 	 * This tests the result of writing an invalid escape sequence to the
 	 * decoder.
 	 * 
 	 * @throws IOException 
 	 * 
 	 */
 	@Test(expected = NumberFormatException.class)
 	public void testInvalidEscapes() throws IOException {
 		GenericDecoderSubclass decoder;
 		
 		decoder = new GenericDecoderSubclass(new StringWriter(), '%');
 		
 		// the escape sequences are the escape character followed by
 		// two hexadecimal characters. therefore anything outside the
 		// range 0-9,a-f is invalid
 		decoder.write("aaa%zzaaa");
 	}
 	
 	/**
 	 * This tests known decoding sequences.
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void testKnownEscapes() throws IOException {
 		GenericDecoderSubclass decoder;
 		StringBuffer buffer;
 		StringWriter writer;
 		
 		writer = new StringWriter();
 		buffer = writer.getBuffer();
 		decoder = new GenericDecoderSubclass(writer, '%');
 		
 		for ( String[] currentPair : new String[][]{
 			new String[] { "aaa", "aaa" },
 			new String[] { "%2A", "*" },
 			new String[] { "%2a", "*" },
 			new String[] { "%2525", "%25" },
 			new String[] { "25%25", "25%" },
 		}) {
 			decoder.write(currentPair[0]);
 			assertEquals("GenericDecoder has altered the input in an invalid way", currentPair[1], buffer.toString());
 			buffer.delete(0, buffer.length());
 		}
 	}
 	
 	/**
 	 * This uses the generic encoder to test the decoding of the generic decoder
 	 * with random strings and random escape characters
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void testRandomEscapes() throws IOException {
 		// This performs only encoding of the control character
 		class GenericEncoder extends GenericEncoderSubclass {
 			// This is the character that starts any escape sequence
 			protected char controlCharacter;
 			
 			public GenericEncoder(Writer writer, char controlCharacter) {
 				super(writer);
 				this.controlCharacter = controlCharacter;
 			}
 			
 			@Override
 			protected String escape(char c) {
 				return controlCharacter + String.format("%02X", (int)c);
 			}
 
 			@Override
 			protected boolean requiresEscaping(char c) {
 				return c == controlCharacter;
 			}
 		};
 		
 		// this encodes every character
 		class GenericEncodeEverything extends GenericEncoder {
 			public GenericEncodeEverything(Writer writer, char controlCharacter) {
 				super(writer, controlCharacter);
 			}
 			
 			@Override
 			protected boolean requiresEscaping(char c) {
 				return true;
 			}
 		}
 		
 		// this randomly selects characters to encode.
 		// the control character is always encoded
 		class GenericEncodeRandom extends GenericEncoder {
 			public GenericEncodeRandom(Writer writer, char controlCharacter) {
 				super(writer, controlCharacter);
 			}
 			
 			@Override
 			protected boolean requiresEscaping(char c) {
 				return c == controlCharacter || (Math.abs(TestUtils.random.nextInt()) & 1) == 0; 
 				// using & 1 because this is equivalent to % 2 but faster
 			}
 		}
 		
 		StringBuffer encoderBuffer, decoderBuffer;
 		StringWriter encoderWriter, decoderWriter;
 		GenericEncoder encoder;
 		GenericDecoderSubclass decoder;
 		String input;
 		char controlCharacter;
 		
 		encoderWriter = new StringWriter();
 		encoderBuffer = encoderWriter.getBuffer();
 		decoderWriter = new StringWriter();
 		decoderBuffer = decoderWriter.getBuffer();
 		
 		for ( int i = 0;i < 100;i++ ) {
 			controlCharacter = TestUtils.randomString('1').toCharArray()[0];
 			encoder = new GenericEncoder(encoderWriter, controlCharacter);
 			decoder = new GenericDecoderSubclass(decoderWriter, controlCharacter);
 			
 			input = TestUtils.randomString(100);
 			encoder.write(input);
 			decoder.write(encoderBuffer.toString());
			assertEquals("GenericDecoder does not decode what GenericEncoder produces", input, decoderBuffer.toString());
 			
 			encoderBuffer.delete(0, encoderBuffer.length());
 			decoderBuffer.delete(0, decoderBuffer.length());
 		}
 		
 		for ( int i = 0;i < 100;i++ ) {
 			controlCharacter = TestUtils.randomString('1').toCharArray()[0];
 			encoder = new GenericEncodeEverything(encoderWriter, controlCharacter);
 			decoder = new GenericDecoderSubclass(decoderWriter, controlCharacter);
 			
 			input = TestUtils.randomString(100);
 			encoder.write(input);
 			decoder.write(encoderBuffer.toString());
 			assertEquals("GenericDecoder does not decode what GenericEncodeEverything produces", input, decoderBuffer.toString());
 			
 			encoderBuffer.delete(0, encoderBuffer.length());
 			decoderBuffer.delete(0, decoderBuffer.length());
 		}
 		
 		for ( int i = 0;i < 100;i++ ) {
 			controlCharacter = TestUtils.randomString('1').toCharArray()[0];
 			encoder = new GenericEncodeRandom(encoderWriter, controlCharacter);
 			decoder = new GenericDecoderSubclass(decoderWriter, controlCharacter);
 			
 			input = TestUtils.randomString(100);
 			encoder.write(input);
 			decoder.write(encoderBuffer.toString());
 			assertEquals("GenericDecoder does not decode what GenericEncodeRandom produces", input, decoderBuffer.toString());
 			
 			encoderBuffer.delete(0, encoderBuffer.length());
 			decoderBuffer.delete(0, decoderBuffer.length());
 		}
 	}
 }
