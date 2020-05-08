 /*
  * Copyright 2012 Barrie Treloar <barrie.treloar@gmail.com>
  *
  *  This file is part of USRPROPS Xtext Editor.
  *
  *  USRPROPS Xtext Editor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  USRPROPS Xtext Editor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with USRPROPS Xtext Editor.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.usrprops_xtext.ui.contentassist;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.Assignment;
 import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
 import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
 import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
 
 import com.github.usrprops_xtext.services.UsrpropsDslGrammarAccess;
 import com.google.inject.Inject;
 
 /**
  * see
  * http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on
  * how to customize content assistant
  */
 public class UsrpropsDslProposalProvider extends
         AbstractUsrpropsDslProposalProvider {
 
     @Inject
     UsrpropsDslGrammarAccess grammarAccess;
 
     @Override
     public void completeKeyword(Keyword keyword,
             ContentAssistContext contentAssistContext,
             ICompletionProposalAcceptor acceptor) {
         /*
          * These tokens are filtered out of content assist
          */
         if (grammarAccess.getBeginAccess().getTokenBEGINKeyword_0_1()
                 .equals(keyword)
                 || grammarAccess.getEndAccess().getTokenENDKeyword_0_1()
                         .equals(keyword)
                 || grammarAccess.getInitialAccess().getINITIALKeyword_0()
                         .equals(keyword)
                 || grammarAccess.getUpdateAccess().getUPDATEKeyword_0()
                         .equals(keyword)) {
             return;
         }
 
         super.completeKeyword(keyword, contentAssistContext, acceptor);
     }
 
     @Override
     public void completeDisplay_Legend(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
         super.completeDisplay_Legend(model, assignment, context, acceptor);
         acceptor.accept(createCompletionProposal("\"\"", context));
         acceptor.accept(createCompletionProposal("\"$$FORCE$$\"", context));
         acceptor.accept(createCompletionProposal("\"$$NONE$$\"", context));
         acceptor.accept(createCompletionProposal("\"$$VFORCE$$\"", context));
         acceptor.accept(createCompletionProposal("\"$$VNONE$$\"", context));
     }
 }
