 package de.fkoeberle.autocommit.message.java.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import de.fkoeberle.autocommit.message.java.CommonParentPackageFinder;
 public class CommonParentPackageFinderTest {
 
 	@Test
 	public void testSimilarSuffix() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test1");
 		finder.checkPackage("org.example.test2");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testParentAndChild() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example");
 		finder.checkPackage("org.example.test2");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testChildAndParent() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test2");
 		finder.checkPackage("org.example");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testDifferInMiddle() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.te1st.deep");
 		finder.checkPackage("org.example.te2st.deep");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testSameSuffixButDifferentLength1() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test");
 		finder.checkPackage("org.example.test2");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testSameSuffixButDifferentLength2() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test2");
 		finder.checkPackage("org.example.test");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testTwoDefaultPackages() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("");
 		finder.checkPackage("");
 		assertEquals("", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testTwoDefaultPackagesAfterNonEmptyPackage() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example");
 		finder.checkPackage("");
 		finder.checkPackage("");
 		assertEquals(null, finder.getCommonPackage());
 	}
 
 	@Test
 	public void testTwoCompleteDifferentPackagesWithSameSuffix() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("helloworld");
 		finder.checkPackage("world");
 		assertEquals(null, finder.getCommonPackage());
 	}
 
 	@Test
 	public void testThreeCompleteDifferentPackages() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("hello");
 		finder.checkPackage("world");
 		finder.checkPackage("org.example");
 		assertEquals(null, finder.getCommonPackage());
 	}
 
 	@Test
 	public void testSmallerAndSmallerCommonPackage() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test.hello.world");
 		finder.checkPackage("org.example.test.hello.x");
 		finder.checkPackage("org.example.hello.x");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testChildParentChild() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test");
 		finder.checkPackage("org.example");
 		finder.checkPackage("org.example.test");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testParentChildParent() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example");
 		finder.checkPackage("org.example.test");
 		finder.checkPackage("org.example");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testThreeDifferentPrefixes() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test0");
 		finder.checkPackage("org.example.test");
 		finder.checkPackage("org.example.test1");
 		assertEquals("org.example", finder.getCommonPackage());
 	}
 
 	@Test
 	public void testDefaultPackageInMiddle() {
 		CommonParentPackageFinder finder = new CommonParentPackageFinder();
 		finder.checkPackage("org.example.test");
 		finder.checkPackage("");
 		finder.checkPackage("org.example.test");
 		assertEquals(null, finder.getCommonPackage());
 	}
 
 }
