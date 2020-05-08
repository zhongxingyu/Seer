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
  * $Id: WebauthResponseTest.java,v 1.8 2005/03/31 15:08:24 jw35 Exp $
  *
  */
 
 package uk.ac.cam.ucs.webauth;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.SimpleTimeZone;
 
 import junit.framework.TestCase;
 
 public class WebauthResponseTest extends TestCase {
 
 	// Note that most of these responses won't validate becasue they
 	// have been hacked to demonstrate various Webauth Response
 	// features. OK_RESPONSE, OTHER_RESPONSE, CANCEL_RESPONSE and
 	// NULL_RESPONSE should however all be syntactically valid
 
 	private static final String OK_RESPONSE = "1!200!A Message %21%21 %25%25!20050303T151131Z!1109862691-30323-5!h"
 			+ "ttp://raven.cam.ac.uk/debug.html!jw35!pwd!foo,bar!36000!for babies"
 			+ "!2!B1PKo8dhTP6eJKCo52xm5IMvzIJI6EaH90u.irWKrnYluaQRePHi6jQOXRBfJ2sG"
 			+ "9GxBh821.q-KdFMVYicjbWBkWO-xJ1VpJBPiPCVtX1KKG4nSYPfcQEmz5SQDGg9OTES"
 			+ "SprdK7wz0H-aGviMsNPU30UlCQh1hdrzmpThwoek_";
 
 	private static final long OK_RESPONSE_TIMEVAL = 1109862691000L;
 
 //	private static final String OTHER_RESPONSE = "1!200!A Message %21%21 %25%25!20050303T151131Z!1109862691-30323-5!h"
 //			+ "ttp://raven.cam.ac.uk/debug.html!jw99!pwd!foo,bar!36000!for babies"
 //			+ "!2!B1PKo8dhTP6eJKCo52xm5IMvzIJI6EaH90u.irWKrnYluaQRePHi6jQOXRBfJ2sG"
 //			+ "9GxBh821.q-KdFMVYicjbWBkWO-xJ1VpJBPiPCVtX1KKG4nSYPfcQEmz5SQDGg9OTES"
 //			+ "SprdK7wz0H-aGviMsNPU30UlCQh1hdrzmpThwoek_";
 
 	private static final String CANCEL_RESPONSE = "1!410!!20050303T151131Z!1109862691-30323-5!http://raven.cam.ac.uk/"
 			+ "debug.html!!!!!!!";
 
 	private static final String SHORT_RESPONSE = "1!200!A Message %21%21 %25%25!20050303T151131Z!1109862691-30323-5!h"
 			+ "ttp://raven.cam.ac.uk/debug.html!jw99!pwd!foo,bar!36000!for babies"
 			+ "!2";
 
 	private static final String NULL_RESPONSE = "!!!!!!!!!!!!";
 
 	private static final String BAD_VER = "A!!!20050303T151131Z!!http://a.b/c/!!!!!!!";
 
 //	private static final String HIGH_VER = "99!!!20050303T151131Z!!http://a.b/c/!!!!!!!";
 
 	private static final String BAD_STATUS = "!A!!20050303T151131Z!!http://a.b/c/!!!!!!!";
 
 	private static final String BAD_ISSUE = "!!!TODAY!!http://a.b/c/!!!!!!!";
 
 	private static final String BAD_LIFE = "!!!20050303T151131Z!!http://a.b/c/!!!!A!!!";
 
 	private static final String SUMMER_RESPONSE = "1!200!A Message %21%21 %25%25!20050603T151131Z!1109862691-30323-5!h"
 			+ "ttp://raven.cam.ac.uk/debug.html!jw35!pwd!foo,bar!36000!for babies"
 			+ "!2!B1PKo8dhTP6eJKCo52xm5IMvzIJI6EaH90u.irWKrnYluaQRePHi6jQOXRBfJ2sG"
 			+ "9GxBh821.q-KdFMVYicjbWBkWO-xJ1VpJBPiPCVtX1KKG4nSYPfcQEmz5SQDGg9OTES"
 			+ "SprdK7wz0H-aGviMsNPU30UlCQh1hdrzmpThwoek_";
 
 	private static final long SUMMER_RESPONSE_TIMEVAL = 1117811491000L;
 
 	private WebauthResponse ok_response, //ok_response2, other_response,
 			null_response, cancel_response, summer_response;
 	private Calendar date;
 	private HashSet<String> set, emptyset;
 	private long seconds, summer_seconds;
 
 	// ----------------------------------------------------------- Housekeeping
 
 	public static void main(String args[]) {
 		junit.textui.TestRunner.run(WebauthRequestTest.class);
 	}
 
 	// --------------------------------------------------------------- Fixtures
 
	@Override
  protected void setUp() {
 
 		ok_response = new WebauthResponse(OK_RESPONSE);
 //		ok_response2 = new WebauthResponse(OK_RESPONSE);
 //		other_response = new WebauthResponse(OTHER_RESPONSE);
 		cancel_response = new WebauthResponse(CANCEL_RESPONSE);
 		null_response = new WebauthResponse(NULL_RESPONSE);
 		summer_response = new WebauthResponse(SUMMER_RESPONSE);
 
 		date = Calendar.getInstance(new SimpleTimeZone(0, "UT"));
 
 		date.clear();
 		date.set(2005, 02, 03, 15, 11, 31);
 		seconds = date.getTime().getTime();
 
 		date.clear();
 		date.set(2005, 05, 03, 15, 11, 31);
 		summer_seconds = date.getTime().getTime();
 
 		emptyset = new HashSet<String>();
 
 		set = new HashSet<String>();
 		set.add("foo");
 		set.add("bar");
 	}
 
	@Override
  protected void tearDown() {
 	}
 
 	// ------------------------------------------------------------------ Tests
 
 	public void testStatusString() {
 		assertEquals("OK", WebauthResponse.statusString("200"));
 		assertEquals("OK", WebauthResponse.statusString(200));
 	}
 
 	public void testLength() {
 		assertEquals(13, ok_response.length());
 		assertEquals(13, cancel_response.length());
 		assertEquals(13, null_response.length());
 		WebauthResponse short_response = new WebauthResponse(SHORT_RESPONSE);
 		assertEquals(12, short_response.length());
 	}
 
 	public void testVer() {
 		assertEquals("1", ok_response.get("ver"));
 		assertEquals("1", cancel_response.get("ver"));
 		assertEquals("", null_response.get("ver"));
 	}
 
 	public void testIntVer() throws WebauthException {
 		assertEquals(1, ok_response.getInt("ver"));
 		assertEquals(1, cancel_response.getInt("ver"));
 		assertEquals(-1, null_response.getInt("ver"));
 	}
 
 	public void testStatus() {
 		assertEquals("200", ok_response.get("status"));
 		assertEquals("410", cancel_response.get("status"));
 		assertEquals("", null_response.get("status"));
 	}
 
 	public void testIntStatus() throws WebauthException {
 		assertEquals(200, ok_response.getInt("status"));
 		assertEquals(410, cancel_response.getInt("status"));
 		assertEquals(-1, null_response.getInt("status"));
 	}
 
 	public void testMsg() {
 		assertEquals("A Message !! %%", ok_response.get("msg"));
 		assertEquals("", cancel_response.get("msg"));
 		assertEquals("", null_response.get("msg"));
 	}
 
 	public void testIssue() {
 		assertEquals("20050303T151131Z", ok_response.get("issue"));
 		assertEquals("20050303T151131Z", cancel_response.get("issue"));
 		assertEquals("", null_response.get("issue"));
 		assertEquals("20050603T151131Z", summer_response.get("issue"));
 	}
 
 	public void testDateIssue() throws WebauthException {
 		assertEquals("Winter sanity", seconds, OK_RESPONSE_TIMEVAL);
 		assertEquals(seconds, ok_response.getDate("issue"));
 		assertEquals(seconds, cancel_response.getDate("issue"));
 		assertEquals(-1, null_response.getDate("issue"));
 		assertEquals("Summer sanity", summer_seconds, SUMMER_RESPONSE_TIMEVAL);
 		assertEquals(summer_seconds, summer_response.getDate("issue"));
 	}
 
 	public void testId() {
 		assertEquals("1109862691-30323-5", ok_response.get("id"));
 		assertEquals("1109862691-30323-5", cancel_response.get("id"));
 		assertEquals("", null_response.get("id"));
 	}
 
 	public void testURL() {
 		assertEquals("http://raven.cam.ac.uk/debug.html",
 				ok_response.get("url"));
 		assertEquals("http://raven.cam.ac.uk/debug.html",
 				cancel_response.get("url"));
 		assertEquals("", null_response.get("url"));
 	}
 
 	public void testPrincipal() {
 		assertEquals("jw35", ok_response.get("principal"));
 		assertEquals("", cancel_response.get("principal"));
 		assertEquals("", null_response.get("principal"));
 	}
 
 	public void testAuth() {
 		assertEquals("pwd", ok_response.get("auth"));
 		assertEquals("", cancel_response.get("auth"));
 		assertEquals("", null_response.get("auth"));
 	}
 
 	public void testSSO() {
 		assertEquals("foo,bar", ok_response.get("sso"));
 		assertEquals("", cancel_response.get("sso"));
 		assertEquals("", null_response.get("sso"));
 	}
 
 	public void testSetSSO() {
 		assertEquals(set, ok_response.getColl("sso"));
 		assertEquals(emptyset, cancel_response.getColl("sso"));
 		assertEquals(emptyset, null_response.getColl("sso"));
 	}
 
 	public void testLife() {
 		assertEquals("36000", ok_response.get("life"));
 		assertEquals("", cancel_response.get("life"));
 		assertEquals("", null_response.get("life"));
 	}
 
 	public void testIntLife() throws WebauthException {
 		assertEquals(36000, ok_response.getInt("life"));
 		assertEquals(-1, cancel_response.getInt("life"));
 		assertEquals(-1, null_response.getInt("life"));
 	}
 
 	public void testParams() {
 		assertEquals("for babies", ok_response.get("params"));
 		assertEquals("", cancel_response.get("params"));
 		assertEquals("", null_response.get("params"));
 	}
 
 	public void testKId() {
 		assertEquals("2", ok_response.get("kid"));
 		assertEquals("", cancel_response.get("kid"));
 		assertEquals("", null_response.get("kid"));
 	}
 
 	public void testSig() {
 		assertEquals(
 				"B1PKo8dhTP6eJKCo52xm5IMvzIJI6EaH90u.irWKrnYluaQRePHi6jQOXRBfJ"
 						+ "2sG9GxBh821.q-KdFMVYicjbWBkWO-xJ1VpJBPiPCVtX1KKG4nSYPfcQEmz5S"
 						+ "QDGg9OTESSprdK7wz0H-aGviMsNPU30UlCQh1hdrzmpThwoek_",
 				ok_response.get("sig"));
 		assertEquals("", cancel_response.get("sig"));
 		assertEquals("", null_response.get("sig"));
 	}
 
 	public void testRawData() {
 		assertEquals(
 				"1!200!A Message %21%21 %25%25!20050303T151131Z!1109862691-303"
 						+ "23-5!http://raven.cam.ac.uk/debug.html!jw35!pwd!foo,bar!3600"
 						+ "0!for babies", ok_response.getRawData());
 		assertEquals(
 				"1!410!!20050303T151131Z!1109862691-30323-5!http://raven.cam."
 						+ "ac.uk/debug.html!!!!!", cancel_response.getRawData());
 		assertEquals("!!!!!!!!!!", null_response.getRawData());
 	}
 
 	public void testNullField() {
 		assertEquals("", ok_response.get(null));
 		assertEquals("", ok_response.get(""));
 	}
 
 	public void testNoSuchField() throws WebauthException {
 
 		assertEquals("", ok_response.get("nonesuch"));
 		assertEquals(-1, ok_response.getInt("nonesuch"));
 		assertEquals(-1, ok_response.getDate("nonesuch"));
 		assertEquals(emptyset, ok_response.getColl("nonesuch"));
 
 	}
 
 	public void testEmpty() {
 		WebauthResponse response = new WebauthResponse("");
 		assertEquals("", response.get("ver"));
 		assertEquals("", response.getRawData());
 	}
 
 	public void testOneField() {
 		WebauthResponse response = new WebauthResponse("FOO");
 		assertEquals("FOO", response.get("ver"));
 		assertEquals("", response.getRawData());
 	}
 
 	public void testBadVer() {
 		try {
 			WebauthResponse test = new WebauthResponse(BAD_VER);
 			test.getInt("ver");
 			fail("Didn't detect non-integer version number");
 		} catch (WebauthException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testBadStatus() {
 		try {
 			WebauthResponse test = new WebauthResponse(BAD_STATUS);
 			test.getInt("status");
 			fail("Didn't detect non-integer status");
 		} catch (WebauthException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testBadIssue() {
 		try {
 			WebauthResponse test = new WebauthResponse(BAD_ISSUE);
 			test.getDate("issue");
 			fail("Didn't detect corrupt date");
 		} catch (WebauthException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testBadLife() {
 		try {
 			WebauthResponse test = new WebauthResponse(BAD_LIFE);
 			test.getInt("life");
 			fail("Didn't detect non-integer life");
 		} catch (WebauthException e) {
 			assertTrue(true);
 		}
 	}
 
 }
