 /*******************************************************************************
  * Copyright (c) 2004, 2008 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.mylyn.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.internal.mylyn.DLTKUiBridgePlugin;
 import org.eclipse.dltk.ui.text.completion.AbstractScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposalComputer;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
 import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.ui.CommonImages;
 import org.eclipse.mylyn.context.core.ContextCore;
 import org.eclipse.mylyn.context.core.IInteractionElement;
 
 /**
  * TODO: parametrize relevance levels (requires JDT changes, bug 119063)
  * 
  * @author Mik Kersten
  */
 public class FocusedDLTKProposalProcessor {
 
 	/**
 	 * Range above which elements are part of the context.
 	 */
 	private static final int THRESHOLD_INTEREST = 10000;
 
 	/**
 	 * Range for implicitly interesting element, such as method parameters.
 	 */
 	private static final int THRESHOLD_IMPLICIT_INTEREST = THRESHOLD_INTEREST * 2;
 
 	/**
 	 * Threshold for determining which JDT proposals should be implicitly interesting.
 	 */
 	private static final int RELEVANCE_IMPLICIT_INTEREST_JAVA = 600;
 
 	/**
 	 * Threshold for implicit interest of IModelElement proposals.
 	 */
 	private static final int RELEVANCE_IMPLICIT_INTEREST_MISC = 300;
 
 	private static final String IDENTIFIER_THIS = "this"; //$NON-NLS-1$
 
 	public static final String LABEL_SEPARATOR = " -------------------------------------------- "; //$NON-NLS-1$
 
 	public static final FocusedProposalSeparator PROPOSAL_SEPARATOR = new FocusedProposalSeparator();
 
 	private final List<IScriptCompletionProposalComputer> monitoredProposalComputers = new ArrayList<IScriptCompletionProposalComputer>();
 
 	private final List<IScriptCompletionProposalComputer> alreadyComputedProposals = new ArrayList<IScriptCompletionProposalComputer>();
 
 	private final List<IScriptCompletionProposalComputer> alreadyContainSeparator = new ArrayList<IScriptCompletionProposalComputer>();
 
 	private final List<IScriptCompletionProposalComputer> containsSingleInterestingProposal = new ArrayList<IScriptCompletionProposalComputer>();
 
 	private static FocusedDLTKProposalProcessor INSTANCE = new FocusedDLTKProposalProcessor();
 
 	private FocusedDLTKProposalProcessor() {
 	}
 
 	public static FocusedDLTKProposalProcessor getDefault() {
 		return INSTANCE;
 	}
 
 	public void addMonitoredComputer(IScriptCompletionProposalComputer proposalComputer) {
 		monitoredProposalComputers.add(proposalComputer);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List projectInterestModel(IScriptCompletionProposalComputer proposalComputer, List proposals) {
 		try {
 			if (!ContextCore.getContextManager().isContextActive()) {
 				return proposals;
 			} else {
 				boolean hasInterestingProposals = false;
 				for (Object object : proposals) {
 					if (object instanceof AbstractScriptCompletionProposal) {
 						boolean foundInteresting = boostRelevanceWithInterest((AbstractScriptCompletionProposal) object);
 						if (!hasInterestingProposals && foundInteresting) {
 							hasInterestingProposals = true;
 						}
 					}
 				}
 
 				// NOTE: this annoying state needs to be maintainted to ensure
 				// the
 				// separator is added only once, and not added for single
 				// proposals
 				if (containsSingleInterestingProposal.size() > 0 && proposals.size() > 0) {
 					proposals.add(FocusedDLTKProposalProcessor.PROPOSAL_SEPARATOR);
 				} else if (hasInterestingProposals && alreadyContainSeparator.isEmpty()) {
 					if (proposals.size() == 1) {
 						containsSingleInterestingProposal.add(proposalComputer);
 					} else {
 						proposals.add(FocusedDLTKProposalProcessor.PROPOSAL_SEPARATOR);
 						alreadyContainSeparator.add(proposalComputer);
 					}
 				}
 
 				alreadyComputedProposals.add(proposalComputer);
 				if (alreadyComputedProposals.size() == monitoredProposalComputers.size()) {
 					alreadyComputedProposals.clear();
 					alreadyContainSeparator.clear();
 					containsSingleInterestingProposal.clear();
 				}
 
 				return proposals;
 			}
 		} catch (Throwable t) {
 			StatusHandler.log(new Status(IStatus.ERROR, DLTKUiBridgePlugin.ID_PLUGIN,
 					"Failed to project interest onto propsals", t)); //$NON-NLS-1$
 			return proposals;
 		}
 	}
 
 	private boolean boostRelevanceWithInterest(AbstractScriptCompletionProposal proposal) {
 		boolean hasInteresting = false;
 		IModelElement javaElement = proposal.getModelElement();
 		if (javaElement != null) {
 			IInteractionElement interactionElement = ContextCore.getContextManager().getElement(
 					javaElement.getHandleIdentifier());
 			float interest = interactionElement.getInterest().getValue();
 			if (interest > ContextCore.getCommonContextScaling().getInteresting()) {
 				// TODO: losing precision here, only going to one decimal place
 				proposal.setRelevance(THRESHOLD_INTEREST + (int) (interest * 10));
 				hasInteresting = true;
 			} else if (proposal.getRelevance() > RELEVANCE_IMPLICIT_INTEREST_JAVA) {
 				proposal.setRelevance(THRESHOLD_IMPLICIT_INTEREST + proposal.getRelevance());
 			}
 		} else if (isImplicitlyInteresting(proposal)) {
 			proposal.setRelevance(THRESHOLD_IMPLICIT_INTEREST + proposal.getRelevance());
 			hasInteresting = true;
 		}
 		return hasInteresting;
 	}
 
 	public boolean isImplicitlyInteresting(AbstractScriptCompletionProposal proposal) {
 		return proposal.getRelevance() > RELEVANCE_IMPLICIT_INTEREST_MISC
 				&& !IDENTIFIER_THIS.equals(proposal.getDisplayString());
 	}
 
 	static class FocusedProposalSeparator extends ScriptCompletionProposal {
 		public FocusedProposalSeparator() {
 			super("", 0, 0, CommonImages.getImage(CommonImages.SEPARATOR_LIST), LABEL_SEPARATOR, //$NON-NLS-1$
 					FocusedDLTKProposalProcessor.THRESHOLD_INTEREST);
 		}
 
 		@Override
 		protected boolean isSmartTrigger(char trigger) {
 			return false;
 		}
 	}
 }
