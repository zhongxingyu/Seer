 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.ui.tests.indenting;
 
 import org.eclipse.dltk.python.internal.ui.text.PythonAutoEditStrategy;
 import org.eclipse.dltk.python.internal.ui.text.PythonPartitionScanner;
 import org.eclipse.dltk.python.ui.PythonPreferenceConstants;
 import org.eclipse.dltk.python.ui.tests.PythonUITestsPlugin;
 import org.eclipse.dltk.python.ui.text.IPythonPartitions;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.DocumentRewriteSession;
 import org.eclipse.jface.text.DocumentRewriteSessionType;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.rules.FastPartitioner;
 
 
 public class SimpleIndentingTest extends PyUITest {
 
 	private IPreferenceStore fStore;
 	
 	/**
 	 * Installs a partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private void installStuff(Document document) {
 		String[] types = new String[] { IPythonPartitions.PYTHON_STRING,
 				IPythonPartitions.PYTHON_COMMENT, IDocument.DEFAULT_CONTENT_TYPE };
 		FastPartitioner partitioner = new FastPartitioner(
 				new PythonPartitionScanner(), types);
 		partitioner.connect(document);
 		document.setDocumentPartitioner(IPythonPartitions.PYTHON_PARTITIONING,
 				partitioner);
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		fStore = PythonUITestsPlugin.getDefault().getPreferenceStore();
 		PythonPreferenceConstants.initializeDefaultValues(fStore);
 	}
 
	public void REM_testParts() throws Exception {
 		String text = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/test_crypt.py" );
 		Document temp = new Document(text);
 
 		installStuff(temp);
 
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		
 		ITypedRegion region = TextUtilities.getPartition(temp, fPartitioning, 2, true);
 		assertEquals(IPythonPartitions.PYTHON_COMMENT, region.getType());
 		
 		region = TextUtilities.getPartition(temp, fPartitioning, temp.getLineOffset(3), true);
 		assertEquals(IPythonPartitions.PYTHON_STRING, region.getType());
 		
 		region = TextUtilities.getPartition(temp, fPartitioning, temp.getLineOffset(5) + 10, true);
 		assertEquals(IDocument.DEFAULT_CONTENT_TYPE, region.getType());
 		
 		region = TextUtilities.getPartition(temp, fPartitioning, temp.getLineOffset(8) + 19, true);
 		assertEquals(IPythonPartitions.PYTHON_STRING, region.getType());
 	}
 
	public void REM_testIndent00() throws Exception {
 		String text = "class Foo:";
 		Document temp = new Document(text);
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = temp.getLineOffset(0) + temp.getLineLength(0);		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text="\n";
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);
 		int newOffset = c.offset + (c.text == null ? 0 : c.text.length());
 		if (c.caretOffset != -1)
 			newOffset = c.caretOffset;
 			
 		temp.stopRewriteSession(session);
 		
 		//check document
 		String correct = "class Foo:\n" +
 			"\t";
 		String result = temp.get();		
 		assertEquals(correct, result);
 		
 		//check caret position
 		assertEquals(offset + 1, newOffset);
 	}
 	
 	public void testIndent01_strings() throws Exception {
 		String text = "print \"hello\"";
 		Document temp = new Document(text);
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = temp.getLineOffset(0) + temp.getLineLength(0);		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text="\n";
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);		
 			
 		temp.stopRewriteSession(session);
 		
 		//check document
 		String correct = "print \"hello\"\n";
 		String result = temp.get();		
 		assertEquals(correct, result);		
 	}
 
 	
	public void REM_testIndent05_pasting() throws Exception {
 		String text0 = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/my_test0_in.py" );
 		Document temp = new Document("");
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = 0;		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text=text0;
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);
 			
 		temp.stopRewriteSession(session);
 		
 		assertEquals (text0, temp.get());	
 	}	
 	
	public void REM_testIndent06_pasting() throws Exception {
 		String textIn = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/my_test1_in.py" );
 		String text0 = "a = 5\n"+
 		"b = 7\n"+
 		"if (smt):\n"+
 		"    go1\n"+
 		"else:\n"+
 		"    go2\n"+
 		"    try:\n"+
 		"        wow\n"+
 		"    except:\n"+
 		"        nothing\n"+
 		"    else:\n"+
 		"        yes!\n";
 		Document temp = new Document(textIn);
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = temp.getLineOffset(12) + 8;		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text=text0;
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);
 			
 		temp.stopRewriteSession(session);
 		
 		String textOut = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/my_test1_out.py" );
 		
 		Document d2 = new Document (textOut);		
 		assertIndentingEquals(d2, temp);
 
 	}
 	
	public void REM_testIndent07_pasting() throws Exception {
 		String textIn = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/my_test1_in.py" );
 		String text0 = "a = 5\n"+
 		"            b = 7\n"+
 		"            if (smt):\n"+
 		"                go1\n"+
 		"            else:\n"+
 		"                go2\n"+
 		"                try:\n"+
 		"                    wow\n"+
 		"                except:\n"+
 		"                    nothing\n"+
 		"                else:\n"+
 		"                    yes!\n";
 		Document temp = new Document(textIn);
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = temp.getLineOffset(1) + 4;		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text=text0;
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);
 			
 		temp.stopRewriteSession(session);
 		
 		String textOut = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/my_test2_out.py" );
 		
 		Document d2 = new Document (textOut);
 		
 		assertIndentingEquals(d2, temp);
 	}
 /*	
 	public void nottestIndent08_pasting() throws Exception {
 		String textIn = PythonUITestsPlugin.getDefault().getPluginFileContent( "/tests/test_builtin.py" );		
 		Document temp = new Document("");
 		DocumentRewriteSession session = temp
 				.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 		installStuff(temp);
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		
 		int offset = 0;		
 		
 		DocumentCommand c = new DocumentCommand() {};
 		c.doit = true;
 		c.caretOffset = -1;
 		c.shiftsCaret = true;
 		c.length = 0;
 		c.offset = offset;
 		c.text=textIn;
 		
 		strategy.customizeDocumentCommand(temp, c);
 		
 		//execute
 		temp.replace(c.offset, c.length, c.text);
 			
 		temp.stopRewriteSession(session);
 		
 		assertModelsEquals(textIn, temp.get());
 	}
 */	
 	
 	private void assertIndentingEquals (IDocument exp, IDocument act) {
 		int line = 0;		
 		//TODO: please, review this code
 		String fPartitioning = IPythonPartitions.PYTHON_PARTITIONING;
 		PythonAutoEditStrategy strategy = new PythonAutoEditStrategy(fStore, fPartitioning);
 		try {
 			int linesTotal = exp.getLineOfOffset(exp.getLength() - 1);
 			assertEquals (linesTotal, act.getLineOfOffset(act.getLength() - 1));
 			for (line = 0; line <= linesTotal; line++) {
 				String s1 = strategy.getDocumentLine(exp, line);
 				String s2 = strategy.getDocumentLine(act, line);
 				String ind1 = strategy.getLineIndent(s1);
 				String ind2 = strategy.getLineIndent(s2);
 				if (strategy.getPhysicalLength(ind1) != strategy.getPhysicalLength(ind2))
 					System.out.println ("(line:" + line +  ")\n"+s1 + "\nvs\n" + s2);
 				assertEquals(strategy.getPhysicalLength(ind1), strategy.getPhysicalLength(ind2));			
 			}
 		} catch (BadLocationException e) {		
 		}		
 	}
 	/*
 	private void saveTempFile (String name, String content) {
 		try {					
 			FileWriter wf = new FileWriter (name);
 			wf.write(content);
 			wf.close();					
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void assertModelsEquals (IModelElement e1, IModelElement e2) throws ModelException {
 		if (!(e1 instanceof ISourceModule)) {
 			assertEquals(e1.getElementName(), e2.getElementName());
 			assertEquals(e1.getElementType(), e2.getElementType());
 		}
 		if (e1 instanceof ISourceModule) {
 			IModelElement[] ch1 = ((ISourceModule)e1).getChildren();
 			IModelElement[] ch2 = ((ISourceModule)e2).getChildren();
 			assertEquals(ch1.length, ch2.length);
 			for (int i = 0; i < ch1.length; i++) {
 				assertModelsEquals (ch1[i], ch2[i]);
 			}
 		}
 		if (e1 instanceof IType) {
 			IModelElement[] ch1 = ((IType)e1).getChildren();
 			IModelElement[] ch2 = ((IType)e1).getChildren();
 			assertEquals(ch1.length, ch2.length);
 			for (int i = 0; i < ch1.length; i++) {
 				assertModelsEquals (ch1[i], ch2[i]);
 			}
 		}
 		if (e1 instanceof IMethod) {
 			IModelElement[] ch1 = ((IMethod)e1).getChildren();
 			IModelElement[] ch2 = ((IMethod)e1).getChildren();
 			assertEquals(ch1.length, ch2.length);
 			for (int i = 0; i < ch1.length; i++) {
 				assertModelsEquals (ch1[i], ch2[i]);
 			}
 		}
 	}
 	
 	private void assertModelsEquals (String src1, String src2) throws CoreException, IOException {
 		String prj = "prj0";
 		IScriptProject project = this.setUpScriptProject(prj); 
 		
 		saveTempFile (project.getPath().append("src1.py").makeRelative().toOSString(), src1);
 		saveTempFile (project.getPath().append("src2.py").makeRelative().toOSString(), src2);
 		ISourceModule module1 = this.getSourceModule(prj, "src", new Path("src1.py"));
 		ISourceModule module2 = this.getSourceModule(prj, "src", new Path("src2.py"));
 		//compare modules
 		assertModelsEquals(module1, module2);
 		deleteProject(prj);
 	}
 */	
 }
