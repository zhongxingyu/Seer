 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.parts.directedit;
 
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.func.IProposal;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ContentProposalProvider implements IContentProposalProvider {
 
 	private IDirectEditHolder directEditHolder;
 
 	public ContentProposalProvider(IDirectEditHolder directEditHolder) {
 		this.directEditHolder = directEditHolder;
 	}
 
 	public IContentProposal[] getProposals(String contents, int position) {
 		// collect and return proposals
 
 		IContentProposal[] proposals = new IContentProposal[0];
 
 		IDirectEditingFeature def = directEditHolder.getDirectEditingFeature();
 		if (directEditHolder.isSimpleMode()) {
 			String[] proposalStrings = def.getValueProposals(contents, position, directEditHolder.getDirectEditingContext());
 
 			if (proposalStrings != null) {
 				proposals = new IContentProposal[proposalStrings.length];
 
 				for (int i = 0; i < proposalStrings.length; i++) {
 					proposals[i] = new ContentProposal(directEditHolder, position, contents, proposalStrings[i], null, null);
 				}
 			}
 		} else {
 			IProposal[] valueProposals = def.getProposalSupport().getValueProposals(contents, position,
 					directEditHolder.getDirectEditingContext());
 
 			if (valueProposals != null) {
 				proposals = new IContentProposal[valueProposals.length];
 
 				for (int i = 0; i < valueProposals.length; i++) {
					proposals[i] = new ContentProposal(directEditHolder, position, contents, valueProposals[i].getText(),
							valueProposals[i], null);
 				}
 			}
 
 		}
 
 		return proposals;
 	}
 
 }
