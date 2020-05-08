 package org.eclipse.dltk.ruby.tests.text.completion;
 
 import junit.framework.Test;
 
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.codeassist.RelevanceConstants;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.tests.model.AbstractModelCompletionTests;
 import org.eclipse.dltk.core.tests.model.CompletionTestsRequestor;
 import org.eclipse.dltk.ruby.tests.Activator;
 
 public class RubyCompletionTests extends AbstractModelCompletionTests {
 
 	private static final int RELEVANCE = (RelevanceConstants.R_DEFAULT
 			+ RelevanceConstants.R_INTERESTING + RelevanceConstants.R_CASE + RelevanceConstants.R_NON_RESTRICTED);
 
 	public RubyCompletionTests(String name) {
 		super(Activator.PLUGIN_ID, name);
 	}
 
 	public void setUpSuite() throws Exception {
 		PROJECT = setUpScriptProject("completion");
 		waitUntilIndexesReady();
 		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 		waitForAutoBuild();
 		super.setUpSuite();
 	}
 
 	public static Test suite() {
 		return new Suite(RubyCompletionTests.class);
 	}
 
 	private String makeResult(String[] elements, String[] completions, int[] relevance) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < elements.length; ++i) {
 			buffer.append("element:" + elements[i] + "    completion:" + completions[i]
 					+ "    relevance:" + relevance[i]);
 
 			if (i != elements.length - 1) {
 				buffer.append("\n");
 			}
 		}
 		return buffer.toString();
 	}
 
 	private String makeResult(String[] elements) {
 		String[] completions = new String[elements.length];
 		int[] relevance = new int[elements.length];
 		for (int i = 0; i < elements.length; ++i) {
 			completions[i] = elements[i];
 			relevance[i] = RELEVANCE;
 		}
 		return makeResult(elements, completions, relevance);
 	}
 
 	private String makeResult(String[] elements, int[] relevance) {
 		String[] completions = new String[elements.length];
 		for (int i = 0; i < elements.length; ++i) {
 			completions[i] = elements[i];
 		}
 		return makeResult(elements, completions, relevance);
 	}
 
 	public void testCompletion001() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "new.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Foo.new.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 
 	public void testCompletion002() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "inner.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Foo42::";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 	
 	public void testCompletion003() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c1.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 	
 	public void testCompletion004() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c2.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 	
 	public void testCompletion005() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c3.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 	
 	public void testCompletion006() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c4.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().length() > 0);
 	}
 	
 	public void testCompletion007() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "colon1.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "::";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().indexOf("Mine42") != -1) &&
 				(requestor.getResults().indexOf("Mix42") != -1));
 				
 	}
 	
 	public void testCompletion008() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "colon2.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "::";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().indexOf("Mine") != -1) &&
 				(requestor.getResults().indexOf("Mix") != -1));
 				
 	}
 	
 	public void testCompletion009() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "colon3.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Min";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().indexOf("Mine") != -1));
 				
 	}
 	
 	public void testCompletion010() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "var1.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "@a";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion011() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "var2.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "@a";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion012() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "var3.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "@@a";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion013() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "const1.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Mega";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion014() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c5.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion015() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c6.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "t.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion016() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c7.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion017() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "singl1.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Foo66.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue(requestor.getResults().indexOf("cool") != -1);
 		assertTrue(requestor.getResults().indexOf("cool2") != -1);
 				
 	}
 	
 	public void testCompletion018() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "singl2.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "Foo66.cool2.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 
 	public void testCompletion019() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c8.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "@categ";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion20() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c0.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "::";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 		
 	}
 	
 	public void testCompletion021() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "c67.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion022() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "object.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "x.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion023() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "b181387.rb");
 
 		String str = cu.getSource();
 		String completeBehind = "t.";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 				
 	}
 	
 	public void testCompletion024() throws ModelException {
 		CompletionTestsRequestor requestor = new CompletionTestsRequestor();
 		ISourceModule cu = getSourceModule("completion", "src", "b180869.rb");
 
 		String str = cu.getSource();
 		String completeBehind = ".x";
 		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
 		cu.codeComplete(cursorLocation, requestor);
 
 		assertTrue((requestor.getResults().length() > 0));
 	}
 	
 }
