 /*
 * Copyright (c) 2005, 2009 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  */
 package org.eclipse.gmf.internal.xpand.editor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.gmf.internal.xpand.codeassist.ExpandProposalComputer;
 import org.eclipse.gmf.internal.xpand.codeassist.FastAnalyzer;
 import org.eclipse.gmf.internal.xpand.codeassist.KeywordProposalComputer;
 import org.eclipse.gmf.internal.xpand.codeassist.ProposalFactoryImpl;
 import org.eclipse.gmf.internal.xpand.codeassist.StatementProposalComputer;
 import org.eclipse.gmf.internal.xpand.codeassist.XpandPartition;
 import org.eclipse.gmf.internal.xpand.codeassist.XpandTokens;
 import org.eclipse.gmf.internal.xpand.expression.codeassist.ExpressionProposalComputer;
 import org.eclipse.gmf.internal.xpand.expression.codeassist.ProposalFactory;
 import org.eclipse.gmf.internal.xpand.expression.codeassist.TypeProposalComputer;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContext;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformationValidator;
 import org.eclipse.swt.graphics.Point;
 
 /**
  * @author Sven Efftinge
  * @author artem
  */
 public class XpandContentAssistProcessor implements IContentAssistProcessor {
 
     private final XpandEditor editor;
     private final ProposalComparator comparator;
 
     // FIXME AbstractOawContentAssistProcessor did a nature check - MOVE it Editor
 
     public XpandContentAssistProcessor(final XpandEditor editor) {
         this.editor = editor;
         this.comparator = new ProposalComparator();
     }
 
     public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int documentOffset) {
         try {
             final IDocument doc = viewer.getDocument();
 			final String txt = doc.get(0, documentOffset);
 			final int additionalTextLen = Math.min(doc.getLength(), documentOffset + doc.getLineLength(doc.getLineOfOffset(documentOffset))) - documentOffset;
 			final String textPastInsertionPoint = doc.get(documentOffset, additionalTextLen);
 
             ExecutionContext ctx = editor.createContext(); 
 
             final XpandPartition p = FastAnalyzer.computePartition(txt);
 
             if (p == XpandPartition.COMMENT) {
 				return new ICompletionProposal[0];
 			}
             List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(20);
             Point selectedRange = viewer.getSelectedRange();
             assert selectedRange.x == documentOffset; // just make sure my assumptions about selectedRange are correct
             final ProposalFactory f = new ProposalFactoryImpl(documentOffset, selectedRange.y, EditorImages.get());
 
             if (p == XpandPartition.TYPE_DECLARATION) {
                 ctx = FastAnalyzer.computeExecutionContext(txt, ctx);
                 proposals = new TypeProposalComputer(f).computeProposals(txt, ctx);
     			Collections.sort(proposals, comparator);
             } else if (p == XpandPartition.EXPRESSION) {
                 ctx = FastAnalyzer.computeExecutionContext(txt, ctx);
                 final String expression = txt.substring(txt.lastIndexOf(XpandTokens.LT_CHAR) + 1);
                 List<ICompletionProposal> ep = new ExpressionProposalComputer(f).computeProposals(expression, ctx);
     			Collections.sort(ep, comparator);
 				proposals.addAll(ep);
                 List<ICompletionProposal> kp = new KeywordProposalComputer(textPastInsertionPoint, f).computeProposals(txt, ctx);
     			Collections.sort(kp, comparator);
 				proposals.addAll(kp);
             } else if (p == XpandPartition.EXPAND_STATEMENT) {
                 ctx = FastAnalyzer.computeExecutionContext(txt, ctx);
                 List<ICompletionProposal> ep = new ExpandProposalComputer(f).computeProposals(txt, ctx);
     			Collections.sort(ep, comparator);
 				proposals.addAll(ep);
             } else if (p == XpandPartition.DEFAULT) {
                 ctx = FastAnalyzer.computeExecutionContext(txt, ctx);
                 List<ICompletionProposal> sp = new StatementProposalComputer(f).computeProposals(txt, ctx);
     			Collections.sort(sp, comparator);
 				proposals.addAll(sp);
                 proposals.add(new org.eclipse.jface.text.contentassist.CompletionProposal(XpandTokens.LT + XpandTokens.RT, documentOffset, 0, 1));
             }
             return proposals.toArray(new ICompletionProposal[proposals.size()]);
         } catch (final Exception e) {
             Activator.logError(e);
         }
         return null;
     }
 
     public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int documentOffset) {
         return null;
     }
 
     public char[] getCompletionProposalAutoActivationCharacters() {
         return null;
     }
 
     public char[] getContextInformationAutoActivationCharacters() {
         return null;
     }
 
     public String getErrorMessage() {
         return null;
     }
 
     public IContextInformationValidator getContextInformationValidator() {
     	// TODO Auto-generated method stub
     	return null;
     }
 
     private static class ProposalComparator implements Comparator<ICompletionProposal> {
         public int compare(final ICompletionProposal p1, final ICompletionProposal p2) {
         	// XXX better would be put most matching proposal first!!!
             return p1.getDisplayString().compareTo(p2.getDisplayString());
         }
     }
 }
