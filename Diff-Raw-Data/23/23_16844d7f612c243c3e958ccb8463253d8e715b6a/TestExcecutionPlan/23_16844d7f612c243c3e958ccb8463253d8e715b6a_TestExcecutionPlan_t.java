 package org.mitre.hdata.test;
 
 import junit.framework.TestCase;
 import org.mitre.hdata.test.StubTest.*;
 
 import java.util.*;
 
 /**
  * @author Jason Mathews, MITRE Corp.
  * Date: 4/23/12 1:11 PM
  */
 public class TestExcecutionPlan extends TestCase {
 
 	private final static Loader loader = Loader.getInstance();
 	private final static List<StubTest> tests = new ArrayList<StubTest>(4);
 	private final static Test1 test1 = new Test1();
 	private final static Test2 test2 = new Test2();
 	private final static Test3 test3 = new Test3();
 	private final static Test4 test4 = new Test4();
 	private final static Test5 test5 = new Test5();
 	private final static Test6 test6 = new Test6();
 	private final static Test7 test7 = new Test7();
 	private final static Test8 test8 = new Test8();
 
 	static {
 		tests.add(test1);
 		tests.add(new TestDup1()); // this will not be loaded because of duplicate id
 		tests.add(test2);
 		tests.add(test3);
 		tests.add(test4);
 		tests.add(test5);
 		tests.add(test6);
 		// test7 is explicitly not loaded so test8 can fail to load
 		tests.add(test8);

        // loading
        System.out.println("Expected ERROR: Duplicate id [1.0.1] found with test TestDup1");
        for(TestUnit test : tests) {
 			// pre-load these tests
 			loader.load(test);
 		}
 	}
 
 	public void testDepends() {
 		System.out.println("\nXXX: testDepends");
 
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(3);
 		set.add(test1);
 		set.add(test2);
 		set.add(test3);
 
 		ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
 		// creating ExcecutionPlan populates the tests
 		assertNotNull(test2.getDependency(Test1.class));
 		assertNull(test2.getDependency(Test3.class));
 	}
 
 	public void testOrder() {
 		System.out.println("\nXXX: testOrder");
 		// add in excecution order
 		// test1 [pre=none]
 		// test2 [pre=test1]
 		// test3 [pre=test2]
 		// test4 [pre=test2, test3]
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(4);
 		set.add(test1);
 		set.add(test2);
 		set.add(test3);
 		set.add(test4);
 		realTest(set.iterator());
 
 		assertEquals(1, test3.getWarnings().size());
 	}
 
 	public void testReverseOrder() {
 		System.out.println("\nXXX: testReverseOrder");
 		// add in reverse order
 		// test4 [pre=test2, test3]
 		// test3 [pre=test2]
 		// test2 [pre=test1]
 		// test1 [pre=none]
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(4);
 		set.add(test4);
 		set.add(test3);
 		set.add(test2);
 		set.add(test1);
 		realTest(set.iterator());
 	}
 
 	private void realTest(Iterator<TestUnit> iterator) {
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 
 		ExcecutionPlan exec = new ExcecutionPlan(iterator);
 		exec.execute();
 		List<TestUnit> list = exec.getList();
 		assertEquals(4, list.size());
 
 		for(TestUnit test : list) {
 			assertEquals(TestUnit.StatusEnumType.SUCCESS, test.getStatus());
 		}
 
 		// tests should be in expected order
 		assertEquals(test1, list.get(0));
 		assertEquals(test2, list.get(1));
 		assertEquals(test3, list.get(2));
 		assertEquals(test4, list.get(3));
 
 		// if test2 is loaded then test1 keeps its response when
 		// executed
 		assertNotNull(test1.getResponse());
 
 		// test2 + test3 should have its execState -> true
 		assertTrue(test2.execState);
 		assertTrue(test3.execState);
 	}
 
 	public void testFailedTest() {
 		System.out.println("\nXXX: testFailedTest");
 
 		resetTests(TestUnit.StatusEnumType.FAILED);
 		test1.testStatus = TestUnit.StatusEnumType.SUCCESS;
 		// add in execution order
 		// test1 [pre=none]
 		// test2 [pre=test1]
 		// test3 [pre=test2]
 		// test4 [pre=test2, test3]
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(4);
 		set.add(test1);
 		set.add(test2);
 		set.add(test3);
 		set.add(test4);
 
 		ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
        System.out.println("Expected: ERROR: test2 [1.0.2] fails");

        exec.execute();
        List<TestUnit> list = exec.getList();
 		assertEquals(4, list.size());
 
 		// test1 passed, test2 fails, others won't exec
 		assertEquals(TestUnit.StatusEnumType.SUCCESS, test1.getStatus());
 		assertEquals(TestUnit.StatusEnumType.FAILED, test2.getStatus());
 		assertEquals(TestUnit.StatusEnumType.PREREQ_FAILED, test3.getStatus());
 		assertEquals(TestUnit.StatusEnumType.PREREQ_FAILED, test4.getStatus());
 
 		assertNotNull(test1.getResponse());
 
 		// test2 + test3 should have its execState -> false
 		assertFalse(test2.execState);
 		assertFalse(test3.execState);
 	}
 
 	public void testBadProperty() {
 		System.out.println("\nXXX: testBadProperty");
 
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(2);
 		set.add(test5);
 		set.add(test1);
 
        System.out.println("Expected: ERROR: Failed to set property on dependent test");
        ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
        exec.execute();
 		assertEquals(TestUnit.StatusEnumType.SUCCESS, test1.getStatus());
 		assertEquals(TestUnit.StatusEnumType.SKIPPED, test5.getStatus());
 		List<TestUnit> list = exec.getList();
 		// only test1 is loaded. test2 is skipped
 		assertEquals(1, list.size());
 	}
 
 	public void testBadDependencyProp() {
 		System.out.println("\nXXX: testBadDependencyProp");
 
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(2);
 		set.add(test6);
 		set.add(test1);
 
 		ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
 		exec.execute();
 		assertEquals(TestUnit.StatusEnumType.SUCCESS, test1.getStatus());
 		assertEquals(TestUnit.StatusEnumType.SKIPPED, test6.getStatus());
 		List<TestUnit> list = exec.getList();
 		// bad property dependency fails to load the test
 		// so only 1 test should be in the execution plan
 		assertEquals(1, list.size());
 	}
 
 	public void testBadDependency() {
 		System.out.println("\nXXX: testBadDependency");
 
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 		Set<TestUnit> set = new LinkedHashSet<TestUnit>(2);
 		set.add(test7);
 		set.add(test1);
 
 		ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
 		exec.execute();
 		assertEquals(TestUnit.StatusEnumType.SUCCESS, test1.getStatus());
 		assertEquals(TestUnit.StatusEnumType.SKIPPED, test7.getStatus());
 		List<TestUnit> list = exec.getList();
 		assertEquals(1, list.size());
 	}
 
 	public void testLoad() {
 		System.out.println("\nXXX: testLoad");
 		resetTests(TestUnit.StatusEnumType.SUCCESS);
 		Set<TestUnit> set = Collections.<TestUnit>singleton(test8);
        // test7 is explicitly not loaded so test8 can fail to load
        System.out.println("Expected: ERROR: Dependency class <Test7> not loaded");
        ExcecutionPlan exec = new ExcecutionPlan(set.iterator());
 		exec.execute();
 		assertEquals(TestUnit.StatusEnumType.SKIPPED, test8.getStatus());
 		List<TestUnit> list = exec.getList();
 		System.out.println("list: " + list);
 		for(StubTest test : tests) {
 			System.out.println(test.getId() + " " + test.getStatus());
 		}
 		assertEquals(0, list.size());
 	}
 
 	private void resetTests(TestUnit.StatusEnumType testStatus) {
 		for(StubTest test : tests) {
 			test.setStatus(null, null);
 			test.execState = false;
 			test.getWarnings().clear();
 			test.setTestStatus(testStatus);
 			test.getDependencies().clear(); // clear dependencies
 		}
 	}
 
 }
