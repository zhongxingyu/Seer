 package org.getcomposer.core.test;
 
 import org.getcomposer.core.ComposerPackage;
 import org.junit.Test;
 
 public class JsonWriterTest extends ComposertTestCase {
 
 	private ComposerPackage phpPackage;
 	private String json;
 
 	/*
 	 * would be better in a @BeforeClass as so, this does not work for me
 	 */
 	public void setUp() {
 		phpPackage = createDummyPackage();
 		json = phpPackage.toJson();
 	}
 	
 	@Test
 	public void testToJson() {
 		assertNotNull(json);
 	}
 	
 	@Test
 	public void testComposerPackage() {
 		doTestComposerPackage(new ComposerPackage(json));
 	}
 	
 	@Test
 	public void testDependencies() {
 		doTestDependencies(new ComposerPackage(json));
 	}
 	
 	@Test
 	public void testAutoload() {
 		doTestAutoload(new ComposerPackage(json));
 	}
 	
 	@Test
 	public void testConfig() {
 		doTestConfig(new ComposerPackage(json));
 	}
 	
 	@Test
 	public void testScripts() {
 		doTestScripts(new ComposerPackage(json));
 	}
 	
 	@Test
 	public void testSupport() {
 		doTestSupport(new ComposerPackage(json));
 	}
 
 	@Test
 	public void testRepositories() {
 		doTestRepositories(new ComposerPackage(json));
 	}
 	
 	@Test 
 	public void testSimplePackage() {
 		ComposerPackage pkg = new ComposerPackage();
 		pkg.setName("test/package");
 		pkg.setType("");
 		
 //		System.out.println(pkg.toJson());
 	}
 }
