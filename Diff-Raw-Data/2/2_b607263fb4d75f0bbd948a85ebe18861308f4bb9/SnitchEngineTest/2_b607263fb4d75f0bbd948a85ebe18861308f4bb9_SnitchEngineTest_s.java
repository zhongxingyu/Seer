 package com.refactr.snitch;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.refactr.snitch.rules.Rule;
 
 public class SnitchEngineTest {
 	protected SnitchEngine e;
 
 	@Before
 	public void setup() {
 		e = new SnitchEngine();
 	}
 
 	@Test
 	public void testDiscoverRules() {
 		List<Rule> rules = e.discoverRules();
		assertEquals(4, rules.size());
 	}
 
 	@Test
 	public void testShouldCheckAllByDefault() {
 		assertTrue(e.shouldCheck(new File("file")));
 		assertTrue(e.shouldCheck(new File("src")));
 		assertTrue(e.shouldCheck(new File("test")));
 	}
 
 	@Test
 	public void testShouldCheckGivesPrecedenceToExcludes() {
 		e.includes = new Glob("{?oo,b*}");
 		e.excludes = new Glob("{foo,bar}");
 		assertTrue(e.shouldCheck(new File("boo")));
 		assertFalse(e.shouldCheck(new File("foo")));
 		assertFalse(e.shouldCheck(new File("bar")));
 		assertTrue(e.shouldCheck(new File("baz")));
 		assertFalse(e.shouldCheck(new File("quux")));
 	}
 
 	@Test
 	public void testShouldCheckHonorsExcludes() {
 		e.excludes = new Glob("foo");
 		assertFalse(e.shouldCheck(new File("foo")));
 		assertTrue(e.shouldCheck(new File("bar")));
 	}
 
 	@Test
 	public void testShouldCheckHonorsIncludes() {
 		e.includes = new Glob("foo");
 		assertTrue(e.shouldCheck(new File("foo")));
 		assertFalse(e.shouldCheck(new File("bar")));
 	}
 
 	@Test
 	public void testShouldCheckOnlyExcludesForDirectories() {
 		e.excludes = new Glob("{test}");
 		e.includes = new Glob("{test}");
 
 		File test = new File("test");
 		assertTrue(test.isDirectory());
 		assertFalse(e.shouldCheck(test));
 
 		File src = new File("src");
 		assertTrue(src.isDirectory());
 		assertTrue(e.shouldCheck(src));
 	}
 }
