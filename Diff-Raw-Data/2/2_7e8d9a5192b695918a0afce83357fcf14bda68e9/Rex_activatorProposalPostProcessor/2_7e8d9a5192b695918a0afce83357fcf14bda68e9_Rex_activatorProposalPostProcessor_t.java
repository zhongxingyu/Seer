 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.coconut.reuseextensionactivator.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.reuseware.coconut.resource.ReuseResources;
 
 /**
  * Customizes completion proposals that queries the repository for reuse extensions and takes
  * their full ID (including namespace) into account.
  */
 public class Rex_activatorProposalPostProcessor {
 	
 	/**
 	 * Adds proposals based on full IDs of registered REX files to proposal list.
 	 * 
 	 * @param proposals initial proposal list
 	 * @return extended proposal list
 	 */
 	public List<Rex_activatorCompletionProposal> process(List<Rex_activatorCompletionProposal> proposals) {
 		if (proposals.size() > 0) {
 			Rex_activatorCompletionProposal prototypeProposal = proposals.get(0);
 			if (prototypeProposal.getInsertString().equals("someRexNamespace")) {
 				List<Rex_activatorCompletionProposal> newProposals = new ArrayList<Rex_activatorCompletionProposal>();
 				for (List<String> rexID : ReuseResources.INSTANCE.getAllReuseExtensionIDs()) {
 					String insertString = null;
 					for (String segment : rexID) {
 						if (insertString == null) {
 							insertString = "";
 						} else {
 							insertString += ".";
 						}
 						insertString = insertString + segment;
 					}
 					insertString = insertString.substring(0, insertString.length() - ".rex".length());
 					String prefix = prototypeProposal.getPrefix();
 					if (!insertString.startsWith(prefix)) {
 						int idx = insertString.indexOf("." + prefix);
 						if (idx != -1) {
 							insertString = insertString.substring(idx + 1);
 						}
 					}
 					
 					if (insertString.startsWith(prefix)) {
 						Rex_activatorCompletionProposal proposal = new Rex_activatorCompletionProposal(
								null, insertString, prototypeProposal.getPrefix(), true, 
 								prototypeProposal.getStructuralFeature(), prototypeProposal.getContainer());
 						newProposals.add(proposal);							
 					}
 				}
 				return newProposals;
 			}
 		}
 		
 		return proposals;
 	}
 	
 }
