 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.editor;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.ICodeAssist;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.text.ScriptWordFinder;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 
 
 /**
  * Script element hyperlink detector.
  *
 	 *
  */
 public class ModelElementHyperlinkDetector implements IHyperlinkDetector {
 
 	private ITextEditor fTextEditor;
 
 	/**
 	 * Creates a new Script element hyperlink detector.
 	 *
 	 * @param editor the editor in which to detect the hyperlink
 	 */
 	public ModelElementHyperlinkDetector(ITextEditor editor) {
 		Assert.isNotNull(editor);
 		fTextEditor= editor;
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
 	 */
 	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || !(fTextEditor instanceof ScriptEditor))
 			return null;
 
 		IAction openAction= fTextEditor.getAction("OpenEditor"); //$NON-NLS-1$
 		if (openAction == null)
 			return null;
 
 		int offset= region.getOffset();
 
 		IModelElement input= EditorUtility.getEditorInputModelElement(fTextEditor, false);
 		if (input == null)
 			return null;
 
 		try {
 			IDocument document= fTextEditor.getDocumentProvider().getDocument(fTextEditor.getEditorInput());
 			IRegion wordRegion= ScriptWordFinder.findWord(document, offset);
 			if (wordRegion == null)
 				return null;
 			
 			IModelElement[] elements= null;
 			elements= ((ICodeAssist) input).codeSelect(wordRegion.getOffset(), wordRegion.getLength());
 			if (elements != null && elements.length > 0)
 				return new IHyperlink[] {new ModelElementHyperlink(wordRegion, openAction)};
 		} catch (ModelException e) {
 			return null;
 		}
 
 		return null;
 	}
 }
