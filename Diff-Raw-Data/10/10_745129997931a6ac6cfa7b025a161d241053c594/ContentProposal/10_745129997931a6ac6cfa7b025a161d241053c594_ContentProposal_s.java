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
 
 import org.eclipse.graphiti.func.IProposal;
 import org.eclipse.jface.fieldassist.IContentProposal;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ContentProposal implements IContentProposal {
 
 	private IDirectEditHolder directEditHolder;
 
 	private String proposalText;
 
 	private IProposal proposal;
 
 	private String currentText;
 
 	private String description;
 
 	private int cursorPosition;
 
 	public ContentProposal(IDirectEditHolder directEditHolder, int cursorPosition, String currentText, String proposalText,
 			IProposal proposal, String description) {
 		this.directEditHolder = directEditHolder;
 		this.cursorPosition = cursorPosition;
 		this.currentText = currentText;
 		this.proposalText = proposalText;
 		this.proposal = proposal;
 		this.description = description;
 	}
 
 	public String getContent() {
		String ret = directEditHolder.getDirectEditingFeature().completeValue(currentText, getCursorPosition(), proposalText,
				directEditHolder.getDirectEditingContext());
 		return ret;
 	}
 
 	public int getCursorPosition() {
 		return cursorPosition;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getLabel() {
 		return proposalText;
 	}
 
 	/**
 	 * @return the proposel
 	 */
 	public IProposal getProposal() {
 		return proposal;
 	}
 }
