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
 
	/*
	 * The modulo operation became negative which caused the following test to fail
	 * with an exception.
	 */
	public void testPadding() {
		Hasher hasher = new SHA256();
		assertEquals(
				"0798649893f9d0bf84befaeafa22a24749ef754eb1cac71229c83e65252ac3ca",
				hasher.hash("1b65c7ef6fb2081201b542c0d35165767bf5da9cf3869209de358c4a0475b4c9:6,ModuleService.setModulesIdsOnTab,[1,[[1,21],[2,3],[4,5,6]]]"));
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
