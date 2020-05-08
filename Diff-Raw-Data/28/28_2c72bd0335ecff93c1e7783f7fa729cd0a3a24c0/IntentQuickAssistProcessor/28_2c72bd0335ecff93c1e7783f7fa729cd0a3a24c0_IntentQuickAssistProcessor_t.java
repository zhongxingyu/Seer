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
 import java.util.List;
 
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
 import org.eclipse.mylyn.docs.intent.core.compiler.ResourceChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerResourceState;
 
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
 		Iterator<?> iter = model.getAnnotationIterator();
 		while (iter.hasNext()) {
 			Annotation annotation = (Annotation)iter.next();
 			if (canFix(annotation)) {
 				Position pos = model.getPosition(annotation);
 				if (pos != null && pos.includes(offset)) {
 					List<ICompletionProposal> proposals = computeProposalsFromStatus(annotation);
 					return proposals.toArray(new ICompletionProposal[proposals.size()]);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the proposals according to the given annotation status.
 	 * 
 	 * @param annotation
 	 *            the annotation
 	 * @return the proposals according to the given annotation status
 	 */
 	private List<ICompletionProposal> computeProposalsFromStatus(Annotation annotation) {
 		SynchronizerCompilationStatus status = (SynchronizerCompilationStatus)((IntentAnnotation)annotation)
 				.getCompilationStatus();
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		if (status instanceof ResourceChangeStatus) {
 			ResourceChangeStatus resourceChangeStatus = (ResourceChangeStatus)status;
 			SynchronizerResourceState compiledResourceState = resourceChangeStatus.getCompiledResourceState();
 			SynchronizerResourceState workingCopyResourceState = resourceChangeStatus
 					.getWorkingCopyResourceState();
 
 			if (SynchronizerResourceState.EMPTY.equals(compiledResourceState)) {
 				proposals.add(new ClearResourceFix(annotation));
 			} else if (SynchronizerResourceState.EMPTY.equals(workingCopyResourceState)) {
 				proposals.add(new MergeEmptyResourceFix(annotation));
 			} else if (SynchronizerResourceState.NULL.equals(workingCopyResourceState)) {
 				proposals.add(new CreateResourceFix(annotation));
 			}
 		} else {
			// proposals.add(new EMFCompareFix(annotation));
 			proposals.add(new UpdateModelingUnitFix(annotation));
 		}
 		return proposals;
 	}
 
 }
