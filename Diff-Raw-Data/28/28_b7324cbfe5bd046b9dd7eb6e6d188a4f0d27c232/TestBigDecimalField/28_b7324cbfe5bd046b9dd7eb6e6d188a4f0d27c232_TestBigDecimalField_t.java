 package org.tomac.protocol.fix.fix42nordic;
 
 import static org.junit.Assert.assertEquals;
 
 import java.math.BigDecimal;
 
 import org.junit.Test;
 import org.tomac.protocol.fix.FixUtils;
import org.tomac.protocol.fix.messaging.FixNewOrderSingle;
 
 public class TestBigDecimalField {
 
 	@Test
 	public void testBigDecimalRoundTripping() throws Exception {
 		// check to see if we have a BigDecimal constructor
 		BigDecimal originalPrice = new BigDecimal("10.3000");
 		assertEquals(4, originalPrice.scale());
		FixNewOrderSingle message = new FixNewOrderSingle();
 		message.price = 103000; // 10.30
 		byte[] out = "00.0000".getBytes();
		FixUtils.longToFixFloat(out, 0, message.price, out.length - 1);
 		BigDecimal extractedPrice = new BigDecimal(new String(out));
 		assertEquals(4, extractedPrice.scale());
 		assertEquals(new BigDecimal("10.3000"), extractedPrice);
 	}
 }
