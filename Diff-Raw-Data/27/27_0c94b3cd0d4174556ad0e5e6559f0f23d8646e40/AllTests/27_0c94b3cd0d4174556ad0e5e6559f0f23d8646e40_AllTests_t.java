 package org.eclipse.dltk.ruby.ui.tests;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.dltk.ruby.ui.tests.folding.RubyCommentElementTests;
 import org.eclipse.dltk.ruby.ui.tests.folding.RubyFoldingTest;
 import org.eclipse.dltk.ruby.ui.tests.indenting.RubyAutoIndentStrategyTest;
 import org.eclipse.dltk.ruby.ui.tests.search.MixinCompleteTests;
 import org.eclipse.dltk.ruby.ui.tests.search.ThreadedUIMixinTests;
 import org.eclipse.dltk.ruby.ui.tests.text.PartitioningTest;
 import org.eclipse.dltk.ruby.ui.tests.text.RubyRequireHyperlinkDetectorTest;
 import org.eclipse.dltk.ruby.ui.tests.text.indenting.IndentingTest;
 
 public class AllTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("org.eclipse.dltk.ruby.ui.tests");
 		// $JUnit-BEGIN$
 		suite.addTestSuite(RubyAutoIndentStrategyTest.class);
 		suite.addTestSuite(PartitioningTest.class);
 		suite.addTestSuite(IndentingTest.class);
 		suite.addTestSuite(RubyFoldingTest.class);
		suite.addTest(MixinCompleteTests.suite());
		suite.addTest(ThreadedUIMixinTests.suite());
 		suite.addTestSuite(RubyRequireHyperlinkDetectorTest.class);
 		suite.addTest(RubyCommentElementTests.suite());
 		// $JUnit-END$
 		return suite;
 	}
 
 }
