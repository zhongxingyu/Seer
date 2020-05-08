 /**
  * Copyright 2012 John Brainard
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.brainardphotography.gravatar;
 
 import static org.junit.Assert.*;
 import static com.brainardphotography.gravatar.GravatarTestUtils.*;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.junit.Test;
 
 import com.brainardphotography.gravatar.contact.PCContact;
 import com.brainardphotography.gravatar.contact.PCEmail;
 
 public class TestGravatarProfileURL {
 	private static final Logger logger = Logger.getLogger(TestGravatarProfileURL.class.getName());
 
 	@Test
 	public void testSimpleURL() {
 		final String hash = getEmailHash("test@test.com");
 		final String expected = "http://www.gravatar.com/" + hash;
 
 		GravatarURL gravatarURL = new GravatarProfileURL("test@test.com");
 		String toString = gravatarURL.toString();
 		assertEquals(expected, toString);
 	}
 
 	@Test
 	public void testFormatURLWithSizeParameter() {
 		final String hash = getEmailHash("test@test.com");
 		final String expected = "http://www.gravatar.com/" + hash + ".qr?s=200";
 
 		GravatarProfileURL gravatarURL = new GravatarProfileURL("test@test.com");
 		gravatarURL.setSize(200);
 		
 		String toString = gravatarURL.toString(GravatarProfileFormat.QR_CODE);
 		assertEquals(expected, toString);
 	}
 
 	@Test
 	public void testGetProfileText() throws IOException {
 		GravatarProfileURL gravatarURL = new GravatarProfileURL("jfbrainard@gmail.com");
 		String url = gravatarURL.toString(GravatarProfileFormat.DEFAULT);
 		String toString = gravatarURL.getText(GravatarProfileFormat.DEFAULT);
 
 		assertNotNull(toString);
 
 		logger.log(Level.INFO, url);
 	}
 
 	@Test
 	public void testGetProfileJSON() throws IOException {
 		GravatarProfileURL gravatarURL = new GravatarProfileURL("jfbrainard@gmail.com");
 		String url = gravatarURL.toString(GravatarProfileFormat.JSON);
 		String toString = gravatarURL.getText(GravatarProfileFormat.JSON);
 
 		assertNotNull(toString);
 
 		logger.log(Level.INFO, url);
 		logger.log(Level.INFO, toString);
 	}
 
 	@Test(expected=java.lang.IllegalArgumentException.class)
 	public void testGetProfileQRCodeAsText() throws IOException {
 		GravatarProfileURL gravatarURL = new GravatarProfileURL("jfbrainard@gmail.com");
 		String url = gravatarURL.toString(GravatarProfileFormat.QR_CODE);
 		String toString = gravatarURL.getText(GravatarProfileFormat.QR_CODE);
 
 		assertNull(toString);
 		logger.log(Level.INFO, "testGetProfileQRCodeAsText: " + url);
 	}
 	
 	@Test
 	public void testGetProfileQRCodeAsBytes() throws IOException {
 		GravatarProfileURL gravatarURL = new GravatarProfileURL("jfbrainard@gmail.com");
 		gravatarURL.setSize(200);
 		String url = gravatarURL.toString(GravatarProfileFormat.QR_CODE);
 		byte[] data = gravatarURL.getBytes(GravatarProfileFormat.QR_CODE);
 
 		assertNotNull(data);
 		assertTrue(data.length > 0);
 		
 		logger.log(Level.INFO, "testGetProfileQRCodeAsBytes: " + url);
 		logger.log(Level.INFO, "testGetProfileQRCodeAsBytes: " + data.length);
 	}
 
 	@Test
 	public void testGetContact() throws IOException {
 		final String email = "jfbrainard@gmail.com";
 		GravatarProfileURL gravatarURL = new GravatarProfileURL(email);
 
 		PCContact contact = gravatarURL.getContact();
 
		assertNotNull("Expecting non-null contact", contact);
		assertNotNull("Expecting non-null contact.getEmails()", contact.getEmails());
 
 		PCEmail pcEmail = contact.getEmails().get(0);
 		assertEquals(email, pcEmail.getValue());
 	}
 }
