 /**
  * Copyright (c) 2011 Sebastian Tomac (tomac.org)
  * Licensed under LGPL licenses.
  * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
  **/
 package org.tomac.protocol.fix;
 
 
 import static org.junit.Assert.*;
 
 import java.nio.ByteBuffer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.tomac.utils.Utils;
 
 /**
  * @author seto
  *
  */
 public class TestFixUtils {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 	
 	@Test
 	public void testPutLongToByteBuffer() {
 		byte[] b;
 		ByteBuffer out = ByteBuffer.wrap(new byte[24]);
 
 		FixUtils.put(out, 0);
 		b = new byte[1];
 		out.position(0);
 		out.get(b);
 		assertTrue("0".equals(new String(b)));
 
 		out.position(0);
 		FixUtils.put(out, 100);
 		b = new byte[3];
 		out.position(0);
 		out.get(b);
 		assertTrue("100".equals(new String(b)));
 
 		out.position(0);
 		FixUtils.put(out, 1000);
 		b = new byte[4];
 		out.position(0);
 		out.get(b);
 		assertTrue("1000".equals(new String(b)));
 		
 
 		out.position(0);
 		FixUtils.put(out, 1234567890);
 		b = new byte[10];
 		out.position(0);
 		out.get(b);
 		assertTrue("1234567890".equals(new String(b)));
 		
 		out.position(0);
 		FixUtils.put(out, -1234567890);
 		b = new byte[11];
 		out.position(0);
 		out.get(b);
 		assertTrue("-1234567890".equals(new String(b)));
 
 	}
 	
 	@Test
 	public void testLongToByteArray() {
 		
 		long l = 182000;
 		ByteBuffer buf = ByteBuffer.wrap(new byte[6]);
 		
 		FixUtils.put(buf, l);
 		
 		assertEquals("182000", new String(buf.array()));
 	}
 	
 	@Test
 	public void testLongToFixFloat() {
 		
 		long l;
 		byte[] out = new byte[FixUtils.FIX_MAX_DIGITS];
 		
 		l = 182000;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("18.2000", new String(out).trim());
 
 		l = 18200;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("1.8200", new String(out).trim());
 
 		l = 1820;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("0.1820", new String(out).trim());
 
 		l = 182;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("0.0182", new String(out).trim());
 
 		l = 18;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("0.0018", new String(out).trim());
 
 		l = 1;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("0.0001", new String(out).trim());
 
 		l = -182000;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-18.2000", new String(out).trim());
 
 		l = -18200;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-1.8200", new String(out).trim());
 
 		l = -1820;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-0.1820", new String(out).trim());
 
 		l = -182;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-0.0182", new String(out).trim());
 
 		l = -18;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-0.0018", new String(out).trim());
 
 		l = -1;
 		Utils.fillNul(out);
 		FixUtils.longToFixFloat(out, 0, l, Utils.digits(l));
 		assertEquals("-0.0001", new String(out).trim());
 
 		
 	}
 	
 }
