 /* This file is part of the University of Cambridge Web Authentication
  * System Java Toolkit
  *
  * Copyright 2005 University of Cambridge
  *
  * This toolkit is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * The toolkit is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this toolkit; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id: WebauthRequestTest.java,v 1.4 2005/07/28 08:34:06 jw35 Exp $
  *
  */
 
 package uk.ac.cam.ucs.webauth;
 
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 public class WebauthRequestTest extends TestCase {
 
 	private WebauthRequest request;
 
 	// ----------------------------------------------------------- Housekeeping
 
 	public static void main(String args[]) {
 		junit.textui.TestRunner.run(WebauthRequestTest.class);
 	}
 
 	// --------------------------------------------------------------- Fixtures
 
	@Override
  protected void setUp() {
 
 		request = new WebauthRequest();
 
 		request.set("ver", 2);
 		request.set("url", "http://www.cam.ac.uk/raven");
 		request.set("desc", "Description");
 		request.set("aauth", "x-foobar,pwd");
 		request.set("msg", "Message");
 		request.set("params", "Params");
 		request.set("date", "20050305T123456Z");
 
 	}
 
	@Override
  protected void tearDown() {
 	}
 
 	// ----------------------------------------------------------------- Tests
 
 	public void testDefaults() throws java.text.ParseException {
 
 		WebauthRequest r = new WebauthRequest();
 
 		assertEquals("1", r.get("ver"));
 
 		// the date in the ticket should be less than about a second
 		// before now
 
 		Date now = new Date();
 		assertTrue(now.getTime() - r.getDate("date") >= 0);
 		assertTrue(now.getTime() - r.getDate("date") < 1000);
 
 	}
 
 	public void testLength() {
 		assertEquals(7, request.length());
 	}
 
 	public void testGetFieldNames() {
 		String[] e = { "ver", "url", "desc", "aauth", "msg", "params", "date" };
 		HashSet<String> expected = new HashSet<String>(Arrays.asList(e));
 		HashSet<String> got = new HashSet<String>();
 		for (Iterator<String> it = request.getFieldNames(); it.hasNext();)
 			got.add(it.next());
 		assertEquals(expected, got);
 	}
 
 	public void testToQString() {
 
 		// Check that request.toQString is as expected
 
 		assertEquals("ver=2&url=http%3A%2F%2Fwww.cam.ac.uk%2Fraven"
 				+ "&desc=Description&aauth=x-foobar%2Cpwd&msg=Message"
 				+ "&params=Params&date=20050305T123456Z", request.toQString());
 
 		// now additionally set fail to true and test again
 
 		request.set("fail", "yes");
 
 		// System.out.println(request.toQString());
 		assertEquals("ver=2&url=http%3A%2F%2Fwww.cam.ac.uk%2Fraven"
 				+ "&desc=Description&aauth=x-foobar%2Cpwd&msg=Message"
 				+ "&params=Params&date=20050305T123456Z&fail=yes",
 				request.toQString());
 
 		// and the same for 'iact' set to "no"
 
 		request.set("iact", "no");
 
 		// System.out.println(request.toQString());
 		assertEquals("ver=2&url=http%3A%2F%2Fwww.cam.ac.uk%2Fraven"
 				+ "&desc=Description&aauth=x-foobar%2Cpwd&iact=no"
 				+ "&msg=Message&params=Params&date=20050305T123456Z"
 				+ "&fail=yes", request.toQString());
 
 		// and finally for 'iact' set to "yes"
 
 		request.set("iact", "yes");
 
 		// System.out.println(request.toQString());
 		assertEquals("ver=2&url=http%3A%2F%2Fwww.cam.ac.uk%2Fraven"
 				+ "&desc=Description&aauth=x-foobar%2Cpwd&iact=yes"
 				+ "&msg=Message&params=Params&date=20050305T123456Z"
 				+ "&fail=yes", request.toQString());
 
 	}
 
 	public void testToString() {
 
 		assertEquals("Webauth request: ver: 2, url: http://www.cam.ac.uk/"
 				+ "raven, desc: Description, aauth: x-foobar,pwd, iact"
 				+ ": , msg: Message, params: Params, date: 20050305T12"
 				+ "3456Z, fail: ", request.toString());
 
 	}
 
 	public void testGet() {
 		assertEquals("2", request.get("ver"));
 	}
 
 	public void testGetInt() {
 
 		assertEquals(2, request.getInt("ver"));
 
 		try {
 			request.getInt("url");
 			fail("didn't spot non-integer");
 		} catch (NumberFormatException e) {
 			assertTrue(true);
 		}
 
 	}
 
 	public void testGetDate() throws java.text.ParseException {
 
 		assertEquals(1110026096000L, request.getDate("date"));
 
 		// Try again with a date in the summer
 
 		WebauthRequest r = new WebauthRequest();
 		r.set("date", "20050605T123456Z");
 		assertEquals(1117974896000L, r.getDate("date"));
 
 		try {
 			request.getDate("url");
 			fail("didn't spot non-date");
 		} catch (java.text.ParseException e) {
 			assertEquals("java.text.ParseException: Unparseable date: "
 					+ "\"http://www.cam.ac.uk/raven\"", e.toString());
 		}
 
 	}
 
 	public void testGetColl() {
 
 		HashSet<String> set = new HashSet<String>();
 		set.add("x-foobar");
 		set.add("pwd");
 		assertEquals(set, request.getColl("aauth"));
 
 		HashSet<String> set2 = new HashSet<String>();
 		set2.add("2");
 		assertEquals(set2, request.getColl("ver"));
 
 	}
 
 	public void testGetNonsuch() {
 		assertEquals("", request.get("nonsuch"));
 		assertEquals(-1, request.getInt("nonsuch"));
 
 		HashSet<String> set = new HashSet<String>();
 		assertEquals(set, request.getColl("nonsuch"));
 
 		try {
 			request.getDate("nonsuch");
 			fail("didn't spot non-existent date");
 		} catch (java.text.ParseException e) {
 			assertEquals("java.text.ParseException: Unparseable date: \"\"",
 					e.toString());
 		}
 	}
 
 	public void testGetNull() {
 		assertEquals("", request.get(null));
 		assertEquals(-1, request.getInt(null));
 
 		HashSet<String> set = new HashSet<String>();
 		assertEquals(set, request.getColl(null));
 
 		try {
 			request.getDate(null);
 			fail("didn't spot non-existent date");
 		} catch (java.text.ParseException e) {
 			assertEquals("java.text.ParseException: Unparseable date: \"\"",
 					e.toString());
 		}
 	}
 
 	public void testSet() {
 
 		request.set("foo", "bar");
 		assertEquals("bar", request.get("foo"));
 
 		request.set("foo", 999);
 		assertEquals("999", request.get("foo"));
 
 		request.set("foo", 1110026096000L);
 		assertEquals("20050305T123456Z", request.get("foo"));
 
 		request.set("foo", 1117974896000L);
 		assertEquals("20050605T123456Z", request.get("foo"));
 
 		HashSet<String> set = new HashSet<String>();
 		set.add("x-foobar");
 		set.add("pwd");
 		request.set("foo", set);
 		assertEquals("x-foobar,pwd", request.get("foo"));
 
 	}
 
 	public void testSerialization() throws java.io.FileNotFoundException,
 			java.io.IOException {
 
 		FileOutputStream fileOut = new FileOutputStream(getClass().getResource(
 				"/").getFile()
 				+ "ser.test");
 		ObjectOutputStream out = new ObjectOutputStream(fileOut);
 		out.writeObject(request);
 		assertTrue(true);
 	}
 
 }
