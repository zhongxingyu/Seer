 package com.cjtucker.findbugsfinaldetector;
 
 import static com.cjtucker.findbugsfinaldetector.Samples.*;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import com.youdevise.fbplugins.tdd4fb.DetectorAssert;
 import edu.umd.cs.findbugs.BugReporter;
 
@Ignore
 public class IllegalExtensionOfFinalAnnotatedClassTest {
 
 	private BugReporter bugReporter;
 	private IllegalExtensionOfFinalAnnotatedClass detector;
 
 	@Before
 	public void setUp() throws Exception {
 		bugReporter = DetectorAssert.bugReporterForTesting();
 		detector = new IllegalExtensionOfFinalAnnotatedClass(bugReporter);
 	}
 
 	@Test
 	public void
 	raisesABugAgainsClassExtendingSuperclassWithFinalAnnotation() throws Exception {
 		DetectorAssert.assertBugReported(BadClass.class, detector, bugReporter);
 	}
 
 	@Test
 	public void
 	raisesNoBugAgainstClassWithNoExplicitSuperclass() throws Exception {
 		DetectorAssert.assertNoBugsReported(SafeToExtend.class, detector, bugReporter);
 	}
 
 	@Test
 	public void
 	raisesNoBugAgainstSubclassWithFinalAnnotation() throws Exception {
 		DetectorAssert.assertNoBugsReported(FinalSubclass.class, detector, bugReporter);
 	}
 
 
 	@Test
 	public void
 	raisesNoBugAgainstClassWithSuperclassThatDoesNotHaveFinalAnnotation() throws Exception {
 		DetectorAssert.assertNoBugsReported(GoodSubclass.class, detector, bugReporter);
 	}
 
 
 }
