 package org.bifrost;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.framework.Test;
 
 import java.util.Random;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.List;
 import java.util.Collection;
 import java.util.Set;
import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Collections;
 import org.bifrost.trie.Trie;
 
 public class TrieTest extends TestCase 
 { 
     public TrieTest() {
 	super("TrieTest");
     }
 
     public static Test suite() { 
 	return new TestSuite(TrieTest.class);
     }
 
     public void testAdd() { 
 	Trie<String, String> trie = new Trie<String, String>();
 	assertEquals(null, trie.put("Hello there", "I am"));
 	assertEquals(null, trie.put("Hi me", "its here"));
 	assertEquals(null, trie.put("Hello its me", "Fine by me"));
 	assertEquals("Fine by me", trie.get("Hello its me"));
 	assertEquals("Fine by me", trie.put("Hello its me", "Not so fine"));
 	assertEquals("Not so fine", trie.get("Hello its me"));
     }
     
     /**
      * Test addition and removing. Including removing the same key twice. 
      */
     public void testRemove() { 
 	Trie<String, Integer> trie = new Trie<String, Integer>();
 	trie.put("String1", 1); trie.put("String23", 23); 
 	trie.put("StrING45", 45); trie.put("String15", 15);
 
 	assertEquals(new Integer(15), trie.get("String15"));
 	assert(!trie.isEmpty());
 	assertEquals(new Integer(15), trie.remove("String15"));
 	assertEquals(new Integer(23), trie.remove("String23"));
 	assertEquals(null, trie.get("String15"));
 	assert(!trie.isEmpty());
 	assertEquals(2, trie.size());
 
 	assertEquals(new Integer(1), trie.remove("String1"));
 	assertEquals(null, trie.remove("String1"));
 	assertEquals(new Integer(45), trie.remove("StrING45"));
 	assertEquals(0, trie.size());
 	assert(trie.isEmpty());
 	
 	assertEquals(null, trie.put("String1", 23));
     }
 
     public void testEntrySet() { 
 	Random rng = new Random();
 	ArrayList<Integer> al = new ArrayList<Integer>(100);
 	Trie<String, Integer> trie = new Trie<String, Integer>();
 
 	for (int i = 0; i < 100; ++i) {
 	    Integer ni = rng.nextInt(100000000);
 	    al.add(ni);
 	    trie.put(ni.toString(), ni);
 	}
 	
 	Set<Map.Entry<String, Integer>> set = trie.entrySet();
 	Iterator<Map.Entry<String, Integer>> iter = set.iterator();
 	assert(iter.hasNext());
 	assertEquals(set.size(), 100);
 	
 	trie.put("", 100);
 	while(iter.hasNext()) {
 	    al.remove(new Integer(iter.next().getValue()));
 	}
 	assert(al.isEmpty());
 	
 	assertEquals(101, trie.size());
 	assertEquals(new Integer(100), trie.entrySet().iterator().next().getValue());
     }
     
     /**
      * test containsKey and containsValue
      */
     public void testContains() {
 	Random rng = new Random();
 	ArrayList<Integer> al = new ArrayList<Integer>(100);
 	Trie<String, Integer> trie = new Trie<String, Integer>();
 
 	for (int i = 0; i < 100; ++i) {
 	    Integer ni = rng.nextInt(100000000);
 	    al.add(ni);
 	    trie.put(ni.toString(), ni);
 	}
 	
 	for(Integer i: al) { 
 	    assert(trie.containsKey(i.toString()));
 	    assert(trie.containsValue(i));
 	}
 
 	for (Integer i = 0 ; i < 1000; ++i ) {
 	    assert (al.contains(i) || (!trie.containsKey(i.toString()) && !trie.containsValue(i)) );
 	}
     }
 
     public void testKeyAndValueSet() { 
 	Random rng = new Random();
 	ArrayList<Integer> al = new ArrayList<Integer>(100);
 	Trie<String, Integer> trie = new Trie<String, Integer>();
 
 	for (int i = 0; i < 100; ++i) {
 	    Integer ni = rng.nextInt(100000000);
 	    al.add(ni);
 	    trie.put(ni.toString(), ni);
 	}
 	
 	for (Integer i: trie.values()) {
 	    assert al.contains(i);
 	}
 	for (String s: trie.keySet()) { 
 	    assert al.contains(Integer.valueOf(s));
 	}
 	for (Integer i: al) { 
 	    assert trie.values().contains(i);
 	    assert trie.keySet().contains(i.toString());
 	}
     }
 
     public void testEqualityOnIndependenOfInsertionAndDeletionSequence() {
	HashSet<Integer> hs = new HashSet<Integer>(1000);
 	Random rng = new Random();
 	
 	for(int i = 0; i < 1000; ++i) {
 	    Integer ni = rng.nextInt(100000000);
	    hs.add(ni);
 	}
	
	ArrayList<Integer> al = new ArrayList<Integer>(hs);
	final List<Integer> permanentPart = al.subList(0, al.size() / 3);
	final List<Integer> deletionPart = al.subList(al.size() / 3, 2 * al.size() / 3);
	final List<Integer> lastPart = al.subList(2 * al.size() / 3, al.size());
 
 	final Trie<String, Integer> trie1 = new Trie<String, Integer>();
 	final Trie<String, Integer> trie2 = new Trie<String, Integer>();
 	
 	assertEquals(trie1, trie2);
 
 	class SetupTries { 
 	    void addToTrie(Trie<String, Integer> trie, List<Integer> l) { 
 		for (Integer i: l) { 
 		    trie.put(i.toString(), i);
 		}
 	    }
 
 	    public void treatTrie(Trie<String, Integer> trie) { 
 		Collections.shuffle(permanentPart);
 		addToTrie(trie, permanentPart);
 		Collections.shuffle(deletionPart);
 		addToTrie(trie, deletionPart);
 		Collections.shuffle(deletionPart);
 		Collections.shuffle(lastPart);
 		Iterator<Integer> deletionIterator =  deletionPart.iterator(), 
 		                  lastIterator = lastPart.iterator();
 		
 		while (deletionIterator.hasNext() || lastIterator.hasNext()) {
 		    if (deletionIterator.hasNext()) {
 			Integer n = deletionIterator.next();
 			trie.remove(n.toString());
 		    }
 		    if (lastIterator.hasNext()) {
 			Integer n = lastIterator.next();
 			trie.put(n.toString(), n);
 		    }
 		}
 	    }
 	}
 	
 	SetupTries st = new SetupTries();
 	
 	st.treatTrie(trie1); st.treatTrie(trie2);
 	assert(!trie1.isEmpty() && !trie2.isEmpty());
 	assertEquals(trie1, trie2);
 	trie1.put("Hello", 1234);
 	assert !trie1.equals(trie2) && !trie2.equals(trie1);
     }
 
     public void testTrieComparedToHashMap() { 
 	Trie<String, Integer> trie = new Trie<String, Integer>();
 	HashMap<String, Integer> hm = new HashMap<String, Integer>();
 	Random rng = new Random();
 
 	for(int i = 0; i < 100; ++i) {
 	    Integer j = rng.nextInt(10000000);
 	    hm.put(Integer.valueOf(j).toString(), j);
 	}
 	assert(!hm.isEmpty());
 
 	trie.putAll(hm);
 	assertEquals(trie.hashCode(), hm.hashCode());
 	ArrayList<Integer> at = new ArrayList<Integer>(trie.values()); Collections.sort(at);
 	ArrayList<Integer> am = new ArrayList<Integer>(hm.values()); Collections.sort(am);
 	assertEquals(at, am);
 	
 	assertEquals(trie.keySet(), hm.keySet());
 
 	HashMap<String, Integer> hm2 = new HashMap<String, Integer>();
 	hm2.putAll(trie);
 	assertEquals(hm2, hm);
     }
 
     public void testTrieEntrySet2() { 
 	Trie<String, String> trie1 = new Trie<String, String>();
 	trie1.put("One for me", "one for you");
 	trie1.put("I am here", "you are me");
 
 	Trie<String, String> trie2 = new Trie<String, String>();
 	trie2.put("One for me", "one for you");
 	trie2.put("I am here", "you are me");
 
 	Set<Map.Entry<String, String>> s1 = trie1.entrySet();
 	Set<Map.Entry<String, String>> s2 = trie2.entrySet();
 	assertEquals(s1, s2);
 	
 	assert(s1.containsAll(s2));
 	assert(s2.containsAll(s1));
 	
 	assert(s1.hashCode() == s2.hashCode());
 	Map.Entry<String, String> entry = s1.iterator().next();
 	String oldValue = entry.getValue();
 	
 	entry.setValue("Something different");
 	assert(!s1.equals(s2));
 	assert(!trie1.equals(trie2));
 	entry.setValue(oldValue);
 	assert(s1.equals(s2));
 	assert(trie1.equals(trie2));
 	
     }
 	    
 }
