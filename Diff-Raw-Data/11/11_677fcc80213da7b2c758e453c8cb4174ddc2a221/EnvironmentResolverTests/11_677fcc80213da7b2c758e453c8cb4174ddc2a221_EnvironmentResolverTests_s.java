 package org.eclipse.dltk.core.tests.launching;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.internal.launching.EnvironmentResolver;
 import org.eclipse.dltk.launching.EnvironmentVariable;
 
 public class EnvironmentResolverTests extends TestCase {
 	public void testEnvironmentResolve001() {
 		Map env = new HashMap();
 		EnvironmentVariable[] vars = new EnvironmentVariable[] { mk("A", "a") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertNotNull(resolve);
 		test(resolve, "A", "a");
 	}
 
 	public void testEnvironmentResolve002() {
 		Map env = new HashMap();
 		EnvironmentVariable[] vars = new EnvironmentVariable[] { mk("A", "a"),
 				mk("B", "a$Ac") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertNotNull(resolve);
 		test(resolve, "A", "a");
 		test(resolve, "B", "aac");
 	}
 
 	public void testEnvironmentResolve003() {
 		Map env = new HashMap();
 		EnvironmentVariable[] vars = new EnvironmentVariable[] {
 				mk("AUTOTEST", "/Develop/cisco/ats5.0.0"),
 				mk("ATS_EASY", "$AUTOTEST/ats_easy") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertNotNull(resolve);
 		test(resolve, "AUTOTEST", "/Develop/cisco/ats5.0.0");
 		test(resolve, "ATS_EASY", "/Develop/cisco/ats5.0.0/ats_easy");
 	}
 
 	public void testEnvironmentResolve004() {
 		Map env = new HashMap();
 		env.put("PATH", "/bin:/usr/bin");
 		EnvironmentVariable[] vars = new EnvironmentVariable[] { mk("PATH",
 				"/sbin:$PATH") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertNotNull(resolve);
 		test(resolve, "PATH", "/sbin:/bin:/usr/bin");
 	}
 
 	public void testEnvironmentResolve005() {
 		Map env = new HashMap();
 		env.put("PATH", "/bin:/usr/bin");
 		EnvironmentVariable[] vars = new EnvironmentVariable[] {
 				new EnvironmentVariable("a1", "A1"),
 				new EnvironmentVariable("a2", "A2$a1"),
 				new EnvironmentVariable("a3", "A3$a2"),
 				new EnvironmentVariable("a4", "A4$a3") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertNotNull(resolve);
 		test(resolve, "a1", "A1");
 		test(resolve, "a2", "A2A1");
 		test(resolve, "a3", "A3A2A1");
 		test(resolve, "a4", "A4A3A2A1");
 	}
 	public void testEnvironmentResolve006() {
 		Map env = new HashMap();
 		env.put("PATH", "/bin:/usr/bin");
 		EnvironmentVariable[] vars = new EnvironmentVariable[] {
 				new EnvironmentVariable("a1", "A1$a4"),
 				new EnvironmentVariable("a2", "A2$a1"),
 				new EnvironmentVariable("a3", "A3$a2"),
 				new EnvironmentVariable("a4", "A4$a3") };
 		EnvironmentVariable[] resolve = EnvironmentResolver.resolve(env, vars);
 		assertEquals(resolve.length, 0);
 	}
 
 	private void test(EnvironmentVariable[] resolve, String b, String v) {
 		assertContains(resolve, mk(b, v));
 	}
 
 	private EnvironmentVariable mk(String name, String value) {
 		return new EnvironmentVariable(name, value);
 	}
 
 	private void assertContains(EnvironmentVariable[] resolve,
 			EnvironmentVariable environmentVariable) {
 		String name = environmentVariable.getName();
 		for (int i = 0; i < resolve.length; i++) {
 			if (resolve[i].equals(environmentVariable)) {
 				return;
 			}
 			if (resolve[i].getName().equals(name)) {
 				assertTrue("Variable:" + name + "=>" + resolve[i].getValue()
 						+ "!=" + environmentVariable.getValue(), false);
 			}
 		}
 		assertTrue(false);
 	}
 }
