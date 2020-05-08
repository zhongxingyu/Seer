 package com.syncron.findbugsextensions;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.testng.annotations.Test;
 
 import com.syncron.findbugsextensions.otherpackage.AnnotationForbiddenUser;
 import com.syncron.findbugsextensions.otherpackage.PackagePrivateExtenderAllowedExtender;
 import com.syncron.findbugsextensions.otherpackage.PackagePrivateForbiddenExtender;
 import com.syncron.findbugsextensions.otherpackage.PackagePrivateForbiddenUser;
 import com.syncron.findbugsextensions.utils.BaseDetectorTestCase;
 
 import edu.umd.cs.findbugs.BugInstance;
 import edu.umd.cs.findbugs.BugPattern;
 
 public class PackagePrivateDetectorTest extends BaseDetectorTestCase {
 
 	@Test
 	public void shouldAllowPackagePrivateForMethod() {
 		// given
 		Class<?> okClass = PackagePrivateUser.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(okClass);
 
 		// then
		assertTrue(bugs.isEmpty(), "There should be no SYNC_PACKAGE_PRIVATE_USAGE bugs in " + okClass);
 	}
 
 	@Test
 	public void shouldForbidPackagePrivateForMethod() {
 		// given
 		Class<?> classWithProblem = PackagePrivateForbiddenUser.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(classWithProblem);
 
 		// then
		assertEquals(bugs.size(), 2, "There should be 2 SYNC_PACKAGE_PRIVATE_USAGE bugs in " + classWithProblem);
 	}
 
 	@Test
 	public void shouldAllowPackagePrivateForClass() {
 		// given
 		Class<?> okClass = PackagePrivateExtender.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(okClass);
 
 		// then
		assertEquals(bugs.size(), 0, "There should be no SYNC_PACKAGE_PRIVATE_USAGE bug in " + okClass);
 	}
 
 	@Test
 	public void shouldForbidPackagePrivateForClass() {
 		// given
 		Class<?> classWithProblem = PackagePrivateForbiddenExtender.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(classWithProblem);
 
 		// then
		assertEquals(bugs.size(), 1, "There should be 1 SYNC_PACKAGE_PRIVATE_USAGE bug in " + classWithProblem);
 	}
 
 	@Test
 	public void shouldForbidPackagePrivateForMethodAnnotation() {
 		// given
 		Class<?> classWithProblem = AnnotationForbiddenUser.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(classWithProblem);
 
 		// then
		assertEquals(bugs.size(), 1, "There should be 1 SYNC_PACKAGE_PRIVATE_USAGE bug in " + classWithProblem);
 	}
 
 	@Test
 	public void shouldAllowIndirectPackagePrivateClassExtensions() {
 		// given
 		Class<?> okClass = PackagePrivateExtenderAllowedExtender.class;
 
 		// when
 		List<BugInstance> bugs = runDetector(okClass);
 
 		// then
		assertEquals(bugs.isEmpty(), true, "There should be no SYNC_PACKAGE_PRIVATE_USAGE bug in " + okClass);
 	}
 
 	private List<BugInstance> runDetector(Class<?> offendingClass) {
		BugPattern bugPattern = new BugPattern("SYNC_PACKAGE_PRIVATE_USAGE", "SPPU", "CORRECTNESS", true, "", "", "");
 
 		// find forbidden @PackagePrivate usage
 		Collection<BugInstance> bugs = runDetector(new PackagePrivateDetector(getBugReporter()), offendingClass,
 				bugPattern);
 		return new LinkedList<BugInstance>(bugs);
 	}
 }
