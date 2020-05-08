 // ========================================================================
 // Copyright 2007-2008 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 package org.cipango;
 
 import javax.servlet.sip.Address;
 import javax.servlet.sip.SipURI;
 
 import junit.framework.TestCase;
 
 public class AddressTest extends TestCase
 {
 	public void testEquals() throws Exception
 	{
 		Address a1 = new NameAddr("<sip:%61lice@bea.com;transport=TCP;lr>;q=0.5");
 		Address a2 = new NameAddr("<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601");
 		Address a3 = new NameAddr("<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.5;expires=3601");
 		Address a4 = new NameAddr("<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.5;expires=3601;param");
 		Address a5 = new NameAddr("\"display name\" <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.5;expires=3601;param");
 		
 		assertFalse(a1.equals(a2));
 		assertTrue(a1.equals(a3));
 
 		assertTrue(a3.equals(a1));
 		assertTrue(a1.equals(a4));
 		assertTrue(a4.equals(a1));
 		assertTrue(a1.equals(a5));
 		assertTrue(a5.equals(a1));
 	}
 	
 	public void testSetValue() throws Exception
 	{
 		Address a1 = new NameAddr("<sip:alice@nexcom.fr;transport=UDP>;q=0.5");
 		a1.setValue("Bob <sip:bob@nexcom.fr;transport=TCP>");
 		assertEquals("Bob <sip:bob@nexcom.fr;transport=TCP>", a1.getValue());
 		assertEquals("bob", ((SipURI) a1.getURI()).getUser());
 		assertEquals("Bob", a1.getDisplayName());
 		assertEquals("Bob <sip:bob@nexcom.fr;transport=TCP>;q=0.5", a1.toString());
 		assertEquals("0.5", a1.getParameter("q"));
 		try { a1.setValue("aa"); fail(); } catch (Exception e) {};
 		
 		a1.setValue("Bob <sip:bob@nexcom.fr;transport=TCP>");
 		a1.setValue("<sip:carol@nexcom.fr>");
 		assertNull(a1.getDisplayName());
 	}
 }
