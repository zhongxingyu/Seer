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
 package org.eclipse.gmf.internal.xpand.codeassist;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.gmf.internal.xpand.expression.codeassist.ProposalComputer;
 import org.eclipse.gmf.internal.xpand.expression.codeassist.ProposalFactory;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContext;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 
 public class ExpandProposalComputer implements ProposalComputer {
 	private final static Pattern p = Pattern.compile("EXPAND\\s+(.*)\\z");
 	private final ProposalFactory proposalFactory;
 
 	public ExpandProposalComputer(ProposalFactory factory) {
 		assert factory != null;
 		proposalFactory = factory;
 	}
 	public List<ICompletionProposal> computeProposals(final String txt, final ExecutionContext ctx) {
 		Matcher m = p.matcher(txt);
 		assert m.find();
 		String templateName = m.group(1).trim();
 		// final List<ICompletionProposal> result = new
 		// ArrayList<ICompletionProposal>();
         
 		// TODO find respective templates
         
 		// return result;
     	return Collections.emptyList();
     }
 }
