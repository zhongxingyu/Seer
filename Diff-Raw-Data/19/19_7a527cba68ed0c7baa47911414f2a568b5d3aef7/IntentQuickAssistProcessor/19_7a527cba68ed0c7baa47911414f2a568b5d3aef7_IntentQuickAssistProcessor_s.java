 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor.quickfix;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
 import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.TextInvocationContext;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotation;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationFactory;
 
 /**
  * {@link IntentQuickAssistProcessor} used by Intent to fix any issues.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentQuickAssistProcessor implements IQuickAssistProcessor {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#getErrorMessage()
 	 */
 	public String getErrorMessage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.quickassist.QuickAssistAssistant#canFix(org.eclipse.jface.text.source.Annotation)
 	 */
 	public boolean canFix(Annotation annotation) {
 		return IntentAnnotationFactory.INTENT_ANNOT_SYNC_WARNING.equals(annotation.getType());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canAssist(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
 	 */
 	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#computeQuickAssistProposals(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
 	 */
 	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext quickAssistContext) {
 		ISourceViewer viewer = quickAssistContext.getSourceViewer();
 		int documentOffset = quickAssistContext.getOffset();
 		int length = viewer.getSelectedRange().y;
 		TextInvocationContext context = new TextInvocationContext(viewer, documentOffset, length);
 
 		IAnnotationModel model = viewer.getAnnotationModel();
 
 		int offset = context.getOffset();
 		ArrayList annotationList = new ArrayList();
 		Iterator iter = model.getAnnotationIterator();
 		while (iter.hasNext()) {
 			Annotation annotation = (Annotation)iter.next();
 			if (canFix(annotation)) {
 				Position pos = model.getPosition(annotation);
 				if (isAtPosition(offset, pos)) {
					ICompletionProposal[] proposals = new ICompletionProposal[1];
 
 					String annotationTag = (String)((IntentAnnotation)annotation).getAdditionalInformations()
 							.toArray()[0];
 
 					if (IntentAnnotationFactory.EMPTY_DOCUMENT_RESOURCE_TAG.equals(annotationTag)) {
						proposals[0] = new ClearResourceFix(annotation);
 					} else if (IntentAnnotationFactory.EMPTY_WORKING_COPY_RESOURCE_TAG.equals(annotationTag)) {
						proposals[0] = new MergeEmptyResourceFix(annotation);
 					} else if (IntentAnnotationFactory.NULL_RESOURCE_TAG.equals(annotationTag)) {
						proposals[0] = new CreateResourceFix(annotation);
 					}
 
					return proposals;
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean isAtPosition(int offset, Position pos) {
 		return (pos != null) && (offset >= pos.getOffset() && offset <= (pos.getOffset() + pos.getLength()));
 	}
 
 }
