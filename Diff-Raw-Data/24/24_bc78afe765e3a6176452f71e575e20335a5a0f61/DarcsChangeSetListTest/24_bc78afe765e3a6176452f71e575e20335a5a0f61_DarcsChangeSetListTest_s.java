 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jenkinsci.plugins.darcs;
 
 import junit.framework.TestCase;
 import org.junit.Ignore;
 import java.util.List;
 import java.util.ArrayList;
 import hudson.Util;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsChangeSetListTest extends TestCase {
 
 	public DarcsChangeSetListTest(String name) {
 		super(name);
 	}
 
 	protected static DarcsChangeSet createChangeSet(String suffix) {
 		DarcsChangeSet cs = new DarcsChangeSet();
 		cs.setAuthor("author" + suffix);
 		cs.setDate("date" + suffix);
 		cs.setLocalDate("local_date" + suffix);
 		cs.setHash("hash" + suffix);
 		cs.setInverted(false);
 
 		return cs;
 	}
 	
 	public void testIsEmpty() {
 		DarcsChangeSetList sut;
 
 		sut = new DarcsChangeSetList(null, new ArrayList<DarcsChangeSet>());
 		assertTrue(sut.isEmptySet());
 		assertEquals(0, sut.size());
 
 		List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
 		list.add(createChangeSet("1"));
 		list.add(createChangeSet("2"));
 		list.add(createChangeSet("3"));
 		sut = new DarcsChangeSetList(null, list);
 		assertFalse(sut.isEmptySet());
 		assertEquals(3, sut.size());
 	}
 
 	public void testDigest() {
 		DarcsChangeSet cs1 = createChangeSet("1");
 		DarcsChangeSet cs2 = createChangeSet("2");
 		DarcsChangeSet cs3 = createChangeSet("3");
 
 		List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
 		list.add(createChangeSet("1"));
 		list.add(createChangeSet("2"));
 		list.add(createChangeSet("3"));
 		DarcsChangeSetList sut = new DarcsChangeSetList(null, list);
 		assertEquals(Util.getDigestOf(cs1.getHash() + cs2.getHash() + cs3.getHash()),
 					 sut.digest());
 	}
 
 	public void testGetKind() {
 		DarcsChangeSetList sut = new DarcsChangeSetList(null, new ArrayList<DarcsChangeSet>());
 		assertEquals("darcs", sut.getKind());
 	}
 
 	public void testEquals() {
 		DarcsChangeSet cs1 = createChangeSet("1");
 		DarcsChangeSet cs2 = createChangeSet("2");
 		DarcsChangeSet cs3 = createChangeSet("3");
 		List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>(),
 							 list2 = new ArrayList<DarcsChangeSet>(),
 							 list3 = new ArrayList<DarcsChangeSet>();
 		list1.add(cs1);
 		list1.add(cs2);

 		list2.add(cs1);
 		list2.add(cs2);

 		list3.add(cs1);
 		list3.add(cs2);
 		list3.add(cs3);
 
 		// equals to itself
		assertTrue(list1.equals(list1));
		assertTrue(list2.equals(list2));
 		assertTrue(list3.equals(list3));
 		// list1 and list2 are euqal
		assertTrue(list1.equals(list2));
		assertTrue(list2.equals(list1));
 		// list3 has one more patch
		assertFalse(list1.equals(list3));
		assertFalse(list2.equals(list3));
 	}
 }
