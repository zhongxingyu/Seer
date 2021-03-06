 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.cdi.seam.text.ext.test;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.jboss.tools.cdi.seam.config.core.test.SeamConfigTest;
 import org.jboss.tools.cdi.seam.text.ext.CDISeamExtPlugin;
 import org.jboss.tools.cdi.seam.text.ext.hyperlink.SeamConfigInjectedPointHyperlinkDetector;
import org.jboss.tools.cdi.text.ext.test.HyperlinkDetectorTest;
 import org.jboss.tools.common.util.FileUtil;
 
 /**
  *   
  * @author Viacheslav Kabanovich
  *
  */
 public class SeamConfigInjectedPointHyperlinkTest extends SeamConfigTest {
 	public SeamConfigInjectedPointHyperlinkTest() {}
 
 	public void testSeamConfigInjectedPointHyperlink() throws Exception {
 		IHyperlink hyperlink = checkHyperLinkInJava(
 				"src/org/jboss/beans/injection/Injections.java", 
 				project, 
 				"@Inject", 1, 
 				new SeamConfigInjectedPointHyperlinkDetector(), 
 				"org.jboss.tools.cdi.seam.text.ext.hyperlink.SeamConfigInjectedPointHyperlink");
 		hyperlink.open();
 		
 		IEditorPart editor = CDISeamExtPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
 		IFile f = input.getFile();
 		assertEquals("seam-beans.xml", f.getName());
 		
 		ITextSelection textSelection = getSelection(editor);
 		
 		String text = FileUtil.readStream(f).substring(textSelection.getOffset(), textSelection.getOffset() + textSelection.getLength());
 		assertEquals("<test04:myType3>", text);
 
 	}
 
 	public static ITextSelection getSelection(IEditorPart editor) {
 		ITextEditor textEditor = (ITextEditor)editor.getAdapter(ITextEditor.class);
 		assertNotNull(textEditor);
 		ISelection s = textEditor.getSelectionProvider().getSelection();
 		assertTrue(s instanceof ITextSelection);
 		return (ITextSelection)s;
 	}
 
 	public static IHyperlink checkHyperLinkInJava(String fileName, IProject project, String substring, int innerOffset, AbstractHyperlinkDetector detector, String hyperlinkClassName) throws Exception {
 		IFile file = project.getFile(fileName);
 
 		assertNotNull("The file \"" + fileName + "\" is not found", file);
 		assertTrue("The file \"" + fileName + "\" is not found", file.isAccessible());
 		
 		String text = FileUtil.readStream(file);
 		int offset = text.indexOf(substring);
 		assertTrue(offset > 0);
 		offset += innerOffset;
 
 		Region region = new Region(offset, 0);
 
 		FileEditorInput editorInput = new FileEditorInput(file);
 
		IEditorPart part = HyperlinkDetectorTest.openFileInEditor(file);
 		CompilationUnitEditor editor = (CompilationUnitEditor)part;
 		ISourceViewer viewer = editor.getViewer();
 		detector.setContext(editor);
 		
 		IHyperlink[] links = detector.detectHyperlinks(viewer, region, true);
 		if(links!=null) {
 			for (IHyperlink hyperlink : links) {
 				if(hyperlink.getClass().getName().equals(hyperlinkClassName)) {
 					return hyperlink;
 				}
 			}
 		}
 		fail("Can't find HyperLink");
 		return null;
 	}
 
 }
