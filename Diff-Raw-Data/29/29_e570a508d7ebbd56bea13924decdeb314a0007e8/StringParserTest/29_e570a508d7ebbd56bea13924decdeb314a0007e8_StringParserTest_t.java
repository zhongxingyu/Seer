 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.util;
 
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestMethod;
 import j2meunit.framework.TestSuite;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.TimeZone;
 
 /**
  * Unit test for StringParser
  */
 public class StringParserTest extends TestCase {
     public StringParserTest() {
     }
 
     public StringParserTest(String testName, TestMethod testMethod) {
         super(testName, testMethod);
     }
 
     public void setUp() {
     }
 
     public void tearDown() {
     }
 
     /**
      * Test of parseDateString method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseDateString() {
         String rawDate;
         Date result;
         Calendar cal = Calendar.getInstance();
 
         rawDate = "Sat, 10 Feb 2007 21:27:01 -0800";
         result = StringParser.parseDateString(rawDate);
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
 
         assertEquals("Test 1", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 1", 1, cal.get(Calendar.MONTH));
         assertEquals("Test 1", 10, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 1", 21, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 1", 27, cal.get(Calendar.MINUTE));
         assertEquals("Test 1", 1, cal.get(Calendar.SECOND));
 
         rawDate = "Sat, 10 Feb 2007 21:30:37 America/Los_Angeles";
         result = StringParser.parseDateString(rawDate);
         cal = Calendar.getInstance();
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
         assertEquals("Test 2", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 2", 1, cal.get(Calendar.MONTH));
         assertEquals("Test 2", 10, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 2", 21, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 2", 30, cal.get(Calendar.MINUTE));
         assertEquals("Test 2", 37, cal.get(Calendar.SECOND));
 
         rawDate = "Tue, 23 Oct 2007 18:01 EST";
         result = StringParser.parseDateString(rawDate);
         cal = Calendar.getInstance();
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
         assertEquals("Test 3", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 3", 9, cal.get(Calendar.MONTH));
         assertEquals("Test 3", 23, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 3", 18, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 3", 01, cal.get(Calendar.MINUTE));
         assertEquals("Test 3", 00, cal.get(Calendar.SECOND));
 
         rawDate = "Sun, 18 Nov 2007 09:00:33 -0500 (EST)";
         result = StringParser.parseDateString(rawDate);
         cal = Calendar.getInstance();
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
         assertEquals("Test 4", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 4", 10, cal.get(Calendar.MONTH));
         assertEquals("Test 4", 18, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 4", 9, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 4", 00, cal.get(Calendar.MINUTE));
         assertEquals("Test 4", 33, cal.get(Calendar.SECOND));
 
         rawDate = "Sun, 18 Nov 2007  16:19:23 -0500";
         result = StringParser.parseDateString(rawDate);
         cal = Calendar.getInstance();
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
         assertEquals("Test 5", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 5", 10, cal.get(Calendar.MONTH));
         assertEquals("Test 5", 18, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 5", 16, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 5", 19, cal.get(Calendar.MINUTE));
         assertEquals("Test 5", 23, cal.get(Calendar.SECOND));
 
         rawDate = "08 Nov 07 17:37:45";
         result = StringParser.parseDateString(rawDate);
         cal = Calendar.getInstance();
         cal.setTime(result);
         cal.setTimeZone(TimeZone.getDefault());
         assertEquals("Test 6", 2007, cal.get(Calendar.YEAR));
         assertEquals("Test 6", 10, cal.get(Calendar.MONTH));
         assertEquals("Test 6", 8, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("Test 6", 17, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("Test 6", 37, cal.get(Calendar.MINUTE));
         assertEquals("Test 6", 45, cal.get(Calendar.SECOND));
     }
 
     /**
      * Test of createDateString method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testCreateDateString() {
         // Test for time zone GMT-5
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"));
         cal.set(Calendar.YEAR, 2007);
         cal.set(Calendar.MONTH, 1);
         cal.set(Calendar.DAY_OF_MONTH, 10);
         cal.set(Calendar.HOUR_OF_DAY, 21);
         cal.set(Calendar.MINUTE, 27);
         cal.set(Calendar.SECOND, 1);
 
         String expected = "Sat, 10 Feb 2007 21:27:01 -0500";
         String actual = StringParser.createDateString(cal.getTime(),
                 TimeZone.getTimeZone("GMT-5"));
         assertEquals("GMT-5", expected, actual);
 
         // Test for time zone GMT+2
         cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
         cal.set(Calendar.YEAR, 2007);
         cal.set(Calendar.MONTH, 1);
         cal.set(Calendar.DAY_OF_MONTH, 10);
         cal.set(Calendar.HOUR_OF_DAY, 21);
         cal.set(Calendar.MINUTE, 27);
         cal.set(Calendar.SECOND, 1);
 
         expected = "Sat, 10 Feb 2007 21:27:01 +0200";
         actual = StringParser.createDateString(cal.getTime(),
                 TimeZone.getTimeZone("GMT+2"));
         assertEquals("GMT+2", expected, actual);
 
         // Test for time zone GMT
         cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
         cal.set(Calendar.YEAR, 2007);
         cal.set(Calendar.MONTH, 1);
         cal.set(Calendar.DAY_OF_MONTH, 10);
         cal.set(Calendar.HOUR_OF_DAY, 21);
         cal.set(Calendar.MINUTE, 27);
         cal.set(Calendar.SECOND, 1);
 
         expected = "Sat, 10 Feb 2007 21:27:01 +0000";
         actual = StringParser.createDateString(cal.getTime(),
                 TimeZone.getTimeZone("GMT"));
         assertEquals("GMT", expected, actual);
     }
 
     /**
      * Test of parseMailHeaders method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseMailHeaders() {
         String[] rawLines = {
                 "Return-Path: <jdoe@generic.test>",
                 "Received: from hyperion.generic.test ([unix socket])",
                 "         by hyperion.generic.test (Cyrus v2.3.0) with LMTPA;",
                 "         Sun, 24 Dec 2006 20:59:45 -0500",
                 "X-Sieve: CMU Sieve 2.3", "From: John Doe <jdoe@generic.test>",
                 "To: neo@generic.test", "Subject: Some test message",
                 "Date: Sun, 24 Dec 2006 20:59:41 -0500",
                 "User-Agent: KMail/1.9.1", "MIME-Version: 1.0",
                 "Content-Type: text/plain;", "  charset=\"utf-8\"",
                 "Content-Transfer-Encoding: base64",
                 "Content-Disposition: inline",
                 "Message-Id: <200612242059.42552.jdoe@generic.test>",
                 "X-Scanned-By: MIMEDefang 2.54 on 10.4.1.12"
             };
 
         Hashtable result = StringParser.parseMailHeaders(rawLines);
 
         // Print actual contents for debugging
         //        for(java.util.Enumeration e = result.keys(); e.hasMoreElements();) {
         //            String key = (String)e.nextElement();
         //            System.err.println("-->"+key+" = "+result.get(key));
         //        }
         assertEquals("Number of headers", 14, result.size());
         assertEquals("<jdoe@generic.test>", result.get("return-path"));
         assertEquals("from hyperion.generic.test ([unix socket])\r\n         by hyperion.generic.test (Cyrus v2.3.0) with LMTPA;\r\n         Sun, 24 Dec 2006 20:59:45 -0500",
             result.get("received"));
         assertEquals("CMU Sieve 2.3", result.get("x-sieve"));
         assertEquals("John Doe <jdoe@generic.test>", result.get("from"));
         assertEquals("neo@generic.test", result.get("to"));
         assertEquals("Some test message", result.get("subject"));
         assertEquals("Sun, 24 Dec 2006 20:59:41 -0500", result.get("date"));
         assertEquals("KMail/1.9.1", result.get("user-agent"));
         assertEquals("1.0", result.get("mime-version"));
         assertEquals("text/plain;\r\n  charset=\"utf-8\"",
             result.get("content-type"));
         assertEquals("base64", result.get("content-transfer-encoding"));
         assertEquals("inline", result.get("content-disposition"));
         assertEquals("<200612242059.42552.jdoe@generic.test>",
             result.get("message-id"));
         assertEquals("MIMEDefang 2.54 on 10.4.1.12", result.get("x-scanned-by"));
     }
 
     /**
      * Test of parseMailHeaders method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseMailHeadersNoBlank() {
         // Test for correct handling of invalid input where there is no
         // blank line between the last header and the message body.
 
         String[] rawLines = {
                 "Return-Path: <jdoe@generic.test>", "Subject: Some test message",
                 "Some random content", "More random content"
             };
 
         Hashtable result = StringParser.parseMailHeaders(rawLines);
         assertEquals("Number of headers", 2, result.size());
         assertEquals("<jdoe@generic.test>", result.get("return-path"));
         assertEquals("Some test message", result.get("subject"));
     }
 
     /**
      * Test of parseEncodedHeader method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseEncodedHeader() {
         String text = "Hello World";
         String expectedResult = "Hello World";
         String result = StringParser.parseEncodedHeader(text);
         assertEquals("plain simple", expectedResult, result);
 
         text = "=?iso-8859-1?q?=A1Hol=E1=20Se=F1or!?=";
         expectedResult = "Hol Seor!";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP simple", expectedResult, result);
 
         text = "Foo =?iso-8859-1?q?=A1Hol=E1=20Se=F1or!?= Bar";
         expectedResult = "Foo Hol Seor! Bar";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP mixed", expectedResult, result);
         
         text = "=?iso-8859-1?q?=A1Hol=E1_?=\r\n =?iso-8859-1?q?Se=F1or!?=";
         expectedResult = "Hol Seor!";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP multi-line", expectedResult, result);
 
         text = "=?iso-8859-1?q?=A1Hol=E1_?=    =?iso-8859-1?q?Se=F1or!?=";
         expectedResult = "Hol Seor!";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP multi-line whitespace 1", expectedResult, result);
         
         text = " =?utf-8?B?R2V0IFJlYWR5IA==?= =?utf-8?B?Zm9yIEJsYWNrQg==?= " +
         		"=?utf-8?B?ZXJyeSBERVZDTw==?= =?utf-8?B?TiBGb3VyIFNxdQ==?= " +
         		"=?utf-8?B?YXJlIENoYWxsZQ==?= =?utf-8?B?bmdlIQ==?=";
         expectedResult = "Get Ready for BlackBerry DEVCON Four Square Challenge!";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP multi-line whitespace 2", expectedResult, result);
        
        text = "=?iso-8859-1?Q?Your 7digital download receipt?=";
        expectedResult = "Your 7digital download receipt";
        result = StringParser.parseEncodedHeader(text);
        assertEquals("QP unencoded spaces", expectedResult, result);
     }
 
     public void testParseEncodedHeaderMalformed() {
         String text = " ";
         String expectedResult = " ";
         String result = StringParser.parseEncodedHeader(text);
         assertEquals(expectedResult, result);
 
         text = "=";
         expectedResult = "=";
         result = StringParser.parseEncodedHeader(text);
         assertEquals(expectedResult, result);
         
         text = " =";
         expectedResult = " =";
         result = StringParser.parseEncodedHeader(text);
         assertEquals(expectedResult, result);
         
         text = " ?";
         expectedResult = " ?";
         result = StringParser.parseEncodedHeader(text);
         assertEquals(expectedResult, result);
         
         text = "=?iso-8859-1?q?Hol=E1?= =Jim";
         expectedResult = "Hol =Jim";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP prefix 1", expectedResult, result);
         
         text = "=?iso-8859-1?q?Hol=E1?= =?Jim";
         expectedResult = "Hol =?Jim";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP prefix 2", expectedResult, result);
         
         text = "=?iso-8859-1?q?Hol=E1?=   =?Jim";
         expectedResult = "Hol   =?Jim";
         result = StringParser.parseEncodedHeader(text);
         assertEquals("QP prefix 3", expectedResult, result);
     }
     
     /**
      * Test of createEncodedHeader method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testCreateEncodedHeaderQP() {
         try {
             String text = "Hello World";
             String expectedResult = "Foo: Hello World";
             String result = StringParser.createEncodedHeader("Foo:", text);
             assertEquals(expectedResult, result);
     
             text = "Hol Seor!";
             expectedResult = "Subject: =?iso-8859-1?q?=A1Hol=E1_Se=F1or!?=";
             result = StringParser.createEncodedHeader("Subject:", text);
             assertEquals("QP simple", expectedResult, result);
             
             text = "Justo de Canterbury (en ocasiones tambin llamado Iustus, " +
             		"nacido probablemente en Roma y fallecido el 10 de " +
             		"noviembre entre el 627 y el 631), fue un clrigo  y " +
             		"misionero  italiano, cuarto arzobispo de Canterbury. " +
             		"Enviado desde Italia  a Inglaterra  por el papa  " +
             		"Gregorio Magno con la misin de convertir a los " +
             		"anglosajones. Probablemente lleg en el segundo grupo, " +
             		"enviado en el ao 601. Se convirti en el primer obispo de " +
             		"Rochester en 604, y asisti a un concilio local en Pars " +
             		"en 614.";
             
             result = StringParser.createEncodedHeader("Subject:", text);
             
             // Parse the result and see that it matches the original.
             String parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals("QP complex", "Subject: " + text, parsedResult);
         } catch (Throwable t) {
             fail("Exception thrown during test: "+t.toString());
             t.printStackTrace();
         }
     }
     
     /**
      * Test of createEncodedHeader method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testCreateEncodedHeaderB64() {
         try {
             String text = "\u041d\u0435\u0440\u0435\u0441\u0442";
             
             String result = StringParser.createEncodedHeader("Subject:", text);
             
             // Parse the result and see that it matches the original.
             String parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals("B64 simple", "Subject: " + text, parsedResult);
             
             // Block of Russian text from some Wikipedia article, which is only
             // so long because of the necessary method of using Russian
             // characters in a Java string.
             text = "\u0420\u0435\u0447\u043d\u043e\u0439 \u043e\u043a\u0443" +
             		"\u043d\u044c \u043e\u0442\u043d\u043e\u0441\u0438\u0442" +
             		"\u0441\u044f \u043a \u0445\u0438\u0449\u043d\u044b\u043c" +
             		" \u0440\u044b\u0431\u0430\u043c: \u0432 \u0440\u0430" +
             		"\u0446\u0438\u043e\u043d\u0435 \u0432\u0437\u0440\u043e" +
             		"\u0441\u043b\u043e\u0433\u043e \u043e\u043a\u0443\u043d" +
             		"\u044f \u0437\u043d\u0430\u0447\u0438\u0442\u0435\u043b" +
             		"\u044c\u043d\u0443\u044e \u0434\u043e\u043b\u044e " +
             		"\u0437\u0430\u043d\u0438\u043c\u0430\u044e\u0442 " +
             		"\u0434\u0440\u0443\u0433\u0438\u0435 \u043f\u0440" +
             		"\u0435\u0441\u043d\u043e\u0432\u043e\u0434\u043d" +
             		"\u044b\u0435 \u0440\u044b\u0431\u044b. \u0420\u0435" +
             		"\u0447\u043d\u043e\u0439 \u043e\u043a\u0443\u043d\u044c" +
             		" \u043f\u0440\u0435\u0434\u043f\u043e\u0447\u0438\u0442" +
             		"\u0430\u0435\u0442 \u043f\u0440\u0438\u0434\u0435\u0440" +
             		"\u0436\u0438\u0432\u0430\u0442\u044c\u0441\u044f \u0440" +
             		"\u0430\u0432\u043d\u0438\u043d\u043d\u044b\u0445 \u0432" +
             		"\u043e\u0434\u043e\u0451\u043c\u043e\u0432, \u0435" +
             		"\u0433\u043e \u043c\u043e\u0436\u043d\u043e \u0432" +
             		"\u0441\u0442\u0440\u0435\u0442\u0438\u0442\u044c " +
             		"\u0432 \u0440\u0435\u043a\u0430\u0445, \u043e\u0437" +
             		"\u0451\u0440\u0430\u0445, \u043f\u0440\u0443\u0434" +
             		"\u0430\u0445, \u0432\u043e\u0434\u043e\u0445\u0440" +
             		"\u0430\u043d\u0438\u043b\u0438\u0449\u0430\u0445 " +
             		"\u0438 \u0434\u0430\u0436\u0435 \u0432 \u043c\u0435" +
             		"\u043d\u0435\u0435 \u0441\u043e\u043b\u043e\u043d\u043e" +
             		"\u0432\u0430\u0442\u044b\u0445 \u0443\u0447\u0430\u0441" +
             		"\u0442\u043a\u0430\u0445 \u043c\u043e\u0440\u0435\u0439.";
             
             result = StringParser.createEncodedHeader("Subject:", text);
             
             // Parse the result and see that it matches the original.
             parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals("B64 complex", "Subject: " + text, parsedResult);
             
         } catch (Throwable t) {
             fail("Exception thrown during test: "+t.toString());
             t.printStackTrace();
         }
     }
     
     /**
      * Test of createEncodedRecipientHeader method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testCreateEncodedRecipientHeader() {
         try {
             String[] recipients = new String[] { "jdoe@generic.org" };
             String expectedResult = "To: jdoe@generic.org";
             String result = StringParser.createEncodedRecipientHeader("To:", recipients);
             assertEquals(expectedResult, result);
     
             recipients = new String[] { "jdoe@generic.org" , "foo@bar.com" };
             expectedResult = "To: jdoe@generic.org, foo@bar.com";
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             assertEquals(expectedResult, result);
             
             recipients = new String[] { "\"John Doe\" <jdoe@generic.org>" };
             expectedResult = "To: \"John Doe\" <jdoe@generic.org>";
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             assertEquals(expectedResult, result);
     
             recipients = new String[] { "\"John Doe\" <jdoe@generic.org>", "\"Foobar\" <foo@bar.com>" };
             expectedResult = "To: \"John Doe\" <jdoe@generic.org>, \"Foobar\" <foo@bar.com>";
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             assertEquals(expectedResult, result);
             
             recipients = new String[] { "\"Hol Seor\" <jdoe@generic.org>" };
             expectedResult = "To: =?iso-8859-1?q?=22Hol=E1_Se=F1or=22?= <jdoe@generic.org>";
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             assertEquals(expectedResult, result);
             
             recipients = new String[] { "\"Hol Seor\" <jdoe@generic.org>", "\"Seor Hol\" <foo@bar.com>" };
             expectedResult = "To: " + StringParser.makeCsvString(recipients);
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             String parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals(expectedResult, parsedResult);
             
             recipients = new String[] { "\"Hol Seor\" <jdoe@generic.org>",
                     "Bob <bob@blah.net>", "\"Seor Hol\" <foo@bar.com>" };
             expectedResult = "To: " + StringParser.makeCsvString(recipients);
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals(expectedResult, parsedResult);
             
             recipients = new String[] { "bar@foo.com",
                     "\"Hol Seor\" <jdoe@generic.org>",
                     "Bob <bob@blah.net>", "foobar@blah.net",
                     "\"Seor Hol\" <foo@bar.com>", "jane.doe@something.org" };
             expectedResult = "To: " + StringParser.makeCsvString(recipients);
             result = StringParser.createEncodedRecipientHeader("To:", recipients);
             parsedResult = StringParser.parseEncodedHeader(result);
             assertEquals(expectedResult, parsedResult);
             
         } catch (Throwable t) {
             fail("Exception thrown during test: "+t.toString());
             t.printStackTrace();
         }
     }
     
     /**
      * Test of parseTokenString method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseTokenString() {
         String text = "one.red, two.green, three.blue";
         String token = ", ";
 
         String[] expectedResult = { "one.red", "two.green", "three.blue" };
         String[] result = StringParser.parseTokenString(text, token);
         assertEquals("First test failed", expectedResult, result);
 
         token = ".";
         expectedResult = new String[] { "one", "red, two", "green, three", "blue" };
         result = StringParser.parseTokenString(text, token);
         assertEquals("Second test failed", expectedResult, result);
     }
 
     /**
      * Test of parseCsvString method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseCsvString() {
         String text = "One, Two, Three";
         String[] expectedResult = { "One", "Two", "Three" };
         String[] result = StringParser.parseCsvString(text);
         assertEquals("First test failed", expectedResult, result);
 
         text = "One,Two, Three";
         expectedResult = new String[] { "One", "Two", "Three" };
         result = StringParser.parseCsvString(text);
         assertEquals("Second test failed", expectedResult, result);
 
         text = "One, \"Two, Three\" Four, Five";
         expectedResult = new String[] { "One", "\"Two, Three\" Four", "Five" };
         result = StringParser.parseCsvString(text);
         assertEquals("Third test failed", expectedResult, result);
 
         text = "One";
         expectedResult = new String[] { "One" };
         result = StringParser.parseCsvString(text);
         assertEquals("Fourth test failed", expectedResult, result);
 
         text = "One, Two,";
         expectedResult = new String[] { "One", "Two" };
         result = StringParser.parseCsvString(text);
         assertEquals("Fifth test failed", expectedResult, result);
 
         text = "\"User, Test\" <test@generic.org>";
         expectedResult = new String[] { "\"User, Test\" <test@generic.org>" };
         result = StringParser.parseCsvString(text);
         assertEquals("Sixth test failed", expectedResult, result);
 
         text = "\"User, Test\" <test@generic.org>, \"Doe, John\" <jdoe@generic.org>";
         expectedResult = new String[] {
                 "\"User, Test\" <test@generic.org>",
                 "\"Doe, John\" <jdoe@generic.org>"
             };
         result = StringParser.parseCsvString(text);
         assertEquals("Seventh test failed", expectedResult, result);
     }
 
     /**
      * Special assertion for arrays
      */
     private void assertEquals(String message, Object[] expected, Object[] actual) {
         assertEquals(message, expected.length, actual.length);
 
         for (int i = 0; i < expected.length; i++) {
             assertEquals(message, expected[i], actual[i]);
         }
     }
 
     /**
      * Test of parseValidCharsetString method, of class org.logicprobe.LogicMail.util.StringParser.
      */
     public void testParseValidCharsetString() {
         this.assertEquals("Accepting bad input", "ISO-8859-1",
             StringParser.parseValidCharsetString("foo"));
         this.assertEquals("Accepting ISO-8859-1", "ISO-8859-1",
             StringParser.parseValidCharsetString("iso-8859-1"));
         this.assertEquals("Accepting UTF-8", "UTF-8",
             StringParser.parseValidCharsetString("utf-8"));
         this.assertEquals("Accepting UTF-16BE", "UTF-16BE",
             StringParser.parseValidCharsetString("utf-16be"));
         this.assertEquals("Accepting US-ASCII", "US-ASCII",
             StringParser.parseValidCharsetString("us-ascii"));
     }
 
     /**
      * Test of decodeQuotedPrintable method, of class org.logicprobe.LogicMail.util.StringParser.
      * Basic test.
      */
     public void testDecodeQuotedPrintable1() {
         String text = "=A1Hol=E1 Se=F1or!";
         String expectedResult = "Hol Seor!";
         String result = StringParser.decodeQuotedPrintable(text.getBytes());
         assertEquals(expectedResult, result);
     }
 
     /**
      * Test of decodeQuotedPrintable method, of class org.logicprobe.LogicMail.util.StringParser.
      * Soft line-break test.
      */
     public void testDecodeQuotedPrintable2() {
         String text = "=A1Hol=E1 Se=F1or!=20=20H=\n" +
             "ow=20are=20you=20today?";
         String expectedResult = "Hol Seor!  How are you today?";
         String result = StringParser.decodeQuotedPrintable(text.getBytes());
         assertEquals(expectedResult, result);
     }
 
     /**
      * Test of encodeQuotedPrintable method, of class org.logicprobe.LogicMail.util.StringParser.
      * Basic test.
      */
     public void testEncodeQuotedPrintable1() {
         String text = "Hol Seor!";
         String expectedResult = "=A1Hol=E1=20Se=F1or!";
         String result = StringParser.encodeQuotedPrintable(text);
         assertEquals(expectedResult, result);
     }
 
     /**
      * Test of encodeQuotedPrintable method, of class org.logicprobe.LogicMail.util.StringParser.
      * Soft line-break test.
      */
     public void testEncodeQuotedPrintable2() {
         String text = "Hol Seor! ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 Hol Seor!";
         String expectedResult = "=A1Hol=E1=20Se=F1or!=20ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz=\r\n0123456789=20=A1Hol=E1=20Se=F1or!";
         String result = StringParser.encodeQuotedPrintable(text);
         assertEquals(expectedResult, result);
     }
 
     public void testParseRecipient() {
         String text = "John Doe <doej@generic.org>";
         String[] expected = new String[] { "John Doe", "doej@generic.org" };
         String[] actual = StringParser.parseRecipient(text);
         assertEquals("Unquoted normal case", expected, actual);
 
         text = "\"John Doe\" <doej@generic.org>";
         actual = StringParser.parseRecipient(text);
         assertEquals("Quoted normal case", expected, actual);
 
         text = "doej@generic.org";
         expected = new String[] { null, "doej@generic.org" };
         actual = StringParser.parseRecipient(text);
         assertEquals("Address-only normal case", expected, actual);
 
         text = "";
         expected = new String[] { null, "" };
         actual = StringParser.parseRecipient(text);
         assertEquals("Empty normal case", expected, actual);
     }
 
     public void testMergeRecipient() {
         String expected = "\"John Doe\" <doej@generic.org>";
         String actual = StringParser.mergeRecipient("John Doe",
                 "doej@generic.org");
         assertEquals("Name and address", expected, actual);
 
         expected = "doej@generic.org";
         actual = StringParser.mergeRecipient(null, "doej@generic.org");
         assertEquals("Address only 1", expected, actual);
 
         expected = "doej@generic.org";
         actual = StringParser.mergeRecipient("", "doej@generic.org");
         assertEquals("Address only 2", expected, actual);
 
         expected = "";
         actual = StringParser.mergeRecipient("John Doe", null);
         assertEquals("Name only 1", expected, actual);
 
         expected = "";
         actual = StringParser.mergeRecipient("John Doe", "");
         assertEquals("Name only 2", expected, actual);
 
         expected = "";
         actual = StringParser.mergeRecipient("", "");
         assertEquals("Empty", expected, actual);
     }
 
     public void testGetOptimalEncoding() {
         assertEquals(StringParser.ENCODING_7BIT, StringParser.getOptimalEncoding("Hello World"));
         assertEquals(StringParser.ENCODING_QUOTED_PRINTABLE, StringParser.getOptimalEncoding("Hol Seor!"));
         assertEquals(StringParser.ENCODING_BASE64, StringParser.getOptimalEncoding("\u1057\u1077\u1081\u1095\u1072\u1089"));
     }
     
     public void testMergePaths() {
         String expected = "file:///foo/bar";
         String actual = StringParser.mergePaths("file:///foo", "bar");
         assertEquals(expected, actual);
         
         actual = StringParser.mergePaths("file:///foo/", "bar");
         assertEquals(expected, actual);
         
         actual = StringParser.mergePaths("file:///foo", "/bar");
         assertEquals(expected, actual);
         
         actual = StringParser.mergePaths("file:///foo/", "/bar");
         assertEquals(expected, actual);
     }
     
     public Test suite() {
         TestSuite testSuite = new TestSuite("StringParser");
         
         testSuite.addTest(new StringParserTest("parseDateString", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseDateString(); }}));
         
         testSuite.addTest(new StringParserTest("createDateString", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testCreateDateString(); }}));
         
         testSuite.addTest(new StringParserTest("parseMailHeaders", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseMailHeaders(); }}));
         
         testSuite.addTest(new StringParserTest("parseMailHeadersNoBlank", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseMailHeadersNoBlank(); }}));
         
         testSuite.addTest(new StringParserTest("parseEncodedHeader", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseEncodedHeader(); }}));
         
         testSuite.addTest(new StringParserTest("parseEncodedHeaderMalformed", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseEncodedHeaderMalformed(); }}));
         
         testSuite.addTest(new StringParserTest("createEncodedHeaderQP", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testCreateEncodedHeaderQP(); }}));
         
         testSuite.addTest(new StringParserTest("createEncodedHeaderB64", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testCreateEncodedHeaderB64(); }}));
         
         testSuite.addTest(new StringParserTest("createEncodedRecipientHeader", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testCreateEncodedRecipientHeader(); }}));
         
         testSuite.addTest(new StringParserTest("parseTokenString", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseTokenString(); }}));
         
         testSuite.addTest(new StringParserTest("parseCsvString", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseCsvString(); }}));
         
         testSuite.addTest(new StringParserTest("parseValidCharsetString", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseValidCharsetString(); }}));
         
         testSuite.addTest(new StringParserTest("decodeQuotedPrintable1", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testDecodeQuotedPrintable1(); }}));
         
         testSuite.addTest(new StringParserTest("decodeQuotedPrintable2", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testDecodeQuotedPrintable2(); }}));
         
         testSuite.addTest(new StringParserTest("encodeQuotedPrintable1", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testEncodeQuotedPrintable1(); }}));
         
         testSuite.addTest(new StringParserTest("encodeQuotedPrintable2", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testEncodeQuotedPrintable2(); }}));
         
         testSuite.addTest(new StringParserTest("parseRecipient", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testParseRecipient(); }}));
         
         testSuite.addTest(new StringParserTest("mergeRecipient", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testMergeRecipient(); }}));
 
         testSuite.addTest(new StringParserTest("getOptimalEncoding", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testGetOptimalEncoding(); }}));
         
         testSuite.addTest(new StringParserTest("mergePaths", new TestMethod()
         { public void run(TestCase tc) { ((StringParserTest) tc).testMergePaths(); }}));
         
         return testSuite;
     }
 }
