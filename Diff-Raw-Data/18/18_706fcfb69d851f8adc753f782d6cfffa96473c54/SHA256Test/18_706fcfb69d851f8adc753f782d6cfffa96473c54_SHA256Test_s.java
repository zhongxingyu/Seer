 package com.kk_electronic.kkportal.client;
 
 import com.google.gwt.junit.client.GWTTestCase;
 import com.kk_electronic.kkportal.core.security.Hasher;
 import com.kk_electronic.kkportal.core.security.SHA256;
 
 public class SHA256Test extends GWTTestCase {
 
 	@Override
 	public String getModuleName() {
 		return "com.kk_electronic.kkportal.KKPortalTest";
 	}
 
 	public void testSmallCall() {
 		Hasher hasher = new SHA256();
 		assertEquals(
 				"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
 				hasher.hash("abc"));
 	}
 
 	public void testNegative() {
 		Hasher hasher = new SHA256();
 		assertNotSame(
 				"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
 				hasher.hash("cab"));
 	}
 
 	public void testLargeCall() {
 		Hasher hasher = new SHA256();
 		assertEquals(
 				"e4a079c6ffb88fd1a34f0b5d85304b10d99c55fabebd519b7f61b081a58a7f2a",
 				hasher.hash("2,TabsService.getTabs,[{\"username\":\"Jes\"}]"));
 	}
 
 	public void testDoubleCall() {
 		Hasher hasher = new SHA256();
 		String hash1 = hasher.hash("abc");
 		String hash2 = hasher.hash("abc");
 		assertNotNull(hash1);
 		assertNotNull(hash2);
 		assertEquals(hash1, hash2);
 	}
 }
