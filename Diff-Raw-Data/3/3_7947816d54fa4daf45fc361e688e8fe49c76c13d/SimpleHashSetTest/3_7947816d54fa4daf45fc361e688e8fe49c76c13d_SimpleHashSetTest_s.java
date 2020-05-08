 package org.bifrost;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.framework.Test;
 import org.bifrost.simplehashset.SimpleHashSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 public class SimpleHashSetTest extends TestCase 
 { 
     public SimpleHashSetTest() {
         super("SimpleHashSetTest");
     }
 
     public static Test suite() {
 	return new TestSuite(SimpleHashSetTest.class);
     }
 
 
     public void testAdd() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	assert(hashSet.add(1));
 	assert(hashSet.add(2));
 	assert(!hashSet.add(2));
     }
 
     
     // A class used in testAddPreserves
     static final class TestObject { 
 	private final String name;
 	
 	public String getName() { return name; }
 	
 	
 	TestObject(String name) { 
 	    this.name = name;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 	    return o instanceof TestObject;
 	}
 	
 	@Override
 	public int hashCode() {
 	    return 42;
 	}
     }
 
     // Tests that if two .equals() objects are added to a hashset, the first one is kept.
     public void testAddPreserves() {
 	SimpleHashSet<TestObject> hashSet = new SimpleHashSet<TestObject>();
 	
 	TestObject in = new TestObject("Inside"), out = new TestObject("Outside");
 
 	assert(hashSet.add(in));
 	assert(!hashSet.add(out));
 	assertEquals(hashSet.iterator().next().getName(), "Inside");
     }
     
     public void testNull() { 
 	SimpleHashSet<TestObject> hashSet = new SimpleHashSet<TestObject>();
 	assert(!hashSet.contains(null));
 	assert(hashSet.add(null));
 	assert(hashSet.contains(null));
 	assert(!hashSet.add(null));
 	assert(hashSet.remove(null));
 	assert(!hashSet.remove(null));
     }
 
     public void testRemove() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(1); hashSet.add(2); hashSet.add(3);
 	assert(hashSet.contains(1)); assert(hashSet.contains(2)); assert(hashSet.contains(3));
 	assert(hashSet.remove(1)); assert(hashSet.remove(2)); 
 	assert(!hashSet.remove(1)); 
 	assert(hashSet.add(1)); assert(hashSet.remove(1)); 
 	assert(!hashSet.remove(1)); 
 	assert(hashSet.contains(3)); assert(!hashSet.contains(1)); assert(!hashSet.contains(2));
     }
 
     public void testContains() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	assert(!hashSet.contains(1));
 	hashSet.add(1);
 	assert(hashSet.contains(1));
 	hashSet.remove(1);
 	assert(!hashSet.contains(1));
     }
 
     public void testSize() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	assertEquals(hashSet.size(), 0);
 	hashSet.add(1); hashSet.add(2); hashSet.add(3); hashSet.add(4);
 	assertEquals(hashSet.size(), 4);
 	hashSet.add(4);
 	assertEquals(hashSet.size(), 4);
 	hashSet.remove(4); hashSet.remove(3);
 	assertEquals(hashSet.size(), 2);
 	hashSet.remove(2); hashSet.remove(1);
 	assertEquals(hashSet.size(), 0);
     }
 
     public void testClear() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(256); hashSet.add(257); hashSet.add(258);
 	assertEquals(hashSet.size(), 3);
 	hashSet.clear();
 	assertEquals(hashSet.size(), 0);
     }
     
     public void testIsEmpty() {
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	assert(hashSet.isEmpty());
 	hashSet.add(256); hashSet.add(257);
 	assert(!hashSet.isEmpty());
 	hashSet.clear();
 	assert(hashSet.isEmpty());
     }
     
     public void testClone() { 
 	SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(-42); hashSet.add(1024); hashSet.add(2048); 
 	SimpleHashSet<Integer> clone1 = (SimpleHashSet<Integer>) hashSet.clone();
 	assertEquals(clone1, hashSet);
 	hashSet.add(4096);
 	assert(!clone1.equals(hashSet));
     }
 
     interface Application {
 	public void apply();
     }
 
     void throwTester(Class<?> c, Application a) {
 	try {
 	    a.apply();
 	} catch (Exception e) {
 	    assertEquals(e.getClass(), c);
 	}
     }
     
     public void testIteratorThrows() { 
 	final SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(65536); hashSet.add(131072); hashSet.add(262144);
 	
 	throwTester(IllegalStateException.class, new Application() { 
 		public void apply() {
 		    hashSet.iterator().remove();
 		}});
 
 	final Iterator<Integer> iter = hashSet.iterator();
 	iter.next();
 	iter.remove();
 	
 	throwTester(IllegalStateException.class, new Application() { 
 	    public void apply() {
 		iter.remove();
 	    }
 	});
 
 	final Iterator<Integer> iter2 = hashSet.iterator();
 
 	throwTester(NoSuchElementException.class, new Application() { 
 	    public void apply() {
 		while(true) iter2.next();
 	    }
 	});
     }
 
     public void testIterator() { 
 	final SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(65536); hashSet.add(131072); hashSet.add(262144);
 	
 	ArrayList<Integer> complement = new ArrayList<Integer>(hashSet);
 	assert(!complement.isEmpty());
 	for(Integer i: hashSet) {
 	    complement.remove(i);
 	}
 	assert(complement.isEmpty());
     }
 
     public void testIteratorRemove() {
 	final SimpleHashSet<Integer> hashSet = new SimpleHashSet<Integer>();
 	hashSet.add(65536); hashSet.add(131072); hashSet.add(262144);
 	
 	Iterator<Integer> iter = hashSet.iterator();
 	
 	assertEquals(hashSet.size(), 3);
 	iter.next();
 	iter.remove();
 	assertEquals(hashSet.size(), 2);
     }
 
     // Here should go tests of inherited template methods, which use the above operations, but I won't add them 
     // for this exercise.
 }
