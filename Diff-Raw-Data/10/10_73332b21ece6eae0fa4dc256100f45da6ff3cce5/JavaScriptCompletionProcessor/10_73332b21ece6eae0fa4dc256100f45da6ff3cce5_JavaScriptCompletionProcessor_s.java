 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import org.eclipse.dltk.javascript.core.JavaScriptNature;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProcessor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.contentassist.ContentAssistant;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.ui.IEditorPart;
 
 /**
  * JavaScript completion processor
  */
 public class JavaScriptCompletionProcessor extends ScriptCompletionProcessor {
 
 	public JavaScriptCompletionProcessor(IEditorPart editor,
 			ContentAssistant assistant, String partition) {
 		super(editor, assistant, partition);
 	}
 
 	@Override
 	protected String getNatureId() {
 		return JavaScriptNature.NATURE_ID;
 	}
 
 	@Override
 	protected IContextInformationValidator createContextInformationValidator() {
 		return new JavaScriptParameterListValidator();
 	}
 
 	protected static class JavaScriptParameterListValidator extends
 			ScriptParameterListValidator {
 
 		protected int installOffset;
 		private IContextInformation info;
 		private ITextViewer viewer;
 
 		public boolean isContextInformationValid(int offset) {
 			if (offset < installOffset)
 				return false;
			return (offset - installOffset) <= info.getContextDisplayString()
					.length();
 		}
 
 		public void install(IContextInformation info, ITextViewer viewer,
 				int offset) {
 			this.info = info;
 			this.viewer = viewer;
 			installOffset = offset;
 		}
 
 		public boolean updatePresentation(int documentPosition,
 				TextPresentation presentation) {
 			try {
 				if (documentPosition < installOffset)
 					return false;
 				String text = viewer.getDocument().get(installOffset,
 						documentPosition - installOffset);
 				int count = 0;
 				for (int i = 0; i < text.length(); i++) {
 					if (text.charAt(i) == ',')
 						count++;
 				}
 				StyleRange styleRange = presentation.getFirstStyleRange();
 				if (styleRange == null) {
 					styleRange = new StyleRange();
 					styleRange.fontStyle = SWT.BOLD;
 					presentation.addStyleRange(styleRange);
 				}
 
 				String informationDisplayString = info
 						.getInformationDisplayString();
 				int index = 0;
 				while (count > 0 && index != -1) {
 					index = informationDisplayString.indexOf(',', index);
 					if (index != -1) {
 						count--;
 						index++;
 					}
 				}
 				if (index != -1) {
 					styleRange.start = index;
 					index = informationDisplayString.indexOf(',', index + 1);
 					if (index == -1)
 						styleRange.length = informationDisplayString.length()
 								- styleRange.start;
 					else
 						styleRange.length = index - styleRange.start;
 					return true;
 				}
 
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 	}
 }
