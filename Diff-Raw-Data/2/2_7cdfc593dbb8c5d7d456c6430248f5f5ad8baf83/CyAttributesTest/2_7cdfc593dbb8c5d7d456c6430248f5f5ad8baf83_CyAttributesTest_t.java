 /*
  File: CyAttributesTest.java
 
  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 package cytoscape.data;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesImpl;
 
 import org.cytoscape.equations.EqnCompiler;
 
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * Tests Implementation of CyAttributes.
  *
  */
 public class CyAttributesTest extends TestCase {
 	private CyAttributes cyAttributes;
 	private static final String DUMMY_ID = "id_123";
 	private static final String DUMMY_BOOLEAN_ATTRIBUTE = "attribute1";
 	private static final String DUMMY_STRING_ATTRIBUTE = "attribute2";
 	private static final String DUMMY_LIST_ATTRIBUTE = "attribute3";
 	private static final String DUMMY_MAP_ATTRIBUTE = "attribute4";
 
 	/**
 	 * Set things up.
 	 *
 	 * @throws Exception
 	 *             All Exceptions.
 	 */
 	protected void setUp() throws Exception {
 		cyAttributes = new CyAttributesImpl();
 	}
 
 	public void testBug1363() {
 		try {
 		boolean b = cyAttributes.deleteAttribute("non-existent-attribute.asdffdsa");
 		assertFalse(b);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("deleting non-existent attr shouldn't have caused exception");
 		}
 	}
 
 	/**
 	 * Tests Boolean Values.
 	 */
 	public void testBooleanValue() {
 		// Verify that attribute does not (yet) exist
 		boolean exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(false, exists);
 
 		// Since the attribute does not yet exist, this should be null
 		Boolean value = cyAttributes.getBooleanAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(null, value);
 
 		// This should be null too
 		Double value2 = cyAttributes.getDoubleAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(null, value2);
 
 		// Try setting an attribute with a null ID; should fail
 		try {
 			cyAttributes.setAttribute(null, DUMMY_BOOLEAN_ATTRIBUTE, new Boolean(true));
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Try setting an attribute with a null attribute name; should fail
 		try {
 			cyAttributes.setAttribute(DUMMY_ID, null, new Boolean(true));
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Set a Boolean Attribute Value; should work
 		cyAttributes.setAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE, new Boolean(true));
 
 		// Verify that attribute now exists
 		exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(true, exists);
 
 		// Verify Type
 		byte type = cyAttributes.getType(DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(CyAttributes.TYPE_BOOLEAN, type);
 
 		// Verify value stored
 		value = cyAttributes.getBooleanAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(true, value.booleanValue());
 
 		// Try getting it as an Integer value; this should trigger
 		// a ClassCastException.
 		try {
 			Integer valueInt = cyAttributes.getIntegerAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 			fail("ClassCastException should have been thrown.");
 		} catch (ClassCastException e) {
 			assertTrue(e != null);
 		}
 
 		// Try setting an integer value; this should trigger an
 		// IllegalArgumentException
 		try {
 			cyAttributes.setAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE, new Integer(5));
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Check out the attribute names
 		String[] attribs = cyAttributes.getAttributeNames();
 		assertEquals(1, attribs.length);
 		assertEquals(DUMMY_BOOLEAN_ATTRIBUTE, attribs[0]);
 
 		// Now delete the attribute
 		boolean success = cyAttributes.deleteAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(true, success);
 
 		// Verify that attribute no longer exists, but that its type remains
 		exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(false, exists);
 		type = cyAttributes.getType(DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(CyAttributes.TYPE_BOOLEAN, type);
 		assertEquals(CyAttributes.TYPE_BOOLEAN, type);
 
 		// Now try deleting all attributes of this name
 		success = cyAttributes.deleteAttribute(DUMMY_BOOLEAN_ATTRIBUTE);
 
 		// Verify that type is now TYPE_UNDEFINED
 		type = cyAttributes.getType(DUMMY_BOOLEAN_ATTRIBUTE);
 		assertEquals(CyAttributes.TYPE_UNDEFINED, type);
 	}
 
 	/**
 	 * Tests String Values.
 	 */
 	public void testStringValue() {
 		// Verify that attribute does not (yet) exist
 		boolean exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE);
 		assertEquals(false, exists);
 
 		// Set a String Attribute Value
 		cyAttributes.setAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE, new String("Cytoscape"));
 
 		// Verify that attribute now exists
 		exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE);
 		assertEquals(true, exists);
 
 		// Verify Type
 		byte type = cyAttributes.getType(DUMMY_STRING_ATTRIBUTE);
 		assertEquals(CyAttributes.TYPE_STRING, type);
 
 		// Verify value stored
 		String value = cyAttributes.getStringAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE);
 		assertEquals("Cytoscape", value);
 
 		// Try getting it as an Integer value; this should trigger
 		// a ClassCastException.
 		try {
 			Integer valueInt = cyAttributes.getIntegerAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE);
 			fail("ClassCastException should have been thrown.");
 		} catch (ClassCastException e) {
 			assertTrue(e != null);
 		}
 
 		// Try setting an integer value; this should trigger an
 		// IllegalArgumentException
 		try {
 			cyAttributes.setAttribute(DUMMY_ID, DUMMY_STRING_ATTRIBUTE, new Integer(5));
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Check out the attribute names
 		String[] attribs = cyAttributes.getAttributeNames();
 		assertEquals(1, attribs.length);
 		assertEquals(DUMMY_STRING_ATTRIBUTE, attribs[0]);
 	}
 
 	/**
 	 * Tests Simple Lists.
 	 */
 	public void testSimpleLists() {
 		// First, try setting a not-so simple list
 		List list = new ArrayList();
 		list.add(new Integer(5));
 		list.add(new String("Cytoscape"));
 
 		// Try setting the list as null; this should fail
 		try {
			cyAttributes.setListAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE, (List)null);
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Try setting the list; this should fail b/c we have mixed data types
 		try {
 			cyAttributes.setListAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE, list);
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Try again with a valid list
 		list = new ArrayList();
 		list.add(new Integer(5));
 		list.add(new Integer(6));
 		cyAttributes.setListAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE, list);
 
 		// Verify type
 		byte type = cyAttributes.getType(DUMMY_LIST_ATTRIBUTE);
 		assertEquals(type, CyAttributes.TYPE_SIMPLE_LIST);
 
 		// Verify value exists
 		boolean exists = cyAttributes.hasAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE);
 		assertEquals(true, exists);
 
 		// Get Stored value as a Simple Map; this should fail
 		try {
 			Map map = cyAttributes.getMapAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE);
 			fail("ClassCastException should have been thrown.");
 		} catch (ClassCastException e) {
 			assertTrue(e != null);
 		}
 
 		// Get the list back, and verify its contents
 		List storedList = cyAttributes.getListAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE);
 		assertEquals(2, storedList.size());
 
 		Integer int0 = (Integer) storedList.get(0);
 		Integer int1 = (Integer) storedList.get(1);
 		assertEquals(5, int0.intValue());
 		assertEquals(6, int1.intValue());
 
 		// Try storing an Empty List; previously, this resulted in
 		// a NoSuchElementException.
 		list = new ArrayList();
 		cyAttributes.setListAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE, list);
 	}
 
 	/**
 	 * Tests Simple Maps.
 	 */
 	public void testSimpleMaps() {
 		// First, try setting a not-so simple map
 		// The following map is considered invalid because all keys must
 		// be of type String.
 		Map map = new HashMap();
 		map.put(new Integer(1), new String("One"));
 		map.put(new Integer(2), new String("Two"));
 
 		// This should fail, b/c of invalid keys
 		try {
 			cyAttributes.setMapAttribute(DUMMY_ID, DUMMY_MAP_ATTRIBUTE, map);
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Now, try another invalid map. This map is invalid because the
 		// values are not all of one type
 		map = new HashMap();
 		map.put(new String("first"), new String("One"));
 		map.put(new String("second"), new Integer(2));
 
 		// This should fail too, b/c of invalid values
 		try {
 			cyAttributes.setMapAttribute(DUMMY_ID, DUMMY_MAP_ATTRIBUTE, map);
 			fail("IllegalArgumentException should have been thrown.");
 		} catch (IllegalArgumentException e) {
 			assertTrue(e != null);
 		}
 
 		// Now, try a valid map
 		map = new HashMap();
 		map.put(new String("first"), new Integer(1));
 		map.put(new String("second"), new Integer(2));
 		cyAttributes.setMapAttribute(DUMMY_ID, DUMMY_MAP_ATTRIBUTE, map);
 
 		// Verify type
 		byte type = cyAttributes.getType(DUMMY_MAP_ATTRIBUTE);
 		assertEquals(CyAttributes.TYPE_SIMPLE_MAP, type);
 
 		// Get Stored value as a Simple List; this should fail
 		try {
 			List list = cyAttributes.getListAttribute(DUMMY_ID, DUMMY_MAP_ATTRIBUTE);
 			fail("ClassCastException should have been thrown.");
 		} catch (ClassCastException e) {
 			assertTrue(e != null);
 		}
 
 		// Get map back, and verify contents
 		Map storedMap = cyAttributes.getMapAttribute(DUMMY_ID, DUMMY_MAP_ATTRIBUTE);
 		assertEquals(2, storedMap.keySet().size());
 
 		Set keySet = storedMap.keySet();
 
 		assertTrue(keySet.contains("first"));
 		assertEquals(1, storedMap.get("first"));
 		assertTrue(keySet.contains("second"));
 		assertEquals(2, storedMap.get("second"));
 
 		// Try storing an Empty Map; previously, this resulted in
 		// a NoSuchElementException.
 		map = new HashMap();
 		cyAttributes.setMapAttribute(DUMMY_ID, DUMMY_LIST_ATTRIBUTE, map);
 	}
 
 	/**
 	 * Tests attribute descriptions.
 	 */
 	public void testAttributeDescriptions() {
 		//  Try setting an attribute description, and verify that you can
 		//  get it back.
 		cyAttributes.setAttributeDescription("attribute1", "sample description");
 
 		String description = cyAttributes.getAttributeDescription("attribute1");
 		assertEquals("sample description", description);
 
 		//  Try getting an attribute description for an unknown attribute
 		//  verify that return value is null
 		assertEquals(null, cyAttributes.getAttributeDescription("attribute2"));
 	}
 
 	/**
 	 * Tests user interaction flags.
 	 */
 	public void testUserInteractionFlags() {
 		String sampleAttribute = "attribute1";
 
 		//  Test that defaults are working.  By default, attributes should
 		//  be visible and editable by the end user.
 		boolean visibleFlag = cyAttributes.getUserVisible(sampleAttribute);
 		assertEquals(true, visibleFlag);
 
 		boolean editableFlag = cyAttributes.getUserVisible(sampleAttribute);
 		assertEquals(true, editableFlag);
 
 		//  Try making attribute user invisible
 		cyAttributes.setUserVisible(sampleAttribute, false);
 		visibleFlag = cyAttributes.getUserVisible(sampleAttribute);
 		assertEquals(false, visibleFlag);
 
 		//  Switch back to user visible
 		cyAttributes.setUserVisible(sampleAttribute, true);
 		visibleFlag = cyAttributes.getUserVisible(sampleAttribute);
 		assertEquals(true, visibleFlag);
 
 		//  Try making attribute user non-editable
 		cyAttributes.setUserEditable(sampleAttribute, false);
 		editableFlag = cyAttributes.getUserEditable(sampleAttribute);
 		assertEquals(false, editableFlag);
 
 		//  Switch back to user editable
 		cyAttributes.setUserEditable(sampleAttribute, true);
 		editableFlag = cyAttributes.getUserEditable(sampleAttribute);
 		assertEquals(true, editableFlag);
 	}
 
 	public void testGetLastEquationError() {
 		final EqnCompiler compiler = new EqnCompiler();
 		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 
 		// First a case that should result in an error message...
 		assertTrue(compiler.compile("=1/0", attribNameToTypeMap));
 		cyAttributes.setAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE, compiler.getEquation());
 		assertNull(cyAttributes.getAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE));
 		assertNotNull(cyAttributes.getLastEquationError());
 
 		// ...and now a case that should not result in an error message.
 		assertTrue(compiler.compile("=1/1", attribNameToTypeMap));
 		cyAttributes.setAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE, compiler.getEquation());
 		assertNotNull(cyAttributes.getAttribute(DUMMY_ID, DUMMY_BOOLEAN_ATTRIBUTE));
 		assertNull(cyAttributes.getLastEquationError());
 	}
 
 	/**
 	 * Runs just this one unit test.
 	 */
 	public static void main(String[] args) {
 		junit.textui.TestRunner.run(CyAttributesTest.class);
 	}
 }
