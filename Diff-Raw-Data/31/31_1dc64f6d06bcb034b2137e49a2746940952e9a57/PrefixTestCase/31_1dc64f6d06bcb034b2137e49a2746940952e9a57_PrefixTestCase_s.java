 package cz.muni.fi.publishsubscribe.countingtree.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cz.muni.fi.publishsubscribe.countingtree.ternarysearchtree.TernarySearchTree;
 
 public class PrefixTestCase {
 
 	private static final int COM_PREFIX_VALUE = 1;
 	private static final int COM_JAVA_PREFIX_VALUE = 2;
 	private static final int COM_JAVA_GLASSFISH_PREFIX_VALUE = 3;
 	private static final int NET_PREFIX_VALUE = 4;
 	private static final int CZ_PREFIX_VALUE = 5;
 	private static final int CZ_MUNI_PREFIX_VALUE = 6;
 	private static final int CZ_MUNI_FI_PREFIX_VALUE = 7;
 	private static final int CZ_MUNI_PED_PREFIX_VALUE = 8;
 
 	private static final String COM_PREFIX = "com";
 	private static final String COM_JAVA_PREFIX = "com.java";
 	private static final String COM_JAVA_GLASSFISH_PREFIX = "com.java.glassfish";
 	private static final String NET_PREFIX = "net";
 	private static final String CZ_PREFIX = "cz";
 	private static final String CZ_MUNI_PREFIX = "cz.muni";
 	private static final String CZ_MUNI_FI_PREFIX = "cz.muni.fi";
 	private static final String CZ_MUNI_PED_PREFIX = "cz.muni.ped";
 
 	private TernarySearchTree<Integer> tree = new TernarySearchTree<>();
 
 	@Before
 	public void prepareTree() {
 		tree.put(COM_PREFIX, COM_PREFIX_VALUE);
 		tree.put(COM_JAVA_PREFIX, COM_JAVA_PREFIX_VALUE);
 		tree.put(COM_JAVA_GLASSFISH_PREFIX, COM_JAVA_GLASSFISH_PREFIX_VALUE);
 		tree.put(NET_PREFIX, NET_PREFIX_VALUE);
 		tree.put(CZ_PREFIX, CZ_PREFIX_VALUE);
 		tree.put(CZ_MUNI_PREFIX, CZ_MUNI_PREFIX_VALUE);
 		tree.put(CZ_MUNI_FI_PREFIX, CZ_MUNI_FI_PREFIX_VALUE);
 		tree.put(CZ_MUNI_PED_PREFIX, CZ_MUNI_PED_PREFIX_VALUE);
 	}
 
 	@Test
 	public void testEmptyTree() {
 		TernarySearchTree<Integer> emptyTree = new TernarySearchTree<>();
 		List<Integer> integers = emptyTree.getAllPrefixes("foo");
 		assertEquals(0, integers.size());
 	}
 	
 	@Test
 	public void testNoPrefixMatched() {
 		List<Integer> integers = tree.getAllPrefixes("foo");
 		assertEquals(0, integers.size());
 	}
 	
 	@Test
 	public void testJavaGlassfish() {
 		List<Integer> integers = tree.getAllPrefixes(COM_JAVA_GLASSFISH_PREFIX);
 		assertEquals(3, integers.size());
 		assertTrue(integers.contains(COM_PREFIX_VALUE));
 		assertTrue(integers.contains(COM_JAVA_PREFIX_VALUE));
 		assertTrue(integers.contains(COM_JAVA_GLASSFISH_PREFIX_VALUE));
 	}
 	
 	@Test
 	public void testJava() {
 		List<Integer> integers = tree.getAllPrefixes(COM_JAVA_PREFIX);
 		assertEquals(2, integers.size());
 		assertTrue(integers.contains(COM_PREFIX_VALUE));
 		assertTrue(integers.contains(COM_JAVA_PREFIX_VALUE));
 	}
 	
 	@Test
 	public void testJavaFoo() {
 		List<Integer> integers = tree.getAllPrefixes(COM_JAVA_PREFIX + ".foo");
 		assertEquals(2, integers.size());
 		assertTrue(integers.contains(COM_PREFIX_VALUE));
 		assertTrue(integers.contains(COM_JAVA_PREFIX_VALUE));
 	}
 	
 	@Test
 	public void testNetSfJsonJsonObject() {
 		List<Integer> integers = tree.getAllPrefixes(NET_PREFIX + ".sf.json.jsonobject");
 		assertEquals(1, integers.size());
 		assertTrue(integers.contains(NET_PREFIX_VALUE));
 	}
 	
 	@Test
 	public void testCzMuniFiPublishsubscribeCountingtree() {
 		List<Integer> integers = tree.getAllPrefixes(CZ_MUNI_FI_PREFIX + ".publishsubscribe.countingtree");
 		assertEquals(3, integers.size());
 		assertTrue(integers.contains(CZ_PREFIX_VALUE));
 		assertTrue(integers.contains(CZ_MUNI_PREFIX_VALUE));
 		assertTrue(integers.contains(CZ_MUNI_FI_PREFIX_VALUE));
 	}
 	
 	@Test
 	public void testCzMuniEcon() {
 		List<Integer> integers = tree.getAllPrefixes(CZ_MUNI_PREFIX + ".econ");
 		assertEquals(2, integers.size());
 		assertTrue(integers.contains(CZ_PREFIX_VALUE));
 		assertTrue(integers.contains(CZ_MUNI_PREFIX_VALUE));
 	}
 
 }
