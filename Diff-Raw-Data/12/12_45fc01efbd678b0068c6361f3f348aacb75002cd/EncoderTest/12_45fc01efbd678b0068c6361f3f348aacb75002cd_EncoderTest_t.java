 /**
  * 
  */
 package hoenheim.encoder;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author zubinmithra
  *
  */
 public class EncoderTest extends Encoder {
	
 	@Test
	public void testEncodeCSSStringsAndUrls() {
		Encoder e = new Encoder();
		assertEquals(e.encodeCSSStringsAndUrls(""), "");
		assertEquals(e.encodeCSSStringsAndUrls("<"), "\\00003c");
		assertEquals(e.encodeCSSStringsAndUrls("<<"), "\\00003c\\00003c");
		assertEquals(e.encodeCSSStringsAndUrls(">"), "\\00003e");
		assertEquals(e.encodeCSSStringsAndUrls("this'is"), "this\\'is");
 	}
 
 }
